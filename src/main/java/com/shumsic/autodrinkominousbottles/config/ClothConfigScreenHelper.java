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

        // Auto Attack Category
        ConfigCategory attackCategory = builder.getOrCreateCategory(Component.translatable("config.auto_drink_ominous_bottles.category.auto_attack"));

        // Auto Attack Enabled
        attackCategory.addEntry(entryBuilder.startBooleanToggle(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_enabled"),
                config.autoAttackEnabled
        ).setDefaultValue(false)
         .setSaveConsumer(newValue -> config.autoAttackEnabled = newValue)
         .build());

        // Auto Attack Mode
        attackCategory.addEntry(entryBuilder.startEnumSelector(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_mode"),
                ModConfig.AutoAttackMode.class,
                config.autoAttackMode
        ).setDefaultValue(ModConfig.AutoAttackMode.FULL_COOLDOWN)
         .setEnumNameProvider(mode -> Component.translatable("config.auto_drink_ominous_bottles.attack_mode." + mode.name().toLowerCase()))
         .setSaveConsumer(newValue -> config.autoAttackMode = newValue)
         .build());

        // Auto Attack Interval Ticks (1~40)
        attackCategory.addEntry(entryBuilder.startIntSlider(
                Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_interval_ticks"),
                config.autoAttackIntervalTicks,
                1,
                40
        ).setDefaultValue(10)
         .setTooltip(Component.translatable("config.auto_drink_ominous_bottles.option.auto_attack_interval_ticks.tooltip"))
         .setRequirement(() -> config.autoAttackMode == ModConfig.AutoAttackMode.CUSTOM_INTERVAL)
         .setSaveConsumer(newValue -> config.autoAttackIntervalTicks = newValue)
         .build());

        builder.setSavingRunnable(() -> {
            config.validate();
            config.save();
        });

        return builder.build();
    }
}
