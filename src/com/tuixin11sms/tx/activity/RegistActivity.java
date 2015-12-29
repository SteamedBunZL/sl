package com.tuixin11sms.tx.activity;

import java.io.File;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class RegistActivity extends BaseActivity implements OnClickListener {
	protected static final String TAG = "RegistActivity";
	private static final int REGISTER_FAILED = 90;
	private static final int REGISTER_NAME_FAILED = 91;
	private static final int REGISTER_NICK_FAILED = 92;
	public static final int GET_HEAD_IMG_FROM_CAMERA = 100;// 拍照
	public static final int GET_HEAD_IMG_FROM_GALLERY = 101; // 相册选择
	public static final int EDIT_HEAD_IMG = 102;// 结果
	public static final int REGISTER_MORE = 103;// 注册次数过多--bobo
	private Button mRegist;
	private TextView mWeb;
	private EditText mNickname;
	private EditText mPassword;
	private int densityDpi;
	private UpdateReceiver updatereceiver;
	private ImageView iv_avatar;
	private boolean canClick = true;
	private int sex_regist = TX.MALE_SEX;//注册时用户选择的性别
	private String mImagePath;//头像图片本地路径

	private SmileyParser smileyParser_edittext_hdpi;
	private SmileyParser smileyParser;
	private LinearLayout ll_regist_sex_male;//性别男选项
	private LinearLayout ll_regist_sex_female;//性别女选项
	private TextView numText;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_regist);
		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// title上的返回按钮
				thisContext.finish();
				
			}
		});
		smileyParser_edittext_hdpi = new SmileyParser(this);
		smileyParser = new SmileyParser(this);
		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		densityDpi = metric.densityDpi;
		Intent intent = this.getIntent();

		int d = intent.getIntExtra("d", 0);
		if (d == 4) {
			if (Utils.debug)
			Utils.startPromptDialog(RegistActivity.this,
					R.string.regist_name_error, R.string.login_nickname_illegal);
		} else if (d == 2) {
			Utils.startPromptDialog(RegistActivity.this, R.string.error,
					R.string.pw_request_failed_prompt);
		}
		initViews();
	}

	private void initViews() {
		mRegist = (Button) findViewById(R.id.btn_regist);
		mNickname = (EditText) findViewById(R.id.et_regist_nick_name);
		mPassword = (EditText) findViewById(R.id.et_regist_pwd);
		iv_avatar = (ImageView) findViewById(R.id.iv_avatar);
		numText = (TextView) findViewById(R.id.tv_name_length_remain);
		ll_regist_sex_male = (LinearLayout) findViewById(R.id.ll_regist_sex_male);
		ll_regist_sex_female = (LinearLayout) findViewById(R.id.ll_regist_sex_female);
		ll_regist_sex_male.setOnClickListener(this);
		ll_regist_sex_female.setOnClickListener(this);
		iv_avatar.setOnClickListener(this);
		mNickname.addTextChangedListener(mTextWatcher);
		String registname =  mSess.mPrefMeme.regist_name.getVal();
		if (!Utils.isNull(registname)) {
			mNickname.setText(registname);
		}
		mWeb = (TextView) findViewById(R.id.mWeb);
		mRegist.setOnClickListener(registClick);
		mWeb.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent i = new Intent(RegistActivity.this, PrivacyViewActivity.class);
				startActivity(i);
				
			}
		});
		mNickname.setOnLongClickListener(nickNameLongclick);
		
		//设置性别
		if (sex_regist == TX.MALE_SEX) {
//			onMaleClick();
			setSexMale();
		}else {
//			onFemaleClick();
			setSexFemale();
		}
	}

	private Handler handler = new WrappedHandler(thisContext) {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REGISTER_FAILED:
				showToast(R.string.regist_fail_later,false);
				break;
			case REGISTER_NAME_FAILED:
				String fbretStr = (String) msg.obj;
				showToast(fbretStr,false);
				break;
			case REGISTER_NICK_FAILED:
				showToast(R.string.regist_name_fail_again,false);
				break;
			case REGISTER_MORE:
				String errmsg = (String) msg.obj;
				showToast(errmsg,false);
				break;
			default:
				break;
			}
		}

	};

	private View.OnLongClickListener nickNameLongclick = new View.OnLongClickListener() {

		@Override
		public boolean onLongClick(View v) {

			final EditText et = (EditText) v;
			final String paste = RegistActivity.this.getResources().getString(
					R.string.chatroom_editText_paste);
			final String copy = RegistActivity.this.getResources().getString(
					R.string.chatroom_longclick_copy);
			final String inputMethod = RegistActivity.this.getResources()
					.getString(R.string.chatroom_editText_inputMethod);
			final String[] items = { copy, paste, inputMethod };
			new AlertDialog.Builder(RegistActivity.this)
					.setTitle(R.string.chatroom_longclick_titile)
					.setItems(items, new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (items[which].equals(copy)) {
								ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
								clipboard.setText(et.getText().toString());
							} else if (items[which].equals(paste)) {
								ClipboardManager clipboard2 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
								StringBuffer clipsb = new StringBuffer()
										.append(clipboard2.getText());
								String clip = clipsb.toString();
								if (densityDpi > 160)
									et.append(smileyParser_edittext_hdpi
											.addSmileySpans(clip, true, 0));
								else
									et.append(smileyParser.addSmileySpans(clip,
											true, 0));
								et.requestFocus();

							} else if (items[which].equals(inputMethod)) {
								showInputMethodDialog();
							}
						}
					}).show();
			return true;
		}
	};

	private void showInputMethodDialog() {
		InputMethodManager imm = (InputMethodManager) RegistActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.getEnabledInputMethodList();
		imm.showInputMethodPicker();
	}

	private View.OnClickListener registClick = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (!Utils.checkNetworkAvailable(RegistActivity.this)) {
				Utils.startPromptDialog(RegistActivity.this,
						R.string.regist_fail, R.string.regist_network_title);
				return;
			}
			if (!canClick) {
				return;
			}

			String nameStr = mNickname.getText().toString().trim();
			String passwordStr = mPassword.getText().toString().trim();
			if (Utils.isNull(mImagePath)) {
				Toast.makeText(RegistActivity.this, "请您设置头像", Toast.LENGTH_LONG)
						.show();
				return;
			}
			int len = nameStr.length();
			int space_index = passwordStr.indexOf(" ");
			if (len <= 0) {
				Toast.makeText(RegistActivity.this,
						R.string.insert_name_prompt, Toast.LENGTH_LONG).show();
			} else if (len > Utils.INPUT_USERNAME_MAX_LENGTH) {
				Utils.startPromptDialog(RegistActivity.this,
						R.string.regist_name_too_long, R.string.name_too_long);
			} else if (!Utils.filterStr(nameStr, RegistActivity.this)) {
				Utils.startPromptDialog(RegistActivity.this, R.string.error,
						R.string.name_sys_error);
			} else if (space_index != -1) {
				Toast.makeText(RegistActivity.this,
						R.string.pw_has_space_prompt, Toast.LENGTH_LONG).show();
			} else if (passwordStr.length() < 6) {
				Toast.makeText(RegistActivity.this,
						R.string.pw_length_notrule_prompt, Toast.LENGTH_LONG)
						.show();
			} else if (passwordStr.length() > 20) {
				Toast.makeText(RegistActivity.this,
						R.string.pw_length_notrule_prompt, Toast.LENGTH_LONG)
						.show();
			} else {
				boolean hasChinese = false;
				for (int i = 0; i < passwordStr.length(); i++) {
					if (passwordStr.substring(i, i + 1).matches(
							"[\\u4e00-\\u9fa5]+")) {
						hasChinese = true;
						break;
					}
				}
				if (hasChinese) {
					Toast.makeText(RegistActivity.this,
							R.string.pw_has_chinese_prompt, Toast.LENGTH_LONG)
							.show();
					return;
				}
				canClick = false;
				Utils.hideSoftInput(RegistActivity.this);
				String nameChangePrompt = RegistActivity.this.getResources()
						.getString(R.string.name_creating);
				showDialogTimer(RegistActivity.this, 0, nameChangePrompt,
						10 * 1000, new BaseTimerTask() {
							public void run() {
								super.run();
								canClick = true;
								handler.sendEmptyMessage(REGISTER_FAILED);
							}
						}).show();
//				getEditorMeme().putString(CommonData.REGIST_NAME, nameStr);
//				getEditorMeme().putString(CommonData.USER_ID, "");
//				getEditorMeme().putString(CommonData.NICKNAME, "");
				
				mSess.mPrefMeme.regist_name.setVal(nameStr);
				mSess.mPrefMeme.user_id.setVal("");
				mSess.mPrefMeme.nickname.setVal("").commit();

//				LoginSessionManager session = LoginSessionManager
//						.getManager(txdata);
				if (sex_regist == TX.FEMAL_SEX) {
					// 是女性
					mSess.setRegisterInfor(nameStr, passwordStr, TX.FEMAL_SEX);
				} else {
					// 是男性
					mSess.setRegisterInfor(nameStr, passwordStr, TX.MALE_SEX);
				}
				mSess.setLoginSuccessed(false);
//				getEditorMeme().putString(CommonData.DOOR, "");
//				getEditorMeme().commit();
				mSess.mPrefMeme.door.setVal("").commit();
				TX.tm.reloadTXMe();// /////
				mSess.getSocketHelper().sendPing();
			}
		}
	};


	
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_avatar:
			onAvatarClick();
			break;
		case R.id.ll_regist_sex_male:
			onMaleClick();
			break;
		case R.id.ll_regist_sex_female:
			onFemaleClick();
			break;
		}
		
	}
	

	public void onResume() {
		super.onResume();
		registReceiver();
	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_REGIST_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_REGIST_RSP.equals(intent.getAction())) {
				dealRegist(serverRsp);
			}
		}
	}

	public void onStop() {
		super.onStop();
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
	}

	@Override
	protected void onDestroy() {
		cancelDialog();
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			Bitmap tempimg = null;
			switch (requestCode) {
			case GET_HEAD_IMG_FROM_GALLERY:
				// Utils.inPhoto = false;
				Uri uri = data.getData();
				ContentResolver cr = this.getContentResolver();
				Cursor cursor = cr.query(uri, null, null, null, null);
				if (cursor == null
						|| (cursor != null && cursor.getCount() <= 0)) {
					Utils.startPromptDialog(RegistActivity.this,
							R.string.prompt,
							R.string.userinfo_upload_failed_unknow);
					return;
				}
				cursor.moveToFirst();
				// String imagePath = cursor.getString(1);
				mImagePath = cursor.getString(1);
				cursor.close();
				try {
					Intent i = new Intent(this, EditHeadIcon.class);
					i.putExtra(EditHeadIcon.GET_IMG_PATH, mImagePath);
					i.putExtra(EditHeadIcon.STATE_COME,
							EditHeadIcon.FROM_GALLERY);
					i.putExtra(EditHeadIcon.IS_REGISTE, true);
					startActivityForResult(i, EDIT_HEAD_IMG);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e("Exception", e.getMessage(), e);
				}
				break;

			case EDIT_HEAD_IMG:
				tempimg = data.getParcelableExtra(EditHeadIcon.GIVE_IMG);
				iv_avatar.setImageDrawable(new BitmapDrawable(tempimg));

				break;
			case GET_HEAD_IMG_FROM_CAMERA:
				// 从相机拍照获取头像
				// Utils.inPhoto = false;

				try {
					String storagePath = Utils
							.getStoragePath(RegistActivity.this);
					// StringBuffer tempPath = new
					// StringBuffer().append(storagePath).append("/").append("self")
					// .append(".jpg");
					// String imagePath = tempPath.toString();
					mImagePath = storagePath + "/self.jpg";
					tempimg = Utils.fitSizeAutoImg(mImagePath,
							Utils.msgroom_list_resolution);
					Intent i = new Intent(this, EditHeadIcon.class);
					i.putExtra(EditHeadIcon.GET_IMG_PATH, mImagePath);
					i.putExtra(EditHeadIcon.STATE_COME,
							EditHeadIcon.FROM_CAMERA);
					i.putExtra(EditHeadIcon.IS_REGISTE, true);
					startActivityForResult(i, EDIT_HEAD_IMG);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e("Exception", "从相机获取注册头像信息异常：", e);
				}
				break;
			}
		}

	}

	private void onMaleClick() {
		if (sex_regist != TX.MALE_SEX) {
			setSexMale();
		}
	}
	
	/**设置为男性*/
	private void setSexMale() {
		ImageView iv_female = (ImageView) ll_regist_sex_female.findViewById(R.id.iv_regist_sex_female);
		TextView tv_female = (TextView) ll_regist_sex_female.findViewById(R.id.tv_regist_sex_female);
		ImageView iv_male = (ImageView) ll_regist_sex_male.findViewById(R.id.iv_regist_sex_male);
		TextView tv_male = (TextView) ll_regist_sex_male.findViewById(R.id.tv_regist_sex_male);
		iv_male.setImageResource(R.drawable.regist_sex_male_selected);
		tv_male.setTextColor(getResources().getColor(R.color.content_color_blue));
		iv_female.setImageResource(R.drawable.regist_sex_female);
		tv_female.setTextColor(getResources().getColor(R.color.sub_title_text_color));
		sex_regist = TX.MALE_SEX;
		if (TextUtils.isEmpty(mImagePath)) {
			iv_avatar.setImageResource(R.drawable.regist_default_boy);
		}
		mSess.setSex(TX.MALE_SEX);
		mSess.mPrefMeme.sex.setVal(TX.MALE_SEX).commit();
		TX.tm.reloadTXMe();// /////////
	}

	private void onFemaleClick() {
		if (sex_regist != TX.FEMAL_SEX) {
			setSexFemale();
		}
	}
	
	/**设置为女性*/
	private void setSexFemale() {
		ImageView iv_female = (ImageView) ll_regist_sex_female.findViewById(R.id.iv_regist_sex_female);
		TextView tv_female = (TextView) ll_regist_sex_female.findViewById(R.id.tv_regist_sex_female);
		ImageView iv_male = (ImageView) ll_regist_sex_male.findViewById(R.id.iv_regist_sex_male);
		TextView tv_male = (TextView) ll_regist_sex_male.findViewById(R.id.tv_regist_sex_male);
		iv_male.setImageResource(R.drawable.regist_sex_male);
		tv_male.setTextColor(getResources().getColor(R.color.sub_title_text_color));
		iv_female.setImageResource(R.drawable.regist_sex_female_selected);
		tv_female.setTextColor(getResources().getColor(R.color.content_color_red));
		sex_regist = TX.FEMAL_SEX;
		if (TextUtils.isEmpty(mImagePath)) {
			iv_avatar.setImageResource(R.drawable.regist_default_girl);
		}
		mSess.setSex(TX.FEMAL_SEX);
		mSess.mPrefMeme.sex.setVal(TX.FEMAL_SEX).commit();
		TX.tm.reloadTXMe();// ///////

	}
	
	

	/**
	 * 头像点击事件处理
	 */
	private void onAvatarClick() {
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.hideSoftInputFromWindow(RegistActivity.this.getCurrentFocus()
						.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		final AlertDialog.Builder headDialog = new AlertDialog.Builder(this);
		headDialog.setTitle("更多选项");
		headDialog.setItems(R.array.msgroom_pic,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (which == 0) {
							// 从系统相册获取头像照片
							String storagePath = Utils
									.getStoragePath(RegistActivity.this);
							if (Utils.isNull(storagePath)) {
								return;
							}
							Intent getImage = new Intent(
									Intent.ACTION_GET_CONTENT);
							getImage.addCategory(Intent.CATEGORY_OPENABLE);
							getImage.setType("image/*");
							startActivityForResult(getImage,
									GET_HEAD_IMG_FROM_GALLERY);
							dialog.dismiss();
							// Utils.inPhoto = true;
						} else if (which == 1) {
							// 从照相机获取头像照片
							if (!Utils.checkSDCard()) {
								if (Utils.debug)
									Log.i(TAG, "无SD卡");
								return;
							}
							String storagePath = Utils
									.getStoragePath(RegistActivity.this);
							if (Utils.isNull(storagePath)) {
								return;
							}
							File sddir = new File(storagePath);
							if (!sddir.exists() && !sddir.mkdirs()) {
								if (Utils.debug)
									Log.e("bitmapFromUrl", "Create dir failed");
							}
							// long user_id =
							// Long.parseLong(prefs.getString(CommonData.USER_ID,
							// "-1"));
							StringBuffer tempPath = new StringBuffer()
									.append(storagePath).append("/")
									.append("self").append(".jpg");
							Intent it = new Intent(
									"android.media.action.IMAGE_CAPTURE");
							it.putExtra(MediaStore.EXTRA_OUTPUT,
									Uri.fromFile(new File(tempPath.toString())));
							it.putExtra(Utils.IMAGE_CAMRA, tempPath.toString());
							startActivityForResult(it, GET_HEAD_IMG_FROM_CAMERA);
							// Utils.inPhoto = true;
						}
					}
				}).show();

	}

	//无引用 2014.02.20 shc
//	private String getRealPathFromURI(Uri contentUri) {
//
//		// can post image
//		String[] proj = { MediaStore.Images.Media.DATA };
//		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
//														// return
//				null, // WHERE clause; which rows to return (all rows)
//				null, // WHERE clause selection arguments (none)
//				null); // Order-by clause (ascending by name)
//		int column_index = cursor
//				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
//		cursor.moveToFirst();
//		String path = cursor.getString(column_index);
//		cursor.close();
//		return path;
//	}

	private void dealRegist(ServerRsp serverRsp) {
		if (serverRsp != null) {
			cancelDialogTimer();
			canClick = true;
			if (serverRsp.getStatusCode() != null) {
				switch (serverRsp.getStatusCode()) {
				case RSP_OK: {
					Intent intent = new Intent(RegistActivity.this,
							RegistSucceedActivity.class);
					startActivity(intent);
					break;
				}
				case OPT_FAILED: {
					String fbret = serverRsp.getBundle().getString("fbret");
					if (fbret != null) {
						if(fbret.equals("")){
							fbret="未知错误";
						}
						Message message = handler.obtainMessage(
								REGISTER_NAME_FAILED, fbret);
						handler.sendMessage(message);
					}
					break;
				}
				case MORE_REGIST: {
//					String fbret = serverRsp.getBundle().getString("fbret");//无引用 2014.02.20
					String errmsg = serverRsp.getBundle().getString("errmsg");
					if (errmsg != null) {
						Message message = handler.obtainMessage(REGISTER_MORE,
								errmsg);
						handler.sendMessage(message);
					}

					break;
				}
				case LOGIN_NAME_FAIELD:
					handler.sendEmptyMessage(REGISTER_NICK_FAILED);
					break;
				default: {
					handler.sendEmptyMessage(REGISTER_FAILED);
					break;
				}
				}

			}
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
		}

		@Override
		public void afterTextChanged(Editable s) {
			//
			editStart = mNickname.getSelectionStart();
			editEnd = mNickname.getSelectionEnd();
			numText.setText((24 - (temp.length())) + "");
			if (temp.length() > 24) {
				Toast.makeText(RegistActivity.this, "最多可以输入24个字符",
						Toast.LENGTH_SHORT).show();
				s.delete(editStart - 1, editEnd);
				int tempSelection = editStart;
				mNickname.setText(s);
				mNickname.setSelection(tempSelection);
			}
		}
	};

	

}
