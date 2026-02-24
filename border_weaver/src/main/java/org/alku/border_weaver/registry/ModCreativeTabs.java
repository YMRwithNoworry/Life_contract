package org.alku.border_weaver.registry;

import org.alku.border_weaver.Border_weaver;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Border_weaver.MODID);

    public static final RegistryObject<CreativeModeTab> BORDER_WEAVER_TAB = CREATIVE_MODE_TABS.register("border_weaver_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BORDER_TOOL.get()))
                    .title(Component.translatable("creativetab.border_weaver_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.BORDER_TOOL.get());
                    })
                    .build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}