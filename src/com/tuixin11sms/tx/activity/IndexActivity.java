package com.tuixin11sms.tx.activity;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

//import net.youmi.android.AdManager;
//import net.youmi.android.spot.SpotDialogListener;
//import net.youmi.android.spot.SpotManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;

import com.baidu.location.f;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.core.LogUtils;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class IndexActivity extends BaseActivity {
	private static final String TAG = IndexActivity.class.getSimpleName();
	private ProgressDialog progress;
	private final int RESULT_DELAPP = 120;
	private boolean isFirst;
	private Long dsf;
	private Handler mHandler = new Handler();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utils.debug)
			Log.i(TAG, "onCreate()");
		setContentView(R.layout.index);
		TxData.addActivity(this);
		MobclickAgent.updateOnlineConfig(this);
		MobclickAgent.setDebugMode(true);
		// AdManager.getInstance(this).init("77e399dd144ceba7",
		// "77cbe8256305f4d6", false);
		// initSpotManager();
		// dsf = mSess.mPrefMeme.day_login_time.getVal();
		// if (isSpotShow()) {
		// soptShow();
		// } else {
		init();
		// }
	}

	// public boolean isSpotShow() {
	// if (isTodayFirstStart()) {
	// SpotManager.getInstance(IndexActivity.this).loadSpotAds();
	// return false;
	// } else {
	// if (isTimeOverOneHour()) {
	// return true;
	// } else {
	// return false;
	// }
	// // return true;
	// }
	// }

	// public void soptShow() {
	// SpotManager.getInstance(IndexActivity.this).showSpotAds(this,
	// new SpotDialogListener() {
	// @Override
	// public void onShowSuccess() {
	// mHandler.postDelayed(new Runnable() {
	// @Override
	// public void run() {
	// init();
	// }
	// }, 6000);
	// }
	//
	// @Override
	// public void onShowFailed() {
	// mHandler.postDelayed(new Runnable() {
	//
	// @Override
	// public void run() {
	// init();
	// }
	// }, 6000);
	// }
	//
	// });
	// }

	public void init() {
		// //第一次启动 跑到欢迎页index.jpg
		LogUtils.markDay(getApplicationContext()); // 记录日期标志
		String sf = mSess.mPrefMeme.start_first.getVal();

		progress = new ProgressDialog(IndexActivity.this);
		// 是第一次登录
		if ("".equals(sf)) {
			String log = "TuiXin startFirst";
			String data = LogUtils.makeLogStr(log);
			LogUtils.logFileOperate(getApplicationContext(), data);
			isFirst = true;
			// 不再这里第一次读取本地联系人，在初始化TXManager的时候读取
			// Utils.executorService.submit(new Runnable() {
			// @Override
			// public void run() {
			// ContactAPI contactapi = ContactAPI.getAPI();
			// contactapi.setCr(getContentResolver());
			// contactapi.SyncContacts(txdata, IndexActivity.this, true);
			// }
			// });
		} else {
			getApplicationContext().startService(
					new Intent(IndexActivity.this, TuixinService1.class));
		}
		checkTuixinApp();
	}

	/**
	 * 初始化插播广告的管理器
	 */
	// public void initSpotManager() {
	// // SpotManager.getInstance(this).loadSpotAds();
	// SpotManager.getInstance(this).setSpotTimeout(6000);
	// SpotManager.getInstance(this).setAutoCloseSpot(true);// 设置自动关闭插屏开关
	// SpotManager.getInstance(this).setCloseTime(5000); // 设置关闭插屏时间
	// }

	public boolean isTimeOverOneHour() {
		int time = (int) ((System.currentTimeMillis() - dsf) / 1000 / 60 / 60);
		return time >= 1 ? true : false;
	}

	public boolean isTodayFirstStart() {
		long currentTime = System.currentTimeMillis();
		Date now = new Date(currentTime);
		Date dsfDate = new Date(dsf);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String todayStr = sdf.format(now);

		java.util.Date parse;
		try {
			parse = sdf.parse(todayStr);
			long time = parse.getTime() - dsfDate.getTime();
			if (time > 0) {
				return true;
			} else {
				return false;
			}
		} catch (ParseException e) {
			return false;
		}

	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == RESULT_DELAPP) {
			checkTuixinApp();
		}
	}

	public void onResume() {
		if (progress != null) {
			progress.setMessage(this.getString(R.string.app_starting));
			progress.show();
		}
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onStop() {
		// SpotManager.getInstance(this).disMiss(false);
		if (progress != null) {
			progress.cancel();
			progress.dismiss();
		}
		super.onStop();
	}

	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// @Override
	// public void onBackPressed() {
	// // 如果有需要，可以点击后退关闭插屏广告。
	// if (!SpotManager.getInstance(this).disMiss(true)) {
	// super.onBackPressed();
	// }
	// }

	/**
	 * 对旧版神聊包的检测，如果存在就先删除 如果不存在,走正常登錄流程
	 */
	public void checkTuixinApp() {

		if (Utils.checkApkExist(this, "com.tuixin11.sms")) {
			if (Utils.debug) {
				Log.e(TAG, "apk存在时什么条件的登陆？？？弹Dialog对话框？");
			}
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.prompt)
					.setMessage(R.string.checkapp)
					.setCancelable(false)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									Uri uri = Uri.fromParts("package",
											"com.tuixin11.sms", null);
									Intent it = new Intent(
											Intent.ACTION_DELETE, uri);
									startActivityForResult(it, RESULT_DELAPP);
									if (Utils.debug)
										Log.i(TAG, "Delete apps ok");
								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
									IndexActivity.this.finish();
								}
							});
			AlertDialog alert = builder.create();
			alert.show();
		} else {
			if (Utils.debug) {
				Log.e(TAG, "这又是哪种情况的登陆啊？？？！");
			}
			// TODO ?

			mSess.mPrefMeme.door.setVal("").commit();
			int accountType = mSess.mPrefMeme.account_type.getVal();
			String user_id = mSess.mPrefMeme.user_id.getVal();
			String pwd = null;
			try {
				if (!TextUtils.isEmpty(user_id)) {
					pwd = mSess.mUserLoginInfor.getCurrentPwd();
				}
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "Index页面，获取用户密码异常", e);
			}

			String weibo_user_id = mSess.mPrefMeme.weibo_user_id.getVal();
			String weibo_token = mSess.mPrefMeme.weibo_token.getVal();
			String weibo_token_secret = mSess.mPrefMeme.weibo_token_secret
					.getVal();
			int authType = mSess.mPrefMeme.auth_type.getVal();
			Long overTime = mSess.mPrefMeme.weibo_over_time.getVal();
			Long weiboShenliaoId = mSess.mPrefMeme.weibo_shenliao_login_id
					.getVal();

			// 提前开启服务，因为开启的晚的话，可能收不到登陆成功的广播 2013.09.18
			IndexActivity.this.getApplicationContext().startService(
					new Intent(IndexActivity.this, TuixinService1.class));
			// mSess.mPrefMeme.day_login_time.setVal(System.currentTimeMillis());
			// 使用神聊账号登陆
			if (accountType == SessionManager.SHEN_LIAO_ACCOUNT
					&& !Utils.isNull(user_id) && !Utils.isNull(pwd)) {
				if (Utils.debug)
					Log.i(TAG, "用神聊号自动登陆");

				Intent intent = new Intent(IndexActivity.this,
						TuiXinMainTab.class);
				startActivity(intent);

			} else if (accountType != SessionManager.SHEN_LIAO_ACCOUNT
					&& !Utils.isNull(weibo_user_id)
					&& !Utils.isNull(weibo_token)
					&& !Utils.isNull(weibo_token_secret)
					&& weiboShenliaoId != 0
					&& authType != SessionManager.INVALID_OATH
					&& System.currentTimeMillis() < overTime) {
				// 使用其他账号登陆
				if (Utils.debug) {
					Log.i(TAG, "用其他账号自动登陆");
				}
				mSess.setWeiboAuto(true);
				mSess.setOtherAccountLoginInfor(weibo_user_id, weibo_token,
						accountType, authType);
				Intent intent = new Intent(IndexActivity.this,
						TuiXinMainTab.class);
				intent.putExtra("sendWeibo", true);
				startActivity(intent);
			} else {
				// 出现异常，或者第一次登录否则跳到选择界面
				if (Utils.debug) {
					Log.e(TAG, "从IndexActivity跳转到LoginActivity登陆页面");
				}
				Intent i = new Intent(IndexActivity.this, LoginActivity.class);
				startActivity(i);
			}

			if (isFirst) {
				isFirst = false;
				mSess.mPrefMeme.start_first.setVal(PrefsMeme.START_FIRST)
						.commit();
			}
			IndexActivity.this.finish();
		}
	}

	@Override
	protected void onDestroy() {
		// SpotManager.getInstance(this).unregisterSceenReceiver();
		TxData.popActivityRemove(this);
		if (progress != null) {
			progress.cancel();
			progress.dismiss();
		}
		super.onDestroy();

	}

}
