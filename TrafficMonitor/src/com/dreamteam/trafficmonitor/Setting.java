package com.dreamteam.trafficmonitor;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dreamteam.trafficmonitor.customdialog.CustomDialog;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

public class Setting extends ActionBarActivity {
	
	private ActionBar actionBar;
	private SeekBar WarningLineSeekBar;
	private TextView TWarningLineValue;
	
	private EditText MonthPlanValue;
	private EditText UsedTrafficValue;
	private EditText PackagePlusValue;
	
	private int intMonthPlanValue;//月套餐
	private float floatUsedTrafficValue;//已用流量
	private int intPackagePlusValue;//加油包
	private int intWarningLineSeekBar;//警戒 1-100
	
	private ImageButton commit;
	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        actionBar = this.getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        
        WarningLineSeekBar = (SeekBar) this.findViewById(R.id.WarningLineSeekBar);
        TWarningLineValue = (TextView) this.findViewById(R.id.WarningLineValue);
        MonthPlanValue = (EditText) this.findViewById(R.id.MonthPlanValue);
        UsedTrafficValue =(EditText) this.findViewById(R.id.UsedTrafficValue);
        PackagePlusValue =(EditText) this.findViewById(R.id.PackagePlusValue);
        
        SharedPreferences userInfo = getSharedPreferences("traffic",MODE_PRIVATE);
        
        intMonthPlanValue=userInfo.getInt("MonthPlanValue", 0);
        floatUsedTrafficValue=userInfo.getFloat("UsedTrafficValue", 0f);
        intWarningLineSeekBar=userInfo.getInt("WarningLineSeekBar", 100);
        intPackagePlusValue=userInfo.getInt("PackagePlusValue", 0);
        
        MonthPlanValue.setText(intMonthPlanValue==0?"":(intMonthPlanValue+""));
        WarningLineSeekBar.setProgress(intWarningLineSeekBar);
        UsedTrafficValue.setText(floatUsedTrafficValue==0f?"":(floatUsedTrafficValue+""));
        PackagePlusValue.setText(intPackagePlusValue==0?"":(intPackagePlusValue+""));
        TWarningLineValue.setText(intWarningLineSeekBar+"%");
        
		if(floatUsedTrafficValue!=0f && intMonthPlanValue !=0 && floatUsedTrafficValue*100/intMonthPlanValue>intWarningLineSeekBar){
			//消息通知栏
	        //定义NotificationManager
	        String ns = Context.NOTIFICATION_SERVICE;
	        NotificationManager mNotificationManager = (NotificationManager) getSystemService(ns);
	        //定义通知栏展现的内容信息
	        int icon = R.drawable.logosmall;
	        CharSequence tickerText = "流量超警戒啦(￣▽￣')";
	        long when = System.currentTimeMillis();
	        Notification notification = new Notification(icon, tickerText, when);
	         
	        //定义下拉通知栏时要展现的内容信息
	        Context context = getApplicationContext();
	        CharSequence contentTitle = "流量超警戒啦(￣▽￣')";
	        CharSequence contentText = "小心使用呦，免得浪费RMB(＃°Д°)";
	        Intent notificationIntent = new Intent(this, WelcomeActivity.class);
	        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
	                notificationIntent, 0);
	        notification.setLatestEventInfo(context, contentTitle, contentText,
	                contentIntent);
	         
	        //用mNotificationManager的notify方法通知用户生成标题栏消息通知
	        mNotificationManager.notify(1, notification);
		}
        
        WarningLineSeekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener(){
        
        
			@Override
			public void onProgressChanged(SeekBar arg0, int arg1, boolean arg2) {
				// TODO Auto-generated method stub
				TWarningLineValue.setText(WarningLineSeekBar.getProgress()+"%");
			}

			@Override
			public void onStartTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onStopTrackingTouch(SeekBar arg0) {
				// TODO Auto-generated method stub
				
			}
        	
        });
        
        
        commit = (ImageButton) findViewById(R.id.ImageButtonCommit);
        commit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				intMonthPlanValue =Integer.parseInt(MonthPlanValue.getText().toString().trim().equals("")?"0":MonthPlanValue.getText().toString().trim());
				floatUsedTrafficValue =Float.parseFloat(UsedTrafficValue.getText().toString().trim().equals("")?"0":UsedTrafficValue.getText().toString().trim());
				intWarningLineSeekBar =WarningLineSeekBar.getProgress();
				intPackagePlusValue =Integer.parseInt(PackagePlusValue.getText().toString().trim().equals("")?"0":PackagePlusValue.getText().toString().trim());
				
				SharedPreferences userInfo = getSharedPreferences("traffic",MODE_PRIVATE);
				
				//如果已用流量发生改变就存储当前时间
				float temp = userInfo.getFloat("UsedTrafficValue", floatUsedTrafficValue);
				if (temp!=floatUsedTrafficValue) {
					SimpleDateFormat formatter = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");      
			        Date curDate = new Date(System.currentTimeMillis());//获取当前时间    
			        String currentTime = formatter.format(curDate);     
					userInfo.edit().putString("Date", currentTime).commit();
				}
		        userInfo.edit().putInt("MonthPlanValue", intMonthPlanValue).commit(); 
		        userInfo.edit().putFloat("UsedTrafficValue", floatUsedTrafficValue).commit();  
		        userInfo.edit().putInt("WarningLineSeekBar", intWarningLineSeekBar).commit();  
		        userInfo.edit().putInt("PackagePlusValue", intPackagePlusValue).commit();

		        Toast toast = Toast.makeText(Setting.this, "保存成功", Toast.LENGTH_SHORT); 
		        toast.show();
		        
		        Intent intent  = new Intent(Setting.this,MainActivity.class);      
		        Setting.this.startActivity(intent);
			}
		});
		//关于我们弹窗
		TextView abouts = (TextView) findViewById(R.id.AboutUs);
		abouts.setOnClickListener(new OnClickListener() {
		
		
            @Override
            public void onClick(View v) {
            	CustomDialog.Builder dialog01 = new CustomDialog.Builder(
            			Setting.this);
                dialog01.setTitle("关于我们");
                dialog01.setMessage(R.string.AboutUsString);
                dialog01.setPositiveButton("确定",new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();
                    }
                });
                dialog01.create().show();
                return;
            }
        });
		
		TextView warning = (TextView) findViewById(R.id.WarningNote);
		warning.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
            	CustomDialog.Builder dialog02 = new CustomDialog.Builder(
            			Setting.this);
                dialog02.setTitle("免责声明");
                dialog02.setMessage(R.string.warningNoteString);
                dialog02.setPositiveButton("确定",new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();  
                    }
                });
                dialog02.create().show();
                return;
            }
        });
	}
	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item)
	    {
	        // TODO Auto-generated method stub
	        if(item.getItemId() == android.R.id.home)
	        {
	            finish();
	            return true;
	        }
	        return super.onOptionsItemSelected(item);
	    }
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
        
	}
	
}
