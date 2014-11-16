package com.dreamteam.trafficmonitor.service;

import com.dreamteam.trafficmonitor.R;
import com.dreamteam.trafficmonitor.Setting;
import com.dreamteam.trafficmonitor.customdialog.TrafficInfo;
import com.dreamteam.trafficmonitor.db.MySQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.text.format.Formatter;
import android.util.Log;


/**
 * @author Ryan Mo 
 * 判断是2g/3g还是wifi，设置个变量W，然后判断，
 * 如果开始就是wifi状态，就获取那个uid流量T，然后赋值给W，直到切换状态为止；
 * 如果切到2g/3g就获取，但是不加入那个变量，就是T，T-W为3g流量，记为G，
 * 如果一直没切换，就是W一直未变；此后如果切换了状态，就用T（最新）-G=W（最新）；
 */

public class TrafficService extends Service {

	private TrafficReceiver tReceiver;
	private WifiManager wifiManager;
	private ConnectivityManager cManager;
	private PackageManager pm;
	List<TrafficInfo> trafficInfosOrigin = new ArrayList<TrafficInfo>();
	List<TrafficInfo> trafficInfosWifi;
	List<TrafficInfo> trafficInfosGprs;
	MySQLiteOpenHelper dbhelper = new MySQLiteOpenHelper(this);
	private boolean isWIFI;
	private boolean isGPRS;

	@Override
	public IBinder onBind(Intent intent){
		return null;
	}

	@Override
	public void onCreate() {
		// 初始化
		trafficInfosOrigin = getTrafficInfos();
		SharedPreferences userInfo = getSharedPreferences("traffic",
				MODE_PRIVATE);
		if (userInfo.getInt("MonthPlanValue", -1) == -1) {
			// 消息通知栏
			// 定义NotificationManager
			String ns = Context.NOTIFICATION_SERVICE;
			NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
			// 定义通知栏展现的内容信息
			int icon = R.drawable.logosmall;
			CharSequence tickerText = "你的流量套餐未设定(￣▽￣')";
			long when = System.currentTimeMillis();
			Notification notification = new Notification(icon, tickerText, when);

			// 定义下拉通知栏时要展现的内容信息
			Context context = getApplicationContext();
			CharSequence contentTitle = "你的流量套餐未设定(￣▽￣')";
			CharSequence contentText = "现在去设置吧~(～o￣▽￣)～o。。。";
			Intent notificationIntent = new Intent(this, Setting.class);
			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			notification.setLatestEventInfo(context, contentTitle, contentText,
					contentIntent);

			// 用mNotificationManager的notify方法通知用户生成标题栏消息通知
			mNotificationManager.notify(1, notification);
		}
		// WifiManager,ConnectivityManager
		wifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
		cManager = (ConnectivityManager) this
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		// 注册TrafficReceiver
		tReceiver = new TrafficReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		registerReceiver(tReceiver, filter);
		System.out.print("Service Start");
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		return super.onStartCommand(intent, flags, startId);
	}
    /**
     * @author Ryan Mo
     * @description 网络状态广播接收
     */
	private class TrafficReceiver extends BroadcastReceiver {
		private String action = "";
		private static final String TAG = "TrafficReceiver";

		public void onReceive(Context context, Intent intent) {
			action = intent.getAction();

			if (action.equals(WifiManager.WIFI_STATE_CHANGED_ACTION)) {
				isWIFI = true;
				if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
					// 启用线程每隔两秒统计WIFI流量
					Log.i(TAG, "WIFI_STATE_ENABLED");
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (isWIFI) {
								try {
									Thread.sleep(2000);
									Log.i(TAG, "一次采集WIFI>>>>>start");
									trafficInfosWifi = new ArrayList<TrafficInfo>();
									trafficInfosWifi = getTrafficInfos();

									for (TrafficInfo infoO : trafficInfosOrigin)
										for (TrafficInfo infoW : trafficInfosWifi) {
											long traffic = infoW.getTraffic()
													- infoO.getTraffic();
											if (infoO.getPackageName().equals(
													infoW.getPackageName())
													&& traffic != 0) {
												dbhelper.insertTrafficWIFI(traffic,infoW.getPackageName());
												Log.i(TAG,infoW
														.getPackageName()
														+ "WIFI:"
														+ Formatter
																.formatFileSize(
																		TrafficService.this,
																		traffic));
											}
										}
									trafficInfosOrigin = trafficInfosWifi;
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}).start();
				} else if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_DISABLED) {
					Log.i(TAG, "WIFI_STATE_DISABLED");
					//停止统计WIFI
					isWIFI = false;
					Log.i(TAG, "一次采集WIFI>>>>>end");
				}
			} else if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)){
				Log.i(TAG, "CONNECTIVITY_ACTION");
				NetworkInfo networkInfo = cManager
						.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
				State state = networkInfo.getState();
				if (state == State.CONNECTED
						&& !(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED)) {
					Log.i(TAG, "State.CONNECTED");
					// 启用线程每隔两秒统计GPRS流量
					isGPRS = true;
					new Thread(new Runnable() {

						@Override
						public void run() {
							while (isGPRS) {

								try {
									Thread.sleep(2000);
									Log.i(TAG, "一次采集GPRS>>>>>start");
									trafficInfosGprs = new ArrayList<TrafficInfo>();
									trafficInfosGprs = getTrafficInfos();
									for (TrafficInfo infoO : trafficInfosOrigin)
										for (TrafficInfo infoG : trafficInfosGprs) {
											long traffic = infoG.getTraffic()
													- infoO.getTraffic();
											if (infoO.getPackageName().equals(
													infoG.getPackageName())
													&& traffic != 0) {
												dbhelper.insertTrafficGPRS(traffic,infoG.getPackageName());
												Log.i(TAG,infoG
														.getPackageName()
														+ "GPRS:"
														+ Formatter
																.formatFileSize(
																		TrafficService.this,
																		traffic));
											}
										}
									trafficInfosOrigin = trafficInfosGprs;
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}

						}
					}).start();
				} else if (state == State.DISCONNECTED) {
					Log.i(TAG, "State.DISCONNECTED");
					//停止统计GPRS
					isGPRS = false;
					Log.i(TAG, "一次采集GPRS>>>>>end");
				}
			}
		}
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(tReceiver);
		super.onDestroy();
	}
    /**
     * 获取所有流量信息
     * @param void
     * @return List<TrafficInfo> MyTrafficInfos
     */
	public List<TrafficInfo> getTrafficInfos() {
		List<TrafficInfo> MyTrafficInfos = new ArrayList<TrafficInfo>();

		pm = getPackageManager();
		List<PackageInfo> packinfos = pm
				.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES
						| PackageManager.GET_PERMISSIONS);

		for (PackageInfo info : packinfos) {
			String[] premissions = info.requestedPermissions;
			if (premissions != null && premissions.length > 0) {
				for (String premission : premissions) {
					if ("android.permission.INTERNET".equals(premission)) {
						int uid = info.applicationInfo.uid;
						long total = TrafficStats.getUidRxBytes(uid)
								+ TrafficStats.getUidTxBytes(uid);
						if (total < 0) {
							TrafficInfo trafficInfo = new TrafficInfo();
							trafficInfo.setPackageName(info.packageName);
							trafficInfo.setTraffic(0);
							MyTrafficInfos.add(trafficInfo);
						} else {
							TrafficInfo trafficInfo = new TrafficInfo();
							trafficInfo.setPackageName(info.packageName);
							trafficInfo.setTraffic(total);
							MyTrafficInfos.add(trafficInfo);
						}
					}
				}
			}
		}
		return MyTrafficInfos;
	}
}
