package o.zimmre.simpletl2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class SampleCameraActivity extends Activity {

    private CameraControl cameraControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_camera);

        SampleApplication app = (SampleApplication) getApplication();
        ServerDevice mTargetServer = app.getTargetServerDevice();

        final RemoteApi remoteApi = new RemoteApi(new SimpleRemoteApi(mTargetServer));
        app.setRemoteApi(remoteApi);
        cameraControl = new CameraControl(getApplicationContext(), remoteApi);
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.button_take_picture).setOnClickListener(v -> cameraControl.shoot());
        findViewById(R.id.check_status).setOnClickListener(v -> checkStatus());
        findViewById(R.id.init_connection).setOnClickListener(v -> cameraControl.start());
        findViewById(R.id.button_bulb_take_picture).setOnClickListener(v -> shootBulb());

        cameraControl.start();
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

}
