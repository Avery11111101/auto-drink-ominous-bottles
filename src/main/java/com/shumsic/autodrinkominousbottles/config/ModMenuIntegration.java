package com.shumsic.autodrinkominousbottles.config;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import net.minecraft.client.gui.screens.Screen;

public class ModMenuIntegration implements ModMenuApi {

    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return (Screen parent) -> {
            try {
                // Try Cloth Config if present at runtime
                Class.forName("me.shedaniel.clothconfig2.api.ConfigBuilder");
                return ClothConfigScreenHelper.createConfigScreen(parent);
            } catch (Throwable t) {
                // Fallback to built-in native screen
                return new FallbackConfigScreen(parent);
            }
        };
    }
}
