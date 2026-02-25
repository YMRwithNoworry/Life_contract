package org.alku.life_contract.jungle_ape_god;

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

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class JungleApeHUD {

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) return;

        String professionId = getProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isJungleApeGod()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        CompoundTag data = player.getPersistentData();
        int rhythmStacks = data.getInt("JungleApeRhythmStacksClient");
        int maxStacks = profession.getRhythmStacksMax();
        boolean isBerserk = data.getBoolean("JungleApeBerserkClient");
        boolean isRActive = data.getBoolean("JungleApeRActiveClient");
        
        int q1Cooldown = data.getInt("JungleApeQ1CooldownClient");
        int q2Cooldown = data.getInt("JungleApeQ2CooldownClient");
        int q3Cooldown = data.getInt("JungleApeQ3CooldownClient");
        int rCooldown = data.getInt("JungleApeRCooldownClient");

        int barWidth = 120;
        int barHeight = 8;
        int slotSize = 20;
        int spacing = 2;
        int totalSkillWidth = 4 * slotSize + 3 * spacing;
        
        int baseX = screenWidth - barWidth - 10;
        int baseY = screenHeight - 10;

        renderRhythmBar(guiGraphics, mc, baseX, baseY - barHeight - 25, rhythmStacks, maxStacks, isBerserk, barWidth, barHeight);
        
        int skillStartX = screenWidth - totalSkillWidth - 10;
        renderSkillCooldowns(guiGraphics, mc, skillStartX, baseY - slotSize - 5, profession, q1Cooldown, q2Cooldown, q3Cooldown, rCooldown, isBerserk, isRActive, slotSize, spacing);
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
        
        return "";
    }

    private static void renderRhythmBar(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int stacks, int maxStacks, boolean isBerserk, int barWidth, int barHeight) {
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF333333);
        
        int fillWidth = (int) ((double) stacks / maxStacks * barWidth);
        int barColor = isBerserk ? 0xFFFF4444 : 0xFF44FF44;
        
        if (fillWidth > 0) {
            guiGraphics.fill(x, y, x + fillWidth, y + barHeight, barColor);
        }
        
        guiGraphics.fill(x, y, x + barWidth, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + barHeight, 0xFF666666);
        guiGraphics.fill(x + barWidth - 1, y, x + barWidth, y + barHeight, 0xFF222222);
        guiGraphics.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, 0xFF222222);

        String stackText = "§f律动: §e" + stacks + "§7/§f" + maxStacks;
        if (isBerserk) {
            stackText = "§c§l暴走状态！";
        }
        guiGraphics.drawString(mc.font, stackText, x + barWidth / 2 - mc.font.width(stackText) / 2, y - 10, 0xFFFFFF);
        
        if (isBerserk) {
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x44FF0000);
        }
    }

    private static void renderSkillCooldowns(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
            Profession profession, int q1Cd, int q2Cd, int q3Cd, int rCd, boolean isBerserk, boolean isRActive, int slotSize, int spacing) {
        renderSkillSlot(guiGraphics, mc, x, y, "Q1", q1Cd, profession.getQ1Cooldown(), isBerserk, 0xFF44AA44, slotSize);
        renderSkillSlot(guiGraphics, mc, x + slotSize + spacing, y, "Q2", q2Cd, profession.getQ2Cooldown(), isBerserk, 0xFF4444AA, slotSize);
        renderSkillSlot(guiGraphics, mc, x + 2 * (slotSize + spacing), y, "Q3", q3Cd, profession.getQ3Cooldown(), isBerserk, 0xFFAA44AA, slotSize);
        renderSkillSlot(guiGraphics, mc, x + 3 * (slotSize + spacing), y, "R", rCd, profession.getRCooldown(), isRActive, 0xFFFFAA00, slotSize);
    }

    private static void renderSkillSlot(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
            String skillName, int cooldown, int maxCooldown, boolean isActive, int activeColor, int slotSize) {
        guiGraphics.fill(x, y, x + slotSize, y + slotSize, 0xFF222222);
        guiGraphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, 0xFF444444);
        
        if (isActive) {
            guiGraphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, activeColor & 0x66FFFFFF);
        }

        int textColor = 0xFFFFFF;
        if (cooldown > 0) {
            textColor = 0xAAAAAA;
            
            int cooldownHeight = (int) ((double) cooldown / maxCooldown * slotSize);
            guiGraphics.fill(x + 1, y + slotSize - 1 - cooldownHeight, x + slotSize - 1, y + slotSize - 1, 0x88000000);
            
            int seconds = cooldown / 20 + 1;
            String cdText = seconds + "s";
            guiGraphics.drawCenteredString(mc.font, cdText, x + slotSize / 2, y + slotSize / 2 - 4, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(mc.font, skillName, x + slotSize / 2, y + slotSize / 2 - 4, textColor);
            
            if (!isActive) {
                guiGraphics.drawCenteredString(mc.font, "§a✓", x + slotSize / 2, y + slotSize - 8, 0xFFFFFF);
            }
        }
        
        if (isActive && cooldown <= 0) {
            guiGraphics.drawCenteredString(mc.font, "§e★", x + slotSize / 2, y + slotSize - 8, 0xFFFFFF);
        }
    }
}
