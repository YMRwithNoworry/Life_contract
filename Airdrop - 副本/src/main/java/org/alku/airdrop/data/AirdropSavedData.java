package org.alku.airdrop.data;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.*;

public class AirdropSavedData extends SavedData {
    private static final String DATA_NAME = "airdrop_world_config";
    private final Map<String, NonNullList<ItemStack>> lootPools = new HashMap<>();
    private final Map<String, Schedule> schedules = new HashMap<>();
    private double minX = 0, minZ = 0, maxX = 0, maxZ = 0;
    private boolean hasRange = false;

    public static class Schedule {
        public String name;
        public int timeOfDay; // 0-24000 ticks
        public double chance; // 0.0-1.0
        public long lastTriggeredDay = -1;

        public Schedule(String name, int timeOfDay, double chance) {
            this.name = name;
            this.timeOfDay = timeOfDay;
            this.chance = chance;
        }
    }

    public static AirdropSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(AirdropSavedData::load, AirdropSavedData::new, DATA_NAME);
    }

    public void savePool(String name, NonNullList<ItemStack> items) {
        lootPools.put(name, items);
        this.setDirty();
    }

    public boolean removePool(String name) {
        if (lootPools.containsKey(name)) {
            lootPools.remove(name);
            this.setDirty();
            return true;
        }
        return false;
    }

    public Set<String> getPoolNames() {
        return lootPools.keySet();
    }

    public void addSchedule(String name, int time, double chance) {
        schedules.put(name, new Schedule(name, time, chance));
        this.setDirty();
    }

    public boolean removeSchedule(String name) {
        if (schedules.remove(name) != null) {
            this.setDirty();
            return true;
        }
        return false;
    }

    public Map<String, Schedule> getSchedules() {
        return schedules;
    }

    public NonNullList<ItemStack> getPool(String name) {
        return lootPools.getOrDefault(name, NonNullList.withSize(27, ItemStack.EMPTY));
    }

    public NonNullList<ItemStack> getRandomPool() {
        if (lootPools.isEmpty())
            return NonNullList.withSize(27, ItemStack.EMPTY);
        String[] keys = lootPools.keySet().toArray(new String[0]);
        String randomKey = keys[new Random().nextInt(keys.length)];
        return lootPools.get(randomKey);
    }

    public void setRange(AABB box) {
        this.minX = Math.min(box.minX, box.maxX);
        this.maxX = Math.max(box.minX, box.maxX);
        this.minZ = Math.min(box.minZ, box.maxZ);
        this.maxZ = Math.max(box.minZ, box.maxZ);
        this.hasRange = true;
        this.setDirty();
    }

    public void clearRange() {
        this.hasRange = false;
        this.setDirty();
    }

    public boolean hasRange() {
        return hasRange;
    }

    public double[] getRange() {
        return new double[] { minX, minZ, maxX, maxZ };
    }

    public static AirdropSavedData load(CompoundTag tag) {
        AirdropSavedData data = new AirdropSavedData();
        CompoundTag poolsTag = tag.getCompound("Pools");
        for (String key : poolsTag.getAllKeys()) {
            NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(poolsTag.getCompound(key), items);
            data.lootPools.put(key, items);
        }
        if (tag.contains("Range")) {
            CompoundTag range = tag.getCompound("Range");
            data.minX = range.getDouble("minX");
            data.maxX = range.getDouble("maxX");
            data.minZ = range.getDouble("minZ");
            data.maxZ = range.getDouble("maxZ");
            data.hasRange = true;
        }

        if (tag.contains("Schedules")) {
            CompoundTag schedulesTag = tag.getCompound("Schedules");
            for (String key : schedulesTag.getAllKeys()) {
                CompoundTag sTag = schedulesTag.getCompound(key);
                Schedule s = new Schedule(
                        sTag.getString("name"),
                        sTag.getInt("time"),
                        sTag.getDouble("chance"));
                if (sTag.contains("lastDay"))
                    s.lastTriggeredDay = sTag.getLong("lastDay");
                data.schedules.put(key, s);
            }
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        CompoundTag poolsTag = new CompoundTag();
        lootPools.forEach((name, items) -> {
            CompoundTag invTag = new CompoundTag();
            ContainerHelper.saveAllItems(invTag, items);
            poolsTag.put(name, invTag);
        });
        tag.put("Pools", poolsTag);
        if (hasRange) {
            CompoundTag range = new CompoundTag();
            range.putDouble("minX", minX);
            range.putDouble("maxX", maxX);
            range.putDouble("minZ", minZ);
            range.putDouble("maxZ", maxZ);
            tag.put("Range", range);
        }

        CompoundTag schedulesTag = new CompoundTag();
        schedules.forEach((name, s) -> {
            CompoundTag sTag = new CompoundTag();
            sTag.putString("name", s.name);
            sTag.putInt("time", s.timeOfDay);
            sTag.putDouble("chance", s.chance);
            sTag.putLong("lastDay", s.lastTriggeredDay);
            schedulesTag.put(name, sTag);
        });
        tag.put("Schedules", schedulesTag);

        return tag;
    }
}