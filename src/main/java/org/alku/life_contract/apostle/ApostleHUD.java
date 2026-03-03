package org.alku.life_contract.apostle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class ApostleHUD {

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) return;

        String professionId = getProfessionId(player);
        
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ClientProfessionCache.getProfession(professionId);
        
        if (profession == null) {
            return;
        }
        
        if (!profession.isApostle()) {
            return;
        }

        GuiGraphics guiGraphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        CompoundTag data = player.getPersistentData();
        int teleportCooldown = data.getInt("ApostleTeleportCooldownClient");
        int fireballCooldown = data.getInt("ApostleFireballCooldownClient");
        boolean inFire = data.getBoolean("ApostleInFireClient");
        
        int slotWidth = 40;
        int slotHeight = 28;
        int spacing = 3;
        
        int baseX = screenWidth - slotWidth - 10;
        int baseY = screenHeight - slotHeight - 10;

        renderSkillSlot(guiGraphics, mc, baseX, baseY, "瞬移", "Z", teleportCooldown, profession.getApostleTeleportCooldown(), 0xFFAA5500, slotWidth, slotHeight);
        renderSkillSlot(guiGraphics, mc, baseX - slotWidth - spacing, baseY, "火球", "X", fireballCooldown, profession.getApostleFireballCooldown(), 0xFFFF4400, slotWidth, slotHeight);
        
        if (inFire) {
            guiGraphics.drawString(mc.font, "§6火焰: 冷却恢复+100%", baseX - slotWidth - spacing, baseY - 10, 0xFFFFFF);
        }
    }

    private static String getProfessionId(LocalPlayer player) {
        ClientDataStorage.PlayerData data = ClientDataStorage.get(player.getUUID());
        if (data != null && data.profession != null && !data.profession.isEmpty()) {
            return data.profession;
        }
        
        CompoundTag persistentData = player.getPersistentData();
        if (persistentData.contains("LifeContractProfession")) {
            return persistentData.getString("LifeContractProfession");
        }
        
        return ClientProfessionCache.getCurrentProfessionId();
    }

    private static void renderSkillSlot(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
            String skillName, String key, int cooldown, int maxCooldown, int activeColor, int slotWidth, int slotHeight) {
        guiGraphics.fill(x, y, x + slotWidth, y + slotHeight, 0x88000000);
        guiGraphics.fill(x + 1, y + 1, x + slotWidth - 1, y + slotHeight - 1, 0xFF333333);
        
        if (cooldown <= 0) {
            guiGraphics.fill(x + 1, y + 1, x + slotWidth - 1, y + slotHeight - 1, activeColor & 0x44FFFFFF);
        }

        guiGraphics.drawCenteredString(mc.font, skillName, x + slotWidth / 2, y + 3, 0xFFFFFF);
        
        if (cooldown > 0) {
            int cooldownWidth = (int) ((double) cooldown / maxCooldown * (slotWidth - 2));
            guiGraphics.fill(x + 1, y + slotHeight - 4, x + 1 + cooldownWidth, y + slotHeight - 1, 0xAAFF5500);
            
            int seconds = cooldown / 20 + 1;
            String cdText = seconds + "s";
            guiGraphics.drawCenteredString(mc.font, cdText, x + slotWidth / 2, y + slotHeight / 2, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(mc.font, "§a[" + key + "]", x + slotWidth / 2, y + slotHeight / 2, 0xFFFFFF);
        }
        
        guiGraphics.fill(x, y, x + slotWidth, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + slotHeight, 0xFF666666);
        guiGraphics.fill(x + slotWidth - 1, y, x + slotWidth, y + slotHeight, 0xFF222222);
        guiGraphics.fill(x, y + slotHeight - 1, x + slotWidth, y + slotHeight, 0xFF222222);
    }
}
