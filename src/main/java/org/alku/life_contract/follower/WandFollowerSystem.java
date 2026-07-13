package org.alku.life_contract.follower;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.life_contract.Life_contract;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class WandFollowerSystem {
    public static final String TAG_BOUND_FOLLOWER_UUID = "LifeContractWandFollowerUUID";
    private static final String TAG_WAND_OWNER_UUID = "LifeContractWandOwnerUUID";

    private static final Map<UUID, Goal> ACTIVE_FOLLOW_GOALS = new HashMap<>();
    private static final Map<UUID, UUID> ACTIVE_OWNER_FOLLOWERS = new HashMap<>();

    private WandFollowerSystem() {
    }

    public static void bind(ServerPlayer player, Mob mob) {
        releaseCurrent(player);

        UUID previousOwnerUUID = getWandOwnerUUID(mob);
        removeFollowGoal(mob);
        if (previousOwnerUUID != null && !previousOwnerUUID.equals(player.getUUID())) {
            clearPreviousOwnerBinding(player.getServer(), previousOwnerUUID, mob.getUUID());
        }

        player.getPersistentData().putUUID(TAG_BOUND_FOLLOWER_UUID, mob.getUUID());
        mob.getPersistentData().putUUID(TAG_WAND_OWNER_UUID, player.getUUID());
        mob.setPersistenceRequired();
        installFollowGoal(mob, player.getUUID());
    }

    public static boolean isBoundFollower(Player player, Mob mob) {
        CompoundTag data = player.getPersistentData();
        return data.hasUUID(TAG_BOUND_FOLLOWER_UUID)
                && data.getUUID(TAG_BOUND_FOLLOWER_UUID).equals(mob.getUUID())
                && mob.getPersistentData().hasUUID(TAG_WAND_OWNER_UUID)
                && mob.getPersistentData().getUUID(TAG_WAND_OWNER_UUID).equals(player.getUUID());
    }

    public static boolean hasBoundFollower(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)
                || !player.getPersistentData().hasUUID(TAG_BOUND_FOLLOWER_UUID)) {
            return false;
        }
        UUID followerUUID = player.getPersistentData().getUUID(TAG_BOUND_FOLLOWER_UUID);
        Entity follower = findEntity(serverPlayer.getServer(), followerUUID);
        return follower instanceof Mob mob
                && mob.isAlive()
                && isBoundFollower(player, mob)
                && followerUUID.equals(ACTIVE_OWNER_FOLLOWERS.get(player.getUUID()));
    }

    public static boolean releaseCurrent(ServerPlayer player) {
        CompoundTag playerData = player.getPersistentData();
        if (!playerData.hasUUID(TAG_BOUND_FOLLOWER_UUID)) {
            return false;
        }

        UUID followerUUID = playerData.getUUID(TAG_BOUND_FOLLOWER_UUID);
        Entity entity = findEntity(player.getServer(), followerUUID);
        if (entity instanceof Mob oldFollower
                && player.getUUID().equals(getWandOwnerUUID(oldFollower))) {
            removeFollowGoal(oldFollower);
            oldFollower.getPersistentData().remove(TAG_WAND_OWNER_UUID);
        }
        ACTIVE_OWNER_FOLLOWERS.remove(player.getUUID());
        playerData.remove(TAG_BOUND_FOLLOWER_UUID);
        return true;
    }

    private static UUID getWandOwnerUUID(Mob mob) {
        CompoundTag mobData = mob.getPersistentData();
        return mobData.hasUUID(TAG_WAND_OWNER_UUID) ? mobData.getUUID(TAG_WAND_OWNER_UUID) : null;
    }

    private static void clearPreviousOwnerBinding(MinecraftServer server, UUID ownerUUID, UUID followerUUID) {
        ACTIVE_OWNER_FOLLOWERS.remove(ownerUUID, followerUUID);
        ServerPlayer previousOwner = server.getPlayerList().getPlayer(ownerUUID);
        if (previousOwner == null) {
            return;
        }

        CompoundTag previousOwnerData = previousOwner.getPersistentData();
        if (previousOwnerData.hasUUID(TAG_BOUND_FOLLOWER_UUID)
                && previousOwnerData.getUUID(TAG_BOUND_FOLLOWER_UUID).equals(followerUUID)) {
            previousOwnerData.remove(TAG_BOUND_FOLLOWER_UUID);
            previousOwner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[跟随之杖] 你的法杖随从已由另一名队友接管，额外饥饿消耗已停止。"));
        }
    }

    private static Entity findEntity(MinecraftServer server, UUID entityUUID) {
        for (ServerLevel level : server.getAllLevels()) {
            Entity entity = level.getEntity(entityUUID);
            if (entity != null) {
                return entity;
            }
        }
        return null;
    }

    private static void installFollowGoal(Mob mob, UUID ownerUUID) {
        removeFollowGoal(mob);
        FollowOwnerGoal followGoal = new FollowOwnerGoal(mob, ownerUUID, 1.35D, 3.0F, 1.5F);
        mob.goalSelector.addGoal(1, followGoal);
        ACTIVE_FOLLOW_GOALS.put(mob.getUUID(), followGoal);
        ACTIVE_OWNER_FOLLOWERS.put(ownerUUID, mob.getUUID());
    }

    private static void removeFollowGoal(Mob mob) {
        Goal goal = ACTIVE_FOLLOW_GOALS.remove(mob.getUUID());
        if (mob.getPersistentData().hasUUID(TAG_WAND_OWNER_UUID)) {
            UUID ownerUUID = mob.getPersistentData().getUUID(TAG_WAND_OWNER_UUID);
            ACTIVE_OWNER_FOLLOWERS.remove(ownerUUID, mob.getUUID());
        }
        if (goal != null) {
            mob.goalSelector.removeGoal(goal);
            mob.getNavigation().stop();
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() || !(event.getEntity() instanceof Mob mob)
                || !mob.getPersistentData().hasUUID(TAG_WAND_OWNER_UUID)) {
            return;
        }

        UUID ownerUUID = mob.getPersistentData().getUUID(TAG_WAND_OWNER_UUID);
        ServerPlayer owner = event.getLevel().getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner == null) {
            return;
        }

        if (isBoundFollower(owner, mob)) {
            installFollowGoal(mob, ownerUUID);
        } else {
            mob.getPersistentData().remove(TAG_WAND_OWNER_UUID);
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Mob mob) {
            removeFollowGoal(mob);
        }
    }

    @SubscribeEvent
    public static void onFollowerDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide() || !(event.getEntity() instanceof Mob mob)
                || !mob.getPersistentData().hasUUID(TAG_WAND_OWNER_UUID)) {
            return;
        }

        UUID ownerUUID = mob.getPersistentData().getUUID(TAG_WAND_OWNER_UUID);
        ServerPlayer owner = mob.getServer() == null ? null : mob.getServer().getPlayerList().getPlayer(ownerUUID);
        if (owner != null && isBoundFollower(owner, mob)) {
            owner.getPersistentData().remove(TAG_BOUND_FOLLOWER_UUID);
            owner.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                    "§e[跟随之杖] 你的法杖随从已死亡，额外饥饿消耗已停止。"));
        }
        removeFollowGoal(mob);
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) {
            return;
        }

        CompoundTag oldData = event.getOriginal().getPersistentData();
        if (oldData.hasUUID(TAG_BOUND_FOLLOWER_UUID)) {
            event.getEntity().getPersistentData().putUUID(
                    TAG_BOUND_FOLLOWER_UUID, oldData.getUUID(TAG_BOUND_FOLLOWER_UUID));
        }
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !player.getPersistentData().hasUUID(TAG_BOUND_FOLLOWER_UUID)) {
            return;
        }

        UUID followerUUID = player.getPersistentData().getUUID(TAG_BOUND_FOLLOWER_UUID);
        Entity entity = findEntity(player.getServer(), followerUUID);
        if (entity instanceof Mob mob
                && mob.getPersistentData().hasUUID(TAG_WAND_OWNER_UUID)
                && mob.getPersistentData().getUUID(TAG_WAND_OWNER_UUID).equals(player.getUUID())) {
            installFollowGoal(mob, player.getUUID());
        } else if (entity != null) {
            player.getPersistentData().remove(TAG_BOUND_FOLLOWER_UUID);
            ACTIVE_OWNER_FOLLOWERS.remove(player.getUUID(), followerUUID);
        }
    }
}
