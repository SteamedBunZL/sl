package com.tuixin11sms.tx.activity;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Looper;
import android.view.KeyEvent;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.ShenliaoOtherLoginService;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.weibo.net.AccessToken;
import com.weibo.net.DialogError;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboDialogListener;
import com.weibo.net.WeiboException;

public class WebViewActivity extends BaseActivity {

	public static final String LOGIN_STATE = "loginState";
	public static final int LOGIN_NORMAL = 1;
	public static final int LOGIN_SINA = 2;
	public static final int TO_SHARE_GROUP = 3;

	public static final String SHARE_GROUP = "share_group";

	private final String callBackStr = "http://www.shenliao.com";
	private final String changePwd = "http://account.weibo.com/settings/password";
	private final String authUrl = "http://api.t.sina.com.cn/oauth/authenticate?oauth_token=";
	private SharedPreferences prefs;
	private int currentState;
	private Editor editor;
	private LoginReceiver receiver;
	private TxGroup txGroup;

	private TextView mTitle;
	private WebView wv;
	private String url;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.web_view_oauth);
		wv = (WebView) findViewById(R.id.webOauth);
		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		editor = prefs.edit();
		mTitle = (TextView) findViewById(R.id.con_info_title_name);

		Intent i = this.getIntent();
		if (i != null) {
			currentState = i.getIntExtra(LOGIN_STATE, 1);
			if (currentState == LOGIN_SINA) {
				mTitle.setText(R.string.login_with_sina);
			}
			if (currentState == TO_SHARE_GROUP) {
				txGroup = i.getParcelableExtra(SHARE_GROUP);
			}
		}
		CookieSyncManager.createInstance(this);
		// 清除缓存，巨坑爹
		CookieManager cm = CookieManager.getInstance();
		cm.removeSessionCookie();

		Weibo weibo = Weibo.getInstance();
		weibo.setupConsumerConfig(Weibo.APP_KEY, Weibo.APP_SECRET);
		// Oauth2.0
		// 隐式授权认证方式
		weibo.setRedirectUrl(callBackStr);// 此处回调页内容应该替换为与appkey对应的应用回调页

		weibo.authorize(WebViewActivity.this, new AuthDialogListener());
	}

	class AuthDialogListener implements WeiboDialogListener {
		@Override
		public void onComplete(Bundle values) {

			String token = values.getString("access_token");
			String uid = values.getString("uid");
			String expires_in = values.getString("expires_in");
			AccessToken accessToken = new AccessToken(token, Weibo.APP_SECRET);
			accessToken.setExpiresIn(expires_in);
			Weibo.getInstance().setAccessToken(accessToken);

			Intent intent = null;
			switch (currentState) {
			case LOGIN_NORMAL:
				intent = new Intent(WebViewActivity.this,
						WeiboCardActivity.class);
				intent.putExtra(LOGIN_STATE, LOGIN_NORMAL);
				break;
			case TO_SHARE_GROUP:
				intent = new Intent(WebViewActivity.this,
						OAuthShareWeiboActivity.class);
				intent.putExtra(LOGIN_STATE, TO_SHARE_GROUP);
				intent.putExtra(SHARE_GROUP, txGroup);
				break;
			}
			if (currentState == LOGIN_SINA) {
//				editor.putString(CommonData.WEIBO_TOKEN, accessToken.getToken())
//						.putString(CommonData.WEIBO_TOKEN_SECRET,
//								accessToken.getSecret())
//						.putString(CommonData.WEIBO_USER_ID, "" + uid)
//						.putString(
//								CommonData.NICKNAME,
//								WebViewActivity.this
//										.getString(R.string.login_syn_sina))
//						.putLong(
//								CommonData.WEIBO_OVER_TIME,
//								System.currentTimeMillis()
//										+ Integer.parseInt(expires_in) * 1000)
//						.commit();
				mSess.mPrefMeme.weibo_token.setVal(accessToken.getToken());
				mSess.mPrefMeme.weibo_token_secret.setVal(accessToken.getSecret());
				mSess.mPrefMeme.weibo_user_id.setVal("" + uid);
				mSess.mPrefMeme.nickname.setVal(WebViewActivity.this.getString(R.string.login_syn_sina));
				mSess.mPrefMeme.weibo_over_time.setVal(System.currentTimeMillis()+ Integer.parseInt(expires_in) * 1000);
				
				loginShenliao("" + uid, accessToken.getToken(),
						accessToken.getSecret(),
						SessionManager.SINA_ACCOUNT,
						SessionManager.OAUTH_TWO);
				return;
			} else {
				editor.putString(
						CommonData.WEIBO_TOKEN + "�" + TX.tm.getUserID(),
						accessToken.getToken())
						.putString(
								CommonData.WEIBO_TOKEN_SECRET + "�"
										+ TX.tm.getUserID(),
								accessToken.getSecret())
						.putString(
								CommonData.WEIBO_USER_ID + "�"
										+ TX.tm.getUserID(), "" + uid)
						.putLong(
								CommonData.WEIBO_OVER_TIME + "�"
										+ TX.tm.getUserID(),
								System.currentTimeMillis()
										+ Integer.parseInt(expires_in) * 1000)
						.commit();
			}
			Toast.makeText(WebViewActivity.this,
					R.string.oauth_shenliao_success, Toast.LENGTH_SHORT).show();
			WebViewActivity.this.finish();
			WebViewActivity.this.startActivity(intent);

			/*
			 * Intent intent = new Intent();
			 * intent.setClass(AuthorizeActivity.this, TestActivity.class);
			 * startActivity(intent);
			 */
		}

		@Override
		public void onError(DialogError e) {
			Toast.makeText(getApplicationContext(),
					"Auth error : " + e.getMessage(), Toast.LENGTH_LONG).show();
		}

		@Override
		public void onCancel() {
			WebViewActivity.this.finish();
		}

		@Override
		public void onWeiboException(WeiboException e) {
			Toast.makeText(getApplicationContext(),
					"Auth exception : " + e.getMessage(), Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		CookieSyncManager.getInstance().stopSync();
	}

	private void loginShenliao(String weibo_user_id, String weibo_token,
			String weibo_token_secret, int accountType, int authType) {
//		editor.putString(CommonData.DOOR, "");
//		editor.commit();
		mSess.mPrefMeme.door.setVal("").commit();
		SessionManager.getInstance()
				.setWeiboAuto(false);
		SessionManager.getInstance()
				.setOtherAccountLoginInfor(weibo_user_id, weibo_token,
						accountType, authType);
		mSess.getSocketHelper().sendPing();

		ProgressDialog progress = showDialogTimer(this, 0,
				R.string.login_shenliao, 10 * 1000, new BaseTimerTask() {
					@Override
					public void run() {
						super.run();
						Looper.prepare();
						mSess.getSocketHelper().recovery();
						AlertDialog.Builder dialog = new AlertDialog.Builder(
								WebViewActivity.this);
						dialog.setTitle(R.string.prompt);
						dialog.setMessage(R.string.login_sina_faild_timeout);
						dialog.setPositiveButton(R.string.login_again,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
//										String token = prefs.getString(
//												CommonData.WEIBO_TOKEN, "");
//										String tokenSecret = prefs.getString(
//												CommonData.WEIBO_TOKEN_SECRET,
//												"");
//										String weiboUserId = prefs.getString(
//												CommonData.WEIBO_USER_ID, "");
										String token = mSess.mPrefMeme.weibo_token.getVal();
										String tokenSecret = mSess.mPrefMeme.weibo_token_secret.getVal();
										String weiboUserId = mSess.mPrefMeme.weibo_user_id.getVal();
										loginShenliao(
												weiboUserId,
												token,
												tokenSecret,
												SessionManager.SINA_ACCOUNT,
												SessionManager.OAUTH_TWO);
									}
								});
						dialog.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
										WebViewActivity.this.finish();
									}
								});
						dialog.setOnKeyListener(new OnKeyListener() {

							@Override
							public boolean onKey(DialogInterface dialog,
									int keyCode, KeyEvent event) {
								if (keyCode == KeyEvent.KEYCODE_BACK) {
									WebViewActivity.this.finish();
								}
								return false;
							}
						});
						dialog.show();
						Looper.loop();
					}
				});
		progress.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_BACK) {
					WebViewActivity.this.finish();
				}
				return false;
			}
		});
		progress.show();

	}

	class JavaScriptInterface {
		public void getHTML(String html) {
			if (html.toLowerCase().contains("sorry")) {
				if (!Utils.isNull(url)) {
					

				}

			}
			/*
			 * String pin = getPin(html); if(pin != null && !"".equals(pin)){
			 * System.out.println("dddddddddddddd="+pin); // 这里就获取到了我们想要的pin码 //
			 * 这个pin码就是oauth_verifier值,用来进一步获取Access Token和Access Secret用 }
			 */
		}
	}

	public class LoginReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_LOGIN_RSP.equals(intent.getAction())) {
				dealLogin(serverRsp);
			}
		}

	}

	@Override
	protected void onStop() {
		super.onStop();

		if (receiver != null) {
			this.unregisterReceiver(receiver);
			receiver = null;
		}
	}

	@Override
	protected void onStart() {
		super.onStart();
		if (receiver == null) {
			receiver = new LoginReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_LOGIN_RSP);
			this.registerReceiver(receiver, filter);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		CookieSyncManager.getInstance().startSync();
	}

	private void dealLogin(ServerRsp serverRsp) {
		if (serverRsp != null) {
			cancelDialogTimer();
//			final String token = prefs.getString(CommonData.WEIBO_TOKEN, "");
//			final String tokenSecret = prefs.getString(
//					CommonData.WEIBO_TOKEN_SECRET, "");
//			final String weiboUserId = prefs.getString(
//					CommonData.WEIBO_USER_ID, "");
//			final Long overTime = prefs.getLong(CommonData.WEIBO_OVER_TIME, 0);
			final String token = mSess.mPrefMeme.weibo_token.getVal();
			final String tokenSecret = mSess.mPrefMeme.weibo_token_secret.getVal();
			final String weiboUserId = mSess.mPrefMeme.weibo_user_id.getVal();
			final Long overTime = mSess.mPrefMeme.weibo_over_time.getVal();
			switch (serverRsp.getStatusCode()) {
			case RSP_OK:
				// 成功登录后把神聊ID记录下来
//				editor.putLong(CommonData.WEIBO_SHENLIAO_LOGIN_ID,TX.tm.getUserID())
				
				editor.putString(CommonData.WEIBO_TOKEN + "�"+ TX.tm.getUserID(), token)
						.putString(CommonData.WEIBO_TOKEN_SECRET + "�"+ TX.tm.getUserID(), tokenSecret)
						.putString(CommonData.WEIBO_USER_ID + "�"+ TX.tm.getUserID(), "" + weiboUserId)
						.putLong(CommonData.WEIBO_OVER_TIME + "�"+ TX.tm.getUserID(), overTime)
//						.putBoolean(CommonData.WEIBO_NEW,serverRsp.getBoolean(CommonData.WEIBO_NEW,false))
						.commit();
				mSess.mPrefMeme.weibo_shenliao_login_id.setVal(TX.tm.getUserID());
				mSess.mPrefMeme.weibo_new.setVal(false).commit();
				// 得到是否是第一次使用微博账号登录
				if (serverRsp.getBoolean(PrefsMeme.WEIBO_NEW, false)) {
					// 起一个service从新浪得到个人资料，并进行上传资料操作
					Intent iOther = new Intent(WebViewActivity.this,
							ShenliaoOtherLoginService.class);
					iOther.putExtra(LOGIN_STATE, LOGIN_SINA);
					startService(iOther);
				}
				Intent mainIntent = new Intent(WebViewActivity.this,
						TuiXinMainTab.class);
				WebViewActivity.this.finish();
				startActivity(mainIntent);
				break;
			default:
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						WebViewActivity.this);
				dialog.setTitle(R.string.prompt);
				dialog.setMessage(R.string.login_failed_again);
				dialog.setPositiveButton(R.string.login_again,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								dialog.cancel();
								loginShenliao(weiboUserId, token, tokenSecret,
										SessionManager.SINA_ACCOUNT,
										SessionManager.OAUTH_TWO);
							}
						});
				dialog.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								WebViewActivity.this.finish();
							}
						});
				dialog.setOnKeyListener(new OnKeyListener() {

					@Override
					public boolean onKey(DialogInterface dialog, int keyCode,
							KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							WebViewActivity.this.finish();
						}
						return false;
					}
				});
				dialog.show();
				break;
			}
		}
	}

	public String getPin(String html) {
		String ret = "";
		String regEx = "[0-9]{6}";
		Pattern p = Pattern.compile(regEx);
		Matcher m = p.matcher(html);
		boolean result = m.find();
		if (result) {
			ret = m.group(0);
		}
		return ret;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		cancelDialogTimer();
	}

}
