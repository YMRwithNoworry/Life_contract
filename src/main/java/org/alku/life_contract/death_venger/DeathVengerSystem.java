package org.alku.life_contract.death_venger;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DeathVengerSystem {

    private static final String TAG_MARKED_TARGET_UUID = "DeathVengerMarkedTargetUUID";
    private static final String TAG_MARKED_TARGET_NAME = "DeathVengerMarkedTargetName";
    private static final Map<UUID, UUID> playerMarkedTargets = new HashMap<>();
    private static final Map<UUID, String> playerMarkedTargetNames = new HashMap<>();

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (event.getLevel().isClientSide) return;
        if (!(event.getEntity() instanceof ServerPlayer player)) return;

        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasMarkTargetAbility()) return;

        Entity target = event.getTarget();
        if (!(target instanceof LivingEntity livingTarget)) return;
        if (target instanceof Player && ContractEvents.isSameTeam(player, (Player) target)) {
            player.displayClientMessage(
                Component.literal("§c[死仇者] §f不能标记队友！"),
                true
            );
            return;
        }

        markTarget(player, livingTarget);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;

        LivingEntity deadEntity = event.getEntity();
        UUID deadUUID = deadEntity.getUUID();

        for (ServerPlayer player : event.getEntity().level().getServer().getPlayerList().getPlayers()) {
            String professionId = ContractEvents.getEffectiveProfessionId(player);
            if (professionId == null || professionId.isEmpty()) continue;

            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.hasMarkTargetAbility()) continue;

            UUID markedUUID = playerMarkedTargets.get(player.getUUID());
            if (markedUUID != null && markedUUID.equals(deadUUID)) {
                onMarkedTargetDeath(player, deadEntity);
            }
        }
    }

    private static void markTarget(ServerPlayer player, LivingEntity target) {
        UUID previousTargetUUID = playerMarkedTargets.get(player.getUUID());
        if (previousTargetUUID != null && player.level() instanceof ServerLevel serverLevel) {
            Entity previousTarget = serverLevel.getEntity(previousTargetUUID);
            if (previousTarget instanceof LivingEntity livingPrev) {
                livingPrev.removeEffect(MobEffects.GLOWING);
            }
        }

        playerMarkedTargets.put(player.getUUID(), target.getUUID());
        String targetName = target.hasCustomName() ? target.getCustomName().getString() : 
                           target.getName().getString();
        playerMarkedTargetNames.put(player.getUUID(), targetName);

        CompoundTag data = player.getPersistentData();
        data.putUUID(TAG_MARKED_TARGET_UUID, target.getUUID());
        data.putString(TAG_MARKED_TARGET_NAME, targetName);

        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));

        syncMarkedTargetToClient(player, target.getUUID(), targetName, 
                                target.blockPosition().getX(), 
                                target.blockPosition().getY(), 
                                target.blockPosition().getZ());

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§4§l[死仇者] §f§l目标已标记！"));
        player.sendSystemMessage(Component.literal("§f  目标: §c" + targetName));
        player.sendSystemMessage(Component.literal("§7  目标将永久发光，坐标已显示"));
        player.sendSystemMessage(Component.literal("§7  目标死亡时将自动转移标记"));
        player.sendSystemMessage(Component.literal(""));

        playMarkEffects(player, target);
    }

    private static void onMarkedTargetDeath(ServerPlayer player, LivingEntity deadTarget) {
        String targetName = playerMarkedTargetNames.getOrDefault(player.getUUID(), "未知目标");

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§4§l[死仇者] §f§l目标已死亡！"));
        player.sendSystemMessage(Component.literal("§f  死亡目标: §c" + targetName));
        player.sendSystemMessage(Component.literal("§7  请选择新的目标进行标记"));
        player.sendSystemMessage(Component.literal(""));

        clearMarkedTarget(player);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.SOUL,
                deadTarget.getX(), deadTarget.getY() + 1, deadTarget.getZ(),
                30, 0.5, 0.5, 0.5, 0.1
            );
        }
    }

    public static void clearMarkedTarget(ServerPlayer player) {
        UUID targetUUID = playerMarkedTargets.remove(player.getUUID());
        playerMarkedTargetNames.remove(player.getUUID());

        CompoundTag data = player.getPersistentData();
        data.remove(TAG_MARKED_TARGET_UUID);
        data.remove(TAG_MARKED_TARGET_NAME);

        syncMarkedTargetToClient(player, null, "", 0, 0, 0);
    }

    public static void loadMarkedTarget(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        if (!data.hasUUID(TAG_MARKED_TARGET_UUID)) return;

        UUID targetUUID = data.getUUID(TAG_MARKED_TARGET_UUID);
        String targetName = data.getString(TAG_MARKED_TARGET_NAME);

        playerMarkedTargets.put(player.getUUID(), targetUUID);
        playerMarkedTargetNames.put(player.getUUID(), targetName);

        if (player.level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUUID);
            if (target instanceof LivingEntity livingTarget) {
                if (!livingTarget.hasEffect(MobEffects.GLOWING)) {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
                }
                syncMarkedTargetToClient(player, targetUUID, targetName,
                                        livingTarget.blockPosition().getX(),
                                        livingTarget.blockPosition().getY(),
                                        livingTarget.blockPosition().getZ());
            } else {
                syncMarkedTargetToClient(player, targetUUID, targetName, 0, 0, 0);
            }
        }
    }

    public static void tickMarkedTarget(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasMarkTargetAbility()) return;

        UUID targetUUID = playerMarkedTargets.get(player.getUUID());
        if (targetUUID == null) return;

        if (player.level() instanceof ServerLevel serverLevel) {
            Entity target = serverLevel.getEntity(targetUUID);
            if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                if (!livingTarget.hasEffect(MobEffects.GLOWING)) {
                    livingTarget.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE, 0, false, false));
                }
                
                syncMarkedTargetToClient(player, targetUUID, 
                                        playerMarkedTargetNames.getOrDefault(player.getUUID(), "未知"),
                                        livingTarget.blockPosition().getX(),
                                        livingTarget.blockPosition().getY(),
                                        livingTarget.blockPosition().getZ());
            }
        }
    }

    private static void syncMarkedTargetToClient(ServerPlayer player, UUID targetUUID, String targetName, 
                                                  int x, int y, int z) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncMarkedTarget(targetUUID, targetName, x, y, z));
    }

    private static void playMarkEffects(ServerPlayer player, LivingEntity target) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.DAMAGE_INDICATOR,
                target.getX(), target.getY() + 1, target.getZ(),
                20, 0.5, 0.5, 0.5, 0.1
            );
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANTED_HIT,
                target.getX(), target.getY() + 1, target.getZ(),
                15, 0.3, 0.3, 0.3, 0.05
            );
        }

        player.level().playSound(null, target.blockPosition(), 
            net.minecraft.sounds.SoundEvents.BELL_RESONATE, 
            net.minecraft.sounds.SoundSource.PLAYERS, 1.0F, 0.5F);
        player.level().playSound(null, target.blockPosition(), 
            net.minecraft.sounds.SoundEvents.WITHER_SPAWN, 
            net.minecraft.sounds.SoundSource.PLAYERS, 0.3F, 1.5F);
    }

    public static UUID getMarkedTargetUUID(Player player) {
        return playerMarkedTargets.get(player.getUUID());
    }

    public static String getMarkedTargetName(Player player) {
        return playerMarkedTargetNames.get(player.getUUID());
    }
}
