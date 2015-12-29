package com.tuixin11sms.tx.activity;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.Window;
import android.widget.ListView;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class SettingsPreference extends PreferenceActivity {
	private static final String TAG = SettingsPreference.class.getSimpleName();

	private static final int PASSWORD_SETTING_SUCCESS = 50;
	private static final int PASSWORD_FORMAT_ERROR = 51;
	private static final int PASSWORD_REQUEST_TIME_LIMIT = 52;
	private static final int PASSWORD_REQUEST_FAIL = 53;

	public static final String TUIXIN_SETTING = "com.tuixin11sms.tx_preferences";
	String settings_key;
	String login_key;
	String about_key;
	String count_key;
	String login_username_key;
	private EditTextPreference EditTextPreference_password;
	private UpdateReceiver updatereceiver;

	// private SharedPreferences prefs = null;

	/*
	 * 
	 * 1.DEFAULT_FEATURES：系统默认状态，一般不需要指定
	 * 2.FEATURE_CONTEXT_MENU：启用ContextMenu，默认该项已启用，一般无需指定
	 * 3.FEATURE_CUSTOM_TITLE：自定义标题。当需要自定义标题时必须指定。如：标题是一个按钮时
	 * 4.FEATURE_INDETERMINATE_PROGRESS：不确定的进度 5.FEATURE_LEFT_ICON：标题栏左侧的图标
	 * 6.FEATURE_NO_TITLE：吴标题 7.FEATURE_OPTIONS_PANEL：启用“选项面板”功能，默认已启用。
	 * 8.FEATURE_PROGRESS：进度指示器功能 9.FEATURE_RIGHT_ICON:标题栏右侧的图标
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
				R.layout.sll_alert_title);

		addPreferencesFromResource(R.xml.activity_settings_preference);
		ListView localListView = getListView();
		localListView.setDivider(null);
		localListView.setCacheColorHint(0);
		int i1 = Color.alpha(0);
		localListView.setBackgroundColor(i1);
		/*
		 * login_bind_password_key
		 * =getResources().getString(R.string.pref_key_login_bind_password);
		 * 
		 * EditTextPreference_password =
		 * (EditTextPreference)findPreference(login_bind_password_key);
		 * EditTextPreference_password.setOnPreferenceChangeListener(this);
		 * EditTextPreference_password
		 * .getEditText().setTransformationMethod(PasswordTransformationMethod
		 * .getInstance());
		 * EditTextPreference_password.getEditText().setSingleLine();
		 * EditTextPreference_password.setPersistent(false);
		 */

		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
	}

	@Override
	protected void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			this.registerReceiver(updatereceiver, filter);
		}
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
	}

	/*
	 * @Override public boolean onPreferenceClick(Preference preference) { //
	 * //判断是哪个Preference被点击了 BLog.i("TAG", "setingspreference is clicked");
	 * BLog.i("TAG", preference.getKey()); return false; }
	 */
	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen,
			Preference preference) {
		return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	/*
	 * @Override public boolean onPreferenceChange(Preference preference, Object
	 * newValue) { BLog.i("TAG", "preference is Change"); BLog.i("TAG",
	 * preference.getKey());
	 * if(preference.getKey().equals(login_bind_password_key)){ BLog.i("TAG",
	 * "点击设置电脑管家"); int code = prefs.getInt(CommonData.TEL_BIND_STATE,
	 * CommonData.TEL_BIND_INIT_CODE); if(code != CommonData.TEL_BIND_SUCCESS){
	 * Toast.makeText(SettingsPreference.this, R.string.tel_not_verified_prompt,
	 * 1).show(); return false; }
	 * 
	 * long current_time = System.currentTimeMillis(); long last_click_time =
	 * prefs.getLong(CommonData.PASSWORD_CHECK_CLICK_TIME, 0); long time_ =
	 * current_time - last_click_time;
	 * if(needcheckchargepassword&&time_<=60000){
	 * startPromptDialog(R.string.prompt, R.string.check_tel_fast);
	 * needcheckchargepassword=false; return false; }
	 * 
	 * if(editor==null){ editor = prefs.edit(); }
	 * editor.putLong(CommonData.PASSWORD_CHECK_CLICK_TIME, current_time);
	 * editor.commit(); String password =
	 * EditTextPreference_password.getEditText().getEditableText().toString();
	 * if(password == null || "".equals(password) || password.length() < 1){
	 * Toast.makeText(SettingsPreference.this, R.string.pw_needed_prompt,
	 * 1).show(); return false; } int space_index = 0; space_index =
	 * password.indexOf(" "); if(space_index != -1){
	 * Toast.makeText(SettingsPreference.this, R.string.pw_has_space_prompt,
	 * 1).show(); return false; } if(password.length() < 6){
	 * Toast.makeText(SettingsPreference.this, R.string.pw_min_length_prompt,
	 * 1).show(); return false; }else if(password.length() > 20){
	 * Toast.makeText(SettingsPreference.this, R.string.pw_max_length_prompt,
	 * 1).show(); return false; } if(progress==null){ progress = new
	 * ProgressDialog(SettingsPreference.this); } String pw_is_uploading_prompt
	 * = SettingsPreference.this.getString(R.string.pw_is_uploading_prompt);
	 * progress.setMessage(pw_is_uploading_prompt); progress.show();
	 * SocketHelper.getSocketHelper(txdata).sendPassword(password);
	 * 
	 * if(outtime!=null){ try{ outtime.cancel(); }catch(Exception e){
	 * 
	 * } outtime=null; } outtime = new Timer(); outtime.schedule(new TimerTask()
	 * { public void run() { progress.dismiss(); Message msg = new Message();
	 * msg.what = PASSWORD_REQUEST_FAIL; handler.sendMessage(msg); } }, 30000);
	 * needcheckchargepassword=true; return false; } return true; }
	 */
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case PASSWORD_SETTING_SUCCESS:
				Toast.makeText(SettingsPreference.this,
						R.string.pw_update_success_prompt, 1).show();
				break;
			case PASSWORD_FORMAT_ERROR:
				Toast.makeText(SettingsPreference.this,
						R.string.pw_format_error_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_TIME_LIMIT:
				Toast.makeText(SettingsPreference.this,
						R.string.pw_request_time_limit_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_FAIL:
				Toast.makeText(SettingsPreference.this,
						R.string.pw_request_failed_prompt, 1).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = intent.getStringExtra("msg");
			if (msg.trim().length() != 0) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// Looper.prepare();
						if (Utils.debug)
							Log.w(TAG, "msg:" + msg);
						dealMsg(msg);
						// Looper.loop();
					}
				});
				// Utils.executorService.submit(new Runnable() {
				// @Override
				// public void run() {
				// // Looper.prepare();
				// BLog.w(TAG, "msg:"+msg);
				// dealMsg(msg);
				// // Looper.loop();
				// }
				// });
			}
			// chattxt2.append(msg + "\n");
		}
	}

	public void dealMsg(String msg) {
		if (msg.startsWith("{")) {
			JSONObject jo = null;
			try {
				jo = new JSONObject(msg);
			} catch (JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
			if (jo != null) {
				int type = 0;
				int d;
				try {
					type = jo.getInt("mt");
					if (Utils.debug)
						Log.i(TAG, "type:" + type);
					switch (type) {
					case MsgInfor.SERVER_CHARGE_PASSWORDGET:
						d = jo.getInt("d");
						if (0 == d) {
							// prefs.edit().putString(CommonData.PASSWORD,
							// EditTextPreference_password.getEditText().getEditableText().toString()).commit();
							// prefs.edit().putString(CommonData.INPUTPASSWORD,
							// EditTextPreference_password.getEditText().getEditableText().toString()).commit();
							// TX.tm.reloadTXMe();///////

							// 修改密码成功，更新存储的密码
							try {
								SessionManager mSess = SessionManager
										.getInstance();
								mSess.mUserLoginInfor.updateUserPwd(
										"" + TX.tm.getUserID(),
										EditTextPreference_password
												.getEditText()
												.getEditableText().toString()
												.trim());
							} catch (JSONException e) {
								if (Utils.debug)
									Log.e(TAG, "更新密码异常", e);
							}

							SessionManager.getInstance()
									.changePassword(
											EditTextPreference_password
													.getEditText()
													.getEditableText()
													.toString());
							Message message = new Message();
							message.what = PASSWORD_SETTING_SUCCESS;
							handler.sendMessage(message);
						} else if (1 == d) {
							Message message = new Message();
							message.what = PASSWORD_FORMAT_ERROR;
							handler.sendMessage(message);
						} else if (3 == d) {
							Message message = new Message();
							message.what = PASSWORD_REQUEST_TIME_LIMIT;
							handler.sendMessage(message);
						} else if (5 == d) {
							Message message = new Message();
							message.what = PASSWORD_REQUEST_FAIL;
							handler.sendMessage(message);
						}
						break;
					}// end switch
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
			}
		} else {
			String illegalInfor = SettingsPreference.this.getResources()
					.getString(R.string.illegal_information);
			String message = illegalInfor + msg;
			Toast.makeText(SettingsPreference.this, message, 1).show();
		}
	}

}
