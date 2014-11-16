package com.dreamteam.trafficmonitor;

import java.util.Timer;
import java.util.TimerTask;

import com.dreamteam.trafficmonitor.control.TrafficControl;
import com.dreamteam.trafficmonitor.customdialog.AnimationTabHost;

import android.app.ActivityGroup;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends ActivityGroup {

	final int RIGHT = 0;
	final int LEFT = 1;
	AnimationTabHost tabHost;
	private GestureDetector gestureDetector;

	private static Boolean isExit = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);// 取消标题栏
		setContentView(R.layout.activity_main);

		tabHost = (AnimationTabHost) findViewById(R.id.tabhost);
		tabHost.setup(this.getLocalActivityManager());

		Intent intent;
		intent = new Intent(this, TrafficControl.class);
		tabHost.addTab(tabHost
				.newTabSpec("TrafficeControl")
				.setIndicator(
						null,
						getResources().getDrawable(
								R.drawable.main_toolbar_firewall_pressed))
				.setContent(intent));

		intent = new Intent(this, TrafficDisplay.class);
		tabHost.addTab(tabHost
				.newTabSpec("TrafficDisplay")
				.setIndicator(
						null,
						getResources()
								.getDrawable(
										R.drawable.main_toolbar_networkassistant_pressed))
				.setContent(intent));

		intent = new Intent(this, TrafficStatistics.class);
		tabHost.addTab(tabHost
				.newTabSpec("TrafficStatistics")
				.setIndicator(
						null,
						getResources().getDrawable(
								R.drawable.main_toolbar_statistic_pressed))
				.setContent(intent));

		tabHost.setCurrentTab(1);
		updateTab(tabHost);
		tabHost.setOpenAnimation(true);

		gestureDetector = new GestureDetector(MainActivity.this,
				onGestureListener);

		tabHost.setOnTabChangedListener(new OnTabChangeListener() {

			@Override
			public void onTabChanged(String arg0) {
				// TODO Auto-generated method stub
				updateTab(tabHost);
			}
		});
	}

	private GestureDetector.OnGestureListener onGestureListener = new GestureDetector.SimpleOnGestureListener() {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			float x = e2.getX() - e1.getX();
			float y = e2.getY() - e1.getY();

			if (x > 150) {
				doResult(RIGHT);
			} else if (x < -150) {
				doResult(LEFT);
			}
			return true;
		}
	};

	public boolean onTouchEvent(MotionEvent event) {
		return gestureDetector.onTouchEvent(event);
	}

	/**
	 * 事件分发，解决TabHost里有Listview无法实现左右滑动的Bug
	 */
	public boolean dispatchTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event)) {
			event.setAction(MotionEvent.ACTION_CANCEL);
		}
		return super.dispatchTouchEvent(event);
	}

	public void doResult(int action) {

		switch (action) {
		case RIGHT:
			if (tabHost.getCurrentTab() > 0) {
				tabHost.setCurrentTab(tabHost.getCurrentTab() - 1);
			}
			break;

		case LEFT:
			if (tabHost.getCurrentTab() < 2) {
				tabHost.setCurrentTab(tabHost.getCurrentTab() + 1);
			}
			break;
		}
	}

	private void updateTab(AnimationTabHost tabHost) {
		for (int i = 0; i < tabHost.getTabWidget().getChildCount(); i++) {
			View view = tabHost.getTabWidget().getChildAt(i);
			TextView tv = (TextView) tabHost.getTabWidget().getChildAt(i)
					.findViewById(android.R.id.title);
			tv.setTextSize(16);
			tv.setTypeface(Typeface.SERIF, 2); // 设置字体和风格
			if (tabHost.getCurrentTab() == i) {// 选中
				tv.setTextColor(this.getResources().getColorStateList(
						android.R.color.darker_gray));
			} else {// 不选中
				tv.setTextColor(this.getResources().getColorStateList(
						android.R.color.white));
			}
		}
	}
}
