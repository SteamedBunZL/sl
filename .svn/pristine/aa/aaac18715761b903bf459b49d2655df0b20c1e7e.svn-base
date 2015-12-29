package com.tuixin11sms.tx.task;

import java.util.LinkedList;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

public class TuixinQueue<T> {
	private LinkedList<T> list1 = new LinkedList<T>();
	private LinkedList<T> list2 = new LinkedList<T>();
	private Object mNotifier = new Object();
	private QueneCallBack mcallBack;
	private int mLimit1;
	private int mLimit2;
	private Thread waiThread;

	public TuixinQueue(int mLimit1, int mLimit2, QueneCallBack callBack,
			int type) {
		this.mLimit1 = mLimit1;
		this.mLimit2 = mLimit2;
		mcallBack = callBack;
		if (type == 0) {
			waiThread = new Thread() {
				public void run() {
					while (true) {
						// if (TaskFinish.isLoadFinished()) {
						T t = poll();
						if (t != null) {
							// TaskFinish.setLoadFinished(false);
							mcallBack.handle((Message) t);
						}
						// }
					}
				};
			};
		} else {
			waiThread = new Thread() {
				public void run() {
					while (true) {
						if (TaskFinish.isDownFinished()) {
							T t = poll();
							if (t != null) {
								TaskFinish.setDownFinished(false);
								mcallBack.handle((Message) t);
							}
						}
					}
				};
			};
		}
	}

	/**
	 * 进队列的策略 现在为一种策略，下载和加载的时候，都是让task进入第一链表，然后如果超过链表指定长度
	 * 优先把后放进的任务放在第一链表中，正在执行任务不移出，移出任务放入第二链表，等待执行依次类推
	 * 
	 * @param task
	 * @return
	 */

	public void add(T t) {
		if (t != null) {
			list1.addLast(t);
			if (list1.size() > mLimit1) {
				T removeT = list1.removeFirst();
				list2.addFirst(removeT);
			}
		}
	}

	public synchronized T poll() {
		T t = null;
		if (list1.size() > 0) {
			t = list1.poll();
		} else if (list2.size() > 0) {
			t = list2.poll();
		}
		return t;
	}

	public void waitTask() {
		if (waiThread == null || !waiThread.isAlive()) {
			waiThread.start();
		}
	}

}
