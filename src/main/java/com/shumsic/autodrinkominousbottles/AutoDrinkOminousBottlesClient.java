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
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import org.lwjgl.glfw.GLFW;

import java.lang.reflect.Field;
import java.util.Map;

public class AutoDrinkOminousBottlesClient implements ClientModInitializer {
    public static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("auto_drink_ominous_bottles", "category")
    );

    // Stable timings
    private static final int HOLD_USE_TICKS = 45;
    private static final int SWAP_BACK_DELAY_TICKS = 3;
    private static final int COOLDOWN_TICKS = 0;

    private int ticksToReleaseUse = -1;
    private int ticksToSwapBack = -1;
    private int targetRestoreHotbarSlot = -1;
    private boolean useWasForced = false;

    private long lastTriggerTick = -1000;
    private boolean victoryVisibleLastTick = false;
    private boolean firedThisVictory = false;

    private int victoryMissingTicks = 0;
    private int triggerLockTicks = 0;

    // Auto Attack state
    private int autoAttackTimerTicks = 0;
    private int lastAttackedEntityId = -1;

    @Override
    public void onInitializeClient() {
        ModConfig config = ModConfig.getInstance();

        KeyMapping toggle = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.auto_drink_ominous_bottles.toggle",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            CATEGORY
        ));

        KeyMapping toggleAttack = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.auto_drink_ominous_bottles.toggle_attack",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggle.consumeClick()) {
                config.enabled = !config.enabled;
                config.save();
                if (client.player != null) {
                    Component stateMsg = config.enabled
                            ? Component.translatable("message.auto_drink_ominous_bottles.enabled")
                            : Component.translatable("message.auto_drink_ominous_bottles.disabled");
                    client.player.sendOverlayMessage(stateMsg);
                }
                firedThisVictory = false;
                victoryVisibleLastTick = false;
                useWasForced = false;
                ticksToReleaseUse = -1;
                ticksToSwapBack = -1;
                targetRestoreHotbarSlot = -1;
            }

            while (toggleAttack.consumeClick()) {
                config.autoAttackEnabled = !config.autoAttackEnabled;
                config.save();
                if (client.player != null) {
                    Component attackMsg = config.autoAttackEnabled
                            ? Component.translatable("message.auto_drink_ominous_bottles.attack_enabled")
                            : Component.translatable("message.auto_drink_ominous_bottles.attack_disabled");
                    client.player.sendOverlayMessage(attackMsg);
                }
                lastAttackedEntityId = -1;
                autoAttackTimerTicks = 0;
            }

            if (client.player == null || client.level == null || client.gameMode == null) return;

            // Auto Attack Logic
            boolean isDrinking = useWasForced || (ticksToSwapBack >= 0);
            if (config.autoAttackEnabled && client.player.isAlive() && (client.gui == null || client.gui.screen() == null) && !isDrinking) {
                if (client.hitResult != null && client.hitResult.getType() == HitResult.Type.ENTITY) {
                    EntityHitResult entityHit = (EntityHitResult) client.hitResult;
                    Entity targetEntity = entityHit.getEntity();
                    if (targetEntity != null && targetEntity.isAlive() && targetEntity.isAttackable()) {
                        int targetId = targetEntity.getId();
                        boolean shouldAttack = false;

                        if (config.autoAttackMode == ModConfig.AutoAttackMode.FULL_COOLDOWN) {
                            if (client.player.getAttackStrengthScale(0.0f) >= 1.0f) {
                                shouldAttack = true;
                            }
                        } else if (config.autoAttackMode == ModConfig.AutoAttackMode.CUSTOM_INTERVAL) {
                            autoAttackTimerTicks++;
                            if (autoAttackTimerTicks >= config.autoAttackIntervalTicks) {
                                shouldAttack = true;
                                autoAttackTimerTicks = 0;
                            }
                        } else if (config.autoAttackMode == ModConfig.AutoAttackMode.ONCE_ON_AIM) {
                            if (lastAttackedEntityId != targetId) {
                                shouldAttack = true;
                                lastAttackedEntityId = targetId;
                            }
                        }

                        if (shouldAttack) {
                            client.gameMode.attack(client.player, targetEntity);
                            client.player.swing(InteractionHand.MAIN_HAND);
                        }
                    } else {
                        lastAttackedEntityId = -1;
                    }
                } else {
                    lastAttackedEntityId = -1;
                }
            } else {
                lastAttackedEntityId = -1;
                autoAttackTimerTicks = 0;
            }

            if (!config.enabled) return;

            if (triggerLockTicks > 0) triggerLockTicks--;

            boolean victoryNow = isRaidVictoryBarVisible(client);

            if (victoryNow) {
                victoryMissingTicks = 0;
                if (!victoryVisibleLastTick && !firedThisVictory && triggerLockTicks == 0) {
                    tryDrink(client, config);
                }
            } else {
                victoryMissingTicks++;
                if (victoryMissingTicks >= 40) { // ~2 seconds at 20 TPS
                    firedThisVictory = false;
                }
            }

            victoryVisibleLastTick = victoryNow;

            // Maintain hold-to-use and swap-back
            if (useWasForced) {
                if (ticksToReleaseUse <= 0 || !client.player.isUsingItem() || (client.player.isUsingItem() && client.player.getUseItemRemainingTicks() <= 1)) {
                    client.options.keyUse.setDown(false);
                    useWasForced = false;
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
                } else {
                    ticksToSwapBack--;
                }
            }
        });
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

    private void tryDrink(Minecraft client, ModConfig config) {
        firedThisVictory = true;
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
            return;
        }

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
        ticksToReleaseUse = HOLD_USE_TICKS;
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

    private void say(Minecraft client, Component msg) {
        if (client.player != null) client.player.sendSystemMessage(msg);
    }
}
