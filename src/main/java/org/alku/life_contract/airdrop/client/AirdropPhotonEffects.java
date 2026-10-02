package org.alku.life_contract.airdrop.client;

import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction;
import com.lowdragmc.photon.client.gameobject.emitter.data.number.NumberFunction3;
import com.lowdragmc.photon.client.gameobject.emitter.particle.ParticleEmitter;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.airdrop.entity.AirdropEntity;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT)
public final class AirdropPhotonEffects {
    private static final FX RED_SMOKE = createRedSmoke();

    private AirdropPhotonEffects() {
    }

    @SubscribeEvent
    public static void onEntityTick(EntityTickEvent.Post event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof AirdropEntity) || !entity.level().isClientSide || !entity.isAlive()) {
            return;
        }

        var activeEffects = EntityEffectExecutor.CACHE.get(entity);
        if (activeEffects != null && activeEffects.stream().anyMatch(effect -> effect.getFx() == RED_SMOKE)) {
            return;
        }

        var effect = new EntityEffectExecutor(RED_SMOKE, entity.level(), entity,
                EntityEffectExecutor.AutoRotate.NONE);
        effect.setAllowMulti(false);
        effect.setForcedDeath(true);
        effect.setOffset(new org.joml.Vector3f(0.0F, 0.6F, 0.0F));
        effect.start();
    }

    private static FX createRedSmoke() {
        FX fx = new FX();
        fx.setFxLocation(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                Life_contract.MODID, "airdrop_red_smoke"));

        ParticleEmitter smoke = new ParticleEmitter();
        smoke.setName("airdrop_red_smoke");
        smoke.config.setLooping(true);
        smoke.config.setDuration(40);
        smoke.config.setMaxParticles(72);
        smoke.config.setStartLifetime(NumberFunction.constant(240));
        smoke.config.setStartSpeed(NumberFunction.constant(0.08F));
        smoke.config.setStartSize(new NumberFunction3(1.7F, 1.7F, 1.7F));
        smoke.config.setStartColor(NumberFunction.color(0xFFFF2028));
        smoke.config.velocityOverLifetime.setEnable(true);
        smoke.config.velocityOverLifetime.setSpace(
                com.lowdragmc.photon.client.gameobject.emitter.data.ValueSpace.World);
        smoke.config.velocityOverLifetime.setLinear(new NumberFunction3(0.0F, 2.0F, 0.0F));
        smoke.config.emission.setEmissionRate(NumberFunction.constant(0.4F));
        smoke.config.renderer.getMaterials().getFirst().setCull(false);
        smoke.config.renderer.getMaterials().getFirst().setDepthMask(false);

        fx.getFxData().objects().add(smoke);
        return fx;
    }
}
