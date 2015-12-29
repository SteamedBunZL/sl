package com.shenliao.group.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.adapter.SearchGroupIndexAdapter;
import com.tuixin11sms.tx.GroupListManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.core.MsgHelper;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class GroupSearch extends BaseActivity implements OnClickListener,
		OnTouchListener {

	View mGroupSearchLayout;
	TextView mGroupTitle;
	EditText mContent;
	ListView mGroupList;
	TextView mSearch;
	Button mClean;
	View mLoading;
	View line_top;
	public static boolean CHANGE = false;
	LinearLayout screen;

	int type; // 1，为公共聊天室，O，为私人群

	private UpdateReceiver updatareceiver;
	private SearchGroupIndexAdapter adapter;
	private List<TxGroup> groupList = new ArrayList<TxGroup>();
	private SmileyParser smileyParser;
	private List<Long> myGroupListids;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		// 获取我的群
		getMyGroupListIds();

		setContentView(R.layout.activity_search_group_list);
		init();
	}

	public void getMyGroupListIds() {
		List<TxGroup> myGroupList = TxGroup.getMyGroups(mSess.getContext());
		myGroupListids = new ArrayList<Long>();
		for (TxGroup group : myGroupList) {
			myGroupListids.add(group.group_id);
		}
	}

	private void init() {
		Intent intent = getIntent();
		type = 1;
		boolean isPublicGroup = intent.getBooleanExtra("isPublicGroup", false);
		mGroupTitle = (TextView) findViewById(R.id.group_title);
		mGroupSearchLayout = findViewById(R.id.group_search_layout);
		mContent = (EditText) findViewById(R.id.search_group_input_box);
		mGroupList = (ListView) findViewById(R.id.group_list);
		mSearch = (TextView) findViewById(R.id.find_group_btn);
		mClean = (Button) findViewById(R.id.seach_group_clean_content);
		mLoading = findViewById(R.id.group_loading);
		line_top = findViewById(R.id.line_top);

		if (!isPublicGroup) {
			mGroupTitle.setText("搜索群");
			mContent.setHint("请输入群id或关键字");
			type = 0;
		}

		LinearLayout btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				GroupSearch.this.finish();
			}
		});
		screen = (LinearLayout) findViewById(R.id.group_search_screen);

		screen.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				InputMethodManager imm = (InputMethodManager) GroupSearch.this
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
				return false;
			}
		});
		mGroupList.setOnTouchListener(this);
		mSearch.setOnClickListener(this);
		mClean.setOnClickListener(this);
		adapter = new SearchGroupIndexAdapter(mGroupList, null,
				GroupSearch.this, myGroupListids);
		mGroupList.setAdapter(adapter);
		mGroupList.setOnScrollListener(new ScrollList());
		mContent.addTextChangedListener(watcher);
		smileyParser = new SmileyParser(this);
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_GET_GROUP.equals(intent.getAction())) {
				dealgetGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_SEARCH_GROUP.equals(intent
					.getAction())) {
				dealSearchGroup(serverRsp);
			}
		}
	}

	public boolean isNumeric(String str) {
		Pattern pattern = Pattern.compile("[0-9]*");
		Matcher isNum = pattern.matcher(str);
		if (!isNum.matches()) {
			return false;
		}
		return true;
	}

	private int currentPage = 0;
	private boolean isGetOver = false;

	public void dealSearchGroup(ServerRsp serverRsp) {
		cancelDialogTimer();
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			sign = 0;
			Bundle bundle = serverRsp.getBundle();
			currentPage++;
			List<TxGroup> searchGroups = bundle
					.getParcelableArrayList(MsgHelper.GROUP_LIST);
			if (groupList.size() < 10 && currentPage != 1) {
				isGetOver = true;
			}
			if (type == 1) {
				// public
				for (TxGroup group : searchGroups) {
					if (TxGroup.isPublicGroup(group)) {
						groupList.add(group);
					}
				}
			} else {
				for (TxGroup group : searchGroups) {
					if (!TxGroup.isPublicGroup(group)) {
						groupList.add(group);
					}
				}
			}
			// groupList.addAll(searchGroups);
			groupList = TxGroup.listUniq(groupList);
			adapter.setData(groupList);
			adapter.notifyDataSetChanged();
			mLoading.setVisibility(View.GONE);
			line_top.setVisibility(View.VISIBLE);
			break;
		case GROUP_NO_EXIST:
			sign++;
			if (sign == 2) {
				Utils.startPromptDialog(GroupSearch.this, R.string.prompt,
						R.string.group_not_find);
			}
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupSearch.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case GROUP_FOR_PAGE:
			currentPage = 0;
			isGetOver = false;
			groupList.clear();
			adapter.notifyDataSetChanged();
			GroupListManager.getInstance().getSearchGroups4Server(currentPage);
			break;
		}
	}

	int cp = 1;
	int ep;
	int sign = 0;

	public void dealgetGroup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			sign = 0;
			Bundle bundle = serverRsp.getBundle();
			TxGroup txGroup = bundle.getParcelable(Utils.MSGROOM_TX_GROUP);
			if (type == 1) {
				// public
				if (TxGroup.isPublicGroup(txGroup)) {
					groupList.add(0, txGroup);
				}
			} else {
				if (!TxGroup.isPublicGroup(txGroup)) {
					groupList.add(0, txGroup);
				}
			}
			// groupList.add(0, txGroup);
			groupList = TxGroup.listUniq(groupList);
			adapter.setData(groupList);
			adapter.notifyDataSetChanged();
			break;
		case GROUP_MODIFY_GROUP_NOT_EXIST:
			sign++;
			if (sign == 2) {
				Utils.startPromptDialog(GroupSearch.this, R.string.prompt,
						R.string.group_not_find);
			}
			break;
		case OPT_FAILED:
			break;

		}
	}

	int lastPos;

	private class ScrollList implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastPos = mGroupList.getLastVisiblePosition();
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (!isGetOver && scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& lastPos == groupList.size() - 1) {
				if (mLoading.getVisibility() == View.VISIBLE) {
					line_top.setVisibility(View.GONE);
					return;
				}
				if (GroupListManager.getInstance().getSearchGroups4Server(
						currentPage)) {
					isGetOver = true;
					mLoading.setVisibility(View.GONE);
					line_top.setVisibility(View.VISIBLE);
				} else {
					mLoading.setVisibility(View.VISIBLE);
					line_top.setVisibility(View.GONE);
				}
			}
		}

	}

	// private void goMsgRoom(TxGroup txGroup){
	// int tmpSign = UtilsGroup.userDignity(TX.tm.getTxMe().partner_id,
	// txGroup.group_own_id, txGroup.group_tx_admin_ids);
	// if(txGroup.group_type_channel ==
	// TxDB.GROUP_TYPE_PUBLIC||UtilsGroup.inGroup(TX.tm.getTxMe().partner_id,
	// txGroup.group_tx_ids)||tmpSign==0||tmpSign==1){
	// showDialogTimer(GroupSearch.this, 0, R.string.group_joining, 10 *
	// 1000).show();
	// SocketHelper.getSocketHelper(txData).sendJoinQuitGroup(txGroup.group_id,"");
	//
	// }else{
	// Intent intent = new Intent(this, GroupJoin.class);
	// intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
	// startActivity(intent);
	// }
	// }
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.find_group_btn:
			sign = 0;
			groupList.clear();
			adapter.notifyDataSetChanged();
			String text = mContent.getText().toString();
			if (Utils.isNull(text)) {
				Toast.makeText(GroupSearch.this,
						R.string.find_group_hint_search_group_bar,
						Toast.LENGTH_SHORT).show();
				line_top.setVisibility(View.GONE);
				return;
			}
			if (isNumeric(text)) {
				mSess.getSocketHelper().sendGetGroup(Long.valueOf(text));
			} else {
				sign = 1;
			}
			mSess.getSocketHelper().sendSearchGroup(text, type);
			showDialogTimer(GroupSearch.this, 0, R.string.find_group_searching,
					10 * 1000).show();
			break;
		case R.id.seach_group_clean_content:
			mContent.setText("");
			mClean.setVisibility(View.GONE);
			break;
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		registReceiver();
		MobclickAgent.onResume(this);
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
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
			filter.addAction(Constants.INTENT_ACTION_GET_GROUP);
			filter.addAction(Constants.INTENT_ACTION_SEARCH_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = (ImageView) mGroupList
					.findViewWithTag("group_" + id);
			if (iv != null && result != null) {
				iv.setBackgroundDrawable(new BitmapDrawable(Utils
						.getRoundedCornerBitmap(result)));
			}

			// Log.i("fffffffff", "fffffffff___:success:"+id);
		}
	};

	private TextWatcher watcher = new TextWatcher() {

		@Override
		public void afterTextChanged(Editable s) {
			if (!s.toString().equals("")) {
				mClean.setVisibility(View.VISIBLE);
			} else {
				mClean.setVisibility(View.GONE);
			}
		}

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		@Override
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {

		}
	};

	@Override
	public boolean onTouch(View v, MotionEvent event) {

		InputMethodManager imm = (InputMethodManager) GroupSearch.this
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm.isActive()) {
			imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		}

		return false;
	}

}