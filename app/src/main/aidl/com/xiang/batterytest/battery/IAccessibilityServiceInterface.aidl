package com.xiang.batterytest.battery;

interface IAccessibilityServiceInterface{

	//强力加速
	boolean  startForceClose(IBinder messenger, in List<String> pnames);
	//通知管理
	boolean  startNotiManage(IBinder messenger, in List<String> pnames, in boolean needClose);
	//获取服务状态
	boolean getServicesStatus();
	//设置服务状态
	boolean setServicesStatus(in boolean isRunning);
	//
	boolean writeServiceFlag(in int flag);
	
	//
	boolean setInterruptFlag(in boolean flag);
	//
	boolean getInterruptFlag();
	
}