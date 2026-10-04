package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollDisplay;
import com.lowdragmc.lowdraglib2.gui.ui.data.ScrollerMode;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ScrollerView;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.math.Size;

/**
 * 界面尺寸自适应。
 * <p>
 * lowdraglib2 的 {@link UI.DynamicSizeProvider} 会在界面初始化时拿到当前屏幕的
 * <b>逻辑</b>尺寸（{@code ModularUI#init} 内部调用它，并把返回值写进根元素的宽高），
 * 所以这里统一用它把界面夹进屏幕内。
 * <p>
 * 这一步是必须的：GUI 缩放是按物理分辨率自动选的，例如 1920x1080 下自动缩放为 4，
 * 逻辑分辨率只有 {@code 480x270}；界面只要比它宽/高就会直接跑到屏幕外。
 */
public final class UiLayout {

    /** 界面四周至少留出的边距（逻辑像素）。 */
    private static final int SCREEN_MARGIN = 12;

    private UiLayout() {
    }

    /** 设计尺寸与屏幕可用尺寸取较小值，保证界面永远不出屏。 */
    public static UI.DynamicSizeProvider fitToScreen(int designWidth, int designHeight) {
        return available -> Size.of(
                clamp(designWidth, available.getWidth()),
                clamp(designHeight, available.getHeight()));
    }

    /**
     * 铺满整个屏幕可用区域（最大化）。
     * <p>
     * 与 {@link #fitToScreen} 相反：不做任何收缩，界面就等于屏幕的逻辑尺寸，
     * 内部再靠百分比 + {@code flexGrow} 分区伸展。列表类界面用它最合适——
     * 屏幕有多高就显示多少行，超出的部分交给滚动条，不会有内容被窗口下边缘裁掉。
     */
    public static UI.DynamicSizeProvider fillScreen() {
        return available -> Size.of(
                Math.max(1, available.getWidth()),
                Math.max(1, available.getHeight()));
    }

    /**
     * 纵向滚动视图，并<b>常显滑块</b>。
     * <p>
     * lowdraglib2 的默认值是 {@link ScrollDisplay#AUTO}：只有正在滚动的那几帧才画出滑块，
     * 玩家根本看不出列表还能往下翻。这里统一改成常显，同时关掉横向滚动
     * （横向滚动条会额外吃掉高度，而这两个界面都是单列布局）。
     */
    public static ScrollerView verticalScroller() {
        ScrollerView view = new ScrollerView();
        // flex 项默认 min-height:auto —— 不给 0 的话，列表会被内容撑到比面板还高，
        // 结果就是"滚不动 + 内容溢出面板画到屏幕外"。这一步是滚动能生效的前提。
        view.getLayout().minHeight(0);
        view.scrollerStyle(style -> style
                .mode(ScrollerMode.VERTICAL)
                .verticalScrollDisplay(ScrollDisplay.ALWAYS));
        return view;
    }

    /**
     * 让文字在元素宽度内自动折行、高度随行数自适应。
     * <p>
     * 窄面板下这一条是必须的：{@link TextWrap#NONE}（默认）会把超宽的文字直接裁掉。
     * 调用方只需要再设置宽度（{@code width(...)} / {@code widthPercent(100)}）。
     */
    public static void wrapText(TextElement element) {
        element.textStyle(style -> style.textWrap(TextWrap.WRAP).adaptiveHeight(true));
    }

    private static int clamp(int design, int available) {
        int usable = available - SCREEN_MARGIN * 2;
        if (usable <= 0) {
            return Math.max(1, available);
        }
        return Math.min(design, usable);
    }
}
