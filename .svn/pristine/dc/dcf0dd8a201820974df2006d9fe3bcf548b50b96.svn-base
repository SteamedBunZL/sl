package com.shenliao.user.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.group.activity.GroupTip;
import com.shenliao.group.activity.GroupWarn;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.set.activity.TabMoreActivity;
import com.shenliao.set.activity.UserInfoJoinFriendActivity;
import com.shenliao.set.adapter.UserFavouriteGridViewAdapter;
import com.shenliao.user.adapter.AlbumGridViewAdapter;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditSendImg;
import com.tuixin11sms.tx.activity.GalleryUrlActivity;
import com.tuixin11sms.tx.activity.MyBlogActivity;
import com.tuixin11sms.tx.activity.SelectFriendListActivity;
import com.tuixin11sms.tx.activity.SingleMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.StatusCode;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.ContentTextWatcher;
import com.tuixin11sms.tx.utils.DialogButtonHandler;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 用户资料
 * 
 * @author xch
 */
public class UserInformationActivity extends BaseActivity implements
		OnClickListener {
	private static final String TAG = "UserInformationActivity";
	public final static String pblicInfo = "pblicinfo";
	public final static String seachtouser = "seachtouser";
	public final static String UID = "uid";
	private AlertDialog telDialog;
	public int goInPageState = 100;
	public static final int TUIXIN_USER_INFO = 99; // 神聊联系人进入的
	public static final int NOT_TUIXIN_USER_INFO = 101; // 通讯录进入的
	private final static int FLUSHFINISH = 98;
	private Intent intent;
	private long userId;
	private TX tx;
	// private Display display;
	// private ContentResolver cr;
	private SmileyParser sParser;// 解析表情
	private TextView tv_title_name;// 标题栏联系人姓名
	private ImageView headImage;// 联系人头像
	private TextView userName;// 联系人姓名
	private TextView tv_userId;// 神聊号
	// private TextView userSex;// 联系人性别
	private View user_info_sex_age;// 联系人性别和年龄父view
	private TextView tv_sex;// 联系人性别
	private TextView tv_age;// 联系人年龄
	private TextView userSign;// 联系人签名
	private TextView userConstellation;// 联系人星座
	private TextView userBloodType;// 联系人血型
	private TextView userProfession;// 联系人职业
	private TextView userArea;// 联系人地区
	private TextView userLanguage;// 语言
	private Button joinOrChatBtn;// 加为好友或开始聊天按钮
	private GridView favouriteGridView;// 兴趣爱好
	// private LinearLayout sexImage;// 性别图
	private ImageView adminImage;// 是否是管理员
	private GridView albumGridView;
	// private LinearLayout albumLinear;// 相册显示区域
	private TextView albumDefaultLinear;// 默认相册显示区域
	private LinearLayout ll_userinfor_favourite;// 相册显示区域
	// private ImageView mNextImage;
	private LinearLayout signLiear;
	private AlbumGridViewAdapter albumAdapter;
	private ImageView moreBtn;// 个人资料也右上角的“更多”按钮
	private String[] bloodtypes;// 血型集合 ,0是A，1是B，2是AB，3是O
	// private ScrollView scroll;
	public static final int[] constellationEdgeDay = { 21, 20, 21, 21, 22, 22,
			23, 24, 24, 24, 23, 22 };
	public static String[] constellationArr;
	private int currentYear;
	// private int currentMonth;
	// private int currentDay;
	// private UserFavouriteGridViewAdapter gridViewAdapter;//只有一个引用，修改为局部变量
	private static final int REFURBISH_UI = 64;// 刷新用户资料变量
	// 加为好友参数
	// private int inviteState;
	// private SharedPreferences prefs;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private String remarkName;
	private ArrayList<AlbumItem> aiList = new ArrayList<AlbumItem>();
	public static final int ADD_FRIEND_SUCCESS = 76;
	public static final int ADD_FRIEND_FAIL = 77;
	private LinearLayout mUserInfoLoading;
	private RelativeLayout mUserInfoTitle;
	private SocketHelper socketHelper;

	// private int deafaultHeadImage;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		setContentView(R.layout.activity_userinfo_detail);
		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// title上的返回按钮
				thisContext.finish();
			}
		});

		socketHelper = mSess.getSocketHelper();

		init();
		if (tx != null) {
			// 如果tx不为空，则给控件填值
			setData(false);// 第一次填值，非刷新
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		// scroll.smoothScrollTo(0, 0); // 解决scrollview与gridview嵌套不显示顶部问题
		registReceiver();// 注册广播，更新数据
		// inviteState = 0;
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

	@Override
	protected void onDestroy() {
		if (Utils.debug) {
			Log.i(TAG, "onDestory()");
			Log.i(TAG, "onDestroy-->"+tx.getPartner_id()+"地区为："+tx.getArea());
		}
		albumAdapter.stopAsyncload();// 退出个人详情，停止下载相册队列 2013.09.27 shc
		super.onDestroy();
	}

	/** 加载中 */
	private void load(long partner_id) {
		if (partner_id != TX.TUIXIN_MAN) {
			if (tx != null) {
				aiList = tx.getAlbum();
				// 此时还没有收到请求服务器获取个人资料的响应，先不处理相册
				albumAdapter.setList(aiList);
				albumAdapter.notifyDataSetChanged();
			}
			mUserInfoLoading.setVisibility(View.VISIBLE);
			mUserInfoTitle.setVisibility(View.GONE);
			moreBtn.setOnClickListener(null);
			headImage.setOnClickListener(null);
			joinOrChatBtn.setOnClickListener(this);
		} else {
			mUserInfoTitle.setVisibility(View.VISIBLE);
			moreBtn.setOnClickListener(this);
			headImage.setOnClickListener(this);
			joinOrChatBtn.setOnClickListener(this);
			albumGridView.setVisibility(View.GONE);
			albumDefaultLinear.setVisibility(View.VISIBLE);
			// 如果是神聊客服，则默认为好友，可以直接聊天
			goInPageState = TUIXIN_USER_INFO;
		}

	}

	/* 个人资料加载完成 */
	private void loadFinish() {
		mUserInfoLoading.setVisibility(View.GONE);
		mUserInfoTitle.setVisibility(View.VISIBLE);
		albumGridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> av, View view, int pos,
					long rowId) {
				// Intent intent = new Intent(UserInformationActivity.this,
				// UserAlbumActivity.class);
				Intent intent = new Intent(UserInformationActivity.this,
						GalleryUrlActivity.class);
				intent.putExtra(GalleryUrlActivity.ALBUM_LIST, aiList);
				intent.putExtra(GalleryUrlActivity.POSITION, pos);
				intent.putExtra(GalleryUrlActivity.UID, tx.partner_id);
				startActivity(intent);
			}
		});
		moreBtn.setOnClickListener(this);
		headImage.setOnClickListener(this);
	}

	// 初始化
	private void init() {
		intent = this.getIntent();
		userId = intent.getLongExtra(UID, Utils.DEFAULT_NUMBER);
		tx = TX.tm.getTx(userId);// 换成缓存中的tx
		if (Utils.debug) {
			Log.e(TAG, "刚进来头像为：" + (tx == null ? "null" : tx.getAvatar_url()));
		}
		headImage = (ImageView) findViewById(R.id.iv_user_info_head);
		signLiear = (LinearLayout) findViewById(R.id.ll_user_sign);
		// 瞬间
		blogLiear = (LinearLayout) findViewById(R.id.ll_user_blog);
		TextView tv_user_blog = (TextView) findViewById(R.id.tv_user_blog);
		tv_user_blog.setText("Ta的瞬间");
		tv_user_blog.setTextColor(getResources().getColor(
				R.color.sub_title_text_color));
		blogLiear.setOnClickListener(this);

		// albumLinear = (LinearLayout) findViewById(R.id.ll_user_info_album);
		albumDefaultLinear = (TextView) findViewById(R.id.tv_user_infor_no_album_style);
		// sexImage = (LinearLayout) findViewById(R.id.ll_sex_age_user_infor);
		tv_title_name = (TextView) findViewById(R.id.tv_user_infor_title);
		userName = (TextView) findViewById(R.id.user_info_name);
		tv_userId = (TextView) findViewById(R.id.user_info_id_content);

		// userSex = (TextView) findViewById(R.id.user_info_sex);
		tv_sex = (TextView) findViewById(R.id.tv_user_info_sex);
		tv_age = (TextView) findViewById(R.id.tv_user_info_age);
		user_info_sex_age = findViewById(R.id.rl_user_info_sex_age);
		userSign = (TextView) findViewById(R.id.tv_user_infor_sign_content);
		userConstellation = (TextView) findViewById(R.id.user_info_constellation);
		userBloodType = (TextView) findViewById(R.id.user_info_blood);
		userProfession = (TextView) findViewById(R.id.user_info_profession);
		userArea = (TextView) findViewById(R.id.user_info_area);
		joinOrChatBtn = (Button) findViewById(R.id.user_Chat_btn);
		ll_userinfor_favourite = (LinearLayout) findViewById(R.id.ll_userinfor_favourite);
		favouriteGridView = (GridView) findViewById(R.id.user_info_favourite_gridView);
		moreBtn = (ImageView) findViewById(R.id.iv_more_operate);
		mUserInfoLoading = (LinearLayout) findViewById(R.id.userinfo_loading);
		mUserInfoTitle = (RelativeLayout) findViewById(R.id.userinfo_title);
		adminImage = (ImageView) findViewById(R.id.iv_admin_icon);
		adminImage.setVisibility(View.GONE);// 管理员图标默认不显示
		userLanguage = (TextView) findViewById(R.id.user_language);
		userLevel = (TextView) findViewById(R.id.user_info_level);
		albumDefaultLinear.setVisibility(View.GONE);
		albumGridView = (GridView) findViewById(R.id.user_info_gridView);

		albumAdapter = new AlbumGridViewAdapter(this, null);
		albumGridView.setAdapter(albumAdapter);
		
//		goInPageState = intent.getIntExtra(pblicInfo, NOT_TUIXIN_USER_INFO);//后续去掉外部开启该activity时的是否好友判定 2014.05.28 shc
		
		boolean isFriend = TX.tm.isTxFriend(userId);//是否是好友
		goInPageState = isFriend?TUIXIN_USER_INFO:NOT_TUIXIN_USER_INFO;
		
		load(userId);
		bloodtypes = this.getResources().getStringArray(
				R.array.bloodtype_dialog_items);
		constellationArr = getResources().getStringArray(
				R.array.constellation_dialog_items);

		sParser = Utils.getSmileyParser(this);
		Calendar calendar = Calendar.getInstance();
		currentYear = calendar.get(Calendar.YEAR);

		if (goInPageState == NOT_TUIXIN_USER_INFO) {
			joinOrChatBtn.setText(R.string.seach_select_ok);
		} else if (goInPageState == TUIXIN_USER_INFO) {

			joinOrChatBtn.setText(R.string.tuixin_chat_btn);
		}
		if (userId == TX.TUIXIN_MAN || userId == TX.TUIXIN_FRIEND) {
			moreBtn.setVisibility(View.GONE);
		} else {
			moreBtn.setVisibility(View.VISIBLE);
		}

		socketHelper.sendGetUserInfor(userId);

	}

	/** 设置用户信息和标题栏的昵称 */
	private void setUserNickName(String nickName, boolean needParse) {
		if (needParse) {
			userName.setText(sParser.addSmileySpans(nickName, true, 0));
			tv_title_name.setText(sParser.addSmileySpans(nickName, true, 0));
		} else {
			userName.setText(nickName);
			tv_title_name.setText(nickName);
		}
	}

	// 设置数据
	private void setData(boolean isFlush) {
		// 神聊号
		tv_userId.setText(Long.toString(userId));

		// 判断是否是好友
		if (goInPageState == NOT_TUIXIN_USER_INFO) {
			joinOrChatBtn.setText(R.string.seach_select_ok);
		} else if (goInPageState == TUIXIN_USER_INFO) {

			joinOrChatBtn.setText(R.string.tuixin_chat_btn);
		}

		// 性别
		if (tx.getSex() == TX.MALE_SEX) {
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_male));
			tv_sex.setText("♂");
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			user_info_sex_age.setBackgroundColor(getResources().getColor(
					R.color.user_sex_bg_color_female));
			tv_sex.setText("♀");
			defaultHeaderImg = defaultHeaderImgFemale;
		}
		// 头像
		if (tx.partner_id == TX.TUIXIN_MAN) {
			headImage.setImageResource(R.drawable.tuixin_manage);
		} else {
			//判断是否在缓存
			if (mSess.avatarDownload.getPartnerCachedBitmap(tx.partner_id)!=null) {
				headImage.setImageBitmap(mSess.avatarDownload.getPartnerCachedBitmap(tx.partner_id));
			}else {
				headImage
				.setImageBitmap(mSess.getDefaultPartnerAvatar(tx.getSex()));
			}
			if (tx.avatar_url != null && !TextUtils.isEmpty(tx.avatar_url)&&isFlush) {
				headImage.setTag(tx.partner_id);
				mSess.avatarDownload.downAvatarForUserInfo(tx.avatar_url, tx.partner_id,
						headImage, avatarHandler);
			}

		}
		// 昵称
		if (tx.partner_id == TX.TUIXIN_MAN) {
			setUserNickName(TX.tm.getTxMan().getNick_name(), false);

		} else {
			if (!Utils.isNull(tx.getRemarkName())) {
				setUserNickName(tx.getRemarkName(), true);

			} else {
				if (!Utils.isNull(tx.getNick_name())) {
					setUserNickName(tx.getNick_name(), true);
				} else {
					setUserNickName(tx.getTxInfor().getContacts_person_name(),
							true);
				}
			}
		}
		
		
		if(tx.isDispalyLevel()){
			userLevel.setVisibility(View.VISIBLE);
			//等级
			userLevel.setText(getString(R.string.level)+tx.getLevel());
		}else{
			userLevel.setVisibility(View.GONE);
		}
		
		
		
		if(Utils.debug){
			Log.i(TAG, "setData-->"+tx.getPartner_id()+"地区为："+tx.getArea());
		}
		// 地区
		if(!TextUtils.isEmpty(tx.area)){
			if(tx.area.contains(";")){
				userArea.setText(tx.area);
			}else {
				List<String> mlist = StringUtils.str2List(tx.area);
				userArea.setText(DataContainer.getAreaNameByIds(mlist
						.toArray(new String[0])));
			}
		} else {
			userArea.setText("-");
		}

		// 语言
		if (!Utils.isNull(tx.getLanguages())) {
			List<String> mlist = StringUtils.str2List(tx.getLanguages());
			if (mlist.contains("0")) {
				mlist.remove("0");
			}
			if (mlist != null && mlist.size() > 0) {
				userLanguage.setText(DataContainer.getLangNameByIds(mlist
						.toArray(new String[0])));

			} else {
				userLanguage.setText("-");
			}

		} else {
			userLanguage.setText("-");
		}

		// 年龄
		showBirthdayAge("" + tx.birthday);
		// 血型
		showBloodType(tx.bloodType);
		// 职业
		if (!Utils.isNull(tx.job)) {
			userProfession.setText(sParser.addSmileySpans(tx.job, true, 0));
		} else {
			userProfession.setText("-");
		}
		// 签名
		if (!Utils.isNull(tx.sign)) {
			signLiear.setVisibility(View.VISIBLE);
			userSign.setText(sParser.addSmileySpans(tx.sign, true, 0));
		} else {
			signLiear.setVisibility(View.GONE);
		}

		// 是否是管理员
		int auth = tx.getIsop();
		if (auth == 3 || auth == 4) {
			adminImage.setVisibility(View.VISIBLE);
		} else {
			adminImage.setVisibility(View.GONE);
		}

		if (isFlush) {
			// 当第二次刷新的时候再做判断显示。防止布局向上滚动。
			// 兴趣
			if (tx.hobby.equals("0,")) {
				tx.hobby = "";
			}
			if (!TextUtils.isEmpty(tx.hobby)) {
				ll_userinfor_favourite.setVisibility(View.VISIBLE);
				UserFavouriteGridViewAdapter gridViewAdapter = new UserFavouriteGridViewAdapter(
						UserInformationActivity.this,
						StringUtils.str2List(tx.hobby));
				favouriteGridView.setAdapter(gridViewAdapter);
			}
		}

	}

	// 显示血型
	private void showBloodType(int blood_type) {
		int ibloodType = blood_type;
		if (ibloodType == -1) {
			userBloodType.setText("-");
		} else if (ibloodType >= bloodtypes.length) {
			userBloodType.setText(bloodtypes[0] + "型");
		} else {
			userBloodType.setText(bloodtypes[ibloodType] + "型");
		}
	}

	private int userAge;

	// 显示星座,年龄
	private void showBirthdayAge(String birthday) {
		if (!"0".equals(birthday) && birthday.length() == 8) {
			int nowyear = Integer.valueOf(birthday.substring(0, 4));
			int xingzuomonth = Integer.valueOf(birthday.substring(4, 6)) - 1;
			int nowmonth = Integer.valueOf(birthday.substring(4, 6));
			int nowday = Integer.valueOf(birthday.substring(6, 8));

			if (nowday < constellationEdgeDay[xingzuomonth]) {
				xingzuomonth = xingzuomonth - 1;
				if (xingzuomonth < 0) {
					xingzuomonth = 11;
				}
			}

			if (xingzuomonth >= 0) {
				userConstellation.setText(constellationArr[xingzuomonth]);
			} else {
				userConstellation.setText("-");
			}
			if (nowyear <= currentYear) {
				int age = currentYear - nowyear;

				// 年龄计算按年去算。
				// if (age != -1) {
				// if (currentMonth + 1 < nowmonth) {
				// age--;
				// } else if (currentMonth + 1 == (nowmonth)
				// && currentDay < nowday) {
				// age--;
				// }
				// }
				if (age == -1) {
					age = 0;
				}
				if ("".equals(birthday))
					age = 0;
				tv_age.setText("" + age);
				tx.setAge(age);
				userAge = age;

			}
		} else {
			userConstellation.setText("-");
			tv_age.setText("0");// 没有设置生日，就显示为0
		}
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 加为好友和开始聊天按钮事件
		case R.id.user_Chat_btn:
			if (goInPageState == NOT_TUIXIN_USER_INFO) {
				Intent intent = new Intent(UserInformationActivity.this,
						UserInfoJoinFriendActivity.class);
				intent.putExtra(UserInfoJoinFriendActivity.INFOTX, tx);
				startActivity(intent);

			} else if (goInPageState == TUIXIN_USER_INFO) {
				// if(getCallingActivity().getComponentName().getShortClassName().equals(SingleMsgRoom.class.getSimpleName())){
				// //如果是从SingleMsgRoom过来，再次开启SingleMsgRoom时不重复开启，会因为两个相同的activity开启时onCreat和onDestroy生命周期交叉出错
				// }else {
				Intent nextintent = new Intent(UserInformationActivity.this,
						SingleMsgRoom.class);
				nextintent.putExtra(Utils.MSGROOM_TX, tx.partner_id);
				startActivity(nextintent);
				// }
				finish();
			}
			break;
		// 头像单击事件
		case R.id.iv_user_info_head:
			if (!Utils.isNull(tx.avatar_url)
					&& tx.avatar_url.length() > Utils.CAN_NOT_DOWNLOAD_LENGTH) {
				Intent intent = new Intent(UserInformationActivity.this,
						EditSendImg.class);
				intent.putExtra(EditSendImg.USER_ID, tx.partner_id);
				intent.putExtra(EditSendImg.USER_URL, tx.avatar_url);
				// intent.putExtra("no_own", true);//这个参数没有人接受，先注掉 2013.09.23
				intent.putExtra(EditSendImg.TOSTATE, EditSendImg.COME_AVATAR);
				startActivity(intent);
			} else {
				if (tx.partner_id == TX.TUIXIN_MAN) {
					Intent intent = new Intent(UserInformationActivity.this,
							EditSendImg.class);
					intent.putExtra(EditSendImg.USER_ID, tx.partner_id);
					intent.putExtra(EditSendImg.USER_URL, tx.avatar_url);
					intent.putExtra(EditSendImg.TOSTATE,
							EditSendImg.COME_AVATAR);
					startActivity(intent);
				}
			}
			break;
		// 更多按钮单击事件
		case R.id.iv_more_operate:
			creatUpMorePop();
			break;
		case R.id.ll_user_blog:
			Intent iBlog = new Intent(UserInformationActivity.this,
					MyBlogActivity.class);
			iBlog.putExtra(MyBlogActivity.ISMY, false);
			iBlog.putExtra(MyBlogActivity.TXINFO, tx);
			startActivity(iBlog);
			break;
		}
	}

	// 注册广播方法
	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_AGREE_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_ADD_BUDDY);
			filter.addAction(Constants.INTENT_ACTION_USERINFO_RSP);
			filter.addAction(Constants.INTENT_ACTION_OPT_BLACKLIST_RSP);
			filter.addAction(Constants.INTENT_ACTION_GET_ALBUM_RSP);
			filter.addAction(Constants.INTENT_ACTION_SET_REMARK_NAME_RSP);
			filter.addAction(Constants.INTENT_ACTION_OPT_SET_STAR_FRIEND_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent.getAction())) {
				dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_AGREE_ADD_BUDDY.equals(intent
					.getAction())) {
				dealAgreeAddBuddy(serverRsp);
			} else if (Constants.INTENT_ACTION_USERINFO_RSP.equals(intent
					.getAction())) {
				long partner_id = intent.getLongExtra(TX.EXTRA_TX,
						Utils.DEFAULT_NUMBER);
				dealUserInfo(partner_id);// 接受广播，处理用户信息
			} else if (Constants.INTENT_ACTION_OPT_BLACKLIST_RSP.equals(intent
					.getAction())) {
				dealOptBlackList(serverRsp);
			} else if (Constants.INTENT_ACTION_GET_ALBUM_RSP.equals(intent
					.getAction())) {
				if (tx != null) {
					// tx不为空，则操作更新该用户的相册
					dealGetAlbum(serverRsp);
				}
			} else if (Constants.INTENT_ACTION_SET_REMARK_NAME_RSP
					.equals(intent.getAction())) {
				dealSetRemarkName(serverRsp);
			} else if (Constants.INTENT_ACTION_OPT_SET_STAR_FRIEND_RSP
					.equals(intent.getAction())) {
				// 处理设置星标好友结果
				dealSetStarFriend(serverRsp);
			}
		}
	}

	/**
	 * 处理设置备注名
	 * 
	 * @param serverRsp
	 */
	private void dealSetRemarkName(ServerRsp serverRsp) {
		if (Utils.debug) {
			Log.i(TAG, "收到设置备注名的广播");
		}
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK:
				// 一定是好友，不是好友设置不了“备注名”
				ContentValues values = new ContentValues();
				values.put(Tx.REMARK_NAME, remarkName);
				TX.tm.updateTXInforByValues(tx.partner_id, values);

				// TX.updateRemarkName(tx.partner_id, remarkName);

				tx.setRemarkName(remarkName);
				// userName.setText(sParser.addSmileySpans(Utils
				// .isNull(remarkName) ? tx.getNick_name() : remarkName,
				// true, 0));
				setUserNickName(Utils.isNull(remarkName) ? tx.getNick_name()
						: remarkName, true);
				break;
			default:
			}
		}
	}

	/**
	 * 处理设置星标好友
	 * 
	 * @param serverRsp
	 */
	private void dealSetStarFriend(ServerRsp serverRsp) {
		if (Utils.debug) {
			Log.i(TAG, "收到设置星标好友的广播");
		}
		cancelDialog();
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				int setStarFriendResult = serverRsp.getInt("attr", -1);
				if (Utils.debug)
					Log.i(TAG, "服务器返回的星标好友结果：" + setStarFriendResult);
				// tx.setStarFriend(setStarFriendResult);

				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.IS_STAR_FRIEND, setStarFriendResult);
				TX.tm.addTBTXId(tx.partner_id, setStarFriendResult);// 更新星标好友值
				TX.tm.updateTXInforByValues(tx.partner_id, values);

				if (Utils.debug) {
					// 验证一下星标好友的值设置成功没有？
					Log.i(TAG, "检查此时--" + tx.partner_id
							+ "--的是否是星标好友(starFriend)的值----------:"
							+ tx.getTxInfor().getStarFriend());

				}
				// TX.updateTxIsStarFriend(tx.partner_id,
				// setStarFriendResult);//不能直接更新数据库吧 2013.10.17 shc
				showToast((setStarFriendResult == TxInfor.TX_STAR_FRIEND ? "已标为"
						: "已取消")
						+ "星标好友");
				break;
			}
			case NOT_FRIEND: {
				Utils.startPromptDialog(UserInformationActivity.this,
						R.string.userinfo_black_friend_result2,
						R.string.userinfo_star_friend_not_friend);
				break;
			}

			case OPT_FAILED: {
				Utils.startPromptDialog(UserInformationActivity.this,
						R.string.userinfo_black_friend_result2,
						R.string.userinfo_server_busy);
				break;
			}
			}

		}
	}

	@SuppressWarnings("unchecked")
	private void dealGetAlbum(ServerRsp serverRsp) {
		// 收到处理头像的广播
		if (Utils.debug) {
			Log.i(TAG, "收到处理相册的广播");
			Log.i(TAG, "dealGetAlbum-->"+tx.getPartner_id()+"地区为："+tx.getArea());
		}
		long uid = serverRsp.getBundle().getLong("uid");
		if (uid != tx.partner_id)
			return;

		if (serverRsp.getStatusCode() != null
				&& serverRsp.getStatusCode() == StatusCode.RSP_OK) {
			aiList = (ArrayList<AlbumItem>) serverRsp.getBundle()
					.getSerializable("aiList");
			if (aiList.size() == 0) {
				albumGridView.setVisibility(View.GONE);
				albumDefaultLinear.setVisibility(View.VISIBLE);
			} else {
				albumGridView.setVisibility(View.VISIBLE);
				albumDefaultLinear.setVisibility(View.GONE);
				// 应该有照片才设置adapter吧？
				tx.setAlbum(aiList);
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.ALBUM, tx.getAlubumString());
				TX.tm.updateTx(tx.partner_id, values);

				albumAdapter.setList(aiList);
				albumAdapter.notifyDataSetChanged();
			}
			handler.sendEmptyMessage(FLUSHFINISH);
		} else if (serverRsp.getStatusCode() != null
				&& serverRsp.getStatusCode() == StatusCode.USERALBUM_NO_EXIST) {
			albumGridView.setVisibility(View.GONE);
			albumDefaultLinear.setVisibility(View.VISIBLE);
			handler.sendEmptyMessage(FLUSHFINISH);
		} else if (serverRsp.getStatusCode() != null
				&& serverRsp.getStatusCode() == StatusCode.OPT_FAILED) {
			// albumDefaultLinear.setVisibility(View.VISIBLE);
			// handler.sendEmptyMessage(FLUSHFINISH);
		}
	}

	// 处理黑名单返回
	private void dealOptBlackList(ServerRsp serverRsp) {
		if (Utils.debug) {
			Log.i(TAG, "收到处理黑名单的广播");
		}
		cancelDialog();
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				String result = null;
				if (serverRsp.getInt("type", -1) == 0) {
					// 我把对方添加进黑名单
					ContentValues values = new ContentValues();
					values.put(TxDB.TX_Friends.TX_TYPE, TxInfor.TX_TYPE_BLACK);
					values.put(TxDB.TX_Friends.IS_STAR_FRIEND,
							TxInfor.TX_NOT_FRIEND);

					TX.tm.removeTxToBlack(tx.partner_id);

					result = "加入";

					// 把开始聊天变为加为好友
					joinOrChatBtn.setText(R.string.seach_select_ok);
					goInPageState = NOT_TUIXIN_USER_INFO;

				} else if (serverRsp.getInt("type", -1) == 1) {
					// 我把对方移除黑名单
					TX.tm.changeTxToST(tx.partner_id);

					result = "移除";
				}

				tx = TX.tm.getTx(tx.partner_id);
				showToast("成功" + result + "黑名单");

				break;
			}
			case OPT_FAILED: {
				Utils.startPromptDialog(UserInformationActivity.this,
						R.string.userinfo_black_friend_result2,
						R.string.userinfo_server_busy);
				break;
			}
			case BUDDY_THAN_LIMIT: {
				Utils.startPromptDialog(UserInformationActivity.this,
						R.string.userinfo_black_friend_result2,
						R.string.userinfo_server_more);
				break;
			}
			}
		}
	}

	// 处理用户返回信息方法
	private void dealUserInfo(long partner_id) {
		if (Utils.debug) {
			Log.i(TAG, "收到处理用户返回信息的广播");
		}

		if (partner_id != TX.TUIXIN_MAN && partner_id != TX.TUIXIN_FRIEND
				&& partner_id != TX.SL_GROUP_NOTICE && partner_id != TX.SL_SAFE
				&& partner_id != TX.tm.getUserID() && userId == partner_id) {
			if (Utils.debug)
				Log.i(TAG, "是这个联系人的广播，需要更新UI");

			Message msg = handler.obtainMessage();
			msg.what = REFURBISH_UI;
			// msg.obj = tx;
			handler.sendMessage(msg);
			// sendMsg(REFURBISH_UI);
		}else {
			if (Utils.debug){
				Log.i(TAG, "dealUserInfo-->"+tx.getPartner_id()+"地区为："+tx.getArea());
				Log.i(TAG, "不是这个联系人的详细信息广播");
			}
		}

	}

	// 同意添加好友应答结果处理
	private void dealAgreeAddBuddy(ServerRsp serverRsp) {
		if (Utils.debug) {
			Log.i(TAG, "收到处理好友应答结果的广播");
		}
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

	// 处理系统广播结果
	private void dealSystemMsg(ServerRsp serverRsp) {
		if (Utils.debug) {
			Log.i(TAG, "收到处理系统的广播");
		}
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

	// handler发消息方法
	private void sendMsg(int what) {
		Message msg = handler.obtainMessage();
		msg.what = what;
		handler.sendMessage(msg);
	}

	// handler
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			cancelDialogTimer();
			int num = msg.what;
			switch (num) {
			// 同意添加好友应答成功
			case ADD_FRIEND_SUCCESS:
				joinOrChatBtn.setText(R.string.tuixin_chat_btn);
				joinOrChatBtn.invalidate();
				goInPageState = TUIXIN_USER_INFO;
				Toast.makeText(UserInformationActivity.this, "成功加为好友",
						Toast.LENGTH_SHORT).show();
				break;
			// 加好友失败
			case ADD_FRIEND_FAIL:
				showToast(R.string.recommended_failed);
				// txdata.showToast(R.string.recommended_failed);
				break;
			// 返回用户信息 刷新界面
			case REFURBISH_UI:
				// TX ttx = (TX) msg.obj;//TODO 不从这里取，这个发信息的操作还需要更改 2014.03.14
				// shc
				TX ttx = TX.tm.getTx(userId);
				if (ttx != null) {
					// flushUserInfo(ttx);
					tx = ttx;
					setData(true);
					//更新该联系人的消息会话
					MsgStat.updateMsgStatByTX(mSess.getContentResolver(), tx);
				}

				break;
			case FLUSHFINISH:
				loadFinish();
				break;
			}
			super.handleMessage(msg);
		}
	};
	// handler
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result2 = (Object[]) msg.obj;
				if (result2[0] != null) {
					headImage.setImageBitmap((Bitmap) result2[0]);
				} else {
					headImage.setImageBitmap(mSess.getDefaultPartnerAvatar(tx
							.getSex()));
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 点击更多按钮弹出的对话框参数
	private PopupWindow upMorePopWindow;
	private TextView tv_popReport;// 举报
	private TextView tv_popBlockId;// 封id
	private TextView tv_popBlockDevice;// 封设备
	private TextView tv_popAddBlackList;// 黑名单
	private TextView tv_popSetStarFriend;// 设置为星标好友
	private TextView tv_popRecommend;// 推荐
	private TextView tv_popMarkName;// 备注名
	private TextView tv_popWarn;// 警告
	private TextView tv_popCancle;// 取消

	// 各种线
	private ImageView iv_MarkNameLine;
	private ImageView iv_RecommendLine;
	private ImageView iv_AddBlackListLine;
	private ImageView iv_SetStarFriendLine;
	private ImageView iv_ReportLing;
	private ImageView iv_WarnLine;
	private ImageView iv_BlockIdLine;
	private ImageView iv_BlockDeviceLine;
	private LinearLayout blogLiear;
	private TextView userLevel;

	// private RelativeLayout uplistshutupline;

	/** 点击右上角的“更多”按钮，创建并显示一个popupWindow，显示更多操作 */
	public void creatUpMorePop() {

		if (upMorePopWindow == null) {
			LayoutInflater mInflate = LayoutInflater
					.from(UserInformationActivity.this);
			View popupWindow_view = mInflate.inflate(
					R.layout.sl_group_userinfo_more_pop, null);

			upMorePopWindow = new PopupWindow(popupWindow_view,
					(int) (Utils.SreenW * 0.55),
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例

			upMorePopWindow.setAnimationStyle(R.style.chat_up_anim);

			upMorePopWindow.setFocusable(true);

			upMorePopWindow.setBackgroundDrawable(new BitmapDrawable());
			tv_popMarkName = (TextView) popupWindow_view
					.findViewById(R.id.tv_more_item_markName);
			tv_popRecommend = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_item_recommend);
			tv_popAddBlackList = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_item_black);
			tv_popSetStarFriend = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_item_star);
			tv_popCancle = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_item_cancle);
			tv_popReport = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_item_report);
			tv_popBlockId = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_block_id);
			tv_popBlockDevice = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_block_device);
			tv_popWarn = (TextView) popupWindow_view
					.findViewById(R.id.userinfo_more_warn);

			iv_MarkNameLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item1_line);
			iv_RecommendLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item2_line);
			iv_AddBlackListLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item3_line);
			iv_SetStarFriendLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item4_line);
			iv_ReportLing = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item5_line);
			iv_WarnLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item6_line);
			iv_BlockIdLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item7_line);
			iv_BlockDeviceLine = (ImageView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item8_line);

			// 备注名
			tv_popMarkName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					showDialog(R.string.user_markName, tx.getRemarkName(),
							tx.getRemarkName(), tv_popMarkName, 999);
				}
			});

			// 加入黑名单
			tv_popAddBlackList.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					// if (tx != null
					// && tx.getIn_black_list() == TxDB.TX_NOT_IN_BLACK_LIST) {
					if (tx != null && !tx.getTxInfor().isBlackType()) {
						// 加入黑名单？？？
						AlertDialog.Builder newTelDialog = new AlertDialog.Builder(
								UserInformationActivity.this);
						newTelDialog.setTitle(R.string.userinfo_black_friend);
						newTelDialog
								.setMessage(R.string.userinfo_black_friend_result);
						newTelDialog.setPositiveButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// if(tx.in_black_list==-1){
										mSess.getSocketHelper()
												.sendAddBlackList(tx.partner_id);

										// TX.updateTxInBlackList(tx.partner_id,
										// 0);//这是只是发请求，应该不能改数据库吧？2013.10.17 shc

										// ContentValues values = new
										// ContentValues();
										// values.put(TxDB.Tx.IN_BLACK_LIST,
										// 0);//把黑名单设置为0，0代表我将此人加入黑名单
										// // cr.update(TxDB.Tx.CONTENT_URI,
										// values,
										// // TxDB.Tx.TX_ID + "=?",
										// // new String[] { ""
										// // + tx.partner_id });S
										// TX.updateTxValuesAndById_1(cr,
										// values,tx.partner_id);
										tv_popAddBlackList
												.setText(R.string.no_black_btn);
									}
								});
						newTelDialog.setNegativeButton(R.string.cancel,
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.cancel();
									}
								});
						newTelDialog.show();
					} else {
						// 移出黑名单？？？
						socketHelper.sendRmvBlackList(tx.partner_id);
						// TX.updateTxInBlackList(tx.partner_id,
						// -1);//这里只是发请求，应该不能改数据库吧？ 2013.10.17 shc

						// ContentValues values = new ContentValues();
						// values.put(TxDB.Tx.IN_BLACK_LIST, -1);
						// // cr.update(TxDB.Tx.CONTENT_URI, values,
						// TxDB.Tx.TX_ID
						// // + "=?", new String[] { "" + tx.partner_id });
						// TX.updateTxValuesAndById_1(cr, values,
						// tx.partner_id);
						if (Utils.debug) {
							// 这个查数据库只为验证in_black_list的值
							// TX t =
							// TX.findTXByPartnerID4DB(tx.partner_id);//不要直接访问数据库
							// 2013.10.17 shc
							TX t = TX.tm.getTx(tx.partner_id);

							// if (Utils.debug)
							Log.i(TAG, "检查此时--" + tx.partner_id
									+ "--的是否黑名单(in_black_list)的值----------:"
									+ t.getTxInfor().isBlackType());

						}
						tv_popAddBlackList.setText(R.string.black_btn);
					}

				}
			});
			tv_popSetStarFriend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					if (tx != null
							&& TX.tm.getStarFriendAttr(tx.partner_id) == 0) {
						// 设置为星标好友
						socketHelper.sendSetStarFriend(tx.partner_id);
					} else {
						// 取消设置为星标好友
						socketHelper.sendCancelStarFriend(tx.partner_id);
					}

				}
			});

			// 推荐好友
			tv_popRecommend.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					Intent intent = new Intent(UserInformationActivity.this,
							SelectFriendListActivity.class);

					intent.putExtra(SelectFriendListActivity.CHAT_TYPE,
							SelectFriendListActivity.CHAT_TYPE_CARD);

					intent.putExtra(
							SelectFriendListActivity.CHAT_TYPE_CARD_TYPE,
							SelectFriendListActivity.CHAT_TYPE_CARD_TUIXIN);

					startActivityForResult(intent,
							SelectFriendListActivity.CHAT_TYPE_CARD);

				}
			});

			// 封设备
			tv_popBlockDevice.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					GroupUtils.showDialog(UserInformationActivity.this, "警告",
							"是否确定将" + tx.getNick_name() + "处以封设备的处罚?",
							R.string.dialog_okbtn, R.string.dialog_nobtn,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									socketHelper.sendBlock(tx.partner_id, true);
								}
							});
				}
			});
			// 封id操作
			tv_popBlockId.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					GroupUtils.showDialog(UserInformationActivity.this, "警告",
							"是否确定将" + tx.getNick_name() + "处以封ID的处罚?",
							R.string.dialog_okbtn, R.string.dialog_nobtn,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									socketHelper
											.sendBlock(tx.partner_id, false);
								}
							});
				}
			});
			// 举报用户
			tv_popReport.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					Intent intent = new Intent(UserInformationActivity.this,
							GroupTip.class);
					intent.putExtra(GroupTip.UID, tx.partner_id);
					intent.putExtra("age", userAge);
					startActivity(intent);

				}
			});
			// 警告用户
			tv_popWarn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
					Intent i = new Intent(UserInformationActivity.this,
							GroupWarn.class);
					i.putExtra("uid", tx.partner_id);
					i.putExtra("age", userAge);
					startActivity(i);
				}
			});
			// 取消
			tv_popCancle.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (upMorePopWindow != null) {
						upMorePopWindow.dismiss();
					}
				}
			});

			upMorePopWindow.setTouchInterceptor(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {

					}
					return false;
				}
			});

			// upMorePopWindow.update();
			//
			// upMorePopWindow.showAsDropDown(moreBtn,
			// -(int) (display.getWidth() * 0.4), 0);

		}

		// else {
		if (upMorePopWindow.isShowing()) {

			upMorePopWindow.dismiss();

		} else {
			{
				// 设置popupWindow显示的内容
				// if (tx.getIn_black_list() == TxDB.TX_NOT_IN_BLACK_LIST) {
				// tv_popAddBlackList.setText(R.string.black_btn);
				// } else if (tx.getIn_black_list() == 0) {
				// tv_popAddBlackList.setText(R.string.no_black_btn);
				// }
				if (tx.getTxInfor().isBlackType()) {
					tv_popAddBlackList.setText(R.string.no_black_btn);
				} else {
					tv_popAddBlackList.setText(R.string.black_btn);
				}

				tv_popAddBlackList.setVisibility(View.VISIBLE);
				iv_AddBlackListLine.setVisibility(View.VISIBLE);
				tv_popReport.setVisibility(View.VISIBLE);
				iv_ReportLing.setVisibility(View.VISIBLE);
				tv_popCancle.setVisibility(View.VISIBLE);
				if (!TX.tm.isTxFriend(tx.partner_id)) {
					// 非好友
					tv_popSetStarFriend.setVisibility(View.GONE);
					iv_SetStarFriendLine.setVisibility(View.GONE);

					tv_popBlockId.setVisibility(View.GONE);
					iv_BlockIdLine.setVisibility(View.GONE);

					tv_popBlockDevice.setVisibility(View.GONE);
					iv_BlockDeviceLine.setVisibility(View.GONE);

					tv_popMarkName.setVisibility(View.GONE);
					iv_MarkNameLine.setVisibility(View.GONE);

					tv_popRecommend.setVisibility(View.GONE);
					iv_RecommendLine.setVisibility(View.GONE);

					tv_popWarn.setVisibility(View.GONE);
					iv_WarnLine.setVisibility(View.GONE);
				} else {
					// 好友
					tv_popSetStarFriend.setVisibility(View.VISIBLE);
					iv_SetStarFriendLine.setVisibility(View.VISIBLE);
					int starFriend = TX.tm.getStarFriendAttr(tx.partner_id);
					if (starFriend == TxInfor.TX_COMMON_FRIEND) {
						// 显示设置为星标好友
						tv_popSetStarFriend
								.setText(R.string.userinfo_star_friend);
						if (Utils.debug)
							Log.e(TAG, "好友的isStarFriend = " + starFriend);
					} else if (starFriend == TxInfor.TX_STAR_FRIEND) {
						tv_popSetStarFriend
								.setText(R.string.userinfo_star_friend_cancel);
						if (Utils.debug)
							Log.e(TAG, "星标好友，好友的isStarFriend = " + starFriend);
					} else {
						if (Utils.debug)
							Log.e(TAG, "数据错误，好友的isStarFriend字段值怎么能等于 "
									+ starFriend);
					}

					tv_popRecommend.setVisibility(View.VISIBLE);
					iv_RecommendLine.setVisibility(View.VISIBLE);

					tv_popMarkName.setVisibility(View.VISIBLE);
					iv_MarkNameLine.setVisibility(View.VISIBLE);

					tv_popBlockId.setVisibility(View.GONE);
					iv_BlockIdLine.setVisibility(View.GONE);
					tv_popBlockDevice.setVisibility(View.GONE);
					iv_BlockDeviceLine.setVisibility(View.GONE);
				}

				if (TX.tm.getTxMe().auth == TxDB.TX_AUTH_S_OP) {
					// 超级op
					tv_popWarn.setVisibility(View.VISIBLE);
					iv_WarnLine.setVisibility(View.VISIBLE);

					tv_popRecommend.setVisibility(View.VISIBLE);
					iv_RecommendLine.setVisibility(View.VISIBLE);

					// uplistmarkname.setVisibility(View.VISIBLE);
					// uplistmarknameline.setVisibility(View.VISIBLE);

					tv_popBlockId.setVisibility(View.VISIBLE);
					iv_BlockIdLine.setVisibility(View.VISIBLE);

					tv_popBlockDevice.setVisibility(View.VISIBLE);
					iv_BlockDeviceLine.setVisibility(View.VISIBLE);

					// uplistshutup.setVisibility(View.VISIBLE);
					// uplistshutupline.setVisibility(View.VISIBLE);
				} else if (TxData.isCloOP()) {
					// 隐身op
					tv_popRecommend.setVisibility(View.VISIBLE);
					iv_RecommendLine.setVisibility(View.VISIBLE);
					tv_popWarn.setVisibility(View.VISIBLE);
					iv_WarnLine.setVisibility(View.VISIBLE);
					// uplistmarkname.setVisibility(View.VISIBLE);
					// uplistmarknameline.setVisibility(View.VISIBLE);
					tv_popBlockId.setVisibility(View.GONE);
					iv_BlockIdLine.setVisibility(View.GONE);
					tv_popBlockDevice.setVisibility(View.GONE);
					iv_BlockDeviceLine.setVisibility(View.GONE);
					// uplistshutup.setVisibility(View.VISIBLE);
					// uplistshutupline.setVisibility(View.VISIBLE);

				}

			}

			upMorePopWindow.update();

			upMorePopWindow.showAsDropDown(moreBtn,
					-(int) (Utils.SreenW * 0.4), 0);

		}

		// }

	}

	// 修改备注名对话框
	private void showDialog(int title, String content, final String type,
			final TextView textView, final int length) {
		telDialog = new AlertDialog.Builder(UserInformationActivity.this)
				.create();
		telDialog.setTitle(title);
		LinearLayout layout = new LinearLayout(UserInformationActivity.this);
		layout.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,
				LayoutParams.WRAP_CONTENT));
		layout.setOrientation(LinearLayout.VERTICAL);
		final EditText mEditText = new EditText(UserInformationActivity.this);
		mEditText.setLines(4);
		mEditText.setGravity(Gravity.TOP);
		mEditText.setText(content);
		mEditText.addTextChangedListener(new ContentTextWatcher(length,
				UserInformationActivity.this, mEditText));
		int index = mEditText.getText().toString().length();
		mEditText.setSelection(index < 0 ? 0 : index);
		layout.addView(mEditText);
		telDialog.setView(layout);
		telDialog.setButton(
				this.getResources().getString(R.string.chatroom_gps_ok),
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (mEditText.getText().toString().length() > length) {
							Toast.makeText(
									UserInformationActivity.this,
									UserInformationActivity.this
											.getResources()
											.getString(
													R.string.userinfo_more_start)
											+ length
											+ UserInformationActivity.this
													.getResources()
													.getString(
															R.string.userinfo_more_end),
									Toast.LENGTH_SHORT).show();
						} else {
							// editor.putString(type,
							// mEditText.getText().toString()).commit();
							// textView.setText(mEditText.getText().toString());
							remarkName = mEditText.getText().toString();
							socketHelper.sendSetRemarkName(tx.partner_id,
									remarkName);
							dialog.cancel();
						}
					}
				});

		telDialog.setButton2(
				this.getResources().getString(R.string.chatroom_gps_cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		// telDialog.getWindow().setContentView(layout);
		try {
			Field field = telDialog.getClass().getDeclaredField("mAlert");
			field.setAccessible(true);
			// 获得mAlert变量的值
			Object obj = field.get(telDialog);
			field = obj.getClass().getDeclaredField("mHandler");
			field.setAccessible(true);
			// 修改mHandler变量的值，使用新的ButtonHandler类
			field.set(obj, new DialogButtonHandler(telDialog));
		} catch (Exception e) {
		}
		telDialog.show();
		((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE))
				.showSoftInput(mEditText, 0);
		// Utils.popSoftInput(this);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				InputMethodManager imm = (InputMethodManager) UserInformationActivity.this
						.getSystemService(INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
			}
		}, 500);
		// txdata.openKeyboard(mEditText);
	}

	// 发送名片返回
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (SelectFriendListActivity.CHAT_TYPE_CARD == requestCode) {// 名片返回
			if (resultCode == SelectFriendListActivity.CHAT_TYPE_CARD) {

				TX txCard = data
						.getParcelableExtra(SelectFriendListActivity.CHAT_TYPE_CARD_OBJ);
				sendCard(txCard);
			}
			super.onActivityResult(requestCode, resultCode, data);
		}
	}

	// 发送名片
	public void sendCard(TX txCard) {
		TXMessage txMessageCard = null;
		// 组消息对象
		if (txCard != null) {
			if (Utils.isIdValid(txCard.partner_id)) {
				txMessageCard = TXMessage.creatTCardEms(
						UserInformationActivity.this, tx.partner_id,
						txCard.getNick_name(), txCard.getNick_name(),
						txCard.partner_id, txCard.sign, txCard.getSex(),
						txCard.getPhone(), txCard.avatar_url, true,
						// TXMessage.NOT_SENT, System.currentTimeMillis() / 1000
						// + getPrefsMeme().getLong(CommonData.ST, 0));
						TXMessage.NOT_SENT, mSess.getServerTime());
				txMessageCard.updateState = TXMessage.UPDATE;
				socketHelper.sendSingleMessage(txMessageCard);

			}

		}
	}
}
