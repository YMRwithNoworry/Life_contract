package org.alku.life_contract.mutation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.alku.life_contract.Life_contract;
import java.util.EnumMap;

public final class MutationMenu extends AbstractContainerMenu {
    public final int mp;
    public final int total;
    public final EnumMap<MutationNode, Integer> levels = new EnumMap<>(MutationNode.class);

    public MutationMenu(int id, Inventory inventory, FriendlyByteBuf buffer) {
        super(Life_contract.MUTATION_MENU.get(), id);
        mp = buffer.readVarInt();
        total = buffer.readVarInt();
        for (MutationNode node : MutationNode.values()) {
            levels.put(node, buffer.readVarInt());
        }
    }

    public MutationMenu(int id, Inventory inventory, MutationSavedData.TeamState state, int availableMp) {
        super(Life_contract.MUTATION_MENU.get(), id);
        mp = availableMp;
        total = state.totalLevels();
        for (MutationNode node : MutationNode.values()) {
            levels.put(node, state.level(node));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public net.minecraft.world.item.ItemStack quickMoveStack(Player player, int index) {
        return net.minecraft.world.item.ItemStack.EMPTY;
    }
}
