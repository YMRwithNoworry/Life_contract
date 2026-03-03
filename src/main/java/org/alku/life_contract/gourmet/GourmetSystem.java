package org.alku.life_contract.gourmet;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.profession.Profession;
import org.alku.life_contract.profession.ProfessionConfig;

import java.util.*;

@Mod.EventBusSubscriber(modid = Life_contract.MODID)
public class GourmetSystem {

    public static final String TAG_UMAMI = "GourmetUmami";
    public static final String TAG_MAX_UMAMI = "GourmetMaxUmami";
    public static final String TAG_LAST_COMBAT_TIME = "GourmetLastCombatTime";
    public static final String TAG_CAMPFIRE_POSITIONS = "GourmetCampfirePositions";
    public static final String TAG_FLAVOR_ENCHANT_ACTIVE = "GourmetFlavorEnchantActive";
    public static final String TAG_FLAVOR_ENCHANT_COUNT = "GourmetFlavorEnchantCount";
    public static final String TAG_FLAVOR_ENCHANT_EXPIRE = "GourmetFlavorEnchantExpire";
    public static final String TAG_DYING_SOUL_COOLDOWN = "GourmetDyingSoulCooldown";
    public static final String TAG_DYING_SOUL_ACTIVE = "GourmetDyingSoulActive";
    public static final String TAG_EMPTY_STOMACH = "GourmetEmptyStomach";
    public static final String TAG_EMPTY_STOMACH_END = "GourmetEmptyStomachEnd";
    public static final String TAG_GOD_CHEF_MODE = "GourmetGodChefMode";
    public static final String TAG_GOD_CHEF_END_TIME = "GourmetGodChefEndTime";
    public static final String TAG_GOD_CHEF_COOLDOWN = "GourmetGodChefCooldown";
    public static final String TAG_STORED_FOODS = "GourmetStoredFoods";
    public static final String TAG_SPECIAL_FOODS = "GourmetSpecialFoods";
    public static final String TAG_LAST_AUTO_EAT = "GourmetLastAutoEat";
    public static final String TAG_LAST_RAW_CONVERT = "GourmetLastRawConvert";
    public static final String TAG_EMERGENCY_COOLDOWN = "GourmetEmergencyCooldown";
    public static final String TAG_FLAVOR_BOMB_COOLDOWN = "GourmetFlavorBombCooldown";
    public static final String TAG_WARM_FEED_COOLDOWN = "GourmetWarmFeedCooldown";
    public static final String TAG_LIFE_SAVED = "GourmetLifeSaved";
    public static final String TAG_LIFE_SAVE_COOLDOWN = "GourmetLifeSaveCooldown";
    public static final String TAG_GREASY_STACKS = "GourmetGreasyStacks";
    public static final String TAG_GREASY_EXPIRE = "GourmetGreasyExpire";
    public static final String TAG_NO_HEAL_EXPIRE = "GourmetNoHealExpire";
    public static final String TAG_SEASONING_NEGATIVE_COOLDOWN = "GourmetSeasoningNegativeCooldown";
    public static final String TAG_PERMANENT_HEALTH_BONUS = "GourmetPermanentHealthBonus";
    public static final String TAG_DAILY_LEGENDARY_MEAL = "GourmetDailyLegendaryMeal";

    public static final int UMAMI_MAX = 200;
    public static final int UMAMI_INITIAL = 200;
    public static final int COMBAT_EXPIRE_TICKS = 100;
    public static final int NATURAL_REGEN_RATE = 20;
    public static final int NATURAL_REGEN_AMOUNT = 2;
    public static final int FOOD_EAT_UMAMI = 10;
    public static final int COOKING_NORMAL_UMAMI = 20;
    public static final int COOKING_RARE_UMAMI = 50;
    public static final int COOKING_PERFECT_BONUS = 30;
    public static final int KILL_WITH_SPATULA_UMAMI = 15;
    public static final int FEED_ALLY_UMAMI = 20;
    public static final int SKILL_TRIGGER_UMAMI = 10;
    public static final int DODGE_UMAMI = 10;
    public static final int SEASONING_NEGATIVE_COOLDOWN = 400;
    public static final int LIFE_SAVE_COOLDOWN = 900;
    public static final int DYING_SOUL_COOLDOWN = 1200;

    private static final UUID DAMAGE_REDUCTION_UUID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID MOVE_SPEED_UUID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID ATTACK_DAMAGE_UUID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID PERMANENT_HEALTH_UUID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");

    private static final Map<UUID, Long> LAST_DODGE_TIME = new HashMap<>();

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        net.minecraft.server.MinecraftServer server = net.minecraftforge.server.ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        int tickCount = server.getTickCount();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            tickGourmet(player, tickCount);
        }
    }

    private static void tickGourmet(ServerPlayer player, int tickCount) {
        if (!isGourmet(player)) return;

        if (isWearingArmor(player)) {
            clearAllGourmetEffects(player);
            return;
        }

        tickCooldowns(player);
        tickUmamiNaturalRegen(player, tickCount);
        tickUmamiTierBonuses(player);
        tickDyingSoul(player);
        tickEmptyStomach(player, tickCount);
        tickGodChefMode(player, tickCount);
        tickFlavorEnchant(player, tickCount);
        tickGreasyDebuff(player, tickCount);
        tickNoHealDebuff(player, tickCount);
        tickAutoEat(player, tickCount);
        tickRawConvert(player, tickCount);
        tickHungerManagement(player);

        if (tickCount % 5 == 0) {
            syncClientState(player);
        }
    }

    private static void tickCooldowns(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        int emergencyCd = data.getInt(TAG_EMERGENCY_COOLDOWN);
        if (emergencyCd > 0) data.putInt(TAG_EMERGENCY_COOLDOWN, emergencyCd - 1);

        int bombCd = data.getInt(TAG_FLAVOR_BOMB_COOLDOWN);
        if (bombCd > 0) data.putInt(TAG_FLAVOR_BOMB_COOLDOWN, bombCd - 1);

        int feedCd = data.getInt(TAG_WARM_FEED_COOLDOWN);
        if (feedCd > 0) data.putInt(TAG_WARM_FEED_COOLDOWN, feedCd - 1);

        int lifeSaveCd = data.getInt(TAG_LIFE_SAVE_COOLDOWN);
        if (lifeSaveCd > 0) data.putInt(TAG_LIFE_SAVE_COOLDOWN, lifeSaveCd - 1);

        int dyingSoulCd = data.getInt(TAG_DYING_SOUL_COOLDOWN);
        if (dyingSoulCd > 0) data.putInt(TAG_DYING_SOUL_COOLDOWN, dyingSoulCd - 1);

        int godChefCd = data.getInt(TAG_GOD_CHEF_COOLDOWN);
        if (godChefCd > 0) data.putInt(TAG_GOD_CHEF_COOLDOWN, godChefCd - 1);

        int seasoningCd = data.getInt(TAG_SEASONING_NEGATIVE_COOLDOWN);
        if (seasoningCd > 0) data.putInt(TAG_SEASONING_NEGATIVE_COOLDOWN, seasoningCd - 1);
    }

    private static void tickUmamiNaturalRegen(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();
        int lastCombat = data.getInt(TAG_LAST_COMBAT_TIME);

        if (tickCount - lastCombat > COMBAT_EXPIRE_TICKS) {
            if (tickCount % NATURAL_REGEN_RATE == 0) {
                int currentUmami = getUmami(player);
                if (currentUmami < UMAMI_MAX) {
                    int newUmami = Math.min(UMAMI_MAX, currentUmami + NATURAL_REGEN_AMOUNT);
                    setUmami(player, newUmami);
                }
            }
        }

        int allyGourmetCount = countAllyGourmetBuff(player);
        if (allyGourmetCount > 0 && tickCount % 20 == 0) {
            int currentUmami = getUmami(player);
            int newUmami = Math.min(UMAMI_MAX, currentUmami + allyGourmetCount);
            setUmami(player, newUmami);
        }
    }

    private static void tickUmamiTierBonuses(ServerPlayer player) {
        int umami = getUmami(player);
        int tier = getUmamiTier(umami);

        applyDamageReduction(player, tier);
        applyMoveSpeedBonus(player, tier);
        applyTrueDamageBonus(player, tier);
    }

    private static void applyDamageReduction(ServerPlayer player, int tier) {
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr == null) return;

        armorAttr.removeModifier(DAMAGE_REDUCTION_UUID);

        double reduction = 0;
        if (tier >= 2) reduction = 0.10;
        if (tier >= 3) reduction = 0.15;
        if (tier >= 4) reduction = 0.20;

        if (reduction > 0) {
            armorAttr.addPermanentModifier(new AttributeModifier(
                DAMAGE_REDUCTION_UUID, "gourmet_damage_reduction",
                reduction, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void applyMoveSpeedBonus(ServerPlayer player, int tier) {
        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) return;

        speedAttr.removeModifier(MOVE_SPEED_UUID);

        double bonus = 0;
        if (tier >= 2) bonus = 0.10;
        if (tier >= 3) bonus = 0.15;
        if (tier >= 4) bonus = 0.20;

        if (bonus > 0) {
            speedAttr.addPermanentModifier(new AttributeModifier(
                MOVE_SPEED_UUID, "gourmet_move_speed",
                bonus, AttributeModifier.Operation.MULTIPLY_TOTAL
            ));
        }
    }

    private static void applyTrueDamageBonus(ServerPlayer player, int tier) {
        var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr == null) return;

        damageAttr.removeModifier(ATTACK_DAMAGE_UUID);

        if (tier >= 4) {
            int umami = getUmami(player);
            int trueDamage = umami / 10;
            damageAttr.addPermanentModifier(new AttributeModifier(
                ATTACK_DAMAGE_UUID, "gourmet_true_damage",
                trueDamage, AttributeModifier.Operation.ADDITION
            ));
        }
    }

    private static void tickDyingSoul(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_DYING_SOUL_ACTIVE)) {
            if (player.getHealth() > player.getMaxHealth() * 0.15) {
                data.putBoolean(TAG_DYING_SOUL_ACTIVE, false);
            }
        }

        if (!data.getBoolean(TAG_DYING_SOUL_ACTIVE) && player.getHealth() <= player.getMaxHealth() * 0.15) {
            int cooldown = data.getInt(TAG_DYING_SOUL_COOLDOWN);
            if (cooldown <= 0) {
                int umami = getUmami(player);
                if (umami >= 50) {
                    triggerDyingSoul(player);
                }
            }
        }
    }

    private static void triggerDyingSoul(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();

        addUmami(player, -50);

        float healAmount = player.getMaxHealth() * 0.5f;
        player.setHealth(player.getHealth() + healAmount);

        player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 100, 3, false, true));

        data.putBoolean(TAG_DYING_SOUL_ACTIVE, true);
        data.putInt(TAG_DYING_SOUL_COOLDOWN, DYING_SOUL_COOLDOWN);

        player.sendSystemMessage(Component.literal("§c[濒死厨魂] 触发！恢复50%生命值，获得5秒抗性提升IV！"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                player.getX(), player.getY() + 1, player.getZ(), 50, 1, 1, 1, 0.2);
        }
    }

    private static void tickEmptyStomach(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_EMPTY_STOMACH)) {
            long endTime = data.getLong(TAG_EMPTY_STOMACH_END);
            if (tickCount >= endTime) {
                data.putBoolean(TAG_EMPTY_STOMACH, false);
                player.sendSystemMessage(Component.literal("§7[空腹状态] 结束"));
            }
        }
    }

    private static void tickGodChefMode(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_GOD_CHEF_MODE)) {
            long endTime = data.getLong(TAG_GOD_CHEF_END_TIME);
            if (tickCount >= endTime) {
                endGodChefMode(player);
            }
        }
    }

    private static void endGodChefMode(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_GOD_CHEF_MODE, false);

        player.getFoodData().setFoodLevel(12);
        data.putBoolean(TAG_EMPTY_STOMACH, true);
        data.putLong(TAG_EMPTY_STOMACH_END, player.getServer().getTickCount() + 80);

        player.sendSystemMessage(Component.literal("§6[厨神模式] 结束！进入空腹状态..."));
    }

    private static void tickFlavorEnchant(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_FLAVOR_ENCHANT_ACTIVE)) {
            long expireTime = data.getLong(TAG_FLAVOR_ENCHANT_EXPIRE);
            if (tickCount >= expireTime) {
                data.putBoolean(TAG_FLAVOR_ENCHANT_ACTIVE, false);
                data.putInt(TAG_FLAVOR_ENCHANT_COUNT, 0);
            }
        }
    }

    private static void tickGreasyDebuff(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        int stacks = data.getInt(TAG_GREASY_STACKS);
        if (stacks > 0) {
            long expireTime = data.getLong(TAG_GREASY_EXPIRE);
            if (tickCount >= expireTime) {
                data.putInt(TAG_GREASY_STACKS, 0);
            }
        }
    }

    private static void tickNoHealDebuff(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        if (data.contains(TAG_NO_HEAL_EXPIRE)) {
            long expireTime = data.getLong(TAG_NO_HEAL_EXPIRE);
            if (tickCount >= expireTime) {
                data.remove(TAG_NO_HEAL_EXPIRE);
            }
        }
    }

    private static void tickAutoEat(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        if (player.getFoodData().getFoodLevel() < 6) {
            int lastAutoEat = data.getInt(TAG_LAST_AUTO_EAT);
            if (tickCount - lastAutoEat >= 160) {
                ItemStack food = getStoredFood(player, 0);
                if (!food.isEmpty()) {
                    player.getFoodData().eat(food.getItem(), food);
                    removeStoredFood(player, 0);
                    data.putInt(TAG_LAST_AUTO_EAT, tickCount);
                    player.sendSystemMessage(Component.literal("§a[便携炖锅] 自动补充饥饿值！"));
                }
            }
        }

        if (player.getFoodData().getFoodLevel() >= 20) {
            int lastAutoEat = data.getInt(TAG_LAST_AUTO_EAT);
            if (tickCount - lastAutoEat >= 40) {
                ItemStack food = getStoredFood(player, 0);
                if (!food.isEmpty()) {
                    player.heal(2);
                    addUmami(player, 2);
                    removeStoredFood(player, 0);
                    data.putInt(TAG_LAST_AUTO_EAT, tickCount);
                }
            }
        }
    }

    private static void tickRawConvert(ServerPlayer player, int tickCount) {
        CompoundTag data = player.getPersistentData();

        int lastConvert = data.getInt(TAG_LAST_RAW_CONVERT);
        if (tickCount - lastConvert >= 100) {
            int converted = convertRawFood(player);
            if (converted > 0) {
                data.putInt(TAG_LAST_RAW_CONVERT, tickCount);
            }
        }
    }

    private static int convertRawFood(ServerPlayer player) {
        int converted = 0;
        int maxConvert = 4;

        for (int i = 0; i < player.getInventory().getContainerSize() && converted < maxConvert; i++) {
            ItemStack stack = player.getInventory().getItem(i);
            ItemStack cookedResult = getCookedResult(stack);
            if (!cookedResult.isEmpty()) {
                stack.shrink(1);
                if (!player.getInventory().add(cookedResult.copy())) {
                    player.drop(cookedResult.copy(), false);
                }
                converted++;
            }
        }

        return converted;
    }

    private static ItemStack getCookedResult(ItemStack raw) {
        if (raw.getItem() == Items.BEEF) return new ItemStack(Items.COOKED_BEEF);
        if (raw.getItem() == Items.PORKCHOP) return new ItemStack(Items.COOKED_PORKCHOP);
        if (raw.getItem() == Items.CHICKEN) return new ItemStack(Items.COOKED_CHICKEN);
        if (raw.getItem() == Items.MUTTON) return new ItemStack(Items.COOKED_MUTTON);
        if (raw.getItem() == Items.RABBIT) return new ItemStack(Items.COOKED_RABBIT);
        if (raw.getItem() == Items.COD) return new ItemStack(Items.COOKED_COD);
        if (raw.getItem() == Items.SALMON) return new ItemStack(Items.COOKED_SALMON);
        if (raw.getItem() == Items.POTATO) return new ItemStack(Items.BAKED_POTATO);
        return ItemStack.EMPTY;
    }

    private static void tickHungerManagement(ServerPlayer player) {
        if (player.getFoodData().getFoodLevel() < 12) {
            player.getFoodData().setFoodLevel(12);
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            if (!isGourmet(player) || isWearingArmor(player)) return;

            onGourmetAttack(player, event.getEntity());
        }

        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isGourmet(player) || isWearingArmor(player)) return;

            tryTriggerLifeSave(player, event);
        }
    }

    private static void onGourmetAttack(ServerPlayer player, LivingEntity target) {
        CompoundTag data = player.getPersistentData();

        if (data.getBoolean(TAG_FLAVOR_ENCHANT_ACTIVE)) {
            int count = data.getInt(TAG_FLAVOR_ENCHANT_COUNT);
            if (count > 0) {
                data.putInt(TAG_FLAVOR_ENCHANT_COUNT, count - 1);
                if (count <= 1) {
                    data.putBoolean(TAG_FLAVOR_ENCHANT_ACTIVE, false);
                }
            }
        }

        if (player.getMainHandItem().getItem() instanceof ChefSpatulaItem) {
            int stacks = data.getInt(TAG_GREASY_STACKS);
            if (stacks < 5) {
                stacks++;
                data.putInt(TAG_GREASY_STACKS, stacks);
                data.putLong(TAG_GREASY_EXPIRE, player.getServer().getTickCount() + 60);

                if (stacks >= 5) {
                    data.putLong(TAG_NO_HEAL_EXPIRE, player.getServer().getTickCount() + 100);
                    player.sendSystemMessage(Component.literal("§c[腻味] 目标无法回血！"));
                }
            }
        }
    }

    private static void tryTriggerLifeSave(ServerPlayer player, LivingAttackEvent event) {
        if (player.getHealth() > 1) return;

        CompoundTag data = player.getPersistentData();
        int cooldown = data.getInt(TAG_LIFE_SAVE_COOLDOWN);
        if (cooldown > 0) return;

        int foodCount = getStoredFoodCount(player);
        if (foodCount <= 0) return;

        consumeAllStoredFood(player);

        float healAmount = foodCount * 2;
        player.setHealth(Math.max(1, healAmount));
        data.putInt(TAG_LIFE_SAVE_COOLDOWN, LIFE_SAVE_COOLDOWN);

        player.sendSystemMessage(Component.literal("§a[便携炖锅] 消耗所有储存食物救命！"));

        if (player.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.HEART,
                player.getX(), player.getY() + 1, player.getZ(), 20, 1, 0.5, 1, 0.1);
        }
    }

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (!isGourmet(player) || isWearingArmor(player)) return;

            int umami = getUmami(player);
            int tier = getUmamiTier(umami);

            if (tier >= 3) {
                float dodgeChance = tier == 3 ? 0.10f : 0.20f;
                if (player.getRandom().nextFloat() < dodgeChance) {
                    event.setCanceled(true);
                    addUmami(player, DODGE_UMAMI);
                    player.sendSystemMessage(Component.literal("§b[闪避] 成功闪避攻击！"));

                    if (player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.DAMAGE_INDICATOR,
                            player.getX(), player.getY() + 1, player.getZ(), 10, 0.5, 0.5, 0.5, 0.1);
                    }
                    return;
                }
            }

            if (player.getFoodData().getFoodLevel() >= 20) {
                float reduction = 0.20f;
                event.setAmount(event.getAmount() * (1 - reduction));
            }
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            if (!isGourmet(player) || isWearingArmor(player)) return;

            if (player.getMainHandItem().getItem() instanceof ChefSpatulaItem) {
                addUmami(player, KILL_WITH_SPATULA_UMAMI);
                player.sendSystemMessage(Component.literal("§e[鲜味值] 击杀获得 " + KILL_WITH_SPATULA_UMAMI + " 点鲜味值！"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            if (isGourmet(player)) {
                ensureGourmetHasTools(player);
                if (getUmami(player) <= 0) {
                    setUmami(player, UMAMI_INITIAL);
                }
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
            if (isGourmet(player)) {
                ensureGourmetHasTools(player);
                setUmami(player, UMAMI_INITIAL);
                player.getPersistentData().putInt(TAG_DYING_SOUL_COOLDOWN, 0);
                player.getPersistentData().putInt(TAG_GOD_CHEF_COOLDOWN, 0);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        if (!event.isWasDeath()) return;

        Player original = event.getOriginal();
        Player newPlayer = event.getEntity();

        CompoundTag originalData = original.getPersistentData();
        CompoundTag newData = newPlayer.getPersistentData();

        if (originalData.contains(TAG_PERMANENT_HEALTH_BONUS)) {
            newData.putFloat(TAG_PERMANENT_HEALTH_BONUS, originalData.getFloat(TAG_PERMANENT_HEALTH_BONUS));
        }
    }

    public static boolean isGourmet(Player player) {
        String professionId = ContractEvents.getEffectiveProfessionId(player);
        if (professionId == null) return false;

        Profession profession = ProfessionConfig.getProfession(professionId);
        return profession != null && profession.isGourmet();
    }

    public static boolean isWearingArmor(Player player) {
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            if (slot.getType() == net.minecraft.world.entity.EquipmentSlot.Type.ARMOR) {
                ItemStack stack = player.getItemBySlot(slot);
                if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem) {
                    return true;
                }
            }
        }
        return false;
    }

    public static int getUmami(Player player) {
        return player.getPersistentData().getInt(TAG_UMAMI);
    }

    public static void setUmami(Player player, int value) {
        player.getPersistentData().putInt(TAG_UMAMI, Math.max(0, Math.min(UMAMI_MAX, value)));
    }

    public static void addUmami(Player player, int amount) {
        int current = getUmami(player);
        setUmami(player, current + amount);
    }

    public static int getUmamiTier(int umami) {
        if (umami >= 200) return 4;
        if (umami >= 120) return 3;
        if (umami >= 60) return 2;
        return 1;
    }

    public static float getFoodEffectMultiplier(Player player) {
        if (!isGourmet(player) || isWearingArmor(player)) return 1.0f;

        int umami = getUmami(player);
        int tier = getUmamiTier(umami);

        return switch (tier) {
            case 4 -> 3.0f;
            case 3 -> 2.5f;
            case 2 -> 2.0f;
            default -> 1.0f;
        };
    }

    public static float getTeamFoodBonus(Player player) {
        if (!isGourmet(player) || isWearingArmor(player)) return 0;

        int umami = getUmami(player);
        int tier = getUmamiTier(umami);

        return switch (tier) {
            case 4 -> 1.0f;
            case 3 -> 0.75f;
            default -> 0;
        };
    }

    private static void clearAllGourmetEffects(ServerPlayer player) {
        var armorAttr = player.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) armorAttr.removeModifier(DAMAGE_REDUCTION_UUID);

        var speedAttr = player.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr != null) speedAttr.removeModifier(MOVE_SPEED_UUID);

        var damageAttr = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (damageAttr != null) damageAttr.removeModifier(ATTACK_DAMAGE_UUID);
    }

    private static int countAllyGourmetBuff(Player player) {
        int count = 0;
        List<Player> nearbyPlayers = player.level().getEntitiesOfClass(
            Player.class,
            player.getBoundingBox().inflate(32),
            p -> p != player && ContractEvents.isSameTeam(player, p)
        );

        for (Player ally : nearbyPlayers) {
            if (isGourmet(ally)) {
                count++;
            }
        }

        return count;
    }

    private static void ensureGourmetHasTools(ServerPlayer player) {
        boolean hasSpatula = false;
        boolean hasPot = false;
        boolean hasSeasoning = false;

        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.getItem() instanceof ChefSpatulaItem) hasSpatula = true;
            if (stack.getItem() instanceof PortablePotItem) hasPot = true;
            if (stack.getItem() instanceof SeasoningBoxItem) hasSeasoning = true;
        }

        if (!hasSpatula) {
            ItemStack spatula = new ItemStack(GourmetItems.CHEF_SPATULA.get());
            if (!player.getInventory().add(spatula)) {
                player.drop(spatula, false);
            }
        }

        if (!hasPot) {
            ItemStack pot = new ItemStack(GourmetItems.PORTABLE_POT.get());
            if (!player.getInventory().add(pot)) {
                player.drop(pot, false);
            }
        }

        if (!hasSeasoning) {
            ItemStack seasoning = new ItemStack(GourmetItems.SEASONING_BOX.get());
            if (!player.getInventory().add(seasoning)) {
                player.drop(seasoning, false);
            }
        }
    }

    public static void addStoredFood(Player player, ItemStack food) {
        CompoundTag data = player.getPersistentData();
        ListTag foods = data.getList(TAG_STORED_FOODS, 10);

        if (foods.size() < 16) {
            CompoundTag foodTag = new CompoundTag();
            food.save(foodTag);
            foods.add(foodTag);
            data.put(TAG_STORED_FOODS, foods);
        }
    }

    public static ItemStack getStoredFood(Player player, int index) {
        CompoundTag data = player.getPersistentData();
        ListTag foods = data.getList(TAG_STORED_FOODS, 10);

        if (index >= 0 && index < foods.size()) {
            return ItemStack.of(foods.getCompound(index));
        }
        return ItemStack.EMPTY;
    }

    public static void removeStoredFood(Player player, int index) {
        CompoundTag data = player.getPersistentData();
        ListTag foods = data.getList(TAG_STORED_FOODS, 10);

        if (index >= 0 && index < foods.size()) {
            foods.remove(index);
            data.put(TAG_STORED_FOODS, foods);
        }
    }

    public static int getStoredFoodCount(Player player) {
        CompoundTag data = player.getPersistentData();
        ListTag foods = data.getList(TAG_STORED_FOODS, 10);
        return foods.size();
    }

    public static void consumeAllStoredFood(Player player) {
        CompoundTag data = player.getPersistentData();
        data.put(TAG_STORED_FOODS, new ListTag());
    }

    public static void addSpecialFood(Player player, ItemStack food) {
        CompoundTag data = player.getPersistentData();
        ListTag foods = data.getList(TAG_SPECIAL_FOODS, 10);

        if (foods.size() < 4) {
            CompoundTag foodTag = new CompoundTag();
            food.save(foodTag);
            foods.add(foodTag);
            data.put(TAG_SPECIAL_FOODS, foods);
        }
    }

    public static boolean isInGodChefMode(Player player) {
        return player.getPersistentData().getBoolean(TAG_GOD_CHEF_MODE);
    }

    public static void activateGodChefMode(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        data.putBoolean(TAG_GOD_CHEF_MODE, true);
        data.putLong(TAG_GOD_CHEF_END_TIME, player.getServer().getTickCount() + 400);
        data.putInt(TAG_GOD_CHEF_COOLDOWN, 1800);

        player.sendSystemMessage(Component.literal("§6[厨神降临] 厨神模式激活！"));
    }

    public static void addPermanentHealth(ServerPlayer player, float amount) {
        CompoundTag data = player.getPersistentData();
        float current = data.getFloat(TAG_PERMANENT_HEALTH_BONUS);
        float newBonus = current + amount;
        data.putFloat(TAG_PERMANENT_HEALTH_BONUS, newBonus);

        var healthAttr = player.getAttribute(Attributes.MAX_HEALTH);
        if (healthAttr != null) {
            healthAttr.removeModifier(PERMANENT_HEALTH_UUID);
            healthAttr.addPermanentModifier(new AttributeModifier(
                PERMANENT_HEALTH_UUID, "gourmet_permanent_health",
                newBonus, AttributeModifier.Operation.ADDITION
            ));
        }

        player.sendSystemMessage(Component.literal("§d[传世料理] 永久最大生命值 +" + (int)(amount * 2) + "！"));
    }

    private static void syncClientState(ServerPlayer player) {
        CompoundTag data = player.getPersistentData();
        int umami = getUmami(player);
        int emergencyCd = data.getInt(TAG_EMERGENCY_COOLDOWN);
        int bombCd = data.getInt(TAG_FLAVOR_BOMB_COOLDOWN);
        int feedCd = data.getInt(TAG_WARM_FEED_COOLDOWN);
        int godChefCd = data.getInt(TAG_GOD_CHEF_COOLDOWN);
        boolean godChefMode = data.getBoolean(TAG_GOD_CHEF_MODE);

        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
            new PacketSyncGourmetState(umami, emergencyCd, bombCd, feedCd, godChefCd, godChefMode));
    }

    public static void onFoodEaten(ServerPlayer player, ItemStack food) {
        if (!isGourmet(player) || isWearingArmor(player)) return;

        addUmami(player, FOOD_EAT_UMAMI);

        player.getPersistentData().putInt(TAG_LAST_COMBAT_TIME, player.getServer().getTickCount());
    }

    public static void onCooking(ServerPlayer player, boolean isRare, int ingredientCount) {
        if (!isGourmet(player) || isWearingArmor(player)) return;

        int umamiGain = isRare ? COOKING_RARE_UMAMI : COOKING_NORMAL_UMAMI;

        if (ingredientCount >= 3) {
            umamiGain += COOKING_PERFECT_BONUS;
        }

        addUmami(player, umamiGain);
        player.sendSystemMessage(Component.literal("§e[鲜味值] 烹饪获得 " + umamiGain + " 点鲜味值！"));
    }

    public static void onFeedAlly(ServerPlayer player) {
        if (!isGourmet(player) || isWearingArmor(player)) return;

        addUmami(player, FEED_ALLY_UMAMI);
    }

    public static void onSkillTrigger(ServerPlayer player) {
        if (!isGourmet(player) || isWearingArmor(player)) return;

        addUmami(player, SKILL_TRIGGER_UMAMI);
    }
}
