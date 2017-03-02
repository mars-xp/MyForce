package com.xiang.batterytest;

import android.app.Application;
import android.content.Intent;
import android.text.TextUtils;

import com.xiang.batterytest.battery.MFDisposeServices;
import com.tools.accessibility.remotebg.PhoneType;
import com.tools.accessibility.remotebg.ProcessService;
import com.tools.accessibility.uitils.SystemUtil;

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
        String vProcessName = SystemUtil.getProcessName();
        if(!TextUtils.isEmpty(vProcessName)){
            if(vProcessName.equals(BuildConfig.APPLICATION_ID)){
                startService(new Intent(this, MFDisposeServices.class));
                startService(new Intent(this, ProcessService.class));
            }
            else if(vProcessName.equals(BuildConfig.APPLICATION_ID+":service")){
                PhoneType.getInstance().init();
            }
        }
    }
}
