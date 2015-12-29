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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.user.activity.UserInfoPerfectActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 职业修改
 * 
 * @author xch
 * 
 */
public class SetUpdateProfessionActivity extends BaseActivity implements
		OnClickListener {
	private EditText edit;// 职业编辑
	private TextView submitBtn;// 提交按钮
	// private SharedPreferences prefs;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private ContentResolver cr;
	private TextView countNum;// 字数
	public static final int PROFESSION_CHANGE_SUCCESS = 8;
	public static final int PROFESSION_CHANGE_FAILED = 9;
	public static final int PROFESSION_CHANGE_NOTRULE = 10;
	public static final int PROFESSION_NOTCHAGE = 11;

	public static final int PERFECTNIFO = 101;
	public static final int NOTPERFECTINFO = 102;
	public static final String GOINPAGE = "goinpage";
	public int goinpage = 102;
	public Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_userinfo_update_profession);
		init();
		setData();
	}

	// 初始化
	private void init() {
		edit = (EditText) findViewById(R.id.userinfo_profession_input_box);
		submitBtn = (TextView) findViewById(R.id.sl_tab_setting_profession_sendBtn);
		back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		countNum = (TextView) findViewById(R.id.sl_tab_setting_userinfo_profession_num);
		cr = this.getContentResolver();
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		intent = this.getIntent();
		goinpage = intent.getIntExtra(GOINPAGE, NOTPERFECTINFO);
		submitBtn.setOnClickListener(this);
		back_left.setOnClickListener(this);
	}

	// 数据
	private void setData() {
		edit.addTextChangedListener(mTextWatcher);
		if (TX.tm.getTxMe().job.equals(" ")) {
			edit.setText(TX.tm.getTxMe().job.trim());
			edit.setSelection(TX.tm.getTxMe().job.trim().length());
		} else {
			edit.setText(TX.tm.getTxMe().job);
			edit.setSelection(TX.tm.getTxMe().job.length());
		}

	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_profession_sendBtn:
			// prefs.edit().putString(CommonData.JOB,
			// edit.getText().toString()).commit();
			if (edit.getText().toString() != null
					&& !edit.getText().toString().equals("")) {
				mSess.mPrefMeme.job.setVal(edit.getText().toString()).commit();
			} else {
				mSess.mPrefMeme.job.setVal(" ").commit();
			}

			TX.tm.reloadTXMe();// ////
			showDialogTimer(SetUpdateProfessionActivity.this, R.string.prompt,
					R.string.group_edit_save, 10 * 1000).show();
			mSess.getSocketHelper().sendUserInforChange();

			break;
		case R.id.btn_back_left:
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
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent
					.getAction())) {
				dealUserNameChange(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealUserNameChange(ServerRsp serverRsp) {
			cancelDialogTimer();
			switch (serverRsp.getStatusCode(Tx.PROFESSION)) {

			case CHANGE_JOB_SUCCESS:

				handler.sendEmptyMessage(PROFESSION_CHANGE_SUCCESS);
				break;
			case CHANGE_JOB_FAILED:
				String fbret = serverRsp.getBundle().getString("fbret");
				Message message = handler.obtainMessage(
						PROFESSION_CHANGE_FAILED, fbret);
				handler.sendMessage(message);

				break;
			case CHANGE_JOB_NOTCHANGE:
				handler.sendEmptyMessage(PROFESSION_NOTCHAGE);

				break;
			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			switch (num) {
			case PROFESSION_CHANGE_SUCCESS:
				if (goinpage == PERFECTNIFO) {
					setResult(UserInfoPerfectActivity.RESULTCODE_FOR_RESULT_PROSESSION);
				}
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_PROSESSION);
				SetUpdateProfessionActivity.this.finish();
				break;
			case PROFESSION_CHANGE_FAILED:
				String signfbret = (String) msg.obj;
				// prefs.edit().putString(CommonData.NICKNAME, "").commit();
				mSess.mPrefMeme.nickname.setVal("").commit();
				TX.tm.reloadTXMe();// ////
				Toast.makeText(SetUpdateProfessionActivity.this, signfbret, 1)
						.show();
				break;
			case PROFESSION_NOTCHAGE:
				if (goinpage == PERFECTNIFO) {
					setResult(UserInfoPerfectActivity.REQUESTCODE_FOR_REQUSET_AREA);
				}
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_PROSESSION);
				SetUpdateProfessionActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
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
			int len = 20 - temp.length();
			countNum.setText(len + "");
			if (temp.length() > 20) {
				Toast.makeText(SetUpdateProfessionActivity.this, "最多可以输入20个字符",
						Toast.LENGTH_SHORT).show();
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				edit.setText(s);
				edit.setSelection(tempSelection);
			}
		}
	};
	private LinearLayout back_left;
}
