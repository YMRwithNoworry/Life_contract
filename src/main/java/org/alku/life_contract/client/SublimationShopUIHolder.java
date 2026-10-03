package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.accessory.AccessoryCatalog;
import org.alku.life_contract.accessory.AccessoryCategory;
import org.alku.life_contract.accessory.AccessoryDefinition;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.market.BulletShopService;

import java.util.List;

/**
 * 升华商店。
 * <p>
 * 界面按 <b>224x152</b> 设计，并用 {@link UiLayout#fitToScreen} 夹进屏幕内：
 * GUI 缩放是自动按物理分辨率选的（1920x1080 下逻辑分辨率只有 480x270），
 * 界面比它大就会跑到屏幕外。
 * <p>
 * 面板只有屏幕宽度的三分之一左右，商品行、分区标题与详情卡文字全部交给
 * {@link UiLayout#wrapText} 自动折行，纵向列表交给 {@link UiLayout#verticalScroller}（常显滑块）。
 * <p>
 * 商品列表与饰品详情卡<b>共用同一块区域</b>：左键点击饰品时把列表换成详情卡，
 * 点详情卡上的关闭按钮再换回来 —— 这样面板不需要左右分栏，宽度才能压到 224。
 */
public final class SublimationShopUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "sublimation_shop");

    /** 设计尺寸（会被夹进屏幕）。 */
    private static final int PANEL_WIDTH = 224;
    private static final int PANEL_HEIGHT = 152;
    private static final int PANEL_PADDING = 6;
    private static final int PANEL_GAP = 4;
    /** 兑换按钮宽度；商品名占满剩下的宽度（flexGrow），窄面板下不会把按钮挤出面板。 */
    private static final int ROW_BUTTON_WIDTH = 80;
    private static final int ROW_HEIGHT = 18;
    private static final int ROW_GAP = 3;
    private static final int FOOTER_HEIGHT = 18;

    private final Player owner;

    public SublimationShopUIHolder(Player player) {
        owner = player;
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(PANEL_WIDTH).paddingAll(PANEL_PADDING).gapAll(PANEL_GAP);
        root.getLayout().flexDirection(FlexDirection.COLUMN);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.shop.title"));
        String contractMod = ContractEvents.getEffectiveContractMod(player);
        Label details = new Label().setValue(contractMod == null || contractMod.isBlank()
                ? Component.translatable("gui.life_contract.shop.no_contract")
                : Component.translatable("gui.life_contract.shop.price", contractMod));
        for (Label header : List.of(title, details)) {
            header.getLayout().widthPercent(100);
            UiLayout.wrapText(header);
        }

        ScrollerView products = UiLayout.verticalScroller();
        products.getLayout().widthPercent(100).flexGrow(1);
        UIElement productRows = new UIElement();
        productRows.getLayout().widthPercent(100).flexDirection(FlexDirection.COLUMN).gapAll(ROW_GAP);
        products.viewContainer(container -> container.addChild(productRows));

        // ---- 详情卡与商品列表共用同一块区域，左键点击饰品时互换显示 ----
        Runnable[] showList = new Runnable[1];
        AccessoryDetailCard detail = new AccessoryDetailCard(player, () -> showList[0].run());
        UIElement detailRoot = detail.element();
        hide(detailRoot);

        showList[0] = () -> {
            hide(detailRoot);
            show(products);
        };
        Runnable showDetail = () -> {
            hide(products);
            show(detailRoot);
        };

        // ---- 补给 ----
        addSection(productRows, Component.translatable("gui.life_contract.shop.section.supply"));
        addProductRow(productRows, new ItemStack(Airdrop.DISPOSABLE_FLARE_GUN.get()),
                BulletShopService.SIGNAL_GUN_PRICE);
        addProductRow(productRows, new ItemStack(Items.COOKED_BEEF), BulletShopService.COOKED_BEEF_PRICE);

        // ---- 手枪（TaCZ；未安装时列表为空，整个分区不显示）----
        List<ItemStack> pistols = BulletShopService.findTaczPistolStacks();
        if (!pistols.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.pistol",
                    BulletShopService.PISTOL_PRICE));
            for (ItemStack pistol : pistols) {
                addProductRow(productRows, pistol, BulletShopService.PISTOL_PRICE);
            }
        }

        // ---- TaCZ 弹药（未安装 TaCZ 时列表为空，整个分区不显示）----
        List<ItemStack> taczAmmo = BulletShopService.findTaczAmmoStacks();
        if (!taczAmmo.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.tacz_ammo",
                    BulletShopService.TACZ_AMMO_QUANTITY, BulletShopService.TACZ_AMMO_PRICE));
            for (ItemStack ammo : taczAmmo) {
                addProductRow(productRows, ammo, BulletShopService.TACZ_AMMO_PRICE);
            }
        }

        // ---- 枪械配件（未安装 TaCZ 时列表为空，整个分区不显示）----
        List<ItemStack> attachments = BulletShopService.findTaczAttachmentStacks();
        if (!attachments.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.attachment",
                    BulletShopService.ATTACHMENT_PRICE));
            for (ItemStack attachment : attachments) {
                addProductRow(productRows, attachment, BulletShopService.ATTACHMENT_PRICE);
            }
        }

        // ---- 契约模组弹药 ----
        addSection(productRows, Component.translatable("gui.life_contract.shop.section.contract_ammo",
                BulletShopService.getAmmoQuantity(), BulletShopService.getAmmoPrice()));
        List<ItemStack> contractAmmo = BulletShopService.findContractAmmoStacks(player);
        if (contractAmmo.isEmpty()) {
            productRows.addChild(new Label().setValue(Component.translatable("gui.life_contract.shop.no_ammo")));
        } else {
            for (ItemStack ammo : contractAmmo) {
                addProductRow(productRows, ammo, BulletShopService.getAmmoPrice());
            }
        }

        // ---- 羊毛 ----
        List<ItemStack> wool = BulletShopService.findWoolStacks();
        if (!wool.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.wool",
                    BulletShopService.WOOL_QUANTITY, BulletShopService.WOOL_PRICE));
            for (ItemStack color : wool) {
                addProductRow(productRows, color, BulletShopService.WOOL_PRICE);
            }
        }

        // ---- 饰品与材料（数据驱动，来自 accessory_catalog.json）----
        for (AccessoryCategory category : AccessoryCategory.values()) {
            List<AccessoryDefinition> definitions = AccessoryCatalog.byCategory(category);
            if (definitions.isEmpty()) {
                continue;
            }

            int cheapest = Integer.MAX_VALUE;
            for (AccessoryDefinition definition : definitions) {
                cheapest = Math.min(cheapest, definition.price());
            }
            addSection(productRows, Component.translatable(
                    "gui.life_contract.shop.section." + category.id(), cheapest));

            for (AccessoryDefinition definition : definitions) {
                Item item = BuiltInRegistries.ITEM.get(definition.itemId());
                if (item == Items.AIR) {
                    continue;
                }
                addAccessoryRow(productRows, new ItemStack(item), definition, detail, showDetail);
            }
        }

        Button back = new Button().setText(Component.translatable("gui.life_contract.shop.back"));
        back.getLayout().widthPercent(100).height(FOOTER_HEIGHT);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });

        root.addChildren(title, details, products, detailRoot, back);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                UiLayout.fitToScreen(PANEL_WIDTH, PANEL_HEIGHT)), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }

    /** 从布局与渲染里同时摘掉（{@code setVisible} 只影响绘制，布局要靠 display）。 */
    private static void hide(UIElement element) {
        element.setVisible(false);
        element.setDisplay(TaffyDisplay.NONE);
    }

    private static void show(UIElement element) {
        element.setVisible(true);
        element.setDisplay(TaffyDisplay.FLEX);
    }

    /** 分区标题（金色）。长标题（例如"契约模组弹药（5 发 / 30 升华）"）会自己折行。 */
    private void addSection(UIElement rows, Component text) {
        Label label = new Label().setValue(text.copy().withStyle(ChatFormatting.GOLD));
        label.getLayout().widthPercent(100);
        UiLayout.wrapText(label);
        rows.addChild(label);
    }

    /**
     * 一行商品。
     * <p>
     * 传的是 {@link ItemStack} 模板而不是 {@code Item}：TaCZ 的弹种信息存在物品组件里，
     * 只有带着组件才能兑换出正确的弹药。
     */
    private void addProductRow(UIElement rows, ItemStack template, int price) {
        UIElement row = new UIElement();
        row.getLayout().widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(PANEL_GAP);
        row.getLayout().alignItems(AlignItems.CENTER);
        Label itemName = new Label().setValue(template.getHoverName());
        itemName.getLayout().flexGrow(1);
        UiLayout.wrapText(itemName);
        Button buy = new Button().setText(Component.translatable("gui.life_contract.shop.buy_for", price));
        buy.getLayout().width(ROW_BUTTON_WIDTH).height(ROW_HEIGHT);
        buy.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(BulletShopService.purchase(serverPlayer, template));
            }
        });
        row.addChildren(itemName, buy);
        rows.addChild(row);
    }

    /**
     * 饰品行：名称区域<b>悬停</b>弹出完整机制说明，<b>左键点击</b>把列表换成右侧详情卡
     * （常驻 / 情境 / 击杀充能 / 主动技 / 代价 / 派系共鸣进度）。
     */
    private void addAccessoryRow(UIElement rows, ItemStack template, AccessoryDefinition definition,
                                 AccessoryDetailCard detail, Runnable showDetail) {
        UIElement row = new UIElement();
        row.getLayout().widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(PANEL_GAP);
        row.getLayout().alignItems(AlignItems.CENTER);

        Label itemName = new Label().setValue(template.getHoverName());
        itemName.getLayout().flexGrow(1);
        UiLayout.wrapText(itemName);

        Button buy = new Button().setText(Component.translatable("gui.life_contract.shop.buy_for", definition.price()));
        buy.getLayout().width(ROW_BUTTON_WIDTH).height(ROW_HEIGHT);
        buy.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(BulletShopService.purchase(serverPlayer, template));
            }
        });

        // 悬停：直接在光标旁弹出完整机制说明（沿用物品 tooltip 的组装逻辑）
        itemName.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (event.hoverTooltips == null) {
                return;
            }
            List<Component> lines = org.alku.life_contract.accessory.AccessoryTooltip.describe(definition);
            event.hoverTooltips = event.hoverTooltips.append(lines.toArray(new Component[0]));
        });

        // 左键：把商品列表换成详情卡
        itemName.addEventListener(UIEvents.CLICK, event -> {
            if (event.button == 0) {
                detail.show(definition);
                showDetail.run();
            }
        });

        row.addChildren(itemName, buy);
        rows.addChild(row);
    }
}
