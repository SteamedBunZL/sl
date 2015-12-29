package com.tuixin11sms.tx.activity;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupCreateResult;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.DialogButtonHandler;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.SearchBar;

/** 好像是选择好友列表的activity */
public class SelectFriendListActivity extends BaseActivity implements
		View.OnClickListener {
	private static final String TAG = SelectFriendListActivity.class
			.getSimpleName();

	public static int maxMember = 200;
	
	private SmileyParser smileyParser;

	private static final int REFRESH_TBS = 11;
	public static final String SELECT_ONE = "selectOne";
	private boolean isSelectOne = false;
	public static final String CHAT_TYPE = "chatType";
	public static final String FROM = "from";
	public String whereFrom = "";
	public static final String FROM_GROUP_MEMBER = "fromGroupMember";
	public static final String CHAT_TYPE_CHAT_ONE = "chatTypeChatOne";
	public static final String CHAT_TYPE_MORE_NAMES = "chatTypeMoreNames";
	public static final String CHAT_TYPE_MORE_PHONES = "chatTypeMorePhones";
	public static final String CHAT_TYPE_SINGLE_TX = "chatTypeSingleTX";
	public static final String CHAT_TYPE_GROUP_OGJ = "chatTypeGroupObj";
	public static final String CHAT_TYPE_ZF_OGJ = "chatTypeZfObj";
	public static final String CHAT_TYPE_CARD_TYPE = "chatTypeCardType";
	public static final String CHAT_TYPE_CARD_OBJ = "chatTypeCardObj";
	private int chatType;
	// int defaltHeaderImg;
	// int defaltHeaderImgMan;
	// int defaltHeaderImgFemale;
	/**
	 * 单人
	 */
	public static final int CHAT_TYPE_SINGLE = 40;
	/**
	 * 群发
	 */
	public static final int CHAT_TYPE_MORE = 41;
	/**
	 * 群
	 */
	public static final int CHAT_TYPE_GROUP = 42;
	/**
	 * 转发
	 */
	public static final int CHAT_TYPE_ZF = 43;
	/**
	 * 名片
	 */
	public static final int CHAT_TYPE_CARD = 44;

	public static final int SHOW_DEFAULT = 50;

	public static final int CHAT_TYPE_CARD_TUIXIN = 440;
	public static final int CHAT_TYPE_CARD_SMS = 441;

	public static final int CHAT_TYPE_CARD_RESULT_SMS = -100;

	public static final int CREATE_TIMEOUT = 60;

	private Bundle bundle;

	// private ArrayList<TX> contacts;// 全部联系人
	private ArrayList<TX> tbs;// 神聊联系人
	private TX singleTX;
	private View mHandleType;
	private EditText mContact;
	private TextView promptText;
	private ImageView listHintEmptyImageView;
	private View listHintEmpty;
	private SearchBar searchBar;

	private ListView txsList;
	private RadioButton mTbs;
	private RadioButton mGroup;
	private Button mClear;
	private Button mSelected;
	private Button mCancel;
//	private Button mCreateGroup;
	private List<TX> matchList;
	private ArrayList<TX> txMatchList;
	// private List<String> mList; // 防止checkbox点中后混乱
	private String sb = "";
	private ContectListAdapter contectAdapter;

	private Map<String, Integer> posContacts;
	private Map<String, Integer> posTbs;
	String specil = "?+*"; // 过滤特殊字符
	private TXMessage txMessage;
	private boolean isFromCreateGroup;

	private UpdateReceiver updatareceiver;
	// //////////////////////////////////////////////////////
	public static final int SHOW_NO_SDCAED = 1;
	public static final int SHOW_IMG_NOSDCAED = 3;
	public final static int CONTACT_CREATE_FAIL = 5;
	public static final int SHOW_DIALOG = 6;
	private ContactAPI api;
	private String groupname;
	// 防止添加的群成员多于10人
	private int memberCount = 1;
	private boolean init;

	private TxGroup group; // 通过其他页面进入此页面得到的群对象
	private int cardType;
	private Drawable moredefaltHeaderImg;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		TxData.addActivity(this);
		Utils.txList = null;
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_select_friend_list);
		memberCount = 0;
		smileyParser = new SmileyParser(this);
		initViews();
		getData();
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
		api = ContactAPI.getAPI();
		api.setCr(getContentResolver());

		// 联系人框
		mContact.addTextChangedListener(watcher);
		mContact.setOnClickListener(new editListener());

	}

	public void getData() {
		contectAdapter = new ContectListAdapter();
		matchList = new ArrayList<TX>();
		posContacts = new HashMap<String, Integer>();
		posTbs = new HashMap<String, Integer>();
		txMatchList = new ArrayList<TX>();
		txsList.setAdapter(contectAdapter);
		//
		// defaltHeaderImg =
		// getResources().getDrawable(R.drawable.no_contact_photo);
		// defaltHeaderImg.setBounds(0, 0, defaltHeaderImg.getIntrinsicWidth(),
		// defaltHeaderImg.getIntrinsicHeight());
		// moredefaltHeaderImg =
		// getResources().getDrawable(R.drawable.broadcast_portrait);
		// moredefaltHeaderImg.setBounds(0, 0,
		// defaltHeaderImg.getIntrinsicWidth(),
		// defaltHeaderImg.getIntrinsicHeight());
		//
		isSelectOne = true;
		threadGetData();
	}

	private void threadGetData() {
		// tbs = TX.getTBListFilterNPC(true);
		tbs = TX.tm.getTBTXList();
		if (tbs == null) {
			tbs = new ArrayList<TX>();
		}
		// ///要改
//		Collections.sort(tbs, new FirstCharComparator(TxInfor.TX_TYPE_DEFAULT));
		bundle = getIntent().getExtras();
		if (bundle != null) {
			chatType = bundle.getInt(CHAT_TYPE);
			whereFrom = bundle.getString(FROM);
			isSelectOne = bundle.getBoolean(SELECT_ONE, false);
			switch (chatType) {
			case CHAT_TYPE_SINGLE:
				handleMessage(CHAT_TYPE_SINGLE);
				break;
			case CHAT_TYPE_MORE:
				handleMessage(CHAT_TYPE_MORE);
				break;
			case CHAT_TYPE_GROUP:
				handleMessage(CHAT_TYPE_GROUP);
				break;
			case CHAT_TYPE_ZF:
				handleMessage(CHAT_TYPE_ZF);
				break;
			case CHAT_TYPE_CARD:
				handleMessage(CHAT_TYPE_CARD);
				break;
			case SHOW_DEFAULT:
				handleMessage(SHOW_DEFAULT);
				mHandleType.setVisibility(View.GONE);
				break;
			}
		} else {
			handleMessage(SHOW_DEFAULT);
		}
		// 初始化各字母的位置
		initItemPosition();
	}

	private void showTbs() {
		searchBar.setVisibility(View.GONE);
		matchList.clear();
		matchList.addAll(tbs);
		mTbs.setChecked(true);
		if (tbs.size() == 0) {
			txsList.setVisibility(View.GONE);
			listHintEmpty.setVisibility(View.VISIBLE);
			mHandleType.setVisibility(View.GONE);
		}
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			cancelDialogTimer();
			switch (msg.what) {
			case SHOW_DEFAULT:
				showTbs();
				contectAdapter.notifyDataSetChanged();
				break;
			case REFRESH_TBS:
				// tbs = TX.getTBListFilterNPC(true);
				tbs = TX.tm.getTBTXList();
				if (tbs.size() != 0) {
					matchList.clear();
					matchList.addAll(tbs);
					contectAdapter.notifyDataSetChanged();
					txsList.setVisibility(View.VISIBLE);
					listHintEmpty.setVisibility(View.GONE);
					mHandleType.setVisibility(View.VISIBLE);
					tbs();
				}
				break;
			case CHAT_TYPE_SINGLE:
				// 需要显示的联系人
				TX tx = bundle.getParcelable(CHAT_TYPE_SINGLE_TX);

				if (Utils.isIdValid(tx.partner_id)) {
					for (TX item : tbs) {
						if (item.partner_id == tx.partner_id) {
							singleTX = item;
							tbs.remove(item);
							memberCount = 1;
							break;
						}
					}
				}
				showTbs();
				contectAdapter.notifyDataSetChanged();
				showCurrentMember();
				break;
			case CHAT_TYPE_GROUP:
				group = bundle.getParcelable(CHAT_TYPE_GROUP_OGJ);
				if (group != null && group.group_tx_ids != null) {
					String[] ids = group.group_tx_ids.split("�");
					for (TX txItem : tbs) {
						for (String id : ids) {
							if (!Utils.isNull(id)) {
								if (txItem.partner_id == Long.valueOf(id)) {
									txMatchList.add(txItem);
								}
							}
						}
					}
					tbs.removeAll(txMatchList);
					txMatchList.clear();
					memberCount = ids.length;
				}
				showTbs();
				contectAdapter.notifyDataSetChanged();
				showCurrentMember();
				break;
			case CHAT_TYPE_ZF:
				txMessage = bundle.getParcelable(CHAT_TYPE_ZF_OGJ);
				showTbs();
				contectAdapter.notifyDataSetChanged();
				break;
			case CHAT_TYPE_CARD:
				cardType = bundle.getInt(CHAT_TYPE_CARD_TYPE);
				if (Utils.debug)
					Log.i(TAG, "cardType =====" + cardType);
				switch (cardType) {
				case CHAT_TYPE_CARD_TUIXIN:
					showTbs();
					break;
				}
				contectAdapter.notifyDataSetChanged();
				break;
			case CREATE_TIMEOUT:
				Utils.startPromptDialog(SelectFriendListActivity.this,
						R.string.prompt, R.string.request_outtime);
				break;
			}
			if (bundle != null) {
				switch (msg.what) {
				case CHAT_TYPE_SINGLE:
				case CHAT_TYPE_MORE:
				case CHAT_TYPE_GROUP:
				case CHAT_TYPE_ZF:
				case CHAT_TYPE_CARD:
					sb = "";
					init = true;
					for (int i = 0; i < txMatchList.size(); i++) {
						TX tx = txMatchList.get(i);
						String name = "";
						if (!Utils.isNull(tx.getRemarkName())) {
							name = tx.getRemarkName();
						} else {
							if (!Utils.isNull(tx.getNick_name())) {
								name = tx.getNick_name();
							}
						}
						int length = mContact.getText().toString().length();
						addContact4Edit(length, name, tx);
					}
					init = false;
					break;
				}
			}
		}

	};

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				ImageView headImage = (ImageView) txsList.findViewWithTag((Long)result[1]);
				if(headImage!= null){
					long tag = (Long) headImage.getTag();
					long id = (Long)result[1];
					if (result[0] != null&&tag == id) {
						headImage.setImageBitmap((Bitmap) result[0]);
					}
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	private void initViews() {
		mHandleType = findViewById(R.id.mHandleType);
		mTbs = (RadioButton) findViewById(R.id.mTbs);
		mGroup = (RadioButton) findViewById(R.id.mGroup);
		mContact = (EditText) findViewById(R.id.createMsg_up_edit);
		mSelected = (Button) findViewById(R.id.mSelected);
		mClear = (Button) findViewById(R.id.mClear);
		mCancel = (Button) findViewById(R.id.mCancel);
//		mCreateGroup = (Button) findViewById(R.id.mCreateGroup);
		tuixin_info_title = (TextView) findViewById(R.id.tuixin_info_title);
		mClear.setOnClickListener(this);
		mTbs.setOnClickListener(this);
		mGroup.setOnClickListener(this);
		mSelected.setOnClickListener(this);
		mCancel.setOnClickListener(this);
//		mCreateGroup.setOnClickListener(this);

		txsList = (ListView) findViewById(R.id.contacts_list);
		// a-z字符提示
		promptText = (TextView) findViewById(R.id.prompt_text);
		listHintEmpty = findViewById(R.id.list_hint_empty);
		listHintEmptyImageView = (ImageView) findViewById(R.id.list_hint_empty_imageview);
		searchBar = (SearchBar) findViewById(R.id.mSearchBar);
		searchBar.setVisibility(View.GONE);
		searchBar.setTv(promptText);
		SearchBar.listview = txsList;
		// defaltHeaderImgMan = R.drawable.tui_con_photo;
		// defaltHeaderImgFemale = R.drawable.sl_regist_head_femal;
	}

	private void tbs() {
		mSelected.setVisibility(View.VISIBLE);
		mCancel.setVisibility(View.VISIBLE);
//		mCreateGroup.setVisibility(View.GONE);
	}

//	private void group() {
//		mHandleType.setVisibility(View.VISIBLE);
//		mSelected.setVisibility(View.GONE);
//		mCancel.setVisibility(View.GONE);
//		mCreateGroup.setVisibility(View.VISIBLE);
//	}

	private void addContact4Edit(int length, String name, TX tx) {
		/*
		 * TextView textView = new TextView(this); textView.setText(name);
		 * textView.setTextSize(txdata.dip2px(13));
		 * textView.setTextColor(Color.BLACK);
		 * textView.setBackgroundResource(R.drawable.chat_p1);
		 * textView.setDrawingCacheEnabled(true); // this is the important code
		 * :) // // Without it the view will // have a dimension of 0,0 and //
		 * the bitmap will be null textView.measure(MeasureSpec
		 * .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED), MeasureSpec
		 * .makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)); textView.layout(0, 0,
		 * textView.getMeasuredWidth(), textView .getMeasuredHeight());
		 * textView.buildDrawingCache(true);
		 * 
		 * Bitmap left =
		 * getBitmap(Bitmap.createBitmap(textView.getDrawingCache()));
		 * textView.setDrawingCacheEnabled(false); // clear drawing cache
		 * 
		 * Bitmap localBitmap = BitmapFactory.decodeResource( getResources(),
		 * R.drawable.recipient_view_bg).copy( Bitmap.Config.ARGB_8888, true);
		 * 
		 * Bitmap right =
		 * getBitmap(BitmapFactory.decodeResource(this.getResources(),
		 * R.drawable.chat_p2));
		 * 
		 * Bitmap all =
		 * getBitmap(Bitmap.createBitmap(left).copy(Bitmap.Config.ARGB_8888,
		 * true)); float scale = (float) ((float) left.getHeight() / (float)
		 * right.getHeight() * 0.9); Matrix matrix = new Matrix();
		 * matrix.postScale(scale, scale); Bitmap resizeBmp =
		 * getBitmap(Bitmap.createBitmap(right, 0, 0, right.getWidth(),
		 * right.getHeight(), matrix, true)); Canvas c = new Canvas(all); Paint
		 * paint = new Paint(); c.drawBitmap(left, 0, 0, paint);
		 * c.drawBitmap(resizeBmp, left.getWidth() - resizeBmp.getWidth(),
		 * (all.getHeight()/2 - right.getHeight()/2), paint);
		 * 
		 * ImageSpan span = new ImageSpan(all,
		 * DynamicDrawableSpan.ALIGN_BOTTOM); SpannableString sp = new
		 * SpannableString("<" + phoneName + ">,"); sp.setSpan(span, 0, ("<" +
		 * phoneName + ">,").length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		 */

		mContact.getText().append(name + ";");
		mContact.setSelection(mContact.getText().length());
		tuixin_info_title.setText("已选定"+txMatchList.size()+"人");

		// left.recycle();
		// right.recycle();
		// box.setChecked(true);
	}

	class ContectListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;

		public ContectListAdapter() {
			this.mInflater = LayoutInflater.from(SelectFriendListActivity.this);
		}

		@Override
		public int getCount() {
			if (matchList == null) {
				matchList = new ArrayList<TX>();
			}
			return matchList.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {
			ContactHolder contact = null;
			if (convertView == null) {
				contact = new ContactHolder();
				convertView = this.mInflater.inflate(R.layout.sll_createmsglist,
						null);
				contact.photo = (ImageView) convertView
						.findViewById(R.id.creatmsgList_contentPhoto);
				contact.displayName = (TextView) convertView
						.findViewById(R.id.creatmsgList_contentName);
				contact.phone = (TextView) convertView
						.findViewById(R.id.creatmsgList_contentPhone);
				contact.checked = (CheckBox) convertView
						.findViewById(R.id.creatmsgList_contentSelect);
				convertView.setTag(contact);
			} else {
				contact = (ContactHolder) convertView.getTag();
			}
			if (isSelectOne) {
				contact.checked.setVisibility(View.GONE);
			} else {
				contact.checked.setVisibility(View.VISIBLE);
			}
			final TX tx = matchList.get(position);
			String name = "";
			// String phone = "";
			if (!Utils.isNull(tx.getRemarkName())) {
				name = tx.getRemarkName();
			} else {
				if (!Utils.isNull(tx.getNick_name())) {
					name = tx.getNick_name();
				}
			}

			// name = tx.getNick_name();
			// if(tx.sex==0){
			// defaltHeaderImg=defaltHeaderImgMan;
			// }else{
			// defaltHeaderImg=defaltHeaderImgFemale;
			// }
			// phone = "" + tx.partner_id;
			contact.phone
					.setText(SelectFriendListActivity.this.getResources()
							.getString(R.string.create_shenliao_number)
							+ tx.partner_id);
			// 头像
			// contact.photo.setBackgroundResource(defaltHeaderImg);
			if (Utils.isIdValid(tx.partner_id)) {

				contact.photo.setTag(tx.partner_id);
				contact.photo.setImageResource(tx.getSex() == TX.MALE_SEX ? defaultHeaderImgMan
						: defaultHeaderImgFemale);

				Bitmap bm = mSess.avatarDownload.getAvatar(tx.avatar_url,
						tx.partner_id, contact.photo, avatarHandler);

				if (bm != null) {
					contact.photo.setImageBitmap(bm);
				}

			}

			contact.displayName.setText(smileyParser.addSmileySpans(
					name, true,
					BaseMsgRoom.nickNameMaxNum));

			convertView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (isSelectOne) {
						Intent i = new Intent(SelectFriendListActivity.this,
								SingleMsgRoom.class);
						i.putExtra(Utils.MSGROOM_TX, tx.partner_id);
						startActivity(i);
						SelectFriendListActivity.this.finish();
						return;
					}
					String name = "";
					if (!Utils.isNull(tx.getRemarkName())) {
						name = tx.getRemarkName();
					} else {
						if (!Utils.isNull(tx.getNick_name())) {
							name = tx.getNick_name();
						}
					}

					int length = mContact.getText().toString().length();
					if (txMatchList.contains(tx)) {
						txMatchList.remove(tx);
						candel = true;
						String[] nameTemp = name.split("");
						StringBuffer newName = new StringBuffer();
						for (String s : nameTemp) {
							if (s.equals(""))
								continue;
							if (Utils.specilStr.indexOf(s) != -1) {
								newName.append("\\");
								newName.append(s);
							} else {
								newName.append(s);
							}
						}
						mContact.setText(mContact.getText().toString()
								.replaceFirst(newName.toString() + ";", ""));
						tuixin_info_title.setText("已选定"+txMatchList.size()+"人");
					} else {
						// 处于转发名片模式
						if (chatType == CHAT_TYPE_CARD
								|| chatType == CHAT_TYPE_ZF) {
							if (txMatchList.size() >= 1) {
								// txdata.showToast(R.string.create_member_onlyone);
								showToast(R.string.create_member_onlyone);
								return;
							}
						}
						if (txMatchList.size() + memberCount
								+ (chatType == CHAT_TYPE_GROUP ? 0 : 1) >= maxMember) {
							Toast.makeText(SelectFriendListActivity.this,
									R.string.group_members_too_more,
									Toast.LENGTH_SHORT).show();

							return;
						}
						if (!"".equals(sb)) {
							String editString = mContact.getText().toString();
							if (editString != null) {
								if (editString.endsWith(sb)) {
									candel = true;
									mContact.setText(mContact
											.getText()
											.toString()
											.subSequence(
													0,
													editString.length()
															- sb.length()));
								}
							}
						}
						
						txMatchList.add(tx);
						addContact4Edit(length, name, tx);// 把选中的联系人放在EditText中
					}
					updateContacts("");

				}
			});

			boolean checked = false;
			if (txMatchList.contains(tx)) {
				checked = true;
			}
			contact.checked.setChecked(checked);
			return convertView;
		}
	}

	private class ContactHolder {
		ImageView photo;
		TextView displayName;
		TextView phone;
		CheckBox checked;
	}

	// 上方的输入框 搜索事件
	private class editListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// 覆盖版面
			getWindow().setSoftInputMode(
					WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

			int length = mContact.getText().length();
			String[] obs = mContact.getText().toString().split(";");
			int selectionPos = mContact.getSelectionStart();
			int itemPos = 0;
			List<Integer> posList = new ArrayList<Integer>();
			int i = 0;
			for (; i < obs.length; i++) {
				itemPos += obs[i].length() + 1;
				posList.add(itemPos);
				if (selectionPos < itemPos) {
					break;
				}
			}
			if (i == 0) {
				selectionPos = 0;
			} else {
				selectionPos = posList.get(i - 1);
			}
			if (itemPos == 1)
				return;

			// 防止数组越界
			if (itemPos > length) {
				itemPos = length;
			}
			if (selectionPos > length) {
				selectionPos = length;
			}
			if (selectionPos <= itemPos - 1) {
				Selection.setSelection(mContact.getText(), selectionPos,
						itemPos);
				candel = true;
				/*
				 * if (mList.contains(s)) { int index = mList.indexOf(s);
				 * mList.remove(index); TX currentTx =
				 * txMatchList.remove(index); if (specialTX != null&&
				 * specialTX.partner_id == currentTx.partner_id) { specialTX =
				 * null; } } else { isDeleteLastStr = true; }
				 */
			}
		}

	}

	public List<TX> queryContactsByFilter(String filter) {
		List<TX> result = new ArrayList<TX>();
		if ("".equals(filter)) {
			result.addAll(tbs);
			return result;
		} else {
			List<TX> temp = null;
			if (tbs != null) {
				temp = tbs;
			} else {
				temp = new ArrayList<TX>();
			}
			for (TX tx : temp) {
				String name = "";
				String pinyin = "";
				String phone = "";
				if (!Utils.isNull(tx.getRemarkName())) {
					name = tx.getRemarkName();
				} else {
					if (!Utils.isNull(tx.getNick_name())) {
						name = tx.getNick_name();
					}
				}
				// name = tx.getNick_name();
				pinyin = tx.nick_name_pinyin;
				phone = "" + tx.partner_id;
				// 中文就取出拼音

				if (name.startsWith(filter) || pinyin.startsWith(filter)
						|| phone.startsWith(filter)) {
					result.add(tx);
				}
			}
			return result;
		}

	}

	int mbefore;
	String beforeStr;
	int now;
	String nowStr;
	boolean candel = false;

	// 联系人输入框内容监控
	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
			mbefore = s.length();
			beforeStr = s.toString();
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (!init) {
				boolean flag = false;
				nowStr = s.toString();
				now = s.length();
				if (now < mbefore && !candel) {
					int length = mContact.getText().length();
					int selectionPos = mContact.getSelectionStart();
					if (length - 1 == selectionPos) {
						mContact.setText(beforeStr);
						mContact.setSelection(mbefore);
					} else {
						mContact.setText(beforeStr);
					}
					int itemPos = 0;
					List<Integer> posList = new ArrayList<Integer>();

					String str = ".*?;";
					String contactStr = mContact.getText().toString();
					if (contactStr.length() > 0) {
						Pattern pattern = Pattern.compile(str);
						Matcher matcher = pattern.matcher(contactStr);
						while (matcher.find()) {
							if (selectionPos <= matcher.end()
									&& selectionPos >= matcher.start()) {
								Selection.setSelection(mContact.getText(),
										matcher.start(), matcher.end());
								flag = true;
								candel = true;
								break;
							} else {
								posList.add(matcher.start());
							}
						}
					}
					if (!flag) {

						if (posList.size() > 0) {
							selectionPos = posList.get(posList.size() - 1);
						}
						itemPos = length;
						Selection.setSelection(mContact.getText(),
								selectionPos, itemPos);
						candel = true;
					}

				} else {
					candel = false;
				}

				String temp = s.toString();
				if (temp.length() > 0) {
					mClear.setVisibility(View.VISIBLE);
				} else {
					mClear.setVisibility(View.GONE);
				}
				int index = temp.lastIndexOf(";");
				if (index > 0) {
					sb = s.toString().substring(index + 1, s.length());
					if (sb.trim().equals("")) {
						sb = "";
					}
				} else {
					sb = temp;
				}
				String temp2 = "";
				int nameCount = 0;
				if (beforeStr.length() > nowStr.length()) {
					temp2 = beforeStr.replaceFirst(nowStr, "");
					int nowstart = 0;
					while (nowstart != beforeStr.length()) {
						int i = beforeStr.indexOf(temp2, nowstart);
						if (i != -1) {
							nameCount++;
							nowstart = i + 1;
						} else
							break;
					}
				}
				if (nameCount >= 2) {
					nameCount--;
				}
				String[] obs = mContact.getText().toString().split(";");
				Set<TX> newTXList = new HashSet<TX>();
				int c = 0;
				// 打了勾的选项如果没出现在上面 会给用户造成困惑
				for (int i = 0; i < obs.length; i++) {
					for (TX tx : txMatchList) {
						String newname = "";
						if (!Utils.isNull(tx.getRemarkName())) {
							newname = tx.getRemarkName();
						} else {
							if (!Utils.isNull(tx.getNick_name())) {
								newname = tx.getNick_name();
							}
						}
						if (newname.equals(obs[i])) {
							if (temp2.equals(obs[i] + ";")) {
								c++;
								if (c <= nameCount) {
									newTXList.add(tx);
								}
							} else {
								newTXList.add(tx);
							}

						}
					}

				}
				txMatchList.clear();
				for (TX tx : newTXList) {
					txMatchList.add(tx);
				}
				if (index > 0) {
					updateContacts(sb);
				} else {
					updateContacts(temp);
				}
			}
			showCurrentMember();
		}
	};

	public static int numberOfStr(String str, String con) {
		if (str.endsWith(con)) {
			return str.split(con).length;
		} else {
			return str.split(con).length - 1;
		}
	}

	public synchronized void updateContacts(String s) {
		int length = mContact.getText().toString().length();
		int index = mContact.getSelectionStart();
		int fenhao = mContact.getText().toString().lastIndexOf(";");
		matchList = queryContactsByFilter(s);
		/*
		 * if(matchList.size() == 0 && length> 0 && currentView == 0){ candel =
		 * true;
		 * mContact.setText(mContact.getText().toString().subSequence(0,length
		 * -1)); mContact.setSelection(mContact.getText().toString().length());
		 * txdata.showToast(R.string.create_no_result); if(!"".endsWith(s)){ s =
		 * s.substring(0,s.length() - 1); updateContacts(s); } //return; }
		 */

		if (length > 0 && index - 1 > fenhao) {
			candel = true;
		}

		/*
		 * Collections.sort(matchList, new FirstCharComparator(
		 * FirstCharComparator.TX_CS_TYPE));
		 */
		contectAdapter.notifyDataSetChanged();

		showCurrentMember();
	}

	private void initItemPosition() {

		if (tbs != null) {
			for (int i = tbs.size() - 1; i >= 0; i--) {
				TX tx = tbs.get(i);
				String spell = tx.getNick_name_pinyin();
				if (spell.length() > 0) {
					posTbs.put(spell.substring(0, 1).toLowerCase(), i);
				}
			}
		}
		SearchBar.posMap = posTbs;
	}

	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onRestart() {
		showCurrentMember();
		super.onRestart();
	}

	public void onResume() {
		registReceiver();

		super.onResume();
	}

	private void showCurrentMember() {
		if ((memberCount != 0 && chatType == CHAT_TYPE_GROUP)
				|| singleTX != null) {
			mSelected.setText(SelectFriendListActivity.this.getResources()
					.getString(R.string.current_member_start));
		} else {
			/*
			 * mSelected.setText(txMatchList.size() == 0 ?
			 * SelectFriendListActivity
			 * .this.getResources().getString(R.string.create_select) :
			 * SelectFriendListActivity
			 * .this.getResources().getString(R.string.create_select_start)
			 * +txMatchList.si);
			 */
			mSelected.setText(SelectFriendListActivity.this.getResources()
					.getString(R.string.create_select));
		}
	}

	public void onStop() {
		unregistReceiver();
		super.onStop();
	}

	public void onDestroy() {
		// stopAsyncload();
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.mClear:
			candel = true;
			mContact.setText("");
			txMatchList.clear();
			mClear.setVisibility(View.GONE);
			break;
		case R.id.mCancel:
			this.finish();
			break;
		case R.id.mSelected:
			String contactStr = mContact.getText().toString();
			if (contactStr.length() > 0) {
				int listSize = txMatchList.size();
				if (listSize != 0) {
					// 处于发送名片模式
					if (chatType == CHAT_TYPE_CARD || chatType == CHAT_TYPE_ZF) {
						if (txMatchList.size() == 0) {
							return;
						}
						if (txMatchList.size() > 1 || txMatchList.size() == 0) {
							// txdata.showToast(R.string.create_member_onlyone);
							showToast(R.string.create_member_onlyone);
							return;
						}
						// 转发
						if (txMessage != null) {
							TxData.finishOne(SingleMsgRoom.class.getName());
							TxData.finishOne(GroupMsgRoom.class.getName());
							// TxData.finishOne(AccostMsgRoom.class.getName());
							Intent intent = new Intent(this,
									SingleMsgRoom.class);
							intent.putExtra("threadId", -1);// (int)
															// MmsUtils.getOrCreateThreadId(this,new
															// String[] {
															// txMatchList.get(0).phone
															// }));
							intent.putExtra(Utils.MSGROOM_TX,
									txMatchList.get(0).partner_id);
							intent.putExtra(CHAT_TYPE_ZF_OGJ, txMessage);
							startActivity(intent);
							this.finish();
							// 发名片
						} else {
							TX txCard = txMatchList.get(0);
							if (txCard != null) {
								Intent icard = new Intent();
								icard.putExtra(CHAT_TYPE_CARD_OBJ, txCard);
								setResult(CHAT_TYPE_CARD, icard);
								this.finish();
							}
						}

					} else {
						// 选人聊天模式
						int chatTypeValue = chatType(txMatchList);
						if (group != null) {
							chatTypeValue = CHAT_TYPE_GROUP;
						}
						if (chatTypeValue == CHAT_TYPE_SINGLE
								&& singleTX != null) {
							chatTypeValue = CHAT_TYPE_GROUP;
						}
						switch (chatTypeValue) {

						// 单人
						case CHAT_TYPE_SINGLE:
							TxData.finishOne(SingleMsgRoom.class.getName());
							Intent intent = new Intent(this,
									SingleMsgRoom.class);
							intent.putExtra("threadId", -1);// (int)MmsUtils.getOrCreateThreadId(this,new
															// String[] {
															// txMatchList.get(0).phone
															// }));
							intent.putExtra(Utils.MSGROOM_TX,
									txMatchList.get(0).partner_id);
							startActivity(intent);
							this.finish();
							break;
						// 群
						case CHAT_TYPE_GROUP:
							if (txMatchList.size() + memberCount > maxMember) {
								Toast.makeText(SelectFriendListActivity.this,
										R.string.group_members_too_more,
										Toast.LENGTH_SHORT).show();
								return;
							}
							// 通过其他页面进入的--拉人聊天模式
							if (chatType == CHAT_TYPE_GROUP) {
								List<Long> nowIds = new ArrayList<Long>();
								if (group != null && group.group_tx_ids != null) {
									for (TX tx : txMatchList) {
										nowIds.add(tx.partner_id);
									}
									if (nowIds.size() > 0) {
										mSess.getSocketHelper()
												.sendDealGroup(group.group_id,
														true, nowIds);
										showDialogTimer(this, 0,
												R.string.create_group_add,
												10 * 1000, new BaseTimerTask() {
													@Override
													public void run() {
														super.run();
														Message msg = handler
																.obtainMessage();
														msg.what = CREATE_TIMEOUT;
														handler.sendMessage(msg);
													}
												}).show();
									} else if (tbs.size() == 0) {
										// txdata.showToast(R.string.create_no_member);
										showToast(R.string.create_no_member);
									} else {
										// txdata.showToast(R.string.create_select_member);
										showToast(R.string.create_select_member);
									}
								}
							} else {
								showCreatGroup();
							}
							break;
						}
					}
				}
				return;
			} else {
				if (chatType == CHAT_TYPE_GROUP) {
					if (memberCount + txMatchList.size() >= maxMember) {
						Toast.makeText(
								this,
								SelectFriendListActivity.this
										.getResources()
										.getString(
												R.string.create_group_member_start)
										+ maxMember
										+ SelectFriendListActivity.this
												.getResources()
												.getString(
														R.string.create_group_member_end),
								Toast.LENGTH_SHORT).show();
						return;
					}
				}
				// txdata.showToast(R.string.create_select_member);
				showToast(R.string.create_select_member);
			}
			break;
		}
		contectAdapter.notifyDataSetChanged();
	}

	private void showCreatGroup() {

		final EditText nameText1 = new EditText(SelectFriendListActivity.this);
		nameText1.setSingleLine(true);
		nameText1.setFilters(new InputFilter[] { new InputFilter.LengthFilter(
				12) });
		final AlertDialog nameDialog1 = new AlertDialog.Builder(
				SelectFriendListActivity.this).create();
		nameDialog1.setView(nameText1);
		nameDialog1.setTitle(R.string.create_group_name);
		nameDialog1.setButton(this.getResources().getString(R.string.confirm),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						String nameStr = nameText1.getText().toString().trim();
						int len = nameStr.length();
						if (len <= 0) {
							Toast.makeText(SelectFriendListActivity.this,
									R.string.name_too_short, Toast.LENGTH_SHORT)
									.show();
						} else if (len > Utils.INPUT_GROUPNAME_MAX_LENGTH) {
							Toast.makeText(SelectFriendListActivity.this,
									R.string.name_too_long, Toast.LENGTH_SHORT)
									.show();
						} else if (!Utils.filterStr(nameStr,
								SelectFriendListActivity.this)) {
							Utils.startPromptDialog(
									SelectFriendListActivity.this,
									R.string.error, R.string.name_sys_error);
						} else {
							createGroup(nameStr, false);
							InputMethodManager imm = (InputMethodManager) SelectFriendListActivity.this
									.getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(
									nameText1.getWindowToken(), 0);
							dialog.cancel();
						}
					}
				});
		nameDialog1.setButton2(this.getResources().getString(R.string.skip),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						createGroup(null, true);
						dialog.cancel();
						InputMethodManager imm = (InputMethodManager) SelectFriendListActivity.this
								.getSystemService(Context.INPUT_METHOD_SERVICE);
						imm.hideSoftInputFromWindow(nameText1.getWindowToken(),
								0);
					}
				});
		try {
			Field field = nameDialog1.getClass().getDeclaredField("mAlert");
			field.setAccessible(true);
			// 获得mAlert变量的值
			Object obj = field.get(nameDialog1);
			field = obj.getClass().getDeclaredField("mHandler");
			field.setAccessible(true);
			// 修改mHandler变量的值，使用新的ButtonHandler类
			field.set(obj, new DialogButtonHandler(nameDialog1));
		} catch (Exception e) {
		}
		nameDialog1.getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		nameDialog1.show();

		Utils.openKeyboard(mSess.getContext(),nameText1);

	}

	private void createGroup(String nameStr, boolean isdefault) {
		String names = TX.tm.getTxMe().getNick_name() + "、";
		showDialogTimer(this, 0, R.string.create_group_talk, 10 * 1000,
				new BaseTimerTask() {
					public void run() {
						super.cancel();
						Message msg = handler.obtainMessage();
						msg.what = CREATE_TIMEOUT;
						handler.sendMessage(msg);
					}
				}).show();
		if (singleTX != null && !txMatchList.contains(singleTX)) {
			txMatchList.add(singleTX);
		}
		long[] ids = new long[txMatchList.size()];
		for (int i = 0; i < txMatchList.size(); i++) {
			names += txMatchList.get(i).getNick_name() + "、";
			ids[i] = txMatchList.get(i).partner_id;
		}
		if (isdefault) {
			int length = names.length();
			if (length > 12) {
				groupname = names.subSequence(0, 12).toString();
			} else {
				groupname = names;
			}
			if (groupname.lastIndexOf("、") == groupname.length() - 1) {
				groupname = groupname.subSequence(0, groupname.length() - 1)
						.toString();
			}
		} else {
			groupname = nameStr;
		}
		// SocketHelper.getSocketHelper(txdata).sendCreatGroup(groupname, ids);
		Utils.txList = txMatchList;
	}

	boolean canDel = false;

	private TextView tuixin_info_title;

	/**
	 * 根据联系人集合判断是群还是群发 群：全部是神聊用户 群发：有任何一个联系人不是神聊用户
	 * 
	 * @param TXList
	 * @return 40:单人 41：群发 42：群
	 */
	public int chatType(List<TX> txList) {
		if (txList.size() == 1) {

			return CHAT_TYPE_SINGLE;
		}

		return CHAT_TYPE_GROUP;
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent.getAction())) {
				dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_ADD_GROUP_MEMBER.equals(intent
					.getAction())) {
				dealAddGroupMember(serverRsp);
			} else if (Constants.INTENT_ACTION_DEL_GROUP_MEMBER.equals(intent
					.getAction())) {
				dealDelGroupMember(serverRsp);
			} else if (Constants.INTENT_ACTION_CREATE_GROUP.equals(intent
					.getAction())) {
				TxGroup txgroup = Utils.getTxGroup(intent);
				dealCreateGroup(serverRsp, txgroup);
			}
		}
	}

	private void dealSystemMsg(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_MAYBE_KNOW: {
				handleMessage(REFRESH_TBS);
				break;
			}
			}
		}
	}

	/**
	 * 处理添加群成员应答
	 * 
	 * @param serverRsp
	 */
	private void dealAddGroupMember(ServerRsp serverRsp) {
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				Toast.makeText(SelectFriendListActivity.this,
						R.string.opt_success, Toast.LENGTH_SHORT).show();

				TxData.finishOne(GroupMsgRoom.class.getName());
				TxData.finishOne(GroupCreateResult.class.getName());
				if (null != whereFrom && whereFrom.equals(FROM_GROUP_MEMBER)) {
					ArrayList<String> ids = serverRsp.getBundle()
							.getStringArrayList("ids");
					ArrayList<TX> temp = new ArrayList<TX>();
					for (String id : ids) {
						// TX tx =
						// TX.findTXByPartnerID4DB(Long.valueOf(id));//不要直接访问数据库
						// 2013.10.17 shc
						TX tx = TX.tm.getTx(Long.valueOf(id));

						temp.add(tx);
					}
					Intent i = new Intent();
					i.putParcelableArrayListExtra("txs", temp);
					setResult(100, i);
					SelectFriendListActivity.this.finish();
					return;
				}
				Intent i = new Intent(SelectFriendListActivity.this,
						GroupMsgRoom.class);
				i.putExtra(Utils.MSGROOM_TX_GROUP, group);
				startActivity(i);
				SelectFriendListActivity.this.finish();
				break;
			}
			case GROUP_DISSOLVED: {
				Utils.startPromptDialog(SelectFriendListActivity.this,
						R.string.prompt, R.string.create_group_over);
				break;
			}
			case GROUP_NO_EXIST: {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.create();
				dialog.setTitle(R.string.prompt);
				dialog.setMessage(R.string.create_group_no);
				dialog.show();
				break;
			}
			case GROUP_MEMBER_SIZE_INVALID: {
				AlertDialog.Builder dialog = new AlertDialog.Builder(this);
				dialog.create();
				dialog.setTitle(R.string.prompt);
				dialog.setMessage(R.string.create_group_more);
				dialog.show();
				break;
			}
			case GROUP_MEMBER_OPT_NO_PERMISSION: {
				Utils.startPromptDialog(SelectFriendListActivity.this,
						R.string.prompt, R.string.create_group_quanxian);
				break;
			}
			case OPT_FAILED: {
				Utils.startPromptDialog(SelectFriendListActivity.this,
						R.string.prompt, R.string.create_group_failed);
				break;
			}
			case USER_IN_BLACK:
				Utils.startPromptDialog(SelectFriendListActivity.this,
						R.string.prompt, R.string.create_group_user_in_black);
				break;
			}
		}
	}

	/**
	 * 处理删除成员应答
	 * 
	 * @param serverRsp
	 */
	private void dealDelGroupMember(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case GROUP_MEMBER_SIZE_INVALID: {
			AlertDialog.Builder dialog = new AlertDialog.Builder(this);
			dialog.create();
			dialog.setTitle(R.string.prompt);
			dialog.setMessage(R.string.create_group_few);
			dialog.show();
			break;
		}
		}
	}

	private void dealCreateGroup(ServerRsp serverRsp, TxGroup txgroup) {
		if (singleTX != null && txMatchList.contains(singleTX)) {
			txMatchList.remove(singleTX);
		}
		switch (serverRsp.getStatusCode()) {
		case RSP_OK: {
			cancelTimer();
			Intent i = new Intent(SelectFriendListActivity.this,
					GroupMsgRoom.class);
			i.putExtra(Utils.MSGROOM_TX_GROUP, txgroup);
			startActivity(i);
			SelectFriendListActivity.this.finish();
			break;
		}
		case GROUP_MEMBER_SIZE_INVALID: {
			Utils.startPromptDialog(SelectFriendListActivity.this,
					R.string.prompt, R.string.create_group_more_few);
			break;
		}
		case GROUP_MEMBER_THAN_LIMIT: {
			Utils.startPromptDialog(SelectFriendListActivity.this,
					R.string.prompt, R.string.create_group_to_more);
			break;
		}
		case OPT_FAILED: {
			Utils.startPromptDialog(SelectFriendListActivity.this,
					R.string.prompt, R.string.create_group_failed);
			break;
		}
		}
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_ADD_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_DEL_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_CREATE_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	private void unregistReceiver() {
		if (updatareceiver != null) {
			this.unregisterReceiver(updatareceiver);
			updatareceiver = null;
		}
	}

	private void handleMessage(int what) {
		Message msg = new Message();
		msg.what = what;
		handler.sendMessage(msg);
	}
}
