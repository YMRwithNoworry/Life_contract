package org.alku.life_contract.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.border.BorderStatusPayload;

@EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BorderStatusHUD {
    private static final ResourceLocation LAYER_ID =
            ResourceLocation.fromNamespaceAndPath(Life_contract.MODID, "border_status");
    private static boolean active;
    private static boolean inside;
    private static String direction = "";
    private static int distance;

    private BorderStatusHUD() {
    }

    @SubscribeEvent
    public static void registerLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, LAYER_ID, BorderStatusHUD::render);
    }

    public static void update(BorderStatusPayload payload) {
        active = payload.active();
        inside = payload.inside();
        direction = payload.direction();
        distance = payload.distance();
    }

    public static boolean isBorderStatusActive() {
        return active;
    }

    private static void render(GuiGraphics graphics, net.minecraft.client.DeltaTracker partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        if (!active || minecraft.player == null || minecraft.options.hideGui) return;

        Component message = inside
                ? Component.literal("§a✓ 你在预计缩圈范围内")
                : Component.literal("§c✗ 圈外，向" + direction + "进圈 " + distance + " 格");
        int x = graphics.guiWidth() / 2;
        int y = graphics.guiHeight() - 66;
        int width = minecraft.font.width(message);
        graphics.fill(x - width / 2 - 5, y - 3, x + width / 2 + 5, y + 12, 0x99000000);
        graphics.drawCenteredString(minecraft.font, message, x, y, 0xFFFFFF);
    }
}
