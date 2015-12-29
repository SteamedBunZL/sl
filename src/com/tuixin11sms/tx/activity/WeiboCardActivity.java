package com.tuixin11sms.tx.activity;

import java.lang.ref.WeakReference;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class WeiboCardActivity extends BaseActivity {

	private UpdateReceiver updatareceiver;
	private int count;
	private int successCount;
	private AlertDialog dialog;
	private View mCardLayout;
	private TextView mNickname;
	private TextView mTuixinnumber;
	private ImageView mSex;
	private ImageView mHeadPic;
	private TX me;
	public static final int UPLOAD_SUCCESS = 0;
	public static final int UPLOAD_TO_MORE = 1;
	public static final int UPLOAD_ALL_MORE = 2;
	public static final int UPLOAD_FAIL = 3;
	public static final int NO_FRIEND = 4;
	public static final int SERVER_BUSY = 5;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_weibo_pre);
		me = TX.tm.getTxMe();

		mCardLayout = this.findViewById(R.id.mCardLayout);

		mNickname = (TextView) findViewById(R.id.mNickname);
		mTuixinnumber = (TextView) findViewById(R.id.mTuixinnumber);
		mSex = (ImageView) findViewById(R.id.mSex);
		mHeadPic = (ImageView) findViewById(R.id.mHeadPic);
		mAge = (TextView) findViewById(R.id.mAge_tv);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				WeiboCardActivity.this.finish();
			}
		});

		Calendar c = Calendar.getInstance();
		int nowYear = c.get(Calendar.YEAR);

		mNickname.setText(me.getNick_name());
		int bri = me.birthday;
		if (!(bri > 0)) {
			mAge.setText(20 + "");
		} else {
			String birthday = String.valueOf(me.birthday);
			int year = Integer.parseInt(birthday.substring(0, 4));
			int age = nowYear - year;
			mAge.setText(age + "");
		}

		mTuixinnumber.setText("" + me.partner_id);
		mSex.setBackgroundResource(me.getSex() == TX.MALE_SEX ? R.drawable.user_infor_sex_boy
				: R.drawable.user_infor_sex_girl);

		if (Utils.isIdValid(me.partner_id)) {

			mHeadPic.setTag(me.partner_id);
			mHeadPic.setImageResource(defaultHeaderImg);

			Bitmap avatar =  mSess.avatarDownload.getAvatar(me.avatar_url,
					me.partner_id, mHeadPic, avatarHandler);

			if (avatar != null)
				mHeadPic.setImageBitmap(avatar);
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				long tag = (Long) mHeadPic.getTag();
				long id = (Long) result[1];
				if (result[0] != null && tag == id) {
					mHeadPic.setImageBitmap((Bitmap) result[0]);
				} 
				break;
			}
			super.handleMessage(msg);
		}
	};

	public void next(View v) {
		mCardLayout.setDrawingCacheEnabled(true);
		TxData.cardBitmap = getBitmap(mCardLayout.getDrawingCache());
		Intent i = new Intent(this, OAuthShareWeiboActivity.class);
		startActivity(i);

	}
 
	private Bitmap getBitmap(Bitmap img) {
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(img);
		return wref.get();
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			dialog = new Builder(WeiboCardActivity.this).create();

			dialog.setButton(
					WeiboCardActivity.this.getResources().getString(
							R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.dismiss();
						}
					});
			switch (msg.what) {
			case UPLOAD_SUCCESS:
				successCount += 1;
				if (successCount == count + 1) {
					dialog.setTitle(R.string.prompt);
					dialog.setMessage(WeiboCardActivity.this.getResources()
							.getString(R.string.weibo_tongbu_success));
					dialog.show();
				}
				break;
			case UPLOAD_TO_MORE:
				break;
			case UPLOAD_ALL_MORE:
				break;
			case UPLOAD_FAIL:
				dialog.setTitle(R.string.weibo_tongbu_failed);
				dialog.setMessage(WeiboCardActivity.this.getResources()
						.getString(R.string.weibo_tongbu_net_bad));
				dialog.show();
				break;
			case NO_FRIEND:
				dialog.setTitle(R.string.prompt);
				dialog.setMessage(WeiboCardActivity.this.getResources()
						.getString(R.string.weibo_tongbu_success));
				dialog.show();
				break;
			case SERVER_BUSY:
				dialog.setTitle(R.string.weibo_failed);
				dialog.setMessage(WeiboCardActivity.this.getResources()
						.getString(R.string.weibo_server_busy));
				dialog.show();
				break;
			}
		}
	};
	private TextView mAge;
	private LinearLayout btn_back_left;

	private void sendMessage(int what) {
		Message msg = handler.obtainMessage();
		msg.what = what;
		handler.sendMessage(msg);
	}

	public void onResume() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_UP_SNS_RSP);
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

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_UP_SNS_RSP.equals(intent.getAction())) {
				dealUpSns(serverRsp);
			}
		}
	}

	private void dealUpSns(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				sendMessage(UPLOAD_SUCCESS);
				break;
			}
			case UP_THE_NUMBER_THAN_LIMIT: {
				break;
			}
			case THE_TOTAL_NUMBER_THAN_LIMIT: {
				break;
			}
			case OPT_FAILED: {
				sendMessage(SERVER_BUSY);
				break;
			}
			}
		}
	}

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

}
