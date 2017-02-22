package com.tools.ipc;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.xiang.batterytest.MyApp;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by joye on 29/11/2016.
 */

public class LocalServiceManager {

    private Context context;
    private Map<String, IBinder> cachedServices = new HashMap<>();

    private static LocalServiceManager _instance;

    public static LocalServiceManager getInstance() {
        synchronized (LocalServiceManager.class) {
            if (_instance == null) {
                _instance = new LocalServiceManager(MyApp.getApp());
            }
        }
        return _instance;
    }

    private LocalServiceManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void addService(String serviceName, IBinder service) {
        try {
            Uri uri = ServiceProvider.getContentUri(context);
            Bundle extras = new Bundle();
            extras.putParcelable(ServiceProvider.EXTRA_BINDER, service != null ? new IBinderWrapper(service) : null);
            context.getContentResolver().call(uri, ServiceProvider.CMD_ADD, serviceName, extras);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public IBinder getService(String serviceName) {
        try {
            synchronized (this) {
                IBinder service = cachedServices.get(serviceName);
                if (service != null && service.pingBinder()) {
                    return service;
                }
                cachedServices.remove(serviceName);
            }

            IBinder cached = getUnStableService(serviceName);
            if (cached != null)
                return cached;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public IBinder getUnStableService(String serviceName) {
        Uri uri = ServiceProvider.getContentUri(context);
        Bundle result = context.getContentResolver().call(uri, ServiceProvider.CMD_GET, serviceName, null);
        if (result != null) {
            result.setClassLoader(context.getClassLoader());
            IBinderWrapper wrapper = result.getParcelable(ServiceProvider.EXTRA_BINDER);
            if (wrapper != null) {
                synchronized (this) {
                    IBinder cached = wrapper.getService();
                    cachedServices.put(serviceName, cached);
                    return cached;
                }
            }
        }
        return null;
    }

}
