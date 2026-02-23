package org.alku.life_contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.server.MinecraftServer;
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
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            Path worldPath = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT);
            CONFIG_PATH = worldPath.resolve("life_contract_professions.json");
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
        return new Profession(
            json.has("id") ? json.get("id").getAsString() : "unknown",
            json.has("name") ? json.get("name").getAsString() : "Unknown",
            json.has("description") ? json.get("description").getAsString() : "",
            json.has("requiresPassword") && json.get("requiresPassword").getAsBoolean(),
            json.has("password") ? json.get("password").getAsString() : "",
            json.has("iconItem") ? json.get("iconItem").getAsString() : "minecraft:paper",
            json.has("bonusDamagePercent") ? json.get("bonusDamagePercent").getAsFloat() : 0.0f,
            json.has("slownessLevel") ? json.get("slownessLevel").getAsInt() : 0,
            json.has("weaknessLevel") ? json.get("weaknessLevel").getAsInt() : 0,
            json.has("bonusArmor") ? json.get("bonusArmor").getAsFloat() : 0.0f,
            json.has("poisonChance") ? json.get("poisonChance").getAsFloat() : 0.0f,
            json.has("poisonDuration") ? json.get("poisonDuration").getAsInt() : 0,
            json.has("poisonDamage") ? json.get("poisonDamage").getAsInt() : 0,
            json.has("fireDamageMultiplier") ? json.get("fireDamageMultiplier").getAsFloat() : 1.0f,
            json.has("resourceItem") ? json.get("resourceItem").getAsString() : "",
            json.has("resourceInterval") ? json.get("resourceInterval").getAsInt() : 0,
            json.has("resourceAmount") ? json.get("resourceAmount").getAsInt() : 0,
            json.has("hasEnderPearlAbility") && json.get("hasEnderPearlAbility").getAsBoolean(),
            json.has("enderPearlCooldown") ? json.get("enderPearlCooldown").getAsInt() : 0,
            json.has("waterDamage") && json.get("waterDamage").getAsBoolean(),
            json.has("waterDamageAmount") ? json.get("waterDamageAmount").getAsFloat() : 0.0f,
            json.has("waterDamageInterval") ? json.get("waterDamageInterval").getAsInt() : 0,
            json.has("fireTrailEnabled") && json.get("fireTrailEnabled").getAsBoolean(),
            json.has("fireTrailDamage") ? json.get("fireTrailDamage").getAsFloat() : 0.0f,
            json.has("fireTrailDuration") ? json.get("fireTrailDuration").getAsInt() : 0,
            json.has("fireTrailRadius") ? json.get("fireTrailRadius").getAsFloat() : 0.0f,
            json.has("fireDamageBonusPercent") ? json.get("fireDamageBonusPercent").getAsFloat() : 0.0f,
            json.has("fireImmunity") && json.get("fireImmunity").getAsBoolean(),
            json.has("waterWeakness") && json.get("waterWeakness").getAsBoolean(),
            json.has("waterWeaknessDamagePercent") ? json.get("waterWeaknessDamagePercent").getAsFloat() : 0.0f,
            json.has("waterWeaknessInterval") ? json.get("waterWeaknessInterval").getAsInt() : 0,
            json.has("rainWeakness") && json.get("rainWeakness").getAsBoolean(),
            json.has("isFaceless") && json.get("isFaceless").getAsBoolean(),
            json.has("switchInterval") ? json.get("switchInterval").getAsInt() : 0,
            json.has("canMountCreatures") && json.get("canMountCreatures").getAsBoolean(),
            json.has("mountSpeedBonus") ? json.get("mountSpeedBonus").getAsFloat() : 0.0f,
            json.has("mountDamageBonus") ? json.get("mountDamageBonus").getAsFloat() : 0.0f,
            json.has("mountControlRange") ? json.get("mountControlRange").getAsInt() : 32,
            json.has("mountHealthBonus") ? json.get("mountHealthBonus").getAsFloat() : 0.0f,
            json.has("hasGachaAbility") && json.get("hasGachaAbility").getAsBoolean(),
            json.has("gachaInterval") ? json.get("gachaInterval").getAsInt() : 300,
            loadStringList(json, "gachaEntityPool"),
            json.has("hasDiceAbility") && json.get("hasDiceAbility").getAsBoolean(),
            json.has("diceCooldown") ? json.get("diceCooldown").getAsInt() : 60,
            loadStringList(json, "diceSkillPool"),
            json.has("hasLuckyCloverAbility") && json.get("hasLuckyCloverAbility").getAsBoolean(),
            json.has("hasDonkBowAbility") && json.get("hasDonkBowAbility").getAsBoolean(),
            json.has("hasGourmetAbility") && json.get("hasGourmetAbility").getAsBoolean(),
            json.has("gourmetHealthBonus") ? json.get("gourmetHealthBonus").getAsFloat() : 4.0f,
            json.has("gourmetDamageBonus") ? json.get("gourmetDamageBonus").getAsFloat() : 0.5f,
            json.has("gourmetDailyLimit") ? json.get("gourmetDailyLimit").getAsInt() : 0,
            json.has("hasForgetterAbility") && json.get("hasForgetterAbility").getAsBoolean(),
            json.has("forgetterInterval") ? json.get("forgetterInterval").getAsInt() : 60,
            json.has("forgetterMinDuration") ? json.get("forgetterMinDuration").getAsInt() : 10,
            json.has("forgetterMaxDuration") ? json.get("forgetterMaxDuration").getAsInt() : 40,
            json.has("isFacelessDeceiver") && json.get("isFacelessDeceiver").getAsBoolean(),
            json.has("isAngel") && json.get("isAngel").getAsBoolean(),
            json.has("healthRegenInterval") ? json.get("healthRegenInterval").getAsInt() : 100,
            json.has("healthRegenAmount") ? json.get("healthRegenAmount").getAsFloat() : 1.0f,
            json.has("hasHalo") && json.get("hasHalo").getAsBoolean(),
            json.has("haloDetectionRange") ? json.get("haloDetectionRange").getAsFloat() : 32.0f,
            json.has("leatherArmorOnly") && json.get("leatherArmorOnly").getAsBoolean(),
            json.has("bonusHealth") ? json.get("bonusHealth").getAsFloat() : 0.0f,
            json.has("bonusArmorToughness") ? json.get("bonusArmorToughness").getAsFloat() : 0.0f,
            json.has("meleeDamageBonus") ? json.get("meleeDamageBonus").getAsFloat() : 0.0f,
            json.has("rangedDamageReduction") ? json.get("rangedDamageReduction").getAsFloat() : 0.0f,
            json.has("rangedDamagePenalty") ? json.get("rangedDamagePenalty").getAsFloat() : 0.0f,
            json.has("isDeathVenger") && json.get("isDeathVenger").getAsBoolean(),
            json.has("hideNameTag") && json.get("hideNameTag").getAsBoolean(),
            json.has("hasMarkTargetAbility") && json.get("hasMarkTargetAbility").getAsBoolean(),
            json.has("isImpostor") && json.get("isImpostor").getAsBoolean(),
            json.has("impostorSkillCooldown") ? json.get("impostorSkillCooldown").getAsInt() : 180,
            json.has("impostorDisguiseDuration") ? json.get("impostorDisguiseDuration").getAsInt() : 120,
            json.has("hasAmbushAbility") && json.get("hasAmbushAbility").getAsBoolean(),
            json.has("ambushMaxTargets") ? json.get("ambushMaxTargets").getAsInt() : 5,
            json.has("ambushMaxDistance") ? json.get("ambushMaxDistance").getAsInt() : 50,
            json.has("ambushCooldown") ? json.get("ambushCooldown").getAsInt() : 60,
            json.has("ambushInvisDuration") ? json.get("ambushInvisDuration").getAsInt() : 10,
            json.has("passiveInvisSeconds") ? json.get("passiveInvisSeconds").getAsInt() : 10,
            json.has("isUndead") && json.get("isUndead").getAsBoolean(),
            json.has("lifePoints") ? json.get("lifePoints").getAsInt() : 0,
            json.has("shieldPoints") ? json.get("shieldPoints").getAsInt() : 0,
            json.has("sunlightVulnerability") && json.get("sunlightVulnerability").getAsBoolean(),
            json.has("sunlightDamageAmount") ? json.get("sunlightDamageAmount").getAsFloat() : 1.0f,
            json.has("sunlightDamageInterval") ? json.get("sunlightDamageInterval").getAsInt() : 40,
            json.has("bonusAttackReach") ? json.get("bonusAttackReach").getAsFloat() : 0.0f,
            json.has("hasEndlessMinerAbility") && json.get("hasEndlessMinerAbility").getAsBoolean(),
            json.has("fortuneLevel") ? json.get("fortuneLevel").getAsInt() : 2,
            json.has("stoneDropChance") ? json.get("stoneDropChance").getAsFloat() : 0.05f,
            json.has("isHealer") && json.get("isHealer").getAsBoolean(),
            json.has("healerPassiveRadius") ? json.get("healerPassiveRadius").getAsFloat() : 5.0f,
            json.has("healerPassiveHealAmount") ? json.get("healerPassiveHealAmount").getAsFloat() : 2.0f,
            json.has("healerActiveHealAmount") ? json.get("healerActiveHealAmount").getAsFloat() : 8.0f,
            json.has("healerActiveCooldown") ? json.get("healerActiveCooldown").getAsInt() : 400,
            json.has("isFool") && json.get("isFool").getAsBoolean(),
            json.has("foolStealRange") ? json.get("foolStealRange").getAsFloat() : 30.0f,
            json.has("foolStealCooldown") ? json.get("foolStealCooldown").getAsInt() : 600,
            json.has("isHighPriest") && json.get("isHighPriest").getAsBoolean(),
            json.has("highPriestSacrificeRange") ? json.get("highPriestSacrificeRange").getAsFloat() : 30.0f,
            json.has("highPriestCooldown") ? json.get("highPriestCooldown").getAsInt() : 600,
            json.has("highPriestReviveHealth") ? json.get("highPriestReviveHealth").getAsFloat() : 10.0f
        );
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
        if (!profession.checkPassword(password)) return false;
        unlockedProfessions.computeIfAbsent(playerUuid, k -> new HashSet<>()).add(professionId);
        saveUnlocks();
        return true;
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
