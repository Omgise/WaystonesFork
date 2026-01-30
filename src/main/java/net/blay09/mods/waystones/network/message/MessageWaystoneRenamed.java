package net.blay09.mods.waystones.network.message;

import net.blay09.mods.waystones.util.BlockPos;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessageWaystoneRenamed implements IMessage {

    private int dimensionId;
    private BlockPos pos;
    private String oldName;
    private String newName;

    public MessageWaystoneRenamed() {}

    public MessageWaystoneRenamed(int dimensionId, BlockPos pos, String oldName, String newName) {
        this.dimensionId = dimensionId;
        this.pos = pos;
        this.oldName = oldName;
        this.newName = newName;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.dimensionId = buf.readInt();
        this.pos = BlockPos.fromLong(buf.readLong());
        this.oldName = ByteBufUtils.readUTF8String(buf);
        this.newName = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(dimensionId);
        buf.writeLong(pos.toLong());
        ByteBufUtils.writeUTF8String(buf, oldName);
        ByteBufUtils.writeUTF8String(buf, newName);
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public BlockPos getPos() {
        return pos;
    }

    public String getOldName() {
        return oldName;
    }

    public String getNewName() {
        return newName;
    }
}
