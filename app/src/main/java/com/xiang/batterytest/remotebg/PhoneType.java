package com.xiang.batterytest.remotebg;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.provider.Settings;


import com.xiang.batterytest.MyApp;
import com.xiang.batterytest.util.AccessUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhoneType {

	// used AccessibilityConfig.xml version
	public final String UAB_SDK_VERSION = "2.0";
	// phone base info
	public String m_manufacturer;
	public String m_mode;
	public String m_release;
	public String m_sdk;
	public String m_uabsdkver;
	public int m_pkgwaitsecond = 3;
	public int m_intervalmillisecond = 600;
	public int m_slicemillisecond = 200;
	public int m_actionwaitmillisecond = 500;
	public int m_messagetype = 1;
	public String m_matchtype;

	// step action list
	public List<ActionStep> m_asForceStopList;
	public List<ActionStep> m_asNotifiClickList;
	public List<ActionStep> m_asNotifiGetList;
	public List<ActionStep> m_asClearCatchList;

	private int m_iCurType = 0;
	private int m_iCurStep = 0;
	private boolean m_bThreadFlag = false;
	private static PhoneType m_phoneType = null;

	// use in sleepaccessibility
	public boolean m_bWorkingFlag = false;
	public boolean m_bCheckFlag = false;
	public String m_curPkgName = null;
	public boolean m_bFinding = false;
	public String[] m_packageNames = { "com.android.settings",
			"com.android.systemui" };
	public String m_allElementName = null;

	public final static int MESSAGE_STATE_CONTEXT = 2;
//	public final int MESSAGE_STATE = 1;
	// use in NotiResultActivity
	public boolean m_bInterruptFlag = false;

	//parse type
	public final static int PARSETYPE_FORCE_STOP = 0x00000001;
	public final static int PARSETYPE_NOTIFY_CLK = 0x00000010;
	public final static int PARSETYPE_NOTIFY_GET = 0x00000100;
	public final static int PARSETYPE_CLEAR_CACH = 0x00001000;

	private HandlerThread mWorkerThread;
	private Handler mWorkerHandler;
	private Messenger mMessenger;
	private ArrayList<String> mAppList;
	private AudioManager mAudioManager;

	public static PhoneType getInstance() {
		if (m_phoneType == null) {
			synchronized (PhoneType.class){
				m_phoneType = new PhoneType();
			}
		}
		return m_phoneType;
	}

	public void init(int aParseType){
		if((aParseType & PARSETYPE_FORCE_STOP) != 0){
			m_asForceStopList = new ArrayList<ActionStep>();
		}
		if((aParseType & PARSETYPE_CLEAR_CACH) != 0){
			m_asClearCatchList = new ArrayList<ActionStep>();
		}
		if((aParseType & PARSETYPE_NOTIFY_CLK) != 0){
			m_asNotifiClickList = new ArrayList<ActionStep>();
		}
		if((aParseType & PARSETYPE_NOTIFY_GET) != 0){
			m_asNotifiGetList = new ArrayList<ActionStep>();
		}
		m_phoneType.parseXML();
	}

	private PhoneType() {
		mAudioManager = (AudioManager) MyApp.getApp().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		mWorkerThread = new HandlerThread("accessibility_thread");
		mWorkerThread.start();
		mWorkerHandler = new Handler(mWorkerThread.getLooper()){
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what){
					case 0:
						doForceStopThread();
						break;
					default:
						break;
				}
			}
		};
	}

	private boolean parseXML() {
		DomParser domParse = new DomParser();
		InputStream is = getClass().getResourceAsStream(
				"/assets/AccessibilityConfig.xml");
		if (is == null) {
			return false;
		}
		if (domParse.parse(is) == false) {
			return false;
		}
		try {
			is.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	private void doForceStopThread(){
		int iErrorCount = 0;
		m_bThreadFlag = true;
		setStreamMute(true);
		Messenger messenger = mMessenger;
		ArrayList<String> appInfoList = mAppList;
		for (int i = 0; i < appInfoList.size(); i++) {
			if (getInterruptFlag()) {
				sendMessageToCaller(
						messenger,
						AccessUtil.TYPE_PACKAGE_FORCE_ERROR_INTERRUPT,
						"INTERRUT");
				break;
			} else {
				String strPkgName = appInfoList.get(i);
				sendMessageToCaller(messenger,
						AccessUtil.TYPE_PACKAGE_FORCE_START,
						strPkgName);
				if (executeClickActionList(MyApp.getApp()
								.getApplicationContext(), strPkgName,
						m_asForceStopList.size(), true, messenger) == true) {
					sendMessageToCaller(messenger,
							AccessUtil.TYPE_PACKAGE_FORCE_SUCCESS,
							strPkgName);
				} else {
					iErrorCount++;
					sendMessageToCaller(
							messenger,
							AccessUtil.TYPE_PACKAGE_FORCE_ERROR_PKG,
							strPkgName);
				}
				waitMilliseconds(m_intervalmillisecond);
				if (getInterruptFlag()) {
					sendMessageToCaller(
							messenger,
							AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_INTERRUPT,
							"INTERRUT");
					break;
				}
			}
		}
		m_allElementName = getBaseInfo() + m_allElementName;
		if (iErrorCount == appInfoList.size()) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FORCE_ALL_ERROR,
					m_allElementName);
		}
		// logInfo(TYPE_LOGINFO, m_allElementName);
		sendMessageToCaller(messenger,
				AccessUtil.TYPE_PACKAGE_FORCE_ALL_END,
				"PACKAGE ALL END");
		m_bThreadFlag = false;
		setStreamMute(false);
	}

	public boolean forceStop(final IBinder aMessenger,
			final ArrayList<String> appInfoList) {
		m_iCurType = PARSETYPE_FORCE_STOP;
		m_iCurStep = 0;
		
		if (aMessenger == null) {
			return false;
		}
		final Messenger messenger = new Messenger(aMessenger);
		if (appInfoList == null) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FORCE_ERROR_APPLIST,
					"APP INFO LIST IS NULL");
			return false;
		}
		if (appInfoList.size() == 0) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FORCE_ERROR_APPLIST,
					"APP INFO LIST IS NULL");
			return false;
		}
		if (m_phoneType == null) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FROCE_MPHONE_NONE,
					"m_phoneType IS NULL");
			return false;
		}
		if (m_asForceStopList == null) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FROCE_ACLIST_NONE,
					"m_asForceStopList IS NULL");
			return false;
		}
		if (m_asForceStopList.size() == 0) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FROCE_ACLIST_NONE,
					"m_asForceStopList IS EMPTY");
			return false;
		}
		if (SleepAccessibilityService.getServiceRunningFlag() == false) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_FORCE_ERROR_SERVICE,
					"SERVICE NOT RUNNING");
			return false;
		}
		setInterruptFlag(false);
		final boolean bRet = false;
		mMessenger = messenger;
		mAppList = appInfoList;
//		if (getThreadFlag() == false) {
//			new Thread() {
//				public void run() {
//					doForceStopThread(messenger, appInfoList);
//				}
//			}.start();
//		}
		if(!m_bThreadFlag){
			mWorkerHandler.removeMessages(0);
			mWorkerHandler.sendEmptyMessage(0);
		}
		return bRet;
	}

//	public boolean notifiChange(final IBinder aBinder,
//								final ArrayList<String> appInfoList, final boolean bStop) {
//		m_iCurType = TYPE_NOTIFICLICK;
//		if (aBinder == null) {
//			PhoneType.getInstance().m_errMsg = "[PhoneType.notifiChange] Exception: handler == null";
//			return false;
//		}
//		final Messenger messenger = new Messenger(aBinder);
//		if (appInfoList == null) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_APPLIST,
//					"APP INFO LIST IS NULL");
//			return false;
//		}
//		if (appInfoList.size() == 0) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_APPLIST,
//					"APP INFO LIST IS NULL");
//			return false;
//		}
//		if (m_phoneType == null) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_MPHONE_NONE,
//					"m_phoneType IS NULL");
//			return false;
//		}
//		if (m_asNotifiClickList == null) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_ACLIST_NONE,
//					"m_asNotifiClickList IS NULL");
//			return false;
//		}
//		if (m_asNotifiClickList.size() == 0) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_ACLIST_NONE,
//					"m_asNotifiClickList IS EMPTY");
//			return false;
//		}
//		if (SleepAccessibilityService.getServiceRunningFlag() == false) {
//			sendMessageToCaller(messenger,
//					AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_SERVICE,
//					"SERVICE NOT RUNNING");
//			return false;
//		}
//		setInterruptFlag(false);
//		final boolean bRet = false;
//		if (getThreadFlag() == false) {
//			new Thread() {
//				public void run() {
//					int iErrorCount = 0;
//					PhoneType.setThreadFlag(true);
//					PhoneType.setStreamMute(true);
//					for (int i = 0; i < appInfoList.size(); i++) {
//						if (getInterruptFlag()) {
//							sendMessageToCaller(
//									messenger,
//									AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_INTERRUPT,
//									"INTERRUT");
//							break;
//						} else {
//							String strPkgName = appInfoList.get(i);
//							sendMessageToCaller(messenger,
//									AccessUtil.TYPE_PACKAGE_NOTIFY_START,
//									strPkgName);
//							if (executeClickActionList(MyApp.getApp()
//									.getApplicationContext(), strPkgName,
//									m_asNotifiClickList.size(), bStop,
//									messenger) == true) {
//								sendMessageToCaller(messenger,
//										AccessUtil.TYPE_PACKAGE_NOTIFY_SUCCESS,
//										strPkgName);
//								PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
//										Integer.toString(i) + strPkgName
//												+ " TRUE");
//							} else {
//								iErrorCount++;
//								sendMessageToCaller(
//										messenger,
//										AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_PKG,
//										strPkgName);
//								PhoneType.logInfo(PhoneType.TYPE_LOGINFO,
//										Integer.toString(i) + strPkgName
//												+ " FALSE");
//							}
//							waitMilliseconds(m_intervalmillisecond);
//							if (getInterruptFlag()) {
//								sendMessageToCaller(
//										messenger,
//										AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_INTERRUPT,
//										"INTERRUT");
//								break;
//							}
//						}
//					}
//					m_allElementName = getBaseInfo() + m_allElementName;
//					if (iErrorCount == appInfoList.size()) {
//						sendMessageToCaller(messenger,
//								AccessUtil.TYPE_PACKAGE_NOTIFY_ALL_ERROR,
//								m_allElementName);
//					}
//					logInfo(TYPE_LOGINFO, m_allElementName);
//					sendMessageToCaller(messenger,
//							AccessUtil.TYPE_PACKAGE_NOTIFY_ALL_END,
//							"PACKAGE ALL END");
//					PhoneType.setThreadFlag(false);
//					PhoneType.setStreamMute(false);
//				}
//			}.start();
//		}
//		return bRet;
//	}

//	public boolean notifiGet(String strPkgName, boolean bStop) {
//		m_iCurType = TYPE_NOTIFIGET;
//		m_iCurStep = 0;
//		if (executeGetActionList(m_asNotifiGetList, strPkgName)) {
//			return true;
//		}
//		return false;
//	}

	public ActionStep getNextStep() {
		ActionStep actionStep = null;
		switch (m_iCurType) {
		case PARSETYPE_FORCE_STOP:
			if ((++m_iCurStep) >= m_asForceStopList.size()) {
				break;
			}
			actionStep = m_asForceStopList.get(m_iCurStep);
			break;
		case PARSETYPE_NOTIFY_GET:
			if ((++m_iCurStep) >= m_asNotifiGetList.size()) {
				break;
			}
			actionStep = m_asNotifiGetList.get(m_iCurStep);
			break;
		case PARSETYPE_NOTIFY_CLK:
			if ((++m_iCurStep) >= m_asNotifiClickList.size()) {
				break;
			}
			actionStep = m_asNotifiClickList.get(m_iCurStep);
			break;
		}
		return actionStep;
	}

	public ActionStep getCurrentStep() {
		ActionStep actionStep = null;
		switch (m_iCurType) {
		case PARSETYPE_FORCE_STOP:
			if (m_iCurStep >= m_asForceStopList.size()) {
				break;
			}
			actionStep = m_asForceStopList.get(m_iCurStep);
			break;
		case PARSETYPE_NOTIFY_GET:
			if (m_iCurStep >= m_asNotifiGetList.size()) {
				break;
			}
			actionStep = m_asNotifiGetList.get(m_iCurStep);
			break;
		case PARSETYPE_NOTIFY_CLK:
			if (m_iCurStep >= m_asNotifiClickList.size()) {
				break;
			}
			actionStep = m_asNotifiClickList.get(m_iCurStep);
			break;
		}
		return actionStep;
	}

	private boolean executeClickActionList(Context m_context,
										   String strPkgName, int stepCount, boolean bStop, Messenger messenger) {
		if (strPkgName == null) {
			return false;
		}
		if (SleepAccessibilityService.getServiceRunningFlag() == false) {
			sendMessageToCaller(messenger,
					AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_SERVICE,
					"SERVICE NOT RUNNING");
			return false;
		}
		m_iCurStep = 0;
		int iCount = m_pkgwaitsecond * 1000 / m_slicemillisecond;
		boolean bRet = true;
		boolean bNext = true;
		int iOldStep = m_iCurStep;

		while (iCount > 0 && bRet) {
			if (getInterruptFlag()) {
				sendMessageToCaller(messenger,
						AccessUtil.TYPE_PACKAGE_NOTIFY_ERROR_INTERRUPT,
						"INTERRUT");
				break;
			}
			try {
				if (bNext == true) {
					bNext = false;
					ActionStep actionStep = getCurrentStep();
					if (SleepAccessibilityService.getServiceRunningFlag() == false) {
						bRet = false;
						break;
					}
					if (actionStep == null) {
						if (m_iCurStep >= stepCount) {
							bRet = true;
							break;
						} else {
							bRet = false;
							break;
						}
					} else {
						if (actionStep.m_asActionName.equalsIgnoreCase("START")) {
							bRet = doAction(m_context, actionStep, strPkgName,
									bStop);
						} else if (actionStep.m_asActionName
								.equalsIgnoreCase("BACK")) {
							bRet = doAction(m_context, actionStep, strPkgName,
									bStop);
							bRet = true;
							break;
						}
					}
				}
				waitMilliseconds(m_slicemillisecond);
				while (getFindingFlag()) {
					if (getInterruptFlag()) {
						break;
					}
					waitMilliseconds(10);
				}
				if (iOldStep == m_iCurStep) {
					bNext = false;
				} else {
					iOldStep = m_iCurStep;
					bNext = true;
				}
				if (m_bWorkingFlag == false) {
					bRet = false;
					break;
				}
				iCount--;
			} catch (Exception e) {
				e.printStackTrace();
				bRet = false;
				break;
			}
		}

		if (iCount <= 0) {
			bRet = false;
			ActionStep actionStep = new ActionStep();
			actionStep.m_asActionName = "BACK";
			doAction(m_context, actionStep, strPkgName, bStop);
		}
		setWrokingFlag(false);
		return bRet;
	}

	private boolean doAction(Context m_context, ActionStep actionStep,
							 String strPkgName, boolean bCheck) {
		boolean bRet = false;
		if (actionStep.m_asActionName.equalsIgnoreCase("START")) {
			if (m_context != null) {
				Uri packageURI = Uri.parse("package:" + strPkgName);
				Intent intentx = new Intent(actionStep.m_asActivityName,
						packageURI);
				intentx.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
						| Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_NO_HISTORY);
				m_context.startActivity(intentx);
				bRet = true;
				setCheckFlag(bCheck);
				setCurPkgName(strPkgName);
				setWrokingFlag(true);
			}
		} else if (actionStep.m_asActionName.equalsIgnoreCase("BACK")) {
			setWrokingFlag(false);
			bRet = true;
		} else {
			bRet = false;
		}
		return bRet;
	}

	public void waitMilliseconds(int count) {
		try {
			Thread.sleep(count);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized boolean getWrokingFlag() {
		return m_bWorkingFlag;
	}

	public synchronized void setWrokingFlag(boolean flag) {
		m_bWorkingFlag = flag;
	}

	public synchronized static boolean getCheckFlag() {
		return getInstance().m_bCheckFlag;
	}

	public synchronized static void setCheckFlag(boolean flag) {
		getInstance().m_bCheckFlag = flag;
	}

	public synchronized static boolean getFindingFlag() {
		return getInstance().m_bFinding;
	}

	public synchronized static void setFindingFlag(boolean flag) {
		getInstance().m_bFinding = flag;
	}

	public synchronized static String getCurPkgName() {
		return getInstance().m_curPkgName;
	}

	public synchronized static void setCurPkgName(String pkgName) {
		getInstance().m_curPkgName = pkgName;
	}

	public synchronized static void setInterruptFlag(boolean flag) {
		getInstance().m_bInterruptFlag = flag;
	}

	public synchronized static boolean getInterruptFlag() {
		return getInstance().m_bInterruptFlag;
	}

	private void sendMessageToCaller(Messenger messenger, int iType,
									 String strData) {
		if (messenger == null) {
			return;
		}

		try {
			Message msg = Message.obtain();
			msg.what = iType;
			Bundle data = new Bundle();
			data.putString("MESSAGE", strData);
			msg.setData(data);
			// msg.sendToTarget();
			// handler.sendMessage(msgMessage);
			messenger.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private String getBaseInfo() {
		String strBaseInfo;
		strBaseInfo = "[PHONE: " + android.os.Build.MANUFACTURER + ","
				+ android.os.Build.MODEL + ","
				+ android.os.Build.VERSION.RELEASE + ","
				+ android.os.Build.VERSION.SDK + "] ";
		strBaseInfo += "[CONFIG:" + m_manufacturer + "," + m_mode + ","
				+ m_release + "," + m_sdk + "] ";

		return strBaseInfo;
	}

	public static void writeServiceFlag(int iFlag) {
		try {
			FileOutputStream fos = MyApp.getApp().getApplicationContext().openFileOutput(
					"serviceflag", Context.MODE_WORLD_WRITEABLE);
			fos.write(String.valueOf(iFlag).toString().getBytes(), 0, 1);
			fos.close();
		} catch (Exception e) {
		}
	}

	public static int getServiceFlag() {
		int iFlag = 0;
		try {
			FileInputStream fin = MyApp.getApp().getApplicationContext()
					.openFileInput("serviceflag");
			iFlag = fin.read();
			iFlag = iFlag - 0x30;
			fin.close();
		} catch (Exception e) {
		}
		return iFlag;
	}

	public void setStreamMute(boolean bMute) {
		if(mAudioManager == null){
			mAudioManager = (AudioManager) MyApp.getApp().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		}
		mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, bMute);
	}
}
