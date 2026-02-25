package org.alku.life_contract;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
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
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class SoulContractItem extends Item {
    public static final String TAG_CONTRACT_MOD = "ContractMod";
    
    public SoulContractItem() {
        super(new Item.Properties().stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }

        if (player.isShiftKeyDown()) {
            return cancelContract(player);
        }

        if (target instanceof Player) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 无法与玩家签订契约！"));
            return InteractionResult.FAIL;
        }

        if (!(target instanceof Mob)) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 只能与生物签订契约！"));
            return InteractionResult.FAIL;
        }

        String modId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).getNamespace();
        
        if (modId == null || modId.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 无法识别该生物的来源！"));
            return InteractionResult.FAIL;
        }

        player.getPersistentData().putString(TAG_CONTRACT_MOD, modId);
        ContractEvents.syncData(player);

        String entityName = target.hasCustomName() ? target.getCustomName().getString() : target.getName().getString();
        player.sendSystemMessage(Component.literal("§a[生灵契约] §f你与 §e" + entityName + " §f签订了契约！"));
        player.sendSystemMessage(Component.literal("§7契约模组: §b" + modId));
        player.sendSystemMessage(Component.literal("§7该模组的生物将不再敌视你，也不会伤害你。"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SOUL_FIRE_FLAME,
                target.getX(), target.getY() + target.getBbHeight() / 2, target.getZ(),
                30, 0.5, 0.5, 0.5, 0.05
            );
            serverLevel.sendParticles(
                ParticleTypes.ENCHANT,
                player.getX(), player.getY() + 1, player.getZ(),
                50, 0.5, 1.0, 0.5, 0.5
            );
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        stack.shrink(1);

        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!level.isClientSide && player.isShiftKeyDown()) {
            InteractionResult result = cancelContract(player);
            return new InteractionResultHolder<>(result, player.getItemInHand(hand));
        }
        return super.use(level, player, hand);
    }

    private InteractionResult cancelContract(Player player) {
        String currentMod = player.getPersistentData().getString(TAG_CONTRACT_MOD);
        
        if (currentMod == null || currentMod.isEmpty()) {
            player.sendSystemMessage(Component.literal("§c[生灵契约] 你当前没有签订任何契约！"));
            return InteractionResult.FAIL;
        }

        player.getPersistentData().remove(TAG_CONTRACT_MOD);
        ContractEvents.syncData(player);

        player.sendSystemMessage(Component.literal("§e[生灵契约] §f你已取消与 §b" + currentMod + " §f的契约！"));
        player.sendSystemMessage(Component.literal("§7该模组的生物将重新对你产生敌意。"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                ParticleTypes.SMOKE,
                player.getX(), player.getY() + 1, player.getZ(),
                30, 0.5, 0.5, 0.5, 0.05
            );
            serverLevel.playSound(null, player.getX(), player.getY(), player.getZ(),
                SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 1.0F, 1.0F);
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> components, TooltipFlag flag) {
        components.add(Component.literal("§d[生灵契约]").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE));
        components.add(Component.literal("§e右键生物 §7- 与该生物所属模组签订契约"));
        components.add(Component.literal("§eShift+右键 §7- 取消当前契约"));
        components.add(Component.literal("§7契约后，该模组的生物将不再敌视你"));
        components.add(Component.literal("§7也不会对你造成伤害和负面效果"));
        components.add(Component.literal("§c一次性物品，使用后消失"));
        super.appendHoverText(stack, level, components, flag);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal("生灵契约").withStyle(net.minecraft.ChatFormatting.LIGHT_PURPLE);
    }
}
