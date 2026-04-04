package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
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
        
        UUID leaderUUID = getLeaderUUID(player);
        if (leaderUUID == null)
            leaderUUID = player.getUUID();

        ChatFormatting teamColor = getTeamColor(leaderUUID);

        MutableComponent styledName = Component.literal("").withStyle(teamColor);
        styledName = styledName.append(event.getDisplayname().copy());
        
        event.setDisplayname(styledName);
    }

    @SubscribeEvent
    public static void onTabListFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getDisplayName() == null)
            return;

        Player player = event.getEntity();
        
        UUID leaderUUID = getLeaderUUID(player);
        if (leaderUUID == null)
            leaderUUID = player.getUUID();

        ChatFormatting teamColor = getTeamColor(leaderUUID);
        String effectiveMod = getEffectiveContractMod(player);

        MutableComponent result = Component.literal("").withStyle(teamColor);
        result = result.append(event.getDisplayName().copy());

        if (effectiveMod != null && !effectiveMod.isEmpty()) {
            result.append(Component.literal(" [" + effectiveMod + "]").withStyle(ChatFormatting.GRAY));
        }

        event.setDisplayName(result);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        syncData(event.getEntity());
        
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            syncAllPlayersDataToNewPlayer(serverPlayer);
        }
    }
    
    private static void syncAllPlayersDataToNewPlayer(ServerPlayer newPlayer) {
        if (newPlayer.getServer() == null) return;
        
        for (ServerPlayer otherPlayer : newPlayer.getServer().getPlayerList().getPlayers()) {
            if (otherPlayer != newPlayer) {
                NetworkHandler.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> newPlayer),
                    new PacketSyncContract(otherPlayer)
                );
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
        if (originalData.hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)) {
            newData.putUUID(TeamOrganizerItem.TAG_LEADER_UUID, originalData.getUUID(TeamOrganizerItem.TAG_LEADER_UUID));
        }
        if (originalData.contains(TeamOrganizerItem.TAG_LEADER_NAME)) {
            newData.putString(TeamOrganizerItem.TAG_LEADER_NAME, originalData.getString(TeamOrganizerItem.TAG_LEADER_NAME));
        }
        if (originalData.contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
            newData.putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, originalData.getInt(TeamOrganizerItem.TAG_TEAM_NUMBER));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncData(event.getEntity());
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncData(event.getEntity());
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;
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

    public static ChatFormatting getTeamColor(UUID uuid) {
        int hash = Math.abs(uuid.hashCode());
        ChatFormatting[] colors = {
            ChatFormatting.RED, ChatFormatting.GOLD, ChatFormatting.YELLOW,
            ChatFormatting.GREEN, ChatFormatting.AQUA, ChatFormatting.BLUE,
            ChatFormatting.LIGHT_PURPLE, ChatFormatting.DARK_RED,
            ChatFormatting.DARK_GREEN, ChatFormatting.DARK_AQUA, ChatFormatting.DARK_BLUE,
            ChatFormatting.DARK_PURPLE
        };
        return colors[hash % colors.length];
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
        
        NetworkHandler.CHANNEL.send(
            PacketDistributor.ALL.noArg(),
            new PacketSyncContract(player)
        );
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
    public static void onTeamFriendlyFire(LivingAttackEvent event) {
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
    public static void onTeamFriendlyFireHurt(LivingHurtEvent event) {
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
    public static void onContractLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.damagesource.DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            
            if (attacker instanceof LivingEntity livingAttacker) {
                String attackerMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(livingAttacker.getType()).getNamespace();
                
                recordAttackerMod(player, livingAttacker);
                
                if (contractMod.equals(attackerMod)) {
                    event.setCanceled(true);
                }
            }
            
            if (source.getDirectEntity() instanceof LivingEntity directAttacker) {
                String directAttackerMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(directAttacker.getType()).getNamespace();
                
                recordAttackerMod(player, directAttacker);
                
                if (contractMod.equals(directAttackerMod)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContractLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.damagesource.DamageSource source = event.getSource();
            Entity attacker = source.getEntity();
            
            if (attacker instanceof LivingEntity livingAttacker) {
                String attackerMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(livingAttacker.getType()).getNamespace();
                
                if (contractMod.equals(attackerMod)) {
                    event.setCanceled(true);
                    return;
                }
            }
            
            if (source.getDirectEntity() instanceof LivingEntity directAttacker) {
                String directAttackerMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                    .getKey(directAttacker.getType()).getNamespace();
                
                if (contractMod.equals(directAttackerMod)) {
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onContractChangeTarget(net.minecraftforge.event.entity.living.LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof net.minecraft.world.entity.Mob mob && 
            event.getNewTarget() instanceof Player player) {
            
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            String mobMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
                .getKey(mob.getType()).getNamespace();
            
            if (contractMod.equals(mobMod)) {
                event.setNewTarget(null);
            }
        }
    }

    @SubscribeEvent
    public static void onContractMobEffectAdded(net.minecraftforge.event.entity.living.MobEffectEvent.Added event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof Player player) {
            String contractMod = getEffectiveContractMod(player);
            if (contractMod == null || contractMod.isEmpty()) return;
            
            net.minecraft.world.effect.MobEffectInstance effectInstance = event.getEffectInstance();
            if (effectInstance == null) return;
            
            net.minecraft.world.effect.MobEffect effect = effectInstance.getEffect();
            boolean isNegativeEffect = isNegativeEffect(effect);
            
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
        String attackerMod = net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES
            .getKey(attacker.getType()).getNamespace();
        LAST_ATTACKER_MOD.put(player.getUUID(), attackerMod);
        LAST_ATTACK_TIME.put(player.getUUID(), player.level().getGameTime());
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
