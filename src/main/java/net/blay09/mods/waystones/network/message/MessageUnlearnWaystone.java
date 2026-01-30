package net.blay09.mods.waystones.network.message;

import net.blay09.mods.waystones.util.WaystoneEntry;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessageUnlearnWaystone implements IMessage {

    private WaystoneEntry entry;

    public MessageUnlearnWaystone() {}

    public MessageUnlearnWaystone(WaystoneEntry entry) {
        this.entry = entry;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entry = WaystoneEntry.read(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.entry.write(buf);
    }

    public WaystoneEntry getEntry() {
        return entry;
    }
}
