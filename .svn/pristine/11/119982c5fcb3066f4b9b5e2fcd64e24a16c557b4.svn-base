package com.tuixin11sms.tx.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class LostPasswordActivity extends BaseActivity {
	//	private static final String TAG = LostPasswordActivity.class.getSimpleName();
//	private SharedPreferences prefs;
//	private Editor editor;
	private UpdateReceiver updatareceiver;
	private LinearLayout mReturn;
	private Button mResetPassword;
	private EditText mResetContent;
	private final int NICKNAME_LOGIN_OK = 0;
	private final int EMAIL_ILLEGAL = 8;
	private final int EMAIL_FAILE = 9;
	private final int PHONE_ILLEGAL = 10;
	private final int POHNE_FAILE = 11;
	private final int HANDLER_FAILE = 12;
	private final int TIME_OUT = 13;

	private boolean canClick = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_lost_password);
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE
//				+ Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
		mReturn = (LinearLayout) findViewById(R.id.mReturn);
		mResetPassword = (Button) findViewById(R.id.mResetPassword);
		mResetContent = (EditText) findViewById(R.id.mResetContent);

		mReturn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				LostPasswordActivity.this.finish();
			}
		});
		mResetPassword.setOnClickListener(resetClick);
	}

	private View.OnClickListener returnClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			LostPasswordActivity.this.finish();
		}
	};

	private View.OnClickListener resetClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			if(!Utils.checkNetworkAvailable(LostPasswordActivity.this)){
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt, R.string.seach_network_title);
				return;
			}
			if(!canClick)return;
			String username = mResetContent.getText().toString().trim();
			int len = username.length();
			if (len <= 0) {
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.insert_email_phone_prompt);
			} else {
				canClick = false;
				if (username.indexOf("@") != -1) {
					String emailValidation = LostPasswordActivity.this.getResources().getString(
							R.string.regex_email_validation);
					Pattern emailPattern = Pattern.compile(emailValidation); //邮箱匹配
					Matcher emailMatcher = emailPattern.matcher(username);
					if (!emailMatcher.find()) {
						Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
								R.string.pw_request_email_illegal_prompt);
						return;
					}
				} else {
					String telephone = Utils.get11Number(username);
					String telValidation = LostPasswordActivity.this.getResources().getString(
							R.string.regex_telephone_validation);
					Pattern telPattern = Pattern.compile(telValidation);
					Matcher telMatcher = telPattern.matcher(telephone);
					if (!telMatcher.find()) {
						Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
								R.string.pw_request_phone_illegal_prompt);
						return;
					}
				}
				String pwPrompt = LostPasswordActivity.this.getResources().getString(
						R.string.pw_is_reset_prompt);
				ProgressDialog dialog = showDialogTimer(LostPasswordActivity.this, 0, pwPrompt, 20 * 1000, new BaseTimerTask(){

					@Override
					public void run() {
						super.run();
						Message msg1 = new Message();
						msg1.what = TIME_OUT;
						handler.sendMessage(msg1);
					}
					
				});
				
				dialog.setOnKeyListener(new DialogInterface.OnKeyListener() {
					//屏蔽按键
					@Override
					public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
						if(keyCode == KeyEvent.KEYCODE_BACK) {
							canClick = true;
							cancelTimer();
						}
						return false;
					}
				});
				dialog.show();
				// 找回密码之前, 首先设置相关标记
				mSess.setFindPasswordBackInfor(username);
				mSess.setLoginSuccessed(false);
//				editor.putString(CommonData.DOOR, "");
//				editor.commit();
				mSess.mPrefMeme.door.setVal("").commit();
				mSess.getSocketHelper().sendPing();
			}
		}
	};

	public void onResume() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_PASSWORD);
			this.registerReceiver(updatareceiver, filter);
		}
		super.onResume();
	}
	
	public void onStop() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			canClick = true;
			cancelDialogTimer();
			int num = msg.what;
			switch (num) {
			//			case NICKNAME_CHANGE_TIMEOUT:
			//				startPromptDialog(R.string.prompt, R.string.insert_nickname_timeout);
			//				break;
			case HANDLER_FAILE:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_failed_prompt);
				break;
			case EMAIL_FAILE:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_email_failed_prompt);
				break;
			case EMAIL_ILLEGAL:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_email_illegal_prompt);
				break;
			case POHNE_FAILE:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_phone_failed_prompt);
				break;
			case PHONE_ILLEGAL:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_phone_illegal_prompt);
				break;
			case TIME_OUT:
				Utils.startPromptDialog(LostPasswordActivity.this, R.string.prompt,
						R.string.pw_request_failed_prompt);
				break;
			case NICKNAME_LOGIN_OK:
				//				Intent i = new Intent(LostPasswordActivity.this,LoginActivity.class);
				//				startActivity(i);
				AlertDialog.Builder promptDialog = new AlertDialog.Builder(
						LostPasswordActivity.this);
				promptDialog.setTitle(R.string.prompt);
				promptDialog.setMessage(R.string.pw_get_success_prompt);
				promptDialog.setNegativeButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								dialog.cancel();
								Intent iResult = new Intent();
								iResult.putExtra("result", mResetContent.getText().toString());
								setResult(10, iResult);
								LostPasswordActivity.this.finish();
							}
						});
				promptDialog.show();
				//				Utils.startPromptDialog(LostPasswordActivity.this,R.string.prompt, R.string.pw_request_failed_prompt);
				break;
			}
		}
	};

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_GET_PASSWORD.equals(intent.getAction())) {
				dealGetPwd(serverRsp);
			}
		}
	}

	private void dealGetPwd(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				handleMessage(NICKNAME_LOGIN_OK);
				break;
			}
			case MOBILE_INVALID: {
				handleMessage(PHONE_ILLEGAL);
				break;
			}
			case MOBILE_NO_BINDED: {
				handleMessage(POHNE_FAILE);
				break;
			}
			case EMAIL_INVALID: {
				handleMessage(EMAIL_ILLEGAL);
				break;
			}
			case EMAIL_NO_BINDED: {
				handleMessage(EMAIL_FAILE);
				break;
			}
			case OPT_FAILED: {
				handleMessage(HANDLER_FAILE);
				break;
			}
			}
		}
	}

	private void handleMessage(int what) {
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}

}
