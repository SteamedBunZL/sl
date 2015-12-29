package com.shenliao.set.activity;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 修改签名类
 * 
 * @author xch
 * 
 */
public class SetUpdateSignActivity extends BaseActivity implements
		OnClickListener {
	private EditText edit;// 编辑签名
	private LinearLayout sendBtn;// 发送按钮
	private LinearLayout backBtn;// 返回按钮
	private TextView tv;  //剩余字数--bobo

	// private SharedPreferences prefs;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private ContentResolver cr;
	private static final String TAG = SetUpdateSignActivity.class
			.getSimpleName();
	public static final int SIGN_CHANGE_SUCCESS = 0;
	public static final int SIGN_CHANGE_FAILED = 2;
	public static final int SING_CHANGE_NOTRULE = 1;
	public static final int SIGN_CHANGE_NOTCHANGE = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_setting_update_sign);
		TxData.addActivity(this);
		init();
		setData();
	}

	// 初始化
	private void init() {
		edit = (EditText) findViewById(R.id.userinfo_sign_input_box);
		sendBtn = (LinearLayout) findViewById(R.id.sl_tab_setting_sign_sendBtn);
		backBtn = (LinearLayout) findViewById(R.id.btn_back_left);
		tv = (TextView) findViewById(R.id.userinfo_sign_input_tv);
		
		sendBtn.setOnClickListener(this);
		backBtn.setOnClickListener(this);
		edit.addTextChangedListener(mTextWatcher);
		cr = this.getContentResolver();
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
	}

	// 设置数据
	private void setData() {
		// edit.setHint(TX.tm.getTxMe().user_sign);
		if(TX.tm.getTxMe().sign.equals(" ")){
			edit.setText(TX.tm.getTxMe().sign.trim());
			edit.setSelection(TX.tm.getTxMe().sign.trim().length());
		}else{
			edit.setText(TX.tm.getTxMe().sign);
			edit.setSelection(TX.tm.getTxMe().sign.length());
		}
		
	}

	// 点击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_sign_sendBtn:
			if (edit.getText().toString() != null && !edit.getText().toString().equals("")) {
				// prefs.edit().putString(CommonData.SIGN,
				// edit.getText().toString()).commit();
				mSess.mPrefMeme.sign.setVal(edit.getText().toString()).commit();
			} else {
				// prefs.edit().putString(CommonData.SIGN, " ").commit();
				//清空时，以空格代替
				mSess.mPrefMeme.sign.setVal(" ").commit();
			}
			TX.tm.reloadTXMe();// //////
			
			//隐藏软键盘
			Utils.hideSoftInput(thisContext);
			Intent intent = new Intent();
			intent.putExtra("sign", edit.getText().toString());
			setResult(RESULT_OK, intent);
			showDialogTimer(SetUpdateSignActivity.this, R.string.prompt,
					R.string.group_edit_save, 10 * 1000).show();
			mSess.getSocketHelper().sendUserInforChange();
			break;
		case R.id.btn_back_left:
			Utils.hideSoftInput(thisContext);
			finish();
			break;

		default:
			break;
		}

	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_CHANGE_USERNAME_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent
					.getAction())) {
				dealUserSignChange(serverRsp);
			}
		}

		// 处理修改签名返回结果方法
		private void dealUserSignChange(ServerRsp serverRsp) {
			if (serverRsp != null
					&& serverRsp.getStatusCode(Tx.TX_SIGN) != null) {

				switch (serverRsp.getStatusCode(Tx.TX_SIGN)) {

				case CHANGE_SIGN_SUCCESS:

					handler.sendEmptyMessage(SIGN_CHANGE_SUCCESS);
					break;
				case CHANGE_SIGN_FAILED:
					String fbret = serverRsp.getBundle().getString("fbret");
					Message message = handler.obtainMessage(SIGN_CHANGE_FAILED,
							fbret);
					handler.sendMessage(message);

					break;
				case CHANGE_SIGN_NOTRULE:
					handler.sendEmptyMessage(SING_CHANGE_NOTRULE);
					break;
				case CHANGE_SIGN_NOTCHANGE:
					handler.sendEmptyMessage(SIGN_CHANGE_NOTCHANGE);
					break;

				}
			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			cancelDialogTimer();
			int num = msg.what;
			switch (num) {
			case SIGN_CHANGE_SUCCESS:
				Intent intent = new Intent(
						Constants.INTENT_ACTION_CHANGE_USERSIGN_RSP);
				thisContext.sendBroadcast(intent);
				SetUpdateSignActivity.this.finish();
				break;
			case SIGN_CHANGE_FAILED:
				String signfbret = (String) msg.obj;
				// prefs.edit().putString(CommonData.SIGN, "").commit();
				mSess.mPrefMeme.sign.setVal("").commit();
				TX.tm.reloadTXMe();// ///////
				Toast.makeText(SetUpdateSignActivity.this, signfbret, 1).show();
				break;

			case SIGN_CHANGE_NOTCHANGE:
				Intent intentnotchage = new Intent(
						Constants.INTENT_ACTION_CHANGE_USERSIGN_RSP);
				thisContext.sendBroadcast(intentnotchage);
				SetUpdateSignActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		private CharSequence temp;
		private int editStart;
		private int editEnd;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			//
			temp = s;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//
			// countNum.setText(s);//将输入的内容实时显示
		}

		@Override
		public void afterTextChanged(Editable s) {
			//
			editStart = edit.getSelectionStart();
			editEnd = edit.getSelectionEnd();
			int leavenum = 60 - temp.length();
			tv.setText(leavenum+"");
			
			if (temp.length() > 60) {
				Toast.makeText(SetUpdateSignActivity.this, "最多可以输入60个字符",
						Toast.LENGTH_SHORT).show();
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				edit.setText(s);
				edit.setSelection(tempSelection);
			}
		}
	};

	public boolean dispatchKeyEvent(android.view.KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {

			return true;

		}

		return super.dispatchKeyEvent(event);

	};
}
