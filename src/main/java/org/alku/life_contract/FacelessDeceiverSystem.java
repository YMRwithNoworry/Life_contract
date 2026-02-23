package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class FacelessDeceiverSystem {

    private static final String TAG_CONTRACT_ENTITY_UUID = "FacelessDeceiverContractEntity";
    private static final String TAG_CONTRACT_ENTITY_TYPE = "FacelessDeceiverContractEntityType";
    private static final String TAG_CONTRACT_ENTITY_NAME = "FacelessDeceiverContractEntityName";
    
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ARMOR_MODIFIER_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID DAMAGE_MODIFIER_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-def1-234567890123");
    private static final UUID KNOCKBACK_RESISTANCE_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-ef12-345678901234");
    
    private static final Map<UUID, UUID> PLAYER_CONTRACT_ENTITY_MAP = new HashMap<>();
    private static final Map<UUID, UUID> ENTITY_OWNER_MAP = new HashMap<>();
    
    public static boolean isFacelessDeceiver(Player player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) {
            return false;
        }
        Profession profession = ProfessionConfig.getProfession(professionId);
        return profession != null && profession.isFacelessDeceiver();
    }
    
    public static boolean hasContract(Player player) {
        return player.getPersistentData().contains(TAG_CONTRACT_ENTITY_UUID);
    }
    
    public static UUID getContractEntityUUID(Player player) {
        if (player.getPersistentData().contains(TAG_CONTRACT_ENTITY_UUID)) {
            return player.getPersistentData().getUUID(TAG_CONTRACT_ENTITY_UUID);
        }
        return null;
    }
    
    public static String getContractEntityType(Player player) {
        return player.getPersistentData().getString(TAG_CONTRACT_ENTITY_TYPE);
    }
    
    public static String getContractEntityName(Player player) {
        return player.getPersistentData().getString(TAG_CONTRACT_ENTITY_NAME);
    }
    
    public static void establishContract(ServerPlayer player, Mob targetMob) {
        if (!isFacelessDeceiver(player)) {
            return;
        }
        
        if (hasContract(player)) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.system.already_contracted"));
            return;
        }
        
        if (targetMob.getPersistentData().contains("FollowerOwnerUUID")) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.system.has_master"));
            return;
        }
        
        UUID mobUUID = targetMob.getUUID();
        String mobType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(targetMob.getType()).toString();
        String mobName = targetMob.hasCustomName() ? targetMob.getCustomName().getString() : targetMob.getName().getString();
        
        player.getPersistentData().putUUID(TAG_CONTRACT_ENTITY_UUID, mobUUID);
        player.getPersistentData().putString(TAG_CONTRACT_ENTITY_TYPE, mobType);
        player.getPersistentData().putString(TAG_CONTRACT_ENTITY_NAME, mobName);
        
        targetMob.getPersistentData().putUUID("FacelessDeceiverOwner", player.getUUID());
        
        PLAYER_CONTRACT_ENTITY_MAP.put(player.getUUID(), mobUUID);
        ENTITY_OWNER_MAP.put(mobUUID, player.getUUID());
        
        applyEntityAttributes(player, targetMob);
        
        playContractEstablishEffects(player, targetMob);
        
        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.translatable("faceless_deceiver.system.contract_success"));
        player.sendSystemMessage(Component.translatable("faceless_deceiver.system.contract_with", mobName));
        player.sendSystemMessage(Component.translatable("faceless_deceiver.system.attribute_applied"));
        player.sendSystemMessage(Component.translatable("faceless_deceiver.system.death_warning"));
        player.sendSystemMessage(Component.literal(""));
        
        syncContractData(player);
    }
    
    private static void applyEntityAttributes(ServerPlayer player, Mob mob) {
        removeContractAttributes(player);
        
        double mobMaxHealth = mob.getMaxHealth();
        double mobArmor = mob.getAttributeValue(Attributes.ARMOR);
        double mobDamage = mob.getAttributeValue(Attributes.ATTACK_DAMAGE);
        double mobSpeed = mob.getAttributeValue(Attributes.MOVEMENT_SPEED);
        double mobKnockbackResistance = mob.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null && mobMaxHealth > 0) {
            AttributeModifier healthModifier = new AttributeModifier(
                HEALTH_MODIFIER_UUID,
                "FacelessDeceiverHealth",
                mobMaxHealth,
                AttributeModifier.Operation.ADDITION
            );
            healthAttribute.addPermanentModifier(healthModifier);
        }
        
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null && mobArmor > 0) {
            AttributeModifier armorModifier = new AttributeModifier(
                ARMOR_MODIFIER_UUID,
                "FacelessDeceiverArmor",
                mobArmor,
                AttributeModifier.Operation.ADDITION
            );
            armorAttribute.addPermanentModifier(armorModifier);
        }
        
        var damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null && mobDamage > 0) {
            AttributeModifier damageModifier = new AttributeModifier(
                DAMAGE_MODIFIER_UUID,
                "FacelessDeceiverDamage",
                mobDamage,
                AttributeModifier.Operation.ADDITION
            );
            damageAttribute.addPermanentModifier(damageModifier);
        }
        
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null && mobSpeed > 0) {
            double basePlayerSpeed = 0.1;
            double speedBonus = mobSpeed - basePlayerSpeed;
            if (speedBonus > 0) {
                AttributeModifier speedModifier = new AttributeModifier(
                    SPEED_MODIFIER_UUID,
                    "FacelessDeceiverSpeed",
                    speedBonus,
                    AttributeModifier.Operation.ADDITION
                );
                speedAttribute.addPermanentModifier(speedModifier);
            }
        }
        
        var knockbackAttribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null && mobKnockbackResistance > 0) {
            AttributeModifier knockbackModifier = new AttributeModifier(
                KNOCKBACK_RESISTANCE_UUID,
                "FacelessDeceiverKnockback",
                mobKnockbackResistance,
                AttributeModifier.Operation.ADDITION
            );
            knockbackAttribute.addPermanentModifier(knockbackModifier);
        }
        
        applySpecialAbilities(player, mob);
    }
    
    private static void applySpecialAbilities(ServerPlayer player, Mob mob) {
        String mobType = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(mob.getType()).toString();
        
        if (mobType.contains("blaze") || mobType.contains("magma")) {
            if (!player.hasEffect(MobEffects.FIRE_RESISTANCE)) {
                player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
            }
        }
        
        if (mobType.contains("spider") || mobType.contains("cave_spider")) {
            player.getPersistentData().putBoolean("FacelessDeceiverClimb", true);
        }
        
        if (mobType.contains("enderman") || mobType.contains("shulker")) {
            player.getPersistentData().putBoolean("FacelessDeceiverTeleport", true);
        }
        
        if (mobType.contains("dolphin")) {
            player.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, Integer.MAX_VALUE, 0, false, false));
        }
        
        if (mobType.contains("wither_skeleton") || mobType.contains("wither")) {
            player.getPersistentData().putBoolean("FacelessDeceiverWitherAttack", true);
        }
    }
    
    public static void removeContractAttributes(ServerPlayer player) {
        var healthAttribute = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttribute != null) {
            healthAttribute.removeModifier(HEALTH_MODIFIER_UUID);
        }
        
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute != null) {
            armorAttribute.removeModifier(ARMOR_MODIFIER_UUID);
        }
        
        var damageAttribute = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttribute != null) {
            damageAttribute.removeModifier(DAMAGE_MODIFIER_UUID);
        }
        
        var speedAttribute = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttribute != null) {
            speedAttribute.removeModifier(SPEED_MODIFIER_UUID);
        }
        
        var knockbackAttribute = player.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
        if (knockbackAttribute != null) {
            knockbackAttribute.removeModifier(KNOCKBACK_RESISTANCE_UUID);
        }
        
        player.removeEffect(MobEffects.FIRE_RESISTANCE);
        player.removeEffect(MobEffects.DOLPHINS_GRACE);
        
        player.getPersistentData().remove("FacelessDeceiverClimb");
        player.getPersistentData().remove("FacelessDeceiverTeleport");
        player.getPersistentData().remove("FacelessDeceiverWitherAttack");
    }
    
    @SubscribeEvent
    public static void onEntityDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) return;
        
        if (entity instanceof Mob mob) {
            UUID ownerUUID = ENTITY_OWNER_MAP.get(mob.getUUID());
            if (ownerUUID == null) {
                ownerUUID = mob.getPersistentData().hasUUID("FacelessDeceiverOwner") ? 
                    mob.getPersistentData().getUUID("FacelessDeceiverOwner") : null;
            }
            
            if (ownerUUID != null) {
                if (mob.level() instanceof ServerLevel serverLevel) {
                    ServerPlayer owner = serverLevel.getServer().getPlayerList().getPlayer(ownerUUID);
                    if (owner != null && isFacelessDeceiver(owner)) {
                        handleContractEntityDeath(owner, mob);
                    }
                }
            }
        }
    }
    
    private static void handleContractEntityDeath(ServerPlayer player, Mob deadMob) {
        String mobName = deadMob.hasCustomName() ? deadMob.getCustomName().getString() : deadMob.getName().getString();
        
        playContractDeathEffects(player, deadMob);
        
        player.getPersistentData().remove(TAG_CONTRACT_ENTITY_UUID);
        player.getPersistentData().remove(TAG_CONTRACT_ENTITY_TYPE);
        player.getPersistentData().remove(TAG_CONTRACT_ENTITY_NAME);
        
        ENTITY_OWNER_MAP.remove(deadMob.getUUID());
        PLAYER_CONTRACT_ENTITY_MAP.remove(player.getUUID());
        
        removeContractAttributes(player);
        
        player.getServer().getPlayerList().broadcastSystemMessage(
            Component.translatable("faceless_deceiver.system.death_broadcast", player.getName().getString(), mobName),
            false
        );
        
        player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
        
        syncContractData(player);
    }
    
    private static void playContractEstablishEffects(ServerPlayer player, Mob mob) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                mob.getX(), mob.getY() + 1, mob.getZ(),
                50, 0.5, 0.5, 0.5, 0.1
            );
            
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 0.5, 0.5, 0.5
            );
            
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.3, 0.3, 0.3, 0.05
            );
            
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 0.5F);
            player.level().playSound(null, mob.blockPosition(), 
                SoundEvents.SOUL_ESCAPE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    
    private static void playContractDeathEffects(ServerPlayer player, Mob deadMob) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL,
                deadMob.getX(), deadMob.getY() + 1, deadMob.getZ(),
                100, 1.0, 1.0, 1.0, 0.2
            );
            
            serverLevel.sendParticles(
                ParticleTypes.DAMAGE_INDICATOR,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 0.5, 0.5, 0.1
            );
            
            serverLevel.sendParticles(
                ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1, player.getZ(),
                5, 0.5, 0.5, 0.5, 0.0
            );
            
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 1.0F, 0.5F);
            player.level().playSound(null, player.blockPosition(), 
                SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 1.0F, 1.0F);
        }
    }
    
    public static void updateContractAttributes(ServerPlayer player) {
        if (!isFacelessDeceiver(player) || !hasContract(player)) {
            return;
        }
        
        UUID contractEntityUUID = getContractEntityUUID(player);
        if (contractEntityUUID == null) return;
        
        if (player.level() instanceof ServerLevel serverLevel) {
            for (Entity entity : serverLevel.getAllEntities()) {
                if (entity instanceof Mob mob && entity.getUUID().equals(contractEntityUUID)) {
                    applyEntityAttributes(player, mob);
                    break;
                }
            }
        }
    }
    
    public static void onPlayerJoin(ServerPlayer player) {
        if (hasContract(player)) {
            UUID contractEntityUUID = getContractEntityUUID(player);
            if (contractEntityUUID != null) {
                PLAYER_CONTRACT_ENTITY_MAP.put(player.getUUID(), contractEntityUUID);
                
                if (player.level() instanceof ServerLevel serverLevel) {
                    for (Entity entity : serverLevel.getAllEntities()) {
                        if (entity instanceof Mob mob && entity.getUUID().equals(contractEntityUUID)) {
                            ENTITY_OWNER_MAP.put(contractEntityUUID, player.getUUID());
                            applyEntityAttributes(player, mob);
                            break;
                        }
                    }
                }
            }
        }
    }
    
    public static void onPlayerDeath(ServerPlayer player) {
        if (hasContract(player)) {
            removeContractAttributes(player);
        }
    }
    
    public static void onPlayerRespawn(ServerPlayer player) {
        if (hasContract(player)) {
            updateContractAttributes(player);
        }
    }
    
    private static void syncContractData(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(
            net.minecraftforge.network.PacketDistributor.ALL.noArg(),
            new PacketSyncContract(player)
        );
    }
    
    public static void tickContractEffects(ServerPlayer player) {
        if (!isFacelessDeceiver(player) || !hasContract(player)) {
            return;
        }
        
        if (player.tickCount % 60 == 0) {
            UUID contractEntityUUID = getContractEntityUUID(player);
            if (contractEntityUUID != null && player.level() instanceof ServerLevel serverLevel) {
                Entity contractEntity = null;
                for (Entity entity : serverLevel.getAllEntities()) {
                    if (entity.getUUID().equals(contractEntityUUID)) {
                        contractEntity = entity;
                        break;
                    }
                }
                
                if (contractEntity != null && contractEntity.isAlive()) {
                    double distance = player.distanceTo(contractEntity);
                    if (distance <= 30) {
                        serverLevel.sendParticles(
                            ParticleTypes.SOUL_FIRE_FLAME,
                            player.getX(), player.getY() + 1, player.getZ(),
                            5, 0.3, 0.5, 0.3, 0.02
                        );
                    }
                }
            }
        }
        
        if (player.tickCount % 200 == 0) {
            String entityName = getContractEntityName(player);
            if (entityName != null && !entityName.isEmpty()) {
                player.displayClientMessage(
                    Component.translatable("faceless_deceiver.system.contract_entity", entityName),
                    true
                );
            }
        }
    }
    
    public static boolean isContractEntity(Mob mob) {
        return ENTITY_OWNER_MAP.containsKey(mob.getUUID()) || 
               mob.getPersistentData().contains("FacelessDeceiverOwner");
    }
    
    public static UUID getOwnerOfContractEntity(Mob mob) {
        if (ENTITY_OWNER_MAP.containsKey(mob.getUUID())) {
            return ENTITY_OWNER_MAP.get(mob.getUUID());
        }
        if (mob.getPersistentData().hasUUID("FacelessDeceiverOwner")) {
            return mob.getPersistentData().getUUID("FacelessDeceiverOwner");
        }
        return null;
    }
}
