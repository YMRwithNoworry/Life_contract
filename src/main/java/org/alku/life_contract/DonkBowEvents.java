package org.alku.life_contract;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class DonkBowEvents {

    private static final Map<UUID, AbstractArrow> trackingArrows = new ConcurrentHashMap<>();

    @SubscribeEvent
    public static void onArrowImpact(ProjectileImpactEvent event) {
        if (event.getEntity().level().isClientSide) return;
        
        if (!(event.getEntity() instanceof AbstractArrow arrow)) return;
        
        trackingArrows.remove(arrow.getUUID());
        
        HitResult hitResult = event.getRayTraceResult();
        if (hitResult.getType() == HitResult.Type.ENTITY) {
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;
            Entity hitEntity = entityHitResult.getEntity();
            if (hitEntity instanceof LivingEntity target) {
                if (arrow.getPersistentData().getBoolean("DonkTrackingArrow")) {
                    DonkBowItem.applyRandomDebuff(target);
                }
            }
        }
    }
    
    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        
        Iterator<Map.Entry<UUID, AbstractArrow>> iterator = trackingArrows.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, AbstractArrow> entry = iterator.next();
            AbstractArrow arrow = entry.getValue();
            
            if (arrow.isRemoved() || !arrow.isAlive()) {
                iterator.remove();
                continue;
            }
            
            DonkBowItem.tickTrackingArrow(arrow);
        }
    }
    
    public static void addTrackingArrow(AbstractArrow arrow) {
        trackingArrows.put(arrow.getUUID(), arrow);
    }
}
