package net.blay09.mods.waystones.network.handler;

import net.blay09.mods.waystones.PlayerWaystoneData;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.network.message.MessagePinWaystone;
import net.blay09.mods.waystones.util.WaystoneEntry;
import net.minecraft.entity.player.EntityPlayer;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;

public class HandlerPinWaystone implements IMessageHandler<MessagePinWaystone, IMessage> {

    @Override
    public IMessage onMessage(final MessagePinWaystone message, final MessageContext ctx) {
        Waystones.proxy.addScheduledTask(() -> {
            EntityPlayer player = ctx.getServerHandler().playerEntity;
            String waystoneName = message.getWaystoneName();

            WaystoneEntry entry = WaystoneManager.getServerWaystone(waystoneName);

            if (entry == null) {
                WaystoneEntry[] playerWaystones = PlayerWaystoneData.fromPlayer(player)
                    .getWaystones();
                for (WaystoneEntry playerEntry : playerWaystones) {
                    if (playerEntry.getName()
                        .equals(waystoneName)) {
                        entry = playerEntry;
                        break;
                    }
                }
            }

            if (entry != null) {
                PlayerWaystoneData.setWaystonePinned(player, entry, message.isPinned());
                WaystoneManager.sendPlayerWaystones(player);
            }
        });
        return null;
    }
}
