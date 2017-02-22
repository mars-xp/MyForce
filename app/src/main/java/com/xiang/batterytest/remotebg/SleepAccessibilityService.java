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
    public static final int TYPE_SERVICE_RUNNING = 1;
    public static final int TYPE_SERVICE_UNBIND = 2;

    private static boolean m_bServiceRunning = false;

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        if (event.getSource() == null) {
            return;
        } else {

        }

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

        if (PhoneType.getWrokingFlag() == false) {
            return;
        }

        try {

            PhoneType
                    .waitMilliseconds(PhoneType.getInstance().m_actionwaitmillisecond);
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
                    return;
                }
            }

            PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                    curActionStep.m_asActionName);
            PhoneType.setFindingFlag(true);
            if (doAction(event, curActionStep, PhoneType.getCheckFlag()) == true) {
                curActionStep = PhoneType.getInstance().getNextStep();
                if (curActionStep != null) {
                    if (curActionStep.m_asActionName.equalsIgnoreCase("BACK")) {
                        PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                "doAction true BACK");
                        PhoneType.getInstance().getNextStep();
                    }
                } else {
                    // PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                    // "getNextStep NULL");
                }
            } else {
                PhoneType.setWrokingFlag(false);
                PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "DoAction FALSE");
            }
            PhoneType.setFindingFlag(false);
        } catch (Exception e) {
            // PhoneType.m_errMsg = new String();
            PhoneType.getInstance().m_errMsg = "[SleepAccessibilityService.onAccessibilityEvent] Exception: "
                    + e.toString();
            PhoneType.setWrokingFlag(false);
            PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "DoAction FALSE");
        }

    }

    @Override
    public void onInterrupt() {
        PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "onInterrupt");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        LocalServiceManager.getInstance().addService("accessibility_service", new AccessibilityBinder().asBinder());
        return super.onStartCommand(intent, flags, startId);
    }

    public void onServiceConnected() {
        PhoneType.writeServiceFlag(TYPE_SERVICE_RUNNING);
        setServiceRunningFlag(true);
        PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "onServiceConnected");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;// AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        // info.eventTypes =
        // AccessibilityEvent.TYPE_VIEW_CLICKED|AccessibilityEvent.TYPE_VIEW_FOCUSED|AccessibilityEvent.TYPE_VIEW_SCROLLED|AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;
        info.flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
                | AccessibilityServiceInfo.FLAG_REQUEST_FILTER_KEY_EVENTS;
        info.packageNames = PhoneType.getInstance().m_packageNames;
        setServiceInfo(info);

        AccessUtil.setStatusForWidget(this, true);

        // 发送一个静态广播 。通知辅助服务已经连接上
        Intent intent = new Intent("com.anguanjia.security.AccessibilityService");
        intent.putExtra("isServicesConnected", true);
        sendOrderedBroadcast(intent, null);
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
                // PhoneType.m_errMsg = new String();
                PhoneType.getInstance().m_errMsg = "[SleepAccessibilityService.forNode] Exception: "
                        + e.toString();
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
        if (accEvent == null || actionStep == null) {
            return bRet;
        }
        if (accEvent.getSource() == null) {
            return bRet;
        }
        try {
            if (actionStep.m_asActionName.equalsIgnoreCase("GET")) {
                if (actionStep.m_asElementType
                        .equalsIgnoreCase("android.widget.CheckBox")) {
                    AccessibilityNodeInfo nodeInfo = findElement(accEvent,
                            actionStep.m_asElementType,
                            actionStep.m_asElementText);
                    if (nodeInfo != null && nodeInfo.isClickable()
                            && nodeInfo.isEnabled() && nodeInfo.isCheckable()
                            && nodeInfo.isFocusable()) {
                        if (nodeInfo.isChecked() == true) {
                            PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                    "Is Checked  " + PhoneType.getCurPkgName());
                        } else {
                            PhoneType.logInfo(
                                    PhoneType.TYPE_LOGINFO,
                                    "Is not Checked  "
                                            + PhoneType.getCurPkgName());
                        }
                    }
                }
            } else if (actionStep.m_asActionName.equalsIgnoreCase("CLICK")) {
                AccessibilityNodeInfo nodeInfo = findElement(accEvent,
                        actionStep.m_asElementType, actionStep.m_asElementText);
                if (android.os.Build.MANUFACTURER.equalsIgnoreCase("HTC")
                        && actionStep.m_asElementType
                        .equalsIgnoreCase("android.widget.CheckBox")) {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "IS HTC");
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
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "ischecked");
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                } else {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "is not checked");
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            } else {
                                if (bCurChecked == true) {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "ischecked");
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                } else {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "is not checked");
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            }
                        } else {
                            PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                    "Is Not click able");
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
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "ischecked");
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                } else {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "is not checked");
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            } else {
                                if (bCurChecked == true) {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "ischecked");
                                    nodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                } else {
                                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                            "is not checked");
                                    PhoneType.getInstance().getNextStep();
                                    PhoneType.getInstance().getNextStep();
                                }
                            }
                        } else {
                            PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                    "Is Not click able");
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
                            PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
                                    "Is Not click able");
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
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "Not find node");
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
            // PhoneType.m_errMsg = new String();
            PhoneType.getInstance().m_errMsg = "[SleepAccessibilityService.doAction] Exception: "
                    + e.toString();
        }
        return bRet;
    }

    public boolean onUnbind(Intent intent) {
        PhoneType.writeServiceFlag(TYPE_SERVICE_UNBIND);
        PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "onUnbind");
        setServiceRunningFlag(false);

        AccessUtil.setStatusForWidget(this, false);

        // 发送一个静态广播 。通知辅助服务已经连接上
        Intent intentUnBind = new Intent(
                "com.anguanjia.security.AccessibilityService");
        intentUnBind.putExtra("isServicesConnected", false);
        stopSelf();
        sendOrderedBroadcast(intentUnBind, null);
        return super.onUnbind(intent);
    }

    public synchronized static boolean getServiceRunningFlag() {
        PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "getServiceRunningFlag"
                + String.valueOf(m_bServiceRunning));
        if (m_bServiceRunning == false) {
            try {
                if ((MyApp.getApp().getApplicationContext().getApplicationInfo().flags & ApplicationInfo.FLAG_STOPPED) == ApplicationInfo.FLAG_STOPPED) {
                    PhoneType.writeServiceFlag(TYPE_SERVICE_UNBIND);
                    return false;
                }
            } catch (Exception e) {
                return false;
            }
            if (PhoneType.getServiceFlag() == TYPE_SERVICE_RUNNING) {
                m_bServiceRunning = true;
            }
        }
        return m_bServiceRunning;
    }

    public synchronized static void setServiceRunningFlag(boolean flag) {
        PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "setServiceRunningFlag"
                + String.valueOf(flag));
        m_bServiceRunning = flag;
    }

    private boolean doForHTC(AccessibilityEvent event, ActionStep curActionStep) {
        if (event == null || curActionStep == null) {
            return false;
        }
        if (event.getSource() == null) {
            return false;
        }
        if (curActionStep.m_asElementType
                .equalsIgnoreCase("android.widget.CheckBox")) {
            if (event.getSource().getChildCount() > 0) {
                PhoneType.setFindingFlag(true);
                if (forNodeHtc(event.getSource(),
                        curActionStep.m_asElementType, 0) == null) {
                    PhoneType.setFindingFlag(false);
                    return false;
                } else {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "HTC FIND"
                            + curActionStep.m_asElementType);
                }
            } else {
                return false;
            }
        } else {
            PhoneType.setFindingFlag(true);
            if (forNode(event.getSource(), curActionStep.m_asElementText) == null) {
                PhoneType.setFindingFlag(false);
                return false;
            } else {
                PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "HTC FIND"
                        + curActionStep.m_asElementType);
            }
        }
        return true;
    }

    private AccessibilityNodeInfo forNodeHtc(AccessibilityNodeInfo node,
                                             String strElementType, int iLevel) {
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
                if (bCurChecked == true) {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "ischecked");
                } else {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "is not checked");
                    nodeInfo.getParent().performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);

                }
            } else {
                if (bCurChecked == true) {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "ischecked");
                    nodeInfo.getParent().performAction(
                            AccessibilityNodeInfo.ACTION_CLICK);
                } else {
                    PhoneType.logInfo(PhoneType.TYPE_LOGINFO, "is not checked");
                }
            }
            nodeInfo.recycle();
            return true;
        }
        return false;
    }
}
