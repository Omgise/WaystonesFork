package net.blay09.mods.waystones.network.handler;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.network.message.MessageWarpStone;
import net.blay09.mods.waystones.util.WaystoneEntry;
import net.blay09.mods.waystones.util.WaystoneXpCost;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ChatComponentTranslation;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerWarpStone implements IMessageHandler<MessageWarpStone, IMessage> {

    @Override
    public IMessage onMessage(final MessageWarpStone message, final MessageContext ctx) {
        Waystones.proxy.addScheduledTask(() -> {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;

            // Free warp validation
            if (message.isFree()) {
                if (!Waystones.getConfig().teleportButton || Waystones.getConfig().teleportButtonReturnOnly
                    || !PlayerWaystoneData.canFreeWarp(player)) {
                    return;
                }
            }

            // XP cost enforcement
            if (!message.isFree() && !player.capabilities.isCreativeMode) {
                WaystoneEntry target = message.getWaystone();
                if (WaystoneConfig.xpBaseCost > -1) {
                    int cost = WaystoneXpCost.getXpCost(player, target);

                    if (player.experienceLevel < cost) { // TODO make this red
                        player.addChatMessage(new ChatComponentTranslation("gui.waystones:notEnoughXp", cost));
                        return;
                    }

                    player.addExperienceLevel(-cost);
                }
            }

            // Teleport
            if (WaystoneManager.teleportToWaystone(player, message.getWaystone())) {
                if (WaystoneManager.getServerWaystone(
                    message.getWaystone()
                        .getName())
                    == null || !Waystones.getConfig().globalNoCooldown) {

                    if (message.isFree()) {
                        PlayerWaystoneData.setLastFreeWarp(player, System.currentTimeMillis());
                    } else {
                        PlayerWaystoneData.setLastWarpStoneUse(player, System.currentTimeMillis());
                    }
                }
            }

            // Sync waystones
            WaystoneManager.sendPlayerWaystones(player);
        });
        return null;
    }
}
