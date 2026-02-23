package org.alku.life_contract;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class HealerHUD {

    private static final ResourceLocation HEART_TEXTURE = new ResourceLocation("minecraft", "textures/gui/icons.png");

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) return;

        String professionId = ClientDataStorage.getProfessionId();
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHealer()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int x = screenWidth / 2 + 10;
        int y = screenHeight - 50;

        int cooldown = ClientDataStorage.getHealerCooldown();
        int maxCooldown = profession.getHealerActiveCooldown();

        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);

        guiGraphics.blit(HEART_TEXTURE, x, y, 52, 0, 9, 9);

        if (cooldown > 0) {
            int barWidth = 20;
            int progress = (int) ((1.0 - (double) cooldown / maxCooldown) * barWidth);

            guiGraphics.fill(x + 10, y + 3, x + 10 + barWidth, y + 6, 0xFF555555);
            if (progress > 0) {
                guiGraphics.fill(x + 10, y + 3, x + 10 + progress, y + 6, 0xFF55FF55);
            }

            int seconds = cooldown / 20 + 1;
            guiGraphics.drawString(mc.font, seconds + "s", x + 12, y - 8, 0xFFFFFF);
        } else {
            guiGraphics.drawString(mc.font, "§a✓", x + 12, y, 0xFFFFFF);
        }
    }
}
