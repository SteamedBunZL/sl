package com.tuixin11sms.tx.contact;

import java.util.Comparator;

import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

/**
 * 
 * 首字母排序
 * 
 */
public class FirstCharComparator implements Comparator<Object> {
	private static final String TAG = "FirstCharComparator";
	private int type;
	// public static final int CONTACT_TYPE = 0;// 没有人用，2013.10.16 shc
	// public static final int TX_CS_TYPE=1;
	// public static final int TX_TB_TYPE=2;
	// public static final int TXMESSAGE_SEND_TIME = 3;// 没有人用，2013.10.16 shc

	private int testTag = 0;

	public FirstCharComparator(int type) {
		this.type = type;
	}

	@Override
	public int compare(Object object1, Object object2) {

		String name_pinyin1 = "";
		String name_pinyin2 = "";
		int result = 0;
		switch (type) {
		case TxInfor.TX_TYPE_TB:
			if (Utils.debug) {
				Log.i(TAG, "比较了第---" + (++testTag) + "---次");
			}
			if (Utils.debug && object1 == null) {
				Log.e(TAG, "obj为空是什么意思？");
				Log.i(TAG, "要比较的第一个TX【" + ((TX) object1).toString() + "】");
				Log.i(TAG, "要比较的第二个TX【" + ((TX) object2).toString() + "】");
			}
			TX tx1 = (TX) object1;
			TX tx2 = (TX) object2;

			name_pinyin1 = tx1.nick_name_pinyin.toLowerCase();
			name_pinyin2 = tx2.nick_name_pinyin.toLowerCase();
			if (tx1.getTxInfor().getStarFriend() == TxInfor.TX_STAR_FRIEND
					|| tx2.getTxInfor().getStarFriend() == TxInfor.TX_STAR_FRIEND) {
				// 如果是星标好友则优先排名,
				// 如果tx1.getStarFriend()-tx2.getStarFriend()大于0，代表tx1为星标好友，那么result为负值tx1才能排名靠前
				result = -(tx1.getTxInfor().getStarFriend() - tx2.getTxInfor()
						.getStarFriend());
				break;
			}
			result = name_pinyin1.compareTo(name_pinyin2);
			break;

		}
		return result;
	}

	public static final int signum(int diff) {
		if (diff > 0)
			return 1;
		if (diff < 0)
			return -1;
		else
			return 0;
	}
}
