/*
 Copyright (c) 2012 Roman Truba

 Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 permit persons to whom the Software is furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all copies or substantial
 portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED
 TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH
 THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package ru.truba.touchgallery.TouchView;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Utils;

public class UrlTouchImageView extends RelativeLayout {
	protected static final String TAG = "UrlTouchImageView";
	protected ProgressBar mProgressBar;
	protected TouchImageView mImageView;
	protected SessionManager mSess;

	protected Context mContext;

	public UrlTouchImageView(Context ctx) {
		super(ctx);
		mContext = ctx;
		init();

	}

	public UrlTouchImageView(Context ctx, AttributeSet attrs) {
		super(ctx, attrs);
		mContext = ctx;
		init();
	}

	public TouchImageView getImageView() {
		return mImageView;
	}

	protected void init() {

		mSess = SessionManager.getInstance();

		mImageView = new TouchImageView(mContext);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		mImageView.setLayoutParams(params);
		this.addView(mImageView);
		mImageView.setScaleType(ScaleType.MATRIX);
		//给view设置一个透明的图片，否则正在加载状态不可触摸滑动
		mImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.transparent));
		

		// mProgressBar = new ProgressBar(mContext, null,
		// android.R.attr.progressBarStyleHorizontal);
		mProgressBar = new ProgressBar(mContext, null, android.R.attr.progressBarStyleSmall);
		params = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		params.setMargins(30, 0, 30, 0);
		mProgressBar.setLayoutParams(params);
		mProgressBar.setIndeterminate(false);
		mProgressBar.setMax(100);

		// mProgressBar = new ProgressBar(mContext);
		// params = new LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// params.addRule(RelativeLayout.CENTER_VERTICAL);
		// params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		// mProgressBar.setLayoutParams(params);
		// mProgressBar.setIndeterminate(false);
		// mProgressBar.setProgressDrawable(getResources().getDrawable(R.drawable.progress_ring));
		// mProgressBar.setMax(100);

		this.addView(mProgressBar);
	}

	Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				if (msg.obj != null) {
					Bitmap mBitmap = (Bitmap) msg.obj;
					mImageView.setScaleType(ScaleType.MATRIX);
					mImageView.setImageBitmap(mBitmap);
				}
				mProgressBar.setVisibility(GONE);
				break;
			}
			}
		};
	};

	public void setUrl(String imageUrl) {
		String file = null;// 相册图片路径
		if (imageUrl != null) {
			file = mSess.mDownUpMgr.getAlbumFile(imageUrl, true);
			if (Utils.debug) {
				Log.i(TAG, "相册图片路径file:" + file);
			}
			if (file != null) {
				File avatar = new File(file);
				if (avatar.exists()) {
					Bitmap bitmap = Utils.readBitMap(file, true);
					if (bitmap != null) {
						mAvatarHandler.obtainMessage(1, bitmap).sendToTarget();
						return;
					}
				}
			}
			if (Utils.debug) {
				Log.i(TAG, "相册图片路径--" + file + "--不存在");
			}
	

			{
				// 加载相册大图前先显示小图？
				String smallFile = mSess.mDownUpMgr.getAlbumFile(imageUrl, false);
				if (smallFile != null) {
					File album = new File(smallFile);
					if (album.exists()) {
						Bitmap bitmap = Utils.readBitMap(smallFile, false);
						if (bitmap != null) {
							mAvatarHandler.obtainMessage(1, bitmap).sendToTarget();
						}
					}
				}
			}
	
			mSess.mDownUpMgr.downloadImg(imageUrl, file, 0, true, true, new FileTransfer.DownUploadListner() {
				// 此处要记录文件名，同时要加载
				@Override
				public void onFinish(FileTaskInfo taskInfo) {
					Bitmap bitmap = Utils.readBitMap(taskInfo.mFullName, true);
					if (bitmap != null) {
						mAvatarHandler.obtainMessage(1, bitmap).sendToTarget();
					}
	
				}
	
				@Override
				public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
					mAvatarHandler.obtainMessage(1, null).sendToTarget();
				}
			}, null);
		}
	}
}
