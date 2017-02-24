package com.xiang.batterytest.battery;

interface IAccessibilityServiceInterface{

	void startForceStop(IBinder messenger, in List<String> pnames);
	boolean setInterruptFlag(in boolean flag);
	boolean getInterruptFlag();
}