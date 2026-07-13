package org.alku.life_contract.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.border.BorderInteractionHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class ServerGamePacketListenerImplMixin {

    @Redirect(
        method = "handleUseItemOn",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;mayInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z"
        ),
        require = 1
    )
    private boolean lifeContract$allowPlacementOutsideBorder(ServerLevel level, Player player, BlockPos pos,
                                                             ServerboundUseItemOnPacket packet) {
        return BorderInteractionHelper.mayInteractIgnoringWorldBorder(level, player, pos);
    }
}
