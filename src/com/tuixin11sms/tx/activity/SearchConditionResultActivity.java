package com.tuixin11sms.tx.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.shenliao.data.DataContainer;
import com.shenliao.search.adapter.SearchResultListViewAapter;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.shenliao.view.SLRetiveLayout;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 搜索返回结果 例如：看看谁再聊
 * 
 * @author xch
 * 
 */
public class SearchConditionResultActivity extends BaseActivity {
	public static final String GRIDLIST = "gridlist";
	private List<String> list = new ArrayList<String>();
	private Intent intent;
	private ListView listView;
	private TextView tileName;// 标题名字
	private SearchResultListViewAapter searchAdapter;
	private UpdateReceiver updatereceiver;
	private List<TX> resultList = new ArrayList<TX>();
	private View mLoading;
	private TX tx;
	private LinearLayout noGroup;
	private boolean isGetOver = false;
	public static final String ISSEARCHONLINE = "issearchonline";
	public int goInPageState = 100;
	public static final int ISSEAERCHONLINE = 99; // 看看
	public static int oldeadline = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_add_condition_result);
		init();
		if (list != null && list.size() > 0) {
			show();
		}
	}

	private void init() {
		intent = this.getIntent();
		tx = intent.getParcelableExtra("tx");
		goInPageState = intent.getIntExtra("goinpage", 100);
		if (tx == null) {
			tx = new TX();
			tx.setOnLine(1);
			tx.setSex(2);
			tx.setBloodType(4);
			tx.setAge(0);
			tx.setConstellation(String.valueOf(0));

		}

		showDialogTimer(SearchConditionResultActivity.this, R.string.prompt,
				R.string.search_result, 15 * 1000, new BaseTimerTask() {

					@Override
					public void run() {
						super.run();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								AlertDialog.Builder promptDialog = new AlertDialog.Builder(
										SearchConditionResultActivity.this);
								promptDialog.setTitle(R.string.prompt);
								promptDialog.setMessage("搜索超时，请重试");
								promptDialog.setCancelable(false);
								promptDialog.setNegativeButton(
										R.string.confirm,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();
												SearchConditionResultActivity.this
														.finish();
											}
										});
								promptDialog.show();

							}
						});

					}

				}).show();
		mSess.getSocketHelper().sendSearchUser(tx, oldeadline);
		tileName = (TextView) findViewById(R.id.con_info_title_name);

		mLoading = findViewById(R.id.group_loading);
		noGroup = (LinearLayout) findViewById(R.id.list_hint_empty_ll);
		linear = (LinearLayout) findViewById(R.id.linear);
		fugai = (ImageView) findViewById(R.id.list_hint_fugai);
		listView = (ListView) findViewById(R.id.search_add_condition_result_listView);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		if (goInPageState == ISSEAERCHONLINE) {
			tileName.setText("看看谁在聊");
			fugai.setVisibility(View.VISIBLE);
		} else {
			tileName.setText("搜索结果");
		}
		list = intent.getStringArrayListExtra(GRIDLIST);
		searchAdapter = new SearchResultListViewAapter(
				SearchConditionResultActivity.this, noGroup, linear, mSess);
		searchAdapter.setData(DataContainer.getSearchList());

		listView.setAdapter(searchAdapter);
		listView.setOnScrollListener(new ScrollList());
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// int currentposition = searchAdapter.getNewPosition(arg2);
				if (DataContainer.getSearchList().get(arg2).partner_id == TX.tm
						.getTxMe().partner_id) {
					Intent intentMine = new Intent(
							SearchConditionResultActivity.this,
							SetUserInfoActivity.class);
					startActivity(intentMine);
				} else {
					TX ttx = DataContainer.getSearchList().get(arg2);
					// if (TX.tm.isTxFriend(ttx.partner_id)) {
					// ttx.setTx_type(TxDB.TX_TYPE_TB);
					// } else {
					// ttx.setTx_type(TxDB.TX_TYPE_ST);
					// }
					// ttx.setStarFriend(TX.tm.getStarFriendAttr(tx.partner_id));
					Intent iSupplement = new Intent(
							SearchConditionResultActivity.this,
							UserInformationActivity.class);
					iSupplement.putExtra(
							UserInformationActivity.pblicInfo,
							TX.tm.isTxFriend(DataContainer.getSearchList().get(
									arg2).partner_id) == true ? UserInformationActivity.TUIXIN_USER_INFO
									: UserInformationActivity.NOT_TUIXIN_USER_INFO);
					iSupplement.putExtra(UserInformationActivity.UID,
							ttx.partner_id);
					startActivity(iSupplement);
				}
			}
		});

		btn_back_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				SearchConditionResultActivity.this.finish();
			}
		});
	}

	// 显示数据
	private void show() {
		LinearLayout layout = (LinearLayout) findViewById(R.id.search_add_condition_linear);
		SLRetiveLayout newRel = new SLRetiveLayout(this);
		newRel.setLayoutParams((new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT)));
		newRel.setMinimumHeight(44);
		newRel.setVerticalGravity(Gravity.DISPLAY_CLIP_VERTICAL);

		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).equals("男")) {
				TextView textView = new TextView(this);
				textView.setLayoutParams(new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT));
				textView.setBackgroundResource(R.drawable.sll_search_result_type_boy);
				textView.setText("男");
				textView.setPadding(7, 4, 7, 5);
				textView.setTextSize(12);
				textView.setTextColor(getResources().getColor(
						R.color.content_color_blue));
				newRel.addView(textView);
			} else if (list.get(i).equals("女")) {
				TextView textView = new TextView(this);
				textView.setLayoutParams(new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT));
				textView.setBackgroundResource(R.drawable.sll_search_result_type_girl);
				textView.setText("女");
				textView.setPadding(7, 4, 7, 5);
				textView.setTextSize(12);
				textView.setTextColor(Color.parseColor("#ff5cad"));
				newRel.addView(textView);
			} else {
				TextView textView = new TextView(this);
				textView.setLayoutParams(new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT,
						RelativeLayout.LayoutParams.WRAP_CONTENT));
				textView.setBackgroundResource(R.drawable.sll_search_result_type);
				textView.setText(list.get(i));
				textView.setPadding(7, 4, 7, 5);
				textView.setTextSize(12);
				textView.setTextColor(Color.parseColor("#0c2049"));
				newRel.addView(textView);
			}
		}
		if (list.size() > 0) {
			layout.setVisibility(View.VISIBLE);
			layout.addView(newRel);
		}

	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_SEARCH_USER_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SEARCH_USER_RSP.equals(intent
					.getAction())) {
				dealSearchResult(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealSearchResult(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null && serverRsp.getStatusCode() != null) {

				switch (serverRsp.getStatusCode()) {

				case RSP_OK:
					searchAdapter.setData(DataContainer.getSearchList());
					searchAdapter.notifyDataSetChanged();
					isGetOver = serverRsp.getBoolean("isEnd", false);
					mLoading.setVisibility(View.GONE);
					break;
				case OPT_FAILED:
					Utils.startPromptDialog(SearchConditionResultActivity.this,
							R.string.prompt, R.string.opt_failed);
					break;

				}
			}

		}
	}

	int lastPos;

	private class ScrollList implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastPos = view.getLastVisiblePosition();
		}

		// public int getNum() {
		// if (DataContainer.getSearchList().size() - 1 <= 9) {
		// return 0;
		// } else {
		// return (searchAdapter.currposition - 10) / 11 + 1;
		// }
		// }

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			int tempTotal = 0;
			// tempTotal = DataContainer.getSearchList().size() - 1 + getNum();
			tempTotal = DataContainer.getSearchList().size() - 1;
			if (Utils.debug) {
				Log.i("Zzl1", "tempTotal : " + tempTotal);
			}
			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& lastPos == tempTotal) {
				if (mLoading.getVisibility() == View.VISIBLE)
					return;
				if (!isGetOver) {
					mLoading.setVisibility(View.VISIBLE);
					mSess.getSocketHelper().sendSearchUser(tx, oldeadline);
				} else {
					mLoading.setVisibility(View.GONE);
				}

			}
		}

	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			// switch (num) {
			// case FAVOURITE_CHANGE_SUCCESS:
			// Intent intentresult = new Intent();
			// setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_FAVOURITE,
			// intentresult);
			// SetUpdateFavouriteActivity.this.finish();
			// break;
			// case FAVOURITE_CHANGE_FAILED:
			// prefs.edit().putString(CommonData.HOBBY, "").commit();
			// Toast.makeText(SetUpdateFavouriteActivity.this, "修改兴趣爱好失败",
			// Toast.LENGTH_LONG).show();
			// break;
			// }
			super.handleMessage(msg);
		}
	};
	private LinearLayout btn_back_left;
	private ImageView fugai;
	private LinearLayout linear;

	@Override
	protected void onResume() {
		registReceiver();
		// if (Utils.idNotInvalid(tx.partner_id)) {
		// TX tx1 = TX.findTXByPartnerID4DB(tx.partner_id,
		// getContentResolver());
		// if (tx1 != null && searchAdapter != null) {
		// searchAdapter.updateListInfo(tx1);
		// searchAdapter.notifyDataSetChanged();
		// }
		// }
		//

		super.onResume();
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
		DataContainer.clearSearchUserList();
		super.onDestroy();
	}

}
