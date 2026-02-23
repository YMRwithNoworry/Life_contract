package org.alku.life_contract;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class TeamInventory implements Container {

    private static final Map<UUID, TeamInventory> INVENTORIES = new HashMap<>();
    private static final String TAG_TEAM_INVENTORY = "TeamInventoryItems";

    private final NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private final UUID leaderUUID;

    private TeamInventory(UUID leaderUUID) {
        this.leaderUUID = leaderUUID;
    }

    public static TeamInventory getOrCreate(Player player) {
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        if (leaderUUID == null) {
            leaderUUID = player.getUUID();
        }
        return INVENTORIES.computeIfAbsent(leaderUUID, TeamInventory::new);
    }

    public static TeamInventory getByLeaderUUID(UUID leaderUUID) {
        return INVENTORIES.get(leaderUUID);
    }

    @SubscribeEvent
    public static void onPlayerSave(PlayerEvent.SaveToFile event) {
        saveToPlayer(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
        loadFromPlayer(event.getEntity());
    }

    private static void saveToPlayer(Player player) {
        if (!player.level().isClientSide && player instanceof ServerPlayer) {
            UUID leaderUUID = ContractEvents.getLeaderUUID(player);
            if (leaderUUID == null) {
                leaderUUID = player.getUUID();
            }
            TeamInventory inv = INVENTORIES.get(leaderUUID);
            if (inv != null) {
                CompoundTag data = player.getPersistentData();
                ListTag listTag = new ListTag();
                for (int i = 0; i < inv.items.size(); i++) {
                    ItemStack stack = inv.items.get(i);
                    if (!stack.isEmpty()) {
                        CompoundTag tag = new CompoundTag();
                        tag.putByte("Slot", (byte) i);
                        stack.save(tag);
                        listTag.add(tag);
                    }
                }
                data.put(TAG_TEAM_INVENTORY, listTag);
            }
        }
    }

    private static void loadFromPlayer(Player player) {
        if (!player.level().isClientSide && player instanceof ServerPlayer) {
            UUID leaderUUID = ContractEvents.getLeaderUUID(player);
            if (leaderUUID == null) {
                leaderUUID = player.getUUID();
            }
            TeamInventory inv = INVENTORIES.computeIfAbsent(leaderUUID, TeamInventory::new);
            CompoundTag data = player.getPersistentData();
            if (data.contains(TAG_TEAM_INVENTORY)) {
                ListTag listTag = data.getList(TAG_TEAM_INVENTORY, 10);
                for (int i = 0; i < listTag.size(); i++) {
                    CompoundTag tag = listTag.getCompound(i);
                    int slot = tag.getByte("Slot") & 255;
                    if (slot >= 0 && slot < inv.items.size()) {
                        inv.items.set(slot, ItemStack.of(tag));
                    }
                }
            }
        }
    }

    @Override
    public int getContainerSize() {
        return 54;
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(items, slot, amount);
        if (!stack.isEmpty()) {
            setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        items.set(slot, stack);
        if (stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public boolean stillValid(Player player) {
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        if (leaderUUID == null) {
            leaderUUID = player.getUUID();
        }
        return this.leaderUUID.equals(leaderUUID);
    }

    @Override
    public void clearContent() {
        items.clear();
    }
}
