package com.tools.accessibility.remotebg;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.view.accessibility.AccessibilityEvent;

import com.tools.accessibility.remotebg.PhoneType;

public class SleepAccessibilityService extends AccessibilityService {

    public static boolean mIsServiceRunning = false;

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(event != null){
//            if(event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//                    && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED){
//                return;
//            }
            PhoneType.getInstance().findAndClick(event);
        }
    }

    @Override
    protected void onServiceConnected() {

        mIsServiceRunning = true;
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
//        Log.v("testDevice", "manu "+Build.MANUFACTURER+" broad "+Build.BOARD+" BRAND "+Build.BRAND);
//        Log.v("testDevice", "MODE "+Build.MODEL);
        info.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED|AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED;
//        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
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
