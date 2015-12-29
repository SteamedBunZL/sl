package com.tuixin11sms.tx.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.Contact;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class SingleMsgRoom extends BaseMsgRoom implements OnTouchListener {
	private static final String TAG = SingleMsgRoom.class.getSimpleName();

	/******* result返回消息 *****/
	public static final int ACTION_SCAN_USERINFORMATION = 10;

	/******* 各种广播变量 *****/

	private LocationReceiver locationReceiver;

	private PopReceiver popReceiver;

	// /******* 数据部分变量 *****/
	// private boolean isAddOld = true;// 是否加载历史数据
	// private boolean isFlushBottem = true;// 列表是否刷新到底部
	private Long lastClickTime = System.currentTimeMillis();

	public String push_str;

	private long partner_id;

	private ConnectionChangeReceiver ccr;

	// 消息变化的handler
	private Handler msgHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {

			case SocketHelper.CHAT_MSG_LIST_CHANGED:

				dealChatMsgChanged(msg);
				break;

			}
			super.handleMessage(msg);
		}

	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		processExtraData(true);
	}

	// 更新最新消息界面数据
	public void updataNewMessage() {
		if (txMsgs != null && txMsgs.size() > 0) {
			TXMessage txmsg = null;
			for (int i = txMsgs.size() - 1; i >= 0; i--) {
				TXMessage msg = txMsgs.get(i);
				if (msg.msg_type < 30) {// <30都是群消息或者个人神聊消息
					// ms.txMsgCount++;
					txmsg = msg;
					break;
				}
			}
			// TXMessage txmsg=txMsgs.get(txMsgs.size()-1);
			if (txmsg != null) {
				MsgStat msgStat = null;
				if (tx != null && Utils.isIdValid(partner_id)) {
					// tx不为空，且其partner_id有效
					msgStat = MsgStat.updateMsgStatByTxmsg(txmsg,
							mSess.getContentResolver(), TxDB.MS_TYPE_TB,
							txmsg.gmid, 0, true);
					msgStat.setPartnerID(partner_id);
					msgStat.setPartnerName(tx.getNick_name());
					// msgStat.setPhone(tx.getPhone());
					SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
				} else {
					// msgStat = MsgStat.convertTxMsgToMsgStat(txmsg, txdata,
					// TxDB.MS_TYPE_CS);//因为TxDB.MS_TYPE_CS类型不会被保存到数据库中
				}

			}
		}
	}

	protected void onStart() {
		// 判断该手机是否支持录音设备
		// if(!Utils.recordIsAvailable()){
		// Utils.creatNoRecordInfo(this);
		// }
		isPhone = false;
		if (ccr == null) {
			ccr = new ConnectionChangeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			this.registerReceiver(ccr, filter);
		}
		if (msgMaxReceiver == null) {
			msgMaxReceiver = new MsgMaxReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_CLEAR_MSGS_FINISH);
			this.registerReceiver(msgMaxReceiver, filter);
		}

		if (locationReceiver == null) {
			locationReceiver = new LocationReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_OK);
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_FAILED);
			this.registerReceiver(locationReceiver, filter);
		}

		if (popReceiver == null) {
			popReceiver = new PopReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_POPWINDOW_SEND);
			registerReceiver(popReceiver, filter);
		}
		Utils.isNotificationShow = true;

		super.onStart();
	}

	long mUpdateId = 0;

	@Override
	protected void onRestart() {
		isPhone = false;
		super.onRestart();
	}

	@Override
	protected void processExtraData(boolean isInOnCreat) {
		if (!isInOnCreat) {
			initMsgRoomData();
		}

		if (Utils.debug)
			Log.i(TAG, "单聊设置数据 notifhDataSetChanged消息adapter");
		contectsListAdapter = new SingleAdapter();
		if (txMsgs != null) {
			contectsListAdapter.setData(txMsgs);
		}
		msgRoomList.setAdapter(contectsListAdapter);

		/**** 数据初始化 *****/

		if (toastList == null) {
			toastList = new ArrayList<Toast>();
		}

		getIntentData();// 接收intent数据
		/**** 数据读取 *****/
		if (tx != null) {
			MsgStat ms = MsgStat.getMsgStatByPartnerId(partner_id);
			if (ms != null) {
				ms.no_read = 0;
			}
		}

		id_lastTxmsg = partner_id;
		// 如果tx中的头像时空的，再取一次getTx（）也是空，没用 2014.06.19 shc
		// 单聊加载个人头像
		// if (TextUtils.isEmpty(tx.avatar_url)) {
		// // 传递过来的TX中没有avatar_url，现在这里查询一下，不过不太合理。
		// TX tx_temp = TX.tm.getTx(partner_id);
		//
		// if (tx_temp != null)
		// tx.avatar_url = tx_temp.avatar_url;
		// }

		// 根据聊天室状态来改变界面显示
		setNameStr();

	}

	protected void onStop() {
		if (ccr != null) {
			unregisterReceiver(ccr);
			ccr = null;
		}
		if (locationReceiver != null) {
			unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		mSess.getSocketHelper().sendNoReadMsg();

		// Utils.roomstate = Utils.IN_OTHER_ROOM;
		// Utils.roomid = -1;
		// updataNewMessage();
		// System.out.println("stop");
		super.onStop();
	}

	protected void onPause() {
		Utils.xf_id = "";
		// Utils.xf_partner_id = null;
		if (toastList != null) {
			for (Toast tempToast : toastList) {
				if (tempToast != null) {
					tempToast.cancel();
				}
			}
		}
		super.onPause();
		MobclickAgent.onPause(this);
	}

	protected void onResume() {
		isPhone = false;
		if (Utils.isIdValid(partner_id)) {
			TX tx1 = TX.tm.getTx(partner_id);
			if (tx1 != null && contectsListAdapter != null) {
				if (Utils.debug)
					Log.i(TAG, "单聊 onResume notifhDataSetChanged消息adapter");
				contectsListAdapter.updateListInfo(tx1);
				contectsListAdapter.notifyDataSetChanged();
			}
			mUpdateId = 0;
		}
		btn_recordShortAduio.setText(R.string.publicmsg_record);
		Utils.roomstate = Utils.IN_SINGLE_ROOM;
		Utils.roomid = partner_id;
		Utils.xf_id = "" + partner_id;
		if (!Utils.isIdValid(partner_id)) {
			Utils.xf_id = tx.getPhone();
		}
		TX ttx = TX.tm.getTx(partner_id);

		if (tx != null && ttx != null && ttx.getRemarkName() != null) {
			tx.setRemarkName(ttx.getRemarkName());
		}

		if (tx != null && ttx != null && ttx.getNick_name() != null) {
			tx.setNick_name(ttx.getNick_name());
			setNameStr();
		}

		Utils.isNotificationShow = true;
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onDestroy() {
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorEventListener);
		}
		if (locationReceiver != null) {
			unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		if (msgMaxReceiver != null) {
			unregisterReceiver(msgMaxReceiver);
			msgMaxReceiver = null;
		}
		if (popReceiver != null) {
			unregisterReceiver(popReceiver);
			popReceiver = null;
		}
		// recordVolume.pause();
		ls = null;
		musicUtils.release();
		Utils.roomstate = Utils.IN_OTHER_ROOM;
		Utils.roomid = -1;
		// System.out.println("onDestroy");
		Utils.isNotificationShow = true;
		// 注销
		if (tx != null) {
			mSess.getSocketHelper().unRegisterSingleHandler(partner_id);
		}
		updataNewMessage();
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQ_TAKE_PHOTO) {
			// TuixinService1.notNeedCheckActivityState = false;
			switch (resultCode) {
			case RESULT_OK:
				Bitmap tempimg = Utils.fitSizeAutoImg(sendimgPath,
						Utils.msgroom_list_resolution);
				if (tempimg == null && data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						tempimg = (Bitmap) extras.get("data");
						if (Utils.createPhotoFile(tempimg, "" + img_msg_id
								+ ".jpg") == null) {
							return;

						}
					} else {
						String path = data.getDataString();
						if (!Utils.isNull(path)) {
							Uri uri = Uri.parse(path);
							sendimgPath = getRealPathFromURI(uri);
						}
					}
				}
				tempimg = null;
				if (!Utils.isNull(sendimgPath)) {
					send("");
				}

				// Utils.inPhoto = false;
				break;
			default:
				sendimgPath = null;// 同群聊天室问题解释
			}
		} else if (requestCode == REQ_TAKE_PICTURE) {
			// TuixinService1.notNeedCheckActivityState = false;
			switch (resultCode) {
			case RESULT_OK:
				// 选择照片
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				cursor.moveToFirst();
				String imagePath = cursor.getString(1);
				cursor.close();
				try {
					Intent i = new Intent(this, EditSendImg.class);
					i.putExtra(EditSendImg.LOCAL, imagePath);
					startActivityForResult(i, REQ_EDIT_PICTURE);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, e.getMessage(), e);
				}

				break;
			}
		} else if (requestCode == REQ_EDIT_PICTURE) {
			if (resultCode == Activity.RESULT_OK) {
				if (data != null) {
					sendimgPath = data.getStringExtra("srcurl");
					send("");
				}
			}
		} else if (SelectFriendListActivity.CHAT_TYPE_CARD == requestCode) {// 名片返回
			if (resultCode == SelectFriendListActivity.CHAT_TYPE_CARD) {

				TX txCard = data
						.getParcelableExtra(SelectFriendListActivity.CHAT_TYPE_CARD_OBJ);
				sendCard(txCard);
			}
		} else if (requestCode == ACTION_SCAN_USERINFORMATION) {// 屏蔽好友后返回更新变量

			// TX t1 = TX.tm.getTx(tx.partner_id);
			//
			// if (tx != null && t1 != null) {
			// tx.setIn_black_list(t1.getIn_black_list());
			// //这里不用再存储，因为t1就是从缓存中取出的，和数据库是一致的 2014.01.21 shc
			// }

		} else if ((requestCode == REQ_TAKE_BIG_FILES || requestCode == REQ_TAKE_MUSIC_FILES)
				&& resultCode == Activity.RESULT_OK) {
			// 选择发送大文件

			dealSendBigFile(requestCode, data);

		} else if (requestCode == REQ_TAKE_GIF_FILE
				&& resultCode == Activity.RESULT_OK) {
			try {
				dealSetGif(requestCode, data);
			} catch (IOException e) {
				e.printStackTrace();
			}

		} else if (requestCode == REQ_TAKE_GIF_SETTING
				&& resultCode == Activity.RESULT_OK) {
			try {
				dealSetGif(requestCode, data);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	protected void uploadAndAddBigFileMsg(String filePath) {
		final TXMessage txmsgTemp = TXMessage.creatBigFileSms(partner_id,
				tx.getNick_name(), tx.getPhone(), filePath,
				mSess.getServerTime());
		uploadBigFile(txmsgTemp);// 上传大文件
		// 直接置为下载中状态，防止出现无网络点击下载按钮没反应，多个下载任务添加到队列的bug，后期还是要找多个重复任务添加到队列的问题所在
		txmsgTemp.updateState = TXMessage.UPDATING;
		mSess.getSocketHelper().addSingleMessage(txmsgTemp);

	}

	// 发送名片
	public void sendCard(TX txCard) {
		TXMessage txMessageCard = null;
		// 组消息对象
		if (txCard != null) {
			if (Utils.isIdValid(txCard.partner_id)) {
				txMessageCard = TXMessage.creatTCardEms(SingleMsgRoom.this,
						partner_id, txCard.getNick_name(),
						txCard.getNick_name(), txCard.partner_id, txCard.sign,
						txCard.getSex(), txCard.getPhone(), txCard.avatar_url,
						true, TXMessage.NOT_SENT, mSess.getServerTime());
				txMessageCard.updateState = TXMessage.UPDATE;
				mSess.getSocketHelper().sendSingleMessage(txMessageCard);

			}

		}
	}

	public void sendGif() {

	}

	@Override
	public void send(String text) {
		// if (!txdata.dataChange)
		// txdata.dataChange = true;
		// 发送按钮频率
		Long justClickTime = System.currentTimeMillis();
		if (justClickTime - lastClickTime < 1000) {
			// Toast.makeText(MyChatRoom.this, R.string.click_too_fast,
			// 1)
			// .show();
			lastClickTime = justClickTime;
			return;
		}

		if (sendimgPath == null) {
			lastClickTime = justClickTime;
			TXMessage txmsgTemp = TXMessage.creatCommonSms(SingleMsgRoom.this,
					partner_id, tx.getNick_name(), tx.getPhone(), text, true,
					TXMessage.NOT_SENT, mSess.getServerTime());
			mSess.getSocketHelper().sendSingleMessage(txmsgTemp);

		} else {

			lastClickTime = justClickTime;
			final TXMessage txmsgTemp;
			txmsgTemp = TXMessage.creatImageEms(SingleMsgRoom.this, partner_id,
					tx.getNick_name(), tx.getPhone(), sendimgPath, "", true, 0,
					mSess.getServerTime());
			txmsgTemp.updateState = TXMessage.UPDATING;
			Utils.executorService.submit(new Runnable() {// 大小图创建异步
						@Override
						public void run() {
							postImgSocket(txmsgTemp);
						}
					});
			mSess.getSocketHelper().addSingleMessage(txmsgTemp);
			sendimgPath = null;
		}

	}

	ViewHolder holder = null;

	// menu菜单

	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0,MENU_DELET_MESSAGE,0,"删除对话");
		// menu.add(0,MENU_QUANTITYDELET_MESSAGE,1,"批量删除");
		// menu.add(0,MENU_INSERT_CONTENT,2,"插入联系人");
		// menu.add(0,MENU_CHANGE_CHANNEL,3,"切换通道");
		// menu.add(0,MENU_CHECK_CONTENT_INFO,4,"查看联系人信息");
		// menu.add(0,MENU_USER_INSTRUCT,5,"用户指引"
		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.msg_menu, menu);
		return super.onCreateOptionsMenu(menu);

	}

	// 单聊群聊一样，写在了BaseMsgRoom中 2014.01.21 shc
	// public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
	// if (wireControl == WIRECONTROL_PLAY) {
	// return false;
	// }
	// super.onPrepareOptionsMenu(menu);
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.msgroom_menu_checkContentInfo:

			if (Utils.isIdValid(partner_id)) {
				if (partner_id == TX.TUIXIN_MAN) {
					Intent intent = new Intent(SingleMsgRoom.this,
							UserInformationActivity.class);
					intent.putExtra(UserInformationActivity.pblicInfo,
							UserInformationActivity.TUIXIN_USER_INFO);
					intent.putExtra(UserInformationActivity.UID, partner_id);
					startActivity(intent);
				} else {
					Intent intent = new Intent(SingleMsgRoom.this,
							UserInformationActivity.class);
					int infoType = TX.tm.isTxFriend(partner_id) ? UserInformationActivity.TUIXIN_USER_INFO
							: UserInformationActivity.NOT_TUIXIN_USER_INFO;
					intent.putExtra(UserInformationActivity.pblicInfo, infoType);
					intent.putExtra(UserInformationActivity.UID, partner_id);
					startActivity(intent);

				}
			}
			break;
		case R.id.msgroom_menu_deletMsg:
			new AlertDialog.Builder(SingleMsgRoom.this)
					.setMessage(R.string.msg_delet_allmessage)
					.setPositiveButton(R.string.confirm,
							new DialogInterface.OnClickListener() {
								public void onClick(
										DialogInterface dialoginterface, int i) {
									deletAllMsg();
									dialoginterface.dismiss();
									finish();

								}
							})
					.setNegativeButton(R.string.cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.dismiss();
								}
							}).show();// 显示对话框

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	// 删除整个会话
	public void deletAllMsg() {
		mSess.getSocketHelper().deleteSingleMessageAll(partner_id);
		MsgStat.delMsgStat(mSess.getContentResolver(), partner_id);
	}

	class popListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private TXMessage txmsg;

		public popListAdapter(TXMessage txmsg) {
			this.mInflater = LayoutInflater.from(SingleMsgRoom.this);
			this.txmsg = txmsg;
		}

		@Override
		public int getCount() {
			int count = 3;
			if (txmsg.was_me && txmsg.read_state == 0) {
				count = 4;
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			convertView = this.mInflater.inflate(R.layout.msg_list_poplist,
					null);
			return convertView;
		}

	}

	public int screeScoW;

	public void startPhotoCapture() {
		if (!Utils.isIdValid(partner_id)) {
			img_msg_id = Long.parseLong(TXMessage.default_sms_id);
		} else {
			img_msg_id = Utils.createMsgId("" + this.partner_id);
		}
		if (!Utils.checkSDCard()) {
			return;
		}
		super.startPhotoCapture();
	}

	private final class MySensorEventListener implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			//
			// i++;

			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
				// if(Utils.listener_state == 0){
				// if(x != maxDistance){
				// //靠近
				// audioManager.setMode(isCallState==0?AudioManager.MODE_NORMAL:AudioManager.MODE_IN_CALL);
				// }else{
				// //离开
				// audioManager.setMode(isCallState==1?AudioManager.MODE_NORMAL:AudioManager.MODE_IN_CALL);
				// }
				// }

			}
			// System.out.println("isCallState="+isCallState);
			// System.out.println("Utils.listener_state="+Utils.listener_state);
			if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
				// tv.append("光线感应。。。");

			}

			if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
				// tv.append("重力感应。。。");
			}

		}
	}

	private SensorManager sensorManager;
	private MySensorEventListener sensorEventListener;

	// 创建信息弹出提示
	ArrayList<Toast> toastList = new ArrayList<Toast>();

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		//
		if (hasFocus) {// 界面全部加在完毕
			flush(UPDATE_WINDOWCOMPLETE);
		}
		super.onWindowFocusChanged(hasFocus);
	}

	// 图片专为字节
	public static byte[] BitmapToBytes(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		return baos.toByteArray();
	}

	// 去发送文字的前后空格回车
	public String trimSendStr(String SendStr) {
		String NewStr = null;
		if (SendStr != null) {
			NewStr = SendStr.trim();
		}

		return NewStr;
	}

	class LocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					Constants.INTENT_ACTION_GET_LOCATION_OK)
					|| intent.getAction().equals(
							Constants.INTENT_ACTION_GET_LOCATION_FAILED)) {
				if (ls == null) {
					return;
				}
				Parcelable parcelable = intent
						.getParcelableExtra(Constants.EXTRA_LOCATION_KEY);
				Location location;
				double latitude;
				double longitude;
				if (parcelable != null) {
					location = (Location) parcelable;
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				} else {
					latitude = 0;
					longitude = 0;
				}

				if (gepTxmsgTemp != null) {

					gepTxmsgTemp.geo = TxData.public_latitude + ","
							+ TxData.public_longitude;
					isLocationing = false;
					if (latitude == 0 && longitude == 0) {
						gepTxmsgTemp.updateState = TXMessage.UPDATE_FAILE;
						TXMessage.saveTXMessagetoDB(gepTxmsgTemp,
								mSess.getContentResolver(), true);

						return;
					}
					gepTxmsgTemp.updateState = TXMessage.UPDATE_OK;
					mSess.getSocketHelper().sendSingleMessage(gepTxmsgTemp);
				}
			}

		}

	}

	// 设置显示名称
	@Override
	public void setNameStr() {
		if (tx != null) {
			String sendName = "";
			if (!Utils.isNull(tx.getRemarkName())) {
				sendName = tx.getRemarkName();
			} else {
				if (!Utils.isNull(tx.getNick_name())) {
					sendName = tx.getNick_name();
				} else if (Utils.isIdValid(partner_id)) {
					sendName = "" + partner_id;
				} else if (!Utils.isNull(tx.getPhone())) {
					sendName = tx.getPhone();
				}
			}

			personName.setText(smileyParser.addSmileySpans(sendName, true, 0));
		}
	}

	// 通过intent读取数据
	void getIntentData() {
		partner_id = mainIntent.getLongExtra(Utils.MSGROOM_TX,
				Utils.DEFAULT_NUMBER);
		if (partner_id == TX.TUIXIN_MAN) {
			tx = TX.tm.getTxMan();
		} else {
			tx = TX.tm.getTx(partner_id);
		}
		// threadId = mainIntent.getIntExtra(Utils.MSGROOM_THREAD_ID, 0);
		//
		// if (threadId == 0 && !Utils.isNull(tx.getPhone())) {
		// threadId = -1;// (int) MmsUtils.getOrCreateThreadId(this, new
		// // String[] { tx.phone });
		// }
		// threadId = 0;
		autoTxmsg = mainIntent
				.getParcelableExtra(SelectFriendListActivity.CHAT_TYPE_ZF_OGJ);

		if (tx != null) {
			mGroupAsynloader.post(new Runnable() {
				@Override
				public void run() {
					// Looper.prepare();
					// 注册并读取txmsg
					if (tx != null) {
						synchronizedMsgs = mSess.getSocketHelper()
								.registerSingleHandler(partner_id, msgHandler);
					}
					txMsgs.clear();
					synchronized (synchronizedMsgs) {
						txMsgs.addAll(synchronizedMsgs);
					}
					if (txMsgs.size() > 0) {
						for (TXMessage txMsgs1 : txMsgs) {
							mAudioRecPlayer.addTalkCache(txMsgs1,
									isStartAudioPlay);
						}
					}
					updataNewMessage();
					flush(FLUSH_ROOM_INIT);
					NotificationPopupWindow.showPersonNotification(partner_id,
							false);
				}
			});

		} else {
			mSess.getSocketHelper().getUserDetail(partner_id,
					new AsyncCallback() {

						@Override
						public void onFailure(Throwable t, long id) {

						}

						@Override
						public void onSuccess(Object result, long id) {
							if (Utils.debug) {
								Log.i(TAG, "获取到了" + partner_id + "详细信息返回");
							}

						}
					});
			this.finish();
			return;
		}
		// 接收
		// isWindowCome = mainIntent.getBooleanExtra(Utils.MSGROOM_WINDOW_IN,
		// false);//注掉原因找MSGROOM_WINDOW_IN的定义处
		boolean forceClear = mainIntent.getBooleanExtra(
				NotificationPopupWindow.FORCE_CLEAR, false);
		if (forceClear) {
			NotificationPopupWindow.cancelNotification(SingleMsgRoom.this);
		}
	}

	// 点击名片跳转
	public void clickCard(TXMessage txmsg) {
		if (txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS) {

		} else {
			if (TX.tm.getUserID() != txmsg.tcard_id) {
				TX tx1 = null;
				if (tx1 == null) {
					if (txmsg != null) {
						tx1 = TX.tm.getTx(txmsg.tcard_id);
						if (tx1 == null) {
							tx1 = new TX();
							tx1.setPartnerId(txmsg.tcard_id);
							tx1.setNick_name(txmsg.tcard_name);
							tx1.setAvatar_url(txmsg.tcard_avatar_url);
							tx1.setSign(txmsg.tcard_sign.trim());
							tx1.setSex(txmsg.tcard_sex);
							tx1.setPhone(txmsg.tcard_phone);

							TX.tm.addTx(tx1);
						}
					}

				}

				Intent intent = new Intent(SingleMsgRoom.this,
						UserInformationActivity.class);
				int infoType = TX.tm.isTxFriend(txmsg.tcard_id) ? UserInformationActivity.TUIXIN_USER_INFO
						: UserInformationActivity.NOT_TUIXIN_USER_INFO;
				intent.putExtra(UserInformationActivity.pblicInfo, infoType);
				intent.putExtra(UserInformationActivity.UID, tx1.partner_id);
				startActivity(intent);
			} else {
				// Intent intent = new Intent(SingleMsgRoom.this,
				// UserInformationActivity.class);
				// intent.putExtra(UserInformationActivity.pblicInfo,
				// UserInformationActivity.SETINGTOUSERINFO);
				// intent.putExtra(UserInfoActivity.infoTX, tx);
				Intent intent = new Intent(SingleMsgRoom.this,
						SetUserInfoActivity.class);
				startActivity(intent);
			}

		}
	}

	private class ConnectionChangeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			int state = Utils.getNetworkType(context);
			switch (state) {
			case Utils.NET_NORMAL:
			case Utils.NET_WIFI:
				// 断网拉离线消息，延时35s
				if (Utils.debug)
					Log.i(TAG, "测试log日志输出" + state);
				msgHandler.postDelayed(new Runnable() {

					@Override
					public void run() {
						Log.i("dealSinleOfflineMessage", "在这里拉离线消息");
						mSess.getSocketHelper().sendGetSingleOfflineMsg(
								TX.tm.getUserID());
					}
				}, 35000);
				// if (Utils.idNotInvalid(tx.partner_id))
				// changeSendChannel(SEND_TX);
				break;
			default:
				// changeSendChannel(SEND_MESSAGE);
				break;
			}
		}

	}

	// 达到500条消息上限的处理
	MsgMaxReceiver msgMaxReceiver;

	class MsgMaxReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (intent.getAction().equals(
					Constants.INTENT_ACTION_CLEAR_MSGS_FINISH)) {
				String[] msgids = intent
						.getStringArrayExtra(Constants.EXTRA_MSGIDS_KEY);
				for (TXMessage tempTxmsg : txMsgs) {
					for (String tempmsgids : msgids) {
						long msgid = 0;
						msgid = Long.parseLong(tempmsgids);
						if (msgid > 0) {
							if (tempTxmsg.msg_id == msgid) {
								txMsgs.remove(tempTxmsg);
								break;
							}
						}
					}
				}
			}
		}

	}

	class PopReceiver extends BroadcastReceiver {// 弹窗广播

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					Constants.INTENT_ACTION_POPWINDOW_SEND)) {
				TXMessage txmsg = intent
						.getParcelableExtra(Constants.INTENT_ACTION_POPWINDOW_SEND_MSG);
				if (txmsg != null) {
					txMsgs.add(txmsg);
				}
			}
		}

	}

	// 下载名片 vcard
	DownUploadListner mVCardCallback = new DownUploadListner() {
		@Override
		public void onStart(FileTaskInfo taskInfo) {
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage msg = (TXMessage) taskInfo.mObj;
			msg.updateCount = 0;
			flush(FLUSH_LISTVIEW_FALSE);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			String msg_path = taskInfo.mFullName;
			txmsg.msg_path = msg_path;
			txmsg.updateState = TXMessage.UPDATE_OK;
			txmsg.read_state = TXMessage.READ;
			String name = msg_path.substring(msg_path.lastIndexOf("/") + 1,
					msg_path.length());
			Contact contact = new Contact(SingleMsgRoom.this);
			contact.readToFile(name, SingleMsgRoom.this);
			ContentValues values = new ContentValues();
			values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
			cr.update(TxDB.Messages.CONTENT_URI, values, TxDB.Messages.MSG_ID
					+ "=?", new String[] { "" + txmsg.msg_id });
			// TXMessage.saveTXMessagetoDB(txmsg, msgactTxdata);
			flush(FLUSH_LISTVIEW_FALSE);
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			flush(FLUSH_LISTVIEW_FALSE);
		}
	};

	@Override
	public TXMessage getGeoTxMsg(String geo) {
		//
		TXMessage txmsgTemp = TXMessage.creatGeoSms(SingleMsgRoom.this,
				partner_id, tx.getNick_name(), geo, true, 0,
				mSess.getServerTime());

		if (txmsgTemp != null) {

			return txmsgTemp;

		}
		return null;
	}

	@Override
	public void sendMsg(TXMessage txmsg) {
		mSess.getSocketHelper().sendSingleMessage(txmsg);
	}

	@Override
	public void upToolList1Logic() {
		if (Utils.isIdValid(partner_id)) {
			Intent creatInfoIntent = new Intent(this,
					SelectFriendListActivity.class);
			creatInfoIntent.putExtra(
					SelectFriendListActivity.CHAT_TYPE_SINGLE_TX, tx);
			creatInfoIntent.putExtra(SelectFriendListActivity.CHAT_TYPE,
					SelectFriendListActivity.CHAT_TYPE_SINGLE);
			startActivity(creatInfoIntent);
		}
	}

	@Override
	protected void clearAllMsg() {
		new AlertDialog.Builder(SingleMsgRoom.this)
				.setMessage(R.string.msg_delete_all)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								deletAllMsg();
								dialoginterface.dismiss();
								// finish();

							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.dismiss();
							}
						}).show();// 显示对话框

	}

	@Override
	public void addMsg(TXMessage txmsg) {
		//
		mSess.getSocketHelper().addSingleMessage(txmsg);
	}

	// 创建待转发的音频消息
	@Override
	public TXMessage getForwardImgTxmsg(String sendImgPath, TXMessage txmsg) {
		TXMessage txmsgTemp = TXMessage.creatImageEms(mSess.getContext(),
				partner_id, tx.getNick_name(), tx.getPhone(), sendImgPath,
				txmsg.msg_url, true, 0, mSess.getServerTime());
		txmsgTemp.updateState = TXMessage.UPDATE_OK;
		return txmsgTemp;
	}

	@Override
	public TXMessage getNewAudioTxmsg() {
		//
		TXMessage audioTxmsg = TXMessage.creatAudioEms(SingleMsgRoom.this,
				partner_id, tx.getNick_name(), tx.getPhone(), "", "", 0, 0,
				Utils.sms_adiou_name, true, 0, mSess.getServerTime());
		return audioTxmsg;
	}

	/** 创建一个待转发的音频消息 */
	@Override
	public TXMessage getForwardAudioTxmsg(TXMessage txmsg) {
		TXMessage tempTxmsg = TXMessage.creatAudioEms(mSess.getContext(),
				partner_id, tx.getNick_name(), tx.getPhone(), txmsg.msg_path,
				txmsg.msg_url, txmsg.msg_file_length, txmsg.audio_times,
				Utils.sms_adiou_name, true, 0, mSess.getServerTime());
		tempTxmsg.updateState = TXMessage.UPDATE_OK;
		return tempTxmsg;
	}

	// 创建待转发的位置消息
	public TXMessage getForwardGeoTxmsg(TXMessage txmsg) {
		TXMessage txmsgTemp1 = TXMessage.creatGeoSms(mSess.getContext(),
				partner_id, tx.getNick_name(), txmsg.geo, true,
				TXMessage.NOT_SENT, mSess.getServerTime());
		txmsgTemp1.updateState = TXMessage.UPDATE;
		return txmsgTemp1;
	}

	// 创建待转发的名片消息
	public TXMessage getForwardTCardTxmsg(TXMessage txmsg) {
		TXMessage txmsg2 = TXMessage.creatTCardEms(mSess.getContext(),
				partner_id, txmsg.contacts_person_name, txmsg.tcard_name,
				txmsg.tcard_id, txmsg.tcard_sign, txmsg.tcard_sex,
				txmsg.tcard_phone, txmsg.tcard_avatar_url, true,
				TXMessage.NOT_SENT, mSess.getServerTime());
		return txmsg2;
	}

	@Override
	public void changeMsg(TXMessage txmsg) {
		//
		mSess.getSocketHelper().changeSingleMessageState(partner_id,
				txmsg.msg_id, txmsg.read_state);
	}

	/** 确认删除消息的点击事件处理 */
	@Override
	public void deleteTxMsg(DialogInterface dialoginterface, TXMessage txmsg) {

		// 如果是文件任务，则取消任务发送
		if (txmsg.was_me) {
			switch (txmsg.msg_type) {
			case TxDB.MSG_TYPE_IMAGE_EMS:
			case TxDB.MSG_TYPE_AUDIO_EMS:
			case TxDB.MSG_TYPE_CARD_EMS:
			case TxDB.MSG_TYPE_TCARD_SMS:
			case TxDB.MSG_TYPE_GEO_SMS:
				// 如果是我发送的消息，则为上传消息，任务id为文件全路径
				String uploadTaskId = mSess.mDownUpMgr.getUploadTaskId(Utils
						.getUploadImageTempPath(txmsg.msg_id));
				mSess.mDownUpMgr.removeUploadTask(uploadTaskId);
				break;
			case TxDB.MSG_TYPE_BIG_FILE_SMS:
				// 删除单聊上传大文件消息
				getDownUpMgr().removeUploadBigTask(txmsg.msg_path);
				break;
			}

		} else {
			// 他人消息，任务id为path+time
			switch (txmsg.msg_type) {
			case TxDB.MSG_TYPE_IMAGE_EMS:
			case TxDB.MSG_TYPE_CARD_EMS:
			case TxDB.MSG_TYPE_TCARD_SMS:
				// 如果是他人发的含有图片消息，删除key为mPath+mTime+".0"的task
				mSess.mDownUpMgr.removeDownloadTask(mSess.mDownUpMgr
						.getDownLoadImageTaskId(txmsg.msg_url, false));
				break;
			case TxDB.MSG_TYPE_AUDIO_EMS:
			case TxDB.MSG_TYPE_GEO_SMS:
				// 如果是不含有图片消息，删除key为mPath+mTime的task
				mSess.mDownUpMgr.removeDownloadTask(mSess.mDownUpMgr
						.getDownloadTaskId(txmsg.msg_url));
				break;
			case TxDB.MSG_TYPE_BIG_FILE_SMS:
				// 删除单聊下载大文件消息
				getDownUpMgr().removeDownloadBigTask(
						getDownUpMgr().getDownloadTaskId(txmsg.msg_url), true);
				break;

			}
		}

		if (txMsgs.size() > 1) {
			// playManager.setRunning(false);
			mAudioRecPlayer.stopPlay();
			mSess.getSocketHelper().deleteSingleMessage(partner_id,
					txmsg.msg_id);
			mAudioRecPlayer.removeTalkCache(txmsg);
		} else {
			// 如果聊天室中只有一条消息，删除后直接退出聊天室？
			deletAllMsg();
			dialoginterface.dismiss();
			finish();
		}
		Toast.makeText(mSess.getContext(), R.string.del_success, 1).show();

	}

	/** 跳转查看用户信息 */
	private void jumpToUserInformation(long partner_id) {
		if (Utils.isIdValid(partner_id)) {
			// TX TemptX = TX.tm.getTx(partner_id);

			if (partner_id == TX.TUIXIN_MAN) {
				Intent intent = new Intent(SingleMsgRoom.this,
						UserInformationActivity.class);
				// intent.putExtra(UserInformationActivity.pblicInfo,
				// UserInformationActivity.TUIXIN_USER_INFO);
				// intent.putExtra(UserInformationActivity.UID,
				// TemptX == null ? partner_id : TemptX.partner_id);
				intent.putExtra(UserInformationActivity.UID, partner_id);
				startActivity(intent);
			} else {

				Intent iSupplement = new Intent(SingleMsgRoom.this,
						UserInformationActivity.class);
				// iSupplement
				// .putExtra(
				// UserInformationActivity.pblicInfo,
				// TX.tm.isTxFriend(partner_id) == true ?
				// UserInformationActivity.TUIXIN_USER_INFO
				// : UserInformationActivity.NOT_TUIXIN_USER_INFO);
				// iSupplement.putExtra(UserInformationActivity.UID,
				// TemptX == null ? partner_id : TemptX.partner_id);
				iSupplement.putExtra(UserInformationActivity.UID, partner_id);
				startActivityForResult(iSupplement, ACTION_SCAN_USERINFORMATION);
			}
			mUpdateId = partner_id;
		}

	}

	/** 单聊中对方头像的点击事件 */
	public void setHeadViewOnClickEvent(ImageView iv_headView,
			final TXMessage txmsg) {
		iv_headView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				jumpToUserInformation(txmsg.partner_id);

			}
		});
	};

	// 派生一个单聊adapter
	class SingleAdapter extends GroupContectListAdapter {

		@Override
		protected TextView updateView(ViewHolder holder) {
			super.updateView(holder);
			TXMessage txmsg = holder.txmsg;
			ImageView iv_headCrown = null;// 本期之星头像皇冠

			if (txmsg.was_me) {
				switch (txmsg.msg_type) {
				case TxDB.MSG_TYPE_COMMON_SMS:
				case TxDB.MSG_TYPE_AUDIO_EMS:
				case TxDB.MSG_TYPE_CARD_EMS:
				case TxDB.MSG_TYPE_TCARD_SMS:
				case TxDB.MSG_TYPE_GEO_SMS:
					sendTypeView.setVisibility(View.VISIBLE);
					switch (txmsg.read_state) {
					case TXMessage.UNREAD:
						// 单聊文字信息
						sendTypeView.setText(R.string.msg_go);
						break;
					case TXMessage.READ:
						sendTypeView.setText(R.string.msg_read);
						break;
					}
					break;
				}
			} else {
				// 对方发送的消息，处理上周之星头像
				iv_headCrown = (ImageView) rl_msgHeadView
						.findViewById(R.id.iv_msgroom_star_crown);
				rl_msgHeadView.setVisibility(View.VISIBLE);// 显示头像布局
				holder.headView.setBackgroundResource(0);// 重置头像背景
				holder.headView.setPadding(0, 0, 0, 0);// 重置头像padding
				iv_headCrown.setVisibility(View.GONE);// 隐藏皇冠

			}
			return dateView;
		}

	}

	@Override
	protected void sendGifMsg(int num, int pkgid, String emomd5,
			String pkg_emomd5) {
		final TXMessage txmsgTemp = TXMessage.creatGifSms(partner_id,
				tx.getNick_name(), tx.getPhone(), mSess.getServerTime(), num,
				pkgid, emomd5, pkg_emomd5);
		sendMsg(txmsgTemp);
		mSess.getSocketHelper().addSingleMessage(txmsgTemp);

	}

}
