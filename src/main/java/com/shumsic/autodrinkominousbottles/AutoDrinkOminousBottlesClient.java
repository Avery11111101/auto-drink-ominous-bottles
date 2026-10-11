package com.shumsic.autodrinkominousbottles;

import com.mojang.blaze3d.platform.InputConstants;
import com.shumsic.autodrinkominousbottles.config.ModConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;
import net.minecraft.world.item.consume_effects.ConsumeEffect;

import java.lang.reflect.Field;
import java.util.Map;

public class AutoDrinkOminousBottlesClient implements ClientModInitializer {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("auto_drink_ominous_bottles", "category")
    );

    private enum ActionType {
        NONE,
        DRINKING_BOTTLE,
        EATING_FOOD
    }

    // Timings
    private static final int BOTTLE_HOLD_USE_TICKS = 45;
    private static final int SWAP_BACK_DELAY_TICKS = 3;
    private static final int COOLDOWN_TICKS = 0;

    private int ticksToReleaseUse = -1;
    private int actionHoldTicks = 0;
    private int ticksToSwapBack = -1;
    private int targetRestoreHotbarSlot = -1;
    private boolean useWasForced = false;
    private ActionType currentAction = ActionType.NONE;

    private long lastTriggerTick = -1000;
    private boolean victoryVisibleLastTick = false;
    private boolean firedThisVictory = false;

    private int victoryMissingTicks = 0;
    private int triggerLockTicks = 0;

    // 常態維持突襲與超時判定狀態
    private int consecutiveSpawnFailures = 0;
    private int badOmenTicks = 0;
    private boolean hadRaidOmen = false;
    private boolean waitingAfterRaidOmenExpired = false;
    private int raidSpawnWaitTicks = 0;

    @Override
    public void onInitializeClient() {
        ModConfig config = ModConfig.getInstance();

        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.auto_drink_ominous_bottles.toggle",
            InputConstants.Type.KEYBOARD,
            InputConstants.KEY_R,
            CATEGORY
        ));

        net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("auto_drink_ominous_bottles", "raid_status_hud"),
            new com.shumsic.autodrinkominousbottles.hud.RaidStatusHudRenderer()
        );

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggle.consumeClick()) {
                config.enabled = !config.enabled;
                config.save();
                if (!config.enabled) {
                    com.shumsic.autodrinkominousbottles.hud.RaidStatusTracker.getInstance().resetConsecutiveCount();
                }
                if (client.player != null) {
                    Component stateMsg = config.enabled
                            ? Component.translatable("message.auto_drink_ominous_bottles.enabled")
                            : Component.translatable("message.auto_drink_ominous_bottles.disabled");
                    client.player.sendOverlayMessage(stateMsg);
                }
                playAlertSound(client, config.enabled);
                firedThisVictory = false;
                victoryVisibleLastTick = false;
                consecutiveSpawnFailures = 0;
                badOmenTicks = 0;
                hadRaidOmen = false;
                waitingAfterRaidOmenExpired = false;
                raidSpawnWaitTicks = 0;
                cancelCurrentAction(client);
            }

            if (client.player == null || client.level == null || client.gameMode == null) return;

            if (triggerLockTicks > 0) triggerLockTicks--;

            // Raid Bar detection (Victory or Active)
            boolean victoryNow = isRaidVictoryBarVisible(client);
            boolean raidActiveNow = isRaidActiveBarVisible(client);
            boolean isBusy = useWasForced || (ticksToSwapBack >= 0);

            // 更新 HUD 狀態追蹤器
            com.shumsic.autodrinkominousbottles.hud.RaidStatusTracker.getInstance().tick(client);

            if (raidActiveNow) {
                // 突襲進行中：重置失敗計數與凶兆等候計時
                consecutiveSpawnFailures = 0;
                badOmenTicks = 0;
                hadRaidOmen = false;
                waitingAfterRaidOmenExpired = false;
                raidSpawnWaitTicks = 0;
            }

            if (victoryNow) {
                victoryMissingTicks = 0;
                consecutiveSpawnFailures = 0;
                badOmenTicks = 0;
                hadRaidOmen = false;
                waitingAfterRaidOmenExpired = false;
                raidSpawnWaitTicks = 0;

                // 勝利瞬間無縫接續下一輪突襲
                if (!victoryVisibleLastTick && !firedThisVictory && triggerLockTicks == 0 && config.enabled && !isBusy) {
                    com.shumsic.autodrinkominousbottles.hud.RaidStatusTracker.getInstance().onRaidVictory();
                    tryDrink(client, config, false);
                }
            } else {
                victoryMissingTicks++;
                if (victoryMissingTicks >= 40) { // ~2 seconds at 20 TPS
                    firedThisVictory = false;
                }
            }
            victoryVisibleLastTick = victoryNow;

            // 常態維持突襲循環與生怪超時判定
            if (config.enabled && !raidActiveNow && !victoryNow) {
                boolean hasBadOmen = client.player.hasEffect(MobEffects.BAD_OMEN);
                boolean hasRaidOmen = client.player.hasEffect(MobEffects.RAID_OMEN);

                if (hasRaidOmen) {
                    // (A) 身上有突襲凶兆倒數中：記錄旗標並靜候倒數
                    hadRaidOmen = true;
                    badOmenTicks = 0;
                    waitingAfterRaidOmenExpired = false;
                    raidSpawnWaitTicks = 0;
                } else if (hasBadOmen) {
                    // (B) 身上有普通不祥之兆：累計計時，防呆不在村莊
                    hadRaidOmen = false;
                    waitingAfterRaidOmenExpired = false;
                    raidSpawnWaitTicks = 0;
                    badOmenTicks++;

                    int timeoutTicks = config.badOmenVillageTimeoutSeconds * 20;
                    if (badOmenTicks >= timeoutTicks) {
                        // 超時未轉化為突襲凶兆，判定不在村莊
                        config.enabled = false;
                        config.save();
                        Component alertMsg = Component.translatable(
                                "message.auto_drink_ominous_bottles.alert.not_in_village",
                                config.badOmenVillageTimeoutSeconds
                        );
                        client.player.sendOverlayMessage(alertMsg);
                        client.player.sendSystemMessage(alertMsg);
                        playAlertSound(client, false);
                        cancelCurrentAction(client);
                        badOmenTicks = 0;
                    }
                } else {
                    // (C) 身上無任何凶兆效果
                    badOmenTicks = 0;

                    if (hadRaidOmen) {
                        // 突襲凶兆剛好在上一刻倒數完畢，進入等待突襲 BossBar 出現的緩衝期
                        hadRaidOmen = false;
                        waitingAfterRaidOmenExpired = true;
                        raidSpawnWaitTicks = 0;
                    }

                    if (waitingAfterRaidOmenExpired) {
                        // 等待突襲生成中
                        raidSpawnWaitTicks++;
                        int spawnTimeoutTicks = config.raidSpawnTimeoutSeconds * 20;
                        if (raidSpawnWaitTicks >= spawnTimeoutTicks && !isBusy) {
                            // 超時仍未出現突襲，判定為「找不到生成點」
                            waitingAfterRaidOmenExpired = false;
                            raidSpawnWaitTicks = 0;
                            consecutiveSpawnFailures++;

                            if (consecutiveSpawnFailures < config.maxSpawnFailures) {
                                // 嘗試重喝
                                Component retryMsg = Component.translatable(
                                        "message.auto_drink_ominous_bottles.alert.spawn_failed_retry",
                                        consecutiveSpawnFailures
                                );
                                client.player.sendOverlayMessage(retryMsg);
                                client.player.sendSystemMessage(retryMsg);
                                tryDrink(client, config, true);
                            } else {
                                // 連續失敗達到上限，自動關閉模組！
                                config.enabled = false;
                                config.save();
                                com.shumsic.autodrinkominousbottles.hud.RaidStatusTracker.getInstance().resetConsecutiveCount();
                                Component disabledMsg = Component.translatable(
                                        "message.auto_drink_ominous_bottles.alert.spawn_failed_disabled",
                                        consecutiveSpawnFailures
                                );
                                client.player.sendOverlayMessage(disabledMsg);
                                client.player.sendSystemMessage(disabledMsg);
                                playAlertSound(client, false);
                                cancelCurrentAction(client);
                                consecutiveSpawnFailures = 0;
                            }
                        }
                    } else {
                        // (D) 當前無突襲、無凶兆，主動喝瓶以長期維持突襲狀態！
                        if (!isBusy && triggerLockTicks == 0 && client.player.isAlive() && !client.player.isSpectator()) {
                            tryDrink(client, config, false);
                        }
                    }
                }
            }

            // Auto Eat Check (if not drinking bottle and not currently locked in an action)
            if (!isBusy && config.autoEatEnabled && client.player.isAlive() && !client.player.isSpectator()) {
                if (client.player.getFoodData().getFoodLevel() <= config.autoEatHungerThreshold) {
                    tryEat(client, config);
                }
            }

            // Maintain hold-to-use and swap-back
            if (useWasForced) {
                actionHoldTicks++;
                // 每次 tick 確保按鍵狀態維持（防禦 GUI 切換或失去焦點時觸發 releaseAll）
                client.options.keyUse.setDown(true);

                // 飲用/進食完成判定條件：
                // 1. 至少維持 MIN_USE_PROTECT_TICKS (20 ticks / 1秒)，避免剛開始使用時的狀態波動提早換手
                // 2. 當客戶端確認玩家已經不再使用物品（且已過保護期），或者剩餘 tick <= 1，或者達到最大保護時長超時
                boolean finishedNaturally = (actionHoldTicks >= 20 && !client.player.isUsingItem());
                boolean finishedByRemaining = (client.player.isUsingItem() && client.player.getUseItemRemainingTicks() <= 1);
                boolean timeout = (ticksToReleaseUse <= 0);

                if (timeout || finishedNaturally || finishedByRemaining) {
                    client.options.keyUse.setDown(false);
                    useWasForced = false;
                    actionHoldTicks = 0;
                    ticksToSwapBack = SWAP_BACK_DELAY_TICKS;
                } else {
                    ticksToReleaseUse--;
                }
            } else if (ticksToSwapBack >= 0) {
                if (ticksToSwapBack == 0) {
                    if (targetRestoreHotbarSlot >= 0 && targetRestoreHotbarSlot < 9) {
                        client.player.getInventory().setSelectedSlot(targetRestoreHotbarSlot);
                        if (client.getConnection() != null) {
                            client.getConnection().send(new ServerboundSetCarriedItemPacket(targetRestoreHotbarSlot));
                        }
                    }
                    targetRestoreHotbarSlot = -1;
                    ticksToSwapBack = -1;
                    currentAction = ActionType.NONE;
                } else {
                    ticksToSwapBack--;
                }
            }
        });
    }

    private void playAlertSound(Minecraft client, boolean success) {
        if (client != null && client.getSoundManager() != null) {
            try {
                if (success) {
                    client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_PLING.value(), 1.2F, 0.6F));
                } else {
                    client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.NOTE_BLOCK_BASS.value(), 0.5F, 1.0F));
                }
            } catch (Exception ignored) {}
        }
    }

    private void cancelCurrentAction(Minecraft client) {
        if (useWasForced) {
            client.options.keyUse.setDown(false);
            useWasForced = false;
        }
        if (targetRestoreHotbarSlot >= 0 && targetRestoreHotbarSlot < 9 && client.player != null) {
            client.player.getInventory().setSelectedSlot(targetRestoreHotbarSlot);
            if (client.getConnection() != null) {
                client.getConnection().send(new ServerboundSetCarriedItemPacket(targetRestoreHotbarSlot));
            }
        }
        ticksToReleaseUse = -1;
        actionHoldTicks = 0;
        ticksToSwapBack = -1;
        targetRestoreHotbarSlot = -1;
        currentAction = ActionType.NONE;
    }

    private boolean isRaidVictoryBarVisible(Minecraft client) {
        try {
            if (client.gui == null || client.gui.hud == null) return false;
            BossHealthOverlay hud = client.gui.hud.getBossOverlay();
            if (hud == null) return false;
            boolean sawMap = false;

            for (Field f : hud.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(hud);
                if (!(v instanceof Map<?, ?> m)) continue;

                sawMap = true;
                for (Object o : m.values()) {
                    if (o instanceof LerpingBossEvent bar) {
                        if (RaidMessageDetector.isRaidVictory(bar.getName())) return true;
                    }
                }
            }

            if (sawMap) return false;
        } catch (Throwable ignored) { }
        return false;
    }

    private boolean isRaidActiveBarVisible(Minecraft client) {
        try {
            if (client.gui == null || client.gui.hud == null) return false;
            BossHealthOverlay hud = client.gui.hud.getBossOverlay();
            if (hud == null) return false;
            boolean sawMap = false;

            for (Field f : hud.getClass().getDeclaredFields()) {
                f.setAccessible(true);
                Object v = f.get(hud);
                if (!(v instanceof Map<?, ?> m)) continue;

                sawMap = true;
                for (Object o : m.values()) {
                    if (o instanceof LerpingBossEvent bar) {
                        if (RaidMessageDetector.isRaidActive(bar.getName())) return true;
                    }
                }
            }

            if (sawMap) return false;
        } catch (Throwable ignored) { }
        return false;
    }

    private void tryDrink(Minecraft client, ModConfig config, boolean isRetry) {
        // High priority: interrupt eating if currently eating
        if (currentAction == ActionType.EATING_FOOD) {
            cancelCurrentAction(client);
        }

        if (!isRetry) {
            firedThisVictory = true;
        }
        triggerLockTicks = 60; // ~3 seconds lock

        long now = client.level.getGameTime();
        if (now - lastTriggerTick < COOLDOWN_TICKS) return;
        lastTriggerTick = now;

        int hotbarSlot = selectBottleHotbarSlot(client, config);

        if (hotbarSlot == -1 && config.pullFromInventory) {
            int invSlotId = selectBottleInventorySlotId(client, config); // slot id for SWAP (9..35)
            if (invSlotId != -1) {
                int targetHotbar = firstEmptyHotbarSlot(client);
                if (targetHotbar == -1) targetHotbar = (client.player.getInventory().getSelectedSlot() + 1) % 9;

                try {
                    client.gameMode.handleContainerInput(
                        client.player.containerMenu.containerId,
                        invSlotId,
                        targetHotbar,
                        ContainerInput.SWAP,
                        client.player
                    );
                    hotbarSlot = targetHotbar;
                } catch (Throwable ignored) {
                    hotbarSlot = -1;
                }
            }
        }

        if (hotbarSlot == -1) {
            if (config.pullFromInventory) {
                say(client, Component.translatable("message.auto_drink_ominous_bottles.none_found"));
            } else {
                say(client, Component.translatable("message.auto_drink_ominous_bottles.none_in_hotbar"));
            }
            triggerLockTicks = 100; // 5 秒防提示洗版
            return;
        }

        int bottleLevel = getOminousLevel(client.player.getInventory().getItem(hotbarSlot));
        sendActionFeedback(client, config, Component.translatable("message.auto_drink_ominous_bottles.action.drinking", bottleLevel));

        int previousSlot = client.player.getInventory().getSelectedSlot();
        if (config.weaponSlotMode == ModConfig.WeaponSlotMode.FIXED_SLOT) {
            targetRestoreHotbarSlot = config.fixedWeaponSlot - 1; // 1-based to 0-based
        } else {
            targetRestoreHotbarSlot = previousSlot;
        }

        client.player.getInventory().setSelectedSlot(hotbarSlot);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
        }

        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        client.options.keyUse.setDown(true);
        useWasForced = true;
        actionHoldTicks = 0;
        currentAction = ActionType.DRINKING_BOTTLE;
        ticksToReleaseUse = BOTTLE_HOLD_USE_TICKS;
    }

    private int selectBottleHotbarSlot(Minecraft client, ModConfig config) {
        int bestSlot = -1;
        int bestLevel = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack st = client.player.getInventory().getItem(i);
            if (!isOminousBottle(st)) continue;
            int lvl = getOminousLevel(st);

            if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.EXACT_LEVEL) {
                if (lvl == config.targetLevel) {
                    return i;
                }
            } else if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.LOWEST) {
                if (bestSlot == -1 || lvl < bestLevel) {
                    bestSlot = i;
                    bestLevel = lvl;
                }
            } else { // HIGHEST
                if (bestSlot == -1 || lvl > bestLevel) {
                    bestSlot = i;
                    bestLevel = lvl;
                }
            }
        }
        return bestSlot;
    }

    private int selectBottleInventorySlotId(Minecraft client, ModConfig config) {
        int bestSlotId = -1;
        int bestLevel = -1;

        for (int slotId = 9; slotId <= 35; slotId++) {
            ItemStack st = client.player.getInventory().getItem(slotId);
            if (!isOminousBottle(st)) continue;
            int lvl = getOminousLevel(st);

            if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.EXACT_LEVEL) {
                if (lvl == config.targetLevel) {
                    return slotId;
                }
            } else if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.LOWEST) {
                if (bestSlotId == -1 || lvl < bestLevel) {
                    bestSlotId = slotId;
                    bestLevel = lvl;
                }
            } else { // HIGHEST
                if (bestSlotId == -1 || lvl > bestLevel) {
                    bestSlotId = slotId;
                    bestLevel = lvl;
                }
            }
        }
        return bestSlotId;
    }

    private int firstEmptyHotbarSlot(Minecraft client) {
        for (int i = 0; i < 9; i++) {
            if (client.player.getInventory().getItem(i).isEmpty()) return i;
        }
        return -1;
    }

    private boolean isOminousBottle(ItemStack st) {
        return st != null && !st.isEmpty() && st.getItem() == Items.OMINOUS_BOTTLE;
    }

    private int getOminousLevel(ItemStack st) {
        try {
            OminousBottleAmplifier comp = st.get(DataComponents.OMINOUS_BOTTLE_AMPLIFIER);
            int amp = comp != null ? comp.value() : 0;
            return 1 + amp;
        } catch (Throwable ignored) {
            return 1;
        }
    }

    private void tryEat(Minecraft client, ModConfig config) {
        int hotbarSlot = selectFoodHotbarSlot(client, config);

        if (hotbarSlot == -1 && config.autoEatPullFromInventory) {
            int invSlotId = selectFoodInventorySlotId(client, config); // slot id for SWAP (9..35)
            if (invSlotId != -1) {
                int targetHotbar = firstEmptyHotbarSlot(client);
                if (targetHotbar == -1) targetHotbar = (client.player.getInventory().getSelectedSlot() + 1) % 9;

                try {
                    client.gameMode.handleContainerInput(
                        client.player.containerMenu.containerId,
                        invSlotId,
                        targetHotbar,
                        ContainerInput.SWAP,
                        client.player
                    );
                    hotbarSlot = targetHotbar;
                } catch (Throwable ignored) {
                    hotbarSlot = -1;
                }
            }
        }

        if (hotbarSlot == -1) return;

        ItemStack foodStack = client.player.getInventory().getItem(hotbarSlot);
        Consumable consumable = foodStack.get(DataComponents.CONSUMABLE);
        int consumeTicks = (consumable != null) ? consumable.consumeTicks() + 4 : 36;

        sendActionFeedback(client, config, Component.translatable("message.auto_drink_ominous_bottles.action.eating", foodStack.getHoverName()));

        int previousSlot = client.player.getInventory().getSelectedSlot();
        if (config.weaponSlotMode == ModConfig.WeaponSlotMode.FIXED_SLOT) {
            targetRestoreHotbarSlot = config.fixedWeaponSlot - 1;
        } else {
            targetRestoreHotbarSlot = previousSlot;
        }

        client.player.getInventory().setSelectedSlot(hotbarSlot);
        if (client.getConnection() != null) {
            client.getConnection().send(new ServerboundSetCarriedItemPacket(hotbarSlot));
        }

        client.gameMode.useItem(client.player, InteractionHand.MAIN_HAND);
        client.options.keyUse.setDown(true);
        useWasForced = true;
        actionHoldTicks = 0;
        currentAction = ActionType.EATING_FOOD;
        ticksToReleaseUse = consumeTicks;
    }

    private void sendActionFeedback(Minecraft client, ModConfig config, Component message) {
        if (client.player == null || config.actionFeedbackMode == ModConfig.ActionFeedbackMode.DISABLED) return;
        if (config.actionFeedbackMode == ModConfig.ActionFeedbackMode.ACTION_BAR) {
            client.player.sendOverlayMessage(message);
        } else {
            client.player.sendSystemMessage(message);
        }
    }

    private int selectFoodHotbarSlot(Minecraft client, ModConfig config) {
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack st = client.player.getInventory().getItem(i);
            if (!isEligibleFood(client, st, config)) continue;
            int score = getFoodScore(st);
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }
        return bestSlot;
    }

    private int selectFoodInventorySlotId(Minecraft client, ModConfig config) {
        int bestSlotId = -1;
        int bestScore = -1;

        for (int slotId = 9; slotId <= 35; slotId++) {
            ItemStack st = client.player.getInventory().getItem(slotId);
            if (!isEligibleFood(client, st, config)) continue;
            int score = getFoodScore(st);
            if (score > bestScore) {
                bestScore = score;
                bestSlotId = slotId;
            }
        }
        return bestSlotId;
    }

    private boolean isEligibleFood(Minecraft client, ItemStack st, ModConfig config) {
        if (st == null || st.isEmpty()) return false;
        if (!st.has(DataComponents.FOOD) || !st.has(DataComponents.CONSUMABLE)) return false;

        // Skip non-edible right now
        FoodProperties food = st.get(DataComponents.FOOD);
        if (food == null || !client.player.canEat(food.canAlwaysEat())) return false;

        // User Decisions: Skip golden apple / enchanted golden apple to avoid waste
        if (st.getItem() == Items.GOLDEN_APPLE || st.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
            return false;
        }

        // User Decisions: Skip chorus fruit to avoid accidental teleportation
        if (st.getItem() == Items.CHORUS_FRUIT) {
            return false;
        }

        // Avoid harmful food effects if enabled (e.g. Rotten Flesh, Pufferfish, Poisonous Potato, Spider Eye)
        if (config.autoEatIgnoreHarmful) {
            Consumable consumable = st.get(DataComponents.CONSUMABLE);
            if (consumable != null) {
                for (ConsumeEffect effect : consumable.onConsumeEffects()) {
                    if (effect instanceof ApplyStatusEffectsConsumeEffect applyEffects) {
                        for (MobEffectInstance instance : applyEffects.effects()) {
                            if (instance.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                                return false;
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    private int getFoodScore(ItemStack st) {
        FoodProperties food = st.get(DataComponents.FOOD);
        if (food == null) return 0;
        // Score primarily by nutrition, secondary by saturation
        return food.nutrition() * 10 + Math.round(food.saturation() * 10.0f);
    }

    private void say(Minecraft client, Component msg) {
        if (client.player != null) client.player.sendSystemMessage(msg);
    }
}
