package com.tuixin11sms.tx.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

public class MsgRoomMainLayout extends FrameLayout{
    public static boolean isIntercept;//是否拦截
	public MsgRoomMainLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
		//
	}

	public boolean onInterceptTouchEvent(MotionEvent ev) {//在线控录音时截获
//		System.out.println("isIntercept="+isIntercept);
		if(isIntercept) return true;
		return false;
	}
}
