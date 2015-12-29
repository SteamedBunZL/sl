package com.tuixin11sms.tx.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.CnToSpell;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.download.AutoUpdater;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 邀请手机好友使用神聊
 * 
 * @author SHC
 */
public class InviteContactsActivity extends BaseActivity {
	private static final String TAG = InviteContactsActivity.class
			.getSimpleName();
	private final int FLUSH_CONTANTS = 101;
	private final int FLUSH_NEWCONTANTSICON = 102;
	private final int CHECK_VER_TIMEOUT = 103;
	private final int CHECK_VER = 104;
	private final int CHECK_VER_NOT_NEEDUP = 105;
	private final int FLUSH_NEWTABNEW = 106;
	private final int DEL_PARTNER = 107;

	// public TxData txdata;
	// private SharedPreferences prefs = null;
	// private Editor editor;
	public ContentResolver cr;
	private ProgressDialog progress;
	private String appurl, applog;
	private int appver;

	protected static final int GUI_MSGS_UPDATA = 0x108;
	protected static final int GUI_TXS_UPDATA = 0x100;

	// 测试数据
	public ArrayList<ContactVo> inviteContacts;// 全部联系人
	public ArrayList<ContactVo> match;// 匹配的联系人 --根据搜索框的值 默认为contacts或tbs或news
	private ListView conlistview;
	private EditText searchInputBox = null;
	public TextView promptText = null;
	// public ImageView news_icon;
	public TextView numPromptText;
	public TextView statePromptText;
	public CheckBox seceltChechbox;
	public Button msg_selcet_btn;
	public Button msg_cancel_btn;
	public Button more_contacts_btn;
	public Button seachClean_btn;
	public ContactAPI api;
	public RelativeLayout invite_title_view;
	// View sms_contacts;
	public View bottomSearchView;
	public View topSearchView;
	public MyConAdapter myconAdapter;
	public Map<String, Integer> posContacts;
	boolean isPopshow;
	boolean isNewsItemContacts;
	boolean launch_tx;
	private boolean isselectall;
	private int selectItemCount = 0;;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		if (Utils.debug)
			Log.i(TAG, "contacts OnCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invite_contacts);
		// txdata = (TxData) getApplication();
		// TuixinService1.OnApp = true;
		// prefs = getSharedPreferences(CommonData.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		//
		// editor = prefs.edit();
		api = ContactAPI.getAPI();
		api.setCr(getContentResolver());
		cr = this.getContentResolver();

		conlistview = (ListView) findViewById(R.id.list_contacts);

		match = new ArrayList<ContactVo>();
		posContacts = new HashMap<String, Integer>();

		// news_icon = (ImageView) findViewById(R.id.new_tuixin_contacts);
		invite_title_view = (RelativeLayout) findViewById(R.id.invite_tab_title);
		LayoutInflater mInflater = LayoutInflater.from(this);
		topSearchView = mInflater.inflate(R.layout.invite_contacts_list_header,
				null);
		bottomSearchView = mInflater.inflate(
				R.layout.invite_contacts_list_footer, null);
		// sms_contacts = findViewById(R.id.tuixin_tab_contacts);
		searchInputBox = (EditText) topSearchView
				.findViewById(R.id.con_search_input_box);
		numPromptText = (TextView) topSearchView.findViewById(R.id.numprompt);
		statePromptText = (TextView) topSearchView
				.findViewById(R.id.stateprompt);
		seceltChechbox = (CheckBox) topSearchView
				.findViewById(R.id.all_select_invite_contacts_checkbox);
		seachClean_btn = (Button) topSearchView.findViewById(R.id.seach_clean);
		seachClean_btn.setVisibility(View.GONE);
		more_contacts_btn = (Button) bottomSearchView
				.findViewById(R.id.more_contacts);
		msg_selcet_btn = (Button) findViewById(R.id.invite_mSelected);
		msg_cancel_btn = (Button) findViewById(R.id.invite_mCancel);
		promptText = (TextView) findViewById(R.id.prompt_text);

		searchInputBox.addTextChangedListener(watcher);

		myconAdapter = new MyConAdapter(InviteContactsActivity.this);
		conlistview.addHeaderView(topSearchView);
		conlistview.addFooterView(bottomSearchView);
		conlistview.setAdapter(myconAdapter);

		if (progress == null) {
			progress = new ProgressDialog(InviteContactsActivity.this);
		}
		selectInviteItem();

		seceltChechbox.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isselectall = isselectall == true ? false : true;
				if (isselectall) {
					statePromptText.setText("取消全选");
					if (inviteContacts != null) {
						for (ContactVo tx : inviteContacts) {
							// if(!tx.deltx){
							tx.setDel(true);
							// }
						}
						selectItemCount = inviteContacts.size();
						seceltChechbox.setChecked(isselectall);
						contactsflush();
					}
				} else {
					statePromptText.setText("全选");
					if (inviteContacts != null) {
						for (ContactVo tx : inviteContacts) {
							// if(!tx.deltx){
							tx.setDel(false);
							// }
						}
						selectItemCount = 0;
						seceltChechbox.setChecked(isselectall);
						contactsflush();
					}
				}
				InviteContactsActivity.this.numPromptText
						.setText(InviteContactsActivity.this.getResources()
								.getString(R.string.select_invite_contacts)
								+ "(" + selectItemCount + ")");
			}

		});

		more_contacts_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// 经检验，该方法有效 2014.01.21 shc
				HashMap<Long, String> csCache = (HashMap<Long, String>) TX.tm
						.getContactsCache().clone();
				ArrayList<ContactVo> allContacts = new ArrayList<ContactVo>();
				// 筛掉有电话号码的好友
				HashMap<Long, TX> tbtxCache = TX.tm.getTBTXCache();
				ArrayList<TX> tbtxList = new ArrayList<TX>(tbtxCache.values());
				for (int i = 0, size = tbtxList.size(); i < size; i++) {
					String phone = tbtxList.get(i).getPhone();
					if (!TextUtils.isEmpty(phone)) {
						// 电话号码不为空
						csCache.remove(Long.parseLong(phone));
					}
				}
				// 有电话号码的陌生人就不管了，因为原来的TX.getLocalList();也只是好友的以电话为key的list

				Set<Entry<Long, String>> csSet = csCache.entrySet();
				Iterator<Entry<Long, String>> iter = csSet.iterator();
				Entry<Long, String> entry = null;
				while (iter.hasNext()) {
					entry = iter.next();
					ContactVo ttx = new ContactVo();
					ttx.setPhone(entry.getKey().toString());
					ttx.setContact_name(entry.getValue());
					allContacts.add(ttx);
				}

				for (int i = 0; i < inviteContacts.size(); i++) {
					for (int j = 0; j < allContacts.size(); j++) {
						if (inviteContacts.get(i).getPhone()
								.equals(allContacts.get(j).getPhone())
								&& inviteContacts
										.get(i)
										.getContact_name()
										.equals(allContacts.get(j)
												.getContact_name()))
							allContacts.remove(j);
					}
				}

				inviteContacts.addAll(allContacts);
				initPos(inviteContacts, null);
				matchData();
				contactsflush();
				more_contacts_btn.setVisibility(View.GONE);
			}
		});
		seachClean_btn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//
				searchInputBox.setText("");
				searchInputBox.setHint(R.string.con_hint_search_bar);
				seachClean_btn.setVisibility(View.GONE);
			}
		});
		invite_title_view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				//
				conlistview.setSelectionAfterHeaderView();
				// conlistview.setSelection(0);
				contactsflush();
			}
		});

	}

	public void contactsflush() {
		Message m = new Message();
		m.what = FLUSH_CONTANTS;
		InviteContactsActivity.this.mHandler.sendMessage(m);
	}

	public void initTX() {

		// 经检验，此方法有效 2014.01.21 shc
		HashMap<Long, String> csCache = (HashMap<Long, String>) TX.tm
				.getContactsCache().clone();
		ArrayList<ContactVo> newtab = new ArrayList<ContactVo>();
		// 筛掉有电话号码的好友
		HashMap<Long, TX> tbtxCache = TX.tm.getTBTXCache();
		ArrayList<TX> tbtxList = new ArrayList<TX>(tbtxCache.values());
		for (int i = 0, size = tbtxList.size(); i < size; i++) {
			String phone = tbtxList.get(i).getPhone();
			if (!TextUtils.isEmpty(phone)) {
				// 电话号码不为空
				csCache.remove(Long.parseLong(phone));
			}
		}

		// 有电话号码的陌生人就不管了，因为原来的TX.getLocalList();也只是好友的以电话为key的list
		Set<Entry<Long, String>> csSet = csCache.entrySet();
		Iterator<Entry<Long, String>> iter = csSet.iterator();
		Entry<Long, String> entry = null;
		while (iter.hasNext()) {
			entry = iter.next();
			ContactVo contact = new ContactVo();
			contact.setPhone(entry.getKey().toString());
			contact.setContact_name(entry.getValue());
			newtab.add(contact);
		}

		inviteContacts.clear();
		inviteContacts.addAll(newtab);
		initPos(inviteContacts, null);
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (progress != null)
				progress.cancel();
			switch (msg.what) {
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
					new AlertDialog.Builder(InviteContactsActivity.this)
							.setTitle(R.string.up_prompt)
							.setCancelable(false)
							.setMessage(applog)
							.setPositiveButton(R.string.confirm,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {
											AutoUpdater
													.CheckForUpdate(
															InviteContactsActivity.this,
															appurl, applog,
															appver);
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
			case InviteContactsActivity.GUI_TXS_UPDATA:

				break;
			case InviteContactsActivity.GUI_MSGS_UPDATA:

				break;
			case FLUSH_CONTANTS:
				if (inviteContacts == null || inviteContacts.size() == 0) {
					conlistview.setVisibility(View.GONE);
					// noContacts.setVisibility(View.VISIBLE);
				} else {
					conlistview.setVisibility(View.VISIBLE);
					// noContacts.setVisibility(View.GONE);
				}
				myconAdapter.notifyDataSetChanged();
				break;

			case FLUSH_NEWCONTANTSICON:

				break;
			case FLUSH_NEWTABNEW:

				break;
			case DEL_PARTNER:

				break;
			}
		}
	};

	private void matchData() {
		if (match == null) {
			match = new ArrayList<ContactVo>();
		}
		match.clear();
		if (inviteContacts == null) {
			inviteContacts = new ArrayList<ContactVo>();
		}
		match.addAll(inviteContacts);
		myconAdapter.setData(match);
		// myconAdapter.notifyDataSetChanged();
	}

	private void initPos(ArrayList<ContactVo> contacts, ArrayList<TX> tbs) {

		// System.out.println("排序开始===" + System.currentTimeMillis());

		if (posContacts != null) {
			posContacts.clear();
		}
		if (contacts != null) {
			for (int i = contacts.size() - 1; i >= 0; i--) {
				ContactVo tx = contacts.get(i);
				String spell = tx.getContact_pinyin_name();
				if (spell.length() > 0) {
					posContacts.put(spell.substring(0, 1).toLowerCase(), i);
				}
			}
		}

		// System.out.println("排序结束===" + System.currentTimeMillis());
	}

	// private void currentPos() {
	// searchBar = (SearchBar) findViewById(R.id.con_mSearchBar);
	// searchBar.setTv(promptText);
	// }

	// 接收数据
	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (!s.toString().equals("")) {
				seachClean_btn.setVisibility(View.VISIBLE);
			} else {
				seachClean_btn.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (Utils.debug)
				Log.i(TAG, "进入搜索功能");

			ArrayList<ContactVo> newList = new ArrayList<ContactVo>();
			if (inviteContacts == null) {
				inviteContacts = new ArrayList<ContactVo>();
			}
			newList = inviteContacts;

			match.clear();
			for (ContactVo tx : newList) {
				String name = tx.getContact_name().toLowerCase();
				String pinyin = tx.getContact_pinyin_name();
				String phone = tx.getPhone();
				if (name.startsWith(s.toString())
						|| pinyin.contains(s.toString())
						|| phone.contains(s.toString())) {
					match.add(tx);
				}
			}
			myconAdapter.setData(match);
			// myconAdapter.notifyDataSetChanged();
			contactsflush();
		}
	};

	public void Rewatcher(String s) {
		ArrayList<ContactVo> newList = new ArrayList<ContactVo>();
		if (inviteContacts == null) {
			inviteContacts = new ArrayList<ContactVo>();
		}
		newList = inviteContacts;

		match.clear();
		for (ContactVo tx : newList) {
			String name = tx.getContact_name().toLowerCase();
			String pinyin = tx.getContact_pinyin_name();
			String phone = tx.getPhone();
			if (name.startsWith(s.toString()) || pinyin.contains(s.toString())
					|| phone.contains(s.toString())) {
				match.add(tx);
			}
		}
		myconAdapter.setData(match);
		// myconAdapter.notifyDataSetChanged();
		contactsflush();
	}

	public void onResume() {
		if (Utils.debug)
			Log.i(TAG, "contacts onResume");
		// Utils.exitStep = 0;
		searchInputBox.setText("");
		searchInputBox.setHint(R.string.con_hint_search_bar);
		monimainData();
		// matchData();
		// currentPos();

		// msgStatflush();
		super.onResume();
	}

	public void onStart() {
		if (Utils.debug) {
			Log.i(TAG, "contactss   onStart");
		}

		super.onStart();
	}

	public void onRestart() {
		super.onRestart();
		if (Utils.debug)
			Log.i(TAG, "contacts onRestart");

	}

	public void onPause() {
		if (Utils.debug)
			Log.i(TAG, "contacts onPause");
		for (ContactVo tx : inviteContacts) {
			tx.setDel(false);
		}
		this.finish();
		super.onPause();
	}

	public void onStop() {
		if (Utils.debug)
			Log.i(TAG, "contacts onStop");
		super.onStop();
	}

	public void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	public boolean onMenuItemSelected(int featureId, MenuItem item) {

		return super.onMenuItemSelected(featureId, item);
	}

	public boolean onKeyDown(int keyCode, KeyEvent event) {

		return super.onKeyDown(keyCode, event);

	}

	public static final class ConViewHolder {
		public TextView conName;
		public TextView conPone;
		public CheckBox conSelect;

	}

	public class MyConAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<ContactVo> txs;

		private void setData(ArrayList<ContactVo> match) {

			this.txs = match;

		}

		public MyConAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			if (Utils.debug)
				if (txs != null)
					if (Utils.debug) {
						if (txs != null) {
							Log.i(TAG, "con.size:" + txs.size());
						} else {
							Log.i(TAG, "con.size:" + 0);
						}
					}

		}

		public int getCount() {
			if (txs != null && txs.size() > 0) {
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
				holder = new ConViewHolder();

				convertView = mInflater.inflate(
						R.layout.invite_contacts_list_item, null);

				holder.conName = (TextView) convertView
						.findViewById(R.id.contact_name);
				holder.conPone = (TextView) convertView
						.findViewById(R.id.contacts_phone);
				holder.conPone.setTextAppearance(InviteContactsActivity.this,
						R.style.IC_contacts);
				holder.conSelect = (CheckBox) convertView
						.findViewById(R.id.select_contacts_checkbox);

				convertView.setTag(holder);
			} else {
				holder = (ConViewHolder) convertView.getTag();
			}

			int id = position;

			// TX tx = null;
			if (txs != null) {

				final ContactVo tx = txs.get(id);

				if (!TextUtils.isEmpty(tx.getContact_name())) {
					holder.conName.setText(tx.getContact_name());
				}
//				else {
//					holder.conName.setText(tx.getNick_name());
//				}
				holder.conPone.setText(tx.getPhone());
				holder.conSelect.setChecked(tx.isDel());
				holder.conSelect.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (((CheckBox) v).isChecked()) {
							tx.setDel(true);
							selectItemCount++;
						} else {
							tx.setDel(false);
							selectItemCount--;
						}
						if (selectItemCount == 0) {
							isselectall = false;
							InviteContactsActivity.this.statePromptText
									.setText("全选");
						} else {
							if (selectItemCount == getCount()) {
								isselectall = true;
								InviteContactsActivity.this.statePromptText
										.setText("取消全选");
							} else {
								isselectall = false;
								// InviteContactsActivity.this.statePromptText.setText("全选");
							}
						}
						numPromptText.setText(InviteContactsActivity.this
								.getResources().getString(
										R.string.select_invite_contacts)
								+ "(" + selectItemCount + ")");
					}
				});
				convertView.setOnClickListener(new inviteConvertViewClick(
						InviteContactsActivity.this, tx, id, txs, this
								.getCount(), holder));

			}
			return convertView;
		}

	}

	public void monimainData() {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				if (Utils.debug) {
					Log.i(TAG, "monimainData 刷新");
				}
				initTX();
				initPos(inviteContacts, null);
				matchData();
				// currentPos();
				contactsflush();
				// searchBarVisible(true);
				// Looper.loop();
			}
		});
	}

	private void startPromptDialog(int titleSource, int msg) {
		AlertDialog.Builder promptDialog = new AlertDialog.Builder(
				InviteContactsActivity.this);
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

	public void selectInviteItem() {
		final String messageInvitePrompt = InviteContactsActivity.this
				.getResources().getString(R.string.invite_message);
		final StringBuffer phones = new StringBuffer();
		msg_selcet_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				if (inviteContacts != null) {
					for (ContactVo tx1 : inviteContacts) {
						if (tx1.isDel()) {
							String mobileModel = android.os.Build.MODEL;
							if (mobileModel.contains("GT-")) {
								phones.append(tx1.getPhone()).append(",");
							} else {
								phones.append(tx1.getPhone()).append(";");
							}
						}
					}
					if (!Utils.isNull(phones.toString())) {
						Uri smsToUri = Uri.parse("smsto:".concat(phones
								.toString()));
						if (Utils.debug)
							Log.i(TAG,
									"(phones.toString():" + (phones.toString()));

						Intent intent = new Intent(
								android.content.Intent.ACTION_SENDTO, smsToUri);
						intent.putExtra("sms_body", messageInvitePrompt);
						startActivity(intent);
					}
				}

			}

		});
		msg_cancel_btn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				for (ContactVo tx : inviteContacts) {
					tx.setDel(false);
				}
				InviteContactsActivity.this.finish();
			}

		});

	}

	private class inviteConvertViewClick implements OnClickListener {
		private Context context;
		private ContactVo tx;
		private int index;
		private ArrayList<ContactVo> txs;
		private int count;
		private ConViewHolder holder;

		public inviteConvertViewClick(Context context, ContactVo tx, int position,
				ArrayList<ContactVo> txs, int count, ConViewHolder holder) {
			super();
			this.context = context;
			this.tx = tx;
			this.index = position;
			this.txs = txs;
			this.count = count;
			this.holder = holder;
		}

		@Override
		public void onClick(View v) {
			ContactVo tx1 = txs.get(index);
			if (!tx1.isDel()) {
				tx1.setDel(true);
				selectItemCount++;
			} else {
				tx1.setDel(false);
				selectItemCount--;
			}
			if (selectItemCount == 0) {
				isselectall = false;
				InviteContactsActivity.this.statePromptText.setText("全选");
			} else {
				if (selectItemCount == txs.size()) {
					isselectall = true;
					InviteContactsActivity.this.statePromptText.setText("取消全选");
				} else {
					isselectall = false;
					// InviteContactsActivity.this.statePromptText.setText("全选");
				}
			}
			holder.conSelect.setChecked(tx1.isDel());
			numPromptText.setText(InviteContactsActivity.this.getResources()
					.getString(R.string.select_invite_contacts)
					+ "("
					+ selectItemCount + ")");
		}

	}

	public boolean isMobileNO(String mobiles) {
		String phone = Utils.get11Number(mobiles);
		Pattern p = Pattern.compile(getResources().getString(
				R.string.regex_telephone_validation));
		Matcher m = p.matcher(phone);
		if (m.matches()) {
			return true;
		}
		return false;

	}

	private static class ContactVo {

		private String contact_name;
		private String contact_pinyin_name;
		private String phone;
		private boolean isDel;

		public String getContact_name() {
			return contact_name;
		}

		public void setContact_name(String contact_name) {
			this.contact_name = contact_name;
			this.contact_pinyin_name = CnToSpell.getFullSpell(contact_name);
		}

		public String getContact_pinyin_name() {
			return contact_pinyin_name;
		}

		public String getPhone() {
			return phone;
		}

		public void setPhone(String phone) {
			this.phone = phone;
		}

		public boolean isDel() {
			return isDel;
		}

		public void setDel(boolean isDel) {
			this.isDel = isDel;
		}

	}
}