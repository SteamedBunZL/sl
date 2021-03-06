package com.tuixin11sms.tx.net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.core.RC4R;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class LBSSocketChannelConnectionImpl {
	private static final String TAG = LBSSocketChannelConnectionImpl.class.getSimpleName();

	/**
	 * 上下文有关的对象
	 */
	private Context context;
	private SharedPreferences prefs;
	private SharedPreferences.Editor editor;

	/**
	 * 用于同步的锁
	 */
	private Object lock;
	/**
	 * 标记线程是否需要继续运行
	 */
	private volatile boolean running;
	/**
	 * 待发送的普通消息列表
	 */
	private List<String> msgList;
	/**
	 * 登录成功广播
	 */
	private LoginSuccessedReceiver lsr;
	/**
	 * ping包json串
	 */
	private String ping;
	
	
	private  SessionManager mSess = null;
	
	

	/**
	 * 构造函数
	 */
	public LBSSocketChannelConnectionImpl(String url, Context context) {
		if (Utils.debug)Log.i(TAG, "LBSSocketChannelConnectionImpl construct");
		this.context = context;
		prefs = this.context.getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		editor = prefs.edit();

		mSess = SessionManager.getInstance();
		
		
		lock = new Object();
		running = true;
		msgList = new ArrayList<String>();
		
		try {
			ping = new JSONStringer().object().key("mt").value(MsgInfor.CLIENT_PING).endObject()
			.toString();
		} catch (JSONException e) {
			if(Utils.debug)e.printStackTrace();
		}

		// 注册广播
		lsr = new LoginSuccessedReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED);
		context.registerReceiver(lsr, filter);
		//3.5版本开始 舍弃掉这个
		//Utils.executorService.submit(new LBSSocketThread(url));
	}

	/**
	 * 发送消息
	 */
	public void sendMessage(String msg) {
		addMsgToList(msg);
		synchronized (lock) {
			running = true;
			lock.notify();
		}
	}

	/**
	 * 将消息添加到消息列表中
	 */
	private void addMsgToList(String msg) {
		synchronized (msgList) {
			msgList.add(msg);
		}
	}
	
	private final class LBSSocketThread implements Runnable {
		/**
		 * 随机数
		 */
		private Random rand;
		/**
		 * 负载均衡server地址
		 */
		private String url;
		/**
		 * server的host
		 */
		private String destHost;
		/**
		 * server的port
		 */
		private int destPort = -1;
		
		/**
		 * 选择器
		 */
		private Selector selector;
		/**
		 * 选择器的select时间
		 */
		private static final int SELECT_TIME = 60 * 1000;
		/**
		 * 通道
		 */
		private SocketChannel channel;
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
		 * socket建立时的网络类型
		 */
		private int preNetType;

		/**
		 * 表示初始化步骤
		 * WAIT_SHAKE_HAND表示需要发送握手请求
		 * WAIT_KEY_CHARGE表示已收到server的握手应答, 需要发送key交换
		 * WAIT_ACCESS_AUTH表示已收到server的key交换返回, 需要发送访问授权
		 * WAIT_NORMAL_MSG表示已收到server认证结果, 可以发送其他消息
		 */
		private int initState;
		private final int WAIT_SHAKE_HAND = 0;
		private final int SENDED_SHAKE_HAND = 1;
		private final int WAIT_KEY_CHARGE = 2;
		private final int SENDED_KEY_CHARGE = 3;
		private final int WAIT_ACCESS_AUTH = 4;
		private final int SENDED_ACCESS_AUTH = 5;
		private final int WAIT_NORMAL_MSG = 6;
		/**
		 * 标记是否需要进行线程等待
		 */
		private boolean needWait;
		/**
		 * 每次wait的时间
		 */
		private int WAIT_TIME = 100;

		/**
		 * 缓存接收的数据的流
		 */
		private ByteArrayOutputStream byteOutStream;
		/**
		 * 用于字符编码的转换
		 */
		private JSonTrackerUTF8 tracker;

		/**
		 * 接收密钥
		 */
		private RC4R receiveRC4;
		/**
		 * 发送密钥
		 */
		private RC4R sendRC4;
		
		/**
		 * 定时器线程池
		 */
		private ScheduledExecutorService ses;
		
		/**
		 * pong包计时器
		 */
		private long pong;

		/**
		 * 最后发送时间, -1表示无效
		 */
		private long lastSendTime = -1;
		/**
		 * 从发送到接收的最大间隔时间
		 */
		private static final long TIME_OUT = 20 * 1000;

		/**
		 * 缓存发送出去的正式消息
		 */
		private Map<Integer, String> sentMsgCashe;

		/**
		 * 代表登录的类
		 */
		private SessionManager session;
		
		/**
		 * 标记是否等待NIO select connectable
		 */
		private boolean isWaitingConnectable = false;
		
		
		public LBSSocketThread(String url) {
			this.url = url;
			this.rand = new Random();
			
			sendBuffer = ByteBuffer.allocateDirect(BLOCK);
			receiveBuffer = ByteBuffer.allocateDirect(BLOCK);
			preNetType = Utils.getNetworkType(context);

			initState = WAIT_SHAKE_HAND;
			byteOutStream = new ByteArrayOutputStream();
			tracker = new JSonTrackerUTF8();
			
			ses = Executors.newScheduledThreadPool(1);
			ses.scheduleAtFixedRate(new Runnable() {
				@Override
				public void run() {
					// 只在需要的时候发心跳
					if (initState == WAIT_NORMAL_MSG)
						sendMessage(ping);
				}
			}, 0, 4 * 60 * 1000, TimeUnit.MILLISECONDS);
			pong = System.currentTimeMillis();

			sentMsgCashe = new HashMap<Integer, String>();

			session = SessionManager.getInstance();
		}
		
		/**
		 * 与server通信
		 * @throws Exception 
		 */
		private void communicateToServer() throws Exception {
			// 首先假设需要进行线程等待
			needWait = true;

			if (selector.select(SELECT_TIME) > 0) {
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) {
					SelectionKey key = it.next();
					it.remove();
					if (key.isConnectable()) {
						channel = (SocketChannel) key.channel();
						if (channel.isConnectionPending()) {
							channel.finishConnect();
							dealWhenConnected();
							isWaitingConnectable = false;
							lastSendTime = -1;
						}
						// connectable的key不应该用于read或write
						return;
					}
					if (key.isWritable()) {
						channel = (SocketChannel) key.channel();
						sendToServer();
					}
					if (key.isReadable()) {
						channel = (SocketChannel) key.channel();
						receiveFromServer();
					}
				}
			}

			if (needWait) {
				synchronized (lock) {
					lock.wait(WAIT_TIME);
				}
			}
		}

		/**
		 * 连接建立之后所需执行的操作
		 */
		private void dealWhenConnected() {
			needWait = false;
			byteOutStream.reset();

			synchronized (msgList) {
				Map<Integer, String> map = LBSSessionManager.getLBSSessionManager()
						.getJoinedChannelMap();
				int index = 0;
				// 重新加入之前未退出的频道, 注意顺序的问题
				for (int key : map.keySet()) {
					String msgInfo = map.get(key);
					if (!msgList.contains(msgInfo) && key != LBSSocketHelper.VIRTUAL_CHANNEL_ID) {
						msgList.add(index++, msgInfo);
					}
				}
				// 将缓存中的正式消息重新添加到msglist中
				for (Integer msgid : sentMsgCashe.keySet()) {
					msgList.add(sentMsgCashe.get(msgid));
					if (Utils.debug) {
						Log.e(TAG, "add msg to msgList from sentMsgCashe: " + sentMsgCashe.get(msgid));
					}
				}
				sentMsgCashe.clear();
			}

			// 1分钟以后如果没有加入的频道, 就清除连接并恢复至初始状态
			ses.schedule(new Runnable() {
				@Override
				public void run() {
					if (LBSSessionManager.getLBSSessionManager().isNoChannelIn()) {
						if (Utils.debug) {
							Log.e(TAG,
									"Haven't add any channel after generating connection, so recovery.");
						}
						recovery();
					}
				}
			}, 60, TimeUnit.SECONDS);
		}

		/**
		 * 发送消息给server
		 * @throws IOException 
		 */
		private void sendToServer() throws Exception {
			String msg = null;
			synchronized (msgList) {
				// 发送握手请求
				if (initState == WAIT_SHAKE_HAND) {
					msg = getShakeMsg();
				}
				// 发送key交换
				else if (initState == WAIT_KEY_CHARGE) {
					msg = getKeyMsg();
				}
				// 发送用户授权消息
				else if (initState == WAIT_ACCESS_AUTH) {
					msg = getAuthMsg();
				}
				// 发送普通消息
				else if (initState == WAIT_NORMAL_MSG) {
					if (msgList.size() == 0) {
						return;
					}
					msg = msgList.remove(0);
				}
				// 处于其他状态时, 不发送任何消息
				else {
					return;
				}
			}

			// 代码运行到这里说明有数据需要发送, 因此不需要进行等待
			needWait = false;

			// 加密
			byte[] data = sendRC4.encrypt(msg);
			int len = data.length;

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
				// 如果发生了异常, 将msgInfo添加回列表中, 并抛出该异常
				if (Utils.debug) {
					Log.e(TAG, "exception happens when sending msg: " + msg);
				}
				if (initState == WAIT_NORMAL_MSG) {
					synchronized (msgList) {
						msgList.add(0, msg);
					}
				}
				throw e;
			}

			// 更新initState
			int type = Utils.getMessageType(msg);
			if (type == MsgInfor.CLIENT_SHAKE_HAND) {
				initState = SENDED_SHAKE_HAND;
			} else if (type == MsgInfor.CLIENT_KEY_CHARGE) {
				initState = SENDED_KEY_CHARGE;
			} else if (type == MsgInfor.CLIENT_ACCESS_AUTH) {
				initState = SENDED_ACCESS_AUTH;
			} else if (type == MsgInfor.CLIENT_SEND_MSG) {
				// 如果是正式消息, 添加到缓存中
				sentMsgCashe.put(new JSONObject(msg).getInt("msgid"), msg);
			}

			// 更新最后发送时间, LBS上所有发送的消息都需要server的返回
			lastSendTime = System.currentTimeMillis();

			if (Utils.debug)
				Log.d(TAG, "sendToServer() method: " + msg);
		}

		/**
		 * 取得授权消息
		 * @throws JSONException 
		 */
		private String getAuthMsg() throws JSONException {
			String authMsg;
			authMsg = new JSONStringer().object().key("mt").value(MsgInfor.CLIENT_ACCESS_AUTH)
					.key("uid").value(TX.tm.getTxMe().partner_id).key("token")
					.value(TX.tm.getTxMe().token).endObject().toString();
			return authMsg;
		}

		/**
		 * 取得key交换消息
		 * @throws JSONException
		 */
		private String getKeyMsg() throws JSONException {
			String keyMsg;
			// 从接收server的key交换返回开始, 使用新的receiveKey解密
			byte[] receiveKey = Utils.buildKey();
			receiveRC4 = new RC4R(receiveKey);
			String bcd = Utils.byte2string(receiveKey);
			keyMsg = new JSONStringer().object().key("mt").value(MsgInfor.CLIENT_KEY_CHARGE).key("k")
					.value(bcd).endObject().toString();
			return keyMsg;
		}

		/**
		 * 取得握手请求消息
		 * @throws JSONException 
		 */
		private String getShakeMsg() throws JSONException {
			// 发送握手请求, 接收握手应答, 发送key交换用的都是初始key
			byte[] initKey = "MEMESERVERV1.0.0".getBytes();
			sendRC4 = new RC4R(initKey);
			receiveRC4 = new RC4R(initKey);

			// 获取相关字段的值
			String sysVersion = android.os.Build.VERSION.RELEASE;
			String mobileModel = android.os.Build.MODEL;
			String imei = Utils.getImei_Iccid(context);
			int lang = Utils.getLang();
//			editor.putInt(CommonData.LANGID, lang);
//			editor.commit();
			mSess.mPrefMeme.langid.setVal(lang).commit();
			String shakeMsg;
			if (imei != null) {
				shakeMsg = new JSONStringer().object().key("mt").value(MsgInfor.CLIENT_SHAKE_HAND)
						.key("app").object().key("pid").value(2).key("tid").value(2).key("cid")
						.value(Utils.getCid()).key("ver").value(Utils.tuixinver).key("lang")
						.value(lang).key("osid").value(Constants.cli).endObject().key("info").object()
						.key("h").value(imei).key("c").value(mobileModel).key("t").value("")
						.key("o").value("Android").key("v").value(sysVersion).endObject().endObject()
						.toString();
			} else {
				shakeMsg = new JSONStringer().object().key("mt").value(MsgInfor.CLIENT_SHAKE_HAND)
						.key("app").object().key("pid").value(2).key("tid").value(2).key("cid")
						.value(Utils.getCid()).key("ver").value(Utils.tuixinver).key("lang")
						.value(lang).key("osid").value(Constants.cli).endObject().key("info").object()
						.key("h").value("").key("c").value(mobileModel).key("t").value("").key("o")
						.value("Android").key("v").value(sysVersion).endObject().endObject().toString();
			}

			return shakeMsg;
		}

		/**
		 * 接收来自server的消息
		 * @throws IOException
		 */
		private void receiveFromServer() throws Exception {
			int len = 0;
			receiveBuffer.clear();
			// 读取channel中的数据到缓冲区中, 直到数据读完, 或者缓冲区填满.
			if (channel.read(receiveBuffer) > 0) {
				// 进入循环说明有数据可读, 不需要线程等待
				needWait = false;
				receiveBuffer.flip();
				byte[] b = new byte[receiveBuffer.limit()];
				len += receiveBuffer.limit();
				receiveBuffer.get(b);
				// 解密
				byte[] rb = receiveRC4.decrypt(b);
				byteOutStream.write(rb);
			}

			receiveBuffer.compact();

			if (len <= 0) {
				return;
			}


			len = byteOutStream.size();
			while (len > 0) {
				byte[] tmp = byteOutStream.toByteArray();
				byteOutStream.reset();
				tracker.reset();
				int itok = tracker.track(tmp, 0);
				if (itok > 0) {
					String message = new String(tmp, 0, itok, "UTF-8");
					dealReceiveMsg(message);
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
		 * 处理接收到的消息
		 * @throws JSONException 
		 */
		private void dealReceiveMsg(String msg) throws Exception {
			if (Utils.debug) {
				Log.i(TAG, "receive msg is:" + msg);
			}

			// 接收到数据后将最后发送时间设置为无效
			lastSendTime = -1;

			int type = Utils.getMessageType(msg);
			if (type != MsgInfor.SERVER_PONG) {
				if (WAIT_TIME > 100) {
					Log.e(TAG, "change wait time back to 100ms");
					WAIT_TIME = 100;
				}
				if (TxData.time > 0) {
					TxData.time = System.currentTimeMillis();
				}
			}

			// 握手应答
			if (type == MsgInfor.SERVER_SHAKE_HAND) {
				dealShakeMsg(msg);
			}
			// key交换返回
			else if (type == MsgInfor.SERVER_KEY_CHARGE) {
				dealKeyMsg(msg);
			}
			// 用户授权返回
			else if (type == MsgInfor.SERVER_ACCESS_AUTH) {
				dealAuthMsg(msg);
			}
			// 心跳
			else if (type == MsgInfor.SERVER_PONG) {
				pong = System.currentTimeMillis();
			}
			// 正式消息
			else if (type == MsgInfor.SERVER_RECEIVE_MSG) {
				preHandleNormalMsg(msg);
				dealOtherMsg(msg);
			}
			// 其他消息
			else {
				dealOtherMsg(msg);
			}
		}

		private void dealOtherMsg(String msg) {
			Intent intent = new Intent(context, LBSMsgHandleService.class);
			intent.putExtra("msg", msg);
			context.startService(intent);
		}

		/**
		 * 处理正式消息返回
		 * @throws Exception 
		 */
		private void preHandleNormalMsg(String msg) throws Exception {
			// 正式消息返回后将其从缓存中删除
			sentMsgCashe.remove(new JSONObject(msg).getInt("msgid"));
		}

		/**
		 * 处理用户授权返回
		 */
		private void dealAuthMsg(String msg) throws Exception {
			JSONObject jo = new JSONObject(msg);
			int d = jo.getInt("d");
			switch (d) {
			case 0:
				initState = WAIT_NORMAL_MSG;
				break;
			// uid不存在
			case 3:
				// token不匹配
			case 4:
				initState = WAIT_ACCESS_AUTH;
				break;
			default:
				clearCurrentConnection();
				break;
			}
		}

		/**
		 * 处理key交换返回
		 */
		private void dealKeyMsg(String msg) throws Exception {
			JSONObject jo = new JSONObject(msg);
			String key = jo.getString("k");
			sendRC4 = new RC4R(Utils.string2byte(key));
			initState = WAIT_ACCESS_AUTH;
		}

		/**
		 * 处理握手应答
		 */
		private void dealShakeMsg(String msg) throws Exception {
			JSONObject jo = new JSONObject(msg);

			JSONObject serverJo = jo.optJSONObject("server");
			if (serverJo != null) {
				editor.putInt("", serverJo.optInt("tm"));
				editor.putInt("", serverJo.optInt("ver"));
				editor.commit();
			}

			int d = jo.getInt("d");
			switch (d) {
			case 0:
				initState = WAIT_KEY_CHARGE;
				int algid = jo.getInt("algid");
				if (algid != 0) {
				}
				break;
			// 操作失败
			case 1:
				clearCurrentConnection();
				break;
			// 服务器忙
			case 2:
				// 服务器维护
			case 3:
				clearCurrentConnection();
				synchronized (lock) {
					running = false;
				}
				Toast.makeText(context, "服务器忙或维护中, 请稍后再试...", Toast.LENGTH_LONG).show();
				break;
			// 客户端版本有更新
			case 4:
//				editor.putInt(CommonData.UPDATA_VER, jo.optInt("ver"));
//				editor.putString(CommonData.UPDATA_URL, jo.optString("url"));
//				editor.putString(CommonData.UPDATA_LOG, jo.optString("log"));
//				editor.commit();
				mSess.mPrefMeme.updata_ver.setVal(jo.optInt("ver"));
				mSess.mPrefMeme.updata_url.setVal(jo.optString("url"));
				mSess.mPrefMeme.updata_log.setVal(jo.optString("log")).commit();
				String action = null;
				if (jo.optBoolean("enable")) {
					action = Constants.INTENT_ACTION_AUTO_VERSION_UPDATE;
					initState = WAIT_KEY_CHARGE;
				} else {
					action = Constants.INTENT_ACTION_FORCE_VERSION_UPDATE;
					clearCurrentConnection();
					synchronized (lock) {
						running = false;
					}
				}
				Intent intent = new Intent(action);
				context.sendBroadcast(intent);
				break;
			// 服务器已转移
			case 5:
				clearCurrentConnection();
				destHost = jo.optString("addr");
				destPort = jo.optInt("port");
				break;
			default:
				clearCurrentConnection();
				break;
			}
		}

		/**
		 * 设置host和port的值
		 */
		private void setHostAndPort() throws Exception {
			HttpURLConnection conn = null;
			try {
				conn = (HttpURLConnection) new URL(url).openConnection();
				conn.setConnectTimeout(5000);
				if (conn.getResponseCode() == 200) {
					InputStream ips = conn.getInputStream();
					String encoding = conn.getContentEncoding();
					if (encoding == null) {
						encoding = "UTF-8";
					}
					String json = Utils.readTextFromStream(ips, encoding);
					JSONObject jObj = new JSONObject(json);
					JSONArray arr = jObj.getJSONArray("v2");
					// 从数组中随机获取一个
					JSONObject addressObj = arr.getJSONObject(rand.nextInt(arr.length()));
					destHost = addressObj.getString("addr");
					destPort = addressObj.getInt("port");
				} else {
					throw new IOException("setHostAndPort(): connection occurs error");
				}
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
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
		
		private InetSocketAddress server;

		/**
		 * 建立和server的连接
		 */
		private void generateConnection() throws Exception {
			if (Utils.debug) {
				Log.e(TAG, "generateConnection()");
			}
			pong = System.currentTimeMillis();
			clearCurrentConnection();
			isWaitingConnectable = true;

			if (server == null || server.isUnresolved()) {
				// 如果未设置server的地址和端口号
				if ("".equals(destHost) || -1 == destPort) {
					setHostAndPort();
				}
				server = new InetSocketAddress(destHost, destPort);
			}
			selector = Selector.open();
			channel = SocketChannel.open();
			channel.configureBlocking(false);
			channel.register(selector, SelectionKey.OP_CONNECT | SelectionKey.OP_READ
					| SelectionKey.OP_WRITE);
			channel.connect(server);
			preNetType = Utils.getNetworkTypeSimple();
			// 建立连接也加入20s超时判定
			lastSendTime = System.currentTimeMillis();
		}

		/**
		 * 清除当前连接
		 */
		private void clearCurrentConnection() {
			// 设置标记
			lastSendTime = -1;
			initState = WAIT_SHAKE_HAND;
			isWaitingConnectable = false;

			if (Utils.debug)
				Log.e(TAG, "clearCurrentConnection()");

			if (channel == null && selector == null) {
				return;
			}

			try {
				if (selector != null) {
					selector.close();
				}
			} catch (Exception e) {
				if(Utils.debug)e.printStackTrace();
				if (Utils.debug) {
					Log.e(TAG,
							"exception occurs in clearCurrentConnectionWithoutSendMsg(): e.getMessage() = "
									+ e.getMessage());
				}
			} finally {
				selector = null;
				try {
					if (channel != null) {
						channel.socket().close();
						channel.close();
					}
				} catch (Exception e) {
					if(Utils.debug)e.printStackTrace();
					if (Utils.debug) {
						Log.e(TAG,
								"exception occurs in clearCurrentConnectionWithoutSendMsg(): e.getMessage() = "
										+ e.getMessage());
					}
				} finally {
					channel = null;
				}
			}
		}
		
		@Override
		public void run() {
			Thread.currentThread().setName("LBSSocketChannelConnectionImpl");
			while (true) {
				try {
					// 如果running被置为false, 网络连接不可用, 未处于登录状态时, 清理连接后wait
					synchronized (lock) {
						while (!running || !session.isLoginSuccessed()
								|| !Utils.checkNetworkAvailableSimple()) {
							// 如果running为true, 同时网络连接可用
							// 那么就是由于未登录造成的wait, 在wait之前向推信服务器发送ping包
							if (running && Utils.checkNetworkAvailable(context)) {
								if (Utils.debug) {
									Log.e(TAG, "not logined, so send ping");
								}
								mSess.getSocketHelper().sendPing();
							}
							if (Utils.debug) {
								Log.e(TAG, "running is set to be false or network is not available "
										+ "or not logined");
							}
							clearCurrentConnection();
							lock.wait(25 * 1000);
						}
					}

					long currentTime = System.currentTimeMillis();
					if (TxData.time == 0) {
						if (WAIT_TIME > 100) {
							if(Utils.debug)Log.e(TAG, "change wait time back to 100ms");
							WAIT_TIME = 100;
						}
					} else if (currentTime - TxData.time >= 5 * 60 * 1000 && WAIT_TIME == 100) {
						if(Utils.debug)Log.e(TAG, "change wait time to 5s");
						WAIT_TIME = 5000;
					}
					// 如果上次发送时间有效且大于最大间隔时间
					if (lastSendTime != -1 && currentTime - lastSendTime > TIME_OUT) {
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
						generateConnection();
					}
					// 处理网络切换到WIFI后连接没有中断的情况
					if (preNetType != Utils.NET_WIFI && Utils.getNetworkTypeSimple() == Utils.NET_WIFI) {
						throw new IOException("it's time to change network");
					}

					communicateToServer();

				} catch (Exception e) {
					if(Utils.debug)Log.e(TAG, e.getMessage() + ", " + e.getCause());
					if(Utils.debug)e.printStackTrace();
					// 发生异常时清除连接
					clearCurrentConnection();
					synchronized (lock) {
						try {
							lock.wait(1000);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * 接收网络改变的广播
	 */
	private class LoginSuccessedReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (Utils.debug) {
				Log.i(TAG, "login successes");
			}
			synchronized (lock) {
				if (running) {
					if (Utils.debug) {
						Log.i(TAG, "LoginSuccessedReceiver: send ping");
					}
					sendMessage(ping);
				}
			}
		}
	}

	/**
	 * 将连接设置为初始状态
	 */
	public void recovery() {
		if (Utils.debug) {
			Log.e(TAG, "recovery");
		}
		synchronized (msgList) {
			msgList.clear();
		}
		synchronized (lock) {
			running = false;
		}
	}
}
