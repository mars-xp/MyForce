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
        if(event != null && PhoneType.getInstance().getWrokingFlag()){
            AccessibilityEvent vEvent = AccessibilityEvent.obtain(event);
            if(vEvent != null){
                PhoneType.getInstance().addEvent(vEvent);
            }
        }
    }

//        @Override
//    public void onAccessibilityEvent(AccessibilityEvent event) {
//        Log.v("xiangpeng", "enter onEvent "+event.getEventType());
//        if(PhoneType.getInstance().getWrokingFlag()){
//            AccessibilityEvent vEvent = AccessibilityEvent.obtain(event);
//
//            if(vEvent != null && vEvent.getSource() != null){
//                if(PhoneType.getInstance().m_messagetype != PhoneType.MESSAGE_STATE_CONTEXT){
//                    if(vEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED){
//                        return;
//                    }
//                }
//                else{
//                    if (vEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
//                            && vEvent.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
//                        return;
//                    }
//                }
//                try {
//                    //PhoneType.getInstance().waitMilliseconds(PhoneType.getInstance().m_actionwaitmillisecond);
//                    ActionStep curActionStep = PhoneType.getInstance().getCurrentStep();
//                    if (curActionStep == null) {
//                        return;
//                    }
//                    if (curActionStep.m_asActionName.equalsIgnoreCase("START")) {
//                        curActionStep = PhoneType.getInstance().getNextStep();
//                    }
//                    if (curActionStep == null) {
//                        return;
//                    }
//                    Log.v("xiangpeng", "message type is "+vEvent.getEventType());
//                    if (PhoneType.getInstance().m_messagetype == PhoneType.MESSAGE_STATE_CONTEXT) {
//                        if (doForHTC(vEvent, curActionStep) == false) {
//                            PhoneType.getInstance().setFindingFlag(false);
//                            return;
//                        }
//                    }
//                    PhoneType.getInstance().setFindingFlag(true);
//                    if (doAction(vEvent, curActionStep)) {
//                        Log.v("xiangpeng", "doaction success");
//                        if(PhoneType.getInstance().checkStepOver()){
//                            Log.v("xiangpeng", "doaction success and notify");
//                            PhoneType.getInstance().setFindingFlag(false);
//                        }
//                    } else {
//                        Log.v("xiangpeng", "doaction failed and notify");
//                        PhoneType.getInstance().setFindingFlag(false);
//                    }
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                    Log.v("xiangpeng", "exception "+e+" and notify");
//                    PhoneType.getInstance().setFindingFlag(false);
//                }
//            }
//        }
//    }

    @Override
    protected void onServiceConnected() {

        mIsServiceRunning = true;
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
//        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
//                | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
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

    private AccessibilityNodeInfo forNode(AccessibilityNodeInfo node,
                                          String strElementText) {
        if (node == null || strElementText == null)
            return null;
        CharSequence cs = node.getText();
        if (cs != null) {
            Log.v("nodeinfo", "node text is "+cs.toString());
//            if(cs.toString().equalsIgnoreCase(strElementText)){
//                Log.w("nodeinfo", "test find node "+strElementText);
//                return node;
//            }
            int iStart = 0;
            try {
                for (int i = 0; i < strElementText.length(); i++) {
                    if (strElementText.charAt(i) == '|') {
                        String strTmp = strElementText.substring(iStart, i);
                        iStart = i + 1;
                        if (strTmp.equalsIgnoreCase(cs.toString()) == true) {
                            return node;
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        int n = node.getChildCount();

        for (int i = 0; i < n; i++) {
            AccessibilityNodeInfo an = node.getChild(i);
            if (an != null) {
                AccessibilityNodeInfo newNodeInfo = forNode(an, strElementText);
                if (newNodeInfo != null) {
                    return newNodeInfo;
                }
                an.recycle();
            }
        }

        return null;
    }

    public AccessibilityNodeInfo findElement(AccessibilityEvent accEvent,
                                             String strElementType, String strElementText) {
        AccessibilityNodeInfo vRet = null;
        if (accEvent == null) {
            return null;
        }
        AccessibilityNodeInfo mNodeInfo = accEvent.getSource();
        if (mNodeInfo == null) {
            return null;
        }
        if (strElementType == null || strElementText == null) {
            return null;
        }
        vRet = forNode(accEvent.getSource(), strElementText);
//        if(vRet == null){
//            Log.v("xiangpeng", "findElement fornode failed first" + strElementText);
//            try{
//                Thread.sleep(1500);
//            }
//            catch (Exception e){
//                e.printStackTrace();
//            }
//            vRet = forNode(accEvent.getSource(), strElementText);
//            if(vRet == null){
//                Log.v("xiangpeng", "findElement fornode failed second" + strElementText);
//            }
//        }
        return vRet;
    }





    public boolean onUnbind(Intent intent) {
        mIsServiceRunning = false;
        return super.onUnbind(intent);
    }

    private boolean doForHTC(AccessibilityEvent event, ActionStep curActionStep) {
        if (curActionStep.m_asElementType.equalsIgnoreCase("android.widget.CheckBox")) {
            if (event.getSource().getChildCount() > 0) {
                PhoneType.getInstance().setFindingFlag(true);
                if (forNodeHtc(event.getSource(), curActionStep.m_asElementType, 0) == null) {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            if (forNode(event.getSource(), curActionStep.m_asElementText) == null) {
                Log.v("xiangpeng", "doforhtc find node failed first " + curActionStep.m_asElementText);
                try{
                    Thread.sleep(1500);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
                if(forNode(event.getSource(), curActionStep.m_asElementText) == null){
                    Log.v("xiangpeng", "doforhtc find node failed second " + curActionStep.m_asElementText);
                    return false;
                }
            }
        }
        return true;
    }

    private AccessibilityNodeInfo forNodeHtc(AccessibilityNodeInfo node, String strElementType, int iLevel) {
        if (node == null || strElementType == null)
            return null;
        if (node.getClassName().toString().equalsIgnoreCase(strElementType)) {
            return node;
        }
        int n = node.getChildCount();
        for (int i = 0; i < n; i++) {
            AccessibilityNodeInfo an = node.getChild(i);
            if (an != null) {
                AccessibilityNodeInfo newNodeInfo = forNodeHtc(an,
                        strElementType, ++iLevel);
                if (newNodeInfo != null) {
                    return newNodeInfo;
                }
                an.recycle();
            }
        }
        return null;
    }

    private boolean doForXiaomi(AccessibilityEvent accEvent, AccessibilityNodeInfo nodeInfo, ActionStep actionStep) {
        boolean vRet = false;
        if(accEvent != null && nodeInfo != null && actionStep != null){
            if(actionStep.m_asElementType.equalsIgnoreCase("android.widget.TextView")){
                nodeInfo = nodeInfo.getParent();
                if(nodeInfo != null){
                    PhoneType.getInstance().getNextStep();
                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                    vRet = true;
                    nodeInfo.recycle();
                }
            }
            else if(actionStep.m_asElementType.equalsIgnoreCase("android.widget.CheckBox")){
                nodeInfo = forNodeHtc(accEvent.getSource(), actionStep.m_asElementType, 0);
                if(nodeInfo != null){
                    boolean vIsChecked = nodeInfo.isChecked();
                    if(!vIsChecked){
                        PhoneType.getInstance().getNextStep();
                        nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        vRet = true;
                        nodeInfo.recycle();
                    }
                }
            }
            else{
                nodeInfo.recycle();
            }
        }
        return vRet;
    }
}
