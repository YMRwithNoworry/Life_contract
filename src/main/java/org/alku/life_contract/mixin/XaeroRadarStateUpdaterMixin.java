package org.alku.life_contract.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.ContractEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import xaero.hud.minimap.radar.state.RadarList;
import xaero.hud.minimap.radar.state.RadarStateUpdater;

@Mixin(value = RadarStateUpdater.class, remap = false)
public abstract class XaeroRadarStateUpdaterMixin {
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
                && !ContractEvents.isSameTeam(localPlayer, otherPlayer)) {
            return false;
        }
        return radarList.add(entity);
    }
}
