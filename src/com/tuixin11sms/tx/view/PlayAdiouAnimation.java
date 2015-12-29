package com.tuixin11sms.tx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.tuixin11sms.tx.R;

public class PlayAdiouAnimation extends ImageView {
	AnimationDrawable Animation_playRecordAn;

	public PlayAdiouAnimation(Context context, AttributeSet attrs) {
		super(context, attrs);

		//
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
	}

	/**
	 * 开启录音播放动画
	 * 
	 * @param wasme
	 */
	public void startAdiouPlayAn(boolean wasme) {
		if (Animation_playRecordAn != null) {
			Animation_playRecordAn.stop();
		}
		setBackgroundResource(wasme == true ? R.anim.adiou_play_right_an
				: R.anim.adiou_play_left_an);
		Animation_playRecordAn = (AnimationDrawable) getBackground();

		if (Animation_playRecordAn != null) {
			Animation_playRecordAn.setOneShot(false);
			Animation_playRecordAn.start();
		}
	}

	/**
	 * 关闭录音播放动画
	 * 
	 * @param wasme
	 */
	public void stopAdiouPlayAn(boolean wasme) {
		if (Animation_playRecordAn != null)
			Animation_playRecordAn.stop();
		setBackgroundResource(wasme == true ? R.drawable.msg_audio_play_mine_3
				: R.drawable.msg_audio_play_other_3);
	}

}
