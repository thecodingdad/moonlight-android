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
        /** Preferred player slot (0-based) when multiple controllers are used. */
        public Integer playerSlot;
    }

    public interface Provider {
        /** Overrides for a device, keyed by its stable descriptor; null if none. */
        Override forDevice(String descriptor);
    }

    private static volatile Provider provider;

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
