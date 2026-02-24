package org.alku.airdrop.event;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.alku.airdrop.Airdrop;
import org.alku.airdrop.data.AirdropSavedData;
import org.alku.airdrop.entity.AirdropEntity;
import org.alku.airdrop.command.AirdropCommand;
import net.minecraft.world.phys.AABB;
import java.util.List;
import java.util.stream.Collectors;

@Mod.EventBusSubscriber(modid = Airdrop.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return;

        // 保存编辑的池
        if (player.getPersistentData().contains("EditingAirdropPool")) {
            String poolName = player.getPersistentData().getString("EditingAirdropPool");
            if (event.getContainer() instanceof ChestMenu chestMenu) {
                NonNullList<ItemStack> items = NonNullList.withSize(27, ItemStack.EMPTY);
                for (int i = 0; i < 27; i++) {
                    items.set(i, chestMenu.getSlot(i).getItem().copy());
                }
                AirdropSavedData.get((ServerLevel) player.level()).savePool(poolName, items);
                player.sendSystemMessage(Component.literal("§a[System] Pool '" + poolName + "' saved."));
            }
            player.getPersistentData().remove("EditingAirdropPool");
        }

        // 如果空投空了，则销毁
        if (event.getContainer() instanceof ChestMenu chestMenu) {
            if (chestMenu.getContainer() instanceof AirdropEntity airdrop) {
                if (airdrop.isEmpty() && airdrop.isAlive()) {
                    airdrop.discard();
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide)
            return;

        ServerLevel level = (ServerLevel) event.level;
        // 每 20 tick (1秒) 检查一次，减少开销
        if (level.getGameTime() % 20 != 0)
            return;

        AirdropSavedData data = AirdropSavedData.get(level);
        long day = level.getDayTime() / 24000;
        int timeOfDay = (int) (level.getDayTime() % 24000);

        data.getSchedules().forEach((name, s) -> {
            // 如果时间接近目标时间，且今天还没触发过
            if (Math.abs(timeOfDay - s.timeOfDay) < 100 && s.lastTriggeredDay < day) {
                s.lastTriggeredDay = day;
                data.setDirty();

                if (level.random.nextDouble() < s.chance) {
                    Airdrop.LOGGER.info("Triggering scheduled airdrop: {}", name);
                    AirdropCommand.spawnRandomAirdrop(level, level.getServer().createCommandSourceStack());
                }
            }
        });

        // --- 持续显示未领取空投坐标 ---
        // 使用一个足够大的范围来寻找实体
        AABB searchBox = new AABB(
                level.getWorldBorder().getMinX(), -64, level.getWorldBorder().getMinZ(),
                level.getWorldBorder().getMaxX(), 320, level.getWorldBorder().getMaxZ());

        List<AirdropEntity> activeAirdrops = level.getEntitiesOfClass(AirdropEntity.class, searchBox,
                airdrop -> airdrop.isAlive() && !airdrop.isClaimed());

        if (!activeAirdrops.isEmpty()) {
            String coords = activeAirdrops.stream()
                    .map(a -> String.format("§e[%.0f, %.0f]", a.getX(), a.getZ()))
                    .collect(Collectors.joining(" §7| "));

            Component actionBarMsg = Component.literal("§6§l[活跃空投] " + coords);
            level.players().forEach(p -> p.sendSystemMessage(actionBarMsg, true));
        }
    }
}