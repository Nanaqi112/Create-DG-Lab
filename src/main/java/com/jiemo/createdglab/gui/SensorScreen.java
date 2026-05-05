package com.jiemo.createdglab.gui;

import com.jiemo.createdglab.block.StressSensorBlockEntity;
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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;

public class SensorScreen extends Screen {
    private final BlockPos sensorPos;
    private float stress;
    private float capacity;
    private float stressRatio;
    private boolean overStressed;

    private ResourceLocation qrTexture;
    private boolean qrGenerated = false;
    private int qrTexWidth;
    private int qrTexHeight;

    public SensorScreen(BlockPos pos, float stress, float capacity, boolean overStressed) {
        super(Component.translatable("screen.createdglab.sensor"));
        this.sensorPos = pos;
        this.stress = stress;
        this.capacity = capacity;
        this.stressRatio = capacity > 0 ? stress / capacity : 0;
        this.overStressed = overStressed;
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(width / 2 - 50, height - 30, 100, 20)
                .build());

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
        qrTexWidth = w;
        qrTexHeight = h;
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
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Dark overlay without blur
        guiGraphics.fill(0, 0, width, height, 0xC0101010);

        // Refresh live values from block entity
        if (minecraft != null && minecraft.level != null) {
            BlockEntity be = minecraft.level.getBlockEntity(sensorPos);
            if (be instanceof StressSensorBlockEntity sensor) {
                this.stress = sensor.getStress();
                this.capacity = sensor.getCapacity();
                this.stressRatio = this.capacity > 0 ? this.stress / this.capacity : 0;
                this.overStressed = sensor.isOverStressed();
            }
        }

        int centerX = width / 2;
        int y = 20;

        // Title
        guiGraphics.drawCenteredString(font, title, centerX, y, 0xFFFFFF);
        y += 20;

        // Connection status
        WebSocketServerManager ws = WebSocketServerManager.getInstance();
        String connStatus = ws.isConnected() ? "Connected to DG-Lab" : "Waiting for DG-Lab connection...";
        int connColor = ws.isConnected() ? ChatFormatting.GREEN.getColor() : ChatFormatting.YELLOW.getColor();
        guiGraphics.drawCenteredString(font, connStatus, centerX, y, connColor);
        y += 12;

        // Server address
        String address = "ws://" + ws.getServerAddress() + ":" + ws.getServerPort();
        guiGraphics.drawCenteredString(font, address, centerX, y, 0xAAAAAA);
        y += 20;

        // Stress bar
        int barX = centerX - 80;
        int barWidth = 160;
        int barHeight = 14;
        guiGraphics.fill(barX - 1, y - 1, barX + barWidth + 1, y + barHeight + 1, 0xFF555555);
        guiGraphics.fill(barX, y, barX + barWidth, y + barHeight, 0xFF222222);

        int barColor;
        if (overStressed) barColor = 0xFFFF0000;
        else if (stressRatio > 0.85) barColor = 0xFFFFAA00;
        else if (stressRatio > 0.6) barColor = 0xFFFFFF00;
        else barColor = 0xFF00CC00;

        int fillWidth = (int) (barWidth * Math.min(stressRatio, 1.0f));
        if (fillWidth > 0) {
            guiGraphics.fill(barX, y, barX + fillWidth, y + barHeight, barColor);
        }

        String stressText = String.format("%.0f / %.0f (%.0f%%)", stress, capacity, stressRatio * 100);
        guiGraphics.drawCenteredString(font, stressText, centerX, y + 2, 0xFFFFFF);
        y += barHeight + 8;

        if (overStressed) {
            guiGraphics.drawCenteredString(font, "NETWORK OVERLOADED!", centerX, y, 0xFF5555);
            y += 12;
        }

        // QR Code
        y += 5;
        if (!ws.isConnected()) {
            guiGraphics.drawCenteredString(font, "Scan with DG-Lab App:", centerX, y, 0xCCCCCC);
            y += 14;

            // Render QR code via direct OpenGL
            if (qrTexture != null) {
                int qrSize = 120;
                int qrX = centerX - qrSize / 2;
                renderQrCode(guiGraphics, qrX, y, qrSize);
                y += qrSize + 5;
            }

            // Show URL below QR
            String qrUrl = ws.getQrCodeUrl();
            if (font.width(qrUrl) > width - 20) {
                qrUrl = qrUrl.substring(0, 40) + "..." + qrUrl.substring(qrUrl.length() - 30);
            }
            guiGraphics.drawCenteredString(font, qrUrl, centerX, y, 0x8888FF);
        } else {
            guiGraphics.drawCenteredString(font, "DG-Lab connected!", centerX, y, 0x55FF55);
            y += 15;
            String appInfo = String.format("A: %d  B: %d",
                    ws.getLastSentIntensityA(), ws.getLastSentIntensityB());
            guiGraphics.drawCenteredString(font, appInfo, centerX, y, 0xAAAAAA);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        if (qrTexture != null) {
            minecraft.getTextureManager().release(qrTexture);
            qrTexture = null;
            qrGenerated = false;
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
