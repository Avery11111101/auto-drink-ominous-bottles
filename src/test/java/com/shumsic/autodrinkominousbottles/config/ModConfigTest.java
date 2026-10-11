package com.shumsic.autodrinkominousbottles.config;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class ModConfigTest {

    @Test
    public void testConfigDefaults() {
        ModConfig config = new ModConfig();
        assertTrue(config.enabled);
        assertTrue(config.pullFromInventory);
        assertEquals(ModConfig.BottleSelectionMode.HIGHEST, config.bottleSelectionMode);
        assertEquals(5, config.targetLevel);
        assertEquals(ModConfig.WeaponSlotMode.RESTORE_PREVIOUS, config.weaponSlotMode);
        assertEquals(1, config.fixedWeaponSlot);
        assertFalse(config.autoEatEnabled);
        assertEquals(14, config.autoEatHungerThreshold);
        assertTrue(config.autoEatPullFromInventory);
        assertTrue(config.autoEatIgnoreHarmful);
        assertEquals(ModConfig.ActionFeedbackMode.ACTION_BAR, config.actionFeedbackMode);
        assertEquals(2, config.maxDrinkRetries);
        assertEquals(10, config.badOmenVillageTimeoutSeconds);
        assertEquals(5, config.raidSpawnTimeoutSeconds);
        assertEquals(2, config.maxSpawnFailures);
    }

    @Test
    public void testConfigValidation() {
        ModConfig config = new ModConfig();
        config.targetLevel = 10;
        config.fixedWeaponSlot = 15;
        config.autoEatHungerThreshold = 50;
        config.maxDrinkRetries = 10;
        config.badOmenVillageTimeoutSeconds = 100;
        config.raidSpawnTimeoutSeconds = 30;
        config.maxSpawnFailures = 10;
        config.validate();

        assertEquals(5, config.targetLevel);
        assertEquals(9, config.fixedWeaponSlot);
        assertEquals(19, config.autoEatHungerThreshold);
        assertEquals(3, config.maxDrinkRetries);
        assertEquals(60, config.badOmenVillageTimeoutSeconds);
        assertEquals(15, config.raidSpawnTimeoutSeconds);
        assertEquals(5, config.maxSpawnFailures);

        config.targetLevel = -1;
        config.fixedWeaponSlot = 0;
        config.autoEatHungerThreshold = 0;
        config.maxDrinkRetries = 0;
        config.badOmenVillageTimeoutSeconds = 1;
        config.raidSpawnTimeoutSeconds = 1;
        config.maxSpawnFailures = 0;
        config.actionFeedbackMode = null;
        config.validate();

        assertEquals(1, config.targetLevel);
        assertEquals(1, config.fixedWeaponSlot);
        assertEquals(1, config.autoEatHungerThreshold);
        assertEquals(1, config.maxDrinkRetries);
        assertEquals(5, config.badOmenVillageTimeoutSeconds);
        assertEquals(3, config.raidSpawnTimeoutSeconds);
        assertEquals(1, config.maxSpawnFailures);
        assertEquals(ModConfig.ActionFeedbackMode.ACTION_BAR, config.actionFeedbackMode);
    }
}
