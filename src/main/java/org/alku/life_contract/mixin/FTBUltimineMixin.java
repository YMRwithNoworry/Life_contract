package org.alku.life_contract.mixin;

import dev.ftb.mods.ftbultimine.FTBUltimine;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.compat.UltimineRestriction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FTBUltimine.class, remap = false)
public abstract class FTBUltimineMixin {
    @Inject(method = "canUltimine", at = @At("HEAD"), cancellable = true, remap = false)
    private void lifeContract$restrictUltimine(Player player, CallbackInfoReturnable<Boolean> cir) {
        if (player instanceof ServerPlayer serverPlayer && !UltimineRestriction.canUse(serverPlayer)) {
            cir.setReturnValue(false);
        }
    }
}
