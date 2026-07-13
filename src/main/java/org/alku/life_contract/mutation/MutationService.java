package org.alku.life_contract.mutation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import java.util.UUID;

public final class MutationService {
    private MutationService() {}
    public static UUID teamId(ServerPlayer player) { UUID id=ContractEvents.getLeaderUUID(player); return id == null ? player.getUUID() : id; }
    public static MutationSavedData.TeamState state(ServerPlayer player) { return MutationSavedData.get(player.server).state(teamId(player)); }
    public static String upgrade(ServerPlayer player, MutationNode node) {
        MutationSavedData data=MutationSavedData.get(player.server); MutationSavedData.TeamState state=data.state(teamId(player));
        int current=state.level(node); int cost=node.costForNext(current);
        if(cost<0)return "该词条已满级";
        if(state.totalLevels()<node.requiredLevels())return "当前总词条等级不足 " + node.requiredLevels();
        if(node.conflict()!=null && state.level(node.conflict())>0)return "与“"+node.conflict().title+"”互斥";
        if(state.points<cost)return "异变点数不足，需要 "+cost+" MP";
        if(countSublimation(player)<cost)return "升华不足，需要 "+cost+" 个";
        consumeSublimation(player,cost); state.points-=cost; state.upgrade(node); data.setDirty(); return "已升级 “"+node.title+"” 至 Lv."+(current+1);
    }
    private static int countSublimation(ServerPlayer p){int n=0;for(ItemStack s:p.getInventory().items)if(s.is(Life_contract.SUBLIMATION.get()))n+=s.getCount();return n;}
    private static void consumeSublimation(ServerPlayer p,int amount){for(ItemStack s:p.getInventory().items){if(!s.is(Life_contract.SUBLIMATION.get()))continue;int take=Math.min(amount,s.getCount());s.shrink(take);amount-=take;if(amount==0)break;}p.getInventory().setChanged();}
}
