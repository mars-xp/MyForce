package com.xiang.batterytest.remotebg;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.tools.ipc.LocalServiceManager;
import com.xiang.batterytest.MyApp;
import com.xiang.batterytest.util.AccessUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class SleepAccessibilityService extends AccessibilityService {

    public static boolean mIsServiceRunning = false;

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event != null){
            Log.v("nodeinfo", "get type "+event.getEventType());
            PhoneType.getInstance().addEventSync(event);
        }
    }

    @Override
    protected void onServiceConnected() {

        mIsServiceRunning = true;
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
//        info.notificationTimeout = 400;
//        if(Build.MODEL.contains("HTC")){
//            info.notificationTimeout = 350;
//        }
//        else{
//            info.notificationTimeout = 30;
//        }
        info.packageNames = PhoneType.getInstance().m_packageNames;
        setServiceInfo(info);
    }

    public boolean onUnbind(Intent intent) {
        mIsServiceRunning = false;
        return super.onUnbind(intent);
    }
}
