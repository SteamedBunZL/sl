package com.tuixin11sms.tx.activity;

import java.util.ArrayList;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;

import com.shenliao.group.activity.GroupIndex;
import com.shenliao.set.activity.TabMoreActivity;
import com.tuixin11sms.tx.MyUncaughtExceptionHandler;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class TuiXinMainTab extends TabActivity {
	public static TabHost tHost;
	// public TxData txData;
	// private static SharedPreferences prefs = null;
	// private Editor editor;
	private LayoutInflater mInflater;
	private sendReceiver sr;
	private singleMsgReceiver smr;// 接收到单聊通知的广播
	public static boolean sendWeibo;
	public static final int MESSAGES_PAGE = 0;
	public static final int AccostMsg_PAGE = 1;
	public static final int CONTACTS_PAGE = 2;
	public static final int ADD_CONTACTS_PAGE = 3;
	public static final int SET_PAGE = 4;
	public static final int RESULT_DELAPP = 20;
	public static int[] tabHostBgNormal = null;
	public static int[] tabHostBgPressed = null;
	private SessionManager mSess;
	private constactsReceiver ctr;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main_tab);
		// TuixinService1.OnApp = true;
		System.gc();
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		// txData = (TxData) this.getApplication();

		mSess = SessionManager.getInstance();

		tabHostBgNormal = new int[] { R.drawable.di_03, R.drawable.di_05,
				R.drawable.di_07, R.drawable.di_09, R.drawable.di_11,
				R.drawable.di_05 };
		tabHostBgPressed = new int[] { R.drawable.di_on_03,
				R.drawable.di_on_05, R.drawable.di_on_07, R.drawable.di_on_09,
				R.drawable.di_on_11, R.drawable.di_on_05 };
		// boolean TutorialTeach = prefs.getBoolean(CommonData.TEACHER, false);
		boolean TutorialTeach = mSess.mPrefMeme.teacher.getVal();

		if (!TutorialTeach) {
			Intent helpIntent = new Intent(TuiXinMainTab.this,
					TutorialTeachActivity.class);
			this.finish();
			startActivity(helpIntent);
			// TODO这里没有committed，起作用了吗？？
			// editor.putBoolean(CommonData.TEACHER, true);
			mSess.mPrefMeme.teacher.setVal(true);
			return;
		}
		// 如果是用新浪微博账号进行第二次登录神聊的话，发微博
		Intent intent = getIntent();
		if (intent != null) {
			sendWeibo = intent.getBooleanExtra("sendWeibo", false);
		}
		tHost = getTabHost();
		// 判断mSess.mPrefInfor.isLevelUp == null,防止退出在登录异常
		if (mSess.mPrefInfor.isLevelUp == null) {
			mSess.mPrefInfor.initFiled(TX.tm.getUserID());
		}
		mInflater = LayoutInflater.from(TuiXinMainTab.this);
		tabItemView(R.drawable.icon, R.string.bottom_name_message,
				MessageActivity.class);
		tabItemView(R.drawable.icon, R.string.bottom_name_msgroom,
				GroupIndex.class);
		tabItemView(R.drawable.icon, R.string.bottom_name_search,
				SearchFriendActivity.class);
		tabItemView(R.drawable.icon, R.string.bottom_name_contants,
				TuixinContactsActivity.class);
		tabItemView(R.drawable.icon, R.string.bottom_name_more,
				TabMoreActivity.class);

		tHost.setCurrentTab(AccostMsg_PAGE);
		// 初始化设置一次标签背景
		updateTabBackground(tHost);
		updateNoReadMsg(tHost);
		// 选择时背景更改。
		tHost.setOnTabChangedListener(new OnTabChangeListener() {
			@Override
			public void onTabChanged(String tabId) {
				updateTabInfoIcon(tHost);
				updateNoReadMsg(tHost);
				if (tabId.equals(getResources().getString(
						R.string.bottom_name_more))) {
					if (Utils.ismUserInfoComplete() && isIconShow(tHost)) {
						setIcon(View.GONE, tHost);
					}
				}
				updateTabBackground(tHost);
			}
		});

	}
	

	/**
	 * 获取缓存的msgstats,遍历是否有未读
	 */
	private void setNoReadMsg() {
		// 获取缓存的msgstats,遍历是否有未读
		ArrayList<MsgStat> msgStats = new ArrayList<MsgStat>(
				MsgStat.getMsgStatsList());
		if (msgStats != null) {
			for (MsgStat msgStat : msgStats) {
				if (msgStat.no_read != 0) {
					mSess.mPrefInfor.isNoReadMsg.setVal(true).commit();
					return;
				} else {
					mSess.mPrefInfor.isNoReadMsg.setVal(false).commit();
				}
			}
		}
	}

	@Override
	protected void onStart() {
		if (smr == null) {
			smr = new singleMsgReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_NEW_SINGLE_MSG);
			registerReceiver(smr, filter);
		}
		if (ctr == null) {
			ctr = new constactsReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.CONSTACTS_RED_SHOW);
			filter.addAction(Constants.CONSTACTS_RED_UN_SHOW);
			registerReceiver(ctr, filter);
		}
		updateTabInfoIcon(tHost);
		if (MsgStat.IsConstactHasUnReadMessage()) {
			updateConstactMsg(tHost, Constants.CONSTACTS_RED_SHOW);
		} else {
			updateConstactMsg(tHost, Constants.CONSTACTS_RED_UN_SHOW);
		}
		super.onStart();
	}

	private View tabItemView(int tabicon, int text, Class paramClass) {

		View itemView = this.mInflater
				.inflate(R.layout.sll_main_tab_item, null);
		// itemView.setBackgroundResource(R.drawable.group_tabs_backgound);
		((ImageView) itemView.findViewById(R.id.iv_tab_item_icon))
				.setImageResource(tabicon);
		((TextView) itemView.findViewById(R.id.tv_tab_item_name)).setText(text);
		// ((TextView) itemView.findViewById(R.id.tv_tab_item_name))
		// .setVisibility(View.GONE);
		String str = getResources().getString(text);
		Intent itemIntent = null;
		if (paramClass != null) {
			itemIntent = new Intent().setClass(this, paramClass);

		}
		TabHost.TabSpec tabSpec = tHost.newTabSpec(str).setIndicator(itemView)
				.setContent(itemIntent);
		tHost.addTab(tabSpec);
		return itemView;
	}

	/**
	 * 更新消息红点
	 * 
	 * @param tabHost
	 */
	public void updateNoReadMsg(TabHost tabHost) {
		// 消息页面气泡
		setNoReadMsg();
		View view = tHost.getTabWidget().getChildAt(0);
		ImageView iconImage = (ImageView) view
				.findViewById(R.id.iv_tab_item_unread);
		if (mSess.mPrefInfor.isNoReadMsg.getVal()) {
			iconImage.setVisibility(View.VISIBLE);
		} else {
			iconImage.setVisibility(View.GONE);
		}
		if (Utils.debug)
			Log.i("MY", "" + mSess.mPrefInfor.isNoReadMsg.getVal());
	}

	/**
	 * 更新设置标题栏，是否资料完整显示气泡
	 * 
	 * @param tabHost
	 */
	public void updateTabInfoIcon(TabHost tabHost) {
		View view = tabHost.getTabWidget().getChildAt(4);
		ImageView iconImage = (ImageView) view
				.findViewById(R.id.iv_tab_item_unread);
		boolean isCompete = Utils.ismUserInfoComplete();
		if (!isCompete || mSess.mPrefInfor.isLevelUp.getVal()) {
			iconImage.setVisibility(View.VISIBLE);
		} else {
			iconImage.setVisibility(View.GONE);
		}
	}

	/**
	 * 判断是否是显示状态
	 * 
	 * @param tabHost
	 * @return
	 */
	public boolean isIconShow(TabHost tabHost) {
		View view = tabHost.getTabWidget().getChildAt(4);
		ImageView iconImage = (ImageView) view
				.findViewById(R.id.iv_tab_item_unread);
		int visibility = iconImage.getVisibility();
		return visibility == View.VISIBLE ? true : false;
	}

	public void setIcon(int visibility, TabHost tabHost) {
		View view = tabHost.getTabWidget().getChildAt(4);
		ImageView iconImage = (ImageView) view
				.findViewById(R.id.iv_tab_item_unread);
		iconImage.setVisibility(visibility);
	}

	/**
	 * 更新Tab标签的背景图
	 * 
	 * @param tabHost
	 */
	public void updateTabBackground(final TabHost tabHost) {
		int sum = tabHost.getTabWidget().getChildCount();
		for (int i = 0; i < sum; i++) {
			View vvv = tabHost.getTabWidget().getChildAt(i);
			if (tabHost.getCurrentTab() == i) {
				// 选中后的背景
				((ImageView) vvv.findViewById(R.id.iv_tab_item_icon))
						.setImageResource(tabHostBgPressed[i]);
				((TextView) vvv.findViewById(R.id.tv_tab_item_name))
						.setTextColor(getResources().getColor(
								R.color.content_color_blue));
				// vvv.setBackgroundDrawable(getResources().getDrawable(tabHostBgNormal[i]));
			} else {
				// 非选择的背景
				((ImageView) vvv.findViewById(R.id.iv_tab_item_icon))
						.setImageResource(tabHostBgNormal[i]);
				((TextView) vvv.findViewById(R.id.tv_tab_item_name))
						.setTextColor(Color.parseColor("#7d879c"));
				// vvv.setBackgroundDrawable(getResources().getDrawable(tabHostBgPressed[i]));
			}
		}
	}

	private class sendReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.INTENT_ACTION_SEND_PWD_MSG)) {
				Boolean isSetpwd = intent.getBooleanExtra("isSetpwd", true);
				updateTabInfoIcon(tHost);
				// 切换背景
				if (isSetpwd) {
					tabHostBgNormal = new int[] { R.drawable.di_03,
							R.drawable.di_05, R.drawable.di_07,
							R.drawable.di_09, R.drawable.di_11 };
					tabHostBgPressed = new int[] { R.drawable.di_on_03,
							R.drawable.di_on_05, R.drawable.di_on_07,
							R.drawable.di_on_09, R.drawable.di_on_11 };
					updateTabBackground(tHost);
				} else {
					// tabHostBgNormal = new
					// int[]{R.drawable.btn_inbox_normal,R.drawable.btn_tuixin_talk_normal,R.drawable.btn_contacts_normal,R.drawable.btn_tuixin_contacts_normal,R.drawable.btn_settings_normal_warm};
					// tabHostBgPressed = new
					// int[]{R.drawable.btn_inbox_pressed,R.drawable.btn_tuixin_talk_pressed,R.drawable.btn_contacts_pressed,R.drawable.btn_tuixin_contacts_pressed,R.drawable.btn_settings_pressed_warm};
					// updateTabBackground(tHost);
				}
			}

		}

		// HandlerThread localHandlerThread;
		// Handler mHandler;
		// private void myexcatch() {
		// localHandlerThread = new HandlerThread("myexcatch");
		// localHandlerThread.start();
		// mHandler = new Handler(localHandlerThread.getLooper());
		/*
		 * Thread .setDefaultUncaughtExceptionHandler(new
		 * MyUncaughtExceptionHandler( TuiXinMainTab.this));
		 */

	}

	private class singleMsgReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
				if (intent.getAction().equals(Constants.INTENT_ACTION_NEW_SINGLE_MSG)) {
					updateNoReadMsg(tHost);
				}
		

		}
	}

	private class constactsReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.CONSTACTS_RED_SHOW)) {
				updateConstactMsg(tHost, Constants.CONSTACTS_RED_SHOW);
			} else if (intent.getAction().equals(Constants.CONSTACTS_RED_UN_SHOW)) {
				updateConstactMsg(tHost, Constants.CONSTACTS_RED_UN_SHOW);
			}
		}

	}

	private void updateConstactMsg(TabHost tabHost, String opera) {
		View view = tHost.getTabWidget().getChildAt(3);
		ImageView iconImage = (ImageView) view
				.findViewById(R.id.iv_tab_item_unread);
		if (opera.equals(Constants.CONSTACTS_RED_SHOW)) {
			iconImage.setVisibility(View.VISIBLE);
		} else {
			iconImage.setVisibility(View.GONE);
		}
	}

	protected void onDestroy() {
		if (sr != null) {
			this.unregisterReceiver(sr);
			sr = null;
		}
		if (smr != null) {
			this.unregisterReceiver(smr);
			smr = null;
		}
		if (ctr !=null) {
			this.unregisterReceiver(ctr);
			ctr = null;
		}
		// if(localHandlerThread!=null)
		// localHandlerThread.getLooper().quit();
		// if(Utils.debug)BLog.i(TAG, "+++++++++++++++ onDestroy");
		// TuixinService.activityState.remove(TAG);
		// TuixinService.OnApp=false;
		// if(prefs==null){
		// prefs = getSharedPreferences(CommonData.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// }
		// prefs.unregisterOnSharedPreferenceChangeListener(mySharedPreferenceListener);
		// TxData.popActivityRemove(this);
		super.onDestroy();
	}

	// HandlerThread localHandlerThread;
	// Handler mHandler;
	private void myexcatch() {
		// localHandlerThread = new HandlerThread("myexcatch");
		// localHandlerThread.start();
		// mHandler = new Handler(localHandlerThread.getLooper());
		Thread.setDefaultUncaughtExceptionHandler(new MyUncaughtExceptionHandler(
				TuiXinMainTab.this));
	}
}
