package o.zimmre.simpletl2;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.TextView;

import o.zimmre.simpletl2.service.CameraControl;
import o.zimmre.simpletl2.service.TimelapseService;

public class SampleCameraActivity extends Activity {

    private CameraControl cameraControl;
    private MyBroadcastReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_camera);

        SampleApplication app = (SampleApplication) getApplication();
        ServerDevice mTargetServer = app.getTargetServerDevice();

        final RemoteApi remoteApi = new RemoteApi(new SimpleRemoteApi(mTargetServer));
        app.setRemoteApi(remoteApi);
        cameraControl = new CameraControl(getApplicationContext(), remoteApi);

        receiver = new MyBroadcastReceiver();
        LocalBroadcastManager.getInstance(this).registerReceiver(receiver, new IntentFilter(TimelapseService.STATUS));
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        findViewById(R.id.button_take_picture).setOnClickListener(v -> cameraControl.shoot());
        findViewById(R.id.check_status).setOnClickListener(v -> checkStatus());
        findViewById(R.id.init_connection).setOnClickListener(v -> cameraControl.initCamera());
        findViewById(R.id.button_bulb_take_picture).setOnClickListener(v -> shootBulb());
        findViewById(R.id.shoot_timelapse).setOnClickListener(v -> shootTimelapse());
        findViewById(R.id.stop_timelapse).setOnClickListener(v -> cameraControl.stopTimelapse());

        cameraControl.start();
    }

    private void shootTimelapse() {
        TextView delay = findViewById(R.id.timelapse_delay);
        TextView count = findViewById(R.id.pictures_count);
        cameraControl.shootTimelapse(
                Long.valueOf(delay.getText().toString()),
                Long.valueOf(count.getText().toString())
        );
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraControl.stop();
    }

    private void shootBulb() {
        final TextView viewById = findViewById(R.id.shutterSpeed);
        cameraControl.shoot(Long.valueOf(viewById.getText().toString()));
    }

    private void checkStatus() {
        cameraControl.checkStatus(
                status -> runOnUiThread(
                        () -> ((TextView) findViewById(R.id.text_camera_status)).setText(status)
                )
        );
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            ((TextView) findViewById(R.id.timelapse_status))
                    .setText(String.valueOf(intent.getLongExtra(TimelapseService.STATUS, -1)));
        }
    }
}
