package net.blay09.mods.waystones.network.message;

import net.blay09.mods.waystones.util.WaystoneEntry;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessageRenameWaystone implements IMessage {

    private WaystoneEntry entry;
    private String newName;

    public MessageRenameWaystone() {}

    public MessageRenameWaystone(WaystoneEntry entry, String newName) {
        this.entry = entry;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.entry = WaystoneEntry.read(buf);
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        this.entry.write(buf);
        ByteBufUtils.writeUTF8String(buf, this.newName);
    }

    public WaystoneEntry getEntry() {
        return entry;
    }

    public String getNewName() {
        return newName;
    }
}
