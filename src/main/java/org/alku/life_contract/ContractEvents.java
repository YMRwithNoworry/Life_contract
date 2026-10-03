package org.alku.life_contract;
import net.neoforged.fml.common.EventBusSubscriber;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import org.alku.life_contract.border.BorderRespawnHandler;
import org.alku.life_contract.events.GameEventManager;
import org.alku.life_contract.follower.FollowerEvents;

import java.util.*;

@EventBusSubscriber(modid = Life_contract.MODID)
public class ContractEvents {

    private static final Random RANDOM = new Random();
    private static final Map<UUID, String> LAST_ATTACKER_MOD = new HashMap<>();
    private static final Map<UUID, Long> LAST_ATTACK_TIME = new HashMap<>();
    private static final long ATTACK_EXPIRE_TICKS = 100;

    @SubscribeEvent
    public static void onPlayerNameFormat(PlayerEvent.NameFormat event) {
        if (event.getDisplayname() == null)
            return;

        Player player = event.getEntity();
        
        int teamColor = getTeamColor(player);

        MutableComponent styledName = Component.literal("").withStyle(style -> style.withColor(teamColor));
        styledName = styledName.append(event.getDisplayname().copy());
        
        event.setDisplayname(styledName);
    }

    @SubscribeEvent
    public static void onTabListFormat(PlayerEvent.TabListNameFormat event) {
        Player player = event.getEntity();
        
        int teamColor = getTeamColor(player);
        String effectiveMod = getEffectiveContractMod(player);

        MutableComponent result = Component.literal(player.getGameProfile().getName())
                .withStyle(style -> style.withColor(teamColor));

        if (effectiveMod != null && !effectiveMod.isEmpty()) {
            result.append(Component.literal(" [" + effectiveMod + "]").withStyle(ChatFormatting.GRAY));
        }

        event.setDisplayName(result);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide) {
            SoulContractItem.applyStoredHealthSacrifice(event.getEntity());
        }
        syncData(event.getEntity());
        
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            GameEventManager.handleLateJoinPlayer(serverPlayer);
            syncAllPlayersDataToNewPlayer(serverPlayer);
            if (serverPlayer.gameMode.getGameModeForPlayer() == GameType.SPECTATOR && !GameEventManager.isPlayerPartOfGame(serverPlayer.getUUID())) {
                sendLateJoinChoices(serverPlayer);
            }
        }
    }

    private static void sendLateJoinChoices(ServerPlayer player) {
        if (!GameEventManager.isGameActive()) {
            return;
        }

        player.sendSystemMessage(Component.literal("§e[生灵契约] §f游戏已开始，你已默认进入旁观者模式。"));
        player.sendSystemMessage(Component.literal("")
            .append(Component.literal("§a[加入人数最少的队伍]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/contract game join_low_team"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击加入当前人数最少的队伍")))))
            .append(Component.literal("  "))
            .append(Component.literal("§7[保持旁观]")
                .setStyle(Style.EMPTY
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/contract game stay_spectator"))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("点击保持旁观状态"))))));
    }
    
    private static void syncAllPlayersDataToNewPlayer(ServerPlayer newPlayer) {
        if (newPlayer.getServer() == null) return;
        
        for (ServerPlayer otherPlayer : newPlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != newPlayer) {
                NetworkHandler.sendToPlayer(newPlayer, new PacketSyncContract(otherPlayer));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        CompoundTag originalData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();
        
        if (originalData.contains(SoulContractItem.TAG_CONTRACT_MOD)) {
            newData.putString(SoulContractItem.TAG_CONTRACT_MOD, originalData.getString(SoulContractItem.TAG_CONTRACT_MOD));
        }
        if (originalData.contains(SoulContractItem.TAG_HEALTH_SACRIFICE)) {
            newData.putDouble(SoulContractItem.TAG_HEALTH_SACRIFICE,
                    originalData.getDouble(SoulContractItem.TAG_HEALTH_SACRIFICE));
            SoulContractItem.applyStoredHealthSacrifice(newPlayer);
        }
        if (originalData.hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)) {
            newData.putUUID(TeamOrganizerItem.TAG_LEADER_UUID, originalData.getUUID(TeamOrganizerItem.TAG_LEADER_UUID));
        }
        if (originalData.contains(TeamOrganizerItem.TAG_LEADER_NAME)) {
            newData.putString(TeamOrganizerItem.TAG_LEADER_NAME, originalData.getString(TeamOrganizerItem.TAG_LEADER_NAME));
        }
        if (originalData.contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
            newData.putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, originalData.getInt(TeamOrganizerItem.TAG_TEAM_NUMBER));
        }
        if (originalData.contains(org.alku.life_contract.follower.WandEggStorage.DATA_KEY)) {
            newData.put(org.alku.life_contract.follower.WandEggStorage.DATA_KEY,
                    originalData.getCompound(org.alku.life_contract.follower.WandEggStorage.DATA_KEY).copy());
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide) {
            SoulContractItem.applyStoredHealthSacrifice(event.getEntity());
            if (!event.isEndConquered() && event.getEntity() instanceof ServerPlayer player) {
                BorderRespawnHandler.ensureInsideBorder(player);
            }
        }
        syncData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncData(event.getEntity());
    }

    public static UUID getLeaderUUID(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)) {
            return data.getUUID(TeamOrganizerItem.TAG_LEADER_UUID);
        }
        return null;
    }

    public static boolean isSameTeam(Player player1, Player player2) {
        UUID leader1 = getLeaderUUID(player1);
        UUID leader2 = getLeaderUUID(player2);
        
        if (leader1 == null && leader2 == null) {
            return player1.getUUID().equals(player2.getUUID());
        }
        
        if (leader1 == null) {
            leader1 = player1.getUUID();
        }
        if (leader2 == null) {
            leader2 = player2.getUUID();
        }
        
        return leader1.equals(leader2);
    }

    private static final int[] TEAM_COLORS = {
            0xF05A5A, 0x55C97B, 0x5C8FF2, 0xF2B84B, 0xB66DE2, 0x43C7C3, 0xED72AD, 0x9AC84B,
            0xE77A3D, 0x6C7CE8, 0xD0B43F, 0x43A6D8, 0xD95F83, 0x70B957, 0xA878E0, 0xDB8D46,
            0x4EB59A, 0xE066D0, 0x7E9B4C, 0xD66550, 0x5A91C8, 0xC59441, 0x8A72CB, 0x4DAF68,
            0xC96B9A, 0x7690DE, 0xB5A64A, 0x46A9A5, 0xD07860, 0x809D55, 0xA46FB4, 0x4F9CBE
    };

    public static int getTeamColor(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
            int teamNumber = data.getInt(TeamOrganizerItem.TAG_TEAM_NUMBER);
            if (teamNumber > 0) return TEAM_COLORS[(teamNumber - 1) % TEAM_COLORS.length];
        }

        UUID teamId = getLeaderUUID(player);
        if (teamId == null) teamId = player.getUUID();
        return TEAM_COLORS[Math.floorMod(teamId.hashCode(), TEAM_COLORS.length)];
    }

    public static String getEffectiveContractMod(Player player) {
        CompoundTag data = player.getPersistentData();
        
        String ownMod = data.getString(SoulContractItem.TAG_CONTRACT_MOD);
        if (ownMod != null && !ownMod.isEmpty()) {
            return ownMod;
        }
        
        UUID leaderUUID = getLeaderUUID(player);
        if (leaderUUID != null && !leaderUUID.equals(player.getUUID())) {
            Player leader = player.level().getPlayerByUUID(leaderUUID);
            if (leader != null) {
                String leaderMod = leader.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
                if (leaderMod != null && !leaderMod.isEmpty()) {
                    return leaderMod;
                }
            }
        }
        
        return null;
    }

    public static void syncData(Player player) {
        if (player.level().isClientSide)
            return;
        
        NetworkHandler.sendToAllPlayers(new PacketSyncContract(player));
    }

    /** 契约模组 -> 持有该契约的玩家，按服务器 tick 缓存。 */
    private static final java.util.Map<String, ServerPlayer> CONTRACT_MOD_OWNERS = new java.util.HashMap<>();
    private static long contractModOwnersTick = Long.MIN_VALUE;

    /**
     * 查找持有指定契约模组的玩家。
     * <p>
     * 生物生成与伤害结算都会调用它，原实现每次都遍历玩家列表并逐个解析契约模组，
     * 在刷怪密集（感染生物、刷怪塔）时会占到大量服务端耗时；这里改成每个 tick 最多重建一次。
     */
    public static ServerPlayer findPlayerForContractMod(net.minecraft.server.MinecraftServer server, String modId) {
        if (server == null || modId == null || modId.isEmpty()) {
            return null;
        }

        long tick = server.getTickCount();
        if (tick != contractModOwnersTick) {
            contractModOwnersTick = tick;
            CONTRACT_MOD_OWNERS.clear();
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                String contractMod = getEffectiveContractMod(player);
                if (contractMod != null && !contractMod.isEmpty()) {
                    CONTRACT_MOD_OWNERS.putIfAbsent(contractMod, player);
                }
            }
        }

        ServerPlayer owner = CONTRACT_MOD_OWNERS.get(modId);
        // 缓存里的玩家可能已离线，做一次校验
        return owner != null && !owner.hasDisconnected() ? owner : null;
    }

    public static void propagateContractToTeam(Player player, String modId) {
        if (player.level().isClientSide)
            return;
        
        UUID leaderUUID = getLeaderUUID(player);
        if (leaderUUID == null) {
            return;
        }
        
        for (ServerPlayer otherPlayer : player.getServer().getPlayerList().getPlayers()) {
            UUID otherLeader = getLeaderUUID(otherPlayer);
            if (leaderUUID.equals(otherLeader)) {
                otherPlayer.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, modId);
                syncData(otherPlayer);
            }
        }
    }

    @SubscribeEvent
    public static void onTeamFriendlyFire(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (!(event.getEntity() instanceof Player targetPlayer)) return;
        
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        
        if (attacker instanceof Player attackerPlayer) {
            if (ContractEvents.isSameTeam(attackerPlayer, targetPlayer)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onTeamFriendlyFireHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (!(event.getEntity() instanceof Player targetPlayer)) return;
        
        net.minecraft.world.damagesource.DamageSource source = event.getSource();
        Entity attacker = source.getEntity();
        
        if (attacker instanceof Player attackerPlayer) {
            if (ContractEvents.isSameTeam(attackerPlayer, targetPlayer)) {
                event.setNewDamage(0);
            }
        }
    }

    @SubscribeEvent
    public static void onContractLivingAttack(LivingIncomingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.damagesource.DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            
            if (attacker instanceof LivingEntity livingAttacker) {
                recordAttackerMod(player, livingAttacker);
                
                if (isContractFactionAlly(player, livingAttacker, contractMod)) {
                    event.setCanceled(true);
                }
            }
            
            if (source.getDirectEntity() instanceof LivingEntity directAttacker) {
                recordAttackerMod(player, directAttacker);
                
                if (isContractFactionAlly(player, directAttacker, contractMod)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContractLivingHurt(LivingDamageEvent.Pre event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.damagesource.DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            
            if (attacker instanceof LivingEntity livingAttacker) {
                if (isContractFactionAlly(player, livingAttacker, contractMod)) {
                    event.setNewDamage(0);
                    return;
                }
            }
            
            if (source.getDirectEntity() instanceof LivingEntity directAttacker) {
                if (isContractFactionAlly(player, directAttacker, contractMod)) {
                    event.setNewDamage(0);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContractChangeTarget(net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob && 
            event.getNewAboutToBeSetTarget() instanceof Player player) {
            
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            if (isContractFactionAlly(player, mob, contractMod)) {
                event.setNewAboutToBeSetTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onInitialDebuffApplicable(
            net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable event) {
        if (event.getEntity().level().isClientSide()
                || !(event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player)
                || !org.alku.life_contract.events.GameEventManager.hasInitialDebuffProtection(player.getUUID())
                || event.getEffectInstance().getEffect().value().getCategory()
                != net.minecraft.world.effect.MobEffectCategory.HARMFUL) {
            return;
        }
        event.setResult(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Applicable.Result.DO_NOT_APPLY);
    }

    @SubscribeEvent
    public static void onContractMobEffectAdded(net.neoforged.neoforge.event.entity.living.MobEffectEvent.Added event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.effect.MobEffectInstance effectInstance = event.getEffectInstance();
            if (effectInstance == null) return;
            
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect = effectInstance.getEffect();
            boolean isNegativeEffect = isNegativeEffect(effect.value());
            
            if (!isNegativeEffect) return;
            
            String lastAttackerMod = LAST_ATTACKER_MOD.get(player.getUUID());
            Long lastAttackTime = LAST_ATTACK_TIME.get(player.getUUID());
            long currentTime = player.level().getGameTime();
            
            if (lastAttackerMod != null && lastAttackTime != null && 
                currentTime - lastAttackTime < ATTACK_EXPIRE_TICKS &&
                contractMod.equals(lastAttackerMod)) {
                player.removeEffect(effect);
            }
        }
    }

    private static void recordAttackerMod(Player player, LivingEntity attacker) {
        if (attacker instanceof net.minecraft.world.entity.Mob mob
                && FollowerEvents.getOwnerUUID(mob) != null
                && !FollowerEvents.isAlliedWithPlayer(player, mob)) {
            LAST_ATTACKER_MOD.remove(player.getUUID());
        } else {
            net.minecraft.resources.ResourceLocation attackerType =
                    net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(attacker.getType());
            if (attackerType != null) {
                LAST_ATTACKER_MOD.put(player.getUUID(), attackerType.getNamespace());
            }
        }
        LAST_ATTACK_TIME.put(player.getUUID(), player.level().getGameTime());
    }

    private static boolean isContractFactionAlly(Player player, LivingEntity entity, String contractMod) {
        if (entity instanceof net.minecraft.world.entity.Mob mob && FollowerEvents.getOwnerUUID(mob) != null) {
            return FollowerEvents.isAlliedWithPlayer(player, mob);
        }

        net.minecraft.resources.ResourceLocation entityType =
                net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType());
        return entityType != null && contractMod.equals(entityType.getNamespace());
    }

    private static boolean isNegativeEffect(net.minecraft.world.effect.MobEffect effect) {
        return effect == net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN
            || effect == net.minecraft.world.effect.MobEffects.DIG_SLOWDOWN
            || effect == net.minecraft.world.effect.MobEffects.HARM
            || effect == net.minecraft.world.effect.MobEffects.CONFUSION
            || effect == net.minecraft.world.effect.MobEffects.BLINDNESS
            || effect == net.minecraft.world.effect.MobEffects.HUNGER
            || effect == net.minecraft.world.effect.MobEffects.WEAKNESS
            || effect == net.minecraft.world.effect.MobEffects.POISON
            || effect == net.minecraft.world.effect.MobEffects.WITHER
            || effect == net.minecraft.world.effect.MobEffects.LEVITATION
            || effect == net.minecraft.world.effect.MobEffects.UNLUCK
            || effect == net.minecraft.world.effect.MobEffects.DARKNESS;
    }
}
