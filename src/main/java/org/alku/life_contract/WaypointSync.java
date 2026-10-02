package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import org.alku.life_contract.border.BorderManager;
import org.alku.life_contract.endgame.StrongholdEndgameManager;
import org.alku.life_contract.events.GameEventManager;

/**
 * 把本局的关键坐标（末地传送门、边界中心）同步给客户端，供左侧 HUD 显示。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class WaypointSync {

    private WaypointSync() {
    }

    /** 开局、传送门开启等坐标变化时广播。 */
    public static void broadcast() {
        NetworkHandler.sendToAllPlayers(GameEventManager.isGameActive()
                ? collect()
                : PacketSyncWaypoints.INACTIVE);
    }

    /** 对局结束时清掉 HUD 上的坐标。 */
    public static void clear() {
        NetworkHandler.sendToAllPlayers(PacketSyncWaypoints.INACTIVE);
    }

    public static void sendTo(ServerPlayer player) {
        NetworkHandler.sendToPlayer(player, GameEventManager.isGameActive()
                ? collect()
                : PacketSyncWaypoints.INACTIVE);
    }

    /** 玩家上线（含中途加入）时补一份坐标。 */
    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            sendTo(player);
        }
    }

    private static PacketSyncWaypoints collect() {
        BlockPos portal = StrongholdEndgameManager.getPortalCenter();
        if (portal == null) {
            return PacketSyncWaypoints.INACTIVE;
        }

        BorderManager.BorderData border = BorderManager.getCurrentBorder();
        int centerX = border != null ? (int) Math.round(border.getCenterX()) : 0;
        int centerZ = border != null ? (int) Math.round(border.getCenterZ()) : 0;
        return new PacketSyncWaypoints(true,
                portal.getX(), portal.getY(), portal.getZ(), StrongholdEndgameManager.isPortalActivated(),
                centerX, centerZ);
    }
}
