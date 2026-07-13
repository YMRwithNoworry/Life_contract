package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.alku.life_contract.follower.FollowerEvents;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class SoulContractItem extends Item {
    public static final String TAG_CONTRACT_MOD = "ContractMod";
    public static final String TAG_HEALTH_SACRIFICE = "LifeContractHealthSacrifice";

    private static final UUID HEALTH_SACRIFICE_MODIFIER_ID = UUID.fromString("93c1fab8-8e68-4df0-aeb8-a50b3817f58c");
    private static final String HEALTH_SACRIFICE_MODIFIER_NAME = "Life contract health sacrifice";
    private static final double HEALTH_COST_FACTOR = 0.03D;
    private static final double MINIMUM_MAX_HEALTH = 1.0D;

    public SoulContractItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (!(target instanceof Mob mob)) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 只能与非玩家生物签订契约！"));
            return InteractionResult.FAIL;
        }

        if (FollowerEvents.isAlliedWithPlayer(player, mob)) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 该生物已经属于你的阵营！"));
            return InteractionResult.FAIL;
        }

        double healthCost = target.getHealth() * HEALTH_COST_FACTOR;
        if (player.getMaxHealth() - healthCost < MINIMUM_MAX_HEALTH) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 你的生命上限不足以支付契约代价！"));
            return InteractionResult.FAIL;
        }

        addHealthSacrifice(player, healthCost);
        FollowerEvents.registerContractAlly(mob, player.getUUID());

        String entityName = target.hasCustomName() ? target.getCustomName().getString() : target.getName().getString();
        String formattedCost = String.format(Locale.ROOT, "%.2f", healthCost);
        player.sendSystemMessage(Component.literal("§a[生灵契约] §e" + entityName + " §f已与你的阵营结盟！"));
        player.sendSystemMessage(Component.literal("§c生命上限永久减少 " + formattedCost + " 点§7（目标当前生命值的 3%）"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.SOUL_FIRE_FLAME,
                    target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                    30, 0.5, 0.5, 0.5, 0.05);
            serverLevel.sendParticles(
                    ParticleTypes.ENCHANT,
                    player.getX(), player.getY() + 1, player.getZ(),
                    50, 0.5, 1.0, 0.5, 0.5);
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                    SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        stack.shrink(1);
        return InteractionResult.SUCCESS;
    }

    private static void addHealthSacrifice(Player player, double amount) {
        double currentSacrifice = player.getPersistentData().getDouble(TAG_HEALTH_SACRIFICE);
        player.getPersistentData().putDouble(TAG_HEALTH_SACRIFICE, currentSacrifice + amount);
        applyStoredHealthSacrifice(player);
    }

    public static void applyStoredHealthSacrifice(Player player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.removeModifier(HEALTH_SACRIFICE_MODIFIER_ID);
        double sacrifice = player.getPersistentData().getDouble(TAG_HEALTH_SACRIFICE);
        if (sacrifice > 0.0D) {
            maxHealth.addPermanentModifier(new AttributeModifier(
                    HEALTH_SACRIFICE_MODIFIER_ID,
                    HEALTH_SACRIFICE_MODIFIER_NAME,
                    -sacrifice,
                    AttributeModifier.Operation.ADDITION));
        }

        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§d[生灵契约]").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("§e右键非己方生物 §7- 使其与你的阵营结盟"));
        components.add(Component.literal("§7契约生物会跟随你，并攻击其他阵营"));
        components.add(Component.literal("§c代价: 永久失去目标当前生命值 3% 的生命上限"));
        components.add(Component.literal("§c一次性物品，使用后消失"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("生灵契约").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
    }
}
