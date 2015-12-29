package com.tuixin11sms.tx.activity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.LinearLayout.LayoutParams;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupInfo;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.Contact;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.PraiseMsgCallBack;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.ReceiveGreatAnim;
import com.umeng.analytics.MobclickAgent;

public class GroupMsgRoom extends BaseMsgRoom implements OnTouchListener {
	private static final String TAG = GroupMsgRoom.class.getSimpleName();
	public static final String NEWCREAT_GROUP_KEY = "newCreat_Group_Key";
	public static final String TX_GROUP_ID = "txgroupid";
	public static final int RECEIVE_PRAISED_NOTICE = 0x10;// 收到消息被赞通知
	private LocationReceiver locationReceiver;
	/******* 数据部分变量 *****/
	private Long lastClickTime = System.currentTimeMillis();// 上次发送消息的时间？
	public boolean isComMsglist;
	private boolean waitDouble = true;
	private int mLineHeight = 0;
	private int mSleepTickCount = 0;
	private TextView noticeText;
	private Timer timer;
	private TimerTask timerTask;
	private RelativeLayout uplayout;
	private int praisedNoticeCount = 0;
	protected PopupWindow praisedUsersPopWindow;// 点击聊天室右上角【被赞总数】弹出的popupWindow

	public static List<ImageView> iv_heads = new ArrayList<ImageView>();

	public static final String GROUP_BAN_LIST = "groupBanList";// 禁言列表

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		processExtraData(true);

		if (Utils.debug) {
			// 测试点赞数据去掉
			// long ids[] = {3049419, 3049122, 3049422, 3049144, 3049418,
			// 3049417, 3049416, 3049423};
			// for (int i = 0; i < ids.length; i++) {
			// idList.add(ids[i]);
			// }
		}

		tv_cur_room_praised_count
				.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 显示所有点赞人的id
						createPraisedUsersPop();

					}
				});
	}

	// 更新最新消息界面数据
	public void updataNewMessage() {
		if (txMsgs != null && txMsgs.size() > 0) {
			int index = txMsgs.size() - 1;
			while (txMsgs.get(index).msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
					|| txMsgs.get(index).msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS) {
				index--;
				if (index < 0) {
					index = 0;
					break;
				}
			}

			TXMessage txmsg = txMsgs.get(index);
			MsgStat msgStat = MsgStat.updateMsgStatByTxmsg(txmsg,
					mSess.getContentResolver(), TxDB.MS_TYPE_QU, txmsg.gmid, 0,
					true);
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
			msgStat.no_read = 0;
		}
	}

	private static Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				if (iv_heads != null && iv_heads.size() > 0) {
					for (ImageView iv : iv_heads) {
						long iv_id = (Long) iv.getTag();
						long id = (Long) result[1];
						if (result[0] != null && iv_id == id) {
							iv.setImageBitmap((Bitmap) result[0]);
						}
					}
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	/**
	 * 聊天数据变化的handler消息
	 */
	Handler msgHandler = new WrappedHandler(thisContext) {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SocketHelper.GROUP_SHUT_UP:
				// 显示你已被禁言toast
				Toast.makeText(mSess.getContext(), R.string.u_shutup,
						Toast.LENGTH_SHORT).show();
				break;
			case SocketHelper.GROUP_NOTICE:
				if (null != (String) msg.obj && !"".equals((String) msg.obj)) {
					createNoticePop((String) msg.obj);
				}
				break;
			case SocketHelper.CHAT_MSG_LIST_CHANGED:
				dealChatMsgChanged(msg);

				break;
			case SocketHelper.UPDATE_GROUP_INFORMATION:
				txGroup = (TxGroup) msg.obj;
				if (txGroup != null && personName != null) {// 更新群名称
					setNameStr();
					missDownTool();
					Utils.roomid = txGroup.group_id;
					flush(FLUSH_ROOM_FALSE);
				}
				break;
			case RECEIVE_PRAISED_NOTICE:
				// 收到消息被赞通知，播放被赞动画
				Boolean isFirstPraised = (Boolean) msg.obj;
				tv_cur_room_praised_count.setText("" + praisedNoticeCount);
				if (isFirstPraised) {
					// 该消息第一次被赞，播放动画
					anim.start();
				}
				break;
			}
		}
	};

	protected void onStart() {
		isPhone = false;
		if (locationReceiver == null) {
			locationReceiver = new LocationReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_OK);
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_FAILED);
			filter.addAction(Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED);
			this.registerReceiver(locationReceiver, filter);
		}

		if (msgMaxReceiver == null) {
			msgMaxReceiver = new MsgMaxReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_INOUT_GROUP_2030);
			filter.addAction(Constants.INTENT_ACTION_CLEAR_MSGS_FINISH);
			filter.addAction(Constants.INTENT_ACTION_DELETE_GROUP_MSG);
			filter.addAction(Constants.INTENT_ACTION_SHUTUP_GROUP_2028);
			filter.addAction(Constants.INTENT_ACTION_GROUP_BAN_LIST);
			// filter.addAction(Constants.INTENT_ACTION_INOUT_GROUP_NOTICE_2041);
			this.registerReceiver(msgMaxReceiver, filter);
		}

		// if (TxGroup.isPublicGroup(txGroup)) {
		// Utils.isNotificationShow = false;
		// } else {
		Utils.isNotificationShow = true;
		// }

		// NotificationPopupWindow.cancelNotification(GroupMsgRoom.this);

		super.onStart();
	}

	private List<Long> idList = new ArrayList<Long>();
	// static long ids[] = {3049419, 3049122, 3049422, 3049144, 3049418,
	// 3049417, 3049416, 3049423};
	private GridView gv_praisedUsers = null;

	/** 创建和显示所有对我点赞人的头像 */
	public void createPraisedUsersPop() {
		if (idList == null || idList.size() < 1) {
			// 点赞人集合为空
			return;
		}
		if (praisedUsersPopWindow != null && praisedUsersPopWindow.isShowing())
			praisedUsersPopWindow.dismiss();

		if (praisedUsersPopWindow == null) {
			View popupWindow_view = mInflate.inflate(
					R.layout.msgroom_praised_users_pop, null);
			// ImageView popupWindow_view = new ImageView(thisContext);
			// popupWindow_view.setBackgroundColor(Color.BLUE);
			// popupWindow_view.setImageResource(R.drawable.qun_default);

			praisedUsersPopWindow = new PopupWindow(popupWindow_view,
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例

			praisedUsersPopWindow.setAnimationStyle(R.style.chat_up_anim);
			praisedUsersPopWindow.setFocusable(true);
			praisedUsersPopWindow.setBackgroundDrawable(new BitmapDrawable());

			// long ids[] = {3049419, 3049122, 3049422, 3049144, 3049418,
			// 3049417, 3049416, 3049423};
			// long ids[] = {3049419, 3049122, 3049422};
			gv_praisedUsers = (GridView) popupWindow_view
					.findViewById(R.id.gv_praisedUsers);
			setGridViewNumColum(idList, gv_praisedUsers);

			if (praisedUsersAdapter == null) {
				praisedUsersAdapter = new PraisedUsersAdapter(thisContext,
						idList, mSess);
			}
			if (gv_praisedUsers.getAdapter() != praisedUsersAdapter) {
				gv_praisedUsers.setAdapter(praisedUsersAdapter);
			}

			praisedUsersPopWindow.setTouchInterceptor(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (Utils.debug)
						// showToast("这个upMorePopWindow的setTouchInterceptor事件是干嘛的？");//先不弹toast提示
						// 2014.03.07 shc
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							Rect t = new Rect();
							sendTypeButton.getGlobalVisibleRect(t);
							if (t.contains((int) event.getRawX(),
									(int) event.getRawY())) {
								changeType(true, false);
								Utils.saveAutoPlayAdiouData(thisContext);
							}
							downMoreButton.getGlobalVisibleRect(t);
							if (t.contains((int) event.getRawX(),
									(int) event.getRawY())) {

								creatDownMorePop();
							}
						}
					return false;
				}
			});
			praisedUsersPopWindow.update();
			praisedUsersPopWindow.showAsDropDown(tv_cur_room_praised_count, 0,
					0);
		} else {
			if (praisedUsersPopWindow.isShowing()) {
				praisedUsersPopWindow.dismiss();
			} else {
				if (gv_praisedUsers != null) {
					setGridViewNumColum(idList, gv_praisedUsers);
					praisedUsersAdapter.notifyDataSetChanged();
				}
				praisedUsersPopWindow.update();
				praisedUsersPopWindow.showAsDropDown(tv_cur_room_praised_count,
						0, 0);

			}
		}
	}

	private void setGridViewNumColum(List<Long> ids, GridView gv_praisedUsers) {
		LinearLayout.LayoutParams params = null;
		if (Utils.debug)
			Log.i(TAG, "ids长度：" + ids.size());
		if (ids.size() == 1) {
			gv_praisedUsers.setNumColumns(1);
			params = new LinearLayout.LayoutParams(Utils.dip2px(50,
					mSess.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
		} else if (ids.size() == 2) {
			gv_praisedUsers.setNumColumns(2);
			params = new LinearLayout.LayoutParams(Utils.dip2px((50 * 2 + 5),
					mSess.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
		} else if (ids.size() == 3) {
			gv_praisedUsers.setNumColumns(3);
			params = new LinearLayout.LayoutParams(Utils.dip2px((50 * 3 + 10),
					mSess.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
		} else if (ids.size() >= 4) {
			gv_praisedUsers.setNumColumns(4);
			params = new LinearLayout.LayoutParams(Utils.dip2px((50 * 4 + 15),
					mSess.getContext()), LinearLayout.LayoutParams.WRAP_CONTENT);
		}
		if (params != null) {
			gv_praisedUsers.setLayoutParams(params);
		}
	}

	@Override
	protected void onRestart() {
		isPhone = false;

		super.onRestart();
	}

	private boolean isSetPraiseLocation = false;// 是否设置了动画的布局坐标

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {

		locations = new int[2];
		iv_aminend.getLocationInWindow(locations);
		location2s = new int[2];
		iv.getLocationInWindow(location2s);

		isSetPraiseLocation = true;

		List<ImageView> ivs = new ArrayList<ImageView>();
		ivs.add(iv);
		ivs.add(iv1);
		ivs.add(iv2);
		ivs.add(iv3);
		ivs.add(iv4);
		ivs.add(iv5);
		ivs.add(iv6);
		ivs.add(iv7);
		ivs.add(iv8);
		ivs.add(ivbg);
		ivs.add(ivbg2);
		ivs.add(iv_aminend);

		for (ImageView iv : ivs) {
			iv.setVisibility(View.GONE);
		}

		anim = new ReceiveGreatAnim(ivs, locations, location2s);
		super.onWindowFocusChanged(hasFocus);
	}

	@Override
	protected void processExtraData(boolean isInOnCreat) {
		// 被onCreat和onNewIntent调用

		if (!isInOnCreat) {
			// 如果是在onCreat方法中调用此processExtraData方法，则不加载initMsgRoomData方法，因为super方法已经调用了这个方法
			initMsgRoomData();
		}

		if (Utils.debug)
			Log.i(TAG, "设置数据 notifhDataSetChanged消息adapter");
		contectsListAdapter = new GroupAdapter();
		if (txMsgs != null) {
			contectsListAdapter.setData(txMsgs);
		}
		msgRoomList.setAdapter(contectsListAdapter);

		uplayout = (RelativeLayout) findViewById(R.id.rl_msgroom_title_bar);

		if (praiseResultCallback == null) {
			praiseResultCallback = new PraiseMsgCallBack() {

				@Override
				public void onSuccess(long gmid, int praiseFlag) {
					// 更新界面
					for (TXMessage tmsg : txMsgs) {
						if (tmsg.gmid == gmid
								&& (tmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS || tmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS)) {
							// 找到了对应的gmid消息,并且该消息是群图片消息或群语音消息
							tmsg.praisedState = praiseFlag;// 服务器返回的赞状态
							showToast(tmsg.partner_id + "的赞状态" + praiseFlag
									+ "设置成功");
							flush(FLUSH_ONE_LINE, tmsg);
							break;
						}
					}
					// txmsg.praisedState = praiseFlag;//服务器返回的赞状态
					// flush(FLUSH_ONE_LINE, txmsg);
				}

				@Override
				public void onFailed(long gmid, int praiseFlag) {
					String opearName = "赞";
					if (praiseFlag == 0) {
						// 点赞标记
						opearName = "赞";
					} else if (praiseFlag == 1) {
						// 取消赞标记
						opearName = "取消赞";
					}
					showToast(opearName + "操作失败");
				}
			};
			mSess.getPraiseNoticeDao().registePraiseResultCallback(
					praiseResultCallback);
		}
		getIntentData();

		setNameStr();

	}

	@Override
	public void setNameStr() {
		if (txGroup != null) {
			personName.setText(smileyParser.addSmileySpans(txGroup.group_title,
					true, 0));
		}
	}

	protected void onStop() {

		if (locationReceiver != null) {
			this.unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		mSess.getSocketHelper().sendNoReadMsg();
		super.onStop();
	}

	protected void onPause() {
		Utils.xf_group_id = -1;
		super.onPause();
		MobclickAgent.onPause(this);
	}

	protected void onResume() {
		isPhone = false;
		if (Utils.isIdValid(mUpdateId)) {
			TX tx = TX.tm.getTx(mUpdateId);

			if (tx != null && contectsListAdapter != null) {
				if (Utils.debug)
					Log.i(TAG, "onResume  notifhDataSetChanged消息adapter");
				contectsListAdapter.updateListInfo(tx);
				contectsListAdapter.notifyDataSetChanged();
			}
			mUpdateId = 0;
		}
		if (txGroup != null) {
			Utils.xf_group_id = txGroup.group_id;
		}
		Utils.roomstate = Utils.IN_GROUP_ROOM;

		btn_recordShortAduio.setText(R.string.publicmsg_record);
		// Utils.inPhoto = false;

		// if (TxGroup.isPublicGroup(txGroup)) {
		// Utils.isNotificationShow = false;
		// } else {
		turn = false;
		Utils.isNotificationShow = true;
		// }

		super.onResume();
		MobclickAgent.onResume(this);

		// t = new Timer();
		// t.schedule(new TimerTask() {
		//
		// @Override
		// public void run() {
		//
		// }
		// }, 3000);
	}

	public void onDestroy() {

		// if (noticePopWindow != null) {
		// noticePopWindow.dismiss();
		// }
		if (noticeMorePopWindow != null) {
			noticeMorePopWindow.dismiss();

		}
		if (timer != null) {
			timer.cancel();
		}
		if (sensorManager != null) {
			sensorManager.unregisterListener(sensorEventListener);
		}

		if (msgMaxReceiver != null) {
			unregisterReceiver(msgMaxReceiver);
			msgMaxReceiver = null;
		}
		musicUtils.release();

		if (iv_heads != null)
			iv_heads.clear();

		Utils.roomstate = Utils.IN_OTHER_ROOM;
		Utils.roomid = -1;
		ls = null;
		if (locationReceiver != null) {
			unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		turn = true;
		Utils.isNotificationShow = true;

		if (Utils.debug) {
			Log.i(TAG, "Utils.isNotificationShow ondestroy is"
					+ Utils.isNotificationShow);
		}

		mSess.getSocketHelper().sendInOutGroup(groupid, 1);

		mSess.getPraiseNoticeDao().unregistReceiveNoticeListener();// 反注册接收被赞通知的监听

		// 判断这个聊天室还是不是我的聊天室 2014.01.23
		if (TxGroup.isMyGroup(txGroup.group_id)) {
			updataNewMessage();
		}

		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == REQ_TAKE_PHOTO) { // 照相
			// TuixinService1.notNeedCheckActivityState = false;
			switch (resultCode) {
			case RESULT_OK:
				Bitmap tempimg = Utils.fitSizeAutoImg(sendimgPath,
						Utils.msgroom_list_resolution);
				if (tempimg == null && data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						tempimg = (Bitmap) extras.get("data");
						if (Utils.createPhotoFile(tempimg, img_msg_id + ".jpg") == null) {
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
				break;
			default:
				sendimgPath = null;// 防止出现这个值在send()消息时被复用（用户点击聊天室中选择图片，没有发送点击返回，再次发送文字信息出错。只显示空的图片消息气泡）
			}
		} else if (requestCode == REQ_TAKE_PICTURE) {// 选择图片
			switch (resultCode) {
			case RESULT_OK:
				String imagePath = null;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor != null && cursor.getCount() > 0) {
					cursor.moveToFirst();
					imagePath = cursor.getString(1);
					cursor.close();

					try {
						Intent i = new Intent(this, EditSendImg.class);
						i.putExtra(EditSendImg.LOCAL, imagePath);
						startActivityForResult(i, REQ_EDIT_PICTURE);
					} catch (Exception e) {
						if (Utils.debug)
							Log.e(TAG, e.getMessage(), e);
					}
				} else {

					Utils.startPromptDialog(GroupMsgRoom.this, "提示",
							"选择图片失败，请尝试从相册中更换图片，如再有问题请联系神聊工作人员");
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
		} else if (SelectFriendListActivity.CHAT_TYPE_CARD == requestCode
				&& resultCode == SelectFriendListActivity.CHAT_TYPE_CARD) {// 名片返回
			TXMessage txMessageCard = null;
			// 组消息对象
			TX txCard = data
					.getParcelableExtra(SelectFriendListActivity.CHAT_TYPE_CARD_OBJ);
			if (txCard != null) {
				if (Utils.isIdValid(txCard.partner_id)) {
					txMessageCard = TXMessage.creatGroupTCardEms(
							GroupMsgRoom.this, txCard.getNick_name(),
							txGroup.group_id, txGroup.group_title, "",
							txCard.getNick_name(), txCard.partner_id,
							txCard.sign, txCard.getSex(), txCard.getPhone(),
							txCard.avatar_url, TXMessage.NOT_SENT,
							// System.currentTimeMillis() / 1000 +
							// getPrefsMeme().getLong(CommonData.ST, 0));
							mSess.getServerTime());
					txMessageCard.updateState = TXMessage.UPDATE;
					mSess.getSocketHelper().sendGroupMsg(txMessageCard);
				}
			}
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
		} else if (requestCode == DOWNLOAD_VIEW_BIG_PIC) {
			// 下载并查看大图的返回则更新txmsg,如果只是查看图片，则不更新这个消息
			if (resultCode != Activity.RESULT_OK || data == null) {
				return;
			}
			TXMessage ttsmg = data.getParcelableExtra(EditSendImg.TXMESSAGE);
			if (ttsmg == null) {
				// 没有消息传递过来
				return;
			}
			for (TXMessage tsg : txMsgs) {
				if (tsg.gmid == ttsmg.gmid && tsg.msg_id == ttsmg.msg_id) {
					// 消息的gmid和msgid都相等
					tsg.fileDownTime = ttsmg.fileDownTime;// 把大图下载完成时间赋给该值；
					if (Utils.debug) {
						showToast("设置了消息【" + tsg.gmid + "】的fileDownTime");
					}

					break;
				}
			}
		}
	}

	@Override
	protected void uploadAndAddBigFileMsg(String filePath) {
		final TXMessage txmsgTemp = TXMessage.creatGroupBigFileSms(
				txGroup.group_id, txGroup.group_title, filePath,
				mSess.getServerTime());
		uploadBigFile(txmsgTemp);// 上传大文件
		// TODO直接置为下载中状态，防止出现无网络点击下载按钮没反应，多个下载任务添加到队列的bug，后期还是要找多个重复任务添加到队列的问题所在
		txmsgTemp.updateState = TXMessage.UPDATING;
		mSess.getSocketHelper().addGroupMessage(txmsgTemp);

	}

	String lastString = "";// 上传一次发送的文字消息？

	/** 发送消息逻辑 */
	public void send(String text) {

		// if (!txdata.dataChange)
		// txdata.dataChange = true;
		// 发送按钮频率
		Long justClickTime = System.currentTimeMillis();
		if (txGroup != null) {
			if (TxGroup.isPublicGroup(txGroup)) {
				// 公共聊天室

				if (justClickTime - lastClickTime < 3000) {
					// lastClickTime = justClickTime;
					showToast("您发送消息过于频繁，请稍后再发");
					return;
				}

				if (text.equals(lastString) && lastString != null
						&& !lastString.equals("")) {
					if (justClickTime - lastClickTime < 60000) {
						showToast("请勿短时间内重复发送消息");
						return;
					}
				}
			}
		}

		if (sendimgPath == null) {// 发送文本
			lastClickTime = justClickTime;
			lastString = text;
			TXMessage txmsgTemp = TXMessage.creatGroupCommonSms(
					GroupMsgRoom.this, txGroup.group_id, txGroup.group_title,
					"这里是自己的头像地址", text, true, TXMessage.NOT_SENT,
					mSess.getServerTime());
			mSess.getSocketHelper().sendGroupMsg(txmsgTemp);

		} else {// 发送图片
			lastClickTime = justClickTime;
			final TXMessage txmsgTemp;
			txmsgTemp = TXMessage.creatGroupImageEms(GroupMsgRoom.this,
					txGroup.group_id, txGroup.group_title, "自己头像地址",
					sendimgPath, "", TXMessage.NOT_SENT, mSess.getServerTime());

			txmsgTemp.updateState = TXMessage.UPDATING;
			// 直接把自己发送图片的bitmap放在消息缓存中
			Bitmap bitmap1 = Utils.getImgByPath(mSess.getContext(),
					txmsgTemp.msg_path, Utils.msgroom_list_resolution);
			if (bitmap1 != null) {
				txmsgTemp.cacheImg = new SoftReference<Bitmap>(bitmap1);
			}

			Utils.executorService.submit(new Runnable() {// 大小图创建异步
						@Override
						public void run() {
							postImgSocket(txmsgTemp);
						}
					});
			mSess.getSocketHelper().addGroupMessage(txmsgTemp);
			sendimgPath = null;
		}

	}

	// }

	// menu菜单

	public boolean onCreateOptionsMenu(Menu menu) {
		// menu.add(0,MENU_DELET_MESSAGE,0,"删除对话");
		// menu.add(0,MENU_QUANTITYDELET_MESSAGE,1,"批量删除");
		// menu.add(0,MENU_INSERT_CONTENT,2,"插入联系人");
		// menu.add(0,MENU_CHANGE_CHANNEL,3,"切换通道");
		// menu.add(0,MENU_CHECK_CONTENT_INFO,4,"查看联系人信息");
		// menu.add(0,MENU_USER_INSTRUCT,5,"用户指引");
		MenuInflater mInflater = getMenuInflater();

		mInflater.inflate(R.menu.msg_menu_group, menu);

		return super.onCreateOptionsMenu(menu);

	}

	// @Override
	// protected void onNewIntent(Intent newintent) {
	// Intent intent1 = new Intent(GroupMsgRoom.this, GroupMsgRoom.class);
	// int threadid = newintent.getIntExtra(Utils.MSGROOM_THREAD_ID, -1);
	// TX tempTx = newintent.getParcelableExtra(Utils.MSGROOM_TX);
	// intent1.putExtra(Utils.MSGROOM_TX, tempTx);
	// if (threadid >= 0) {// 短信
	// intent1.putExtra(Utils.MSGROOM_THREAD_ID, threadid);
	// }
	// startActivity(intent1);
	// this.finish();
	// super.onNewIntent(intent1);
	//
	// setIntent(newintent);
	//
	// }

	// 单聊群聊一样，写在了BaseMsgRoom中 2014.01.21 shc
	// public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
	// if (wireControl == WIRECONTROL_PLAY) {
	// return false;
	// }
	// super.onPrepareOptionsMenu(menu);
	//
	// return true;
	// }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.msgroom_menu_deletMsg:
			new AlertDialog.Builder(GroupMsgRoom.this)
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

	public void clearGroupAllMsg() {
		if (Utils.isIdValid(txGroup.group_id)) {
			mSess.getSocketHelper().deleteGroupMessageAll(txGroup.group_id);
			MsgStat.delMsgStatByGroupId(mSess.getContentResolver(),
					txGroup.group_id);
		}

	}

	public int screeScoW;

	// 照相逻辑
	public void startPhotoCapture() {
		if (!Utils.isIdValid(txGroup.group_id)) {
			img_msg_id = Long.parseLong(TXMessage.default_sms_id);
		} else {
			img_msg_id = Utils.createMsgId("" + txGroup.group_id);
		}
		if (!Utils.checkSDCard()) {
			return;
		}
		super.startPhotoCapture();
	}

	// 选择图片逻辑
	public void startPicView() {
		String storagePath = Utils.getStoragePath(this);
		if (Utils.isNull(storagePath)) {
			// Utils.initStoragePath(BigPicActivity.this);
			return;
		}

		Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
		getImage.addCategory(Intent.CATEGORY_OPENABLE);
		getImage.setType(MIME_TYPE_IMAGE_JPEG);
		startActivityForResult(getImage, REQ_TAKE_PICTURE);
		// Utils.inPhoto = true;
		Utils.isNotificationShow = false;
	}

	/**
	 * 传感器接口目前没有用到
	 */
	// int isCallState;//刚进去时 0表示听筒 1表示非听筒
	private final class MySensorEventListener implements SensorEventListener {

		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			//

		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			//
			float x = event.values[SensorManager.DATA_X];

			if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {

			}

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

	int mDeltaY = 0;

	// 图片专为字节
	public static byte[] BitmapToBytes(Bitmap bm) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		bm.compress(Bitmap.CompressFormat.JPEG, 100, baos);

		return baos.toByteArray();
	}

	/** gps定位后发送的广播 */
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
								mSess.getContentResolver(), false);
						return;
					}

					gepTxmsgTemp.updateState = TXMessage.UPDATE_OK;
					mSess.getSocketHelper().sendGroupMsg(gepTxmsgTemp);
				}
			} else if (intent.getAction().equals(
					Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED)) {
				if (Utils.debug) {
					Log.e(TAG, "接受登陆成功的广播。这里能接收到吗？？？登陆成功时聊天室都没打开呢。");
				}
				if (groupid != 0) {
					mSess.getSocketHelper().sendInOutGroup(groupid, 0);

					mSess.getSocketHelper().sendGetGroupNewMessage(groupid,
							null);
					mSess.getSocketHelper().sendGetGroupOfflineMsg(groupid, "",
							1);

				}

			}

		}

	}

	// 设置信息提示栏是否显示
	// void setInfo() {
	// if (txGroup.group_title != null) {
	// groupName.setText(smileyParser.addSmileySpans(txGroup.group_title,
	// true, 0));
	// // grouPersonNum.setText("群");
	// }
	// }

	// 群按钮不同类型聊天的逻辑
	void GroupButtonLogic() {
		if (downTools != null && downTools.getVisibility() == View.VISIBLE) {
			Intent creatInfoIntent = new Intent(this,
					SelectFriendListActivity.class);
			creatInfoIntent.putExtra(
					SelectFriendListActivity.CHAT_TYPE_GROUP_OGJ, txGroup);
			creatInfoIntent.putExtra(SelectFriendListActivity.CHAT_TYPE,
					SelectFriendListActivity.CHAT_TYPE_GROUP);
			startActivity(creatInfoIntent);
		}
	}

	// 通过intent读取数据
	// long groupid;

	/**
	 * 初始进入时获取群的相关数据
	 */
	private void getIntentData() {

		txGroup = mainIntent.getParcelableExtra(Utils.MSGROOM_TX_GROUP);
		if (Utils.debug) {
			Log.i(TAG, "传递过来的群头像url为：" + txGroup.group_avatar);
		}
		groupid = mainIntent.getLongExtra(TX_GROUP_ID, 0);

		final long register_group_id;
		if (txGroup != null) {
			// initBan(txGroup);
			mSess.getSocketHelper().sendGetGroupBanList(txGroup.group_id, 0);// 获取禁言列表
			register_group_id = txGroup.group_id;
			groupid = txGroup.group_id;
		} else {
			register_group_id = groupid;
		}

		id_lastTxmsg = txGroup.group_id;

		autoTxmsg = mainIntent
				.getParcelableExtra(SelectFriendListActivity.CHAT_TYPE_ZF_OGJ);
		mGroupAsynloader.post(new Runnable() {
			@Override
			public void run() {

				synchronizedMsgs = mSess.getSocketHelper()
						.registerGroupHandler(register_group_id, msgHandler);// 该群消息的引用
				synchronized (synchronizedMsgs) {
					// 第一次取得的都是本地离线消息
					txMsgs.addAll(synchronizedMsgs);// 添加到txMsgs的list中
				}
				flush(FLUSH_ROOM_INIT);
				// 先执行隐藏群公告popupWindow操作
				dismissMsgRoomPopWindow();
				mSess.getSocketHelper().sendGetGroupNotice(register_group_id);// 获取群公告信息

				if (txGroup.isOfficialGroup()
						|| txGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC) {
					// 官方聊天室或者公告聊天室，拉取最新的10条消息
					String lastGmid = "0";
					if (synchronizedMsgs.size() > 0) {
						lastGmid = ""
								+ synchronizedMsgs.get(synchronizedMsgs.size() - 1).gmid;
					}
					if (Utils.debug) {
						Log.i(TAG, "最新的gmid是：" + lastGmid);
					}
					mSess.getSocketHelper().sendGetGroupNewMessage(
							register_group_id, lastGmid);
					// 星标好友在拉取完最新10条后

					if (txGroup.isOfficialGroup()) {
						// 官方聊天室注册被赞通知
						// 给点赞总数和点赞id设置默认值
						praisedNoticeCount = mSess.mPrefInfor.getPraisedCount();
						idList = mSess.mPrefInfor.getPraiseUserIds();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								tv_cur_room_praised_count.setText(""
										+ praisedNoticeCount);
								rl_praised_anim_view
										.setVisibility(View.VISIBLE);// 动画布局设为可见
							}
						});
						mSess.getPraiseNoticeDao().registReceiveNoticeListener(
								new PraiseNoticeImpl.IReceivePraiseNotice() {

									@Override
									public void onReceiveNotice(long groupId,
											long uid, boolean isFirstPraised) {
										// if(txGroup.group_id == groupId){
										// 聊天室id相等
										showToast("收到被点赞通知");
										if (isFirstPraised) {
											// 第一次收到被赞通知，隐藏软键盘，播放动画
											Utils.hideSoftInput(thisContext);// 隐藏软键盘
										}
										// praisedNoticeCount++;//被赞总数自增1
										// Long uidObj = uid;
										// if(idList.contains(uidObj)){
										// idList.remove(uidObj);
										// }
										// idList.add(0, uid);
										// if(idList.size()>PraiseNoticeImpl.PRAISED_USERS_LIMIT){
										// //如果点赞人大于12个，只显示最后点赞的12个人头像
										// idList.remove(idList.size()-1);
										// }
										praisedNoticeCount = mSess.mPrefInfor
												.getPraisedCount();
										idList = mSess.mPrefInfor
												.getPraiseUserIds();

										Message msg = Message.obtain();
										msg.what = RECEIVE_PRAISED_NOTICE;
										msg.obj = isFirstPraised;
										msgHandler.sendMessage(msg);
										// }

									}
								});
					}
				} else {
					// 其他类型的聊天室拉取10条离线消息
					mSess.getSocketHelper().sendGetGroupOfflineMsg(groupid, "",
							1);
					// gmid含义解释:string,全局消息id,由服务器生成,同一聊天室内唯一,实际上是64位无符号整数（因json标准没明确支持64位,所以用字符串类型）
				}
				mSess.getSocketHelper().sendInOutGroup(register_group_id, 0);

				NotificationPopupWindow.showGroupNotification(
						register_group_id, false);
			}
		});

		// initData();
		// 接收
		// isWindowCome =
		// mainIntent.getBooleanExtra(Utils.MSGROOM_WINDOW_IN,false);////注掉原因找MSGROOM_WINDOW_IN的定义处

	}

	/** 发消息隐藏公告信息 */
	private void dismissMsgRoomPopWindow() {
		MsgHandler.sendEmptyMessage(DISMISS_MSG_ROOM_POPUP);
	}

	// 将每个人名字合成一个字符串
	public String getPersonsName(String[] names) {
		StringBuffer nameBuffer = new StringBuffer();
		if (names != null) {
			for (String name : names) {
				nameBuffer.append(name + ",");
			}
		}
		return nameBuffer.toString();
	}

	// 判断下功能栏在被踢出群和群解散的时候隐藏
	LinearLayout downTools;

	public void missDownTool() {
		downTools = (LinearLayout) findViewById(R.id.publicmsg_layout);
		if (txGroup != null) {

			if (txGroup.group_tx_state != -1
					&& txGroup.group_tx_state == TxDB.QU_TX_STATE_OUT) {
				downTools.setVisibility(View.GONE);
			} else {
				downTools.setVisibility(View.VISIBLE);
			}
		}
	}

	// 单聊群聊一样，写在了BaseMsgRoom中 2014.01.21 shc
	// // 重写dispatchTouchEvent方法
	// public boolean dispatchTouchEvent(MotionEvent ev) {
	//
	// if (wireControl == WIRECONTROL_PLAY) {
	// return false;
	// }
	// return super.dispatchTouchEvent(ev);
	//
	// }

	// 达到500条消息上限的处理
	MsgMaxReceiver msgMaxReceiver;

	class MsgMaxReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					Constants.INTENT_ACTION_CLEAR_MSGS_FINISH)) {
				dealClearMsg(intent);
			} else if (intent.getAction().equals(
					Constants.INTENT_ACTION_DELETE_GROUP_MSG)) {
				cancelDialogTimer();
				dealDeleteMsg(intent);
			} else if (intent.getAction().equals(
					Constants.INTENT_ACTION_INOUT_GROUP_2030)) {
				dealInOutGroup(intent);
			} else if (Constants.INTENT_ACTION_SHUTUP_GROUP_2028.equals(intent
					.getAction())) {
				// 处理禁言结果
				cancelDialogTimer();
				dealShutup(intent);
			} else if (intent.getAction().equals(
					Constants.INTENT_ACTION_GROUP_BAN_LIST)) {
				// 收到禁言列表
				dealBanList(intent);

			}
		}

	}

	private void dealInOutGroup(Intent intent) {
		ServerRsp serverRsp = Utils.getServerRsp(intent);
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
		case OPT_FAILED:
			break;
		case GROUP_FULL:
			if (txGroup != null) {
				if (TxGroup.isPublicGroup(txGroup)) {
					showToastText("聊天室已满员");
				} else {
					showToastText("你不是该聊天室成员");
				}
				GroupMsgRoom.this.finish();
			}
			break;
		case GROUP_NO_EXIST:
			showToastText("聊天室已不存在");
			missDownTool();
			downTools.setVisibility(View.GONE);
			break;
		case USER_IN_BLACK:
			showToastText("你已被加入此聊天室黑名单");
			GroupMsgRoom.this.finish();
			break;

		}
	}

	private void dealDeleteMsg(Intent intent) {
		ServerRsp serverRsp = Utils.getServerRsp(intent);
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			long gmid = serverRsp.getBundle().getLong("gmid");
			for (int i = 0; i < txMsgs.size(); i++) {
				if (txMsgs.get(i).gmid == gmid) {
					localDelete(txMsgs.get(i), null);
				}
			}
			break;
		case NO_PERMISSION:
			showToastText("没有这个权限");
			break;
		case MSG_NO_EXIST:
			showToastText("消息不存在");
			break;
		case OPT_FAILED:
			showToastText("操作失败");
			break;
		}
	}

	private void showToastText(final CharSequence s) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(GroupMsgRoom.this, s, Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void dealClearMsg(Intent intent) {
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

	@Override
	protected void setText4Data() {
		super.setText4Data();
		if (txGroup != null) {
			if (!TxGroup.isPublicGroup(txGroup)) {
				// 非官方聊天室或公开聊天室
				text4.setText("查看群成员");
			}
			// if (txGroup.group_type_channel == TxDB.GROUP_TYPE_REQUEST
			// || txGroup.group_type_channel == TxDB.GROUP_TYPE_SECRET) {
			// text4.setText("查看聊天室成员");
			// }
		}
	}

	@Override
	protected void clearAllMsg() {
		new AlertDialog.Builder(GroupMsgRoom.this)
				.setMessage(R.string.msg_delete_all)
				.setPositiveButton(R.string.confirm,
						new DialogInterface.OnClickListener() {
							public void onClick(
									DialogInterface dialoginterface, int i) {
								clearGroupAllMsg();
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

	// 下载名片

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
			txmsg.msg_path = taskInfo.mFullName;
			txmsg.updateState = TXMessage.UPDATE_OK;
			String name = txmsg.msg_path.substring(
					txmsg.msg_path.lastIndexOf("/") + 1,
					txmsg.msg_path.length());
			Contact contact = new Contact(GroupMsgRoom.this);
			contact.readToFile(name, GroupMsgRoom.this);
			ContentValues values = new ContentValues();
			values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
			cr.update(TxDB.Messages.CONTENT_URI, values, TxDB.Messages.MSG_ID
					+ "=?", new String[] { "" + txmsg.msg_id });
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
		TXMessage txmsgTemp = TXMessage.creatGroupGeoSms(GroupMsgRoom.this,
				txGroup.group_id, txGroup.group_title,
				// "自己头像地址", geo, 0, System.currentTimeMillis() / 1000 +
				// getPrefsMeme().getLong(CommonData.ST, 0));
				"自己头像地址", geo, 0, mSess.getServerTime());

		if (txmsgTemp != null) {

			return txmsgTemp;

		}
		return null;
	}

	@Override
	public void sendMsg(TXMessage txmsg) {
		mSess.getSocketHelper().sendGroupMsg(txmsg);
	}

	@Override
	public void upToolList1Logic() {
		Intent intent = new Intent(GroupMsgRoom.this, GroupInfo.class);
		intent.putExtra(Utils.MSGROOM_GROUP_ID, txGroup.group_id);
		startActivity(intent);
		turn = true;
		if (TxGroup.isPublicGroup(txGroup)) {
			Utils.isNotificationShow = false;
		} else {
			Utils.isNotificationShow = true;
		}
	}

	@Override
	public void upToolList1Text() {
		if (txGroup != null && !TxGroup.isPublicGroup(txGroup)) {
			// 是群组非聊天室
			checkGroupInfoText.setText("查看群资料");
		} else {
			checkGroupInfoText.setText(R.string.msg_menu_add_guoup_info);
		}
	}

	@Override
	public void addMsg(TXMessage txmsg) {
		mSess.getSocketHelper().addGroupMessage(txmsg);
	}

	// 创建待转发的音频消息
	@Override
	public TXMessage getForwardImgTxmsg(String sendImgPath, TXMessage txmsg) {
		TXMessage txmsgTemp = TXMessage.creatGroupImageEms(mSess.getContext(),
				txGroup.group_id, txGroup.group_title, "自己头像地址",
				// sendImgPath, "", 0, System.currentTimeMillis() / 1000 +
				// getPrefsMeme().getLong(CommonData.ST, 0));
				sendImgPath, "", 0, mSess.getServerTime());
		txmsgTemp.updateState = TXMessage.UPDATE_OK;
		return txmsgTemp;
	}

	/** 创建一个音频消息，准备发送 */
	@Override
	public TXMessage getNewAudioTxmsg() {
		TXMessage audioTxmsg = TXMessage.creatGroupAudioEms(GroupMsgRoom.this,
				txGroup.group_id, txGroup.group_title, "", "", "", 0, 0,// 这里本来是1，现在改为0，因为用户方发出音频消息时，音频时长先不显示，等录音延时的1s结束后再设置录音时长，在getView中，如果音频时长为0，则不显示时长
				// "amr", 0, System.currentTimeMillis() / 1000 +
				// getPrefsMeme().getLong(CommonData.ST, 0));
				"amr", 0, mSess.getServerTime());
		return audioTxmsg;
	}

	/** 创建一个待转发的群音频消息，userPhone是单聊语音需要的参数，群语音不用 */
	@Override
	public TXMessage getForwardAudioTxmsg(TXMessage txmsg) {
		TXMessage tempTxmsg = TXMessage.creatGroupAudioEms(mSess.getContext(),
				txGroup.group_id, txGroup.group_title, "自己头像地址",
				txmsg.msg_path, txmsg.msg_url, txmsg.msg_file_length,
				txmsg.audio_times, "amr", 0, mSess.getServerTime());

		tempTxmsg.updateState = TXMessage.UPDATE_OK;

		return tempTxmsg;
	}

	// 创建待转发的位置消息
	public TXMessage getForwardGeoTxmsg(TXMessage txmsg) {
		TXMessage txmsgTemp1 = TXMessage.creatGroupGeoSms(mSess.getContext(),
				txGroup.group_id, txGroup.group_title, "自己头像地址",
				// txmsg.geo, 0, System.currentTimeMillis() / 1000 +
				// getPrefsMeme().getLong(CommonData.ST, 0));
				txmsg.geo, 0, mSess.getServerTime());
		return txmsgTemp1;
	}

	// 创建待转发的名片消息
	public TXMessage getForwardTCardTxmsg(TXMessage txmsg) {
		TXMessage txmsg2 = TXMessage.creatGroupTCardEms(mSess.getContext(),
				txmsg.contacts_person_name, txGroup.group_id,
				txGroup.group_title, "", txmsg.tcard_name, txmsg.tcard_id,
				txmsg.tcard_sign, txmsg.tcard_sex, txmsg.tcard_phone,
				txmsg.tcard_avatar_url, TXMessage.NOT_SENT,
				mSess.getServerTime());
		txmsg2.updateState = TXMessage.UPDATE;
		return txmsg2;
	}

	@Override
	public void changeMsg(TXMessage txmsg) {
		mSess.getSocketHelper().changeGroupMessageState(txGroup.group_id,
				txmsg.msg_id, txmsg.read_state);
	}

	// @Override
	// public void SaveAdiouState(TXMessage txmsg) {
	// SocketHelper.getSocketHelper(txdata).changeGroupMessageState(
	// txGroup.group_id, txmsg.msg_id, txmsg.read_state);
	// }

	/** 确认删除消息的点击事件处理 */
	@Override
	public void deleteTxMsg(DialogInterface dialoginterface, TXMessage txmsg) {

		// 确认删除的点击事件
		// 如果是文件任务，则取消任务发送
		if (txmsg.was_me) {
			switch (txmsg.msg_type) {
			case TxDB.MSG_TYPE_QU_IMAGE_EMS:
			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
			case TxDB.MSG_TYPE_QU_CARD_EMS:
			case TxDB.MSG_TYPE_QU_TCARD_SMS:
			case TxDB.MSG_TYPE_QU_GEO_SMS:
				// 如果是我发送的消息，则为上传消息，任务id为文件全路径
				String uploadTaskId = mSess.mDownUpMgr.getUploadTaskId(Utils
						.getUploadImageTempPath(txmsg.msg_id));
				mSess.mDownUpMgr.removeUploadTask(uploadTaskId);
				break;
			case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
				// 删除群上传大文件消息
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
			case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
				// 删除群下载大文件消息
				getDownUpMgr().removeDownloadBigTask(
						getDownUpMgr().getDownloadTaskId(txmsg.msg_url), true);
				break;
			}
		}

		if (txGroup != null) {
			boolean isOp = false;
			if (TxData.isOP() || TxData.isCloOP()) {
				isOp = true;
			}

			int type = TxGroup.checkAdminCreator(txGroup);// 返回值：本人在群众的状态是群主、管理员、成员
			// 创建者或管理员 可以删服务器消息
			if (type == TxDB.QU_TX_STATE_OWN || type == TxDB.QU_TX_STATE_GM
					|| (isOp && TxGroup.isPublicGroup(txGroup))) {
				// 群主或者是管理员或者（是op并且是公开群）， 可以删服务器消息
				if (txmsg.msg_type != TxDB.MSG_TYPE_QU_BIG_FILE_SMS
						|| (txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS && txmsg.read_state != TXMessage.NOT_SENT)) {
					// 非大文件消息，或者已经传输完毕的大文件消息
					showDialogTimer(GroupMsgRoom.this, R.string.prompt,
							R.string.group_deling, 10 * 1000).show();
					mSess.getSocketHelper().sendDeleteGroupMsg(
							String.valueOf(txmsg.gmid), groupid);
				} else {
					localDelete(txmsg, dialoginterface);
				}
			} else {
				localDelete(txmsg, dialoginterface);
			}
		}
		// else {
		// //这里应该走不到吧,这里txGroup==null,并且localDelete调用了txGroup，如果txGroup等于null，应该会空指针异常
		// 2014.01.21 shc
		// localDelete(txmsg, dialoginterface);
		// }
	}

	/** 群聊中他人头像的点击和长按事件 */
	public void setHeadViewOnClickEvent(ImageView iv_headView, TXMessage txmsg) {
		iv_headView.setTag(txmsg);
		iv_headView.setOnClickListener(mHeadOnClick);
		iv_headView.setOnLongClickListener(new OnLongClickListener() {

			@Override
			public boolean onLongClick(View v) {
				TXMessage txmsg = (TXMessage) v.getTag();
				createWindow(txmsg);
				return false;
			}
		});
	};

	// 群聊中他人头像的单击事件
	OnClickListener mHeadOnClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			final TXMessage txmsg = (TXMessage) v.getTag();
			if (waitDouble == true) {
				waitDouble = false;
				Thread thread = new Thread() {
					@Override
					public void run() {
						try {
							sleep(300);
							if (waitDouble == false) {
								waitDouble = true;
								// 单击事件处理
								inTxInfoActivity(txmsg.partner_id);
							}
						} catch (InterruptedException e) {
							// block
							if (Utils.debug)
								e.printStackTrace();
						}
					}
				};
				thread.start();
			} else {
				waitDouble = true;
				// 双击事件处理
				if (msgEdit != null) {
					String tempStr = msgEdit.getText().toString() + "@"
							+ txmsg.partner_name + " ";
					msgEdit.setText(smileyParser.addSmileySpans(tempStr, true,
							0));
					msgEdit.setSelection(tempStr.length());
					Utils.isSendText = true;
					changeType(false, false);
				}
			}
		}
	};

	// 进入发送该条消息人的资料界面
	public void inTxInfoActivity(Long partner_id) {
		if (Utils.isIdValid(partner_id)) {

			// TX tx1 = TX.findTXByPartnerID4DB(partner_id);//不要直接访问数据库
			// 2013.10.17 shc
			TX tx1 = TX.tm.getTx(partner_id);

			if (tx1 == null) {
				tx1 = new TX();
				for (TXMessage msgs : txMsgs) {
					if (msgs.partner_id == partner_id) {
						tx1.partner_id = msgs.partner_id;
						tx1.setNick_name(msgs.nick_name);
						tx1.avatar_url = msgs.avatar_url;
					}
				}

			}
			Intent iSupplement = new Intent(this, UserInformationActivity.class);
			iSupplement
					.putExtra(
							UserInformationActivity.pblicInfo,
							TX.tm.isTxFriend(partner_id) == true ? UserInformationActivity.TUIXIN_USER_INFO
									: UserInformationActivity.NOT_TUIXIN_USER_INFO);
			iSupplement.putExtra(UserInformationActivity.UID, tx1.partner_id);
			// startActivity(iSupplement);
			startActivityForResult(iSupplement, RESULT_OK);
			turn = true;
			if (TxGroup.isPublicGroup(txGroup)) {
				Utils.isNotificationShow = false;
			} else {
				Utils.isNotificationShow = true;
			}
			if (Utils.debug) {
				Log.i(TAG, "Utils.isNotificationShow init is"
						+ Utils.isNotificationShow);
			}

			mUpdateId = partner_id;

		}
	}

	// PopupWindow noticePopWindow;

	public void createNoticePop(final String msg) {
		// if (noticePopWindow != null) {
		// noticePopWindow.dismiss();
		// }
		// LayoutInflater mInflate = LayoutInflater.from(this);
		// View toastView = mInflate.inflate(R.layout.sll_msgroom_notice, null);
		// noticePopWindow = new PopupWindow(toastView,
		// LinearLayout.LayoutParams.FILL_PARENT,
		// LinearLayout.LayoutParams.WRAP_CONTENT, false);
		// RelativeLayout toastRelitive = (RelativeLayout)
		// toastView.findViewById(R.id.reletive);
		toastNotice.getBackground().setAlpha(200);
		mScrollView = (ScrollView) toastNotice.findViewById(R.id.scroll);
		noticeText = (TextView) toastNotice
				.findViewById(R.id.msgRoom_notice_msg);
		if (msg != null && !msg.equals("")) {
			noticeText.setText(msg);

		} else {

			noticeText.setText("暂时没有聊天室公告");
		}

		if (timer != null) {
			if (timerTask != null) {
				timerTask.cancel();
			}
		}
		timerTask = new task();
		timer = new Timer(true);
		timer.schedule(timerTask, 200, 200);

		noticeText.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (noticePopWindow != null) {
				// noticePopWindow.dismiss();
				// }
				toastNotice.setVisibility(View.GONE);
				createMoreNoticePop(msg);
			}
		});
		View noticeClose = toastNotice.findViewById(R.id.notice_close);
		noticeClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// if (noticePopWindow != null) {
				// noticePopWindow.dismiss();
				// }
				toastNotice.setVisibility(View.GONE);

			}
		});

		// noticePopWindow.setOnDismissListener(new OnDismissListener() {
		//
		// @Override
		// public void onDismiss() {
		// 点赞窗口上移//TODO 这么处理有问题，被赞计数器会消失 2014.05.09 shc
		// if(txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL){
		// //官方聊天室
		// RelativeLayout.LayoutParams param = new
		// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
		// Utils.dip2px(26, mSess.getContext()));
		// param.setMargins(0, Utils.dip2px(10, mSess.getContext()),
		// Utils.dip2px(10, mSess.getContext()), 0);
		// tv_cur_room_praised_count.setLayoutParams(param);
		// if (isSetPraiseLocation) {
		// //从新定位布局坐标
		// iv_aminend.getLocationInWindow(locations);
		// iv.getLocationInWindow(location2s);
		// }
		//
		// }

		// }
		// });
		// noticePopWindow.setFocusable(false);
		// noticePopWindow.setBackgroundDrawable(new BitmapDrawable());
		// noticePopWindow.update();

		if (!isFinishing()) {

			toastNotice.setVisibility(View.VISIBLE);
			// noticePopWindow.showAsDropDown(uplayout, 0, 0);
			// 点赞窗口下移
			// if(txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL){
			// //官方聊天室
			// RelativeLayout.LayoutParams param =
			// (android.widget.RelativeLayout.LayoutParams)
			// tv_cur_room_praised_count.getLayoutParams();
			// if(param==null){
			// param = new
			// RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
			// Utils.dip2px(26, mSess.getContext()));
			// }
			//
			// int top = Utils.dip2px(10,
			// mSess.getContext())+toastView.getHeight();
			// param.setMargins(0, top, Utils.dip2px(10, mSess.getContext()),
			// 0);
			// tv_cur_room_praised_count.setLayoutParams(param);
			// tv_cur_room_praised_count.requestLayout();
			// if(Utils.debug){
			// Log.e(TAG, "从新设置了被赞数布局,top = "+top);
			// }
			// if (isSetPraiseLocation) {
			// //从新定位布局坐标
			// iv_aminend.getLocationInWindow(locations);
			// iv.getLocationInWindow(location2s);
			// }
			//
			// }
		}

	}

	// PopupWindow noticeMorePopWindow;
	// View toastView;

	/**
	 * 点击公告
	 * 
	 * @param msg
	 */
	public void createMoreNoticePop(String msg) {
		if (noticeMorePopWindow != null) {
			noticeMorePopWindow.dismiss();
		}
		// RelativeLayout uplayout = (RelativeLayout)
		// findViewById(R.id.MsgRoom_personInfo);
		LayoutInflater mInflate = LayoutInflater.from(GroupMsgRoom.this);

		toastView = mInflate.inflate(R.layout.sl_group_msgroom_more_notice,
				null);

		noticeMorePopWindow = new PopupWindow(toastView,
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT, false);

		TextView morenoticeText = (TextView) toastView
				.findViewById(R.id.sl_group_msg_more);
		if (msg != null && !msg.equals("")) {
			morenoticeText.setText(msg);

		} else {

			morenoticeText.setText("暂时没有聊天室公告");

		}
		morenoticeText.setAutoLinkMask(Linkify.ALL);
		setLinkClickIntercept(morenoticeText);

		View noticeClose = toastView.findViewById(R.id.notice_close);
		noticeClose.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (noticeMorePopWindow != null) {
					noticeMorePopWindow.dismiss();
				}

			}
		});
		noticeMorePopWindow.setFocusable(false);
		noticeMorePopWindow.setBackgroundDrawable(new BitmapDrawable());
		noticeMorePopWindow.update();
		// noticeMorePopWindow.showAsDropDown(uplayout, 0,100);
		if (!isFinishing()) {
			noticeMorePopWindow.showAtLocation(uplayout,
					Gravity.CENTER_VERTICAL, 0, 0);
		}

	}

	public class task extends TimerTask {

		@Override
		public void run() {
			if (mSleepTickCount > 0) {
				mSleepTickCount++;
				if (mSleepTickCount > 5)
					mSleepTickCount = 0;
				return;
			}
			mCurYPos++;
			if (mCurYPos <= noticeText.getHeight()) {
				MsgHandler.sendEmptyMessage(FLUSH_NOTICE);
				if (mLineHeight == mCurYPos) {
					mSleepTickCount = 1;
				}
			} else {
				mCurYPos = 0;
				mLineHeight = 35;
			}
		}
	}

	private void setLinkClickIntercept(TextView tv) {

		tv.setMovementMethod(LinkMovementMethod.getInstance());
		CharSequence text = tv.getText();
		if (text instanceof Spannable) {
			int end = text.length();

			Spannable sp = (Spannable) tv.getText();

			URLSpan[] urls = sp.getSpans(0, end, URLSpan.class);

			if (urls.length == 0) {

				return;

			}

			SpannableStringBuilder spannable = new SpannableStringBuilder(text);
			// 只拦截 http:// URI

			LinkedList<String> myurls = new LinkedList<String>();

			for (URLSpan uri : urls) {

				String uriString = uri.getURL();

				myurls.add(uriString);
			}

			// 循环把链接发过去

			for (URLSpan uri : urls) {

				String uriString = uri.getURL();
				MyURLSpan myURLSpan = new MyURLSpan(uriString, myurls);

				spannable.setSpan(myURLSpan, sp.getSpanStart(uri),

				sp.getSpanEnd(uri), Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
			}

			tv.setText(spannable);

		}

	}

	private class MyURLSpan extends ClickableSpan {
		private String mUrl; // 当前点击的实际链接

		private LinkedList<String> mUrls; // 根据需求，一个TextView中存在多个link的话，这个和我求有关，可已删除掉

		// 无论点击哪个都必须知道该TextView中的所有link，因此添加改变量

		MyURLSpan(String url, LinkedList<String> urls) {
			mUrl = url;
			mUrls = urls;
		}

		@Override
		public void onClick(View widget) {
			// 这里你可以做任何你想要的处理

			// 比如在你自己的应用中用webview打开，而不是打开系统的浏览器

			String info = new String();

			if (mUrls.size() == 1) {
				// 只有一个url，根据策略弹出提示对话框
				info = mUrls.get(0);

			} else {
				// 多个url，弹出选择对话框，意思一下

				info = mUrls.get(0) + "\n" + mUrls.get(1);

			}

			Intent i = new Intent(GroupMsgRoom.this, ADActivity.class);
			i.putExtra("url", info);
			startActivity(i);
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		// InputMethodManager imm = (InputMethodManager) GroupMsgRoom.this
		// .getSystemService(Context.INPUT_METHOD_SERVICE);
		// if (imm != null && imm.isActive()) {
		// imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		// }
		// if (downMorePopWindow != null && downMorePopWindow.isShowing()) {
		// downMorePopWindow.dismiss();
		// }
		// if (mPop.getVisibility() == View.VISIBLE) {
		// mPop.setVisibility(View.GONE);
		// }

		super.onTouch(v, event);
		if (noticeMorePopWindow != null && noticeMorePopWindow.isShowing()) {
			noticeMorePopWindow.dismiss();
			toastNotice.setVisibility(View.VISIBLE);
		}
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			if (noticeMorePopWindow != null && noticeMorePopWindow.isShowing()) {
				Rect t = new Rect();
				toastView.getLocalVisibleRect(t);
				if (!t.contains((int) event.getRawX(), (int) event.getRawY())
						&& !t.contains((int) event.getRawX(),
								(int) event.getRawY())) {
					if (noticeMorePopWindow != null
							&& noticeMorePopWindow.isShowing())
						noticeMorePopWindow.dismiss();
					toastNotice.setVisibility(View.VISIBLE);
				}
			}
		}
		return false;
	}

	public void dealShutup(Intent intent) {

		ServerRsp serverRsp = Utils.getServerRsp(intent);
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			if (Utils.debug)
				Log.i(TAG, "处理禁言的结果：" + serverRsp.getBundle().toString());
			int op = serverRsp.getBundle().getInt("op");
			boolean did = serverRsp.getBundle().getBoolean("did");
			Long uid = serverRsp.getBundle().getLong("uid");
			if (op == 0) {
				banList.add(uid);
			} else {
				banList.remove(uid);

			}
			if (did) {
				if (op == 0) {
					// 被禁言
					showToast(R.string.shutup_done);
				} else {
					// 被取消禁言
					showToast(R.string.shutup_cancel);
				}
			} else {
				showToast(R.string.opt_success);
			}

			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupMsgRoom.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupMsgRoom.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_NOT_MEMBER:
			Utils.startPromptDialog(GroupMsgRoom.this, R.string.prompt,
					R.string.group_not_member);
			break;
		case GROUP_NO_EXIST:
			Utils.startPromptDialog(GroupMsgRoom.this, R.string.prompt,
					R.string.group_not_exists);
			break;
		}

	}

	/** 处理禁言列表 */
	private void dealBanList(Intent intent) {
		ServerRsp serverRsp = Utils.getServerRsp(intent);
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			banList.addAll((Set<Long>) serverRsp.getBundle().getSerializable(
					GROUP_BAN_LIST));
			if (Utils.debug)
				Log.i(TAG, "进入聊天室获取的禁言列表：" + banList.toString());
			break;
		case OPT_FAILED:
			showToastText("获取禁言列表失败");
			break;
		}

	}

	// private void showToastText(final int msg) {
	// runOnUiThread(new Runnable() {
	//
	// @Override
	// public void run() {
	// Toast.makeText(GroupMsgRoom.this, msg, Toast.LENGTH_SHORT).show();
	// }
	// });
	// }

	/** 无引用，注掉 2014.01.03 */
	// private String initBan(TxGroup txGroup) {
	// banList = new HashSet<Long>();
	// for (String id : txGroup.ban_ids.split("�")) {
	// if (!Utils.isNull(id)) {
	// banList.add(Long.valueOf(id));
	// }
	// }
	// return "";
	// }

	private GroupStarAdapter starAdapter = null;// 星标好友的adapter
	private PraisedUsersAdapter praisedUsersAdapter = null;// 点赞用户头像的adapter

	// 派生一个群聊adapter类
	class GroupAdapter extends GroupContectListAdapter {

		@Override
		protected TextView updateView(ViewHolder holder) {
			super.updateView(holder);
			TXMessage txmsg = holder.txmsg;
			ImageView iv_headCrown = null;// 本期之星头像皇冠

			if (txmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
				// 显示本期之星的条目
				// dateView.setVisibility(View.GONE);
				ll_my_head_icon.setVisibility(View.GONE);
				holder.last_week_starts.setVisibility(View.VISIBLE);
				if (starAdapter == null) {
					ArrayList<Long> starIds = mGroupStarsMap
							.get(txGroup.group_id);
					Collections.shuffle(starIds);// 随机排序
					starAdapter = new GroupStarAdapter(thisContext, starIds,
							mSess);
				}
				if (holder.gv_last_week_stars.getAdapter() != starAdapter) {
					holder.gv_last_week_stars.setAdapter(starAdapter);
				}

			} else {
				// 先把活跃之星的gridview清空，否则滑动会卡
				holder.last_week_starts.setVisibility(View.GONE);
				if (holder.gv_last_week_stars.getAdapter() != null) {
					holder.gv_last_week_stars.setAdapter(null);
				}

				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS) {
					// 公告提示
					rl_msgHeadView.setVisibility(View.GONE);
					ll_my_head_icon.setVisibility(View.GONE);
					holder.list_type12.setVisibility(View.VISIBLE);
					// 更新短信内容
					TextView msgTextView = (TextView) holder.list_type12
							.findViewById(R.id.msgroomitem_list12_MsgText);
					if (txmsg.cache_charSequence == null) {
						txmsg.cache_charSequence = smileyParser.addSmileySpans(
								linkUser(txmsg.msg_body, txmsg.tcard_name,
										txmsg.tcard_id, txmsg), true, 0);
					}
					msgTextView.setText(txmsg.cache_charSequence);
					msgTextView.setMovementMethod(LinkMovementMethod
							.getInstance());

				} else {
					if (txmsg.was_me) {
						switch (txmsg.msg_type) {
						case TxDB.MSG_TYPE_QU_COMMON_SMS:
							// 更新短信内容
							TextView msgTextView = (TextView) holder.leftlist_type1
									.findViewById(R.id.msgroomitem_List1_MsgText);
							// 已在群聊文字信息中加上txmsg.partner_id，
							// 下面的txmsg.msg_type==TxDB.MSG_TYPE_QU_COMMON_SMS是为了兼容老版本，让自己发的群信息能正常显示，老版本中自己发送的群消息没有txmsg.partner_id
							if (txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS
									|| Utils.isIdValid(txmsg.partner_id)) {
								if (txmsg.cache_charSequence == null) {
									CharSequence s = smileyParser
											.addSmileySpans(txmsg.msg_body,
													false, 0);
									txmsg.cache_charSequence = smileyParser
											.addAtpans(s);
								}
								msgTextView.setText(txmsg.cache_charSequence);
							}

						case TxDB.MSG_TYPE_QU_AUDIO_EMS:
						case TxDB.MSG_TYPE_QU_CARD_EMS:
						case TxDB.MSG_TYPE_QU_TCARD_SMS:
						case TxDB.MSG_TYPE_QU_GEO_SMS:
							switch (txmsg.read_state) {
							case TXMessage.UNREAD:
							case TXMessage.READ:
								sendTypeView.setText(null);
								break;
							case TXMessage.FORBID:
								sendTypeView.setText(R.string.msg_forbid);
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

						// 判断是否是星标好友，如果是则显示皇冠背景
						if (Utils.isIdValid(txmsg.partner_id)
								&& txGroup != null) {
							ArrayList<Long> idList = mGroupStarsMap
									.get(txGroup.group_id);
							if (idList != null
									&& idList.contains(txmsg.partner_id)) {
								holder.headView
										.setBackgroundResource(R.drawable.group_star_bg);
								holder.headView.setPadding(4, 4, 4, 4);
								iv_headCrown.setVisibility(View.VISIBLE);
							}
						}
					}
				}
			}

			return dateView;// 这个日期textView的返回是为了统一处理日期的显示和隐藏

		}

	}

	// 上周活跃之星的Adapter
	public static class GroupStarAdapter extends BaseAdapter {
		private Activity contextAct;
		private ArrayList<Long> mIdList;
		private SessionManager mSess;

		public GroupStarAdapter(Activity contextAct, ArrayList<Long> idList,
				SessionManager mSess) {
			this.contextAct = contextAct;
			this.mIdList = idList;
			this.mSess = mSess;
		}

		@Override
		public int getCount() {
			return mIdList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(contextAct).inflate(
					R.layout.sl_group_star_gridview_item, null);
			final ImageView iv_head = (ImageView) convertView
					.findViewById(R.id.iv_group_star_head);
			iv_head.setLayoutParams(new LinearLayout.LayoutParams(
					(int) toScaleZoomWidth(15, 5), (int) toScaleZoomHeight()));
			TextView tv_name = (TextView) convertView
					.findViewById(R.id.tv_group_star_nickname);
			if (position < mIdList.size()) {

				long partner_id = mIdList.get(position);
				final TX tx = TX.tm.getTx(partner_id);
				if (tx != null) {
					// tx.sex == 0:男性
					tv_name.setTextColor(tx.getSex() == TX.MALE_SEX ? Color
							.parseColor("#486f81") : Color
							.parseColor("#fc3ea0"));
					// tv_name.setText(tx.getNick_name());
					tv_name.setText(smileyParser.addSmileySpans(
							tx.getNick_name(),
							tv_name.getPaint().getTextSize(), false));
					Bitmap headBitmap = mSess.avatarDownload
							.getPartnerCachedBitmap(partner_id);
					if (headBitmap == null) {
						headBitmap = mSess.cachePartnerDefault(partner_id,
								tx.getSex());
						iv_head.setImageBitmap(headBitmap);
						iv_head.setTag(partner_id);
						iv_heads.add(iv_head);
						mSess.avatarDownload.downAvatar(tx.avatar_url,
								partner_id, iv_head, avatarHandler);

					} else {
						iv_head.setImageBitmap(headBitmap);
					}
				}

				iv_head.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						// 头像的点击事件
						Intent intent = null;
						if (TX.tm.getUserID() == tx.partner_id) {
							intent = new Intent(contextAct,
									SetUserInfoActivity.class);
						} else {
							intent = new Intent(contextAct,
									UserInformationActivity.class);
							intent.putExtra(
									UserInformationActivity.pblicInfo,
									TX.tm.isTxFriend(tx.partner_id) ? UserInformationActivity.TUIXIN_USER_INFO
											: UserInformationActivity.NOT_TUIXIN_USER_INFO);
							intent.putExtra(UserInformationActivity.UID,
									tx.partner_id);
						}
						contextAct.startActivity(intent);
					}
				});
			}

			return convertView;
		}

		public float toScaleZoomWidth(int horizontalSpacing, int margin) {
			DisplayMetrics metrics = new DisplayMetrics();
			metrics = contextAct.getResources().getDisplayMetrics();
			int width = metrics.widthPixels;
			float density = metrics.densityDpi;
			float hs_pixs = horizontalSpacing * (density / 160);
			float margin_pixs = margin * (density / 160);
			float divideWidth = (width - ((hs_pixs * 9) + (margin_pixs * 2))) / 5;
			if (Utils.debug) {
				Log.i("GroupInfo", "width的值：" + width + "*****density的值:"
						+ density + "*****hs_pixs的值:" + hs_pixs
						+ "*****margin_pixs的值：" + margin_pixs
						+ "*****divideWidth的值:" + divideWidth);
			}
			return divideWidth;
		}

		public float toScaleZoomHeight() {
			int imageWidth = 85;
			int imageHeight = 85;
			float scaleHeight = (imageHeight * toScaleZoomWidth(15, 5))
					/ imageWidth;

			return scaleHeight;
		}
	}

	// 点赞用户头像的Adapter
	public class PraisedUsersAdapter extends BaseAdapter {
		private Activity contextAct;
		private List<Long> mIdList;
		private SessionManager mSess;

		public PraisedUsersAdapter(Activity contextAct, List<Long> idList,
				SessionManager mSess) {
			this.contextAct = contextAct;
			this.mIdList = idList;
			this.mSess = mSess;
		}

		// public PraisedUsersAdapter(Activity contextAct, SessionManager mSess)
		// {
		// this.contextAct = contextAct;
		// this.mSess = mSess;
		// }

		@Override
		public int getCount() {
			return idList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			convertView = LayoutInflater.from(contextAct).inflate(
					R.layout.sl_praised_users_icon_gridview_item, null);
			final ImageView iv_head = (ImageView) convertView
					.findViewById(R.id.iv_group_star_head);
			// iv_head.setLayoutParams(new LinearLayout.LayoutParams(
			// (int) toScaleZoomWidth(15, 5), (int) toScaleZoomHeight()));
			if (position < idList.size()) {

				long partner_id = idList.get(position);
				iv_head.setTag(partner_id);
				Bitmap bmHead = mSess.avatarDownload.getHeadIcon(partner_id,
						new AsyncCallback<Bitmap>() {

							@Override
							public void onSuccess(Bitmap result, long id) {
								if (((Long) iv_head.getTag()) == id
										&& result != null) {
									iv_head.setImageBitmap(result);
								}
							}

							@Override
							public void onFailure(Throwable t, long id) {

							}
						});
				if (bmHead != null) {
					iv_head.setImageBitmap(bmHead);
				} else {
					// 默认头像全部为女的
					iv_head.setImageBitmap(mSess
							.getDefaultPartnerAvatar(TX.FEMAL_SEX));
				}

				// 下面这个方法不行，因为有被赞通知中只有该用户的uid,没有性别和头像等信息，所以如果该用户本地数据库中没有，则取到的tx一定为空
				// 2014.05.29 shc
				// final TX tx = TX.tm.getTx(partner_id);
				// if (tx != null) {
				// Bitmap headBitmap = mSess.avatarDownload
				// .getPartnerCachedBitmap(partner_id);
				// if (headBitmap == null) {
				// headBitmap = mSess.cachePartnerDefault(partner_id,
				// tx.getSex());
				// iv_head.setImageBitmap(headBitmap);
				// iv_head.setTag(partner_id);
				// iv_heads.add(iv_head);
				// mSess.avatarDownload.downAvatar(tx.avatar_url,
				// partner_id, iv_head, avatarHandler);
				//
				// } else {
				// iv_head.setImageBitmap(headBitmap);
				// }
				// }

			}
			return convertView;

		}

		// public float toScaleZoomWidth(int horizontalSpacing, int margin) {
		// DisplayMetrics metrics = new DisplayMetrics();
		// metrics = contextAct.getResources().getDisplayMetrics();
		// int width = metrics.widthPixels;
		// float density = metrics.densityDpi;
		// float hs_pixs = horizontalSpacing * (density / 160);
		// float margin_pixs = margin * (density / 160);
		// float divideWidth = (width - ((hs_pixs * 9) + (margin_pixs * 2))) /
		// 5;
		// if(Utils.debug){
		// Log.i(TAG, "width的值：" + width + "*****density的值:" + density
		// + "*****hs_pixs的值:" + hs_pixs + "*****margin_pixs的值："
		// + margin_pixs + "*****divideWidth的值:" + divideWidth);
		// }
		// return divideWidth;
		// }
		//
		// public float toScaleZoomHeight() {
		// int imageWidth = 85;
		// int imageHeight = 85;
		// float scaleHeight = (imageHeight * toScaleZoomWidth(15, 5))
		// / imageWidth;
		//
		// return scaleHeight;
		// }
	}

	/**
	 * 拼联系人的链接（例如：XXX进入聊天室的连接）
	 */
	private SpannableString linkUser(String str, String userName,
			final long uid, final TXMessage txmsg) {
		SpannableString sp = new SpannableString(str);

		String name1 = userName + "(" + uid + ")";

		int index = str.indexOf(name1);
		if (index == -1)
			return sp;

		// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
		TX tx = TX.tm.getTx(uid);

		if (tx == null) {
			// SocketHelper.getSocketHelper(txdata).sendGetUserInfor(uid);
			tx = new TX();
			tx.partner_id = txmsg.tcard_id;
			tx.setNick_name(txmsg.tcard_name);
			tx.avatar_url = txmsg.avatar_url;
			tx.setSex(txmsg.tcard_sex);// 添加一个性别 ,好像没有用，因为txmsg.tcard_sex也是默认值 1
										// 2013.09.26

			if (Utils.debug) {
				Log.i(TAG, "XXX进入了聊天室，新创建的tx,partner_id=" + tx.partner_id
						+ ",nick_name=" + tx.getNick_name() + ",avatar_url="
						+ tx.avatar_url + ",sex=" + tx.getSex());
			}
			// return sp;
		} else {
			if (Utils.debug) {
				Log.i(TAG, "XXX进入了聊天室，从数据取出tx不为空，tx:" + tx.toString());
			}
		}

		sp.setSpan(new ForegroundColorSpan(Color.BLUE), index,
				index + name1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		final Intent iSupplement = new Intent(this,
				UserInformationActivity.class);
		iSupplement.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		iSupplement
				.putExtra(
						UserInformationActivity.pblicInfo,
						TX.tm.isTxFriend(uid) == true ? UserInformationActivity.TUIXIN_USER_INFO
								: UserInformationActivity.NOT_TUIXIN_USER_INFO);
		iSupplement.putExtra(UserInformationActivity.UID, tx.partner_id);
		sp.setSpan(new IntentSpan(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (waitDouble == true) {
					waitDouble = false;
					Thread thread = new Thread() {
						@Override
						public void run() {
							try {
								sleep(300);
								if (waitDouble == false) {
									waitDouble = true;
									// 单击事件处理

									// inTxInfoActivity(uid);
									thisContext.startActivity(iSupplement);
									turn = true;
									if (TxGroup.isPublicGroup(txGroup)) {
										Utils.isNotificationShow = false;
									} else {
										Utils.isNotificationShow = true;
									}

								}
							} catch (InterruptedException e) {
								if (Utils.debug) {
									Log.w(TAG, "InterruptedException线程睡眠被打断异常",
											e);
								}
							}

						}
					};
					thread.start();
				} else {
					waitDouble = true;
					// 双击事件处理

					if (msgEdit != null) {
						String tempStr = null;
						if (!Utils.isNull(txmsg.partner_name)) {
							tempStr = msgEdit.getText().toString() + "@"
									+ txmsg.partner_name + " ";
						} else {
							tempStr = msgEdit.getText().toString() + "@"
									+ txmsg.tcard_name + " ";
						}
						msgEdit.setText(smileyParser.addSmileySpans(tempStr,
								true, 0));
						msgEdit.setSelection(tempStr.length());
						Utils.isSendText = true;
						changeType(false, false);
					}

				}

			}

		}), index, index + name1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return sp;
	}

	@Override
	protected void sendGifMsg(int num, int pkgid, String emomd5,
			String pkg_emomd5) {
		// TODO Auto-generated method stub
		
	}

	
	

}
