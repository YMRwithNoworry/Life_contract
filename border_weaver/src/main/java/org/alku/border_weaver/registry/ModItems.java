package org.alku.border_weaver.registry;

import org.alku.border_weaver.Border_weaver;
import org.alku.border_weaver.item.BorderWeaverItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Border_weaver.MODID);

    public static final RegistryObject<Item> BORDER_TOOL = ITEMS.register("border_tool",
            () -> new BorderWeaverItem(new Item.Properties().stacksTo(1)));
}