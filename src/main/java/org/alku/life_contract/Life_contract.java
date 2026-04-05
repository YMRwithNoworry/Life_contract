package org.alku.life_contract;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;

import org.alku.life_contract.follower.FollowerWandMenu;
import org.alku.life_contract.follower.FollowerWandItem;
import org.alku.life_contract.follower.FollowerWandScreen;
import org.alku.life_contract.revive.ReviveTeammateMenu;
import org.alku.life_contract.revive.ReviveTeammateScreen;

@Mod(Life_contract.MODID)
public class Life_contract {
    public static final String MODID = "life_contract";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MODID);

    public static final RegistryObject<Item> SOUL_CONTRACT = ITEMS.register("soul_contract", SoulContractItem::new);
    public static final RegistryObject<Item> TEAM_ORGANIZER = ITEMS.register("team_organizer", TeamOrganizerItem::new);
    public static final RegistryObject<Item> FOLLOWER_WAND = ITEMS.register("follower_wand", FollowerWandItem::new);
    public static final RegistryObject<Item> CREATURE_EGG = ITEMS.register("creature_egg", CreatureEggItem::new);
    public static final RegistryObject<Item> SURVIVOR_EMBLEM = ITEMS.register("survivor_emblem", SurvivorEmblemItem::new);
    public static final RegistryObject<Item> SPORE_GLAND = ITEMS.register("spore_gland", SporeGlandItem::new);

    public static final RegistryObject<MobEffect> SLOW_INFECTION = MOB_EFFECTS.register("slow_infection", SlowInfectionEffect::new);

    public static final RegistryObject<MenuType<TeamInventoryMenu>> TEAM_INVENTORY_MENU = MENU_TYPES.register("team_inventory",
            () -> IForgeMenuType.create(TeamInventoryMenu::new));
    public static final RegistryObject<MenuType<FollowerWandMenu>> FOLLOWER_WAND_MENU = MENU_TYPES.register("follower_wand",
            () -> IForgeMenuType.create(FollowerWandMenu::new));
    public static final RegistryObject<MenuType<ReviveTeammateMenu>> REVIVE_TEAMMATE_MENU = MENU_TYPES.register("revive_teammate",
            () -> IForgeMenuType.create(ReviveTeammateMenu::new));

    public static final RegistryObject<EntityType<TeamSentinel>> TEAM_SENTINEL = ENTITY_TYPES.register("team_sentinel",
            () -> EntityType.Builder.of(TeamSentinel::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("team_sentinel"));
    
    public static final RegistryObject<EntityType<FireTrailEntity>> FIRE_TRAIL = ENTITY_TYPES.register("fire_trail",
            () -> EntityType.Builder.<FireTrailEntity>of(FireTrailEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("fire_trail"));

    public static final RegistryObject<CreativeModeTab> MOD_TAB = CREATIVE_TABS.register("life_contract_tab", () -> CreativeModeTab.builder()
            .icon(() -> SOUL_CONTRACT.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.life_contract"))
            .displayItems((parameters, output) -> {
                output.accept(SOUL_CONTRACT.get());
                output.accept(TEAM_ORGANIZER.get());
                output.accept(FOLLOWER_WAND.get());
                output.accept(CREATURE_EGG.get());
                output.accept(SURVIVOR_EMBLEM.get());
                output.accept(SPORE_GLAND.get());
            }).build());

    public Life_contract() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);

        NetworkHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
        
        modEventBus.addListener(this::onCommonSetup);
    }
    
    private void onCommonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            CuriosIntegration.init();
        });
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(TEAM_INVENTORY_MENU.get(), TeamInventoryScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(FOLLOWER_WAND_MENU.get(), FollowerWandScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(REVIVE_TEAMMATE_MENU.get(), ReviveTeammateScreen::new);
            });
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END)
                return;

            net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
            if (mc.player == null)
                return;

            while (KeyBindings.OPEN_TEAM_INVENTORY.consumeClick()) {
                NetworkHandler.sendOpenTeamInventoryPacket();
            }
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(TEAM_SENTINEL.get(), TeamSentinel.createAttributes().build());
        }
    }
}
