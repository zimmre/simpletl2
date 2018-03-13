package o.zimmre.simpletl2.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

public final class DisplayHelper {

    public static void toast(final Context context, final String message) {
        Handler uiHandler = new Handler(context.getMainLooper());
        uiHandler.post(() -> Toast.makeText(context, message, Toast.LENGTH_SHORT).show());
    }

}
