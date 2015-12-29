package com.tuixin11sms.tx;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.activity.TabSetActivity;
import com.tuixin11sms.tx.activity.SettingsPreference;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ChatChannel;
import com.tuixin11sms.tx.net.LBSMsgHandleService;
import com.tuixin11sms.tx.net.LBSSocketHelper;
import com.tuixin11sms.tx.net.TuiboSlienceManager;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 整个应用的application
 * 
 */
public class TxData extends Application {
	private static final String TAG = "TxData";
	public static final String FLUSH_TXS = "flush txs";
	public static final String FLUSH_MSGS = "flush msgs";
	public static int HEADSET_STATE = Utils.HEADSET_OUT;
	public static Map<String, String> downloadMap = new HashMap<String, String>();
	public static TxGroup txGroup;// 群
	private static SharedPreferences prefsMeme;
	public static Editor editorMeme;
	public static Bitmap cardBitmap;
	public static boolean isContactLoaded;
	public static boolean isCall;
	public static Context context;
	// ksz
	// 服务器断开连接的toast变量
	public static Toast noNetToast;
	/**公用经纬度 以及记录的时间 */
	public static double public_latitude;
	public static double public_longitude;
	public static long public_location_time;
	public static String public_location_info;// 具体位置信息
	public volatile static long time = 0;
	
	public static SessionManager mSess = null;	

	@Override
	public void onCreate() { 
		super.onCreate();
		context = this;
		int pid=android.os.Process.myPid();
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningAppProcessInfo> allprocs=am.getRunningAppProcesses();
		for(RunningAppProcessInfo info : allprocs){
			if(info.pid==pid){
				if(info.processName.contains(":"))
					return;
				break;
			}
		}

		
		
		//如果是debug状态，则读取sharedpreference，确定登陆哪个服务器
		if (Utils.debug) {
			SharedPreferences pprefs_set = getSharedPreferences(SettingsPreference.TUIXIN_SETTING, Context.MODE_PRIVATE);
			if (pprefs_set.contains(TabSetActivity.LOGIN_TEST_SERVER)) {
				Utils.test = pprefs_set.getBoolean(TabSetActivity.LOGIN_TEST_SERVER, true);
				//重新加载host和port，因为设置host和port是在Utils的静态代码块中进行，优先于这里Utils的test变量设置
				Utils.loadHostAndPort();
			}
		}
		
		Utils.initContext(this);//传递context给Utils
		
		if(Utils.debug){
			Log.i(TAG, "SP中登陆的信息："+getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_PRIVATE).getString("user_login_infors", ""));
		}
//		MmsUtils.init(this);
		ClientManager.init(this);
		
//		SocketHelper.getSocketHelper(this);//会间接调用到LoginSessionMagnager.setAutoLoginInfor方法
		
		mSess = SessionManager.getManager(this);
		mSess.initHelper();
		
		LBSSocketHelper.getLBSSocketHelper(this);
		TuiboSlienceManager.init(this);
		DataContainer.init(this);// 初始化数据容器，加载assets中json文件为对象
		setUitlsCid();//打包时需要用到
		// localHandlerThread = new HandlerThread("myexcatch");
		// localHandlerThread.start();
		// mHandler = new Handler(localHandlerThread.getLooper());
		// Thread.setDefaultUncaughtExceptionHandler(new
		// CrashLogger("Crash.log"));
		if (!Utils.debug) {
			myexcatch();
		}
		NotificationPopupWindow.mSess = mSess;
		prefsMeme = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		editorMeme = prefsMeme.edit();
		
		EndCallListener callListener = new EndCallListener();
		TelephonyManager mTM = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
		mTM.listen(callListener, PhoneStateListener.LISTEN_CALL_STATE);
	}

	//暂时调用的方法，后续修改删掉  2014.04.22 shc
	//没有get的引用，清除也没有用吧？2014.04.23 shc
//	public void clearChannel() {
//		if (channelCache != null)
//			channelCache.clear();
//		SocketHelper.upCount = 0;
//
//	}
	
//	public void clear() {
//		NotificationPopupWindow.cancelNotification(this);
//		mSess.mPrefMeme.auth.setVal(0);
//		mSess.mPrefMeme.user_id.setVal("");
//		mSess.mPrefMeme.nickname.setVal("");
//		mSess.mPrefMeme.token.setVal("");
//		mSess.mPrefMeme.telephone.setVal("");
//		mSess.mPrefMeme.auth.setVal(TxDB.TX_AUTH_NORMAL);
//		mSess.mPrefMeme.is_bind_phone.setVal(false);
//		mSess.mPrefMeme.email.setVal("");
//		mSess.mPrefMeme.is_bind_email.setVal(false);
//		mSess.mPrefMeme.sign.setVal("");
//		mSess.mPrefMeme.birthday.setVal(0);
//		mSess.mPrefMeme.age.setVal(0);
//		mSess.mPrefMeme.constellation.setVal("");
//		mSess.mPrefMeme.bloodtype.setVal(0);
//		mSess.mPrefMeme.hobby.setVal("");
//		mSess.mPrefMeme.job.setVal("");
//		mSess.mPrefMeme.area.setVal("");
//		mSess.mPrefMeme.sex.setVal(TX.DEFAULT_SEX);
//		mSess.mPrefMeme.avatarver.setVal(0);
//		mSess.mPrefMeme.avatar_url.setVal("");
//		mSess.mPrefMeme.friendver.setVal(0);
//		mSess.mPrefMeme.avatar_path.setVal("");
//		mSess.mPrefMeme.account_type.setVal(LoginSessionManager.SHEN_LIAO_ACCOUNT);
//		mSess.mPrefMeme.weibo_user_id.setVal("");
//		mSess.mPrefMeme.weibo_token.setVal("");
//		mSess.mPrefMeme.weibo_token_secret.setVal("");
//		mSess.mPrefMeme.auth_type.setVal(LoginSessionManager.INVALID_OATH);
//		mSess.mPrefMeme.album.setVal("");
//		mSess.mPrefMeme.album_version.setVal(-1);
//		mSess.mPrefMeme.languages.setVal("").commit();
//		
//		TX.tm.reloadTXMe();///////
//		TX.tm.clearTXCache();
//		
//		
//		MsgStat.clearMsgStats(true,this);
//		if (channelCache != null)
//			channelCache.clear();
//		SocketHelper.upCount = 0;
//		
//		if(Utils.debug){
//			Log.i(TAG, "TxData 清空了所有的数据");
//		}
//	}


	private class EndCallListener extends PhoneStateListener {
		@Override
		public void onCallStateChanged(int state, String incomingNumber) {
			switch (state) {
			case TelephonyManager.CALL_STATE_IDLE:// 闲置或结束电话
				isCall = false;
				break;
			case TelephonyManager.CALL_STATE_OFFHOOK:// 通话中
			case TelephonyManager.CALL_STATE_RINGING:// 来电
				isCall = true;
				break;
			default:
				isCall = false;
				break;

			}
		}
	}

//	/**
//	 * 是否是第三方账号登录神聊 如新浪
//	 * 
//	 * @return true 是 false 不是
//	 */
//	public boolean isOtherAccount() {
////		int type = prefsMeme.getInt(CommonData.ACCOUNT_TYPE, LoginSessionManager.SHEN_LIAO_ACCOUNT);
//		int type = mSess.mPrefMeme.account_type.getVal();
//		if (type != LoginSessionManager.SHEN_LIAO_ACCOUNT) {
//			return true;
//		} else {
//			return false;
//		}
//	}

	private void myexcatch() {
		// localHandlerThread = new HandlerThread("myexcatch");
		// localHandlerThread.start();
		// mHandler = new Handler(localHandlerThread.getLooper());
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(this));
	}


//	/**
//	 * 弹出软键盘
//	 * 
//	 * @param context
//	 */
//	public void openKeyboard(View context) {
//		InputMethodManager imm = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
//		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
//		imm.showSoftInput(context, InputMethodManager.SHOW_IMPLICIT);
//	}

//	public ArrayList<TXMessage> getFriendHelperList() {
//		ArrayList<TXMessage> tmp = new ArrayList<TXMessage>();
//		Cursor cur = getContentResolver()
//				.query(TxDB.Messages.CONTENT_URI,
//						null,
//						TxDB.Messages.MSG_PARTNER_ID + "= ? AND " + TxDB.Messages.MSG_TYPE + " <> ? and "
//								+ TxDB.Messages.TCARD_ID + " > 0 and " + TxDB.Messages.TCARD_ID + " <> ? ",
//						new String[] { "" + TX.TUIXIN_FRIEND, "" + TxDB.MSG_TYPE_TCARD_SMS,
//								"" + TX.tm.getTxMe().partner_id }, TxDB.Messages.SEND_TIME);
//		if (cur != null) {
//			tmp = TXMessage.fetchAllDBMsgs(cur);
//			cur.close();
//		}
//		return tmp;
//	}


	public static boolean isOP() {
		// return true;
		TX tx = TX.tm.getTxMe();
		if (tx.auth == TxDB.TX_AUTH_S_OP || tx.auth == TxDB.TX_AUTH_OP) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isCloOP() {
		// return true;
		TX tx = TX.tm.getTxMe();
		if (tx.auth == TxDB.TX_AUTH_CLO_OP) {
			return true;
		} else {
			return false;
		}
	}


	public static List<TXMessage> unreadMessageList = new ArrayList<TXMessage>();
	public static Set<Long> messageIds = new HashSet<Long>();

	public static void addUnreadMessage(TXMessage msg) {
		unreadMessageList.add(msg);
		messageIds.add(msg.msg_id);
	}

	public static boolean isExists(Long id) {
		if (messageIds != null && messageIds.size() != 0) {
			if (messageIds.contains(id))
				return true;
		}
		return false;
	}

	public static TXMessage getNewUnreadMessage() {
		if (unreadMessageList.size() > 0) {
			return unreadMessageList.get(unreadMessageList.size() - 1);
		}
		return null;
	}

	public static void clearOldMsg(Long id, boolean special) {
		TXMessage waitDel = null;
		if (unreadMessageList.size() != 0) {
			// unreadMessageList.remove(unreadMessageList.size() - 1);
			for (TXMessage currentMsg : unreadMessageList) {
				if (id == currentMsg.msg_id) {
					waitDel = currentMsg;
					break;
				}
			}

			if (special) {
				if (messageIds != null && messageIds.size() != 0) {
					messageIds.remove(getNewUnreadMessage().msg_id);
				}
				unreadMessageList.remove(getNewUnreadMessage());
			} else if (waitDel != null) {
				if (messageIds != null && messageIds.size() != 0) {
					messageIds.remove(waitDel.msg_id);
				}
				unreadMessageList.remove(waitDel);
			}

		}
	}

	public static void clearOldMsg(List<Long> msgs) {
		List<TXMessage> watiDels = new ArrayList<TXMessage>();
		for (Long wait : msgs) {
			for (TXMessage currentMsg : unreadMessageList) {
				if (wait == currentMsg.msg_id) {
					watiDels.add(currentMsg);
				}
			}
		}
		if (watiDels.size() != 0) {
			unreadMessageList.removeAll(watiDels);
		}
	}

	public static void clearGroupMsg(Long groupid) {
		List<TXMessage> watiDels = new ArrayList<TXMessage>();
		for (TXMessage currentMsg : unreadMessageList) {
			if (currentMsg.group_id == groupid) {
				watiDels.add(currentMsg);
			}
		}
		if (watiDels.size() != 0) {
			unreadMessageList.removeAll(watiDels);
		}
	}

	public static void clearPersonMsg(Long partnerId) {
		List<TXMessage> watiDels = new ArrayList<TXMessage>();
		for (TXMessage currentMsg : unreadMessageList) {
			if (currentMsg.partner_id == partnerId) {
				watiDels.add(currentMsg);
			}
		}
		if (watiDels.size() != 0) {
			unreadMessageList.removeAll(watiDels);
		}
	}

	public static int getUnreadSize() {
		if (unreadMessageList == null)
			return 0;
		return unreadMessageList.size();
	}


	public static Stack<Activity> stackActivity;

	public static void addActivity(Activity activity) {
		if (stackActivity == null)
			stackActivity = new Stack<Activity>();
		stackActivity.add(activity);
	}

	public static Activity getTopActivity() {
		if (stackActivity == null && stackActivity.size() != 0) {
			return stackActivity.get(stackActivity.size() - 1);
		}
		return null;
	}

	public static void popActivityRemove(Activity activity) {
		if (stackActivity != null && stackActivity.contains(activity)) {
			stackActivity.remove(activity);
		}
	}

	public static void finishAll() {
		if (stackActivity != null) {
			for (Activity a : TxData.stackActivity) {
				a.finish();
			}
			TxData.stackActivity.clear();
		}
	}

	public static void finishOne(String clazzName) {
		if (stackActivity != null) {
			for (Activity activity : stackActivity) {
				if (activity.getComponentName().getClassName().equals(clazzName)) {
					activity.finish();
				}
			}
		}
	}

	
	//TODO 需要挪出去 2014.04.22 shc
//	public void broadcastMsg(String msg) {
//		if (!msg.trim().equals("")) {
//			// 指定广播目标的 action （注：指定了此 action 的 receiver 会接收此广播）
//			Intent intent = new Intent(Constants.INTENT_ACTION_FLUSH);
//			// 需要传递的参数
//			intent.putExtra("msg", msg);
//			// 发送广播
//			sendBroadcast(intent);
//		}
//
//	}


	//这个方法废弃掉，都改用TxGroup的findGroupById  2014.01.23 shc
//	public TxGroup getTxGroupByGroupId4DB(long groupId) {
//		TxGroup txGroup = null;
//		if (Utils.isIdValid(groupId)) {
//			Cursor cur = this.getContentResolver().query(TxDB.Qun.CONTENT_URI, null, TxDB.Qun.QU_ID + "=?",
//					new String[] { "" + groupId }, null);
//			if (cur != null) {
//				if (cur.moveToNext()) {
//					txGroup = TxGroup.fetchOneGroup(cur);
//				}
//				cur.close();
//			}
//		}
//		return txGroup;
//	}


	//引用解除，待删除  2014.02.25 shc
//	public int px2dip(float pxValue) {
//		final float scale = getResources().getDisplayMetrics().density;
//		return (int) (pxValue / scale + 0.5f);
//	}
//
//	public int dip2px(float dipValue) {
//		final float scale = getResources().getDisplayMetrics().density;
//		return (int) (dipValue * scale + 0.5f);
//	}

	
	//挪到了LoginSessionManager中 2014.04.23 shc
//	public void uploadSinaFriend() {
//		Utils.executorService.submit(new Runnable() {
//			@Override
//			public void run() {
//				// Looper.prepare();
//				String userId = prefsMeme.getString(CommonData.WEIBO_USER_ID + "�" + TX.tm.getUserID(), "");
//				if (Utils.isNull(userId))
//					return;
////				long lastTime = prefsMeme.getLong(CommonData.WEIBO_UPLOAD_LAST_TIME, 0);
//				long lastTime = mSess.mPrefMeme.weibo_upload_last_time.getVal();
//				long day = 1000 * 60 * 60 * 24;
//				long now = System.currentTimeMillis();
//				if ((now > lastTime + day) && !"".equals(userId)) {
////					prefsMeme.edit().putLong(CommonData.WEIBO_UPLOAD_LAST_TIME, now).commit();
//					mSess.mPrefMeme.weibo_upload_last_time.setVal(now).commit();
//					String token = prefsMeme.getString(CommonData.WEIBO_TOKEN + "�" + TX.tm.getUserID(), "");
//					String tokenSecret = prefsMeme.getString(CommonData.WEIBO_TOKEN_SECRET + "�" + TX.tm.getUserID(), "");
//					if (!Utils.isNull(token) && !Utils.isNull(tokenSecret)) {
//						/*
//						 * Weibo weibo = new Weibo(); weibo.setToken(token,
//						 * tokenSecret); try { // 粉丝 IDs followers =
//						 * weibo.getFollowersIDSByUserId(userId, 0); Long[] fo =
//						 * followers.getIDs(); List<Long> fos =
//						 * Arrays.asList(fo); // 关注 IDs friends =
//						 * weibo.getFriendsIDSByUserId(userId, 0); Long[] fr =
//						 * friends.getIDs(); List<Long> frs = Arrays.asList(fr);
//						 * // 交集 List<Long> results = new ArrayList<Long>();
//						 * 
//						 * for (long l : fos) { if (frs.contains(l)) {
//						 * results.add(l); } } if (results.size() == 0) {
//						 * return; } int size = results.size(); int count = size
//						 * / 50; int yushu = size % 50; int i = 0; for (i = 0; i
//						 * < count; i++) { int index = i * 50; int end = (i + 1)
//						 * * 50 - 1;
//						 * SocketHelper.getSocketHelper(TxData.this).sendUpSNS
//						 * (0, userId, results.subList(index, end),
//						 * prefs.getBoolean(CommonData.WEIBO_UPLOAD_FIRST,
//						 * true)); } if (yushu != 0) {
//						 * SocketHelper.getSocketHelper
//						 * (TxData.this).sendUpSNS(0, userId,
//						 * results.subList(count * 50, count * 50 + yushu - 1),
//						 * prefs.getBoolean(CommonData.WEIBO_UPLOAD_FIRST,
//						 * true)); } } catch (WeiboException e) {
//						 * e.printStackTrace(); }
//						 */
//					}
//				}
//
//			}
//		});
//	}

	
	
	//没有get的引用，添加和删除都没有用吧？2014.04.23 shc
//	public void addChannel(ChatChannel channel) {
//		channelCache.put(String.valueOf(channel.getChannelId()), channel);
//	}
//
//	public void removeChannel(ChatChannel channel) {
//		if (channel.getChannelId() > 0 && channelCache.containsKey(String.valueOf(channel.getChannelId()))) {
//			channelCache.remove(String.valueOf(channel.getChannelId()));
//		}
//	}

	
	//无引用 2014.04.23 shc
//	public ChatChannel getChannel(int channelId) {
//		if (channelCache.containsKey(String.valueOf(channelId))) {
//			return channelCache.get(String.valueOf(channelId));
//		} else {
//			ChatChannel channel = DAOFactory.getInstance().getChannelDAO().getChannelById(channelId);
//			if (channel != null) {
//				channelCache.put(String.valueOf(channelId), channel);
//				return channel;
//			}
//		}
//		return null;
//	}


//	Timer outtime2;
//	ProgressDialog progress2;

	public void stopService() {
		Intent intent = new Intent(this, TuixinService1.class);
		stopService(intent);
		Intent intent1 = new Intent(this, ShenliaoOtherLoginService.class);
		stopService(intent1);
		Intent intent2 = new Intent(this, LBSMsgHandleService.class);
		stopService(intent2);

	}

	public static void saveDeviceId(String deviceId) {
//		if (editorMeme != null) {
//			editorMeme.putString(CommonData.DEVICE_ID, deviceId).commit();
			mSess.mPrefMeme.device_id.setVal(deviceId).commit();
//		}
	}

	public static String getDeviceId() {
//		if (prefsMeme != null) {
//			return prefsMeme.getString(CommonData.DEVICE_ID, "");
			return mSess.mPrefMeme.device_id.getVal();
//		}
//		return "";
	}

	private void setUitlsCid(){
		try {
			JSONObject jsonObject=new JSONObject(Utils.is2String(this.getApplicationContext().getAssets().open("cid.json")));
			Utils.appver=jsonObject.optInt("appver");
			Utils.cid =jsonObject.optInt("cid",33);
			Utils.buildNo=jsonObject.optInt("buildNo",1);
			Utils.tuixinver=jsonObject.optInt("tuixinver");
			Utils.apptype=jsonObject.getString("apptype");
			if (Utils.debug) {
				Log.i(TAG, "cid-->" + Utils.cid + "appver-->" + Utils.appver+"buildNo-->"+Utils.buildNo+"tuixinver-->"+Utils.tuixinver
						+"apptype-->"+Utils.apptype
						);
			}
		} catch (IOException e) {
			if(Utils.debug)e.printStackTrace();
		} catch (JSONException e) {
			if(Utils.debug)e.printStackTrace();
		}
	}
}