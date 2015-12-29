package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;

import ru.truba.touchgallery.TouchView.TouchImageView;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.PraisedAnim;
import com.tuixin11sms.tx.view.ProgressTextBar;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.PraiseMsgCallBack;

/**
 * 聊天室里下载查看大图页面,头像大图页面 TODO 这里的”重试“按钮有机会显示，需要抽时间清理一下 2014.03.11
 * 
 */
public class EditSendImg extends BaseActivity {
	private static final String TAG = EditSendImg.class.getSimpleName();
	
	private static final int FLUSH_PRAISE_BUTTON = 0x17;
	
	private String imageFilePath;// 大图存储的本地路径
	private String imgUrl;// 大图的下载地址url
	private long msgid;
	private String avatarDir;
	private long userid;
	private String save_prompt;
	private Display display;
	private Bitmap bm;
	private String up_imageing;
	private String up_imagefail;
	private TouchImageView imgview;
	private ProgressDialog progress;
	private ProgressTextBar customProgress;
	private View ll_loading_img;
	private TextView tv_loadingText;
	private Button btn_retry;
	private TextView ok, cancle;
	private TextView btn_savePicture;
	private View rl_bottom_bar;
	private Timer outtime;
	private boolean ispress;
	/** 用来判定是否是显示头像，当comeToPageState==99的时候，显示头像，否则显示聊天室大图 */
	public int comeToPageState = 0;
	public static final int COME_AVATAR = 99; // 头像查看大图
	public final static String TOSTATE = "tostate";
	public final static String USER_ID = "userId";
	public final static String TXMESSAGE = "txMessage";
	public final static String FROM_MSG_ROOM = "fromMsgRoom";//从聊天室跳转过来的
	public final static String USER_URL = "user_url";
	public final static String LOCAL = "local";
	private TXMessage txmsg;
	private boolean isFromMsgRoom = false;//是否从聊天室跳转过来
	private ImageView iv_praise_img;
	private PraiseMsgCallBack praiseResultCallback;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		if (progress == null) {
			progress = new ProgressDialog(EditSendImg.this);
		}

		display = getWindowManager().getDefaultDisplay();
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_editimage);
		File avatarFolder = new File(Utils.getStoragePath(this),
				Utils.AVATAR_PATH);
		if (!avatarFolder.exists()) {
			avatarFolder.mkdirs();
		}
		up_imagefail = getResources().getString(R.string.down_img_fail);
		up_imageing = getResources().getString(R.string.down_imging);
		save_prompt = this.getResources().getString(R.string.save_img);
		avatarDir = avatarFolder.getAbsolutePath();
		imgview = (TouchImageView) findViewById(R.id.img_src);
		imgview.setVisibility(View.GONE);
		btn_savePicture = (TextView) findViewById(R.id.head_Confirm);// 右上角保存按钮
		rl_bottom_bar = findViewById(R.id.edit_bottom);// 下面的“确定”和“取消”按钮布局
		customProgress = (ProgressTextBar) findViewById(R.id.edit_ProgressTextBar);
		ll_loading_img = findViewById(R.id.edit_no_bigimg_layout);// 图片加载中布局
		tv_loadingText = (TextView) findViewById(R.id.edit_load_context);// 图片正在加载文字
		btn_retry = (Button) findViewById(R.id.edit_restart);// 重试
		Intent intent = getIntent();
		comeToPageState = intent.getIntExtra(TOSTATE, 0);
		String templocal = intent.getStringExtra(LOCAL);// 本地图片地址，是GroupMsgRoom和SingleMsgRoom传递过来的
		if (TextUtils.isEmpty(templocal)) {
			//TODO 这是从聊天室传过来的查看大图消息，处理点击赞事件
			isFromMsgRoom = intent.getBooleanExtra(FROM_MSG_ROOM,false);
			txmsg = intent.getParcelableExtra(TXMESSAGE);
			iv_praise_img = (ImageView) findViewById(R.id.iv_praise_img);//赞按钮
			iv_praise_img.setVisibility(View.GONE);
			if (txmsg != null) {
				msgid = txmsg.msg_id;
				imageFilePath = txmsg.msg_path;
				imgUrl = txmsg.msg_url;
				if (Utils.debug) {
					Log.i(TAG, "第一次打印msg:" + txmsg.toString());
				}
				if (txmsg.was_me) {
					btn_savePicture.setVisibility(View.GONE);
				}
				rl_bottom_bar.setVisibility(View.GONE);
				
				if(isFromMsgRoom&&!txmsg.was_me&&txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS){
					//群图片消息
					TxGroup txgroup = TxGroup.getTxGroup(mSess.getContentResolver(), txmsg.group_id);
					if(txgroup!=null&&txgroup.isOfficialGroup()){
						//官方聊天室
						setPraiseBackground(txmsg.praisedState);
						iv_praise_img.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								if(!txmsg.isCanBePraise()){
									//不可取消赞（超时后，赞按钮不显示，所以不会是赞点击操作）
									if(Utils.debug){
										showToast("取消赞超时");
									}
									return;
								}
								
								if(praiseResultCallback==null){
									praiseResultCallback = new PraiseNoticeImpl.PraiseMsgCallBack(){
										
										@Override
										public void onSuccess(long gmid,int praiseFlag) {
											txmsg.praisedState = praiseFlag;
											handler.sendEmptyMessage(FLUSH_PRAISE_BUTTON);
											
										}
										
										@Override
										public void onFailed(long gmid,int praiseFlag) {
											String operatName = txmsg.praisedState == PraiseNotice.ACTION_NONE?"点赞":"取消赞";
											showToast(operatName+"操作失败");
										}
										
									};
									mSess.getPraiseNoticeDao().registePraiseResultCallback(praiseResultCallback);
								}
								
								if (txmsg.praisedState == PraiseNotice.ACTION_NONE) {
									// 状态是没有被赞，执行赞
									PraisedAnim panim = new PraisedAnim(iv_praise_img);
									panim.start();

								}
								mSess.getPraiseNoticeDao().praiseMsg(txmsg.group_id, txmsg.partner_id, txmsg.gmid, txmsg.praisedState);
								
							}
						});
					}
				}
				
			} else {
				btn_savePicture.setVisibility(View.GONE);
			}

			if (comeToPageState != 0) {
				// 显示头像大图
				userid = intent.getLongExtra(USER_ID, -1);
				imgUrl = intent.getStringExtra(USER_URL);
				btn_savePicture.setVisibility(View.VISIBLE);
				rl_bottom_bar.setVisibility(View.GONE);
				imageFilePath = tempPath(Utils.AVATAR_PATH, true);
			} else {
				imageFilePath = tempPath(Utils.IMAGE_PATH, true);
				if (txmsg != null && txmsg.was_me) {
					imageFilePath = txmsg.msg_path;
				}
			}
		} else {
			// templocal为本地的一个地址，从聊天室传递过来的
			imageFilePath = templocal;
			btn_savePicture.setVisibility(View.GONE);//选择相册照片，隐藏保存按钮
			ll_loading_img.setVisibility(View.GONE);
			imgview.setVisibility(View.VISIBLE);
		}

		if (userid == TX.TUIXIN_MAN) {
			ll_loading_img.setVisibility(View.GONE);
			bm = BitmapFactory.decodeResource(this.getResources(),
					R.drawable.tuixin_manager_320);
			imgview.setImageBitmap(bm);
			imgview.setVisibility(View.VISIBLE);
		} else {
			if (!Utils.isNull(imageFilePath)) {// 有路径
				if (new File(imageFilePath).exists()) {
					// 文件存在则去加载图片，否则bitmap decode的时候会报FileNotFoundException异常
					bm = getBigImgByPath(imageFilePath);
				}
				if (bm == null) {
					dowloadBigImg(imageFilePath, imgUrl);
				}
				if (bm != null) {// 读大图
					if (Utils.debug)
						Log.i(TAG, "++++++++++++++2+++++++++++++++++");
					imgview.setImageBitmap(bm);
					progress.cancel();
					ll_loading_img.setVisibility(View.GONE);
					imgview.setVisibility(View.VISIBLE);
				} else {
					if (Utils.debug)
						Log.i(TAG, "++++++++++++++3+++++++++++++++++");
					if (progress != null) {
						progress.cancel();
						progress = new ProgressDialog(this);
					}
					int nettype = Utils.getNetworkType(this);
					if (nettype == Utils.NET_NOT_AVAILABLE) {
						Message mscr = new Message();
						mscr.what = 3;
						handler.sendMessage(mscr);
						progress.cancel();
						return;
					}
				}
			} else {
				// TODO 会执行到这里，之前见过一次，待检查怎么出现？2014.01.20
				if (Utils.debug) {
					Log.e(TAG, "imageFilePath=null,中奖了");
					Toast.makeText(getApplicationContext(), "中奖了！！！居然执行到了这里！",
							1).show();
				}
				bm = getBigImg(msgid);
				if (bm == null) {
					if (imgUrl != null) {

						bm = receiveSameImg(imgUrl);
					}
				}
				if (bm != null) {
					imgview.setImageBitmap(bm);
					progress.cancel();
				} else {
					dowloadBigImg(imageFilePath, imgUrl);
				}

			}
		}
		this.ok = (TextView) this.findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {
			public void onClick(View arg0) {
				if (Utils.debug) {
					if (Utils.debug)
						Log.i(TAG, "+++++++++++" + ok);
				}

				Intent i = new Intent();
				i.putExtra("srcurl", imageFilePath);
				EditSendImg.this.setResult(RESULT_OK, i);
				EditSendImg.this.finish();

			}
		});
		this.cancle = (TextView) this.findViewById(R.id.cancle);
		cancle.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (Utils.debug) {
					if (Utils.debug)
						Log.i(TAG, "+++++++++++" + cancle);
				}

				//先不回收，回收会造成TouchImageView onDraw（） 302行异常  2014.03.11 shc
//				if (bm != null && !bm.isRecycled()) {
//					bm.recycle();
//					bm = null;
//				}
				// Intent i = new Intent();
				// // i.putExtra("image", dstBitmap);
				// EditImage1.this.setResult(RESULT_OK, i);
				EditSendImg.this.finish();
			}
		});

		btn_savePicture.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				if (Utils.debug) {
					if (Utils.debug)
						Log.i(TAG, "+++++++++++" + btn_savePicture);
				}
				if(bm==null){
					Toast.makeText(EditSendImg.this, "图片尚未下载成功", Toast.LENGTH_LONG)
					.show();
					return;
				}
				String storagePath = Utils.getStoragePath(EditSendImg.this);
				String saveImgType = null;
				if (comeToPageState != 0) {
					saveImgType = Utils.AVATAR_PATH;
				} else {
					saveImgType = Utils.IMAGE_PATH;
				}
				String tempPath = null;
				tempPath = tempPath(saveImgType, true);
				File shenliaoAvatar = new File(tempPath.toString());
				if (!shenliaoAvatar.exists()) {
					ispress = true;
					if (progress != null) {
						progress.cancel();
						progress = new ProgressDialog(EditSendImg.this);
					}
					progress.setMessage(getResources().getString(
							R.string.down_image_loading));
					progress.show();
					try {
						shenliaoAvatar.getParentFile().mkdirs();
						shenliaoAvatar.createNewFile();
						bm.compress(Bitmap.CompressFormat.JPEG, 100,
								new FileOutputStream(shenliaoAvatar));
						progress.cancel();
						Toast.makeText(EditSendImg.this, "保存成功", Toast.LENGTH_LONG)
						.show();
					} catch (Exception e) {
						if (Utils.debug)
							e.printStackTrace();
					}
				} else {
					duplicateImages(msgid);
					Toast.makeText(EditSendImg.this, save_prompt,
							Toast.LENGTH_LONG).show();
				}

			}
		});

		btn_retry.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				int nettype = Utils.getNetworkType(EditSendImg.this);
				if (nettype == Utils.NET_NOT_AVAILABLE) {
					Message mscr = new Message();
					mscr.what = 3;
					handler.sendMessage(mscr);
					// progressbar.setVisibility(View.GONE);
					progress.cancel();
					return;
				}
				Message msg1 = new Message();
				msg1.what = 6;
				handler.sendMessage(msg1);

				dowloadBigImg(imageFilePath, imgUrl);
			}

		});
	}

	public void initPath(String mDirName, String storagePath) {
		try {
			// 初始化即将保存到的文件夹目录

			StringBuffer tempPath = new StringBuffer().append(storagePath);
			String mLastPath = tempPath + "/" + mDirName;
			File mLastFolder = new File(mLastPath);
			if (!mLastFolder.exists()) {
				mLastFolder.mkdirs();
			}
			mLastFolder = null;
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	public boolean duplicateImages(long msgid) {
		String storagePath = Utils.getStoragePath(this);
		initPath(Utils.PHOTO_IMAGE_PATH, storagePath);
		FileInputStream mFileIn = null;
		FileOutputStream mFileOut;
		File mFile;
		try {
			// shenliao
			StringBuffer newPath = new StringBuffer().append(storagePath)
					.append("/").append(Utils.PHOTO_IMAGE_PATH).append("/")
					.append(msgid).append(".jpg");
			mFile = new File(newPath.toString());
			if (!mFile.exists()) {
				mFile.createNewFile();
			}
			// image
			String saveImgType = null;
			if (comeToPageState != 0) {
				saveImgType = Utils.AVATAR_PATH;
			} else {
				saveImgType = Utils.IMAGE_PATH;
			}
			String tempPath = null;
			tempPath = tempPath(saveImgType, true);
			mFileIn = new FileInputStream(tempPath.toString());

			mFileOut = new FileOutputStream(mFile);
			int readedBytes;
			byte[] buf = new byte[1024];
			while ((readedBytes = mFileIn.read(buf)) > 0) {
				mFileOut.write(buf, 0, readedBytes);
			}
			mFileOut.flush();
			mFileOut.close();

			Uri localUri = Uri.fromFile(mFile);
			Intent localIntent = new Intent(
					Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, localUri);
			sendBroadcast(localIntent);

		} catch (IOException e) {
			if (Utils.debug)
				e.printStackTrace();
			return false;
		}

		return true;

	}

	// 工具方法
	public static Bitmap fitSizeImg(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists())
			return null;
		// Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
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
			opts.inSampleSize = 4;
		} else if (file.length() < 1048576 * 2) { // 1024-1024k
			opts.inSampleSize = 6;
		} else {
			opts.inSampleSize = 7;
		}
		if (Utils.debug)
			Log.e(TAG, "4");
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(file.getPath(), opts));
		return wref.get();
	}

	public static Bitmap fitSizeBigImg(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists())
			return null;
		// Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 20480) { // 0-20k
			opts.inSampleSize = 1;
		} else if (file.length() < 51200) { // 20-50k
			opts.inSampleSize = 1;
		} else if (file.length() < 307200) { // 50-300k
			opts.inSampleSize = 1;
		} else if (file.length() < 819200) { // 300-800k
			opts.inSampleSize = 1;
		} else if (file.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 1;
		} else if (file.length() < 1048576 * 2) { // 1024-1024k
			opts.inSampleSize = 2;
		} else {
			opts.inSampleSize = 3;
		}
		if (Utils.debug)
			Log.e(TAG, "3");
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(file.getPath(), opts));
		return wref.get();
	}

	public static Drawable drawable(Bitmap bmp) {
		Bitmap bm = bmp; // xxx根据你的情况获取
		BitmapDrawable bd = new BitmapDrawable(bm);
		return bd;
	}

	private Handler handler = new WrappedHandler(thisContext) {
		public void handleMessage(Message msg) {
			// progressbar.setVisibility(View.GONE);
			progress.cancel();
			switch (msg.what) {
			case 1:
				if (!destroy) {
					timeCancel();
					imgview.setVisibility(View.VISIBLE);
					ll_loading_img.setVisibility(View.GONE);
					// bm=getBigImg(msgid);
					bm = getBigImage((String) msg.obj);
					if (bm != null) {
						if (bm.getWidth() < display.getWidth()) {
							bm = Utils.ResizeBitmap(bm, display.getWidth());
						}
						imgview.setImageBitmap(bm);
						imgview.setVisibility(View.VISIBLE);
						imgview.invalidate();
						if (progress.isShowing()) {
							if (progress != null) {
								progress.cancel();
								progress = null;
							}
							if (ispress) {
								duplicateImages(msgid);
								Toast.makeText(EditSendImg.this, save_prompt,
										Toast.LENGTH_LONG).show();
								ispress = false;
							}
						}
						
						//显示点赞心按钮
						if(isFromMsgRoom){
							TxGroup txgroup = TxGroup.getTxGroup(mSess.getContentResolver(), txmsg.group_id);
							if(txgroup!=null&&txgroup.isOfficialGroup()){
								//官方聊天室中
								if(txmsg.isCanBePraise()){
									iv_praise_img.setVisibility(View.VISIBLE);
								}
							}
						}
						
					} else {
					}
				}
				break;
			case 2:
				if (!destroy) {
					new AlertDialog.Builder(EditSendImg.this)
							.setTitle(R.string.prompt)
							.setMessage(R.string.img_download_fail)
							.setPositiveButton(
									EditSendImg.this.getResources().getString(
											R.string.confirm),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {

											dialoginterface.cancel();
											EditSendImg.this.finish();
										}
									}).show();
				}

				break;
			case 3:
				if (!destroy) {
					ll_loading_img.setVisibility(View.GONE);
					imgview.setVisibility(View.GONE);
					new AlertDialog.Builder(EditSendImg.this)
							.setTitle(R.string.prompt)
							.setMessage(R.string.net_not_available)
							.setPositiveButton(
									EditSendImg.this.getResources().getString(
											R.string.confirm),
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {

											dialoginterface.cancel();

										}
									}).show();
				}
				break;
			case 4:

				int count = msg.getData().getInt("progress");
				customProgress.setProgress(count);
				customProgress.setText("");
				break;
			case 5:
				timeCancel();
				tv_loadingText.setText(up_imagefail);
				btn_retry.setVisibility(View.VISIBLE);
				// customProgress.setProgress(0);
				customProgress.setText("");
				break;
			case 6:
				tv_loadingText.setText(up_imageing);
				btn_retry.setVisibility(View.GONE);
				customProgress.setProgress(0);
				customProgress.setText("");
				break;
			case FLUSH_PRAISE_BUTTON:
				if(isFromMsgRoom){
					setPraiseBackground(txmsg.praisedState);
				}
				break;

			}
		}
	};
	private void setPraiseBackground(int praisedState) {
		if(iv_praise_img!=null){
			
			if (praisedState == PraiseNotice.ACTION_PRAISE) {
				//被赞了
				iv_praise_img.setVisibility(View.VISIBLE);
				iv_praise_img.setImageResource(R.drawable.state_praised);
			}else if (praisedState == PraiseNotice.ACTION_CANCEL_PRAISE) {
				//被取消赞了
				iv_praise_img.setImageBitmap(null);
			}else if(praisedState == PraiseNotice.ACTION_NONE){
				//还没有被赞
				if (txmsg.isCanBePraise()) {
					//可以被赞
					iv_praise_img.setVisibility(View.VISIBLE);
					iv_praise_img.setImageResource(R.drawable.state_unpraised);
				}
				
			}
			
//			if(praisedState == PraiseNotice.ACTION_NONE){
//				iv_praise_img.setImageResource(R.drawable.state_unpraised);
//			}else if (praisedState == PraiseNotice.ACTION_PRAISE) {
//				iv_praise_img.setImageResource(R.drawable.state_praised);
//			}else if (praisedState == PraiseNotice.ACTION_CANCEL_PRAISE) {
//				iv_praise_img.setImageBitmap(null);
//			}
		}
	}

	DownUploadListner mImageCallback = new DownUploadListner() {
		@Override
		public void onStart(FileTaskInfo taskInfo) {
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			NumberFormat nf = NumberFormat.getPercentInstance();
			nf.setMinimumFractionDigits(0);
			String per = nf.format((double) curlen / (double) totallen);
			int progress = Integer.parseInt(per.substring(0, per.length() - 1));
			if (Utils.debug) {
				Log.i(TAG, "下载大图进度progress:" + progress);
			}
			Message msg1 = new Message();
			msg1.what = 4;
			msg1.getData().putInt("progress", progress);
			handler.sendMessage(msg1);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			ContentValues values = new ContentValues();
			if(isFromMsgRoom&&TextUtils.isEmpty(txmsg.fileDownTime)){
				//从聊天室过来的消息,且第一次下载时间为空
				txmsg.fileDownTime = System.currentTimeMillis()+"";
				values.put(TxDB.Messages.FILE_DOWN_TIME, txmsg.fileDownTime);
			}
			values.put(TxDB.Messages.MSG_PATH, taskInfo.mFullName);
			mSess.getContentResolver().update(
					TxDB.Messages.CONTENT_URI, values,
					TxDB.Messages.MSG_ID + "=?", new String[] { "" + msgid });
			Message mscr = new Message();
			mscr.obj = taskInfo.mFullName;
			mscr.what = 1;
			handler.sendMessage(mscr);

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			Message mscr = new Message();
			mscr.what = 5;
			handler.sendMessage(mscr);
		}
	};


	public void dowloadBigImg(String localPath, String url) {
		if (Utils.debug)
			Log.d(TAG, "下载的大图的地址为：" + localPath);
		if(isFromMsgRoom&&iv_praise_img!=null){
			iv_praise_img.setVisibility(View.INVISIBLE);
		}
		
		// 这个回调中不需要与消息相关操作，故最后一个参数obj传为null
		mSess.mDownUpMgr.downloadImg(url, localPath, 1, true, true,
				mImageCallback, null);
	}

	SessionManager mSess = SessionManager.getInstance();

	public Bitmap getBigImgByPath(String path) {
		if (path == null)
			return null;
		// if(path.startsWith(TXMessage.MOBILE_TYPE)){
		try {
			Bitmap img = null;
			if (comeToPageState != 0) {
				img = Utils.fitSizeAutoImg(path, Utils.wxb_default);
			} else {
				img = Utils.fitSizeAutoImg(path, Utils.msgroom_big_resolution);
			}
			if (img == null)
				return null;
			WeakReference<Bitmap> wref1 = new WeakReference<Bitmap>(img);
			img = null;
			return wref1.get();
		} catch (Exception e) {
			// if(Utils.debug)System.out.println("getImgByPath..."+e.getMessage());
		} catch (OutOfMemoryError e) {
			// if(Utils.debug)System.out.println("getImgByPath..."+e.getMessage());
		}
		return null;
	}

	// 根据图片路径加载大图
	private Bitmap getBigImage(String filePath) {
		try {
			File file = new File(filePath);
			if (Utils.debug)
				Log.i(TAG, "大图文件file为：" + file);
			if (!file.exists()) {
				return null;
			}
			int fitSizeType = 0;
			if (comeToPageState != 0) {
				fitSizeType = Utils.wxb_default;
			} else {
				fitSizeType = Utils.msgroom_big_resolution;
			}
			Bitmap bitmap = Utils.fitSizeAutoImg(filePath, fitSizeType);
			if (Utils.debug)
				Log.i(TAG, "根据指定分辨率压缩的相册bitmap：" + bitmap);
			WeakReference<Bitmap> wref = new WeakReference<Bitmap>(bitmap);
			bitmap = null;
			return wref.get();
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "加载bitmap大图片异常", e);
			}
		}
		return null;
	}

	public Bitmap getBigImg(long msgid) {
		String storagePath = Utils.getStoragePath(this);
		if (Utils.isNull(storagePath)) {
			// Utils.initStoragePath(BigPicActivity.this);
			return null;
		}
		String imgTypePath = null;
		if (comeToPageState != 0) {
			imgTypePath = Utils.AVATAR_PATH;
		} else {
			imgTypePath = Utils.IMAGE_PATH;
		}
		String tempPath = null;
		// StringBuffer tempPath=new
		// StringBuffer().append(storagePath).append("/"+imgTypePath+"/").append(msgid).append("_big.jpg");
		tempPath = tempPath(imgTypePath, true);
		try {
			File file = new File(tempPath.toString());
			if (Utils.debug)
				Log.i(TAG, file + "____________getImgByPath______________");
			if (!file.exists()) {
				return null;
			}
			int fitSizeType = 0;
			if (comeToPageState != 0) {
				fitSizeType = Utils.wxb_default;
			} else {
				fitSizeType = Utils.msgroom_big_resolution;
			}
			Bitmap bitmap = Utils.fitSizeAutoImg(tempPath.toString(),
					fitSizeType);
			// Bitmap
			// bitmap=getImgByPath(tempPath.toString(),display.getWidth()*display.getHeight());
			if (Utils.debug)
				Log.i(TAG, bitmap + "____________getImgByPath______________");
			WeakReference<Bitmap> wref = new WeakReference<Bitmap>(bitmap);
			bitmap = null;
			// Bitmap img=BitmapFactory.decodeFile(tempPath.toString());
			// if(img==null)return null;
			return wref.get();
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return null;
	}

	private Bitmap receiveSameImg(String imgdown) {
		Cursor c = this.getContentResolver().query(TxDB.Messages.CONTENT_URI,
				null, TxDB.Messages.MSG_URL + "=?", new String[] { imgdown },
				null);
		if (c != null) {

			while (c.moveToNext()) {
				TXMessage cm = TXMessage.fetchOneMsg(c);
				Bitmap bm = getBigImg(cm.msg_id);
				if (bm != null) {
					if (Utils.debug)
						Log.i(TAG, "是已经收到过的图片");
					c.close();
					return bm;
				}
			}
			c.close();
		}
		return null;
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_MENU) {

		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if(isFromMsgRoom){
				//从聊天室跳转过来的消息
				Intent data = new Intent();
				data.putExtra(TXMESSAGE, txmsg);
				setResult(RESULT_OK, data);
			}
			this.finish();
		}
		return super.onKeyDown(keyCode, event);
	}


	protected void onStop() {
		timeCancel();
		super.onStop();
	}

	private boolean destroy;

	protected void onDestroy() {
		if (bm != null && !bm.isRecycled()) {
			bm.recycle();
			bm = null;
		}
		destroy = true;
		
		if(praiseResultCallback!=null){
			mSess.getPraiseNoticeDao().unregistePraiseResultCallback(praiseResultCallback);
		}
		
		super.onDestroy();
	}

	public Bitmap getBitmap(Bitmap bitmap) {
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(bitmap);
		return wref.get();
	}


	public String tempPath(String saveImgType, boolean _big) {
		if (userid == TX.TUIXIN_MAN) {
			String rootPath = Utils
					.getStoragePath(this.getApplicationContext());
			File folder = new File(rootPath, mSess.mDownUpMgr.AVATAR_PATH);
			if (!folder.exists())
				folder.mkdirs();

			imageFilePath = folder.getAbsolutePath() + "/" + TX.TUIXIN_MAN
					+ "_big.jpg";
		} else if (saveImgType.equalsIgnoreCase(Utils.AVATAR_PATH)) {
			imageFilePath = mSess.mDownUpMgr
					.getAvatarFile(imgUrl, userid, true);
		} else {
			imageFilePath = mSess.mDownUpMgr.getImageFile(imgUrl, FileTransfer.SRC_TYPE_DEFAULT, msgid,
					true);
		}
		if (TextUtils.isEmpty(imageFilePath)) {
			return "";
		}
		return imageFilePath;

	}

	private String getAvatarFilePath(long partnerId, String url) {
		String tmp = ".jpg";
		if (url != null) {
			try {
				String[] values = url.split(":");
				if (values != null && values.length > 2) {
					tmp = values[2].substring(values[2].lastIndexOf("."));
				}
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "截取url文件名异常", e);
				}
			}
		}
		return avatarDir + "/" + partnerId + tmp;
	}

	public void timeOut() {
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "创建定时器时，取消下载大图超时定时器异常", e);
				}
			}
			outtime = null;
		}
		outtime = new Timer();
		outtime.schedule(new TimerTask() {
			public void run() {

				Message msg = new Message();
				msg.what = 5;
				handler.sendMessage(msg);
			}
		}, 30000);
	}

	public void timeCancel() {
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "取消下载大图超时定时器异常", e);
				}
			}
			outtime = null;
		}
	}

	public Bitmap getImgByPath(String path, int resolution) {
		if (path == null)
			return null;
		// if(path.startsWith(TXMessage.MOBILE_TYPE)){
		try {

			if (!Utils.checkSDCard()) {// 无SD卡
//				Bitmap nosd_img = BitmapFactory.decodeResource(getResources(),
//						R.drawable.no_sd_img);
//				return nosd_img;
				if(Utils.debug)Log.i(TAG, "没有SD卡，editsendimg");
				return null;
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			if (Utils.debug)
				Log.e(TAG, "2");
			BitmapFactory.decodeFile(path, opts);

			opts.inSampleSize = Utils.computeSampleSize(opts, -1, resolution);

			opts.inJustDecodeBounds = false;

			WeakReference<Bitmap> wref = null;
			try {
				// Bitmap bmp = BitmapFactory.decodeFile(path, opts);
				if (Utils.debug)
					Log.e(TAG, "1");
				wref = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path,
						opts));
			} catch (OutOfMemoryError err) {
				return null;
			}
			Bitmap bm = Utils.headImg_Resize(wref.get(), display.getWidth(),
					display.getHeight());
			WeakReference<Bitmap> wref1 = new WeakReference<Bitmap>(bm);
			bm = null;

			return wref1.get();
		} catch (Exception e) {
		} catch (OutOfMemoryError e) {
		}
		return null;
	}
}
