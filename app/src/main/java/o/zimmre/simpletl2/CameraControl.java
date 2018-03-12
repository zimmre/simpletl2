package o.zimmre.simpletl2;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import o.zimmre.simpletl2.utils.DisplayHelper;

public class CameraControl {
    private static final String TAG = CameraControl.class.getName();

    private final RemoteApi remoteApi;
    private final Context context;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public CameraControl(SimpleRemoteApi simpleRemoteApi, Context context) {
        this.remoteApi = new RemoteApi(simpleRemoteApi);
        this.context = context;
    }

    public void start() {

        executorService.submit(() -> {
            try {
                remoteApi.startRecMode();
            } catch (Exception e) {
                Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                DisplayHelper.toast(context, R.string.msg_error_connection);
            }
        });

    }

    public void checkStatus(Consumer<String> consumer) {
        executorService.submit(()-> {
            consumer.accept(remoteApi.getStatus());
        });
    }
}
