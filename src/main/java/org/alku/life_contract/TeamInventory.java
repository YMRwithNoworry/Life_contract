package org.alku.life_contract;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeamInventory implements Container {

    private static final String DATA_NAME = Life_contract.MODID + "_team_inventory";
    private static final Map<UUID, TeamInventory> CLIENT_CACHE = new HashMap<>();
    
    private final NonNullList<ItemStack> items;
    private final UUID teamId;
    private int openCount = 0;
    private TeamInventoryData parentData;

    public TeamInventory(UUID teamId) {
        this.teamId = teamId;
        this.items = NonNullList.withSize(54, ItemStack.EMPTY);
    }

    public TeamInventory(UUID teamId, NonNullList<ItemStack> items) {
        this.teamId = teamId;
        this.items = items;
    }

    public static TeamInventory getOrCreate(Player player) {
        UUID teamId = getTeamId(player);
        
        if (!player.level().isClientSide) {
            return getOrCreateServer(teamId);
        }
        
        TeamInventory cached = CLIENT_CACHE.get(teamId);
        if (cached != null) {
            return cached;
        }
        
        return new TeamInventory(teamId);
    }

    public static TeamInventory getOrCreateServer(UUID teamId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return new TeamInventory(teamId);
        }
        
        DimensionDataStorage storage = server.overworld().getDataStorage();
        TeamInventoryData data = storage.computeIfAbsent(TeamInventoryData::load, TeamInventoryData::new, DATA_NAME);
        
        TeamInventory inv = data.getInventory(teamId);
        inv.parentData = data;
        return inv;
    }

    public static TeamInventory getByTeamId(UUID teamId) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            DimensionDataStorage storage = server.overworld().getDataStorage();
            TeamInventoryData data = storage.get(TeamInventoryData::load, DATA_NAME);
            if (data != null) {
                TeamInventory inv = data.getInventory(teamId);
                if (inv != null) {
                    inv.parentData = data;
                    return inv;
                }
            }
        }
        return CLIENT_CACHE.get(teamId);
    }

    private static UUID getTeamId(Player player) {
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        return leaderUUID != null ? leaderUUID : player.getUUID();
    }

    public static void setClientInventory(UUID teamId, NonNullList<ItemStack> items) {
        TeamInventory inv = new TeamInventory(teamId, items);
        CLIENT_CACHE.put(teamId, inv);
    }

    public static void clearClientCache() {
        CLIENT_CACHE.clear();
    }

    public static TeamInventory load(UUID teamId, CompoundTag tag) {
        NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
        ListTag listTag = tag.getList("Items", Tag.TAG_COMPOUND);
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag itemTag = listTag.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(itemTag));
            }
        }
        return new TeamInventory(teamId, items);
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag listTag = new ListTag();
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                listTag.add(itemTag);
            }
        }
        tag.put("Items", listTag);
        return tag;
    }

    public void broadcastChanges() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        UUID teamId = this.teamId;
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID playerTeamId = getTeamId(player);
            if (teamId.equals(playerTeamId)) {
                if (player.containerMenu instanceof TeamInventoryMenu menu) {
                    if (menu.getTeamInventory() == this) {
                        menu.broadcastChanges();
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
        if (slot >= 0 && slot < items.size()) {
            return items.get(slot);
        }
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack result = ContainerHelper.removeItem(items, slot, amount);
        if (!result.isEmpty()) {
            setChanged();
            broadcastChanges();
        }
        return result;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        if (slot >= 0 && slot < items.size()) {
            ItemStack stack = items.get(slot);
            items.set(slot, ItemStack.EMPTY);
            setChanged();
            broadcastChanges();
            return stack;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        if (slot >= 0 && slot < items.size()) {
            items.set(slot, stack);
            if (stack.getCount() > getMaxStackSize()) {
                stack.setCount(getMaxStackSize());
            }
            setChanged();
            broadcastChanges();
        }
    }

    @Override
    public void setChanged() {
        if (parentData != null) {
            parentData.setDirty();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        UUID playerTeamId = getTeamId(player);
        return teamId.equals(playerTeamId);
    }

    @Override
    public void clearContent() {
        items.clear();
        setChanged();
    }

    public UUID getTeamId() {
        return teamId;
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    public void startOpen(Player player) {
        openCount++;
    }

    @Override
    public void stopOpen(Player player) {
        openCount--;
    }

    public static class TeamInventoryData extends SavedData {
        private final Map<UUID, TeamInventory> inventories = new HashMap<>();

        public TeamInventoryData() {}

        public static TeamInventoryData load(CompoundTag tag) {
            TeamInventoryData data = new TeamInventoryData();
            ListTag listTag = tag.getList("Teams", Tag.TAG_COMPOUND);
            for (int i = 0; i < listTag.size(); i++) {
                CompoundTag teamTag = listTag.getCompound(i);
                UUID teamId = teamTag.getUUID("TeamId");
                TeamInventory inv = TeamInventory.load(teamId, teamTag);
                inv.parentData = data;
                data.inventories.put(teamId, inv);
            }
            return data;
        }

        @Override
        public CompoundTag save(CompoundTag tag) {
            ListTag listTag = new ListTag();
            for (Map.Entry<UUID, TeamInventory> entry : inventories.entrySet()) {
                CompoundTag teamTag = new CompoundTag();
                teamTag.putUUID("TeamId", entry.getKey());
                entry.getValue().save(teamTag);
                listTag.add(teamTag);
            }
            tag.put("Teams", listTag);
            return tag;
        }

        public TeamInventory getInventory(UUID teamId) {
            TeamInventory inv = inventories.computeIfAbsent(teamId, TeamInventory::new);
            inv.parentData = this;
            return inv;
        }
    }
}
