package org.alku.border_weaver.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import org.alku.border_weaver.handler.AutoTaskHandler;
import org.alku.border_weaver.handler.CountdownHandler;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.coordinates.Vec2Argument;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.phys.Vec2;

import java.util.Collection;

public class BorderCommand {
    private static final int DEFAULT_ZONE_SIZE = 500;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("bw_random_shrink")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("percentage", DoubleArgumentType.doubleArg(0.0001, 1.0))
                        .then(Commands.argument("countdown", IntegerArgumentType.integer(0))
                                .then(Commands.argument("shrink_time", IntegerArgumentType.integer(0))
                                        .executes(context -> {
                                            ServerLevel level = context.getSource().getLevel();
                                            WorldBorder border = level.getWorldBorder();
                                            double percentage = DoubleArgumentType.getDouble(context, "percentage");
                                            int countdown = IntegerArgumentType.getInteger(context, "countdown");
                                            int shrinkTime = IntegerArgumentType.getInteger(context, "shrink_time");

                                            double currentSize = border.getSize();
                                            double newSize = Math.max(1.0, currentSize * percentage);

                                            double range = Math.max(0, (currentSize - newSize) / 2.0);
                                            RandomSource random = level.getRandom();
                                            double newX = border.getCenterX() + (random.nextDouble() * 2 - 1) * range;
                                            double newZ = border.getCenterZ() + (random.nextDouble() * 2 - 1) * range;

                                            CountdownHandler.startCountdown(level, newX, newZ, newSize, countdown, shrinkTime);

                                            context.getSource().sendSuccess(() -> Component.literal("§6[Border Weaver]§a 任务启动！目标大小: §e" + String.format("%.2f", newSize)), true);
                                            return 1;
                                        })
                                ))));

        dispatcher.register(Commands.literal("bw_clear")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    ServerLevel level = context.getSource().getLevel();
                    WorldBorder border = level.getWorldBorder();
                    CountdownHandler.stopCountdown();
                    AutoTaskHandler.stopAutoTask();
                    border.setSize(6.0E7D);
                    border.setCenter(0.5D, 0.5D);
                    context.getSource().sendSuccess(() -> Component.literal("§c[Border Weaver]§f 边界已清除，所有任务已停止。"), true);
                    return 1;
                }));

        dispatcher.register(Commands.literal("bw_auto_start")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("interval_seconds", IntegerArgumentType.integer(1))
                        .then(Commands.argument("percentage", DoubleArgumentType.doubleArg(0.0001, 1.0))
                                .then(Commands.argument("countdown", IntegerArgumentType.integer(0))
                                        .then(Commands.argument("shrink_time", IntegerArgumentType.integer(0))
                                                .executes(context -> {
                                                    ServerLevel level = context.getSource().getLevel();
                                                    int intervalSeconds = IntegerArgumentType.getInteger(context, "interval_seconds");
                                                    double percentage = DoubleArgumentType.getDouble(context, "percentage");
                                                    int countdown = IntegerArgumentType.getInteger(context, "countdown");
                                                    int shrinkTime = IntegerArgumentType.getInteger(context, "shrink_time");

                                                    AutoTaskHandler.startAutoTask(level, intervalSeconds, percentage, countdown, shrinkTime);
                                                    return 1;
                                                })
                                        )))));

        dispatcher.register(Commands.literal("bw_auto_stop")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    AutoTaskHandler.stopAutoTask();
                    return 1;
                }));

        // bw_zone - 以执行者当前位置为中心创建500x500边界
        dispatcher.register(Commands.literal("bw_zone")
                .requires(source -> source.hasPermission(2))
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    ServerLevel level = source.getLevel();
                    
                    if (source.getEntity() == null) {
                        source.sendFailure(Component.literal("§c[Border Weaver]§f 此指令需要由实体执行，或指定坐标/目标。"));
                        return 0;
                    }
                    
                    double centerX = source.getEntity().getX();
                    double centerZ = source.getEntity().getZ();
                    
                    return createZone(level, centerX, centerZ, DEFAULT_ZONE_SIZE, source);
                })
                // bw_zone <x> <z> - 以指定坐标为中心
                .then(Commands.argument("pos", Vec2Argument.vec2())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();
                            
                            Vec2 pos = Vec2Argument.getVec2(context, "pos");
                            double centerX = pos.x;
                            double centerZ = pos.y;
                            
                            return createZone(level, centerX, centerZ, DEFAULT_ZONE_SIZE, source);
                        }))
                // bw_zone <targets> - 以指定实体为中心
                .then(Commands.argument("targets", EntityArgument.entities())
                        .executes(context -> {
                            CommandSourceStack source = context.getSource();
                            ServerLevel level = source.getLevel();
                            
                            Collection<? extends Entity> targets = EntityArgument.getEntities(context, "targets");
                            if (targets.isEmpty()) {
                                source.sendFailure(Component.literal("§c[Border Weaver]§f 未找到目标实体。"));
                                return 0;
                            }
                            
                            // 计算所有实体的中心点
                            double sumX = 0, sumZ = 0;
                            for (Entity entity : targets) {
                                sumX += entity.getX();
                                sumZ += entity.getZ();
                            }
                            double centerX = sumX / targets.size();
                            double centerZ = sumZ / targets.size();
                            
                            return createZone(level, centerX, centerZ, DEFAULT_ZONE_SIZE, source);
                        }))
                // bw_zone <x> <z> <size> - 指定坐标和大小
                .then(Commands.argument("pos", Vec2Argument.vec2())
                        .then(Commands.argument("size", IntegerArgumentType.integer(1))
                                .executes(context -> {
                                    CommandSourceStack source = context.getSource();
                                    ServerLevel level = source.getLevel();
                                    
                                    Vec2 pos = Vec2Argument.getVec2(context, "pos");
                                    double centerX = pos.x;
                                    double centerZ = pos.y;
                                    int size = IntegerArgumentType.getInteger(context, "size");
                                    
                                    return createZone(level, centerX, centerZ, size, source);
                                }))));
    }

    private static int createZone(ServerLevel level, double centerX, double centerZ, int size, CommandSourceStack source) {
        WorldBorder border = level.getWorldBorder();
        
        CountdownHandler.stopCountdown();
        AutoTaskHandler.stopAutoTask();
        
        border.setCenter(centerX, centerZ);
        border.setSize(size);
        
        spawnZoneVisuals(level, centerX, centerZ, size);
        
        source.sendSuccess(() -> Component.literal(
                "§b[Border Weaver]§a 边界区域已创建！\n" +
                "§f━━━━━━━━━━━━━━━━━━━━━━\n" +
                "§e  中心坐标: §l(" + String.format("%.1f", centerX) + ", " + String.format("%.1f", centerZ) + ")\n" +
                "§e  区域大小: §l" + size + " x " + size + "\n" +
                "§e  边界范围: §lX: " + (int)(centerX - size/2.0) + " ~ " + (int)(centerX + size/2.0) + "\n" +
                "              §lZ: " + (int)(centerZ - size/2.0) + " ~ " + (int)(centerZ + size/2.0) + "\n" +
                "§f━━━━━━━━━━━━━━━━━━━━━━\n" +
                "§7  提示: 边界不可移动，需重新执行指令调整位置"
        ), true);
        
        return 1;
    }

    private static void spawnZoneVisuals(ServerLevel level, double centerX, double centerZ, double size) {
        double halfSize = size / 2.0;
        double minX = centerX - halfSize;
        double maxX = centerX + halfSize;
        double minZ = centerZ - halfSize;
        double maxZ = centerZ + halfSize;
        
        int surfaceY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.WORLD_SURFACE, (int)centerX, (int)centerZ);
        
        for (int y = surfaceY; y < surfaceY + 50; y += 5) {
            for (int i = 0; i < (int)size; i += 20) {
                level.sendParticles(ParticleTypes.END_ROD, minX + i, y, minZ, 1, 0, 0, 0, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, minX + i, y, maxZ, 1, 0, 0, 0, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, minX, y, minZ + i, 1, 0, 0, 0, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, maxX, y, minZ + i, 1, 0, 0, 0, 0.0);
            }
        }
        
        level.sendParticles(ParticleTypes.FLAME, centerX, surfaceY + 1, centerZ, 20, 0.5, 0.5, 0.5, 0.02);
    }
}
