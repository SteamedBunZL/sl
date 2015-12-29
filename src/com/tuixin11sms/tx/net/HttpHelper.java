package com.tuixin11sms.tx.net;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class HttpHelper {
	private static final String TAG = "HttpHelper";
	/**
	 * 获取附近神聊用户模式
	 */
	public static final int USER_INFO_MODE = 0;
	/**
	 * 获取附近用户留言模式
	 */
	public static final int MESSAGE_INFO_MODE = 1;
	/**
	 * 查询所有性别
	 */
	public static final int QUERY_ALL = -1;
	/**
	 * 只查询男
	 */
	public static final int QUERY_BOY = 0;
	/**
	 * 只查询女
	 */
	public static final int QUERY_GIRL = 1;
	// private static final String GATEWAY_URL =
	// "http://geo.tuibo.com/api/serverlist";
	private static final String GATEWAY_URL = Utils.LBSURL;
	private String host = "118.145.23.31";
	//	private String host = "192.168.16.168";
	//	private String upposUrl = host + "/api/uppos";
	private String arroundUri = "/api/nearby";
	private String clearUri = "/api/clspos";
	private static HttpHelper httpHelper = null;
	private static HttpClient client = null;
	private Context context;
	private SendMsgTask sendMsgTask;
	private static SharedPreferences prefs = null;
	private static Editor editor = null;

	public static HttpHelper getInstance(Context context) {
		if (httpHelper == null) {
			httpHelper = new HttpHelper();
			prefs = context.getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE
					+ Context.MODE_WORLD_WRITEABLE);
			editor = prefs.edit();
		}
		httpHelper.context = context;
		return httpHelper;
	}

	private HttpHelper() {
		client = new DefaultHttpClient();
	}

	/**
	 * 下载并保存新浪微博头像
	 */
	public String downSinaAvatar(String sinaUrl, int s) {
		HttpGet request = null;
		if (client == null)
			client = new DefaultHttpClient();
		try {
			request = new HttpGet(sinaUrl);
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null && statusLine.getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null && entity.getContentLength() > 0) {
					File file = new File(Utils.getStoragePath(context) + "/" + Utils.AVATAR_PATH, "sina_avatar"+s);
					byte[] buffer = new byte[1024];
					InputStream in = entity.getContent();
					OutputStream out = new FileOutputStream(file);
					int len = 0;
					while ((len = in.read(buffer)) > 0) {
						out.write(buffer, 0, len);
					}
					in.close();
					out.close();
					return file.length() > 0 ? file.getAbsolutePath() : null;
				}
			}
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * 通过网关服务器获取LBS服务
	 */
	public void getServerList() {
		if (client == null)
			client = new DefaultHttpClient();
		HttpGet request = new HttpGet(GATEWAY_URL);
		HttpResponse response;
		try {
			long b = System.currentTimeMillis();
			response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null && statusLine.getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
						if (entity.getContentLength() > 0) {
							BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
							String s = null;
							StringBuilder sb = new StringBuilder();
							while ((s = br.readLine()) != null) {
								sb.append(s);
							}
							JSONObject jsonObject = new JSONObject(sb.toString());
							if (jsonObject.has("svrlist")) {
								JSONArray array = jsonObject.getJSONArray("svrlist");
								Random random = new Random();
								int cur = random.nextInt(array.length());
								host = array.getString(cur);
								if (Utils.debug)
									Log.i(TAG, "host is:" + host);
								// array.len
							}
						}
					} catch (JSONException e) {
						//
						if(Utils.debug)e.printStackTrace();
					} finally {
						if (entity != null) {
							entity.consumeContent();
						}
					}
				}
			}
			long e = System.currentTimeMillis();
			if (Utils.debug)
				Log.i(TAG, "Get server list cost time is:" + (e - b) + "ms");
		} catch (ClientProtocolException e) {
			if (Utils.debug)
				Log.e(TAG, "ClientProtocolException", e);
		} catch (IOException e) {
			if (Utils.debug)
				Log.e(TAG, "IOException", e);
		}
	}

	/**
	 * 清除自己的位置消息
	 * 
	 * @throws IOException
	 * @throws ClientProtocolException
	 */
	public int clearSelfPosition() throws ClientProtocolException, IOException {
		if (client == null)
			client = new DefaultHttpClient();
		String url = "http://" + host + clearUri + "?uid=" + TX.tm.getTxMe().partner_id + "&token="
				+ TX.tm.getTxMe().token;
		HttpGet request = new HttpGet(url);
		HttpResponse response = client.execute(request);
		StatusLine statusLine = response.getStatusLine();
		return statusLine.getStatusCode();
	}

	private int send(TXMessage txMsg) throws JSONException, ClientProtocolException, IOException {
		int status = 0;
		String geo = txMsg.geo;
		double latitude = 0;
		double longitude = 0;
		if (geo != null) {
			String[] geos = geo.split(",");
			if (geos.length == 2) {
				latitude = Double.parseDouble(geos[0]);
				longitude = Double.parseDouble(geos[1]);
			}
		}
		StringBuilder urlSb = new StringBuilder("http://" + host + arroundUri);
		urlSb.append("?appid=").append(MESSAGE_INFO_MODE);
		urlSb.append("&uid=").append(TX.tm.getTxMe().partner_id);
		urlSb.append("&lat=").append(latitude);
		urlSb.append("&lng=").append(longitude);
		urlSb.append("&token=").append(TX.tm.getTxMe().token);
		urlSb.append("&query=0");
		if (Utils.debug)
			Log.i(TAG, "sendmsg url is:" + urlSb.toString());
		HttpPost request = new HttpPost(urlSb.toString());
		String body = "";
		switch (txMsg.msg_type) {
		case TxDB.MSG_TYPE_QU_COMMON_SMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype")
					.value(TxDB.MSG_TYPE_COMMON_SMS).key("ct").value(txMsg.msg_body).endObject().toString();
			break;
		case TxDB.MSG_TYPE_QU_IMAGE_EMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype").value(2).key("imgurl")
					.value(txMsg.msg_url).endObject().toString();
			break;
		case TxDB.MSG_TYPE_QU_GEO_SMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype").value(7).key("gpsla")
					.value(latitude).key("gpslo").value(longitude).endObject().toString();
			break;
		case TxDB.MSG_TYPE_QU_AUDIO_EMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype")
					.value(TxDB.MSG_TYPE_AUDIO_EMS).key("aduurl").value(txMsg.msg_url).key("adut")
					.value(txMsg.audio_times).endObject().toString();
			break;
		case TxDB.MSG_TYPE_QU_CARD_EMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype")
					.value(TxDB.MSG_TYPE_CARD_EMS).key("vcurl").value(txMsg.msg_url).key("vcnn")
					.value(txMsg.tcard_name).endObject().toString();
			break;
		case TxDB.MSG_TYPE_QU_TCARD_SMS:
			body = new JSONStringer().object().key("msgid").value(txMsg.msg_id).key("msgtype")
					.value(TxDB.MSG_TYPE_TCARD_SMS).key("tvcid").value(txMsg.tcard_id).key("tvcnn")
					.value(txMsg.tcard_name).key("tvcsex").value(txMsg.tcard_sex).key("tvcaurl")
					.value(txMsg.tcard_avatar_url).endObject().toString();
			break;
		}
		// body = new JSONStringer().object().key("").value("").endObject()
		// .endObject().toString();
		request.setEntity(new StringEntity(body, HTTP.UTF_8));
		HttpResponse response = client.execute(request);
		StatusLine statusLine = response.getStatusLine();
		if (statusLine != null && statusLine.getStatusCode() == 200) {
			HttpEntity entity = response.getEntity();
			if (entity != null) {
				try {
					if (entity.getContentLength() > 0) {
						BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
						String s = null;
						StringBuilder sb = new StringBuilder();
						while ((s = br.readLine()) != null) {
							sb.append(s);
						}
						JSONObject jsonObject = new JSONObject(sb.toString());
						status = jsonObject.getInt("code");
						if (Utils.debug)
							Log.i(TAG, "code is:" + status);
						if (jsonObject.has("msg")) {
							String msg = jsonObject.getString("msg");
							if (Utils.debug)
								Log.i(TAG, "msg is:" + msg);
						}
					}
				} finally {
					if (entity != null) {
						entity.consumeContent();
					}
				}
			}
		} else {
			status = 400;
		}
		return status;
	}

	public void startSendTask() {
		if (sendMsgTask == null) {
			sendMsgTask = new SendMsgTask();
			sendMsgTask.setRunning(true);
			Thread t = new Thread(sendMsgTask);
			t.start();
		}
	}

	public void stopSendTask() {
		if (sendMsgTask != null) {
			sendMsgTask.setRunning(false);
			sendMsgTask = null;
		}
	}

	/**
	 * 上传用户当前位置
	 * 
	 * @param latitude
	 *            经度
	 * @param longitude
	 *            纬度
	 * @throws UnsupportedEncodingException
	 */
	public void postLocation(int appid, double latitude, double longitude) throws UnsupportedEncodingException {

		StringBuilder urlSb = new StringBuilder("http://" + host + arroundUri);
		urlSb.append("?appid=").append(appid);
		urlSb.append("&uid=").append(TX.tm.getTxMe().partner_id);
		urlSb.append("&lat=").append(latitude);
		urlSb.append("&lng=").append(longitude);
		urlSb.append("&token=").append(TX.tm.getTxMe().token);
		urlSb.append("&sex=").append(TX.tm.getTxMe().getSex());
		urlSb.append("&age=").append(TX.tm.getTxMe().age);
		urlSb.append("&city=").append(TX.tm.getTxMe().area);
		urlSb.append("&nickname=").append(URLEncoder.encode(TX.tm.getTxMe().getNick_name()));
		urlSb.append("&sign=").append(
				URLEncoder.encode(TX.tm.getTxMe().sign == null ? "" : TX.tm.getTxMe().sign));
		urlSb.append("&avatar=").append(TX.tm.getTxMe().avatar_url);
		urlSb.append("&query=0");
		if (Utils.debug)
			Log.i(TAG, "postUserInfo url is:" + urlSb.toString());
		HttpPost request = new HttpPost(urlSb.toString());
		if (client == null)
			client = new DefaultHttpClient();
		try {
			HttpResponse response = client.execute(request);
			StatusLine statusLine = response.getStatusLine();
			if (statusLine != null && statusLine.getStatusCode() == 200) {
				HttpEntity entity = response.getEntity();
				if (entity != null) {
					try {
						if (entity.getContentLength() > 0) {
							BufferedReader br = new BufferedReader(new InputStreamReader(entity.getContent()));
							String s = null;
							StringBuilder sb = new StringBuilder();
							while ((s = br.readLine()) != null) {
								sb.append(s);
							}
							JSONObject jsonObject = new JSONObject(sb.toString());
							int code = jsonObject.getInt("code");
							if (Utils.debug)
								Log.i(TAG, "code is:" + code);
							if (jsonObject.has("msg")) {
								String msg = jsonObject.getString("msg");
								if (Utils.debug)
									Log.i(TAG, "msg is:" + msg);
							}
						}
					} catch (JSONException e) {
						//
						if(Utils.debug)e.printStackTrace();
					} finally {
						if (entity != null) {
							entity.consumeContent();
						}
					}
				}
			}
		} catch (ClientProtocolException e) {
			//
			if(Utils.debug)e.printStackTrace();
		} catch (IOException e) {
			//
			if(Utils.debug)e.printStackTrace();
		}
	}

	
	
	public void sendMsg(TXMessage txMsg, double msgroom_latitude, double msgroom_longitude) {
		txMsg.geo = msgroom_latitude + "," + msgroom_longitude;
		sendMsgTask.addMsg(txMsg);
	}

	class SendMsgTask implements Runnable {
		public List<TXMessage> msgList = new LinkedList<TXMessage>();
		private boolean isRunning;

		@Override
		public void run() {
			while (isRunning) {
				synchronized (msgList) {
					if (msgList.size() == 0) {
						try {
							msgList.wait();
						} catch (InterruptedException e) {
							//
							if(Utils.debug)e.printStackTrace();
						}
					} else {
						TXMessage txMsg = msgList.remove(0);
						try {
							int status = send(txMsg);
							if (status == 0)
								notifyUI(Constants.INTENT_ACTION_MESSAGE_SEND_OK, txMsg);
							else
								notifyUI(Constants.INTENT_ACTION_MESSAGE_SEND_FAILED, txMsg);
						} catch (ClientProtocolException e) {
							notifyUI(Constants.INTENT_ACTION_MESSAGE_SEND_FAILED, txMsg);
						} catch (JSONException e) {
							notifyUI(Constants.INTENT_ACTION_MESSAGE_SEND_FAILED, txMsg);
						} catch (IOException e) {
							notifyUI(Constants.INTENT_ACTION_MESSAGE_SEND_FAILED, txMsg);
						}
					}
				}
			}
		}

		private void notifyUI(String action, TXMessage txMsg) {
			Intent intent = new Intent();
			intent.setAction(action);
			intent.putExtra("msgId", txMsg.msg_id);
			httpHelper.context.sendBroadcast(intent);
		}

		public void addMsg(TXMessage txMsg) {
			synchronized (msgList) {
				msgList.add(txMsg);
				msgList.notify();
			}
		}

		public boolean isRunning() {
			return isRunning;
		}

		public void setRunning(boolean isRunning) {
			synchronized (msgList) {
				this.isRunning = isRunning;
				if (!isRunning)
					msgList.notify();
			}
		}
	}

}
