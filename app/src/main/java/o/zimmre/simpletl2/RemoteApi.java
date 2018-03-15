package o.zimmre.simpletl2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.Callable;

public class RemoteApi {


    private final SimpleRemoteApi simpleRemoteApi;


    public RemoteApi(SimpleRemoteApi simpleRemoteApi) {
        this.simpleRemoteApi = simpleRemoteApi;
    }

    public String getStatus() {
        try {
            JSONObject replyJson = simpleRemoteApi.getEvent(false);
            JSONObject cameraStatusObj = findInRespone(replyJson, "cameraStatus");
            return cameraStatusObj.getString("cameraStatus");

        } catch (IOException | JSONException e) {
        }
        return "Unknown";

    }

    public void startRecMode() {
        withTryCatch(() -> {
            final JSONObject response = simpleRemoteApi.startRecMode();
            return checkError(response);
        });
    }

    private Class<Void> checkError(JSONObject response) {
        try {
            if (response.has("error")) {
                final JSONArray error = response.getJSONArray("error");
                throw new RuntimeException(error.toString());
            }
            return Void.TYPE;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private JSONObject findInRespone(JSONObject response, String type) throws JSONException {
        JSONArray result = response.getJSONArray("result");
        for (int i = 0; i < result.length(); i++) {
            JSONObject e = result.getJSONObject(i);
            if (type.equals(e.getString("type"))) {
                return e;
            }
        }
        throw new RuntimeException(type + " not found in response " + response.toString());
    }

    public void actTakePicture() {
        withTryCatch(() -> {
            final JSONObject response = simpleRemoteApi.actTakePicture();
            return checkError(response);
        });
    }

    private static <V> V withTryCatch(Callable<V> r) {
        try {
            return r.call();
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public void startBulbShooting() {
        withTryCatch(() -> checkError(simpleRemoteApi.startBulbShooting()));
    }

    public void stopBulbShooting() {
        withTryCatch(() -> checkError(simpleRemoteApi.stopBulbShooting()));
    }
}
