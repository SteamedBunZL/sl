package com.tuixin11sms.tx.view;

import com.tuixin11sms.tx.R;

import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;

public class PraisedAnim {

	private ImageView iv;
	private Animation anim_big;
	private Animation anim_small;


	public PraisedAnim(ImageView iv) {
		this.iv = iv;
		init_big();
	}

	
	public void start(){
		iv.setImageResource(R.drawable.state_praised);
		iv.startAnimation(anim_big);

		anim_big.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				iv.startAnimation(anim_small);
			}
		});
	}

	private void init_big() {
		anim_big = new ScaleAnimation(0.0f, 1.2f, 0.0f, 1.2f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim_big.setDuration(200);
		anim_big.setFillAfter(true);

		anim_small = new ScaleAnimation(1.2f, 1.0f, 1.2f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim_small.setDuration(160);
	}



}
