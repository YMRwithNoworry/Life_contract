package org.alku.life_contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public final class ModPoolConfig {
    public static final List<String> DEFAULT_POOL = List.of(
            "caerula_arbor", "spore", "phayriosis_two", "sculkhorde");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path PATH = FMLPaths.CONFIGDIR.get().resolve("life_contract_pool.json");
    private static ConfigData data = new ConfigData();

    static { load(); }
    private ModPoolConfig() {}

    public static synchronized void load() {
        if (Files.isRegularFile(PATH)) {
            try (Reader reader = Files.newBufferedReader(PATH)) {
                com.google.gson.JsonElement root = com.google.gson.JsonParser.parseReader(reader);
                if (root.isJsonArray()) {
                    data.modPool = GSON.fromJson(root, new com.google.gson.reflect.TypeToken<List<String>>(){}.getType());
                } else {
                    ConfigData loaded = GSON.fromJson(root, ConfigData.class);
                    if (loaded != null) data = loaded;
                }
            } catch (IOException | RuntimeException ignored) {}
        }
        if (data.modPool == null) data.modPool = new ArrayList<>(DEFAULT_POOL);
        data.modPool = new ArrayList<>(new LinkedHashSet<>(data.modPool));
        data.teamCount = Math.max(1, Math.min(32, data.teamCount));
        save();
    }

    public static synchronized void save() {
        try { Files.createDirectories(PATH.getParent()); try (Writer writer = Files.newBufferedWriter(PATH)) { GSON.toJson(data, writer); } }
        catch (IOException exception) { Life_contract.LOGGER.error("Unable to save infection mod pool", exception); }
    }

    public static synchronized List<String> getModPool() { return List.copyOf(data.modPool); }
    public static List<String> getLoadedModPool() { return getModPool().stream().filter(ModList.get()::isLoaded).toList(); }
    public static synchronized int getTeamCount() { return data.teamCount; }
    public static synchronized void setTeamCount(int count) { data.teamCount = Math.max(1, Math.min(32, count)); save(); }
    public static synchronized void setModEnabled(String modId, boolean enabled) { if (enabled) { if (!data.modPool.contains(modId)) data.modPool.add(modId); } else data.modPool.remove(modId); save(); }
    public static void addMod(String modId) { setModEnabled(modId, true); }
    public static void removeMod(String modId) { setModEnabled(modId, false); }
    public static synchronized void clear() { data.modPool.clear(); save(); }

    private static final class ConfigData {
        int teamCount = 4;
        List<String> modPool = new ArrayList<>(DEFAULT_POOL);
    }
}
