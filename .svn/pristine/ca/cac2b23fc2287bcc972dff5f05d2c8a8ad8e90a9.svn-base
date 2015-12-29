package com.tuixin11sms.tx.message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class MsgStat implements Parcelable {
	private static final String TAG = MsgStat.class.getSimpleName();
	private final static Object lock = new Object();
	// public static final int SELECT_COUNT = 10;//无引用，注掉 2014.01.23 shc
	private int _id;
	public int ms_type;// 对话类型
	public int msg_type;// 消息类型
	public long msg_id;// 消息id
	// msg_session_id为2013.10.18新添加字段 shc
	private long msg_session_id;// 消息的sessionId由消息类型和发信息人的id组合生成，来避免同一个人发送的单聊和群聊信息
	public long msg_date;// 消息时间，单位应该是秒
	public int message_count;// 消息总数
	public String contacts_person_name;// 通讯录名称，或者多人群发短信名称
	public long partner_id;// sender_partner_id
	public String partner_display_name;// 神聊名称或者群发送者名称
	public long group_id;
	public long group_id_notice;
	// gmid含义解释:string,全局消息id,由服务器生成,同一聊天室内唯一,实际上是64位无符号整数（因json标准没明确支持64位,所以用字符串类型）
	public long gmid;// 应该是GroupManagerId,服务器用来标记该群消息在这个这个群中所有消息的位置 2014.01.21
	public String group_name;
	public String group_display_avatars;// 群头像
	public boolean wasme;
	public String msg_body;// 消息正文
	public int no_read;// 未读数
	public int read_state;// 最后一条消息状态 //0未发 1已发 2未读 3已读
	public boolean del;

	// 2013/07/08添加字段
	private CharSequence displayName;// 会话条目上显示的名字
	private String msgSendTime;// 消息时间，格式化之后的,在MessageActivity中使用
	private String msgSendState;// 消息发送状态，功能类似read_state
	private CharSequence msgDisplayBody;// 显示需要显示的消息体，原消息体msg_body尽量不要污染，里面有特殊字段需要被替换等
	private boolean hasSaveCache = false;// 该会话信息是否做了缓存

	private static HashMap<Long, MsgStat> msgstats = new HashMap<Long, MsgStat>();// 存放消息会话的集合，key为MsgStat根据类型生成的惟一的key

	public long getSessionId() {
		long sid = ms_type;
		switch (ms_type) {
		case TxDB.MS_TYPE_CS:
			sid |= 2 << 8; // 很老的用户有问题
			break;
		case TxDB.MS_TYPE_MORE_CS:
			sid |= 3 << 8; // 很老的用户有问题
			break;
		case TxDB.MS_TYPE_QU:
			sid |= group_id << 8;
			break;
		case TxDB.MS_TYPE_TB:
			sid |= partner_id << 8;
			break;
		case TxDB.MS_TYPE_NOTICE:
			sid |= partner_id << 8;
			break;
		default:
			sid |= 1 << 8;
			break;
		}

		msg_session_id = sid;
		return sid;
	}

	/**
	 * 取得MsgStats这个hashmap的key,删除对应的消息会话MsgStat
	 */
	public static long getMsgStatsSessionId(int ms_type, long id) {
		long sid = ms_type;
		switch (ms_type) {
		case TxDB.MS_TYPE_QU:
		case TxDB.MS_TYPE_TB:
		case TxDB.MS_TYPE_NOTICE:
			sid |= id << 8;
			break;
		case TxDB.MS_TYPE_MORE_CS:
			sid |= 3 << 8; // 很老的用户有问题
			break;
		case TxDB.MS_TYPE_CS:
			sid |= 2 << 8; // 很老的用户有问题
			break;
		default:
			sid |= 1 << 8;
			break;
		}
		return sid;
	}

	public MsgStat() {
		this.msg_type = -1;
		this.msg_id = -1;
		this.msg_date = 0;
		this.message_count = 0;
		this.contacts_person_name = "";
		this.partner_id = -1;
		this.partner_display_name = "";
		this.group_id = -1;
		this.group_id_notice = -1;
		this.group_name = "";
		this.group_display_avatars = "";
		this.wasme = false;
		this.msg_body = "";
		this.no_read = 0;
		this.read_state = -1;
		this.group_id = 0;
	}

	public CharSequence getDisplayName() {
		return displayName;
	}

	public void setDisplayName(CharSequence displayName) {
		this.displayName = displayName;
	}

	public String getMsgSendTime() {
		return msgSendTime;
	}

	public void setMsgSendTime(String msgSendTime) {
		this.msgSendTime = msgSendTime;
	}

	public String getMsgSendState() {
		return msgSendState;
	}

	public void setMsgSendState(String msgSendState) {
		this.msgSendState = msgSendState;
	}

	public CharSequence getMsgDisplayBody() {
		return msgDisplayBody;
	}

	public void setMsgDisplayBody(CharSequence msgDisplayBody) {
		this.msgDisplayBody = msgDisplayBody;
	}

	public boolean isHasSaveCache() {
		return hasSaveCache;
	}

	public void setHasSaveCache(boolean hasSaveCache) {
		this.hasSaveCache = hasSaveCache;
	}

	public long getGmid() {
		return gmid;
	}

	public void setGmid(long gmid) {
		this.gmid = gmid;
	}

	public void setMsType(int ms_type) {
		if (ms_type > -1)
			this.ms_type = ms_type;
	}

	public int getMsType() {
		return ms_type;
	}

	public void setMsgType(int msg_type) {
		this.msg_type = msg_type;
	}

	public int getMsgType() {
		return msg_type;
	}

	public void setMsgId(long msg_id) {
		if (msg_id > 0)
			this.msg_id = msg_id;
	}

	public long getMsgId() {
		return msg_id;
	}

	public void setMsgDate(long msg_date) {
		// 消息会话时间的单位是秒
		if (("" + msg_date).length() == 13) {
			// 如果时间单位是毫秒，则变为秒
			msg_date = msg_date / 1000;
		}
		this.msg_date = msg_date;
	}

	public long getMsgDate() {
		return msg_date;
	}

	public void setMsgCount(int message_count) {
		this.message_count = message_count;
	}

	public int getMsgCount() {
		return message_count;
	}

	public void setContactPersonName(String contacts_person_name) {
		if (!Utils.isNull(contacts_person_name))
			this.contacts_person_name = contacts_person_name;
	}

	public String getContactPersonName() {
		return contacts_person_name;
	}

	public void setPartnerID(long partner_id) {
		if (Utils.isIdValid(partner_id))
			this.partner_id = partner_id;
	}

	public long getPartnerID() {
		return partner_id;
	}

	public void setPartnerName(String partner_display_name) {
		if (!Utils.isNull(partner_display_name))
			this.partner_display_name = partner_display_name;
	}

	public String getPartnerName() {
		return partner_display_name;
	}

	public void setGroupID(long group_id) {
		if (Utils.isIdValid(group_id))
			this.group_id = group_id;
	}

	public long getGroupID() {
		return group_id;
	}

	public void setGroupName(String group_name) {
		if (!Utils.isNull(group_name))
			this.group_name = group_name;
	}

	public String getGroupName() {
		return group_name;
	}

	public void setGroupAvatar(String group_display_avatars) {
		if (!Utils.isNull(group_display_avatars))
			this.group_display_avatars = group_display_avatars;
	}

	public String getGroupAvatar() {
		return group_display_avatars;
	}

	public void setNoRead(int no_read) {
		this.no_read = no_read;
	}

	public int getNoRead() {
		return no_read;
	}

	public void setReadState(int read_state) {
		this.read_state = read_state;
	}

	public int getReadState() {
		return read_state;
	}

	public void setMsgBody(String msg_body) {
		this.msg_body = msg_body;
	}

	public String getMsgBody() {
		return msg_body;
	}

	public boolean wasMe() {
		return wasme;
	}

	public void setWasMe(Boolean wasme) {
		this.wasme = wasme;
	}

	public static MsgStat fetchOneMsg(Cursor c) {
		MsgStat chat = new MsgStat();
		chat._id = c.getInt(c.getColumnIndex(TxDB.MsgStat._ID));
		chat.msg_type = c.getInt(c.getColumnIndex(TxDB.MsgStat.MSG_TYPE));
		chat.ms_type = c.getInt(c.getColumnIndex(TxDB.MsgStat.MSG_STAT_TYPE));
		chat.msg_id = c.getLong(c.getColumnIndex(TxDB.MsgStat.MSG_ID));
		chat.msg_date = c.getLong(c.getColumnIndex(TxDB.MsgStat.MSG_DATE));
		chat.contacts_person_name = c.getString(c
				.getColumnIndex(TxDB.MsgStat.CONTACTS_PERSON_NAME));
		chat.message_count = c.getInt(c.getColumnIndex(TxDB.MsgStat.MSG_COUT));
		chat.partner_id = c.getLong(c
				.getColumnIndex(TxDB.MsgStat.MSG_PARTNER_ID));
		chat.partner_display_name = c.getString(c
				.getColumnIndex(TxDB.MsgStat.MSG_DISPLAY_NAME));
		chat.group_id = c.getLong(c.getColumnIndex(TxDB.MsgStat.MSG_GROUP_ID));
		chat.group_name = c.getString(c
				.getColumnIndex(TxDB.MsgStat.MSG_GROUP_NAME));
		chat.group_display_avatars = c.getString(c
				.getColumnIndex(TxDB.MsgStat.MSG_GROUP_DISPLAY_AVATARS));
		chat.msg_body = c.getString(c.getColumnIndex(TxDB.MsgStat.MSG_BODY));
		chat.no_read = c
				.getInt(c.getColumnIndex(TxDB.MsgStat.MSG_NOTREAD_COUT));
		chat.read_state = c.getInt(c.getColumnIndex(TxDB.MsgStat.READ_STATE));
		chat.wasme = c.getString(c.getColumnIndex(TxDB.MsgStat.WASME)).equals(
				"1");
		chat.group_id_notice = c.getLong(c
				.getColumnIndex(TxDB.MsgStat.GROUP_ID_NOTICE));
		chat.gmid = c.getLong(c.getColumnIndex(TxDB.MsgStat.GMID));
		// 类型直接从数据库中取，不再临时判断 2014.05.09 shc
		// if (Utils.isIdValid(chat.group_id)) {
		// chat.ms_type = TxDB.MS_TYPE_QU;
		// } else {
		// if(chat.partner_id == PraiseNoticeImpl.PRAISE_NOTICE_ID){
		// //点赞通知消息
		// chat.ms_type = TxDB.MS_TYPE_NOTICE;
		// }else {
		// chat.ms_type = TxDB.MS_TYPE_TB;
		// }
		// }

		return chat;
	}

	public static ArrayList<MsgStat> fetchAllDBMsgs(Cursor c) {
		ArrayList<MsgStat> ret = new ArrayList<MsgStat>();
		while (c.moveToNext()) {
			ret.add(fetchOneMsg(c));
		}
		return ret;
	}
	public static Intent intent = new Intent();
	public static void sendConstactsBroadCast(){
		if (IsConstactHasUnReadMessage()) {
			intent.setAction(Constants.CONSTACTS_RED_SHOW);
			TxData.context.sendBroadcast(intent);
		}else {
			intent.setAction(Constants.CONSTACTS_RED_UN_SHOW);
			TxData.context.sendBroadcast(intent);
		}
	}
	public static Uri saveMsgStatToDB(MsgStat mst, ContentResolver cr) {
		mst.setHasSaveCache(false);
		synchronized (lock) {
			if (mst.ms_type >= 30)
				return null;
			ContentValues values = new ContentValues();
			int msgCount = mst.message_count;
			if (Utils.debug) {
				Log.i(TAG, "msg_id =====" + mst.msg_id);
				Log.i(TAG, "mst.group_id =====" + mst.group_id);
				Log.i(TAG, "mst.group_name =====" + mst.group_name);
			}
			Uri aMsgUri = null;
			boolean find = false;

			Cursor c = cr.query(TxDB.MsgStat.CONTENT_URI,
					new String[] { TxDB.MsgStat._ID },
					TxDB.MsgStat.MSG_SESSION_ID + "=?",
					new String[] { "" + mst.getSessionId() }, null);
			if (c != null) {
				if (c.moveToFirst()) {
					int _id = c.getInt(0);
					mst._id = _id;
					if (Utils.debug)
						Log.i(TAG, "===find==" + mst._id);
					find = true;
				} else {
					if (Utils.debug)
						Log.i(TAG, "===木有找到==" + mst._id);
				}
				c.close();
			}

			values.put(TxDB.MsgStat.MSG_TYPE, mst.msg_type);
			values.put(TxDB.MsgStat.MSG_ID, mst.msg_id);
			values.put(TxDB.MsgStat.MSG_DATE, mst.msg_date);
			values.put(TxDB.MsgStat.CONTACTS_PERSON_NAME,
					mst.contacts_person_name);
			values.put(TxDB.MsgStat.MSG_PARTNER_ID, mst.partner_id);
			values.put(TxDB.MsgStat.MSG_DISPLAY_NAME, mst.partner_display_name);
			values.put(TxDB.MsgStat.MSG_GROUP_ID, mst.group_id);
			values.put(TxDB.MsgStat.MSG_GROUP_NAME, mst.group_name);
			values.put(TxDB.MsgStat.MSG_GROUP_DISPLAY_AVATARS,
					mst.group_display_avatars);
			values.put(TxDB.MsgStat.MSG_BODY, mst.msg_body);
			values.put(TxDB.MsgStat.MSG_COUT, msgCount);
			values.put(TxDB.MsgStat.MSG_NOTREAD_COUT, mst.no_read);
			values.put(TxDB.MsgStat.READ_STATE, mst.read_state);
			values.put(TxDB.MsgStat.WASME, mst.wasme);
			values.put(TxDB.MsgStat.GROUP_ID_NOTICE, mst.group_id_notice);
			values.put(TxDB.MsgStat.GMID, mst.gmid);
			// 3.8.5新添加的字段
			values.put(TxDB.MsgStat.MSG_SESSION_ID, mst.getSessionId());
			values.put(TxDB.MsgStat.MSG_STAT_TYPE, mst.ms_type);
			if (find) {
				try {
					cr.update(TxDB.MsgStat.CONTENT_URI, values,
							TxDB.MsgStat._ID + "=?", new String[] { ""
									+ mst._id });
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "消息会话数据库更新异常：", e);
				}
			} else {
				if (Utils.debug)
					Log.i(TAG, "数据库插入,mst._id=" + mst._id + ",mst.toString():"
							+ mst.toString());
				try {
					aMsgUri = cr.insert(TxDB.MsgStat.CONTENT_URI, values);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "消息会话数据库插入新数据异常：", e);
				}
			}

			return aMsgUri;
		}
	}

	@Override
	public String toString() {
		return "MsgStat [_id=" + _id + ", ms_type=" + ms_type + ", msg_type="
				+ msg_type + ", msg_id=" + msg_id + ", msg_session_id="
				+ msg_session_id + ", msg_date=" + msg_date
				+ ", message_count=" + message_count
				+ ", contacts_person_name=" + contacts_person_name
				+ ", partner_id=" + partner_id + ", partner_display_name="
				+ partner_display_name + ", group_id=" + group_id
				+ ", group_id_notice=" + group_id_notice + ", gmid=" + gmid
				+ ", group_name=" + group_name + ", group_display_avatars="
				+ group_display_avatars + ", wasme=" + wasme + ", msg_body="
				+ msg_body + ", no_read=" + no_read + ", read_state="
				+ read_state + ", del=" + del + ", displayName=" + displayName
				+ ", msgSendTime=" + msgSendTime + ", msgSendState="
				+ msgSendState + ", msgDisplayBody=" + msgDisplayBody
				+ ", hasSaveCache=" + hasSaveCache + "]";
	}

	public static final Parcelable.Creator<MsgStat> CREATOR = new Parcelable.Creator<MsgStat>() {
		public MsgStat createFromParcel(Parcel in) {
			return new MsgStat(in);
		}

		public MsgStat[] newArray(int size) {
			return new MsgStat[size];
		}
	};

	private MsgStat(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		//
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(ms_type);
		out.writeInt(msg_type);
		out.writeLong(msg_id);
		out.writeLong(msg_date);
		out.writeInt(message_count);
		out.writeString(contacts_person_name);
		out.writeLong(partner_id);
		out.writeString(partner_display_name);
		out.writeLong(group_id);
		out.writeString(group_name);
		out.writeString(group_display_avatars);
		out.writeString(msg_body);
		out.writeInt(no_read);
		out.writeInt(read_state);
		boolean[] bool = new boolean[1];
		bool[0] = wasme;
		out.writeBooleanArray(bool);

		out.writeLong(group_id_notice);
		out.writeLong(gmid);
	}

	public void readFromParcel(Parcel in) {
		ms_type = in.readInt();
		msg_type = in.readInt();
		msg_id = in.readLong();
		msg_date = in.readLong();
		message_count = in.readInt();
		contacts_person_name = in.readString();
		partner_id = in.readLong();
		partner_display_name = in.readString();
		group_id = in.readLong();
		group_name = in.readString();
		group_display_avatars = in.readString();
		msg_body = in.readString();
		no_read = in.readInt();
		read_state = in.readInt();
		boolean[] bool = new boolean[1];
		in.readBooleanArray(bool);
		wasme = bool[0];

		group_id_notice = in.readInt();
		gmid = in.readLong();
	}

	/** 通过TXMessage找到缓存中的MsgStat，并更新 */
	// 最后一个参数unReadCount=-1，代表让未读消息数自增
	// TODO isRoom这个参数是存到数据库时用的，待删除
	public static MsgStat updateMsgStatByTxmsg(TXMessage txmsg,
			ContentResolver cr, int ms_type, long gmid, int unReadCount,
			boolean isRoom) {
		if (txmsg == null)
			return null;

		// 通过txmsg取得缓存中的msgStat
		MsgStat msgStat = null;
		if (Utils.isIdValid(txmsg.group_id)) {
			// 消息TXMessage中group_id有效，代表是群消息
			msgStat = getMsgStatByGroupid(txmsg.group_id);
		} else {
			// 代表TXMessage是个人会话消息
			if (Utils.isIdValid(txmsg.partner_id)) {
				msgStat = getMsgStatByPartnerId(txmsg.partner_id);
			}
		}

		// 用TXMessage中的值更新MsgStat对应的值

		if (msgStat == null)
			msgStat = new MsgStat();
		if (Utils.debug)
			Log.i(TAG, "缓存中找到的msgStat:" + msgStat.toString());
		msgStat.setContactPersonName(txmsg.contacts_person_name);
		msgStat.setMsgId(txmsg.msg_id);
		msgStat.setMsgDate(txmsg.send_time);
		msgStat.setMsgBody(txmsg.msg_body);
		msgStat.setReadState(txmsg.read_state);
		msgStat.setPartnerID(txmsg.partner_id);
		msgStat.setPartnerName(txmsg.partner_name);
		// msgStat.gmid = txmsg.gmid;//TODO
		// 这里如果设置了，下面保存群信息时判断gmid会有问题，但是这么处理不对，应该是更新MsgStat操作都放在这里方法里面。保存是下面方法的事，分清。
		if (Utils.isIdValid(txmsg.group_id)) {
			msgStat.group_id = txmsg.group_id;
			if (!Utils.isNull(txmsg.group_name)
					&& !txmsg.group_name.equals("" + txmsg.group_id)) {
				TxGroup ttxgroup = TxGroup.getTxGroup(cr, msgStat.group_id);
				if (ttxgroup != null) {
					msgStat.group_name = ttxgroup.group_title;
				} else {
					msgStat.group_name = txmsg.group_name;
				}
			}
			msgStat.setGroupAvatar(txmsg.group_avatars_url);
		}
		if (Utils.isIdValid(txmsg.group_id_notice)) {
			msgStat.group_id_notice = txmsg.group_id_notice;
			TxGroup ttxgroup = TxGroup.getTxGroup(cr, msgStat.group_id);
			if (ttxgroup != null) {
				msgStat.group_name = ttxgroup.group_title;
			} else {
				msgStat.group_name = txmsg.group_name;
			}
			msgStat.setGroupAvatar(txmsg.group_avatars_url);
		}
		msgStat.setMsgType(txmsg.msg_type);// 消息类型
		msgStat.setWasMe(txmsg.was_me);
		msgStat.setMsType(ms_type);// 会话类型

		addOrUpdateMsgStat(cr, msgStat, gmid, unReadCount, isRoom);

		return msgStat;
	}

	/** 通过通知消息更新会话 */
	public static MsgStat updateMsgStatByNotice(PraiseNotice pn,
			ContentResolver cr) {
		if (pn == null)
			return null;

		synchronized (lock) {
			// 取得缓存中的msgStat
			MsgStat msgStat = getMsgStatByNoticeid(PraiseNoticeImpl.PRAISE_NOTICE_ID);

			if (msgStat == null) {
				msgStat = new MsgStat();
				msgStat.setPartnerID(PraiseNoticeImpl.PRAISE_NOTICE_ID);
				msgStat.setMsType(TxDB.MS_TYPE_NOTICE);// 会话类型
				msgstats.put(msgStat.getSessionId(), msgStat);
			}

			// msgStat.setContactPersonName("");
			// msgStat.setMsgId(-1);
			// msgStat.setMsgBody("");
			// msgStat.setReadState("");
			// msgStat.setPartnerName("");
			// msgStat.setWasMe(false);
			// msgStat.setMsgType(-1);//消息类型
			msgStat.setMsgDate(pn.getTime());
			msgStat.no_read++;
			MsgStat.saveMsgStatToDB(msgStat, cr);
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
			return msgStat;
		}
	}

	public static boolean IsConstactHasUnReadMessage() {
		long friendId = getMsgStatsSessionId(TxDB.MS_TYPE_TB, TX.TUIXIN_FRIEND);
		long groupId = getMsgStatsSessionId(TxDB.MS_TYPE_TB, TX.SL_GROUP_NOTICE);
		MsgStat msgFriend = msgstats.get(friendId);
		MsgStat msgGroup = msgstats.get(groupId);
		if (msgFriend != null) {
			if (msgFriend.getNoRead() > 0) {
				return true;
			}
		}

		if (msgGroup != null) {
			if (msgGroup.getNoRead() > 0) {
				return true;
			}
		}
		return false;

	}
	

	/**
	 * 获取短信、神聊对话集合
	 * 
	 * @param txdata
	 *            Application obj
	 * @return 对话集合
	 */
	public static ArrayList<MsgStat> filterTXList(final ContentResolver cr) {
		ArrayList<MsgStat> txs = new ArrayList<MsgStat>();
		ArrayList<MsgStat> dbtxs = new ArrayList<MsgStat>();
		Cursor cursor_msg = cr.query(TxDB.MsgStat.CONTENT_URI, null, null,
				null, "msg_date desc");
		if (cursor_msg != null) {
			dbtxs = MsgStat.fetchAllDBMsgs(cursor_msg);
			if (dbtxs != null) {
				if (Utils.debug) {
					Log.i(TAG, "所有会话dbTxs.size()=" + dbtxs.size());
				}
				for (int i = 0; i < dbtxs.size(); i++) {
					MsgStat ms = dbtxs.get(i);
					msgstats.put(ms.getSessionId(), ms);// 把msgStat都放在map中
					// 这个判断现在还有用吗？
					if (ms.group_id == TxGroup.ACCOST_ID) {
						dbtxs.remove(i);
						i--;
					}
					if (Utils.debug)
						Log.i(TAG, "filterTXList:ms_type=" + ms.ms_type
								+ ",group_id=" + ms.group_id + ",partner_id="
								+ ms.partner_id);
				}
			}
			cursor_msg.close();
		}
		sendConstactsBroadCast();
		txs.addAll(dbtxs);
		Collections.sort(txs, new MessageComparator());
		return txs;
	}

	// isRoom应该代表是否是聊天室执行的此方法，如果是，那么就清空未读条目数，因为进入聊天室后所有的未读消息应该清零
	// 注释：unReadCount=-1，代表让未读消息数自增1
	public static void addOrUpdateMsgStat(ContentResolver cr, MsgStat ms,
			long gmid, int unReadCount, boolean isRoom) {
		if (ms.ms_type == TxDB.MS_TYPE_CS || ms.ms_type == TxDB.MS_TYPE_MORE_CS) {
			// 如果是通讯录会话类型，则直接不处理，不添加到缓存中
			return;
		}
		synchronized (lock) {
			if (ms.group_id == TxGroup.ACCOST_ID)
				return;
			// boolean add = true;
			if (Utils.debug) {
				Log.i(TAG, "添加或者更新的MsgStat:" + ms.toString());
			}
			MsgStat msgStatCache = msgstats.get(ms.getSessionId());

			if (msgStatCache != null) {
				// 缓存中找到了对应的MsgStat，则【更新】该对象
				switch (msgStatCache.ms_type) {
				case TxDB.MS_TYPE_QU:
					// 群聊天
					if (unReadCount != -1) {
						msgStatCache.no_read = unReadCount;
					} else {
						if (!ms.wasMe()) {
							// 这条群消息不是我发的
							// if (ms.gmid > msgStatCache.gmid) {
							if (gmid > msgStatCache.gmid) {
								// 代表这个群消息比较新，未读数要+1
								msgStatCache.no_read++;
							} else {
								// 如果是来的消息比我最新的 消息老，那么就不处理了？
							}
						}
					}
					if (gmid > msgStatCache.gmid || isRoom) {
						if (gmid > msgStatCache.gmid) {
							msgStatCache.setGmid(gmid);
						}
						TxGroup txGroup = TxGroup.getTxGroup(cr,
								(int) msgStatCache.group_id);

						if (txGroup != null && !TxGroup.isPublicGroup(txGroup)) {
							// 这个地方txGroup不应该为null，如果数据库中没有此群，就不应该收到消息，并且MsgStat的save方法也没有保存txGroup的操作
							// 2014.01.21 shc
							MsgStat.saveMsgStatToDB(msgStatCache, cr);
						}
					}
					break;
				case TxDB.MS_TYPE_TB:
					// 个人聊天
					if (Utils.debug) {
						Log.i(TAG, msgStatCache.partner_id + "--pid--"
								+ ms.partner_id);
					}
					if (unReadCount != -1) {
						msgStatCache.no_read = unReadCount;
					} else {
						if (!ms.wasMe()) {
							// 消息不是我发的，那么未读消息数+1，（因为如果是我发的代表我已经看过消息）
							msgStatCache.no_read++;
						}
					}

					MsgStat.saveMsgStatToDB(msgStatCache, cr);
					break;
				default:
					break;
				}

				if (Utils.debug) {
					Log.w(TAG, "更新会话信息状态，MsgStat：" + ms.toString());
				}
			} else {
				// 新【添加】会话
				if (Utils.isIdValid(ms.partner_id)
						|| Utils.isIdValid(ms.group_id)) {
					if (unReadCount != -1) {
						ms.no_read = unReadCount;
					} else {
						ms.no_read = 1;
					}
					if (ms.group_id > 0) {
						TxGroup txGroup = TxGroup.getTxGroup(cr,
								(int) ms.group_id);
						if (txGroup != null && !TxGroup.isPublicGroup(txGroup)) {
							// 该群不是官方或者公共聊天室
							msgstats.put(ms.getSessionId(), ms);
							sendConstactsBroadCast();
							MsgStat.saveMsgStatToDB(ms, cr);
						}
					} else {
						// 单聊会话
						msgstats.put(ms.getSessionId(), ms);
						sendConstactsBroadCast();
						MsgStat.saveMsgStatToDB(ms, cr);
					}
				}
				if (Utils.debug) {
					Log.w(TAG, "添加新消息会话，MsgStat:" + ms.toString());
				}
			}
		}
	}

	public static void delMsgStatByPartnerId(ContentResolver cr, long partnerId) {
		synchronized (lock) {
			long sessionId = getMsgStatsSessionId(TxDB.MS_TYPE_TB, partnerId);
			MsgStat ms = msgstats.get(sessionId);

			if (ms != null) {
				// 取得了对应的msgStat
				msgstats.remove(sessionId);

				// txdata.getContentResolver().delete(TxDB.MsgStat.CONTENT_URI,TxDB.MsgStat.MSG_PARTNER_ID
				// + "=? AND "+ TxDB.MsgStat.MSG_GROUP_ID+ "< ?  ",new String[]
				// {""+ partnerId,"1" });

				cr.delete(TxDB.MsgStat.CONTENT_URI, TxDB.MsgStat.MSG_SESSION_ID
						+ "=?", new String[] { "" + sessionId });

				SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
			} else {
				if (Utils.debug) {
					Log.i(TAG, "delMsgStatByPartnerId(),没有找到对应的partnerId");
				}
			}
		}
	}

	/**
	 * 获取会话集合的缓存对象时，再进行排序
	 */
	public static ArrayList<MsgStat> getMsgStatsList() {
		ArrayList<MsgStat> aRet = new ArrayList<MsgStat>(msgstats.values());
		ArrayList<MsgStat> delStat = new ArrayList<MsgStat>();
		// 去除好友管家和群组动态的会话 3.9新需求
		for (MsgStat ms : aRet) {
			if (ms.getPartnerID() == TX.SL_GROUP_NOTICE) {
				// 群组动态
				delStat.add(ms);
			} else if (ms.getPartnerID() == TX.TUIXIN_FRIEND) {
				// 好友管家
				delStat.add(ms);
			}
		}
		if (delStat.size() > 0) {
			aRet.removeAll(delStat);
		}
		Collections.sort(aRet, new MessageComparator());
		return aRet;
	}

	public static MsgStat getMsgStatByGroupid(long groupid) {
		if (Utils.isIdValid(groupid)) {
			long msSessionId = getMsgStatsSessionId(TxDB.MS_TYPE_QU, groupid);
			MsgStat ms = msgstats.get(msSessionId);
			if (ms != null) {
				// 取得了对应的msgStat
				return ms;
			} else {
				if (Utils.debug) {
					Log.i(TAG, "findMsgStatByGroupid(),没有找到对应的groupid");
				}
			}

		}
		return null;
	}

	public static MsgStat getMsgStatByNoticeid(long noticeid) {
		if (Utils.isIdValid(noticeid)) {
			long msSessionId = getMsgStatsSessionId(TxDB.MS_TYPE_NOTICE,
					noticeid);
			MsgStat ms = msgstats.get(msSessionId);
			if (ms != null) {
				// 取得了对应的msgStat
				return ms;
			} else {
				if (Utils.debug) {
					Log.i(TAG, "getMsgStatByNoticeid(),没有找到对应的noticeid");
				}
			}

		}
		return null;
	}

	/**
	 * 更新MsgStat对象的阅读状态
	 */
	public static void updateMsgStatReadState(ContentResolver cr, long msgID,
			int read_state) {
		synchronized (lock) {
			Set<Long> keysSet = msgstats.keySet();
			for (Iterator<Long> it = keysSet.iterator(); it.hasNext();) {
				MsgStat ms = msgstats.get(it.next());
				if (ms.msg_id == msgID) {
					ms.read_state = read_state;
					// ms.no_read = 0;// 许春会添加更新未读数//先注掉 2014.05.12 shc
					ms.setHasSaveCache(false);// 会话对象有更新，则不使用缓存，重新读取 2014.05.12
												// shc
					ContentValues values = new ContentValues();
					values.put(TxDB.MsgStat.READ_STATE, read_state);
					int row = cr.update(TxDB.MsgStat.CONTENT_URI, values,
							TxDB.MsgStat.MSG_ID + "=" + msgID, null);

					if (Utils.debug) {
						Log.e(TAG,
								"updateMsgStatReadState()发送广播flush msgs，read_state="
										+ read_state + ",更新了MsgStat记录个数" + row);
					}
					SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
					break;
				}

			}

		}
	}

	public static MsgStat getMsgStatByPartnerId(long partnerid) {
		if (Utils.isIdValid(partnerid)) {
			long partnerSessionId = getMsgStatsSessionId(TxDB.MS_TYPE_TB,
					partnerid);
			MsgStat ms = msgstats.get(partnerSessionId);
			if (ms != null) {
				// 取得了对应的msgStat
				return ms;
			} else {
				if (Utils.debug) {
					Log.i(TAG, "findMsgStatByPartnerId(),没有找到对应的partnerid");
				}
			}
		}
		return null;
	}

	public static void delMsgStat(ContentResolver cr, long partnerid) {
		Set<Long> keysSet = msgstats.keySet();
		for (Iterator<Long> it = keysSet.iterator(); it.hasNext();) {
			MsgStat ms = msgstats.get(it.next());
			if (!Utils.isIdValid(ms.group_id)) {
				if (Utils.isIdValid(ms.partner_id)) {
					if (ms.partner_id == partnerid) {
						msgstats.remove(ms.getSessionId());
						// txdata.getContentResolver().delete(TxDB.MsgStat.CONTENT_URI,
						// TxDB.MsgStat.MSG_PARTNER_ID + "=?",
						// new String[] { "" + ms.partner_id });
						cr.delete(TxDB.MsgStat.CONTENT_URI,
								TxDB.MsgStat.MSG_SESSION_ID + "=?",
								new String[] { "" + ms.getSessionId() });

						SessionManager.broadcastMsg(TxData.FLUSH_MSGS);

						break;
					}
				}
			}
		}

	}

	// 删除群消息会话
	public static void delMsgStatByGroupId(ContentResolver cr, long groupid) {
		long groupSessionId = getMsgStatsSessionId(TxDB.MS_TYPE_QU, groupid);
		MsgStat ms = msgstats.get(groupSessionId);
		if (ms != null) {
			// 取得了对应的msgStat
			msgstats.remove(ms.getSessionId());
			cr.delete(TxDB.MsgStat.CONTENT_URI, TxDB.MsgStat.MSG_SESSION_ID
					+ "=?", new String[] { "" + groupSessionId });

			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
		} else {
			if (Utils.debug) {
				Log.i(TAG, "removeMsgStat(),没有找到对应的groupid");
			}
		}
	}

	/**
	 * 更新MsgStat对象的群组信息
	 */
	public static void updateGroupInfo(ContentResolver cr, TxGroup group) {
		synchronized (lock) {
			boolean isChanged = false;

			MsgStat ms = getMsgStatByGroupid(group.group_id);
			if (ms != null) {
				if (!Utils.isNull(group.group_title)) {
					ms.setGroupName(group.group_title);
					isChanged = true;// 有更新
				}
				if (!Utils.isNull(group.group_avatar)) {
					ms.setGroupAvatar(group.group_avatar);
					isChanged = true;
				}

				if (isChanged) {
					saveMsgStatToDB(ms, cr);// 保存到数据库中 2014.03.13
					SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
				}
			}

		}
	}

	public static void updateSingleMsg(TX tx) {
		synchronized (lock) {
			Set<Long> keysSet = msgstats.keySet();
			for (Iterator<Long> it = keysSet.iterator(); it.hasNext();) {
				MsgStat ms = msgstats.get(it.next());
				if (ms.partner_id == tx.partner_id
						&& ms.ms_type == TxDB.MS_TYPE_TB) {
					ms.partner_display_name = tx.getNick_name();
					SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
					break;
				}
			}
		}
	}

	public static void updateMsgStatByTX(ContentResolver cr, TX tx) {
		long partnerSessionId = getMsgStatsSessionId(TxDB.MS_TYPE_TB,
				tx.partner_id);
		MsgStat ms = msgstats.get(partnerSessionId);
		if (ms != null) {
			ms.partner_display_name = tx.getNick_name();

			ContentValues values = new ContentValues();
			values.put(TxDB.MsgStat.MSG_DISPLAY_NAME, tx.getNick_name());
			cr.update(TxDB.MsgStat.CONTENT_URI, values,
					TxDB.MsgStat.MSG_SESSION_ID + "=?", new String[] { ""
							+ partnerSessionId });
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
		}

	}

	// public static void clearMessageUnread(ContentResolver cr,List<TXMessage>
	// list) {
	// if (list != null && list.size() > 0) {
	// TXMessage txmsg = list.get(list.size() - 1);//这个代表最新消息吗？2014.04.30 shc
	// MsgStat.updateMsgStatByTxmsg(txmsg, cr, TxDB.MS_TYPE_TB, txmsg.gmid, 0,
	// true);
	// SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
	//
	// }
	// }

	public static void clearMessageUnread(ContentResolver cr, TXMessage txmsg) {
		if (txmsg != null) {
			MsgStat.updateMsgStatByTxmsg(txmsg, cr, TxDB.MS_TYPE_TB,
					txmsg.gmid, 0, true);
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
		}
	}

	public static void clearMessageUnread(ContentResolver cr, int msgType,
			long id) {
		MsgStat msgStat = null;
		if (msgType == TxDB.MS_TYPE_TB) {
			// 个人会话
			msgStat = getMsgStatByPartnerId(id);

		} else if (msgType == TxDB.MS_TYPE_QU) {
			// 群组会话
			msgStat = getMsgStatByGroupid(id);
		} else if (msgType == TxDB.MS_TYPE_NOTICE) {
			// 通知会话
			msgStat = getMsgStatByNoticeid(id);
		}

		if (msgStat != null) {
			msgStat.no_read = 0;// 清空未读消息数
			saveMsgStatToDB(msgStat, cr);
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
		}

	}

	public static void clearMsgStats(boolean isEixt, ContentResolver cr) {
		msgstats.clear();
		if (!isEixt) {
			// 如果不是退出时清空，则删除数据库内容
			cr.delete(TxDB.MsgStat.CONTENT_URI, null, null);
			SessionManager.broadcastMsg(TxData.FLUSH_MSGS);
		}
	}

}
