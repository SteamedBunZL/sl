package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.engine.BlogOpea;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 瞬间查看大图界面
 * 
 */
public class CheckBigImgActivity extends BaseActivity {
	private static final String TAG = CheckBigImgActivity.class.getSimpleName();
	public static final String IMG_URL = "img_url";
	public static final String IMG_PATH = "img_path";
	private long time_down;
	private long time_up;
	private ImageView image;
	private Bitmap img = null;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**** 屏幕初始化 *****/
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		image = new ImageView(this);
		image.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		setContentView(image);

		Intent intent = getIntent();
		String imgpath = intent.getStringExtra(IMG_PATH);
		String imgurl = intent.getStringExtra(IMG_URL);

		if (!Utils.isNull(imgpath)) {
			img = getBitmap(imgpath);
			image.setImageBitmap(img);
		} else {
			if (!Utils.isNull(imgurl)) {
				loadAlbumImg(0, imgurl, new AsyncCallback<Bitmap>() {
					@Override
					public void onSuccess(Bitmap result, long id) {
						image.setImageBitmap(result);
						// notifyDataSetChanged();
						result = null;
					}
					@Override
					public void onFailure(Throwable t, long id) {

					}
				});
			}
		}
	}

	public Bitmap getBitmap(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(path);
			int fileSize = fis.available();
			if(fileSize<1024*1024){
				//不做压缩等处理，直接上传
				
				BitmapFactory.Options opts = new BitmapFactory.Options();
				opts.inJustDecodeBounds = true;

				BitmapFactory.decodeFile(path, opts);
				// opts.inSampleSize = Utils.computeSampleSize(opts, -1,
				// Utils.wxb_default);
				// 数字越大读出的图片占用的heap越小 不然总是溢出
				if (file.length() < 20480) { // 0-20k
					opts.inSampleSize = 1;
				} else if (file.length() < 51200) { // 20-50k
					opts.inSampleSize = 1;
				} else if (file.length() < 307200) { // 50-300k
					opts.inSampleSize = 1;
				} else if (file.length() < 819200) { // 300-800k
					opts.inSampleSize = 2;
				} else if (file.length() < 1048576) { // 800-1024k
					opts.inSampleSize = 3;
				} else if (file.length() < 1048576 * 2) { // 1024-1024k
					opts.inSampleSize = 4;
				} else {
					opts.inSampleSize = 5;
					return null;
				}
				opts.inJustDecodeBounds = false;
				try {

					 return BitmapFactory.decodeFile(path, opts);
				} catch (Exception e) {
					if (Utils.debug)
					Log.e(TAG, "解析加载图片fitSizeAutoImg异常", e);
					return null;
				} catch (OutOfMemoryError err) {
					System.gc();
					if (Utils.debug) {
						Log.e(TAG, "修改加载大图片策略后又OOM异常了", err);
					}
					try {
						return BlogOpea.fitSizeImg(path, 3);
					} catch (OutOfMemoryError oerr) {
						System.gc();
						if (Utils.debug) {
							Log.e(TAG, "修改为加载小图片策略后咋会也OOM异常了", oerr);
						}
					}
					return null;
				}
				
			}else {
				//压缩至总像素数小于一百万
//				resultBm = getImgByPath(img_path, 1000000);
				return compressBitmapByMaxPix(path, 1000000);
			}
		} catch (IOException e) {
			if(Utils.debug){
				Log.e(TAG, "压缩相册图片，出现文件未找到异常",e);
			}
		}finally{
			try {
				fis.close();
			} catch (IOException e2) {
				if(Utils.debug){
					Log.e(TAG, "文件输入流关闭异常",e2);
				}
			}
		}
		return null;
	}
	
	/**
	 * 按比例压缩bitmap到指定像素
	 * @param srcBitmap 需要压缩的bitmap
	 * @param maxPixNum 压缩后最大的像素数
	 */
	private Bitmap compressBitmapByMaxPix(String filePath, int maxPixNum) {
		if(!new File(filePath).exists()){
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options,maxPixNum);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		try {
			 return BitmapFactory.decodeFile(filePath, options);
		} catch (Exception e) {
			if (Utils.debug)
			Log.e(TAG, "解析加载图片fitSizeAutoImg异常", e);
			return null;
		} catch (OutOfMemoryError err) {
			System.gc();
			if (Utils.debug) {
				Log.e(TAG, "修改加载大图片策略后又OOM异常了", err);
			}
			try {
				return BlogOpea.fitSizeImg(filePath, 3);
			} catch (OutOfMemoryError oerr) {
				System.gc();
				if (Utils.debug) {
					Log.e(TAG, "修改为加载小图片策略后咋会也OOM异常了", oerr);
				}
			}
			return null;
		}
	}
	
	/**
	 * 计算图片的缩放值
	 * @param options
	 * @param maxPixNum
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,int maxPixNum) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    if(height*width>maxPixNum){
	    	//图片像素数大于指定的最大像素数，进行压缩
	    	inSampleSize = maxPixNum/(height*width);
	    }
        return inSampleSize;
	}
	

	@Override
	protected void onStop() {
		if (img != null) {
			if (!img.isRecycled()) {
				img.recycle();
			}
			img = null;
		}
		super.onStop();
	}

	// 下载图片
	// 这个加载不能用BaseActivity中的，因为这个相册的图片不能被圆角化
	Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				Object[] obj = (Object[]) msg.obj;
				CallInfo callinfo = (CallInfo) obj[0];
				callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
				break;
			}
			}
		};
	};
	Handler mAsynloader;

	public void prepairAsyncload() {
		mAsynloader = new Handler(SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				CallInfo ci;
				switch (msg.what) {
				case 1:
					Object[] obj = (Object[]) msg.obj;
					ci = (CallInfo) obj[0];
					if (ci.mUrl == null)
						break;
					String file = mSess.mDownUpMgr.getAlbumFile(ci.mUrl, false);
					if (file == null)
						break;
					File avatar = new File(file);
					if (avatar.exists()) {
						// blogmsg.setImg(Utils.readBitMap(file, true));
						Bitmap bitmap = Utils.readBitMap(file, true);
						if (bitmap != null) {
							ci.mBitmap = bitmap;
							Object[] objblog = new Object[] { ci };
							mAvatarHandler.obtainMessage(1, objblog)
									.sendToTarget();

							System.gc();
							bitmap = null;
							break;
						}
					}
					mSess.mDownUpMgr.downloadImg(ci.mUrl, file, 1, true, true,
							new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, true);
									if (bitmap != null) {
										String storagePath = Utils
												.getStoragePath(CheckBigImgActivity.this);
										final File sddir = new File(
												storagePath, Utils.IMAGE_PATH);
										String fileName = taskInfo.mTime + "";
										try {
											Utils.creatBlogFile(
													taskInfo.mFullName, sddir,
													fileName);
											CallInfo ci = (CallInfo) taskInfo.mObj;

											ci.mBitmap = bitmap;
											Object[] obj = new Object[] { ci };
											mAvatarHandler
													.obtainMessage(1, obj)
													.sendToTarget();

										} catch (Exception e) {
											e.printStackTrace();
										}
									}

									System.gc();
									bitmap = null;
								}

							}, ci);
					break;
				}
			}
		};
	}

	protected void loadAlbumImg(long tag, String avatar_url,
			AsyncCallback<Bitmap> callback) {
		CallInfo callinfo = new CallInfo(avatar_url, tag, callback);
		Object[] obj = new Object[] { callinfo };
		mAsynloader.obtainMessage(1, obj).sendToTarget();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {

		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			time_down = System.currentTimeMillis();

			break;
		case MotionEvent.ACTION_MOVE:

			break;
		case MotionEvent.ACTION_UP:
			time_up = System.currentTimeMillis();
			long i = time_up - time_down;
			if (i < 150) {
				this.finish();
			}
		}
		return super.onTouchEvent(event);
	}
}
