package org.alku.life_contract;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = Life_contract.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ContractHUD {

    public static boolean isHudEnabled = true;

    @SubscribeEvent
    public static void registerOverlays(RegisterGuiOverlaysEvent event) {
        event.registerAbove(VanillaGuiOverlay.EXPERIENCE_BAR.id(), "contract_status", HUD_OVERLAY);
    }

    public static final IGuiOverlay HUD_OVERLAY = (gui, guiGraphics, partialTick, screenWidth, screenHeight) -> {
        if (!isHudEnabled)
            return;

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null)
            return;

        int x = 10;
        int y = screenHeight / 2 - 20;
        int color = 0xFFFFFF;

        UUID myUUID = player.getUUID();
        ClientDataStorage.PlayerData myData = ClientDataStorage.get(myUUID);

        String selfMod = myData != null ? myData.contractMod
                : player.getPersistentData().getString(SoulContractItem.TAG_CONTRACT_MOD);
        UUID leaderUUID = myData != null ? myData.leaderUUID
                : (player.getPersistentData().hasUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        ? player.getPersistentData().getUUID(TeamOrganizerItem.TAG_LEADER_UUID)
                        : null);
        int teamNumber = myData != null && myData.teamNumber != -1 ? myData.teamNumber
                : (player.getPersistentData().contains(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        ? player.getPersistentData().getInt(TeamOrganizerItem.TAG_TEAM_NUMBER)
                        : -1);

        UUID myTeamUUID = (leaderUUID != null) ? leaderUUID : myUUID;

        guiGraphics.drawString(mc.font, "§e== 生灵契约 ==", x, y, color);
        y += 10;

        if (teamNumber != -1) {
            guiGraphics.drawString(mc.font, "§6队伍编号: §b" + teamNumber, x, y, color);
            y += 10;
        }

        if (!selfMod.isEmpty()) {
            guiGraphics.drawString(mc.font, "契约模组: §a" + selfMod, x, y, color);
            y += 10;
        } else {
            guiGraphics.drawString(mc.font, "契约模组: §7无", x, y, color);
            y += 10;
        }

        List<ClientDataStorage.PlayerData> teamMembers = new ArrayList<>();
        String myName = player.getName().getString();
        for (ClientDataStorage.PlayerData data : ClientDataStorage.PLAYER_DATA_CACHE.values()) {
            UUID theirTeamUUID = (data.leaderUUID != null) ? data.leaderUUID : data.playerUUID;
            
            if (myTeamUUID.equals(theirTeamUUID)) {
                teamMembers.add(data);
            }
        }

        if (!teamMembers.isEmpty()) {
            y += 5;
            guiGraphics.drawString(mc.font, "§6队友:", x, y, color);
            y += 10;
            for (ClientDataStorage.PlayerData memberData : teamMembers) {
                String memberName = memberData.playerName;
                String prefix = memberName.equals(myName) ? "§a● " : "§7- ";
                String displayText = prefix + memberName;
                guiGraphics.drawString(mc.font, displayText, x, y, memberName.equals(myName) ? 0x00FF00 : 0xAAAAAA);
                y += 10;
            }
        }
    };
}
