package com.tuixin11sms.tx.net;

import java.util.ArrayList;

import com.tuixin11sms.tx.message.TXMessage;

public class GroupMessageManager implements MessageListManager {
	/**
	 * 群组ID
	 */
	private long groupID;
	/**
	 * 是否发送了客户端版本信息
	 */
	private boolean sentClientInfor = false;
	/**
	 * 消息列表
	 */
	private ArrayList<TXMessage> messageList;
	
	
	/**记录当前聊天室最新gmid,用户给自己发送失败的信息设置*/
	private long groupLatestGmid;
	

	public GroupMessageManager(long groupID) {
		this.groupID = groupID;
		messageList = new ArrayList<TXMessage>();
	}

	public long getGroupID() {
		return groupID;
	}

	@Override
	public ArrayList<TXMessage> getMessageList() {
		return messageList;
	}

	public boolean isSentClientInfor() {
		return sentClientInfor;
	}

	public void setSentClientInfor(boolean sentClientInfor) {
		this.sentClientInfor = sentClientInfor;
	}

	@Override
	public long getGmid() {
		return groupLatestGmid;
	}

	@Override
	public void setGmid(long gmid) {
		if (groupLatestGmid < gmid) {
			groupLatestGmid = gmid;
		}
		
	}

}
