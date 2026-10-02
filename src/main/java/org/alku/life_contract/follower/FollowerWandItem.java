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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;

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

        if (!(target instanceof Mob mob)) return InteractionResult.PASS;

        if (!(player instanceof ServerPlayer serverPlayer)) return InteractionResult.SUCCESS;

        if (!FollowerEvents.isAlliedWithPlayer(player, mob)) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 只能选择自己阵营的怪物！"));
            return InteractionResult.FAIL;
        }

        WandEggStorage storage = WandEggStorage.getOrCreate(serverPlayer);
        if (!storage.hasSpace()) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 收服仓库已满！请先取出生物蛋。"));
            return InteractionResult.FAIL;
        }
        WandFollowerSystem.onCapturedByWand(mob);
        if (!storage.capture(mob)) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 无法保存该生物。"));
            return InteractionResult.FAIL;
        }
        mob.discard();
        String mobName = mob.hasCustomName() ? mob.getCustomName().getString() : mob.getName().getString();
        player.sendSystemMessage(Component.literal("§a[跟随之杖] §e" + mobName + " §f已收服并存入生物蛋仓库。"));

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
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && !PlayerUIMenuType.openUI(serverPlayer, WandEggUIHolder.UI_ID)) {
            player.sendSystemMessage(Component.literal("§c[跟随之杖] 无法打开收服仓库。"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§d[跟随之杖]").withStyle(ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("§e右键盟友生物 §7- 收服并存入生物蛋"));
        components.add(Component.literal("§e右键空气 §7- 打开 20 格生物蛋仓库"));
        components.add(Component.literal("§7生物蛋释放后，收服的生物会跟随你"));
        super.appendHoverText(stack, context, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("跟随之杖").withStyle(ChatFormatting.LIGHT_PURPLE);
    }
}
