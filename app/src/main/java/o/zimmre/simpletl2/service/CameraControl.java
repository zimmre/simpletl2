package o.zimmre.simpletl2.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.support.annotation.NonNull;

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

    private ExecutorService executorService;

    public CameraControl(Context context, RemoteApi remoteApi) {
        this.remoteApi = remoteApi;
        this.context = context;
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    public void start() {
        initCamera();
    }

    public synchronized void stop() {
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
                new Uri.Builder().query(Long.toString(bulbDelay)).build(),
                context,
                BulbService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
    }

    public void shootTimelapse(long delay, long count) {
        final Intent intent = new Intent(TimelapseService.Action.START.toString(),
                new Uri.Builder()
                        .appendQueryParameter("count", Long.toString(count))
                        .appendQueryParameter("delay", Long.toString(delay))
                        .build(),
                context,
                TimelapseService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
    }

    private void submit(Runnable r) {
        executor().submit(r);
    }

    @NonNull
    private synchronized ExecutorService executor() {
        if (executorService == null) {
            executorService = Executors.newSingleThreadExecutor();
        }
        return this.executorService;
    }

    public void stopTimelapse() {
        final Intent intent = new Intent(TimelapseService.Action.STOP.toString(),
                null,
                context,
                TimelapseService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime(), pendingIntent);
    }

}
