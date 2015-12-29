package com.tuixin11sms.tx.engine;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.EditText;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.activity.AddMyBlogActivity;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.task.FileTransfer.UploadAudioTask;
import com.tuixin11sms.tx.utils.LocationStation;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 处理发布瞬间
 * @author BrendaZ
 *
 */
public class ReleaseBlogOpea {
	protected static final int RECORD_PAUSE = 0;
	protected static final int RECORD_PLAY = 1;

	protected final static int HANDLE_RECORDER_ERROR = 1;
	protected final static int FLUSH_PROGRESS_TIME = 2;
	protected final static int FLUSH_SEEK_TEIME = 3;
	protected final static int CANCLE_SHORT_RECORD = 10;
	protected final static int CANCLE_LONG_RECORD = 11;
	protected final static int FLUSH_VOLUEM_AN = 2089;
	protected final static int RECORD_TIME_SHORT = 12;// 录音时间太短
	protected final int LONG_RECORD_EDN_TIME = 420;

	public static final int GET_LOCATION = 5;
	public static final int RECORD_FINISH = 6;
	public static final int GET_LOCATION_CITY = 7;
	public static final int RELEASE_OK = 8;

	private static final String TAG = "ReleaseBlogOpea";
	protected boolean mHasRecordErr; // 是否设备异常
	protected ClientManager mAudioRecPlayer;//
	protected boolean mIsRecording;
	private AddMyBlogActivity.RecorderPopupWindow mRecordPopupWindow;
	private int[] volumeResource;
	private boolean isCancelSendAudioMsg = false;
	private TXMessage mLongAudioMsg;// 到达7分钟后自动停止等待发送的长录音消息
	private Handler handler;
	public SessionManager mSess = SessionManager.getInstance();

	public ReleaseBlogOpea() {
		mHasRecordErr = false;
		mAudioRecPlayer = ClientManager.getRecordManager();
		Utils.readAutoPlayAdiouData(mSess.getContext());
	}

	public void setParam(
			AddMyBlogActivity.RecorderPopupWindow mRecordPopupWindow,
			int[] volumeResource, Handler handler, Handler mGroupAsynloader) {
		this.mGroupAsynloader = mGroupAsynloader;
		this.mRecordPopupWindow = mRecordPopupWindow;
		this.volumeResource = volumeResource;
		this.handler = handler;
	}

	/**
	 * 确定发布瞬间
	 * 
	 * @param msg
	 */
	public void sendOKMsg(BlogMsg msg) {
		mSess.getSocketHelper().sendReleaseBlog(msg);
		setReleaseBlogMsgCallback(callback);
	}

	public void setReleaseBlogMsgCallback(ReleaseBlogCallback callback) {
		this.callback = callback;
	}

	public interface ReleaseBlogCallback {
		/**
		 * 
		 * @param obj
		 *            第一个是是否成功boolea,true为成功，false为失败，第二个是返回内容
		 */
		public void receive(Object[] obj);
	}

	public ReleaseBlogCallback getcallback() {
		return callback;
	}

	public ReleaseBlogCallback callback = new ReleaseBlogCallback() {

		@Override
		public void receive(Object[] obj) {
			
			handler.obtainMessage(RELEASE_OK, obj).sendToTarget();
		}
	};

	
	/***************** 处理音频，录音及播放***************************/
	public ClientManager getAudioRecPlayer() {
		return this.mAudioRecPlayer;
	}

	public void setmAudioRecPlayer(ClientManager mAudioRecPlayer) {
		this.mAudioRecPlayer = mAudioRecPlayer;
	}

	public void setHasRecordErr(boolean is) {
		mHasRecordErr = is;
	}

	public void setCancelSendAudioMsg(boolean isCancel) {
		isCancelSendAudioMsg = isCancel;
	}

	public TXMessage getmLongAudioMsg() {
		return mLongAudioMsg;
	}

	public void setmLongAudioMsg(TXMessage mLongAudioMsg) {
		this.mLongAudioMsg = mLongAudioMsg;
	}

	public boolean getIsRecording() {
		return mIsRecording;
	}

	protected boolean record_interrupt;

	/**
	 * 停止长录音，例如长录音时点击取消按钮
	 */
	public void stopLongAudioRecord() {
		if (mAudioRecPlayer != null) {

			record_interrupt = true;
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					mIsRecording = false;
					record_interrupt = false;
					if (mAudioRecPlayer != null) {
						mAudioRecPlayer.stopRecord();
					}
				}
			};
			timer.schedule(timerTask, 1000);
		}
	}

	public void longRecordAudioUpLoad() {

		isCancelSendAudioMsg = false;// 初始化是否取消录音变量
		stopPlayAudioRecord();
		if (mAudioRecPlayer != null && !mAudioRecPlayer.isRecording()
				&& mAudioRecPlayer.startToRecord(recordListener)) {
			// TXMessage audioMsg = getNewAudioTxmsg();
			TXMessage audioMsg = new TXMessage();
			audioMsg.read_state = TXMessage.UPDATING;// 设置状态为正在上传
			mAudioRecPlayer.setRecordTxMsg(audioMsg);
			mSess.mDownUpMgr.uploadAudioFile(mAudioRecPlayer, null, false,
					false, mUploadAudioListener, audioMsg);

			mRecordPopupWindow.longrecordSeekTime();
			mRecordPopupWindow.longRecordTimes();

		}

		isCancelSendAudioMsg = false;// 初始化是否取消录音变量
		if (mAudioRecPlayer != null && !mAudioRecPlayer.isRecording()
				&& mAudioRecPlayer.startToRecord(recordListener)) {

			mRecordPopupWindow.longrecordSeekTime();
			mRecordPopupWindow.longRecordTimes();

		}

	}

	// 上传录音的listener
	DownUploadListner mUploadAudioListener = new DownUploadListner() {

		@Override
		public void onStart(FileTaskInfo taskInfo) {
			// mUploadTask = ((UploadAudioTask) task);
			if (Utils.debug) {
				Log.i(TAG, "开始上传");
			}
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			if (Utils.debug) {
				Log.e(TAG, "上传进度curlen:" + curlen + "---totallen:" + totallen);
			}
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			String fileUrl = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			UploadAudioTask upTask = ((UploadAudioTask) taskInfo);
			if (Utils.debug) {
				Log.i(TAG, "fileUrl-->" + fileUrl);
				Log.i(TAG, "录音总时长为："
						+ upTask.getmAudioManager().getAudioDuration() + "s");
				Log.i(TAG, "上传音频完成,开始发送消息");
			}

			if (isCancelSendAudioMsg) {
				if (Utils.debug) {
					Log.i(TAG, "音频信息取消发送");
					Log.i(TAG, "isCancelSend = " + isCancelSendAudioMsg);
				}
				isCancelSendAudioMsg = false;// 把该值复原
				if (Utils.debug) {
					Log.i(TAG, "isCancelSend = " + isCancelSendAudioMsg);
				}
				return;
			}

			TXMessage adiouMsg = (TXMessage) taskInfo.mObj;
			adiouMsg.msg_path = upTask.getmAudioManager().getAudioFilePath();// 设置录音文件本地地址
			adiouMsg.msg_url = fileUrl;
			// 非强制停止的短录音都是2分钟,长录音为7分钟
			adiouMsg.audio_times = upTask.getmAudioManager().getAudioDuration();

			if (!Utils.isRrecord) {
				// 如果还在录音状态，则不发送消息，先存储起来
				// 长录音到达7分钟时，停止录音，但是先不发送消息，用户点击发送按钮时再去发送
				setmLongAudioMsg(adiouMsg);
				return;
			}

			// mIsRecording = false;
			record_interrupt = true;
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					record_interrupt = false;
					if (mAudioRecPlayer != null) {
						if (Utils.debug) {
							Log.e(TAG, "443");
						}
						// recordManager.setRunning(false);
						mAudioRecPlayer.stopRecord();
					}
					mIsRecording = false;
				}
			};

			handler.obtainMessage(RECORD_FINISH, adiouMsg).sendToTarget();
			timer.schedule(timerTask, 1000);
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			if (Utils.debug) {
				Log.i(TAG, "上传音频异常", (Exception) iPara);
			}
			String fileUrl = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			UploadAudioTask upTask = ((UploadAudioTask) taskInfo);
			TXMessage adiouMsg = (TXMessage) taskInfo.mObj;
			adiouMsg.msg_path = upTask.getmAudioManager().getAudioFilePath();// 设置录音文件本地地址
			adiouMsg.msg_url = fileUrl;
			// 非强制停止的短录音都是2分钟,长录音为7分钟
			adiouMsg.audio_times = upTask.getmAudioManager().getAudioDuration();

		}

	};

	// 停止播放录音
	public void stopPlayAudioRecord() {
		mAudioRecPlayer.stopPlay();
		TXMessage msg = mAudioRecPlayer.getTxMsg();
		if (msg != null)
			msg.PlayAudio = RECORD_PAUSE;
		// if (playManager != null) {
		// playManager.setRunning(false);
		// }
	}

	RecordListener recordListener = new RecordListener() {

		private int playTime;

		@Override
		public void uploadFinish(TXMessage txMsg) {

			if (mAudioRecPlayer != null && txMsg != null) {

				if (txMsg.updateState == TXMessage.UPDATE_OK) {

				} else {

					txMsg.updateState = TXMessage.UPDATE_FAILE;

					txMsg.read_state = TXMessage.FAIL;

				}
			}
		}

		@Override
		public void recordError(int errcode) {
			// creatUnInitRecordInfo();
			Message msg = Message.obtain(mRecordPopupWindow.getHandler());
			msg.what = HANDLE_RECORDER_ERROR;
			msg.arg1 = errcode;
			msg.sendToTarget();
			mHasRecordErr = true;
		}

		@Override
		public void onPlayFinish(TXMessage txMsg) {

			// playManager.setRunning(false);
			// mAudioRecPlayer.stopPlay();
			handler.obtainMessage(GET_LOCATION).sendToTarget();

		}

		@Override
		public void doRecordVolume(float volume) {
			volume = Math.abs(volume);
			volume = volume > 1000000 ? volume / 100 : volume;
			volume = volume > 100000 ? volume / 10 : volume;
			int id = (int) (volume / 10000);
			if (id >= volumeResource.length - 1) {
				id = volumeResource.length - 1;
			}
			if (++playTime >= 10) {
				playTime = 0;
				Message message = new Message();
				message.what = FLUSH_VOLUEM_AN;
				message.arg1 = volumeResource[id];
				mRecordPopupWindow.getHandler().sendMessage(message);
			}
		}

		@Override
		public void deviceInitOK() {

			mIsRecording = true;

		}
	};

	public void sendAudioMsgAfterRocord(TXMessage audioMsg) {
		record_interrupt = true;
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				record_interrupt = false;
				if (mAudioRecPlayer != null) {
					if (Utils.debug) {
						Log.e(TAG, "444");
					}
					// recordManager.setRunning(false);
					mAudioRecPlayer.stopRecord();
				}
				mIsRecording = false;
			}
		};
		timer.schedule(timerTask, 1000);
	}

	/**
	 * 录音结束时逻辑 //应该是短录音的，因为录音时长为120s
	 * 
	 * @param isOutTime
	 */
	public void stopAudioRecordSocket(boolean isOutTime) {

		if (mAudioRecPlayer != null) {
			// 先不把这个语音消息添加到消息队列中
			if (!isCancelSendAudioMsg) {
				// 没有取消该语音消息的发送
				final TXMessage audioMsg = mAudioRecPlayer.getTxMsg();
				audioMsg.read_state = TXMessage.NOT_SENT;
				if (audioMsg.updateState != TXMessage.UPDATE_FAILE)
					audioMsg.updateState = TXMessage.UPDATING;
				audioMsg.msg_path = mAudioRecPlayer.getAudioFilePath();
				if (mAudioRecPlayer.getAudioDuration() > 0) {
					// 录音大于1秒钟时把此录音添加到界面
					// addMsg(audioMsg);
				} else {
					// 弹toast显示录音时间过短
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							RECORD_TIME_SHORT);
					isCancelSendAudioMsg = true;// 设置音频消息的标记为取消发送
				}

				// mIsRecording = false;
				record_interrupt = true;
				Timer timer = new Timer();
				TimerTask timerTask = new TimerTask() {
					public void run() {
						record_interrupt = false;
						if (mAudioRecPlayer != null) {
							mAudioRecPlayer.stopRecord();
							mIsRecording = false;

							audioMsg.audio_times = mAudioRecPlayer
									.getAudioDuration();
							// flush(FLUSH_ONE_LINE, audioMsg);

						}
					}
				};
				timer.schedule(timerTask, 1000);
			}
		}
	}
	
	/*****************处理音频，录音及播放end***************************/

	protected LocationStation ls;// 定位类

	// 获取地理位置
	public void startSendMeGeo() {

		if (Utils.isReLocation()) {
			ls = LocationStation.getInstance(mSess.getContext());
			ls.getLocation(mSess.getContext(), 30000);
		}
	}

	public LocationStation getLocationStation() {
		return ls;
	}

	public void getLocationStr(final double lat, final double lon) {
		mGroupAsynloader.post(new Runnable() {
			@Override
			public void run() {

				String city = Utils.getCity(lat, lon);

				handler.obtainMessage(GET_LOCATION_CITY, city).sendToTarget();
			}
		});
	}

	/**
	 * 获取文字瞬间截图
	 * @param editText
	 * @return
	 */
	public String getMsgImg(EditText editText) {

		editText.setDrawingCacheEnabled(true);
		Bitmap bitmap = getBitmap(editText.getDrawingCache());
		String filepath = "";
		filepath = Utils.getStoragePath(mSess.getContext())
				+ File.separator + Utils.IMAGE_PATH + File.separator
				+ System.currentTimeMillis() + ".jpg";
		// 生成合成的文件
		getImg(filepath, bitmap);
		return filepath;
	}

	public void getImg(String path, Bitmap bitmap) {
		File file = new File(path);
		FileOutputStream out;
		try {
			file.createNewFile();
			out = new FileOutputStream(file);
			if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
				out.flush();
				out.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Bitmap getBitmap(Bitmap img) {
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(img);
		return wref.get();
	}

	protected static Handler mGroupAsynloader;
}
