package net.blay09.mods.waystones.network.message;

import java.lang.reflect.InvocationTargetException;

import net.blay09.mods.waystones.mixins.early.minecraftforge.AccessorDimensionManager;
import net.minecraft.world.WorldProvider;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

public class MessageDimensionNames implements IMessage {

    int amount;
    Integer[] ids;
    String[] names;

    public MessageDimensionNames() {
        this.amount = 0;
        this.ids = new Integer[AccessorDimensionManager.getProviders()
            .size()];
        this.names = new String[AccessorDimensionManager.getProviders()
            .size()];
        for (int key : AccessorDimensionManager.getProviders()
            .keySet()) {
            this.ids[this.amount] = key;
            try {
                WorldProvider provider = AccessorDimensionManager.getProviders()
                    .get(key)
                    .getDeclaredConstructor()
                    .newInstance();
                this.names[this.amount] = provider.getDimensionName();
            } catch (InvocationTargetException | NoSuchMethodException | InstantiationException
                | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
            this.amount++;
        }
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        this.amount = buf.readInt();
        this.ids = new Integer[this.amount];
        this.names = new String[this.amount];
        for (int i = 0; i < this.amount; i++) {
            this.ids[i] = buf.readInt();
            this.names[i] = ByteBufUtils.readUTF8String(buf);
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeInt(this.amount);
        for (int i = 0; i < this.amount; i++) {
            buf.writeInt(this.ids[i]);
            ByteBufUtils.writeUTF8String(buf, this.names[i]);
        }
    }

    public int getAmount() {
        return amount;
    }

    public Integer[] getIds() {
        return ids;
    }

    public String[] getNames() {
        return names;
    }
}
