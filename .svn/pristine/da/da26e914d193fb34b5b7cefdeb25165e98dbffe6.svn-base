package com.shenliao.set.activity;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.InviteContactsActivity;
import com.tuixin11sms.tx.contact.TX;
import com.umeng.analytics.MobclickAgent;

/**
 * 邀请好友
 * 
 * @author xch
 * 
 */
public class SetUpdateInviteFriendActivity extends BaseActivity implements OnClickListener {
	private LinearLayout invitedPhone;// 通过短信邀请好友
	private LinearLayout invitedEmail;// 通过邮件邀请好友
	private LinearLayout backBtn;//返回按钮
//	private SharedPreferences prefs;
//	private Editor editor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_update_invitefriend);
		init();
	}

	// 初始化
	private void init() {
		invitedPhone = (LinearLayout) findViewById(R.id.sl_tab_setting_invitedfriend_phone);
		invitedEmail = (LinearLayout) findViewById(R.id.sl_tab_setting_invitedfriend_email);
        backBtn=(LinearLayout) findViewById(R.id.btn_back_left);
		
        backBtn.setOnClickListener(this);
        invitedPhone.setOnClickListener(this);
		invitedEmail.setOnClickListener(this);
		

//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();

	}
	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		// 通过短信邀请好友
		case R.id.sl_tab_setting_invitedfriend_phone:
			Intent intent = new Intent(SetUpdateInviteFriendActivity.this, InviteContactsActivity.class);
			startActivity(intent);

			break;
		// 通过邮件邀请好友
		case R.id.sl_tab_setting_invitedfriend_email:
//			if (prefs == null) {
//				prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE
//						+ Context.MODE_WORLD_WRITEABLE);
//			}
//			String realname = prefs.getString(CommonData.REALNAME, "");
			String realname = mSess.mPrefMeme.realname.getVal();
			String feedbackSubject = "";
			if (!"".equals(realname)) {
				feedbackSubject = SetUpdateInviteFriendActivity.this.getResources().getString(
						R.string.sl_settings_feedback_subject);
				feedbackSubject = feedbackSubject.replace("$", realname);
			} else {
				feedbackSubject = SetUpdateInviteFriendActivity.this.getResources().getString(R.string.invited);
			}

			String text = String.format(getString(R.string.email_invite_text), TX.tm.getTxMe().partner_id);
			Intent feedbackIntent = new Intent(Intent.ACTION_SEND);
			feedbackIntent.setType("application/*");
			feedbackIntent.putExtra(Intent.EXTRA_SUBJECT, feedbackSubject);
			feedbackIntent.putExtra(Intent.EXTRA_TEXT, text);
			startActivity(Intent.createChooser(feedbackIntent,
					this.getApplication().getText(R.string.invited_choose_send)));// 调用系统的mail客户端进行发送

			break;
			//返回按钮点击事件
		case R.id.btn_back_left:
			finish();
			break;
		default:
			break;
		}

	}
}
