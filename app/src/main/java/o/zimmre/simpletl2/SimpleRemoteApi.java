/*
 * Copyright 2014 Sony Corporation
 */

package o.zimmre.simpletl2;

import o.zimmre.simpletl2.utils.SimpleHttpClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Simple Camera Remote API wrapper class. (JSON based API <--> Java API)
 */
public class SimpleRemoteApi {

    private static final String TAG = SimpleRemoteApi.class.getSimpleName();

    private ServerDevice mTargetServer;

    private AtomicLong mRequestId = new AtomicLong();

    /**
     * Constructor.
     * 
     * @param target server device of Remote API
     */
    public SimpleRemoteApi(ServerDevice target) {
        mTargetServer = target;
    }

    /**
     * Retrieves Action List URL from Server information.
     * 
     * @param service
     * @return
     * @throws IOException
     */
    private String findActionListUrl(String service) throws IOException {
        List<ServerDevice.ApiService> services = mTargetServer.getApiServices();
        for (ServerDevice.ApiService apiService : services) {
            if (apiService.getName().equals(service)) {
                return apiService.getActionListUrl();
            }
        }
        throw new IOException("actionUrl not found. service : " + service);
    }

    /**
     * Request ID. Counted up after calling.
     * 
     * @return
     */
    private long id() {
        return mRequestId.incrementAndGet();
    }

    private JSONObject execute(String getAvailableShootMode) throws IOException {
        String service = "camera";
        try {
            JSONObject requestJson =
                    new JSONObject().put("method", getAvailableShootMode)
                            .put("params", new JSONArray())
                            .put("id", id())
                            .put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;

            Log.d(TAG,"Request:  " + requestJson.toString());
            String responseJson = SimpleHttpClient.httpPost(url, requestJson.toString());
            Log.d(TAG,"Response: " + responseJson);
            return new JSONObject(responseJson);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    /**
     * Calls startRecMode API to the target server. Request JSON data is such
     * like as below.
     * 
     * <pre>
     * {
     *   "method": "startRecMode",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     * 
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public JSONObject startRecMode() throws IOException {
        return execute("startRecMode");
    }

    /**
     * Calls actTakePicture API to the target server. Request JSON data is such
     * like as below.
     * 
     * <pre>
     * {
     *   "method": "actTakePicture",
     *   "params": [],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     * 
     * @return JSON data of response
     * @throws IOException
     */
    public JSONObject actTakePicture() throws IOException {
        return execute("actTakePicture");
    }

    /**
     * Calls getEvent API to the target server. Request JSON data is such like
     * as below.
     * 
     * <pre>
     * {
     *   "method": "getEvent",
     *   "params": [true],
     *   "id": 2,
     *   "version": "1.0"
     * }
     * </pre>
     * 
     * @param longPollingFlag true means long polling request.
     * @return JSON data of response
     * @throws IOException all errors and exception are wrapped by this
     *             Exception.
     */
    public JSONObject getEvent(boolean longPollingFlag) throws IOException {
        String service = "camera";
        try {
            JSONObject requestJson =
                    new JSONObject().put("method", "getEvent") //
                            .put("params", new JSONArray().put(longPollingFlag)) //
                            .put("id", id()).put("version", "1.0");
            String url = findActionListUrl(service) + "/" + service;
            int longPollingTimeout = (longPollingFlag) ? 20000 : 8000; // msec

            Log.d(TAG,"Request:  " + requestJson.toString());
            String responseJson = SimpleHttpClient.httpPost(url, requestJson.toString(),
                    longPollingTimeout);
            Log.d(TAG,"Response: " + responseJson);
            return new JSONObject(responseJson);
        } catch (JSONException e) {
            throw new IOException(e);
        }
    }

    public JSONObject startBulbShooting() throws IOException {
        return execute("startBulbShooting");
    }

    public JSONObject stopBulbShooting() throws IOException {
        return execute("stopBulbShooting");
    }
}
