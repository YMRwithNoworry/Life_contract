package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeastRiderMountSystem {

    private static final String TAG_MOUNT_UUID = "BeastRiderMountUUID";
    private static final String TAG_RIDER_UUID = "BeastRiderRiderUUID";
    private static final String TAG_MOUNT_BEHAVIOR = "BeastRiderBehavior";
    private static final String TAG_MOUNT_OFFSET_Y = "BeastRiderOffsetY";
    
    private static final Map<UUID, UUID> RIDER_MOUNT_MAP = new HashMap<>();
    private static final Map<UUID, UUID> MOUNT_RIDER_MAP = new HashMap<>();
    private static final Map<UUID, MountBehavior> MOUNT_BEHAVIOR_MAP = new HashMap<>();
    private static final Map<UUID, Long> MOUNT_COOLDOWN_MAP = new HashMap<>();
    
    private static final UUID MOUNT_SPEED_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID MOUNT_DAMAGE_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    
    private static final float MIN_MOUNT_HEIGHT = 1.0f;
    private static final float MAX_MOUNT_HEIGHT = 10.0f;
    private static final int MOUNT_COOLDOWN_TICKS = 20;
    private static final int CONTROL_RANGE_DEFAULT = 32;

    public enum MountBehavior {
        FOLLOW("follow"),
        STAY("stay"),
        DEFEND("defend"),
        ATTACK("attack");
        
        private final String name;
        
        MountBehavior(String name) {
            this.name = name;
        }
        
        public String getName() {
            return name;
        }
        
        public static MountBehavior fromString(String name) {
            for (MountBehavior behavior : values()) {
                if (behavior.name.equalsIgnoreCase(name)) {
                    return behavior;
                }
            }
            return FOLLOW;
        }
    }

    public static boolean isBeastRider(Player player) {
        String professionId = getProfessionId(player);
        if (professionId == null || professionId.isEmpty()) {
            return false;
        }
        Profession profession = ProfessionConfig.getProfession(professionId);
        return profession != null && profession.canMountCreatures();
    }

    private static String getProfessionId(Player player) {
        if (player.level().isClientSide) {
            ClientDataStorage.PlayerData data = ClientDataStorage.get(player.getUUID());
            if (data != null && !data.profession.isEmpty()) {
                return data.profession;
            }
        }
        return player.getPersistentData().getString("LifeContractProfession");
    }

    public static Profession getBeastRiderProfession(Player player) {
        if (!isBeastRider(player)) return null;
        String professionId = getProfessionId(player);
        return ProfessionConfig.getProfession(professionId);
    }

    public static boolean canMount(Player player, Mob target) {
        if (!isBeastRider(player)) {
            return false;
        }
        
        if (isMounted(player)) {
            return false;
        }
        
        if (MOUNT_RIDER_MAP.containsKey(target.getUUID())) {
            return false;
        }
        
        UUID ownerUUID = FollowerEvents.getOwnerUUID(target);
        if (ownerUUID == null || !ownerUUID.equals(player.getUUID())) {
            return false;
        }
        
        float height = target.getBbHeight();
        if (height < MIN_MOUNT_HEIGHT || height > MAX_MOUNT_HEIGHT) {
            return false;
        }
        
        if (!target.isAlive()) {
            return false;
        }
        
        Long lastMount = MOUNT_COOLDOWN_MAP.get(player.getUUID());
        if (lastMount != null && player.level().getGameTime() - lastMount < MOUNT_COOLDOWN_TICKS) {
            return false;
        }
        
        return true;
    }

    public static boolean mountCreature(Player player, Mob target) {
        if (!canMount(player, target)) {
            return false;
        }
        
        UUID playerUUID = player.getUUID();
        UUID targetUUID = target.getUUID();
        
        RIDER_MOUNT_MAP.put(playerUUID, targetUUID);
        MOUNT_RIDER_MAP.put(targetUUID, playerUUID);
        MOUNT_BEHAVIOR_MAP.put(targetUUID, MountBehavior.FOLLOW);
        
        player.getPersistentData().putUUID(TAG_MOUNT_UUID, targetUUID);
        target.getPersistentData().putUUID(TAG_RIDER_UUID, playerUUID);
        
        Profession profession = getBeastRiderProfession(player);
        if (profession != null) {
            applyMountBonuses(player, target, profession);
        }
        
        MOUNT_COOLDOWN_MAP.put(playerUUID, player.level().getGameTime());
        
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            syncMountToClients(serverPlayer, targetUUID, true);
            serverPlayer.displayClientMessage(
                Component.literal("§a[驯兽师] §r已骑乘 " + target.getName().getString()),
                true
            );
        }
        
        return true;
    }

    public static void dismountCreature(Player player) {
        UUID mountUUID = RIDER_MOUNT_MAP.remove(player.getUUID());
        if (mountUUID == null) return;
        
        MOUNT_RIDER_MAP.remove(mountUUID);
        MOUNT_BEHAVIOR_MAP.remove(mountUUID);
        
        player.getPersistentData().remove(TAG_MOUNT_UUID);
        
        Entity mount = player.level() instanceof ServerLevel sl ? sl.getEntity(mountUUID) : null;
        if (mount instanceof Mob mob) {
            mob.getPersistentData().remove(TAG_RIDER_UUID);
            removeMountBonuses(mob);
            
            Vec3 playerPos = player.position();
            Vec3 dismountPos = playerPos.add(1.5, 0, 0);
            player.moveTo(dismountPos.x, dismountPos.y, dismountPos.z, player.getYRot(), player.getXRot());
        }
        
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            syncMountToClients(serverPlayer, mountUUID, false);
            serverPlayer.displayClientMessage(
                Component.literal("§e[驯兽师] §r已解除骑乘"),
                true
            );
        }
    }

    public static boolean isMounted(Player player) {
        return RIDER_MOUNT_MAP.containsKey(player.getUUID());
    }

    public static UUID getMountUUID(Player player) {
        return RIDER_MOUNT_MAP.get(player.getUUID());
    }

    public static Mob getMountEntity(Player player) {
        UUID mountUUID = getMountUUID(player);
        if (mountUUID == null) return null;
        Entity entity = player.level() instanceof ServerLevel sl ? sl.getEntity(mountUUID) : null;
        return entity instanceof Mob mob ? mob : null;
    }

    public static MountBehavior getMountBehavior(UUID mountUUID) {
        return MOUNT_BEHAVIOR_MAP.getOrDefault(mountUUID, MountBehavior.FOLLOW);
    }

    public static void setMountBehavior(UUID mountUUID, MountBehavior behavior) {
        MOUNT_BEHAVIOR_MAP.put(mountUUID, behavior);
    }

    public static void setMountBehavior(Player player, MountBehavior behavior) {
        UUID mountUUID = getMountUUID(player);
        if (mountUUID != null) {
            setMountBehavior(mountUUID, behavior);
            
            if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(
                    Component.literal("§b[驯兽师] §r行为模式: " + behavior.getName()),
                    true
                );
            }
        }
    }

    private static void applyMountBonuses(Player player, Mob mount, Profession profession) {
        float speedBonus = profession.getMountSpeedBonus();
        float damageBonus = profession.getMountDamageBonus();
        float healthBonus = profession.getMountHealthBonus();
        
        if (speedBonus > 0) {
            var speedAttr = mount.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    MOUNT_SPEED_MODIFIER_UUID,
                    "BeastRiderSpeedBonus",
                    speedBonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                speedAttr.addPermanentModifier(modifier);
            }
        }
        
        if (damageBonus > 0) {
            var damageAttr = mount.getAttribute(Attributes.ATTACK_DAMAGE);
            if (damageAttr != null) {
                AttributeModifier modifier = new AttributeModifier(
                    MOUNT_DAMAGE_MODIFIER_UUID,
                    "BeastRiderDamageBonus",
                    damageBonus,
                    AttributeModifier.Operation.MULTIPLY_TOTAL
                );
                damageAttr.addPermanentModifier(modifier);
            }
        }
        
        if (healthBonus > 0) {
            var healthAttr = mount.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null) {
                float newMaxHealth = mount.getMaxHealth() + healthBonus;
                mount.setHealth(mount.getHealth() + healthBonus);
            }
        }
    }

    private static void removeMountBonuses(Mob mount) {
        var speedAttr = mount.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(MOUNT_SPEED_MODIFIER_UUID);
        }
        
        var damageAttr = mount.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) {
            damageAttr.removeModifier(MOUNT_DAMAGE_MODIFIER_UUID);
        }
    }

    public static void controlMovement(Player player, Vec3 movementInput) {
        Mob mount = getMountEntity(player);
        if (mount == null || !mount.isAlive()) {
            dismountCreature(player);
            return;
        }
        
        Profession profession = getBeastRiderProfession(player);
        double speed = 0.3D;
        if (profession != null && profession.getMountSpeedBonus() > 0) {
            speed *= (1.0 + profession.getMountSpeedBonus());
        }
        
        double dx = movementInput.x * speed;
        double dy = movementInput.y * speed * 0.5;
        double dz = movementInput.z * speed;
        
        float yaw = player.getYRot();
        double sin = Math.sin(Math.toRadians(yaw));
        double cos = Math.cos(Math.toRadians(yaw));
        
        double moveX = dx * cos - dz * sin;
        double moveZ = dx * sin + dz * cos;
        
        mount.setDeltaMovement(moveX, mount.getDeltaMovement().y + dy, moveZ);
        
        positionRiderOnMount(player, mount);
    }

    public static void positionRiderOnMount(Player player, Mob mount) {
        float mountHeight = mount.getBbHeight();
        float mountEyeHeight = mount.getEyeHeight();
        
        double offsetY = mountHeight + 0.3;
        
        Vec3 mountPos = mount.position();
        float yaw = mount.getYRot();
        
        double offsetX = Math.sin(Math.toRadians(yaw)) * -0.2;
        double offsetZ = Math.cos(Math.toRadians(yaw)) * -0.2;
        
        player.moveTo(
            mountPos.x + offsetX,
            mountPos.y + offsetY,
            mountPos.z + offsetZ,
            yaw,
            player.getXRot()
        );
    }

    public static void commandAttack(Player player, LivingEntity target) {
        Mob mount = getMountEntity(player);
        if (mount == null || !mount.isAlive()) {
            dismountCreature(player);
            return;
        }
        
        if (target == null || !target.isAlive()) return;
        
        if (target instanceof Player targetPlayer && ContractEvents.isSameTeam(player, targetPlayer)) {
            return;
        }
        
        mount.setTarget(target);
        setMountBehavior(player, MountBehavior.ATTACK);
        
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ANGRY_VILLAGER,
                mount.getX(), mount.getY() + mount.getBbHeight() + 0.5, mount.getZ(),
                5, 0.3, 0.2, 0.3, 0.0
            );
        }
        
        if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[驯兽师] §r命令攻击: " + target.getName().getString()),
                true
            );
        }
    }

    public static void commandSpecialAbility(Player player) {
        Mob mount = getMountEntity(player);
        if (mount == null || !mount.isAlive()) {
            dismountCreature(player);
            return;
        }
        
        if (!player.level().isClientSide && player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                mount.getX(), mount.getY() + mount.getBbHeight() / 2, mount.getZ(),
                1, 0, 0, 0, 0
            );
            
            float damage = 5.0f;
            Profession profession = getBeastRiderProfession(player);
            if (profession != null && profession.getMountDamageBonus() > 0) {
                damage *= (1.0f + profession.getMountDamageBonus());
            }
            
            for (Entity entity : mount.level().getEntities(mount, mount.getBoundingBox().inflate(3))) {
                if (entity instanceof LivingEntity living && entity != player && entity != mount) {
                    if (entity instanceof Player targetPlayer && ContractEvents.isSameTeam(player, targetPlayer)) {
                        continue;
                    }
                    living.hurt(mount.level().damageSources().mobAttack(mount), damage);
                }
            }
        }
    }

    private static void syncMountToClients(ServerPlayer player, UUID mountUUID, boolean isMounting) {
        PacketSyncMount packet = new PacketSyncMount(player.getUUID(), mountUUID, isMounting);
        NetworkHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.TRACKING_ENTITY.with(() -> player), packet);
        NetworkHandler.CHANNEL.send(net.minecraftforge.network.PacketDistributor.PLAYER.with(() -> player), packet);
    }

    public static int getControlRange(Player player) {
        Profession profession = getBeastRiderProfession(player);
        if (profession != null) {
            return profession.getMountControlRange();
        }
        return CONTROL_RANGE_DEFAULT;
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            if (!isMounted(player)) continue;
            
            Mob mount = getMountEntity(player);
            if (mount == null || !mount.isAlive()) {
                dismountCreature(player);
                continue;
            }
            
            int controlRange = getControlRange(player);
            if (player.distanceToSqr(mount) > controlRange * controlRange) {
                dismountCreature(player);
                player.displayClientMessage(
                    Component.literal("§c[驯兽师] §r超出控制范围，已解除骑乘"),
                    true
                );
                continue;
            }
            
            Entity mountEntity = player.level() instanceof ServerLevel sl ? sl.getEntity(mount.getUUID()) : null;
            if (mountEntity == null || !mountEntity.equals(mount)) {
                dismountCreature(player);
                continue;
            }
            
            MountBehavior behavior = getMountBehavior(mount.getUUID());
            executeBehavior(player, mount, behavior);
            
            positionRiderOnMount(player, mount);
        }
    }

    private static void executeBehavior(Player player, Mob mount, MountBehavior behavior) {
        switch (behavior) {
            case FOLLOW -> {
                if (mount.getTarget() == null) {
                    mount.getNavigation().moveTo(player, 1.0D);
                }
            }
            case STAY -> {
                mount.getNavigation().stop();
                mount.setTarget(null);
            }
            case DEFEND -> {
                if (mount.getTarget() == null || !mount.getTarget().isAlive()) {
                    LivingEntity nearestEnemy = findNearestEnemy(player, mount, 16);
                    if (nearestEnemy != null) {
                        mount.setTarget(nearestEnemy);
                    }
                }
            }
            case ATTACK -> {
                if (mount.getTarget() == null || !mount.getTarget().isAlive()) {
                    LivingEntity nearestEnemy = findNearestEnemy(player, mount, 32);
                    if (nearestEnemy != null) {
                        mount.setTarget(nearestEnemy);
                    } else {
                        setMountBehavior(mount.getUUID(), MountBehavior.FOLLOW);
                    }
                }
            }
        }
    }

    private static LivingEntity findNearestEnemy(Player player, Mob mount, double range) {
        LivingEntity nearest = null;
        double nearestDist = range * range;
        
        for (Entity entity : mount.level().getEntities(mount, mount.getBoundingBox().inflate(range))) {
            if (!(entity instanceof LivingEntity living)) continue;
            if (entity == player || entity == mount) continue;
            if (!entity.isAlive()) continue;
            
            if (entity instanceof Player targetPlayer && ContractEvents.isSameTeam(player, targetPlayer)) {
                continue;
            }
            
            double dist = mount.distanceToSqr(living);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = living;
            }
        }
        
        return nearest;
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            UUID riderUUID = MOUNT_RIDER_MAP.remove(mob.getUUID());
            if (riderUUID != null) {
                RIDER_MOUNT_MAP.remove(riderUUID);
                MOUNT_BEHAVIOR_MAP.remove(mob.getUUID());
                
                Entity rider = mob.level() instanceof ServerLevel sl ? sl.getEntity(riderUUID) : null;
                if (rider instanceof Player player) {
                    player.getPersistentData().remove(TAG_MOUNT_UUID);
                    
                    if (!player.level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                        serverPlayer.displayClientMessage(
                            Component.literal("§c[驯兽师] §r坐骑已死亡，解除骑乘"),
                            true
                        );
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        if (event.getTarget() instanceof Mob mob) {
            if (isBeastRider(player) && !isMounted(player)) {
                if (canMount(player, mob)) {
                    event.setCanceled(true);
                    mountCreature(player, mob);
                }
            }
        }
    }

    public static void loadFromNBT(Player player) {
        CompoundTag tag = player.getPersistentData();
        if (tag.contains(TAG_MOUNT_UUID)) {
            UUID mountUUID = tag.getUUID(TAG_MOUNT_UUID);
            RIDER_MOUNT_MAP.put(player.getUUID(), mountUUID);
        }
    }

    public static void clearData() {
        RIDER_MOUNT_MAP.clear();
        MOUNT_RIDER_MAP.clear();
        MOUNT_BEHAVIOR_MAP.clear();
        MOUNT_COOLDOWN_MAP.clear();
    }
}
