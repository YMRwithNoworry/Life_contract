package org.alku.life_contract.mixin;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DimensionType.class)
public abstract class DimensionTypeHeightMixin {
    @Unique
    private static final int LIFE_CONTRACT_MIN_Y = -128;
    @Unique
    private static final int LIFE_CONTRACT_HEIGHT = 448;

    @Shadow
    @Final
    private int minY;
    @Shadow
    @Final
    private int height;
    @Shadow
    @Final
    private ResourceLocation effectsLocation;

    @Inject(method = "minY", at = @At("HEAD"), cancellable = true, require = 1)
    private void lifeContract$extendOverworldMinY(CallbackInfoReturnable<Integer> cir) {
        if (lifeContract$isVanillaOverworldHeight()) {
            cir.setReturnValue(LIFE_CONTRACT_MIN_Y);
        }
    }

    @Inject(method = "height", at = @At("HEAD"), cancellable = true, require = 1)
    private void lifeContract$extendOverworldHeight(CallbackInfoReturnable<Integer> cir) {
        if (lifeContract$isVanillaOverworldHeight()) {
            cir.setReturnValue(LIFE_CONTRACT_HEIGHT);
        }
    }

    @Inject(method = "logicalHeight", at = @At("HEAD"), cancellable = true, require = 1)
    private void lifeContract$extendOverworldLogicalHeight(CallbackInfoReturnable<Integer> cir) {
        if (lifeContract$isVanillaOverworldHeight()) {
            cir.setReturnValue(LIFE_CONTRACT_HEIGHT);
        }
    }

    @Unique
    private boolean lifeContract$isVanillaOverworldHeight() {
        return this.minY == -64
                && this.height == 384
                && BuiltinDimensionTypes.OVERWORLD_EFFECTS.equals(this.effectsLocation);
    }
}
