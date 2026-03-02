package org.alku.life_contract.heavy_knight;

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
import org.alku.life_contract.profession.ClientProfessionCache;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class HeavyKnightHUD {

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        
        if (player == null || mc.options.hideGui) return;

        Profession profession = ClientProfessionCache.getCurrentProfession();
        if (profession == null || !profession.isHeavyKnight()) return;

        GuiGraphics guiGraphics = event.getGuiGraphics();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int battleWill = ClientHeavyKnightState.getBattleWill();
        boolean shieldWallActive = ClientHeavyKnightState.isShieldWallActive();
        int chargeCooldown = ClientHeavyKnightState.getChargeCooldown();
        int protectCooldown = ClientHeavyKnightState.getProtectCooldown();
        int shieldBashCooldown = ClientHeavyKnightState.getShieldBashCooldown();
        
        int barWidth = 120;
        int barHeight = 10;
        int slotSize = 24;
        int spacing = 4;
        int totalSkillWidth = 3 * slotSize + 2 * spacing;
        
        int baseX = screenWidth - barWidth - 10;
        int baseY = screenHeight - 10;

        renderBattleWillBar(guiGraphics, mc, baseX, baseY - barHeight - slotSize - 15, battleWill, shieldWallActive, barWidth, barHeight);
        
        int skillStartX = screenWidth - totalSkillWidth - 10;
        renderSkillCooldowns(guiGraphics, mc, skillStartX, baseY - slotSize - 5, profession, chargeCooldown, protectCooldown, shieldBashCooldown, shieldWallActive, slotSize, spacing);
    }

    private static void renderBattleWillBar(GuiGraphics guiGraphics, Minecraft mc, int x, int y, int battleWill, boolean shieldWallActive, int barWidth, int barHeight) {
        guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0xFF333333);
        
        int fillWidth = (int) ((double) battleWill / 100 * barWidth);
        int barColor = battleWill >= 100 ? 0xFFFF6600 : 0xFFAA4444;
        
        if (fillWidth > 0) {
            guiGraphics.fill(x, y, x + fillWidth, y + barHeight, barColor);
        }
        
        guiGraphics.fill(x, y, x + barWidth, y + 1, 0xFF666666);
        guiGraphics.fill(x, y, x + 1, y + barHeight, 0xFF666666);
        guiGraphics.fill(x + barWidth - 1, y, x + barWidth, y + barHeight, 0xFF222222);
        guiGraphics.fill(x, y + barHeight - 1, x + barWidth, y + barHeight, 0xFF222222);

        String willText = "§f战意: §e" + battleWill + "§7/§f100";
        if (battleWill >= 100) {
            willText = "§c§l战意已满！";
        }
        guiGraphics.drawString(mc.font, willText, x + barWidth / 2 - mc.font.width(willText) / 2, y - 10, 0xFFFFFF);
        
        if (shieldWallActive) {
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x44FFAA00);
            String shieldText = "§6§l盾墙激活中";
            guiGraphics.drawString(mc.font, shieldText, x + barWidth / 2 - mc.font.width(shieldText) / 2, y + barHeight + 2, 0xFFFFFF);
        }
        
        if (battleWill >= 100) {
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x44FF6600);
        }
    }

    private static void renderSkillCooldowns(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
            Profession profession, int chargeCd, int protectCd, int bashCd, boolean shieldWallActive, int slotSize, int spacing) {
        renderSkillSlot(guiGraphics, mc, x, y, "冲锋", "Z", chargeCd, profession.getHeavyKnightChargeCooldown(), false, 0xFF44AA44, slotSize);
        renderSkillSlot(guiGraphics, mc, x + slotSize + spacing, y, "援护", "自动", protectCd, profession.getHeavyKnightProtectCooldown(), false, 0xFF4444AA, slotSize);
        renderSkillSlot(guiGraphics, mc, x + 2 * (slotSize + spacing), y, "盾击", "X", bashCd, profession.getHeavyKnightShieldBashCooldown(), shieldWallActive, 0xFFFFAA00, slotSize);
    }

    private static void renderSkillSlot(GuiGraphics guiGraphics, Minecraft mc, int x, int y, 
            String skillName, String key, int cooldown, int maxCooldown, boolean isActive, int activeColor, int slotSize) {
        guiGraphics.fill(x, y, x + slotSize, y + slotSize, 0xFF222222);
        guiGraphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, 0xFF444444);
        
        if (isActive) {
            guiGraphics.fill(x + 1, y + 1, x + slotSize - 1, y + slotSize - 1, activeColor & 0x66FFFFFF);
        }

        int textColor = 0xFFFFFF;
        if (cooldown > 0) {
            textColor = 0xAAAAAA;
            
            int cooldownHeight = (int) ((double) cooldown / maxCooldown * (slotSize - 2));
            guiGraphics.fill(x + 1, y + slotSize - 1 - cooldownHeight, x + slotSize - 1, y + slotSize - 1, 0x88000000);
            
            double seconds = cooldown / 20.0;
            String cdText = String.format("%.1f", seconds);
            guiGraphics.drawCenteredString(mc.font, cdText, x + slotSize / 2, y + slotSize / 2 - 4, 0xFFFFFF);
        } else {
            guiGraphics.drawCenteredString(mc.font, skillName, x + slotSize / 2, y + 4, textColor);
            guiGraphics.drawCenteredString(mc.font, "§7[" + key + "]", x + slotSize / 2, y + slotSize - 10, 0xAAAAAA);
        }
        
        if (isActive && cooldown <= 0) {
            guiGraphics.drawCenteredString(mc.font, "§e★", x + slotSize / 2, y + slotSize - 10, 0xFFFFFF);
        }
    }
}
