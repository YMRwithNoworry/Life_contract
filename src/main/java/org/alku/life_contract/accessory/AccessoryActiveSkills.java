package org.alku.life_contract.accessory;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.ContractEvents;

import java.util.ArrayList;
import java.util.List;

/**
 * 饰品主动技（按 G 释放）。
 * <p>
 * 一次只触发一件：在所有已佩戴饰品里取<b>品阶最高、且冷却已结束</b>的那一件；
 * 同阶时按 {@link AccessoryEffects#activeDefinitions} 的顺序（品阶降序 + id 升序）。
 * 冷却按饰品 id 独立记录，换装不会清冷却。
 */
public final class AccessoryActiveSkills {

    private static final double AURA_RADIUS = 16.0D;

    private AccessoryActiveSkills() {
    }

    /** 玩家按下 G 键时调用。 */
    public static void use(ServerPlayer player) {
        List<AccessoryDefinition> definitions = AccessoryEffects.activeDefinitions(player);

        AccessoryDefinition ready = null;
        AccessoryDefinition cooling = null;
        int shortestRemaining = Integer.MAX_VALUE;

        for (AccessoryDefinition definition : definitions) {
            if (definition.active().isEmpty()) {
                continue;
            }
            int remaining = AccessoryState.cooldownRemaining(player, definition.id());
            if (remaining <= 0) {
                ready = definition;
                break;
            }
            if (remaining < shortestRemaining) {
                shortestRemaining = remaining;
                cooling = definition;
            }
        }

        if (ready == null) {
            if (cooling != null) {
                player.displayClientMessage(Component.translatable(
                        "message.life_contract.accessory.active_cooling",
                        Component.translatable(cooling.nameKey()),
                        String.format("%.1f", shortestRemaining / 20.0D)), true);
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.life_contract.accessory.active_none"), true);
            }
            return;
        }

        cast(player, ready);

        int tick = player.getServer() == null ? 0 : player.getServer().getTickCount();
        AccessoryState.setCooldownEnd(player, ready.id(), tick + ready.active().cooldownTicks());

        player.displayClientMessage(Component.translatable(
                "message.life_contract.accessory.active_used",
                Component.translatable(ready.nameKey()),
                String.format("%.1f", ready.active().cooldownTicks() / 20.0D)), true);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.4F);
    }

    private static void cast(ServerPlayer player, AccessoryDefinition definition) {
        AccessoryActive active = definition.active();
        int duration = Math.max(20, active.durationTicks());
        int amplifier = Math.max(0, (int) Math.round(active.value()) - 1);

        switch (active.kind()) {
            case BURST -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, true, true));
            }
            case WARD -> {
                player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier, false, true, true));
                int absorption = Math.max(0, (int) Math.round(active.value() / 4.0D) - 1);
                player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, duration, absorption, false, true, true));
            }
            case MEND -> {
                player.heal((float) active.value());
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, false, true, true));
            }
            case DASH -> {
                Vec3 look = player.getLookAngle();
                player.push(look.x * active.value(), 0.35D, look.z * active.value());
                player.hurtMarked = true;
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, true, true));
            }
            case PHASE -> {
                player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, duration, 0, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, duration, amplifier, false, true, true));
            }
            case PURGE -> {
                List<MobEffectInstance> negatives = new ArrayList<>();
                for (MobEffectInstance instance : player.getActiveEffects()) {
                    if (!instance.getEffect().value().isBeneficial()) {
                        negatives.add(instance);
                    }
                }
                for (MobEffectInstance instance : negatives) {
                    player.removeEffect(instance.getEffect());
                }
                player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, duration, amplifier, false, true, true));
            }
            case MARK -> {
                AABB box = player.getBoundingBox().inflate(AURA_RADIUS);
                for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class, box)) {
                    if (target == player) {
                        continue;
                    }
                    boolean hostile = target instanceof Enemy;
                    if (target instanceof Player other) {
                        if (ContractEvents.isSameTeam(player, other)) {
                            continue;
                        }
                        hostile = true;
                    }
                    if (!hostile) {
                        continue;
                    }
                    target.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0, false, true, true));
                    target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, amplifier, false, true, true));
                }
            }
            case RALLY -> {
                AABB box = player.getBoundingBox().inflate(AURA_RADIUS);
                for (ServerPlayer ally : player.level().getEntitiesOfClass(ServerPlayer.class, box)) {
                    if (ally != player && !ContractEvents.isSameTeam(player, ally)) {
                        continue;
                    }
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, duration, amplifier, false, true, true));
                    ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, duration, amplifier, false, true, true));
                }
            }
        }
    }
}
