package o.zimmre.simpletl2.utils;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import o.zimmre.simpletl2.service.CameraControl;

public final class DisplayHelper {

    private static final String TAG = DisplayHelper.class.getName();

    public static void toast(final Context context, final String message) {
        Handler uiHandler = new Handler(context.getMainLooper());
        uiHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

    public static void withToast(Runnable r, Context context) {
        try {
            r.run();
        } catch (RuntimeException e) {
            Log.w(TAG, "Error when executing operation", e);
            toast(context, e.getMessage());
        }
    }
}
