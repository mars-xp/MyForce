package com.xiang.batterytest.util;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;

import com.xiang.batterytest.MyApp;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by jinxiangpeng on 2017/2/21.
 */

public class SystemUtil {

    private static SystemUtil _instance;
    private List<InputMethodInfo> mInputList;
    private List<String> mHomeList;
    private PackageManager mPkgMgr;

    private SystemUtil(){
        mPkgMgr = MyApp.getApp().getApplicationContext().getPackageManager();
        readIme();
        readHome();
    }

    public static SystemUtil getInstance(){
        if(_instance == null){
            synchronized (SystemUtil.class){
                if(_instance == null){
                    _instance = new SystemUtil();
                }
            }
        }
        return _instance;
    }

    private void readHome(){
        if(mPkgMgr == null){
            mPkgMgr = MyApp.getApp().getApplicationContext().getPackageManager();
        }
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = null;
        try{
            resolveInfo = mPkgMgr.queryIntentActivities(
                    intent, PackageManager.MATCH_DEFAULT_ONLY);
        }
        catch (Exception e){
            mPkgMgr = null;
        }
        if(resolveInfo != null){
            mHomeList = new ArrayList<>(resolveInfo.size());
            for (ResolveInfo ri : resolveInfo) {
                mHomeList.add(ri.activityInfo.packageName);
            }
        }
        else{
            mHomeList = new ArrayList<>();
        }
    }

    private void readIme(){
        InputMethodManager vMgr = (InputMethodManager) MyApp.getApp().getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        mInputList = vMgr.getInputMethodList();
    }

    public boolean isIme(String aPkgNm){
        boolean vRet = false;
        if(!TextUtils.isEmpty(aPkgNm)){
            for(int i = 0; i < mInputList.size(); i++){
                if(mInputList.get(i).getPackageName().equals(aPkgNm)){
                    vRet = true;
                    break;
                }
            }
        }
        return vRet;
    }

    public boolean isHome(String aPkgNm){
        boolean vRet = false;
        if(!TextUtils.isEmpty(aPkgNm)){
            for(int i = 0; i < mHomeList.size(); i++){
                if(mHomeList.get(i).equals(aPkgNm)){
                    vRet = true;
                }
            }
            if(!vRet){
                if("com.android.contacts".equals(aPkgNm)){
                    vRet = true;
                }
            }
        }
        return vRet;
    }

    public boolean isSystemUpApp(ApplicationInfo aAppInfo){
        boolean vRet = false;
        if((aAppInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0){
            vRet = true;
        }
        return vRet;
    }

    public boolean isSystemUpApp(String aPkgNm){
        boolean vRet = false;
        if(mPkgMgr == null){
            mPkgMgr = MyApp.getApp().getApplicationContext().getPackageManager();
        }
        try{
            ApplicationInfo vAppInfo = mPkgMgr.getApplicationInfo(aPkgNm, 0);
            vRet = isSystemUpApp(vAppInfo);
        }
        catch (Exception e){
            e.printStackTrace();
            vRet = false;
            mPkgMgr = null;
        }
        return vRet;
    }

    public boolean isSystemApp(ApplicationInfo aAppInfo){
        boolean vRet = false;
        if((aAppInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0){
            vRet = true;
        }
        return vRet;
    }

    public boolean isSystemApp(String aPkgNm) {
        boolean vRet = false;
        if(mPkgMgr == null){
            mPkgMgr = MyApp.getApp().getApplicationContext().getPackageManager();
        }
        try {
            ApplicationInfo vAppInfo = mPkgMgr.getApplicationInfo(aPkgNm, 0);
            vRet = isSystemApp(vAppInfo);
        } catch (Exception e) {
            e.printStackTrace();
            vRet = false;
            mPkgMgr = null;
        }
        return vRet;
    }

    public boolean isStopedApp(ApplicationInfo aAppInfo){
        boolean vRet = false;
        if((aAppInfo.flags & ApplicationInfo.FLAG_STOPPED) != 0){
            vRet = true;
        }
        return vRet;
    }

    public boolean isStopedApp(String aPkgNm){
        boolean vRet = false;
        if(mPkgMgr == null){
            mPkgMgr = MyApp.getApp().getApplicationContext().getPackageManager();
        }
        try{
            ApplicationInfo vAppInfo = mPkgMgr.getApplicationInfo(aPkgNm, 0);
            vRet = isStopedApp(vAppInfo);
        }
        catch (Exception e){
            e.printStackTrace();
            vRet = false;
            mPkgMgr = null;
        }
        return vRet;
    }

    public static boolean isGooglePatch(){
        boolean vRet = false;
        Context aContext = MyApp.getApp().getApplicationContext();
        ActivityManager vManager = (ActivityManager)aContext.getSystemService(Context.ACTIVITY_SERVICE);
        if(vManager != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                List<ActivityManager.RunningAppProcessInfo> vList = vManager.getRunningAppProcesses();
                if(vList != null && vList.size() > 0){
                    vRet = true;
                    for(int i = 0; i < vList.size(); i++){
                        ActivityManager.RunningAppProcessInfo vTmp = vList.get(i);
                        if(vTmp != null && vTmp.pkgList != null && vTmp.pkgList.length > 0){
                            if(!vTmp.pkgList[0].equals(aContext.getPackageName())){
                                vRet = false;
                                break;
                            }
                        }
                    }
                }
                else{
                    vRet = true;
                }
            }
        }
        return vRet;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static String getTopPkgName(){
        String vRet = "";
        Context aContext = MyApp.getApp().getApplicationContext();
        if(isGooglePatch()){
            UsageStatsManager vMgr = (UsageStatsManager) aContext.getSystemService(Context.USAGE_STATS_SERVICE);
            if(vMgr != null){
                long vEnd = System.currentTimeMillis();
                long vBegin = vEnd - 1000*60;
                List<UsageStats> vStateList = vMgr.queryUsageStats(UsageStatsManager.INTERVAL_BEST, vBegin, vEnd);
                if(vStateList != null && vStateList.size() > 0){
                    long vLastTime = 0;
                    for(int i = 0; i < vStateList.size(); i++){
                        UsageStats vTmp = vStateList.get(i);
                        if(vTmp != null && vTmp.getLastTimeUsed() > vLastTime){
                            vLastTime = vTmp.getLastTimeUsed();
                            vRet = vTmp.getPackageName();
                        }
                    }
                }
            }
        }
        else{
            ActivityManager vManager = (ActivityManager) aContext.getSystemService(Context.ACTIVITY_SERVICE);
            if(vManager != null){
                if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
                    List<ActivityManager.RunningAppProcessInfo> vAppList = vManager.getRunningAppProcesses();
                    if (vAppList != null && vAppList.size()>0) {
                        ActivityManager.RunningAppProcessInfo rInfo = vAppList.get(0);
                        if (rInfo != null && rInfo.pkgList != null && rInfo.pkgList.length >0) {
                            vRet = rInfo.pkgList[0];
                        }
                    }
                }
                else{
                    List<ActivityManager.RunningTaskInfo> vTaskList = vManager.getRunningTasks(1);
                    if(vTaskList != null && vTaskList.size() > 0){
                        ActivityManager.RunningTaskInfo vRunningTask = vTaskList.get(0);
                        if(vRunningTask != null && vRunningTask.topActivity != null){
                            vRet = vRunningTask.topActivity.getPackageName();
                        }
                    }
                }
            }
        }
        return vRet;
    }

    public static int getRunProCount(){
        int vRet = 0;
        Context aContext = MyApp.getApp().getApplicationContext();
        ActivityManager vMgr = (ActivityManager)aContext.getSystemService(Context.ACTIVITY_SERVICE);
        if(vMgr != null){
            if(SystemUtil.isGooglePatch()){
                ArrayList<String> vPros = new ArrayList<String>();
                List<ActivityManager.RunningServiceInfo> vList = vMgr.getRunningServices(Integer.MAX_VALUE);
                if(vList != null){
                    for(int i = 0; i < vList.size(); i++){
                        ActivityManager.RunningServiceInfo vInfo = vList.get(i);
                        if(vInfo != null && !vPros.contains(vInfo.process)){
                            vPros.add(vInfo.process);
                        }
                    }
                    vRet = vPros.size();
                }
            }
            else{
                List<ActivityManager.RunningAppProcessInfo> vList = vMgr.getRunningAppProcesses();
                if(vList != null){
                    vRet = vList.size();
                }
            }
        }
        return vRet;
    }

    public static void addWindowView(WindowManager aWm, View aView, WindowManager.LayoutParams aParams){
        if(aWm != null && aView != null && aParams != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                aParams.type = WindowManager.LayoutParams.TYPE_TOAST;
            }
            try{
                aWm.addView(aView, aParams);
            }catch (Exception e){

            }

        }
    }

    public static String getProcessName() {
        BufferedReader cmdlineReader = null;
        try {
            cmdlineReader = new BufferedReader(new InputStreamReader(new FileInputStream("/proc/" + android.os.Process.myPid() + "/cmdline"), "iso-8859-1"));
            int c;
            StringBuilder processName = new StringBuilder();
            while ((c = cmdlineReader.read()) > 0) {
                processName.append((char) c);
            }
            return processName.toString();
        } catch (Exception ignore) {
        } finally {
            try {
                if (cmdlineReader != null) {
                    cmdlineReader.close();
                }
            } catch (IOException ignore) {
            }
        }
        return "";
    }

    public static String getCurrentAppPname() {
        String activePackage = null;
        try {
            activePackage = getTopPkgName();
            if(activePackage == null || activePackage.length() <= 0){
                if(SystemUtil.isGooglePatch()){
                    activePackage = "com.android.settings";
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return activePackage;
    }
}
