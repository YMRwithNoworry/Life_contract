package org.alku.life_contract;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class FacelessDeceiverMaskItem extends Item {

    public FacelessDeceiverMaskItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
        if (player.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.PASS;
        }
        
        if (!(target instanceof Mob targetMob)) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.only_mob"));
            return InteractionResult.FAIL;
        }
        
        String professionId = player.getPersistentData().getString("LifeContractProfession");
        if (professionId == null || professionId.isEmpty()) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.need_profession"));
            return InteractionResult.FAIL;
        }
        
        Profession profession = ProfessionConfig.getProfession(professionId);
        if (profession == null || !profession.isFacelessDeceiver()) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.only_faceless"));
            return InteractionResult.FAIL;
        }
        
        if (FacelessDeceiverSystem.hasContract(player)) {
            String existingEntityName = FacelessDeceiverSystem.getContractEntityName(player);
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.already_contracted", existingEntityName));
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.cannot_change"));
            return InteractionResult.FAIL;
        }
        
        if (targetMob.getPersistentData().contains("FacelessDeceiverOwner")) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.already_owned"));
            return InteractionResult.FAIL;
        }
        
        if (targetMob.getPersistentData().contains("FollowerOwnerUUID")) {
            player.sendSystemMessage(Component.translatable("faceless_deceiver.mask.has_master"));
            return InteractionResult.FAIL;
        }
        
        FacelessDeceiverSystem.establishContract(serverPlayer, targetMob);
        
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltipComponents, TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, tooltipComponents, isAdvanced);
        
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.contract").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.effects").withStyle(ChatFormatting.YELLOW));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.attribute").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.ability").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.sync").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.cost").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.death").withStyle(ChatFormatting.RED));
        tooltipComponents.add(Component.literal(""));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.rules").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.single").withStyle(ChatFormatting.GRAY));
        tooltipComponents.add(Component.translatable("item.life_contract.faceless_deceiver_mask.tooltip.no_change").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }
}
