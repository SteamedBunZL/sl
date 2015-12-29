package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class VideoCaptureActivity extends BaseActivity {
	private static final String TAG = "VideoCaptureActivity";

	private final int FLUSH_DURATION = 0x0101;
	private final int REACH_MAX_DURATION = 0x0102;
	private final int REACH_MAX_FILESIZE = 0x0103;

	private Camera myCamera;
	private MyCameraSurfaceView myCameraSurfaceView;
	private MediaRecorder mediaRecorder;
	private SurfaceHolder surfaceHolder;
	private String videoFilePath;// 录制的视频保存的路径

	private ImageView myButton;
	private FrameLayout fl_CameraPreview;
	private TextView tv_video_duration;
	private boolean recording;
	private String recordFolderPath;// 录像文件的根目录

	private List<VideoSizeT> cameraSizeList = new ArrayList<VideoCaptureActivity.VideoSizeT>();
	private List<Integer> frameRates = null;// 相机帧速
	private long startTime;// 开始录制视频的时间
	private Timer videoDurationTimer;// 显示录制视频时长的定时任务

	/******* 视频录制播放控件 **********/
	private TextView tv_record_video_prompt;
	private ImageView iv_switch_camera;
	private int cameraPosition = 0;// 0代表后置摄像头，1代表前置摄像头

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case FLUSH_DURATION:
				// 刷新录视频时长显示
				long currentTime = System.currentTimeMillis();
				tv_video_duration.setText(Utils.formatDurationTimes((currentTime - startTime) / 1000));
				break;
			case REACH_MAX_DURATION:
			case REACH_MAX_FILESIZE:
				// 视频录制时长到达上限或 视频文件大小到达上限
				stopRecordVideo();
				startPreviewVideo();
				break;

			default:
				break;
			}
		};
	};

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_video_capture);
		// 录制视频控件
		fl_CameraPreview = (FrameLayout) findViewById(R.id.fl_video_capture);
		RelativeLayout rl_video_tools_bar = (RelativeLayout) findViewById(R.id.rl_video_tools_bar);

		myButton = (ImageView) rl_video_tools_bar.findViewById(R.id.iv_record_video);
		tv_video_duration = (TextView) findViewById(R.id.tv_record_duration);
		iv_switch_camera = (ImageView) findViewById(R.id.iv_switch_camera);

		// 播放视频控件
		tv_record_video_prompt = (TextView) findViewById(R.id.tv_record_video_prompt);

//		// 录制视频
//		recording = false;
//
//		// Get Camera for preview
//		myCamera = getCameraInstance();
//		if (myCamera == null) {
//			showToast("Fail to get Camera");
//		}
//
//		myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
//
//		fl_CameraPreview.addView(myCameraSurfaceView);

		myButton.setOnClickListener(myButtonOnClickListener);
		iv_switch_camera.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (recording || Build.VERSION.SDK_INT < 10) {
					// SDK版本低于10,或者视频录制过程中不能切换摄像头
					return;
				}
				if (Utils.debug)
					showToast("切换摄像头");
				{

					// 切换前后摄像头
					try {
						Class<?> clazzCameraInfo = Class.forName("android.hardware.Camera$CameraInfo");
						Object cameraInfoInstance = clazzCameraInfo.newInstance();
						Field fieldFacing = clazzCameraInfo.getDeclaredField("facing");
						Field field_CAMERA_FACING_FRONT = clazzCameraInfo.getDeclaredField("CAMERA_FACING_FRONT");
						Field field_CAMERA_FACING_BACK = clazzCameraInfo.getDeclaredField("CAMERA_FACING_BACK");

						Class<Camera> clazzCamera = Camera.class;
						Method methodGetCameraInfo = clazzCamera.getDeclaredMethod("getCameraInfo", int.class,
								clazzCameraInfo);
						Method methodGetNumberOfCameras = clazzCamera.getDeclaredMethod("getNumberOfCameras");
						Method methodOpen = clazzCamera.getDeclaredMethod("open", int.class);

						int cameraCount = (Integer) methodGetNumberOfCameras.invoke(null, null);// 得到摄像头的个数
						for (int i = 0; i < cameraCount; i++) {
							// Camera.getCameraInfo(i, cameraInfo);//
							// 得到每一个摄像头的信息
							methodGetCameraInfo.invoke(clazzCamera, i, cameraInfoInstance);// 得到每一个摄像头的信息
							if (cameraPosition == 0) {
								// 现在是后置，变更为前置

								if (fieldFacing.getInt(cameraInfoInstance) == field_CAMERA_FACING_FRONT.getInt(null)) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
									// CAMERA_FACING_BACK后置
									myCamera.stopPreview();// 停掉原来摄像头的预览
									myCamera.release();// 释放资源
									myCamera = null;// 取消原来摄像头
									myCamera = (Camera) methodOpen.invoke(null, i);// 打开当前选中的摄像头
									myCamera.setDisplayOrientation(90);
									try {
										myCamera.setPreviewDisplay(surfaceHolder);// 通过surfaceview显示取景画面
									} catch (IOException e) {
										Log.e(TAG, "设置预览显示异常");
									}
									myCamera.startPreview();// 开始预览
									cameraPosition = 1;
									break;
								}
							} else {
								// 现在是前置， 变更为后置
								if (fieldFacing.getInt(cameraInfoInstance) == field_CAMERA_FACING_BACK.getInt(null)) {// 代表摄像头的方位，CAMERA_FACING_FRONT前置
									// CAMERA_FACING_BACK后置
									myCamera.stopPreview();// 停掉原来摄像头的预览
									myCamera.release();// 释放资源
									myCamera = null;// 取消原来摄像头
									myCamera = (Camera) methodOpen.invoke(null, i);// 打开当前选中的摄像头
									myCamera.setDisplayOrientation(90);
									try {
										myCamera.setPreviewDisplay(surfaceHolder);// 通过surfaceview显示取景画面
									} catch (IOException e) {
										if (Utils.debug)
											Log.e(TAG, "开启预览异常：", e);
									}
									myCamera.startPreview();// 开始预览
									cameraPosition = 0;// 当前摄像头为后置摄像头
									break;
								}
							}
						}

					} catch (Exception e1) {
						if (Utils.debug)
							Log.e(TAG, "摄像头切换类反射异常", e1);
					}
				}
			}
		});

		recordFolderPath = mSess.mDownUpMgr.getStoragePath() + "/recordVideo";
		Utils.creatFolder(recordFolderPath);// 创建路径的文件夹

	}
	
	@Override
	protected void onRestart() {
		super.onRestart();
		//重置录像按钮状态、录像时长和背景图
		
	}
	
	@Override
	protected void onStart() {
		super.onStart();
		// 录制视频
		recording = false;

		// Get Camera for preview
		myCamera = getCameraInstance();
		if (myCamera == null) {
			showToast("Fail to get Camera");
		}

		myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
		//removeAllViews()必须调用，不删除的话，原来的surfaceView也会启动，这时之前的SurfaceView上的camera其实已经被release了，会异常 2013.12.27
		fl_CameraPreview.removeAllViews();
		fl_CameraPreview.addView(myCameraSurfaceView);
		tv_video_duration.setText("00:00");//重置录像时长显示
		tv_record_video_prompt.setVisibility(View.VISIBLE);//刚开启都是显示录像提示语
		
	};
	
	

	/** 开始和停止录制录制视频按钮 */
	Button.OnClickListener myButtonOnClickListener = new Button.OnClickListener() {

		@Override
		public void onClick(View v) {
			if (recording) {
				stopRecordVideo();
				startPreviewVideo();
			} else {
				startRecordVideo();
			}
		}
	};

	/** 开始录制视频 */
	private void startRecordVideo() {
		// Release Camera before MediaRecorder start
		releaseCamera();

		if (!prepareMediaRecorder()) {
			showToast("摄像机初始化失败");
			finish();
		} else {

			mediaRecorder.start();

			recording = true;
			myButton.setImageResource(R.drawable.stop_record_video);
			tv_record_video_prompt.setVisibility(View.INVISIBLE);//隐藏提示语
			startTime = System.currentTimeMillis();

			// 开启计时器
			TimerTask ttsk = new TimerTask() {

				@Override
				public void run() {
					handler.sendEmptyMessage(FLUSH_DURATION);
				}
			};

			videoDurationTimer = new Timer();
			videoDurationTimer.schedule(ttsk, 500, 500);
		}

	}

	/** 停止录视频 */
	private void stopRecordVideo() {
		// stop recording and release camera
		videoDurationTimer.cancel();
		// tv_video_duration.setText("00:00");//不重置时长显示 2013.12.07
		mediaRecorder.stop(); // stop the recording
		releaseMediaRecorder(); // release the MediaRecorder object
		// 下面给mediaRecorder置空是为了防止finish这个activity时，onPause方法中判断mediaRecorder为空，会再次释放mediaRecorder，底层lib库会报异常
		mediaRecorder = null;
		myButton.setImageResource(R.drawable.start_record_video);
		tv_record_video_prompt.setVisibility(View.VISIBLE);//显示录像提示语
		recording = false;
	}

	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "开启相机异常：", e);
			// Camera is not available (in use or does not exist)
		}
		return c; // returns null if camera is unavailable
	}

	private boolean prepareMediaRecorder() {
		// myCamera = getCameraInstance();
		try {
			Class<Camera> clazzCamera = Camera.class;
			Method methodOpen = clazzCamera.getDeclaredMethod("open", int.class);

			myCamera = (Camera) methodOpen.invoke(null, cameraPosition);
		} catch (Exception e1) {
			if (Utils.debug)
				Log.e(TAG, "反射调用摄像头异常", e1);
		}
		myCamera.setDisplayOrientation(90);
		mediaRecorder = new MediaRecorder();
		cameraSizeList = getCamreaPx(myCamera);// 填充分辨率集合
		myCamera.unlock();
		mediaRecorder.setCamera(myCamera);

		mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
		mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

		// mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));
		mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
		// mediaRecorder.setVideoFrameRate(30);
		VideoSizeT vs = null;
		boolean isIncrease = true;// 集合中的分辨率是增序还是减序
		// 取出不大于且最接近于640的尺寸
		for (int i = 0, size = cameraSizeList.size(); i < size; i++) {
			vs = cameraSizeList.get(i);
			if (i == 0 && vs.width > 640) {
				// 第一个宽就大于640，那么集合中的尺寸一定是降序排列
				isIncrease = false;
			}
			if (vs.width == 640) {
				break;
			} else {
				if (isIncrease) {
					// 尺寸是升序排序
					if (vs.width > 640) {
						// 第一个宽大于640的尺寸
						vs = cameraSizeList.get(i > 0 ? i - 1 : 0);// 防止角标越界
						break;
					}
				} else {
					// 尺寸是降序排序
					if (vs.width < 640) {
						// 第一个宽小于640的尺寸
						break;
					}
				}
			}
		}
		mediaRecorder.setVideoSize(vs.width, vs.height);
		// mediaRecorder.setVideoSize(640, 480);
		if (Build.VERSION.SDK_INT >= 10) {
			// API 10以上才提供音频AAC编码
			Class<? extends MediaRecorder> recordClass = mediaRecorder.getClass();
			Method mSetEncoder;
			Method mOutputOrientation;
			try {
				mSetEncoder = recordClass.getDeclaredMethod("setAudioEncoder", int.class);
				mOutputOrientation = recordClass.getDeclaredMethod("setOrientationHint", int.class);
				// MediaRecorder.AudioEncoder.AAC= 3
				mSetEncoder.invoke(mediaRecorder, 3);
				if (cameraPosition == 0) {
					// 后置摄像头选择90度
					mOutputOrientation.invoke(mediaRecorder, 90);
				} else {
					// 设置前置摄像头旋转270°
					if (Utils.debug)
						Log.i(TAG, "设置前置摄像头mediaRecorder录像成像角度");
					mOutputOrientation.invoke(mediaRecorder, 270);
				}
			} catch (Exception e) {
				Log.e(TAG, "反射调用AAC编码异常", e);
			}
		} else {
			mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
		}
		mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
		try {
			long currentMills = System.currentTimeMillis();
			String resolution = +vs.width + "*" + vs.height;// 当前录制的分辨率
			// String videoFilePath = recordFolderPath + "/" + currentMills +
			// "-"
			// + resolution + ".mp4";
			videoFilePath = recordFolderPath + "/" + currentMills + ".mp4";
			File videoFile = new File(videoFilePath);
			if (videoFile.exists()) {
				videoFile.delete();
			}
			videoFile.createNewFile();
			mediaRecorder.setOutputFile(videoFilePath);

			if (Utils.debug)
				showToast("当前录制的分辨率为：" + resolution);

			mediaRecorder.setMaxDuration(5 * 60 * 1000); // 最大时长为5分钟
			mediaRecorder.setMaxFileSize(20 * 1024 * 1024); // Set max file size
															// 20M

			mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

			mediaRecorder.prepare();
			mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {

				@Override
				public void onInfo(MediaRecorder mr, int what, int extra) {
					if (Utils.debug)
						Log.i(TAG, "infor-->what:" + what + ",extra:" + extra);
					if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED) {
						showToast("到达录制时长上限");
						handler.sendEmptyMessage(REACH_MAX_DURATION);
					} else if (what == MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED) {
						showToast("到达录制文件大小上限");
						handler.sendEmptyMessage(REACH_MAX_FILESIZE);
					} else if (what == MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN) {
						showToast("其他未知消息");
					}

				}
			});
			mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {

				@Override
				public void onError(MediaRecorder mr, int what, int extra) {
					showToast("录制视频出错");
					if (Utils.debug)
						Log.i(TAG, "error");

				}
			});
		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "准备相机异常：", e);
			// 如果SD卡容量不足，准备MediaRecorder会异常，这里把MediaRecorder给release了，
			// 在该activity执行finish方法时，会再次执行MediaRecorder的reset方法，这时会异常，所以这里就不释放MediaRecorder
			// 2013.12.27
			// releaseMediaRecorder();
			return false;
		}
		return true;

	}

	/** 跳转到开始预览视频界面 */
	private void startPreviewVideo() {
		Intent intent = new Intent(thisContext, VideoPlayActivity.class);
		intent.putExtra(Constants.FILE_LOCAL_PATH, videoFilePath);
		intent.putExtra(VideoPlayActivity.IS_PREVIEW_VIDEO, true);
		intent.putExtra(VideoPlayActivity.VIDEO_DURATION, tv_video_duration.getText().toString());
		startActivityForResult(intent, BaseMsgRoom.REQ_TAKE_BIG_FILES);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
//		releaseVideoRecorder();
	}

	@Override
	protected void onPause() {
		super.onPause();
		releaseVideoRecorder();
		if (videoDurationTimer!=null) {
			videoDurationTimer.cancel();
		}
		myButton.setImageResource(R.drawable.start_record_video);

	}

	/** 当要finish这个activity时要执行的释放操作 */
	private void releaseVideoRecorder() {
		// if you are using MediaRecorder, release it first
		releaseMediaRecorder();
		// release the camera immediately on pause event
		releaseCamera();
	}

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			myCamera.lock(); // lock camera for later use
		}
	}

	private void releaseCamera() {
		if (myCamera != null) {
			myCamera.release(); // release the camera for other applications
			myCamera = null;
		}
	}

	public class MyCameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

		// private SurfaceHolder mHolder;
		private Camera mCamera;

		public MyCameraSurfaceView(Context context, Camera camera) {
			super(context);
			mCamera = camera;

			// Install a SurfaceHolder.Callback so we get notified when the
			// underlying surface is created and destroyed.
			surfaceHolder = getHolder();
			surfaceHolder.addCallback(this);
			// deprecated setting, but required on Android versions prior to 3.0
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}

		@Override
		public void surfaceChanged(SurfaceHolder holder, int format, int weight, int height) {
			if (Utils.debug)
				Log.e(TAG, "预览发生变化surfaceChanged");
			// If your preview can change or rotate, take care of those events
			// here.
			// Make sure to stop the preview before resizing or reformatting it.

			if (surfaceHolder.getSurface() == null) {
				// preview surface does not exist
				return;
			}

			mCamera.setDisplayOrientation(90);

			// stop preview before making changes
			try {
				mCamera.stopPreview();
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "停止预览异常：", e);
				// ignore: tried to stop a non-existent preview
			}

			// make any resize, rotate or reformatting changes here

			// start preview with new settings
			try {
				mCamera.setPreviewDisplay(surfaceHolder);
				mCamera.startPreview();

			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "相机开启预览异常：", e);
			}
		}

		@Override
		public void surfaceCreated(SurfaceHolder holder) {
			// The Surface has been created, now tell the camera where to draw
			// the preview.
			if (Utils.debug)
				Log.e(TAG, "创建surface，surfaceCreated");

			try {
				mCamera.setPreviewDisplay(holder);
				mCamera.startPreview();

				cameraSizeList = getCamreaPx(mCamera);

			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "开启预览相机异常：", e);
			}
		}

		@Override
		public void surfaceDestroyed(SurfaceHolder holder) {
			if (Utils.debug)
				Log.e(TAG, "surface被销毁，surfaceDestroyed");

		}
	}

	/** 获取摄像头支持的分辨率 */
	private List<VideoSizeT> getCamreaPx(Camera mCamera) {
		List<VideoSizeT> cameraSizeList = new ArrayList<VideoCaptureActivity.VideoSizeT>();

		List<Size> sizes = mCamera.getParameters().getSupportedPreviewSizes();
		for (Size size : sizes) {
			Log.i(TAG, "支持预览尺寸，宽：" + size.width + "，高：" + size.height);
			cameraSizeList.add(new VideoSizeT(size.width, size.height));
		}

		frameRates = mCamera.getParameters().getSupportedPreviewFrameRates();
		for (Integer size : frameRates) {
			Log.i(TAG, "支持预览帧速：" + size);
		}
		return cameraSizeList;
	}

	private class VideoSizeT {
		public int width;
		public int height;

		public VideoSizeT(int width, int height) {
			this.width = width;
			this.height = height;

		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == BaseMsgRoom.REQ_TAKE_BIG_FILES && resultCode == Activity.RESULT_OK) {
			if (data != null) {
				setResult(resultCode, data);
			}
		}
		finish();

	}

}
