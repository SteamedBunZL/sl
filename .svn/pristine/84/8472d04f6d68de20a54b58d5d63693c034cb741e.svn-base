package com.shenliao.set.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.PhoneBindActivity;
import com.umeng.analytics.MobclickAgent;

/**
 * 绑定管理
 * 
 * @author xch
 * 
 */
public class SetBindManageActivity extends BaseActivity implements OnClickListener {
	private String tel;
	private boolean tel_bind_state;
	private boolean email_bind_state;
//	private SharedPreferences prefs = null;
//	private Editor editor;
	private View mSetPhoneChecked;
	private View mSetPhoneUnChecked;
	private View mEmailChecked;
	private View mEmailUnChecked;
	private TextView mSettingPhone;
	private TextView mSettingEmail;
	private LinearLayout mSetPhoneLinear;
	private LinearLayout mSetEmailLinear;
	private LinearLayout  backBtn;//返回按钮

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_bindmanage);
		init();
	}

	private void init() {
		mSetEmailLinear = (LinearLayout) findViewById(R.id.sl_tab_setting_bindmanage_email);
		mSetPhoneLinear = (LinearLayout) findViewById(R.id.sl_tab_setting_bindmanage_phone);
		mSettingPhone = (TextView) findViewById(R.id.sl_mSettingPhone);
		mSetPhoneChecked = findViewById(R.id.sl_mSetPhoneChecked);
		mSetPhoneUnChecked = findViewById(R.id.sl_mSetPhoneUnChecked);
		mSettingEmail = (TextView) findViewById(R.id.sl_mSettingEmail);
		mEmailChecked = findViewById(R.id.sl_mSetEmailChecked);
		mEmailUnChecked = findViewById(R.id.sl_mSetEmailUnChecked);
		backBtn=(LinearLayout) findViewById(R.id.btn_back_left);

//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
//		tel_bind_state = prefs.getBoolean(CommonData.IS_BIND_PHONE, false);
//		email_bind_state = prefs.getBoolean(CommonData.IS_BIND_EMAIL, false);
		tel_bind_state = mSess.mPrefMeme.is_bind_phone.getVal();
		email_bind_state = mSess.mPrefMeme.is_bind_email.getVal();
		
		backBtn.setOnClickListener(this);
		if (!tel_bind_state) {
			mSetPhoneLinear.setOnClickListener(this);
		}
		if(!email_bind_state){
			mSetEmailLinear.setOnClickListener(this);
		}
	}

	// 判断邮箱是否绑定
	private void emailSummaryInit() {
		if (email_bind_state) {
			mEmailChecked.setVisibility(View.VISIBLE);
			mEmailUnChecked.setVisibility(View.GONE);
		} else {
			mEmailChecked.setVisibility(View.GONE);
			mEmailUnChecked.setVisibility(View.VISIBLE);
		}
//		showEmail(prefs.getString(CommonData.EMAIL, ""));
		showEmail(mSess.mPrefMeme.email.getVal());
	}

	// 判断电话是否绑定
	private void telSummaryInit() {
//		tel = prefs.getString(CommonData.TELEPHONE, "");
		tel = mSess.mPrefMeme.telephone.getVal();
		showTel(tel);
		if (tel_bind_state) {
			// 已绑定
			mSetPhoneUnChecked.setVisibility(View.GONE);
			mSetPhoneChecked.setVisibility(View.VISIBLE);
		} else {
			mSetPhoneUnChecked.setVisibility(View.VISIBLE);
			mSetPhoneChecked.setVisibility(View.GONE);
		}
	}

	// 显示邮件地址
	private void showEmail(String email) {
		if (email == null || "".equals(email) || !email_bind_state) {
			mSettingEmail.setText("");
		} else {
			mSettingEmail.setText(email);
		}

	}

	// 显示电话号
	private void showTel(String phone) {
		if (phone == null || "".equals(phone) || !tel_bind_state) {
			mSettingPhone.setText("");
		} else {
			if (phone.length() >= 11) {
				String s1 = phone.substring(0, 3);
				String s2 = phone.substring(phone.length() - 4, phone.length());
				mSettingPhone.setText(s1 + "****" + s2);
			}
		}

	}

	@Override
	protected void onResume() {
		telSummaryInit();
		emailSummaryInit();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// 点击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 手机点击事件
		case R.id.sl_tab_setting_bindmanage_phone:
			Intent phoneIntent = new Intent(SetBindManageActivity.this, PhoneBindActivity.class);
			startActivity(phoneIntent);
			SetBindManageActivity.this.finish();
			break;
		// 邮箱点击事件
		case R.id.sl_tab_setting_bindmanage_email:
			Intent emailIntent = new Intent(SetBindManageActivity.this, SetEmailBindActivity.class);
			startActivity(emailIntent);
			SetBindManageActivity.this.finish();
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
