package com.tuixin11sms.tx.net;

import java.util.ArrayList;

import com.tuixin11sms.tx.message.TXMessage;

/**
 * 消息列表管理类的公有接口
 */
public interface MessageListManager {

	long getGmid();
	void setGmid(long gmid);//为了防止最新的gmid被覆盖，在setGmid时做判断，小的话就不修改了
	
	ArrayList<TXMessage> getMessageList();
	boolean isSentClientInfor();
	void setSentClientInfor(boolean sentClientInfor);
}
