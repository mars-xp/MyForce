package com.xiang.batterytest.battery;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.WindowManager;

import com.xiang.batterytest.MFScanActivity;
import com.xiang.batterytest.MyApp;
import com.xiang.batterytest.R;
import com.xiang.batterytest.util.AccessUtil;
import com.xiang.batterytest.util.SystemUtil;

import java.util.ArrayList;

public class MFBlankActivity extends AppCompatActivity {
    private static final int MSG_START_DISPOSE = 0;
    private static final int MSG_RESTART_BLANK = 1;
    private static final int MSG_FINISH_BLANK = 2;
    private static final int MSG_INTERRUPT = 3;
    public static final String FINISH_MEM_BLANK = "finish_blank";
    private ArrayList<String> mSelectList;

    private int currentCount = 0;
    private int errorCount = 0;
    private int dispossCount=0;

    private BroadcastReceiver mBatteryReceiver;
    private PhoneReceiver phoneReceiver;

    private int mBatteryScale;
    private int mBatteryLevel;
    private int mProCount;

    private String interrupt = "";

    private Messenger messenger;

    /**
     * 传给PhoneType，供回调消息使用。消息码由处理方定义
     */
    public Handler mHandler = new Handler() {
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case MSG_START_DISPOSE: {
                    if (AccessUtil.needDesktop) {
                        AccessibiltyManager.getAccessibiltyManager(
                                getApplicationContext()).setInterruptFlag(true);
                        finish();
                    } else {
                        try {
                            AccessibiltyManager.getAccessibiltyManager(
                                    getApplicationContext()).startForceClose(messenger.getBinder(),
                                    mSelectList);
                        } catch (RemoteException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                case MSG_FINISH_BLANK: {
                    Intent vIntent = new Intent(MFBlankActivity.this,
                            MFScanActivity.class);
                    vIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                            | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    vIntent.putExtra("finish", true);

                    vIntent.putExtra("result", true);
//				int vSaveTime = 24 * 60;
//				vSaveTime = (vSaveTime * mBatteryLevel / mBatteryScale)
//						//* currentCount / mProCount;
//						* currentCount / mSelectList.size();

                    int vSaveTime =(int) (Math.random() * 30+30)*( currentCount>3?3:currentCount);

                    int vHours = vSaveTime / 60;
                    int vMins = vSaveTime % 60;
                    String vSave = null;
                    if (vHours > 0) {
                        vSave = vHours + "小时" + vMins + "分钟";
                    } else {
                        vSave = vMins + "分钟";
                    }
                    //disabled by xiangpeng
//                    MemoryUtil.saveTime = vSave;
//                    MemoryUtil.currentCount = currentCount;
//                    MemoryUtil.errorCount = errorCount;
                    //ended by xiangpeng

                    startActivity(vIntent);
                    finish();
                    break;
                }
                case MSG_INTERRUPT: {// 打断
                    AccessibiltyManager.getAccessibiltyManager(
                            getApplicationContext()).setInterruptFlag(true);
                    break;
                }
                case MSG_RESTART_BLANK: {
                    MyApp.getApp().getMfService().startBlank();
                    break;
                }

                case AccessUtil.TYPE_PACKAGE_FORCE_START: {
                    String pname = msg.getData().getString("MESSAGE");
                    MyApp.getApp().getMfService().startDisposeOne(pname);
                    dispossCount++;
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_SUCCESS: {
                    // TODO:完成一个要记录，然后好刷新
                    String pname = msg.getData().getString("MESSAGE");
                    currentCount++;
                    // NotiUtil.saveAppNotiStatus(pname, bNeedClose);
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ERROR_PKG: {// 包名有错
                    errorCount++;
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ERROR_SERVICE: {// 服务有错
                    // Toast.makeText(context, text, duration)
                    finish();
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ERROR_APPLIST: {// 列表有错

                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ERROR_INTERRUPT: {// 打断
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ERROR_HANDLER: {// handler 错误
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ALL_END: {// 完成
                    mHandler.sendEmptyMessage(MSG_RESTART_BLANK);
                    break;
                }
                case AccessUtil.TYPE_PACKAGE_FORCE_ALL_ERROR: {// 错误
                    String str = msg.getData().getString("MESSAGE");
                    sendErrorMsg(str);
                    break;
                }
                default:

                    break;
            }
        };
    };

    private void sendErrorMsg(final String str) {
//        new Thread(new Runnable() {
//
//            @Override
//            public void run() {
//                SystemUtil
//                        .uploadLog(MFBlankActivity.this, str, "userAssistlog");
//            }
//        }).start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AccessUtil.needDesktop = false;
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        messenger=new Messenger(mHandler);
        int test = getIntent().getIntExtra("TEST", 0);
//        setContentView(R.layout.noti_blank_layout);
        int mode = getIntent().getIntExtra("mode", 0);
        if (mode == 1) {
            phoneReceiver = new PhoneReceiver();
            IntentFilter filter = new IntentFilter();
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            filter.addAction(TelephonyManager.ACTION_PHONE_STATE_CHANGED);
            filter.addAction(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            filter.setPriority(Integer.MAX_VALUE);
            registerReceiver(phoneReceiver, filter);

            if (mBatteryReceiver == null) {
                mBatteryReceiver = new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(
                                Intent.ACTION_BATTERY_CHANGED)) {
                            mBatteryLevel = intent.getIntExtra("level", 0);
                            mBatteryScale = intent.getIntExtra("scale", 100);
                            mProCount = SystemUtil.getRunProCount();

//							ActivityManager vActivityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
//							List<ActivityManager.RunningAppProcessInfo> list = vActivityManager.getRunningAppProcesses();
//							if (list != null){
//								mProCount = list.size();
//							}else {
//								mProCount = 0;
//							}

                            unregisterReceiver(mBatteryReceiver);
                            mBatteryReceiver = null;
                        }
                    }
                };
                IntentFilter vFilter = new IntentFilter();
                vFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
                registerReceiver(mBatteryReceiver, vFilter);
            }

            mSelectList = getIntent().getStringArrayListExtra("selectList");
            // MyLog.e("onCreate_mSelectList",
            // String.valueOf(mSelectList.size()));
            if (mSelectList == null||mSelectList.size()==0) {
                finish();
            } else {
                Intent intent = new Intent(getApplicationContext(),
                        MFDisposeServices.class);
                intent.putExtra("count", mSelectList.size());
                intent.putExtra("firstName", mSelectList.get(0));
                MyApp.getApp().getMfService().onConnected(intent);
                mHandler.sendEmptyMessageDelayed(MSG_START_DISPOSE, 500);
            }
        } else {
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        try {
            if (phoneReceiver != null) {
                unregisterReceiver(phoneReceiver);
            }
            if (mBatteryReceiver != null) {
                unregisterReceiver(mBatteryReceiver);
                mBatteryReceiver = null;
            }
            MyApp.getApp().getMfService().onDisconnected();
        } catch (Exception e) {
            e.printStackTrace();
        }

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        int test = getIntent().getIntExtra("TEST", 0);
        int mode = getIntent().getIntExtra("mode", 0);

        if (mode == 1) {
            mHandler.sendEmptyMessageDelayed(MSG_FINISH_BLANK, 500);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            interrupt = "onBack";
            mHandler.sendEmptyMessage(MSG_INTERRUPT);
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }

    public class PhoneReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)
                    || intent.getAction().equals(
                    TelephonyManager.ACTION_PHONE_STATE_CHANGED)) {
                interrupt = intent.getAction().replace(
                        "android.intent.action.", "");
                mHandler.sendEmptyMessage(MSG_INTERRUPT);
            }
            if (intent.getAction().equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra("reason");
                interrupt = reason;
                if ("homekey".equals(reason)) {
                    AccessUtil.needDesktop = true;
                    finish();
                }
                if ("homekey".equals(reason) || "lock".equals(reason)
                        || "recentapps".equals(reason)
                        || "assist".equals(reason)) {
                    mHandler.sendEmptyMessage(MSG_INTERRUPT);
                }
            }
        }
    }
}
