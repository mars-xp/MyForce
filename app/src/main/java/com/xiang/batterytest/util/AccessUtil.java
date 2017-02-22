package com.xiang.batterytest.util;

import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

public class AccessUtil {
	private static String TAG = "PhoneUtil";

	public static final int TYPE_PACKAGE_NOTIFY_START = 10001;
	public static final int TYPE_PACKAGE_NOTIFY_SUCCESS = 10002;
	public static final int TYPE_PACKAGE_NOTIFY_ERROR_PKG = 10003;
	public static final int TYPE_PACKAGE_NOTIFY_ERROR_SERVICE = 10004;
	public static final int TYPE_PACKAGE_NOTIFY_ERROR_APPLIST = 10005;
	public static final int TYPE_PACKAGE_NOTIFY_ERROR_INTERRUPT = 10006;
	public static final int TYPE_PACKAGE_NOTIFY_ERROR_HANDLER = 10007;
	public static final int TYPE_PACKAGE_NOTIFY_ALL_ERROR = 10008;
	public static final int TYPE_PACKAGE_NOTIFY_ALL_END = 10009;
	public static final int TYPE_PACKAGE_NOTIFY_MPHONE_NONE = 10010;
	public static final int TYPE_PACKAGE_NOTIFY_ACLIST_NONE = 10011;
	public static final int TYPE_PACKAGE_NOTIFY_CONTEXT_NONE = 10012;

	public static final int TYPE_PACKAGE_FORCE_START = 11001;
	public static final int TYPE_PACKAGE_FORCE_SUCCESS = 11002;
	public static final int TYPE_PACKAGE_FORCE_ERROR_PKG = 11003;
	public static final int TYPE_PACKAGE_FORCE_ERROR_SERVICE = 11004;
	public static final int TYPE_PACKAGE_FORCE_ERROR_APPLIST = 11005;
	public static final int TYPE_PACKAGE_FORCE_ERROR_INTERRUPT = 11006;
	public static final int TYPE_PACKAGE_FORCE_ERROR_HANDLER = 11007;
	public static final int TYPE_PACKAGE_FORCE_ALL_ERROR = 11008;
	public static final int TYPE_PACKAGE_FORCE_ALL_END = 11009;
	public static final int TYPE_PACKAGE_FROCE_MPHONE_NONE = 11010;
	public static final int TYPE_PACKAGE_FROCE_ACLIST_NONE = 11011;
	public static final int TYPE_PACKAGE_FROCE_CONTEXT_NONE = 11012;

	public static boolean isDebug = true;
	public static boolean needDesktop = false;

	// android 4.1.x 以上支持辅助功能
	public static boolean isAccessibilitySupported() {
		if (Build.MANUFACTURER.equalsIgnoreCase("huawei")){
			return Build.VERSION.SDK_INT >= 16 && Build.VERSION.SDK_INT < 23;
		}else if(Build.MODEL.toUpperCase().equals("SM-J3119")) {
            return false;
		}else{
			return android.os.Build.VERSION.SDK_INT >= 16;
		}
	}

	/**
	 * 获取CPU核心数
	 * 
	 * @return
	 */
	public static int getNumCores() {
		// Private Class to display only CPU devices in the directory listing
		class CpuFilter implements FileFilter {
			@Override
			public boolean accept(File pathname) {
				// Check if filename is "cpu", followed by a single digit number
				if (Pattern.matches("cpu[0-9]", pathname.getName())) {
					return true;
				}
				return false;
			}
		}

		try {
			// Get directory containing CPU info
			File dir = new File("/sys/devices/system/cpu/");
			// Filter to only list the devices we care about
			File[] files = dir.listFiles(new CpuFilter());
			if (files != null) {
				return files.length;
			}
			// Return the number of cores (virtual CPU devices)
			return 0;
		} catch (Exception e) {
			// Print exception
			e.printStackTrace();
			// Default to return 1 core
			return 1;
		}
	}

	/**
	 * 实时获取CPU当前频率（单位KHZ）
	 * 
	 * @return
	 */
	public static String getCurCpuFreqString() {
		String result = "N/A";
		try {
			FileReader fr = new FileReader(
					"/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
			BufferedReader br = new BufferedReader(fr);
			String text = br.readLine();
			if (text != null) {
				result = text.trim();
			}
			br.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return result;
	}

	public static float getCurCpuFreq() {
		String strCpuFreq = getCurCpuFreqString();

		if ("N/A".equals(strCpuFreq)) {
			return 0f;
		} else {
			return Float.parseFloat(strCpuFreq);
		}
	}

	/**
	 * 内存：/proc/meminfo：
	 */
	public static int getTotalMemory() {
		String str1 = "/proc/meminfo";// 系统内存信息文件
		String str2;
		String[] arrayOfString;
		int initial_memory = 0;

		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
			if (str2 != null) {
				arrayOfString = str2.split("\\s+");
				for (String num : arrayOfString) {
				}

				initial_memory = Integer.valueOf(arrayOfString[1]).intValue();// 获得系统总内存，单位是KB
				localBufferedReader.close();
			}
		} catch (IOException e) {
		}
		return initial_memory / (1024 * 1024);// KByte =>GByte
	}

	public long[] getRomMemroy() {
		long[] romInfo = new long[2];
		// Total rom memory
		romInfo[0] = getTotalInternalMemorySize();

		// Available rom memory
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		romInfo[1] = blockSize * availableBlocks;
		getVersion();
		return romInfo;
	}

	public long getTotalInternalMemorySize() {
		File path = Environment.getDataDirectory();
		StatFs stat = new StatFs(path.getPath());
		long blockSize = stat.getBlockSize();
		long totalBlocks = stat.getBlockCount();
		return totalBlocks * blockSize;
	}

	/**
	 * 系统的版本信息：
	 * 
	 * @return
	 */
	public String[] getVersion() {
		String[] version = { "null", "null", "null", "null" };
		String str1 = "/proc/version";
		String str2;
		String[] arrayOfString;
		try {
			FileReader localFileReader = new FileReader(str1);
			BufferedReader localBufferedReader = new BufferedReader(
					localFileReader, 8192);
			str2 = localBufferedReader.readLine();
			if (str2 != null) {
				arrayOfString = str2.split("\\s+");
				version[0] = arrayOfString[2];// KernelVersion
			}
			localBufferedReader.close();
		} catch (IOException e) {
		}
		version[1] = Build.VERSION.RELEASE;// firmware version
		version[2] = Build.MODEL;// model
		version[3] = Build.DISPLAY;// system version
		return version;
	}

	public static void setStatusForWidget(Context context, boolean status) {
		try {
			FileOutputStream outStream = context.openFileOutput(
					"SleepAccessibilityService.txt", Context.MODE_PRIVATE);
			outStream.write(status ? "Running".getBytes() : "UnRunning"
					.getBytes());
			outStream.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public synchronized static boolean getServiceRunningFlagForWidget(
			Context context) {
		try {
			FileInputStream inputStream = context
					.openFileInput("SleepAccessibilityService.txt");// 只需传文件名
			byte[] bytes = new byte[1024];
			ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();
			while (inputStream.read(bytes) != -1) {
				arrayOutputStream.write(bytes, 0, bytes.length);
			}
			inputStream.close();
			arrayOutputStream.close();
			String content = new String(arrayOutputStream.toByteArray());

			return (content != null && content.startsWith("Running"));
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

	}
}
