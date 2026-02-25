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

import org.alku.life_contract.follower.FollowerWandMenu;
import org.alku.life_contract.follower.FollowerWandItem;
import org.alku.life_contract.mineral_generator.MineralGeneratorBlockEntity;
import org.alku.life_contract.mineral_generator.MineralGeneratorBlock;
import org.alku.life_contract.mineral_generator.MineralGeneratorMenu;
import org.alku.life_contract.profession.ProfessionConfig;
import org.alku.life_contract.revive.ReviveTeammateMenu;

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
            }).build());

    public Life_contract() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ITEMS.register(modEventBus);
        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        NetworkHandler.register();

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
        public static void onClientSetup(FMLClientSetupEvent event) {
            event.enqueueWork(() -> {
                try {
                    Class<?> screensClass = Class.forName("net.minecraft.client.gui.screens.MenuScreens");
                    Class<?> screenClass = Class.forName("net.minecraft.client.gui.screens.Screen");
                    
                    Class<?> teamInvScreenClass = Class.forName("org.alku.life_contract.TeamInventoryScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, TEAM_INVENTORY_MENU.get(), 
                            teamInvScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> shopScreenClass = Class.forName("org.alku.life_contract.ShopScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, SHOP_MENU.get(), 
                            shopScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> tradeSetupScreenClass = Class.forName("org.alku.life_contract.TradeSetupScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, TRADE_SETUP_MENU.get(), 
                            tradeSetupScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> tradeShopScreenClass = Class.forName("org.alku.life_contract.TradeShopScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, TRADE_SHOP_MENU.get(), 
                            tradeShopScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> mineralGenScreenClass = Class.forName("org.alku.life_contract.mineral_generator.MineralGeneratorScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, MINERAL_GENERATOR_MENU.get(), 
                            mineralGenScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> followerWandScreenClass = Class.forName("org.alku.life_contract.follower.FollowerWandScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, FOLLOWER_WAND_MENU.get(), 
                            followerWandScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
                    Class<?> reviveScreenClass = Class.forName("org.alku.life_contract.revive.ReviveTeammateScreen");
                    screensClass.getMethod("register", MenuType.class, Class.forName("net.minecraft.client.gui.screens.MenuScreens$ScreenConstructor"))
                        .invoke(null, REVIVE_TEAMMATE_MENU.get(), 
                            reviveScreenClass.getConstructor(MenuType.class, 
                                Class.forName("net.minecraft.world.entity.player.Inventory"),
                                Class.forName("net.minecraft.network.chat.Component")));
                    
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

            try {
                Class<?> mcClass = Class.forName("net.minecraft.client.Minecraft");
                Object mc = mcClass.getMethod("getInstance").invoke(null);
                Object player = mcClass.getMethod("player").invoke(mc);
                if (player == null)
                    return;

                Class<?> keyBindingsClass = Class.forName("org.alku.life_contract.KeyBindings");
                Object openTeamInv = keyBindingsClass.getField("OPEN_TEAM_INVENTORY").get(null);
                java.lang.reflect.Method consumeClick = openTeamInv.getClass().getMethod("consumeClick");
                
                while ((boolean) consumeClick.invoke(openTeamInv)) {
                    Object level = player.getClass().getMethod("level").invoke(player);
                    boolean isClientSide = (boolean) level.getClass().getMethod("isClientSide").invoke(level);
                    if (!isClientSide)
                        return;
                    NetworkHandler.sendOpenTeamInventoryPacket();
                }

                Object healerActiveHeal = keyBindingsClass.getField("HEALER_ACTIVE_HEAL").get(null);
                while ((boolean) consumeClick.invoke(healerActiveHeal)) {
                    Object level = player.getClass().getMethod("level").invoke(player);
                    boolean isClientSide = (boolean) level.getClass().getMethod("isClientSide").invoke(level);
                    if (!isClientSide)
                        return;
                    NetworkHandler.sendHealerActiveHealPacket();
                }

                Object foolStealProfession = keyBindingsClass.getField("FOOL_STEAL_PROFESSION").get(null);
                while ((boolean) consumeClick.invoke(foolStealProfession)) {
                    Object level = player.getClass().getMethod("level").invoke(player);
                    boolean isClientSide = (boolean) level.getClass().getMethod("isClientSide").invoke(level);
                    if (!isClientSide)
                        return;
                    FoolSystem.sendStealPacket(null);
                }

                Class<?> clientDataStorageClass = Class.forName("org.alku.life_contract.ClientDataStorage");
                clientDataStorageClass.getMethod("tickHealerCooldown").invoke(null);
            } catch (Exception e) {
                e.printStackTrace();
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
