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
        int centerX = this.width / 2;
        int startY = this.height / 4;
        int spacing = 24;

        // 1. Enabled Toggle Button
        this.addRenderableWidget(Button.builder(
                getEnabledText(),
                button -> {
                    config.enabled = !config.enabled;
                    button.setMessage(getEnabledText());
                }
        ).bounds(centerX - 120, startY, 240, 20).build());

        // 2. Pull from inventory Toggle Button
        this.addRenderableWidget(Button.builder(
                getPullInventoryText(),
                button -> {
                    config.pullFromInventory = !config.pullFromInventory;
                    button.setMessage(getPullInventoryText());
                }
        ).bounds(centerX - 120, startY + spacing, 240, 20).build());

        // 3. Selection Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getSelectionModeText(),
                button -> {
                    ModConfig.BottleSelectionMode[] modes = ModConfig.BottleSelectionMode.values();
                    int nextIdx = (config.bottleSelectionMode.ordinal() + 1) % modes.length;
                    config.bottleSelectionMode = modes[nextIdx];
                    button.setMessage(getSelectionModeText());
                }
        ).bounds(centerX - 120, startY + spacing * 2, 240, 20).build());

        // 4. Target Level Cycle Button (1~5)
        this.addRenderableWidget(Button.builder(
                getTargetLevelText(),
                button -> {
                    config.targetLevel = (config.targetLevel % 5) + 1;
                    button.setMessage(getTargetLevelText());
                }
        ).bounds(centerX - 120, startY + spacing * 3, 240, 20).build());

        // 5. Weapon Slot Mode Cycle Button
        this.addRenderableWidget(Button.builder(
                getWeaponSlotModeText(),
                button -> {
                    ModConfig.WeaponSlotMode[] modes = ModConfig.WeaponSlotMode.values();
                    int nextIdx = (config.weaponSlotMode.ordinal() + 1) % modes.length;
                    config.weaponSlotMode = modes[nextIdx];
                    button.setMessage(getWeaponSlotModeText());
                }
        ).bounds(centerX - 120, startY + spacing * 4, 240, 20).build());

        // 6. Fixed Weapon Slot Cycle Button (1~9)
        this.addRenderableWidget(Button.builder(
                getFixedWeaponSlotText(),
                button -> {
                    config.fixedWeaponSlot = (config.fixedWeaponSlot % 9) + 1;
                    button.setMessage(getFixedWeaponSlotText());
                }
        ).bounds(centerX - 120, startY + spacing * 5, 240, 20).build());

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
        ).bounds(centerX - 100, this.height - 32, 200, 20).build());
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

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 16, 0xFFFFFF);
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
