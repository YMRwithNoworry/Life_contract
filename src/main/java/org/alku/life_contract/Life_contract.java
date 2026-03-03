package org.alku.life_contract;

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
import org.alku.life_contract.mineral_generator.MineralGeneratorBlockEntity;
import org.alku.life_contract.mineral_generator.MineralGeneratorBlock;
import org.alku.life_contract.mineral_generator.MineralGeneratorMenu;
import org.alku.life_contract.mineral_generator.MineralGeneratorScreen;
import org.alku.life_contract.profession.ProfessionConfig;
import org.alku.life_contract.revive.ReviveTeammateMenu;
import org.alku.life_contract.revive.ReviveTeammateScreen;
import org.alku.life_contract.byte_chen.ByteChenHUD;
import org.alku.life_contract.wraith_councilor.SoulforgedArmorMaterial;
import org.alku.life_contract.wraith_councilor.SoulforgedArmorItem;
import org.alku.life_contract.wraith_councilor.CouncilSoulCandleStaffItem;
import org.alku.life_contract.wraith_councilor.CouncilEmblemItem;
import org.alku.life_contract.wraith_councilor.PurpleHoodedSkullArmorItem;
import org.alku.life_contract.byte_chen.DataTerminalArmorMaterial;
import org.alku.life_contract.byte_chen.DataGogglesItem;
import org.alku.life_contract.byte_chen.TerminalRobeItem;
import org.alku.life_contract.byte_chen.DataLeggingsItem;
import org.alku.life_contract.byte_chen.FlashBootsItem;
import org.alku.life_contract.byte_chen.ByteCodeScepterItem;
import org.alku.life_contract.byte_chen.ChenCoreChipItem;

@Mod(Life_contract.MODID)
public class Life_contract {
    public static final String MODID = "life_contract";

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(ForgeRegistries.MENU_TYPES, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final DeferredRegister<net.minecraft.world.level.block.Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<net.minecraft.world.level.block.entity.BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, MODID);

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
    
    public static final RegistryObject<Item> SOULFORGED_HOOD = ITEMS.register("soulforged_hood",
            () -> new SoulforgedArmorItem(ArmorItem.Type.HELMET, new Item.Properties()));
    public static final RegistryObject<Item> SOULFORGED_ROBE = ITEMS.register("soulforged_robe",
            () -> new SoulforgedArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    public static final RegistryObject<Item> SOULFORGED_LEGGINGS = ITEMS.register("soulforged_leggings",
            () -> new SoulforgedArmorItem(ArmorItem.Type.LEGGINGS, new Item.Properties()));
    public static final RegistryObject<Item> SOULFORGED_BOOTS = ITEMS.register("soulforged_boots",
            () -> new SoulforgedArmorItem(ArmorItem.Type.BOOTS, new Item.Properties()));
    public static final RegistryObject<Item> COUNCIL_SOUL_CANDLE_STAFF = ITEMS.register("council_soul_candle_staff",
            () -> new CouncilSoulCandleStaffItem(net.minecraft.world.item.Tiers.WOOD, 2, -2.0f, new Item.Properties()));
    public static final RegistryObject<Item> COUNCIL_EMBLEM = ITEMS.register("council_emblem",
            () -> new CouncilEmblemItem(new Item.Properties()));
    public static final RegistryObject<Item> PURPLE_HOODED_SKULL_CHESTPLATE = ITEMS.register("purple_hooded_skull_chestplate",
            () -> new PurpleHoodedSkullArmorItem(ArmorItem.Type.CHESTPLATE, new Item.Properties()));
    
    public static final RegistryObject<Item> DATA_GOGGLES = ITEMS.register("data_goggles",
            () -> new DataGogglesItem(DataTerminalArmorMaterial.INSTANCE, new Item.Properties()));
    public static final RegistryObject<Item> TERMINAL_ROBE = ITEMS.register("terminal_robe",
            () -> new TerminalRobeItem(DataTerminalArmorMaterial.INSTANCE, new Item.Properties()));
    public static final RegistryObject<Item> DATA_LEGGINGS = ITEMS.register("data_leggings",
            () -> new DataLeggingsItem(DataTerminalArmorMaterial.INSTANCE, new Item.Properties()));
    public static final RegistryObject<Item> FLASH_BOOTS = ITEMS.register("flash_boots",
            () -> new FlashBootsItem(DataTerminalArmorMaterial.INSTANCE, new Item.Properties()));
    public static final RegistryObject<Item> BYTE_CODE_SCEPTER = ITEMS.register("byte_code_scepter",
            () -> new ByteCodeScepterItem(net.minecraft.world.item.Tiers.WOOD, 2, -2.8f, new Item.Properties()));
    public static final RegistryObject<Item> CHEN_CORE_CHIP = ITEMS.register("chen_core_chip",
            () -> new ChenCoreChipItem(new Item.Properties()));
    
    public static final RegistryObject<Item> GOLD_COIN = ITEMS.register("gold_coin", GoldCoinItem::new);

    public static final RegistryObject<net.minecraft.world.level.block.Block> MINERAL_GENERATOR_BLOCK = BLOCKS.register("mineral_generator",
            () -> new MineralGeneratorBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties.of()
                    .strength(-1.0F, 3600000.0F)));

    public static final RegistryObject<Item> MINERAL_GENERATOR_ITEM = ITEMS.register("mineral_generator",
            () -> new net.minecraft.world.item.BlockItem(MINERAL_GENERATOR_BLOCK.get(), new Item.Properties()));

    public static final RegistryObject<net.minecraft.world.level.block.entity.BlockEntityType<MineralGeneratorBlockEntity>> MINERAL_GENERATOR_BLOCK_ENTITY = BLOCK_ENTITY_TYPES.register("mineral_generator",
            () -> net.minecraft.world.level.block.entity.BlockEntityType.Builder.of(MineralGeneratorBlockEntity::new, MINERAL_GENERATOR_BLOCK.get()).build(null));

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
    public static final RegistryObject<MenuType<ReviveTeammateMenu>> REVIVE_TEAMMATE_MENU = MENU_TYPES.register("revive_teammate",
            () -> IForgeMenuType.create(ReviveTeammateMenu::new));
    public static final RegistryObject<MenuType<EggShopMenu>> EGG_SHOP_MENU = MENU_TYPES.register("egg_shop",
            () -> IForgeMenuType.create(EggShopMenu::new));

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
                output.accept(MINERAL_GENERATOR_ITEM.get());
                output.accept(GAMBLER_DICE.get());
                output.accept(DONK_BOW.get());
                output.accept(SEALED_BOW.get());
                output.accept(FACELESS_DECEIVER_MASK.get());
                output.accept(AMBUSH_ORB.get());
                output.accept(TEAM_GOLEM_WAND.get());
                output.accept(SOULFORGED_HOOD.get());
                output.accept(SOULFORGED_ROBE.get());
                output.accept(SOULFORGED_LEGGINGS.get());
                output.accept(SOULFORGED_BOOTS.get());
                output.accept(COUNCIL_SOUL_CANDLE_STAFF.get());
                output.accept(COUNCIL_EMBLEM.get());
                output.accept(PURPLE_HOODED_SKULL_CHESTPLATE.get());
                output.accept(DATA_GOGGLES.get());
                output.accept(TERMINAL_ROBE.get());
                output.accept(DATA_LEGGINGS.get());
                output.accept(FLASH_BOOTS.get());
                output.accept(BYTE_CODE_SCEPTER.get());
                output.accept(CHEN_CORE_CHIP.get());
                output.accept(GOLD_COIN.get());
            }).build());

    public Life_contract() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        org.alku.life_contract.gourmet.GourmetItems.ITEMS.register(modEventBus);

        NetworkHandler.register();

        MinecraftForge.EVENT_BUS.register(this);
    }

    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ServerEvents {
        @SubscribeEvent
        public static void onServerStarting(net.minecraftforge.event.server.ServerStartingEvent event) {
            TradeConfig.init();
            ProfessionConfig.load();
            EggShopConfig.init(event.getServer().getServerDirectory());
        }
    }

    @Mod.EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onRegisterGuiOverlays(RegisterGuiOverlaysEvent event) {
            event.registerAboveAll("byte_chen_hud", new ByteChenHUD());
        }

        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                net.minecraft.client.gui.screens.MenuScreens.register(TEAM_INVENTORY_MENU.get(), TeamInventoryScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(SHOP_MENU.get(), ShopScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(TRADE_SETUP_MENU.get(), TradeSetupScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(TRADE_SHOP_MENU.get(), TradeShopScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(MINERAL_GENERATOR_MENU.get(), MineralGeneratorScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(FOLLOWER_WAND_MENU.get(), FollowerWandScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(REVIVE_TEAMMATE_MENU.get(), ReviveTeammateScreen::new);
                net.minecraft.client.gui.screens.MenuScreens.register(EGG_SHOP_MENU.get(), EggShopScreen::new);
                
                try {
                    Class<?> itemPropertiesClass = Class.forName("net.minecraft.client.renderer.item.ItemProperties");
                    Class<?> itemPropertyFunctionClass = Class.forName("net.minecraft.client.renderer.item.ItemProperties$PropertyFunction");
                    
                    Object pullProperty = java.lang.reflect.Proxy.newProxyInstance(
                        itemPropertyFunctionClass.getClassLoader(),
                        new Class<?>[] { itemPropertyFunctionClass },
                        (proxy, method, args) -> {
                            if (method.getName().equals("call") || method.getName().equals("apply")) {
                                Object stack = args[0];
                                Object level = args[1];
                                Object entity = args[2];
                                Integer seed = args.length > 3 ? (Integer) args[3] : null;
                                
                                if (entity == null) return 0.0F;
                                
                                java.lang.reflect.Method getUseItem = entity.getClass().getMethod("getUseItem");
                                Object useItem = getUseItem.invoke(entity);
                                java.lang.reflect.Method getItem = useItem.getClass().getMethod("getItem");
                                Object item = getItem.invoke(useItem);
                                
                                if (item == DONK_BOW.get()) {
                                    java.lang.reflect.Method getUseDuration = stack.getClass().getMethod("getUseDuration");
                                    int useDuration = (int) getUseDuration.invoke(stack);
                                    java.lang.reflect.Method getUseItemRemainingTicks = entity.getClass().getMethod("getUseItemRemainingTicks");
                                    int remainingTicks = (int) getUseItemRemainingTicks.invoke(entity);
                                    return (float)(useDuration - remainingTicks) / 20.0F;
                                }
                                return 0.0F;
                            }
                            return null;
                        }
                    );
                    
                    itemPropertiesClass.getMethod("register", Item.class, ResourceLocation.class, itemPropertyFunctionClass)
                        .invoke(null, DONK_BOW.get(), new ResourceLocation("pull"), pullProperty);
                    
                    Object pullingProperty = java.lang.reflect.Proxy.newProxyInstance(
                        itemPropertyFunctionClass.getClassLoader(),
                        new Class<?>[] { itemPropertyFunctionClass },
                        (proxy, method, args) -> {
                            if (method.getName().equals("call") || method.getName().equals("apply")) {
                                Object entity = args[2];
                                if (entity != null) {
                                    java.lang.reflect.Method isUsingItem = entity.getClass().getMethod("isUsingItem");
                                    boolean using = (boolean) isUsingItem.invoke(entity);
                                    java.lang.reflect.Method getUseItem = entity.getClass().getMethod("getUseItem");
                                    Object useItem = getUseItem.invoke(entity);
                                    java.lang.reflect.Method getItem = useItem.getClass().getMethod("getItem");
                                    Object item = getItem.invoke(useItem);
                                    return using && item == DONK_BOW.get() ? 1.0F : 0.0F;
                                }
                                return 0.0F;
                            }
                            return null;
                        }
                    );
                    
                    itemPropertiesClass.getMethod("register", Item.class, ResourceLocation.class, itemPropertyFunctionClass)
                        .invoke(null, DONK_BOW.get(), new ResourceLocation("pulling"), pullingProperty);
                    
                    Object sealedPullProperty = java.lang.reflect.Proxy.newProxyInstance(
                        itemPropertyFunctionClass.getClassLoader(),
                        new Class<?>[] { itemPropertyFunctionClass },
                        (proxy, method, args) -> {
                            if (method.getName().equals("call") || method.getName().equals("apply")) {
                                Object stack = args[0];
                                Object entity = args[2];
                                
                                if (entity == null) return 0.0F;
                                
                                java.lang.reflect.Method getUseItem = entity.getClass().getMethod("getUseItem");
                                Object useItem = getUseItem.invoke(entity);
                                java.lang.reflect.Method getItem = useItem.getClass().getMethod("getItem");
                                Object item = getItem.invoke(useItem);
                                
                                if (item == SEALED_BOW.get()) {
                                    java.lang.reflect.Method getUseDuration = stack.getClass().getMethod("getUseDuration");
                                    int useDuration = (int) getUseDuration.invoke(stack);
                                    java.lang.reflect.Method getUseItemRemainingTicks = entity.getClass().getMethod("getUseItemRemainingTicks");
                                    int remainingTicks = (int) getUseItemRemainingTicks.invoke(entity);
                                    return (float)(useDuration - remainingTicks) / 20.0F;
                                }
                                return 0.0F;
                            }
                            return null;
                        }
                    );
                    
                    itemPropertiesClass.getMethod("register", Item.class, ResourceLocation.class, itemPropertyFunctionClass)
                        .invoke(null, SEALED_BOW.get(), new ResourceLocation("pull"), sealedPullProperty);
                    
                    Object sealedPullingProperty = java.lang.reflect.Proxy.newProxyInstance(
                        itemPropertyFunctionClass.getClassLoader(),
                        new Class<?>[] { itemPropertyFunctionClass },
                        (proxy, method, args) -> {
                            if (method.getName().equals("call") || method.getName().equals("apply")) {
                                Object entity = args[2];
                                if (entity != null) {
                                    java.lang.reflect.Method isUsingItem = entity.getClass().getMethod("isUsingItem");
                                    boolean using = (boolean) isUsingItem.invoke(entity);
                                    java.lang.reflect.Method getUseItem = entity.getClass().getMethod("getUseItem");
                                    Object useItem = getUseItem.invoke(entity);
                                    java.lang.reflect.Method getItem = useItem.getClass().getMethod("getItem");
                                    Object item = getItem.invoke(useItem);
                                    return using && item == SEALED_BOW.get() ? 1.0F : 0.0F;
                                }
                                return 0.0F;
                            }
                            return null;
                        }
                    );
                    
                    itemPropertiesClass.getMethod("register", Item.class, ResourceLocation.class, itemPropertyFunctionClass)
                        .invoke(null, SEALED_BOW.get(), new ResourceLocation("pulling"), sealedPullingProperty);
                    
                } catch (Exception e) {
                    e.printStackTrace();
                }
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
            
            while (KeyBindings.HEALER_ACTIVE_HEAL.consumeClick()) {
                NetworkHandler.sendHealerActiveHealPacket();
            }
            
            while (KeyBindings.FOOL_STEAL_PROFESSION.consumeClick()) {
                FoolSystem.sendStealPacket(null);
            }
            
            while (KeyBindings.OPEN_EGG_SHOP.consumeClick()) {
                NetworkHandler.sendOpenEggShopPacket();
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
