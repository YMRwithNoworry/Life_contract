package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.List;

public class SealedBowItem extends BowItem {
    private static final String TAG_SEALED_BOW = "SealedBow";

    public SealedBowItem() {
        super(new Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entityLiving, int timeLeft) {
        if (!(entityLiving instanceof Player player)) {
            return;
        }

        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            String professionId = ContractEvents.getEffectiveProfessionId(serverPlayer);
            if (professionId == null || professionId.isEmpty()) {
                serverPlayer.displayClientMessage(
                    Component.literal("§c[尘封之弓] §r你没有职业，无法使用这个武器！"),
                    true
                );
                return;
            }
            
            Profession profession = ProfessionConfig.getProfession(professionId);
            if (profession == null || !profession.hasDonkBowAbility()) {
                serverPlayer.displayClientMessage(
                    Component.literal("§c[尘封之弓] §r只有donk职业才能使用这个武器！"),
                    true
                );
                return;
            }
        }

        ItemStack arrowStack = player.getProjectile(stack);

        int charge = this.getUseDuration(stack) - timeLeft;
        charge = net.minecraftforge.event.ForgeEventFactory.onArrowLoose(stack, level, player, charge, !arrowStack.isEmpty());
        if (charge < 0) return;

        float velocity = getPowerForTime(charge);
        if (velocity < 0.1) return;

        if (!level.isClientSide) {
            if (arrowStack.isEmpty()) {
                return;
            }

            AbstractArrow arrow = createArrow(level, player, stack, arrowStack, velocity);
            arrow.setCritArrow(velocity >= 1.0F);

            if (arrowStack.getItem() == Items.SPECTRAL_ARROW) {
                arrow = new net.minecraft.world.entity.projectile.SpectralArrow(level, player);
            } else if (arrowStack.getItem() == Items.TIPPED_ARROW) {
                arrow = new net.minecraft.world.entity.projectile.Arrow(level, player);
                ((net.minecraft.world.entity.projectile.Arrow) arrow).setEffectsFromItem(arrowStack);
            }

            arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 3.0F, 1.0F);

            if (player.getAbilities().instabuild) {
                arrow.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
            } else {
                arrow.pickup = AbstractArrow.Pickup.ALLOWED;
                if (!arrowStack.isEmpty()) {
                    arrowStack.shrink(1);
                    if (arrowStack.isEmpty()) {
                        player.getInventory().removeItem(arrowStack);
                    }
                }
            }

            level.addFreshEntity(arrow);
        }

        level.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.ARROW_SHOOT, SoundSource.PLAYERS, 1.0F,
                1.0F / (level.getRandom().nextFloat() * 0.4F + 1.2F) + velocity * 0.5F);
    }

    private AbstractArrow createArrow(Level level, Player player, ItemStack bowStack, ItemStack arrowStack, float velocity) {
        AbstractArrow arrow;
        
        if (arrowStack.getItem() == Items.SPECTRAL_ARROW) {
            arrow = new net.minecraft.world.entity.projectile.SpectralArrow(level, player);
        } else {
            arrow = new net.minecraft.world.entity.projectile.Arrow(level, player);
            if (arrowStack.getItem() == Items.TIPPED_ARROW) {
                ((net.minecraft.world.entity.projectile.Arrow) arrow).setEffectsFromItem(arrowStack);
            }
        }

        arrow.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, velocity * 3.0F, 1.0F);

        return arrow;
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§7尘封的弓 §r§7- donk初始武器"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e✦ 无限耐久 §7- 永不损坏"));
        tooltip.add(Component.literal("§c✗ 需要箭矢 §7- 必须消耗箭矢才能发射"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§b★ 可用于合成颗秒之弓"));
    }

    @Override
    public ItemStack getDefaultInstance() {
        ItemStack stack = super.getDefaultInstance();
        stack.getOrCreateTag().putBoolean(TAG_SEALED_BOW, true);
        return stack;
    }

    public static boolean isSealedBow(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(TAG_SEALED_BOW);
    }

    @Override
    public boolean isDamageable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }
}
