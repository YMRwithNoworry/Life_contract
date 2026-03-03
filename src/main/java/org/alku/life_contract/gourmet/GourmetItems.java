package org.alku.life_contract.gourmet;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.alku.life_contract.Life_contract;

public class GourmetItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Life_contract.MODID);

    public static final RegistryObject<Item> CHEF_SPATULA = ITEMS.register("chef_spatula",
            () -> new ChefSpatulaItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> PORTABLE_POT = ITEMS.register("portable_pot",
            () -> new PortablePotItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SEASONING_BOX = ITEMS.register("seasoning_box",
            () -> new SeasoningBoxItem(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> ENERGY_BREAKFAST = ITEMS.register("energy_breakfast",
            EnergyBreakfastItem::new);

    public static final RegistryObject<Item> NOURISHING_SOUP = ITEMS.register("nourishing_soup",
            NourishingSoupItem::new);

    public static final RegistryObject<Item> SPICY_HOT_POT = ITEMS.register("spicy_hot_pot",
            SpicyHotPotItem::new);

    public static final RegistryObject<Item> SWEET_DESSERT = ITEMS.register("sweet_dessert",
            SweetDessertItem::new);

    public static final RegistryObject<Item> NETHER_CUISINE = ITEMS.register("nether_cuisine",
            NetherCuisineItem::new);

    public static final RegistryObject<Item> LEGENDARY_FEAST = ITEMS.register("legendary_feast",
            LegendaryFeastItem::new);

    public static final RegistryObject<Item> EMERGENCY_DISH = ITEMS.register("emergency_dish",
            EmergencyDishItem::new);
}
