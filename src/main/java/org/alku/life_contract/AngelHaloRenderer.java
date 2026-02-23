package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Random;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AngelHaloRenderer {

    private static final Random random = new Random();
    private static final int PARTICLE_INTERVAL = 5;

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.level == null) return;

        Level world = mc.level;
        Player player = mc.player;
        if (player == null) return;

        if (world.getGameTime() % PARTICLE_INTERVAL != 0) return;

        String professionId = ClientDataStorage.getSelfProfessionId();
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isAngel() || !profession.hasHalo()) return;

        renderHaloParticles(world, player);
    }

    private static void renderHaloParticles(Level world, Player player) {
        double x = player.getX();
        double y = player.getY() + 2.2;
        double z = player.getZ();

        int particleCount = 8;
        double radius = 0.4;

        for (int i = 0; i < particleCount; i++) {
            double angle = (world.getGameTime() * 0.05 + i * (2 * Math.PI / particleCount));
            double px = x + Math.cos(angle) * radius;
            double pz = z + Math.sin(angle) * radius;

            world.addParticle(
                ParticleTypes.END_ROD,
                px, y, pz,
                0.0, 0.01, 0.0
            );
        }

        if (world.getGameTime() % 20 == 0) {
            world.addParticle(
                ParticleTypes.END_ROD,
                x + (random.nextDouble() - 0.5) * 0.3,
                y + 0.1,
                z + (random.nextDouble() - 0.5) * 0.3,
                0.0, 0.05, 0.0
            );
        }
    }
}
