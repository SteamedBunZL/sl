package com.shenliao.group.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class GroupJoin extends BaseActivity {

	private Button mSubmit;
	private EditText mContent;
	private TxGroup txGroup;
	TextView introl; // 简介
	TextView creator; // 群主
	ImageView avatar; // 头像
	TextView groupId;
	TextView creatorTime;
	TextView name;
	private UpdateReceiver updatareceiver;
	private ScrollView  screen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_group_join);
		init();
	}

	private void init() {
		avatar = (ImageView) findViewById(R.id.group_join_head);
		name = (TextView) findViewById(R.id.group_join_groupname);
		creator = (TextView) findViewById(R.id.group_join_groupcreate);
		groupId = (TextView) findViewById(R.id.group_join_groupid);
		//	creatorTime=(TextView) findViewById(R.id.group_join_grouptime);
		introl = (TextView) findViewById(R.id.group_join_groupintrol);
        screen=(ScrollView) findViewById(R.id.group_info_screen);
        btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
        btn_back_left.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				GroupJoin.this.finish();
			}
		});
         screen.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				 InputMethodManager imm = (InputMethodManager)GroupJoin.this.getSystemService(Context.INPUT_METHOD_SERVICE);  
	             imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
		Intent intent = getIntent();
		if (intent != null) {
			txGroup = intent.getParcelableExtra(Utils.MSGROOM_TX_GROUP);
		}

		mSubmit = (Button) findViewById(R.id.group_setting_create_btn);
		mContent = (EditText) findViewById(R.id.create_group_join_input_introl_box);
		setInfoData(txGroup);
		mSubmit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				showDialogTimer(GroupJoin.this, R.string.prompt, R.string.group_join_sending, 10 * 1000).show();
				mSess.getSocketHelper().sendJoinQuitGroup(txGroup.group_id, mContent.getText().toString());
				//GroupList.CHANGE=true;
			}
		});
	}

	/**
	 * 设置群资料信息
	 */
	public void setInfoData(TxGroup txGroup) {
		if (txGroup != null) {
			name.setText(txGroup.group_title);
			creator.setText(txGroup.group_own_name);
			introl.setText(txGroup.group_sign);
			groupId.setText(""+txGroup.group_id);
			//creatorTime.setText("(" + formatTime(txGroup.group_time * 1000) + ")");
			avatar.setTag("group_" + txGroup.group_id);
			
			Bitmap bm = getGroupCachedBitmap(txGroup.group_id);
			if (bm!=null) {
				avatar.setImageBitmap(bm);
			}else {
				loadGroupImg(txGroup.group_avatar, txGroup.group_id, avatarCallback);
				avatar.setImageResource(R.drawable.qun_default);
			}
		}
	}
	

	@Override
	protected void onResume() {
		super.onResume();
		registReceiver();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregistReceiver();
	}

	private void unregistReceiver() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_AGREE_GROUP_JOIN);
			filter.addAction(Constants.INTENT_ACTION_JOIN_GROUP_2018);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_JOIN_GROUP_2018.equals(intent.getAction())) {
				dealJoinGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_AGREE_GROUP_JOIN.equals(intent.getAction())) {
				dealAgreeNotice(serverRsp);

			}
		}
	}

	public void dealJoinGroup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Intent intent = new Intent(GroupJoin.this, GroupMsgRoom.class);
			intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
			startActivity(intent);
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(GroupJoin.this, R.string.group_join_success, Toast.LENGTH_SHORT).show();
				}
			});
			GroupJoin.this.finish();
			break;
		case GROUP_REQUEST_SUCCESS:
			Utils.startPromptDialog(GroupJoin.this, R.string.prompt, R.string.group_join_send_request_success);
			break;
		case GROUP_FULL:
			Utils.startPromptDialog(GroupJoin.this, R.string.prompt, R.string.group_join_full);
			break;
		case GROUP_IN_BLACK_LIST:
			Utils.startPromptDialog(GroupJoin.this, R.string.prompt, R.string.group_join_failed);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupJoin.this, R.string.prompt, R.string.opt_failed);
			break;
		}
	}

	public void dealAgreeNotice(ServerRsp serverRsp) {
		cancelDialogTimer();
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			boolean agree = serverRsp.getBundle().getBoolean("agree");
			if (agree) {
				mSubmit.setText("申请已通过,点击进入聊天室");
                mContent.setVisibility(View.GONE);
			}
			break;
		}
	}

	/**
	 * 
	 * @param time
	 * @return 格式化时间
	 */
	public String formatTime(long time) {
		Date date = new Date(time);
		SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		String formatTime = format.format(date);
		return formatTime;

	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = (ImageView) avatar.findViewWithTag("group_" + id);
			if (iv != null && result != null) {
				iv.setImageBitmap(Utils.getRoundedCornerBitmap(result));
			}
			//Log.i("fffffffff", "fffffffff___:success:"+id);
		}
	};
	private LinearLayout btn_back_left;
	
	@Override
	protected void onDestroy() {
//		stopAsyncload();
		super.onDestroy();
	};


}
