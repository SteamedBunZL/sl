package com.tuixin11sms.tx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.tuixin11sms.tx.R;

public class WaitAdiouAnimation extends RelativeLayout{
	AnimationDrawable Animation_playRecordAn;
	public WaitAdiouAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);
		
		
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}
	
	public void startAdiouPlayAn(boolean wasme){
		
	    if(Animation_playRecordAn == null){
			this.setBackgroundResource(wasme==true?R.anim.adiou_wait_right_an:R.anim.adiou_wait_left_an);
			Animation_playRecordAn = (AnimationDrawable)getBackground();
			Animation_playRecordAn.setOneShot(false);
	    }
		if(Animation_playRecordAn != null&&!Animation_playRecordAn.isRunning()){
   		    Animation_playRecordAn.start();
   	     }else{
   	    	this.setBackgroundResource(wasme==true?R.anim.adiou_wait_right_an:R.anim.adiou_wait_left_an);
   	     }
		
	}
	public void stopAdiouPlayAn(boolean wasme){
		
		 if(Animation_playRecordAn != null){
		   setBackgroundResource(wasme==true?R.drawable.msg_bg_mine:R.drawable.msg_bg_other);
      	   Animation_playRecordAn.stop();
      	   Animation_playRecordAn = null;
         }
	}
}
