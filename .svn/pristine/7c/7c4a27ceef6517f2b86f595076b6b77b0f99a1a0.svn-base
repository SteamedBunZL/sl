package com.tuixin11sms.tx.net;

import java.util.LinkedHashMap;
import java.util.Map;

public class LBSSessionManager {
	private Map<Integer, String> map = new LinkedHashMap<Integer, String>();
	private boolean lbsLoginSuccessed = false;

	private static LBSSessionManager manager = new LBSSessionManager();

	private LBSSessionManager() {}
	
	public static LBSSessionManager getLBSSessionManager() {
		return manager;
	}
	
	public synchronized boolean isLbsLoginSuccessed() {
		return lbsLoginSuccessed;
	}

	public synchronized void setLbsLoginSuccessed(boolean lbsLoginSuccessed) {
		this.lbsLoginSuccessed = lbsLoginSuccessed;
	}

	public synchronized void joinChannel(int channelID, String msgInfo) {
		map.remove(channelID);
		map.put(channelID, msgInfo);
	}

	/**
	 * 离开频道， 如果map中存在channelID对应的频道返回true， 否则返回false
	 */
	public synchronized boolean leaveChannel(int channelID) {
		return map.remove(channelID) != null;
	}

	public synchronized Map<Integer, String> getJoinedChannelMap() {
		return new LinkedHashMap<Integer, String>(map);
	}
	
	public synchronized void clearAllChannel() {
		map.clear();
	}
	
	/**
	 * 判断已加入频道列表是否为空
	 */
	public synchronized boolean isNoChannelIn() {
		return map.isEmpty();
	}
	
	/**
	 * 判断某个频道是否在线, 包括前台和后台
	 */
	public synchronized boolean isChannelOnline(int channelID) {
		return map.containsKey(channelID);
	}
}