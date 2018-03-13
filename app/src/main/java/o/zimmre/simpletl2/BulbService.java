package o.zimmre.simpletl2;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import o.zimmre.simpletl2.utils.DisplayHelper;

public class BulbService extends Service {
    public static final String START = "bulb";
    public static final String STOP_AND_DESTROY = "stopAndDestroy";
    private static final String TAG = BulbService.class.getName();

    private Context context;
    private RemoteApi remoteApi;
    private ExecutorService executorService;
    private AlarmManager alarmManager;

    private boolean shooting = false;

    @Override
    public void onCreate() {
        Log.w(TAG, "Service created");
        final SampleApplication application = (SampleApplication) getApplication();
        remoteApi = application.getRemoteApi();
        executorService = Executors.newSingleThreadExecutor();
        context = getApplicationContext();
        alarmManager = context.getSystemService(AlarmManager.class);
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Service destroyed");
        executorService.shutdownNow();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        final String action = intent.getAction();
        Log.w(TAG, this.toString() + " recieved intent. action: " + action);
        if (action == null) {
            if (!shooting) {
                stopSelf();
            }
            return START_STICKY;
        }
        switch (action) {
            case START:
                startBulb(intent.getData());
                break;
            case STOP_AND_DESTROY:
                executorService.submit(() -> withToast(remoteApi::stopBulbShooting, true));
                break;
            default:
                stopSelf();
        }

        return START_STICKY;
    }

    private void startBulb(Uri data) {
        if (shooting) {
            return;
        }
        try {
            final String query = data.getQuery();
            final Long delay = Long.valueOf(query) * 1000;
            final Intent intent = new Intent(BulbService.STOP_AND_DESTROY, null, context, BulbService.class);
            final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
            alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
            executorService.submit(() -> withToast(remoteApi::startBulbShooting, false));
            shooting = true;
        } catch (Exception e) {
            Log.e(TAG, "Failed to start bulb shooting", e);
            stopSelf();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void withToast(Runnable r, boolean destroy) {
        try {
            r.run();
        } catch (RuntimeException e) {
            Log.w(TAG, "Error when executing camera operation", e);
            DisplayHelper.toast(context, e.getMessage());
        } finally {
            if (destroy) {
                stopSelf();
            }
        }
    }

}
