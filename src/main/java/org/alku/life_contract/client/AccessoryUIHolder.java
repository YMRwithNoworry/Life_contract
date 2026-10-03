package org.alku.life_contract.client;

import com.lowdragmc.lowdraglib2.gui.factory.PlayerUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import dev.vfyjxf.taffy.style.FlexDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.accessory.AccessoryCatalog;
import org.alku.life_contract.accessory.AccessoryCategory;
import org.alku.life_contract.accessory.AccessoryDefinition;
import org.alku.life_contract.accessory.AccessorySlots;

import java.util.List;

/** 饰品栏界面：五个佩戴槽位，点按钮从背包里装备/卸下。 */
public final class AccessoryUIHolder implements PlayerUIMenuType.PlayerUIHolder {
    public static final ResourceLocation UI_ID =
            ResourceLocation.fromNamespaceAndPath("life_contract", "accessory_slots");

    private final Player owner;

    public AccessoryUIHolder(Player player) {
        owner = player;
    }

    @Override
    public ModularUI createUI(Player player) {
        UIElement root = new UIElement();
        root.getLayout().width(340).paddingAll(12).gapAll(8);
        root.addClass("panel_bg");

        Label title = new Label().setValue(Component.translatable("gui.life_contract.accessory.title")
                .withStyle(ChatFormatting.GOLD));
        Label hint = new Label().setValue(Component.translatable("gui.life_contract.accessory.hint")
                .withStyle(ChatFormatting.GRAY));
        hint.getLayout().width(316).height(24);

        UIElement rows = new UIElement();
        rows.getLayout().flexDirection(FlexDirection.COLUMN).gapAll(4);
        for (AccessoryCategory category : AccessoryCategory.values()) {
            if (!category.isEquippable()) {
                continue;
            }
            rows.addChild(buildRow(category));
        }

        Button back = new Button().setText(Component.translatable("gui.life_contract.accessory.back"));
        back.getLayout().width(316).height(28);
        back.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                PlayerUIMenuType.openUI(serverPlayer, UpgradeHubUIHolder.UI_ID);
            }
        });

        root.addChildren(title, hint, rows, back);
        return new ModularUI(UI.of(root,
                List.of(StylesheetManager.INSTANCE.getStylesheetSafe(StylesheetManager.GDP))), player);
    }

    private UIElement buildRow(AccessoryCategory category) {
        UIElement row = new UIElement();
        row.getLayout().flexDirection(FlexDirection.ROW).gapAll(6)
                .alignItems(dev.vfyjxf.taffy.style.AlignItems.CENTER);

        ItemStack equipped = AccessorySlots.get(owner, category);
        String equippedName = equipped.isEmpty()
                ? Component.translatable("gui.life_contract.accessory.empty_slot").getString()
                : equipped.getHoverName().getString();

        Label name = new Label().setValue(Component.translatable("gui.life_contract.accessory.slot",
                Component.translatable("gui.life_contract.accessory.category." + category.id()),
                equippedName));
        name.getLayout().width(216).height(24);

        Button equip = new Button().setText(Component.translatable("gui.life_contract.accessory.equip"));
        equip.getLayout().width(46).height(24);
        equip.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                equipBest(serverPlayer, category);
            }
        });

        Button unequip = new Button().setText(Component.translatable("gui.life_contract.accessory.unequip"));
        unequip.getLayout().width(46).height(24);
        unequip.setOnServerClick(event -> {
            if (owner instanceof ServerPlayer serverPlayer) {
                ItemStack removed = AccessorySlots.unequip(serverPlayer, category);
                if (removed.isEmpty()) {
                    serverPlayer.sendSystemMessage(
                            Component.translatable("gui.life_contract.accessory.slot_empty"));
                } else {
                    serverPlayer.sendSystemMessage(Component.translatable(
                            "gui.life_contract.accessory.unequipped", removed.getHoverName()));
                }
            }
        });

        row.addChildren(name, equip, unequip);
        return row;
    }

    /** 从背包里挑该类别品阶最高的一件装备上，被换下的旧饰品放回背包。 */
    private static void equipBest(ServerPlayer player, AccessoryCategory category) {
        ItemStack best = ItemStack.EMPTY;
        int bestTier = 0;
        for (ItemStack stack : player.getInventory().items) {
            AccessoryDefinition definition = AccessoryCatalog.of(stack);
            if (definition == null || definition.category() != category) {
                continue;
            }
            if (definition.tier() > bestTier) {
                bestTier = definition.tier();
                best = stack;
            }
        }

        if (best.isEmpty()) {
            player.sendSystemMessage(Component.translatable("gui.life_contract.accessory.none_available",
                    Component.translatable("gui.life_contract.accessory.category." + category.id())));
            return;
        }

        Component name = best.getHoverName();
        ItemStack previous = AccessorySlots.equip(player, best);
        best.shrink(1);
        if (!previous.isEmpty() && !player.getInventory().add(previous)) {
            player.drop(previous, false);
        }
        player.getInventory().setChanged();
        player.sendSystemMessage(Component.translatable("gui.life_contract.accessory.equipped", name));
    }

    @Override
    public boolean isStillValid(Player player) {
        return player == owner && player.isAlive();
    }
}