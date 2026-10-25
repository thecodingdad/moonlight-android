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

    /** Toggles mouse emulation of the controller [deviceId] in the running stream; false without one. */
    public static boolean toggleMouseEmulation(int deviceId) {
        Game game = Game.instance;
        return game != null && game.toggleMouseEmulation(deviceId);
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
