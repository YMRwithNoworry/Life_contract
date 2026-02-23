package org.alku.life_contract;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class TeamSmelter {

    private static final Map<Item, Item> SMELTING_MAP = new HashMap<>();
    private static final Map<Item, Item> COOKING_MAP = new HashMap<>();

    static {
        SMELTING_MAP.put(Items.RAW_IRON, Items.IRON_INGOT);
        SMELTING_MAP.put(Items.RAW_GOLD, Items.GOLD_INGOT);
        SMELTING_MAP.put(Items.RAW_COPPER, Items.COPPER_INGOT);
        SMELTING_MAP.put(Items.IRON_ORE, Items.IRON_INGOT);
        SMELTING_MAP.put(Items.GOLD_ORE, Items.GOLD_INGOT);
        SMELTING_MAP.put(Items.COPPER_ORE, Items.COPPER_INGOT);
        SMELTING_MAP.put(Items.DEEPSLATE_IRON_ORE, Items.IRON_INGOT);
        SMELTING_MAP.put(Items.DEEPSLATE_GOLD_ORE, Items.GOLD_INGOT);
        SMELTING_MAP.put(Items.DEEPSLATE_COPPER_ORE, Items.COPPER_INGOT);
        SMELTING_MAP.put(Items.NETHER_GOLD_ORE, Items.GOLD_INGOT);
        SMELTING_MAP.put(Items.ANCIENT_DEBRIS, Items.NETHERITE_SCRAP);

        addModSmeltingRecipe("raw_zinc", "zinc_ingot");
        addModSmeltingRecipe("raw_tin", "tin_ingot");
        addModSmeltingRecipe("raw_lead", "lead_ingot");
        addModSmeltingRecipe("raw_silver", "silver_ingot");
        addModSmeltingRecipe("raw_nickel", "nickel_ingot");
        addModSmeltingRecipe("raw_osmium", "osmium_ingot");
        addModSmeltingRecipe("raw_uranium", "uranium_ingot");
        addModSmeltingRecipe("raw_platinum", "platinum_ingot");
        addModSmeltingRecipe("raw_tungsten", "tungsten_ingot");
        addModSmeltingRecipe("osmium_ore", "osmium_ingot");
        addModSmeltingRecipe("tin_ore", "tin_ingot");
        addModSmeltingRecipe("lead_ore", "lead_ingot");
        addModSmeltingRecipe("silver_ore", "silver_ingot");
        addModSmeltingRecipe("nickel_ore", "nickel_ingot");
        addModSmeltingRecipe("uranium_ore", "uranium_ingot");
        addModSmeltingRecipe("platinum_ore", "platinum_ingot");
        addModSmeltingRecipe("tungsten_ore", "tungsten_ingot");
        addModSmeltingRecipe("zinc_ore", "zinc_ingot");
        addModSmeltingRecipe("deepslate_tin_ore", "tin_ingot");
        addModSmeltingRecipe("deepslate_lead_ore", "lead_ingot");
        addModSmeltingRecipe("deepslate_silver_ore", "silver_ingot");
        addModSmeltingRecipe("deepslate_nickel_ore", "nickel_ingot");
        addModSmeltingRecipe("deepslate_zinc_ore", "zinc_ingot");
        addModSmeltingRecipe("deepslate_osmium_ore", "osmium_ingot");
        addModSmeltingRecipe("deepslate_uranium_ore", "uranium_ingot");

        COOKING_MAP.put(Items.BEEF, Items.COOKED_BEEF);
        COOKING_MAP.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        COOKING_MAP.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        COOKING_MAP.put(Items.MUTTON, Items.COOKED_MUTTON);
        COOKING_MAP.put(Items.RABBIT, Items.COOKED_RABBIT);
        COOKING_MAP.put(Items.COD, Items.COOKED_COD);
        COOKING_MAP.put(Items.SALMON, Items.COOKED_SALMON);
        COOKING_MAP.put(Items.POTATO, Items.BAKED_POTATO);
        COOKING_MAP.put(Items.KELP, Items.DRIED_KELP);
    }

    private static void addModSmeltingRecipe(String rawName, String ingotName) {
        String[] modIds = {"create", "mekanism", "immersiveengineering", "thermal", "tconstruct", "mekanism"};
        for (String modId : modIds) {
            Item rawItem = getItem(modId, rawName);
            Item ingotItem = getItem(modId, ingotName);
            if (rawItem != Items.AIR && ingotItem != Items.AIR) {
                SMELTING_MAP.put(rawItem, ingotItem);
            }
        }
    }

    private static Item getItem(String modId, String name) {
        Item item = ForgeRegistries.ITEMS.getValue(new net.minecraft.resources.ResourceLocation(modId + ":" + name));
        return item != null ? item : Items.AIR;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }

        int tickCount = server.getTickCount();
        if (tickCount % 20 != 0) {
            return;
        }

        Set<UUID> processedTeams = new HashSet<>();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            UUID leaderUUID = ContractEvents.getLeaderUUID(player);
            if (leaderUUID == null) {
                leaderUUID = player.getUUID();
            }

            if (processedTeams.contains(leaderUUID)) {
                continue;
            }
            processedTeams.add(leaderUUID);

            processTeamInventory(player, leaderUUID);
        }
    }

    private static void processTeamInventory(ServerPlayer player, UUID leaderUUID) {
        TeamInventory inventory = TeamInventory.getByLeaderUUID(leaderUUID);
        if (inventory == null) {
            return;
        }

        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack.isEmpty()) {
                continue;
            }

            Item smeltedItem = SMELTING_MAP.get(stack.getItem());
            if (smeltedItem != null && smeltedItem != Items.AIR) {
                int count = stack.getCount();
                ItemStack result = new ItemStack(smeltedItem, count);
                inventory.setItem(i, result);
                continue;
            }

            Item cookedItem = COOKING_MAP.get(stack.getItem());
            if (cookedItem != null && cookedItem != Items.AIR) {
                int count = stack.getCount();
                ItemStack result = new ItemStack(cookedItem, count);
                inventory.setItem(i, result);
            }
        }
    }

    public static boolean isSmeltable(Item item) {
        return SMELTING_MAP.containsKey(item);
    }

    public static boolean isCookable(Item item) {
        return COOKING_MAP.containsKey(item);
    }

    public static Item getSmeltedResult(Item item) {
        return SMELTING_MAP.getOrDefault(item, Items.AIR);
    }

    public static Item getCookedResult(Item item) {
        return COOKING_MAP.getOrDefault(item, Items.AIR);
    }
}
