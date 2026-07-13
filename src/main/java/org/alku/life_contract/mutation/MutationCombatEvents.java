package org.alku.life_contract.mutation;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.alku.life_contract.*;
import org.alku.life_contract.follower.FollowerEvents;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public final class MutationCombatEvents {
 private static final String TEAM="LifeContractMutationTeam",APPLIED="LifeContractMutationStats";
 @SubscribeEvent public static void onDeath(LivingDeathEvent e){if(e.getEntity().level().isClientSide)return;UUID team=resolveTeam(e.getSource().getEntity());if(team!=null&&e.getEntity().getServer()!=null)MutationSavedData.get(e.getEntity().getServer()).addPoint(team);}
 @SubscribeEvent public static void onJoin(EntityJoinLevelEvent e){if(e.getLevel().isClientSide||!(e.getEntity() instanceof Mob m)||m.getPersistentData().getBoolean(APPLIED))return;UUID team=resolveTeam(m);if(team==null||m.getServer()==null)return;MutationSavedData.TeamState s=MutationSavedData.get(m.getServer()).state(team);int armor=s.level(MutationNode.ARMOR);var armorAttr=m.getAttribute(Attributes.ARMOR);var healthAttr=m.getAttribute(Attributes.MAX_HEALTH);if(armor>0&&armorAttr!=null&&healthAttr!=null){armorAttr.setBaseValue(m.getAttributeValue(Attributes.ARMOR)+armor*2);healthAttr.setBaseValue(m.getAttributeValue(Attributes.MAX_HEALTH)*(1+armor*.2));m.setHealth(m.getMaxHealth());}m.getPersistentData().putBoolean(APPLIED,true);m.getPersistentData().putUUID(TEAM,team);}
 @SubscribeEvent public static void onHurt(LivingHurtEvent e){Entity a=e.getSource().getEntity();UUID team=resolveTeam(a);if(team==null||a instanceof Player||e.getEntity().getServer()==null)return;int lv=MutationSavedData.get(e.getEntity().getServer()).state(team).level(MutationNode.BLADE);float[] bonus={0,.15f,.30f,.50f};e.setAmount(e.getAmount()*(1+bonus[lv]));}
 @SubscribeEvent public static void onPlayerAttack(LivingAttackEvent e){if(!(e.getSource().getEntity() instanceof ServerPlayer p))return;MutationSavedData.TeamState s=MutationService.state(p);if(s.level(MutationNode.MARK)==0)return;for(Mob m:p.serverLevel().getEntitiesOfClass(Mob.class,e.getEntity().getBoundingBox().inflate(32),mob->teamOfMob(mob).equals(MutationService.teamId(p))))m.setTarget(e.getEntity());e.getEntity().getPersistentData().putLong("LifeContractMarkedUntil",p.level().getGameTime()+200);}
 private static UUID resolveTeam(Entity entity){if(entity instanceof ServerPlayer p)return MutationService.teamId(p);if(entity instanceof Mob m){UUID owner=FollowerEvents.getOwnerUUID(m);if(owner!=null&&m.getServer()!=null){ServerPlayer p=m.getServer().getPlayerList().getPlayer(owner);if(p!=null)return MutationService.teamId(p);}if(m.getPersistentData().hasUUID(TEAM))return m.getPersistentData().getUUID(TEAM);ResourceLocation id=ForgeRegistries.ENTITY_TYPES.getKey(m.getType());if(id!=null&&m.getServer()!=null)for(ServerPlayer p:m.getServer().getPlayerList().getPlayers())if(id.getNamespace().equals(ContractEvents.getEffectiveContractMod(p)))return MutationService.teamId(p);}return null;}
 private static UUID teamOfMob(Mob m){UUID id=resolveTeam(m);return id==null?new UUID(0,0):id;}
}
