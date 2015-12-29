package com.tuixin11sms.tx.group;

import java.util.Comparator;

import android.util.Log;

import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 
 * 按群类型排序，我创建的排前面
 * 
 */
public class GroupTypeComparator implements Comparator<Object> {
	private static final String TAG = "GroupTypeComparator";
	private int testTag = 0;


	@Override
	public int compare(Object object1, Object object2) {

		int result = 0;
		if (Utils.debug) {
			Log.i(TAG, "TxGroup比较了第---" + (++testTag) + "---次");
		}
		if (Utils.debug && object1 == null) {
			Log.e(TAG, "obj为空是什么意思？");
			Log.i(TAG, "要比较的第一个TxGroup【" + ((TxGroup) object1).toString() + "】");
			Log.i(TAG, "要比较的第二个TxGroup【" + ((TxGroup) object2).toString() + "】");
		}
		TxGroup tx1 = (TxGroup) object1;
		TxGroup tx2 = (TxGroup) object2;
		// 如果是我创建的群，则优先排序
		if (tx1.group_tx_state == TxDB.QU_TX_STATE_OWN
				&& tx2.group_tx_state == TxDB.QU_TX_STATE_OWN) {
			result = (int) (tx1.group_id - tx2.group_id);
		} else if (tx1.group_tx_state == TxDB.QU_TX_STATE_OWN) {
			result = -1;
		} else if (tx2.group_tx_state == TxDB.QU_TX_STATE_OWN) {
			result = 1;
		} else {
			result = (int) (tx1.group_id - tx2.group_id);
		}
		return result;
	}

}
