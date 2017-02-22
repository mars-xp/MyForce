package com.xiang.batterytest.remotebg;

import android.os.IBinder;
import android.os.RemoteException;

import com.tools.ipc.LocalServiceManager;
import com.xiang.batterytest.battery.IAccessibilityServiceInterface;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityBinder extends IAccessibilityServiceInterface.Stub {

	public AccessibilityBinder() {

	}	

	@Override
	public boolean getServicesStatus() throws RemoteException {
		// TODO 自动生成的方法存根
		return SleepAccessibilityService.getServiceRunningFlag();
	}

	@Override
	public boolean setServicesStatus(boolean isRunning) {
		SleepAccessibilityService.setServiceRunningFlag(isRunning);
		return true;
	}

	@Override
	public boolean writeServiceFlag(int flag) throws RemoteException {
		PhoneType.writeServiceFlag(flag);
		return true;
	}

	@Override
	public boolean setInterruptFlag(boolean flag) throws RemoteException {
		PhoneType.setInterruptFlag(flag);
		return true;
	}

	@Override
	public boolean getInterruptFlag() throws RemoteException {
		return PhoneType.getInterruptFlag();
	}

	@Override
	public boolean startForceClose(IBinder messenger, List<String> pnames)
			throws RemoteException {
		PhoneType.getInstance().forceStop(messenger,
				new ArrayList<String>(pnames));
		return true;
	}

	@Override
	public boolean startNotiManage(IBinder messenger, List<String> pnames,
								   boolean needClose) throws RemoteException {
		PhoneType.getInstance().notifiChange(messenger,
				new ArrayList<String>(pnames), !needClose);
		return true;
	}

}
