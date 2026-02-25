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
}
