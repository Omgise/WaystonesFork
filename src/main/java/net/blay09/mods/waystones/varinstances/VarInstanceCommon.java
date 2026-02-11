package net.blay09.mods.waystones.varinstances;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

import net.blay09.mods.waystones.Waystones;
import net.minecraft.block.Block;

public class VarInstanceCommon {

    private final Set<Block> sandyWaystonePathBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Block> mossyWaystonePathBlocks = Collections.newSetFromMap(new IdentityHashMap<>());

    public void preInitHook() {
        rebuildWaystonePathBlockCaches();
    }

    public void rebuildWaystonePathBlockCaches() {
        rebuildPathBlockCache(sandyWaystonePathBlocks, Waystones.getConfig().sandyWaystonePathBlocks, "sandy");
        rebuildPathBlockCache(mossyWaystonePathBlocks, Waystones.getConfig().mossyWaystonePathBlocks, "mossy");
    }

    public boolean isSandyWaystonePathBlock(Block block) {
        return sandyWaystonePathBlocks.contains(block);
    }

    private void rebuildPathBlockCache(Set<Block> cache, String[] blockNames, String label) {
        cache.clear();
        if (blockNames == null) {
            return;
        }

        for (String blockName : blockNames) {
            if (blockName == null) {
                continue;
            }

            String trimmed = blockName.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            Block block = Block.getBlockFromName(trimmed);
            if (block != null) {
                cache.add(block);
            } else {
                Waystones.LOG.warn("Invalid {} waystone path block in config: {}", label, trimmed);
            }
        }
    }

    public boolean isMossyWaystonePathBlock(Block block) {
        return mossyWaystonePathBlocks.contains(block);
    }
}
