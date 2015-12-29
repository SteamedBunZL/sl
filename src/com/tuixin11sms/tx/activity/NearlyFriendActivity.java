package com.tuixin11sms.tx.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

//import net.youmi.android.banner.AdSize;
//import net.youmi.android.banner.AdView;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TuixinService1;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AutoUpdater;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.model.ChatChannel;
import com.tuixin11sms.tx.model.LBSUserInfo;
import com.tuixin11sms.tx.net.HttpHelper;
import com.tuixin11sms.tx.net.LBSSocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.LocationStation;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.utils.WeakAsyncTask;
import com.tuixin11sms.tx.view.SearchBar;
import com.umeng.analytics.MobclickAgent;

/** 查看附近的人 */
public class NearlyFriendActivity extends BaseActivity {
	private static final String TAG = NearlyFriendActivity.class
			.getSimpleName();
	private static final int SELECT_COUNT = 28;
	private static final int MAX_COUNT = 999;
	private final int FLUSH_CONTANTS = 101;
	private final int FLUSH_NEWCONTANTSICON = 102;
	private final int CHECK_VER_TIMEOUT = 103;
	private final int CHECK_VER = 104;
	private final int CHECK_VER_NOT_NEEDUP = 105;
	private final int FLUSH_NEWTABNEW = 106;
	private final int CHECK_TIMEOUT = 26;
	public boolean isGrid = false;
	private final int LOAD_SHOW = 107;
	private final int LOAD_HIDE = 108;

	public static String CHANNEL_DATA = "channel_data";

	private SmileyParser sParser;
	// public TxData txdata;
	// private SharedPreferences prefs = null;
	// private Editor editor;
	// private MsgHelperReceiver msghelperreceiver;
	private DataReceiver datareceiver;
	public ContentResolver cr;
	// private SmileyParser sParser;
	// private SmileyParser sysParser;
	private MsgReceiver msgreceiver;
	private Timer outtime;
	private ProgressDialog progress;
	private ProgressDialog dialog;
	private String appurl, applog;
	private int appver;
	private int menu_State;
	private boolean isChangeShow;
	protected static final int GUI_STATE_FLUSH = 0x109;
	protected static final int GUI_TXS_UPDATA = 0x100;
	private LocationStation ls;
	private TextView noContacts;

	public List<LBSUserInfo> lbsUser = new ArrayList<LBSUserInfo>();// 神聊联系人
	public List<LBSUserInfo> templbsUser = new ArrayList<LBSUserInfo>();// 神聊联系人
	private int queryState = -1;
	public ListView nearlyListView;
	public GridView nearlyGridView;
	public ImageView changeView;
	public ImageView select_sex_view;
	public TextView backBtn;
	public TextView channelId;
	public TextView titleView;
	public TextView channelTitle;
	public TextView channelTimeInfo;
	public TextView channelPersonInfo;
	private RelativeLayout near_title_view;
	private RelativeLayout channelroomInfo;
	private RelativeLayout channelroomHeadInfo;
	public ImageView channelroomIcon;
	public TextView channelTitle2;
	private View loadBarView;
	// private SearchBar searchBar = null;
	// public ImageView newchat=null;
	// public TextView promptText = null;
	public ContactAPI api;
	// int defaltHeaderImg;
	// int defaltHeaderImgFemale;
	// int defaltHeaderImgMan;
	// Drawable moredefaltHeaderImg;

	private ChatChannel chatChannel;

	private MyConAdapter myconAdapter;
	private Map<String, Integer> posContacts;
	private Map<String, Integer> posTbs;
	boolean isPopshow;
	boolean isNewsItemContacts;
	private LocationReceiver locationReceiver;
	private double lat;
	private double lng;

	boolean launch_tx;
	boolean isConnecting;
	boolean isConnecting1;
	private Intent intent;

	// private Editor editor;
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		// Log.i(TAG, "message OnCreate");
		if (Utils.debug)
			Log.i(TAG, "NearlyFriend OnCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_nearly_friend);

		View v_back_btn = findViewById(R.id.btn_back_left);
		v_back_btn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 返回按钮
				thisContext.finish();

			}
		});

		// txdata = (TxData) getApplication();
		// TuixinService1.OnApp = true;
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		intent = this.getIntent();
		chatChannel = intent.getParcelableExtra(CHANNEL_DATA);

		sParser = Utils.getSmileyParser(this);

		// defaltHeaderImgMan = R.drawable.defaultgrid;
		// defaltHeaderImgMan.setBounds(0, 0,
		// defaltHeaderImgMan.getIntrinsicWidth(),
		// defaltHeaderImgMan.getIntrinsicHeight());
		// defaltHeaderImgFemale=R.drawable.sl_regist_head_femal;
		// defaltHeaderImgFemale.setBounds(0, 0,
		// defaltHeaderImgFemale.getIntrinsicWidth(),
		// defaltHeaderImgFemale.getIntrinsicHeight());
		// moredefaltHeaderImg =
		// getResources().getDrawable(R.drawable.broadcast_portrait);
		// moredefaltHeaderImg.setBounds(0, 0,
		// defaltHeaderImg.getIntrinsicWidth(),
		// defaltHeaderImg.getIntrinsicHeight());

		// editor = prefs.edit();
		api = ContactAPI.getAPI();
		api.setCr(getContentResolver());
		cr = this.getContentResolver();
		near_title_view = (RelativeLayout) findViewById(R.id.group_index_title);
		mid_title_name_choose = (TextView) findViewById(R.id.mid_title_name_choose);

		channelroomInfo = (RelativeLayout) findViewById(R.id.channelroom_createinfo);
		channelroomHeadInfo = (RelativeLayout) findViewById(R.id.channelroom_createinfo_head);
		nearlyListView = (ListView) findViewById(R.id.list_contacts);
		nearlyGridView = (GridView) findViewById(R.id.con_gridview);
		changeView = (ImageView) findViewById(R.id.change_view);
		select_sex_view = (ImageView) findViewById(R.id.select_sex_view);
		backBtn = (TextView) findViewById(R.id.nearly_back_btn);
		titleView = (TextView) findViewById(R.id.mid_title_name);
		channelTitle = (TextView) findViewById(R.id.mid_channel_name);

		loadBarView = findViewById(R.id.head_loading);
		posTbs = new HashMap<String, Integer>();
		noContacts = (TextView) findViewById(R.id.nearly_list_hint_empty);

		// promptText = (TextView) findViewById(R.id.nearly_prompt_text);
		if (chatChannel == null) {
			// queryState = prefs.getInt(CommonData.SELECT_SEX_STATE, -1);
			queryState = mSess.mPrefMeme.select_sex_state.getVal();

			switch (queryState) {
			case -1:
				mid_title_name_choose.setVisibility(View.GONE);
				break;
			case 0:
				mid_title_name_choose.setVisibility(View.VISIBLE);
				mid_title_name_choose.setText("(男)");
				break;
			case 1:
				mid_title_name_choose.setVisibility(View.VISIBLE);
				mid_title_name_choose.setText("(女)");
				break;

			default:
				break;
			}

			nearlyListView.setOnScrollListener(new ScrollListener());
			nearlyGridView.setOnScrollListener(new ScrollListener());
		} else {
			noContacts.setVisibility(View.GONE);
			LayoutInflater mInflater = LayoutInflater.from(this);
			View channelTopView = mInflater.inflate(
					R.layout.channelroom_info_head, null);
			channelroomIcon = (ImageView) channelTopView
					.findViewById(R.id.channel_create_icon);
			channelTitle2 = (TextView) channelTopView
					.findViewById(R.id.channel_create_title_info);
			channelId = (TextView) channelTopView
					.findViewById(R.id.channel_create_id_info);

			View channelBottomView = mInflater.inflate(
					R.layout.channelroom_info_bottom, null);
			channelTimeInfo = (TextView) channelBottomView
					.findViewById(R.id.channel_create_time_info);
			channelPersonInfo = (TextView) channelBottomView
					.findViewById(R.id.channel_create_personname_info);
			if (chatChannel.getCreateTime() > 0) {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				channelTimeInfo.setText(sdf.format(new Date(chatChannel
						.getCreateTime() * 1000)));
			} else {
				channelTimeInfo.setText(R.string.channelroom_createtime_unknow);
			}
			// channelPersonInfo.setText(chatChannel.get)

			nearlyListView.addHeaderView(channelTopView);
			nearlyListView.addFooterView(channelBottomView);

			channelTitle.setVisibility(View.VISIBLE);
			channelId.setText("" + chatChannel.getChannelId());
			channelTitle.setText(chatChannel.getSubject());
			channelTitle2.setText(chatChannel.getSubject());
			if (chatChannel.getCreateUid() < 0) {
				channelPersonInfo
						.setText(R.string.channelroom_create_by_system);
			} else {
				channelPersonInfo.setText("" + chatChannel.getCreateUid());
			}

			titleView.setVisibility(View.GONE);
			changeView.setVisibility(View.GONE);
			select_sex_view.setVisibility(View.GONE);
		}
		myconAdapter = new MyConAdapter(NearlyFriendActivity.this);
		nearlyListView.setAdapter(myconAdapter);

		if (Utils.debug)
			System.out.println("lbs_msg1=" + lbsUser);
		lbsUser = null;// intent.getParcelableArrayListExtra(AccostMsgRoom.ACCOST_PERSON_INFO);
		if (Utils.debug)
			System.out.println("lbs_msg2=" + lbsUser);
		if (lbsUser == null) {
			lbsUser = new ArrayList<LBSUserInfo>();
			if (chatChannel == null) {
				loadLocationData();
				if (progress == null) {
					progress = new ProgressDialog(NearlyFriendActivity.this);
				}
			}
		} else {
			matchData();
			contactsflush();
		}
		if (chatChannel == null) {
			if (mHandler != null) {
				LBSSocketHelper.getLBSSocketHelper(mSess.getContext())
						.registerNearbyHandler(mHandler);
				// 好像是这个handler传给AsyncImageLoader，也没有被AsyncImageLoader调用。无用传递
				// AsyncImageLoader.getInstance(txdata).setHandler(mHandler,
				// Utils.lbs_tag);
			}
			templbsUser = LBSSocketHelper
					.getLBSSocketHelper(mSess.getContext()).getLBSUserInfoList(
							queryState);
		}
		changeView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				List<LBSUserInfo> temporarylbsUser = null;
				temporarylbsUser = lbsUser;
				if (temporarylbsUser != null && temporarylbsUser.size() != 0) {
					isChangeShow = isChangeShow == true ? false : true;
					if (isChangeShow) {
						menu_State = 1;
						changeView.setImageResource(R.drawable.list_mode);
						nearlyGridView.setVisibility(View.VISIBLE);
						nearlyListView.setVisibility(View.GONE);
						nearlyGridView.setAdapter(myconAdapter);
					} else {
						menu_State = 0;
						changeView.setImageResource(R.drawable.touxiang_mode);
						nearlyListView.setVisibility(View.VISIBLE);
						myconAdapter.currposition = 10;
						nearlyGridView.setVisibility(View.GONE);
						nearlyListView.setAdapter(myconAdapter);
					}
					if (Utils.debug)
						Log.i(TAG, "改变布局");
				}

			}

		});
		select_sex_view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				new AlertDialog.Builder(NearlyFriendActivity.this)
						.setTitle("筛选")
						.setItems(R.array.nearly_select_sex_items,
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										currentPage = 0;
										switch (which) {
										case 0:
											if (progress != null) {
												progress.cancel();
												progress = new ProgressDialog(
														NearlyFriendActivity.this);
											}
											progress.setMessage("正在重新加载");
											progress.show();
											queryState = HttpHelper.QUERY_GIRL;
											// editor.putInt(CommonData.SELECT_SEX_STATE,
											// HttpHelper.QUERY_GIRL).commit();
											mSess.mPrefMeme.select_sex_state
													.setVal(HttpHelper.QUERY_GIRL)
													.commit();
											mid_title_name_choose
													.setVisibility(View.VISIBLE);
											mid_title_name_choose
													.setText("(女)");
											// tempslbsUser.clear();
											// diffSex(tempslbsUser,HttpHelper.QUERY_GIRL,tempslbsUser);
											templbsUser = LBSSocketHelper
													.getLBSSocketHelper(
															mSess.getContext())
													.getLBSUserInfoList(
															queryState);
											if (templbsUser == null
													|| (templbsUser != null && templbsUser
															.size() <= 1)) {
												Double[] loc = new Double[] {
														lat, lng };
												LocationWeakAsyncTask locationtask = new LocationWeakAsyncTask(
														NearlyFriendActivity.this,
														false);
												locationtask.execute(loc);
											} else {
												contactsflush();
											}
											break;
										case 1:
											if (progress != null) {
												progress.cancel();
												progress = new ProgressDialog(
														NearlyFriendActivity.this);
											}
											progress.setMessage("正在重新加载");
											progress.show();
											queryState = HttpHelper.QUERY_BOY;
											// editor.putInt(CommonData.SELECT_SEX_STATE,
											// HttpHelper.QUERY_BOY).commit();
											mSess.mPrefMeme.select_sex_state
													.setVal(HttpHelper.QUERY_BOY)
													.commit();
											mid_title_name_choose
													.setVisibility(View.VISIBLE);
											mid_title_name_choose
													.setText("(男)");
											// tempflbsUser.clear();
											// diffSex(tempflbsUser,HttpHelper.QUERY_BOY,tempflbsUser);
											templbsUser = LBSSocketHelper
													.getLBSSocketHelper(
															mSess.getContext())
													.getLBSUserInfoList(
															queryState);
											if (templbsUser == null
													|| (templbsUser != null && templbsUser
															.size() <= 1)) {
												Double[] loc = new Double[] {
														lat, lng };
												LocationWeakAsyncTask locationtask = new LocationWeakAsyncTask(
														NearlyFriendActivity.this,
														false);
												locationtask.execute(loc);
											} else {
												contactsflush();
											}
											break;
										case 2:
											if (progress != null) {
												progress.cancel();
												progress = new ProgressDialog(
														NearlyFriendActivity.this);
											}
											progress.setMessage("正在重新加载");
											progress.show();
											queryState = HttpHelper.QUERY_ALL;
											// editor.putInt(CommonData.SELECT_SEX_STATE,
											// HttpHelper.QUERY_ALL).commit();
											mSess.mPrefMeme.select_sex_state
													.setVal(HttpHelper.QUERY_ALL)
													.commit();
											mid_title_name_choose
													.setVisibility(View.GONE);
											// diffSex(lbsUser,HttpHelper.QUERY_ALL,lbsUser);
											templbsUser = LBSSocketHelper
													.getLBSSocketHelper(
															mSess.getContext())
													.getLBSUserInfoList(
															queryState);
											if (templbsUser == null
													|| (templbsUser != null && templbsUser
															.size() <= 1)) {
												Double[] loc = new Double[] {
														lat, lng };
												LocationWeakAsyncTask locationtask = new LocationWeakAsyncTask(
														NearlyFriendActivity.this,
														false);
												locationtask.execute(loc);
											} else {
												contactsflush();
											}
											break;
										// Double[] loc=new Double[]{lat,lng};
										// LocationWeakAsyncTask locationtask
										// =new
										// LocationWeakAsyncTask(NearlyFriendActivity.this,true);
										// locationtask.execute(loc);
										}
									}
								}).show();

			}

		});
		backBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				NearlyFriendActivity.this.finish();
			}

		});
		near_title_view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				if (isChangeShow) {
					nearlyGridView.setSelection(0);
				} else {
					nearlyListView.setSelectionAfterHeaderView();
				}
			}

		});

	}

	public void loadLocationData() {
		showLoading();
		noContacts.setVisibility(View.GONE);
		ls = LocationStation.getInstance(thisContext);
		ls.getLocation(NearlyFriendActivity.this, 40000);
		locSuccessed = false;
		timeOut();

	}

	class LocationWeakAsyncTask extends
			WeakAsyncTask<Double[], Integer, String, Context> {
		public Context context;
		private boolean next;

		public LocationWeakAsyncTask(Context context, boolean next) {
			super(context);
			this.context = context;
			this.next = next;
		}

		@Override
		protected String doInBackground(Context target, Double[]... params) {
			try {
				lat = params[0][0];
				lng = params[0][1];
				if (Utils.debug)
					Log.i(TAG, "lat is:" + lat);
				if (Utils.debug)
					Log.i(TAG, "lng is:" + lng);
				if (next) {
					try {
						if ((lbsUser != null && lbsUser.size() > 0)) {
							matchData();
							contactsflush();
							isConnecting = false;
						}
					} catch (Exception e) {
						isConnecting = false;
					}
				} else {
					try {
						LBSSocketHelper.getLBSSocketHelper(mSess.getContext())
								.getNearbyPeople(lat, lng, 0, SELECT_COUNT,
										queryState, currentPage);
					} catch (Exception e) {
						if (Utils.debug)
							e.printStackTrace();
						isConnecting = false;
						contactsflush();
					}
				}
				if (Utils.debug)
					Log.i(TAG, "lbs task ok" + next);
				return null;
			} catch (Exception e) {
				if (Utils.debug)
					Log.i(TAG, "附近的人异常", e);
				return null;
			}
		}
	}

	int currentPage = 0;
	private boolean locSuccessed;

	class LocationReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (outtime != null) {
				try {
					outtime.cancel();
				} catch (Exception e) {

				}
				outtime = null;
			}

			if (intent.getAction().equals(
					Constants.INTENT_ACTION_GET_LOCATION_OK)
					&& !locSuccessed) {
				locSuccessed = true;
				Parcelable parcelable = intent.getParcelableExtra("location");
				Location location;
				if (parcelable != null) {
					location = (Location) parcelable;
					lat = location.getLatitude();
					lng = location.getLongitude();
				} else {
					lat = 0;
					lng = 0;
				}

				if (Utils.debug)
					Log.i(TAG, "lat is:" + lat);
				if (Utils.debug)
					Log.i(TAG, "lng is:" + lng);

				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
					dialog = null;
				}
				dialog = new ProgressDialog(NearlyFriendActivity.this);
				dialog.setMessage(getResources().getString(
						R.string.nearfriend_load));
				dialog.show();
				Double[] loc = new Double[] { lat, lng };
				LocationWeakAsyncTask locationtask = new LocationWeakAsyncTask(
						NearlyFriendActivity.this, false);
				locationtask.execute(loc);
			} else if (intent.getAction().equals(
					Constants.INTENT_ACTION_GET_LOCATION_FAILED)) {
				matchData();
				contactsflush();
				Utils.startPromptDialog(NearlyFriendActivity.this,
						R.string.prompt, R.string.gps_fail_title);
			} else if (intent.getAction().equals(
					Constants.LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED)) {
				matchData();
				contactsflush();
				Utils.startPromptDialog(NearlyFriendActivity.this,
						R.string.prompt, R.string.net_not_available);
			} else if (intent.getAction().equals(
					Constants.LOAD_LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED)) {
				matchData();
				contactsflush();
				Utils.startPromptDialog(NearlyFriendActivity.this,
						R.string.prompt, R.string.gps_fail_title);
			}
		}

	}

	// public class MsgHelperReceiver extends BroadcastReceiver {
	// public void onReceive(Context context, Intent intent) {
	// final String msg = intent.getStringExtra("notify");
	// if (msg != null && msg.trim().length() != 0) {
	// String[] msgs = msg.split("�");
	// String mss = msgs[0];
	// String title = msgs[1];
	// new
	// AlertDialog.Builder(NearlyFriendActivity.this).setTitle(title).setMessage(mss).setPositiveButton(
	// R.string.confirm, new DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.cancel();
	// }
	// }).show();
	// }
	//
	// final String msg2 = intent.getStringExtra("msg");
	// if ("otherlogin".equals(msg2)) {
	//
	// AlertDialog.Builder promptDialog = new
	// AlertDialog.Builder(NearlyFriendActivity.this);
	// promptDialog.setTitle(R.string.other_login_title);
	// promptDialog.setMessage(R.string.other_login);
	// promptDialog.setPositiveButton(R.string.login_retry, new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.cancel();
	// // tuixinService.sendPing();
	// }
	// });
	// promptDialog.setNegativeButton(R.string.confirm, new
	// DialogInterface.OnClickListener() {
	// @Override
	// public void onClick(DialogInterface dialog, int which) {
	// dialog.cancel();
	// editor = prefs.edit();
	// //editor.putString(CommonData.USERNAME, "");
	// //editor.putString(CommonData.EMAIL, "");
	// //editor.putString(CommonData.PASSWORD, "");
	// //editor.putString(CommonData.REALNAME, "");
	// //editor.putString(CommonData.TELEPHONE, "");
	// //editor.commit();
	// //System.exit(0);
	// ActivityManager manager = (ActivityManager)
	// getSystemService(Context.ACTIVITY_SERVICE);
	// manager.restartPackage(getPackageName());
	// //Intent intent = new Intent(MyChatRoom.this,
	// //LoginActivity.class);
	// //// intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
	// //MyChatRoom.this.startActivity(intent);
	// }
	// });
	// promptDialog.show();
	// } else if (msg2 != null && msg2.startsWith("headerimg")) {
	// // String tem[]=msg2.split(":");
	// // for(LBSUserInfo lbs:lbsUser){
	// // if(lbs.getUid()==Integer.parseInt(tem[1])){
	// // lbs.s
	// // }
	// //
	// //
	// // }
	//
	// }
	// }
	//
	// }

	public class ScrollListener implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//
			// 分别是开始滚动（SCROLL_STATE_FLING ），正在滚动(SCROLL_STATE_TOUCH_SCROLL ),
			// 已经停止（SCROLL_STATE_IDLE ）
			// 剩10条的话就加载
			// currentViewIndex = view.getFirstVisiblePosition();
			if (Utils.debug)
				Log.i(TAG, "view count is:" + view.getCount());
			List<LBSUserInfo> temporarylbsUser = null;
			temporarylbsUser = lbsUser;
			if (view.getLastVisiblePosition() >= view.getCount() - 5
					&& temporarylbsUser != null
					&& temporarylbsUser.size() <= MAX_COUNT
					&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				if (!isConnecting) {

					// LocationWeakAsyncTask locationtask =new
					// LocationWeakAsyncTask(NearlyFriendActivity.this,true);
					// locationtask.execute(loc);

					Utils.executorService.submit(new Runnable() {
						@Override
						public void run() {
							try {
								loadflush(LOAD_SHOW);
								isConnecting = true;
								a = 0;
								LBSSocketHelper.getLBSSocketHelper(
										mSess.getContext()).getNearbyPeople(
										lat, lng, lbsUser.size(), SELECT_COUNT,
										queryState, currentPage);

							} catch (Exception e) {
								// if(Utils.debug)e.printStackTrace();
								isConnecting = false;
							}

						}
					});
				}

			}
		}

	}

	// private void searchBarVisible(boolean isShow){
	// Message m = searchBarHandler.obtainMessage();
	// if(isShow){
	// m.what = 1;
	// }
	// m.sendToTarget();
	// }
	// Handler searchBarHandler = new Handler(){
	//
	// @Override
	// public void handleMessage(Message msg) {
	// if(msg.what == 1){
	// searchBar.setVisibility(View.GONE);
	// }else{
	// searchBar.setVisibility(View.GONE);
	// }
	// super.handleMessage(msg);
	// }
	//
	// };

	public void Statflush(int state) {
		Message m = new Message();
		m.what = state;
		NearlyFriendActivity.this.mHandler.sendMessage(m);
	}

	public void contactsflush() {
		Message m = new Message();
		m.what = FLUSH_CONTANTS;
		NearlyFriendActivity.this.mHandler.sendMessage(m);
	}

	public void newContactsIconflush() {
		Message m = new Message();
		m.what = FLUSH_NEWCONTANTSICON;
		NearlyFriendActivity.this.mHandler.sendMessage(m);
	}

	public void RadioButtonflush() {
		Message m = new Message();
		m.what = FLUSH_NEWTABNEW;
		NearlyFriendActivity.this.mHandler.sendMessage(m);
	}

	public void loadflush(int what) {
		Message m = new Message();
		m.what = what;
		NearlyFriendActivity.this.mHandler.sendMessage(m);
	}

	int a;
	Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {

			switch (msg.what) {
			case LOAD_SHOW:
				loadBarView.setVisibility(View.VISIBLE);
				break;
			case LOAD_HIDE:
				loadBarView.setVisibility(View.GONE);
				break;
			case CHECK_VER_TIMEOUT:
				startPromptDialog(R.string.prompt,
						R.string.request_check_ver_outtime);
				break;
			case CHECK_VER_NOT_NEEDUP:
				startPromptDialog(R.string.prompt,
						R.string.check_new_versoin_not_need_updata);
				break;
			case CHECK_VER:
				if (!AutoUpdater.isUping) {
					AutoUpdater.isUping = true;
					new AlertDialog.Builder(NearlyFriendActivity.this)
							.setTitle(R.string.up_prompt)
							.setCancelable(false)
							.setMessage(applog)
							.setPositiveButton(R.string.confirm,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {
											AutoUpdater.CheckForUpdate(
													NearlyFriendActivity.this,
													appurl, applog, appver);
											// editor.putInt(CommonData.OLD_VER,
											// appver);
											// editor.commit();
											mSess.mPrefMeme.old_ver.setVal(
													appver).commit();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
											AutoUpdater.isUping = false;
											// editor.putInt(CommonData.OLD_VER,
											// appver);
											// editor.commit();
											mSess.mPrefMeme.old_ver.setVal(
													appver).commit();
										}
									}).show();// 显示对话框
				}
				break;
			case NearlyFriendActivity.GUI_TXS_UPDATA:
				// AutoSyncContactsAndSms没用已被删除
				// AutoSyncContactsAndSms.Syncing = true;
				break;
			case NearlyFriendActivity.GUI_STATE_FLUSH:

				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
					dialog = null;
				}
				if (progress != null && progress.isShowing()) {
					progress.cancel();
				}
				if (outtime != null) {
					try {
						outtime.cancel();
					} catch (Exception e) {

					}
					outtime = null;
				}
				loadflush(LOAD_HIDE);
				if (chatChannel == null) {
					if (lbsUser == null || lbsUser.size() <= 1) {
						nearlyListView.setVisibility(View.GONE);
						nearlyGridView.setVisibility(View.GONE);
						noContacts.setVisibility(View.VISIBLE);
					}
				}
				break;
			case FLUSH_CONTANTS:

				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
					dialog = null;
				}
				if (progress != null && progress.isShowing()) {
					progress.cancel();
				}
				if (outtime != null) {
					try {
						outtime.cancel();
					} catch (Exception e) {

					}
					outtime = null;
				}
				loadflush(LOAD_HIDE);

				// myconAdapter.setData(match);
				if (chatChannel == null) {
					if (lbsUser == null || lbsUser.size() == 0) {
						nearlyListView.setVisibility(View.GONE);
						nearlyGridView.setVisibility(View.GONE);
						noContacts.setVisibility(View.VISIBLE);
					} else {
						if (isChangeShow)
							nearlyGridView.setVisibility(View.VISIBLE);
						else
							nearlyListView.setVisibility(View.VISIBLE);
						noContacts.setVisibility(View.GONE);
						// synchronized(lbsUser){
						// diffSex(lbsUser,HttpHelper.QUERY_ALL,lbsUser);
						myconAdapter.setData(lbsUser);
						myconAdapter.notifyDataSetChanged();
						// }

					}
				} else {
					noContacts.setVisibility(View.GONE);
					myconAdapter.setData(lbsUser);
					myconAdapter.notifyDataSetChanged();
				}
				break;
			// AsyncImageLoader中的REFESH_UI没有被发送调用
			// case AsyncImageLoader.REFRESH_UI:
			// if (msg.arg1 == Utils.lbs_tag) {
			// if (isChangeShow) {
			// nearlyGridView.invalidateViews();
			// } else {
			// nearlyListView.invalidateViews();
			// }
			// }
			// break;
			case FLUSH_NEWCONTANTSICON:

				break;
			case FLUSH_NEWTABNEW:
				// contacts_btn_all.setChecked(true);
				break;
			case CHECK_TIMEOUT:
				if (dialog != null && dialog.isShowing()) {
					dialog.cancel();
					dialog = null;
				}
				Utils.startPromptDialog(NearlyFriendActivity.this,
						R.string.prompt, R.string.foreign_check_code_outtime);
				matchData();
				// contactsflush();
				Statflush(GUI_STATE_FLUSH);
				break;
			case LBSSocketHelper.LBS_USER_LIST:
				synchronized (templbsUser) {
					lbsUser.clear();
					lbsUser.addAll(templbsUser);
				}
				currentPage++;
				matchData();
				contactsflush();
				isConnecting = false;
				break;
			case LBSSocketHelper.LBS_SINGLE_USER:
				synchronized (templbsUser) {
					lbsUser.clear();
					lbsUser.addAll(templbsUser);
				}
				matchData();
				contactsflush();

				break;
			}
		}
	};

	private void matchData() {

	}

	private void currentPos() {
		SearchBar.posMap = posTbs;

	}

	public void onResume() {
		if (Utils.debug)
			Log.i(TAG, "NearlyFriend onResume");
		if (chatChannel == null && locationReceiver == null) {
			if (Utils.debug)
				Log.i(TAG, "注册");
			locationReceiver = new LocationReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_OK);
			filter.addAction(Constants.INTENT_ACTION_GET_LOCATION_FAILED);
			filter.addAction(Constants.LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED);
			filter.addAction(Constants.LOAD_LBS_INTENT_ACTION_NETWORK_LOCATION_FAILED);
			this.registerReceiver(locationReceiver, filter);
		}
		if (datareceiver == null) {
			datareceiver = new DataReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_FLUSH);
			this.registerReceiver(datareceiver, filter);
		}
		if (msgreceiver == null) {
			// 不会被执行，AutoSyncContactsAndSms.INTENT_ACTION_SEND_MSG这个广播不会发送，这里先注掉，后期再清理删除
			// msgreceiver = new MsgReceiver();
			// IntentFilter filter = new IntentFilter();
			// // 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			// filter.addAction(AutoSyncContactsAndSms.INTENT_ACTION_SEND_MSG);
			// this.registerReceiver(msgreceiver, filter);
		}
		if (mHandler != null) {

			LBSSocketHelper.getLBSSocketHelper(mSess.getContext())
					.registerNearbyHandler(mHandler);
			// 同onCreate方法中一样，是无用传递
			// AsyncImageLoader.getInstance(txdata).setHandler(mHandler,
			// Utils.lbs_tag);
		}
		// monimainData();
		// pageState = MESSAGES_PAGE;

		// msgStatflush();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	public void onStart() {
		// if(Utils.debug)System.out.println("**** MessageActivity2 onStart");
		// if (msghelperreceiver == null) {
		// msghelperreceiver = new MsgHelperReceiver();
		// IntentFilter filter = new IntentFilter();
		// // 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
		// filter.addAction(Constants.INTENT_ACTION_MSGHELPER_MSG);
		// this.registerReceiver(msghelperreceiver, filter);
		// }
		if (mHandler != null) {
			LBSSocketHelper.getLBSSocketHelper(mSess.getContext())
					.registerNearbyHandler(mHandler);
			// 同onCreate方法中一样，是无用传递
			// AsyncImageLoader.getInstance(txdata).setHandler(mHandler,
			// Utils.lbs_tag);
		}
		super.onStart();
	}

	public void onRestart() {
		super.onRestart();
		contactsflush();
		if (Utils.debug)
			Log.i(TAG, "NearlyFriend onRestart");

	}

	public void onPause() {
		if (Utils.debug)
			Log.i(TAG, "NearlyFriend onPause");
		// PopMenuDimss();

		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void onStop() {
		if (Utils.debug)
			Log.i(TAG, "NearlyFriend onStop");

		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {

			}
			outtime = null;
		}
		// if(ls!=null){
		// if(ls.gpsListener!=null){
		// ls.locationManager.removeUpdates(ls.gpsListener);
		// ls.gpsListener=null;
		// }
		// if(ls.networkListner!=null){
		// ls.locationManager.removeUpdates(ls.networkListner);
		// ls.networkListner=null;
		// }
		// }
		// if (msghelperreceiver != null) {
		// this.unregisterReceiver(msghelperreceiver);
		// msghelperreceiver = null;
		// }
		if (msgreceiver != null) {
			this.unregisterReceiver(msgreceiver);
			msgreceiver = null;
		}
		if (datareceiver != null) {
			this.unregisterReceiver(datareceiver);
			datareceiver = null;
		}
		if (locationReceiver != null) {
			this.unregisterReceiver(locationReceiver);
			locationReceiver = null;
		}
		// if(ls!=null){
		// if(ls.gpsListener!=null){
		// ls.locationManager.removeUpdates(ls.gpsListener);
		// ls.gpsListener=null;
		// }
		// if(ls.networkListner!=null){
		// ls.locationManager.removeUpdates(ls.networkListner);
		// ls.networkListner=null;
		// }
		// }

		AvatarDownload.removeTXHeadImgCache_gv();

		super.onStop();
	}

	public void onDestroy() {
		// stopAsyncload();
		TxData.popActivityRemove(this);
		timeCancel();

		LBSSocketHelper.getLBSSocketHelper(mSess.getContext())
				.unRegisterNearbyHandler();
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	public boolean onCreateOptionsMenu(Menu menu) {

		if (chatChannel == null) {
			MenuInflater mInflater = getMenuInflater();

			mInflater.inflate(R.menu.nearly_menu, menu);

		}
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
		case R.id.nearly_menu_clear:
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(
					NearlyFriendActivity.this);
			promptDialog.setTitle(R.string.clean_user_ifo_title);
			promptDialog.setMessage(R.string.clean_user_ifo_text);
			promptDialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
							// prefs.edit().putBoolean(CommonData.FIRST_ALERT,
							// false).commit();
							mSess.mPrefMeme.first_alert.setVal(false).commit();
							LBSSocketHelper.getLBSSocketHelper(
									mSess.getContext()).clearMyLocation();
							// HttpHelper.getInstance(NearlyFriendActivity.this).clearSelfPosition();
							NearlyFriendActivity.this.finish();
						}

					});
			promptDialog.setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
						}
					});

			promptDialog.show();
			break;
		case R.id.nearly_menu_refresh:
			loadLocationData();
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.i("Message", keyCode + "+++++++++" + event);
		//
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			// if(pageState==MESSAGES_PAGE){
			// // msglistview.findFocus();
			// View view =msglistview.getFocusedChild();
			// if(view!=null)
			// msglistview.getPositionForView(view);
			// }else if(pageState==CONTACTS_PAGE){

			// conlistview.findFocus();
			View view = nearlyListView.getFocusedChild();
			if (view != null)
				nearlyListView.getPositionForView(view);

			// }
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// TuixinService1.notNeedCheckActivityState = true;
			NearlyFriendActivity.this.finish();
			// }

			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
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
	// // ci.mBitmap = getCachedBitmap(ci.mUid);
	// // if (ci.mBitmap != null) {
	// // mAvatarHandler.obtainMessage(1, ci).sendToTarget();
	// // break;
	// // } else
	// if (ci.mUrl != null) {
	// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
	// ci.mUid, false);
	// if (file != null) {
	// File avatar = new File(file);
	// if (avatar.exists()) {
	// Bitmap bitmap = Utils.readBitMap(file, false);
	// if (bitmap != null) {
	// // ci.mBitmap=cacheBitmap(ci.mUid, bitmap);
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
	// ci.mBitmap = Utils.getRoundedCornerBitmap(bitmap);
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

	// protected void loadHeadImage(long uid, AsyncCallback<Bitmap> callback) {
	// // Bitmap bitmap = getCachedBitmap(uid);
	// // if (bitmap == null) {
	// String url = null;
	// if (uid == TX.tm.getTxMe().partner_id)
	// url = TX.tm.getTxMe().avatar_url;
	// else {
	// // TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
	// TX tx = TX.tm.getTx(uid);
	//
	//
	// if (tx != null)
	// url = tx.avatar_url;
	// }
	// if (url != null) {
	// // CallInfo callinfo = new CallInfo(url, uid, callback);
	// // mAsynloader.obtainMessage(1, callinfo).sendToTarget();
	// loadHeadImg(url, uid, callback);
	// }
	//
	//
	// // }
	// // return bitmap;
	// }

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

	}

	public static final class ConViewHolder {

		public TextView conName;
		public TextView conpone;
		public RelativeLayout lbsSex;
		public ImageView con_photo;
		public TextView tv_levle;
		public TextView sign_text;
		public TextView conage;

	}

	public class MyConAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		// private ArrayList<Base> contactsAdapter;
		private List<LBSUserInfo> txs;
		private Bitmap bm;
		private Context context;
		public int currposition = 10;

		private void setData(List<LBSUserInfo> match) {
			if (txs != null) {
				if (Utils.debug)
					Log.i(TAG, "tbs----------:" + this.txs.size()
							+ "currentView:");
			}
			txs = match;
			if (Utils.debug) {
				Log.i("Zzl5", txs.toString());
			}
		}

		public MyConAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			if (txs != null)
				if (Utils.debug)
					Log.i(TAG, "con.size:" + txs.size());
				else if (Utils.debug)
					Log.i(TAG, "con.size:" + 0);
			// txs=new ArrayList<TX>();
			this.context = context;
		}

		public int getCount() {
			if (txs != null) {
				noContacts.setVisibility(View.GONE);
				// if (menu_State==0) {
				// return txs.size() < 10 ? (txs.size() + 1) : txs.size()
				// + ((currposition - 10) / 11 + 1);
				// }else {
				return txs.size();
				// }

			}
			return 0;
		}

		// @Override
		// public int getViewTypeCount() {
		// return 2;
		// }

		// @Override
		// public int getItemViewType(int position) {
		// if (getCount() < 10 && position == getCount() - 1) {
		// return Constants.ADD_VIEW;
		// } else {
		// if ((position - 10) % 11 == 0) {
		// return Constants.ADD_VIEW;
		// } else {
		// return Constants.NORMAL_VIEW;
		// }
		// }
		//
		// }

		// public void setData(ArrayList<TX> txs){
		// this.txs=txs;
		// }
		// public ArrayList<TX> getData(){
		// return this.txs;
		// }
		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		// public int getNewPosition(int position) {
		// int n = (currposition - 10) / 11;
		// if (position - currposition < 0) {
		// return position - n;
		// } else {
		// return position - (n + 1);
		// }
		// }

		public View getView(int position, View convertView, ViewGroup parent) {

			ConViewHolder holder = null;
			if (menu_State == 0) {
				// if (getItemViewType(position) == Constants.ADD_VIEW) {
				// currposition = position;
				// if (convertView == null) {
				// AdView adView = new AdView(context, AdSize.FIT_SCREEN);
				// convertView = adView;
				// }
				// } else {
				if (convertView == null) {
					holder = new ConViewHolder();
					if (menu_State == 0) {
						convertView = mInflater.inflate(
								R.layout.sll_nearly_friend_list_item, null);
					} else {
						convertView = mInflater.inflate(
								R.layout.sll_nearly_friend_grid_item, null);
					}
					// holder.layout =
					// (LinearLayout)convertView.findViewById(R.id.contacts_list_item);
					// holder.con_new_contact = (TextView) convertView
					// .findViewById(R.id.ntuixin_contact_new_tv);
					holder.con_photo = (ImageView) convertView
							.findViewById(R.id.contact_photo);
					holder.conName = (TextView) convertView
							.findViewById(R.id.contact_name);
					holder.conpone = (TextView) convertView
							.findViewById(R.id.contact_detail);
					holder.conage = (TextView) convertView
							.findViewById(R.id.nearly_choose_tv);
					holder.lbsSex = (RelativeLayout) convertView
							.findViewById(R.id.list_item_message_type_rl);
					holder.sign_text = (TextView) convertView
							.findViewById(R.id.tx_list_item_sign_str);
					holder.sign_text.setVisibility(View.GONE);
					holder.tv_levle = (TextView) convertView
							.findViewById(R.id.tv_level);
					if (holder.tv_levle != null) {
						holder.sign_text.setVisibility(View.GONE);
					}

					convertView.setTag(holder);
				} else {
					holder = (ConViewHolder) convertView.getTag();
				}

				int id = position;

				LBSUserInfo single_msg = null;
				if (txs != null) {
					if (id >= txs.size()) {
						id = 0;
					}
					single_msg = txs.get(id);

					if (single_msg != null
							&& !Utils.isNull(single_msg.getNickName())) {
						holder.conName.setText(sParser.addSmileySpans(
								single_msg.getNickName(), true, 0));
						TX ttx = TX.tm.getTx(single_msg.getUid());
						if (ttx != null) {
							if (Utils.debug) {
								Log.i(TAG, "id是：" + ttx.partner_id + ",生日是："
										+ ttx.birthday + ",年龄是：" + ttx.age);
							}
							holder.conage.setText(""
									+ getAge("" + ttx.birthday));
						} else {
							holder.conage.setText("0");
						}
						// holder.conage.setText(String.valueOf(single_msg.getAge()));
					} else {
						holder.conName.setText("");
					}
					// if (menu_State == 0) {
					// if (TX.tm.isTxFriend(single_msg.getUid())) {
					// holder.con_new_contact.setVisibility(View.VISIBLE);
					// } else {
					// holder.con_new_contact.setVisibility(View.GONE);
					// }
					// }
					// holder.conpone.setVisibility(View.GONE);
					if (holder.tv_levle != null) {
						TX txx = mSess.getTxMgr().getTx(single_msg.getUid());
						if (txx != null) {
							if (txx.isDispalyLevel()) {
								holder.tv_levle.setVisibility(View.VISIBLE);
								holder.tv_levle
										.setText(getString(R.string.level)
												+ txx.getLevel());
							} else {
								holder.tv_levle.setVisibility(View.INVISIBLE);
							}
						}
					}

					if (single_msg.getUid() == TX.tm.getUserID()) {
						holder.conpone.setText("0m");
					} else if (single_msg.getDist() <= 50) {
						// if(single_msg.getDist()==0){
						// holder.conpone.setText("");
						// }else{
						holder.conpone.setText("50m");
						// }
					} else if (single_msg.getDist() > 50
							&& single_msg.getDist() <= 100) {
						holder.conpone.setText(100 + "m");
					} else if (single_msg.getDist() > 100
							&& single_msg.getDist() <= 999) {
						holder.conpone.setText((single_msg.getDist() / 100)
								* 100 + "m");
					} else if (single_msg.getDist() >= 1000) {
						holder.conpone.setText((single_msg.getDist() / 1000)
								+ "km");
					}

					if (!Utils.isNull(single_msg.getSignature())) {
						holder.sign_text.setVisibility(View.VISIBLE);
						holder.sign_text.setText(sParser.addSmileySpans(
								single_msg.getSignature(), true, 0));
					} else {

						holder.sign_text.setVisibility(View.GONE);
						holder.sign_text.setText(NearlyFriendActivity.this
								.getResources()
								.getString(R.string.sign_context));
					}
					if (single_msg.getSex() == -1) {
						holder.lbsSex.setVisibility(View.GONE);
					} else {
						holder.lbsSex.setVisibility(View.VISIBLE);
						if (single_msg.getSex() == 0) {

							holder.lbsSex
									.setBackgroundResource(R.drawable.user_infor_sex_boy);
							defaultHeaderImg = defaultHeaderImgMan;

						} else {

							holder.lbsSex
									.setBackgroundResource(R.drawable.user_infor_sex_girl);
							defaultHeaderImg = defaultHeaderImgFemale;

						}
					}
					if (!Utils.checkSDCard()) {// 无SD卡
						holder.con_photo.setImageResource(defaultHeaderImg);
					} else {
						Bitmap bm = mSess.avatarDownload
								.getPartnerCachedBitmap_nearltGv(single_msg
										.getUid());
						if (bm != null) {
							if (menu_State == 0) {
								holder.con_photo.setImageBitmap(Utils
										.getRoundedCornerBitmap(bm));
							} else {
								holder.con_photo.setImageBitmap(bm);
							}

						} else {
							ImageView imageView = holder.con_photo;
							imageView.setTag(single_msg.getUid());
							TX tx = TX.tm.getTx(single_msg.getUid());
							if (tx != null) {
								// if(menu_State == 0){
								// loadHeadImg_nearGv(tx.avatar_url,
								// tx.partner_id,
								// callback);
								// }else{
								// loadHeadImg_nearGv(tx.avatar_url,
								// tx.partner_id,
								// callback);
								// }

								mSess.avatarDownload.downAvatar_nearly(
										tx.avatar_url, tx.partner_id,
										imageView, avatarHandler);
							}

							imageView.setImageResource(defaultHeaderImg);
						}
					}
				}
				final int index = position;
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						LBSUserInfo lbstx = txs.get(index);
						// TX tx =
						// TX.findTXByPartnerID4DB(lbstx.getUid());//不要直接访问数据库
						// 2013.10.17 shc
						TX tx = TX.tm.getTx(lbstx.getUid());

						if (tx == null && lbstx != null) {
							tx = new TX();
							tx.setPartnerId(lbstx.getUid());
							tx.setNick_name(lbstx.getNickName());
							tx.setAvatar_url(lbstx.getAvatar());
							if (!Utils.isNull(lbstx.getSignature())) {
								tx.setSign(lbstx.getSignature());
							} else {
								tx.setSign(NearlyFriendActivity.this
										.getResources().getString(
												R.string.sign_context));
							}
							tx.setSex(lbstx.getSex());
						}
						if (lbstx.getUid() != TX.tm.getUserID()) {
							if (!TX.tm.isTxFriend(lbstx.getUid())) {
								Intent intent = new Intent(
										NearlyFriendActivity.this,
										UserInformationActivity.class);
								intent.putExtra(
										UserInformationActivity.pblicInfo,
										UserInformationActivity.NOT_TUIXIN_USER_INFO);
								intent.putExtra(UserInformationActivity.UID,
										tx.partner_id);
								startActivity(intent);
							} else {
								Intent intent = new Intent(
										NearlyFriendActivity.this,
										UserInformationActivity.class);
								intent.putExtra(
										UserInformationActivity.pblicInfo,
										UserInformationActivity.TUIXIN_USER_INFO);
								intent.putExtra(UserInformationActivity.UID,
										tx.partner_id);
								startActivity(intent);
							}

							/*
							 * Intent intent = new
							 * Intent(NearlyFriendActivity.this,
							 * FindTxInfoActivity.class);
							 * intent.putExtra(FindTxInfoActivity.pblicInfo,
							 * FindTxInfoActivity.LBSTOFINDTXINFO);
							 * intent.putExtra("lbstx", lbstx);
							 * startActivity(intent);
							 */
							/*
							 * Intent intent = new
							 * Intent(NearlyFriendActivity.this,
							 * FindTxInfoActivity.class);
							 * intent.putExtra(FindTxInfoActivity.pblicInfo,
							 * FindTxInfoActivity.HAOYOUTOFINDTXINFO);
							 * intent.putExtra("tx", tx); startActivity(intent);
							 */
						} else {
							/*
							 * Intent iUserInfo = new
							 * Intent(NearlyFriendActivity.this,
							 * UserInfoActivity.class);
							 * iUserInfo.putExtra(UserInfoActivity .pblicInfo,
							 * UserInfoActivity.SETINGTOUSERINFO);
							 * startActivity(iUserInfo);
							 */
							Intent iUserInfo = new Intent(
									NearlyFriendActivity.this,
									SetUserInfoActivity.class);
							startActivity(iUserInfo);

						}
					}
				});
				// }
			} else {
				if (convertView == null) {
					holder = new ConViewHolder();
					if (menu_State == 0) {
						convertView = mInflater.inflate(
								R.layout.sll_nearly_friend_list_item, null);
					} else {
						convertView = mInflater.inflate(
								R.layout.sll_nearly_friend_grid_item, null);
					}
					// holder.layout =
					// (LinearLayout)convertView.findViewById(R.id.contacts_list_item);
					// holder.con_new_contact = (TextView) convertView
					// .findViewById(R.id.ntuixin_contact_new_tv);
					holder.con_photo = (ImageView) convertView
							.findViewById(R.id.contact_photo);
					holder.conName = (TextView) convertView
							.findViewById(R.id.contact_name);
					holder.conpone = (TextView) convertView
							.findViewById(R.id.contact_detail);
					holder.conage = (TextView) convertView
							.findViewById(R.id.nearly_choose_tv);
					holder.lbsSex = (RelativeLayout) convertView
							.findViewById(R.id.list_item_message_type_rl);
					holder.sign_text = (TextView) convertView
							.findViewById(R.id.tx_list_item_sign_str);
					holder.sign_text.setVisibility(View.GONE);
					holder.tv_levle = (TextView) convertView
							.findViewById(R.id.tv_level);
					if (holder.tv_levle != null) {
						holder.sign_text.setVisibility(View.GONE);
					}

					convertView.setTag(holder);
				} else {
					holder = (ConViewHolder) convertView.getTag();
				}

				int id = position;

				LBSUserInfo single_msg = null;
				if (txs != null) {
					if (id >= txs.size()) {
						id = 0;
					}
					single_msg = txs.get(id);

					if (single_msg != null
							&& !Utils.isNull(single_msg.getNickName())) {
						holder.conName.setText(sParser.addSmileySpans(
								single_msg.getNickName(), true, 0));
						TX ttx = TX.tm.getTx(single_msg.getUid());
						if (ttx != null) {
							if (Utils.debug) {
								Log.i(TAG, "id是：" + ttx.partner_id + ",生日是："
										+ ttx.birthday + ",年龄是：" + ttx.age);
							}
							holder.conage.setText(""
									+ getAge("" + ttx.birthday));
						} else {
							holder.conage.setText("0");
						}
						// holder.conage.setText(String.valueOf(single_msg.getAge()));
					} else {
						holder.conName.setText("");
					}
					// if (menu_State == 0) {
					// if (TX.tm.isTxFriend(single_msg.getUid())) {
					// holder.con_new_contact.setVisibility(View.VISIBLE);
					// } else {
					// holder.con_new_contact.setVisibility(View.GONE);
					// }
					// }
					// holder.conpone.setVisibility(View.GONE);
					if (holder.tv_levle != null) {
						TX txx = mSess.getTxMgr().getTx(single_msg.getUid());
						if (txx != null) {
							if (txx.isDispalyLevel()) {
								holder.tv_levle.setVisibility(View.VISIBLE);
								holder.tv_levle
										.setText(getString(R.string.level)
												+ txx.getLevel());
							} else {
								holder.tv_levle.setVisibility(View.INVISIBLE);
							}
						}
					}

					if (single_msg.getUid() == TX.tm.getUserID()) {
						holder.conpone.setText("0m");
					} else if (single_msg.getDist() <= 50) {
						// if(single_msg.getDist()==0){
						// holder.conpone.setText("");
						// }else{
						holder.conpone.setText("50m");
						// }
					} else if (single_msg.getDist() > 50
							&& single_msg.getDist() <= 100) {
						holder.conpone.setText(100 + "m");
					} else if (single_msg.getDist() > 100
							&& single_msg.getDist() <= 999) {
						holder.conpone.setText((single_msg.getDist() / 100)
								* 100 + "m");
					} else if (single_msg.getDist() >= 1000) {
						holder.conpone.setText((single_msg.getDist() / 1000)
								+ "km");
					}

					if (!Utils.isNull(single_msg.getSignature())) {
						holder.sign_text.setVisibility(View.VISIBLE);
						holder.sign_text.setText(sParser.addSmileySpans(
								single_msg.getSignature(), true, 0));
					} else {

						holder.sign_text.setVisibility(View.GONE);
						holder.sign_text.setText(NearlyFriendActivity.this
								.getResources()
								.getString(R.string.sign_context));
					}
					if (single_msg.getSex() == -1) {
						holder.lbsSex.setVisibility(View.GONE);
					} else {
						holder.lbsSex.setVisibility(View.VISIBLE);
						if (single_msg.getSex() == 0) {

							holder.lbsSex
									.setBackgroundResource(R.drawable.user_infor_sex_boy);
							defaultHeaderImg = defaultHeaderImgMan;

						} else {

							holder.lbsSex
									.setBackgroundResource(R.drawable.user_infor_sex_girl);
							defaultHeaderImg = defaultHeaderImgFemale;

						}
					}
					if (!Utils.checkSDCard()) {// 无SD卡
						holder.con_photo.setImageResource(defaultHeaderImg);
					} else {
						Bitmap bm = mSess.avatarDownload
								.getPartnerCachedBitmap_nearltGv(single_msg
										.getUid());
						if (bm != null) {
							if (menu_State == 0) {
								holder.con_photo.setImageBitmap(Utils
										.getRoundedCornerBitmap(bm));
							} else {
								holder.con_photo.setImageBitmap(bm);
							}

						} else {
							ImageView imageView = holder.con_photo;
							imageView.setTag(single_msg.getUid());
							TX tx = TX.tm.getTx(single_msg.getUid());
							if (tx != null) {
								// if(menu_State == 0){
								// loadHeadImg_nearGv(tx.avatar_url,
								// tx.partner_id,
								// callback);
								// }else{
								// loadHeadImg_nearGv(tx.avatar_url,
								// tx.partner_id,
								// callback);
								// }

								mSess.avatarDownload.downAvatar_nearly(
										tx.avatar_url, tx.partner_id,
										imageView, avatarHandler);
							}

							imageView.setImageResource(defaultHeaderImg);
						}
					}
				}
				final int index = position;
				convertView.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						LBSUserInfo lbstx = txs.get(index);
						// TX tx =
						// TX.findTXByPartnerID4DB(lbstx.getUid());//不要直接访问数据库
						// 2013.10.17 shc
						TX tx = TX.tm.getTx(lbstx.getUid());

						if (tx == null && lbstx != null) {
							tx = new TX();
							tx.setPartnerId(lbstx.getUid());
							tx.setNick_name(lbstx.getNickName());
							tx.setAvatar_url(lbstx.getAvatar());
							if (!Utils.isNull(lbstx.getSignature())) {
								tx.setSign(lbstx.getSignature());
							} else {
								tx.setSign(NearlyFriendActivity.this
										.getResources().getString(
												R.string.sign_context));
							}
							tx.setSex(lbstx.getSex());
						}
						if (lbstx.getUid() != TX.tm.getUserID()) {
							if (!TX.tm.isTxFriend(lbstx.getUid())) {
								Intent intent = new Intent(
										NearlyFriendActivity.this,
										UserInformationActivity.class);
								intent.putExtra(
										UserInformationActivity.pblicInfo,
										UserInformationActivity.NOT_TUIXIN_USER_INFO);
								intent.putExtra(UserInformationActivity.UID,
										tx.partner_id);
								startActivity(intent);
							} else {
								Intent intent = new Intent(
										NearlyFriendActivity.this,
										UserInformationActivity.class);
								intent.putExtra(
										UserInformationActivity.pblicInfo,
										UserInformationActivity.TUIXIN_USER_INFO);
								intent.putExtra(UserInformationActivity.UID,
										tx.partner_id);
								startActivity(intent);
							}

							/*
							 * Intent intent = new
							 * Intent(NearlyFriendActivity.this,
							 * FindTxInfoActivity.class);
							 * intent.putExtra(FindTxInfoActivity.pblicInfo,
							 * FindTxInfoActivity.LBSTOFINDTXINFO);
							 * intent.putExtra("lbstx", lbstx);
							 * startActivity(intent);
							 */
							/*
							 * Intent intent = new
							 * Intent(NearlyFriendActivity.this,
							 * FindTxInfoActivity.class);
							 * intent.putExtra(FindTxInfoActivity.pblicInfo,
							 * FindTxInfoActivity.HAOYOUTOFINDTXINFO);
							 * intent.putExtra("tx", tx); startActivity(intent);
							 */
						} else {
							/*
							 * Intent iUserInfo = new
							 * Intent(NearlyFriendActivity.this,
							 * UserInfoActivity.class);
							 * iUserInfo.putExtra(UserInfoActivity.pblicInfo,
							 * UserInfoActivity.SETINGTOUSERINFO);
							 * startActivity(iUserInfo);
							 */
							Intent iUserInfo = new Intent(
									NearlyFriendActivity.this,
									SetUserInfoActivity.class);
							startActivity(iUserInfo);

						}
					}
				});
			}

			return convertView;
		}

	}

	// AsyncCallback<Bitmap> callback = new AsyncCallback<Bitmap>() {
	// @Override
	// public void onSuccess(Bitmap result, long id) {
	// ImageView iv = null;
	// if (menu_State == 0) {
	// iv = (ImageView) nearlyListView.findViewWithTag(Long
	// .valueOf(id).intValue());
	//
	// if (result != null && iv != null) {
	// iv.setImageBitmap(Utils.getRoundedCornerBitmap(result));
	// }
	//
	// } else {
	// iv = (ImageView) nearlyGridView.findViewWithTag(Long
	// .valueOf(id).intValue());
	// if (result != null && iv != null) {
	// iv.setImageBitmap(result);
	// }
	//
	// }
	// }
	// @Override
	// public void onFailure(Throwable t, long id) {
	//
	// }
	// };
	//
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_NEARLY:
				Object[] result = (Object[]) msg.obj;

				ImageView iv = null;
				if (menu_State == 0) {
					iv = (ImageView) nearlyListView.findViewWithTag(Long
							.valueOf((Long) result[1]).intValue());
					if (iv != null) {
						int tag = (Integer) iv.getTag();
						long id = (Long) result[1];
						if (result != null && tag == id) {
							iv.setImageBitmap(Utils
									.getRoundedCornerBitmap((Bitmap) result[0]));
						}
					}

				} else {
					iv = (ImageView) nearlyGridView.findViewWithTag(Long
							.valueOf((Long) result[1]).intValue());
					if (iv != null) {
						long tag = (Integer) iv.getTag();
						long id = (Long) result[1];
						if (result[0] != null && tag == id) {
							iv.setImageBitmap((Bitmap) result[0]);
						}
					}
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private TextView mid_title_name_choose;

	public void monimainData() {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				if (Utils.debug)
					Log.i(TAG, "monimainData 刷新");
				// initTX();
				matchData();
				currentPos();
				contactsflush();
				// searchBarVisible(true);

			}
		});

	}

	public class MsgReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = null;// intent.getStringExtra(AutoSyncContactsAndSms.SEND_KEY);
			if (!Utils.isNull(msg)) {
				if (msg.equals("complete")) {
					monimainData();
				}
			}
		}
	}

	public class DataReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			final String msg = intent.getStringExtra("msg");
			if (msg != null) {
				if (msg.equals(TxData.FLUSH_TXS)) {
					matchData();
					contactsflush();
				}
			}
		}
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				NearlyFriendActivity.this);
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

	public void showLoading() {
		dialog = new ProgressDialog(this);
		dialog.setMessage("正在定位中.....");
		dialog.setCancelable(true);
		dialog.show();
	}

	public void timeOut() {
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

				Message msg = new Message();
				msg.what = CHECK_TIMEOUT;
				mHandler.sendMessage(msg);
			}
		}, 70000);
	}

	public void timeCancel() {
		if (outtime != null) {
			try {
				outtime.cancel();
			} catch (Exception e) {

			}
			outtime = null;
		}
	}

	public void removeDuplicateWithOrder(List<LBSUserInfo> list) {
		Set<LBSUserInfo> set = new HashSet<LBSUserInfo>();
		List<LBSUserInfo> newList = new ArrayList<LBSUserInfo>();
		for (Iterator<LBSUserInfo> iter = list.iterator(); iter.hasNext();) {
			LBSUserInfo element = iter.next();
			if (set.add(element))
				newList.add(element);
		}
		list.clear();
		list.addAll(newList);
	}

	public static void removeDuplicateSex(ArrayList<LBSUserInfo> list, int sex) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getSex() == sex) {
				list.remove(i);
			}
		}
	}

	public List<LBSUserInfo> diff(List<LBSUserInfo> arrayNew) {
		// try {
		ArrayList<LBSUserInfo> Array = new ArrayList<LBSUserInfo>();
		if (arrayNew == null) {
			return Array;
		}
		if (arrayNew.size() == 1) {
			arrayNew.remove(0);
			return arrayNew;
		}
		// Array = arrayNew;
		for (int i = 0; i < arrayNew.size(); i++) {
			for (int j = 0; j < i; j++) {
				if (arrayNew.get(j).getUid() == (arrayNew.get(i).getUid())) {
					arrayNew.remove(i);
					i--;
					break;
				}
			}
		}

		return arrayNew;

		// } catch (Exception ex) {
		// System.out.println("error:"+ex.toString());
		// return null;
		// }
	}

	public List<LBSUserInfo> diffSex(List<LBSUserInfo> arrayNew, int sex,
			List<LBSUserInfo> lbsarrayNew) {
		try {
			List<LBSUserInfo> tempArray = new ArrayList<LBSUserInfo>();
			tempArray.addAll(arrayNew);
			if (arrayNew == null) {
				return lbsarrayNew;
			}
			if (tempArray.size() == 1) {
				tempArray.remove(0);
				return tempArray;
			}
			List<LBSUserInfo> tempArray1 = new ArrayList<LBSUserInfo>();
			tempArray1.clear();
			for (LBSUserInfo lbsfo : tempArray) {

				if (lbsfo.getSex() == sex) {
					tempArray1.add(lbsfo);

				}
			}
			// tempArray.removeAll(tempArray1);
			lbsarrayNew.addAll(tempArray1);
			return lbsarrayNew;

		} catch (Exception ex) {
			return null;
		}
	}

	/** 通过生日获得年龄 */
	private int getAge(String birthday) {
		if (!TextUtils.isEmpty(birthday) && birthday.length() == 8) {
			// 如果birthday不为空，设置年龄
			try {
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
				Calendar birCal = Calendar.getInstance();
				birCal.setTime(sdf.parse(birthday));
				Calendar curCal = Calendar.getInstance();
				curCal.setTime(new Date());
				int tempAge = curCal.get(Calendar.YEAR)
						- birCal.get(Calendar.YEAR);
				return tempAge > 0 ? tempAge : 0;
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "转换生日异常", e);
			}
		}
		return 0;
	}

}