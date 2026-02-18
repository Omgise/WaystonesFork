package net.blay09.mods.waystones.varinstances;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.block.TileWaystone;
import net.minecraft.client.settings.KeyBinding;

import org.lwjgl.input.Keyboard;

import cpw.mods.fml.client.registry.ClientRegistry;

public class VarInstanceClient {

    public KeyBinding closeGuiKey;

    private static final float SCALE = 0.0625f;
    private static final OverlayClipBounds DEFAULT_BOUNDS = new OverlayClipBounds(-18 * SCALE, -48 * SCALE);

    private final Map<Integer, OverlayClipBounds> overlayClipBounds = new HashMap<>();

    public void initHook() {
        closeGuiKey = new KeyBinding("key.waystones.closegui", Keyboard.KEY_NONE, "key.categories.waystones");
        ClientRegistry.registerKeyBinding(closeGuiKey);

        rebuildOverlayClipBoundsCache();
    }

    public void rebuildOverlayClipBoundsCache() {
        overlayClipBounds.clear();
        String[] lines = WaystoneConfig.overlayClipBounds;
        if (lines == null) {
            return;
        }

        for (String line : lines) {
            if (line == null) {
                continue;
            }
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                continue;
            }

            String variantName = null;
            float lower = -18f;
            float upper = -48f;

            for (String segment : trimmed.split(";")) {
                String token = segment.trim();
                if (token.isEmpty()) {
                    continue;
                }

                int eq = token.indexOf('=');
                if (eq <= 0) {
                    Waystones.LOG.warn("Invalid overlay clip bounds token '{}': {}", token, trimmed);
                    continue;
                }

                String key = token.substring(0, eq)
                    .trim()
                    .toLowerCase(Locale.ROOT);
                String value = token.substring(eq + 1)
                    .trim();

                switch (key) {
                    case "variant":
                        variantName = value.toLowerCase(Locale.ROOT);
                        break;
                    case "lower":
                        try {
                            lower = Float.parseFloat(value);
                        } catch (NumberFormatException ex) {
                            Waystones.LOG.warn("Invalid lower bound '{}' in overlay clip bounds: {}", value, trimmed);
                        }
                        break;
                    case "upper":
                        try {
                            upper = Float.parseFloat(value);
                        } catch (NumberFormatException ex) {
                            Waystones.LOG.warn("Invalid upper bound '{}' in overlay clip bounds: {}", value, trimmed);
                        }
                        break;
                    default:
                        Waystones.LOG.warn("Unknown overlay clip bounds key '{}': {}", key, trimmed);
                }
            }

            if (variantName == null || variantName.isEmpty()) {
                Waystones.LOG.warn("Overlay clip bounds entry missing variant: {}", trimmed);
                continue;
            }

            int variantId = variantNameToId(variantName);
            if (variantId < 0) {
                Waystones.LOG.warn("Unknown variant '{}' in overlay clip bounds: {}", variantName, trimmed);
                continue;
            }

            overlayClipBounds.put(variantId, new OverlayClipBounds(lower * SCALE, upper * SCALE));
        }
    }

    public OverlayClipBounds getOverlayClipBounds(int variant) {
        OverlayClipBounds bounds = overlayClipBounds.get(variant);
        return bounds != null ? bounds : DEFAULT_BOUNDS;
    }

    private static int variantNameToId(String name) {
        switch (name) {
            case "stone":
                return TileWaystone.VARIANT_STONE;
            case "sandstone":
            case "sandy":
                return TileWaystone.VARIANT_SANDSTONE;
            case "mossy":
                return TileWaystone.VARIANT_MOSSY;
            case "stonebrick":
                return TileWaystone.VARIANT_STONEBRICK;
            case "mossy_stonebrick":
            case "mossystonebrick":
                return TileWaystone.VARIANT_MOSSY_STONEBRICK;
            case "nether":
            case "netherbrick":
                return TileWaystone.VARIANT_NETHER;
            case "end":
            case "endstone":
                return TileWaystone.VARIANT_END;
            default:
                return -1;
        }
    }

    public static class OverlayClipBounds {

        public final float lower;
        public final float upper;

        public OverlayClipBounds(float lower, float upper) {
            this.lower = lower;
            this.upper = upper;
        }
    }
}
