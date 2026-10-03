package org.alku.life_contract.accessory;

import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 饰品效果结算引擎。
 * <p>
 * 每 10 tick 为每个玩家重算一次"当前真正生效的效果集合"，来源有四层：
 * <ol>
 *   <li><b>基础</b>：每个佩戴类别取品阶最高的一件（{@link AccessoryCategory} 的同类取优规则）；</li>
 *   <li><b>情境</b>：效果自带 {@link AccessoryCondition}，条件成立才计入；</li>
 *   <li><b>充能</b>：击杀叠加的层数按 {@code per_stack} 折算；</li>
 *   <li><b>共鸣</b>：生效饰品里同派系达到 2/3 件时追加 {@link AccessoryResonance} 的效果。</li>
 * </ol>
 * 属性修饰符按"来源"（{@code base:<类别>} / {@code resonance:<派系>}）分组做差分更新，
 * 只有数值变化时才写回属性，避免每 tick 触发客户端同步。
 */
@EventBusSubscriber(modid = Life_contract.MODID, bus = EventBusSubscriber.Bus.GAME)
public final class AccessoryEffects {

    private static final int SCAN_INTERVAL_TICKS = 10;
    /** 持续药水类效果的刷新时长，留出余量避免闪烁。 */
    private static final int PASSIVE_EFFECT_DURATION_TICKS = 60;
    private static final double MAX_DAMAGE_REDUCTION = 0.75D;
    private static final double MAX_LIFESTEAL = 0.5D;
    private static final double MAX_SUBLIMATION_BONUS = 1.5D;

    /** 每个玩家当前生效的全部效果合计，供战斗/掉落等钩子快速查询。 */
    private static final Map<UUID, Map<AccessoryEffectType, Double>> TOTALS = new HashMap<>();
    /** 每个玩家当前生效的饰品（每类一件）。 */
    private static final Map<UUID, List<AccessoryDefinition>> ACTIVE = new HashMap<>();
    /** 每个玩家当前的派系计数。 */
    private static final Map<UUID, Map<AccessoryFaction, Integer>> FACTION_COUNTS = new HashMap<>();
    /** 每个玩家当前生效的条件集合。 */
    private static final Map<UUID, EnumSet<AccessoryCondition>> CONDITIONS = new HashMap<>();
    /** 每个玩家已写入的属性修饰符：来源 key -> 属性 -> 数值。 */
    private static final Map<UUID, Map<String, Map<Holder<Attribute>, Double>>> APPLIED = new HashMap<>();
    /** 每个玩家上次播报过的共鸣档位，用于在共鸣触发/断开时提示玩家。 */
    private static final Map<UUID, Map<AccessoryFaction, Integer>> RESONANCE_STATE = new HashMap<>();

    private AccessoryEffects() {
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (event.getServer().getTickCount() % SCAN_INTERVAL_TICKS != 0L) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            refresh(player);
            syncClientState(player);
        }
    }

    /** 把"生效的各派系件数 + 各饰品充能层数"同步给玩家客户端，供详情卡显示实时状态。 */
    private static void syncClientState(ServerPlayer player) {
        Map<String, Integer> factionCounts = new LinkedHashMap<>();
        for (Map.Entry<AccessoryFaction, Integer> entry : factionCounts(player).entrySet()) {
            factionCounts.put(entry.getKey().id(), entry.getValue());
        }

        Map<String, Integer> chargeStacks = new LinkedHashMap<>();
        for (AccessoryDefinition definition : activeDefinitions(player)) {
            if (!definition.charge().isEmpty()) {
                chargeStacks.put(definition.id(), AccessoryState.charge(player, definition.id()));
            }
        }

        NetworkHandler.sendToPlayer(player, new AccessoryStatePayload(factionCounts, chargeStacks));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        clear(event.getEntity().getUUID());
    }

    /** 重生后是新实体，清掉记录让下一次扫描重新加回加成。 */
    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        clear(event.getEntity().getUUID());
    }

    private static void clear(UUID uuid) {
        TOTALS.remove(uuid);
        ACTIVE.remove(uuid);
        FACTION_COUNTS.remove(uuid);
        CONDITIONS.remove(uuid);
        APPLIED.remove(uuid);
        RESONANCE_STATE.remove(uuid);
        AccessoryState.clear(uuid);
    }

    // ==================== 结算 ====================

    /** 立即重算该玩家的饰品效果（换装、受伤、击杀后调用可让反馈更快）。 */
    public static void refresh(ServerPlayer player) {
        EnumSet<AccessoryCondition> conditions = AccessoryCondition.evaluate(player);
        Map<AccessoryCategory, AccessoryDefinition> best = bestPerCategory(player);

        List<AccessoryDefinition> activeDefs = new ArrayList<>(best.values());
        Collections.sort(activeDefs, (left, right) -> {
            int byTier = Integer.compare(right.tier(), left.tier());
            return byTier != 0 ? byTier : left.id().compareTo(right.id());
        });
        ACTIVE.put(player.getUUID(), List.copyOf(activeDefs));
        CONDITIONS.put(player.getUUID(), conditions);

        // 来源 -> 属性加成
        Map<String, Map<Holder<Attribute>, Double>> attributeSources = new LinkedHashMap<>();
        Map<Holder<MobEffect>, Integer> mobEffects = new LinkedHashMap<>();
        Map<AccessoryEffectType, Double> totals = new EnumMap<>(AccessoryEffectType.class);

        for (AccessoryDefinition definition : activeDefs) {
            String source = "base_" + definition.category().id();
            Map<Holder<Attribute>, Double> attributes = attributeSources.computeIfAbsent(source, key -> new LinkedHashMap<>());

            for (AccessoryEffect effect : definition.effects()) {
                accumulate(effect, attributes, mobEffects, totals, conditions);
            }

            // 击杀充能
            AccessoryCharge charge = definition.charge();
            if (!charge.isEmpty()) {
                tickCharge(player, definition, charge);
                int stacks = AccessoryState.charge(player, definition.id());
                if (stacks > 0) {
                    for (AccessoryEffect effect : charge.perStack()) {
                        accumulate(effect.scaled(stacks), attributes, mobEffects, totals, conditions);
                    }
                }
            }
        }

        // 派系共鸣
        Map<AccessoryFaction, Integer> counts = new EnumMap<>(AccessoryFaction.class);
        for (AccessoryDefinition definition : activeDefs) {
            if (definition.hasFaction()) {
                counts.merge(definition.faction(), 1, Integer::sum);
            }
        }
        FACTION_COUNTS.put(player.getUUID(), counts);

        for (Map.Entry<AccessoryFaction, Integer> entry : counts.entrySet()) {
            int tier = AccessoryResonance.tierFor(entry.getValue());
            if (tier <= 0) {
                continue;
            }
            AccessoryResonance resonance = AccessoryCatalog.resonance(entry.getKey());
            if (resonance == null) {
                continue;
            }
            Map<Holder<Attribute>, Double> attributes = attributeSources
                    .computeIfAbsent("resonance_" + entry.getKey().id(), key -> new LinkedHashMap<>());
            for (AccessoryEffect effect : resonance.effectsForTier(tier)) {
                accumulate(effect, attributes, mobEffects, totals, conditions);
            }
        }

        TOTALS.put(player.getUUID(), totals);
        applyAttributes(player, attributeSources);
        applyMobEffects(player, mobEffects);
        notifyResonanceChanges(player, counts);
    }

    /** 共鸣档位变化时用 action bar 提示一次，让玩家知道"搭配生效了 / 断了"。 */
    private static void notifyResonanceChanges(ServerPlayer player, Map<AccessoryFaction, Integer> counts) {
        Map<AccessoryFaction, Integer> previous =
                RESONANCE_STATE.computeIfAbsent(player.getUUID(), key -> new EnumMap<>(AccessoryFaction.class));

        for (AccessoryFaction faction : AccessoryFaction.values()) {
            if (faction == AccessoryFaction.NONE) {
                continue;
            }
            int now = AccessoryResonance.tierFor(counts.getOrDefault(faction, 0));
            int before = previous.getOrDefault(faction, 0);
            if (now == before) {
                continue;
            }
            previous.put(faction, now);
            if (now > 0) {
                player.displayClientMessage(Component.translatable(
                        "message.life_contract.accessory.resonance_on",
                        Component.translatable("faction.life_contract." + faction.id()), now), true);
            } else {
                player.displayClientMessage(Component.translatable(
                        "message.life_contract.accessory.resonance_off",
                        Component.translatable("faction.life_contract." + faction.id())), true);
            }
        }
    }

    private static void accumulate(AccessoryEffect effect,
                                   Map<Holder<Attribute>, Double> attributes,
                                   Map<Holder<MobEffect>, Integer> mobEffects,
                                   Map<AccessoryEffectType, Double> totals,
                                   EnumSet<AccessoryCondition> conditions) {
        if (effect.value() == 0.0D || !effect.test(conditions)) {
            return;
        }
        totals.merge(effect.type(), effect.value(), Double::sum);

        AccessoryEffectType type = effect.type();
        if (type.kind() == AccessoryEffectType.Kind.ATTRIBUTE && type.attribute() != null) {
            attributes.merge(type.attribute(), type.modifierAmount(effect.value()), Double::sum);
        } else if (type.kind() == AccessoryEffectType.Kind.EFFECT && type.mobEffect() != null) {
            int amplifier = Math.max(0, (int) Math.round(effect.value()) - 1);
            mobEffects.merge(type.mobEffect(), amplifier, Math::max);
        }
    }

    /** 充能衰减：每 decay_ticks 掉 1 层。 */
    private static void tickCharge(ServerPlayer player, AccessoryDefinition definition, AccessoryCharge charge) {
        int stacks = AccessoryState.charge(player, definition.id());
        if (stacks <= 0) {
            AccessoryState.setLastDecayTick(player, definition.id(), player.getServer() == null
                    ? 0 : player.getServer().getTickCount());
            return;
        }
        if (charge.decayTicks() <= 0) {
            return;
        }
        int now = player.getServer() == null ? 0 : player.getServer().getTickCount();
        int last = AccessoryState.lastDecayTick(player, definition.id());
        if (last == 0) {
            AccessoryState.setLastDecayTick(player, definition.id(), now);
            return;
        }
        if (now - last >= charge.decayTicks()) {
            AccessoryState.addCharge(player, definition.id(), -1, charge.max());
            AccessoryState.setLastDecayTick(player, definition.id(), now);
        }
    }

    /** 属性差分写入：只改动数值变化的条目。 */
    private static void applyAttributes(Player player, Map<String, Map<Holder<Attribute>, Double>> desired) {
        Map<String, Map<Holder<Attribute>, Double>> previous =
                APPLIED.computeIfAbsent(player.getUUID(), key -> new LinkedHashMap<>());

        for (Map.Entry<String, Map<Holder<Attribute>, Double>> entry : desired.entrySet()) {
            Map<Holder<Attribute>, Double> before = previous.getOrDefault(entry.getKey(), Map.of());
            ResourceLocation modifierId = modifierId(entry.getKey());

            for (Map.Entry<Holder<Attribute>, Double> attribute : entry.getValue().entrySet()) {
                AttributeInstance instance = player.getAttribute(attribute.getKey());
                if (instance == null) {
                    continue;
                }
                Double old = before.get(attribute.getKey());
                if (old != null && Math.abs(old - attribute.getValue()) < 1.0E-6D) {
                    continue;
                }
                instance.addOrUpdateTransientModifier(new AttributeModifier(
                        modifierId, attribute.getValue(), operationFor(attribute.getKey())));
            }

            for (Holder<Attribute> attribute : before.keySet()) {
                if (!entry.getValue().containsKey(attribute)) {
                    AttributeInstance instance = player.getAttribute(attribute);
                    if (instance != null) {
                        instance.removeModifier(modifierId);
                    }
                }
            }
            previous.put(entry.getKey(), new LinkedHashMap<>(entry.getValue()));
        }

        // 来源整体消失（例如共鸣断了）
        List<String> stale = new ArrayList<>();
        for (String key : previous.keySet()) {
            if (!desired.containsKey(key)) {
                stale.add(key);
            }
        }
        for (String key : stale) {
            ResourceLocation modifierId = modifierId(key);
            for (Holder<Attribute> attribute : previous.get(key).keySet()) {
                AttributeInstance instance = player.getAttribute(attribute);
                if (instance != null) {
                    instance.removeModifier(modifierId);
                }
            }
            previous.remove(key);
        }
    }

    private static AttributeModifier.Operation operationFor(Holder<Attribute> attribute) {
        for (AccessoryEffectType type : AccessoryEffectType.values()) {
            if (type.kind() == AccessoryEffectType.Kind.ATTRIBUTE && Objects.equals(type.attribute(), attribute)) {
                return type.operation();
            }
        }
        return AttributeModifier.Operation.ADD_VALUE;
    }

    /** 持续药水类效果：每次扫描刷新时长，取所有来源里最高的等级。 */
    private static void applyMobEffects(Player player, Map<Holder<MobEffect>, Integer> mobEffects) {
        for (Map.Entry<Holder<MobEffect>, Integer> entry : mobEffects.entrySet()) {
            player.addEffect(new MobEffectInstance(entry.getKey(), PASSIVE_EFFECT_DURATION_TICKS,
                    entry.getValue(), true, false, true));
        }
    }

    /**
     * 每个佩戴类别取品阶最高的一件（同阶取先遇到的）。
     * <p>
     * 生效范围只有 Curios 饰品栏：放在背包里不生效，必须戴进对应槽位。
     */
    private static Map<AccessoryCategory, AccessoryDefinition> bestPerCategory(Player player) {
        Map<AccessoryCategory, AccessoryDefinition> best = new EnumMap<>(AccessoryCategory.class);
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

    private static ResourceLocation modifierId(String sourceKey) {
        return ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "acc_" + sourceKey);
    }

    // ==================== 对外查询（供战斗/掉落等钩子使用） ====================

    /** 该玩家当前生效的饰品（每类一件，按品阶从高到低）。 */
    public static List<AccessoryDefinition> activeDefinitions(Player player) {
        List<AccessoryDefinition> cached = ACTIVE.get(player.getUUID());
        if (cached != null) {
            return cached;
        }
        return List.copyOf(bestPerCategory(player).values());
    }

    /** 某类效果的合计数值（百分比类返回百分数本身，例如 20 表示 20%）。 */
    public static double total(Player player, AccessoryEffectType type) {
        Map<AccessoryEffectType, Double> totals = TOTALS.get(player.getUUID());
        if (totals == null) {
            double sum = 0.0D;
            for (AccessoryDefinition definition : activeDefinitions(player)) {
                sum += definition.total(type);
            }
            return sum;
        }
        return totals.getOrDefault(type, 0.0D);
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

    /** 升华额外掉落比例（0..1.5）。 */
    public static double sublimationBonus(Player player) {
        double percent = total(player, AccessoryEffectType.SUBLIMATION_PERCENT);
        return Math.min(MAX_SUBLIMATION_BONUS, Math.max(0.0D, percent / 100.0D));
    }

    /** 该玩家当前生效的派系计数（只统计已生效的佩戴类饰品）。 */
    public static Map<AccessoryFaction, Integer> factionCounts(Player player) {
        Map<AccessoryFaction, Integer> counts = FACTION_COUNTS.get(player.getUUID());
        if (counts != null) {
            return counts;
        }
        Map<AccessoryFaction, Integer> computed = new EnumMap<>(AccessoryFaction.class);
        for (AccessoryDefinition definition : activeDefinitions(player)) {
            if (definition.hasFaction()) {
                computed.merge(definition.faction(), 1, Integer::sum);
            }
        }
        return computed;
    }

    /** 已触发的共鸣：派系 -> 档位（1 = 共鸣 I，2 = 共鸣 II）。 */
    public static Map<AccessoryFaction, Integer> activeResonances(Player player) {
        Map<AccessoryFaction, Integer> result = new EnumMap<>(AccessoryFaction.class);
        for (Map.Entry<AccessoryFaction, Integer> entry : factionCounts(player).entrySet()) {
            int tier = AccessoryResonance.tierFor(entry.getValue());
            if (tier > 0) {
                result.put(entry.getKey(), tier);
            }
        }
        return result;
    }

    /** 该玩家当前生效的条件集合（可能滞后最多 10 tick）。 */
    public static EnumSet<AccessoryCondition> conditions(Player player) {
        EnumSet<AccessoryCondition> cached = CONDITIONS.get(player.getUUID());
        if (cached != null) {
            return cached;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            return AccessoryCondition.evaluate(serverPlayer);
        }
        return EnumSet.of(AccessoryCondition.ALWAYS);
    }

    // ==================== 消耗品 ====================

    public static void applyConsumable(Player player, List<AccessoryEffect> effects) {
        if (player.level().isClientSide() || effects == null || effects.isEmpty()) {
            return;
        }
        for (AccessoryEffect effect : effects) {
            applyConsumable(player, effect);
        }
    }

    private static void applyConsumable(Player player, AccessoryEffect effect) {
        if (effect == null) {
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
                    int amplifier = effect.type().isPercent() ? 0 : 0;
                    player.addEffect(new MobEffectInstance(effect.type().mobEffect(), duration, amplifier,
                            false, true, true));
                }
            }
        }
    }
}
