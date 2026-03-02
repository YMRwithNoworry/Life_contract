package org.alku.life_contract.byte_chen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class ByteChenSystem {

    public enum NodeType {
        SCOUT,
        BUFF,
        COUNTER
    }

    public static class InfoNode {
        public NodeType type;
        public BlockPos position;
        public int remainingTicks;
        public UUID ownerUuid;
        public int cost;

        public InfoNode(NodeType type, BlockPos position, int duration, UUID ownerUuid, int cost) {
            this.type = type;
            this.position = position;
            this.remainingTicks = duration;
            this.ownerUuid = ownerUuid;
            this.cost = cost;
        }

        public CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", type.name());
            tag.putInt("x", position.getX());
            tag.putInt("y", position.getY());
            tag.putInt("z", position.getZ());
            tag.putInt("remainingTicks", remainingTicks);
            tag.putUUID("ownerUuid", ownerUuid);
            tag.putInt("cost", cost);
            return tag;
        }

        public static InfoNode load(CompoundTag tag) {
            NodeType type = NodeType.valueOf(tag.getString("type"));
            BlockPos pos = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
            int remaining = tag.getInt("remainingTicks");
            UUID owner = tag.getUUID("ownerUuid");
            int cost = tag.getInt("cost");
            return new InfoNode(type, pos, remaining, owner, cost);
        }
    }

    private static final String TAG_COMPUTE = "ByteChenCompute";
    private static final String TAG_NODES = "ByteChenNodes";
    private static final String TAG_FULL_READ_COOLDOWN = "ByteChenFullReadCooldown";
    private static final String TAG_DATA_DISPATCH_COOLDOWN = "ByteChenDataDispatchCooldown";
    private static final String TAG_DATA_BAN_COOLDOWN = "ByteChenDataBanCooldown";
    private static final String TAG_ULTIMATE_COOLDOWN = "ByteChenUltimateCooldown";
    private static final String TAG_RECYCLE_COOLDOWN = "ByteChenRecycleCooldown";
    private static final String TAG_EXHAUST_TIMER = "ByteChenExhaustTimer";
    private static final String TAG_ULTIMATE_ACTIVE = "ByteChenUltimateActive";
    private static final String TAG_ULTIMATE_TIMER = "ByteChenUltimateTimer";
    private static final String TAG_DATA_BAN_TARGET = "ByteChenDataBanTarget";
    private static final String TAG_DATA_BAN_TIMER = "ByteChenDataBanTimer";

    private static final UUID SPEED_BONUS_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567902");
    private static final UUID MELEE_PENALTY_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345679013");
    private static final UUID DAMAGE_BONUS_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456790124");
    private static final UUID DAMAGE_REDUCTION_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890235");

    private static final Map<UUID, List<InfoNode>> playerNodes = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickByteChen(player, tickCount);
        }
    }

    private static void tickByteChen(ServerPlayer player, int tickCount) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        tickCooldowns(player, data);
        tickComputeRegen(player, data, profession);
        tickNodes(player, data, profession);
        tickExhaust(player, data, profession);
        tickUltimate(player, data, profession);
        applyPassiveEffects(player, data, profession);
        
        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickCooldowns(ServerPlayer player, CompoundTag data) {
        int fullReadCd = data.getInt(TAG_FULL_READ_COOLDOWN);
        if (fullReadCd > 0) data.putInt(TAG_FULL_READ_COOLDOWN, fullReadCd - 1);
        
        int dispatchCd = data.getInt(TAG_DATA_DISPATCH_COOLDOWN);
        if (dispatchCd > 0) data.putInt(TAG_DATA_DISPATCH_COOLDOWN, dispatchCd - 1);
        
        int banCd = data.getInt(TAG_DATA_BAN_COOLDOWN);
        if (banCd > 0) data.putInt(TAG_DATA_BAN_COOLDOWN, banCd - 1);
        
        int ultimateCd = data.getInt(TAG_ULTIMATE_COOLDOWN);
        if (ultimateCd > 0) data.putInt(TAG_ULTIMATE_COOLDOWN, ultimateCd - 1);
        
        int recycleCd = data.getInt(TAG_RECYCLE_COOLDOWN);
        if (recycleCd > 0) data.putInt(TAG_RECYCLE_COOLDOWN, recycleCd - 1);
    }

    private static void tickComputeRegen(ServerPlayer player, CompoundTag data, Profession profession) {
        int compute = data.getInt(TAG_COMPUTE);
        int maxCompute = profession.getByteChenComputeMax();
        
        if (compute < maxCompute) {
            int regenRate = profession.getByteChenComputeRegenRate();
            float nodeBonus = profession.getByteChenComputeNodeRegenBonus();
            
            List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
            int activeNodes = 0;
            for (InfoNode node : nodes) {
                if (node.position.closerToCenterThan(player.position(), profession.getByteChenNodeRange())) {
                    activeNodes++;
                }
            }
            
            int totalRegen = regenRate + (int)(activeNodes * nodeBonus);
            data.putInt(TAG_COMPUTE, Math.min(maxCompute, compute + totalRegen));
        }
    }

    private static void tickNodes(ServerPlayer player, CompoundTag data, Profession profession) {
        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        List<InfoNode> toRemove = new ArrayList<>();
        
        for (InfoNode node : nodes) {
            node.remainingTicks--;
            
            if (node.remainingTicks <= 0) {
                toRemove.add(node);
                continue;
            }
            
            if (node.position.closerToCenterThan(player.position(), profession.getByteChenNodeRange())) {
                applyNodeEffects(player, node, profession);
            }
        }
        
        nodes.removeAll(toRemove);
        playerNodes.put(player.getUUID(), nodes);
        
        if (!toRemove.isEmpty()) {
            saveNodesToData(data, nodes);
        }
    }

    private static void applyNodeEffects(ServerPlayer player, InfoNode node, Profession profession) {
        if (player.level() instanceof ServerLevel serverLevel) {
            if (node.type == NodeType.SCOUT && node.remainingTicks % 20 == 0) {
                serverLevel.sendParticles(ParticleTypes.END_ROD,
                    node.position.getX() + 0.5, node.position.getY() + 1, node.position.getZ() + 0.5,
                    5, 0.3, 0.3, 0.3, 0.02);
            }
        }
    }

    private static void tickExhaust(ServerPlayer player, CompoundTag data, Profession profession) {
        int exhaustTimer = data.getInt(TAG_EXHAUST_TIMER);
        if (exhaustTimer > 0) {
            data.putInt(TAG_EXHAUST_TIMER, exhaustTimer - 1);
            
            if (exhaustTimer == 1) {
                player.sendSystemMessage(Component.literal("§a[字节陈] 算力枯竭结束，系统恢复运行"));
            }
        }
    }

    private static void tickUltimate(ServerPlayer player, CompoundTag data, Profession profession) {
        boolean ultimateActive = data.getBoolean(TAG_ULTIMATE_ACTIVE);
        if (ultimateActive) {
            int timer = data.getInt(TAG_ULTIMATE_TIMER);
            timer--;
            
            if (timer <= 0) {
                endUltimate(player, data, profession);
            } else {
                data.putInt(TAG_ULTIMATE_TIMER, timer);
                applyUltimateEffects(player, data, profession);
            }
        }
    }

    private static void applyUltimateEffects(ServerPlayer player, CompoundTag data, Profession profession) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                player.getX(), player.getY() + 1, player.getZ(),
                10, 2, 0.5, 2, 0.1);
        }
    }

    private static void endUltimate(ServerPlayer player, CompoundTag data, Profession profession) {
        data.putBoolean(TAG_ULTIMATE_ACTIVE, false);
        data.putInt(TAG_EXHAUST_TIMER, profession.getByteChenExhaustDuration());
        
        removeAttributeModifier(player, Attributes.MOVEMENT_SPEED, SPEED_BONUS_UUID);
        removeAttributeModifier(player, Attributes.ATTACK_DAMAGE, DAMAGE_BONUS_UUID);
        
        player.sendSystemMessage(Component.literal("§c[字节陈] 全域字节重构结束，进入算力枯竭状态！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION_EMITTER,
                player.getX(), player.getY() + 1, player.getZ(),
                1, 0, 0, 0, 0);
        }
    }

    private static void applyPassiveEffects(ServerPlayer player, CompoundTag data, Profession profession) {
        int exhaustTimer = data.getInt(TAG_EXHAUST_TIMER);
        if (exhaustTimer > 0) {
            var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
            if (speedAttr != null) {
                speedAttr.removeModifier(SPEED_BONUS_UUID);
                speedAttr.addPermanentModifier(new AttributeModifier(
                    SPEED_BONUS_UUID, "byte_chen_exhaust_slow",
                    -0.2, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }
    }

    public static void deployNode(ServerPlayer player, NodeType type) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        int compute = data.getInt(TAG_COMPUTE);
        
        int cost = getNodeCost(type, profession);
        if (compute < cost) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力不足！需要 " + cost + " 点算力"));
            return;
        }

        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        int maxNodes = profession.getByteChenNodeMax();
        if (nodes.size() >= maxNodes) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 信息节点已达上限！"));
            return;
        }

        data.putInt(TAG_COMPUTE, compute - cost);
        
        BlockPos pos = player.blockPosition();
        int duration = profession.getByteChenNodeDuration();
        InfoNode node = new InfoNode(type, pos, duration, player.getUUID(), cost);
        nodes.add(node);
        playerNodes.put(player.getUUID(), nodes);
        saveNodesToData(data, nodes);
        
        String nodeName = getNodeName(type);
        player.sendSystemMessage(Component.literal("§b[字节陈] 部署" + nodeName + "成功！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5,
                30, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0f, 1.5f);
        }
    }

    private static int getNodeCost(NodeType type, Profession profession) {
        switch (type) {
            case SCOUT: return profession.getByteChenScoutNodeCost();
            case BUFF: return profession.getByteChenBuffNodeCost();
            case COUNTER: return profession.getByteChenCounterNodeCost();
            default: return 20;
        }
    }

    private static String getNodeName(NodeType type) {
        switch (type) {
            case SCOUT: return "侦察节点";
            case BUFF: return "增益节点";
            case COUNTER: return "反制节点";
            default: return "信息节点";
        }
    }

    public static void useFullRead(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        if (data.getInt(TAG_EXHAUST_TIMER) > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力枯竭中，无法使用技能！"));
            return;
        }
        
        int cooldown = data.getInt(TAG_FULL_READ_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 全量信息读取冷却中！"));
            return;
        }

        int cost = profession.getByteChenFullReadCost();
        int compute = data.getInt(TAG_COMPUTE);
        if (compute < cost) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力不足！需要 " + cost + " 点算力"));
            return;
        }

        data.putInt(TAG_COMPUTE, compute - cost);
        data.putInt(TAG_FULL_READ_COOLDOWN, profession.getByteChenFullReadCooldown());
        
        addCompute(player, profession.getByteChenComputeOnRead());
        
        player.sendSystemMessage(Component.literal("§d[字节陈] 全量信息读取启动！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            float radius = profession.getByteChenFullReadRadius();
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                100, radius, radius / 2, radius, 0.1);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.PLAYERS, 1.0f, 1.2f);
        }
    }

    public static void useDataDispatch(ServerPlayer player, int mode) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        if (data.getInt(TAG_EXHAUST_TIMER) > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力枯竭中，无法使用技能！"));
            return;
        }
        
        int cooldown = data.getInt(TAG_DATA_DISPATCH_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 战场数据调度冷却中！"));
            return;
        }

        int cost = profession.getByteChenDataDispatchCost();
        int compute = data.getInt(TAG_COMPUTE);
        if (compute < cost) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力不足！需要 " + cost + " 点算力"));
            return;
        }

        data.putInt(TAG_COMPUTE, compute - cost);
        data.putInt(TAG_DATA_DISPATCH_COOLDOWN, profession.getByteChenDataDispatchCooldown());
        
        if (mode == 0) {
            applyDataDispatchBuff(player, profession);
        } else {
            recycleAllNodes(player, data, profession);
        }
    }

    private static void applyDataDispatchBuff(ServerPlayer player, Profession profession) {
        float radius = 8.0f;
        float speedBonus = profession.getByteChenDataDispatchSpeedBonus();
        int duration = profession.getByteChenDataDispatchBuffDuration();
        
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(
            Player.class,
            player.getBoundingBox().inflate(radius),
            p -> ContractEvents.isSameTeam(player, p)
        );
        
        for (Player teammate : nearbyPlayers) {
            teammate.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, true));
            teammate.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, duration, 0, false, true));
        }
        
        player.sendSystemMessage(Component.literal("§a[字节陈] 数据加速已应用于 " + nearbyPlayers.size() + " 名队友！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1, player.getZ(),
                30, radius / 2, 1, radius / 2, 0.1);
        }
    }

    private static void recycleAllNodes(ServerPlayer player, CompoundTag data, Profession profession) {
        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        int totalRefund = 0;
        float refundRate = profession.getByteChenNodeRecycleRefund();
        
        for (InfoNode node : nodes) {
            totalRefund += (int)(node.cost * refundRate);
        }
        
        nodes.clear();
        playerNodes.put(player.getUUID(), nodes);
        saveNodesToData(data, nodes);
        
        addCompute(player, totalRefund);
        
        player.sendSystemMessage(Component.literal("§a[字节陈] 全域回收完成，返还 " + totalRefund + " 点算力！"));
        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, false, true));
    }

    public static void useDataBan(ServerPlayer player, int targetId) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        if (data.getInt(TAG_EXHAUST_TIMER) > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力枯竭中，无法使用技能！"));
            return;
        }
        
        int cooldown = data.getInt(TAG_DATA_BAN_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 定向数据封禁冷却中！"));
            return;
        }

        int cost = profession.getByteChenDataBanCost();
        int compute = data.getInt(TAG_COMPUTE);
        if (compute < cost) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力不足！需要 " + cost + " 点算力"));
            return;
        }

        Entity target = player.level().getEntity(targetId);
        if (!(target instanceof LivingEntity livingTarget)) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 无效的目标！"));
            return;
        }

        float range = profession.getByteChenDataBanRange();
        if (player.distanceTo(target) > range) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 目标超出范围！"));
            return;
        }

        data.putInt(TAG_COMPUTE, compute - cost);
        data.putInt(TAG_DATA_BAN_COOLDOWN, profession.getByteChenDataBanCooldown());
        
        int duration = profession.getByteChenDataBanDuration();
        boolean isBoss = livingTarget.hasCustomName() && livingTarget.getMaxHealth() > 100;
        if (isBoss) {
            duration = profession.getByteChenDataBanBossDuration();
        }
        
        livingTarget.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 5, false, true));
        livingTarget.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, true));
        
        data.putInt(TAG_DATA_BAN_TARGET, targetId);
        data.putInt(TAG_DATA_BAN_TIMER, duration);
        
        player.sendSystemMessage(Component.literal("§c[字节陈] 数据封禁已应用于目标！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(), target.getY() + 1, target.getZ(),
                30, 0.5, 0.5, 0.5, 0.1);
            serverLevel.playSound(null, target.getX(), target.getY(), target.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 0.5f, 1.5f);
        }
    }

    public static void useUltimate(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        if (data.getInt(TAG_EXHAUST_TIMER) > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力枯竭中，无法使用终极技能！"));
            return;
        }
        
        int cooldown = data.getInt(TAG_ULTIMATE_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 全域字节重构冷却中！"));
            return;
        }

        int minCost = profession.getByteChenUltimateMinCost();
        int compute = data.getInt(TAG_COMPUTE);
        if (compute < minCost) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 算力不足！需要至少 " + minCost + " 点算力"));
            return;
        }

        data.putInt(TAG_COMPUTE, 0);
        data.putInt(TAG_ULTIMATE_COOLDOWN, profession.getByteChenUltimateCooldown());
        data.putBoolean(TAG_ULTIMATE_ACTIVE, true);
        data.putInt(TAG_ULTIMATE_TIMER, profession.getByteChenUltimateDuration());
        
        applyUltimateBuffs(player, profession);
        applyUltimateDebuffs(player, profession);
        
        player.sendSystemMessage(Component.literal("§d[字节陈] 全域字节重构启动！信息掌控领域展开！"));
        
        if (player.level() instanceof ServerLevel serverLevel) {
            float radius = profession.getByteChenUltimateRadius();
            serverLevel.sendParticles(ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1, player.getZ(),
                200, radius / 2, radius / 4, radius / 2, 0.2);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.END_PORTAL_SPAWN, SoundSource.PLAYERS, 1.0f, 0.5f);
        }
    }

    private static void applyUltimateBuffs(ServerPlayer player, Profession profession) {
        float radius = 12.0f;
        int duration = profession.getByteChenUltimateDuration();
        
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(
            Player.class,
            player.getBoundingBox().inflate(radius),
            p -> ContractEvents.isSameTeam(player, p)
        );
        
        for (Player teammate : nearbyPlayers) {
            teammate.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, 1, false, true));
            teammate.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, 1, false, true));
            teammate.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, 1, false, true));
        }
    }

    private static void applyUltimateDebuffs(ServerPlayer player, Profession profession) {
        float radius = profession.getByteChenUltimateRadius();
        int duration = profession.getByteChenUltimateDuration();
        
        List<LivingEntity> enemies = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(radius),
            e -> {
                if (e == player) return false;
                if (e instanceof Player otherPlayer) {
                    return !ContractEvents.isSameTeam(player, otherPlayer);
                }
                return true;
            }
        );
        
        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 1, false, true));
        }
    }

    public static void recycleNodes(ServerPlayer player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;

        CompoundTag data = player.getPersistentData();
        
        int cooldown = data.getInt(TAG_RECYCLE_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 节点回收冷却中！"));
            return;
        }

        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        if (nodes.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[字节陈] 没有可回收的节点！"));
            return;
        }

        int totalRefund = 0;
        float refundRate = profession.getByteChenNodeRecycleRefund();
        
        for (InfoNode node : nodes) {
            totalRefund += (int)(node.cost * refundRate);
        }
        
        nodes.clear();
        playerNodes.put(player.getUUID(), nodes);
        saveNodesToData(data, nodes);
        
        data.putInt(TAG_RECYCLE_COOLDOWN, profession.getByteChenNodeRecycleCooldown());
        addCompute(player, totalRefund);
        
        player.sendSystemMessage(Component.literal("§a[字节陈] 节点回收完成，返还 " + totalRefund + " 点算力！"));
    }

    public static void addCompute(ServerPlayer player, int amount) {
        CompoundTag data = player.getPersistentData();
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null) return;
        
        int compute = data.getInt(TAG_COMPUTE);
        int maxCompute = profession.getByteChenComputeMax();
        data.putInt(TAG_COMPUTE, Math.min(maxCompute, compute + amount));
    }

    private static void removeAttributeModifier(Player player, net.minecraft.world.entity.ai.attributes.Attribute attribute, UUID uuid) {
        var attr = player.getAttribute(attribute);
        if (attr != null) {
            attr.removeModifier(uuid);
        }
    }

    private static void saveNodesToData(CompoundTag data, List<InfoNode> nodes) {
        ListTag nodeList = new ListTag();
        for (InfoNode node : nodes) {
            nodeList.add(node.save());
        }
        data.put(TAG_NODES, nodeList);
    }

    private static List<InfoNode> loadNodesFromData(CompoundTag data) {
        List<InfoNode> nodes = new ArrayList<>();
        if (data.contains(TAG_NODES)) {
            ListTag nodeList = data.getList(TAG_NODES, 10);
            for (Tag tag : nodeList) {
                nodes.add(InfoNode.load((CompoundTag) tag));
            }
        }
        return nodes;
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return;
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isByteChen()) return;
        
        data.putInt(TAG_COMPUTE, profession.getByteChenComputeInitial());
        data.putInt(TAG_FULL_READ_COOLDOWN, 0);
        data.putInt(TAG_DATA_DISPATCH_COOLDOWN, 0);
        data.putInt(TAG_DATA_BAN_COOLDOWN, 0);
        data.putInt(TAG_ULTIMATE_COOLDOWN, 0);
        data.putInt(TAG_RECYCLE_COOLDOWN, 0);
        data.putInt(TAG_EXHAUST_TIMER, 0);
        data.putBoolean(TAG_ULTIMATE_ACTIVE, false);
        data.putInt(TAG_ULTIMATE_TIMER, 0);
        
        playerNodes.remove(player.getUUID());
    }

    public static void onPlayerJoin(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        List<InfoNode> nodes = loadNodesFromData(data);
        playerNodes.put(player.getUUID(), nodes);
    }

    public static void onPlayerLeave(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        saveNodesToData(data, nodes);
        playerNodes.remove(player.getUUID());
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int compute = data.getInt(TAG_COMPUTE);
        int fullReadCd = data.getInt(TAG_FULL_READ_COOLDOWN);
        int dispatchCd = data.getInt(TAG_DATA_DISPATCH_COOLDOWN);
        int banCd = data.getInt(TAG_DATA_BAN_COOLDOWN);
        int ultimateCd = data.getInt(TAG_ULTIMATE_COOLDOWN);
        int recycleCd = data.getInt(TAG_RECYCLE_COOLDOWN);
        int exhaustTimer = data.getInt(TAG_EXHAUST_TIMER);
        boolean ultimateActive = data.getBoolean(TAG_ULTIMATE_ACTIVE);
        int ultimateTimer = data.getInt(TAG_ULTIMATE_TIMER);
        
        List<InfoNode> nodes = playerNodes.getOrDefault(player.getUUID(), new ArrayList<>());
        int nodeCount = nodes.size();

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncByteChenState(compute, fullReadCd, dispatchCd, banCd, ultimateCd, 
                recycleCd, exhaustTimer, ultimateActive, ultimateTimer, nodeCount));
    }

    public static int getCompute(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_COMPUTE);
    }

    public static int getFullReadCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_FULL_READ_COOLDOWN);
    }

    public static int getDataDispatchCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_DATA_DISPATCH_COOLDOWN);
    }

    public static int getDataBanCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_DATA_BAN_COOLDOWN);
    }

    public static int getUltimateCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_ULTIMATE_COOLDOWN);
    }

    public static int getRecycleCooldown(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_RECYCLE_COOLDOWN);
    }

    public static int getExhaustTimer(ServerPlayer player) {
        return player.getPersistentData().getInt(TAG_EXHAUST_TIMER);
    }

    public static boolean isUltimateActive(ServerPlayer player) {
        return player.getPersistentData().getBoolean(TAG_ULTIMATE_ACTIVE);
    }

    public static int getNodeCount(ServerPlayer player) {
        return playerNodes.getOrDefault(player.getUUID(), new ArrayList<>()).size();
    }
}
