package com.tuixin11sms.tx.dao;

import java.util.ArrayList;

import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.IPraiseNoticeUpdate;
import com.tuixin11sms.tx.message.PraiseNotice;

public interface PraiseNoticeDao {
	//被赞消息的虚拟发送者id，因为会话的sessionId生成需要一个id

	/**增加一个被赞消息*/
	boolean add(PraiseNotice pn);
	/**删除一个被赞消息*/
	int delete(long id);
	/**获取一个被赞消息*/
	PraiseNotice get(long id);

	/**更新一个被赞消息*/
	int update(PraiseNotice pn);
	
	ArrayList<PraiseNotice> getNoticeList();
	
	void registObserver(IPraiseNoticeUpdate iPnu);
}
