package org.alku.life_contract.mixin;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FriendlyByteBuf.class)
public abstract class FriendlyByteBufItemCountMixin {
    @Inject(method = "writeItemStack", at = @At("HEAD"), cancellable = true, remap = false)
    private void lifeContract$writeExtendedCount(ItemStack stack, boolean limitedTag,
                                                  CallbackInfoReturnable<FriendlyByteBuf> cir) {
        FriendlyByteBuf buffer = (FriendlyByteBuf) (Object) this;
        if (stack.isEmpty()) {
            buffer.writeBoolean(false);
        } else {
            buffer.writeBoolean(true);
            Item item = stack.getItem();
            buffer.writeId(BuiltInRegistries.ITEM, item);
            buffer.writeVarInt(stack.getCount());
            CompoundTag tag = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                tag = limitedTag ? stack.getShareTag() : stack.getTag();
            }
            buffer.writeNbt(tag);
        }
        cir.setReturnValue(buffer);
    }

    @Inject(method = "readItem", at = @At("HEAD"), cancellable = true)
    private void lifeContract$readExtendedCount(CallbackInfoReturnable<ItemStack> cir) {
        FriendlyByteBuf buffer = (FriendlyByteBuf) (Object) this;
        if (!buffer.readBoolean()) {
            cir.setReturnValue(ItemStack.EMPTY);
            return;
        }
        Item item = buffer.readById(BuiltInRegistries.ITEM);
        ItemStack stack = new ItemStack(item, buffer.readVarInt());
        stack.readShareTag(buffer.readNbt());
        cir.setReturnValue(stack);
    }
}
