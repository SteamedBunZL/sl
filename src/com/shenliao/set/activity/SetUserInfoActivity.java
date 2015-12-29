package com.shenliao.set.activity;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.shenliao.data.DataContainer;
import com.shenliao.set.activity.SetUserInfoUpdateNameAcitivity.UpdateReceiver;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditHeadIcon;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 个人资料页设置
 * 
 * @author xch
 * 
 */
public class SetUserInfoActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "SetUserInfoActivity";
	public final static String pblicInfo = "pblicinfo";
	public final static String INFORTX = "infortx";
	public static final int TUIXIN_USER_INFO = 110; // 设置进入的

	private LinearLayout mUserNickNameLinear;// 昵称模块
	private LinearLayout mUserSexLinear;// 性别模块
	private LinearLayout mUserBirthdayLinear;// 生日模块
	private LinearLayout mUserAreaLinear;// 地区模块
	private LinearLayout mUserBloodTypeLinear;// 血型模块
	private LinearLayout mUserProfessionLinear;// 职业模块
	private LinearLayout mUserLanguageLinear;// 语言模块
	private LinearLayout mUserFavouriteLinear;// 兴趣爱好模块
	private LinearLayout mUserHeadLinear;// 头像模块
	private LinearLayout btn_back_left;// 头像模块

	private TextView mUserNickName;// 昵称
	private TextView mUserSex;// 性别
	private TextView mUserBirthday;// 出生日期
	private TextView mUserArea;// 地区
	private TextView mUserBloodType;// 血型
	private TextView mUserProfession;// 职业
	private TextView mUserLanguage;// 语言
	private TextView mUserFavourite;// 兴趣爱好
	private TX myTx;// 个人信息
	// private GridView albumGridView;// 相册
	// private AlbumGridViewAdapter albumAdapter;
	// private SharedPreferences prefs;
	// private Editor editor;
	private SmileyParser sParser;// 解析表情
	private String[] bloodtypes;// 血型集合 ,0是A，1是B，2是AB，3是O
	private String[] sexs;// 性别集合，0是男，1是女
	// 生日参数
	private int year;
	private int month;
	private int day;
	private int currentYear;
	private int currentMonth;
	private int currentDay;

	private static final int FLUSH_ADAPTER = 1;// 刷新
	private static final int FLUSH_ALBUM = 200;
	public static final int REQUESTCODE_FOR_REQUSET_NICKNAME = 1;// 昵称请求码
	public static final int RESULTCODE_FOR_RESULT_NICENAME = 2;// 昵称返回请求码
	public static final int REQUESTCODE_FOR_REQUSET_PROSESSION = 3;// 职业请求码
	public static final int RESULTCODE_FOR_RESULT_PROSESSION = 4;// 职业返回码
	public static final int REQUESTCODE_FOR_REQUSET_LANGUAGE = 5;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_LANGUAGE = 6;// 语言返回码
	public static final int REQUESTCODE_FOR_REQUSET_FAVOURITE = 7;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_FAVOURITE = 8;// 语言返回码
	public static final int REQUESTCODE_FOR_REQUSET_AREA = 9;// 语言请求码
	public static final int RESULTCODE_FOR_RESULT_AREA = 10;// 语言返回码
	// public static final int PHOTOHRAPH = 100;// 拍照
	// public static final int PHOTOZOOM = 101; // 相册选择
	// public static final int PHOTORESOULT = 102;// 结果
	// public ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
	// public static final String IMAGE_UNSPECIFIED = "image/*";
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	// public static final int ACTIVITY_ACTION_PIC_VIEW = 103;
	// public static final int ACTIVITY_ACTION_PHOTO_CAPTURE = 104;
	public static final int GET_HEAD_IMG_FROM_CAMERA = 14;// 用相机拍照上传头像
	public static final int GET_HEAD_IMG_FROM_GALLERY = 15;// 从图库选择图片上传头像
	public static final int GET_ALBUM_IMG_FROM_CAMERA = 99;// 拍照上传相册图片
	public static final int GET_ALBUM_IMG_FROM_GALLERY = 103;// 从图片选择要上传的相册照片

	public static final int EDIT_HEAD_IMG = 17;// 用EditHeadIcon编辑过的头像
	// public static final int MAX_SIZE = 8;
	// private int imgPos;
	// private AlbumItem aiAdd = new AlbumItem();
	// private boolean isAdded;

	private UpdateReceiver updatereceiver;

	// private int deafaultHeadImage;

	// private ProgressDialog pd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_userinfo);

		init();
		setData();
	}

	// 初始化
	private void init() {
		mUserNickNameLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_nick);
		mUserSexLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_sex);
		mUserBirthdayLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_birth);
		mUserAreaLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_area);
		mUserBloodTypeLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_bloodtype);
		mUserProfessionLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_profession);
		mUserLanguageLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_language);
		mUserFavouriteLinear = (LinearLayout) findViewById(R.id.sl_tab_setting__userinfo_favourite);
		mUserHeadLinear = (LinearLayout) findViewById(R.id.sl_tab_setting_userinfo_head);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		mUserNickName = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_nickName);
		mUserSex = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_sex);
		mUserBirthday = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_birth);
		mUserArea = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_area);
		mUserBloodType = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_bloodType);
		mUserProfession = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_profession);
		mUserLanguage = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_language);
		mUserFavourite = (TextView) findViewById(R.id.sl_tab_setting_userinfo_include_favourite);

		mUserHead = (ImageView) findViewById(R.id.sl_tab_setting_userinfo_include_head);

		// albumGridView = (GridView)
		// findViewById(R.id.sl_tab_setting_userinfo_gridView);
		// albumAdapter = new AlbumGridViewAdapter(this);
		// albumGridView.setAdapter(albumAdapter);
		// albumAdapter.setList(list);
		// albumAdapter.notifyDataSetChanged();
		// albumGridView.setOnItemClickListener(new OnItemClickListener() {
		//
		// @Override
		// public void onItemClick(AdapterView<?> parent, View view, int pos,
		// long rowId) {
		// imgPos = pos;
		// AlbumItem ai = list.get(pos);
		// // if (ai.isAdd()) {
		// // showAlbumAddMenu();
		// // } else {
		// // showAlbumItemMenu();
		// // }
		// if (ai.isAdd()) {
		// showAlbumAddMenu();
		// } else {
		// Intent intent = new Intent(SetUserInfoActivity.this,
		// UserAlbumActivity.class);
		// intent.putExtra("aiList", list);
		// intent.putExtra("pos", pos);
		// intent.putExtra("uid", TX.tm.getTxMe().partner_id);
		// startActivity(intent);
		// }
		//
		// }
		// });
		// albumGridView.setOnItemLongClickListener(new
		// OnItemLongClickListener() {
		//
		// @Override
		// public boolean onItemLongClick(AdapterView<?> parent, View view, int
		// pos, long id) {
		// imgPos = pos;
		// AlbumItem ai = list.get(pos);
		// if (!ai.isAdd()) {
		// showAlbumItemMenu();
		// }
		// return true;
		// }
		// });

		// 设置各个模块的点击事件
		mUserNickNameLinear.setOnClickListener(this);
		mUserSexLinear.setOnClickListener(this);
		mUserBirthdayLinear.setOnClickListener(this);
		mUserAreaLinear.setOnClickListener(this);
		mUserBloodTypeLinear.setOnClickListener(this);
		mUserProfessionLinear.setOnClickListener(this);
		mUserLanguageLinear.setOnClickListener(this);
		mUserFavouriteLinear.setOnClickListener(this);
		mUserHeadLinear.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);

		Calendar calendar = Calendar.getInstance();
		currentYear = calendar.get(Calendar.YEAR);
		currentMonth = calendar.get(Calendar.MONTH);
		currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		bloodtypes = this.getResources().getStringArray(
				R.array.bloodtype_dialog_items);
		sexs = this.getResources().getStringArray(R.array.sex_dialog_items);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		sParser = Utils.getSmileyParser(this);
		myTx = TX.tm.getTxMe();
	}

	// protected void showAlbumItemMenu() {
	// new AlertDialog.Builder(this).setTitle("更多选项")
	// .setItems(R.array.album_item_menu, new DialogInterface.OnClickListener()
	// {
	// public void onClick(DialogInterface dialog, int which) {
	// if (which == 0) {
	// String storagePath = Utils.getStoragePath(SetUserInfoActivity.this);
	// if (Utils.isNull(storagePath)) {
	// return;
	// }
	// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	// intent.addCategory(Intent.CATEGORY_OPENABLE);
	// intent.setType(MIME_TYPE_IMAGE_JPEG);
	// startActivityForResult(intent, PHOTOZOOM);
	// Utils.inPhoto = true;
	//
	// } else if (which == 1) {
	// if (!Utils.checkSDCard()) {
	// BLog.i("TAG", "无SD卡");
	// return;
	// }
	// String storagePath = Utils.getStoragePath(SetUserInfoActivity.this);
	// if (Utils.isNull(storagePath)) {
	// return;
	// }
	// File sddir = new File(storagePath);
	// if (!sddir.exists() && !sddir.mkdirs()) {
	// if (Utils.debug)
	// Log.e("bitmapFromUrl", "Create dir failed");
	// }
	// StringBuffer tempPath = new
	// StringBuffer().append(storagePath).append("/self")
	// .append(".jpg");
	// Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
	// BLog.i("TAG", "+++++++++++++++++++++" + tempPath.toString());
	// it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
	// File(tempPath.toString())));
	// it.putExtra("image_camra", tempPath.toString());
	// startActivityForResult(it, PHOTOHRAPH);
	// Utils.inPhoto = true;
	//
	// } else if (which == 2) {
	// list.remove(imgPos);
	// addAlbumAddItem();
	// updateAlbumUrl(imgPos, null);
	// albumAdapter.setList(list);
	// albumAdapter.notifyDataSetChanged();
	// } else {
	//
	// }
	// }
	// }).show();
	// }
	//
	// private void addAlbumAddItem() {
	// // if (!isAdded) {
	// // aiAdd.setAdd(true);
	// // list.add(aiAdd);
	// // isAdded = true;
	// // }
	// for (int i = 0; i < list.size(); i++) {
	// if (list.get(i).isAdd()) {
	// break;
	// }
	// if (i == list.size() - 1) {
	// aiAdd.setAdd(true);
	// list.add(aiAdd);
	// }
	// }
	// }
	//
	// protected void showAlbumAddMenu() {
	// new AlertDialog.Builder(this).setTitle("更多选项")
	// .setItems(R.array.album_add_menu, new DialogInterface.OnClickListener() {
	// public void onClick(DialogInterface dialog, int which) {
	// if (which == 0) {
	// String storagePath = Utils.getStoragePath(SetUserInfoActivity.this);
	// if (Utils.isNull(storagePath)) {
	// return;
	// }
	// Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
	// intent.addCategory(Intent.CATEGORY_OPENABLE);
	// intent.setType(MIME_TYPE_IMAGE_JPEG);
	// startActivityForResult(intent, PHOTOZOOM);
	// Utils.inPhoto = true;
	//
	// } else if (which == 1) {
	// if (!Utils.checkSDCard()) {
	// BLog.i("TAG", "无SD卡");
	// return;
	// }
	// String storagePath = Utils.getStoragePath(SetUserInfoActivity.this);
	// if (Utils.isNull(storagePath)) {
	// return;
	// }
	// File sddir = new File(storagePath);
	// if (!sddir.exists() && !sddir.mkdirs()) {
	// if (Utils.debug)
	// Log.e("bitmapFromUrl", "Create dir failed");
	// }
	// StringBuffer tempPath = new
	// StringBuffer().append(storagePath).append("/self")
	// .append(".jpg");
	// Intent it = new Intent("android.media.action.IMAGE_CAPTURE");
	// BLog.i("TAG", "+++++++++++++++++++++" + tempPath.toString());
	// it.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new
	// File(tempPath.toString())));
	// it.putExtra("image_camra", tempPath.toString());
	// startActivityForResult(it, PHOTOHRAPH);
	// Utils.inPhoto = true;
	//
	// } else {
	//
	// }
	// }
	// }).show();
	// }

	// handler
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				long tag = (Long) mUserHead.getTag();
				long id = (Long) result[1];
				if (result[0] != null && tag == id) {
					mUserHead.setImageBitmap((Bitmap) result[0]);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 设置数据
	private void setData() {
		// 昵称
		if (!Utils.isNull(myTx.getNick_name())) {
			mUserNickName.setText(sParser.addSmileySpans(myTx.getNick_name(),
					true, 0));
		} else {
			// String tempname = prefs.getString(CommonData.INPUTNAME, "");
			// mUserNickName.setText(tempname);
			// 昵称为空则显示神聊号吧？2013.12.19 shc
			mUserNickName.setText("" + myTx.partner_id);
		}
		// 性别
		mUserSex.setText(myTx.getSex() == TX.MALE_SEX ? this.getResources()
				.getString(R.string.user_male) : this.getResources().getString(
				R.string.user_female));

		// 头像
		if (myTx.getSex() == TX.MALE_SEX) {

			defaultHeaderImg = R.drawable.user_infor_head_boy;

		} else {

			defaultHeaderImg = R.drawable.user_infor_head_girl;

		}
		if (myTx != null && Utils.isIdValid(myTx.partner_id)) {
			if (myTx.partner_id == TX.TUIXIN_MAN) {
				mUserHead.setImageResource(R.drawable.tuixin_manage);
			} else {

				mUserHead.setTag(myTx.partner_id);
				mUserHead.setImageResource(defaultHeaderImg);

				Bitmap avatar = mSess.avatarDownload.getAvatar(myTx.avatar_url,
						myTx.partner_id, mUserHead, avatarHandler);

				if (avatar != null)
					mUserHead.setImageBitmap(avatar);

			}
		}

		// 生日
		showBirthDay("" + myTx.birthday);
		// 地区
		if (myTx.area != null && !"".equals(myTx.area)) {
			List<String> mlist = StringUtils.str2List(myTx.area);
			mUserArea.setText(DataContainer.getAreaNameByIds(mlist
					.toArray(new String[0])));
		} else {
			mUserArea.setTextColor(this.getResources().getColor(R.color.red));
			mUserArea.setText("未填写");
		}
		// 血型
		showBloodType(myTx.bloodType);
		// 职业
		if (!Utils.isNull(myTx.job)) {
			mUserProfession.setText(sParser.addSmileySpans(myTx.job, true, 0));
		} else {
			mUserProfession.setTextColor(this.getResources().getColor(
					R.color.red));
			mUserProfession.setText("未填写");
		}
		// 兴趣爱好
		if (!Utils.isNull(myTx.hobby)) {
			List<String> mlist = StringUtils.str2List(myTx.hobby);
			if (mlist.contains("0")) {
				mlist.remove("0");
			}
			if (mlist != null && mlist.size() > 0) {
				mUserFavourite.setText(DataContainer.getHobbyNameByIds(mlist
						.toArray(new String[0])));
			} else {
				mUserFavourite.setTextColor(this.getResources().getColor(
						R.color.red));
				mUserFavourite.setText("未填写");
			}
		} else {
			mUserFavourite.setTextColor(this.getResources().getColor(
					R.color.red));
			mUserFavourite.setText("未填写");
		}
		// 语言
		if (!Utils.isNull(myTx.getLanguages())) {
			List<String> mlist = StringUtils.str2List(myTx.getLanguages());
			if (mlist.contains("0")) {
				mlist.remove("0");
			}
			if (mlist != null && mlist.size() > 0) {
				mUserLanguage.setText(DataContainer.getLangNameByIds(mlist
						.toArray(new String[0])));
			} else {
				mUserLanguage.setTextColor(this.getResources().getColor(
						R.color.red));
				mUserLanguage.setText("未填写");
			}
		} else {
			mUserLanguage.setTextColor(this.getResources()
					.getColor(R.color.red));
			mUserLanguage.setText("未填写");
		}
		// list = TX.tm.getTxMe().getAlbum();
		// albumAdapter.setList(list);
		// albumAdapter.notifyDataSetChanged();

	}

	// 显示生日方法
	private void showBirthDay(String birthday) {

		if (!"0".equals(birthday) && birthday.length() == 8) {
			int nowyear = Integer.valueOf(birthday.substring(0, 4));
			int nowmonth = Integer.valueOf(birthday.substring(4, 6));
			int nowday = Integer.valueOf(birthday.substring(6, 8));
			mUserBirthday.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			mUserBirthday.setText(nowyear
					+ SetUserInfoActivity.this.getResources().getString(
							R.string.year_prompt)
					+ nowmonth
					+ SetUserInfoActivity.this.getResources().getString(
							R.string.month_prompt)
					+ nowday
					+ SetUserInfoActivity.this.getResources().getString(
							R.string.day_prompt));
		} else {
			mUserBirthday.setTextColor(this.getResources()
					.getColor(R.color.red));
			mUserBirthday.setText("未填写");
		}

	}

	// 显示血型
	private void showBloodType(int blood_type) {
		int ibloodType = blood_type;
		if (ibloodType == -1) {
			mUserBloodType.setTextColor(this.getResources().getColor(
					R.color.red));
			mUserBloodType.setText("未填写");
		} else if (ibloodType >= bloodtypes.length) {
			mUserBloodType.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			mUserBloodType.setText(bloodtypes[4]);
		} else {
			mUserBloodType.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			mUserBloodType.setText(bloodtypes[ibloodType]);
		}
	}

	// 个人模块的点击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 点击头像
		case R.id.sl_tab_setting_userinfo_head:
			final AlertDialog.Builder headDialog = new AlertDialog.Builder(
					SetUserInfoActivity.this);
			headDialog.setTitle("更多选项");
			headDialog.setItems(R.array.album_add_menu,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								// 从手机图库中选择头像照片
								String storagePath = Utils
										.getStoragePath(SetUserInfoActivity.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								Intent getImage = new Intent(
										Intent.ACTION_GET_CONTENT);
								getImage.addCategory(Intent.CATEGORY_OPENABLE);
								getImage.setType(MIME_TYPE_IMAGE_JPEG);
								startActivityForResult(getImage,
										GET_HEAD_IMG_FROM_GALLERY);
								dialog.dismiss();
								// Utils.inPhoto = true;
							} else if (which == 1) {
								// 用相机拍照选择头像照片

								if (!Utils.checkSDCard()) {
									if (Utils.debug)
										Log.i(TAG, "无SD卡");
									return;
								}
								String storagePath = Utils
										.getStoragePath(SetUserInfoActivity.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								File sddir = new File(storagePath);
								if (!sddir.exists() && !sddir.mkdirs()) {
									if (Utils.debug)
										Log.e(TAG, "拍照取头像时，创建神聊根目录失败");
								}
								// long user_id =
								// Long.parseLong(getPrefsMeme().getString(
								// CommonData.USER_ID, "-1"));
								// StringBuffer tempPath = new StringBuffer()
								// .append(storagePath).append("/")
								// .append(user_id).append(".jpg");
								String tempPath = storagePath + "/"
										+ TX.tm.getUserID() + ".jpg";
								Intent it = new Intent(
										"android.media.action.IMAGE_CAPTURE");
								if (Utils.debug)
									Log.i(TAG, "相机拍照获取的头像图片临时文件路径为：" + tempPath);
								it.putExtra(MediaStore.EXTRA_OUTPUT,
										Uri.fromFile(new File(tempPath
												.toString())));
								it.putExtra(Utils.IMAGE_CAMRA,
										tempPath.toString());
								startActivityForResult(it,
										GET_HEAD_IMG_FROM_CAMERA);
								// Utils.inPhoto = true;
							}
						}
					}).show();
			break;
		// 点击昵称
		case R.id.sl_tab_setting__userinfo_nick:
			Intent intentNick = new Intent(SetUserInfoActivity.this,
					SetUserInfoUpdateNameAcitivity.class);
			startActivityForResult(intentNick, REQUESTCODE_FOR_REQUSET_NICKNAME);
			break;
		// 性别
		case R.id.sl_tab_setting__userinfo_sex:
			Builder dialogSex = new AlertDialog.Builder(
					SetUserInfoActivity.this).setItems(sexs,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < sexs.length) {
								// editor.putInt(CommonData.SEX,
								// which).commit();
								mSess.mPrefMeme.sex.setVal(which).commit();
								TX.tm.reloadTXMe();// /////
								// mUserBloodType.setText(bloodtypes[which]);
								Message msg = new Message();
								msg.what = FLUSH_ADAPTER;
								handler.sendMessage(msg);
								mSess.getSocketHelper().sendUserInforChange();
							}
						}
					});
			dialogSex.setTitle(this.getResources().getString(
					R.string.user_sel_sex));
			dialogSex.show();
			break;

		// 出生日期

		case R.id.sl_tab_setting__userinfo_birth:
			String birthStr = String.valueOf(myTx.birthday);
			if (myTx.birthday != 0 && birthStr.length() == 8) {
				year = Integer.valueOf(birthStr.substring(0, 4));
				month = Integer.valueOf(birthStr.substring(4, 6)) - 1;
				day = Integer.valueOf(birthStr.substring(6, 8));
			} else {
				year = 1990;
				month = 0;
				day = 1;

			}
			// if (year == 0 && month == 0 && day == 0) {
			// year = currentYear - 20;
			// month = currentMonth;
			// day = currentDay;
			// if (year < 0)
			// year = 0;
			// }
			new DatePickerDialog(SetUserInfoActivity.this, onDateSetListener,
					year, month, day).show();
			break;

		// 点击血型
		case R.id.sl_tab_setting__userinfo_bloodtype:
			Builder dialogBlood = new AlertDialog.Builder(
					SetUserInfoActivity.this).setItems(bloodtypes,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (which < bloodtypes.length) {
								// editor.putInt(CommonData.BLOODTYPE,
								// which).commit();
								mSess.mPrefMeme.bloodtype.setVal(which)
										.commit();
								TX.tm.reloadTXMe();// ///
								// mUserBloodType.setText(bloodtypes[which]);
								Message msg = new Message();
								msg.what = FLUSH_ADAPTER;
								handler.sendMessage(msg);
								mSess.getSocketHelper().sendUserInforChange();
							}
						}
					});
			dialogBlood.setTitle(this.getResources().getString(
					R.string.user_sel_bloodtype));
			dialogBlood.show();
			break;
		// 职业修改
		case R.id.sl_tab_setting__userinfo_profession:
			Intent intentPro = new Intent(SetUserInfoActivity.this,
					SetUpdateProfessionActivity.class);
			startActivityForResult(intentPro,
					REQUESTCODE_FOR_REQUSET_PROSESSION);
			break;
		// 兴趣修改
		case R.id.sl_tab_setting__userinfo_favourite:
			Intent intentFavor = new Intent(SetUserInfoActivity.this,
					SetUpdateFavouriteActivity.class);
			startActivityForResult(intentFavor,
					REQUESTCODE_FOR_REQUSET_FAVOURITE);
			break;
		// 语言
		case R.id.sl_tab_setting__userinfo_language:
			Intent intentLan = new Intent(SetUserInfoActivity.this,
					SetUpdateLanguageActivity.class);
			intentLan.putExtra(SetUpdateLanguageActivity.GOINPAGE,
					SetUpdateLanguageActivity.NOTFINDFRIEND);
			startActivityForResult(intentLan, REQUESTCODE_FOR_REQUSET_LANGUAGE);
			break;
		// 地区
		case R.id.sl_tab_setting__userinfo_area:
			Intent intentArea = new Intent(SetUserInfoActivity.this,
					SetUpdateAreaActivity.class);
			startActivityForResult(intentArea, REQUESTCODE_FOR_REQUSET_AREA);
			break;
		// 地区
		case R.id.btn_back_left:
			finish();
			break;
		}

	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case FLUSH_ADAPTER:
				// int ibloodType = prefs.getInt(CommonData.BLOODTYPE, -1);
				int ibloodType = mSess.mPrefMeme.bloodtype.getVal();
				if (ibloodType == -1) {
					mUserBloodType.setText("未填写");
					mUserBloodType.setTextColor(SetUserInfoActivity.this
							.getResources().getColor(R.color.red));
				} else if (ibloodType >= bloodtypes.length) {
					mUserBloodType.setTextColor(SetUserInfoActivity.this
							.getResources().getColor(
									R.color.sub_title_text_color));
					mUserBloodType.setText(bloodtypes[0]);
				} else {
					mUserBloodType.setTextColor(SetUserInfoActivity.this
							.getResources().getColor(
									R.color.sub_title_text_color));
					mUserBloodType.setText(bloodtypes[ibloodType]);
				}
				// int isex = prefs.getInt(CommonData.SEX, -1);
				int isex = mSess.mPrefMeme.sex.getVal();
				mUserSex.setText(isex == TX.MALE_SEX ? SetUserInfoActivity.this
						.getResources().getString(R.string.user_male)
						: SetUserInfoActivity.this.getResources().getString(
								R.string.user_female));
				// showBirthDay(prefs.getInt(CommonData.BIRTHDAY, 0) + "");
				showBirthDay(mSess.mPrefMeme.birthday.getVal() + "");

				break;
			// case FLUSH_ALBUM:
			// albumAdapter.notifyDataSetChanged();
			// break;

			}
		}
	};
	public static final int[] constellationEdgeDay = { 20, 19, 21, 20, 21, 22,
			23, 23, 23, 24, 23, 22 };
	DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
		@Override
		public void onDateSet(DatePicker view, int year, int month, int day) {
			String tempMonth = "";
			if (month + 1 < 10) {
				tempMonth = "0" + (month + 1);
			} else {
				tempMonth = "" + (month + 1);
			}
			String tempDay = "";
			if (day < 10) {
				tempDay = "0" + day;
			} else {
				tempDay = "" + day;
			}
			// editor.putInt(CommonData.BIRTHDAY, Integer.valueOf("" + year +
			// tempMonth + tempDay)).commit();
			mSess.mPrefMeme.birthday.setVal(
					Integer.valueOf("" + year + tempMonth + tempDay)).commit();
			TX.tm.reloadTXMe();// ///
			SetUserInfoActivity.this.year = year;
			SetUserInfoActivity.this.month = month;
			SetUserInfoActivity.this.day = day;
			Message msg = new Message();
			msg.what = FLUSH_ADAPTER;
			handler.sendMessage(msg);
			mSess.getSocketHelper().sendUserInforChange();
		}
	};
	private ImageView mUserHead;

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUESTCODE_FOR_REQUSET_NICKNAME
				&& resultCode == RESULTCODE_FOR_RESULT_NICENAME) {
			mUserNickName.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			// mUserNickName.setText(sParser.addSmileySpans(prefs.getString(CommonData.NICKNAME,
			// ""), true, 0));
			mUserNickName.setText(sParser.addSmileySpans(
					mSess.mPrefMeme.nickname.getVal(), true, 0));
		} else if (requestCode == REQUESTCODE_FOR_REQUSET_PROSESSION
				&& resultCode == RESULTCODE_FOR_RESULT_PROSESSION) {
			mUserProfession.setTextColor(SetUserInfoActivity.this
					.getResources().getColor(R.color.sub_title_text_color));
			// mUserProfession.setText(sParser.addSmileySpans(prefs.getString(CommonData.JOB,
			// ""), true, 0));
			mUserProfession.setText(sParser.addSmileySpans(
					mSess.mPrefMeme.job.getVal(), true, 0));

		} else if (requestCode == REQUESTCODE_FOR_REQUSET_LANGUAGE
				&& resultCode == RESULTCODE_FOR_RESULT_LANGUAGE) {
			// List<String> mlist =
			// StringUtils.str2List(prefs.getString(CommonData.LANGUAGES, ""));

			List<String> mlist = StringUtils.str2List(mSess.mPrefMeme.languages
					.getVal());
			if (mlist != null && mlist.contains("0")) {
				mlist.remove("0");
			}
			mUserLanguage.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			if (mlist != null) {
				mUserLanguage.setText(DataContainer.getLangNameByIds(mlist
						.toArray(new String[0])));
			}
		} else if (requestCode == REQUESTCODE_FOR_REQUSET_FAVOURITE
				&& resultCode == RESULTCODE_FOR_RESULT_FAVOURITE) {
			mUserFavourite.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			// List<String> mlist =
			// StringUtils.str2List(prefs.getString(CommonData.HOBBY, ""));
			List<String> mlist = StringUtils.str2List(mSess.mPrefMeme.hobby
					.getVal());
			mUserFavourite.setText(DataContainer.getHobbyNameByIds(mlist
					.toArray(new String[0])));
		} else if (requestCode == REQUESTCODE_FOR_REQUSET_AREA
				&& resultCode == RESULTCODE_FOR_RESULT_AREA) {
			mUserArea.setTextColor(SetUserInfoActivity.this.getResources()
					.getColor(R.color.sub_title_text_color));
			// mUserArea.setText(DataContainer.getAreaNameByIds(StringUtils.str2List(prefs.getString(CommonData.AREA,
			// ""))
			// .toArray(new String[0])));
			mUserArea.setText(DataContainer.getAreaNameByIds(StringUtils
					.str2List(mSess.mPrefMeme.area.getVal()).toArray(
							new String[0])));
		} else if (requestCode == GET_HEAD_IMG_FROM_GALLERY
				&& resultCode == RESULT_OK) {
			// 从相册修改头像的返回
			// Utils.inPhoto = false;
			Uri uri = data.getData();
			ContentResolver cr = this.getContentResolver();
			Cursor cursor = cr.query(uri, null, null, null, null);
			if (cursor == null || (cursor != null && cursor.getCount() <= 0)) {
				Utils.startPromptDialog(SetUserInfoActivity.this,
						R.string.prompt, R.string.userinfo_upload_failed_unknow);
				return;
			}
			cursor.moveToFirst();
			String imagePath = cursor.getString(1);
			cursor.close();
			try {

				Intent i = new Intent(this, EditHeadIcon.class);
				i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
				i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.FROM_GALLERY);
				startActivityForResult(i, EDIT_HEAD_IMG);
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "异常", e);
			}
		} else if (requestCode == EDIT_HEAD_IMG && resultCode == RESULT_OK) {
			// 编辑头像返回
			Bitmap tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
			mSess.avatarDownload.cachePartnerBitmap(TX.tm.getUserID(),
					Utils.getRoundedCornerBitmap(tempimg));
			if (Utils.debug)
				Log.e(TAG, "已经更新了头像缓存，等待onResume时更新头像");

		} else if (requestCode == GET_HEAD_IMG_FROM_CAMERA
				&& resultCode == RESULT_OK) {
			// 从照相机获取头像照片
			// Utils.inPhoto = false;
			try {
				String storagePath = Utils
						.getStoragePath(SetUserInfoActivity.this);
				// long user_id = Long.parseLong(getPrefsMeme().getString(
				// CommonData.USER_ID, "-1"));
				long user_id = Long.parseLong(mSess.mPrefMeme.user_id.getVal());
				StringBuffer tempPath = new StringBuffer().append(storagePath)
						.append("/").append(user_id).append(".jpg");
				File file = new File(tempPath.toString());
				String imagePath = tempPath.toString();
				Bitmap tempimg = Utils.fitSizeAutoImg(imagePath,
						Utils.msgroom_list_resolution);
				if (tempimg == null && data != null) {
					Bundle extras = data.getExtras();
					if (extras != null) {
						tempimg = (Bitmap) extras.get("data");
						if (Utils.createPhotoFile(tempimg, "" + user_id
								+ ".jpg") == null) {
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
				Intent i = new Intent(this, EditHeadIcon.class);
				i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
				i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.FROM_CAMERA);
				startActivityForResult(i, EDIT_HEAD_IMG);
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "处理从照相机获取头像照片异常", e);
			}
		}
	}

	@Override
	protected void onResume() {
		// int isex = getPrefsMeme().getInt(CommonData.SEX, -1);
		int isex = mSess.mPrefMeme.sex.getVal();
		if (isex == TX.MALE_SEX) {

			defaultHeaderImg = R.drawable.user_infor_head_boy;

		} else {

			defaultHeaderImg = R.drawable.user_infor_head_girl;

		}

		mUserHead.setTag(TX.tm.getUserID());
		mUserHead.setImageResource(defaultHeaderImg);

		Bitmap avatar = mSess.avatarDownload.getAvatar(myTx.avatar_url,
				TX.tm.getUserID(), mUserHead, avatarHandler);

		if (avatar != null)
			mUserHead.setImageBitmap(avatar);

		// ImageLoader.getInstance(txdata).loadImage(txdata.getUserID(),
		// myTx.avatar_url, mUserinfoHead,
		// deafaultHeadImage);
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	// // 拍照
	// if (requestCode == PHOTOHRAPH) {
	// Utils.inPhoto = false;
	//
	// try {
	// String storagePath = Utils.getStoragePath(this);
	// // long user_id = Long.parseLong(prefs.getString(CommonData.USER_ID,
	// "-1"));
	// StringBuffer tempPath = new
	// StringBuffer().append(storagePath).append("/").append("self")
	// .append(".jpg");
	// // File file = new File(tempPath.toString());
	// String imagePath = tempPath.toString();
	// // if (!file.exists()) {
	// // Uri uri = data.getData();
	// // imagePath=getRealPathFromURI(uri);
	// //
	// // }
	// Bitmap tempimg = Utils.fitSizeAutoImg(imagePath,
	// Utils.msgroom_list_resolution);
	// if (tempimg == null && data != null) {
	// Bundle extras = data.getExtras();
	// if (extras != null) {
	// tempimg = (Bitmap) extras.get("data");
	// if (Utils.createPhotoFile(tempimg, "self.jpg") == null) {
	// return;
	//
	// }
	// } else {
	// String path = data.getDataString();
	// if (!Utils.isNull(path)) {
	// Uri uri = Uri.parse(path);
	// imagePath = getRealPathFromURI(uri);
	// }
	// }
	// }
	// Intent i = new Intent(this, EditHeadIcon.class);
	// i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
	// i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.PHOTO_COME);
	// i.putExtra("isAlbum", true);
	// startActivityForResult(i, PHOTORESOULT);
	// } catch (Exception e) {
	// if (Utils.debug)
	// Log.e("Exception", e.getMessage(), e);
	// }
	// }
	//
	// // 读取相册缩放图片
	// if (requestCode == PHOTOZOOM && data != null) {
	// Utils.inPhoto = false;
	// Uri uri = data.getData();
	// ContentResolver cr = this.getContentResolver();
	// Cursor cursor = cr.query(uri, null, null, null, null);
	// if (cursor == null || (cursor != null && cursor.getCount() <= 0)) {
	// Utils.startPromptDialog(this, R.string.prompt,
	// R.string.userinfo_upload_failed_unknow);
	// return;
	// }
	// cursor.moveToFirst();
	// String imagePath = cursor.getString(1);
	// cursor.close();
	// try {
	// // Bitmap bitmap = BitmapFactory
	// // .decodeStream(cr.openInputStream(uri));
	// // Bitmap bitmap = Utils.fitSizeImg(imagePath);
	// /* Bitmap bitmap =Utils.getSrcImage(uri,this,480,800);
	// Intent i = new Intent(this,EditHeadActivity.class);
	// TxData td = (TxData)this.getApplication();
	// td.cardBitmap = bitmap;
	// startActivityForResult(i, 20);*/
	// // Bitmap bitmap =Utils.getSrcImage(uri,this,480,800);
	// // Intent i = new Intent(this,EditImage1.class);
	// // // TxData td = (TxData)this.getApplication();
	// // // td.cardBitmap = bitmap;
	// // i.putExtra("local", imagePath);
	// // startActivityForResult(i, ACTION_EDIT_PICK);
	// Intent i = new Intent(this, EditHeadIcon.class);
	// i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
	// i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.PIC_COME);
	// i.putExtra("isAlbum", true);
	// startActivityForResult(i, PHOTORESOULT);
	// } catch (Exception e) {
	// if (Utils.debug)
	// Log.e("Exception", e.getMessage(), e);
	// }
	// }
	// // 处理结果
	// if (requestCode == PHOTORESOULT) {
	// // Bundle extras = data.getExtras();
	// // Bitmap bm = extras.getParcelable(EditHeadIcon.GIVE_IMG);
	// if (Utils.tempBm != null) {
	// final Bitmap smallDstBitmap = Utils.ResizeBitmap(Utils.tempBm, 92);
	// final Bitmap bigdstBitmap = Utils.ResizeBitmap(Utils.tempBm, 400);
	// Utils.tempBm = null;
	// System.out.println("small bm:" + smallDstBitmap);
	// System.out.println("bit bm:" + bigdstBitmap);
	// String storagePath = Utils.getStoragePath(this);
	// final File sddir = new File(storagePath, Utils.AVATAR_PATH);
	// if (!sddir.exists() && !sddir.mkdirs()) {
	// if (Utils.debug)
	// Log.e("bitmapFromUrl", "Create dir failed");
	// sddir.mkdir();
	// }
	//
	// // ByteArrayOutputStream stream = new ByteArrayOutputStream();
	// // photo.compress(Bitmap.CompressFormat.JPEG, 75, stream);// (0
	// // -
	// // // 100)压缩文件
	//
	// final SocketClient socketClient = new SocketClient();
	// socketClient.setMode(SocketClient.UPLOAD_MODE);
	// socketClient.setFileName("ai.jpg");
	// socketClient.setId(imgPos);
	// socketClient.setFileType(SocketClient.FILE_TYPE_AVATAR);
	// socketClient.setUpAlbumCallback(new UploadAlbumCallback() {
	//
	// @Override
	// public void uploadFinish(long id, int resultCode) {
	// System.out.println("id:" + id);
	// System.out.println("result:" + resultCode);
	// System.out.println("file url:" + socketClient.getFileUrl());
	// StringBuffer tempPath = new
	// StringBuffer().append(sddir.getAbsolutePath()).append("/")
	// .append(new MD5().getMD5ofStr(socketClient.getFileUrl())).append(".jpg");
	// StringBuffer tempPath1 = new
	// StringBuffer().append(sddir.getAbsolutePath()).append("/")
	// .append(new
	// MD5().getMD5ofStr(socketClient.getFileUrl())).append("_big.jpg");
	// createFile(tempPath.toString(), smallDstBitmap);
	// createFile(tempPath1.toString(), bigdstBitmap);
	// updateAlbumUrl((int) id, socketClient.getFileUrl());
	// }
	//
	// });
	// socketClient.uploadImage(smallDstBitmap, bigdstBitmap);
	// showProgressDialog();
	// AlbumItem ai = new AlbumItem();
	// ai.setBitmap(smallDstBitmap);
	// if (!list.get(imgPos).isAdd()) {
	// list.remove(imgPos);
	// }
	// list.add(imgPos, ai);
	// if (list.size() > MAX_SIZE) {
	// list.remove(MAX_SIZE);
	// isAdded = !isAdded;
	// }
	// albumAdapter.setList(list);
	// albumAdapter.notifyDataSetChanged();
	// }
	//
	// }
	//
	// }
	//
	// private void updateAlbumUrl(int pos, String url) {
	// cancelProgressDialog();
	// if (url != null) {
	// for (int i = 0; i < list.size(); i++) {
	// if (pos == i) {
	// list.get(pos).setUrl(url);
	// list.get(pos).setBitmap(null);
	// }
	// }
	// handler.sendEmptyMessage(FLUSH_ALBUM);
	// }
	// List<String> albumUrls = new ArrayList<String>();
	// for (AlbumItem ai : list) {
	// if (!Utils.isNull(ai.getUrl()))
	// albumUrls.add(ai.getUrl());
	// }
	// getEditor().putString(CommonData.ALBUM,
	// StringUtils.list2String(albumUrls)).commit();
	// SocketHelper.getSocketHelper(txdata).setAlbum(albumUrls);
	// }
	//
	// public void createFile(String path, Bitmap bitmap) {
	// File file = new File(path);
	// if (file.exists())
	// file.delete();
	// if (!file.exists()) {
	// // 创建文件输出流
	// OutputStream os = null;
	// try {
	// os = new FileOutputStream(file);
	// // 存储
	// bitmap.compress(CompressFormat.JPEG, 100, os);
	// // 关闭流
	// } catch (FileNotFoundException e) {
	// //
	// e.printStackTrace();
	// } finally {
	// try {
	// if (os != null)
	// os.close();
	// } catch (IOException e) {
	// //
	// e.printStackTrace();
	// }
	// }
	// }
	// }

	// public void startPhotoZoom(Uri uri) {
	// Intent intent = new Intent("com.android.camera.action.CROP");
	// intent.setDataAndType(uri, IMAGE_UNSPECIFIED);
	// intent.putExtra("crop", "true");
	// // aspectX aspectY 是宽高的比例
	// intent.putExtra("aspectX", 1);
	// intent.putExtra("aspectY", 1);
	// // outputX outputY 是裁剪图片宽高
	// intent.putExtra("outputX", 64);
	// intent.putExtra("outputY", 64);
	// intent.putExtra("return-data", true);
	// startActivityForResult(intent, PHOTORESOULT);
	// }
	private String getRealPathFromURI(Uri contentUri) {

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

	// private void showProgressDialog() {
	// if (pd == null) {
	// pd = new ProgressDialog(this);
	// pd.setMessage("uploading....");
	// pd.setCancelable(false);
	// }
	// if (!pd.isShowing()) {
	// pd.show();
	// }
	// }
	//
	// private void cancelProgressDialog() {
	// if (pd != null && pd.isShowing()) {
	// pd.cancel();
	// }
	// }
}
