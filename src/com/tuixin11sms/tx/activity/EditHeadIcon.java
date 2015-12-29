package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupEdit;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.EditHeadIconView;


/**
 * 修改头像和上传相册的界面都有此activity完成
 *
 */
public class EditHeadIcon extends BaseActivity {
	private static final String TAG = EditHeadIcon.class.getSimpleName();
	public static int REGET_IMG_FROM_CAMERA = 101;
	public static int REGET_IMG_FROM_GALLERY = 102;
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	private String img_path;//要编辑的图片的路径地址
	private String reselectTempImgPath;//重新选择头像时临时图片的路径地址
	private String turnState = "";//跳转状态,判断是从照相机还是系统图库传递跳转过来的，因为点击“重新获取照片”时要执行原路经获取
	public static String STATE_COME = "state_come"; //跳转过来状态
	public static String FROM_CAMERA = "from_camera"; //照相跳转过来
	public static String FROM_GALLERY = "from_gallery";//相册跳转过来
	public static String GET_IMG_PATH = "get_img"; //得到传过来的头像
	public static final int EDIT_IMG = 2123;//编辑好的图片
	public static String GIVE_IMG = "give_img"; //传过去的图片
	public static String SMALL_IMG = "small_img";
	public static String BIG_IMG = "give_big_img";
	public static final String IS_REGISTE = "isRegist";//是否从注册页面跳转过来
//	private SharedPreferences prefs = null;
//	private Editor editor;
	private Timer outtime;
	private Intent dataIntent;
	private Display display;
	private ImageView translate_button;
	private TextView confirm_button;
	private TextView ib_reselectImg;
	private EditHeadIconView editHeadIconView;
	private ImageView iv_editAlbum;//编辑相册图片的view,没有缩放框
	private Bitmap clip_img;
	private boolean isRegist;
	private boolean isAlbum = false;
	private Bitmap currentBitmap = null;//持有当前显示的图片的引用

	public static String FROM_GROUP = "from_group";
	public static String GROUP_ID = "group_id";
	public static String GROUP_ICON_PATH = "group_icon_path";

	private boolean destroy;
	// handle更新界面
	private static final int TEL_CHECK_TIMEOUT = 10;
	private static final int UPLOAD_FAIL = 11;
	private Handler Handler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case TEL_CHECK_TIMEOUT:
				if (!destroy) {
					startPromptDialog(R.string.prompt, R.string.change_name_outhead);
				}
				break;
			case UPLOAD_FAIL:
				Toast.makeText(EditHeadIcon.this, R.string.up_img_fail, Toast.LENGTH_SHORT).show();
				break;
			}
		}
	};

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		/**** 屏幕初始化 *****/
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		display = getWindowManager().getDefaultDisplay();
		dataIntent = this.getIntent();
		setContentView(R.layout.activity_edit_head_icon);
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
		editHeadIconView = (EditHeadIconView) findViewById(R.id.edit_head_img);
		iv_editAlbum = (ImageView) findViewById(R.id.iv_editAlbumImg);
		confirm_button = (TextView) findViewById(R.id.edit_head_confirm);

		translate_button = (ImageView) findViewById(R.id.edit_head_translate);
		ib_reselectImg = (TextView) findViewById(R.id.edit_head_reselect);
		translate_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//旋转照片角度
				roateImgLogic();
			}

		});
		turnState = dataIntent.getStringExtra(STATE_COME);//判断是从照相机还是系统图库传递跳转过来的，因为点击“重新获取照片”时要执行原路经获取
		isRegist = dataIntent.getBooleanExtra(IS_REGISTE, false);
		isAlbum = dataIntent.getBooleanExtra("isAlbum", false);
		if (Utils.isNull(img_path)) {
			img_path = dataIntent.getStringExtra(GET_IMG_PATH);//要显示的图片路径
		}
		//如果是从相册过来的，那么去掉编辑框
		if(isAlbum){
			editHeadIconView.setVisibility(View.GONE);
			iv_editAlbum.setVisibility(View.VISIBLE);
		}
		if (!Utils.isNull(turnState)) {
			if (turnState.equals(FROM_CAMERA)) {
				//从照相机跳转过来
//				ib_reselectImg.setImageResource(R.drawable.headimg_edit_photo_selector);
				ib_reselectImg.setText("重新拍照");
			} else if (turnState.equals(FROM_GALLERY)) {
				//从系统图库跳转过来
//				ib_reselectImg.setImageResource(R.drawable.headimg_edit_select_selector);
				ib_reselectImg.setText("重选照片");
			}
			ib_reselectImg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					//重新获取头像照片
					if (turnState.equals(FROM_CAMERA)) {
						if (!Utils.checkSDCard()) {
							if (Utils.debug)Log.i(TAG, "重新拍照时无SD卡");
							return;
						}
						String storagePath = Utils.getStoragePath(EditHeadIcon.this);
						if (Utils.isNull(storagePath)) {
							if (Utils.debug)Log.i(TAG, "神聊根目录为空");
							return;
						}
						File sddir = new File(storagePath);
						if (!sddir.exists() && !sddir.mkdirs()) {
							if (Utils.debug)
								Log.e(TAG, "创建神聊根目录失败");
						}
						//如果是注册的话，这里取user_id为空字符串  2013.12.27 
//						String tempUserId = prefs.getString(CommonData.USER_ID, "");
						String tempUserId = mSess.mPrefMeme.user_id.getVal();
//						long user_id = 0;
						if (TextUtils.isEmpty(tempUserId)) {
							tempUserId = ""+System.currentTimeMillis();
						}
//						else {
//							user_id = Long.parseLong(tempUserId);
//						}
						reselectTempImgPath = storagePath+"/"+tempUserId+".jpg";
						Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
						it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(reselectTempImgPath)));
						it.putExtra(Utils.IMAGE_CAMRA, reselectTempImgPath);
						startActivityForResult(it, REGET_IMG_FROM_CAMERA);
						
					} else if (turnState.equals(FROM_GALLERY)) {
						Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);
						getImage.addCategory(Intent.CATEGORY_OPENABLE);
						getImage.setType(MIME_TYPE_IMAGE_JPEG);
						startActivityForResult(getImage, REGET_IMG_FROM_GALLERY);

					}
				}

			});
		}
		confirm_button.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//确定照片按钮
				if(isAlbum){
					clip_img = currentBitmap;
				}else{
					clip_img = editHeadIconView.getEditImg();
				}
				if (clip_img != null) {
					Bitmap DstBitmap = null;
					if(isAlbum){
						DstBitmap = clip_img;
						currentBitmap = null;//释放bitmap的一个引用，这样操作有问题吗？
					}else {
						DstBitmap = Utils.ImageCrop(clip_img);
					}
					
					
					if (isAlbum) {
						Intent i = new Intent();
						EditHeadIcon.this.setResult(RESULT_OK, i);
						Utils.tempBm = DstBitmap;
						EditHeadIcon.this.finish();
					}else {
						Bitmap smallDstBitmap = Utils.ResizeBitmap(DstBitmap, 92);
						Bitmap bigdstBitmap = Utils.ResizeBitmap(DstBitmap, 400);
						if (isRegist) {
							SessionManager.getInstance().setSmallAvatar(smallDstBitmap);
							SessionManager.getInstance().setBigAvatar(bigdstBitmap);
							Intent i = new Intent();
							i.putExtra(GIVE_IMG, smallDstBitmap);
							EditHeadIcon.this.setResult(RESULT_OK, i);
							EditHeadIcon.this.finish();
						}else {
							showLoading();
							createFileImage(smallDstBitmap, bigdstBitmap);
							if (outtime != null) {
								try {
									outtime.cancel();
								} catch (Exception e) {

								}
								outtime = null;
							}
							outtime = new Timer();
							outtime.schedule(new TimerTask() {
								public void run() {
									dialog.dismiss();
									Message msg = new Message();
									msg.what = TEL_CHECK_TIMEOUT;
									Handler.sendMessage(msg);
								}
							}, 90 * 1000);
						}
					}
				}
			}
		});

	}

	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		if (hasFocus && editHeadIconView != null) {// 界面全部加在完毕

//			if (Utils.isNull(img_path)) {
//				img_path = dataIntent.getStringExtra(GET_IMG_PATH);
//			}
			Bitmap headImg = null;
			if(isAlbum){
				headImg = getImgByPath(img_path, iv_editAlbum.getWidth() * iv_editAlbum.getHeight());
				iv_editAlbum.setImageBitmap(headImg);
				currentBitmap = headImg;
			}else {
				headImg = getImgByPath(img_path, editHeadIconView.getWidth() * editHeadIconView.getHeight());
				editHeadIconView.setImg(headImg);
				editHeadIconView.setInitPos(display.getWidth(), display.getHeight());
			}

		}
		super.onWindowFocusChanged(hasFocus);
	}
	
//	@Override
//	protected void onResume() {
//		super.onResume();
//		if (editHeadIconView != null) {// 界面全部加在完毕
//			
//			Bitmap headImg = null;
//			if(isAlbum){
//				headImg = getImgByPath(img_path, iv_editAlbum.getWidth() * iv_editAlbum.getHeight());
//				iv_editAlbum.setImageBitmap(headImg);
//				currentBitmap = headImg;
//			}else {
//				headImg = getImgByPath(img_path, editHeadIconView.getWidth() * editHeadIconView.getHeight());
//				editHeadIconView.setImg(headImg);
//				editHeadIconView.setInitPos(display.getWidth(), display.getHeight());
//			}
//
//		}
//	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REGET_IMG_FROM_CAMERA) {
//				TuixinService1.notNeedCheckActivityState = false;
				if (Utils.debug)
					Log.v(TAG, "resultCode=" + resultCode);
				Bitmap tempimg = null;
				Uri uri = null;
				if (isRegist) {
					//注册页面跳转过来的，重新修改注册头像的逻辑
					if (!TextUtils.isEmpty(img_path)) {
						//把旧图片文件删除
						File oldImgFile = new File(img_path);
						if (oldImgFile!=null&&oldImgFile.exists()) {
							oldImgFile.delete();
						}
					}
					
					img_path = reselectTempImgPath;
					
				}else {
					
					img_path = null;
					String storagePath = Utils.getStoragePath(EditHeadIcon.this);
					String tempUserId = mSess.mPrefMeme.user_id.getVal();
					String path = storagePath+"/"+tempUserId+".jpg";
					
					if (Utils.isNull(path)) {
						img_path = data.getDataString();
						if (!Utils.isNull(img_path)) {
							uri = Uri.parse(img_path);
							img_path = getRealPathFromURI(uri);
							tempimg = getTempImg(isAlbum);
						}
					} else {
						img_path = path;
					}
					
					if (tempimg != null) {
						if(isAlbum){
							iv_editAlbum.setImageBitmap(tempimg);
							currentBitmap = tempimg;
						}else{
							editHeadIconView.setImg(tempimg);
							editHeadIconView.setInitPos(display.getWidth(), display.getHeight());
						}
						rotate_angle = 0;
					}
				}
				
				
			} else if (requestCode == REGET_IMG_FROM_GALLERY) {
				if (Utils.debug)
					Log.v(TAG, "tempimg11111=");
//				TuixinService1.notNeedCheckActivityState = false;
				Bitmap tempimg = null;
				if (Utils.debug)
					Log.v(TAG, "tempimg2222=" + tempimg);
				Uri originalUri = data.getData();
				img_path = null;
				try {
					img_path = getRealPathFromURI(originalUri);
					tempimg = getTempImg(isAlbum);
//						if(isAlbum){
//							tempimg = getImgByPath(img_path, iv_editAlbum.getWidth() * iv_editAlbum.getHeight());
//						}else {
//							tempimg = getImgByPath(img_path, editHeadIconView.getWidth() * editHeadIconView.getHeight());
//						}
				} catch (Exception e) {
					img_path = originalUri.toString();
					img_path = img_path.substring(7, img_path.length());
				}
				if (Utils.debug)
					Log.v(TAG, "tempimg3333=" + tempimg);
				if (tempimg != null) {
					if(isAlbum){
						iv_editAlbum.setImageBitmap(tempimg);
						currentBitmap = tempimg;
					}else{
						editHeadIconView.setImg(tempimg);
						editHeadIconView.setInitPos(display.getWidth(), display.getHeight());
					}
					rotate_angle = 0;
				}
			}
		}

	}

	/**
	 * 根据是否是相册，获取相应图片bitmap
	 * 相册大图如果小于1M则不压缩，若大于1M，则压缩至一百万像素上传
	 * @param isAlbum
	 */
	private Bitmap getTempImg(boolean isAlbum) {
		Bitmap  resultBm = null;
		if(isAlbum){
//			tempimg = getImgByPath(img_path, iv_editAlbum.getWidth() * iv_editAlbum.getHeight());
			FileInputStream fis = null;
			try {
				fis = new FileInputStream(img_path);
				int fileSize = fis.available();
				if(fileSize<1024*1024){
					//不做压缩等处理，直接上传
					resultBm = BitmapFactory.decodeFile(img_path);
				}else {
					//压缩至总像素数小于一百万
//					resultBm = getImgByPath(img_path, 1000000);
					resultBm = compressBitmapByMaxPix(img_path, 1000000);
				}
			} catch (IOException e) {
				if(Utils.debug){
					Log.e(TAG, "压缩相册图片，出现文件未找到异常",e);
				}
			}finally{
				try {
					fis.close();
				} catch (IOException e2) {
					if(Utils.debug){
						Log.e(TAG, "文件输入流关闭异常",e2);
					}
				}
			}
		}else{
			resultBm = getImgByPath(img_path, editHeadIconView.getWidth() * editHeadIconView.getHeight());
		}
		return resultBm;

	}


	public void onDestroy() {
		removeAllImg();
		destroy = true;
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {

			}
			outtime = null;
		}
		TxData.popActivityRemove(this);

		super.onDestroy();

	}
	
	
	
	/**
	 * 按比例压缩bitmap到指定像素
	 * @param srcBitmap 需要压缩的bitmap
	 * @param maxPixNum 压缩后最大的像素数
	 */
	private Bitmap compressBitmapByMaxPix(String filePath, int maxPixNum) {
		if(!new File(filePath).exists()){
			return null;
		}
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, options);

		// Calculate inSampleSize
		options.inSampleSize = calculateInSampleSize(options,maxPixNum);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;

		return BitmapFactory.decodeFile(filePath, options);

	}
	
	
	/**
	 * 计算图片的缩放值
	 * @param options
	 * @param maxPixNum
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,int maxPixNum) {
	    final int height = options.outHeight;
	    final int width = options.outWidth;
	    int inSampleSize = 1;
	    if(height*width>maxPixNum){
	    	//图片像素数大于指定的最大像素数，进行压缩
	    	inSampleSize = maxPixNum/(height*width);
	    }
        return inSampleSize;
	}
	
	

	public Bitmap getImgByPath(String path, int resolution) {
		if (path == null)
			return null;
		// if(path.startsWith(TXMessage.MOBILE_TYPE)){
		try {

			if (!Utils.checkSDCard()) {// 无SD卡
//				Bitmap nosd_img = BitmapFactory.decodeResource(getResources(), R.drawable.no_sd_img);
//				return nosd_img;
				if(Utils.debug)Log.i(TAG, "没有sd卡，editheadicon");
				return null;
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, opts);

			opts.inSampleSize = Utils.computeSampleSize(opts, -1, resolution);

			opts.inJustDecodeBounds = false;

			WeakReference<Bitmap> wref = null;
			try {
				// Bitmap bmp = BitmapFactory.decodeFile(path, opts);
				wref = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
			} catch (OutOfMemoryError err) {
				if(Utils.debug){
					Log.e(TAG, "从图片地址加载图片OOM了",err);
				}
				return null;
			}
			Bitmap bm = null;
			if(isAlbum){
				bm = Utils.headImg_Resize(wref.get(), iv_editAlbum.getWidth(), iv_editAlbum.getHeight());
			}else{
				bm = Utils.headImg_Resize(wref.get(), editHeadIconView.getWidth(), editHeadIconView.getHeight());
			}
			WeakReference<Bitmap> wref1 = new WeakReference<Bitmap>(bm);
			bm = null;

			return wref1.get();
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "通过路径获取bitmap异常：",e);
			}
		} catch (OutOfMemoryError e) {
			if (Utils.debug) {
				Log.e(TAG, "通过路径获取bitmap出现OOM错误：",e);
			}
		}
		return null;
	}

	public String getRealPathFromURI(Uri contentUri) {

		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
														// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)
		int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		//Android4.0系统自动关闭Cursor
		if (Integer.parseInt(Build.VERSION.SDK) < 14)
			cursor.close();
		return path;
	}

	
	SessionManager mSess = SessionManager
			.getInstance();
	
	
	
	private void createFileImage(final Bitmap Sbp, final Bitmap Bbp) {
		
		String tempImgPath = Utils.getStoragePath(mSess.getContext()) + File.separator
				+ Utils.IMAGE_PATH + File.separator + System.currentTimeMillis() + ".jpg";
		try {
			// 生成合成的文件
			mSess.mDownUpMgr.getCompoundImgFile(tempImgPath, Sbp,
					Bbp);
		} catch (IOException e) {
			if (Utils.debug) {
				Log.w(TAG, "合成大小图文件异常", e);
			}
		}
		mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,	new DownUploadListner() {
			
			@Override
			public void onStart(FileTaskInfo taskInfo) {
				
			}
			
			@Override
			public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
				
			}
			
			@Override
			public void onFinish(FileTaskInfo taskInfo) {
				String fileUrl = taskInfo.mServerHost+":"+taskInfo.mPath+":"+taskInfo.mTime;
				
				if (outtime != null) {
					try {
						outtime.cancel();
					} catch (Exception e) {

					}
					outtime = null;
				}
//				if (resultCode == SocketClient.RESULT_OK) {
					//					Toast.makeText(context, resId, duration)
					String storagePath = Utils.getStoragePath(EditHeadIcon.this);
					if (Utils.isNull(storagePath)) {
						if (dialog != null) {
							dialog.cancel();
						}
						Utils.startPromptDialog(EditHeadIcon.this, R.string.prompt, R.string.chatroom_upload_no_sdcard);
						return;
					}
					File sddir = new File(storagePath, Utils.AVATAR_PATH);
					if (!sddir.exists() && !sddir.mkdirs()) {
						if (Utils.debug)
							Log.e("bitmapFromUrl", "Create dir failed");
						sddir.mkdir();
					}
					if ("change_group_icon".equals(dataIntent.getStringExtra(FROM_GROUP))) {
//						StringBuffer tempPath = new StringBuffer().append(sddir.getAbsolutePath()).append("/")
//								.append(dataIntent.getStringExtra(GROUP_ID)).append(".jpg");
//						StringBuffer tempPath1 = new StringBuffer().append(sddir.getAbsolutePath()).append("/")
//								.append(dataIntent.getStringExtra(GROUP_ID)).append("_big.jpg");
						String tempBasePath = new StringBuffer().append(sddir.getAbsolutePath()).append("/")
								.append(dataIntent.getStringExtra(GROUP_ID)).toString();
						String tempSmallPath = tempBasePath.concat(GroupEdit.TEMP).concat(".jpg");
						String tempBigPath = tempBasePath.concat(GroupEdit.TEMP).concat("_big.jpg");
						if (Utils.debug) {
							Log.i(TAG, "创建的群小图路径为："+tempSmallPath);
							Log.i(TAG, "创建的群大图路径为："+tempBigPath);
						}
						createFile(tempSmallPath, Sbp);
						createFile(tempBigPath, Sbp);
						removeAllImg();
						Intent i = new Intent();
						i.putExtra(GIVE_IMG, Sbp);
						i.putExtra(GROUP_ICON_PATH, fileUrl);
						EditHeadIcon.this.setResult(RESULT_OK, i);
						EditHeadIcon.this.finish();
					} else {

						long user_id;
						try {
//							user_id = Long.parseLong(prefs.getString(CommonData.USER_ID, "-1"));
							user_id = Long.parseLong(mSess.mPrefMeme.user_id.getVal());
						} catch (NumberFormatException e) {
							user_id = -1;
						}
						StringBuffer tempPath = new StringBuffer().append(sddir.getAbsolutePath()).append("/")
								.append(TX.tm.getTxMe().partner_id).append(".jpg");
						StringBuffer tempPath1 = new StringBuffer().append(sddir.getAbsolutePath()).append("/")
								.append(TX.tm.getTxMe().partner_id).append("_big.jpg");
						createFile(tempPath.toString(), Sbp);
						createFile(tempPath1.toString(), Bbp);
//						editor = prefs.edit();
//						editor.putBoolean(CommonData.IS_SETHEAD, true);
//						editor.putString(CommonData.AVATAR_URL, fileUrl);
						mSess.mPrefMeme.is_sethead.setVal(true);
						mSess.mPrefMeme.avatar_url.setVal(fileUrl);
						
//				        editor.putString(CommonData.AVATAR_URL_USER+TX.tm.getTxMe().partner_id,  fileUrl);
				        try {
							mSess.mUserLoginInfor.updateUserAvatarSex(TX.tm.getUserID(), fileUrl);
						} catch (JSONException e) {
							if(Utils.debug)Log.e(TAG,"更新头像json异常",e);
						}
						if (user_id != -1){
//							editor.putString(CommonData.AVATAR_PATH, tempPath.toString());
							mSess.mPrefMeme.avatar_path.setVal(tempPath.toString());
						}
						mSess.mPrefMeme.commit();
						//storagePath
//						editor.commit();
						TX.tm.reloadTXMe();/////////
						removeAllImg();
//						SocketHelper.getSocketHelper(txdata).sendUpAvatar(prefs.getString(CommonData.AVATAR_URL, ""));
						mSess.getSocketHelper().sendUpAvatar(mSess.mPrefMeme.avatar_url.getVal());
						Intent i = new Intent();
						i.putExtra(GIVE_IMG, Sbp);
						EditHeadIcon.this.setResult(RESULT_OK, i);
						EditHeadIcon.this.finish();
					}

//				} else {
	//
//					SendHandleMsg(UPLOAD_FAIL);
	//
//				}

				dialog.dismiss();
				
			}
			
			@Override
			public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
				if (outtime != null) {
					try {
						outtime.cancel();
					} catch (Exception e) {

					}
					outtime = null;
				}
				
				SendHandleMsg(UPLOAD_FAIL);

				dialog.dismiss();
				
			}
		}, null);

	}
	
	
	
	
	
	public void createFile(String path, Bitmap bitmap) {
		File file = new File(path);
		if (file.exists())
			file.delete();
		if (!file.exists()) {
			// 创建文件输出流
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				// 存储
				bitmap.compress(CompressFormat.JPEG, 100, os);
				// 关闭流
			} catch (FileNotFoundException e) {
				if(Utils.debug)Log.e(TAG, "创建图片文件，文件没有找到异常");
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (IOException e) {
					if(Utils.debug)Log.e(TAG, "创建图片文件，流关闭异常");
				}
			}
		}
	}

	ProgressDialog dialog;

	public void showLoading() {
		if (dialog != null) {
			dialog.cancel();
			dialog = null;
		}
		dialog = new ProgressDialog(this);
		dialog.setMessage("上传头像中...");
		dialog.setIndeterminate(true);
		dialog.setCancelable(true);
		dialog.show();
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(this);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setNegativeButton(R.string.confirm, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});
		promptDialog.show();
	}

	public void removeAllImg() {
		if (clip_img != null && !clip_img.isRecycled()) {
			clip_img.recycle();
			clip_img = null;
		}
		if (editHeadIconView != null) {
			editHeadIconView.removeBigImg();
		}
		if(isAlbum){
			iv_editAlbum.setImageBitmap(null);
			currentBitmap = null;
		}
	}

	private float rotate_angle = 0;

	public void roateImgLogic() {
		rotate_angle += 90;
		if (rotate_angle >= 360) {
			rotate_angle = 0;
		}
		if (isAlbum) {
			Bitmap headImg = getImgByPath(img_path, iv_editAlbum.getWidth() * iv_editAlbum.getHeight());
			Matrix matrix = new Matrix();
			matrix.setRotate(rotate_angle);
			headImg = Bitmap.createBitmap(headImg, 0, 0, headImg.getWidth(), headImg.getHeight(), matrix, true);
			headImg = Utils.headImg_Resize(headImg, iv_editAlbum.getWidth(), iv_editAlbum.getHeight());
			iv_editAlbum.setImageBitmap(headImg);
			currentBitmap = headImg;
		}else {
			Bitmap headImg = getImgByPath(img_path, editHeadIconView.getWidth() * editHeadIconView.getHeight());
			Matrix matrix = new Matrix();
			matrix.setRotate(rotate_angle);
			headImg = Bitmap.createBitmap(headImg, 0, 0, headImg.getWidth(), headImg.getHeight(), matrix, true);
			headImg = Utils.headImg_Resize(headImg, editHeadIconView.getWidth(), editHeadIconView.getHeight());
			editHeadIconView.setImg(headImg);
			editHeadIconView.setInitPos(display.getWidth(), display.getHeight());
		}
	}

	// 发送handle消息
	public void SendHandleMsg(int msg) {

		Message message = new Message();// 生成消息，并赋予ID值

		message.what = msg;

		Handler.sendMessage(message);// 投递消息

	}
}
