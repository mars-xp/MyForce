package com.xiang.batterytest;

import android.app.Application;
import android.content.Intent;

import com.xiang.batterytest.battery.MFDisposeServices;
import com.xiang.batterytest.remotebg.SleepAccessibilityService;

/**
 * Created by jinxiangpeng on 2017/2/21.
 */

public class MyApp extends Application {
    private static MyApp _instance;
    public static MyApp getApp(){
        return _instance;
    }
    private MFDisposeServices mfService;

    public void setMfService(MFDisposeServices aService){
        mfService = aService;
    }

    public MFDisposeServices getMfService(){
        return mfService;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        _instance = this;
        startService(new Intent(this, MFDisposeServices.class));
        startService(new Intent(this, SleepAccessibilityService.class));
    }
}
