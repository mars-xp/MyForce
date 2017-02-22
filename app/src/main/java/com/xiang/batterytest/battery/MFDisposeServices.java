package com.xiang.batterytest.battery;

import android.annotation.TargetApi;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.drawable.AnimationDrawable;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xiang.batterytest.MyApp;
import com.xiang.batterytest.R;
import com.xiang.batterytest.util.AccessUtil;
import com.xiang.batterytest.util.SystemUtil;

public class MFDisposeServices extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        MyApp.getApp().setMfService(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private final static int MSG_START_DISPOSE = 1;
    private final static int MSG_INIT = 2;
    private final static int MSG_REMOVE_VIEW = 3;
    private RelativeLayout mLayout;
    public int appsCount = 0;
    float mScreenH;
    WindowManager.LayoutParams wmParams;
    // 创建浮动窗口设置布局参数的对象
    WindowManager mWindowManager;

    private boolean isFlowShowing = false;

    private ImageView imgIcon;
    private TextView txtNum, txtCount, txtName;

    private String mOwner;

    private boolean needMonitor = true;

    private String mSimpleName;

    private int mIndex = 1;

    private int time = 14;

    private Handler mHandler = new Handler() {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 0: {
                    new Thread() {
                        public void run() {
                            while (needMonitor) {
                                String p = SystemUtil
                                        .getCurrentAppPname();
                                if (p == null) {
                                    p = "";
                                }

                                if (!"com.android.settings".equals(p)
                                        && !"com.htc.usage".equals(p)) {
                                    if (!	 AccessibiltyManager.getAccessibiltyManager(
                                            getApplicationContext()).getInterruptFlag()) {
                                        AccessibiltyManager.getAccessibiltyManager(
                                                getApplicationContext()).setInterruptFlag(true);
                                    }
                                    needMonitor = false;
                                }
                                try {
                                    sleep(1000);
                                } catch (InterruptedException e) {
                                    // TODO 自动生成的 catch 块
                                    e.printStackTrace();
                                }
                            }

                        };
                    }.start();
                    break;
                }
                case MSG_START_DISPOSE: {
                    time = 14;
                    try {
                        String pname = (String) msg.obj;
                        //disabled by xiangpeng
//                        AppInfo appInfo = AppInfoManager.getAppInfoManager(
//                                MFDisposeServices.this).getAppInfo(pname);
                        //disabled by xiangpeng
                        txtName.setText(pname);
                        //disabled by xiangpeng
//                        mImageFetcher.loadImage(ImageFetcher.getKeyWithOwner(
//                                mSimpleName, ImageFetcher.IMG_TYPE_OTHER,
//                                appInfo.pname), imgIcon);
                        //disabled by xiangpeng
                        txtNum.setText(String.valueOf(mIndex));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mIndex++;
                    break;
                }
                case MSG_INIT: {
                    try {
                        String pname = (String) msg.obj;
//                        AppInfo appInfo = AppInfoManager.getAppInfoManager(
//                                MFDisposeServices.this).getAppInfo(pname);
                        txtName.setText(pname);
//                        mImageFetcher.loadImage(ImageFetcher.getKeyWithOwner(
//                                mSimpleName, ImageFetcher.IMG_TYPE_OTHER,
//                                appInfo.pname), imgIcon);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                }
                case MSG_REMOVE_VIEW: {
                    try{
                        if (mLayout != null && mWindowManager != null) {
                            // 移除悬浮窗口
                            try {
                                mWindowManager.removeView(mLayout);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
                case 99: {
                    try{
                        if (AccessUtil.needDesktop) {
                            hideView();
                        } else {
                            if (time == 0) {
                                hideView();
                                startBlank();
                            } else {
                                time--;
                                if(mHandler != null){
                                    mHandler.sendEmptyMessageDelayed(99, 1000);
                                }
                            }
                        }
                    }
                    catch(Exception e){
                        e.printStackTrace();
                    }
                    break;
                }
            }

        };

    };

    public void onConnected(Intent intent){
        mSimpleName = MFDisposeServices.class.getSimpleName();
        createFloatView();
        int count = intent.getIntExtra("count", 0);
        txtCount.setText("/" + String.valueOf(count));
        txtNum.setText("1");
        mIndex = 1;
//        createInfoLoader();
//        mImageFetcher = createImageFetcher(mSimpleName, R.drawable.icon_default);

        String firstName = intent.getStringExtra("firstName");
        Message msg = new Message();
        msg.what = MSG_INIT;
        msg.obj = firstName;
        mHandler.sendMessage(msg);
        mHandler.sendEmptyMessageDelayed(0, 2000);
        mHandler.sendEmptyMessage(99);
    }

    public void onDisconnected(){
        needMonitor = false;
        hideView();
        stopSelf();
    }

//    protected ImageFetcher createImageFetcher(String owner, int iconDefault) {
//        this.mOwner = owner;
//        ImageCacheParams cacheParams = ImageCacheParams.getInstance(this,
//                "thumbs");
//        cacheParams
//                .setMemCacheSizePercent(ImageCacheParams.DEFAULT_MEM_CACHE_PERCENT);
//        cacheParams.diskCacheEnabled = false;
//        mImageFetcher = ImageFetcher.getInstance(this, getResources()
//                .getDimensionPixelSize(R.dimen.common_software_list_item_icon));
//        mImageFetcher.setLoadingImage(iconDefault);
//        mImageFetcher.addImageCache(cacheParams);
//
//        mImageFetcher.addGetter(mOwner, new IconGetter(this, mInfoLoader));
//        mImageFetcher.setPauseWork(false);
//        mImageFetcher.setExitTasksEarly(false);
//        return mImageFetcher;
//    }

//    protected void createInfoLoader() {
//        mInfoLoader = AppInfoLoader.createAppInfoLoader(this);
//    }

    @TargetApi(Build.VERSION_CODES.FROYO)
    private void createFloatView() {
        isFlowShowing = false;
        mWindowManager = (WindowManager) this.getApplicationContext()
                .getSystemService(Context.WINDOW_SERVICE);
        wmParams = new WindowManager.LayoutParams();
        wmParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        wmParams.height = WindowManager.LayoutParams.MATCH_PARENT;

        wmParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 1280
                | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
        wmParams.format = 1;
        wmParams.type = WindowManager.LayoutParams.TYPE_SYSTEM_ERROR;
        wmParams.softInputMode = WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE;
        wmParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        LayoutInflater inflate = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (mLayout == null) {
            mLayout = (RelativeLayout) inflate.inflate(
                    R.layout.mf_dispose_activity, null);
            imgIcon = (ImageView) mLayout.findViewById(R.id.imgIcon);
            txtCount = (TextView) mLayout.findViewById(R.id.txtCount);
            txtName = (TextView) mLayout.findViewById(R.id.txtName);
            txtNum = (TextView) mLayout.findViewById(R.id.txtNum);
            ImageButton btnClose = (ImageButton) mLayout
                    .findViewById(R.id.btnClose);
            btnClose.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    if (! AccessibiltyManager.getAccessibiltyManager(
                            getApplicationContext()).getInterruptFlag()) {
                        AccessibiltyManager.getAccessibiltyManager(
                                getApplicationContext()).setInterruptFlag(true);
                    }
                }
            });
        }

        if (mWindowManager != null && mLayout != null && !isFlowShowing
                && !AccessUtil.isDebug) {
            //modify by xiangpeng for android M
            //mWindowManager.addView(mLayout, wmParams);
            SystemUtil.addWindowView(mWindowManager, mLayout, wmParams);
            //ended
            ImageView anim = (ImageView) mLayout.findViewById(R.id.imgAnim);
            anim.setImageResource(R.drawable.mf_anim);
            AnimationDrawable animationDrawable = (AnimationDrawable) anim
                    .getDrawable();
            animationDrawable.start();
            mLayout.invalidate();
            mLayout.invalidate();

            isFlowShowing = true;
        }

    }

    public void startDisposeOne(String pname) {
        Message msg = new Message();
        msg.what = MSG_START_DISPOSE;
        msg.obj = pname;
        mHandler.sendMessage(msg);
    }

    public class MyBinder extends Binder {

        public MFDisposeServices getService() {
            return MFDisposeServices.this;
        }
    }

    public void startBlank() {
        if (AccessUtil.needDesktop) {
            hideView();
            // PhoneType.needDesktop = false;
        } else {
            time = 1;
            Intent intent = new Intent(getApplicationContext(),
                    MFBlankActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                    | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("mode", 1);
            intent.putExtra("TEST", 999);
            // SystemUtil.startActivity(mContext, intent);
            startActivity(intent);
        }
    }

    public void hideView() {

        mHandler.sendEmptyMessage(MSG_REMOVE_VIEW);
    }

}
