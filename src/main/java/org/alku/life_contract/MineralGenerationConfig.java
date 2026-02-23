package org.alku.life_contract;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MineralGenerationConfig {

    private static final String CONFIG_FILE_NAME = "mineral_generation.cfg";
    private static final LevelResource CONFIG_RESOURCE = new LevelResource("data");

    private static boolean globalGenerationEnabled = true;
    private static boolean clientSideEnabled = true;
    private static MinecraftServer currentServer;

    public static boolean isGlobalGenerationEnabled() {
        return globalGenerationEnabled;
    }

    public static boolean isClientSideEnabled() {
        return clientSideEnabled;
    }

    public static void setClientSideEnabled(boolean enabled) {
        clientSideEnabled = enabled;
    }

    public static void setGlobalGenerationEnabled(boolean enabled) {
        globalGenerationEnabled = enabled;
        saveConfig();
        syncToAllClients();
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        currentServer = event.getServer();
        loadConfig();
    }

    @SubscribeEvent
    public static void onServerStopping(ServerStoppingEvent event) {
        saveConfig();
        currentServer = null;
    }

    @SubscribeEvent
    public static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            syncToClient(player);
        }
    }

    private static void loadConfig() {
        if (currentServer == null) {
            return;
        }

        File configFile = getConfigFile();
        if (configFile == null || !configFile.exists()) {
            globalGenerationEnabled = true;
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("enabled=")) {
                globalGenerationEnabled = Boolean.parseBoolean(line.substring(8));
            }
        } catch (IOException e) {
            globalGenerationEnabled = true;
        }
    }

    private static void saveConfig() {
        if (currentServer == null) {
            return;
        }

        File configFile = getConfigFile();
        if (configFile == null) {
            return;
        }

        try {
            configFile.getParentFile().mkdirs();
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(configFile))) {
                writer.write("enabled=" + globalGenerationEnabled);
            }
        } catch (IOException e) {
        }
    }

    private static File getConfigFile() {
        if (currentServer == null) {
            return null;
        }
        File dataDir = currentServer.getWorldPath(CONFIG_RESOURCE).toFile();
        return new File(dataDir, CONFIG_FILE_NAME);
    }

    private static void syncToAllClients() {
        if (currentServer != null) {
            NetworkHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), 
                    new PacketSyncMineralGenerationState(globalGenerationEnabled));
        }
    }

    private static void syncToClient(ServerPlayer player) {
        NetworkHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player),
                new PacketSyncMineralGenerationState(globalGenerationEnabled));
    }
}
