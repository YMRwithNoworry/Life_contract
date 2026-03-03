package org.alku.life_contract.gourmet;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public class GourmetHUD {

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) return;

        while (GourmetKeyHandler.EMERGENCY_STIR_FRY.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketGourmetSkill(PacketGourmetSkill.EMERGENCY_STIR_FRY));
        }

        while (GourmetKeyHandler.FLAVOR_BOMB.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketGourmetSkill(PacketGourmetSkill.FLAVOR_BOMB,
                mc.player.getX(), mc.player.getY(), mc.player.getZ()));
        }

        while (GourmetKeyHandler.WARM_FEED.consumeClick()) {
            boolean isGroupMode = mc.options.keyShift.isDown();
            NetworkHandler.CHANNEL.sendToServer(new PacketGourmetSkill(PacketGourmetSkill.WARM_FEED, isGroupMode));
        }

        while (GourmetKeyHandler.GOD_CHEF_DESCENT.consumeClick()) {
            NetworkHandler.CHANNEL.sendToServer(new PacketGourmetSkill(PacketGourmetSkill.GOD_CHEF_DESCENT));
        }
    }

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiOverlayEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.options.hideGui) return;

        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (!"gourmet".equals(professionId)) return;

        RenderSystem.disableDepthTest();

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        int umami = ClientGourmetState.getUmami();
        int tier = ClientGourmetState.getUmamiTier();

        int barWidth = 100;
        int barHeight = 10;
        int x = screenWidth / 2 - barWidth / 2;
        int y = screenHeight - 60;

        int umamiWidth = (int) ((umami / 200.0) * barWidth);

        int color = getTierColor(tier);
        fill(event, x, y, x + barWidth, y + barHeight, 0x80000000);
        fill(event, x, y, x + umamiWidth, y + barHeight, color | 0xFF000000);

        String umamiText = "鲜味值: " + umami + "/200";
        event.getGuiGraphics().drawString(mc.font, umamiText, x + barWidth / 2 - mc.font.width(umamiText) / 2, y + 1, 0xFFFFFF);

        String tierText = getTierText(tier);
        event.getGuiGraphics().drawString(mc.font, tierText, x + barWidth / 2 - mc.font.width(tierText) / 2, y - 10, color);

        drawSkillPanel(event, mc, screenWidth, screenHeight);

        RenderSystem.enableDepthTest();
    }

    private static void drawSkillPanel(RenderGuiOverlayEvent.Post event, Minecraft mc, int screenWidth, int screenHeight) {
        int emergencyCd = ClientGourmetState.getEmergencyCooldown();
        int bombCd = ClientGourmetState.getFlavorBombCooldown();
        int feedCd = ClientGourmetState.getWarmFeedCooldown();
        int godChefCd = ClientGourmetState.getGodChefCooldown();
        boolean godChefMode = ClientGourmetState.isGodChefMode();

        int panelWidth = 120;
        int panelHeight = 70;
        int padding = 10;
        int panelX = screenWidth - panelWidth - padding;
        int panelY = screenHeight - panelHeight - padding;

        fill(event, panelX, panelY, panelX + panelWidth, panelY + panelHeight, 0x80000000);

        String title = "§6[美食家技能]";
        event.getGuiGraphics().drawString(mc.font, title, panelX + panelWidth / 2 - mc.font.width(title) / 2, panelY + 3, 0xFFFFFF);

        int skillY = panelY + 15;
        int lineHeight = 14;

        drawSkillLine(event, mc, panelX + 5, skillY, "Z", "应急快炒", emergencyCd, 0x55FF55, 0xAAAAAA);
        skillY += lineHeight;
        drawSkillLine(event, mc, panelX + 5, skillY, "X", "风味爆弹", bombCd, 0x55FF55, 0xAAAAAA);
        skillY += lineHeight;
        drawSkillLine(event, mc, panelX + 5, skillY, "C", "暖心投喂", feedCd, 0x55FF55, 0xAAAAAA);
        skillY += lineHeight;
        drawSkillLine(event, mc, panelX + 5, skillY, "V", "厨神降临", godChefCd, 0xFFAA00, 0xAAAAAA, godChefMode);
    }

    private static void drawSkillLine(RenderGuiOverlayEvent.Post event, Minecraft mc, int x, int y, 
                                       String key, String skillName, int cooldown, int readyColor, int cdColor) {
        drawSkillLine(event, mc, x, y, key, skillName, cooldown, readyColor, cdColor, false);
    }

    private static void drawSkillLine(RenderGuiOverlayEvent.Post event, Minecraft mc, int x, int y, 
                                       String key, String skillName, int cooldown, int readyColor, int cdColor, boolean isActive) {
        String keyText = "[" + key + "]";
        int keyWidth = mc.font.width(keyText);

        event.getGuiGraphics().drawString(mc.font, keyText, x, y, 0xFFFF00);

        if (isActive) {
            String activeText = skillName + " §a激活中!";
            event.getGuiGraphics().drawString(mc.font, activeText, x + keyWidth + 3, y, 0xFFAA00);
        } else if (cooldown > 0) {
            int seconds = cooldown / 20;
            String cdText = skillName + " §c" + seconds + "s";
            event.getGuiGraphics().drawString(mc.font, cdText, x + keyWidth + 3, y, cdColor);
        } else {
            String readyText = skillName + " §a就绪";
            event.getGuiGraphics().drawString(mc.font, readyText, x + keyWidth + 3, y, readyColor);
        }
    }

    private static int getTierColor(int tier) {
        return switch (tier) {
            case 4 -> 0xFFAA00;
            case 3 -> 0xAA00FF;
            case 2 -> 0x00AAFF;
            default -> 0xFFFFFF;
        };
    }

    private static String getTierText(int tier) {
        return switch (tier) {
            case 4 -> "§6厨神模式";
            case 3 -> "§d高阶美食家";
            case 2 -> "§b进阶美食家";
            default -> "§f美食家";
        };
    }

    private static void fill(RenderGuiOverlayEvent.Post event, int x1, int y1, int x2, int y2, int color) {
        net.minecraft.client.gui.GuiGraphics graphics = event.getGuiGraphics();
        graphics.fill(x1, y1, x2, y2, color);
    }
}
