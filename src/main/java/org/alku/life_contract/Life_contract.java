package org.alku.life_contract;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.resources.ResourceLocation;
import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModLoadingContext;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.alku.life_contract.blocks.ModBlocks;
import org.alku.life_contract.follower.FollowerWandItem;
import org.alku.life_contract.follower.WandEggUIHolder;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.airdrop.command.AirdropCommand;
import org.alku.life_contract.airdrop.entity.AirdropRenderer;
import org.alku.life_contract.client.UpgradeHubUIHolder;
import org.alku.life_contract.client.MutationUIHolder;
import org.alku.life_contract.client.SublimationShopUIHolder;
import org.alku.life_contract.client.TeamInventoryUIHolder;
import org.alku.life_contract.items.MeatPasteItem;
import org.alku.life_contract.items.SporeBombItem;
import org.alku.life_contract.items.SublimationItem;
import org.alku.life_contract.mutation.MutationMenu;
import org.alku.life_contract.mutation.MutationScreen;
import org.alku.life_contract.revive.ReviveTeammateMenu;
import org.alku.life_contract.revive.ReviveTeammateScreen;

@Mod(Life_contract.MODID)
public class Life_contract {
    public static final String MODID = "life_contract";
    public static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(BuiltInRegistries.ITEM, MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<MenuType<?>> MENU_TYPES = DeferredRegister.create(BuiltInRegistries.MENU, MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(BuiltInRegistries.ENTITY_TYPE, MODID);
    public static final DeferredHolder<Item, Item> SOUL_CONTRACT = ITEMS.register("soul_contract", SoulContractItem::new);
    public static final DeferredHolder<Item, Item> TEAM_ORGANIZER = ITEMS.register("team_organizer", TeamOrganizerItem::new);
    public static final DeferredHolder<Item, Item> FOLLOWER_WAND = ITEMS.register("follower_wand", FollowerWandItem::new);
    public static final DeferredHolder<Item, Item> CREATURE_EGG = ITEMS.register("creature_egg", CreatureEggItem::new);
    public static final DeferredHolder<Item, Item> SPORE_BOMB = ITEMS.register("spore_bomb", SporeBombItem::new);
    public static final DeferredHolder<Item, Item> MEAT_PASTE = ITEMS.register("meat_paste", MeatPasteItem::new);
    public static final DeferredHolder<Item, Item> SUBLIMATION = ITEMS.register("sublimation", SublimationItem::new);

    /** 全部饰品 / 材料 / 消耗品，注册顺序与 accessory_catalog.json 一致。 */
    public static final java.util.Map<String, DeferredHolder<Item, Item>> ACCESSORY_ITEMS = new java.util.LinkedHashMap<>();

    static {
        // 物品注册由数据文件驱动：调整数值只需要改 JSON
        org.alku.life_contract.accessory.AccessoryCatalog.load();
        for (org.alku.life_contract.accessory.AccessoryDefinition definition
                : org.alku.life_contract.accessory.AccessoryCatalog.all()) {
            ACCESSORY_ITEMS.put(definition.id(), ITEMS.register(definition.id(),
                    () -> new org.alku.life_contract.accessory.AccessoryItem(definition)));
        }
    }

    public static final DeferredHolder<MenuType<?>, MenuType<TeamInventoryMenu>> TEAM_INVENTORY_MENU =
            MENU_TYPES.register("team_inventory", () -> IMenuTypeExtension.create(TeamInventoryMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<ReviveTeammateMenu>> REVIVE_TEAMMATE_MENU =
            MENU_TYPES.register("revive_teammate", () -> IMenuTypeExtension.create(ReviveTeammateMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MutationMenu>> MUTATION_MENU =
            MENU_TYPES.register("mutation_tree", () -> IMenuTypeExtension.create(MutationMenu::new));

    public static final DeferredHolder<EntityType<?>, EntityType<TeamSentinel>> TEAM_SENTINEL =
            ENTITY_TYPES.register("team_sentinel", () -> EntityType.Builder.of(TeamSentinel::new, MobCategory.MISC)
                    .sized(0.6F, 1.8F)
                    .clientTrackingRange(8)
                    .build("team_sentinel"));

    public static final DeferredHolder<EntityType<?>, EntityType<FireTrailEntity>> FIRE_TRAIL =
            ENTITY_TYPES.register("fire_trail", () -> EntityType.Builder.<FireTrailEntity>of(FireTrailEntity::new, MobCategory.MISC)
                    .sized(0.0F, 0.0F)
                    .clientTrackingRange(8)
                    .updateInterval(1)
                    .build("fire_trail"));

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB =
            CREATIVE_TABS.register("life_contract_tab", () -> CreativeModeTab.builder()
                    .icon(() -> SOUL_CONTRACT.get().getDefaultInstance())
                    .title(Component.translatable("itemGroup.life_contract"))
                    .displayItems((parameters, output) -> {
                        output.accept(SOUL_CONTRACT.get());
                        output.accept(TEAM_ORGANIZER.get());
                        output.accept(FOLLOWER_WAND.get());
                        output.accept(CREATURE_EGG.get());
                        output.accept(SPORE_BOMB.get());
                        output.accept(MEAT_PASTE.get());
                        output.accept(SUBLIMATION.get());
                        for (DeferredHolder<Item, Item> accessory : ACCESSORY_ITEMS.values()) {
                            output.accept(accessory.get());
                        }
                    })
                    .build());

    public Life_contract(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        PlayerUIMenuType.register(WandEggUIHolder.UI_ID, WandEggUIHolder::new);
        PlayerUIMenuType.register(UpgradeHubUIHolder.UI_ID, UpgradeHubUIHolder::new);
        PlayerUIMenuType.register(SublimationShopUIHolder.UI_ID, SublimationShopUIHolder::new);
        PlayerUIMenuType.register(TeamInventoryUIHolder.UI_ID, TeamInventoryUIHolder::new);
        PlayerUIMenuType.register(MutationUIHolder.UI_ID, MutationUIHolder::new);
        if (!org.alku.life_contract.accessory.CuriosCompat.isAvailable()) {
            LOGGER.warn("[饰品] 未检测到 Curios：饰品无法装备，因此不会提供任何加成。"
                    + "请安装 Curios，并把饰品放进对应的饰品栏槽位。");
        }
        Airdrop.register(modEventBus);

        ITEMS.register(modEventBus);
        CREATIVE_TABS.register(modEventBus);
        MENU_TYPES.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlocks.BLOCK_ITEMS.register(modEventBus);
        modEventBus.addListener(NetworkHandler::registerPayloads);
        modEventBus.addListener(ModEvents::onEntityAttributeCreation);
        modEventBus.addListener(ModEvents::onEntityAttributeModification);
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            ModLoadingContext.get().registerExtensionPoint(
                    net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                    () -> (container, parent) -> org.alku.life_contract.client.LifeContractConfigScreen.create(parent));
        }

        @SubscribeEvent
        public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
            event.register(REVIVE_TEAMMATE_MENU.get(), ReviveTeammateScreen::new);
            event.register(MUTATION_MENU.get(), MutationScreen::new);
        }

        @SubscribeEvent
        public static void onRegisterEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerEntityRenderer(Airdrop.AIRDROP_ENTITY.get(), AirdropRenderer::new);
        }
    }

    @EventBusSubscriber(modid = MODID, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void onClientTick(net.neoforged.neoforge.client.event.ClientTickEvent.Post event) {
            if (net.minecraft.client.Minecraft.getInstance().player == null) {
                return;
            }

            while (KeyBindings.OPEN_TEAM_INVENTORY.consumeClick()) {
                NetworkHandler.sendOpenTeamInventoryPacket();
            }
            while (KeyBindings.OPEN_MUTATION_TREE.consumeClick()) {
                NetworkHandler.sendToServer(new org.alku.life_contract.mutation.MutationPackets.Open());
            }
            while (KeyBindings.USE_ACCESSORY_ACTIVE.consumeClick()) {
                NetworkHandler.sendToServer(org.alku.life_contract.accessory.PacketUseAccessoryActive.INSTANCE);
            }
        }

        /**
         * 退出服务器/换世界时清掉客户端缓存。
         * <p>
         * 左侧 HUD 的队友列表来自 {@link ClientDataStorage#PLAYER_DATA_CACHE}，而它是静态的：
         * 不清的话，上一局的队友会一直挂在 HUD 上（看起来就是"更新不及时"）。
         */
        @SubscribeEvent
        public static void onLoggingOut(net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
            ClientDataStorage.clearPlayerData();
            ContractHUD.invalidateCache();
        }
    }

    public static class ModEvents {
        /** 玩家最大生命上限：原版 20 -> 40。 */
        public static final double PLAYER_MAX_HEALTH = 40.0D;
        /**
         * 玩家挖掘速度倍率：原版 1.0 -> 2.5。
         * <p>
         * 原版 {@code Player#getDigSpeed} 最后一步就是
         * {@code f *= getAttributeValue(Attributes.BLOCK_BREAK_SPEED)}，
         * 所以把该属性设成 2.5 就是"所有人的挖掘速度 ×2.5"，
         * 并且和效率附魔、急迫效果按原版规则叠乘。
         */
        public static final double PLAYER_BLOCK_BREAK_SPEED = 2.5D;

        public static void onEntityAttributeCreation(EntityAttributeCreationEvent event) {
            event.put(TEAM_SENTINEL.get(), TeamSentinel.createAttributes().build());
        }

        public static void onEntityAttributeModification(
                net.neoforged.neoforge.event.entity.EntityAttributeModificationEvent event) {
            event.add(EntityType.PLAYER,
                    net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH,
                    PLAYER_MAX_HEALTH);
            event.add(EntityType.PLAYER,
                    net.minecraft.world.entity.ai.attributes.Attributes.BLOCK_BREAK_SPEED,
                    PLAYER_BLOCK_BREAK_SPEED);
        }
    }
}
