package org.alku.life_contract.follower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.alku.life_contract.CreatureEggItem;
import org.alku.life_contract.Life_contract;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;

public final class WandEggStorage extends ItemStackHandler {
    public static final int SIZE = 20;
    public static final String DATA_KEY = "LifeContractWandEggStorage";
    private static final Map<Player, WeakReference<WandEggStorage>> CACHE = new WeakHashMap<>();

    private final Player owner;

    public WandEggStorage(Player owner) {
        super(SIZE);
        this.owner = owner;
        CompoundTag saved = owner.getPersistentData().getCompound(DATA_KEY);
        if (!saved.isEmpty()) {
            deserializeNBT(owner.registryAccess(), saved);
        }
    }

    public static synchronized WandEggStorage getOrCreate(Player owner) {
        WeakReference<WandEggStorage> reference = CACHE.get(owner);
        WandEggStorage storage = reference == null ? null : reference.get();
        if (storage == null) {
            storage = new WandEggStorage(owner);
            CACHE.put(owner, new WeakReference<>(storage));
        }
        return storage;
    }

    public boolean capture(Mob mob) {
        if (!hasSpace()) return false;
        ItemStack egg = new ItemStack(Life_contract.CREATURE_EGG.get());
        if (!CreatureEggItem.captureEntity(egg, mob, true)) {
            return false;
        }

        for (int slot = 0; slot < getSlots(); slot++) {
            if (getStackInSlot(slot).isEmpty()) {
                setStackInSlot(slot, egg);
                owner.containerMenu.broadcastChanges();
                return true;
            }
        }
        return false;
    }

    public boolean hasSpace() {
        for (int slot = 0; slot < getSlots(); slot++) {
            if (getStackInSlot(slot).isEmpty()) return true;
        }
        return false;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return stack.is(Life_contract.CREATURE_EGG.get()) && CreatureEggItem.hasEntity(stack);
    }

    @Override
    public int getSlotLimit(int slot) {
        return 1;
    }

    @Override
    protected void onContentsChanged(int slot) {
        if (!owner.level().isClientSide()) {
            owner.getPersistentData().put(DATA_KEY, serializeNBT(owner.registryAccess()));
        }
    }
}
