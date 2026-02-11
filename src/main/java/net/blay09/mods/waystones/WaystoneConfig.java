package net.blay09.mods.waystones;

import net.minecraftforge.common.config.Configuration;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;

// TODO: Clean up this mess

// Static fields are not synced from the server, non-static are
public class WaystoneConfig {

    private static Configuration config;

    public static boolean debugMode;
    public static int teleportButtonX;
    public static int teleportButtonY;
    public static boolean disableParticles;
    public static boolean disableTextGlow;

    public boolean teleportButton;
    public int teleportButtonCooldown;
    public boolean teleportButtonReturnOnly;
    public static boolean showCooldownOnWaystone;

    public boolean allowReturnScrolls;
    public boolean allowWarpStone;

    public int warpStoneCooldown;

    public boolean interDimension;

    public boolean creativeModeOnly;
    public boolean setSpawnPoint;

    public boolean globalNoCooldown;
    public boolean globalInterDimension;

    public static boolean showNametag;
    public static boolean enableWorldgen;
    public static boolean villageNamesCompat;

    public static int xpBaseCost;
    public static int xpBlocksPerLevel;
    public static int xpCrossDimCost;

    public static boolean menusPauseGame;

    public static int sortingMode;

    public float waystoneLightLevel;
    public boolean disableWaystoneDrops;
    public String[] sandyWaystonePathBlocks;
    public String[] mossyWaystonePathBlocks;
    public String[] structureWaystoneRules;

    public static class Categories {

        public static final String general = "general";
        public static final String client = "client";
    }

    public void reloadLocal(Configuration config) {
        teleportButton = config.getBoolean("Debug mode", Categories.general, false, "Additional logs");
        teleportButton = config.getBoolean(
            "Teleport Button in GUI",
            Categories.general,
            false,
            "Should there be a button in the inventory to access the waystone menu?");
        teleportButtonCooldown = config.getInt(
            "Teleport Button Cooldown",
            Categories.general,
            300,
            0,
            86400,
            "The cooldown between usages of the teleport button in seconds.");
        teleportButtonReturnOnly = config.getBoolean(
            "Teleport Button Return Only",
            Categories.general,
            false,
            "If true, the teleport button will only let you return to the last activated waystone, instead of allowing to choose.");

        allowReturnScrolls = config
            .getBoolean("Allow Return Scrolls", Categories.general, true, "If true, return scrolls will be craftable.");
        allowWarpStone = config
            .getBoolean("Allow Warp Stone", Categories.general, true, "If true, the warp stone will be craftable.");

        teleportButtonX = config.getInt(
            "Teleport Button GUI X",
            Categories.client,
            60,
            -100,
            250,
            "The x position of the warp button in the inventory.");
        teleportButtonY = config.getInt(
            "Teleport Button GUI Y",
            Categories.client,
            60,
            -100,
            250,
            "The y position of the warp button in the inventory.");
        disableTextGlow = config.getBoolean(
            "Disable Text Glow",
            Categories.client,
            false,
            "If true, the text overlay on waystones will no longer always render at full brightness.");
        disableParticles = config.getBoolean(
            "Disable Particles",
            Categories.client,
            false,
            "If true, activated waystones will not emit particles.");
        menusPauseGame = config.getBoolean(
            "Menus Pause Game",
            Categories.client,
            false,
            "If true, GUI menus pause the game in singleplayer.");

        warpStoneCooldown = config.getInt(
            "Teleportation Cooldown",
            Categories.general,
            300,
            0,
            86400,
            "The cooldown between usages of the Warp Stone and Waystone in seconds.");

        setSpawnPoint = config.getBoolean(
            "Set Spawnpoint on Activation",
            Categories.general,
            false,
            "If true, the player's spawnpoint will be set to the last activated waystone.");
        interDimension = config.getBoolean(
            "Interdimensional Teleport",
            Categories.general,
            true,
            "If true, all waystones work inter-dimensionally.");

        creativeModeOnly = config.getBoolean(
            "Creative Mode Only",
            Categories.general,
            false,
            "If true, waystones can only be placed in creative mode.");

        globalNoCooldown = config.getBoolean(
            "No Cooldown on Global Waystones",
            Categories.general,
            true,
            "If true, waystones marked as global have no cooldown.");
        globalInterDimension = config.getBoolean(
            "Interdimensional Teleport on Global Waystones",
            Categories.general,
            true,
            "If true, waystones marked as global work inter-dimensionally.");

        showNametag = config.getBoolean(
            "Show Waystone nametag",
            Categories.client,
            false,
            "If true, show a floating nametag with the Waystone's name, above it.");

        enableWorldgen = config
            .getBoolean("Enable Worldgen", Categories.general, true, "If true, generate a Waystone in each village.");

        villageNamesCompat = config.getBoolean(
            "Enable Village Names Compat",
            Categories.general,
            true,
            "If true, village Waystones will take their name from Village Names.");

        xpBaseCost = config.getInt(
            "Teleport Base XP Cost",
            Categories.general,
            5,
            -1,
            Integer.MAX_VALUE,
            "The minimum amount of XP levels consumed when using a Waystone. Set to -1 to disable cost altogether.");

        xpBlocksPerLevel = config.getInt(
            "Teleport XP Cost per X Blocks",
            Categories.general,
            100,
            0,
            Integer.MAX_VALUE,
            "Each how many blocks consume one XP level.");

        xpCrossDimCost = config.getInt(
            "Cross-dim Teleport XP Cost",
            Categories.general,
            5,
            0,
            Integer.MAX_VALUE,
            "How many XP levels are consumed for teleporting to another dimension.");

        sortingMode = config.getInt(
            "Sorting mode",
            Categories.client,
            0,
            0,
            1,
            "The Waystone sorting mode. Alphabetical: 0, Distance: 1.");

        showCooldownOnWaystone = config.getBoolean(
            "Show cooldown on Waystone",
            Categories.client,
            true,
            "If true, Waystone glow texture will display the cooldown status.");

        waystoneLightLevel = config.getFloat(
            "Waystone Light Level",
            Categories.general,
            0.5f,
            0f,
            1f,
            "Light level emitted by waystones. 0 = none, 1 = maximum.");

        disableWaystoneDrops = config.getBoolean(
            "Disable Waystone Drops",
            Categories.general,
            false,
            "If true, waystones will not drop as an item when mined (including Silk Touch).");

        sandyWaystonePathBlocks = config.getStringList(
            "Sandy Waystone Path Blocks",
            Categories.general,
            new String[] { "minecraft:sandstone" },
            "List of path/surface blocks that should make village-generated Waystones use the sandy variant.");
        mossyWaystonePathBlocks = config.getStringList(
            "Mossy Waystone Path Blocks",
            Categories.general,
            new String[] {},
            "List of path/surface blocks that should make village-generated Waystones use the mossy variant.");
        structureWaystoneRules = config.getStringList(
            "Structure Waystone Rules",
            Categories.general,
            new String[] { "structure=village;chance=1;type=auto", "structure=temple_desert;chance=1;type=sandy" },
            "How waystones generate in structures. One rule per structure id. "
                + "Format: structure=<id>;chance=<0..1>;type=<auto|stone|sandy|mossy>;"
                + "name=<override>;forceGlobal=<true|false>;autoActivateGlobal=<true|false>;"
                + "dimensionWhitelist=<*|0,-1,1>;biomeWhitelist=<*|2,17,21>");
    }

    public static WaystoneConfig read(ByteBuf buf) {
        WaystoneConfig config = new WaystoneConfig();
        config.teleportButton = buf.readBoolean();
        config.teleportButtonCooldown = buf.readInt();
        config.teleportButtonReturnOnly = buf.readBoolean();
        config.warpStoneCooldown = buf.readInt();
        config.interDimension = buf.readBoolean();
        config.globalInterDimension = buf.readBoolean();
        config.creativeModeOnly = buf.readBoolean();
        config.setSpawnPoint = buf.readBoolean();
        config.enableWorldgen = buf.readBoolean();
        config.villageNamesCompat = buf.readBoolean();
        config.xpBaseCost = buf.readInt();
        config.xpBlocksPerLevel = buf.readInt();
        config.xpCrossDimCost = buf.readInt();
        config.allowReturnScrolls = buf.readBoolean();
        config.allowWarpStone = buf.readBoolean();
        config.globalNoCooldown = buf.readBoolean();
        config.waystoneLightLevel = buf.readFloat();
        config.disableWaystoneDrops = buf.readBoolean();
        int sandyPathBlockCount = buf.readInt();
        config.sandyWaystonePathBlocks = new String[sandyPathBlockCount];
        for (int i = 0; i < sandyPathBlockCount; i++) {
            config.sandyWaystonePathBlocks[i] = ByteBufUtils.readUTF8String(buf);
        }
        int mossyPathBlockCount = buf.readInt();
        config.mossyWaystonePathBlocks = new String[mossyPathBlockCount];
        for (int i = 0; i < mossyPathBlockCount; i++) {
            config.mossyWaystonePathBlocks[i] = ByteBufUtils.readUTF8String(buf);
        }
        int structureRuleCount = buf.readInt();
        config.structureWaystoneRules = new String[structureRuleCount];
        for (int i = 0; i < structureRuleCount; i++) {
            config.structureWaystoneRules[i] = ByteBufUtils.readUTF8String(buf);
        }
        return config;
    }

    public void write(ByteBuf buf) {
        buf.writeBoolean(teleportButton);
        buf.writeInt(teleportButtonCooldown);
        buf.writeBoolean(teleportButtonReturnOnly);
        buf.writeInt(warpStoneCooldown);
        buf.writeBoolean(interDimension);
        buf.writeBoolean(globalInterDimension);
        buf.writeBoolean(creativeModeOnly);
        buf.writeBoolean(setSpawnPoint);
        buf.writeBoolean(enableWorldgen);
        buf.writeBoolean(villageNamesCompat);
        buf.writeInt(xpBaseCost);
        buf.writeInt(xpBlocksPerLevel);
        buf.writeInt(xpCrossDimCost);
        buf.writeBoolean(allowReturnScrolls);
        buf.writeBoolean(allowWarpStone);
        buf.writeBoolean(globalNoCooldown);
        buf.writeFloat(waystoneLightLevel);
        buf.writeBoolean(disableWaystoneDrops);
        String[] sandyBlocks = sandyWaystonePathBlocks != null ? sandyWaystonePathBlocks : new String[0];
        buf.writeInt(sandyBlocks.length);
        for (String sandyPathBlock : sandyBlocks) {
            ByteBufUtils.writeUTF8String(buf, sandyPathBlock);
        }
        String[] mossyBlocks = mossyWaystonePathBlocks != null ? mossyWaystonePathBlocks : new String[0];
        buf.writeInt(mossyBlocks.length);
        for (String mossyPathBlock : mossyBlocks) {
            ByteBufUtils.writeUTF8String(buf, mossyPathBlock);
        }
        String[] structureRules = structureWaystoneRules != null ? structureWaystoneRules : new String[0];
        buf.writeInt(structureRules.length);
        for (String structureRule : structureRules) {
            ByteBufUtils.writeUTF8String(buf, structureRule);
        }
    }

    public static Configuration getRawConfig() {
        return config;
    }

    public static void setConfig(Configuration config) {
        WaystoneConfig.config = config;
    }
}
