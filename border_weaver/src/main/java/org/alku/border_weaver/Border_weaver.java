package org.alku.border_weaver;

import com.mojang.logging.LogUtils;
import org.alku.border_weaver.command.BorderCommand;
import org.alku.border_weaver.registry.ModCreativeTabs;
import org.alku.border_weaver.registry.ModItems;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(Border_weaver.MODID)
public class Border_weaver {
    public static final String MODID = "border_weaver";
    public static final Logger LOGGER = LogUtils.getLogger();

    public Border_weaver() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // 注册物品和标签页
        ModItems.ITEMS.register(modEventBus);
        ModCreativeTabs.register(modEventBus);

        // 注册到 Forge 事件总线
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        BorderCommand.register(event.getDispatcher());
    }
}