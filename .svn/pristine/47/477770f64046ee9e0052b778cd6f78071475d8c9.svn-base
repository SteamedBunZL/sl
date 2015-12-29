package com.tuixin11sms.tx.group;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;

import org.json.JSONException;

import u.aly.ct;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.activity.GifPackageDownActivity;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.core.MyPopupWindow;
import com.tuixin11sms.tx.core.MyPopupWindow.newAdapter;
import com.tuixin11sms.tx.gif.GifDownActivity;
import com.tuixin11sms.tx.gif.MD5FileUtil;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.TSLogonPara;
import com.tuixin11sms.tx.task.FileTransfer.TSServerProp;
import com.tuixin11sms.tx.utils.Utils;

import android.R.integer;
import android.R.mipmap;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

public class GifTransfer {
	private static final String TAG = "GifTransfer";
	/** 登录认证 ************************************************************/
	/** 表情通讯协议版本号 */
	public static final int GS_VERSION = 1;
	/** 登陆认证请求 */
	public static final int GS_LOGIN = 1;
	/** 登陆认证请求应答 */
	public static final int GS_LOGIN_RSP = 2;

	public static final int DUERR_CANCELLED = 4;

	/** 表情包上传 ************************************************************/
	/** 表情包上传请求 */
	private static final int GIF_PACKAGE_START_UPLOAD = 50;
	private static final int GS_START_UPLOAD_RSP = 51;
	private static final int GS_UPLOAD_DATA = 52;
	private static final int GS_UPLOAD_DATA_RSP = 53;
	private static final int GS_UPLOAD_FINISH = 54;
	private static final int GS_UPLOAD_FINISH_RSP = 55;

	private static final int GIF_PACKAGE_START_DOWNLOAD = 100;
	private static final int GIF_PACKAGE_START_DOWNLOAD_RSP = 101;
	private static final int GIF_PACKAGE_DOWNLOAD = 102;
	private static final int GIF_PACKAGE_DOWNLOAD_RSP = 103;
	private static final int GIF_PACKAGE_DOWNLOAD_FINISH = 104;

	private static final int GIF_START_DOWNLOAD = 150;
	private static final int GIF_START_DOWNLOAD_RSP = 151;
	private static final int GIF_DOWNLOAD_DATA = 152;
	private static final int GIF_DOWNLOAD_FINISH = 153;

	private static final int GIF_LIST_START_DOWNLOAD = 200;
	private static final int GIF_LIST_START_DOWNLOAD_RSP = 201;
	private static final int GIF_DETAIL_START_DOWNLOAD = 202;
	private static final int GIF_DETAIL_START_DOWNLOAD_RSP = 203;

	private static final int GIF_PIC_LIST_START_DOWNLOAD = 204;
	private static final int GIF_PIC_LIST_START_DOWNLOAD_RSP = 205;
	// 0继续下载
	private final int DOWNLOAD_CONTINUE = 0;
	// 1移动指针
	private final int DOWNLOAD_MOVE_POINT = 1;
	// 2:中止（服务器会补发Prot454，结果码为1）
	private final int DOWNLOAD_ABORT = 2;

	public static final int DOWNERR_IOEXCEPTION = 1;
	public static final int DUERR_SERVERREJECT = 2;
	public static final int DUERR_INVALIDPARA = 3;
	/** 断网重连 */
	public static final int TASK_RES_RESET_NET = 2;
	/** 下载文件超时时间20s */
	public static final int DOWN_FILE_TIME_OUT = 20 * 1000;

	public static final int STATUS_SUCCESS = 0;
	public static final int STATUS_NOT_FOUND_GIF_PACKAGE = 22;
	public static final int STATUS_NOT_FOUND_GIF = 31;
	GifTaskQueue mTaskIdQueue;
	// 上传队列
	GifTaskQueue mUpTaskQueue;
	// 下载队列
	GifTaskQueue mDownTaskQueue;
	// 页面队列
	GifTaskQueue mPageTaskQueue;

	// 单个表情的队列

	GifTaskQueue mGifTaskQueue;

	GifTransfer(GSServerProp gsp, GSLogonPara para) {
		mServerProp = gsp;
		mLogonPara = para;
		// 初始化上传队列

		GifTaskExecutor[] taskIdExecutors = { new GifTaskExecutor() };
		GifTaskExecutor[] upExecutors = { new GifTaskExecutor() };
		GifTaskExecutor[] downExecutors = { new GifTaskExecutor() };
		GifTaskExecutor[] pageExecutors = { new GifTaskExecutor() };
		GifTaskExecutor[] gifExecutors = { new GifTaskExecutor() };
		mTaskIdQueue = new GifTaskQueue(taskIdExecutors);
		mUpTaskQueue = new GifTaskQueue(upExecutors);
		mDownTaskQueue = new GifTaskQueue(downExecutors);
		mPageTaskQueue = new GifTaskQueue(pageExecutors);
		mGifTaskQueue = new GifTaskQueue(gifExecutors);

	}

	public int downGifPackageDetail(int id, GifDownUploadListner listner,
			int iQueue) {
		GifDetailDownloadTask task = new GifDetailDownloadTask(id, listner);
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mPageTaskQueue.AddTask(task, iQueue, false);
		mPageTaskQueue.startExecutors();
		return 0;
	}

	public int downGifList(int page, GifDownUploadListner listner, int iQueue) {
		GifListDownloadTask task = new GifListDownloadTask(page, listner);
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mPageTaskQueue.AddTask(task, iQueue, false);
		mPageTaskQueue.startExecutors();
		return 0;
	}

	public int downGifPicList(int id, int page, GifDownUploadListner listner,
			int iQueue) {
		GifPicListDownloadTask task = new GifPicListDownloadTask(id, page,
				listner);
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mPageTaskQueue.AddTask(task, iQueue, false);
		mPageTaskQueue.startExecutors();
		return 0;
	}

	public int downGifPic(int id, String md5, GifDownUploadListner listner,
			int iQueue, Object obj) {
		// 用于测试 name /sdcard/test1/
		String path = FileManager.getShenLiaoGifOppositePath();
		GifDownloadTask task = new GifDownloadTask(path + id + "_" + md5
				+ ".gif", 10000, false, md5, id, listner);
		task.mObj = obj;

		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mGifTaskQueue.AddTask(task, iQueue, false);
		if (Utils.debug) {
			Log.d(TAG, "2015/3/16 md5:" + md5 + "已经加入到任务中了");
		}
		mGifTaskQueue.startExecutors();
		return 0;
	}

	public int downGifPic2(int id, String md5, GifDownUploadListner listner,
			int iQueue, Object obj) {
		// 用于测试 name /sdcard/test1/
		String path = FileManager.getShenLiaoGifPackagePath();
		File file = new File(path);
		if (!file.exists()) {
			file.mkdir();
		}
		GifDownloadTask task = new GifDownloadTask(path + id + ".jpg", 10000,
				false, md5, id, listner);
		task.mObj = obj;

		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mGifTaskQueue.AddTask(task, iQueue, false);
		if (Utils.debug) {
			Log.d(TAG, "2015/3/16 md5:" + md5 + "已经加入到任务中了");
		}
		mGifTaskQueue.startExecutors();
		return 0;
	}

	/**
	 * 得到gif包本地的存储地址
	 * 
	 * @param name
	 *            gif包的名字
	 * @return
	 */
	String general_path = "shenliao/gif_package";

	public String getGifPackageLocalPath(String name) {
		if (!Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			return null;
		}
		String path = Environment.getExternalStorageDirectory().getPath();
		File file = new File(path + "/" + general_path);
		if (!file.exists()) {
			file.mkdirs();
		}
		path = path + "/" + general_path + "/" + name;
		return path;
	}

	public int taskGetGifPackageId(String file, int iType, int iQueue,
			String fileUrl, GifDownUploadListner listner, boolean bNow) {
		file = FileManager.getShenLiaoGifPackagePath() + file;
		GTaskForId task = new GTaskForId(file, iType, fileUrl, listner);
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mTaskIdQueue.AddTask(task, iQueue, bNow);
			mTaskIdQueue.startExecutors();
		} catch (IOException e) {
			e.printStackTrace();
			listner.onError(task, DUERR_INVALIDPARA, null);
		}
		return 0;

	}

	public int downGifPackage(String md5, String name, int id,
			GifDownUploadListner listner, int iQueue) {
		GIFPackageDownloadTask task = new GIFPackageDownloadTask(md5, null,
				getGifPackageLocalPath(FileManager.createSaveGifPackageName(id,
						name, md5)), 10001, false, listner, id);
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownTaskQueue.AddTask(task, iQueue, false);
		mDownTaskQueue.startExecutors();
		return 0;
	}

	public int uploadGifBag(String file, int iType, int iQueue, String fileUrl,
			GifDownUploadListner listner, boolean bNow) {
		GUploadTask task = new GUploadTask(file, iType, fileUrl, listner);
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpTaskQueue.AddTask(task, iQueue, bNow);
			mUpTaskQueue.startExecutors();
		} catch (Exception e) {
			listner.onError(task, DUERR_INVALIDPARA, null);
		}
		return 0;
	}

	// 1.先写服务器的登录
	// ① GS服务器登录的信息
	/** GS服务器登录信息类，TOKEN由神聊提供 */
	public static class GSLogonPara {
		public int uid;// 推信号uid
		public int cver = Utils.appver;// 客户端版本
		public int ver = GS_VERSION;// 表情通讯协议版本号
		public int osid = 600; // 固定
		public byte[] tokenBytes = null;// "8d1b472d70bc9ead85fb3c0a68408113".getBytes();
										// 用setUserToken方法传递的token
		public final Object mNetNotifier = new Object();

		// 登录成功后调此方法
		public void setUserToken(int uid, byte[] token) {
			this.uid = uid;
			tokenBytes = token;
		}

		@Override
		public String toString() {
			return "GSLogonPara [uid=" + uid + ", cver=" + cver + ", ver="
					+ ver + ", osid=" + osid + ", tokenBytes="
					+ Arrays.toString(tokenBytes) + "]";
		}

	}

	// ② GS服务器地址和端口类
	public static class GSServerProp {
		public String mServer;
		public int mPort;
		public int mTimeout;
		public boolean bSvrValid = false;

		public GSServerProp() {
			mServer = "";
			mPort = 0;
			mTimeout = 20000;
		}

		public GSServerProp(String svr, int port, int timeout) {
			mServer = svr;
			mPort = port;
			mTimeout = timeout;
		}
	}

	// ③读取缓冲区，输入/输出流
	static class GSProtContext {
		public final GSServerProp gsSvr;
		public final GSLogonPara mLogonPara;
		public DataInputStream mtIS;
		DataInputStream mDIS;
		DataOutputStream mDOS;
		int mRecvLen, mRecvMT;
		int mSendLen, mSendMT;
		public byte[] mSendBuff;
		public byte[] mRecvBuff;

		GSProtContext(GSLogonPara para, GSServerProp svr) {
			gsSvr = svr;
			mLogonPara = para;
		}

		public void prepSend() {
			mSendLen = 0;
			mSendMT = 0;
		}

		public void prepRecv() throws IOException {
			if (mRecvLen > 0x40000)
				throw new IOException("Invalid data format");
			if (mRecvBuff == null || mRecvLen > mRecvBuff.length)
				mRecvBuff = new byte[mRecvLen];

			Log.i(TAG, "available : " + mDIS.available());
			mDIS.readFully(mRecvBuff, 0, mRecvLen);
			if (Utils.debug) {
				Log.i(TAG, "BUFF : " + mRecvBuff.length);
			}
			mtIS = new DataInputStream(new ByteArrayInputStream(mRecvBuff, 0,
					mRecvLen));
		}

		public int prepRecvFor102(int pos) throws IOException {
			if (mRecvLen > 0x40000) {
				throw new IOException("Invalid data format");
			}
			if (mRecvBuff == null || mRecvLen > mRecvBuff.length) {
				mRecvBuff = new byte[mRecvLen];
			}
			int len = mDIS.available();
			// 8700
			// if (len > mRecvLen) {
			// len = mRecvLen;
			// }
			// 3744 4996
			if (pos + len > mRecvLen) {
				len = mRecvLen - pos;
			}
			if (len > 0) {
				if (Utils.debug) {
					Log.i(TAG, "this time len :" + len);
				}
				mDIS.readFully(mRecvBuff, pos, len);
				mtIS = new DataInputStream(new ByteArrayInputStream(mRecvBuff,
						0, len));
				return len;
			}
			return 0;

		}

		public void prepRecvFor102Test() throws IOException {
			if (mRecvLen > 0x40000)
				throw new IOException("Invalid data format");
			if (mRecvBuff == null || mRecvLen > mRecvBuff.length)
				mRecvBuff = new byte[mRecvLen];
			AsynRead(mDIS, mRecvLen, mRecvBuff);
			mtIS = new DataInputStream(new ByteArrayInputStream(mRecvBuff, 0,
					mRecvLen));
		}

		public byte[] getSendBuff(int len) throws IOException {
			if (len > 0x8000)
				throw new IOException("Invalid data format");
			if (mSendBuff == null || len > mSendBuff.length)
				mSendBuff = new byte[len];
			return mSendBuff;
		}

		public void sendMT(int len, int mt) throws IOException {
			mDOS.write(toLH(len));
			mDOS.write(toLH(mt));
			mSendLen = len;
			mSendMT = mt;
		}

		public String readMTString(int iPos, int len) throws IOException {
			// int len = iPos;
			// while (mRecvBuff[len] != 0 && len < mRecvLen) {
			// len++;
			// }
			// if (len<mRecvLen) {
			//
			// }
			// len -= iPos;
			mtIS.skip(len);
			return new String(mRecvBuff, iPos, len);
		}
	}

	public static abstract class GifDownUploadListner {
		public void onStart(GifTaskInfo taskInfo) {
		};

		public void onProgress(GifTaskInfo taskInfo, int curlen, int totallen) {
		};

		public abstract void onFinish(GifTaskInfo taskInfo);

		public void onError(GifTaskInfo taskInfo, int iCode, Object iPara) {

		};
	}

	public static class GifTaskInfo {
		public String mFullName;
		public int mId = -1;
		public String mUrl;
		public String mServerHost;// 新添加字段，上传任务服务器ip地址和端口
		public String mPath;
		public int mTime;
		public int mFileType;
		public boolean mAppend;
		public int mExtra; // 大图小图
		public long mSrcId; // 用户id，group id等
		public Object mObj; // 附加数据

		public GifTaskInfo() {

		}

		public GifTaskInfo(String url, String name, int type, boolean bAppend,
				int extra, Object obj) {
			mFullName = name;
			mUrl = url;
			mFileType = type;
			mAppend = bAppend;
			mExtra = extra;
			mObj = obj;
		}

	}

	static abstract class GifMTTaskBase extends GifTaskInfo {
		/** 成功 */
		public static final int RES_SUCCESS = 0;
		/** 不可恢复错误，任务终止 */
		public static final int RES_CRITICALERR = 3;

		/** 下载文件超时时间20s */
		public static final int DOWN_FILE_TIME_OUT = 20 * 1000;

		protected File mFile;// 上传下载任务使用的file对象，可能是一个临时文件

		public GifMTTaskBase(String url, String name, int type,
				boolean bAppend, int extra, Object obj) {
			super(url, name, type, bAppend, extra, obj);
		}

		public GifMTTaskBase() {

		}

		String mTaskId;
		int mTryTimes = 5;
		private boolean mbDone = false;

		/** 是否删除下载的临时文件,默认不删除 */
		public boolean mDeleteTempFile = false;

		// private boolean isTransferBigFile = false;//是否传输大文件

		protected void reset() {
			mbDone = false;
		};

		GifDownUploadListner mListner;

		protected void finish() {
			if (!mbDone) {
				mbDone = true;

				if (mListner != null)
					mListner.onFinish(this);
			}
		}

		// 根据重试次数判定是否再次执行
		protected void retry(int iCode, Object para) {
			if (Utils.test) {
				if (para instanceof Exception) {
					if (Utils.test)
						Log.w(TAG, "上传下载时异常retry--errorCode:" + iCode,
								(Exception) para);
				} else {
					if (Utils.test)
						Log.w(TAG, "retry其他错误信息--errorCode:" + iCode + "---"
								+ para);
				}
			}
			if (mTryTimes > 0) {
				mTryTimes--;
				if (Utils.test)
					Log.e(TAG, "重试了一次。还有" + mTryTimes + "次");
			} else {
				if (Utils.test)
					Log.e(TAG, "5次重试机会用完了。任务结束，但是没有完成");
				finish(iCode, para);
			}
		}

		protected void finish(int iCode, Object para) {
			if (!mbDone) {
				mbDone = true;
				if (mListner != null)
					mListner.onError(this, iCode, para);
			}
		}

		/** 任务是否有效 */
		protected boolean mbTaskValid = true;

		public boolean IsTaskValid() {
			return mbTaskValid;
		}

		public abstract void onReconnected(GSProtContext protCtx);

		public abstract int onSend(GSProtContext protCtx) throws IOException;

		public abstract int onRecv(GSProtContext protCtx) throws IOException;

		public int onCancel() {
			finish(DUERR_CANCELLED, null);
			return 0;
		}

		public boolean isDone() {
			// 任务结束了，不一定是正常完成了
			return mbDone;
		};

		// 协议封解包在此处理
		/** GS登录请求Mt的发送 */
		int reqGSLogon(GSProtContext ctx) throws IOException {
			ctx.sendMT(ctx.mLogonPara.tokenBytes.length + 4 * 5, GS_LOGIN);
			// 在写之前先转为低字节数组，然后写数组
			if (Utils.debug) {
				Log.i(TAG, "LOGIN_MESSAGE : " + ctx.mLogonPara.toString());
			}
			ctx.mDOS.write(toLH(ctx.mLogonPara.uid));
			ctx.mDOS.write(toLH(ctx.mLogonPara.cver));
			ctx.mDOS.write(toLH(ctx.mLogonPara.ver));
			ctx.mDOS.write(toLH(ctx.mLogonPara.osid));
			ctx.mDOS.write(toLH(ctx.mLogonPara.tokenBytes.length));
			ctx.mDOS.write(ctx.mLogonPara.tokenBytes);
			return RES_SUCCESS;
		}

		/** 表情包上传的请求 */
		int reqGifPackageStartUpload(GSProtContext ctx, String packagename,
				int size, String md5) throws IOException {
			byte[] packageNameBytes = packagename.getBytes();
			byte[] md5Bytes = md5.getBytes();
			ctx.sendMT(packageNameBytes.length + md5Bytes.length + 4 * 3,
					GIF_PACKAGE_START_UPLOAD);
			ctx.mDOS.write(toLH(packageNameBytes.length));
			ctx.mDOS.write(packageNameBytes);
			ctx.mDOS.write(toLH(size));
			ctx.mDOS.write(toLH(md5Bytes.length));
			ctx.mDOS.write(md5Bytes);
			return RES_SUCCESS;
		}

	}

	class GSLogonTask extends GifMTTaskBase {
		int miStatus = -1;
		int mStep = 0;

		public void reset() {
			miStatus = -1;
			mStep = 0;
			super.reset();
		}

		public GSLogonTask() {

		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			if (mStep == 0) {
				reqGSLogon(protCtx);
				mStep++;
			}
			return iRet;

		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			if (protCtx.mRecvMT != GS_LOGIN_RSP)
				return 0;
			miStatus = toInt(readBytes(protCtx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "login_miStatus = " + miStatus);
			}
			if (miStatus != 0)
				throw new IOException("Server reject.");
			finish();
			return 0;
		}

	}

	// 这里新增一个任务，单为获取服务器上的表情包的Id
	public class GTaskForId extends GifMTTaskBase {
		int mStep = 0;
		int mFileSize;
		FileInputStream mFileIS;
		int uploadRspErr;// 请求上传服务器，返回的应答错误码

		public void reset() {
			mStep = 0;
		}

		// 由路径获得包名
		String getPackageName(String fullName) {
			int index = fullName.lastIndexOf("/");
			String name = fullName.substring(index + 1, fullName.length());
			if (name.contains("\\.")) {
				String[] split = name.split("\\.");
				return split[0];
			}
			return name;

		}

		int prepFile() throws IOException {
			mFileIS = new FileInputStream(mFile);
			mFileSize = mFileIS.available();
			return 0;
		}

		void closeFile() throws IOException {
			if (mFileIS != null) {
				mFileIS.close();
				mFileIS = null;
			}
		}

		int onStartUploadRsp(GSProtContext ctx) throws IOException {
			// 获取表情包的id
			mId = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.e(TAG, "mId : " + mId);
			}
			if (ctx.mtIS.available() >= 4)
				uploadRspErr = toInt(readBytes(ctx.mtIS, 4));// 服务器返回的错误码
			else
				uploadRspErr = 0;
			if (Utils.debug) {
				Log.d(TAG, "2015/4/12 uploadRspErr :" + uploadRspErr);
			}
			if (Utils.debug) {
				switch (uploadRspErr) {
				case 0:
					break;
				case 1:
					Log.e(TAG, "51 :" + "操作失败");
					break;
				case 10:
					Log.e(TAG, "51 :表情包已经存在，不允许上传");
					break;
				}
			}
			switch (uploadRspErr) {
			case 0:
				finish();
				closeFile();
				break;
			case 1:
			case 10:
				finish(DUERR_SERVERREJECT, uploadRspErr);
				break;
			default:
				finish(DUERR_SERVERREJECT, uploadRspErr);
				break;
			}
			return 0;
		}

		public GTaskForId(String fullName, int fileType, String fileUrl,
				GifDownUploadListner listner) {
			mFile = new File(fullName);
			mFullName = fullName;
			if (fileUrl != null && fileUrl.length() > 0) {
				// 如果文件地址不为空，则续传
				mAppend = true;
				mPath = fileUrl;
			} else {
				mAppend = false;
				mPath = mFile.getName();
			}
			mListner = listner;
			mFileType = fileType;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {

		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {

			int iRet = 0;
			try {
				switch (mStep) {
				case 0:// 请求上传,需要包名，大小，md5
						// 1.获取包大小
						// prepFile();
					// 2.获取包名
					String packageName = getPackageName(mFullName);
					// 3.获取包的md5值
					String md5 = MD5FileUtil.getFileMD5String(mFile);
					reqGifPackageStartUpload(protCtx, packageName, mFileSize,
							md5);
					mStep++;
					break;
				}
			} catch (Exception e) {
				retry(DOWNERR_IOEXCEPTION, e);
				if (Utils.debug)
					Log.e(TAG, "发送上传请求异常", e);
				iRet = TASK_RES_RESET_NET;
			}

			return iRet;

		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {

			int iRet = 0;
			int pos, size, svrpos, status;
			try {
				switch (protCtx.mRecvMT) {
				case GS_START_UPLOAD_RSP:
					iRet = onStartUploadRsp(protCtx);
					break;
				}
			} catch (IOException e) {
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				if (Utils.debug)
					Log.e(TAG, "上传receive异常", e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;

		}

	}

	// 上传表情包任务
	// 1.上传请求
	// 2.上传内容
	class GUploadTask extends GifMTTaskBase {

		int mStep = 0;

		int mReqPos; // 文件位置
		// boolean mbDone = false;
		int uploadRspErr;// 请求上传服务器，返回的应答错误码

		int mLenDone;

		// File mFile;//用父类MTTaskBase的mFile对象
		int mTarUID;// 接收方id
		int mFileSize;
		FileInputStream mFileIS;

		public void reset() {
			mStep = 0;
		}

		int prepFile() throws IOException {
			mFileIS = new FileInputStream(mFile);
			mFileSize = mFileIS.available();
			return 0;
		}

		// 由路径获得包名
		String getPackageName(String fullName) {
			int index = fullName.lastIndexOf("/");
			String name = fullName.substring(index + 1, fullName.length());
			if (name.contains("\\.")) {
				String[] split = name.split("\\.");
				return split[0];
			}
			return name;

		}

		int readData(byte[] buff, int off, int len) throws IOException {
			if (mFileIS == null)
				mFileIS = new FileInputStream(mFile);
			return mFileIS.read(buff, off, len);
		}

		void closeFile() throws IOException {
			if (mFileIS != null) {
				mFileIS.close();
				mFileIS = null;
			}
		}

		public GUploadTask(String fullName, int fileType, boolean bAppend,
				GifDownUploadListner listner) {
			// mTaskId = getUploadTaskId(fullName);
			mFullName = fullName;
			mFile = new File(fullName);
			mAppend = bAppend;
			mListner = listner;
			mFileType = fileType;
			mPath = mFile.getName();
			mTryTimes = 5;
		}

		int reqUploadData(GSProtContext ctx) throws IOException {
			byte[] buff = ctx.getSendBuff(0x1000);
			int len = readData(buff, 0, 0x1000);
			if (len <= 0) {
				ctx.sendMT(4, GS_UPLOAD_FINISH);
				ctx.mDOS.write(toLH(mId));
			} else {
				ctx.sendMT(len + 12, GS_UPLOAD_DATA);
				ctx.mDOS.write(toLH(mId));
				ctx.mDOS.write(toLH(mReqPos));
				ctx.mDOS.write(toLH(len));
				ctx.mDOS.write(buff, 0, len);
				mReqPos += len;
			}
			return 0;
		}

		int onStartUploadRsp(GSProtContext ctx) throws IOException {
			// 获取表情包的id
			mId = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.e(TAG, "mId : " + mId);
			}
			if (ctx.mtIS.available() >= 4)
				uploadRspErr = toInt(readBytes(ctx.mtIS, 4));// 服务器返回的错误码
			else
				uploadRspErr = 0;
			switch (uploadRspErr) {
			case 0:
				break;
			case 1:
			case 10:
				finish(DUERR_SERVERREJECT, uploadRspErr);
				break;
			}
			return 0;
		}

		int rspUploadData(GSProtContext ctx) throws IOException {
			mId = toInt(readBytes(ctx.mtIS, 4));
			int filepos = toInt(readBytes(ctx.mtIS, 4));
			int size = toInt(readBytes(ctx.mtIS, 4));
			int svrpos = toInt(readBytes(ctx.mtIS, 4));
			int status = toInt(readBytes(ctx.mtIS, 4));
			if (status != 0) {
				finish(DUERR_SERVERREJECT, status);
			}
			return 0;
		}

		public GUploadTask(String fullName, int fileType, String fileUrl,
				GifDownUploadListner listner) {
			mFile = new File(fullName);
			mFullName = fullName;
			if (fileUrl != null && fileUrl.length() > 0) {
				// 如果文件地址不为空，则续传
				mAppend = true;
				mPath = fileUrl;
			} else {
				mAppend = false;
				mPath = mFile.getName();
			}
			mListner = listner;
			mFileType = fileType;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			try {
				switch (mStep) {
				case 0:// 请求上传,需要包名，大小，md5
						// 1.获取包大小
						// prepFile();
					// 2.获取包名
					String packageName = getPackageName(mFullName);
					// 3.获取包的md5值
					String md5 = MD5FileUtil.getFileMD5String(mFile);
					reqGifPackageStartUpload(protCtx, packageName, mFileSize,
							md5);
					mStep++;
					break;
				case 1:
					break;
				case 2:
				case 3:
					iRet = reqUploadData(protCtx);
					mStep++;
					break;
				}
			} catch (Exception e) {
				retry(DOWNERR_IOEXCEPTION, e);
				if (Utils.debug)
					Log.e(TAG, "发送上传请求异常", e);
				iRet = TASK_RES_RESET_NET;
			}

			return iRet;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			int pos, size, svrpos, status;
			try {
				switch (protCtx.mRecvMT) {
				case GS_START_UPLOAD_RSP:
					iRet = onStartUploadRsp(protCtx);
					mStep = 2;
					// if (uploadRspErr == 28) {
					// // 28:断点续传文件信息不存在，需要重新使用命令3上传
					// // 29:上传文件大小超出限制（按某某文件类型>2M需求待定）
					// // 30:文件类型不对，现有文件类型请参考上面的上传命令3
					// mStep = 0;// 重新开始请求上传
					// mAppend = false;
					// mPath = mFile.getName();
					// if (Utils.debug) {
					// Log.i(TAG, "续传失败，从新开始上传");
					// }
					// }
					break;
				case GS_UPLOAD_DATA_RSP: //
					iRet = rspUploadData(protCtx);
					mStep--; // 继续发? 是否要缓冲？
					break;
				case GS_UPLOAD_FINISH_RSP:
					finish();
					closeFile();
					break;
				}
			} catch (IOException e) {
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				if (Utils.debug)
					Log.e(TAG, "上传receive异常", e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}

	}

	/**
	 * 通过url,分割成4段数组
	 * 
	 * @param url
	 */
	public String[] getUrlArray(String url) {
		if (TextUtils.isEmpty(url)) {
			// url为空
			return null;
		}
		String[] parts = url.split(":");
		if (parts.length == 4) {
			return parts;
		}
		return null;
	}

	/** 获取普通下载任务taskId */
	public String getGDownloadTaskId(String url) {
		String[] urlArray = getUrlArray(url);
		if (urlArray != null) {
			return urlArray[2] + urlArray[3];
		}
		return null;
	}

	public class GifPicListDownloadTask extends GifMTTaskBase {
		int mStep = 0;
		public int page;
		public String name;
		public int hasNextPage;// 0为有后续 1为没有
		public int numOfCurPage;
		public String json;

		public GifPicListDownloadTask(int id, int page_index,
				GifDownUploadListner listner) {
			if (listner != null) {
				mListner = listner;
			}
			this.page = page_index;
			mId = id;
		}

		int reqGifPicListSartDownload(GSProtContext ctx, int page, int id)
				throws IOException {
			ctx.sendMT(8, GIF_PIC_LIST_START_DOWNLOAD);
			ctx.mDOS.write(toLH(id));
			ctx.mDOS.write(toLH(page));
			if (Utils.debug) {
				Log.e(TAG, "id : " + id + ",page : " + page);
			}
			return RES_SUCCESS;
		}

		int onGifPicListDownload(GSProtContext ctx) throws IOException {
			int status = toInt(readBytes(ctx.mtIS, 4));
			if (status != 0) {
				if (mListner != null) {
					mListner.onError(this, DUERR_SERVERREJECT, (Integer) status);
				}
			}
			int id = toInt(readBytes(ctx.mtIS, 4));
			numOfCurPage = toInt(readBytes(ctx.mtIS, 4));
			hasNextPage = toInt(readBytes(ctx.mtIS, 4));
			int skip = toInt(readBytes(ctx.mtIS, 4));
			json = ctx.readMTString(20, skip);
			if (Utils.debug) {
				Log.i(TAG, "205 : " + "status :" + status + ",id : " + id
						+ ",numOfCurPage :" + numOfCurPage + ",hasNextPage :"
						+ hasNextPage + ",skip : " + skip + ",json :" + json);
			}
			return RES_SUCCESS;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			switch (mStep) {
			case 0:
				reqGifPicListSartDownload(protCtx, page, mId);
				mStep++;
				break;

			default:
				break;
			}
			return 0;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			switch (mStep) {
			case 1:
				switch (protCtx.mRecvMT) {
				case GIF_PIC_LIST_START_DOWNLOAD_RSP:
					iRet = onGifPicListDownload(protCtx);
					finish();
					break;

				default:
					break;
				}
				break;

			default:
				break;
			}
			return 0;
		}

	}

	// 下载表情包列表
	public class GifListDownloadTask extends GifMTTaskBase {
		int mStep = 0;
		public int page;
		public int hasNextPage;// 0为有后续 1为没有
		public int numOfCurPage;
		public String json;

		public GifListDownloadTask(int page_index, GifDownUploadListner listner) {
			if (listner != null) {
				mListner = listner;
			}
			mTaskId = GifDownActivity.GIF_LIST_MESSAGE_ID;
			mTryTimes = 1;
			this.page = page_index;
		}

		int reqGifListSartDownload(GSProtContext ctx, int page)
				throws IOException {
			ctx.sendMT(4, GIF_LIST_START_DOWNLOAD);
			ctx.mDOS.write(toLH(page));
			return RES_SUCCESS;
		}

		int onGifListDownload(GSProtContext ctx) throws IOException {
			int cur_page = toInt(readBytes(ctx.mtIS, 4));
			hasNextPage = toInt(readBytes(ctx.mtIS, 4));
			numOfCurPage = toInt(readBytes(ctx.mtIS, 4));
			int skip = toInt(readBytes(ctx.mtIS, 4));
			json = ctx.readMTString(16, skip);
			if (Utils.debug) {
				Log.i(TAG, "201 [ " + "cur_page :" + cur_page
						+ ",hasNextPage : " + hasNextPage + ",numOfcurPage :"
						+ numOfCurPage + ",skip : " + skip + ",json : " + json);
			}
			return RES_SUCCESS;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			switch (mStep) {
			case 0:
				reqGifListSartDownload(protCtx, page);
				mStep++;
				break;

			default:
				break;
			}

			return 0;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			try {
				switch (mStep) {
				case 1:
					switch (protCtx.mRecvMT) {
					case GIF_LIST_START_DOWNLOAD_RSP:
						iRet = onGifListDownload(protCtx);

						finish();
						break;

					default:
						break;
					}
					break;

				default:
					break;
				}
			} catch (IOException e) {
				retry(DOWNERR_IOEXCEPTION, e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}

	}

	// 下载表情包信息
	public class GifDetailDownloadTask extends GifMTTaskBase {
		int package_id;
		int mStep = 0;
		String name;
		int num;
		int size;
		public String md5;

		public GifDetailDownloadTask(int id, GifDownUploadListner listner) {
			package_id = id;
			mListner = listner;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		int reqStartDownloadGifDetail(GSProtContext ctx, int id)
				throws IOException {
			ctx.sendMT(4, GIF_DETAIL_START_DOWNLOAD);
			if (Utils.debug) {
				Log.i(TAG, "202 id :" + id);
			}
			ctx.mDOS.write(toLH(id));
			return RES_SUCCESS;
		}

		int onDownloadGifDetail(GSProtContext ctx) throws IOException {
			int status = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 status :" + status);
			}
			if (status != 0) {
				if (mListner != null) {
					mListner.onError(this, DUERR_SERVERREJECT, (Integer) status);
				}
			}
			package_id = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 id :" + package_id);
			}
			int skip1 = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 skip1 :" + skip1);
			}
			name = ctx.readMTString(12, skip1);
			if (Utils.debug) {
				Log.i(TAG, "203 name :" + name);
			}
			num = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 num :" + num);
			}
			size = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 size :" + size);
			}
			int skip2 = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "203 skip2 :" + skip2);
			}
			md5 = ctx.readMTString(skip1 + 4 * 6, skip2);
			if (Utils.debug) {
				Log.i(TAG, "203 md5 :" + md5);
			}
			if (Utils.debug) {
				Log.i(TAG, "203 : " + "status : " + status + ",package_id : "
						+ package_id + ",name : " + name + ",num : " + num
						+ ",size : " + size + ",md5 : " + md5);
			}
			return RES_SUCCESS;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			// 发送获取表情包的请求
			reqStartDownloadGifDetail(protCtx, package_id);
			mStep++;

			return 0;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			if (Utils.debug) {
				Log.i(TAG, "执行了包信息的下载onRecv方法");
			}
			int iRet = 0;
			try {
				switch (protCtx.mRecvMT) {
				case GIF_DETAIL_START_DOWNLOAD_RSP:
					iRet = onDownloadGifDetail(protCtx);
					finish();
					break;
				}
			} catch (Exception e) {
				retry(DOWNERR_IOEXCEPTION, e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}

	}

	public class GifDownloadTask extends GifMTTaskBase {
		int mStep = 0;
		String md5;
		int mFileOff;
		long doTaskTime = 0;
		int mGifOff;
		int mGifSize;

		public GifDownloadTask(String name, int type, boolean bAppend,
				String md5, int id, GifDownUploadListner listner) {
			super(null, name, type, bAppend, 0, null);
			try {
				mId = id;
				this.md5 = md5;
				mFile = new File(name + ".download");
				mFullName = name;
				mAppend = bAppend;
				mListner = listner;
				mTryTimes = 5;
				mTaskId = md5;

			} catch (Exception e) {

			}
		}

		OutputStream mFileOS;
		int mLenDone;

		/**
		 * 每次下载图片请求前获取一下当前文件的大小，为断点续传做偏移使用
		 * 
		 * @throws IOException
		 */
		public void onPrepair() throws IOException {
			if (mFile.exists()) {
				if (Utils.debug) {
					Log.e(TAG, "续传图片存在");
				}
				RandomAccessFile raf = new RandomAccessFile(mFile, "rw");
				mFileOff = (int) raf.length();
				raf.close();
				mStep = 2;
			} else {
				mFileOff = 0;
			}
		}

		void writeFile(byte[] buff, int off, int len) throws IOException {
			doTaskTime = System.currentTimeMillis();
			if (mDeleteTempFile) {
				// 要删除下载的临时文件，那么就不执行下面的写文件操作，直接返回
				return;
			}
			if (mFileOS == null)
				mFileOS = new FileOutputStream(mFile, mAppend);
			mFileOS.write(buff, off, len);
			if (Utils.debug)
				Log.i(TAG, "输出流写文件：off=" + off + ",len=" + len);
			mLenDone += len;
		}

		void closeFile() throws IOException {
			if (mFileOS != null) {
				mFileOS.close();
				mFileOS = null;
			}
		}

		// 请求下载
		int reqStartGifStartDownload(GSProtContext ctx, int id, String md5,
				int start) throws IOException {
			byte[] md5Bytes = md5.getBytes();
			ctx.sendMT(md5Bytes.length + 12, GIF_START_DOWNLOAD);
			ctx.mDOS.write(toLH(id));
			ctx.mDOS.write(toLH(md5Bytes.length));
			ctx.mDOS.write(md5Bytes);
			if (Utils.debug) {
				Log.e(TAG, "2015/3/10 md5 :" + md5 + ",id :" + id);
			}
			ctx.mDOS.write(toLH(start));
			return RES_SUCCESS;
		}

		int onGifStartDownloadRsp(GSProtContext ctx) throws IOException {
			int statusCode = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				switch (statusCode) {
				case STATUS_SUCCESS:
					Log.e(TAG, "151 statusCod :" + "表情包存在，成功");
					break;
				case STATUS_NOT_FOUND_GIF_PACKAGE:
					Log.e(TAG, "151 statusCod :" + "指定的表情包不存在");
					break;
				case STATUS_NOT_FOUND_GIF:
					Log.e(TAG, "151 statusCod :" + "指定的表情不存在");
					break;

				}
			}
			if (statusCode != 0) {
				finish(DUERR_SERVERREJECT, (Integer) statusCode);
			}
			return RES_SUCCESS;
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			doTaskTime = System.currentTimeMillis();
			if (mAppend && mGifOff > 0 && mStep == 0) {
				// 在要求断点续传、并且mImgOff值大于0(即已经获取大小图偏移)、并且是第一次请求（重新请求）下载的前提下，去准备文件大小
				onPrepair();
			}
			int iRet = 0;
			try {
				switch (mStep) {
				case 0:// 请求下载
					reqStartGifStartDownload(protCtx, mId, md5, 0);
					if (mListner != null) {
						mListner.onStart(this);
					}
					mStep++;
				case 1: // 等待接收
					break;
				case 2: // 发送下载请求

					break;

				default:
					break;
				}

			} catch (Exception e) {

			}
			return 0;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			try {
				switch (mStep) {
				case 1:
					switch (protCtx.mRecvMT) {
					case GIF_START_DOWNLOAD_RSP:
						iRet = onGifStartDownloadRsp(protCtx);
						break;
					case GIF_DOWNLOAD_DATA:
						mGifOff = toInt(readBytes(protCtx.mtIS, 4));
						mGifSize = toInt(readBytes(protCtx.mtIS, 4));
						writeFile(protCtx.mRecvBuff, 8, mGifSize);
						break;

					case GIF_DOWNLOAD_FINISH:
						closeFile();
						mFile.renameTo(new File(mFullName));
						finish();
						break;
					}
					break;

				}

			} catch (SocketException se) {
				if (Utils.debug) {
					Log.e(TAG, "SocketException:" + se.getMessage());
				}
				try {
					closeFile();
				} catch (IOException e2) {
					if (Utils.debug) {
						Log.w(TAG, "关闭流异常", e2);
					}
				}
				retry(DOWNERR_IOEXCEPTION, se);
				iRet = TASK_RES_RESET_NET;
			} catch (IOException e) {
				if (Utils.debug) {
					Log.e(TAG, "IOException:", e);
				}
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					if (Utils.debug) {
						Log.w(TAG, "关闭文件流异常", e2);
					}
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				iRet = TASK_RES_RESET_NET;
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "其他Exception:", e);
				}
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					if (Utils.debug) {
						Log.w(TAG, "关闭文件流异常", e2);
					}
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}
	}

	class GIFPackageDownloadTask extends GifMTTaskBase {
		int mStep = 0;

		int pos;
		int size;

		protected int mDownloadFileLength; // 要下载的文件长度

		protected long doTaskTime = 0;// 任务被执行的时间戳，如果超过20s没有执行写操作则重置任务

		int mReqPos; // 文件位置
		int mReqSize; // 请求的总长度，0表示所有

		int mLenDone;

		OutputStream mFileOS;

		public void reset() {
			mStep = 0;
		}

		/** 删除下载任务的临时文件 */
		public boolean deleteTaskFile() {
			if (isDone()) {
				// 任务结束，删除失败
				return false;
			} else {
				try {
					closeFile();
					if (Utils.debug)
						Log.e(TAG, "文件【" + mFile.getPath() + "】将要被删除");
					return mFile.delete();
				} catch (IOException e) {
					if (Utils.debug)
						Log.e(TAG, "删除文件前，关闭文件流异常", e);
				}
				return false;
			}
		}

		void writeFile(byte[] buff, int off, int len) throws IOException {
			doTaskTime = System.currentTimeMillis();
			if (mDeleteTempFile) {
				// 要删除下载的临时文件，那么就不执行下面的写文件操作，直接返回
				return;
			}
			if (mFileOS == null)
				mFileOS = new FileOutputStream(mFile, mAppend);
			mFileOS.write(buff, off, len);
			if (Utils.debug)
				Log.i(TAG, "输出流写文件：off=" + off + ",len=" + len);
			mLenDone += len;
		}

		void closeFile() throws IOException {
			if (mFileOS != null) {
				mFileOS.close();
				mFileOS = null;
			}
		}

		public GIFPackageDownloadTask(String md5, String url, String fullName,
				int type, boolean bAppend, GifDownUploadListner listner,
				int packageId) {
			super(url, fullName, type, bAppend, 0, null);
			if (Utils.debug) {
				Log.i(TAG, "下载文件地址为：" + url);
			}
			try {
				mId = packageId;
				// String[] parts = url.split(":");
				// mTime = Integer.valueOf(parts[3]);
				// mPath = parts[2];
				// mTaskId = mPath + mTime;
				mTaskId = md5;
				// if (fullName == null || fullName.equals("")) {
				// fullName = parts[2];
				// }
				FileManager.getShenLiaoGifPackagePath();
				mFile = new File(fullName + ".download");
				mFullName = fullName;
				mAppend = bAppend;
				mListner = listner;
				mTryTimes = 5;
			} catch (NumberFormatException e) {
				mbTaskValid = false;
			} catch (ArrayIndexOutOfBoundsException e) {
				mbTaskValid = false;
			} catch (Exception e) {
				mbTaskValid = false;
			}
			// 移到调用下载方法，生成下载任务的时候去操作，这样易于管理，这个下载的实现相当于对调用者透明
			// if(!mbTaskValid)
			// listner.onError(this,DUERR_INVALIDPARA, null);
		}

		@Override
		public void onReconnected(GSProtContext protCtx) {
			mStep = 0;
		}

		int reqStartDownloadGifPackage(GSProtContext ctx, int packageId)
				throws IOException {
			mId = packageId;
			ctx.sendMT(4, GIF_PACKAGE_START_DOWNLOAD);
			ctx.mDOS.write(toLH(packageId));
			if (Utils.debug) {
				Log.i(TAG, "100 : " + Arrays.toString(toLH(packageId)));
			}
			return RES_SUCCESS;
		}

		int onFinishDownloadGifPackage(GSProtContext ctx) throws IOException {
			int available = ctx.mDIS.available();
			if (Utils.debug) {
				Log.i(TAG, "available : " + available);
			}
			int id = toInt(readBytes(ctx.mtIS, 4));
			int statuscode = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "104: id :" + id + ",statuscode : " + statuscode);
			}
			return RES_SUCCESS;
		}

		int onStartDownloadGifPackage(GSProtContext ctx) throws IOException {
			int id = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "recevice Id : " + id);
			}
			mDownloadFileLength = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "recevice length : " + mDownloadFileLength);
			}
			int skip = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "recevice skip : " + skip);
			}
			String md5 = ctx.readMTString(12, skip);
			if (Utils.debug) {
				Log.i(TAG, "recevice md5 : " + md5);
			}
			int statusCode = toInt(readBytes(ctx.mtIS, 4));
			if (Utils.debug) {
				Log.i(TAG, "101 : " + "id :" + id + ",length : "
						+ mDownloadFileLength + ",skip : " + skip + ",md5 : "
						+ md5 + ",statusCode : " + statusCode);
			}
			if (statusCode != 0) {
				finish(DUERR_SERVERREJECT, (Integer) statusCode);
			}
			if (statusCode == 0 && mListner != null) {
				mListner.onStart(this);
			}
			return RES_SUCCESS;
		};

		int rspGifPackageDownloadData(GSProtContext ctx, int id, int start,
				int len, int nextPosition, int reqCode) throws IOException {
			ctx.sendMT(20, GIF_PACKAGE_DOWNLOAD_RSP);
			ctx.mDOS.write(toLH(id));
			ctx.mDOS.write(toLH(start));
			ctx.mDOS.write(toLH(len));
			ctx.mDOS.write(toLH(nextPosition));
			ctx.mDOS.write(toLH(reqCode));
			if (Utils.debug) {
				Log.i(TAG, "103 : " + "id :" + id + ",start : " + start
						+ ",len : " + len + ",nextPosition : " + nextPosition
						+ ",reqCode : " + reqCode);
			}
			return RES_SUCCESS;

		}

		@Override
		public int onSend(GSProtContext protCtx) throws IOException {
			doTaskTime = System.currentTimeMillis();
			int iRet = RES_SUCCESS;
			try {
				switch (mStep) {
				case 0:// 请求下载表情包
					if (mFile.exists()) {
						// 如果该临时文件存在，则进行断点下载
						mReqPos = (int) mFile.length();
					}
					reqStartDownloadGifPackage(protCtx, mId);
					break;
				case 1:
				case 2:

					rspGifPackageDownloadData(protCtx, mId, pos, size, pos
							+ size, DOWNLOAD_CONTINUE);
					break;
				}

			} catch (Exception e) {
				iRet = TASK_RES_RESET_NET;
				retry(DOWNERR_IOEXCEPTION, e);
			}
			return iRet;
		}

		@Override
		public int onRecv(GSProtContext protCtx) throws IOException {
			int iRet = 0;
			try {
				switch (protCtx.mRecvMT) {
				case GIF_PACKAGE_START_DOWNLOAD_RSP:
					onStartDownloadGifPackage(protCtx);
					if (mReqPos == mDownloadFileLength) {
						// 如果请求下载的文件位置等于服务器文件总长度，则直接执行结束操作

						closeFile();
						// TODO 避免重名
						// mFullName = getValidName(mFullName);

						mFile.renameTo(new File(mFullName));
						finish();

					}
					mStep = -1;
					break;
				case GIF_PACKAGE_DOWNLOAD:

					int id = toInt(readBytes(protCtx.mtIS, 4));
					pos = toInt(readBytes(protCtx.mtIS, 4));
					size = toInt(readBytes(protCtx.mtIS, 4));
					if (Utils.debug) {
						Log.i(TAG, "102 : " + "id : " + id + ",pos : " + pos
								+ ",size : " + size);
					}
					writeFile(protCtx.mRecvBuff, 12, size);
					if (mListner != null) {
						if (Utils.debug) {
							Log.e(TAG, "mReqPos : " + mReqPos + ",mLenDonw : "
									+ mLenDone + ",mDownloadFileLength : "
									+ mDownloadFileLength);
						}
						mListner.onProgress(this, mReqPos + mLenDone,
								mDownloadFileLength);
					}
					mStep = 2;
					break;
				case GIF_PACKAGE_DOWNLOAD_FINISH:

					onFinishDownloadGifPackage(protCtx);
					closeFile();

					mFile.renameTo(new File(mFullName));
					finish();
					break;
				}
			} catch (Exception e) {
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}
	}

	static class GSSocket {
		static final int BUFFERSIZE = 0x1000;

		/** Socket */
		Socket mSocket;
		GSProtContext mProtCtx;

		public GSSocket(GSProtContext protCtx) {
			mProtCtx = protCtx;
		}

		/** 网络已连接 */
		public boolean connected() {
			return (mSocket != null && mSocket.isConnected() && !mSocket
					.isInputShutdown());
		}

		public void connect() throws IOException {
			mSocket = new Socket();
			mSocket.setSendBufferSize(BUFFERSIZE);
			mSocket.setTcpNoDelay(true);
			mSocket.setKeepAlive(true);

			mSocket.connect(new InetSocketAddress(mProtCtx.gsSvr.mServer,
					mProtCtx.gsSvr.mPort));
			try {
				if (mProtCtx.gsSvr.mTimeout > 0)
					mSocket.setSoTimeout(mProtCtx.gsSvr.mTimeout);
			} catch (SocketException e) {
				e.printStackTrace();
				if (Utils.debug) {
					Log.e(TAG, "socket连接超时");
				}
			}
			// mSockDOS =
			mProtCtx.mDOS = new DataOutputStream(mSocket.getOutputStream());
			// mSockDIS =
			mProtCtx.mDIS = new DataInputStream(mSocket.getInputStream());

		}

		public void reset() {
			if (connected()) {
				try {
					mSocket.close();
				} catch (IOException e) {

				}
			}
			mProtCtx.mDOS = null;
			mProtCtx.mDIS = null;
			mSocket = null;
		}

		public int doTask(GifMTTaskBase task, boolean bReconnected) {
			int iRet;
			try {
				mProtCtx.prepSend();
				mProtCtx.mRecvLen = 0;
				if (bReconnected)
					task.onReconnected(mProtCtx);
				iRet = task.onSend(mProtCtx);
				if (mProtCtx.mSendMT > 0)
					if (Utils.debug) {
						Log.i(TAG, "1onSend:len=" + mProtCtx.mSendLen + ",mt="
								+ mProtCtx.mSendMT + ",Thread:"
								+ Thread.currentThread().getId());
					}
				// while (mProtCtx.mDIS.available() >0) {
				// if (Utils.debug) {
				// Log.i(TAG, "这里的mDis :" + mProtCtx.mDIS.available());
				// }
				// }
				if (mProtCtx.mSendMT > 0 || mProtCtx.mDIS.available() >= 4) {
					mProtCtx.mRecvLen = toInt(readBytes(mProtCtx.mDIS, 4));
					mProtCtx.mRecvMT = toInt(readBytes(mProtCtx.mDIS, 4));

					if (Utils.debug) {
						Log.i(TAG, "2onRecv:len=" + mProtCtx.mRecvLen + ",mt="
								+ mProtCtx.mRecvMT + ",Thread:"
								+ Thread.currentThread().getId());
					}
					// int package_length = mProtCtx.mRecvLen;
					// if (Utils.debug) {
					// Log.i(TAG, "===" + package_length);
					// int recev_length = 0;
					// int availableLength = 0;
					// while (recev_length < package_length) {
					// // recev_length=mProtCtx.mDIS.available();
					// // if (recev_length>0) {
					// availableLength = mProtCtx.prepRecvFor102(recev_length);
					// recev_length += availableLength;
					// // }
					// }
					// }
					if (Utils.debug) {
						Log.e(TAG, "2015/1/30 mProtCtx.mRecvLen :"
								+ mProtCtx.mRecvLen);
					}
					mProtCtx.prepRecvFor102Test();
					iRet |= task.onRecv(mProtCtx);
					// }
					// else {
					// mProtCtx.prepRecv();
					// iRet |= task.onRecv(mProtCtx);
					// }

				} else {
					// 正常情况下应该走不到这里，除非前端调用有问题
					// task.finish(DOWNERR_IOEXCEPTION, null);//好像会频繁引起下载失败的问题
				}
			} catch (IOException e) {
				task.retry(DOWNERR_IOEXCEPTION, e);
				if (Utils.debug)
					Log.e(TAG, "doTask exception:", e);
				iRet = TASK_RES_RESET_NET;
			}

			/** 判断下载任务是否超时 */
			if (task instanceof GIFPackageDownloadTask) {
				GIFPackageDownloadTask dtask = (GIFPackageDownloadTask) task;
				if (dtask.doTaskTime == 0) {
					// 任务没有执行，断网重连
					iRet = TASK_RES_RESET_NET;
				} else if ((System.currentTimeMillis() - dtask.doTaskTime) > GifTransfer.DOWN_FILE_TIME_OUT) {
					// 任务执行超时，则断网重连
					iRet = TASK_RES_RESET_NET;
					if (Utils.debug)
						Log.e(TAG,
								"doTask 任务执行超时:"
										+ (System.currentTimeMillis() - dtask.doTaskTime));
					GifPackageDownActivity.mDowningList.clear();
				}
			}
			if (task instanceof GifDownloadTask) {
				GifDownloadTask gtask = (GifDownloadTask) task;
				if (Utils.debug) {
					Log.e(TAG, "2015/3/25 gif " + gtask.md5 + ",的下载时间："
							+ (System.currentTimeMillis() - gtask.doTaskTime));
				}
				if (gtask.doTaskTime == 0) {
					iRet = TASK_RES_RESET_NET;
				} else if ((System.currentTimeMillis() - gtask.doTaskTime) > GifTransfer.DOWN_FILE_TIME_OUT) {
					iRet = TASK_RES_RESET_NET;
				}
			}

			return iRet;
		}

		public int waitTask(GifMTTaskBase task, boolean bReconnected) {
			int iRet = 0;
			while (!task.isDone() && iRet == 0) {
				if (!connected())
					break;
				iRet = doTask(task, bReconnected);
				bReconnected = false;
			}
			return iRet;
		}
	}

	static class GifTaskQueue {
		static class TaskWrapper {
			GifMTTaskBase mTask;
			int mQueue;

			void set(GifMTTaskBase task, int iQueue) {
				mTask = task;
				mQueue = iQueue;
			}
		}

		final int mLimit1 = 200;
		final int mLimit2 = 200;
		Object mNotifier = new Object();
		GifTaskExecutor[] mExecutors;
		LinkedList<GifMTTaskBase> mMTTasks1 = new LinkedList<GifMTTaskBase>();// 优先级高，先从该list取任务
		LinkedList<GifMTTaskBase> mMTTasks2 = new LinkedList<GifMTTaskBase>();// 优先级低，当mMTTasks1中没有任务时，从该list取任务
		HashMap<String, TaskWrapper> mMTTasksMap = new HashMap<String, TaskWrapper>();
		GifMTTaskBase[] mCurTasks;// 当前正在被执行的任务的引用

		public GifTaskQueue(GifTaskExecutor[] executors) {
			mCurTasks = new GifMTTaskBase[executors.length];
			mExecutors = executors;
			int channel = 0;
			for (GifTaskExecutor exec : mExecutors) {
				exec.setQueue(this, channel);
				channel++;
			}
		}

		public int getChnlNo() {
			return mCurTasks.length;
		}

		public int startExecutors() {
			for (GifTaskExecutor exec : mExecutors) {
				if (exec != null && !exec.isAlive())
					exec.start();
			}
			return mExecutors.length;
		}

		public boolean AddTask(GifMTTaskBase task, int iQueue, boolean bNow) {
			boolean bRet = AddTask_(task, iQueue, bNow);
			synchronized (mNotifier) {
				mNotifier.notifyAll();
			}
			return bRet;
		}

		synchronized boolean AddTask_(GifMTTaskBase task, int iQueue,
				boolean bNow) {
			if (Utils.debug) {
				Log.d(TAG, "2015/3/16 AddTask_调用了");
			}
			if (iQueue != 0)
				iQueue = 1;
			// for (GifMTTaskBase tsk : mCurTasks) {
			// if (tsk == task
			// || (tsk != null && tsk.mTaskId.equals(task.mTaskId))) {
			// // 添加判断taskId是否相同 2013.12.20
			// // 解决下面问题：大文件上传过程中断网，此时任务在当前执行队列，
			// // 重新连上网络，会自动登陆神聊服务器，登陆成功后会自动发送未发送成功的消息
			// // 添加任务到等待队列时，没有相同taskId，因为相同的任务在正在执行的队列，这样重复的任务就被添加到任务队列
			// // 问题结果：同一个文件会上传两次，可能会发两次消息
			// return true;
			// }
			// }

			TaskWrapper tw = mMTTasksMap.get(task.mTaskId);
			int iLimit = (iQueue == 0 ? mLimit1 : mLimit2);
			// tssn为当前任务指定要插入的队列
			LinkedList<GifMTTaskBase> tssn = (iQueue == 0 ? mMTTasks1
					: mMTTasks2);
			if (tw != null) {
				if (iQueue == tw.mQueue && !bNow)
					return true;
				// 此任务已经在等待队列，tsso为当前任务所在队列的引用
				LinkedList<GifMTTaskBase> tsso = (tw.mQueue == 0 ? mMTTasks1
						: mMTTasks2);
				tsso.remove(tw.mTask);
				mMTTasksMap.remove(task.mTaskId);
			} else
				tw = new TaskWrapper();

			if (bNow) {
				tssn.addFirst(task);
				if (tssn.size() > iLimit)
					tssn.removeLast();
			} else {
				if (tssn.size() >= iLimit)
					return false;
				tssn.addLast(task);
			}
			if (Utils.debug) {
				for (int i = 0; i < tssn.size(); i++) {
					Log.d(TAG, "2015/3/16 tssn " + tssn.get(0).mTaskId);
				}
			}
			tw.set(task, iQueue);
			mMTTasksMap.put(task.mTaskId, tw);
			return true;
		}

		public synchronized GifMTTaskBase getTask(int chnl) {
			if (chnl < 0 || chnl >= mCurTasks.length)
				return null;
			GifMTTaskBase vRet = mCurTasks[chnl];
			if (vRet != null)
				return vRet;
			if (mMTTasks1.size() > 0)
				vRet = mMTTasks1.poll();
			else if (mMTTasks2.size() > 0)
				vRet = mMTTasks2.poll();
			if (vRet != null) {
				mCurTasks[chnl] = vRet;
				mMTTasksMap.remove(vRet.mTaskId);
				if (Utils.debug) {
					Log.i("downloadAvatar", "StartTask,id=" + vRet.mTaskId);
				}
			}
			return vRet;
		}

		public synchronized void clearCurTask(int chnl) {
			if (chnl < 0 || chnl >= mCurTasks.length)
				return;
			GifMTTaskBase tsk = mCurTasks[chnl];
			mCurTasks[chnl] = null;

		}

		public GifMTTaskBase waitTask(int chnl, int timeout) {
			if (chnl >= mCurTasks.length)
				return null;
			GifMTTaskBase vRet = getTask(chnl);
			if (vRet == null) {
				synchronized (mNotifier) {
					try {
						mNotifier.wait(timeout);
					} catch (InterruptedException e) {
						if (Utils.debug) {
							Log.e(TAG, "线程被interrupted异常", e);
						}
					}
				}
				vRet = getTask(chnl);
			}
			return vRet;
		}

		public synchronized GifMTTaskBase cancelCurrent(int chnl) {
			if (chnl < mCurTasks.length) {
				GifMTTaskBase task = mCurTasks[chnl];
				mCurTasks[chnl] = null;
				return task;
			}
			return null;
		}

		public synchronized boolean removeTask(String taskId) {
			TaskWrapper tw = mMTTasksMap.remove(taskId);
			if (tw != null) {
				if (tw.mQueue == 0)
					return mMTTasks1.remove(tw.mTask);
				else
					return mMTTasks2.remove(tw.mTask);
			}
			return false;
		}

		public synchronized boolean cancelTask(String taskid,
				boolean isDeleteTempFile) {
			for (int i = 0; i < mCurTasks.length; i++) {
				GifMTTaskBase task = mCurTasks[i];
				if (task != null && task.mTaskId.equals(taskid)) {
					task.mDeleteTempFile = isDeleteTempFile;
					mCurTasks[i] = null;
					this.mExecutors[i].cancelCurTask();
					if (isDeleteTempFile) {
						if (task instanceof GIFPackageDownloadTask) {
							// 如果是大文件下载任务，任务没有结束，则删除临时文件
							if (!task.isDone()) {
								((GIFPackageDownloadTask) task)
										.deleteTaskFile();
							}
						}
					}
					return true;
				}
			}
			return false;
		}

		public synchronized boolean cancelBigTask(String taskid) {
			for (int i = 0; i < mCurTasks.length; i++) {
				GifMTTaskBase task = mCurTasks[i];
				if (task != null && task.mTaskId.equals(taskid)) {
					mCurTasks[i] = null;
					this.mExecutors[i].cancelCurTask();
					task.mFile.delete();
					return true;
				}
			}
			return false;
		}

	}

	// 外部传入
	GSLogonPara mLogonPara;
	GSServerProp mServerProp;

	class GifTaskExecutor extends Thread {
		GifTaskQueue mTaskQueue;
		int mChnl;
		GSProtContext mCtx;
		GSSocket mGSSockt;
		volatile boolean mbRun = true;
		GSLogonTask mGSlogonTask;

		public void setQueue(GifTaskQueue queue, int chnl) {
			mTaskQueue = queue;
			mChnl = chnl;
		}

		public boolean stopMe() {
			boolean bRet = mbRun;
			mbRun = false;
			return bRet;
		}

		public GifTaskExecutor() {
			mCtx = new GSProtContext(mLogonPara, mServerProp);
			mGSSockt = new GSSocket(mCtx);
			mGSlogonTask = new GSLogonTask();
		}

		int mGSTryTimes;
		int mSta = 0;

		public void cancelCurTask() {
			mSta |= TASK_RES_RESET_NET;
		}

		@Override
		public void run() {
			mGSTryTimes = 0;
			GifMTTaskBase task;
			// mSta = TASK_RES_CHGTS;
			while (mbRun) {
				boolean bNetReconnected = false;
				// 有任务才试图连网, 无任务时也不主动断网
				task = mTaskQueue.waitTask(mChnl, 2000);
				if (task == null) {
					// idle策略
					continue;
				}

				if (Utils.debug) {
					if (!task.isDone()) {
						// Log.d(TAG, "任务未完成，检查网络");
					}
				}

				// 有任务,检查网络
				if (!mGSSockt.connected()) {
					if (Utils.debug) {
						Log.d(TAG, "TryTimes :" + mGSTryTimes);
					}
					if (0 < mGSTryTimes) {
						if (Utils.debug) {
							Log.d(TAG, "socket未连接，但是有有重连次数");
						}
						if (mGSTryTimes < 5) {
							long waitTime = (mGSTryTimes < 3) ? 1000
									: ((mGSTryTimes < 5) ? 2000 : 5000);
							synchronized (mLogonPara.mNetNotifier) {
								try {
									mLogonPara.mNetNotifier.wait(waitTime);
								} catch (InterruptedException e) {
								}
							}
						} else {
							Log.e(TAG, "list任务socket连接失败,任务被清除");
							task.finish(DOWNERR_IOEXCEPTION,
									DOWNERR_IOEXCEPTION);
							mTaskQueue.clearCurTask(mChnl);
							task = null;
							mGSTryTimes = 0;
							mGSSockt.reset();
						}
					}
					mGSTryTimes++;
				}
				try {
					if (!mGSSockt.connected()
							|| (mSta & TASK_RES_RESET_NET) != 0) {
						mGSlogonTask.reset();
						if (Utils.debug) {
							Log.d(TAG, "socket未连接，现在试图连接socket");
						}
						mGSSockt.connect();
						mSta |= mGSSockt.waitTask(mGSlogonTask, true);
						bNetReconnected = true;
						mSta &= ~TASK_RES_RESET_NET;
						mGSTryTimes = 0;
					}
				} catch (IOException e) {
					if (Utils.debug) {
						Log.w(TAG, "连接GS服务器异常", e);
					}
				}
				if (mGSSockt.connected()) {
					// 如果有多个任务需要完成，在此一并处理
					if (Utils.debug) {
						// Log.d(TAG, "socket连接成功，开始任务");
					}
					int taskRes = mGSSockt.doTask(task, bNetReconnected);
					if (task.isDone()) {
						Log.d(TAG, "任务完成，断开socket");
						mTaskQueue.clearCurTask(mChnl);
						mGSSockt.reset();
					} else {
						// Log.d(TAG, "任务未完成，线程yield");
						Thread.yield();
					}

					if (mGSSockt.mProtCtx.mRecvLen == 0
							&& mGSSockt.mProtCtx.mSendLen == 0) {
						try {
							if (Utils.debug) {
								// Log.d(TAG, "没有协议，线程休眠");
							}
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					// 如果任务需要重试，则由任务本身决定
					if ((taskRes & TASK_RES_RESET_NET) != 0) { // 需要断网重连
						if (Utils.debug) {
							Log.d(TAG, "任务需要重试，断开socket,不能清除当前任务");
						}
						// mTaskQueue.clearCurTask(mChnl);
						mGSSockt.reset();
						// task.mAppend =
						// true;//当断网时，设定续传mAppend为true;因为断网重连后需要续传或者断点下载

					}
					// if ((taskRes & TASK_RES_CHGTS) != 0) {
					// mGSSockt.reset();
					// mSta |= TASK_RES_CHGTS;
					// if (Utils.debug) {
					// Log.i(TAG, "TaskExecutor, TASK_RES_CHGTS");
					// }
					// }
				}
			}
		}

	}

	/** 将int转为低字节在前，高字节在后的byte数组 */
	public static byte[] toLH(int n) {
		byte[] b = new byte[4];
		b[0] = (byte) (n & 0xff);
		b[1] = (byte) (n >> 8 & 0xff);
		b[2] = (byte) (n >> 16 & 0xff);
		b[3] = (byte) (n >> 24 & 0xff);
		return b;
	}

	/** 将低字节数组转换为int */
	public static int lBytesToInt(byte[] b) {
		int s = 0;
		for (int i = 0; i < 3; i++) {
			if (b[3 - i] >= 0) {
				s = s + b[3 - i];
			} else {
				s = s + 256 + b[3 - i];
			}
			s = s * 256;
		}
		if (b[0] >= 0) {
			s = s + b[0];
		} else {
			s = s + 256 + b[0];
		}
		return s;
	}

	/**
	 * 将int类型的值转换为字节序颠倒过来对应的int值
	 * 
	 * @param i
	 *            int
	 * @return int
	 */
	// public static int reverseInt(int i) {
	// int result = hBytesToInt(toLH(i));
	// return result;
	// }

	public static int hBytesToInt(byte[] b) {
		int s = 0;
		for (int i = 0; i < 3; i++) {
			if (b[0] >= 0) {
				s = s + b[0];
			} else {
				s = s + 256 + b[0];
			}
			s = s * 256;
		}
		if (b[3] >= 0) {
			s = s + b[3];
		} else {
			s = s + 256 + b[3];
		}
		return s;

	}

	public static GifTransfer gInstance;

	public static GifTransfer Init(GSServerProp gsm, GSLogonPara para) {
		gInstance = new GifTransfer(gsm, para);
		return gInstance;
	}

	public static byte[] readBytes(InputStream in, long length)
			throws IOException {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int read = 0;
		while (read < length) {
			int cur = in.read(buffer, 0, (int) Math.min(1024, length - read));
			if (cur < 0) {
				break;
			}
			read += cur;
			bo.write(buffer, 0, cur);
		}
		return bo.toByteArray();
	}

	public static void AsynRead(InputStream in, int length, byte[] buffer)
			throws IOException {
		int read = 0;
		int remain_length = length;
		while (read < length) {
			int cur = in.read(buffer, read, remain_length);
			// 0 8204
			// 3744 4460
			// 0 8204 , 3744 4460 , 4000 4204 ,
			if (cur < 0) {
				break;
			}
			read += cur;
			remain_length = length - read;
			if (Utils.debug) {
				// Log.e(TAG, "2015/1/30 cur :" + cur + ",length :" + length
				// + ",read :" + read + Arrays.toString(buffer));
			}
		}
	}

	public static int toInt(byte[] bRefArr) {
		int iOutcome = 0;
		byte bLoop;
		for (int i = 0; i < bRefArr.length; i++) {
			bLoop = bRefArr[i];
			iOutcome += (bLoop & 0xFF) << (8 * i);
		}
		return iOutcome;
	}

	public boolean clearTasks(String TaskId) {
		boolean mbResult = mDownTaskQueue.cancelTask(TaskId, true);
		if (mbResult) {
			return true;
		} else {
			mbResult = mDownTaskQueue.removeTask(TaskId);
		}
		if (Utils.debug) {
			if (mbResult) {
				Log.i(TAG, "delete cur task successfully" + ",and TaskId :"
						+ TaskId);
			} else {
				Log.i(TAG, "delete cur task fail" + ",and TaskId :" + TaskId);
			}
		}
		return mbResult;
	}

}
