package com.tuixin11sms.tx.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.AsyncTask;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupNewsActivity;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.SearchBar;
import com.umeng.analytics.MobclickAgent;

/**
 * 主页面tabhost中的好友标签页面
 */
public class TuixinContactsActivity extends BaseActivity {
	private static final String TAG = TuixinContactsActivity.class
			.getSimpleName();
	private final int FLUSH_CONTANTS = 101;
	// private final int CHECK_VER_TIMEOUT = 103;
	// private final int CHECK_VER_NOT_NEEDUP = 105;
	private final int DEL_PARTNER_TIMEOUT = 107;
	private final int DEL_PARTNER = 108;
	private final int DEL_PARTNER_SUCCEE = 109;
	private final int DEL_PARTNER_FAILED = 110;
	private final int FLUSH_GROUPS = 111;// 刷新我的群组
	private UpdateReceiver updatereceiver;
	private DataReceiver datareceiver;
	public ContentResolver cr;
	private SmileyParser sParser;
	private SmileyParser sysParser;
	private boolean isFriendSelected = true;// 好友列表是否被选中。
	private ArrayList<TX> tbTxList = new ArrayList<TX>();// 神聊联系人
	private Map<Long, TX> matchedTX = new LinkedHashMap<Long, TX>();// 为了出现重复好友，用hashMap来存储需要显示的TX
	private List<TxGroup> txGroupList = new ArrayList<TxGroup>();// 我的群组

	private List<ImageView> ivs = new ArrayList<ImageView>();

	private ListView conListView;
	private RelativeLayout rl_no_contacts;// 没有联系人的提示布局
	private SearchBar searchBar = null;
	private MyTuixinConAdapter myconAdapter;
	private MyGroupsAdapter myGroupsAdapter;
	private Map<String, Integer> txPinyinInitialMap;// key为tx拼音首字母，value为position位置
	private TextView tv_my_friend_tab;// 我的好友tab
	private TextView tv_my_group_tab;// 我的群组tab
	private TextView tv_over;
	private int isqut;
	private Toast exitToast;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (Utils.debug)
			Log.i(TAG, "TuixinContacts OnCreate");

		TxData.addActivity(this);
		setContentView(R.layout.activity_contacts);

		sParser = new SmileyParser(this);
		sParser.setInTuixin(true);
		sysParser = new SmileyParser(this);
		sysParser.setInChatView(true);
		cr = this.getContentResolver();
		View addContacts = findViewById(R.id.iv_add_contacts);
		addContacts.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 添加好友
				Intent intent = new Intent(thisContext,
						AddContactsActivity.class);
				startActivity(intent);
			}
		});

		conListView = (ListView) findViewById(R.id.list_contacts);
		rl_no_contacts = (RelativeLayout) findViewById(R.id.rl_no_contacts);
		RelativeLayout rl_contacts_tab = (RelativeLayout) findViewById(R.id.rl_contacts_tab);
		tv_my_friend_tab = (TextView) rl_contacts_tab
				.findViewById(R.id.tv_my_friend_tab);
		tv_my_group_tab = (TextView) rl_contacts_tab
				.findViewById(R.id.tv_my_group_tab);
		rl_contacts_tab.findViewById(R.id.tv_my_group_tab);
		txPinyinInitialMap = new HashMap<String, Integer>();
		TextView promptText = (TextView) findViewById(R.id.prompt_text);
		tv_over = (TextView) findViewById(R.id.list_over);
		searchBar = (SearchBar) findViewById(R.id.con_tuixin_mSearchBar);
		searchBar.setTv(promptText);
		SearchBar.listview = conListView;
		SearchBar.posMap = txPinyinInitialMap;
		myconAdapter = new MyTuixinConAdapter(TuixinContactsActivity.this);
		myGroupsAdapter = new MyGroupsAdapter();
		conListView.setAdapter(myconAdapter);
		// 触摸联系人listView就隐藏软键盘
		RelativeLayout linear = (RelativeLayout) findViewById(R.id.linearLayout1);
		linear.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) TuixinContactsActivity.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				if (imm != null && imm.isActive()) {
					imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				}

				return false;
			}
		});

		tv_my_friend_tab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 显示我的好友list
				if (!isFriendSelected) {
					switchContactsTab(true);
				}
			}
		});
		tv_my_group_tab.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 显示我的群组list
				if (isFriendSelected) {
					switchContactsTab(false);
				}
			}
		});
	}

	// public void sendMsgflushContacts() {
	// Message m = new Message();
	// m.what = FLUSH_CONTANTS;
	// mHandler.sendMessage(m);
	// }

	private Handler mHandler = new WrappedHandler(thisContext) {
		public void handleMessage(Message msg) {
			cancelDialog();
			switch (msg.what) {
			case DEL_PARTNER:
				isDeltx = false;
				ProgressDialog dialog = showDialogTimer(
						TuixinContactsActivity.this, 0, "删除好友中...", 50 * 1000,
						new BaseTimerTask() {
							public void run() {
								super.run();
								Message msg = new Message();
								msg.what = DEL_PARTNER_TIMEOUT;
								mHandler.sendMessage(msg);
								isDeltx = false;
							}
						});
				dialog.setOnDismissListener(new OnDismissListener() {
					public void onDismiss(DialogInterface dialog) {
						cancelTimer();
					}

				});
				dialog.show();
				mSess.getSocketHelper().sendDelPartner(
						deltx.partner_id);
				break;
			case DEL_PARTNER_SUCCEE:
				Toast.makeText(mSess.getContext(), "删除好友成功", Toast.LENGTH_LONG).show();
				break;
			case DEL_PARTNER_FAILED:
				Toast.makeText(mSess.getContext(), "删除好友失败", Toast.LENGTH_LONG).show();
				break;
			case DEL_PARTNER_TIMEOUT:
				startPromptDialog(R.string.prompt,
						R.string.request_del_partner_outtime);
				break;
			// case CHECK_VER_TIMEOUT:
			// startPromptDialog(R.string.prompt,
			// R.string.request_check_ver_outtime);
			// break;
			// case CHECK_VER_NOT_NEEDUP:
			// startPromptDialog(R.string.prompt,
			// R.string.check_new_versoin_not_need_updata);
			// break;
			case FLUSH_CONTANTS:
				if (Utils.debug)
					Log.i(TAG,
							"开始刷新联系人数据,刷新时间：" + (new Date().toLocaleString())
									+ ",毫秒：" + System.currentTimeMillis());
				myconAdapter.setData(new ArrayList<TX>(matchedTX.values()));

				if (matchedTX != null && matchedTX.size() > 1) {
					// 有联系人
					rl_no_contacts.setVisibility(View.GONE);
				} else {
					rl_no_contacts.setVisibility(View.VISIBLE);
				}

				conListView.setAdapter(myconAdapter);// 如果是adapter删除了一个元素，这时listView可能有缓存，最后一个条目会重复显示

				myconAdapter.notifyDataSetChanged();
				break;
			case FLUSH_GROUPS:
				if (Utils.debug)
					Log.i(TAG, "开始刷新我的群组");
				myGroupsAdapter.setData(new ArrayList<TxGroup>(txGroupList));
				rl_no_contacts.setVisibility(View.GONE);// 该布局一直隐藏
				conListView.setAdapter(myGroupsAdapter);
				myGroupsAdapter.notifyDataSetChanged();
				if (Utils.debug)
					Log.i("Zzl8","count : "+conListView.getChildCount());
				break;

			}
		}
	};

	// private void flushFriendsList() {
	// if (txPinyinInitialMap != null) {
	// txPinyinInitialMap.clear();
	// }
	// if (tbTxList != null) {
	// for (int i = tbTxList.size() - 1; i >= 2; i--) {
	// TX tx = tbTxList.get(i);
	// String spell = tx.nick_name_pinyin;
	// if (spell.length() > 0) {
	// txPinyinInitialMap.put(spell.substring(0, 1).toLowerCase(), i);
	// }
	// }
	// }
	//
	// matchedTX.clear();
	// for (TX tx : tbTxList) {
	// matchedTX.put(tx.partner_id, tx);
	// }
	// // sendMsgflushContacts();
	// Message m = new Message();
	// m.what = FLUSH_CONTANTS;
	// mHandler.sendMessage(m);
	// }

	public void onResume() {
		if (Utils.debug)
			Log.i(TAG, "TuixinContacts  onResume");

		// Utils.exitStep = 0;
		SearchBar.listview = conListView;// 应该是listView右侧滑动字母快速定位listView的字母条
		switchContactsTab(isFriendSelected);
		// loadFriendsList();
		super.onResume();
		MobclickAgent.onResume(this);
	}

	// 初始化我的联系人和我的群组tab显示
	private void switchContactsTab(boolean isDisplayFriends) {
		if (isDisplayFriends) {
			if (Utils.debug)
				Log.i(TAG, "显示我的好友");
			tv_my_friend_tab
					.setBackgroundResource(R.drawable.contacts_tab_left_selected);
			tv_my_friend_tab.setTextColor(Color.WHITE);
			tv_my_group_tab
					.setBackgroundResource(R.drawable.contacts_tab_right_unselected);
			tv_my_group_tab.setTextColor(getResources().getColor(
					R.color.content_color_blue));
			isFriendSelected = true;
			// 显示快速滑动条
			searchBar.setVisibility(View.VISIBLE);

			loadFriendsList();
		} else {
			if (Utils.debug)
				Log.i(TAG, "显示我的群组");
			tv_my_friend_tab
					.setBackgroundResource(R.drawable.contacts_tab_left_unselected);
			tv_my_friend_tab.setTextColor(getResources().getColor(
					R.color.content_color_blue));
			tv_my_group_tab
					.setBackgroundResource(R.drawable.contacts_tab_right_selected);
			tv_my_group_tab.setTextColor(Color.WHITE);
			isFriendSelected = false;
			// 隐藏快速滑动条
			searchBar.setVisibility(View.GONE);
			loadMyGroupList();
		}
	}

	public void onStart() {
		// 这两个广播的注册从onResume移到onStart中
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			// TODO
			// 这个是SocketChannelConnectionImpl发的广播，TuixinService收到后调MsgHelper处理这个消息，界面收到后也做处理，会不会不一致？？！！
			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			this.registerReceiver(updatereceiver, filter);
		}
		if (datareceiver == null) {
			datareceiver = new DataReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_FLUSH);
			filter.addAction(Constants.CONSTACTS_RED_SHOW);
			filter.addAction(Constants.CONSTACTS_RED_UN_SHOW);
			this.registerReceiver(datareceiver, filter);
		}

		super.onStart();
	}

	public void onRestart() {
		super.onRestart();
		if (Utils.debug)
			Log.i(TAG, "TuixinContacts onRestart");

	}

	public void onPause() {
		if (Utils.debug)
			Log.i(TAG, "TuixinContacts onPause");
		// PopMenuDimss();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	public void onStop() {
		if (Utils.debug)
			Log.i(TAG, "TuixinContacts onStop");
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}
		if (datareceiver != null) {
			this.unregisterReceiver(datareceiver);
			datareceiver = null;
		}

		super.onStop();
	}

	public void onDestroy() {
		TxData.popActivityRemove(this);
		// stopAsyncload();
		super.onDestroy();
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.i(TAG, keyCode + "+++++++++" + event);
		//
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			View view = conListView.getFocusedChild();
			if (view != null)
				conListView.getPositionForView(view);

			return true;
		}

		else if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isqut == 0) {
				isqut = 1;
				exitToast = new Toast(getApplicationContext());
				LayoutInflater mInflater = LayoutInflater
						.from(getApplicationContext());
				View toastView = mInflater.inflate(R.layout.message_exit_toast,
						null);
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
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			onSearchRequested();
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}

	}

	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater mInflater = getMenuInflater();

		mInflater.inflate(R.menu.tuixin_contacts_menu2_nogroup, menu);

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
			GroupUtils.showDialog(TuixinContactsActivity.this,
					R.string.logout_prompt, R.string.dialog_okbtn,
					R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.exitProcessLogic(TuixinContactsActivity.this);
						}
					});

			break;
		}

		return super.onOptionsItemSelected(item);

	}

	public static final class ConViewHolder {

		// public ImageView con_invite;
		// public TextView con_new_contact;
		public TextView conName;
		// public TextView conpone;
		public ImageView con_photo;
		public TextView sign_text;
		public TextView tv_unread_count;// 未读消息总数
		public TextView tv_group_members_num;// 群成员总数
		public RelativeLayout rl_conatct_item_content;
		public LinearLayout ll_contacts_titile;// 星标好友、好友的标题分割条目
		public long partner_id;
		public TextView tv_level;
	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = (ImageView) conListView.findViewWithTag(id);
			if (iv != null && result != null) {
				// iv.setBackgroundDrawable(new
				// BitmapDrawable(Utils.getRoundedCornerBitmap(result)));
				iv.setImageBitmap(result);

			}
		}
	};

	LinkedList<ConViewHolder> mViewLines = new LinkedList<ConViewHolder>();
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {

			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				long id = (Long) result[1];
				for(ConViewHolder hldr:mViewLines){
					if(hldr.partner_id==id){
						hldr.con_photo.setImageBitmap((Bitmap)result[0]);
						break;
					}
				}
				break;

			}
			super.handleMessage(msg);
		}
	};

	private class MyTuixinConAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<TX> txs;

		private void setData(ArrayList<TX> match) {
			this.txs = new ArrayList<TX>(match);
			if (Utils.debug)
				Log.i(TAG,
						"contacts adapter setData() 神聊好友txs----:"
								+ this.txs.size());
		}

		public MyTuixinConAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			if (Utils.debug)
				Log.i(TAG, "构造联系人adapter con.size:"
						+ (txs != null ? txs.size() : 0));
			// txs=new ArrayList<TX>();

		}

		public int getCount() {
			if (txs != null) {
				// noContacts.setVisibility(View.GONE);
				return txs.size();
			}
			// noContacts.setVisibility(View.VISIBLE);
			return 0;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}
		public View getView(int position, View convertView, ViewGroup parent) {
			ConViewHolder holder = null;

			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.contacts_list_item,
						null);
				holder = new ConViewHolder();
				holder.rl_conatct_item_content = (RelativeLayout) convertView
						.findViewById(R.id.rl_conatct_item_content);
				holder.con_photo = (ImageView) convertView
						.findViewById(R.id.contact_photo);
				holder.conName = (TextView) convertView
						.findViewById(R.id.contact_name);
				holder.ll_contacts_titile = (LinearLayout) convertView
						.findViewById(R.id.ll_contacts_titile);
				holder.tv_unread_count = (TextView) convertView
						.findViewById(R.id.tv_unread_count);// 未读消息总数
				holder.sign_text = (TextView) convertView
						.findViewById(R.id.tx_list_item_sign_str);
				holder.tv_level = (TextView) convertView.findViewById(R.id.tv_level);
				if(Utils.debug)Log.i(TAG, "在TuixinContactActivity中的this="+this.toString());
				holder.tv_unread_count.setVisibility(View.GONE);
				holder.sign_text.setVisibility(View.GONE);
				convertView.setTag(holder);
				mViewLines.add(holder);
			} else {
				holder = (ConViewHolder) convertView.getTag();
			}

			int id = position;
			TX tx = null;
			if (txs != null) {
				tx = txs.get(id);
				holder.partner_id=tx.partner_id;
				if (id == 0) {
					// 好友管家条目
					holder.ll_contacts_titile.setVisibility(View.GONE);
					holder.tv_level.setVisibility(View.GONE);
					
					holder.conName.setText(tx.getNick_name());

					holder.sign_text.setVisibility(View.VISIBLE);
					MsgStat mst = MsgStat
							.getMsgStatByPartnerId(TX.TUIXIN_FRIEND);
					if (mst != null) {
						// 有好友管家的信息
						holder.sign_text.setText(mst.getMsgBody());
						if (mst.getNoRead() > 0) {
							// 有未读消息
							holder.tv_unread_count.setVisibility(View.VISIBLE);
							holder.tv_unread_count
									.setText(mst.getNoRead() + "");
						} else {
							holder.tv_unread_count.setVisibility(View.GONE);
						}
					} else {
						holder.sign_text.setText(sParser.addSmileySpans(
								tx.sign.trim().toString(), true, 0));
						holder.tv_unread_count.setVisibility(View.GONE);
					}
					holder.con_photo.setTag(tx.partner_id);
					holder.con_photo.setImageResource(R.drawable.friend_manage);
					holder.rl_conatct_item_content.setOnLongClickListener(null);// 把长按事件取消掉
					holder.rl_conatct_item_content
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// 进入好友管家
									Intent i = new Intent(thisContext,
											FriendManagerActivity.class);
									startActivity(i);
								}
							});

				} else {

					holder.tv_unread_count.setVisibility(View.GONE);// 隐藏未读消息数布局。防止重用。
					if(tx.isDispalyLevel()){
						holder.tv_level.setVisibility(View.VISIBLE);
						holder.tv_level.setText(getString(R.string.level)+tx.getLevel());
					}else {
						holder.tv_level.setVisibility(View.INVISIBLE);
					}

					if (id == 1) {
						holder.ll_contacts_titile.setVisibility(View.VISIBLE);
						TextView tv_titleLine = (TextView) holder.ll_contacts_titile
								.findViewById(R.id.tv_title_layout);
						ImageView iv_star_friend_tag = (ImageView) holder.ll_contacts_titile
								.findViewById(R.id.iv_star_friend_tag);
						// if (tx.getStarFriend() == TxDB.TX_STAR_FRIEND) {
						if (TX.tm.getStarFriendAttr(tx.partner_id) == TxInfor.TX_STAR_FRIEND) {
							// 第一个是星标好友
							iv_star_friend_tag.setVisibility(View.VISIBLE);
							tv_titleLine.setText("星标好友");
						} else {
							// 第一个不是星标好友
							iv_star_friend_tag.setVisibility(View.GONE);
							tv_titleLine.setText("A-Z");
						}
					} else if (id > 1) {
						// 从第二个条目开始判定
						if (TX.tm.getStarFriendAttr(txs.get(id).partner_id) != TxInfor.TX_STAR_FRIEND
								&& TX.tm.getStarFriendAttr(txs.get(id - 1).partner_id) == TxInfor.TX_STAR_FRIEND) {
							holder.ll_contacts_titile
									.setVisibility(View.VISIBLE);
							TextView tv_titleLine = (TextView) holder.ll_contacts_titile
									.findViewById(R.id.tv_title_layout);
							ImageView iv_star_friend_tag = (ImageView) holder.ll_contacts_titile
									.findViewById(R.id.iv_star_friend_tag);
							iv_star_friend_tag.setVisibility(View.GONE);
							tv_titleLine.setText("A-Z");
						} else {
							// 其他情况隐藏标题条目
							holder.ll_contacts_titile.setVisibility(View.GONE);
						}
					}

					if (tx != null)
						defaultHeaderImg = tx.getSex() == TX.MALE_SEX ? defaultHeaderImgMan
								: defaultHeaderImgFemale;

					if (!Utils.isNull(tx.sign)) {
						holder.sign_text.setVisibility(View.VISIBLE);
						holder.sign_text.setText(sParser.addSmileySpans(
								tx.sign.trim().toString(), true, 0));
					} else {
						holder.sign_text.setVisibility(View.GONE);
					}

					// 昵称显示级别原则，备注名>通讯录名称>昵称
					if (!TextUtils.isEmpty(tx.getRemarkName())) {
						// 有备注名
						holder.conName.setText(sParser.addSmileySpans(
								tx.getRemarkName(), true, 0));
					} else if (!TextUtils.isEmpty(tx.getTxInfor()
							.getContacts_person_name())) {
						// 有通讯录名字
						holder.conName.setText(tx.getTxInfor()
								.getContacts_person_name());
					} else {
						// 有昵称
						holder.conName.setText(sParser.addSmileySpans(
								tx.getNick_name(), true, 0));
					}
					if (Utils.isIdValid(tx.partner_id)) {

						ImageView imageView = holder.con_photo;
						Bitmap bm =  mSess.avatarDownload
								.getPartnerCachedBitmap((long) tx.partner_id);
						if (bm == null) {
							bm=mSess.cachePartnerDefault(tx.partner_id, tx.getSex());
							if (!Utils.isNull(tx.avatar_url)) {
								mSess.avatarDownload.downAvatar(tx.avatar_url,
										(long) tx.partner_id, imageView,
										avatarHandler);
							}
						}
						imageView.setImageBitmap(bm);
					} else {
						holder.con_photo.setImageResource(defaultHeaderImg);
					}

					final int index = position;

					OnLongClickListener onLongClick = new OnLongClickListener() {

						@Override
						public boolean onLongClick(View v) {
							if (Utils.debug)
								Log.i(TAG, "弹出长按菜单");
							final TX tx = txs.get(index);
							new AlertDialog.Builder(TuixinContactsActivity.this)
									.setTitle("")
									.setItems(
											R.array.tuixin_con_select_know,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int which) {
													switch (which) {
													case 0:
														Intent intent = new Intent(
																thisContext,
																SingleMsgRoom.class);
														intent.putExtra(
																Utils.MSGROOM_TX,
																tx.partner_id);
														long ThreadId = -1;// MmsUtils.getOrCreateThreadId(TuixinContactsActivity.this,new
																			// String[]
																			// {
																			// tx.phone
																			// });
														intent.putExtra(
																"threadId",
																Long.valueOf(
																		ThreadId)
																		.intValue());
														startActivity(intent);
														break;
													case 1:
														Intent intent1 = new Intent(
																TuixinContactsActivity.this,
																UserInformationActivity.class);
														intent1.putExtra(
																UserInformationActivity.pblicInfo,
																UserInformationActivity.TUIXIN_USER_INFO);
														intent1.putExtra(
																UserInformationActivity.UID,
																tx.partner_id);
														startActivity(intent1);
														break;
													case 2:// 删除好友
														dealPartner(tx);
														dialog.cancel();
														break;
													}
												}
											}).show();

							return true;
						}

					};
					holder.rl_conatct_item_content
							.setOnLongClickListener(onLongClick);
					holder.rl_conatct_item_content
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									Intent intent = new Intent(
											TuixinContactsActivity.this,
											UserInformationActivity.class);
									intent.putExtra(
											UserInformationActivity.pblicInfo,
											UserInformationActivity.TUIXIN_USER_INFO);
									intent.putExtra(
											UserInformationActivity.UID,
											txs.get(index).partner_id);
									startActivity(intent);
								}
							});
				}

			}

			return convertView;
		}

	}

	// 我的群组的adapter
	private class MyGroupsAdapter extends BaseAdapter {
		private ArrayList<TxGroup> txGroups;

		private void setData(ArrayList<TxGroup> match) {
			this.txGroups = new ArrayList<TxGroup>(match);
			if (Utils.debug)
				Log.i(TAG, "我的群组txGroups----:" + this.txGroups.size());
		}

		public int getCount() {
			if (txGroups != null) {
				return txGroups.size() + 1;
			}
			return 1;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			ConViewHolder holder = null;

			if (convertView == null) {
				convertView = View.inflate(mSess.getContext(), R.layout.contacts_list_item,
						null);
				holder = new ConViewHolder();
				holder.rl_conatct_item_content = (RelativeLayout) convertView
						.findViewById(R.id.rl_conatct_item_content);
				holder.con_photo = (ImageView) convertView
						.findViewById(R.id.contact_photo);
				holder.conName = (TextView) convertView
						.findViewById(R.id.contact_name);
				holder.ll_contacts_titile = (LinearLayout) convertView
						.findViewById(R.id.ll_contacts_titile);
				holder.sign_text = (TextView) convertView
						.findViewById(R.id.tx_list_item_sign_str);
				holder.sign_text.setVisibility(View.GONE);
				holder.tv_group_members_num = (TextView) convertView
						.findViewById(R.id.tv_group_members_num);
				holder.tv_group_members_num.setVisibility(View.VISIBLE);
				holder.tv_unread_count = (TextView) convertView
						.findViewById(R.id.tv_unread_count);// 未读消息总数
				holder.tv_unread_count.setVisibility(View.GONE);

				convertView.setTag(holder);
			} else {
				holder = (ConViewHolder) convertView.getTag();
			}

			// int id = (position - 1) < 0 ? 0 : (position - 1);
			// if (txGroups != null) {
			// final TxGroup txgroup = txGroups.get(id);
			if (position == 0) {
				// 群组动态
				holder.ll_contacts_titile.setVisibility(View.GONE);
				TX tx = TX.tm.getSlGroupNotice();
				holder.conName.setText(tx.getNick_name());

				holder.sign_text.setVisibility(View.VISIBLE);
				MsgStat mst = MsgStat.getMsgStatByPartnerId(TX.SL_GROUP_NOTICE);
				if (mst != null) {
					// 有群组动态的信息
					if (mst.getMsgBody().contains("�slgroup�")) {
						// 群字符串还没有被替换掉
						switch (mst.getMsgType()) {
						// 群组动态
						case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN:
							TxGroup txGroupAdmin = TxGroup.getTxGroup(
									mSess.getContentResolver(),
									(int) mst.group_id_notice);
							if (txGroupAdmin != null) {
								String content = Utils
										.isNull(txGroupAdmin.group_title) ? ""
										+ txGroupAdmin.group_id
										: txGroupAdmin.group_title;
								mst.msg_body = mst.msg_body.replace(
										"�slgroup�", content);
							} else {
								mst.msg_body = mst.msg_body.replace(
										"�slgroup�", "" + mst.group_id_notice);
							}
							break;
						case TxDB.MSG_TYPE_SET_GROUP_ADMIN:
						case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER:
						case TxDB.MSG_TYPE_DISMISS_GROUP:
						case TxDB.MSG_TYPE_LEAVE:
						case TxDB.MSG_TYPE_IN:
							TxGroup txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
									(int) mst.group_id_notice);
							if (txGroup != null) {
								String content = Utils
										.isNull(txGroup.group_title) ? ""
										+ txGroup.group_id
										: txGroup.group_title;
								mst.msg_body = mst.msg_body.replace(
										"�slgroup�", content);
							} else {
								mst.msg_body = mst.msg_body.replace(
										"�slgroup�", "" + mst.group_id_notice);
							}
							break;
						}
					} else {
						holder.sign_text.setText(mst.getMsgBody());
					}
					if (mst.getNoRead() > 0) {
						// 有未读消息
						holder.tv_unread_count.setVisibility(View.VISIBLE);
						holder.tv_unread_count.setText(mst.getNoRead() + "");
					} else {
						holder.tv_unread_count.setVisibility(View.GONE);
					}
				} else {
					holder.sign_text.setText(sParser.addSmileySpans(
							tx.sign.trim().toString(), true, 0));
					holder.tv_unread_count.setVisibility(View.GONE);
				}

				holder.tv_group_members_num.setVisibility(View.GONE);
				holder.con_photo.setTag(tx.partner_id);
				holder.con_photo.setImageResource(R.drawable.slgroupnotice);
				holder.rl_conatct_item_content
						.setOnClickListener(new View.OnClickListener() {

							@Override
							public void onClick(View v) {
								// 进入群组动态
								Intent i = new Intent(thisContext,
										GroupNewsActivity.class);
								startActivity(i);
							}
						});

			} else {

				holder.tv_unread_count.setVisibility(View.GONE);// 隐藏未读消息数布局。防止重用。

				int id = (position - 1) < 0 ? 0 : (position - 1);
				if (txGroups != null && txGroups.size() > 0) {
					final TxGroup txgroup = txGroups.get(id);
					if (id == 0) {
						holder.ll_contacts_titile.setVisibility(View.VISIBLE);
						TextView tv_titleLine = (TextView) holder.ll_contacts_titile
								.findViewById(R.id.tv_title_layout);
						ImageView iv_star_friend_tag = (ImageView) holder.ll_contacts_titile
								.findViewById(R.id.iv_star_friend_tag);
						if (txgroup.group_tx_state == TxDB.QU_TX_STATE_OWN) {
							// 第一个是我创建的群
							iv_star_friend_tag.setVisibility(View.GONE);
							tv_titleLine.setText("我创建的群");
						} else {
							// 第一个不是星标好友
							iv_star_friend_tag.setVisibility(View.GONE);
							tv_titleLine.setText("我加入的群");
						}
					} else if (id > 0) {
						// 从第二个条目开始判定
						if (txGroups.get(id).group_tx_state != TxDB.QU_TX_STATE_OWN
								&& txGroups.get(id - 1).group_tx_state == TxDB.QU_TX_STATE_OWN) {
							holder.ll_contacts_titile
									.setVisibility(View.VISIBLE);
							TextView tv_titleLine = (TextView) holder.ll_contacts_titile
									.findViewById(R.id.tv_title_layout);
							ImageView iv_star_friend_tag = (ImageView) holder.ll_contacts_titile
									.findViewById(R.id.iv_star_friend_tag);
							iv_star_friend_tag.setVisibility(View.GONE);
							tv_titleLine.setText("我加入的群");
						} else {
							// 其他情况隐藏标题条目
							holder.ll_contacts_titile.setVisibility(View.GONE);
						}
					}

					if (txgroup != null)
						defaultHeaderImg = R.drawable.qun_default;

					if (!Utils.isNull(txgroup.group_sign)) {
						holder.sign_text.setVisibility(View.VISIBLE);
						holder.sign_text.setText(sParser.addSmileySpans(
								txgroup.group_sign.trim().toString(), true, 0));
					} else {
						holder.sign_text.setVisibility(View.GONE);
					}
					holder.tv_group_members_num.setVisibility(View.VISIBLE);
					holder.tv_group_members_num.setText(""
							+ txgroup.group_all_num);// 群成员总数

					// 昵称显示级别原则，备注名>通讯录名称>昵称
					// 有群名称
					holder.conName.setText(sParser.addSmileySpans(
							txgroup.group_title, true, 0));
					holder.con_photo.setTag(-txgroup.group_id);
					if (Utils.isIdValid(txgroup.group_id)) {
						Bitmap bm = getGroupCachedBitmap(txgroup.group_id);
						if (bm != null) {
							holder.con_photo.setImageBitmap(bm);
						} else {
							holder.con_photo.setImageResource(defaultHeaderImg);

							if (!Utils.isNull(txgroup.group_avatar)) {
								loadGroupImg(txgroup.group_avatar,
										txgroup.group_id,
										new AsyncCallback<Bitmap>() {

											@Override
											public void onSuccess(
													Bitmap result, long id) {
												ImageView iv = (ImageView) conListView
														.findViewWithTag(-id);
												if (iv != null
														&& result != null) {
													iv.setImageBitmap(result);
												}
											}

											@Override
											public void onFailure(Throwable t,
													long id) {
											}
										});
							}
						}

					} else {
						holder.con_photo.setImageResource(defaultHeaderImg);
					}

					holder.rl_conatct_item_content
							.setOnClickListener(new OnClickListener() {

								@Override
								public void onClick(View v) {
									if (Utils.debug) {
										showToast("点击进入聊天室");
									}
									Intent intent = new Intent(thisContext,
											GroupMsgRoom.class);
									intent.putExtra(Utils.MSGROOM_TX_GROUP,
											txgroup);
									thisContext.startActivity(intent);
								}
							});
				}

			}

			return convertView;
		}

	}

	private TX deltx;// 待删除的好友？？？
	private boolean isDeltx;
	private int cot;

	private void dealPartner(TX tx) {
		int nettype = Utils.getNetworkType(this);
		if (nettype == Utils.NET_NOT_AVAILABLE) {
			startPromptDialog(R.string.prompt, R.string.msg_nonetstr);
			return;
		}

		deltx = tx;
		if (!isDeltx) {
			cot++;
			if (Utils.debug)
				Log.i(TAG, "dealPartner" + cot);
			isDeltx = true;
			String content = this.getString(R.string.del_partner_prompt)
					+ deltx.getNick_name();
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(
					TuixinContactsActivity.this);
			promptDialog.setTitle(R.string.prompt);
			promptDialog.setMessage(content);
			promptDialog.setPositiveButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int i) {
							dialog.cancel();
							dialog.dismiss();
							if (Utils.debug)
								Log.i(TAG, "ok dealPartner" + cot);
							Message m = new Message();
							m.what = DEL_PARTNER;
							TuixinContactsActivity.this.mHandler.sendMessage(m);

						}
					}).setNegativeButton(R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
							isDeltx = false;
						}
					});// 显示对话框
			promptDialog.show();
		}
	}

	// 加载好友
	private void loadFriendsList() {
		if (!isFriendSelected) {
			// 如果不是在显示联系人的状态，则不刷新
			return;
		}
		if (Utils.debug)
			Log.i(TAG, "loadFriendsList,加载并刷新好友list");
		tbTxList = TX.tm.getTBTXList();
		if (Utils.debug)
			Log.i(TAG, "好友list总数：" + tbTxList.size());
		// flushFriendsList();

		if (txPinyinInitialMap != null) {
			txPinyinInitialMap.clear();
		}
		if (tbTxList != null) {
			for (int i = tbTxList.size() - 1; i >= 2; i--) {
				TX tx = tbTxList.get(i);
				String spell = tx.nick_name_pinyin;
				if (spell.length() > 0) {
					txPinyinInitialMap.put(spell.substring(0, 1).toLowerCase(),
							i);
				}
			}
		}

		matchedTX.clear();
		matchedTX.put(TX.TUIXIN_FRIEND, TX.tm.getTxFriend());
		for (TX tx : tbTxList) {
			matchedTX.put(tx.partner_id, tx);
		}
		// sendMsgflushContacts();
		Message m = new Message();
		m.what = FLUSH_CONTANTS;
		mHandler.sendMessage(m);
	}
	

	// 加载我的群组
	private void loadMyGroupList() {
		if (Utils.debug)
			Log.i(TAG, "loadMyGroupList,加载并刷新群list");
		new AsyncTask<Void, Void, List<TxGroup>>() {

			@Override
			protected List<TxGroup> doInBackground(Void... params) {
				txGroupList = TxGroup.getMyGroups(mSess.getContext());
				if (txGroupList != null) {
					// matchedTxGroup.clear();
					// for (TxGroup txgroup : txGroupList) {
					// matchedTxGroup.put(txgroup.group_id, txgroup);
					// }
					Message m = new Message();
					m.what = FLUSH_GROUPS;
					mHandler.sendMessage(m);
				}

				return null;
			}

		}.execute();
	}

	// 没有注册，注掉 2014.03.04
	// public class MsgReceiver extends BroadcastReceiver {
	// public void onReceive(Context context, Intent intent) {
	// final String msg =
	// null;//intent.getStringExtra(AutoSyncContactsAndSms.SEND_KEY);
	// if (!Utils.isNull(msg)) {
	// if (msg.equals("complete")) {
	// loadFriendsList();
	// }
	// }
	// }
	// }

	// 没有人开启这个popupWindow 2014.03.04 shc
	// public void PopMenuDimss() {
	// if (conpage_popupWindow != null && (conpage_popupWindow.isShowing())) {
	// // isPopshow = false;
	// conpage_popupWindow.dismiss();
	// }
	// }

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			// 好友加载应该不会有问题了。暂时不弹吐司测试。2013.10.09 shc
			// if (Utils.debug) {
			// Toast.makeText(getApplicationContext(),
			// "收到了UpdateReceiver广播，赶紧去看看好友列表出问题了没", 1).show();
			// }
			final String msg = intent.getStringExtra("msg");
			if (msg.trim().length() != 0) {
				Utils.executorService.submit(new Runnable() {
					@Override
					public void run() {
						if (Utils.debug)
							Log.w(TAG, "开启子线程处理广播收到的消息msg:" + msg);
						dealReceivedMsg(msg);
					}
				});
			}
		}
	}

	public class DataReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Constants.CONSTACTS_RED_SHOW)||intent.getAction().equals(Constants.CONSTACTS_RED_UN_SHOW)) {
				if (isFriendSelected) {
					loadFriendsList();
				}else {
					loadMyGroupList();
				}
			}
			final String msg = intent.getStringExtra("msg");
			if (msg != null) {
				if (msg.equals(TxData.FLUSH_TXS)) {
					loadFriendsList();
				}

			}
		}
	}

	/** 处理广播中收到的消息 */
	public void dealReceivedMsg(String msg) {
		if (msg.startsWith("{")) {
			JSONObject jo = null;
			try {
				jo = new JSONObject(msg);
			} catch (JSONException e) {
				if (Utils.debug)
					e.printStackTrace();
			}
			if (jo != null) {
				int type = 0;
				try {
					type = jo.getInt("mt");
				} catch (JSONException e) {

					if (Utils.debug)
						e.printStackTrace();
				}
				switch (type) {
				case MsgInfor.SERVER_SYSTEM:
					if (Utils.debug)
						Log.i(TAG, "收到系统推送消息");
					dealSystemMsg(jo);
					break;
				case MsgInfor.SERVER_DEL_PARTNER:
					cancelDialogTimer();
					try {
						int d = jo.getInt("d");
						String id = jo.getString("uid");
						if (Utils.debug) {
							Log.i(TAG, "删除好友成功后，服务器返回到 uid = " + id);
						}
						if (d == 0) {
							long partner_id = Long.parseLong(id);
							deltx = TX.tm.getTx(partner_id);
							// TX.tm.removeTxToBlack(txdata,
							// partner_id);//不应该把好友拉进黑名单，应该是把好友变为陌生人 2014.01.21
							// shc
							TX.tm.changeTxToST(partner_id);// 应该是把好友变为陌生人
							tbTxList.remove(deltx);
							loadFriendsList();

							MsgStat.delMsgStatByPartnerId(mSess.getContentResolver(),
									Long.parseLong(id));

							if (TXMessage.findTXMessageByTcardId(cr, id) != null) {
								// 根据TCARD_ID删除与该好友所有的消息？？？
								cr.delete(TxDB.Messages.CONTENT_URI,
										TxDB.Messages.TCARD_ID + "=? ",
										new String[] { id });
							}

							Message msg1 = new Message();
							msg1.what = DEL_PARTNER_SUCCEE;
							mHandler.sendMessage(msg1);
						} else {
							Message msg1 = new Message();
							msg1.what = DEL_PARTNER_FAILED;
							mHandler.sendMessage(msg1);
						}
					} catch (JSONException e1) {
						if (Utils.debug)
							Log.e(TAG, "处理删除好友系统返回的json异常", e1);
					}
					break;
				case MsgInfor.SERVER_SYSTEM_MSG:
					try {
						JSONArray ja = jo.getJSONArray("ms");
						dealOfflineSystemMsgs(ja);
					} catch (JSONException e) {
						if (Utils.debug)
							e.printStackTrace();
					}
					break;
				}
			}
		} else {
			// Toast.makeText(context, "信息不合法" + msg, 1).show();
		}
	}

	/** 收到系统推送消息 34号协议 */
	private void dealSystemMsg(JSONObject jo) {
		try {
			int st = jo.getInt("s");
			if (st == 0) {
				JSONObject jooo = jo.getJSONObject("obj");
				JSONObject joo = jooo.getJSONObject("um");
				// boolean needsound=false;
				if (joo != null) {
					String id = joo.optString("id", "");
					// String sn = joo.optString("sn", "");
					String un = joo.optString("un", "");
					String ph = joo.optString("ph", "");
					String em = joo.optString("em", "");
					boolean obd = joo.getBoolean("obd");
					boolean ebd = joo.getBoolean("ebd");
					// if(id!=null&&!id.equals("")){
					// needsound=true;
					// }

					long partner_id = Long.parseLong(id);

					ContentValues values = new ContentValues();
					values.put(TxDB.Tx.DISPLAY_NAME, un);
					values.put(TxDB.Tx.PHONE, ph);
					values.put(TxDB.Tx.EMAIL, em);
					values.put(TxDB.Tx.IS_E_BIND, ebd);
					values.put(TxDB.Tx.IS_P_BIND, obd);

					if (TX.tm.updateTx(partner_id, values) == null) {
						// 更新失败，则执行添加tx
						TX tx = new TX();
						tx.partner_id = Long.parseLong(id);
						tx.setNick_name(un);
						// tx.nick_name_pinyin =
						// CnToSpell.getFullSpell(tx.getNick_name());
						tx.setEmail(em);
						tx.setPhone(ph);
						tx.setEmailBind(ebd);
						tx.setPhoneBind(obd);
						// tx.setTx_type(TxDB.TX_TYPE_ST);
						// tx.setStarFriend(TxDB.TX_NOT_FRIEND);
						// tx.setIn_black_list(TxDB.TX_NOT_IN_BLACK_LIST);

						TX.tm.addTx(tx);
					}
				}
			} else if (st == 2) {
			} else if (st == 3) {
			} else if (st == 4) {
			} else if (st == 5) {
			} else if (st == 6) {
			} else if (st == 1) {
			}
		} catch (JSONException e1) {
			if (Utils.debug)
				Log.e(TAG, "处理系统推送消息json异常", e1);
		}
	}

	/** 处理Server返回离线系统消息 */
	private void dealOfflineSystemMsgs(JSONArray ja) {
		JSONObject jo = null;
		int len = ja.length();
		for (int i = 0; i < len; i++) {
			try {
				jo = ja.getJSONObject(i);
				dealSystemMsg(jo);
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "处理Server返回离线系统消息异常", e);
			}
		}

	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				TuixinContactsActivity.this);
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

}