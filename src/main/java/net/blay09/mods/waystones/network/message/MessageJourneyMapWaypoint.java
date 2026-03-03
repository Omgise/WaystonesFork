package net.blay09.mods.waystones.network.message;

import net.blay09.mods.waystones.util.BlockPos;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessageJourneyMapWaypoint implements IMessage {

    private String waystoneName;
    private int dimensionId;
    private BlockPos pos;

    public MessageJourneyMapWaypoint() {}

    public MessageJourneyMapWaypoint(String waystoneName, int dimensionId, BlockPos pos) {
        this.waystoneName = waystoneName;
        this.dimensionId = dimensionId;
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        waystoneName = ByteBufUtils.readUTF8String(buf);
        dimensionId = buf.readInt();
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf) {
        ByteBufUtils.writeUTF8String(buf, waystoneName);
        buf.writeInt(dimensionId);
        buf.writeLong(pos.toLong());
    }

    public String getWaystoneName() {
        return waystoneName;
    }

    public int getDimensionId() {
        return dimensionId;
    }

    public BlockPos getPos() {
        return pos;
    }
}
