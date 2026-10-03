package org.alku.life_contract.accessory;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.alku.life_contract.Life_contract;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 饰品效果结算。
 * <p>
 * 规则：每个佩戴类别（吊坠/戒指/护符/王冠/护身符）只有<b>品阶最高的一件</b>生效，
 * 每 20 tick 重算一次；属性加成通过带固定 id 的临时修饰符写入，换装时先移除旧的再加新的。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AccessoryEffects {

    private static final int SCAN_INTERVAL_TICKS = 20;
    /** 持续药水类效果的刷新时长，留出余量避免闪烁。 */
    private static final int PASSIVE_EFFECT_DURATION_TICKS = 80;
    private static final double MAX_DAMAGE_REDUCTION = 0.75D;
    private static final double MAX_LIFESTEAL = 0.5D;

    /** player -> 类别 -> 当前生效的饰品 id。 */
    private static final Map<UUID, Map<AccessoryCategory, String>> ACTIVE = new HashMap<>();

    private AccessoryEffects() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            refresh(player);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    /** 重生后是新实体，清掉记录让下一次扫描重新加回加成。 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        ACTIVE.remove(event.getEntity().getUUID());
    }

    // ==================== 结算 ====================

    private static void refresh(ServerPlayer player) {
        Map<AccessoryCategory, AccessoryDefinition> best = bestPerCategory(player);
        Map<AccessoryCategory, String> previous =
                ACTIVE.computeIfAbsent(player.getUUID(), key -> new EnumMap<>(AccessoryCategory.class));

        for (AccessoryCategory category : AccessoryCategory.values()) {
            if (!category.isEquippable()) {
                continue;
            }

            AccessoryDefinition desired = best.get(category);
            String desiredId = desired == null ? null : desired.id();
            String currentId = previous.get(category);

            if (Objects.equals(desiredId, currentId)) {
                if (desired != null) {
                    applyPassiveEffects(player, desired);
                }
                continue;
            }

            AccessoryDefinition current = currentId == null ? null : AccessoryCatalog.byId(currentId);
            if (current != null) {
                removeBonuses(player, current);
            }
            if (desired != null) {
                applyBonuses(player, desired);
                previous.put(category, desiredId);
            } else {
                previous.remove(category);
            }
        }
    }

    /**
     * 每个佩戴类别取品阶最高的一件（同阶取先遇到的）。
     * <p>
     * 生效范围同时包括主物品栏与 Curios 饰品栏：装了 Curios 就能把饰品戴在对应槽位里，
     * 没装（或该槽位为 0）时放在背包里同样生效，两种情况下都只取每类最高品阶的一件。
     */
    private static Map<AccessoryCategory, AccessoryDefinition> bestPerCategory(Player player) {
        Map<AccessoryCategory, AccessoryDefinition> best = new EnumMap<>(AccessoryCategory.class);
        // 只有"装进饰品栏"的饰品才生效：本模组饰品栏 + Curios 槽位
        for (ItemStack stack : AccessorySlots.all(player).values()) {
            consider(best, stack);
        }
        for (ItemStack stack : CuriosCompat.equippedStacks(player)) {
            consider(best, stack);
        }
        return best;
    }

    private static void consider(Map<AccessoryCategory, AccessoryDefinition> best, ItemStack stack) {
        AccessoryDefinition definition = AccessoryCatalog.of(stack);
        if (definition == null || !definition.isEquippable()) {
            return;
        }
        AccessoryDefinition current = best.get(definition.category());
        if (current == null || definition.tier() > current.tier()) {
            best.put(definition.category(), definition);
        }
    }

    private static void applyBonuses(Player player, AccessoryDefinition definition) {
        for (AccessoryEffect effect : definition.effects()) {
            if (effect.type().kind() == AccessoryEffectType.Kind.ATTRIBUTE) {
                AttributeInstance instance = player.getAttribute(effect.type().attribute());
                if (instance != null) {
                    instance.addOrUpdateTransientModifier(new AttributeModifier(
                            modifierId(definition.category()),
                            effect.type().modifierAmount(effect.value()),
                            effect.type().operation()));
                }
            }
        }
        applyPassiveEffects(player, definition);
    }

    private static void removeBonuses(Player player, AccessoryDefinition definition) {
        for (AccessoryEffect effect : definition.effects()) {
            if (effect.type().kind() == AccessoryEffectType.Kind.ATTRIBUTE) {
                AttributeInstance instance = player.getAttribute(effect.type().attribute());
                if (instance != null) {
                    instance.removeModifier(modifierId(definition.category()));
                }
            } else if (effect.type().kind() == AccessoryEffectType.Kind.EFFECT
                    && effect.type().mobEffect() != null) {
                player.removeEffect(effect.type().mobEffect());
            }
        }
    }

    /** 持续药水类效果：每次扫描刷新一次时长。 */
    private static void applyPassiveEffects(Player player, AccessoryDefinition definition) {
        for (AccessoryEffect effect : definition.effects()) {
            if (effect.type().kind() != AccessoryEffectType.Kind.EFFECT || effect.type().mobEffect() == null) {
                continue;
            }
            int amplifier = Math.max(0, (int) Math.round(effect.value()) - 1);
            player.addEffect(new MobEffectInstance(effect.type().mobEffect(),
                    PASSIVE_EFFECT_DURATION_TICKS, amplifier, true, false, true));
        }
    }

    private static ResourceLocation modifierId(AccessoryCategory category) {
        return ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "accessory_" + category.id());
    }

    // ==================== 对外查询（供战斗/掉落等钩子使用） ====================

    /** 该玩家当前生效的饰品定义（含主物品栏里选中的每一类最佳件）。 */
    public static java.util.List<AccessoryDefinition> activeDefinitions(Player player) {
        java.util.List<AccessoryDefinition> result = new java.util.ArrayList<>();
        for (AccessoryDefinition definition : bestPerCategory(player).values()) {
            result.add(definition);
        }
        return result;
    }

    /** 某类效果的合计数值（百分比类返回百分数本身，例如 20 表示 20%）。 */
    public static double total(Player player, AccessoryEffectType type) {
        double sum = 0.0D;
        for (AccessoryDefinition definition : activeDefinitions(player)) {
            sum += definition.total(type);
        }
        return sum;
    }

    /** 受伤减免比例（0..0.75）。 */
    public static double damageReduction(Player player) {
        double percent = total(player, AccessoryEffectType.DAMAGE_REDUCTION_PERCENT);
        return Math.min(MAX_DAMAGE_REDUCTION, Math.max(0.0D, percent / 100.0D));
    }

    /** 吸血比例（0..0.5）。 */
    public static double lifesteal(Player player) {
        double percent = total(player, AccessoryEffectType.LIFESTEAL_PERCENT);
        return Math.min(MAX_LIFESTEAL, Math.max(0.0D, percent / 100.0D));
    }

    /** 升华额外掉落比例（0..1.0）。 */
    public static double sublimationBonus(Player player) {
        double percent = total(player, AccessoryEffectType.SUBLIMATION_PERCENT);
        return Math.min(1.0D, Math.max(0.0D, percent / 100.0D));
    }

    // ==================== 消耗品 ====================

    public static void applyConsumable(Player player, AccessoryEffect effect) {
        if (player.level().isClientSide() || effect == null) {
            return;
        }

        double value = effect.value();
        switch (effect.type()) {
            case HEAL -> player.heal((float) value);
            case FEED -> player.getFoodData().eat((int) value, (float) value * 0.6F);
            case XP -> {
                if (player instanceof ServerPlayer serverPlayer) {
                    serverPlayer.giveExperienceLevels((int) value);
                }
            }
            default -> {
                if (effect.type().mobEffect() != null) {
                    int duration = (int) Math.round(value) * 20;
                    player.addEffect(new MobEffectInstance(effect.type().mobEffect(), duration, 0, false, true, true));
                }
            }
        }
    }
}