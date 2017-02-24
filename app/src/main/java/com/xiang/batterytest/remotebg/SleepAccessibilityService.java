package com.xiang.batterytest.remotebg;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.os.Build;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.tools.ipc.LocalServiceManager;
import com.xiang.batterytest.MyApp;
import com.xiang.batterytest.util.AccessUtil;

public class SleepAccessibilityService extends AccessibilityService {

    public static boolean mIsServiceRunning = false;

    @Override
    public void onInterrupt() {

    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if(PhoneType.getInstance().getWrokingFlag()){
            if(event != null && event.getSource() != null){
                if (PhoneType.getInstance().m_messagetype == PhoneType.MESSAGE_STATE_CONTEXT) {
                    if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                            && event.getEventType() != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED) {
                        return;
                    }
                } else {
                    if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                        return;
                    }
                }
                try {
                    //PhoneType.getInstance().waitMilliseconds(PhoneType.getInstance().m_actionwaitmillisecond);
                    ActionStep curActionStep = PhoneType.getInstance().getCurrentStep();
                    if (curActionStep == null) {
                        return;
                    }
                    if (curActionStep.m_asActionName.equalsIgnoreCase("START")) {
                        curActionStep = PhoneType.getInstance().getNextStep();
                    }
                    if (curActionStep == null) {
                        return;
                    }
                    if (PhoneType.getInstance().m_messagetype == PhoneType.MESSAGE_STATE_CONTEXT) {
                        if (doForHTC(event, curActionStep) == false) {
                            PhoneType.getInstance().setFindingFlag(false);
                            return;
                        }
                    }
                    PhoneType.getInstance().setFindingFlag(true);
                    if (doAction(event, curActionStep, PhoneType.getCheckFlag()) == true) {
                        curActionStep = PhoneType.getInstance().getNextStep();
                        if (curActionStep != null) {
                            if (curActionStep.m_asActionName.equalsIgnoreCase("BACK")) {
                                PhoneType.getInstance().getNextStep();
                            }
                        } else {
                            // PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                            // "getNextStep NULL");
                        }
                        if(PhoneType.getInstance().checkStepOver()){
                            PhoneType.getInstance().setFindingFlag(false);
                        }
                    } else {
                        PhoneType.getInstance().setWrokingFlag(false);
                        PhoneType.getInstance().setFindingFlag(false);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    PhoneType.getInstance().setWrokingFlag(false);
                    PhoneType.getInstance().setFindingFlag(false);
                }
            }
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
        info.packageNames = PhoneType.getInstance().m_packageNames;
        setServiceInfo(info);
    }

    private AccessibilityNodeInfo forNode(AccessibilityNodeInfo node,
                                          String strElementText) {
        if (node == null || strElementText == null)
            return null;
        CharSequence cs = node.getText();
        if (cs != null) {
            int iStart = 0;
            PhoneType.getInstance().m_allElementName += cs;
            CharSequence clsName = node.getClassName();
            if (clsName != null) {
                PhoneType.getInstance().m_allElementName += "(";
                PhoneType.getInstance().m_allElementName += clsName;
                PhoneType.getInstance().m_allElementName += "),";
            } else {
                PhoneType.getInstance().m_allElementName += ",";
            }
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

        PhoneType.getInstance().m_allElementName = new String();
        return forNode(mNodeInfo, strElementText);
    }

    public boolean doAction(AccessibilityEvent accEvent, ActionStep actionStep,
                            boolean bCheck) {
        boolean bRet = false;
        try {
//            if (actionStep.m_asActionName.equalsIgnoreCase("GET")) {
//                if (actionStep.m_asElementType
//                        .equalsIgnoreCase("android.widget.CheckBox")) {
//                    AccessibilityNodeInfo nodeInfo = findElement(accEvent,
//                            actionStep.m_asElementType,
//                            actionStep.m_asElementText);
//                    if (nodeInfo != null && nodeInfo.isClickable()
//                            && nodeInfo.isEnabled() && nodeInfo.isCheckable()
//                            && nodeInfo.isFocusable()) {
//                    }
//                }
//            } else
            if (actionStep.m_asActionName.equalsIgnoreCase("CLICK")) {
                AccessibilityNodeInfo nodeInfo = findElement(accEvent,
                        actionStep.m_asElementType, actionStep.m_asElementText);
                if (android.os.Build.MANUFACTURER.equalsIgnoreCase("HTC")
                        && actionStep.m_asElementType
                        .equalsIgnoreCase("android.widget.CheckBox")) {
                    nodeInfo = forNodeHtc(accEvent.getSource(),
                            actionStep.m_asElementType, 0);
                }

                if (BuildProperties.isMIUI()
                        && (actionStep.m_asElementType
                        .equalsIgnoreCase("android.widget.CheckBox") || actionStep.m_asElementType
                        .equalsIgnoreCase("android.widget.TextView"))) {
                    return doForXiaomi(accEvent, nodeInfo, actionStep, bCheck);
                }

                if (android.os.Build.MANUFACTURER.equalsIgnoreCase("ZTE")
                        && actionStep.m_asElementType
                        .equalsIgnoreCase("com.nubia.nubiaswitch.NubiaSwitch")) {
                    nodeInfo = forNodeHtc(accEvent.getSource(),
                            actionStep.m_asElementType, 0);
                }

                if (nodeInfo != null) {
                    if (actionStep.m_asElementType
                            .equalsIgnoreCase("android.widget.CheckBox")) {
                        if (nodeInfo.isClickable() && nodeInfo.isEnabled()) {
                            boolean bCurChecked = nodeInfo.isChecked();
                            if (bCheck == true) {
                                if (bCurChecked == true) {
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                } else {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            } else {
                                if (bCurChecked == true) {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                } else {
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            }
                        } else {
                            PhoneType.getInstance().getNextStep();
                            PhoneType.getInstance().getNextStep();
                        }
                        nodeInfo.recycle();
                        bRet = true;
                    } else if (actionStep.m_asElementType
                            .equalsIgnoreCase("com.nubia.nubiaswitch.NubiaSwitch")) {
                        if (nodeInfo.isCheckable() && nodeInfo.isEnabled()) {
                            boolean bCurChecked = nodeInfo.isChecked();
                            if (bCheck == true) {
                                if (bCurChecked == true) {
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                } else {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            } else {
                                if (bCurChecked == true) {
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                } else {
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            }
                        } else {
                            PhoneType.getInstance().getNextStep();
                            PhoneType.getInstance().getNextStep();
                        }
                        nodeInfo.recycle();
                        bRet = true;
                    } else if (actionStep.m_asElementType
                            .equalsIgnoreCase("android.widget.Button")) {
                        if (/* nodeInfo.isClickable() && nodeInfo.isEnabled() */true) {
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        } else {
                            PhoneType.getInstance().getNextStep();
                        }
                        nodeInfo.recycle();
                        bRet = true;
                    }
                    else if(actionStep.m_asElementType.equalsIgnoreCase("android.widget.textview") && Build.VERSION.SDK_INT > 22){
                        AccessibilityNodeInfo vTmp = nodeInfo.getParent();
                        if(vTmp != null){
                            nodeInfo = vTmp;
                            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        }
                    }
                    nodeInfo.recycle();
                    bRet = true;
                } else {
                    bRet = false;
                }
            } else if (actionStep.m_asActionName.equalsIgnoreCase("BACK")) {
                bRet = true;
            } else if (actionStep.m_asActionName.equalsIgnoreCase("START")) {
                bRet = true;
            } else {
                bRet = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bRet;
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
                return false;
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

    private boolean doForXiaomi(AccessibilityEvent accEvent,
                                AccessibilityNodeInfo nodeInfo, ActionStep actionStep,
                                boolean bCheck) {
        if (accEvent == null || nodeInfo == null || actionStep == null) {
            return false;
        }

        if (actionStep.m_asElementType
                .equalsIgnoreCase("android.widget.TextView")) {
            nodeInfo = nodeInfo.getParent();
            if (nodeInfo == null) {
                return false;
            }
            nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
            return true;
        } else if (actionStep.m_asElementType
                .equalsIgnoreCase("android.widget.CheckBox")) {
            nodeInfo = forNodeHtc(accEvent.getSource(),
                    actionStep.m_asElementType, 0);

            boolean bCurChecked = nodeInfo.isChecked();
            if (bCheck == true) {
                if (!bCurChecked) {
                    nodeInfo.getParent().performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);
                }
            } else {
                if (bCurChecked == true) {
                    nodeInfo.getParent().performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);
                }
            }
            nodeInfo.recycle();
            return true;
        }
        return false;
    }
}
