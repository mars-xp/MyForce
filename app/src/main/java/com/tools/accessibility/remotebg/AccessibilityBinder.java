package com.tools.accessibility.remotebg;

import android.os.IBinder;
import android.os.RemoteException;

import com.xiang.batterytest.battery.IAccessibilityServiceInterface;

import java.util.List;

public class AccessibilityBinder extends IAccessibilityServiceInterface.Stub {

	public AccessibilityBinder() {

	}

	@Override
	public void startForceStop(IBinder messenger, List<String> pnames) throws RemoteException {
		PhoneType.getInstance().startForceStop(messenger, pnames);
	}

	@Override
	public boolean setInterruptFlag(boolean flag) throws RemoteException {
		PhoneType.getInstance().setInterruptFlag(flag);
		return true;
	}

	@Override
	public boolean isServiceEnable() throws RemoteException {
		return SleepAccessibilityService.mIsServiceRunning;
	}
}
