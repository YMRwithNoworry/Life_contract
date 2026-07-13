package org.alku.life_contract.mutation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.alku.life_contract.Life_contract;
import java.util.EnumMap;
public final class MutationMenu extends AbstractContainerMenu {
 public final int points,total; public final EnumMap<MutationNode,Integer> levels=new EnumMap<>(MutationNode.class);
 public MutationMenu(int id, Inventory inv, FriendlyByteBuf buf){super(Life_contract.MUTATION_MENU.get(),id);points=buf.readVarInt();total=buf.readVarInt();for(MutationNode n:MutationNode.values())levels.put(n,buf.readVarInt());}
 public MutationMenu(int id, Inventory inv, MutationSavedData.TeamState s){super(Life_contract.MUTATION_MENU.get(),id);points=s.points;total=s.totalLevels();for(MutationNode n:MutationNode.values())levels.put(n,s.level(n));}
 @Override public boolean stillValid(Player player){return true;}
 @Override public net.minecraft.world.item.ItemStack quickMoveStack(Player p,int i){return net.minecraft.world.item.ItemStack.EMPTY;}
}
