package org.alku.life_contract;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExtendedArmorRenderer {

    private static final ResourceLocation ARMOR_ICONS = new ResourceLocation("textures/gui/icons.png");
    private static final int ICON_WIDTH = 9;
    private static final int ICON_HEIGHT = 9;

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onRenderGuiOverlayPost(RenderGuiOverlayEvent.Post event) {
        if (event.getOverlay() != VanillaGuiOverlay.ARMOR_LEVEL.type()) {
            return;
        }
        
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) {
            return;
        }
        
        if (!ExtendedArmor.hasExtendedArmor(player)) {
            return;
        }
        
        GuiGraphics guiGraphics = event.getGuiGraphics();
        
        renderExtendedArmor(guiGraphics, player, mc);
    }

    private static void renderExtendedArmor(GuiGraphics guiGraphics, LocalPlayer player, Minecraft mc) {
        double totalArmor = ExtendedArmor.getTotalArmor(player);
        double extendedArmor = ExtendedArmor.getExtendedArmor(player);
        double totalReduction = ExtendedArmor.calculateDamageReduction(totalArmor);
        double reductionPercent = totalReduction * 100;
        
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        
        int baseX = scaledWidth / 2 - 91;
        int baseY = scaledHeight - 49 + 10;
        
        int armorColor = ExtendedArmor.getArmorColor(player);
        int r = (armorColor >> 16) & 0xFF;
        int g = (armorColor >> 8) & 0xFF;
        int b = armorColor & 0xFF;
        
        String armorText;
        if (extendedArmor > 0) {
            armorText = String.format("%.0f(+%.0f) %.1f%%", 
                ExtendedArmor.VANILLA_ARMOR_CAP, 
                extendedArmor, 
                reductionPercent);
        } else {
            armorText = String.format("%.0f", totalArmor);
        }
        
        int textWidth = mc.font.width(armorText);
        int textX = baseX + 81 - textWidth / 2;
        
        guiGraphics.drawString(mc.font, armorText, textX, baseY - 12, armorColor, true);
        
        if (extendedArmor > 0) {
            renderExtendedArmorBar(guiGraphics, player, mc, baseX, baseY - 2);
        }
    }

    private static void renderExtendedArmorBar(GuiGraphics guiGraphics, LocalPlayer player, Minecraft mc, int x, int y) {
        double extendedArmor = ExtendedArmor.getExtendedArmor(player);
        double totalArmor = ExtendedArmor.getTotalArmor(player);
        double totalReduction = ExtendedArmor.calculateDamageReduction(totalArmor);
        double reductionRatio = totalReduction / ExtendedArmor.MAX_DAMAGE_REDUCTION;
        
        int barWidth = 182;
        int barHeight = 5;
        
        int backgroundColor = 0xFF333333;
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, backgroundColor);
        
        int fillWidth = (int) (barWidth * Math.min(1.0, reductionRatio));
        
        int r = (int) (50 + 150 * reductionRatio);
        int g = (int) (150 + 105 * reductionRatio);
        int b = (int) (200 + 55 * reductionRatio);
        int fillColor = (0xFF << 24) | (r << 16) | (g << 8) | b;
        
        guiGraphics.fill(x, y, x + fillWidth, y + barHeight, fillColor);
        
        String reductionText = String.format("减伤: %.1f%%", totalReduction * 100);
        int textWidth = mc.font.width(reductionText);
        guiGraphics.drawString(mc.font, reductionText, x + barWidth / 2 - textWidth / 2, y + barHeight + 2, 0xFFFFFF, true);
    }
}
