package org.alku.life_contract.gourmet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.alku.life_contract.ContractEvents;

import java.util.List;

public class GourmetSkills {

    public static void useEmergencyStirFry(ServerPlayer player) {
        if (GourmetSystem.isInGodChefMode(player)) {
            executeEmergencyStirFry(player, false);
            return;
        }

        int cooldown = player.getPersistentData().getInt(GourmetSystem.TAG_EMERGENCY_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[应急快炒] 冷却中！"));
            return;
        }

        int umami = GourmetSystem.getUmami(player);
        if (umami < 20) {
            player.sendSystemMessage(Component.literal("§c[应急快炒] 鲜味值不足！需要20点"));
            return;
        }

        boolean hasIngredient = false;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem().isEdible()) {
                hasIngredient = true;
                break;
            }
        }

        executeEmergencyStirFry(player, hasIngredient);

        if (!hasIngredient) {
            GourmetSystem.addUmami(player, -20);
        }

        player.getPersistentData().putInt(GourmetSystem.TAG_EMERGENCY_COOLDOWN, 160);
    }

    private static void executeEmergencyStirFry(ServerPlayer player, boolean hasIngredient) {
        player.heal(10);
        player.getFoodData().eat(Items.COOKED_BEEF, new ItemStack(Items.COOKED_BEEF));

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 1, false, true));

        if (hasIngredient) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true));
            player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0, false, true));

            List<Player> nearbyAllies = player.level().getEntitiesOfClass(
                Player.class,
                player.getBoundingBox().inflate(5),
                p -> p != player && ContractEvents.isSameTeam(player, p)
            );

            for (Player ally : nearbyAllies) {
                ally.heal(5);
                ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true));
            }
        }

        int umami = GourmetSystem.getUmami(player);
        if (umami >= 200) {
            ItemStack dish1 = new ItemStack(GourmetItems.EMERGENCY_DISH.get());
            ItemStack dish2 = new ItemStack(GourmetItems.EMERGENCY_DISH.get());
            GourmetSystem.addStoredFood(player, dish1);
            GourmetSystem.addStoredFood(player, dish2);
            player.sendSystemMessage(Component.literal("§a[应急快炒] 满鲜味值！额外生成2份应急料理！"));
        }

        player.sendSystemMessage(Component.literal("§a[应急快炒] 生成应急料理！"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER,
                player.getX(), player.getY() + 1, player.getZ(), 20, 1, 0.5, 1, 0.1);
        }

        GourmetSystem.onSkillTrigger(player);
    }

    public static void useFlavorBomb(ServerPlayer player, double x, double y, double z) {
        if (GourmetSystem.isInGodChefMode(player)) {
            executeFlavorBomb(player, x, y, z);
            return;
        }

        int cooldown = player.getPersistentData().getInt(GourmetSystem.TAG_FLAVOR_BOMB_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[风味爆弹] 冷却中！"));
            return;
        }

        int umami = GourmetSystem.getUmami(player);
        if (umami < 30) {
            player.sendSystemMessage(Component.literal("§c[风味爆弹] 鲜味值不足！需要30点"));
            return;
        }

        GourmetSystem.addUmami(player, -30);
        executeFlavorBomb(player, x, y, z);
        player.getPersistentData().putInt(GourmetSystem.TAG_FLAVOR_BOMB_COOLDOWN, 240);
    }

    private static void executeFlavorBomb(ServerPlayer player, double x, double y, double z) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.EXPLOSION,
                x, y + 1, z, 1, 0, 0, 0, 0);

            serverLevel.sendParticles(ParticleTypes.FLAME,
                x, y + 1, z, 50, 2, 1, 2, 0.1);
        }

        List<LivingEntity> enemies = player.level().getEntitiesOfClass(
            LivingEntity.class,
            new AABB(x - 4, y - 2, z - 4, x + 4, y + 4, z + 4),
            e -> e != player && e.isAlive()
        );

        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 160, 2, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 160, 0, false, true));

            int greasyStacks = player.getPersistentData().getInt(GourmetSystem.TAG_GREASY_STACKS);
            player.getPersistentData().putInt(GourmetSystem.TAG_GREASY_STACKS, greasyStacks + 1);
        }

        List<Player> allies = player.level().getEntitiesOfClass(
            Player.class,
            new AABB(x - 4, y - 2, z - 4, x + 4, y + 4, z + 4),
            p -> ContractEvents.isSameTeam(player, p)
        );

        for (Player ally : allies) {
            ally.heal(3);
            ally.getFoodData().setFoodLevel(20);
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 160, 0, false, true));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 0, false, true));
        }

        player.sendSystemMessage(Component.literal("§6[风味爆弹] 释放成功！"));

        player.level().playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.0f, 1.2f);

        GourmetSystem.onSkillTrigger(player);
    }

    public static void useWarmFeed(ServerPlayer player, boolean isGroupMode) {
        if (GourmetSystem.isInGodChefMode(player)) {
            executeWarmFeed(player, isGroupMode);
            return;
        }

        int cooldown = player.getPersistentData().getInt(GourmetSystem.TAG_WARM_FEED_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[暖心投喂] 冷却中！"));
            return;
        }

        int umami = GourmetSystem.getUmami(player);
        int cost = isGroupMode ? 45 : 25;

        if (umami < cost) {
            player.sendSystemMessage(Component.literal("§c[暖心投喂] 鲜味值不足！需要" + cost + "点"));
            return;
        }

        GourmetSystem.addUmami(player, -cost);
        executeWarmFeed(player, isGroupMode);

        int cd = isGroupMode ? 500 : 200;
        player.getPersistentData().putInt(GourmetSystem.TAG_WARM_FEED_COOLDOWN, cd);
    }

    private static void executeWarmFeed(ServerPlayer player, boolean isGroupMode) {
        if (isGroupMode) {
            List<Player> allies = player.level().getEntitiesOfClass(
                Player.class,
                player.getBoundingBox().inflate(10),
                p -> ContractEvents.isSameTeam(player, p)
            );

            for (Player ally : allies) {
                feedAlly(player, ally, false);
            }

            player.sendSystemMessage(Component.literal("§a[暖心投喂] 群体投喂成功！"));
        } else {
            Player nearestAlly = null;
            double nearestDist = 15;

            List<Player> allies = player.level().getEntitiesOfClass(
                Player.class,
                player.getBoundingBox().inflate(15),
                p -> p != player && ContractEvents.isSameTeam(player, p)
            );

            for (Player ally : allies) {
                double dist = player.distanceTo(ally);
                if (dist < nearestDist) {
                    nearestDist = dist;
                    nearestAlly = ally;
                }
            }

            if (nearestAlly != null) {
                boolean isEmergency = nearestAlly.getHealth() < nearestAlly.getMaxHealth() * 0.2;
                feedAlly(player, nearestAlly, isEmergency);

                if (isEmergency) {
                    player.sendSystemMessage(Component.literal("§c[急救投喂] 目标生命值拉至50%，获得5秒无敌！"));
                    nearestAlly.sendSystemMessage(Component.literal("§c[急救投喂] 生命值恢复，获得5秒无敌！"));
                } else {
                    player.sendSystemMessage(Component.literal("§a[暖心投喂] 投喂成功！"));
                }
            } else {
                player.sendSystemMessage(Component.literal("§c[暖心投喂] 周围没有队友！"));
            }
        }

        GourmetSystem.onSkillTrigger(player);
    }

    private static void feedAlly(ServerPlayer gourmet, Player ally, boolean isEmergency) {
        if (isEmergency) {
            ally.setHealth(ally.getMaxHealth() * 0.5f);
            ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 4, false, true));
        } else {
            ally.heal(12);
            ally.getFoodData().eat(Items.COOKED_BEEF, new ItemStack(Items.COOKED_BEEF));
        }

        ally.removeAllEffects();
        ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 200, 0, false, true));

        SeasoningBoxItem.onFeedAlly(gourmet, (ServerPlayer) ally);

        GourmetSystem.onFeedAlly(gourmet);

        if (ally.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                ally.getX(), ally.getY() + 1, ally.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public static void useGodChefDescent(ServerPlayer player) {
        int cooldown = player.getPersistentData().getInt(GourmetSystem.TAG_GOD_CHEF_COOLDOWN);
        if (cooldown > 0) {
            player.sendSystemMessage(Component.literal("§c[厨神降临] 冷却中！"));
            return;
        }

        int umami = GourmetSystem.getUmami(player);
        if (umami < 150) {
            player.sendSystemMessage(Component.literal("§c[厨神降临] 鲜味值不足！需要150点"));
            return;
        }

        int ingredientTypes = 0;
        boolean[] types = new boolean[4];

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem().isEdible()) {
                if (!types[0]) { types[0] = true; ingredientTypes++; }
            }
            if (stack.getItem() == Items.GOLDEN_APPLE || stack.getItem() == Items.ENCHANTED_GOLDEN_APPLE) {
                if (!types[1]) { types[1] = true; ingredientTypes++; }
            }
            if (stack.getItem() == Items.BLAZE_POWDER || stack.getItem() == Items.BLAZE_ROD) {
                if (!types[2]) { types[2] = true; ingredientTypes++; }
            }
            if (stack.getItem() == Items.DRAGON_BREATH) {
                if (!types[3]) { types[3] = true; ingredientTypes++; }
            }
        }

        if (ingredientTypes < 2) {
            player.sendSystemMessage(Component.literal("§c[厨神降临] 需要至少2种不同食材！"));
            return;
        }

        GourmetSystem.setUmami(player, 0);
        GourmetSystem.activateGodChefMode(player);

        executeGodChefDescent(player);
    }

    private static void executeGodChefDescent(ServerPlayer player) {
        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(), 100, 8, 2, 8, 0.1);
        }

        List<Player> allies = player.level().getEntitiesOfClass(
            Player.class,
            player.getBoundingBox().inflate(16),
            p -> ContractEvents.isSameTeam(player, p)
        );

        for (Player ally : allies) {
            if (ally.getHealth() < ally.getMaxHealth() * 0.2) {
                ally.setHealth(ally.getMaxHealth());
                ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 160, 4, false, true));
            }

            ally.getFoodData().setFoodLevel(20);
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 400, 1, false, true));
            ally.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 400, 1, false, true));
            ally.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 400, 1, false, true));
        }

        List<LivingEntity> enemies = player.level().getEntitiesOfClass(
            LivingEntity.class,
            player.getBoundingBox().inflate(16),
            e -> e != player && e.isAlive() && !(e instanceof Player p && ContractEvents.isSameTeam(player, p))
        );

        for (LivingEntity enemy : enemies) {
            enemy.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 400, 1, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 400, 1, false, true));
            enemy.addEffect(new MobEffectInstance(MobEffects.WITHER, 400, 0, false, true));
        }

        player.sendSystemMessage(Component.literal("§6[厨神降临] 传世盛宴开启！"));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
            SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
    }
}
