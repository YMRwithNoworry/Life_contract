package org.alku.life_contract.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerGameMode.class)
public abstract class ServerPlayerGameModeMixin {

    @Redirect(
        method = "handleBlockBreakAction",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/server/level/ServerLevel;mayInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/core/BlockPos;)Z"
        ),
        require = 0
    )
    private boolean lifeContract$allowMiningOutsideBorder(ServerLevel level, Player player, BlockPos pos,
                                                          BlockPos packetPos,
                                                          ServerboundPlayerActionPacket.Action action,
                                                          Direction direction,
                                                          int maxBuildHeight,
                                                          int sequence) {
        if (level.getWorldBorder().isWithinBounds(pos)) {
            return level.mayInteract(player, pos);
        }

        if (player instanceof ServerPlayer serverPlayer &&
                level.getServer().isUnderSpawnProtection(level, pos, serverPlayer)) {
            return false;
        }

        return true;
    }
}
