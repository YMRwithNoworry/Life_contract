package org.alku.life_contract.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public abstract class ItemStackCountMixin {
    @Inject(method = "<init>(Lnet/minecraft/nbt/CompoundTag;)V", at = @At("RETURN"))
    private void lifeContract$readExtendedCount(CompoundTag tag, CallbackInfo ci) {
        if (tag.contains("Count", Tag.TAG_INT)) {
            ((ItemStack) (Object) this).setCount(tag.getInt("Count"));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void lifeContract$saveExtendedCount(CompoundTag tag, org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable<CompoundTag> cir) {
        tag.putInt("Count", ((ItemStack) (Object) this).getCount());
    }
}
