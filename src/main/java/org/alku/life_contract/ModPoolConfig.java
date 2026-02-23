package org.alku.life_contract;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ModPoolConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH = FMLPaths.CONFIGDIR.get().resolve("life_contract_pool.json");
    private static List<String> modPool = new ArrayList<>();

    static {
        load();
    }

    public static void load() {
        File file = CONFIG_PATH.toFile();
        if (file.exists()) {
            try (FileReader reader = new FileReader(file)) {
                modPool = GSON.fromJson(reader, new TypeToken<List<String>>() {
                }.getType());
                if (modPool == null)
                    modPool = new ArrayList<>();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            modPool = new ArrayList<>();
            save();
        }
    }

    public static void save() {
        try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
            GSON.toJson(modPool, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<String> getModPool() {
        return new ArrayList<>(modPool);
    }

    public static void addMod(String modId) {
        if (!modPool.contains(modId)) {
            modPool.add(modId);
            save();
        }
    }

    public static void removeMod(String modId) {
        if (modPool.remove(modId)) {
            save();
        }
    }

    public static void clear() {
        modPool.clear();
        save();
    }
}
