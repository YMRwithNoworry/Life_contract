package org.alku.life_contract.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.alku.life_contract.PlayerInfectionSystem;

import javax.annotation.Nullable;
import java.util.List;

public class InfectionPurifierBlock extends Block {
    
    public InfectionPurifierBlock(Properties properties) {
        super(properties);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        int currentInfection = PlayerInfectionSystem.getInfection(player);
        if (currentInfection > 0) {
            int purifiedAmount = Math.min(currentInfection, 20);
            PlayerInfectionSystem.setInfection(player, currentInfection - purifiedAmount);
            
            player.sendSystemMessage(Component.literal("§a[感染净化器] §f已净化 §e" + purifiedAmount + " §f点感染值！"));
            
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                    15, 0.5, 0.5, 0.5, 0.1);
            }
        } else {
            player.sendSystemMessage(Component.literal("§a[感染净化器] §f你已经很健康了！"));
        }
        
        return InteractionResult.CONSUME;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§a感染净化器"));
        tooltip.add(Component.literal("§7右键使用可净化20点感染值"));
        tooltip.add(Component.literal("§7放置在基地中使用"));
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
        return true;
    }
    
    @Override
    public int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
