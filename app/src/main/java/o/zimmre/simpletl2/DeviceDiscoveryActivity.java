/*
 * Copyright 2014 Sony Corporation
 */

package o.zimmre.simpletl2;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * An Activity class of Device Discovery screen.
 */
public class DeviceDiscoveryActivity extends Activity {

    private static final String TAG = DeviceDiscoveryActivity.class.getSimpleName();

    private DeviceListAdapter mListAdapter;

    private boolean mActivityActive;


    public static final String MY_CAMERA_LOCATION = "http://192.168.122.1:64321/scalarwebapi_dd.xml";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_device_discovery);
        setProgressBarIndeterminateVisibility(false);

        mListAdapter = new DeviceListAdapter(this);

        Log.d(TAG, "onCreate() completed.");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityActive = true;
        ListView listView = (ListView) findViewById(R.id.list_device);
        listView.setAdapter(mListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListView listView = (ListView) parent;
                ServerDevice device = (ServerDevice) listView.getAdapter().getItem(position);
                launchSampleActivity(device);
            }
        });

        findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Button btn = (Button) v;
                searchDevices();
                btn.setEnabled(false);
            }
        });

        Log.d(TAG, "onResume() completed.");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityActive = false;

        Log.d(TAG, "onPause() completed.");
    }

    /**
     * Start searching supported devices.
     */
    private void searchDevices() {
        mListAdapter.clearDevices();
        setProgressBarIndeterminateVisibility(true);

        new Thread(new Runnable() {
            @Override
            public void run() {
                final ServerDevice device = fetchMyCamera();
                runOnUiThread(
                        new Runnable() {
                            @Override
                            public void run() {
                                if (device != null) {
                                    mListAdapter.addDevice(device);
                                }
                                if (mActivityActive) {
                                    Toast.makeText(DeviceDiscoveryActivity.this,
                                            device != null ? R.string.msg_device_search_finish : R.string.msg_error_device_searching,
                                            Toast.LENGTH_SHORT).show(); //
                                }

                                setProgressBarIndeterminateVisibility(false);
                                findViewById(R.id.button_search).setEnabled(true);
                            }
                        }
                );

            }
        }).start();
    }

    private ServerDevice fetchMyCamera() {
        try {
            return ServerDevice.fetch(MY_CAMERA_LOCATION);
        } catch (Exception e) {
            Log.e(TAG, "fetch failed", e);
        }
        return null;
    }


    /**
     * Launch a SampleCameraActivity.
     *
     * @param device
     */
    private void launchSampleActivity(ServerDevice device) {
        // Go to CameraSampleActivity.
        Toast.makeText(DeviceDiscoveryActivity.this, device.getFriendlyName(), Toast.LENGTH_SHORT) //
                .show();

        // Set target ServerDevice instance to control in Activity.
        SampleApplication app = (SampleApplication) getApplication();
        app.setTargetServerDevice(device);
        Intent intent = new Intent(this, SampleCameraActivity.class);
        startActivity(intent);
    }

    /**
     * Adapter class for DeviceList
     */
    private static class DeviceListAdapter extends BaseAdapter {

        private final List<ServerDevice> mDeviceList;

        private final LayoutInflater mInflater;

        public DeviceListAdapter(Context context) {
            mDeviceList = new ArrayList<>();
            mInflater = LayoutInflater.from(context);
        }

        public void addDevice(ServerDevice device) {
            mDeviceList.add(device);
            notifyDataSetChanged();
        }

        public void clearDevices() {
            mDeviceList.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0; // not fine
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            TextView textView = (TextView) convertView;
            if (textView == null) {
                textView = (TextView) mInflater.inflate(R.layout.device_list_item, parent, false);
            }
            ServerDevice device = (ServerDevice) getItem(position);
            ServerDevice.ApiService apiService = device.getApiService("camera");
            String endpointUrl = null;
            if (apiService != null) {
                endpointUrl = apiService.getEndpointUrl();
            }

            // Label
            String htmlLabel =
                    String.format("%s ", device.getFriendlyName()) //
                            + String.format(//
                            "<br><small>Endpoint URL:  <font color=\"blue\">%s</font></small>", //
                            endpointUrl);
            textView.setText(Html.fromHtml(htmlLabel));

            return textView;
        }
    }
}
