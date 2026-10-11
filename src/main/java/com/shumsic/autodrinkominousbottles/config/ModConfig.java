package com.shumsic.autodrinkominousbottles.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;

public class ModConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "auto_drink_ominous_bottles.json");
    private static ModConfig INSTANCE;

    public enum BottleSelectionMode {
        HIGHEST,
        LOWEST,
        EXACT_LEVEL
    }

    public enum WeaponSlotMode {
        RESTORE_PREVIOUS,
        FIXED_SLOT
    }

    public enum AutoAttackMode {
        FULL_COOLDOWN,
        CUSTOM_INTERVAL,
        ONCE_ON_AIM
    }

    // Config options
    public boolean enabled = true;
    public boolean pullFromInventory = true;
    public BottleSelectionMode bottleSelectionMode = BottleSelectionMode.HIGHEST;
    public int targetLevel = 5; // 1 ~ 5
    public WeaponSlotMode weaponSlotMode = WeaponSlotMode.RESTORE_PREVIOUS;
    public int fixedWeaponSlot = 1; // 1 ~ 9 (Hotbar 1 to 9)

    public boolean autoAttackEnabled = false;
    public AutoAttackMode autoAttackMode = AutoAttackMode.FULL_COOLDOWN;
    public int autoAttackIntervalTicks = 10; // 1 ~ 100

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        if (CONFIG_FILE.exists()) {
            try (FileReader reader = new FileReader(CONFIG_FILE, StandardCharsets.UTF_8)) {
                ModConfig config = GSON.fromJson(reader, ModConfig.class);
                if (config != null) {
                    config.validate();
                    return config;
                }
            } catch (Exception e) {
                System.err.println("[AutoDrinkOminousBottles] Failed to load config, using defaults: " + e.getMessage());
            }
        }
        ModConfig newConfig = new ModConfig();
        newConfig.save();
        return newConfig;
    }

    public void save() {
        try {
            if (!CONFIG_FILE.getParentFile().exists()) {
                CONFIG_FILE.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(CONFIG_FILE, StandardCharsets.UTF_8)) {
                GSON.toJson(this, writer);
            }
        } catch (Exception e) {
            System.err.println("[AutoDrinkOminousBottles] Failed to save config: " + e.getMessage());
        }
    }

    public void validate() {
        if (targetLevel < 1) targetLevel = 1;
        if (targetLevel > 5) targetLevel = 5;
        if (fixedWeaponSlot < 1) fixedWeaponSlot = 1;
        if (fixedWeaponSlot > 9) fixedWeaponSlot = 9;
        if (bottleSelectionMode == null) bottleSelectionMode = BottleSelectionMode.HIGHEST;
        if (weaponSlotMode == null) weaponSlotMode = WeaponSlotMode.RESTORE_PREVIOUS;
        if (autoAttackMode == null) autoAttackMode = AutoAttackMode.FULL_COOLDOWN;
        if (autoAttackIntervalTicks < 1) autoAttackIntervalTicks = 1;
        if (autoAttackIntervalTicks > 100) autoAttackIntervalTicks = 100;
    }
}
