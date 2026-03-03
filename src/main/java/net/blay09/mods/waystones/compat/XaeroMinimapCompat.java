package net.blay09.mods.waystones.compat;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import net.blay09.mods.waystones.WaystoneConfig;
import net.blay09.mods.waystones.Waystones;
import net.blay09.mods.waystones.util.BlockPos;

import cpw.mods.fml.common.Loader;

public class XaeroMinimapCompat {

    private static final int DEFAULT_WAYPOINT_COLOR = 11;

    public static void addOrUpdateWaypoint(String waystoneName, int dimensionId, BlockPos pos) {
        if (!WaystoneConfig.xaeroMinimapWaypoints || !isXaeroMinimapLoaded()) {
            return;
        }

        try {
            Object waypointsManager = getWaypointsManager();
            if (waypointsManager == null) {
                return;
            }

            Object currentWorld = getCurrentWorld(waypointsManager);
            if (currentWorld == null) {
                return;
            }

            Object waypoint = findWaypointByName(currentWorld, waystoneName);
            int color = resolveWaypointColor(waystoneName);
            if (waypoint != null) {
                updateWaypoint(waypoint, waystoneName, pos, color);
            } else {
                addWaypoint(currentWorld, createWaypoint(waystoneName, pos, color));
            }

            saveWaypoints(waypointsManager, currentWorld);
        } catch (Exception e) {
            if (WaystoneConfig.debugMode || Waystones.DEBUG_MODE) {
                Waystones.LOG.warn("Failed to update Xaero's Minimap waypoint for {}", waystoneName, e);
            }
        }
    }

    public static void renameWaypoint(String oldWaystoneName, String newWaystoneName, int dimensionId, BlockPos pos) {
        if (!WaystoneConfig.xaeroMinimapWaypoints || !isXaeroMinimapLoaded()) {
            return;
        }

        try {
            Object waypointsManager = getWaypointsManager();
            if (waypointsManager == null) {
                return;
            }

            Object currentWorld = getCurrentWorld(waypointsManager);
            if (currentWorld == null) {
                return;
            }

            Object waypoint = findWaypointByName(currentWorld, oldWaystoneName);
            if (waypoint == null) {
                return;
            }

            updateWaypoint(waypoint, newWaystoneName, pos, resolveWaypointColor(newWaystoneName));
            saveWaypoints(waypointsManager, currentWorld);
        } catch (Exception e) {
            if (WaystoneConfig.debugMode || Waystones.DEBUG_MODE) {
                Waystones.LOG.warn(
                    "Failed to rename Xaero's Minimap waypoint from {} to {}",
                    oldWaystoneName,
                    newWaystoneName,
                    e);
            }
        }
    }

    private static boolean isXaeroMinimapLoaded() {
        return Loader.isModLoaded("XaeroMinimap") || Loader.isModLoaded("xaerominimap");
    }

    private static Object getWaypointsManager() throws Exception {
        Class<?> sessionClass = Class.forName("xaero.common.XaeroMinimapSession");
        Object session = sessionClass.getMethod("getCurrentSession")
            .invoke(null);
        if (session == null) {
            return null;
        }
        return sessionClass.getMethod("getWaypointsManager")
            .invoke(session);
    }

    private static Object getCurrentWorld(Object waypointsManager) throws Exception {
        return waypointsManager.getClass()
            .getMethod("getCurrentWorld")
            .invoke(waypointsManager);
    }

    private static Object getCurrentSet(Object currentWorld) throws Exception {
        Object currentSet = currentWorld.getClass()
            .getMethod("getCurrentSet")
            .invoke(currentWorld);
        if (currentSet != null) {
            return currentSet;
        }

        @SuppressWarnings("unchecked")
        Map<String, Object> sets = (Map<String, Object>) currentWorld.getClass()
            .getMethod("getSets")
            .invoke(currentWorld);
        if (sets == null || sets.isEmpty()) {
            return null;
        }
        return sets.values()
            .iterator()
            .next();
    }

    private static Object findWaypointByName(Object currentWorld, String waystoneName) throws Exception {
        @SuppressWarnings("unchecked")
        Map<String, Object> sets = (Map<String, Object>) currentWorld.getClass()
            .getMethod("getSets")
            .invoke(currentWorld);
        if (sets == null) {
            return null;
        }

        for (Object set : sets.values()) {
            @SuppressWarnings("unchecked")
            List<Object> waypoints = (List<Object>) set.getClass()
                .getMethod("getList")
                .invoke(set);
            if (waypoints == null) {
                continue;
            }

            for (Object waypoint : waypoints) {
                Method getNameMethod = waypoint.getClass()
                    .getMethod("getName");
                if (waystoneName.equals(getNameMethod.invoke(waypoint))) {
                    return waypoint;
                }
            }
        }

        return null;
    }

    private static Object createWaypoint(String waystoneName, BlockPos pos, int color) throws Exception {
        Class<?> waypointClass = Class.forName("xaero.common.minimap.waypoints.Waypoint");
        Constructor<?> constructor = waypointClass
            .getConstructor(Integer.TYPE, Integer.TYPE, Integer.TYPE, String.class, String.class, Integer.TYPE);
        return constructor.newInstance(
            pos.getX(),
            getWaypointY(pos),
            pos.getZ(),
            waystoneName,
            getWaypointSymbol(waystoneName),
            color);
    }

    private static void updateWaypoint(Object waypoint, String waystoneName, BlockPos pos, int color) throws Exception {
        Class<?> waypointClass = waypoint.getClass();
        waypointClass.getMethod("setX", Integer.TYPE)
            .invoke(waypoint, pos.getX());
        waypointClass.getMethod("setY", Integer.TYPE)
            .invoke(waypoint, getWaypointY(pos));
        waypointClass.getMethod("setZ", Integer.TYPE)
            .invoke(waypoint, pos.getZ());
        waypointClass.getMethod("setName", String.class)
            .invoke(waypoint, waystoneName);
        waypointClass.getMethod("setSymbol", String.class)
            .invoke(waypoint, getWaypointSymbol(waystoneName));
        waypointClass.getMethod("setColor", Integer.TYPE)
            .invoke(waypoint, color);
        waypointClass.getMethod("setDisabled", Boolean.TYPE)
            .invoke(waypoint, false);
    }

    private static void addWaypoint(Object currentWorld, Object waypoint) throws Exception {
        Object currentSet = getCurrentSet(currentWorld);
        if (currentSet == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Object> waypoints = (List<Object>) currentSet.getClass()
            .getMethod("getList")
            .invoke(currentSet);
        if (waypoints == null) {
            return;
        }

        if (addWaypointsAtBottom()) {
            waypoints.add(waypoint);
        } else {
            waypoints.add(0, waypoint);
        }
    }

    private static void saveWaypoints(Object waypointsManager, Object currentWorld) throws Exception {
        waypointsManager.getClass()
            .getMethod("updateWaypoints")
            .invoke(waypointsManager);

        Object xaeroMinimap = getXaeroMinimapInstance();
        if (xaeroMinimap == null) {
            return;
        }

        Object settings = xaeroMinimap.getClass()
            .getMethod("getSettings")
            .invoke(xaeroMinimap);
        settings.getClass()
            .getMethod("saveWaypoints", currentWorld.getClass())
            .invoke(settings, currentWorld);
    }

    private static Object getXaeroMinimapInstance() throws Exception {
        Class<?> xaeroMinimapClass = Class.forName("xaero.minimap.XaeroMinimap");
        Field instanceField = xaeroMinimapClass.getField("instance");
        return instanceField.get(null);
    }

    private static boolean addWaypointsAtBottom() throws Exception {
        Object xaeroMinimap = getXaeroMinimapInstance();
        if (xaeroMinimap == null) {
            return true;
        }

        Object settings = xaeroMinimap.getClass()
            .getMethod("getSettings")
            .invoke(xaeroMinimap);
        Field waypointsBottomField = settings.getClass()
            .getField("waypointsBottom");
        return waypointsBottomField.getBoolean(settings);
    }

    private static int resolveWaypointColor(String waystoneName) {
        if (WaystoneConfig.xaeroMinimapWaypointRandomColor) {
            return (waystoneName.hashCode() & Integer.MAX_VALUE) % 16;
        }

        return WaystoneConfig.xaeroMinimapWaypointColor % 16;
    }

    private static int getWaypointY(BlockPos pos) {
        return pos.getY() + WaystoneConfig.xaeroMinimapWaypointYOffset;
    }

    private static String getWaypointSymbol(String waystoneName) {
        for (int i = 0; i < waystoneName.length(); i++) {
            char c = waystoneName.charAt(i);
            if (!Character.isWhitespace(c)) {
                return String.valueOf(Character.toUpperCase(c));
            }
        }

        return "W";
    }
}
