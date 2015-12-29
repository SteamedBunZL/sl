package com.shenliao.set.activity;

import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.adapter.SetUpdateLanguageAdapter;
import com.shenliao.set.adapter.SetUpdateLanguageAdapter.Holder;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.FindConditionFriendActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.Language;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 语言
 * 
 * @author xch
 * 
 */
public class SetUpdateLanguageActivity extends BaseActivity implements
		OnClickListener {
	private ListView listView;
	private SetUpdateLanguageAdapter adapter;
	private List<String> list = new ArrayList<String>();
	private List<String> selectList = new ArrayList<String>();
	private List<Language> langList = new ArrayList<Language>();
	private TextView sendBtn;
	// private SharedPreferences prefs;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private static final int LANGUAGE_CHANGE_SUCCESS = 1;
	private static final int LANGUAGE_CHANGE_FAILED = 2;
	private static final int LANGUAGE_CHANGE_NOTCHANGE = 3;
	public static final String GOINPAGE = "goinpage";
	public static final int FINDFIEND = 101;
	public static final int NOTFINDFRIEND = 100;
	private Intent intent;
	private int goinpage = 100;
	private int limitNum = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_update_language);
		init();
	}

	// 初始化
	private void init() {
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		sendBtn = (TextView) findViewById(R.id.sl_tab_setting_language_sendBtn);
		back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		listView = (ListView) findViewById(R.id.sl_tab_setting_language_listView);
		listView.setDivider(null);
		sendBtn.setOnClickListener(this);
		back_left.setOnClickListener(this);
		intent = this.getIntent();
		goinpage = intent.getIntExtra(GOINPAGE, NOTFINDFRIEND);
		//
		if (goinpage != FINDFIEND) {
			// List<String> mlist =
			// StringUtils.str2List(prefs.getString(CommonData.LANGUAGES, ""));
			List<String> mlist = StringUtils.str2List(mSess.mPrefMeme.languages
					.getVal());

			selectList = new ArrayList<String>(mlist);
		}

		langList = new ArrayList<Language>(DataContainer.getLangList());
		if (goinpage == FINDFIEND) {
			sendBtn.setVisibility(View.GONE);
			langList.add(0, Language.createUnlimitLang());
		}

		adapter = new SetUpdateLanguageAdapter(SetUpdateLanguageActivity.this);
		adapter.setSelectData(selectList);
		adapter.setData(langList);
		listView.setAdapter(adapter);

		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view,
					int positon, long arg3) {
				Holder holder = (Holder) view.getTag();
				if (goinpage == FINDFIEND) {
					limitNum = 1;
				} else if (goinpage == NOTFINDFRIEND) {
					limitNum = 3;
				}

				if (selectList.contains(String.valueOf(langList
						.get(positon - 1).getId()))) {
					selectList.remove(String.valueOf(langList.get(positon - 1)
							.getId()));
					holder.imageView.setBackgroundDrawable(null);
					holder.textView.setTextColor(SetUpdateLanguageActivity.this
							.getResources().getColor(
									R.color.searchtext_default_color));

				} else {
					if(selectList.contains("0")){
						selectList.remove("0");
					}
					if (selectList.size() < limitNum) {
						selectList.add(String.valueOf(langList.get(positon - 1)
								.getId()));
						holder.imageView
								.setBackgroundResource(R.drawable.right_ok_lan);
						holder.textView
								.setTextColor(SetUpdateLanguageActivity.this
										.getResources().getColor(
												R.color.select_lan));

					} else {
						Toast.makeText(SetUpdateLanguageActivity.this,
								"最多可以选择" + limitNum + "种语言", Toast.LENGTH_SHORT)
								.show();
					}
				}

				if (goinpage == FINDFIEND) {
					Intent intentresult = new Intent();
					intentresult.putExtra("language",
							StringUtils.list2String(selectList));
					setResult(
							FindConditionFriendActivity.RESULTCODE_FOR_RESULT_LANGUAGE_FIND,
							intentresult);
					SetUpdateLanguageActivity.this.finish();
				}
				adapter.setSelectData(selectList);

			}
		});

	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_language_sendBtn:
			// if (goinpage == FINDFIEND) {
			// Intent intentresult = new Intent();
			// // intentresult.putParcelableArrayListExtra(name, value)
			// setResult(FindConditionFriendActivity.RESULTCODE_FOR_RESULT_LANGUAGE_FIND,
			// intentresult);
			// SetUpdateLanguageActivity.this.finish();
			// } else if (goinpage == NOTFINDFRIEND) {
			showDialogTimer(SetUpdateLanguageActivity.this, R.string.prompt,
					R.string.group_edit_save, 10 * 1000).show();
			// getEditorMeme().putString(CommonData.LANGUAGES,
			// StringUtils.list2String(selectList)).commit();

			if (selectList.contains(0)) {
				selectList.remove(0);
			}
			if (StringUtils.list2String(selectList) != null
					&& !StringUtils.list2String(selectList).equals("")) {
				mSess.mPrefMeme.languages.setVal(
						StringUtils.list2String(selectList)).commit();
				
			} else {
				mSess.mPrefMeme.languages.setVal("0").commit();
			}
			TX.tm.reloadTXMe();// ///////
			mSess.getSocketHelper().sendUserInforChange();
			// }

			break;
		case R.id.btn_back_left:
			finish();
			break;

		default:
			break;
		}

	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_CHANGE_USERNAME_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent
					.getAction())) {
				dealLanguageChange(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealLanguageChange(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null
					&& serverRsp.getStatusCode(Tx.LANGUAGES) != null) {

				switch (serverRsp.getStatusCode(Tx.LANGUAGES)) {

				case CHANGE_LANG_SUCCESS:

					handler.sendEmptyMessage(LANGUAGE_CHANGE_SUCCESS);
					break;
				case CHANGE_LANG_FAILED:
					// String fbret = serverRsp.getBundle().getString("fbret");
					// Message message =
					// handler.obtainMessage(LANGUAGE_CHANGE_FAILED, fbret);
					handler.sendEmptyMessage(LANGUAGE_CHANGE_FAILED);

					break;
				case CHANGE_LANG_NOTCHANGE:
					handler.sendEmptyMessage(LANGUAGE_CHANGE_NOTCHANGE);
					break;
				}
			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			switch (num) {
			case LANGUAGE_CHANGE_SUCCESS:
				// prefs.edit().putString(CommonData.LANGUAGES,
				// StringUtils.list2String(selectList)).commit();
				Intent intentresult = new Intent();
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_LANGUAGE,
						intentresult);
				SetUpdateLanguageActivity.this.finish();
				break;
			case LANGUAGE_CHANGE_FAILED:
				// prefs.edit().putString(CommonData.LANGUAGES, "").commit();
				mSess.mPrefMeme.languages.setVal("").commit();
				TX.tm.reloadTXMe();// /////
				Toast.makeText(SetUpdateLanguageActivity.this, "修改语言失败",
						Toast.LENGTH_LONG).show();
				break;
			case LANGUAGE_CHANGE_NOTCHANGE:
				setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_LANGUAGE);
				SetUpdateLanguageActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};
	private LinearLayout back_left;

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}
}
