package org.alku.life_contract.mutation;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class MutationSavedData extends SavedData {
    private static final String NAME = "life_contract_mutations";
    private final Map<UUID, TeamState> teams = new HashMap<>();

    public static MutationSavedData get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(MutationSavedData::load, MutationSavedData::new, NAME);
    }
    public TeamState state(UUID team) { return teams.computeIfAbsent(team, key -> new TeamState()); }

    public static MutationSavedData load(CompoundTag root) {
        MutationSavedData data = new MutationSavedData();
        CompoundTag teamsTag = root.getCompound("Teams");
        for (String key : teamsTag.getAllKeys()) {
            try { data.teams.put(UUID.fromString(key), TeamState.load(teamsTag.getCompound(key))); }
            catch (IllegalArgumentException ignored) {}
        }
        return data;
    }
    @Override public CompoundTag save(CompoundTag root) {
        CompoundTag teamsTag = new CompoundTag();
        teams.forEach((id, state) -> teamsTag.put(id.toString(), state.save()));
        root.put("Teams", teamsTag); return root;
    }

    public static final class TeamState {
        private final EnumMap<MutationNode, Integer> levels = new EnumMap<>(MutationNode.class);
        public int level(MutationNode node) { return levels.getOrDefault(node, 0); }
        public int totalLevels() { return levels.values().stream().mapToInt(Integer::intValue).sum(); }
        public void upgrade(MutationNode node) { levels.put(node, level(node) + 1); }
        CompoundTag save() { CompoundTag tag = new CompoundTag(); levels.forEach((n,l)->tag.putInt(n.name(),l)); return tag; }
        static TeamState load(CompoundTag tag) { TeamState s=new TeamState(); for(MutationNode n:MutationNode.values()) { int l=tag.getInt(n.name()); if(l>0)s.levels.put(n,Math.min(l,n.maxLevel())); } return s; }
    }
}
