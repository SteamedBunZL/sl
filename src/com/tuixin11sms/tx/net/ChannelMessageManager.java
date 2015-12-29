package com.tuixin11sms.tx.net;

import java.util.ArrayList;

import com.tuixin11sms.tx.message.TXMessage;

public class ChannelMessageManager implements MessageListManager {
	private int channelID;
	private ArrayList<TXMessage> messageList;
	
	/**记录当前聊天室最新gmid,用户给自己发送失败的信息设置*/
	private long channelLatestGmid;

	public ChannelMessageManager(int channelID) {
		this.channelID = channelID;
		messageList = new ArrayList<TXMessage>();
	}

	public int getChannelID() {
		return channelID;
	}

	@Override
	public ArrayList<TXMessage> getMessageList() {
		return messageList;
	}

	@Override
	public boolean isSentClientInfor() {
		return false;
	}

	@Override
	public void setSentClientInfor(boolean sentClientInfor) {
	}

	@Override
	public long getGmid() {
		return channelLatestGmid;
	}

	@Override
	public void setGmid(long gmid) {
		if (channelLatestGmid < gmid) {
			channelLatestGmid = gmid;
		}
		
	}
}
