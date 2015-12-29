package com.tuixin11sms.tx.callbacks;

import com.tuixin11sms.tx.message.TXMessage;

public interface RecordListener {
	public final static int ERR_FAILCREATEFILE=1;
	public final static int ERR_DEVNOTINITED=2;
	public final static int ERR_DEVFILEERR=3;	
	public final static int ERR_INTERNALERR=4;	
	
	public void doRecordVolume(float volume);
	public void onPlayFinish(TXMessage txMsg);
	public void uploadFinish(TXMessage txMsg);
	public void recordError(int errcode);
	public void deviceInitOK();
}
