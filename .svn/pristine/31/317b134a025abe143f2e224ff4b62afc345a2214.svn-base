package com.tuixin11sms.tx.engine;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 瞬间数据及逻辑处理
 * 
 * @author BrendaZ
 * 
 */
public class BlogOpea {

	public static final int RECORD_PAUSE = 0;
	public static final int RECORD_PLAY = 1;

	public static final int SEEDBAR_PROCESS = 3;
	public static final int SERVER_SUCCESS_HEAD = 4;
	public static final int SERVER_SUCCESS_LIST = 5;
	public static final int SERVER_DEL_BLOG = 6;
	public static final int SERVER_LIKE_BLOG = 7;
	public static final int RECORD_PLAY_FINISH = 11;
	public static final int SERVER_USER_LIST = 12;
	public static final int RECORD_DOWNLOAD_FINISH = 13;
	public static final int SERVER_ERROR = 17;

	static final String TAG = "BlogOpea";

	private Handler handler;
	private BlogMsg blogMsg;
	public boolean flag;
	public SessionManager mSess;

	public BlogOpea(SessionManager sm) {
		mSess = sm;
		initAnim();
		flag = false;
		mAudioRecPlayer = ClientManager.getRecordManager();
		socketHelper = mSess.getSocketHelper();
	}

	public void setHandler(Handler handler) {
		this.handler = handler;
	}

	// 获取数据
	public void getData(long uid, long beginMid) {
		if (socketHelper == null) {
			socketHelper = SocketHelper.getSocketHelper(mSess);
		}
		if (beginMid == 0) {
			socketHelper.sendGetBlogMsg(uid);
		} else {
			socketHelper.sendGetBlogMsgs(uid, beginMid + "", 10);
		}

		setBlogMsgCallback(callback, uid);
	}

	// 服务器返回数据接口回调
	public ReceiveCallback getcallback() {
		return callback;
	}

	public ReceiveCallback callback = new ReceiveCallback() {

		private List<BlogMsg> list = new ArrayList<BlogMsg>();
		private BlogMsg mblogmsg;
		private int size;

		@Override
		public void receiveList(BlogMsg msg) {

			ArrayList<BlogMsg> blogList = new ArrayList<BlogMsg>();

			if (mblogmsg == null)
				mblogmsg = new BlogMsg();
			mblogmsg.setUid(msg.getUid());

			if (msg != null && msg.getBlogMsgs() != null
					&& msg.getBlogMsgs().size() > 0) {

				if (list != null && list.size() > 0) {
					list.clear();
				}

				// 查找数据库，查出的是数据库中的所有
				List<BlogMsg> blogDataList = mSess.getBlogDao()
						.findBlogMsgByUid(msg.getUid());

				size = msg.getBlogMsgs().size();
				for (BlogMsg blog : msg.getBlogMsgs()) {
					if (blog.isIsdel()) {
						size--;
					}
				}
				
				if (blogDataList != null && blogDataList.size() != 0) {
					List<Long> local_mids = new ArrayList<Long>();

					if (msg.getBlogMsgs().get(msg.getBlogMsgs().size() - 1)
							.getMid() < blogDataList.get(
							blogDataList.size() - 1).getMid()
							|| msg.getBlogMsgs().get(0).getMid() > blogDataList
									.get(0).getMid()) {
						// //说明有新的瞬间
						if (size == 0) {
							// 说明新的瞬间全部是被OP删除的瞬间，不用获取详情了
							handler.obtainMessage(SERVER_SUCCESS_LIST, blogList)
									.sendToTarget();
						} else {
							for (BlogMsg bloginfo : msg.getBlogMsgs()) {
								if (!bloginfo.isIsdel()) {
									mSess.getSocketHelper().sendGetBlogInfo(
											bloginfo.getMid() + "");
								}
							}
						}
						return;
					}

					for (BlogMsg blog : blogDataList) {
						local_mids.add(blog.getMid());
					}

					// 遍历服务器返回的瞬间集合
					for (BlogMsg blog : msg.getBlogMsgs()) {

						if (local_mids.contains(blog.getMid())) {
							// 说明数据库中有，遍历数据库的瞬间，更新
							for (BlogMsg mblog : blogDataList) {
								if (mblog.getMid() == blog.getMid()) {
									if (!blog.isIsdel()) {
										mblog.setIdlist(blog.getIdlist());
										mblog.setLikednum(blog.getLikednum());
										// mSess.getBlogDao().updateIdlist(blog);//
										// 更新数据库及缓存
										mblog.setUid(mblogmsg.getUid());
										mblog.setTime(blog.getTime());
										mSess.getBlogDao().update(mblog);// 加入到数据库及缓存
										blogList.add(mblog);
									} else {
										if (Utils.debug) {
											Log.i("bobo",
													"===========这条删除了=========="
															+ blog.getMid());
										}
									}
								}
							}
						} else {
							// 说明数据库中木有，要么删了，要么没拉到呢。。。怎么办
							if (!blog.isIsdel()) {
								mSess.getSocketHelper().sendGetBlogInfo(
										blog.getMid() + "");
							}
						}
					}
					handler.obtainMessage(SERVER_SUCCESS_LIST, blogList)
							.sendToTarget();
				} else {
					if (msg.getBlogMsgs().size() == 0) {
						handler.obtainMessage(SERVER_SUCCESS_LIST,
								msg.getBlogMsgs()).sendToTarget();
					} else {
						if (size == 0) {
							handler.obtainMessage(SERVER_SUCCESS_LIST, blogList)
									.sendToTarget();
							return;
						}
						for (BlogMsg blog : msg.getBlogMsgs()) {
							if (!blog.isIsdel()) {
								mSess.getSocketHelper().sendGetBlogInfo(
										blog.getMid() + "");
							}
						}
					}
				}

			} else {
				handler.obtainMessage(SERVER_ERROR, "没有更多瞬间了").sendToTarget();
			}
		}

		@Override
		public void receiveHead(BlogMsg msg) {
			// 获取了瞬间
			// 将头信息存入个人数据库
			if (mblogmsg == null)
				mblogmsg = new BlogMsg();
			mblogmsg.setUid(msg.getUid());

			if (msg.getBlogNums() == 0) {
				handler.obtainMessage(SERVER_SUCCESS_HEAD, msg).sendToTarget();
				return;
			}

			mblogmsg.setBlogNums(msg.getBlogNums());
			String blog_head = msg.getBlogNums() + "," + msg.getAccessNums()
					+ "," + msg.getLikedNums();
			TX tx = new TX();
			tx.partner_id = msg.getUid();
			tx.blog_head_msg = blog_head;

			if (msg.getUid() == TX.tm.getUserID()) {
				mSess.mPrefMeme.blogmsg.setVal(blog_head);
				TX.tm.reloadTXMe();
			}
			ContentValues values = new ContentValues();
			values.put(TxDB.Tx.TX_ID, tx.partner_id);
			values.put(TxDB.Tx.BLOG_INFOR, tx.blog_head_msg);
			TX updateTx = TX.tm.updateTx(tx.partner_id, values);
			if (updateTx == null) {
				// 更新失败，执行添加tx操作
				TX.tm.addTx(tx);
			}
			// 批量获取瞬间
			mSess.getSocketHelper().sendGetBlogMsgs(uid, 0 + "", 10);

			handler.obtainMessage(SERVER_SUCCESS_HEAD, msg).sendToTarget();

		}

		@Override
		public void receiveDel(Object[] obj) {
			// 刪除瞬間返回

			int d = (Integer) obj[0];

			String s = null;
			switch (d) {
			case 0:
				s = "删除成功";
				BlogMsg msg = (BlogMsg) obj[1];
				mSess.getBlogDao().delete(msg.getMid());
				break;
			case 1:
				s = "你没有这个瞬间";
				break;
			case 2:
				s = "你没有权限";
				break;
			case 3:
				s = "OP已经删除";
				break;
			case 4:
				s = "删除失败";
				break;
			}
			Object[] server_msg = new Object[] { s, (BlogMsg) obj[1] };
			handler.obtainMessage(SERVER_DEL_BLOG, server_msg).sendToTarget();
		}

		@Override
		public void receiveLike(Object[] obj) {
			// 喜欢瞬间返回
			int d = (Integer) obj[0];

			String s = null;
			switch (d) {
			case 0:
				s = "操作成功";
				break;
			case 1:
				s = "没有这个瞬间";
				break;
			case 2:
				s = "瞬间被OP删除";
				break;
			case 3:
				s = "操作失败";
				break;
			}
			Object[] server_msg = new Object[] { s, (BlogMsg) obj[1] };
			handler.obtainMessage(SERVER_LIKE_BLOG, server_msg).sendToTarget();
		}

		@Override
		public void receiveUserList(List<TX> userlist) {
			// 批量获取个人资料返回
			HashMap<Long, TX> usermap = new HashMap<Long, TX>();
			if (userlist != null && userlist.size() > 0) {
				for (TX tx : userlist) {
					usermap.put(tx.getPartner_id(), tx);
				}
			}
			handler.obtainMessage(SERVER_USER_LIST, usermap).sendToTarget();
		}

		@Override
		public void receiveListInfor(BlogMsg msg) {
			// 服务器返回瞬间每条信息

			if (!msg.isIsdel()) {
				if (list == null) {
					list = new ArrayList<BlogMsg>();
					list.add(msg);
				} else {
					list.add(msg);
				}
				msg.setUid(mblogmsg.getUid());
				mSess.getBlogDao().update(msg);// 加入到数据库及缓存
			}
			if (list.size() == size) {
				handler.obtainMessage(SERVER_SUCCESS_LIST, list).sendToTarget();
			}
		}

		@Override
		public void receiveError(String errormsg) {
			handler.obtainMessage(SERVER_ERROR, errormsg).sendToTarget();
		}
	};

	private long uid;

	public void setBlogMsgCallback(ReceiveCallback callback, long uid) {
		this.callback = callback;
		this.uid = uid;
	}

	public interface ReceiveCallback {
		public void receiveList(BlogMsg msg);

		public void receiveListInfor(BlogMsg msg);

		public void receiveHead(BlogMsg msg);

		public void receiveDel(Object[] obj);

		public void receiveLike(Object[] obj);

		public void receiveUserList(List<TX> userlist);

		public void receiveError(String errormsg);

	}

	// 删除瞬间
	public void delete(BlogMsg msg, long uid) {
		if (msg == null) {
			return;
		}
		socketHelper.sendDelBlogInfo(msg.getMid() + "", uid);
	}

	/*************** 处理音频相关 **************/
	protected ClientManager mAudioRecPlayer;
	private long tag;

	/**
	 * 
	 * 播放音频
	 * 
	 * @param txmsg
	 */
	public void playAudioRecord(TXMessage txmsg, long tag) {
		mAudioRecPlayer.startToPlay(txmsg, recordListener);
		rotateAnimation.start();
		flag = true;
		this.tag = tag;
		aduioTime = (int) txmsg.audio_times * 1000;
		int speed = (int) (txmsg.audio_times * 1000 / 100);
		DelayThread delaythread = new DelayThread(speed, tag);
		delaythread.start();
		if (Utils.debug) {
			Log.i("bobo", "playAudioRecord-->" + txmsg.msg_path + "-------"
					+ txmsg.audio_times + "-------==" + tag);
		}
		txmsg.PlayAudio = RECORD_PLAY;
	}

	RecordListener recordListener = new RecordListener() {

		@Override
		public void uploadFinish(TXMessage txMsg) {

			if (mAudioRecPlayer != null && txMsg != null) {

			}
		}

		@Override
		public void recordError(int errcode) {
			// creatUnInitRecordInfo();
		}

		@Override
		public void onPlayFinish(TXMessage txMsg) {
			if (Utils.debug) {
				Log.i("bobo", "playAudioRecord-->okkkkkkkkkkkk");
			}
			endAduMsg = txMsg;
			if (milliseconds >= aduioTime || milliseconds == 0) {
				// 当进度总和大于播放时间长度的时候说明已经播完，否则没有进度没有走完，继续走进度
				// 当正常播完后，进度总和置0的时候说明已经播完，并且进度走完
				flag = false;
				handler.obtainMessage(RECORD_PLAY_FINISH, txMsg).sendToTarget();
			}
		}

		@Override
		public void doRecordVolume(float volume) {
		}

		@Override
		public void deviceInitOK() {

		}
	};
	private Animation rotateAnimation;
	private SocketHelper socketHelper;
	private TXMessage endAduMsg;

	// 停止播放录音
	public void stopPlayAudioRecord() {
		mAudioRecPlayer.stopPlay();
		flag = false;
		TXMessage msg = mAudioRecPlayer.getTxMsg();
		if (msg != null)
			msg.PlayAudio = RECORD_PAUSE;
	}

	public Animation getAnim() {
		return rotateAnimation;
	}

	private void initAnim() {
		if (rotateAnimation == null) {
			rotateAnimation = new RotateAnimation(0f, 359f,
					Animation.RELATIVE_TO_SELF, 0.5f,
					Animation.RELATIVE_TO_SELF, 0.5f);
			rotateAnimation.setFillAfter(true);
			rotateAnimation.setRepeatCount(-1);
			rotateAnimation.setDuration(10 * 1000);
		}
	}

	int milliseconds;
	class DelayThread extends Thread {

		long id;
		int speed;

		public DelayThread(int speed, long tag) {
			this.speed = speed;
			id = tag;
		}

		public void run() {
			while (flag) {
				milliseconds += speed;
				if (tag == id) {
					Object[] obj = new Object[] { id, milliseconds };
					handler.obtainMessage(SEEDBAR_PROCESS, obj).sendToTarget();
					if (milliseconds >= aduioTime) {
						flag = false;
						handler.obtainMessage(RECORD_PLAY_FINISH, endAduMsg)
								.sendToTarget();
					}
				} else {
					break;
				}
				try {
					sleep(speed);
					// 设置音乐进度读取频率
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			milliseconds = 0;
		}
	}

	public void DownAduioScoket(BlogMsg blogMsg) {
		if (Utils.debug)
			Log.i("bobo", "开始下载音频：blogMsg.msg_id:");
		String audioFilePath = mSess.mDownUpMgr.getAudioFile(
				blogMsg.getAduUrl(), blogMsg.getMid());
		mSess.mDownUpMgr.downloadAudio(blogMsg.getAduUrl(), audioFilePath, 0,
				false, mAudioDownloadCallback, blogMsg);
	}

	DownUploadListner mAudioDownloadCallback = new DownUploadListner() {
		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			BlogMsg blogMsg = (BlogMsg) taskInfo.mObj;
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			BlogMsg blogMsg = (BlogMsg) taskInfo.mObj;
			if (blogMsg != null) {

				blogMsg.setAduPath(taskInfo.mFullName);
				mSess.getBlogDao().update(blogMsg);
				handler.obtainMessage(RECORD_DOWNLOAD_FINISH, blogMsg)
						.sendToTarget();
			}
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {

		}
	};
	private int aduioTime;

	/**
	 * 处理日期
	 * 
	 * @param date
	 * @return
	 */
	public String dealDate(long date) {
		Date nowTime;
		if (("" + date).length() >= 13 && ("" + date).length() < 16) {
			nowTime = new Date(date);
		} else if (("" + date).length() >= 16) {
			nowTime = new Date(date / 1000);
		} else {
			nowTime = new Date(date * 1000);
		}

		SimpleDateFormat time = new SimpleDateFormat("MM-dd HH:mm");
		return time.format(nowTime);
	}

	/**
	 * 是否喜欢瞬间
	 * 
	 * @param msg
	 *            瞬间
	 * @param flag
	 *            是否喜欢
	 * @param uid
	 *            发送喜欢的人id
	 */
	public void islikeBlog(BlogMsg msg, boolean flag, long uid) {
		if (flag) {
			// 喜欢
			socketHelper.sendLikedBlogInfo(msg.getMid() + "", uid, 0);
		}
	}

	/*************** 处理图片相关 **************/
	Map<Long, SoftReference<Bitmap>> bitmaps = new HashMap<Long, SoftReference<Bitmap>>();
	Bitmap readBitMap;

	// 取缓存中的照片
	public Bitmap getcatchImg(long mid) {
		SoftReference<Bitmap> soft = bitmaps.get(mid);
		return (soft != null) ? soft.get() : null;
	}

	public void cachImg(long mid, Bitmap img) {
		bitmaps.put(mid, new SoftReference<Bitmap>(img));
	}

	public Bitmap dealBitmap(Bitmap bitmap) {

		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();

		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

		int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
		int retY = w > h ? 0 : (h - w) / 2;

		float scaleWidth = ((float) Utils.SreenW) / wh;
		float scaleHeight = ((float) Utils.SreenW) / wh;
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);

		// 下面这句是关键

		try {
			Bitmap createBitmap = Bitmap.createBitmap(bitmap, retX, retY, wh,
					wh, matrix, true);
			return createBitmap;
		} catch (OutOfMemoryError e) {
			System.gc();
		}

		return null;
	}

	// resolution 分辨率 例如640*640
	public Bitmap getBitmap(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}

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
				Log.e(TAG, "加载图片为指定分辨率OOM异常了", err);
			}
			return null;
		}
	}

	// 按图片大小缩放图片
	public static Bitmap fitSizeImg(String path, int scaleNum)
			throws OutOfMemoryError {
		if (TextUtils.isEmpty(path))
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 300 * 1024) { // 0-0.3M
			opts.inSampleSize = 1;
		} else if (file.length() < 800 * 1024) { // 0.3M-0.8M
			opts.inSampleSize = 2;
		} else if (file.length() < 1024 * 1024) { // 0.8M-1M
			opts.inSampleSize = 3;
		} else if (file.length() < 2 * 1024 * 1024) { // 1M-2M
			opts.inSampleSize = 4;
		} else if (file.length() < 5 * 1024 * 1024) { // 2M-5M
			opts.inSampleSize = 8;
		} else if (file.length() < 10 * 1024 * 1024) { // 5M-10M
			opts.inSampleSize = 10;
		} else {
			opts.inSampleSize = 50;
		}
		if (scaleNum > 0) {
			opts.inSampleSize = opts.inSampleSize * scaleNum;
		}
		return BitmapFactory.decodeFile(file.getPath(), opts);
	}

	/**
	 * 批量获取个人资料
	 * 
	 * @param idlist
	 * @param fields
	 */
	public void sendUserList(List<Long> idlist, List<String> fields) {

		if (idlist != null && idlist.size() > 0) {

			if (idlist.size() > 10) {
				List<Long> idlist_10 = new ArrayList<Long>();
				for (int i = 0; i < 9; i++) {
					idlist_10.add(idlist.get(i));
				}
				mSess.getSocketHelper().sendUserInfoList(idlist_10, fields);
				idlist.removeAll(idlist_10);
				sendUserList(idlist, fields);
			} else {
				mSess.getSocketHelper().sendUserInfoList(idlist, fields);
			}
		}
	}

	/**
	 * 检查瞬间样式
	 * 
	 * @param msg
	 * @return
	 */
	public BlogMsg checkBlogType(BlogMsg msg) {
		if (!Utils.isNull(msg.getImgUrl())) {
			if (!Utils.isNull(msg.getAduUrl())
					|| !Utils.isNull(msg.getAduPath())) {
				msg.setType(BlogMsg.IMG_AUDIO);
			} else {
				msg.setType(BlogMsg.IMG);
			}
			return msg;
		}
		if (!Utils.isNull(msg.getAduUrl()) || !Utils.isNull(msg.getAduPath())) {
			msg.setType(BlogMsg.AUDIO);
			return msg;
		}
		msg.setType(BlogMsg.MSG);
		return msg;
	}

	public void recycle() {
		if (readBitMap != null) {
			if (!readBitMap.isRecycled()) {
				readBitMap.recycle();
			}
			readBitMap = null;
		}

		bitmaps.clear();

		blogMsg = null;
	}

}
