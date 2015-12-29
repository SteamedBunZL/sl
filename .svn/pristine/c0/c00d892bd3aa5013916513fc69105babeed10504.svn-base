package com.tuixin11sms.tx.message;

import java.util.LinkedHashSet;

import org.json.JSONArray;

import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.utils.Utils;

import android.content.ContentValues;
import android.util.Log;

/**
 * 赞的通知消息
 */

public class PraiseNotice {
	private static final String TAG = "PraiseNotice";
	public static final int ACTION_NONE = -1;// 没有被赞
	public static final int ACTION_PRAISE = 0;// 赞
	public static final int ACTION_CANCEL_PRAISE = 1;// 取消赞

	private LinkedHashSet<Long> uidSet = new LinkedHashSet<Long>();// 点赞人id集合
	private long groupId;// 群id
	private long gmId;// 消息在聊天室中的id
	private long uid;// 点赞人的id
	private int praiseFlag = ACTION_NONE;
	private long time;// 点赞时间
	private TXMessage txmsg;// 被赞的消息

	public PraiseNotice(long groupId, long gmId, long uid, int praiseFlag,
			long time) {
		super();
		this.groupId = groupId;
		this.gmId = gmId;
		this.uid = uid;
		uidSet.add(uid);
		this.praiseFlag = praiseFlag;
		setTime(time);
//		this.time = time;
	}
	public PraiseNotice(long groupId, long gmId, JSONArray idList, int praiseFlag,
			long time) {
		super();
		this.groupId = groupId;
		this.gmId = gmId;
		if(idList!=null){
			for (int i = 0; i < idList.length(); i++) {
				try {
					uidSet.add(idList.getLong(i));
				} catch (Exception e) {
					if(Utils.debug)Log.i(TAG, "构造PraiseNotice时Json异常",e);
				}
			}
		}
		this.praiseFlag = praiseFlag;
		setTime(time);
//		this.time = time;
	}

	public long getGroupId() {
		return groupId;
	}

	public void setGroupId(long groupId) {
		this.groupId = groupId;
	}

	public LinkedHashSet<Long> getUidSet() {
		return uidSet;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	 public void setUidSet(LinkedHashSet<Long> uidSet) {
		 this.uidSet = uidSet;
	 }
	
	public void addUid(long uid) {
		uidSet.add(uid);
	}
	
	public long getGmId() {
		return gmId;
	}

	public void setGmId(long gmId) {
		this.gmId = gmId;
	}

	public int getPraiseFlag() {
		return praiseFlag;
	}

	public void setPraiseFlag(int praiseFlag) {
		this.praiseFlag = praiseFlag;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		// 1398754691348
		if (("" + time).length() == 10) {
			// 如果时间单位是秒，变为毫秒
			time *= 1000;
		}
		this.time = time;
	}

	public TXMessage getTxmsg() {
		return txmsg;
	}

	public void setTxmsg(TXMessage txmsg) {
		this.txmsg = txmsg;
	}

	private String setToString() {
		JSONArray ja = new JSONArray();
		for (long id : uidSet) {
			ja.put(id);
		}
		return ja.toString();
	}

	public ContentValues toValues() {
		ContentValues values = new ContentValues();
		values.put(TxDB.PraiseNotice.GROUP_ID, groupId);
		values.put(TxDB.PraiseNotice.GMID, gmId);
		values.put(TxDB.PraiseNotice.ID_LIST, setToString());
		values.put(TxDB.PraiseNotice.TIME, time);
		return values;

	}

	@Override
	public String toString() {
		return "PraiseNotice [uidSet=" + uidSet + ", groupId=" + groupId
				+ ", gmId=" + gmId + ", uid=" + uid + ", praiseFlag="
				+ praiseFlag + ", time=" + time+", txMsg=" + (txmsg!=null) + "]";
	}
	

}
