package org.alku.life_contract.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.ContractEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.hud.minimap.radar.state.RadarList;
import xaero.hud.minimap.radar.state.RadarStateUpdater;

import java.util.UUID;

@Mixin(value = RadarStateUpdater.class, remap = false)
public abstract class XaeroRadarStateUpdaterMixin {
    /**
     * 本地玩家的队伍 ID 每 tick 只解析一次。
     * 雷达一次更新会遍历大量实体，原实现对每个实体都要重新读一遍本地玩家的持久化数据。
     */
    @Unique
    private static UUID lifeContract$localTeamId;
    @Unique
    private static int lifeContract$localTeamTick = -1000;

    @Redirect(
            method = "update",
            at = @At(
                    value = "INVOKE",
                    target = "Lxaero/hud/minimap/radar/state/RadarList;add(Lnet/minecraft/world/entity/Entity;)Z"
            ),
            remap = false
    )
    private boolean lifeContract$hideEnemyPlayers(RadarList radarList, Entity entity) {
        Player localPlayer = Minecraft.getInstance().player;
        if (entity instanceof Player otherPlayer
                && localPlayer != null
                && otherPlayer != localPlayer
                && !lifeContract$isSameTeam(localPlayer, otherPlayer)) {
            return false;
        }
        return radarList.add(entity);
    }

    /** 与 ContractEvents.isSameTeam 语义一致，但本地一侧按 tick 缓存。 */
    @Unique
    private static boolean lifeContract$isSameTeam(Player localPlayer, Player otherPlayer) {
        if (localPlayer.tickCount != lifeContract$localTeamTick) {
            lifeContract$localTeamTick = localPlayer.tickCount;
            UUID leader = ContractEvents.getLeaderUUID(localPlayer);
            lifeContract$localTeamId = leader != null ? leader : localPlayer.getUUID();
        }

        UUID otherLeader = ContractEvents.getLeaderUUID(otherPlayer);
        UUID otherTeamId = otherLeader != null ? otherLeader : otherPlayer.getUUID();
        return lifeContract$localTeamId != null && lifeContract$localTeamId.equals(otherTeamId);
    }
}
