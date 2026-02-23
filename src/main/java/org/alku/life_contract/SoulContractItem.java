package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulContractItem extends Item {

    public static final String TAG_CONTRACT_MOD = "LifeContractModId";

    public SoulContractItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            clearContract(player);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        if (!player.level().isClientSide) {
            if (player.isShiftKeyDown()) {
                clearContract(player);
                return InteractionResult.SUCCESS;
            }

            String modId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).getNamespace();
            CompoundTag data = player.getPersistentData();
            String oldMod = data.getString(TAG_CONTRACT_MOD);

            if (oldMod.equals(modId)) {
                player.sendSystemMessage(Component.literal("§e[生灵契约] §f你已经与 §e" + modId + " §f签订过契约了。"));
            } else {
                data.putString(TAG_CONTRACT_MOD, modId);
                ContractEvents.propagateContractToTeam(player, modId); // 立即同步全队
                player.sendSystemMessage(Component.literal("§a[生灵契约] §f契约签订成功！当前阵营: §e" + modId));
            }
        }
        return InteractionResult.SUCCESS;
    }

    private void clearContract(Player player) {
        CompoundTag data = player.getPersistentData();
        if (data.contains(TAG_CONTRACT_MOD)) {
            String oldMod = data.getString(TAG_CONTRACT_MOD);
            data.remove(TAG_CONTRACT_MOD);
            ContractEvents.syncData(player); // 立即同步
            player.sendSystemMessage(Component.literal("§c[生灵契约] §f已断开与 §e" + oldMod + " §f模组的灵魂连接。"));

            // 提示队伍状态
            String effectiveMod = ContractEvents.getEffectiveContractMod(player);
            if (effectiveMod != null) {
                player.sendSystemMessage(Component.literal("§e注意：§f你仍处于队伍中，正在共享队长的 §a" + effectiveMod + " §f契约效果。"));
            }
        } else {
            player.sendSystemMessage(Component.literal("§7[生灵契约] 你当前没有签订任何个人契约。"));
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.translatable("item.life_contract.soul_contract.desc").withStyle(ChatFormatting.GRAY));
        components.add(Component.literal(" "));
        components.add(Component.literal("§e[右键生物] §7签订契约"));
        components.add(Component.literal("§c[Shift+右键] §7解除个人契约"));
        super.appendHoverText(stack, level, components, flag);
    }
}