package com.shumsic.autodrinkominousbottles.hud;

import com.shumsic.autodrinkominousbottles.config.ModConfig;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class RaidStatusHudRenderer implements HudElement {

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, DeltaTracker deltaTracker) {
        Minecraft client = Minecraft.getInstance();
        if (client == null || client.player == null || client.gui == null || client.gui.hud == null || client.gui.hud.isHidden()) {
            return;
        }

        ModConfig config = ModConfig.getInstance();
        if (!config.raidStatusHudEnabled) {
            return;
        }

        RaidStatusTracker tracker = RaidStatusTracker.getInstance();
        // 僅在處於突襲期間才顯示
        if (!tracker.isInRaid()) {
            return;
        }

        int width = 135;
        int height = config.showConsecutiveRaidsCount ? 62 : 50;

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        // 防呆邊界校驗，防止跑出螢幕
        int x = Math.max(0, Math.min(config.hudX, Math.max(0, screenWidth - width)));
        int y = Math.max(0, Math.min(config.hudY, Math.max(0, screenHeight - height)));

        // 視覺風格判定：半透明背景框 vs 極簡純文字
        if (config.hudStyle == ModConfig.HudStyle.TRANSLUCENT_BOX) {
            // 背景框
            guiGraphicsExtractor.fill(x, y, x + width, y + height, 0x88000000);
            // 邊框
            guiGraphicsExtractor.fill(x, y, x + width, y + 1, 0x44FFFFFF);
            guiGraphicsExtractor.fill(x, y + height - 1, x + width, y + height, 0x44FFFFFF);
            guiGraphicsExtractor.fill(x, y, x + 1, y + height, 0x44FFFFFF);
            guiGraphicsExtractor.fill(x + width - 1, y, x + width, y + height, 0x44FFFFFF);
        }

        int textX = x + 5;
        int currentY = y + 5;

        // 標題
        guiGraphicsExtractor.text(client.font, Component.translatable("hud.auto_drink_ominous_bottles.title"), textX, currentY, 0xFFAA00);
        currentY += 11;

        // 波次
        Component waveText = Component.translatable("hud.auto_drink_ominous_bottles.wave", tracker.getCurrentWave(), tracker.getTotalWaves());
        guiGraphicsExtractor.text(client.font, waveText, textX, currentY, 0xFFFFFF);
        currentY += 11;

        // 血量
        int hpPercent = Math.round(tracker.getRaidHealthPercent() * 100.0f);
        int hpColor = hpPercent > 50 ? 0x55FF55 : (hpPercent > 20 ? 0xFFFF55 : 0xFF5555);
        Component hpText = Component.translatable("hud.auto_drink_ominous_bottles.health", hpPercent);
        guiGraphicsExtractor.text(client.font, hpText, textX, currentY, hpColor);
        currentY += 11;

        // 附近敵人數量
        int mobCount = tracker.getNearbyRaidersCount();
        Component mobText = Component.translatable("hud.auto_drink_ominous_bottles.mobs_nearby", mobCount);
        guiGraphicsExtractor.text(client.font, mobText, textX, currentY, 0xFF7777);
        currentY += 11;

        // 連續完成場次
        if (config.showConsecutiveRaidsCount) {
            Component streakText = Component.translatable("hud.auto_drink_ominous_bottles.consecutive", tracker.getConsecutiveRaidsCount());
            guiGraphicsExtractor.text(client.font, streakText, textX, currentY, 0x55FFFF);
        }
    }
}
