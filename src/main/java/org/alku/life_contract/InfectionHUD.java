package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InfectionHUD {
    
    public static final IGuiOverlay INFECTION_HUD_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;
        
        int infection = ClientInfectionData.getInfection();
        int maxInfection = PlayerInfectionSystem.getMaxInfection();
        float percent = (float) infection / maxInfection;
        
        float scale = 0.7f;
        int x = 10;
        int y = screenHeight - 25;
        
        guiGraphics.pose().pushPose();
        guiGraphics.pose().scale(scale, scale, scale);
        
        int scaledX = (int) (x / scale);
        int scaledY = (int) (y / scale);
        
        int color = getInfectionColor(percent);
        
        String text = String.format("感染值: %d/%d", infection, maxInfection);
        guiGraphics.drawString(mc.font, text, scaledX, scaledY, color);
        
        guiGraphics.pose().popPose();
    };
    
    private static int getInfectionColor(float percent) {
        int green = (int) (255 * (1 - percent));
        int red = (int) (255 * percent);
        return (red << 16) | (green << 8) | 255;
    }
    
    @SubscribeEvent
    public static void registerGuiOverlays(RegisterGuiOverlaysEvent event) {
        event.registerBelowAll("infection_hud", INFECTION_HUD_OVERLAY);
    }
}
