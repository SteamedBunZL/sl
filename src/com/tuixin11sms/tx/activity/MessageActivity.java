package com.tuixin11sms.tx.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Future;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupNewsActivity;
import com.shenliao.group.activity.GroupSmallGuard;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AutoUpdater;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 消息activity，显示用户所有会话列表
 */
public class MessageActivity extends BaseActivity implements OnClickListener,
		OnTouchListener {
	private static final String TAG = MessageActivity.class.getSimpleName();
	private final int CHECK_VER_TIMEOUT = 103;
	private final int CHECK_VER = 104;
	private final int CHECK_VER_NOT_NEEDUP = 105;
	private final int SYSTEM_NOTIFY = 106;
	protected static final int GUI_MSGS_UPDATA = 0x108;
	protected static final int GUI_TXS_UPDATA = 0x100;
	protected static final int GUI_TITLE_UPDATA_FAILED = 0x109;
	protected static final int GUI_TITLE_UPDATA_SUCCEE = 0x101;
	// private static final int CHECK_TIMEOUT = 0x110;
	private static final int FLUSH_LISTVIEWHEAD = 0x111;
	// private String addphone;
	private final Object lock = new Object();
	private long user_id;
	private UpdateReceiver updatereceiver;
	private DataReceiver datareceiver;
	private ConnectionReceiver ConnectionReceiver;
	public ContentResolver cr;
	private SmileyParser sParser;
	// private SmileyParser sysParser;//这个变量只在onCreat中初始化，没有调用，暂注掉 2014.01.20
	// private MsgReceiver msgreceiver;//该变量无效，没有构造也没有注册filter 2014.01.24 shc
	private String appurl, applog;
	private int appver;
	private String yearPrompt, monthPrompt, dayPrompt;
	private SimpleDateFormat curDayFormat, preDayFormat;
	private int isqut;
	private int menu_State;
	private final int DELET = 1;
	private int selectItemCount = 0;
	private boolean ismsgcheck;
	private boolean isselectall;
	// private boolean teachState;
	// private boolean teachShow;
	private boolean inlogin;
	public ArrayList<MsgStat> msgStats;// 对话数据
	private ArrayList<MsgStat> tempmsgStats;
	private int currentViewIndex; // 点击某条信息进入到聊天页面，然后关闭聊天界面返回到对话页面时，点中的那条数据已经移到上面或下面去了，需要定�?
	private PopupWindow popupWindow;// 这个变量可以注掉 2014.03.17 shc
	// private RelativeLayout listHeader;
	private ImageView noContacts;
	public ListView msglistview;
	// public ImageView newchat = null;
	public TextView promptText = null;
	public TextView tuixin_id;
	public TextView tuixin_id_name;
	public TextView tuixin_name;
	public TextView connect_title_state;
	public Button msg_selcet_btn;
	public Button msg_delet_btn;
	public ContactAPI api;
	// private int defaultHeadImg;
	// private int defaultHeadImgMan;
	// private int defaultHeadImgFemal;
	private Bitmap tuixinManHeaderImg;
	private Bitmap tuixinFriendHeaderImg;
	private Bitmap slGroupNoticeImg;
	private Bitmap slSafeImg;
	private RelativeLayout message_title_view;
	// private View groupTopView;
	private View sms_inbox;
	private MyMsgAdapter myMsgAdapter;
	// boolean isNewContacts;//这个变量好像不对，没有人给它传递值 2014.01.08
	boolean isPopshow;
	boolean isNewsItemContacts;
	boolean launch_tx;
	private String system_notify_title;
	private String system_notify_content;
	// public boolean tempSetHead;
	public boolean foreverSetHead;
	private int channelId;
	// private ImageView myDefaultImage;

	public List<ImageView> par_avas = new ArrayList<ImageView>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// 读取经纬度和时间
		Utils.readLocationData(this);
		TxData.addActivity(this);

		if (Utils.debug)
			Log.i(TAG, "message OnCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_message);
		// defaultHeaderImgMan = R.drawable.sl_regist_default_head;
		// defaultHeaderImgFemale = R.drawable.sl_regist_head_femal;

		// TuixinService1.OnApp = true;

		// String shortcut = getPrefsMeme().getString(CommonData.SHORTCUT, "");
		String shortcut = mSess.mPrefMeme.shortcut.getVal();
		if ("".equals(shortcut)) {
			Utils.addShortcut(this);
			// getEditorMeme().putString(CommonData.SHORTCUT,
			// CommonData.SHORTCUT);
			// getEditorMeme().commit();
			mSess.mPrefMeme.shortcut.setVal(PrefsMeme.SHORTCUT).commit();
		}

		// int sex = getPrefsMeme().getInt(CommonData.SEX, -1);
		int sex = mSess.mPrefMeme.sex.getVal();
		if (sex == TX.MALE_SEX) {
			defaultHeaderImg = defaultHeaderImgMan;
		} else {
			defaultHeaderImg = defaultHeaderImgFemale;
		}

		tuixinManHeaderImg = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.tuixin_manage)).getBitmap();
		tuixinFriendHeaderImg = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.friend_manage)).getBitmap();

		slGroupNoticeImg = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.slgroupnotice)).getBitmap();
		slSafeImg = ((BitmapDrawable) getResources().getDrawable(
				R.drawable.slsafe)).getBitmap();

		// if (!Utils.isNull(getPrefsMeme().getString(CommonData.AVATAR_URL,
		// ""))) {
		if (!Utils.isNull(mSess.mPrefMeme.avatar_url.getVal())) {
			// getEditorMeme().putBoolean(CommonData.IS_SETHEAD, true).commit();
			mSess.mPrefMeme.is_sethead.setVal(true).commit();
		}
		// String strUserId = getPrefsMeme().getString(CommonData.USER_ID,
		// "-1");
		String strUserId = mSess.mPrefMeme.user_id.getVal();
		if (!Utils.isNull(strUserId)) {
			user_id = Long.parseLong(strUserId);
			// String moreuser = getPrefs().getString(CommonData.MORE_USER_251,
			// "");
			// // String nickname=prefs.getString(CommonData.NICKNAME, "");
			// String pwd = getPrefs().getString(CommonData.PASSWORD, "");
			// String userpwd = user_id + "�" + pwd;
			// boolean defult = false;
			// if (Utils.isNull(moreuser)) {
			// defult = true;
			// getEditor().putString(CommonData.MORE_USER_251, userpwd);
			// } else if (!Utils.isNull(userpwd)) {
			// if (moreuser.equals(userpwd)
			// || moreuser.indexOf("�#�" + userpwd + "�#�") != -1
			// || moreuser.endsWith("�#�" + userpwd)
			// || moreuser.startsWith(userpwd + "�#�")) {
			//
			// } else {
			// getEditor().putString(CommonData.MORE_USER_251,
			// moreuser + "�#�" + userpwd);
			// }
			// }
			// getEditor().putString(CommonData.USER_ID, strUserId);
			// TX.updateTXMe();////////
			// if (Utils.debug)
			// Log.i(TAG, "进入主界面creatDB" + strUserId);
			// // TxDBContentProvider.creatDB(this, strUserId,
			// defult);//登陆成以后创建的数据库名字是DATABASE_NAME+uid，这里相当于又创建了一个数据库名字为DATABASE_NAME+uid+uid
		} else
			user_id = -1;
		if (user_id == -1) {
			if (Utils.debug)
				Log.i(TAG, "没有用户id!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			inlogin = false;
			// return;
		} else {
			inlogin = true;
		}

		msgStats = new ArrayList<MsgStat>();
		tempmsgStats = new ArrayList<MsgStat>();
		api = ContactAPI.getAPI();
		api.setCr(getContentResolver());
		sParser = new SmileyParser(this);
		sParser.setInTuixin(true);
		// sysParser = new SmileyParser(this);//这个变量只在onCreat中初始化，没有调用，暂注掉
		// 2014.01.20
		// sysParser.setInChatView(true);
		cr = this.getContentResolver();
		// posContacts = new HashMap<String, Integer>();
		// posTbs = new HashMap<String, Integer>();
		sms_inbox = findViewById(R.id.tuixin_tab_inbox);
		// 会话消息的list，修改getView方法，信息封装一次后把信息缓存起来，有新的再访问数据库获取
		msglistview = (ListView) sms_inbox.findViewById(R.id.list_inbox);
		noContacts = (ImageView) sms_inbox
				.findViewById(R.id.message_list_hint_empty);
		tuixin_id_name = (TextView) sms_inbox.findViewById(R.id.tuixin_id_name);// 文字为“神聊号”，固定值?
		tuixin_id = (TextView) sms_inbox.findViewById(R.id.tuixin_id);
		tuixin_name = (TextView) sms_inbox.findViewById(R.id.inbox_title_name);
		connect_title_state = (TextView) sms_inbox
				.findViewById(R.id.connect_state);
		message_title_view = (RelativeLayout) findViewById(R.id.message_tab_title);
		// newchat = (ImageView) findViewById(R.id.new_chat);

		promptText = (TextView) findViewById(R.id.prompt_text);
		sms_inbox.setOnTouchListener(this);
		LayoutInflater mInflater = LayoutInflater.from(this);
		// groupTopView = mInflater.inflate(R.layout.main_list_header, null);//
		// 搜索框和list头上的设置头像提醒框
		// listHeader = (RelativeLayout) groupTopView
		// .findViewById(R.id.tx_list_item_header);// list上面设置头像提醒框
		// myDefaultImage = (ImageView) groupTopView
		// .findViewById(R.id.tx_list_item_photoview1);// 提醒中的默认照片
		// myDefaultImage.setBackgroundResource(defaultHeaderImg);

		// isNewContacts = getPrefsMeme().getBoolean(CommonData.NEW_CONTACT,
		// false);//没有传递之歌值的intentExtra 2014.01.08
		initDateStr();
		// foreverSetHead = getPrefsMeme().getBoolean(CommonData.IS_SETHEAD,
		// false);
		foreverSetHead = mSess.mPrefMeme.is_sethead.getVal();

		// fillMsgStatListAndRefresh();// 开启子线程获取数据
		msgStatflush(); // 这样直接获取吧 2013.10.23 shc
		// msglistview.addHeaderView(groupTopView);
		myMsgAdapter = new MyMsgAdapter(MessageActivity.this, msgStats);
		msglistview.setAdapter(myMsgAdapter);

		// openPopupwin1();

		// newchat.setOnClickListener(this);
		// listHeader.setOnClickListener(this);
		message_title_view.setOnClickListener(this);
		// xiaoxiTest.setOnClickListener(this);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// case R.id.new_chat:
		// Intent intent = new Intent(MessageActivity.this,
		// SelectFriendListActivity.class);
		// startActivity(intent);
		// break;
		// case R.id.tx_list_item_header:
		// tempSetHead = true;
		// Intent iSupplement = new Intent(MessageActivity.this,
		// UserInfoSupplementActivity.class);
		// iSupplement.putExtra(UserInfoSupplementActivity.pblicInfo,
		// UserInfoSupplementActivity.OTHERTOUSERINFO);
		// startActivity(iSupplement);
		// break;
		case R.id.message_tab_title:
			msglistview.setSelectionAfterHeaderView();
			break;
		}
	}

	public void msgStatflush() {
		if (Utils.debug) {
			Log.e(TAG, "发送消息更新adapter");
		}
		Message m = new Message();
		m.what = GUI_MSGS_UPDATA;
		MessageActivity.this.mHandler.sendMessageDelayed(m, 500);
		// titleflush(FLUSH_LISTVIEWHEAD);
	}

	/**
	 * 刷新view,
	 * 
	 * @param title_updata
	 *            根据此值传的不同执行不同的更新
	 *            例如：标题栏的“神聊号：1234567”后面的网络连接状态textview；刷新listview
	 *            ,执行invalidateViews
	 * 
	 */
	public void titleflush(int title_updata) {
		Message m = new Message();
		m.what = title_updata;
		MessageActivity.this.mHandler.sendMessage(m);
	}

	// public void newContactsIconflush() {
	// Message m = new Message();
	// m.what = FLUSH_NEWCONTANTSICON;
	// MessageActivity.this.mHandler.sendMessage(m);
	// }

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case CHECK_VER_TIMEOUT:
				// 检查版本超时？
				startPromptDialog(R.string.prompt,
						R.string.request_check_ver_outtime);
				break;
			case CHECK_VER_NOT_NEEDUP:
				// 检查新版本无需更新
				startPromptDialog(R.string.prompt,
						R.string.check_new_versoin_not_need_updata);
				break;
			case SYSTEM_NOTIFY:
				// 系统通知
				startPromptDialogOtherLogin(system_notify_title,
						system_notify_content);
				break;
			case CHECK_VER:
				if (!AutoUpdater.isUping) {
					AutoUpdater.isUping = true;
					if (upapp) {
						new AlertDialog.Builder(MessageActivity.this)
								.setTitle(R.string.up_prompt)
								.setCancelable(false)
								.setMessage("您的当前版本过低，已经无法使用，需要升级到新版本！")
								.setNegativeButton(R.string.confirm,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialoginterface,
													int i) {
												AutoUpdater.isUping = false;
												AutoUpdater.CheckForUpdate(
														MessageActivity.this,
														appurl, "", appver);
												// getEditorMeme().putInt(
												// CommonData.OLD_VER,
												// appver);
												// getEditorMeme().commit();
												mSess.mPrefMeme.old_ver.setVal(
														appver).commit();
											}
										}).show();// 显示对话�?
					} else {
						new AlertDialog.Builder(MessageActivity.this)
								.setTitle(R.string.up_prompt)
								.setCancelable(false)
								.setMessage(applog)
								.setPositiveButton(R.string.confirm,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialoginterface,
													int i) {
												AutoUpdater.isUping = false;
												AutoUpdater.CheckForUpdate(
														MessageActivity.this,
														appurl, applog, appver);
												// getEditorMeme().putInt(
												// CommonData.OLD_VER,
												// appver);
												// getEditorMeme().commit();
												mSess.mPrefMeme.old_ver.setVal(
														appver).commit();
											}
										})
								.setNegativeButton(R.string.cancel,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int id) {
												dialog.cancel();
												AutoUpdater.isUping = false;
												// getEditorMeme().putInt(
												// CommonData.OLD_VER,
												// appver);
												// getEditorMeme().commit();
												mSess.mPrefMeme.old_ver.setVal(
														appver).commit();
												// MessageActivity.this.finish();
											}
										}).show();// 显示对话�?
					}

				}
				break;
			case MessageActivity.GUI_TXS_UPDATA:
				break;
			case MessageActivity.GUI_MSGS_UPDATA:
				if (Utils.debug)
					Log.i(TAG, "强制刷新listview");
				removeMessages(MessageActivity.GUI_MSGS_UPDATA);
				// 强制刷新消息会话的listview，经常用到
				msgStats = new ArrayList<MsgStat>(MsgStat.getMsgStatsList());// 新创建对象，不改动txdata中的集合？
				// mymsgAdapter = new
				// MyMsgAdapter(MessageActivity.this,msgStats);
				if (msgStats == null || msgStats.size() == 0) {
					showNoMsgs();// 显示无消息页面图片
					if (popupWindow != null && (popupWindow.isShowing())) {
						// 如果popupWindow在显示，则隐藏
						isselectall = false;
						isPopshow = false;
						popupWindow.dismiss();
						ismsgcheck = false;
						selectItemCount = 0;
						MessageActivity.this.msg_delet_btn.setText("删除");
						MessageActivity.this.msg_selcet_btn.setText("全选");
					}
				} else {
					showHaveMsgs();
				}
				myMsgAdapter.setData(msgStats);
				myMsgAdapter.notifyDataSetChanged();
				// msglistview.setAdapter(mymsgAdapter);
				// mymsgAdapter.notifyDataSetChanged();
				msglistview.setSelection(currentViewIndex);
				// msglistview.invalidateViews();
				if (Utils.debug)
					Log.i(TAG, "handleMessage conversation refresh");
				break;
			// case MsgInfor.SERVER_OLD_MSG:
			// break;
			// case FLUSH_NEWCONTANTSICON:
			// if (isNewContacts) {
			// news_icon.setVisibility(View.VISIBLE);
			// } else {
			// news_icon.setVisibility(View.GONE);
			// }
			//
			// break;
			case MessageActivity.GUI_TITLE_UPDATA_FAILED:
				connect_title_state.setVisibility(View.VISIBLE);
				break;
			case MessageActivity.GUI_TITLE_UPDATA_SUCCEE:
				connect_title_state.setVisibility(View.GONE);
				break;
			// case CHECK_TIMEOUT:
			// if (channelId > 0) {
			// new AlertDialog.Builder(MessageActivity.this)
			// .setTitle(R.string.msgroom_lbs_fail_title)
			// .setMessage(R.string.channel_data_outtime)
			// .setIcon(R.drawable.alert_dialog_icon)
			// .setPositiveButton(R.string.channelroom_try,
			// new DialogInterface.OnClickListener() {
			// public void onClick(
			// DialogInterface dialoginterface,
			// int id) {
			// // gotoChannelRoom();
			// dialoginterface.dismiss();
			// }
			// })
			// .setNegativeButton(R.string.cancel,
			// new DialogInterface.OnClickListener() {
			// public void onClick(
			// DialogInterface dialoginterface,
			// int id) {
			// dialoginterface.dismiss();
			// }
			// }).show();
			// }
			// break;
			case FLUSH_LISTVIEWHEAD:

				// foreverSetHead =
				// getPrefsMeme().getBoolean(CommonData.IS_SETHEAD,
				foreverSetHead = mSess.mPrefMeme.is_sethead.getVal();
				// if (!foreverSetHead) {
				// if (!tempSetHead) {
				// } else {
				// listHeader.setVisibility(View.GONE);
				// }
				// } else {
				// listHeader.setVisibility(View.GONE);
				// }
				if (Utils.debug) {
					Log.i(TAG, "刷新了listview，所有的item重新调用getView方法");
				}
				msglistview.invalidateViews();
				break;

			}
		}
	};

	private void showNoMsgs() {
		noContacts.setVisibility(View.VISIBLE);
		msglistview.setVisibility(View.GONE);
	}

	private void showHaveMsgs() {
		noContacts.setVisibility(View.GONE);
		msglistview.setVisibility(View.VISIBLE);
	}

	private boolean upapp;

	public void onStart() {
		if (Utils.debug)
			Log.i(TAG, "message onStart");

		// myDefaultImage.setBackgroundResource(defaultHeaderImg);
		registReceiver();

		super.onStart();
	}

	public void onResume() {
		if (Utils.debug)
			Log.i(TAG, "message onResume");

		isqut = -1;
		// isMsgStatflush = false;
		// isReceiverLocation=true;
		new Timer().schedule(new TimerTask() {
			public void run() {
				isqut = 0;
			}
		}, 1000);
		titleflush(FLUSH_LISTVIEWHEAD);// 会刷新整个listview

		msgStatflush();// 暂时这样强制刷新，后续再细致处理 2013.10.23 shc

		this.setIntent(this.getIntent());
		TX me = TX.tm.getTxMe();
		if (Utils.isIdValid(me.partner_id)) {
			tuixin_id.setText(Long.toString(me.partner_id));
			tuixin_id_name.setText(R.string.create_shenliao_number);
			tuixin_id_name.setVisibility(View.VISIBLE);
		} else {
			tuixin_id_name.setVisibility(View.GONE);
			tuixin_id.setText("");
		}

		if (!Utils.isNull(me.getNick_name())) {
			tuixin_name.setText(sParser.addSmileySpans(me.getNick_name(), true,
					0));
			int type = Utils.getNetworkType(this);
			if (type == Utils.NET_PROXY) {
				tuixin_id_name.setText(R.string.net_not_support_shenliao);
				tuixin_id_name.setVisibility(View.VISIBLE);

			} else if (type == Utils.NET_NOT_AVAILABLE) {
				connect_title_state.setText(R.string.nonet_mode);
				if (Utils.debug) {
					Log.i(TAG, "昵称名不为空，网络不通，刷新titleview");
				}
				titleflush(GUI_TITLE_UPDATA_FAILED);
			} else {
				connect_title_state.setVisibility(View.GONE);
			}

		} else {
			// String tempname =
			// getPrefsMeme().getString(CommonData.REGIST_NAME, "");
			String tempname = mSess.mPrefMeme.regist_name.getVal();
			// String inputped = prefs.getString(CommonData.INPUTPASSWORD, "");
			// if(){
			int type = Utils.getNetworkType(this);
			if (type == Utils.NET_NORMAL || type == Utils.NET_WIFI
					&& (!Utils.isNull(tempname))) {
				if (inlogin)
					tuixin_id_name.setText(R.string.login_shenliao_numbering);
				else
					tuixin_id_name.setText(R.string.create_shenliao_numbering);
				tuixin_id_name.setVisibility(View.VISIBLE);
				connect_title_state.setVisibility(View.GONE);
			} else if (type == Utils.NET_PROXY) {
				tuixin_id_name.setText(R.string.net_not_support_shenliao);
				tuixin_id_name.setVisibility(View.VISIBLE);
			} else {
				connect_title_state.setText(R.string.nonet_mode);
				if (Utils.debug) {
					Log.i(TAG, "昵称名空，刷新titleview");
				}
				titleflush(GUI_TITLE_UPDATA_FAILED);

			}
			tuixin_name.setText(tempname);
			// }
		}

		// 没有用户的时候
		if (tuixin_id.getText().toString().equals("")
				&& tuixin_name.getText().toString().equals("")) {
			Intent i = new Intent(MessageActivity.this, LoginActivity.class);
			startActivity(i);
			this.finish();
			if (Utils.debug) {
				Log.i(TAG, "onResum()跳转到登陆页面");
			}
		}
		// if(mHandler != null){
		// LBSSocketHelper.getLBSSocketHelper(txdata).registerHandler(mHandler);
		// }
		// msgStatflush();

		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onPause() {
		if (Utils.debug)
			Log.i(TAG, "message onPause");
		// mAvatarCache.clear();//当onPause的时候清空头像缓存，防止有的用户头像更改后缓存没有同步更改
		// for(ViewHolder holder:holderList){
		// holder.headimg.setImageBitmap(null);//情况所有的bitmap，防止避免进入Message页面头像闪一下的问题。
		// }
		// PopMenuDimss1();
		// isMsgStatflush = false;
		// isReceiverLocation=true;
		// if (locationReceiver != null) {
		// this.unregisterReceiver(locationReceiver);
		// locationReceiver = null;
		// }
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void onStop() {
		if (Utils.debug)
			Log.i(TAG, "message onStop");
		// isMsgStatflush = false;
		// isReceiverLocation=true;
		// if (msgreceiver != null) {
		// this.unregisterReceiver(msgreceiver);
		// msgreceiver = null;
		// }
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
		if (datareceiver != null) {
			this.unregisterReceiver(datareceiver);
			datareceiver = null;
		}
		if (ConnectionReceiver != null) {
			this.unregisterReceiver(ConnectionReceiver);
			ConnectionReceiver = null;
		}

		if (par_avas != null)
			par_avas.clear();

		// if (locationReceiver != null) {
		// this.unregisterReceiver(locationReceiver);
		// locationReceiver = null;
		// }

		super.onStop();
	}

	public void onDestroy() {
		TxData.popActivityRemove(this);
		// stopAsyncload();
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater mInflater = getMenuInflater();

		mInflater.inflate(R.menu.message_menu, menu);

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
		// case R.id.tselect_delete_menu:
		// // contacts_genderGroup.setVisibility(View.GONE);
		// // TuiXinMainTab.tHost.setVisibility(View.GONE);
		// // main_delet.setVisibility(View.VISIBLE);
		// if (!popupWindow.isShowing()) {
		// menu_State = DELET;
		// popupWindow.showAtLocation(findViewById(R.id.tuixin_tab_inbox),
		// Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
		// ismsgcheck = true;
		// msgStatflush();
		// }
		// break;

		case R.id.texit_menu:
			Utils.broadcastExitMsg(MessageActivity.this);
			GroupUtils.showDialog(MessageActivity.this, R.string.logout_prompt,
					R.string.dialog_okbtn, R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.exitProcessLogic(MessageActivity.this);
						}
					});

			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.i("Message", keyCode + "+++++++++" + event);
		//
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// msglistview.findFocus();
			View view = msglistview.getFocusedChild();
			if (view != null)
				msglistview.getPositionForView(view);

			return true;
		}

		else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (popupWindow != null && (popupWindow.isShowing())) {
				// PopMenuDimss1();
			} else {
				if (Utils.debug)
					Log.i(TAG, "!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
				if (isqut == 0) {
					isqut = 1;
					exitToast = new Toast(getApplicationContext());
					LayoutInflater mInflater = LayoutInflater
							.from(getApplicationContext());
					View toastView = mInflater.inflate(
							R.layout.message_exit_toast, null);
					TextView exitText = (TextView) toastView
							.findViewById(R.id.message_exit_text);
					exitText.setText(R.string.message_exit_str);
					exitToast.setDuration(Toast.LENGTH_SHORT);
					exitToast.setView(toastView);
					exitToast.show();
					new Timer().schedule(new TimerTask() {
						public void run() {
							isqut = 0;
						}
					}, 2000);
				} else if (isqut == 1) {
					isqut = 0;
					if (exitToast != null)
						exitToast.cancel();
					TxData.finishAll();

				}

			}
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	Toast exitToast;

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		//
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (menu_State != DELET) {
				if (isqut == 0) {

				}
			} else {
				menu_State = 0;
			}
			return true;
		} else {
			return super.onKeyUp(keyCode, event);
		}
	}

	public final class ViewHolder {
		public ImageView headimg;
		public TextView displayName;
		public TextView message;
		public TextView unreadmessagenum;
		public ImageView iv_praised_msgs_unread;
		public TextView time;
		public CheckBox cBox;
		public TextView message_state;
	}

	public class MyMsgAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<MsgStat> msgs;

		public MyMsgAdapter(Context context, ArrayList<MsgStat> mMsgStats) {
			if (Utils.debug) {
				Log.d(TAG, "创建了新的MyMsgAdapter");
			}
			this.mInflater = LayoutInflater.from(context);
			setData(mMsgStats);// 初始化时直接设置arraylist
		}

		public int getCount() {
			if (msgs != null) {
				return msgs.size();
			}
			return 0;
		}

		public void setData(ArrayList<MsgStat> data) {
			if (Utils.debug)
				Log.i(TAG, "setdata:" + data.size());
			this.msgs = data;

		}

		public Object getItem(int arg0) {
			return arg0;
		}

		public long getItemId(int arg0) {
			return arg0;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (Utils.debug) {
				Log.i(TAG, "*************************************");
				Log.d(TAG, "getView()-->" + position);
			}
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.main_list_item, null);
				holder = new ViewHolder();
				// holderList.add(holder);//把holder添加到list中，onPause的时候把所有的ImageView清空,onResume时重新加载
				// holder.layout =
				// (LinearLayout)convertView.findViewById(R.id.contacts_list_item);
				holder.headimg = (ImageView) convertView
						.findViewById(R.id.tx_list_item_photoview);
				holder.displayName = (TextView) convertView
						.findViewById(R.id.tx_list_item_message_name);// 会话名字
				holder.message = (TextView) convertView
						.findViewById(R.id.tx_list_item_message_summary);// 会话内容
				holder.time = (TextView) convertView
						.findViewById(R.id.list_item_time);
				holder.unreadmessagenum = (TextView) convertView
						.findViewById(R.id.tx_list_item_message_count);
				holder.iv_praised_msgs_unread = (ImageView) convertView
						.findViewById(R.id.iv_praised_msgs_unread);
				holder.message_state = (TextView) convertView
						.findViewById(R.id.tx_list_item_message_state);

				holder.cBox = (CheckBox) convertView
						.findViewById(R.id.tx_list_item_message_checkbox);
				convertView.setTag(holder);
				par_avas.add(holder.headimg);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final MsgStat single_msg = msgs.get(position);// 取出当前item的msgStat对象进行取值显示
			int converType = single_msg.ms_type;//会话类型

			holder.cBox.setChecked(single_msg.del);
			holder.cBox.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						single_msg.del = true;
						selectItemCount++;

					} else {
						single_msg.del = false;
						selectItemCount--;
					}
					if (selectItemCount == 0) {
						isselectall = false;
						MessageActivity.this.msg_selcet_btn.setText("全选");
						MessageActivity.this.msg_delet_btn.setText("删除");
					} else {
						if (selectItemCount == getCount()) {
							isselectall = true;
							MessageActivity.this.msg_selcet_btn.setText("取消全选");
						} else {
							isselectall = false;
							MessageActivity.this.msg_selcet_btn.setText("全选");
						}
						MessageActivity.this.msg_delet_btn.setText("删除("
								+ selectItemCount + ")");
					}
				}
			});

			if (ismsgcheck) {
				// 点击了popupWindow的“批量删除”option
				holder.cBox.setVisibility(View.VISIBLE);
			} else {
				holder.cBox.setVisibility(View.GONE);
			}

			// 设置每个item的长点击事件，根据会话类型的不同，实现不同的功能
			convertView.setOnLongClickListener(new msgConvertViewLongClick(
					MessageActivity.this, single_msg, position, msgs));
			// 设置每个item的点击事件
			convertView.setOnClickListener(new msgConvertViewClick(
					thisContext, single_msg, position, msgs));

			holder.headimg.setOnClickListener(new msgConvertViewClick(
					MessageActivity.this, single_msg, position, msgs));

			// 加载头像
			if(converType == TxDB.MS_TYPE_NOTICE){
				holder.headimg.setImageResource(R.drawable.praise_notice_icon);
			}else{
				loadHeadImage(holder, single_msg);// 加载会话的头像
			}

			if (single_msg.isHasSaveCache()) {
				// 有对应的缓存对象，直接加载
				if (Utils.debug) {
					Log.w(TAG, "调用缓存--" + position);
				}
				holder.displayName.setText(single_msg.getDisplayName());// 设置会话对方名字
				holder.message.setText(single_msg.getMsgDisplayBody());
				holder.time.setText(single_msg.getMsgSendTime());
				
				setUnreadCount(holder, single_msg, converType);
				
				
				if (single_msg.wasMe()) {
					holder.message_state.setVisibility(View.VISIBLE);
					holder.message_state.setText(single_msg.getMsgSendState());
				} else {
					holder.message_state.setVisibility(View.GONE);
				}

				return convertView;// 直接返回

			}

			if (Utils.debug) {
				Log.w(TAG, "新创建的itemInfoCache条目对象--" + position);
			}

			// holder.headimg.setBackgroundResource(R.drawable.tui_con_photo);
			// 下面这三个if语句为根据single_msg来显示正确的会话条目标题名,if (single_msg.ms_type ==
			// TxDB.MS_TYPE_CHANNEL)有数据库访问操作
			CharSequence displayName = null;// 会话条目上对方名字,可能会有表情解析，需要用CharSquence
			CharSequence messageContent = null;// 会话条目上消息体内容,可能会有表情解析，需要用CharSquence
			String messageSendState = null;// 我自己的消息的发送状态
			if (Utils.isIdValid(single_msg.group_id)) {
				// group_id 有效
				if (Utils.debug) {
					Log.d(TAG, "getView()--->single_msg.group_id="
							+ single_msg.group_id + "有效");
				}
				if (!Utils.isNull(single_msg.group_name)) {
					displayName = sParser.addSmileySpans(single_msg.group_name,
							true, 0);
				} else {
					displayName = Long.toString(single_msg.group_id);
				}
			} else if (converType == TxDB.MS_TYPE_NOTICE) {
				//被赞通知会话
				displayName = getString(R.string.praised_notice);//被赞条目标题
			} else {
				// 个人会话聊天?
				if (single_msg.partner_id == TX.TUIXIN_FRIEND) {
					// 好友管家
					displayName = getString(R.string.tuixinfriend);
				} else if (single_msg.partner_id == TX.TUIXIN_MAN) {
					// 神聊客服
					displayName = getString(R.string.tuixinman);
				} else if (single_msg.partner_id == TX.SL_GROUP_NOTICE) {
					// 群组动态
					displayName = getString(R.string.slgroupnotice);
				} else if (single_msg.partner_id == TX.SL_SAFE) {
					// 神聊小卫士
					displayName = getString(R.string.slsafe);
				} else {
					if (!Utils.isNull(String.valueOf(single_msg.partner_id))) {
						// partner_id不为空,神聊号不为空
						TX tx = TX.tm.getTx(single_msg.partner_id);// 通过会员id在缓存hashmap里面查找会员信息
						if (tx != null && !Utils.isNull(tx.getRemarkName())) {
							// 备注名不为空
							displayName = sParser.addSmileySpans(
									tx.getRemarkName(), true, 0);
						} else {
							if (tx != null && !Utils.isNull(tx.getNick_name())) {
								// 备注名为空，昵称不为空
								displayName = sParser.addSmileySpans(
										tx.getNick_name(), true, 0);
							} else {
								// 备注名为空，且昵称为空
								if (!Utils
										.isNull(single_msg.partner_display_name)) {
									// 备注名和昵称为空，但是【神聊名称或者群发送者名称】？不为空
									displayName = sParser.addSmileySpans(
											single_msg.partner_display_name,
											true, 0);
								} else {
									// 会话名直接显示神聊号
									displayName = Long
											.toString(single_msg.partner_id);
								}

							}
						}

					}

				}
			}

			holder.displayName.setText(displayName);// 设置会话对方名字
			single_msg.setDisplayName(displayName);

			// 根据会话消息类型，显示消息体内容
			holder.message_state.setVisibility(View.GONE);// 默认把消息发送状态设置为gone
			if (converType == TxDB.MS_TYPE_NOTICE) {
				//通知消息
				messageContent = "看看大家对你的赞美吧！";
			}else {
				switch (single_msg.msg_type) {
				// 神聊普通消�?
				case TxDB.MSG_TYPE_COMMON_SMS:
				case TxDB.MSG_TYPE_GREET_SMS:
				case TxDB.MSG_TYPE_SNS_SMS:
				case TxDB.MSG_TYPE_CONTACTS_SMS:
					// 普通消�?
				case TxDB.MSG_TYPE_SMS_SMS:
					// 群普通消�?
				case TxDB.MSG_TYPE_QU_COMMON_SMS:
				case TxDB.MSG_TYPE_QU_NOTICE_SMS:
				case TxDB.MSG_TYPE_QU_PROMPT_SMS:
					// 普通彩�?
				case TxDB.MSG_TYPE_SMS_EMS:
					if (single_msg.msg_body != null) {
						messageContent = sParser.addSmileySpans(
								single_msg.msg_body, true, 0);
					}
					break;
					// 名片
				case TxDB.MSG_TYPE_CARD_EMS:
				case TxDB.MSG_TYPE_TCARD_SMS:
				case TxDB.MSG_TYPE_SMS_CRAD:
				case TxDB.MSG_TYPE_QU_CARD_EMS:
				case TxDB.MSG_TYPE_QU_TCARD_SMS:
				case TxDB.MSG_TYPE_ADD_FRIEND_SMS:
				case TxDB.MSG_TYPE_ADD_FRIEND_RE_SMS:
					messageContent = getString(R.string.send_card);
					
					break;
					// 图片
				case TxDB.MSG_TYPE_IMAGE_EMS:
				case TxDB.MSG_TYPE_SMS_IMG:
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:
					messageContent = getString(R.string.send_image);
					break;
					// 音频
				case TxDB.MSG_TYPE_AUDIO_EMS:
				case TxDB.MSG_TYPE_SMS_AUDIO:
				case TxDB.MSG_TYPE_QU_AUDIO_EMS:
					messageContent = getString(R.string.send_audio);
					break;
					// 地理消息
				case TxDB.MSG_TYPE_GEO_SMS:
				case TxDB.MSG_TYPE_SMS_GEO:
				case TxDB.MSG_TYPE_QU_GEO_SMS:
					messageContent = getString(R.string.send_location);
					break;
				case TxDB.MSG_TYPE_BIG_FILE_SMS:
				case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
					messageContent = getString(R.string.send_big_file);
					break;
					// 草稿
				case TxDB.MSG_TYPE_SMS_DRAFT:
				case TxDB.MSG_TYPE_DRAFT:
					break;
					// 群组动态
				case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN:
					// 申请加入群，给管理员看的
					// holder.message_state.setVisibility(View.GONE);
					// 通过群组消息id，查询数据库获得群对象【group_id_notice和group_id好像是一样的？】
					TxGroup txGroupAdmin = TxGroup.getTxGroup(
							mSess.getContentResolver(),
							(int) single_msg.group_id_notice);
					if (Utils.debug) {
						Log.d(TAG, "getView()--->single_msg.group_id_notice="
								+ single_msg.group_id_notice
								+ "-----single_msg.group_id=" + single_msg.group_id);
					}
					if (txGroupAdmin != null) {
						String content = Utils.isNull(txGroupAdmin.group_title) ? ""
								+ txGroupAdmin.group_id
								: txGroupAdmin.group_title;// 群名称
						messageContent = single_msg.partner_display_name
								+ single_msg.msg_body.replace("�slgroup�", content);
						// holder.message.setText(single_msg.partner_display_name
						// + single_msg.msg_body.replace("�slgroup�", content));//
						// 估计是显示“XXX申请加入XXX群”
					} else {
						messageContent = single_msg.partner_display_name
								+ single_msg.msg_body.replace("�slgroup�", ""
										+ single_msg.group_id_notice);
						// holder.message.setText(single_msg.partner_display_name
						// + single_msg.msg_body.replace("�slgroup�", "" +
						// single_msg.group_id_notice));
					}
					break;
				case TxDB.MSG_TYPE_SET_GROUP_ADMIN:
				case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER:
					// 申请加入群的结果，给会员看的
				case TxDB.MSG_TYPE_DISMISS_GROUP:
				case TxDB.MSG_TYPE_LEAVE:
				case TxDB.MSG_TYPE_IN:
					TxGroup txGroupin = TxGroup.getTxGroup(
							mSess.getContentResolver(),
							(int) single_msg.group_id_notice);
					if (txGroupin != null) {
						String content = Utils.isNull(txGroupin.group_title) ? ""
								+ txGroupin.group_id : txGroupin.group_title;
						messageContent = single_msg.msg_body.replace("�slgroup�",
								content);
					} else {
						messageContent = single_msg.msg_body.replace("�slgroup�",
								"" + single_msg.group_id_notice);
					}
					break;
				case TxDB.MSG_TYPE_DISMISS_4_CREATOR:
					messageContent = "警告:" + single_msg.msg_body;
					break;
					// 神聊小卫士
				case TxDB.MSG_TYPE_MANAGER_WARN:
				case TxDB.MSG_TYPE_MANAGER_NOTICE_BLOG_DELETE:
				case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER:
				case TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_MEMBER:
				case TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_MEMBER:
				case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER_OVER:
					messageContent = single_msg.msg_body;
					// holder.message.setText(single_msg.msg_body);
					break;
				case TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN:
					// 给管理员处理的信息
					TXMessage txMessage = TXMessage.findTXMessageByMsgid(
							getContentResolver(), "" + single_msg.msg_id);
					if (txMessage != null) {
						messageContent = txMessage.reportName + "举报 "
								+ txMessage.partner_name;
					} else {
						messageContent = "举报信息";
					}
					break;
				case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_ADMIN_CLEAR:
					String name = getUserName(single_msg.msg_id);// 查询两次数据库获得用户的昵称
					if (name == null) {
						messageContent = "你解除了某人的禁言";
					} else {
						messageContent = "你解除了" + name + "的禁言";
					}
					
					break;
				case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_ADMIN:
					name = getUserName(single_msg.msg_id);
					if (name == null) {
						messageContent = "你禁言了某人";
					} else {
						messageContent = "你禁言了" + name;
					}
					break;
				case TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_ADMIN:
					name = getUserName(single_msg.msg_id);
					if (name == null) {
						messageContent = "你封了某人的ID";
					} else {
						messageContent = "你封了用户" + name + "的ID";
					}
					break;
				case TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_ADMIN:
					name = getUserName(single_msg.msg_id);
					if (name == null) {
						messageContent = "你封了某人的设备";
					} else {
						messageContent = "你封了用户" + name + "的设备";
					}
					break;
				case TxDB.MSG_TYPE_MANAGER_WARN_4_ADMIN:
					name = getUserName(single_msg.msg_id);
					if (name == null) {
						messageContent = "你警告了某人";
					} else {
						messageContent = "你警告了用户" + name;
					}
					break;
				}
			}

			holder.message.setText(messageContent);// 设置消息体内容
			single_msg.setMsgDisplayBody(messageContent);


			long date = single_msg.msg_date;
			long a = date * 1000;
			if (Utils.debug)
				Log.i(TAG, "time:" + a);
			String time = dealDate(a);// 格式化消息发送时间显示格式
			holder.time.setText(time);
			// 未读信息条数
			int nonum = single_msg.no_read;

			single_msg.setMsgSendTime(time);// 消息发送时间
			single_msg.setNoRead(nonum);// 未读消息数

			// 设置消息发送的状态
			int msg_type = single_msg.msg_type;//消息类型
//			holder.unreadmessagenum.setVisibility(View.GONE);
			
			setUnreadCount(holder, single_msg, converType);
			if(converType != TxDB.MS_TYPE_NOTICE){
				//非系统通知消息
//				holder.iv_praised_msgs_unread.setVisibility(View.VISIBLE);
//				holder.unreadmessagenum.setVisibility(View.INVISIBLE);
//				holder.message_state.setVisibility(View.GONE);
//			}else {
//				
//				if (nonum > 0) {
//					holder.iv_praised_msgs_unread.setVisibility(View.INVISIBLE);
//					holder.unreadmessagenum.setVisibility(View.VISIBLE);
//					holder.unreadmessagenum.setText(Integer.toString(nonum));
//				}
				
				if (msg_type <= TxDB.MSG_TYPE_CONTACTS_SMS) {
					// 神聊
					if (single_msg.wasme) {
						single_msg.wasme = true;// 是否是我的发的信息
						// 自己发送的消息，显示发送状态
						holder.message_state.setVisibility(View.VISIBLE);
						switch (single_msg.read_state) {
						case TXMessage.NOT_SENT:
							messageSendState = getString(R.string.mmsg_wait_send);
							break;
						case TXMessage.SENT:
							break;
						case TXMessage.UNREAD:
							messageSendState = getString(R.string.mmsg_go);
							break;
						case TXMessage.READ:
							messageSendState = getString(R.string.mmsg_read);
							break;
						case TXMessage.FAIL:
							messageSendState = getString(R.string.mmsg_fail);
							break;
						}
					}
					
				} else if (msg_type >= TxDB.MSG_TYPE_QU_COMMON_SMS
						&& msg_type <= TxDB.MSG_TYPE_GEO_PROMPT) {
					// 信息类型为群信息
					if (single_msg.wasme) {
						single_msg.wasme = true;// 是否是我的发的信息
						if (msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
								|| msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS
								|| msg_type == TxDB.MSG_TYPE_GEO_PROMPT) {
							// 群公告或群提示信息等则不显示消息发送状态
							// 有问题，既然消息为自己发送，那就不可能会是系统发送的
							holder.message_state.setVisibility(View.GONE);
							single_msg.wasme = false;// 设置成false,该控件不显示
						} else {
							holder.message_state.setVisibility(View.VISIBLE);
						}
						switch (single_msg.read_state) {
						case TXMessage.NOT_SENT:
							messageSendState = getString(R.string.mmsg_wait_send);
							break;
						case TXMessage.SENT:
						case TXMessage.READ:
							break;
						case TXMessage.UNREAD:
							
							break;
						case TXMessage.FAIL:
							messageSendState = getString(R.string.mmsg_fail);
							break;
							
						}
					}
				}
				
				holder.message_state.setText(messageSendState);// 设置我自己消息的发送状态
				single_msg.setMsgSendState(messageSendState);
			}
			single_msg.setHasSaveCache(true);// 说明信息已经做了缓存

			return convertView;
		}

		private void setUnreadCount(ViewHolder holder,
				final MsgStat single_msg, int converType) {
			holder.iv_praised_msgs_unread.setVisibility(View.GONE);
			holder.unreadmessagenum.setVisibility(View.GONE);
			
			if (single_msg.getNoRead() > 0) {
				if(converType == TxDB.MS_TYPE_NOTICE){
					//有未读系统消息
					holder.iv_praised_msgs_unread.setVisibility(View.VISIBLE);
					holder.message_state.setVisibility(View.GONE);
				}else {
					//非系统会话消息
					holder.unreadmessagenum.setVisibility(View.VISIBLE);
					holder.unreadmessagenum
					.setText(single_msg.getNoRead() + "");
				}
			}else if (converType == TxDB.MS_TYPE_NOTICE) {
				//没有未读消息且是系统通知条目，隐藏消息发送状态
				holder.message_state.setVisibility(View.GONE);
			}
		}

		/** 提取出来的加载会话头像的方法 */
		private void loadHeadImage(final ViewHolder holder,
				final MsgStat msgState) {

			Bitmap headImgBitmap = null;// 头像的bitmap
			// if (Utils.isIdValid(msgState.group_id))
			// {//这样可能会出现串头像的问题，单人会话MsgStat可能有群id,这时加载头像设置的是群的tag,从缓存中取到了头像加载成了群头像。
			if (msgState.ms_type == TxDB.MS_TYPE_QU) {
				// 群组id有效
				if (Utils.debug)
					Log.i(TAG, "加载群头像，group_id:" + msgState.getGroupID());
				headImgBitmap = getGroupCachedBitmap(msgState.getGroupID());
				holder.headimg.setTag(msgState.group_id);
				if (headImgBitmap == null) {
					TxGroup txGroup = TxGroup.getTxGroup(
							MessageActivity.this.getContentResolver(),
							(int) msgState.group_id);

					holder.headimg.setImageResource(R.drawable.qun_default);
					if (txGroup != null) {

						loadGroupImg(txGroup.group_avatar,
								msgState.getGroupID(),
								new AsyncCallback<Bitmap>() {
									@Override
									public void onFailure(Throwable t, long id) {
									}

									@Override
									public void onSuccess(Bitmap result, long id) {
										if (result != null
												&& (Long) holder.headimg
														.getTag() == id) {
											holder.headimg
													.setImageBitmap(result);
										}
									}
								});
					}
				} else {
					if (Utils.debug) {
						Log.e(TAG, "从缓存中加载群组头像");
					}
					holder.headimg.setImageBitmap(headImgBitmap);
				}
			} else if (msgState.ms_type == TxDB.MS_TYPE_CHANNEL) {
				// TxDB.MS_TYPE_CHANNEL这个类型已经不会再有了。以下方法不去操作
			} else {
				// 个人会话？
//				final TX tx = TX.tm.findTXByMsgStat(msgState);
				final TX tx = mSess.getTxMgr().getTx(msgState.partner_id);
				
				if (tx != null) {
					if (Utils.debug) {
						Log.e(TAG, tx.partner_id + "性别:" + tx.getSex());
					}
					if (tx.getSex() == TX.MALE_SEX) {
						defaultHeaderImg = defaultHeaderImgMan;
					} else {
						defaultHeaderImg = defaultHeaderImgFemale;
					}
					if (Utils.isIdValid(tx.partner_id)) {
						ImageView imageView = holder.headimg;
						imageView.setTag(-tx.partner_id);// 这里有一个负号，应该是顾问为了区分群tag和个人tag特意加的。

						if (tx.partner_id == TX.TUIXIN_MAN) {
							headImgBitmap = tuixinManHeaderImg;
						} else if (tx.partner_id == TX.TUIXIN_FRIEND) {
							headImgBitmap = tuixinFriendHeaderImg;
						} else if (msgState.partner_id == TX.SL_GROUP_NOTICE) {
							headImgBitmap = slGroupNoticeImg;
						} else if (msgState.partner_id == TX.SL_SAFE) {
							headImgBitmap = slSafeImg;
						} else {
							if (TextUtils.isEmpty(tx.avatar_url)) {
								TX ttx = TX.tm.getTx(tx.partner_id);
								if (ttx != null) {
									tx.avatar_url = ttx.avatar_url;
									if (Utils.debug) {
										Log.i(TAG, tx.partner_id + "的头像地址为："
												+ tx.avatar_url);
									}
								}
							}
							Bitmap tempHeadBitmap = mSess.avatarDownload
									.getAvatar(tx.avatar_url, tx.partner_id,
											holder.headimg, avatarHandler);
							if (tempHeadBitmap == null) {
								headImgBitmap = BitmapFactory.decodeResource(
										getResources(), defaultHeaderImg);
							} else {
								headImgBitmap = tempHeadBitmap;
								if (Utils.debug) {
									Log.e(TAG, tx.partner_id + "从缓存中加载个人回话的头像");
								}
							}
						}
						holder.headimg.setImageBitmap(headImgBitmap);

					} else {
						// 用户的账号partner_id无效
					}
				}
			}
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;

				if (par_avas != null && par_avas.size() > 0) {
					for (ImageView iv : par_avas) {
						Long lv=(Long)iv.getTag();
						if(lv!=null){
							long tid = lv;
							long id = (Long) result[1];
							if (result != null && tid == -id) {
								iv.setImageBitmap((Bitmap) result[0]);
								myMsgAdapter.notifyDataSetChanged();
							}
						}
					}
				}

				break;
			}
			super.handleMessage(msg);
		}
	};


	private class msgConvertViewLongClick implements OnLongClickListener {

		private Context context;
		private MsgStat single_msg;
		private int index;
		private ArrayList<MsgStat> msgs;

		public msgConvertViewLongClick(Context context, MsgStat single_msg,
				int position, ArrayList<MsgStat> msgs) {
			super();
			this.context = context;
			this.single_msg = single_msg;
			this.index = position;
			this.msgs = msgs;
		}

		@Override
		public boolean onLongClick(View v) {

			if (Utils.debug)
				Log.i(TAG, "弹出长按菜单");
			if (single_msg.group_id > 0) {
				TxGroup txgroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), single_msg.group_id);
				if (txgroup.group_ver == 0
						|| txgroup.group_tx_state == TxDB.QU_TX_STATE_OUT) {
					new AlertDialog.Builder(MessageActivity.this)
							.setTitle("")
							.setItems(R.array.select_dialog_items_all5,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												// 删除对话
												if (Utils
														.isIdValid(single_msg.group_id)) {
													cr.delete(
															TxDB.Messages.CONTENT_URI,
															TxDB.Messages.MSG_GROUP_ID
																	+ "=?",
															new String[] { ""
																	+ single_msg.group_id });
													MsgStat.delMsgStatByGroupId(
															mSess.getContentResolver(),
															single_msg.group_id);
												}
												MessageActivity.this
														.msgStatflush();
												break;

											}
										}
									}).create().show();
				} else {
					new AlertDialog.Builder(MessageActivity.this)
							.setTitle("")
							.setItems(R.array.select_dialog_items_all6,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) {
											switch (which) {
											case 0:
												// 删除此群记录并禁止接收消息
												if (Utils
														.isIdValid(single_msg.group_id)) {
													cr.delete(
															TxDB.Messages.CONTENT_URI,
															TxDB.Messages.MSG_GROUP_ID
																	+ "=?",
															new String[] { ""
																	+ single_msg.group_id });

													MsgStat.delMsgStatByGroupId(
															mSess.getContentResolver(),
															single_msg.group_id);
												}
												mSess.getSocketHelper()
														.sendGroupRemind(
																single_msg.group_id,
																false, false);

												msgStatflush();
												break;

											}
										}
									}).create().show();
				}
			} else if (single_msg.ms_type == TxDB.MS_TYPE_NOTICE) {
				//系统通知条目，什么都不操作  2014.05.09 shc
			} else {
				if (Utils.isIdValid(single_msg.partner_id)) {
					// 神聊
					if (single_msg.partner_id != TX.TUIXIN_FRIEND
							&& single_msg.partner_id != TX.SL_SAFE
							&& single_msg.partner_id != TX.SL_GROUP_NOTICE) {

						new AlertDialog.Builder(MessageActivity.this)
								.setTitle("")
								.setItems(R.array.select_dialog_items_all1,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												switch (which) {
												case 0:
													// 查看好友
													if (single_msg.partner_id == TX.TUIXIN_MAN) {
														Intent intent = new Intent(
																MessageActivity.this,
																UserInformationActivity.class);
														intent.putExtra(
																UserInformationActivity.pblicInfo,
																UserInformationActivity.TUIXIN_USER_INFO);
//														TX tx = TX.tm
//																.getTx(single_msg.partner_id);
//
//														if (tx == null) {
//															tx = TX.tm
//																	.findTXByMsgStat(single_msg);
//														}
//														intent.putExtra(
//																UserInformationActivity.UID,
//																tx.partner_id);
														intent.putExtra(
																UserInformationActivity.UID,
																single_msg.partner_id);
														startActivity(intent);

													} else {
														Intent intent = new Intent(
																MessageActivity.this,
																UserInformationActivity.class);
														intent.putExtra(
																UserInformationActivity.pblicInfo,
																UserInformationActivity.TUIXIN_USER_INFO);
//														TX tx = TX.tm
//																.getTx(single_msg.partner_id);
//
//														if (tx == null) {
//															tx = TX.tm
//																	.findTXByMsgStat(single_msg);
//														}
//														intent.putExtra(
//																UserInformationActivity.UID,
//																tx.partner_id);
														intent.putExtra(
																UserInformationActivity.UID,
																single_msg.partner_id);
														startActivity(intent);
													}
													break;
												case 1:
													// 删除对话
													if (Utils
															.isIdValid(single_msg.partner_id)) {
														cr.delete(
																TxDB.Messages.CONTENT_URI,
																TxDB.Messages.MSG_PARTNER_ID
																		+ "=? AND "
																		+ TxDB.Messages.MSG_GROUP_ID
																		+ "< ?  ",
																new String[] {
																		""
																				+ single_msg.partner_id,
																		"1" });

														MsgStat.delMsgStatByPartnerId(
																mSess.getContentResolver(),
																single_msg.partner_id);
														mSess.getSocketHelper().sendNoReadMsg();
													}

													MessageActivity.this
															.msgStatflush();
													break;

												}
											}
										}).create().show();
					} else {
						new AlertDialog.Builder(MessageActivity.this)
								.setTitle("")
								.setItems(R.array.select_dialog_items_all5,
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int which) {
												switch (which) {
												case 0:
													// 删除对话
													if (Utils
															.isIdValid(single_msg.partner_id)) {
														cr.delete(
																TxDB.Messages.CONTENT_URI,
																TxDB.Messages.MSG_PARTNER_ID
																		+ "=? AND "
																		+ TxDB.Messages.MSG_GROUP_ID
																		+ "< ?  ",
																new String[] {
																		""
																				+ single_msg.partner_id,
																		"1" });
														MsgStat.delMsgStatByPartnerId(
																mSess.getContentResolver(),
																single_msg.partner_id);
													}

													msgStatflush();
													break;

												}
											}
										}).create().show();
					}
				}

			}

			return true;

		}

	}

	private class msgConvertViewClick implements OnClickListener {
		private Context context;
		private MsgStat single_msg;
		private int index;
		private ArrayList<MsgStat> msgs;

		public msgConvertViewClick(Context context, MsgStat single_msg,
				int position, ArrayList<MsgStat> msgs) {
			super();
			this.context = context;
			this.single_msg = single_msg;
			this.index = position;
			this.msgs = msgs;
		}

		@Override
		public void onClick(View v) {

			MsgStat ms = msgs.get(index);
			switch (ms.ms_type) {
			case TxDB.MS_TYPE_TB:
				if (ms.partner_id == TX.TUIXIN_FRIEND) {
					Intent i = new Intent(MessageActivity.this,
							FriendManagerActivity.class);
					startActivity(i);
				} else if (ms.partner_id == TX.SL_GROUP_NOTICE) {
					Intent i = new Intent(MessageActivity.this,
							GroupNewsActivity.class);
					startActivity(i);
				} else if (ms.partner_id == TX.SL_SAFE) {
					Intent i = new Intent(MessageActivity.this,
							GroupSmallGuard.class);
					startActivity(i);
				} else {

					Intent intent = new Intent(context, SingleMsgRoom.class);
					if (Utils.debug)
						Log.i(TAG, ms.toString());
//					TX tx = TX.tm.findTXByMsgStat(ms);
//					// 许春会修改,显示备注名
//					TX tx1 = TX.tm.getTx(ms.partner_id);
//					if (tx1 != null && tx1.getRemarkName() != null) {
//						tx.setRemarkName(tx1.getRemarkName());
//					}
//					if (tx1 != null) {
//						tx.setSex(tx1.getSex());
//					}
//					intent.putExtra(Utils.MSGROOM_TX, tx.partner_id);
					
					intent.putExtra(Utils.MSGROOM_TX, ms.partner_id);
					startActivity(intent);
				}

				break;
			case TxDB.MS_TYPE_QU: // 群聊
				
				Intent intent2 = new Intent(context, GroupMsgRoom.class);
				TxGroup txgroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), ms.group_id);
				if (txgroup == null) {
					mSess.getSocketHelper().sendGetGroup(
							ms.group_id);
					intent2.putExtra(GroupMsgRoom.TX_GROUP_ID, ms.group_id);
				} else {
					if (Utils.debug)
						Log.i(TAG, "group" + txgroup);
					intent2.putExtra(Utils.MSGROOM_TX_GROUP, txgroup);
				}
				startActivity(intent2);
				break;
			case TxDB.MS_TYPE_NOTICE:
				
				intent2 = new Intent(context, PraiseNoticeActivity.class);
				startActivity(intent2);
				
				break;
			}

			if (Utils.debug)
				Log.i(TAG, "进入对话界面");
		}

	}

	// 引用解除 2014.01.22 shc
	// private class addContactClick implements OnClickListener {
	//
	// private Context context;
	// private String phone;
	//
	// public addContactClick(Context context, String phone) {
	// super();
	// this.context = context;
	// this.phone = phone;
	// }
	//
	// @Override
	// public void onClick(View v) {
	// api.insert(context, phone);
	//
	// addphone = phone;
	// }
	//
	// }

	private Set<Future> futureSet = new HashSet<Future>();// 存储Future的集合，onStop的时候全部终止
	private Future future;// ExecutorService开启执行子线程后返回的future对象

	/**
	 * 开启子线程 给TXData中的会话信息集合填充数据，并且更新页面listview 不一定更新，如果会话个数大于2就不更新，为什么？
	 */
	private void fillMsgStatListAndRefresh() {
		if (Utils.debug) {
			Log.e(TAG, "fillMsgStatListAndRefresh()");
		}
		// 开启子线程访问填充数据，并通知ui更新
		future = Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				// 不用再去取TX和删除更新了吧？2013.10.23 shc
				// {
				// try {
				// msgStats = MsgStat.filterTXList(txdata);
				// } catch (Exception e) {
				// if (Utils.debug)
				// Log.e(TAG, "查询数据库消息会话异常", e);
				// }
				// MsgStat.setMsgstats(txdata, msgStats, false);//
				// 设置msgStats集合给txdata，但是不发更新广播
				// }
				// if (Utils.debug) {
				// Log.e(TAG, "fillMsgStatListAndRefresh取到的最近会话list长度:"
				// + msgStats.size());
				// Log.d(TAG, "所有的msgStats:" + msgStats.toString());
				// }
				// msgStatflush();
			}
		});

		futureSet.add(future);
	}

	// 该广播无引用，注掉 2014.01.24 shc
	// public class MsgReceiver extends BroadcastReceiver {
	// public void onReceive(Context context, Intent intent) {
	// if (Utils.debug) {
	// Log.d(TAG, "MsgReceiver---onReceive");
	// }
	// final String msg =
	// null;//intent.getStringExtra(AutoSyncContactsAndSms.SEND_KEY);
	// if (!Utils.isNull(msg)) {
	// if (msg.equals("complete")) {
	// if (Utils.debug)
	// Log.i(TAG, "broadcast receiver init msgStats");
	// // teachState = getPrefsMeme().getBoolean(CommonData.TEACH_STATE,true);
	// teachState = mSess.mPrefMeme.teach_state.getVal();
	// if (Utils.debug)
	// Log.i(TAG, "teachState:" + teachState);
	// if (teachState == false && teachShow == false) {
	// // if (teachShow == false) {
	// Intent in = new Intent(MessageActivity.this,TutorialActivity.class);
	// startActivity(in);
	// teachShow = true;
	// // }
	// }
	// }
	// }
	// }
	// }

	public void dealDeleteMsg(Intent intent) {
		if (intent != null) {
			TXMessage txMessage = intent.getParcelableExtra("message");
			Log.i("op", txMessage.toString());
			if (txMessage != null) {
				tempmsgStats.remove(txMessage);
				myMsgAdapter.notifyDataSetChanged();
				if (Utils.debug) {
					Log.i(TAG, "拉黑删除好友管家列表项？？？调用了通知更新adapter");
				}
			}
		}
	}

	// private void openPopupwin1() {
	// LayoutInflater mLayoutInflater = (LayoutInflater)
	// getSystemService(LAYOUT_INFLATER_SERVICE);
	// ViewGroup menuView = (ViewGroup) mLayoutInflater.inflate(
	// R.layout.msgroom_checkdelet, null, true);
	// msg_selcet_btn = (Button) menuView
	// .findViewById(R.id.msgRoom_chectDelet_SelectAll);
	// msg_delet_btn = (Button) menuView
	// .findViewById(R.id.msgRoom_chectDelet_Delet);
	// MsgListItemDelete();
	// popupWindow = new PopupWindow(menuView, LayoutParams.FILL_PARENT,
	// LayoutParams.WRAP_CONTENT, false);
	// // popupWindow.setBackgroundDrawable(new BitmapDrawable());
	// popupWindow.setAnimationStyle(R.style.PopupAnimation);
	// // popupWindow.showAtLocation(findViewById(R.id.message_view),
	// // Gravity.BOTTOM | Gravity.BOTTOM, 0, 0);
	// popupWindow.setOutsideTouchable(true);
	// popupWindow.setFocusable(false);
	// popupWindow.update();
	// }

	// public void PopMenuDimss1() {
	// if (popupWindow != null && (popupWindow.isShowing())) {
	//
	// isselectall = false;
	// isPopshow = false;
	// popupWindow.dismiss();
	// ismsgcheck = false;
	// selectItemCount = 0;
	// MessageActivity.this.msg_delet_btn.setText("删除");
	// MessageActivity.this.msg_selcet_btn.setText("全选");
	// for (int i = 0; i < msgStats.size(); i++) {
	// msgStats.get(i).del = false;
	//
	// }
	// msgStatflush();
	//
	// }
	//
	// }

	// public void 1PopMenuDimss1(){
	//
	// }.l.l
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (Utils.debug) {
				Log.d(TAG, "UpdateReceiver---onReceive");
			}
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_LOGIN_RSP.equals(intent.getAction())) {
				dealLogin(serverRsp);
			} else if (Constants.INTENT_ACTION_REGIST_RSP.equals(intent
					.getAction())) {
				dealRegist(serverRsp);
			} else if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent
					.getAction())) {
				dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_SERVER_RECEIVE_MSG.equals(intent
					.getAction())) {
				dealReceiveMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_SERVER_CLENT_RECEIPT
					.equals(intent.getAction())) {
				dealReceipt(serverRsp);
			} else if (Constants.INTENT_ACTION_SERVER_MSG_READ.equals(intent
					.getAction())) {
				long fromId = intent.getLongExtra("fromId", 0);
				String[] msgIds = intent.getStringArrayExtra("msgIds");
				dealMsgRead(fromId, msgIds);
			} else if (Constants.INTENT_ACTION_BLACK_DELETE_MESSAGE
					.equals(intent.getAction())) {
				dealDeleteMsg(intent);

			}
		}
	}

	private void dealLogin(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				connect_title_state.setText("");
				// tuixin_id.setText(getPrefsMeme().getString(CommonData.USER_ID,
				// ""));
				tuixin_id.setText(mSess.mPrefMeme.user_id.getVal());
				tuixin_id_name.setText(R.string.create_shenliao_number);
				tuixin_id_name.setVisibility(View.VISIBLE);
				titleflush(GUI_TITLE_UPDATA_SUCCEE);
				break;
			}
			}
		}
	}

	private void dealRegist(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				connect_title_state.setText("");
				// tuixin_id.setText(getPrefsMeme().getString(CommonData.USER_ID,
				// ""));
				tuixin_id.setText(mSess.mPrefMeme.user_id.getVal());
				tuixin_id_name.setText(R.string.create_shenliao_number);
				tuixin_id_name.setVisibility(View.VISIBLE);
				titleflush(GUI_TITLE_UPDATA_SUCCEE);
				break;
			}
			default: {
				connect_title_state.setText(R.string.connectfail);
			}
			}
		}
	}

	private void dealReceiveMsg(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				String msgId = serverRsp.getString("msgId");
				synchronized (lock) {
					for (MsgStat ms : msgStats) {
						if (Long.parseLong(msgId) == ms.msg_id) {
							if (ms.read_state < 1) {
								ms.read_state = 1;
								msgStatflush();
								if (Utils.debug)
									Log.i(TAG, "dealMsg 刷新");
							}
							break;
						}
					}
				}

			}
			}
		}
	}

	private void dealReceipt(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				String msgId = serverRsp.getString("msgId");
				synchronized (lock) {
					for (MsgStat ms : msgStats) {
						if (Long.parseLong(msgId) == ms.msg_id) {
							int state = serverRsp.getInt("readState", 0);
							if (state > ms.read_state) {
								ms.read_state = state;
								msgStatflush();
								if (Utils.debug)
									Log.i(TAG, "dealReceipt 刷新");
							}
							break;
						}
					}
				}
				break;
			}
			}
		}
	}

	private void dealMsgRead(long fromId, String[] msgIds) {
		for (int i = 0; i < msgIds.length; i++) {
			for (MsgStat ms : msgStats) {
				if (ms.partner_id == fromId
						&& Long.parseLong(msgIds[i]) == ms.msg_id) {
					ms.read_state = TXMessage.READ;
					break;
				}
			}
		}
		msgStatflush();
	}

	public class DataReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = intent.getStringExtra("msg");
			if (msg != null) {
				if (msg.equals(TxData.FLUSH_TXS)) {
					// 不做处理？
				} else if (msg.equals(TxData.FLUSH_MSGS)) {
					if (Utils.debug) {
						Log.d(TAG,
								"DataReceiver---onReceive---msg.equals(flush msgs) !需要重新创建adapter绘制listview");
					}
					msgStatflush();// 直接用这个方法吧 2013.10.23 shc
				}
			}
		}
	}

	public class ConnectionReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (Utils.debug) {
				Log.d(TAG, "ConnectionReceiver---onReceive");
			}
			if (intent.getAction().equals(
					Constants.INTENT_ACTION_NETWORK_LOCATION_FAILED)) {
				connect_title_state.setText(R.string.nonet_mode);
				titleflush(GUI_TITLE_UPDATA_FAILED);
			} else {
				titleflush(GUI_TITLE_UPDATA_SUCCEE);
			}

		}
	}

	public void chargeTitle() {
		connect_title_state.setText("");
		String id = String.valueOf(TX.tm.getUserID());
		// String nickname=jo.optString("nn");
		tuixin_id.setText(id);
		tuixin_id_name.setText(R.string.create_shenliao_number);
		tuixin_id_name.setVisibility(View.VISIBLE);
		titleflush(GUI_TITLE_UPDATA_SUCCEE);
	}

	private void dealSystemMsg(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_SYS_DIALOG:
				this.system_notify_content = serverRsp.getString("msg");
				this.system_notify_title = serverRsp.getString("title");
				// broadcastMsg("notify",msg+"�?+title);
				Message m = new Message();
				m.what = SYSTEM_NOTIFY;
				MessageActivity.this.mHandler.sendMessage(m);
				break;

			}
		}

	}

	private String dealDate(Long time) {
		long currentTime = System.currentTimeMillis();
		// if(time>currentTime)time=time/1000;
		if (("" + time).length() >= 16)
			time = time / 1000;
		Date date = new Date(time);
		long d_count = time / 1000 / 86400;

		long c_count = currentTime / 1000 / 86400;
		long time_ = c_count - d_count;
		String curDate = curDayFormat.format(date);
		if (time_ == 0) {
			return curDate;
		} else {
			int d_year = date.getYear();
			int c_year = new Date(currentTime).getYear();
			int year_ = c_year - d_year;
			if (year_ == 0) {
				preDayFormat = new SimpleDateFormat("MM" + monthPrompt + "dd"
						+ dayPrompt);
			} else {
				preDayFormat = new SimpleDateFormat("yyyy" + yearPrompt + "MM"
						+ monthPrompt + "dd" + dayPrompt);
			}
			String preDate = preDayFormat.format(date);
			return preDate;
		}
	}

	private void initDateStr() {
		curDayFormat = new SimpleDateFormat("HH:mm");
		yearPrompt = MessageActivity.this.getResources().getString(
				R.string.year_prompt);
		monthPrompt = MessageActivity.this.getResources().getString(
				R.string.month_prompt);
		dayPrompt = MessageActivity.this.getResources().getString(
				R.string.day_prompt);
		if ("-".equals(dayPrompt)) {
			dayPrompt = "";
		}
	}

	public void MsgListItemDelete() {
		msg_selcet_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				isselectall = isselectall == true ? false : true;
				if (isselectall) {
					msg_selcet_btn.setText("取消全选");
					for (int i = 0; i < msgStats.size(); i++) {
						msgStats.get(i).del = true;

					}
					selectItemCount = msgStats.size();
					MessageActivity.this.msg_delet_btn.setText("删除("
							+ selectItemCount + ")");
				} else {
					msg_selcet_btn.setText("全选");
					for (int i = 0; i < msgStats.size(); i++) {
						msgStats.get(i).del = false;
					}
					selectItemCount = 0;
					MessageActivity.this.msg_delet_btn.setText("删除");
				}

				msgStatflush();

			}

		});
		msg_delet_btn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				for (int i = 0; i < msgStats.size(); i++) {
					MsgStat ms = msgStats.get(i);
					if (ms.del) {

						if (Utils.isIdValid(ms.group_id)) {
							cr.delete(TxDB.Messages.CONTENT_URI,
									TxDB.Messages.MSG_GROUP_ID + "=?",
									new String[] { "" + ms.group_id });
							// cr.delete(TxDB.MsgStat.CONTENT_URI,
							// TxDB.MsgStat.MSG_GROUP_ID + "=?",
							// new String[] { "" + ms.group_id });
							MsgStat.delMsgStatByGroupId(mSess.getContentResolver(), ms.group_id);
							// } else if (Utils.isIdValid(ms.getChannelId())) {
							// cr.delete(TxDB.Messages.CONTENT_URI,
							// TxDB.Messages.CHANNEL_ID + "=?",
							// new String[] { "" + ms.getChannelId() });
							// // cr.delete(TxDB.MsgStat.CONTENT_URI,
							// // TxDB.MsgStat.CHANNEL_ID + "=?",
							// // new String[] { "" + ms.getChannelId() });
							// MsgStat.delMsgStatByChannelId(txdata,ms.getChannelId());
						} else if (Utils.isIdValid(ms.partner_id)) {
							cr.delete(TxDB.Messages.CONTENT_URI,
									TxDB.Messages.MSG_PARTNER_ID + "=?",
									new String[] { "" + ms.partner_id });
							// cr.delete(TxDB.MsgStat.CONTENT_URI,
							// TxDB.MsgStat.MSG_PARTNER_ID + "=?",
							// new String[] { "" + ms.partner_id });
							MsgStat.delMsgStat(mSess.getContentResolver(), ms.partner_id);
						}
						MessageActivity.this.msgStats.remove(i);
						i--;
					}
				}
				selectItemCount = 0;
				MessageActivity.this.msg_delet_btn.setText("删除");
				MessageActivity.this.msg_selcet_btn.setText("全选");
				MessageActivity.this.msgStatflush();
			}

		});
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				MessageActivity.this);
		promptDialog.setTitle(titleSource);
		promptDialog.setMessage(msg);
		promptDialog.setNegativeButton(R.string.confirm,
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		promptDialog.show();
	}

	AlertDialog promptDialogOtherlogin;

	private void startPromptDialogOtherLogin(String titleSource, String msg) {
		if (promptDialogOtherlogin == null
				|| (promptDialogOtherlogin != null && !promptDialogOtherlogin
						.isShowing())) {
			promptDialogOtherlogin = new AlertDialog.Builder(
					MessageActivity.this).create();
			promptDialogOtherlogin.setTitle(titleSource);
			promptDialogOtherlogin.setMessage(msg);
			promptDialogOtherlogin.setButton(
					MessageActivity.this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							TxData.finishAll();
							Intent i = new Intent(MessageActivity.this,
									LoginActivity.class);
							MessageActivity.this.startActivity(i);
							dialog.cancel();
							MessageActivity.this.finish();

						}
					});
			promptDialogOtherlogin.show();
		}
	}

	// 这个轨迹球的操作还要保留啊，有轨迹球的手机能跑神聊吗…… 2014.01.23 shc
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		if (event.getAction() == MotionEvent.ACTION_DOWN) {

			int index = this.msglistview.getSelectedItemPosition();
			if (Utils.debug)
				Log.i(TAG, "进入对话界面");
			MsgStat ms = tempmsgStats.get(index);
			switch (ms.ms_type) {
			case TxDB.MS_TYPE_TB:
				if (ms.partner_id == TX.TUIXIN_FRIEND) {
					Intent i = new Intent(MessageActivity.this,
							FriendManagerActivity.class);
					startActivity(i);
				} else {
					Intent intent = new Intent(MessageActivity.this,
							SingleMsgRoom.class);

					if (Utils.debug)
						Log.i(TAG, ms.toString());
//					TX tx = TX.tm.findTXByMsgStat(ms);
//					intent.putExtra(Utils.MSGROOM_TX, tx.partner_id);
					intent.putExtra(Utils.MSGROOM_TX, ms.partner_id);
					startActivity(intent);
				}
				break;
			case TxDB.MS_TYPE_QU: // 群聊
				Intent intent2 = new Intent(MessageActivity.this,
						GroupMsgRoom.class);
				TxGroup txgroup = TxGroup.getTxGroup(
						mSess.getContentResolver(), ms.group_id);
				if (txgroup == null) {
					mSess.getSocketHelper().sendGetGroup(
							ms.group_id);
					intent2.putExtra(GroupMsgRoom.TX_GROUP_ID, ms.group_id);
				} else {
					if (Utils.debug)
						Log.i(TAG, "group" + txgroup);
					intent2.putExtra(Utils.MSGROOM_TX_GROUP, txgroup);
				}
				startActivity(intent2);
				break;
			}

		}
		return super.onTrackballEvent(event);
	}

//	/**
//	 * 神聊官方id集合，无引用
//	 * 
//	 * @return
//	 */
//	private List<Long> slNPCIds() {
//		List<Long> ids = new ArrayList<Long>();
//		ids.add(TX.TUIXIN_FRIEND);
//		ids.add(TX.TUIXIN_MAN);
//		ids.add(TX.SL_GROUP_NOTICE);
//		ids.add(TX.SL_SAFE);
//		return ids;
//	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_LOGIN_RSP);
			filter.addAction(Constants.INTENT_ACTION_REGIST_RSP);
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_SERVER_RECEIVE_MSG);
			filter.addAction(Constants.INTENT_ACTION_SERVER_CLENT_RECEIPT);
			filter.addAction(Constants.INTENT_ACTION_SERVER_MSG_READ);
			filter.addAction(Constants.INTENT_ACTION_BLACK_DELETE_MESSAGE);
			this.registerReceiver(updatereceiver, filter);
		}
		if (datareceiver == null) {
			datareceiver = new DataReceiver();
			IntentFilter filter = new IntentFilter();
			// �?BroadcastReceiver 指定 action ，使之用于接收同 action 的广�?
			filter.addAction(Constants.INTENT_ACTION_FLUSH);
			this.registerReceiver(datareceiver, filter);
		}
		// if (msgreceiver == null) {
		// //不会被执行，AutoSyncContactsAndSms.INTENT_ACTION_SEND_MSG这个广播不会发送，这里先注掉，后期再删除
		// // msgreceiver = new MsgReceiver();
		// // IntentFilter filter = new IntentFilter();
		// // // �?BroadcastReceiver 指定 action ，使之用于接收同 action 的广�?
		// // filter.addAction(AutoSyncContactsAndSms.INTENT_ACTION_SEND_MSG);
		// // this.registerReceiver(msgreceiver, filter);
		// }
		if (ConnectionReceiver == null) {
			ConnectionReceiver = new ConnectionReceiver();
			IntentFilter filter = new IntentFilter();
			// �?BroadcastReceiver 指定 action ，使之用于接收同 action 的广�?
			filter.addAction(Constants.INTENT_ACTION_NETWORK_LOCATION_FAILED);
			filter.addAction(Constants.INTENT_ACTION_NETWORK_LOCATION_SUCCEE);
			this.registerReceiver(ConnectionReceiver, filter);
		}
	}

	/**
	 * 查询两次数据库获取用户名字
	 * 
	 * @param msgid
	 *            消息id
	 */
	private String getUserName(long msgid) {
		TXMessage txMessage = TXMessage.findTXMessageByMsgid(
				getContentResolver(), "" + msgid);
		String name = null;
		if (txMessage != null) {
			name = "" + txMessage.tcard_id;
			// TX tx = TX.findTXByPartnerID4DB(txMessage.tcard_id);//不要直接访问数据库
			// 2013.10.17 shc
			TX tx = TX.tm.getTx(txMessage.tcard_id);

			if (tx != null) {
				name = tx.getNick_name();
			}
		}
		return name;

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		InputMethodManager imm = (InputMethodManager) MessageActivity.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null && imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		return false;
	}

}