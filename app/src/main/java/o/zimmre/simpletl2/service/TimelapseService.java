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
import java.util.concurrent.atomic.AtomicLong;

import o.zimmre.simpletl2.RemoteApi;
import o.zimmre.simpletl2.SampleApplication;

import static o.zimmre.simpletl2.utils.DisplayHelper.withToast;

public class TimelapseService extends Service {

    private static final String TAG = TimelapseService.class.getName();

    // poor man state machine
    public enum Action {
        START,
        SCHEDULE,
        SHOOT,
        STOP
    }

    private enum State {
        INIT {
            @Override
            State process(Action action, TimelapseService service, Intent intent) {
                switch (action) {
                    case START:
                        service.init(intent.getData());
                        service.transit(Action.SCHEDULE);
                        return SCHEDULING;
                    default:
                        service.transit(Action.STOP);
                        return DEAD;
                }
            }
        },
        SCHEDULING {
            @Override
            State process(Action action, TimelapseService service, Intent intent) {
                switch (action) {
                    case SCHEDULE:
                        if (service.counter.get() <= 0) {
                            service.transit(Action.STOP);
                            return DEAD;
                        }
                        service.transit(Action.SHOOT);
                        return SHOOTING;
                    default:
                        return this;
                }
            }
        },
        SHOOTING {
            @Override
            State process(Action action, TimelapseService service, Intent intent) {
                switch (action) {
                    case SHOOT:
                        service.takePicture();
                        service.transit(Action.SCHEDULE, service.delay);
                        return SCHEDULING;
                    default:
                        return this;
                }
            }
        },
        DEAD {
            @Override
            State process(Action action, TimelapseService service, Intent intent) {
                service.stopSelf();
                return this;
            }
        };

        abstract State process(Action action, TimelapseService service, Intent intent);
    }

    private Context context;
    private RemoteApi remoteApi;
    private ExecutorService executorService;
    private AlarmManager alarmManager;

    private State state;
    private long delay = 0;
    private AtomicLong counter = new AtomicLong();

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

    private void takePicture() {
        counter.decrementAndGet();
        executorService.submit(() -> withToast(remoteApi::actTakePicture, context));
    }

    private void init(Uri data) {
        this.counter = new AtomicLong(Long.valueOf(data.getQueryParameter("count")));
        this.delay = Long.valueOf(data.getQueryParameter("delay")) * 1000;
    }

    private void transit(Action schedule) {
        transit(schedule, 0);
    }

    private void transit(Action schedule, long delay) {
        final Intent intent = new Intent(schedule.toString(), null, context, TimelapseService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + delay, pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
