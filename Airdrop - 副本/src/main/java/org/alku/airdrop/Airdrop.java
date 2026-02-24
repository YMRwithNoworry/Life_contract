package org.alku.airdrop;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.alku.airdrop.command.AirdropCommand;
import org.alku.airdrop.entity.AirdropEntity;
import org.alku.airdrop.entity.AirdropRenderer;
import org.alku.airdrop.event.CommonEvents;
import org.alku.airdrop.item.DisposableFlareGunItem;
import org.alku.airdrop.item.FlareGunItem;
import org.alku.airdrop.item.RangeLimiterItem;
import org.slf4j.Logger;

@Mod(Airdrop.MODID)
public class Airdrop {
    public static final String MODID = "airdrop";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES,
            MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister
            .create(net.minecraft.resources.ResourceKey.createRegistryKey(
                    new net.minecraft.resources.ResourceLocation("minecraft", "creative_mode_tab")), MODID);

    // Items
    public static final RegistryObject<Item> FLARE_GUN = ITEMS.register("flare_gun", FlareGunItem::new);
    public static final RegistryObject<Item> DISPOSABLE_FLARE_GUN = ITEMS.register("disposable_flare_gun",
            DisposableFlareGunItem::new);
    public static final RegistryObject<Item> RANGE_LIMITER = ITEMS.register("range_limiter", RangeLimiterItem::new);

    // Entity
    public static final RegistryObject<EntityType<AirdropEntity>> AIRDROP_ENTITY = ENTITIES.register("airdrop",
            () -> EntityType.Builder.<AirdropEntity>of(AirdropEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(10)
                    .build("airdrop"));

    // Creative Tab
    public static final RegistryObject<CreativeModeTab> AIRDROP_TAB = TABS.register("airdrop_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FLARE_GUN.get()))
                    .title(Component.translatable("itemGroup.airdrop"))
                    .displayItems((params, output) -> {
                        output.accept(FLARE_GUN.get());
                        output.accept(DISPOSABLE_FLARE_GUN.get());
                        output.accept(RANGE_LIMITER.get());
                    })
                    .build());

    public Airdrop() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);
        modEventBus.addListener(this::clientSetup);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(CommonEvents.class);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        EntityRenderers.register(AIRDROP_ENTITY.get(), AirdropRenderer::new);
    }

    @SubscribeEvent
    public void onRegisterCommands(RegisterCommandsEvent event) {
        AirdropCommand.register(event.getDispatcher());
    }
}