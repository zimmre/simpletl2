package o.zimmre.simpletl2.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import o.zimmre.simpletl2.RemoteApi;
import o.zimmre.simpletl2.utils.DisplayHelper;

// not thread safe
public class CameraControl {
    private static final String TAG = CameraControl.class.getName();

    private final RemoteApi remoteApi;
    private final Context context;
    private final AlarmManager alarmManager;

    private ExecutorService executorService;

    public CameraControl(Context context, RemoteApi remoteApi) {
        this.remoteApi = remoteApi;
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    public void start() {
        initCamera();
    }

    public void stop() {
        if (executorService != null) {
            executorService.shutdown();
            executorService = null;
        }
    }

    public void initCamera() {
        submit(() -> DisplayHelper.withToast(remoteApi::startRecMode, context));
    }

    public void checkStatus(Consumer<String> consumer) {
        submit(() ->
                DisplayHelper.withToast(() ->
                        consumer.accept(remoteApi.getStatus()), context
                )
        );
    }

    /**
     * Shoots using camera set shutter speed
     */
    public void shoot() {
        submit(() -> DisplayHelper.withToast(remoteApi::actTakePicture, context));
    }

    /**
     * Shoots in bulb mode
     *
     * @param bulbDelay - bulb delay
     */
    public void shoot(Long bulbDelay) {
        final Intent intent = new Intent(BulbService.Action.START.toString(),
                Uri.parse("camera://shoot/bulb?" + String.valueOf(bulbDelay)),
                context,
                BulbService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
    }

    private void submit(Runnable r) {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        executorService.submit(r);
    }

}
