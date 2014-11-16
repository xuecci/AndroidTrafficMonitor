package com.dreamteam.trafficmonitor.db;

import com.dreamteam.trafficmonitor.customdialog.TrafficInfo;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {

	public MySQLiteOpenHelper(Context context) {
		super(context, "TrafficDb", null, 1);
		Log.i("TrafficDb", "MySQLiteOpenHelper>>>>>>>>start");
	}

	public MySQLiteOpenHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// TODO Auto-generated method stub
		String CREATE_TrafficInfo_SQL = "CREATE TABLE TrafficInfo("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "WIFI long default(0),"
				+ "GPRS long default(0),"
				+ "time TIMESTAMP default (datetime('now', 'localtime')),"
				+ "packagename varchar(50))";

		String CREATE_Plans_SQL = "CREATE TABLE Plans("
				+ "company varchar(20)," + "planName varchar(50) primary key,"
				+ "planTraffic int," + "planPrice int)";
		// String DATE_TRIGGER ="";

		db.execSQL(CREATE_TrafficInfo_SQL);
		db.execSQL(CREATE_Plans_SQL);

		Log.i("TrifficDb", "onCreate>>>>>>>>>>>start");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		// TODO Auto-generated method stub
	}

	public void insertTrafficGPRS(long traffic, String packagename) {
		SQLiteDatabase db = getWritableDatabase();
		String sql = "insert into TrafficInfo(GPRS,packagename) values("
				+ traffic + ",'"+ packagename + "')";
		db.execSQL(sql);
		Log.i("TrafficDb", "insertTrafficGPRS>>>>>>>>>>>");
		db.close();
	}
	
	public void insertTrafficWIFI(long traffic, String packagename) {
		SQLiteDatabase db = getWritableDatabase();
		String sql = "insert into TrafficInfo(WIFI,packagename) values("
				+ traffic +",'" + packagename + "')";
		db.execSQL(sql);
		Log.i("TrafficDb", "insertTrafficWIFI>>>>>>>>>>>");
		db.close();
	}

	public ArrayList<TrafficInfo> queryByTime(Context context, String time) {// today,yesterday,samemonth
		SQLiteDatabase db = getReadableDatabase();
		String time1;
		String time2;
		Date d = new Date();
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd 00:00:00");

		if (time.equals("today")) {
			time1 = sDateFormat.format(d.getTime());

			time2 = sDateFormat.format(new Date(d.getTime() + 24 * 60 * 60
					* 1000));
		} else if (time.equals("yesterday")) {
			time1 = sDateFormat.format(new Date(d.getTime() - 24 * 60 * 60
					* 1000));

			time2 = sDateFormat.format(d.getTime());
		} else {
			Calendar a = Calendar.getInstance();
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd 00:00:00");
			a.set(Calendar.DATE, 1);// 把日期设置为当月第一天

			time1 = df.format(a.getTime());
			a.roll(Calendar.DATE, -1);// 日期回滚一天，也就是最后一天

			time2 = df.format(a.getTime());
		}

		String sql = "select sum(WIFI),sum(GPRS),packagename from TrafficInfo where time>='"
				+ time1
				+ "' and time<'"
				+ time2
				+ "' Group by packagename";

		ArrayList<TrafficInfo> appList = new ArrayList<TrafficInfo>();

		PackageManager pm = context.getPackageManager(); // 获得PackageManager对象
		
		Cursor cursor = db.rawQuery(sql, null);

		while (cursor.moveToNext()) {
			long WIFILong = cursor.getLong(0);
			long GPRSLong = cursor.getLong(1);
			String PackNameString = cursor.getString(2);
			Log.i("TrafficStatistics", GPRSLong +","+ WIFILong + PackNameString);
			
			TrafficInfo temp = new TrafficInfo();
			try {
				Drawable icon = pm.getApplicationIcon(PackNameString);
				String   name = pm.getApplicationLabel(pm.getApplicationInfo(PackNameString,0)).toString();
				temp.setAppicon(icon);
				temp.setAppName(name);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			temp.setGPRS(GPRSLong);
			temp.setWIFI(WIFILong);
			appList.add(temp);
		}

		Collections.sort(appList,new Comparator<TrafficInfo>(){
			@Override
			public int compare(TrafficInfo info0, TrafficInfo info1) {
				// TODO Auto-generated method stub
				if(info0.getGPRS() != info1.getGPRS()){
					return (int) (info1.getGPRS()-info0.getGPRS());
				}
				else{
					return (int) (info1.getWIFI()-info0.getWIFI());
				}
			}
		});
		db.close();
		return appList;
	}
}
