package org.alku.life_contract;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nullable;
import java.util.List;

public class SporeGlandItem extends Item implements ICurioItem {

    public SporeGlandItem() {
        super(new Item.Properties()
                .stacksTo(1)
                .rarity(Rarity.RARE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        
        tooltip.add(Component.literal("§a孢子腺体"));
        tooltip.add(Component.literal("§7攻击时30%概率传播孢子"));
        tooltip.add(Component.literal("§7孢子会在5秒后生成感染生物"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§6击杀孢子精英可获得此饰品"));
    }

    @Override
    public boolean canEquip(SlotContext slotContext, ItemStack stack) {
        if (slotContext.entity() instanceof Player player) {
            return !hasSameAccessoryEquipped(player, this.getClass());
        }
        return true;
    }
    
    private boolean hasSameAccessoryEquipped(Player player, Class<?> itemClass) {
        var curiosHelper = top.theillusivec4.curios.api.CuriosApi.getCuriosHelper();
        var handlerOpt = curiosHelper.getCuriosHandler(player).resolve();
        if (handlerOpt.isEmpty()) return false;
        
        var handler = handlerOpt.get();
        for (var entry : handler.getCurios().entrySet()) {
            var stacksHandler = entry.getValue();
            var stacks = stacksHandler.getStacks();
            for (int i = 0; i < stacks.getSlots(); i++) {
                var equippedStack = stacks.getStackInSlot(i);
                if (!equippedStack.isEmpty() && itemClass.isInstance(equippedStack.getItem())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean canUnequip(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 0;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }
}
