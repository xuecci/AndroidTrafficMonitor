package com.dreamteam.trafficmonitor;

import java.util.ArrayList;

import com.dreamteam.trafficmonitor.db.MySQLiteOpenHelper;
import com.dreamteam.trafficmonitor.customdialog.CustomAdapter;
import com.dreamteam.trafficmonitor.customdialog.CustomDialog;
import com.dreamteam.trafficmonitor.customdialog.TrafficInfo;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;

public class TrafficStatistics extends Activity {

	private ListView Statisticslist;
	private ProgressDialog progressDialog;
	private ImageButton yes;
	private ImageButton tod;
	private ImageButton sam;
	 MySQLiteOpenHelper dbHelper;
	 ArrayList<TrafficInfo> appList;
	 CustomAdapter browseAppAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.trafficstatistics);
		Statisticslist = (ListView) findViewById(R.id.Statistics);
		yes = (ImageButton)findViewById(R.id.button1);
		tod = (ImageButton)findViewById(R.id.button2);
		sam = (ImageButton)findViewById(R.id.button3);
		 yes.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				appList = dbHelper.queryByTime(TrafficStatistics.this,"yesterday");
				 browseAppAdapter = new CustomAdapter(TrafficStatistics.this, appList);
				 Statisticslist.setAdapter(browseAppAdapter);
			}
		 });
		 
		 tod.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 appList = dbHelper.queryByTime(TrafficStatistics.this,"today");
					 browseAppAdapter = new CustomAdapter(TrafficStatistics.this, appList);
					 Statisticslist.setAdapter(browseAppAdapter);
				}		 
			 });
		 
		 sam.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					 appList = dbHelper.queryByTime(TrafficStatistics.this,"sameMonth");
					 browseAppAdapter = new CustomAdapter(TrafficStatistics.this, appList);
					 Statisticslist.setAdapter(browseAppAdapter);
				}
			 });
		progressDialog = ProgressDialog.show(TrafficStatistics.this, "请稍等", "程序正在拼命加载...", true);
	      // 开始动态加载线程
	    mThreadLoadApps.start();
	}
	
	private Thread mThreadLoadApps = new Thread(){ 
        @Override 
        public void run() {

    		dbHelper = new MySQLiteOpenHelper(TrafficStatistics.this);
        	appList = dbHelper.queryByTime(TrafficStatistics.this,"today");
    		
    		browseAppAdapter = new CustomAdapter(
    				TrafficStatistics.this, appList);
            hander.sendEmptyMessage(0); // 下载完成后发送处理消息
            progressDialog.dismiss();;   // 关闭进度条对话框
        }
 };
 private Handler hander = new Handler(){
     @Override
     public void handleMessage(Message msg) {
         switch(msg.what){
         case 0:
        	 browseAppAdapter.notifyDataSetChanged(); //发送消息通知ListView更新
        	 Statisticslist.setAdapter(browseAppAdapter); // 重新设置ListView的数据适配器
             break;
         default:
             //do something
             break;
         }
     }
 };
}
