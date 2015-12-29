package com.shenliao.user.activity;

import java.util.Calendar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditSendImg;
import com.tuixin11sms.tx.activity.SingleMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 好友请求个人资料
 * 
 * @author xch
 * 
 */
public class UserInforRequestActivity extends BaseActivity implements
		OnClickListener {
	private ImageView headImage;// 头像
	private TextView nickName;// 昵称
	private TextView slNum;// 神聊号
	// private TextView sex;// 性别
	private View user_info_sex_age;// 联系人性别和年龄父view
	private TextView tv_sex;// 性别
	private TextView tv_age;// 年龄
	// private TextView sign;// 标签
	// private LinearLayout sexImage;//性别图
	private LinearLayout userInfoLinear;// 个人资料区域
	private ImageView adminImage;// 是否是管理员
	private TextView admin;
	private Intent intent;
	private SmileyParser sParser;
	// private SharedPreferences prefs;
	private UpdateReceiver updatareceiver;
	private TXMessage txMessage;
	private TX me;
	private boolean isfriend;
	private boolean isOnclick;
	private boolean isNotFirstShow;
	// private int tempAvatar = 0;

	private int currentYear;
	private int currentMonth;
	private int currentDay;

	private Button okBtn;
	// private Button cancleBtn;
	private Button noBtn;
	private int time_out = 5 * 1000;
	public static final int ADD_FRIEND_SUCCESS = 1;
	public static final int ADD_FRIEND_FAIL = 2;
	public static final int BLACK_SUCCESS = 3;
	public static final int BLACK_FAIL = 4;
	public static final int BLACK_FAIL_MORE = 5;
	public static final int TEL_CHECK_TIMEOUT = 6;
	public static final int SEND_REQUEST_SUCCESS = 7;
	public static final int TIMER_OUT = 8;
	public static final int REFRESH = 9;
	private static final int REFURBISH_UI = 10;
	public static final String REFRESH_OBJ = "refreshObj";

	// private int defaultHeadImg;
	// private int defaultHeadImgMan;
	// private int defaultHeadImgFemal;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_requestfriend_userinfo);
		init();
		setData();
	}

	// 初始化
	private void init() {
		// defaultHeadImgMan=R.drawable.sl_regist_default_head;
		// defaultHeadImgFemal=R.drawable.sl_regist_head_femal;
		// sexImage=(LinearLayout) findViewById(R.id.ll_sex_age_user_infor);
		headImage = (ImageView) findViewById(R.id.iv_user_info_head);
		nickName = (TextView) findViewById(R.id.user_info_name);
		slNum = (TextView) findViewById(R.id.user_info_id_content);
		// sex = (TextView) findViewById(R.id.user_info_sex);
		tv_sex = (TextView) findViewById(R.id.tv_user_info_sex);
		tv_age = (TextView) findViewById(R.id.tv_user_info_age);
		user_info_sex_age = findViewById(R.id.rl_user_info_sex_age);
		// sign = (TextView) findViewById(R.id.tv_user_infor_sign_content);
		okBtn = (Button) findViewById(R.id.user_request_ok);
		// cancleBtn=(Button) findViewById(R.id.user_request_cancle);
		noBtn = (Button) findViewById(R.id.user_request_no);
		userInfoLinear = (LinearLayout) findViewById(R.id.userinfo_settings_photo);
		adminImage = (ImageView) findViewById(R.id.iv_admin_icon);
		userLevel = (TextView) findViewById(R.id.user_info_level);
		userInfoLinear.setOnClickListener(this);
		okBtn.setOnClickListener(this);
		noBtn.setOnClickListener(this);
		// cancleBtn.setOnClickListener(this);
		headImage.setOnClickListener(this);

		Calendar calendar = Calendar.getInstance();
		currentYear = calendar.get(Calendar.YEAR);
		currentMonth = calendar.get(Calendar.MONTH);
		currentDay = calendar.get(Calendar.DAY_OF_MONTH);
		sParser = Utils.getSmileyParser(this);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		intent = this.getIntent();
		if (intent != null) {
			txMessage = intent.getExtras().getParcelable("txMessage");
			if (txMessage == null) {
				this.finish();
				return;
			}
			// tempAvatar =
			// TX.findTXByPartnerID4DB(txMessage.tcard_id).avatar_ver;//不要直接访问数据库
			// 2013.10.17 shc
			// tempAvatar = TX.tm.getTx(txMessage.tcard_id).getAvatar_ver();
			NotificationPopupWindow.showNotification(txMessage.msg_id, false);
			isfriend = intent.getExtras().getBoolean("isfriend");
			mSess.getSocketHelper().sendGetUserInfor(txMessage.tcard_id);
		}

	}

	// 无引用，注掉 2014.03.14 shc
	// protected void loadHeadImage(long uid, boolean isVerChanged,
	// AsyncCallback<Bitmap> callback){
	// // Bitmap bitmap = getCachedBitmap(uid);
	// // if (bitmap == null) {
	// String url = null;
	// if (uid == TX.tm.getTxMe().partner_id)
	// url = TX.tm.getTxMe().avatar_url;
	// else{
	// // TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
	// TX tx = TX.tm.getTx(uid);
	//
	//
	// if (tx != null)
	// url = tx.avatar_url;
	// }
	// Bitmap bm = getPartnerCachedBitmap(uid);
	// if (bm!=null) {
	// headImage.setImageBitmap(bm);
	// }else {
	// if(url!=null){
	// loadHeadImg(url, uid, callback);
	// // CallInfo callinfo=new CallInfo(url,uid,callback);
	// //// mAsynloader.obtainMessage(1,callinfo).sendToTarget();
	// // mPartnerAsynloader.obtainMessage(1, isVerChanged?1:0, -1,
	// callinfo).sendToTarget();
	// }
	// }
	//
	// // }
	// // return bitmap;
	// }

	// 设置数据
	private void setData() {

		if (txMessage == null) {
			this.finish();
			return;
		}
		if (me == null) {
			// me = TX.findTXByPartnerID4DB(txMessage.tcard_id);//不要直接访问数据库
			// 2013.10.17 shc
			me = TX.tm.getTx(txMessage.tcard_id);

		}

		if (me == null) {
			// 如果数据库中取不到，则直接关闭页面。等待用户再次进入
			this.finish();
			return;
		}
		// // 性别
		// sex.setText(me.sex == 0 ?
		// this.getResources().getString(R.string.user_male) :
		// this.getResources().getString(
		// R.string.user_female));

		if (me.getSex() == TX.MALE_SEX) {
			// sex.setText(getString(R.string.user_male));
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_male));
			tv_sex.setText("♂");
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			// sex.setText(getString(R.string.user_female));
			defaultHeaderImg = defaultHeaderImgFemale;
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_female));
			tv_sex.setText("♀");
		}

		long uid = txMessage.tcard_id;
		String url = null;
		TX ttx = TX.tm.getTx(uid);
		if (ttx != null)
			url = ttx.avatar_url;
		if (Utils.isIdValid(uid)) {
			// 头像版本号
			// boolean isImgChanged = false;
			// if (me.getAvatar_ver() != tempAvatar || me.getAvatar_ver() == 0)
			// {
			// isImgChanged = true;
			// }

			headImage.setTag(txMessage.tcard_id);
			headImage.setImageResource(defaultHeaderImg);

			Bitmap bm = mSess.avatarDownload.getAvatar(url, uid, headImage,
					avatarHandler);
			if (bm != null) {
				headImage.setImageBitmap(bm);
			} else {
				headImage.setImageResource(defaultHeaderImg);
				// 如果bm为空，或者版本号有变需要更新则都去重新获取头像
			}
			// if (isImgChanged) {
			// //代表头像版本号有改变，那么下载更新头像版本之前，删除本地已有的头像大小图
			// String filePath = mSess.mDownUpMgr.getAvatarFile(url, uid,
			// false);
			// File headFile = null;
			// if (filePath!=null) {
			// headFile = new File(filePath);
			// if (headFile.exists()) {
			// headFile.delete();
			// }
			// }
			// filePath = mSess.mDownUpMgr.getAvatarFile(url, uid, true);
			// if (filePath!=null) {
			// headFile = new File(filePath);
			// if (headFile.exists()) {
			// headFile.delete();
			// }
			// }
			// }
			// if (bm==null || isImgChanged) {
			// //如果bm为空，或者版本号有变需要更新则都去重新获取头像
			// headImage.setTag(txMessage.tcard_id);
			// loadHeadImg(url, uid, new AsyncCallback<Bitmap>() {
			//
			// @Override
			// public void onFailure(Throwable t, long id) {
			//
			// }
			//
			// @Override
			// public void onSuccess(Bitmap result, long id) {
			// if((Long)headImage.getTag() == id){
			// headImage.setImageBitmap(result);
			// }
			// }
			// });
			// }

		}

		// 昵称
		if (!Utils.isNull(me.getNick_name())) {
			nickName.setText(sParser.addSmileySpans(me.getNick_name(), true, 0));
		} else {
			// nickName.setText(sParser.addSmileySpans(me.getContacts_person_name(),
			// true, 0));
			nickName.setText(sParser.addSmileySpans(me.getTxInfor()
					.getContacts_person_name(), true, 0));

		}
		// 神聊号
		slNum.setText(Long.toString(me.partner_id));

		// 性别
		// sex.setText(me.sex == TX.MALE_SEX ?
		// this.getResources().getString(R.string.user_male) :
		// this.getResources().getString(
		// R.string.user_female));

		if (me.getSex() == TX.MALE_SEX) {
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_male));
			tv_sex.setText("♂");
		} else {
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_female));
			tv_sex.setText("♀");
		}
		// 年龄
		showBirthdayAge("" + me.birthday);

		// 等级
		// if(me.isDispalyLevel()){
		// userLevel.setVisibility(View.VISIBLE);
		// userLevel.setText(getString(R.string.level)+me.getLevel());
		// }else{
		userLevel.setVisibility(View.GONE);
		// }

		// // 签名
		// if (!Utils.isNull(me.user_sign)) {
		// sign.setText(sParser.addSmileySpans(me.user_sign, true, 0));
		// } else {
		// sign.setText("");
		// sign.setBackgroundDrawable(null);
		// }
		// if (me.getIsop() != null) {
		// int auth = Integer.valueOf(me.getIsop());
		int auth = me.getIsop();

		if (auth == 3 || auth == 4) {
			adminImage.setVisibility(View.VISIBLE);
		} else {
			adminImage.setVisibility(View.GONE);
		}
		// }
		switch (txMessage.msg_type) {
		// 打招呼l
		case TxDB.MSG_TYPE_GREET_SMS:
			if (isfriend) {
				okBtn.setText("开始聊天");
				// cancleBtn.setVisibility(View.GONE);
				noBtn.setVisibility(View.GONE);
			} else {
				okBtn.setText(R.string.recommended_access);
				// cancleBtn.setVisibility(View.VISIBLE);
				noBtn.setVisibility(View.VISIBLE);
			}

			break;
		// 通讯录
		case TxDB.MSG_TYPE_CONTACTS_SMS:
			if (isfriend) {
				okBtn.setText("开始聊天");
				// cancleBtn.setVisibility(View.GONE);
				noBtn.setVisibility(View.GONE);
			} else {
				okBtn.setText(R.string.recommended_access);
				// cancleBtn.setVisibility(View.VISIBLE);
				noBtn.setVisibility(View.VISIBLE);
			}
			break;
		// 新浪微博
		case TxDB.MSG_TYPE_SNS_SMS:
			if (isfriend) {
				okBtn.setText("开始聊天");
				// cancleBtn.setVisibility(View.GONE);
				noBtn.setVisibility(View.GONE);
			} else {
				okBtn.setText(R.string.recommended_access);
				// cancleBtn.setVisibility(View.VISIBLE);
				noBtn.setVisibility(View.VISIBLE);
			}
			break;
		// 有人想加你为好友
		case TxDB.MSG_TYPE_ADD_FRIEND_SMS:
			if (isfriend) {
				okBtn.setText("开始聊天");
				// cancleBtn.setVisibility(View.GONE);
				noBtn.setVisibility(View.GONE);
			} else {
				okBtn.setText(R.string.recommended_access);
				// cancleBtn.setVisibility(View.VISIBLE);
				noBtn.setVisibility(View.VISIBLE);
			}
			break;
		}
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 头像单击事件
		case R.id.con_info_user_head:
			if (!Utils.isNull(me.avatar_url)) {
				Intent intent = new Intent(UserInforRequestActivity.this,
						EditSendImg.class);
				intent.putExtra(EditSendImg.USER_ID, me.partner_id);
				intent.putExtra(EditSendImg.USER_URL, me.avatar_url);
				// intent.putExtra("no_own",true);//这个参数没有人接受，先注掉 2013.09.23
				intent.putExtra(EditSendImg.TOSTATE, EditSendImg.COME_AVATAR);
				startActivity(intent);
			}
			break;

		// case R.id.user_request_cancle:
		// UserInforRequestActivity.this.finish();
		// break;
		case R.id.user_request_ok:
			if (isfriend) {
				// TX tx =
				// TX.findTXByPartnerID4DB(txMessage.tcard_id);//不要直接访问数据库
				// 2013.10.17 shc
				TX tx = TX.tm.getTx(txMessage.tcard_id);

				Intent i = new Intent(UserInforRequestActivity.this,
						SingleMsgRoom.class);
				i.putExtra(Utils.MSGROOM_TX, tx.partner_id);
				startActivity(i);
				this.finish();
			} else {

				switch (txMessage.msg_type) {
				// 打招呼//新浪微博
				case TxDB.MSG_TYPE_GREET_SMS:
				case TxDB.MSG_TYPE_SNS_SMS:
					mSess.getSocketHelper().sendAddPartener(txMessage.tcard_id,
							txMessage.ac, "");
					break;
				// 有人想加你为好友
				case TxDB.MSG_TYPE_ADD_FRIEND_SMS:
					// 41号协议==加为好友
					mSess.getSocketHelper().sendAgreeMsg(true,
							txMessage.tcard_id, true, txMessage.ac);
					break;
				}
				showDialogTimer(this, 0, "操作中...", time_out,
						new BaseTimerTask() {
							public void run() {
								super.run();
								sendMsg(TIMER_OUT);
							}
						}).show();
			}
			break;

		case R.id.user_request_no:
			// 拒绝好友请求并添加到黑名单？？？
			mSess.getSocketHelper().sendAddBlackList(txMessage.tcard_id);
			showDialogTimer(this, 0, "操作中...", time_out, new BaseTimerTask() {
				public void run() {
					super.run();
					sendMsg(TIMER_OUT);
				}
			}).show();
			if (me != null) {
				// TX.updateTxInBlackList(me.partner_id,
				// me.in_black_list);//这时只是发送一个拉黑请求而已，不应该操作数据库吧？

				// ContentValues values = new ContentValues();
				// me.in_black_list=0;
				// values.put(TxDB.Tx.IN_BLACK_LIST, me.in_black_list);
				// //
				// UserInforRequestActivity.this.getContentResolver().update(TxDB.Tx.CONTENT_URI,
				// values, TxDB.Tx.TX_ID+"=?", new String[]{""+me.partner_id});
				// TX.updateTxValuesAndById_1(UserInforRequestActivity.this.getContentResolver(),
				// values, me.partner_id);
			}

			break;
		case R.id.userinfo_settings_photo:
			Intent iSupplement = new Intent(UserInforRequestActivity.this,
					UserInformationActivity.class);
			iSupplement
					.putExtra(
							UserInformationActivity.pblicInfo,
							TX.tm.isTxFriend(me.partner_id) == true ? UserInformationActivity.TUIXIN_USER_INFO
									: UserInformationActivity.NOT_TUIXIN_USER_INFO);
			iSupplement.putExtra(UserInformationActivity.UID, me.partner_id);
			startActivity(iSupplement);
			break;
		// case R.id.mRemoveBlack:
		// if(me!=null){
		// ContentValues values = new ContentValues();
		// me.in_black_list=-1;
		// values.put(TxDB.Tx.IN_BLACK_LIST, me.in_black_list);
		// UserInforRequestActivity.this.getContentResolver().update(TxDB.Tx.CONTENT_URI,
		// values, TxDB.Tx.TX_ID+"=?", new String[]{""+me.partner_id});
		// }
		// SocketHelper.getSocketHelper(txdata).sendRmvBlackList(txMessage.tcard_id);
		// break;
		//
		}
	}

	// 显示星座,年龄
	private void showBirthdayAge(String birthday) {
		if (!"0".equals(birthday) && birthday.length() == 8) {
			int nowyear = Integer.valueOf(birthday.substring(0, 4));
			int nowmonth = Integer.valueOf(birthday.substring(4, 6));
			int nowday = Integer.valueOf(birthday.substring(6, 8));

			if (nowyear <= currentYear) {
				int age = currentYear - nowyear;

				// if (age != -1) {
				// if (currentMonth + 1 < nowmonth) {
				// age--;
				// } else if (currentMonth + 1 == (nowmonth) && currentDay <
				// nowday) {
				// age--;
				// }
				// }
				if (age == -1) {
					age = 0;
				}
				if ("".equals(birthday))
					age = 0;
				tv_age.setText("" + age);

			}
		}else {
			tv_age.setText(""+0);
		}
	}

	private void sendMsg(int what) {
		Message msg = handler.obtainMessage();
		msg.what = what;
		handler.sendMessage(msg);
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			int num = msg.what;
			cancelDialogTimer();
			switch (num) {
			case ADD_FRIEND_SUCCESS:
				// txdata.showToast(R.string.recommended_success_add_friend);
				showToast(R.string.recommended_success_add_friend);
				okBtn.setText("开始聊天");
				// cancleBtn.setVisibility(View.GONE);
				noBtn.setVisibility(View.GONE);
				isfriend = true;
				break;
			case ADD_FRIEND_FAIL:
				// txdata.showToast(R.string.recommended_failed);
				showToast(R.string.recommended_failed);
				break;
			case BLACK_SUCCESS:
				// txdata.showToast(R.string.recommended_black);
				showToast(R.string.recommended_black);
				if (TXMessage.deleteByMsgId(
						UserInforRequestActivity.this.getContentResolver(),
						txMessage.msg_id) != 0) {

					Intent intent = new Intent(
							Constants.INTENT_ACTION_BLACK_DELETE_MESSAGE);
					intent.putExtra("message", txMessage);
					sendBroadcast(intent);
					UserInforRequestActivity.this.finish();
				}

				// TXMessage.deleteByMsgId(cr, id);
				break;
			case BLACK_FAIL:
				// txdata.showToast(R.string.recommended_failed);
				showToast(R.string.recommended_failed);
				break;
			case BLACK_FAIL_MORE:
				// txdata.showToast(R.string.recommended_black_more);
				showToast(R.string.recommended_black_more);
				break;
			case TEL_CHECK_TIMEOUT:
				Utils.startPromptDialog(UserInforRequestActivity.this,
						R.string.prompt, R.string.foreign_check_code_outtime);
				break;
			case SEND_REQUEST_SUCCESS:
				if (isOnclick && !isNotFirstShow) {
					isNotFirstShow = true;
					// txdata.showToast(R.string.recommended_send_success);
					showToast(R.string.recommended_send_success);
				}
				break;
			case TIMER_OUT:
				// txdata.showToast(R.string.foreign_check_code_outtime);
				showToast(R.string.foreign_check_code_outtime);
				break;
			case REFRESH:
				setData();
				break;
			case REFURBISH_UI:
				setData();
				break;

			}
		}
	};

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				if (result[0] != null && headImage.getTag() == (Long) result[1]) {
					headImage.setImageBitmap((Bitmap) result[0]);
				}

				break;
			}
			super.handleMessage(msg);
		}
	};
	private TextView userLevel;

	public void onResume() {
		registReceiver();
		super.onResume();
	}

	public void onStop() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}

		super.onStop();
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_USERINFO_RSP);
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_AGREE_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_OPT_BLACKLIST_RSP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_ADD_BUDDY.equals(intent.getAction())) {
				dealAddBuddy(serverRsp);
			} else if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent
					.getAction())) {
				dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_AGREE_ADD_BUDDY.equals(intent
					.getAction())) {
				dealAgreeAddBuddy(serverRsp);
			} else if (Constants.INTENT_ACTION_OPT_BLACKLIST_RSP.equals(intent
					.getAction())) {
				dealOptBlackList(serverRsp);
			} else if (Constants.INTENT_ACTION_USERINFO_RSP.equals(intent
					.getAction())) {
				// TX tx = Utils.getTX(intent);
				long partner_id = intent.getLongExtra(TX.EXTRA_TX,
						Utils.DEFAULT_NUMBER);
				dealUserInfo(partner_id);
			}
		}
	}

	private void dealUserInfo(long partner_id) {
		// String userid = prefs.getString(CommonData.USER_ID, "");
		String userid = mSess.mPrefMeme.user_id.getVal();
		if (partner_id != TX.TUIXIN_MAN && partner_id != TX.TUIXIN_FRIEND
				&& partner_id != Integer.parseInt(userid) && this.me != null
				&& this.txMessage.tcard_id == partner_id) {
			this.me = TX.tm.getTx(partner_id);
			Message msg = new Message();
			msg.what = REFURBISH_UI;
			handler.sendMessage(msg);
		}
	}

	private void dealAddBuddy(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				sendMsg(SEND_REQUEST_SUCCESS);
				break;
			}
			}
		}
	}

	private void dealSystemMsg(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_VERIFY_FRIEND: {
				if (serverRsp.getBoolean("agree", false)) {
					sendMsg(ADD_FRIEND_SUCCESS);
				}
				break;
			}
			}
		}
	}

	private void dealAgreeAddBuddy(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				sendMsg(ADD_FRIEND_SUCCESS);
				break;
			}
			case OPT_FAILED: {
				sendMsg(ADD_FRIEND_FAIL);
				break;
			}
			}
		}
	}

	// 把对方添加到黑名单或者从黑名单中删除，服务器的回应
	private void dealOptBlackList(ServerRsp serverRsp) {
		cancelDialog();
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				if (serverRsp.getInt("type", -1) == 0) {
					// me.setIn_black_list(TxDB.TX_IN_MY_BLACK_LIST);

					// ContentValues values = new ContentValues();
					// values.put(TxDB.TX_Friends.IS_STAR_FRIEND,
					// TxInfor.TX_IN_MY_BLACK_LIST);
					TX.tm.removeTxToBlack(me.partner_id);
				} else if (serverRsp.getInt("type", -1) == 1) {
					// me.setIn_black_list(TxDB.TX_NOT_IN_BLACK_LIST);

					TX.tm.changeTxToST(me.partner_id);

				}

				sendMsg(BLACK_SUCCESS);
				break;
			}
			case OPT_FAILED: {
				sendMsg(BLACK_FAIL);
				break;
			}
			case BUDDY_THAN_LIMIT: {
				sendMsg(BLACK_FAIL_MORE);
				break;
			}
			}
		}
	}

}
