package org.alku.life_contract.mixin;

import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.follower.WandFollowerSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(Player.class)
public abstract class PlayerExhaustionMixin {
    private static final float WAND_FOLLOWER_EXHAUSTION_MULTIPLIER = 1.10F;

    @ModifyVariable(
            method = "causeFoodExhaustion",
            at = @At("HEAD"),
            argsOnly = true,
            require = 1)
    private float lifeContract$addWandFollowerExhaustion(float exhaustion) {
        Player player = (Player) (Object) this;
        if (!player.level().isClientSide() && WandFollowerSystem.hasBoundFollower(player)) {
            return exhaustion * WAND_FOLLOWER_EXHAUSTION_MULTIPLIER;
        }
        return exhaustion;
    }
}
