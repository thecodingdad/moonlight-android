package com.limelight.binding.input;

import android.view.InputDevice;

/**
 * Per-controller overrides of the global gamepad settings, supplied by the embedding app
 * (Moonlight Launcher). Null fields mean "use the global setting". Kept in its own file so the
 * changes to ControllerHandler stay small.
 */
public final class ControllerOverrides {
    public static final class Override {
        public Boolean flipFaceButtons;
        public Boolean touchpadAsMouse;
        public Boolean motionSensors;
        public Integer deadzonePercent;
        /** 0 = rumble off, 100 = full strength. */
        public Integer rumblePercent;
        /** True: the device vibrates instead of the controller. */
        public Boolean deviceRumble;
        /** True: the device vibrates when the controller has no rumble of its own. */
        public Boolean vibrateFallback;
        /** Strength (percent) of the device's vibration for this controller. */
        public Integer deviceRumbleStrength;
        /** True: use the device's motion sensors when the controller has none. */
        public Boolean motionFallback;
        /** Report the controller's battery to the host. */
        public Boolean batteryReport;
        /** Preferred player slot (0-based) when multiple controllers are used. */
        public Integer playerSlot;
        /** True: don't auto-attach this device at stream start (e.g. built-in pad while an
         *  external one is connected). It still attaches if the user actually uses it. */
        public Boolean skipAutoAttach;
    }

    public interface Provider {
        /** Overrides for a device, keyed by its stable descriptor; null if none. */
        Override forDevice(String descriptor);
    }

    private static volatile Provider provider;

    /** True: attach the connected controllers at stream start instead of on their first input. */
    public static volatile boolean attachAtStart;

    /** Descriptor of the controller that started the stream (it becomes player 1); null if none. */
    public static volatile String starterDescriptor;

    private ControllerOverrides() {}

    public static void setProvider(Provider p) {
        provider = p;
    }

    static Override get(InputDevice dev) {
        Provider p = provider;
        if (p == null || dev == null) {
            return null;
        }
        try {
            return p.forDevice(dev.getDescriptor());
        } catch (RuntimeException e) {
            return null;
        }
    }

    /** True if another connected controller prefers this player slot. */
    static boolean slotPreferredByOther(int slot, InputDevice self) {
        if (provider == null) {
            return false;
        }
        for (int id : InputDevice.getDeviceIds()) {
            InputDevice dev = InputDevice.getDevice(id);
            if (dev == null || (self != null && dev.getDescriptor().equals(self.getDescriptor()))) {
                continue;
            }
            Override o = get(dev);
            if (o != null && o.playerSlot != null && o.playerSlot == slot) {
                return true;
            }
        }
        return false;
    }
}
