package org.alku.life_contract;

import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ExtendedArmor {

    public static final double VANILLA_ARMOR_CAP = 30.0;
    public static final double MAX_DAMAGE_REDUCTION = 0.95;

    public static double getTotalArmor(Player player) {
        var armorAttribute = player.getAttribute(Attributes.ARMOR);
        if (armorAttribute == null) {
            return 0.0;
        }
        return armorAttribute.getValue();
    }

    public static double getExtendedArmor(Player player) {
        double totalArmor = getTotalArmor(player);
        return Math.max(0, totalArmor - VANILLA_ARMOR_CAP);
    }

    public static double calculateDamageReduction(double totalArmor) {
        if (totalArmor <= 0) {
            return 0.0;
        }
        
        double reduction = 1.0 - (1.0 / (1.0 + totalArmor / 10.0));
        
        return Math.min(reduction, MAX_DAMAGE_REDUCTION);
    }

    public static double calculateExtendedDamageReduction(double extendedArmor) {
        if (extendedArmor <= 0) {
            return 0.0;
        }
        
        double vanillaReduction = calculateDamageReduction(VANILLA_ARMOR_CAP);
        double totalReduction = calculateDamageReduction(VANILLA_ARMOR_CAP + extendedArmor);
        
        return totalReduction - vanillaReduction;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide) {
            return;
        }
        
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        
        double extendedArmor = getExtendedArmor(player);
        if (extendedArmor <= 0) {
            return;
        }
        
        if (event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.DROWN) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.STARVE) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MAGIC) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.WITHER) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FELL_OUT_OF_WORLD) ||
            event.getSource().is(net.minecraft.world.damagesource.DamageTypes.GENERIC)) {
            return;
        }
        
        double extendedReduction = calculateExtendedDamageReduction(extendedArmor);
        if (extendedReduction <= 0) {
            return;
        }
        
        float originalDamage = event.getAmount();
        float reducedDamage = (float) (originalDamage * (1.0 - extendedReduction));
        
        event.setAmount(reducedDamage);
    }

    @OnlyIn(Dist.CLIENT)
    public static String getArmorDisplayText(Player player) {
        double totalArmor = getTotalArmor(player);
        double extendedArmor = getExtendedArmor(player);
        
        if (extendedArmor > 0) {
            double totalReduction = calculateDamageReduction(totalArmor);
            double reductionPercent = totalReduction * 100;
            
            return String.format("%.0f (+%.0f) %.1f%%", 
                VANILLA_ARMOR_CAP, 
                extendedArmor, 
                reductionPercent);
        }
        
        return String.format("%.0f", totalArmor);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getArmorColor(Player player) {
        double extendedArmor = getExtendedArmor(player);
        
        if (extendedArmor <= 0) {
            return 0xFFFFFF;
        }
        
        double totalReduction = calculateDamageReduction(getTotalArmor(player));
        double reductionRatio = totalReduction / MAX_DAMAGE_REDUCTION;
        
        int r = (int) (255 * (1 - reductionRatio * 0.5));
        int g = (int) (200 + 55 * reductionRatio);
        int b = (int) (100 + 155 * reductionRatio);
        
        return (r << 16) | (g << 8) | b;
    }

    @OnlyIn(Dist.CLIENT)
    public static float getArmorBarFill(Player player) {
        double totalArmor = getTotalArmor(player);
        return (float) Math.min(1.0, totalArmor / VANILLA_ARMOR_CAP);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean hasExtendedArmor(Player player) {
        return getExtendedArmor(player) > 0;
    }
}
