package net.blay09.mods.waystones;

import java.util.Random;

import net.blay09.mods.waystones.block.TileWaystone;
import net.blay09.mods.waystones.network.NetworkHandler;
import net.blay09.mods.waystones.network.message.MessageConfig;
import net.blay09.mods.waystones.network.message.MessageDimensionNames;
import net.blay09.mods.waystones.util.WaystoneEntry;
import net.blay09.mods.waystones.worldgen.VillageWaystone;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.IChatComponent;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.MapGenStructureIO;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.registry.VillagerRegistry;

public class CommonProxy {

    public void preInit(FMLPreInitializationEvent event) {
        FMLCommonHandler.instance()
            .bus()
            .register(this);
        MinecraftForge.EVENT_BUS.register(this);

        if (WaystoneConfig.enableWorldgen) {
            MapGenStructureIO
                .func_143031_a(VillageWaystone.VillageWaystonePiece.class, Waystones.MODID + ":VillageWaystone");
        }
    }

    public void init(FMLInitializationEvent event) {
        if (WaystoneConfig.enableWorldgen) {
            Waystones.debug("Registering VillageWaystone CreationHandler");
            VillagerRegistry.instance()
                .registerVillageCreationHandler(new VillageWaystone.CreationHandler());
        }
    }

    public void addScheduledTask(Runnable runnable) {
        runnable.run();
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        NetworkHandler.channel.sendTo(new MessageConfig(Waystones.getConfig()), (EntityPlayerMP) event.player);
        NetworkHandler.channel.sendTo(new MessageDimensionNames(), (EntityPlayerMP) event.player);
        WaystoneManager.sendPlayerWaystones(event.player);
    }

    @SubscribeEvent
    public void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        WaystoneManager.sendPlayerWaystones(event.player);
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        WaystoneManager.sendPlayerWaystones(event.player);
    }

    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event) {
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            GlobalWaystoneData.get(event.world);
        }
    }

    @SubscribeEvent
    public void onWorldUnload(WorldEvent.Unload event) {
        // Prevents global waystones to carry over in-memory when switching worlds
        if (!event.world.isRemote && event.world.provider.dimensionId == 0) {
            WaystoneManager.setServerWaystones(new WaystoneEntry[0]);
        }
    }

    public void openWaystoneNameEdit(TileWaystone tileEntity) {

    }

    public void openWaystoneSelection(TileWaystone currentWaystone, boolean isFree) {

    }

    public void printChatMessage(int i, IChatComponent chatComponent) {

    }

    public void playSound(String soundName, float pitch) {

    }

    public void spawnWaystoneParticles(World world, int x, int y, int z, TileWaystone tileWaystone, Random random) {}

    public int getWaystoneRenderId() {
        return 0;
    }

    public WaystoneEntry[] getCombinedWaystones() {
        return new WaystoneEntry[] {};
    }
}
