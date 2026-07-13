package org.alku.life_contract.mutation;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import org.alku.life_contract.ContractEvents;
import org.alku.life_contract.Life_contract;
import org.alku.life_contract.TeamInventory;

import java.util.UUID;

public final class MutationService {
    private MutationService() {
    }

    public static UUID teamId(ServerPlayer player) {
        UUID id = ContractEvents.getLeaderUUID(player);
        return id == null ? player.getUUID() : id;
    }

    public static MutationSavedData.TeamState state(ServerPlayer player) {
        return MutationSavedData.get(player.server).state(teamId(player));
    }

    public static int availableMp(ServerPlayer player) {
        return availableMp(player, TeamInventory.getOrCreateServer(teamId(player)));
    }

    public static String upgrade(ServerPlayer player, MutationNode node) {
        MutationSavedData data = MutationSavedData.get(player.server);
        MutationSavedData.TeamState state = data.state(teamId(player));
        int current = state.level(node);
        int cost = node.costForNext(current);
        if (cost < 0) return "该词条已满级";
        if (state.totalLevels() < node.requiredLevels()) {
            return "当前总词条等级不足 " + node.requiredLevels();
        }
        if (node.conflict() != null && state.level(node.conflict()) > 0) {
            return "与“" + node.conflict().title + "”互斥";
        }

        TeamInventory teamInventory = TeamInventory.getOrCreateServer(teamId(player));
        if (availableMp(player, teamInventory) < cost) {
            return "MP不足，需要 " + cost + " 个升华（玩家背包与队伍背包合计）";
        }
        if (!consumeSublimation(player, teamInventory, cost)) {
            return "升华数量发生变化，请重试";
        }

        state.upgrade(node);
        data.setDirty();
        return "已升级 “" + node.title + "” 至 Lv." + (current + 1);
    }

    private static int availableMp(ServerPlayer player, TeamInventory teamInventory) {
        long total = countSublimation(player.getInventory().items)
                + countSublimation(teamInventory.getItems());
        return (int) Math.min(total, Integer.MAX_VALUE);
    }

    private static long countSublimation(Iterable<ItemStack> stacks) {
        long total = 0;
        for (ItemStack stack : stacks) {
            if (stack.is(Life_contract.SUBLIMATION.get())) {
                total += stack.getCount();
            }
        }
        return total;
    }

    private static boolean consumeSublimation(ServerPlayer player, TeamInventory teamInventory, int amount) {
        int remaining = consumeFromStacks(player.getInventory().items, amount);
        if (remaining < amount) {
            player.getInventory().setChanged();
        }

        int beforeTeamInventory = remaining;
        remaining = consumeFromStacks(teamInventory.getItems(), remaining);
        if (remaining < beforeTeamInventory) {
            teamInventory.setChanged();
            teamInventory.broadcastChanges();
        }
        return remaining == 0;
    }

    private static int consumeFromStacks(Iterable<ItemStack> stacks, int amount) {
        int remaining = amount;
        for (ItemStack stack : stacks) {
            if (!stack.is(Life_contract.SUBLIMATION.get())) continue;
            int consumed = Math.min(remaining, stack.getCount());
            stack.shrink(consumed);
            remaining -= consumed;
            if (remaining == 0) break;
        }
        return remaining;
    }
}
