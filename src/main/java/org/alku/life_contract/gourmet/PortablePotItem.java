package org.alku.life_contract.gourmet;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.alku.life_contract.ContractEvents;

import java.util.List;

public class PortablePotItem extends Item {

    public PortablePotItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[专属厨具] 便携炖锅"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e被动效果:"));
        tooltip.add(Component.literal("§7- 储存最多 §a16 §7份已烹饪食物"));
        tooltip.add(Component.literal("§7- 额外储存 §a4 §7份招牌料理"));
        tooltip.add(Component.literal("§7- 每5秒自动转化生食材"));
        tooltip.add(Component.literal("§7- 饥饿值满时自动消耗食物回血"));
        tooltip.add(Component.literal("§7- 饥饿值低时自动补充"));
        tooltip.add(Component.literal("§7- 受致命伤害时消耗食物救命"));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            openPotInventory(serverPlayer);
        }

        return InteractionResultHolder.success(stack);
    }

    private void openPotInventory(ServerPlayer player) {
        player.sendSystemMessage(Component.literal("§a[便携炖锅] 当前储存: " + 
            GourmetSystem.getStoredFoodCount(player) + "/16 份食物"));
    }

    public static boolean storeFood(Player player, ItemStack food) {
        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return false;
        }

        if (!food.getItem().isEdible()) {
            return false;
        }

        int currentCount = GourmetSystem.getStoredFoodCount(player);
        if (currentCount >= 16) {
            player.sendSystemMessage(Component.literal("§c[便携炖锅] 已满！"));
            return false;
        }

        GourmetSystem.addStoredFood(player, food.copy());
        food.shrink(1);

        player.sendSystemMessage(Component.literal("§a[便携炖锅] 储存成功！(" + (currentCount + 1) + "/16)"));

        if (player instanceof ServerPlayer serverPlayer) {
            serverPlayer.level().playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(),
                SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.5f, 1.2f);
        }

        return true;
    }

    public static ItemStack takeFood(Player player) {
        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return ItemStack.EMPTY;
        }

        ItemStack food = GourmetSystem.getStoredFood(player, 0);
        if (!food.isEmpty()) {
            GourmetSystem.removeStoredFood(player, 0);
            return food.copy();
        }

        return ItemStack.EMPTY;
    }

    public static boolean hasFood(Player player) {
        return GourmetSystem.getStoredFoodCount(player) > 0;
    }

    public static int getFoodCount(Player player) {
        return GourmetSystem.getStoredFoodCount(player);
    }
}
