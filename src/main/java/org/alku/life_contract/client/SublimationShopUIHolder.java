package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvent;
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
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.NetworkHandler;
import org.alku.life_contract.accessory.AccessoryCatalog;
import org.alku.life_contract.accessory.AccessoryDefinition;
import org.alku.life_contract.market.ShopCatalog;
import org.alku.life_contract.market.ShopCategory;
import org.alku.life_contract.market.ShopFeedbackPayload;
import org.alku.life_contract.market.ShopProduct;
import org.alku.life_contract.market.ShopPurchasePayload;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * 升华商店界面。
 * <p>
 * 按玩家打开商店后的思考顺序组织信息：
 * <ol>
 *     <li><b>我有什么升华</b>：标题右侧常驻余额，兑换后立刻刷新；</li>
 *     <li><b>我能买什么</b>：顶部分类标签（补给/装备/战斗/弹药/枪械/饰品/特殊），
 *         每行左边物品图标、右边"价格 + 兑换"，一眼扫完；</li>
 *     <li><b>买这个有什么用</b>：悬停整行弹出用途说明 + 数量 + 价格；</li>
 *     <li><b>我是否买得起</b>：买不起的行整体变灰、按钮变暗，点了也会告诉你还差多少。</li>
 * </ol>
 * 交互：左键买 1 份，右键一次买 5 份（服务端会按余额与背包空间夹住）；
 * 兑换结果同时显示在界面底部状态行与聊天栏。
 * <p>
 * 界面代码不引用任何客户端专属 API（不使用 {@code Minecraft.getInstance()}），
 * 因此服务端构造同一套 UI 时也不会出错。
 */
public final class SublimationShopUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "sublimation_shop");

    /** 设计尺寸（会被夹进屏幕）。1920x1080 下逻辑分辨率只有 480x270。 */
    private static final int PANEL_WIDTH = 240;
    private static final int PANEL_HEIGHT = 152;
    private static final int PANEL_PADDING = 4;
    private static final int PANEL_GAP = 3;
    /** MC 字体一行的高度。 */
    private static final int LABEL_HEIGHT = 9;
    private static final int TAB_HEIGHT = 14;
    private static final int TAB_GAP = 1;
    private static final int ROW_HEIGHT = 20;
    private static final int ROW_GAP = 2;
    private static final int ICON_SIZE = 18;
    private static final int BUY_BUTTON_WIDTH = 86;
    private static final int FOOTER_HEIGHT = 16;
    /**
     * 列表可视高度：面板高 − 内边距 − 标题 − 状态行 − 分类标签 − 返回按钮 − 五条间距。
     * <p>
     * 写死像素值而不是靠 flexGrow 分剩余空间：flex 项的 min-height 会被内容顶开，
     * 一旦算错就变成"面板被撑破、滑块拖不动"。写死 + {@code minHeight(0)} 之后可视区高度是确定的。
     */
    private static final int SCROLL_HEIGHT = PANEL_HEIGHT - PANEL_PADDING * 2 - LABEL_HEIGHT * 2
            - TAB_HEIGHT - FOOTER_HEIGHT - PANEL_GAP * 5;
    /** 面板整体往上抬的余量（抬升量 = 该值的一半）。 */
    private static final int PANEL_LIFT = 44;
    /** 余额与可购买状态的刷新间隔（tick）。 */
    private static final int REFRESH_TICKS = 5;
    /** 右键一次买几份。 */
    private static final int QUICK_BUNDLES = 5;

    // ==================== 服务端反馈（跨界面实例共享） ====================

    private static String feedbackText = "";
    private static boolean feedbackSuccess = true;
    private static int feedbackRevision;
    private static int lastSeenFeedbackRevision = -1;

    /** 由 {@code ShopFeedbackPayload} 的客户端处理器调用。 */
    public static void showFeedback(ShopFeedbackPayload payload) {
        feedbackText = payload.text();
        feedbackSuccess = payload.success();
        feedbackRevision++;
    }

    private final Player owner;
    private final List<RowBinding> rowBindings = new ArrayList<>();
    private final Map<ShopCategory, Button> tabButtons = new EnumMap<>(ShopCategory.class);

    private ShopCategory selected = ShopCategory.SURVIVAL;
    private Label balanceLabel;
    private Label statusLabel;
    private UIElement rowContainer;
    private ScrollerView listScroller;
    private AccessoryDetailCard detailCard;
    private UIElement detailRoot;
    private int lastRefreshTick = -1000;

    public SublimationShopUIHolder(Player player) {
        owner = player;
    }

    /** 一行商品上需要随余额变化的两个控件。 */
    private record RowBinding(ShopProduct product, Label name, Button buy) {
    }

    @Override
    public ModularUI createUI(Player player) {
        // 外层透明容器只负责把面板抬高（见 PANEL_LIFT），面板本体才是带背景的那块
        UIElement root = new UIElement();
        root.getLayout().width(PANEL_WIDTH).height(PANEL_HEIGHT + PANEL_LIFT);
        root.getLayout().flexDirection(FlexDirection.COLUMN);

        UIElement panel = new UIElement();
        panel.getLayout().widthPercent(100).height(PANEL_HEIGHT);
        panel.getLayout().paddingAll(PANEL_PADDING).gapAll(PANEL_GAP);
        panel.getLayout().flexDirection(FlexDirection.COLUMN);
        panel.addClass("panel_bg");

        panel.addChildren(buildHeader(player), buildTabs(), buildListArea(), buildStatusLine(), buildBackButton());
        root.addChild(panel);

        rebuildRows(player);

        // 余额、可购买状态与服务端反馈都会变，按固定间隔刷新即可
        root.addEventListener(UIEvents.TICK, event -> refresh());
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                UiLayout.fitToScreen(PANEL_WIDTH, PANEL_HEIGHT + PANEL_LIFT)), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }

    // ==================== 结构 ====================

    /** 标题 + 升华余额：打开商店第一眼就该知道自己有多少钱。 */
    private UIElement buildHeader(Player player) {
        UIElement header = new UIElement();
        header.getLayout().widthPercent(100).height(LABEL_HEIGHT);
        header.getLayout().flexDirection(FlexDirection.ROW).alignItems(AlignItems.CENTER);

        Label title = new Label().setValue(
                Component.translatable("gui.life_contract.shop.title").withStyle(ChatFormatting.GOLD));
        title.getLayout().flexGrow(1);
        UiLayout.wrapText(title);

        balanceLabel = new Label().setValue(balanceText(countSublimation(player)));
        UiLayout.wrapText(balanceLabel);

        header.addChildren(title, balanceLabel);
        return header;
    }

    /** 分类标签：每个分类一个按钮，选中的高亮。 */
    private UIElement buildTabs() {
        UIElement tabs = new UIElement();
        tabs.getLayout().widthPercent(100).height(TAB_HEIGHT);
        tabs.getLayout().flexDirection(FlexDirection.ROW).gapAll(TAB_GAP);

        for (ShopCategory category : ShopCategory.values()) {
            Button tab = new Button().setText(Component.literal(""));
            tab.getLayout().flexGrow(1).height(TAB_HEIGHT);
            tab.setOnClick(event -> selectCategory(category));
            tabButtons.put(category, tab);
            tabs.addChild(tab);
        }
        return tabs;
    }

    /** 列表区：商品列表与饰品详情卡共用同一块空间，点详情卡的"关闭"再换回来。 */
    private UIElement buildListArea() {
        UIElement listArea = new UIElement();
        listArea.getLayout().widthPercent(100).height(SCROLL_HEIGHT).flexDirection(FlexDirection.COLUMN);

        listScroller = UiLayout.verticalScroller();
        listScroller.getLayout().widthPercent(100).height(SCROLL_HEIGHT).flexGrow(1);
        rowContainer = new UIElement();
        rowContainer.getLayout().widthPercent(100).flexDirection(FlexDirection.COLUMN).gapAll(ROW_GAP);
        listScroller.viewContainer(container -> container.addChild(rowContainer));

        detailCard = new AccessoryDetailCard(owner, this::showList);
        detailRoot = detailCard.element();
        hide(detailRoot);

        listArea.addChildren(listScroller, detailRoot);
        return listArea;
    }

    private UIElement buildStatusLine() {
        statusLabel = new Label().setValue(Component.translatable("gui.life_contract.shop.hint")
                .withStyle(ChatFormatting.DARK_GRAY));
        statusLabel.getLayout().widthPercent(100).height(LABEL_HEIGHT);
        UiLayout.wrapText(statusLabel);
        return statusLabel;
    }

    private UIElement buildBackButton() {
        Button back = new Button().setText(Component.translatable("gui.life_contract.shop.back"));
        back.getLayout().widthPercent(100).height(FOOTER_HEIGHT);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });
        return back;
    }

    // ==================== 列表 ====================

    private void selectCategory(ShopCategory category) {
        if (selected == category) {
            return;
        }
        selected = category;
        showList();
        rebuildRows(owner);
    }

    private void rebuildRows(Player player) {
        rowContainer.clearAllChildren();
        rowBindings.clear();

        int balance = countSublimation(player);
        String currentSection = null;
        for (ShopProduct product : ShopCatalog.productsFor(player)) {
            if (product.category() != selected) {
                continue;
            }
            if (product.sectionKey() != null && !product.sectionKey().equals(currentSection)) {
                currentSection = product.sectionKey();
                rowContainer.addChild(sectionHeader(currentSection));
            }
            rowContainer.addChild(buildRow(product, balance));
        }

        if (rowBindings.isEmpty()) {
            Label empty = new Label().setValue(Component.translatable("gui.life_contract.shop.empty")
                    .withStyle(ChatFormatting.GRAY));
            empty.getLayout().widthPercent(100);
            UiLayout.wrapText(empty);
            rowContainer.addChild(empty);
        }

        updateTabLabels();
    }

    private UIElement sectionHeader(String sectionKey) {
        Label header = new Label().setValue(Component.translatable(sectionKey).withStyle(ChatFormatting.GOLD));
        header.getLayout().widthPercent(100);
        UiLayout.wrapText(header);
        return header;
    }

    private UIElement buildRow(ShopProduct product, int balance) {
        UIElement row = new UIElement();
        row.getLayout().widthPercent(100).height(ROW_HEIGHT);
        row.getLayout().flexDirection(FlexDirection.ROW).gapAll(PANEL_GAP).alignItems(AlignItems.CENTER);

        Label name = new Label().setValue(nameText(product, balance));
        name.getLayout().flexGrow(1);
        UiLayout.wrapText(name);

        Button buy = new Button().setText(buyText(product, balance));
        buy.getLayout().width(BUY_BUTTON_WIDTH).height(ROW_HEIGHT);
        buy.setOnClick(event -> requestPurchase(product, event.button == 1 ? QUICK_BUNDLES : 1));

        row.addChildren(new ShopIcon(product.template()), name, buy);

        // 悬停整行：用途说明 + 数量 + 价格 + 当前是否买得起
        row.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
            if (event.hoverTooltips == null) {
                return;
            }
            event.hoverTooltips = event.hoverTooltips.append(
                    tooltip(product, countSublimation(owner)).toArray(new Component[0]));
        });

        // 饰品：左键名字打开详情卡（保留原有"查看完整机制"的入口）
        AccessoryDefinition accessory = AccessoryCatalog.of(product.template());
        if (accessory != null) {
            name.addEventListener(UIEvents.CLICK, event -> {
                if (event.button == 0) {
                    showDetail(accessory);
                }
            });
        }

        rowBindings.add(new RowBinding(product, name, buy));
        return row;
    }

    private void showDetail(AccessoryDefinition definition) {
        detailCard.show(definition);
        hide(listScroller);
        show(detailRoot);
    }

    private void showList() {
        hide(detailRoot);
        show(listScroller);
    }

    // ==================== 刷新 ====================

    private void refresh() {
        Player player = owner;
        if (player == null) {
            return;
        }
        if (player.tickCount - lastRefreshTick < REFRESH_TICKS) {
            return;
        }
        lastRefreshTick = player.tickCount;

        int balance = countSublimation(player);
        balanceLabel.setValue(balanceText(balance));
        for (RowBinding binding : rowBindings) {
            binding.name().setValue(nameText(binding.product(), balance));
            binding.buy().setText(buyText(binding.product(), balance));
        }

        if (feedbackRevision != lastSeenFeedbackRevision) {
            lastSeenFeedbackRevision = feedbackRevision;
            statusLabel.setValue(Component.literal((feedbackSuccess ? "§a" : "§c") + feedbackText));
        }
    }

    // ==================== 文案 ====================

    private static Component balanceText(int balance) {
        return Component.translatable("gui.life_contract.shop.balance", balance).withStyle(ChatFormatting.GOLD);
    }

    private static Component nameText(ShopProduct product, int balance) {
        String color = balance >= product.price() ? "§f" : "§8";
        return Component.literal(color + product.displayName() + " §7×" + product.quantity());
    }

    private static Component buyText(ShopProduct product, int balance) {
        String color = balance >= product.price() ? "§e" : "§8";
        return Component.literal(color + product.price() + " §7升华");
    }

    private static List<Component> tooltip(ShopProduct product, int balance) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("§f" + product.displayName() + " §7×" + product.quantity()));
        lines.add(Component.translatable(ShopCatalog.descriptionKey(product.descriptionKey()))
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.literal("§7价格: §e" + product.price() + " §7升华"));
        lines.add(balance >= product.price()
                ? Component.literal("§a可兑换")
                : Component.translatable("gui.life_contract.shop.need_more", product.price() - balance)
                        .withStyle(ChatFormatting.RED));
        lines.add(Component.translatable("gui.life_contract.shop.tooltip_hint").withStyle(ChatFormatting.DARK_GRAY));
        return lines;
    }

    private void updateTabLabels() {
        for (Map.Entry<ShopCategory, Button> entry : tabButtons.entrySet()) {
            boolean active = entry.getKey() == selected;
            entry.getValue().setText(Component.literal(
                    (active ? "§e§l" : "§7") + Component.translatable(entry.getKey().labelKey()).getString()));
        }
    }

    private static void requestPurchase(ShopProduct product, int bundles) {
        NetworkHandler.sendToServer(new ShopPurchasePayload(product.id(), bundles));
    }

    private static int countSublimation(Player player) {
        if (player == null) {
            return 0;
        }
        int count = 0;
        for (ItemStack stack : player.getInventory().items) {
            if (stack.is(Life_contract.SUBLIMATION.get())) {
                count += stack.getCount();
            }
        }
        return count;
    }

    // ==================== 工具 ====================

    /** 从布局与渲染里同时摘掉（{@code setVisible} 只影响绘制，布局要靠 display）。 */
    private static void hide(UIElement element) {
        element.setVisible(false);
        element.setDisplay(TaffyDisplay.NONE);
    }

    private static void show(UIElement element) {
        element.setVisible(true);
        element.setDisplay(TaffyDisplay.FLEX);
    }

    /** 只用来显示图标的物品槽：点它不会把商品"拖"出来。 */
    private static final class ShopIcon extends ItemSlot {
        ShopIcon(ItemStack stack) {
            setItem(stack);
            getLayout().width(ICON_SIZE).height(ICON_SIZE);
            slotStyle(style -> style.showItemTooltips(false));
        }

        @Override
        protected void onMouseDown(UIEvent event) {
            // 图标仅作展示：不响应鼠标，避免被拖进背包
        }
    }
}
