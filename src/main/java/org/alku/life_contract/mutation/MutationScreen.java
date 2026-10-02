package org.alku.life_contract.mutation;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.alku.life_contract.NetworkHandler;

public final class MutationScreen extends AbstractContainerScreen<MutationMenu> {
    private static final int WIDTH = 430;
    private static final int HEIGHT = 244;
    private static final MutationNode[][] COLUMNS = {
            {MutationNode.BLADE, MutationNode.ARMOR, MutationNode.NEST, MutationNode.MARK},
            {MutationNode.SWARM, MutationNode.BEHEMOTH, MutationNode.SENSE, MutationNode.PARASITE},
            {MutationNode.CALAMITY, MutationNode.PURIFICATION, MutationNode.AIRDROP}
    };

    public MutationScreen(MutationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = WIDTH;
        imageHeight = HEIGHT;
    }

    @Override
    protected void init() {
        super.init();
        for (int column = 0; column < COLUMNS.length; column++) {
            int x = 18 + column * 140;
            for (int row = 0; row < COLUMNS[column].length; row++) {
                MutationNode node = COLUMNS[column][row];
                int y = 46 + row * 47;
                int level = menu.levels.get(node);
                int cost = node.costForNext(level);
                boolean locked = menu.total < node.requiredLevels()
                        || node.conflict() != null && menu.levels.get(node.conflict()) > 0;
                String action = cost < 0 ? "已满级" : locked ? "未解锁" : "消耗 " + cost + " MP";
                Component label = Component.literal(node.title + "  " + roman(level) + "/" + roman(node.maxLevel())
                        + "\n" + action);
                Button button = Button.builder(label,
                                ignored -> NetworkHandler.sendToServer(new MutationPackets.Upgrade(node)))
                        .bounds(leftPos + x, topPos + y, 116, 37)
                        .tooltip(Tooltip.create(createTooltip(node, level, locked)))
                        .build();
                button.active = !locked && cost >= 0;
                addRenderableWidget(button);
            }
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, 0xF00E1519);
        graphics.fill(x, y, x + WIDTH, y + 1, 0xFFD7A928);
        graphics.fill(x, y + HEIGHT - 1, x + WIDTH, y + HEIGHT, 0xFFD7A928);
        graphics.fill(x, y, x + 1, y + HEIGHT, 0xFFD7A928);
        graphics.fill(x + WIDTH - 1, y, x + WIDTH, y + HEIGHT, 0xFFD7A928);

        graphics.fill(x + 130, y + 90, x + 160, y + 92, 0x99D7A928);
        graphics.fill(x + 270, y + 90, x + 300, y + 92, 0x99D7A928);
        for (int column = 0; column < 2; column++) {
            int lineX = 18 + column * 140 + 56;
            for (int row = 0; row < 3; row++) {
                int lineY = 46 + row * 47 + 37;
                graphics.fill(x + lineX, y + lineY, x + lineX + 2, y + lineY + 10, 0x99D7A928);
            }
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, "阵营异变树", 12, 7, 0xFFFFD76A, false);
        graphics.drawString(font, "MP（升华）: " + menu.mp + "   已激活: " + menu.total,
                230, 8, 0xFFF3E6B4, false);
        graphics.drawString(font, "T1 基础指令", 20, 28, 0xFF76D5C8, false);
        graphics.drawString(font, "T2 进阶协议 ≥5", 160, 28, 0xFFFFC857, false);
        graphics.drawString(font, "T3 终末天灾 ≥12", 300, 28, 0xFFFF6B55, false);
    }

    private Component createTooltip(MutationNode node, int level, boolean locked) {
        StringBuilder text = new StringBuilder(node.title)
                .append("\n当前等级: ").append(roman(level)).append(" / ").append(roman(node.maxLevel()));
        for (int nextLevel = 1; nextLevel <= node.maxLevel(); nextLevel++) {
            text.append("\nLv.").append(nextLevel).append(" · ").append(node.costs[nextLevel - 1])
                    .append(" MP\n  ").append(node.effectAt(nextLevel));
        }
        if (menu.total < node.requiredLevels()) {
            text.append("\n未解锁：需要已激活词条总等级达到 ").append(node.requiredLevels());
        }
        if (node.conflict() != null && menu.levels.get(node.conflict()) > 0) {
            text.append("\n互斥：已选择“").append(node.conflict().title).append('”');
        }
        if (!locked && node.costForNext(level) >= 0) {
            text.append("\nMP = 玩家背包与队伍背包中的升华总数");
        }
        return Component.literal(text.toString());
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
