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
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;
/**
 * 修改本账号密码为空时密码
 * @author Administrator
 *
 */
public class SetUpdateOtherPassWordActivity extends BaseActivity implements OnClickListener{
	private static final String TAG = "SetUpdateOtherPassWordActivity";
	private EditText pwdEdit;
	private Button commit;
	private static final int PASSWORD_SETTING_SUCCESS = 0;
	private static final int PASSWORD_FORMAT_ERROR = 1;
	private static final int PASSWORD_REQUEST_TIME_LIMIT = 2;
	private static final int PASSWORD_REQUEST_FAIL = 3;
	private UpdateReceiver updatereceiver;
//	private SharedPreferences prefs;// 配置参数
//	private Editor editor;
	private boolean needcheckchargepassword = true;
     @Override
    protected void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	setContentView(R.layout.activity_setting_update_other_password);
    	init();
    }
     private void init(){
//    	 prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
// 		 editor = prefs.edit();
    	 pwdEdit=(EditText) findViewById(R.id.sl_tab_setting_update_Text_other_NewpassWord);
    	 commit=(Button) findViewById(R.id.sl_tab_setting_update_other_btn);
    	 back = (LinearLayout) findViewById(R.id.btn_back_left);
    	 
    	 commit.setOnClickListener(this);
    	 back.setOnClickListener(this);
    	 
    	 
     }
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 提交事件
		case R.id.sl_tab_setting_update_other_btn:
			if (!Utils.checkNetworkAvailable1(this)) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.msg_nonetstr, Toast.LENGTH_SHORT).show();
				return;
			}
			
			if (pwdEdit.getEditableText().toString() == null
					|| "".equals(pwdEdit.getEditableText().toString())
					|| pwdEdit.getEditableText().toString().length() < 1) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_needed_prompt, 1).show();
				return;
			}
			
			
			
			long current_time = System.currentTimeMillis();
//			long last_click_time = prefs.getLong(CommonData.PASSWORD_CHECK_CLICK_TIME, 0);
			long last_click_time = mSess.mPrefMeme.password_check_click_time.getVal();
			long time_ = current_time - last_click_time;
			if (needcheckchargepassword && time_ <= 60000) {
				Utils.startPromptDialog(SetUpdateOtherPassWordActivity.this, R.string.prompt, R.string.check_tel_fast);
				needcheckchargepassword = false;
				return;
			}

//			editor.putLong(CommonData.PASSWORD_CHECK_CLICK_TIME, current_time);
//			editor.commit();
			mSess.mPrefMeme.password_check_click_time.setVal(current_time).commit();
			
			String password = pwdEdit.getEditableText().toString();
			if (password == null || "".equals(password) || password.length() < 1) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_needed_prompt, 1).show();
				return;
			}
			int space_index = 0;
			space_index = password.indexOf(" ");
			if (space_index != -1) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_has_space_prompt, 1).show();
				return;
			}
			if (password.length() < 6) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_min_length_prompt, 1).show();
				return;
			} else if (password.length() > 20) {
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_max_length_prompt, 1).show();
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
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_has_chinese_prompt, 1).show();
				return;
			}
			String pw_is_uploading_prompt = SetUpdateOtherPassWordActivity.this.getString(R.string.pw_is_uploading_prompt);

			showDialogTimer(SetUpdateOtherPassWordActivity.this, 0, pw_is_uploading_prompt, 30 * 1000, new BaseTimerTask() {
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
	
	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			cancelDialogTimer();
			switch (msg.what) {

			case PASSWORD_SETTING_SUCCESS:
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_update_success_prompt, 1).show();
				SetUpdateOtherPassWordActivity.this.finish();
				break;
			case PASSWORD_FORMAT_ERROR:
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_format_error_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_TIME_LIMIT:
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_request_time_limit_prompt, 1).show();
				break;
			case PASSWORD_REQUEST_FAIL:
				Toast.makeText(SetUpdateOtherPassWordActivity.this, R.string.pw_request_failed_prompt, 1).show();
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
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_PWD_RSP.equals(intent.getAction())) {
				dealChangePwd(serverRsp);

			}
		}
	}
	private void dealChangePwd(ServerRsp serverRsp) {
		cancelDialogTimer();
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
//				prefs.edit().putString(CommonData.PASSWORD, pwdEdit.getEditableText().toString()).commit();
//				prefs.edit().putString(CommonData.INPUTPASSWORD, pwdEdit.getEditableText().toString()).commit();
				String newPwd = pwdEdit.getEditableText().toString().trim();
				//修改密码成功，更新存储的密码
				try {
//					mSess.mUserLoginInfor.updateUserPwd(""+TX.tm.getUserID(),newPwd);
					mSess.saveTempPwd(newPwd);
					mSess.mUserLoginInfor.saveLoginSuccessUserInfor(""+TX.tm.getUserID(),mSess.mPrefMeme.avatar_url.getVal());
				} catch (JSONException e) {
					if(Utils.debug)Log.e(TAG,"更新用户密码异常",e);
				}
				
				mSess.changePassword(
						pwdEdit.getEditableText().toString());
				handleMessage(PASSWORD_SETTING_SUCCESS);
				
//				String moreuser = prefs.getString(CommonData.MORE_USER_251, "");
//				if (!Utils.isNull(moreuser)) {
//					String[] userpwd = moreuser.split("�#�");
//					for (int i = 0; i < userpwd.length; i++) {
//						if (!Utils.isNull(userpwd[i])) {
//							String[] tmp = userpwd[i].split("�");
//							String name = tmp[0];
//							String pwd = null;
//							if (tmp.length < 2) {
//								pwd = tmp[0];
//							} else {
//								pwd = tmp[1];
//							}
//							if (name.equals("" + TX.tm.getTxMe().partner_id)) {
//								String s = Utils.filterSpecial(name + "�" + pwd);
//								if (!Utils.isNull(s)) {
//									moreuser = moreuser.replaceAll(s, name + "�"
//											+ pwdEdit.getEditableText().toString());
//								}
//								break;
//							}
//						}
//					}
//				}
//				prefs.edit().putString(CommonData.MORE_USER_251, moreuser).commit();
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

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_CHANGE_PWD_RSP);
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
