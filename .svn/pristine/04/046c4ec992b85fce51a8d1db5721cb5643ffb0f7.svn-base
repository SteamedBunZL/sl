package com.shenliao.set.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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

/**
 * 邮箱绑定
 * 
 * @author xch
 * 
 */
public class SetEmailBindActivity extends BaseActivity implements
		android.view.View.OnClickListener {
	private EditText emailEdit;// 邮件地址
	private Button submitBtn;// 提交按钮
	// private SharedPreferences prefs = null;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private String newEmailStr;
	private String email;
	// 绑定邮箱相关参数
	private static final int EMAIL_CHECK_TIMEOUT = 0;
	private static final int EMAIL_CHECK_SUCCESS = 1;
	private static final int EMAIL_FORMAT_ERROR = 2;
	private static final int EMAIL_HAVE_EXIST = 3;
	private static final int EMAIL_CHECK_REPEAT = 4;
	private static final int EMAIL_CHECK_SERVER_BUSYING = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_bindmanage_email);

		init();

	}

	// 初始化
	private void init() {
		emailEdit = (EditText) findViewById(R.id.mEmail);
		submitBtn = (Button) findViewById(R.id.mSkip);
		back = (LinearLayout) findViewById(R.id.btn_back_left);

		submitBtn.setOnClickListener(this);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SetEmailBindActivity.this.finish();
			}
		});
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mSkip:

			if (!Utils.checkNetworkAvailable1(this)) {
				Toast.makeText(SetEmailBindActivity.this,
						R.string.msg_nonetstr, Toast.LENGTH_SHORT).show();
				return;
			}
			String emailValue = emailEdit.getEditableText().toString();
			emailValue = emailValue.replace(" ", "");
			int len = emailValue.length();
			if (len <= 0) {
				Toast.makeText(SetEmailBindActivity.this,
						R.string.insert_email_prompt, Toast.LENGTH_SHORT)
						.show();
			} else if (len < 6 || len > 50) {
				Toast.makeText(SetEmailBindActivity.this,
						R.string.email_format_error, Toast.LENGTH_SHORT).show();
			} else {
				String emailCheckPrompt = SetEmailBindActivity.this
						.getResources().getString(R.string.send_email_reset);
				String emailValidation = SetEmailBindActivity.this
						.getResources().getString(
								R.string.regex_email_validation);
				Pattern emailPattern = Pattern.compile(emailValidation); // 邮箱匹配
				Matcher emailMatcher = emailPattern.matcher(emailValue);
				if (!emailMatcher.find()) {
					Toast.makeText(SetEmailBindActivity.this,
							R.string.email_format_error, Toast.LENGTH_SHORT)
							.show();
				} else {
					// prefs.edit().putString(CommonData.EMAIL,
					// emailValue).commit();
					mSess.mPrefMeme.email.setVal(emailValue).commit();
					TX.tm.reloadTXMe();// //

					showDialogTimer(SetEmailBindActivity.this, 0,
							emailCheckPrompt, 10 * 1000, new BaseTimerTask() {
								public void run() {
									super.run();
									Message msg = handler.obtainMessage();
									msg.what = EMAIL_CHECK_TIMEOUT;
									handler.sendMessage(msg);
								}
							}).show();
					newEmailStr = emailValue;

					mSess.getSocketHelper().sendCheckEmail(
							emailValue);

				}
				break;

			}
		}

	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				SetEmailBindActivity.this);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setNegativeButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		promptDialog.show();
	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_BIND_EMAIL_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_BIND_EMAIL_RSP.equals(intent
					.getAction())) {
				dealEmailCheck(serverRsp);
			}
		}

		// 处理修改签名返回结果方法
		private void dealEmailCheck(ServerRsp serverRsp) {

			cancelDialog();
			if (serverRsp != null) {
				switch (serverRsp.getStatusCode()) {
				case RSP_OK: {
					handler.sendEmptyMessage(EMAIL_CHECK_SUCCESS);
					break;
				}
				case EMAIL_INVALID: {
					handler.sendEmptyMessage(EMAIL_FORMAT_ERROR);
					break;
				}
				case OTHER_BIND_THIS_EMAIL: {
					handler.sendEmptyMessage(EMAIL_HAVE_EXIST);
					break;
				}
				case EMAIL_HAS_BINDED: {
					handler.sendEmptyMessage(EMAIL_CHECK_REPEAT);
					break;
				}
				case SERVER_BUSY: {
					handler.sendEmptyMessage(EMAIL_CHECK_SERVER_BUSYING);
					break;
				}
				}

			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case EMAIL_CHECK_SUCCESS:
				AlertDialog.Builder promptDialog = new AlertDialog.Builder(
						SetEmailBindActivity.this);
				promptDialog.setTitle(null);
				promptDialog
						.setMessage("我们向您提供的邮箱发送了一封验证邮件,请查收邮件以完成验证,验证之后退出神聊再进入");
				promptDialog.setNegativeButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								dialog.cancel();
								Intent intent = new Intent(
										SetEmailBindActivity.this,
										SetBindManageActivity.class);
								startActivity(intent);
								SetEmailBindActivity.this.finish();
							}
						});
				promptDialog.show();
				email = newEmailStr;
				// editor = prefs.edit();
				// editor.putString(CommonData.EMAIL, email);
				// editor.putBoolean(CommonData.IS_BIND_EMAIL, false);
				// editor.commit();
				mSess.mPrefMeme.email.setVal(email);
				mSess.mPrefMeme.is_bind_email.setVal(false).commit();
				break;
			case EMAIL_FORMAT_ERROR:
				startPromptDialog(R.string.error, R.string.email_format_error);
				break;
			case EMAIL_HAVE_EXIST:
				startPromptDialog(R.string.prompt, R.string.email_have_exist);
				break;
			case EMAIL_CHECK_REPEAT:
				email = newEmailStr;
				// editor = prefs.edit();
				// editor.putString(CommonData.EMAIL, email);
				// editor.putBoolean(CommonData.IS_BIND_EMAIL, true);
				// editor.commit();
				mSess.mPrefMeme.email.setVal(email);
				mSess.mPrefMeme.is_bind_email.setVal(true).commit();
				startPromptDialog(R.string.prompt, R.string.email_check_repeat);
				break;
			case EMAIL_CHECK_SERVER_BUSYING:
				startPromptDialog(R.string.prompt, R.string.server_busying);
				break;

			case EMAIL_CHECK_TIMEOUT:
				startPromptDialog(R.string.prompt, R.string.email_check_outtime);
				break;
			}

			TX.tm.reloadTXMe();// ////
			super.handleMessage(msg);
		}
	};
	private LinearLayout back;

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}
}
