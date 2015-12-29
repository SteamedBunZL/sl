package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONStringer;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.PopupWindow.OnDismissListener;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.engine.BlogOpea;
import com.tuixin11sms.tx.engine.ReleaseBlogOpea;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.BlogMusicSeekBar;

public class AddMyBlogActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "AddMyBlogActivity";

	private LocationReceiver locationReceiver;

	protected static final int RECORD_PAUSE = 0;
	protected static final int RECORD_PLAY = 1;
	
	public static final String BLOGIMG_URL = "blogimg_url";
	public static final String BLOGADU_URL = "blogadu_url";
	public static final String BLOGIMG_PATH = "blogimg_path";
	public static final String BLOGADU_PATH = "blogadu_path";
	public static final String BLOGADU_TIME = "blogadu_time";

	// 头像和相册图片的获取和处理的相关参数
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	public static final int GET_ALBUM_IMG_FROM_CAMERA = 99;// 拍照上传相册图片
	public static final int GET_ALBUM_IMG_FROM_GALLERY = 103;// 从图片选择要上传的相册照片
	public static final int EDIT_ALBUM_IMG = 102;// 用EditHeadIcon编辑相册图片

	protected final static int HANDLE_RECORDER_ERROR = 1;
	protected final static int FLUSH_PROGRESS_TIME = 2;
	protected final static int FLUSH_SEEK_TEIME = 3;
	protected final static int CANCLE_SHORT_RECORD = 10;
	protected final static int CANCLE_LONG_RECORD = 11;
	protected final static int FLUSH_VOLUEM_AN = 2089;
	protected final static int RECORD_TIME_SHORT = 12;// 录音时间太短
	protected final int RECORD_END_TIME = 120;
	protected final int LONG_RECORD_EDN_TIME = 420;
	private String mImagePath;// 头像图片本地路径
	private UpdateReceiver upReceiver;
	// SharedPreferences prefs;
	// Editor editor;
	private String appurl, applog;
	private int appver;
	private int newappver;
	private boolean checkver;
	private Timer outtime;
	protected Toast unInitRecordToast;
	protected LayoutInflater mInflate;
	public RecorderPopupWindow mRecordPopupWindow;

	private ImageView myblog_add_ok;
	private ImageView iv_myblog_place;
	private TextView tv_myblog_place;
	private TextView tv_addblog_time;
	private boolean isAddImg = false;
	private boolean isAddAudio = false;
	private boolean isAddMsg = false;

	private EditText edit_msg;
	private TextView tv_msg_num;
	private LinearLayout back;
	private LinearLayout myblog_place;
	private ImageView bt_myblog_add_img;
	private ImageView bt_myblog_add_aduio;
	private RelativeLayout rl_myblog;
	private ReleaseBlogOpea opea;
	private LinearLayout ll_addblog_audio;

	private boolean isRecorded;
	protected boolean isLocationed;// 是否定位過

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ReleaseBlogOpea.GET_LOCATION:
				Toast.makeText(AddMyBlogActivity.this,
						R.string.msgroom_locationing_toast, Toast.LENGTH_SHORT)
						.show();
				break;
			case ReleaseBlogOpea.RECORD_FINISH:
				isRecorded = true;
				isAddAudio = true;
				isShowSendButton(isAddImg, isAddAudio, isAddMsg);
				TXMessage txmsg_recoed = (TXMessage) msg.obj;
				if (txmsg_recoed != null) {
					blogmsg.setAduPath(txmsg_recoed.msg_path);
					blogmsg.setAduUrl(txmsg_recoed.msg_url);
					blogmsg.setAduTime(txmsg_recoed.audio_times);
					sb_addblog_process
							.setMax((int) txmsg_recoed.audio_times * 1000);
					bt_myblog_add_aduio
							.setImageResource(R.drawable.add_audio_ok);
					ll_addblog_audio.setVisibility(View.VISIBLE);
					tv_addblog_time.setText(MessageUtil
							.getRecordTime(txmsg_recoed.audio_times));
				}
				break;
			case ReleaseBlogOpea.GET_LOCATION_CITY:
				String city = (String) msg.obj;
				iv_myblog_place.setImageResource(R.drawable.place_ok);
				tv_myblog_place.setText("所在城市：" + city);
				blogmsg.setCity(city);
				tv_myblog_place.setTextColor(Color.BLACK);
				myblog_place.setBackgroundResource(R.color.place_bg);
				isLocationed = true;
				cancelProgressDialog();
				break;
			case ReleaseBlogOpea.RELEASE_OK:
				Object[] rel_obj = (Object[]) msg.obj;
				boolean isOK = (Boolean) rel_obj[0];
				BlogMsg rel_blog = (BlogMsg) rel_obj[1];
				if (isOK) {
					Toast.makeText(AddMyBlogActivity.this, "发布成功", 0).show();

					if (rel_blog != null) {
						blogmsg.setUid(TX.tm.getUserID());
						blogmsg.setMid(rel_blog.getMid());
						blogmsg.setTime(rel_blog.getTime());
						blogmsg.setMdiaInfo(getMediaInfo(blogmsg));
					}
					if (isAddMsg && !isAddAudio && !isAddImg
							&& Utils.isNull(blogmsg.getImgUrl())) {
						// 判断如果是纯文字信息，进行截图
						edit_msg.setFocusable(false);
						String filepath = opea.getMsgImg(edit_msg);
						blogmsg.setImgPath(filepath);
						blogmsg.setType(BlogMsg.MSG);
					} else if (!isAddMsg && !isAddAudio && isAddImg) {
						blogmsg.setType(BlogMsg.IMG);
					} else if (!isAddMsg && isAddAudio && isAddImg) {
						blogmsg.setType(BlogMsg.IMG_AUDIO);
					} else if (!isAddMsg && isAddAudio && !isAddImg) {
						blogmsg.setType(BlogMsg.AUDIO);
					}
					mSess.getBlogDao().update(blogmsg);
					cancelProgressDialog();
					AddMyBlogActivity.this.setResult(RESULT_OK);
					AddMyBlogActivity.this.finish();
				} else {
					cancelProgressDialog();
					Toast.makeText(AddMyBlogActivity.this, "发布失败", 0).show();
				}

				break;
			case BlogOpea.SEEDBAR_PROCESS:
				Object[] result = (Object[]) msg.obj;

				if (txmsg.PlayAudio == RECORD_PLAY) {
					sb_addblog_process.setProgress((Integer) result[1]);
					blogmsg.setAdu_process((Integer) result[1]);
				}
				
				break;
			case BlogOpea.RECORD_PLAY_FINISH:
				if (txmsg == null)
					txmsg = new TXMessage();
				txmsg.PlayAudio = RECORD_PAUSE;
				sb_addblog_process.setProgress(0);
				bt_myblog_add_aduio.setImageResource(R.drawable.add_audio_ok);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//
		super.onCreate(savedInstanceState);

		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		mRecordPopupWindow = new RecorderPopupWindow();
		blogmsg = new BlogMsg();
		setContentView(R.layout.activity_myblog_add);

		init();

		opea = mSess.getReleaseBlogOpea();
		opea.setParam(mRecordPopupWindow, volumeResource, handler,
				mGroupAsynloader);
		mInflate = LayoutInflater.from(this);
		isLocationed = false;
		isRecorded = false;

		opeaAudio = mSess.getBlogOpea();
		opeaAudio.setHandler(handler);
		
		getMsg();
	}

	// 从赞获取
	public void getMsg() {
		Intent intent = getIntent();
		if (intent == null) {
			return;
		}
		String blogimg_url = intent.getStringExtra(BLOGIMG_URL);
		String blogimg_path = intent.getStringExtra(BLOGIMG_PATH);

		String blogadu_url = intent.getStringExtra(BLOGADU_URL);
		String blogadu_path = intent.getStringExtra(BLOGADU_PATH);
		long blogadu_time = intent.getLongExtra(BLOGADU_TIME, 0);

		blogmsg.setImgUrl(blogimg_url);
		blogmsg.setImgPath(blogimg_path);
		blogmsg.setAduUrl(blogadu_url);
		blogmsg.setAduPath(blogadu_path);
		blogmsg.setAduTime(blogadu_time);

		// 界面显示
		if (!Utils.isNull(blogimg_url) && !Utils.isNull(blogimg_path)) {
			// 设置图片
			Bitmap bit = Utils.readBitMap(blogimg_path, false);
			if(bit != null){
				bit = Utils.ResizeBitmap(Utils.ImageCrop(bit), 145);
				bt_myblog_add_img.setImageBitmap(bit);
				isAddImg = true;
				isShowSendButton(isAddImg, isAddAudio, isAddMsg);
			}
			
		}
		if (!Utils.isNull(blogadu_url) && !Utils.isNull(blogadu_path)) {
			// 设置音频
			isRecorded = true;
			isAddAudio = true;
			isShowSendButton(isAddImg, isAddAudio, isAddMsg);
			sb_addblog_process.setMax((int) blogadu_time * 1000);
			bt_myblog_add_aduio.setImageResource(R.drawable.add_audio_ok);
			ll_addblog_audio.setVisibility(View.VISIBLE);
			tv_addblog_time.setText(MessageUtil.getRecordTime(blogadu_time));
			isShowSendButton(isAddImg, isAddAudio, isAddMsg);
		}
	}

	protected void init() {
		edit_msg = (EditText) this.findViewById(R.id.et_myblog_msg);
		tv_msg_num = (TextView) this.findViewById(R.id.tv_myblog_msg_num);

		bt_myblog_add_img = (ImageView) this
				.findViewById(R.id.bt_myblog_add_img);
		bt_myblog_add_aduio = (ImageView) this
				.findViewById(R.id.bt_myblog_add_aduio);
		back = (LinearLayout) this.findViewById(R.id.btn_back_left);
		myblog_place = (LinearLayout) this.findViewById(R.id.myblog_place);
		ll_addblog_audio = (LinearLayout) this
				.findViewById(R.id.ll_addblog_audio);
		rl_myblog = (RelativeLayout) this.findViewById(R.id.rl_myblog);
		myblog_add_ok = (ImageView) this.findViewById(R.id.myblog_add_ok);
		iv_myblog_place = (ImageView) this.findViewById(R.id.iv_myblog_place);
		tv_myblog_place = (TextView) this.findViewById(R.id.tv_myblog_place);
		tv_addblog_time = (TextView) this.findViewById(R.id.tv_addblog_time);
		sb_addblog_process = (BlogMusicSeekBar) this
				.findViewById(R.id.sb_addblog_process);

		edit_msg.addTextChangedListener(mTextWatcher);
		bt_myblog_add_img.setOnClickListener(this);
		bt_myblog_add_aduio.setOnClickListener(this);
		back.setOnClickListener(this);
		myblog_add_ok.setOnClickListener(this);

		myblog_place.setOnClickListener(this);
		isShowSendButton(isAddImg, isAddAudio, isAddMsg);
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {

		if (!mRecordPopupWindow.isWindowShowing()) {
			Utils.isRrecord = true;
		}
		if (locationReceiver == null) {
			locationReceiver = new LocationReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_OK);
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_FAILED);
			filter.addAction(Constants.INTENT_ACTION_LBS_LOGIN_SUCCESSED);
			this.registerReceiver(locationReceiver, filter);
		}
		super.onStart();
	}

	@Override
	protected void onDestroy() {
		if (mRecordPopupWindow.isWindowShowing()) {
			opea.stopLongAudioRecord();
			Utils.isRrecord = true;
			mRecordPopupWindow.exitLongRecorderScreen();
		}
		if (opea.getAudioRecPlayer() != null) {
			opea.getAudioRecPlayer().stopPlay();
			opea.getAudioRecPlayer().stopRecord();
			// recordManager.setNeedExit(true);
		}
		if (locationReceiver != null) {
			unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	private String[] items;

	@Override
	public void onClick(View v) {

		switch (v.getId()) {
		case R.id.bt_myblog_add_img:
			showAlbumAddMenu(isAddImg);
			break;
		case R.id.bt_myblog_add_aduio:

			Utils.hideSoftInput(AddMyBlogActivity.this);

			if (!isRecorded) {
				mRecordPopupWindow.showLongRecorderScreen(rl_myblog);
			} else {
				if (txmsg == null) {
					txmsg = new TXMessage(blogmsg.getAduPath(),
							blogmsg.getAduUrl(), blogmsg.getAduTime());
				}
				if (txmsg.PlayAudio == RECORD_PLAY) {
					bt_myblog_add_aduio
							.setImageResource(R.drawable.add_audio_ok);
					sb_addblog_process.setProgress(0);
					opeaAudio.stopPlayAudioRecord();
				} else if (txmsg.PlayAudio == RECORD_PAUSE) {
					bt_myblog_add_aduio
							.setImageResource(R.drawable.add_audio_playing);
					opeaAudio.playAudioRecord(txmsg, txmsg.gmid);
				}
			}
			break;
		case R.id.btn_back_left:
			// AddMyBlogActivity.this.setResult(RESULT_OK);
			AddMyBlogActivity.this.finish();
			break;
		case R.id.myblog_add_ok:
			Utils.hideSoftInput(AddMyBlogActivity.this);
			if (Utils.checkNetworkAvailable1(AddMyBlogActivity.this)) {
				long id = Utils.createMsgId(TX.tm.getUserID() + "");
				blogmsg.setId(id);
				String mmsg = edit_msg.getText().toString();
				if (mmsg != null && !mmsg.equals("")) {
					blogmsg.setMmsg(mmsg);
				}
				showProgressDialog(true);
				opea.sendOKMsg(blogmsg);
			} else {
				Utils.startPromptDialog(AddMyBlogActivity.this,
						R.string.prompt, R.string.seach_network_title);
			}
			break;
		case R.id.myblog_place:
			// 获取地理位置
			if (isLocationed) {
				items = new String[] { "取消显示" };
				new AlertDialog.Builder(AddMyBlogActivity.this).setItems(items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								String s = items[which];
								if (s.equals("取消显示")) {
									iv_myblog_place
											.setImageResource(R.drawable.myblog_place_show);
									tv_myblog_place.setText("显示所在城市");
									tv_myblog_place.setTextColor(Color.parseColor("#888888"));
									isLocationed = false;
									blogmsg.setCity("");
									blogmsg.setGeo("");
									myblog_place
											.setBackgroundResource(R.drawable.sll_myblog_seekbar_style);
								}
							}
						}).show();
			} else {
				showProgressDialog(false);
				opea.startSendMeGeo();
			}

			break;
		}
	}

	private class UpdateReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

		}
	}

	// 添加相册图片的点击事件处理
	protected void showAlbumAddMenu(boolean isAdd) {
		int id;
		if (isAdd) {
			id = R.array.album_item_menu;
		} else {
			id = R.array.album_add_menu;
		}

		new AlertDialog.Builder(this).setTitle("更多选项")
				.setItems(id, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							// 从手机图库选择要上传的相册照片
							String storagePath = Utils
									.getStoragePath(AddMyBlogActivity.this);
							if (Utils.isNull(storagePath)) {
								return;
							}
							Intent intent = new Intent(
									Intent.ACTION_GET_CONTENT);
							intent.addCategory(Intent.CATEGORY_OPENABLE);
							intent.setType(MIME_TYPE_IMAGE_JPEG);
							startActivityForResult(intent,
									GET_ALBUM_IMG_FROM_GALLERY);
							// Utils.inPhoto = true;

						} else if (which == 1) {
							// 用手机拍照上传相册的照片
							if (!Utils.checkSDCard()) {
								if (Utils.debug)
									Log.i(TAG, "无SD卡");
								return;
							}
							String storagePath = Utils
									.getStoragePath(AddMyBlogActivity.this);
							if (Utils.isNull(storagePath)) {
								return;
							}
							File sddir = new File(storagePath);
							if (!sddir.exists() && !sddir.mkdirs()) {
								if (Utils.debug)
									Log.e(TAG, "拍照上传相册，创建神聊目录失败");
							}
							// StringBuffer tempPath = new
							// StringBuffer()
							// .append(storagePath)
							// .append("/self").append(".jpg");
							String tempPath = storagePath + "/self.jpg";
							Intent it = new Intent(
									"android.media.action.IMAGE_CAPTURE");
							if (Utils.debug)
								Log.i(TAG, "拍照上传的相册临时路径：" + tempPath);
							it.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(new File(tempPath)));
							it.putExtra(Utils.IMAGE_CAMRA, tempPath);
							startActivityForResult(it,
									GET_ALBUM_IMG_FROM_CAMERA);
							// Utils.inPhoto = true;
						} else if (which == 2) {
							// 删除已经存在的这个相册照片
							bt_myblog_add_img
									.setImageResource(R.drawable.myblog_img_normal);
							blogmsg.setImgPath("");
							blogmsg.setImgUrl("");
							isAddImg = false;
							isShowSendButton(isAddImg, isAddAudio, isAddMsg);
						}
					}
				}).show();
	}

	//解析媒体信息
	public String getMediaInfo(BlogMsg msg) {

		JSONStringer regster = null;
		if (msg != null) {
			try {
				String img = "";
				if (msg.getImgUrl() != null) {
					img = msg.getImgUrl();
				}
				regster = new JSONStringer().object().key("img").value(img)
						.key("adu").object();
				String aud_url = "";
				if (msg.getAduUrl() != null && !Utils.isNull(msg.getAduUrl())) {
					aud_url = msg.getAduUrl();
					long aud_time = msg.getAduTime();
					regster = regster.key("url").value(aud_url).key("t")
							.value(aud_time);
				}
				regster.endObject().key("geo").object();
				String geo = msg.getGeo();
				if (!Utils.isNull(geo)) {
					String[] split = new String[2];
					Double la = 0.0;
					Double lo = 0.0;
					if (!Utils.isNull(geo)) {
						split = geo.split(",");
						la = Double.parseDouble(split[0]);
						lo = Double.parseDouble(split[1]);
					}
					regster.key("la").value(la).key("lo").value(lo).key("ct")
							.value(msg.getCity());
				}
				return regster.endObject().endObject().toString();

			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;
	}

	public void isShowSendButton(boolean isAddImg, boolean isAddAudio,
			boolean isAddMsg) {
		if (isAddImg || isAddAudio || isAddMsg) {
			myblog_add_ok.setVisibility(View.VISIBLE);
		} else {
			myblog_add_ok.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == GET_ALBUM_IMG_FROM_GALLERY && data != null) {
				// 从图库选择要上传的相册照片
				// Utils.inPhoto = false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(this, R.string.prompt,
							R.string.userinfo_upload_failed_unknow);
					return;
				}
				cursor.moveToFirst();
				String imagePath = cursor.getString(1);
				cursor.close();
				Intent i = new Intent(this, EditHeadIcon.class);
				i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
				if (Utils.debug) {
					Log.i(TAG, "相册图片地址imagePath:" + imagePath);
				}
				i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.FROM_GALLERY);
				i.putExtra("isAlbum", true);
				startActivityForResult(i, EDIT_ALBUM_IMG);

			} else if (requestCode == GET_ALBUM_IMG_FROM_CAMERA) {
				// 拍照上传相册图片
				// Utils.inPhoto = false;
				try {
					String storagePath = Utils.getStoragePath(this);
					String imagePath = storagePath + "/self.jpg";
					Bitmap tempimg = Utils.fitSizeAutoImg(imagePath,
							Utils.msgroom_list_resolution);
					if (tempimg == null && data != null) {
						Bundle extras = data.getExtras();
						if (extras != null) {
							tempimg = (Bitmap) extras.get("data");
							if (Utils.createPhotoFile(tempimg, "self.jpg") == null) {
								return;
							}
						} else {
							String path = data.getDataString();
							if (!Utils.isNull(path)) {
								Uri uri = Uri.parse(path);
								imagePath = getRealPathFromURI(uri);
							}
						}
					}
					if (new File(imagePath).exists()) {
						Intent i = new Intent(this, EditHeadIcon.class);
						i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
						if (Utils.debug) {
							Log.i(TAG, "照片地址imagePath:" + imagePath);
						}
						i.putExtra(EditHeadIcon.STATE_COME,
								EditHeadIcon.FROM_CAMERA);
						i.putExtra("isAlbum", true);
						startActivityForResult(i, EDIT_ALBUM_IMG);
					}
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "异常", e);
				}
			} else if (requestCode == EDIT_ALBUM_IMG) {
				// 用EditHeadIcon编辑过的相册图片返回
				if (Utils.tempBm != null) {
					String storagePath = Utils.getStoragePath(this);
					final File sddir = new File(storagePath, Utils.IMAGE_PATH);
					if (!sddir.exists() && !sddir.mkdirs()) {
						if (Utils.debug)
							Log.e(TAG, "bitmapFromUrl---Create dir failed");
						sddir.mkdir();
					}

					final String tempImgPath = Utils.getStoragePath(mSess
							.getContext())
							+ File.separator
							+ Utils.IMAGE_PATH
							+ File.separator
							+ System.currentTimeMillis()
							+ ".jpg";

					try {
						// 生成合成的文件
						Utils.getAlbumFile(tempImgPath, Utils.tempBm);

						blogmsg.setImgPath(tempImgPath);

						mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
								new DownUploadListner() {

									@Override
									public void onFinish(FileTaskInfo taskInfo) {
										blogmsg.setImgUrl(taskInfo.mServerHost
												+ ":" + taskInfo.mPath + ":"
												+ taskInfo.mTime);

										// String fileName = new MD5()
										// .getMD5ofStr(taskInfo.mServerHost
										// + ":"
										// + taskInfo.mPath
										// + ":" + taskInfo.mTime);

										String fileName = taskInfo.mTime + "";

										try {
											Utils.creatBlogFile(tempImgPath,
													sddir, fileName);
											if (Utils.debug) {
												Log.i(TAG, "创建瞬间图片成功");
											}
										} catch (IOException e) {
											if (Utils.debug) {
												Log.e(TAG, "创建瞬间图片异常", e);
											}
										}

									}
								}, null);
						Bitmap smallDstBitmap = Utils.ResizeBitmap(
								Utils.ImageCrop(Utils.tempBm), 145);

						blogmsg.setImg(Utils.tempBm);

						bt_myblog_add_img.setImageBitmap(smallDstBitmap);
						isAddImg = true;
						isShowSendButton(isAddImg, isAddAudio, isAddMsg);
					} catch (Exception e) {
						if (Utils.debug) {
							Log.w(TAG, "合成大小图文件异常", e);
						}
						showToast("上传相册失败");
					}
				}
			}
		}
	}

	public String getRealPathFromURI(Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				AddMyBlogActivity.this);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setNegativeButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		promptDialog.show();
	}

	@Override
	protected void onResume() {
		if (upReceiver == null) {
			upReceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_VERSION_UPDATE);
			registerReceiver(upReceiver, filter);
		}
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (locationReceiver != null) {
			this.unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		super.onStop();
	}

	@Override
	protected void onPause() {
		// Utils.hideSoftInput(thisContext);
		if (upReceiver != null) {
			unregisterReceiver(upReceiver);
			upReceiver = null;
		}

		if (txmsg != null) {
			txmsg.PlayAudio = RECORD_PAUSE;
			opeaAudio.stopPlayAudioRecord();
		}

		super.onPause();
	}

	private void timeCancel() {
		if (outtime != null) {
			outtime.cancel();
		}
	}

	/** 录音的popupWindow */
	public class RecorderPopupWindow {
		private int recordWindowW, recordWindowH;
		private int longrecordWindowW, longrecordWindowH;
		protected View popupWindow_view;
		protected PopupWindow recordPopup;
		protected ImageView volumeImgView;
		protected ImageView longvolumeImgView;
		protected TextView recordPopupText1;
		protected TextView recordPopupText2;
		protected TextView progressTime;
		protected View popupWindow_view_long;
		protected PopupWindow longrecordPopup;
		protected Button canclebtn;
		protected Button beginRecord;
		protected TextView currentTime;
		protected SeekBar volumeSeek;
		protected Timer progressTimer;
		protected TimerTask progressTask;
		protected Timer seekTimer;
		protected TimerTask seekTask;

		protected Timer recordtime;
		protected TimerTask recordTask;
		protected Timer longRecordTimer;
		protected TimerTask longRecordTask;

		private Handler recordWindowHandler;
		private ImageView beginRecord_point;

		public RecorderPopupWindow() {
			recordWindowHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int resid;
					switch (msg.what) {
					case HANDLE_RECORDER_ERROR:
						switch (msg.arg1) {
						case RecordListener.ERR_FAILCREATEFILE:
							resid = R.string.record_failcreatefile;
							break;
						case RecordListener.ERR_DEVNOTINITED:
							resid = R.string.record_uninit;
							break;
						case RecordListener.ERR_DEVFILEERR:
							resid = R.string.record_devfileerr;
							break;
						case RecordListener.ERR_INTERNALERR:
							resid = R.string.record_internal;
							break;
						default:
							resid = R.string.record_internal;
							break;
						}
						if (longrecordPopup != null
								&& longrecordPopup.isShowing()) {

							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							opea.stopLongAudioRecord();
							beginRecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							longrecordPopup.dismiss();
						}
						if (recordPopup != null && recordPopup.isShowing()) {
							recordPopup.dismiss();
						}

						if (unInitRecordToast == null) {
							unInitRecordToast = Toast.makeText(thisContext,
									resid, Toast.LENGTH_SHORT);
						}
						unInitRecordToast.show();

						break;
					case FLUSH_PROGRESS_TIME:
						if (opea.getAudioRecPlayer() != null) {
							int ms = opea.getAudioRecPlayer()
									.getAudioDuration();
							String str = Utils.formatDurationTimes(ms);
							if (Utils.debug) {
								Log.i("bobo", "FLUSH_PROGRESS_TIME" + str);
							}
							progressTime.setText(str);
						}
						break;

					case FLUSH_SEEK_TEIME:
						int mss = opea.getAudioRecPlayer().getAudioDuration();
						if (Utils.debug) {
							Log.i("bobo", "长录音时间长audio time is:" + mss);
						}

						// if ((mss / 1000) <= LONG_RECORD_EDN_TIME) {
						if (mss <= LONG_RECORD_EDN_TIME) {// 取到的mss单位已经是秒了，不能再除以1000
							currentTime.setText(Utils.formatDurationTimes(mss));
							volumeSeek.setProgress(mss);
						}

						break;
					case CANCLE_LONG_RECORD:
						// 长录音时间到，取消长录音
						mRecordPopupWindow.stopLongRecord();

						break;
					case RECORD_TIME_SHORT:
						// 录音时间太短
						Toast.makeText(AddMyBlogActivity.this,
								R.string.record_time_short, Toast.LENGTH_SHORT)
								.show();
						break;

					case FLUSH_VOLUEM_AN:
						if (volumeImgView != null) {
							int reid = msg.arg1;
							volumeImgView.setImageResource(reid);
							volumeImgView.invalidate();
						}

						if (longvolumeImgView != null) {

							int reid = msg.arg1;
							longvolumeImgView.setImageResource(reid);
							longvolumeImgView.invalidate();
						}
						break;
					}
				}
			};
		}

		// 是否有window在展示
		public boolean isWindowShowing() {
			if (recordPopup != null && recordPopup.isShowing()) {
				return true;
			}
			if (longrecordPopup != null && longrecordPopup.isShowing()) {
				return true;
			}
			return false;

		}

		// 显示长录音界面
		public void showLongRecorderScreen(View parent) {

			if (longrecordPopup == null) {
				// 获得长按录音窗口宽高
				Bitmap windowImg = BitmapFactory.decodeResource(getResources(),
						R.drawable.longrecordbg);

				if (windowImg != null) {
					longrecordWindowW = windowImg.getWidth();
					longrecordWindowH = windowImg.getHeight();
				}

				windowImg.recycle();

				popupWindow_view_long = mInflate.inflate(
						R.layout.longrecordscreen, null);
				longrecordPopup = new PopupWindow(popupWindow_view_long,
						longrecordWindowW, longrecordWindowH, true);// 创建PopupWindow实例
				canclebtn = (Button) popupWindow_view_long
						.findViewById(R.id.longrecord_del);
				beginRecord = (Button) popupWindow_view_long
						.findViewById(R.id.longrecord_begin);
				beginRecord_point = (ImageView) popupWindow_view_long
						.findViewById(R.id.longrecord_point);
				longvolumeImgView = (ImageView) popupWindow_view_long
						.findViewById(R.id.longrecordscreen_MicValoum);
				currentTime = (TextView) popupWindow_view_long
						.findViewById(R.id.curtime);
				volumeSeek = (SeekBar) popupWindow_view_long
						.findViewById(R.id.longrecord_seekBar);
				volumeSeek.setMax(421);
				popupWindow_view_long.setFocusable(true);
				popupWindow_view_long.setFocusableInTouchMode(true);
				longrecordPopup.setFocusable(true);
				popupWindow_view_long.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (Utils.isRrecord) {
								longrecordPopup.dismiss();
							}

							return true;
						}
						return false;
					}
				});
				longrecordPopup.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {

					}
				});

				/**
				 * 长按录音的相关逻辑方法 开始录音按钮和点击发送按钮的点击事件
				 */
				beginRecord.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (Utils.isRrecord) {
							if (Utils
									.checkNetworkAvailable1(AddMyBlogActivity.this)) {
								opea.setHasRecordErr(false);
								if (!Utils.checkSDCard()) {// 无SD卡
									// toastList.add(Utils
									// .creatNoSdCardInfo(AddMyBlogActivity.this));

								} else {
									Utils.isRrecord = false;
									beginRecord_point.setVisibility(View.GONE);
									beginRecord.setText("立即发送");
									opea.longRecordAudioUpLoad();
								}

							} else {
								Toast.makeText(AddMyBlogActivity.this,
										R.string.msg_nonetstr, 1);
							}
						} else {
							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							mRecordPopupWindow.exitLongRecorderScreen();
							beginRecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							bt_myblog_add_aduio
									.setImageResource(R.drawable.add_audio_ok);
							ll_addblog_audio.setVisibility(View.VISIBLE);
							
							if (opea.getmLongAudioMsg() != null) {
								// 有等待发送的长录音消息，直接发送消息
								handler.obtainMessage(
										ReleaseBlogOpea.RECORD_FINISH,
										opea.getmLongAudioMsg()).sendToTarget();
								opea.sendAudioMsgAfterRocord(opea
										.getmLongAudioMsg());
								opea.setmLongAudioMsg(null);
							} else {
								opea.getAudioRecPlayer().stopRecord();
								if (!opea.getIsRecording()) {
									// recordManager.setRunning(false);
								}
								if (opea.getAudioRecPlayer() != null) {
									opea.stopAudioRecordSocket(false);
								}
							}
						}
					}
				});
				// 取消按钮点击事件
				canclebtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (opea.getAudioRecPlayer() != null
								&& opea.getAudioRecPlayer().isRecording()) {
							// 如果不在录音状态，则不做任何操作，直接退出界面
							opea.stopLongAudioRecord();
							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							beginRecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							opea.setCancelSendAudioMsg(true);// 取消音频发送
							mSess.mDownUpMgr.removeUploadTask(mSess.mDownUpMgr
									.getUploadTaskId(opea.getAudioRecPlayer()
											.getAudioFilePath()));

						}
						mRecordPopupWindow.exitLongRecorderScreen();
					}
				});
			}
			if (longrecordPopup != null) {
				longrecordPopup.showAtLocation(parent, Gravity.CENTER, 0, 0);
			}

		}

		public Handler getHandler() {
			return recordWindowHandler;

		}

		public void exitLongRecorderScreen() {
			if (longrecordPopup != null && longrecordPopup.isShowing()) {
				longrecordPopup.dismiss();
			}

		}

		// 关闭录音
		public void exitRecorderScreen() {

			if (recordPopup != null && recordPopup.isShowing()) {

				recordPopup.dismiss();

			}
		}

		public void longrecordSeekTime() {

			if (seekTimer != null) {
				if (seekTask != null) {
					seekTask.cancel();
				}
			}
			seekTask = new TimerTask() {
				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							FLUSH_SEEK_TEIME);
				}
			};
			seekTimer = new Timer(true);
			seekTimer.schedule(seekTask, 100, 500);

		}

		/**
		 * 长录音到7分钟自动结束的结束方法
		 */
		private void stopLongRecord() {
			opea.stopLongAudioRecord();
			// Utils.isRrecord = true;
			cancelLongRecordSeekTime();
			cancelLongRecordTimes();

		}

		/**
		 * 取消长录音的每隔半秒取录音时长，发消息更新popupWindow
		 */
		public void cancelLongRecordSeekTime() {
			if (seekTimer != null) {
				seekTimer.cancel();
				seekTimer = null;
			}
			if (seekTask != null) {
				seekTask.cancel();
				seekTask = null;
			}

		}

		public void cancelLongRecordTimes() {
			if (longRecordTimer != null) {
				longRecordTimer.cancel();
				longRecordTimer = null;
			}
			if (longRecordTask != null) {
				longRecordTask.cancel();
				longRecordTask = null;
			}
		}

		// 长按录音开始会自动计时7分钟后会停止录音

		public void longRecordTimes() {

			longRecordTimer = new Timer();

			longRecordTask = new TimerTask() {

				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							CANCLE_LONG_RECORD);

				}
			};

			longRecordTimer.schedule(longRecordTask,
					LONG_RECORD_EDN_TIME * 1000);

		}
	}

	TextWatcher mTextWatcher = new TextWatcher() {
		private CharSequence temp;
		private int editStart;
		private int editEnd;

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			//
			temp = s;
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			//
			// countNum.setText(s);//将输入的内容实时显示
		}

		@Override
		public void afterTextChanged(Editable s) {
			//
			editStart = edit_msg.getSelectionStart();
			editEnd = edit_msg.getSelectionEnd();

			if (temp.length() > 0) {
				isAddMsg = true;
			} else {
				isAddMsg = false;
			}
			isShowSendButton(isAddImg, isAddAudio, isAddMsg);
			tv_msg_num.setVisibility(View.GONE);
			if (temp.length() > 2000) {
				Toast.makeText(AddMyBlogActivity.this, "最多可以输入2000个字符",
						Toast.LENGTH_SHORT).show();
				tv_msg_num.setVisibility(View.VISIBLE);
				int leavenum = 2000 - temp.length();
				tv_msg_num.setText(leavenum + "");
				// s.delete(editStart - 1, editEnd);
				// int tempSelection = editStart;
				// edit_msg.setText(s);
				// edit_msg.setSelection(tempSelection);
			}
		}
	};

	private BlogMusicSeekBar sb_addblog_process;

	private BlogMsg blogmsg;

	/** gps定位后发送的广播 */
	class LocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(
					Constants.INTENT_ACTION_GET_LOCATION_OK)
					|| intent.getAction().equals(
							Constants.INTENT_ACTION_GET_LOCATION_FAILED)) {
				if (opea.getLocationStation() == null) {
					return;
				}
				Parcelable parcelable = intent
						.getParcelableExtra(Constants.EXTRA_LOCATION_KEY);
				Location location;
				double latitude;
				double longitude;
				if (parcelable != null) {
					location = (Location) parcelable;
					latitude = location.getLatitude();
					longitude = location.getLongitude();
				} else {
					latitude = 0;
					longitude = 0;
				}

				String s = TxData.public_latitude + ","
						+ TxData.public_longitude;
				blogmsg.setGeo(s);

				opea.getLocationStr(latitude, longitude);
				// mSess.getSocketHelper().sendGroupMsg(
				// gepTxmsgTemp);
			}
		}

	}

	private ProgressDialog pd;

	private BlogMusicSeekBar sb_myblog_process;

	private BlogOpea opeaAudio;

	private TXMessage txmsg;

	private void showProgressDialog(boolean isSend) {
		if (pd == null) {
			pd = new ProgressDialog(this);
			if (isSend) {
				pd.setMessage("正在发布");
			} else {
				pd.setMessage("正在获取");
			}
			pd.setCancelable(false);
		}
		if (!pd.isShowing()) {
			pd.show();
		}
	}

	private void cancelProgressDialog() {
		if (pd != null && pd.isShowing()) {
			pd.cancel();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {

		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// AddMyBlogActivity.this.setResult(RESULT_OK);
			AddMyBlogActivity.this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}

}
