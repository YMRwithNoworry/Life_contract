package org.alku.life_contract.accessory;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import java.util.List;

/** 饰品/材料/消耗品的统一物品类，行为完全由 {@link AccessoryDefinition} 决定。 */
public class AccessoryItem extends Item {

    private final AccessoryDefinition definition;

    public AccessoryItem(AccessoryDefinition definition) {
        super(new Item.Properties().stacksTo(definition.isEquippable() ? 1 : 64));
        this.definition = definition;
    }

    public AccessoryDefinition definition() {
        return definition;
    }

    /** 品阶颜色：1 灰 / 2 白 / 3 青 / 4 金 / 5 红。 */
    public static ChatFormatting tierColor(int tier) {
        return switch (tier) {
            case 1 -> ChatFormatting.GRAY;
            case 2 -> ChatFormatting.WHITE;
            case 3 -> ChatFormatting.AQUA;
            case 4 -> ChatFormatting.GOLD;
            default -> ChatFormatting.RED;
        };
    }

    @Override
    public Component getName(ItemStack stack) {
        return super.getName(stack).copy().withStyle(tierColor(definition.tier()));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.life_contract.accessory.tier", definition.tier())
                .withStyle(ChatFormatting.DARK_GRAY));

        if (definition.hasFaction()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.faction",
                            Component.translatable("faction.life_contract." + definition.faction().id()))
                    .withStyle(definition.faction().color()));
        }

        if (!definition.effects().isEmpty() || !definition.consume().isEmpty()) {
            tooltip.add(Component.translatable(definition.effectKey()).withStyle(ChatFormatting.GRAY));
        }

        AccessoryCharge charge = definition.charge();
        if (!charge.isEmpty()) {
            if (charge.decayTicks() > 0) {
                tooltip.add(Component.translatable("tooltip.life_contract.accessory.charge",
                                charge.max(), String.format("%.0f", charge.decayTicks() / 20.0D))
                        .withStyle(ChatFormatting.YELLOW));
            } else {
                tooltip.add(Component.translatable("tooltip.life_contract.accessory.charge_no_decay", charge.max())
                        .withStyle(ChatFormatting.YELLOW));
            }
            if (charge.loseOnHurt() > 0) {
                tooltip.add(Component.translatable("tooltip.life_contract.accessory.charge_hurt", charge.loseOnHurt())
                        .withStyle(ChatFormatting.DARK_RED));
            }
        }

        AccessoryActive active = definition.active();
        if (!active.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.active",
                            Component.translatable("active.life_contract." + active.kind().name()),
                            String.format("%.0f", active.cooldownTicks() / 20.0D))
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }

        if (definition.hasDrawback()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.drawback")
                    .withStyle(ChatFormatting.DARK_RED));
        }

        if (definition.isEquippable()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.equip_rule")
                    .withStyle(ChatFormatting.DARK_GRAY));
            if (definition.hasFaction()) {
                tooltip.add(Component.translatable("tooltip.life_contract.accessory.resonance_hint")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
        } else if (definition.isConsumable()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.use_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }

        if (definition.price() > 0) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.price", definition.price())
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    /** 消耗品：右键使用。 */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!definition.isConsumable() || definition.consume().isEmpty()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level.isClientSide()) {
            return InteractionResultHolder.sidedSuccess(stack, true);
        }

        AccessoryEffects.applyConsumable(player, definition.consume());
        if (!player.getAbilities().instabuild) {
            stack.shrink(1);
        }
        return InteractionResultHolder.sidedSuccess(stack, false);
    }
}
