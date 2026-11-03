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

    /** Touch mode of the running stream (index of mouse_mode_list), -1 without one. */
    public static int touchMode() {
        Game game = Game.instance;
        return game == null ? -1 : game.currentMouseMode;
    }

    /** Touch modes the running stream's display allows (empty without one). */
    public static int[] touchModes() {
        Game game = Game.instance;
        return game == null ? new int[0] : game.allowedMouseModes();
    }

    /** Switches the running stream's touch mode for this stream only; false without one. */
    public static boolean setTouchMode(int mode) {
        Game game = Game.instance;
        if (game == null) return false;
        game.runOnUiThread(() -> game.applyMouseMode(mode));
        return true;
    }

    /** Shows the system keyboard ([full]: Artemis' PC keyboard) in the running stream; false without one. */
    public static boolean showKeyboard(boolean full) {
        Game game = Game.instance;
        if (game == null) return false;
        game.runOnUiThread(() -> game.requestKeyboard(full));
        return true;
    }

    /** Shows or hides the performance overlay of the running stream; false without one. */
    public static boolean togglePerformanceOverlay() {
        Game game = Game.instance;
        if (game == null) return false;
        game.runOnUiThread(game::toggleHUD);
        return true;
    }

    /**
     * The stream's connection once a second while one runs: round trip time and its variation (ms),
     * Moonlight's poor-connection verdict (lost frames), audio packets dropped since the last call
     * and whether the performance overlay shows. Set: Artemis' own connection warning stays off.
     */
    public interface QualityListener {
        void onQuality(int rttMs, int rttVarianceMs, boolean frameLoss, int audioDrops, boolean perfOverlay);
    }

    public static volatile QualityListener qualityListener;

    private static final java.util.concurrent.atomic.AtomicInteger audioDrops = new java.util.concurrent.atomic.AtomicInteger();

    /** An audio packet was dropped (it came too late or bunched up). */
    public static void audioDropped() {
        audioDrops.incrementAndGet();
    }

    static int takeAudioDrops() {
        return audioDrops.getAndSet(0);
    }

    private StreamControl() {
    }
}
