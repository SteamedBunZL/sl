package com.tuixin11sms.tx.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.Utils;

public class PraiseNoticeImpl {

	private static final String TAG = "PraiseNoticeImpl";
	//代表被赞通知会话的id
	public static final long PRAISE_NOTICE_ID = MsgInfor.CLIENT_GET_PRAISED_MESSAGES;
	
	/**最多需要显示的点赞人的id*/
	public static final int PRAISED_USERS_LIMIT = 12;

	// 存储用户被点赞的消息,此map中key是消息的gmid
	private HashMap<Long, PraiseNotice> mPraiseNoticeMap = new HashMap<Long, PraiseNotice>();

	private List<IPraiseNoticeUpdate> mPriaseNoticeObservers = new ArrayList<PraiseNoticeImpl.IPraiseNoticeUpdate>();

	/** 赞或者取消赞消息的回调，key是被操作消息的gmid */
	private Set<PraiseMsgCallBack> praiseMsgSet = new HashSet<PraiseNoticeImpl.PraiseMsgCallBack>();

	private IReceivePraiseNotice mrpn = null;// 在聊天室中收到被赞消息通知时的回调

	private SessionManager mSess;
	
	private long lastGmid = 0;//每次从服务器取到消息集合的最后一个gmid
	
	private boolean isEndOfNoticeList = false;//是否取到了通知列表底部 

	public PraiseNoticeImpl(SessionManager sm) {
		mSess = sm;
	}

	public boolean add(PraiseNotice pn) {
		PraiseNotice mPn = mPraiseNoticeMap.get(pn.getGmId());
		if (mPn != null) {
			// 缓存中有该通知消息
			if(Utils.isIdValid(pn.getUid())){
				//id有效则存到缓存中更新数据库
				mPn.addUid(pn.getUid());
				if (mrpn != null) {
					// 通知聊天室播放心形被赞动画
					mrpn.onReceiveNotice(pn.getGroupId(),pn.getUid(),false);
				}
			}else {
				//uid无效，说明是批量add，不是收到了被赞通知，直接替换uid集合
				mPn.setUidSet(pn.getUidSet());
			}
			ContentValues values = mPn.toValues();
			mSess.getContentResolver().update(TxDB.PraiseNotice.CONTENT_URI, values, TxDB.PraiseNotice.GMID + "=?", new String[] { ""+pn.getGmId()});
			return false;
		} else if (updateMsgDB("" + pn.getGmId(), pn.getPraiseFlag()) > 0) {
			// 缓存中没有此通知，但是本地数据库中有此消息

			TXMessage txmsg = TXMessage.findTXMessageByGmid(
					mSess.getContentResolver(), pn.getGmId());
			pn.setTxmsg(txmsg);// 给pn设置txmsg
			// 插入数据库中和缓存中
			mPraiseNoticeMap.put(pn.getGmId(), pn);
			ContentValues values = pn.toValues();
			int refactRow = mSess.getContentResolver().update(
					TxDB.PraiseNotice.CONTENT_URI, values,
					TxDB.PraiseNotice.GMID + "=?",
					new String[] { "" + pn.getGmId() });
			if (refactRow == 0) {
				// 没有更新记录
				mSess.getContentResolver().insert(
						TxDB.PraiseNotice.CONTENT_URI, values);
				if (mrpn != null) {
					// 通知聊天室播放心形被赞动画
					mrpn.onReceiveNotice(pn.getGroupId(),pn.getUid(),true);
				}
			}
			return true;
		} else {
			if (Utils.debug) {
				Log.i(TAG, "缓存中没有此通知，并且本地数据中也没有这个Message消息");
			}
		}
		return false;
	}

	// 批量插入被赞通知消息
	public void addPraiseNotices(ArrayList<PraiseNotice> pnList,boolean isEnd) {
		//先清空idList为空的通知消息
		
		isEndOfNoticeList = isEnd;
		if(pnList!=null&&pnList.size()>0){
			lastGmid = pnList.get(pnList.size()-1).getGmId();
			Iterator<PraiseNotice> iter = pnList.iterator();
			while (iter.hasNext()) {
				PraiseNotice pn = iter.next();
				
				if(pn!=null&&pn.getUidSet().size()==0){
					//id集合为空，则删除本地记录
					delete(pn.getGmId());
				}else {
					//id集合不为空，则添加到本地
					add(pn);
				}
			}
			if(!isEnd&&mPraiseNoticeMap.size()<10){
				getNoticesFromServer();
			}
		}
		//通知观察者更新
//		if(mPraiseNoticeMap.size()>0){
			notifyDataSetChange();
//		}
	}

	public int delete(long gmid) {
		PraiseNotice mPn = mPraiseNoticeMap.remove(gmid);
		if (mPn != null) {
			Log.i(TAG, "删除缓存中此赞通知，mPn=" + mPn.toString());
		}
		int row = mSess.getContentResolver().delete(
				TxDB.PraiseNotice.CONTENT_URI, TxDB.PraiseNotice.GMID + "=?",
				new String[] { "" + gmid }); 
		return row;
	}

	public PraiseNotice get(long id) {
		PraiseNotice mPn = mPraiseNoticeMap.get(id);
		if (mPn == null) {
			// 如果TXmessage消息没有找到，则不去查PraiseNotice数据库
			TXMessage txmsg = TXMessage.findTXMessageByGmid(
					mSess.getContentResolver(), id);
			if (txmsg != null) {
				Cursor cur = mSess.getContentResolver().query(
						TxDB.PraiseNotice.CONTENT_URI, null,
						TxDB.PraiseNotice.GMID + "=?",
						new String[] { "" + id }, null);
				if (cur != null && cur.moveToNext()) {
					mPn = generatePraiseNotice(cur, txmsg);
					mPraiseNoticeMap.put(mPn.getGmId(), mPn);
				}
				cur.close();
			}
		}

		return mPn;
	}

	/** 更新消息表 */
	public int updateMsgDB(String gmid, int praiseFlag) {
		ContentValues values = new ContentValues();
		values.put(TxDB.Messages.PRAISE_STATE, praiseFlag);
		int row = mSess.getContentResolver().update(TxDB.Messages.CONTENT_URI,
				values, TxDB.Messages.GMID + "=?", new String[] { gmid });
		if (Utils.debug) {
			Log.i(TAG, "更新了" + row + "条记录");
		}
		return row;
	}

	public ArrayList<PraiseNotice> getNoticeLocalList() {
		ArrayList<PraiseNotice> noticeList = null;
		Cursor cur = mSess.getContentResolver().query(
				TxDB.PraiseNotice.CONTENT_URI, null, null, null,
				TxDB.PraiseNotice.DEFAULT_SORT_ORDER);
		if (cur != null) {
			while (cur.moveToNext()) {
				long gmid = cur.getInt(cur
						.getColumnIndex(TxDB.PraiseNotice.GMID));
				if (!mPraiseNoticeMap.containsKey(gmid)) {
					// 加载到内存中
					TXMessage txmsg = TXMessage.findTXMessageByGmid(
							mSess.getContentResolver(), gmid);
					PraiseNotice pn = generatePraiseNotice(cur, txmsg);
					if (pn != null) {
						mPraiseNoticeMap.put(gmid, pn);
					}
				}
			}
		}

		Collection<PraiseNotice> pns = mPraiseNoticeMap.values();
		if (pns != null) {
			noticeList = new ArrayList<PraiseNotice>(pns);
			Collections.sort(noticeList, new Comparator<PraiseNotice>() {

				@Override
				public int compare(PraiseNotice lhs, PraiseNotice rhs) {
					
					return (int)(lhs.getTime()-rhs.getTime());
				}
			});
			return noticeList;
		}

		return null;
	}

	/** 进入查看通知消息页面,清空未读消息数，返回消息集合 */
	public void onEnterNoticePage() {
		isEndOfNoticeList = false;
		lastGmid = 0;
//		ArrayList<PraiseNotice> pnList = getNoticeLocalList();

		mSess.getSocketHelper().sendGetPraisedMsgs(0, 10);// 获取被赞消息列表
		
		MsgStat.clearMessageUnread(mSess.getContentResolver(),TxDB.MS_TYPE_NOTICE, PRAISE_NOTICE_ID);

//		return pnList;
	}
	
	/**用户离开通知消息界面时*/
	public void onExitNoticePage() {
		isEndOfNoticeList = false;
		lastGmid = 0;
	}
	
	
	
	/**获取通知消息集合*/
	public ArrayList<PraiseNotice> getNoticeList() {
		ArrayList<PraiseNotice> noticeList = null;
		Collection<PraiseNotice> pns = mPraiseNoticeMap.values();
		if (pns != null) {
			noticeList = new ArrayList<PraiseNotice>(pns);
			Collections.sort(noticeList, new Comparator<PraiseNotice>() {

				@Override
				public int compare(PraiseNotice lhs, PraiseNotice rhs) {
					
					return (int)(rhs.getTime()-lhs.getTime());
				}
			});
		}
		return noticeList;

	}
	

	/**
	 * 通过cursor生成PraiseNotice对象
	 */
	private PraiseNotice generatePraiseNotice(Cursor c, TXMessage txmsg) {
		if (txmsg == null) {
			return null;
		}

		long groupId = c.getLong(c.getColumnIndex(TxDB.PraiseNotice.GROUP_ID));
		long gmid = c.getLong(c.getColumnIndex(TxDB.PraiseNotice.GMID));
		long time = c.getLong(c.getColumnIndex(TxDB.PraiseNotice.TIME));
		String idList = c
				.getString(c.getColumnIndex(TxDB.PraiseNotice.ID_LIST));

		PraiseNotice pn = null;
		try {
			pn = new PraiseNotice(groupId, gmid,
					new JSONArray(idList), PraiseNotice.ACTION_PRAISE, time);
			pn.setTxmsg(txmsg);
		} catch (JSONException e) {
			if(Utils.debug)Log.e(TAG,"构造PraiseNotice时Json异常",e);
		}
		return pn;
	}

	/** 赞一个消息 */
	public void praiseMsg(long groupId, long uid, long gmid, int praiseState) {
		if (praiseState == PraiseNotice.ACTION_NONE) {
			// 状态是没有被赞，执行赞
			mSess.getSocketHelper().sendPraiseMsg(groupId, uid, gmid, 0);

		} else if (praiseState == PraiseNotice.ACTION_PRAISE) {
			// 状态是咱过，执行取消赞
			mSess.getSocketHelper().sendPraiseMsg(groupId, uid, gmid, 1);

		}
	}
	
	
	/**注册接收赞结果的回调*/
	public void registePraiseResultCallback(PraiseMsgCallBack callback){
		if (callback != null) {
			// 传递过来的回调不为空
			praiseMsgSet.add(callback);
		}
	}
	
	
	/**反注册接收赞结果的回调*/
	public boolean unregistePraiseResultCallback(PraiseMsgCallBack callback){
		return praiseMsgSet.remove(callback);
	}
	

	/** 收到赞或者取消赞的结果 */
	public void onReceivePraiseResult(int result, int groupId, String gmid,
			int uid, int flag) {
			if (result == 0) {
				// 操作成功
				updateMsgDB(gmid, flag);
				for (PraiseMsgCallBack callback : praiseMsgSet) {
					callback.onSuccess(Long.parseLong(gmid),flag);
				}
			} else {
				// 操作失败
				for (PraiseMsgCallBack callback : praiseMsgSet) {
					callback.onFailed(Long.parseLong(gmid),flag);
				}
			}
	}
	
	
	/**收到用户消息被赞的通知*/
	public void onReceivePrisedNotice(PraiseNotice pn) {
		add(pn);
		MsgStat.updateMsgStatByNotice(pn, mSess.getContentResolver());
	}
	

	public void getNoticesFromServer() {
		if(!isEndOfNoticeList){
			mSess.getSocketHelper().sendGetPraisedMsgs(lastGmid, 10);
		}
	}
	
	
	public boolean isEndOfList() {
		return isEndOfNoticeList;
	}

	/** 通知观察者数据变化了 */
	private void notifyDataSetChange() {
		for (IPraiseNoticeUpdate ipnu : mPriaseNoticeObservers) {
			ipnu.update();
		}

	}

	public void registObserver(IPraiseNoticeUpdate iPnu) {
		if (iPnu != null) {
			mPriaseNoticeObservers.add(iPnu);
		}
	}

	public void unregistObserver(IPraiseNoticeUpdate iPnu) {
		if (iPnu != null) {
			mPriaseNoticeObservers.remove(iPnu);
		}
	}

	public void registReceiveNoticeListener(IReceivePraiseNotice rpn) {
		if (rpn != null) {
			mrpn = rpn;
		}
	}
	
	/**反注册聊天室中的Receiver*/
	public void unregistReceiveNoticeListener() {
		if (mrpn != null) {
			mrpn = null;
		}
	}

	// 赞数据更新的接口
	public interface IPraiseNoticeUpdate {
		// 参数为刚获取到的赞通知消息
		void update();
	}

	// 赞一个消息的回调
	public interface PraiseMsgCallBack {
		void onSuccess(long gmid,int praiseFlag);

		void onFailed(long gmid,int praiseFlag);
	}

	// 收到被赞消息通知的回调
	public interface IReceivePraiseNotice {
		void onReceiveNotice(long groupId,long uid,boolean isFirstPraised);
	}

}
