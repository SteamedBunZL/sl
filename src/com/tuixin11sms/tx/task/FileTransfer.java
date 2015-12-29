package com.tuixin11sms.tx.task;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.LinkedList;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.utils.Utils;

public class FileTransfer {
	private static final String TAG = "FileTransfer";
	public static final int TS_VERSION = 3;
	public static final int TS_VERSION_BIG_FILE = 4;

	/** 登录TS或TSM请求的协议号 */
	public static final int TS_LOGIN = 1;
	/** 下载文件登录TSM的协议号 */
	public static final int DOWNLOAD_TSM_LOGIN = 2;
	/** 登录TS响应的协议号 */
	public static final int TS_LOGIN_RSP = 2;
	/** 登录TSM响应的协议号 */
	public static final int TSM_LOGIN_RSP = 3;

	/******************** 图片、音频、名片等非纯文本消息的协议 **********************/
	/** 传输文件请求的协议号 */
	public static final int TS_START_UPLOAD = 3;
	/** 传输文件响应的协议号 */
	public static final int TS_START_UPLOAD_RSP = 4;
	/** 上传文件的协议号 */
	public static final int TS_UPLOAD_DATA = 5;
	/** 上传结束的协议号 */
	public static final int TS_UPLOAD_FINISH = 6;
	/** Server对“上传结束协议号”的响应 */
	public static final int TS_UPLOAD_FINISH_RSP = 7;
	/** 下载文件请求的协议号 */
	public static final int TS_START_DOWNLOAD = 50;
	/** 下载文件响应的协议号 */
	public static final int TS_START_DOWNLOAD_RSP = 51;
	/** Server向client传输文件的协议号 */
	public static final int TS_DOWNLOAD_DATA = 52;
	/** Server通知客户端文件传输完毕的协议号 */
	public static final int TS_DOWNLOAD_FINISH = 53;
	/** 新加协议 */
	public static final int TS_UPLOAD_DATA_RSP = 150;
	/** 请求续传文件协议 */
	public static final int TS_RE_UPLOAD_DATA = 151;

	/*********************** 大文件上传下载协议号 *********************************/

	/** 心跳包协议号，client每30秒给server发一次 */
	public static final int TS_PING = 100;

	/** 传输大文件请求协议号 */
	public static final int TS_START_UPLOAD_BIG = 401;
	/** 传输大文件响应协议号 */
	public static final int TS_START_UPLOAD_BIG_RSP = 402;
	/** 客户端开始传输大文件内容协议号 */
	public static final int TS_UPLOAD_DATA_BIG = 403;
	/** 服务器对客户端传输大文件数据内容响应协议号 */
	public static final int TS_UPLOAD_DATA_BIG_RSP = 404;
	/** 客户端上传大文件结束的协议号 */
	public static final int TS_UPLOAD_FINISH_BIG = 405;
	/** 服务器对客户端上传大文件结束响应的协议号 */
	public static final int TS_UPLOAD_FINISH_BIG_RSP = 406;

	/** 下载大文件请求的协议号 */
	public static final int TS_START_DOWNLOAD_BIG = 450;
	/** Server对下载大文件响应的协议号 */
	public static final int TS_START_DOWNLOAD_BIG_RSP = 451;
	/** Server向client传输大文件的协议号 */
	public static final int TS_DOWNLOAD_DATA_BIG = 452;
	/** client应答Server大文件传输内容的协议号 */
	public static final int TS_DOWNLOAD_DATA_BIG_RSP = 453;
	/** Server通知client大文件传输完毕的协议号 */
	public static final int TS_DOWNLOAD_FINISH_BIG = 454;

	/********************** 服务器应答码 ********************************/
	// //0:成功应答码
	// private final int RSP_SUCCESS = 0;
	// 26:上传文件偏移不对
	private final int UPLOAD_INVALID_FILE_OFFSET = 26;
	// 27:接收失败
	private final int UPLOAD_RECEIVE_FAILED = 27;
	// 29:上传文件大小超出限制（此文件类型暂定>20M）
	private final int UPLOAD_BEYOND_MAX_FILE_SIZE = 29;
	// 30:文件类型不对，现在此种文件类型仅有一种文件类型。
	private final int UPLOAD_INVALID_FILE_TYPE = 30;
	// 31:文件已经上传完毕，不需要上传了
	private final int UPLOAD_FILE_FINISH = 31;
	// 32:此文件名url为新建立的文件，非断点续传
	private final int UPLOAD_INVALID_CONTINUE_URL = 32;

	// 20:正在接收
	private final int DOWNLOAD_RECEIVING = 20;
	// 21:文件不存在
	private final int DOWNLOAD_FILE_NOT_EXIST = 21;
	// 22:下载时（或上传续传时）文件打开失败
	private final int SERVER_FILE_OPEN_FAILED = 22;
	// 23:请求的位置错误
	private final int DOWNLOAD_INVALID_FILE_POSITION = 23;
	// 0继续下载
	private final int DOWNLOAD_CONTINUE = 0;
	// 1移动指针
	private final int DOWNLOAD_MOVE_POINT = 1;
	// 2:中止（服务器会补发Prot454，结果码为1）
	private final int DOWNLOAD_ABORT = 2;
	// 33:下载或上传过程终止
	private final int TRANSFER_ABORTED = 33;

	// 下载的URL非法
	public static final int ERR_URL_INVALID = -1;
	// static final int RRES_DOWNLOAD_RECVING = 20;
	// static final int RRES_DOWNLOAD_NOSUCHFILE = 21;
	// static final int RRES_DOWNLOAD_FILEOPENERR = 22;
	// static final int RRES_DOWNLOAD_INVALIDPOS = 23;

	/** 文件类型 */
	public static final int FILE_TYPE_DEFAULT = 0;
	public static final int FILE_TYPE_VCARD = 1;
	public static final int FILE_TYPE_IMAGE = 2;
	public static final int FILE_TYPE_AUDIO = 3;
	public static final int FILE_TYPE_AVATAR = 4;
	public static final int FILE_TYPE_BIG_FILE = 5;// 大文件类型，最大20M
	public static final int FILE_TYPE_GIF = 6;

	/** TSM/TS登录用TOKEN，由神聊登录提供 */
	public static class TSLogonPara {
		public int uid;// = 3943614;用setUserToken方法传递的uid 2013.10.09 shc
		public int tid = 2; // 固定
		public int cver = Utils.appver; //
		public int ver = TS_VERSION;
		public int osid = 600; // 固定
		public int cid = Utils.getCid();
		public byte[] tokenBytes = null;// "8d1b472d70bc9ead85fb3c0a68408113".getBytes();
										// 用setUserToken方法传递的token 2013.10.09
										// shc
		public final Object mNetNotifier = new Object();

		// 登录成功后调此方法
		public void setUserToken(int uid, byte[] token) {
			this.uid = uid;
			tokenBytes = token;
		}
	}

	// 外部传入
	final TSLogonPara mLogonPara;
	final TSServerProp mTSMSvr;

	public static class TSServerProp {
		public String mServer;
		public int mPort;
		/** 超时时间，毫秒数, 防止socket状态正常，但网络无响应，WIFI网络经常发生这种情况 */
		public int mTimeout;
		public boolean bSvrValid = false;

		public TSServerProp() {
			mServer = "";
			mPort = 0;
			mTimeout = 20000;
		}

		public TSServerProp(String svr, int port, int timeout) {
			mServer = svr;
			mPort = port;
			mTimeout = timeout;
		}
	}

	volatile boolean mAccessTSM = true;

	static class FileUrlParts {
		String mServer;
		int mPort;
		String mPath;
		int mTime;

		boolean parseUrl(String url) {
			String[] parts = url.split(":");
			if (parts.length == 4) {
				mServer = parts[0];
				mPort = Integer.valueOf(parts[1]);
				mPath = parts[2];
				try {
					mTime = Integer.parseInt(parts[3]);
				} catch (Exception e) {
					mTime = (int) (System.currentTimeMillis() / 1000);
				}
				return true;
			}
			return false;
		}

		String toUrl() {
			return mServer + ":" + mPort + ":" + mPath + ":" + mTime;
		}
	}

	static class ProtContext {
		public final TSServerProp tsSvr;
		public final TSServerProp subSrv;

		public final TSLogonPara mLogonPara;

		DataOutputStream mDOS; // 可能是socket，也可能不是
		DataInputStream mDIS; // 可能是socket, 也可能不是

		ProtContext(TSLogonPara para, TSServerProp svr, TSServerProp subSvr) {
			mLogonPara = para;
			tsSvr = svr;
			this.subSrv = subSvr;
		}

		int mRecvLen, mRecvMT;
		int mSendLen, mSendMT;

		public void prepSend() {
			mSendLen = 0;
			mSendMT = 0;
		}

		public void prepRecv() throws IOException {
			if (mRecvLen > 0x40000)
				throw new IOException("Invalid data format");
			if (mRecvBuff == null || mRecvLen > mRecvBuff.length)
				mRecvBuff = new byte[mRecvLen];
			//
			mDIS.readFully(mRecvBuff, 0, mRecvLen);
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
			mDOS.writeInt(len);
			mDOS.writeInt(mt);
			mSendLen = len;
			mSendMT = mt;
		}

		/** 协议buffer */
		public byte[] mRecvBuff;
		// 发送buff，只所以在此申请，是为了便于协议限制
		public byte[] mSendBuff;
		public DataInputStream mtIS;

		// 目前不需要发送buffer和stream
		public String readMTString(int iPos) throws IOException {
			int len = iPos;
			while (mRecvBuff[len] != 0 && len < mRecvLen) {
				len++;
			}
			len -= iPos;
			mtIS.skip(len + 1);
			return new String(mRecvBuff, iPos, len);
		}
	}

	// 这些变量没有引用到，先注释掉，用到在开启
	// // 任务状态
	// public static final int DUSTA_SUCCESS = 0;
	// public static final int DUSTA_WAITING = 1;
	// public static final int DUSTA_ONGOING = 2;
	// /** 网络错,可重试 */
	// public static final int DUSTA_NETIOERROR = 3;
	// /** 文件错,不自动重试 */
	// public static final int DUSTA_FILEIOERROR = 4;
	// /** 资源无效,不自动重试 */
	// public static final int DUSTA_INVALIDRESOURCE = 5;
	// /** 无此资源或资源有错,不自动重试 */
	// public static final int DUSTA_INTERNALERROR = 6;
	//
	// static final int XFR_DOWNLOAD = 1;
	// static final int XFR_UPLOAD = 2;
	/** 图片文件的文件头大小 */
	static final int IMGFILE_HEAD_SIZE = 24;

	/** 断网重连 */
	public static final int TASK_RES_RESET_NET = 2;
	/** 换TS重连 */
	public static final int TASK_RES_CHGTS = 4;

	public static class FileTaskInfo {
		public String mFullName;
		public String mUrl;
		public String mServerHost;// 新添加字段，上传任务服务器ip地址和端口
		public String mPath;
		public int mTime;
		public int mFileType;
		public boolean mAppend;
		public int mExtra; // 大图小图
		public long mSrcId; // 用户id，group id等
		public Object mObj; // 附加数据

		public FileTaskInfo() {
		}

		public FileTaskInfo(String url, String name, int type, boolean bAppend,
				int extra, Object obj) {
			mFullName = name;
			mUrl = url;
			mFileType = type;
			mAppend = bAppend;
			mExtra = extra;
			mObj = obj;
		}
	}

	static class ImageTaskInfo extends FileTaskInfo {
		public ImageTaskInfo(String name, String url, boolean bSmall,
				boolean bAppend) {
			super(name, url, FILE_TYPE_IMAGE, bAppend, bSmall ? 0 : 1, null);
		}
	}

	static class AvatarTaskInfo extends FileTaskInfo {
		public AvatarTaskInfo(String name, String url, boolean bSmall,
				boolean bAppend) {
			super(name, url, FILE_TYPE_AVATAR, bAppend, bSmall ? 0 : 1, null);
		}
	}

	static class UpTaskInfo extends FileTaskInfo {
		public UpTaskInfo(String name, String urlPath, int iType, Object obj) {
			super(name, urlPath, iType, urlPath != null, 0, obj);
		}
	}

	public static abstract class DownUploadListner {
		public void onStart(FileTaskInfo taskInfo) {
		};

		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
		};

		public abstract void onFinish(FileTaskInfo taskInfo);

		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
		};
	}

	static abstract class MTTaskBase extends FileTaskInfo {
		/** 成功 */
		public static final int RES_SUCCESS = 0;
		/** 不可恢复错误，任务终止 */
		public static final int RES_CRITICALERR = 3;

		/** 下载文件超时时间20s */
		public static final int DOWN_FILE_TIME_OUT = 20 * 1000;

		protected File mFile;// 上传下载任务使用的file对象，可能是一个临时文件

		public MTTaskBase(String url, String name, int type, boolean bAppend,
				int extra, Object obj) {
			super(url, name, type, bAppend, extra, obj);
		}

		public MTTaskBase() {
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

		DownUploadListner mListner;

		protected void finish() {
			if (!mbDone) {
				mbDone = true;
				if (mListner != null)
					mListner.onFinish(this);
			}
		}

		// 根据重试次数判定是否再次执行
		protected void retry(int iCode, Object para) {
			if (Utils.debug) {
				if (para instanceof Exception) {
					if (Utils.debug)
						Log.w(TAG, "上传下载时异常retry--errorCode:" + iCode,
								(Exception) para);
				} else {
					if (Utils.debug)
						Log.w(TAG, "retry其他错误信息--errorCode:" + iCode + "---"
								+ para);
				}
			}
			if (mTryTimes > 0) {
				mTryTimes--;
				if (Utils.debug)
					Log.e(TAG, "重试了一次。还有" + mTryTimes + "次");
			} else {
				if (Utils.debug)
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

		public abstract void onReconnected(ProtContext protCtx);

		public abstract int onSend(ProtContext protCtx) throws IOException;

		public abstract int onRecv(ProtContext protCtx) throws IOException;

		public int onCancel() {
			finish(DUERR_CANCELLED, null);
			return 0;
		}

		public boolean isDone() {
			// 任务结束了，不一定是正常完成了
			return mbDone;
		};

		// 协议封解包在此处理
		int reqTSMLogon_Download(ProtContext ctx, String tsUrl, int times)
				throws IOException {
			byte[] urlBytes = tsUrl.getBytes();
			ctx.sendMT(ctx.mLogonPara.tokenBytes.length + urlBytes.length + 4
					* 4 + 2, DOWNLOAD_TSM_LOGIN);
			ctx.mDOS.writeInt(ctx.mLogonPara.uid);
			ctx.mDOS.writeInt(ctx.mLogonPara.cver);
			ctx.mDOS.writeInt(ctx.mLogonPara.ver);
			ctx.mDOS.write(ctx.mLogonPara.tokenBytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.write(urlBytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(times);
			return RES_SUCCESS;
		}

		int reqTSMLogon_Upload(ProtContext ctx) throws IOException {
			ctx.sendMT(ctx.mLogonPara.tokenBytes.length + 13, TS_LOGIN);
			ctx.mDOS.writeInt(ctx.mLogonPara.uid);
			ctx.mDOS.writeInt(ctx.mLogonPara.cver);
			ctx.mDOS.writeInt(ctx.mLogonPara.ver);
			ctx.mDOS.write(ctx.mLogonPara.tokenBytes);
			ctx.mDOS.writeByte(0);
			return RES_SUCCESS;
		}

		int reqTSLogon(ProtContext ctx) throws IOException {
			ctx.sendMT(ctx.mLogonPara.tokenBytes.length + 25, TS_LOGIN);
			ctx.mDOS.writeInt(ctx.mLogonPara.uid); // list.add(uidBytes);
			ctx.mDOS.writeInt(ctx.mLogonPara.tid); // list.add(tidBytes);
			ctx.mDOS.writeInt(ctx.mLogonPara.cver); // list.add(cverBytes);
			ctx.mDOS.writeInt(ctx.mLogonPara.ver); // list.add(verBytes);
			ctx.mDOS.writeInt(ctx.mLogonPara.osid); // list.add(osidBytes);
			ctx.mDOS.writeInt(ctx.mLogonPara.cid); // list.add(cidBytes);
			ctx.mDOS.write(ctx.mLogonPara.tokenBytes); // list.add(tokenBytes);
			ctx.mDOS.writeByte(0);
			return RES_SUCCESS;
		}

		int reqStartDownload(ProtContext ctx, String path, int start, int len)
				throws IOException {
			if (Utils.debug)
				Log.i(TAG, "reqStartDownload:Path=" + path + ",pos=" + start
						+ ",reqsize=" + len);
			byte[] pathBytes = path.getBytes();
			ctx.sendMT(pathBytes.length + 13, TS_START_DOWNLOAD);
			ctx.mDOS.write(pathBytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(start);
			ctx.mDOS.writeInt(len);
			ctx.mDOS.writeInt(ctx.mLogonPara.uid);
			return RES_SUCCESS;
		}

		/*
		 * 协议3： 文件名：string， 文件类型:uint 文件大小:uint 文件MD5(文件唯一标识，“1”表示无)string
		 * 接收方uid:uint 返回：位置:uint, 路径:String, 时间:uint
		 */
		int reqStartUpload(ProtContext ctx, String name, int filetype,
				int filesize, String md5, int recvuid) throws IOException {
			byte[] nameByes = name.getBytes();// 如果为续传的话，name为文件在服务器的地址，如果非续传，则只为文件名
			byte[] md5Bytes = md5.getBytes();
			// 如果未续传，则用151号协议请求，否则用3号协议请求
			if (mAppend == true) {
				if (Utils.debug) {
					Log.i(TAG, "reqStartUpload--->续传文件");
				}
			}
			ctx.sendMT(nameByes.length + md5Bytes.length + 14,
					mAppend ? TS_RE_UPLOAD_DATA : TS_START_UPLOAD);
			ctx.mDOS.write(nameByes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(filetype);
			ctx.mDOS.writeInt(filesize);
			ctx.mDOS.write(md5Bytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(recvuid);
			return RES_SUCCESS;
		}

	}

	class TSMLogonTask extends MTTaskBase {
		int mStep = 0;
		// 登录结果：
		// 0=成功，1版本太低，2超出最大连接数，客户端主动关闭连接，3=认证失败
		int miStatus = -1;
		boolean mbDownload = false;
		String mTSUrl;
		int mTimes = 1;

		/** 上传用 */
		TSMLogonTask() {
			mbDownload = false;
		}

		/** 下载用 */
		TSMLogonTask(String url, int times) {
			super();
			mTSUrl = url;
			mTimes = times;
			mbDownload = false;
		}

		public void reset() {
			mStep = 0;
			miStatus = -1;
			super.reset();
		}

		@Override
		public void onReconnected(ProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			int iRet = 0;
			switch (mStep) {
			case 0:
				if (mbDownload) { // 下载要传URL，由URL确定优先的服务器
					reqTSMLogon_Download(ctx, mTSUrl, mTimes);
				} else { // 上传
					reqTSMLogon_Upload(ctx);
				}
				mStep = 1;
				break;
			case 1:
				// 等待回应
				break;
			}
			return iRet;
		}

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			if (ctx.mRecvMT != TSM_LOGIN_RSP)
				return 0; // 不处理
			int iRet = 0;
			switch (mStep) {
			case 0:
				break;
			case 1:
				miStatus = ctx.mtIS.readInt();
				if (miStatus == 0) {
					String tsUrl = new String(ctx.mRecvBuff, 4,
							ctx.mRecvLen - 5);
					String[] bodies = tsUrl.split(":");
					if (bodies.length >= 2) {
						String host = bodies[0];
						int port = 0;
						if (!Utils.isNull(bodies[1])) {
							port = Integer.parseInt(bodies[1]);
						}
						ctx.subSrv.mServer = host;
						ctx.subSrv.mPort = port;
						ctx.subSrv.bSvrValid = true;
					}
					finish();
				} else
					iRet = TASK_RES_CHGTS;
				break;
			}
			return iRet;
		}
	};

	class TSLogonTask extends MTTaskBase {
		int miStatus = -1;
		int mStep = 0;

		public void reset() {
			miStatus = -1;
			mStep = 0;
			super.reset();
		}

		public TSLogonTask() {
		}

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			int iRet = 0;
			if (mStep == 0) {
				reqTSLogon(ctx);
				mStep++;
			}
			return iRet;
		}

		@Override
		public void onReconnected(ProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			if (ctx.mRecvMT != TS_LOGIN_RSP)
				return 0; // 不处理
			finish();
			miStatus = ctx.mtIS.readInt();
			if (miStatus != 0)
				throw new IOException("Server reject.");
			return 0;
		}
	};

	public static final int DOWNERR_IOEXCEPTION = 1;
	public static final int DUERR_SERVERREJECT = 2;
	public static final int DUERR_INVALIDPARA = 3;
	public static final int DUERR_CANCELLED = 4;

	// 这些变量没有引用到，先注释掉，用到在开启
	// static final int PROT_TSM_LOGON = 1;
	// // 文件发送方： 客户端id:uint; 客户端版本:uint; 协议版本:uint, Token:string
	// // 文件接收方： 客户端id:uint; 客户端版本:uint; 协议版本:uint, Token:string，TS地址:String,
	// // 时间:uint
	// static final int PROT_TSM_LOGON_RSP = 2;
	// // 状态：uint(0成功，1版本太低，3：认证失败, 域名:String
	// // 客户端关闭连接 ?
	// static final int PROT_TS_LOGON = 1;
	// // 推信号: uint, 组id:uint, 客户端版本

	private String getRequestResult(int code) {
		switch (code) {
		case 0:
			return "成功";
		case DOWNLOAD_RECEIVING:
			return "文件接收中";
		case DOWNLOAD_FILE_NOT_EXIST:
			return "文件不存在";
		case SERVER_FILE_OPEN_FAILED:
			return "文件打开错误";
		case DOWNLOAD_INVALID_FILE_POSITION:
			return "文件位置错误";
		case UPLOAD_INVALID_FILE_OFFSET:
			return "上传文件偏移不对";
		case UPLOAD_RECEIVE_FAILED:
			return "接收失败";
		case UPLOAD_BEYOND_MAX_FILE_SIZE:
			return "上传文件大小超出限制（此文件类型暂定>20M）";
		case UPLOAD_INVALID_FILE_TYPE:
			return "文件类型不对，现在此种文件类型仅有一种文件类型";
		case UPLOAD_FILE_FINISH:
			return "文件已经上传完毕，不需要上传了";
		case UPLOAD_INVALID_CONTINUE_URL:
			return "此文件名url为新建立的文件，非断点续传";
		default:
			return "未知错误";
		}
	}

	class DownloadTask extends MTTaskBase {
		int mStep = 0;

		protected int mDownloadFileLength; // 要下载的文件长度

		protected long doTaskTime = 0;// 任务被执行的时间戳，如果超过20s没有执行写操作则重置任务

		int mReqPos; // 文件位置
		int mReqSize; // 请求的总长度，0表示所有

		int mLenDone;

		// File mFile;//用父类MTTaskBase的mFile对象
		OutputStream mFileOS;

		public void reset() {
			mStep = 0;
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

		public DownloadTask(String url, String fullName, int type,
				boolean bAppend, DownUploadListner listner) {
			super(url, fullName, type, bAppend, 0, null);
			if (Utils.debug) {
				Log.i(TAG, "下载文件地址为：" + url);
			}
			try {
				String[] parts = url.split(":");
				mTime = Integer.valueOf(parts[3]);
				mPath = parts[2];
				// mTaskId = mPath + mTime;
				mTaskId = getDownloadTaskId(url);
				if (fullName == null || fullName.equals("")) {
					fullName = parts[2];
				}
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

		int onStartDownloadRsp(ProtContext ctx) throws IOException {
			int statusCode = ctx.mtIS.readInt();
			if (statusCode != 0) {
				// TODO: 需要区分是什么原因，决定是否重试，现在一律不重试
				finish(DUERR_SERVERREJECT, (Integer) statusCode);
				if (Utils.debug) {
					Log.i(TAG, "下载出错：" + getRequestResult(statusCode));
				}
			}
			return RES_SUCCESS;
		}

		@Override
		public void onReconnected(ProtContext protCtx) {
			mStep = 0;
		}

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			doTaskTime = System.currentTimeMillis();

			int iRet = RES_SUCCESS;
			try {
				switch (mStep) {
				case 0: // 请求下载
					reqStartDownload(ctx, mPath, mReqPos, mReqSize);
					mStep++;
					break;
				}
			} catch (IOException e) {
				iRet = TASK_RES_RESET_NET;
				retry(DOWNERR_IOEXCEPTION, e);
			}
			return iRet;
		}

		int testFlag = 0;

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			int iRet = 0;
			int pos, size;
			try {
				switch (ctx.mRecvMT) {
				case TS_START_DOWNLOAD_RSP:
					iRet = onStartDownloadRsp(ctx);
					break;
				case TS_DOWNLOAD_DATA: // 服务器持续提供下行数据，无需要等客户端的请求
					pos = ctx.mtIS.readInt();
					size = ctx.mtIS.readInt();
					// TODO: 异常检查 也许会出现ArrayIndexOutOfBoundsException
					writeFile(ctx.mRecvBuff, 8, size);
					if (mListner != null)
						mListner.onProgress(this, mLenDone, mReqSize);
					if (Utils.debug) {
						Log.i(TAG, "TS_DOWNLOAD_DATA:pos=" + pos + ",size="
								+ size);
					}
					if (Utils.debug) {
						// 测试图片的断点续传，当testFlag==6时断网一次
						if (testFlag == 3) {
							// iRet = TASK_RES_RESET_NET;
						}
						testFlag++;
					}
					break;
				case TS_DOWNLOAD_FINISH:
					if (Utils.debug) {
						Log.i(TAG, "TS_DOWNLOAD_FINISH");
					}
					closeFile();
					mFile.renameTo(new File(mFullName));
					finish();
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

	}

	class DownImageTask extends DownloadTask {
		int mVer;
		int mTotalSize;

		boolean mbBigImage = false;
		int mFileOff;

		int mImgOff, mImgSize;

		public DownImageTask(boolean bBigImage, String url, String file,
				int type, boolean bAppend, DownUploadListner listner) {
			super(url, file, type, bAppend, listner);
			mbBigImage = bBigImage;
			// mTaskId += mbBigImage ? ".1" : ".0";
			mTaskId = getDownLoadImageTaskId(url, mbBigImage);
		}

		/**
		 * 每次下载图片请求前获取一下当前文件的大小，为断点续传做偏移使用
		 * 
		 * @throws IOException
		 */
		public void onPrepair() throws IOException {
			if (Utils.debug) {
				Log.i(TAG, "准备续传下载图片");
			}
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

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			doTaskTime = System.currentTimeMillis();
			if (mAppend && mImgOff > 0 && mStep == 0) {
				// 在要求断点续传、并且mImgOff值大于0(即已经获取大小图偏移)、并且是第一次请求（重新请求）下载的前提下，去准备文件大小
				onPrepair();
			}
			int iRet = 0;
			try {
				switch (mStep) {
				case 0: // 请求下载
					reqStartDownload(ctx, mPath, 0, IMGFILE_HEAD_SIZE);
					mStep++;
					break;
				case 1: // 等待接收
					break;
				case 2: // 发送下载请求
					// TODO: 异常检查
					reqStartDownload(ctx, mPath, mImgOff + mFileOff,
							(mImgSize - mFileOff) <= 0 ? 0
									: (mImgSize - mFileOff));// 请求长度最小为0
					mStep++;
				case 3: // 等待接收
					break;
				}
			} catch (IOException e) {
				retry(DOWNERR_IOEXCEPTION, e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			int iRet = 0;
			try {
				switch (mStep) {
				case 1:
					switch (ctx.mRecvMT) {
					case TS_START_DOWNLOAD_RSP:
						if (Utils.debug) {
							Log.i(TAG, "TS_START_DOWNLOAD_RSP");
						}
						iRet = onStartDownloadRsp(ctx);
						// 后面紧接TS_DOWNLOAD_DATA
						break;
					case TS_DOWNLOAD_DATA: // 服务器持续提供下行数据，无需要等客户端的请求
						// pos=ctx.mtIS.readInt();size=ctx.mtIS.readInt();
						if (Utils.debug) {
							Log.i(TAG, "TS_DOWNLOAD_DATA");
						}
						ctx.mtIS.skip(8);
						mVer = ctx.mtIS.readInt();
						mTotalSize = ctx.mtIS.readInt();
						if (mbBigImage) {
							if (Utils.debug) {
								Log.i(TAG, "下载大图图片");
							}
							ctx.mtIS.skip(8);
						}
						mImgOff = ctx.mtIS.readInt();
						mImgSize = ctx.mtIS.readInt();
						mReqSize = mImgSize;// 把图片总大小设置给DownloadTask的mReqSize,用于下载进度显示时使用
						if (Utils.debug) {
							Log.i(TAG, "图片位置ImgOff:" + mImgOff + ",ImgSize:"
									+ mImgSize);
						}
						break;
					case TS_DOWNLOAD_FINISH:
						if (Utils.debug) {
							Log.i(TAG, "TS_DOWNLOAD_FINISH");
						}
						// 必须等,否则转下步直接关文件了
						mStep++;
						break;
					}
					break;
				case 2:
					break;
				case 3: // 真正开始下载文件
					iRet = super.onRecv(ctx);
					break;
				}
			} catch (IOException e) {
				try {
					closeFile();
					retry(DOWNERR_IOEXCEPTION, e);
				} catch (IOException e2) {
					if (Utils.debug) {
						Log.w(TAG, "下载图片关闭流异常", e2);
					}
					finish(DOWNERR_IOEXCEPTION, e2);
				}
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}
	};

	/** 下载大文件，用大文件新协议 */
	public class DownloadBigFileTask extends DownloadTask {
		private int pos;// 下载的到的数据的位置
		private int size;// 下载到的数据长度
		private int msgFileLength;// 消息中携带的文件长度

		public DownloadBigFileTask(String url, int fileLength,
				DownUploadListner listner) {
			super(url, null, FILE_TYPE_BIG_FILE, true, listner);
			msgFileLength = fileLength;
			if (Utils.debug) {
				Log.i(TAG, "下载文件地址为：" + url);
			}
			try {
				mFullName = getDownLoadBigFilePath(url);
				if (TextUtils.isEmpty(mFullName)) {
					mFullName = mPath;
				}
				File tempFile = new File(mFullName);
				File tempFolder = new File(tempFile.getParent() + "/temp/");
				if (!tempFolder.exists()) {
					tempFolder.mkdirs();
				}
				mFile = new File(tempFolder, tempFile.getName() + "."
						+ new MD5().getMD5ofStr(url));// 用url的md5作为临时文件的后缀
				if (Utils.debug)
					Log.i(TAG, "临时文件地址为：" + mFile.getAbsolutePath());
			} catch (NumberFormatException e) {
				mbTaskValid = false;
			} catch (ArrayIndexOutOfBoundsException e) {
				mbTaskValid = false;
			}
		}

		@Override
		int reqStartDownload(ProtContext ctx, String path, int start, int len)
				throws IOException {
			if (Utils.debug)
				Log.i(TAG, "reqStartDownload:Path=" + path + ",pos=" + start
						+ ",reqsize=" + len);
			byte[] pathBytes = path.getBytes();
			ctx.sendMT(pathBytes.length + 13, TS_START_DOWNLOAD_BIG);
			ctx.mDOS.write(pathBytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(start);
			ctx.mDOS.writeInt(len);
			ctx.mDOS.writeInt(ctx.mLogonPara.uid);
			return RES_SUCCESS;
		}

		@Override
		int onStartDownloadRsp(ProtContext ctx) throws IOException {
			mDownloadFileLength = ctx.mtIS.readInt();
			int statusCode = ctx.mtIS.readInt();
			if (statusCode != 0) {
				// TODO: 需要区分是什么原因，决定是否重试，现在一律不重试
				finish(DUERR_SERVERREJECT, (Integer) statusCode);
				if (Utils.debug) {
					Log.i(TAG, "下载出错：" + getRequestResult(statusCode));
				}
			}
			if (statusCode == 0 && mListner != null) {
				mListner.onStart(this);
			}
			return RES_SUCCESS;
		}

		/** 下载文件时给server的实时回执 */
		int rspDownloadData(ProtContext ctx, int start, int len,
				int nextPosition, int reqCode) throws IOException {
			if (Utils.debug)
				Log.i(TAG, "rspDownloadData:pos=" + start + ",receivedSize="
						+ len + ",nextPosition=" + nextPosition + ",reqCode="
						+ reqCode);
			ctx.sendMT(16, TS_DOWNLOAD_DATA_BIG_RSP);
			ctx.mDOS.writeInt(start);
			ctx.mDOS.writeInt(len);
			ctx.mDOS.writeInt(nextPosition);
			ctx.mDOS.writeInt(reqCode);
			return RES_SUCCESS;
		}

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			doTaskTime = System.currentTimeMillis();

			int iRet = RES_SUCCESS;
			try {
				switch (mStep) {
				case 0: // 请求下载
					if (mFile.exists()) {
						// 如果该临时文件存在，则进行断点下载
						mReqPos = (int) mFile.length();
						if (Utils.debug)
							Log.i(TAG, "请求下载的文件本地长度：" + mReqPos);
					}
					reqStartDownload(ctx, mPath, mReqPos, mReqSize);
					mStep++;
					break;
				case 1: // 这case好像没用，要不去掉？
				case 2: // 给服务发送已接受到下载数据的回执
					rspDownloadData(ctx, pos, size, pos + size,
							DOWNLOAD_CONTINUE);
					break;
				}
			} catch (IOException e) {
				iRet = TASK_RES_RESET_NET;
				retry(DOWNERR_IOEXCEPTION, e);
			}
			return iRet;
		}

		int testFlag = 0;

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			int iRet = 0;
			// int pos, size;
			try {
				switch (ctx.mRecvMT) {
				case TS_START_DOWNLOAD_BIG_RSP:
					iRet = onStartDownloadRsp(ctx);
					if (Utils.debug) {
						Log.i(TAG, "mReqPos = " + mReqPos
								+ ",mDownloadFileLength = "
								+ mDownloadFileLength);
					}
					if (msgFileLength != mDownloadFileLength) {
						// 消息中携带的文件大小和服务器返回的文件大小不一致，直接置任务失败
						closeFile();
						finish(DOWNERR_IOEXCEPTION, null);
						break;
					}

					if (mReqPos == mDownloadFileLength) {
						// 如果请求下载的文件位置等于服务器文件总长度，则直接执行结束操作
						if (Utils.debug) {
							Log.i(TAG, "TS_DOWNLOAD_FINISH---请求文件位置等于服务器文件总长度");
						}
						closeFile();

						mFullName = getValidName(mFullName);

						mFile.renameTo(new File(mFullName));
						finish();

					}
					break;
				case TS_DOWNLOAD_DATA_BIG: // 服务器持续提供下行数据，无需要等客户端的请求
					pos = ctx.mtIS.readInt();
					size = ctx.mtIS.readInt();
					// TODO: 异常检查 也许会出现ArrayIndexOutOfBoundsException
					writeFile(ctx.mRecvBuff, 8, size);
					if (mListner != null)
						mListner.onProgress(this, mReqPos + mLenDone,
								mDownloadFileLength);
					if (Utils.debug) {
						Log.i(TAG, "TS_DOWNLOAD_DATA:pos=" + pos + ",size="
								+ size + ",mReqPos = " + mReqPos
								+ ",mLenDone = " + mLenDone
								+ ",mDownloadFileLength = "
								+ mDownloadFileLength);
					}
					mStep = 2;
					if (Utils.debug) {
						// 测试图片的断点续传，当testFlag==6时断网一次
						if (testFlag == 3) {
							// iRet = TASK_RES_RESET_NET;
						}
						testFlag++;
					}
					break;
				case TS_DOWNLOAD_FINISH_BIG:
					if (Utils.debug) {
						Log.i(TAG, "TS_DOWNLOAD_FINISH");
					}
					closeFile();

					mFullName = getValidName(mFullName);

					mFile.renameTo(new File(mFullName));
					finish();
					break;
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "下载文件Exception:", e);
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

	/** 避免重名，生成正确的文件名 */
	private String getValidName(String mFullName) {

		// 判定下载的文件名是否有重名
		final String pureFileName;// 不带后缀扩展名的纯文件名
		String bigFileName = mFullName
				.substring(mFullName.lastIndexOf("/") + 1);
		if (bigFileName.contains(".")) {
			// 该文件名有后缀名
			pureFileName = bigFileName.substring(0,
					bigFileName.lastIndexOf("."));
		} else {
			pureFileName = bigFileName;
		}

		File folder = new File(getStoragePath(),
				getSaveDir(FileTransfer.FILE_TYPE_BIG_FILE));
		File folderFile = new File(folder.getAbsolutePath());
		File[] sameNameFiles = folderFile.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String filename) {
				// 判定该文件夹中是否有重复的文件名
				if (filename.contains(".")) {
					filename = filename.substring(0, filename.lastIndexOf("."));
				}
				if (filename.length() >= pureFileName.length()
						&& pureFileName.equals(filename.substring(0,
								pureFileName.length()))) {
					// 文件夹下的该文件名长度大于url中的文件名，并且url中的文件名和文件夹中的文件有重复
					return true;
				}
				return false;
			}
		});
		if (sameNameFiles != null && sameNameFiles.length > 0) {
			// 文件夹中有重复的文件名
			if (bigFileName.contains(".")) {
				bigFileName = pureFileName + "(" + sameNameFiles.length + ")"
						+ bigFileName.substring(bigFileName.lastIndexOf("."));
			} else {
				bigFileName = pureFileName + "(" + sameNameFiles.length + ")";
			}
		}
		mFullName = mFullName.substring(0, mFullName.lastIndexOf("/") + 1)
				+ bigFileName;

		return mFullName;
	}

	class UploadTask extends MTTaskBase {
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

		public UploadTask(String fullName, int fileType, boolean bAppend,
				DownUploadListner listner) {
			// mTaskId = fullName;//暂时以文件全路径为上传任务的id
			mTaskId = getUploadTaskId(fullName);
			mFile = new File(fullName);
			mAppend = bAppend;
			mListner = listner;
			mFileType = fileType;
			mPath = mFile.getName();
			mTryTimes = 5;
		}

		/**
		 * 续传文件的构造函数，去掉了mAppend，因为它一定为true,增加了fileUrl（文件在服务器的地址）
		 * 
		 * @param fullName
		 * @param fileType
		 * @param fileUrl
		 * @param listner
		 */
		public UploadTask(String fullName, int fileType, String fileUrl,
				DownUploadListner listner) {
			mFile = new File(fullName);
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

		int reqStartUpload(ProtContext ctx) throws IOException {
			if (Utils.debug) {
				Log.i(TAG, "UploadTask---reqStartUpload:" + mFile.getName());
			}
			return reqStartUpload(ctx, mPath, mFileType, mFileSize, "1",
					mTarUID);
		}

		/*
		 * 4号协议: 服务器没有拒绝的可能？ 传输位置：uint, 续传位置，目前为0 路径：string 时间：uint
		 * 增加一个字段：状态，可能拒绝
		 */
		int onStartUploadRsp(ProtContext ctx) throws IOException {
			int pos = ctx.mtIS.readInt();
			mServerHost = ctx.tsSvr.mServer + ":" + ctx.tsSvr.mPort;
			mPath = ctx.readMTString(4);
			mTime = ctx.mtIS.readInt();
			if (ctx.mtIS.available() >= 4)
				uploadRspErr = ctx.mtIS.readInt();// 服务器返回的错误码
			else
				uploadRspErr = 0;
			prepFile();
			if (mAppend) {
				mReqPos = pos;
				if (Utils.debug) {
					Log.i(TAG, "续传文件跳过" + mReqPos + "个字节续传");
				}
			}
			mFileIS.skip(mReqPos);
			if (mListner != null)
				mListner.onStart(this);
			if (Utils.debug) {
				Log.i(TAG, "UploadRequest.onRecv---TS_START_UPLOAD_RSP:"
						+ mPath + ",Pos=" + pos + ",time:" + mTime
						+ ",uploadRspErr:" + uploadRspErr);
			}
			return 0;
		}

		/*
		 * 位置，uint 大小：uint 内容：byte[]
		 */
		int reqUploadData(ProtContext ctx) throws IOException {
			byte[] buff = ctx.getSendBuff(0x1000);
			int len = readData(buff, 0, 0x1000);
			if (len <= 0) {
				ctx.sendMT(0, TS_UPLOAD_FINISH);
			} else {
				ctx.sendMT(len + 8, TS_UPLOAD_DATA);
				ctx.mDOS.writeInt(mReqPos);
				ctx.mDOS.writeInt(len);
				ctx.mDOS.write(buff, 0, len);
				mReqPos += len;
			}
			return 0;
		}

		int rspUploadData(ProtContext ctx) throws IOException {
			int pos = ctx.mtIS.readInt(); // 文件本地开始position
			int size = ctx.mtIS.readInt();// 此次上传的data长度
			int svrpos = ctx.mtIS.readInt();// 服务器文件的末尾位置，应该是svrpos = pos + size
			int status = ctx.mtIS.readInt();// 响应状态码
			if (Utils.debug) {
				Log.i(TAG, "TS_UPLOAD_DATA_RSP:pos=" + pos + ",size=" + size
						+ ",svrpos=" + svrpos + ",status=" + status);
			}
			switch (status) {
			case RES_SUCCESS:
				if (mListner != null)
					mListner.onProgress(this, pos + size, mFileSize);
				break;
			case UPLOAD_INVALID_FILE_OFFSET:// 上传文件偏移不对
			case UPLOAD_RECEIVE_FAILED:// 接收失败
			case UPLOAD_BEYOND_MAX_FILE_SIZE:// 上传文件大小超出限制
			default:
				finish(DUERR_SERVERREJECT, status);
				break;
			}
			return 0;
		}

		@Override
		public void onReconnected(ProtContext protCtx) {
			mStep = 0;
		}

		int testFlag = 0;// 测试断网续传的flag

		@Override
		public int onSend(ProtContext ctx) throws IOException {
			// mPath值不等于文件名(即已经获取需要上传到服务器存储的地址)、并且是第一次请求（重新请求）下载的前提下，去准备请求续传
			if (!mPath.equalsIgnoreCase(mFile.getName()) && mStep == 0) {
				mAppend = true;// 如果上传文件地址已经取到，则设置续传为true
			}

			int iRet = 0;
			try {
				switch (mStep) {
				case 0: // 请求上传
					iRet = reqStartUpload(ctx);
					mStep++;
					break;
				// 上传数据,结束是上传一个0长度包,是否会失败？
				case 1:
					break;
				case 2:
				case 3:
					iRet = reqUploadData(ctx);
					mStep++;
					if (Utils.debug) {
						if (testFlag == 5) {
							// iRet = TASK_RES_RESET_NET;//测试每次写数据都断网重连
						}
						testFlag++;
					}
					break;
				}
			} catch (IOException e) {
				retry(DOWNERR_IOEXCEPTION, e);
				if (Utils.debug)
					Log.e(TAG, "发送上传请求异常", e);
				iRet = TASK_RES_RESET_NET;
			}
			return iRet;
		}

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			int iRet = 0;
			int pos, size, svrpos, status;
			try {
				switch (ctx.mRecvMT) {
				case TS_START_UPLOAD_RSP:
					iRet = onStartUploadRsp(ctx);
					mStep = 2;
					if (uploadRspErr == 28) {
						// 28:断点续传文件信息不存在，需要重新使用命令3上传
						// 29:上传文件大小超出限制（按某某文件类型>2M需求待定）
						// 30:文件类型不对，现有文件类型请参考上面的上传命令3
						mStep = 0;// 重新开始请求上传
						mAppend = false;
						mPath = mFile.getName();
						if (Utils.debug) {
							Log.i(TAG, "续传失败，从新开始上传");
						}
					}
					break;
				case TS_UPLOAD_DATA_RSP: //
					iRet = rspUploadData(ctx);
					mStep--; // 继续发? 是否要缓冲？
					break;
				case TS_UPLOAD_FINISH_RSP:
					finish();
					closeFile();
					if (mFileType == FILE_TYPE_IMAGE
							|| mFileType == FILE_TYPE_AVATAR) {
						// 根据类型，删除合成的临时文件？
						mFile.delete();
					}
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
	 * 上传音频的任务
	 * 
	 * @author SHC
	 * 
	 */
	public class UploadAudioTask extends UploadTask {
		private ClientManager mAudioManager = null;

		public UploadAudioTask(ClientManager pcmAudioManager, String fileUrl,
				boolean bAppend, DownUploadListner listner) {
			super(pcmAudioManager.getAudioFilePath(), FILE_TYPE_AUDIO, bAppend,
					listner);
			mAudioManager = pcmAudioManager;
		}

		int sleepTime = 0;// 睡眠次数

		/**
		 * 重写上传数据方法，因为上传录音时需要判断是否读到文件末尾 位置，uint 大小：uint 内容：byte[]
		 */
		@Override
		int reqUploadData(ProtContext ctx) throws IOException {
			byte[] buff = ctx.getSendBuff(0x1000);
			int len = readData(buff, 0, 0x1000);
			if (len <= 0) {
				if (!mAudioManager.isRecordFinish()) {
					// 没有读到文件末尾，睡眠20毫秒再读
					try {
						Thread.sleep(20);
						reqUploadData(ctx);
					} catch (InterruptedException e) {
						if (Utils.debug) {
							Log.e(TAG, "上传音频时睡眠20毫秒异常", e);
						}
					}
				} else {
					ctx.sendMT(0, TS_UPLOAD_FINISH);
				}
			} else {
				sleepTime = 0;// 把睡眠次数清零
				ctx.sendMT(len + 8, TS_UPLOAD_DATA);
				ctx.mDOS.writeInt(mReqPos);
				ctx.mDOS.writeInt(len);
				ctx.mDOS.write(buff, 0, len);
				mReqPos += len;
			}
			return 0;
		}

		/**
		 * 取得audioManager对象,可以通过它获取录音和播放的对象
		 */
		public ClientManager getmAudioManager() {
			return mAudioManager;
		}

	}

	/** 大文件的上传任务 */
	public class UploadBigFileTask extends UploadTask {
		private int mFileMaxSize;// 上传文件最大值
		private int mUpPacketMaxSize;// 上传文件单个上传数据包最大值

		/** 上传大文件的构造函数 */
		public UploadBigFileTask(String fullName, String fileUrl,
				DownUploadListner listner) {
			super(fullName, FILE_TYPE_BIG_FILE, fileUrl, listner);
			mTaskId = getUploadTaskId(fullName);
			mTryTimes = 5;
		}

		/* 协议401： */
		@Override
		int reqStartUpload(ProtContext ctx) throws IOException {
			byte[] nameByes = mPath.getBytes();// 如果为续传的话，mPath为文件在服务器的地址，如果非续传，则只为文件名
			byte[] md5Bytes = "1".getBytes();
			ctx.sendMT(nameByes.length + md5Bytes.length + 14,
					TS_START_UPLOAD_BIG);
			ctx.mDOS.write(nameByes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(FILE_TYPE_BIG_FILE);
			ctx.mDOS.writeInt(mFileSize);
			ctx.mDOS.write(md5Bytes);
			ctx.mDOS.writeByte(0);
			ctx.mDOS.writeInt(mTarUID);
			return RES_SUCCESS;
		}

		/*
		 * 402号协议: 服务器对上传大文件请求的响应
		 */
		@Override
		int onStartUploadRsp(ProtContext ctx) throws IOException {
			int pos = ctx.mtIS.readInt();
			mServerHost = ctx.tsSvr.mServer + ":" + ctx.tsSvr.mPort;
			mPath = ctx.readMTString(4);
			mTime = ctx.mtIS.readInt();
			mFileMaxSize = ctx.mtIS.readInt();// 文件上传最大限制
			mUpPacketMaxSize = ctx.mtIS.readInt();// 文件上传一个数据包最大限制
			if (ctx.mtIS.available() >= 4) {
				uploadRspErr = ctx.mtIS.readInt();// 服务器返回的错误码
				if (Utils.debug)
					Log.i(TAG, "请求上传文件收到的服务器响应码："
							+ getRequestResult(uploadRspErr));
			} else
				uploadRspErr = 0;
			prepFile();
			if (mAppend) {
				mReqPos = pos;
				if (Utils.debug) {
					Log.i(TAG, "续传文件跳过" + mReqPos + "个字节续传");
				}
			}
			mFileIS.skip(mReqPos);
			if (mListner != null)
				mListner.onStart(this);
			if (Utils.debug) {
				Log.i(TAG, "UploadRequest.onRecv---TS_START_UPLOAD_RSP:"
						+ mPath + ",Pos=" + pos + ",time:" + mTime
						+ ",uploadRspErr:" + uploadRspErr);
			}
			return 0;
		}

		/*
		 * 协议 403，上传文件数据
		 */
		@Override
		int reqUploadData(ProtContext ctx) throws IOException {
			byte[] buff = ctx.getSendBuff(0x1000);
			int len = readData(buff, 0, 0x1000);
			if (len <= 0) {
				ctx.sendMT(0, TS_UPLOAD_FINISH_BIG);
			} else {
				ctx.sendMT(len + 8, TS_UPLOAD_DATA_BIG);
				ctx.mDOS.writeInt(mReqPos);
				ctx.mDOS.writeInt(len);
				ctx.mDOS.write(buff, 0, len);
				mReqPos += len;
			}
			return 0;
		}

		@Override
		public int onRecv(ProtContext ctx) throws IOException {
			int iRet = 0;
			try {
				switch (ctx.mRecvMT) {
				case TS_START_UPLOAD_BIG_RSP:
					iRet = onStartUploadRsp(ctx);
					mStep = 2;
					if (uploadRspErr == UPLOAD_BEYOND_MAX_FILE_SIZE) {
						// 29:上传文件大小超出限制（此文件类型暂定>20M）
						// TODO 怎么向UI反馈？
						closeFile();
						finish(DOWNERR_IOEXCEPTION, null);
					} else if (uploadRspErr == UPLOAD_INVALID_FILE_TYPE) {
						// 30:文件类型不对，现在此种文件类型仅有一种文件类型。

					} else if (uploadRspErr == UPLOAD_FILE_FINISH) {
						// 31:文件已经上传完毕，不需要上传了
						// 应该走正常上传完毕流程吧
						finish();
						closeFile();
					} else if (uploadRspErr == UPLOAD_INVALID_CONTINUE_URL
							|| uploadRspErr == SERVER_FILE_OPEN_FAILED) {
						// 32:此文件名url为新建立的文件，非断点续传
						// 22:续传时，服务器打开被续传临时文件失败
						mAppend = false;
						// mStep = 0;// 重新开始请求上传
						// mPath = mFile.getName();
						if (Utils.debug) {
							Log.i(TAG, "续传失败，从新开始上传");
						}
					}
					break;
				case TS_UPLOAD_DATA_BIG_RSP: //
					iRet = rspUploadData(ctx);
					mStep--;
					break;
				case TS_UPLOAD_FINISH_BIG_RSP:
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
	 * TSSocket：基础协议交互处理, 单请求处理模式，即一个请求完成后才进行下一个。
	 * 如果需要多个请求同时处理，则建一个会话管理请求，在会话管理请求中可以同时进行多个请求的处理。
	 */
	static class TSSocket {
		static final int BUFFERSIZE = 0x1000;

		/** Socket */
		Socket mSocket;
		final ProtContext mProtCtx;

		/** msTimeout是指读取超时时间 */
		public TSSocket(ProtContext protCtx) {
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
			if (Utils.debug)
				Log.i(TAG, "connecting " + mProtCtx.tsSvr.mServer + ",port:"
						+ mProtCtx.tsSvr.mPort);
			mSocket.connect(new InetSocketAddress(mProtCtx.tsSvr.mServer,
					mProtCtx.tsSvr.mPort));
			if (mProtCtx.tsSvr.mTimeout > 0)
				mSocket.setSoTimeout(mProtCtx.tsSvr.mTimeout);
			// mSockDOS =
			mProtCtx.mDOS = new DataOutputStream(mSocket.getOutputStream());
			// mSockDIS =
			mProtCtx.mDIS = new DataInputStream(mSocket.getInputStream());

			if (Utils.debug) {
				Log.i(TAG, "TSSocket connect");
			}
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

		/** 调用的前置条件是连网+有任务(包括登录任务) */
		public int doTask(MTTaskBase task, boolean bReconnected) {
			int iRet;
			try {
				mProtCtx.prepSend();
				mProtCtx.mRecvLen = 0;
				if (bReconnected)
					task.onReconnected(mProtCtx);
				iRet = task.onSend(mProtCtx);
				if (mProtCtx.mSendMT > 0)
					if (Utils.debug) {
						Log.i(TAG, "onSend:len=" + mProtCtx.mSendLen + ",mt="
								+ mProtCtx.mSendMT + ",Thread:"
								+ Thread.currentThread().getId());
					}
				if (mProtCtx.mSendMT > 0 || mProtCtx.mDIS.available() >= 4) {
					mProtCtx.mRecvLen = mProtCtx.mDIS.readInt();
					mProtCtx.mRecvMT = mProtCtx.mDIS.readInt();
					// if (Utils.test) {
					Log.i(TAG, "======onRecv:len=" + mProtCtx.mRecvLen + ",mt="
							+ mProtCtx.mRecvMT + ",Thread:"
							+ Thread.currentThread().getId());
					// }
					mProtCtx.prepRecv();
					iRet |= task.onRecv(mProtCtx);
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
			if (task instanceof DownloadTask) {
				DownloadTask dtask = (DownloadTask) task;
				if (dtask.doTaskTime == 0) {
					// 任务没有执行，断网重连
					iRet = TASK_RES_RESET_NET;
				} else if ((System.currentTimeMillis() - dtask.doTaskTime) > MTTaskBase.DOWN_FILE_TIME_OUT) {
					// 任务执行超时，则断网重连
					iRet = TASK_RES_RESET_NET;
					if (Utils.debug)
						Log.e(TAG,
								"doTask 任务执行超时:"
										+ (System.currentTimeMillis() - dtask.doTaskTime));
				}
			}

			return iRet;
		}

		public int waitTask(MTTaskBase task, boolean bReconnected) {
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

	// 任务队列管理
	static class TaskQueue {
		static class TaskWrapper {
			MTTaskBase mTask;
			int mQueue;

			void set(MTTaskBase task, int iQueue) {
				mTask = task;
				mQueue = iQueue;
			}
		}

		final int mLimit1 = 200;
		final int mLimit2 = 200;
		Object mNotifier = new Object();

		TaskExecutor[] mExecutors;

		// 所有任务在此队列中
		LinkedList<MTTaskBase> mMTTasks1 = new LinkedList<MTTaskBase>();// 优先级高，先从该list取任务
		LinkedList<MTTaskBase> mMTTasks2 = new LinkedList<MTTaskBase>();// 优先级低，当mMTTasks1中没有任务时，从该list取任务
		HashMap<String, TaskWrapper> mMTTasksMap = new HashMap<String, TaskWrapper>();
		MTTaskBase[] mCurTasks;// 当前正在被执行的任务的引用

		public TaskQueue(TaskExecutor[] executors) {
			mCurTasks = new MTTaskBase[executors.length];
			mExecutors = executors;
			int iQueue = 0;
			for (TaskExecutor exec : mExecutors) {
				exec.setQueue(this, iQueue);
				iQueue++;
			}
		}

		public int getChnlNo() {
			return mCurTasks.length;
		}

		public int startExecutors() {
			for (TaskExecutor exec : mExecutors) {
				if (exec != null && !exec.isAlive())
					exec.start();
			}
			return mExecutors.length;
		}

		public boolean AddTask(MTTaskBase task, int iQueue, boolean bNow) {
			boolean bRet = AddTask_(task, iQueue, bNow);
			synchronized (mNotifier) {
				mNotifier.notifyAll();
			}
			return bRet;
		}

		synchronized boolean AddTask_(MTTaskBase task, int iQueue, boolean bNow) {
			if (iQueue != 0)
				iQueue = 1;
			for (MTTaskBase tsk : mCurTasks) {
				if (tsk == task
						|| (tsk != null && tsk.mTaskId.equals(task.mTaskId))) {
					// 添加判断taskId是否相同 2013.12.20
					// 解决下面问题：大文件上传过程中断网，此时任务在当前执行队列，
					// 重新连上网络，会自动登陆神聊服务器，登陆成功后会自动发送未发送成功的消息
					// 添加任务到等待队列时，没有相同taskId，因为相同的任务在正在执行的队列，这样重复的任务就被添加到任务队列
					// 问题结果：同一个文件会上传两次，可能会发两次消息
					return true;
				}
			}
			TaskWrapper tw = mMTTasksMap.get(task.mTaskId);
			int iLimit = (iQueue == 0 ? mLimit1 : mLimit2);
			// tssn为当前任务指定要插入的队列
			LinkedList<MTTaskBase> tssn = (iQueue == 0 ? mMTTasks1 : mMTTasks2);
			if (tw != null) {
				if (iQueue == tw.mQueue && !bNow)
					return true;
				// 此任务已经在等待队列，tsso为当前任务所在队列的引用
				LinkedList<MTTaskBase> tsso = (tw.mQueue == 0 ? mMTTasks1
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
			tw.set(task, iQueue);
			mMTTasksMap.put(task.mTaskId, tw);
			return true;
		}

		public synchronized MTTaskBase getTask(int chnl) {
			if (chnl < 0 || chnl >= mCurTasks.length)
				return null;
			MTTaskBase vRet = mCurTasks[chnl];
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
			MTTaskBase tsk = mCurTasks[chnl];
			mCurTasks[chnl] = null;
			if (tsk != null)
				if (Utils.debug) {
					Log.i(TAG, "TaskQueue.clearCurTask, chnl=" + chnl
							+ ",taskid=" + tsk.mTaskId);
				}
		}

		// 取任务，如果线程执行时，取不到可以执行的任务，wait
		public MTTaskBase waitTask(int chnl, int timeout) {
			if (chnl >= mCurTasks.length)
				return null;
			MTTaskBase vRet = getTask(chnl);
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

		// 这个方法和上面的clearCurTask功能貌似相同，是否应该被整合删除？？？
		public synchronized MTTaskBase cancelCurrent(int chnl) {
			if (chnl < mCurTasks.length) {
				MTTaskBase task = mCurTasks[chnl];
				mCurTasks[chnl] = null;
				return task;
			}
			return null;
		}

		// public synchronized boolean removeTask(MTTaskBase task) {
		// TaskWrapper tw = mMTTasksMap.remove(task.mTaskId);
		// if (tw != null) {
		// if (tw.mQueue == 0)
		// return mMTTasks1.remove(tw.mTask);
		// else
		// return mMTTasks2.remove(tw.mTask);
		// }
		// return false;
		// return removeTask(task.mTaskId);
		// }

		/**
		 * 通过TaskId删除上传下载任务
		 * 
		 * @param taskId
		 * @return
		 */
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
				MTTaskBase task = mCurTasks[i];
				if (task != null && task.mTaskId.equals(taskid)) {
					task.mDeleteTempFile = isDeleteTempFile;
					mCurTasks[i] = null;
					this.mExecutors[i].cancelCurTask();
					if (isDeleteTempFile) {
						if (task instanceof DownloadTask) {
							// 如果是大文件下载任务，任务没有结束，则删除临时文件
							if (!task.isDone()) {
								((DownloadTask) task).deleteTaskFile();
							}
						}
					}
					return true;
				}
			}
			return false;
		}

		/** 删除大文件任务 */
		public synchronized boolean cancelBigTask(String taskid) {
			for (int i = 0; i < mCurTasks.length; i++) {
				MTTaskBase task = mCurTasks[i];
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

	class TaskExecutor extends Thread {
		TaskQueue mTaskQueue;
		int mChnl;// 通道号？
		final ProtContext mTSMContext;
		final ProtContext mTSContext;
		final TSSocket mTSMSocket;
		final TSSocket mTSSocket;
		volatile boolean mbRun = true;
		MTTaskBase mTSMLogonTask;
		TSLogonTask mTSLogonTask;

		public void setQueue(TaskQueue queue, int chnl) {
			mTaskQueue = queue;
			mChnl = chnl;
		}

		public boolean stopMe() {
			boolean bRet = mbRun;
			mbRun = false;
			return bRet;
		}

		/** isTransferBigFile:是否是大文件传输，如果是则需要更改TS版本号 */
		public TaskExecutor(boolean isTransferBigFile) {
			if (isTransferBigFile) {
				// 大文件传输，登陆TS的版本号为4
				mLogonPara.ver = TS_VERSION_BIG_FILE;
			} else {
				mLogonPara.ver = TS_VERSION;
			}
			mTSContext = new ProtContext(mLogonPara, new TSServerProp(), null);
			mTSMContext = new ProtContext(mLogonPara, mTSMSvr, mTSContext.tsSvr);
			mTSMSocket = new TSSocket(mTSMContext);
			mTSSocket = new TSSocket(mTSContext);
			mTSMLogonTask = new TSMLogonTask();
			mTSLogonTask = new TSLogonTask();
		}

		int mTSTryTimes;
		int mSta = 0;

		public void cancelCurTask() {
			mSta |= TASK_RES_RESET_NET;
		}

		@Override
		public void run() {
			mTSTryTimes = 0;
			MTTaskBase task;
			mSta = TASK_RES_CHGTS;
			while (mbRun) {
				boolean bNetReconnected = false;
				// 有任务才试图连网, 无任务时也不主动断网
				task = mTaskQueue.waitTask(mChnl, 2000);
				if (task == null) {
					// idle策略
					continue;
				}
				// 有任务,检查网络
				if (!mTSSocket.connected()) {
					// 延时:1秒，2秒，2秒，5秒。。。
					// 还可以考虑与网络相关
					if (mTSTryTimes > 0) {
						long waitTime = (mTSTryTimes < 3) ? 1000
								: ((mTSTryTimes < 5) ? 2000 : 5000);
						synchronized (mLogonPara.mNetNotifier) {
							try {
								mLogonPara.mNetNotifier.wait(waitTime);
							} catch (InterruptedException e) {
							}
						}
					}
					mTSTryTimes++;
					// 偿试
					if ((mSta & TASK_RES_CHGTS) != 0
							|| !mTSMSocket.mProtCtx.subSrv.bSvrValid) { // 根据条件连TSM，不一定要连
						if (Utils.debug)
							Log.i(TAG, "TSSocket.run,TSM logon."
									+ Thread.currentThread().getId());
						try {
							// mTSMLogonTask.reset();
							mTSMLogonTask.reset();
							mTSMSocket.connect();
							mTSMSocket.waitTask(mTSMLogonTask, true);
							mTSMSocket.reset();
							if (mTSMLogonTask.isDone()) {
								mSta &= ~TASK_RES_CHGTS;
								mSta |= TASK_RES_RESET_NET;
							} else {
								continue;
							}
						} catch (IOException e) {
							if (Utils.debug)
								Log.e(TAG, "重置socket异常", e);
						}
					}
				}
				try {
					if (!mTSSocket.connected()
							|| (mSta & TASK_RES_RESET_NET) != 0) {
						if (Utils.debug)
							Log.i(TAG, "TSSocket.run, TS logon."
									+ Thread.currentThread().getId());
						mTSLogonTask.reset();
						mTSSocket.connect();
						mSta |= mTSSocket.waitTask(mTSLogonTask, true);
						bNetReconnected = true;
						if ((mSta & TASK_RES_CHGTS) != 0) {
							mTSSocket.reset();
							continue;
						}
						mSta &= ~TASK_RES_RESET_NET;
						mTSTryTimes = 0;
					}
				} catch (IOException e) {
					if (Utils.debug) {
						Log.w(TAG, "连接TS服务器异常", e);
					}
				}
				if (mTSSocket.connected()) {
					// 如果有多个任务需要完成，在此一并处理
					int taskRes = mTSSocket.doTask(task, bNetReconnected);
					if (task.isDone()) {
						if (Utils.debug) {
							Log.i(TAG, "TaskExecutor, TaskDone, Removed");
						}
						mTaskQueue.clearCurTask(mChnl);
					} else
						Thread.yield();

					if (mTSSocket.mProtCtx.mRecvLen == 0
							&& mTSSocket.mProtCtx.mSendLen == 0) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
						}
					}
					// 如果任务需要重试，则由任务本身决定
					if ((taskRes & TASK_RES_RESET_NET) != 0) { // 需要断网重连
						mTSSocket.reset();
						// task.mAppend =
						// true;//当断网时，设定续传mAppend为true;因为断网重连后需要续传或者断点下载
						if (Utils.debug) {
							Log.i(TAG, "TaskExecutor, TASK_RES_RESET_NET");
						}
					}
					if ((taskRes & TASK_RES_CHGTS) != 0) {
						mTSSocket.reset();
						mSta |= TASK_RES_CHGTS;
						if (Utils.debug) {
							Log.i(TAG, "TaskExecutor, TASK_RES_CHGTS");
						}
					}
				}
			}
		}
	}

	// 图片、音频、名片消息的上传下载队列
	TaskQueue mDownTaskQueue;
	TaskQueue mUpTaskQueue;

	// 大文件的上传下载队列
	TaskQueue mDownBigTaskQueue;
	TaskQueue mUpBigTaskQueue;

	FileTransfer(TSServerProp tsm, TSLogonPara para) {
		mLogonPara = para;
		mTSMSvr = tsm;
		// 初始化图片、音频、名片消息的上传下载队列
		TaskExecutor[] downExecutors = { new TaskExecutor(false) };
		TaskExecutor[] upExecutors = { new TaskExecutor(false) };
		mDownTaskQueue = new TaskQueue(downExecutors);
		mUpTaskQueue = new TaskQueue(upExecutors);
		// 初始化大文件的上传下载队列
		TaskExecutor[] downBigExecutors = { new TaskExecutor(true) };
		TaskExecutor[] upBigExecutors = { new TaskExecutor(true) };
		mDownBigTaskQueue = new TaskQueue(downBigExecutors);
		mUpBigTaskQueue = new TaskQueue(upBigExecutors);
	}

	static String mRRoot = "/shenliao";
	/*** 头像目录 */
	public static final String AVATAR_PATH = "avatar";
	/*** 聊天图片目录 */
	public static final String IMAGE_PATH = "image";
	/*** Vcard目录 */
	public static final String VCARD_PATH = "cvf";
	/*** 音频目录 */
	public static final String AUDIO_PATH = "audio";
	/*** 接收到的大文件目录 */
	public static final String RECEIVE_FILE_FOLDER = "receivedFiles";
	public static final String GIF_PATH = "gif";
	/**
	 * 图片保存目录
	 */
	public static final String PHOTO_IMAGE_PATH = "sheliao";
	public Context mAppContext;

	private String getSaveDir(int fileType) {
		String saveDir = "";
		switch (fileType) {
		case FILE_TYPE_AUDIO:
			saveDir = AUDIO_PATH;
			break;
		case FILE_TYPE_AVATAR:
			saveDir = AVATAR_PATH;
			break;
		case FILE_TYPE_IMAGE:
			saveDir = IMAGE_PATH;
			break;
		case FILE_TYPE_VCARD:
			saveDir = VCARD_PATH;
			break;
		case FILE_TYPE_BIG_FILE:
			saveDir = RECEIVE_FILE_FOLDER;
			break;
		case FILE_TYPE_GIF:
			saveDir = GIF_PATH;
		}
		return saveDir;
	}

	public static boolean IsUrlValid(String url) {
		if (url == null)
			return false;
		String[] parts = url.split(":");
		if (parts.length != 4)
			return false;
		try {
			Integer.parseInt(parts[1]);
			Integer.parseInt(parts[3]);
		} catch (NumberFormatException e) {
			return false;
		}
		return true;
	}

	public String getStoragePath() {
		File root = Environment.getExternalStorageState().equals("mounted") ? Environment
				.getExternalStorageDirectory() : mAppContext.getFilesDir();
		return root.getAbsolutePath() + mRRoot;
	}

	/** 源类型 */
	public static final int SRC_TYPE_DEFAULT = 0;
	public static final int SRC_TYPE_GROUP = 1;
	public static final int SRC_TYPE_ALBUM = 2;
	public static final int SRC_TYPE_BIGFILE = 3;
	public static final int SRC_TYPE_GIFFILE = 4;

	public String getLocalFileName(String path, int filetype, int srctype,
			long srcid, boolean isBig, final String albumFileName) {
		// path.substring(path.indexOf("-") + 1);//这句操作好像没什么用？？
		String rootPath = getStoragePath();
		File folder = new File(rootPath, getSaveDir(filetype));
		if (!folder.exists())
			folder.mkdirs();

		String file;
		int suffixIndex = path.lastIndexOf(".");// 后缀名的开始index,如果为-1则代表没有这个值
		String newSuffix = null;
		if (suffixIndex != -1) {
			newSuffix = (isBig ? "_big" : "")
					+ path.substring(path.lastIndexOf("."));// 如果是大图，加上“_big”后缀
			// /2/3049091/537307587303049091_user_ablum_photo.jpg-20130520T183402.773942.jpg
		} else {
			// 路径没有后缀名，一般路径就为空,例如：4571341的第7张照地址--->118.145.23.46:80::0
			if (Utils.debug) {
				Log.e(TAG, "出现了没有后缀名的地址：" + path);
			}
			if (filetype == FILE_TYPE_IMAGE || filetype == FILE_TYPE_AVATAR) {
				newSuffix = (isBig ? "_big" : "") + ".jpg";// 没有后缀名，图片用".jpg"作为后缀
			} else if (filetype == FILE_TYPE_AUDIO) {
				newSuffix = ".pcm";// 音频用".pcm"作为后缀
			} else if (filetype == FILE_TYPE_GIF) {
				newSuffix = ".gif";
			}
		}
		switch (srctype) {
		case SRC_TYPE_GROUP: // group
			file = folder.getAbsolutePath() + "/" + "group_" + srcid
					+ newSuffix;
			break;
		case SRC_TYPE_ALBUM: // Album
			file = folder.getAbsolutePath() + "/" + albumFileName + newSuffix;
			break;
		case SRC_TYPE_BIGFILE: // bigFile
			file = folder.getAbsolutePath() + "/" + albumFileName;
			break;
		case SRC_TYPE_GIFFILE:
			file = folder.getAbsolutePath() + "/" + albumFileName;
		default: // Other
			file = folder.getAbsolutePath() + "/" + srcid + newSuffix;
			break;
		}
		return file;
	}

	// 通过url，截取文件的下载地址
	private String getDownloadPath(String url) {
		if (TextUtils.isEmpty(url)) {
			return null;
		}
		String parts[] = url.split(":");
		if (parts.length != 4) {
			return null;
		}
		return parts[2];
	}

	/** 取个人头像文件路径 */
	public String getAvatarFile(String url, long uid, boolean bBig) {
		return getAvatarFile(url, false, uid, bBig);
	}

	/** 取头像本地文件路径 */
	public String getAvatarFile(String url, boolean isGroup, long uid,
			boolean bBig) {
		// String parts[]=url.split(":");
		// if(parts.length!=4)
		// return null;
		String path = getDownloadPath(url);
		if (path != null) {
			return getLocalFileName(path, FILE_TYPE_AVATAR,
					isGroup ? SRC_TYPE_GROUP : SRC_TYPE_DEFAULT, uid, bBig,
					null);
		}
		return null;
	}

	/**
	 * 取相册的文件路径
	 * 
	 * @param url
	 * @return
	 */
	public String getAlbumFile(String url, boolean isBig) {
		String parts[] = url.split(":");
		if (parts.length != 4)
			return null;
		// String tail = isBig ? "_big" : "";
		return getLocalFileName(parts[2], FILE_TYPE_IMAGE, SRC_TYPE_ALBUM, -1,
				isBig, new MD5().getMD5ofStr(url));
	}

	/**
	 * 获取音频的下载地址
	 * 
	 * @param url
	 * @param srctype
	 *            是群组的，相册的还是其他的类型
	 * @param srcid
	 *            文件的id名字
	 * @return
	 */
	public String getAudioFile(String url, long srcid) {
		String path = getDownloadPath(url);
		if (path == null) {
			return null;
		}
		return getLocalFileName(path, FILE_TYPE_AUDIO, SRC_TYPE_DEFAULT, srcid,
				false, null);
	}

	/**
	 * 获取图片的下载地址
	 * 
	 * @param url
	 * @param srctype
	 * @param srcid
	 * @return
	 */
	public String getImageFile(String url, int srctype, long srcid,
			boolean isBig) {
		String path = getDownloadPath(url);
		if (path == null) {
			return null;
		}
		return getLocalFileName(path, FILE_TYPE_IMAGE, srctype, srcid, isBig,
				null);
	}

	public int downloadFile(String url, String file, int iType, int iQueue,
			boolean bAppend, DownUploadListner listner, Object obj) {
		int iRet = 0;
		DownloadTask task = new DownloadTask(url, file, iType, bAppend, listner);
		task.mObj = obj;
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownTaskQueue.AddTask(task, iQueue, false);
		mDownTaskQueue.startExecutors();
		return iRet;
	}

	public int downloadImg(String url, String file, int iQueue, boolean bBig,
			boolean bAppend, DownUploadListner listner, Object obj) {
		int iRet = 0;
		DownImageTask task = new DownImageTask(bBig, url, file,
				FILE_TYPE_IMAGE, bAppend, listner);
		task.mObj = obj;
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownTaskQueue.AddTask(task, iQueue, false);
		mDownTaskQueue.startExecutors();
		return iRet;
	}

	/**
	 * 下载个人头像，文件命名不加"group_"前缀
	 */
	public int downloadAvatar(String url, long uid, int iQueue, boolean bBig,
			boolean bAppend, DownUploadListner listner, Object obj) {

		return downloadAvatar(url, uid, iQueue, false, bBig, bAppend, listner,
				obj);
	}

	/** 下载大文件 */
	public int downloadBigFile(String url, int iQueue,
			DownUploadListner listner, int fileLength, Object obj) {
		int iRet = 0;
		DownloadBigFileTask task = new DownloadBigFileTask(url, fileLength,
				listner);
		task.mObj = obj;
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownBigTaskQueue.AddTask(task, iQueue, false);
		mDownBigTaskQueue.startExecutors();
		return iRet;
	}

	static class CHackListner extends DownUploadListner {
		DownUploadListner mOrg;

		CHackListner(DownUploadListner listner) {
			mOrg = listner;
		}

		@Override
		public void onStart(FileTaskInfo taskInfo) {
			if (Utils.debug) {
				Log.i("downloadAvatar", "onStart,uid=" + taskInfo.mSrcId);
			}
			if (mOrg != null)
				mOrg.onStart(taskInfo);
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			if (Utils.debug) {
				Log.i("downloadAvatar", "onProgress,uid=" + taskInfo.mSrcId);
			}
			if (mOrg != null)
				mOrg.onProgress(taskInfo, curlen, totallen);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			if (Utils.debug) {
				Log.i("downloadAvatar", "onFinish,uid=" + taskInfo.mSrcId);
			}
			if (mOrg != null)
				mOrg.onFinish(taskInfo);
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			if (Utils.debug) {
				Log.i("downloadAvatar", "onError,uid=" + taskInfo.mSrcId);
			}
			if (mOrg != null)
				mOrg.onError(taskInfo, iCode, iPara);
		}
	}

	public int downloadAvatar(String url, long uid, int iQueue,
			boolean isGroup, boolean bBig, boolean bAppend,
			DownUploadListner listner, Object obj) {
		if (!isGroup && !bBig) {
			listner = new CHackListner(listner);
			if (Utils.debug) {
				Log.i("downloadAvatar", "uid=" + uid);
			}
		}
		String file = getAvatarFile(url, isGroup, uid, bBig);
		if (file == null)
			return ERR_URL_INVALID;
		DownImageTask task = new DownImageTask(bBig, url, file,
				FILE_TYPE_AVATAR, bAppend, listner);
		task.mSrcId = uid;
		task.mObj = obj;
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownTaskQueue.AddTask(task, iQueue, false);
		mDownTaskQueue.startExecutors();
		return 0;
	}

	public int downloadVCard(String url, long msgid, boolean bAppend,
			DownUploadListner listner, Object obj) {
		int iRet = 0;
		String fileName = this.getLocalFileName(".vcf", FILE_TYPE_VCARD,
				SRC_TYPE_DEFAULT, msgid, false, null);
		DownloadTask task = new DownloadTask(url, fileName, FILE_TYPE_VCARD,
				bAppend, listner);
		task.mSrcId = msgid;
		task.mObj = obj;
		if (!task.IsTaskValid()) {
			listner.onError(task, DUERR_INVALIDPARA, null);
			return 0;
		}
		mDownTaskQueue.AddTask(task, 0, false);
		mDownTaskQueue.startExecutors();
		return iRet;
	}

	public int downloadAudio(String url, String file, int iQueue,
			boolean bAppend, DownUploadListner listner, Object obj) {
		int iRet = downloadFile(url, file, FILE_TYPE_AUDIO, iQueue, bAppend,
				listner, obj);
		return iRet;
	}

	public int uploadFile(String file, int iType, int iQueue, boolean bNow,
			boolean bAppend, DownUploadListner listner) {
		UploadTask task = new UploadTask(file, iType, bAppend, listner);
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpTaskQueue.AddTask(new UploadTask(file, iType, bAppend, listner),
					iQueue, bNow);
		} catch (IOException e) {
		}
		mUpTaskQueue.startExecutors();
		return 0;
	}

	public int uploadImg(String file, int iQueue, boolean bNow,
			boolean bAppend, DownUploadListner listner, Object obj) {
		UploadTask task = new UploadTask(file, FILE_TYPE_IMAGE, bAppend,
				listner);
		task.mObj = obj;
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpTaskQueue.AddTask(task, iQueue, bNow);
			mUpTaskQueue.startExecutors();
		} catch (IOException e) {
			if (Utils.debug) {
				Log.e(TAG, "准备上传文件异常", e);
			}
		}
		return 0;
	}

	/** 续传图片时调用的方法 */
	public int reUploadImg(String file, int iQueue, boolean bNow,
			String fileUrl, DownUploadListner listner, Object obj) {
		UploadTask task = new UploadTask(file, FILE_TYPE_IMAGE, fileUrl,
				listner);
		task.mObj = obj;
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpTaskQueue.AddTask(task, iQueue, bNow);
			mUpTaskQueue.startExecutors();
		} catch (IOException e) {
			if (Utils.debug) {
				Log.e(TAG, "准备续传的上传文件异常", e);
			}
		}
		return 0;
	}

	/**
	 * 上传音频文件
	 * 
	 * @param manager
	 *            为了能通知task录音文件已经录完。
	 * @param filePath
	 * @param bNow
	 * @param bAppend
	 * @param listner
	 * @return
	 */
	public int uploadAudioFile(ClientManager audioManager, String fileUrl,
			boolean bNow, boolean bAppend, DownUploadListner listner, Object obj) {
		UploadAudioTask task = new UploadAudioTask(audioManager, fileUrl,
				bAppend, listner);
		task.mObj = obj;
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpTaskQueue.AddTask(task, 0, bNow);// 这个任务优先级高，并且需要立即执行
		} catch (IOException e) {
			if (Utils.debug) {
				Log.e(TAG, "准备上传录音异常", e);
			}
		}
		mUpTaskQueue.startExecutors();
		return 0;
	}

	/** 上传大文件,用新协议 */
	public int uploadBigFile(String file, int iQueue, boolean bNow,
			String fileUrl, DownUploadListner listner) {
		UploadBigFileTask task = new UploadBigFileTask(file, fileUrl, listner);
		try {
			task.prepFile();
			if (!task.IsTaskValid()) {
				listner.onError(task, DUERR_INVALIDPARA, null);
				return 0;
			}
			mUpBigTaskQueue.AddTask(task, iQueue, bNow);
			if (Utils.debug) {
				Log.e(TAG, "TaskId:" + task.mTaskId);
			}
		} catch (IOException e) {
			if (Utils.debug)
				Log.e(TAG, "创建大文件上传任务，添加到队列异常", e);
			listner.onError(task, DUERR_INVALIDPARA, null);// 可能选择了没有读取权限的系统文件，open流时直接异常
		}
		mUpBigTaskQueue.startExecutors();
		return 0;
	}

	/** 删除大文件的上传任务 */
	public boolean removeUploadBigTask(String taskId) {
		boolean bResult = mUpBigTaskQueue.cancelTask(taskId, false);
		if (bResult) {
			// 当前正在被执行的任务被取消删除
			return true;
		} else {
			// 删除队列中待执行的任务
			bResult = mUpBigTaskQueue.removeTask(taskId);
		}
		return bResult;
	}

	/** 删除下载大文件任务 */
	public boolean removeDownloadBigTask(String taskId, boolean isDeleteTempFile) {
		boolean bResult = mDownBigTaskQueue
				.cancelTask(taskId, isDeleteTempFile);
		if (bResult) {
			// 当前正在被执行的任务被取消删除
			return true;
		} else {
			// 删除队列中待执行的任务
			bResult = mDownBigTaskQueue.removeTask(taskId);
		}
		if (Utils.debug) {
			if (bResult) {
				Log.e(TAG, "删除下载任务--" + taskId + "成功");
			} else {
				Log.e(TAG, "删除下载任务--" + taskId + "失败");
			}
		}
		return bResult;
	}

	// 删除上传任务
	public boolean removeUploadTask(String taskId) {
		boolean bResult = mUpTaskQueue.cancelTask(taskId, false);
		if (bResult) {
			if (Utils.debug) {
				Log.e(TAG, "删除当前上传任务--" + taskId + "成功");
			}
			return true;
		} else {
			if (Utils.debug) {
				Log.e(TAG, "删除当前上传任务--" + taskId + "失败");
			}
		}
		bResult = mUpTaskQueue.removeTask(taskId);
		if (Utils.debug) {
			if (bResult) {
				Log.e(TAG, "删除上传队列任务--" + taskId + "成功");
			} else {
				Log.e(TAG, "删除上传队列任务--" + taskId + "失败");
			}
		}
		return bResult;
	}

	public boolean removeDownloadTask(String taskId) {
		boolean bResult = mDownTaskQueue.removeTask(taskId);
		if (Utils.debug) {
			if (bResult) {
				Log.e(TAG, "删除下载任务--" + taskId + "成功");
			} else {
				Log.e(TAG, "删除下载任务--" + taskId + "失败");
			}
		}
		return bResult;
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

	/**
	 * 获取上传任务taskId 这么处理只是为了统一，后续想想怎么更好的处理
	 */
	public String getUploadTaskId(String fileFullName) {
		return fileFullName;
	}

	/** 获取普通下载任务taskId */
	public String getDownloadTaskId(String url) {
		String[] urlArray = getUrlArray(url);
		if (urlArray != null) {
			return urlArray[2] + urlArray[3];
		}
		return null;
	}

	/**
	 * 通过url获取图片下载任务taskId
	 */
	public String getDownLoadImageTaskId(String url, boolean isBig) {
		String taskId = getDownloadTaskId(url);
		if (taskId != null) {
			return taskId += isBig ? ".1" : ".0";
		}
		return null;
	}

	/** 通过url获取大文件存储的本地路径 */
	public String getDownLoadBigFilePath(String url) {
		String path = getDownloadPath(url);
		if (path != null) {
			String fileName = null;// getDownLoadBigFileName(path);
			String sub = path.substring(0, path.lastIndexOf("-"));
			String[] strs = sub.split("/");
			if (strs.length == 4) {
				fileName = strs[3];
			}
			if (fileName != null) {
				return getLocalFileName(path, FILE_TYPE_BIG_FILE,
						SRC_TYPE_BIGFILE, 0, false, fileName);
			}
		}
		return null;
	}

	// 只有一个地方调用，就放在方法里面了。减少代码理解负担
	// /** 通过path获取大文件名 */
	// public String getDownLoadBigFileName(String path) {
	// String sub = path.substring(0, path.lastIndexOf("-"));
	// String[] strs = sub.split("/");
	// if (strs.length == 4) {
	// return strs[3];
	// }
	// return null;
	// }

	/**
	 * 合成大小图混合的图片
	 * 
	 * @return
	 * @throws IOException
	 */
	public String getCompoundImgFile(String tempFilePath, Bitmap smallBitmap,
			Bitmap bigBitmap) throws IOException {
		if (TextUtils.isEmpty(tempFilePath)) {
			// 文件路径为空
			return null;
		}
		File tempImg = new File(tempFilePath);
		File parent = new File(tempImg.getParent());
		if (!parent.exists()) {
			parent.mkdirs();
		}
		if (tempImg != null && tempImg.exists()) {
			tempImg.delete();
		}
		tempImg.createNewFile();
		// 把小bitmap放在字节输出流中
		ByteArrayOutputStream small_baos = new ByteArrayOutputStream();
		smallBitmap.compress(Bitmap.CompressFormat.PNG, 100, small_baos);
		// 把大bitmap放在输出流中
		ByteArrayOutputStream big_baos = new ByteArrayOutputStream();
		bigBitmap.compress(Bitmap.CompressFormat.PNG, 100, big_baos);

		final int FILE_HEAD_SIZE = 24;
		final int TS_VERSION = 1;
		int sBitmapLength = small_baos.size();
		int bBitmapLength = big_baos.size();
		int totalLength = sBitmapLength + bBitmapLength;
		if (Utils.debug) {
			Log.i(TAG, "upload total CompoundImg Image size is: " + totalLength);
		}
		// 大图的起始字节位置
		int bigStartPosition = sBitmapLength + FILE_HEAD_SIZE;
		// int dataTotalSize = FILE_HEAD_SIZE + totalLength; //头信息和大小图字节的总长度

		FileOutputStream fos = new FileOutputStream(tempImg);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.writeInt(TS_VERSION);
		dos.writeInt(totalLength);
		dos.writeInt(FILE_HEAD_SIZE);
		dos.writeInt(sBitmapLength);
		dos.writeInt(bigStartPosition);
		dos.writeInt(bBitmapLength);
		small_baos.writeTo(dos);
		big_baos.writeTo(dos);
		dos.flush();
		dos.close();
		fos.close();
		small_baos.close();
		big_baos.close();

		return tempFilePath;
	}

	/** 删除用户头像文件 */
	public int delTXAvatarFiles(String avatarUrl, long partner_id) {
		int delNum = 0;
		String filePath = getAvatarFile(avatarUrl, partner_id, false);
		File headFile = null;
		if (filePath != null) {
			headFile = new File(filePath);
			if (headFile.exists()) {
				headFile.delete();
				delNum++;
			}
		}
		filePath = getAvatarFile(avatarUrl, partner_id, true);
		if (filePath != null) {
			headFile = new File(filePath);
			if (headFile.exists()) {
				headFile.delete();
				delNum++;
			}
		}

		return delNum;

	}

	public static FileTransfer gInstance;

	public static FileTransfer Init(TSServerProp tsm, TSLogonPara para) {
		gInstance = new FileTransfer(tsm, para);
		return gInstance;
	}
}