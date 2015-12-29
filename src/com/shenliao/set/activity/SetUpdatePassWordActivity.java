package com.shenliao.set.activity;

import org.json.JSONException;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 密码修改
 * 
 * @author xch
 * 
 */
public class SetUpdatePassWordActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "SetUpdatePassWordActivity";
	private EditText oldPassWordEdit;// 旧密码
	private EditText newPassWrodEdit;// 新密码
	private EditText againPassWrodEdit;// 重复输入密码
	private Button submitBtn;// 提交按钮
	// private SharedPreferences prefs;// 配置参数
	// private Editor editor;
	private boolean needcheckchargepassword = true;
	private static final int PASSWORD_SETTING_SUCCESS = 0;
	private static final int PASSWORD_FORMAT_ERROR = 1;
	private static final int PASSWORD_REQUEST_TIME_LIMIT = 2;
	private static final int PASSWORD_REQUEST_FAIL = 3;
	private UpdateReceiver updatereceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_update_password);
		init();
	}

	// 初始化
	private void init() {
		oldPassWordEdit = (EditText) findViewById(R.id.sl_tab_setting_update_Text_oldPassWord);
		newPassWrodEdit = (EditText) findViewById(R.id.sl_tab_setting_update_Text_NewpassWord);
		againPassWrodEdit = (EditText) findViewById(R.id.sl_tab_setting_update_Text_NewpassWordAgain);
		submitBtn = (Button) findViewById(R.id.sl_tab_setting_update_btn);
		back = (LinearLayout) findViewById(R.id.btn_back_left);

		submitBtn.setOnClickListener(this);
		back.setOnClickListener(this);

		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 提交事件
		case R.id.sl_tab_setting_update_btn:
			if (!Utils.checkNetworkAvailable1(this)) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.msg_nonetstr, Toast.LENGTH_SHORT).show();
				return;
			}
			if (oldPassWordEdit.getEditableText().toString() == null
					|| "".equals(oldPassWordEdit.getEditableText().toString())
					|| oldPassWordEdit.getEditableText().toString().length() < 1) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_needed_prompt_old, 1).show();
				return;
			}
			if (newPassWrodEdit.getEditableText().toString() == null
					|| "".equals(newPassWrodEdit.getEditableText().toString())
					|| newPassWrodEdit.getEditableText().toString().length() < 1) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_needed_prompt_new, 1).show();
				return;
			}
			if (againPassWrodEdit.getEditableText().toString() == null
					|| "".equals(againPassWrodEdit.getEditableText().toString())
					|| againPassWrodEdit.getEditableText().toString().length() < 1) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_needed_prompt_again, 1).show();
				return;
			}

			String userPwdd = null;
			try {
				// userPwdd = mSess.getPwd(TX.tm.getUserID());
				userPwdd = mSess.mUserLoginInfor.getCurrentPwd();
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "获取旧密码异常", e);
			}
			if (!oldPassWordEdit.getEditableText().toString().equals(userPwdd)) {
				showToast(R.string.pw_needed_prompt_noequal);
				return;
			}
			if (!newPassWrodEdit.getEditableText().toString()
					.equals(againPassWrodEdit.getEditableText().toString())) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_needed_prompt_noagain, 1).show();
				return;
			}
			long current_time = System.currentTimeMillis();
			// long last_click_time =
			// prefs.getLong(CommonData.PASSWORD_CHECK_CLICK_TIME, 0);
			long last_click_time = mSess.mPrefMeme.password_check_click_time
					.getVal();
			long time_ = current_time - last_click_time;
			if (needcheckchargepassword && time_ <= 60000) {
				Utils.startPromptDialog(SetUpdatePassWordActivity.this,
						R.string.prompt, R.string.check_tel_fast);
				needcheckchargepassword = false;
				return;
			}

			// editor.putLong(CommonData.PASSWORD_CHECK_CLICK_TIME,
			// current_time);//这个与TXMe个人信息无关
			// editor.commit();
			mSess.mPrefMeme.password_check_click_time.setVal(current_time)
					.commit();

			String password = newPassWrodEdit.getEditableText().toString();
			if (password == null || "".equals(password)
					|| password.length() < 1) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_needed_prompt, 1).show();
				return;
			}
			int space_index = 0;
			space_index = password.indexOf(" ");
			if (space_index != -1) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_has_space_prompt, 1).show();
				return;
			}
			if (password.length() < 6) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_min_length_prompt, 1).show();
				return;
			} else if (password.length() > 20) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_max_length_prompt, 1).show();
				return;
			}
			boolean hasChinese = false;
			for (int i = 0; i < password.length(); i++) {
				if (password.substring(i, i + 1).matches("[\\u4e00-\\u9fa5]+")) {
					hasChinese = true;
					break;
				}
			}
			if (hasChinese) {
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_has_chinese_prompt, 1).show();
				return;
			}
			String pw_is_uploading_prompt = SetUpdatePassWordActivity.this
					.getString(R.string.pw_is_uploading_prompt);

			showDialogTimer(SetUpdatePassWordActivity.this, 0,
					pw_is_uploading_prompt, 30 * 1000, new BaseTimerTask() {
						public void run() {
							super.run();
							Message msg = new Message();
							msg.what = PASSWORD_REQUEST_FAIL;
							mHandler.sendMessage(msg);
						}
					}).show();

			mSess.getSocketHelper().sendPassword(password);
			needcheckchargepassword = true;
			break;

		case R.id.btn_back_left:
			finish();
			break;

		default:
			break;
		}

	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_PWD_RSP.equals(intent
					.getAction())) {
				dealChangePwd(serverRsp);

			}
		}
	}

	private void dealChangePwd(ServerRsp serverRsp) {
		cancelDialogTimer();
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				// prefs.edit().putString(CommonData.PASSWORD,
				// newPassWrodEdit.getEditableText().toString()).commit();
				// prefs.edit().putString(CommonData.INPUTPASSWORD,
				// newPassWrodEdit.getEditableText().toString()).commit();
				// TX.tm.reloadTXMe();///////

//				// 修改密码成功，更新存储的密码
//				try {
//					mSess.mUserLoginInfor
//							.updateUserPwd("" + TX.tm.getUserID(),
//									newPassWrodEdit.getEditableText()
//											.toString().trim());
//				} catch (JSONException e) {
//					if (Utils.debug)
//						Log.e(TAG, "更新密码异常", e);
//				}
//				LoginSessionManager.getManager(txdata).changePassword(
//						newPassWrodEdit.getEditableText().toString());
				handleMessage(PASSWORD_SETTING_SUCCESS);
				
				
				// String moreuser = prefs.getString(CommonData.MORE_USER_251,
				// "");
				// if (!Utils.isNull(moreuser)) {
				// String[] userpwd = moreuser.split("�#�");
				// for (int i = 0; i < userpwd.length; i++) {
				// if (!Utils.isNull(userpwd[i])) {
				// String[] tmp = userpwd[i].split("�");
				// String name = tmp[0];
				// String pwd = null;
				// if (tmp.length < 2) {
				// pwd = tmp[0];
				// } else {
				// pwd = tmp[1];
				// }
				// if (name.equals("" + TX.tm.getTxMe().partner_id)) {
				// String s = Utils.filterSpecial(name + "�" + pwd);
				// if (!Utils.isNull(s)) {
				// moreuser = moreuser.replaceAll(s, name + "�"
				// + newPassWrodEdit.getEditableText().toString());
				// }
				// break;
				// }
				// }
				// }
				// }
				// prefs.edit().putString(CommonData.MORE_USER_251,
				// moreuser).commit();
				break;
			}
			case PWD_INVALID: {
				handleMessage(PASSWORD_FORMAT_ERROR);
				break;
			}
			case REQ_THAN_LIMIT: {
				handleMessage(PASSWORD_REQUEST_TIME_LIMIT);
				break;
			}
			case OPT_FAILED: {
				handleMessage(PASSWORD_REQUEST_FAIL);
				break;
			}
			}
		}
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			cancelDialogTimer();
			switch (msg.what) {

			case PASSWORD_SETTING_SUCCESS:
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_update_success_prompt, 1).show();
				
				
				// 修改密码成功，清除存储的密码
				try {
					mSess.mUserLoginInfor.clearUserPwd(""+TX.tm.getUserID());//清除密码
				} catch (JSONException e) {
					if (Utils.debug)
						Log.e(TAG, "清除密码异常", e);
				}
				
				SetUpdatePassWordActivity.this.finish();
				exitLogin();
				
				break;
			case PASSWORD_FORMAT_ERROR:
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_format_error_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_TIME_LIMIT:
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_request_time_limit_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_FAIL:
				Toast.makeText(SetUpdatePassWordActivity.this,
						R.string.pw_request_failed_prompt, 1).show();
				break;

			}
		}
	};
	private LinearLayout back;

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_CHANGE_PWD_RSP);
			filter.addAction(Constants.INTENT_ACTION_BIND_MOBILE_RSP);
			filter.addAction(Constants.INTENT_ACTION_UNBIND_MOBILE_RSP);
			this.registerReceiver(updatereceiver, filter);
		}

	}

	private void handleMessage(int what) {
		Message message = new Message();
		message.what = what;
		mHandler.sendMessage(message);
	}

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}
}
