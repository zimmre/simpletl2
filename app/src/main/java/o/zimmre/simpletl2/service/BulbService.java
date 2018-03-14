package o.zimmre.simpletl2.service;

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

import o.zimmre.simpletl2.RemoteApi;
import o.zimmre.simpletl2.SampleApplication;

import static o.zimmre.simpletl2.utils.DisplayHelper.withToast;

public class BulbService extends Service {

    // poor man state machine
    enum State {
        INIT {
            @Override
            State process(Action action, BulbService service, Intent intent) {
                switch (action) {
                    case START:
                        service.startBulb();
                        service.scheduleStop(intent.getData());
                        return SHOOTING;
                    case STOP:
                    default:
                        service.stopSelf();
                        return DEAD;
                }
            }
        },
        SHOOTING {
            @Override
            State process(Action action, BulbService service, Intent intent) {
                switch (action) {
                    case STOP:
                        service.stopBulb();
                        service.stopSelf();
                        return DEAD;
                    case START:
                    default:
                        // do nothing
                        return this;
                }
            }
        },
        DEAD {
            @Override
            State process(Action action, BulbService service, Intent intent) {
                service.stopSelf();
                return this;
            }
        };

        abstract State process(Action action, BulbService service, Intent intent);
    }

    enum Action {
        START,
        STOP
    }

    private static final String TAG = BulbService.class.getName();

    private Context context;
    private RemoteApi remoteApi;
    private ExecutorService executorService;
    private AlarmManager alarmManager;

    private State state;

    @Override
    public void onCreate() {
        Log.w(TAG, "BulbService created");
        final SampleApplication application = (SampleApplication) getApplication();
        remoteApi = application.getRemoteApi();
        executorService = Executors.newSingleThreadExecutor();
        context = getApplicationContext();
        alarmManager = context.getSystemService(AlarmManager.class);
        state = State.INIT;
    }

    @Override
    public void onDestroy() {
        Log.w(TAG, "Service destroyed");
        executorService.shutdown();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        state = state.process(Action.valueOf(intent.getAction()), this, intent);
        return START_STICKY;
    }

    private void stopBulb() {
        executorService.submit(() -> withToast(remoteApi::stopBulbShooting, context));
    }

    private void startBulb() {
        executorService.submit(() -> withToast(remoteApi::startBulbShooting, context));
    }

    private void scheduleStop(Uri data) {
        final String query = data.getQuery();
        final Long delay = Long.valueOf(query) * 1000;

        final Intent intent = new Intent(Action.STOP.toString(), null, context, BulbService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
