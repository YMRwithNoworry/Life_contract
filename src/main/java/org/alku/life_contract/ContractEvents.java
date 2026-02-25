package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;

import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;
import org.alku.life_contract.follower.FollowerHungerSystem;
import org.alku.life_contract.death_venger.DeathVengerSystem;
import org.alku.life_contract.healer.HealerSystem;
import org.alku.life_contract.revive.ReviveTeammateSystem;

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
        
        UUID leaderUUID;
        if (ImpostorSystem.isDisguised(player)) {
            leaderUUID = ImpostorSystem.getDisguisedTeam(player);
            if (leaderUUID == null) {
                leaderUUID = getLeaderUUID(player);
            }
        } else {
            leaderUUID = getLeaderUUID(player);
        }
        if (leaderUUID == null)
            leaderUUID = player.getUUID();

        ChatFormatting teamColor = getTeamColor(leaderUUID);

        MutableComponent styledName = Component.literal("").withStyle(teamColor);
        
        if (ImpostorSystem.isDisguised(player)) {
            String disguiseName = ImpostorSystem.getDisguisedName(player);
            styledName = styledName.append(Component.literal(disguiseName));
        } else {
            styledName = styledName.append(event.getDisplayname().copy());
        }
        
        event.setDisplayname(styledName);
    }

    @SubscribeEvent
    public static void onTabListFormat(PlayerEvent.TabListNameFormat event) {
        if (event.getDisplayName() == null)
            return;

        Player player = event.getEntity();
        
        UUID leaderUUID;
        if (ImpostorSystem.isDisguised(player)) {
            leaderUUID = ImpostorSystem.getDisguisedTeam(player);
            if (leaderUUID == null) {
                leaderUUID = getLeaderUUID(player);
            }
        } else {
            leaderUUID = getLeaderUUID(player);
        }
        if (leaderUUID == null)
            leaderUUID = player.getUUID();

        ChatFormatting teamColor = getTeamColor(leaderUUID);
        String effectiveMod = getEffectiveContractMod(player);

        MutableComponent result = Component.literal("").withStyle(teamColor);
        
        if (ImpostorSystem.isDisguised(player)) {
            String disguiseName = ImpostorSystem.getDisguisedName(player);
            result = result.append(Component.literal(disguiseName));
        } else {
            result = result.append(event.getDisplayName().copy());
        }

        if (effectiveMod != null && !effectiveMod.isEmpty()) {
            result.append(Component.literal(" [" + effectiveMod + "]").withStyle(ChatFormatting.GRAY));
        }

        event.setDisplayName(result);
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        syncData(event.getEntity());
        
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            loadPlayerProfessionToConfig(serverPlayer);
            ensureGamblerHasDice(serverPlayer);
            ensureDonkHasBow(serverPlayer);
            ensureFacelessDeceiverHasMask(serverPlayer);
            restoreGourmetDamageBonus(serverPlayer);
            FacelessDeceiverSystem.onPlayerJoin(serverPlayer);
            ImpostorSystem.onPlayerJoin(serverPlayer);
            FollowerHungerSystem.syncHungerMultiplierToClient(serverPlayer);
            DeathVengerSystem.loadMarkedTarget(serverPlayer);
            initializeUndeadPlayerOnJoin(serverPlayer);
            
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

    private static void loadPlayerProfessionToConfig(ServerPlayer player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId != null && !professionId.isEmpty()) {
            ProfessionConfig.setPlayerProfession(player.getUUID(), professionId);
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;
        
        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();
        
        CompoundTag originalData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();
        
        if (originalData.contains("LifeContractProfession")) {
            newData.putString("LifeContractProfession", originalData.getString("LifeContractProfession"));
        }
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
        if (originalData.contains("FacelessCurrentMimic")) {
            newData.putString("FacelessCurrentMimic", originalData.getString("FacelessCurrentMimic"));
        }
        if (originalData.contains("GourmetDamageBonus")) {
            newData.putFloat("GourmetDamageBonus", originalData.getFloat("GourmetDamageBonus"));
        }
        if (originalData.contains("GourmetHealthBonus")) {
            newData.putFloat("GourmetHealthBonus", originalData.getFloat("GourmetHealthBonus"));
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        syncData(event.getEntity());
        
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            loadPlayerProfessionToConfig(serverPlayer);
            restoreGamblerDiceOnRespawn(serverPlayer);
            restoreDonkBowOnRespawn(serverPlayer);
            FacelessDeceiverSystem.onPlayerRespawn(serverPlayer);
            ImpostorSystem.onPlayerRespawn(serverPlayer);
            HealerSystem.onPlayerRespawn(serverPlayer);
            ReviveTeammateSystem.onPlayerRespawn(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerChangeDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        syncData(event.getEntity());
        
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer serverPlayer) {
            FacelessDeceiverSystem.onPlayerJoin(serverPlayer);
            ImpostorSystem.onPlayerJoin(serverPlayer);
            FollowerHungerSystem.syncHungerMultiplierToClient(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null)
            return;

        int tickCount = server.getTickCount();

        if (tickCount % 20 == 0) {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                tickGamblerDiceCooldown(player);
                tickAmbushCooldown(player);
            }
        }

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            applyProfessionEffects(player);
            applyTurtleAura(player);
            applyEnderPearlAbility(player, tickCount);
            applyWaterDamage(player, tickCount);
            applyEnderParticles(player);
            applyFireParticles(player);
            applyWaterWeakness(player, tickCount);
            tickFacelessSwitch(player, tickCount);
            tickForgetterInvisibility(player, tickCount);
            FacelessDeceiverSystem.tickContractEffects(player);
            FollowerHungerSystem.tickHungerDrain(player, tickCount);
            ImpostorSystem.tickImpostor(player);
            FoolSystem.tickCooldown(player);
        }

        if (tickCount % 100 == 0) {
            generateProfessionResources(server, tickCount);
            generateGachaEgg(server, tickCount);
            generateFireTrail(server, tickCount);
        }
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

    public static String getEffectiveProfessionId(Player player) {
        CompoundTag data = player.getPersistentData();
        String professionId = data.getString("LifeContractProfession");
        
        if (professionId == null || professionId.isEmpty()) {
            return null;
        }
        
        String mimicId = data.getString("FacelessCurrentMimic");
        if (mimicId != null && !mimicId.isEmpty()) {
            return mimicId;
        }
        
        return professionId;
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

    private static void applyProfessionEffects(ServerPlayer player) {
        String professionId = getEffectiveProfessionId(player);
        
        if (professionId == null || professionId.isEmpty()) {
            clearAllProfessionModifiers(player);
            return;
        }
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null) {
            clearAllProfessionModifiers(player);
            return;
        }
        
        applyAttributeModifiers(player, profession);
        applyPotionEffects(player, profession);
    }

    private static void clearAllProfessionModifiers(ServerPlayer player) {
        removeModifier(player, Attributes.ARMOR, "profession_armor");
        removeModifier(player, Attributes.MAX_HEALTH, "profession_health");
        removeModifier(player, Attributes.ARMOR_TOUGHNESS, "profession_toughness");
        removeModifier(player, Attributes.ATTACK_DAMAGE, "profession_melee");
    }

    private static void removeModifier(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, String name) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        
        UUID modifierUUID = UUID.nameUUIDFromBytes(name.getBytes());
        instance.removeModifier(modifierUUID);
    }

    private static void applyAttributeModifiers(ServerPlayer player, Profession profession) {
        applyModifierValue(player, Attributes.ARMOR, "profession_armor", profession.getBonusArmor());
        applyModifierValue(player, Attributes.MAX_HEALTH, "profession_health", profession.getBonusHealth());
        applyModifierValue(player, Attributes.ARMOR_TOUGHNESS, "profession_toughness", profession.getBonusArmorToughness());
        applyModifierValue(player, Attributes.ATTACK_DAMAGE, "profession_melee", profession.getMeleeDamageBonus());
    }

    private static void applyModifierValue(ServerPlayer player, net.minecraft.world.entity.ai.attributes.Attribute attribute, String name, double value) {
        var instance = player.getAttribute(attribute);
        if (instance == null) return;
        
        UUID modifierUUID = UUID.nameUUIDFromBytes(name.getBytes());
        instance.removeModifier(modifierUUID);
        
        if (value > 0) {
            instance.addPermanentModifier(new AttributeModifier(modifierUUID, name, value, AttributeModifier.Operation.ADDITION));
        }
    }

    private static void applyPotionEffects(ServerPlayer player, Profession profession) {
        if (profession.getSlownessLevel() > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 40, profession.getSlownessLevel() - 1, false, false));
        }
        if (profession.getWeaknessLevel() > 0) {
            player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 40, profession.getWeaknessLevel() - 1, false, false));
        }
        if (profession.isAngel()) {
            player.addEffect(new MobEffectInstance(MobEffects.SATURATION, 40, 0, false, false));
        }
    }

    private static void applyEnderPearlAbility(ServerPlayer player, int tickCount) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasEnderPearlAbility()) return;
        
        if (tickCount % 100 == 0) {
            if (!player.getInventory().hasAnyMatching(stack -> stack.getItem() == Items.ENDER_PEARL)) {
                player.getInventory().add(new ItemStack(Items.ENDER_PEARL, 1));
            }
        }
    }

    private static void applyWaterDamage(ServerPlayer player, int tickCount) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasWaterDamage()) return;
        
        if (player.isInWaterOrRain()) {
            int interval = profession.getWaterDamageInterval();
            if (interval > 0 && tickCount % interval == 0) {
                player.hurt(player.level().damageSources().magic(), profession.getWaterDamageAmount());
            }
        }
    }

    private static void applyEnderParticles(ServerPlayer player) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasEnderPearlAbility()) return;
        
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 20 == 0) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 5, 0.5, 0.5, 0.5, 0.05);
        }
    }

    private static void applyFireParticles(ServerPlayer player) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasFireTrailEnabled()) return;
        
        if (player.level() instanceof ServerLevel serverLevel && player.tickCount % 10 == 0) {
            serverLevel.sendParticles(ParticleTypes.FLAME, player.getX(), player.getY() + 0.1, player.getZ(), 3, 0.3, 0.1, 0.3, 0.02);
        }
    }

    private static void applyWaterWeakness(ServerPlayer player, int tickCount) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasWaterWeakness()) return;
        
        if (player.isInWaterOrRain()) {
            int interval = profession.getWaterWeaknessInterval();
            if (interval > 0 && tickCount % interval == 0) {
                float damagePercent = profession.getWaterWeaknessDamagePercent();
                player.hurt(player.level().damageSources().magic(), player.getMaxHealth() * damagePercent / 100);
            }
        }
    }

    private static void applyTurtleAura(ServerPlayer player) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasTurtleAura()) return;
        
        float radius = profession.getTurtleAuraRadius();
        int slownessLevel = profession.getTurtleAuraSlownessLevel();
        int duration = profession.getTurtleAuraDuration();
        
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(radius),
            entity -> entity != player && entity.isAlive()
        );
        
        for (LivingEntity entity : nearbyEntities) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.MOVEMENT_SLOWDOWN,
                duration,
                slownessLevel - 1,
                false,
                false
            ));
        }
    }

    private static void tickFacelessSwitch(ServerPlayer player, int tickCount) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || !professionId.equals("faceless")) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isFaceless()) return;
        
        int switchInterval = profession.getSwitchInterval();
        if (switchInterval <= 0) return;
        
        int lastSwitch = player.getPersistentData().getInt("FacelessLastSwitch");
        if (tickCount - lastSwitch >= switchInterval * 20) {
            switchToRandomProfession(player);
            player.getPersistentData().putInt("FacelessLastSwitch", tickCount);
        }
    }

    private static void switchToRandomProfession(ServerPlayer player) {
        List<Profession> professions = ProfessionConfig.getProfessions();
        if (professions.isEmpty()) return;
        
        Profession randomProfession = professions.get(RANDOM.nextInt(professions.size()));
        player.getPersistentData().putString("FacelessCurrentMimic", randomProfession.getId());
        
        player.sendSystemMessage(Component.literal("§5[无面人] §f你变身为 §e" + randomProfession.getName() + "§f！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1, player.getZ(), 50, 0.5, 0.5, 0.5, 0.1);
        }
        
        syncData(player);
    }

    private static void tickForgetterInvisibility(ServerPlayer player, int tickCount) {
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasForgetterAbility()) return;
        
        int interval = profession.getForgetterInterval();
        if (interval <= 0) return;
        
        int lastInvis = player.getPersistentData().getInt("ForgetterLastInvis");
        if (tickCount - lastInvis >= interval * 20) {
            int minDuration = profession.getForgetterMinDuration();
            int maxDuration = profession.getForgetterMaxDuration();
            int duration = minDuration + RANDOM.nextInt(maxDuration - minDuration + 1);
            
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration * 20, 0, false, false));
            player.getPersistentData().putInt("ForgetterLastInvis", tickCount);
            
            player.sendSystemMessage(Component.literal("§7[遗忘者] §f你进入了隐身状态..."));
        }
    }

    private static void generateProfessionResources(net.minecraft.server.MinecraftServer server, int tickCount) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String professionId = getEffectiveProfessionId(player);
            if (professionId == null) continue;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || profession.getResourceInterval() <= 0) continue;
            
            int lastGen = player.getPersistentData().getInt("LastResourceGen");
            if (tickCount - lastGen >= profession.getResourceInterval() * 20) {
                String resourceItem = profession.getResourceItem();
                int amount = profession.getResourceAmount();
                
                if (resourceItem != null && !resourceItem.isEmpty() && amount > 0) {
                    net.minecraft.resources.ResourceLocation itemLoc = new net.minecraft.resources.ResourceLocation(resourceItem);
                    net.minecraft.world.item.Item item = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(itemLoc);
                    if (item != null) {
                        ItemStack stack = new ItemStack(item, amount);
                        if (!player.getInventory().add(stack)) {
                            player.drop(stack, false);
                        }
                    }
                }
                
                player.getPersistentData().putInt("LastResourceGen", tickCount);
            }
        }
    }

    private static void generateGachaEgg(net.minecraft.server.MinecraftServer server, int tickCount) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String professionId = getEffectiveProfessionId(player);
            if (professionId == null) continue;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.hasGachaAbility()) continue;
            
            int interval = profession.getGachaInterval();
            if (interval <= 0) continue;
            
            int lastGacha = player.getPersistentData().getInt("LastGachaGen");
            if (tickCount - lastGacha >= interval * 20) {
                List<String> entityPool = profession.getGachaEntityPool();
                if (!entityPool.isEmpty()) {
                    String entityId = entityPool.get(RANDOM.nextInt(entityPool.size()));
                    ItemStack egg = new ItemStack(Life_contract.CREATURE_EGG.get());
                    egg.getOrCreateTag().putString("EntityId", entityId);
                    
                    if (!player.getInventory().add(egg)) {
                        player.drop(egg, false);
                    }
                    
                    player.sendSystemMessage(Component.literal("§d[扭蛋大师] §f你获得了一个随机生物蛋！"));
                }
                
                player.getPersistentData().putInt("LastGachaGen", tickCount);
            }
        }
    }

    private static void generateFireTrail(net.minecraft.server.MinecraftServer server, int tickCount) {
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String professionId = getEffectiveProfessionId(player);
            if (professionId == null) continue;
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.hasFireTrailEnabled()) continue;
            
            if (player.level() instanceof ServerLevel serverLevel) {
                FireTrailEntity fireTrail = new FireTrailEntity(Life_contract.FIRE_TRAIL.get(), serverLevel);
                fireTrail.setPos(player.getX(), player.getY(), player.getZ());
                fireTrail.setRadius(profession.getFireTrailRadius());
                fireTrail.setDamage(profession.getFireTrailDamage());
                fireTrail.setDuration(profession.getFireTrailDuration());
                fireTrail.setOwnerUUID(player.getUUID());
                serverLevel.addFreshEntity(fireTrail);
            }
        }
    }

    private static void tickGamblerDiceCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt("GamblerDiceCooldown");
        if (cooldown > 0) {
            data.putInt("GamblerDiceCooldown", cooldown - 20);
        }
    }

    private static void tickAmbushCooldown(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt("AmbushCooldown");
        if (cooldown > 0) {
            data.putInt("AmbushCooldown", cooldown - 20);
        }
    }

    private static void ensureGamblerHasDice(ServerPlayer player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasDiceAbility()) return;
        
        boolean hasDice = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() == Life_contract.GAMBLER_DICE.get()) {
                hasDice = true;
                break;
            }
        }
        
        if (!hasDice) {
            ItemStack dice = new ItemStack(Life_contract.GAMBLER_DICE.get());
            if (!player.getInventory().add(dice)) {
                player.drop(dice, false);
            }
        }
    }

    private static void ensureDonkHasBow(ServerPlayer player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasDonkBowAbility()) return;
        
        boolean hasBow = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() == Life_contract.SEALED_BOW.get()) {
                hasBow = true;
                break;
            }
        }
        
        if (!hasBow) {
            ItemStack bow = new ItemStack(Life_contract.SEALED_BOW.get());
            if (!player.getInventory().add(bow)) {
                player.drop(bow, false);
            }
        }
    }

    private static void ensureFacelessDeceiverHasMask(ServerPlayer player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isFacelessDeceiver()) return;
        
        boolean hasMask = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            if (player.getInventory().getItem(i).getItem() == Life_contract.FACELESS_DECEIVER_MASK.get()) {
                hasMask = true;
                break;
            }
        }
        
        if (!hasMask) {
            ItemStack mask = new ItemStack(Life_contract.FACELESS_DECEIVER_MASK.get());
            if (!player.getInventory().add(mask)) {
                player.drop(mask, false);
            }
        }
    }

    private static void restoreGourmetDamageBonus(ServerPlayer player) {
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasGourmetAbility()) return;
        
        float storedBonus = player.getPersistentData().getFloat("GourmetDamageBonus");
        if (storedBonus > 0) {
            applyModifierValue(player, Attributes.ATTACK_DAMAGE, "gourmet_bonus", storedBonus);
        }
    }

    private static void restoreGamblerDiceOnRespawn(ServerPlayer player) {
        ensureGamblerHasDice(player);
    }

    private static void restoreDonkBowOnRespawn(ServerPlayer player) {
        ensureDonkHasBow(player);
    }

    private static void initializeUndeadPlayerOnJoin(ServerPlayer player) {
    }

    @SubscribeEvent
    public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null) return;
        
        EquipmentSlot slot = event.getSlot();
        if (slot.getType() != EquipmentSlot.Type.ARMOR) return;
        
        ItemStack newItem = event.getTo();
        if (newItem.isEmpty() || !(newItem.getItem() instanceof ArmorItem armorItem)) return;
        
        if (profession.isLeatherArmorOnly()) {
            if (!isLeatherArmor(armorItem)) {
                player.drop(newItem.copy(), false);
                newItem.setCount(0);
                player.sendSystemMessage(Component.literal("§c[职业限制] 你只能穿戴皮革盔甲！"));
            }
        } else if (profession.isIronArmorOnly()) {
            if (!isIronArmor(armorItem)) {
                player.drop(newItem.copy(), false);
                newItem.setCount(0);
                player.sendSystemMessage(Component.literal("§c[职业限制] 你只能穿戴铁盔甲！"));
            }
        }
    }

    private static boolean isLeatherArmor(ArmorItem armorItem) {
        return armorItem.getMaterial() == net.minecraft.world.item.ArmorMaterials.LEATHER;
    }

    private static boolean isIronArmor(ArmorItem armorItem) {
        return armorItem.getMaterial() == net.minecraft.world.item.ArmorMaterials.IRON;
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        
        String professionId = getEffectiveProfessionId(player);
        if (professionId == null || professionId.isEmpty()) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null) return;
        
        float rangedDamagePenalty = profession.getRangedDamagePenalty();
        if (rangedDamagePenalty > 0) {
            if (isRangedAttack(event.getSource())) {
                float originalDamage = event.getAmount();
                float reducedDamage = originalDamage * (1.0f - rangedDamagePenalty);
                event.setAmount(reducedDamage);
            }
        }
        
        if (profession.isEvilPoisoner()) {
            LivingEntity target = event.getEntity();
            int strengthDuration = profession.getPoisonerStrengthDuration();
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, strengthDuration, 0, false, true));
            applyRandomDebuff(target);
        }
    }
    
    private static final MobEffect[] POISONER_DEBUFF_POOL = {
        MobEffects.POISON,
        MobEffects.WEAKNESS,
        MobEffects.MOVEMENT_SLOWDOWN,
        MobEffects.WITHER,
        MobEffects.HUNGER,
        MobEffects.BLINDNESS,
        MobEffects.DIG_SLOWDOWN
    };
    
    private static void applyRandomDebuff(LivingEntity target) {
        MobEffect randomDebuff = POISONER_DEBUFF_POOL[RANDOM.nextInt(POISONER_DEBUFF_POOL.length)];
        target.addEffect(new MobEffectInstance(randomDebuff, 200, 2, false, true));
    }

    private static boolean isRangedAttack(net.minecraft.world.damagesource.DamageSource source) {
        String msgId = source.getMsgId();
        return "arrow".equals(msgId) || "trident".equals(msgId) || 
               "mob_projectile".equals(msgId) || "fireball".equals(msgId) ||
               "witherSkull".equals(msgId) || "shulkerBullet".equals(msgId);
    }

    @SubscribeEvent
    public static void onContractLivingAttack(net.minecraftforge.event.entity.living.LivingAttackEvent event) {
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
    public static void onContractLivingHurt(net.minecraftforge.event.entity.living.LivingHurtEvent event) {
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
            
            MobEffectInstance effectInstance = event.getEffectInstance();
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
