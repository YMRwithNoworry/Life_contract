package org.alku.life_contract.wraith_councilor;

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
import org.alku.life_contract.GhostSenatorSystem;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class WraithEquipmentHandler {
    
    private static final String TAG_EMBLEM_COOLDOWN = "WraithEmblemCooldown";
    private static final String TAG_HAS_FULL_SET = "WraithHasFullSet";
    
    private static final UUID HEALTH_PENALTY_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-def1-234567890123");
    private static final UUID ARMOR_PENALTY_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-ef12-345678901234");
    private static final UUID SUMMON_HEALTH_UUID = UUID.fromString("f6a7b8c9-d0e1-2345-f123-456789012345");
    private static final UUID SUMMON_DAMAGE_UUID = UUID.fromString("a7b8c9d0-e1f2-3456-0123-567890123456");
    
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
        if (profession == null || !profession.isWraithCouncilor()) return;
        
        CompoundTag data = player.getPersistentData();
        
        int emblemCd = data.getInt(TAG_EMBLEM_COOLDOWN);
        if (emblemCd > 0) {
            data.putInt(TAG_EMBLEM_COOLDOWN, emblemCd - 1);
        }
        
        boolean hasFullSet = hasFullSoulforgedSet(player);
        boolean hadFullSet = data.getBoolean(TAG_HAS_FULL_SET);
        
        if (hasFullSet != hadFullSet) {
            data.putBoolean(TAG_HAS_FULL_SET, hasFullSet);
            if (hasFullSet) {
                applySetBonus(player, profession);
                player.sendSystemMessage(Component.literal("§5[亡魂议员] 魂铸议会套装效果激活！"));
            } else {
                removeSetBonus(player);
                player.sendSystemMessage(Component.literal("§7[亡魂议员] 魂铸议会套装效果消失。"));
            }
        }
        
        if (hasFullSet) {
            tickSunlightPenalty(player, profession);
        }
        
        if (hasStaffInMainHand(player) && hasEmptyOffHand(player)) {
            if (player.getServer().getTickCount() % 20 == 0) {
                addSoulValue(player, profession, 1);
            }
        }
    }
    
    private static boolean hasFullSoulforgedSet(Player player) {
        ItemStack helmet = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.HEAD);
        ItemStack chestplate = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST);
        ItemStack leggings = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.LEGS);
        ItemStack boots = player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.FEET);
        
        return helmet.getItem() instanceof SoulforgedArmorItem &&
               chestplate.getItem() instanceof SoulforgedArmorItem &&
               leggings.getItem() instanceof SoulforgedArmorItem &&
               boots.getItem() instanceof SoulforgedArmorItem;
    }
    
    private static boolean hasStaffInMainHand(Player player) {
        ItemStack mainHand = player.getMainHandItem();
        return mainHand.getItem() instanceof CouncilSoulCandleStaffItem;
    }
    
    private static boolean hasEmptyOffHand(Player player) {
        ItemStack offHand = player.getOffhandItem();
        return offHand.isEmpty();
    }
    
    private static boolean hasEmblemInOffHand(Player player) {
        ItemStack offHand = player.getOffhandItem();
        return offHand.getItem() instanceof CouncilEmblemItem;
    }
    
    private static void applySetBonus(ServerPlayer player, Profession profession) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.addPermanentModifier(new AttributeModifier(
                SUMMON_HEALTH_UUID, "soulforged_set_health",
                40, AttributeModifier.Operation.ADDITION
            ));
        }
    }
    
    private static void removeSetBonus(ServerPlayer player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(SUMMON_HEALTH_UUID);
        }
        
        removeSunlightPenalty(player);
    }
    
    private static void tickSunlightPenalty(ServerPlayer player, Profession profession) {
        boolean inSunlight = isInSunlight(player);
        
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        
        if (inSunlight) {
            if (healthAttr != null && healthAttr.getModifier(HEALTH_PENALTY_UUID) == null) {
                healthAttr.addPermanentModifier(new AttributeModifier(
                    HEALTH_PENALTY_UUID, "soulforged_sunlight_health",
                    -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
            if (armorAttr != null && armorAttr.getModifier(ARMOR_PENALTY_UUID) == null) {
                armorAttr.addPermanentModifier(new AttributeModifier(
                    ARMOR_PENALTY_UUID, "soulforged_sunlight_armor",
                    -0.3, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        } else {
            removeSunlightPenalty(player);
        }
    }
    
    private static void removeSunlightPenalty(ServerPlayer player) {
        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        
        if (healthAttr != null) {
            healthAttr.removeModifier(HEALTH_PENALTY_UUID);
        }
        if (armorAttr != null) {
            armorAttr.removeModifier(ARMOR_PENALTY_UUID);
        }
    }
    
    private static boolean isInSunlight(ServerPlayer player) {
        if (player.level().dimensionType().hasCeiling()) return false;
        int lightLevel = player.level().getMaxLocalRawBrightness(player.blockPosition());
        return lightLevel > 12 && player.level().canSeeSky(player.blockPosition());
    }
    
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isWraithCouncilor()) return;
        
        if (!hasEmblemInOffHand(player)) return;
        
        CompoundTag data = player.getPersistentData();
        int emblemCd = data.getInt(TAG_EMBLEM_COOLDOWN);
        
        if (emblemCd > 0) return;
        
        int soulValue = GhostSenatorSystem.getSoulValue(player);
        float healthRestore = soulValue / 20.0f;
        
        if (healthRestore >= 1.0f) {
            event.setCanceled(true);
            player.setHealth(1.0f);
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 200, (int)healthRestore - 1));
            
            data.putInt(TAG_SOUL_VALUE, 0);
            data.putInt(TAG_EMBLEM_COOLDOWN, 90 * 20);
            
            player.sendSystemMessage(Component.literal("§5[冥府议员徽记] 冥魂护佑，死里逃生！"));
            
            if (player.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SOUL, 
                    player.getX(), player.getY() + 1, player.getZ(), 
                    100, 1, 1, 1, 0.2);
            }
            
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.8f);
        }
    }
    
    private static final String TAG_SOUL_VALUE = "WraithSoulValue";
    
    private static void addSoulValue(ServerPlayer player, Profession profession, int amount) {
        CompoundTag data = player.getPersistentData();
        int soulValue = data.getInt(TAG_SOUL_VALUE);
        int maxSoul = profession.getWraithSoulMax();
        
        boolean hasFullSet = hasFullSoulforgedSet(player);
        if (hasFullSet) {
            maxSoul += 40;
        }
        
        soulValue = Math.min(maxSoul, soulValue + amount);
        data.putInt(TAG_SOUL_VALUE, soulValue);
    }
    
    public static boolean hasFullSetBonus(Player player) {
        return player.getPersistentData().getBoolean(TAG_HAS_FULL_SET);
    }
    
    public static boolean hasStaffBonus(Player player) {
        return hasStaffInMainHand(player);
    }
    
    public static boolean hasEmblemBonus(Player player) {
        return hasEmblemInOffHand(player);
    }
    
    public static float getRangeBonus(Player player) {
        if (hasStaffInMainHand(player)) {
            return 2.0f;
        }
        return 0.0f;
    }
    
    public static float getAreaBonus(Player player) {
        if (hasStaffInMainHand(player)) {
            return 0.1f;
        }
        return 0.0f;
    }
    
    public static float getErosionDurationBonus(Player player) {
        if (hasStaffInMainHand(player)) {
            return 0.5f;
        }
        return 0.0f;
    }
    
    public static float getSummonHealthBonus(Player player) {
        if (hasEmblemInOffHand(player)) {
            return 0.3f;
        }
        return 0.0f;
    }
    
    public static float getSummonDamageBonus(Player player) {
        if (hasEmblemInOffHand(player)) {
            return 0.2f;
        }
        return 0.0f;
    }
}
