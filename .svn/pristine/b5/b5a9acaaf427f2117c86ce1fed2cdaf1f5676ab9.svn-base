package com.tuixin11sms.tx.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.LoginMode;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.LoginActivity;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.core.RC4R;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class SocketChannelConnectionImpl {
	private static final String TAG = SocketChannelConnectionImpl.class
			.getSimpleName();
	private final boolean IS_PRINT_LOG = false;// 控制是否显示调用sendToServer的log，因为是while死循环，调用这个方法的log太多。
	public final Object mServiceNotifier = new Object();
	/**
	 * 应用上下文
	 */
	private Context context;

	/**
	 * ping字符串
	 */
	private String ping;
	/**
	 * 代表登录的类
	 */
	private SessionManager mSess;
	/**
	 * 待发送的消息列表
	 */
	private List<String> msgList;

	/**
	 * 标记线程是否需要继续运行
	 */
	private volatile boolean running;
	/**
	 * 用于wait的锁
	 */
	private Object lock;

	/**
	 * 网络连接改变的广播
	 */
	private ConnectionChangeReceiver ccr;

	private final class ConnectionChangeReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Utils.debug) {
				Log.d(TAG, "ConnectionChangeReceiver, onReceive. running = "
						+ running);
			}
			if (running && Utils.checkNetworkAvailable(context)) {
				if (Utils.debug) {
					Log.d(TAG, "ConnectionChangeReceiver, send ping");
				}
				sendMessage(ping);
			}
		}
	}

	/**
	 * 构造函数
	 * 
	 * @throws IOException
	 */
	public SocketChannelConnectionImpl(String url, Context context) {
		if (Utils.debug)
			Log.i(TAG, "SocketChannelConnection construct");
		this.context = context;

		mSess = SessionManager.getInstance();

		msgList = new ArrayList<String>();
		lock = new Object();

		// 如果程序异常退出, 则running设为true. 否则设为false.
		// if (CommonData.USER_EXIT.equals(prefs.getString(CommonData.EXIT,
		// CommonData.USER_EXIT))) {
		if (PrefsMeme.USER_EXIT.equals(mSess.mPrefMeme.exit.getVal())) {
			running = false;
		} else {
			running = true;
		}
		// editor.putString(CommonData.EXIT, CommonData.EXCPTION_EXIT);
		// editor.commit();
		mSess.mPrefMeme.exit.setVal(PrefsMeme.EXCPTION_EXIT).commit();

		ping = "{\"mt\":49}";

		// 注册广播
		ccr = new ConnectionChangeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
		context.registerReceiver(ccr, filter);

		if (Utils.debug)
			Log.i(TAG,
					"SocketChannelConnectionImpl构造函数中，开始执行SocketThread这个runnable对象");
		Utils.executorService.submit(new SocketThread(url));
	}

	/**
	 * 发送消息
	 */
	public void sendMessage(String msgInfo) {
		addMsgToList(msgInfo);
		synchronized (lock) {
			running = true;
			lock.notify();
		}
	}

	/**
	 * 暂停线程的运行
	 */
	public void recovery() {
		mSess.setLoginSuccessed(false);
		synchronized (msgList) {
			msgList.clear();
		}
		synchronized (lock) {
			running = false;
			lock.notify();
		}
	}

	/**
	 * 向待发送列表中添加元素
	 * 
	 * @param msgInfo
	 *            待添加的元素
	 */
	private void addMsgToList(String msgInfo) {
		synchronized (msgList) {
			msgList.add(msgInfo);
			if (Utils.debug)
				Log.i(TAG, "添加待发送消息到list,待发送消息msgInfo:" + msgInfo + ",msgList:"
						+ msgList.toString());
		}
	}

	private final class SocketThread implements Runnable {
		/**
		 * 通道
		 */
		private SocketChannel channel;

		/**
		 * server地址
		 */
		private InetSocketAddress server;
		private String destHost;
		private int destPort;
		/**
		 * 缓冲区大小
		 */
		private static final int BLOCK = 1024 * 10;
		/**
		 * 发送缓冲区
		 */
		private ByteBuffer sendBuffer;
		/**
		 * 接收缓冲区
		 */
		private ByteBuffer receiveBuffer;
		/**
		 * 存储接收到的数据
		 */
		private ByteArrayOutputStream byteOutStream;

		/**
		 * socket建立时的网络类型
		 */
		private int preNetType;
		/**
		 * 标记是否需要进行线程等待
		 */
		private boolean needWait;
		/**
		 * 每次wait的时间
		 */
		private int waitTime = 100;
		/**
		 * 最后发送时间, -1表示无效
		 */
		private long lastSendTime = -1;

		/**
		 * 发送密钥
		 */
		public RC4R sendRC4;
		/**
		 * 接收密钥
		 */
		public RC4R receiveRC4;

		/**
		 * 从发送到接收的最大间隔时间
		 */
		private static final long TIME_OUT = 20 * 1000;

		/**
		 * 缓存发送出去的正式消息
		 */
		private Map<Integer, String> sentMsgCashe;

		/**
		 * 标记连接建立之后是否发送过数据给server
		 */
		private boolean onSentData = false;

		/**
		 * 表示初始化状态
		 */
		private int initState;
		/**
		 * 等待key交换
		 */
		private final int WAIT_KEY_CHARGE = 0;
		/**
		 * 已发送key交换
		 */
		private final int SENDED_KEY_CHARGE = 1;
		/**
		 * 等待登陆消息
		 */
		private final int WAIT_LOGIN_MSG = 2;
		/**
		 * 已发送登录消息
		 */
		private final int SENDED_LOGIN_MSG = 3;
		/**
		 * 等待普通消息
		 */
		private final int WAIT_NORMAL_MSG = 4;

		/**
		 * pong时间
		 */
		private long pong;

		/**
		 * 标记是否等待NIO select connectable
		 */
		private boolean isWaitingConnectable = false;

		public SocketThread(String url) {
			sendBuffer = ByteBuffer.allocateDirect(BLOCK);
			receiveBuffer = ByteBuffer.allocateDirect(BLOCK);
			byteOutStream = new ByteArrayOutputStream();

			preNetType = Utils.getNetworkType(context);
			sentMsgCashe = new HashMap<Integer, String>();

			initState = WAIT_KEY_CHARGE;
			Executors.newScheduledThreadPool(1).scheduleAtFixedRate(
					new Runnable() {
						@Override
						public void run() {
							// 只在需要的时候发心跳(如果initState ==
							// WAIT_NORMAL_MSG，每隔四分钟发一次心跳)
							if (initState == WAIT_NORMAL_MSG)
								sendMessage(ping);
						}
					}, 0, 4 * 60 * 1000, TimeUnit.MILLISECONDS);
			pong = System.currentTimeMillis();

			// 设置server的host和port
			setServerAddress(url);
		}

		/**
		 * 出现登录错误时调用 手动输入密码登陆时
		 */
		private void dealLoginErrorHappened() {
			if (Utils.debug)
				Log.e(TAG, "dealLoginErrorHappened");
			recoveryAll();
			ActivityManager am = (ActivityManager) context
					.getSystemService(Service.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			if (cn.getClassName().indexOf("com.tuixin11sms.tx") != -1
//					&& cn.getClassName().indexOf(
//							"com.tuixin11sms.tx.RegisterOrLoginActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.RegistActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.LoginActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.IndexActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.TutorialTeachActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.LostPasswordActivity") == -1) {
				TxData.finishAll();
				Intent intent = new Intent(context, LoginActivity.class);
				// intent.putExtra("AutoLoginFailure", true);
				intent.putExtra(Utils.AUTOLOGINFAILURE, Utils.DISLOGINFAILURE);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				if (Utils.debug) {
					Log.i(TAG, "跳转到登陆页面");
				}
			}
		}

		// 自动登陆出错是的处理
		private void dealLoginErrorIdHappened() {
			if (Utils.debug)
				Log.e(TAG, "dealLoginErrorHappened");
			recoveryAll();
			ActivityManager am = (ActivityManager) context
					.getSystemService(Service.ACTIVITY_SERVICE);
			ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
			if (cn.getClassName().indexOf("com.tuixin11sms.tx") != -1
//					&& cn.getClassName().indexOf(
//							"com.tuixin11sms.tx.RegisterOrLoginActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.RegistActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.LoginActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.IndexActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.TutorialTeachActivity") == -1
					&& cn.getClassName().indexOf(
							"com.tuixin11sms.tx.LostPasswordActivity") == -1) {
				TxData.finishAll();
				Intent intent = new Intent(context, LoginActivity.class);
				// intent.putExtra("AutoLoginFailure", false);
				intent.putExtra(Utils.AUTOLOGINFAILURE, Utils.FIDLOGINFAILURE);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(intent);
				if (Utils.debug) {
					Log.i(TAG, "跳转到登陆页面");
				}
			}
		}

		/**
		 * 恢复所有状态
		 */
		private void recoveryAll() {
			if (Utils.debug) {
				Log.e(TAG, "recoveryAll()");
			}
			clearCurrentConnection();
			recovery();
			// editor.putString(CommonData.DOOR, "close").commit();
			mSess.mPrefMeme.door.setVal("close").commit();
			mSess.setAutoLoginInfor("", "");
		}

		/**
		 * 产生key交换信息
		 */
		private String getKeyMsg() {
			// 初始化密钥
			sendRC4 = new RC4R("MEMESERVERV1.0.0".getBytes());
			byte[] receiveKey = Utils.buildKey();
			receiveRC4 = new RC4R(receiveKey);
			String bcd = Utils.byte2string(receiveKey);

			String imei = Utils.getImei_Iccid(context);
			// 系统版本
			String sysVersion = android.os.Build.VERSION.RELEASE;
			// 设备名称
			String mobileModel = android.os.Build.MODEL;
			int lang = Utils.getLang();
			// editor.putInt(CommonData.LANGID, lang);
			// editor.commit();
			mSess.mPrefMeme.langid.setVal(lang).commit();
			String keyMsg = null;

			try {

				if (imei != null) {
					keyMsg = new JSONStringer().object().key("mt")
							.value(MsgInfor.CLENT_KEY_MSG).key("k").value(bcd)
							.key("app").object().key("pid").value(2).key("tid")
							.value(2).key("ver").value(Utils.tuixinver)
							.key("cid").value(Utils.getCid()).key("lang")
							.value(lang).key("osid").value(Constants.cli)
							.endObject().key("info").object().key("h")
							.value(imei).key("c").value(mobileModel).key("t")
							.value("").key("o").value("Android").key("v")
							.value(sysVersion).endObject().endObject()
							.toString();
				} else {
					keyMsg = new JSONStringer().object().key("mt")
							.value(MsgInfor.CLENT_KEY_MSG).key("k").value(bcd)
							.key("app").object().key("pid").value(2).key("tid")
							.value(2).key("ver").value(Utils.tuixinver)
							.key("cid").value(Utils.getCid()).key("lang")
							.value(lang).key("osid").value(Constants.cli)
							.endObject().key("info").object().key("h")
							.value("").key("c").value(mobileModel).key("t")
							.value("").key("o").value("Android").key("v")
							.value(sysVersion).endObject().endObject()
							.toString();
				}

				if (Utils.debug)
					Log.i(TAG, "#####" + keyMsg.toString());
			} catch (JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
			return keyMsg;
		}

		/** 接收来自server的消息 */
		private void receiveFromServer() throws IOException {
			JSonTrackerUTF8 tracker = new JSonTrackerUTF8();
			int len = 0;
			receiveBuffer.clear();
			// 读取channel中的数据到缓冲区中, 直到数据读完, 或者缓冲区填满.
			if (channel.read(receiveBuffer) > 0) {
				// 进入循环说明有数据可读, 不需要线程等待
				needWait = false;
				receiveBuffer.flip();
				byte[] b = new byte[receiveBuffer.limit()];
				len += receiveBuffer.limit(); 
				// 将缓冲区的数据读取到数组中
				receiveBuffer.get(b);
				// 解密
				byte[] rb = receiveRC4.decrypt(b);
				// 将解密后的数据写入流中
				byteOutStream.write(rb);
			}

			// len为0说明没有读取到数据
			if (len <= 0) {
				return;
			}

			receiveBuffer.compact();
			len = byteOutStream.size();
			while (len > 0) {
				byte[] tmp = byteOutStream.toByteArray();
				byteOutStream.reset();
				tracker.reset();
				int itok = tracker.track(tmp, 0);
				if (itok > 0) {
					String message = new String(tmp, 0, itok, "UTF-8");
					if (Utils.debug)
						Log.i(TAG, "接收到服务器信息receiveFromServer，发送信息广播");
					broadcastReceiveMsg(message);
					byteOutStream.reset();
					len = tmp.length - itok;
					if (len > 0) {
						byteOutStream.write(tmp, itok, len);
						Thread.yield();
					} else
						break;
				} else {
					if (itok == 0 && byteOutStream.size() < 0x80000)
						byteOutStream.write(tmp);
					else {
						byteOutStream.reset();
						clearCurrentConnection();
					}
					break;
				}
			}
		}

		/**
		 * 与server通信
		 * 
		 * @throws Exception
		 */
		private void communicateToServer() throws Exception {
			// 首先假设需要进行线程等待
			needWait = true;
			if(isWaitingConnectable){
				if (channel.isConnectionPending()){
					try {
						//等待，否则在连接期间CPU会占用过高
						Thread.sleep(100);
					} catch (Exception e) {
						if(Utils.debug){
							e.printStackTrace();
						}
					}
					if(!channel.finishConnect())
						return;
				}
				if(!channel.isConnected())
					throw new Exception("Fail to cnnect.");

				dealWhenConnected();
				lastSendTime = -1;		
				isWaitingConnectable = false;
			}
			
			sendToServer();
			receiveFromServer();

			if (needWait) {
				synchronized (lock) {
					lock.wait(waitTime);
				}
			}
		}

		/**
		 * 连接建立之后进行一些必要的处理
		 */
		private void dealWhenConnected() {
			needWait = false;
			onSentData = false;
			byteOutStream.reset();
			// 将缓存中的正式消息重新添加到msglist中
			synchronized (msgList) {
				for (Integer msgid : sentMsgCashe.keySet()) {
					msgList.add(sentMsgCashe.get(msgid));
					if (Utils.debug) {
						Log.e(TAG, "add msg to msgList from sentMsgCashe: "
								+ sentMsgCashe.get(msgid));
						Log.i(TAG, "待发送消息msgList:" + msgList.toString());
					}
				}
				sentMsgCashe.clear();
			}
		}

		/**
		 * 清除当前连接
		 */
		private void clearCurrentConnection() {
			if (Utils.debug)
				Log.e(TAG, "clearCurrentConnection()");

			lastSendTime = -1;
			mSess.setLoginSuccessed(false);
			initState = WAIT_KEY_CHARGE;
			isWaitingConnectable = false;

			if (channel == null) {
				return;
			}

			try {
				if (channel != null) {
					channel.socket().close();
					channel.close();
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG,
							"exception occurs in clearCurrentConnection():"
									+ e);
				}
			} finally {
				channel = null;
			}
		}

		/** 发送消息给server */
		private void sendToServer() throws Exception {
			String msg;
			if (initState == WAIT_KEY_CHARGE) {
				// key交换没有进行时, 只能发送45号协议
				if (onSentData) {
					throw new Exception(
							"The key msg must be the first msg after generating connection");
				}
				msg = getKeyMsg();
				if (Utils.debug) {
					Log.e(TAG, "key交换的信息msg:" + msg);
				}
			} else if (initState == WAIT_LOGIN_MSG) {
				// 发送用户登录(2号协议)或注册消息(0号协议)或26号找回密码协议
				msg = mSess.getLoginMsg();
				if (Utils.debug) {
					Log.e(TAG, "登陆信息msg:" + msg);
				}
				if (Utils.isNull(msg)) {
					dealLoginErrorHappened();
					throw new Exception(
							"the login msg can't be null, so jump to register ui");
				}
			} else if (initState == WAIT_NORMAL_MSG) {
				// 发送普通消息
				if (!mSess.isLoginSuccessed()) {
					throw new Exception(
							"Something crazy happened. WAIT_NORMAL_MSG conflicts with login successed");
				}
				synchronized (msgList) {
					if (msgList.size() == 0) {
						if (Utils.debug && IS_PRINT_LOG)
							Log.i(TAG, "消息列表中没有消息，不发送");
						return;
					} else {
						msg = msgList.remove(0);
					}
				}
			} else {
				// 其他情形直接返回
				if (Utils.debug)
					Log.i(TAG, "其他情形，没有什么要发送给server，直接return");
				return;
			}

			// 代码运行到这里说明有数据需要发送, 因此不需要进行等待
			needWait = false;

			int type = Utils.getMessageType(msg);
			byte[] data = sendRC4.encrypt(msg);
			int len = data.length;

			// 将加密后的数据发送给server
			int off = 0;
			try {
				while (off<len) {
					sendBuffer.clear();
					int sendlen=Math.min(len - off,sendBuffer.capacity());
					sendBuffer.put(data, off, sendlen);
					sendBuffer.flip();
					int wrlen=channel.write(sendBuffer);
					off += wrlen;
					if(wrlen<sendlen)
						Thread.sleep(100);
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "exception ocurrs when sending, msg = " + msg);
				}
				// 如果发生了异常, 将msgInfo添加回列表中, 并抛出该异常
				if (type != MsgInfor.CLENT_REGSTER
						&& type != MsgInfor.CLENT_LOGIN
						&& type != MsgInfor.CLENT_PASSWORDGET
						&& type != MsgInfor.CLENT_KEY_MSG) {
					synchronized (msgList) {
						msgList.add(0, msg);
						if (Utils.debug)
							Log.i(TAG, "给服务器发数据时异常，待发送数据msg:" + msg
									+ ",msgList:" + msgList.toString());
					}
				}
				throw e;
			}

			// 消息发送出去之后更新状态
			if (type == MsgInfor.CLENT_UPMSG
					|| type == MsgInfor.CLENT_MSG_UP_QUN) {
				sentMsgCashe.put(new JSONObject(msg).getJSONObject("sm")
						.getInt("id"), msg);
			} else if (type == MsgInfor.CLENT_KEY_MSG) {
				initState = SENDED_KEY_CHARGE;
			} else if (type == MsgInfor.CLENT_REGSTER
					|| type == MsgInfor.CLENT_LOGIN
					|| type == MsgInfor.CLENT_PASSWORDGET) {
				initState = SENDED_LOGIN_MSG;
			}

			// 9, 28, 2015, 2016号协议消息不需要server返回确认
			if (type != MsgInfor.CLENT_DOWNMSG
					&& type != MsgInfor.CLENT_MSGREAD
					&& type != MsgInfor.CLENT_MSG_DOWN_QUN
					&& type != MsgInfor.CLENT_GET_OFFLINERECEIPTE_MEG_QUN) {
				lastSendTime = System.currentTimeMillis();
			}
			onSentData = true;

			if (Utils.debug)
				Log.d(TAG, "sendToServer() method: " + msg);
		}

		/**
		 * 建立和server的连接
		 * 
		 * @throws Exception
		 */
		private void generateConnection() throws Exception {
			if (Utils.debug) {
				Log.e(TAG, "generateConnection()");
			}
			pong = System.currentTimeMillis();
			clearCurrentConnection();
			isWaitingConnectable = true;
			if (server == null || server.isUnresolved()) {
				server = new InetSocketAddress(destHost, destPort);
			}
			channel = SocketChannel.open();			
			Socket socket=channel.socket();
			if(Utils.debug)
				Log.i(TAG,"sendbuf:"+socket.getSendBufferSize()+"recvbuf:"+socket.getReceiveBufferSize());

			//控制发送缓冲区大小，防止过多请求数据堆积在系统缓冲区中，效果待观察
			socket.setSendBufferSize(0x400);

			//在一定程度上防止自己踢自己
			socket.setSoLinger(true, 10);

			channel.configureBlocking(false);
			channel.connect(server);
			preNetType = Utils.getNetworkTypeSimple();
			// 建立连接也加入20s超时判定
			lastSendTime = System.currentTimeMillis();
		}

		/**
		 * 判断连接状态
		 */
		private boolean isConnected() {
			if (channel == null) {
				return false;
			}
			if (channel.isConnected() && !channel.socket().isClosed()) {
				return true;
			}
			return false;
		}

		/**
		 * 获得server地址
		 */
		private void setServerAddress(String url) {
			if (Utils.debug)
				Log.i(TAG, "getServerAddress(String url)");
			String socketPrefix = "socket://";
			if (url == null || !url.startsWith(socketPrefix)
					|| url.equals(socketPrefix)) {
				throw new IllegalArgumentException("invalid URL");
			}
			// 去掉前缀部分
			url = url.substring(socketPrefix.length());
			int splitIndex = url.indexOf(":");
			if (splitIndex == -1 || !(url.length() > splitIndex + 2)) {
				throw new IllegalArgumentException("Invalid URL format");
			}
			destHost = url.substring(0, splitIndex);
			destPort = Integer.parseInt(url.substring(splitIndex + 1));
		}

		/**
		 * 接收到server发送的消息时广播
		 */
		private void broadcastReceiveMsg(String msg) {
			if (Utils.debug)
				Log.i(TAG, "broadcastReceiveMsg(String msg): " + msg);

			// 接收到数据后将最后发送时间设置为无效
			lastSendTime = -1;

			// 预处理7号和46号协议消息
			int type = Utils.getMessageType(msg);
			if (type != MsgInfor.SERVER_PING) {
				if (waitTime > 100) {
					Log.e(TAG, "change wait time back to 100ms");
					waitTime = 100;
				}
				if (TxData.time > 0) {
					TxData.time = System.currentTimeMillis();
				}
			}
			if (type != MsgInfor.SERVER_KEY_MSG
					&& type != MsgInfor.SERVER_REGSTER
					&& type != MsgInfor.SERVER_LOGIN
					&& type != MsgInfor.SERVER_PASSWORDGET) {
				if (Utils.debug)
					Log.i(TAG, "设置LoginSessionManager登陆成功标记？");
				mSess.setLoginSuccessed(true);
			}

			if (type == MsgInfor.SERVER_KEY_MSG) {
				if (Utils.debug)
					Log.i(TAG, "预处理key交换返回");
				preHandleKeyMsg(msg);
			} else if (type == MsgInfor.SERVER_REGSTER) {
				if (Utils.debug)
					Log.i(TAG, "预处理注册成功？");
				preHandleRegister(msg);
			} else if (type == MsgInfor.SERVER_LOGIN) {
				if (Utils.debug)
					Log.i(TAG, "收到登陆返回，开始预处理服务器登陆成功的响应。");
				// 如果服务没有开启，则等待200毫秒再去判断，直到开启为止 2013.10.09 shc 
				int waitServiceTimes = 0;// 等待服务的次数，如果等待了5次还没有开启，则它自己启服务
				while (true) {
					if (!TuixinService1.isOnCreated()) {
						synchronized (mServiceNotifier) {
							try {
								if (waitServiceTimes == 5) {
									// 开启TuixinService服务，因为程序可能会崩溃但是不闪退，开启一个应用内的activity，这时没有人触发开启service
									context.startService(new Intent(context,
											TuixinService1.class));
									if (Utils.debug)
										Log.e(TAG,
												"等待了5次服务没有起来，SocketChannelConnectionImpl自己启服务");
									waitServiceTimes = 0;
								}
								mServiceNotifier.wait(200);
								waitServiceTimes++;
								if (Utils.debug) {
									Log.e(TAG, "服务还没有开启，等待了200毫秒");
								}
							} catch (InterruptedException e) {
								if (Utils.debug) {
									Log.e(TAG, "等待服务开启锁被打断异常", e);
								}
							}
						}
					} else {
						break;
					}
				}
				preHandleLogin(msg);
			} else if (type == MsgInfor.SERVER_PASSWORDGET) {
				if (Utils.debug)
					Log.i(TAG, "预处理找回密码返回。");
				preHandleFindPwd(msg);
			} else if (type == MsgInfor.SERVER_PING) {
				if (Utils.debug)
					Log.i(TAG, "收到服务器心跳返回，重新设置当前心跳时间");
				pong = System.currentTimeMillis();
			} else if (type == MsgInfor.SERVER_UPMSG
					|| type == MsgInfor.SERVER_MSG_UP_QUN) {
				if (Utils.debug)
					Log.i(TAG, "预处理正式消息返回");
				preHandleNormalMsg(msg);
			}
			if (Utils.debug) {
				if (type == MsgInfor.SERVER_LOGIN)
					if (Utils.debug)
						Log.e(TAG, "预处理完之后，就开始发广播消息，通知service处理msg消息");
			}
			Intent intent = new Intent(Constants.INTENT_ACTION_RECEIVE_MSG);
			intent.putExtra("msg", msg);
			context.sendOrderedBroadcast(intent, null);
		}

		/**
		 * 预处理找回密码返回
		 */
		private void preHandleFindPwd(String msg) {
			recoveryAll();
		}

		/**
		 * 预处理登录返回
		 */
		private void preHandleLogin(String msg) {
			try {
				JSONObject jo = new JSONObject(msg);
				int d = jo.getInt("d");
				if (d == 0) {
					String uid = jo.getInt("i") + "";
					String token = jo.getString("tk");
					if (mSess.getLoginMode() == LoginMode.USER_LOGIN) {
						// String password = prefs.getString(
						// CommonData.INPUTPASSWORD, "");
						// String password =
						// session.getPwd(Long.parseLong(uid));
						mSess.setLoginSuccessInfo(uid);
					} else if (mSess.getLoginMode() == LoginMode.OTHER_ACCOUNT_LOGIN) {
						mSess.setWeiboAuto(true);
					}
					mSess.setLoginSuccessed(true);
					mSess.setUidAndToken(uid, token);
					
					//和服务器同步时间
					long st = jo.getLong("st");
					st = st - System.nanoTime()/(1000*1000*1000);
					mSess.mPrefMeme.st.setVal(st);
					
					initState = WAIT_NORMAL_MSG;
					// 发送登录成功的广播
					Intent intent = new Intent(
							Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED);
					context.sendBroadcast(intent);
					if (Utils.debug) {
						Log.e(TAG, "发送登陆成功的广播，什么情况啊？？？？");
					}
				} else if (d == 5) {
					mSess.setLoginSuccessed(false);
					if (mSess.getLoginMode() == LoginMode.AUTO_LOGIN
							|| (mSess.isWeiboAuto() && mSess.getLoginMode() == LoginMode.OTHER_ACCOUNT_LOGIN)) {
						dealLoginErrorIdHappened();
					} else {
						recoveryAll();
					}

				} else {
					//d:uint,状态,0成功,1帐号不存在,2密码错误,3操作失败,4昵称无效,5帐号被封
					
					mSess.setLoginSuccessed(false);
					if (mSess.getLoginMode() == LoginMode.AUTO_LOGIN
							|| (mSess.isWeiboAuto() && mSess.getLoginMode() == LoginMode.OTHER_ACCOUNT_LOGIN)) {
						dealLoginErrorHappened();
					} else {
						recoveryAll();
					}
				}
			} catch (org.json.JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
		}

		/**
		 * 预处理注册返回
		 */
		private void preHandleRegister(String msg) {
			try {
				JSONObject jo = new JSONObject(msg);
				int d = jo.getInt("d");
				if (d == 0) {
					String uid = jo.getInt("i") + "";
					String password = jo.getString("pwd");
					String token = jo.getString("tk");
					mSess.setAutoLoginInfor(uid, password);
					mSess.setLoginSuccessed(true);
					// session.setToken(token); 
					mSess.setUidAndToken(uid, token);
					
					//和服务器同步时间
					long st = jo.getLong("st");
					if(Utils.debug)Log.i(TAG, "登陆成返回的服务器时间是："+st);
					st = st - System.nanoTime()/(1000*1000*1000);
					mSess.mPrefMeme.st.setVal(st);
					
					initState = WAIT_NORMAL_MSG;
					// 发送登录成功的广播
					Intent intent = new Intent(
							Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED);
					context.sendBroadcast(intent);
				}else {
					mSess.setLoginSuccessed(false);
					recoveryAll();
				}
			} catch (org.json.JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
		}

		/**
		 * 预处理正式消息的返回
		 */
		private void preHandleNormalMsg(String msg) {
			try {
				sentMsgCashe.remove(new JSONObject(msg).getInt("id"));
			} catch (org.json.JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
		}

		/**
		 * 预处理key交换返回
		 */
		private void preHandleKeyMsg(String msg) {
			try {
				JSONObject jo = new JSONObject(msg);
				int d = jo.getInt("d");
				if (d == 0) {
					String key = jo.getString("k");
					sendRC4.InitKey(Utils.string2byte(key));
					initState = WAIT_LOGIN_MSG;
				} else {
					recoveryAll();
				}
			} catch (org.json.JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
		}

		@Override
		public void run() {
			Thread.currentThread().setName("SocketChannelConnectionImpl");
			while (true) {
				try {
					// 如果running被置为false或者网络连接不可用时, 清理连接后wait
					synchronized (lock) {
						while (!running || !Utils.checkNetworkAvailableSimple()) {
							if (Utils.debug) {
								Log.e(TAG,
										"running is set to be false or network is not available");
							}
							clearCurrentConnection();
							lock.wait(25 * 1000);
						}
					}

					long currentTime = System.currentTimeMillis();
					if (TxData.time == 0) {
						if (waitTime > 100) {
							if (Utils.debug)
								Log.e(TAG, "change wait time back to 100ms");
							waitTime = 100;
						}
					} else if (currentTime - TxData.time >= 5 * 60 * 1000
							&& waitTime == 100) {
						if (Utils.debug)
							Log.e(TAG, "change wait time to 5s");
						waitTime = 5000;
					}
					// 如果上次发送时间有效且大于最大间隔时间
					if (lastSendTime != -1
							&& currentTime - lastSendTime > TIME_OUT) {
						if (Utils.debug) {
							Log.e(TAG, "上次发送时间有效且大于最大间隔时间");
						}
						clearCurrentConnection();
					}
					// 如果pong包超时
					if (currentTime - pong > 5 * 60 * 1000) {
						if (Utils.debug) {
							Log.e(TAG, "pong包超时");
						}
						clearCurrentConnection();
					}
					// 如果未处于连接状态, 先建立连接
					if (!isConnected() && !isWaitingConnectable) {
						if (Utils.debug)
							Log.i(TAG, "未建立连接，先和server建立连接");
						generateConnection();
					}
					// 处理网络切换到WIFI后连接没有中断的情况
					if (preNetType != Utils.NET_WIFI
							&& Utils.getNetworkTypeSimple() == Utils.NET_WIFI) {
						throw new IOException("it's time to change network");
					}

					if (Utils.debug && IS_PRINT_LOG)
						Log.i(TAG, "与server通讯");
					communicateToServer();

				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "socketThread异常", e);
					// 发生异常时清除连接
					clearCurrentConnection();
					synchronized (lock) {
						try {
							lock.wait(1000);
						} catch (InterruptedException e1) {
							if (Utils.debug)
								Log.e(TAG, "lock锁被打断异常", e);
						}
					}
				}
			}
		}
	}
}