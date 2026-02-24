package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import org.alku.life_contract.follower.FollowerEvents;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class TeamIronGolemSystem {

    public static final String TAG_TEAM_NUMBER = "LifeContractTeamNumber";
    public static final String TAG_OWNER_UUID = "LifeContractOwnerUUID";
    public static final String TAG_IS_TEAM_GOLEM = "IsTeamIronGolem";
    
    private static final Map<UUID, Integer> GOLEM_TEAM_MAP = new HashMap<>();
    private static final Map<UUID, UUID> GOLEM_OWNER_MAP = new HashMap<>();
    
    static {
        try {
            SynchedEntityData.defineId(IronGolem.class, EntityDataSerializers.INT);
        } catch (Exception e) {
        }
    }

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        
        Entity entity = event.getEntity();
        if (entity instanceof IronGolem golem) {
            CompoundTag tag = golem.getPersistentData();
            
            if (tag.contains(TAG_IS_TEAM_GOLEM) && tag.getBoolean(TAG_IS_TEAM_GOLEM)) {
                int teamNumber = tag.getInt(TAG_TEAM_NUMBER);
                UUID ownerUUID = tag.hasUUID(TAG_OWNER_UUID) ? tag.getUUID(TAG_OWNER_UUID) : null;
                
                GOLEM_TEAM_MAP.put(golem.getUUID(), teamNumber);
                if (ownerUUID != null) {
                    GOLEM_OWNER_MAP.put(golem.getUUID(), ownerUUID);
                }
                
                golem.setPersistenceRequired();
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getSource().getEntity() instanceof IronGolem golem) {
            LivingEntity target = event.getEntity();
            
            if (isSameTeam(golem, target)) {
                event.setCanceled(true);
            }
        }
        
        if (event.getEntity() instanceof IronGolem golem && event.getSource().getEntity() instanceof LivingEntity attacker) {
            if (isSameTeam(golem, attacker)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onChangeTarget(LivingChangeTargetEvent event) {
        if (event.getEntity().level().isClientSide()) return;
        
        if (event.getEntity() instanceof IronGolem golem) {
            LivingEntity target = event.getNewTarget();
            if (target != null && isSameTeam(golem, target)) {
                event.setNewTarget(null);
            }
        }
    }

    public static boolean isSameTeam(IronGolem golem, LivingEntity target) {
        Integer golemTeam = GOLEM_TEAM_MAP.get(golem.getUUID());
        if (golemTeam == null) {
            golemTeam = golem.getPersistentData().getInt(TAG_TEAM_NUMBER);
            if (golemTeam > 0) {
                GOLEM_TEAM_MAP.put(golem.getUUID(), golemTeam);
            }
        }
        
        if (golemTeam == null || golemTeam <= 0) {
            return false;
        }
        
        if (target instanceof Player player) {
            return isPlayerInTeam(player, golemTeam);
        }
        
        if (target instanceof IronGolem targetGolem) {
            Integer targetTeam = GOLEM_TEAM_MAP.get(targetGolem.getUUID());
            if (targetTeam == null) {
                targetTeam = targetGolem.getPersistentData().getInt(TAG_TEAM_NUMBER);
            }
            return targetTeam != null && targetTeam.equals(golemTeam);
        }
        
        if (target instanceof net.minecraft.world.entity.Mob mob) {
            UUID mobOwner = FollowerEvents.getOwnerUUID(mob);
            if (mobOwner != null) {
                Player owner = golem.level().getPlayerByUUID(mobOwner);
                if (owner != null) {
                    return isPlayerInTeam(owner, golemTeam);
                }
            }
        }
        
        return false;
    }

    public static boolean isPlayerInTeam(Player player, int teamNumber) {
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        
        if (leaderUUID == null) {
            int playerTeamNumber = getPlayerTeamNumber(player);
            return playerTeamNumber == teamNumber;
        }
        
        Player leader = player.level().getPlayerByUUID(leaderUUID);
        if (leader != null) {
            int leaderTeamNumber = getPlayerTeamNumber(leader);
            return leaderTeamNumber == teamNumber;
        }
        
        return false;
    }

    public static int getPlayerTeamNumber(Player player) {
        CompoundTag data = player.getPersistentData();
        
        if (data.contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
            return data.getInt(TeamOrganizerItem.TAG_TEAM_NUMBER);
        }
        
        UUID leaderUUID = ContractEvents.getLeaderUUID(player);
        if (leaderUUID != null) {
            Player leader = player.level().getPlayerByUUID(leaderUUID);
            if (leader != null && leader.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
                return leader.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER);
            }
        }
        
        int teamNumber = Math.abs(player.getUUID().hashCode() % 9999) + 1;
        data.putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);
        
        return teamNumber;
    }

    public static void setGolemTeam(IronGolem golem, int teamNumber, UUID ownerUUID) {
        CompoundTag tag = golem.getPersistentData();
        tag.putBoolean(TAG_IS_TEAM_GOLEM, true);
        tag.putInt(TAG_TEAM_NUMBER, teamNumber);
        
        if (ownerUUID != null) {
            tag.putUUID(TAG_OWNER_UUID, ownerUUID);
            GOLEM_OWNER_MAP.put(golem.getUUID(), ownerUUID);
        }
        
        GOLEM_TEAM_MAP.put(golem.getUUID(), teamNumber);
        
        golem.setPersistenceRequired();
    }

    public static Integer getGolemTeam(IronGolem golem) {
        Integer teamNumber = GOLEM_TEAM_MAP.get(golem.getUUID());
        if (teamNumber == null) {
            teamNumber = golem.getPersistentData().getInt(TAG_TEAM_NUMBER);
            if (teamNumber > 0) {
                GOLEM_TEAM_MAP.put(golem.getUUID(), teamNumber);
            }
        }
        return teamNumber != null && teamNumber > 0 ? teamNumber : null;
    }

    public static UUID getGolemOwner(IronGolem golem) {
        UUID ownerUUID = GOLEM_OWNER_MAP.get(golem.getUUID());
        if (ownerUUID == null && golem.getPersistentData().hasUUID(TAG_OWNER_UUID)) {
            ownerUUID = golem.getPersistentData().getUUID(TAG_OWNER_UUID);
            GOLEM_OWNER_MAP.put(golem.getUUID(), ownerUUID);
        }
        return ownerUUID;
    }

    public static boolean isTeamGolem(IronGolem golem) {
        return golem.getPersistentData().getBoolean(TAG_IS_TEAM_GOLEM);
    }

    public static void removeGolemFromCache(UUID golemUUID) {
        GOLEM_TEAM_MAP.remove(golemUUID);
        GOLEM_OWNER_MAP.remove(golemUUID);
    }

    public static void applyPushResistance(IronGolem golem) {
        if (!isTeamGolem(golem)) return;
        
        golem.setDeltaMovement(golem.getDeltaMovement().multiply(1.0, 1.0, 1.0));
    }
}
