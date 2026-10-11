package com.shumsic.autodrinkominousbottles.gui;

import com.shumsic.autodrinkominousbottles.config.ModConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RaidHudEditScreen extends Screen {
    private final Screen parent;
    private final ModConfig config;

    private int previewX;
    private int previewY;
    private final int previewWidth = 140;
    private final int previewHeight = 62;

    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;

    public RaidHudEditScreen(Screen parent) {
        super(Component.translatable("screen.auto_drink_ominous_bottles.hud_edit.title"));
        this.parent = parent;
        this.config = ModConfig.getInstance();
        this.previewX = config.hudX;
        this.previewY = config.hudY;
    }

    @Override
    protected void init() {
        this.clearWidgets();
        clampPosition();

        int btnWidth = 110;
        int btnHeight = 20;
        int btnSpacing = 10;
        int totalBtnWidth = btnWidth * 2 + btnSpacing;
        int startBtnX = (this.width - totalBtnWidth) / 2;
        int btnY = this.height - 32;

        // 儲存並返回
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.auto_drink_ominous_bottles.hud_edit.save"),
                button -> {
                    config.hudX = previewX;
                    config.hudY = previewY;
                    config.validate();
                    config.save();
                    this.onClose();
                }
        ).bounds(startBtnX, btnY, btnWidth, btnHeight).build());

        // 重置為預設位置（左下角）
        this.addRenderableWidget(Button.builder(
                Component.translatable("screen.auto_drink_ominous_bottles.hud_edit.reset"),
                button -> {
                    this.previewX = 10;
                    this.previewY = Math.max(10, this.height - 110);
                    clampPosition();
                }
        ).bounds(startBtnX + btnWidth + btnSpacing, btnY, btnWidth, btnHeight).build());
    }

    private void clampPosition() {
        int maxX = Math.max(0, this.width - previewWidth);
        int maxY = Math.max(0, this.height - previewHeight - 40); // 預留底部按鈕區域

        if (previewX < 0) previewX = 0;
        if (previewX > maxX) previewX = maxX;
        if (previewY < 0) previewY = 0;
        if (previewY > maxY) previewY = maxY;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDoubleClick) {
        if (event.button() == 0) { // 左鍵
            double mouseX = event.x();
            double mouseY = event.y();
            if (mouseX >= previewX && mouseX <= previewX + previewWidth &&
                mouseY >= previewY && mouseY <= previewY + previewHeight) {
                isDragging = true;
                dragOffsetX = (int) mouseX - previewX;
                dragOffsetY = (int) mouseY - previewY;
                return true;
            }
        }
        return super.mouseClicked(event, isDoubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double deltaX, double deltaY) {
        if (isDragging && event.button() == 0) {
            previewX = (int) event.x() - dragOffsetX;
            previewY = (int) event.y() - dragOffsetY;
            clampPosition();
            return true;
        }
        return super.mouseDragged(event, deltaX, deltaY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (event.button() == 0 && isDragging) {
            isDragging = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor guiGraphicsExtractor, int mouseX, int mouseY, float partialTick) {
        super.extractRenderState(guiGraphicsExtractor, mouseX, mouseY, partialTick);

        // 繪製標題與說明文字
        guiGraphicsExtractor.centeredText(this.font, this.title, this.width / 2, 12, 0xFFFFFF);
        guiGraphicsExtractor.centeredText(this.font, Component.translatable("screen.auto_drink_ominous_bottles.hud_edit.hint"), this.width / 2, 26, 0xAAAAAA);

        // 繪製 HUD 預覽方塊
        int bgColor = isDragging ? 0xCC1E3A8A : 0xAA000000; // 拖曳中為藍色半透明，平常為黑色半透明
        guiGraphicsExtractor.fill(previewX, previewY, previewX + previewWidth, previewY + previewHeight, bgColor);

        // 邊框標示（上、下、左、右）
        int borderColor = isDragging ? 0xFF60A5FA : 0xFFFFFFFF;
        guiGraphicsExtractor.fill(previewX, previewY, previewX + previewWidth, previewY + 1, borderColor);
        guiGraphicsExtractor.fill(previewX, previewY + previewHeight - 1, previewX + previewWidth, previewY + previewHeight, borderColor);
        guiGraphicsExtractor.fill(previewX, previewY, previewX + 1, previewY + previewHeight, borderColor);
        guiGraphicsExtractor.fill(previewX + previewWidth - 1, previewY, previewX + previewWidth, previewY + previewHeight, borderColor);

        // 預覽文字
        guiGraphicsExtractor.text(this.font, Component.translatable("screen.auto_drink_ominous_bottles.hud_edit.preview_title"), previewX + 5, previewY + 5, 0xFFAA00);
        guiGraphicsExtractor.text(this.font, Component.translatable("hud.auto_drink_ominous_bottles.wave", 3, 7), previewX + 5, previewY + 18, 0xFFFFFF);
        guiGraphicsExtractor.text(this.font, Component.translatable("hud.auto_drink_ominous_bottles.health", 75), previewX + 5, previewY + 30, 0x55FF55);
        guiGraphicsExtractor.text(this.font, Component.translatable("hud.auto_drink_ominous_bottles.mobs_nearby", 4), previewX + 5, previewY + 42, 0xFF5555);
        if (config.showConsecutiveRaidsCount) {
            guiGraphicsExtractor.text(this.font, Component.translatable("hud.auto_drink_ominous_bottles.consecutive", 5), previewX + 5, previewY + 52, 0x55FFFF);
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && this.minecraft.gui != null) {
            this.minecraft.gui.setScreen(this.parent);
        }
    }
}
