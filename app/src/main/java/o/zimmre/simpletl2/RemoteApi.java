package o.zimmre.simpletl2;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

public class RemoteApi {


    private final SimpleRemoteApi simpleRemoteApi;


    public RemoteApi(SimpleRemoteApi simpleRemoteApi) {
        this.simpleRemoteApi = simpleRemoteApi;
    }

    public String getStatus() {
        try {
            String cameraStatus;
            JSONObject replyJson = simpleRemoteApi.getEvent(false);
            JSONObject cameraStatusObj = findInRespone(replyJson, "cameraStatus");
            return cameraStatusObj.getString("cameraStatus");

        } catch (IOException | JSONException e) {
            e.printStackTrace();
        }
        return null;

    }

    public void startRecMode() {
        try {
            final JSONObject response = simpleRemoteApi.startRecMode();
            final JSONArray error = response.getJSONArray("error");
            if (error == null) {
                return;
            }
            throw new RuntimeException(error.toString());
        } catch (IOException | JSONException e) {
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
}
