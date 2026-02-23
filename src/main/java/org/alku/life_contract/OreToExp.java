package org.alku.life_contract;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;

public class OreToExp {
    private static final Map<Item, Integer> ingotValues = new HashMap<>();
    public static final String TAG_INGOT_TO_EXP = "ingot_to_exp_enabled";

    static {
        ingotValues.put(Items.IRON_INGOT, 1);
        ingotValues.put(Items.GOLD_INGOT, 10);
        ingotValues.put(Items.DIAMOND, 100);
        ingotValues.put(Items.EMERALD, 1000);
        ingotValues.put(Items.NETHERITE_INGOT, 50000);
        ingotValues.put(Items.COPPER_INGOT, 1);
        ingotValues.put(Items.NETHERITE_SCRAP, 20);
    }

    public static boolean isEnabled(Player player) {
        return player.getPersistentData().getBoolean(TAG_INGOT_TO_EXP);
    }

    public static void setEnabled(Player player, boolean enabled) {
        player.getPersistentData().putBoolean(TAG_INGOT_TO_EXP, enabled);
    }

    public static void loadEnabled(Player player) {
        if (!player.getPersistentData().contains(TAG_INGOT_TO_EXP)) {
            player.getPersistentData().putBoolean(TAG_INGOT_TO_EXP, false);
        }
    }

    public static int convertIngotsToExp(net.minecraft.server.level.ServerPlayer player) {
        int totalLevels = 0;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (!stack.isEmpty() && ingotValues.containsKey(stack.getItem())) {
                int value = ingotValues.get(stack.getItem());
                totalLevels += value * stack.getCount();
                stack.setCount(0);
            }
        }

        if (totalLevels > 0) {
            player.giveExperienceLevels(totalLevels);
        }

        return totalLevels;
    }
}
