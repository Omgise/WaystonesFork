package net.blay09.mods.waystones.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessagePinWaystone implements IMessage {

    private String waystoneName;
    private boolean pin;

    public MessagePinWaystone() {}

    public MessagePinWaystone(String waystoneName, boolean pin) {
        this.waystoneName = waystoneName;
        this.pin = pin;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.waystoneName = ByteBufUtils.readUTF8String(buf);
        this.pin = buf.readBoolean();
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, this.waystoneName);
        buf.writeBoolean(this.pin);
    }

    public String getWaystoneName() {
        return waystoneName;
    }

    public boolean isPinned() {
        return pin;
    }
}
