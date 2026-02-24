package org.alku.airdrop.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.border.WorldBorder;
import org.alku.airdrop.data.AirdropExportImport;
import org.alku.airdrop.data.AirdropSavedData;
import org.alku.airdrop.entity.AirdropEntity;

import java.nio.file.Path;
import java.util.Map;

public class AirdropCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("airdrop")
                .requires(s -> s.hasPermission(2))
                .then(Commands.literal("summon")
                        .executes(ctx -> {
                            spawnRandomAirdrop(ctx.getSource().getLevel(), ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("edit")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        AirdropSavedData.get(ctx.getSource().getLevel()).getPoolNames(), builder))
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    return openEditGui(ctx.getSource().getPlayerOrException(), name);
                                })))
                .then(Commands.literal("delete")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        AirdropSavedData.get(ctx.getSource().getLevel()).getPoolNames(), builder))
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    return deletePool(ctx.getSource(), name);
                                })))
                // 导出单个池
                .then(Commands.literal("export")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        AirdropSavedData.get(ctx.getSource().getLevel()).getPoolNames(), builder))
                                .executes(ctx -> {
                                    String name = StringArgumentType.getString(ctx, "name");
                                    return exportPool(ctx.getSource(), name);
                                })))
                // 导入单个池
                .then(Commands.literal("import")
                        .then(Commands.argument("file", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        AirdropExportImport.getImportableFiles(), builder))
                                .executes(ctx -> {
                                    String file = StringArgumentType.getString(ctx, "file");
                                    return importPool(ctx.getSource(), file);
                                })))
                // 导出全部池
                .then(Commands.literal("exportall")
                        .executes(ctx -> exportAll(ctx.getSource(), "airdrop_all"))
                        .then(Commands.argument("filename", StringArgumentType.word())
                                .executes(ctx -> {
                                    String filename = StringArgumentType.getString(ctx, "filename");
                                    return exportAll(ctx.getSource(), filename);
                                })))
                // 导入全部池
                .then(Commands.literal("importall")
                        .then(Commands.argument("file", StringArgumentType.word())
                                .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                        AirdropExportImport.getImportableFiles(), builder))
                                .executes(ctx -> {
                                    String file = StringArgumentType.getString(ctx, "file");
                                    return importAllPools(ctx.getSource(), file);
                                })))
                // 定时事件管理
                .then(Commands.literal("schedule")
                        .then(Commands.literal("list")
                                .executes(ctx -> listSchedules(ctx.getSource())))
                        .then(Commands.literal("add")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .then(Commands.argument("time", IntegerArgumentType.integer(0, 24000))
                                                .then(Commands
                                                        .argument("chance", DoubleArgumentType.doubleArg(0.0, 1.0))
                                                        .executes(ctx -> {
                                                            String name = StringArgumentType.getString(ctx, "name");
                                                            int time = IntegerArgumentType.getInteger(ctx, "time");
                                                            double chance = DoubleArgumentType.getDouble(ctx, "chance");
                                                            return addSchedule(ctx.getSource(), name, time, chance);
                                                        })))))
                        .then(Commands.literal("remove")
                                .then(Commands.argument("name", StringArgumentType.word())
                                        .suggests((ctx, builder) -> SharedSuggestionProvider.suggest(
                                                AirdropSavedData.get(ctx.getSource().getLevel()).getSchedules()
                                                        .keySet(),
                                                builder))
                                        .executes(ctx -> removeSchedule(ctx.getSource(),
                                                StringArgumentType.getString(ctx, "name")))))));
    }

    public static void spawnRandomAirdrop(ServerLevel level, CommandSourceStack source) {
        AirdropSavedData data = AirdropSavedData.get(level);
        RandomSource random = level.getRandom();
        double x, z;

        if (data.hasRange()) {
            double[] r = data.getRange();
            double deltaX = r[2] - r[0];
            double deltaZ = r[3] - r[1];
            x = r[0] + (random.nextDouble() * deltaX);
            z = r[1] + (random.nextDouble() * deltaZ);
        } else {
            BlockPos pos = BlockPos.containing(source.getPosition());
            x = pos.getX() + random.nextInt(200) - 100;
            z = pos.getZ() + random.nextInt(200) - 100;
        }

        x = Math.floor(x) + 0.5;
        z = Math.floor(z) + 0.5;

        double[] safePos = clampToBorder(level, x, z);
        x = safePos[0];
        z = safePos[1];

        spawnAirdrop(level, x, z);

        double finalX = x;
        double finalZ = z;
        Component msg = Component.literal(
                String.format("§6§l[⚠ 空投警报] §e物资正在投放! 目标坐标: §c[%.0f, %.0f]", finalX, finalZ));

        level.players().forEach(p -> p.sendSystemMessage(msg));
    }

    public static double[] clampToBorder(ServerLevel level, double x, double z) {
        WorldBorder border = level.getWorldBorder();
        double borderSize = border.getSize();
        double maxBorderSize = 5.9E7;
        
        if (borderSize >= maxBorderSize) {
            return new double[]{x, z};
        }
        
        double centerX = border.getCenterX();
        double centerZ = border.getCenterZ();
        double halfSize = borderSize / 2.0;
        double margin = 5.0;
        
        double minX = centerX - halfSize + margin;
        double maxX = centerX + halfSize - margin;
        double minZ = centerZ - halfSize + margin;
        double maxZ = centerZ + halfSize - margin;
        
        double clampedX = Math.max(minX, Math.min(maxX, x));
        double clampedZ = Math.max(minZ, Math.min(maxZ, z));
        
        return new double[]{clampedX, clampedZ};
    }

    public static void spawnAirdrop(ServerLevel level, double x, double z) {
        AirdropEntity entity = new AirdropEntity(level, x, 300, z);
        NonNullList<ItemStack> loot = AirdropSavedData.get(level).getRandomPool();
        for (int i = 0; i < loot.size(); i++) {
            entity.setItem(i, loot.get(i).copy());
        }
        level.addFreshEntity(entity);
    }

    private static int deletePool(CommandSourceStack source, String name) {
        if (AirdropSavedData.get(source.getLevel()).removePool(name)) {
            source.sendSuccess(() -> Component.literal("§aDeleted pool: " + name), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cPool not found: " + name));
            return 0;
        }
    }

    // ===== 导出单个池 =====
    private static int exportPool(CommandSourceStack source, String name) {
        AirdropSavedData data = AirdropSavedData.get(source.getLevel());
        if (!data.getPoolNames().contains(name)) {
            source.sendFailure(Component.literal("§c[导出失败] 找不到空投池: " + name));
            return 0;
        }
        NonNullList<ItemStack> items = data.getPool(name);
        Path result = AirdropExportImport.exportPool(name, items);
        if (result != null) {
            source.sendSuccess(() -> Component.literal(
                    "§a[导出成功] §f空投池 '§e" + name + "§f' 已导出到: §7" + result), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c[导出失败] 写入文件时出错"));
            return 0;
        }
    }

    // ===== 导入单个池 =====
    private static int importPool(CommandSourceStack source, String fileName) {
        AirdropSavedData data = AirdropSavedData.get(source.getLevel());
        String poolName = AirdropExportImport.importPool(fileName, data);
        if (poolName != null) {
            source.sendSuccess(() -> Component.literal(
                    "§a[导入成功] §f已从文件 '§e" + fileName + ".json§f' 导入空投池 '§e" + poolName + "§f'"), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c[导入失败] 文件不存在或格式错误: " + fileName + ".json"));
            return 0;
        }
    }

    // ===== 导出全部池 =====
    private static int exportAll(CommandSourceStack source, String fileName) {
        AirdropSavedData data = AirdropSavedData.get(source.getLevel());
        if (data.getPoolNames().isEmpty()) {
            source.sendFailure(Component.literal("§c[导出失败] 当前没有任何空投池"));
            return 0;
        }
        Path result = AirdropExportImport.exportAll(data, fileName);
        if (result != null) {
            source.sendSuccess(() -> Component.literal(
                    "§a[导出成功] §f已导出 §e" + data.getPoolNames().size() + "§f 个空投池到: §7" + result), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§c[导出失败] 写入文件时出错"));
            return 0;
        }
    }

    // ===== 导入全部池 =====
    private static int importAllPools(CommandSourceStack source, String fileName) {
        AirdropSavedData data = AirdropSavedData.get(source.getLevel());
        int count = AirdropExportImport.importAll(fileName, data);
        if (count > 0) {
            source.sendSuccess(() -> Component.literal(
                    "§a[导入成功] §f已从文件 '§e" + fileName + ".json§f' 导入 §e" + count + "§f 个空投池"), true);
            return 1;
        } else if (count == 0) {
            source.sendFailure(Component.literal("§e[导入提示] 文件中没有找到空投池数据"));
            return 0;
        } else {
            source.sendFailure(Component.literal("§c[导入失败] 文件不存在或格式错误: " + fileName + ".json"));
            return 0;
        }
    }

    private static int listSchedules(CommandSourceStack source) {
        AirdropSavedData data = AirdropSavedData.get(source.getLevel());
        Map<String, AirdropSavedData.Schedule> schedules = data.getSchedules();
        if (schedules.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§e[System] No schedules active."), false);
            return 1;
        }
        source.sendSuccess(() -> Component.literal("§6--- Active Schedules ---"), false);
        schedules.forEach((name, s) -> {
            source.sendSuccess(() -> Component.literal(
                    String.format("§e- %s: §fTime: §b%d §f| Chance: §a%.1f%%", name, s.timeOfDay, s.chance * 100)),
                    false);
        });
        return 1;
    }

    private static int addSchedule(CommandSourceStack source, String name, int time, double chance) {
        AirdropSavedData.get(source.getLevel()).addSchedule(name, time, chance);
        source.sendSuccess(() -> Component.literal(
                String.format("§a[System] Schedule '%s' added (Time: %d, Chance: %.1f%%)", name, time, chance * 100)),
                true);
        return 1;
    }

    private static int removeSchedule(CommandSourceStack source, String name) {
        if (AirdropSavedData.get(source.getLevel()).removeSchedule(name)) {
            source.sendSuccess(() -> Component.literal("§a[System] Schedule '" + name + "' removed."), true);
            return 1;
        }
        source.sendFailure(Component.literal("§c[System] Schedule not found: " + name));
        return 0;
    }

    private static int openEditGui(Player player, String poolName) {
        ServerLevel level = (ServerLevel) player.level();
        AirdropSavedData data = AirdropSavedData.get(level);
        NonNullList<ItemStack> existing = data.getPool(poolName);
        SimpleMenuProvider menu = new SimpleMenuProvider((id, inv, p) -> {
            ChestMenu chest = ChestMenu.threeRows(id, inv);
            for (int i = 0; i < existing.size(); i++) {
                chest.getContainer().setItem(i, existing.get(i).copy());
            }
            return chest;
        }, Component.literal("Editing: " + poolName));
        player.getPersistentData().putString("EditingAirdropPool", poolName);
        player.openMenu(menu);
        return 1;
    }
}