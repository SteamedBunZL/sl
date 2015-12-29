package com.tuixin11sms.tx.sms;

import java.util.Calendar;
import java.util.List;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.Vibrator;
import android.util.Log;

import com.shenliao.group.activity.GroupNewsActivity;
import com.shenliao.group.activity.GroupSmallGuard;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.FriendManagerActivity;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.activity.SettingsPreference;
import com.tuixin11sms.tx.activity.SingleMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class NotificationPopupWindow {
	private static final String TAG = "NotificationPopupWindow";
	private static TX tx11manger;
	private static TX tx11friend;
	private static TX slGroupNotice;
	private static TX slSafe;
	public static SessionManager mSess;
	private static NotificationPopupWindow instance;
	// public static TxData txData;

	public static String RECEIVE = "MmsSmsHandler_RECEIVE";
	public static String LAST_SYSTEM_TIME = "system_lastTime";
	public static String FORCE_CLEAR = "force_clear";

	public static String TO_HOME = "toHome";
	public static String RECEIVER_MESSAGE = "com.tuixin11.receiverMessage";
	public static String RECEIVER_MESSAGE_OBJ = "receiverMessageObj";
	public static String RECEIVER_MESSAGE_TYPE = "receiverMessageType";
	public static String RECEIVER_MESSAGE_ISOFFLINE = "receiverMessageIsoffiline";

	public static final String TUIXIN_LASTTIME_SETTING = "com.tuixin11.lasttime";

	public static NotificationPopupWindow getInstance() {
		if (instance == null) {
			instance = new NotificationPopupWindow();
		}
		// NotificationPopupWindow.mContext = context;
		return instance;
	}

	public void showNotificationPop(TXMessage txMsg, int messageType,
			boolean isOffline) {
		TXMessage item = txMsg;
		SharedPreferences pres = mSess.getContext().getSharedPreferences(
				TUIXIN_LASTTIME_SETTING, Context.MODE_PRIVATE);
		SharedPreferences tuixin_setting = mSess.getContext()
				.getSharedPreferences(SettingsPreference.TUIXIN_SETTING,
						Context.MODE_PRIVATE);
		if (txMsg.group_id > 0) {
			// 群界面
			if (Utils.xf_group_id == item.group_id) {
				return;
			}
			// 处理按返回键退出公共聊天室那一刻，通知栏会收到公共聊天室的消息通知 2014.01.23
			TxGroup tempGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
					(int) txMsg.group_id);
			if (Utils.roomid == -1
					&& (tempGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL || tempGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC)) {
				// 如果已经退出聊天室，并且是公共聊天室的消息也不提示
				return;
			}
		} else {
			// 单聊
			if (Utils.isIdValid(item.partner_id)
					&& Utils.xf_id.equals("" + item.partner_id)) {
				return;
			}
		}
		if (TxData.isExists(item.msg_id))
			return;

		boolean timecheck = tuixin_setting.getBoolean(mSess.getResources()
				.getString(R.string.pref_key_stranger_title), false);
		boolean isTimeCheck = false;

		Calendar calendar = Calendar.getInstance();
		int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
		if (timecheck) {
			if (timeOfDay > 22 || timeOfDay < 8) {
				isTimeCheck = true;
			}

		}

		// 是否震动提醒
		boolean zd = tuixin_setting
				.getBoolean(
						mSess.getContext().getString(
								R.string.pref_key_notify_title_zd), false);
		if (!isTimeCheck && zd) {
			Vibrator vibrator = (Vibrator) mSess.getContext().getSystemService(
					Service.VIBRATOR_SERVICE);
			long[] pattern = { 300, 1000 }; // OFF/ON/OFF/ON...
			vibrator.vibrate(pattern, -1);
		}

		// 加入信息
		if (Utils.debug)
			Log.e(TAG, "GroupId : " + txMsg.group_id);
		TxData.addUnreadMessage(item);
		if (Utils.isNotificationShow) {
			if (txMsg.group_id > 0) {
				// CommonData.GROUP_NOTIC这个值只有使用没有写入，那么这个地方获取的应该只是true,2014.01.20
				// boolean showNotic =
				// tuixin_setting.getBoolean(CommonData.GROUP_NOTIC+
				// txMsg.group_id, true);
				// if(showNotic){
				if (!isOffline) {
					showNotification(true, true);
				} else {
					showNotification(true, false);
				}
				// }
			} else {
				if (!isOffline) {
					showNotification(true, true);
				} else {
					showNotification(true, false);
				}
			}

		}

	}

	static TX currentTx = null;
	static SharedPreferences pre = null;

	private static TXMessage lastUnreadTxMessage() {
		tx11manger = null;
		tx11friend = null;
		slGroupNotice = null;
		slSafe = null;
		TXMessage txMsg = TxData.getNewUnreadMessage();
		if (txMsg != null) {
			TX tx = TX.tm.getTx(txMsg.partner_id);
			// 神聊管家
			if (tx.partner_id == TX.TUIXIN_MAN) {
				tx11manger = tx;
				// txMsg.sms_address = String.valueOf(tx11manger.partner_id);
				txMsg.contacts_person_name = tx11manger.getNick_name();
			} else if (tx.partner_id == TX.TUIXIN_FRIEND) {
				tx11friend = tx;
				// txMsg.sms_address = String.valueOf(tx11friend.partner_id);
				txMsg.contacts_person_name = tx11friend.getNick_name();
			} else if (tx.partner_id == TX.SL_GROUP_NOTICE) {
				slGroupNotice = tx;
				// txMsg.sms_address = String.valueOf(slGroupNotice.partner_id);
				txMsg.contacts_person_name = slGroupNotice.getNick_name();
			} else if (tx.partner_id == TX.SL_SAFE) {
				slSafe = tx;
				// txMsg.sms_address = String.valueOf(slSafe.partner_id);
				txMsg.contacts_person_name = slSafe.getNick_name();
			} else {
				currentTx = tx;
				tx11manger = null;
				tx11friend = null;
			}
			switch (txMsg.msg_type) {

			case TxDB.MSG_TYPE_SMS_GEO:
			case TxDB.MSG_TYPE_GEO_SMS:
			case TxDB.MSG_TYPE_QU_GEO_SMS:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_loaction);
				break;
			case TxDB.MSG_TYPE_IMAGE_EMS:
			case TxDB.MSG_TYPE_QU_IMAGE_EMS:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_pic);
				break;
			case TxDB.MSG_TYPE_SMS_AUDIO:
			case TxDB.MSG_TYPE_AUDIO_EMS:
			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_audio);
				break;
			case TxDB.MSG_TYPE_BIG_FILE_SMS:
			case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_big_file);
				break;
			case TxDB.MSG_TYPE_QU_GIF_SMS:
			case TxDB.MSG_TYPE_SMS_GIF:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_gif);
				break;
			case TxDB.MSG_TYPE_CARD_EMS:
			case TxDB.MSG_TYPE_TCARD_SMS:
			case TxDB.MSG_TYPE_SMS_CRAD:
			case TxDB.MSG_TYPE_QU_CARD_EMS:
			case TxDB.MSG_TYPE_QU_TCARD_SMS:
				txMsg.msg_body = mSess.getResources().getString(
						R.string.notifi_popup_card);
				break;
			case TxDB.MSG_TYPE_SNS_SMS:
				break;
			// 群组动态
			case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN:
				TxGroup txGroupAdmin = TxGroup
						.getTxGroup(mSess.getContentResolver(),
								(int) txMsg.group_id_notice);
				if (txGroupAdmin != null) {
					String content = Utils.isNull(txGroupAdmin.group_title) ? ""
							+ txGroupAdmin.group_id
							: txGroupAdmin.group_title;
					txMsg.msg_body = txMsg.msg_body.replace("�slgroup�",
							content);
				} else {
					txMsg.msg_body = txMsg.msg_body.replace("�slgroup�", ""
							+ txMsg.group_id_notice);
				}
				// 修改消息会话的内容,为了联系人页面我的群组的显示
				MsgStat mst = MsgStat.getMsgStatByPartnerId(TX.SL_GROUP_NOTICE);
				if (mst != null) {
					mst.setMsgBody(txMsg.msg_body);
					MsgStat.saveMsgStatToDB(mst, mSess.getContentResolver());
					// MsgStat.updateMsgStatByTxmsg(txMsg,
					// txData.getContentResolver(), TxDB.MS_TYPE_TB, txMsg.gmid,
					// -1,false);
				}
				break;
			case TxDB.MSG_TYPE_SET_GROUP_ADMIN:
			case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER:
			case TxDB.MSG_TYPE_DISMISS_GROUP:
			case TxDB.MSG_TYPE_LEAVE:
			case TxDB.MSG_TYPE_IN:
				TxGroup txGroup = TxGroup
						.getTxGroup(mSess.getContentResolver(),
								(int) txMsg.group_id_notice);
				if (txGroup != null) {
					String content = Utils.isNull(txGroup.group_title) ? ""
							+ txGroup.group_id : txGroup.group_title;
					txMsg.msg_body = txMsg.msg_body.replace("�slgroup�",
							content);
				} else {
					txMsg.msg_body = txMsg.msg_body.replace("�slgroup�", ""
							+ txMsg.group_id_notice);
				}
				// 修改消息会话的内容,为了联系人页面我的群组的显示
				MsgStat mstt = MsgStat
						.getMsgStatByPartnerId(TX.SL_GROUP_NOTICE);
				if (mstt != null) {
					mstt.setMsgBody(txMsg.msg_body);
					MsgStat.saveMsgStatToDB(mstt, mSess.getContentResolver());
					// MsgStat.updateMsgStatByTxmsg(txMsg,
					// txData.getContentResolver(), TxDB.MS_TYPE_TB, txMsg.gmid,
					// -1,false);
				}
				break;
			case TxDB.MSG_TYPE_DISMISS_4_CREATOR:
				txMsg.msg_body = txMsg.msg_body;

				break;

			}
		}

		return txMsg;
	}

	private static void updateNotification(boolean isNewContent,
			boolean showSound) {
		if (Utils.isNotificationShow) {
			SharedPreferences tuixin_setting = mSess.getContext()
					.getSharedPreferences(SettingsPreference.TUIXIN_SETTING,
							Context.MODE_PRIVATE);
			// 是否显示通知
			boolean showTitle = tuixin_setting.getBoolean(mSess.getContext()
					.getString(R.string.pref_key_notify_title_show), true);

			if (showTitle) {
				NotificationManager mNotificationManager = (NotificationManager) mSess
						.getContext().getSystemService(
								Context.NOTIFICATION_SERVICE);
				// 这里的audio_times只是用做记录未读数字
				if (TxData.getUnreadSize() > 0) {
					TXMessage msg = NotificationPopupWindow
							.lastUnreadTxMessage();
					Notification notification = new Notification();
					notification.icon = R.drawable.notify_icon;

					// 是否直接在状态栏显示短息内容
					boolean showMags = tuixin_setting.getBoolean(
							mSess.getContext().getString(
									R.string.pref_key_notify_show_mags), false);
					String str = "";
					if (msg.msg_type == TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN) {
						str = msg.reportName + "举报" + msg.partner_name;
					} else {
						str = msg.msg_body;
					}

					if (isNewContent) {
						if (!showMags) {
							if (msg.msg_type == TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN) {
								notification.tickerText = msg.nick_name + ":"
										+ str;
							} else {
								notification.tickerText = msg.nick_name + ":"
										+ str;
							}

						} else {
							notification.tickerText = mSess.getContext()
									.getString(R.string.pref_notify_newcontent);
						}

					}
					/*
					 * if ((msg.send_time + "").length() < 10) {
					 * notification.when = msg.send_time * 1000; } else {
					 * notification.when = msg.send_time; }
					 */

					// 设置跳转目的地
					Intent openintent = new Intent(mSess.getContext(),
							SingleMsgRoom.class);
					String displayName = "";
					if (tx11manger == null && tx11friend == null
							&& slGroupNotice == null && slSafe == null) {
						if (Utils.isIdValid(msg.group_id)) {
							openintent = new Intent(mSess.getContext(),
									GroupMsgRoom.class);
							// TxGroup txGroup =
							// txData.getTxGroupByGroupId4DB(msg.group_id);
							TxGroup txGroup = TxGroup.getTxGroup(
									mSess.getContentResolver(), msg.group_id);
							openintent
									.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
							// displayName =
							// currentTx.getContacts_person_name();
						} else if (Utils.isIdValid(msg.partner_id)) {
							openintent.putExtra(Utils.MSGROOM_TX,
									currentTx.partner_id);
							// displayName =
							// currentTx.getContacts_person_name();
						}
						displayName = currentTx.getTxInfor()
								.getContacts_person_name();
						/*
						 * else { TX t = txData.findTxByPersonIDAndPhone(
						 * msg.contacts_person_id, msg.sms_address);
						 * openintent.putExtra("threadId", (int) MmsUtils
						 * .getOrCreateThreadId(mContext, new String[] {
						 * msg.sms_address })); openintent.putExtra("tx", t);
						 * displayName = t.nick_name; }
						 */
					} else if (tx11manger != null) {
						openintent.putExtra(Utils.MSGROOM_TX,
								tx11manger.partner_id);
						displayName = tx11manger.getNick_name();
					} else if (tx11friend != null) {
						// 好友管家特殊处理
						openintent = new Intent(mSess.getContext(),
								FriendManagerActivity.class);
						openintent.putExtra(TO_HOME, true);
						// openintent.putExtra("tx", tx11friend);
						// displayName = tx11friend.nick_name;
					} else if (slGroupNotice != null) {
						openintent = new Intent(mSess.getContext(),
								GroupNewsActivity.class);
					} else if (slSafe != null) {
						openintent = new Intent(mSess.getContext(),
								GroupSmallGuard.class);
					}
					openintent.putExtra("clear_this", msg.msg_id);
					if (TxData.getUnreadSize() == 1) {
						openintent.putExtra(FORCE_CLEAR, true);
					} else {
						openintent.putExtra(FORCE_CLEAR, false);
					}
					// openintent.putExtra(Utils.MSGROOM_WINDOW_IN,
					// true);//注掉原因找MSGROOM_WINDOW_IN的定义处
					openintent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
							| Intent.FLAG_ACTIVITY_CLEAR_TOP);
					// openintent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					PendingIntent contentIntent = PendingIntent.getActivity(
							mSess.getContext(), 0, openintent,
							PendingIntent.FLAG_UPDATE_CURRENT);

					notification.setLatestEventInfo(
							mSess.getContext(),
							!Utils.isNull(msg.nick_name) ? msg.nick_name
									: displayName,
							mSess.getResources().getString(
									R.string.notification_new_msg)
									+ ":" + str, contentIntent);
					boolean timecheck = tuixin_setting.getBoolean(
							mSess.getContext().getString(
									R.string.pref_key_stranger_title), false);
					boolean isTimeCheck = false;

					Calendar calendar = Calendar.getInstance();
					int timeOfDay = calendar.get(Calendar.HOUR_OF_DAY);
					if (timecheck) {
						if (timeOfDay > 22 || timeOfDay < 8) {
							isTimeCheck = true;
						}

					}

					// 是否有声音提示
					boolean sound = tuixin_setting
							.getBoolean(
									mSess.getContext()
											.getString(
													R.string.pref_key_notify_title_sound),
									true);

					if (!isTimeCheck && showSound && sound) {
						notification.defaults |= Notification.DEFAULT_SOUND;
						notification.audioStreamType = AudioManager.STREAM_RING;

					}
					notification.number = TxData.getUnreadSize();
					notification.ledARGB = Color.BLUE;
					notification.ledOffMS = 0;
					notification.ledOnMS = 1;
					notification.flags = notification.flags
							| Notification.FLAG_SHOW_LIGHTS;

					// 保存当前的时间 用于下次读取未读短信数目的时间判断
					Intent delintent = new Intent(RECEIVE);
					PendingIntent deleteIntent = PendingIntent.getBroadcast(
							mSess.getContext(), 0, delintent,
							PendingIntent.FLAG_UPDATE_CURRENT);
					notification.deleteIntent = deleteIntent;
					mNotificationManager.notify(
							Constants.NOTIFICATION_NEW_MESSAGE, notification);
				} else {
					cancelNotification(mSess.getContext());
				}
			}
		}
	}

	/**
	 * 通知栏更新
	 * 
	 * @param oldMsg
	 *            旧TXmessage对象
	 * @param isNewContent
	 *            是否有声音提示 及 新内容文本提示
	 */
	public synchronized static void showNotification(Long id,
			boolean isNewContent) {
		if (id != 0 && !isNewContent) {
			TxData.clearOldMsg(id, false);
			updateNotification(isNewContent, false);
		}

	}

	public synchronized static void showNotification(boolean isNewContent,
			boolean showSound) {
		updateNotification(isNewContent, showSound);
	}

	public synchronized static void showSpecialNotification(Long msgId) {
		if (msgId != 0) {
			TxData.clearOldMsg(msgId, true);
			// updateNotification(false, false);
		}

	}

	public synchronized static void showNotification(List<Long> oldMsgs,
			boolean isNewContent) {
		if (oldMsgs != null && oldMsgs.size() != 0 && !isNewContent) {
			TxData.clearOldMsg(oldMsgs);
			updateNotification(isNewContent, false);
		}
	}

	public synchronized static void showGroupNotification(Long groupId,
			boolean isNewContent) {
		if (groupId > 0 && !isNewContent) {
			TxData.clearGroupMsg(groupId);
			updateNotification(isNewContent, false);
		}
	}

	public synchronized static void showPersonNotification(Long partnerId,
			boolean isNewContent) {
		if (partnerId > 0 && !isNewContent) {
			TxData.clearPersonMsg(partnerId);
			updateNotification(isNewContent, false);
		}
	}

	public static void cancelNotification(Context context) {
		if (TxData.unreadMessageList != null) {
			TxData.unreadMessageList.clear();
		}

		if (TxData.messageIds != null) {
			TxData.messageIds.clear();
		}

		if (context != null) {
			NotificationManager mNotificationManager = (NotificationManager) context
					.getSystemService(Context.NOTIFICATION_SERVICE);
			if (mNotificationManager != null) {
				mNotificationManager.cancel(Constants.NOTIFICATION_NEW_MESSAGE);
			}

		}

	}

}
