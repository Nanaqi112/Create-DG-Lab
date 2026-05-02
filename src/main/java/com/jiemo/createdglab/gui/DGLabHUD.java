package com.jiemo.createdglab.gui;

import com.jiemo.createdglab.websocket.WebSocketServerManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "createdglab", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DGLabHUD {

    private static String cachedAddress = "";
    private static long lastAddressCheck = 0;

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // Only render once per frame (on hotbar overlay)
        if (event.getOverlay() != VanillaGuiOverlay.HOTBAR.type()) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.options.hideGui) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        WebSocketServerManager ws = WebSocketServerManager.getInstance();

        if (!ws.isRunning()) return;

        String text;
        if (ws.isConnected()) {
            int strengthA = ws.getLastSentIntensityA();
            int strengthB = ws.getLastSentIntensityB();
            text = String.format("\u00a7aDG-Lab\u00a7r | A: \u00a76%d\u00a7r | B: \u00a79%d\u00a7r",
                    strengthA, strengthB);
        } else {
            // Cache address - only refresh every 5 seconds
            long now = System.currentTimeMillis();
            if (now - lastAddressCheck > 5000) {
                cachedAddress = ws.getServerAddress();
                lastAddressCheck = now;
            }
            text = "\u00a7eDG-Lab\u00a7r | \u00a7c\u672a\u8fde\u63a5\u00a7r | ws://" + cachedAddress + ":" + ws.getServerPort();
        }

        guiGraphics.drawString(mc.font, text, 4, 4, 0xFFFFFF, true);
    }
}
