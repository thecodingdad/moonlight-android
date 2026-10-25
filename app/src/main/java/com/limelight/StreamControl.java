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

    /**
     * Artemis' own controller combos (Start held: mouse emulation / game menu, Start+Select+LB+RB:
     * quit); off when the embedding app maps these actions to its own combinations.
     */
    public static volatile boolean builtInCombos = true;

    /** Shown instead of Artemis' game menu when set (the embedding app's own menu). */
    public static volatile Runnable menuAction;

    /**
     * Mouse emulation of every controller, set by the embedding app (also for controllers that
     * appear later and for the next stream). Its pointer speed in host pixels per second at full
     * stick deflection, and the curve exponent (higher: slower near the centre).
     */
    public static volatile boolean mouseEmulation;
    public static volatile float mouseSpeed = 1200f;
    public static volatile float mouseExponent = 2.4f;

    /** Switches [mouseEmulation] and applies it to the running stream. */
    public static void setMouseEmulation(boolean on) {
        mouseEmulation = on;
        Game game = Game.instance;
        if (game != null) game.applyMouseEmulation(on);
    }

    /** Shows or hides the performance overlay of the running stream; false without one. */
    public static boolean togglePerformanceOverlay() {
        Game game = Game.instance;
        if (game == null) return false;
        game.runOnUiThread(game::toggleHUD);
        return true;
    }

    private StreamControl() {
    }
}
