package o.zimmre.simpletl2.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import o.zimmre.simpletl2.RemoteApi;
import o.zimmre.simpletl2.utils.DisplayHelper;

public class CameraControl {
    private static final String TAG = CameraControl.class.getName();

    private final RemoteApi remoteApi;
    private final Context context;
    private final AlarmManager alarmManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CameraControl(Context context, RemoteApi remoteApi) {
        this.remoteApi = remoteApi;
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);

    }

    public void start() {
        executorService.submit(() -> withToast(remoteApi::startRecMode));
    }

    public void checkStatus(Consumer<String> consumer) {
        executorService.submit(() ->
                withToast(() ->
                        consumer.accept(remoteApi.getStatus())
                )
        );
    }

    /**
     * Shoots using camera set shutter speed
     */
    public void shoot() {
        executorService.submit(() -> withToast(remoteApi::actTakePicture));
    }

    /**
     * Shoots in bulb mode
     *
     * @param bulbDelay - bulb delay
     */
    public void shoot(Long bulbDelay) {
        final Intent intent = new Intent(BulbService.START, Uri.parse("camera://shoot/bulb?" + String.valueOf(bulbDelay)), context, BulbService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
    }

    private void withToast(Runnable r) {
        try {
            r.run();
        } catch (RuntimeException e) {
            Log.w(TAG, "Error when executing camera operation", e);
            DisplayHelper.toast(context, e.getMessage());
        }
    }

}
