package org.alku.airdrop.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.alku.airdrop.command.AirdropCommand;

public class DisposableFlareGunItem extends Item {
    public DisposableFlareGunItem() {
        super(new Properties().stacksTo(16).rarity(Rarity.COMMON));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            double[] safePos = AirdropCommand.clampToBorder((ServerLevel) level, player.getX(), player.getZ());
            double x = safePos[0];
            double z = safePos[1];

            AirdropCommand.spawnAirdrop((ServerLevel) level, x, z);

            level.playSound(null, x, player.getY(), z,
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.0F, 1.0F);

            Component msg = Component.literal(
                    String.format("§b[信号枪] §f玩家 %s 发射了信号! 空投将降落在: §a[%.0f, %.0f]",
                            player.getName().getString(), x, z));
            level.players().forEach(p -> p.sendSystemMessage(msg));

            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }

            player.getCooldowns().addCooldown(this, 20);
        }

        return InteractionResultHolder.success(stack);
    }
}