package com.tuixin11sms.tx.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import weibo4android.WeiboException;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Utils;
import com.weibo.net.AccessToken;
import com.weibo.net.AsyncWeiboRunner;
import com.weibo.net.AsyncWeiboRunner.RequestListener;
import com.weibo.net.Utility;
import com.weibo.net.Weibo;
import com.weibo.net.WeiboParameters;

public class OAuthShareWeiboActivity extends BaseActivity implements
		RequestListener {

	private TextView mTextLength;
	private TextView mTextPre;
	private ImageView mPrePic;
	private Button mWeiboSubmit;
	private EditText mWeiboContent;
	private int fromType;
	private TxGroup txGroup;

	private int length;
	SharedPreferences prefs;

	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_weibo);
		TxData.addActivity(this);
		mTextPre = (TextView) findViewById(R.id.pre_text);
		mTextLength = (TextView) findViewById(R.id.mTextLength);
		mPrePic = (ImageView) findViewById(R.id.mPrePic);
		mWeiboSubmit = (Button) findViewById(R.id.mWeiboSubmit);
		mWeiboContent = (EditText) findViewById(R.id.mWeiboContent);

		LinearLayout btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				OAuthShareWeiboActivity.this.finish();
			}
		});
		Intent intentt = getIntent();
		if (intentt != null) {
			fromType = intentt.getIntExtra(WebViewActivity.LOGIN_STATE, 1);
			if (fromType == WebViewActivity.TO_SHARE_GROUP) {
				mTextPre.setVisibility(View.GONE);
				mPrePic.setVisibility(View.GONE);
				txGroup = intentt.getParcelableExtra(WebViewActivity.SHARE_GROUP);
			}
		}
		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

		if (TxData.cardBitmap != null
				&& fromType != WebViewActivity.TO_SHARE_GROUP) {
			mPrePic.setImageBitmap(TxData.cardBitmap);
		}
		mWeiboContent.addTextChangedListener(new LengthTextWatcher());
		mWeiboSubmit.setOnClickListener(new WeiboSubmit());
		String s = null;
		if (mSess.isOtherAccount()) {
			s = this.getResources()
					.getString(R.string.weibo_content_sina_start)
					+ TX.tm.getTxMe().partner_id
					+ this.getResources().getString(
							R.string.weibo_content_sina_end);
		} else if (fromType != WebViewActivity.TO_SHARE_GROUP) {
			s = this.getResources().getString(R.string.weibo_content_start)
					+ TX.tm.getTxMe().partner_id
					+ this.getResources().getString(R.string.weibo_content_end);
		} else {
			s = "我在神聊创建了一个聊天室：【" + txGroup.group_title + "】，聊天室号是：【"
					+ txGroup.group_id
					+ "】，快来加入和我一起群聊吧。手机下载 http://shenliao.com";

		}

		mWeiboContent.setText(s);

	}

	private class LengthTextWatcher implements TextWatcher {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			try {
				length = s.toString().getBytes("GBK").length;
				if (length % 2 == 1) {
					length = length / 2 + 1;
				} else {
					length = length / 2;
				}
				
				mTextLength.setText((140 - length) + "");
			} catch (UnsupportedEncodingException e) {
				if (Utils.debug)
					e.printStackTrace();
			}

		}

	}

	private class WeiboSubmit implements OnClickListener {

		@Override
		public void onClick(View v) {
			if (length > 140) {
				Toast.makeText(OAuthShareWeiboActivity.this,
						R.string.weibo_send_length, Toast.LENGTH_SHORT).show();
				return;
			}

			showDialogTimer(OAuthShareWeiboActivity.this, 0,
					R.string.weibo_send_ing, 20 * 1000, new BaseTimerTask() {
						public void run() {
							super.run();
							Looper.prepare();
							Toast.makeText(OAuthShareWeiboActivity.this,
									R.string.weibo_send_wait_long,
									Toast.LENGTH_SHORT).show();
							Looper.loop();
						}
					}).show();
			Weibo weibo = com.weibo.net.Weibo.getInstance();
			String token = prefs.getString(
					CommonData.WEIBO_TOKEN + "�" + TX.tm.getUserID(), "");
			String tokenSecret = prefs.getString(CommonData.WEIBO_TOKEN_SECRET
					+ "�" + TX.tm.getUserID(), "");
			Long overTime = prefs.getLong(CommonData.WEIBO_OVER_TIME + "�"
					+ TX.tm.getUserID(), 0);
			if (overTime < System.currentTimeMillis()) {
				err();
				return;
			}
			AccessToken accessToken = new AccessToken(token, tokenSecret);

			weibo.setAccessToken(accessToken);

			if (Utils.isNull(token) || Utils.isNull(tokenSecret)) {
				err();
				return;
			}
			try {
				upload(weibo, Weibo.getAppKey(), TxData.cardBitmap,
						mWeiboContent.getText().toString(), "", "");
			} catch (WeiboException e) {
				err();
			}
		}
	}

	private void err() {
		prefs.edit()
				.putLong(CommonData.WEIBO_OVER_TIME + "�" + TX.tm.getUserID(),
						0).commit();
		AlertDialog builder = new AlertDialog.Builder(
				OAuthShareWeiboActivity.this).create();
		builder.setMessage(OAuthShareWeiboActivity.this.getResources()
				.getString(R.string.weibo_send_failed));
		builder.setButton(OAuthShareWeiboActivity.this.getResources()
				.getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Intent iOauth = new Intent(
								OAuthShareWeiboActivity.this,
								WebViewActivity.class);
						iOauth.putExtra(WebViewActivity.LOGIN_STATE, fromType);
						iOauth.putExtra(WebViewActivity.SHARE_GROUP, txGroup);
						startActivity(iOauth);
						dialog.dismiss();
						cancelDialogTimer();
						OAuthShareWeiboActivity.this.finish();
					}
				});
		builder.show();
	}

	private String upload(Weibo weibo, String source, Bitmap file,
			String status, String lon, String lat) throws WeiboException {
		WeiboParameters bundle = new WeiboParameters();
		bundle.add("source", source);
		if (fromType != WebViewActivity.TO_SHARE_GROUP) {
			bundle.add("pic", weibo.getBytes(file));
		}
		bundle.add("status", status);
		if (!TextUtils.isEmpty(lon)) {
			bundle.add("lon", lon);
		}
		if (!TextUtils.isEmpty(lat)) {
			bundle.add("lat", lat);
		}
		String rlt = "";
		String url = "";
		switch (fromType) {
		case WebViewActivity.TO_SHARE_GROUP:
			url = Weibo.SERVER + "statuses/update.json";
			break;
		default:
			url = Weibo.SERVER + "statuses/upload.json";
			break;
		}
		AsyncWeiboRunner weiboRunner = new AsyncWeiboRunner(weibo);
		weiboRunner.request(this, url, bundle, Utility.HTTPMETHOD_POST, this);

		return rlt;
	}

	@Override
	protected void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	public static byte[] Bitmap2Bytes(Bitmap bm) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bm.compress(Bitmap.CompressFormat.PNG, 100, baos);
		return baos.toByteArray();
	}

	@Override
	public void onComplete(String response) {
		mPrePic.setVisibility(View.GONE);
		cancelDialogTimer();
		Toast.makeText(OAuthShareWeiboActivity.this,
				R.string.weibo_send_success, Toast.LENGTH_SHORT).show();
		mSess.getSocketHelper().sendForwardWeibo();//微博转发成功，通知服务器
		OAuthShareWeiboActivity.this.finish();
		TxData.finishOne(WeiboCardActivity.class.getName());
	}

	@Override
	public void onError(com.weibo.net.WeiboException e) {
		cancelDialogTimer();
		AlertDialog builder = new AlertDialog.Builder(
				OAuthShareWeiboActivity.this).create();
		builder.setTitle(OAuthShareWeiboActivity.this.getResources().getString(
				R.string.prompt));
		if (e.getStatusCode() == 20034) {
			builder.setMessage("账号有危险，请上新浪微博官方处理");
		} else if (e.getStatusCode() == 20111 || e.getStatusCode() == 20019) {
			builder.setMessage(OAuthShareWeiboActivity.this.getResources()
					.getString(R.string.weibo_send_failed_again));
			builder.setButton(OAuthShareWeiboActivity.this.getResources()
					.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();

						}
					});
			builder.show();
		} else {
			err();
		}
	}

	@Override
	public void onIOException(IOException e) {
		cancelDialogTimer();
		AlertDialog builder = new AlertDialog.Builder(
				OAuthShareWeiboActivity.this).create();
		builder.setTitle(OAuthShareWeiboActivity.this.getResources().getString(
				R.string.prompt));
		builder.setMessage(OAuthShareWeiboActivity.this.getResources()
				.getString(R.string.weibo_send_failed_again));
		builder.setButton(OAuthShareWeiboActivity.this.getResources()
				.getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();

					}
				});
		builder.show();

	}

}
