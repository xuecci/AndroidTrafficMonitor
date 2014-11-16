package com.dreamteam.trafficmonitor;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import com.dreamteam.trafficmonitor.customdialog.MaskImage;
import com.dreamteam.trafficmonitor.customdialog.Shaker;
import com.dreamteam.trafficmonitor.customdialog.Shaker.OnShakeListener;
import com.dreamteam.trafficmonitor.db.MySQLiteOpenHelper;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Matrix;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TrafficDisplay extends Activity {

	private TextView company;
	private TextView usedTraffic;
	private TextView traffic;
	private TextView suggestion;
	private ImageButton setting;
	MaskImage maskImage;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trafficdisplay);

		MySQLiteOpenHelper dbhelper = new MySQLiteOpenHelper(this);
		SQLiteDatabase db = dbhelper.getWritableDatabase();
		
		long sum=0;
		float usedTrafficText=0;

				
		//读取月套餐、保存已用流量百分比
		SharedPreferences userInfo = getSharedPreferences("traffic", 
				Context.MODE_PRIVATE);
		// getString()第二个参数为缺省值，如果preference中不存在该key，将返回缺省值
		String companyText = userInfo.getString("company", "未知");
		float monthPlan = userInfo.getInt("MonthPlanValue", 50);
		
		
		//以下一段是月初加油包清零
		int monthFromFile = userInfo.getInt("Month", 1);
		Date date=new Date();
		//今天是几号
		int day=date.getDate();
		Calendar c=Calendar.getInstance();
		c.setTime(date);
		int month = c.get(Calendar.MONTH)+1;
		if (monthFromFile != month){
			//实例化SharedPreferences.Editor对象
			SharedPreferences.Editor editor = userInfo.edit(); 
			//用putString的方法保存数据 
			editor.putInt("Month", month); 
			editor.putInt("PackagePlusValue", 0); 
			//提交当前数据 
			editor.commit(); 
		}
		
		//读取手动流量校正的月份
		String changedDate = userInfo.getString("Date", "2000-13-32 25:61:61");
		int changedMonth =  Integer.parseInt(String.valueOf(changedDate.charAt(5)) 
				+ String.valueOf(changedDate.charAt(6)));
		//根据本月是否手动校正过读取已用流量
		if (month!=changedMonth){
			String sql = "select GPRS from TrafficInfo where time>=datetime('now', 'start of month')";
			Cursor cursor = db.rawQuery(sql, null);	
	        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
	        while (cursor.moveToNext()) {
	        	sum += cursor.getLong(cursor.getColumnIndex("GPRS"));
	        	Log.i("trafficDisplay", sum + "M");
	        }  
			usedTrafficText = ((float)sum)/1024/1024;
		} else { 
			String sql = "select GPRS from TrafficInfo where time>=datetime('"+ changedDate +"')";
			Cursor cursor = db.rawQuery(sql, null);			
	        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
	        while (cursor.moveToNext()) {
	        	sum += cursor.getLong(cursor.getColumnIndex("GPRS")); 
	        	Log.i("trafficDisplay", sum + "M");
	        }  
			usedTrafficText = ((float)sum)/1024/1024;	
			usedTrafficText += userInfo.getFloat("UsedTrafficValue", 0);
		}
		
		//实例化SharedPreferences.Editor对象
		SharedPreferences.Editor editor = userInfo.edit(); 
		//用putString的方法保存数据 
		SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");      
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间    
        String currentTime = formatter.format(curDate);     
		userInfo.edit().putString("Date", currentTime).commit();
		editor.putFloat("UsedTrafficValue", usedTrafficText); 
		//提交当前数据 
		editor.commit(); 
		
		//本月总可用流量为月套餐加上加油包
		float trafficText = monthPlan + userInfo.getInt("PackagePlusValue", 0);
		
		//布局上几处文字显示设置
		DecimalFormat df = new DecimalFormat("0.00"); 
		company = (TextView) findViewById(R.id.company);
		company.setText(companyText);
		usedTraffic = (TextView) findViewById(R.id.usedTraffic);
		usedTraffic.setText(df.format(usedTrafficText) + "");
		traffic = (TextView) findViewById(R.id.traffic);
		traffic.setText(df.format(trafficText) + "");
		
		float usedPercentage = (usedTrafficText / trafficText) ;
				
		maskImage = (MaskImage) findViewById(R.id.imageView2);
		maskImage.setUsedPercentage(usedPercentage);
		maskImage.draw();

		//跳转到设置页面的按钮
		setting = (ImageButton) findViewById(R.id.setting);
		setting.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				Intent intent = new Intent(TrafficDisplay.this, Setting.class);
				startActivity(intent);
			}
		});
		
		
		//以下部分是智能套餐推荐
		//今天是几号
		int curDay = c.get(Calendar.DATE);
		//当前月的最后一天是几号
		int lastDay = c.getActualMaximum(Calendar.DAY_OF_MONTH);
		//预测本月将会消耗的流量
		float forecast = usedTrafficText/curDay*lastDay;
		suggestion = (TextView) findViewById(R.id.suggestion);
		
		if (forecast < monthPlan){
			suggestion.setText("本月流量充足，请放心使用");
		} else if (forecast < monthPlan+20){
			suggestion.setText("本月流量使用较快，请考虑使用流量加油包");
		} else {
			// 调用SQLiteDatabase对象的query方法进行查询，返回一个Cursor对象：由数据库查询返回的结果集对象  
	        // 第一个参数String：表名  
	        // 第二个参数String[]:要查询的列名  
	        // 第三个参数String：查询条件  
	        // 第四个参数String[]：查询条件的参数  
	        // 第五个参数String:对查询的结果进行分组  
	        // 第六个参数String：对分组的结果进行限制  
	        // 第七个参数String：对查询的结果进行排序  
			int minPlan=999999999;
			int temp=999999999;
	        Cursor planCursor = db.query("Plans", new String[] { "planTraffic" }, "planTraffic>=?",
	        		new String[] { forecast+"" },
	        		null, null, null);  
	        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
	        while (planCursor.moveToNext()) {
	        	temp = minPlan;
	        	minPlan = planCursor.getInt(planCursor.getColumnIndex("planTraffic"));
	        	if (temp<minPlan)
	        		minPlan = temp;
	        }  
	        
	        String planName="";
	        planCursor = db.query("Plans", new String[] { "planName" }, "planTraffic=?",
	        		new String[] { minPlan+"" },
	        		null, null, null);  
	        // 将光标移动到下一行，从而判断该结果集是否还有下一条数据，如果有则返回true，没有则返回false  
	        while (planCursor.moveToNext()) {
	        	planName = planCursor.getString(planCursor.getColumnIndex("planName"));
	        }
			suggestion.setText("每月套餐流量不足，推荐升级为" + planName + "套餐");
		}
		
		db.close();
//		/*Shaker sensorHelper = new Shaker(this);  
//	    sensorHelper.setOnShakeListener(new OnShakeListener() {  
//	          
//	        @Override  
//	        public void onShake() {  
//	            // TODO Auto-generated method stub  
//	            System.out.println("shake");  
//	            RotateAnimation animation = new RotateAnimation(0, -15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//				animation.setDuration(500);
//				maskImage.startAnimation(animation);
//				
//				Timer timer = new Timer();
//				TimerTask task = new TimerTask() {
//					
//					@Override
//					public void run() {
//						// TODO Auto-generated method stub
//						RotateAnimation animation = new RotateAnimation(-15, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//						animation.setDuration(500);
//						
//						maskImage.startAnimation(animation);
//						
//						Timer timer = new Timer();
//						TimerTask task = new TimerTask() {
//							
//							@Override
//							public void run() {
//								// TODO Auto-generated method stub
//								RotateAnimation animation = new RotateAnimation(0, 15, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//								animation.setDuration(500);
//								
//								maskImage.startAnimation(animation);
//								
//								Timer timer = new Timer();
//								TimerTask task = new TimerTask() {
//									
//									@Override
//									public void run() {
//										// TODO Auto-generated method stub
//										RotateAnimation animation = new RotateAnimation(15, 0, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
//										animation.setDuration(500);
//										
//										maskImage.startAnimation(animation);
//										
//										
//									}
//								};
//								
//								timer.schedule(task, 500);
//								
//								
//							}
//						};
//						
//						timer.schedule(task, 500);
//						
//						
//					}
//				};
//				
//				timer.schedule(task, 500);  
//	        }  
//	    });*/
	}
}
