package com.tools.accessibility.remotebg;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.text.TextUtils;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.xiang.batterytest.MyApp;
import com.tools.accessibility.uitils.AccessUtil;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class PhoneType {

	// phone base info
	public String m_manufacturer;
	public String m_mode;
	public int m_minsdk;
	public int m_maxsdk;
	public String m_uabsdkver;
	public int m_id;

	// step action list
	public List<ActionStep> m_asForceStopList;

	private int m_iCurStep = 0;
	private static PhoneType m_phoneType = null;

	// use in sleepaccessibility
	public boolean m_bWorkingFlag = false;
	public String[] m_packageNames = { "com.android.settings",
			"com.android.systemui" };

	// use in NotiResultActivity
	public boolean m_bInterruptFlag = false;

	private HandlerThread mWorkerThread;
	private Handler mWorkerHandler;
	private Messenger mMessenger;
	private ArrayList<String> mAppList;
	private AudioManager mAudioManager;
	private Object mFindLock = new Object();
	private Context mContext;

	public void findAndClick(AccessibilityEvent aEvent){
		Log.v("xiangpeng", "event type is "+aEvent.getEventType()+" package name is "+aEvent.getPackageName()+" workflag is "+m_bWorkingFlag);
		if(m_bWorkingFlag){
			ActionStep vStep = getCurrentStep();
			if(vStep != null && vStep.m_asActionName.equalsIgnoreCase("CLICK")){
				Log.v("xiangpeng", "find node "+vStep.m_asElementText.substring(0, 10));
				AccessibilityNodeInfo vNodeInfo = forNode(aEvent.getSource(), vStep.m_asElementText, vStep.m_asElementType);
				if(vNodeInfo != null){
					if(vNodeInfo.isClickable() && vNodeInfo.isEnabled()){
						Log.v("xiangpeng", "go to click "+vNodeInfo.getText().toString());
						getNextStep();
						if(!vNodeInfo.performAction(AccessibilityNodeInfo.ACTION_CLICK)){
							Log.v("xiangpeng", "Click error");
							m_iCurStep--;
						}
						if(checkStepOver()){
							notifyFindLock();
						}
					}
					else{
						Log.v("xiangpeng", "this node is disabled"+vNodeInfo.getText().toString());
					}
				}
			}
		}
	}

	private AccessibilityNodeInfo forNode(AccessibilityNodeInfo node, String strElementText, String aNodeType) {
		AccessibilityNodeInfo vRet = null;
		if(node != null && !TextUtils.isEmpty(strElementText) && !TextUtils.isEmpty(aNodeType)){
			CharSequence vNoteCs = node.getText();
			if(vNoteCs != null && node.getClassName() != null && aNodeType.equalsIgnoreCase(node.getClassName().toString())){
				int vStart = 0;
				try {
					for (int i = 0; i < strElementText.length(); i++) {
						if (strElementText.charAt(i) == '|') {
							String strTmp = strElementText.substring(vStart, i);
							vStart = i + 1;
							if (strTmp.equalsIgnoreCase(vNoteCs.toString())) {
								Log.w("xiangpeng", "find node is"+vNoteCs.toString());
								vRet = node;
								break;
							}
						}
					}

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if(vRet == null){
				int vChildCount = node.getChildCount();
				for(int i = 0; i < vChildCount; i++){
					vRet = forNode(node.getChild(i), strElementText, aNodeType);
					if(vRet != null){
						break;
					}
				}
			}
		}
		return vRet;
	}

	public static PhoneType getInstance() {
		if (m_phoneType == null) {
			synchronized (PhoneType.class){
				m_phoneType = new PhoneType();
			}
		}
		return m_phoneType;
	}

	public void init(){
		m_asForceStopList = new ArrayList<ActionStep>();
		m_phoneType.parseXML();
		Log.v("xiangpeng", "get xml id "+m_id);
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

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private boolean exeClickAction(String aPkgName){
		m_iCurStep = 0;
		m_bWorkingFlag = false;
		boolean vRet = false;
		ActionStep vActionStep = null;
		if(m_iCurStep < m_asForceStopList.size()){
			vActionStep = m_asForceStopList.get(m_iCurStep);
		}
		if(vActionStep != null && vActionStep.m_asActionName != null && vActionStep.m_asActionName.equalsIgnoreCase("START")){
			if(m_bInterruptFlag){
				return false;
			}
			m_bWorkingFlag = true;
			try{
				getNextStep();
				Uri packageURI = Uri.parse("package:" + aPkgName);
				Intent intentx = new Intent(vActionStep.m_asActivityName,
						packageURI);
				intentx.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				mContext.startActivity(intentx, ActivityOptions.makeCustomAnimation(mContext, 0, 0).toBundle());
				synchronized (mFindLock){
					mFindLock.wait(3000);
				}
				if(m_asForceStopList.get(m_iCurStep).m_asActionName.equalsIgnoreCase("BACK")){
					vRet = true;
				}
				else{
					Log.v("xiangpeng", "find time out");
				}
			}
			catch (Exception e){
				vRet = false;
			}
		}
		return vRet;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private void doForceStopThread(){
		setStreamMute(true);
		for (int i = 0; i < mAppList.size(); i++) {
			if (m_bInterruptFlag) {
				sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_ERROR_INT, "INTERRUT");
				break;
			} else {
				String strPkgName = mAppList.get(i);
				sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_START_ONE, strPkgName);
				if (exeClickAction(strPkgName) == true) {
					sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_SUCCE_ONE, strPkgName);
				} else {
					if(m_bInterruptFlag){
						sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_ERROR_INT, "INTERRUT");
						break;
					}
					else{
						sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_ERROR_ONE, strPkgName);
					}
				}
			}
		}
		setStreamMute(false);
		mContext = null;
		Intent vIntent = new Intent(MyApp.getApp().getApplicationContext(), BlankActivity.class);
		vIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
		vIntent.putExtra("finish", true);
		MyApp.getApp().getApplicationContext().startActivity(vIntent, ActivityOptions.makeCustomAnimation(MyApp.getApp().getApplicationContext(), 0, 0).toBundle());
	}

	public void sendOver(){
		sendMessageToCaller(mMessenger, AccessUtil.TYPE_PACKAGE_FORCE_ENDED_ALL, "PACKAGE ALL END");
	}

	public  void realStart(Context aContext){
		if(mAppList.size() > 0){
			mContext = aContext;
			mWorkerHandler.removeMessages(0);
			mWorkerHandler.sendEmptyMessage(0);
		}
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	public void startForceStop(IBinder aBinder, List<String> aAppInfoList){
		if(aBinder != null && aAppInfoList != null && aAppInfoList.size() > 0){
			m_iCurStep = 0;
			mMessenger = new Messenger(aBinder);
			if(mAppList != null){
				mAppList.clear();
			}
			else{
				mAppList = new ArrayList<>(aAppInfoList.size());
			}
			for(int i = 0; i < aAppInfoList.size(); i++){
				String vTmp = aAppInfoList.get(i);
				if(vTmp != null && vTmp.length() > 0){
					mAppList.add(aAppInfoList.get(i));
				}
			}
			m_bInterruptFlag = false;
			Intent vIntent = new Intent(MyApp.getApp().getApplicationContext(), BlankActivity.class);
			vIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_NO_ANIMATION|Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
			MyApp.getApp().getApplicationContext().startActivity(vIntent, ActivityOptions.makeCustomAnimation(MyApp.getApp().getApplicationContext(), 0, 0).toBundle());
		}
	}

	public boolean checkStepOver(){
		boolean vRet = false;
		if((m_iCurStep + 1) >= m_asForceStopList.size()){
			vRet = true;
		}
		return vRet;
	}

	public ActionStep getNextStep() {
		ActionStep actionStep = null;
		m_iCurStep++;
		if(m_iCurStep < m_asForceStopList.size()){
			actionStep = m_asForceStopList.get(m_iCurStep);
		}
		return actionStep;
	}

	public ActionStep getCurrentStep() {
		ActionStep actionStep = null;
		if(m_iCurStep < m_asForceStopList.size()){
			actionStep = m_asForceStopList.get(m_iCurStep);
		}
		return actionStep;
	}


	public void notifyFindLock() {
		m_bWorkingFlag = false;
		synchronized (mFindLock){
			mFindLock.notifyAll();
		}
	}

	public void setInterruptFlag(boolean flag) {
		m_bInterruptFlag = flag;
		if(m_bInterruptFlag){
			notifyFindLock();
		}
	}

	private void sendMessageToCaller(Messenger messenger, int iType,
									 String strData) {
		try {
			Message msg = Message.obtain();
			msg.what = iType;
			Bundle data = new Bundle();
			data.putString("MESSAGE", strData);
			msg.setData(data);
			messenger.send(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void setStreamMute(boolean bMute) {
		if(mAudioManager == null){
			mAudioManager = (AudioManager) MyApp.getApp().getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
		}
		mAudioManager.setStreamMute(AudioManager.STREAM_SYSTEM, bMute);
	}
}
