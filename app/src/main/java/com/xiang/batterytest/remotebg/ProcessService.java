package com.xiang.batterytest.remotebg;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.tools.ipc.LocalServiceManager;

public class ProcessService extends Service {
    public ProcessService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalServiceManager.getInstance().addService("accessibility_service", new AccessibilityBinder().asBinder());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
