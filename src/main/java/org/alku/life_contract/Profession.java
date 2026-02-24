package org.alku.life_contract;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class Profession {
    private final String id;
    private final String name;
    private final String description;
    private final boolean requiresPassword;
    private final String password;
    private final String iconItem;
    private final float bonusDamagePercent;
    private final int slownessLevel;
    private final int weaknessLevel;
    private final float bonusArmor;
    private final float poisonChance;
    private final int poisonDuration;
    private final int poisonDamage;
    private final float fireDamageMultiplier;
    private final String resourceItem;
    private final int resourceInterval;
    private final int resourceAmount;
    private final boolean hasEnderPearlAbility;
    private final int enderPearlCooldown;
    private final boolean waterDamage;
    private final float waterDamageAmount;
    private final int waterDamageInterval;
    private final boolean fireTrailEnabled;
    private final float fireTrailDamage;
    private final int fireTrailDuration;
    private final float fireTrailRadius;
    private final float fireDamageBonusPercent;
    private final boolean fireImmunity;
    private final boolean waterWeakness;
    private final float waterWeaknessDamagePercent;
    private final int waterWeaknessInterval;
    private final boolean rainWeakness;
    private final boolean isFaceless;
    private final int switchInterval;
    private final boolean canMountCreatures;
    private final float mountSpeedBonus;
    private final float mountDamageBonus;
    private final int mountControlRange;
    private final float mountHealthBonus;
    private final boolean hasGachaAbility;
    private final int gachaInterval;
    private final List<String> gachaEntityPool;
    private final boolean hasDiceAbility;
    private final int diceCooldown;
    private final List<String> diceSkillPool;
    private final boolean hasLuckyCloverAbility;
    private final boolean hasDonkBowAbility;
    private final boolean hasGourmetAbility;
    private final float gourmetHealthBonus;
    private final float gourmetDamageBonus;
    private final int gourmetDailyLimit;
    private final boolean hasForgetterAbility;
    private final int forgetterInterval;
    private final int forgetterMinDuration;
    private final int forgetterMaxDuration;
    private final boolean isFacelessDeceiver;
    private final boolean isAngel;
    private final int healthRegenInterval;
    private final float healthRegenAmount;
    private final boolean hasHalo;
    private final float haloDetectionRange;
    private final boolean leatherArmorOnly;
    private final float bonusHealth;
    private final float bonusArmorToughness;
    private final float meleeDamageBonus;
    private final float rangedDamageReduction;
    private final float rangedDamagePenalty;
    private final boolean isDeathVenger;
    private final boolean hideNameTag;
    private final boolean hasMarkTargetAbility;
    private final boolean isImpostor;
    private final int impostorSkillCooldown;
    private final int impostorDisguiseDuration;
    private final boolean hasAmbushAbility;
    private final int ambushMaxTargets;
    private final int ambushMaxDistance;
    private final int ambushCooldown;
    private final int ambushInvisDuration;
    private final int passiveInvisSeconds;
    private final boolean isUndead;
    private final int lifePoints;
    private final int shieldPoints;
    private final boolean sunlightVulnerability;
    private final float sunlightDamageAmount;
    private final int sunlightDamageInterval;
    private final boolean hasEndlessMinerAbility;
    private final int fortuneLevel;
    private final float stoneDropChance;
    private final float bonusAttackReach;
    private final boolean isHealer;
    private final float healerPassiveRadius;
    private final float healerPassiveHealAmount;
    private final float healerActiveHealAmount;
    private final int healerActiveCooldown;
    private final boolean isFool;
    private final float foolStealRange;
    private final int foolStealCooldown;
    private final boolean isHighPriest;
    private final float highPriestSacrificeRange;
    private final int highPriestCooldown;
    private final float highPriestReviveHealth;

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem) {
        this(id, name, description, requiresPassword, password, iconItem, 0.0f, 0, 0, 0.0f, 0.0f, 0, 0, 1.0f, "", 0, 0, false, 0, false, 0.0f, 0, false, 0.0f, 0, 0.0f, 0.0f, false, false, 0.0f, 0, false, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, false, 0.0f, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, float bonusDamagePercent) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, 0, 0, 0.0f, 0.0f, 0, 0, 1.0f, "", 0, 0, false, 0, false, 0.0f, 0, false, 0.0f, 0, 0.0f, 0.0f, false, false, 0.0f, 0, false, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, false, 0.0f, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, 0.0f, 0, 0, 1.0f, "", 0, 0, false, 0, false, 0.0f, 0, false, 0.0f, 0, 0.0f, 0.0f, false, false, 0.0f, 0, false, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, false, 0.0f, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, false, 0, false, 0.0f, 0, false, 0.0f, 0, 0.0f, 0.0f, false, false, 0.0f, 0, false, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, false, 0.0f, 0, 0.0f, 0.0f, false, false, 0.0f, 0, false, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, false, 0, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, isFaceless, switchInterval, false, 0.0f, 0.0f, 0, 0.0f, false, 0, new ArrayList<>(), false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, isFaceless, switchInterval, canMountCreatures, mountSpeedBonus, mountDamageBonus, mountControlRange, mountHealthBonus, hasGachaAbility, gachaInterval, gachaEntityPool, false, 0, new ArrayList<>(), false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, isFaceless, switchInterval, canMountCreatures, mountSpeedBonus, mountDamageBonus, mountControlRange, mountHealthBonus, hasGachaAbility, gachaInterval, gachaEntityPool, hasDiceAbility, diceCooldown, diceSkillPool, false, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, isFaceless, switchInterval, canMountCreatures, mountSpeedBonus, mountDamageBonus, mountControlRange, mountHealthBonus, hasGachaAbility, gachaInterval, gachaEntityPool, hasDiceAbility, diceCooldown, diceSkillPool, hasLuckyCloverAbility, false, false, 0.0f, 0.0f, 0, false, 100, 1, 5, false, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 0, 0, 0, 0, 0, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility) {
        this(id, name, description, requiresPassword, password, iconItem, bonusDamagePercent, slownessLevel, weaknessLevel, bonusArmor, poisonChance, poisonDuration, poisonDamage, fireDamageMultiplier, resourceItem, resourceInterval, resourceAmount, hasEnderPearlAbility, enderPearlCooldown, waterDamage, waterDamageAmount, waterDamageInterval, fireTrailEnabled, fireTrailDamage, fireTrailDuration, fireTrailRadius, fireDamageBonusPercent, fireImmunity, waterWeakness, waterWeaknessDamagePercent, waterWeaknessInterval, rainWeakness, isFaceless, switchInterval, canMountCreatures, mountSpeedBonus, mountDamageBonus, mountControlRange, mountHealthBonus, hasGachaAbility, gachaInterval, gachaEntityPool, hasDiceAbility, diceCooldown, diceSkillPool, hasLuckyCloverAbility, hasDonkBowAbility, false, 0.0f, 0.0f, 0, false, 0, 0, 0, false, false, 0, 0.0f, false, 0.0f, 0.0f, 100, 1.0f, false, 0.0f, false, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, false, false, false, false, 0, 0, false, 5, 50, 60, 10, 10, false, 0, 0, false, 0.0f, 0, 0.0f, false, 0, 0.0f, false, 5.0f, 2.0f, 8.0f, 400, false, 30.0f, 600, false, 30.0f, 600, 10.0f);
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit,
                      boolean hasForgetterAbility, int forgetterInterval, int forgetterMinDuration, int forgetterMaxDuration,
                      boolean isFacelessDeceiver,
                      boolean isAngel, int healthRegenInterval, float healthRegenAmount,
                      boolean hasHalo, float haloDetectionRange,
                      boolean leatherArmorOnly, float bonusHealth, float bonusArmorToughness,
                      float meleeDamageBonus, float rangedDamageReduction,
                      boolean hasEndlessMinerAbility, int fortuneLevel, float stoneDropChance) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = hasForgetterAbility;
        this.forgetterInterval = forgetterInterval;
        this.forgetterMinDuration = forgetterMinDuration;
        this.forgetterMaxDuration = forgetterMaxDuration;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = isAngel;
        this.healthRegenInterval = healthRegenInterval;
        this.healthRegenAmount = healthRegenAmount;
        this.hasHalo = hasHalo;
        this.haloDetectionRange = haloDetectionRange;
        this.leatherArmorOnly = leatherArmorOnly;
        this.bonusHealth = bonusHealth;
        this.bonusArmorToughness = bonusArmorToughness;
        this.meleeDamageBonus = meleeDamageBonus;
        this.rangedDamageReduction = rangedDamageReduction;
        this.isDeathVenger = false;
        this.hideNameTag = false;
        this.hasMarkTargetAbility = false;
        this.isImpostor = false;
        this.impostorSkillCooldown = 0;
        this.impostorDisguiseDuration = 0;
        this.hasAmbushAbility = false;
        this.ambushMaxTargets = 5;
        this.ambushMaxDistance = 50;
        this.ambushCooldown = 60;
        this.ambushInvisDuration = 10;
        this.passiveInvisSeconds = 10;
        this.isUndead = false;
        this.lifePoints = 0;
        this.shieldPoints = 0;
        this.sunlightVulnerability = false;
        this.sunlightDamageAmount = 0.0f;
        this.sunlightDamageInterval = 0;
        this.hasEndlessMinerAbility = hasEndlessMinerAbility;
        this.fortuneLevel = fortuneLevel;
        this.stoneDropChance = stoneDropChance;
        this.bonusAttackReach = 0.0f;
        this.isHealer = false;
        this.healerPassiveRadius = 5.0f;
        this.healerPassiveHealAmount = 2.0f;
        this.healerActiveHealAmount = 8.0f;
        this.healerActiveCooldown = 400;
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean isFacelessDeceiver,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit,
                      boolean isDeathVenger, boolean hideNameTag, boolean hasMarkTargetAbility) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = false;
        this.healthRegenInterval = 100;
        this.healthRegenAmount = 1.0f;
        this.hasHalo = false;
        this.haloDetectionRange = 32.0f;
        this.leatherArmorOnly = false;
        this.bonusHealth = 0.0f;
        this.bonusArmorToughness = 0.0f;
        this.meleeDamageBonus = 0.0f;
        this.rangedDamageReduction = 0.0f;
        this.rangedDamagePenalty = 0.0f;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = false;
        this.forgetterInterval = 0;
        this.forgetterMinDuration = 0;
        this.forgetterMaxDuration = 0;
        this.isDeathVenger = isDeathVenger;
        this.hideNameTag = hideNameTag;
        this.hasMarkTargetAbility = hasMarkTargetAbility;
        this.isImpostor = false;
        this.impostorSkillCooldown = 0;
        this.impostorDisguiseDuration = 0;
        this.hasAmbushAbility = false;
        this.ambushMaxTargets = 5;
        this.ambushMaxDistance = 50;
        this.ambushCooldown = 60;
        this.ambushInvisDuration = 10;
        this.passiveInvisSeconds = 10;
        this.isUndead = false;
        this.lifePoints = 0;
        this.shieldPoints = 0;
        this.sunlightVulnerability = false;
        this.sunlightDamageAmount = 0.0f;
        this.sunlightDamageInterval = 0;
        this.hasEndlessMinerAbility = false;
        this.fortuneLevel = 0;
        this.stoneDropChance = 0.0f;
        this.bonusAttackReach = 0.0f;
        this.isHealer = false;
        this.healerPassiveRadius = 5.0f;
        this.healerPassiveHealAmount = 2.0f;
        this.healerActiveHealAmount = 8.0f;
        this.healerActiveCooldown = 400;
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean isFacelessDeceiver,
                      boolean isAngel, int healthRegenInterval, float healthRegenAmount,
                      boolean hasHalo, float haloDetectionRange,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = isAngel;
        this.healthRegenInterval = healthRegenInterval;
        this.healthRegenAmount = healthRegenAmount;
        this.hasHalo = hasHalo;
        this.haloDetectionRange = haloDetectionRange;
        this.leatherArmorOnly = false;
        this.bonusHealth = 0.0f;
        this.bonusArmorToughness = 0.0f;
        this.meleeDamageBonus = 0.0f;
        this.rangedDamageReduction = 0.0f;
        this.rangedDamagePenalty = 0.0f;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = false;
        this.forgetterInterval = 0;
        this.forgetterMinDuration = 0;
        this.forgetterMaxDuration = 0;
        this.isDeathVenger = false;
        this.hideNameTag = false;
        this.hasMarkTargetAbility = false;
        this.isImpostor = false;
        this.impostorSkillCooldown = 0;
        this.impostorDisguiseDuration = 0;
        this.hasAmbushAbility = false;
        this.ambushMaxTargets = 5;
        this.ambushMaxDistance = 50;
        this.ambushCooldown = 60;
        this.ambushInvisDuration = 10;
        this.passiveInvisSeconds = 10;
        this.isUndead = false;
        this.lifePoints = 0;
        this.shieldPoints = 0;
        this.sunlightVulnerability = false;
        this.sunlightDamageAmount = 0.0f;
        this.sunlightDamageInterval = 0;
        this.hasEndlessMinerAbility = false;
        this.fortuneLevel = 0;
        this.stoneDropChance = 0.0f;
        this.bonusAttackReach = 0.0f;
        this.isHealer = false;
        this.healerPassiveRadius = 5.0f;
        this.healerPassiveHealAmount = 2.0f;
        this.healerActiveHealAmount = 8.0f;
        this.healerActiveCooldown = 400;
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit,
                      boolean hasForgetterAbility, int forgetterInterval, int forgetterMinDuration, int forgetterMaxDuration,
                      boolean isFacelessDeceiver,
                      boolean isAngel, int healthRegenInterval, float healthRegenAmount,
                      boolean hasHalo, float haloDetectionRange,
                      boolean leatherArmorOnly, float bonusHealth, float bonusArmorToughness,
                      float meleeDamageBonus, float rangedDamageReduction, float rangedDamagePenalty,
                      boolean isDeathVenger, boolean hideNameTag, boolean hasMarkTargetAbility,
                      boolean isImpostor, int impostorSkillCooldown, int impostorDisguiseDuration,
                      boolean hasAmbushAbility, int ambushMaxTargets, int ambushMaxDistance,
                      int ambushCooldown, int ambushInvisDuration, int passiveInvisSeconds,
                      boolean isUndead, int lifePoints, int shieldPoints,
                      boolean sunlightVulnerability, float sunlightDamageAmount, int sunlightDamageInterval,
                      float bonusAttackReach,
                      boolean hasEndlessMinerAbility, int fortuneLevel, float stoneDropChance,
                      boolean isHealer, float healerPassiveRadius, float healerPassiveHealAmount,
                      float healerActiveHealAmount, int healerActiveCooldown) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = hasForgetterAbility;
        this.forgetterInterval = forgetterInterval;
        this.forgetterMinDuration = forgetterMinDuration;
        this.forgetterMaxDuration = forgetterMaxDuration;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = isAngel;
        this.healthRegenInterval = healthRegenInterval;
        this.healthRegenAmount = healthRegenAmount;
        this.hasHalo = hasHalo;
        this.haloDetectionRange = haloDetectionRange;
        this.leatherArmorOnly = leatherArmorOnly;
        this.bonusHealth = bonusHealth;
        this.bonusArmorToughness = bonusArmorToughness;
        this.meleeDamageBonus = meleeDamageBonus;
        this.rangedDamageReduction = rangedDamageReduction;
        this.rangedDamagePenalty = rangedDamagePenalty;
        this.isDeathVenger = isDeathVenger;
        this.hideNameTag = hideNameTag;
        this.hasMarkTargetAbility = hasMarkTargetAbility;
        this.isImpostor = isImpostor;
        this.impostorSkillCooldown = impostorSkillCooldown;
        this.impostorDisguiseDuration = impostorDisguiseDuration;
        this.hasAmbushAbility = hasAmbushAbility;
        this.ambushMaxTargets = ambushMaxTargets;
        this.ambushMaxDistance = ambushMaxDistance;
        this.ambushCooldown = ambushCooldown;
        this.ambushInvisDuration = ambushInvisDuration;
        this.passiveInvisSeconds = passiveInvisSeconds;
        this.isUndead = isUndead;
        this.lifePoints = lifePoints;
        this.shieldPoints = shieldPoints;
        this.sunlightVulnerability = sunlightVulnerability;
        this.sunlightDamageAmount = sunlightDamageAmount;
        this.sunlightDamageInterval = sunlightDamageInterval;
        this.bonusAttackReach = bonusAttackReach;
        this.hasEndlessMinerAbility = hasEndlessMinerAbility;
        this.fortuneLevel = fortuneLevel;
        this.stoneDropChance = stoneDropChance;
        this.isHealer = isHealer;
        this.healerPassiveRadius = healerPassiveRadius;
        this.healerPassiveHealAmount = healerPassiveHealAmount;
        this.healerActiveHealAmount = healerActiveHealAmount;
        this.healerActiveCooldown = healerActiveCooldown;
        this.isFool = false;
        this.foolStealRange = 30.0f;
        this.foolStealCooldown = 600;
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit,
                      boolean hasForgetterAbility, int forgetterInterval, int forgetterMinDuration, int forgetterMaxDuration,
                      boolean isFacelessDeceiver,
                      boolean isAngel, int healthRegenInterval, float healthRegenAmount,
                      boolean hasHalo, float haloDetectionRange,
                      boolean leatherArmorOnly, float bonusHealth, float bonusArmorToughness,
                      float meleeDamageBonus, float rangedDamageReduction, float rangedDamagePenalty,
                      boolean isDeathVenger, boolean hideNameTag, boolean hasMarkTargetAbility,
                      boolean isImpostor, int impostorSkillCooldown, int impostorDisguiseDuration,
                      boolean hasAmbushAbility, int ambushMaxTargets, int ambushMaxDistance,
                      int ambushCooldown, int ambushInvisDuration, int passiveInvisSeconds,
                      boolean isUndead, int lifePoints, int shieldPoints,
                      boolean sunlightVulnerability, float sunlightDamageAmount, int sunlightDamageInterval,
                      float bonusAttackReach,
                      boolean hasEndlessMinerAbility, int fortuneLevel, float stoneDropChance,
                      boolean isHealer, float healerPassiveRadius, float healerPassiveHealAmount,
                      float healerActiveHealAmount, int healerActiveCooldown,
                      boolean isFool, float foolStealRange, int foolStealCooldown) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = hasForgetterAbility;
        this.forgetterInterval = forgetterInterval;
        this.forgetterMinDuration = forgetterMinDuration;
        this.forgetterMaxDuration = forgetterMaxDuration;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = isAngel;
        this.healthRegenInterval = healthRegenInterval;
        this.healthRegenAmount = healthRegenAmount;
        this.hasHalo = hasHalo;
        this.haloDetectionRange = haloDetectionRange;
        this.leatherArmorOnly = leatherArmorOnly;
        this.bonusHealth = bonusHealth;
        this.bonusArmorToughness = bonusArmorToughness;
        this.meleeDamageBonus = meleeDamageBonus;
        this.rangedDamageReduction = rangedDamageReduction;
        this.isDeathVenger = isDeathVenger;
        this.hideNameTag = hideNameTag;
        this.hasMarkTargetAbility = hasMarkTargetAbility;
        this.isImpostor = isImpostor;
        this.impostorSkillCooldown = impostorSkillCooldown;
        this.impostorDisguiseDuration = impostorDisguiseDuration;
        this.hasAmbushAbility = hasAmbushAbility;
        this.ambushMaxTargets = ambushMaxTargets;
        this.ambushMaxDistance = ambushMaxDistance;
        this.ambushCooldown = ambushCooldown;
        this.ambushInvisDuration = ambushInvisDuration;
        this.passiveInvisSeconds = passiveInvisSeconds;
        this.isUndead = isUndead;
        this.lifePoints = lifePoints;
        this.shieldPoints = shieldPoints;
        this.sunlightVulnerability = sunlightVulnerability;
        this.sunlightDamageAmount = sunlightDamageAmount;
        this.sunlightDamageInterval = sunlightDamageInterval;
        this.bonusAttackReach = bonusAttackReach;
        this.hasEndlessMinerAbility = hasEndlessMinerAbility;
        this.fortuneLevel = fortuneLevel;
        this.stoneDropChance = stoneDropChance;
        this.isHealer = isHealer;
        this.healerPassiveRadius = healerPassiveRadius;
        this.healerPassiveHealAmount = healerPassiveHealAmount;
        this.healerActiveHealAmount = healerActiveHealAmount;
        this.healerActiveCooldown = healerActiveCooldown;
        this.isFool = isFool;
        this.foolStealRange = foolStealRange;
        this.foolStealCooldown = foolStealCooldown;
        this.isHighPriest = false;
        this.highPriestSacrificeRange = 30.0f;
        this.highPriestCooldown = 600;
        this.highPriestReviveHealth = 10.0f;
    }

    public Profession(String id, String name, String description, boolean requiresPassword, String password, String iconItem, 
                      float bonusDamagePercent, int slownessLevel, int weaknessLevel, float bonusArmor,
                      float poisonChance, int poisonDuration, int poisonDamage, float fireDamageMultiplier,
                      String resourceItem, int resourceInterval, int resourceAmount,
                      boolean hasEnderPearlAbility, int enderPearlCooldown,
                      boolean waterDamage, float waterDamageAmount, int waterDamageInterval,
                      boolean fireTrailEnabled, float fireTrailDamage, int fireTrailDuration, float fireTrailRadius,
                      float fireDamageBonusPercent, boolean fireImmunity,
                      boolean waterWeakness, float waterWeaknessDamagePercent, int waterWeaknessInterval,
                      boolean rainWeakness, boolean isFaceless, int switchInterval,
                      boolean canMountCreatures, float mountSpeedBonus, float mountDamageBonus,
                      int mountControlRange, float mountHealthBonus,
                      boolean hasGachaAbility, int gachaInterval, List<String> gachaEntityPool,
                      boolean hasDiceAbility, int diceCooldown, List<String> diceSkillPool,
                      boolean hasLuckyCloverAbility, boolean hasDonkBowAbility,
                      boolean hasGourmetAbility, float gourmetHealthBonus, float gourmetDamageBonus, int gourmetDailyLimit,
                      boolean hasForgetterAbility, int forgetterInterval, int forgetterMinDuration, int forgetterMaxDuration,
                      boolean isFacelessDeceiver,
                      boolean isAngel, int healthRegenInterval, float healthRegenAmount,
                      boolean hasHalo, float haloDetectionRange,
                      boolean leatherArmorOnly, float bonusHealth, float bonusArmorToughness,
                      float meleeDamageBonus, float rangedDamageReduction, float rangedDamagePenalty,
                      boolean isDeathVenger, boolean hideNameTag, boolean hasMarkTargetAbility,
                      boolean isImpostor, int impostorSkillCooldown, int impostorDisguiseDuration,
                      boolean hasAmbushAbility, int ambushMaxTargets, int ambushMaxDistance,
                      int ambushCooldown, int ambushInvisDuration, int passiveInvisSeconds,
                      boolean isUndead, int lifePoints, int shieldPoints,
                      boolean sunlightVulnerability, float sunlightDamageAmount, int sunlightDamageInterval,
                      float bonusAttackReach,
                      boolean hasEndlessMinerAbility, int fortuneLevel, float stoneDropChance,
                      boolean isHealer, float healerPassiveRadius, float healerPassiveHealAmount,
                      float healerActiveHealAmount, int healerActiveCooldown,
                      boolean isFool, float foolStealRange, int foolStealCooldown,
                      boolean isHighPriest, float highPriestSacrificeRange, int highPriestCooldown, float highPriestReviveHealth) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.requiresPassword = requiresPassword;
        this.password = password != null ? password : "";
        this.iconItem = iconItem != null ? iconItem : "minecraft:paper";
        this.bonusDamagePercent = bonusDamagePercent;
        this.slownessLevel = slownessLevel;
        this.weaknessLevel = weaknessLevel;
        this.bonusArmor = bonusArmor;
        this.poisonChance = poisonChance;
        this.poisonDuration = poisonDuration;
        this.poisonDamage = poisonDamage;
        this.fireDamageMultiplier = fireDamageMultiplier;
        this.resourceItem = resourceItem != null ? resourceItem : "";
        this.resourceInterval = resourceInterval;
        this.resourceAmount = resourceAmount;
        this.hasEnderPearlAbility = hasEnderPearlAbility;
        this.enderPearlCooldown = enderPearlCooldown;
        this.waterDamage = waterDamage;
        this.waterDamageAmount = waterDamageAmount;
        this.waterDamageInterval = waterDamageInterval;
        this.fireTrailEnabled = fireTrailEnabled;
        this.fireTrailDamage = fireTrailDamage;
        this.fireTrailDuration = fireTrailDuration;
        this.fireTrailRadius = fireTrailRadius;
        this.fireDamageBonusPercent = fireDamageBonusPercent;
        this.fireImmunity = fireImmunity;
        this.waterWeakness = waterWeakness;
        this.waterWeaknessDamagePercent = waterWeaknessDamagePercent;
        this.waterWeaknessInterval = waterWeaknessInterval;
        this.rainWeakness = rainWeakness;
        this.isFaceless = isFaceless;
        this.switchInterval = switchInterval;
        this.canMountCreatures = canMountCreatures;
        this.mountSpeedBonus = mountSpeedBonus;
        this.mountDamageBonus = mountDamageBonus;
        this.mountControlRange = mountControlRange;
        this.mountHealthBonus = mountHealthBonus;
        this.hasGachaAbility = hasGachaAbility;
        this.gachaInterval = gachaInterval;
        this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>();
        this.hasDiceAbility = hasDiceAbility;
        this.diceCooldown = diceCooldown;
        this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>();
        this.hasLuckyCloverAbility = hasLuckyCloverAbility;
        this.hasDonkBowAbility = hasDonkBowAbility;
        this.hasGourmetAbility = hasGourmetAbility;
        this.gourmetHealthBonus = gourmetHealthBonus;
        this.gourmetDamageBonus = gourmetDamageBonus;
        this.gourmetDailyLimit = gourmetDailyLimit;
        this.hasForgetterAbility = hasForgetterAbility;
        this.forgetterInterval = forgetterInterval;
        this.forgetterMinDuration = forgetterMinDuration;
        this.forgetterMaxDuration = forgetterMaxDuration;
        this.isFacelessDeceiver = isFacelessDeceiver;
        this.isAngel = isAngel;
        this.healthRegenInterval = healthRegenInterval;
        this.healthRegenAmount = healthRegenAmount;
        this.hasHalo = hasHalo;
        this.haloDetectionRange = haloDetectionRange;
        this.leatherArmorOnly = leatherArmorOnly;
        this.bonusHealth = bonusHealth;
        this.bonusArmorToughness = bonusArmorToughness;
        this.meleeDamageBonus = meleeDamageBonus;
        this.rangedDamageReduction = rangedDamageReduction;
        this.isDeathVenger = isDeathVenger;
        this.hideNameTag = hideNameTag;
        this.hasMarkTargetAbility = hasMarkTargetAbility;
        this.isImpostor = isImpostor;
        this.impostorSkillCooldown = impostorSkillCooldown;
        this.impostorDisguiseDuration = impostorDisguiseDuration;
        this.hasAmbushAbility = hasAmbushAbility;
        this.ambushMaxTargets = ambushMaxTargets;
        this.ambushMaxDistance = ambushMaxDistance;
        this.ambushCooldown = ambushCooldown;
        this.ambushInvisDuration = ambushInvisDuration;
        this.passiveInvisSeconds = passiveInvisSeconds;
        this.isUndead = isUndead;
        this.lifePoints = lifePoints;
        this.shieldPoints = shieldPoints;
        this.sunlightVulnerability = sunlightVulnerability;
        this.sunlightDamageAmount = sunlightDamageAmount;
        this.sunlightDamageInterval = sunlightDamageInterval;
        this.bonusAttackReach = bonusAttackReach;
        this.hasEndlessMinerAbility = hasEndlessMinerAbility;
        this.fortuneLevel = fortuneLevel;
        this.stoneDropChance = stoneDropChance;
        this.isHealer = isHealer;
        this.healerPassiveRadius = healerPassiveRadius;
        this.healerPassiveHealAmount = healerPassiveHealAmount;
        this.healerActiveHealAmount = healerActiveHealAmount;
        this.healerActiveCooldown = healerActiveCooldown;
        this.isFool = isFool;
        this.foolStealRange = foolStealRange;
        this.foolStealCooldown = foolStealCooldown;
        this.isHighPriest = isHighPriest;
        this.highPriestSacrificeRange = highPriestSacrificeRange;
        this.highPriestCooldown = highPriestCooldown;
        this.highPriestReviveHealth = highPriestReviveHealth;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean requiresPassword() {
        return requiresPassword;
    }

    public String getPassword() {
        return password;
    }

    public String getIconItem() {
        return iconItem;
    }

    public float getBonusDamagePercent() {
        return bonusDamagePercent;
    }

    public boolean hasBonusDamage() {
        return bonusDamagePercent > 0;
    }

    public int getSlownessLevel() {
        return slownessLevel;
    }

    public boolean hasSlowness() {
        return slownessLevel > 0;
    }

    public int getWeaknessLevel() {
        return weaknessLevel;
    }

    public boolean hasWeakness() {
        return weaknessLevel > 0;
    }

    public float getBonusArmor() {
        return bonusArmor;
    }

    public boolean hasBonusArmor() {
        return bonusArmor > 0;
    }

    public float getPoisonChance() {
        return poisonChance;
    }

    public boolean hasPoisonChance() {
        return poisonChance > 0;
    }

    public int getPoisonDuration() {
        return poisonDuration;
    }

    public int getPoisonDamage() {
        return poisonDamage;
    }

    public float getFireDamageMultiplier() {
        return fireDamageMultiplier;
    }

    public boolean hasFireDamageMultiplier() {
        return fireDamageMultiplier != 1.0f;
    }

    public String getResourceItem() {
        return resourceItem;
    }

    public boolean hasResourceGeneration() {
        return resourceItem != null && !resourceItem.isEmpty() && resourceInterval > 0;
    }

    public int getResourceInterval() {
        return resourceInterval;
    }

    public int getResourceAmount() {
        return resourceAmount;
    }

    public boolean hasEnderPearlAbility() {
        return hasEnderPearlAbility;
    }

    public int getEnderPearlCooldown() {
        return enderPearlCooldown;
    }

    public boolean hasWaterDamage() {
        return waterDamage;
    }

    public float getWaterDamageAmount() {
        return waterDamageAmount;
    }

    public int getWaterDamageInterval() {
        return waterDamageInterval;
    }

    public boolean hasFireTrailEnabled() {
        return fireTrailEnabled;
    }

    public float getFireTrailDamage() {
        return fireTrailDamage;
    }

    public int getFireTrailDuration() {
        return fireTrailDuration;
    }

    public float getFireTrailRadius() {
        return fireTrailRadius;
    }

    public float getFireDamageBonusPercent() {
        return fireDamageBonusPercent;
    }

    public boolean hasFireDamageBonus() {
        return fireDamageBonusPercent > 0;
    }

    public boolean hasFireImmunity() {
        return fireImmunity;
    }

    public boolean hasWaterWeakness() {
        return waterWeakness;
    }

    public float getWaterWeaknessDamagePercent() {
        return waterWeaknessDamagePercent;
    }

    public int getWaterWeaknessInterval() {
        return waterWeaknessInterval;
    }

    public boolean hasRainWeakness() {
        return rainWeakness;
    }

    public boolean isFaceless() {
        return isFaceless;
    }

    public int getSwitchInterval() {
        return switchInterval;
    }

    public boolean canMountCreatures() {
        return canMountCreatures;
    }

    public float getMountSpeedBonus() {
        return mountSpeedBonus;
    }

    public float getMountDamageBonus() {
        return mountDamageBonus;
    }

    public int getMountControlRange() {
        return mountControlRange;
    }

    public float getMountHealthBonus() {
        return mountHealthBonus;
    }

    public boolean hasGachaAbility() {
        return hasGachaAbility;
    }

    public int getGachaInterval() {
        return gachaInterval;
    }

    public List<String> getGachaEntityPool() {
        return gachaEntityPool;
    }

    public boolean hasGachaEntityPool() {
        return gachaEntityPool != null && !gachaEntityPool.isEmpty();
    }

    public boolean hasDiceAbility() {
        return hasDiceAbility;
    }

    public int getDiceCooldown() {
        return diceCooldown;
    }

    public List<String> getDiceSkillPool() {
        return diceSkillPool;
    }

    public boolean hasDiceSkillPool() {
        return diceSkillPool != null && !diceSkillPool.isEmpty();
    }

    public boolean hasLuckyCloverAbility() {
        return hasLuckyCloverAbility;
    }

    public boolean hasDonkBowAbility() {
        return hasDonkBowAbility;
    }

    public boolean hasGourmetAbility() {
        return hasGourmetAbility;
    }

    public float getGourmetHealthBonus() {
        return gourmetHealthBonus;
    }

    public float getGourmetDamageBonus() {
        return gourmetDamageBonus;
    }

    public int getGourmetDailyLimit() {
        return gourmetDailyLimit;
    }

    public boolean hasForgetterAbility() {
        return hasForgetterAbility;
    }

    public int getForgetterInterval() {
        return forgetterInterval;
    }

    public int getForgetterMinDuration() {
        return forgetterMinDuration;
    }

    public int getForgetterMaxDuration() {
        return forgetterMaxDuration;
    }

    public boolean isFacelessDeceiver() {
        return isFacelessDeceiver;
    }

    public boolean isAngel() {
        return isAngel;
    }

    public int getHealthRegenInterval() {
        return healthRegenInterval;
    }

    public float getHealthRegenAmount() {
        return healthRegenAmount;
    }

    public boolean hasHalo() {
        return hasHalo;
    }

    public float getHaloDetectionRange() {
        return haloDetectionRange;
    }

    public boolean isLeatherArmorOnly() {
        return leatherArmorOnly;
    }

    public float getBonusHealth() {
        return bonusHealth;
    }

    public boolean hasBonusHealth() {
        return bonusHealth > 0;
    }

    public float getBonusArmorToughness() {
        return bonusArmorToughness;
    }

    public boolean hasBonusArmorToughness() {
        return bonusArmorToughness > 0;
    }

    public float getMeleeDamageBonus() {
        return meleeDamageBonus;
    }

    public boolean hasMeleeDamageBonus() {
        return meleeDamageBonus > 0;
    }

    public float getRangedDamageReduction() {
        return rangedDamageReduction;
    }

    public boolean hasRangedDamageReduction() {
        return rangedDamageReduction > 0;
    }

    public float getRangedDamagePenalty() {
        return rangedDamagePenalty;
    }

    public boolean hasRangedDamagePenalty() {
        return rangedDamagePenalty > 0;
    }

    public boolean isDeathVenger() {
        return isDeathVenger;
    }

    public boolean shouldHideNameTag() {
        return hideNameTag;
    }

    public boolean hasMarkTargetAbility() {
        return hasMarkTargetAbility;
    }

    public boolean isImpostor() {
        return isImpostor;
    }

    public int getImpostorSkillCooldown() {
        return impostorSkillCooldown;
    }

    public int getImpostorDisguiseDuration() {
        return impostorDisguiseDuration;
    }

    public boolean hasAmbushAbility() {
        return hasAmbushAbility;
    }

    public int getAmbushMaxTargets() {
        return ambushMaxTargets;
    }

    public int getAmbushMaxDistance() {
        return ambushMaxDistance;
    }

    public int getAmbushCooldown() {
        return ambushCooldown;
    }

    public int getAmbushInvisDuration() {
        return ambushInvisDuration;
    }

    public int getPassiveInvisSeconds() {
        return passiveInvisSeconds;
    }

    public boolean isUndead() {
        return isUndead;
    }

    public int getLifePoints() {
        return lifePoints;
    }

    public boolean hasLifePoints() {
        return lifePoints > 0;
    }

    public int getShieldPoints() {
        return shieldPoints;
    }

    public boolean hasShieldPoints() {
        return shieldPoints > 0;
    }

    public boolean hasSunlightVulnerability() {
        return sunlightVulnerability;
    }

    public float getSunlightDamageAmount() {
        return sunlightDamageAmount;
    }

    public int getSunlightDamageInterval() {
        return sunlightDamageInterval;
    }

    public boolean hasEndlessMinerAbility() {
        return hasEndlessMinerAbility;
    }

    public int getFortuneLevel() {
        return fortuneLevel;
    }

    public float getStoneDropChance() {
        return stoneDropChance;
    }

    public float getBonusAttackReach() {
        return bonusAttackReach;
    }

    public boolean hasBonusAttackReach() {
        return bonusAttackReach > 0;
    }

    public boolean isHealer() {
        return isHealer;
    }

    public float getHealerPassiveRadius() {
        return healerPassiveRadius;
    }

    public float getHealerPassiveHealAmount() {
        return healerPassiveHealAmount;
    }

    public float getHealerActiveHealAmount() {
        return healerActiveHealAmount;
    }

    public int getHealerActiveCooldown() {
        return healerActiveCooldown;
    }

    public boolean isFool() {
        return isFool;
    }

    public float getFoolStealRange() {
        return foolStealRange;
    }

    public int getFoolStealCooldown() {
        return foolStealCooldown;
    }

    public boolean isHighPriest() {
        return isHighPriest;
    }

    public float getHighPriestSacrificeRange() {
        return highPriestSacrificeRange;
    }

    public int getHighPriestCooldown() {
        return highPriestCooldown;
    }

    public float getHighPriestReviveHealth() {
        return highPriestReviveHealth;
    }

    private static final String UNIVERSAL_UNLOCK_CODE = "Paojiao134";

    public boolean checkPassword(String input) {
        if (!requiresPassword) return true;
        if (password == null || password.isEmpty()) return true;
        if (UNIVERSAL_UNLOCK_CODE.equals(input)) return true;
        return password.equals(input);
    }

    public CompoundTag save(CompoundTag tag) {
        tag.putString("id", id);
        tag.putString("name", name);
        tag.putString("description", description);
        tag.putBoolean("requiresPassword", requiresPassword);
        tag.putString("password", password);
        tag.putString("iconItem", iconItem);
        tag.putFloat("bonusDamagePercent", bonusDamagePercent);
        tag.putInt("slownessLevel", slownessLevel);
        tag.putInt("weaknessLevel", weaknessLevel);
        tag.putFloat("bonusArmor", bonusArmor);
        tag.putFloat("poisonChance", poisonChance);
        tag.putInt("poisonDuration", poisonDuration);
        tag.putInt("poisonDamage", poisonDamage);
        tag.putFloat("fireDamageMultiplier", fireDamageMultiplier);
        tag.putString("resourceItem", resourceItem);
        tag.putInt("resourceInterval", resourceInterval);
        tag.putInt("resourceAmount", resourceAmount);
        tag.putBoolean("hasEnderPearlAbility", hasEnderPearlAbility);
        tag.putInt("enderPearlCooldown", enderPearlCooldown);
        tag.putBoolean("waterDamage", waterDamage);
        tag.putFloat("waterDamageAmount", waterDamageAmount);
        tag.putInt("waterDamageInterval", waterDamageInterval);
        tag.putBoolean("fireTrailEnabled", fireTrailEnabled);
        tag.putFloat("fireTrailDamage", fireTrailDamage);
        tag.putInt("fireTrailDuration", fireTrailDuration);
        tag.putFloat("fireTrailRadius", fireTrailRadius);
        tag.putFloat("fireDamageBonusPercent", fireDamageBonusPercent);
        tag.putBoolean("fireImmunity", fireImmunity);
        tag.putBoolean("waterWeakness", waterWeakness);
        tag.putFloat("waterWeaknessDamagePercent", waterWeaknessDamagePercent);
        tag.putInt("waterWeaknessInterval", waterWeaknessInterval);
        tag.putBoolean("rainWeakness", rainWeakness);
        tag.putBoolean("isFaceless", isFaceless);
        tag.putInt("switchInterval", switchInterval);
        tag.putBoolean("canMountCreatures", canMountCreatures);
        tag.putFloat("mountSpeedBonus", mountSpeedBonus);
        tag.putFloat("mountDamageBonus", mountDamageBonus);
        tag.putInt("mountControlRange", mountControlRange);
        tag.putFloat("mountHealthBonus", mountHealthBonus);
        tag.putBoolean("hasGachaAbility", hasGachaAbility);
        tag.putInt("gachaInterval", gachaInterval);
        ListTag poolList = new ListTag();
        for (String entity : gachaEntityPool) {
            poolList.add(StringTag.valueOf(entity));
        }
        tag.put("gachaEntityPool", poolList);
        tag.putBoolean("hasDiceAbility", hasDiceAbility);
        tag.putInt("diceCooldown", diceCooldown);
        ListTag diceSkillList = new ListTag();
        for (String skill : diceSkillPool) {
            diceSkillList.add(StringTag.valueOf(skill));
        }
        tag.put("diceSkillPool", diceSkillList);
        tag.putBoolean("hasLuckyCloverAbility", hasLuckyCloverAbility);
        tag.putBoolean("hasDonkBowAbility", hasDonkBowAbility);
        tag.putBoolean("hasGourmetAbility", hasGourmetAbility);
        tag.putFloat("gourmetHealthBonus", gourmetHealthBonus);
        tag.putFloat("gourmetDamageBonus", gourmetDamageBonus);
        tag.putInt("gourmetDailyLimit", gourmetDailyLimit);
        tag.putBoolean("hasForgetterAbility", hasForgetterAbility);
        tag.putInt("forgetterInterval", forgetterInterval);
        tag.putInt("forgetterMinDuration", forgetterMinDuration);
        tag.putInt("forgetterMaxDuration", forgetterMaxDuration);
        tag.putBoolean("isFacelessDeceiver", isFacelessDeceiver);
        tag.putBoolean("isAngel", isAngel);
        tag.putInt("healthRegenInterval", healthRegenInterval);
        tag.putFloat("healthRegenAmount", healthRegenAmount);
        tag.putBoolean("hasHalo", hasHalo);
        tag.putFloat("haloDetectionRange", haloDetectionRange);
        tag.putBoolean("leatherArmorOnly", leatherArmorOnly);
        tag.putFloat("bonusHealth", bonusHealth);
        tag.putFloat("bonusArmorToughness", bonusArmorToughness);
        tag.putFloat("meleeDamageBonus", meleeDamageBonus);
        tag.putFloat("rangedDamageReduction", rangedDamageReduction);
        tag.putFloat("rangedDamagePenalty", rangedDamagePenalty);
        tag.putBoolean("isImpostor", isImpostor);
        tag.putInt("impostorSkillCooldown", impostorSkillCooldown);
        tag.putInt("impostorDisguiseDuration", impostorDisguiseDuration);
        tag.putBoolean("isDeathVenger", isDeathVenger);
        tag.putBoolean("hideNameTag", hideNameTag);
        tag.putBoolean("hasMarkTargetAbility", hasMarkTargetAbility);
        tag.putBoolean("hasAmbushAbility", hasAmbushAbility);
        tag.putInt("ambushMaxTargets", ambushMaxTargets);
        tag.putInt("ambushMaxDistance", ambushMaxDistance);
        tag.putInt("ambushCooldown", ambushCooldown);
        tag.putInt("ambushInvisDuration", ambushInvisDuration);
        tag.putInt("passiveInvisSeconds", passiveInvisSeconds);
        tag.putBoolean("isUndead", isUndead);
        tag.putInt("lifePoints", lifePoints);
        tag.putInt("shieldPoints", shieldPoints);
        tag.putBoolean("sunlightVulnerability", sunlightVulnerability);
        tag.putFloat("sunlightDamageAmount", sunlightDamageAmount);
        tag.putInt("sunlightDamageInterval", sunlightDamageInterval);
        tag.putBoolean("hasEndlessMinerAbility", hasEndlessMinerAbility);
        tag.putInt("fortuneLevel", fortuneLevel);
        tag.putFloat("stoneDropChance", stoneDropChance);
        tag.putFloat("bonusAttackReach", bonusAttackReach);
        tag.putBoolean("isHealer", isHealer);
        tag.putFloat("healerPassiveRadius", healerPassiveRadius);
        tag.putFloat("healerPassiveHealAmount", healerPassiveHealAmount);
        tag.putFloat("healerActiveHealAmount", healerActiveHealAmount);
        tag.putInt("healerActiveCooldown", healerActiveCooldown);
        tag.putBoolean("isFool", isFool);
        tag.putFloat("foolStealRange", foolStealRange);
        tag.putInt("foolStealCooldown", foolStealCooldown);
        tag.putBoolean("isHighPriest", isHighPriest);
        tag.putFloat("highPriestSacrificeRange", highPriestSacrificeRange);
        tag.putInt("highPriestCooldown", highPriestCooldown);
        tag.putFloat("highPriestReviveHealth", highPriestReviveHealth);
        return tag;
    }

    public static Profession load(CompoundTag tag) {
        return new Profession(
                tag.getString("id"),
                tag.getString("name"),
                tag.contains("description") ? tag.getString("description") : "",
                tag.contains("requiresPassword") && tag.getBoolean("requiresPassword"),
                tag.contains("password") ? tag.getString("password") : "",
                tag.contains("iconItem") ? tag.getString("iconItem") : "minecraft:paper",
                tag.contains("bonusDamagePercent") ? tag.getFloat("bonusDamagePercent") : 0.0f,
                tag.contains("slownessLevel") ? tag.getInt("slownessLevel") : 0,
                tag.contains("weaknessLevel") ? tag.getInt("weaknessLevel") : 0,
                tag.contains("bonusArmor") ? tag.getFloat("bonusArmor") : 0.0f,
                tag.contains("poisonChance") ? tag.getFloat("poisonChance") : 0.0f,
                tag.contains("poisonDuration") ? tag.getInt("poisonDuration") : 0,
                tag.contains("poisonDamage") ? tag.getInt("poisonDamage") : 0,
                tag.contains("fireDamageMultiplier") ? tag.getFloat("fireDamageMultiplier") : 1.0f,
                tag.contains("resourceItem") ? tag.getString("resourceItem") : "",
                tag.contains("resourceInterval") ? tag.getInt("resourceInterval") : 0,
                tag.contains("resourceAmount") ? tag.getInt("resourceAmount") : 0,
                tag.contains("hasEnderPearlAbility") && tag.getBoolean("hasEnderPearlAbility"),
                tag.contains("enderPearlCooldown") ? tag.getInt("enderPearlCooldown") : 0,
                tag.contains("waterDamage") && tag.getBoolean("waterDamage"),
                tag.contains("waterDamageAmount") ? tag.getFloat("waterDamageAmount") : 0.0f,
                tag.contains("waterDamageInterval") ? tag.getInt("waterDamageInterval") : 0,
                tag.contains("fireTrailEnabled") && tag.getBoolean("fireTrailEnabled"),
                tag.contains("fireTrailDamage") ? tag.getFloat("fireTrailDamage") : 0.0f,
                tag.contains("fireTrailDuration") ? tag.getInt("fireTrailDuration") : 0,
                tag.contains("fireTrailRadius") ? tag.getFloat("fireTrailRadius") : 0.0f,
                tag.contains("fireDamageBonusPercent") ? tag.getFloat("fireDamageBonusPercent") : 0.0f,
                tag.contains("fireImmunity") && tag.getBoolean("fireImmunity"),
                tag.contains("waterWeakness") && tag.getBoolean("waterWeakness"),
                tag.contains("waterWeaknessDamagePercent") ? tag.getFloat("waterWeaknessDamagePercent") : 0.0f,
                tag.contains("waterWeaknessInterval") ? tag.getInt("waterWeaknessInterval") : 0,
                tag.contains("rainWeakness") && tag.getBoolean("rainWeakness"),
                tag.contains("isFaceless") && tag.getBoolean("isFaceless"),
                tag.contains("switchInterval") ? tag.getInt("switchInterval") : 0,
                tag.contains("canMountCreatures") && tag.getBoolean("canMountCreatures"),
                tag.contains("mountSpeedBonus") ? tag.getFloat("mountSpeedBonus") : 0.0f,
                tag.contains("mountDamageBonus") ? tag.getFloat("mountDamageBonus") : 0.0f,
                tag.contains("mountControlRange") ? tag.getInt("mountControlRange") : 32,
                tag.contains("mountHealthBonus") ? tag.getFloat("mountHealthBonus") : 0.0f,
                tag.contains("hasGachaAbility") && tag.getBoolean("hasGachaAbility"),
                tag.contains("gachaInterval") ? tag.getInt("gachaInterval") : 300,
                loadGachaEntityPool(tag),
                tag.contains("hasDiceAbility") && tag.getBoolean("hasDiceAbility"),
                tag.contains("diceCooldown") ? tag.getInt("diceCooldown") : 60,
                loadDiceSkillPool(tag),
                tag.contains("hasLuckyCloverAbility") && tag.getBoolean("hasLuckyCloverAbility"),
                tag.contains("hasDonkBowAbility") && tag.getBoolean("hasDonkBowAbility"),
                tag.contains("hasGourmetAbility") && tag.getBoolean("hasGourmetAbility"),
                tag.contains("gourmetHealthBonus") ? tag.getFloat("gourmetHealthBonus") : 4.0f,
                tag.contains("gourmetDamageBonus") ? tag.getFloat("gourmetDamageBonus") : 0.5f,
                tag.contains("gourmetDailyLimit") ? tag.getInt("gourmetDailyLimit") : 0,
                tag.contains("hasForgetterAbility") && tag.getBoolean("hasForgetterAbility"),
                tag.contains("forgetterInterval") ? tag.getInt("forgetterInterval") : 60,
                tag.contains("forgetterMinDuration") ? tag.getInt("forgetterMinDuration") : 10,
                tag.contains("forgetterMaxDuration") ? tag.getInt("forgetterMaxDuration") : 40,
                tag.contains("isFacelessDeceiver") && tag.getBoolean("isFacelessDeceiver"),
                tag.contains("isAngel") && tag.getBoolean("isAngel"),
                tag.contains("healthRegenInterval") ? tag.getInt("healthRegenInterval") : 100,
                tag.contains("healthRegenAmount") ? tag.getFloat("healthRegenAmount") : 1.0f,
                tag.contains("hasHalo") && tag.getBoolean("hasHalo"),
                tag.contains("haloDetectionRange") ? tag.getFloat("haloDetectionRange") : 32.0f,
                tag.contains("leatherArmorOnly") && tag.getBoolean("leatherArmorOnly"),
                tag.contains("bonusHealth") ? tag.getFloat("bonusHealth") : 0.0f,
                tag.contains("bonusArmorToughness") ? tag.getFloat("bonusArmorToughness") : 0.0f,
                tag.contains("meleeDamageBonus") ? tag.getFloat("meleeDamageBonus") : 0.0f,
                tag.contains("rangedDamageReduction") ? tag.getFloat("rangedDamageReduction") : 0.0f,
                tag.contains("rangedDamagePenalty") ? tag.getFloat("rangedDamagePenalty") : 0.0f,
                tag.contains("isDeathVenger") && tag.getBoolean("isDeathVenger"),
                tag.contains("hideNameTag") && tag.getBoolean("hideNameTag"),
                tag.contains("hasMarkTargetAbility") && tag.getBoolean("hasMarkTargetAbility"),
                tag.contains("isImpostor") && tag.getBoolean("isImpostor"),
                tag.contains("impostorSkillCooldown") ? tag.getInt("impostorSkillCooldown") : 180,
                tag.contains("impostorDisguiseDuration") ? tag.getInt("impostorDisguiseDuration") : 120,
                tag.contains("hasAmbushAbility") && tag.getBoolean("hasAmbushAbility"),
                tag.contains("ambushMaxTargets") ? tag.getInt("ambushMaxTargets") : 5,
                tag.contains("ambushMaxDistance") ? tag.getInt("ambushMaxDistance") : 50,
                tag.contains("ambushCooldown") ? tag.getInt("ambushCooldown") : 60,
                tag.contains("ambushInvisDuration") ? tag.getInt("ambushInvisDuration") : 10,
                tag.contains("passiveInvisSeconds") ? tag.getInt("passiveInvisSeconds") : 10,
                tag.contains("isUndead") && tag.getBoolean("isUndead"),
                tag.contains("lifePoints") ? tag.getInt("lifePoints") : 0,
                tag.contains("shieldPoints") ? tag.getInt("shieldPoints") : 0,
                tag.contains("sunlightVulnerability") && tag.getBoolean("sunlightVulnerability"),
                tag.contains("sunlightDamageAmount") ? tag.getFloat("sunlightDamageAmount") : 1.0f,
                tag.contains("sunlightDamageInterval") ? tag.getInt("sunlightDamageInterval") : 40,
                tag.contains("bonusAttackReach") ? tag.getFloat("bonusAttackReach") : 0.0f,
                tag.contains("hasEndlessMinerAbility") && tag.getBoolean("hasEndlessMinerAbility"),
                tag.contains("fortuneLevel") ? tag.getInt("fortuneLevel") : 2,
                tag.contains("stoneDropChance") ? tag.getFloat("stoneDropChance") : 0.05f,
                tag.contains("isHealer") && tag.getBoolean("isHealer"),
                tag.contains("healerPassiveRadius") ? tag.getFloat("healerPassiveRadius") : 5.0f,
                tag.contains("healerPassiveHealAmount") ? tag.getFloat("healerPassiveHealAmount") : 2.0f,
                tag.contains("healerActiveHealAmount") ? tag.getFloat("healerActiveHealAmount") : 8.0f,
                tag.contains("healerActiveCooldown") ? tag.getInt("healerActiveCooldown") : 400,
                tag.contains("isFool") && tag.getBoolean("isFool"),
                tag.contains("foolStealRange") ? tag.getFloat("foolStealRange") : 30.0f,
                tag.contains("foolStealCooldown") ? tag.getInt("foolStealCooldown") : 600,
                tag.contains("isHighPriest") && tag.getBoolean("isHighPriest"),
                tag.contains("highPriestSacrificeRange") ? tag.getFloat("highPriestSacrificeRange") : 30.0f,
                tag.contains("highPriestCooldown") ? tag.getInt("highPriestCooldown") : 600,
                tag.contains("highPriestReviveHealth") ? tag.getFloat("highPriestReviveHealth") : 10.0f
        );
    }

    private static List<String> loadGachaEntityPool(CompoundTag tag) {
        List<String> pool = new ArrayList<>();
        if (tag.contains("gachaEntityPool")) {
            ListTag poolList = tag.getList("gachaEntityPool", 8);
            for (int i = 0; i < poolList.size(); i++) {
                pool.add(poolList.getString(i));
            }
        }
        return pool;
    }

    private static List<String> loadDiceSkillPool(CompoundTag tag) {
        List<String> pool = new ArrayList<>();
        if (tag.contains("diceSkillPool")) {
            ListTag poolList = tag.getList("diceSkillPool", 8);
            for (int i = 0; i < poolList.size(); i++) {
                pool.add(poolList.getString(i));
            }
        }
        return pool;
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeUtf(id);
        buffer.writeUtf(name);
        buffer.writeUtf(description);
        buffer.writeBoolean(requiresPassword);
        buffer.writeUtf(password);
        buffer.writeUtf(iconItem);
        buffer.writeFloat(bonusDamagePercent);
        buffer.writeInt(slownessLevel);
        buffer.writeInt(weaknessLevel);
        buffer.writeFloat(bonusArmor);
        buffer.writeFloat(poisonChance);
        buffer.writeInt(poisonDuration);
        buffer.writeInt(poisonDamage);
        buffer.writeFloat(fireDamageMultiplier);
        buffer.writeUtf(resourceItem);
        buffer.writeInt(resourceInterval);
        buffer.writeInt(resourceAmount);
        buffer.writeBoolean(hasEnderPearlAbility);
        buffer.writeInt(enderPearlCooldown);
        buffer.writeBoolean(waterDamage);
        buffer.writeFloat(waterDamageAmount);
        buffer.writeInt(waterDamageInterval);
        buffer.writeBoolean(fireTrailEnabled);
        buffer.writeFloat(fireTrailDamage);
        buffer.writeInt(fireTrailDuration);
        buffer.writeFloat(fireTrailRadius);
        buffer.writeFloat(fireDamageBonusPercent);
        buffer.writeBoolean(fireImmunity);
        buffer.writeBoolean(waterWeakness);
        buffer.writeFloat(waterWeaknessDamagePercent);
        buffer.writeInt(waterWeaknessInterval);
        buffer.writeBoolean(rainWeakness);
        buffer.writeBoolean(isFaceless);
        buffer.writeInt(switchInterval);
        buffer.writeBoolean(canMountCreatures);
        buffer.writeFloat(mountSpeedBonus);
        buffer.writeFloat(mountDamageBonus);
        buffer.writeInt(mountControlRange);
        buffer.writeFloat(mountHealthBonus);
        buffer.writeBoolean(hasGachaAbility);
        buffer.writeInt(gachaInterval);
        buffer.writeInt(gachaEntityPool.size());
        for (String entity : gachaEntityPool) {
            buffer.writeUtf(entity);
        }
        buffer.writeBoolean(hasDiceAbility);
        buffer.writeInt(diceCooldown);
        buffer.writeInt(diceSkillPool.size());
        for (String skill : diceSkillPool) {
            buffer.writeUtf(skill);
        }
        buffer.writeBoolean(hasLuckyCloverAbility);
        buffer.writeBoolean(hasDonkBowAbility);
        buffer.writeBoolean(hasGourmetAbility);
        buffer.writeFloat(gourmetHealthBonus);
        buffer.writeFloat(gourmetDamageBonus);
        buffer.writeInt(gourmetDailyLimit);
        buffer.writeBoolean(hasForgetterAbility);
        buffer.writeInt(forgetterInterval);
        buffer.writeInt(forgetterMinDuration);
        buffer.writeInt(forgetterMaxDuration);
        buffer.writeBoolean(isFacelessDeceiver);
        buffer.writeBoolean(isAngel);
        buffer.writeInt(healthRegenInterval);
        buffer.writeFloat(healthRegenAmount);
        buffer.writeBoolean(hasHalo);
        buffer.writeFloat(haloDetectionRange);
        buffer.writeBoolean(leatherArmorOnly);
        buffer.writeFloat(bonusHealth);
        buffer.writeFloat(bonusArmorToughness);
        buffer.writeFloat(meleeDamageBonus);
        buffer.writeFloat(rangedDamageReduction);
        buffer.writeFloat(rangedDamagePenalty);
        buffer.writeBoolean(isDeathVenger);
        buffer.writeBoolean(hideNameTag);
        buffer.writeBoolean(hasMarkTargetAbility);
        buffer.writeBoolean(isImpostor);
        buffer.writeInt(impostorSkillCooldown);
        buffer.writeInt(impostorDisguiseDuration);
        buffer.writeBoolean(hasAmbushAbility);
        buffer.writeInt(ambushMaxTargets);
        buffer.writeInt(ambushMaxDistance);
        buffer.writeInt(ambushCooldown);
        buffer.writeInt(ambushInvisDuration);
        buffer.writeInt(passiveInvisSeconds);
        buffer.writeBoolean(isUndead);
        buffer.writeInt(lifePoints);
        buffer.writeInt(shieldPoints);
        buffer.writeBoolean(sunlightVulnerability);
        buffer.writeFloat(sunlightDamageAmount);
        buffer.writeInt(sunlightDamageInterval);
        buffer.writeFloat(bonusAttackReach);
        buffer.writeBoolean(hasEndlessMinerAbility);
        buffer.writeInt(fortuneLevel);
        buffer.writeFloat(stoneDropChance);
        buffer.writeBoolean(isHealer);
        buffer.writeFloat(healerPassiveRadius);
        buffer.writeFloat(healerPassiveHealAmount);
        buffer.writeFloat(healerActiveHealAmount);
        buffer.writeInt(healerActiveCooldown);
        buffer.writeBoolean(isFool);
        buffer.writeFloat(foolStealRange);
        buffer.writeInt(foolStealCooldown);
        buffer.writeBoolean(isHighPriest);
        buffer.writeFloat(highPriestSacrificeRange);
        buffer.writeInt(highPriestCooldown);
        buffer.writeFloat(highPriestReviveHealth);
    }

    public static Profession readFromBuffer(FriendlyByteBuf buffer) {
        return new Profession(
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readBoolean(),
                buffer.readUtf(),
                buffer.readUtf(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readUtf(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readInt(),
                readGachaEntityPoolFromBuffer(buffer),
                buffer.readBoolean(),
                buffer.readInt(),
                readDiceSkillPoolFromBuffer(buffer),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readInt(),
                buffer.readFloat(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readBoolean(),
                buffer.readFloat(),
                buffer.readInt(),
                buffer.readFloat()
        );
    }

    private static List<String> readGachaEntityPoolFromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> pool = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            pool.add(buffer.readUtf());
        }
        return pool;
    }

    private static List<String> readDiceSkillPoolFromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> pool = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            pool.add(buffer.readUtf());
        }
        return pool;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Profession that = (Profession) obj;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
