package com.shenliao.group.activity;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 查看群资料页
 */
public class GroupInfo extends BaseActivity implements OnClickListener,
		OnCheckedChangeListener {
	private static final String TAG = "GroupInfo";
	private Button mMember, mEditGrop, exitGroup;
	private TxGroup txGroup = new TxGroup();
	private TextView name;
	private TextView notice;
	private TextView introl; // 简介
	private TextView creator; // 群主
	private ImageView avatar; // 头像
	private TextView groupNum;
	// private TextView creatorTime;//无引用，先注掉 2013.09.25
	private CheckBox mRcvMsg;
	private CheckBox mRcvPush;
	private Intent intent;
	private LinearLayout disGroupInfo;
	private TextView chanelTypeImage;
//	private ImageView imageGuan;
	private UpdateReceiver updatareceiver;
	private SmileyParser smileyParser;
	private LinearLayout ll_LastWeekStars;// 活跃之星整个布局
	private GridView mStarGridView;// 活跃之星列表
	private GroupMsgRoom.GroupStarAdapter mStarAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_group_info);
		
		init();

	}

	private void init() {
		intent = getIntent();
		disGroupInfo = (LinearLayout) findViewById(R.id.group_info_chanelType);
		mMember = (Button) findViewById(R.id.group_info_members);
		mEditGrop = (Button) findViewById(R.id.group_info_editbtn);
		name = (TextView) findViewById(R.id.group_info_groupname);
		creator = (TextView) findViewById(R.id.group_info_groupcreate);
		groupNum = (TextView) findViewById(R.id.group_info_groupid);
		introl = (TextView) findViewById(R.id.group_info_groupintrol);
		notice = (TextView) findViewById(R.id.group_info_notice);

		group_join_title = (TextView) findViewById(R.id.group_join_title);
		group_info_groupidname = (TextView) findViewById(R.id.group_info_groupidname);
		group_info_groupCreate = (TextView) findViewById(R.id.group_info_groupCreate);

		// creatorTime = (TextView) findViewById(R.id.group_info_creatortime);
		exitGroup = (Button) findViewById(R.id.group_info_exit_btn);
		mRcvMsg = (CheckBox) findViewById(R.id.group_info_accaptmessage_checkbox);
		mRcvPush = (CheckBox) findViewById(R.id.group_info_noticemessage_checkbox);
		avatar = (ImageView) findViewById(R.id.group_info_head);
		chanelTypeImage = (TextView) findViewById(R.id.group_info_imagetype);
//		imageGuan = (ImageView) findViewById(R.id.group_info_imageGuan);

		ll_LastWeekStars = (LinearLayout) findViewById(R.id.rl_lask_week_stars);
		btn_back_left_groupinfo = (LinearLayout) findViewById(R.id.btn_back_left_groupinfo);
		btn_back_left_groupinfo.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupInfo.this.finish();
			}
		});
		mStarGridView = (GridView) findViewById(R.id.sl_groupinfo_start_gridView);
		ll_LastWeekStars.setVisibility(View.GONE);
		// mStarAdapter = new
		// BaseMsgRoom.GroupStarAdapter(this,GroupMsgRoom.mGroupStarsMap.get(txGroup.group_id));
		// mStarGridView.setAdapter(mStarAdapter);
		smileyParser = new SmileyParser(this);
		if (intent != null) {
			long id = intent.getLongExtra(Utils.MSGROOM_GROUP_ID, 0);
			if (id == 0) {
				this.finish();
				return;
			}

			// TODO 从数据库查询群资料，没有从服务器取最新的资料
			txGroup = TxGroup.getTxGroup(GroupInfo.this.getContentResolver(),
					(int) id);
			if (txGroup != null) {
				if (Utils.debug) {
					Log.i(TAG, "群头像地址为：" + txGroup.group_avatar);
				}
				setInfoData(true);
				mSess.getSocketHelper().sendGetGroup(id);
			}

		}

		mRcvMsg.setOnCheckedChangeListener(this);
		mRcvPush.setOnCheckedChangeListener(this);
		mEditGrop.setOnClickListener(this);
		mMember.setOnClickListener(this);
		exitGroup.setOnClickListener(this);
		checkUser();

	}

	private void checkUser() {
		int tmpSign = GroupUtils.userDignity(TX.tm.getUserID(),
				txGroup.group_own_id, txGroup.group_tx_admin_ids);
		switch (tmpSign) {
		case 0:
			if (TxGroup.isPublicGroup(txGroup)) {
				exitGroup.setText("解散聊天室");
			} else {
				exitGroup.setText("解散群");
			}

			break;
		case 1:
			break;
		case 2:
			mEditGrop.setVisibility(View.GONE);
			if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL
					|| txGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC) {
				if (!GroupUtils.inGroup(TX.tm.getUserID(),
						txGroup.group_tx_ids)) {
					exitGroup.setVisibility(View.GONE);
					// mMember.setLayoutParams(new
					// RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,
					// LayoutParams.WRAP_CONTENT));
				}
			} else {
				exitGroup.setText("退出群");
			}
			break;
		default:
			break;
		}
	}

	/**
	 * 设置群资料信息
	 * 
	 * @param isSetAvatar
	 *            如果为false不设置默认群头像，否则头像会闪动
	 */
	public void setInfoData(boolean isSetAvatar) {
		if (TxGroup.isPublicGroup(txGroup)) {
			disGroupInfo.setVisibility(View.GONE);

		} else {

			disGroupInfo.setVisibility(View.VISIBLE);
			group_join_title.setText("群资料");
			group_info_groupidname.setText("群号：");
			group_info_groupCreate.setText("群主：");
			mMember.setText("查看群成员");
		}

		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL) {
			// chanelTypeImage.setVisibility(View.VISIBLE);
			// chanelTypeImage.setBackgroundResource(R.drawable.sl_group_index_chaneltype_guan);
//			imageGuan
//					.setImageResource(R.drawable.sl_group_index_chaneltype_liao);
		} else if (txGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC) {
			// chanelTypeImage.setImageResource(R.drawable.sl_group_index_chaneltype_liao);
		} else if (txGroup.group_type_channel == TxDB.GROUP_TYPE_SECRET) {
			// chanelTypeImage.setImageResource(R.drawable.sl_group_index_chanel_secret);

		} else {
			// chanelTypeImage.setImageResource(R.drawable.sl_group_index_chaneltype_si);
		}
		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL) {
			creator.setText("官方");
		} else {
			creator.setText(smileyParser.addSmileySpans(txGroup.group_own_name,
					true, 0));
		}
		name.setText(smileyParser.addSmileySpans(txGroup.group_title, true, 0));

		groupNum.setText(smileyParser.addSmileySpans(txGroup.group_id + "",
				true, 0));
		introl.setText(smileyParser.addSmileySpans(txGroup.group_sign, true, 0));
		notice.setText(smileyParser.addSmileySpans(txGroup.group_bulletin,
				true, 0));

		// creatorTime.setText("(" + formatTime(txGroup.group_time * 1000) +
		// ")");
		mRcvMsg.setChecked(txGroup.rcv_msg == 1 ? true : false);
		mRcvPush.setChecked(txGroup.rcv_push == 1 ? true : false);
		if (mRcvMsg.isChecked()) {
			mRcvPush.setEnabled(true);
		} else {
			mRcvPush.setEnabled(false);
		}
		avatar.setTag("group_" + txGroup.group_id);
		String storagePath = Utils.getStoragePath(GroupInfo.this);
		File sddir = new File(storagePath, Utils.AVATAR_PATH);
		if (!sddir.exists() && !sddir.mkdirs()) {
			if (Utils.debug)
				Log.e(TAG, "bitmapFromUrl---Create dir failed");
			sddir.mkdir();
		}

		Bitmap bm = getGroupCachedBitmap(txGroup.group_id);
		if (bm != null) {
			avatar.setImageBitmap(bm);
		} else {
			loadGroupImg(txGroup.group_avatar, txGroup.group_id, avatarCallback);
			if (isSetAvatar) {
				avatar.setImageResource(R.drawable.qun_default);
			}
		}

		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL) {
			// 加载上周活跃之星的GridView
			ArrayList<Long> starsIdList = GroupMsgRoom.mGroupStarsMap
					.get(txGroup.group_id);
			if (starsIdList == null || starsIdList.size() == 0) {
				return;
			}
			ll_LastWeekStars.setVisibility(View.VISIBLE);
			mStarAdapter = new GroupMsgRoom.GroupStarAdapter(thisContext,
					GroupMsgRoom.mGroupStarsMap.get(txGroup.group_id),mSess);
			mStarGridView.setAdapter(mStarAdapter);
		}

	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		switch (buttonView.getId()) {
		case R.id.group_info_accaptmessage_checkbox:
			if (isChecked) {
				mRcvPush.setEnabled(true);
			} else {
				mRcvPush.setEnabled(false);
			}
			mSess.getSocketHelper().sendGroupRemind(
					txGroup.group_id, isChecked, mRcvPush.isChecked());
			break;
		case R.id.group_info_noticemessage_checkbox:
			mSess.getSocketHelper().sendGroupRemind(
					txGroup.group_id, mRcvMsg.isChecked(), isChecked);
			break;
		}
	}

	@Override
	public void onClick(View v) {
		Intent i;
		switch (v.getId()) {
		case R.id.group_info_members:
			i = new Intent(GroupInfo.this, GroupMember.class);

			i.putExtra(Utils.MSGROOM_GROUP_ID, txGroup.group_id);
			startActivity(i);
			break;
		case R.id.group_info_editbtn:
			i = new Intent(GroupInfo.this, GroupEdit.class);
			i.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
			startActivityForResult(i, 10);
			break;
		case R.id.group_info_exit_btn:
			/*
			 * i = new Intent(GroupInfo.this, GroupEdit.class);
			 * i.putExtra(Utils.MSGROOM_TX_GROUP, txGroup); startActivity(i);
			 */

			AlertDialog.Builder nameDialog1 = new AlertDialog.Builder(
					GroupInfo.this);
			if (exitGroup.getText().toString() == "解散聊天室") {
				nameDialog1.setTitle("确认解散聊天室?");
			} else {
				nameDialog1.setTitle("确认退出聊天室?");
			}
			nameDialog1.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							exitGroup();
						}
					});
			nameDialog1.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});
			nameDialog1.show();

			break;
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 10 && resultCode == 20) {
			intent = data;
			txGroup = intent.getParcelableExtra(Utils.MSGROOM_TX_GROUP);
			if (txGroup != null) {
				setInfoData(true);
			}
		}

	}

	private void exitGroup() {
		showDialogTimer(this, R.string.prompt, R.string.group_sending,
				10 * 1000).show();
		if (Utils.debug)
			Log.i(TAG, "dealCreateGroup---dealCreateGroup:_exitGroup_:"
					+ txGroup.group_id);

		ArrayList<Long> ids = new ArrayList<Long>();
		ids.add(TX.tm.getUserID());
		mSess.getSocketHelper().sendDealGroup(txGroup.group_id,
				false, ids);
		// GroupIndex.CHANGE=true;

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
			filter.addAction(Constants.INTENT_ACTION_DEL_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_SETTING_GROUP_RESULT);
			filter.addAction(Constants.INTENT_ACTION_GET_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_DEL_GROUP_MEMBER.equals(intent
					.getAction())) {
				dealCreateGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_SETTING_GROUP_RESULT
					.equals(intent.getAction())) {
				dealSettingResult(serverRsp);
			} else if (Constants.INTENT_ACTION_GET_GROUP.equals(intent
					.getAction())) {
				dealGetGroupInfo(serverRsp);
			}
		}
	}

	/**
	 * 获取群资料返回
	 * 
	 */
	private void dealGetGroupInfo(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Bundle bundle = serverRsp.getBundle();
			txGroup = bundle.getParcelable(Utils.MSGROOM_TX_GROUP);
			if (txGroup.group_avatar != null) {
				// 判断如果本地头像地址存在，则删除本地头像，从新下载，确保群头像最新
				String file = mSess.mDownUpMgr.getAvatarFile(
						txGroup.group_avatar, true, txGroup.group_id, false);
				if (file != null) {
					File avatar = new File(file);
					if (avatar.exists()) {
						avatar.delete();
					}
				}
			}
			if (Utils.debug) {
				Log.i(TAG, "收到广播的群头像地址:" + txGroup.group_avatar);
			}
			setInfoData(false);

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

	public void dealSettingResult(ServerRsp serverRsp) {

		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			// Utils.startPromptDialog(GroupInfo.this,
			// R.string.prompt,R.string.group_setting_result_success);
			Toast.makeText(GroupInfo.this,
					R.string.group_setting_result_success, Toast.LENGTH_SHORT)
					.show();
			break;
		case OPT_FAILED:
			Toast.makeText(GroupInfo.this, "设置失败,请重新设置", Toast.LENGTH_SHORT)
					.show();
			mRcvMsg.setChecked(txGroup.rcv_msg == 1 ? true : false);
			mRcvPush.setChecked(txGroup.rcv_push == 1 ? true : false);

			break;
		default:
			break;
		}

	}

	public void dealCreateGroup(ServerRsp serverRsp) {
		cancelDialogTimer();
		//
		switch (serverRsp.getStatusCode()) {
		case GROUP_MEMBER_OPT_NO_PERMISSION:
			Utils.startPromptDialog(GroupInfo.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_MEMBER_SIZE_INVALID:
			Utils.startPromptDialog(GroupInfo.this, R.string.prompt,
					R.string.create_group_more_few);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupInfo.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case RSP_OK:
		case GROUP_NO_EXIST:// 群不存在
		case GROUP_LEAVE:// 退出此群
		case GROUP_DISSOLVED:// 群解散
			// 删除消息会话页面的该群的会话条目
			MsgStat.delMsgStatByGroupId(mSess.getContentResolver(), txGroup.group_id);// 删除群组消息会话条目
			// TxGroup tempTxGroup = null;
			// for (TxGroup group : TxGroup.mGroups) {
			// if (group.group_id == txGroup.group_id) {
			// tempTxGroup = group;
			// }
			// }
			// if(tempTxGroup!=null){
			// //缓存中删除这个群组
			// TxGroup.mGroups.remove(tempTxGroup);
			// }
			//
			ContentValues values = new ContentValues();
			values.put(TxDB.Qun.QU_TX_STATE, TxDB.QU_TX_STATE_OUT);// 已经不在群中
			TxGroup.updateTxGroup(mSess.getContentResolver(), txGroup.group_id, values);

			TxData.finishOne(GroupMsgRoom.class.getName());
			this.finish();
			break;

		}
	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			avatar.setImageBitmap(result);
		}
	};
	private TextView group_join_title;
	private TextView group_info_groupidname;
	private TextView group_info_groupCreate;
	private LinearLayout btn_back_left_groupinfo;

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	};
}
