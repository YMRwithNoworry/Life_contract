package org.alku.airdrop.entity;

import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.alku.airdrop.Airdrop;

public class AirdropEntity extends Entity implements MenuProvider, Container {
    private static final EntityDataAccessor<Boolean> CLAIMED = SynchedEntityData.defineId(AirdropEntity.class,
            EntityDataSerializers.BOOLEAN);
    private final NonNullList<ItemStack> inventory = NonNullList.withSize(27, ItemStack.EMPTY);
    private boolean landed = false;

    public AirdropEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    public AirdropEntity(Level level, double x, double y, double z) {
        this(Airdrop.AIRDROP_ENTITY.get(), level);
        this.setPos(x, y, z);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        if (!landed && !this.isNoGravity()) {
            // 模拟降落伞效果：固定匀速下落
            // 从 Y=300 降落到 Y=64 约为 236 格
            // 236 格 / 45 秒 / 20 tick/s = 0.2622 格/tick
            // 精确控制落地时间为 45 秒
            this.setDeltaMovement(0.0D, -0.2622D, 0.0D);
        }

        this.move(MoverType.SELF, this.getDeltaMovement());

        // 阻力（仅保留水平阻力，垂直方向由上方逻辑控制）
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(motion.x * 0.98D, motion.y, motion.z * 0.98D);

        if (this.onGround() && !landed) {
            landed = true;
            this.setDeltaMovement(Vec3.ZERO);

            if (!this.level().isClientSide) {
                // 构建坐标信息
                String posText = (int) this.getX() + ", " + (int) this.getY() + ", " + (int) this.getZ();

                // 1. 聊天栏消息
                Component chatMsg = Component.literal("§c§l[注意] §f空投已着陆! 坐标: §e" + posText);

                // 2. Title (屏幕中央大字)
                Component titleMsg = Component.literal("§4空投着陆!");
                Component subTitleMsg = Component.literal("§e坐标: " + posText);

                // 向全服玩家发送
                this.level().players().forEach(p -> {
                    p.sendSystemMessage(chatMsg);

                    if (p instanceof ServerPlayer sp) {
                        // 发送 Title
                        sp.connection.send(new ClientboundSetTitlesAnimationPacket(10, 70, 20));
                        sp.connection.send(new ClientboundSetTitleTextPacket(titleMsg));
                        sp.connection.send(new ClientboundSetSubtitleTextPacket(subTitleMsg));

                        // 播放声音 (UI声音，确保能听到)
                        sp.playNotifySound(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.MASTER, 1.0f, 1.0f);
                    }
                });
            }
        }

        if (this.level().isClientSide) {
            if (!landed) {
                // 掉落时的粒子轨迹
                if (this.tickCount % 2 == 0) {
                    this.level().addParticle(ParticleTypes.SMALL_FLAME, this.getX(), this.getY() + 0.5, this.getZ(),
                            0.0, 0.05, 0.0);
                    this.level().addParticle(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.1,
                            0.0);
                }
            } else {
                // 着陆后的强烈信号效果
                // 1. 光柱标记（从地面到天空）
                if (this.tickCount % 2 == 0) {
                    // 使用多种发光粒子组合形成光柱效果
                    for (int y = 0; y < 256; y += 2) {
                        double offsetX = (this.random.nextDouble() - 0.5) * 0.2;
                        double offsetZ = (this.random.nextDouble() - 0.5) * 0.2;
                        this.level().addParticle(ParticleTypes.SOUL_FIRE_FLAME,
                                this.getX() + offsetX, y, this.getZ() + offsetZ, 0.0, 0.05, 0.0);
                    }
                    // 中心光柱用更亮的粒子
                    for (int y = 0; y < 256; y += 4) {
                        this.level().addParticle(ParticleTypes.GLOW,
                                this.getX(), y, this.getZ(), 0.0, 0.0, 0.0);
                    }
                }
                // 2. 高耸浓烟
                if (this.tickCount % 2 == 0) {
                    this.level().addParticle(ParticleTypes.CAMPFIRE_SIGNAL_SMOKE,
                            this.getX(), this.getY() + 0.5, this.getZ(), 0.0, 0.4, 0.0);
                }
                // 3. 底部的火焰和烟雾提示
                if (this.tickCount % 5 == 0) {
                    for (int i = 0; i < 3; i++) {
                        double ox = (this.random.nextDouble() - 0.5) * 0.5;
                        double oz = (this.random.nextDouble() - 0.5) * 0.5;
                        this.level().addParticle(ParticleTypes.FLAME, this.getX() + ox, this.getY() + 0.1,
                                this.getZ() + oz, 0.0, 0.1, 0.0);
                    }
                    this.level().addParticle(ParticleTypes.LARGE_SMOKE, this.getX(), this.getY() + 0.2, this.getZ(),
                            0.0, 0.2, 0.0);
                }
            }
        }
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (!this.level().isClientSide && hand == InteractionHand.MAIN_HAND) {
            // 如果玩家是生存模式且点击了空投，标记为已被领取 (不再显示坐标)
            if (!player.getAbilities().instabuild && !this.isClaimed()) {
                this.setClaimed(true);
            }
            player.openMenu(this);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    public boolean isClaimed() {
        return this.entityData.get(CLAIMED);
    }

    public void setClaimed(boolean claimed) {
        this.entityData.set(CLAIMED, claimed);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(CLAIMED, false);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.landed = tag.getBoolean("Landed");
        if (tag.contains("Claimed")) {
            this.setClaimed(tag.getBoolean("Claimed"));
        }
        ContainerHelper.loadAllItems(tag, this.inventory);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putBoolean("Landed", landed);
        tag.putBoolean("Claimed", this.isClaimed());
        ContainerHelper.saveAllItems(tag, this.inventory);
    }

    @Override
    public int getContainerSize() {
        return 27;
    }

    @Override
    public boolean isEmpty() {
        return inventory.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return inventory.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return ContainerHelper.removeItem(inventory, slot, amount);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(inventory, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        inventory.set(slot, stack);
    }

    @Override
    public boolean stillValid(Player player) {
        return this.isAlive() && player.distanceToSqr(this) < 64.0D;
    }

    @Override
    public void clearContent() {
        inventory.clear();
    }

    @Override
    public void setChanged() {
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInv, Player player) {
        return ChestMenu.threeRows(id, playerInv, this);
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("entity.airdrop.airdrop");
    }
}