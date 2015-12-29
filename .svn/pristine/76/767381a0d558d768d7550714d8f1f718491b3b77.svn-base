package com.shenliao.group.activity;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupUtils;
import com.shenliao.set.activity.SetGroupNewInfonActivity;
import com.shenliao.set.activity.SetGroupNewNameAcitivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditHeadIcon;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class GroupCreate extends BaseActivity implements OnClickListener {
	protected static final String TAG = "GroupCreate";
	Button mCreate;
	LinearLayout mTitle;
	LinearLayout mIntrol; // 群简介
	RadioGroup publicGroupType, privateGroupType;
	TextView updateHead;
	final int typeValue = TxDB.GROUP_TYPE_PUBLIC;
	LinearLayout icon;
	public static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	private UpdateReceiver updatareceiver;
	public static final int ACTIVITY_ACTION_PIC_VIEW = 5;
	// private SharedPreferences prefs;
	// private Editor editor;
	public static final int ACTIVITY_ACTION_PHOTO_CAPTURE = 4;
	private CheckBox publicRadio, searchedRaidio, invitedRadio;
	private ScrollView screen;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_new_group_setting);
		init();
	}

	private void init() {
		// typeValue = TxDB.GROUP_TYPE_REQUEST;

		mCreate = (Button) findViewById(R.id.group_setting_create_btn);
		mTitle = (LinearLayout) findViewById(R.id.create_group_input_introl_box);
		mIntrol = (LinearLayout) findViewById(R.id.create_group_input_box);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left_creatQun);

		group_new_name = (TextView) findViewById(R.id.group_new_name);
		group_new_info = (TextView) findViewById(R.id.group_new_info);

		updateHead = (TextView) findViewById(R.id.create_group_update_photo);
		updateHead.getBackground().setAlpha(100);
		icon = (LinearLayout) findViewById(R.id.create_group_head);
		headIcon = (ImageView) findViewById(R.id.sl_tab_setting_userinfo_include_head);

		publicRadio = (CheckBox) findViewById(R.id.group_setting_checkpublic);
		searchedRaidio = (CheckBox) findViewById(R.id.group_setting_checksearched);
		invitedRadio = (CheckBox) findViewById(R.id.group_setting_checkinvited);
		screen = (ScrollView) findViewById(R.id.group_create_screen);

		screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) GroupCreate.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
		screen.setOnClickListener(this);
		publicRadio.setOnClickListener(radioOclick);
		searchedRaidio.setOnClickListener(radioOclick);
		invitedRadio.setOnClickListener(radioOclick);

		mTitle.setOnClickListener(this);
		mIntrol.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);
		searchedRaidio.setChecked(true);
		icon.setOnClickListener(this);
		mCreate.setOnClickListener(this);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
	}

	public static final int REQUESTCODE_FOR_REQUSET_GROUPNAME = 11;// 昵称请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPNAME = 12;// 昵称返回请求码
	public static final int REQUESTCODE_FOR_REQUSET_GROUPINFO = 13;// 简介请求码
	public static final int RESULTCODE_FOR_RESULT_GROUPINFO = 14;// 简介返回请求码

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_left_creatQun:
			// 返回按钮;
			GroupCreate.this.finish();
			break;
		case R.id.group_create_screen:
			InputMethodManager imm = (InputMethodManager) GroupCreate.this
					.getSystemService(Context.INPUT_METHOD_SERVICE);

			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);

			break;
		case R.id.create_group_input_introl_box:
			// 简介
			Intent intentGroupinfo = new Intent(GroupCreate.this,
					SetGroupNewInfonActivity.class);
			intentGroupinfo.putExtra("info", group_new_info.getText());
			startActivityForResult(intentGroupinfo,
					REQUESTCODE_FOR_REQUSET_GROUPINFO);
			break;
		case R.id.create_group_input_box:
			// 名称
			Intent intentGroupname = new Intent(GroupCreate.this,
					SetGroupNewNameAcitivity.class);
			intentGroupname.putExtra("name", group_new_name.getText());
			startActivityForResult(intentGroupname,
					REQUESTCODE_FOR_REQUSET_GROUPNAME);
			break;

		case R.id.group_setting_create_btn:

			if ((group_new_name.getText().length() == 0)) {
				Toast.makeText(GroupCreate.this, "聊天室名称不能为空",
						Toast.LENGTH_SHORT).show();
			} else if (group_new_info.getText().length() == 0) {
				Toast.makeText(GroupCreate.this, "聊天室简介不能为空",
						Toast.LENGTH_SHORT).show();
			} else if (group_new_info.getText().toString().trim().length() > 100) {
				Toast.makeText(GroupCreate.this, "聊天室简介最多可输入100字",
						Toast.LENGTH_SHORT).show();
			} else {
				showDialogTimer(GroupCreate.this, R.string.prompt,
						R.string.creating_group_title, 10 * 1000).show();
				mSess.getSocketHelper().sendCreatGroup(
						group_new_name.getText().toString(),
						group_new_info.getText().toString(), avatorPath,
						typeValue);

			}

			// changeGroupMsg();

			break;
		case R.id.create_group_head:

			final AlertDialog.Builder headDialog = new AlertDialog.Builder(
					GroupCreate.this);
			headDialog.setTitle("更多选项");
			headDialog.setItems(R.array.msgroom_pic,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							if (which == 0) {
								String storagePath = Utils
										.getStoragePath(GroupCreate.this);
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
								// Utils.inPhoto=true;
							} else if (which == 1) {
								if (!Utils.checkSDCard()) {
									if (Utils.debug) {
										Log.i(TAG, "无SD卡");
									}
									return;
								}
								String storagePath = Utils
										.getStoragePath(GroupCreate.this);
								if (Utils.isNull(storagePath)) {
									return;
								}
								File sddir = new File(storagePath);
								if (!sddir.exists() && !sddir.mkdirs()) {
									if (Utils.debug)
										Log.e(TAG,
												"bitmapFromUrl----Create dir failed");
								}
								// long
								// user_id=Long.parseLong(prefs.getString(CommonData.USER_ID,
								// "-1"));
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
								// Utils.inPhoto=true;
							}
						}
					}).show();
			break;
		}
	}

	public static final int ACTION_EDIT_PICK = 7;
	private String avatorPath;

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case REQUESTCODE_FOR_REQUSET_GROUPNAME:
			if (resultCode == RESULTCODE_FOR_RESULT_GROUPNAME) {
				String newname = data.getExtras().getString("newname");
				group_new_name.setText(newname);
			}
			break;
		case REQUESTCODE_FOR_REQUSET_GROUPINFO:
			if (resultCode == RESULTCODE_FOR_RESULT_GROUPINFO) {
				String newinfo = data.getExtras().getString("newinfo");
				group_new_info.setText(newinfo);
			}
			break;
		case ACTIVITY_ACTION_PIC_VIEW:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto=false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(GroupCreate.this, R.string.prompt,
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
					i.putExtra(EditHeadIcon.GROUP_ID, "group_" + 0);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "异常", e);
				}
			}
			break;
		case ACTION_EDIT_PICK:
			if (resultCode == Activity.RESULT_OK) {
				tempimg = null;
				tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
				avatorPath = data.getStringExtra(EditHeadIcon.GROUP_ICON_PATH);
				headIcon.setImageBitmap(Utils.getRoundedCornerBitmap(tempimg));
			}
			break;
		case ACTIVITY_ACTION_PHOTO_CAPTURE:
			switch (resultCode) {
			case RESULT_OK:
				// Utils.inPhoto=false;
				try {
					String storagePath = Utils.getStoragePath(GroupCreate.this);
					// long
					// user_id=Long.parseLong(prefs.getString(CommonData.USER_ID,
					// "-1"));
					long user_id = Long.parseLong(mSess.mPrefMeme.user_id
							.getVal());
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
					i.putExtra(EditHeadIcon.GROUP_ID, "group_" + 0);
					startActivityForResult(i, ACTION_EDIT_PICK);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "异常", e);
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

	/**
	 * 
	 * @return true 通过检查
	 */
	private boolean checkInput() {

		if (group_new_name.getText().length() != 0
				&& group_new_info.getText().length() != 0) {
			return true;
		} else {
			return false;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		registReceiver();
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
			filter.addAction(Constants.INTENT_ACTION_CREATE_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CREATE_GROUP.equals(intent.getAction())) {
				dealCreateGroup(serverRsp);
			}
		}
	}

	private void dealCreateGroup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Intent i = new Intent(GroupCreate.this, GroupCreateResult.class);
			i.putExtra("avatar", tempimg);
			long gid = serverRsp.getBundle().getLong(Utils.MSGROOM_GROUP_ID);
			i.putExtra(Utils.MSGROOM_TX_GROUP, TxGroup.getTxGroup(
					GroupCreate.this.getContentResolver(), (int) gid));
			startActivity(i);
			GroupCreate.this.finish();
			break;
		case GROUP_MEMBER_SIZE_INVALID:
			Utils.startPromptDialog(GroupCreate.this, R.string.prompt,
					R.string.create_group_more_few);
			break;
		case GROUP_MEMBER_THAN_LIMIT:
			Utils.startPromptDialog(GroupCreate.this, R.string.prompt,
					R.string.create_group_to_more);
			break;
		case OPT_FAILED:
			String fbret = serverRsp.getBundle().getString("fbret");

			if (fbret != null && !fbret.equals("")) {
				GroupUtils.showChangeFailedDialog(GroupCreate.this, fbret);
			} else {
				Utils.startPromptDialog(GroupCreate.this, R.string.prompt,
						R.string.create_group_failed);
			}
			break;
		case GROUP_NAME_INTRO_SPECIAL_CHAR:
			Utils.startPromptDialog(GroupCreate.this, R.string.prompt,
					R.string.create_group_special_chat);
			break;

		}
	}

	OnClickListener radioOclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {

			case R.id.group_setting_checkpublic:

				publicRadio.setChecked(true);

				searchedRaidio.setChecked(false);
				invitedRadio.setChecked(false);
				// typeValue = TxDB.GROUP_TYPE_PUBLIC;
				break;
			case R.id.group_setting_checksearched:

				searchedRaidio.setChecked(true);
				publicRadio.setChecked(false);
				invitedRadio.setChecked(false);
				// typeValue = TxDB.GROUP_TYPE_REQUEST;
				break;
			case R.id.group_setting_checkinvited:

				invitedRadio.setChecked(true);
				publicRadio.setChecked(false);
				searchedRaidio.setChecked(false);
				// typeValue = TxDB.GROUP_TYPE_SECRET;
				break;
			}

		}
	};
	// OnTouchListener onTouch=new OnTouchListener() {
	//
	// @Override
	// public boolean onTouch(View v, MotionEvent event) {
	//
	// switch (v.getId()) {
	// case R.id.create_group_input_introl_box:
	// mTitle.setCursorVisible(true);
	// break;
	// case R.id.create_group_input_box:
	// mIntrol.setCursorVisible(true);
	// break;
	// default:
	// break;
	// }
	// return false;
	// }
	// };
	private ImageView headIcon;
	private TextView group_new_name;
	private TextView group_new_info;
	private LinearLayout btn_back_left;
	private Bitmap tempimg;
}
