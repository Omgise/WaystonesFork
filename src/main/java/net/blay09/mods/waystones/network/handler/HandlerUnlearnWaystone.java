package net.blay09.mods.waystones.network.handler;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.network.message.MessageUnlearnWaystone;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerUnlearnWaystone implements IMessageHandler<MessageUnlearnWaystone, IMessage> {

    @Override
    public IMessage onMessage(MessageUnlearnWaystone message, MessageContext ctx) {
        Waystones.debug(
            "Received unlearn packet from user " + ctx.getServerHandler().playerEntity.getDisplayName()
                + " for waystone "
                + message.getEntry()
                    .getName());
        if (message.getEntry()
            .isGlobal()) {
            return null;
        }
        WaystoneManager.removePlayerWaystone(ctx.getServerHandler().playerEntity, message.getEntry());
        PlayerWaystoneData.setWaystonePinned(ctx.getServerHandler().playerEntity, message.getEntry(), false);
        WaystoneManager.sendPlayerWaystones(ctx.getServerHandler().playerEntity);
        return null;
    }
}
