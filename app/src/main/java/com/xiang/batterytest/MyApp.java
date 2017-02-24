package com.xiang.batterytest;

import android.app.Application;
import android.content.Intent;
import android.os.Process;
import android.text.TextUtils;

import com.xiang.batterytest.battery.MFDisposeServices;
import com.xiang.batterytest.remotebg.PhoneType;
import com.xiang.batterytest.remotebg.ProcessService;
import com.xiang.batterytest.remotebg.SleepAccessibilityService;
import com.xiang.batterytest.util.SystemUtil;

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
                PhoneType.getInstance().init(PhoneType.PARSETYPE_FORCE_STOP);
            }
        }
    }
}
