package org.alku.life_contract;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.ArrayList;
import java.util.List;

public class AmbushOrbItem extends Item {
    private static final String TAG_AMBUSH_COOLDOWN = "AmbushOrbCooldown";
    private static final String TAG_AMBUSH_MODE = "AmbushMode";
    private static final String TAG_TARGET_POS = "AmbushTargetPos";
    private static final String TAG_LAST_ATTACK_TICK = "AmbushLastAttackTick";
    private static final String TAG_PASSIVE_INVISIBLE = "AmbushPassiveInvisible";
    private static final String TAG_PASSIVE_INVIS_TICK = "AmbushPassiveInvisTick";

    public AmbushOrbItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.RARE));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }

        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResultHolder.fail(stack);
        }

        String professionId = ContractEvents.getEffectiveProfessionId(serverPlayer);
        if (professionId == null || professionId.isEmpty()) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[奇兵宝珠] §r你不是奇兵，无法使用这个物品！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasAmbushAbility()) {
            serverPlayer.displayClientMessage(
                Component.literal("§c[奇兵宝珠] §r只有奇兵职业才能使用这个物品！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        int cooldownTicks = serverPlayer.getPersistentData().getInt(TAG_AMBUSH_COOLDOWN);
        if (cooldownTicks > 0) {
            int remainingSeconds = (cooldownTicks + 19) / 20;
            serverPlayer.displayClientMessage(
                Component.literal("§e[奇兵宝珠] §r冷却中，还需等待 " + remainingSeconds + " 秒！"),
                true
            );
            return InteractionResultHolder.fail(stack);
        }

        int mode = stack.getOrCreateTag().getInt(TAG_AMBUSH_MODE);
        if (mode == 0) {
            setTargetPosition(stack, serverPlayer);
            stack.getOrCreateTag().putInt(TAG_AMBUSH_MODE, 1);
            serverPlayer.displayClientMessage(
                Component.literal("§b[奇兵宝珠] §r已设置传送目标位置！再次使用执行范围隐身传送。"),
                true
            );
            playModeSwitchEffects(serverPlayer, false);
        } else {
            executeAmbushSkill(serverPlayer, stack, profession);
            stack.getOrCreateTag().putInt(TAG_AMBUSH_MODE, 0);

            int cooldownSeconds = profession.getAmbushCooldown();
            serverPlayer.getPersistentData().putInt(TAG_AMBUSH_COOLDOWN, cooldownSeconds * 20);
        }

        return InteractionResultHolder.success(stack);
    }

    private void setTargetPosition(ItemStack stack, ServerPlayer player) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putDouble(TAG_TARGET_POS + "X", player.getX());
        tag.putDouble(TAG_TARGET_POS + "Y", player.getY());
        tag.putDouble(TAG_TARGET_POS + "Z", player.getZ());
    }

    private BlockPos getTargetPosition(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_TARGET_POS + "X")) {
            return null;
        }
        return new BlockPos(
            (int)tag.getDouble(TAG_TARGET_POS + "X"),
            (int)tag.getDouble(TAG_TARGET_POS + "Y"),
            (int)tag.getDouble(TAG_TARGET_POS + "Z")
        );
    }

    private void executeAmbushSkill(ServerPlayer player, ItemStack stack, Profession profession) {
        BlockPos targetPos = getTargetPosition(stack);
        if (targetPos == null) {
            player.displayClientMessage(
                Component.literal("§c[奇兵宝珠] §r未设置传送目标位置！"),
                true
            );
            return;
        }

        double distance = player.distanceToSqr(targetPos.getX(), targetPos.getY(), targetPos.getZ());
        double maxDistance = profession.getAmbushMaxDistance();
        if (Math.sqrt(distance) > maxDistance) {
            player.displayClientMessage(
                Component.literal("§c[奇兵宝珠] §r目标位置超出传送距离限制（" + (int)maxDistance + "格）！"),
                true
            );
            return;
        }

        int maxTargets = profession.getAmbushMaxTargets();
        int invisDuration = profession.getAmbushInvisDuration() * 20;

        double radius = 10.0;
        AABB searchBox = player.getBoundingBox().inflate(radius);
        List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(
            LivingEntity.class,
            searchBox,
            entity -> entity != player && entity.isAlive()
        );

        List<LivingEntity> targetsToTeleport = new ArrayList<>();
        int count = 0;
        for (LivingEntity entity : nearbyEntities) {
            if (count >= maxTargets) break;
            targetsToTeleport.add(entity);
            count++;
        }

        for (LivingEntity entity : targetsToTeleport) {
            entity.addEffect(new MobEffectInstance(
                MobEffects.INVISIBILITY, invisDuration, 0, false, false));

            double offsetX = (player.getRandom().nextDouble() - 0.5) * 4;
            double offsetZ = (player.getRandom().nextDouble() - 0.5) * 4;
            entity.teleportTo(
                targetPos.getX() + offsetX,
                targetPos.getY(),
                targetPos.getZ() + offsetZ
            );

            if (entity instanceof Mob mob) {
                mob.setTarget(null);
            }
        }

        player.addEffect(new MobEffectInstance(
            MobEffects.INVISIBILITY, invisDuration, 0, false, false));
        player.teleportTo(targetPos.getX(), targetPos.getY(), targetPos.getZ());

        playAmbushEffects(player, targetsToTeleport.size());

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§b§l[奇兵] §f§l战术突袭！"));
        player.sendSystemMessage(Component.literal("§7  已将 §e" + targetsToTeleport.size() + " §7个生物隐身并传送"));
        player.sendSystemMessage(Component.literal("§7  隐身持续: §b" + (invisDuration / 20) + "秒"));
        player.sendSystemMessage(Component.literal(""));
    }

    private void playAmbushEffects(ServerPlayer player, int targetCount) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.REVERSE_PORTAL,
                player.getX(), player.getY() + 1, player.getZ(),
                100, 5, 1, 5, 0.2
            );
            serverLevel.sendParticles(
                ParticleTypes.END_ROD,
                player.getX(), player.getY() + 1.5, player.getZ(),
                50, 3, 1, 3, 0.1
            );
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(), player.getY() + 0.5, player.getZ(),
                30, 3, 0.5, 3, 0.05
            );
        }

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
        player.level().playSound(null, player.blockPosition(),
            SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.5F, 1.5F);
    }

    private void playModeSwitchEffects(ServerPlayer player, boolean isExecute) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1.5, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.3
            );
        }

        player.level().playSound(null, player.blockPosition(),
            SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void tickCooldown(Player player) {
        int cooldownTicks = player.getPersistentData().getInt(TAG_AMBUSH_COOLDOWN);
        if (cooldownTicks > 0) {
            player.getPersistentData().putInt(TAG_AMBUSH_COOLDOWN, cooldownTicks - 1);
        }
    }

    public static void recordAttack(Player player) {
        player.getPersistentData().putInt(TAG_LAST_ATTACK_TICK, player.tickCount);
    }

    public static void tickPassiveInvisibility(ServerPlayer player, Profession profession) {
        if (profession == null || !profession.hasAmbushAbility()) {
            return;
        }

        if (player.isSpectator() || !player.isAlive()) {
            return;
        }

        CompoundTag data = player.getPersistentData();
        int lastAttackTick = data.getInt(TAG_LAST_ATTACK_TICK);
        int currentTick = player.tickCount;
        int passiveSeconds = profession.getPassiveInvisSeconds();
        int requiredIdleTicks = passiveSeconds * 20;

        boolean isInvisible = data.getBoolean(TAG_PASSIVE_INVISIBLE);

        if (isInvisible) {
            if (currentTick - lastAttackTick < requiredIdleTicks) {
                endPassiveInvisibility(player);
                data.putBoolean(TAG_PASSIVE_INVISIBLE, false);
            } else {
                if (currentTick % 20 == 0) {
                    player.displayClientMessage(
                        Component.literal("§8[奇兵] §f被动隐身中..."),
                        true
                    );
                }
            }
        } else {
            int idleTicks = currentTick - lastAttackTick;
            if (idleTicks >= requiredIdleTicks) {
                startPassiveInvisibility(player, profession);
                data.putBoolean(TAG_PASSIVE_INVISIBLE, true);
            }
        }
    }

    private static void startPassiveInvisibility(ServerPlayer player, Profession profession) {
        int duration = 400;

        player.addEffect(new MobEffectInstance(
            MobEffects.INVISIBILITY, duration, 0, false, false));

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§8§l[奇兵] §f§l你已融入阴影！"));
        player.sendSystemMessage(Component.literal("§7  攻击将打破隐身状态"));
        player.sendSystemMessage(Component.literal(""));

        playPassiveInvisEffects(player, true);
    }

    private static void endPassiveInvisibility(ServerPlayer player) {
        player.removeEffect(MobEffects.INVISIBILITY);

        player.sendSystemMessage(Component.literal(""));
        player.sendSystemMessage(Component.literal("§8§l[奇兵] §f§l隐身状态已打破！"));
        player.sendSystemMessage(Component.literal(""));

        playPassiveInvisEffects(player, false);
    }

    private static void playPassiveInvisEffects(ServerPlayer player, boolean start) {
        if (player.level() instanceof ServerLevel serverLevel) {
            if (start) {
                serverLevel.sendParticles(
                    ParticleTypes.SMOKE,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 0.5, 0.5, 0.1
                );
                serverLevel.sendParticles(
                    ParticleTypes.PORTAL,
                    player.getX(), player.getY() + 1, player.getZ(),
                    30, 0.3, 0.3, 0.3, 0.05
                );
            } else {
                serverLevel.sendParticles(
                    ParticleTypes.END_ROD,
                    player.getX(), player.getY() + 1, player.getZ(),
                    30, 0.5, 0.5, 0.5, 0.1
                );
            }
        }

        if (start) {
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 0.5F);
        } else {
            player.level().playSound(null, player.blockPosition(),
                SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.5F);
        }
    }

    public static boolean isPassiveInvisible(Player player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null || professionId.isEmpty()) {
            return false;
        }

        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.hasAmbushAbility()) {
            return false;
        }

        return player.getPersistentData().getBoolean(TAG_PASSIVE_INVISIBLE);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public boolean hasCustomEntity(ItemStack stack) {
        return true;
    }

    @Override
    public Entity createEntity(Level level, Entity location, ItemStack itemstack) {
        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        int mode = stack.getOrCreateTag().getInt(TAG_AMBUSH_MODE);
        tooltip.add(Component.literal("§b奇兵专属物品"));
        tooltip.add(Component.literal("§b★ 永久绑定 - 死亡不掉落"));
        tooltip.add(Component.literal("§7第一次使用: 设置传送目标位置"));
        tooltip.add(Component.literal("§7第二次使用: 执行范围隐身传送"));
        tooltip.add(Component.literal("§e核心能力:"));
        tooltip.add(Component.literal("§7- §b范围隐身 §8(最多5个目标)"));
        tooltip.add(Component.literal("§7- §d群体传送 §8(最大距离50格)"));
        tooltip.add(Component.literal("§7- §8被动隐身 §8(10秒未攻击自动隐身)"));
        if (mode == 1) {
            tooltip.add(Component.literal("§a当前状态: 已设置目标位置"));
        } else {
            tooltip.add(Component.literal("§7当前状态: 等待设置目标"));
        }
    }
}
