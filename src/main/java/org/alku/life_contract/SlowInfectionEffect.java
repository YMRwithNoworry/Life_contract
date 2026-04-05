package org.alku.life_contract;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class SlowInfectionEffect extends MobEffect {
    
    public SlowInfectionEffect() {
        super(MobEffectCategory.HARMFUL, 0x4A7C2E);
        
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, 
            "7A8B9C0D-1E2F-3A4B-5C6D-7E8F9A0B1C2D", 
            -0.15, 
            AttributeModifier.Operation.MULTIPLY_TOTAL);
    }
    
    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (!entity.level().isClientSide) {
            entity.hurt(entity.level().damageSources().magic(), 1.0F);
        }
    }
    
    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        int interval = 40 >> amplifier;
        return interval <= 0 || duration % interval == 0;
    }
}
