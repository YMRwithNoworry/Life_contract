package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.accessory.AccessoryClientState;
import org.alku.life_contract.accessory.AccessoryDefinition;
import org.alku.life_contract.accessory.AccessoryEffects;
import org.alku.life_contract.accessory.AccessoryFaction;
import org.alku.life_contract.accessory.AccessoryState;
import org.alku.life_contract.accessory.AccessoryTooltip;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 饰品详情卡：把一件饰品的全部机制（常驻 / 情境 / 充能 / 主动技 / 代价 / 共鸣进度）
 * 逐条展开，显示在商店面板右侧。
 * <p>
 * 列表里的饰品行支持<b>左键点击</b>打开它；打开后每 10 tick 重刷一次，
 * 因此充能层数、共鸣进度这类实时数据也会跟着变。
 */
public final class AccessoryDetailCard {

    /** 详情卡固定宽度，父面板按它预留右侧空间。 */
    public static final int PANEL_WIDTH = 256;
    /** 详情卡高度，与商品列表对齐（一屏刚好放得下最长的条目，更长的可滚动）。 */
    public static final int PANEL_HEIGHT = 288;
    /** 列表里"物品名称"一列的宽度（可点击区域）。 */
    public static final int NAME_COLUMN_WIDTH = 190;

    private static final int LINE_HEIGHT = 11;
    private static final int REFRESH_TICKS = 10;

    private final UIElement root = new UIElement();
    private final UIElement body = new UIElement();
    private final Label title = new Label();
    private final Player viewer;

    private AccessoryDefinition current;
    private int lastRefreshTick = -1000;

    public AccessoryDetailCard(Player viewer) {
        this.viewer = viewer;

        root.getLayout().width(PANEL_WIDTH).height(PANEL_HEIGHT).paddingAll(8).gapAll(4);
        root.getLayout().flexDirection(FlexDirection.COLUMN);
        root.addClass("panel_bg");

        title.getLayout().width(PANEL_WIDTH - 16).height(LINE_HEIGHT * 2);
        title.setValue(Component.translatable("gui.life_contract.accessory.detail.hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        ScrollerView scroller = new ScrollerView();
        scroller.getLayout().width(PANEL_WIDTH - 16).height(PANEL_HEIGHT - 16 - LINE_HEIGHT * 2 - 10);
        body.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(0);
        scroller.viewContainer(container -> container.addChild(body));

        root.addChildren(title, scroller);

        // 实时数据：充能层数随击杀变化，共鸣进度随换装变化
        root.addEventListener(UIEvents.TICK, event -> refreshLive());
    }

    /** 详情卡本体，交给父容器 addChild。 */
    public UIElement element() {
        return root;
    }

    /** 打开某件饰品的详情。 */
    public void show(AccessoryDefinition definition) {
        current = definition;
        lastRefreshTick = -1000;
        rebuild();
    }

    // ==================== 渲染 ====================

    private void rebuild() {
        if (current == null) {
            return;
        }
        body.clearAllChildren();

        title.setValue(Component.translatable(current.nameKey())
                .withStyle(org.alku.life_contract.accessory.AccessoryItem.tierColor(current.tier())));

        List<Component> lines = AccessoryTooltip.describeForDisplay(
                current, resonanceTiers(), chargeStacks(current));
        for (Component line : lines) {
            Label label = new Label().setValue(line);
            label.getLayout().width(PANEL_WIDTH - 26).height(LINE_HEIGHT);
            body.addChild(label);
        }
    }

    /** 每 10 tick 重刷一次，实时数据（充能层数 / 共鸣进度）才会跟着变。 */
    private void refreshLive() {
        if (current == null || viewer == null) {
            return;
        }
        if (viewer.tickCount - lastRefreshTick < REFRESH_TICKS) {
            return;
        }
        lastRefreshTick = viewer.tickCount;
        rebuild();
    }

    /**
     * 共鸣进度来自服务端同步的 {@link AccessoryClientState}。
     * 单人游戏里集成服与客户端同进程，直接现算也能拿到同样的结果。
     */
    private Map<AccessoryFaction, Integer> resonanceTiers() {
        Map<AccessoryFaction, Integer> synced = AccessoryClientState.resonanceTiers();
        if (!synced.isEmpty()) {
            return synced;
        }
        return viewer == null ? Map.of() : AccessoryEffects.activeResonances(viewer);
    }

    /** 充能层数同样优先用同步值；未同步到返回 -1，详情卡就不显示"当前"那一行。 */
    private int chargeStacks(AccessoryDefinition definition) {
        if (definition.charge().isEmpty()) {
            return -1;
        }
        int synced = AccessoryClientState.chargeStacks(definition.id());
        if (synced >= 0) {
            return synced;
        }
        if (viewer instanceof ServerPlayer serverPlayer) {
            return AccessoryState.charge(serverPlayer, definition.id());
        }
        return -1;
    }

    // ==================== 供商店列表复用的行工厂 ====================

    /**
     * 生成一行商品；给 {@code definition} 时这一行的名称区域<b>左键可点</b>，
     * 点击后在 {@code detail} 里展开该饰品的完整机制。
     */
    public static UIElement buildRow(Component name, int nameWidth, Component buyText, int buyWidth,
                                     AccessoryDefinition definition, AccessoryDetailCard detail,
                                     Consumer<Button> buySetup) {
        UIElement row = new UIElement();
        row.getLayout().flexDirection(FlexDirection.ROW).gapAll(6).alignItems(AlignItems.CENTER);

        Label itemName = new Label().setValue(name);
        itemName.getLayout().width(nameWidth).height(24);

        Button buy = new Button().setText(buyText);
        buy.getLayout().width(buyWidth).height(24);
        if (buySetup != null) {
            buySetup.accept(buy);
        }

        row.addChildren(itemName, buy);

        if (definition != null) {
            // 悬停：直接在光标旁弹出完整机制说明（沿用物品 tooltip 的组装逻辑）
            itemName.addEventListener(UIEvents.HOVER_TOOLTIPS, event -> {
                if (event.hoverTooltips == null) {
                    return;
                }
                List<Component> lines = AccessoryTooltip.describe(definition);
                event.hoverTooltips = event.hoverTooltips.append(lines.toArray(new Component[0]));
            });

            // 左键：在右侧详情卡里展开，方便对照比较几件饰品
            if (detail != null) {
                itemName.addEventListener(UIEvents.CLICK, event -> {
                    if (event.button == 0) {
                        detail.show(definition);
                    }
                });
            }
        }
        return row;
    }
}
