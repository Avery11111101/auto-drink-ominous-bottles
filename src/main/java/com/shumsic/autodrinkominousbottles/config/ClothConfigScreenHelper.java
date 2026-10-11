package com.shumsic.autodrinkominousbottles.config;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClothConfigScreenHelper {

    public static Screen createConfigScreen(Screen parent) {
        ModConfig config = ModConfig.getInstance();

        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.translatable("config.auto_drink_ominous_bottles.title"));

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();
        ConfigCategory general = builder.getOrCreateCategory(Component.translatable("config.auto_drink_ominous_bottles.category.general"));

        // Enabled
        general.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.enabled"),
                config.enabled
        ).setDefaultValue(true)
         .setSaveConsumer(newValue -> config.enabled = newValue)
         .build());

        // Pull from inventory
        general.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.pull_from_inventory"),
                config.pullFromInventory
        ).setDefaultValue(true)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.pull_from_inventory.tooltip"))
         .setSaveConsumer(newValue -> config.pullFromInventory = newValue)
         .build());

        // Bottle Selection Mode
        general.addEntry(entryBuilder.startEnumSelector(
                Component.translatable("config.auto_drink_ominous_bottles.option.bottle_selection_mode"),
                ModConfig.BottleSelectionMode.class,
                config.bottleSelectionMode
        ).setDefaultValue(ModConfig.BottleSelectionMode.HIGHEST)
         .setEnumNameProvider(mode -> Component.translatable("config.auto_drink_ominous_bottles.mode." + mode.name().toLowerCase()))
         .setSaveConsumer(newValue -> config.bottleSelectionMode = newValue)
         .build());

        // Target Level (1~5)
        general.addEntry(entryBuilder.startIntSlider(
                Component.translatable("config.auto_drink_ominous_bottles.option.target_level"),
                config.targetLevel,
                1,
                5
        ).setDefaultValue(5)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.target_level.tooltip"))
         .setRequirement(() -> config.bottleSelectionMode == ModConfig.BottleSelectionMode.EXACT_LEVEL)
         .setSaveConsumer(newValue -> config.targetLevel = newValue)
         .build());

        // Weapon Slot Mode
        general.addEntry(entryBuilder.startEnumSelector(
                Component.translatable("config.auto_drink_ominous_bottles.option.weapon_slot_mode"),
                ModConfig.WeaponSlotMode.class,
                config.weaponSlotMode
        ).setDefaultValue(ModConfig.WeaponSlotMode.RESTORE_PREVIOUS)
         .setEnumNameProvider(mode -> Component.translatable("config.auto_drink_ominous_bottles.weapon_mode." + mode.name().toLowerCase()))
         .setSaveConsumer(newValue -> config.weaponSlotMode = newValue)
         .build());

        // Fixed Weapon Hotbar Slot (1~9)
        general.addEntry(entryBuilder.startIntSlider(
                Component.translatable("config.auto_drink_ominous_bottles.option.fixed_weapon_slot"),
                config.fixedWeaponSlot,
                1,
                9
        ).setDefaultValue(1)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.fixed_weapon_slot.tooltip"))
         .setRequirement(() -> config.weaponSlotMode == ModConfig.WeaponSlotMode.FIXED_SLOT)
         .setSaveConsumer(newValue -> config.fixedWeaponSlot = newValue)
         .build());

        // Action Feedback Mode
        general.addEntry(entryBuilder.startEnumSelector(
                Component.translatable("config.auto_drink_ominous_bottles.option.action_feedback_mode"),
                ModConfig.ActionFeedbackMode.class,
                config.actionFeedbackMode
        ).setDefaultValue(ModConfig.ActionFeedbackMode.ACTION_BAR)
         .setEnumNameProvider(mode -> Component.translatable("config.auto_drink_ominous_bottles.feedback_mode." + mode.name().toLowerCase()))
         .setSaveConsumer(newValue -> config.actionFeedbackMode = newValue)
         .build());

        // Max Drink Retries (1~3)
        general.addEntry(entryBuilder.startIntSlider(
                Component.translatable("config.auto_drink_ominous_bottles.option.max_drink_retries"),
                config.maxDrinkRetries,
                1,
                3
        ).setDefaultValue(2)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.max_drink_retries.tooltip"))
         .setSaveConsumer(newValue -> config.maxDrinkRetries = newValue)
         .build());

        // Auto Eat Category
        ConfigCategory eatCategory = builder.getOrCreateCategory(Component.translatable("config.auto_drink_ominous_bottles.category.auto_eat"));

        // Auto Eat Enabled
        eatCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_enabled"),
                config.autoEatEnabled
        ).setDefaultValue(false)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_enabled.tooltip"))
         .setSaveConsumer(newValue -> config.autoEatEnabled = newValue)
         .build());

        // Auto Eat Hunger Threshold (1~19)
        eatCategory.addEntry(entryBuilder.startIntSlider(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_hunger_threshold"),
                config.autoEatHungerThreshold,
                1,
                19
        ).setDefaultValue(14)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_hunger_threshold.tooltip"))
         .setSaveConsumer(newValue -> config.autoEatHungerThreshold = newValue)
         .build());

        // Auto Eat Pull From Inventory
        eatCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_pull_from_inventory"),
                config.autoEatPullFromInventory
        ).setDefaultValue(true)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_pull_from_inventory.tooltip"))
         .setSaveConsumer(newValue -> config.autoEatPullFromInventory = newValue)
         .build());

        // Auto Eat Ignore Harmful Food
        eatCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_ignore_harmful"),
                config.autoEatIgnoreHarmful
        ).setDefaultValue(true)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.auto_eat_ignore_harmful.tooltip"))
         .setSaveConsumer(newValue -> config.autoEatIgnoreHarmful = newValue)
         .build());

        builder.setSavingRunnable(() -> {
            config.validate();
            config.save();
        });

        return builder.build();
    }
}
