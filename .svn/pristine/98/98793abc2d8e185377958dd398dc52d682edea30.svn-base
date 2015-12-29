package com.shenliao.set.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
 * 加好友打招呼页面
 * 
 * @author xch
 * 
 */
public class UserInfoJoinFriendActivity extends BaseActivity implements
		OnClickListener {
	private EditText editText;// 打招呼编辑框
	private Button submitBtn;// 提交按钮
	private int inviteState;
	// private SharedPreferences prefs;
	// private Editor editor;
	private TX tx;
	private Intent intent;
	public final static String INFOTX = "tx";
	private UpdateReceiver updatereceiver;
	private LinearLayout btn_back_left;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo_join);
		init();
	}

	@Override
	protected void onResume() {
		registReceiver();
		inviteState = 0;
		super.onResume();
	}

	// 初始化
	private void init() {
		TxData.addActivity(this);
		editText = (EditText) findViewById(R.id.userinfo_join_input_box);
		submitBtn = (Button) findViewById(R.id.userinfo_join_send);
		btn_back_left = (LinearLayout) findViewById(R.id.back_join);
		btn_back_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				thisContext.finish();
			}
		});
		submitBtn.setOnClickListener(this);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		intent = this.getIntent();
		tx = intent.getParcelableExtra(INFOTX);
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.userinfo_join_send:
			long current_time1 = System.currentTimeMillis();
			// long last_click_time =
			// prefs.getLong(CommonData.SOKET_CHECK_CLICK_TIME, 0);//与个人信息TXMe无关
			long last_click_time = mSess.mPrefMeme.soket_check_click_time
					.getVal();

			long time_1 = current_time1 - last_click_time;
			if (time_1 <= 10000) {
				if (inviteState == 0) {
					Toast.makeText(UserInfoJoinFriendActivity.this,
							R.string.check_tel_fast, Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(UserInfoJoinFriendActivity.this,
							R.string.invite_tel_fast, Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				inviteState = 0;
				showDialogTimer(UserInfoJoinFriendActivity.this,
						R.string.prompt, R.string.weibo_send_ing, 10 * 1000)
						.show();
				if (tx != null) {
					mSess.getSocketHelper().sendAddPartener(
							tx.partner_id, "",
							editText.getText().toString().trim());
				}

				long current_time = System.currentTimeMillis();
				// editor.putLong(CommonData.SOKET_CHECK_CLICK_TIME,
				// current_time);//与个人信息TXMe无关
				// editor.commit();
				mSess.mPrefMeme.soket_check_click_time.setVal(current_time);
			}

			break;
		}

	}

	// 注册广播方法
	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_AGREE_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_USERINFO_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_ADD_BUDDY.equals(intent.getAction())) {
				dealAddBuddy(serverRsp);
			}
		}
	}

	// 添加好友请求结果处理
	private void dealAddBuddy(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK:
				if (tx != null && tx.partner_id == serverRsp.getInt("uid", -1)) {
					startPromptDialog(UserInfoJoinFriendActivity.this, "加好友",
							"已成功发送加好友申请。");
				}
				break;
			case REFUSE_FRIEND_REQ:
				startPromptDialog(UserInfoJoinFriendActivity.this, "加好友",
						"对方拒绝加为好友。");
				break;
			case BUDDY_THAN_LIMIT:
				Utils.startPromptDialog(UserInfoJoinFriendActivity.this, "加好友",
						"好友到达上限。");
				break;
			case OPT_FAILED:
				startPromptDialog(UserInfoJoinFriendActivity.this, "加好友",
						"操作失败。");
				break;
			default:
			}
		}
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
		super.onStop();
	}

	public static void startPromptDialog(final Activity ctxt,
			String titleSource, String msg) {

		AlertDialog.Builder promptDialog = new AlertDialog.Builder(ctxt);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setCancelable(false);
		promptDialog.setNegativeButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
						ctxt.finish();

					}
				});
		promptDialog.show();
	}

}
