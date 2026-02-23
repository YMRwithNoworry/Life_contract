package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.List;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class HealerSystem {

    private static final String TAG_HEALER_COOLDOWN = "HealerActiveCooldown";

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickHealerPassive(player, tickCount);
            tickHealerCooldown(player);
        }
    }

    private static void tickHealerPassive(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHealer()) return;

        if (tickCount % 20 != 0) return;

        float radius = profession.getHealerPassiveRadius();
        float healAmount = profession.getHealerPassiveHealAmount();

        AABB area = new AABB(
                player.getX() - radius, player.getY() - radius, player.getZ() - radius,
                player.getX() + radius, player.getY() + radius, player.getZ() + radius
        );

        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, area);

        for (LivingEntity entity : nearbyEntities) {
            if (entity instanceof Player targetPlayer) {
                if (isAlly(player, targetPlayer)) {
                    healEntity(targetPlayer, healAmount);
                    spawnHealParticles((ServerLevel) player.level(), targetPlayer);
                }
            }
        }
    }

    private static void tickHealerCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_HEALER_COOLDOWN);
        if (cooldown > 0) {
            data.putInt(TAG_HEALER_COOLDOWN, cooldown - 1);
        }
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        player.getPersistentData().remove(TAG_HEALER_COOLDOWN);
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncHealerCooldown(0));
    }

    public static boolean canUseActiveHeal(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return false;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHealer()) return false;

        return player.getPersistentData().getInt(TAG_HEALER_COOLDOWN) <= 0;
    }

    public static int getCooldownRemaining(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_HEALER_COOLDOWN);
    }

    public static int getCooldownMax(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return 400;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHealer()) return 400;

        return profession.getHealerActiveCooldown();
    }

    public static void useActiveHeal(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isHealer()) return;

        if (!canUseActiveHeal(player)) {
            int remaining = getCooldownRemaining(player);
            int seconds = remaining / 20;
            player.sendSystemMessage(Component.literal("§c[医者] 技能冷却中，还需 " + seconds + " 秒"));
            return;
        }

        float healAmount = profession.getHealerActiveHealAmount();
        float radius = profession.getHealerPassiveRadius();

        Player target = findNearestAlly(player, radius);

        if (target == null) {
            target = player;
        }

        healEntity(target, healAmount);
        spawnActiveHealParticles((ServerLevel) player.level(), target);
        player.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.2f);

        int cooldown = profession.getHealerActiveCooldown();
        player.getPersistentData().putInt(TAG_HEALER_COOLDOWN, cooldown);

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new PacketSyncHealerCooldown(cooldown));

        if (target == player) {
            player.sendSystemMessage(Component.literal("§a[医者] 你为自己恢复了 " + (int)healAmount + " 点生命值！"));
        } else {
            player.sendSystemMessage(Component.literal("§a[医者] 你为 " + target.getName().getString() + " 恢复了 " + (int)healAmount + " 点生命值！"));
            if (target instanceof ServerPlayer serverTarget) {
                serverTarget.sendSystemMessage(Component.literal("§a[医者] " + player.getName().getString() + " 为你恢复了 " + (int)healAmount + " 点生命值！"));
            }
        }
    }

    private static Player findNearestAlly(ServerPlayer healer, float radius) {
        AABB area = new AABB(
                healer.getX() - radius, healer.getY() - radius, healer.getZ() - radius,
                healer.getX() + radius, healer.getY() + radius, healer.getZ() + radius
        );

        List<Player> nearbyPlayers = healer.level().getEntitiesOfClass(Player.class, area);

        Player nearest = null;
        double nearestDist = Double.MAX_VALUE;

        for (Player player : nearbyPlayers) {
            if (player == healer) continue;
            if (!isAlly(healer, player)) continue;

            double dist = healer.distanceToSqr(player);
            if (dist < nearestDist) {
                nearestDist = dist;
                nearest = player;
            }
        }

        return nearest;
    }

    private static boolean isAlly(Player healer, Player target) {
        if (healer == target) return true;

        return ContractEvents.isSameTeam(healer, target);
    }

    private static void healEntity(LivingEntity entity, float amount) {
        float currentHealth = entity.getHealth();
        float maxHealth = entity.getMaxHealth();
        float newHealth = Math.min(currentHealth + amount, maxHealth);
        entity.setHealth(newHealth);
    }

    private static void spawnHealParticles(ServerLevel level, LivingEntity entity) {
        level.sendParticles(
                ParticleTypes.HEART,
                entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ(),
                5, 0.3, 0.3, 0.3, 0.05
        );

        level.sendParticles(
                ParticleTypes.COMPOSTER,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                10, 0.3, 0.3, 0.3, 0.02
        );
    }

    private static void spawnActiveHealParticles(ServerLevel level, LivingEntity entity) {
        level.sendParticles(
                ParticleTypes.TOTEM_OF_UNDYING,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
        );

        level.sendParticles(
                ParticleTypes.HEART,
                entity.getX(), entity.getY() + entity.getBbHeight() + 0.5, entity.getZ(),
                10, 0.5, 0.3, 0.5, 0.1
        );

        level.sendParticles(
                ParticleTypes.END_ROD,
                entity.getX(), entity.getY() + entity.getBbHeight() / 2, entity.getZ(),
                15, 0.3, 0.5, 0.3, 0.05
        );
    }
}
