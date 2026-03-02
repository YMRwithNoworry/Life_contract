package org.alku.life_contract.byte_chen;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class ByteChenEquipmentHandler {
    
    private static final String TAG_HAS_FULL_SET = "ByteChenHasFullSet";
    private static final String TAG_EMERGENCY_COMPUTE_COOLDOWN = "ByteChenEmergencyComputeCooldown";
    private static final String TAG_KILLER_HIGHLIGHT_TIMER = "ByteChenKillerHighlightTimer";
    private static final String TAG_LAST_KILLER_ID = "ByteChenLastKillerId";
    
    private static final UUID MELEE_DAMAGE_PENALTY_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567900");
    private static final UUID MELEE_RECEIVED_PENALTY_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345679011");
    private static final UUID SPEED_BONUS_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456790022");
    private static final UUID DIG_SPEED_BONUS_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890133");
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickEquipmentEffects(player);
        }
    }
    
    private static void tickEquipmentEffects(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;
        
        CompoundTag data = player.getPersistentData();
        
        int emergencyCd = data.getInt(TAG_EMERGENCY_COMPUTE_COOLDOWN);
        if (emergencyCd > 0) {
            data.putInt(TAG_EMERGENCY_COMPUTE_COOLDOWN, emergencyCd - 1);
        }
        
        int highlightTimer = data.getInt(TAG_KILLER_HIGHLIGHT_TIMER);
        if (highlightTimer > 0) {
            data.putInt(TAG_KILLER_HIGHLIGHT_TIMER, highlightTimer - 1);
        }
        
        boolean hasFullSet = hasFullDataTerminalSet(player);
        boolean hadFullSet = data.getBoolean(TAG_HAS_FULL_SET);
        
        if (hasFullSet != hadFullSet) {
            data.putBoolean(TAG_HAS_FULL_SET, hasFullSet);
            if (hasFullSet) {
                applySetBonus(player, profession);
                player.sendSystemMessage(Component.literal("§b[字节陈] 数据终端套装效果激活！"));
            } else {
                removeSetBonus(player);
                player.sendSystemMessage(Component.literal("§7[字节陈] 数据终端套装效果消失。"));
            }
        }
        
        if (hasFullSet) {
            tickSetPenalties(player, profession);
        }
        
        checkEmergencyCompute(player, data, profession);
        
        tickWeaponBonus(player, profession);
        tickOffHandBonus(player, profession);
    }
    
    private static boolean hasFullDataTerminalSet(Player player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        
        return helmet.getItem() instanceof DataGogglesItem &&
               chestplate.getItem() instanceof TerminalRobeItem &&
               leggings.getItem() instanceof DataLeggingsItem &&
               boots.getItem() instanceof FlashBootsItem;
    }
    
    public static boolean hasScepterInMainHand(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof ByteCodeScepterItem;
    }
    
    public static boolean hasChipInOffHand(Player player) {
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof ChenCoreChipItem;
    }
    
    public static boolean hasEmptyOffHand(Player player) {
        ItemStack offHand = player.getOffhandItem();
        return offHand.isEmpty();
    }
    
    private static void applySetBonus(ServerPlayer player, Profession profession) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.addPermanentModifier(new AttributeModifier(
                SPEED_BONUS_UUID, "byte_chen_set_speed",
                profession.getByteChenLightweightSpeedBonus(), AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
        
        applyMeleePenalty(player, profession);
    }
    
    private static void removeSetBonus(ServerPlayer player) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) {
            speedAttr.removeModifier(SPEED_BONUS_UUID);
        }
        
        removeMeleePenalty(player);
    }
    
    private static void applyMeleePenalty(ServerPlayer player, Profession profession) {
        var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.addPermanentModifier(new AttributeModifier(
                MELEE_DAMAGE_PENALTY_UUID, "byte_chen_melee_penalty",
                profession.getByteChenLightweightMeleePenalty(), AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }
    
    private static void removeMeleePenalty(ServerPlayer player) {
        var attackAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attackAttr != null) {
            attackAttr.removeModifier(MELEE_DAMAGE_PENALTY_UUID);
        }
    }
    
    private static void tickSetPenalties(ServerPlayer player, Profession profession) {
    }
    
    private static void checkEmergencyCompute(ServerPlayer player, CompoundTag data, Profession profession) {
        if (!hasChipInOffHand(player)) return;
        
        int compute = ByteChenSystem.getCompute(player);
        int threshold = profession.getByteChenComputeLowThreshold();
        
        if (compute < threshold) {
            int emergencyCd = data.getInt(TAG_EMERGENCY_COMPUTE_COOLDOWN);
            if (emergencyCd <= 0) {
                ByteChenSystem.addCompute(player, 50);
                data.putInt(TAG_EMERGENCY_COMPUTE_COOLDOWN, 1200);
                
                player.sendSystemMessage(Component.literal("§e[陈式核心芯片] 应急算力触发！回复 50 点算力"));
                
                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.END_ROD,
                        player.getX(), player.getY() + 1, player.getZ(),
                        30, 0.5, 0.5, 0.5, 0.1);
                    serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0f, 1.5f);
                }
            }
        }
    }
    
    private static void tickWeaponBonus(ServerPlayer player, Profession profession) {
    }
    
    private static void tickOffHandBonus(ServerPlayer player, Profession profession) {
        if (hasScepterInMainHand(player) && hasEmptyOffHand(player)) {
            if (player.getServer().getTickCount() % 20 == 0) {
                int bonus = (int)(profession.getByteChenComputeRegenRate() * 0.5f);
                if (bonus > 0) {
                    ByteChenSystem.addCompute(player, bonus);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;
        
        if (!hasFullDataTerminalSet(player)) return;
        
        if (event.getSource().getEntity() != null) {
            float penalty = profession.getByteChenLightweightReceivedMeleePenalty();
            float newDamage = event.getAmount() * (1.0f + penalty);
            event.setAmount(newDamage);
        }
    }
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer victim)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(victim);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;
        
        if (!hasChipInOffHand(victim)) return;
        
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            if (ContractEvents.isSameTeam(victim, killer)) {
                CompoundTag data = victim.getPersistentData();
                ByteChenSystem.addCompute(victim, 30);
                data.putInt(TAG_KILLER_HIGHLIGHT_TIMER, 200);
                data.putInt(TAG_LAST_KILLER_ID, killer.getId());
                
                victim.sendSystemMessage(Component.literal("§e[陈式核心芯片] 检测到友方被击杀，获得 30 点算力！"));
            }
        }
    }
    
    public static boolean hasFullSetBonus(Player player) {
        return player.getPersistentData().getBoolean(TAG_HAS_FULL_SET);
    }
    
    public static int getComputeMaxBonus(Player player, Profession profession) {
        if (hasFullDataTerminalSet(player)) {
            return 50;
        }
        return 0;
    }
    
    public static float getNodeRangeBonus(Player player) {
        if (hasFullDataTerminalSet(player)) {
            return 0.2f;
        }
        return 0.0f;
    }
    
    public static float getSkillRangeBonus(Player player) {
        if (hasFullDataTerminalSet(player)) {
            return 10.0f;
        }
        return 0.0f;
    }
    
    public static float getNodeCostReduction(Player player) {
        if (hasScepterInMainHand(player)) {
            return 0.1f;
        }
        return 0.0f;
    }
    
    public static float getSkillDurationBonus(Player player) {
        if (hasScepterInMainHand(player)) {
            return 0.3f;
        }
        return 0.0f;
    }
    
    public static float getCooldownReduction(Player player) {
        if (hasChipInOffHand(player)) {
            return 0.15f;
        }
        return 0.0f;
    }
    
    public static int getEffectiveNodeMax(Player player, Profession profession) {
        int base = profession.getByteChenNodeMax();
        if (hasFullDataTerminalSet(player)) {
            base += 2;
        }
        return base;
    }
}
