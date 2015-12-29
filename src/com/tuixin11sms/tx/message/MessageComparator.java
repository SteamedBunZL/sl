package com.tuixin11sms.tx.message;

import java.util.Comparator;

import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

/**
 * 单聊按照消息日期排序比较，群聊按照gmid排序比较，该Comparator名字后期改为MessageComparator
 */
public class MessageComparator implements Comparator<Object>{
	private static final String TAG = "MessageComparator";

	@Override
	public int compare(Object object1, Object object2) {
		long first = 0 ;
		long second= 0 ;
		if(object1 instanceof TXMessage){
			TXMessage ms1 =((TXMessage) object1);
			TXMessage ms2 = (TXMessage) object2;
			if(Utils.isIdValid(ms1.group_id)&&Utils.isIdValid(ms2.group_id)){
				//是群消息
				//加入判断 gmid为0的就用日期比较
				if(ms1.gmid!=ms2.gmid&&ms1.gmid!=0&&ms2.gmid!=0){
					//如果gmid不相等，代表二者有gmid,按gmid排序
					return signum(ms1.gmid-ms2.gmid);
				}
			}
			if(ms1.msg_type<30 || ms1.msg_type> 40)
				//单聊、群聊消息或者系统通知消息
			    first = ((TXMessage) object1).send_time*1000;
			else
				//系统短信发送的普通或图片等类型消息
				first = ((TXMessage) object1).send_time;
			
			if(ms2.msg_type<30 || ms2.msg_type> 40)
			    second = ((TXMessage) object2).send_time*1000;
			else
				second = ((TXMessage) object2).send_time;
			if(Utils.debug){
				Log.i(TAG, "比较的是消息，二者的gmid分别是："+ms1.gmid+","+ms2.gmid+";;ms1.sendTime="+ms1.send_time+",ms1.msg_body="+ms1.msg_body+",ms2.sendTime="+ms2.send_time+",ms2.msg_body="+ms2.msg_body);
			}
			return signum(first - second);
			
			
		}else if(object1 instanceof MsgStat){
			//会话消息暂时不按gmid排序，单聊消息没有gmid
			MsgStat ms1 =((MsgStat) object1);
			MsgStat ms2 = (MsgStat) object2;
			if(ms1.msg_type<30 || ms1.msg_type> 40)
			   first = (((MsgStat) object1).msg_date*1000);
			else
				first = ((MsgStat) object1).msg_date;
			if(ms2.msg_type<30 || ms2.msg_type> 40)
			    second = ((MsgStat) object2).msg_date*1000;
			else
				second = ((MsgStat) object2).msg_date;
			return signum(second-first);
		}		
		return signum(first - second);
	}

	public static final int signum(long l) {
		if (l > 0)
			return 1;
		if (l < 0)
			return -1;
		else
			return 0;
	}
}
