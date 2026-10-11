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
        int startY = 36;
        int spacing = 22;
        int currentY = startY;

        // 1. Enabled Toggle Button
        this.addRenderableWidget(Button.builder(
                getEnabledText(),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(getEnabledText());
                }
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 2. Pull from inventory Toggle Button
        this.addRenderableWidget(Button.builder(
                getPullInventoryText(),
                button -> {
                    config.pullFromInventory = !config.pullFromInventory;
                    button.setMessage(getPullInventoryText());
                }
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 3. Selection Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getSelectionModeText(),
                button -> {
                    ModConfig.BottleSelectionMode[] modes = ModConfig.BottleSelectionMode.values();
                    int nextIdx = (config.bottleSelectionMode.ordinal() + 1) % modes.length;
                    config.bottleSelectionMode = modes[nextIdx];
                    this.init();
                }
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 4. Target Level Cycle Button (1~5) - Only visible if EXACT_LEVEL
        if (config.bottleSelectionMode == ModConfig.BottleSelectionMode.EXACT_LEVEL) {
            this.addRenderableWidget(Button.builder(
                    getTargetLevelText(),
                    button -> {
                        config.targetLevel = (config.targetLevel % 5) + 1;
                        button.setMessage(getTargetLevelText());
                    }
            ).bounds(centerX - 120, currentY, 240, 20).build());
            currentY += spacing;
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
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 6. Fixed Weapon Slot Cycle Button (1~9) - Only visible if FIXED_SLOT
        if (config.weaponSlotMode == ModConfig.WeaponSlotMode.FIXED_SLOT) {
            this.addRenderableWidget(Button.builder(
                    getFixedWeaponSlotText(),
                    button -> {
                        config.fixedWeaponSlot = (config.fixedWeaponSlot % 9) + 1;
                        button.setMessage(getFixedWeaponSlotText());
                    }
            ).bounds(centerX - 120, currentY, 240, 20).build());
            currentY += spacing;
        }

        // 7. Auto Attack Enabled Toggle Button
        this.addRenderableWidget(Button.builder(
                getAutoAttackEnabledText(),
                button -> {
                    config.autoAttackEnabled = !config.autoAttackEnabled;
                    button.setMessage(getAutoAttackEnabledText());
                }
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 8. Auto Attack Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getAutoAttackModeText(),
                button -> {
                    ModConfig.AutoAttackMode[] modes = ModConfig.AutoAttackMode.values();
                    int nextIdx = (config.autoAttackMode.ordinal() + 1) % modes.length;
                    config.autoAttackMode = modes[nextIdx];
                    this.init();
                }
        ).bounds(centerX - 120, currentY, 240, 20).build());
        currentY += spacing;

        // 9. Auto Attack Interval Cycle Button - Only visible if CUSTOM_INTERVAL
        if (config.autoAttackMode == ModConfig.AutoAttackMode.CUSTOM_INTERVAL) {
            this.addRenderableWidget(Button.builder(
                    getAutoAttackIntervalText(),
                    button -> {
                        int[] commonIntervals = new int[]{1, 2, 4, 6, 8, 10, 12, 15, 20, 30, 40};
                        int currentIdx = -1;
                        for (int i = 0; i < commonIntervals.length; i++) {
                            if (commonIntervals[i] == config.autoAttackIntervalTicks) {
                                currentIdx = i;
                                break;
                            }
                        }
                        int nextIdx = (currentIdx + 1) % commonIntervals.length;
                        config.autoAttackIntervalTicks = commonIntervals[nextIdx];
                        button.setMessage(getAutoAttackIntervalText());
                    }
            ).bounds(centerX - 120, currentY, 240, 20).build());
            currentY += spacing;
        }

        // Done / Save Button
        this.addRenderableWidget(Button.builder(
                CommonComponents.GUI_DONE,
                button -> {
                    config.validate();
                    config.save();
                    if (this.minecraft != null) {
                        this.minecraft.setScreen(this.parent);
                    }
                }
        ).bounds(centerX - 100, this.height - 28, 200, 20).build());
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

    private Component getAutoAttackEnabledText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_enabled")
                .append(": ")
                .append(config.autoAttackEnabled ? CommonComponents.OPTION_ON : CommonComponents.OPTION_OFF);
    }

    private Component getAutoAttackModeText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_mode")
                .append(": ")
                .append(Component.translatable("config.auto_drink_ominous_bottles.attack_mode." + config.autoAttackMode.name().toLowerCase()));
    }

    private Component getAutoAttackIntervalText() {
        return Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_interval_ticks")
                .append(": ")
                .append(String.valueOf(config.autoAttackIntervalTicks))
                .append(" Ticks");
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
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }
}
