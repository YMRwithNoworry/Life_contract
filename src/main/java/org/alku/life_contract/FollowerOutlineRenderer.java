package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class FollowerOutlineRenderer {

    private static final int RADIUS = 32;
    private static final int INTERVAL_TICKS = 4;
    private static final Random random = new Random();

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        Level world = mc.level;
        Player player = mc.player;
        if (player == null) return;

        int tick = (int) (world.getGameTime() % INTERVAL_TICKS);
        if (tick != 0) return;

        AABB box = new AABB(
                player.getX() - RADIUS, player.getY() - RADIUS, player.getZ() - RADIUS,
                player.getX() + RADIUS, player.getY() + RADIUS, player.getZ() + RADIUS);

        List<Mob> mobs = world.getEntitiesOfClass(Mob.class, box, m -> m != null);

        for (Mob mob : mobs) {
            CompoundTag data = mob.getPersistentData();
            if (data != null && data.hasUUID(FollowerWandItem.TAG_OWNER_UUID)) {
                UUID ownerUUID = data.getUUID(FollowerWandItem.TAG_OWNER_UUID);
                if (ownerUUID != null && ownerUUID.equals(player.getUUID())) {
                    double x = mob.getX();
                    double y = mob.getY() + mob.getBbHeight() * 0.5;
                    double z = mob.getZ();

                    for (int i = 0; i < 6; i++) {
                        double ox = x + (random.nextDouble() - 0.5) * mob.getBbWidth();
                        double oy = y + (random.nextDouble() - 0.5) * mob.getBbHeight();
                        double oz = z + (random.nextDouble() - 0.5) * mob.getBbWidth();
                        ParticleOptions particle = ParticleTypes.EFFECT;
                        world.addParticle(particle, ox, oy, oz, 1.0D, 0.0D, 0.0D);
                    }
                }
            }
        }
    }
}
