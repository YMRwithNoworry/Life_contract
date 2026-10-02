package org.alku.life_contract.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;

@Mixin(targets = "net.minecraft.world.item.ItemStack")
public abstract class ItemStackNetworkCountMixin {
    @ModifyConstant(method = "lambda$static$3", constant = @Constant(intValue = 99))
    private static int lifeContract$allowExtendedCodecCount(int vanillaLimit) {
        return 1024;
    }
}
