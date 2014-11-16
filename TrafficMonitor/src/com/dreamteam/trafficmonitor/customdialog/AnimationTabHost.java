package com.dreamteam.trafficmonitor.customdialog;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TabHost;

import com.dreamteam.trafficmonitor.R;

public class AnimationTabHost extends TabHost {
	
	/* @author Ryan Mo
	 * 
	 * Tabhost滑动效果
	 * 
	 */
	private Animation slideLeftIn;
	private Animation slideLeftOut;
	private Animation slideRightIn;
	private Animation slideRightOut;

	/** 记录是否打开动画效果 */
	private boolean isOpenAnimation;
	/** 记录当前标签页的总数 */
	private int mTabCount;

	public AnimationTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);

		slideLeftIn = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_in);
		slideLeftOut = AnimationUtils.loadAnimation(context,
				R.anim.slide_left_out);
		slideRightIn = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_in);
		slideRightOut = AnimationUtils.loadAnimation(context,
				R.anim.slide_right_out);

		isOpenAnimation = false;
	}

	/**
	 * 设置是否打开动画效果
	 * 
	 * @param isOpenAnimation
	 *            true：打开
	 */
	public void setOpenAnimation(boolean isOpenAnimation) {
		this.isOpenAnimation = isOpenAnimation;
	}

	/**
	 * @return 返回当前标签页的总数
	 */
	public int getTabCount() {
		return mTabCount;
	}

	@Override
	public void addTab(TabSpec tabSpec) {
		mTabCount++;
		super.addTab(tabSpec);
	}

	@Override
	public void setCurrentTab(int index) {
		int mCurrentTabID = getCurrentTab();//1,0

		if (null != getCurrentView()) {
			// 第一次设置 Tab 时，该值为 null。
			if (isOpenAnimation) {
				if (index > mCurrentTabID) {
					getCurrentView().startAnimation(slideLeftOut);
					Log.i("demo","slideLeftOut");
				} else if (index < mCurrentTabID) {
					getCurrentView().startAnimation(slideRightOut);
					Log.i("demo","slideRightOut");
				}
			}

			super.setCurrentTab(index);

			if (isOpenAnimation) {
				if (index > mCurrentTabID) {
					getCurrentView().startAnimation(slideLeftIn);
					Log.i("demo","slideLeftIn");
				} else if (index < mCurrentTabID) {
					getCurrentView().startAnimation(slideRightIn);
					Log.i("demo","slideRightIn");
				}
			}
		}
		else {
			super.setCurrentTab(index);
		}
	}
}
