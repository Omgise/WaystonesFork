package net.blay09.mods.waystones.util;

public class MiscUtil {

    // Source:
    // https://github.com/GTNewHorizons/Applied-Energistics-2-Unofficial/blob/master/src/main/java/appeng/util/Platform.java
    /**
     * @return (a divided by b) rounded up
     */
    public static long ceilDiv(long a, long b) {
        return Math.addExact(Math.addExact(a, b), -1) / b;
    }
}
