package org.alku.airdrop.item;

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

public class FlareGunItem extends Item {
    public FlareGunItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.EPIC));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            double[] safePos = AirdropCommand.clampToBorder((ServerLevel) level, player.getX(), player.getZ());
            AirdropCommand.spawnAirdrop((ServerLevel) level, safePos[0], safePos[1]);
            level.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.PLAYERS, 3.0F, 1.0F);
            player.getCooldowns().addCooldown(this, 100);
        }
        return InteractionResultHolder.success(stack);
    }
}