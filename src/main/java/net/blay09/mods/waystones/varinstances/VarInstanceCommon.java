package net.blay09.mods.waystones.varinstances;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.WaystoneManager;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class VarInstanceCommon {

    private final Set<Block> sandyWaystonePathBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Set<Block> mossyWaystonePathBlocks = Collections.newSetFromMap(new IdentityHashMap<>());
    private final Map<String, StructureWaystoneRule> structureWaystoneRules = new HashMap<>();

    public void preInitHook() {
        rebuildCaches();
    }

    public void rebuildCaches() {
        rebuildWaystonePathBlockCaches();
        rebuildStructureWaystoneRuleCache();
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

    public StructureWaystoneRule getStructureWaystoneRule(String structureId) {
        if (structureId == null) {
            return null;
        }
        return structureWaystoneRules.get(normalizeStructureId(structureId));
    }

    public int resolveStructureWaystoneVariant(String structureId, int autoVariant, World world, int biomeId,
        Random rand) {
        if (!WaystoneConfig.enableWorldgen) {
            return -1;
        }

        StructureWaystoneRule rule = getStructureWaystoneRule(structureId);
        if (rule == null) {
            return autoVariant;
        }

        if (!rule.dimensionAllowed(world.provider.dimensionId)) {
            return -1;
        }
        if (!rule.biomeAllowed(biomeId)) {
            return -1;
        }

        if (rule.chance <= 0f) {
            return -1;
        }

        if (rule.chance < 1f && rand.nextFloat() > rule.chance) {
            return -1;
        }

        return rule.typeOverride == WaystoneTypeOverride.AUTO ? autoVariant : rule.typeOverride.toVariant();
    }

    public String resolveStructureWaystoneName(String structureId, String autoName) {
        StructureWaystoneRule rule = getStructureWaystoneRule(structureId);
        String baseName = rule != null && rule.nameOverride != null
            && !rule.nameOverride.trim()
                .isEmpty() ? rule.nameOverride.trim() : autoName;
        if (baseName == null || baseName.trim()
            .isEmpty()) {
            return baseName;
        }
        return makeUniqueWaystoneName(baseName.trim());
    }

    public boolean shouldForceGlobalStructureWaystone(String structureId) {
        StructureWaystoneRule rule = getStructureWaystoneRule(structureId);
        return rule != null && rule.forceGlobal;
    }

    public boolean shouldAutoActivateGlobalStructureWaystone(String structureId) {
        StructureWaystoneRule rule = getStructureWaystoneRule(structureId);
        return rule == null || rule.autoActivateGlobal;
    }

    private String makeUniqueWaystoneName(String baseName) {
        if (WaystoneManager.getServerWaystone(baseName) == null) {
            return baseName;
        }

        int i = 2;
        while (WaystoneManager.getServerWaystone(baseName + " (" + i + ")") != null) {
            i++;
        }
        return baseName + " (" + i + ")";
    }

    public void rebuildStructureWaystoneRuleCache() {
        structureWaystoneRules.clear();
        String[] lines = Waystones.getConfig().structureWaystoneRules;
        if (lines == null) {
            return;
        }

        for (String line : lines) {
            StructureWaystoneRule rule = parseStructureRule(line);
            if (rule == null) {
                continue;
            }

            if (structureWaystoneRules.containsKey(rule.structureId)) {
                Waystones.LOG.warn("Duplicate structure waystone rule for '{}'; keeping first.", rule.structureId);
                continue;
            }
            structureWaystoneRules.put(rule.structureId, rule);
        }
    }

    private StructureWaystoneRule parseStructureRule(String line) {
        if (line == null) {
            return null;
        }

        String trimmedLine = line.trim();
        if (trimmedLine.isEmpty()) {
            return null;
        }

        String structureId = null;
        float chance = 1f;
        WaystoneTypeOverride typeOverride = WaystoneTypeOverride.AUTO;
        String nameOverride = null;
        boolean forceGlobal = false;
        boolean autoActivateGlobal = false;
        Set<Integer> dimensionWhitelist = null;
        Set<Integer> biomeWhitelist = null;

        for (String segment : trimmedLine.split(";")) {
            String token = segment.trim();
            if (token.isEmpty()) {
                continue;
            }

            int eq = token.indexOf('=');
            if (eq <= 0) {
                Waystones.LOG.warn("Invalid structure waystone rule token '{}': {}", token, trimmedLine);
                continue;
            }

            String key = token.substring(0, eq)
                .trim()
                .toLowerCase(Locale.ROOT);
            String value = token.substring(eq + 1)
                .trim();

            switch (key) {
                case "structure":
                    structureId = normalizeStructureId(value);
                    break;
                case "chance":
                case "probability":
                    try {
                        chance = Math.max(0f, Math.min(1f, Float.parseFloat(value)));
                    } catch (NumberFormatException ex) {
                        Waystones.LOG.warn("Invalid chance '{}' in structure waystone rule: {}", value, trimmedLine);
                    }
                    break;
                case "type":
                case "typeoverride":
                    typeOverride = WaystoneTypeOverride.fromConfig(value);
                    break;
                case "name":
                case "nameoverride":
                    nameOverride = value.isEmpty() ? null : value;
                    break;
                case "forceglobal":
                    forceGlobal = Boolean.parseBoolean(value);
                    break;
                case "autoactivateglobal":
                case "activateglobalautomatically":
                    autoActivateGlobal = Boolean.parseBoolean(value);
                    break;
                case "dimensionwhitelist":
                case "dimensions":
                    dimensionWhitelist = parseDimensionWhitelist(value, trimmedLine);
                    break;
                case "biomewhitelist":
                case "biomes":
                    biomeWhitelist = parseBiomeWhitelist(value, trimmedLine);
                    break;
                default:
                    Waystones.LOG.warn("Unknown structure waystone rule key '{}': {}", key, trimmedLine);
            }
        }

        if (structureId == null || structureId.isEmpty()) {
            Waystones.LOG.warn("Structure waystone rule is missing structure id: {}", trimmedLine);
            return null;
        }

        if (forceGlobal && (nameOverride == null || nameOverride.trim()
            .isEmpty())) {
            Waystones.LOG.warn("forceGlobal requires name override; disabling forceGlobal for rule: {}", trimmedLine);
            forceGlobal = false;
        }

        return new StructureWaystoneRule(
            structureId,
            chance,
            typeOverride,
            nameOverride,
            forceGlobal,
            autoActivateGlobal,
            dimensionWhitelist,
            biomeWhitelist);
    }

    private Set<Integer> parseDimensionWhitelist(String value, String fullRule) {
        if (value == null || value.trim()
            .isEmpty()
            || value.trim()
                .equals("*")) {
            return null;
        }

        Set<Integer> dimensions = new HashSet<>();
        for (String token : value.split(",")) {
            String dimText = token.trim();
            if (dimText.isEmpty()) {
                continue;
            }
            try {
                dimensions.add(Integer.parseInt(dimText));
            } catch (NumberFormatException ex) {
                Waystones.LOG.warn("Invalid dimension id '{}' in structure waystone rule: {}", dimText, fullRule);
            }
        }
        return dimensions.isEmpty() ? null : dimensions;
    }

    private Set<Integer> parseBiomeWhitelist(String value, String fullRule) {
        if (value == null || value.trim()
            .isEmpty()
            || value.trim()
                .equals("*")) {
            return null;
        }

        Set<Integer> biomes = new HashSet<>();
        for (String token : value.split(",")) {
            String biomeText = token.trim();
            if (biomeText.isEmpty()) {
                continue;
            }
            try {
                biomes.add(Integer.parseInt(biomeText));
            } catch (NumberFormatException ex) {
                Waystones.LOG.warn("Invalid biome id '{}' in structure waystone rule: {}", biomeText, fullRule);
            }
        }
        return biomes.isEmpty() ? null : biomes;
    }

    private String normalizeStructureId(String structureId) {
        return structureId == null ? ""
            : structureId.trim()
                .toLowerCase();
    }

    public enum WaystoneTypeOverride {

        AUTO,
        STONE,
        SANDY,
        MOSSY;

        public static WaystoneTypeOverride fromConfig(String value) {
            if (value == null) {
                return AUTO;
            }

            String normalized = value.trim()
                .toLowerCase();
            switch (normalized) {
                case "stone":
                    return STONE;
                case "sandy":
                case "sandstone":
                    return SANDY;
                case "mossy":
                    return MOSSY;
                default:
                    return AUTO;
            }
        }

        public int toVariant() {
            switch (this) {
                case STONE:
                    return TileWaystone.VARIANT_STONE;
                case SANDY:
                    return TileWaystone.VARIANT_SANDSTONE;
                case MOSSY:
                    return TileWaystone.VARIANT_MOSSY;
                default:
                    return TileWaystone.VARIANT_STONE;
            }
        }
    }

    public static class StructureWaystoneRule {

        public final String structureId;
        public final float chance;
        public final WaystoneTypeOverride typeOverride;
        public final String nameOverride;
        public final boolean forceGlobal;
        public final boolean autoActivateGlobal;
        public final Set<Integer> dimensionWhitelist;
        public final Set<Integer> biomeWhitelist;

        private StructureWaystoneRule(String structureId, float chance, WaystoneTypeOverride typeOverride,
            String nameOverride, boolean forceGlobal, boolean autoActivateGlobal, Set<Integer> dimensionWhitelist,
            Set<Integer> biomeWhitelist) {
            this.structureId = structureId;
            this.chance = chance;
            this.typeOverride = typeOverride;
            this.nameOverride = nameOverride;
            this.forceGlobal = forceGlobal;
            this.autoActivateGlobal = autoActivateGlobal;
            this.dimensionWhitelist = dimensionWhitelist != null
                ? Collections.unmodifiableSet(new HashSet<>(dimensionWhitelist))
                : null;
            this.biomeWhitelist = biomeWhitelist != null ? Collections.unmodifiableSet(new HashSet<>(biomeWhitelist))
                : null;
        }

        public boolean dimensionAllowed(int dimensionId) {
            return dimensionWhitelist == null || dimensionWhitelist.contains(dimensionId);
        }

        public boolean biomeAllowed(int biomeId) {
            return biomeWhitelist == null || biomeWhitelist.contains(biomeId);
        }

        @Override
        public String toString() {
            return "StructureWaystoneRule{" + "structureId='"
                + structureId
                + '\''
                + ", chance="
                + chance
                + ", typeOverride="
                + typeOverride
                + ", nameOverride='"
                + nameOverride
                + '\''
                + ", forceGlobal="
                + forceGlobal
                + ", autoActivateGlobal="
                + autoActivateGlobal
                + ", dimensionWhitelist="
                + (dimensionWhitelist == null ? "*" : Arrays.toString(dimensionWhitelist.toArray()))
                + ", biomeWhitelist="
                + (biomeWhitelist == null ? "*" : Arrays.toString(biomeWhitelist.toArray()))
                + '}';
        }
    }
}
