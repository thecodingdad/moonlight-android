package com.limelight;

/**
 * Hook for embedding apps: the running stream's performance text while they show it, so their own
 * menus can display it without Artemis' overlay.
 */
public final class StreamControl {
    public interface PerfListener {
        void onPerfUpdate(String text);
    }

    /** Set while an embedding app shows the stats; the renderer computes them then as well. */
    public static volatile PerfListener perfListener;

    private StreamControl() {
    }
}
