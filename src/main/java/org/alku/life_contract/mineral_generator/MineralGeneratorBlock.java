package org.alku.life_contract.mineral_generator;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import org.alku.life_contract.mineral_generator.MineralGeneratorBlockEntity.MineralType;

public class MineralGeneratorBlock extends Block implements EntityBlock {
    public static final EnumProperty<MineralType> MINERAL_TYPE = EnumProperty.create("mineral_type", MineralType.class);

    public MineralGeneratorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(MINERAL_TYPE, MineralType.IRON));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MINERAL_TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MineralGeneratorBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MineralGeneratorBlockEntity) {
                blockEntity.setRemoved();
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
