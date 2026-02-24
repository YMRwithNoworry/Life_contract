package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EndlessMinerSystem {

    private static final Random RANDOM = new Random();
    
    private static final Map<UUID, Long> DROP_COOLDOWN = new HashMap<>();
    private static final long DROP_COOLDOWN_TICKS = 5;

    private static final Map<DropEntry, Float> STONE_DROPS = new HashMap<>();
    
    static {
        STONE_DROPS.put(new DropEntry(Items.COAL, 1, 2), 0.03f);
        STONE_DROPS.put(new DropEntry(Items.IRON_NUGGET, 1, 3), 0.02f);
        STONE_DROPS.put(new DropEntry(Items.GOLD_NUGGET, 1, 2), 0.01f);
        STONE_DROPS.put(new DropEntry(Items.REDSTONE, 1, 2), 0.008f);
        STONE_DROPS.put(new DropEntry(Items.LAPIS_LAZULI, 1, 2), 0.006f);
        STONE_DROPS.put(new DropEntry(Items.DIAMOND, 1, 1), 0.002f);
        STONE_DROPS.put(new DropEntry(Items.EMERALD, 1, 1), 0.001f);
        STONE_DROPS.put(new DropEntry(Items.COPPER_INGOT, 1, 1), 0.015f);
        STONE_DROPS.put(new DropEntry(Items.QUARTZ, 1, 2), 0.005f);
    }

    private static class DropEntry {
        final net.minecraft.world.item.Item item;
        final int minCount;
        final int maxCount;
        
        DropEntry(net.minecraft.world.item.Item item, int minCount, int maxCount) {
            this.item = item;
            this.minCount = minCount;
            this.maxCount = maxCount;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }

        if (!(event.getPlayer() instanceof ServerPlayer player)) {
            return;
        }

        Profession profession = ProfessionConfig.getProfession(ProfessionConfig.getPlayerProfession(player.getUUID()));
        if (profession == null || !profession.hasEndlessMinerAbility()) {
            return;
        }

        BlockState state = event.getState();
        Block block = state.getBlock();
        BlockPos pos = event.getPos();
        ServerLevel level = (ServerLevel) event.getLevel();

        if (isOreBlock(block)) {
            applyFortuneEffect(player, state, pos, level, profession);
        }

        if (isStoneBlock(block)) {
            handleStoneDrop(player, pos, level, profession);
        }
    }

    private static boolean isOreBlock(Block block) {
        return block == Blocks.COAL_ORE || block == Blocks.DEEPSLATE_COAL_ORE ||
               block == Blocks.IRON_ORE || block == Blocks.DEEPSLATE_IRON_ORE ||
               block == Blocks.GOLD_ORE || block == Blocks.DEEPSLATE_GOLD_ORE ||
               block == Blocks.COPPER_ORE || block == Blocks.DEEPSLATE_COPPER_ORE ||
               block == Blocks.DIAMOND_ORE || block == Blocks.DEEPSLATE_DIAMOND_ORE ||
               block == Blocks.EMERALD_ORE || block == Blocks.DEEPSLATE_EMERALD_ORE ||
               block == Blocks.LAPIS_ORE || block == Blocks.DEEPSLATE_LAPIS_ORE ||
               block == Blocks.REDSTONE_ORE || block == Blocks.DEEPSLATE_REDSTONE_ORE ||
               block == Blocks.NETHER_GOLD_ORE || block == Blocks.NETHER_QUARTZ_ORE;
    }

    private static boolean isStoneBlock(Block block) {
        return block == Blocks.STONE || block == Blocks.COBBLESTONE ||
               block == Blocks.ANDESITE || block == Blocks.DIORITE || block == Blocks.GRANITE ||
               block == Blocks.DEEPSLATE || block == Blocks.COBBLED_DEEPSLATE ||
               block == Blocks.TUFF || block == Blocks.NETHERRACK;
    }

    private static void applyFortuneEffect(ServerPlayer player, BlockState state, BlockPos pos, 
                                           ServerLevel level, Profession profession) {
        int fortuneLevel = profession.getFortuneLevel();
        if (fortuneLevel <= 0) {
            return;
        }

        ItemStack mainHand = player.getMainHandItem();
        int existingFortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, mainHand);
        
        if (existingFortune < fortuneLevel) {
            player.getPersistentData().putInt("EndlessMinerFortuneBonus", fortuneLevel - existingFortune);
        }
    }

    private static void handleStoneDrop(ServerPlayer player, BlockPos pos, ServerLevel level, Profession profession) {
        long currentTime = level.getGameTime();
        Long lastDrop = DROP_COOLDOWN.get(player.getUUID());
        
        if (lastDrop != null && currentTime - lastDrop < DROP_COOLDOWN_TICKS) {
            return;
        }

        float dropChance = profession.getStoneDropChance();
        if (dropChance <= 0) {
            return;
        }

        if (RANDOM.nextFloat() < dropChance) {
            DropEntry drop = selectRandomDrop();
            if (drop != null) {
                int count = drop.minCount + RANDOM.nextInt(drop.maxCount - drop.minCount + 1);
                ItemStack dropStack = new ItemStack(drop.item, count);
                
                net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level, 
                    pos.getX() + 0.5, 
                    pos.getY() + 0.5, 
                    pos.getZ() + 0.5, 
                    dropStack
                );
                level.addFreshEntity(itemEntity);
                
                DROP_COOLDOWN.put(player.getUUID(), currentTime);
                
                spawnMiningParticles(level, pos);
            }
        }
    }

    private static DropEntry selectRandomDrop() {
        float totalWeight = 0;
        for (Float weight : STONE_DROPS.values()) {
            totalWeight += weight;
        }
        
        float random = RANDOM.nextFloat() * totalWeight;
        float currentWeight = 0;
        
        for (Map.Entry<DropEntry, Float> entry : STONE_DROPS.entrySet()) {
            currentWeight += entry.getValue();
            if (random < currentWeight) {
                return entry.getKey();
            }
        }
        
        return null;
    }

    private static void spawnMiningParticles(ServerLevel level, BlockPos pos) {
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.ENCHANT,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            20, 0.5, 0.5, 0.5, 0.3
        );
        
        level.sendParticles(
            net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER,
            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
            10, 0.3, 0.3, 0.3, 0.1
        );
    }

    public static int getFortuneBonus(ServerPlayer player) {
        return player.getPersistentData().getInt("EndlessMinerFortuneBonus");
    }

    public static void clearFortuneBonus(ServerPlayer player) {
        player.getPersistentData().remove("EndlessMinerFortuneBonus");
    }

    public static void clearCooldown(UUID playerUUID) {
        DROP_COOLDOWN.remove(playerUUID);
    }
}
