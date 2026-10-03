package org.alku.life_contract.items;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class SublimationItem extends Item {
    public static final int MAX_STACK_SIZE = 1024;

    public SublimationItem() {
        super(new Item.Properties());
    }

    @Override
    public int getMaxStackSize(ItemStack stack) {
        return MAX_STACK_SIZE;
    }

    @Override
    public Component getName(ItemStack stack) {
        MutableComponent name = Component.translatable(getDescriptionId(stack));
        return name.setStyle(Style.EMPTY.withColor(animatedGoldColor()));
    }

    /** 名称颜色刷新间隔：getName 在背包/提示框里每帧都会调用，不必每次都算三角函数。 */
    private static final long COLOR_REFRESH_MILLIS = 50L;
    private static long cachedColorMillis = -1L;
    private static int cachedColor = 0xFFBE14;

    private static int animatedGoldColor() {
        long now = System.currentTimeMillis();
        if (now - cachedColorMillis < COLOR_REFRESH_MILLIS) {
            return cachedColor;
        }

        cachedColorMillis = now;
        double phase = now / 700.0;
        int red = 255;
        int green = 190 + (int) Math.round(45.0 * (Math.sin(phase) + 1.0) / 2.0);
        int blue = 20 + (int) Math.round(45.0 * (Math.sin(phase + 2.1) + 1.0) / 2.0);
        cachedColor = red << 16 | green << 8 | blue;
        return cachedColor;
    }
}
