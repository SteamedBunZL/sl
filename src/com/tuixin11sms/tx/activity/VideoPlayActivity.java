package com.tuixin11sms.tx.activity;

import java.io.File;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class VideoPlayActivity extends BaseActivity {
	private static final String TAG = "VideoPlayActivity";
	public static final String IS_PREVIEW_VIDEO = "is_preview_video";// 是否是预览视频状态
	public static final String VIDEO_DURATION = "video_duration";// 视频录制时长
	
	private boolean isRecordVideo = false;// 是否是自己录制的视频，如果“是”在点击取消按钮时则删除本地文件，否则不删除
	private MediaController mediaController;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_play_video);
		Intent intent = getIntent();
		final String filePath = intent
				.getStringExtra(Constants.FILE_LOCAL_PATH);
		final boolean isPreviewVideo = intent
				.getBooleanExtra(IS_PREVIEW_VIDEO, false);

		TextView tv_titleName = (TextView) findViewById(R.id.tv_title_name);
		final ImageView iv_preiewBtn = (ImageView) findViewById(R.id.iv_preview_video);
		RelativeLayout rl_send_bar = (RelativeLayout) findViewById(R.id.rl_send_and_cancel);
		TextView tv_video_duration = (TextView) findViewById(R.id.tv_video_duration);

		final VideoView vv_paly_video = (VideoView) findViewById(R.id.vv_play_video);
		
		if (!isPreviewVideo) {
			// 非预览视频状态，隐藏部分布局
			iv_preiewBtn.setVisibility(View.GONE);
			rl_send_bar.setVisibility(View.INVISIBLE);
			tv_video_duration.setVisibility(View.INVISIBLE);

			//设置mediaController与VideoView关联，可控制播放暂停等
			mediaController = new MediaController(this);
//			mediaController = new MediaController(this){
//				@Override
//				public void hide() {
//					mediaController.show(0);
//				}
//			};
			mediaController.setAnchorView(vv_paly_video);
			vv_paly_video.setMediaController(mediaController);
			
			
		} else {
			String videoDuration = intent.getStringExtra(VIDEO_DURATION);
			if (TextUtils.isEmpty(videoDuration)) {
				isRecordVideo = false;//从手机视频库中选择的视频文件
			}else {
				isRecordVideo = true;//用手机录制的
			}
			iv_preiewBtn.setVisibility(View.VISIBLE);
			rl_send_bar.setVisibility(View.VISIBLE);
			if (isRecordVideo) {
				tv_video_duration.setText(videoDuration);
			}else {
				tv_video_duration.setVisibility(View.INVISIBLE);
			}
			
			
		}
		
		
		Uri videoUri = Uri.parse("file://" + filePath);
		vv_paly_video.setOnErrorListener(new MediaPlayer.OnErrorListener() {

			@Override
			public boolean onError(MediaPlayer mp, int what, int extra) {
				// 播放视频文件异常
				if (Utils.debug) {
					Log.e(TAG, "what:" + what + ",extra:" + extra);
				}
				Intent intent = new Intent(thisContext,
						UnkownFileActivity.class);
				intent.putExtra(Constants.FILE_LOCAL_PATH, filePath);
				startActivity(intent);
				thisContext.finish();
				return true;
			}
		});

		
		vv_paly_video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
			
			@Override
			public void onCompletion(MediaPlayer mp) {
				if (isPreviewVideo) {
					iv_preiewBtn.setVisibility(View.VISIBLE);
				}
				
			}
		});
		
		vv_paly_video.setVideoURI(videoUri);
		
		iv_preiewBtn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				vv_paly_video.start(); 
				iv_preiewBtn.setVisibility(View.GONE);
			}
		});
		
		if (!isPreviewVideo) {
			//非预览状态，进来直接播放
			vv_paly_video.start();
		}
		
		TextView tv_cancel = (TextView) rl_send_bar.findViewById(R.id.tv_cancel_send_video);
		TextView tv_sendVideo = (TextView) rl_send_bar.findViewById(R.id.tv_send_video);
		tv_cancel.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 取消发送，直接finish，删除录制的视频文件？
				if(vv_paly_video.isPlaying()){
					vv_paly_video.stopPlayback();
				}
				if (isRecordVideo) {
					//录制的视频，点击取消按钮则删除本地文件
					File videoFile = new File(filePath);
					boolean result = videoFile.delete();
					if(Utils.debug)Log.i(TAG, "删除视频文件【"+videoFile.getPath()+"】结果："+result);
				}
				thisContext.finish();
				
			}
		});
		
		tv_sendVideo.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 发送视频
				Intent data = new Intent();
				ArrayList<String> fileList = new ArrayList<String>();
				fileList.add(filePath);
				data.putStringArrayListExtra(BaseMsgRoom.MSG_FILE_LIST, fileList);
				thisContext.setResult(Activity.RESULT_OK, data);
				thisContext.finish();
			}
		});
	}

	
}
