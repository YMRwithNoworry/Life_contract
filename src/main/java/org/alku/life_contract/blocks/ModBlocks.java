package org.alku.life_contract.blocks;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.alku.life_contract.Life_contract;

public class ModBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Life_contract.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Life_contract.MODID);
    
    public static final RegistryObject<Block> INFECTION_PURIFIER = BLOCKS.register("infection_purifier",
        () -> new InfectionPurifierBlock(BlockBehaviour.Properties.of()
            .mapColor(MapColor.METAL)
            .strength(3.0f, 6.0f)
            .sound(SoundType.METAL)
            .lightLevel(state -> 10)
            .requiresCorrectToolForDrops()
        ));
    
    public static final RegistryObject<Item> INFECTION_PURIFIER_ITEM = BLOCK_ITEMS.register("infection_purifier",
        () -> new BlockItem(INFECTION_PURIFIER.get(), new Item.Properties()
            .rarity(net.minecraft.world.item.Rarity.RARE)
        ));
}
