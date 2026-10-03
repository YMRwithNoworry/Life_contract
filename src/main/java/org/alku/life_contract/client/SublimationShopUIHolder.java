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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.accessory.AccessoryCatalog;
import org.alku.life_contract.accessory.AccessoryCategory;
import org.alku.life_contract.accessory.AccessoryDefinition;
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
        root.getLayout().width(330 + AccessoryDetailCard.PANEL_WIDTH + 10).paddingAll(12).gapAll(8);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.shop.title"));
        String contractMod = ContractEvents.getEffectiveContractMod(player);
        Label details = new Label().setValue(contractMod == null || contractMod.isBlank()
                ? Component.translatable("gui.life_contract.shop.no_contract")
                : Component.translatable("gui.life_contract.shop.price", contractMod));
        // 右侧详情卡要在列表构建前就绪：饰品行点击时会引用它
        AccessoryDetailCard detail = new AccessoryDetailCard(player);

        ScrollerView products = new ScrollerView();
        products.getLayout().width(306).height(204);
        UIElement productRows = new UIElement();
        productRows.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(4);
        products.viewContainer(container -> container.addChild(productRows));

        // ---- 补给 ----
        addSection(productRows, Component.translatable("gui.life_contract.shop.section.supply"));
        addProductRow(productRows, new ItemStack(Airdrop.DISPOSABLE_FLARE_GUN.get()),
                BulletShopService.SIGNAL_GUN_PRICE);
        addProductRow(productRows, new ItemStack(Items.COOKED_BEEF), BulletShopService.COOKED_BEEF_PRICE);

        // ---- 手枪（TaCZ；未安装时列表为空，整个分区不显示）----
        List<ItemStack> pistols = BulletShopService.findTaczPistolStacks();
        if (!pistols.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.pistol",
                    BulletShopService.PISTOL_PRICE));
            for (ItemStack pistol : pistols) {
                addProductRow(productRows, pistol, BulletShopService.PISTOL_PRICE);
            }
        }

        // ---- TaCZ 弹药（未安装 TaCZ 时列表为空，整个分区不显示）----
        List<ItemStack> taczAmmo = BulletShopService.findTaczAmmoStacks();
        if (!taczAmmo.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.tacz_ammo",
                    BulletShopService.TACZ_AMMO_QUANTITY, BulletShopService.TACZ_AMMO_PRICE));
            for (ItemStack ammo : taczAmmo) {
                addProductRow(productRows, ammo, BulletShopService.TACZ_AMMO_PRICE);
            }
        }

        // ---- 枪械配件（未安装 TaCZ 时列表为空，整个分区不显示）----
        List<ItemStack> attachments = BulletShopService.findTaczAttachmentStacks();
        if (!attachments.isEmpty()) {
            addSection(productRows, Component.translatable("gui.life_contract.shop.section.attachment",
                    BulletShopService.ATTACHMENT_PRICE));
            for (ItemStack attachment : attachments) {
                addProductRow(productRows, attachment, BulletShopService.ATTACHMENT_PRICE);
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

        // ---- 饰品与材料（数据驱动，来自 accessory_catalog.json）----
        for (AccessoryCategory category : AccessoryCategory.values()) {
            List<AccessoryDefinition> definitions = AccessoryCatalog.byCategory(category);
            if (definitions.isEmpty()) {
                continue;
            }

            int cheapest = Integer.MAX_VALUE;
            for (AccessoryDefinition definition : definitions) {
                cheapest = Math.min(cheapest, definition.price());
            }
            addSection(productRows, Component.translatable(
                    "gui.life_contract.shop.section." + category.id(), cheapest));

            for (AccessoryDefinition definition : definitions) {
                Item item = BuiltInRegistries.ITEM.get(definition.itemId());
                if (item == Items.AIR) {
                    continue;
                }
                addAccessoryRow(productRows, new ItemStack(item), definition, detail);
            }
        }

        Button back = new Button().setText(Component.translatable("gui.life_contract.shop.back"));
        back.getLayout().width(306).height(30);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });

        // ---- 右侧：饰品详情卡（左键点击列表里的饰品时展开它的机制）----
        UIElement left = new UIElement();
        left.getLayout().width(330).flexDirection(FlexDirection.COLUMN).gapAll(8);
        left.addChildren(title, details, products, back);

        UIElement content = new UIElement();
        content.getLayout().flexDirection(FlexDirection.ROW).gapAll(10);
        content.addChildren(left, detail.element());

        root.addChild(content);
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

    /**
     * 饰品行：名称区域<b>左键点击</b>会在右侧详情卡里展开该饰品的完整机制
     * （常驻 / 情境 / 击杀充能 / 主动技 / 代价 / 派系共鸣进度）。
     */
    private void addAccessoryRow(UIElement rows, ItemStack template, AccessoryDefinition definition,
                                 AccessoryDetailCard detail) {
        UIElement row = AccessoryDetailCard.buildRow(
                template.getHoverName(),
                AccessoryDetailCard.NAME_COLUMN_WIDTH - 15,
                Component.translatable("gui.life_contract.shop.buy_for", definition.price()),
                88,
                definition,
                detail,
                buy -> buy.setOnServerClick(event -> {
                    if (owner instanceof ServerPlayer serverPlayer) {
                        serverPlayer.sendSystemMessage(BulletShopService.purchase(serverPlayer, template));
                    }
                }));
        rows.addChild(row);
    }
}
