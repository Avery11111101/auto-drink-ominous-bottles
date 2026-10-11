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
    private static ModConfig INSTANCE;

    private static File getConfigFile() {
        try {
            if (FabricLoader.getInstance() != null && FabricLoader.getInstance().getConfigDir() != null) {
                return new File(FabricLoader.getInstance().getConfigDir().toFile(), "auto_drink_ominous_bottles.json");
            }
        } catch (Throwable ignored) {}
        return new File("config", "auto_drink_ominous_bottles.json");
    }

    public enum BottleSelectionMode {
        HIGHEST,
        LOWEST,
        EXACT_LEVEL
    }

    public enum WeaponSlotMode {
        RESTORE_PREVIOUS,
        FIXED_SLOT
    }

    public enum ActionFeedbackMode {
        ACTION_BAR,
        CHAT,
        DISABLED
    }

    public enum HudStyle {
        TRANSLUCENT_BOX,
        TEXT_ONLY
    }

    // Config options
    public boolean enabled = true;
    public boolean pullFromInventory = true;
    public BottleSelectionMode bottleSelectionMode = BottleSelectionMode.HIGHEST;
    public int targetLevel = 5; // 1 ~ 5
    public WeaponSlotMode weaponSlotMode = WeaponSlotMode.RESTORE_PREVIOUS;
    public int fixedWeaponSlot = 1; // 1 ~ 9 (Hotbar 1 to 9)

    // Action feedback & retry options
    public ActionFeedbackMode actionFeedbackMode = ActionFeedbackMode.ACTION_BAR;
    public int maxDrinkRetries = 2; // 未生怪最多重試飲用次數 (1~3，預設 2)
    public int badOmenVillageTimeoutSeconds = 10; // 不在村莊轉化超時判定秒數 (5~60，預設 10)
    public int raidSpawnTimeoutSeconds = 5; // 突襲凶兆倒數完等待生成判定秒數 (3~15，預設 5)
    public int maxSpawnFailures = 2; // 連續找不到生成點自動關閉次數上限 (1~5，預設 2)

    // Auto-Eat options
    public boolean autoEatEnabled = false;
    public int autoEatHungerThreshold = 14; // 1 ~ 19
    public boolean autoEatPullFromInventory = true;
    public boolean autoEatIgnoreHarmful = true;

    // Raid Status HUD options
    public boolean raidStatusHudEnabled = true;
    public boolean showConsecutiveRaidsCount = true;
    public HudStyle hudStyle = HudStyle.TRANSLUCENT_BOX;
    public int hudX = 10;
    public int hudY = 160; // 預設左下角（快捷列左上方）

    public static ModConfig getInstance() {
        if (INSTANCE == null) {
            INSTANCE = load();
        }
        return INSTANCE;
    }

    public static ModConfig load() {
        File configFile = getConfigFile();
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile, StandardCharsets.UTF_8)) {
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
            File configFile = getConfigFile();
            if (configFile.getParentFile() != null && !configFile.getParentFile().exists()) {
                configFile.getParentFile().mkdirs();
            }
            try (FileWriter writer = new FileWriter(configFile, StandardCharsets.UTF_8)) {
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
        if (actionFeedbackMode == null) actionFeedbackMode = ActionFeedbackMode.ACTION_BAR;
        if (maxDrinkRetries < 1) maxDrinkRetries = 1;
        if (maxDrinkRetries > 3) maxDrinkRetries = 3;
        if (badOmenVillageTimeoutSeconds < 5) badOmenVillageTimeoutSeconds = 5;
        if (badOmenVillageTimeoutSeconds > 60) badOmenVillageTimeoutSeconds = 60;
        if (raidSpawnTimeoutSeconds < 3) raidSpawnTimeoutSeconds = 3;
        if (raidSpawnTimeoutSeconds > 15) raidSpawnTimeoutSeconds = 15;
        if (maxSpawnFailures < 1) maxSpawnFailures = 1;
        if (maxSpawnFailures > 5) maxSpawnFailures = 5;
        if (autoEatHungerThreshold < 1) autoEatHungerThreshold = 1;
        if (autoEatHungerThreshold > 19) autoEatHungerThreshold = 19;
        if (hudStyle == null) hudStyle = HudStyle.TRANSLUCENT_BOX;
        if (hudX < 0) hudX = 0;
        if (hudY < 0) hudY = 0;
    }
}
