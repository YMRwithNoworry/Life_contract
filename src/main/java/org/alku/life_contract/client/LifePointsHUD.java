package org.alku.life_contract.client;

import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.alku.life_contract.ClientDataStorage;
import org.alku.life_contract.Life_contract;

/**
 * 屏幕左下角的剩余生命数 HUD。
 * <p>
 * 数据来自 {@link ClientDataStorage} 里由 {@code PacketSyncLifePoints} 同步的命数，
 * 因此不需要额外的网络包。命数未同步（不在对局中）时整块 HUD 不显示。
 * <p>
 * 心形是用 {@code GuiGraphics.fill} 逐像素画出来的 7x7 图案，
 * 不依赖任何贴图或字体里的特殊字符，因此在任何资源包下都能正常显示。
 */
@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LifePointsHUD {

    private static final ResourceLocation HUD_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "life_points");

    /** 每局默认命数，用于决定画几个"心格"（多出来的命数会额外补格）。 */
    private static final int DEFAULT_LIFE_SLOTS = 5;
    /** 心格上限：命数被指令改成很大时只画这么多，具体数值仍由右侧数字给出。 */
    private static final int MAX_LIFE_SLOTS = 10;

    /** 心形图案：7x7，true 表示填充。 */
    private static final boolean[][] HEART = {
            {false, true, true, false, true, true, false},
            {true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true},
            {true, true, true, true, true, true, true},
            {false, true, true, true, true, true, false},
            {false, false, true, true, true, false, false},
            {false, false, false, true, false, false, false},
    };

    private static final int HEART_SIZE = HEART.length;      // 7
    private static final int HEART_STEP = HEART_SIZE + 2;    // 心与心之间留 2px
    private static final int HEART_FULL = 0xFFE03B3B;
    private static final int HEART_FULL_TOP = 0xFFFF7A7A;
    private static final int HEART_EMPTY = 0xFF3C3C42;
    private static final int PANEL_BG = 0x90101418;
    private static final int PANEL_BORDER = 0x40FFFFFF;
    private static final int LABEL_COLOR = 0xFFCCCCCC;
    private static final int COUNT_COLOR = 0xFFFFFFFF;
    private static final int COUNT_DANGER_COLOR = 0xFFFF5555;
    private static final int OUT_COLOR = 0xFFFF5555;

    /** 独立开关；后续要接指令或配置项时改这里即可。 */
    public static boolean isHudEnabled = true;

    private LifePointsHUD() {
    }

    @SubscribeEvent
    public static void registerLayers(RegisterGuiLayersEvent event) {
        // 画在经验条之后，保证盖在快捷栏上方不被遮住
        event.registerAbove(VanillaGuiLayers.EXPERIENCE_BAR, HUD_ID, LifePointsHUD::renderHud);
    }

    private static void renderHud(GuiGraphics guiGraphics, DeltaTracker partialTick) {
        if (!isHudEnabled) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) {
            return;
        }

        int lives = ClientDataStorage.getLifePointsFor(minecraft.player.getUUID());
        if (lives < 0) {
            // 还没同步到命数（不在对局中），不显示
            return;
        }

        Component label = Component.translatable("hud.life_contract.lives");
        boolean eliminated = lives <= 0;

        int slots = eliminated
                ? DEFAULT_LIFE_SLOTS
                : Math.min(MAX_LIFE_SLOTS, Math.max(DEFAULT_LIFE_SLOTS, lives));
        int heartsWidth = slots * HEART_STEP - 2;
        int labelWidth = minecraft.font.width(label);
        Component countText = eliminated
                ? Component.translatable("hud.life_contract.lives_out")
                : Component.literal(String.valueOf(lives));
        int countWidth = minecraft.font.width(countText);

        int padding = 6;
        int gap = 5;
        int panelHeight = 15;
        int panelLeft = 6;
        int panelRight = panelLeft + padding * 2 + labelWidth + gap + heartsWidth + gap + countWidth;
        int panelBottom = guiGraphics.guiHeight() - 4;
        int panelTop = panelBottom - panelHeight;

        guiGraphics.fill(panelLeft, panelTop, panelRight, panelBottom, PANEL_BG);
        guiGraphics.fill(panelLeft, panelTop, panelRight, panelTop + 1, PANEL_BORDER);

        int textY = panelTop + 3;
        int cursor = panelLeft + padding;
        guiGraphics.drawString(minecraft.font, label, cursor, textY, LABEL_COLOR, true);
        cursor += labelWidth + gap;

        int heartY = textY + 1;
        for (int slot = 0; slot < slots; slot++) {
            boolean filled = !eliminated && slot < lives;
            drawHeart(guiGraphics, cursor + slot * HEART_STEP, heartY, filled);
        }
        cursor += heartsWidth + gap;

        int countColor = eliminated ? OUT_COLOR : (lives <= 1 ? COUNT_DANGER_COLOR : COUNT_COLOR);
        guiGraphics.drawString(minecraft.font, countText, cursor, textY, countColor, true);
    }

    /** 用矩形拼出一颗 7x7 的心；满心带一层高光，空心用暗色描出轮廓。 */
    private static void drawHeart(GuiGraphics guiGraphics, int x, int y, boolean filled) {
        int body = filled ? HEART_FULL : HEART_EMPTY;
        for (int row = 0; row < HEART_SIZE; row++) {
            for (int col = 0; col < HEART_SIZE; col++) {
                if (!HEART[row][col]) {
                    continue;
                }
                int color = filled && row <= 1 && col <= 4 ? HEART_FULL_TOP : body;
                guiGraphics.fill(x + col, y + row, x + col + 1, y + row + 1, color);
            }
        }
    }
}
