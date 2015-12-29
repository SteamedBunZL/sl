package com.tuixin11sms.tx.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.shenliao.user.activity.UserInforRequestActivity;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/** 好友管家的activity，就是各种请求加我为好友的信息list */
public class FriendManagerActivity extends BaseActivity {
	private static final String TAG = FriendManagerActivity.class
			.getSimpleName();
//	private View mNoContact;
	private UpdateReceiver updatareceiver;
	private NewMsgReceiver newMsgReceiver;
	private ListView mRecommendInfoList;
	private ArrayList<TXMessage> dataList;
	private HashMap<Long, Boolean> friendsSet;
	private Recommened recommended;
	private Set<TXMessage> waitHandlerMessage;
	private HashMap<Long, Long> clickMap;
	private HashSet<Long> successSet;
	private int time_out = 5 * 1000;
	private boolean toHome;

	// private int defaultHeadImg;
	// private int defaultHeadImgMan;
	// private int defaultHeadImgFemal;
	public static final String RECEIVER_FOR_RECOMMENDINFO = "recommendActivity";

	public static final int HANDLER_SUCCESS = 1;
	public static final int HANDLER_FAIL = 2;
	public static final int TIMER_OUT = 3;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_AGREE_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_BLACK_DELETE_MESSAGE);
			this.registerReceiver(updatareceiver, filter);
		}

		if (newMsgReceiver == null) {
			newMsgReceiver = new NewMsgReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(RECEIVER_FOR_RECOMMENDINFO);
			this.registerReceiver(newMsgReceiver, filter);
		}
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_friend_manager);
		
		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// title上的返回按钮
				thisContext.finish();
				
			}
		});

		// defaultHeadImgMan=R.drawable.sl_regist_default_head;
		// defaultHeadImgFemal=R.drawable.sl_regist_head_femal;
		dataList = TXMessage.getFriendHelperList(mSess.getContentResolver());
		if (dataList != null) {
			for (TXMessage txmsg : dataList) {
				if (Utils.debug)
					Log.i(TAG, txmsg.toString());
			}
		}
		Intent intent = getIntent();
		if (intent != null) {
			toHome = intent.getBooleanExtra(NotificationPopupWindow.TO_HOME,
					false);
		}
		waitHandlerMessage = new HashSet<TXMessage>();
		friendsSet = new HashMap<Long, Boolean>();
		clickMap = new HashMap<Long, Long>();
		successSet = new HashSet<Long>();
		recommended = new Recommened();
//		mNoContact = findViewById(R.id.list_hint_empty);
		mRecommendInfoList = (ListView) findViewById(R.id.mRecommendInfoList);
		mRecommendInfoList.setAdapter(recommended);
		int index = 0;
		if (dataList != null) {
			index = dataList.size() - 1;
		}
		mRecommendInfoList.setSelection(index);
		mRecommendInfoList.setDividerHeight(0);
	}

	class Recommened extends BaseAdapter {

		@Override
		public int getCount() {
			if (dataList != null) {
				return dataList.size();
			} else {
				return 0;
			}

		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			TestHolder holder = null;
			if (convertView == null) {
				convertView = LayoutInflater.from(FriendManagerActivity.this)
						.inflate(R.layout.sll_item_friend_manager, null);
				holder = new TestHolder();
				holder.time = (TextView) convertView.findViewById(R.id.mTime);
//				holder.infoType = (ImageView) convertView
//						.findViewById(R.id.mInfoTypePic);
//				holder.infoTypeText = (TextView) convertView
//						.findViewById(R.id.mInfoTypeText);

				holder.tuixinNumber = (TextView) convertView
						.findViewById(R.id.mNumber);
				holder.headPic = (ImageView) convertView
						.findViewById(R.id.mHeadPic);
				holder.nickname = (TextView) convertView
						.findViewById(R.id.mNickname);
				holder.content = (TextView) convertView
						.findViewById(R.id.mContent);
//				holder.handleType = (ImageView) convertView
//						.findViewById(R.id.handle_image);
				// holder.second = (Button)
				// convertView.findViewById(R.id.mSecond);
				convertView.setTag(holder);
			} else {
				holder = (TestHolder) convertView.getTag();
			}

			final TXMessage txMessage = dataList.get(position);
			if (txMessage.read_state == 2) {
				waitHandlerMessage.add(txMessage);
				NotificationPopupWindow.showNotification(txMessage.msg_id,
						false);
			}

			long time = txMessage.send_time;
			Date date = Utils.formatDateTime(time);

			
			//如果下面的条目和上面的条目同一日期，则不显示日期条目
			if(position>0){
				TXMessage preTxmsg = dataList.get(position-1);
				if(preTxmsg!=null){
					Date predate = Utils.formatDateTime(preTxmsg.send_time);
					Calendar preCal = Calendar.getInstance();
					Calendar curCal = Calendar.getInstance();
					preCal.setTime(predate);
					curCal.setTime(date);
					if(curCal.get(Calendar.YEAR)==preCal.get(Calendar.YEAR)){
						//同一年份
						int compResult = curCal.get(Calendar.DAY_OF_YEAR)-preCal.get(Calendar.DAY_OF_YEAR);
						if(compResult==0){
							convertView.findViewById(R.id.v_divide_line).setVisibility(View.GONE);
							holder.time.setVisibility(View.GONE);
						}else {
							holder.time.setVisibility(View.VISIBLE);
							convertView.findViewById(R.id.v_divide_line).setVisibility(View.VISIBLE);
							holder.time.setText(new SimpleDateFormat("MM月dd日 HH:mm")
							.format(date));
						}
					}else {
						//不同年份
						holder.time.setVisibility(View.VISIBLE);
						convertView.findViewById(R.id.v_divide_line).setVisibility(View.VISIBLE);
						holder.time.setText(new SimpleDateFormat("MM月dd日 HH:mm")
						.format(date));
					}
				}else {
					//这种情况该怎么处理？ 2014.03.06
					holder.time.setVisibility(View.VISIBLE);
					convertView.findViewById(R.id.v_divide_line).setVisibility(View.VISIBLE);
					holder.time.setText(new SimpleDateFormat("MM月dd日 HH:mm")
					.format(date));
				}
			}else {
				holder.time.setVisibility(View.VISIBLE);
				convertView.findViewById(R.id.v_divide_line).setVisibility(View.VISIBLE);
				holder.time.setText(new SimpleDateFormat("MM月dd日 HH:mm")
				.format(date));
			}
			
			
			boolean isFriend = TX.tm.isTxFriend(txMessage.tcard_id);
			if (txMessage.tcard_id != 0) {
				friendsSet.put(txMessage.tcard_id, isFriend);
			}

			switch (txMessage.msg_type) {
			case TxDB.MSG_TYPE_GREET_SMS:
				//应该没有打招呼信息了 2014.03.06 shc
//				holder.infoType.setImageResource(R.drawable.loaction);
//				holder.infoTypeText.setText(txMessage.tcard_name
//						+ FriendManagerActivity.this.getResources().getString(
//								R.string.recommended_request));
//				holder.nickname.setText(txMessage.tcard_name);
//				holder.content.setText(txMessage.msg_body);
//				if (friendsSet.get(txMessage.tcard_id)) {
//					holder.tuixinNumber.setText("已通过");
//					holder.tuixinNumber.setTextColor(FriendManagerActivity.this
//							.getResources().getColor(
//									R.color.request_tilte_color_blue));
//					holder.handleType
//							.setImageDrawable(FriendManagerActivity.this
//									.getResources().getDrawable(
//											R.drawable.sl_request_ok));
//				} else {
//					holder.tuixinNumber.setTextColor(FriendManagerActivity.this
//							.getResources().getColor(
//									R.color.request_tilte_color_red));
//					holder.handleType
//							.setImageDrawable(FriendManagerActivity.this
//									.getResources().getDrawable(
//											R.drawable.sl_request_no));
//					holder.tuixinNumber.setText("未处理");
//				}
				break;
			case TxDB.MSG_TYPE_SNS_SMS:
				//应该没有匹配sns信息了 2014.03.06 shc
//				holder.infoType
//						.setImageResource(R.drawable.sl_request_sina_icon);
//				holder.infoTypeText.setText(R.string.recommended_sina_friend);
//				holder.nickname.setText(txMessage.tcard_name);
//				holder.content.setText(FriendManagerActivity.this
//						.getResources().getString(
//								R.string.recommended_sina_friend2)
//						+ txMessage.sns_name);
//				if (friendsSet.get(txMessage.tcard_id)) {
//					holder.handleType
//							.setImageDrawable(FriendManagerActivity.this
//									.getResources().getDrawable(
//											R.drawable.sl_request_ok));
//					holder.tuixinNumber.setText("已通过");
//					holder.tuixinNumber.setTextColor(FriendManagerActivity.this
//							.getResources().getColor(
//									R.color.request_tilte_color_blue));
//				} else {
//					holder.handleType
//							.setImageDrawable(FriendManagerActivity.this
//									.getResources().getDrawable(
//											R.drawable.sl_request_no));
//					holder.tuixinNumber.setTextColor(FriendManagerActivity.this
//							.getResources().getColor(
//									R.color.request_tilte_color_red));
//					holder.tuixinNumber.setText("未处理");
//				}
				break;
			case TxDB.MSG_TYPE_ADD_FRIEND_SMS:
				holder.nickname.setText(txMessage.tcard_name);
				TX txx = TX.tm.getTx(txMessage.tcard_id);
				if(txx!=null){
					//显示个性签名
					holder.content.setText(txx.sign);
				}else {
					//TODO 应该显示请求信息吧？ 2014.03.06 shc
					holder.content.setText(txMessage.msg_body);
				}
//				if (friendsSet.get(txMessage.tcard_id)) {
//					holder.content.setText(R.string.recommended_let_talk);
//					holder.tuixinNumber.setText("已处理");
//					holder.tuixinNumber.setTextColor(getResources().getColor(R.color.item_sign_text));
//					holder.tuixinNumber.setBackgroundColor(Color.TRANSPARENT);
//				} else {
//					holder.content.setText(txMessage.msg_body);
//					holder.tuixinNumber.setTextColor(Color.WHITE);
//					holder.tuixinNumber.setText("接受");
//					holder.tuixinNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_btn_blue_selector));
//				}
				break;
			case TxDB.MSG_TYPE_CONTACTS_SMS:
				//跟秀确认了一下，通讯录匹配服务器还在做 2014.03.06 shc
				holder.nickname.setText(txMessage.tcard_name);
				TX contact = TX.tm.getTx(txMessage.tcard_id);

				if (!TextUtils.isEmpty(contact.getTxInfor().getContacts_person_name())) {
					holder.content.setText(getResources().getString(
									R.string.recommended_phone_contact)
							+ contact.getTxInfor().getContacts_person_name());
				} else {
					holder.content.setText(R.string.recommended_let_talk);
				}
				break;
			}
			if (friendsSet.get(txMessage.tcard_id)) {
				holder.tuixinNumber.setText("已处理");
				holder.tuixinNumber.setTextColor(getResources().getColor(R.color.item_sign_text));
				holder.tuixinNumber.setBackgroundColor(Color.TRANSPARENT);
			} else {
				holder.tuixinNumber.setTextColor(Color.WHITE);
				holder.tuixinNumber.setText("接受");
				holder.tuixinNumber.setBackgroundDrawable(getResources().getDrawable(R.drawable.action_btn_blue_selector));
				holder.tuixinNumber.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						switch(txMessage.msg_type){
						//打招呼//新浪微博 
						case TxDB.MSG_TYPE_GREET_SMS:
						case TxDB.MSG_TYPE_SNS_SMS:
							mSess.getSocketHelper().sendAddPartener(txMessage.tcard_id,txMessage.ac,"");
							break;
							//有人想加你为好友
						case TxDB.MSG_TYPE_ADD_FRIEND_SMS:
							//41号协议==加为好友
							mSess.getSocketHelper().sendAgreeMsg(true,txMessage.tcard_id,true,txMessage.ac);
							break;
						}
						showDialogTimer(thisContext, 0, "操作中...", time_out, new BaseTimerTask(){
							public void run() {
								super.run();
								sendMsg(TIMER_OUT);
							}
						}).show();
						
					}
				});
			}
			
			if (txMessage.tcard_sex == TX.MALE_SEX) {
				defaultHeaderImg = defaultHeaderImgMan;
			} else {
				defaultHeaderImg = defaultHeaderImgFemale;
			}
			holder.headPic.setImageResource(defaultHeaderImg);
			long uid = txMessage.tcard_id;
			if (Utils.isIdValid(uid)) {

				holder.headPic.setTag(uid);
				holder.headPic.setImageResource(defaultHeaderImg);
				String url = null;
				TX tx = TX.tm.getTx(uid);
				if (tx != null)
					url = tx.avatar_url;
				Bitmap bm =  mSess.avatarDownload.getAvatar(url, uid, holder.headPic,
						avatarHandler);
				if (bm != null) {
					holder.headPic.setImageBitmap(bm);
				}

			}
			convertView.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(FriendManagerActivity.this,
							UserInforRequestActivity.class);
					intent.putExtra("txMessage", txMessage);
					intent.putExtra("isfriend",
							friendsSet.get(txMessage.tcard_id));
					startActivity(intent);
				}
			});

			convertView.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					new AlertDialog.Builder(FriendManagerActivity.this)
							.setTitle("")
							.setItems(R.array.recommend_select_know,
									new OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												new AlertDialog.Builder(
														FriendManagerActivity.this)
														.setTitle(
																R.string.prompt)
														.setMessage(
																R.string.delete_confirm_message)
														.setNeutralButton(
																R.string.confirm,
																new OnClickListener() {
																	@Override
																	public void onClick(
																			DialogInterface dialog,
																			int which) {
																		if (TXMessage
																				.deleteByMsgId(
																						FriendManagerActivity.this
																								.getContentResolver(),
																						txMessage.msg_id) != 0) {
																			dataList.remove(txMessage);
																			if (dataList
																					.isEmpty()) {
																				MsgStat.delMsgStat(
																						mSess.getContentResolver(),
																						TX.TUIXIN_FRIEND);
																			}
																			sendMsg(0);
																		}
																	}
																})
														.setNegativeButton(
																R.string.cancel,
																new OnClickListener() {

																	@Override
																	public void onClick(
																			DialogInterface dialog,
																			int which) {
																		dialog.cancel();

																	}
																}).show();

												break;
											}
										}
									}).show();
					return false;
				}
			});

			return convertView;
		}

	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				ImageView iv = (ImageView) mRecommendInfoList.findViewWithTag((Long)result[1]);
				if(iv != null){
					long tag = (Long) iv.getTag();
					long id = (Long)result[1];
					if (result != null&&tag == id) {
						iv.setImageBitmap((Bitmap)result[0]);
						recommended.notifyDataSetChanged();// 刷新adapter,为了相同的个人头像都能显示
					} 
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	
	@Override
	protected void onDestroy() {
		// stopAsyncload();
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
		if (newMsgReceiver != null) {
			this.unregisterReceiver(newMsgReceiver);
			newMsgReceiver = null;
		}
		handlerUnread();
		TxData.popActivityRemove(this);
		super.onDestroy();
	}
	
	//挪到Utils中了 2014.03.11 shc
//	/**格式化消息中的发送时间*/
//	private Date formatDateTime(long time) {
//		Date date;
//		if (("" + time).length() >= 13 && ("" + time).length() < 16) {
//			date = new Date(time);
//		} else if (("" + time).length() >= 16) {
//			date = new Date(time / 1000);
//		} else {
//			date = new Date(time * 1000);
//		}
//
//		return date;
//	}
	

	class TestHolder {
		TextView time;
//		ImageView infoType;
//		TextView infoTypeText;
		TextView tuixinNumber;
		ImageView headPic;
//		ImageView handleType;
		TextView nickname;
		TextView content;
		// Button first;
		Button second;

	}

	// @Override
	// public boolean onKeyDown(int keyCode, KeyEvent event) {
	// if (keyCode == KeyEvent.KEYCODE_BACK) {
	// if (toHome) {
	// startActivity(new Intent(this, TuiXinMainTab.class));
	// }
	// if (dataList.size() > 0) {
	// TXMessage txmsg = dataList.get(dataList.size() - 1);
	// BLog.d(TAG, "++++++++++++++" + txmsg.toString());
	// MsgStat msgStat = MsgStat.tmToms(txmsg, txdata, TxDB.MS_TYPE_TB);
	// BLog.d(TAG, "addMsgstats msgroom 1");
	// txdata.addOrUpdateMsgStat(msgStat, true, false);
	// // ReturnLogic();
	// this.finish();
	// return true;
	// }
	// }
	// //
	// return super.onKeyDown(keyCode, event);
	// }
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (toHome) {
				startActivity(new Intent(this, TuiXinMainTab.class));
			}
			if (dataList.size() > 0) {
				TXMessage txmsg = dataList.get(dataList.size() - 1);
				if (Utils.debug)
					Log.d(TAG, "++++++++++++++" + txmsg.toString());
				MsgStat.updateMsgStatByTxmsg(txmsg,mSess.getContentResolver(), TxDB.MS_TYPE_TB,
						txmsg.gmid, 0, true);
				SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
				if (Utils.debug)
					Log.d(TAG, "addMsgstats msgroom 1");

				// 整合到updateMsgStatByTxmsg方法内，暂注掉 2013.10.19 shc
				// if(msgStat!=null){
				// MsgStat.addOrUpdateMsgStat(txdata,msgStat, true);
				// }
				// ReturnLogic();
				this.finish();
				return true;
			}
		}

		return super.dispatchKeyEvent(event);

	}

	public void onResume() {

		handlerUnread();
		sendMsg(HANDLER_SUCCESS);
		recommended.notifyDataSetChanged();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void handlerUnread() {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				for (TXMessage txMsg : waitHandlerMessage) {
					// 本地已读
					TXMessage.updateByMsgId(mSess.getContentResolver(), txMsg.msg_id, 3);
					// 回执 已读
					// SocketHelper.getSocketHelper(txdata).sendReceipt(txMsg.tcard_id,""+txMsg.msg_id,
					// 3);
				}

			}
		});

	}

	public void onStop() {
		// if (updatareceiver != null) {
		// this.unregisterReceiver(updatareceiver);
		// updatareceiver = null;
		// }
		// if (newMsgReceiver != null) {
		// this.unregisterReceiver(newMsgReceiver);
		// newMsgReceiver = null;
		// }
		handlerUnread();
		super.onStop();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			cancelDialogTimer();
			int num = msg.what;
			recommended.notifyDataSetChanged();
			switch (num) {
			// 好友添加成功
			case HANDLER_SUCCESS:
				break;
			case HANDLER_FAIL:
				break;
			case TIMER_OUT:
				// txdata.showToast(R.string.foreign_check_code_outtime);
				showToast(R.string.foreign_check_code_outtime);
				break;
			}
			if (dataList.size() == 0) {
//				mNoContact.setVisibility(View.VISIBLE);
				mRecommendInfoList.setVisibility(View.GONE);
			} else {
				if (mRecommendInfoList.getVisibility() == View.GONE) {
//					mNoContact.setVisibility(View.GONE);
					mRecommendInfoList.setVisibility(View.VISIBLE);
				}

			}
		}
	};

	// 此处处理好友添加结果
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent.getAction())) {
				dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_ADD_BUDDY.equals(intent
					.getAction())) {
				dealAddBuddy(serverRsp);
			} else if (Constants.INTENT_ACTION_AGREE_ADD_BUDDY.equals(intent
					.getAction())) {
				dealAgreeAddBuddy(serverRsp);
			} else if (Constants.INTENT_ACTION_BLACK_DELETE_MESSAGE
					.equals(intent.getAction())) {
				dealDeleteMsg(intent);

			}
		}
	}

	public void dealDeleteMsg(Intent intent) {
		if (intent != null) {

			TXMessage txMessage = intent.getParcelableExtra("message");
			if (Utils.debug)
				Log.i("op", txMessage.toString());
			if (txMessage != null) {
				dataList.remove(txMessage);
				MsgStat.delMsgStatByPartnerId(mSess.getContentResolver(), txMessage.partner_id);
				recommended.notifyDataSetChanged();
			}

		}

	}

	private void dealSystemMsg(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_VERIFY_FRIEND: {
				if (serverRsp.getBoolean("agree", false)) {
					sendMsg(HANDLER_SUCCESS);
				}
				break;
			}
			}
		}
	}

	private void dealAddBuddy(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				long uid = serverRsp.getInt("uid", -1);
				// 防止重复弹窗
				if (clickMap.containsKey(uid)) {
					if (!successSet.contains(uid)) {
						successSet.add(uid);
						boolean bf = serverRsp.getBoolean("bf", false);
						if (bf) {
							// txdata.showToast(R.string.add_friend_success);
							showToast(R.string.add_friend_success);
						} else {
							// txdata.showToast(R.string.recommended_send_success);
							showToast(R.string.recommended_send_success);
						}

					}
				}
				sendMsg(HANDLER_SUCCESS);
				break;
			}
			}
		}
	}

	private void dealAgreeAddBuddy(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				long uid = serverRsp.getInt("uid", -1);
				// 防止重复弹窗
				if (clickMap.containsKey(uid)) {
					if (!successSet.contains(uid)) {
						successSet.add(uid);
						// txdata.showToast(R.string.recommend_request_success);
						showToast(R.string.recommend_request_success);
					}
				}
				sendMsg(HANDLER_SUCCESS);
				break;
			}
			case OPT_FAILED: {
				sendMsg(HANDLER_FAIL);
				break;
			}
			}
		}
	}

	public class NewMsgReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			TXMessage msg = intent.getParcelableExtra("newMsg");
			boolean isNew = intent.getBooleanExtra("isNew", false);
			if (msg != null && isNew) {
				dataList.add(msg);
			}
			sendMsg(HANDLER_SUCCESS);
		}
	}

	private void sendMsg(int what) {
		Message msg1 = new Message();
		msg1.what = what;
		handler.sendMessage(msg1);
	}

}
