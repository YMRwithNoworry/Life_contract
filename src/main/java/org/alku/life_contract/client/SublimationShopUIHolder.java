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
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.market.BulletShopService;
import net.minecraft.world.item.Items;

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
        root.getLayout().width(330).paddingAll(12).gapAll(8);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.shop.title"));
        String contractMod = ContractEvents.getEffectiveContractMod(player);
        Label details = new Label().setValue(contractMod == null || contractMod.isBlank()
                ? Component.translatable("gui.life_contract.shop.no_contract")
                : Component.translatable("gui.life_contract.shop.price", contractMod));
        ScrollerView products = new ScrollerView();
        products.getLayout().width(306).height(174);
        UIElement productRows = new UIElement();
        productRows.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(4);
        products.viewContainer(container -> container.addChild(productRows));

        addProductRow(productRows, Airdrop.DISPOSABLE_FLARE_GUN.get(),
                Component.translatable("gui.life_contract.shop.buy_for", BulletShopService.SIGNAL_GUN_PRICE));
        addProductRow(productRows, Items.COOKED_BEEF,
                Component.translatable("gui.life_contract.shop.buy_for", BulletShopService.COOKED_BEEF_PRICE));

        var ammoItems = BulletShopService.findAmmoItems(player);
        if (ammoItems.isEmpty()) {
            productRows.addChild(new Label().setValue(Component.translatable("gui.life_contract.shop.no_ammo")));
        } else {
            for (Item item : ammoItems) {
                addProductRow(productRows, item, Component.translatable("gui.life_contract.shop.buy"));
            }
        }

        Button back = new Button().setText(Component.translatable("gui.life_contract.shop.back"));
        back.getLayout().width(306).height(30);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });

        root.addChildren(title, details, products, back);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP))), player);
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }

    private void addProductRow(UIElement rows, Item item, Component buttonText) {
        UIElement row = new UIElement();
        row.getLayout().flexDirection(FlexDirection.ROW).gapAll(6)
                .alignItems(dev.vfyjxf.taffy.style.AlignItems.CENTER);
        Label itemName = new Label().setValue(item.getDefaultInstance().getHoverName());
        itemName.getLayout().width(205).height(24);
        Button buy = new Button().setText(buttonText);
        buy.getLayout().width(88).height(24);
        buy.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(BulletShopService.purchase(serverPlayer, item));
            }
        });
        row.addChildren(itemName, buy);
        rows.addChild(row);
    }
}
