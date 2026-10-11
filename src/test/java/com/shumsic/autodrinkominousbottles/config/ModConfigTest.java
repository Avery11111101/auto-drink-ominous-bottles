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
        assertFalse(config.autoAttackEnabled);
        assertEquals(ModConfig.AutoAttackMode.FULL_COOLDOWN, config.autoAttackMode);
        assertEquals(10, config.autoAttackIntervalTicks);
    }

    @Test
    public void testConfigValidation() {
        ModConfig config = new ModConfig();
        config.targetLevel = 10;
        config.fixedWeaponSlot = 15;
        config.autoAttackIntervalTicks = 200;
        config.autoAttackMode = null;
        config.validate();

        assertEquals(5, config.targetLevel);
        assertEquals(9, config.fixedWeaponSlot);
        assertEquals(100, config.autoAttackIntervalTicks);
        assertEquals(ModConfig.AutoAttackMode.FULL_COOLDOWN, config.autoAttackMode);

        config.targetLevel = -1;
        config.fixedWeaponSlot = 0;
        config.autoAttackIntervalTicks = 0;
        config.validate();

        assertEquals(1, config.targetLevel);
        assertEquals(1, config.fixedWeaponSlot);
        assertEquals(1, config.autoAttackIntervalTicks);
    }
}
