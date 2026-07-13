package org.alku.life_contract.client;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fml.ModList;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.ModPoolConfig;

import java.util.Comparator;

public final class LifeContractConfigScreen {
    private LifeContractConfigScreen() {}

    public static Screen create(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Component.literal("生灵契约配置"));
        ConfigEntryBuilder entries = builder.entryBuilder();
        var general = builder.getOrCreateCategory(Component.literal("游戏与阵营"));
        general.addEntry(entries.startIntSlider(Component.literal("随机队伍数量"), ModPoolConfig.getTeamCount(), 1, 32)
                .setDefaultValue(4).setSaveConsumer(ModPoolConfig::setTeamCount).build());

        var pool = builder.getOrCreateCategory(Component.literal("感染模组池"));
        ModList.get().getMods().stream()
                .filter(info -> !info.getModId().equals("minecraft") && !info.getModId().equals(Life_contract.MODID))
                .sorted(Comparator.comparing(info -> info.getDisplayName().toLowerCase()))
                .forEach(info -> pool.addEntry(entries.startBooleanToggle(
                                Component.literal(info.getDisplayName() + " [" + info.getModId() + "]"),
                                ModPoolConfig.getModPool().contains(info.getModId()))
                        .setDefaultValue(ModPoolConfig.DEFAULT_POOL.contains(info.getModId()))
                        .setSaveConsumer(value -> ModPoolConfig.setModEnabled(info.getModId(), value)).build()));
        builder.setSavingRunnable(ModPoolConfig::save);
        return builder.build();
    }
}
