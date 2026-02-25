package org.alku.life_contract.death_venger;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class DeathVengerHUD {

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.player == null) return;

        String professionId = ClientDataStorage.getSelfProfessionId();
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ClientProfessionCache.getProfession(professionId);
        if (profession == null || !profession.hasMarkTargetAbility()) return;

        if (!ClientDataStorage.hasMarkedTarget()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();
        int screenWidth = mc.getWindow().getGuiScaledWidth();

        String targetName = ClientDataStorage.getMarkedTargetName();
        int x = ClientDataStorage.getMarkedTargetX();
        int y = ClientDataStorage.getMarkedTargetY();
        int z = ClientDataStorage.getMarkedTargetZ();

        Component headerText = Component.literal("§4§l[死仇者] §f目标追踪");
        Component nameText = Component.literal("§f目标: §c" + targetName);
        Component coordText = Component.literal("§f坐标: §eX:" + x + " Y:" + y + " Z:" + z);

        int headerWidth = mc.font.width("§4§l[死仇者] §f目标追踪");
        int nameWidth = mc.font.width("§f目标: " + targetName);
        int coordWidth = mc.font.width("§f坐标: X:" + x + " Y:" + y + " Z:" + z);

        int boxWidth = Math.max(headerWidth, Math.max(nameWidth, coordWidth)) + 20;
        int boxHeight = 55;
        int boxX = screenWidth - boxWidth - 10;
        int boxY = 10;

        guiGraphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, 0x80000000);
        guiGraphics.renderOutline(boxX, boxY, boxWidth, boxHeight, 0xFF800000);

        guiGraphics.drawString(mc.font, headerText, boxX + 10, boxY + 8, 0xFFFFFFFF);
        guiGraphics.drawString(mc.font, nameText, boxX + 10, boxY + 22, 0xFFFFFFFF);
        guiGraphics.drawString(mc.font, coordText, boxX + 10, boxY + 36, 0xFFFFFFFF);
    }
}
