package org.alku.life_contract.mixin;

import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.border.WorldBorder;
import org.alku.life_contract.client.BorderStatusHUD;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(MultiPlayerGameMode.class)
public abstract class MultiPlayerGameModeBorderMixin {
    @Redirect(
            method = {"startDestroyBlock", "continueDestroyBlock", "useItemOn"},
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/border/WorldBorder;isWithinBounds(Lnet/minecraft/core/BlockPos;)Z"
            ),
            require = 3
    )
    private boolean lifeContract$allowBlockActionsOutsideBorder(WorldBorder border, BlockPos pos) {
        return BorderStatusHUD.isBorderStatusActive() || border.isWithinBounds(pos);
    }
}
