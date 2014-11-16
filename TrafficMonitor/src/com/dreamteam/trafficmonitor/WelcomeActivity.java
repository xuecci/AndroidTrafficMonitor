package com.dreamteam.trafficmonitor;

import com.dreamteam.trafficmonitor.service.TrafficService;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;

public class WelcomeActivity extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);  
		setContentView(R.layout.activity_welcome);
        
		String company;
		
		Configuration cfg = getResources().getConfiguration();
		if (cfg.mcc == 460 && cfg.mnc == 01)
		{
		   //联通的
			company = "中国联通";
		}
		else if (cfg.mcc == 460 && (cfg.mnc == 00 ||cfg.mnc == 02))
		{
		   //移动的
			company = "中国移动";
		}
		else if (cfg.mcc == 460 && cfg.mnc == 03)
		{
		   //电信
			company = "中国电信";
		}
		else {
			company = "未知";
		}
		
		SharedPreferences userInfo = getSharedPreferences("traffic",MODE_PRIVATE);
		userInfo.edit().putString("company", company).commit();
		
		Intent MainService=new Intent(WelcomeActivity.this, TrafficService.class); 
	    startService(MainService);//启动服务
	    
		new Handler().postDelayed(new Runnable(){

		@Override
		public void run() {
		    Intent intent = new Intent(WelcomeActivity.this,MainActivity.class);  
		    startActivity(intent);
		    WelcomeActivity.this.finish();
		}
		},1000);
	}
}
