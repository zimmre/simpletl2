/*
 * Copyright 2014 Sony Corporation
 */

package o.zimmre.simpletl2;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import o.zimmre.simpletl2.utils.DisplayHelper;

/**
 * An Activity class of Sample Camera screen.
 */
public class SampleCameraActivity extends Activity {

    private static final String TAG = SampleCameraActivity.class.getSimpleName();

    private Button mButtonTakePicture;

    private TextView mTextCameraStatus;

    private ServerDevice mTargetServer;

    private SimpleRemoteApi mRemoteApi;

    private CameraControl cameraControl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sample_camera);

        SampleApplication app = (SampleApplication) getApplication();
        mTargetServer = app.getTargetServerDevice();
        mRemoteApi = new SimpleRemoteApi(mTargetServer);
        app.setRemoteApi(mRemoteApi);
        mButtonTakePicture = findViewById(R.id.button_take_picture);
        mTextCameraStatus = findViewById(R.id.text_camera_status);

        cameraControl = new CameraControl(mRemoteApi, getApplicationContext());


    }

    @Override
    protected void onResume() {
        super.onResume();
        mButtonTakePicture.setOnClickListener(v -> takeAndFetchPicture());
        findViewById(R.id.check_status).setOnClickListener(x -> checkStatus());
        cameraControl.start();

    }

    private void checkStatus() {
        cameraControl.checkStatus(
                status -> runOnUiThread(
                        () -> mTextCameraStatus.setText(status)
                )
        );
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void prepareOpenConnection() {
        Log.d(TAG, "prepareToOpenConection() exec");

        new Thread() {

            @Override
            public void run() {
                try {
                    // confirm current camera status
                    String cameraStatus;
                    JSONObject replyJson = mRemoteApi.getEvent(false);
                    JSONArray resultsObj = replyJson.getJSONArray("result");
                    JSONObject cameraStatusObj = resultsObj.getJSONObject(1);
                    String type = cameraStatusObj.getString("type");
                    if ("cameraStatus".equals(type)) {
                        cameraStatus = cameraStatusObj.getString("cameraStatus");
                    } else {
                        throw new IOException();
                    }

                    if (isShootingStatus(cameraStatus)) {
                        Log.d(TAG, "camera function is Remote Shooting.");
                        openConnection();
                    }
                } catch (IOException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: IOException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(SampleCameraActivity.this, false);
                } catch (JSONException e) {
                    Log.w(TAG, "prepareToStartContentsListMode: JSONException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_api_calling);
                    DisplayHelper.setProgressIndicator(SampleCameraActivity.this, false);
                }
            }
        }.start();
    }

    private static boolean isShootingStatus(String currentStatus) {
        Set<String> shootingStatus = new HashSet<>();
        shootingStatus.add("IDLE");
        shootingStatus.add("NotReady");
        shootingStatus.add("StillCapturing");
        shootingStatus.add("StillSaving");
        shootingStatus.add("MovieWaitRecStart");
        shootingStatus.add("MovieRecording");
        shootingStatus.add("MovieWaitRecStop");
        shootingStatus.add("MovieSaving");
        shootingStatus.add("IntervalWaitRecStart");
        shootingStatus.add("IntervalRecording");
        shootingStatus.add("IntervalWaitRecStop");
        shootingStatus.add("AudioWaitRecStart");
        shootingStatus.add("AudioRecording");
        shootingStatus.add("AudioWaitRecStop");
        shootingStatus.add("AudioSaving");

        return shootingStatus.contains(currentStatus);
    }

    /**
     * Open connection to the camera device to start monitoring Camera events
     * and showing liveview.
     */
    private void openConnection() {

        new Thread() {

            @Override
            public void run() {

                try {

                    mRemoteApi.startRecMode();


                } catch (IOException e) {
                    Log.w(TAG, "openConnection : IOException: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), R.string.msg_error_connection);
                }
            }
        }.start();

    }

    /**
     * Take a picture and retrieve the image data.
     */
    private void takeAndFetchPicture() {

        new Thread() {

            @Override
            public void run() {
                try {


                    JSONObject replyJson = mRemoteApi.actTakePicture();
//
//                    alarmManager.setExact(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                            SystemClock.elapsedRealtime() + 22000,
//                            "bulbmode",
//                            new AlarmManager.OnAlarmListener() {
//                                @Override
//                                public void onAlarm() {
//                                    try {
//                                        mRemoteApi.stopBulbShooting();
//                                    } catch (IOException e) {
//                                        e.printStackTrace();
//                                    }
//
//                                }
//                            },
//                            null);


                } catch (Exception e) {
                    Log.w(TAG, "Exception while taking picture: " + e.getMessage());
                    DisplayHelper.toast(getApplicationContext(), //
                            R.string.msg_error_take_picture);
                } finally {
                    DisplayHelper.setProgressIndicator(SampleCameraActivity.this, false);
                }
            }
        }.start();
    }

}
