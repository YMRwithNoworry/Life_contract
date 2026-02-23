package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.UUID;

public class FireTrailEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(FireTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(FireTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_DURATION = SynchedEntityData.defineId(FireTrailEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_TICK_COUNT = SynchedEntityData.defineId(FireTrailEntity.class, EntityDataSerializers.INT);
    
    private UUID ownerUUID;
    private int damageInterval = 20;
    private int lastDamageTick = 0;
    private BlockPos fireBlockPos = null;

    public FireTrailEntity(EntityType<FireTrailEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    public FireTrailEntity(Level level, double x, double y, double z, float radius, float damage, int duration, UUID ownerUUID) {
        this(Life_contract.FIRE_TRAIL.get(), level);
        this.setPos(x, y, z);
        this.entityData.set(DATA_RADIUS, radius);
        this.entityData.set(DATA_DAMAGE, damage);
        this.entityData.set(DATA_DURATION, duration);
        this.entityData.set(DATA_TICK_COUNT, 0);
        this.ownerUUID = ownerUUID;
        placeFireBlock();
    }

    private void placeFireBlock() {
        BlockPos pos = BlockPos.containing(this.getX(), this.getY(), this.getZ());
        BlockPos groundPos = findGroundPosition(pos);
        
        if (groundPos != null && canPlaceFire(groundPos)) {
            BlockState fireState = Blocks.FIRE.defaultBlockState();
            if (this.level().getBlockState(groundPos).isAir() || this.level().getBlockState(groundPos).canBeReplaced()) {
                this.level().setBlock(groundPos, fireState, 3);
                this.fireBlockPos = groundPos;
            }
        }
    }

    private BlockPos findGroundPosition(BlockPos start) {
        BlockPos pos = start;
        for (int i = 0; i < 5; i++) {
            if (!this.level().getBlockState(pos.below()).isAir() && 
                this.level().getBlockState(pos).isAir()) {
                return pos;
            }
            pos = pos.below();
        }
        for (int i = 0; i < 5; i++) {
            if (!this.level().getBlockState(pos.below()).isAir()) {
                return pos;
            }
            pos = pos.below();
        }
        return start;
    }

    private boolean canPlaceFire(BlockPos pos) {
        if (this.level().getBlockState(pos).getBlock() == Blocks.FIRE) {
            return false;
        }
        return this.level().getBlockState(pos).isAir() || 
               this.level().getBlockState(pos).canBeReplaced();
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_RADIUS, 1.0f);
        this.entityData.define(DATA_DAMAGE, 1.0f);
        this.entityData.define(DATA_DURATION, 100);
        this.entityData.define(DATA_TICK_COUNT, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.entityData.set(DATA_RADIUS, tag.getFloat("Radius"));
        this.entityData.set(DATA_DAMAGE, tag.getFloat("Damage"));
        this.entityData.set(DATA_DURATION, tag.getInt("Duration"));
        this.entityData.set(DATA_TICK_COUNT, tag.getInt("TickCount"));
        if (tag.hasUUID("OwnerUUID")) {
            this.ownerUUID = tag.getUUID("OwnerUUID");
        }
        if (tag.contains("DamageInterval")) {
            this.damageInterval = tag.getInt("DamageInterval");
        }
        if (tag.contains("FireBlockX")) {
            this.fireBlockPos = new BlockPos(
                tag.getInt("FireBlockX"),
                tag.getInt("FireBlockY"),
                tag.getInt("FireBlockZ")
            );
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Radius", this.entityData.get(DATA_RADIUS));
        tag.putFloat("Damage", this.entityData.get(DATA_DAMAGE));
        tag.putInt("Duration", this.entityData.get(DATA_DURATION));
        tag.putInt("TickCount", this.entityData.get(DATA_TICK_COUNT));
        if (this.ownerUUID != null) {
            tag.putUUID("OwnerUUID", this.ownerUUID);
        }
        tag.putInt("DamageInterval", this.damageInterval);
        if (this.fireBlockPos != null) {
            tag.putInt("FireBlockX", this.fireBlockPos.getX());
            tag.putInt("FireBlockY", this.fireBlockPos.getY());
            tag.putInt("FireBlockZ", this.fireBlockPos.getZ());
        }
    }

    @Override
    public void tick() {
        super.tick();
        
        if (this.level().isClientSide) {
            spawnClientParticles();
            return;
        }

        int currentTick = this.entityData.get(DATA_TICK_COUNT);
        this.entityData.set(DATA_TICK_COUNT, currentTick + 1);
        
        int duration = this.entityData.get(DATA_DURATION);
        if (currentTick >= duration) {
            removeFireBlock();
            this.discard();
            return;
        }

        if (currentTick % 5 == 0) {
            spawnServerParticles();
        }

        if (currentTick - lastDamageTick >= damageInterval) {
            applyDamageToEntities();
            lastDamageTick = currentTick;
        }
    }

    private void removeFireBlock() {
        if (fireBlockPos != null && this.level().getBlockState(fireBlockPos).getBlock() == Blocks.FIRE) {
            this.level().setBlock(fireBlockPos, Blocks.AIR.defaultBlockState(), 3);
        }
    }

    private void spawnClientParticles() {
        float radius = this.entityData.get(DATA_RADIUS);
        for (int i = 0; i < 3; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2;
            double r = this.random.nextDouble() * radius;
            double px = this.getX() + Math.cos(angle) * r;
            double pz = this.getZ() + Math.sin(angle) * r;
            double py = this.getY() + this.random.nextDouble() * 0.5;
            
            this.level().addParticle(
                ParticleTypes.FLAME,
                px, py, pz,
                0.0, 0.05, 0.0
            );
            
            if (this.random.nextFloat() < 0.3f) {
                this.level().addParticle(
                    ParticleTypes.LAVA,
                    px, py, pz,
                    0.0, 0.0, 0.0
                );
            }
        }
    }

    private void spawnServerParticles() {
        if (this.level() instanceof ServerLevel serverLevel) {
            float radius = this.entityData.get(DATA_RADIUS);
            serverLevel.sendParticles(
                ParticleTypes.FLAME,
                this.getX(), this.getY() + 0.1, this.getZ(),
                10, radius, 0.1, radius, 0.02
            );
            serverLevel.sendParticles(
                ParticleTypes.LAVA,
                this.getX(), this.getY() + 0.1, this.getZ(),
                2, radius, 0.1, radius, 0.0
            );
        }
    }

    private void applyDamageToEntities() {
        float radius = this.entityData.get(DATA_RADIUS);
        float damage = this.entityData.get(DATA_DAMAGE);
        
        AABB box = new AABB(
            this.getX() - radius, this.getY() - 0.5, this.getZ() - radius,
            this.getX() + radius, this.getY() + 1.5, this.getZ() + radius
        );
        
        List<LivingEntity> entities = this.level().getEntitiesOfClass(LivingEntity.class, box, entity -> {
            if (entity.getUUID().equals(this.ownerUUID)) {
                return false;
            }
            if (entity instanceof Player player) {
                if (ownerUUID != null) {
                    Player owner = this.level().getPlayerByUUID(ownerUUID);
                    if (owner != null && ContractEvents.isSameTeam(player, owner)) {
                        return false;
                    }
                }
            }
            return entity.isAlive();
        });
        
        for (LivingEntity entity : entities) {
            entity.hurt(this.level().damageSources().inFire(), damage);
            entity.setSecondsOnFire(2);
        }
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public int getDuration() {
        return this.entityData.get(DATA_DURATION);
    }

    public int getRemainingDuration() {
        return Math.max(0, this.entityData.get(DATA_DURATION) - this.entityData.get(DATA_TICK_COUNT));
    }

    public void setDamageInterval(int interval) {
        this.damageInterval = interval;
    }

    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public void setDuration(int duration) {
        this.entityData.set(DATA_DURATION, duration);
    }

    public void setOwnerUUID(UUID uuid) {
        this.ownerUUID = uuid;
    }

    @Override
    public net.minecraft.world.phys.Vec3 getDeltaMovement() {
        return net.minecraft.world.phys.Vec3.ZERO;
    }

    @Override
    public void setDeltaMovement(net.minecraft.world.phys.Vec3 motion) {
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isInvulnerableTo(net.minecraft.world.damagesource.DamageSource source) {
        return true;
    }
}
