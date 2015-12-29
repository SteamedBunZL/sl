package com.tuixin11sms.tx.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 在第三个tab页点击 通过号码添加好友 就跳转到这个页面
 * 
 */
public class FindTxFriendActivity extends BaseActivity implements
		OnTouchListener {
	// private static final String TAG = FindTxFriendActivity.class
	// .getSimpleName();
	ImageView contactsHead;
	TextView contactsName;
	Button findFriendBtn;
	LinearLayout backBtn;
	EditText search_input_box;
	ImageView call;
	ImageView newChat;
	Button cleanSeachContent_btn;
	LinearLayout relative;

	private UpdateReceiver updatareceiver;
	private static final int TEL_CHECK_TIMEOUT = 26;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_search_tx_friend);
		findFriendBtn = (Button) findViewById(R.id.find_friend_btn);
		relative = (LinearLayout) findViewById(R.id.search_tx_friend_relative);
		relative.setOnTouchListener(this);
		backBtn = (LinearLayout) findViewById(R.id.btn_back_addFri);
		backBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				FindTxFriendActivity.this.finish();
			}
		});
		search_input_box = (EditText) findViewById(R.id.search_input_box);
		search_input_box.addTextChangedListener(watcher);

		cleanSeachContent_btn = (Button) findViewById(R.id.seach_clean_content);
		cleanSeachContent_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//
				search_input_box.setText("");
				search_input_box.setHint(R.string.hint_search_friend_bar);
				cleanSeachContent_btn.setVisibility(View.GONE);
			}
		});
		findFriendBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (Utils.getNetworkType(FindTxFriendActivity.this) != Utils.NET_NOT_AVAILABLE) {
					String search = search_input_box.getText().toString()
							.trim();
					int len = search.length();
					if (len <= 0) {
						Utils.startPromptDialog(FindTxFriendActivity.this,
								R.string.prompt, R.string.search_firend_prompt);
					} else if (len > Utils.INPUT_USERNAME_MAX_LENGTH) {
						Utils.startPromptDialog(FindTxFriendActivity.this,
								R.string.prompt, R.string.name_too_long);
					} else if (search.equals("9999999")
							|| search.endsWith("9999996")) {
						Utils.startPromptDialog(FindTxFriendActivity.this,
								FindTxFriendActivity.this
										.getString(R.string.seach_fail_title),
								null);
					} else {
						showDialogTimer(FindTxFriendActivity.this, 0,
								R.string.search_friend, 30 * 1000,
								new BaseTimerTask() {
									public void run() {
										super.run();
										Message msg = new Message();
										msg.what = TEL_CHECK_TIMEOUT;
										handler.sendMessage(msg);
									}
								}).show();
						mSess.getSocketHelper().sendSearchUser(search);
					}
				} else {
					Utils.startPromptDialog(FindTxFriendActivity.this,
							FindTxFriendActivity.this
									.getString(R.string.seach_network_title1),
							FindTxFriendActivity.this
									.getString(R.string.seach_network_title));
				}

				Utils.hideSoftInput(FindTxFriendActivity.this, search_input_box);
			}
		});

	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			cancelDialog();
			int num = msg.what;
			switch (num) {
			case TEL_CHECK_TIMEOUT:
				Utils.startPromptDialog(FindTxFriendActivity.this,
						R.string.prompt, R.string.foreign_check_code_outtime);

				break;
			}
		}
	};

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	public void onStart() {

		super.onStart();
	}

	public void onResume() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.setPriority(1);
			filter.addAction(Constants.INTENT_ACTION_FIND_FRIEND);
			this.registerReceiver(updatareceiver, filter);
		}
		// if (newTX == null) {
		// newTX = new NewTX();
		// IntentFilter filter = new IntentFilter();
		// // 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
		// filter.addAction(NEW_TX);
		// this.registerReceiver(newTX, filter);
		// }
		search_input_box.setText("");
		search_input_box.setHint(R.string.hint_search_bar2);
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
		cancelTimer();
		// if (newTX != null) {
		// this.unregisterReceiver(newTX);
		// newTX = null;
		// }
		super.onDestroy();
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_FIND_FRIEND.equals(intent.getAction())) {
				cancelTimer();
				if (serverRsp != null) {
					switch (serverRsp.getStatusCode()) {
					case RSP_OK: {
						cancelDialog();
//						TX tx = Utils.getTX(intent);
						long partner_id = intent.getLongExtra(TX.EXTRA_TX, Utils.DEFAULT_NUMBER);
						TX tx = TX.tm.getTx(partner_id);
//						if (TX.tm.isTxFriend(tx.partner_id)) {
//							tx.setTx_type(TxDB.TX_TYPE_TB);
//						} else {
//							tx.setTx_type(TxDB.TX_TYPE_ST);
//						}
//						tx.setStarFriend(TX.tm.getStarFriendAttr(tx.partner_id));

						if (tx != null) {
							if (TX.tm.getUserID() == tx.partner_id) {
								Intent iUserInfo = new Intent(
										FindTxFriendActivity.this,
										SetUserInfoActivity.class);

								startActivity(iUserInfo);
							} else {
								Intent intent1 = new Intent(
										FindTxFriendActivity.this,
										UserInformationActivity.class);
								int entryType = TX.tm.isTxFriend(tx.partner_id) ? UserInformationActivity.TUIXIN_USER_INFO
										: UserInformationActivity.NOT_TUIXIN_USER_INFO;
								intent1.putExtra(
										UserInformationActivity.pblicInfo,
										entryType);
								intent1.putExtra(
										UserInformationActivity.UID, tx.partner_id);
								startActivity(intent1);
							}
						}
						break;
					}
					default: {
						cancelDialog();
						Utils.startPromptDialog(thisContext, thisContext
								.getString(R.string.seach_fail_title), null);
					}
					}
				}

			}
		}
	}

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (!s.toString().equals("")) {
				cleanSeachContent_btn.setVisibility(View.VISIBLE);
			} else {
				cleanSeachContent_btn.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		InputMethodManager imm = (InputMethodManager) FindTxFriendActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		return false;
	}

}
