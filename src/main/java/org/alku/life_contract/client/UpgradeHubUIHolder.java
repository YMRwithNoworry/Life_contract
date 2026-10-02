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
import org.alku.life_contract.mutation.MutationPackets;

import java.util.List;

public final class UpgradeHubUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID = ResourceLocation.fromNamespaceAndPath("life_contract", "upgrade_hub");

    private final Player owner;

    public UpgradeHubUIHolder(Player player) {
        owner = player;
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(220).paddingAll(10).gapAll(8);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.literal("生灵契约"));
        Button shop = new Button().setText("商店");
        shop.getLayout().width(200).height(30);
        shop.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, SublimationShopUIHolder.UI_ID);
            }
        });

        Button mutation = new Button().setText("升华界面");
        mutation.getLayout().width(200).height(30);
        mutation.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                MutationPackets.open(serverPlayer);
            }
        });

        root.addChildren(title, shop, mutation);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.MC))), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }
}
