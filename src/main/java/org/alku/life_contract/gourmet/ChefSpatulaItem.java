package org.alku.life_contract.gourmet;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ContractEvents;

import java.util.List;
import java.util.UUID;

public class ChefSpatulaItem extends SwordItem {

    private static final UUID ATTACK_SPEED_UUID = UUID.fromString("e5f6a7b8-c9d0-1234-efab-567890123456");
    private static final UUID ARMOR_UUID = UUID.fromString("f6a7b8c9-d0e1-2345-fabc-678901234567");

    public ChefSpatulaItem(Properties properties) {
        super(Tiers.IRON, 3, -2.4f, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(Component.literal("§6[专属厨具] 主厨锅铲"));
        tooltip.add(Component.literal("§7伤害: §f7 §7| 攻击速度: §f1.4"));
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("§e被动效果:"));
        tooltip.add(Component.literal("§7- 手持时烹饪速度 §a+100%"));
        tooltip.add(Component.literal("§7- 普攻附加 §c腻味 §7debuff"));
        tooltip.add(Component.literal("§7- 右键放置营火（最多2个）"));
        tooltip.add(Component.literal("§7- 潜行+右键附魔锋味（伤害+50%）"));
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof ServerPlayer player) {
            if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
                return super.hurtEnemy(stack, target, attacker);
            }

            applyGreasyDebuff(player, target);

            if (player.getPersistentData().getBoolean(GourmetSystem.TAG_FLAVOR_ENCHANT_ACTIVE)) {
                float bonusDamage = (float) (target.getMaxHealth() * 0.1);
                target.hurt(player.level().damageSources().playerAttack(player), bonusDamage);

                if (player.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.FLAME,
                        target.getX(), target.getY() + 1, target.getZ(), 10, 0.5, 0.5, 0.5, 0.05);
                }
            }
        }

        return super.hurtEnemy(stack, target, attacker);
    }

    private void applyGreasyDebuff(ServerPlayer player, LivingEntity target) {
        int stacks = player.getPersistentData().getInt(GourmetSystem.TAG_GREASY_STACKS);

        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 60, 0, false, true));

        if (stacks >= 3) {
            var armorAttr = target.getAttribute(Attributes.ARMOR);
            if (armorAttr != null) {
                armorAttr.addPermanentModifier(new AttributeModifier(
                    ARMOR_UUID, "greasy_armor_reduction",
                    -0.15, AttributeModifier.Operation.MULTIPLY_TOTAL
                ));
            }
        }

        if (stacks >= 5) {
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0, false, true));
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return new InteractionResultHolder<>(InteractionResult.PASS, stack);
        }

        if (player.isShiftKeyDown()) {
            return activateFlavorEnchant(player, stack);
        } else {
            return placeCampfire(player, stack);
        }
    }

    private InteractionResultHolder<ItemStack> activateFlavorEnchant(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        for (int i = 0; i < serverPlayer.getInventory().getContainerSize(); i++) {
            ItemStack foodStack = serverPlayer.getInventory().getItem(i);
            if (!foodStack.isEmpty() && foodStack.getItem().isEdible()) {
                foodStack.shrink(1);

                serverPlayer.getPersistentData().putBoolean(GourmetSystem.TAG_FLAVOR_ENCHANT_ACTIVE, true);
                serverPlayer.getPersistentData().putInt(GourmetSystem.TAG_FLAVOR_ENCHANT_COUNT, 5);
                serverPlayer.getPersistentData().putLong(GourmetSystem.TAG_FLAVOR_ENCHANT_EXPIRE,
                    serverPlayer.getServer().getTickCount() + 200);

                serverPlayer.sendSystemMessage(Component.literal("§e[锋味] 锅铲附魔成功！后续5次攻击伤害+50%"));

                if (serverPlayer.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.ENCHANTED_HIT,
                        serverPlayer.getX(), serverPlayer.getY() + 1, serverPlayer.getZ(),
                        30, 0.5, 0.5, 0.5, 0.1);
                }

                return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
            }
        }

        serverPlayer.sendSystemMessage(Component.literal("§c[锋味] 需要消耗1份食材！"));
        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private InteractionResultHolder<ItemStack> placeCampfire(Player player, ItemStack stack) {
        if (player.level().isClientSide) {
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        ServerPlayer serverPlayer = (ServerPlayer) player;

        int campfireCount = countPlayerCampfires(serverPlayer);
        if (campfireCount >= 2) {
            serverPlayer.sendSystemMessage(Component.literal("§c[锅铲] 最多同时存在2个营火！"));
            return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
        }

        BlockPos placePos = player.blockPosition();
        BlockState campfireState = Blocks.CAMPFIRE.defaultBlockState()
            .setValue(CampfireBlock.LIT, true);

        Level level = player.level();
        if (level.isEmptyBlock(placePos) || level.getBlockState(placePos).canBeReplaced()) {
            level.setBlockAndUpdate(placePos, campfireState);
            level.playSound(null, placePos, SoundEvents.FIRE_AMBIENT, SoundSource.PLAYERS, 1.0f, 1.0f);

            saveCampfirePosition(serverPlayer, placePos);

            serverPlayer.sendSystemMessage(Component.literal("§a[锅铲] 放置营火成功！"));

            return new InteractionResultHolder<>(InteractionResult.SUCCESS, stack);
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private int countPlayerCampfires(ServerPlayer player) {
        String positions = player.getPersistentData().getString(GourmetSystem.TAG_CAMPFIRE_POSITIONS);
        if (positions.isEmpty()) return 0;

        String[] posArray = positions.split(";");
        int count = 0;

        for (String pos : posArray) {
            if (!pos.isEmpty()) {
                String[] coords = pos.split(",");
                if (coords.length == 3) {
                    try {
                        int x = Integer.parseInt(coords[0]);
                        int y = Integer.parseInt(coords[1]);
                        int z = Integer.parseInt(coords[2]);
                        BlockPos blockPos = new BlockPos(x, y, z);

                        if (player.level().getBlockState(blockPos).getBlock() == Blocks.CAMPFIRE) {
                            count++;
                        }
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
        }

        return count;
    }

    private void saveCampfirePosition(ServerPlayer player, BlockPos pos) {
        String positions = player.getPersistentData().getString(GourmetSystem.TAG_CAMPFIRE_POSITIONS);
        String newPos = pos.getX() + "," + pos.getY() + "," + pos.getZ();

        if (positions.isEmpty()) {
            positions = newPos;
        } else {
            positions = positions + ";" + newPos;
        }

        player.getPersistentData().putString(GourmetSystem.TAG_CAMPFIRE_POSITIONS, positions);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (player == null) return InteractionResult.PASS;

        if (!GourmetSystem.isGourmet(player) || GourmetSystem.isWearingArmor(player)) {
            return InteractionResult.PASS;
        }

        if (level.getBlockState(pos).getBlock() instanceof CampfireBlock ||
            level.getBlockState(pos).getBlock() == Blocks.FURNACE ||
            level.getBlockState(pos).getBlock() == Blocks.SMOKER ||
            level.getBlockState(pos).getBlock() == Blocks.BREWING_STAND) {

            if (!level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(Component.literal("§a[锅铲] 烹饪速度已提升！"));
            }

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.PASS;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
