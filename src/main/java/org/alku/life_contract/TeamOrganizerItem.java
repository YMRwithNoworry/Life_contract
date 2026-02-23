package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeamOrganizerItem extends Item {

    public static final String TAG_LEADER_UUID = "LifeContractLeaderUUID";
    public static final String TAG_LEADER_NAME = "LifeContractLeaderName";
    public static final String TAG_TEAM_NUMBER = "LifeContractTeamNumber";

    public TeamOrganizerItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
            InteractionHand hand) {
        if (!player.level().isClientSide && target instanceof Player targetPlayer) {

            if (player.isShiftKeyDown()) {
                CompoundTag targetData = targetPlayer.getPersistentData();
                if (targetData.hasUUID(TAG_LEADER_UUID)) {
                    targetData.remove(TAG_LEADER_UUID);
                    targetData.remove(TAG_LEADER_NAME);
                    
                    // 确保被踢出的玩家有自己的队伍编号
                    if (!targetData.contains(TAG_TEAM_NUMBER)) {
                        int teamNumber = Math.abs(targetPlayer.getUUID().hashCode() % 9999) + 1;
                        targetData.putInt(TAG_TEAM_NUMBER, teamNumber);
                    }
                    
                    ContractEvents.syncData(targetPlayer); // 同步目标玩家

                    player.sendSystemMessage(
                            Component.literal("§c[组队器] §f已将 " + targetPlayer.getName().getString() + " 移出你的队伍。"));
                    targetPlayer.sendSystemMessage(
                            Component.literal("§c[组队器] §f你已离开 " + player.getName().getString() + " 的队伍。"));
                }
                return InteractionResult.SUCCESS;
            }

            CompoundTag targetData = targetPlayer.getPersistentData();
            targetData.putUUID(TAG_LEADER_UUID, player.getUUID());
            targetData.putString(TAG_LEADER_NAME, player.getName().getString());

            // 核心修复：新队员加入即同步队长的契约状态，防止队长离线失效
            String leaderMod = player.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
            if (!leaderMod.isEmpty()) {
                targetData.putString(SoulContractItem.TAG_CONTRACT_MOD, leaderMod);
            }

            ContractEvents.syncData(targetPlayer); // 同步目标玩家

            player.sendSystemMessage(
                    Component.literal("§a[组队器] §f玩家 " + targetPlayer.getName().getString() + " 已加入你的队伍！"));
            targetPlayer.sendSystemMessage(
                    Component.literal("§a[组队器] §f你已加入 " + player.getName().getString() + " 的队伍，共享其契约效果。"));

            return InteractionResult.SUCCESS;
        }
        return super.interactLivingEntity(stack, player, target, hand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§e右键玩家§7: 拉人入队（共享契约）"));
        components.add(Component.literal("§cShift+右键玩家§7: 踢人/离队"));
        super.appendHoverText(stack, level, components, flag);
    }
}