package com.limelight.utils;

import java.util.ArrayList;
import java.util.Iterator;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;

public class SpinnerDialog implements Runnable,OnCancelListener {
    /**
     * Moonlight Launcher: lets the embedding app replace the progress dialog with its own
     * (e.g. full-screen) view. Returning null keeps the default dialog.
     */
    public interface Overlay {
        void show();
        void setMessage(String message);
        void dismiss();
        boolean isShowing();
    }

    public interface OverlayFactory {
        Overlay create(Activity activity, String title, String message, boolean cancelable, Runnable onCancel);
    }

    private static volatile OverlayFactory overlayFactory;

    public static void setOverlayFactory(OverlayFactory factory) {
        overlayFactory = factory;
    }

    private final String title;
    private final String message;
    private final Activity activity;
    private ProgressDialog progress;
    private Overlay overlay;
    private boolean created;
    private final boolean finish;

    private static final ArrayList<SpinnerDialog> rundownDialogs = new ArrayList<>();

    private SpinnerDialog(Activity activity, String title, String message, boolean finish)
    {
        this.activity = activity;
        this.title = title;
        this.message = message;
        this.progress = null;
        this.finish = finish;
    }

    public static SpinnerDialog displayDialog(Activity activity, String title, String message, boolean finish)
    {
        SpinnerDialog spinner = new SpinnerDialog(activity, title, message, finish);
        activity.runOnUiThread(spinner);
        return spinner;
    }

    public static void closeDialogs(Activity activity)
    {
        synchronized (rundownDialogs) {
            Iterator<SpinnerDialog> i = rundownDialogs.iterator();
            while (i.hasNext()) {
                SpinnerDialog dialog = i.next();
                if (dialog.activity == activity) {
                    i.remove();
                    if (dialog.overlay != null) {
                        if (dialog.overlay.isShowing()) {
                            dialog.overlay.dismiss();
                        }
                    }
                    else if (dialog.progress.isShowing()) {
                        dialog.progress.dismiss();
                    }
                }
            }
        }
    }

    public void dismiss()
    {
        // Running again with progress != null will destroy it
        activity.runOnUiThread(this);
    }

    public void setMessage(final String message)
    {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (overlay != null) {
                    overlay.setMessage(message);
                }
                else if (progress != null) {
                    progress.setMessage(message);
                }
            }
        });
    }

    @Override
    public void run() {

        // If we're dying, don't bother doing anything
        if (activity.isFinishing()) {
            return;
        }

        if (!created && overlayFactory != null)
        {
            Overlay o = null;
            try {
                o = overlayFactory.create(activity, title, message, finish, new Runnable() {
                    @Override
                    public void run() {
                        onCancel(null);
                    }
                });
            } catch (RuntimeException ignored) {
            }
            if (o != null) {
                created = true;
                overlay = o;
                synchronized (rundownDialogs) {
                    rundownDialogs.add(this);
                    overlay.show();
                }
                return;
            }
        }

        if (overlay != null)
        {
            synchronized (rundownDialogs) {
                if (rundownDialogs.remove(this) && overlay.isShowing()) {
                    overlay.dismiss();
                }
            }
        }
        else if (progress == null)
        {
            created = true;
            progress = new ProgressDialog(activity);

            progress.setTitle(title);
            progress.setMessage(message);
            progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            progress.setOnCancelListener(this);

            // If we want to finish the activity when this is killed, make it cancellable
            if (finish)
            {
                progress.setCancelable(true);
                progress.setCanceledOnTouchOutside(false);
            }
            else
            {
                progress.setCancelable(false);
            }

            synchronized (rundownDialogs) {
                rundownDialogs.add(this);
                progress.show();
            }
        }
        else
        {
            synchronized (rundownDialogs) {
                if (rundownDialogs.remove(this) && progress.isShowing()) {
                    progress.dismiss();
                }
            }
        }
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        synchronized (rundownDialogs) {
            rundownDialogs.remove(this);
        }

        // This will only be called if finish was true, so we don't need to check again
        activity.finish();
    }
}
