package com.tuixin11sms.tx.task;

import android.graphics.Bitmap;

import com.tuixin11sms.tx.utils.AsyncCallback;

/**
 * 加载头像图片的回调信息
 * 
 */
public class CallInfo {

	public long mUid;
	public Bitmap mBitmap;
	public String mUrl;
	public AsyncCallback<Bitmap> mCallback;

	public CallInfo(String url, long uid, AsyncCallback<Bitmap> callback) {
		mUid = uid;
		mBitmap = null;
		mUrl = url;
		mCallback = callback;
	}
}
