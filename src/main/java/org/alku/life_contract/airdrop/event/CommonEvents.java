package org.alku.life_contract.airdrop.event;

import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.alku.life_contract.airdrop.Airdrop;
import org.alku.life_contract.airdrop.command.AirdropCommand;
import org.alku.life_contract.airdrop.data.AirdropSavedData;
import org.alku.life_contract.airdrop.entity.AirdropEntity;
import org.alku.life_contract.Life_contract;

@EventBusSubscriber(modid = Life_contract.MODID)
public class CommonEvents {

    @SubscribeEvent
    public static void onContainerClose(PlayerContainerEvent.Close event) {
        Player player = event.getEntity();
        if (player.level().isClientSide)
            return;

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

        if (event.getContainer() instanceof ChestMenu chestMenu) {
            if (chestMenu.getContainer() instanceof AirdropEntity airdrop) {
                if (airdrop.isEmpty() && airdrop.isAlive()) {
                    airdrop.discard();
                }
            }
        }
    }

    /** 玩家离线时清掉导航状态，避免粒子开关集合只增不减。 */
    @SubscribeEvent
    public static void onPlayerLogout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        AirdropNavigator.forgetPlayer(event.getEntity().getUUID());
    }

    /** 登记/注销已加载的空投，导航时无需再扫描整个世界边界。 */
    @SubscribeEvent
    public static void onEntityJoinLevel(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            AirdropNavigator.trackAirdrop(event.getLevel(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onEntityLeaveLevel(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide()) {
            AirdropNavigator.untrackAirdrop(event.getLevel(), event.getEntity());
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide())
            return;

        ServerLevel level = (ServerLevel) event.getLevel();
        if (level.getGameTime() % 5 != 0)
            return;

        AirdropSavedData data = AirdropSavedData.get(level);
        long day = level.getDayTime() / 24000;
        int timeOfDay = (int) (level.getDayTime() % 24000);

        data.getSchedules().forEach((name, s) -> {
            if (Math.abs(timeOfDay - s.timeOfDay) < 100 && s.lastTriggeredDay < day) {
                s.lastTriggeredDay = day;
                data.setDirty();

                if (level.random.nextDouble() < s.chance) {
                    Airdrop.LOGGER.info("Triggering scheduled airdrop: {}", name);
                    AirdropCommand.spawnRandomAirdrop(level, level.getServer().createCommandSourceStack());
                }
            }
        });

        AirdropNavigator.updateNavigation(level);
    }
}
