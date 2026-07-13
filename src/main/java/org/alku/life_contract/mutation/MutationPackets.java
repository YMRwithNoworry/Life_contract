package org.alku.life_contract.mutation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import java.util.function.Supplier;
public final class MutationPackets {
 private MutationPackets(){}
 public static void open(ServerPlayer p){MutationSavedData.TeamState s=MutationService.state(p);NetworkHooks.openScreen(p,new net.minecraft.world.MenuProvider(){public Component getDisplayName(){return Component.literal("阵营异变树");}public net.minecraft.world.inventory.AbstractContainerMenu createMenu(int id,net.minecraft.world.entity.player.Inventory inv,net.minecraft.world.entity.player.Player pl){return new MutationMenu(id,inv,s);}},buf->{buf.writeVarInt(s.points);buf.writeVarInt(s.totalLevels());for(MutationNode n:MutationNode.values())buf.writeVarInt(s.level(n));});}
 public static final class Open {public Open(){}public Open(FriendlyByteBuf b){}public void encode(FriendlyByteBuf b){}public void handle(Supplier<NetworkEvent.Context> c){NetworkEvent.Context x=c.get();x.enqueueWork(()->{if(x.getSender()!=null)open(x.getSender());});x.setPacketHandled(true);}}
 public static final class Upgrade {private final MutationNode node;public Upgrade(MutationNode n){node=n;}public Upgrade(FriendlyByteBuf b){node=MutationNode.values()[b.readVarInt()];}public void encode(FriendlyByteBuf b){b.writeVarInt(node.ordinal());}public void handle(Supplier<NetworkEvent.Context> c){NetworkEvent.Context x=c.get();x.enqueueWork(()->{ServerPlayer p=x.getSender();if(p!=null){p.sendSystemMessage(Component.literal("§6[异变] §f"+MutationService.upgrade(p,node)));open(p);}});x.setPacketHandled(true);}}
}
