package org.alku.life_contract.events;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;

import java.util.List;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class EventHUD {
    
    public static boolean isEnabled = true;
    
    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "event_status", EVENT_HUD_OVERLAY);
    }
    
    public static final IGuiOverlay EVENT_HUD_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isEnabled) return;
        
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        float scale = 0.8f;
        int color = 0xFFFFFF;
        
        boolean hasAnyEvent = false;
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        
        int scaledWidth = (int) (screenWidth / scale);
        int scaledHeight = (int) (screenHeight / scale);
        int y = (int) (10 / scale);
        int rightMargin = 10;
        
        String title = "§6== 游戏事件 ==";
        int titleWidth = mc.font.width(title);
        guiGraphics.drawString(mc.font, title, scaledWidth - titleWidth - rightMargin, y, color);
        y += 12;
        
        if (ClientDataStorage.isSporeSurgeActive()) {
            hasAnyEvent = true;
            int remaining = ClientDataStorage.getSporeSurgeRemaining();
            String line1 = "§c[孢潮推进]";
            String line2 = "  §f剩余: §e" + remaining + "秒";
            guiGraphics.drawString(mc.font, line1, scaledWidth - mc.font.width(line1) - rightMargin, y, color);
            y += 10;
            guiGraphics.drawString(mc.font, line2, scaledWidth - mc.font.width(line2) - rightMargin, y, color);
            y += 12;
        }
        
        if (ClientDataStorage.isPurificationRiftActive()) {
            hasAnyEvent = true;
            int remaining = ClientDataStorage.getSafeBubbleRemaining();
            String line1 = "§b[净化裂隙]";
            guiGraphics.drawString(mc.font, line1, scaledWidth - mc.font.width(line1) - rightMargin, y, color);
            y += 10;
            if (remaining > 0) {
                String line2 = "  §f剩余: §e" + remaining + "秒";
                guiGraphics.drawString(mc.font, line2, scaledWidth - mc.font.width(line2) - rightMargin, y, color);
                y += 10;
            }
            
            List<int[]> bubbles = ClientDataStorage.getBubblePositions();
            if (bubbles != null && !bubbles.isEmpty()) {
                String bubbleTitle = "  §f安全气泡坐标:";
                guiGraphics.drawString(mc.font, bubbleTitle, scaledWidth - mc.font.width(bubbleTitle) - rightMargin, y, color);
                y += 10;
                int bubbleIndex = 1;
                for (int[] bubble : bubbles) {
                    if (bubble.length >= 4) {
                        String bubbleLine = "    §b气泡" + bubbleIndex + ": §fX:" + bubble[0] + " Y:" + bubble[1] + " Z:" + bubble[2];
                        guiGraphics.drawString(mc.font, bubbleLine, scaledWidth - mc.font.width(bubbleLine) - rightMargin, y, color);
                        y += 10;
                    }
                    bubbleIndex++;
                }
            }
            y += 2;
        }
        
        if (ClientDataStorage.isBountyActive()) {
            hasAnyEvent = true;
            String targetName = ClientDataStorage.getBountyTargetName();
            String line1 = "§e[清道夫悬赏]";
            guiGraphics.drawString(mc.font, line1, scaledWidth - mc.font.width(line1) - rightMargin, y, color);
            y += 10;
            if (targetName != null && !targetName.isEmpty()) {
                String line2 = "  §f目标: §c" + targetName;
                guiGraphics.drawString(mc.font, line2, scaledWidth - mc.font.width(line2) - rightMargin, y, color);
                y += 10;
            }
            String line3 = "  §f状态: §a永久";
            guiGraphics.drawString(mc.font, line3, scaledWidth - mc.font.width(line3) - rightMargin, y, color);
            y += 12;
        }
        
        if (ClientDataStorage.isEndgameOverloadActive()) {
            hasAnyEvent = true;
            String line1 = "§4[终局过载]";
            String line2 = "  §f状态: §c永久";
            guiGraphics.drawString(mc.font, line1, scaledWidth - mc.font.width(line1) - rightMargin, y, color);
            y += 10;
            guiGraphics.drawString(mc.font, line2, scaledWidth - mc.font.width(line2) - rightMargin, y, color);
            y += 12;
        }
        
        if (ClientDataStorage.isSporeRainActive()) {
            hasAnyEvent = true;
            int remaining = ClientDataStorage.getSporeRainRemaining();
            String line1 = "§2[孢子雨]";
            String line2 = "  §f剩余: §e" + remaining + "秒";
            guiGraphics.drawString(mc.font, line1, scaledWidth - mc.font.width(line1) - rightMargin, y, color);
            y += 10;
            guiGraphics.drawString(mc.font, line2, scaledWidth - mc.font.width(line2) - rightMargin, y, color);
            y += 12;
        }
        
        if (!hasAnyEvent) {
            if (ClientDataStorage.isGameActive()) {
                String line = "§7暂无进行中事件";
                guiGraphics.drawString(mc.font, line, scaledWidth - mc.font.width(line) - rightMargin, y, 0xAAAAAA);
            } else {
                String line = "§7游戏未开始";
                guiGraphics.drawString(mc.font, line, scaledWidth - mc.font.width(line) - rightMargin, y, 0xAAAAAA);
            }
        }
        
        guiGraphics.pose().popPose();
    };
}
