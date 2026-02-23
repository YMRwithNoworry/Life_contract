package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class TeamGolemWandItem extends Item {

    public static final String TAG_SELECTED_TEAM = "SelectedTeamNumber";

    public TeamGolemWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        if (!(target instanceof IronGolem golem)) {
            if (!player.isShiftKeyDown()) {
                player.sendSystemMessage(Component.literal("§c[队伍铁傀儡法杖] §f只能对铁傀儡使用！"));
            }
            return InteractionResult.PASS;
        }

        if (player.isShiftKeyDown()) {
            if (TeamIronGolemSystem.isTeamGolem(golem)) {
                TeamIronGolemSystem.removeGolemFromCache(golem.getUUID());
                golem.getPersistentData().remove(TeamIronGolemSystem.TAG_IS_TEAM_GOLEM);
                golem.getPersistentData().remove(TeamIronGolemSystem.TAG_TEAM_NUMBER);
                golem.getPersistentData().remove(TeamIronGolemSystem.TAG_OWNER_UUID);
                player.sendSystemMessage(Component.literal("§c[队伍铁傀儡法杖] §f已移除铁傀儡的队伍绑定！"));
                return InteractionResult.SUCCESS;
            } else {
                player.sendSystemMessage(Component.literal("§e[队伍铁傀儡法杖] §f这个铁傀儡没有绑定队伍！"));
                return InteractionResult.FAIL;
            }
        }

        int teamNumber = getSelectedTeam(stack);
        if (teamNumber <= 0) {
            teamNumber = TeamIronGolemSystem.getPlayerTeamNumber(player);
            setSelectedTeam(stack, teamNumber);
        }

        TeamIronGolemSystem.setGolemTeam(golem, teamNumber, player.getUUID());

        String golemName = golem.hasCustomName() ? golem.getCustomName().getString() : "铁傀儡";
        player.sendSystemMessage(Component.literal("§a[队伍铁傀儡法杖] §f已将 " + golemName + " 绑定到队伍 §b#" + teamNumber + "§f！"));
        player.sendSystemMessage(Component.literal("§7该铁傀儡将不会攻击同队玩家，且不会被推动。"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.ENCHANT,
                golem.getX(), golem.getY() + golem.getBbHeight() / 2, golem.getZ(),
                30, 0.5, 0.5, 0.5, 0.5
            );
            serverLevel.sendParticles(
                net.minecraft.core.particles.ParticleTypes.END_ROD,
                golem.getX(), golem.getY() + golem.getBbHeight() + 0.5, golem.getZ(),
                10, 0.3, 0.3, 0.3, 0.1
            );
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        if (player.isShiftKeyDown()) {
            int currentTeam = getSelectedTeam(stack);
            int playerTeam = TeamIronGolemSystem.getPlayerTeamNumber(player);
            
            if (currentTeam <= 0 || currentTeam == playerTeam) {
                int newTeam = playerTeam + 1;
                if (newTeam > 9999) newTeam = 1;
                setSelectedTeam(stack, newTeam);
                player.sendSystemMessage(Component.literal("§e[队伍铁傀儡法杖] §f已选择队伍编号: §b#" + newTeam));
            } else {
                setSelectedTeam(stack, playerTeam);
                player.sendSystemMessage(Component.literal("§e[队伍铁傀儡法杖] §f已重置为你的队伍编号: §b#" + playerTeam));
            }
            return InteractionResultHolder.success(stack);
        }

        int teamNumber = getSelectedTeam(stack);
        if (teamNumber <= 0) {
            teamNumber = TeamIronGolemSystem.getPlayerTeamNumber(player);
            setSelectedTeam(stack, teamNumber);
        }
        
        player.sendSystemMessage(Component.literal("§b[队伍铁傀儡法杖] §f当前选择队伍: §e#" + teamNumber));
        player.sendSystemMessage(Component.literal("§7右键铁傀儡 - 绑定到当前队伍"));
        player.sendSystemMessage(Component.literal("§7Shift+右键铁傀儡 - 移除队伍绑定"));
        player.sendSystemMessage(Component.literal("§7Shift+右键空气 - 切换队伍编号"));

        return InteractionResultHolder.success(stack);
    }

    public static int getSelectedTeam(ItemStack stack) {
        if (stack.hasTag() && stack.getTag() != null) {
            return stack.getTag().getInt(TAG_SELECTED_TEAM);
        }
        return 0;
    }

    public static void setSelectedTeam(ItemStack stack, int teamNumber) {
        stack.getOrCreateTag().putInt(TAG_SELECTED_TEAM, teamNumber);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        int teamNumber = getSelectedTeam(stack);
        if (teamNumber > 0) {
            components.add(Component.literal("§b当前选择队伍: §e#" + teamNumber));
        } else {
            components.add(Component.literal("§7未选择队伍（将使用你的队伍编号）"));
        }
        components.add(Component.literal(" "));
        components.add(Component.literal("§e右键铁傀儡 §7- 绑定到当前队伍"));
        components.add(Component.literal("§cShift+右键铁傀儡 §7- 移除队伍绑定"));
        components.add(Component.literal("§eShift+右键空气 §7- 切换队伍编号"));
        components.add(Component.literal(" "));
        components.add(Component.literal("§6绑定后的铁傀儡将："));
        components.add(Component.literal("§a  · 不攻击同队玩家"));
        components.add(Component.literal("§a  · 不会被其他生物推动"));
        components.add(Component.literal("§a  · 保留对敌对生物的攻击能力"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        int teamNumber = getSelectedTeam(stack);
        if (teamNumber > 0) {
            return Component.literal("§d[队伍铁傀儡法杖] §b#" + teamNumber);
        }
        return Component.literal("§d[队伍铁傀儡法杖]");
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
