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
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.market.BulletShopService;

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

        // ---- 补给 ----
        addSection(productRows, Component.translatable("gui.life_contract.shop.section.supply"));
        addProductRow(productRows, new ItemStack(Airdrop.DISPOSABLE_FLARE_GUN.get()),
                BulletShopService.SIGNAL_GUN_PRICE);
        addProductRow(productRows, new ItemStack(Items.COOKED_BEEF), BulletShopService.COOKED_BEEF_PRICE);

        // ---- TaCZ 弹药（未安装 TaCZ 时列表为空，整个分区不显示）----
        List<ItemStack> taczAmmo = BulletShopService.findTaczAmmoStacks();
        if (!taczAmmo.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.tacz_ammo",
                    BulletShopService.TACZ_AMMO_QUANTITY, BulletShopService.TACZ_AMMO_PRICE));
            for (ItemStack ammo : taczAmmo) {
                addProductRow(productRows, ammo, BulletShopService.TACZ_AMMO_PRICE);
            }
        }

        // ---- 契约模组弹药 ----
        addSection(productRows, Component.translatable("gui.life_contract.shop.section.contract_ammo",
                BulletShopService.getAmmoQuantity(), BulletShopService.getAmmoPrice()));
        List<ItemStack> contractAmmo = BulletShopService.findContractAmmoStacks(player);
        if (contractAmmo.isEmpty()) {
            productRows.addChild(new Label().setValue(Component.translatable("gui.life_contract.shop.no_ammo")));
        } else {
            for (ItemStack ammo : contractAmmo) {
                addProductRow(productRows, ammo, BulletShopService.getAmmoPrice());
            }
        }

        // ---- 羊毛 ----
        List<ItemStack> wool = BulletShopService.findWoolStacks();
        if (!wool.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.wool",
                    BulletShopService.WOOL_QUANTITY, BulletShopService.WOOL_PRICE));
            for (ItemStack color : wool) {
                addProductRow(productRows, color, BulletShopService.WOOL_PRICE);
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

    /** 分区标题（金色）。 */
    private void addSection(UIElement rows, Component text) {
        Label label = new Label().setValue(text.copy().withStyle(ChatFormatting.GOLD));
        label.getLayout().width(205).height(18);
        rows.addChild(label);
    }

    /**
     * 一行商品。
     * <p>
     * 传的是 {@link ItemStack} 模板而不是 {@code Item}：TaCZ 的弹种信息存在物品组件里，
     * 只有带着组件才能兑换出正确的弹药。
     */
    private void addProductRow(UIElement rows, ItemStack template, int price) {
        UIElement row = new UIElement();
        row.getLayout().flexDirection(FlexDirection.ROW).gapAll(6)
                .alignItems(dev.vfyjxf.taffy.style.AlignItems.CENTER);
        Label itemName = new Label().setValue(template.getHoverName());
        itemName.getLayout().width(205).height(24);
        Button buy = new Button().setText(Component.translatable("gui.life_contract.shop.buy_for", price));
        buy.getLayout().width(88).height(24);
        buy.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                serverPlayer.sendSystemMessage(BulletShopService.purchase(serverPlayer, template));
            }
        });
        row.addChildren(itemName, buy);
        rows.addChild(row);
    }
}
