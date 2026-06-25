package com.PorTracker.PorTrackerBE.global.common;

public class ReplayContextHolder {
    private static final ThreadLocal<Boolean> IS_REPLAYING = ThreadLocal.withInitial(() -> false);

    public static void setReplaying(boolean isReplaying) {
        IS_REPLAYING.set(isReplaying);
    }

    public static boolean isReplaying() {
        return IS_REPLAYING.get();
    }

    public static void clear() {
        IS_REPLAYING.remove();
    }
}
