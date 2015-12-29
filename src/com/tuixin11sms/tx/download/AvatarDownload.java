package com.tuixin11sms.tx.download;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.HashMap;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 头像下载
 * 
 * @author BrendaZ
 * 
 */
public class AvatarDownload {
	private static final String TAG = "AvatarDownload";
	private static final String TAG_HEAD = BaseMsgRoom.TAG_HEAD;
	private static Handler mPartnerAsynloader;
	public final static int AVATAR_RESULT = 3;
	public final static int DOWN_AVATAR_RESULT_NEARLY = 5;
	public final static int DOWN_AVATAR_RESULT_ALL = 6;

	public AvatarDownload(Context context, SessionManager mSess) {
		// mSess = LoginSessionManager.getManager(context);
		this.mSess = mSess;
		prepairAsyncload();
	}

	public Bitmap getAvatar(String url, long user_id, final View view,
			final Handler avatarHandler) {
		Bitmap bm = getPartnerCachedBitmap(user_id);
		if (bm == null) {
			if (Utils.debug) {
				Log.i(TAG, "loadHeadImage加载头像");
			}
			if (TextUtils.isEmpty(url)) {
				TX ttx = TX.tm.getTx(user_id);
				if (ttx != null) {
					url = ttx.getAvatar_url();
				}
			}
			if (!TextUtils.isEmpty(url)) {
				if (Utils.debug) {
					Log.i(TAG, "个人头像地址：" + url);
				}
				loadHeadImg(url, user_id, new AsyncCallback<Bitmap>() {
					@Override
					public void onFailure(Throwable t, long id) {

					}

					@Override
					public void onSuccess(Bitmap result, long id) {
						if (result == null) {
							return;
						}
						// result = Utils.getRoundedCornerBitmap(result);
						Object[] obj = new Object[] { result, id };

						avatarHandler.obtainMessage(AVATAR_RESULT, obj)
								.sendToTarget();
					}
				});

			}
		}
		return bm;
	}

	// 附近的人头像模式显示图片
	public void loadHeadImg_nearGv(String avatar_url, long partner_id,
			AsyncCallback<Bitmap> callback) {
		CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
		mPartnerAsynloader.obtainMessage(2, callinfo).sendToTarget();
	}

	public void downAvatar(String url, long id, final View view,
			final Handler avatarHandler) {

		loadHeadImg(url, id, new AsyncCallback<Bitmap>() {

			@Override
			public void onFailure(Throwable t, long id) {
			}

			@Override
			public void onSuccess(Bitmap result, long id) {
				Object[] obj = new Object[] { result, id };
				avatarHandler.obtainMessage(DOWN_AVATAR_RESULT_ALL, obj)
						.sendToTarget();
			}
		});
	}
	
	/**
	 * 在个人资料中，强制刷新头像
	 */
	public void downAvatarForUserInfo(String url, long id, final View view,
			final Handler avatarHandler) {

		loadHeadImgForUserInfo(url, id, new AsyncCallback<Bitmap>() {

			@Override
			public void onFailure(Throwable t, long id) {
			}

			@Override
			public void onSuccess(Bitmap result, long id) {
				Object[] obj = new Object[] { result, id };
				avatarHandler.obtainMessage(DOWN_AVATAR_RESULT_ALL, obj)
						.sendToTarget();
			}
		});
	}
	
	

	public void downAvatar_nearly(String url, long id, final View view,
			final Handler avatarHandler) {
		loadHeadImg_nearGv(url, id, new AsyncCallback<Bitmap>() {

			@Override
			public void onFailure(Throwable t, long id) {
			}

			@Override
			public void onSuccess(Bitmap result, long id) {
				Object[] obj = new Object[] { result, id };
				avatarHandler.obtainMessage(DOWN_AVATAR_RESULT_NEARLY, obj)
						.sendToTarget();
			}
		});
	}

	// 个人头像缓存
	public static HashMap<Long, SoftReference<Bitmap>> mPartnerAvatarCache = new HashMap<Long, SoftReference<Bitmap>>();
	public static HashMap<Long, SoftReference<Bitmap>> mPartnerAvatarCache_gv = new HashMap<Long, SoftReference<Bitmap>>();

	public void loadHeadImg(String avatar_url, long partner_id,
			AsyncCallback<Bitmap> callback) {
		if (Utils.debug)
			Log.w(TAG_HEAD, "#####loadHeadImg  发消息加载个人头像 " + partner_id);
		CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
		mPartnerAsynloader.obtainMessage(1, callinfo).sendToTarget();
	}
	public void loadHeadImgForUserInfo(String avatar_url, long partner_id,
			AsyncCallback<Bitmap> callback) {
		if (Utils.debug)
			Log.w(TAG_HEAD, "#####loadHeadImg  发消息加载个人信息头像 " + partner_id);
		CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
		mPartnerAsynloader.obtainMessage(3, callinfo).sendToTarget();
	}

	// 用一个队列管理个人和群头像的回调有问题吗？
	protected Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				CallInfo callinfo = (CallInfo) msg.obj;
				if (Utils.debug)
					Log.w(BaseMsgRoom.TAG_HEAD, "4  开始用传递过来的回调设置 "
							+ callinfo.mUid + " 的头像");
				callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
				break;
			}
			}
		};
	};
	private SessionManager mSess;

	protected void prepairAsyncload() {
		if (mPartnerAsynloader != null) {
			return;
		}
		mPartnerAsynloader = new Handler(
				SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				CallInfo ci;
				switch (msg.what) {
				case 1:
					ci = (CallInfo) msg.obj;
					if (!TextUtils.isEmpty(ci.mUrl)) {
						String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
								ci.mUid, false);
						if (file != null) {
							File avatar = new File(file);
							if (avatar.exists()) {
								Bitmap bitmap = Utils.readBitMap(file, false);
								if (bitmap != null) {
									ci.mBitmap = cachePartnerBitmap(ci.mUid,
											bitmap);
									mAvatarHandler.obtainMessage(1, ci)
											.sendToTarget();
									break;
								}
							}
						}
					}
					int resultCode = mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, false,
							true, new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, false);
									if (bitmap != null) {
										CallInfo ci = (CallInfo) taskInfo.mObj;
										if (Utils.debug) {
											Log.w(BaseMsgRoom.TAG_HEAD, "3 ---"
													+ ci.mUid + " 的头像下载完成");
										}
										ci.mBitmap = cachePartnerBitmap(
												taskInfo.mSrcId, bitmap);
										mAvatarHandler.obtainMessage(1,
												taskInfo.mObj).sendToTarget();
									}
								}
							}, ci);
					if(resultCode == FileTransfer.ERR_URL_INVALID){
						//url非法，使用性别的默认头像
						TX tx = TX.tm.getTx(ci.mUid);
						if(tx!=null){
							ci.mBitmap = cachePartnerBitmap(ci.mUid,
									mSess.getDefaultPartnerAvatar(tx.getSex()));
							mAvatarHandler.obtainMessage(1,ci).sendToTarget();
						}
					}
					
					break;
				case 2:
					ci = (CallInfo) msg.obj;
					if (!TextUtils.isEmpty(ci.mUrl)) {
						String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
								ci.mUid, false);
						if (file != null) {
							File avatar = new File(file);
							if (avatar.exists()) {
								Bitmap bitmap = Utils.readBitMap(file, false);
								if (bitmap != null) {
									ci.mBitmap = cachePartnerBitmap_nearlyGv(
											ci.mUid, bitmap);
									mAvatarHandler.obtainMessage(1, ci)
											.sendToTarget();
									break;
								}
							}
						}
					}
					mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, false,
							true, new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, false);
									if (bitmap != null) {
										CallInfo ci = (CallInfo) taskInfo.mObj;
										ci.mBitmap = cachePartnerBitmap_nearlyGv(
												taskInfo.mSrcId, bitmap);
										mAvatarHandler.obtainMessage(1,
												taskInfo.mObj).sendToTarget();
									}
								}
							}, ci);
					break;
				case 3:
					ci = (CallInfo) msg.obj;
					int resultCode2 = mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, false,
							true, new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, false);
									if (bitmap != null) {
										CallInfo ci = (CallInfo) taskInfo.mObj;
										if (Utils.debug) {
											Log.w(BaseMsgRoom.TAG_HEAD, "3 ---"
													+ ci.mUid + " 的头像下载完成");
										}
										ci.mBitmap = cachePartnerBitmap(
												taskInfo.mSrcId, bitmap);
										mAvatarHandler.obtainMessage(1,
												taskInfo.mObj).sendToTarget();
									}
								}
							}, ci);
					if(resultCode2 == FileTransfer.ERR_URL_INVALID){
						//url非法，使用性别的默认头像
						TX tx = TX.tm.getTx(ci.mUid);
						if(tx!=null){
							ci.mBitmap = cachePartnerBitmap(ci.mUid,
									mSess.getDefaultPartnerAvatar(tx.getSex()));
							mAvatarHandler.obtainMessage(1,ci).sendToTarget();
						}
					}
					
					break;
				}
			}
		};
	}

	public Bitmap cachePartnerBitmap(long tx_partner_id, Bitmap bitmap) {
		if (Utils.debug)
			Log.w(TAG_HEAD, "缓存cachePartnerBitmap  " + tx_partner_id + "  的头像");
		synchronized (mPartnerAvatarCache) {
			bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
			mPartnerAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
					bitmap));
			return bitmap;
		}
	}

	// 为默认头像设置
	public void cachePartnerBitmapDir(long tx_partner_id, Bitmap bitmap) {

		if (Utils.debug)
			Log.w(TAG_HEAD, "默认头像缓存缓存cachePartnerBitmapDir  " + tx_partner_id
					+ "  的头像");
		synchronized (mPartnerAvatarCache) {
			mPartnerAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
					bitmap));
		}
	}

	// 部分adapter中要用BaseActivity对象调用该方法，故需要用public
	public Bitmap getPartnerCachedBitmap(long tx_partner_id) {
		// 个人头像
		synchronized (mPartnerAvatarCache) {
			if (Utils.debug)
				Log.w(TAG_HEAD, "获取头像getPartnerCachedBitmap  " + tx_partner_id
						+ "  的头像");
			SoftReference<Bitmap> soft = mPartnerAvatarCache.get(tx_partner_id);
			return (soft != null) ? soft.get() : null;
		}
	}

	public Bitmap getPartnerCachedBitmap_nearltGv(long tx_partner_id) {
		// 个人头像
		synchronized (mPartnerAvatarCache_gv) {
			SoftReference<Bitmap> soft = mPartnerAvatarCache_gv
					.get(tx_partner_id);
			return (soft != null) ? soft.get() : null;
		}
	}

	public Bitmap cachePartnerBitmap_nearlyGv(long tx_partner_id, Bitmap bitmap) {
		synchronized (mPartnerAvatarCache_gv) {
			// bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
			mPartnerAvatarCache_gv.put(tx_partner_id,
					new SoftReference<Bitmap>(bitmap));
			return bitmap;
		}
	}

	/** 删除TX头像缓存,返回值为是否删除 */
	public static boolean removeTXHeadImgCache(long partner_id) {
		SoftReference<Bitmap> removedImg = mPartnerAvatarCache
				.remove(partner_id);
		if (removedImg != null) {
			removedImg.clear();
		}
		return removedImg != null;
	}

	public static boolean removeTXHeadImgCache_gv() {

		if (mPartnerAvatarCache_gv != null) {
			mPartnerAvatarCache_gv.clear();
			return true;
		}

		return false;
	}

	/** 被赞消息页面获取用户头像 */
	public Bitmap getHeadIcon(final long partner_id,
			final AsyncCallback<Bitmap> callback) {
		Bitmap bm = getPartnerCachedBitmap(partner_id);
		if (bm == null) {
			// 缓存中没有此头像
			bm = mSess.getDefaultPartnerAvatar(TX.FEMAL_SEX);// 先给一个女性默认的头像

			TX tx = TX.tm.getTx(partner_id);
			if (tx != null) {
				loadHeadImg(tx.avatar_url, tx.partner_id, callback);
			} else {
				// 本地没有此tx
				mSess.getSocketHelper().getUserDetail(partner_id,
						new AsyncCallback() {

							@Override
							public void onFailure(Throwable t, long id) {

							}

							@Override
							public void onSuccess(Object result, long id) {
								// 获取到了用户的详细信息
								TX txt = TX.tm.getTx(partner_id);
								if (txt != null) {
									loadHeadImg(txt.avatar_url, txt.partner_id,
											callback);
								}
							}
						});
			}
		}

		return bm;

	}

}
