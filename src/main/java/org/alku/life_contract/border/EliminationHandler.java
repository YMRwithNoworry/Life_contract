package org.alku.life_contract.border;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.registries.ForgeRegistries;

import org.alku.life_contract.Life_contract;

import java.util.Random;

public class EliminationHandler {
    
    private static final String[] ELIMINATION_MOBS = {
        "spore:hindenburg",
        "spore:hohlfresser",
        "caerula_arbor:oceanized_witheria",
        "caerula_arbor:oceanized_wither",
        "caerula_arbor:oceanized_wardenis"
    };
    
    private static final Random RANDOM = new Random();
    
    public static void spawnEliminationMobAtBorderCenter(ServerLevel level) {
        String mobId = ELIMINATION_MOBS[RANDOM.nextInt(ELIMINATION_MOBS.length)];
        
        try {
            String[] parts = mobId.split(":");
            ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceId);
            
            if (entityType == null) {
                return;
            }
            
            WorldBorder worldBorder = level.getWorldBorder();
            double centerX = worldBorder.getCenterX();
            double centerZ = worldBorder.getCenterZ();
            BlockPos spawnPos = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                new BlockPos((int)centerX, 0, (int)centerZ)
            );
            
            Entity entity = entityType.create(level);
            if (entity == null) {
                return;
            }
            
            entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
                level.random.nextFloat() * 360F, 0);
            
            level.addFreshEntity(entity);
            
            level.getPlayers(p -> true).forEach(p -> 
                p.sendSystemMessage(Component.literal("§5[感染生物] §f一只可怕的生物已在边界中心生成！")));
            
        } catch (Exception e) {
        }
    }
    
    public static void spawnEliminationMobAtPlayer(ServerLevel level, ServerPlayer player) {
        String mobId = ELIMINATION_MOBS[RANDOM.nextInt(ELIMINATION_MOBS.length)];
        
        try {
            String[] parts = mobId.split(":");
            ResourceLocation resourceId = ResourceLocation.fromNamespaceAndPath(parts[0], parts[1]);
            EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(resourceId);
            
            if (entityType == null) {
                return;
            }
            
            BlockPos spawnPos = level.getHeightmapPos(
                net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                player.blockPosition()
            );
            
            Entity entity = entityType.create(level);
            if (entity == null) {
                return;
            }
            
            entity.moveTo(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5, 
                level.random.nextFloat() * 360F, 0);
            
            level.addFreshEntity(entity);
            
            level.getPlayers(p -> true).forEach(p -> 
                p.sendSystemMessage(Component.literal("§5[感染生物] §f一只可怕的生物已生成！")));
            
        } catch (Exception e) {
        }
    }
}
