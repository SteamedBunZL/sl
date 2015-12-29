package com.tuixin11sms.tx.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import weibo4android.org.json.JSONException;
import weibo4android.org.json.JSONStringer;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.contact.CnToSpell;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ChatChannel;
import com.tuixin11sms.tx.model.LBSUserInfo;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class LBSSocketHelper {
	private static final String TAG = LBSSocketHelper.class.getSimpleName();

	private static String url;
	static {
		if (Utils.test) {
			url = "http://10.1.39.48:9080/wall/NeighborAction.action";
		} else {
			url = "http://api.secondworld.us/wall/NeighborAction.action";
		}
	}

	/**
	 * 本应用Application对象
	 */
	private Context txdata;
	// private SharedPreferences prefs;
	// private Editor editor;
	/**
	 * SocketHelper对象
	 */
	private SocketHelper socketHelper;
	/**
	 * 底层消息收发类
	 */
	private static LBSSocketChannelConnectionImpl connection;

	/**
	 * Handler, 用于频道相关
	 */
	private Handler handler;
	/**
	 * 聊天室的消息列表映射
	 */
	private Map<Integer, ChannelMessageManager> managers = Collections
			.synchronizedMap(new HashMap<Integer, ChannelMessageManager>());
	private static int INVALID_CHANNEL_ID = -1;
	private AtomicInteger currentChannelID = new AtomicInteger(
			INVALID_CHANNEL_ID);
	/**
	 * 聊天室的发言用户
	 */
	private ArrayList<LBSUserInfo> talkPersons = new ArrayList<LBSUserInfo>();

	/**
	 * 聊天室列表的初始消息数量
	 */
	private static final int INIT_LBS_MSG_COUNT = 30;
	/**
	 * 禁言提示的what属性
	 */
	public static final int LBS_CHANNEL_FORBIDDEN = -1232;
	/**
	 * 标识系统公告消息的what属性
	 */
	public static final int LBS_CHANNEL_NOTICE = -1233;
	/**
	 * 频道消息列表发生改变的标记
	 */
	public static final int LBS_CHAT_MSG_LIST_CHANGED = -1234;
	/**
	 * LBS数据变化的原因--删除数据
	 */
	public static final int LBS_CHAT_DEL = 0;
	/**
	 * LBS数据变化的原因--数据增加
	 */
	public static final int LBS_CHAT_ADD = 1;
	/**
	 * LBS数据变化的原因--用户本人发送消息
	 */
	public static final int LBS_CHAT_ADD_SELF = 2;
	/**
	 * LBS数据变化的原因--加载历史数据. 此时arg2有效, arg2表示历史数据的个数
	 */
	public static final int LBS_CHAT_ADD_OLD = 3;
	/**
	 * LBS数据变化的原因--数据更新, 但数据的数量没有改变
	 */
	public static final int LBS_CHAT_UPDATE = 4;
	/**
	 * 音频消息发送成功时的arg2
	 */
	public static final int LBS_AUDIO_SENT = 10;

	/**
	 * Handler, 用于附近的人
	 */
	private Handler nearbyHandler;
	/**
	 * 附近的人列表
	 */
	private List<LBSUserInfo> nearbyUsers = new ArrayList<LBSUserInfo>();

	/**
	 * 用户信息下载完成的广播
	 */
	private BroadcastReceiver receiver;
	/**
	 * 底层删除TXMessage的广播
	 */
	private MessageDelReceiver mdr;

	public static final String OLD_MSG_LIST = "old_msg_list";
	public static final String PUSH_MSG_LIST = "push_list";
	public static final String PUSH_MSG = "push_msg";
	public static final String REMOVE_CHANNLE_LIST = "remove_list";
	public static final String ADD_CHANNEL_LIST = "add_list";
	public static final String CHANGE_CHANEL_LIST = "change_list";
	public static final String MSG_COUNT = "msg_count";
	public static final String ROLL_MSG = "roll_msg";
	public static final String PUTSH_MSG_TYPE = "msg_type";
	public static final String MSG_TIME = "msg_time";
	public static final String USER_INFO = "user_info";
	public static final String DISTANCE = "dis";
	public static final String USERID = "uid";
	public static final String LNG = "lng";
	public static final String LAT = "lat";
	public static final String EOF = "eof";
	public static final String LAST = "last";
	public static final String SERVER_MSGID = "servermsgid";
	public static final String MSGID = "msgid";
	public static final String CHANNEL_PARAM = "channel_param";
	public static final String JOIN_RESULT = "join_result";
	public static final String CHANNEL_DETAIL = "channel_detail";
	public static final String CHANNEL_LIST = "channel_list";
	public static final String CHANNEL_ID = "cid";
	public static final String ERROR_MSG = "error_msg";
	public static final String STATUS = "status";

	/**
	 * 记录最新频道顺序
	 */
	private StringBuilder channelOrder;

	private static LBSSocketHelper helper;

	private static SessionManager mSess = null;

	/**
	 * 私有构造函数
	 */
	private LBSSocketHelper(Context txData) {
		this.txdata = txData;
		// prefs = txData.getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE
		// + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();

		mSess = SessionManager.getInstance();

		socketHelper = mSess.getSocketHelper();

		if (connection == null) {
			connection = new LBSSocketChannelConnectionImpl(SocketHelper.LBS_SOCKET_URL, txdata);
		} else {
			System.err.println(new Date());
			System.err
					.println("LBSSocketHelper is created again and connection is not null!");
		}


		// 注册广播
		receiver = new UserInforDoneReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.INTENT_ACTION_USERINFO_RSP);
		txdata.registerReceiver(receiver, filter);

		mdr = new MessageDelReceiver();
		IntentFilter mdrFilter = new IntentFilter();
		mdrFilter.addAction(Constants.INTENT_ACTION_CLEAR_MSGS_FINISH);
		txdata.registerReceiver(mdr, mdrFilter);

		channelList = new ArrayList<ChatChannel>();
		channelOrder = new StringBuilder();
		// channelOrder.append(prefs.getString(CommonData.CHANNEL_ORDER, ""));
		channelOrder.append(mSess.mPrefMeme.channel_order.getVal());
	}


	/**
	 * 单例
	 */
	public static LBSSocketHelper getLBSSocketHelper(Context txData) {
		if (helper == null) {
			synchronized (LBSSocketHelper.class) {
				if (helper == null) {
					helper = new LBSSocketHelper(txData);
				}
			}
		}
		return helper;
	}

	/**
	 * 将LBS置为原始状态
	 */
	public void recovery() {
		if (Utils.debug) {
			Log.e(TAG, "recovery");
		}
		managers.clear();
		currentChannelID.set(INVALID_CHANNEL_ID);
		synchronized (nearbyUsers) {
			nearbyUsers.clear();
		}
		synchronized (leaveTasks) {
			Map<Integer, String> channels = LBSSessionManager
					.getLBSSessionManager().getJoinedChannelMap();
			for (int channelID : channels.keySet()) {
				if (leaveTasks.containsKey(channelID)) {
					if (Utils.debug) {
						Log.d(TAG,
								"cancel the task of leaving channel, channelID = "
										+ channelID);
					}
					ScheduledFuture<?> oldFuture = leaveTasks.get(channelID);
					oldFuture.cancel(false);
					leaveTasks.remove(channelID);
				}
			}
			leaveTasks.clear();
			LBSSessionManager.getLBSSessionManager().clearAllChannel();
		}
		connection.recovery();
	}

	/**
	 * 注册Handler
	 */
	public void registerHandler(Handler handlerComming) {
		handler = handlerComming;
	}

	/**
	 * 获取底层维护的聊天室列表集合
	 */
	public ChannelMessageManager getChannelListManager(int channelID) {
		if (Utils.debug)
			Log.d(TAG, "getChannelListManager: channelID = " + channelID);
		ChannelMessageManager manager = new ChannelMessageManager(channelID);
		synchronized (managers) {
			managers.remove(manager);
			managers.put(channelID, manager);
		}
		if (channelID != currentChannelID.get()) {
			synchronized (talkPersons) {
				talkPersons.clear();
			}
			currentChannelID.set(channelID);
		}
		return manager;
	}

	/**
	 * 加载频道通知
	 */
	public void loadChannelNotice(int channelID) {
		synchronized (channelList) {
			for (ChatChannel cc : channelList) {
				if (cc.getChannelId() == channelID) {
					String notice = cc.getNotice();
					if (!Utils.isNull(notice)) {
						Message message = new Message();
						message.what = LBS_CHANNEL_NOTICE;
						message.obj = notice;
						cc.setNotice("");
						if (handler != null) {
							if (Utils.debug)
								Log.d(TAG, notice);
							handler.sendMessage(message);
						}
					}
					break;
				}
			}
		}
	}

	public ArrayList<LBSUserInfo> getTalkPersons() {
		ArrayList<LBSUserInfo> temp = null;
		synchronized (talkPersons) {
			temp = new ArrayList<LBSUserInfo>();
			temp.addAll(talkPersons);
		}
		return temp;
	}

	/**
	 * 清除获取的manager对象
	 */
	public void clearChannelListManager(int channelID) {
		if (Utils.debug)
			Log.d(TAG, "clearChannelListManager: channelID = " + channelID);
		managers.remove(channelID);
		currentChannelID.compareAndSet(channelID, INVALID_CHANNEL_ID);
	}

	/**
	 * 注销Handler
	 */
	public void unRegisterHandler() {

	}

	/**
	 * 通知刷新附近的人页面
	 */
	private ScheduledFuture<?> lbsFuture;

	/**
	 * 注册nearbyHandler
	 */
	public void registerNearbyHandler(Handler handlerComming) {
		nearbyHandler = handlerComming;
		// 为保持连接以及考虑到减少对底层代码的修改, 在获取附近的人时虚拟加入一个频道
		joinVirtualChannel();
		lbsFuture = executor.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				if (lbsNeedUpdate && nearbyHandler != null) {
					lbsNeedUpdate = false;
					// nearbyHandler.sendEmptyMessage(LBS_SINGLE_USER);
				}
			}
		}, 0, 1, TimeUnit.SECONDS);
	}

	/**
	 * 获取底层维护的附近的人集合
	 */
	public List<LBSUserInfo> getLBSUserInfoList(int sex) {
		nearbyCount = 0;
		synchronized (nearbyUsers) {
			nearbyUsers.clear();
			// 增加本人信息
			TX me = TX.tm.getTxMe();
			if (sex == QUERY_ALL || sex == me.getSex()) {
				LBSUserInfo lbsme = new LBSUserInfo();
				lbsme.setUid((int) me.partner_id);
				lbsme.setNickName(me.getNick_name());
				lbsme.setSex(me.getSex());
				lbsme.setDist(0);
				lbsme.setSignature(me.sign);
				lbsme.setAvatar(me.avatar_url);
				nearbyUsers.add(lbsme);
			}
			return nearbyUsers;
		}
	}

	/**
	 * 注销nearbyHandler
	 */
	public void unRegisterNearbyHandler() {
		nearbyHandler = null;
		synchronized (nearbyUsers) {
			nearbyUsers.clear();
		}
		leaveChannelNow(VIRTUAL_CHANNEL_ID);
		lbsFuture.cancel(true);
	}

	/**
	 * 发送消息
	 */
	private void sendMessage(String message, int infortype) {
		if (Utils.debug)
			Log.d(TAG, message);
		if (Utils.isNull(message))
			return;
		connection.sendMessage(message);
	}

	/**
	 * 获取频道列表, 3200
	 */
	public void getChannelList(int page, boolean detail) {
		try {
			String msg = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_CHANNEL_LIST).key("p").value(page)
					.key("type").value(-1).key("detail").value(detail)
					.endObject().toString();
			sendMessage(msg, SocketHelper.custom_type);
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取频道详细信息, 3202
	 */
	public void getChannelDetail(int channelID) {
		try {
			String msg = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_CHANNEL_DETAIL).key("cid")
					.value(channelID).endObject().toString();
			sendMessage(msg, SocketHelper.custom_type);
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 加入虚拟频道, 查找附近的人时调用该方法
	 */
	private void joinVirtualChannel() {
		LBSSessionManager.getLBSSessionManager().joinChannel(
				VIRTUAL_CHANNEL_ID, "");
	}




	/**
	 * 获取加入的频道的参数信息, 3206
	 */
	public void getChannelParamInfo(int channelID) {
		try {
			String msg = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_CHANNEL_INFO).key("cid")
					.value(channelID).endObject().toString();
			sendMessage(msg, SocketHelper.custom_type);
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 用于执行延时退出频道的线程池和更新附近的人
	 */
	private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(
			1);
	/**
	 * 退出频道的延时时间
	 */
	private static final int LEAVE_CHANNEL_DELAY_TIME = 5 * 60 * 1000;

	/**
	 * 缓存待退频道任务
	 */
	private Map<Integer, ScheduledFuture<?>> leaveTasks = new HashMap<Integer, ScheduledFuture<?>>();

	/**
	 * 离开频道, 3208协议. 供外部调用, 会做延时处理
	 */
	public void leaveChannel(int channelID) {
		cancelLeaveTask(channelID);
		Runnable leaveRunnable = new LeaveChannelRunnable(channelID);
		ScheduledFuture<?> future = executor.schedule(leaveRunnable,
				LEAVE_CHANNEL_DELAY_TIME, TimeUnit.MILLISECONDS);
		synchronized (leaveTasks) {
			leaveTasks.put(channelID, future);
		}
	}

	/**
	 * 取消离开channelID频道的定时任务
	 */
	private void cancelLeaveTask(int channelID) {
		synchronized (leaveTasks) {
			if (leaveTasks.containsKey(channelID)) {
				if (Utils.debug) {
					Log.d(TAG,
							"cancel the task of leaving channel, channelID = "
									+ channelID);
				}
				ScheduledFuture<?> oldFuture = leaveTasks.get(channelID);
				oldFuture.cancel(false);
				leaveTasks.remove(channelID);
			}
		}
	}

	private final class LeaveChannelRunnable implements Runnable {
		private int channelID;

		public LeaveChannelRunnable(int channelID) {
			this.channelID = channelID;
		}

		@Override
		public void run() {
			synchronized (leaveTasks) {
				leaveTasks.remove(channelID);
			}
			leaveChannelNow(channelID);
		}
	}

	/**
	 * 离开频道, 供内部调用. 真正离开频道
	 */
	private void leaveChannelNow(int channelID) {
		try {
			if (Utils.debug) {
				Log.d(TAG, "leaveChannelNow, channelID = " + channelID);
			}
			if (LBSSessionManager.getLBSSessionManager()
					.leaveChannel(channelID)) {
				if (channelID != VIRTUAL_CHANNEL_ID) {
					String msg = new JSONStringer().object().key("mt")
							.value(MsgInfor.CLIENT_LEAVE_CHANNEL).key("cid")
							.value(channelID).endObject().toString();
					sendMessage(msg, SocketHelper.custom_type);
				}
				// 如果所有加入的频道都已经离开， 就关闭connection
				if (LBSSessionManager.getLBSSessionManager().isNoChannelIn()) {
					recovery();
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 得到个人位置, 3302
	 */
	public void getMyLocation() {
		try {
			String msg = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLIENT_MY_LOCATION).endObject().toString();
			sendMessage(msg, SocketHelper.custom_type);
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 清除个人位置, 3304
	 */
	public void clearMyLocation() {
		String json = "";
		try {
			json = new JSONStringer().object().key("method")
					.value("deleteGEOInfo").key("params").array()
					.value(TX.tm.getTxMe().partner_id)
					.value(TX.tm.getTxMe().token).endArray().key("id")
					.value(33).endObject().toString();
		} catch (JSONException e) {
			//
			if (Utils.debug)
				e.printStackTrace();
		}
		handler(json);
	}

	/**
	 * 标记查询的性别
	 */
	private volatile int sex;
	/**
	 * 只查询女
	 */
	public static final int QUERY_GIRL = 1;
	/**
	 * 只查询男
	 */
	public static final int QUERY_BOY = 0;
	/**
	 * 查询所有性别
	 */
	public static final int QUERY_ALL = -1;

	/**
	 * 虚拟频道号
	 */
	public static final int VIRTUAL_CHANNEL_ID = -6666;

	/**
	 * 查找附近的人已从server返回的人数
	 */
	private volatile int nearbyCount = 0;

	/*	*//**
	 * 查找附近的人, 3400 sex只能是QUERY_GIRL, QUERY_BOY或QUERY_ALL
	 */
	/*
	 * @SuppressWarnings("unused") private void getNearbyPeople(int start, int
	 * count, int sex) { try { this.sex = sex; String msg = new
	 * JSONStringer().object
	 * ().key("mt").value(MsgInfor.CLIENT_NEARBY_PEOPLE).key("sex")
	 * .value(sex).key
	 * ("start").value(nearbyCount).key("count").value(count).endObject
	 * ().toString(); sendMessage(msg, SocketHelper.custom_type); nearbyCount +=
	 * count; } catch (Exception e) { if(Utils.debug)e.printStackTrace(); } }
	 */

	/**
	 * 查找附近的人, 3400 sex只能是QUERY_GIRL, QUERY_BOY或QUERY_ALL 将上传手机位置给server.
	 */
	/*
	 * public void getNearbyPeople(double lat, double lng, int start, int count,
	 * int sex) { try { this.sex = sex; String msg = new
	 * JSONStringer().object().
	 * key("mt").value(MsgInfor.CLIENT_NEARBY_PEOPLE).key("trans")
	 * .value(false).
	 * key("lat").value(lat).key("lng").value(lng).key("sex").value
	 * (sex).key("start")
	 * .value(nearbyCount).key("count").value(count).endObject().toString();
	 * sendMessage(msg, SocketHelper.custom_type);
	 * 
	 * } catch (Exception e) { if(Utils.debug)e.printStackTrace(); } }
	 */

	/**
	 * 查找附近的人, 3400 sex只能是QUERY_GIRL, QUERY_BOY或QUERY_ALL trans为true时,
	 * lat和lng表示传送到的位置. 否则lat和lng表示将上传的当前位置.
	 */
	/*
	 * @SuppressWarnings("unused") private void getNearbyPeople(boolean trans,
	 * double lat, double lng, int start, int count, int sex) { try { this.sex =
	 * sex; String msg = new
	 * JSONStringer().object().key("mt").value(MsgInfor.CLIENT_NEARBY_PEOPLE
	 * ).key("trans")
	 * .value(trans).key("lat").value(lat).key("lng").value(lng).key
	 * ("sex").value(sex).key("start")
	 * .value(nearbyCount).key("count").value(count).endObject().toString();
	 * sendMessage(msg, SocketHelper.custom_type); nearbyCount += count; } catch
	 * (Exception e) { if(Utils.debug)e.printStackTrace(); } }
	 */

	// 无引用 2014.01.16
	// /**
	// * 上传个人资料, 3500
	// */
	// public void sentPersonalInfo(TX tx) {
	// try {
	// JSONWriter writer = new
	// JSONStringer().object().key("mt").value(MsgInfor.CLIENT_UPLOAD_PERSONAL_INFO);
	// if (tx.sex == TX.MALE_SEX || tx.sex == TX.FEMAL_SEX) {
	// writer.key("sex").value(tx.sex);
	// }
	// if (!Utils.isNull(tx.birthday)) {
	// writer.key("birthday").value(tx.birthday);
	// }
	// if (!Utils.isNull(tx.avatar_url)) {
	// writer.key("avatar").value(tx.avatar_url);
	// }
	// if (!Utils.isNull(tx.area)) {
	// writer.key("city").value(tx.area);
	// }
	// if (!Utils.isNull(tx.getNick_name())) {
	// writer.key("nick").value(tx.getNick_name());
	// }
	// if (!Utils.isNull(tx.user_sign)) {
	// writer.key("sign").value(tx.user_sign);
	// }
	// sendMessage(writer.endObject().toString(), SocketHelper.custom_type);
	// } catch (Exception e) {
	// if(Utils.debug)e.printStackTrace();
	// }
	// }
	//
	// /**
	// * 获取用户的自建频道ID列表, 3504
	// */
	// public void getMyChannel() {
	// try {
	// String msg = new
	// JSONStringer().object().key("mt").value(MsgInfor.CLIENT_GET_MY_CHANNEL).endObject()
	// .toString();
	// sendMessage(msg, SocketHelper.custom_type);
	// } catch (JSONException e) {
	// if(Utils.debug)e.printStackTrace();
	// }
	// }

	private final class MessageDelReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			String[] msgids = intent
					.getStringArrayExtra(Constants.EXTRA_MSGIDS_KEY);
			for (String msgidStr : msgids) {
				TXMessage txmsg = new TXMessage();
				txmsg.msg_id = Long.valueOf(msgidStr);
				ChannelMessageManager mana = managers.get(currentChannelID
						.get());
				if (mana != null) {
					ArrayList<TXMessage> lbsChatMsgs = mana.getMessageList();
					synchronized (lbsChatMsgs) {
						if (lbsChatMsgs.remove(txmsg)) {
							Message message = new Message();
							message.what = LBS_CHAT_MSG_LIST_CHANGED;
							message.arg1 = LBS_CHAT_DEL;
							if (handler != null) {
								handler.sendMessage(message);
							}
						}
					}
				}
			}
		}
	}

	/**
	 * 标记用户资料是否更新
	 */
	private volatile boolean lbsNeedUpdate = false;

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

			synchronized (talkPersons) {
				for (LBSUserInfo talkPerson : talkPersons) {
					if (talkPerson.getUid() == tx.partner_id) {
						copyInfo2LBSUser(tx, talkPerson);
						break;
					}
				}
			}

			synchronized (nearbyUsers) {
				for (LBSUserInfo user : nearbyUsers) {
					if (user.getUid() == tx.partner_id) {
						if (Utils.debug) {
							Log.d(TAG, "从server获取的用户信息 " + tx);
						}
						if (sex != QUERY_ALL && sex != tx.getSex()) {
							nearbyUsers.remove(user);
						} else {
							copyInfo2LBSUser(tx, user);
						}
						lbsNeedUpdate = true;
						break;
					}
				}
			}

			boolean updated = false;
			ChannelMessageManager mana = managers.get(currentChannelID.get());
			if (mana != null) {
				ArrayList<TXMessage> lbsChatMsgs = mana.getMessageList();
				synchronized (lbsChatMsgs) {
					for (TXMessage txmsg : lbsChatMsgs) {
						if (txmsg.partner_id == tx.partner_id) {
							if (Utils.debug) {
								Log.d(TAG, "从server获取的用户信息 " + tx);
							}
							updated = true;
							txmsg.getInfoFromTX(tx, true);
							// txmsg.setNick_name( tx.getNick_name();
							// txmsg.avatar_url = tx.avatar_url;
						}
					}
				}
			}
			if (updated && handler != null) {
				Message lbsMsg = new Message();
				lbsMsg.what = LBS_CHAT_MSG_LIST_CHANGED;
				lbsMsg.arg1 = LBS_CHAT_UPDATE;
				handler.sendMessage(lbsMsg);
			}
		}
	}

	/**
	 * 改变消息状态
	 */
	public void changeMessageState(int channelID, long msgID, int state) {
		TXMessage.updateByMsgId(mSess.getContentResolver(), msgID, state);
		if (channelID == currentChannelID.get()) {
			MessageListManager mana = managers.get(channelID);
			if (mana != null) {
				ArrayList<TXMessage> txmsgs = mana.getMessageList();
				synchronized (txmsgs) {
					for (TXMessage txmsg : txmsgs) {
						if (txmsg.msg_id == msgID) {
							txmsg.read_state = state;
							Message message = new Message();
							message.what = LBS_CHAT_MSG_LIST_CHANGED;
							message.arg1 = LBS_CHAT_UPDATE;
							if (handler != null) {
								handler.sendMessage(message);
							}
							break;
						}
					}
				}
			}
		}
	}

	/**
	 * 处理接收到的消息
	 */
	public void dealMsg(String msg) {
		int type = Utils.getMessageType(msg);
		switch (type) {
//		case MsgInfor.SERVER_CHANNEL_LIST:
//			dealChannelList(msg);
//			break;
//		case MsgInfor.SERVER_CHANNEL_DETAIL:
//			dealChannelDetail(msg);
//			break;
//		case MsgInfor.SERVER_JOIN_CHANNEL:
//			dealJoinChannel(msg);
//			break;
//		case MsgInfor.SERVER_CHANNEL_INFO:
//			dealChannelInfo(msg);
//			break;
//		case MsgInfor.SERVER_LEAVE_CHANNEL:
//			dealLeaveChannel(msg);
//			break;
//		case MsgInfor.SERVER_RECEIVE_MSG:
//			dealReceiveMsg(msg);
//			break;
		// case MsgInfor.SERVER_OLD_MSG:
		// 无人请求该协议，这个返回协议号是不是也没用了？ 2013.12.06
		// dealHistoryMsg(msg);
		// break;
		case MsgInfor.SERVER_LOCATION_INFO:
			dealLocationInfo(msg);
			break;
		case MsgInfor.SERVER_MY_LOCATION:
			dealMyLocation(msg);
			break;
		case MsgInfor.SERVER_CLEAR_LOCATION:
			dealClearLocation(msg);
			break;
		/*
		 * case MsgInfor.SERVER_NEARBY_PEOPLE: //dealNearbyPeople(msg); break;
		 */
		case MsgInfor.SERVER_UPLOAD_PERSONAL_INFO:
			dealUploadInfo(msg);
			break;
		case MsgInfor.SERVER_GET_USERINFO:
			dealUserInfo(msg);
			break;
		case MsgInfor.SERVER_MY_CHANNEL:
			dealMyChannel(msg);
			break;
		// case MsgInfor.SERVER_PUSH:
		// dealServerPush(msg);
		// break;
		default:
			break;
		}
	}

	// 该协议应该已经废弃 2014.01.23 shc
	// /**
	// * server下推消息, 3600
	// */
	// private void dealServerPush(String msg) {
	// try {
	// JSONObject jo = new JSONObject(msg);
	// long tm = jo.getLong("tm");
	// int t = jo.getInt("t");
	// // 系统提示
	// if (t == 0) {
	// JSONObject joo = jo.getJSONObject("obj");
	// int cid = joo.getInt("cid");
	// String notice = joo.getString("msg");
	// // int level = joo.getInt("level");
	// int type = joo.getInt("type");
	// TuiboSlienceManager tsm = TuiboSlienceManager.getInstance();
	//
	// // 系统通知
	// if (type == 0) {
	// Message message = new Message();
	// message.what = LBS_CHANNEL_NOTICE;
	// if (cid == currentChannelID.get() && handler != null) {
	// message.obj = notice;
	// handler.sendMessage(message);
	// }
	// }
	// // 用户进入频道时的欢迎消息
	// else if (type == 3) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_QU_NOTICE_SMS;
	// txmsg.msg_body = notice;
	// txmsg.msg_id = Utils.createMsgId(TX.tm.getTxMe().partner_id + "");
	// txmsg.read_state = TXMessage.READ;
	// // txmsg.send_time = System.currentTimeMillis() / 1000 +
	// prefs.getLong(CommonData.ST, 0);
	// txmsg.send_time = System.currentTimeMillis() / 1000 +
	// mSess.mPrefMeme.st.getVal();
	// // txmsg.setChannelId(cid);
	// addOtherInfor(cid, txmsg);
	// // TXMessage.saveTXMessagetoDB(txmsg, txdata);
	// MessageListManager mana = managers.get(cid);
	// if (mana != null) {
	// txmsg.gmid = mana.getGmid()+1;//临时给这个消息加一个gmid
	// ArrayList<TXMessage> txmsgs = mana.getMessageList();
	// synchronized (txmsgs) {
	// if (!txmsgs.contains(txmsg)) {
	// addMessageToList(cid, txmsg, txmsgs);
	// }
	// }
	// }
	// } else if (type == 5) {
	// tsm.setGlobalAdmin(true);
	// } else if (type == 6) {
	// tsm.setGlobalAdmin(false);
	// } else if (type == 7) {
	// tsm.updateChannelPri(2, true, cid);
	// } else if (type == 8) {
	// tsm.updateChannelPri(2, false, cid);
	// }
	// }
	// // 频道消息
	// else if (t == 1) {
	// JSONObject joo = jo.getJSONObject("obj");
	// int cid = joo.getInt("cid");
	// int uid = joo.getInt("uid");
	// double lat = joo.getDouble("lat");
	// double lng = joo.getDouble("lng");
	// // String servermsgid = joo.getString("servermsgid");
	// TXMessage txmsg = getTXMessage(joo.getJSONObject("msg"), cid);
	// txmsg.send_time = tm;
	// // txmsg.setChannelId(cid);
	// txmsg.partner_id = uid;
	// if (txmsg.partner_id == TX.tm.getTxMe().partner_id) {
	// txmsg.was_me = true;
	// }
	// txmsg.geo = lat + "," + lng;
	//
	// Utils.getLbsLoction(txmsg);
	// // TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
	// TX tx = TX.tm.getTx(uid);
	//
	//
	// if (tx == null || Utils.isNull(tx.getNick_name())) {
	// socketHelper.sendGetUserInfor(uid);
	// } else {
	// if (Utils.debug) {
	// Log.d(TAG, "从数据库获取的用户信息 " + tx);
	// }
	// txmsg.nick_name= tx.getNick_name();
	// txmsg.avatar_url = tx.avatar_url;
	// }
	// TXMessage.saveTXMessagetoDB(txmsg, txdata,true);
	//
	// addTalkPerson(uid, txmsg, tx);
	//
	// ChannelMessageManager mana = managers.get(cid);
	// if (mana != null) {
	// ArrayList<TXMessage> txmsgs = mana.getMessageList();
	// synchronized (txmsgs) {
	// if (!txmsgs.contains(txmsg)) {
	// addMessageToList(cid, txmsg, txmsgs);
	// }
	// }
	// }
	// }
	// // 频道消息列表
	// else if (t == 2) {
	// Message message = new Message();
	// message.what = LBS_CHAT_MSG_LIST_CHANGED;
	// message.arg1 = LBS_CHAT_ADD;
	// JSONObject joo = jo.getJSONObject("obj");
	// int cid = joo.getInt("cid");
	// // int dropcount = joo.getInt("dropcount");
	// JSONArray ja = joo.getJSONArray("msglist");
	// ArrayList<TXMessage> txmsgs = new ArrayList<TXMessage>();
	// Set<Integer> set = new HashSet<Integer>();
	// for (int i = 0; i < ja.length(); i++) {
	// JSONObject jooo = ja.getJSONObject(i);
	// int uid = jooo.getInt("uid");
	// double lat = jooo.getDouble("lat");
	// double lng = jooo.getDouble("lng");
	// // String servermsgid = jooo.getString("servermsgid");
	// TXMessage txmsg = getTXMessage(jooo.getJSONObject("msg"), uid);
	// txmsg.send_time = tm;
	// // txmsg.setChannelId(cid);
	// txmsg.partner_id = uid;
	// if (txmsg.partner_id == TX.tm.getTxMe().partner_id) {
	// txmsg.was_me = true;
	// }
	// txmsg.geo = lat + "," + lng;
	//
	// // TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
	// TX tx = TX.tm.getTx(uid);
	//
	//
	// if (tx == null || Utils.isNull(tx.getNick_name())) {
	// set.add(uid);
	// } else {
	// if (Utils.debug) {
	// Log.d(TAG, "从数据库获取的用户信息 " + tx);
	// }
	// txmsg.nick_name = tx.getNick_name();
	// txmsg.avatar_url = tx.avatar_url;
	// }
	// Utils.getLbsLoction(txmsg);
	// txmsgs.add(txmsg);
	// TXMessage.saveTXMessagetoDB(txmsg, txdata,true);
	// addTalkPerson(uid, txmsg, tx);
	// }
	//
	// ChannelMessageManager mana = managers.get(cid);
	// if (mana != null) {
	// ArrayList<TXMessage> lbsChatMsgs = mana.getMessageList();
	// synchronized (lbsChatMsgs) {
	// Collections.sort(txmsgs, new MessageDateComparator());
	// for (TXMessage txmsg : txmsgs) {
	// if (!lbsChatMsgs.contains(txmsg)) {
	// addMessageToList(cid, txmsg, lbsChatMsgs);
	// }
	// }
	// }
	// }
	// for (int uid : set) {
	// socketHelper.sendGetUserInfor(uid);
	// }
	// }
	// // 频道消息数量
	// else if (t == 3) {
	// // ...
	// }
	// // 频道列表改变
	// else if (t == 4) {
	// Message message = new Message();
	// message.what = LBS_CHANNEL_LIST_UPDATE;
	// JSONObject joo = jo.getJSONObject("obj");
	// JSONArray changeJa = joo.optJSONArray("change");
	// if (changeJa != null) {
	// for (int i = 0; i < changeJa.length(); i++) {
	// getChannelDetail(changeJa.getInt(i));
	// }
	// }
	//
	// JSONArray addJa = joo.optJSONArray("add");
	// if (addJa != null) {
	// for (int i = 0; i < addJa.length(); i++) {
	// getChannelDetail(addJa.getInt(i));
	// }
	// }
	//
	// JSONArray removeJa = joo.optJSONArray("remove");
	// if (removeJa != null) {
	// for (int i = 0; i < removeJa.length(); i++) {
	// ChatChannel cc = dao.getChannelById(removeJa.getInt(i));
	// if (cc != null) {
	// cc.setClosed(true);
	// dao.updateChannel(cc);
	// synchronized (channelList) {
	// channelList.remove(cc);
	// }
	// }
	// }
	// }
	//
	// if (handler != null) {
	// handler.sendMessage(message);
	// }
	// }
	// } catch (Exception e) {
	// if(Utils.debug)e.printStackTrace();
	// }
	// }

	/**
	 * 将消息添加到消息列表末尾
	 */
	private void addMessageToList(int cid, TXMessage txmsg,
			ArrayList<TXMessage> txmsgs) {
		txmsgs.add(txmsg);
		if (txmsgs.size() > INIT_LBS_MSG_COUNT) {
			if (txmsgs.get(0).msg_type == TxDB.MSG_TYPE_GEO_PROMPT) {
				txmsgs.remove(1);
			} else {
				txmsgs.remove(0);
			}
		}
		Message message = new Message();
		message.what = LBS_CHAT_MSG_LIST_CHANGED;
		message.arg1 = LBS_CHAT_ADD;
		if (txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
			message.obj = txmsg.msg_id;
		}
		if (cid == currentChannelID.get() && handler != null) {
			handler.sendMessage(message);
		}
	}

	// 引用解除，注掉 2014.01.23 shc
	// private void addTalkPerson(int uid, TXMessage txmsg, TX tx) {
	// if (uid == -1) {
	// return;
	// }
	// LBSUserInfo talkPerson = new LBSUserInfo();
	// talkPerson.setUid(uid);
	// synchronized (talkPersons) {
	// if (!talkPersons.contains(talkPerson)) {
	// copyInfo2LBSUser(tx, talkPerson);
	// talkPerson.setDist((int) txmsg.lbs_distance);
	// talkPersons.add(talkPerson);
	// }
	// }
	// }

	/**
	 * 将时间换算成易理解的字符串
	 * 
	 * @param remainTime
	 *            以秒为单位的时间
	 */
	private String time2String(int remainTime) {
		if (remainTime == -1) {
			return "永久";
		}
		StringBuilder result = new StringBuilder();
		int day = 24 * 60 * 60;
		int hour = 60 * 60;
		int minute = 60;
		if (remainTime >= day) {
			result.append(remainTime / day).append("天");
			remainTime %= day;
		}
		if (remainTime >= hour) {
			result.append(remainTime / hour).append("小时");
			remainTime %= hour;
		}
		if (remainTime >= minute) {
			result.append(remainTime / minute).append("分");
			remainTime %= minute;
		}
		result.append(remainTime).append("秒");
		return result.toString();
	}

	/**
	 * 获取我的频道的缓存
	 */
	private List<Integer> myChannels = new ArrayList<Integer>();

	/**
	 * 返回用户的自建频道ID列表, 3505
	 */
	private void dealMyChannel(String msg) {
		try {
			JSONObject jo = new JSONObject(msg);
			TuiboSlienceManager.getInstance().setGlobalAdmin(
					jo.optBoolean("admin", false));
			JSONArray ja = jo.getJSONArray("cids");
			int[] cids = new int[ja.length()];
			for (int i = 0; i < cids.length; i++) {
				cids[i] = ja.getInt(i);
			}
			for (int cid : cids) {
				myChannels.add(cid);
				getChannelDetail(cid);
			}
		} catch (org.json.JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}

	}

	/**
	 * 返回用户资料, 3503
	 */
	private void dealUserInfo(String msg) {
		try {
			if (Utils.debug) {
				Log.d(TAG, "dealUserInfo");
			}
			JSONObject jo = new JSONObject(msg);
			Message message = new Message();
			message.what = MsgInfor.SERVER_GET_USERINFO;
			Map<String, Object> map = new HashMap<String, Object>();
			int d = jo.getInt("d");
			message.arg1 = d;
			int uid = jo.getInt("uid");
			map.put(LBSSocketHelper.USERID, uid);
			if (d == 0) {
				// TX tx = TX.getTx(uid);
				// if (tx == null) {
				// tx = new TX();
				// tx.setTx_type(TxDB.TX_TYPE_ST);
				// }
				//
				// tx.setSex(jo.optInt("sex"));
				// tx.setAge(jo.optInt("age"));
				// tx.setAvatar_url(jo.optString("avatar"));
				// tx.setArea(jo.optString("city"));
				// tx.setNick_name(jo.optString("nick"));
				// tx.setSign(jo.optString("sign"));
				// tx.setPartnerId(uid);
				// if (Utils.debug) {
				// Log.d(TAG, "从server获取的用户信息" + tx);
				// }
				//
				//
				// // TX.saveTXtoDB(tx, txdata.getContentResolver());
				// TX.addTX_new(tx);

				int sex = jo.optInt("sex");
				int age = jo.optInt("age");
				String avatarUrl = jo.optString("avatar");
				String area = jo.optString("city");
				String nickName = jo.optString("nick");
				String sign = jo.optString("sign");

				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.SEX, sex);
				values.put(TxDB.Tx.AGE, age);
				values.put(TxDB.Tx.AVATAR_URL, avatarUrl);
				values.put(TxDB.Tx.HOME, area);
				values.put(TxDB.Tx.DISPLAY_NAME, nickName);
				values.put(TxDB.Tx.SECOND_CHAR,
						CnToSpell.getFullSpell(nickName));
				values.put(TxDB.Tx.TX_SIGN, sign);

				TX tx = TX.tm.updateTx(uid, values);

				if (tx == null) {
					// 更新失败，添加tx
					tx = new TX();
					tx.setPartnerId(uid);
					tx.setSex(sex);
					tx.setAge(age);
					tx.setAvatar_url(avatarUrl);
					tx.setArea(area);
					tx.setNick_name(nickName);
					tx.setSign(sign);

					TX.tm.addTx(tx);
				}

				// if (TX.tm.isTxFriend(uid)) {
				// //好友
				// tx = TX.tm.updateTBTX(uid, values);
				// } else if (TX.tm.isInBlack(uid)) {
				// //黑名单用户
				// tx = TX.tm.updateBlackTX(uid, values);
				// }else {
				// //陌生人
				// TX ttx = TX.tm.getSTTX(uid);
				// if (ttx != null) {
				// tx = TX.tm.updateSTTX(uid, values);
				// }else {
				// tx = new TX();
				// tx.setPartnerId(uid);
				// tx.setSex(sex);
				// tx.setAge(age);
				// tx.setAvatar_url(avatarUrl);
				// tx.setArea(area);
				// tx.setNick_name(nickName);
				// tx.setSign(sign);
				//
				// TX.tm.addSTTX(tx);
				// }
				// }

				map.put(LBSSocketHelper.USER_INFO, tx);
			}
			// uid不存在
			else if (d == 3) {
				map.put(LBSSocketHelper.ERROR_MSG, "id不存在");
			}
			message.obj = map;
			if (handler != null) {
				handler.sendMessage(message);
			} else {
				if (Utils.debug) {
					Log.e(TAG, "dealUserInfo: handler == null");
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 返回个人资料上传结果, 3501
	 */
	private void dealUploadInfo(String msg) {
		try {
			JSONObject jo = new JSONObject(msg);
			Message message = new Message();
			message.what = MsgInfor.SERVER_UPLOAD_PERSONAL_INFO;
			Map<String, Object> map = new HashMap<String, Object>();
			int d = jo.getInt("d");
			message.arg1 = d;
			// 参数错误
			if (d == 3) {
				map.put(LBSSocketHelper.ERROR_MSG, "参数错误");
			}
			message.obj = map;
			if (handler != null) {
				handler.sendMessage(message);
			} else {
				if (Utils.debug) {
					Log.e(TAG, "dealUploadInfo: handler == null");
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * what的值为LBS_USER_LIST时, obj字段是一个LBSUerInfo的List
	 */
	public static final int LBS_USER_LIST = 2222;
	/**
	 * waht的值为LBS_SINGLE_USER时, obj字段是一个单独的LBSUerInfo对象
	 */
	public static final int LBS_SINGLE_USER = 3333;

	public void uploadLocation(double lat, double lon) {
		handler(uploadLocationJson(lat, lon));
	}

	private static ArrayList<LBSUserInfo> lbsUserInfos = new ArrayList<LBSUserInfo>();

	/**
	 * 返回附近的人, 3401
	 */
	public void getNearbyPeople(double lat, double lon, int start, int count,
			int sex, int currentPage) {
		uploadLocation(lat, lon);
		if (lbsUserInfos.size() == 0) {
			JSONObject json2 = handler(getNearlyFriendJson(lat, lon));
			if (json2 != null) {
				try {
					JSONObject jo = json2.getJSONObject("result");
					int flag = jo.getInt("flag");
					if (flag == 0) {
						List<Integer> list = new ArrayList<Integer>();
						JSONArray ja = jo.getJSONArray("resultList");
						if (Utils.debug) {
							System.out.print(ja.toString());
						}
						for (int i = 0; i < ja.length(); i++) {
							JSONObject joo = ja.getJSONObject(i);
							int uid = joo.getInt("uid");
							int u_sex = joo.optInt("sex");
							// double u_lat = joo.getDouble("lat");
							// double u_lng = joo.getDouble("lon");
							int dis = joo.getInt("distance");

							LBSUserInfo lbsUser = new LBSUserInfo();
							lbsUser.setUid(uid);
							lbsUser.setSex(u_sex);
							/*
							 * lbsUser.setLat(u_lat); lbsUser.setLng(u_lng);
							 */
							lbsUser.setDist(dis);

							// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库
							// 2013.10.17 shc
							TX tx = TX.tm.getTx(uid);

							if (uid == TX.tm.getUserID()) {
								tx = TX.tm.getTxMe();
							}
							if (tx == null || Utils.isNull(tx.getNick_name())
									|| Utils.isNull(tx.avatar_url)) {
								// -1表示性别信息无效c
								lbsUser.setSex(-1);
								list.add(uid);
							}

							lbsUserInfos.add(lbsUser);
						}
						// 从server取用户信息, 该操作集中在一起可以增加效率
						for (int id : list) {
							socketHelper.sendGetUserInfor(id);
						}

						synchronized (lbsUserInfos) {
							Collections.sort(lbsUserInfos,
									new Comparator<LBSUserInfo>() {
										@Override
										public int compare(LBSUserInfo o1,
												LBSUserInfo o2) {
											return o1.getDist() - o2.getDist();
										}
									});
						}
					}
				} catch (org.json.JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
			}
		}
		this.sex = sex;
		Message message = new Message();
		message.what = LBS_USER_LIST;
		nearbyCount += count;

		List<LBSUserInfo> partList;
		int total = currentPage * 50;
		if (total >= lbsUserInfos.size()) {
			partList = new ArrayList<LBSUserInfo>();
			// 取完了
		} else if (total + 50 > lbsUserInfos.size()) {
			partList = lbsUserInfos.subList(total, lbsUserInfos.size());
		} else {
			partList = lbsUserInfos.subList(total, total + 50);
		}
		for (int i = 0; i < partList.size(); i++) {

			LBSUserInfo lbsUser = partList.get(i);

			int uid = lbsUser.getUid();

			// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
			TX tx = TX.tm.getTx(uid);

			if (uid == TX.tm.getUserID()) {
				tx = TX.tm.getTxMe();
			}
			if (tx != null) {
				copyInfo2LBSUser(tx, lbsUser);
				if (sex != QUERY_ALL && lbsUser.getSex() != sex) {
					continue;
				}
			}
			synchronized (nearbyUsers) {
				if (!nearbyUsers.contains(lbsUser)) {
					nearbyUsers.add(lbsUser);
				}
			}
		}

		if (nearbyHandler != null) {
			nearbyHandler.sendMessage(message);
		} else {
			if (Utils.debug) {
				Log.e(TAG, "dealNearbyPeople: nearbyHandler == null");
			}
		}

	}

	public JSONObject handler(String json) {
		HttpPost request = createHttpPost();
		request.setURI(URI.create(url));
		HttpEntity entity = null;
		try {
			entity = new StringEntity(json, "utf-8");
		} catch (UnsupportedEncodingException ex) {
		}
		request.setEntity(entity);
		JSONObject jsonResponse = null;
		try {
			jsonResponse = executeRequest(request);
			//附近的人数据正确返回了，给服务器发送访问了附近的人的数据
			mSess.getSocketHelper().sendViewNearbyPeople();
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return jsonResponse;
	}

	private JSONObject executeRequest(HttpPost request) throws ParseException,
			IOException, org.json.JSONException {
		// Execute the request and try to decode the JSON Response
		HttpClient httpClient = new DefaultHttpClient();

		long t = System.currentTimeMillis();
		HttpResponse response = httpClient.execute(request);
		if (Utils.debug) {
			Log.i(TAG, "响应码：" + response.getStatusLine().getStatusCode());
		}

		long time = System.currentTimeMillis() - t;

		HttpEntity resEntity = response.getEntity();
		String responseString = EntityUtils.toString(resEntity);
		responseString = responseString.trim();
		JSONObject jsonResponse = new JSONObject(responseString);
		return jsonResponse;
	}

	private HttpPost createHttpPost() {
		HttpPost post = new HttpPost();
		post.addHeader("ver", "" + Utils.appver);
		post.addHeader("os", Utils.os);
		post.addHeader("osv", Utils.osv);
		post.addHeader("chl", "" + Utils.cid);
		post.addHeader("app", Utils.app);
		post.addHeader(HTTP.CONTENT_TYPE, "application/json-rpc");
		return post;
	}

	public String uploadLocationJson(double lat, double lon) {
		String json = "";
		try {
			json = new JSONStringer().object().key("method")
					.value("uploadGEOInfo").key("params").array()
					.value(TX.tm.getTxMe().partner_id)
					.value(TX.tm.getTxMe().token).value(lat).value(lon)
					.endArray().key("id").value(33).endObject().toString();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return json;
	}

	private String getNearlyFriendJson(double lat, double lon) {
		String json = "";
		try {
			json = new JSONStringer().object().key("method")
					.value("nearbyUsers").key("params").array()
					.value(TX.tm.getTxMe().partner_id)
					.value(TX.tm.getTxMe().token).value(lat).value(lon)
					.endArray().key("id").value(33).endObject().toString();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return json;
	}

	/**
	 * 将信息从TX对象copy到LBSUserInfo对象
	 */
	private void copyInfo2LBSUser(TX src, LBSUserInfo dest) {
		if (src == null || dest == null) {
			return;
		}
		dest.setNickName(src.getNick_name());
		dest.setSex(src.getSex());
		dest.setAvatar(src.avatar_url);
		dest.setSignature(src.sign);
		dest.setAge(src.age);
		dest.setArea(src.area);
	}

	/**
	 * 返回清除位置结果, 3305
	 */
	private void dealClearLocation(String msg) {
		try {
			JSONObject jo = new JSONObject(msg);
			Message message = new Message();
			message.what = MsgInfor.SERVER_CLEAR_LOCATION;
			Map<String, Object> map = new HashMap<String, Object>();
			int d = jo.getInt("d");
			message.arg1 = d;
			message.obj = map;
			if (handler != null) {
				handler.sendMessage(message);
			} else {
				if (Utils.debug) {
					Log.e(TAG, "dealClearLocation: handler == null");
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 返回个人位置, 3303
	 */
	private void dealMyLocation(String msg) {
		try {
			JSONObject jo = new JSONObject(msg);
			Message message = new Message();
			message.what = MsgInfor.SERVER_MY_LOCATION;
			Map<String, Object> map = new HashMap<String, Object>();
			int d = jo.getInt("d");
			message.arg1 = d;
			if (d == 0) {
				double lat = jo.getDouble("lat");
				double lng = jo.getDouble("lng");
				map.put(LBSSocketHelper.LAT, lat);
				map.put(LBSSocketHelper.LNG, lng);
			}
			message.obj = map;
			if (handler != null) {
				handler.sendMessage(message);
			} else {
				if (Utils.debug) {
					Log.e(TAG, "dealMyLocation: handler == null");
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 上报位置确认, 3301
	 */
	private void dealLocationInfo(String msg) {
		try {
			JSONObject jo = new JSONObject(msg);
			Message message = new Message();
			message.what = MsgInfor.SERVER_LOCATION_INFO;
			Map<String, Object> map = new HashMap<String, Object>();
			int d = jo.getInt("d");
			message.arg1 = d;
			// 无效经纬度
			if (d == 3) {
				map.put(LBSSocketHelper.ERROR_MSG,
						jo.optString("msg") != null ? jo.optString("msg")
								: "无效经纬度");
			}
			message.obj = map;
			if (handler != null) {
				handler.sendMessage(message);
			} else {
				if (Utils.debug) {
					Log.e(TAG, "dealLocationInfo: handler == null");
				}
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

//	/**
//	 * 从json中生成TXMessage对象
//	 */
//	private TXMessage getTXMessage(JSONObject jo, int cid)
//			throws JSONException, org.json.JSONException {
//		TXMessage txmsg = new TXMessage();
//		int type = jo.getInt("msgtype");
//		txmsg.msg_id = jo.getLong("msgid");
//		txmsg.msg_type = type;
//		switch (type) {
//		case TxDB.MSG_TYPE_COMMON_SMS:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_COMMON_SMS;
//			txmsg.msg_body = jo.getString("ct");
//			txmsg.read_state = TXMessage.READ;
//			break;
//		case TxDB.MSG_TYPE_GEO_SMS:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_IMAGE_EMS;
//			txmsg.msg_url = jo.getString("imgurl");
//			txmsg.read_state = TXMessage.READ;
//			break;
//		case 7:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_GEO_SMS;
//			txmsg.read_state = TXMessage.READ;
//			break;
//		case TxDB.MSG_TYPE_AUDIO_EMS:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_AUDIO_EMS;
//			txmsg.msg_url = jo.getString("aduurl");
//			txmsg.audio_times = jo.getLong("adut");
//			txmsg.read_state = TXMessage.UNREAD;
//			break;
//		case TxDB.MSG_TYPE_CARD_EMS:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_CARD_EMS;
//			txmsg.msg_url = jo.getString("vcurl");
//			txmsg.tcard_name = jo.getString("vcnn");
//			txmsg.read_state = TXMessage.READ;
//			break;
//		case TxDB.MSG_TYPE_TCARD_SMS:
//			txmsg.msg_type = TxDB.MSG_TYPE_QU_TCARD_SMS;
//			txmsg.tcard_id = jo.getLong("tvcid");
//			txmsg.tcard_name = jo.getString("tvcnn");
//			txmsg.tcard_sex = jo.getInt("tvcsex");
//			txmsg.tcard_avatar_url = jo.getString("tvcaurl");
//			txmsg.read_state = TXMessage.READ;
//			break;
//		}
//		addOtherInfor(cid, txmsg);
//		return txmsg;
//	}

	/**
	 * 给txmsg增加group_name和group_avatars_url
	 */
	private void addOtherInfor(int cid, TXMessage txmsg) {
		synchronized (channelList) {
			for (ChatChannel cc : channelList) {
				if (cc.getChannelId() == cid) {
					txmsg.group_name = cc.getSubject();
					txmsg.group_avatars_url = cc.getIconUrl();
					break;
				}
			}
		}
	}


	/**
	 * 生成代表加入频道结果的Bundle对象
	 * 
	 * @throws org.json.JSONException
	 */
	private Bundle generateJoinResultBundle(JSONObject joo)
			throws JSONException, org.json.JSONException {
		boolean fg = joo.getBoolean("fg");
		boolean trans = joo.getBoolean("trans");
		double lat = joo.getDouble("lat");
		double lng = joo.getDouble("lng");
		int range = joo.getInt("range");
		int bc1 = joo.getInt("bc1");
		int bc2 = joo.getInt("bc2");
		Bundle bundle = new Bundle();
		bundle.putBoolean("fg", fg);
		bundle.putBoolean("trans", trans);
		bundle.putDouble("lat", lat);
		bundle.putDouble("lng", lng);
		bundle.putInt("range", range);
		bundle.putInt("bc1", bc1);
		bundle.putInt("bc2", bc2);
		return bundle;
	}

	/**
	 * 底层持有的频道列表
	 */
	private ArrayList<ChatChannel> channelList;

	/**
	 * 标识获取列表返回时的Message
	 */
	public static final int LBS_CHANNEL_LIST_ADD = 7789;
	/**
	 * 标识获取列表返回时的Message, 此时server已经没有更多频道
	 */
	public static final int LBS_CHANNEL_LIST_ADD_LAST = 7790;
	/**
	 * 标识搜索频道返回时的Message
	 */
	public static final int LBS_CHANNEL_RETURN = 7791;
	/**
	 * 标识其余引起频道列表改变的原因
	 */
	public static final int LBS_CHANNEL_LIST_UPDATE = 7792;


	/**
	 * 将channel对象添加到list中
	 */
	private void addChannelToList(List<ChatChannel> channels) {
		synchronized (channelList) {
			for (ChatChannel cc : channels) {
				int index = channelList.indexOf(cc);
				if (index == -1) {
					channelList.add(cc);
				} else {
					channelList.set(index, cc);
				}
			}
		}
	}


	/**
	 * 从json中获得ChatChannel对象
	 * 
	 * @throws org.json.JSONException
	 */
	private ChatChannel getChatChannel(JSONObject jo) throws JSONException,
			org.json.JSONException {
		int id = jo.getInt("id");
		int ver = jo.getInt("ver");
		int count = jo.getInt("count");
		boolean join = jo.getBoolean("join");
		ChatChannel channel = new ChatChannel();
		if (!channelOrder.toString().contains(id + "&")
				&& !channelOrder.toString().contains("&" + id)) {
			if (channelOrder.length() == 0) {
				channelOrder.append(id);
			} else {
				channelOrder.append("&" + id);
			}
		}
		channel.setChannelId(id);
		channel.setChannelVer(ver);
		channel.setPeopleNum(count);
		channel.setJoin(join);
		channel.setClosed(false);

		TuiboSlienceManager tsm = TuiboSlienceManager.getInstance();
		int pri = jo.optInt("pri", -111);
		if (pri != -111) {
			tsm.updateChannelPri(pri, true, id);
		}

		if ("".equals(jo.optString("subject"))) {
			return channel;
		}
		String subject = jo.getString("subject");
		String content = jo.getString("content");
		String icon_url = jo.getString("icon_url");
		int range = jo.getInt("range");
		int prop = jo.getInt("prop");
		int createUid = jo.getInt("createuid");
		long createTime = jo.getLong("createdateline");
		channel.setCreateTime(createTime);
		String notice = jo.getString("notice");
		channel.setSubject(subject);
		channel.setContent(content);
		channel.setIconUrl(icon_url);
		channel.setRange(range);
		channel.setProp(prop);
		channel.setCreateUid(createUid);
		channel.setNotice(notice);
		return channel;
	}

}