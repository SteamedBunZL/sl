package com.shenliao.set.activity;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

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
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.shenliao.user.adapter.AlbumGridViewAdapter;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.AboutActivity;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditHeadIcon;
import com.tuixin11sms.tx.activity.GalleryUrlActivity;
import com.tuixin11sms.tx.activity.HelpActivity;
import com.tuixin11sms.tx.activity.ReceivedFilesActivity;
import com.tuixin11sms.tx.activity.SellActivity;
import com.tuixin11sms.tx.activity.SettingsPreference;
import com.tuixin11sms.tx.activity.SingleMsgRoom;
import com.tuixin11sms.tx.activity.explorer.FolderExplorerActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 设置
 * 
 * @author xch
 * 
 */
public class TabSetActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "TabSetActivity";

	public static final String LOGIN_TEST_SERVER = "login_test_server";// 登陆测试服务器，debug版本有此功能，debug关闭，此功能自动消失

	private TX myTx;// 个人信息
	private static final int FLUSH_ALBUM = 200;
	public static final int MAX_SIZE = 8;
	private LinearLayout userinfo;// 我的个人资料
	private LinearLayout updateSign;// 签名修改
	private LinearLayout rl_receiveFiles;// 收到的文件
	private LinearLayout updateMessageNotice;// 新消息通知
	private LinearLayout updateAssistFuction;// 辅助功能
	private LinearLayout updateBindManage;// 绑定管理
	private LinearLayout updatePassWord;// 密码修改

	private LinearLayout updateInvitedFriend;// 邀请好友
	private LinearLayout updateBlackManage;// 黑名单管理
	private LinearLayout updateRecommend;// 精品应用推荐
	private LinearLayout updateConactSlMan;// 联系神聊客服
	private LinearLayout updateHelp;// 帮助与反馈
	private LinearLayout updateAbout;// 关于
//	private RelativeLayout exitLinear;// 退出
	private ImageView mIsCommpete;
	private TextView albumText;
	
	private LinearLayout btn_back_left;

	// private SharedPreferences prefs;
	// private Editor editor;
	private SmileyParser sParser;// 解析表情
	// 头部个人资料
	private TextView mNickName;// 昵称
	private ImageView mUserinfoHead;// 头像修改
	private TextView mUserNum;// 神聊号
	private TextView mUserSign;// 个人签名
	private ImageView adminImage;// 是否是管理员
	private UpdateReceiver updatereceiver;
	private ProgressDialog pd;
//	private int deafaultHeadImage;
	private int isqut;
	private Toast exitToast;

	// 头像和相册图片的获取和处理的相关参数
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	public static final int GET_HEAD_IMG_FROM_CAMERA = 4;// 用相机拍照上传头像
	public static final int GET_HEAD_IMG_FROM_GALLERY = 5;// 从图库选择图片上传头像
	public static final int GET_ALBUM_IMG_FROM_CAMERA = 99;// 拍照上传相册图片
	public static final int GET_ALBUM_IMG_FROM_GALLERY = 103;// 从图片选择要上传的相册照片
	public static final int EDIT_HEAD_IMG = 7;// 用EditHeadIcon编辑过的头像
	public static final int EDIT_ALBUM_IMG = 102;// 用EditHeadIcon编辑相册图片

	// public static final int ACITIVIYT_ACITON_ALBUM_PIC_VIEW = 10;
	// public static final int ACTION_PICK = 6;
	// public static final int ACTION_ALBUM_PICK = 11;
	// public static final int ACTION_EDIT_ALBUM = 12;
	// public static final int AVTIVITY_ACTION_ALBUM_PHOTO_CAPTURE = 9;
	// public static final int PHOTOHRAPH = 100;// 拍照
	// public static final int PHOTOZOOM = 101; // 相册选择
	// public static final String IMAGE_UNSPECIFIED = "image/*";
	public ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
	private GridView albumGridView;// 相册
	private AlbumGridViewAdapter albumAdapter;
	private int imgPos;
	private AlbumItem aiAdd = new AlbumItem();
	private boolean isAdded;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		setContentView(R.layout.activity_tab_setting);
		TxData.addActivity(this);
		init();
		setData();
	}

	// 初始化view
	private void init() {
		adminImage = (ImageView) findViewById(R.id.admin_rel);
		updateConactSlMan = (LinearLayout) findViewById(R.id.sl_tab_setting_contact);
//		exitLinear = (RelativeLayout) findViewById(R.id.sl_tab_setting_exit);
		updateInvitedFriend = (LinearLayout) findViewById(R.id.sl_tab_setting_invited);
		updateAbout = (LinearLayout) findViewById(R.id.sl_tab_setting_about);
		updateHelp = (LinearLayout) findViewById(R.id.sl_tab_setting_help);
		updateRecommend = (LinearLayout) findViewById(R.id.sl_tab_setting_recommend);
		updateBlackManage = (LinearLayout) findViewById(R.id.sl_tab_setting_shield);
		updatePassWord = (LinearLayout) findViewById(R.id.sl_tab_setting_password);
		updateBindManage = (LinearLayout) findViewById(R.id.sl_tab_setting_binding);
		updateAssistFuction = (LinearLayout) findViewById(R.id.sl_tab_setting_assist);
		updateMessageNotice = (LinearLayout) findViewById(R.id.sl_tab_setting_state);
		userinfo = (LinearLayout) findViewById(R.id.userinfo_settings_userinfo);
		updateSign = (LinearLayout) findViewById(R.id.sl_tab_settting_sign);
		rl_receiveFiles = (LinearLayout) findViewById(R.id.rl_tab_my_receive_files);

		// View userInforHead = findViewById(R.id.userinfo_settings_userinfo);
		// mUserinfoHead = (ImageView)
		// userInforHead.findViewById(R.id.con_info_user_head);
		mUserinfoHead = (ImageView) findViewById(R.id.con_info_user_head);
		mNickName = (TextView) findViewById(R.id.con_info_user_name);
		mUserNum = (TextView) findViewById(R.id.con_info_tuixin_id_context);
		mUserSign = (TextView) findViewById(R.id.sl_tab_setting_inculde_signText);
		albumText = (TextView) findViewById(R.id.album_text);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		mIsCommpete = (ImageView) findViewById(R.id.con_info_complete);

		// 设置监听事件
		mUserinfoHead.setOnClickListener(this);
		userinfo.setOnClickListener(this);
		updateSign.setOnClickListener(this);
		rl_receiveFiles.setOnClickListener(this);
		updateMessageNotice.setOnClickListener(this);
		updateAssistFuction.setOnClickListener(this);
		updateBindManage.setOnClickListener(this);
		updatePassWord.setOnClickListener(this);
		updateBlackManage.setOnClickListener(this);
		updateRecommend.setOnClickListener(this);
		updateHelp.setOnClickListener(this);
		updateAbout.setOnClickListener(this);
		updateInvitedFriend.setOnClickListener(this);
//		exitLinear.setOnClickListener(this);
		updateConactSlMan.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);
		// prefs = getSharedPreferences(CommonData.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		sParser = Utils.getSmileyParser(this);
		myTx = TX.tm.getTxMe();
		if (Utils.debug) {
			Log.i(TAG, "个人相册地址为：" + myTx.avatar_url);
		}

		albumGridView = (GridView) findViewById(R.id.sl_tab_setting_userinfo_gridView);
		albumAdapter = new AlbumGridViewAdapter(this, albumText);
		albumGridView.setAdapter(albumAdapter);
		albumAdapter.setList(list);
		albumAdapter.notifyDataSetChanged();
		albumGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int pos,
					long rowId) {
				imgPos = pos;
				AlbumItem ai = list.get(pos);
				if (ai.isAdd()) {
					// 该位置没有照片，填出菜单添加照片
					showAlbumAddMenu();
				} else {
					// 测试相册大图显示
					// Intent intent = new Intent(TabSetActivity.this,
					// UserAlbumActivity.class);
					Intent intent = new Intent(TabSetActivity.this,
							GalleryUrlActivity.class);
					intent.putExtra(GalleryUrlActivity.ALBUM_LIST, list);
					intent.putExtra(GalleryUrlActivity.POSITION, pos);
					intent.putExtra(GalleryUrlActivity.UID, TX.tm.getUserID());
					startActivity(intent);
				}

			}
		});
		albumGridView.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int pos, long id) {
				imgPos = pos;
				AlbumItem ai = list.get(pos);
				if (!ai.isAdd()) {
					showAlbumItemMenu();
				}
				return true;
			}
		});

	}

	private void deleteFolder(File file) {
		if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					if (pathname.isFile()) {
						pathname.delete();
					} else if (pathname.isDirectory()) {
						deleteFolder(pathname);
					}
					return false;
				}
			});
		}

	}

	private void copyFolder(File file, final File destFolder) {
		if (file.isFile()) {
			copyFile(file, destFolder);
		} else if (file.isDirectory()) {
			file.listFiles(new FileFilter() {

				@Override
				public boolean accept(File pathname) {
					copyFolder(pathname, destFolder);
					return false;
				}
			});
		}

	}

	// 拷贝data/data文件夹下面的所有文件
	private void copyFile(File file, File destFolder) {

		String fileName = file.getName();

		File destFile = new File(destFolder, fileName);
		FileInputStream fis = null;
		FileOutputStream fos = null;
		try {
			if (!destFile.getParentFile().exists()) {
				destFile.getParentFile().mkdirs();
			}
			destFile.createNewFile();
			fis = new FileInputStream(file);
			fos = new FileOutputStream(destFile);
			int length = 0;
			byte[] buffer = new byte[2048];
			while ((length = fis.read(buffer)) != -1) {
				fos.write(buffer, 0, length);
			}
			fis.close();
			fos.close();

		} catch (IOException e) {
			Log.e(TAG, "创建数据库文件异常", e);
			showToast("数据库拷贝出错异常了！！！");
		}
	}

	/** 已经存在的相册图片的长按事件菜单显示和处理 */
	protected void showAlbumItemMenu() {
		new AlertDialog.Builder(this)
				.setTitle("更多选项")
				.setItems(R.array.album_item_menu,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									// 从相册替换已经存在的相册照片
									String storagePath = Utils
											.getStoragePath(TabSetActivity.this);
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
									// 重新拍照替换已经存在的照片
									if (!Utils.checkSDCard()) {
										if (Utils.debug)
											Log.i(TAG, "无SD卡");
										return;
									}
									String storagePath = Utils
											.getStoragePath(TabSetActivity.this);
									if (Utils.isNull(storagePath)) {
										return;
									}
									File sddir = new File(storagePath);
									if (!sddir.exists() && !sddir.mkdirs()) {
										if (Utils.debug)
											Log.e(TAG,
													"bitmapFromUrl---Create dir failed");
									}
									StringBuffer tempPath = new StringBuffer()
											.append(storagePath)
											.append("/self").append(".jpg");
									Intent it = new Intent(
											"android.media.action.IMAGE_CAPTURE");
									if (Utils.debug)
										Log.i(TAG, "+++++++++++++++++++++"
												+ tempPath.toString());
									it.putExtra(MediaStore.EXTRA_OUTPUT, Uri
											.fromFile(new File(tempPath
													.toString())));
									it.putExtra(Utils.IMAGE_CAMRA,
											tempPath.toString());
									startActivityForResult(it,
											GET_ALBUM_IMG_FROM_CAMERA);
									// Utils.inPhoto = true;

								} else if (which == 2) {
									// 删除已经存在的这个相册照片
									list.remove(imgPos);
									addAlbumAddItem();
									updateAlbumUrl(imgPos, null);
									albumAdapter.setList(list);
									albumAdapter.notifyDataSetChanged();
								}
							}
						}).show();
	}

	private void showProgressDialog() {
		if (pd == null) {
			pd = new ProgressDialog(this);
			pd.setMessage("uploading....");
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

	/** 更新相册url */
	private void updateAlbumUrl(int pos, String url) {
		cancelProgressDialog();
		if (url != null) {
			for (int i = 0; i < list.size(); i++) {
				if (pos == i) {
					list.get(pos).setUrl(url);
					// 这里不应该设置为空，否则会出现上传成功后相册缩略图闪动的问题，
					// 因为原来有缩略图，上传成功后把bitmap设置为null,notifyDataSetChanged后会重新读取list,
					// 这时判定为bitmap为null，会再去服务器下载缩略图显示
					// list.get(pos).setBitmap(null);
				}
			}
			handler.sendEmptyMessage(FLUSH_ALBUM);
		}
		List<String> albumUrls = new ArrayList<String>();
		for (AlbumItem ai : list) {
			if (!Utils.isNull(ai.getUrl()))
				albumUrls.add(ai.getUrl());
		}
		// getEditorMeme().putString(CommonData.ALBUM,
		// StringUtils.list2String(albumUrls)).commit();
		mSess.mPrefMeme.album.setVal(StringUtils.list2String(albumUrls))
				.commit();
		TX.tm.reloadTXMe();// //////
		mSess.getSocketHelper().setAlbum(albumUrls);
	}

	private void addAlbumAddItem() {
		// if (!isAdded) {
		// aiAdd.setAdd(true);
		// list.add(aiAdd);
		// isAdded = true;
		// }
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).isAdd()) {
				break;
			}
			if (i == list.size() - 1) {
				aiAdd.setAdd(true);
				list.add(aiAdd);
			}
		}
	}

	// 添加相册图片的点击事件处理
	protected void showAlbumAddMenu() {
		new AlertDialog.Builder(this)
				.setTitle("更多选项")
				.setItems(R.array.album_add_menu,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								if (which == 0) {
									// 从手机图库选择要上传的相册照片
									String storagePath = Utils
											.getStoragePath(TabSetActivity.this);
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
											.getStoragePath(TabSetActivity.this);
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
								}
							}
						}).show();
	}

	// 设置数据
	private void setData() {

		// int isex = getPrefsMeme().getInt(CommonData.SEX, -1);
		int isex = mSess.mPrefMeme.sex.getVal();
		if (isex == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			defaultHeaderImg = defaultHeaderImgFemale;
		}

		mUserinfoHead.setTag(TX.tm.getUserID());
		mUserinfoHead.setImageResource(defaultHeaderImg);

		// ImageLoader.getInstance(txdata).loadImage(txdata.getUserID(),
		// myTx.avatar_url, mUserinfoHead,
		// deafaultHeadImage);

		if (Utils.isIdValid(myTx.partner_id)) {
			mUserNum.setText(Long.toString(myTx.partner_id));
			mUserNum.setVisibility(View.VISIBLE);
		} else {
			mUserNum.setVisibility(View.INVISIBLE);
		}
		if (!Utils.isNull(myTx.getNick_name())) {
			mNickName.setText(sParser.addSmileySpans(myTx.getNick_name(), true,
					0));

		} else {
			// String tempname = getPrefsMeme().getString(CommonData.INPUTNAME,
			// "");
			// mNickName.setText(tempname);
			// 没有昵称是不是应该直接显示神聊号啊？ 2013.12.19
			mNickName.setText("" + myTx.partner_id);

		}

		if (!Utils.isNull(myTx.sign)) {
			mUserSign.setText(sParser.addSmileySpans(myTx.sign, true, 0));
		} else {
			mUserSign.setText("");
		}
		if (TxData.isOP()) {
			adminImage.setVisibility(View.VISIBLE);
		} else {
			adminImage.setVisibility(View.GONE);
		}
		list = TX.tm.getTxMe().getAlbum();
		albumAdapter.setList(list);
		albumAdapter.notifyDataSetChanged();
	}

	// 点击事件
	@Override
	public void onClick(View v) {
		switch (v.getId()) {

		// 个人资料
		case R.id.userinfo_settings_userinfo:
			Intent intent = new Intent(TabSetActivity.this,
					SetUserInfoActivity.class);
			startActivity(intent);
			break;
		// 头像设置
		case R.id.con_info_user_head:
			final AlertDialog.Builder headDialog = new AlertDialog.Builder(
					TabSetActivity.this);
			headDialog.setTitle("更多选项");
			headDialog.setItems(R.array.album_add_menu,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								// 从手机图库中选择头像照片
								String storagePath = Utils
										.getStoragePath(TabSetActivity.this);
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
										.getStoragePath(TabSetActivity.this);
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
		// 修改签名
		case R.id.sl_tab_settting_sign:
			Intent intentSign = new Intent(TabSetActivity.this,
					SetUpdateSignActivity.class);
			startActivity(intentSign);
			break;
		// 查看我收到的文件
		case R.id.rl_tab_my_receive_files:
			Intent intentReceiveFiles = new Intent(thisContext,
					ReceivedFilesActivity.class);
			intentReceiveFiles.putExtra(FolderExplorerActivity.IS_SEND_FILES,
					false);// 查看目录文件而不是发送
			startActivity(intentReceiveFiles);
			break;
		// 新消息通知
		case R.id.sl_tab_setting_state:
			Intent intentMessage = new Intent(TabSetActivity.this,
					SettingsPreference.class);
			startActivity(intentMessage);
			break;
		// 辅助功能
		case R.id.sl_tab_setting_assist:
			Intent intentAssistFunction = new Intent(TabSetActivity.this,
					SetAssistFunctionActivity.class);
			startActivity(intentAssistFunction);
			break;
		// 返回按钮
		case R.id.btn_back_left:
			finish();
			break;
		// 绑定管理
		case R.id.sl_tab_setting_binding:
			Intent intentBind = new Intent(TabSetActivity.this,
					SetBindManageActivity.class);
			startActivity(intentBind);
			break;
		// 密码修改
		case R.id.sl_tab_setting_password:
			Intent intentPassWord = null;
			String ppwd = null;
			try {
				// ppwd = mSess.getPwd(myTx.partner_id);
				ppwd = mSess.mUserLoginInfor.getCurrentPwd();
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "获取旧密码异常", e);
			}
			if (!Utils.isNull(ppwd)) {
				intentPassWord = new Intent(TabSetActivity.this,
						SetUpdatePassWordActivity.class);
			} else {
				intentPassWord = new Intent(TabSetActivity.this,
						SetUpdateOtherPassWordActivity.class);
			}

			startActivity(intentPassWord);
			break;
		// 黑名单管理
		case R.id.sl_tab_setting_shield:
			Intent intentBlack = new Intent(TabSetActivity.this,
					SetBlackManageActivity.class);
			startActivity(intentBlack);
			break;
		// 邀请朋友使用神聊
		case R.id.sl_tab_setting_invited:
			Intent intentInvited = new Intent(TabSetActivity.this,
					SetUpdateInviteFriendActivity.class);
			startActivity(intentInvited);
			break;

		// 精品应用推荐
		case R.id.sl_tab_setting_recommend:
			Intent iSell = new Intent(TabSetActivity.this, SellActivity.class);
			startActivity(iSell);
			break;
		// 联系神聊客服
		case R.id.sl_tab_setting_contact:
			Intent nextintent = new Intent(TabSetActivity.this,
					SingleMsgRoom.class);
			nextintent.putExtra(Utils.MSGROOM_TX, TX.TUIXIN_MAN);
			// nextintent.putExtra("threadId", threadid);
			startActivity(nextintent);

			break;
		// 帮助与反馈
		case R.id.sl_tab_setting_help:
			Intent helpIntent = new Intent(TabSetActivity.this,
					HelpActivity.class);
			startActivity(helpIntent);
			break;
		// 关于
		case R.id.sl_tab_setting_about:
			Intent iAbout = new Intent(TabSetActivity.this, AboutActivity.class);
			startActivity(iAbout);
			break;
		default:
			break;
		}

	}

	// 照相，相册返回结果
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (resultCode == RESULT_OK) {
			if (requestCode == GET_HEAD_IMG_FROM_GALLERY) {
				// 从相册修改头像的返回
				// Utils.inPhoto = false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(TabSetActivity.this,
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
					startActivityForResult(i, EDIT_HEAD_IMG);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "异常", e);
				}
			} else if (requestCode == EDIT_HEAD_IMG) {
				// 编辑头像返回
				Bitmap tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
//				cachePartnerBitmap(TX.tm.getUserID(),
//						Utils.getRoundedCornerBitmap(tempimg));
				if (Utils.debug)
					Log.e(TAG, "已经更新了头像缓存，等待onResume时更新头像");

			} else if (requestCode == GET_HEAD_IMG_FROM_CAMERA) {
				// 从照相机获取头像照片
				// Utils.inPhoto = false;
				try {
					String storagePath = Utils
							.getStoragePath(TabSetActivity.this);
					// long user_id = Long.parseLong(getPrefsMeme().getString(
					// CommonData.USER_ID, "-1"));
					long user_id = Long.parseLong(mSess.mPrefMeme.user_id
							.getVal());
					StringBuffer tempPath = new StringBuffer()
							.append(storagePath).append("/").append(user_id)
							.append(".jpg");
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
					i.putExtra(EditHeadIcon.STATE_COME,
							EditHeadIcon.FROM_CAMERA);
					startActivityForResult(i, EDIT_HEAD_IMG);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "处理从照相机获取头像照片异常", e);
				}
			} else if (requestCode == GET_ALBUM_IMG_FROM_GALLERY
					&& data != null) {
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

					final String tempImgPath = Utils.getStoragePath(mSess.getContext())
							+ File.separator + Utils.IMAGE_PATH
							+ File.separator + imgPos + ".jpg";

					try {
						// 生成合成的文件
						Utils.getAlbumFile(tempImgPath, Utils.tempBm);
						if (Utils.debug) {
							Log.i(TAG, "创建需要上传相册图片成功");
						}

						mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
								new DownUploadListner() {

									@Override
									public void onFinish(FileTaskInfo taskInfo) {
										String fileName = new MD5()
												.getMD5ofStr(taskInfo.mServerHost
														+ ":"
														+ taskInfo.mPath
														+ ":" + taskInfo.mTime);

										try {
											Utils.creatAlbumFile(tempImgPath,
													sddir, fileName);
											if (Utils.debug) {
												Log.i(TAG, "创建相册图片的大小图成功");
											}
										} catch (IOException e) {
											if (Utils.debug) {
												Log.e(TAG, "创建相册图片异常", e);
											}
										}

										updateAlbumUrl((int) imgPos,
												taskInfo.mServerHost + ":"
														+ taskInfo.mPath + ":"
														+ taskInfo.mTime);

									}
								}, null);

						showProgressDialog();
						AlbumItem ai = new AlbumItem();
						Bitmap smallDstBitmap = Utils.ResizeBitmap(
								Utils.ImageCrop(Utils.tempBm), 92);
						ai.setBitmap(smallDstBitmap);
						// isAdd属性好像是可以是否已被添加，true为可以被添加
						if (!list.get(imgPos).isAdd()) {
							list.remove(imgPos);
						}
						list.add(imgPos, ai);
						if (list.size() > MAX_SIZE) {
							list.remove(MAX_SIZE);
							isAdded = !isAdded;
						}
						albumAdapter.setList(list);
						albumAdapter.notifyDataSetChanged();

					} catch (Exception e) {
						if (Utils.debug) {
							Log.w(TAG, "合成大小图文件异常", e);
						}
						showToast("上传相册失败");

					}

				}

			}
		}

		// 拍照
		// if (requestCode == GET_ALBUM_IMG_FROM_CAMERA) {
		// //拍照上传相册图片
		// Utils.inPhoto = false;
		// try {
		// String storagePath = Utils.getStoragePath(this);
		// String imagePath = storagePath+"/self.jpg";
		// Bitmap tempimg = Utils.fitSizeAutoImg(imagePath,
		// Utils.msgroom_list_resolution);
		// if (tempimg == null && data != null) {
		// Bundle extras = data.getExtras();
		// if (extras != null) {
		// tempimg = (Bitmap) extras.get("data");
		// if (Utils.createPhotoFile(tempimg, "self.jpg") == null) {
		// return;
		// }
		// } else {
		// String path = data.getDataString();
		// if (!Utils.isNull(path)) {
		// Uri uri = Uri.parse(path);
		// imagePath = getRealPathFromURI(uri);
		// }
		// }
		// }
		// if (new File(imagePath).exists()) {
		// Intent i = new Intent(this, EditHeadIcon.class);
		// i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
		// if (Utils.debug) {
		// Log.i(TAG, "照片地址imagePath:" + imagePath);
		// }
		// i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.PHOTO_COME);
		// i.putExtra("isAlbum", true);
		// startActivityForResult(i, EDIT_ALBUM_IMG);
		// }
		// } catch (Exception e) {
		// if (Utils.debug)
		// Log.e(TAG,"异常", e);
		// }
		// }

		// if (requestCode == GET_ALBUM_IMG_FROM_GALLERY && data != null) {
		// //从图片选择要上传的相册照片
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
		// Intent i = new Intent(this, EditHeadIcon.class);
		// i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
		// if (Utils.debug) {
		// Log.i(TAG, "相册图片地址imagePath:" + imagePath);
		// }
		// i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.PIC_COME);
		// i.putExtra("isAlbum", true);
		// startActivityForResult(i, EDIT_ALBUM_IMG);
		// }
		// if (requestCode == EDIT_ALBUM_IMG) {
		// //用EditHeadIcon编辑过的相册图片返回
		// if (Utils.tempBm != null) {
		// String storagePath = Utils.getStoragePath(this);
		// final File sddir = new File(storagePath, Utils.IMAGE_PATH);
		// if (!sddir.exists() && !sddir.mkdirs()) {
		// if (Utils.debug)
		// Log.e(TAG,"bitmapFromUrl---Create dir failed");
		// sddir.mkdir();
		// }
		//
		//
		// final String tempImgPath = Utils.getStoragePath(txdata)
		// + File.separator + Utils.IMAGE_PATH + File.separator
		// + imgPos + ".jpg";
		//
		// try {
		// // 生成合成的文件
		// Utils.getAlbumFile(tempImgPath, Utils.tempBm);
		// if (Utils.debug) {
		// Log.i(TAG, "创建需要上传相册图片成功");
		// }
		//
		// mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
		// new DownUploadListner() {
		//
		// @Override
		// public void onFinish(FileTaskInfo taskInfo) {
		// String fileName = new MD5()
		// .getMD5ofStr(taskInfo.mServerHost + ":"
		// + taskInfo.mPath + ":"
		// + taskInfo.mTime);
		//
		// try {
		// Utils.creatAlbumFile(tempImgPath, sddir, fileName);
		// if (Utils.debug) {
		// Log.i(TAG, "创建相册图片的大小图成功");
		// }
		// } catch (IOException e) {
		// if (Utils.debug) {
		// Log.e(TAG, "创建相册图片异常",e);
		// }
		// }
		//
		// updateAlbumUrl((int) imgPos,
		// taskInfo.mServerHost + ":"
		// + taskInfo.mPath + ":"
		// + taskInfo.mTime);
		//
		// }
		// }, null);
		//
		// showProgressDialog();
		// AlbumItem ai = new AlbumItem();
		// Bitmap smallDstBitmap = Utils.ResizeBitmap(
		// Utils.ImageCrop(Utils.tempBm), 92);
		// ai.setBitmap(smallDstBitmap);
		// // isAdd属性好像是可以是否已被添加，true为可以被添加
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
		//
		//
		// } catch (Exception e) {
		// if (Utils.debug) {
		// Log.w(TAG, "合成大小图文件异常", e);
		// }
		// showToast("上传相册失败");
		//
		// }
		//
		// }
		//
		// }

		// switch (requestCode) {
		// case GET_HEAD_IMG_FROM_GALLERY:
		// //从相册修改头像的返回
		// if (resultCode == RESULT_OK) {
		// Utils.inPhoto = false;
		// Uri uri = data.getData();
		// ContentResolver cr = this.getContentResolver();
		// Cursor cursor = cr.query(uri, null, null, null, null);
		// if (cursor == null
		// || (cursor != null && cursor.getCount() <= 0)) {
		// Utils.startPromptDialog(TabSetActivity.this,
		// R.string.prompt,
		// R.string.userinfo_upload_failed_unknow);
		// return;
		// }
		// cursor.moveToFirst();
		// String imagePath = cursor.getString(1);
		// cursor.close();
		// try {
		//
		// Intent i = new Intent(this, EditHeadIcon.class);
		// i.putExtra(EditHeadIcon.GET_IMG_PATH, imagePath);
		// i.putExtra(EditHeadIcon.STATE_COME, EditHeadIcon.PIC_COME);
		// startActivityForResult(i, EDIT_HEAD_IMG);
		// } catch (Exception e) {
		// if (Utils.debug)
		// Log.e(TAG,"异常", e);
		// }
		// }
		// break;
		// case EDIT_HEAD_IMG:
		// if (resultCode == Activity.RESULT_OK) {
		// Bitmap tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
		// cachePartnerBitmap(TX.tm.getUserID(), Utils
		// .getRoundedCornerBitmap(tempimg));
		// if(Utils.debug)Log.e(TAG, "已经更新了头像缓存，等待onResum时更新头像");
		// }
		//
		// break;
		// case GET_HEAD_IMG_FROM_CAMERA:
		// if (resultCode == RESULT_OK) {
		//
		// Utils.inPhoto = false;
		//
		// try {
		// String storagePath = Utils
		// .getStoragePath(TabSetActivity.this);
		// long user_id = Long.parseLong(getPrefsMeme().getString(
		// CommonData.USER_ID, "-1"));
		// StringBuffer tempPath = new StringBuffer()
		// .append(storagePath).append("/").append(user_id)
		// .append(".jpg");
		// File file = new File(tempPath.toString());
		// String imagePath = tempPath.toString();
		// Bitmap tempimg = Utils.fitSizeAutoImg(imagePath,
		// Utils.msgroom_list_resolution);
		// if (tempimg == null && data != null) {
		// Bundle extras = data.getExtras();
		// if (extras != null) {
		// tempimg = (Bitmap) extras.get("data");
		// if (Utils.createPhotoFile(tempimg, "" + user_id
		// + ".jpg") == null) {
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
		// startActivityForResult(i, EDIT_HEAD_IMG);
		// } catch (Exception e) {
		// if (Utils.debug)
		// Log.e(TAG,"异常", e);
		// }
		// }
		// break;
		//
		// }
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

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_CHANGE_USERSIGN_RSP);
			filter.addAction(Constants.INTENT_ACTION_USERNAME_CHANGE_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERSIGN_RSP.equals(intent
					.getAction())) {
				handler.sendEmptyMessage(SetUpdateSignActivity.SIGN_CHANGE_SUCCESS);
			} else if (Constants.INTENT_ACTION_USERNAME_CHANGE_RSP
					.equals(intent.getAction())) {
				handler.sendEmptyMessage(SetUserInfoUpdateNameAcitivity.USERNAME_CHANGE_SUCCESS);
			}
		}

	}

	// Handler mAvatarHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case 1: {
	// CallInfo callinfo = (CallInfo) msg.obj;
	// callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
	// break;
	// }
	// }
	// };
	// };
	// HandlerThread mHandlerThread;
	// Handler mAsynloader;
	// LoginSessionManager mSess = LoginSessionManager
	// .getLoginSessionManager(this);
	//
	// void prepairAsyncload() {
	// mHandlerThread = new HandlerThread("Asynloader");
	// mHandlerThread.start();
	// mAsynloader = new Handler(mHandlerThread.getLooper()) {
	// @Override
	// public void handleMessage(Message msg) {
	// CallInfo ci;
	// switch (msg.what) {
	// case 1:
	// ci = (CallInfo) msg.obj;
	// if (ci.mUrl != null) {
	// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
	// ci.mUid, false);
	// if (file != null) {
	// File avatar = new File(file);
	// if (avatar.exists()) {
	// Bitmap bitmap = Utils.readBitMap(file, false);
	// if (bitmap != null) {
	// // ci.mBitmap = cacheBitmap(ci.mUid,
	// // bitmap);
	// ci.mBitmap = Utils
	// .getRoundedCornerBitmap(bitmap);
	// mAvatarHandler.obtainMessage(1, ci)
	// .sendToTarget();
	// break;
	// }
	// }
	// }
	// }
	// mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, false,
	// true, new FileTransfer.DownUploadListner() {
	// // 此处要记录文件名，同时要加载
	// @Override
	// public void onStart(FileTaskInfo taskInfo) {
	// }
	//
	// @Override
	// public void onProgress(FileTaskInfo taskInfo,
	// int curlen, int totallen) {
	// }
	//
	// @Override
	// public void onFinish(FileTaskInfo taskInfo) {
	// Bitmap bitmap = Utils.readBitMap(
	// taskInfo.mFullName, false);
	// if (bitmap != null) {
	// CallInfo ci = (CallInfo) taskInfo.mObj;
	// // ci.mBitmap = cacheBitmap(
	// // taskInfo.mSrcId, bitmap);
	// ci.mBitmap = Utils
	// .getRoundedCornerBitmap(bitmap);
	// mAvatarHandler.obtainMessage(1,
	// taskInfo.mObj).sendToTarget();
	// }
	// }
	//
	// @Override
	// public void onError(FileTaskInfo taskInfo,
	// int iCode, Object iPara) {
	// }
	// }, ci);
	// break;
	// }
	// }
	// };
	// }
	//
	// void stopAsyncload() {
	// mHandlerThread.quit();
	// mHandlerThread = null;
	// mAsynloader = null;
	// }
	//
	// protected void loadHeadImg(String avatar_url, long partner_id,
	// AsyncCallback<Bitmap> callback) {
	// CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
	// mAsynloader.obtainMessage(1, callinfo).sendToTarget();
	// }

	@Override
	protected void onResume() {
		updateTabInfoIcon();// 检测资料是否完整
		// int isex = getPrefsMeme().getInt(CommonData.SEX, -1);
		int isex = mSess.mPrefMeme.sex.getVal();
		if (isex == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			defaultHeaderImg = defaultHeaderImgFemale;
		}


		// ImageLoader.getInstance(txdata).loadImage(txdata.getUserID(),
		// myTx.avatar_url, mUserinfoHead,
		// deafaultHeadImage);
		registReceiver();
		super.onResume();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}

	// 更新viewhandler
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case SetUpdateSignActivity.SIGN_CHANGE_SUCCESS:
				// mUserSign.setText(sParser.addSmileySpans(
				// getPrefsMeme().getString(CommonData.SIGN, ""), true, 0));
				mUserSign.setText(sParser.addSmileySpans(
						mSess.mPrefMeme.sign.getVal(), true, 0));

				break;
			case SetUserInfoUpdateNameAcitivity.USERNAME_CHANGE_SUCCESS:
				// mNickName.setText(sParser.addSmileySpans(
				// getPrefsMeme().getString(CommonData.NICKNAME, ""), true, 0));
				mNickName.setText(sParser.addSmileySpans(
						mSess.mPrefMeme.nickname.getVal(), true, 0));
				break;

			case FLUSH_ALBUM:
				albumAdapter.notifyDataSetChanged();
				break;
			}
			super.handleMessage(msg);
		}
	};

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater mInflater = getMenuInflater();
		mInflater.inflate(R.menu.contacts_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
		super.onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int id = item.getItemId();
		switch (id) {
		case R.id.texit_menu:
			// Utils.exitProcessLogic(txdata,this,getEditor());
			GroupUtils.showDialog(TabSetActivity.this, R.string.logout_prompt,
					R.string.dialog_okbtn, R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.exitProcessLogic(TabSetActivity.this);
						}
					});

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// if (isqut == 0) {
			// isqut = 1;
			// exitToast = new Toast(getApplicationContext());
			// LayoutInflater mInflater = LayoutInflater
			// .from(getApplicationContext());
			// View toastView = mInflater.inflate(R.layout.message_exit_toast,
			// null);
			// TextView exitText = (TextView) toastView
			// .findViewById(R.id.message_exit_text);
			// exitText.setText(R.string.message_exit_str);
			// exitToast.setDuration(Toast.LENGTH_SHORT);
			// exitToast.setView(toastView);
			// exitToast.show();
			// new Timer().schedule(new TimerTask() {
			// public void run() {
			// isqut = 0;
			// }
			// }, 2000);
			// } else if (isqut == 1) {
			// isqut = 0;
			// if (exitToast != null)
			// exitToast.cancel();
			// TxData.finishAll();
			//
			// }
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 更新设置标题栏，是否资料完整显示气泡
	 * 
	 * @param tabHost
	 */
	public void updateTabInfoIcon() {
		boolean isCompete = Utils.ismUserInfoComplete();
		if (!isCompete) {
			mIsCommpete.setVisibility(View.VISIBLE);
		} else {
			mIsCommpete.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	};

}
