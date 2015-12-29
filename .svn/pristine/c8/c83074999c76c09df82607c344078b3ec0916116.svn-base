package com.tuixin11sms.tx.activity;

import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.set.activity.SetBindManageActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class TelCheckActivity extends BaseActivity {
	private static final int TEL_CHECK_TIMEOUT = 0;
	private static final int TEL_CHECK_SUCCESS = 1;
	private static final int TEL_FORMAT_ERROR = 2;
	private static final int TEL_CHECK_REPEAT = 4;
	private static final int TEL_HAVE_EXIST = 5;
	private static final int TEL_CHECK_BEYOND_LIMIT = 6;

	private static final int BIND_CODE_TIMEOUT = 11;
	private static final int BIND_CODE_SUCCESS = 12;
	private static final int BIND_CODE_ERROR = 13;
	private static final int BIND_CODE_INVALID = 14;
	private static final int SERVER_BUSYING = 15;

	private static final int SEND_CHECK_MESSAGE_TIMEOUT = 20;
	private static final int DEAL_SEND_SMS_MESSAGE = 21;

	private Button get_code_again, code_confirm_button;
	private EditText inputText;
	// private TuixinService tuixinService;
	private ServerReceiver serverReceiver;
	private static final String TAG = TelCheckActivity.class.getSimpleName();
	// private SharedPreferences prefs;
	// private Editor editor;
	// private Button mSkip;
	private TextView mPhoneNumber;
	private String tel, newTelStr;
	private AlertDialog telDialog;
	private boolean isFromRegister;

	private CountDown cd;

	private void showTel(String phone) {
		if (phone != null) {
			if (phone.length() >= 11) {
				String s1 = phone.substring(0, 3);
				String s2 = phone.substring(phone.length() - 4, phone.length());
				mPhoneNumber.setText("已经向" + s1 + "****" + s2
						+ "发送短信，请在下方输入验证码");
			}
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case TEL_CHECK_TIMEOUT:
				startPromptDialog(R.string.prompt,
						R.string.request_tel_code_outtime);
				break;
			case TEL_CHECK_SUCCESS:
				// System.out.println("TelCheckActivity     case TEL_CHECK_SUCCESS:...");
				long receive_time = System.currentTimeMillis();
				if (cd != null) {
					cd.cancel();
					cd.start();
				} else {
					cd = new CountDown(60 * 1000, 1000);
					cd.start();
				}
				tel = newTelStr;
				// editor = prefs.edit();
				// editor.putString(CommonData.TELEPHONE, tel);
				// editor.putLong(CommonData.TEL_CHECK_TIME, receive_time);
				// editor.putInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_NO_CHECK);
				// editor.putBoolean(CommonData.IS_BIND_PHONE, false);
				// editor.commit();
				mSess.mPrefMeme.telephone.setVal(tel);
				mSess.mPrefMeme.tel_check_time.setVal(receive_time);
				mSess.mPrefMeme.tel_bind_state.setVal(PrefsMeme.TEL_NO_CHECK);
				mSess.mPrefMeme.is_bind_phone.setVal(false).commit();
				TX.tm.reloadTXMe();// ////////
				final Dialog confirmDialog = new Dialog(TelCheckActivity.this);
				confirmDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				confirmDialog.setContentView(R.layout.telephone_check_dialog);
				Button check_tel_confirm = (Button) confirmDialog
						.findViewById(R.id.check_tel_confirm);
				check_tel_confirm
						.setOnClickListener(new View.OnClickListener() {
							@Override
							public void onClick(View v) {
								confirmDialog.cancel();
							}
						});
				confirmDialog.show();
				showTel(tel);
				break;
			case TEL_FORMAT_ERROR:
				// int c1 = prefs.getInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_INIT_CODE);
				// editor = prefs.edit();
				// editor.putInt(CommonData.TEL_BIND_STATE, c1);
				// editor.commit();
				int c1 = mSess.mPrefMeme.tel_bind_state.getVal();
				mSess.mPrefMeme.tel_bind_state.setVal(c1);
				startPromptDialog(R.string.error, R.string.telephone_wrong);
				break;
			case TEL_CHECK_REPEAT:
				tel = newTelStr;
				// editor = prefs.edit();
				// editor.putString(CommonData.TELEPHONE, newTelStr);
				// editor.putInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_SUCCESS);
				// editor.putBoolean(CommonData.IS_BIND_PHONE, true);
				// editor.commit();
				mSess.mPrefMeme.telephone.setVal(newTelStr);
				mSess.mPrefMeme.tel_bind_state
						.setVal(PrefsMeme.TEL_BIND_SUCCESS);
				mSess.mPrefMeme.is_bind_phone.setVal(true).commit();
				startPromptDialog(R.string.prompt,
						R.string.telephone_check_repeat);
				break;
			case TEL_HAVE_EXIST:
				// int c2 = prefs.getInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_INIT_CODE);
				// editor = prefs.edit();
				// editor.putInt(CommonData.TEL_BIND_STATE, c2);
				// editor.commit();
				int c2 = mSess.mPrefMeme.tel_bind_state.getVal();
				mSess.mPrefMeme.tel_bind_state.setVal(c2).commit();
				startPromptDialog(R.string.prompt, R.string.tel_have_exist);
				break;
			case TEL_CHECK_BEYOND_LIMIT:
				// int c3 = prefs.getInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_INIT_CODE);
				// editor = prefs.edit();
				// editor.putInt(CommonData.TEL_BIND_STATE, c3);
				// editor.commit();
				int c3 = mSess.mPrefMeme.tel_bind_state.getVal();
				mSess.mPrefMeme.tel_bind_state.setVal(c3).commit();
				startPromptDialog(R.string.prompt,
						R.string.tel_check_beyond_limit);
				break;
			case BIND_CODE_TIMEOUT:
				startPromptDialog(R.string.prompt,
						R.string.bind_tel_code_outtime);
				break;
			case BIND_CODE_SUCCESS:
				// startPromptDialog("提示", "手机号码绑定成功");
				Toast.makeText(TelCheckActivity.this,
						R.string.bind_tel_code_success, Toast.LENGTH_LONG)
						.show();
				inputText.setText("");
				// editor = prefs.edit();
				// editor.putBoolean(CommonData.IS_BIND_PHONE, true);
				// editor.putInt(CommonData.TEL_BIND_STATE,
				// CommonData.TEL_BIND_SUCCESS);
				// editor.commit();
				mSess.mPrefMeme.is_bind_phone.setVal(true);
				mSess.mPrefMeme.tel_bind_state
						.setVal(PrefsMeme.TEL_BIND_SUCCESS);
				TelCheckActivity.this.finish();

				if (isFromRegister) {
					Intent helpIntent = new Intent(TelCheckActivity.this,
							SetBindManageActivity.class);
					startActivity(helpIntent);
				}
				break;
			case BIND_CODE_ERROR:
				startPromptDialog(R.string.error, R.string.tel_code_wrong);
				break;
			case BIND_CODE_INVALID:
				startPromptDialog(R.string.prompt, R.string.tel_code_invalid);
				break;
			case SERVER_BUSYING:
				int code = mSess.mPrefMeme.tel_bind_state.getVal();
				// editor = prefs.edit();
				// editor.putInt(CommonData.TEL_BIND_STATE, code);
				// editor.commit();
				mSess.mPrefMeme.tel_bind_state.setVal(code).commit();
				startPromptDialog(R.string.prompt, R.string.server_busying);
				break;
			case SEND_CHECK_MESSAGE_TIMEOUT:
				startPromptDialog(R.string.prompt,
						R.string.foreign_check_code_outtime);
				break;
			case DEAL_SEND_SMS_MESSAGE:
				cancelDialog();
				Bundle bundle = msg.getData();
				boolean sendState = bundle.getBoolean("send_message_state");
				final Dialog resultDialog = new Dialog(TelCheckActivity.this);
				resultDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
				if (sendState) {
					// editor = prefs.edit();
					// editor.putString(CommonData.TELEPHONE, "");
					// editor.putInt(CommonData.TEL_BIND_STATE,
					// CommonData.TEL_NO_CHECK);
					// editor.putBoolean(CommonData.IS_BIND_PHONE, false);
					// editor.commit();
					mSess.mPrefMeme.telephone.setVal("");
					mSess.mPrefMeme.tel_bind_state
							.setVal(PrefsMeme.TEL_NO_CHECK);
					mSess.mPrefMeme.is_bind_phone.setVal(false).commit();

					resultDialog
							.setContentView(R.layout.send_message_success_dialog);
					Button confirmButton = (Button) resultDialog
							.findViewById(R.id.send_message_success_button);
					confirmButton
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									resultDialog.dismiss();
								}
							});
					TelCheckActivity.this.finish();
				} else {
					// editor = prefs.edit();
					// editor.putString(CommonData.TELEPHONE, "");
					// editor.putInt(CommonData.TEL_BIND_STATE,
					// CommonData.TEL_BIND_FAILED);
					// editor.putBoolean(CommonData.IS_BIND_PHONE, false);
					// editor.commit();
					mSess.mPrefMeme.telephone.setVal("");
					mSess.mPrefMeme.tel_bind_state
							.setVal(PrefsMeme.TEL_BIND_FAILED);
					mSess.mPrefMeme.is_bind_phone.setVal(false).commit();

					resultDialog
							.setContentView(R.layout.send_message_failed_dialog);
					Button confirmButton = (Button) resultDialog
							.findViewById(R.id.send_message_failed_button);
					confirmButton
							.setOnClickListener(new View.OnClickListener() {
								@Override
								public void onClick(View v) {
									resultDialog.dismiss();
								}
							});
				}
				resultDialog.show();

				break;
			}
			TX.tm.reloadTXMe();// ///////
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_telephone_check_confirm);
		// prefs = getSharedPreferences(CommonData.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		inputText = (EditText) findViewById(R.id.input_code);
		LinearLayout btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		btn_back_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				TelCheckActivity.this.finish();
			}
		});
		get_code_again = (Button) findViewById(R.id.get_code_again);
		code_confirm_button = (Button) findViewById(R.id.code_confirm_button);
		// mSkip = (Button) findViewById(R.id.mSkip);
		mPhoneNumber = (TextView) findViewById(R.id.mPhoneNumber);
		// 弹出软键盘

		Bundle bundle = getIntent().getExtras();
		if (bundle != null && bundle.containsKey("fromRegister")) {
			isFromRegister = true;
			// mSkip.setVisibility(View.VISIBLE);
			// mSkip.setOnClickListener(new View.OnClickListener() {
			// @Override
			// public void onClick(View v) {
			// Intent helpIntent = new Intent(TelCheckActivity.this,
			// TuiXinMainTab.class);
			// startActivity(helpIntent);
			// TelCheckActivity.this.finish();
			// }
			// });

		} else {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					InputMethodManager m = (InputMethodManager) TelCheckActivity.this
							.getSystemService(Context.INPUT_METHOD_SERVICE);
					m.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				}
			}, 1000);
		}

		get_code_again.setOnClickListener(myOnClickListener);
		code_confirm_button.setOnClickListener(myOnClickListener);

		// if(prefs != null) {
		// showTel(prefs.getString(CommonData.TELEPHONE, ""));
		showTel(mSess.mPrefMeme.telephone.getVal());
		// }
		cd = new CountDown(60 * 1000, 1000);
		cd.start();
	}

	class CountDown extends CountDownTimer {

		public CountDown(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onFinish() {
			get_code_again.setText(R.string.get_code_again);
			get_code_again.setEnabled(true);
		}

		@Override
		public void onTick(long millisUntilFinished) {
			// String again = TelCheckActivity.this
			// .getString(R.string.get_code_again);
			String again = "重新获取";
			get_code_again.setText(again + "(" + millisUntilFinished / 1000
					+ ")");
			get_code_again.setEnabled(false);
		}

	};

	View.OnClickListener myOnClickListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			int id = v.getId();
			switch (id) {
			case R.id.get_code_again:
				// tel = prefs.getString(CommonData.TELEPHONE, "");
				tel = mSess.mPrefMeme.telephone.getVal();
				telDialog = new AlertDialog.Builder(TelCheckActivity.this)
						.create();
				telDialog.setTitle(R.string.check_tel_button);
				LinearLayout layout = new LinearLayout(TelCheckActivity.this);
				layout.setLayoutParams(new LayoutParams(
						LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
				layout.setOrientation(LinearLayout.VERTICAL);
				final EditText telText = new EditText(TelCheckActivity.this);
				telText.setSingleLine(true);
				final Button telButton = new Button(TelCheckActivity.this);
				telButton.setTextSize(17);
				telButton.setText(R.string.foreign_check_tel_button);
				layout.addView(telText);
				layout.addView(telButton);
				telDialog.setView(layout);
				if (Utils.checkSIMCardState(TelCheckActivity.this)) {
					telButton.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View v) {
							telDialog.cancel();
							AlertDialog.Builder newTelDialog = new AlertDialog.Builder(
									TelCheckActivity.this);
							newTelDialog.setTitle(R.string.check_tel_button);
							newTelDialog
									.setMessage(R.string.foreign_check_tel_text);
							newTelDialog.setPositiveButton(
									R.string.foreign_check_button_text,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											boolean networkState = Utils
													.checkNetworkAvailable(TelCheckActivity.this);
											if (networkState) {
												String foreignCheckPrompt = TelCheckActivity.this
														.getResources()
														.getString(
																R.string.foreign_check_code_request);

												showDialogTimer(
														TelCheckActivity.this,
														0, foreignCheckPrompt,
														30 * 1000,
														new BaseTimerTask() {
															public void run() {
																super.run();
																Message msg = new Message();
																msg.what = SEND_CHECK_MESSAGE_TIMEOUT;
																handler.sendMessage(msg);
															}
														}).show();

												long current_time = System
														.currentTimeMillis();
												// long forgien_check_time =
												// prefs.getLong(CommonData.FOREIGN_CHECK_TIME,
												// 0);
												long forgien_check_time = mSess.mPrefMeme.foreign_check_time
														.getVal();
												long time_ = current_time
														- forgien_check_time;
												if (time_ <= 24 * 60 * 60 * 1000) {
													// String smsNumber =
													// prefs.getString(CommonData.FOREIGN_CHECK_SMS_NUMBER,
													// "");
													// String smsContent =
													// prefs.getString(CommonData.FOREIGN_CHECK_SMS_TEXT,
													// "");
													String smsNumber = mSess.mPrefMeme.foreign_check_sms_number
															.getVal();
													String smsContent = mSess.mPrefMeme.foreign_check_sms_text
															.getVal();
													if (!"".endsWith(smsNumber)) {
														if (Utils
																.checkSIMCardState(TelCheckActivity.this)) {
															boolean sendState = Utils
																	.sendSmsMessage(
																			smsNumber,
																			smsContent);
															Bundle bundle = new Bundle();
															bundle.putBoolean(
																	"send_message_state",
																	sendState);
															Message message = new Message();
															message.setData(bundle);
															message.what = DEAL_SEND_SMS_MESSAGE;
															handler.sendMessage(message);
														} else {
															new AlertDialog.Builder(
																	TelCheckActivity.this)
																	.setTitle(
																			R.string.error)
																	.setMessage(
																			R.string.sim_error)
																	.setPositiveButton(
																			R.string.confirm,
																			new DialogInterface.OnClickListener() {
																				@Override
																				public void onClick(
																						DialogInterface dialog,
																						int which) {
																					dialog.cancel();
																				}
																			})
																	.show();
														}
													}
												} else {
													mSess.getSocketHelper()
															.sendSmsIdentification();
												}
											} else {
												startPromptDialog(
														R.string.prompt,
														R.string.net_not_support);
											}
										}
									});
							newTelDialog.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.cancel();
										}
									});
							newTelDialog.show();
						}
					});
				} else {
					telButton.setBackgroundColor(Color.GRAY);
				}

				if (!"".equals(tel)) {
					telText.setText(tel);
				}
				String getCodeStr = TelCheckActivity.this.getResources()
						.getString(R.string.get_check_code);
				String cancelStr = TelCheckActivity.this.getResources()
						.getString(R.string.cancel);
				telDialog.setButton(getCodeStr,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								String telStr = telText.getText().toString()
										.trim();
								if ("".equals(telStr)
										|| telStr.trim().length() <= 0) {
									startPromptDialog(R.string.prompt,
											R.string.insert_tel_prompt);
								} else {
									String telephone = Utils
											.get11Number(telStr);
									String telValidation = TelCheckActivity.this
											.getResources()
											.getString(
													R.string.regex_telephone_validation);
									Pattern telPattern = Pattern
											.compile(telValidation);
									Matcher telMatcher = telPattern
											.matcher(telephone);
									if (telMatcher.find()) {
										long current_time = System
												.currentTimeMillis();
										// long last_click_time =
										// prefs.getLong(CommonData.TEL_CHECK_CLICK_TIME,
										// 0);
										long last_click_time = mSess.mPrefMeme.tel_check_click_time
												.getVal();
										long time_ = current_time
												- last_click_time;
										if (time_ <= 60000) {
											startPromptDialog(R.string.prompt,
													R.string.check_tel_fast);
										} else {
											String requestCodePrompt = TelCheckActivity.this
													.getResources()
													.getString(
															R.string.request_check_code);

											showDialogTimer(
													TelCheckActivity.this, 0,
													requestCodePrompt,
													30 * 1000,
													new BaseTimerTask() {
														public void run() {
															super.run();
															Message msg = new Message();
															msg.what = TEL_CHECK_TIMEOUT;
															handler.sendMessage(msg);
														}
													}).show();

											mSess.getSocketHelper()
													.sendBindPhone(telephone);
											newTelStr = telephone;
											// if(editor==null){
											// editor = prefs.edit();
											// }
											current_time = System
													.currentTimeMillis();
											// editor.putLong(CommonData.TEL_CHECK_CLICK_TIME,
											// current_time);
											// editor.commit();
											mSess.mPrefMeme.tel_check_click_time
													.setVal(current_time)
													.commit();
										}
									} else {
										startPromptDialog(R.string.error,
												R.string.tel_format_error);
									}
								}
							}
						});
				telDialog.setButton2(cancelStr,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								telDialog.cancel();
							}
						});
				telDialog.show();
				break;
			case R.id.code_confirm_button:
				/*
				 * Toast.makeText(TelCheckActivity.this, "手机号码绑定成功",
				 * Toast.LENGTH_LONG).show(); editor = prefs.edit();
				 * editor.putBoolean(CommonData.OBD, false); editor.commit();
				 * TelCheckActivity.this.finish();
				 */
				String sendCodePrompt = TelCheckActivity.this.getResources()
						.getString(R.string.send_tel_code);
				String check_code = inputText.getText().toString().trim();
				// System.out.println("check_code : "+check_code);
				if (check_code.length() <= 0) {
					startPromptDialog(R.string.prompt,
							R.string.insert_tel_code_outtime);
				} else {
					boolean isStandardCode = isCode(check_code);
					if (isStandardCode) {
						showDialogTimer(TelCheckActivity.this, 0,
								sendCodePrompt, 30 * 1000, new BaseTimerTask() {
									public void run() {
										super.run();
										Message msg = new Message();
										msg.what = BIND_CODE_TIMEOUT;
										handler.sendMessage(msg);
									}
								}).show();

						mSess.getSocketHelper()
								.sendCheckBindPhone(check_code);
					} else {
						startPromptDialog(R.string.prompt,
								R.string.tel_code_format_error);
					}
				}
				break;
			}
		}
	};

	private void dealMessage(String msg) {
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
					// System.out.println("mt:"+type);
					cancelDialogTimer();
					switch (type) {
					case MsgInfor.SERVER_BIND_PHONE:
						d = jo.getInt("d");
						if (0 == d) {
							Message message = new Message();
							message.what = TEL_CHECK_SUCCESS;
							handler.sendMessage(message);
						} else if (1 == d) {
							Message message = new Message();
							message.what = TEL_FORMAT_ERROR;
							handler.sendMessage(message);
						} else if (4 == d) {
							Message message = new Message();
							message.what = TEL_CHECK_REPEAT;
							handler.sendMessage(message);
						} else if (5 == d) {
							Message message = new Message();
							message.what = TEL_HAVE_EXIST;
							handler.sendMessage(message);
						} else if (6 == d) {
							Message message = new Message();
							message.what = TEL_CHECK_BEYOND_LIMIT;
							handler.sendMessage(message);
						} else {
							Message message = new Message();
							message.what = SERVER_BUSYING;
							handler.sendMessage(message);
						}
						break;
					case MsgInfor.SERVER_CHECK_BIND_PHONE:
						d = jo.getInt("d");
						if (0 == d) {
							Message message = new Message();
							message.what = BIND_CODE_SUCCESS;
							handler.sendMessage(message);
							
							//获取我的最新个人信息
							mSess.getSocketHelper().getNewUserinforForLevel();
							
						} else if (1 == d) {
							Message message = new Message();
							message.what = BIND_CODE_ERROR;
							handler.sendMessage(message);
						} else if (2 == d) {
							Message message = new Message();
							message.what = BIND_CODE_INVALID;
							handler.sendMessage(message);
						} else if (5 == d) {
							Message message = new Message();
							message.what = TEL_HAVE_EXIST;
							handler.sendMessage(message);
						} else {
							Message message = new Message();
							message.what = SERVER_BUSYING;
							handler.sendMessage(message);
						}
						break;
					case MsgInfor.SERVER_SMS_IDENTIFICATION:
						String smsNumber = jo.getString("o");
						String smsContent = jo.getString("sn");
						if (!"".equals(smsNumber)) {
							long foreign_check_time = System
									.currentTimeMillis();
							// editor = prefs.edit();
							// editor.putLong(CommonData.FOREIGN_CHECK_TIME,
							// foreign_check_time);
							// editor.putString(CommonData.FOREIGN_CHECK_SMS_NUMBER,
							// smsNumber);
							// editor.putString(CommonData.FOREIGN_CHECK_SMS_TEXT,
							// smsContent);
							// editor.commit();
							mSess.mPrefMeme.foreign_check_time
									.setVal(foreign_check_time);
							mSess.mPrefMeme.foreign_check_sms_number
									.setVal(smsNumber);
							mSess.mPrefMeme.foreign_check_sms_text.setVal(
									smsContent).commit();

							if (Utils.checkSIMCardState(TelCheckActivity.this)) {
								boolean sendState = Utils.sendSmsMessage(
										smsNumber, smsContent);
								Bundle bundle = new Bundle();
								bundle.putBoolean("send_message_state",
										sendState);
								Message message = new Message();
								message.setData(bundle);
								message.what = DEAL_SEND_SMS_MESSAGE;
								handler.sendMessage(message);
							} else {
								new AlertDialog.Builder(TelCheckActivity.this)
										.setTitle(R.string.error)
										.setMessage(R.string.sim_error)
										.setPositiveButton(
												R.string.confirm,
												new DialogInterface.OnClickListener() {
													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														dialog.cancel();
													}
												}).show();
							}
						}
						break;
					}// end switch
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}

			}
		} else {
			String illegalInfor = TelCheckActivity.this.getResources()
					.getString(R.string.illegal_information);
			String message = illegalInfor + msg;
			Toast.makeText(TelCheckActivity.this, message, 1).show();
		}

	}

	// 实现一个 BroadcastReceiver，用于接收指定的 Broadcast
	private class ServerReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = intent.getStringExtra("msg");
			if (msg.trim().length() != 0) {
				handler.post(new Runnable() {
					@Override
					public void run() {
						// Looper.prepare();
						if (Utils.debug)
							Log.w(TAG, "msg:" + msg);
						dealMessage(msg);
						// Looper.loop();
					}
				});
				// Utils.executorService.submit(new Runnable() {
				// @Override
				// public void run() {
				// // Looper.prepare();
				// BLog.w(TAG, "msg:"+msg);
				// dealMessage(msg);
				// // Looper.loop();
				// }
				// });
			}
		}
	}

	// private ServiceConnection serviceConnection = new ServiceConnection() {
	//
	// public void onServiceConnected(ComponentName name, IBinder service) {
	// tuixinService = ((LocalBinder) service).getService();
	// }
	// @Override
	// public void onServiceDisconnected(ComponentName name) {
	// tuixinService = null;
	// }
	// };
	@Override
	protected void onResume() {
		if (serverReceiver == null) {
			serverReceiver = new ServerReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			this.registerReceiver(serverReceiver, filter);
		}
		super.onResume();
	}

	protected void onDestroy() {
		TxData.popActivityRemove(this);
//		TuixinService1.activityState.remove(TAG);
		super.onDestroy();
	}

	@Override
	protected void onRestart() {
		super.onRestart();

	}

	// private MsgHelperReceiver msgreceiver;
	protected void onStart() {
		// System.out.println("......Contacts on start");
		// doQueryContacts();
		// TelCheckActivity.this.getApplicationContext().bindService( new
		// Intent("com.tuixin11sms.tx.TuixinService"),
		// this.serviceConnection, BIND_AUTO_CREATE);
		if (serverReceiver == null) {
			serverReceiver = new ServerReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			this.registerReceiver(serverReceiver, filter);
		}
		// if (msgreceiver == null) {
		// msgreceiver = new MsgHelperReceiver();
		// IntentFilter filter = new IntentFilter();
		// // 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
		// filter.addAction(Constants.INTENT_ACTION_MSGHELPER_MSG);
		// this.registerReceiver(msgreceiver, filter);
		// }
		// TuixinService.activityState.put(TAG, "onStart");
		super.onStart();
	}

	protected void onStop() {
		// if (msgreceiver != null) {
		// this.unregisterReceiver(msgreceiver);
		// msgreceiver = null;
		// }
		cancelDialogTimer();
		if (serverReceiver != null) {
			this.unregisterReceiver(serverReceiver);
			serverReceiver = null;
		}
		// TuixinService.activityState.put(TAG, "onStop");
		// if(TuixinService.checkAllStop()&&tuixinService!=null){
		// // Log.i(TAG, "+++++++++++++++++++++++++++++++++AllStop");
		// tuixinService.showNotification(TelCheckActivity.class);
		// }
		// TelCheckActivity.this.getApplicationContext().unbindService(serviceConnection);
		super.onStop();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {

		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			TelCheckActivity.this.finish();
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public boolean isCode(String num) {
		boolean isStandardCode = true;
		if (num != null) {
			for (int i = 0; i < num.length(); i++) {
				char c = num.charAt(i);
				// if(c!='-' && c!=':' && c!=' ')
				boolean isNum = (c >= '0' && c <= '9') ? true : false;
				boolean isLowChar = (c >= 'a' && c <= 'z') ? true : false;
				boolean isUpChar = (c >= 'A' && c <= 'Z') ? true : false;
				if (isNum || isLowChar || isUpChar) {
					isStandardCode = true;
				} else {
					isStandardCode = false;
					return false;
				}
			}
		} else {
			isStandardCode = false;
		}
		return isStandardCode;
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				TelCheckActivity.this);
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

}
