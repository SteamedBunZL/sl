package com.tuixin11sms.tx.net;

import java.util.HashSet;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import android.content.Context;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 禁言管理
 * @author xing
 *
 */
public class TuiboSlienceManager {
	private static final String TAG = TuiboSlienceManager.class.getSimpleName();
	private static TuiboSlienceManager tsm;
	private static Context txData;

	/**
	 * 创建者频道集合
	 */
	private Set<Integer> createSet = new HashSet<Integer>();
	/**
	 * 管理员频道集合
	 */
	private Set<Integer> adminSet = new HashSet<Integer>();
	/**
	 * 是否为全局管理员
	 */
	private boolean globalAdmin = false;

	public static TuiboSlienceManager getInstance() {
		if (tsm == null) {
			synchronized (TuiboSlienceManager.class) {
				if (tsm == null) {
					tsm = new TuiboSlienceManager();
				}
			}
		}
		return tsm;
	}

	public static void init(Context txData) {
		TuiboSlienceManager.txData = txData;
	}

	/**
	 * 更新聊天室权限
	 * @param pri 0为无权限, 1为创建者, 2为管理者
	 * @param channelID
	 */
	public void updateChannelPri(int pri, boolean isAdd, int channelID) {
		if (Utils.debug) {
			Log.d(TAG, "pri = " + pri + ", isAdd = " + isAdd + ", channelID = " + channelID);
		}
		if (pri == 0) {
			createSet.remove(channelID);
			adminSet.remove(channelID);
		} else if (pri == 1) {
			if (isAdd) {
				createSet.add(channelID);
			} else {
				createSet.remove(channelID);
			}
		} else if (pri == 2) {
			if (isAdd) {
				adminSet.add(channelID);
			} else {
				adminSet.remove(channelID);
			}
		}
	}

	public void setGlobalAdmin(boolean globalAdmin) {
		if (Utils.debug) {
			Log.d(TAG, "setGlobalAdmin = " + globalAdmin);
		}
		this.globalAdmin = globalAdmin;
	}

	/**
	 * 清空当前用户的禁言channel集合
	 */
	public void clearSlienceChannels() {
		createSet.clear();
		adminSet.clear();
		globalAdmin = false;
	}

	/**
	 * 检查当前用户是否在channelID指定的频道中拥有禁言权限
	 * @param channelID
	 * @return
	 */
	public boolean hasSlienceRight(int channelID) {
		return globalAdmin || createSet.contains(channelID) || adminSet.contains(channelID);
	}

	/**
	 * 执行禁言
	 * @param channelID 频道ID
	 * @param slienceUserID 要禁言的人的ID
	 * @param observer 回调
	 */
	public void slienceOpt(final int channelID, final long slienceUserID, SlienceObserver observer) {
		final SlienceObserver observer2 = observer;
		new Thread() {
			public void run() {
				DefaultHttpClient client = new DefaultHttpClient();
				StringBuilder sb = new StringBuilder();
				sb.append("?").append("Uid=").append(SessionManager.getInstance().getUserid()+"")
						.append("&");
				sb.append("TUid=").append(slienceUserID).append("&");
				sb.append("token=").append(SessionManager.getInstance().getToken()).append("&");
				sb.append("ChannelId=").append(channelID);
				String requestUrl = SocketHelper.SILENCE_Url + sb.toString();
				HttpGet get = new HttpGet(requestUrl);
				if (Utils.debug) {
					Log.d(TAG, requestUrl);
				}
				try {
					HttpResponse response = client.execute(get);
					if (response.getStatusLine().getStatusCode() == 200) {
						HttpEntity entity = response.getEntity();
						String str = EntityUtils.toString(entity, "utf-8");
						JSONObject jo = new JSONObject(str);
						int flag = jo.getInt("D");
						observer2.slience(flag);
					}
				} catch (Exception e) {
					if(Utils.debug)e.printStackTrace();
					observer2.slience(4);
				}
			};
		}.start();
	}

	public static class SlienceObserver {
		public void slience(int flag) {
		}
	}
}
