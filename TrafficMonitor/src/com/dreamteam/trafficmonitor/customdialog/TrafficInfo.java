package com.dreamteam.trafficmonitor.customdialog;

import android.graphics.drawable.Drawable;

public class TrafficInfo{
	
	/* @author Ryan Mo
	 * 
	 * 流量信息类
	 * 
	 */
	public long traffic;
	public String packageName;
	public String appName;
	public Drawable appicon;
	public Long GPRS = (long) 0;
	public Long WIFI = (long) 0;
	
	public Long getGPRS() {
		return GPRS;
	}

	public void setGPRS(Long gPRS) {
		GPRS = gPRS;
	}

	public Long getWIFI() {
		return WIFI;
	}

	public void setWIFI(Long wIFI) {
		WIFI = wIFI;
	}

	public String getAppName() {
		return appName;
	}

	public void setAppName(String appName) {
		this.appName = appName;
	}

	public Drawable getAppicon() {
		return appicon;
	}

	public void setAppicon(Drawable appicon) {
		this.appicon = appicon;
	}

	public long getTraffic() {
		return traffic;
	}
	
	public void setTraffic(long traffic) {
		this.traffic = traffic;
	}
		
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
}
