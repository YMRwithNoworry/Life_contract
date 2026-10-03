package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.AlignItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.mutation.MutationNode;
import org.alku.life_contract.mutation.MutationService;

import java.util.List;

public final class MutationUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "mutation_tree");

    /** 设计尺寸；会被 UiLayout.fitToScreen 夹进屏幕（最小可用逻辑分辨率约 426x240）。 */
    private static final int PANEL_WIDTH = 232;
    private static final int PANEL_HEIGHT = 144;
    private static final int PANEL_PADDING = 4;
    private static final int PANEL_GAP = 3;
    /** MC 字体一行的高度，用来把列表可视高度算成确定值。 */
    private static final int LABEL_HEIGHT = 9;
    /** 一张卡片占一行的比例：48% × 2 + 间距刚好铺满，并给滚动条留出位置。 */
    private static final float CARD_WIDTH_PERCENT = 48.0F;
    private static final int CARD_GAP = 6;
    /** 按钮要放下「节点名 + Lv.x/y · n 升华」两行。 */
    private static final int CARD_BUTTON_HEIGHT = 26;
    /**
     * 列表可视高度（写死，理由同升华商店：flex 的 min-height 会被内容顶开，
     * 写死 + {@code minHeight(0)} 才能保证滑块一定拖得动）。
     */
    private static final int SCROLL_HEIGHT = PANEL_HEIGHT - PANEL_PADDING * 2
            - LABEL_HEIGHT * 2 - PANEL_GAP * 2;
    /**
     * 面板整体往上抬的余量：外层容器比面板高这么多，界面仍然居中，
     * 面板就自然落在屏幕中心上方（抬升量 = 该值的一半）。
     */
    private static final int PANEL_LIFT = 44;

    private final Player owner;

    public MutationUIHolder(Player player) {
        owner = player;
    }

    @Override
    public ModularUI createUI(Player player) {
        var initialState = player instanceof ServerPlayer serverPlayer
                ? MutationService.state(serverPlayer)
                : null;
        int initialMp = player instanceof ServerPlayer serverPlayer
                ? MutationService.availableMp(serverPlayer)
                : 0;
        int initialTotalLevels = initialState == null ? 0 : initialState.totalLevels();
        // 外层透明容器只负责把面板抬高（见 PANEL_LIFT），面板本体才是带背景的那块
        UIElement root = new UIElement();
        root.getLayout().width(PANEL_WIDTH).height(PANEL_HEIGHT + PANEL_LIFT);
        root.getLayout().flexDirection(FlexDirection.COLUMN);

        UIElement panel = new UIElement();
        panel.getLayout().widthPercent(100).height(PANEL_HEIGHT);
        panel.getLayout().paddingAll(PANEL_PADDING).gapAll(PANEL_GAP);
        panel.getLayout().flexDirection(FlexDirection.COLUMN);
        panel.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.mutations.title"));
        Label balance = new Label().setValue(Component.translatable("gui.life_contract.mutations.balance",
                initialMp, initialTotalLevels));
        for (Label header : List.of(title, balance)) {
            header.getLayout().widthPercent(100);
            UiLayout.wrapText(header);
        }
        ScrollerView scroll = UiLayout.verticalScroller();
        scroll.getLayout().widthPercent(100).height(SCROLL_HEIGHT).flexGrow(1);
        UIElement rows = new UIElement();
        rows.getLayout().widthPercent(100).flexDirection(FlexDirection.COLUMN).gapAll(CARD_GAP);
        scroll.viewContainer(container -> container.addChild(rows));

        UIElement row = null;
        int column = 0;

        for (MutationNode node : MutationNode.values()) {
            if (row == null) {
                row = new UIElement();
                row.getLayout().widthPercent(100).flexDirection(FlexDirection.ROW).gapAll(CARD_GAP);
                row.getLayout().alignItems(AlignItems.START);
            }
            UIElement card = new UIElement();
            card.getLayout().widthPercent(CARD_WIDTH_PERCENT).gapAll(3);
            card.getLayout().flexDirection(FlexDirection.COLUMN);
            Button upgrade = new Button();
            upgrade.getLayout().widthPercent(100).height(CARD_BUTTON_HEIGHT);
            int initialLevel = initialState == null ? 0 : initialState.level(node);
            upgrade.setText(buttonText(node, initialLevel));
            Label effect = new Label().setValue(effectText(node, initialLevel));
            effect.getLayout().widthPercent(100);
            // 卡片只有 127 宽，效果说明交给折行 + 自适应高度，不再手工数格子
            UiLayout.wrapText(effect);
            upgrade.setOnServerClick(event -> {
                if (!(owner instanceof ServerPlayer clicker)) return;
                String result = MutationService.upgrade(clicker, node);
                clicker.sendSystemMessage(Component.literal("§6[异变] §f" + result));
                var state = MutationService.state(clicker);
                upgrade.setText(buttonText(node, state.level(node)));
                effect.setValue(effectText(node, state.level(node)));
                balance.setValue(Component.translatable("gui.life_contract.mutations.balance",
                        MutationService.availableMp(clicker), state.totalLevels()));
            });
            card.addChildren(upgrade, effect);
            row.addChild(card);
            if (++column == 2) {
                rows.addChild(row);
                row = null;
                column = 0;
            }
        }
        if (row != null) rows.addChild(row);

        panel.addChildren(title, balance, scroll);
        root.addChild(panel);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP)),
                UiLayout.fitToScreen(PANEL_WIDTH, PANEL_HEIGHT + PANEL_LIFT)), player);
    }

    private static Component buttonText(MutationNode node, int level) {
        int nextCost = node.costForNext(level);
        String cost = nextCost < 0 ? "MAX" : nextCost + " 升华";
        return Component.literal(node.title + "\nLv." + level + "/" + node.maxLevel() + " · " + cost);
    }

    private static Component effectText(MutationNode node, int level) {
        String text = level >= node.maxLevel() ? "已满级" : node.effectAt(level + 1);
        return Component.literal(text);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }
}
