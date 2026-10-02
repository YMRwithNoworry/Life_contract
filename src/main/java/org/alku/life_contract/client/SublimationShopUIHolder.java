package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.List;

public final class SublimationShopUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "sublimation_shop");

    private final Player owner;

    public SublimationShopUIHolder(Player player) {
        owner = player;
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(220).paddingAll(10).gapAll(8);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.literal("商店"));
        Label contents = new Label().setValue(Component.literal("商品尚未配置"));
        Button back = new Button().setText("返回");
        back.getLayout().width(200).height(30);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });

        root.addChildren(title, contents, back);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC))), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }
}
