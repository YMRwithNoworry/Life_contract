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

    /**
     * 物品 tooltip 与商店里的详情卡共用 {@link AccessoryTooltip} 的组装逻辑，
     * 因此背包里看到的机制说明和在商店详情卡里看到的一致。
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.addAll(AccessoryTooltip.describe(definition));

        if (definition.isEquippable()) {
            tooltip.add(Component.translatable("gui.life_contract.accessory.slot." + slotFor(definition))
                    .withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.equip_rule")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else if (definition.isConsumable()) {
            tooltip.add(Component.translatable("tooltip.life_contract.accessory.use_hint")
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
    }

    /** 该饰品对应的 Curios 槽位 id。 */
    public static String slotFor(AccessoryDefinition definition) {
        return switch (definition.category()) {
            case PENDANT, AMULET -> "necklace";
            case RING -> "ring";
            case CHARM -> "charm";
            case CROWN -> "head";
            default -> "none";
        };
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
