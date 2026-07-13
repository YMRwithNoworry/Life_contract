package org.alku.life_contract.mutation;

import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.alku.life_contract.NetworkHandler;

public final class MutationUi {
    public static final int WIDTH = 430;
    public static final int HEIGHT = 244;
    private static final IGuiTexture BACKGROUND = GuiTextureGroup.of(
            new ColorRectTexture(0xF00E1519),
            new ColorBorderTexture(1, 0xFFD7A928));

    private MutationUi() {
    }

    public static ModularUI create(MutationMenu menu, Player player) {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(BACKGROUND));

        label(root, "阵营异变树", 12, 7, 180, 14, 0xFFFFD76A, 12);
        label(root, "MP（升华）: " + menu.mp + "   已激活: " + menu.total,
                230, 8, 190, 12, 0xFFF3E6B4, 9);
        label(root, "T1 基础指令", 20, 28, 110, 12, 0xFF76D5C8, 9);
        label(root, "T2 进阶协议  ≥5", 160, 28, 120, 12, 0xFFFFC857, 9);
        label(root, "T3 终末天灾  ≥12", 300, 28, 120, 12, 0xFFFF6B55, 9);

        MutationNode[][] columns = {
                {MutationNode.BLADE, MutationNode.ARMOR, MutationNode.NEST, MutationNode.MARK},
                {MutationNode.SWARM, MutationNode.BEHEMOTH, MutationNode.SENSE, MutationNode.PARASITE},
                {MutationNode.CALAMITY, MutationNode.PURIFICATION, MutationNode.AIRDROP}
        };
        for (int column = 0; column < columns.length; column++) {
            int x = 18 + column * 140;
            for (int row = 0; row < columns[column].length; row++) {
                MutationNode node = columns[column][row];
                int y = 46 + row * 47;
                if (column < 2 && row < columns[column].length - 1) {
                    line(root, x + 56, y + 37, 2, 10);
                }
                node(root, menu, node, x, y);
            }
        }
        line(root, 130, 91, 30, 2);
        line(root, 270, 91, 30, 2);
        return ModularUI.of(UI.of(root), player);
    }

    private static void node(UIElement root, MutationMenu menu, MutationNode node, int x, int y) {
        int level = menu.levels.get(node);
        int cost = node.costForNext(level);
        boolean locked = menu.total < node.requiredLevels()
                || node.conflict() != null && menu.levels.get(node.conflict()) > 0;
        String action = cost < 0 ? "已满级" : locked ? "未解锁" : "消耗 " + cost + " MP";

        Button button = new Button()
                .setText(node.title + "  " + roman(level) + "/" + roman(node.maxLevel()) + "\n" + action);
        button.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(116)
                .height(37));
        button.style(style -> style.backgroundTexture(GuiTextureGroup.of(
                new ColorRectTexture(locked ? 0xFF24282B : 0xFF17282B),
                new ColorBorderTexture(1, locked ? 0xFF555555 : 0xFFD7A928))));
        button.textStyle(style -> style
                .textColor(locked ? 0xFF888888 : 0xFFFFE59A)
                .fontSize(7));
        button.setOnClick(event -> NetworkHandler.CHANNEL.sendToServer(new MutationPackets.Upgrade(node)));
        button.addEventListener(UIEvents.HOVER_TOOLTIPS,
                event -> event.hoverTooltips = nodeTooltips(menu, node, level, locked));
        root.addChild(button);
    }

    private static HoverTooltips nodeTooltips(
            MutationMenu menu,
            MutationNode node,
            int currentLevel,
            boolean locked) {
        HoverTooltips tooltips = HoverTooltips.empty().append(
                Component.literal(node.title).withStyle(ChatFormatting.GOLD),
                Component.literal("当前等级: " + roman(currentLevel) + " / " + roman(node.maxLevel()))
                        .withStyle(ChatFormatting.GRAY));

        for (int level = 1; level <= node.maxLevel(); level++) {
            ChatFormatting levelColor = level <= currentLevel
                    ? ChatFormatting.GREEN
                    : level == currentLevel + 1 && !locked ? ChatFormatting.YELLOW : ChatFormatting.GRAY;
            tooltips = tooltips.append(
                    Component.literal("Lv." + level + " · " + node.costs[level - 1] + " MP")
                            .withStyle(levelColor),
                    Component.literal("  " + node.effectAt(level)).withStyle(ChatFormatting.WHITE));
        }

        if (menu.total < node.requiredLevels()) {
            tooltips = tooltips.append(Component.literal(
                    "未解锁：当前已激活词条总等级需要达到 " + node.requiredLevels())
                    .withStyle(ChatFormatting.RED));
        }
        if (node.conflict() != null && menu.levels.get(node.conflict()) > 0) {
            tooltips = tooltips.append(Component.literal("互斥：已选择“" + node.conflict().title + "”")
                    .withStyle(ChatFormatting.RED));
        }
        return tooltips.append(Component.literal("MP = 玩家背包与队伍背包中的升华总数")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static void label(
            UIElement root,
            String text,
            int x,
            int y,
            int width,
            int height,
            int color,
            float fontSize) {
        Label label = new Label();
        label.setText(Component.literal(text));
        label.layout(layout -> layout
                .positionType(TaffyPosition.ABSOLUTE)
                .left(x)
                .top(y)
                .width(width)
                .height(height));
        label.textStyle(style -> style
                .textColor(color)
                .fontSize(fontSize)
                .textAlignHorizontal(Horizontal.LEFT));
        label.setAllowHitTest(false);
        root.addChild(label);
    }

    private static void line(UIElement root, int x, int y, int width, int height) {
        root.addChild(new UIElement()
                .layout(layout -> layout
                        .positionType(TaffyPosition.ABSOLUTE)
                        .left(x)
                        .top(y)
                        .width(width)
                        .height(height))
                .style(style -> style.backgroundTexture(new ColorRectTexture(0x99D7A928)))
                .setAllowHitTest(false));
    }

    private static String roman(int number) {
        return switch (number) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            default -> "0";
        };
    }
}
