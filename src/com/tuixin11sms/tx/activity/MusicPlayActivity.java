package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class MusicPlayActivity extends BaseActivity { 
	private static final String TAG = "MusicPlayActivity";

	private MediaPlayer mediaPlayer;
	private String audioFile;
	private SeekBar sb_music_progress;
	private int progress = 0;// mediaPlayer播放进度
	// private Timer timer;//获取播放进度的定时器
	private ImageView iv_play_music;
	private Timer timer;// 获取音乐播放进度的定时器

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (timer != null) {
				// 定时器没有取消的情况下
				progress = mediaPlayer.getCurrentPosition()*100/mediaPlayer.getDuration();
				if (Utils.debug) {
					Log.i(TAG, "音乐播放进度："+progress);
				}
				sb_music_progress.setProgress(progress);
			}

		};
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_music_player);

		audioFile = this.getIntent().getStringExtra(Constants.FILE_LOCAL_PATH);

		// LoginSessionManager lsn = LoginSessionManager
		// .getLoginSessionManager(txdata);
		// audioFile = lsn.mDownUpMgr.getStoragePath() + "/ylzs.mp3";

		((TextView) findViewById(R.id.tv_playing_music_name)).setText(new File(
				audioFile).getName());
		iv_play_music = (ImageView) findViewById(R.id.iv_play_music);
		sb_music_progress = (SeekBar) findViewById(R.id.sb_music_progress);

		// Uri myUri = Uri.parse("file://"+audioFile); // initialize Uri here

		// initMusicPlayer();
	}

	/** 初始化音乐播放器 */
	private void initMusicPlayer() {
		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			// mediaPlayer.setDataSource(getApplicationContext(), myUri);
			File file = new File(audioFile);
			FileInputStream fis = new FileInputStream(file);
			mediaPlayer.setDataSource(fis.getFD());
			// mediaPlayer.setDataSource(audioFile);
			mediaPlayer
					.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							iv_play_music
									.setBackgroundResource(R.drawable.play_music_button);
						}
					});
			if (Utils.debug) {
				Log.i(TAG, "音乐播放初始进度：" + progress);
			}
			mediaPlayer.prepare();
			mediaPlayer.start();
			if (progress > 0) {
				//如果上次播放进度不是0，则从上次位置开始播放
				sb_music_progress.setProgress(progress);
				mediaPlayer.seekTo(mediaPlayer.getDuration()
						* progress / 100);
			}
			timer = new Timer();
			timer.schedule(new TimerTask() {

				@Override
				public void run() {
					// 让handler获取播放器进度，更新进度条
					handler.sendEmptyMessage(0);
				}
			}, 500, 500);

			iv_play_music.setBackgroundResource(R.drawable.pause_music_button);
		} catch (IOException e) {
			Log.e(TAG, "Could not open file 【" + audioFile + "】 for playback.",
					e);
		}

		iv_play_music.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mediaPlayer.isPlaying()) {
					// 点击暂停
					iv_play_music
							.setBackgroundResource(R.drawable.play_music_button);
					mediaPlayer.pause();
				} else {
					// 点击播放
					iv_play_music
							.setBackgroundResource(R.drawable.pause_music_button);
					mediaPlayer.start();
				}
			}
		});

		sb_music_progress
				.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

					@Override
					public void onStopTrackingTouch(SeekBar seekBar) {
						int progress = seekBar.getProgress();
						if (Utils.debug)
							Log.i(TAG, "onStopTrackingTouch,progress:"
									+ progress);
						if (sb_music_progress.getProgress() < sb_music_progress
								.getMax()) {
							mediaPlayer.seekTo(mediaPlayer.getDuration()
									* progress / 100);
						}
					}

					@Override
					public void onStartTrackingTouch(SeekBar seekBar) {
						if (Utils.debug)
							Log.i(TAG, "onStartTrackingTouch");
					}

					@Override
					public void onProgressChanged(SeekBar seekBar,
							int progress, boolean fromUser) {
						if (Utils.debug)
							Log.i(TAG, "onProgressChanged  progress:"
									+ progress + ",fromUser:" + fromUser);
						// handler.sendEmptyMessage(progress);

					}
				});

	}

	// 为了处理来电情况的处理
	@Override
	protected void onStart() {
		super.onStart();
		initMusicPlayer();
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (timer!=null) {
			timer.cancel();
			timer = null;
		}
		mediaPlayer.stop();
		mediaPlayer.release();
	}

}
