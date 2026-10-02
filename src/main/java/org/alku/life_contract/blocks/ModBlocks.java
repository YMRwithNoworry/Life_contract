package org.alku.life_contract.blocks;

import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.alku.life_contract.Life_contract;

public class ModBlocks {
    
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(BuiltInRegistries.BLOCK, Life_contract.MODID);
    public static final DeferredRegister<Item> BLOCK_ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, Life_contract.MODID);
    
}
