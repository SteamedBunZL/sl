package com.tuixin11sms.tx.audio.manager;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AutomaticGainControl;
import android.os.Handler;
import android.util.Log;

import com.tuixin11sms.tx.audio.encode.Speex;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.Utils;

public class ClientManager extends Thread{
	private static final String TAG = "ClientManager";
	private final Object mutex = new Object();
	// NONE = live
	public static final int NONE = 0;
	public static final int NETONLY = 1;
	public static final int FILEONLY = 2;
	public static final int NETANDFILE = 3;
	// record or playback
	public static final int SING = 1;
	public static final int PLAY = 2;
	public int clientMode = SING;
	private String path = "";
	private volatile boolean isRunning;
	private volatile boolean mPlaying=false;
	private volatile boolean mRecording=false;
	private volatile boolean isNeedExit;
	private String publishName;
	private String playName;

	private String fileName;
	private String mRecFilePath;
	private PcmRecorder mRecorder = null;
	private PcmReader mPlayer=null;
	private RecordListener recordListener;
	private Context context;
	private String tsUrl;
	private int time;
	private Speex speex = new Speex();
	private TXMessage txMsg = null;//录音或放音消息 都被这个引用持有
	private TXMessage mPlayingMsg = null;//正在播放录音消息
	
	static ClientManager msManager=null;
	public static void init(Context context){
		if(msManager==null){
			msManager=new ClientManager(context);
		}
	}
	public static ClientManager getPlayManager(){
		return msManager;
	}
	public static ClientManager getRecordManager(){
		return msManager;
	}
	private ClientManager(Context context) {
		super();
		speex.init();
		this.context = context;
	}

	@Override
	public void run() {
		if (Utils.debug)
			Log.d(TAG, "publish thread runing");
		while (!this.isNeedExit()) {
			while (!this.isRunning) {
				if (isNeedExit)
					break;
				synchronized (mutex) {
					try {
						mutex.wait();
					} catch (InterruptedException e) {
						if(Utils.debug)e.printStackTrace();
					}
				}
			}
			if (isNeedExit)
				break;
			//speex.init();
			if (clientMode == SING) {
				startPcmRecorder();
			} else if (clientMode == PLAY) {
				startPcmReader();
			}
			isRunning=false;
		}
	}

	/**
	 * 创建录音文件
	 */
	private void setupParams() {
		String rootFile = Utils.getStoragePath(context);
		File audioFile = new File(rootFile, Utils.AUDIO_PATH);
		if (!audioFile.exists())
			audioFile.mkdirs();
		fileName = String.valueOf(System.currentTimeMillis()) + ".spx";
		mRecFilePath = audioFile.getAbsolutePath() + "/" + fileName;
		if (Utils.debug)
			Log.i(TAG, "filePath is:" + mRecFilePath);
	}

	/**初始化并开启reader播放录音*/
	private void startPcmReader() {
		mPlayer.setFileName(playName);
		if(mRecorder!=null)
			mRecorder.stop();
		//setRunning(true);
		mPlayer.readPcm();
	}

	
	private boolean creatNewFile(String audioFilePath) {
		boolean bRet = true;
		File audioSaveFile = new File(audioFilePath);
		if (audioSaveFile.exists()) {
			audioSaveFile.delete();
		}
		try {
			audioSaveFile.createNewFile();
		} catch (IOException e) {
			if (recordListener != null) {
				recordListener.recordError(RecordListener.ERR_FAILCREATEFILE);
			}
			bRet = false;
		}
		mRecorder.setSaveFile(audioSaveFile);
		return bRet;

	}
		
	/**初始化并开启recorder录音*/
	private void startPcmRecorder() {
		//setRunning(true);
		mRecorder.setRecordListener(recordListener);
		if(mPlayer!=null)
			mPlayer.stop();
		mRecorder.record();
	}

	public boolean startToRecord(RecordListener listner)
	{
		if(mRecorder==null)
			mRecorder = new PcmRecorder();
		clientMode = SING;
		recordListener=listner;
		setupParams();
		boolean bRet=creatNewFile(mRecFilePath);
		if(bRet)
			setRunning(true);
		return bRet;
	}

	public void startToPlay(TXMessage msg, RecordListener listner){
		if(msg.msg_path==null)
			return;
		mPlayingMsg=msg;
		txMsg = msg;
		if(mPlayer==null)
			mPlayer = new PcmReader();
		clientMode=PLAY;
		recordListener=listner;
		playName=txMsg.msg_path;// 播放文件的本地路径
		if(Utils.debug){
			Log.i(TAG, "播放音频路径 ："+msg.msg_path);
		}
		setRunning(true);
	}

	private void resetMe() {
		if (clientMode == SING) {
			if (mRecorder != null) {
				if (Utils.debug)
					Log.i(TAG, "clientManager record stop");
				mRecorder.stop();
			}
		} else if (clientMode == PLAY) {
			if (Utils.debug)
				Log.i(TAG, "stop play audio");
			if(mPlayer!=null)
				mPlayer.stop();
		}
	}

	public boolean isNeedExit() {
		synchronized (mutex) {
			return isNeedExit;
		}
	}

	public void setNeedExit(boolean isNeedExit) {
		synchronized (mutex) {
			this.isNeedExit = isNeedExit;
			if (this.isNeedExit) {
				mutex.notify();
			}
		}
	}
	public void stopRecord(){
		if(mRecorder!=null)
			mRecorder.stop();
	}
	public void stopPlay(){
		if(mPlayer!=null)
			mPlayer.stop();
	}
	private void setRunning(boolean isRunning) {
		if (Utils.debug)
			Log.i(TAG, "clientMode:" + clientMode + ",running:" + isRunning);
		
		try{
			if(isRunning && !this.isAlive())
				start();
		}catch(IllegalThreadStateException e){}
		
		synchronized (mutex) {
			this.isRunning = isRunning;
			if (this.isRunning) {
				mutex.notify();
			}
		}
//		if (!isRunning) {
//			resetMe();
//		}
	}

	public boolean isPlaying(){
		return mPlaying;
	}
	public boolean isRecording(){
		return mRecording;
	}
//	public boolean isRunning() {
//		// synchronized (mutex) {
//		return isRunning;
//		// }
//	}

	public String getFileName() {
		return fileName;
	}

	public String getPublishName() {
		return publishName;
	}

	public void setPublishName(String publishName) {
		this.publishName = publishName;
	}

	public void setRecordListener(RecordListener recordListener) {
		this.recordListener = recordListener;
	}

	public String getTsUrl() {
		return tsUrl;
	}

	public void setTsUrl(String tsUrl) {
		this.tsUrl = tsUrl;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

	public RecordListener getRecordListener() {
		return recordListener;
	}

	public TXMessage getTxMsg() {
		return txMsg;
	}
	public TXMessage getPlayingMsg(){
		return mPlayingMsg;
	}

	
	//设置录音的TxMsg
	public void setRecordTxMsg(TXMessage txMsg) {
		this.txMsg = txMsg;
	}

	/**判断录音是否结束*/
	public boolean isRecordFinish() {
		return mRecorder.isRecordFinish();
	}
	
	/**获取录音的总时长*/
	public int getAudioDuration() {
		return mRecorder.getAudioTime();
	}
	
	/**获取录音保存文件地址*/
	public String getAudioFilePath() {
		return mRecFilePath;
	}

	private class PcmRecorder{
		private static final String TAG="PcmRecorder";
		private byte[] processedData = new byte[1024];
		private boolean mbRecFinish = false;	//是否录音完毕,关系到上传线程是否停止上传
		private DataOutputStream mAudioDOS;	//录音文件输出流
		private int mFrameSize;// 编码库生成的frameSize=16
		/**采样频率*/
		private static final int frequency = 8000;
		/** 采样位数*/
		private static final int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
		private RecordListener mRecordListener;
		private volatile AudioRecord mAudioRecord;
		private volatile AutomaticGainControl mAGC=null;
		private int mFrameCount;
		private int mRecTime = 0;//录音总时长,ms

		/**录音的方法 一边录音一边传送到服务器和存储到本地*/
		public void record() {
			mRecording=true;
			mbRecFinish=false;
			mFrameCount=0;
			mRecTime=0;
			mFrameSize=speex.getFrameSize();
			android.os.Process
			.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
			int bufferRead = 0;
			createRecorder();
			if (mAudioRecord != null) {
				int state=mAudioRecord.getState();
				if (Utils.debug)
					Log.i(TAG, "audioRecord state is:" + state);
				if (state == AudioRecord.STATE_UNINITIALIZED) {
					mRecordListener.recordError(RecordListener.ERR_DEVNOTINITED);
				} else if (state == AudioRecord.STATE_INITIALIZED) {
					short[] tempBuffer = new short[mFrameSize];
					mAudioRecord.startRecording();
					try {
						if (Utils.debug)
							Log.i(TAG, "start reading data.");
						while ((bufferRead = mAudioRecord.read(tempBuffer, 0, mFrameSize)) == mFrameSize) {
							if (mFrameCount == 0) {
								mRecordListener.deviceInitOK();
								//mbRecFinish = false;
							}
							// v记录音量大小，控制界面上音量图片的高低
							int v = 0;
							mFrameCount++;
							for (int i = 0; i < bufferRead; i++) {
								v += tempBuffer[i] * tempBuffer[i];
							}
							// 调用音量大小listener
							mRecordListener.doRecordVolume(v / (float) bufferRead);
							int getSize = speex.encode(tempBuffer, 0, processedData, mFrameSize);							
							if (getSize > 0) {
								writeDataToFile(processedData, getSize);
							}
						}
						if (Utils.debug)
							Log.i(TAG, "stop reading data.");
						mbRecFinish = true;
						if (bufferRead == AudioRecord.ERROR_INVALID_OPERATION) {
							// 非法操作
							if (Utils.debug)
								Log.i(TAG, "ERROR_INVALID_OPERATION");
							mRecordListener.recordError(RecordListener.ERR_DEVFILEERR);
						} else if (bufferRead == AudioRecord.ERROR_BAD_VALUE) {
							if (Utils.debug)
								Log.i(TAG, "ERROR_BAD_VALUE");
							mRecordListener.recordError(RecordListener.ERR_DEVFILEERR);
						} else if (mFrameCount == 0) {
							mRecordListener.recordError(RecordListener.ERR_DEVFILEERR);
							//setRunning(false);
						}
					} catch (IOException e) {
						if (Utils.debug)
							Log.i(TAG, "ERROR_INVALID_OPERATION");
						mRecordListener.recordError(RecordListener.ERR_DEVFILEERR);
					}
					catch(Exception e){
						if (Utils.debug)
							Log.i(TAG, "ERROR_INVALID_OPERATION");
						mRecordListener.recordError(RecordListener.ERR_INTERNALERR);					
					}	
				}
				finish();
			}else
				mRecordListener.recordError(RecordListener.ERR_DEVNOTINITED);
			mRecording=false;
		}
		@SuppressLint("NewApi")
		private void finish(){
			try {
				if (mAudioRecord!=null){
					if(mAudioRecord.getState() == AudioRecord.STATE_INITIALIZED) {
						if (Utils.debug) Log.i(TAG, "record stop");
						mAudioRecord.stop();
						if(mAGC!=null){
							mAGC.release();
							mAGC=null;
						}
						mAudioRecord.release();
						mAudioRecord=null;
					}
				}
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "AudioRecord stop exception:" ,e);
			}			
		}
		public void stop() {
			finish();
		}
		
		@SuppressLint("NewApi")
		private void createRecorder() {
			//不能缓存mAudioRecord对象，因为它包括对MIC的选择
			if(mAudioRecord!=null){
				if(mAGC!=null){
					mAGC.release();
					mAGC=null;
				}
				mAudioRecord.release();
			}
			int bufferSize = AudioRecord.getMinBufferSize(frequency,
					AudioFormat.CHANNEL_CONFIGURATION_MONO, audioEncoding);
			mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
					frequency, AudioFormat.CHANNEL_CONFIGURATION_MONO,
					audioEncoding, bufferSize * 2);
			if(android.os.Build.VERSION.SDK_INT>=16){
				mAGC=AutomaticGainControl.create(mAudioRecord.getAudioSessionId());
				if(mAGC!= null){
					mAGC.setEnabled(true);
				}
			}
		}

		public void setRecordListener(RecordListener recordListener) {
			this.mRecordListener = recordListener;
		}

		public boolean setSaveFile(File file) {
			if(file==null){
				return false;
			}
			BufferedOutputStream bos = null;
			try {
				bos = new BufferedOutputStream(new FileOutputStream(
						file));
			} catch (FileNotFoundException e) {
				if(Utils.debug){
					Log.w(TAG, "创建录音文件输出流异常",e);
				}
				return false;
			}

			mAudioDOS = new DataOutputStream(bos);
			return true;
		}

		/**录音总时长*/
		public int getAudioTime() {
			return mRecTime/1000;
		}
		
		
		private int mCurLen=0;
		
		/**把录音文件数据存储到本地*/
		private void writeDataToFile(byte[] buf, int size) throws IOException {
			mRecTime +=20;
//			if (Utils.debug)
//				Log.i(TAG, "audio data wite size is:" + size);
			int wrlen=size+4;
			if(mCurLen>(2048-wrlen)){
				if (Utils.debug)
					Log.i(TAG, "flush audio data, wrlen="+wrlen+",frame size is:" + size);
				mAudioDOS.flush();
				mCurLen=0;
			}
			mAudioDOS.writeInt(Integer.reverseBytes(size));
			mAudioDOS.write(buf, 0, size);
			mCurLen+=wrlen;
		}
		public boolean isRecordFinish() {
			return mbRecFinish;
		}
	}
	private class PcmReader{
		private static final String TAG = "PcmReader";
		private String fileName;
		private AudioTrack mAudioTrack;
		private short[] processedData = new short[256];
		byte[] mReadBuff = new byte[256];

		public void readPcm() {
			mPlaying=true;
			DataInputStream dis=null;
			File pcmFile = new File(fileName);
			boolean bFinish=true;
			try {
				startAudioTrack();
				dis = new DataInputStream(new FileInputStream(pcmFile));
				if (Utils.debug)
					Log.i(TAG, "file is:" + pcmFile +",len="+pcmFile.length());
				int size=-1;
				//注意：iOS用的是动态编码, 帧长不是固定值
				while (mPlaying && isRunning && dis.available() > 0) {
					size = Integer.reverseBytes(dis.readInt());					
					if(size<0 || size>256){
						if(Utils.debug)
							Log.i(TAG, "play exception:framelen="+size);
						break;
					}
					dis.read(mReadBuff,0,size);
					int getSize = speex.decode(mReadBuff, processedData, size);
					mAudioTrack.write(processedData, 0, getSize);
					if(dis.available()<=0)
						break;
				}
				if (Utils.debug)
					Log.i(TAG, "while outer");
				//stop();
				//if (dis.available() == 0) {
				//	playFinish();
				//}
				if(dis.available()>0)
					bFinish=false;
				dis.close();
			} catch (Exception e) {
				Log.e(TAG, "Audio play Exception:"+e);
				if(dis!=null){
					try {
						dis.close();
					} catch (IOException e1) {}
				}
				//stop();
				//setRunning(false);				
			}
			if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
					|| mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED)
				mAudioTrack.stop();
			mPlayingMsg=null;
			if(bFinish)
				playFinish();
			mPlaying=false;
		}
		private void startAudioTrack() {
			if(mAudioTrack==null){
				int bufferSizeInBytes = AudioTrack.getMinBufferSize(8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT);
				mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, 8000,
						AudioFormat.CHANNEL_CONFIGURATION_MONO,
						AudioFormat.ENCODING_PCM_16BIT, 2 * bufferSizeInBytes,
						AudioTrack.MODE_STREAM);
			}
			mAudioTrack.play();
		}

		public void setFileName(String fileName) {
			this.fileName = fileName;
		}

		private void stop() {
			if (Utils.debug)
				Log.i(TAG, "pcm reader stop");
			mPlaying=false;
			if (mAudioTrack != null) {
				if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING
						|| mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PAUSED){
					mAudioTrack.stop();
					try {
						Thread.sleep(40);
					} catch (InterruptedException e) {
					}					
				}
				//mAudioTrack.release();
			}
		}

		private void playFinish() {
			if (getRecordListener() != null) {
				getRecordListener().onPlayFinish(getTxMsg());
			}
		}
	}
	
	public  ArrayList<TXMessage> talkMsgsCache = new ArrayList<TXMessage>();// 语音消息集合？
	
	/**
	 * 添加需要自动播放的音频到队列
	 * 
	 * @param txmsg
	 */
	public void addTalkCache(TXMessage txmsg,boolean isStartAudioPlay) {

		if (isStartAudioPlay
				&& Utils.isOpenPlayAdiou
				&& !txmsg.was_me
				&& txmsg.read_state == TXMessage.UNREAD
				&& txmsg.updateState != TXMessage.UPDATE_FAILE
				&& (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS || txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS)) {

			if(!talkMsgsCache.contains(txmsg))
				talkMsgsCache.add(txmsg);
//			talkMsgsCache.add(txmsg);
		}
	}

	/**
	 * 删除队列里的音频对象
	 * 
	 * @param txmsg
	 */
	public void removeTalkCache(TXMessage txmsg) {
		if (talkMsgsCache.size() > 0) {
			talkMsgsCache.remove(txmsg);
		}
	}

	/**
	 * 删除所有音频
	 * 
	 */
	public void removeAllTalkCache() {
		talkMsgsCache.clear();
	}
	public static final int ADU = 91;
	
	public void playTalkCache(TXMessage txmsg,Handler handler) {
		if (Utils.isOpenPlayAdiou && talkMsgsCache.size() > 0) {

			TXMessage txmsg1 = talkMsgsCache.get(0);
			if (txmsg1 != null && txmsg1.equals(txmsg)) {
				while (txmsg.updateState == TXMessage.UPDATE_FAILE) {
					talkMsgsCache.remove(txmsg1);
					if (talkMsgsCache.size() == 0) {
						break;
					}
					txmsg1 = talkMsgsCache.get(0);
				}
				if (!Utils.isNull(txmsg1.msg_path)&&getPlayingMsg()==null) {
					if (Utils.debug) {
						Log.i(TAG, "playTalkCache->" + txmsg1.msg_path);
					}
					
					handler.obtainMessage(ADU, txmsg1).sendToTarget();
//					playAudioRecord(txmsg1);
				}
			}
		}
	}
	
}
