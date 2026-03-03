package net.blay09.mods.waystones.network.handler;

import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.compat.JourneyMapCompat;
import net.blay09.mods.waystones.compat.XaeroMinimapCompat;
import net.blay09.mods.waystones.network.message.MessageJourneyMapWaypoint;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerJourneyMapWaypoint implements IMessageHandler<MessageJourneyMapWaypoint, IMessage> {

    @Override
    public IMessage onMessage(final MessageJourneyMapWaypoint message, MessageContext ctx) {
        Waystones.proxy.addScheduledTask(new Runnable() {

            @Override
            public void run() {
                JourneyMapCompat
                    .addOrUpdateWaypoint(message.getWaystoneName(), message.getDimensionId(), message.getPos());
                XaeroMinimapCompat
                    .addOrUpdateWaypoint(message.getWaystoneName(), message.getDimensionId(), message.getPos());
            }
        });
        return null;
    }
}
