package com.tuixin11sms.tx.activity;

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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class PhoneBindActivity extends BaseActivity {
	private static final int TEL_CHECK_TIMEOUT = 0;
	private static final int TEL_CHECK_SUCCESS = 1;
	private static final int TEL_CHECK_SERVER_BUSYING = 2;
	private static final int TEL_FORMAT_ERROR = 3;
	private static final int TEL_CHECK_REPEAT = 4;
	private static final int TEL_HAVE_EXIST = 5;
	private static final int TEL_CHECK_BEYOND_LIMIT = 6;

	private static final int DEAL_SEND_SMS_MESSAGE = 41;
	private UpdateReceiver updatereceiver;
	// private SharedPreferences prefs = null;
	private Spinner mCountry;
	private ArrayAdapter adapter2;
	// private Button mSubmit;
	private Button mSkip;
	private EditText mPhone;
	// private Editor editor;
	private String[] mLoc;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_bindmanage_phone);
		// prefs = txdata.getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		mCountry = (Spinner) findViewById(R.id.mCountry);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				PhoneBindActivity.this.finish();
			}
		});

		// mSubmit = (Button) findViewById(R.id.mSubmit);
		mSkip = (Button) findViewById(R.id.mSkip);
		mPhone = (EditText) findViewById(R.id.mPhone);
		// mID = (TextView) findViewById(R.id.mID);
		mLoc = this.getResources().getStringArray(R.array.country_bind);
		adapter2 = ArrayAdapter.createFromResource(this, R.array.country_bind,
				R.layout.country_sel);
		adapter2.setDropDownViewResource(R.layout.country_sel_item);
		mCountry.setAdapter(adapter2);
		mCountry.setOnItemSelectedListener(new SpinnerOnSelectedListener());
		mSkip.setOnClickListener(new SubmitButton());
		// mSkip.setOnClickListener(new View.OnClickListener() {
		// @Override
		// public void onClick(View v) {
		// Intent helpIntent = new Intent(PhoneBindActivity.this,
		// TuiXinMainTab.class);
		// startActivity(helpIntent);
		// PhoneBindActivity.this.finish();
		// }
		// });
		// mID.setText(prefs.getString(CommonData.USER_ID, ""));

	}

	class SpinnerOnSelectedListener implements OnItemSelectedListener {

		// 当用户选定了一个条目时，就会调用该方法
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view,
				int position, long id) {
			if (position != 0) {
				alertPhoneBind();
			} else {
				mPhone.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
			//
		}
	}

	private void alertPhoneBind() {

		AlertDialog newTelDialog = new AlertDialog.Builder(
				PhoneBindActivity.this).create();
		newTelDialog.setTitle(R.string.check_tel_button);
		newTelDialog.setMessage(PhoneBindActivity.this.getResources()
				.getString(R.string.regist_bind_phone));
		newTelDialog.setButton(
				PhoneBindActivity.this.getResources().getString(
						R.string.foreign_check_button_text),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String foreignCheckPrompt = PhoneBindActivity.this
								.getResources().getString(
										R.string.foreign_check_code_request);
						showDialogTimer(PhoneBindActivity.this, 0,
								foreignCheckPrompt, 30 * 1000,
								new BaseTimerTask() {
									public void run() {
										super.run();
										Message msg = new Message();
										msg.what = TEL_CHECK_TIMEOUT;
										mHandler.sendMessage(msg);
									}
								}).show();
						mSess.getSocketHelper()
								.sendSmsIdentification();
						// CommonData.isSettingPage
						// = true;
					}
				});
		newTelDialog.setButton2(PhoneBindActivity.this.getResources()
				.getString(R.string.skip),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent helpIntent = new Intent(PhoneBindActivity.this,
								TuiXinMainTab.class);
						startActivity(helpIntent);
						PhoneBindActivity.this.finish();

					}
				});
		newTelDialog.show();
		mPhone.setVisibility(View.GONE);

	}

	class SubmitButton implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			if (!Utils.checkNetworkAvailable(PhoneBindActivity.this)) {
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.weibo_tongbu_net_bad);
				return;
			}
			if (mPhone.getVisibility() == View.VISIBLE) {
				String telStr = mPhone.getText().toString().trim();
				if ("".equals(telStr) || telStr.trim().length() <= 0) {
					Toast.makeText(PhoneBindActivity.this,
							R.string.insert_tel_prompt, Toast.LENGTH_SHORT)
							.show();
					// startPromptDialog(R.string.prompt,R.string.insert_tel_prompt);
				} else {
					String telephone = Utils.get11Number(telStr);
					String telValidation = PhoneBindActivity.this
							.getResources().getString(
									R.string.regex_telephone_validation);
					Pattern telPattern = Pattern.compile(telValidation);
					Matcher telMatcher = telPattern.matcher(telephone);
					if (telMatcher.find()) {
						String telCheckPrompt = PhoneBindActivity.this
								.getResources().getString(
										R.string.request_check_code);

						showDialogTimer(PhoneBindActivity.this, 0,
								telCheckPrompt, 30 * 1000, new BaseTimerTask() {
									public void run() {
										super.run();
										Message msg = new Message();
										msg.what = TEL_CHECK_TIMEOUT;
										mHandler.sendMessage(msg);
									}
								}).show();

						mSess.getSocketHelper().sendBindPhone(
								telephone);
					} else {
						Toast.makeText(PhoneBindActivity.this,
								R.string.tel_format_error, Toast.LENGTH_SHORT)
								.show();
						// startPromptDialog(R.string.error,R.string.tel_format_error);
					}
				}
			} else {
				alertPhoneBind();
			}
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_BIND_MOBILE_RSP);
			filter.addAction(Constants.INTENT_ACTION_SMS_IDENTIFICATION_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_BIND_MOBILE_RSP.equals(intent
					.getAction())) {
				dealBindMobile(serverRsp);
			} else if (Constants.INTENT_ACTION_SMS_IDENTIFICATION_RSP
					.equals(intent.getAction())) {
				dealSmsIdentify(serverRsp);
			}
		}
	}

	private void dealSmsIdentify(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				boolean sendState = serverRsp.getBoolean("sendState", false);
				Bundle bundle = new Bundle();
				bundle.putBoolean("send_message_state", sendState);
				Message message = new Message();
				message.setData(bundle);
				message.what = DEAL_SEND_SMS_MESSAGE;
				mHandler.sendMessage(message);
				break;
			}
			case OPT_FAILED: {
				new AlertDialog.Builder(PhoneBindActivity.this)
						.setTitle(R.string.error)
						.setMessage(R.string.sim_error)
						.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								}).show();
				break;
			}
			}
		}
	}

	private void dealBindMobile(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				mHandler.sendEmptyMessage(TEL_CHECK_SUCCESS);
				break;
			}
			case SERVER_BUSY: {
				mHandler.sendEmptyMessage(TEL_CHECK_SERVER_BUSYING);
				break;
			}
			case MOBILE_INVALID: {
				mHandler.sendEmptyMessage(TEL_FORMAT_ERROR);
				break;
			}
			case MOBILE_HAS_BINDED: {
				mHandler.sendEmptyMessage(TEL_CHECK_REPEAT);
				break;
			}
			case OTHER_BIND_THIS_MOBILE: {
				mHandler.sendEmptyMessage(TEL_HAVE_EXIST);
				break;
			}
			case REQ_THAN_LIMIT: {
				mHandler.sendEmptyMessage(TEL_CHECK_BEYOND_LIMIT);
				break;
			}
			}
		}
	}

	private Handler mHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			cancelDialogTimer();
			switch (msg.what) {
			case TEL_CHECK_TIMEOUT:
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.request_tel_code_outtime);
				break;
			case TEL_CHECK_SUCCESS:
				long receive_time = System.currentTimeMillis();
				// editor.putString(CommonData.TELEPHONE, mPhone.getText()
				// .toString().trim());
				// editor.putLong(CommonData.TEL_CHECK_TIME, receive_time);
				// editor.putBoolean(CommonData.IS_BIND_PHONE, false);
				// editor.putInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_NO_CHECK);
				// editor.commit();
				mSess.mPrefMeme.telephone.setVal(mPhone.getText().toString()
						.trim());
				mSess.mPrefMeme.tel_check_time.setVal(receive_time);
				mSess.mPrefMeme.is_bind_phone.setVal(false);
				mSess.mPrefMeme.tel_bind_state.setVal(PrefsMeme.TEL_NO_CHECK)
						.commit();
				TX.tm.reloadTXMe();// ///
				Intent intent = new Intent(PhoneBindActivity.this,
						TelCheckActivity.class);
				intent.putExtra("fromRegister", true);
				startActivity(intent);
				PhoneBindActivity.this.finish();
				break;
			case TEL_CHECK_SERVER_BUSYING:
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.server_busying);
				break;
			case TEL_FORMAT_ERROR:
				Utils.startPromptDialog(PhoneBindActivity.this, R.string.error,
						R.string.telephone_wrong);
				break;
			case TEL_CHECK_REPEAT:
				// editor.putString(CommonData.TELEPHONE, mPhone.getText()
				// .toString().trim());
				// editor.putInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_SUCCESS);
				// editor.putBoolean(CommonData.IS_BIND_PHONE, true);
				// editor.commit();
				mSess.mPrefMeme.telephone.setVal(mPhone.getText().toString()
						.trim());
				mSess.mPrefMeme.tel_bind_state
						.setVal(PrefsMeme.TEL_BIND_SUCCESS);
				mSess.mPrefMeme.is_bind_phone.setVal(true).commit();
				TX.tm.reloadTXMe();// ////
				// flush();
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.telephone_check_repeat);
				break;
			case TEL_HAVE_EXIST:
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.tel_have_exist);
				break;
			case TEL_CHECK_BEYOND_LIMIT:
				Utils.startPromptDialog(PhoneBindActivity.this,
						R.string.prompt, R.string.tel_check_beyond_limit);
				break;
			case DEAL_SEND_SMS_MESSAGE:
				Bundle bundle = msg.getData();
				boolean sendState = bundle.getBoolean("send_message_state");
				if (sendState) {
					// prefs.edit().putString(CommonData.TELEPHONE, "");
					// prefs.edit().putInt(CommonData.TEL_BIND_STATE,
					// CommonData.TEL_NO_CHECK);
					// prefs.edit().putBoolean(CommonData.IS_BIND_PHONE, false);
					// prefs.edit().commit();
					mSess.mPrefMeme.telephone.setVal("");
					mSess.mPrefMeme.tel_bind_state
							.setVal(PrefsMeme.TEL_NO_CHECK);
					mSess.mPrefMeme.is_bind_phone.setVal(false).commit();
				} else {
					// prefs.edit().putString(CommonData.TELEPHONE, "");
					// prefs.edit().putInt(CommonData.TEL_BIND_STATE,
					// CommonData.TEL_BIND_FAILED);
					// prefs.edit().putBoolean(CommonData.IS_BIND_PHONE, false);
					// prefs.edit().commit();
					mSess.mPrefMeme.telephone.setVal("");
					mSess.mPrefMeme.tel_bind_state
							.setVal(PrefsMeme.TEL_BIND_FAILED);
					mSess.mPrefMeme.is_bind_phone.setVal(false).commit();
				}
				TX.tm.reloadTXMe();// //////
				Intent helpIntent = new Intent(PhoneBindActivity.this,
						TuiXinMainTab.class);
				startActivity(helpIntent);
				PhoneBindActivity.this.finish();
				break;
			}

		}

	};
	private LinearLayout btn_back_left;

}
