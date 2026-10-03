package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.ui.UI;
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

    private static int clamp(int design, int available) {
        int usable = available - SCREEN_MARGIN * 2;
        if (usable <= 0) {
            return Math.max(1, available);
        }
        return Math.min(design, usable);
    }
}
