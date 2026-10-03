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

    /**
     * 让 HUD 下一帧就重建内容。
     * <p>
     * 收到同步包（契约模组/队伍变更）时调用，玩家改完契约不用等下一个 5 tick 刷新点。
     */
    public static void invalidateCache() {
        lastRefreshTick = -1000;
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
        net.minecraft.client.multiplayer.ClientPacketListener connection = Minecraft.getInstance().getConnection();
        // Tab 列表里连自己都没有（还没同步完 / 被服务端插件隐藏）时就不过滤，免得队友全被藏掉
        boolean tabListUsable = connection != null && connection.getPlayerInfo(myUUID) != null;
        for (ClientDataStorage.PlayerData data : ClientDataStorage.PLAYER_DATA_CACHE.values()) {
            // 只列在线玩家：客户端缓存是静态的，换服/换世界后不会自动清，
            // 不按 Tab 列表过滤就会把早就不在的玩家一直挂在 HUD 上（"更新不及时"的来源之一）。
            if (tabListUsable && data.playerUUID != null && connection.getPlayerInfo(data.playerUUID) == null) {
                continue;
            }
            UUID theirTeamUUID = data.leaderUUID != null ? data.leaderUUID : data.playerUUID;
            if (myTeamUUID.equals(theirTeamUUID)) {
                teamMembers.add(data);
            }
        }
        // 固定顺序：自己排最前，其余按名字排序。
        // 缓存是 HashMap，顺序每次都可能不同，HUD 会看起来"一会儿一个样"。
        teamMembers.sort(java.util.Comparator
                .comparing((ClientDataStorage.PlayerData data) -> !myName.equals(data.playerName))
                .thenComparing(data -> data.playerName));

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
                // 队友的契约模组直接跟在名字后面，一眼能看出同盟阵营
                String memberMod = memberData.contractMod == null || memberData.contractMod.isBlank()
                        ? "无" : memberData.contractMod;
                lines.add(new HudLine(Component.literal((self ? "§a● " : "§7- ") + memberName
                        + " §8[§f" + memberMod + "§8]"),
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
