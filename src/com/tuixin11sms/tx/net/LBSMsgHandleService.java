package com.tuixin11sms.tx.net;

import android.app.IntentService;
import android.content.Intent;

/**
 * 该类以单独的子线程处理所有LBS消息解析
 */
public class LBSMsgHandleService extends IntentService {

	public LBSMsgHandleService() {
		super("the thread handlering received msg in lbs model");
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		String msg = intent.getStringExtra("msg");
		LBSSocketHelper.getLBSSocketHelper(this).dealMsg(msg);
	}
}
