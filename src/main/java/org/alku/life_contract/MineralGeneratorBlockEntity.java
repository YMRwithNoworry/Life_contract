package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Clearable;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MineralGeneratorBlockEntity extends BlockEntity implements Clearable, MenuProvider {

    public enum MineralType implements StringRepresentable {
        IRON("iron", Items.IRON_INGOT),
        GOLD("gold", Items.GOLD_INGOT),
        DIAMOND("diamond", Items.DIAMOND),
        EMERALD("emerald", Items.EMERALD);

        private final String name;
        private final Item item;

        MineralType(String name, Item item) {
            this.name = name;
            this.item = item;
        }

        @Override
        public String getSerializedName() {
            return this.name;
        }

        public Item getItem() {
            return item;
        }
        
        public String getDisplayName() {
            return switch (this) {
                case IRON -> "铁锭";
                case GOLD -> "金锭";
                case DIAMOND -> "钻石";
                case EMERALD -> "绿宝石";
            };
        }
    }

    private MineralType mineralType = MineralType.IRON;
    private int interval = 60;
    private boolean enabled = false;
    private long lastTick = 0;

    private static final String TAG_MINERAL_TYPE = "mineral_type";
    private static final String TAG_INTERVAL = "interval";
    private static final String TAG_ENABLED = "enabled";
    private static final String TAG_LAST_TICK = "last_tick";

    public MineralGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(Life_contract.MINERAL_GENERATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MineralGeneratorBlockEntity entity) {
        if (!level.isClientSide) {
            boolean globalEnabled = MineralGenerationConfig.isGlobalGenerationEnabled();
            boolean effectiveEnabled = entity.enabled && globalEnabled;
            
            if (effectiveEnabled) {
                long currentTick = level.getGameTime();
                int intervalTicks = entity.interval * 20;

                if (currentTick - entity.lastTick >= intervalTicks) {
                    entity.lastTick = currentTick;
                    entity.setChanged();

                    ItemStack stack = new ItemStack(entity.mineralType.getItem(), 1);
                    net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                            level,
                            pos.getX() + 0.5,
                            pos.getY() + 1.0,
                            pos.getZ() + 0.5,
                            stack
                    );
                    level.addFreshEntity(itemEntity);
                }
            }
            
            NetworkHandler.sendSyncMineralGenerator(pos, entity.mineralType.getSerializedName(), entity.interval, entity.enabled, entity.lastTick, level.getGameTime());
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putString(TAG_MINERAL_TYPE, mineralType.name());
        tag.putInt(TAG_INTERVAL, interval);
        tag.putBoolean(TAG_ENABLED, enabled);
        tag.putLong(TAG_LAST_TICK, lastTick);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains(TAG_MINERAL_TYPE)) {
            try {
                mineralType = MineralType.valueOf(tag.getString(TAG_MINERAL_TYPE));
            } catch (IllegalArgumentException e) {
                mineralType = MineralType.IRON;
            }
        }
        if (tag.contains(TAG_INTERVAL)) {
            interval = tag.getInt(TAG_INTERVAL);
        }
        if (tag.contains(TAG_ENABLED)) {
            enabled = tag.getBoolean(TAG_ENABLED);
        }
        if (tag.contains(TAG_LAST_TICK)) {
            lastTick = tag.getLong(TAG_LAST_TICK);
        }
    }
    
    public MineralType getMineralType() {
        return mineralType;
    }

    public void setMineralType(MineralType type) {
        this.mineralType = type;
        if (level != null && !level.isClientSide) {
            BlockState currentState = level.getBlockState(worldPosition);
            if (currentState.hasProperty(MineralGeneratorBlock.MINERAL_TYPE)) {
                level.setBlock(worldPosition, currentState.setValue(MineralGeneratorBlock.MINERAL_TYPE, type), 3);
            }
        }
        setChanged();
    }

    public int getInterval() {
        return interval;
    }

    public void setInterval(int interval) {
        this.interval = interval;
        setChanged();
    }
    
    public void setLastTick(long tick) {
        this.lastTick = tick;
        setChanged();
    }
    
    public long getLastTick() {
        return lastTick;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (enabled && level != null) {
            lastTick = level.getGameTime();
        }
        setChanged();
    }

    @Override
    public void clearContent() {
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("矿物生成器");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MineralGeneratorMenu(id, inventory, worldPosition);
    }
}
