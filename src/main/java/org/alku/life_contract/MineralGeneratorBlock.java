package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MineralGeneratorBlock extends BaseEntityBlock {

    public static final EnumProperty<MineralGeneratorBlockEntity.MineralType> MINERAL_TYPE = 
        EnumProperty.create("mineral_type", MineralGeneratorBlockEntity.MineralType.class);

    public MineralGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(MINERAL_TYPE, MineralGeneratorBlockEntity.MineralType.IRON));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block, BlockState> builder) {
        builder.add(MINERAL_TYPE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MineralGeneratorBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        return createTickerHelper(blockEntityType, Life_contract.MINERAL_GENERATOR_BLOCK_ENTITY.get(),
                MineralGeneratorBlockEntity::tick);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResult.PASS;
        }
        
        if (!level.isClientSide) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MineralGeneratorBlockEntity generator) {
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    player.openMenu(generator);
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
