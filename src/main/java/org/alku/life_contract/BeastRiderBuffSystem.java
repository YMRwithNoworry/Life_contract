package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BeastRiderBuffSystem {

    public static final String TAG_BEAST_RIDER_BUFFED = "BeastRiderBuffed";
    public static final String TAG_BUFF_OWNER_UUID = "BeastRiderBuffOwner";
    
    private static final Set<UUID> BUFFED_MOBS = new HashSet<>();
    private static final Map<UUID, Long> LAST_BUFF_APPLY = new HashMap<>();
    
    private static final int RESISTANCE_LEVEL = 1;
    private static final int REGENERATION_LEVEL = 0;
    private static final int BUFF_DURATION = 400;
    private static final int BUFF_APPLY_INTERVAL = 200;
    
    private static final UUID HEALTH_BONUS_UUID = UUID.fromString("c3d4e5f6-7890-abcd-ef12-34567890abcd");
    private static final float HEALTH_BONUS = 10.0f;

    public static boolean isBeastRiderBuffed(Mob mob) {
        CompoundTag tag = mob.getPersistentData();
        return tag.contains(TAG_BEAST_RIDER_BUFFED) && tag.getBoolean(TAG_BEAST_RIDER_BUFFED);
    }

    public static void applyBeastRiderBuff(Mob mob, UUID ownerUUID) {
        if (isBeastRiderBuffed(mob)) {
            return;
        }
        
        CompoundTag tag = mob.getPersistentData();
        tag.putBoolean(TAG_BEAST_RIDER_BUFFED, true);
        tag.putUUID(TAG_BUFF_OWNER_UUID, ownerUUID);
        
        BUFFED_MOBS.add(mob.getUUID());
        
        applyPermanentEffects(mob);
        
        applyHealthBonus(mob);
        
        if (!mob.level().isClientSide && mob.level() instanceof ServerLevel serverLevel) {
            spawnBuffParticles(serverLevel, mob);
        }
    }

    private static void applyPermanentEffects(Mob mob) {
        MobEffectInstance resistance = new MobEffectInstance(
            MobEffects.DAMAGE_RESISTANCE,
            BUFF_DURATION,
            RESISTANCE_LEVEL,
            false,
            false,
            true
        );
        mob.addEffect(resistance);
        
        MobEffectInstance regeneration = new MobEffectInstance(
            MobEffects.REGENERATION,
            BUFF_DURATION,
            REGENERATION_LEVEL,
            false,
            false,
            true
        );
        mob.addEffect(regeneration);
    }

    private static void applyHealthBonus(Mob mob) {
        var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            AttributeModifier modifier = new AttributeModifier(
                HEALTH_BONUS_UUID,
                "BeastRiderHealthBonus",
                HEALTH_BONUS,
                AttributeModifier.Operation.ADDITION
            );
            
            healthAttr.removeModifier(HEALTH_BONUS_UUID);
            healthAttr.addPermanentModifier(modifier);
            
            mob.setHealth(mob.getHealth() + HEALTH_BONUS);
        }
    }

    private static void spawnBuffParticles(ServerLevel level, Mob mob) {
        double x = mob.getX();
        double y = mob.getY() + mob.getBbHeight() / 2;
        double z = mob.getZ();
        
        level.sendParticles(
            ParticleTypes.ENCHANT,
            x, y, z,
            30,
            0.5, 0.5, 0.5,
            0.5
        );
        
        level.sendParticles(
            ParticleTypes.HEART,
            x, y + 0.5, z,
            10,
            0.3, 0.3, 0.3,
            0.1
        );
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        long currentTime = event.getServer().getTickCount();
        
        for (net.minecraft.server.level.ServerLevel level : event.getServer().getAllLevels()) {
            for (net.minecraft.world.entity.Entity entity : level.getAllEntities()) {
                if (entity instanceof Mob mob && isBeastRiderBuffed(mob)) {
                    Long lastApply = LAST_BUFF_APPLY.get(mob.getUUID());
                    
                    if (lastApply == null || currentTime - lastApply >= BUFF_APPLY_INTERVAL) {
                        refreshBuffEffects(mob);
                        LAST_BUFF_APPLY.put(mob.getUUID(), currentTime);
                    }
                }
            }
        }
    }

    private static void refreshBuffEffects(Mob mob) {
        if (!mob.isAlive()) return;
        
        MobEffectInstance currentResistance = mob.getEffect(MobEffects.DAMAGE_RESISTANCE);
        if (currentResistance == null || currentResistance.getDuration() < BUFF_APPLY_INTERVAL) {
            MobEffectInstance resistance = new MobEffectInstance(
                MobEffects.DAMAGE_RESISTANCE,
                BUFF_DURATION,
                RESISTANCE_LEVEL,
                false,
                false,
                true
            );
            mob.addEffect(resistance);
        }
        
        MobEffectInstance currentRegeneration = mob.getEffect(MobEffects.REGENERATION);
        if (currentRegeneration == null || currentRegeneration.getDuration() < BUFF_APPLY_INTERVAL) {
            MobEffectInstance regeneration = new MobEffectInstance(
                MobEffects.REGENERATION,
                BUFF_DURATION,
                REGENERATION_LEVEL,
                false,
                false,
                true
            );
            mob.addEffect(regeneration);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity() instanceof Mob mob) {
            BUFFED_MOBS.remove(mob.getUUID());
            LAST_BUFF_APPLY.remove(mob.getUUID());
        }
    }

    public static void loadFromNBT(Mob mob) {
        CompoundTag tag = mob.getPersistentData();
        if (tag.contains(TAG_BEAST_RIDER_BUFFED) && tag.getBoolean(TAG_BEAST_RIDER_BUFFED)) {
            BUFFED_MOBS.add(mob.getUUID());
            
            var healthAttr = mob.getAttribute(Attributes.MAX_HEALTH);
            if (healthAttr != null && healthAttr.getModifier(HEALTH_BONUS_UUID) == null) {
                applyHealthBonus(mob);
            }
        }
    }

    public static void clearData() {
        BUFFED_MOBS.clear();
        LAST_BUFF_APPLY.clear();
    }
}
