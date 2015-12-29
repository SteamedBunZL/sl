package com.tuixin11sms.tx.activity;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.animation.Animator.AnimatorListener;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 注册成功页面
 * @author SHC
 *
 */
public class RegistSucceedActivity extends BaseActivity {

	private static final String TAG = "RegistSucceedActivity";
	private TextView btnStart;
	private TextView txtNumber;
	private ImageView imgAvatar;
	private TextView  nickName;
	private TextView  tv_regist_success_prompt;
	private RelativeLayout  rl_title_regist_success;//标题栏

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.regist_succeed);
		initViews();
	}

	private void initViews() {
		
		tv_regist_success_prompt = (TextView) findViewById(R.id.tv_regist_success_prompt);
		rl_title_regist_success = (RelativeLayout) findViewById(R.id.rl_title_regist_success);
		btnStart = (TextView) findViewById(R.id.start);
		txtNumber = (TextView) findViewById(R.id.sl_number);
		imgAvatar = (ImageView) findViewById(R.id.iv_avatar);
		nickName=(TextView) findViewById(R.id.regist_successed_nickName);
		btnStart.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startMainTab();
				
			}
		});
		nickName.setText(TX.tm.getTxMe().getNick_name());
		txtNumber
				.setText(TX.tm.getTxMe().partner_id+"");
		if (mSess.getSmallAvatar() != null) {
			imgAvatar.setImageBitmap(Utils
					.getRoundedCornerBitmap(mSess.getSmallAvatar()));
		}else{
			if(TX.tm.getTxMe().getSex()==TX.MALE_SEX){
				imgAvatar.setImageResource(R.drawable.user_infor_head_boy);
			}else{
				imgAvatar.setImageResource(R.drawable.user_infor_head_girl);
			}
		}
	}

	
	
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && tv_regist_success_prompt != null&&rl_title_regist_success!=null) {// 界面全部加在完毕

			if(Utils.debug)Log.i(TAG, "标题高度："+rl_title_regist_success.getHeight());
			animate(tv_regist_success_prompt).x(0).y(rl_title_regist_success.getHeight()).setDuration(1000).setListener(new AnimatorListener() {
				
				@Override
				public void onAnimationStart(Animator animation) {
					
				}
				
				@Override
				public void onAnimationRepeat(Animator animation) {
					
				}
				
				@Override
				public void onAnimationEnd(Animator animation) {
					animate(tv_regist_success_prompt).x(0).y(0).setDuration(1000).setStartDelay(1000);
					
				}
				
				@Override
				public void onAnimationCancel(Animator animation) {
					
				}
			});

		}
		super.onWindowFocusChanged(hasFocus);
	}
	
	
	@Override
	public void onBackPressed() {
		startMainTab();
	}
	
	
	private void startMainTab() {
		Intent intent = new Intent(RegistSucceedActivity.this, TuiXinMainTab.class);
		startActivity(intent);
		SessionManager.getInstance().setSmallAvatar(null);
		finish();
	}
	

}
