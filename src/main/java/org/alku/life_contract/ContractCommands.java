package org.alku.life_contract;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Random;

import org.alku.life_contract.events.GameEventManager;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ContractCommands {
        private static final Random RANDOM = new Random();

        @SubscribeEvent
        public static void onRegisterCommands(RegisterCommandsEvent event) {
                registerContractCommand(event);
                registerContractHudCommand(event);
        }

        private static void registerContractCommand(RegisterCommandsEvent event) {
                var contract = Commands.literal("contract");

                contract.then(Commands.literal("hud")
                        .executes(context -> {
                                ContractHUD.isHudEnabled = !ContractHUD.isHudEnabled;
                                boolean state = ContractHUD.isHudEnabled;
                                context.getSource().sendSuccess(() -> Component
                                        .literal("§a[生灵契约] §fHUD显示已"
                                                + (state ? "开启" : "关闭")), false);
                                return 1;
                        }));

                contract.then(Commands.literal("highlight")
                        .executes(context -> {
                                TeamHighlightRenderer.isHighlightEnabled = !TeamHighlightRenderer.isHighlightEnabled;
                                boolean state = TeamHighlightRenderer.isHighlightEnabled;
                                context.getSource().sendSuccess(() -> Component
                                        .literal("§a[生灵契约] §f队友高光显示已"
                                                + (state ? "开启" : "关闭")), false);
                                return 1;
                        }));

                contract.then(Commands.literal("ingot_to_exp")
                        .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                OreToExp.setEnabled(player, !OreToExp.isEnabled(player));
                                boolean state = OreToExp.isEnabled(player);
                                context.getSource().sendSuccess(() -> Component
                                        .literal("§a[生灵契约] §f锭转经验已"
                                                + (state ? "开启" : "关闭")), false);
                                return 1;
                        }));

                contract.then(Commands.literal("convert_ingots")
                        .executes(context -> {
                                ServerPlayer player = context.getSource().getPlayerOrException();
                                int levels = OreToExp.convertIngotsToExp(player);
                                if (levels > 0) {
                                        context.getSource().sendSuccess(() -> Component
                                                .literal("§a[生灵契约] §f成功转换获得 " + levels + " 等级！"), true);
                                } else {
                                        context.getSource().sendFailure(
                                                Component.literal("§c背包中没有可转换的锭！"));
                                }
                                return 1;
                        }));

                contract.then(Commands.literal("spawn_sentinel")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.argument("team_number", IntegerArgumentType.integer(1))
                                .suggests((context, builder) -> suggestTeamNumbers(context, builder))
                                .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        int teamNumber = IntegerArgumentType.getInteger(context, "team_number");

                                        try {
                                                net.minecraft.world.entity.animal.IronGolem golem = new net.minecraft.world.entity.animal.IronGolem(
                                                        net.minecraft.world.entity.EntityType.IRON_GOLEM,
                                                        player.level());
                                                Vec3 pos = player.position();
                                                golem.moveTo(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot());
                                                
                                                golem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000.0);
                                                golem.setHealth(1000.0f);
                                                golem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0);
                                                golem.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED).setBaseValue(0.0);
                                                golem.setPersistenceRequired();
                                                
                                                golem.setCustomName(net.minecraft.network.chat.Component.literal("§6队伍守卫 §b#" + teamNumber));
                                                golem.setCustomNameVisible(true);

                                                player.level().addFreshEntity(golem);

                                                context.getSource().sendSuccess(() ->
                                                        Component.literal("§a[生灵契约] §f已为队伍 §e" + teamNumber + " §f生成守卫实体！"), true);
                                        } catch (Exception e) {
                                                context.getSource().sendFailure(
                                                        Component.literal("§c[生灵契约] 生成守卫失败: " + e.getMessage()));
                                                e.printStackTrace();
                                        }
                                        return 1;
                                })));

                contract.then(Commands.literal("pool")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("add")
                                .then(Commands.argument("modid", StringArgumentType.string())
                                        .executes(context -> {
                                                String modId = StringArgumentType.getString(context, "modid");
                                                ModPoolConfig.addMod(modId);
                                                context.getSource().sendSuccess(() -> Component
                                                        .literal("§a[生灵契约] §f已将 §e" + modId + " §f添加到随机池。"), true);
                                                return 1;
                                        })))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("modid", StringArgumentType.string())
                                        .executes(context -> {
                                                String modId = StringArgumentType.getString(context, "modid");
                                                ModPoolConfig.removeMod(modId);
                                                context.getSource().sendSuccess(() -> Component
                                                        .literal("§a[生灵契约] §f已从随机池移除 §e" + modId + "。"), true);
                                                return 1;
                                        })))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                        List<String> pool = ModPoolConfig.getModPool();
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§e== 随机契约池 (" + pool.size() + ") =="), false);
                                        for (String m : pool) {
                                                context.getSource().sendSuccess(() -> Component.literal(" §8- §f" + m), false);
                                        }
                                        return 1;
                                })));

                registerTeamCommands(contract);
                registerAdminCommand(contract);
                registerGameCommands(contract);

                event.getDispatcher().register(contract);
        }
        
        private static void registerGameCommands(LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> contract) {
                contract.then(Commands.literal("game")
                        .then(Commands.literal("join_low_team")
                                .executes(context -> {
                                        ServerPlayer joiningPlayer = context.getSource().getPlayerOrException();
                                        return GameEventManager.joinSmallestTeam(joiningPlayer) ? 1 : 0;
                                }))
                        .then(Commands.literal("stay_spectator")
                                .executes(context -> {
                                        ServerPlayer joiningPlayer = context.getSource().getPlayerOrException();
                                        joiningPlayer.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
                                        joiningPlayer.sendSystemMessage(Component.literal("§7[生灵契约] 你将继续保持旁观状态。"));
                                        return 1;
                                }))
                        .then(Commands.literal("start")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                        ServerPlayer player = context.getSource().getPlayerOrException();
                                        GameEventManager.startGame(player.serverLevel(), player.getX(), player.getZ());
                                        
                                        net.minecraft.server.MinecraftServer server = player.getServer();
                                        if (server != null) {
                                                server.getCommands().performPrefixedCommand(
                                                        server.createCommandSourceStack(),
                                                        "bw_auto_start 60 0.8 45 20"
                                                );
                                        }
                                        
                                        context.getSource().sendSuccess(() -> 
                                                Component.literal("§a[游戏] §f游戏已开始！边界已收缩至你周围600x600区域。"), true);
                                        return 1;
                                }))
                        .then(Commands.literal("pause")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                        GameEventManager.pauseGame();
                                        return 1;
                                }))
                        .then(Commands.literal("resume")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                        GameEventManager.resumeGame();
                                        return 1;
                                }))
                        .then(Commands.literal("stop")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                        GameEventManager.stopGame();
                                        context.getSource().sendSuccess(() -> 
                                                Component.literal("§c[游戏] §f游戏已停止。"), true);
                                        return 1;
                                }))
                        .then(Commands.literal("status")
                                .requires(source -> source.hasPermission(2))
                                .executes(context -> {
                                        boolean active = GameEventManager.isGameActive();
                                        boolean paused = GameEventManager.isGamePaused();
                                        long elapsed = GameEventManager.getElapsedSeconds();
                                        
                                        if (!active) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("§7[游戏] §f游戏未开始。"), false);
                                        } else if (paused) {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("§e[游戏] §f游戏已暂停。已进行时间: §b" + formatTime(elapsed)), false);
                                        } else {
                                                context.getSource().sendSuccess(() -> 
                                                        Component.literal("§a[游戏] §f游戏进行中。已进行时间: §b" + formatTime(elapsed)), false);
                                        }
                                        return 1;
                                })));
        }

        private static String formatTime(long seconds) {
                long minutes = seconds / 60;
                long secs = seconds % 60;
                return String.format("%d:%02d", minutes, secs);
        }

        private static void registerTeamCommands(LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> contract) {
                contract.then(Commands.literal("team")
                        .then(Commands.literal("split")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                                int count = IntegerArgumentType.getInteger(context, "count");
                                                List<ServerPlayer> players = new ArrayList<>(
                                                        context.getSource().getServer().getPlayerList().getPlayers());
                                                if (players.isEmpty()) return 0;

                                                Collections.shuffle(players);
                                                int total = players.size();
                                                int actualTeams = Math.min(count, total);

                                                Map<UUID, Integer> teamNumbers = new HashMap<>();
                                                for (int i = 0; i < actualTeams; i++) {
                                                        UUID leaderUUID = players.get(i).getUUID();
                                                        teamNumbers.put(leaderUUID, i + 1);
                                                }

                                                for (int i = 0; i < total; i++) {
                                                        ServerPlayer player = players.get(i);
                                                        ServerPlayer leader = players.get(i % actualTeams);
                                                        int teamNumber = teamNumbers.get(leader.getUUID());

                                                        player.getPersistentData().putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leader.getUUID());
                                                        player.getPersistentData().putString(TeamOrganizerItem.TAG_LEADER_NAME, leader.getName().getString());
                                                        player.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);

                                                        String leaderMod = leader.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
                                                        if (!leaderMod.isEmpty()) {
                                                                player.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, leaderMod);
                                                        }
                                                        ContractEvents.syncData(player);
                                                }

                                                context.getSource().sendSuccess(() -> Component
                                                        .literal("§a[生灵契约] §f成功将 " + total + " 名玩家分配至 " + actualTeams + " 个队伍。"), true);
                                                return 1;
                                        })))
                        .then(Commands.literal("random_split")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1))
                                        .executes(context -> {
                                                int count = IntegerArgumentType.getInteger(context, "count");
                                                List<ServerPlayer> players = new ArrayList<>(
                                                        context.getSource().getServer().getPlayerList().getPlayers());
                                                if (players.isEmpty()) return 0;

                                                List<String> modPool = ModPoolConfig.getModPool();
                                                boolean poolEmpty = modPool.isEmpty();
                                                
                                                if (poolEmpty) {
                                                        context.getSource().sendSuccess(() -> Component
                                                                .literal("§e[生灵契约] §f随机池为空，将进行普通分队（不分配契约模组）。使用 /contract pool add 添加模组。"), true);
                                                }

                                                Collections.shuffle(players);
                                                int total = players.size();
                                                int actualTeams = Math.min(count, total);

                                                Map<UUID, String> teamMods = new HashMap<>();
                                                Map<UUID, Integer> teamNumbers = new HashMap<>();
                                                List<String> shuffledPool = new ArrayList<>(modPool);
                                                Collections.shuffle(shuffledPool);

                                                for (int i = 0; i < actualTeams; i++) {
                                                        UUID leaderUUID = players.get(i).getUUID();
                                                        teamNumbers.put(leaderUUID, i + 1);
                                                }

                                                for (int i = 0; i < total; i++) {
                                                        ServerPlayer player = players.get(i);
                                                        ServerPlayer leader = players.get(i % actualTeams);
                                                        UUID leaderUUID = leader.getUUID();
                                                        int teamNumber = teamNumbers.get(leaderUUID);

                                                        player.getPersistentData().putUUID(TeamOrganizerItem.TAG_LEADER_UUID, leaderUUID);
                                                        player.getPersistentData().putString(TeamOrganizerItem.TAG_LEADER_NAME, leader.getName().getString());
                                                        player.getPersistentData().putInt(TeamOrganizerItem.TAG_TEAM_NUMBER, teamNumber);

                                                        if (!poolEmpty) {
                                                                if (!teamMods.containsKey(leaderUUID)) {
                                                                        String modId = shuffledPool.get(teamMods.size() % shuffledPool.size());
                                                                        teamMods.put(leaderUUID, modId);
                                                                }

                                                                String assignedMod = teamMods.get(leaderUUID);
                                                                player.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, assignedMod);
                                                        }

                                                        ContractEvents.syncData(player);
                                                }

                                                String message = poolEmpty 
                                                        ? "§a[生灵契约] §f成功将 " + total + " 名玩家分配至 " + actualTeams + " 个队伍（未分配契约模组）。"
                                                        : "§a[生灵契约] §f成功将 " + total + " 名玩家分配至 " + actualTeams + " 个随机阵营队伍。";
                                                final String finalMessage = message;
                                                context.getSource().sendSuccess(() -> Component.literal(finalMessage), true);
                                                return 1;
                                        })))
                        .then(Commands.literal("set_mod")
                                .requires(source -> source.hasPermission(2))
                                .then(Commands.argument("target", EntityArgument.player())
                                        .then(Commands.argument("modid", StringArgumentType.string())
                                                .executes(context -> {
                                                        ServerPlayer target = EntityArgument.getPlayer(context, "target");
                                                        String modId = StringArgumentType.getString(context, "modid");

                                                        target.getPersistentData().putString(SoulContractItem.TAG_CONTRACT_MOD, modId);
                                                        ContractEvents.propagateContractToTeam(target, modId);
                                                        ContractEvents.syncData(target);

                                                        context.getSource().sendSuccess(() -> Component
                                                                .literal("§a[生灵契约] §f已为 " + target.getName().getString() + " (及该队) 指定契约模组: §e" + modId), true);
                                                        return 1;
                                                }))))
                        .then(Commands.literal("tp")
                                .then(Commands.argument("target", EntityArgument.player())
                                        .executes(context -> {
                                                ServerPlayer source = context.getSource().getPlayerOrException();
                                                ServerPlayer target = EntityArgument.getPlayer(context, "target");

                                                if (source == target) {
                                                        context.getSource().sendFailure(Component.literal("§c你不能传送到你自己！"));
                                                        return 0;
                                                }

                                                if (ContractEvents.isSameTeam(source, target)) {
                                                        source.teleportTo(target.serverLevel(), target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                                                        context.getSource().sendSuccess(() -> Component
                                                                .literal("§a[生灵契约] §f已传送到队友 §e" + target.getName().getString() + " §f身边。"), false);
                                                        target.sendSystemMessage(Component.literal("§a[生灵契约] §f队友 §e" + source.getName().getString() + " §f传送到了你身边。"));
                                                        return 1;
                                                } else {
                                                        context.getSource().sendFailure(Component.literal("§c只能传送到同一队伍的玩家！"));
                                                        return 0;
                                                }
                                        }))));
        }

        private static void registerAdminCommand(LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> contract) {
                contract.then(Commands.literal("admin")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("list")
                                .executes(context -> {
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§e== 生灵契约 管理面板 =="), false);
                                        Map<UUID, List<ServerPlayer>> teams = new HashMap<>();
                                        for (ServerPlayer p : context.getSource().getServer().getPlayerList().getPlayers()) {
                                                UUID leader = ContractEvents.getLeaderUUID(p);
                                                if (leader == null) leader = p.getUUID();
                                                teams.computeIfAbsent(leader, k -> new ArrayList<>()).add(p);
                                        }

                                        for (Map.Entry<UUID, List<ServerPlayer>> entry : teams.entrySet()) {
                                                UUID leaderUUID = entry.getKey();
                                                List<ServerPlayer> members = entry.getValue();
                                                ServerPlayer leaderPlayer = context.getSource().getServer().getPlayerList().getPlayer(leaderUUID);

                                                String lName = leaderPlayer != null ? leaderPlayer.getName().getString() : "未知(" + leaderUUID.toString().substring(0, 8) + ")";
                                                String lMod = "无";
                                                int lTeamNumber = -1;
                                                if (leaderPlayer != null) {
                                                        lMod = leaderPlayer.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
                                                        if (lMod.isEmpty()) lMod = "无";
                                                        lTeamNumber = leaderPlayer.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER);
                                                }

                                                final String finalLeaderName = lName;
                                                final String finalModId = lMod;
                                                final int finalTeamNumber = lTeamNumber;
                                                String teamNumberStr = finalTeamNumber > 0 ? "§e#" + finalTeamNumber + " " : "";
                                                context.getSource().sendSuccess(() -> Component
                                                        .literal("§6队: " + teamNumberStr + "§b" + finalLeaderName + " §7[" + finalModId + "]"), false);

                                                for (ServerPlayer m : members) {
                                                        if (!m.getUUID().equals(leaderUUID)) {
                                                                final String mName = m.getName().getString();
                                                                context.getSource().sendSuccess(() -> Component
                                                                        .literal(" §8- §f" + mName), false);
                                                        }
                                                }
                                        }
                                        return 1;
                                })));
        }

        private static void registerContractHudCommand(RegisterCommandsEvent event) {
                event.getDispatcher().register(
                        Commands.literal("contract_hud")
                                .executes(context -> {
                                        ContractHUD.isHudEnabled = !ContractHUD.isHudEnabled;
                                        boolean state = ContractHUD.isHudEnabled;
                                        context.getSource().sendSuccess(
                                                () -> Component.literal("§a[生灵契约] §fHUD显示已"
                                                        + (state ? "开启" : "关闭")), false);
                                        return 1;
                                }));
        }

        private static CompletableFuture<Suggestions> suggestTeamNumbers(CommandContext<net.minecraft.commands.CommandSourceStack> context, SuggestionsBuilder builder) {
                Set<Integer> teamNumbers = new HashSet<>();
                
                try {
                        net.minecraft.commands.CommandSourceStack source = context.getSource();
                        if (source.getEntity() instanceof ServerPlayer sourcePlayer 
                                && sourcePlayer.getServer() != null) {
                                for (ServerPlayer player : sourcePlayer.getServer().getPlayerList().getPlayers()) {
                                        if (player.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)) {
                                                int number = player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER);
                                                teamNumbers.add(number);
                                        }
                                }
                        }
                } catch (Exception e) {
                }

                String remaining = builder.getRemaining();
                teamNumbers.forEach(number -> {
                        String numberStr = String.valueOf(number);
                        if (numberStr.startsWith(remaining)) {
                                builder.suggest(number);
                        }
                });

                return builder.buildFuture();
        }
}
