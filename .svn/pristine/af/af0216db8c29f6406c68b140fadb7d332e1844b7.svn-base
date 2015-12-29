package com.tuixin11sms.tx.view;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

public class ReceiveGreatAnim {

	private ImageView iv;
	private ImageView iv1;
	private ImageView iv2;
	private ImageView iv3;
	private ImageView iv4;
	private ImageView iv5;
	private ImageView iv6;
	private ImageView iv7;
	private ImageView iv8;
	private Animation anim_big;
	private Animation anim2;
	private Animation anim3;
	private Animation anim4;
	private Animation anim5;
	private Animation anim6;
	private Animation anim7;
	private Animation anim8;
	private Animation anim9;
	private AnimationSet set1;
	private AnimationSet set2;
	private AnimationSet set3;
	private AnimationSet set4;
	private AnimationSet set5;
	private AnimationSet set6;
	private AnimationSet set7;
	private AnimationSet set8;
	private Animation anim_small;
	private AnimationSet set_big;
	private AnimationSet set_move;
	private Animation anim_move;
	private Animation anim_movesmall;

	private int[] locations;
	private int[] location2s;
	private ImageView ivbg;
	private ImageView ivbg2;
	private AnimationSet set_light;
	private Animation anim_alhp2;
	private Animation alanim_end;
	private ImageView iv_aminend;
	private List<ImageView> ivs;

	public ReceiveGreatAnim(List<ImageView> ivs,int[] locations,int[] location2s) {
		this.ivs = ivs;
		this.locations = locations;
		this.location2s = location2s;
		init();
		init_big();
		init_bang();
		init_move();
		init_light();
		iv_aminend.setVisibility(View.GONE);
	}

	private void init() {
		iv = ivs.get(0);
		ivbg = ivs.get(9);
		iv1 = ivs.get(1);
		iv2 = ivs.get(2);
		iv3 = ivs.get(3);
		iv4 = ivs.get(4);
		iv5 = ivs.get(5);
		iv6 = ivs.get(6);
		iv7 = ivs.get(7);
		iv8 = ivs.get(8);
		ivbg2 = ivs.get(10);
		iv_aminend = ivs.get(11);
	}
	
	public void start(){
		iv.setVisibility(View.GONE);
		iv.startAnimation(anim_big);

		// iv.startAnimation(set_move);
		// iv.startAnimation(anim_movesmall);

		// iv.startAnimation(set_move);
		// iv.startAnimation(anim_move);

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
		anim_small.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

				// iv.startAnimation(set_move);
				ivbg.setVisibility(View.VISIBLE);
				ivbg.startAnimation(set_light);
			}
		});
		set_light.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {

//				ivbg.setVisibility(View.GONE);
				ivbg2.setVisibility(View.VISIBLE);
				ivbg2.startAnimation(anim_alhp2);
				// iv.startAnimation(set_move);
			}
		});
		anim_alhp2.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
				ivbg.setVisibility(View.GONE);
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
//				ivbg2.setVisibility(View.GONE);
				iv.startAnimation(set_move);
			}
		});

		set_move.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				iv1.setVisibility(View.VISIBLE);
				iv2.setVisibility(View.VISIBLE);
				iv3.setVisibility(View.VISIBLE);
				iv4.setVisibility(View.VISIBLE);
				iv5.setVisibility(View.VISIBLE);
				iv6.setVisibility(View.VISIBLE);
				iv7.setVisibility(View.VISIBLE);
				iv8.setVisibility(View.VISIBLE);
				iv_aminend.setVisibility(View.VISIBLE);
				iv1.startAnimation(set1);
				iv2.startAnimation(set2);
				iv3.startAnimation(set3);
				iv4.startAnimation(set4);
				iv5.startAnimation(set5);
				iv6.startAnimation(set6);
				iv7.startAnimation(set7);
				iv8.startAnimation(set8);
				
				iv_aminend.startAnimation(alanim_end);
				
			}
		});
		set8.setAnimationListener(new AnimationListener() {

			@Override
			public void onAnimationStart(Animation animation) {
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
			}

			@Override
			public void onAnimationEnd(Animation animation) {
				
				for (ImageView iv : ivs) {
					iv.setVisibility(View.GONE);
				}
				
			}
		});
	}

	private void init_move() {
		

		int toX = locations[0] - location2s[0];
		int toY = location2s[1] - locations[1];
		
		anim_move = new TranslateAnimation(0, toX, 0, -toY);
		anim_move.setDuration(500);
		anim_move.setFillAfter(true);
		anim_movesmall = new ScaleAnimation(1.0f, 0.2f, 1.0f, 0.2f);
		anim_movesmall.setDuration(500);
		anim_movesmall.setFillAfter(false);
		set_move = new AnimationSet(true);
		set_move.addAnimation(anim_movesmall);
		// set_move.setFillAfter(true);
		set_move.addAnimation(anim_move);
		set_move.setDuration(500);
	}

	private void init_big() {
		anim_big = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim_big.setDuration(500);
		anim_big.setFillAfter(true);

		anim_small = new ScaleAnimation(1.0f, 0.9f, 1.0f, 0.9f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		anim_small.setDuration(500);
		anim_small.setFillAfter(true);

		alanim_end = new AlphaAnimation(1.0f, 0.0f);
		alanim_end.setDuration(500);
		alanim_end.setFillAfter(true);
	}

	private void init_bang() {
		anim2 = new TranslateAnimation(0, 80f, 0, -80f);
		anim3 = new TranslateAnimation(0, 20, 0, 80f);
		anim4 = new TranslateAnimation(0, 60, 0, 60);
		anim5 = new TranslateAnimation(0, -55, 0, -55);
		anim6 = new TranslateAnimation(0, 70, 0, 0);
		anim7 = new TranslateAnimation(0, 30, 0, -45);
		anim8 = new TranslateAnimation(0, -20, 0, 65);
		anim9 = new TranslateAnimation(0, -85, 0, 30);

		Animation alanim = new AlphaAnimation(1.0f, 0.0f);
		ScaleAnimation anim_b1 = new ScaleAnimation(0.0f, 1.0f, 0.0f, 1.0f);
		ScaleAnimation anim_b2 = new ScaleAnimation(0.0f, 0.2f, 0.0f, 0.2f);
		ScaleAnimation anim_b3 = new ScaleAnimation(0.0f, 0.5f, 0.0f, 0.5f);

		set1 = new AnimationSet(true);
		set1.addAnimation(anim2);
		set1.addAnimation(alanim);
		set1.setFillAfter(true);
		set1.addAnimation(anim_b1);
		set1.setDuration(500);
		set1.setStartTime(500);

		// Interpolator inter = new DecelerateInterpolator();
		// set1.setInterpolator(inter);
		set2 = new AnimationSet(true);
		set2.addAnimation(anim3);
		set2.addAnimation(alanim);
		set2.setFillAfter(true);
		set2.addAnimation(anim_b2);
		set2.setDuration(500);
		set2.setStartTime(500);

		set3 = new AnimationSet(true);
		set3.addAnimation(anim4);
		set3.addAnimation(alanim);
		set3.setFillAfter(true);
		set3.addAnimation(anim_b1);
		set3.setDuration(500);
		set3.setStartTime(500);

		set4 = new AnimationSet(true);
		set4.addAnimation(anim5);
		set4.addAnimation(alanim);
		set4.setFillAfter(true);
		set4.addAnimation(anim_b1);
		set4.setDuration(500);
		set4.setStartTime(500);

		set5 = new AnimationSet(true);
		set5.addAnimation(anim6);
		set5.addAnimation(alanim);
		set5.setFillAfter(true);
		set5.addAnimation(anim_b2);
		set5.setDuration(500);
		set5.setStartTime(500);
		
		set6 = new AnimationSet(true);
		set6.addAnimation(anim7);
		set6.addAnimation(alanim);
		set6.setFillAfter(true);
		set6.addAnimation(anim_b3);
		set6.setDuration(500);
		set6.setStartTime(500);
		
		set7 = new AnimationSet(true);
		set7.addAnimation(anim8);
		set7.addAnimation(alanim);
		set7.setFillAfter(true);
		set7.addAnimation(anim_b1);
		set7.setDuration(500);
		set7.setStartTime(500);
		
		set8 = new AnimationSet(true);
		set8.addAnimation(anim9);
		set8.addAnimation(alanim);
		set8.setFillAfter(true);
		set8.addAnimation(anim_b3);
		set8.setDuration(500);
		set8.setStartTime(500);
	}

	public void init_light() {
		Animation anim_alhp = new AlphaAnimation(0.1f, 1.0f);

		Animation rotateAnimation = new RotateAnimation(0f, 90f,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		set_light = new AnimationSet(true);
		set_light.addAnimation(anim_alhp);
		set_light.addAnimation(rotateAnimation);
		set_light.setFillAfter(false);
		set_light.setDuration(500);

		anim_alhp2 = new AlphaAnimation(1.0f, 0.0f);
		anim_alhp2.setFillAfter(true);
		anim_alhp2.setDuration(500);
	}

}
