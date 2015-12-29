package com.tuixin11sms.tx.net;

import java.util.ArrayList;

import com.tuixin11sms.tx.message.TXMessage;

/**
 * 单聊界面的消息列表管理类
 */
public class SingleMessageManager implements MessageListManager {
	/**
	 * 单聊对象的uid
	 */
	private final long userid;
	/**
	 * 是否发送了客户端版本信息
	 */
	private boolean sentClientInfor = false;
	/**
	 * 单聊界面的消息列表
	 */
	private ArrayList<TXMessage> messageList;
	
	
	/**记录当前聊天室最新gmid,用户给自己发送失败的信息设置*/
	private long singleRoomLatestGmid;
	

	public SingleMessageManager(long userid) {
		this.userid = userid;
		messageList = new ArrayList<TXMessage>();
	}
	
	public boolean isSentClientInfor() {
		return sentClientInfor;
	}

	public void setSentClientInfor(boolean sentClientInfor) {
		this.sentClientInfor = sentClientInfor;
	}

	public long getUserid() {
		return userid;
	}

	@Override
	public ArrayList<TXMessage> getMessageList() {
		return messageList;
	}

	@Override
	public long getGmid() {
		return singleRoomLatestGmid;
	}

	@Override
	public void setGmid(long gmid) {
		if (singleRoomLatestGmid < gmid) {
			singleRoomLatestGmid = gmid;
		}
		
	}
}