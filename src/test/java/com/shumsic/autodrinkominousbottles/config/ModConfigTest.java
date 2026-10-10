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
    }

    @Test
    public void testConfigValidation() {
        ModConfig config = new ModConfig();
        config.targetLevel = 10;
        config.fixedWeaponSlot = 15;
        config.validate();

        assertEquals(5, config.targetLevel);
        assertEquals(9, config.fixedWeaponSlot);

        config.targetLevel = -1;
        config.fixedWeaponSlot = 0;
        config.validate();

        assertEquals(1, config.targetLevel);
        assertEquals(1, config.fixedWeaponSlot);
    }
}
