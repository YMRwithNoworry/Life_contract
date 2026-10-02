package org.alku.life_contract;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.resources.ResourceLocation;

public class SlowInfectionEffect extends MobEffect {
    
    public SlowInfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A7C2E);
        
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, 
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "slow_infection_movement_speed"),
            -0.15, 
            AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
    }
    
    @Override
    public boolean applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.hurt(entity.level().damageSources().magic(), 1.0F);
        }
        return true;
    }
    
    @Override
    public boolean shouldApplyEffectTickThisTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }
}
