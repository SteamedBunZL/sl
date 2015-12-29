package com.tuixin11sms.tx.utils;

/**
 * 与消息相关的工具类
 * @author SHC
 *
 */
public class MessageUtil {

	/**计算音频消息时长*/ 
	public static String getRecordTime(long adiou_time) {
		StringBuffer recordtime = new StringBuffer();
		if (adiou_time >= 60) {
			recordtime.append(adiou_time / 60 + "'");
		}
		// 如果音频时长大于0则加上"“"显示，如果为0，那么就什么都不显示，显示空字符串
		if (adiou_time > 0) {
			recordtime.append(adiou_time % 60 + "“");
		}
		return recordtime.toString();
	}

}
