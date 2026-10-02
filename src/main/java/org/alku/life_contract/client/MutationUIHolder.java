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
        UIElement root = new UIElement();
        root.getLayout().width(450).height(330).paddingAll(10).gapAll(6);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.mutations.title"));
        Label balance = new Label().setValue(Component.translatable("gui.life_contract.mutations.balance",
                initialMp, initialTotalLevels));
        ScrollerView scroll = new ScrollerView();
        scroll.getLayout().width(426).height(276);
        UIElement rows = new UIElement();
        rows.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(8);
        scroll.viewContainer(container -> container.addChild(rows));

        UIElement row = null;
        int column = 0;

        for (MutationNode node : MutationNode.values()) {
            if (row == null) {
                row = new UIElement();
                row.getLayout().flexDirection(FlexDirection.ROW).gapAll(8).alignItems(AlignItems.START);
            }
            UIElement card = new UIElement();
            card.getLayout().width(209).gapAll(3);
            card.getLayout().flexDirection(FlexDirection.COLUMN);
            Button upgrade = new Button();
            upgrade.getLayout().width(209).height(38);
            int initialLevel = initialState == null ? 0 : initialState.level(node);
            upgrade.setText(buttonText(node, initialLevel));
            Label effect = new Label().setValue(effectText(node, initialLevel));
            effect.getLayout().width(205).height(54);
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

        root.addChildren(title, balance, scroll);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP))), player);
    }

    private static Component buttonText(MutationNode node, int level) {
        int nextCost = node.costForNext(level);
        String cost = nextCost < 0 ? "MAX" : nextCost + " 升华";
        return Component.literal(node.title + "\nLv. " + level + "/" + node.maxLevel() + " · " + cost);
    }

    private static Component effectText(MutationNode node, int level) {
        String text = level >= node.maxLevel() ? "已满级" : node.effectAt(level + 1);
        StringBuilder wrapped = new StringBuilder();
        int lineLength = 0;
        for (int index = 0; index < text.length(); index++) {
            char character = text.charAt(index);
            if (lineLength >= 22 && character != ' ' && character != '，' && character != '、') {
                wrapped.append('\n');
                lineLength = 0;
            }
            wrapped.append(character);
            lineLength = character == '\n' ? 0 : lineLength + 1;
        }
        return Component.literal(wrapped.toString());
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }
}
