package org.alku.life_contract.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(ItemStack.class)
public abstract class ItemStackCountMixin {
    @Inject(method = "parse", at = @At("RETURN"))
    private static void lifeContract$readExtendedCount(HolderLookup.Provider registries, Tag tag,
                                                        CallbackInfoReturnable<Optional<ItemStack>> cir) {
        if (tag instanceof CompoundTag compound && compound.contains("Count", Tag.TAG_INT)) {
            cir.getReturnValue().ifPresent(stack -> stack.setCount(compound.getInt("Count")));
        }
    }

    @Inject(method = "save", at = @At("RETURN"))
    private void lifeContract$saveExtendedCount(HolderLookup.Provider registries, Tag tag,
                                                  CallbackInfoReturnable<Tag> cir) {
        if (cir.getReturnValue() instanceof CompoundTag compound) {
            compound.putInt("Count", ((ItemStack) (Object) this).getCount());
        }
    }
}
