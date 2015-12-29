package com.tuixin11sms.tx;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Binder;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.Toast;

import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgHelper;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class TuixinService1 extends Service {
	private static final String TAG = TuixinService1.class.getSimpleName();
	private static boolean isOnCreated = false;//判定是否开启服务的标记，主要用于应用开启时，自动登陆收到服务成功应答时，如果service已开启则发广播，未开启则等待
//	private SocketHelper socketHelper;
	private PendingIntent sender;
	private AlarmManager am;
	private sendReceiver sr;
	private msgLastTimeReceiver mr;
//	private getReceiver gr;
	private SdCardChangeReceiver sdr;
	private ConnectionChangeReceiver ccr;
	private ScreenReceiver screenReceiver;
//	public static HashMap<String, String> activityState = new HashMap<String, String>();
//	public static boolean notNeedCheckActivityState;
	private boolean isFirst = true;
	public static boolean needGetInfor;
	public static long startTime;
	private ContactAPI contactapi;
	private ContentResolver cr;
//	private MsgHelper msghelper;
	private boolean onCheckContact;
//	private ParseHandler parseHandler;

	public class LocalBinder extends Binder {
		public TuixinService1 getService() {
			return TuixinService1.this;
		}
	}


	//这个应该是手机本地通讯录发生改变时就会调用  2013.10.16  shc
	public Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1:
				Utils.executorService.submit(new Runnable() {
					@Override
					public void run() {
						HashMap<Long, String> conCache = TX.tm.getContactsCache();
						synchronized (conCache) {
							//重新填充本地联系人
							conCache.clear();
							contactapi.fillAllContacts(cr, conCache);
						}
						//同时更新好友缓存中本地联系人的字段
						TX.tm.syncTBTXs();
						
//						ArrayList<TX> txs = contactapi.SyncContacts(txData, TuixinService1.this,false);
//						TX.syncTXstoDB(txs, cr);
//						TX.getLocalList();//没什么用啊，取到好友列表后又没有什么操作。2013.10.17
//						TX.getTBListFilterNPC(false);//没什么用啊，取到好友列表后又没有什么操作。2013.10.14
						TX.tm.getTBTXList();
						SessionManager.broadcastMsg(TxData.FLUSH_TXS);//统一在txData处理
						if (Utils.debug) {
							Log.i(TAG, "syncTXstoDB,getLocalList,getTBListFilterNPC之后，发送刷新TX的广播，下面开始同步通讯录");
							Log.i(TAG, "++++++++++++upaddr+++++");
						}
						if (!mSess.getSocketHelper().upAddr)
							mSess.getSocketHelper().upAddressBook(TuixinService1.this);
						onCheckContact = false;
						// Looper.loop();
					}
				});
				break;

			case 2:
				Toast.makeText(TuixinService1.this, R.string.net_not_available, Toast.LENGTH_LONG)
						.show();
				break;
			case 3:
				Toast.makeText(TuixinService1.this, R.string.net_not_support, Toast.LENGTH_LONG)
						.show();
				break;
			}
		}
	};

	public void observerContact() {
		getContentResolver().registerContentObserver(contactapi.getContactUri(), true,
				new TuixinContentObserver(handler));
	}

	class TuixinContentObserver extends ContentObserver {
		private Handler handler;

		public TuixinContentObserver(Handler handler) {
			super(handler);
			this.handler = handler;
		}

		public void onChange(boolean selfChange) {
			super.onChange(selfChange);
			// System.out.println("TuixinContentObserver  onChange(selfChange);...");
			if (!onCheckContact) {
				onCheckContact = true;
				Timer checkContactTimer = new Timer();
				checkContactTimer.schedule(new TimerTask() {
					public void run() {
						Message msg = new Message();
						msg.what = 1;
						handler.sendMessage(msg);
					}
				}, 2000);
			}
		}
	}

//	public static boolean checkAllStop() {
//		if (notNeedCheckActivityState) {
//			notNeedCheckActivityState = false;
//			return false;
//		} else {
//			Set<String> set = activityState.keySet();
//			Iterator<String> it = set.iterator();
//			while (it.hasNext()) {
//				String key = it.next();
//				if (activityState.get(key).equals("onStart")) {
//					return false;
//				}
//			}
//			return true;
//		}
//	}


	private final IBinder mBinder = new LocalBinder();

	public IBinder onBind(Intent intent) {
		if(Utils.debug)Log.d(TAG, "onBind");
		registAllReceiver();
		return mBinder;
	}

	// 重新绑定时调用该方法
	public void onRebind(Intent intent) {
		if(Utils.debug)Log.d(TAG, "onRebind");
		registAllReceiver();
		super.onRebind(intent);
	}

	// 解除绑定时调用该方法
	public boolean onUnbind(Intent intent) {
		if(Utils.debug)Log.d(TAG, "onUnbind");
		return super.onUnbind(intent);
	}


	private SessionManager mSess;
	
	public void onCreate() {
		super.onCreate();
		// if(parseTask == null){
		// parseTask = new ParseTask();
		// Utils.executorService.submit(parseTask);
		// }
		if (Utils.debug)
			Log.e(TAG, " service ++++++++++++++++++++++++++++++++++++++++onCreate");
		mSess = SessionManager.getInstance();
		inits();
		observerContact();
		registAllReceiver();
		isOnCreated = true;//服务已开启，且广播已注册，置开启标记为true
	}

	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		if (Utils.debug)
			Log.e(TAG, "service ++++++++++++++++++++++++++++++++++++++++ onStart");
		if (isFirst) {
			if (Utils.debug)
				Log.i(TAG, "========isFirst======================================" + isFirst);
			isFirst = false;
			Intent in = new Intent(TuixinService1.this, Alarmreceiver.class);
			in.setAction("TuixinServicecheck");
			sender = PendingIntent.getBroadcast(TuixinService1.this, 0, in, 0);
			// 开始时间
			long firstime = SystemClock.elapsedRealtime();
			am = (AlarmManager) getSystemService(ALARM_SERVICE);
			// 180秒一个周期，不停的发送广播
			am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, firstime, 180 * 1000, sender);
			startTime = System.currentTimeMillis();
			if (intent != null) {
				String bootStart = intent.getStringExtra(Constants.EXTRA_BOOT_START_KEY);
				if (Utils.debug)
					Log.i(TAG, "bootStart value is:" + bootStart);
				if (bootStart != null && bootStart.equals(Constants.EXTRA_BOOT_START_VALUE))
					initConversations();
			}
		}
		inits();
		observerContact();
		registAllReceiver();
	}

	//整合onCreate和onStart方法中共同初始化的一些变量
	private void inits() {
//		if (parseHandler == null) {
//			HandlerThread handlerThread = new HandlerThread("ParseTx");
//			handlerThread.start();
//			parseHandler = new ParseHandler(handlerThread.getLooper());
//		}
//		if (msghelper == null)
//			msghelper = MsgHelper.CreateMsgHelper(TuixinService1.this);
//		if (socketHelper == null)
//			socketHelper = mSess.getSocketHelper();
		if (contactapi == null) {
			contactapi = ContactAPI.getAPI();
			cr = TuixinService1.this.getContentResolver();
			contactapi.setCr(cr);
		}
	}
	
	
	public void stopTxService() {
		if (am != null && sender != null) {
			am.cancel(sender);
			am = null;
			sender = null;
		}
		this.stopSelf();
	}

	public void onDestroy() {
		// Log.d(TAG, "===========================onDestroy");
		if (sr != null) {
			this.unregisterReceiver(sr);
			sr = null;
		}
		if (mr != null) {
			this.unregisterReceiver(mr);
			mr = null;
		}
		/*
		 * if (mr == null) { this.unregisterReceiver(msr); msr = null; }
		 */
//		if (gr != null) {
//			this.unregisterReceiver(gr);
//			gr = null;
//		}
		if (ccr != null) {
			this.unregisterReceiver(ccr);
			ccr = null;
		}
		if (sdr != null) {
			this.unregisterReceiver(sdr);
			sdr = null;
		}
		unregistReceiver();
//		if (mSess.getSocketHelper() != null) {
			// SocketHelper.closeSocketConnect();
//		}
		super.onDestroy();
	}


	// 实现一个 BroadcastReceiver，用于接收指定的 Broadcast
//	private class getReceiver extends BroadcastReceiver {
//		public void onReceive(Context context, Intent intent) {
//			if (Utils.debug) {
//				Log.e(TAG, "Service收到了广播，执行了onReceive()方法");
//			}
//			final String msg = intent.getStringExtra("msg");
//			if (msg.trim().length() != 0) {
//				if (Utils.debug) {
//					Log.i(TAG, "msg字符串长度>0，开始处理");
//				}
//				int msgType = Utils.getMessageType(msg);
//				if (msgType == MsgInfor.SERVER_INFO_QUN || msgType == MsgInfor.SERVER_USERINFRO) {
//					if (Utils.debug) {
//						Log.i(TAG, "获取群组信息结果|||或者|||返回用户基本信息，不是登陆响应");
//					}
//					// parseTask.putData(msg);
//					if (parseHandler != null) {
//						Message message = parseHandler.obtainMessage();
//						message.obj = msg;
//						parseHandler.sendMessage(message);
//					}
//				} else {
//					if (Utils.debug) {
//						Log.i(TAG, "其他的服务器响应，具体是什么就不知道了。");
//					}
//					new Thread() {
//						public void run() {
//							setDefaultDisplay();//处理前先设置一下分辨率，因为可能是上传图片，需要这个操作
//							
//							if (Utils.debug) {
//								try {
//									int type = 0;
//								    JSONObject jo = new JSONObject(msg);
//									if (jo != null)
//										type  = jo.getInt("mt");
//									if (type==MsgInfor.SERVER_LOGIN) {
//										Log.e(TAG, "在getReceiver广播中调用了msghelper.dealMsg(msg);是登陆成功返回的消息哦！！！！");
//									}else {
//										Log.i(TAG, "不是登陆成功，这个类型type="+type);
//									}
//								} catch (JSONException e) {
//									if (Utils.debug) {
//										Log.e(TAG, "格式化Json异常",e);
//									}
//								}
//								
//							}
//							msghelper.dealMsg(msg);
//						}
//					}.start();
//				}
//			}else {
//				if (Utils.debug) {
//					Log.e(TAG, "msg字符串长度为0，这是怎么回事儿？？？");
//				}
//			}
//		}
//	}

//	private final class ParseHandler extends Handler {
//		private int handleCount;
//
//		public ParseHandler(Looper looper) {
//			super(looper);
//		}
//
//		@Override
//		public void handleMessage(Message message) {
//			if (message.obj != null) {
//				handleCount++;
//				String msg = message.obj.toString();
//				setDefaultDisplay();//处理前先设置一下分辨率，因为可能是上传图片，需要这个操作
//				if (Utils.debug) {
//					try {
//						int type = 0;
//					    JSONObject jo = new JSONObject(msg);
//						if (jo != null)
//							type  = jo.getInt("mt");
//						if (type==MsgInfor.SERVER_LOGIN) {
//							if(Utils.debug)Log.e(TAG, "在ParseHandler中调用了msghelper.dealMsg(msg);是登陆成功返回的消息哦！！！！");
//						}
//					} catch (JSONException e) {
//						if(Utils.debug)e.printStackTrace();
//					}
//					
//				}
//				msghelper.dealMsg(msg);
//				if (Utils.debug)
//					Log.i(TAG, "handleCount is:" + handleCount);
//			}
//		}
//
//	}

	// 实现一个 BroadcastReceiver，用于接收指定的 Broadcast
	private class sendReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = intent.getStringExtra("msg");
			if (msg != null) {
				mSess.getSocketHelper().sendMessage(msg);
			}
			String alarm = intent.getStringExtra("alarm");
			if (alarm != null) {
				if (Utils.debug)
					Log.e(TAG, "alarm != null");
			}
			String exc = intent.getStringExtra("exception");
			if (exc != null) {
				mSess.getSocketHelper().sendException(exc);
			}
			String exit = intent.getStringExtra("exit");
			if (exit != null) {
				TuixinService1.this.stopTxService();
			}
		}
	}

	// 实现一个 BroadcastReceiver，用于接收指定的 Broadcast
	private class msgLastTimeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			NotificationPopupWindow.cancelNotification(context);
			// 保存当前的时间 用于下次读取未读短信数目的时间判断
		}
	}

	private class ConnectionChangeReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			int state = Utils.getNetworkType(context);
			Utils.setNetworkType(state);
			switch (state) {
			case Utils.NET_NORMAL:
			case Utils.NET_WIFI:
				Intent i1 = new Intent(Constants.INTENT_ACTION_NETWORK_LOCATION_SUCCEE);
				context.sendBroadcast(i1);
				break;
			default:
				Intent i = new Intent(Constants.INTENT_ACTION_NETWORK_LOCATION_FAILED);
				context.sendBroadcast(i);
				break;
			}
		}
	}

	// 监听类
	private class SdCardChangeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (Intent.ACTION_MEDIA_MOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_SCANNER_STARTED.equals(action)
					|| Intent.ACTION_MEDIA_SCANNER_FINISHED.equals(action)) {
				// SD卡成功挂载
				if (Utils.debug)
					Log.d(TAG, "1sdcard action:::::" + action);
				File file = new File(Utils.getStoragePath(context), Utils.AVATAR_PATH);
				if (!file.exists()) {
					file.mkdirs();
				}

			} else if (Intent.ACTION_MEDIA_REMOVED.equals(action)
					|| Intent.ACTION_MEDIA_UNMOUNTED.equals(action)
					|| Intent.ACTION_MEDIA_BAD_REMOVAL.equals(action)) {
				// SD卡挂载失败
				if (Utils.debug)
					Log.d(TAG, "2sdcard action:::::" + action);
				Utils.creatNoSdCardInfo(TuixinService1.this);
			}

		}
	};

	private class ScreenReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
				TxData.time = System.currentTimeMillis();
				if(Utils.debug)Log.d("SocketChannelConnectionImpl",
						"screen off, set TxData.time = currentTimeMillis");
			} else if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
				if(Utils.debug)Log.d("SocketChannelConnectionImpl", "screen on, set TxData.time = 0");
				TxData.time = 0;
			}
		}

	}

	
	//因为onCreate、onStart、onBind、onReBind都注册了四个广播，在这里统一处理   2013.10.09  shc
	private void registAllReceiver(){
		if(Utils.debug)Log.i(TAG, "registAllReceiver");
		if (sr == null) {
			sr = new sendReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_SEND_MSG);
			this.registerReceiver(sr, filter);
		}
		if (mr == null) {
			mr = new msgLastTimeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(NotificationPopupWindow.RECEIVE);
			this.registerReceiver(mr, filter);
		}
//		if (gr == null) {
//			gr = new getReceiver();
//			IntentFilter filter = new IntentFilter();
//			filter.setPriority(1000);
//			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
//			this.registerReceiver(gr, filter);
//		}
		if (ccr == null) {
			ccr = new ConnectionChangeReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
			this.registerReceiver(ccr, filter);
		}
		if (sdr == null) {
			sdr = new SdCardChangeReceiver();
			IntentFilter intentFilter = new IntentFilter();
			intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
			intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
			intentFilter.addAction(Intent.ACTION_MEDIA_REMOVED);
			intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
			intentFilter.addAction(Intent.ACTION_MEDIA_BAD_REMOVAL);
			intentFilter.addAction(Intent.ACTION_MEDIA_SHARED);
			intentFilter.addAction(Intent.ACTION_UMS_CONNECTED);
			intentFilter.addAction(Intent.ACTION_UMS_DISCONNECTED);
			intentFilter.addDataScheme("file");
			registerReceiver(sdr, intentFilter);
		}
		if (screenReceiver == null) {
			screenReceiver = new ScreenReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Intent.ACTION_SCREEN_OFF);
			filter.addAction(Intent.ACTION_SCREEN_ON);
			registerReceiver(screenReceiver, filter);
		}
	}
	
	
	
	

	private void unregistReceiver() {
		if (screenReceiver != null) {
			unregisterReceiver(screenReceiver);
			screenReceiver = null;
		}
	}

	private void initConversations() {
		MsgStat.filterTXList(this.getContentResolver());
	}
	
//	/**设置默认分辨率值，因为有可能需要上传图片，需要这个值*/
//	private void setDefaultDisplay() {
//		DisplayMetrics dm = new DisplayMetrics();
//		dm = getResources().getDisplayMetrics();
//		Utils.SreenW = dm.widthPixels;
//		Utils.SreenH = dm.heightPixels;
//	}

	/**获取服务是否开始的标记*/
	public static boolean isOnCreated() {
		return isOnCreated;
	}
	
}
