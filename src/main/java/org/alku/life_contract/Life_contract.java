package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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

@Mod(Life_contract.MODID)
public class Life_contract {
    public static final String MODID = "life_contract";

    // 注册器
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

    // 物品注册
    public static final RegistryObject<Item> SOUL_CONTRACT = ITEMS.register("soul_contract", SoulContractItem::new);
    public static final RegistryObject<Item> TEAM_ORGANIZER = ITEMS.register("team_organizer", TeamOrganizerItem::new);
    public static final RegistryObject<Item> FOLLOWER_WAND = ITEMS.register("follower_wand", FollowerWandItem::new);
    public static final RegistryObject<Item> CREATURE_EGG = ITEMS.register("creature_egg", CreatureEggItem::new);
    public static final RegistryObject<Item> GAMBLER_DICE = ITEMS.register("gambler_dice", GamblerDiceItem::new);
    public static final RegistryObject<Item> DONK_BOW = ITEMS.register("donk_bow", DonkBowItem::new);
    public static final RegistryObject<Item> SEALED_BOW = ITEMS.register("sealed_bow", SealedBowItem::new);
    public static final RegistryObject<Item> FACELESS_DECEIVER_MASK = ITEMS.register("faceless_deceiver_mask", 
            () -> new FacelessDeceiverMaskItem(new Item.Properties()));
    public static final RegistryObject<Item> AMBUSH_ORB = ITEMS.register("ambush_orb", AmbushOrbItem::new);
    public static final RegistryObject<Item> TEAM_GOLEM_WAND = ITEMS.register("team_golem_wand", TeamGolemWandItem::new);

    // 方块注册
    public static final RegistryObject<net.minecraft.world.level.block.Block> MINERAL_GENERATOR_BLOCK = BLOCKS.register("mineral_generator",
            () -> new MineralGeneratorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)));

    // 方块物品注册
    public static final RegistryObject<Item> MINERAL_GENERATOR_ITEM = ITEMS.register("mineral_generator",
            () -> new net.minecraft.world.item.BlockItem(MINERAL_GENERATOR_BLOCK.get(), new Item.Properties()));

    // 方块实体注册
    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<MineralGeneratorBlockEntity>> MINERAL_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("mineral_generator",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(MineralGeneratorBlockEntity::new, MINERAL_GENERATOR_BLOCK.get()).build(null));

    // 菜单注册
    public static final RegistryObject<MenuType<TeamInventoryMenu>> TEAM_INVENTORY_MENU = MENU_TYPES.register("team_inventory",
            () -> IForgeMenuType.create(TeamInventoryMenu::new));
    public static final RegistryObject<MenuType<ShopMenu>> SHOP_MENU = MENU_TYPES.register("shop",
            () -> IForgeMenuType.create(ShopMenu::new));
    public static final RegistryObject<MenuType<TradeSetupMenu>> TRADE_SETUP_MENU = MENU_TYPES.register("trade_setup",
            () -> IForgeMenuType.create(TradeSetupMenu::new));
    public static final RegistryObject<MenuType<TradeShopMenu>> TRADE_SHOP_MENU = MENU_TYPES.register("trade_shop",
            () -> IForgeMenuType.create(TradeShopMenu::new));
    public static final RegistryObject<MenuType<MineralGeneratorMenu>> MINERAL_GENERATOR_MENU = MENU_TYPES.register("mineral_generator",
            () -> IForgeMenuType.create(MineralGeneratorMenu::new));
    public static final RegistryObject<MenuType<FollowerWandMenu>> FOLLOWER_WAND_MENU = MENU_TYPES.register("follower_wand",
            () -> IForgeMenuType.create(FollowerWandMenu::new));

    // 实体注册
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

    // 创造模式物品栏
    public static final RegistryObject<CreativeModeTab> MOD_TAB = CREATIVE_TABS.register("life_contract_tab", () -> CreativeModeTab.builder()
            .icon(() -> SOUL_CONTRACT.get().getDefaultInstance())
            .title(Component.translatable("itemGroup.life_contract"))
            .displayItems((parameters, output) -> {
                output.accept(SOUL_CONTRACT.get());
                output.accept(TEAM_ORGANIZER.get());
                output.accept(FOLLOWER_WAND.get());
                output.accept(CREATURE_EGG.get());
                output.accept(MINERAL_GENERATOR_ITEM.get());
                output.accept(GAMBLER_DICE.get());
                output.accept(DONK_BOW.get());
                output.accept(SEALED_BOW.get());
                output.accept(FACELESS_DECEIVER_MASK.get());
                output.accept(AMBUSH_ORB.get());
                output.accept(TEAM_GOLEM_WAND.get());
            }).build());

    public Life_contract() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        // 注册网络包通道
        NetworkHandler.register();

        // 注册到 Forge 总线
        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
            TradeConfig.init();
            ProfessionConfig.load();
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                MenuScreens.register(TEAM_INVENTORY_MENU.get(), TeamInventoryScreen::new);
                MenuScreens.register(SHOP_MENU.get(), ShopScreen::new);
                MenuScreens.register(TRADE_SETUP_MENU.get(), TradeSetupScreen::new);
                MenuScreens.register(TRADE_SHOP_MENU.get(), TradeShopScreen::new);
                MenuScreens.register(MINERAL_GENERATOR_MENU.get(), MineralGeneratorScreen::new);
                MenuScreens.register(FOLLOWER_WAND_MENU.get(), FollowerWandScreen::new);
                
                net.minecraft.client.renderer.item.ItemProperties.register(
                    DONK_BOW.get(),
                    new ResourceLocation("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        ItemStack useItem = entity.getUseItem();
                        return useItem.getItem() == DONK_BOW.get() ? (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F : 0.0F;
                    }
                );
                net.minecraft.client.renderer.item.ItemProperties.register(
                    DONK_BOW.get(),
                    new ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().getItem() == DONK_BOW.get() ? 1.0F : 0.0F
                );
                net.minecraft.client.renderer.item.ItemProperties.register(
                    SEALED_BOW.get(),
                    new ResourceLocation("pull"),
                    (stack, level, entity, seed) -> {
                        if (entity == null) return 0.0F;
                        ItemStack useItem = entity.getUseItem();
                        return useItem.getItem() == SEALED_BOW.get() ? (float)(stack.getUseDuration() - entity.getUseItemRemainingTicks()) / 20.0F : 0.0F;
                    }
                );
                net.minecraft.client.renderer.item.ItemProperties.register(
                    SEALED_BOW.get(),
                    new ResourceLocation("pulling"),
                    (stack, level, entity, seed) -> entity != null && entity.isUsingItem() && entity.getUseItem().getItem() == SEALED_BOW.get() ? 1.0F : 0.0F
                );
            });
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void onClientTick(TickEvent.ClientTickEvent event) {
            if (event.phase != TickEvent.Phase.END)
                return;

            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null)
                return;

            while (KeyBindings.OPEN_TEAM_INVENTORY.consumeClick()) {
                if (!player.level().isClientSide)
                    return;
                NetworkHandler.sendOpenTeamInventoryPacket();
            }

            while (KeyBindings.HEALER_ACTIVE_HEAL.consumeClick()) {
                if (!player.level().isClientSide)
                    return;
                NetworkHandler.sendHealerActiveHealPacket();
            }

            while (KeyBindings.FOOL_STEAL_PROFESSION.consumeClick()) {
                if (!player.level().isClientSide)
                    return;
                FoolSystem.sendStealPacket(null);
            }

            ClientDataStorage.tickHealerCooldown();
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