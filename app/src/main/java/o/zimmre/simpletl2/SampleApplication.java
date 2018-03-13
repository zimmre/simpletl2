/*
 * Copyright 2014 Sony Corporation
 */

package o.zimmre.simpletl2;

import android.app.Application;

import java.util.Set;

/**
 * Application class for the sample application.
 */
public class SampleApplication extends Application {

    private ServerDevice mTargetDevice;
    private RemoteApi remoteApi;

    public void setTargetServerDevice(ServerDevice device) {
        mTargetDevice = device;
    }

    public ServerDevice getTargetServerDevice() {
        return mTargetDevice;
    }

    public void setRemoteApi(RemoteApi remoteApi) {
        this.remoteApi = remoteApi;
    }

    public RemoteApi getRemoteApi() {
        return remoteApi;
    }
}
