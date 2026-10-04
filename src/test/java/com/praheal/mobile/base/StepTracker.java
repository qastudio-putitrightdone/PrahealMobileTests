package com.praheal.mobile.base;

public final class StepTracker {

    private static final ThreadLocal<Integer> DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<String> LAST_TOP_LEVEL_STEP = new ThreadLocal<>();

    private StepTracker() {
    }

    static void enter(String methodName) {
        if (DEPTH.get() == 0) {
            LAST_TOP_LEVEL_STEP.set(methodName);
        }
        DEPTH.set(DEPTH.get() + 1);
    }

    static void exit() {
        DEPTH.set(DEPTH.get() - 1);
    }

    public static String getLastTopLevelStep() {
        return LAST_TOP_LEVEL_STEP.get();
    }

    public static void reset() {
        DEPTH.set(0);
        LAST_TOP_LEVEL_STEP.remove();
    }
}
