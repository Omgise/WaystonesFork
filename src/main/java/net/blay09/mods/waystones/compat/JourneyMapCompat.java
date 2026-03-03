package net.blay09.mods.waystones.compat;

import java.awt.Color;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.util.BlockPos;

import cpw.mods.fml.common.Loader;

public class JourneyMapCompat {

    private static final int DEFAULT_WAYPOINT_COLOR = 0x7FDBFF;

    public static void addOrUpdateWaypoint(String waystoneName, int dimensionId, BlockPos pos) {
        if (!WaystoneConfig.journeyMapWaypoints || !Loader.isModLoaded("journeymap")) {
            return;
        }

        try {
            if (!isManagerEnabled()) {
                return;
            }

            Object waypointStore = getWaypointStore();
            if (waypointStore == null || !hasLoaded(waypointStore)) {
                return;
            }

            Object waypoint = findWaypointByName(waypointStore, waystoneName);
            int color = resolveWaypointColor(waystoneName);
            if (waypoint != null) {
                updateWaypoint(waypoint, waystoneName, dimensionId, pos, color);
            } else {
                waypoint = createWaypoint(waystoneName, dimensionId, pos, color);
            }

            if (waypoint != null) {
                saveWaypoint(waypointStore, waypoint);
            }
        } catch (Exception e) {
            if (WaystoneConfig.debugMode || Waystones.DEBUG_MODE) {
                Waystones.LOG.warn("Failed to update JourneyMap waypoint for {}", waystoneName, e);
            }
        }
    }

    public static void renameWaypoint(String oldWaystoneName, String newWaystoneName, int dimensionId, BlockPos pos) {
        if (!WaystoneConfig.journeyMapWaypoints || !Loader.isModLoaded("journeymap")) {
            return;
        }

        try {
            if (!isManagerEnabled()) {
                return;
            }

            Object waypointStore = getWaypointStore();
            if (waypointStore == null || !hasLoaded(waypointStore)) {
                return;
            }

            Object waypoint = findWaypointByName(waypointStore, oldWaystoneName);
            if (waypoint == null) {
                return;
            }

            updateWaypoint(waypoint, newWaystoneName, dimensionId, pos, resolveWaypointColor(newWaystoneName));
            saveWaypoint(waypointStore, waypoint);
        } catch (Exception e) {
            if (WaystoneConfig.debugMode || Waystones.DEBUG_MODE) {
                Waystones.LOG
                    .warn("Failed to rename JourneyMap waypoint from {} to {}", oldWaystoneName, newWaystoneName, e);
            }
        }
    }

    private static boolean isManagerEnabled() throws Exception {
        Class<?> waypointsDataClass = Class.forName("journeymap.client.data.WaypointsData");
        Method isManagerEnabled = waypointsDataClass.getMethod("isManagerEnabled");
        return (Boolean) isManagerEnabled.invoke(null);
    }

    private static Object getWaypointStore() throws Exception {
        Class<?> waypointStoreClass = Class.forName("journeymap.client.waypoint.WaypointStore");
        Method instanceMethod = waypointStoreClass.getMethod("instance");
        return instanceMethod.invoke(null);
    }

    private static boolean hasLoaded(Object waypointStore) throws Exception {
        Method hasLoadedMethod = waypointStore.getClass()
            .getMethod("hasLoaded");
        return (Boolean) hasLoadedMethod.invoke(waypointStore);
    }

    private static Object findWaypointByName(Object waypointStore, String waystoneName) throws Exception {
        Method getAllMethod = waypointStore.getClass()
            .getMethod("getAll");
        Collection<?> waypoints = (Collection<?>) getAllMethod.invoke(waypointStore);
        if (waypoints == null) {
            return null;
        }

        for (Object waypoint : waypoints) {
            Method getNameMethod = waypoint.getClass()
                .getMethod("getName");
            if (waystoneName.equals(getNameMethod.invoke(waypoint))) {
                return waypoint;
            }
        }

        return null;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static Object createWaypoint(String waystoneName, int dimensionId, BlockPos pos, int color)
        throws Exception {
        Class<?> waypointClass = Class.forName("journeymap.client.model.Waypoint");
        Class<? extends Enum> typeClass = (Class<? extends Enum>) Class.forName("journeymap.client.model.Waypoint$Type")
            .asSubclass(Enum.class);
        Constructor<?> constructor = waypointClass.getConstructor(
            String.class,
            Integer.TYPE,
            Integer.TYPE,
            Integer.TYPE,
            Color.class,
            typeClass,
            Integer.class);
        Object normalType = Enum.valueOf(typeClass, "Normal");
        return constructor.newInstance(
            waystoneName,
            pos.getX(),
            getWaypointY(pos),
            pos.getZ(),
            new Color(color),
            normalType,
            Integer.valueOf(dimensionId));
    }

    private static void updateWaypoint(Object waypoint, String waystoneName, int dimensionId, BlockPos pos, int color)
        throws Exception {
        Class<?> waypointClass = waypoint.getClass();
        waypointClass.getMethod("setName", String.class)
            .invoke(waypoint, waystoneName);
        waypointClass.getMethod("setLocation", Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE)
            .invoke(waypoint, pos.getX(), getWaypointY(pos), pos.getZ(), dimensionId);
        waypointClass.getMethod("setColor", Integer.class)
            .invoke(waypoint, Integer.valueOf(color));
        waypointClass.getMethod("setEnable", Boolean.TYPE)
            .invoke(waypoint, true);
    }

    private static void saveWaypoint(Object waypointStore, Object waypoint) throws Exception {
        waypointStore.getClass()
            .getMethod("save", waypoint.getClass())
            .invoke(waypointStore, waypoint);
    }

    private static int resolveWaypointColor(String waystoneName) {
        if (WaystoneConfig.journeyMapWaypointRandomColor) {
            int hash = waystoneName.hashCode();
            float hue = (hash & 0x7FFFFFFF) / (float) Integer.MAX_VALUE;
            return Color.HSBtoRGB(hue, 0.75f, 1f) & 0xFFFFFF;
        }

        return parseWaypointColor();
    }

    private static int parseWaypointColor() {
        String colorString = WaystoneConfig.journeyMapWaypointColor;
        if (colorString == null) {
            return DEFAULT_WAYPOINT_COLOR;
        }

        colorString = colorString.trim();
        if (colorString.startsWith("#")) {
            colorString = colorString.substring(1);
        }

        try {
            return Integer.parseInt(colorString, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return DEFAULT_WAYPOINT_COLOR;
        }
    }

    private static int getWaypointY(BlockPos pos) {
        return pos.getY() + WaystoneConfig.journeyMapWaypointYOffset;
    }
}
