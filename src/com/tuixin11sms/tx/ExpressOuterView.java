package com.tuixin11sms.tx;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.LinearLayout;

public class ExpressOuterView extends LinearLayout{

	public ExpressOuterView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	public float touch_x1 = -1;
	public float touch_x2 = -1;
	public boolean isIntercept;
	public boolean InterceptResult;

	@Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        int action = event.getAction();        
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        	 touch_x1 = event.getX();
        	 isIntercept = true;
        	 InterceptResult = false;
        	 break;

        case MotionEvent.ACTION_MOVE:
        	 if(isIntercept){
        		 InterceptResult = true;
        	 }
        	 break;

        case MotionEvent.ACTION_UP:
        	if(isIntercept){
	       		 InterceptResult = true;
	       	 }
        	break;

        case MotionEvent.ACTION_CANCEL:
        	 if(isIntercept){
        		 InterceptResult = true;
        	 }
        	 break;
        }
        return InterceptResult;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        switch (action) {

        case MotionEvent.ACTION_DOWN:
            break;

        case MotionEvent.ACTION_MOVE:
            break;

        case MotionEvent.ACTION_UP:
            break;

        case MotionEvent.ACTION_CANCEL:
            break;
        }
        return true;
    }

}
