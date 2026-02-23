package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;

public class TeamSentinel extends Mob {
    private static final EntityDataAccessor<Integer> DATA_TEAM_NUMBER = SynchedEntityData.defineId(TeamSentinel.class, EntityDataSerializers.INT);
    private int lastAttackedTick = 0;
    private static final int NO_ATTACK_TICKS_FOR_HEAL = 3 * 60 * 20;

    public TeamSentinel(EntityType<? extends TeamSentinel> type, Level level) {
        super(type, level);
        this.setNoGravity(false);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, 0.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 0.0)
                .add(Attributes.ATTACK_DAMAGE, 0.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TEAM_NUMBER, 0);
    }

    public void setTeamNumber(int number) {
        this.entityData.set(DATA_TEAM_NUMBER, number);
    }

    public int getTeamNumber() {
        return this.entityData.get(DATA_TEAM_NUMBER);
    }

    @Override
    public void aiStep() {
        super.aiStep();

        if (!this.level().isClientSide) {
            this.setDeltaMovement(0, this.getDeltaMovement().y, 0);

            int currentTick = this.tickCount;
            if (currentTick - this.lastAttackedTick > NO_ATTACK_TICKS_FOR_HEAL) {
                if (currentTick % 20 == 0 && this.getHealth() < this.getMaxHealth()) {
                    this.heal(0.25f);
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        this.lastAttackedTick = this.tickCount;
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void push(Entity entity) {
    }

    @Override
    protected void pushEntities() {
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            player.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "§6队伍守卫 §b#" + this.getTeamNumber() + " §f血量: §c" + 
                    Math.round(this.getHealth()) + "§7/§c" + Math.round(this.getMaxHealth())
                )
            );
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("TeamNumber", this.getTeamNumber());
        tag.putInt("LastAttackedTick", this.lastAttackedTick);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("TeamNumber")) {
            this.setTeamNumber(tag.getInt("TeamNumber"));
        }
        if (tag.contains("LastAttackedTick")) {
            this.lastAttackedTick = tag.getInt("LastAttackedTick");
        }
    }
}
