package com.jiemo.createdglab.gui;

import com.jiemo.createdglab.util.QRCodeGenerator;
import com.jiemo.createdglab.websocket.WebSocketServerManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class SensorScreen extends AbstractContainerScreen<SensorMenu> {
    private ResourceLocation qrTexture;
    private boolean qrGenerated = false;

    public SensorScreen(SensorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 280;
        this.imageHeight = 166;
    }

    @Override
    protected void init() {
        super.init();
        this.titleLabelY = 6;
        this.inventoryLabelY = this.imageHeight - 94;
        if (!qrGenerated) {
            generateQrCode();
        }
    }

    private void generateQrCode() {
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        String url = ws.getQrCodeUrl();

        BufferedImage image = QRCodeGenerator.generateQRCode(url);
        if (image == null) return;

        int w = image.getWidth();
        int h = image.getHeight();

        NativeImage nativeImage = new NativeImage(w, h, false);
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = image.getRGB(x, y);
                int a = (rgb >> 24) & 0xFF;
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;
                nativeImage.setPixelRGBA(x, y, (a << 24) | (b << 16) | (g << 8) | r);
            }
        }

        DynamicTexture texture = new DynamicTexture(nativeImage);
        texture.setFilter(false, false); // GL_NEAREST

        qrTexture = ResourceLocation.fromNamespaceAndPath("createdglab", "textures/dynamic/qr_" + System.currentTimeMillis());
        minecraft.getTextureManager().register(qrTexture, texture);
        qrGenerated = true;
    }

    private void renderQrCode(GuiGraphics guiGraphics, int x, int y, int renderSize) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, qrTexture);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        RenderSystem.enableBlend();

        Tesselator tesselator = Tesselator.getInstance();
        var bufferBuilder = tesselator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferBuilder.addVertex(x, y + renderSize, 0).setUv(0, 1);
        bufferBuilder.addVertex(x + renderSize, y + renderSize, 0).setUv(1, 1);
        bufferBuilder.addVertex(x + renderSize, y, 0).setUv(1, 0);
        bufferBuilder.addVertex(x, y, 0).setUv(0, 0);
        BufferUploader.drawWithShader(bufferBuilder.buildOrThrow());

        RenderSystem.disableBlend();
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        // Dark panel background
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xC0101010);
        // Border
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + 1, 0xFF555555);
        guiGraphics.fill(leftPos, topPos + imageHeight - 1, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);
        guiGraphics.fill(leftPos, topPos, leftPos + 1, topPos + imageHeight, 0xFF555555);
        guiGraphics.fill(leftPos + imageWidth - 1, topPos, leftPos + imageWidth, topPos + imageHeight, 0xFF555555);

        // Stress bar
        float stress = menu.getStress();
        float capacity = menu.getCapacity();
        float stressRatio = capacity > 0 ? stress / capacity : 0;
        boolean overStressed = menu.isOverStressed();

        int barX = leftPos + 8;
        int barY = topPos + 20;
        int barWidth = 160;
        int barHeight = 10;

        // Bar background
        guiGraphics.fill(barX, barY, barX + barWidth, barY + barHeight, 0xFF222222);

        // Bar fill color
        int barColor;
        if (overStressed) barColor = 0xFFFF0000;
        else if (stressRatio > 0.85) barColor = 0xFFFFAA00;
        else if (stressRatio > 0.6) barColor = 0xFFFFFF00;
        else barColor = 0xFF00CC00;

        int fillWidth = (int) (barWidth * Math.min(stressRatio, 1.0f));
        if (fillWidth > 0) {
            guiGraphics.fill(barX, barY, barX + fillWidth, barY + barHeight, barColor);
        }

        // Stress text on bar
        String stressText = String.format("%.0f / %.0f (%.0f%%)", stress, capacity, stressRatio * 100);
        guiGraphics.drawCenteredString(font, stressText, barX + barWidth / 2, barY + 1, 0xFFFFFF);

        // Overstressed warning
        if (overStressed) {
            guiGraphics.drawCenteredString(font, "NETWORK OVERLOADED!", leftPos + imageWidth / 2, barY + barHeight + 3, 0xFF5555);
        }

        // QR Code section
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        if (!ws.isConnected() && qrTexture != null) {
            int qrSize = 64;
            int qrX = leftPos + imageWidth / 2 - qrSize / 2;
            int qrY = topPos + 38;
            renderQrCode(guiGraphics, qrX, qrY, qrSize);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        // Title
        guiGraphics.drawCenteredString(font, title, imageWidth / 2, 6, 0xFFFFFF);

        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        // Connection status
        String connStatus = ws.isConnected() ? "Connected" : "Waiting...";
        int connColor = ws.isConnected() ? ChatFormatting.GREEN.getColor() : ChatFormatting.YELLOW.getColor();
        guiGraphics.drawCenteredString(font, connStatus, imageWidth / 2, 34, connColor);

        if (!ws.isConnected()) {
            // Show server address and QR info
            String address = "ws://" + ws.getServerAddress() + ":" + ws.getServerPort();
            guiGraphics.drawCenteredString(font, address, imageWidth / 2, 105, 0xAAAAAA);
            guiGraphics.drawCenteredString(font, "Scan with DG-Lab App", imageWidth / 2, 115, 0xCCCCCC);

            // URL below
            String qrUrl = ws.getQrCodeUrl();
            if (font.width(qrUrl) > imageWidth - 16) {
                qrUrl = qrUrl.substring(0, 30) + "..." + qrUrl.substring(qrUrl.length() - 20);
            }
            guiGraphics.drawCenteredString(font, qrUrl, imageWidth / 2, 125, 0x8888FF);
        } else {
            guiGraphics.drawCenteredString(font, "DG-Lab connected!", imageWidth / 2, 105, 0x55FF55);
            String appInfo = String.format("A: %d  B: %d",
                    ws.getLastSentIntensityA(), ws.getLastSentIntensityB());
            guiGraphics.drawCenteredString(font, appInfo, imageWidth / 2, 115, 0xAAAAAA);
        }

        // No inventory label needed
    }

    @Override
    public void removed() {
        super.removed();
        if (qrTexture != null) {
            minecraft.getTextureManager().release(qrTexture);
            qrTexture = null;
            qrGenerated = false;
        }
    }
}
