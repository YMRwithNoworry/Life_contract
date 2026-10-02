package org.alku.life_contract.airdrop;

import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.alku.life_contract.airdrop.command.AirdropCommand;
import org.alku.life_contract.airdrop.entity.AirdropEntity;
import org.alku.life_contract.airdrop.entity.AirdropRenderer;
import org.alku.life_contract.airdrop.event.BorderIntegrationHandler;
import org.alku.life_contract.airdrop.event.CommonEvents;
import org.alku.life_contract.airdrop.item.DisposableFlareGunItem;
import org.alku.life_contract.airdrop.item.FlareGunItem;
import org.alku.life_contract.airdrop.item.RangeLimiterItem;
import org.alku.life_contract.airdrop.item.SignalDecoyItem;
import org.slf4j.Logger;
import org.alku.life_contract.Life_contract;

public final class Airdrop {
    public static final String MODID = Life_contract.MODID;
    public static final Logger LOGGER = LogUtils.getLogger();

    // Deferred registers (NeoForge uses vanilla registry keys)
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, MODID);
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    // Items
    public static final DeferredItem<Item> FLARE_GUN = ITEMS.register("flare_gun", FlareGunItem::new);
    public static final DeferredItem<Item> DISPOSABLE_FLARE_GUN = ITEMS.register("disposable_flare_gun",
            DisposableFlareGunItem::new);
    public static final DeferredItem<Item> RANGE_LIMITER = ITEMS.register("range_limiter", RangeLimiterItem::new);
    public static final DeferredItem<Item> SIGNAL_DECOY = ITEMS.register("signal_decoy", SignalDecoyItem::new);

    // Entity
    public static final DeferredHolder<EntityType<?>, EntityType<AirdropEntity>> AIRDROP_ENTITY = ENTITIES.register(
            "airdrop",
            () -> EntityType.Builder.<AirdropEntity>of(AirdropEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(10)
                    .build("airdrop"));

    // Creative Tab
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> AIRDROP_TAB = TABS.register("airdrop_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(FLARE_GUN.get()))
                    .title(Component.translatable("itemGroup.life_contract.airdrop_tab"))
                    .displayItems((params, output) -> {
                        output.accept(FLARE_GUN.get());
                        output.accept(DISPOSABLE_FLARE_GUN.get());
                        output.accept(SIGNAL_DECOY.get());
                        output.accept(RANGE_LIMITER.get());
                    })
                    .build());

    private Airdrop() {
    }

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
        ENTITIES.register(modEventBus);
        TABS.register(modEventBus);
    }
}
