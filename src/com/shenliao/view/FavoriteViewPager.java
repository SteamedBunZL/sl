package com.shenliao.view;

import com.tuixin11sms.tx.utils.Utils;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewConfiguration;

/**
 * 处理兴趣爱好页面，ViewPager里面嵌套GridView出现有时滑动ViewPager没反应的问题。
 * @author SHC
 */
public class FavoriteViewPager extends ViewPager {
	private static final String TAG = "FavoriteViewPager";

	private int mTouchSlop = 0;
	
	public FavoriteViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	public FavoriteViewPager(Context context) {
		super(context);
		mTouchSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
	}

	private float evX=0f;//手指按下时的x坐标
	boolean isIntercept = false;
	
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		isIntercept = false;
		switch (ev.getAction()) {
		case MotionEvent.ACTION_DOWN:
			evX = ev.getX();
			break;
		case MotionEvent.ACTION_MOVE:
			if(Math.abs(ev.getX()-evX)>mTouchSlop){
				//滑动距离大于系统提供的最小距离
				if(Utils.debug)Log.i(TAG, "坐标值：evX"+evX+"Math.abs(ev.getX()-evX)"+Math.abs(ev.getX()-evX)+"mTouchSlop："+mTouchSlop);
				isIntercept = true;
			}
			break;
		case MotionEvent.ACTION_UP:
			if(Math.abs(ev.getX()-evX)>mTouchSlop){
				//滑动距离大于系统提供的最小距离
				if(Utils.debug)Log.i(TAG, "坐标值：evX"+evX+"Math.abs(ev.getX()-evX)"+Math.abs(ev.getX()-evX)+"mTouchSlop："+mTouchSlop);
				isIntercept = true;
			}
			break;
		}
		if(Utils.debug)Log.i(TAG, "拦截值为："+isIntercept);
		super.onInterceptTouchEvent(ev);
		return isIntercept;
	}
}
