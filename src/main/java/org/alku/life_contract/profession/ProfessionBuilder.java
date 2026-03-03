package org.alku.life_contract.profession;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;

import java.util.ArrayList;
import java.util.List;

public class ProfessionBuilder {
    private String id = "unknown";
    private String name = "Unknown";
    private String description = "";
    private boolean requiresPassword = false;
    private String password = "";
    private String iconItem = "minecraft:paper";
    
    private float bonusDamagePercent = 0.0f;
    private int slownessLevel = 0;
    private int weaknessLevel = 0;
    private float bonusArmor = 0.0f;
    
    private float poisonChance = 0.0f;
    private int poisonDuration = 0;
    private int poisonDamage = 0;
    private float fireDamageMultiplier = 1.0f;
    
    private String resourceItem = "";
    private int resourceInterval = 0;
    private int resourceAmount = 0;
    
    private boolean hasEnderPearlAbility = false;
    private int enderPearlCooldown = 0;
    
    private boolean waterDamage = false;
    private float waterDamageAmount = 0.0f;
    private int waterDamageInterval = 0;
    
    private boolean fireTrailEnabled = false;
    private float fireTrailDamage = 0.0f;
    private int fireTrailDuration = 0;
    private float fireTrailRadius = 0.0f;
    private float fireDamageBonusPercent = 0.0f;
    private boolean fireImmunity = false;
    
    private boolean waterWeakness = false;
    private float waterWeaknessDamagePercent = 0.0f;
    private int waterWeaknessInterval = 0;
    private boolean rainWeakness = false;
    
    private boolean isFaceless = false;
    private int switchInterval = 0;
    
    private boolean canMountCreatures = false;
    private float mountSpeedBonus = 0.0f;
    private float mountDamageBonus = 0.0f;
    private int mountControlRange = 32;
    private float mountHealthBonus = 0.0f;
    
    private boolean hasGachaAbility = false;
    private int gachaInterval = 300;
    private List<String> gachaEntityPool = new ArrayList<>();
    
    private boolean hasDiceAbility = false;
    private int diceCooldown = 60;
    private List<String> diceSkillPool = new ArrayList<>();
    
    private boolean hasLuckyCloverAbility = false;
    private boolean hasDonkBowAbility = false;
    
    private boolean hasGourmetAbility = false;
    private boolean isGourmet = false;
    private float gourmetHealthBonus = 4.0f;
    private float gourmetDamageBonus = 0.5f;
    private int gourmetDailyLimit = 0;
    
    private boolean hasForgetterAbility = false;
    private int forgetterInterval = 60;
    private int forgetterMinDuration = 10;
    private int forgetterMaxDuration = 40;
    
    private boolean isFacelessDeceiver = false;
    
    private boolean isAngel = false;
    private int healthRegenInterval = 100;
    private float healthRegenAmount = 1.0f;
    private boolean hasHalo = false;
    private float haloDetectionRange = 32.0f;
    
    private boolean leatherArmorOnly = false;
    private boolean ironArmorOnly = false;
    private float bonusHealth = 0.0f;
    private float bonusArmorToughness = 0.0f;
    private float meleeDamageBonus = 0.0f;
    private float rangedDamageReduction = 0.0f;
    private float rangedDamagePenalty = 0.0f;
    
    private boolean isDeathVenger = false;
    private boolean hideNameTag = false;
    private boolean hasMarkTargetAbility = false;
    
    private boolean isImpostor = false;
    private int impostorSkillCooldown = 180;
    private int impostorDisguiseDuration = 120;
    
    private boolean hasAmbushAbility = false;
    private int ambushMaxTargets = 5;
    private int ambushMaxDistance = 50;
    private int ambushCooldown = 60;
    private int ambushInvisDuration = 10;
    private int passiveInvisSeconds = 10;
    
    private boolean isUndead = false;
    private int lifePoints = 0;
    private int shieldPoints = 0;
    private boolean sunlightVulnerability = false;
    private float sunlightDamageAmount = 1.0f;
    private int sunlightDamageInterval = 40;
    
    private float bonusAttackReach = 0.0f;
    
    private boolean hasEndlessMinerAbility = false;
    private int fortuneLevel = 2;
    private float stoneDropChance = 0.05f;
    
    private boolean isHealer = false;
    private float healerPassiveRadius = 5.0f;
    private float healerPassiveHealAmount = 2.0f;
    private float healerActiveHealAmount = 8.0f;
    private int healerActiveCooldown = 400;
    
    private boolean isFool = false;
    private float foolStealRange = 30.0f;
    private int foolStealCooldown = 600;
    
    private boolean isHighPriest = false;
    private float highPriestSacrificeRange = 30.0f;
    private int highPriestCooldown = 600;
    private float highPriestReviveHealth = 10.0f;
    
    private boolean isGhostSenator = false;
    private float ghostSenatorHealAmount = 7.0f;
    private int ghostSenatorStrengthDuration = 300;
    private float ghostSenatorDetectionRadius = 20.0f;
    private boolean isWraithCouncilor = false;
    private int wraithSoulMax = 120;
    private int wraithSoulInitial = 60;
    private int wraithSoulRegenRate = 2;
    private int wraithSoulDarkBonus = 1;
    private float wraithSoulSunlightPenalty = 0.5f;
    private int wraithSoulKillBonus = 15;
    private int wraithSoulHitBonus = 3;
    private int wraithSoulSummonKillBonus = 8;
    private float wraithErosionDamage = 2.0f;
    private float wraithErosionArmorReduction = 0.1f;
    private float wraithErosionSlowPercent = 0.15f;
    private int wraithErosionMaxStacks = 5;
    private int wraithErosionDuration = 100;
    private int wraithSummonCost = 35;
    private int wraithSummonCooldown = 360;
    private int wraithSummonCount = 2;
    private float wraithSummonHealth = 20.0f;
    private float wraithSummonDamage = 6.0f;
    private int wraithSummonDuration = 240;
    private float wraithSummonCorpseRange = 5.0f;
    private int wraithSummonExtraMax = 2;
    private int wraithDomainCost = 40;
    private int wraithDomainCooldown = 440;
    private float wraithDomainRadius = 3.0f;
    private int wraithDomainDuration = 80;
    private float wraithDomainDamage = 6.0f;
    private float wraithDomainBossSlow = 0.5f;
    private int wraithDomainCharmDuration = 80;
    private int wraithBarrageBaseCost = 25;
    private int wraithBarrageChargedCost = 10;
    private int wraithBarrageCooldown = 200;
    private int wraithBarrageBaseOrbs = 6;
    private int wraithBarrageChargedOrbs = 12;
    private float wraithBarrageBaseDamage = 4.0f;
    private float wraithBarrageChargedDamage = 6.0f;
    private float wraithBarrageBaseRange = 8.0f;
    private float wraithBarrageChargedRange = 12.0f;
    private int wraithBarrageMaxChargeTime = 30;
    private int wraithBarrageMaxHits = 3;
    private int wraithUltimateMinCost = 100;
    private int wraithUltimateCooldown = 2400;
    private int wraithUltimateCloneCount = 3;
    private float wraithUltimateCloneDamageRatio = 0.8f;
    private int wraithUltimateDuration = 300;
    private int wraithUltimateSoulRegen = 10;
    private float wraithUltimateDamageIncrease = 0.2f;
    private int wraithUltimateExhaustDuration = 100;
    
    private boolean isEvilPoisoner = false;
    private int poisonerStrengthDuration = 200;
    private boolean hasTurtleAura = false;
    private float turtleAuraRadius = 5.0f;
    private int turtleAuraSlownessLevel = 1;
    private int turtleAuraDuration = 100;
    private boolean isJungleApeGod = false;
    private int rhythmStacksMax = 10;
    private float rhythmAttackSpeedPerStack = 0.03f;
    private float rhythmMoveSpeedPerStack = 0.02f;
    private int berserkDuration = 100;
    private float berserkCooldownReduction = 0.3f;
    private float berserkLifeSteal = 0.2f;
    private float flatDamageReduction = 5.0f;
    private float resistanceChance = 0.2f;
    private int resistanceDuration = 20;
    private float q1DamageMultiplier = 2.0f;
    private float q1MovingTargetDamageMultiplier = 2.8f;
    private int q1SlowDuration = 20;
    private int q1Cooldown = 200;
    private float q1Angle = 90.0f;
    private float q2MaxDistance = 25.0f;
    private float q2DamageMultiplier = 1.5f;
    private float q2KnockbackDuration = 10.0f;
    private float q2SplashDamagePercent = 0.5f;
    private float q2BonusAttackRange = 2.0f;
    private int q2BonusAttackDuration = 60;
    private int q2Cooldown = 200;
    private float q3Radius = 6.0f;
    private float q3DamageMultiplier = 1.0f;
    private float q3FearDuration = 1.5f;
    private float q3BerserkFearDuration = 2.5f;
    private int q3WeaknessDuration = 60;
    private int q3Cooldown = 400;
    private int rDuration = 200;
    private float rHealPercent = 0.3f;
    private float rHealthBonusPercent = 0.5f;
    private int rPowerLevel = 4;
    private int rSpeedLevel = 3;
    private int rFatigueDuration = 40;
    private int rCooldown = 1200;

    private boolean isByteChen = false;
    private int byteChenComputeMax = 150;
    private int byteChenComputeInitial = 150;
    private int byteChenComputeRegenRate = 3;
    private float byteChenComputeNodeRegenBonus = 0.5f;
    private int byteChenComputeOnRead = 10;
    private int byteChenComputeOnNodeTrigger = 8;
    private int byteChenComputeOnInterrupt = 25;
    private int byteChenComputeLowThreshold = 20;
    private int byteChenNodeMax = 8;
    private int byteChenNodeRange = 32;
    private int byteChenNodeDuration = 1200;
    private int byteChenScoutNodeCost = 20;
    private float byteChenScoutNodeRadius = 12.0f;
    private int byteChenScoutNodeComputeRegen = 1;
    private int byteChenBuffNodeCost = 25;
    private float byteChenBuffNodeRadius = 8.0f;
    private float byteChenBuffNodeDamageBonus = 0.1f;
    private float byteChenBuffNodeDamageReduction = 0.1f;
    private float byteChenBuffNodeSpeedBonus = 0.15f;
    private float byteChenBuffNodeCooldownReduction = 0.1f;
    private int byteChenBuffNodeMaxStacks = 3;
    private int byteChenCounterNodeCost = 30;
    private float byteChenCounterNodeRadius = 6.0f;
    private int byteChenCounterNodeSilenceDuration = 40;
    private int byteChenCounterNodeDisorderDuration = 100;
    private float byteChenCounterNodeDamageReduction = 0.15f;
    private float byteChenCounterNodeCooldownMultiplier = 2.0f;
    private float byteChenDataVisionRange = 16.0f;
    private float byteChenLightweightSpeedBonus = 0.15f;
    private float byteChenLightweightDigSpeedBonus = 0.2f;
    private float byteChenLightweightMeleePenalty = -0.45f;
    private float byteChenLightweightReceivedMeleePenalty = 0.2f;
    private int byteChenNodeRecycleCooldown = 200;
    private float byteChenNodeRecycleRefund = 0.8f;
    private int byteChenFullReadCost = 30;
    private int byteChenFullReadCooldown = 300;
    private float byteChenFullReadRadius = 32.0f;
    private int byteChenFullReadDuration = 160;
    private int byteChenDataDispatchCost = 40;
    private int byteChenDataDispatchCooldown = 400;
    private float byteChenDataDispatchSpeedBonus = 0.2f;
    private float byteChenDataDispatchCooldownBonus = 0.2f;
    private int byteChenDataDispatchBuffDuration = 160;
    private int byteChenDataBanCost = 35;
    private int byteChenDataBanCooldown = 360;
    private float byteChenDataBanRange = 12.0f;
    private int byteChenDataBanDuration = 80;
    private int byteChenDataBanBossDuration = 40;
    private float byteChenDataBanTrueDamageBonus = 0.1f;
    private int byteChenUltimateMinCost = 120;
    private int byteChenUltimateCooldown = 2400;
    private float byteChenUltimateRadius = 64.0f;
    private int byteChenUltimateDuration = 240;
    private float byteChenUltimateDamageBonus = 0.25f;
    private float byteChenUltimateDamageReduction = 0.25f;
    private float byteChenUltimateSpeedBonus = 0.3f;
    private float byteChenUltimateCooldownBonus = 0.3f;
    private float byteChenUltimateEnemyDamageReduction = 0.3f;
    private float byteChenUltimateEnemySpeedReduction = 0.25f;
    private float byteChenUltimateEnemyCooldownMultiplier = 2.0f;
    private float byteChenUltimateInterruptChance = 0.5f;
    private int byteChenExhaustDuration = 160;

    private boolean isHeavyKnight = false;
    private float heavyKnightSpeedPenalty = 0.2f;
    private int heavyKnightWillOnHit = 5;
    private int heavyKnightWillOnBlock = 6;
    private int heavyKnightWillOnDamaged = 8;
    private float heavyKnightFullWillDamageBonus = 0.1f;
    private float heavyKnightFullWillDamageReduction = 0.1f;
    private int heavyKnightShieldWallTriggerTicks = 40;
    private float heavyKnightShieldWallReduction = 0.2f;
    private float heavyKnightShieldWallRadius = 8.0f;
    private float heavyKnightShieldWallArmorBonus = 5.0f;
    private int heavyKnightChargeWillCost = 30;
    private int heavyKnightChargeCooldown = 240;
    private float heavyKnightChargeDistance = 5.0f;
    private float heavyKnightChargeDamage = 12.0f;
    private float heavyKnightChargeKnockback = 3.0f;
    private int heavyKnightProtectRange = 6;
    private int heavyKnightProtectWillCost = 20;
    private int heavyKnightProtectCooldown = 400;
    private float heavyKnightShieldBashDamage = 4.0f;
    private int heavyKnightShieldBashStunDuration = 30;
    private int heavyKnightShieldBashCooldown = 80;

    private boolean isApostle = false;
    private float apostleMeleeDamagePercent = 0.1f;
    private float apostleNetherDamageReduction = 0.5f;
    private int apostleTeleportCooldown = 200;
    private float apostleTeleportDistance = 8.0f;
    private int apostleFireballCooldown = 300;
    private float apostleFireballDamage = 6.0f;
    private int apostleDebuffDuration = 100;
    private float apostleDebuffDamageIncrease = 0.2f;
    private float apostleHealingReduction = 0.5f;
    private int apostleArrowFireRadius = 3;
    private float apostleArrowFireDuration = 5.0f;

    public ProfessionBuilder() {}

    public ProfessionBuilder id(String id) { this.id = id != null ? id : "unknown"; return this; }
    public ProfessionBuilder name(String name) { this.name = name != null ? name : "Unknown"; return this; }
    public ProfessionBuilder description(String description) { this.description = description != null ? description : ""; return this; }
    public ProfessionBuilder requiresPassword(boolean requiresPassword) { this.requiresPassword = requiresPassword; return this; }
    public ProfessionBuilder password(String password) { this.password = password != null ? password : ""; return this; }
    public ProfessionBuilder iconItem(String iconItem) { this.iconItem = iconItem != null ? iconItem : "minecraft:paper"; return this; }
    
    public ProfessionBuilder bonusDamagePercent(float bonusDamagePercent) { this.bonusDamagePercent = bonusDamagePercent; return this; }
    public ProfessionBuilder slownessLevel(int slownessLevel) { this.slownessLevel = slownessLevel; return this; }
    public ProfessionBuilder weaknessLevel(int weaknessLevel) { this.weaknessLevel = weaknessLevel; return this; }
    public ProfessionBuilder bonusArmor(float bonusArmor) { this.bonusArmor = bonusArmor; return this; }
    
    public ProfessionBuilder poisonChance(float poisonChance) { this.poisonChance = poisonChance; return this; }
    public ProfessionBuilder poisonDuration(int poisonDuration) { this.poisonDuration = poisonDuration; return this; }
    public ProfessionBuilder poisonDamage(int poisonDamage) { this.poisonDamage = poisonDamage; return this; }
    public ProfessionBuilder fireDamageMultiplier(float fireDamageMultiplier) { this.fireDamageMultiplier = fireDamageMultiplier; return this; }
    
    public ProfessionBuilder resourceItem(String resourceItem) { this.resourceItem = resourceItem != null ? resourceItem : ""; return this; }
    public ProfessionBuilder resourceInterval(int resourceInterval) { this.resourceInterval = resourceInterval; return this; }
    public ProfessionBuilder resourceAmount(int resourceAmount) { this.resourceAmount = resourceAmount; return this; }
    
    public ProfessionBuilder hasEnderPearlAbility(boolean hasEnderPearlAbility) { this.hasEnderPearlAbility = hasEnderPearlAbility; return this; }
    public ProfessionBuilder enderPearlCooldown(int enderPearlCooldown) { this.enderPearlCooldown = enderPearlCooldown; return this; }
    
    public ProfessionBuilder waterDamage(boolean waterDamage) { this.waterDamage = waterDamage; return this; }
    public ProfessionBuilder waterDamageAmount(float waterDamageAmount) { this.waterDamageAmount = waterDamageAmount; return this; }
    public ProfessionBuilder waterDamageInterval(int waterDamageInterval) { this.waterDamageInterval = waterDamageInterval; return this; }
    
    public ProfessionBuilder fireTrailEnabled(boolean fireTrailEnabled) { this.fireTrailEnabled = fireTrailEnabled; return this; }
    public ProfessionBuilder fireTrailDamage(float fireTrailDamage) { this.fireTrailDamage = fireTrailDamage; return this; }
    public ProfessionBuilder fireTrailDuration(int fireTrailDuration) { this.fireTrailDuration = fireTrailDuration; return this; }
    public ProfessionBuilder fireTrailRadius(float fireTrailRadius) { this.fireTrailRadius = fireTrailRadius; return this; }
    public ProfessionBuilder fireDamageBonusPercent(float fireDamageBonusPercent) { this.fireDamageBonusPercent = fireDamageBonusPercent; return this; }
    public ProfessionBuilder fireImmunity(boolean fireImmunity) { this.fireImmunity = fireImmunity; return this; }
    
    public ProfessionBuilder waterWeakness(boolean waterWeakness) { this.waterWeakness = waterWeakness; return this; }
    public ProfessionBuilder waterWeaknessDamagePercent(float waterWeaknessDamagePercent) { this.waterWeaknessDamagePercent = waterWeaknessDamagePercent; return this; }
    public ProfessionBuilder waterWeaknessInterval(int waterWeaknessInterval) { this.waterWeaknessInterval = waterWeaknessInterval; return this; }
    public ProfessionBuilder rainWeakness(boolean rainWeakness) { this.rainWeakness = rainWeakness; return this; }
    
    public ProfessionBuilder isFaceless(boolean isFaceless) { this.isFaceless = isFaceless; return this; }
    public ProfessionBuilder switchInterval(int switchInterval) { this.switchInterval = switchInterval; return this; }
    
    public ProfessionBuilder canMountCreatures(boolean canMountCreatures) { this.canMountCreatures = canMountCreatures; return this; }
    public ProfessionBuilder mountSpeedBonus(float mountSpeedBonus) { this.mountSpeedBonus = mountSpeedBonus; return this; }
    public ProfessionBuilder mountDamageBonus(float mountDamageBonus) { this.mountDamageBonus = mountDamageBonus; return this; }
    public ProfessionBuilder mountControlRange(int mountControlRange) { this.mountControlRange = mountControlRange; return this; }
    public ProfessionBuilder mountHealthBonus(float mountHealthBonus) { this.mountHealthBonus = mountHealthBonus; return this; }
    
    public ProfessionBuilder hasGachaAbility(boolean hasGachaAbility) { this.hasGachaAbility = hasGachaAbility; return this; }
    public ProfessionBuilder gachaInterval(int gachaInterval) { this.gachaInterval = gachaInterval; return this; }
    public ProfessionBuilder gachaEntityPool(List<String> gachaEntityPool) { this.gachaEntityPool = gachaEntityPool != null ? gachaEntityPool : new ArrayList<>(); return this; }
    
    public ProfessionBuilder hasDiceAbility(boolean hasDiceAbility) { this.hasDiceAbility = hasDiceAbility; return this; }
    public ProfessionBuilder diceCooldown(int diceCooldown) { this.diceCooldown = diceCooldown; return this; }
    public ProfessionBuilder diceSkillPool(List<String> diceSkillPool) { this.diceSkillPool = diceSkillPool != null ? diceSkillPool : new ArrayList<>(); return this; }
    
    public ProfessionBuilder hasLuckyCloverAbility(boolean hasLuckyCloverAbility) { this.hasLuckyCloverAbility = hasLuckyCloverAbility; return this; }
    public ProfessionBuilder hasDonkBowAbility(boolean hasDonkBowAbility) { this.hasDonkBowAbility = hasDonkBowAbility; return this; }
    
    public ProfessionBuilder hasGourmetAbility(boolean hasGourmetAbility) { this.hasGourmetAbility = hasGourmetAbility; return this; }
    public ProfessionBuilder isGourmet(boolean isGourmet) { this.isGourmet = isGourmet; return this; }
    public ProfessionBuilder gourmetHealthBonus(float gourmetHealthBonus) { this.gourmetHealthBonus = gourmetHealthBonus; return this; }
    public ProfessionBuilder gourmetDamageBonus(float gourmetDamageBonus) { this.gourmetDamageBonus = gourmetDamageBonus; return this; }
    public ProfessionBuilder gourmetDailyLimit(int gourmetDailyLimit) { this.gourmetDailyLimit = gourmetDailyLimit; return this; }
    
    public ProfessionBuilder hasForgetterAbility(boolean hasForgetterAbility) { this.hasForgetterAbility = hasForgetterAbility; return this; }
    public ProfessionBuilder forgetterInterval(int forgetterInterval) { this.forgetterInterval = forgetterInterval; return this; }
    public ProfessionBuilder forgetterMinDuration(int forgetterMinDuration) { this.forgetterMinDuration = forgetterMinDuration; return this; }
    public ProfessionBuilder forgetterMaxDuration(int forgetterMaxDuration) { this.forgetterMaxDuration = forgetterMaxDuration; return this; }
    
    public ProfessionBuilder isFacelessDeceiver(boolean isFacelessDeceiver) { this.isFacelessDeceiver = isFacelessDeceiver; return this; }
    
    public ProfessionBuilder isAngel(boolean isAngel) { this.isAngel = isAngel; return this; }
    public ProfessionBuilder healthRegenInterval(int healthRegenInterval) { this.healthRegenInterval = healthRegenInterval; return this; }
    public ProfessionBuilder healthRegenAmount(float healthRegenAmount) { this.healthRegenAmount = healthRegenAmount; return this; }
    public ProfessionBuilder hasHalo(boolean hasHalo) { this.hasHalo = hasHalo; return this; }
    public ProfessionBuilder haloDetectionRange(float haloDetectionRange) { this.haloDetectionRange = haloDetectionRange; return this; }
    
    public ProfessionBuilder leatherArmorOnly(boolean leatherArmorOnly) { this.leatherArmorOnly = leatherArmorOnly; return this; }
    public ProfessionBuilder ironArmorOnly(boolean ironArmorOnly) { this.ironArmorOnly = ironArmorOnly; return this; }
    public ProfessionBuilder bonusHealth(float bonusHealth) { this.bonusHealth = bonusHealth; return this; }
    public ProfessionBuilder bonusArmorToughness(float bonusArmorToughness) { this.bonusArmorToughness = bonusArmorToughness; return this; }
    public ProfessionBuilder meleeDamageBonus(float meleeDamageBonus) { this.meleeDamageBonus = meleeDamageBonus; return this; }
    public ProfessionBuilder rangedDamageReduction(float rangedDamageReduction) { this.rangedDamageReduction = rangedDamageReduction; return this; }
    public ProfessionBuilder rangedDamagePenalty(float rangedDamagePenalty) { this.rangedDamagePenalty = rangedDamagePenalty; return this; }
    
    public ProfessionBuilder isDeathVenger(boolean isDeathVenger) { this.isDeathVenger = isDeathVenger; return this; }
    public ProfessionBuilder hideNameTag(boolean hideNameTag) { this.hideNameTag = hideNameTag; return this; }
    public ProfessionBuilder hasMarkTargetAbility(boolean hasMarkTargetAbility) { this.hasMarkTargetAbility = hasMarkTargetAbility; return this; }
    
    public ProfessionBuilder isImpostor(boolean isImpostor) { this.isImpostor = isImpostor; return this; }
    public ProfessionBuilder impostorSkillCooldown(int impostorSkillCooldown) { this.impostorSkillCooldown = impostorSkillCooldown; return this; }
    public ProfessionBuilder impostorDisguiseDuration(int impostorDisguiseDuration) { this.impostorDisguiseDuration = impostorDisguiseDuration; return this; }
    
    public ProfessionBuilder hasAmbushAbility(boolean hasAmbushAbility) { this.hasAmbushAbility = hasAmbushAbility; return this; }
    public ProfessionBuilder ambushMaxTargets(int ambushMaxTargets) { this.ambushMaxTargets = ambushMaxTargets; return this; }
    public ProfessionBuilder ambushMaxDistance(int ambushMaxDistance) { this.ambushMaxDistance = ambushMaxDistance; return this; }
    public ProfessionBuilder ambushCooldown(int ambushCooldown) { this.ambushCooldown = ambushCooldown; return this; }
    public ProfessionBuilder ambushInvisDuration(int ambushInvisDuration) { this.ambushInvisDuration = ambushInvisDuration; return this; }
    public ProfessionBuilder passiveInvisSeconds(int passiveInvisSeconds) { this.passiveInvisSeconds = passiveInvisSeconds; return this; }
    
    public ProfessionBuilder isUndead(boolean isUndead) { this.isUndead = isUndead; return this; }
    public ProfessionBuilder lifePoints(int lifePoints) { this.lifePoints = lifePoints; return this; }
    public ProfessionBuilder shieldPoints(int shieldPoints) { this.shieldPoints = shieldPoints; return this; }
    public ProfessionBuilder sunlightVulnerability(boolean sunlightVulnerability) { this.sunlightVulnerability = sunlightVulnerability; return this; }
    public ProfessionBuilder sunlightDamageAmount(float sunlightDamageAmount) { this.sunlightDamageAmount = sunlightDamageAmount; return this; }
    public ProfessionBuilder sunlightDamageInterval(int sunlightDamageInterval) { this.sunlightDamageInterval = sunlightDamageInterval; return this; }
    
    public ProfessionBuilder bonusAttackReach(float bonusAttackReach) { this.bonusAttackReach = bonusAttackReach; return this; }
    
    public ProfessionBuilder hasEndlessMinerAbility(boolean hasEndlessMinerAbility) { this.hasEndlessMinerAbility = hasEndlessMinerAbility; return this; }
    public ProfessionBuilder fortuneLevel(int fortuneLevel) { this.fortuneLevel = fortuneLevel; return this; }
    public ProfessionBuilder stoneDropChance(float stoneDropChance) { this.stoneDropChance = stoneDropChance; return this; }
    
    public ProfessionBuilder isHealer(boolean isHealer) { this.isHealer = isHealer; return this; }
    public ProfessionBuilder healerPassiveRadius(float healerPassiveRadius) { this.healerPassiveRadius = healerPassiveRadius; return this; }
    public ProfessionBuilder healerPassiveHealAmount(float healerPassiveHealAmount) { this.healerPassiveHealAmount = healerPassiveHealAmount; return this; }
    public ProfessionBuilder healerActiveHealAmount(float healerActiveHealAmount) { this.healerActiveHealAmount = healerActiveHealAmount; return this; }
    public ProfessionBuilder healerActiveCooldown(int healerActiveCooldown) { this.healerActiveCooldown = healerActiveCooldown; return this; }
    
    public ProfessionBuilder isFool(boolean isFool) { this.isFool = isFool; return this; }
    public ProfessionBuilder foolStealRange(float foolStealRange) { this.foolStealRange = foolStealRange; return this; }
    public ProfessionBuilder foolStealCooldown(int foolStealCooldown) { this.foolStealCooldown = foolStealCooldown; return this; }
    
    public ProfessionBuilder isHighPriest(boolean isHighPriest) { this.isHighPriest = isHighPriest; return this; }
    public ProfessionBuilder highPriestSacrificeRange(float highPriestSacrificeRange) { this.highPriestSacrificeRange = highPriestSacrificeRange; return this; }
    public ProfessionBuilder highPriestCooldown(int highPriestCooldown) { this.highPriestCooldown = highPriestCooldown; return this; }
    public ProfessionBuilder highPriestReviveHealth(float highPriestReviveHealth) { this.highPriestReviveHealth = highPriestReviveHealth; return this; }
    
    public ProfessionBuilder isGhostSenator(boolean isGhostSenator) { this.isGhostSenator = isGhostSenator; return this; }
    public ProfessionBuilder ghostSenatorHealAmount(float ghostSenatorHealAmount) { this.ghostSenatorHealAmount = ghostSenatorHealAmount; return this; }
    public ProfessionBuilder ghostSenatorStrengthDuration(int ghostSenatorStrengthDuration) { this.ghostSenatorStrengthDuration = ghostSenatorStrengthDuration; return this; }
    public ProfessionBuilder ghostSenatorDetectionRadius(float ghostSenatorDetectionRadius) { this.ghostSenatorDetectionRadius = ghostSenatorDetectionRadius; return this; }
    public ProfessionBuilder isWraithCouncilor(boolean isWraithCouncilor) { this.isWraithCouncilor = isWraithCouncilor; return this; }
    public ProfessionBuilder wraithSoulMax(int wraithSoulMax) { this.wraithSoulMax = wraithSoulMax; return this; }
    public ProfessionBuilder wraithSoulInitial(int wraithSoulInitial) { this.wraithSoulInitial = wraithSoulInitial; return this; }
    public ProfessionBuilder wraithSoulRegenRate(int wraithSoulRegenRate) { this.wraithSoulRegenRate = wraithSoulRegenRate; return this; }
    public ProfessionBuilder wraithSoulDarkBonus(int wraithSoulDarkBonus) { this.wraithSoulDarkBonus = wraithSoulDarkBonus; return this; }
    public ProfessionBuilder wraithSoulSunlightPenalty(float wraithSoulSunlightPenalty) { this.wraithSoulSunlightPenalty = wraithSoulSunlightPenalty; return this; }
    public ProfessionBuilder wraithSoulKillBonus(int wraithSoulKillBonus) { this.wraithSoulKillBonus = wraithSoulKillBonus; return this; }
    public ProfessionBuilder wraithSoulHitBonus(int wraithSoulHitBonus) { this.wraithSoulHitBonus = wraithSoulHitBonus; return this; }
    public ProfessionBuilder wraithSoulSummonKillBonus(int wraithSoulSummonKillBonus) { this.wraithSoulSummonKillBonus = wraithSoulSummonKillBonus; return this; }
    public ProfessionBuilder wraithErosionDamage(float wraithErosionDamage) { this.wraithErosionDamage = wraithErosionDamage; return this; }
    public ProfessionBuilder wraithErosionArmorReduction(float wraithErosionArmorReduction) { this.wraithErosionArmorReduction = wraithErosionArmorReduction; return this; }
    public ProfessionBuilder wraithErosionSlowPercent(float wraithErosionSlowPercent) { this.wraithErosionSlowPercent = wraithErosionSlowPercent; return this; }
    public ProfessionBuilder wraithErosionMaxStacks(int wraithErosionMaxStacks) { this.wraithErosionMaxStacks = wraithErosionMaxStacks; return this; }
    public ProfessionBuilder wraithErosionDuration(int wraithErosionDuration) { this.wraithErosionDuration = wraithErosionDuration; return this; }
    public ProfessionBuilder wraithSummonCost(int wraithSummonCost) { this.wraithSummonCost = wraithSummonCost; return this; }
    public ProfessionBuilder wraithSummonCooldown(int wraithSummonCooldown) { this.wraithSummonCooldown = wraithSummonCooldown; return this; }
    public ProfessionBuilder wraithSummonCount(int wraithSummonCount) { this.wraithSummonCount = wraithSummonCount; return this; }
    public ProfessionBuilder wraithSummonHealth(float wraithSummonHealth) { this.wraithSummonHealth = wraithSummonHealth; return this; }
    public ProfessionBuilder wraithSummonDamage(float wraithSummonDamage) { this.wraithSummonDamage = wraithSummonDamage; return this; }
    public ProfessionBuilder wraithSummonDuration(int wraithSummonDuration) { this.wraithSummonDuration = wraithSummonDuration; return this; }
    public ProfessionBuilder wraithSummonCorpseRange(float wraithSummonCorpseRange) { this.wraithSummonCorpseRange = wraithSummonCorpseRange; return this; }
    public ProfessionBuilder wraithSummonExtraMax(int wraithSummonExtraMax) { this.wraithSummonExtraMax = wraithSummonExtraMax; return this; }
    public ProfessionBuilder wraithDomainCost(int wraithDomainCost) { this.wraithDomainCost = wraithDomainCost; return this; }
    public ProfessionBuilder wraithDomainCooldown(int wraithDomainCooldown) { this.wraithDomainCooldown = wraithDomainCooldown; return this; }
    public ProfessionBuilder wraithDomainRadius(float wraithDomainRadius) { this.wraithDomainRadius = wraithDomainRadius; return this; }
    public ProfessionBuilder wraithDomainDuration(int wraithDomainDuration) { this.wraithDomainDuration = wraithDomainDuration; return this; }
    public ProfessionBuilder wraithDomainDamage(float wraithDomainDamage) { this.wraithDomainDamage = wraithDomainDamage; return this; }
    public ProfessionBuilder wraithDomainBossSlow(float wraithDomainBossSlow) { this.wraithDomainBossSlow = wraithDomainBossSlow; return this; }
    public ProfessionBuilder wraithDomainCharmDuration(int wraithDomainCharmDuration) { this.wraithDomainCharmDuration = wraithDomainCharmDuration; return this; }
    public ProfessionBuilder wraithBarrageBaseCost(int wraithBarrageBaseCost) { this.wraithBarrageBaseCost = wraithBarrageBaseCost; return this; }
    public ProfessionBuilder wraithBarrageChargedCost(int wraithBarrageChargedCost) { this.wraithBarrageChargedCost = wraithBarrageChargedCost; return this; }
    public ProfessionBuilder wraithBarrageCooldown(int wraithBarrageCooldown) { this.wraithBarrageCooldown = wraithBarrageCooldown; return this; }
    public ProfessionBuilder wraithBarrageBaseOrbs(int wraithBarrageBaseOrbs) { this.wraithBarrageBaseOrbs = wraithBarrageBaseOrbs; return this; }
    public ProfessionBuilder wraithBarrageChargedOrbs(int wraithBarrageChargedOrbs) { this.wraithBarrageChargedOrbs = wraithBarrageChargedOrbs; return this; }
    public ProfessionBuilder wraithBarrageBaseDamage(float wraithBarrageBaseDamage) { this.wraithBarrageBaseDamage = wraithBarrageBaseDamage; return this; }
    public ProfessionBuilder wraithBarrageChargedDamage(float wraithBarrageChargedDamage) { this.wraithBarrageChargedDamage = wraithBarrageChargedDamage; return this; }
    public ProfessionBuilder wraithBarrageBaseRange(float wraithBarrageBaseRange) { this.wraithBarrageBaseRange = wraithBarrageBaseRange; return this; }
    public ProfessionBuilder wraithBarrageChargedRange(float wraithBarrageChargedRange) { this.wraithBarrageChargedRange = wraithBarrageChargedRange; return this; }
    public ProfessionBuilder wraithBarrageMaxChargeTime(int wraithBarrageMaxChargeTime) { this.wraithBarrageMaxChargeTime = wraithBarrageMaxChargeTime; return this; }
    public ProfessionBuilder wraithBarrageMaxHits(int wraithBarrageMaxHits) { this.wraithBarrageMaxHits = wraithBarrageMaxHits; return this; }
    public ProfessionBuilder wraithUltimateMinCost(int wraithUltimateMinCost) { this.wraithUltimateMinCost = wraithUltimateMinCost; return this; }
    public ProfessionBuilder wraithUltimateCooldown(int wraithUltimateCooldown) { this.wraithUltimateCooldown = wraithUltimateCooldown; return this; }
    public ProfessionBuilder wraithUltimateCloneCount(int wraithUltimateCloneCount) { this.wraithUltimateCloneCount = wraithUltimateCloneCount; return this; }
    public ProfessionBuilder wraithUltimateCloneDamageRatio(float wraithUltimateCloneDamageRatio) { this.wraithUltimateCloneDamageRatio = wraithUltimateCloneDamageRatio; return this; }
    public ProfessionBuilder wraithUltimateDuration(int wraithUltimateDuration) { this.wraithUltimateDuration = wraithUltimateDuration; return this; }
    public ProfessionBuilder wraithUltimateSoulRegen(int wraithUltimateSoulRegen) { this.wraithUltimateSoulRegen = wraithUltimateSoulRegen; return this; }
    public ProfessionBuilder wraithUltimateDamageIncrease(float wraithUltimateDamageIncrease) { this.wraithUltimateDamageIncrease = wraithUltimateDamageIncrease; return this; }
    public ProfessionBuilder wraithUltimateExhaustDuration(int wraithUltimateExhaustDuration) { this.wraithUltimateExhaustDuration = wraithUltimateExhaustDuration; return this; }
    
    public ProfessionBuilder isEvilPoisoner(boolean isEvilPoisoner) { this.isEvilPoisoner = isEvilPoisoner; return this; }
    public ProfessionBuilder poisonerStrengthDuration(int poisonerStrengthDuration) { this.poisonerStrengthDuration = poisonerStrengthDuration; return this; }
    public ProfessionBuilder hasTurtleAura(boolean hasTurtleAura) { this.hasTurtleAura = hasTurtleAura; return this; }
    public ProfessionBuilder turtleAuraRadius(float turtleAuraRadius) { this.turtleAuraRadius = turtleAuraRadius; return this; }
    public ProfessionBuilder turtleAuraSlownessLevel(int turtleAuraSlownessLevel) { this.turtleAuraSlownessLevel = turtleAuraSlownessLevel; return this; }
    public ProfessionBuilder turtleAuraDuration(int turtleAuraDuration) { this.turtleAuraDuration = turtleAuraDuration; return this; }
    public ProfessionBuilder isJungleApeGod(boolean isJungleApeGod) { this.isJungleApeGod = isJungleApeGod; return this; }
    public ProfessionBuilder rhythmStacksMax(int rhythmStacksMax) { this.rhythmStacksMax = rhythmStacksMax; return this; }
    public ProfessionBuilder rhythmAttackSpeedPerStack(float rhythmAttackSpeedPerStack) { this.rhythmAttackSpeedPerStack = rhythmAttackSpeedPerStack; return this; }
    public ProfessionBuilder rhythmMoveSpeedPerStack(float rhythmMoveSpeedPerStack) { this.rhythmMoveSpeedPerStack = rhythmMoveSpeedPerStack; return this; }
    public ProfessionBuilder berserkDuration(int berserkDuration) { this.berserkDuration = berserkDuration; return this; }
    public ProfessionBuilder berserkCooldownReduction(float berserkCooldownReduction) { this.berserkCooldownReduction = berserkCooldownReduction; return this; }
    public ProfessionBuilder berserkLifeSteal(float berserkLifeSteal) { this.berserkLifeSteal = berserkLifeSteal; return this; }
    public ProfessionBuilder flatDamageReduction(float flatDamageReduction) { this.flatDamageReduction = flatDamageReduction; return this; }
    public ProfessionBuilder resistanceChance(float resistanceChance) { this.resistanceChance = resistanceChance; return this; }
    public ProfessionBuilder resistanceDuration(int resistanceDuration) { this.resistanceDuration = resistanceDuration; return this; }
    public ProfessionBuilder q1DamageMultiplier(float q1DamageMultiplier) { this.q1DamageMultiplier = q1DamageMultiplier; return this; }
    public ProfessionBuilder q1MovingTargetDamageMultiplier(float q1MovingTargetDamageMultiplier) { this.q1MovingTargetDamageMultiplier = q1MovingTargetDamageMultiplier; return this; }
    public ProfessionBuilder q1SlowDuration(int q1SlowDuration) { this.q1SlowDuration = q1SlowDuration; return this; }
    public ProfessionBuilder q1Cooldown(int q1Cooldown) { this.q1Cooldown = q1Cooldown; return this; }
    public ProfessionBuilder q1Angle(float q1Angle) { this.q1Angle = q1Angle; return this; }
    public ProfessionBuilder q2MaxDistance(float q2MaxDistance) { this.q2MaxDistance = q2MaxDistance; return this; }
    public ProfessionBuilder q2DamageMultiplier(float q2DamageMultiplier) { this.q2DamageMultiplier = q2DamageMultiplier; return this; }
    public ProfessionBuilder q2KnockbackDuration(float q2KnockbackDuration) { this.q2KnockbackDuration = q2KnockbackDuration; return this; }
    public ProfessionBuilder q2SplashDamagePercent(float q2SplashDamagePercent) { this.q2SplashDamagePercent = q2SplashDamagePercent; return this; }
    public ProfessionBuilder q2BonusAttackRange(float q2BonusAttackRange) { this.q2BonusAttackRange = q2BonusAttackRange; return this; }
    public ProfessionBuilder q2BonusAttackDuration(int q2BonusAttackDuration) { this.q2BonusAttackDuration = q2BonusAttackDuration; return this; }
    public ProfessionBuilder q2Cooldown(int q2Cooldown) { this.q2Cooldown = q2Cooldown; return this; }
    public ProfessionBuilder q3Radius(float q3Radius) { this.q3Radius = q3Radius; return this; }
    public ProfessionBuilder q3DamageMultiplier(float q3DamageMultiplier) { this.q3DamageMultiplier = q3DamageMultiplier; return this; }
    public ProfessionBuilder q3FearDuration(float q3FearDuration) { this.q3FearDuration = q3FearDuration; return this; }
    public ProfessionBuilder q3BerserkFearDuration(float q3BerserkFearDuration) { this.q3BerserkFearDuration = q3BerserkFearDuration; return this; }
    public ProfessionBuilder q3WeaknessDuration(int q3WeaknessDuration) { this.q3WeaknessDuration = q3WeaknessDuration; return this; }
    public ProfessionBuilder q3Cooldown(int q3Cooldown) { this.q3Cooldown = q3Cooldown; return this; }
    public ProfessionBuilder rDuration(int rDuration) { this.rDuration = rDuration; return this; }
    public ProfessionBuilder rHealPercent(float rHealPercent) { this.rHealPercent = rHealPercent; return this; }
    public ProfessionBuilder rHealthBonusPercent(float rHealthBonusPercent) { this.rHealthBonusPercent = rHealthBonusPercent; return this; }
    public ProfessionBuilder rPowerLevel(int rPowerLevel) { this.rPowerLevel = rPowerLevel; return this; }
    public ProfessionBuilder rSpeedLevel(int rSpeedLevel) { this.rSpeedLevel = rSpeedLevel; return this; }
    public ProfessionBuilder rFatigueDuration(int rFatigueDuration) { this.rFatigueDuration = rFatigueDuration; return this; }
    public ProfessionBuilder rCooldown(int rCooldown) { this.rCooldown = rCooldown; return this; }

    public ProfessionBuilder isByteChen(boolean isByteChen) { this.isByteChen = isByteChen; return this; }
    public ProfessionBuilder byteChenComputeMax(int byteChenComputeMax) { this.byteChenComputeMax = byteChenComputeMax; return this; }
    public ProfessionBuilder byteChenComputeInitial(int byteChenComputeInitial) { this.byteChenComputeInitial = byteChenComputeInitial; return this; }
    public ProfessionBuilder byteChenComputeRegenRate(int byteChenComputeRegenRate) { this.byteChenComputeRegenRate = byteChenComputeRegenRate; return this; }
    public ProfessionBuilder byteChenComputeNodeRegenBonus(float byteChenComputeNodeRegenBonus) { this.byteChenComputeNodeRegenBonus = byteChenComputeNodeRegenBonus; return this; }
    public ProfessionBuilder byteChenComputeOnRead(int byteChenComputeOnRead) { this.byteChenComputeOnRead = byteChenComputeOnRead; return this; }
    public ProfessionBuilder byteChenComputeOnNodeTrigger(int byteChenComputeOnNodeTrigger) { this.byteChenComputeOnNodeTrigger = byteChenComputeOnNodeTrigger; return this; }
    public ProfessionBuilder byteChenComputeOnInterrupt(int byteChenComputeOnInterrupt) { this.byteChenComputeOnInterrupt = byteChenComputeOnInterrupt; return this; }
    public ProfessionBuilder byteChenComputeLowThreshold(int byteChenComputeLowThreshold) { this.byteChenComputeLowThreshold = byteChenComputeLowThreshold; return this; }
    public ProfessionBuilder byteChenNodeMax(int byteChenNodeMax) { this.byteChenNodeMax = byteChenNodeMax; return this; }
    public ProfessionBuilder byteChenNodeRange(int byteChenNodeRange) { this.byteChenNodeRange = byteChenNodeRange; return this; }
    public ProfessionBuilder byteChenNodeDuration(int byteChenNodeDuration) { this.byteChenNodeDuration = byteChenNodeDuration; return this; }
    public ProfessionBuilder byteChenScoutNodeCost(int byteChenScoutNodeCost) { this.byteChenScoutNodeCost = byteChenScoutNodeCost; return this; }
    public ProfessionBuilder byteChenScoutNodeRadius(float byteChenScoutNodeRadius) { this.byteChenScoutNodeRadius = byteChenScoutNodeRadius; return this; }
    public ProfessionBuilder byteChenScoutNodeComputeRegen(int byteChenScoutNodeComputeRegen) { this.byteChenScoutNodeComputeRegen = byteChenScoutNodeComputeRegen; return this; }
    public ProfessionBuilder byteChenBuffNodeCost(int byteChenBuffNodeCost) { this.byteChenBuffNodeCost = byteChenBuffNodeCost; return this; }
    public ProfessionBuilder byteChenBuffNodeRadius(float byteChenBuffNodeRadius) { this.byteChenBuffNodeRadius = byteChenBuffNodeRadius; return this; }
    public ProfessionBuilder byteChenBuffNodeDamageBonus(float byteChenBuffNodeDamageBonus) { this.byteChenBuffNodeDamageBonus = byteChenBuffNodeDamageBonus; return this; }
    public ProfessionBuilder byteChenBuffNodeDamageReduction(float byteChenBuffNodeDamageReduction) { this.byteChenBuffNodeDamageReduction = byteChenBuffNodeDamageReduction; return this; }
    public ProfessionBuilder byteChenBuffNodeSpeedBonus(float byteChenBuffNodeSpeedBonus) { this.byteChenBuffNodeSpeedBonus = byteChenBuffNodeSpeedBonus; return this; }
    public ProfessionBuilder byteChenBuffNodeCooldownReduction(float byteChenBuffNodeCooldownReduction) { this.byteChenBuffNodeCooldownReduction = byteChenBuffNodeCooldownReduction; return this; }
    public ProfessionBuilder byteChenBuffNodeMaxStacks(int byteChenBuffNodeMaxStacks) { this.byteChenBuffNodeMaxStacks = byteChenBuffNodeMaxStacks; return this; }
    public ProfessionBuilder byteChenCounterNodeCost(int byteChenCounterNodeCost) { this.byteChenCounterNodeCost = byteChenCounterNodeCost; return this; }
    public ProfessionBuilder byteChenCounterNodeRadius(float byteChenCounterNodeRadius) { this.byteChenCounterNodeRadius = byteChenCounterNodeRadius; return this; }
    public ProfessionBuilder byteChenCounterNodeSilenceDuration(int byteChenCounterNodeSilenceDuration) { this.byteChenCounterNodeSilenceDuration = byteChenCounterNodeSilenceDuration; return this; }
    public ProfessionBuilder byteChenCounterNodeDisorderDuration(int byteChenCounterNodeDisorderDuration) { this.byteChenCounterNodeDisorderDuration = byteChenCounterNodeDisorderDuration; return this; }
    public ProfessionBuilder byteChenCounterNodeDamageReduction(float byteChenCounterNodeDamageReduction) { this.byteChenCounterNodeDamageReduction = byteChenCounterNodeDamageReduction; return this; }
    public ProfessionBuilder byteChenCounterNodeCooldownMultiplier(float byteChenCounterNodeCooldownMultiplier) { this.byteChenCounterNodeCooldownMultiplier = byteChenCounterNodeCooldownMultiplier; return this; }
    public ProfessionBuilder byteChenDataVisionRange(float byteChenDataVisionRange) { this.byteChenDataVisionRange = byteChenDataVisionRange; return this; }
    public ProfessionBuilder byteChenLightweightSpeedBonus(float byteChenLightweightSpeedBonus) { this.byteChenLightweightSpeedBonus = byteChenLightweightSpeedBonus; return this; }
    public ProfessionBuilder byteChenLightweightDigSpeedBonus(float byteChenLightweightDigSpeedBonus) { this.byteChenLightweightDigSpeedBonus = byteChenLightweightDigSpeedBonus; return this; }
    public ProfessionBuilder byteChenLightweightMeleePenalty(float byteChenLightweightMeleePenalty) { this.byteChenLightweightMeleePenalty = byteChenLightweightMeleePenalty; return this; }
    public ProfessionBuilder byteChenLightweightReceivedMeleePenalty(float byteChenLightweightReceivedMeleePenalty) { this.byteChenLightweightReceivedMeleePenalty = byteChenLightweightReceivedMeleePenalty; return this; }
    public ProfessionBuilder byteChenNodeRecycleCooldown(int byteChenNodeRecycleCooldown) { this.byteChenNodeRecycleCooldown = byteChenNodeRecycleCooldown; return this; }
    public ProfessionBuilder byteChenNodeRecycleRefund(float byteChenNodeRecycleRefund) { this.byteChenNodeRecycleRefund = byteChenNodeRecycleRefund; return this; }
    public ProfessionBuilder byteChenFullReadCost(int byteChenFullReadCost) { this.byteChenFullReadCost = byteChenFullReadCost; return this; }
    public ProfessionBuilder byteChenFullReadCooldown(int byteChenFullReadCooldown) { this.byteChenFullReadCooldown = byteChenFullReadCooldown; return this; }
    public ProfessionBuilder byteChenFullReadRadius(float byteChenFullReadRadius) { this.byteChenFullReadRadius = byteChenFullReadRadius; return this; }
    public ProfessionBuilder byteChenFullReadDuration(int byteChenFullReadDuration) { this.byteChenFullReadDuration = byteChenFullReadDuration; return this; }
    public ProfessionBuilder byteChenDataDispatchCost(int byteChenDataDispatchCost) { this.byteChenDataDispatchCost = byteChenDataDispatchCost; return this; }
    public ProfessionBuilder byteChenDataDispatchCooldown(int byteChenDataDispatchCooldown) { this.byteChenDataDispatchCooldown = byteChenDataDispatchCooldown; return this; }
    public ProfessionBuilder byteChenDataDispatchSpeedBonus(float byteChenDataDispatchSpeedBonus) { this.byteChenDataDispatchSpeedBonus = byteChenDataDispatchSpeedBonus; return this; }
    public ProfessionBuilder byteChenDataDispatchCooldownBonus(float byteChenDataDispatchCooldownBonus) { this.byteChenDataDispatchCooldownBonus = byteChenDataDispatchCooldownBonus; return this; }
    public ProfessionBuilder byteChenDataDispatchBuffDuration(int byteChenDataDispatchBuffDuration) { this.byteChenDataDispatchBuffDuration = byteChenDataDispatchBuffDuration; return this; }
    public ProfessionBuilder byteChenDataBanCost(int byteChenDataBanCost) { this.byteChenDataBanCost = byteChenDataBanCost; return this; }
    public ProfessionBuilder byteChenDataBanCooldown(int byteChenDataBanCooldown) { this.byteChenDataBanCooldown = byteChenDataBanCooldown; return this; }
    public ProfessionBuilder byteChenDataBanRange(float byteChenDataBanRange) { this.byteChenDataBanRange = byteChenDataBanRange; return this; }
    public ProfessionBuilder byteChenDataBanDuration(int byteChenDataBanDuration) { this.byteChenDataBanDuration = byteChenDataBanDuration; return this; }
    public ProfessionBuilder byteChenDataBanBossDuration(int byteChenDataBanBossDuration) { this.byteChenDataBanBossDuration = byteChenDataBanBossDuration; return this; }
    public ProfessionBuilder byteChenDataBanTrueDamageBonus(float byteChenDataBanTrueDamageBonus) { this.byteChenDataBanTrueDamageBonus = byteChenDataBanTrueDamageBonus; return this; }
    public ProfessionBuilder byteChenUltimateMinCost(int byteChenUltimateMinCost) { this.byteChenUltimateMinCost = byteChenUltimateMinCost; return this; }
    public ProfessionBuilder byteChenUltimateCooldown(int byteChenUltimateCooldown) { this.byteChenUltimateCooldown = byteChenUltimateCooldown; return this; }
    public ProfessionBuilder byteChenUltimateRadius(float byteChenUltimateRadius) { this.byteChenUltimateRadius = byteChenUltimateRadius; return this; }
    public ProfessionBuilder byteChenUltimateDuration(int byteChenUltimateDuration) { this.byteChenUltimateDuration = byteChenUltimateDuration; return this; }
    public ProfessionBuilder byteChenUltimateDamageBonus(float byteChenUltimateDamageBonus) { this.byteChenUltimateDamageBonus = byteChenUltimateDamageBonus; return this; }
    public ProfessionBuilder byteChenUltimateDamageReduction(float byteChenUltimateDamageReduction) { this.byteChenUltimateDamageReduction = byteChenUltimateDamageReduction; return this; }
    public ProfessionBuilder byteChenUltimateSpeedBonus(float byteChenUltimateSpeedBonus) { this.byteChenUltimateSpeedBonus = byteChenUltimateSpeedBonus; return this; }
    public ProfessionBuilder byteChenUltimateCooldownBonus(float byteChenUltimateCooldownBonus) { this.byteChenUltimateCooldownBonus = byteChenUltimateCooldownBonus; return this; }
    public ProfessionBuilder byteChenUltimateEnemyDamageReduction(float byteChenUltimateEnemyDamageReduction) { this.byteChenUltimateEnemyDamageReduction = byteChenUltimateEnemyDamageReduction; return this; }
    public ProfessionBuilder byteChenUltimateEnemySpeedReduction(float byteChenUltimateEnemySpeedReduction) { this.byteChenUltimateEnemySpeedReduction = byteChenUltimateEnemySpeedReduction; return this; }
    public ProfessionBuilder byteChenUltimateEnemyCooldownMultiplier(float byteChenUltimateEnemyCooldownMultiplier) { this.byteChenUltimateEnemyCooldownMultiplier = byteChenUltimateEnemyCooldownMultiplier; return this; }
    public ProfessionBuilder byteChenUltimateInterruptChance(float byteChenUltimateInterruptChance) { this.byteChenUltimateInterruptChance = byteChenUltimateInterruptChance; return this; }
    public ProfessionBuilder byteChenExhaustDuration(int byteChenExhaustDuration) { this.byteChenExhaustDuration = byteChenExhaustDuration; return this; }

    public ProfessionBuilder isHeavyKnight(boolean isHeavyKnight) { this.isHeavyKnight = isHeavyKnight; return this; }
    public ProfessionBuilder heavyKnightSpeedPenalty(float heavyKnightSpeedPenalty) { this.heavyKnightSpeedPenalty = heavyKnightSpeedPenalty; return this; }
    public ProfessionBuilder heavyKnightWillOnHit(int heavyKnightWillOnHit) { this.heavyKnightWillOnHit = heavyKnightWillOnHit; return this; }
    public ProfessionBuilder heavyKnightWillOnBlock(int heavyKnightWillOnBlock) { this.heavyKnightWillOnBlock = heavyKnightWillOnBlock; return this; }
    public ProfessionBuilder heavyKnightWillOnDamaged(int heavyKnightWillOnDamaged) { this.heavyKnightWillOnDamaged = heavyKnightWillOnDamaged; return this; }
    public ProfessionBuilder heavyKnightFullWillDamageBonus(float heavyKnightFullWillDamageBonus) { this.heavyKnightFullWillDamageBonus = heavyKnightFullWillDamageBonus; return this; }
    public ProfessionBuilder heavyKnightFullWillDamageReduction(float heavyKnightFullWillDamageReduction) { this.heavyKnightFullWillDamageReduction = heavyKnightFullWillDamageReduction; return this; }
    public ProfessionBuilder heavyKnightShieldWallTriggerTicks(int heavyKnightShieldWallTriggerTicks) { this.heavyKnightShieldWallTriggerTicks = heavyKnightShieldWallTriggerTicks; return this; }
    public ProfessionBuilder heavyKnightShieldWallReduction(float heavyKnightShieldWallReduction) { this.heavyKnightShieldWallReduction = heavyKnightShieldWallReduction; return this; }
    public ProfessionBuilder heavyKnightShieldWallRadius(float heavyKnightShieldWallRadius) { this.heavyKnightShieldWallRadius = heavyKnightShieldWallRadius; return this; }
    public ProfessionBuilder heavyKnightShieldWallArmorBonus(float heavyKnightShieldWallArmorBonus) { this.heavyKnightShieldWallArmorBonus = heavyKnightShieldWallArmorBonus; return this; }
    public ProfessionBuilder heavyKnightChargeWillCost(int heavyKnightChargeWillCost) { this.heavyKnightChargeWillCost = heavyKnightChargeWillCost; return this; }
    public ProfessionBuilder heavyKnightChargeCooldown(int heavyKnightChargeCooldown) { this.heavyKnightChargeCooldown = heavyKnightChargeCooldown; return this; }
    public ProfessionBuilder heavyKnightChargeDistance(float heavyKnightChargeDistance) { this.heavyKnightChargeDistance = heavyKnightChargeDistance; return this; }
    public ProfessionBuilder heavyKnightChargeDamage(float heavyKnightChargeDamage) { this.heavyKnightChargeDamage = heavyKnightChargeDamage; return this; }
    public ProfessionBuilder heavyKnightChargeKnockback(float heavyKnightChargeKnockback) { this.heavyKnightChargeKnockback = heavyKnightChargeKnockback; return this; }
    public ProfessionBuilder heavyKnightProtectRange(int heavyKnightProtectRange) { this.heavyKnightProtectRange = heavyKnightProtectRange; return this; }
    public ProfessionBuilder heavyKnightProtectWillCost(int heavyKnightProtectWillCost) { this.heavyKnightProtectWillCost = heavyKnightProtectWillCost; return this; }
    public ProfessionBuilder heavyKnightProtectCooldown(int heavyKnightProtectCooldown) { this.heavyKnightProtectCooldown = heavyKnightProtectCooldown; return this; }
    public ProfessionBuilder heavyKnightShieldBashDamage(float heavyKnightShieldBashDamage) { this.heavyKnightShieldBashDamage = heavyKnightShieldBashDamage; return this; }
    public ProfessionBuilder heavyKnightShieldBashStunDuration(int heavyKnightShieldBashStunDuration) { this.heavyKnightShieldBashStunDuration = heavyKnightShieldBashStunDuration; return this; }
    public ProfessionBuilder heavyKnightShieldBashCooldown(int heavyKnightShieldBashCooldown) { this.heavyKnightShieldBashCooldown = heavyKnightShieldBashCooldown; return this; }

    public ProfessionBuilder isApostle(boolean isApostle) { this.isApostle = isApostle; return this; }
    public ProfessionBuilder apostleMeleeDamagePercent(float apostleMeleeDamagePercent) { this.apostleMeleeDamagePercent = apostleMeleeDamagePercent; return this; }
    public ProfessionBuilder apostleNetherDamageReduction(float apostleNetherDamageReduction) { this.apostleNetherDamageReduction = apostleNetherDamageReduction; return this; }
    public ProfessionBuilder apostleTeleportCooldown(int apostleTeleportCooldown) { this.apostleTeleportCooldown = apostleTeleportCooldown; return this; }
    public ProfessionBuilder apostleTeleportDistance(float apostleTeleportDistance) { this.apostleTeleportDistance = apostleTeleportDistance; return this; }
    public ProfessionBuilder apostleFireballCooldown(int apostleFireballCooldown) { this.apostleFireballCooldown = apostleFireballCooldown; return this; }
    public ProfessionBuilder apostleFireballDamage(float apostleFireballDamage) { this.apostleFireballDamage = apostleFireballDamage; return this; }
    public ProfessionBuilder apostleDebuffDuration(int apostleDebuffDuration) { this.apostleDebuffDuration = apostleDebuffDuration; return this; }
    public ProfessionBuilder apostleDebuffDamageIncrease(float apostleDebuffDamageIncrease) { this.apostleDebuffDamageIncrease = apostleDebuffDamageIncrease; return this; }
    public ProfessionBuilder apostleHealingReduction(float apostleHealingReduction) { this.apostleHealingReduction = apostleHealingReduction; return this; }
    public ProfessionBuilder apostleArrowFireRadius(int apostleArrowFireRadius) { this.apostleArrowFireRadius = apostleArrowFireRadius; return this; }
    public ProfessionBuilder apostleArrowFireDuration(float apostleArrowFireDuration) { this.apostleArrowFireDuration = apostleArrowFireDuration; return this; }

    public Profession build() {
        return new Profession(this);
    }

    public static ProfessionBuilder fromNBT(CompoundTag tag) {
        ProfessionBuilder builder = new ProfessionBuilder();
        builder.id(tag.getString("id"));
        builder.name(tag.getString("name"));
        builder.description(tag.contains("description") ? tag.getString("description") : "");
        builder.requiresPassword(tag.contains("requiresPassword") && tag.getBoolean("requiresPassword"));
        builder.password(tag.contains("password") ? tag.getString("password") : "");
        builder.iconItem(tag.contains("iconItem") ? tag.getString("iconItem") : "minecraft:paper");
        builder.bonusDamagePercent(tag.contains("bonusDamagePercent") ? tag.getFloat("bonusDamagePercent") : 0.0f);
        builder.slownessLevel(tag.contains("slownessLevel") ? tag.getInt("slownessLevel") : 0);
        builder.weaknessLevel(tag.contains("weaknessLevel") ? tag.getInt("weaknessLevel") : 0);
        builder.bonusArmor(tag.contains("bonusArmor") ? tag.getFloat("bonusArmor") : 0.0f);
        builder.poisonChance(tag.contains("poisonChance") ? tag.getFloat("poisonChance") : 0.0f);
        builder.poisonDuration(tag.contains("poisonDuration") ? tag.getInt("poisonDuration") : 0);
        builder.poisonDamage(tag.contains("poisonDamage") ? tag.getInt("poisonDamage") : 0);
        builder.fireDamageMultiplier(tag.contains("fireDamageMultiplier") ? tag.getFloat("fireDamageMultiplier") : 1.0f);
        builder.resourceItem(tag.contains("resourceItem") ? tag.getString("resourceItem") : "");
        builder.resourceInterval(tag.contains("resourceInterval") ? tag.getInt("resourceInterval") : 0);
        builder.resourceAmount(tag.contains("resourceAmount") ? tag.getInt("resourceAmount") : 0);
        builder.hasEnderPearlAbility(tag.contains("hasEnderPearlAbility") && tag.getBoolean("hasEnderPearlAbility"));
        builder.enderPearlCooldown(tag.contains("enderPearlCooldown") ? tag.getInt("enderPearlCooldown") : 0);
        builder.waterDamage(tag.contains("waterDamage") && tag.getBoolean("waterDamage"));
        builder.waterDamageAmount(tag.contains("waterDamageAmount") ? tag.getFloat("waterDamageAmount") : 0.0f);
        builder.waterDamageInterval(tag.contains("waterDamageInterval") ? tag.getInt("waterDamageInterval") : 0);
        builder.fireTrailEnabled(tag.contains("fireTrailEnabled") && tag.getBoolean("fireTrailEnabled"));
        builder.fireTrailDamage(tag.contains("fireTrailDamage") ? tag.getFloat("fireTrailDamage") : 0.0f);
        builder.fireTrailDuration(tag.contains("fireTrailDuration") ? tag.getInt("fireTrailDuration") : 0);
        builder.fireTrailRadius(tag.contains("fireTrailRadius") ? tag.getFloat("fireTrailRadius") : 0.0f);
        builder.fireDamageBonusPercent(tag.contains("fireDamageBonusPercent") ? tag.getFloat("fireDamageBonusPercent") : 0.0f);
        builder.fireImmunity(tag.contains("fireImmunity") && tag.getBoolean("fireImmunity"));
        builder.waterWeakness(tag.contains("waterWeakness") && tag.getBoolean("waterWeakness"));
        builder.waterWeaknessDamagePercent(tag.contains("waterWeaknessDamagePercent") ? tag.getFloat("waterWeaknessDamagePercent") : 0.0f);
        builder.waterWeaknessInterval(tag.contains("waterWeaknessInterval") ? tag.getInt("waterWeaknessInterval") : 0);
        builder.rainWeakness(tag.contains("rainWeakness") && tag.getBoolean("rainWeakness"));
        builder.isFaceless(tag.contains("isFaceless") && tag.getBoolean("isFaceless"));
        builder.switchInterval(tag.contains("switchInterval") ? tag.getInt("switchInterval") : 0);
        builder.canMountCreatures(tag.contains("canMountCreatures") && tag.getBoolean("canMountCreatures"));
        builder.mountSpeedBonus(tag.contains("mountSpeedBonus") ? tag.getFloat("mountSpeedBonus") : 0.0f);
        builder.mountDamageBonus(tag.contains("mountDamageBonus") ? tag.getFloat("mountDamageBonus") : 0.0f);
        builder.mountControlRange(tag.contains("mountControlRange") ? tag.getInt("mountControlRange") : 32);
        builder.mountHealthBonus(tag.contains("mountHealthBonus") ? tag.getFloat("mountHealthBonus") : 0.0f);
        builder.hasGachaAbility(tag.contains("hasGachaAbility") && tag.getBoolean("hasGachaAbility"));
        builder.gachaInterval(tag.contains("gachaInterval") ? tag.getInt("gachaInterval") : 300);
        builder.gachaEntityPool(loadStringListFromNBT(tag, "gachaEntityPool"));
        builder.hasDiceAbility(tag.contains("hasDiceAbility") && tag.getBoolean("hasDiceAbility"));
        builder.diceCooldown(tag.contains("diceCooldown") ? tag.getInt("diceCooldown") : 60);
        builder.diceSkillPool(loadStringListFromNBT(tag, "diceSkillPool"));
        builder.hasLuckyCloverAbility(tag.contains("hasLuckyCloverAbility") && tag.getBoolean("hasLuckyCloverAbility"));
        builder.hasDonkBowAbility(tag.contains("hasDonkBowAbility") && tag.getBoolean("hasDonkBowAbility"));
        builder.hasGourmetAbility(tag.contains("hasGourmetAbility") && tag.getBoolean("hasGourmetAbility"));
        builder.isGourmet(tag.contains("isGourmet") && tag.getBoolean("isGourmet"));
        builder.gourmetHealthBonus(tag.contains("gourmetHealthBonus") ? tag.getFloat("gourmetHealthBonus") : 4.0f);
        builder.gourmetDamageBonus(tag.contains("gourmetDamageBonus") ? tag.getFloat("gourmetDamageBonus") : 0.5f);
        builder.gourmetDailyLimit(tag.contains("gourmetDailyLimit") ? tag.getInt("gourmetDailyLimit") : 0);
        builder.hasForgetterAbility(tag.contains("hasForgetterAbility") && tag.getBoolean("hasForgetterAbility"));
        builder.forgetterInterval(tag.contains("forgetterInterval") ? tag.getInt("forgetterInterval") : 60);
        builder.forgetterMinDuration(tag.contains("forgetterMinDuration") ? tag.getInt("forgetterMinDuration") : 10);
        builder.forgetterMaxDuration(tag.contains("forgetterMaxDuration") ? tag.getInt("forgetterMaxDuration") : 40);
        builder.isFacelessDeceiver(tag.contains("isFacelessDeceiver") && tag.getBoolean("isFacelessDeceiver"));
        builder.isAngel(tag.contains("isAngel") && tag.getBoolean("isAngel"));
        builder.healthRegenInterval(tag.contains("healthRegenInterval") ? tag.getInt("healthRegenInterval") : 100);
        builder.healthRegenAmount(tag.contains("healthRegenAmount") ? tag.getFloat("healthRegenAmount") : 1.0f);
        builder.hasHalo(tag.contains("hasHalo") && tag.getBoolean("hasHalo"));
        builder.haloDetectionRange(tag.contains("haloDetectionRange") ? tag.getFloat("haloDetectionRange") : 32.0f);
        builder.leatherArmorOnly(tag.contains("leatherArmorOnly") && tag.getBoolean("leatherArmorOnly"));
        builder.ironArmorOnly(tag.contains("ironArmorOnly") && tag.getBoolean("ironArmorOnly"));
        builder.bonusHealth(tag.contains("bonusHealth") ? tag.getFloat("bonusHealth") : 0.0f);
        builder.bonusArmorToughness(tag.contains("bonusArmorToughness") ? tag.getFloat("bonusArmorToughness") : 0.0f);
        builder.meleeDamageBonus(tag.contains("meleeDamageBonus") ? tag.getFloat("meleeDamageBonus") : 0.0f);
        builder.rangedDamageReduction(tag.contains("rangedDamageReduction") ? tag.getFloat("rangedDamageReduction") : 0.0f);
        builder.rangedDamagePenalty(tag.contains("rangedDamagePenalty") ? tag.getFloat("rangedDamagePenalty") : 0.0f);
        builder.isDeathVenger(tag.contains("isDeathVenger") && tag.getBoolean("isDeathVenger"));
        builder.hideNameTag(tag.contains("hideNameTag") && tag.getBoolean("hideNameTag"));
        builder.hasMarkTargetAbility(tag.contains("hasMarkTargetAbility") && tag.getBoolean("hasMarkTargetAbility"));
        builder.isImpostor(tag.contains("isImpostor") && tag.getBoolean("isImpostor"));
        builder.impostorSkillCooldown(tag.contains("impostorSkillCooldown") ? tag.getInt("impostorSkillCooldown") : 180);
        builder.impostorDisguiseDuration(tag.contains("impostorDisguiseDuration") ? tag.getInt("impostorDisguiseDuration") : 120);
        builder.hasAmbushAbility(tag.contains("hasAmbushAbility") && tag.getBoolean("hasAmbushAbility"));
        builder.ambushMaxTargets(tag.contains("ambushMaxTargets") ? tag.getInt("ambushMaxTargets") : 5);
        builder.ambushMaxDistance(tag.contains("ambushMaxDistance") ? tag.getInt("ambushMaxDistance") : 50);
        builder.ambushCooldown(tag.contains("ambushCooldown") ? tag.getInt("ambushCooldown") : 60);
        builder.ambushInvisDuration(tag.contains("ambushInvisDuration") ? tag.getInt("ambushInvisDuration") : 10);
        builder.passiveInvisSeconds(tag.contains("passiveInvisSeconds") ? tag.getInt("passiveInvisSeconds") : 10);
        builder.isUndead(tag.contains("isUndead") && tag.getBoolean("isUndead"));
        builder.lifePoints(tag.contains("lifePoints") ? tag.getInt("lifePoints") : 0);
        builder.shieldPoints(tag.contains("shieldPoints") ? tag.getInt("shieldPoints") : 0);
        builder.sunlightVulnerability(tag.contains("sunlightVulnerability") && tag.getBoolean("sunlightVulnerability"));
        builder.sunlightDamageAmount(tag.contains("sunlightDamageAmount") ? tag.getFloat("sunlightDamageAmount") : 1.0f);
        builder.sunlightDamageInterval(tag.contains("sunlightDamageInterval") ? tag.getInt("sunlightDamageInterval") : 40);
        builder.bonusAttackReach(tag.contains("bonusAttackReach") ? tag.getFloat("bonusAttackReach") : 0.0f);
        builder.hasEndlessMinerAbility(tag.contains("hasEndlessMinerAbility") && tag.getBoolean("hasEndlessMinerAbility"));
        builder.fortuneLevel(tag.contains("fortuneLevel") ? tag.getInt("fortuneLevel") : 2);
        builder.stoneDropChance(tag.contains("stoneDropChance") ? tag.getFloat("stoneDropChance") : 0.05f);
        builder.isHealer(tag.contains("isHealer") && tag.getBoolean("isHealer"));
        builder.healerPassiveRadius(tag.contains("healerPassiveRadius") ? tag.getFloat("healerPassiveRadius") : 5.0f);
        builder.healerPassiveHealAmount(tag.contains("healerPassiveHealAmount") ? tag.getFloat("healerPassiveHealAmount") : 2.0f);
        builder.healerActiveHealAmount(tag.contains("healerActiveHealAmount") ? tag.getFloat("healerActiveHealAmount") : 8.0f);
        builder.healerActiveCooldown(tag.contains("healerActiveCooldown") ? tag.getInt("healerActiveCooldown") : 400);
        builder.isFool(tag.contains("isFool") && tag.getBoolean("isFool"));
        builder.foolStealRange(tag.contains("foolStealRange") ? tag.getFloat("foolStealRange") : 30.0f);
        builder.foolStealCooldown(tag.contains("foolStealCooldown") ? tag.getInt("foolStealCooldown") : 600);
        builder.isHighPriest(tag.contains("isHighPriest") && tag.getBoolean("isHighPriest"));
        builder.highPriestSacrificeRange(tag.contains("highPriestSacrificeRange") ? tag.getFloat("highPriestSacrificeRange") : 30.0f);
        builder.highPriestCooldown(tag.contains("highPriestCooldown") ? tag.getInt("highPriestCooldown") : 600);
        builder.highPriestReviveHealth(tag.contains("highPriestReviveHealth") ? tag.getFloat("highPriestReviveHealth") : 10.0f);
        builder.isGhostSenator(tag.contains("isGhostSenator") && tag.getBoolean("isGhostSenator"));
        builder.ghostSenatorHealAmount(tag.contains("ghostSenatorHealAmount") ? tag.getFloat("ghostSenatorHealAmount") : 7.0f);
        builder.ghostSenatorStrengthDuration(tag.contains("ghostSenatorStrengthDuration") ? tag.getInt("ghostSenatorStrengthDuration") : 300);
        builder.ghostSenatorDetectionRadius(tag.contains("ghostSenatorDetectionRadius") ? tag.getFloat("ghostSenatorDetectionRadius") : 20.0f);
        builder.isWraithCouncilor(tag.contains("isWraithCouncilor") && tag.getBoolean("isWraithCouncilor"));
        builder.wraithSoulMax(tag.contains("wraithSoulMax") ? tag.getInt("wraithSoulMax") : 120);
        builder.wraithSoulInitial(tag.contains("wraithSoulInitial") ? tag.getInt("wraithSoulInitial") : 60);
        builder.wraithSoulRegenRate(tag.contains("wraithSoulRegenRate") ? tag.getInt("wraithSoulRegenRate") : 2);
        builder.wraithSoulDarkBonus(tag.contains("wraithSoulDarkBonus") ? tag.getInt("wraithSoulDarkBonus") : 1);
        builder.wraithSoulSunlightPenalty(tag.contains("wraithSoulSunlightPenalty") ? tag.getFloat("wraithSoulSunlightPenalty") : 0.5f);
        builder.wraithSoulKillBonus(tag.contains("wraithSoulKillBonus") ? tag.getInt("wraithSoulKillBonus") : 15);
        builder.wraithSoulHitBonus(tag.contains("wraithSoulHitBonus") ? tag.getInt("wraithSoulHitBonus") : 3);
        builder.wraithSoulSummonKillBonus(tag.contains("wraithSoulSummonKillBonus") ? tag.getInt("wraithSoulSummonKillBonus") : 8);
        builder.wraithErosionDamage(tag.contains("wraithErosionDamage") ? tag.getFloat("wraithErosionDamage") : 2.0f);
        builder.wraithErosionArmorReduction(tag.contains("wraithErosionArmorReduction") ? tag.getFloat("wraithErosionArmorReduction") : 0.1f);
        builder.wraithErosionSlowPercent(tag.contains("wraithErosionSlowPercent") ? tag.getFloat("wraithErosionSlowPercent") : 0.15f);
        builder.wraithErosionMaxStacks(tag.contains("wraithErosionMaxStacks") ? tag.getInt("wraithErosionMaxStacks") : 5);
        builder.wraithErosionDuration(tag.contains("wraithErosionDuration") ? tag.getInt("wraithErosionDuration") : 100);
        builder.wraithSummonCost(tag.contains("wraithSummonCost") ? tag.getInt("wraithSummonCost") : 35);
        builder.wraithSummonCooldown(tag.contains("wraithSummonCooldown") ? tag.getInt("wraithSummonCooldown") : 360);
        builder.wraithSummonCount(tag.contains("wraithSummonCount") ? tag.getInt("wraithSummonCount") : 2);
        builder.wraithSummonHealth(tag.contains("wraithSummonHealth") ? tag.getFloat("wraithSummonHealth") : 20.0f);
        builder.wraithSummonDamage(tag.contains("wraithSummonDamage") ? tag.getFloat("wraithSummonDamage") : 6.0f);
        builder.wraithSummonDuration(tag.contains("wraithSummonDuration") ? tag.getInt("wraithSummonDuration") : 240);
        builder.wraithSummonCorpseRange(tag.contains("wraithSummonCorpseRange") ? tag.getFloat("wraithSummonCorpseRange") : 5.0f);
        builder.wraithSummonExtraMax(tag.contains("wraithSummonExtraMax") ? tag.getInt("wraithSummonExtraMax") : 2);
        builder.wraithDomainCost(tag.contains("wraithDomainCost") ? tag.getInt("wraithDomainCost") : 40);
        builder.wraithDomainCooldown(tag.contains("wraithDomainCooldown") ? tag.getInt("wraithDomainCooldown") : 440);
        builder.wraithDomainRadius(tag.contains("wraithDomainRadius") ? tag.getFloat("wraithDomainRadius") : 3.0f);
        builder.wraithDomainDuration(tag.contains("wraithDomainDuration") ? tag.getInt("wraithDomainDuration") : 80);
        builder.wraithDomainDamage(tag.contains("wraithDomainDamage") ? tag.getFloat("wraithDomainDamage") : 6.0f);
        builder.wraithDomainBossSlow(tag.contains("wraithDomainBossSlow") ? tag.getFloat("wraithDomainBossSlow") : 0.5f);
        builder.wraithDomainCharmDuration(tag.contains("wraithDomainCharmDuration") ? tag.getInt("wraithDomainCharmDuration") : 80);
        builder.wraithBarrageBaseCost(tag.contains("wraithBarrageBaseCost") ? tag.getInt("wraithBarrageBaseCost") : 25);
        builder.wraithBarrageChargedCost(tag.contains("wraithBarrageChargedCost") ? tag.getInt("wraithBarrageChargedCost") : 10);
        builder.wraithBarrageCooldown(tag.contains("wraithBarrageCooldown") ? tag.getInt("wraithBarrageCooldown") : 200);
        builder.wraithBarrageBaseOrbs(tag.contains("wraithBarrageBaseOrbs") ? tag.getInt("wraithBarrageBaseOrbs") : 6);
        builder.wraithBarrageChargedOrbs(tag.contains("wraithBarrageChargedOrbs") ? tag.getInt("wraithBarrageChargedOrbs") : 12);
        builder.wraithBarrageBaseDamage(tag.contains("wraithBarrageBaseDamage") ? tag.getFloat("wraithBarrageBaseDamage") : 4.0f);
        builder.wraithBarrageChargedDamage(tag.contains("wraithBarrageChargedDamage") ? tag.getFloat("wraithBarrageChargedDamage") : 6.0f);
        builder.wraithBarrageBaseRange(tag.contains("wraithBarrageBaseRange") ? tag.getFloat("wraithBarrageBaseRange") : 8.0f);
        builder.wraithBarrageChargedRange(tag.contains("wraithBarrageChargedRange") ? tag.getFloat("wraithBarrageChargedRange") : 12.0f);
        builder.wraithBarrageMaxChargeTime(tag.contains("wraithBarrageMaxChargeTime") ? tag.getInt("wraithBarrageMaxChargeTime") : 30);
        builder.wraithBarrageMaxHits(tag.contains("wraithBarrageMaxHits") ? tag.getInt("wraithBarrageMaxHits") : 3);
        builder.wraithUltimateMinCost(tag.contains("wraithUltimateMinCost") ? tag.getInt("wraithUltimateMinCost") : 100);
        builder.wraithUltimateCooldown(tag.contains("wraithUltimateCooldown") ? tag.getInt("wraithUltimateCooldown") : 2400);
        builder.wraithUltimateCloneCount(tag.contains("wraithUltimateCloneCount") ? tag.getInt("wraithUltimateCloneCount") : 3);
        builder.wraithUltimateCloneDamageRatio(tag.contains("wraithUltimateCloneDamageRatio") ? tag.getFloat("wraithUltimateCloneDamageRatio") : 0.8f);
        builder.wraithUltimateDuration(tag.contains("wraithUltimateDuration") ? tag.getInt("wraithUltimateDuration") : 300);
        builder.wraithUltimateSoulRegen(tag.contains("wraithUltimateSoulRegen") ? tag.getInt("wraithUltimateSoulRegen") : 10);
        builder.wraithUltimateDamageIncrease(tag.contains("wraithUltimateDamageIncrease") ? tag.getFloat("wraithUltimateDamageIncrease") : 0.2f);
        builder.wraithUltimateExhaustDuration(tag.contains("wraithUltimateExhaustDuration") ? tag.getInt("wraithUltimateExhaustDuration") : 100);
        builder.isEvilPoisoner(tag.contains("isEvilPoisoner") && tag.getBoolean("isEvilPoisoner"));
        builder.poisonerStrengthDuration(tag.contains("poisonerStrengthDuration") ? tag.getInt("poisonerStrengthDuration") : 200);
        builder.hasTurtleAura(tag.contains("hasTurtleAura") && tag.getBoolean("hasTurtleAura"));
        builder.turtleAuraRadius(tag.contains("turtleAuraRadius") ? tag.getFloat("turtleAuraRadius") : 5.0f);
        builder.turtleAuraSlownessLevel(tag.contains("turtleAuraSlownessLevel") ? tag.getInt("turtleAuraSlownessLevel") : 1);
        builder.turtleAuraDuration(tag.contains("turtleAuraDuration") ? tag.getInt("turtleAuraDuration") : 100);
        builder.isJungleApeGod(tag.contains("isJungleApeGod") && tag.getBoolean("isJungleApeGod"));
        builder.rhythmStacksMax(tag.contains("rhythmStacksMax") ? tag.getInt("rhythmStacksMax") : 10);
        builder.rhythmAttackSpeedPerStack(tag.contains("rhythmAttackSpeedPerStack") ? tag.getFloat("rhythmAttackSpeedPerStack") : 0.03f);
        builder.rhythmMoveSpeedPerStack(tag.contains("rhythmMoveSpeedPerStack") ? tag.getFloat("rhythmMoveSpeedPerStack") : 0.02f);
        builder.berserkDuration(tag.contains("berserkDuration") ? tag.getInt("berserkDuration") : 100);
        builder.berserkCooldownReduction(tag.contains("berserkCooldownReduction") ? tag.getFloat("berserkCooldownReduction") : 0.3f);
        builder.berserkLifeSteal(tag.contains("berserkLifeSteal") ? tag.getFloat("berserkLifeSteal") : 0.2f);
        builder.flatDamageReduction(tag.contains("flatDamageReduction") ? tag.getFloat("flatDamageReduction") : 5.0f);
        builder.resistanceChance(tag.contains("resistanceChance") ? tag.getFloat("resistanceChance") : 0.2f);
        builder.resistanceDuration(tag.contains("resistanceDuration") ? tag.getInt("resistanceDuration") : 20);
        builder.q1DamageMultiplier(tag.contains("q1DamageMultiplier") ? tag.getFloat("q1DamageMultiplier") : 2.0f);
        builder.q1MovingTargetDamageMultiplier(tag.contains("q1MovingTargetDamageMultiplier") ? tag.getFloat("q1MovingTargetDamageMultiplier") : 2.8f);
        builder.q1SlowDuration(tag.contains("q1SlowDuration") ? tag.getInt("q1SlowDuration") : 20);
        builder.q1Cooldown(tag.contains("q1Cooldown") ? tag.getInt("q1Cooldown") : 200);
        builder.q1Angle(tag.contains("q1Angle") ? tag.getFloat("q1Angle") : 90.0f);
        builder.q2MaxDistance(tag.contains("q2MaxDistance") ? tag.getFloat("q2MaxDistance") : 25.0f);
        builder.q2DamageMultiplier(tag.contains("q2DamageMultiplier") ? tag.getFloat("q2DamageMultiplier") : 1.5f);
        builder.q2KnockbackDuration(tag.contains("q2KnockbackDuration") ? tag.getFloat("q2KnockbackDuration") : 10.0f);
        builder.q2SplashDamagePercent(tag.contains("q2SplashDamagePercent") ? tag.getFloat("q2SplashDamagePercent") : 0.5f);
        builder.q2BonusAttackRange(tag.contains("q2BonusAttackRange") ? tag.getFloat("q2BonusAttackRange") : 2.0f);
        builder.q2BonusAttackDuration(tag.contains("q2BonusAttackDuration") ? tag.getInt("q2BonusAttackDuration") : 60);
        builder.q2Cooldown(tag.contains("q2Cooldown") ? tag.getInt("q2Cooldown") : 200);
        builder.q3Radius(tag.contains("q3Radius") ? tag.getFloat("q3Radius") : 6.0f);
        builder.q3DamageMultiplier(tag.contains("q3DamageMultiplier") ? tag.getFloat("q3DamageMultiplier") : 1.0f);
        builder.q3FearDuration(tag.contains("q3FearDuration") ? tag.getFloat("q3FearDuration") : 1.5f);
        builder.q3BerserkFearDuration(tag.contains("q3BerserkFearDuration") ? tag.getFloat("q3BerserkFearDuration") : 2.5f);
        builder.q3WeaknessDuration(tag.contains("q3WeaknessDuration") ? tag.getInt("q3WeaknessDuration") : 60);
        builder.q3Cooldown(tag.contains("q3Cooldown") ? tag.getInt("q3Cooldown") : 400);
        builder.rDuration(tag.contains("rDuration") ? tag.getInt("rDuration") : 200);
        builder.rHealPercent(tag.contains("rHealPercent") ? tag.getFloat("rHealPercent") : 0.3f);
        builder.rHealthBonusPercent(tag.contains("rHealthBonusPercent") ? tag.getFloat("rHealthBonusPercent") : 0.5f);
        builder.rPowerLevel(tag.contains("rPowerLevel") ? tag.getInt("rPowerLevel") : 4);
        builder.rSpeedLevel(tag.contains("rSpeedLevel") ? tag.getInt("rSpeedLevel") : 3);
        builder.rFatigueDuration(tag.contains("rFatigueDuration") ? tag.getInt("rFatigueDuration") : 40);
        builder.rCooldown(tag.contains("rCooldown") ? tag.getInt("rCooldown") : 1200);
        builder.isByteChen(tag.contains("isByteChen") && tag.getBoolean("isByteChen"));
        builder.byteChenComputeMax(tag.contains("byteChenComputeMax") ? tag.getInt("byteChenComputeMax") : 150);
        builder.byteChenComputeInitial(tag.contains("byteChenComputeInitial") ? tag.getInt("byteChenComputeInitial") : 150);
        builder.byteChenComputeRegenRate(tag.contains("byteChenComputeRegenRate") ? tag.getInt("byteChenComputeRegenRate") : 3);
        builder.byteChenComputeNodeRegenBonus(tag.contains("byteChenComputeNodeRegenBonus") ? tag.getFloat("byteChenComputeNodeRegenBonus") : 0.5f);
        builder.byteChenComputeOnRead(tag.contains("byteChenComputeOnRead") ? tag.getInt("byteChenComputeOnRead") : 10);
        builder.byteChenComputeOnNodeTrigger(tag.contains("byteChenComputeOnNodeTrigger") ? tag.getInt("byteChenComputeOnNodeTrigger") : 8);
        builder.byteChenComputeOnInterrupt(tag.contains("byteChenComputeOnInterrupt") ? tag.getInt("byteChenComputeOnInterrupt") : 25);
        builder.byteChenComputeLowThreshold(tag.contains("byteChenComputeLowThreshold") ? tag.getInt("byteChenComputeLowThreshold") : 20);
        builder.byteChenNodeMax(tag.contains("byteChenNodeMax") ? tag.getInt("byteChenNodeMax") : 8);
        builder.byteChenNodeRange(tag.contains("byteChenNodeRange") ? tag.getInt("byteChenNodeRange") : 32);
        builder.byteChenNodeDuration(tag.contains("byteChenNodeDuration") ? tag.getInt("byteChenNodeDuration") : 1200);
        builder.byteChenScoutNodeCost(tag.contains("byteChenScoutNodeCost") ? tag.getInt("byteChenScoutNodeCost") : 20);
        builder.byteChenScoutNodeRadius(tag.contains("byteChenScoutNodeRadius") ? tag.getFloat("byteChenScoutNodeRadius") : 12.0f);
        builder.byteChenScoutNodeComputeRegen(tag.contains("byteChenScoutNodeComputeRegen") ? tag.getInt("byteChenScoutNodeComputeRegen") : 1);
        builder.byteChenBuffNodeCost(tag.contains("byteChenBuffNodeCost") ? tag.getInt("byteChenBuffNodeCost") : 25);
        builder.byteChenBuffNodeRadius(tag.contains("byteChenBuffNodeRadius") ? tag.getFloat("byteChenBuffNodeRadius") : 8.0f);
        builder.byteChenBuffNodeDamageBonus(tag.contains("byteChenBuffNodeDamageBonus") ? tag.getFloat("byteChenBuffNodeDamageBonus") : 0.1f);
        builder.byteChenBuffNodeDamageReduction(tag.contains("byteChenBuffNodeDamageReduction") ? tag.getFloat("byteChenBuffNodeDamageReduction") : 0.1f);
        builder.byteChenBuffNodeSpeedBonus(tag.contains("byteChenBuffNodeSpeedBonus") ? tag.getFloat("byteChenBuffNodeSpeedBonus") : 0.15f);
        builder.byteChenBuffNodeCooldownReduction(tag.contains("byteChenBuffNodeCooldownReduction") ? tag.getFloat("byteChenBuffNodeCooldownReduction") : 0.1f);
        builder.byteChenBuffNodeMaxStacks(tag.contains("byteChenBuffNodeMaxStacks") ? tag.getInt("byteChenBuffNodeMaxStacks") : 3);
        builder.byteChenCounterNodeCost(tag.contains("byteChenCounterNodeCost") ? tag.getInt("byteChenCounterNodeCost") : 30);
        builder.byteChenCounterNodeRadius(tag.contains("byteChenCounterNodeRadius") ? tag.getFloat("byteChenCounterNodeRadius") : 6.0f);
        builder.byteChenCounterNodeSilenceDuration(tag.contains("byteChenCounterNodeSilenceDuration") ? tag.getInt("byteChenCounterNodeSilenceDuration") : 40);
        builder.byteChenCounterNodeDisorderDuration(tag.contains("byteChenCounterNodeDisorderDuration") ? tag.getInt("byteChenCounterNodeDisorderDuration") : 100);
        builder.byteChenCounterNodeDamageReduction(tag.contains("byteChenCounterNodeDamageReduction") ? tag.getFloat("byteChenCounterNodeDamageReduction") : 0.15f);
        builder.byteChenCounterNodeCooldownMultiplier(tag.contains("byteChenCounterNodeCooldownMultiplier") ? tag.getFloat("byteChenCounterNodeCooldownMultiplier") : 2.0f);
        builder.byteChenDataVisionRange(tag.contains("byteChenDataVisionRange") ? tag.getFloat("byteChenDataVisionRange") : 16.0f);
        builder.byteChenLightweightSpeedBonus(tag.contains("byteChenLightweightSpeedBonus") ? tag.getFloat("byteChenLightweightSpeedBonus") : 0.15f);
        builder.byteChenLightweightDigSpeedBonus(tag.contains("byteChenLightweightDigSpeedBonus") ? tag.getFloat("byteChenLightweightDigSpeedBonus") : 0.2f);
        builder.byteChenLightweightMeleePenalty(tag.contains("byteChenLightweightMeleePenalty") ? tag.getFloat("byteChenLightweightMeleePenalty") : -0.45f);
        builder.byteChenLightweightReceivedMeleePenalty(tag.contains("byteChenLightweightReceivedMeleePenalty") ? tag.getFloat("byteChenLightweightReceivedMeleePenalty") : 0.2f);
        builder.byteChenNodeRecycleCooldown(tag.contains("byteChenNodeRecycleCooldown") ? tag.getInt("byteChenNodeRecycleCooldown") : 200);
        builder.byteChenNodeRecycleRefund(tag.contains("byteChenNodeRecycleRefund") ? tag.getFloat("byteChenNodeRecycleRefund") : 0.8f);
        builder.byteChenFullReadCost(tag.contains("byteChenFullReadCost") ? tag.getInt("byteChenFullReadCost") : 30);
        builder.byteChenFullReadCooldown(tag.contains("byteChenFullReadCooldown") ? tag.getInt("byteChenFullReadCooldown") : 300);
        builder.byteChenFullReadRadius(tag.contains("byteChenFullReadRadius") ? tag.getFloat("byteChenFullReadRadius") : 32.0f);
        builder.byteChenFullReadDuration(tag.contains("byteChenFullReadDuration") ? tag.getInt("byteChenFullReadDuration") : 160);
        builder.byteChenDataDispatchCost(tag.contains("byteChenDataDispatchCost") ? tag.getInt("byteChenDataDispatchCost") : 40);
        builder.byteChenDataDispatchCooldown(tag.contains("byteChenDataDispatchCooldown") ? tag.getInt("byteChenDataDispatchCooldown") : 400);
        builder.byteChenDataDispatchSpeedBonus(tag.contains("byteChenDataDispatchSpeedBonus") ? tag.getFloat("byteChenDataDispatchSpeedBonus") : 0.2f);
        builder.byteChenDataDispatchCooldownBonus(tag.contains("byteChenDataDispatchCooldownBonus") ? tag.getFloat("byteChenDataDispatchCooldownBonus") : 0.2f);
        builder.byteChenDataDispatchBuffDuration(tag.contains("byteChenDataDispatchBuffDuration") ? tag.getInt("byteChenDataDispatchBuffDuration") : 160);
        builder.byteChenDataBanCost(tag.contains("byteChenDataBanCost") ? tag.getInt("byteChenDataBanCost") : 35);
        builder.byteChenDataBanCooldown(tag.contains("byteChenDataBanCooldown") ? tag.getInt("byteChenDataBanCooldown") : 360);
        builder.byteChenDataBanRange(tag.contains("byteChenDataBanRange") ? tag.getFloat("byteChenDataBanRange") : 12.0f);
        builder.byteChenDataBanDuration(tag.contains("byteChenDataBanDuration") ? tag.getInt("byteChenDataBanDuration") : 80);
        builder.byteChenDataBanBossDuration(tag.contains("byteChenDataBanBossDuration") ? tag.getInt("byteChenDataBanBossDuration") : 40);
        builder.byteChenDataBanTrueDamageBonus(tag.contains("byteChenDataBanTrueDamageBonus") ? tag.getFloat("byteChenDataBanTrueDamageBonus") : 0.1f);
        builder.byteChenUltimateMinCost(tag.contains("byteChenUltimateMinCost") ? tag.getInt("byteChenUltimateMinCost") : 120);
        builder.byteChenUltimateCooldown(tag.contains("byteChenUltimateCooldown") ? tag.getInt("byteChenUltimateCooldown") : 2400);
        builder.byteChenUltimateRadius(tag.contains("byteChenUltimateRadius") ? tag.getFloat("byteChenUltimateRadius") : 64.0f);
        builder.byteChenUltimateDuration(tag.contains("byteChenUltimateDuration") ? tag.getInt("byteChenUltimateDuration") : 240);
        builder.byteChenUltimateDamageBonus(tag.contains("byteChenUltimateDamageBonus") ? tag.getFloat("byteChenUltimateDamageBonus") : 0.25f);
        builder.byteChenUltimateDamageReduction(tag.contains("byteChenUltimateDamageReduction") ? tag.getFloat("byteChenUltimateDamageReduction") : 0.25f);
        builder.byteChenUltimateSpeedBonus(tag.contains("byteChenUltimateSpeedBonus") ? tag.getFloat("byteChenUltimateSpeedBonus") : 0.3f);
        builder.byteChenUltimateCooldownBonus(tag.contains("byteChenUltimateCooldownBonus") ? tag.getFloat("byteChenUltimateCooldownBonus") : 0.3f);
        builder.byteChenUltimateEnemyDamageReduction(tag.contains("byteChenUltimateEnemyDamageReduction") ? tag.getFloat("byteChenUltimateEnemyDamageReduction") : 0.3f);
        builder.byteChenUltimateEnemySpeedReduction(tag.contains("byteChenUltimateEnemySpeedReduction") ? tag.getFloat("byteChenUltimateEnemySpeedReduction") : 0.25f);
        builder.byteChenUltimateEnemyCooldownMultiplier(tag.contains("byteChenUltimateEnemyCooldownMultiplier") ? tag.getFloat("byteChenUltimateEnemyCooldownMultiplier") : 2.0f);
        builder.byteChenUltimateInterruptChance(tag.contains("byteChenUltimateInterruptChance") ? tag.getFloat("byteChenUltimateInterruptChance") : 0.5f);
        builder.byteChenExhaustDuration(tag.contains("byteChenExhaustDuration") ? tag.getInt("byteChenExhaustDuration") : 160);
        builder.isHeavyKnight(tag.contains("isHeavyKnight") && tag.getBoolean("isHeavyKnight"));
        builder.heavyKnightSpeedPenalty(tag.contains("heavyKnightSpeedPenalty") ? tag.getFloat("heavyKnightSpeedPenalty") : 0.2f);
        builder.heavyKnightWillOnHit(tag.contains("heavyKnightWillOnHit") ? tag.getInt("heavyKnightWillOnHit") : 5);
        builder.heavyKnightWillOnBlock(tag.contains("heavyKnightWillOnBlock") ? tag.getInt("heavyKnightWillOnBlock") : 6);
        builder.heavyKnightWillOnDamaged(tag.contains("heavyKnightWillOnDamaged") ? tag.getInt("heavyKnightWillOnDamaged") : 8);
        builder.heavyKnightFullWillDamageBonus(tag.contains("heavyKnightFullWillDamageBonus") ? tag.getFloat("heavyKnightFullWillDamageBonus") : 0.1f);
        builder.heavyKnightFullWillDamageReduction(tag.contains("heavyKnightFullWillDamageReduction") ? tag.getFloat("heavyKnightFullWillDamageReduction") : 0.1f);
        builder.heavyKnightShieldWallTriggerTicks(tag.contains("heavyKnightShieldWallTriggerTicks") ? tag.getInt("heavyKnightShieldWallTriggerTicks") : 40);
        builder.heavyKnightShieldWallReduction(tag.contains("heavyKnightShieldWallReduction") ? tag.getFloat("heavyKnightShieldWallReduction") : 0.2f);
        builder.heavyKnightShieldWallRadius(tag.contains("heavyKnightShieldWallRadius") ? tag.getFloat("heavyKnightShieldWallRadius") : 8.0f);
        builder.heavyKnightShieldWallArmorBonus(tag.contains("heavyKnightShieldWallArmorBonus") ? tag.getFloat("heavyKnightShieldWallArmorBonus") : 5.0f);
        builder.heavyKnightChargeWillCost(tag.contains("heavyKnightChargeWillCost") ? tag.getInt("heavyKnightChargeWillCost") : 30);
        builder.heavyKnightChargeCooldown(tag.contains("heavyKnightChargeCooldown") ? tag.getInt("heavyKnightChargeCooldown") : 240);
        builder.heavyKnightChargeDistance(tag.contains("heavyKnightChargeDistance") ? tag.getFloat("heavyKnightChargeDistance") : 5.0f);
        builder.heavyKnightChargeDamage(tag.contains("heavyKnightChargeDamage") ? tag.getFloat("heavyKnightChargeDamage") : 12.0f);
        builder.heavyKnightChargeKnockback(tag.contains("heavyKnightChargeKnockback") ? tag.getFloat("heavyKnightChargeKnockback") : 3.0f);
        builder.heavyKnightProtectRange(tag.contains("heavyKnightProtectRange") ? tag.getInt("heavyKnightProtectRange") : 6);
        builder.heavyKnightProtectWillCost(tag.contains("heavyKnightProtectWillCost") ? tag.getInt("heavyKnightProtectWillCost") : 20);
        builder.heavyKnightProtectCooldown(tag.contains("heavyKnightProtectCooldown") ? tag.getInt("heavyKnightProtectCooldown") : 400);
        builder.heavyKnightShieldBashDamage(tag.contains("heavyKnightShieldBashDamage") ? tag.getFloat("heavyKnightShieldBashDamage") : 4.0f);
        builder.heavyKnightShieldBashStunDuration(tag.contains("heavyKnightShieldBashStunDuration") ? tag.getInt("heavyKnightShieldBashStunDuration") : 30);
        builder.heavyKnightShieldBashCooldown(tag.contains("heavyKnightShieldBashCooldown") ? tag.getInt("heavyKnightShieldBashCooldown") : 80);
        builder.isApostle(tag.contains("isApostle") && tag.getBoolean("isApostle"));
        builder.apostleMeleeDamagePercent(tag.contains("apostleMeleeDamagePercent") ? tag.getFloat("apostleMeleeDamagePercent") : 0.1f);
        builder.apostleNetherDamageReduction(tag.contains("apostleNetherDamageReduction") ? tag.getFloat("apostleNetherDamageReduction") : 0.5f);
        builder.apostleTeleportCooldown(tag.contains("apostleTeleportCooldown") ? tag.getInt("apostleTeleportCooldown") : 200);
        builder.apostleTeleportDistance(tag.contains("apostleTeleportDistance") ? tag.getFloat("apostleTeleportDistance") : 8.0f);
        builder.apostleFireballCooldown(tag.contains("apostleFireballCooldown") ? tag.getInt("apostleFireballCooldown") : 300);
        builder.apostleFireballDamage(tag.contains("apostleFireballDamage") ? tag.getFloat("apostleFireballDamage") : 6.0f);
        builder.apostleDebuffDuration(tag.contains("apostleDebuffDuration") ? tag.getInt("apostleDebuffDuration") : 100);
        builder.apostleDebuffDamageIncrease(tag.contains("apostleDebuffDamageIncrease") ? tag.getFloat("apostleDebuffDamageIncrease") : 0.2f);
        builder.apostleHealingReduction(tag.contains("apostleHealingReduction") ? tag.getFloat("apostleHealingReduction") : 0.5f);
        builder.apostleArrowFireRadius(tag.contains("apostleArrowFireRadius") ? tag.getInt("apostleArrowFireRadius") : 3);
        builder.apostleArrowFireDuration(tag.contains("apostleArrowFireDuration") ? tag.getFloat("apostleArrowFireDuration") : 5.0f);
        return builder;
    }

    public static ProfessionBuilder fromBuffer(FriendlyByteBuf buffer) {
        ProfessionBuilder builder = new ProfessionBuilder();
        builder.id(buffer.readUtf());
        builder.name(buffer.readUtf());
        builder.description(buffer.readUtf());
        builder.requiresPassword(buffer.readBoolean());
        builder.password(buffer.readUtf());
        builder.iconItem(buffer.readUtf());
        builder.bonusDamagePercent(buffer.readFloat());
        builder.slownessLevel(buffer.readInt());
        builder.weaknessLevel(buffer.readInt());
        builder.bonusArmor(buffer.readFloat());
        builder.poisonChance(buffer.readFloat());
        builder.poisonDuration(buffer.readInt());
        builder.poisonDamage(buffer.readInt());
        builder.fireDamageMultiplier(buffer.readFloat());
        builder.resourceItem(buffer.readUtf());
        builder.resourceInterval(buffer.readInt());
        builder.resourceAmount(buffer.readInt());
        builder.hasEnderPearlAbility(buffer.readBoolean());
        builder.enderPearlCooldown(buffer.readInt());
        builder.waterDamage(buffer.readBoolean());
        builder.waterDamageAmount(buffer.readFloat());
        builder.waterDamageInterval(buffer.readInt());
        builder.fireTrailEnabled(buffer.readBoolean());
        builder.fireTrailDamage(buffer.readFloat());
        builder.fireTrailDuration(buffer.readInt());
        builder.fireTrailRadius(buffer.readFloat());
        builder.fireDamageBonusPercent(buffer.readFloat());
        builder.fireImmunity(buffer.readBoolean());
        builder.waterWeakness(buffer.readBoolean());
        builder.waterWeaknessDamagePercent(buffer.readFloat());
        builder.waterWeaknessInterval(buffer.readInt());
        builder.rainWeakness(buffer.readBoolean());
        builder.isFaceless(buffer.readBoolean());
        builder.switchInterval(buffer.readInt());
        builder.canMountCreatures(buffer.readBoolean());
        builder.mountSpeedBonus(buffer.readFloat());
        builder.mountDamageBonus(buffer.readFloat());
        builder.mountControlRange(buffer.readInt());
        builder.mountHealthBonus(buffer.readFloat());
        builder.hasGachaAbility(buffer.readBoolean());
        builder.gachaInterval(buffer.readInt());
        builder.gachaEntityPool(readStringListFromBuffer(buffer));
        builder.hasDiceAbility(buffer.readBoolean());
        builder.diceCooldown(buffer.readInt());
        builder.diceSkillPool(readStringListFromBuffer(buffer));
        builder.hasLuckyCloverAbility(buffer.readBoolean());
        builder.hasDonkBowAbility(buffer.readBoolean());
        builder.hasGourmetAbility(buffer.readBoolean());
        builder.isGourmet(buffer.readBoolean());
        builder.gourmetHealthBonus(buffer.readFloat());
        builder.gourmetDamageBonus(buffer.readFloat());
        builder.gourmetDailyLimit(buffer.readInt());
        builder.hasForgetterAbility(buffer.readBoolean());
        builder.forgetterInterval(buffer.readInt());
        builder.forgetterMinDuration(buffer.readInt());
        builder.forgetterMaxDuration(buffer.readInt());
        builder.isFacelessDeceiver(buffer.readBoolean());
        builder.isAngel(buffer.readBoolean());
        builder.healthRegenInterval(buffer.readInt());
        builder.healthRegenAmount(buffer.readFloat());
        builder.hasHalo(buffer.readBoolean());
        builder.haloDetectionRange(buffer.readFloat());
        builder.leatherArmorOnly(buffer.readBoolean());
        builder.ironArmorOnly(buffer.readBoolean());
        builder.bonusHealth(buffer.readFloat());
        builder.bonusArmorToughness(buffer.readFloat());
        builder.meleeDamageBonus(buffer.readFloat());
        builder.rangedDamageReduction(buffer.readFloat());
        builder.rangedDamagePenalty(buffer.readFloat());
        builder.isDeathVenger(buffer.readBoolean());
        builder.hideNameTag(buffer.readBoolean());
        builder.hasMarkTargetAbility(buffer.readBoolean());
        builder.isImpostor(buffer.readBoolean());
        builder.impostorSkillCooldown(buffer.readInt());
        builder.impostorDisguiseDuration(buffer.readInt());
        builder.hasAmbushAbility(buffer.readBoolean());
        builder.ambushMaxTargets(buffer.readInt());
        builder.ambushMaxDistance(buffer.readInt());
        builder.ambushCooldown(buffer.readInt());
        builder.ambushInvisDuration(buffer.readInt());
        builder.passiveInvisSeconds(buffer.readInt());
        builder.isUndead(buffer.readBoolean());
        builder.lifePoints(buffer.readInt());
        builder.shieldPoints(buffer.readInt());
        builder.sunlightVulnerability(buffer.readBoolean());
        builder.sunlightDamageAmount(buffer.readFloat());
        builder.sunlightDamageInterval(buffer.readInt());
        builder.bonusAttackReach(buffer.readFloat());
        builder.hasEndlessMinerAbility(buffer.readBoolean());
        builder.fortuneLevel(buffer.readInt());
        builder.stoneDropChance(buffer.readFloat());
        builder.isHealer(buffer.readBoolean());
        builder.healerPassiveRadius(buffer.readFloat());
        builder.healerPassiveHealAmount(buffer.readFloat());
        builder.healerActiveHealAmount(buffer.readFloat());
        builder.healerActiveCooldown(buffer.readInt());
        builder.isFool(buffer.readBoolean());
        builder.foolStealRange(buffer.readFloat());
        builder.foolStealCooldown(buffer.readInt());
        builder.isHighPriest(buffer.readBoolean());
        builder.highPriestSacrificeRange(buffer.readFloat());
        builder.highPriestCooldown(buffer.readInt());
        builder.highPriestReviveHealth(buffer.readFloat());
        builder.isGhostSenator(buffer.readBoolean());
        builder.ghostSenatorHealAmount(buffer.readFloat());
        builder.ghostSenatorStrengthDuration(buffer.readInt());
        builder.ghostSenatorDetectionRadius(buffer.readFloat());
        builder.isWraithCouncilor(buffer.readBoolean());
        builder.wraithSoulMax(buffer.readInt());
        builder.wraithSoulInitial(buffer.readInt());
        builder.wraithSoulRegenRate(buffer.readInt());
        builder.wraithSoulDarkBonus(buffer.readInt());
        builder.wraithSoulSunlightPenalty(buffer.readFloat());
        builder.wraithSoulKillBonus(buffer.readInt());
        builder.wraithSoulHitBonus(buffer.readInt());
        builder.wraithSoulSummonKillBonus(buffer.readInt());
        builder.wraithErosionDamage(buffer.readFloat());
        builder.wraithErosionArmorReduction(buffer.readFloat());
        builder.wraithErosionSlowPercent(buffer.readFloat());
        builder.wraithErosionMaxStacks(buffer.readInt());
        builder.wraithErosionDuration(buffer.readInt());
        builder.wraithSummonCost(buffer.readInt());
        builder.wraithSummonCooldown(buffer.readInt());
        builder.wraithSummonCount(buffer.readInt());
        builder.wraithSummonHealth(buffer.readFloat());
        builder.wraithSummonDamage(buffer.readFloat());
        builder.wraithSummonDuration(buffer.readInt());
        builder.wraithSummonCorpseRange(buffer.readFloat());
        builder.wraithSummonExtraMax(buffer.readInt());
        builder.wraithDomainCost(buffer.readInt());
        builder.wraithDomainCooldown(buffer.readInt());
        builder.wraithDomainRadius(buffer.readFloat());
        builder.wraithDomainDuration(buffer.readInt());
        builder.wraithDomainDamage(buffer.readFloat());
        builder.wraithDomainBossSlow(buffer.readFloat());
        builder.wraithDomainCharmDuration(buffer.readInt());
        builder.wraithBarrageBaseCost(buffer.readInt());
        builder.wraithBarrageChargedCost(buffer.readInt());
        builder.wraithBarrageCooldown(buffer.readInt());
        builder.wraithBarrageBaseOrbs(buffer.readInt());
        builder.wraithBarrageChargedOrbs(buffer.readInt());
        builder.wraithBarrageBaseDamage(buffer.readFloat());
        builder.wraithBarrageChargedDamage(buffer.readFloat());
        builder.wraithBarrageBaseRange(buffer.readFloat());
        builder.wraithBarrageChargedRange(buffer.readFloat());
        builder.wraithBarrageMaxChargeTime(buffer.readInt());
        builder.wraithBarrageMaxHits(buffer.readInt());
        builder.wraithUltimateMinCost(buffer.readInt());
        builder.wraithUltimateCooldown(buffer.readInt());
        builder.wraithUltimateCloneCount(buffer.readInt());
        builder.wraithUltimateCloneDamageRatio(buffer.readFloat());
        builder.wraithUltimateDuration(buffer.readInt());
        builder.wraithUltimateSoulRegen(buffer.readInt());
        builder.wraithUltimateDamageIncrease(buffer.readFloat());
        builder.wraithUltimateExhaustDuration(buffer.readInt());
        builder.isEvilPoisoner(buffer.readBoolean());
        builder.poisonerStrengthDuration(buffer.readInt());
        builder.hasTurtleAura(buffer.readBoolean());
        builder.turtleAuraRadius(buffer.readFloat());
        builder.turtleAuraSlownessLevel(buffer.readInt());
        builder.turtleAuraDuration(buffer.readInt());
        builder.isJungleApeGod(buffer.readBoolean());
        builder.rhythmStacksMax(buffer.readInt());
        builder.rhythmAttackSpeedPerStack(buffer.readFloat());
        builder.rhythmMoveSpeedPerStack(buffer.readFloat());
        builder.berserkDuration(buffer.readInt());
        builder.berserkCooldownReduction(buffer.readFloat());
        builder.berserkLifeSteal(buffer.readFloat());
        builder.flatDamageReduction(buffer.readFloat());
        builder.resistanceChance(buffer.readFloat());
        builder.resistanceDuration(buffer.readInt());
        builder.q1DamageMultiplier(buffer.readFloat());
        builder.q1MovingTargetDamageMultiplier(buffer.readFloat());
        builder.q1SlowDuration(buffer.readInt());
        builder.q1Cooldown(buffer.readInt());
        builder.q1Angle(buffer.readFloat());
        builder.q2MaxDistance(buffer.readFloat());
        builder.q2DamageMultiplier(buffer.readFloat());
        builder.q2KnockbackDuration(buffer.readFloat());
        builder.q2SplashDamagePercent(buffer.readFloat());
        builder.q2BonusAttackRange(buffer.readFloat());
        builder.q2BonusAttackDuration(buffer.readInt());
        builder.q2Cooldown(buffer.readInt());
        builder.q3Radius(buffer.readFloat());
        builder.q3DamageMultiplier(buffer.readFloat());
        builder.q3FearDuration(buffer.readFloat());
        builder.q3BerserkFearDuration(buffer.readFloat());
        builder.q3WeaknessDuration(buffer.readInt());
        builder.q3Cooldown(buffer.readInt());
        builder.rDuration(buffer.readInt());
        builder.rHealPercent(buffer.readFloat());
        builder.rHealthBonusPercent(buffer.readFloat());
        builder.rPowerLevel(buffer.readInt());
        builder.rSpeedLevel(buffer.readInt());
        builder.rFatigueDuration(buffer.readInt());
        builder.rCooldown(buffer.readInt());
        builder.isByteChen(buffer.readBoolean());
        builder.byteChenComputeMax(buffer.readInt());
        builder.byteChenComputeInitial(buffer.readInt());
        builder.byteChenComputeRegenRate(buffer.readInt());
        builder.byteChenComputeNodeRegenBonus(buffer.readFloat());
        builder.byteChenComputeOnRead(buffer.readInt());
        builder.byteChenComputeOnNodeTrigger(buffer.readInt());
        builder.byteChenComputeOnInterrupt(buffer.readInt());
        builder.byteChenComputeLowThreshold(buffer.readInt());
        builder.byteChenNodeMax(buffer.readInt());
        builder.byteChenNodeRange(buffer.readInt());
        builder.byteChenNodeDuration(buffer.readInt());
        builder.byteChenScoutNodeCost(buffer.readInt());
        builder.byteChenScoutNodeRadius(buffer.readFloat());
        builder.byteChenScoutNodeComputeRegen(buffer.readInt());
        builder.byteChenBuffNodeCost(buffer.readInt());
        builder.byteChenBuffNodeRadius(buffer.readFloat());
        builder.byteChenBuffNodeDamageBonus(buffer.readFloat());
        builder.byteChenBuffNodeDamageReduction(buffer.readFloat());
        builder.byteChenBuffNodeSpeedBonus(buffer.readFloat());
        builder.byteChenBuffNodeCooldownReduction(buffer.readFloat());
        builder.byteChenBuffNodeMaxStacks(buffer.readInt());
        builder.byteChenCounterNodeCost(buffer.readInt());
        builder.byteChenCounterNodeRadius(buffer.readFloat());
        builder.byteChenCounterNodeSilenceDuration(buffer.readInt());
        builder.byteChenCounterNodeDisorderDuration(buffer.readInt());
        builder.byteChenCounterNodeDamageReduction(buffer.readFloat());
        builder.byteChenCounterNodeCooldownMultiplier(buffer.readFloat());
        builder.byteChenDataVisionRange(buffer.readFloat());
        builder.byteChenLightweightSpeedBonus(buffer.readFloat());
        builder.byteChenLightweightDigSpeedBonus(buffer.readFloat());
        builder.byteChenLightweightMeleePenalty(buffer.readFloat());
        builder.byteChenLightweightReceivedMeleePenalty(buffer.readFloat());
        builder.byteChenNodeRecycleCooldown(buffer.readInt());
        builder.byteChenNodeRecycleRefund(buffer.readFloat());
        builder.byteChenFullReadCost(buffer.readInt());
        builder.byteChenFullReadCooldown(buffer.readInt());
        builder.byteChenFullReadRadius(buffer.readFloat());
        builder.byteChenFullReadDuration(buffer.readInt());
        builder.byteChenDataDispatchCost(buffer.readInt());
        builder.byteChenDataDispatchCooldown(buffer.readInt());
        builder.byteChenDataDispatchSpeedBonus(buffer.readFloat());
        builder.byteChenDataDispatchCooldownBonus(buffer.readFloat());
        builder.byteChenDataDispatchBuffDuration(buffer.readInt());
        builder.byteChenDataBanCost(buffer.readInt());
        builder.byteChenDataBanCooldown(buffer.readInt());
        builder.byteChenDataBanRange(buffer.readFloat());
        builder.byteChenDataBanDuration(buffer.readInt());
        builder.byteChenDataBanBossDuration(buffer.readInt());
        builder.byteChenDataBanTrueDamageBonus(buffer.readFloat());
        builder.byteChenUltimateMinCost(buffer.readInt());
        builder.byteChenUltimateCooldown(buffer.readInt());
        builder.byteChenUltimateRadius(buffer.readFloat());
        builder.byteChenUltimateDuration(buffer.readInt());
        builder.byteChenUltimateDamageBonus(buffer.readFloat());
        builder.byteChenUltimateDamageReduction(buffer.readFloat());
        builder.byteChenUltimateSpeedBonus(buffer.readFloat());
        builder.byteChenUltimateCooldownBonus(buffer.readFloat());
        builder.byteChenUltimateEnemyDamageReduction(buffer.readFloat());
        builder.byteChenUltimateEnemySpeedReduction(buffer.readFloat());
        builder.byteChenUltimateEnemyCooldownMultiplier(buffer.readFloat());
        builder.byteChenUltimateInterruptChance(buffer.readFloat());
        builder.byteChenExhaustDuration(buffer.readInt());
        builder.isHeavyKnight(buffer.readBoolean());
        builder.heavyKnightSpeedPenalty(buffer.readFloat());
        builder.heavyKnightWillOnHit(buffer.readInt());
        builder.heavyKnightWillOnBlock(buffer.readInt());
        builder.heavyKnightWillOnDamaged(buffer.readInt());
        builder.heavyKnightFullWillDamageBonus(buffer.readFloat());
        builder.heavyKnightFullWillDamageReduction(buffer.readFloat());
        builder.heavyKnightShieldWallTriggerTicks(buffer.readInt());
        builder.heavyKnightShieldWallReduction(buffer.readFloat());
        builder.heavyKnightShieldWallRadius(buffer.readFloat());
        builder.heavyKnightShieldWallArmorBonus(buffer.readFloat());
        builder.heavyKnightChargeWillCost(buffer.readInt());
        builder.heavyKnightChargeCooldown(buffer.readInt());
        builder.heavyKnightChargeDistance(buffer.readFloat());
        builder.heavyKnightChargeDamage(buffer.readFloat());
        builder.heavyKnightChargeKnockback(buffer.readFloat());
        builder.heavyKnightProtectRange(buffer.readInt());
        builder.heavyKnightProtectWillCost(buffer.readInt());
        builder.heavyKnightProtectCooldown(buffer.readInt());
        builder.heavyKnightShieldBashDamage(buffer.readFloat());
        builder.heavyKnightShieldBashStunDuration(buffer.readInt());
        builder.heavyKnightShieldBashCooldown(buffer.readInt());
        builder.isApostle(buffer.readBoolean());
        builder.apostleMeleeDamagePercent(buffer.readFloat());
        builder.apostleNetherDamageReduction(buffer.readFloat());
        builder.apostleTeleportCooldown(buffer.readInt());
        builder.apostleTeleportDistance(buffer.readFloat());
        builder.apostleFireballCooldown(buffer.readInt());
        builder.apostleFireballDamage(buffer.readFloat());
        builder.apostleDebuffDuration(buffer.readInt());
        builder.apostleDebuffDamageIncrease(buffer.readFloat());
        builder.apostleHealingReduction(buffer.readFloat());
        builder.apostleArrowFireRadius(buffer.readInt());
        builder.apostleArrowFireDuration(buffer.readFloat());
        return builder;
    }

    private static List<String> loadStringListFromNBT(CompoundTag tag, String key) {
        List<String> list = new ArrayList<>();
        if (tag.contains(key)) {
            ListTag poolList = tag.getList(key, 8);
            for (int i = 0; i < poolList.size(); i++) {
                list.add(poolList.getString(i));
            }
        }
        return list;
    }

    private static List<String> readStringListFromBuffer(FriendlyByteBuf buffer) {
        int size = buffer.readInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            list.add(buffer.readUtf());
        }
        return list;
    }

    String getId() { return id; }
    String getName() { return name; }
    String getDescription() { return description; }
    boolean isRequiresPassword() { return requiresPassword; }
    String getPassword() { return password; }
    String getIconItem() { return iconItem; }
    float getBonusDamagePercent() { return bonusDamagePercent; }
    int getSlownessLevel() { return slownessLevel; }
    int getWeaknessLevel() { return weaknessLevel; }
    float getBonusArmor() { return bonusArmor; }
    float getPoisonChance() { return poisonChance; }
    int getPoisonDuration() { return poisonDuration; }
    int getPoisonDamage() { return poisonDamage; }
    float getFireDamageMultiplier() { return fireDamageMultiplier; }
    String getResourceItem() { return resourceItem; }
    int getResourceInterval() { return resourceInterval; }
    int getResourceAmount() { return resourceAmount; }
    boolean hasEnderPearlAbility() { return hasEnderPearlAbility; }
    int getEnderPearlCooldown() { return enderPearlCooldown; }
    boolean hasWaterDamage() { return waterDamage; }
    float getWaterDamageAmount() { return waterDamageAmount; }
    int getWaterDamageInterval() { return waterDamageInterval; }
    boolean isFireTrailEnabled() { return fireTrailEnabled; }
    float getFireTrailDamage() { return fireTrailDamage; }
    int getFireTrailDuration() { return fireTrailDuration; }
    float getFireTrailRadius() { return fireTrailRadius; }
    float getFireDamageBonusPercent() { return fireDamageBonusPercent; }
    boolean hasFireImmunity() { return fireImmunity; }
    boolean hasWaterWeakness() { return waterWeakness; }
    float getWaterWeaknessDamagePercent() { return waterWeaknessDamagePercent; }
    int getWaterWeaknessInterval() { return waterWeaknessInterval; }
    boolean hasRainWeakness() { return rainWeakness; }
    boolean isFaceless() { return isFaceless; }
    int getSwitchInterval() { return switchInterval; }
    boolean canMountCreatures() { return canMountCreatures; }
    float getMountSpeedBonus() { return mountSpeedBonus; }
    float getMountDamageBonus() { return mountDamageBonus; }
    int getMountControlRange() { return mountControlRange; }
    float getMountHealthBonus() { return mountHealthBonus; }
    boolean hasGachaAbility() { return hasGachaAbility; }
    int getGachaInterval() { return gachaInterval; }
    List<String> getGachaEntityPool() { return gachaEntityPool; }
    boolean hasDiceAbility() { return hasDiceAbility; }
    int getDiceCooldown() { return diceCooldown; }
    List<String> getDiceSkillPool() { return diceSkillPool; }
    boolean hasLuckyCloverAbility() { return hasLuckyCloverAbility; }
    boolean hasDonkBowAbility() { return hasDonkBowAbility; }
    boolean hasGourmetAbility() { return hasGourmetAbility; }
    boolean isGourmet() { return isGourmet; }
    float getGourmetHealthBonus() { return gourmetHealthBonus; }
    float getGourmetDamageBonus() { return gourmetDamageBonus; }
    int getGourmetDailyLimit() { return gourmetDailyLimit; }
    boolean hasForgetterAbility() { return hasForgetterAbility; }
    int getForgetterInterval() { return forgetterInterval; }
    int getForgetterMinDuration() { return forgetterMinDuration; }
    int getForgetterMaxDuration() { return forgetterMaxDuration; }
    boolean isFacelessDeceiver() { return isFacelessDeceiver; }
    boolean isAngel() { return isAngel; }
    int getHealthRegenInterval() { return healthRegenInterval; }
    float getHealthRegenAmount() { return healthRegenAmount; }
    boolean hasHalo() { return hasHalo; }
    float getHaloDetectionRange() { return haloDetectionRange; }
    boolean isLeatherArmorOnly() { return leatherArmorOnly; }
    boolean isIronArmorOnly() { return ironArmorOnly; }
    float getBonusHealth() { return bonusHealth; }
    float getBonusArmorToughness() { return bonusArmorToughness; }
    float getMeleeDamageBonus() { return meleeDamageBonus; }
    float getRangedDamageReduction() { return rangedDamageReduction; }
    float getRangedDamagePenalty() { return rangedDamagePenalty; }
    boolean isDeathVenger() { return isDeathVenger; }
    boolean isHideNameTag() { return hideNameTag; }
    boolean hasMarkTargetAbility() { return hasMarkTargetAbility; }
    boolean isImpostor() { return isImpostor; }
    int getImpostorSkillCooldown() { return impostorSkillCooldown; }
    int getImpostorDisguiseDuration() { return impostorDisguiseDuration; }
    boolean hasAmbushAbility() { return hasAmbushAbility; }
    int getAmbushMaxTargets() { return ambushMaxTargets; }
    int getAmbushMaxDistance() { return ambushMaxDistance; }
    int getAmbushCooldown() { return ambushCooldown; }
    int getAmbushInvisDuration() { return ambushInvisDuration; }
    int getPassiveInvisSeconds() { return passiveInvisSeconds; }
    boolean isUndead() { return isUndead; }
    int getLifePoints() { return lifePoints; }
    int getShieldPoints() { return shieldPoints; }
    boolean hasSunlightVulnerability() { return sunlightVulnerability; }
    float getSunlightDamageAmount() { return sunlightDamageAmount; }
    int getSunlightDamageInterval() { return sunlightDamageInterval; }
    float getBonusAttackReach() { return bonusAttackReach; }
    boolean hasEndlessMinerAbility() { return hasEndlessMinerAbility; }
    int getFortuneLevel() { return fortuneLevel; }
    float getStoneDropChance() { return stoneDropChance; }
    boolean isHealer() { return isHealer; }
    float getHealerPassiveRadius() { return healerPassiveRadius; }
    float getHealerPassiveHealAmount() { return healerPassiveHealAmount; }
    float getHealerActiveHealAmount() { return healerActiveHealAmount; }
    int getHealerActiveCooldown() { return healerActiveCooldown; }
    boolean isFool() { return isFool; }
    float getFoolStealRange() { return foolStealRange; }
    int getFoolStealCooldown() { return foolStealCooldown; }
    boolean isHighPriest() { return isHighPriest; }
    float getHighPriestSacrificeRange() { return highPriestSacrificeRange; }
    int getHighPriestCooldown() { return highPriestCooldown; }
    float getHighPriestReviveHealth() { return highPriestReviveHealth; }
    boolean isGhostSenator() { return isGhostSenator; }
    float getGhostSenatorHealAmount() { return ghostSenatorHealAmount; }
    int getGhostSenatorStrengthDuration() { return ghostSenatorStrengthDuration; }
    float getGhostSenatorDetectionRadius() { return ghostSenatorDetectionRadius; }
    boolean isWraithCouncilor() { return isWraithCouncilor; }
    int getWraithSoulMax() { return wraithSoulMax; }
    int getWraithSoulInitial() { return wraithSoulInitial; }
    int getWraithSoulRegenRate() { return wraithSoulRegenRate; }
    int getWraithSoulDarkBonus() { return wraithSoulDarkBonus; }
    float getWraithSoulSunlightPenalty() { return wraithSoulSunlightPenalty; }
    int getWraithSoulKillBonus() { return wraithSoulKillBonus; }
    int getWraithSoulHitBonus() { return wraithSoulHitBonus; }
    int getWraithSoulSummonKillBonus() { return wraithSoulSummonKillBonus; }
    float getWraithErosionDamage() { return wraithErosionDamage; }
    float getWraithErosionArmorReduction() { return wraithErosionArmorReduction; }
    float getWraithErosionSlowPercent() { return wraithErosionSlowPercent; }
    int getWraithErosionMaxStacks() { return wraithErosionMaxStacks; }
    int getWraithErosionDuration() { return wraithErosionDuration; }
    int getWraithSummonCost() { return wraithSummonCost; }
    int getWraithSummonCooldown() { return wraithSummonCooldown; }
    int getWraithSummonCount() { return wraithSummonCount; }
    float getWraithSummonHealth() { return wraithSummonHealth; }
    float getWraithSummonDamage() { return wraithSummonDamage; }
    int getWraithSummonDuration() { return wraithSummonDuration; }
    float getWraithSummonCorpseRange() { return wraithSummonCorpseRange; }
    int getWraithSummonExtraMax() { return wraithSummonExtraMax; }
    int getWraithDomainCost() { return wraithDomainCost; }
    int getWraithDomainCooldown() { return wraithDomainCooldown; }
    float getWraithDomainRadius() { return wraithDomainRadius; }
    int getWraithDomainDuration() { return wraithDomainDuration; }
    float getWraithDomainDamage() { return wraithDomainDamage; }
    float getWraithDomainBossSlow() { return wraithDomainBossSlow; }
    int getWraithDomainCharmDuration() { return wraithDomainCharmDuration; }
    int getWraithBarrageBaseCost() { return wraithBarrageBaseCost; }
    int getWraithBarrageChargedCost() { return wraithBarrageChargedCost; }
    int getWraithBarrageCooldown() { return wraithBarrageCooldown; }
    int getWraithBarrageBaseOrbs() { return wraithBarrageBaseOrbs; }
    int getWraithBarrageChargedOrbs() { return wraithBarrageChargedOrbs; }
    float getWraithBarrageBaseDamage() { return wraithBarrageBaseDamage; }
    float getWraithBarrageChargedDamage() { return wraithBarrageChargedDamage; }
    float getWraithBarrageBaseRange() { return wraithBarrageBaseRange; }
    float getWraithBarrageChargedRange() { return wraithBarrageChargedRange; }
    int getWraithBarrageMaxChargeTime() { return wraithBarrageMaxChargeTime; }
    int getWraithBarrageMaxHits() { return wraithBarrageMaxHits; }
    int getWraithUltimateMinCost() { return wraithUltimateMinCost; }
    int getWraithUltimateCooldown() { return wraithUltimateCooldown; }
    int getWraithUltimateCloneCount() { return wraithUltimateCloneCount; }
    float getWraithUltimateCloneDamageRatio() { return wraithUltimateCloneDamageRatio; }
    int getWraithUltimateDuration() { return wraithUltimateDuration; }
    int getWraithUltimateSoulRegen() { return wraithUltimateSoulRegen; }
    float getWraithUltimateDamageIncrease() { return wraithUltimateDamageIncrease; }
    int getWraithUltimateExhaustDuration() { return wraithUltimateExhaustDuration; }
    boolean isEvilPoisoner() { return isEvilPoisoner; }
    int getPoisonerStrengthDuration() { return poisonerStrengthDuration; }
    boolean hasTurtleAura() { return hasTurtleAura; }
    float getTurtleAuraRadius() { return turtleAuraRadius; }
    int getTurtleAuraSlownessLevel() { return turtleAuraSlownessLevel; }
    int getTurtleAuraDuration() { return turtleAuraDuration; }
    boolean isJungleApeGod() { return isJungleApeGod; }
    int getRhythmStacksMax() { return rhythmStacksMax; }
    float getRhythmAttackSpeedPerStack() { return rhythmAttackSpeedPerStack; }
    float getRhythmMoveSpeedPerStack() { return rhythmMoveSpeedPerStack; }
    int getBerserkDuration() { return berserkDuration; }
    float getBerserkCooldownReduction() { return berserkCooldownReduction; }
    float getBerserkLifeSteal() { return berserkLifeSteal; }
    float getFlatDamageReduction() { return flatDamageReduction; }
    float getResistanceChance() { return resistanceChance; }
    int getResistanceDuration() { return resistanceDuration; }
    float getQ1DamageMultiplier() { return q1DamageMultiplier; }
    float getQ1MovingTargetDamageMultiplier() { return q1MovingTargetDamageMultiplier; }
    int getQ1SlowDuration() { return q1SlowDuration; }
    int getQ1Cooldown() { return q1Cooldown; }
    float getQ1Angle() { return q1Angle; }
    float getQ2MaxDistance() { return q2MaxDistance; }
    float getQ2DamageMultiplier() { return q2DamageMultiplier; }
    float getQ2KnockbackDuration() { return q2KnockbackDuration; }
    float getQ2SplashDamagePercent() { return q2SplashDamagePercent; }
    float getQ2BonusAttackRange() { return q2BonusAttackRange; }
    int getQ2BonusAttackDuration() { return q2BonusAttackDuration; }
    int getQ2Cooldown() { return q2Cooldown; }
    float getQ3Radius() { return q3Radius; }
    float getQ3DamageMultiplier() { return q3DamageMultiplier; }
    float getQ3FearDuration() { return q3FearDuration; }
    float getQ3BerserkFearDuration() { return q3BerserkFearDuration; }
    int getQ3WeaknessDuration() { return q3WeaknessDuration; }
    int getQ3Cooldown() { return q3Cooldown; }
    int getRDuration() { return rDuration; }
    float getRHealPercent() { return rHealPercent; }
    float getRHealthBonusPercent() { return rHealthBonusPercent; }
    int getRPowerLevel() { return rPowerLevel; }
    int getRSpeedLevel() { return rSpeedLevel; }
    int getRFatigueDuration() { return rFatigueDuration; }
    int getRCooldown() { return rCooldown; }
    boolean isByteChen() { return isByteChen; }
    int getByteChenComputeMax() { return byteChenComputeMax; }
    int getByteChenComputeInitial() { return byteChenComputeInitial; }
    int getByteChenComputeRegenRate() { return byteChenComputeRegenRate; }
    float getByteChenComputeNodeRegenBonus() { return byteChenComputeNodeRegenBonus; }
    int getByteChenComputeOnRead() { return byteChenComputeOnRead; }
    int getByteChenComputeOnNodeTrigger() { return byteChenComputeOnNodeTrigger; }
    int getByteChenComputeOnInterrupt() { return byteChenComputeOnInterrupt; }
    int getByteChenComputeLowThreshold() { return byteChenComputeLowThreshold; }
    int getByteChenNodeMax() { return byteChenNodeMax; }
    int getByteChenNodeRange() { return byteChenNodeRange; }
    int getByteChenNodeDuration() { return byteChenNodeDuration; }
    int getByteChenScoutNodeCost() { return byteChenScoutNodeCost; }
    float getByteChenScoutNodeRadius() { return byteChenScoutNodeRadius; }
    int getByteChenScoutNodeComputeRegen() { return byteChenScoutNodeComputeRegen; }
    int getByteChenBuffNodeCost() { return byteChenBuffNodeCost; }
    float getByteChenBuffNodeRadius() { return byteChenBuffNodeRadius; }
    float getByteChenBuffNodeDamageBonus() { return byteChenBuffNodeDamageBonus; }
    float getByteChenBuffNodeDamageReduction() { return byteChenBuffNodeDamageReduction; }
    float getByteChenBuffNodeSpeedBonus() { return byteChenBuffNodeSpeedBonus; }
    float getByteChenBuffNodeCooldownReduction() { return byteChenBuffNodeCooldownReduction; }
    int getByteChenBuffNodeMaxStacks() { return byteChenBuffNodeMaxStacks; }
    int getByteChenCounterNodeCost() { return byteChenCounterNodeCost; }
    float getByteChenCounterNodeRadius() { return byteChenCounterNodeRadius; }
    int getByteChenCounterNodeSilenceDuration() { return byteChenCounterNodeSilenceDuration; }
    int getByteChenCounterNodeDisorderDuration() { return byteChenCounterNodeDisorderDuration; }
    float getByteChenCounterNodeDamageReduction() { return byteChenCounterNodeDamageReduction; }
    float getByteChenCounterNodeCooldownMultiplier() { return byteChenCounterNodeCooldownMultiplier; }
    float getByteChenDataVisionRange() { return byteChenDataVisionRange; }
    float getByteChenLightweightSpeedBonus() { return byteChenLightweightSpeedBonus; }
    float getByteChenLightweightDigSpeedBonus() { return byteChenLightweightDigSpeedBonus; }
    float getByteChenLightweightMeleePenalty() { return byteChenLightweightMeleePenalty; }
    float getByteChenLightweightReceivedMeleePenalty() { return byteChenLightweightReceivedMeleePenalty; }
    int getByteChenNodeRecycleCooldown() { return byteChenNodeRecycleCooldown; }
    float getByteChenNodeRecycleRefund() { return byteChenNodeRecycleRefund; }
    int getByteChenFullReadCost() { return byteChenFullReadCost; }
    int getByteChenFullReadCooldown() { return byteChenFullReadCooldown; }
    float getByteChenFullReadRadius() { return byteChenFullReadRadius; }
    int getByteChenFullReadDuration() { return byteChenFullReadDuration; }
    int getByteChenDataDispatchCost() { return byteChenDataDispatchCost; }
    int getByteChenDataDispatchCooldown() { return byteChenDataDispatchCooldown; }
    float getByteChenDataDispatchSpeedBonus() { return byteChenDataDispatchSpeedBonus; }
    float getByteChenDataDispatchCooldownBonus() { return byteChenDataDispatchCooldownBonus; }
    int getByteChenDataDispatchBuffDuration() { return byteChenDataDispatchBuffDuration; }
    int getByteChenDataBanCost() { return byteChenDataBanCost; }
    int getByteChenDataBanCooldown() { return byteChenDataBanCooldown; }
    float getByteChenDataBanRange() { return byteChenDataBanRange; }
    int getByteChenDataBanDuration() { return byteChenDataBanDuration; }
    int getByteChenDataBanBossDuration() { return byteChenDataBanBossDuration; }
    float getByteChenDataBanTrueDamageBonus() { return byteChenDataBanTrueDamageBonus; }
    int getByteChenUltimateMinCost() { return byteChenUltimateMinCost; }
    int getByteChenUltimateCooldown() { return byteChenUltimateCooldown; }
    float getByteChenUltimateRadius() { return byteChenUltimateRadius; }
    int getByteChenUltimateDuration() { return byteChenUltimateDuration; }
    float getByteChenUltimateDamageBonus() { return byteChenUltimateDamageBonus; }
    float getByteChenUltimateDamageReduction() { return byteChenUltimateDamageReduction; }
    float getByteChenUltimateSpeedBonus() { return byteChenUltimateSpeedBonus; }
    float getByteChenUltimateCooldownBonus() { return byteChenUltimateCooldownBonus; }
    float getByteChenUltimateEnemyDamageReduction() { return byteChenUltimateEnemyDamageReduction; }
    float getByteChenUltimateEnemySpeedReduction() { return byteChenUltimateEnemySpeedReduction; }
    float getByteChenUltimateEnemyCooldownMultiplier() { return byteChenUltimateEnemyCooldownMultiplier; }
    float getByteChenUltimateInterruptChance() { return byteChenUltimateInterruptChance; }
    int getByteChenExhaustDuration() { return byteChenExhaustDuration; }
    boolean isHeavyKnight() { return isHeavyKnight; }
    float getHeavyKnightSpeedPenalty() { return heavyKnightSpeedPenalty; }
    int getHeavyKnightWillOnHit() { return heavyKnightWillOnHit; }
    int getHeavyKnightWillOnBlock() { return heavyKnightWillOnBlock; }
    int getHeavyKnightWillOnDamaged() { return heavyKnightWillOnDamaged; }
    float getHeavyKnightFullWillDamageBonus() { return heavyKnightFullWillDamageBonus; }
    float getHeavyKnightFullWillDamageReduction() { return heavyKnightFullWillDamageReduction; }
    int getHeavyKnightShieldWallTriggerTicks() { return heavyKnightShieldWallTriggerTicks; }
    float getHeavyKnightShieldWallReduction() { return heavyKnightShieldWallReduction; }
    float getHeavyKnightShieldWallRadius() { return heavyKnightShieldWallRadius; }
    float getHeavyKnightShieldWallArmorBonus() { return heavyKnightShieldWallArmorBonus; }
    int getHeavyKnightChargeWillCost() { return heavyKnightChargeWillCost; }
    int getHeavyKnightChargeCooldown() { return heavyKnightChargeCooldown; }
    float getHeavyKnightChargeDistance() { return heavyKnightChargeDistance; }
    float getHeavyKnightChargeDamage() { return heavyKnightChargeDamage; }
    float getHeavyKnightChargeKnockback() { return heavyKnightChargeKnockback; }
    int getHeavyKnightProtectRange() { return heavyKnightProtectRange; }
    int getHeavyKnightProtectWillCost() { return heavyKnightProtectWillCost; }
    int getHeavyKnightProtectCooldown() { return heavyKnightProtectCooldown; }
    float getHeavyKnightShieldBashDamage() { return heavyKnightShieldBashDamage; }
    int getHeavyKnightShieldBashStunDuration() { return heavyKnightShieldBashStunDuration; }
    int getHeavyKnightShieldBashCooldown() { return heavyKnightShieldBashCooldown; }

    boolean isApostle() { return isApostle; }
    float getApostleMeleeDamagePercent() { return apostleMeleeDamagePercent; }
    float getApostleNetherDamageReduction() { return apostleNetherDamageReduction; }
    int getApostleTeleportCooldown() { return apostleTeleportCooldown; }
    float getApostleTeleportDistance() { return apostleTeleportDistance; }
    int getApostleFireballCooldown() { return apostleFireballCooldown; }
    float getApostleFireballDamage() { return apostleFireballDamage; }
    int getApostleDebuffDuration() { return apostleDebuffDuration; }
    float getApostleDebuffDamageIncrease() { return apostleDebuffDamageIncrease; }
    float getApostleHealingReduction() { return apostleHealingReduction; }
    int getApostleArrowFireRadius() { return apostleArrowFireRadius; }
    float getApostleArrowFireDuration() { return apostleArrowFireDuration; }
}
