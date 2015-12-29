package com.tuixin11sms.tx.net;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;

import com.shenliao.data.DataContainer;
import com.shenliao.data.SearchData;
import com.shenliao.group.activity.GroupMember;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.SearchFriendActivity;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Messages;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MessageComparator;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.Area;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.Language;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.StatusCode;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

public class SocketHelper {
	private static final String TAG = SocketHelper.class.getSimpleName();
	/**
	 * 本应用Application对象
	 */
	private Context txdata;

	/**
	 * 上传的联系人个数
	 */
	public static int upCount;

	/**
	 * 底层通讯协议
	 */
	private static SocketChannelConnectionImpl connection;

	public static String LBS_SOCKET_URL = null;
	public static String SILENCE_Url = null;
	public static String SOCKET_URL = null;
	static {
		if (Utils.test) {
			LBS_SOCKET_URL = "http://192.168.16.168/api/serverlist";
			SILENCE_Url = "http://10.1.39.48:9080/siteOperation/support/shutUpSL!shutUp.action";
			// 外网
			SOCKET_URL = "socket://118.145.23.21:80";
			// 内网
			// SOCKET_URL = "socket://192.168.16.168:8900";

		} else {
			LBS_SOCKET_URL = "http://geo.clientapi.shenliao.com/api/serverlist";
			SILENCE_Url = "http://118.145.23.90:9080/siteOperation/support/shutUpSL!shutUp.action";
			// SOCKET_URL = "socket://mo.tuixin11.com:80";
			SOCKET_URL = "socket://im.clientapi.shenliao.com:80";
		}
	}

	// private SharedPreferences prefs = null;

	/**
	 * 网络类型
	 */
	public volatile static int net_type;
	public static final int mobile_type = 0;
	public static final int wifi_type = 1;

	/**
	 * 消息类型
	 */
	public static final int custom_type = 0;
	// 以下三个类型无引用 2014.05.27 shc
	// public static final int sms_type = 1;
	// public static final int photo_type = 2;
	// public static final int media_type = 3;

	public static ContactAPI contactapi;
	public ContentResolver cr;
	private static SocketHelper socketHelper;

	/**
	 * 无效的singleID/groupID
	 */
	private static final long INVALID_ID = -1;

	/**
	 * 当前singleID
	 */
	private AtomicLong currentSingleID = new AtomicLong(INVALID_ID);
	/**
	 * key为单聊的singleID, value为单聊界面的handler
	 */
	private Map<Long, Handler> singleHandlers = Collections
			.synchronizedMap(new HashMap<Long, Handler>());
	/**
	 * key为单聊的singleID, value为单聊界面的消息列表
	 */
	private Map<Long, MessageListManager> singleManagers = Collections
			.synchronizedMap(new HashMap<Long, MessageListManager>());

	/**
	 * 当前的groupID
	 */
	private AtomicLong currentGroupID = new AtomicLong(INVALID_ID);
	/**
	 * key为groupID, value为群聊界面的handler
	 */
	private Map<Long, Handler> groupHandlers = Collections
			.synchronizedMap(new HashMap<Long, Handler>());// 聊天室的handler集合
	/**
	 * key为groupID, value为群聊界面的消息列表
	 */
	private Map<Long, MessageListManager> groupManagers = Collections
			.synchronizedMap(new HashMap<Long, MessageListManager>());

	/**
	 * 更新群组信息
	 */
	public static final int UPDATE_GROUP_INFORMATION = -1235;
	/**
	 * SINGLE/GROUP消息列表发生改变的标记
	 */
	public static final int CHAT_MSG_LIST_CHANGED = -1234;
	/**
	 * SINGLE/GROUP数据变化的原因--删除数据
	 */
	public static final int CHAT_MSG_DEL = 0;
	/**
	 * SINGLE/GROUP数据变化的原因--数据增加
	 */
	public static final int CHAT_MSG_ADD = 1;
	/**
	 * SINGLE/GROUP数据变化的原因--用户本人发送消息
	 */
	public static final int CHAT_MSG_ADD_SELF = 2;
	/**
	 * SINGLE/GROUP数据变化的原因--加载历史消息
	 */
	public static final int CHAT_MSG_ADD_OLD = 3;
	/**
	 * SINGLE/GROUP数据变化的原因--数据更新, 但数据的数量没有改变
	 */
	public static final int CHAT_MSG_UPDATE = 4;
	/**
	 * 音频消息发送成功时的arg2
	 */
	public static final int CHAT_AUDIO_SENT = 10;

	/**
	 * 群公告
	 */
	public static final int GROUP_NOTICE = 11;
	/**
	 * 群里被人禁言了
	 */
	public static final int GROUP_SHUT_UP = 12;

	/**
	 * 用户信息下载完成的receiver
	 */
	private BroadcastReceiver uir;
	/**
	 * 删除消息的receiver
	 */
	private BroadcastReceiver mdr;

	private SessionManager mSess = null;//

	/**
	 * 私有构造函数
	 */
	private SocketHelper(SessionManager sm) {
		this.txdata = sm.getContext();
		cr = sm.getContentResolver();
		mSess = sm;
		contactapi = ContactAPI.getAPI();
		contactapi.setCr(cr);
		// prefs = txdata.getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);

		// 注册广播
		uir = new UserInforDoneReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.INTENT_ACTION_USERINFO_RSP);
		txdata.registerReceiver(uir, filter);

		mdr = new MessageDelReceiver();
		IntentFilter mdrFilter = new IntentFilter();
		mdrFilter.addAction(Constants.INTENT_ACTION_CLEAR_MSGS_FINISH);
		txdata.registerReceiver(mdr, mdrFilter);

		if (connection == null) {
			connection = new SocketChannelConnectionImpl(SOCKET_URL, txdata);
		} else {
			if (Utils.debug)
				Log.e(TAG,
						new Date().toLocaleString()
								+ "----SocketHelper is created again and connection is not null!");
		}
	}

	/**
	 * 构造获取SocketHelper的单一实例，计划只有LoginsessionManager调用
	 */
	public static SocketHelper getSocketHelper(SessionManager sm) {
		// 双重检查加锁
		if (socketHelper == null) {
			synchronized (SocketHelper.class) {
				if (socketHelper == null) {
					socketHelper = new SocketHelper(sm);
				}
			}
		}
		return socketHelper;
	}

	// /**
	// * 获得SocketHelper的实例，理论上调用该方法时，socketHelper不可能为空
	// */
	// public static SocketHelper getInstance() {
	// return socketHelper;
	// }

	/**
	 * 注册单聊界面handler
	 */
	public ArrayList<TXMessage> registerSingleHandler(long singleID,
			Handler handler) {
		singleHandlers.put(singleID, handler);
		MessageListManager mana = new SingleMessageManager(singleID);
		synchronized (singleManagers) {
			singleManagers.remove(singleID);
			singleManagers.put(singleID, mana);
		}
		currentSingleID.set(singleID);
		ArrayList<TXMessage> singleMsgs = mana.getMessageList();
		// onRetrieveOldMessages(singleMsgs, true, singleID, 0, false);
		onRetrieveOldMessages(mana, true, singleID, 0, false);
		return singleMsgs;
	}

	/**
	 * 注销单聊handler
	 */
	public void unRegisterSingleHandler(long singleID) {
		singleHandlers.remove(singleID);
		singleManagers.remove(singleID);
		currentSingleID.compareAndSet(singleID, INVALID_ID);
	}

	/**
	 * 注册群聊界面handler
	 */
	public ArrayList<TXMessage> registerGroupHandler(long groupID,
			Handler handler) {
		groupHandlers.put(groupID, handler);// 群聊天室的handler集合
		MessageListManager msgManager = new GroupMessageManager(groupID);
		synchronized (groupManagers) {
			groupManagers.remove(groupID);
			groupManagers.put(groupID, msgManager);
		}
		currentGroupID.set(groupID);// 设置当前聊天室的id
		ArrayList<TXMessage> groupMsgs = msgManager.getMessageList();// 取得与groupID匹配的消息集合
		long startTime = System.currentTimeMillis();
		// onRetrieveOldMessages(groupMsgs, false, groupID, 0,
		// true);//从数据库中加载单聊群聊消息，放在groupMsgs中
		onRetrieveOldMessages(msgManager, false, groupID, 0, true);// 从数据库中加载单聊群聊消息，放在groupMsgs中
		long spendTime = System.currentTimeMillis() - startTime;
		if (Utils.debug)
			Log.i(TAG, "onRetrieveOldMessages spends:" + spendTime);

		// TxGroup group = txdata.getTxGroupByGroupId4DB(groupID);//
		// 从数据库中通过groupID查询TxGroup
		TxGroup group = TxGroup
				.getTxGroup(txdata.getContentResolver(), groupID);//
		if (group == null) {
			mSess.getSocketHelper().sendGetGroup(groupID);// 如果本地数据库中没有，则发送消息获取群资料
		} else {
			// 获取群组信息后通知UI
			flushUIWithGroupInfor(group, handler);// 群对象不为空时，通知刷新UI
		}
		return groupMsgs;
	}

	/**
	 * 注销群聊handler
	 */
	public void unRegisterGroupHandler(long groupID) {
		groupHandlers.remove(groupID);
		groupManagers.remove(groupID);
		currentGroupID.compareAndSet(groupID, INVALID_ID);
	}

	/**
	 * 单聊获取历史消息
	 */
	public void getSingleOldMesaage(long singleID) {
		MessageListManager singleMana = singleManagers.get(singleID);
		if (singleMana != null && currentSingleID.get() == singleID) {
			// onRetrieveOldMessages(singleMana.getMessageList(), true,
			// singleID,
			onRetrieveOldMessages(singleMana, true, singleID, singleMana
					.getMessageList().size(), true);
		}
	}

	/**
	 * 加载群聊历史消息
	 */
	public void getGroupOldMessage(long groupID) {
		MessageListManager groupMana = groupManagers.get(groupID);
		if (groupMana != null && currentGroupID.get() == groupID) {
			// onRetrieveOldMessages(groupMana.getMessageList(), false, groupID,
			onRetrieveOldMessages(groupMana, false, groupID, groupMana
					.getMessageList().size(), true);
		}
	}

	/**
	 * 从数据库中加载单聊/群聊的历史消息
	 * 
	 * @param msgList
	 *            将历史消息加载到此list中
	 * @param isSingle
	 *            表明是加载单聊历史消息, 还是加载群聊历史消息
	 * @param mId
	 *            单聊的singleID/群聊的groupID
	 * @param beginpos
	 *            起始位置
	 * @param needFlush
	 *            是否需要通知界面刷新
	 */
	int eof = 0;

	// public void onRetrieveOldMessages(ArrayList<TXMessage> msgList,
	public void onRetrieveOldMessages(MessageListManager mana,
			boolean isSingle, long id, int beginpos, boolean needFlush) {
		ArrayList<TXMessage> msgList = mana.getMessageList();
		TX me = TX.tm.getTxMe();
		Set<Long> set = new HashSet<Long>();
		ArrayList<TXMessage> txmsgs = null;

		// 这里查询数据库了

		// TODO 判断如果有缓存的20条，就显示缓存的20条，不然从数据库中找

		if (mSess.lastMsgCatch != null && mSess.lastMsgCatch.containsKey(id)) {

			txmsgs = mSess.lastMsgCatch.get(id);
		} else {
			if (isSingle) {
				txmsgs = TXMessage.filterSingleMessageList(cr, id, beginpos);
			} else {
				long dtfirst = System.currentTimeMillis();
				txmsgs = TXMessage.filterGroupMessageList(cr, id, beginpos);
				if (txmsgs != null && txmsgs.size() > 0 && txmsgs.size() >= 20) {
					eof = 0;
				} else {
					eof = 1;
				}
				long dtsecond = System.currentTimeMillis() - dtfirst;
				if (Utils.debug) {
					Log.i(TAG, "filterGroupMessageList spends:" + dtsecond);
				}
			}
		}

		Collections.sort(txmsgs, new MessageComparator());

		if (!isSingle) {
			// 如果是群聊
			if (txmsgs.size() > 0 && msgList.size() == 0) {
				mana.setGmid(txmsgs.get(txmsgs.size() - 1).gmid);
			}
		}

		HashMap<Long, TX> txs = new HashMap<Long, TX>();
		HashSet<Long> ids = new HashSet<Long>();
		for (TXMessage txmsg : txmsgs) {
			long partnerid = txmsg.partner_id;
			if (partnerid <= 0) {
				continue;
			}
			if (partnerid != me.partner_id) {
				ids.add(partnerid);
			}
		}
		if (ids.size() > 0) {
			// txs = TX.findAllTXByPartnerID4DB(ids,
			// txdata.getContentResolver());

			for (Long tempId : ids) {
				TX tx = TX.tm.getTx(tempId);
				if (tx != null) {
					txs.put(tx.partner_id, tx);
				}
			}

		}

		for (TXMessage txmsg : txmsgs) {
			long partnerid = txmsg.partner_id;
			if (partnerid <= 0) {
				continue;
			}
			// 判断是否是本人发送
			if (partnerid == me.partner_id) {
				txmsg.was_me = true;
				txmsg.avatar_url = me.avatar_url;
				txmsg.nick_name = me.getNick_name();
				if (txmsg.read_state == TXMessage.NOT_SENT) {
					sendSingleMessage(txmsg);
				}
			}
			// 不是本人发送的时候需要给TxMessage添加用户信息
			else {
				// 对于单聊未读且非音频消息发送已读回执
				if (isSingle && txmsg.msg_type != TxDB.MSG_TYPE_AUDIO_EMS
						&& txmsg.read_state == TXMessage.UNREAD) {
					txmsg.read_state = TXMessage.READ;
					TXMessage.updateByMsgId(mSess.getContentResolver(),
							txmsg.msg_id, TXMessage.READ);
					sendReceipt(txmsg.partner_id, txmsg.msg_id + "",
							TXMessage.READ);
				}
				TX tx = null;
				if (txmsg.partner_id == TX.TUIXIN_MAN) {
					tx = TX.tm.getTxMan();
				} else {
					tx = txs.get(partnerid);

				}
				txmsg.getInfoFromTX(tx, isSingle);
				if (tx == null || Utils.isNull(tx.getNick_name())) {
					set.add(partnerid);
				}
			}
		}

		for (long partnerid : set) {
			sendGetUserInfor(partnerid);
		}

		int addcount = 0;

		synchronized (msgList) {
			for (int i = txmsgs.size() - 1; i >= 0; i--) {
				TXMessage txmsg = txmsgs.get(i);
				if (!msgList.contains(txmsg)) {
					addcount++;
					msgList.add(0, txmsg);
				}
			}
		}

		// 通知界面刷新
		if (needFlush) {
			Handler handler = null;
			if (isSingle) {
				handler = singleHandlers.get(id);
			} else {
				handler = groupHandlers.get(id);
			}
			if (handler != null) {
				if (Utils.debug) {
					Log.i(TAG, "onRetrieveOldMessages() eof = " + eof);
				}
				flushUIWithAddOld(addcount, handler, eof);
			}
		}
	}

	// /**
	// * 更新单聊消息为已读
	// */
	// public void updateSingleMessageToREAD(TXMessage txmsg) {
	// sendReceipt(txmsg.partner_id, txmsg.msg_id + "", TXMessage.READ);
	// onUpdateMessageState(true, txmsg.partner_id, txmsg.msg_id,
	// TXMessage.READ);
	// }
	//
	// /**
	// * 更新群聊消息为已读
	// */
	// public void updateGroupMessageToREAD(TXMessage txmsg) {
	// onUpdateMessageState(false, txmsg.group_id, txmsg.msg_id,
	// TXMessage.READ);
	// }

	/**
	 * 发送广播
	 * 
	 * @param key
	 *            键
	 * @param msg
	 *            内容
	 */
	private void broadcastMsg(String key, String msg) {
		Intent intent = new Intent(Constants.INTENT_ACTION_MSGHELPER_MSG);
		intent.putExtra(key, msg);
		txdata.sendBroadcast(intent);
	}

	/**
	 * 发送广播消息
	 * 
	 * @param action
	 *            Intent's action
	 * @param serverRsp
	 *            Server响应数据
	 */
	private void broadcastMsg(String action, ServerRsp serverRsp) {
		Intent intent = new Intent(action);
		intent.putExtra(Constants.EXTRA_SERVER_RSP_KEY, serverRsp.getBundle());
		intent.setExtrasClassLoader(ServerRsp.class.getClassLoader());
		txdata.sendBroadcast(intent);
	}

	/**
	 * 断开连接
	 */
	public void recovery() {
		if (Utils.debug) {
			Log.e(TAG, "断开连接时间为：" + new Date().toLocaleString());
		}
		// System.err.println(new Date());
		Thread.dumpStack();// 报java.lang.Throwable: stack dump异常
		connection.recovery();
		// editor.putString(CommonData.DOOR, "close").commit();
		// editor.commit();
		mSess.mPrefMeme.door.setVal("close").commit();

		singleManagers.clear();
		singleHandlers.clear();
		currentSingleID.set(INVALID_ID);
		groupManagers.clear();
		groupHandlers.clear();
		currentGroupID.set(INVALID_ID);
		TuiboSlienceManager.getInstance().clearSlienceChannels();

		LBSSocketHelper.getLBSSocketHelper(txdata).recovery();
	}

	/**
	 * 向server发送Message
	 * 
	 * @param message
	 *            待发送的字符串
	 */
	public void sendMessage(String message) {
		if (Utils.isNull(message))
			return;
		if (Utils.debug)
			Log.d(TAG, message);

		int net = Utils.getNetworkType(txdata);
		if (net == Utils.NET_NORMAL) {
			net_type = mobile_type;
		} else if (net == Utils.NET_WIFI) {
			net_type = wifi_type;
		}

		// if ("close".equals(prefs.getString(CommonData.DOOR, ""))) {
		if ("close".equals(mSess.mPrefMeme.door.getVal())) {
			return;
		}

		connection.sendMessage(message);
	}

	/**
	 * 新增联系人列表
	 */
	public ArrayList<String> newPhones;
	/**
	 * 需要上传的联系人数量
	 */
	public int adcot = 0;
	/**
	 * 判断是否上传过通讯录
	 */
	public boolean upAddr;

	/**
	 * 上传通讯录
	 */
	public void upAddressBook(Context context) {
		upAddr = true;
		String imsi = Utils.getIMSI(context);
		boolean isCN = true;
		if (imsi != null && !imsi.startsWith("460")) {
			isCN = false;
		}
		HashMap<Long, String> csCache = TX.tm.getContactsCache();
		if (csCache.size() == 0) {
			return;
		}

		Set<Long> allTmpPhones = csCache.keySet();
		ArrayList<String> allPhones = new ArrayList<String>();
		Iterator<Long> iter = allTmpPhones.iterator();
		while (iter.hasNext()) {
			// 用String这个方法可能会报 java.util.ConcurrentModificationException
			// String phone = String.valueOf(iter.next());

			String phone = "" + iter.next();
			if (isCN) {
				if (Utils.IsUserNumber(phone))
					allPhones.add(phone);
				else if (Utils.IsUpNumber(phone)) {
					allPhones.add(phone);
				}
			} else {
				allPhones.add(phone);
			}

		}
		allTmpPhones = null;
		// ArrayList<String> allTmpPhones = contactapi.getAllPhones(txdata);
		// ArrayList<String> allPhones;
		// if (isCN) {
		// allPhones = new ArrayList<String>();
		// for (String phone : allTmpPhones) {
		// if (Utils.IsUserNumber(phone))
		// allPhones.add(phone);
		// else if (Utils.IsUpNumber(phone)) {
		// allPhones.add(phone);
		// }
		// }
		// allTmpPhones.clear();
		// allTmpPhones = null;
		// } else {
		// allPhones = allTmpPhones;
		// }
		Cursor cursor = cr.query(TxDB.Aid.CONTENT_URI,
				new String[] { TxDB.Aid.UP_PHONES }, null, null, null);
		ArrayList<String> adrPhones = new ArrayList<String>();
		if (cursor != null) {
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					String num = cursor.getString(0);
					adrPhones.add(num);
				}
			}
			cursor.close();
		}
		if (adrPhones.size() > 0)
			for (String str : adrPhones) {
				if (!Utils.isNull(str)) {
					if (str.length() > 11) {
						str = str.substring(str.length() - 11);
					}
					for (int i = 0; i < allPhones.size(); i++) {
						if (allPhones.get(i).indexOf(str) != -1) {
							allPhones.remove(i);
							i--;
						}
					}
				}
			}
		adrPhones.clear();
		adrPhones = null;
		System.gc();
		if (allPhones.size() > 0) {
			if (Utils.debug)
				for (int i = 0; i < allPhones.size(); i++)
					if (Utils.debug)
						Log.w(TAG, "dddallPhones:" + allPhones.get(i));
			newPhones = new ArrayList<String>(allPhones);
			adcot = 0;
			sendAddressBook1(newPhones);
		}
	}

	/**
	 * 上传通讯录, 14号协议
	 */
	public void sendAddressBook1(ArrayList<String> phones) {
		try {
			String imsi = Utils.getIMSI(txdata);
			boolean isCN = true;
			// 把imsi != null改为!TextUtils.isEmpty(imsi)，
			// 解决在Galaxy Nexus 4.2.1系统下不插卡取得的imsi为空字符串不为null.
			// 避免imsi为空字符串时，后面执行的逻辑有误
			if (!TextUtils.isEmpty(imsi)) {
				if (Utils.debug) {
					Log.i(TAG, "mcc==============" + imsi.substring(0, 3));
				}
			}
			if (!TextUtils.isEmpty(imsi) && !imsi.startsWith("460")) {
				isCN = false;
			}
			MD5 m = new MD5();
			if (phones.size() < 10) {
				JSONStringer regster = new JSONStringer();
				regster.object().key("mt").value(MsgInfor.CLENT_CONTACTSUP)
						.key("ad").array();
				for (int i = 0; i < phones.size(); i++) {
					String phone = phones.get(i);
					if (isCN) {
						String num = Utils.getUserNumber(phone);
						if (num != null) {
							String firPhone = m.getMD5ofStr(num);
							String secPhone = m.getMD5ofStr(firPhone);
							regster.object().key("n").value("").key("p")
									.value(secPhone).key("e").value("")
									.key("c").value("").endObject();
						} else {
							num = Utils.getUpNumber(phone);
							if (num != null) {
								String firPhone = m.getMD5ofStr(num);
								String secPhone = m.getMD5ofStr(firPhone);
								regster.object().key("n").value("").key("p")
										.value(secPhone).key("e").value("")
										.key("c").value("").endObject();
							}
						}
					} else {
						String mcc = imsi.substring(0, 3);
						HashMap<Integer, String> mccCode = Utils.getMccCode();
						String code = mccCode.get(Integer.parseInt(mcc));
						String num = Utils.getMccNumber(phone, code);
						if (num != null) {
							String firPhone = m.getMD5ofStr(num);
							String secPhone = m.getMD5ofStr(firPhone);
							regster.object().key("n").value("").key("p")
									.value(secPhone).key("e").value("")
									.key("c").value("").endObject();
						}
					}
				}
				regster.endArray().endObject();
				String reg = regster.toString();
				sendMessage(reg);
			} else {
				JSONStringer regster = new JSONStringer();
				regster.object().key("mt").value(MsgInfor.CLENT_CONTACTSUP)
						.key("ad").array();
				for (int i = 0; i < 10; i++) {
					if (adcot < phones.size()) {
						String phone = phones.get(adcot);
						if (isCN) {
							String num = Utils.getUserNumber(phone);
							if (num != null) {
								String firPhone = m.getMD5ofStr(num);
								String secPhone = m.getMD5ofStr(firPhone);
								regster.object().key("n").value("").key("p")
										.value(secPhone).key("e").value("")
										.key("c").value("").endObject();
							} else {
								num = Utils.getUpNumber(phone);
								if (num != null) {
									String firPhone = m.getMD5ofStr(num);
									String secPhone = m.getMD5ofStr(firPhone);
									regster.object().key("n").value("")
											.key("p").value(secPhone).key("e")
											.value("").key("c").value("")
											.endObject();
								}
							}
						} else {
							String mcc = imsi.substring(0, 3);
							HashMap<Integer, String> mccCode = Utils
									.getMccCode();
							String code = mccCode.get(Integer.parseInt(mcc));
							String num = Utils.getMccNumber(phone, code);
							if (num != null) {
								String firPhone = m.getMD5ofStr(num);
								String secPhone = m.getMD5ofStr(firPhone);
								regster.object().key("n").value("").key("p")
										.value(secPhone).key("e").value("")
										.key("c").value("").endObject();
							}

						}
						adcot++;
					} else {
					}

				}
				regster.endArray().endObject();
				String reg = regster.toString();
				sendMessage(reg);
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送异常信息给推信管家, 6号协议
	 */
	public void sendException(String ex) {
		try {
			String exc = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_UPMSG).key("s").value(3).key("to")
					.value(0).key("sm").object().key("id")
					.value(Utils.createMsgId("" + TX.TUIXIN_FRIEND)).key("ct")
					.value(ex).endObject().endObject().toString();
			sendMessage(exc);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 在2008号协议的规则下, 根据TXMessage构造出json字符串
	 * 
	 * @param txmsg
	 *            待发送的消息
	 * @return 用于发送的json字符串
	 */
	private String getGroupMsgStr(TXMessage txmsg) {
		String chat = null;
		try {
			JSONStringer stringer = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_MSG_UP_QUN).key("to")
					.value(txmsg.group_id);
			if (txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS
					|| txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
				// 文本消息或者大文件消息(为了兼容性，大文件消息格式扩展文本消息的json格式)
				stringer = attachClientInfor(false, txmsg.group_id, stringer);
			}
			stringer = stringer.key("sm").object().key("id")
					.value("" + txmsg.msg_id).key("ct").value(txmsg.msg_body)
					.endObject();
			if (txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
				chat = stringer.endObject().toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS) {
				chat = stringer.key("obj").object().key("img")
						.value(txmsg.msg_url).endObject().endObject()
						.toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
				chat = stringer.key("obj").object().key("img").value("")
						.key("adu").object().key("end").value(txmsg.audio_end)
						.key("url").value(txmsg.msg_url).key("l")
						.value(txmsg.msg_file_length).key("t")
						.value(txmsg.audio_times).endObject().endObject()
						.endObject().toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS) {
				chat = stringer
						.key("obj")
						.object()
						.key("img")
						.value("")
						.key("card")
						.object()
						.key("u")
						.value(txmsg.msg_url)
						.key("d")
						.value(Utils
								.splitString(txmsg.contacts_person_name, 20))
						.endObject().endObject().endObject().toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_TCARD_SMS) {
				chat = stringer.key("obj").object().key("img").value("")
						.key("tcard").object().key("nn")
						.value(txmsg.tcard_name).key("phone")
						.value(txmsg.tcard_phone).key("avatar")
						.value(txmsg.tcard_avatar_url).key("id")
						.value(txmsg.tcard_id).key("sign")
						.value(txmsg.tcard_sign).key("sex")
						.value(txmsg.tcard_sex).endObject().endObject()
						.endObject().toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_GEO_SMS) {
				String geo = txmsg.geo;
				double la = Double.parseDouble(geo.substring(0,
						geo.indexOf(",")));
				double lo = Double
						.parseDouble(geo.substring(geo.indexOf(",") + 1));
				chat = stringer.key("obj").object().key("img").value("")
						.key("geo").object().key("la").value(la).key("lo")
						.value(lo).endObject().endObject().endObject()
						.toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
				// 大文件消息
				chat = stringer.key("obj").object().key("img")
						.value("")
						// 表明不是一个img消息
						.key("file").object().key("url").value(txmsg.msg_url)
						.key("l").value(txmsg.msg_file_length).key("t")
						.value(txmsg.audio_times)// 音频或者视频时长
						.endObject().endObject().endObject().toString();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_GIF_SMS) {
				chat = stringer.key("obj").object().key("img").value("")
						.key("emot").object().key("pkgid").value(txmsg.pkgid)
						.key("emomd5").value(txmsg.emomd5).endObject()
						.endObject().endObject().toString();
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return chat;
	}

	/**
	 * 给单聊或群聊增加客户端版本信息
	 */
	private JSONStringer attachClientInfor(boolean isSingle, long id,
			JSONStringer stringer) throws JSONException {
		MessageListManager mana = null;
		if (isSingle) {
			mana = singleManagers.get(id);
		}
		if (mana != null && !mana.isSentClientInfor()) {
			mana.setSentClientInfor(true);
			stringer = stringer.key("cli").value(Constants.cli);
		}
		return stringer;
	}

	/**
	 * 在6号协议的规则下, 根据TXMessage构造出json字符串
	 * 
	 * @param txmsg
	 *            待发送的消息
	 * @return 用于发送的json字符串
	 */
	private String getSingleMsgStr(TXMessage txmsg) {
		JSONStringer stringer = null;
		try {
			if (txmsg.partner_id == TX.TUIXIN_MAN) {
				stringer = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_UPMSG).key("s").value(3)
						.key("to").value(0);
			} else {
				stringer = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_UPMSG).key("to")
						.value(txmsg.partner_id);
			}

			// 为了兼容3.8.7之前的老版本，大文件消息json格式和普通文本格式保持一致
			if (txmsg.msg_type == TxDB.MSG_TYPE_COMMON_SMS
					|| txmsg.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS) {
				stringer = attachClientInfor(true, txmsg.partner_id, stringer);
			}
			stringer = getMesageBody(stringer, txmsg);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return stringer.toString();
	}

	/** 获取单聊消息的消息体内容 */
	private JSONStringer getMesageBody(JSONStringer stringer, TXMessage txmsg) {
		try {
			stringer = stringer.key("sm").object().key("id")
					.value("" + txmsg.msg_id).key("ct").value(txmsg.msg_body)
					.endObject();
			if (txmsg.msg_type == TxDB.MSG_TYPE_COMMON_SMS) {
				stringer = stringer.endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_IMAGE_EMS) {
				stringer = stringer.key("obj").object().key("img")
						.value(txmsg.msg_url).endObject().endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS) {
				stringer = stringer.key("obj").object().key("img").value("")
						.key("adu").object().key("end").value(txmsg.audio_end)
						.key("url").value(txmsg.msg_url).key("l")
						.value(txmsg.msg_file_length).key("t")
						.value(txmsg.audio_times).endObject().endObject()
						.endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
				stringer = stringer
						.key("obj")
						.object()
						.key("img")
						.value("")
						.key("card")
						.object()
						.key("u")
						.value(txmsg.msg_url)
						.key("d")
						.value(Utils
								.splitString(txmsg.contacts_person_name, 20))
						.endObject().endObject().endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_TCARD_SMS) {
				stringer = stringer.key("obj").object().key("img").value("")
						.key("tcard").object().key("nn")
						.value(txmsg.tcard_name).key("phone")
						.value(txmsg.tcard_phone).key("avatar")
						.value(txmsg.tcard_avatar_url).key("id")
						.value(txmsg.tcard_id).key("sign")
						.value(txmsg.tcard_sign).key("sex")
						.value(txmsg.tcard_sex).endObject().endObject()
						.endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_GEO_SMS) {
				String geo = txmsg.geo;
				double la = Double.parseDouble(geo.substring(0,
						geo.indexOf(",")));
				double lo = Double
						.parseDouble(geo.substring(geo.indexOf(",") + 1));
				stringer = stringer.key("obj").object().key("img").value("")
						.key("geo").object().key("la").value(la).key("lo")
						.value(lo).endObject().endObject().endObject();

			} else if (txmsg.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS) {
				// 大文件消息
				stringer = stringer.key("obj").object().key("img")
						.value("")
						// 表明不是一个img消息
						.key("file").object().key("url").value(txmsg.msg_url)
						.key("l").value(txmsg.msg_file_length).key("t")
						.value(txmsg.audio_times)// 音频或者视频时长
						.endObject().endObject().endObject();
			} else if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_GIF) {
				stringer = stringer.key("obj").object().key("img").value("")
						.key("emot").object().key("pkgid").value(txmsg.pkgid)
						.key("emomd5").value(txmsg.emomd5).endObject()
						.endObject().endObject();
			}

		} catch (NumberFormatException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return stringer;
	}

	/**
	 * 发送消息, 6号协议
	 * 
	 * @param txmsg
	 *            待发送的消息
	 */
	public void sendSingleMessage(TXMessage txmsg) {
		if (txmsg.partner_id == 0) {
			txmsg.partner_id = TX.TUIXIN_MAN;
		}
		switch (txmsg.msg_type) {
		case TxDB.MSG_TYPE_COMMON_SMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_AUDIO_EMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_CARD_EMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_GEO_SMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_TCARD_SMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_IMAGE_EMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_BIG_FILE_SMS:
			sendMessage(getSingleMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_SMS_GIF:
			sendMessage(getSingleMsgStr(txmsg));
		}
		onSendMessage(true, txmsg);
	}

	/**
	 * 增加单聊消息到消息列表中, 该消息需要存储数据库并更新缓存, 但不能发送给server 用于上传图片和音频时的显示
	 */
	public void addSingleMessage(TXMessage txmsg) {
		onSendMessage(true, txmsg);
	}

	/**
	 * 发送消息时的数据处理
	 * 
	 * @param isSingle
	 *            是单聊还是群聊
	 * @param txmsg
	 *            发送的消息
	 */
	private void onSendMessage(boolean isSingle, TXMessage txmsg) {
		AtomicLong currentID = null;
		long id = 0;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			id = txmsg.partner_id;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			id = txmsg.group_id;
			managers = groupManagers;
			handlers = groupHandlers;
		}
		TXMessage.saveTXMessagetoDB(txmsg, mSess.getContentResolver(), true);
		if (id == currentID.get()) {
			MessageListManager mana = managers.get(id);

			{
				if (!isSingle) {
					// 群消息则设置临时gmid
					txmsg.gmid = mana.getGmid() + 1;// 先给消息设置临时gmid 2013.11.07
													// shc
				}
			}

			if (mana != null) {
				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					int index = txmsgs.indexOf(txmsg);
					if (index == -1) {
						txmsgs.add(txmsg);
					} else {
						txmsgs.set(index, txmsg);
					}
					Handler handler = handlers.get(id);
					if (handler != null) {
						flushUIWithAddSelf(handler);
					}
				}
			}
		}
	}

	public void sendNoSendMsg(final List<TXMessage> list, final String user_id) {
		if (Utils.debug)
			Log.i(TAG, "发送未发送成功消息，sendNoSendMsg:" + list.size());
		new Thread() {
			public void run() {
				for (int i = list.size() - 1; i >= 0; i--) {
					autoUpMsg(list.get(i), user_id);
				}
			}
		}.start();
	}

	/** 自动重发消息？？？ */
	// 2013.12.03 shc
	public void autoUpMsg(final TXMessage txmessage, final String user_id) {
		if (Utils.debug) {
			Log.i(TAG, "autoUpMsg:" + txmessage.toString());
		}
		String msgUrl = txmessage.msg_url;
		if (txmessage.msg_type == TxDB.MSG_TYPE_IMAGE_EMS) {
			if (Utils.isNull(msgUrl)) {
				Utils.postImgSocket(txmessage);
			} else {
				sendMessage(getSingleMsgStr(txmessage));
			}
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_GEO_SMS) {
			sendMessage(getSingleMsgStr(txmessage));
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_AUDIO_EMS) {
			if (!Utils.isNull(msgUrl)) {
				sendMessage(getSingleMsgStr(txmessage));
			}
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
			if (Utils.isNull(msgUrl)) {
				String name = txmessage.msg_path.substring(
						txmessage.msg_path.lastIndexOf("/") + 1,
						txmessage.msg_path.length());
				File cardFile = new File(txmessage.msg_path);
				Utils.PostContactsSocket(txmessage, cardFile, name);
			} else {
				sendMessage(getSingleMsgStr(txmessage));
			}
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS
				|| txmessage.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
			// 单聊或者群聊的失败大文件消息，登陆成功后不自动重发，因为不知道网络状况是3G或wifi
			// Utils.uploadBigFile(txdata, txmessage, null, null);

		} else if (txmessage.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS) {
			if (Utils.isNull(msgUrl)) {
				Utils.postImgSocket(txmessage);
			} else {
				sendMessage(getGroupMsgStr(txmessage));
			}
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
			sendMessage(getGroupMsgStr(txmessage));
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
			if (!Utils.isNull(msgUrl)) {
				sendMessage(getGroupMsgStr(txmessage));
			}
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS) {
			if (Utils.isNull(msgUrl)) {
				String rootFile = Utils.getStoragePath(txdata);
				File cardFile = new File(rootFile);
				String cardPath = cardFile.getAbsolutePath() + "/"
						+ Utils.VCARD_PATH + "/" + txmessage.msg_path;
				cardFile = new File(cardPath);
				Utils.PostContactsSocket(txmessage, cardFile,
						txmessage.msg_path);
			} else {
				sendMessage(getGroupMsgStr(txmessage));
			}
		} else {
			String msgs = txmessage.msg_body;
			if (msgs != null && !msgs.equals("")) {
				String msg = getSingleMsgStr(txmessage);
				if (msg != null)
					sendMessage(msg);
			}
		}
	}

	public void upStringImg(final TXMessage txmessage, final String user_id) {
		String path = txmessage.msg_url;
		if (txmessage.msg_type == TxDB.MSG_TYPE_IMAGE_EMS) {
			if (Utils.isNull(path)) {
				if (path.startsWith(TXMessage.MOBILE_TYPE)) {
					String img_id = path.substring(TXMessage.MOBILE_TYPE
							.length());
					if (Utils.debug)
						Log.i(TAG, img_id
								+ "____________getImgByPath______________"
								+ path);
					File file = new File(img_id);
					if (!file.exists()) {
						if (Utils.debug)
							Log.i(TAG, img_id + "file not exists");
						ContentValues values = new ContentValues();
						values.put("read_state", 5);
						cr.update(TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?", new String[] { ""
										+ txmessage.msg_id });
						broadcastMsg("msg", "nofindimg");
						return;
					}
					String imgPath = Utils.postImgs(txdata, user_id, img_id);
					if (Utils.debug)
						Log.i(TAG, "+++++++++++imgPath++++++++++" + imgPath);
					if (imgPath != null) {
						ContentValues values = new ContentValues();
						values.put(TxDB.Messages.MSG_PATH, imgPath);
						cr.update(TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?", new String[] { ""
										+ txmessage.msg_id });
						txmessage.msg_url = imgPath;
						sendMessage(getSingleMsgStr(txmessage));
					} else {
					}
				} else if (path.startsWith(TXMessage.IMG_TYPE)) {
					String img_id = path.substring(TXMessage.IMG_TYPE.length());
					if (Utils.debug)
						Log.i(TAG, img_id
								+ "____________getImgByPath______________"
								+ path);
					String storagePath = Utils.getStoragePath(txdata);
					if (Utils.isNull(storagePath)) {
						return;
					}
					StringBuffer tempPath = new StringBuffer()
							.append(storagePath).append("/").append(img_id)
							.append(".jpg");
					File file = new File(tempPath.toString());
					if (!file.exists()) {
						if (Utils.debug)
							Log.i(TAG, tempPath.toString() + "file not exists");
						ContentValues values = new ContentValues();
						values.put("read_state", 5);
						cr.update(TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?", new String[] { ""
										+ txmessage.msg_id });
						broadcastMsg("msg", "nofindimg");
						return;
					}
					String imgPath = Utils.postImgs(txdata, user_id,
							tempPath.toString());
					if (Utils.debug)
						Log.i(TAG, "+++++++++++imgPath++++++++++" + imgPath);
					if (imgPath != null) {
						ContentValues values = new ContentValues();
						values.put(TxDB.Messages.MSG_PATH, imgPath);
						cr.update(TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?", new String[] { ""
										+ txmessage.msg_id });
						txmessage.msg_url = imgPath;
						sendMessage(getSingleMsgStr(txmessage));
					} else {
					}
				}
			} else {
				sendMessage(getSingleMsgStr(txmessage));
			}

		} else if (txmessage.msg_type == TxDB.MSG_TYPE_GEO_SMS) {
			sendMessage(getSingleMsgStr(txmessage));
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_AUDIO_EMS) {
			sendMessage(getSingleMsgStr(txmessage));
		} else if (txmessage.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
			sendMessage(getSingleMsgStr(txmessage));
		} else {
			String msgs = txmessage.msg_body;
			if (msgs != null && !msgs.equals("")) {
				String msg = getSingleMsgStr(txmessage);
				if (msg != null)
					sendMessage(msg);
			}
		}
	}

	/**
	 * 获取离线消息, 10号协议
	 * 
	 * @param userid
	 *            用户ID
	 */
	public void sendGetSingleOfflineMsg(long userid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_OFFLINEGET).key("uid")
					.value((int) userid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * client收到消息后发送消息回执, 9号协议
	 * 
	 * @param to
	 *            目标ID
	 * @param id
	 *            消息ID
	 * @param d
	 *            消息状态 {"mt":9, "to":1235532363, "id":1642473375,"d":1}
	 */
	public void sendReceipt(long to, String id, int d) {
		try {
			String send = null;
			if (to == TX.TUIXIN_MAN || to == 0) {
				send = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_DOWNMSG).key("s").value(3)
						.key("to").value(0).key("id").value(id).key("d")
						.value(d).endObject().toString();
			} else {
				send = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_DOWNMSG).key("to").value(to)
						.key("id").value(id).key("d").value(d).endObject()
						.toString();
			}
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 确认收到2010号协议, 2015号协议
	 */
	public void sendGroupReceipt(long groupId, Long id) {
		if (groupId == -1)
			return;
		String send = null;
		try {
			send = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_MSG_DOWN_QUN).key("id")
					.value(id.toString()).endObject().toString();
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 确认收到2012, 2016号协议 {"mt":2016,”msglist”:[{"id":"261426046500120938"},]}
	 */
	public void sendGroupOffLineReceipt(List<String> list) {
		try {
			JSONStringer stringer = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_OFFLINERECEIPTE_MEG_QUN)
					.key("msglist").array();
			for (int i = 0; i < list.size(); i++) {
				stringer = stringer.object().key("id").value(list.get(i))
						.endObject();
				if ((i + 1) % 20 == 0) {
					stringer = stringer.endArray().endObject();
					String send = stringer.toString();
					sendMessage(send);
					stringer = new JSONStringer().object().key("mt")
							.value(MsgInfor.CLENT_GET_OFFLINERECEIPTE_MEG_QUN)
							.key("msglist").array();
				} else if (i == list.size() - 1) {
					stringer = stringer.endArray().endObject();
					String send = stringer.toString();
					sendMessage(send);
				}
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送输入状态, 4号协议
	 * 
	 * @param groupId
	 * @param id
	 */
	public void sendInputType(Long id) {
		if (id <= 0)
			return;
		String send = null;
		try {
			send = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_INPUT).key("to").value(id).key("d")
					.value(1).endObject().toString();
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 批量发送已读回执, 28号协议
	 * 
	 * @param to
	 *            目标ID
	 * @param list
	 *            已读消息ID列表 {"mt":28, "to":1642473375
	 *            ,"rd":[{"id":"1642473375"},{"id":"1642473375"}]}
	 */
	public void sendAllReceipts(long to, List<String> list) {
		try {
			JSONStringer stringer = null;
			if (to == TX.TUIXIN_MAN) {
				stringer = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_MSGREAD).key("s").value(3)
						.key("to").value(0).key("rd").array();
			} else {
				stringer = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_MSGREAD).key("to").value(to)
						.key("rd").array();
			}
			for (int i = 0; i < list.size(); i++) {
				String msgid = list.get(i);
				stringer.object().key("id").value(msgid).endObject();
				onUpdateMessageState(true, currentSingleID.get(),
						Long.valueOf(msgid), TXMessage.READ);
			}
			sendMessage(stringer.endArray().endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取离线消息回执, 12号协议
	 * 
	 * @param userid
	 *            用户ID
	 */
	public void sendGetOffLineReceipt(long userid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_OFFLINERECEIPT).key("uid")
					.value((int) userid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送修改后的用户信息, 16号协议
	 */
	public void sendUserInforChange() {
		try {
			// JSONStringer jsonStr = new JSONStringer().object().key("mt")
			// .value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
			// .key("nn").value(prefs.getString(PrefsMeme.NICKNAME, ""))
			// .key("avatarurl")
			// .value(prefs.getString(PrefsMeme.AVATAR_URL, ""))
			// .key("sex").value(prefs.getInt(PrefsMeme.SEX, TX.DEFAULT_SEX))
			// .key("sign").value(prefs.getString(PrefsMeme.SIGN, " "))
			// .key("birthday")
			// .value(prefs.getInt(PrefsMeme.BIRTHDAY, 0)).key("blood")
			// .value(prefs.getInt(PrefsMeme.BLOODTYPE, 0)).key("job")
			// .value(prefs.getString(PrefsMeme.JOB, " "));
			// str2Ja(jsonStr, "narea", prefs.getString(PrefsMeme.AREA, ""));
			// str2Ja(jsonStr, "nhobby", prefs.getString(PrefsMeme.HOBBY, ""));
			// str2Ja(jsonStr, "lang", prefs.getString(PrefsMeme.LANGUAGES,
			// ""));
			JSONStringer jsonStr = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
					.key("nn").value(mSess.mPrefMeme.nickname.getVal())
					.key("avatarurl")
					.value(mSess.mPrefMeme.avatar_url.getVal()).key("sex")
					.value(mSess.mPrefMeme.sex.getVal()).key("sign")
					.value(mSess.mPrefMeme.sign.getVal()).key("birthday")
					.value(mSess.mPrefMeme.birthday.getVal()).key("blood")
					.value(mSess.mPrefMeme.bloodtype.getVal()).key("job")
					.value(mSess.mPrefMeme.job.getVal());
			str2Ja(jsonStr, "narea", mSess.mPrefMeme.area.getVal());
			str2Ja(jsonStr, "nhobby", mSess.mPrefMeme.hobby.getVal());
			str2Ja(jsonStr, "lang", mSess.mPrefMeme.languages.getVal());
			jsonStr.endObject();
			sendMessage(jsonStr.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 向神聊服务器上传资料
	 */
	public void sendUserSinaInforChange() {
		try {
			// JSONStringer jsonStr = new JSONStringer().object().key("mt")
			// .value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
			// .key("nn").value(prefs.getString(PrefsMeme.NICKNAME, ""))
			// .key("avatarurl")
			// .value(prefs.getString(PrefsMeme.AVATAR_URL, ""))
			// .key("sex").value(prefs.getInt(PrefsMeme.SEX, TX.DEFAULT_SEX))
			// .key("sign").value(prefs.getString(PrefsMeme.SIGN, " "))
			// .key("birthday")
			// .value(prefs.getInt(PrefsMeme.BIRTHDAY, 0)).key("blood")
			// .value(prefs.getInt(PrefsMeme.BLOODTYPE, 0));
			JSONStringer jsonStr = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
					.key("nn").value(mSess.mPrefMeme.nickname.getVal())
					.key("avatarurl")
					.value(mSess.mPrefMeme.avatar_url.getVal()).key("sex")
					.value(mSess.mPrefMeme.sex.getVal()).key("sign")
					.value(mSess.mPrefMeme.sign.getVal()).key("birthday")
					.value(mSess.mPrefMeme.birthday.getVal()).key("blood")
					.value(mSess.mPrefMeme.bloodtype.getVal());
			jsonStr.endObject();
			sendMessage(jsonStr.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 字符串转JSON数组
	 * 
	 * @param jsonStr
	 * @param key
	 * @param values
	 * @throws JSONException
	 */
	private void str2Ja(JSONStringer jsonStr, String key, String values)
			throws JSONException {
		if (!StringUtils.isNull(values)) {
			jsonStr.key(key).array();
			List<String> list = StringUtils.str2List(values);
			for (String str : list) {
				jsonStr.value(Integer.parseInt(str));
			}
			jsonStr.endArray();
		}
	}

	// 无引用，注掉 2014.01.22 shc
	// /**
	// * 发送修改后的用户信息, 16号协议 xuchunhui修改
	// */
	// public void sendUserChange(String nickName) {
	// try {
	// String regster = new JSONStringer().object().key("mt")
	// .value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
	// .key("nn").value(nickName).key("avatarurl")
	// .value(prefs.getString(PrefsMeme.AVATAR_URL, ""))
	// .key("sex").value(prefs.getInt(PrefsMeme.SEX, TX.DEFAULT_SEX))
	// .key("area").value(prefs.getString(PrefsMeme.AREA, ""))
	// .key("sign").value(prefs.getString(PrefsMeme.SIGN, " "))
	// .key("birthday")
	// .value(prefs.getInt(PrefsMeme.BIRTHDAY, 0)).key("blood")
	// .value(prefs.getInt(PrefsMeme.BLOODTYPE, 0)).key("hobby")
	// .value(prefs.getString(PrefsMeme.HOBBY, "")).key("job")
	// .value(prefs.getString(PrefsMeme.JOB, "")).endObject()
	// .toString();
	// sendMessage(regster);
	// } catch (JSONException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// }
	// }

	/**
	 * 发送修改后的用户信息, 16号协议
	 */
	public void sendNameChange(String usename) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n")
					.value(usename).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 更改用户性别(16)
	 * 
	 * @param sex
	 */
	public void sendSexChange(int sex) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
					.key("sex").value(sex).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送修改后的用户信息, 16号协议
	 */
	public void sendNickNameChange(String nickname) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
					.key("nn").value(nickname).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送修改后的用户信息, 16号协议
	 */
	public void sendUpAvatar(String avatar_url) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERNAMECHARGE).key("n").value("")
					.key("avatarurl").value(avatar_url).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	// /**
	// * 发送修改后的手机号, 18号协议
	// */
	// public void sendPhoneChange(String phone) {
	// try {
	// String regster = new JSONStringer().object().key("mt")
	// .value(MsgInfor.CLENT_USERPHONCHARGE).key("p").value(phone)
	// .endObject().toString();
	// sendMessage(regster);
	// } catch (JSONException e) {
	// if (Utils.debug)
	// e.printStackTrace();
	// }
	// }

	/**
	 * 获取用户相册(111)
	 * 
	 * @param userId
	 *            用户id
	 */
	public void getAlbum(long userId, int aver) {
		try {
			String getAlbum = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_ALBUM).key("uid").value(userId)
					.key("aver").value(aver).endObject().toString();
			sendMessage(getAlbum);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 设置相册(113)
	 * 
	 * @param album
	 *            相册url
	 */
	public void setAlbum(List<String> album) {
		try {
			JSONStringer jsonStr = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_SET_ALBUM).key("album").array();
			for (String url : album) {
				jsonStr.value(url);
			}
			jsonStr.endArray().endObject();
			sendMessage(jsonStr.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 拒绝接收所有好友请求(114)
	 * 
	 * @param op
	 *            操作，0为接收，1为不接收好友请求
	 */
	public void sendRefuseReq(int op) {
		try {
			String refuseReq = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_REFUSE_REQ).key("op").value(op)
					.endObject().toString();
			sendMessage(refuseReq);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送修改后的密码, 30号协议
	 */
	public void sendPassword(String password) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHARGE_PASSWORDGET).key("oldpwd")
					.value("").key("newpwd").value(password).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送请求，获取联系人ID列表
	 * 
	 * @param page
	 *            页数, 从0开始
	 */
	public void sendContactsId(int page) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_FRIENDS_IDS).key("p").value(page)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取离线系统消息, 43号协议
	 * 
	 * @param userid
	 *            用户ID
	 */
	public void sendGetOfflineSystemMsg(long userid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SYSTEM_MSG).key("uid")
					.value((int) userid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送查询最新客户端版本号信息, 64号协议
	 */
	public void sendCheckVer() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHECK_VER).key("ver").value(0)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 加好友请求 带请求信息 37号
	 * 
	 * @param partenerid
	 * @param ac
	 * @param desc
	 */
	public void sendAddPartener(long partenerid, String ac, String desc) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_ADD_PARTNER).key("uid")
					.value(partenerid).key("ac").value(ac).key("desc")
					.value(desc).endObject();
			String send = regster.toString();
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送删除好友信息, 39号协议
	 * 
	 * @param partenerid
	 *            待删除的好友ID
	 */
	public void sendDelPartner(long partenerid) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_DEL_PARTNER).key("uid")
					.value(partenerid).endObject();// .value(value).value(userid).endObject().toString();
			String send = regster.toString();
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送添加好友应答消息, 41号协议
	 * 
	 * @param agree
	 *            是否同意
	 * @param partner_id
	 *            好友请求发起方ID
	 * @param addtoo
	 *            在同意的前提下, 是否同时加对方为好友
	 * @param ac
	 *            验证码
	 */
	public void sendAgreeMsg(boolean agree, long partner_id, boolean addtoo,
			String ac) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_AGREE_MSG).key("agree").value(agree)
					.key("uid").value(partner_id).key("add").value(addtoo)
					.key("ac").value(ac).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送查找用户信息, 24号协议
	 * 
	 * @param msg
	 *            查找内容
	 */
	public void sendSearchUser(String msg) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SEARCH).key("m").value(msg)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 搜索用户(116)
	 * 
	 * @param tx
	 *            用户对象
	 */
	public void sendSearchUser(TX tx, int oldeadline) {
		try {
			if (tx != null) {
				JSONStringer jsonStr = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLIENT_SEARCH_USER);
				jsonStr.key("m").value(tx.getNick_name());
				jsonStr.key("narea").array();

				int constellation = 0;
				int language = 0;
				if (!Utils.isNull(tx.area)) {
					List<String> list = StringUtils.str2List(tx.area);
					for (String str : list) {
						if (Integer.parseInt(str) != Area.UNLIMIT_ID)
							jsonStr.value(Integer.parseInt(str));
					}
				}
				try {
					constellation = Integer.parseInt(tx.constellation);
				} catch (NumberFormatException e) {
				}
				try {
					language = Integer.parseInt(tx.getLanguages());
					if (language == Language.UNLIMIT_ID)
						language = 0;
				} catch (NumberFormatException e) {
				}
				jsonStr.endArray();

				jsonStr.key("oldeadline").value(oldeadline);
				jsonStr.key("sex").value(tx.getSex());
				jsonStr.key("age").value(tx.age);
				jsonStr.key("constellation").value(constellation);
				jsonStr.key("blood").value(tx.bloodType);
				jsonStr.key("lang").value(language);
				jsonStr.key("ol").value(tx.getOnLine());
				jsonStr.key("cp").value(DataContainer.getSearchUserPageNum());
				jsonStr.endObject();
				sendMessage(jsonStr.toString());
				if (!SearchFriendActivity.isOnline) {
					SearchData.getInstance(txdata).saveSearch(tx);
				}

			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取好友备注名(81)
	 */
	public void sendGetRemarkNames() {
		try {
			String str = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_REMARK_NAMES).key("p").value(0)
					.endObject().toString();
			sendMessage(str);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 设置备注名(83)
	 */
	public void sendSetRemarkName(long uid, String remarkName) {
		try {
			String str = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_SET_REMARK_NAME).key("uid")
					.value(uid).key("n").value(remarkName).endObject()
					.toString();
			sendMessage(str);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取黑名单列表(78)
	 */
	public void sendGetBlackList(int pageNum) {
		try {
			// 用TX管理黑名单吧，2014.01.21 shc
			// String str = new JSONStringer().object().key("mt")
			// .value(MsgInfor.CLIENT_GET_BLACKLIST).key("p")
			// .value(DataContainer.getBlackListPageNum()).endObject()
			// .toString();
			String str = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_BLACKLIST).key("p")
					.value(pageNum).endObject().toString();
			sendMessage(str);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送通过手机号寻回密码消息, 26号协议
	 * 
	 * @param username
	 *            手机号
	 */
	public void sendGetPassWordbyPhone(String username) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_PASSWORDGET).key("o").value(username)
					.key("e").value("").endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送修改密码消息, 30号协议
	 * 
	 * @param oldpass
	 *            旧密码
	 * @param newpass
	 *            新密码
	 */
	public void sendChargePassWord(String oldpass, String newpass) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHARGE_PASSWORDGET).key("oldpwd")
					.value(oldpass).key("newpwd").value(newpass).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送心跳, 49号协议
	 */
	public void sendPing() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_PING).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送客户端语言消息, 59号协议
	 */
	public void sendLangid() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_LANGID).key("langid")
					.value(Utils.getLang()).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送绑定邮箱请求, 51号协议
	 * 
	 * @param mail
	 *            用户邮箱
	 */
	public void sendCheckEmail(String mail) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHECK_EMAIL).key("mail").value(mail)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送获取手机邮箱绑定状态的消息, 53号协议
	 */
	public void sendQueryPhoneEmail() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_QUERY_PHONE_EMAIL).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送手机解绑消息, 85号协议
	 */
	public void sendUnbindPhone() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_UNBIND_PHONE).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送邮箱解绑消息, 87号协议
	 */
	public void sendUnbindEmail() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_UNBIND_EMAIL).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送手机绑定请求, 55号协议
	 * 
	 * @param phone
	 *            手机号
	 */
	public void sendBindPhone(String phone) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_BIND_PHONE).key("phone").value(phone)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送验证码绑定手机, 57号协议
	 * 
	 * @param code
	 *            验证码
	 */
	public void sendCheckBindPhone(String code) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHECK_BIND_PHONE).key("c")
					.value(code).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送消息, 以获取绑定手机所需发送的短信内容, 60号协议
	 */
	public void sendSmsIdentification() {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SMS_IDENTIFICATION).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 获取用户信息的回调,key是用户id,value是所有关于此id的回调 */
	public HashMap<Long, List<AsyncCallback<?>>> userInforsCallback = new HashMap<Long, List<AsyncCallback<?>>>();

	/**
	 * 发送消息, 以获取用户基本信息, 32号协议
	 * 
	 * @param userid
	 *            用户ID
	 */
	public void sendGetUserInfor(long userid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_USERINFRO).key("uid").value(userid)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 获取用户的详细信息 */
	public void getUserDetail(long uid, AsyncCallback<?> callback) {
		List<AsyncCallback<?>> callbackList = userInforsCallback.get(uid);
		if (callbackList != null) {
			callbackList.add(callback);
		} else {
			callbackList = new ArrayList<AsyncCallback<?>>();
			callbackList.add(callback);
			userInforsCallback.put(uid, callbackList);
		}

		sendGetUserInfor(uid);

	}

	/**
	 * 获取用户资料
	 * 
	 * @param userId
	 *            用户ID
	 * @param fields
	 *            获取那些用户资料
	 */
	public void sendGetUserInfor(long userId, List<String> fields) {
		try {
			JSONStringer jsonStr = new JSONStringer().key("mt")
					.value(MsgInfor.CLENT_USERINFRO).key("uid").value(userId);
			if (fields != null && !fields.isEmpty()) {
				jsonStr.key("fields").array();
				for (String field : fields) {
					jsonStr.value(field);
				}
				jsonStr.endArray();
			}
			jsonStr.endObject();
			sendMessage(jsonStr.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	// 获取我的最新个人信息

	/** 获取最新的个人信息，看是否升等级 */
	public void getNewUserinforForLevel() {
		sendGetUserInfor(mSess.getUserid());
	}

	/**
	 * 发送创建群组信息, 2000号协议
	 * 
	 * @param title
	 *            群组名称
	 * @param intro
	 *            群简介
	 * @param avatar
	 *            群头像
	 * @param type
	 *            群类型 0为半公开，1为公开，2为私密
	 */
	public void sendCreatGroup(String title, String intro, String avatar,
			int type) {
		try {

			TxGroup txGroup = new TxGroup();
			txGroup.group_title = title;
			txGroup.group_avatar = avatar;
			txGroup.group_sign = intro;
			txGroup.group_type_channel = type;
			Utils.waitSaveTxGroup = txGroup;

			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CREATE_QUN).key("topic").value(title)
					.key("intro").value(intro).key("avatar").value(avatar)
					.key("type").value(type).key("sn").value(type).endObject()// sn为一个随意的数字，服务器会返回
					.toString();
			if (Utils.debug)
				Log.i(TAG, regster);
			sendMessage(regster);

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送消息, 以获取群组信息, 2033号协议
	 * 
	 * @param group_id
	 *            群组ID
	 */
	public void sendGetGroup(long group_id) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_INFO_QUN).key("gid").value(group_id)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 通过页码获取群成员id */
	public void sendGetGroup(long group_id, int pageNum) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_INFO_QUN).key("gid").value(group_id)
					.key("mecp").value(pageNum).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送消息, 群黑名单列表, 2035号协议
	 * 
	 * @param group_id
	 *            群组ID
	 */
	public void sendGetBlackListGroup(long group_id, int cp) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_BLACKLIST_QUN).key("gid")
					.value(group_id).key("cp").value(cp).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 批量获取群信息, 2037号协议
	 * 
	 * @param gids
	 *            群组ID 集合 最多10个
	 * @param m
	 *            区别 101公共/102搜索 /103我的群
	 */
	public void sendGetMoreGroup(List<String> gids, int m) {
		if (gids.size() == 0)
			return;
		StringBuffer sn = new StringBuffer();
		switch (m) {
		case MsgInfor.SERVER_GET_PUBLIC_GROUP:
			sn.append(101);
			break;
		case MsgInfor.SERVER_SEARCH_GROUP:
			sn.append(102);
			break;
		case MsgInfor.SERVER_GET_USER_QUN:
			sn.append(103);
			break;
		}
		// 随即返回一个4位数
		sn.append(Math.round(Math.random() * 10000));
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_MORE_GROUP).key("sn")
					.value(Integer.valueOf(sn.toString())).key("gids").array();
			for (String gid : gids) {
				regster.value(Long.valueOf(gid));
			}
			regster = regster.endArray().endObject();
			sendMessage(regster.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 批量获取用户信息, 93号协议
	 * 
	 * @param gids
	 *            群组ID 集合 最多10个
	 * @param m
	 *            区别 1群成员 2Group黑名单 3黑名单
	 */
	public void sendGetMoreUsers(List<String> uids, int m) {
		if (uids.size() == 0)
			return;
		StringBuffer sn = new StringBuffer();
		switch (m) {
		case MsgInfor.SERVER_INFO_QUN:
			sn.append(1);
			break;
		case MsgInfor.SERVER_GET_BLACKLIST_QUN:
			sn.append(2);
			break;
		case MsgInfor.SERVER_GET_BLACKLIST:
			sn.append(3);
			break;
		}
		// 随即返回一个7位数
		sn.append(Math.round(Math.random() * 10000000));
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_USERSINFO).key("sn")
					.value(Integer.valueOf(sn.toString())).key("ids").array();
			for (String uid : uids) {
				regster.value(Long.valueOf(uid));
			}
			regster = regster.endArray().endObject();
			sendMessage(regster.toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 封id，设备
	 * 
	 * @param uid
	 * @param mobile
	 *            true 封设备 false不封设备
	 */
	public void sendBlock(long uid, boolean mobile) {

		int sn = mobile ? 1 : 0;
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_BLOCK).key("sn").value(sn).key("rs")
					.value("").key("uid").value(uid).key("dev").value(mobile)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 添加删除群组成员，退出群、解散群也是这个接口？ */
	// op---> 0：添加 1：删除
	public void sendDealGroup(long group_id, boolean add, List<Long> partner_ids) {
		try {
			JSONStringer regster;
			if (add) {
				regster = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_DEAL_QUN).key("gid")
						.value(group_id).key("op").value(0).key("idlist")
						.array();
				for (Long id : partner_ids) {
					regster = regster.object().key("uid").value(id).endObject();
				}
			} else {
				regster = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_DEAL_QUN).key("gid")
						.value(group_id).key("op").value(1).key("idlist")
						.array();
				for (Long id : partner_ids) {
					regster = regster.object().key("uid").value(id).endObject();
				}
			}

			regster = regster.endArray().endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 修改群信息 ， 5种信息至少需要一种 2006 号协议
	 * 
	 * @param group_id
	 * @param title
	 * @param intro
	 *            简介
	 * @param avatar
	 * @param type
	 *            群类型：0为半公开，1为公开，2为私密
	 * @param bulletin
	 *            群公告
	 */
	public void sendChargeTitle(long group_id, String title, String intro,
			String avatar, int type, String bulletin) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_TITLE_QUN).key("gid").value(group_id)
					.key("topic").value(title).key("intro").value(intro)
					.key("avatar").value(avatar).key("type").value(type)
					.key("bulletin").value(bulletin).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送群聊消息, 2008号协议
	 * 
	 * @param txmsg
	 *            要发送的消息
	 */
	public void sendGroupMsg(TXMessage txmsg) {
		switch (txmsg.msg_type) {
		case TxDB.MSG_TYPE_QU_COMMON_SMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_AUDIO_EMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_CARD_EMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_GEO_SMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_TCARD_SMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_IMAGE_EMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		case TxDB.MSG_TYPE_QU_GIF_SMS:
			sendMessage(getGroupMsgStr(txmsg));
			break;
		}
		onSendMessage(false, txmsg);
	}

	/**
	 * 2017 申请加入或直接退出群
	 * 
	 * @param group_id
	 * @param op
	 *            0为加入，1为退出
	 * @param rs
	 *            申请理由
	 */
	public void sendJoinQuitGroup(long group_id, String rs) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_JOIN_GROUP).key("gid")
					.value(group_id).key("rs").value(rs).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2019 管理员同意加群申请
	 * 
	 * @param group_id
	 * @param op
	 *            0为加入，1为退出
	 */
	public void sendAgreeGroupReq(long group_id, long uid, String sn,
			String ac, boolean agree, long clisn) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_AGREE_GROUP_REQ).key("gid")
					.value(group_id).key("uid").value(uid).key("sn")
					.value(Long.valueOf(sn)).key("ac").value(ac).key("agree")
					.value(agree).key("clisn").value(clisn).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2021 设置管理员
	 * 
	 * @param group_id
	 * @param uid
	 * @param op
	 *            0设为管理员,1取消管理员权限
	 */
	public void sendSetGroupAdmin(long group_id, long uid, int op) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SET_ADMIN).key("gid").value(group_id)
					.key("uid").value(uid).key("op").value(op).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2023 搜索群
	 * 
	 * @param kw
	 *            关键字
	 * @param type
	 *            搜索类型：0为私人群可搜索，1为公共聊天室
	 */
	public void sendSearchGroup(String kw, int type) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SEARCH_GROUP).key("kw").value(kw)
					.key("gettype").value(type).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2025 加入/取消黑名单
	 * 
	 * @param group_id
	 * @param uid
	 * @param op
	 */
	public void sendSetBlackInGroup(long group_id, long uid, int op) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_BLACK_GROUP).key("gid")
					.value(group_id).key("uid").value(uid).key("op").value(op)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2027 禁言/解除禁言
	 * 
	 * @param group_id
	 * @param uid
	 * @param op
	 *            0禁言,1解除禁言
	 */
	public void sendShutupGroup(long group_id, long uid, int op, int time) {
		try {
			JSONStringer jo = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_SHUTUP_GROUP).key("gid")
					.value(group_id).key("uid").value(uid).key("op").value(op);
			if (op == 0) {
				jo.key("time").value(time);
			}

			sendMessage(jo.endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2029 进入/离开群
	 * 
	 * @param op
	 *            ： 0进入群,1离开群
	 */
	public void sendInOutGroup(long group_id, int op) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_IN_OUT_GROUP).key("gid")
					.value(group_id).key("op").value(op).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2031 获取公共群列表
	 * 
	 * @param page
	 */
	public void sendPublicGroup(int page) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_PUBLIC_GROUP).key("cp")
					.value(page).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2039 个人群设置
	 * 
	 * @param page
	 * @param sn
	 *            0 表示 都是false 1表示 msg = true 2表示 push=true 3表示都是true
	 */
	public void sendGroupRemind(long gid, boolean rcvmsg, boolean rcvpush) {
		int sn = (rcvmsg ? 0 : 1) + (rcvpush ? 0 : 2);
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GROUP_REMIND).key("gid").value(gid)
					.key("rcvmsg").value(rcvmsg).key("rcvpush").value(rcvpush)
					.key("sn").value(sn).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2042 获取公共聊天室在线人员信息
	 * 
	 * @param cp
	 */
	public void sendGetGroupOnlineMember(long gid, int cp) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_PUBLIC_ONLINE_MEMBER).key("gid")
					.value(gid).key("cp").value(cp).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2044 获取公共聊天室最近消息
	 * 
	 * @param cp
	 */
	public void sendGetGroupNewMessage(long gid, String gmid) {
		try {
			JSONStringer js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_PUBLIC_LAST_MESSAGE).key("gid")
					.value(gid);
			if (!Utils.isNull(gmid)) {
				js = js.key("gmid").value(gmid);
			}
			sendMessage(js.endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2046 举报 //TODO 这个怎么和发送群消息字符串（getGroupMsgStr()）合并到一起？因为都是变消息为字符串
	 * 2013.12.18 shc
	 * 
	 * @param cp
	 */
	public void sendReportUser(long userid, long gid, String rs, int t,
			TXMessage txmsg, int sn) {
		try {
			JSONStringer stringer = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_REPORT_USER).key("uid")
					.value(userid).key("rs").value(rs).key("t").value(t)
					.key("gid").value(gid).key("msg").object();
			if (txmsg != null) {
				stringer = stringer.key("sm").object().key("id")
						.value("" + txmsg.msg_id).key("ct")
						.value(txmsg.msg_body).endObject();
				if (txmsg.msg_type == TxDB.MSG_TYPE_COMMON_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {

				} else if (txmsg.msg_type == TxDB.MSG_TYPE_IMAGE_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS) {
					stringer = stringer.key("obj").object().key("img")
							.value(txmsg.msg_url).endObject();
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
					stringer = stringer.key("obj").object().key("img")
							.value("").key("adu").object().key("end")
							.value(txmsg.audio_end).key("url")
							.value(txmsg.msg_url).key("l")
							.value(txmsg.msg_file_length).key("t")
							.value(txmsg.audio_times).endObject().endObject();
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS) {
					stringer = stringer
							.key("obj")
							.object()
							.key("img")
							.value("")
							.key("card")
							.object()
							.key("u")
							.value(txmsg.msg_url)
							.key("d")
							.value(Utils.splitString(
									txmsg.contacts_person_name, 20))
							.endObject().endObject();
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_TCARD_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_TCARD_SMS) {
					stringer = stringer.key("obj").object().key("img")
							.value("").key("tcard").object().key("nn")
							.value(txmsg.tcard_name).key("phone")
							.value(txmsg.tcard_phone).key("avatar")
							.value(txmsg.tcard_avatar_url).key("id")
							.value(txmsg.tcard_id).key("sign")
							.value(txmsg.tcard_sign).key("sex")
							.value(txmsg.tcard_sex).endObject().endObject();
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_GEO_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_GEO_SMS) {
					String geo = txmsg.geo;
					double la = Double.parseDouble(geo.substring(0,
							geo.indexOf(",")));
					double lo = Double.parseDouble(geo.substring(geo
							.indexOf(",") + 1));
					stringer = stringer.key("obj").object().key("img")
							.value("").key("geo").object().key("la").value(la)
							.key("lo").value(lo).endObject().endObject();
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
					// 发送举报的大文件消息
					stringer = stringer.key("obj")
							.object()
							.key("img")
							.value("")
							// 表明不是一个img消息
							.key("file").object().key("url")
							.value(txmsg.msg_url).key("l")
							.value(txmsg.msg_file_length).key("t")
							.value(txmsg.audio_times)// 音频或者视频时长
							.endObject().endObject();
				}
			}
			String regster = stringer.endObject().key("sn").value(sn)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2048 删除群消息
	 * 
	 * @param cp
	 */
	public void sendDeleteGroupMsg(String gmid, long gid) {
		try {
			JSONStringer js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_DELETE_GROUP_MESSAGE).key("gid")
					.value(gid).key("gmid").value(gmid);
			sendMessage(js.endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2050 获取一批聊天室消息ID
	 * 
	 * @param cp
	 */
	public void sendGetGroupMessageIds(long gid, String gmid, int sn) {
		try {
			JSONStringer js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_GROUP_MESSAGE_IDS).key("gid")
					.value(gid);
			if (!Utils.isNull(gmid)) {
				js = js.key("gmid").value(gmid);
			}
			js = js.key("sn").value(sn);
			sendMessage(js.endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2059 获取官方聊天室发言最多人员信息
	 */
	public void sendGetGroupLastWeekStars(long gid, int ver) {
		try {
			String getLastWeekStars = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_GROUP_LAST_WEEK_STARS)
					.key("gid").value(gid).key("ver").value(ver).endObject()
					.toString();
			sendMessage(getLastWeekStars);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获得禁言列表 2057
	 * 
	 * @param gid
	 */
	public void sendGetGroupBanList(long gid, int pageNum) {
		try {
			String js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_BAN_LIST).key("gid").value(gid)
					.key("cp").value(pageNum).endObject().toString();
			sendMessage(js);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获得群公告
	 * 
	 * @param gid
	 */
	public void sendGetGroupNotice(long gid) {
		try {
			String js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_GROUP_NOTICE).key("gid")
					.value(gid).endObject().toString();
			sendMessage(js);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 警告用户
	 * 
	 * @param uid
	 * @param sn
	 */
	public void sendUserWarn(long uid, long sn, String str) {
		try {
			String js = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GROUP_WARN).key("uid").value(uid)
					.key("rs").value(str).key("sn").value(sn).endObject()
					.toString();
			sendMessage(js);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 增加单聊消息到消息列表中, 该消息需要存储数据库并更新缓存, 但不能发送给server 用于上传图片和音频时的显示
	 */
	public void addGroupMessage(TXMessage txmsg) {
		onSendMessage(false, txmsg);
	}

	/**
	 * 获取离线消息 2011
	 * 
	 * @param gid
	 * @param gmid
	 * @param dir
	 *            0 新消息 1 老消息
	 */
	public void sendGetGroupOfflineMsg(long gid, String gmid, int dir) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_OFFLINE_MEG_QUN).key("gid")
					.value(gid).key("gmid").value(gmid).key("dir").value(dir)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 加入黑名单,type: 0添加,1移除 */
	public void sendAddBlackList(long id) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_ADDORDEL_BLACKLIST).key("type")
					.value(0).key("uid").value(id).endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 从黑名单移除,type: 0添加,1移除 */
	public void sendRmvBlackList(long id) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_ADDORDEL_BLACKLIST).key("type")
					.value(1).key("uid").value(id).endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 设置星标好友 */
	public void sendSetStarFriend(long id) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_SET_STAR_FRIEND).key("attr")
					.value(1).key("uid").value(id).endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, "设置星标好友：" + send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 取消星标好友 */
	public void sendCancelStarFriend(long id) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_SET_STAR_FRIEND).key("attr")
					.value(0).key("uid").value(id).endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, "取消星标好友：" + send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 查询用户在线状态
	 */
	public void sendCheckOnline(long[] ids) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_CHECK_ONLINE).key("ids").array();
			for (int i = 0; i < ids.length; i++) {
				regster = regster.value(ids[i]);
			}
			regster = regster.endArray().endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 打招呼
	 */
	public void sendGreet(long partner_id, String text) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GREET).key("to").value(partner_id)
					.key("msg").value(text).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 发送点赞/取消点赞消息
	 * 
	 * @param uid
	 *            被点赞用户id
	 */
	public void sendPraiseMsg(long groupId, long uid, long gmid, int flag) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_PRAISE_MESSAGE).key("gid")
					.value(groupId).key("uid").value(uid).key("gmid")
					.value("" + gmid).key("flag").value(flag).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 获取被点赞消息的消息 */
	public void sendGetPraisedMsgs(long gmid, int page) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_PRAISED_MESSAGES).key("gmid")
					.value("" + gmid).key("page").value(page).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 131获取某人瞬间 */
	public void sendGetBlogMsg(long uid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_BLOG_MESSAGE).key("uid")
					.value(uid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 133批量获取某人瞬间 */
	public void sendGetBlogMsgs(long uid, String mid, int page) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_BLOG_MESLIST).key("uid")
					.value(uid).key("mid").value(mid).key("page").value(page)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 135获取瞬间具体信息 */
	public void sendGetBlogInfo(String mid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_BLOG_INFO).key("mid").value(mid)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 137删除瞬间信息 */
	public void sendDelBlogInfo(String mid, long uid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_DEL_BLOG).key("mid").value(mid)
					.key("uid").value(uid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 141喜欢瞬间信息 */
	public void sendLikedBlogInfo(String mid, long uid, int flag) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_LIKE_BLOG).key("uid").value(uid)
					.key("mid").value(mid).key("flag").value(flag).endObject()
					.toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 145批量获取用户基本资料 */
	public void sendUserInfoList(List<Long> idlist, List<String> fields) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_GET_USERINFOLIST).key("uids")
					.array();
			for (int i = 0; i < idlist.size(); i++) {
				regster = regster.value(idlist.get(i));
			}
			regster = regster.endArray();

			if (fields != null && !fields.isEmpty()) {
				regster.key("fields").array();
				for (String field : fields) {
					regster.value(field);
				}
				regster.endArray();
			}
			regster = regster.endObject();
			String send = regster.toString();
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 139举报瞬间信息 */
	public void sendReportBlogInfo(String mid, long uid, String reason) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_REPORT_BLOG).key("uid").value(uid)
					.key("mid").value(mid).key("reason").value(reason)
					.endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/** 143发布瞬间信息 */
	public void sendReleaseBlog(BlogMsg msg) {
		try {
			JSONStringer regster = null;
			if (msg != null) {
				String img = "";
				if (msg.getImgUrl() != null) {
					img = msg.getImgUrl();
				}
				regster = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLIENT_RELEASE_BLOG).key("mmsg")
						.value(msg.getMmsg()).key("mobj").object().key("img")
						.value(img).key("adu").object();
				String aud_url = "";
				if (msg.getAduUrl() != null && !Utils.isNull(msg.getAduUrl())) {
					aud_url = msg.getAduUrl();
					long aud_time = msg.getAduTime();
					regster = regster.key("url").value(aud_url).key("t")
							.value(aud_time);
				}
				regster.endObject().key("geo").object();
				String geo = msg.getGeo();
				if (!Utils.isNull(geo)) {
					String[] split = new String[2];
					Double la = 0.0;
					Double lo = 0.0;
					if (!Utils.isNull(geo)) {
						split = geo.split(",");
						la = Double.parseDouble(split[0]);
						lo = Double.parseDouble(split[1]);
					}
					regster.key("la").value(la).key("lo").value(lo).key("ct")
							.value(msg.getCity());
				}

				String send = regster.endObject().endObject().key("id")
						.value(msg.getId() + "").endObject().toString();
				sendMessage(send);
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 147 查看了附近的人
	 */
	public void sendViewNearbyPeople() {
		sendClientAction(1, 1);
	}

	/**
	 * 147 操作了微博转发
	 */
	public void sendForwardWeibo() {
		sendClientAction(2, 2);
	}

	/**
	 * 147 客户端事件通知协议，例如：查看了附近的人
	 */
	private void sendClientAction(int type, int sn) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_ACTION_OPREATE).key("type")
					.value(type).key("obj").object().endObject().key("sn")
					.value(sn).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 2013 获取某人参加的所有群
	 */
	public void sendUserQun(long userid) {
		try {
			String regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_GET_USER_QUN).key("uid")
					.value(userid).endObject().toString();
			sendMessage(regster);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 上传Sns好友
	 */
	public void sendUpSNS(int sns_type, String snsId, List<Long> ids,
			boolean isFirst) {
		try {
			JSONStringer regster = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_UP_SNS).key("sns").value(sns_type)
					.key("snsid").value(snsId).key("first").value(isFirst)
					.key("snsuids").array();
			for (Long id : ids) {
				regster = regster.value("" + id);
			}
			regster = regster.endArray().endObject();
			String send = regster.toString();
			if (Utils.debug)
				Log.i(TAG, send);
			sendMessage(send);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 分享的内容为文本
	 */
	public static final int CONTENT = 0;
	/**
	 * 分享的内容为图片
	 */
	public static final int PHOTO = 1;
	/**
	 * 分享的内容为音频
	 */
	public static final int VOICE = 2;

	/**
	 * wap分享协议
	 * 
	 * @param content
	 *            分享内容. 对于图片和音频, 就取链接地址去掉域名和时间戳剩下的相对路径
	 * @param type
	 *            只能是CONTENT, PHOTO, VOICE其中之一, 分别代表文本, 图片, 音频
	 * @param sn
	 *            序号. 客户端自定义, 在90号协议里服务器将返回相同的序号
	 */
	public void sendWapShare(String content, int type, String sn) {
		try {
			String msg = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_WAP_SHARE).key("ct").value(content)
					.key("t").value(type).key("sn").value(sn).endObject()
					.toString();
			sendMessage(msg);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 微博好友比对
	 * 
	 * @param weibo_user_id
	 *            微博账号
	 * @param weibo_token
	 *            微博access_token
	 * @param weibo_token_secret
	 *            微博access_token_secret
	 * @param accountType
	 *            账号类型
	 * @param authType
	 *            auth类型
	 */
	public void sendWeiboFriendCompare(String weibo_user_id,
			String weibo_token, String weibo_token_secret, int accountType,
			int authType) {
		try {
			JSONStringer stringer = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_WEIBO_FRIEND).key("u")
					.value(weibo_user_id).key("t").value(accountType)
					.key("auth").value(authType).key("tk").value(weibo_token);
			if (weibo_token_secret != null) {
				stringer.key("tks").value(weibo_token_secret);
			}
			sendMessage(stringer.endObject().toString());
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理群公告消息, 包括创建, 加入, 退出. 34号协议
	 */
	public void dealGroupNotice(JSONObject jo) {
		try {
			JSONObject obj = jo.getJSONObject("obj");
			if (obj != null) {
				int gid = obj.getInt("gid");
				int gver = obj.getInt("gver");
				int opId = obj.getInt("op");
				String opName = obj.getString("opn");
				long time = obj.getLong("time");
				StringBuffer notifytem = new StringBuffer();
				// TxGroup txgroup = txdata.getTxGroupByGroupId4DB(gid);
				TxGroup txgroup = TxGroup.getTxGroup(
						txdata.getContentResolver(), gid);
				if (txgroup == null) {
					txgroup = new TxGroup();
					txgroup.group_id = gid;
					txgroup.group_ver = gver;
				}
				int type = obj.getInt("type");

				TxGroup.initListArray(txgroup);
				ArrayList<Long> ids = null;
				String[] names = null;
				JSONArray me = obj.optJSONArray("me");
				if (me != null) {
					JSONObject user = null;
					ids = new ArrayList<Long>();
					names = new String[me.length()];
					for (int i = 0; i < me.length(); i++) {
						try {
							user = me.getJSONObject(i);
							long id = user.optLong("i", 0);
							socketHelper.sendGetUserInfor(id);
							String name = user.optString("nn", "");
							notifytem.append(name).append(",");
							ids.add(id);
							names[i] = name;
							// if (type == 1 || type == 2)
							// TX.saveGroupTXtoDB(id);//这个方法内部实现没什么意义。按id从好友缓存或者数据库中查找，找打后在设置一个id存到数据库，已注掉
							// 2013.10.16 shc
						} catch (JSONException e) {
							if (Utils.debug)
								e.printStackTrace();

						}
					}
				}
				if (type == 0) {
					// 表示退出该群
					TXMessage txmsg = null;
					if (gver == 0) {
						// 代表该群的该群已被上次操作删除
						txmsg = TXMessage.createDismissGroup(txdata, gid,
								txgroup.group_title, txgroup.group_avatar,
								opId, opName, time);
						txgroup.group_tx_ids = "";// 把群成员id清空 2014.01.23 shc
						txgroup.changeMembers(txgroup, null, false);
						// txgroup.group_ids_list.clear();
					} else {
						// 被T

						txmsg = TXMessage.createLeaveGroup(txdata, gid,
								txgroup.group_title, txgroup.group_avatar,
								opId, opName, time);

						for (int i = 0; i < ids.size(); i++) {
							txgroup.changeMembers(txgroup, ids.get(i), false);
							// txgroup.group_ids_list.remove(ids.get(i));//群聊天室成员id中删除被踢的人
						}

						if (ids.contains(TX.tm.getUserID())) {
							// txmsg.msg_body="【�slgroup�】"+"("+gid+")"+"的管理员"+opName+"("+opId+")"+"已将您移除该聊天室";
							txmsg.msg_body = opName + "(" + opId + ")"
									+ "把您移除该聊天室【�slgroup�】";
							// 被提出聊天室后，删除聊天室在消息会话列表中的条目, 2014.01.03
							MsgStat.delMsgStatByGroupId(
									mSess.getContentResolver(),
									txgroup.group_id);
						} else {
							for (int i = 0; i < names.length; i++) {
								// txmsg.msg_body="管理员"+opName+"("+opId+")"+"把"+names[i]+"("+ids.get(i)+")"+"移出了聊天室【�slgroup�】"+"("+gid+")";
								txmsg.msg_body = opName + "(" + opId + ")"
										+ "把" + names[i] + "(" + ids.get(i)
										+ ")" + "移除该聊天室【�slgroup�】";
							}
						}
					}

					// 不再统一处理了，分别放在上面的if-else方法中处理 2014.01.23 shc
					// synchronized (txgroup) {
					// txgroup.changeMembers(txgroup, null, false);
					// }

					if (txmsg != null) {
						TXMessage.saveTXMessagetoDB(txmsg,
								mSess.getContentResolver(), true);
						mSess.getSocketHelper().showNotification(txmsg, false);
					}
					if (ids.contains(TX.tm.getUserID())) {
						txgroup.group_tx_state = TxDB.QU_TX_STATE_OUT;
					}

					// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
					values.put(TxDB.Qun.ALL_NUM, txgroup.group_all_num);
					values.put(TxDB.Qun.QU_TX_STATE, txgroup.group_tx_state);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txgroup.group_id, values);

				} else if (type == 1) {
					// 被邀请进入某个聊天室
					if (ids.contains(TX.tm.getUserID())) {
						TXMessage txmsg = TXMessage.createInGroup(txdata, gid,
								txgroup.group_title, txgroup.group_avatar,
								opId, opName, time);
						txmsg.msg_body = opName + "(" + opId + ")"
								+ "把您加入聊天室【�slgroup�】";
						TXMessage.saveTXMessagetoDB(txmsg,
								mSess.getContentResolver(), true);
						mSess.getSocketHelper().showNotification(txmsg, false);
					}
					for (int i = 0; i < ids.size(); i++) {
						txgroup.changeMembers(txgroup, ids.get(i), true);
					}
					txgroup.group_tx_state = TxDB.QU_TX_STATE_CM;
					// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
					values.put(TxDB.Qun.ALL_NUM, txgroup.group_all_num);
					values.put(TxDB.Qun.QU_TX_STATE, txgroup.group_tx_state);
					TxGroup dbgroup = TxGroup.updateTxGroup(
							mSess.getContentResolver(), txgroup.group_id,
							values);
					if (dbgroup == null) {
						// 没有更新成功，数据库中没有该群组
						if (Utils.debug)
							Log.i(TAG, "数据库中没有群组:" + txgroup.group_id + ","
									+ txgroup.group_title);
						TxGroup.addTxGroup(mSess.getContentResolver(), txgroup);
						sendGetGroup(txgroup.group_id);// 获取群资料

					}

				} else if (type == 2) {
					// 表示(创建)创建该群
					for (int i = 0; i < ids.size(); i++) {
						txgroup.changeMembers(txgroup, ids.get(i), true);
					}
					txgroup.group_tx_state = TxDB.QU_TX_STATE_CM;
					// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法
					// 2014.01.23 shc
					ContentValues values = new ContentValues();
					values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
					values.put(TxDB.Qun.ALL_NUM, txgroup.group_all_num);
					values.put(TxDB.Qun.QU_TX_STATE, txgroup.group_tx_state);
					TxGroup.updateTxGroup(mSess.getContentResolver(),
							txgroup.group_id, values);

					if (ids.contains(TX.tm.getTxMe().partner_id)) {
						TXMessage txmsg = TXMessage.createInGroup(txdata, gid,
								"", txgroup.group_avatar, opId, opName, time);
						TXMessage.saveTXMessagetoDB(txmsg,
								mSess.getContentResolver(), true);
						mSess.getSocketHelper().showNotification(txmsg, false);
					}

				}

				Handler handler = groupHandlers.get(gid);
				if (handler != null) {
					flushUIWithGroupInfor(txgroup, handler);
				}
				mSess.getSocketHelper().sendGetGroup(gid);
				Intent intent = new Intent(Constants.INTENT_ACTION_FLUSH_GROUP);
				txdata.sendBroadcast(intent);
			}
		} catch (NumberFormatException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 删除单个单聊消息
	 */
	public void deleteSingleMessage(long singleID, long msgID) {
		TXMessage.deleteByMsgId(cr, msgID);
		onDeleteMessage(true, singleID, msgID);
	}

	/**
	 * 删除所有单聊消息
	 */
	public void deleteSingleMessageAll(long singleID) {
		onDeleteAllMessage(true, singleID);
	}

	/**
	 * 删除单个群聊消息
	 */
	public void deleteGroupMessage(long groupID, long msgID) {
		TXMessage.deleteByMsgId(cr, msgID);
		// TXMessage.deleleByGmid(cr, msgID);
		onDeleteMessage(false, groupID, msgID);
	}

	/**
	 * 删除所有群聊消息
	 */
	public void deleteGroupMessageAll(long groupID) {
		onDeleteAllMessage(false, groupID);
	}

	/**
	 * 改变单聊消息状态
	 */
	public void changeSingleMessageState(long singleID, long msgID, int state) {
		onUpdateMessageState(true, singleID, msgID, state);
	}

	/**
	 * 改变群聊消息状态
	 */
	public void changeGroupMessageState(long groupID, long msgID, int state) {
		onUpdateMessageState(false, groupID, msgID, state);
	}

	/**
	 * 处理群组信息, 2034号协议
	 */
	public void dealGroupInfo(JSONObject jo) {
		try {
			ServerRsp serverRsp = new ServerRsp();
			int d = jo.getInt("d");
			if (d == 1) {
				serverRsp
						.setStatusCode(StatusCode.GROUP_MODIFY_GROUP_NOT_EXIST);
			} else if (d == 2) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			} else {
				long group_id = jo.getLong("gid");// 群号
				int group_ver = jo.getInt("gver");// 群版本
				String intro = jo.getString("intro"); // 群简介
				String bulletin = jo.getString("bulletin");// 群公告
				String topic = jo.getString("topic"); // 群标题
				String avatar = jo.getString("avatar"); // 群头像
				JSONArray me = jo.getJSONArray("me"); // 群成员，不包括群主及管理员
				long group_time = jo.getLong("ctime");// 群创建时间
				JSONArray adm = jo.getJSONArray("adm"); // 群管理员
				int group_own_id = jo.getInt("owner"); // 群主ID
				String group_own_name = jo.getString("ownn"); // 群主名字
				JSONArray ban = jo.getJSONArray("ban");
				int type = jo.getInt("t");

				int currentPageNum = jo.getInt("mecp");// 返回的群成员当前页数
				int totalPageCount = jo.getInt("meep");// 返回的群成员页数总数（返回群成员时，服务器做了分页）

				int len = me.length();
				StringBuffer group_tx_ids = new StringBuffer();
				for (int i = 0; i < len; i++) {
					group_tx_ids.append(me.getLong(i)).append("�");
				}

				len = adm.length();
				StringBuffer group_adm_ids = new StringBuffer();
				StringBuffer group_adm_names = new StringBuffer();
				for (int i = 0; i < len; i++) {
					JSONObject joo = adm.getJSONObject(i);
					group_adm_names.append(joo.optString("n", "")).append("�");
					group_adm_ids.append(joo.optString("i", "")).append("�");
				}

				StringBuffer ban_sb = new StringBuffer();
				for (int i = 0; i < ban.length(); i++) {
					ban_sb.append(ban.getLong(i)).append("�");
				}

				TxGroup txgroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), (int) group_id);
				if (txgroup == null) {
					txgroup = new TxGroup();
				}
				txgroup.group_id = group_id;
				txgroup.group_ver = group_ver;
				txgroup.group_title = topic;
				txgroup.group_own_id = group_own_id;
				txgroup.group_own_name = group_own_name;
				txgroup.group_time = group_time;
				txgroup.group_sign = intro;
				txgroup.group_bulletin = bulletin;
				txgroup.group_avatar = avatar;
				txgroup.group_tx_ids = group_tx_ids.toString();
				txgroup.group_tx_admin_ids = group_adm_ids.toString();
				txgroup.group_tx_admin_names = group_adm_names.toString();
				txgroup.ban_ids = ban_sb.toString();
				txgroup.group_type_channel = type;
				txgroup.group_ids_list.clear();
				txgroup = TxGroup.initListArray(txgroup);

				// MsgStat ms1 = MsgStat.getMsgStatByGroupid(txgroup.group_id);
				// if (ms1 != null) {
				// ms1.group_name = txgroup.group_title;
				// ms1.group_display_avatars = txgroup.group_avatar;
				// }

				// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_VER, txgroup.group_ver);
				values.put(TxDB.Qun.QU_DISPLAY_NAME, txgroup.group_title);
				values.put(TxDB.Qun.QU_OWN_ID, txgroup.group_own_id);
				values.put(TxDB.Qun.QU_OWN_NAME, txgroup.group_own_name);
				values.put(TxDB.Qun.QU_TIME, txgroup.group_time);
				values.put(TxDB.Qun.QU_SIGN, txgroup.group_sign);
				values.put(TxDB.Qun.QU_BULLETIN, txgroup.group_bulletin);
				values.put(TxDB.Qun.QU_AVATAR, txgroup.group_avatar);
				values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
				values.put(TxDB.Qun.QU_TX_ADMIN_IDS, txgroup.group_tx_admin_ids);
				values.put(TxDB.Qun.QU_TX_ADMIN_NAMES,
						txgroup.group_tx_admin_names);
				values.put(TxDB.Qun.QU_TYPE_CHANNEL, txgroup.group_type_channel);

				if (TxGroup.updateTxGroup(mSess.getContentResolver(), group_id,
						values) == null) {
					// 更新失败，添加
					values.put(TxDB.Qun.QU_ID, txgroup.group_id);
					TxGroup.addTxGroup(mSess.getContentResolver(), txgroup);
				}

				// 更新消息会话数据库中的内容 2014.03.13 shc
				MsgStat.updateGroupInfo(mSess.getContentResolver(), txgroup);

				serverRsp.setStatusCode(StatusCode.RSP_OK);

				Bundle bundle = serverRsp.getBundle();
				bundle.putParcelable(Utils.MSGROOM_TX_GROUP, txgroup);
				bundle.putInt(GroupMember.CURRENT_PAGE_NUM, currentPageNum);
				bundle.putInt(GroupMember.END_PAGE_NUM, totalPageCount);
				if (group_id == currentGroupID.get()) {
					Handler handler = groupHandlers.get(group_id);
					if (handler != null) {
						flushUIWithGroupInfor(txgroup, handler);
					}
				}
			}

			broadcastMsg(Constants.INTENT_ACTION_GET_GROUP, serverRsp);

		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 处理单聊消息回执, 36号协议
	 */
	public void dealSingleMessageReceipt(JSONObject jo) {
		try {
			long partnerid = jo.getInt("from");
			if (partnerid == 0 && jo.getInt("s") == 3) {
				partnerid = TX.TUIXIN_MAN;
			}
			String msgid = jo.getString("id");
			int readState = jo.getInt("d");
			onUpdateMessageState(true, partnerid, Long.valueOf(msgid),
					readState);
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理消息状态改变
	 * 
	 * @param isSingle
	 *            是单聊还是群聊
	 * @param id
	 *            如果是单聊该id表示partnerid, 否则表示groupid
	 * @param msgid
	 *            消息id
	 * @param readState
	 *            消息状态
	 */
	private void onUpdateMessageState(boolean isSingle, long id, long msgid,
			int readState) {
		AtomicLong currentID = null;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			managers = groupManagers;
			handlers = groupHandlers;
		}

		TXMessage.updateByMsgId(mSess.getContentResolver(), msgid, readState);
		if (id == currentID.get()) {
			MessageListManager mana = managers.get(id);
			if (mana != null) {
				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					for (TXMessage txmsg : txmsgs) {
						if (txmsg.msg_id == msgid) {
							txmsg.read_state = readState;
							Handler handler = handlers.get(id);
							if (handler != null) {
								// 音频消息发送成功时需要播放提示音
								boolean needVoicePrompt = false;
								if (readState == TXMessage.SENT
										&& (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS || txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS)) {
									needVoicePrompt = true;
								}
								flushUIWithUpdate(handler, needVoicePrompt);
							}
							break;
						}
					}
				}
			}
		}
	}

	private void onUpdateMessageState(boolean isSingle, long id, long msgid,
			int readState, long gmid) {
		TXMessage.updateByMsgId(mSess.getContentResolver(), msgid, readState,
				gmid);
		// onUpdateMessageState(isSingle, id, msgid, readState);//再确定一下这个方法怎么修改
		// 2013.11.07 shc

		{
			// 临时这么写，为了给list中的msg设置gmid

			AtomicLong currentID = null;
			Map<Long, MessageListManager> managers = null;
			Map<Long, Handler> handlers = null;
			if (isSingle) {
				currentID = currentSingleID;
				managers = singleManagers;
				handlers = singleHandlers;
			} else {
				currentID = currentGroupID;
				managers = groupManagers;
				handlers = groupHandlers;
			}

			TXMessage.updateByMsgId(mSess.getContentResolver(), msgid,
					readState);
			if (id == currentID.get()) {
				MessageListManager mana = managers.get(id);
				if (mana != null) {
					ArrayList<TXMessage> txmsgs = mana.getMessageList();
					synchronized (txmsgs) {
						for (TXMessage txmsg : txmsgs) {
							if (txmsg.msg_id == msgid) {
								txmsg.read_state = readState;
								txmsg.gmid = gmid;
								Handler handler = handlers.get(id);
								if (handler != null) {
									// 音频消息发送成功时需要播放提示音
									boolean needVoicePrompt = false;
									if (readState == TXMessage.SENT
											&& (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS || txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS)) {
										needVoicePrompt = true;
									}
									flushUIWithUpdate(handler, needVoicePrompt);
								}
								break;
							}
						}
					}
				}
			}

		}

	}

	/**
	 * 处理单聊已读回执列表, 29号协议
	 */
	public void dealSingleReadList(JSONObject jo) {
		try {
			long partnerid = jo.getInt("from");
			if (partnerid == 0 && jo.getInt("s") == 3) {
				partnerid = TX.TUIXIN_MAN;
			}
			JSONArray ja = jo.getJSONArray("rd");
			for (int i = 0; i < ja.length(); i++) {
				JSONObject joo = ja.getJSONObject(i);
				String msgid = joo.getString("id");
				onUpdateMessageState(true, partnerid, Long.valueOf(msgid),
						TXMessage.READ);
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理单聊离线回执返回, 13号协议
	 */
	public void dealSingleOfflineReceipt(JSONObject jo) {
		try {
			JSONArray ja = jo.getJSONArray("rp");
			for (int i = 0; i < ja.length(); i++) {
				JSONObject joo = ja.getJSONObject(i);
				long partnerid = joo.getInt("from");
				if (partnerid == 0 && joo.getInt("subid") == 3) {
					partnerid = TX.TUIXIN_MAN;
				}
				String msgid = joo.getString("id");
				int readState = joo.getInt("d");
				onUpdateMessageState(true, partnerid, Long.valueOf(msgid),
						readState);
			}

			if (jo.getInt("eof") == 0) {
				// String id = prefs.getString(CommonData.USER_ID, "");
				String id = mSess.mPrefMeme.user_id.getVal();
				if (!"".equals(id)) {
					sendGetOffLineReceipt(Integer.parseInt(id));
				}
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理单聊离线消息返回, 11号协议
	 */
	public void dealSinleOfflineMessage(JSONObject jo) {
		try {
			JSONArray ja = jo.getJSONArray("ms");
			for (int i = 0; i < ja.length(); i++) {
				JSONObject joo = ja.getJSONObject(i);
				dealSingleReceiveMessage(joo, true);
			}

			if (jo.getInt("eof") == 0) {
				// String id = prefs.getString(CommonData.USER_ID, "");
				String id = mSess.mPrefMeme.user_id.getVal();
				if (!"".equals(id)) {
					sendGetSingleOfflineMsg(Integer.parseInt(id));
				}
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理获取到的聊天室最近的消息 2045
	 * 
	 * @param jo
	 */
	public boolean dealGroupLastMessage(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			int gid = jo.getInt("gid");
			if (d == 0) {
				JSONArray ja = jo.getJSONArray("ms");
				// ja的顺序是按照时间的从小到大，即从前到后的顺序
				for (int i = 0; i < ja.length(); i++) {
					JSONObject joo = ja.getJSONObject(i);
					int v = dealGroupReceiveMessage(joo,
							MsgInfor.SERVER_PUBLIC_LAST_MESSAGE, gid);
					if (Utils.debug)
						Log.i(TAG, "群最新消息：v:" + v + ",i=" + i);
					// if (v > 0) {
					// // v:-1表示出错，0，正常，1表示本地存在此消息(根据消息id判断)
					// break;// 直接break有历史原因，好像是防止重复添加本地历史消息 2014.01.20
					// }
				}
				return true;// 正常解析完最新10条消息
			} else if (d == 1) {
				// 不是公共聊天室 统一传失败
			} else if (d == 2) {
				// 不存在
			} else if (d == 3) {
				// 失败
			}
		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "解析聊天室最新Json消息异常", e);
		}
		return false;
	}

	/** 发送一个虚假消息在聊提示显示上周之星 */
	public void sendShamMsgToShowLaskWeekStars(long gid) {
		try {
			JSONObject starobj = new JSONObject();
			starobj.put("t", "" + System.currentTimeMillis() / 1000);// 消息时间
			starobj.put("atlist", "");
			starobj.put("snm", TX.tm.getTxMe().getNick_name());
			starobj.put("ssex", TX.tm.getTxMe().getSex());
			starobj.put("from", TX.tm.getUserID());
			starobj.put("siver", 4);// 这个字段好像整个工程都没有解析
			starobj.put("gmid", 14745689);// 这个值随便写应该没事儿，因为收到此消息还要重新给它赋值
											// 2014.01.22 shc
			JSONObject smobj = new JSONObject();
			// 消息id取当前时间应该没有问题，此不应该存到数据库中 2014.01.23 shc
			smobj.put("id", "" + System.currentTimeMillis());
			smobj.put("ct", "");
			starobj.put("sm", smobj);

			JSONObject tyoeobj = new JSONObject();
			JSONObject shamobj = new JSONObject()
					.put("sham",
							"this is a sham message for show last week stars in groupmsgroom");
			tyoeobj.put("img", "");
			tyoeobj.put("star", shamobj);
			starobj.put("obj", tyoeobj);

			if (Utils.debug)
				Log.e(TAG, "生成的虚拟消息Json串为：" + starobj.toString());

			int v = dealGroupReceiveMessage(starobj,
					MsgInfor.SERVER_PUBLIC_LAST_MESSAGE, gid);
		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "生成上周之星虚拟消息时异常：", e);
		}

	}

	/**
	 * 处理群聊离线消息返回, 2012号协议
	 */
	// long minMsgId = 0;
	private Map<Long, Long> groupOfflineGmid = new HashMap<Long, Long>();

	public String getMinGmid(long gid) {
		Long gmid = groupOfflineGmid.get(gid);
		if (gmid == null || gmid == 0) {
			return "";
		} else {
			return "" + gmid;
		}
	}

	/** 应该是获取群离线消息 */
	public void dealGroupOfflineMessage(JSONObject jo) {
		try {
			// List<String> msgidList = new ArrayList<String>();
			long gid = jo.getLong("gid");
			JSONArray ja = jo.getJSONArray("ms");
			int eof = jo.getInt("eof");
			// msgidList.clear();
			long minMsgId = 0;
			for (int i = 0; i < ja.length(); i++) {
				JSONObject joo = ja.getJSONObject(i);
				long temp = joo.optLong("gmid", 0);
				if (minMsgId != 0 && temp < minMsgId) {
					minMsgId = temp;
				} else if (minMsgId == 0) {
					minMsgId = temp;
				}
				// msgidList.add(joo.getJSONObject("sm").getString("id"));
				dealGroupReceiveMessage(joo, 2012, gid);
			}
			groupOfflineGmid.put(gid, minMsgId);

			Map<Long, Handler> handlers = groupHandlers;
			Handler handler = handlers.get(gid);
			if (handler != null) {
				flushUIWithAddOld(ja.length(), handler, 0);
			}

			// sendGroupOffLineReceipt(msgidList);
			if (jo.getInt("eof") == 0) {
				// String id = prefs.getString(CommonData.USER_ID, "");
				String id = mSess.mPrefMeme.user_id.getVal();
				if (!"".equals(id)) {
					// sendGetGroupOfflineMsg();
				}
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 接收到server转发的消息, 8号协议
	 * 
	 * @param offLine
	 *            是否是离线消息
	 */
	public void dealSingleReceiveMessage(JSONObject jo, boolean offLine) {
		try {
			String displayname = "";
			// String phone = "";
			// int sex = -1;//这个变量没有引用 2014.01.16
			long partnerId = jo.getLong("from");

			if (partnerId == 0 && jo.getInt("s") == 3) {
				partnerId = TX.TUIXIN_MAN;
				displayname = txdata.getResources().getString(
						R.string.tuixinman);
			}
			// final int partnerId = ""+id;
			String time = jo.getString("t");
			Long ltime = Long.parseLong(time);
			/*
			 * int cli = jo.optInt("cli", -1); String sendend = "";
			 */
			/*
			 * if (cli != -1) { if (cli >= 0 && cli < 100) { // pc sendend =
			 * txdata.getString(R.string.send_end_pc); } else if (cli >= 100 &&
			 * cli < 200) { // mac os } else if (cli >= 200 && cli < 300) { //
			 * linux } else if (cli >= 300 && cli < 400) { // windows mobile }
			 * else if (cli >= 400 && cli < 500) { // linux mobile } else if
			 * (cli >= 500 && cli < 600) { // IOS sendend =
			 * txdata.getString(R.string.send_end_ios); } else if (cli >= 600 &&
			 * cli < 700) { // android } else if (cli >= 700 && cli < 800) { //
			 * symbian sendend = txdata.getString(R.string.send_end_mobile); }
			 * else if (cli == 1000) { // web } }
			 */
			TXMessage txmsg = null;
			JSONObject obj = jo.optJSONObject("obj");
			String img_downlod = "";
			String img = "";
			String audio_path = "";
			String audio_url = "";
			String audioend = "";
			long audio_length = 0;
			long audio_times = 0;
			String geo = "";
			int msg_type = TxDB.MSG_TYPE_COMMON_SMS;
			String cardUrl = "";
			String cardfile = "";
			String cardName = "";
			long tcard_id = -1;
			String tcard_name = "";// 名片姓名
			String tcard_sign = "";// 名片签名
			int tcard_sex = TX.MALE_SEX;// 名片性别
			String tcard_avatar_url = "";
			String tcard_phone = "";// 名片电话
			String file_url = "";// 单聊大文件的下载地址
			int file_length = 0;// 单聊大文件的文件大小
			int pkgid = -1;
			String emomd5 = "";

			if (obj != null) {
				img_downlod = obj.optString("img", "");
				if (!Utils.isNull(img_downlod)) {
					img = receiveSameImg(img_downlod);
					msg_type = TxDB.MSG_TYPE_IMAGE_EMS;
				}
				JSONObject vcard = obj.optJSONObject("card");
				if (vcard != null) {
					cardUrl = vcard.optString("u", "");
					if (Utils.debug)
						Log.i(TAG, "cardUrl+++++++" + cardUrl);
					cardName = vcard.optString("d", "");
					if (!Utils.isNull(cardUrl)) {
						cardfile = receiveSameCard(cardUrl);
						msg_type = TxDB.MSG_TYPE_CARD_EMS;
					}
				}
				JSONObject tcard = obj.optJSONObject("tcard");
				if (tcard != null) {
					msg_type = TxDB.MSG_TYPE_TCARD_SMS;
					tcard_name = tcard.optString("nn", "");
					tcard_id = tcard.optInt("id", -1);
					tcard_sign = tcard.optString("sign", "");
					tcard_sex = tcard.optInt("sex", TX.MALE_SEX);
					tcard_avatar_url = tcard.optString("avatar", "");
					tcard_phone = tcard.optString("phone", "");
				}
				JSONObject adu = obj.optJSONObject("adu");
				if (adu != null) {
					audio_url = adu.optString("url", "");
					if (!Utils.isNull(audio_url)) {
						audio_path = receiveSameAudio(audio_url);
						msg_type = TxDB.MSG_TYPE_AUDIO_EMS;
					}
					audioend = adu.optString("end", "");
					audio_length = adu.optLong("l", 0);
					audio_times = adu.optLong("t", 0);
					if (Utils.debug)
						Log.i(TAG, "adu+++++++++++++++++++++" + adu);
				}
				JSONObject geo_jsonObj = obj.optJSONObject("geo");
				if (geo_jsonObj != null) {
					double la = geo_jsonObj.optDouble("la", 0);
					double lo = geo_jsonObj.optDouble("lo", 0);
					geo = la + "," + lo;
					if (la == 0 && lo == 0)
						geo = "";
					msg_type = TxDB.MSG_TYPE_GEO_SMS;
				}

				// 单聊大文件消息
				JSONObject bigFile_jsonObj = obj.optJSONObject("file");
				if (bigFile_jsonObj != null) {
					msg_type = TxDB.MSG_TYPE_BIG_FILE_SMS;
					file_url = bigFile_jsonObj.optString("url");
					file_length = bigFile_jsonObj.optInt("l");
				}
				JSONObject gif_jsonObj = obj.optJSONObject("emot");
				if (gif_jsonObj != null) {
					msg_type = TxDB.MSG_TYPE_SMS_GIF;
					emomd5 = gif_jsonObj.optString("emomd5");
					pkgid = gif_jsonObj.optInt("pkgid");
				}

			}
			JSONObject sms = jo.getJSONObject("sm");
			if (sms != null) {
				String smsid = sms.getString("id");
				// if (TX.inBlacklist(partnerId)) {
				// return;
				// }
				if (TX.tm.isInBlack(partnerId)) {
					return;
				}
				Long sms_id = Long.parseLong(smsid);
				String sms_content = sms.getString("ct");

				TX tempTx = TX.tm.getTx(partnerId);
				if (tempTx != null) {
					displayname = tempTx.getNick_name();
				} else {
					if (displayname.equals(""))
						displayname = "" + partnerId;
				}

				int readState = TXMessage.UNREAD;
				if (msg_type != TxDB.MSG_TYPE_AUDIO_EMS
						&& partnerId == currentSingleID.get()) {
					readState = TXMessage.READ;
				}
				// 向非客服用户发送消息回执
				// if (smsid.length() != 10) {
				mSess.getSocketHelper()
						.sendReceipt(partnerId, smsid, readState);
				// }
				switch (msg_type) {
				case TxDB.MSG_TYPE_COMMON_SMS:
					txmsg = TXMessage.creatCommonSmsWhenReceive(txdata, sms_id,
							partnerId, displayname, sms_content, false,
							readState, ltime);
					break;
				case TxDB.MSG_TYPE_AUDIO_EMS:
					txmsg = TXMessage.creatAudioEms(txdata, sms_id, partnerId,
							displayname, audio_path, audio_url, audio_length,
							audio_times, audioend, false, TXMessage.UNREAD,
							ltime);
					break;
				case TxDB.MSG_TYPE_CARD_EMS:
					txmsg = TXMessage.creatCardEms(txdata, sms_id, cardName,
							partnerId, displayname, cardfile, cardUrl, false,
							readState, ltime);
					break;
				case TxDB.MSG_TYPE_IMAGE_EMS:
					txmsg = TXMessage.creatImageEms(txdata, sms_id, partnerId,
							displayname, img, img_downlod, false, readState,
							ltime);
					break;
				case TxDB.MSG_TYPE_GEO_SMS:
					txmsg = TXMessage.creatGeoSms(txdata, sms_id, partnerId,
							displayname, geo, false, readState, ltime);
					break;
				case TxDB.MSG_TYPE_TCARD_SMS:
					txmsg = TXMessage.creatTCardEms(txdata, sms_id, tcard_name,
							partnerId, displayname, tcard_name, tcard_id,
							tcard_sign, tcard_sex, tcard_phone,
							tcard_avatar_url, false, readState, ltime);
					break;
				case TxDB.MSG_TYPE_BIG_FILE_SMS:
					txmsg = TXMessage.creatBigFileSmsWhenReceive(txdata,
							sms_id, partnerId, displayname, file_url,
							file_length, ltime);
					break;
				case TxDB.MSG_TYPE_SMS_GIF:
					txmsg = TXMessage.creatGifSmsWhenReceive(txdata, sms_id,
							partnerId, displayname, ltime, pkgid, emomd5);
					break;
				}
				showNotification(txmsg, offLine);
				autoDownload(txmsg);
				onReceiveMessageFromServer(txmsg, true, offLine);
			}
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * 接收到server转发的消息, 2010号协议
	 * 
	 * @param isOffline
	 *            表明该方法是因收到2010号协议消息还是2012号协议消息而被调用的
	 * @return -1表示出错，0，正常，1表示本地存在此消息(根据消息id判断)
	 */
	public int dealGroupReceiveMessage(JSONObject jo, int mt, long gid) {
		try {
			boolean isOffline = mt == 2012 ? true : false;
			String displayname = "";
			String tx_avatarUrl = "";
			String groupname = "";
			int sex = -1;
			String nickName = "";

			// 根据版本号决定是否取群组信息

			long groupid = 0;
			if (mt == 2012 || mt == 2045) {
				groupid = gid;// jo.getLong("from");
			} else {
				// 2010
				groupid = jo.getLong("from");

			}
			long gmid = jo.optLong("gmid", 0);
			TxGroup txgroup = TxGroup.getTxGroup(txdata.getContentResolver(),
					groupid);
			if (txgroup == null) {
				mSess.getSocketHelper().sendGetGroup(groupid);
			} else {
				groupname = txgroup.group_title;
			}
			sex = jo.optInt("ssex", -1);
			displayname = jo.optString("snm");
			String time = jo.getString("t");
			Long ltime = System.currentTimeMillis();
			if (!Utils.isNull(time))
				ltime = Long.parseLong(time);
			long senderid = 0;
			if (mt == 2012 || mt == 2045) {
				senderid = jo.getLong("from");
				sex = jo.optInt("ssex", -1);
				displayname = jo.optString("snm");
			} else {
				senderid = jo.optLong("sid");
			}
			String sendend = "";
			TXMessage txmsg = null;
			JSONObject obj = jo.optJSONObject("obj");
			String img_downlod = "";
			String img = "";
			String audio_path = "";
			String audio_url = "";
			String audioend = "";
			long audio_length = 0;
			long audio_times = 0;
			String geo = "";
			int msg_type = TxDB.MSG_TYPE_QU_COMMON_SMS;
			String cardUrl = "";
			String cardfile = "";
			String cardName = "";
			long tcard_id = -1;
			String tcard_name = "";// 名片姓名
			String tcard_sign = "";// 名片签名
			int tcard_sex = TX.MALE_SEX;// 名片性别
			String tcard_avatar_url = "";
			String tcard_phone = "";
			String file_url = "";// 大文件的下载地址
			int file_length = 0;// 大文件的文件大小
			int pkgid = -1;// gif id
			String emomd5 = "";// gif md5值

			if (obj != null) {
				img_downlod = obj.optString("img", "");
				if (Utils.debug) {
					Log.i(TAG, "群组重复图片的地址：" + img_downlod);
				}
				if (!Utils.isNull(img_downlod)) {
					img = receiveSameImg(img_downlod);
					msg_type = TxDB.MSG_TYPE_QU_IMAGE_EMS;
				}
				JSONObject vcard = obj.optJSONObject("card");
				if (vcard != null) {
					cardUrl = vcard.optString("u", "");
					cardName = vcard.optString("d", "");
					if (!Utils.isNull(cardUrl)) {
						cardfile = receiveSameCard(cardUrl);
						msg_type = TxDB.MSG_TYPE_QU_CARD_EMS;
					}
				}
				JSONObject tcard = obj.optJSONObject("tcard");
				if (tcard != null) {
					msg_type = TxDB.MSG_TYPE_QU_TCARD_SMS;
					tcard_name = tcard.optString("nn", "");
					tcard_id = tcard.optInt("id", -1);
					tcard_sign = tcard.optString("sign", "");
					tcard_sex = tcard.optInt("sex", TX.MALE_SEX);
					tcard_avatar_url = tcard.optString("avatar", "");
					tcard_phone = tcard.optString("phone", "");
				}
				JSONObject adu = obj.optJSONObject("adu");
				if (adu != null) {
					audio_url = adu.optString("url", "");
					if (!Utils.isNull(audio_url)) {
						audio_path = receiveSameAudio(audio_url);
						msg_type = TxDB.MSG_TYPE_QU_AUDIO_EMS;
					}
					audioend = adu.optString("end", "");
					audio_length = adu.optLong("l", 0);
					audio_times = adu.optLong("t", 0);
				}
				JSONObject geo_jsonObj = obj.optJSONObject("geo");
				if (geo_jsonObj != null) {
					double la = geo_jsonObj.optDouble("la", 0);
					double lo = geo_jsonObj.optDouble("lo", 0);
					geo = la + "," + lo;
					if (la == 0 && lo == 0)
						geo = "";
					msg_type = TxDB.MSG_TYPE_QU_GEO_SMS;
				}
				JSONObject star_jsonObj = obj.optJSONObject("star");
				if (star_jsonObj != null) {
					// 只要不为空就设置消息类型
					msg_type = TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS;
				}
				// 大文件消息
				JSONObject bigFile_jsonObj = obj.optJSONObject("file");
				if (bigFile_jsonObj != null) {
					msg_type = TxDB.MSG_TYPE_QU_BIG_FILE_SMS;
					file_url = bigFile_jsonObj.optString("url");
					file_length = bigFile_jsonObj.optInt("l");
				}
				JSONObject gif_jsonObj = obj.optJSONObject("emot");
				if (gif_jsonObj != null) {
					msg_type = TxDB.MSG_TYPE_QU_GIF_SMS;
					emomd5 = gif_jsonObj.optString("emomd5");
					pkgid = gif_jsonObj.optInt("pkgid");
				}
			}
			JSONObject sms = jo.getJSONObject("sm");
			if (sms != null) {
				String smsid = sms.getString("id");
				if (TX.tm.isInBlack(senderid)) {
					return -1;
				}
				Long sms_id = Long.parseLong(smsid);
				if (TXMessage.findTXMessageByMsgid(cr, smsid) != null)
					return 1;
				String sms_content = sms.getString("ct") + sendend;

				TX tx = TX.tm.getTx(senderid);

				if (tx != null) {
					tx_avatarUrl = tx.avatar_url;
				}

				switch (msg_type) {
				case TxDB.MSG_TYPE_QU_COMMON_SMS:
					txmsg = TXMessage.creatGroupCommonSmsWhenReceive(
							this.txdata, sms_id, senderid, displayname,
							groupid, groupname, tx_avatarUrl, sms_content,
							false, 2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_AUDIO_EMS:
					txmsg = TXMessage.creatGroupAudioEmsWhenReceive(
							this.txdata, sms_id, senderid, displayname,
							groupid, groupname, tx_avatarUrl, audio_path,
							audio_url, audio_length, audio_times, audioend,
							false, 2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_CARD_EMS:
					txmsg = TXMessage.creatGroupCardEmsWhenReceive(this.txdata,
							sms_id, cardName, senderid, displayname, groupid,
							groupname, tx_avatarUrl, cardfile, cardUrl, false,
							2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:
					txmsg = TXMessage.creatGroupImageEmsWhenReceive(
							this.txdata, sms_id, senderid, displayname,
							groupid, groupname, tx_avatarUrl, img, img_downlod,
							false, 2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_GEO_SMS:
					txmsg = TXMessage.creatGroupGeoSmsWhenReceive(this.txdata,
							sms_id, senderid, displayname, groupid, groupname,
							tx_avatarUrl, geo, false, 2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_TCARD_SMS:
					txmsg = TXMessage.creatGroupTCardEmsWhenReceive(
							this.txdata, sms_id, tcard_name, senderid,
							displayname, groupid, groupname, tx_avatarUrl,
							tcard_name, tcard_id, tcard_sign, tcard_sex,
							tcard_phone, tcard_avatar_url, false, 2, ltime);
					break;
				case TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS:
					// 上周之星的消息，其实是个假消息
					txmsg = TXMessage.creatGroupCommonSmsWhenReceive(
							this.txdata, sms_id, senderid, displayname,
							groupid, groupname, tx_avatarUrl, sms_content,
							false, 2, ltime);
					txmsg.msg_type = TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS;
					break;
				case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
					txmsg = TXMessage.creatGroupBigFileSmsWhenReceive(txdata,
							sms_id, senderid, displayname, groupid, groupname,
							tx_avatarUrl, file_url, file_length, ltime);
					break;
				case TxDB.MSG_TYPE_QU_GIF_SMS:
					txmsg = TXMessage.creatGroupGIFSmsWhenReceive(txdata,
							sms_id, senderid, displayname, groupid, groupname,
							tx_avatarUrl, emomd5, pkgid, ltime);
					break;
				}
				txmsg.gmid = gmid;

				if (tx == null || Utils.isNull(tx.getNick_name())
						|| !nickName.equals(tx.getNick_name())) {
					socketHelper.sendGetUserInfor(senderid);
				} else {
					if (Utils.debug) {
						Log.d(TAG,
								"dealGroupReceiveMessage(),处理从服务器获取的群消息，从数据库获取的用户信息： "
										+ tx);
					}
					txmsg.nick_name = tx.getNick_name();
					txmsg.avatar_url = tx.avatar_url;
				}

				if (smsid.length() != 10 && !isOffline) {
					mSess.getSocketHelper().sendGroupReceipt(groupid, sms_id);
				}
				if (txgroup != null && txgroup.rcv_msg == 1
						&& txgroup.rcv_push == 1 && !isOffline && mt != 2045) {
					showNotification(txmsg, isOffline);
				}
				txmsg.sex = sex;
				autoDownload(txmsg);
				onReceiveMessageFromServer(txmsg, false, isOffline);
			}
		} catch (JSONException e1) {
			if (Utils.debug) {
				Log.e(TAG, "Json异常：", e1);
			}
			return -1;
		}
		return 0;
	}

	/**
	 * 发送更新消息红点的广播
	 */
	public void sendNoReadMsg() {
		Intent intent = new Intent();
		intent.setAction(Constants.INTENT_ACTION_NEW_SINGLE_MSG);
		txdata.sendBroadcast(intent);
	}

	/**
	 * 从server收到消息后的数据处理
	 * 
	 * @param txmsg
	 *            收到的消息
	 * @param isSingle
	 *            表明是单聊还是群聊
	 * @param isoffline
	 *            是否是离线消息
	 */
	private void onReceiveMessageFromServer(TXMessage txmsg, boolean isSingle,
			boolean isoffline) {
		AtomicLong currentID = null;
		long id = 0;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			id = txmsg.partner_id;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			id = txmsg.group_id;
			managers = groupManagers;
			handlers = groupHandlers;
		}

		TX me = TX.tm.getTxMe();
		if (me != null) {
			if (txmsg.partner_id == me.partner_id) {
				txmsg.was_me = true;
				txmsg.avatar_url = me.avatar_url;
				txmsg.nick_name = me.getNick_name();
				if (txmsg.read_state == TXMessage.NOT_SENT) {
					sendSingleMessage(txmsg);
				}
			}
		}
		if (txmsg.msg_type != TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
			// 非上周之星的虚拟消息则存数据库
			// TODO 判断一下是否是重复发送的消息
			if (TXMessage.findTXMessageByMsgid(cr, txmsg.msg_id + "") == null) {
				TXMessage.saveTXMessagetoDB(txmsg, mSess.getContentResolver(),
						true);
			}
		}
		if (id == currentID.get()) {
			MessageListManager mana = managers.get(id);
			if (mana != null) {

				// //给上周之星的虚拟消息设置Gmid，用于群聊天室排序 2014.01.22 shc
				// if (txmsg.msg_type != TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS)
				// {
				// mana.setGmid(txmsg.gmid);// 设置当前消息的gmid为该聊天室最新的gmid
				// } else {
				// txmsg.gmid = mana.getGmid() + 1;// 上周之星的虚拟消息gmid设置为最新gmid+1
				// }

				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					boolean findStarMsg = false;// 标记是否在该集合中找到了上周之星的消息
					if (txmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
						// 该消息是上周之星的虚拟消息
						for (TXMessage tempTxmsg : txmsgs) {
							if (tempTxmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
								// 消息中已经有一条是上周之星的消息了
								findStarMsg = true;
							}
						}
					}

					if (!findStarMsg && !txmsgs.contains(txmsg)) {
						findStarMsg = false;// 重置该布尔值

						// 把这个方法执行语句整合到下面去 2014.01.22 shc
						// if (txmsg.msg_type != TxDB.MSG_TYPE_QU_NOTICE_SMS
						// && txmsg.msg_type != TxDB.MSG_TYPE_QU_PROMPT_SMS
						// && txmsg.msg_type !=
						// TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
						// // 群公告消息，提示消息、和自己发送的虚拟消息，不去判定获取用户资料 2014.01.22 shc
						// TX tx = TX.tm.getTx(txmsg.partner_id);
						//
						// if (tx == null) {
						// sendGetUserInfor(txmsg.partner_id);
						// } else {
						// txmsg.getInfoFromTX(tx, isSingle);
						// // txmsg.setNick_name( tx.getNick_name();
						// // txmsg.avatar_url = tx.avatar_url;
						// }
						// }

						// 给群公告、群提示、上周之星的虚拟消息设置Gmid，用于群聊天室排序 2014.01.22 shc
						// 俺觉得群公告(TxDB.MSG_TYPE_QU_NOTICE_SMS)、群提示(TxDB.MSG_TYPE_QU_PROMPT_SMS)信息应该不会通过这个方法处理
						// 2014.01.22 shc
						if (txmsg.msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
								|| txmsg.msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS
								|| txmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
							txmsg.gmid = mana.getGmid() + 1;// 群公告、群提示、上周之星的虚拟消息gmid设置为最新gmid+1
						} else {
							if (!isSingle) {
								// 群聊则设置gmid
								mana.setGmid(txmsg.gmid);// 设置当前消息的gmid为该聊天室最新的gmid
							}

							TX tx = TX.tm.getTx(txmsg.partner_id);
							if (tx == null) {
								sendGetUserInfor(txmsg.partner_id);
							} else {
								txmsg.getInfoFromTX(tx, isSingle);
								// txmsg.setNick_name( tx.getNick_name();
								// txmsg.avatar_url = tx.avatar_url;
							}
						}

						if (isoffline) {
							txmsgs.add(0, txmsg);
						} else {
							txmsgs.add(txmsg);
						}
						// if (isSingle) {
						// // 单聊用日期排序
						Collections.sort(txmsgs, new MessageComparator());// 用时间排序可能会出现信息顺序不正确的问题。暂时改为用gmid来排序
						// } else {
						// // 群聊用gmid排序
						// Collections.sort(txmsgs,
						// new MessageGmidComparator());
						// }
						Handler handler = handlers.get(id);
						if (handler != null) {
							long audioID = 0;
							if (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS
									|| txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
								audioID = txmsg.msg_id;
							}
							// if (!isoffline) {
							flushUIWithAdd(audioID, handler);
							// }
						}
					}
				}
			}
		}

	}

	// TODO 自动下载消息，应该是收到的消息是文件消息，调此方法下载文件？？？ 2013.10.12 shc
	private void autoDownload(TXMessage txmsg) {
		if (!Utils.isAutoDownLoadImg(mSess.getContext())) {
			// 2G/3G网络下，不能自动下载消息中的附件 2014.06.23 shc
			return;
		}
		if (Utils.roomstate == Utils.IN_OTHER_ROOM) {
			downloadMsg(txmsg);
		} else if (Utils.roomstate == Utils.IN_GROUP_ROOM) {
			if (Utils.roomid != txmsg.group_id) {
				downloadMsg(txmsg);
			}
		} else if (Utils.roomstate == Utils.IN_SINGLE_ROOM) {
			if (Utils.roomid != txmsg.partner_id) {
				downloadMsg(txmsg);
			}
		}
	}

	public void downloadMsg(TXMessage txmsg) {
		if (txmsg.msg_type == TxDB.MSG_TYPE_IMAGE_EMS && txmsg.msg_path == null) {
			Utils.dowloadImgSocket(txmsg);
		} else if (txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS
				&& txmsg.msg_path == null) {
			Utils.downContackSocket(txmsg);
		} else if (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS
				&& txmsg.msg_path == null) {
			Utils.downAduioScoket(txmsg);
		}
	}

	/**
	 * 通知提醒
	 * 
	 * @param isOffline
	 *            是否是离线消息
	 */
	public void showNotification(TXMessage txMsg, boolean isOffline) {
		if (txMsg.partner_id <= 0) {
			ContentValues values = new ContentValues();
			values.put(TxDB.Messages.READ_STATE, TXMessage.READ);
			txdata.getContentResolver().update(TxDB.Messages.CONTENT_URI,
					values, TxDB.Messages.MSG_ID + "=" + txMsg.msg_id, null);
			return;
		}
		TX tx = TX.tm.getTx(txMsg.partner_id);
		if (tx != null) {
			txMsg.nick_name = tx.getNick_name();
			txMsg.send_time = txMsg.send_time * 1000;
			NotificationPopupWindow.getInstance().showNotificationPop(txMsg,
					Constants.DISTINGUISH_TUIXIN_MSG, isOffline);
			txMsg.send_time = txMsg.send_time / 1000;
			new Handler().postDelayed(new Runnable() {

				@Override
				public void run() {
					sendNoReadMsg();
				}
			}, 500);
		}
	}

	private String receiveSameImg(String imgdown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { imgdown }, null);
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	private String receiveSameCard(String carddown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { carddown }, null);
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	private String receiveSameAudio(String audiodown) {
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, Messages.MSG_URL
				+ "=?", new String[] { audiodown }, null);
		if (c == null) {
			return null;
		}
		while (c.moveToNext()) {
			TXMessage cm = TXMessage.fetchOneMsg(c);
			if (!Utils.isNull(cm.msg_path)) {
				c.close();
				return cm.msg_path;
			}
		}
		c.close();
		return null;
	}

	/**
	 * 处理server收到客户端发送的消息, 7号协议
	 */
	public void dealSingleMessageResponse(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			String msgId = jo.getString("id");
			if (d == 0) {
				onUpdateMessageState(true, currentSingleID.get(),
						Long.valueOf(msgId), TXMessage.SENT);
			}
			// 账号不存在
			else if (d == 1) {
				if (Utils.debug)
					Log.e(TAG, "账号不存在: msgid = " + msgId);
			}
			// 数据库错误
			else if (d == 2) {
				if (Utils.debug)
					Log.e(TAG, "数据库错误: msgid = " + msgId);
			}
			// 好友到达上限
			else if (d == 3) {
				if (Utils.debug)
					Log.e(TAG, "好友到达上限: msgid = " + msgId);
			}
			// 手机号被其他用户绑定
			else if (d == 4) {
				if (Utils.debug)
					Log.e(TAG, "手机号被其他用户绑定: msgid = " + msgId);
			}
			// msgID错误
			else if (d == 5) {
				if (Utils.debug)
					Log.e(TAG, "msgID错误: msgid = " + msgId);
			}
		} catch (NumberFormatException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 处理server收到客户端发送的消息, 2009号协议
	 */
	public void dealGroupMessageResponse(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			String msgId = jo.getString("id");
			long gmid = jo.optLong("gmid", 0);
			if (d == 0) {
				onUpdateMessageState(false, currentGroupID.get(),
						Long.valueOf(msgId), TXMessage.SENT, gmid);
			}
			// gid不存在
			else if (d == 1) {
				if (Utils.debug)
					Log.e(TAG, "gid不存在: msgid = " + msgId);
			}
			// gid已被删除
			else if (d == 2) {
				if (Utils.debug)
					Log.e(TAG, "gid已被删除: msgid = " + msgId);
			}
			// 用户不属于这个gid
			else if (d == 3) {
				if (Utils.debug)
					Log.e(TAG, "用户不属于这个gid: msgid = " + msgId);
			}
			// 操作错误
			else if (d == 4) {
				if (Utils.debug)
					Log.e(TAG, "操作错误: msgid = " + msgId);
			}
			// msgID错误
			else if (d == 5) {
				if (Utils.debug)
					Log.e(TAG, "msgID错误: msgid = " + msgId);
			} else if (d == 6) {

				Handler handler = groupHandlers.get(currentGroupID.get());
				handler.sendEmptyMessage(GROUP_SHUT_UP);

				onUpdateMessageState(false, currentGroupID.get(),
						Long.valueOf(msgId), TXMessage.FORBID, gmid);
			}
		} catch (NumberFormatException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 删除消息的广播
	 */
	private final class MessageDelReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] msgids = intent
					.getStringArrayExtra(Constants.EXTRA_MSGIDS_KEY);
			for (String msgid : msgids) {
				onDeleteMessage(true, currentSingleID.get(),
						Long.valueOf(msgid));
				onDeleteMessage(false, currentGroupID.get(),
						Long.valueOf(msgid));
			}
		}
	}

	/**
	 * DB删除消息的处理
	 * 
	 * @param isSingle
	 *            单聊还是群聊
	 * @param msgid
	 *            删除的消息ID
	 */
	private void onDeleteMessage(boolean isSingle, long id, long msgid) {
		AtomicLong currentID = null;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			managers = groupManagers;
			handlers = groupHandlers;
		}

		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Long.valueOf(msgid);

		if (id == currentID.get()) {
			MessageListManager mana = managers.get(id);
			if (mana != null) {
				ArrayList<TXMessage> msgs = mana.getMessageList();
				synchronized (msgs) {
					if (msgs.remove(txmsg)) {
						Handler handler = handlers.get(id);
						if (handler != null) {
							flushUIWithDel(handler);

						}
					}
				}
			}
		}
	}

	/**
	 * 删除所有消息
	 */
	private void onDeleteAllMessage(boolean isSingle, long id) {
		AtomicLong currentID = null;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			managers = groupManagers;
			handlers = groupHandlers;
		}

		if (id == currentID.get()) {
			MessageListManager mana = managers.get(id);
			if (mana != null) {
				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					for (TXMessage txmsg : txmsgs) {
						TXMessage.deleteByMsgId(cr, txmsg.msg_id);
					}
					txmsgs.clear();
				}
				Handler handler = handlers.get(id);
				if (handler != null) {
					flushUIWithDel(handler);
				}
			}
		}
	}

	/**
	 * 接受用户信息下载完成的广播
	 */
	private final class UserInforDoneReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			// TX tx = Utils.getTX(intent);
			long partner_id = intent.getLongExtra(TX.EXTRA_TX,
					Utils.DEFAULT_NUMBER);
			TX tx = TX.tm.getTx(partner_id);
			if (tx == null) {
				return;
			}
			onReceiveUserInfor(true, tx);
			onReceiveUserInfor(false, tx);
		}
	}

	public void dealShutupNotice(JSONObject jo) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_NOTICE_SMS;
		long uid = jo.optLong("uid", 0);
		String name = jo.optString("n", "");
		long gid = jo.optLong("gid", 0);
		long opId = jo.optLong("opid", 0);
		String opName = jo.optString("opn", "");
		int t = jo.optInt("t", 300);
		String tsr = "";
		switch (t) {
		case 0:
			tsr = "永久禁言";
			break;
		case 5 * 60:
			tsr = "禁言5分钟";
			break;
		case 30 * 60:
			tsr = "禁言30分钟";
			break;
		case 24 * 60 * 60:
			tsr = "禁言24小时";
			break;
		default:
			tsr = "禁言" + t + "秒";
			break;
		}
		// 进入群才通知
		txmsg.msg_body = name + "(" + uid + ")" + "被该聊天室管理员" + opName + "("
				+ opId + ")" + tsr;
		txmsg.msg_id = Long.valueOf("" + t + uid);
		txmsg.read_state = TXMessage.READ;
		txmsg.send_time = mSess.getServerTime();
		if (gid == currentGroupID.get()) {
			MessageListManager mana = groupManagers.get(gid);
			if (mana != null) {
				txmsg.gmid = mana.getGmid() + 1;
				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					if (!txmsgs.contains(txmsg)) {
						txmsgs.add(txmsg);
						Handler handler = groupHandlers.get(gid);
						if (handler != null) {
							flushUIWithAdd(1, handler);
						}
					}
				}
			}
		}
	}

	public void dealJoinOutGroup(JSONObject jo) {
		try {
			TXMessage txmsg = new TXMessage();
			txmsg.msg_type = TxDB.MSG_TYPE_QU_NOTICE_SMS;

			int act = jo.getInt("act");
			long uid = jo.getLong("i");
			long gid = jo.getLong("gid");
			int ol = jo.getInt("ol");
			TxGroup txGroup = TxGroup.getTxGroup(txdata.getContentResolver(),
					(int) gid);
			if (txGroup != null) {
				txGroup.group_ol_num = ol;
				// TxGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.OL_NUM, txGroup.group_ol_num);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txGroup.group_id, values);
			} else {
				mSess.getSocketHelper().sendGetGroup(gid);
			}
			// 进入群才通知
			if (act == 0) {
				ServerRsp serverRsp = new ServerRsp();
				if (txGroup == null
						|| (txGroup != null && TxGroup.isPublicGroup(txGroup))) {
					String name = jo.getString("n");
					int sex = jo.getInt("sx");
					txmsg.msg_body = act == 0 ? name + "(" + uid + ")"
							+ "进入聊天室" : name + "(" + uid + ")" + "离开聊天室";
					// txmsg.msg_body = act == 0 ? name+"("+uid+")":
					// name+"("+uid+")";
					txmsg.msg_id = 0;// Long.valueOf("" + gid + uid);
					txmsg.tcard_name = name;
					txmsg.tcard_id = uid;
					txmsg.tcard_sex = sex;

					// TODO 添加数据库信息

					TX tx = new TX();
					tx.setNick_name(name);
					tx.setSex(sex);
					tx.setPartner_id(uid);
					TX.tm.addTx(tx);

					txmsg.read_state = TXMessage.READ;
					txmsg.send_time = mSess.getServerTime();
					if (gid == currentGroupID.get()) {
						MessageListManager mana = groupManagers.get(gid);
						if (mana != null) {
							txmsg.gmid = mana.getGmid() + 1;
							ArrayList<TXMessage> txmsgs = mana.getMessageList();
							synchronized (txmsgs) {
								// 由于丢失进入聊天室的消息 所以注掉
								// if (!txmsgs.contains(txmsg)) {
								txmsgs.add(txmsg);
								Handler handler = groupHandlers.get(gid);
								if (handler != null) {
									flushUIWithAdd(1, handler);
								}
								// }
							}
						}
					}
					serverRsp.setStatusCode(StatusCode.RSP_OK);
				}
				// 此广播没人接收 2014.04.14 shc
				// broadcastMsg(Constants.INTENT_ACTION_INOUT_GROUP_NOTICE_2041,
				// serverRsp);
			}

		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/** 处理获取群公告信息 */
	public void dealPersonGroupNotice(JSONObject jo) {
		try {
			int d = jo.getInt("d");
			long gid = jo.getLong("gid");
			String bl = jo.optString("bl", "");
			if (d == 0) {
				Handler handler = groupHandlers.get(gid);
				if (handler != null && !Utils.isNull(bl)) {
					handler.obtainMessage(SocketHelper.GROUP_NOTICE, bl)
							.sendToTarget();
				}
				TxGroup txgroup = TxGroup.getTxGroup(
						txdata.getContentResolver(), (int) gid);
				if (txgroup == null) {
					txgroup = new TxGroup();
				}
				txgroup.group_bulletin = bl;
				txgroup.group_id = gid;
				// TxGroup.saveTxGroupToDB(txgroup, txdata);//用下面的方法 2014.01.23
				// shc
				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_BULLETIN, txgroup.group_bulletin);
				TxGroup.updateTxGroup(mSess.getContentResolver(),
						txgroup.group_id, values);

			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 接收到用户信息的处理
	 * 
	 * @param isSingle
	 *            单聊还是群聊
	 * @param tx
	 *            用户信息
	 */
	private void onReceiveUserInfor(boolean isSingle, TX tx) {
		AtomicLong currentID = null;
		Map<Long, MessageListManager> managers = null;
		Map<Long, Handler> handlers = null;
		if (isSingle) {
			currentID = currentSingleID;
			managers = singleManagers;
			handlers = singleHandlers;
		} else {
			currentID = currentGroupID;
			managers = groupManagers;
			handlers = groupHandlers;
		}

		boolean needUpdate = false;
		long id = currentID.get();
		MessageListManager mana = managers.get(id);
		if (mana != null) {
			ArrayList<TXMessage> txmsgs = mana.getMessageList();
			synchronized (txmsgs) {
				for (TXMessage txmsg : txmsgs) {
					if (txmsg.partner_id == tx.partner_id) {
						txmsg.getInfoFromTX(tx, isSingle);
						// txmsg.setNick_name( tx.getNick_name();
						// txmsg.avatar_url = tx.avatar_url;
						// if (!isSingle) {
						// txmsg.group_avatars_url = tx.avatar_url;
						// }
						needUpdate = true;
					}
				}

				Handler handler = handlers.get(id);
				if (needUpdate && handler != null) {
					flushUIWithUpdate(handler, false);
				}
			}
		}
	}

	/**
	 * 接收到新消息时通知UI刷新界面 考虑到UI可能需要自动播放音频, 因此将audioID通过obj传递给UI(当audioID不为0时)
	 */
	private void flushUIWithAdd(long audioID, Handler handler) {
		Message message = new Message();
		message.what = CHAT_MSG_LIST_CHANGED;
		message.arg1 = CHAT_MSG_ADD;
		if (audioID != 0) {
			message.obj = Long.valueOf(audioID);
		}
		handler.sendMessage(message);
	}

	/**
	 * 删除消息时通知UI刷新界面
	 */
	private void flushUIWithDel(Handler handler) {
		Message message = new Message();
		message.what = CHAT_MSG_LIST_CHANGED;
		message.arg1 = CHAT_MSG_DEL;
		handler.sendMessage(message);
	}

	/**
	 * 更新消息时通知UI刷新界面
	 * 
	 * @param needVoicePrompt
	 *            表明是否需要播放提示音
	 */
	private void flushUIWithUpdate(Handler handler, boolean needVoicePrompt) {
		Message message = new Message();
		message.what = CHAT_MSG_LIST_CHANGED;
		message.arg1 = CHAT_MSG_UPDATE;
		if (needVoicePrompt) {
			message.arg2 = CHAT_AUDIO_SENT;
		}
		handler.sendMessage(message);
	}

	/**
	 * 加载历史消息时通知UI刷新界面
	 */
	private void flushUIWithAddOld(int addcount, Handler handler, int eof) {
		Message message = new Message();
		message.what = CHAT_MSG_LIST_CHANGED;
		message.arg1 = CHAT_MSG_ADD_OLD;
		Map<String, Long> map = new HashMap<String, Long>();
		map.put("eof", Long.valueOf(eof));
		message.obj = map;
		message.arg2 = addcount;
		handler.sendMessage(message);
	}

	/**
	 * 发送消息时通知UI刷新界面
	 */
	private void flushUIWithAddSelf(Handler handler) {
		Message message = new Message();
		message.what = CHAT_MSG_LIST_CHANGED;
		message.arg1 = CHAT_MSG_ADD_SELF;
		handler.sendMessage(message);
	}

	/**
	 * 获取群组信息后通知UI
	 */
	private void flushUIWithGroupInfor(TxGroup group, Handler handler) {
		Message message = new Message();
		message.what = UPDATE_GROUP_INFORMATION;
		message.obj = group;
		handler.sendMessage(message);
	}

}