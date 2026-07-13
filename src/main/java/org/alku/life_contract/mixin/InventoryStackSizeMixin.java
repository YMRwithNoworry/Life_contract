package org.alku.life_contract.mixin;

import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import org.alku.life_contract.items.SublimationItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Inventory.class)
public abstract class InventoryStackSizeMixin implements Container {
    @Override
    public int getMaxStackSize() {
        return SublimationItem.MAX_STACK_SIZE;
    }
}
