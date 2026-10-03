package org.alku.life_contract.accessory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 把一件饰品的全部机制展开成多行 tooltip。
 * <p>
 * 与物品 tooltip 的区别：物品 tooltip 只有一行"效果说明"，这里会把
 * 品阶 / 派系 / 常驻效果 / 情境条件 / 击杀充能 / 主动技 / 消耗品 / 代价 / 共鸣进度
 * 逐条拆开，并配上当前是否生效的标记 —— 用来做"点击饰品查看机制"的详情卡。
 * <p>
 * 同时被 {@link AccessoryItem} 的物品 tooltip 和升华商店的详情卡复用，
 * 保证两处显示完全一致。
 */
public final class AccessoryTooltip {

    /** 一行说明；{@code highlight} 为真时用高亮色（常用于"当前已生效"的条目）。 */
    private record Line(Component text, boolean highlight) {
    }

    private AccessoryTooltip() {
    }

    // ==================== 公共入口 ====================

    /** 物品 tooltip（无佩戴上下文）。 */
    public static List<Component> describe(AccessoryDefinition definition) {
        return render(build(definition, null, -1));
    }

    /** 详情卡：带上玩家当前的共鸣进度与充能层数。 */
    public static List<Component> describeForDisplay(AccessoryDefinition definition,
                                                     Map<AccessoryFaction, Integer> resonanceTiers,
                                                     int chargeStacks) {
        return render(build(definition, resonanceTiers, chargeStacks));
    }

    /** 详情卡（没有运行时数据的场合，例如只读列表）。 */
    public static List<Component> describeForDisplay(AccessoryDefinition definition) {
        return describeForDisplay(definition, Map.of(), -1);
    }

    // ==================== 组装 ====================

    private static List<Line> build(AccessoryDefinition definition, Map<AccessoryFaction, Integer> resonanceTiers,
                                    int chargeStacks) {
        boolean forDisplay = resonanceTiers != null || chargeStacks >= 0;
        List<Line> lines = new ArrayList<>();

        // ---- 名称 + 品阶 + 派系 ----
        Component header = Component.translatable(definition.nameKey())
                .withStyle(AccessoryItem.tierColor(definition.tier()))
                .append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("tooltip.life_contract.accessory.tier", definition.tier())
                        .withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.literal("  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable("gui.life_contract.accessory.category." + definition.category().id())
                        .withStyle(ChatFormatting.GRAY));
        lines.add(new Line(header, true));

        if (definition.hasFaction()) {
            lines.add(new Line(Component.translatable("tooltip.life_contract.accessory.faction",
                            Component.translatable("faction.life_contract." + definition.faction().id()))
                    .withStyle(definition.faction().color()), false));
        }

        // ---- 常驻效果 ----
        List<AccessoryEffect> always = new ArrayList<>();
        List<AccessoryEffect> conditional = new ArrayList<>();
        for (AccessoryEffect effect : definition.effects()) {
            (effect.isAlways() ? always : conditional).add(effect);
        }
        appendEffectGroup(lines, Component.translatable("gui.life_contract.accessory.section.base"), always);

        // ---- 情境效果：按条件分组 ----
        if (!conditional.isEmpty()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.conditional")
                    .withStyle(ChatFormatting.AQUA), false));
            Map<AccessoryCondition, List<AccessoryEffect>> groups = new java.util.LinkedHashMap<>();
            for (AccessoryEffect effect : conditional) {
                groups.computeIfAbsent(effect.when(), key -> new ArrayList<>()).add(effect);
            }
            for (Map.Entry<AccessoryCondition, List<AccessoryEffect>> group : groups.entrySet()) {
                Component condition = Component.translatable("condition.life_contract." + group.getKey().name())
                        .withStyle(ChatFormatting.YELLOW);
                lines.add(new Line(Component.literal("  ").append(condition), false));
                for (AccessoryEffect effect : group.getValue()) {
                    lines.add(new Line(Component.literal("    ").append(effectText(effect)), false));
                }
            }
        }

        // ---- 击杀充能 ----
        AccessoryCharge charge = definition.charge();
        if (!charge.isEmpty()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.charge")
                    .withStyle(ChatFormatting.GOLD), false));
            lines.add(new Line(Component.literal("  ").append(Component.translatable(
                    "gui.life_contract.accessory.charge_detail", charge.max())), false));
            for (AccessoryEffect effect : charge.perStack()) {
                lines.add(new Line(Component.literal("  ").append(Component.translatable(
                        "gui.life_contract.accessory.charge_per_stack", effectText(effect))), false));
            }
            if (charge.decayTicks() > 0) {
                lines.add(new Line(Component.literal("  ").append(Component.translatable(
                        "gui.life_contract.accessory.charge_decay",
                        String.format("%.0f", charge.decayTicks() / 20.0D))), false));
            }
            if (charge.loseOnHurt() > 0) {
                lines.add(new Line(Component.literal("  ").append(Component.translatable(
                        "gui.life_contract.accessory.charge_lose", charge.loseOnHurt()))
                        .withStyle(ChatFormatting.RED), false));
            }
            if (chargeStacks >= 0) {
                lines.add(new Line(Component.literal("  ").append(Component.translatable(
                        "gui.life_contract.accessory.charge_current", chargeStacks, charge.max()))
                        .withStyle(chargeStacks > 0 ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY), chargeStacks > 0));
            }
        }

        // ---- 主动技 ----
        AccessoryActive active = definition.active();
        if (!active.isEmpty()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.active")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false));
            lines.add(new Line(Component.literal("  ").append(Component.translatable(
                    "active.life_contract." + active.kind().name()).withStyle(ChatFormatting.WHITE))
                    .append(Component.literal(" — ").withStyle(ChatFormatting.DARK_GRAY))
                    .append(Component.translatable("active.life_contract." + active.kind().name() + ".desc")
                            .withStyle(ChatFormatting.GRAY)), true));
            lines.add(new Line(Component.literal("  ").append(Component.translatable(
                    "gui.life_contract.accessory.active_detail",
                    String.format("%.0f", active.durationTicks() / 20.0D),
                    String.format("%.0f", active.cooldownTicks() / 20.0D))), false));
        }

        // ---- 消耗品 ----
        if (!definition.consume().isEmpty()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.consume")
                    .withStyle(ChatFormatting.GREEN), false));
            for (AccessoryEffect effect : definition.consume()) {
                lines.add(new Line(Component.literal("  ").append(consumeText(effect)), false));
            }
        }

        // ---- 代价 ----
        if (definition.hasDrawback()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.drawback")
                    .withStyle(ChatFormatting.DARK_RED), false));
            for (AccessoryEffect effect : definition.effects()) {
                if (effect.value() < 0.0D) {
                    lines.add(new Line(Component.literal("  ").append(effectText(effect))
                            .withStyle(ChatFormatting.RED), false));
                }
            }
        }

        // ---- 派系共鸣进度 ----
        if (definition.hasFaction()) {
            AccessoryResonance resonance = AccessoryCatalog.resonance(definition.faction());
            int count = resonanceTiers == null ? 0
                    : resonanceTiers.getOrDefault(definition.faction(), 0);
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.section.resonance")
                    .withStyle(ChatFormatting.LIGHT_PURPLE), false));
            int tier = AccessoryResonance.tierFor(count);
            lines.add(new Line(Component.literal("  ").append(Component.translatable(
                    "gui.life_contract.accessory.resonance_state", count, tier))
                    .withStyle(tier > 0 ? ChatFormatting.GREEN : ChatFormatting.GRAY), tier > 0));
            if (resonance != null) {
                appendResonanceTier(lines, "gui.life_contract.accessory.resonance_t2",
                        resonance.descriptionForTier(1, true), count >= 1);
                appendResonanceTier(lines, "gui.life_contract.accessory.resonance_t3",
                        resonance.descriptionForTier(2, true), count >= 2);
            }
        }

        // ---- 佩戴规则与售价：物品 tooltip 里由 AccessoryItem 自己补，详情卡里也补一份 ----
        if (forDisplay && definition.isEquippable()) {
            lines.add(new Line(Component.translatable("gui.life_contract.accessory.rule")
                    .withStyle(ChatFormatting.DARK_GRAY), false));
        }
        if (forDisplay && definition.price() > 0) {
            lines.add(new Line(Component.translatable("tooltip.life_contract.accessory.price", definition.price())
                    .withStyle(ChatFormatting.GOLD), false));
        }
        return lines;
    }

    /**
     * 共鸣档位说明。
     * <p>
     * 共鸣描述里常有多个并列效果（用「；」分隔），一整行在固定宽度的详情卡里会溢出，
     * 这里按分隔符拆成多行，每行都很短。
     */
    private static void appendResonanceTier(List<Line> lines, String key, String description, boolean active) {
        ChatFormatting color = active ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY;
        String[] parts = description.split("[；;，,、]");
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            if (part.isEmpty()) {
                continue;
            }
            Component text = i == 0
                    ? Component.translatable(key, part)
                    : Component.literal("    " + part);
            lines.add(new Line(Component.literal(i == 0 ? "  " : "").append(text).withStyle(color), active));
        }
    }

    private static void appendEffectGroup(List<Line> lines, Component title, List<AccessoryEffect> effects) {
        if (effects.isEmpty()) {
            return;
        }
        lines.add(new Line(title.copy().withStyle(ChatFormatting.WHITE), false));
        for (AccessoryEffect effect : effects) {
            lines.add(new Line(Component.literal("  ").append(effectText(effect)), false));
        }
    }

    /** 效果数值 -> 一行文字；负值用红色。 */
    private static Component effectText(AccessoryEffect effect) {
        ChatFormatting color = effect.value() < 0.0D ? ChatFormatting.RED
                : (effect.value() > 0.0D ? ChatFormatting.GREEN : ChatFormatting.GRAY);
        return Component.translatable("effect.life_contract." + effect.type().name(),
                format(effect.type(), effect.value())).withStyle(color);
    }

    /** 消耗品里的药水效果 value 是"秒数"，用单独的键，避免显示成"抗性提升 20 级"。 */
    private static Component consumeText(AccessoryEffect effect) {
        String key = effect.type().kind() == AccessoryEffectType.Kind.EFFECT
                ? "effect.life_contract." + effect.type().name() + "_SEC"
                : "effect.life_contract." + effect.type().name();
        return Component.translatable(key, format(effect.type(), effect.value())).withStyle(ChatFormatting.GREEN);
    }

    /**
     * 效果数值的文本。
     * <p>
     * "数值型"效果（属性、吸血、伤害减免等）带正负号，负数显示成 {@code -8%} 而不是 {@code +-8%}；
     * 等级型（夜视 I）与治疗/饱食/经验这类自带语义的数字则不加符号。
     */
    private static String format(AccessoryEffectType type, double value) {
        String number = number(Math.abs(value));
        if (type.isPercent()) {
            number = number + "%";
        }
        if (!isSigned(type)) {
            return number;
        }
        return (value >= 0.0D ? "+" : "-") + number;
    }

    /** 属性、吸血、伤害减免、升华掉落这类"加成数值"要带正负号。 */
    private static boolean isSigned(AccessoryEffectType type) {
        return switch (type.kind()) {
            case ATTRIBUTE, SUBLIMATION, LIFESTEAL, DAMAGE_REDUCTION -> true;
            default -> false;
        };
    }

    /** 整数不留小数位，其余保留最多两位并去掉末尾的 0。 */
    private static String number(double magnitude) {
        if (magnitude == Math.rint(magnitude)) {
            return String.format("%.0f", magnitude);
        }
        return String.format("%.2f", magnitude).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    private static String slotFor(AccessoryDefinition definition) {
        return switch (definition.category()) {
            case PENDANT, AMULET -> "necklace";
            case RING -> "ring";
            case CHARM -> "charm";
            case CROWN -> "head";
            default -> "none";
        };
    }

    private static List<Component> render(List<Line> lines) {
        List<Component> result = new ArrayList<>(lines.size());
        for (Line line : lines) {
            result.add(line.text());
        }
        return result;
    }
}
