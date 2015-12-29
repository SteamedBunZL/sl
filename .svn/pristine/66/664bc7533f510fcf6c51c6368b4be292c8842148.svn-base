package com.shenliao.group.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
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
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.set.activity.SetGroupNewInfonActivity;
import com.shenliao.set.activity.SetGroupNewNameAcitivity;
import com.shenliao.set.activity.SetGroupNewNoticeActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditHeadIcon;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 编辑聊天室资料，（群主在查看聊天室资料页面，点击“编辑”后跳转到的页面）
 */
public class GroupEdit extends BaseActivity implements OnClickListener {
	protected static final String TAG = "GroupEdit";
	private TxGroup txGroup;
	private TextView name;
	private TextView notice;
	private TextView introl; // 简介
	private ImageView groupIcon;
	private TextView updateHead;
	private Button saveBtnButton;
	private LinearLayout screen;

	private Bitmap bm;

	private static final int GROUP_MODIFY_SUCCESS = 100000;
	private static final int GROUP_MODIFY_NAME_FAILED = 100001;
	private static final int GROUP_MODIFY_INFO_FAILED = 100002;
	private static final int GROUP_MODIFY_NOTICE_FAILED = 100003;
	private static final int GROUP_OPT_FAILED = 100004;

	public static final String TEMP = "_temp";// 修改群头像时，新头像名字的临时后缀，为了区分新老头像，因为生成新头像后，用户不一定确定保存

	private UpdateReceiver updatareceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_group_edit);
		init();
	}

	private boolean firstTime = true;
	private SmileyParser smileyParser;

	public void init() {
		Intent intent = getIntent();

		notice = (TextView) findViewById(R.id.edit_group_notice_box); // 公告
		name = (TextView) findViewById(R.id.edit_group_input_box); // 名称
		introl = (TextView) findViewById(R.id.create_group_introl); // 简介

		head_ll = (LinearLayout) findViewById(R.id.sl_tab_setting_include_image_ll); // 头像
		name_ll = (LinearLayout) findViewById(R.id.edit_group_input_box_ll); // 名称
		notice_ll = (LinearLayout) findViewById(R.id.edit_group_notice_box_ll);
		introl_ll = (LinearLayout) findViewById(R.id.create_group_introl_ll);
		btn_back_edit = (LinearLayout) findViewById(R.id.btn_back_edit);

		btn_back_edit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupEdit.this.finish();
			}
		});

		saveBtnButton = (Button) findViewById(R.id.group_edit_ok_btn);
		groupIcon = (ImageView) findViewById(R.id.group_edit_head);
		screen = (LinearLayout) findViewById(R.id.group_edit_screen);

		screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) GroupEdit.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
		saveBtnButton.setOnClickListener(this);
		smileyParser = new SmileyParser(this);
		groupIcon.setOnClickListener(this);
		head_ll.setOnClickListener(this);
		name_ll.setOnClickListener(this);
		notice_ll.setOnClickListener(this);
		introl_ll.setOnClickListener(this);

		if (intent != null) {
			txGroup = intent.getParcelableExtra(Utils.MSGROOM_TX_GROUP);
			if (txGroup != null) {

				// if(firstTime){
				// Log.i("yyyyyyyyyy","yyyyyyyyyyyyyyyyy");
				setEditData();
				// firstTime = false;
				// }
			}
		}

		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();

	}

	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	public static final int ACTIVITY_ACTION_PIC_VIEW = 5;
	// private SharedPreferences prefs;
	// private Editor editor;
	public static final int ACTIVITY_ACTION_PHOTO_CAPTURE = 4;

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_include_image_ll:
			final AlertDialog.Builder headDialog = new AlertDialog.Builder(
					GroupEdit.this);
			headDialog.setTitle("更多选项");
			headDialog.setItems(R.array.msgroom_pic,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								String storagePath = Utils
										.getStoragePath(GroupEdit.this);
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
									if (Utils.debug) {
										Log.i(TAG, "无SD卡");
									}
									return;
								}
								String storagePath = Utils
										.getStoragePath(GroupEdit.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								File sddir = new File(storagePath);
								if (!sddir.exists() && !sddir.mkdirs()) {
									if (Utils.debug)
										Log.e(TAG,
												"bitmapFromUrl---Create dir failed");
								}
								// long user_id =
								// Long.parseLong(prefs.getString(
								// CommonData.USER_ID, "-1"));
								long user_id = Long
										.parseLong(mSess.mPrefMeme.user_id
												.getVal());
								StringBuffer tempPath = new StringBuffer()
										.append(storagePath).append("/")
										.append(user_id).append(".jpg");
								Intent it = new Intent(
										"android.media.action.IMAGE_CAPTURE");
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
		case R.id.edit_group_input_box_ll:
			Intent intentGroupname = new Intent(GroupEdit.this,
					SetGroupNewNameAcitivity.class);
			intentGroupname.putExtra("name", name.getText().toString());
			startActivityForResult(intentGroupname,
					REQUESTCODE_FOR_REQUSET_GROUPNAME);

			break;
		case R.id.create_group_introl_ll:

			Intent intentGroupinfo = new Intent(GroupEdit.this,
					SetGroupNewInfonActivity.class);
			intentGroupinfo.putExtra("info", introl.getText().toString());
			startActivityForResult(intentGroupinfo,
					REQUESTCODE_FOR_REQUSET_GROUPINFO);

			break;
		case R.id.edit_group_notice_box_ll:

			Intent intentGroupinfo1 = new Intent(GroupEdit.this,
					SetGroupNewNoticeActivity.class);
			intentGroupinfo1.putExtra("gonggao", notice.getText().toString());
			startActivityForResult(intentGroupinfo1,
					REQUESTCODE_FOR_REQUSET_GROUPNOTICE);

			break;

		case R.id.group_edit_ok_btn:

			changeGroupMsg();

			break;
		default:
			break;
		}
	}

	public static final int REQUESTCODE_FOR_REQUSET_GROUPNAME = 11;// 昵称请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPNAME = 12;// 昵称返回请求码
	public static final int REQUESTCODE_FOR_REQUSET_GROUPINFO = 13;// 简介请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPINFO = 14;// 简介返回请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPNOTICE = 15;// gonggao请求码
	public static final int REQUESTCODE_FOR_REQUSET_GROUPNOTICE = 16;// gonggao返回请求码
	public static final int ACTION_EDIT_PICK = 7;
	private String avatorPath;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {

		case REQUESTCODE_FOR_REQUSET_GROUPNAME:
			if (resultCode == RESULTCODE_FOR_RESULT_GROUPNAME) {
				String newname = data.getExtras().getString("newname");
				name.setText(newname);
			}
			break;
		case REQUESTCODE_FOR_REQUSET_GROUPINFO:
			if (resultCode == RESULTCODE_FOR_RESULT_GROUPINFO) {
				String newinfo = data.getExtras().getString("newinfo");
				introl.setText(newinfo);
			}
			break;
		case REQUESTCODE_FOR_REQUSET_GROUPNOTICE:
			if (resultCode == RESULTCODE_FOR_RESULT_GROUPNOTICE) {
				String newinfo = data.getExtras().getString("newgonggao");
				notice.setText(newinfo);
			}
			break;

		case ACTIVITY_ACTION_PIC_VIEW:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto = false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(GroupEdit.this, R.string.prompt,
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
					i.putExtra(EditHeadIcon.FROM_GROUP, "change_group_icon");
					i.putExtra(EditHeadIcon.GROUP_ID, "group_"
							+ txGroup.group_id);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "异常", e);
				}
			}
			break;
		case ACTION_EDIT_PICK:
			if (resultCode == Activity.RESULT_OK) {
				Bitmap tempimg = null;
				tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
				avatorPath = data.getStringExtra(EditHeadIcon.GROUP_ICON_PATH);
				// groupIcon.setBackgroundDrawable(new
				// BitmapDrawable(Utils.getRoundedCornerBitmap(tempimg)));
				// groupIcon.setImageBitmap(Utils.getRoundedCornerBitmap(tempimg));

				bm = Utils.getRoundedCornerBitmap(tempimg);

				groupIcon.setImageBitmap(bm);
				// groupIcon.setImageResource(R.drawable.icon);
			}
			break;
		case ACTIVITY_ACTION_PHOTO_CAPTURE:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto = false;
				try {
					String storagePath = Utils.getStoragePath(GroupEdit.this);
					// long user_id = Long.parseLong(prefs.getString(
					// CommonData.USER_ID, "-1"));
					long user_id = Long.parseLong(mSess.mPrefMeme.user_id
							.getVal());
					;
					StringBuffer tempPath = new StringBuffer()
							.append(storagePath).append("/").append(user_id)
							.append(".jpg");
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
					i.putExtra(EditHeadIcon.FROM_GROUP, "change_group_icon");
					i.putExtra(EditHeadIcon.GROUP_ID, "group_"
							+ txGroup.group_id);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e("Exception", e.getMessage(), e);
				}
				break;
			}
			break;

		default:
			break;
		}
	}

	public String getRealPathFromURI(Uri contentUri) {

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

	private void setEditData() {

		notice.setText(smileyParser.addSmileySpans(txGroup.group_bulletin,
				true, 0));
		name.setText(smileyParser.addSmileySpans(txGroup.group_title, true, 0));
		introl.setText(smileyParser.addSmileySpans(txGroup.group_sign, true, 0));
		// groupType(txGroup.group_type_channel);

		groupIcon.setTag("group_" + txGroup.group_id);

		// Bitmap bm = AsyncImageLoader.getInstance(txdata).loadGroupImage(
		// txGroup.group_avatar, txGroup.group_id, avatarCallback);
		bm = getGroupCachedBitmap(txGroup.group_id);
		if (bm != null) {
			groupIcon.setImageBitmap(bm);
		} else {
			loadGroupImg(txGroup.group_avatar, txGroup.group_id, avatarCallback);
			// if (bm == null) {
			groupIcon.setImageResource(R.drawable.qun_default);
			// } else {
			// groupIcon.setImageBitmap(Utils.getRoundedCornerBitmap(bm));
			// }
		}

	}

	/** 点击保存按钮，发送修改群资料请求 */
	private void changeGroupMsg() {

		showDialogTimer(GroupEdit.this, R.string.prompt,
				R.string.group_edit_save, 10 * 1000).show();
		mSess.getSocketHelper().sendChargeTitle(txGroup.group_id,
				name.getText().toString(), introl.getText().toString(),
				avatorPath, txGroup.group_type_channel,
				notice.getText().toString());

		cacheGroupBitmap(txGroup.group_id, bm);
		
		// 删除老群头像文件，修改新头像名字为群id
		String storagePath = Utils.getStoragePath(mSess.getContext());
		File sddir = new File(storagePath, Utils.AVATAR_PATH);

		String tempBasePath = new StringBuffer()
				.append(sddir.getAbsolutePath()).append("/").append("group_")
				.append(txGroup.group_id).toString();
		String oldSmallAvatarPath = tempBasePath.concat(".jpg");
		String smallTempPath = tempBasePath.concat(GroupEdit.TEMP).concat(
				".jpg");
		String oldBigAvatarPath = tempBasePath.concat("_big.jpg");
		String bigTempPath = tempBasePath.concat(GroupEdit.TEMP).concat(
				"_big.jpg");

		File smallFile = new File(oldSmallAvatarPath);
		File smallTempFile = new File(smallTempPath);
		File bigFile = new File(oldBigAvatarPath);
		File bigTempFile = new File(bigTempPath);
		if (smallFile.exists()) {
			smallFile.delete();
			smallTempFile.renameTo(smallFile);
		}

		if (bigFile.exists()) {
			bigFile.delete();
			bigTempFile.renameTo(bigFile);
		}

	}

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
		{
			// 如果临时大小图存在，则删除
			String storagePath = Utils.getStoragePath(mSess.getContext());
			File sddir = new File(storagePath, Utils.AVATAR_PATH);
			String tempBasePath = new StringBuffer()
					.append(sddir.getAbsolutePath()).append("/")
					.append("group_").append(txGroup.group_id).toString();
			String tempSmallPath = tempBasePath.concat(GroupEdit.TEMP).concat(
					".jpg");
			String tempBigPath = tempBasePath.concat(GroupEdit.TEMP).concat(
					"_big.jpg");

			File smallTempFile = new File(tempSmallPath);
			File bigTempFile = new File(tempBigPath);
			if (smallTempFile.exists()) {
				smallTempFile.delete();
			}

			if (bigTempFile.exists()) {
				bigTempFile.delete();
			}

		}

	}

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregistReceiver();
	}

	private void unregistReceiver() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_MODIFY_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();

			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_MODIFY_GROUP.equals(intent.getAction())) {
				dealModifyGroup(serverRsp);
			}
		}
	}

	public void dealModifyGroup(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK:
				if (Utils.debug)
					Log.i(TAG, "dealCreateGroup---dealCreateGroup:_0");
				// txGroup.findGroupById(GroupEdit.this, txGroup.group_id);
				txGroup.group_avatar = avatorPath;
				// txGroup.saveTxGroupToDB(txGroup, txdata);//用下面的方法 2014.01.23
				// shc

				ContentValues values = new ContentValues();
				values.put(TxDB.Qun.QU_AVATAR, txGroup.group_avatar);
				TxGroup.updateTxGroup(mSess.getContentResolver(), txGroup.group_id, values);

				handler.sendEmptyMessage(GROUP_MODIFY_SUCCESS);
				break;
			case GROUP_MEMBER_SIZE_INVALID:
				if (Utils.debug)
					Log.i(TAG, "dealCreateGroup---dealCreateGroup:_1");
				break;
			case GROUP_MEMBER_THAN_LIMIT:
				if (Utils.debug)
					Log.i(TAG, "dealCreateGroup---dealCreateGroup:_2");
				break;
			case OPT_FAILED:
				String fbret = serverRsp.getBundle().getString("fbret");
				Message msg = handler.obtainMessage(GROUP_OPT_FAILED, fbret);
				handler.sendMessage(msg);
				break;
			case GROUP_MODIFY_NAME_FAILED:
				handler.sendEmptyMessage(GROUP_MODIFY_NAME_FAILED);
				break;
			case GROUP_MODIFY_INTRO_FAILED:
				handler.sendEmptyMessage(GROUP_MODIFY_INFO_FAILED);
				break;
			case GROUP_MODIFY_BULLENTIN_FAILED:
				handler.sendEmptyMessage(GROUP_MODIFY_NOTICE_FAILED);

				break;
			}
		}
	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			bm = Utils.getRoundedCornerBitmap(result);
			groupIcon.setImageBitmap(bm);
		}
	};
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			int num = msg.what;
			switch (num) {
			case GROUP_MODIFY_SUCCESS:
				Toast.makeText(GroupEdit.this, "修改成功", Toast.LENGTH_SHORT)
						.show();
				Intent intent = new Intent();
				txGroup.group_bulletin = notice.getText().toString();
				txGroup.group_sign = introl.getText().toString();
				txGroup.group_title = name.getText().toString();
				TxData.txGroup = txGroup;
				// GroupMine.CHANGE = true;

				intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
				setResult(20, intent);
				TxData.finishOne(GroupEdit.class.getName());
				TxData.finishOne(GroupMsgRoom.class.getName());
				// TxData.finishOne(GroupInfo.class.getName());
				GroupEdit.this.finish();
				break;

			case GROUP_MODIFY_NAME_FAILED:
				Toast.makeText(GroupEdit.this, "群名称不符合规则,请重新设置",
						Toast.LENGTH_SHORT).show();
				break;

			case GROUP_MODIFY_INFO_FAILED:
				Toast.makeText(GroupEdit.this, "群简介不符合规则,请重新设置",
						Toast.LENGTH_SHORT).show();
				break;

			case GROUP_MODIFY_NOTICE_FAILED:
				Toast.makeText(GroupEdit.this, "群公告不符合规则,请重新设置",
						Toast.LENGTH_SHORT).show();
				break;
			case GROUP_OPT_FAILED:
				String fbret = (String) msg.obj;
				Toast.makeText(GroupEdit.this, fbret, Toast.LENGTH_SHORT)
						.show();
				break;
			}
		}
	};
	private LinearLayout head_ll;
	private LinearLayout name_ll;
	private LinearLayout introl_ll;
	private LinearLayout notice_ll;
	private LinearLayout btn_back_edit;
}
