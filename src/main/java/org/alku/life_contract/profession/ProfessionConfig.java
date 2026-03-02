package org.alku.life_contract.profession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;

public class ProfessionConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static Path CONFIG_PATH;
    private static Path UNLOCK_PATH;
    private static Path LOCKS_PATH;
    private static List<Profession> professions = new ArrayList<>();
    private static Map<UUID, Set<String>> unlockedProfessions = new HashMap<>();
    private static Map<UUID, String> playerProfessions = new HashMap<>();
    private static Set<String> lockedProfessions = new HashSet<>();

    public static void load() {
        initPaths();
        loadProfessions();
        loadUnlocks();
        loadLocks();
    }

    private static void initPaths() {
        Path configDir = FMLPaths.CONFIGDIR.get();
        CONFIG_PATH = configDir.resolve("life_contract_professions.json");
        
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            UNLOCK_PATH = worldPath.resolve("life_contract_unlocks.json");
            LOCKS_PATH = worldPath.resolve("life_contract_locks.json");
        }
    }

    private static void loadProfessions() {
        if (CONFIG_PATH == null) {
            return;
        }
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null && json.has("professions")) {
                    JsonArray array = json.getAsJsonArray("professions");
                    professions.clear();
                    for (int i = 0; i < array.size(); i++) {
                        JsonObject profJson = array.get(i).getAsJsonObject();
                        Profession profession = loadProfessionFromJson(profJson);
                        professions.add(profession);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static Profession loadProfessionFromJson(JsonObject json) {
        ProfessionBuilder builder = Profession.builder()
            .id(json.has("id") ? json.get("id").getAsString() : "unknown")
            .name(json.has("name") ? json.get("name").getAsString() : "Unknown")
            .description(json.has("description") ? json.get("description").getAsString() : "")
            .requiresPassword(json.has("requiresPassword") && json.get("requiresPassword").getAsBoolean())
            .password(json.has("password") ? json.get("password").getAsString() : "")
            .iconItem(json.has("iconItem") ? json.get("iconItem").getAsString() : "minecraft:paper")
            .bonusDamagePercent(json.has("bonusDamagePercent") ? json.get("bonusDamagePercent").getAsFloat() : 0.0f)
            .slownessLevel(json.has("slownessLevel") ? json.get("slownessLevel").getAsInt() : 0)
            .weaknessLevel(json.has("weaknessLevel") ? json.get("weaknessLevel").getAsInt() : 0)
            .bonusArmor(json.has("bonusArmor") ? json.get("bonusArmor").getAsFloat() : 0.0f)
            .poisonChance(json.has("poisonChance") ? json.get("poisonChance").getAsFloat() : 0.0f)
            .poisonDuration(json.has("poisonDuration") ? json.get("poisonDuration").getAsInt() : 0)
            .poisonDamage(json.has("poisonDamage") ? json.get("poisonDamage").getAsInt() : 0)
            .fireDamageMultiplier(json.has("fireDamageMultiplier") ? json.get("fireDamageMultiplier").getAsFloat() : 1.0f)
            .resourceItem(json.has("resourceItem") ? json.get("resourceItem").getAsString() : "")
            .resourceInterval(json.has("resourceInterval") ? json.get("resourceInterval").getAsInt() : 0)
            .resourceAmount(json.has("resourceAmount") ? json.get("resourceAmount").getAsInt() : 0)
            .hasEnderPearlAbility(json.has("hasEnderPearlAbility") && json.get("hasEnderPearlAbility").getAsBoolean())
            .enderPearlCooldown(json.has("enderPearlCooldown") ? json.get("enderPearlCooldown").getAsInt() : 0)
            .waterDamage(json.has("waterDamage") && json.get("waterDamage").getAsBoolean())
            .waterDamageAmount(json.has("waterDamageAmount") ? json.get("waterDamageAmount").getAsFloat() : 0.0f)
            .waterDamageInterval(json.has("waterDamageInterval") ? json.get("waterDamageInterval").getAsInt() : 0)
            .fireTrailEnabled(json.has("fireTrailEnabled") && json.get("fireTrailEnabled").getAsBoolean())
            .fireTrailDamage(json.has("fireTrailDamage") ? json.get("fireTrailDamage").getAsFloat() : 0.0f)
            .fireTrailDuration(json.has("fireTrailDuration") ? json.get("fireTrailDuration").getAsInt() : 0)
            .fireTrailRadius(json.has("fireTrailRadius") ? json.get("fireTrailRadius").getAsFloat() : 0.0f)
            .fireDamageBonusPercent(json.has("fireDamageBonusPercent") ? json.get("fireDamageBonusPercent").getAsFloat() : 0.0f)
            .fireImmunity(json.has("fireImmunity") && json.get("fireImmunity").getAsBoolean())
            .waterWeakness(json.has("waterWeakness") && json.get("waterWeakness").getAsBoolean())
            .waterWeaknessDamagePercent(json.has("waterWeaknessDamagePercent") ? json.get("waterWeaknessDamagePercent").getAsFloat() : 0.0f)
            .waterWeaknessInterval(json.has("waterWeaknessInterval") ? json.get("waterWeaknessInterval").getAsInt() : 0)
            .rainWeakness(json.has("rainWeakness") && json.get("rainWeakness").getAsBoolean())
            .isFaceless(json.has("isFaceless") && json.get("isFaceless").getAsBoolean())
            .switchInterval(json.has("switchInterval") ? json.get("switchInterval").getAsInt() : 0)
            .canMountCreatures(json.has("canMountCreatures") && json.get("canMountCreatures").getAsBoolean())
            .mountSpeedBonus(json.has("mountSpeedBonus") ? json.get("mountSpeedBonus").getAsFloat() : 0.0f)
            .mountDamageBonus(json.has("mountDamageBonus") ? json.get("mountDamageBonus").getAsFloat() : 0.0f)
            .mountControlRange(json.has("mountControlRange") ? json.get("mountControlRange").getAsInt() : 32)
            .mountHealthBonus(json.has("mountHealthBonus") ? json.get("mountHealthBonus").getAsFloat() : 0.0f)
            .hasGachaAbility(json.has("hasGachaAbility") && json.get("hasGachaAbility").getAsBoolean())
            .gachaInterval(json.has("gachaInterval") ? json.get("gachaInterval").getAsInt() : 300)
            .gachaEntityPool(loadStringList(json, "gachaEntityPool"))
            .hasDiceAbility(json.has("hasDiceAbility") && json.get("hasDiceAbility").getAsBoolean())
            .diceCooldown(json.has("diceCooldown") ? json.get("diceCooldown").getAsInt() : 60)
            .diceSkillPool(loadStringList(json, "diceSkillPool"))
            .hasLuckyCloverAbility(json.has("hasLuckyCloverAbility") && json.get("hasLuckyCloverAbility").getAsBoolean())
            .hasDonkBowAbility(json.has("hasDonkBowAbility") && json.get("hasDonkBowAbility").getAsBoolean())
            .hasGourmetAbility(json.has("hasGourmetAbility") && json.get("hasGourmetAbility").getAsBoolean())
            .gourmetHealthBonus(json.has("gourmetHealthBonus") ? json.get("gourmetHealthBonus").getAsFloat() : 4.0f)
            .gourmetDamageBonus(json.has("gourmetDamageBonus") ? json.get("gourmetDamageBonus").getAsFloat() : 0.5f)
            .gourmetDailyLimit(json.has("gourmetDailyLimit") ? json.get("gourmetDailyLimit").getAsInt() : 0)
            .hasForgetterAbility(json.has("hasForgetterAbility") && json.get("hasForgetterAbility").getAsBoolean())
            .forgetterInterval(json.has("forgetterInterval") ? json.get("forgetterInterval").getAsInt() : 60)
            .forgetterMinDuration(json.has("forgetterMinDuration") ? json.get("forgetterMinDuration").getAsInt() : 10)
            .forgetterMaxDuration(json.has("forgetterMaxDuration") ? json.get("forgetterMaxDuration").getAsInt() : 40)
            .isFacelessDeceiver(json.has("isFacelessDeceiver") && json.get("isFacelessDeceiver").getAsBoolean())
            .isAngel(json.has("isAngel") && json.get("isAngel").getAsBoolean())
            .healthRegenInterval(json.has("healthRegenInterval") ? json.get("healthRegenInterval").getAsInt() : 100)
            .healthRegenAmount(json.has("healthRegenAmount") ? json.get("healthRegenAmount").getAsFloat() : 1.0f)
            .hasHalo(json.has("hasHalo") && json.get("hasHalo").getAsBoolean())
            .haloDetectionRange(json.has("haloDetectionRange") ? json.get("haloDetectionRange").getAsFloat() : 32.0f)
            .leatherArmorOnly(json.has("leatherArmorOnly") && json.get("leatherArmorOnly").getAsBoolean())
            .ironArmorOnly(json.has("ironArmorOnly") && json.get("ironArmorOnly").getAsBoolean())
            .bonusHealth(json.has("bonusHealth") ? json.get("bonusHealth").getAsFloat() : 0.0f)
            .bonusArmorToughness(json.has("bonusArmorToughness") ? json.get("bonusArmorToughness").getAsFloat() : 0.0f)
            .meleeDamageBonus(json.has("meleeDamageBonus") ? json.get("meleeDamageBonus").getAsFloat() : 0.0f)
            .rangedDamageReduction(json.has("rangedDamageReduction") ? json.get("rangedDamageReduction").getAsFloat() : 0.0f)
            .rangedDamagePenalty(json.has("rangedDamagePenalty") ? json.get("rangedDamagePenalty").getAsFloat() : 0.0f)
            .isDeathVenger(json.has("isDeathVenger") && json.get("isDeathVenger").getAsBoolean())
            .hideNameTag(json.has("hideNameTag") && json.get("hideNameTag").getAsBoolean())
            .hasMarkTargetAbility(json.has("hasMarkTargetAbility") && json.get("hasMarkTargetAbility").getAsBoolean())
            .isImpostor(json.has("isImpostor") && json.get("isImpostor").getAsBoolean())
            .impostorSkillCooldown(json.has("impostorSkillCooldown") ? json.get("impostorSkillCooldown").getAsInt() : 180)
            .impostorDisguiseDuration(json.has("impostorDisguiseDuration") ? json.get("impostorDisguiseDuration").getAsInt() : 120)
            .hasAmbushAbility(json.has("hasAmbushAbility") && json.get("hasAmbushAbility").getAsBoolean())
            .ambushMaxTargets(json.has("ambushMaxTargets") ? json.get("ambushMaxTargets").getAsInt() : 5)
            .ambushMaxDistance(json.has("ambushMaxDistance") ? json.get("ambushMaxDistance").getAsInt() : 50)
            .ambushCooldown(json.has("ambushCooldown") ? json.get("ambushCooldown").getAsInt() : 60)
            .ambushInvisDuration(json.has("ambushInvisDuration") ? json.get("ambushInvisDuration").getAsInt() : 10)
            .passiveInvisSeconds(json.has("passiveInvisSeconds") ? json.get("passiveInvisSeconds").getAsInt() : 10)
            .isUndead(json.has("isUndead") && json.get("isUndead").getAsBoolean())
            .lifePoints(json.has("lifePoints") ? json.get("lifePoints").getAsInt() : 0)
            .shieldPoints(json.has("shieldPoints") ? json.get("shieldPoints").getAsInt() : 0)
            .sunlightVulnerability(json.has("sunlightVulnerability") && json.get("sunlightVulnerability").getAsBoolean())
            .sunlightDamageAmount(json.has("sunlightDamageAmount") ? json.get("sunlightDamageAmount").getAsFloat() : 1.0f)
            .sunlightDamageInterval(json.has("sunlightDamageInterval") ? json.get("sunlightDamageInterval").getAsInt() : 40)
            .bonusAttackReach(json.has("bonusAttackReach") ? json.get("bonusAttackReach").getAsFloat() : 0.0f)
            .hasEndlessMinerAbility(json.has("hasEndlessMinerAbility") && json.get("hasEndlessMinerAbility").getAsBoolean())
            .fortuneLevel(json.has("fortuneLevel") ? json.get("fortuneLevel").getAsInt() : 2)
            .stoneDropChance(json.has("stoneDropChance") ? json.get("stoneDropChance").getAsFloat() : 0.05f)
            .isHealer(json.has("isHealer") && json.get("isHealer").getAsBoolean())
            .healerPassiveRadius(json.has("healerPassiveRadius") ? json.get("healerPassiveRadius").getAsFloat() : 5.0f)
            .healerPassiveHealAmount(json.has("healerPassiveHealAmount") ? json.get("healerPassiveHealAmount").getAsFloat() : 2.0f)
            .healerActiveHealAmount(json.has("healerActiveHealAmount") ? json.get("healerActiveHealAmount").getAsFloat() : 8.0f)
            .healerActiveCooldown(json.has("healerActiveCooldown") ? json.get("healerActiveCooldown").getAsInt() : 400)
            .isFool(json.has("isFool") && json.get("isFool").getAsBoolean())
            .foolStealRange(json.has("foolStealRange") ? json.get("foolStealRange").getAsFloat() : 30.0f)
            .foolStealCooldown(json.has("foolStealCooldown") ? json.get("foolStealCooldown").getAsInt() : 600)
            .isHighPriest(json.has("isHighPriest") && json.get("isHighPriest").getAsBoolean())
            .highPriestSacrificeRange(json.has("highPriestSacrificeRange") ? json.get("highPriestSacrificeRange").getAsFloat() : 30.0f)
            .highPriestCooldown(json.has("highPriestCooldown") ? json.get("highPriestCooldown").getAsInt() : 600)
            .highPriestReviveHealth(json.has("highPriestReviveHealth") ? json.get("highPriestReviveHealth").getAsFloat() : 10.0f)
            .isGhostSenator(json.has("isGhostSenator") && json.get("isGhostSenator").getAsBoolean())
            .ghostSenatorHealAmount(json.has("ghostSenatorHealAmount") ? json.get("ghostSenatorHealAmount").getAsFloat() : 7.0f)
            .ghostSenatorStrengthDuration(json.has("ghostSenatorStrengthDuration") ? json.get("ghostSenatorStrengthDuration").getAsInt() : 300)
            .ghostSenatorDetectionRadius(json.has("ghostSenatorDetectionRadius") ? json.get("ghostSenatorDetectionRadius").getAsFloat() : 20.0f)
            .isWraithCouncilor(json.has("isWraithCouncilor") && json.get("isWraithCouncilor").getAsBoolean())
            .wraithSoulMax(json.has("wraithSoulMax") ? json.get("wraithSoulMax").getAsInt() : 120)
            .wraithSoulInitial(json.has("wraithSoulInitial") ? json.get("wraithSoulInitial").getAsInt() : 60)
            .wraithSoulRegenRate(json.has("wraithSoulRegenRate") ? json.get("wraithSoulRegenRate").getAsInt() : 2)
            .wraithSoulDarkBonus(json.has("wraithSoulDarkBonus") ? json.get("wraithSoulDarkBonus").getAsInt() : 1)
            .wraithSoulSunlightPenalty(json.has("wraithSoulSunlightPenalty") ? json.get("wraithSoulSunlightPenalty").getAsFloat() : 0.5f)
            .wraithSoulKillBonus(json.has("wraithSoulKillBonus") ? json.get("wraithSoulKillBonus").getAsInt() : 15)
            .wraithSoulHitBonus(json.has("wraithSoulHitBonus") ? json.get("wraithSoulHitBonus").getAsInt() : 3)
            .wraithSoulSummonKillBonus(json.has("wraithSoulSummonKillBonus") ? json.get("wraithSoulSummonKillBonus").getAsInt() : 8)
            .wraithErosionDamage(json.has("wraithErosionDamage") ? json.get("wraithErosionDamage").getAsFloat() : 2.0f)
            .wraithErosionArmorReduction(json.has("wraithErosionArmorReduction") ? json.get("wraithErosionArmorReduction").getAsFloat() : 0.1f)
            .wraithErosionSlowPercent(json.has("wraithErosionSlowPercent") ? json.get("wraithErosionSlowPercent").getAsFloat() : 0.15f)
            .wraithErosionMaxStacks(json.has("wraithErosionMaxStacks") ? json.get("wraithErosionMaxStacks").getAsInt() : 5)
            .wraithErosionDuration(json.has("wraithErosionDuration") ? json.get("wraithErosionDuration").getAsInt() : 100)
            .wraithSummonCost(json.has("wraithSummonCost") ? json.get("wraithSummonCost").getAsInt() : 35)
            .wraithSummonCooldown(json.has("wraithSummonCooldown") ? json.get("wraithSummonCooldown").getAsInt() : 360)
            .wraithSummonCount(json.has("wraithSummonCount") ? json.get("wraithSummonCount").getAsInt() : 2)
            .wraithSummonHealth(json.has("wraithSummonHealth") ? json.get("wraithSummonHealth").getAsFloat() : 20.0f)
            .wraithSummonDamage(json.has("wraithSummonDamage") ? json.get("wraithSummonDamage").getAsFloat() : 6.0f)
            .wraithSummonDuration(json.has("wraithSummonDuration") ? json.get("wraithSummonDuration").getAsInt() : 240)
            .wraithSummonCorpseRange(json.has("wraithSummonCorpseRange") ? json.get("wraithSummonCorpseRange").getAsFloat() : 5.0f)
            .wraithSummonExtraMax(json.has("wraithSummonExtraMax") ? json.get("wraithSummonExtraMax").getAsInt() : 2)
            .wraithDomainCost(json.has("wraithDomainCost") ? json.get("wraithDomainCost").getAsInt() : 40)
            .wraithDomainCooldown(json.has("wraithDomainCooldown") ? json.get("wraithDomainCooldown").getAsInt() : 440)
            .wraithDomainRadius(json.has("wraithDomainRadius") ? json.get("wraithDomainRadius").getAsFloat() : 3.0f)
            .wraithDomainDuration(json.has("wraithDomainDuration") ? json.get("wraithDomainDuration").getAsInt() : 80)
            .wraithDomainDamage(json.has("wraithDomainDamage") ? json.get("wraithDomainDamage").getAsFloat() : 6.0f)
            .wraithDomainBossSlow(json.has("wraithDomainBossSlow") ? json.get("wraithDomainBossSlow").getAsFloat() : 0.5f)
            .wraithDomainCharmDuration(json.has("wraithDomainCharmDuration") ? json.get("wraithDomainCharmDuration").getAsInt() : 80)
            .wraithBarrageBaseCost(json.has("wraithBarrageBaseCost") ? json.get("wraithBarrageBaseCost").getAsInt() : 25)
            .wraithBarrageChargedCost(json.has("wraithBarrageChargedCost") ? json.get("wraithBarrageChargedCost").getAsInt() : 10)
            .wraithBarrageCooldown(json.has("wraithBarrageCooldown") ? json.get("wraithBarrageCooldown").getAsInt() : 200)
            .wraithBarrageBaseOrbs(json.has("wraithBarrageBaseOrbs") ? json.get("wraithBarrageBaseOrbs").getAsInt() : 6)
            .wraithBarrageChargedOrbs(json.has("wraithBarrageChargedOrbs") ? json.get("wraithBarrageChargedOrbs").getAsInt() : 12)
            .wraithBarrageBaseDamage(json.has("wraithBarrageBaseDamage") ? json.get("wraithBarrageBaseDamage").getAsFloat() : 4.0f)
            .wraithBarrageChargedDamage(json.has("wraithBarrageChargedDamage") ? json.get("wraithBarrageChargedDamage").getAsFloat() : 6.0f)
            .wraithBarrageBaseRange(json.has("wraithBarrageBaseRange") ? json.get("wraithBarrageBaseRange").getAsFloat() : 8.0f)
            .wraithBarrageChargedRange(json.has("wraithBarrageChargedRange") ? json.get("wraithBarrageChargedRange").getAsFloat() : 12.0f)
            .wraithBarrageMaxChargeTime(json.has("wraithBarrageMaxChargeTime") ? json.get("wraithBarrageMaxChargeTime").getAsInt() : 30)
            .wraithBarrageMaxHits(json.has("wraithBarrageMaxHits") ? json.get("wraithBarrageMaxHits").getAsInt() : 3)
            .wraithUltimateMinCost(json.has("wraithUltimateMinCost") ? json.get("wraithUltimateMinCost").getAsInt() : 100)
            .wraithUltimateCooldown(json.has("wraithUltimateCooldown") ? json.get("wraithUltimateCooldown").getAsInt() : 2400)
            .wraithUltimateCloneCount(json.has("wraithUltimateCloneCount") ? json.get("wraithUltimateCloneCount").getAsInt() : 3)
            .wraithUltimateCloneDamageRatio(json.has("wraithUltimateCloneDamageRatio") ? json.get("wraithUltimateCloneDamageRatio").getAsFloat() : 0.8f)
            .wraithUltimateDuration(json.has("wraithUltimateDuration") ? json.get("wraithUltimateDuration").getAsInt() : 300)
            .wraithUltimateSoulRegen(json.has("wraithUltimateSoulRegen") ? json.get("wraithUltimateSoulRegen").getAsInt() : 10)
            .wraithUltimateDamageIncrease(json.has("wraithUltimateDamageIncrease") ? json.get("wraithUltimateDamageIncrease").getAsFloat() : 0.2f)
            .wraithUltimateExhaustDuration(json.has("wraithUltimateExhaustDuration") ? json.get("wraithUltimateExhaustDuration").getAsInt() : 100)
            .hasTurtleAura(json.has("hasTurtleAura") && json.get("hasTurtleAura").getAsBoolean())
            .turtleAuraRadius(json.has("turtleAuraRadius") ? json.get("turtleAuraRadius").getAsFloat() : 5.0f)
            .turtleAuraSlownessLevel(json.has("turtleAuraSlownessLevel") ? json.get("turtleAuraSlownessLevel").getAsInt() : 1)
            .turtleAuraDuration(json.has("turtleAuraDuration") ? json.get("turtleAuraDuration").getAsInt() : 100)
            .isJungleApeGod(json.has("isJungleApeGod") && json.get("isJungleApeGod").getAsBoolean())
            .rhythmStacksMax(json.has("rhythmStacksMax") ? json.get("rhythmStacksMax").getAsInt() : 10)
            .rhythmAttackSpeedPerStack(json.has("rhythmAttackSpeedPerStack") ? json.get("rhythmAttackSpeedPerStack").getAsFloat() : 0.03f)
            .rhythmMoveSpeedPerStack(json.has("rhythmMoveSpeedPerStack") ? json.get("rhythmMoveSpeedPerStack").getAsFloat() : 0.02f)
            .berserkDuration(json.has("berserkDuration") ? json.get("berserkDuration").getAsInt() : 100)
            .berserkCooldownReduction(json.has("berserkCooldownReduction") ? json.get("berserkCooldownReduction").getAsFloat() : 0.3f)
            .berserkLifeSteal(json.has("berserkLifeSteal") ? json.get("berserkLifeSteal").getAsFloat() : 0.2f)
            .flatDamageReduction(json.has("flatDamageReduction") ? json.get("flatDamageReduction").getAsFloat() : 5.0f)
            .resistanceChance(json.has("resistanceChance") ? json.get("resistanceChance").getAsFloat() : 0.2f)
            .resistanceDuration(json.has("resistanceDuration") ? json.get("resistanceDuration").getAsInt() : 20)
            .q1DamageMultiplier(json.has("q1DamageMultiplier") ? json.get("q1DamageMultiplier").getAsFloat() : 2.0f)
            .q1MovingTargetDamageMultiplier(json.has("q1MovingTargetDamageMultiplier") ? json.get("q1MovingTargetDamageMultiplier").getAsFloat() : 2.8f)
            .q1SlowDuration(json.has("q1SlowDuration") ? json.get("q1SlowDuration").getAsInt() : 20)
            .q1Cooldown(json.has("q1Cooldown") ? json.get("q1Cooldown").getAsInt() : 200)
            .q1Angle(json.has("q1Angle") ? json.get("q1Angle").getAsFloat() : 90.0f)
            .q2MaxDistance(json.has("q2MaxDistance") ? json.get("q2MaxDistance").getAsFloat() : 25.0f)
            .q2DamageMultiplier(json.has("q2DamageMultiplier") ? json.get("q2DamageMultiplier").getAsFloat() : 1.5f)
            .q2KnockbackDuration(json.has("q2KnockbackDuration") ? json.get("q2KnockbackDuration").getAsFloat() : 10.0f)
            .q2SplashDamagePercent(json.has("q2SplashDamagePercent") ? json.get("q2SplashDamagePercent").getAsFloat() : 0.5f)
            .q2BonusAttackRange(json.has("q2BonusAttackRange") ? json.get("q2BonusAttackRange").getAsFloat() : 2.0f)
            .q2BonusAttackDuration(json.has("q2BonusAttackDuration") ? json.get("q2BonusAttackDuration").getAsInt() : 60)
            .q2Cooldown(json.has("q2Cooldown") ? json.get("q2Cooldown").getAsInt() : 200)
            .q3Radius(json.has("q3Radius") ? json.get("q3Radius").getAsFloat() : 6.0f)
            .q3DamageMultiplier(json.has("q3DamageMultiplier") ? json.get("q3DamageMultiplier").getAsFloat() : 1.0f)
            .q3FearDuration(json.has("q3FearDuration") ? json.get("q3FearDuration").getAsFloat() : 1.5f)
            .q3BerserkFearDuration(json.has("q3BerserkFearDuration") ? json.get("q3BerserkFearDuration").getAsFloat() : 2.5f)
            .q3WeaknessDuration(json.has("q3WeaknessDuration") ? json.get("q3WeaknessDuration").getAsInt() : 60)
            .q3Cooldown(json.has("q3Cooldown") ? json.get("q3Cooldown").getAsInt() : 400)
            .rDuration(json.has("rDuration") ? json.get("rDuration").getAsInt() : 200)
            .rHealPercent(json.has("rHealPercent") ? json.get("rHealPercent").getAsFloat() : 0.3f)
            .rHealthBonusPercent(json.has("rHealthBonusPercent") ? json.get("rHealthBonusPercent").getAsFloat() : 0.5f)
            .rPowerLevel(json.has("rPowerLevel") ? json.get("rPowerLevel").getAsInt() : 4)
            .rSpeedLevel(json.has("rSpeedLevel") ? json.get("rSpeedLevel").getAsInt() : 3)
            .rFatigueDuration(json.has("rFatigueDuration") ? json.get("rFatigueDuration").getAsInt() : 40)
            .rCooldown(json.has("rCooldown") ? json.get("rCooldown").getAsInt() : 1200)
            .isEvilPoisoner(json.has("isEvilPoisoner") && json.get("isEvilPoisoner").getAsBoolean())
            .poisonerStrengthDuration(json.has("poisonerStrengthDuration") ? json.get("poisonerStrengthDuration").getAsInt() : 200)
            .isHeavyKnight(json.has("isHeavyKnight") && json.get("isHeavyKnight").getAsBoolean())
            .heavyKnightSpeedPenalty(json.has("heavyKnightSpeedPenalty") ? json.get("heavyKnightSpeedPenalty").getAsFloat() : 0.2f)
            .heavyKnightWillOnHit(json.has("heavyKnightWillOnHit") ? json.get("heavyKnightWillOnHit").getAsInt() : 5)
            .heavyKnightWillOnBlock(json.has("heavyKnightWillOnBlock") ? json.get("heavyKnightWillOnBlock").getAsInt() : 6)
            .heavyKnightWillOnDamaged(json.has("heavyKnightWillOnDamaged") ? json.get("heavyKnightWillOnDamaged").getAsInt() : 8)
            .heavyKnightFullWillDamageBonus(json.has("heavyKnightFullWillDamageBonus") ? json.get("heavyKnightFullWillDamageBonus").getAsFloat() : 0.1f)
            .heavyKnightFullWillDamageReduction(json.has("heavyKnightFullWillDamageReduction") ? json.get("heavyKnightFullWillDamageReduction").getAsFloat() : 0.1f)
            .heavyKnightShieldWallTriggerTicks(json.has("heavyKnightShieldWallTriggerTicks") ? json.get("heavyKnightShieldWallTriggerTicks").getAsInt() : 40)
            .heavyKnightShieldWallReduction(json.has("heavyKnightShieldWallReduction") ? json.get("heavyKnightShieldWallReduction").getAsFloat() : 0.2f)
            .heavyKnightShieldWallRadius(json.has("heavyKnightShieldWallRadius") ? json.get("heavyKnightShieldWallRadius").getAsFloat() : 8.0f)
            .heavyKnightShieldWallArmorBonus(json.has("heavyKnightShieldWallArmorBonus") ? json.get("heavyKnightShieldWallArmorBonus").getAsFloat() : 5.0f)
            .heavyKnightChargeWillCost(json.has("heavyKnightChargeWillCost") ? json.get("heavyKnightChargeWillCost").getAsInt() : 30)
            .heavyKnightChargeCooldown(json.has("heavyKnightChargeCooldown") ? json.get("heavyKnightChargeCooldown").getAsInt() : 240)
            .heavyKnightChargeDistance(json.has("heavyKnightChargeDistance") ? json.get("heavyKnightChargeDistance").getAsFloat() : 5.0f)
            .heavyKnightChargeDamage(json.has("heavyKnightChargeDamage") ? json.get("heavyKnightChargeDamage").getAsFloat() : 12.0f)
            .heavyKnightChargeKnockback(json.has("heavyKnightChargeKnockback") ? json.get("heavyKnightChargeKnockback").getAsFloat() : 3.0f)
            .heavyKnightProtectRange(json.has("heavyKnightProtectRange") ? json.get("heavyKnightProtectRange").getAsInt() : 6)
            .heavyKnightProtectWillCost(json.has("heavyKnightProtectWillCost") ? json.get("heavyKnightProtectWillCost").getAsInt() : 20)
            .heavyKnightProtectCooldown(json.has("heavyKnightProtectCooldown") ? json.get("heavyKnightProtectCooldown").getAsInt() : 400)
            .heavyKnightShieldBashDamage(json.has("heavyKnightShieldBashDamage") ? json.get("heavyKnightShieldBashDamage").getAsFloat() : 4.0f)
            .heavyKnightShieldBashStunDuration(json.has("heavyKnightShieldBashStunDuration") ? json.get("heavyKnightShieldBashStunDuration").getAsInt() : 30)
            .heavyKnightShieldBashCooldown(json.has("heavyKnightShieldBashCooldown") ? json.get("heavyKnightShieldBashCooldown").getAsInt() : 80)
            .isByteChen(json.has("isByteChen") && json.get("isByteChen").getAsBoolean())
            .byteChenComputeMax(json.has("byteChenComputeMax") ? json.get("byteChenComputeMax").getAsInt() : 150)
            .byteChenComputeInitial(json.has("byteChenComputeInitial") ? json.get("byteChenComputeInitial").getAsInt() : 150)
            .byteChenComputeRegenRate(json.has("byteChenComputeRegenRate") ? json.get("byteChenComputeRegenRate").getAsInt() : 3)
            .byteChenComputeNodeRegenBonus(json.has("byteChenComputeNodeRegenBonus") ? json.get("byteChenComputeNodeRegenBonus").getAsFloat() : 0.5f)
            .byteChenComputeOnRead(json.has("byteChenComputeOnRead") ? json.get("byteChenComputeOnRead").getAsInt() : 10)
            .byteChenComputeOnNodeTrigger(json.has("byteChenComputeOnNodeTrigger") ? json.get("byteChenComputeOnNodeTrigger").getAsInt() : 8)
            .byteChenComputeOnInterrupt(json.has("byteChenComputeOnInterrupt") ? json.get("byteChenComputeOnInterrupt").getAsInt() : 25)
            .byteChenComputeLowThreshold(json.has("byteChenComputeLowThreshold") ? json.get("byteChenComputeLowThreshold").getAsInt() : 20)
            .byteChenNodeMax(json.has("byteChenNodeMax") ? json.get("byteChenNodeMax").getAsInt() : 8)
            .byteChenNodeRange(json.has("byteChenNodeRange") ? json.get("byteChenNodeRange").getAsInt() : 32)
            .byteChenNodeDuration(json.has("byteChenNodeDuration") ? json.get("byteChenNodeDuration").getAsInt() : 1200)
            .byteChenScoutNodeCost(json.has("byteChenScoutNodeCost") ? json.get("byteChenScoutNodeCost").getAsInt() : 20)
            .byteChenScoutNodeRadius(json.has("byteChenScoutNodeRadius") ? json.get("byteChenScoutNodeRadius").getAsFloat() : 12.0f)
            .byteChenScoutNodeComputeRegen(json.has("byteChenScoutNodeComputeRegen") ? json.get("byteChenScoutNodeComputeRegen").getAsInt() : 1)
            .byteChenBuffNodeCost(json.has("byteChenBuffNodeCost") ? json.get("byteChenBuffNodeCost").getAsInt() : 25)
            .byteChenBuffNodeRadius(json.has("byteChenBuffNodeRadius") ? json.get("byteChenBuffNodeRadius").getAsFloat() : 8.0f)
            .byteChenBuffNodeDamageBonus(json.has("byteChenBuffNodeDamageBonus") ? json.get("byteChenBuffNodeDamageBonus").getAsFloat() : 0.1f)
            .byteChenBuffNodeDamageReduction(json.has("byteChenBuffNodeDamageReduction") ? json.get("byteChenBuffNodeDamageReduction").getAsFloat() : 0.1f)
            .byteChenBuffNodeSpeedBonus(json.has("byteChenBuffNodeSpeedBonus") ? json.get("byteChenBuffNodeSpeedBonus").getAsFloat() : 0.15f)
            .byteChenBuffNodeCooldownReduction(json.has("byteChenBuffNodeCooldownReduction") ? json.get("byteChenBuffNodeCooldownReduction").getAsFloat() : 0.1f)
            .byteChenBuffNodeMaxStacks(json.has("byteChenBuffNodeMaxStacks") ? json.get("byteChenBuffNodeMaxStacks").getAsInt() : 3)
            .byteChenCounterNodeCost(json.has("byteChenCounterNodeCost") ? json.get("byteChenCounterNodeCost").getAsInt() : 30)
            .byteChenCounterNodeRadius(json.has("byteChenCounterNodeRadius") ? json.get("byteChenCounterNodeRadius").getAsFloat() : 6.0f)
            .byteChenCounterNodeSilenceDuration(json.has("byteChenCounterNodeSilenceDuration") ? json.get("byteChenCounterNodeSilenceDuration").getAsInt() : 40)
            .byteChenCounterNodeDisorderDuration(json.has("byteChenCounterNodeDisorderDuration") ? json.get("byteChenCounterNodeDisorderDuration").getAsInt() : 100)
            .byteChenCounterNodeDamageReduction(json.has("byteChenCounterNodeDamageReduction") ? json.get("byteChenCounterNodeDamageReduction").getAsFloat() : 0.15f)
            .byteChenCounterNodeCooldownMultiplier(json.has("byteChenCounterNodeCooldownMultiplier") ? json.get("byteChenCounterNodeCooldownMultiplier").getAsFloat() : 2.0f)
            .byteChenDataVisionRange(json.has("byteChenDataVisionRange") ? json.get("byteChenDataVisionRange").getAsFloat() : 16.0f)
            .byteChenLightweightSpeedBonus(json.has("byteChenLightweightSpeedBonus") ? json.get("byteChenLightweightSpeedBonus").getAsFloat() : 0.15f)
            .byteChenLightweightDigSpeedBonus(json.has("byteChenLightweightDigSpeedBonus") ? json.get("byteChenLightweightDigSpeedBonus").getAsFloat() : 0.2f)
            .byteChenLightweightMeleePenalty(json.has("byteChenLightweightMeleePenalty") ? json.get("byteChenLightweightMeleePenalty").getAsFloat() : -0.45f)
            .byteChenLightweightReceivedMeleePenalty(json.has("byteChenLightweightReceivedMeleePenalty") ? json.get("byteChenLightweightReceivedMeleePenalty").getAsFloat() : 0.2f)
            .byteChenNodeRecycleCooldown(json.has("byteChenNodeRecycleCooldown") ? json.get("byteChenNodeRecycleCooldown").getAsInt() : 200)
            .byteChenNodeRecycleRefund(json.has("byteChenNodeRecycleRefund") ? json.get("byteChenNodeRecycleRefund").getAsFloat() : 0.8f)
            .byteChenFullReadCost(json.has("byteChenFullReadCost") ? json.get("byteChenFullReadCost").getAsInt() : 30)
            .byteChenFullReadCooldown(json.has("byteChenFullReadCooldown") ? json.get("byteChenFullReadCooldown").getAsInt() : 300)
            .byteChenFullReadRadius(json.has("byteChenFullReadRadius") ? json.get("byteChenFullReadRadius").getAsFloat() : 32.0f)
            .byteChenFullReadDuration(json.has("byteChenFullReadDuration") ? json.get("byteChenFullReadDuration").getAsInt() : 160)
            .byteChenDataDispatchCost(json.has("byteChenDataDispatchCost") ? json.get("byteChenDataDispatchCost").getAsInt() : 40)
            .byteChenDataDispatchCooldown(json.has("byteChenDataDispatchCooldown") ? json.get("byteChenDataDispatchCooldown").getAsInt() : 400)
            .byteChenDataDispatchSpeedBonus(json.has("byteChenDataDispatchSpeedBonus") ? json.get("byteChenDataDispatchSpeedBonus").getAsFloat() : 0.2f)
            .byteChenDataDispatchCooldownBonus(json.has("byteChenDataDispatchCooldownBonus") ? json.get("byteChenDataDispatchCooldownBonus").getAsFloat() : 0.2f)
            .byteChenDataDispatchBuffDuration(json.has("byteChenDataDispatchBuffDuration") ? json.get("byteChenDataDispatchBuffDuration").getAsInt() : 160)
            .byteChenDataBanCost(json.has("byteChenDataBanCost") ? json.get("byteChenDataBanCost").getAsInt() : 35)
            .byteChenDataBanCooldown(json.has("byteChenDataBanCooldown") ? json.get("byteChenDataBanCooldown").getAsInt() : 360)
            .byteChenDataBanRange(json.has("byteChenDataBanRange") ? json.get("byteChenDataBanRange").getAsFloat() : 12.0f)
            .byteChenDataBanDuration(json.has("byteChenDataBanDuration") ? json.get("byteChenDataBanDuration").getAsInt() : 80)
            .byteChenDataBanBossDuration(json.has("byteChenDataBanBossDuration") ? json.get("byteChenDataBanBossDuration").getAsInt() : 40)
            .byteChenDataBanTrueDamageBonus(json.has("byteChenDataBanTrueDamageBonus") ? json.get("byteChenDataBanTrueDamageBonus").getAsFloat() : 0.1f)
            .byteChenUltimateMinCost(json.has("byteChenUltimateMinCost") ? json.get("byteChenUltimateMinCost").getAsInt() : 120)
            .byteChenUltimateCooldown(json.has("byteChenUltimateCooldown") ? json.get("byteChenUltimateCooldown").getAsInt() : 2400)
            .byteChenUltimateRadius(json.has("byteChenUltimateRadius") ? json.get("byteChenUltimateRadius").getAsFloat() : 64.0f)
            .byteChenUltimateDuration(json.has("byteChenUltimateDuration") ? json.get("byteChenUltimateDuration").getAsInt() : 240)
            .byteChenUltimateDamageBonus(json.has("byteChenUltimateDamageBonus") ? json.get("byteChenUltimateDamageBonus").getAsFloat() : 0.25f)
            .byteChenUltimateDamageReduction(json.has("byteChenUltimateDamageReduction") ? json.get("byteChenUltimateDamageReduction").getAsFloat() : 0.25f)
            .byteChenUltimateSpeedBonus(json.has("byteChenUltimateSpeedBonus") ? json.get("byteChenUltimateSpeedBonus").getAsFloat() : 0.3f)
            .byteChenUltimateCooldownBonus(json.has("byteChenUltimateCooldownBonus") ? json.get("byteChenUltimateCooldownBonus").getAsFloat() : 0.3f)
            .byteChenUltimateEnemyDamageReduction(json.has("byteChenUltimateEnemyDamageReduction") ? json.get("byteChenUltimateEnemyDamageReduction").getAsFloat() : 0.3f)
            .byteChenUltimateEnemySpeedReduction(json.has("byteChenUltimateEnemySpeedReduction") ? json.get("byteChenUltimateEnemySpeedReduction").getAsFloat() : 0.25f)
            .byteChenUltimateEnemyCooldownMultiplier(json.has("byteChenUltimateEnemyCooldownMultiplier") ? json.get("byteChenUltimateEnemyCooldownMultiplier").getAsFloat() : 2.0f)
            .byteChenUltimateInterruptChance(json.has("byteChenUltimateInterruptChance") ? json.get("byteChenUltimateInterruptChance").getAsFloat() : 0.5f)
            .byteChenExhaustDuration(json.has("byteChenExhaustDuration") ? json.get("byteChenExhaustDuration").getAsInt() : 160);
        
        return builder.build();
    }

    private static List<String> loadStringList(JsonObject json, String key) {
        List<String> list = new ArrayList<>();
        if (json.has(key)) {
            JsonArray array = json.getAsJsonArray(key);
            for (int i = 0; i < array.size(); i++) {
                list.add(array.get(i).getAsString());
            }
        }
        return list;
    }

    private static void loadUnlocks() {
        if (UNLOCK_PATH == null) return;
        File file = UNLOCK_PATH.toFile();
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null) {
                    unlockedProfessions.clear();
                    playerProfessions.clear();
                    for (String uuidStr : json.keySet()) {
                        if (json.get(uuidStr).isJsonObject()) {
                            JsonObject playerData = json.getAsJsonObject(uuidStr);
                            UUID uuid = UUID.fromString(uuidStr);
                            Set<String> unlocked = new HashSet<>();
                            if (playerData.has("unlocked")) {
                                for (var elem : playerData.getAsJsonArray("unlocked")) {
                                    unlocked.add(elem.getAsString());
                                }
                            }
                            unlockedProfessions.put(uuid, unlocked);
                            if (playerData.has("current")) {
                                playerProfessions.put(uuid, playerData.get("current").getAsString());
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveUnlocks() {
        if (UNLOCK_PATH == null) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(UNLOCK_PATH.toFile()), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonObject();
            for (Map.Entry<UUID, Set<String>> entry : unlockedProfessions.entrySet()) {
                JsonObject playerData = new JsonObject();
                JsonArray unlocked = new JsonArray();
                for (String prof : entry.getValue()) {
                    unlocked.add(prof);
                }
                playerData.add("unlocked", unlocked);
                String current = playerProfessions.get(entry.getKey());
                if (current != null) {
                    playerData.addProperty("current", current);
                }
                json.add(entry.getKey().toString(), playerData);
            }
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadLocks() {
        if (LOCKS_PATH == null) return;
        File file = LOCKS_PATH.toFile();
        if (file.exists()) {
            try (InputStreamReader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(reader, JsonObject.class);
                if (json != null && json.has("locked")) {
                    lockedProfessions.clear();
                    for (var elem : json.getAsJsonArray("locked")) {
                        lockedProfessions.add(elem.getAsString());
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void saveLocks() {
        if (LOCKS_PATH == null) return;
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(LOCKS_PATH.toFile()), StandardCharsets.UTF_8)) {
            JsonObject json = new JsonObject();
            JsonArray locked = new JsonArray();
            for (String prof : lockedProfessions) {
                locked.add(prof);
            }
            json.add("locked", locked);
            GSON.toJson(json, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Profession> getProfessions() {
        return new ArrayList<>(professions);
    }

    public static Profession getProfession(String id) {
        for (Profession profession : professions) {
            if (profession.getId().equals(id)) {
                return profession;
            }
        }
        return null;
    }

    public static boolean isProfessionLocked(String id) {
        return lockedProfessions.contains(id);
    }

    public static boolean isProfessionUnlocked(UUID playerUuid, String professionId) {
        Set<String> unlocked = unlockedProfessions.get(playerUuid);
        return unlocked != null && unlocked.contains(professionId);
    }

    public static boolean unlockProfession(UUID playerUuid, String professionId, String password) {
        Profession profession = getProfession(professionId);
        if (profession == null) return false;
        if (isProfessionLocked(professionId)) return false;
        if (!checkPassword(profession, password)) return false;
        unlockedProfessions.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(professionId);
        saveUnlocks();
        return true;
    }

    private static boolean checkPassword(Profession profession, String password) {
        if (!profession.requiresPassword()) return true;
        return profession.getPassword().equals(password);
    }

    public static void setPlayerProfession(UUID playerUuid, String professionId) {
        playerProfessions.put(playerUuid, professionId);
        saveUnlocks();
    }

    public static String getPlayerProfession(UUID playerUuid) {
        return playerProfessions.get(playerUuid);
    }

    public static void lockProfession(String professionId) {
        lockedProfessions.add(professionId);
        saveLocks();
    }

    public static void unlockProfessionForAdmin(String professionId) {
        lockedProfessions.remove(professionId);
        saveLocks();
    }

    public static Set<String> getLockedProfessions() {
        return new HashSet<>(lockedProfessions);
    }

    public static Set<String> getUnlockedProfessionsForPlayer(UUID playerUuid) {
        Set<String> unlocked = unlockedProfessions.get(playerUuid);
        return unlocked != null ? new HashSet<>(unlocked) : new HashSet<>();
    }

    public static Set<String> getPlayerUnlockedProfessions(UUID playerUuid) {
        return getUnlockedProfessionsForPlayer(playerUuid);
    }

    public static void resetUnlocks() {
        unlockedProfessions.clear();
        playerProfessions.clear();
        saveUnlocks();
    }

    public static void unlockProfessionGlobal(String professionId) {
        for (UUID uuid : unlockedProfessions.keySet()) {
            unlockedProfessions.get(uuid).add(professionId);
        }
        saveUnlocks();
    }
}
