package com.limelight.binding.input;

import android.app.Activity;
import android.view.KeyEvent;

/**
 * Hook for embedding apps: sees every key event of the stream before it is handled and can keep
 * it from the host (e.g. a button combination that brings the embedding app to the front).
 */
public final class KeyInterceptor {
    public interface Interceptor {
        /** True = the event is consumed and not sent to the host. */
        boolean intercept(Activity activity, KeyEvent event);
    }

    public static volatile Interceptor interceptor;

    private KeyInterceptor() {}

    public static boolean intercept(Activity activity, KeyEvent event) {
        Interceptor i = interceptor;
        return i != null && i.intercept(activity, event);
    }
}
