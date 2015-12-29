package com.tuixin11sms.tx.activity;

import static com.nineoldandroids.view.ViewPropertyAnimator.animate;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.Alarmreceiver;
import com.tuixin11sms.tx.SessionManager.UserLoginInfor;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.db.TxDBContentProvider;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.LocationStation;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class LoginActivity extends BaseActivity {
	private static final String TAG = LoginActivity.class.getSimpleName();
	private static final int REQ_FORGET_PWD = 10;// 忘记密码的请求码
	public static final String NEED_INIT = "needinit";
	private UpdateReceiver updatareceiver;
	private TextView mRegist;
	private Button mLogin;
	private LinearLayout mSinaLogin;
	private EditText mUserId;
	private EditText mPassword;
	private ImageView iv_user_head_img;
	private ImageView iv_forget_pwd;
	private final int NICKNAME_LOGIN_OK = 0;
	private final int NICKNAME_LOGIN_ILLEGAL = 1;
	private final int NICKNAME_LOGIN_PWD_ERROR = 2;
	private final int NICKNAME_LOGIN_FAILE = 3;
	private final int NICKNAME_LOGIN_NICKNAME_ILLEGAL = 4;
	private final int CHECK_TIMEOUT = 5;
	private final int USER_BLOCK = 7; // 账号被封
	private boolean isOnSelectedAccount = false;// 是否在选择账户状态
	private ImageButton ib_show_avatars;
	private RelativeLayout rl_down_container;// 下方的容器
	private HorizontalScrollView hsv_login_avatars;// 显示登录过的账户头像
	private LinearLayout ll_login_avatars;// 头像显示的view
	// public myAdapter adapter;
	// public AvatarAdapter loginAvatarAdapter;// 登陆时多账号的头像
	public ArrayList<UserLoginInfor> usrLoginforList;// 用户登陆信息的list
	// private ListView listView;
	private boolean needinit;// 点击退出登录时跳转到LoginActivity时，按返回键用到了这个变量 2014.01.21
								// shc

	private boolean canClick = true;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);

		// 临时这么处理一下，因为锁屏状态被迫下线后，如果开锁第一个页面是messageActivity，在onResume方法中会刷新view调用数据库，不能在那个方法中关闭数据库
		if (TxDBContentProvider.getmOpenHelper() != null) {
			TxDBContentProvider.closeDB();
		}

		setContentView(R.layout.activity_login);
		Intent intent = this.getIntent();
		needinit = intent.getBooleanExtra(NEED_INIT, false);
		int d = intent.getIntExtra("d", 0);
		
		if (d == 3) {
			Utils.startPromptDialog(LoginActivity.this, R.string.error,
					R.string.pw_request_failed_prompt);

		} else if (d == 2) {
			Utils.startPromptDialog(LoginActivity.this, R.string.error,
					R.string.login_pwd_error);
		} else if (d == 1) {
			Utils.startPromptDialog(LoginActivity.this, R.string.error,
					R.string.login_illegal);
		}
		mRegist = (TextView) findViewById(R.id.mRegist);
		mLogin = (Button) findViewById(R.id.mLogin);
		mUserId = (EditText) findViewById(R.id.et_user_id);
		mPassword = (EditText) findViewById(R.id.mPassword);
		iv_user_head_img = (ImageView) findViewById(R.id.iv_user_head_img);
		ib_show_avatars = (ImageButton) findViewById(R.id.ib_show_avatars);
		rl_down_container = (RelativeLayout) findViewById(R.id.rl_down_container);
		ll_login_avatars = (LinearLayout) findViewById(R.id.ll_login_avatars);

		hsv_login_avatars = (HorizontalScrollView) findViewById(R.id.hsv_login_avatars);// 账户头像父view

		iv_forget_pwd = (ImageView) findViewById(R.id.iv_forget_pwd);
		mSinaLogin = (LinearLayout) findViewById(R.id.mSinaLogin);

		mRegist.setOnClickListener(registClick);
		mLogin.setOnClickListener(loginClick);
		mSinaLogin.setOnClickListener(sinaLoginClick);
		iv_forget_pwd.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent i = new Intent(LoginActivity.this,
						LostPasswordActivity.class);
				startActivityForResult(i, REQ_FORGET_PWD);

			}
		});
		// iv_forget_pwd.setText(Html.fromHtml("<u>" +
		// this.getResources().getString(R.string.regist_forget_password)
		// + "</u>"));
		mUserId.addTextChangedListener(watcher);
		mUserId.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//
				if (isOnSelectedAccount) {
					cancelOnSelectedAccountState();
				}
				mUserId.selectAll();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(v, 0);
				return false;
			}
		});

		mPassword.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				//
				if (isOnSelectedAccount) {
					cancelOnSelectedAccountState();
				}
				mPassword.selectAll();
				InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(v, 0);
				return false;
			}
		});
		if (getIntent().getIntExtra(Utils.AUTOLOGINFAILURE,
				Utils.DEFAULTLOGINFAILURE) == Utils.DISLOGINFAILURE) {
			Toast.makeText(this, "登陆失败", Toast.LENGTH_SHORT).show();
		} else if (getIntent().getIntExtra(Utils.AUTOLOGINFAILURE,
				Utils.DEFAULTLOGINFAILURE) == Utils.FIDLOGINFAILURE) {
			GroupUtils.showChangeFailedDialog(LoginActivity.this,
					"此账号的神聊服务已被禁用，如有疑问，请发送邮件至help@shenliao.com");
		} else {

		}
		initAccountData();

		// 密码一旦被修改，就清楚sp中关于密码的存储
		mPassword.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

				if (Utils.debug)
					Log.i(TAG, "文字改变了：" + s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				if (Utils.debug)
					Log.i(TAG, "原密码：" + s.toString());
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (!isOnSelectedAccount) {
					String muid = mUserId.getText().toString();
					try {
						String prePwd = mSess.mUserLoginInfor
								.getUserLoginPwd(muid);
						if (!TextUtils.isEmpty(prePwd)) {
							if (!prePwd.equals(s.toString())) {
								// 清除账户密码
								mSess.mUserLoginInfor.clearUserPwd(muid);
								if (Utils.debug) {
									Log.i(TAG, "清空密码：" + muid + ",原密码："
											+ prePwd + ",现在的密码：" + s.toString());
								}
							}
						}
					} catch (JSONException e) {
						if (Utils.debug)
							Log.e(TAG, "删除账号异常", e);
					}
				}
				if (Utils.debug)
					Log.i(TAG, "修改后的密码：" + s.toString());
			}
		});

		initAccountAvatarsView();

		// loginAvatarAdapter = new AvatarAdapter();
		// iv_login_avatars.setAdapter(loginAvatarAdapter);
		ib_show_avatars.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

				// final View ll_down_content =
				// findViewById(R.id.ll_down_content);// 下半部分
				// View ll_up_content = findViewById(R.id.ll_up_content);
				// Rect rec_up_content = new Rect();
				// Rect rec_down_content = new Rect();
				// ll_up_content.getGlobalVisibleRect(rec_down_content);
				// ll_down_content.getGlobalVisibleRect(rec_down_content);

				if (isOnSelectedAccount) {
					// 在选择账户状态
					// isOnSelectedAccount = false;
					// ib_show_avatars.startAnimation(Utils.getAnimationRotate(
					// 180, 0));// 下拉箭头还原动画
					//
					// if(Utils.debug)Log.i(TAG,
					// "2rec_down_content.top = "+rec_down_content.top+", Utils.dip2px(75, thisContext)="+Utils.dip2px(75,
					// thisContext));
					// if(Utils.debug)Log.i(TAG,
					// "1rec_up_content.bottom = "+rec_up_content.bottom+", rl_down_container.getHeight()="+rl_down_container.getHeight());
					//
					// animate(ll_down_content).x(0).y(0);

					cancelOnSelectedAccountState();
				} else {
					// 不在选择账户状态
					// // 播放动画，显示账户选择框
					// isOnSelectedAccount = true;// 在选择账户状态
					// v.startAnimation(Utils.getAnimationRotate(0, 180));//
					// 下拉箭头旋转动画
					// Utils.hideSoftInput(txdata, thisContext);//隐藏软键盘
					//
					// if(Utils.debug)Log.i(TAG,
					// "1rec_down_content.top = "+rec_down_content.top+", Utils.dip2px(75, thisContext)="+Utils.dip2px(75,
					// thisContext));
					// if(Utils.debug)Log.i(TAG,
					// "1rec_up_content.bottom = "+rec_up_content.bottom+", rl_down_container.getHeight()="+rl_down_container.getHeight());
					//
					// animate(ll_down_content).x(0).y(ll_login_avatars.getHeight());

					setOnSelectedAccountState();
				}
			}
		});
	}

	// @Override
	// public void onWindowFocusChanged(boolean hasFocus) {
	// super.onWindowFocusChanged(hasFocus);
	// // 设置账号选择框的触摸事件
	// if(Utils.debug)Log.i(TAG, "onWindowFocusChanged");
	// if (hasFocus == true && ll_login_avatars != null) {
	// if(Utils.debug)Log.i(TAG,
	// "onWindowFocusChanged，hasFocus == true && ll_login_avatars != null");
	// findViewById(R.id.ll_login_content).setOnTouchListener(
	// new View.OnTouchListener() {
	//
	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	// if(Utils.debug)Log.i(TAG, "Login页面触摸事件");
	// if (isOnSelectedAccount) {
	// // 在选择账户状态下
	// if (event.getAction() == MotionEvent.ACTION_DOWN) {
	// // 在按下时
	// Rect rec = new Rect();
	// ll_login_avatars.getGlobalVisibleRect(rec);
	// if (!rec.contains((int) event.getRawX(),
	// (int) event.getRawY())) {
	// // 触摸点不再选择账户view区
	// cancelOnSelectedAccountState();
	// return true;
	// }
	// }
	// }
	// return false;
	// }
	// });
	// }
	//
	// }

	/** 取消输入账号状态 */
	private void cancelOnSelectedAccountState() {
		final View ll_down_content = findViewById(R.id.ll_down_content);// 下半部分
		View ll_up_content = findViewById(R.id.ll_up_content);
		Rect rec_up_content = new Rect();
		Rect rec_down_content = new Rect();
		ll_up_content.getGlobalVisibleRect(rec_down_content);
		ll_down_content.getGlobalVisibleRect(rec_down_content);

		isOnSelectedAccount = false;
		ib_show_avatars.startAnimation(Utils.getAnimationRotate(180, 0));// 下拉箭头还原动画

		if (Utils.debug)
			Log.i(TAG,
					"2rec_down_content.top = " + rec_down_content.top
							+ ", Utils.dip2px(75, thisContext)="
							+ Utils.dip2px(75, thisContext));
		if (Utils.debug)
			Log.i(TAG,
					"1rec_up_content.bottom = " + rec_up_content.bottom
							+ ", rl_down_container.getHeight()="
							+ rl_down_container.getHeight());

		animate(ll_down_content).x(0).y(0);

		// 取消蒙版效果
		findViewById(R.id.iv_user_head_img_mask).setVisibility(View.INVISIBLE);// 头像的蒙版
		findViewById(R.id.iv_account_layout_mask).setVisibility(View.INVISIBLE);// 账号的蒙版
		findViewById(R.id.iv_pwd_layout_mask).setVisibility(View.INVISIBLE);// 密码的蒙版
	}

	/** 设置为在输入账号状态下 */
	private void setOnSelectedAccountState() {
		final View ll_down_content = findViewById(R.id.ll_down_content);// 下半部分
		View ll_up_content = findViewById(R.id.ll_up_content);
		Rect rec_up_content = new Rect();
		Rect rec_down_content = new Rect();
		ll_up_content.getGlobalVisibleRect(rec_down_content);
		ll_down_content.getGlobalVisibleRect(rec_down_content);

		// 播放动画，显示账户选择框
		isOnSelectedAccount = true;// 在选择账户状态
		ib_show_avatars.startAnimation(Utils.getAnimationRotate(0, 180));// 下拉箭头旋转动画
		Utils.hideSoftInput(thisContext);// 隐藏软键盘

		if (Utils.debug)
			Log.i(TAG,
					"1rec_down_content.top = " + rec_down_content.top
							+ ", Utils.dip2px(75, thisContext)="
							+ Utils.dip2px(75, thisContext));
		if (Utils.debug)
			Log.i(TAG,
					"1rec_up_content.bottom = " + rec_up_content.bottom
							+ ", rl_down_container.getHeight()="
							+ rl_down_container.getHeight());

		animate(ll_down_content).x(0).y(ll_login_avatars.getHeight());

		// 设置蒙版效果
		findViewById(R.id.iv_user_head_img_mask).setVisibility(View.VISIBLE);// 头像的蒙版
		findViewById(R.id.iv_account_layout_mask).setVisibility(View.VISIBLE);// 账号的蒙版
		findViewById(R.id.iv_pwd_layout_mask).setVisibility(View.VISIBLE);// 密码的蒙版
	}

	// ll_login_avatars

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable arg0) {

		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			// 手动输入账号时，密码置空，头像变为默认
			iv_user_head_img.setImageResource(R.drawable.login_default_head);
			mPassword.setText("");

		}

	};

	private void initAccountData() {
		usrLoginforList = mSess.mUserLoginInfor.getUserInforList();
		UserLoginInfor usrInfor = null;
		if (usrLoginforList.size() == 0) {
			// 没有登录过的账号
			mUserId.setText(null);
			mPassword.setText(null);
		} else {
			usrInfor = usrLoginforList.get(0);
			String uidPwdd = usrInfor.pwd;
			mUserId.setText(usrInfor.partner_id);
			mPassword.setText(uidPwdd);
		}

		if (usrLoginforList.size() == 0) {
			ib_show_avatars.setVisibility(View.GONE);
		}

		mUserId.setSelection(mUserId.getText().length());
		mPassword.setSelection(mPassword.getText().length());

		if (usrInfor != null) {
			readHeadImg(iv_user_head_img, Long.parseLong(usrInfor.partner_id),
					usrInfor.avatar_url, usrInfor.sex, false);
		}

	}

	/** 初始化账户头像view */
	private void initAccountAvatarsView() {
		final LinearLayout ll_login_inner_avatars = (LinearLayout) hsv_login_avatars
				.findViewById(R.id.ll_login_inner_avatars);
		for (final UserLoginInfor usrInfor : usrLoginforList) {

			final View view = View.inflate(thisContext,
					R.layout.sll_item_login_avatar, null);
			// LinearLayout.LayoutParams ll_item_param = new
			// LinearLayout.LayoutParams(
			// Utils.dip2px(60, thisContext),
			// Utils.dip2px(60, thisContext));
			// ll_item_param.setMargins(Utils.dip2px(21, thisContext), 0,
			// Utils.dip2px(21, thisContext), 0);
			// view.setLayoutParams(ll_item_param);
			ImageView iv_avatar = (ImageView) view
					.findViewById(R.id.iv_account_avatar);
			ImageView iv_del = (ImageView) view
					.findViewById(R.id.iv_del_account);

			// final UserLoginInfor usrInfor = usr;
			final String avatar_path = usrInfor.avatar_url;
			final int sex = usrInfor.sex;

			if (!TextUtils.isEmpty(usrInfor.partner_id)) {

				readHeadImg(iv_avatar, Long.parseLong(usrInfor.partner_id),
						avatar_path, sex, true);

				iv_avatar.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// ib_show_avatars.setImageResource(R.drawable.loginselect);

						mUserId.setText(usrInfor.partner_id);
						mPassword.setText(usrInfor.pwd);
						readHeadImg(iv_user_head_img,
								Long.parseLong(usrInfor.partner_id),
								avatar_path, sex, false);
						cancelOnSelectedAccountState();// 取消账号选择状态

						// 直接登录
						login();
					}
				});

				iv_avatar
						.setOnLongClickListener(new View.OnLongClickListener() {

							@Override
							public boolean onLongClick(View v) {
								// 出现删除账号按钮
								if (ll_login_inner_avatars != null) {
									int childCount = ll_login_inner_avatars
											.getChildCount();
									View item = null;
									for (int i = 0; i < childCount; i++) {
										item = ll_login_inner_avatars
												.getChildAt(i);
										item.findViewById(R.id.iv_del_account)
												.setVisibility(View.VISIBLE);
									}
								}
								return true;
							}
						});

				iv_del.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 删除账号
						try {
							mSess.mUserLoginInfor
									.removeLoginUserInfor(usrInfor.partner_id);
							// usrLoginforList.remove(position);//删除该账号信息
							String tempUid = mUserId.getText().toString();// 账号输入框中的账号
							if (!TextUtils.isEmpty(tempUid)
									&& usrInfor.partner_id.equals(tempUid)) {
								// 如果删除的账号就是当前显示的账号，则清空当前账号框中的显示，头像恢复默认
								mUserId.setText(null);
								mPassword.setText(null);
								iv_user_head_img
										.setImageResource(R.drawable.login_default_head);
							}
							ll_login_inner_avatars.removeView(view);
							// handler.sendEmptyMessage(REFRESH_DATA);
						} catch (JSONException e) {
							if (Utils.debug)
								Log.e(TAG, "删除用户登陆信息异常", e);
						}
					}
				});

				ll_login_inner_avatars.addView(view);

			} else {
				if (Utils.debug)
					Log.i(TAG, "有一个用户id无效：" + usrInfor.partner_id);
			}

		}

	}

	/*
	 * CompoundButton.OnCheckedChangeListener showPasswordChecked = new
	 * OnCheckedChangeListener() {
	 * 
	 * @Override public void onCheckedChanged(CompoundButton buttonView, boolean
	 * isChecked) { if (isChecked) { // display password text, for example
	 * "123456"
	 * mPassword.setTransformationMethod(HideReturnsTransformationMethod
	 * .getInstance()); } else { // hide password, display "."
	 * mPassword.setTransformationMethod
	 * (PasswordTransformationMethod.getInstance());
	 * 
	 * } mPassword.postInvalidate();
	 * 
	 * } };
	 */

	private OnClickListener sinaLoginClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			if (!Utils.checkNetworkAvailable(LoginActivity.this)) {
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.seach_network_title);
				return;
			}
			// Long userid = prefs.getLong(CommonData.WEIBO_SHENLIAO_LOGIN_ID,
			// 0);
			// if(userid == 0){
			Intent iOauth = new Intent(LoginActivity.this,
					WebViewActivity.class);
			iOauth.putExtra(WebViewActivity.LOGIN_STATE,
					WebViewActivity.LOGIN_SINA);
			startActivity(iOauth);
		}
	};

	private View.OnClickListener loginClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {

			login();

		}
	};

	// 点击登陆按钮登陆
	private void login() {

		if (!Utils.checkNetworkAvailable(LoginActivity.this)) {
			Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
					R.string.seach_network_title);
			return;
		}
		if (!canClick) {
			return;
		}
		String userId = mUserId.getText().toString().trim();
		String pwd = mPassword.getText().toString().trim();
		int len = userId.length();
		int plen = pwd.length();
		if (len <= 0) {
			showToast(R.string.insert_username_prompt);
		} else if (plen <= 0) {
			showToast(R.string.insert_pwd_prompt);
		} else if (plen < 6) {
			Utils.startPromptDialog(LoginActivity.this,
					R.string.login_pwd_too_show, R.string.pwd_too_short);
		} else if (plen > 20) {
			Utils.startPromptDialog(LoginActivity.this,
					R.string.login_pwd_too_long, R.string.pwd_too_long);
		} else {
			canClick = false;
			String logining = LoginActivity.this.getResources().getString(
					R.string.login_logining);

			ProgressDialog dialog = showDialogTimer(LoginActivity.this, 0,
					logining, 30 * 1000, new BaseTimerTask() {
						public void run() {
							super.run();
							Message msg = new Message();
							msg.what = CHECK_TIMEOUT;
							handler.sendMessage(msg);
						}
					});

			dialog.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(DialogInterface dialog, int keyCode,
						KeyEvent event) {
					if (keyCode == KeyEvent.KEYCODE_BACK) {
						canClick = true;
						cancelTimer();
					}
					return false;
				}
			});
			dialog.show();
			if (Utils.debug)
				Log.w(TAG, "点击登陆按钮login");

			mSess.saveTempPwd(pwd);// 临时保存用户密码和是否记住密码状态

			mSess.setLoginInfor(userId, pwd);
			mSess.setLoginSuccessed(false);
			// getEditorMeme().putString(CommonData.DOOR, "");
			// getEditorMeme().commit();
			mSess.mPrefMeme.door.setVal("").commit();

			mSess.getSocketHelper().sendPing();
			Utils.hideSoftInput(LoginActivity.this);
		}

	}

	private View.OnClickListener registClick = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			Intent i = new Intent(LoginActivity.this, RegistActivity.class);
			i.putExtra(NEED_INIT, true);
			startActivity(i);
		}
	};

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_FORGET_PWD && resultCode == 10) {
			mUserId.setText(data.getExtras().getString("result"));
		}
	}

	public void onResume() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			// filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			filter.addAction(Constants.INTENT_ACTION_LOGIN_RSP);
			this.registerReceiver(updatareceiver, filter);
		}
		// Utils.popSoftInput(txdata);
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
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
		// stopAsyncload();
		TxData.popActivityRemove(this);
		cancelDialog();
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.i(TAG, keyCode + "+++++++++" + event);
		//
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (needinit) {
				Utils.broadcastExitMsg(LoginActivity.this);
				Alarmreceiver.isexit = true;
				if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
					Intent intent2 = new Intent(Intent.ACTION_MAIN);
					intent2.addCategory(Intent.CATEGORY_HOME);
					intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivity(intent2);
					LocationStation.gpsCancel();
					android.os.Process.killProcess(android.os.Process.myPid());

				} else {
					ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
					am.restartPackage(getPackageName());
					System.exit(0);
				}
				return true;
			}

		}
		return super.onKeyDown(keyCode, event);
	}

	private Handler handler = new WrappedHandler(thisContext) {
		@Override
		public void handleMessage(Message msg) {
			canClick = true;
			cancelDialog();
			int num = msg.what;
			switch (num) {
			case NICKNAME_LOGIN_ILLEGAL:
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.login_illegal);
				// SocketHelper.closeSocketConnect();
				break;
			// 2
			case NICKNAME_LOGIN_PWD_ERROR:
				// editor.putString(CommonData.INPUTPASSWORD, "");
				// editor.commit();
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.login_pwd_error);
				// SocketHelper.closeSocketConnect();
				break;
			// 3
			case NICKNAME_LOGIN_FAILE:
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.pw_request_failed_prompt);
				// SocketHelper.closeSocketConnect();
				break;
			// 4
			case NICKNAME_LOGIN_NICKNAME_ILLEGAL:
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.login_nickname_illegal);
				// SocketHelper.closeSocketConnect();
				break;
			case USER_BLOCK:
				Utils.startPromptDialog(LoginActivity.this, R.string.prompt,
						R.string.user_block);
				break;
			case NICKNAME_LOGIN_OK:

				Intent iMainTab = new Intent(LoginActivity.this,
						TuiXinMainTab.class);
				startActivity(iMainTab);
				LoginActivity.this.finish();

				break;
			case CHECK_TIMEOUT:
				Utils.startPromptDialog(LoginActivity.this,
						R.string.login_failed, R.string.login_failed_again);

				break;
			// case REFRESH_DATA:
			// loginAvatarAdapter.notifyDataSetChanged();
			// break;
			}
		}
	};

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_LOGIN_RSP.equals(intent.getAction())) {
				if (serverRsp != null) {
					switch (serverRsp.getStatusCode()) {
					// 这里的枚举导包了
					case USER_BLOCK: {
						Message msg = new Message();
						msg.what = USER_BLOCK;
						handler.sendMessage(msg);
						break;
					}
					case LOGIN_ACCOUNT_NO_EXIST: {
						Message msg = new Message();
						msg.what = NICKNAME_LOGIN_ILLEGAL;
						handler.sendMessage(msg);
						break;
					}
					case LOGIN_NICK_PWD_ERROR: {
						Message msg = new Message();
						msg.what = NICKNAME_LOGIN_PWD_ERROR;
						handler.sendMessage(msg);
						break;
					}
					case LOGIN_OPT_FAIELD: {
						Message msg = new Message();
						msg.what = NICKNAME_LOGIN_FAILE;
						handler.sendMessage(msg);
						break;
					}
					case LOGIN_NICK_INVALID: {
						Message msg = new Message();
						msg.what = NICKNAME_LOGIN_NICKNAME_ILLEGAL;
						handler.sendMessage(msg);
						break;
					}
					case RSP_OK: {
						Message msg = new Message();
						msg.what = NICKNAME_LOGIN_OK;
						handler.sendMessage(msg);
						break;
					}
					default:
						break;
					}
				}
			}
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				if (avImages != null && avImages.size() > 0) {
					for (ImageView iv : avImages) {
						long tag = (Long) iv.getTag();
						long id = (Long) result[1];
						if (tag == id) {
							iv.setImageBitmap((Bitmap) result[0]);
						}
					}
				}

				break;
			}
			super.handleMessage(msg);
		}
	};
	private Handler avatarHandler_big = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				if (result[0] != null)
					iv_user_head_img.setImageBitmap((Bitmap) result[0]);
				break;
			}
			super.handleMessage(msg);
		}
	};

	/** 头像显示adapter */
	// private class AvatarAdapter extends PagerAdapter {
	//
	// @Override
	// public int getCount() {
	// return usrLoginforList.size();
	// }
	//
	// @Override
	// public Object instantiateItem(ViewGroup container, int position) {
	// View view = View.inflate(thisContext,
	// R.layout.sll_item_login_avatar, null);
	// ImageView iv_avatar = (ImageView) view
	// .findViewById(R.id.iv_account_avatar);
	// ImageView iv_del = (ImageView) view
	// .findViewById(R.id.iv_del_account);
	//
	// final UserLoginInfor usrInfor = usrLoginforList.get(position);
	// String avatar_path = usrInfor.avatar_url;
	// int sex = usrInfor.sex;
	//
	// if (!TextUtils.isEmpty(usrInfor.partner_id)) {
	//
	// readHeadImg(iv_avatar, Long.parseLong(usrInfor.partner_id),
	// avatar_path, sex);
	//
	// iv_avatar.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // ib_show_avatars.setImageResource(R.drawable.loginselect);
	//
	// mUserId.setText(usrInfor.partner_id);
	// String tempPwd = usrInfor.pwd;
	// mPassword.setText(tempPwd);
	//
	// }
	// });
	// iv_del.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // 删除账号
	// try {
	// mSess.mUserLoginInfor
	// .removeLoginUserInfor(usrInfor.partner_id);
	// // usrLoginforList.remove(position);//删除该账号信息
	// String tempUid = mUserId.getText().toString();// 账号输入框中的账号
	// if (!TextUtils.isEmpty(tempUid)
	// && usrInfor.partner_id.equals(tempUid)) {
	// // 如果删除的账号就是当前显示的账号，则清空当前账号框中的显示
	// mUserId.setText(null);
	// mPassword.setText(null);
	// }
	// handler.sendEmptyMessage(REFRESH_DATA);
	// } catch (JSONException e) {
	// if (Utils.debug)
	// Log.e(TAG, "删除用户登陆信息异常", e);
	// }
	//
	// }
	// });
	// }
	//
	//
	// return view;
	// }
	//
	// @Override
	// public boolean isViewFromObject(View view, Object object) {
	// return view.equals(object);
	// }
	//
	//
	//
	// }

	private List<ImageView> avImages = new ArrayList<ImageView>();

	public void readHeadImg(final ImageView iv_avatar, long tx_partner_id,
			String avatar_path, int sex, boolean isSmall) {
		Bitmap bm = null;

		if (iv_avatar != null) {
			if (!Utils.isIdValid(tx_partner_id))
				return;
			if (TX.TUIXIN_MAN == tx_partner_id) {
				iv_avatar.setImageResource(R.drawable.tuixin_manage);
				iv_avatar.setTag(tx_partner_id);
				return;
			}

			if (sex == TX.MALE_SEX) {
				iv_avatar.setImageResource(R.drawable.user_infor_head_boy);
			} else {
				iv_avatar.setImageResource(R.drawable.user_infor_head_girl);
			}

			bm = mSess.avatarDownload.getPartnerCachedBitmap(tx_partner_id);
			if (bm != null) {
				iv_avatar.setImageBitmap(bm);
			} else {
				if (!Utils.checkSDCard()) {// 无SD卡
					// iv_avatar.setImageResource(R.drawable.no_sd_img);
					return;
				}
				if (Utils.isNull(avatar_path)) {
					return;
				}

				iv_avatar.setTag(tx_partner_id);
				avImages.add(iv_avatar);

				if (Utils.debug) {
					Log.i(TAG, "tx_partner_id=" + tx_partner_id + "==sex=="
							+ sex);
				}

				if (isSmall) {
					mSess.avatarDownload.downAvatar(avatar_path, tx_partner_id,
							iv_avatar, avatarHandler);

				} else {
					mSess.avatarDownload.downAvatar(avatar_path, tx_partner_id,
							iv_avatar, avatarHandler_big);
				}

			}

		}

	}
	// class myAdapter extends BaseAdapter {
	// LayoutInflater mInflater;
	// LinkedList<Holder> mViewLines = new LinkedList<Holder>();//
	// 记录创建的viewholder
	//
	// public myAdapter() {
	// mInflater = LayoutInflater.from(LoginActivity.this);
	// // account = (String[]) list.keySet().toArray(new String[0]);
	// }
	//
	// @Override
	// public int getCount() {
	// return usrLoginforList.size();
	// }
	//
	// @Override
	// public Object getItem(int position) {
	// return null;
	// }
	//
	// @Override
	// public long getItemId(int position) {
	// //
	// return position;
	// }
	//
	// @Override
	// public View getView(final int position, View convertView, ViewGroup
	// parent) {
	// //
	// Holder holder = null;
	// if (convertView == null) {
	// convertView = mInflater.inflate(R.layout.popup, null);
	// holder = new Holder();
	// holder.view = (TextView) convertView.findViewById(R.id.mTX);
	// holder.userHead = (ImageView) convertView.findViewById(R.id.mLog_head);
	// holder.del = (ImageView) convertView.findViewById(R.id.mDel);
	// convertView.setTag(holder);
	// mViewLines.add(holder);
	// } else {
	// holder = (Holder) convertView.getTag();
	// }
	// if (holder != null) {
	// // holder.mUserId = Long.valueOf(account[position]);
	// final long currentId =
	// Long.parseLong(usrLoginforList.get(position).partner_id);
	// holder.mUserId = currentId;
	// convertView.setId(position);
	// // holder.setId(position);
	// // String avatar_path =
	// // prefs.getString(CommonData.AVATAR_URL_USER+
	// // Long.valueOf(account[position]), "");
	// // int
	// // sex=prefs.getInt(CommonData.SEX_USER+Long.valueOf(account[position]),
	// // 0);
	// String avatar_path = usrLoginforList.get(position).avatar_url;
	// int sex = usrLoginforList.get(position).sex;
	//
	// readHeadImg(holder.userHead, holder.mUserId, avatar_path, sex);
	//
	// holder.view.setText("" + holder.mUserId);
	// convertView.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// pop.dismiss();
	// ib_show_avatars.setImageResource(R.drawable.loginselect);
	//
	// mUserId.setText("" + usrLoginforList.get(position).partner_id);
	// String tempPwd = usrLoginforList.get(position).pwd;
	// mPassword.setText(tempPwd);
	//
	// }
	// });
	// holder.del.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// Utils.creatDialog(thisContext, R.string.login_clear_content,
	// R.string.login_clear_title,
	// R.string.confirm, new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// // 删除账号
	// dialog.dismiss();
	//
	// try {
	// mSess.mUserLoginInfor.removeLoginUserInfor(""+currentId);
	// // usrLoginforList.remove(position);//删除该账号信息
	// String tempUid = mUserId.getText().toString();//账号输入框中的账号
	// if (!TextUtils.isEmpty(tempUid)&&currentId == Long.parseLong(tempUid)) {
	// // 如果删除的账号就是当前显示的账号，则清空当前账号框中的显示
	// mUserId.setText(null);
	// mPassword.setText(null); }
	// handler.sendEmptyMessage(REFRESH_DATA);
	// } catch (JSONException e) {
	// if (Utils.debug)
	// Log.e(TAG, "删除用户登陆信息异常", e);
	// }
	//
	// }
	// }, R.string.cancel, new DialogInterface.OnClickListener() {
	//
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.cancel();
	//
	// }
	// });
	//
	// }
	// });
	//
	// }
	// return convertView;
	// }
	//
	// class Holder {
	// ImageView userHead;
	// TextView view;
	// ImageView del;
	// long mUserId;
	//
	// }
	//
	// public void readHeadImg(ImageView headView1, long tx_partner_id, String
	// tx_avatar, int sex) {
	//
	// Bitmap bm = null;
	// if (headView1 != null) {
	// if (!Utils.isIdValid(tx_partner_id))
	// return;
	// if (TX.TUIXIN_MAN == tx_partner_id) {
	// headView1.setImageResource(R.drawable.tuixin_manage);
	// headView1.setTag(tx_partner_id);
	// return;
	// }
	//
	// bm = getPartnerCachedBitmap(tx_partner_id);
	// if (bm != null) {
	// headView1.setImageBitmap(bm);
	// } else {
	//
	// if (sex == TX.MALE_SEX) {
	// headView1.setImageResource(R.drawable.tui_con_photo);
	// } else {
	// headView1.setImageResource(R.drawable.sl_regist_head_femal);
	// }
	// if (!Utils.checkSDCard()) {// 无SD卡
	// headView1.setImageResource(R.drawable.no_sd_img);
	// return;
	// }
	// if (Utils.isNull(tx_avatar)) {
	// return;
	// }
	// headView1.setTag(tx_partner_id);
	// if (Utils.debug) {
	// Log.i(TAG, "tx_partner_id=" + tx_partner_id);
	// }
	//
	// loadHeadImg(tx_avatar, tx_partner_id, new AsyncCallback<Bitmap>() {
	// @Override
	// public void onSuccess(Bitmap result, long id) {
	//
	// if (result != null) {
	// for (Holder holder : mViewLines) {
	//
	// if (holder.mUserId == id && holder.userHead != null) {
	// holder.userHead.setImageBitmap(result);
	//
	// }
	// }
	// }
	// }
	//
	// @Override
	// public void onFailure(Throwable t, long id) {
	// }
	// });
	// }
	//
	// }
	// }
	//
	// }

}
