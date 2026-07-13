package org.alku.life_contract.follower;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FollowerWandItem extends Item {
    public FollowerWandItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(player instanceof ServerPlayer serverPlayer) || !(target instanceof Mob mob)
                || mob.getType().getCategory() != MobCategory.MONSTER) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 只能选择怪物！"));
            return InteractionResult.FAIL;
        }

        if (!FollowerEvents.isAlliedWithPlayer(player, mob)) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 只能选择自己阵营的怪物！"));
            return InteractionResult.FAIL;
        }

        if (WandFollowerSystem.isBoundFollower(player, mob)) {
            player.sendSystemMessage(Component.literal("§e[跟随之杖] 该怪物已经在紧紧跟随你。"));
            return InteractionResult.SUCCESS;
        }

        WandFollowerSystem.bind(serverPlayer, mob);
        String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
        player.sendSystemMessage(Component.literal("§a[跟随之杖] §e" + mobName + " §f开始紧紧跟随你。"));
        player.displayClientMessage(Component.literal("§e额外饥饿消耗: §c+10%"), true);

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                    mob.getX(), mob.getY() + mob.getBbHeight() * 0.5D, mob.getZ(),
                    20, 0.4D, 0.5D, 0.4D, 0.05D);
            serverLevel.sendParticles(ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    20, 0.4D, 0.7D, 0.4D, 0.3D);
            serverLevel.playSound(null, mob.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME,
                    SoundSource.PLAYERS, 1.0F, 1.2F);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }

        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
            if (WandFollowerSystem.releaseCurrent(serverPlayer)) {
                player.sendSystemMessage(Component.literal("§e[跟随之杖] 已解除当前怪物的跟随。"));
                player.displayClientMessage(Component.literal("§a额外饥饿消耗已停止"), true);
            } else {
                player.sendSystemMessage(Component.literal("§7[跟随之杖] 当前没有法杖随从。"));
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§d[跟随之杖]").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("§e右键己方怪物 §7- 使其紧紧跟随你"));
        components.add(Component.literal("§eShift+右键空气 §7- 解除当前跟随"));
        components.add(Component.literal("§7同时只能有 §f1 §7只法杖随从，重新选择会替换旧目标"));
        components.add(Component.literal("§c绑定期间额外饥饿消耗为原版的 10%"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("跟随之杖").withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
