package com.xiang.batterytest.battery;

import android.content.Context;
import android.os.IBinder;
import android.os.RemoteException;

import com.tools.ipc.LocalServiceManager;

import java.util.List;

/**
 * Created by jinxiangpeng on 2017/2/22.
 */

public class AccessibiltyManager implements IAccessibilityServiceInterface {
    private static AccessibiltyManager mInstance;

    public static synchronized AccessibiltyManager getAccessibiltyManager(
            Context context) {
        if (null == mInstance) {
            mInstance = new AccessibiltyManager(context);
        }
        return mInstance;
    }

    private AccessibiltyManager(Context context) {
        mIAccessibilityServiceInterface = IAccessibilityServiceInterface.Stub.asInterface(LocalServiceManager.getInstance().getService("accessibility_service"));
    }

    IAccessibilityServiceInterface mIAccessibilityServiceInterface;

    @Override
    public IBinder asBinder() {
        // TODO 自动生成的方法存根
        return null;
    }

    @Override
    public synchronized boolean getServicesStatus() {
        try {
            return mIAccessibilityServiceInterface.getServicesStatus();
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public synchronized boolean setServicesStatus(boolean isRunning) {
        try {
            mIAccessibilityServiceInterface.setServicesStatus(isRunning);
        } catch (Exception e) {
            return false;
        }
        return true;

    }

    @Override
    public boolean writeServiceFlag(int flag) {
        try {
            mIAccessibilityServiceInterface.writeServiceFlag(flag);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean setInterruptFlag(boolean flag) {
        try {
            mIAccessibilityServiceInterface.setInterruptFlag(flag);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean getInterruptFlag()  {
        // TODO 自动生成的方法存根
        return false;
    }

    @Override
    public boolean startForceClose(IBinder messenger, List<String> pnames)
            throws RemoteException {
        try {
            mIAccessibilityServiceInterface.startForceClose(messenger, pnames);
        } catch (Exception e) {
            return false;
        }
        return true;
        // TODO Auto-generated method stub
    }

    @Override
    public boolean startNotiManage(IBinder messenger, List<String> pnames,
                                   boolean needClose) throws RemoteException {
        try {
            mIAccessibilityServiceInterface.startNotiManage(messenger, pnames,
                    needClose);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
