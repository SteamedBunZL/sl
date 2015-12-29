package com.tuixin11sms.tx.activity;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.activity.SetUpdateAreaActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.model.ChatChannel;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class UserInfoSupplementActivity extends BaseActivity implements
		OnClickListener {
	protected static final String TAG = "UserInfoSupplementActivity";
	// 下面5个变量无引用，暂时注掉 2013.09.22 shc
	// private static final int NAME_CHANGE_SUCCESS = 11000;
	// private static final int NAME_CHANGE_FAILED = 110001;
	// private static final int NAME_CHANGE_OPTFAILED = 110002;
	// private static final int NAME_CHANGE_NOTCHANGE = 110003;
	// private static final int NAME_CHANGE_NORRULE = 110004;
	private RelativeLayout userDistrict;
	private View userHeadLayout;
	private Button mConfirm;
	private TextView mDistrictContent;
	// private SharedPreferences prefs;
	// private Editor editor;
	public ImageView headPhoto;

	// private View upHeadRlayout;

	public int goInPageState = 0;
	public int skipActivityState = 3;
	ChatChannel channelData;
	private boolean isClick;

	private static final int FLUSH_ADAPTER = 30;

	public static final int SETINGTOUSERINFO = 0;// 设置页面进入的
	public static final int SEARCHFRTOUSERINFO = 1; // 通讯录进入的
	public static final int OTHERTOUSERINFO = 2;// 其他页面进入的
	public static final int REQUESTCODE_FOR_REQUSET_AREA = 9;// 语言请求码
	// 补充资料
	public static final int DEFULTTOUSERINFO = 3;// 默认
	public static final int MSGTOUSERINFO = 4; // 消息
	public static final int CHANNELTOUSERINFO = 5; // 频道

	public static final int ACTIVITY_ACTION_PHOTO_CAPTURE = 4;
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	public static final int ACTIVITY_ACTION_PIC_VIEW = 5;
	public static final int ACTION_PICK = 6;
	public static final int ACTION_EDIT_PICK = 7;

	public final static String pblicInfo = "pblicinfo";
	public final static String channelDataTo = "channeldata";
	public final static String defultToInfo = "defultToInfo";

	Intent intent;
	String niName;
	String mDistrict;
	private TX me;

	// private AlertDialog longEditTextDialog;
	// private int densityDpi;
	// private SmileyParser smileyParser_edittext_hdpi;
	// private SmileyParser smileyParser;
	// private int defaultHeadImg;
	// private int defaultHeadImgMan;
	// private int defaultHeadImgFemal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);

		intent = this.getIntent();
		// defaultHeadImgMan=R.drawable.sl_regist_default_head;
		// defaultHeadImgFemal=R.drawable.sl_regist_head_femal;

		goInPageState = intent.getIntExtra(pblicInfo, 0);
		channelData = intent.getParcelableExtra(channelDataTo);
		skipActivityState = intent.getIntExtra(defultToInfo, DEFULTTOUSERINFO);
		// smileyParser_edittext_hdpi = new SmileyParser(this);
		// smileyParser = new SmileyParser(this);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		// densityDpi = metric.densityDpi;

		setContentView(R.layout.activity_userinfo_settings_supplement);
		mDistrictContent = (TextView) findViewById(R.id.mDistrictContent);
		userHeadLayout = findViewById(R.id.updata_head_Rlayout);

		mConfirm = (Button) findViewById(R.id.mConfirm);
		userDistrict = (RelativeLayout) findViewById(R.id.user_sel_district);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		headPhoto = (ImageView) findViewById(R.id.setmHeadPic);
		userDistrict.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);

		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		// 进入过就标记一下 为了评分

		// int sex=getPrefsMeme().getInt(CommonData.SEX, -1);
		int sex = mSess.mPrefMeme.sex.getVal();
		if (sex == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {

			defaultHeaderImg = defaultHeaderImgFemale;
		}
		// prefs.edit().putBoolean(CommonData.FIRST_COMMONDATA, true).commit();
		mSess.mPrefMeme.first_commondata.setVal(true).commit();
		me = TX.tm.getTxMe();
		// String avatar_path = prefs.getString(CommonData.AVATAR_URL, "");
		String avatar_path = mSess.mPrefMeme.avatar_url.getVal();
		// 地区
		if (me.area != null && !"".equals(me)) {
			List<String> mlist = StringUtils.str2List(me.area);
			mDistrictContent.setText(DataContainer.getAreaNameByIds(mlist
					.toArray(new String[0])));
		}
		headPhoto.setBackgroundResource(defaultHeaderImg);
		if (!Utils.isNull(avatar_path)) {

			headPhoto.setTag(TX.tm.getUserID());
			headPhoto.setImageResource(defaultHeaderImg);
			mSess.avatarDownload.downAvatar(avatar_path,
					TX.tm.getUserID(), headPhoto, avatarHandler);

//			if (downAvatar == null) {
//				headPhoto.setImageResource(defaultHeaderImg);
//			} else {
//				headPhoto.setImageBitmap(downAvatar);
//			}
		}
		userHeadLayout.setOnClickListener(this);
		mConfirm.setOnClickListener(new ConfirmClick());

	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result2 = (Object[]) msg.obj;
				if (result2 != null){
					headPhoto.setImageBitmap((Bitmap)result2[0]);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		MobclickAgent.onResume(this);
		// int sex=getPrefsMeme().getInt(CommonData.SEX, -1);
		int sex = mSess.mPrefMeme.sex.getVal();
		if (sex == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {

			defaultHeaderImg = defaultHeaderImgFemale;
		}
		headPhoto.setImageResource(defaultHeaderImg);
		// String avatar_path = prefs.getString(CommonData.AVATAR_URL, "");
		String avatar_path = mSess.mPrefMeme.avatar_url.getVal();

		if (!Utils.isNull(avatar_path)) {
			headPhoto.setTag(TX.tm.getUserID());
			headPhoto.setImageResource(defaultHeaderImg);
			 mSess.avatarDownload.downAvatar(avatar_path,
					TX.tm.getUserID(), headPhoto, avatarHandler);

//			if (downAvatar == null) {
//				headPhoto.setImageResource(defaultHeaderImg);
//			} else {
//				headPhoto.setImageBitmap(downAvatar);
//			}

		}
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private class ConfirmClick implements OnClickListener {
		@Override
		public void onClick(View v) {
			// if(Utils.isNull(prefs.getString(CommonData.AVATAR_URL,
			// ""))||Utils.isNull(prefs.getString(CommonData.AREA, ""))){
			if (Utils.isNull(mSess.mPrefMeme.avatar_url.getVal())
					|| Utils.isNull(mSess.mPrefMeme.area.getVal())) {
				Toast.makeText(UserInfoSupplementActivity.this, "请补充个人信息",
						Toast.LENGTH_SHORT).show();
			} else {

				Intent intent = new Intent(UserInfoSupplementActivity.this,
						NearlyFriendActivity.class);
				startActivity(intent);
				UserInfoSupplementActivity.this.finish();
			}

		}
	}

	public void onDestroy() {
		// stopAsyncload();
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	protected void onStop() {
		super.onStop();

	}

	AlertDialog telDialog = null;
	protected boolean isSelected;
	private LinearLayout btn_back_left;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		case R.id.user_sel_district:

			Intent intentArea = new Intent(this, SetUpdateAreaActivity.class);
			startActivityForResult(intentArea, REQUESTCODE_FOR_REQUSET_AREA);

			break;
		case R.id.btn_back_left:
			UserInfoSupplementActivity.this.finish();
			break;
		case R.id.updata_head_Rlayout:
			final AlertDialog.Builder headDialog = new AlertDialog.Builder(
					UserInfoSupplementActivity.this);
			headDialog.setTitle("更多选项");
			headDialog.setItems(R.array.msgroom_pic,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								String storagePath = Utils
										.getStoragePath(UserInfoSupplementActivity.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								Intent getImage = new Intent(
										Intent.ACTION_GET_CONTENT);
								getImage.addCategory(Intent.CATEGORY_OPENABLE);
								getImage.setType(MIME_TYPE_IMAGE_JPEG);
								startActivityForResult(getImage,
										ACTIVITY_ACTION_PIC_VIEW);
								dialog.dismiss();
								// Utils.inPhoto = true;
							} else if (which == 1) {

								if (!Utils.checkSDCard()) {
									if (Utils.debug)
										Log.i(TAG, "无SD卡");

									// Message mscr = new Message();
									// mscr.what = SHOW_IMG_NOSDCAED;
									// handler.sendMessage(mscr);
									return;
								}
								String storagePath = Utils
										.getStoragePath(UserInfoSupplementActivity.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								File sddir = new File(storagePath);
								if (!sddir.exists() && !sddir.mkdirs()) {
									if (Utils.debug)
										Log.e(TAG,
												"bitmapFromUrl------Create dir failed");
								}
								// long user_id =
								// Long.parseLong(prefs.getString(CommonData.USER_ID,
								// "-1"));
								long user_id = Long
										.parseLong(mSess.mPrefMeme.user_id
												.getVal());
								StringBuffer tempPath = new StringBuffer()
										.append(storagePath).append("/")
										.append(user_id).append(".jpg");
								Intent it = new Intent(
										"android.media.action.IMAGE_CAPTURE");
								if (Utils.debug)
									Log.i(TAG, "+++++++++++++++++++++"
											+ tempPath.toString());
								it.putExtra(MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(new File(tempPath
												.toString())));
								it.putExtra(Utils.IMAGE_CAMRA,
										tempPath.toString());
								startActivityForResult(it,
										ACTIVITY_ACTION_PHOTO_CAPTURE);
								// Utils.inPhoto = true;
							}
						}
					}).show();
			break;
		}

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_ACTION_PIC_VIEW:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto = false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(UserInfoSupplementActivity.this,
							R.string.prompt,
							R.string.userinfo_upload_failed_unknow);
					return;
				}
				cursor.moveToFirst();
				String imagePath = cursor.getString(1);
				cursor.close();
				try {

					Intent i = new Intent(this, EditHeadIcon.class);
					i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
					i.putExtra(EditHeadIcon.STATE_COME,
							EditHeadIcon.FROM_GALLERY);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e("Exception", e.getMessage(), e);
				}
			}
			break;
		case ACTION_EDIT_PICK:
			// srcBitmap = intent.getParcelableExtra("image");
			if (resultCode == Activity.RESULT_OK) {
				Bitmap tempimg = null;
				// tempimg=data.getParcelableExtra("image");
				tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
				headPhoto.setBackgroundDrawable(new BitmapDrawable(Utils
						.getRoundedCornerBitmap(tempimg)));
			}

			break;
		case ACTIVITY_ACTION_PHOTO_CAPTURE:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto = false;

				try {
					if (Utils.debug)
						Log.i(TAG, "_____________:进入");
					String storagePath = Utils
							.getStoragePath(UserInfoSupplementActivity.this);
					// long user_id =
					// Long.parseLong(prefs.getString(CommonData.USER_ID,
					// "-1"));
					long user_id = Long.parseLong(mSess.mPrefMeme.user_id
							.getVal());
					StringBuffer tempPath = new StringBuffer()
							.append(storagePath).append("/").append(user_id)
							.append(".jpg");
					File file = new File(tempPath.toString());
					if (Utils.debug) {
						if (Utils.debug)
							Log.i(TAG, file
									+ "____________getImgByPath______________");
					}
					String imagePath = tempPath.toString();
					if (!file.exists()) {
						Uri uri = data.getData();
						imagePath = getRealPathFromURI(uri);

					}
					// Bitmap bitmap = BitmapFactory
					// .decodeStream(cr.openInputStream(uri));
					// Bitmap bitmap = Utils.fitSizeImg(imagePath);
					/*
					 * Bitmap bitmap =Utils.getSrcImage(uri,this,480,800);
					 * Intent i = new Intent(this,EditHeadActivity.class);
					 * TxData td = (TxData)this.getApplication(); td.cardBitmap
					 * = bitmap; startActivityForResult(i, 20);
					 */
					// Bitmap bitmap =Utils.getSrcImage(uri,this,480,800);
					// Intent i = new Intent(this,EditImage1.class);
					// // TxData td = (TxData)this.getApplication();
					// // td.cardBitmap = bitmap;
					// i.putExtra("local", imagePath);
					// startActivityForResult(i, ACTION_EDIT_PICK);
					Intent i = new Intent(this, EditHeadIcon.class);
					i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
					i.putExtra(EditHeadIcon.STATE_COME,
							EditHeadIcon.FROM_CAMERA);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e("Exception", e.getMessage(), e);
				}
				break;
			}
			break;
		case REQUESTCODE_FOR_REQUSET_AREA:
			mDistrictContent.setText(DataContainer.getAreaNameByIds(StringUtils
					.str2List(mSess.mPrefMeme.area.getVal()).toArray(
							new String[0])));

		}
	}

	public Bitmap fitSizeImg(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		// Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 20480) { // 0-20k
			opts.inSampleSize = 1;
		} else if (file.length() < 51200) { // 20-50k
			opts.inSampleSize = 2;
		} else if (file.length() < 307200) { // 50-300k
			opts.inSampleSize = 4;
		} else if (file.length() < 819200) { // 300-800k
			opts.inSampleSize = 6;
		} else if (file.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 8;
		} else if (file.length() < 1048576 * 2) { // 1024-1024k
			opts.inSampleSize = 9;
		} else {
			// opts.inSampleSize = 10;
			return null;
		}
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(file.getPath(), opts));
		file = null;
		return wref.get();
	}

	public Bitmap getSelectSendImg(Intent data) {
		try {
			if (data == null)
				return null;
			Uri originalUri = data.getData();
			String realpath = getRealPathFromURI(originalUri);
			Bitmap img = fitSizeImg(realpath);
			if (img == null) {
				return null;
			}
			WeakReference<Bitmap> wref = new WeakReference<Bitmap>(img);
			Bitmap bm = Utils.ResizeBitmapLv(wref.get(), 6);
			img = null;
			WeakReference<Bitmap> wref1 = new WeakReference<Bitmap>(bm);
			bm = null;
			return wref1.get();
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
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
		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

}
