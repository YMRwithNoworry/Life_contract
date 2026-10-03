package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.DeltaTracker;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ContractHUD {
    private static final ResourceLocation HUD_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "contract_status");

    /** 每帧重建组件没有意义：内容最多每 5 tick（0.25 秒）刷新一次。 */
    private static final int CONTENT_REFRESH_TICKS = 5;

    public static boolean isHudEnabled = true;

    /** 一行 HUD 内容及其到下一行的行距。 */
    private record HudLine(Component text, int color, int lineAdvance) {
    }

    private static List<HudLine> cachedLines = List.of();
    private static int cachedContentHeight;
    private static int lastRefreshTick = -1000;

    private ContractHUD() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, HUD_ID, ContractHUD::renderHud);
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (!isHudEnabled) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }

        if (player.tickCount - lastRefreshTick >= CONTENT_REFRESH_TICKS) {
            lastRefreshTick = player.tickCount;
            rebuildContent(player);
        }

        int x = 10;
        int y = Math.max(4, (guiGraphics.guiHeight() - cachedContentHeight) / 2);
        for (HudLine line : cachedLines) {
            guiGraphics.drawString(mc.font, line.text(), x, y, line.color());
            y += line.lineAdvance();
        }
    }

    /** 组装左侧 HUD 的全部内容（仅在缓存过期时调用）。 */
    private static void rebuildContent(Player player) {
        List<HudLine> lines = new ArrayList<>();

        UUID myUUID = player.getUUID();
        ClientDataStorage.PlayerData myData = ClientDataStorage.get(myUUID);
        String selfMod = myData != null && !myData.contractMod.isBlank() ? myData.contractMod
                : player.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
        UUID leaderUUID = myData != null ? myData.leaderUUID
                : (player.getPersistentData().hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        ? player.getPersistentData().getUUID(TeamOrganizerItem.TAG_LEADER_UUID) : null);
        int teamNumber = myData != null && myData.teamNumber != -1 ? myData.teamNumber
                : (player.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        ? player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER) : -1);
        UUID myTeamUUID = leaderUUID != null ? leaderUUID : myUUID;

        List<ClientDataStorage.PlayerData> teamMembers = new ArrayList<>();
        String myName = player.getName().getString();
        for (ClientDataStorage.PlayerData data : ClientDataStorage.PLAYER_DATA_CACHE.values()) {
            UUID theirTeamUUID = data.leaderUUID != null ? data.leaderUUID : data.playerUUID;
            if (myTeamUUID.equals(theirTeamUUID)) {
                teamMembers.add(data);
            }
        }

        lines.add(new HudLine(Component.translatable("hud.life_contract.title").withStyle(ChatFormatting.YELLOW),
                0xFFFFFF, 12));
        if (teamNumber != -1) {
            lines.add(new HudLine(Component.literal("§6队伍编号: §b" + teamNumber), 0xFFFFFF, 10));
        }

        Component contractLine = selfMod.isEmpty()
                ? Component.translatable("hud.life_contract.contract_mod_none").withStyle(ChatFormatting.GRAY)
                : Component.translatable("hud.life_contract.contract_mod")
                        .withStyle(ChatFormatting.GOLD)
                        .append(Component.literal(selfMod).withStyle(ChatFormatting.GREEN));
        lines.add(new HudLine(contractLine, 0xFFFFFF, 15));

        if (!teamMembers.isEmpty()) {
            lines.add(new HudLine(Component.literal("§6队友:"), 0xFFFFFF, 10));
            for (ClientDataStorage.PlayerData memberData : teamMembers) {
                String memberName = memberData.playerName;
                boolean self = memberName.equals(myName);
                lines.add(new HudLine(Component.literal((self ? "§a● " : "§7- ") + memberName),
                        self ? 0x00FF00 : 0xAAAAAA, 10));
            }
        }

        if (ClientDataStorage.hasWaypoints()) {
            lines.add(new HudLine(Component.literal("§6坐标:"), 0xFFFFFF, 12));

            int portalX = ClientDataStorage.getPortalX();
            int portalY = ClientDataStorage.getPortalY();
            int portalZ = ClientDataStorage.getPortalZ();
            // 传送门在主世界，跨维度时距离没有意义，就不显示
            String distanceText = "";
            if (net.minecraft.world.level.Level.OVERWORLD.equals(player.level().dimension())) {
                int portalDistance = (int) Math.round(Math.sqrt(
                        player.distanceToSqr(portalX + 0.5D, portalY + 0.5D, portalZ + 0.5D)));
                distanceText = " §7(" + portalDistance + "格)";
            }
            lines.add(new HudLine(Component.literal("§7末地传送门: §b" + portalX + " " + portalY + " " + portalZ
                    + distanceText + " "
                    + (ClientDataStorage.isPortalActivated() ? "§a已开启" : "§e未开启")), 0xFFFFFF, 10));
            lines.add(new HudLine(Component.literal("§7边界中心: §b"
                    + ClientDataStorage.getBorderCenterX() + " " + ClientDataStorage.getBorderCenterZ()),
                    0xFFFFFF, 10));
        }

        int height = 0;
        for (HudLine line : lines) {
            height += line.lineAdvance();
        }

        cachedLines = lines;
        cachedContentHeight = height;
    }
}
