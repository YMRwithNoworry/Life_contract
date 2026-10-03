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
import org.alku.life_contract.accessory.AccessoryItem;
import org.alku.life_contract.accessory.AccessoryState;
import org.alku.life_contract.accessory.AccessoryTooltip;

import java.util.List;
import java.util.Map;

/**
 * 饰品详情卡：把一件饰品的全部机制（常驻 / 情境 / 充能 / 主动技 / 代价 / 共鸣进度）
 * 逐条展开。
 * <p>
 * 尺寸完全跟随父容器（宽高用百分比 + {@code flexGrow}），所以它既能塞进 320 宽的小面板，
 * 也能在大屏上自动铺满，不会把界面撑出屏幕。
 */
public final class AccessoryDetailCard {

    private static final int LINE_HEIGHT = 11;
    /** 实时数据的刷新间隔（tick）。 */
    private static final int REFRESH_TICKS = 10;
    /** 说明文字的行宽：面板按 320 宽设计，这里预留内边距与滚动条。 */
    private static final int TEXT_WIDTH = 268;

    private final UIElement root = new UIElement();
    private final UIElement body = new UIElement();
    private final Label title = new Label();
    private final Player viewer;

    private AccessoryDefinition current;
    private int lastRefreshTick = -1000;

    /** @param onClose 关闭按钮的回调（由商店用来切回商品列表） */
    public AccessoryDetailCard(Player viewer, Runnable onClose) {
        this.viewer = viewer;

        root.getLayout().widthPercent(100).flexGrow(1).paddingAll(8).gapAll(4);
        root.getLayout().flexDirection(FlexDirection.COLUMN);
        root.addClass("panel_bg");

        UIElement header = new UIElement();
        header.getLayout().widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(6);
        header.getLayout().alignItems(AlignItems.CENTER);

        title.getLayout().flexGrow(1).height(LINE_HEIGHT * 2);
        title.setValue(Component.translatable("gui.life_contract.accessory.detail.hint")
                .withStyle(ChatFormatting.DARK_GRAY));

        Button close = new Button().setText(Component.translatable("gui.life_contract.accessory.detail.close"));
        close.getLayout().width(64).height(20);
        close.setOnClick(event -> {
            if (onClose != null) {
                onClose.run();
            }
        });

        header.addChildren(title, close);

        ScrollerView scroller = new ScrollerView();
        scroller.getLayout().widthPercent(100).flexGrow(1);
        body.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(0);
        scroller.viewContainer(container -> container.addChild(body));

        root.addChildren(header, scroller);

        // 充能层数随击杀变化、共鸣进度随换装变化，所以定时重刷
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
                .withStyle(AccessoryItem.tierColor(current.tier())));

        List<Component> lines = AccessoryTooltip.describeForDisplay(
                current, resonanceTiers(), chargeStacks(current));
        for (Component line : lines) {
            Label label = new Label().setValue(line);
            label.getLayout().width(TEXT_WIDTH).height(LINE_HEIGHT);
            body.addChild(label);
        }
    }

    /** 每 10 tick 重刷一次，实时数据（充能层数 / 共鸣进度）才会跟着变。 */
    private void refreshLive() {
        if (current == null || viewer == null || !root.isVisible()) {
            return;
        }
        if (viewer.tickCount - lastRefreshTick < REFRESH_TICKS) {
            return;
        }
        lastRefreshTick = viewer.tickCount;
        rebuild();
    }

    /** 共鸣进度优先用服务端同步值；单人游戏里集成服与客户端同进程，现算也能拿到同样结果。 */
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
}
