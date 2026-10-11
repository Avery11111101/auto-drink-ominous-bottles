package com.shumsic.autodrinkominousbottles.config;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

public class FallbackConfigScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    public FallbackConfigScreen(Screen parent) {
        super(Component.translatable("config.auto_drink_ominous_bottles.title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
    }

    @Override
    protected void init() {
        this.clearWidgets();
        int centerX = this.width / 2;
        int leftX = centerX - 155;
        int rightX = centerX + 5;
        int colW = 150;
        int startY = 32;
        int spacing = 22;

        int leftY = startY;
        // 1. Enabled Toggle Button
        this.addRenderableWidget(Button.builder(
                getEnabledText(),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(getEnabledText());
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 2. Pull from inventory Toggle Button
        this.addRenderableWidget(Button.builder(
                getPullInventoryText(),
                button -> {
                    config.pullFromInventory = !config.pullFromInventory;
                    button.setMessage(getPullInventoryText());
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 3. Selection Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getSelectionModeText(),
                button -> {
                    ModConfig.BottleSelectionMode[] modes = ModConfig.BottleSelectionMode.values();
                    int nextIdx = (config.bottleSelectionMode.ordinal() + 1) % modes.length;
                    config.bottleSelectionMode = modes[nextIdx];
                    this.init();
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 4. Target Level Cycle Button (1~5) - Only visible if EXACT_LEVEL
        if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.EXACT_LEVEL) {
            this.addRenderableWidget(Button.builder(
                    getTargetLevelText(),
                    button -> {
                        config.targetLevel = (config.targetLevel % 5) + 1;
                        button.setMessage(getTargetLevelText());
                    }
            ).bounds(leftX, leftY, colW, 20).build());
            leftY += spacing;
        }

        // 5. Weapon Slot Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getWeaponSlotModeText(),
                button -> {
                    ModConfig.WeaponSlotMode[] modes = ModConfig.WeaponSlotMode.values();
                    int nextIdx = (config.weaponSlotMode.ordinal() + 1) % modes.length;
                    config.weaponSlotMode = modes[nextIdx];
                    this.init();
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 6. Fixed Weapon Slot Cycle Button (1~9) - Only visible if FIXED_SLOT
        if (config.weaponSlotMode == ModConfig.WeaponSlotMode.FIXED_SLOT) {
            this.addRenderableWidget(Button.builder(
                    getFixedWeaponSlotText(),
                    button -> {
                        config.fixedWeaponSlot = (config.fixedWeaponSlot % 9) + 1;
                        button.setMessage(getFixedWeaponSlotText());
                    }
            ).bounds(leftX, leftY, colW, 20).build());
            leftY += spacing;
        }

        // 7. Action Feedback Mode Button
        this.addRenderableWidget(Button.builder(
                getActionFeedbackModeText(),
                button -> {
                    ModConfig.ActionFeedbackMode[] modes = ModConfig.ActionFeedbackMode.values();
                    int nextIdx = (config.actionFeedbackMode.ordinal() + 1) % modes.length;
                    config.actionFeedbackMode = modes[nextIdx];
                    button.setMessage(getActionFeedbackModeText());
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 8. Max Drink Retries Button
        this.addRenderableWidget(Button.builder(
                getMaxDrinkRetriesText(),
                button -> {
                    config.maxDrinkRetries = (config.maxDrinkRetries % 3) + 1;
                    button.setMessage(getMaxDrinkRetriesText());
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        // 9. Raid Status HUD Enabled Toggle
        this.addRenderableWidget(Button.builder(
                getRaidStatusHudEnabledText(),
                button -> {
                    config.raidStatusHudEnabled = !config.raidStatusHudEnabled;
                    this.init();
                }
        ).bounds(leftX, leftY, colW, 20).build());
        leftY += spacing;

        if (config.raidStatusHudEnabled) {
            // 10. HUD Style Cycle
            this.addRenderableWidget(Button.builder(
                    getHudStyleText(),
                    button -> {
                        ModConfig.HudStyle[] styles = ModConfig.HudStyle.values();
                        int nextIdx = (config.hudStyle.ordinal() + 1) % styles.length;
                        config.hudStyle = styles[nextIdx];
                        button.setMessage(getHudStyleText());
                    }
            ).bounds(leftX, leftY, colW, 20).build());
            leftY += spacing;

            // 11. Consecutive Raids Count Toggle
            this.addRenderableWidget(Button.builder(
                    getShowConsecutiveRaidsCountText(),
                    button -> {
                        config.showConsecutiveRaidsCount = !config.showConsecutiveRaidsCount;
                        button.setMessage(getShowConsecutiveRaidsCountText());
                    }
            ).bounds(leftX, leftY, colW, 20).build());
            leftY += spacing;

            // 12. Adjust HUD Position Button
            this.addRenderableWidget(Button.builder(
                    Component.translatable("config.auto_drink_ominous_bottles.button.edit_hud"),
                    button -> {
                        if (this.minecraft != null && this.minecraft.gui != null) {
                            this.minecraft.gui.setScreen(new com.shumsic.autodrinkominousbottles.gui.RaidHudEditScreen(this));
                        }
                    }
            ).bounds(leftX, leftY, colW, 20).build());
            leftY += spacing;
        }

        // --- 右欄 ---
        int rightY = startY;

        // 9. Bad Omen Village Timeout (5~60s)
        this.addRenderableWidget(Button.builder(
                getBadOmenVillageTimeoutText(),
                button -> {
                    int[] timeouts = new int[]{5, 10, 15, 20, 30, 45, 60};
                    int cur = -1;
                    for (int i = 0; i < timeouts.length; i++) {
                        if (timeouts[i] == config.badOmenVillageTimeoutSeconds) {
                            cur = i;
                            break;
                        }
                    }
                    config.badOmenVillageTimeoutSeconds = timeouts[(cur + 1) % timeouts.length];
                    button.setMessage(getBadOmenVillageTimeoutText());
                }
        ).bounds(rightX, rightY, colW, 20).build());
        rightY += spacing;

        // 10. Raid Spawn Timeout (3~15s)
        this.addRenderableWidget(Button.builder(
                getRaidSpawnTimeoutText(),
                button -> {
                    int[] timeouts = new int[]{3, 5, 7, 10, 15};
                    int cur = -1;
                    for (int i = 0; i < timeouts.length; i++) {
                        if (timeouts[i] == config.raidSpawnTimeoutSeconds) {
                            cur = i;
                            break;
                        }
                    }
                    config.raidSpawnTimeoutSeconds = timeouts[(cur + 1) % timeouts.length];
                    button.setMessage(getRaidSpawnTimeoutText());
                }
        ).bounds(rightX, rightY, colW, 20).build());
        rightY += spacing;

        // 11. Max Spawn Failures (1~5)
        this.addRenderableWidget(Button.builder(
                getMaxSpawnFailuresText(),
                button -> {
                    config.maxSpawnFailures = (config.maxSpawnFailures % 5) + 1;
                    button.setMessage(getMaxSpawnFailuresText());
                }
        ).bounds(rightX, rightY, colW, 20).build());
        rightY += spacing;

        // 12. Auto Eat Enabled Toggle Button
        this.addRenderableWidget(Button.builder(
                getAutoEatEnabledText(),
                button -> {
                    config.autoEatEnabled = !config.autoEatEnabled;
                    this.init();
                }
        ).bounds(rightX, rightY, colW, 20).build());
        rightY += spacing;

        if (config.autoEatEnabled) {
            // 13. Auto Eat Hunger Threshold Cycle Button
            this.addRenderableWidget(Button.builder(
                    getAutoEatHungerThresholdText(),
                    button -> {
                        int[] thresholds = new int[]{6, 8, 10, 12, 14, 16, 18, 19};
                        int currentIdx = -1;
                        for (int i = 0; i < thresholds.length; i++) {
                            if (thresholds[i] == config.autoEatHungerThreshold) {
                                currentIdx = i;
                                break;
                            }
                        }
                        int nextIdx = (currentIdx + 1) % thresholds.length;
                        config.autoEatHungerThreshold = thresholds[nextIdx];
                        button.setMessage(getAutoEatHungerThresholdText());
                    }
            ).bounds(rightX, rightY, colW, 20).build());
            rightY += spacing;

            // 14. Auto Eat Pull From Inventory Toggle Button
            this.addRenderableWidget(Button.builder(
                    getAutoEatPullInventoryText(),
                    button -> {
                        config.autoEatPullFromInventory = !config.autoEatPullFromInventory;
                        button.setMessage(getAutoEatPullInventoryText());
                    }
            ).bounds(rightX, rightY, colW, 20).build());
            rightY += spacing;

            // 15. Auto Eat Ignore Harmful Food Toggle Button
            this.addRenderableWidget(Button.builder(
                    getAutoEatIgnoreHarmfulText(),
                    button -> {
                        config.autoEatIgnoreHarmful = !config.autoEatIgnoreHarmful;
                        button.setMessage(getAutoEatIgnoreHarmfulText());
                    }
            ).bounds(rightX, rightY, colW, 20).build());
            rightY += spacing;
        }

        // Done / Save Button
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    config.validate();
                    config.save();
                    if (this.minecraft != null && this.minecraft.gui != null) {
                        this.minecraft.gui.setScreen(this.parent);
                    }
                }
        ).bounds(centerX - 100, this.height - 28, 200, 20).build());
    }

    private Component getBadOmenVillageTimeoutText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.bad_omen_village_timeout")
                .append(": ")
                .append(String.valueOf(config.badOmenVillageTimeoutSeconds))
                .append("s");
    }

    private Component getRaidSpawnTimeoutText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.raid_spawn_timeout")
                .append(": ")
                .append(String.valueOf(config.raidSpawnTimeoutSeconds))
                .append("s");
    }

    private Component getMaxSpawnFailuresText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.max_spawn_failures")
                .append(": ")
                .append(String.valueOf(config.maxSpawnFailures));
    }

    private Component getEnabledText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.enabled")
                .append(": ")
                .append(config.enabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getPullInventoryText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.pull_from_inventory")
                .append(": ")
                .append(config.pullFromInventory ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getSelectionModeText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.bottle_selection_mode")
                .append(": ")
                .append(Component.translatable("config.auto_drink_ominous_bottles.mode." + config.bottleSelectionMode.name().toLowerCase()));
    }

    private Component getTargetLevelText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.target_level")
                .append(": Level ")
                .append(String.valueOf(config.targetLevel));
    }

    private Component getWeaponSlotModeText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.weapon_slot_mode")
                .append(": ")
                .append(Component.translatable("config.auto_drink_ominous_bottles.weapon_mode." + config.weaponSlotMode.name().toLowerCase()));
    }

    private Component getFixedWeaponSlotText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.fixed_weapon_slot")
                .append(": Slot ")
                .append(String.valueOf(config.fixedWeaponSlot));
    }

    private Component getActionFeedbackModeText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.action_feedback_mode")
                .append(": ")
                .append(Component.translatable("config.auto_drink_ominous_bottles.feedback_mode." + config.actionFeedbackMode.name().toLowerCase()));
    }

    private Component getMaxDrinkRetriesText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.max_drink_retries")
                .append(": ")
                .append(String.valueOf(config.maxDrinkRetries));
    }

    private Component getAutoEatEnabledText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_enabled")
                .append(": ")
                .append(config.autoEatEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getAutoEatHungerThresholdText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_hunger_threshold")
                .append(": ")
                .append(String.valueOf(config.autoEatHungerThreshold))
                .append(" / 20");
    }

    private Component getAutoEatPullInventoryText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_pull_from_inventory")
                .append(": ")
                .append(config.autoEatPullFromInventory ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getAutoEatIgnoreHarmfulText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_ignore_harmful")
                .append(": ")
                .append(config.autoEatIgnoreHarmful ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getRaidStatusHudEnabledText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.hud_enabled")
                .append(": ")
                .append(config.raidStatusHudEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getHudStyleText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.hud_style")
                .append(": ")
                .append(Component.translatable("config.auto_drink_ominous_bottles.hud_style." + config.hudStyle.name().toLowerCase()));
    }

    private Component getShowConsecutiveRaidsCountText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.hud_consecutive")
                .append(": ")
                .append(config.showConsecutiveRaidsCount ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
    }

    @Override
    public void onClose() {
        config.validate();
        config.save();
        if (this.minecraft != null && this.minecraft.gui != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
