package org.alku.life_contract.profession;

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
    private final boolean ironArmorOnly;
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
    private final boolean isGhostSenator;
    private final float ghostSenatorHealAmount;
    private final int ghostSenatorStrengthDuration;
    private final float ghostSenatorDetectionRadius;
    private final boolean isWraithCouncilor;
    private final int wraithSoulMax;
    private final int wraithSoulInitial;
    private final int wraithSoulRegenRate;
    private final int wraithSoulDarkBonus;
    private final float wraithSoulSunlightPenalty;
    private final int wraithSoulKillBonus;
    private final int wraithSoulHitBonus;
    private final int wraithSoulSummonKillBonus;
    private final float wraithErosionDamage;
    private final float wraithErosionArmorReduction;
    private final float wraithErosionSlowPercent;
    private final int wraithErosionMaxStacks;
    private final int wraithErosionDuration;
    private final int wraithSummonCost;
    private final int wraithSummonCooldown;
    private final int wraithSummonCount;
    private final float wraithSummonHealth;
    private final float wraithSummonDamage;
    private final int wraithSummonDuration;
    private final float wraithSummonCorpseRange;
    private final int wraithSummonExtraMax;
    private final int wraithDomainCost;
    private final int wraithDomainCooldown;
    private final float wraithDomainRadius;
    private final int wraithDomainDuration;
    private final float wraithDomainDamage;
    private final float wraithDomainBossSlow;
    private final int wraithDomainCharmDuration;
    private final int wraithBarrageBaseCost;
    private final int wraithBarrageChargedCost;
    private final int wraithBarrageCooldown;
    private final int wraithBarrageBaseOrbs;
    private final int wraithBarrageChargedOrbs;
    private final float wraithBarrageBaseDamage;
    private final float wraithBarrageChargedDamage;
    private final float wraithBarrageBaseRange;
    private final float wraithBarrageChargedRange;
    private final int wraithBarrageMaxChargeTime;
    private final int wraithBarrageMaxHits;
    private final int wraithUltimateMinCost;
    private final int wraithUltimateCooldown;
    private final int wraithUltimateCloneCount;
    private final float wraithUltimateCloneDamageRatio;
    private final int wraithUltimateDuration;
    private final int wraithUltimateSoulRegen;
    private final float wraithUltimateDamageIncrease;
    private final int wraithUltimateExhaustDuration;
    private final boolean isEvilPoisoner;
    private final int poisonerStrengthDuration;
    private final boolean hasTurtleAura;
    private final float turtleAuraRadius;
    private final int turtleAuraSlownessLevel;
    private final int turtleAuraDuration;
    private final boolean isJungleApeGod;
    private final int rhythmStacksMax;
    private final float rhythmAttackSpeedPerStack;
    private final float rhythmMoveSpeedPerStack;
    private final int berserkDuration;
    private final float berserkCooldownReduction;
    private final float berserkLifeSteal;
    private final float flatDamageReduction;
    private final float resistanceChance;
    private final int resistanceDuration;
    private final float q1DamageMultiplier;
    private final float q1MovingTargetDamageMultiplier;
    private final int q1SlowDuration;
    private final int q1Cooldown;
    private final float q1Angle;
    private final float q2MaxDistance;
    private final float q2DamageMultiplier;
    private final float q2KnockbackDuration;
    private final float q2SplashDamagePercent;
    private final float q2BonusAttackRange;
    private final int q2BonusAttackDuration;
    private final int q2Cooldown;
    private final float q3Radius;
    private final float q3DamageMultiplier;
    private final float q3FearDuration;
    private final float q3BerserkFearDuration;
    private final int q3WeaknessDuration;
    private final int q3Cooldown;
    private final int rDuration;
    private final float rHealPercent;
    private final float rHealthBonusPercent;
    private final int rPowerLevel;
    private final int rSpeedLevel;
    private final int rFatigueDuration;
    private final int rCooldown;
    
    private final boolean isHeavyKnight;
    private final float heavyKnightSpeedPenalty;
    private final int heavyKnightWillOnHit;
    private final int heavyKnightWillOnBlock;
    private final int heavyKnightWillOnDamaged;
    private final float heavyKnightFullWillDamageBonus;
    private final float heavyKnightFullWillDamageReduction;
    private final int heavyKnightShieldWallTriggerTicks;
    private final float heavyKnightShieldWallReduction;
    private final float heavyKnightShieldWallRadius;
    private final float heavyKnightShieldWallArmorBonus;
    private final int heavyKnightChargeWillCost;
    private final int heavyKnightChargeCooldown;
    private final float heavyKnightChargeDistance;
    private final float heavyKnightChargeDamage;
    private final float heavyKnightChargeKnockback;
    private final int heavyKnightProtectRange;
    private final int heavyKnightProtectWillCost;
    private final int heavyKnightProtectCooldown;
    private final float heavyKnightShieldBashDamage;
    private final int heavyKnightShieldBashStunDuration;
    private final int heavyKnightShieldBashCooldown;

    private final boolean isByteChen;
    private final int byteChenComputeMax;
    private final int byteChenComputeInitial;
    private final int byteChenComputeRegenRate;
    private final float byteChenComputeNodeRegenBonus;
    private final int byteChenComputeOnRead;
    private final int byteChenComputeOnNodeTrigger;
    private final int byteChenComputeOnInterrupt;
    private final int byteChenComputeLowThreshold;
    private final int byteChenNodeMax;
    private final int byteChenNodeRange;
    private final int byteChenNodeDuration;
    private final int byteChenScoutNodeCost;
    private final float byteChenScoutNodeRadius;
    private final int byteChenScoutNodeComputeRegen;
    private final int byteChenBuffNodeCost;
    private final float byteChenBuffNodeRadius;
    private final float byteChenBuffNodeDamageBonus;
    private final float byteChenBuffNodeDamageReduction;
    private final float byteChenBuffNodeSpeedBonus;
    private final float byteChenBuffNodeCooldownReduction;
    private final int byteChenBuffNodeMaxStacks;
    private final int byteChenCounterNodeCost;
    private final float byteChenCounterNodeRadius;
    private final int byteChenCounterNodeSilenceDuration;
    private final int byteChenCounterNodeDisorderDuration;
    private final float byteChenCounterNodeDamageReduction;
    private final float byteChenCounterNodeCooldownMultiplier;
    private final float byteChenDataVisionRange;
    private final float byteChenLightweightSpeedBonus;
    private final float byteChenLightweightDigSpeedBonus;
    private final float byteChenLightweightMeleePenalty;
    private final float byteChenLightweightReceivedMeleePenalty;
    private final int byteChenNodeRecycleCooldown;
    private final float byteChenNodeRecycleRefund;
    private final int byteChenFullReadCost;
    private final int byteChenFullReadCooldown;
    private final float byteChenFullReadRadius;
    private final int byteChenFullReadDuration;
    private final int byteChenDataDispatchCost;
    private final int byteChenDataDispatchCooldown;
    private final float byteChenDataDispatchSpeedBonus;
    private final float byteChenDataDispatchCooldownBonus;
    private final int byteChenDataDispatchBuffDuration;
    private final int byteChenDataBanCost;
    private final int byteChenDataBanCooldown;
    private final float byteChenDataBanRange;
    private final int byteChenDataBanDuration;
    private final int byteChenDataBanBossDuration;
    private final float byteChenDataBanTrueDamageBonus;
    private final int byteChenUltimateMinCost;
    private final int byteChenUltimateCooldown;
    private final float byteChenUltimateRadius;
    private final int byteChenUltimateDuration;
    private final float byteChenUltimateDamageBonus;
    private final float byteChenUltimateDamageReduction;
    private final float byteChenUltimateSpeedBonus;
    private final float byteChenUltimateCooldownBonus;
    private final float byteChenUltimateEnemyDamageReduction;
    private final float byteChenUltimateEnemySpeedReduction;
    private final float byteChenUltimateEnemyCooldownMultiplier;
    private final float byteChenUltimateInterruptChance;
    private final int byteChenExhaustDuration;

    Profession(ProfessionBuilder builder) {
        this.id = builder.getId();
        this.name = builder.getName();
        this.description = builder.getDescription();
        this.requiresPassword = builder.isRequiresPassword();
        this.password = builder.getPassword();
        this.iconItem = builder.getIconItem();
        this.bonusDamagePercent = builder.getBonusDamagePercent();
        this.slownessLevel = builder.getSlownessLevel();
        this.weaknessLevel = builder.getWeaknessLevel();
        this.bonusArmor = builder.getBonusArmor();
        this.poisonChance = builder.getPoisonChance();
        this.poisonDuration = builder.getPoisonDuration();
        this.poisonDamage = builder.getPoisonDamage();
        this.fireDamageMultiplier = builder.getFireDamageMultiplier();
        this.resourceItem = builder.getResourceItem();
        this.resourceInterval = builder.getResourceInterval();
        this.resourceAmount = builder.getResourceAmount();
        this.hasEnderPearlAbility = builder.hasEnderPearlAbility();
        this.enderPearlCooldown = builder.getEnderPearlCooldown();
        this.waterDamage = builder.hasWaterDamage();
        this.waterDamageAmount = builder.getWaterDamageAmount();
        this.waterDamageInterval = builder.getWaterDamageInterval();
        this.fireTrailEnabled = builder.isFireTrailEnabled();
        this.fireTrailDamage = builder.getFireTrailDamage();
        this.fireTrailDuration = builder.getFireTrailDuration();
        this.fireTrailRadius = builder.getFireTrailRadius();
        this.fireDamageBonusPercent = builder.getFireDamageBonusPercent();
        this.fireImmunity = builder.hasFireImmunity();
        this.waterWeakness = builder.hasWaterWeakness();
        this.waterWeaknessDamagePercent = builder.getWaterWeaknessDamagePercent();
        this.waterWeaknessInterval = builder.getWaterWeaknessInterval();
        this.rainWeakness = builder.hasRainWeakness();
        this.isFaceless = builder.isFaceless();
        this.switchInterval = builder.getSwitchInterval();
        this.canMountCreatures = builder.canMountCreatures();
        this.mountSpeedBonus = builder.getMountSpeedBonus();
        this.mountDamageBonus = builder.getMountDamageBonus();
        this.mountControlRange = builder.getMountControlRange();
        this.mountHealthBonus = builder.getMountHealthBonus();
        this.hasGachaAbility = builder.hasGachaAbility();
        this.gachaInterval = builder.getGachaInterval();
        this.gachaEntityPool = builder.getGachaEntityPool();
        this.hasDiceAbility = builder.hasDiceAbility();
        this.diceCooldown = builder.getDiceCooldown();
        this.diceSkillPool = builder.getDiceSkillPool();
        this.hasLuckyCloverAbility = builder.hasLuckyCloverAbility();
        this.hasDonkBowAbility = builder.hasDonkBowAbility();
        this.hasGourmetAbility = builder.hasGourmetAbility();
        this.gourmetHealthBonus = builder.getGourmetHealthBonus();
        this.gourmetDamageBonus = builder.getGourmetDamageBonus();
        this.gourmetDailyLimit = builder.getGourmetDailyLimit();
        this.hasForgetterAbility = builder.hasForgetterAbility();
        this.forgetterInterval = builder.getForgetterInterval();
        this.forgetterMinDuration = builder.getForgetterMinDuration();
        this.forgetterMaxDuration = builder.getForgetterMaxDuration();
        this.isFacelessDeceiver = builder.isFacelessDeceiver();
        this.isAngel = builder.isAngel();
        this.healthRegenInterval = builder.getHealthRegenInterval();
        this.healthRegenAmount = builder.getHealthRegenAmount();
        this.hasHalo = builder.hasHalo();
        this.haloDetectionRange = builder.getHaloDetectionRange();
        this.leatherArmorOnly = builder.isLeatherArmorOnly();
        this.ironArmorOnly = builder.isIronArmorOnly();
        this.bonusHealth = builder.getBonusHealth();
        this.bonusArmorToughness = builder.getBonusArmorToughness();
        this.meleeDamageBonus = builder.getMeleeDamageBonus();
        this.rangedDamageReduction = builder.getRangedDamageReduction();
        this.rangedDamagePenalty = builder.getRangedDamagePenalty();
        this.isDeathVenger = builder.isDeathVenger();
        this.hideNameTag = builder.isHideNameTag();
        this.hasMarkTargetAbility = builder.hasMarkTargetAbility();
        this.isImpostor = builder.isImpostor();
        this.impostorSkillCooldown = builder.getImpostorSkillCooldown();
        this.impostorDisguiseDuration = builder.getImpostorDisguiseDuration();
        this.hasAmbushAbility = builder.hasAmbushAbility();
        this.ambushMaxTargets = builder.getAmbushMaxTargets();
        this.ambushMaxDistance = builder.getAmbushMaxDistance();
        this.ambushCooldown = builder.getAmbushCooldown();
        this.ambushInvisDuration = builder.getAmbushInvisDuration();
        this.passiveInvisSeconds = builder.getPassiveInvisSeconds();
        this.isUndead = builder.isUndead();
        this.lifePoints = builder.getLifePoints();
        this.shieldPoints = builder.getShieldPoints();
        this.sunlightVulnerability = builder.hasSunlightVulnerability();
        this.sunlightDamageAmount = builder.getSunlightDamageAmount();
        this.sunlightDamageInterval = builder.getSunlightDamageInterval();
        this.bonusAttackReach = builder.getBonusAttackReach();
        this.hasEndlessMinerAbility = builder.hasEndlessMinerAbility();
        this.fortuneLevel = builder.getFortuneLevel();
        this.stoneDropChance = builder.getStoneDropChance();
        this.isHealer = builder.isHealer();
        this.healerPassiveRadius = builder.getHealerPassiveRadius();
        this.healerPassiveHealAmount = builder.getHealerPassiveHealAmount();
        this.healerActiveHealAmount = builder.getHealerActiveHealAmount();
        this.healerActiveCooldown = builder.getHealerActiveCooldown();
        this.isFool = builder.isFool();
        this.foolStealRange = builder.getFoolStealRange();
        this.foolStealCooldown = builder.getFoolStealCooldown();
        this.isHighPriest = builder.isHighPriest();
        this.highPriestSacrificeRange = builder.getHighPriestSacrificeRange();
        this.highPriestCooldown = builder.getHighPriestCooldown();
        this.highPriestReviveHealth = builder.getHighPriestReviveHealth();
        this.isGhostSenator = builder.isGhostSenator();
        this.ghostSenatorHealAmount = builder.getGhostSenatorHealAmount();
        this.ghostSenatorStrengthDuration = builder.getGhostSenatorStrengthDuration();
        this.ghostSenatorDetectionRadius = builder.getGhostSenatorDetectionRadius();
        this.isWraithCouncilor = builder.isWraithCouncilor();
        this.wraithSoulMax = builder.getWraithSoulMax();
        this.wraithSoulInitial = builder.getWraithSoulInitial();
        this.wraithSoulRegenRate = builder.getWraithSoulRegenRate();
        this.wraithSoulDarkBonus = builder.getWraithSoulDarkBonus();
        this.wraithSoulSunlightPenalty = builder.getWraithSoulSunlightPenalty();
        this.wraithSoulKillBonus = builder.getWraithSoulKillBonus();
        this.wraithSoulHitBonus = builder.getWraithSoulHitBonus();
        this.wraithSoulSummonKillBonus = builder.getWraithSoulSummonKillBonus();
        this.wraithErosionDamage = builder.getWraithErosionDamage();
        this.wraithErosionArmorReduction = builder.getWraithErosionArmorReduction();
        this.wraithErosionSlowPercent = builder.getWraithErosionSlowPercent();
        this.wraithErosionMaxStacks = builder.getWraithErosionMaxStacks();
        this.wraithErosionDuration = builder.getWraithErosionDuration();
        this.wraithSummonCost = builder.getWraithSummonCost();
        this.wraithSummonCooldown = builder.getWraithSummonCooldown();
        this.wraithSummonCount = builder.getWraithSummonCount();
        this.wraithSummonHealth = builder.getWraithSummonHealth();
        this.wraithSummonDamage = builder.getWraithSummonDamage();
        this.wraithSummonDuration = builder.getWraithSummonDuration();
        this.wraithSummonCorpseRange = builder.getWraithSummonCorpseRange();
        this.wraithSummonExtraMax = builder.getWraithSummonExtraMax();
        this.wraithDomainCost = builder.getWraithDomainCost();
        this.wraithDomainCooldown = builder.getWraithDomainCooldown();
        this.wraithDomainRadius = builder.getWraithDomainRadius();
        this.wraithDomainDuration = builder.getWraithDomainDuration();
        this.wraithDomainDamage = builder.getWraithDomainDamage();
        this.wraithDomainBossSlow = builder.getWraithDomainBossSlow();
        this.wraithDomainCharmDuration = builder.getWraithDomainCharmDuration();
        this.wraithBarrageBaseCost = builder.getWraithBarrageBaseCost();
        this.wraithBarrageChargedCost = builder.getWraithBarrageChargedCost();
        this.wraithBarrageCooldown = builder.getWraithBarrageCooldown();
        this.wraithBarrageBaseOrbs = builder.getWraithBarrageBaseOrbs();
        this.wraithBarrageChargedOrbs = builder.getWraithBarrageChargedOrbs();
        this.wraithBarrageBaseDamage = builder.getWraithBarrageBaseDamage();
        this.wraithBarrageChargedDamage = builder.getWraithBarrageChargedDamage();
        this.wraithBarrageBaseRange = builder.getWraithBarrageBaseRange();
        this.wraithBarrageChargedRange = builder.getWraithBarrageChargedRange();
        this.wraithBarrageMaxChargeTime = builder.getWraithBarrageMaxChargeTime();
        this.wraithBarrageMaxHits = builder.getWraithBarrageMaxHits();
        this.wraithUltimateMinCost = builder.getWraithUltimateMinCost();
        this.wraithUltimateCooldown = builder.getWraithUltimateCooldown();
        this.wraithUltimateCloneCount = builder.getWraithUltimateCloneCount();
        this.wraithUltimateCloneDamageRatio = builder.getWraithUltimateCloneDamageRatio();
        this.wraithUltimateDuration = builder.getWraithUltimateDuration();
        this.wraithUltimateSoulRegen = builder.getWraithUltimateSoulRegen();
        this.wraithUltimateDamageIncrease = builder.getWraithUltimateDamageIncrease();
        this.wraithUltimateExhaustDuration = builder.getWraithUltimateExhaustDuration();
        this.isEvilPoisoner = builder.isEvilPoisoner();
        this.poisonerStrengthDuration = builder.getPoisonerStrengthDuration();
        this.hasTurtleAura = builder.hasTurtleAura();
        this.turtleAuraRadius = builder.getTurtleAuraRadius();
        this.turtleAuraSlownessLevel = builder.getTurtleAuraSlownessLevel();
        this.turtleAuraDuration = builder.getTurtleAuraDuration();
        this.isJungleApeGod = builder.isJungleApeGod();
        this.rhythmStacksMax = builder.getRhythmStacksMax();
        this.rhythmAttackSpeedPerStack = builder.getRhythmAttackSpeedPerStack();
        this.rhythmMoveSpeedPerStack = builder.getRhythmMoveSpeedPerStack();
        this.berserkDuration = builder.getBerserkDuration();
        this.berserkCooldownReduction = builder.getBerserkCooldownReduction();
        this.berserkLifeSteal = builder.getBerserkLifeSteal();
        this.flatDamageReduction = builder.getFlatDamageReduction();
        this.resistanceChance = builder.getResistanceChance();
        this.resistanceDuration = builder.getResistanceDuration();
        this.q1DamageMultiplier = builder.getQ1DamageMultiplier();
        this.q1MovingTargetDamageMultiplier = builder.getQ1MovingTargetDamageMultiplier();
        this.q1SlowDuration = builder.getQ1SlowDuration();
        this.q1Cooldown = builder.getQ1Cooldown();
        this.q1Angle = builder.getQ1Angle();
        this.q2MaxDistance = builder.getQ2MaxDistance();
        this.q2DamageMultiplier = builder.getQ2DamageMultiplier();
        this.q2KnockbackDuration = builder.getQ2KnockbackDuration();
        this.q2SplashDamagePercent = builder.getQ2SplashDamagePercent();
        this.q2BonusAttackRange = builder.getQ2BonusAttackRange();
        this.q2BonusAttackDuration = builder.getQ2BonusAttackDuration();
        this.q2Cooldown = builder.getQ2Cooldown();
        this.q3Radius = builder.getQ3Radius();
        this.q3DamageMultiplier = builder.getQ3DamageMultiplier();
        this.q3FearDuration = builder.getQ3FearDuration();
        this.q3BerserkFearDuration = builder.getQ3BerserkFearDuration();
        this.q3WeaknessDuration = builder.getQ3WeaknessDuration();
        this.q3Cooldown = builder.getQ3Cooldown();
        this.rDuration = builder.getRDuration();
        this.rHealPercent = builder.getRHealPercent();
        this.rHealthBonusPercent = builder.getRHealthBonusPercent();
        this.rPowerLevel = builder.getRPowerLevel();
        this.rSpeedLevel = builder.getRSpeedLevel();
        this.rFatigueDuration = builder.getRFatigueDuration();
        this.rCooldown = builder.getRCooldown();
        this.isHeavyKnight = builder.isHeavyKnight();
        this.heavyKnightSpeedPenalty = builder.getHeavyKnightSpeedPenalty();
        this.heavyKnightWillOnHit = builder.getHeavyKnightWillOnHit();
        this.heavyKnightWillOnBlock = builder.getHeavyKnightWillOnBlock();
        this.heavyKnightWillOnDamaged = builder.getHeavyKnightWillOnDamaged();
        this.heavyKnightFullWillDamageBonus = builder.getHeavyKnightFullWillDamageBonus();
        this.heavyKnightFullWillDamageReduction = builder.getHeavyKnightFullWillDamageReduction();
        this.heavyKnightShieldWallTriggerTicks = builder.getHeavyKnightShieldWallTriggerTicks();
        this.heavyKnightShieldWallReduction = builder.getHeavyKnightShieldWallReduction();
        this.heavyKnightShieldWallRadius = builder.getHeavyKnightShieldWallRadius();
        this.heavyKnightShieldWallArmorBonus = builder.getHeavyKnightShieldWallArmorBonus();
        this.heavyKnightChargeWillCost = builder.getHeavyKnightChargeWillCost();
        this.heavyKnightChargeCooldown = builder.getHeavyKnightChargeCooldown();
        this.heavyKnightChargeDistance = builder.getHeavyKnightChargeDistance();
        this.heavyKnightChargeDamage = builder.getHeavyKnightChargeDamage();
        this.heavyKnightChargeKnockback = builder.getHeavyKnightChargeKnockback();
        this.heavyKnightProtectRange = builder.getHeavyKnightProtectRange();
        this.heavyKnightProtectWillCost = builder.getHeavyKnightProtectWillCost();
        this.heavyKnightProtectCooldown = builder.getHeavyKnightProtectCooldown();
        this.heavyKnightShieldBashDamage = builder.getHeavyKnightShieldBashDamage();
        this.heavyKnightShieldBashStunDuration = builder.getHeavyKnightShieldBashStunDuration();
        this.heavyKnightShieldBashCooldown = builder.getHeavyKnightShieldBashCooldown();
        this.isByteChen = builder.isByteChen();
        this.byteChenComputeMax = builder.getByteChenComputeMax();
        this.byteChenComputeInitial = builder.getByteChenComputeInitial();
        this.byteChenComputeRegenRate = builder.getByteChenComputeRegenRate();
        this.byteChenComputeNodeRegenBonus = builder.getByteChenComputeNodeRegenBonus();
        this.byteChenComputeOnRead = builder.getByteChenComputeOnRead();
        this.byteChenComputeOnNodeTrigger = builder.getByteChenComputeOnNodeTrigger();
        this.byteChenComputeOnInterrupt = builder.getByteChenComputeOnInterrupt();
        this.byteChenComputeLowThreshold = builder.getByteChenComputeLowThreshold();
        this.byteChenNodeMax = builder.getByteChenNodeMax();
        this.byteChenNodeRange = builder.getByteChenNodeRange();
        this.byteChenNodeDuration = builder.getByteChenNodeDuration();
        this.byteChenScoutNodeCost = builder.getByteChenScoutNodeCost();
        this.byteChenScoutNodeRadius = builder.getByteChenScoutNodeRadius();
        this.byteChenScoutNodeComputeRegen = builder.getByteChenScoutNodeComputeRegen();
        this.byteChenBuffNodeCost = builder.getByteChenBuffNodeCost();
        this.byteChenBuffNodeRadius = builder.getByteChenBuffNodeRadius();
        this.byteChenBuffNodeDamageBonus = builder.getByteChenBuffNodeDamageBonus();
        this.byteChenBuffNodeDamageReduction = builder.getByteChenBuffNodeDamageReduction();
        this.byteChenBuffNodeSpeedBonus = builder.getByteChenBuffNodeSpeedBonus();
        this.byteChenBuffNodeCooldownReduction = builder.getByteChenBuffNodeCooldownReduction();
        this.byteChenBuffNodeMaxStacks = builder.getByteChenBuffNodeMaxStacks();
        this.byteChenCounterNodeCost = builder.getByteChenCounterNodeCost();
        this.byteChenCounterNodeRadius = builder.getByteChenCounterNodeRadius();
        this.byteChenCounterNodeSilenceDuration = builder.getByteChenCounterNodeSilenceDuration();
        this.byteChenCounterNodeDisorderDuration = builder.getByteChenCounterNodeDisorderDuration();
        this.byteChenCounterNodeDamageReduction = builder.getByteChenCounterNodeDamageReduction();
        this.byteChenCounterNodeCooldownMultiplier = builder.getByteChenCounterNodeCooldownMultiplier();
        this.byteChenDataVisionRange = builder.getByteChenDataVisionRange();
        this.byteChenLightweightSpeedBonus = builder.getByteChenLightweightSpeedBonus();
        this.byteChenLightweightDigSpeedBonus = builder.getByteChenLightweightDigSpeedBonus();
        this.byteChenLightweightMeleePenalty = builder.getByteChenLightweightMeleePenalty();
        this.byteChenLightweightReceivedMeleePenalty = builder.getByteChenLightweightReceivedMeleePenalty();
        this.byteChenNodeRecycleCooldown = builder.getByteChenNodeRecycleCooldown();
        this.byteChenNodeRecycleRefund = builder.getByteChenNodeRecycleRefund();
        this.byteChenFullReadCost = builder.getByteChenFullReadCost();
        this.byteChenFullReadCooldown = builder.getByteChenFullReadCooldown();
        this.byteChenFullReadRadius = builder.getByteChenFullReadRadius();
        this.byteChenFullReadDuration = builder.getByteChenFullReadDuration();
        this.byteChenDataDispatchCost = builder.getByteChenDataDispatchCost();
        this.byteChenDataDispatchCooldown = builder.getByteChenDataDispatchCooldown();
        this.byteChenDataDispatchSpeedBonus = builder.getByteChenDataDispatchSpeedBonus();
        this.byteChenDataDispatchCooldownBonus = builder.getByteChenDataDispatchCooldownBonus();
        this.byteChenDataDispatchBuffDuration = builder.getByteChenDataDispatchBuffDuration();
        this.byteChenDataBanCost = builder.getByteChenDataBanCost();
        this.byteChenDataBanCooldown = builder.getByteChenDataBanCooldown();
        this.byteChenDataBanRange = builder.getByteChenDataBanRange();
        this.byteChenDataBanDuration = builder.getByteChenDataBanDuration();
        this.byteChenDataBanBossDuration = builder.getByteChenDataBanBossDuration();
        this.byteChenDataBanTrueDamageBonus = builder.getByteChenDataBanTrueDamageBonus();
        this.byteChenUltimateMinCost = builder.getByteChenUltimateMinCost();
        this.byteChenUltimateCooldown = builder.getByteChenUltimateCooldown();
        this.byteChenUltimateRadius = builder.getByteChenUltimateRadius();
        this.byteChenUltimateDuration = builder.getByteChenUltimateDuration();
        this.byteChenUltimateDamageBonus = builder.getByteChenUltimateDamageBonus();
        this.byteChenUltimateDamageReduction = builder.getByteChenUltimateDamageReduction();
        this.byteChenUltimateSpeedBonus = builder.getByteChenUltimateSpeedBonus();
        this.byteChenUltimateCooldownBonus = builder.getByteChenUltimateCooldownBonus();
        this.byteChenUltimateEnemyDamageReduction = builder.getByteChenUltimateEnemyDamageReduction();
        this.byteChenUltimateEnemySpeedReduction = builder.getByteChenUltimateEnemySpeedReduction();
        this.byteChenUltimateEnemyCooldownMultiplier = builder.getByteChenUltimateEnemyCooldownMultiplier();
        this.byteChenUltimateInterruptChance = builder.getByteChenUltimateInterruptChance();
        this.byteChenExhaustDuration = builder.getByteChenExhaustDuration();
    }

    public static ProfessionBuilder builder() {
        return new ProfessionBuilder();
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean requiresPassword() { return requiresPassword; }
    public String getPassword() { return password; }
    public String getIconItem() { return iconItem; }
    public float getBonusDamagePercent() { return bonusDamagePercent; }
    public int getSlownessLevel() { return slownessLevel; }
    public int getWeaknessLevel() { return weaknessLevel; }
    public float getBonusArmor() { return bonusArmor; }
    public float getPoisonChance() { return poisonChance; }
    public int getPoisonDuration() { return poisonDuration; }
    public int getPoisonDamage() { return poisonDamage; }
    public float getFireDamageMultiplier() { return fireDamageMultiplier; }
    public String getResourceItem() { return resourceItem; }
    public int getResourceInterval() { return resourceInterval; }
    public int getResourceAmount() { return resourceAmount; }
    public boolean hasEnderPearlAbility() { return hasEnderPearlAbility; }
    public int getEnderPearlCooldown() { return enderPearlCooldown; }
    public boolean hasWaterDamage() { return waterDamage; }
    public float getWaterDamageAmount() { return waterDamageAmount; }
    public int getWaterDamageInterval() { return waterDamageInterval; }
    public boolean isFireTrailEnabled() { return fireTrailEnabled; }
    public float getFireTrailDamage() { return fireTrailDamage; }
    public int getFireTrailDuration() { return fireTrailDuration; }
    public float getFireTrailRadius() { return fireTrailRadius; }
    public float getFireDamageBonusPercent() { return fireDamageBonusPercent; }
    public boolean hasFireImmunity() { return fireImmunity; }
    public boolean hasWaterWeakness() { return waterWeakness; }
    public float getWaterWeaknessDamagePercent() { return waterWeaknessDamagePercent; }
    public int getWaterWeaknessInterval() { return waterWeaknessInterval; }
    public boolean hasRainWeakness() { return rainWeakness; }
    public boolean isFaceless() { return isFaceless; }
    public int getSwitchInterval() { return switchInterval; }
    public boolean canMountCreatures() { return canMountCreatures; }
    public float getMountSpeedBonus() { return mountSpeedBonus; }
    public float getMountDamageBonus() { return mountDamageBonus; }
    public int getMountControlRange() { return mountControlRange; }
    public float getMountHealthBonus() { return mountHealthBonus; }
    public boolean hasGachaAbility() { return hasGachaAbility; }
    public int getGachaInterval() { return gachaInterval; }
    public List<String> getGachaEntityPool() { return gachaEntityPool; }
    public boolean hasDiceAbility() { return hasDiceAbility; }
    public int getDiceCooldown() { return diceCooldown; }
    public List<String> getDiceSkillPool() { return diceSkillPool; }
    public boolean hasLuckyCloverAbility() { return hasLuckyCloverAbility; }
    public boolean hasDonkBowAbility() { return hasDonkBowAbility; }
    public boolean hasGourmetAbility() { return hasGourmetAbility; }
    public float getGourmetHealthBonus() { return gourmetHealthBonus; }
    public float getGourmetDamageBonus() { return gourmetDamageBonus; }
    public int getGourmetDailyLimit() { return gourmetDailyLimit; }
    public boolean hasForgetterAbility() { return hasForgetterAbility; }
    public int getForgetterInterval() { return forgetterInterval; }
    public int getForgetterMinDuration() { return forgetterMinDuration; }
    public int getForgetterMaxDuration() { return forgetterMaxDuration; }
    public boolean isFacelessDeceiver() { return isFacelessDeceiver; }
    public boolean isAngel() { return isAngel; }
    public int getHealthRegenInterval() { return healthRegenInterval; }
    public float getHealthRegenAmount() { return healthRegenAmount; }
    public boolean hasHalo() { return hasHalo; }
    public float getHaloDetectionRange() { return haloDetectionRange; }
    public boolean isLeatherArmorOnly() { return leatherArmorOnly; }
    public boolean isIronArmorOnly() { return ironArmorOnly; }
    public float getBonusHealth() { return bonusHealth; }
    public float getBonusArmorToughness() { return bonusArmorToughness; }
    public float getMeleeDamageBonus() { return meleeDamageBonus; }
    public float getRangedDamageReduction() { return rangedDamageReduction; }
    public float getRangedDamagePenalty() { return rangedDamagePenalty; }
    public boolean isDeathVenger() { return isDeathVenger; }
    public boolean isHideNameTag() { return hideNameTag; }
    public boolean hasMarkTargetAbility() { return hasMarkTargetAbility; }
    public boolean isImpostor() { return isImpostor; }
    public int getImpostorSkillCooldown() { return impostorSkillCooldown; }
    public int getImpostorDisguiseDuration() { return impostorDisguiseDuration; }
    public boolean hasAmbushAbility() { return hasAmbushAbility; }
    public int getAmbushMaxTargets() { return ambushMaxTargets; }
    public int getAmbushMaxDistance() { return ambushMaxDistance; }
    public int getAmbushCooldown() { return ambushCooldown; }
    public int getAmbushInvisDuration() { return ambushInvisDuration; }
    public int getPassiveInvisSeconds() { return passiveInvisSeconds; }
    public boolean isUndead() { return isUndead; }
    public int getLifePoints() { return lifePoints; }
    public int getShieldPoints() { return shieldPoints; }
    public boolean hasSunlightVulnerability() { return sunlightVulnerability; }
    public float getSunlightDamageAmount() { return sunlightDamageAmount; }
    public int getSunlightDamageInterval() { return sunlightDamageInterval; }
    public float getBonusAttackReach() { return bonusAttackReach; }
    public boolean hasEndlessMinerAbility() { return hasEndlessMinerAbility; }
    public int getFortuneLevel() { return fortuneLevel; }
    public float getStoneDropChance() { return stoneDropChance; }
    public boolean isHealer() { return isHealer; }
    public float getHealerPassiveRadius() { return healerPassiveRadius; }
    public float getHealerPassiveHealAmount() { return healerPassiveHealAmount; }
    public float getHealerActiveHealAmount() { return healerActiveHealAmount; }
    public int getHealerActiveCooldown() { return healerActiveCooldown; }
    public boolean isFool() { return isFool; }
    public float getFoolStealRange() { return foolStealRange; }
    public int getFoolStealCooldown() { return foolStealCooldown; }
    public boolean isHighPriest() { return isHighPriest; }
    public float getHighPriestSacrificeRange() { return highPriestSacrificeRange; }
    public int getHighPriestCooldown() { return highPriestCooldown; }
    public float getHighPriestReviveHealth() { return highPriestReviveHealth; }
    public boolean isGhostSenator() { return isGhostSenator; }
    public float getGhostSenatorHealAmount() { return ghostSenatorHealAmount; }
    public int getGhostSenatorStrengthDuration() { return ghostSenatorStrengthDuration; }
    public float getGhostSenatorDetectionRadius() { return ghostSenatorDetectionRadius; }
    public boolean isWraithCouncilor() { return isWraithCouncilor; }
    public int getWraithSoulMax() { return wraithSoulMax; }
    public int getWraithSoulInitial() { return wraithSoulInitial; }
    public int getWraithSoulRegenRate() { return wraithSoulRegenRate; }
    public int getWraithSoulDarkBonus() { return wraithSoulDarkBonus; }
    public float getWraithSoulSunlightPenalty() { return wraithSoulSunlightPenalty; }
    public int getWraithSoulKillBonus() { return wraithSoulKillBonus; }
    public int getWraithSoulHitBonus() { return wraithSoulHitBonus; }
    public int getWraithSoulSummonKillBonus() { return wraithSoulSummonKillBonus; }
    public float getWraithErosionDamage() { return wraithErosionDamage; }
    public float getWraithErosionArmorReduction() { return wraithErosionArmorReduction; }
    public float getWraithErosionSlowPercent() { return wraithErosionSlowPercent; }
    public int getWraithErosionMaxStacks() { return wraithErosionMaxStacks; }
    public int getWraithErosionDuration() { return wraithErosionDuration; }
    public int getWraithSummonCost() { return wraithSummonCost; }
    public int getWraithSummonCooldown() { return wraithSummonCooldown; }
    public int getWraithSummonCount() { return wraithSummonCount; }
    public float getWraithSummonHealth() { return wraithSummonHealth; }
    public float getWraithSummonDamage() { return wraithSummonDamage; }
    public int getWraithSummonDuration() { return wraithSummonDuration; }
    public float getWraithSummonCorpseRange() { return wraithSummonCorpseRange; }
    public int getWraithSummonExtraMax() { return wraithSummonExtraMax; }
    public int getWraithDomainCost() { return wraithDomainCost; }
    public int getWraithDomainCooldown() { return wraithDomainCooldown; }
    public float getWraithDomainRadius() { return wraithDomainRadius; }
    public int getWraithDomainDuration() { return wraithDomainDuration; }
    public float getWraithDomainDamage() { return wraithDomainDamage; }
    public float getWraithDomainBossSlow() { return wraithDomainBossSlow; }
    public int getWraithDomainCharmDuration() { return wraithDomainCharmDuration; }
    public int getWraithBarrageBaseCost() { return wraithBarrageBaseCost; }
    public int getWraithBarrageChargedCost() { return wraithBarrageChargedCost; }
    public int getWraithBarrageCooldown() { return wraithBarrageCooldown; }
    public int getWraithBarrageBaseOrbs() { return wraithBarrageBaseOrbs; }
    public int getWraithBarrageChargedOrbs() { return wraithBarrageChargedOrbs; }
    public float getWraithBarrageBaseDamage() { return wraithBarrageBaseDamage; }
    public float getWraithBarrageChargedDamage() { return wraithBarrageChargedDamage; }
    public float getWraithBarrageBaseRange() { return wraithBarrageBaseRange; }
    public float getWraithBarrageChargedRange() { return wraithBarrageChargedRange; }
    public int getWraithBarrageMaxChargeTime() { return wraithBarrageMaxChargeTime; }
    public int getWraithBarrageMaxHits() { return wraithBarrageMaxHits; }
    public int getWraithUltimateMinCost() { return wraithUltimateMinCost; }
    public int getWraithUltimateCooldown() { return wraithUltimateCooldown; }
    public int getWraithUltimateCloneCount() { return wraithUltimateCloneCount; }
    public float getWraithUltimateCloneDamageRatio() { return wraithUltimateCloneDamageRatio; }
    public int getWraithUltimateDuration() { return wraithUltimateDuration; }
    public int getWraithUltimateSoulRegen() { return wraithUltimateSoulRegen; }
    public float getWraithUltimateDamageIncrease() { return wraithUltimateDamageIncrease; }
    public int getWraithUltimateExhaustDuration() { return wraithUltimateExhaustDuration; }
    public boolean isEvilPoisoner() { return isEvilPoisoner; }
    public int getPoisonerStrengthDuration() { return poisonerStrengthDuration; }
    public boolean hasTurtleAura() { return hasTurtleAura; }
    public float getTurtleAuraRadius() { return turtleAuraRadius; }
    public int getTurtleAuraSlownessLevel() { return turtleAuraSlownessLevel; }
    public int getTurtleAuraDuration() { return turtleAuraDuration; }
    public boolean isJungleApeGod() { return isJungleApeGod; }
    public int getRhythmStacksMax() { return rhythmStacksMax; }
    public float getRhythmAttackSpeedPerStack() { return rhythmAttackSpeedPerStack; }
    public float getRhythmMoveSpeedPerStack() { return rhythmMoveSpeedPerStack; }
    public int getBerserkDuration() { return berserkDuration; }
    public float getBerserkCooldownReduction() { return berserkCooldownReduction; }
    public float getBerserkLifeSteal() { return berserkLifeSteal; }
    public float getFlatDamageReduction() { return flatDamageReduction; }
    public float getResistanceChance() { return resistanceChance; }
    public int getResistanceDuration() { return resistanceDuration; }
    public float getQ1DamageMultiplier() { return q1DamageMultiplier; }
    public float getQ1MovingTargetDamageMultiplier() { return q1MovingTargetDamageMultiplier; }
    public int getQ1SlowDuration() { return q1SlowDuration; }
    public int getQ1Cooldown() { return q1Cooldown; }
    public float getQ1Angle() { return q1Angle; }
    public float getQ2MaxDistance() { return q2MaxDistance; }
    public float getQ2DamageMultiplier() { return q2DamageMultiplier; }
    public float getQ2KnockbackDuration() { return q2KnockbackDuration; }
    public float getQ2SplashDamagePercent() { return q2SplashDamagePercent; }
    public float getQ2BonusAttackRange() { return q2BonusAttackRange; }
    public int getQ2BonusAttackDuration() { return q2BonusAttackDuration; }
    public int getQ2Cooldown() { return q2Cooldown; }
    public float getQ3Radius() { return q3Radius; }
    public float getQ3DamageMultiplier() { return q3DamageMultiplier; }
    public float getQ3FearDuration() { return q3FearDuration; }
    public float getQ3BerserkFearDuration() { return q3BerserkFearDuration; }
    public int getQ3WeaknessDuration() { return q3WeaknessDuration; }
    public int getQ3Cooldown() { return q3Cooldown; }
    public int getRDuration() { return rDuration; }
    public float getRHealPercent() { return rHealPercent; }
    public float getRHealthBonusPercent() { return rHealthBonusPercent; }
    public int getRPowerLevel() { return rPowerLevel; }
    public int getRSpeedLevel() { return rSpeedLevel; }
    public int getRFatigueDuration() { return rFatigueDuration; }
    public int getRCooldown() { return rCooldown; }

    public boolean isByteChen() { return isByteChen; }
    public int getByteChenComputeMax() { return byteChenComputeMax; }
    public int getByteChenComputeInitial() { return byteChenComputeInitial; }
    public int getByteChenComputeRegenRate() { return byteChenComputeRegenRate; }
    public float getByteChenComputeNodeRegenBonus() { return byteChenComputeNodeRegenBonus; }
    public int getByteChenComputeOnRead() { return byteChenComputeOnRead; }
    public int getByteChenComputeOnNodeTrigger() { return byteChenComputeOnNodeTrigger; }
    public int getByteChenComputeOnInterrupt() { return byteChenComputeOnInterrupt; }
    public int getByteChenComputeLowThreshold() { return byteChenComputeLowThreshold; }
    public int getByteChenNodeMax() { return byteChenNodeMax; }
    public int getByteChenNodeRange() { return byteChenNodeRange; }
    public int getByteChenNodeDuration() { return byteChenNodeDuration; }
    public int getByteChenScoutNodeCost() { return byteChenScoutNodeCost; }
    public float getByteChenScoutNodeRadius() { return byteChenScoutNodeRadius; }
    public int getByteChenScoutNodeComputeRegen() { return byteChenScoutNodeComputeRegen; }
    public int getByteChenBuffNodeCost() { return byteChenBuffNodeCost; }
    public float getByteChenBuffNodeRadius() { return byteChenBuffNodeRadius; }
    public float getByteChenBuffNodeDamageBonus() { return byteChenBuffNodeDamageBonus; }
    public float getByteChenBuffNodeDamageReduction() { return byteChenBuffNodeDamageReduction; }
    public float getByteChenBuffNodeSpeedBonus() { return byteChenBuffNodeSpeedBonus; }
    public float getByteChenBuffNodeCooldownReduction() { return byteChenBuffNodeCooldownReduction; }
    public int getByteChenBuffNodeMaxStacks() { return byteChenBuffNodeMaxStacks; }
    public int getByteChenCounterNodeCost() { return byteChenCounterNodeCost; }
    public float getByteChenCounterNodeRadius() { return byteChenCounterNodeRadius; }
    public int getByteChenCounterNodeSilenceDuration() { return byteChenCounterNodeSilenceDuration; }
    public int getByteChenCounterNodeDisorderDuration() { return byteChenCounterNodeDisorderDuration; }
    public float getByteChenCounterNodeDamageReduction() { return byteChenCounterNodeDamageReduction; }
    public float getByteChenCounterNodeCooldownMultiplier() { return byteChenCounterNodeCooldownMultiplier; }
    public float getByteChenDataVisionRange() { return byteChenDataVisionRange; }
    public float getByteChenLightweightSpeedBonus() { return byteChenLightweightSpeedBonus; }
    public float getByteChenLightweightDigSpeedBonus() { return byteChenLightweightDigSpeedBonus; }
    public float getByteChenLightweightMeleePenalty() { return byteChenLightweightMeleePenalty; }
    public float getByteChenLightweightReceivedMeleePenalty() { return byteChenLightweightReceivedMeleePenalty; }
    public int getByteChenNodeRecycleCooldown() { return byteChenNodeRecycleCooldown; }
    public float getByteChenNodeRecycleRefund() { return byteChenNodeRecycleRefund; }
    public int getByteChenFullReadCost() { return byteChenFullReadCost; }
    public int getByteChenFullReadCooldown() { return byteChenFullReadCooldown; }
    public float getByteChenFullReadRadius() { return byteChenFullReadRadius; }
    public int getByteChenFullReadDuration() { return byteChenFullReadDuration; }
    public int getByteChenDataDispatchCost() { return byteChenDataDispatchCost; }
    public int getByteChenDataDispatchCooldown() { return byteChenDataDispatchCooldown; }
    public float getByteChenDataDispatchSpeedBonus() { return byteChenDataDispatchSpeedBonus; }
    public float getByteChenDataDispatchCooldownBonus() { return byteChenDataDispatchCooldownBonus; }
    public int getByteChenDataDispatchBuffDuration() { return byteChenDataDispatchBuffDuration; }
    public int getByteChenDataBanCost() { return byteChenDataBanCost; }
    public int getByteChenDataBanCooldown() { return byteChenDataBanCooldown; }
    public float getByteChenDataBanRange() { return byteChenDataBanRange; }
    public int getByteChenDataBanDuration() { return byteChenDataBanDuration; }
    public int getByteChenDataBanBossDuration() { return byteChenDataBanBossDuration; }
    public float getByteChenDataBanTrueDamageBonus() { return byteChenDataBanTrueDamageBonus; }
    public int getByteChenUltimateMinCost() { return byteChenUltimateMinCost; }
    public int getByteChenUltimateCooldown() { return byteChenUltimateCooldown; }
    public float getByteChenUltimateRadius() { return byteChenUltimateRadius; }
    public int getByteChenUltimateDuration() { return byteChenUltimateDuration; }
    public float getByteChenUltimateDamageBonus() { return byteChenUltimateDamageBonus; }
    public float getByteChenUltimateDamageReduction() { return byteChenUltimateDamageReduction; }
    public float getByteChenUltimateSpeedBonus() { return byteChenUltimateSpeedBonus; }
    public float getByteChenUltimateCooldownBonus() { return byteChenUltimateCooldownBonus; }
    public float getByteChenUltimateEnemyDamageReduction() { return byteChenUltimateEnemyDamageReduction; }
    public float getByteChenUltimateEnemySpeedReduction() { return byteChenUltimateEnemySpeedReduction; }
    public float getByteChenUltimateEnemyCooldownMultiplier() { return byteChenUltimateEnemyCooldownMultiplier; }
    public float getByteChenUltimateInterruptChance() { return byteChenUltimateInterruptChance; }
    public int getByteChenExhaustDuration() { return byteChenExhaustDuration; }

    public boolean isHeavyKnight() { return isHeavyKnight; }
    public float getHeavyKnightSpeedPenalty() { return heavyKnightSpeedPenalty; }
    public int getHeavyKnightWillOnHit() { return heavyKnightWillOnHit; }
    public int getHeavyKnightWillOnBlock() { return heavyKnightWillOnBlock; }
    public int getHeavyKnightWillOnDamaged() { return heavyKnightWillOnDamaged; }
    public float getHeavyKnightFullWillDamageBonus() { return heavyKnightFullWillDamageBonus; }
    public float getHeavyKnightFullWillDamageReduction() { return heavyKnightFullWillDamageReduction; }
    public int getHeavyKnightShieldWallTriggerTicks() { return heavyKnightShieldWallTriggerTicks; }
    public float getHeavyKnightShieldWallReduction() { return heavyKnightShieldWallReduction; }
    public float getHeavyKnightShieldWallRadius() { return heavyKnightShieldWallRadius; }
    public float getHeavyKnightShieldWallArmorBonus() { return heavyKnightShieldWallArmorBonus; }
    public int getHeavyKnightChargeWillCost() { return heavyKnightChargeWillCost; }
    public int getHeavyKnightChargeCooldown() { return heavyKnightChargeCooldown; }
    public float getHeavyKnightChargeDistance() { return heavyKnightChargeDistance; }
    public float getHeavyKnightChargeDamage() { return heavyKnightChargeDamage; }
    public float getHeavyKnightChargeKnockback() { return heavyKnightChargeKnockback; }
    public int getHeavyKnightProtectRange() { return heavyKnightProtectRange; }
    public int getHeavyKnightProtectWillCost() { return heavyKnightProtectWillCost; }
    public int getHeavyKnightProtectCooldown() { return heavyKnightProtectCooldown; }
    public float getHeavyKnightShieldBashDamage() { return heavyKnightShieldBashDamage; }
    public int getHeavyKnightShieldBashStunDuration() { return heavyKnightShieldBashStunDuration; }
    public int getHeavyKnightShieldBashCooldown() { return heavyKnightShieldBashCooldown; }

    public boolean hasBonusDamage() { return bonusDamagePercent > 0; }
    public boolean hasSlowness() { return slownessLevel > 0; }
    public boolean hasWeakness() { return weaknessLevel > 0; }
    public boolean hasBonusArmor() { return bonusArmor > 0; }
    public boolean hasPoisonChance() { return poisonChance > 0; }
    public boolean hasResourceGeneration() { return resourceItem != null && !resourceItem.isEmpty() && resourceInterval > 0; }
    public boolean hasMountAbility() { return canMountCreatures; }
    public boolean hasFireTrailEnabled() { return fireTrailEnabled; }
    public boolean shouldHideNameTag() { return hideNameTag; }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
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
        saveStringList(tag, "gachaEntityPool", gachaEntityPool);
        tag.putBoolean("hasDiceAbility", hasDiceAbility);
        tag.putInt("diceCooldown", diceCooldown);
        saveStringList(tag, "diceSkillPool", diceSkillPool);
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
        tag.putBoolean("ironArmorOnly", ironArmorOnly);
        tag.putFloat("bonusHealth", bonusHealth);
        tag.putFloat("bonusArmorToughness", bonusArmorToughness);
        tag.putFloat("meleeDamageBonus", meleeDamageBonus);
        tag.putFloat("rangedDamageReduction", rangedDamageReduction);
        tag.putFloat("rangedDamagePenalty", rangedDamagePenalty);
        tag.putBoolean("isDeathVenger", isDeathVenger);
        tag.putBoolean("hideNameTag", hideNameTag);
        tag.putBoolean("hasMarkTargetAbility", hasMarkTargetAbility);
        tag.putBoolean("isImpostor", isImpostor);
        tag.putInt("impostorSkillCooldown", impostorSkillCooldown);
        tag.putInt("impostorDisguiseDuration", impostorDisguiseDuration);
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
        tag.putFloat("bonusAttackReach", bonusAttackReach);
        tag.putBoolean("hasEndlessMinerAbility", hasEndlessMinerAbility);
        tag.putInt("fortuneLevel", fortuneLevel);
        tag.putFloat("stoneDropChance", stoneDropChance);
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
        tag.putBoolean("isGhostSenator", isGhostSenator);
        tag.putFloat("ghostSenatorHealAmount", ghostSenatorHealAmount);
        tag.putInt("ghostSenatorStrengthDuration", ghostSenatorStrengthDuration);
        tag.putFloat("ghostSenatorDetectionRadius", ghostSenatorDetectionRadius);
        tag.putBoolean("isWraithCouncilor", isWraithCouncilor);
        tag.putInt("wraithSoulMax", wraithSoulMax);
        tag.putInt("wraithSoulInitial", wraithSoulInitial);
        tag.putInt("wraithSoulRegenRate", wraithSoulRegenRate);
        tag.putInt("wraithSoulDarkBonus", wraithSoulDarkBonus);
        tag.putFloat("wraithSoulSunlightPenalty", wraithSoulSunlightPenalty);
        tag.putInt("wraithSoulKillBonus", wraithSoulKillBonus);
        tag.putInt("wraithSoulHitBonus", wraithSoulHitBonus);
        tag.putInt("wraithSoulSummonKillBonus", wraithSoulSummonKillBonus);
        tag.putFloat("wraithErosionDamage", wraithErosionDamage);
        tag.putFloat("wraithErosionArmorReduction", wraithErosionArmorReduction);
        tag.putFloat("wraithErosionSlowPercent", wraithErosionSlowPercent);
        tag.putInt("wraithErosionMaxStacks", wraithErosionMaxStacks);
        tag.putInt("wraithErosionDuration", wraithErosionDuration);
        tag.putInt("wraithSummonCost", wraithSummonCost);
        tag.putInt("wraithSummonCooldown", wraithSummonCooldown);
        tag.putInt("wraithSummonCount", wraithSummonCount);
        tag.putFloat("wraithSummonHealth", wraithSummonHealth);
        tag.putFloat("wraithSummonDamage", wraithSummonDamage);
        tag.putInt("wraithSummonDuration", wraithSummonDuration);
        tag.putFloat("wraithSummonCorpseRange", wraithSummonCorpseRange);
        tag.putInt("wraithSummonExtraMax", wraithSummonExtraMax);
        tag.putInt("wraithDomainCost", wraithDomainCost);
        tag.putInt("wraithDomainCooldown", wraithDomainCooldown);
        tag.putFloat("wraithDomainRadius", wraithDomainRadius);
        tag.putInt("wraithDomainDuration", wraithDomainDuration);
        tag.putFloat("wraithDomainDamage", wraithDomainDamage);
        tag.putFloat("wraithDomainBossSlow", wraithDomainBossSlow);
        tag.putInt("wraithDomainCharmDuration", wraithDomainCharmDuration);
        tag.putInt("wraithBarrageBaseCost", wraithBarrageBaseCost);
        tag.putInt("wraithBarrageChargedCost", wraithBarrageChargedCost);
        tag.putInt("wraithBarrageCooldown", wraithBarrageCooldown);
        tag.putInt("wraithBarrageBaseOrbs", wraithBarrageBaseOrbs);
        tag.putInt("wraithBarrageChargedOrbs", wraithBarrageChargedOrbs);
        tag.putFloat("wraithBarrageBaseDamage", wraithBarrageBaseDamage);
        tag.putFloat("wraithBarrageChargedDamage", wraithBarrageChargedDamage);
        tag.putFloat("wraithBarrageBaseRange", wraithBarrageBaseRange);
        tag.putFloat("wraithBarrageChargedRange", wraithBarrageChargedRange);
        tag.putInt("wraithBarrageMaxChargeTime", wraithBarrageMaxChargeTime);
        tag.putInt("wraithBarrageMaxHits", wraithBarrageMaxHits);
        tag.putInt("wraithUltimateMinCost", wraithUltimateMinCost);
        tag.putInt("wraithUltimateCooldown", wraithUltimateCooldown);
        tag.putInt("wraithUltimateCloneCount", wraithUltimateCloneCount);
        tag.putFloat("wraithUltimateCloneDamageRatio", wraithUltimateCloneDamageRatio);
        tag.putInt("wraithUltimateDuration", wraithUltimateDuration);
        tag.putInt("wraithUltimateSoulRegen", wraithUltimateSoulRegen);
        tag.putFloat("wraithUltimateDamageIncrease", wraithUltimateDamageIncrease);
        tag.putInt("wraithUltimateExhaustDuration", wraithUltimateExhaustDuration);
        tag.putBoolean("isEvilPoisoner", isEvilPoisoner);
        tag.putInt("poisonerStrengthDuration", poisonerStrengthDuration);
        tag.putBoolean("hasTurtleAura", hasTurtleAura);
        tag.putFloat("turtleAuraRadius", turtleAuraRadius);
        tag.putInt("turtleAuraSlownessLevel", turtleAuraSlownessLevel);
        tag.putInt("turtleAuraDuration", turtleAuraDuration);
        tag.putBoolean("isJungleApeGod", isJungleApeGod);
        tag.putInt("rhythmStacksMax", rhythmStacksMax);
        tag.putFloat("rhythmAttackSpeedPerStack", rhythmAttackSpeedPerStack);
        tag.putFloat("rhythmMoveSpeedPerStack", rhythmMoveSpeedPerStack);
        tag.putInt("berserkDuration", berserkDuration);
        tag.putFloat("berserkCooldownReduction", berserkCooldownReduction);
        tag.putFloat("berserkLifeSteal", berserkLifeSteal);
        tag.putFloat("flatDamageReduction", flatDamageReduction);
        tag.putFloat("resistanceChance", resistanceChance);
        tag.putInt("resistanceDuration", resistanceDuration);
        tag.putFloat("q1DamageMultiplier", q1DamageMultiplier);
        tag.putFloat("q1MovingTargetDamageMultiplier", q1MovingTargetDamageMultiplier);
        tag.putInt("q1SlowDuration", q1SlowDuration);
        tag.putInt("q1Cooldown", q1Cooldown);
        tag.putFloat("q1Angle", q1Angle);
        tag.putFloat("q2MaxDistance", q2MaxDistance);
        tag.putFloat("q2DamageMultiplier", q2DamageMultiplier);
        tag.putFloat("q2KnockbackDuration", q2KnockbackDuration);
        tag.putFloat("q2SplashDamagePercent", q2SplashDamagePercent);
        tag.putFloat("q2BonusAttackRange", q2BonusAttackRange);
        tag.putInt("q2BonusAttackDuration", q2BonusAttackDuration);
        tag.putInt("q2Cooldown", q2Cooldown);
        tag.putFloat("q3Radius", q3Radius);
        tag.putFloat("q3DamageMultiplier", q3DamageMultiplier);
        tag.putFloat("q3FearDuration", q3FearDuration);
        tag.putFloat("q3BerserkFearDuration", q3BerserkFearDuration);
        tag.putInt("q3WeaknessDuration", q3WeaknessDuration);
        tag.putInt("q3Cooldown", q3Cooldown);
        tag.putInt("rDuration", rDuration);
        tag.putFloat("rHealPercent", rHealPercent);
        tag.putFloat("rHealthBonusPercent", rHealthBonusPercent);
        tag.putInt("rPowerLevel", rPowerLevel);
        tag.putInt("rSpeedLevel", rSpeedLevel);
        tag.putInt("rFatigueDuration", rFatigueDuration);
        tag.putInt("rCooldown", rCooldown);
        tag.putBoolean("isByteChen", isByteChen);
        tag.putInt("byteChenComputeMax", byteChenComputeMax);
        tag.putInt("byteChenComputeInitial", byteChenComputeInitial);
        tag.putInt("byteChenComputeRegenRate", byteChenComputeRegenRate);
        tag.putFloat("byteChenComputeNodeRegenBonus", byteChenComputeNodeRegenBonus);
        tag.putInt("byteChenComputeOnRead", byteChenComputeOnRead);
        tag.putInt("byteChenComputeOnNodeTrigger", byteChenComputeOnNodeTrigger);
        tag.putInt("byteChenComputeOnInterrupt", byteChenComputeOnInterrupt);
        tag.putInt("byteChenComputeLowThreshold", byteChenComputeLowThreshold);
        tag.putInt("byteChenNodeMax", byteChenNodeMax);
        tag.putInt("byteChenNodeRange", byteChenNodeRange);
        tag.putInt("byteChenNodeDuration", byteChenNodeDuration);
        tag.putInt("byteChenScoutNodeCost", byteChenScoutNodeCost);
        tag.putFloat("byteChenScoutNodeRadius", byteChenScoutNodeRadius);
        tag.putInt("byteChenScoutNodeComputeRegen", byteChenScoutNodeComputeRegen);
        tag.putInt("byteChenBuffNodeCost", byteChenBuffNodeCost);
        tag.putFloat("byteChenBuffNodeRadius", byteChenBuffNodeRadius);
        tag.putFloat("byteChenBuffNodeDamageBonus", byteChenBuffNodeDamageBonus);
        tag.putFloat("byteChenBuffNodeDamageReduction", byteChenBuffNodeDamageReduction);
        tag.putFloat("byteChenBuffNodeSpeedBonus", byteChenBuffNodeSpeedBonus);
        tag.putFloat("byteChenBuffNodeCooldownReduction", byteChenBuffNodeCooldownReduction);
        tag.putInt("byteChenBuffNodeMaxStacks", byteChenBuffNodeMaxStacks);
        tag.putInt("byteChenCounterNodeCost", byteChenCounterNodeCost);
        tag.putFloat("byteChenCounterNodeRadius", byteChenCounterNodeRadius);
        tag.putInt("byteChenCounterNodeSilenceDuration", byteChenCounterNodeSilenceDuration);
        tag.putInt("byteChenCounterNodeDisorderDuration", byteChenCounterNodeDisorderDuration);
        tag.putFloat("byteChenCounterNodeDamageReduction", byteChenCounterNodeDamageReduction);
        tag.putFloat("byteChenCounterNodeCooldownMultiplier", byteChenCounterNodeCooldownMultiplier);
        tag.putFloat("byteChenDataVisionRange", byteChenDataVisionRange);
        tag.putFloat("byteChenLightweightSpeedBonus", byteChenLightweightSpeedBonus);
        tag.putFloat("byteChenLightweightDigSpeedBonus", byteChenLightweightDigSpeedBonus);
        tag.putFloat("byteChenLightweightMeleePenalty", byteChenLightweightMeleePenalty);
        tag.putFloat("byteChenLightweightReceivedMeleePenalty", byteChenLightweightReceivedMeleePenalty);
        tag.putInt("byteChenNodeRecycleCooldown", byteChenNodeRecycleCooldown);
        tag.putFloat("byteChenNodeRecycleRefund", byteChenNodeRecycleRefund);
        tag.putInt("byteChenFullReadCost", byteChenFullReadCost);
        tag.putInt("byteChenFullReadCooldown", byteChenFullReadCooldown);
        tag.putFloat("byteChenFullReadRadius", byteChenFullReadRadius);
        tag.putInt("byteChenFullReadDuration", byteChenFullReadDuration);
        tag.putInt("byteChenDataDispatchCost", byteChenDataDispatchCost);
        tag.putInt("byteChenDataDispatchCooldown", byteChenDataDispatchCooldown);
        tag.putFloat("byteChenDataDispatchSpeedBonus", byteChenDataDispatchSpeedBonus);
        tag.putFloat("byteChenDataDispatchCooldownBonus", byteChenDataDispatchCooldownBonus);
        tag.putInt("byteChenDataDispatchBuffDuration", byteChenDataDispatchBuffDuration);
        tag.putInt("byteChenDataBanCost", byteChenDataBanCost);
        tag.putInt("byteChenDataBanCooldown", byteChenDataBanCooldown);
        tag.putFloat("byteChenDataBanRange", byteChenDataBanRange);
        tag.putInt("byteChenDataBanDuration", byteChenDataBanDuration);
        tag.putInt("byteChenDataBanBossDuration", byteChenDataBanBossDuration);
        tag.putFloat("byteChenDataBanTrueDamageBonus", byteChenDataBanTrueDamageBonus);
        tag.putInt("byteChenUltimateMinCost", byteChenUltimateMinCost);
        tag.putInt("byteChenUltimateCooldown", byteChenUltimateCooldown);
        tag.putFloat("byteChenUltimateRadius", byteChenUltimateRadius);
        tag.putInt("byteChenUltimateDuration", byteChenUltimateDuration);
        tag.putFloat("byteChenUltimateDamageBonus", byteChenUltimateDamageBonus);
        tag.putFloat("byteChenUltimateDamageReduction", byteChenUltimateDamageReduction);
        tag.putFloat("byteChenUltimateSpeedBonus", byteChenUltimateSpeedBonus);
        tag.putFloat("byteChenUltimateCooldownBonus", byteChenUltimateCooldownBonus);
        tag.putFloat("byteChenUltimateEnemyDamageReduction", byteChenUltimateEnemyDamageReduction);
        tag.putFloat("byteChenUltimateEnemySpeedReduction", byteChenUltimateEnemySpeedReduction);
        tag.putFloat("byteChenUltimateEnemyCooldownMultiplier", byteChenUltimateEnemyCooldownMultiplier);
        tag.putFloat("byteChenUltimateInterruptChance", byteChenUltimateInterruptChance);
        tag.putInt("byteChenExhaustDuration", byteChenExhaustDuration);
        tag.putBoolean("isHeavyKnight", isHeavyKnight);
        tag.putFloat("heavyKnightSpeedPenalty", heavyKnightSpeedPenalty);
        tag.putInt("heavyKnightWillOnHit", heavyKnightWillOnHit);
        tag.putInt("heavyKnightWillOnBlock", heavyKnightWillOnBlock);
        tag.putInt("heavyKnightWillOnDamaged", heavyKnightWillOnDamaged);
        tag.putFloat("heavyKnightFullWillDamageBonus", heavyKnightFullWillDamageBonus);
        tag.putFloat("heavyKnightFullWillDamageReduction", heavyKnightFullWillDamageReduction);
        tag.putInt("heavyKnightShieldWallTriggerTicks", heavyKnightShieldWallTriggerTicks);
        tag.putFloat("heavyKnightShieldWallReduction", heavyKnightShieldWallReduction);
        tag.putFloat("heavyKnightShieldWallRadius", heavyKnightShieldWallRadius);
        tag.putFloat("heavyKnightShieldWallArmorBonus", heavyKnightShieldWallArmorBonus);
        tag.putInt("heavyKnightChargeWillCost", heavyKnightChargeWillCost);
        tag.putInt("heavyKnightChargeCooldown", heavyKnightChargeCooldown);
        tag.putFloat("heavyKnightChargeDistance", heavyKnightChargeDistance);
        tag.putFloat("heavyKnightChargeDamage", heavyKnightChargeDamage);
        tag.putFloat("heavyKnightChargeKnockback", heavyKnightChargeKnockback);
        tag.putInt("heavyKnightProtectRange", heavyKnightProtectRange);
        tag.putInt("heavyKnightProtectWillCost", heavyKnightProtectWillCost);
        tag.putInt("heavyKnightProtectCooldown", heavyKnightProtectCooldown);
        tag.putFloat("heavyKnightShieldBashDamage", heavyKnightShieldBashDamage);
        tag.putInt("heavyKnightShieldBashStunDuration", heavyKnightShieldBashStunDuration);
        tag.putInt("heavyKnightShieldBashCooldown", heavyKnightShieldBashCooldown);
        return tag;
    }

    private void saveStringList(CompoundTag tag, String key, List<String> list) {
        ListTag listTag = new ListTag();
        for (String s : list) {
            listTag.add(StringTag.valueOf(s));
        }
        tag.put(key, listTag);
    }

    public static Profession load(CompoundTag tag) {
        return ProfessionBuilder.fromNBT(tag).build();
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
        buffer.writeBoolean(ironArmorOnly);
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
        buffer.writeBoolean(isGhostSenator);
        buffer.writeFloat(ghostSenatorHealAmount);
        buffer.writeInt(ghostSenatorStrengthDuration);
        buffer.writeFloat(ghostSenatorDetectionRadius);
        buffer.writeBoolean(isWraithCouncilor);
        buffer.writeInt(wraithSoulMax);
        buffer.writeInt(wraithSoulInitial);
        buffer.writeInt(wraithSoulRegenRate);
        buffer.writeInt(wraithSoulDarkBonus);
        buffer.writeFloat(wraithSoulSunlightPenalty);
        buffer.writeInt(wraithSoulKillBonus);
        buffer.writeInt(wraithSoulHitBonus);
        buffer.writeInt(wraithSoulSummonKillBonus);
        buffer.writeFloat(wraithErosionDamage);
        buffer.writeFloat(wraithErosionArmorReduction);
        buffer.writeFloat(wraithErosionSlowPercent);
        buffer.writeInt(wraithErosionMaxStacks);
        buffer.writeInt(wraithErosionDuration);
        buffer.writeInt(wraithSummonCost);
        buffer.writeInt(wraithSummonCooldown);
        buffer.writeInt(wraithSummonCount);
        buffer.writeFloat(wraithSummonHealth);
        buffer.writeFloat(wraithSummonDamage);
        buffer.writeInt(wraithSummonDuration);
        buffer.writeFloat(wraithSummonCorpseRange);
        buffer.writeInt(wraithSummonExtraMax);
        buffer.writeInt(wraithDomainCost);
        buffer.writeInt(wraithDomainCooldown);
        buffer.writeFloat(wraithDomainRadius);
        buffer.writeInt(wraithDomainDuration);
        buffer.writeFloat(wraithDomainDamage);
        buffer.writeFloat(wraithDomainBossSlow);
        buffer.writeInt(wraithDomainCharmDuration);
        buffer.writeInt(wraithBarrageBaseCost);
        buffer.writeInt(wraithBarrageChargedCost);
        buffer.writeInt(wraithBarrageCooldown);
        buffer.writeInt(wraithBarrageBaseOrbs);
        buffer.writeInt(wraithBarrageChargedOrbs);
        buffer.writeFloat(wraithBarrageBaseDamage);
        buffer.writeFloat(wraithBarrageChargedDamage);
        buffer.writeFloat(wraithBarrageBaseRange);
        buffer.writeFloat(wraithBarrageChargedRange);
        buffer.writeInt(wraithBarrageMaxChargeTime);
        buffer.writeInt(wraithBarrageMaxHits);
        buffer.writeInt(wraithUltimateMinCost);
        buffer.writeInt(wraithUltimateCooldown);
        buffer.writeInt(wraithUltimateCloneCount);
        buffer.writeFloat(wraithUltimateCloneDamageRatio);
        buffer.writeInt(wraithUltimateDuration);
        buffer.writeInt(wraithUltimateSoulRegen);
        buffer.writeFloat(wraithUltimateDamageIncrease);
        buffer.writeInt(wraithUltimateExhaustDuration);
        buffer.writeBoolean(isEvilPoisoner);
        buffer.writeInt(poisonerStrengthDuration);
        buffer.writeBoolean(hasTurtleAura);
        buffer.writeFloat(turtleAuraRadius);
        buffer.writeInt(turtleAuraSlownessLevel);
        buffer.writeInt(turtleAuraDuration);
        buffer.writeBoolean(isJungleApeGod);
        buffer.writeInt(rhythmStacksMax);
        buffer.writeFloat(rhythmAttackSpeedPerStack);
        buffer.writeFloat(rhythmMoveSpeedPerStack);
        buffer.writeInt(berserkDuration);
        buffer.writeFloat(berserkCooldownReduction);
        buffer.writeFloat(berserkLifeSteal);
        buffer.writeFloat(flatDamageReduction);
        buffer.writeFloat(resistanceChance);
        buffer.writeInt(resistanceDuration);
        buffer.writeFloat(q1DamageMultiplier);
        buffer.writeFloat(q1MovingTargetDamageMultiplier);
        buffer.writeInt(q1SlowDuration);
        buffer.writeInt(q1Cooldown);
        buffer.writeFloat(q1Angle);
        buffer.writeFloat(q2MaxDistance);
        buffer.writeFloat(q2DamageMultiplier);
        buffer.writeFloat(q2KnockbackDuration);
        buffer.writeFloat(q2SplashDamagePercent);
        buffer.writeFloat(q2BonusAttackRange);
        buffer.writeInt(q2BonusAttackDuration);
        buffer.writeInt(q2Cooldown);
        buffer.writeFloat(q3Radius);
        buffer.writeFloat(q3DamageMultiplier);
        buffer.writeFloat(q3FearDuration);
        buffer.writeFloat(q3BerserkFearDuration);
        buffer.writeInt(q3WeaknessDuration);
        buffer.writeInt(q3Cooldown);
        buffer.writeInt(rDuration);
        buffer.writeFloat(rHealPercent);
        buffer.writeFloat(rHealthBonusPercent);
        buffer.writeInt(rPowerLevel);
        buffer.writeInt(rSpeedLevel);
        buffer.writeInt(rFatigueDuration);
        buffer.writeInt(rCooldown);
        buffer.writeBoolean(isByteChen);
        buffer.writeInt(byteChenComputeMax);
        buffer.writeInt(byteChenComputeInitial);
        buffer.writeInt(byteChenComputeRegenRate);
        buffer.writeFloat(byteChenComputeNodeRegenBonus);
        buffer.writeInt(byteChenComputeOnRead);
        buffer.writeInt(byteChenComputeOnNodeTrigger);
        buffer.writeInt(byteChenComputeOnInterrupt);
        buffer.writeInt(byteChenComputeLowThreshold);
        buffer.writeInt(byteChenNodeMax);
        buffer.writeInt(byteChenNodeRange);
        buffer.writeInt(byteChenNodeDuration);
        buffer.writeInt(byteChenScoutNodeCost);
        buffer.writeFloat(byteChenScoutNodeRadius);
        buffer.writeInt(byteChenScoutNodeComputeRegen);
        buffer.writeInt(byteChenBuffNodeCost);
        buffer.writeFloat(byteChenBuffNodeRadius);
        buffer.writeFloat(byteChenBuffNodeDamageBonus);
        buffer.writeFloat(byteChenBuffNodeDamageReduction);
        buffer.writeFloat(byteChenBuffNodeSpeedBonus);
        buffer.writeFloat(byteChenBuffNodeCooldownReduction);
        buffer.writeInt(byteChenBuffNodeMaxStacks);
        buffer.writeInt(byteChenCounterNodeCost);
        buffer.writeFloat(byteChenCounterNodeRadius);
        buffer.writeInt(byteChenCounterNodeSilenceDuration);
        buffer.writeInt(byteChenCounterNodeDisorderDuration);
        buffer.writeFloat(byteChenCounterNodeDamageReduction);
        buffer.writeFloat(byteChenCounterNodeCooldownMultiplier);
        buffer.writeFloat(byteChenDataVisionRange);
        buffer.writeFloat(byteChenLightweightSpeedBonus);
        buffer.writeFloat(byteChenLightweightDigSpeedBonus);
        buffer.writeFloat(byteChenLightweightMeleePenalty);
        buffer.writeFloat(byteChenLightweightReceivedMeleePenalty);
        buffer.writeInt(byteChenNodeRecycleCooldown);
        buffer.writeFloat(byteChenNodeRecycleRefund);
        buffer.writeInt(byteChenFullReadCost);
        buffer.writeInt(byteChenFullReadCooldown);
        buffer.writeFloat(byteChenFullReadRadius);
        buffer.writeInt(byteChenFullReadDuration);
        buffer.writeInt(byteChenDataDispatchCost);
        buffer.writeInt(byteChenDataDispatchCooldown);
        buffer.writeFloat(byteChenDataDispatchSpeedBonus);
        buffer.writeFloat(byteChenDataDispatchCooldownBonus);
        buffer.writeInt(byteChenDataDispatchBuffDuration);
        buffer.writeInt(byteChenDataBanCost);
        buffer.writeInt(byteChenDataBanCooldown);
        buffer.writeFloat(byteChenDataBanRange);
        buffer.writeInt(byteChenDataBanDuration);
        buffer.writeInt(byteChenDataBanBossDuration);
        buffer.writeFloat(byteChenDataBanTrueDamageBonus);
        buffer.writeInt(byteChenUltimateMinCost);
        buffer.writeInt(byteChenUltimateCooldown);
        buffer.writeFloat(byteChenUltimateRadius);
        buffer.writeInt(byteChenUltimateDuration);
        buffer.writeFloat(byteChenUltimateDamageBonus);
        buffer.writeFloat(byteChenUltimateDamageReduction);
        buffer.writeFloat(byteChenUltimateSpeedBonus);
        buffer.writeFloat(byteChenUltimateCooldownBonus);
        buffer.writeFloat(byteChenUltimateEnemyDamageReduction);
        buffer.writeFloat(byteChenUltimateEnemySpeedReduction);
        buffer.writeFloat(byteChenUltimateEnemyCooldownMultiplier);
        buffer.writeFloat(byteChenUltimateInterruptChance);
        buffer.writeInt(byteChenExhaustDuration);
        buffer.writeBoolean(isHeavyKnight);
        buffer.writeFloat(heavyKnightSpeedPenalty);
        buffer.writeInt(heavyKnightWillOnHit);
        buffer.writeInt(heavyKnightWillOnBlock);
        buffer.writeInt(heavyKnightWillOnDamaged);
        buffer.writeFloat(heavyKnightFullWillDamageBonus);
        buffer.writeFloat(heavyKnightFullWillDamageReduction);
        buffer.writeInt(heavyKnightShieldWallTriggerTicks);
        buffer.writeFloat(heavyKnightShieldWallReduction);
        buffer.writeFloat(heavyKnightShieldWallRadius);
        buffer.writeFloat(heavyKnightShieldWallArmorBonus);
        buffer.writeInt(heavyKnightChargeWillCost);
        buffer.writeInt(heavyKnightChargeCooldown);
        buffer.writeFloat(heavyKnightChargeDistance);
        buffer.writeFloat(heavyKnightChargeDamage);
        buffer.writeFloat(heavyKnightChargeKnockback);
        buffer.writeInt(heavyKnightProtectRange);
        buffer.writeInt(heavyKnightProtectWillCost);
        buffer.writeInt(heavyKnightProtectCooldown);
        buffer.writeFloat(heavyKnightShieldBashDamage);
        buffer.writeInt(heavyKnightShieldBashStunDuration);
        buffer.writeInt(heavyKnightShieldBashCooldown);
    }

    public static Profession readFromBuffer(FriendlyByteBuf buffer) {
        return ProfessionBuilder.fromBuffer(buffer).build();
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
