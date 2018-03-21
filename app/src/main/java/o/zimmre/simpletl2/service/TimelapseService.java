package o.zimmre.simpletl2.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import o.zimmre.simpletl2.RemoteApi;
import o.zimmre.simpletl2.SampleApplication;

import static o.zimmre.simpletl2.utils.DisplayHelper.withToast;

public class TimelapseService extends Service {

    private static final String TAG = TimelapseService.class.getName();

    public static final String STATUS = "o.zimmre.simpletl2.service.TimelapseService.STATUS";
    private PowerManager.WakeLock wakeLock;

    // poor man state machine
    public enum Action {
        START,
        SCHEDULE,
        SHOOT,
        STOP,
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
                        return super.process(Action.STOP, service, intent);
                }
            }
        },
        SCHEDULING {
            @Override
            State process(Action action, TimelapseService service, Intent intent) {
                switch (action) {
                    case SCHEDULE:
                        service.wakeLock.acquire(service.delay*2);
                        if (service.counter.getAndDecrement() < 0) {
                            service.transit(Action.STOP);
                            return DEAD;
                        }
                        service.transit(Action.SHOOT);

                        LocalBroadcastManager.getInstance(service)
                                .sendBroadcast(
                                        new Intent(STATUS)
                                                .putExtra(STATUS, service.counter.get()));
                        return SHOOTING;
                    default:
                        return super.process(action, service, intent);
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
                        return super.process(action, service, intent);
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

        State process(Action action, TimelapseService service, Intent intent) {
            if (action == Action.STOP) {
                service.transit(Action.STOP);
                return DEAD;
            }
            return this;
        }
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
        Log.w(TAG, "TimelapseService created");
        final SampleApplication application = (SampleApplication) getApplication();
        remoteApi = application.getRemoteApi();
        executorService = Executors.newSingleThreadExecutor();
        context = getApplicationContext();
        alarmManager = context.getSystemService(AlarmManager.class);
        state = State.INIT;
        PowerManager powerManager = context.getSystemService(PowerManager.class);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, this.getClass().getName());
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
        executorService.submit(() -> withToast(() -> {
            remoteApi.startRecMode();
            remoteApi.actTakePicture();
        }, context));
    }

    private void init(Uri data) {
        this.counter = new AtomicLong(Long.valueOf(data.getQueryParameter("count")));
        this.delay = Long.valueOf(data.getQueryParameter("delay")) * 1000;
    }

    private void transit(Action action) {
        transit(action, 0);
    }

    private void transit(Action schedule, long delay) {
        final Intent intent = new Intent(schedule.toString(), null, context, TimelapseService.class);
        final PendingIntent pendingIntent = PendingIntent.getService(context, 0, intent, 0);
        alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delay, pendingIntent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
