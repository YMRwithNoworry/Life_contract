package org.alku.life_contract.mixin;

import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(NoiseGeneratorSettings.class)
public abstract class NoiseGeneratorSettingsMixin {
    @Inject(method = "noiseSettings", at = @At("RETURN"), cancellable = true, require = 1)
    private void lifeContract$extendOverworldNoise(CallbackInfoReturnable<NoiseSettings> cir) {
        NoiseSettings original = cir.getReturnValue();
        if (original.minY() == -64 && original.height() == 384) {
            cir.setReturnValue(NoiseSettings.create(
                    -128,
                    448,
                    original.noiseSizeHorizontal(),
                    original.noiseSizeVertical()));
        }
    }
}
