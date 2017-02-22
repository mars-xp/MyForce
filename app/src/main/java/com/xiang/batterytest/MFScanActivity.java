package com.xiang.batterytest;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.xiang.batterytest.battery.MFBlankActivity;
import com.xiang.batterytest.util.SystemUtil;

import java.util.ArrayList;
import java.util.List;

public class MFScanActivity extends AppCompatActivity {

    private Button mForceStop;
    private int mAppCount;
    private ArrayList<String> mCheckedList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mfscan);
        initView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initData();
        mForceStop.setText("优化"+"("+mAppCount+")");
    }

    private void initView(){
        mForceStop = (Button)findViewById(R.id.id_btn_optimize);
        mForceStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doClean();
            }
        });
    }

    private void doClean() {
        Intent intent = new Intent();
        intent.putStringArrayListExtra("selectList", mCheckedList);
        intent.putExtra("mode", 1);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION
                | Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setClass(MFScanActivity.this, MFBlankActivity.class);
        startActivity(intent);
    }

    private boolean isToRemove(ApplicationInfo aAppInfo){
        boolean vRet = false;
        String vPkgNm = this.getPackageName();
        if(vPkgNm.equals(aAppInfo.packageName) || SystemUtil.getInstance().isHome(aAppInfo.packageName)
                || SystemUtil.getInstance().isIme(aAppInfo.packageName) || SystemUtil.getInstance().isSystemApp(aAppInfo)
                || SystemUtil.getInstance().isStopedApp(aAppInfo) || SystemUtil.getInstance().isSystemUpApp(aAppInfo)){
            vRet = true;
        }
        return vRet;
    }


    private void initData(){
        mAppCount = 0;
        mCheckedList = new ArrayList<>();
        List<ApplicationInfo> vList = this.getPackageManager().getInstalledApplications(0);
        if(vList != null && vList.size() != 0){
            for(int i = 0; i < vList.size(); i++){
                if(isToRemove(vList.get(i))){
                    vList.remove(i);
                    i--;
                }
                else{
                    mCheckedList.add(vList.get(i).packageName);
                }
            }
            Log.v("xiang", "get a list "+mCheckedList.size());
            mAppCount = mCheckedList.size();
        }
    }
}