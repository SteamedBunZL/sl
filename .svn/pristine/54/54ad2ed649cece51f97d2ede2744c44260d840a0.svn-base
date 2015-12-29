package com.shenliao.group.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ListView;

import com.shenliao.group.adapter.GroupIndexAdapter;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/** 我的聊天室页面 */
public class GroupMine extends BaseActivity {
	private static final String TAG = "GroupMine";
	private ListView mListView;
	private GroupIndexAdapter adapter;
	private List<TxGroup> groups = new ArrayList<TxGroup>();
	private List<MsgStat> msgStatsList = new ArrayList<MsgStat>();
	private UpdateReceiver updatareceiver;
//	public static boolean CHANGE = false;//只有赋值，没有引用 注掉 2014.03.04
	private View noGroup;
//	protected TxData txData;
	private static final int SHOW_MSGSTAT = 100;
	private HashMap<Long, Integer> msgStats = new HashMap<Long, Integer>();// 对话数据
																			// key：groupid
																			// value：unreadcount

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_index_myroom);
		init();
		registReceiver();
	}

	private void init() {
//		txData = (TxData) getApplication();
		mListView = (ListView) findViewById(R.id.group_index_listView);
		noGroup = findViewById(R.id.list_hint_empty);
		adapter = new GroupIndexAdapter(thisContext, mListView, noGroup,
				GroupMine.this);
		adapter.setData(new ArrayList<TxGroup>());
		mListView.setAdapter(adapter);
		msgStatflush();
		mAsyncTask.execute();
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_SEARCH_USER);
			filter.addAction(Constants.INTENT_ACTION_JOIN_GROUP_2018);
			filter.addAction(Constants.INTENT_ACTION_FLUSH_GROUP);
			filter.addAction(Constants.INTENT_ACTION_FLUSH);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SEARCH_USER.equals(intent.getAction())) {
				if(Utils.debug)Log.i(TAG, "收到的广播INTENT_ACTION_SEARCH_USER，View刷新");
				dealMyGroups(serverRsp);
			} else if (Constants.INTENT_ACTION_JOIN_GROUP_2018.equals(intent
					.getAction())) {
				dealJoinGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_FLUSH_GROUP.equals(intent
					.getAction())) {
				if(Utils.debug)Log.i(TAG, "收到广播INTENT_ACTION_FLUSH_GROUP，View刷新");
				msgStatflush();
			} else if (Constants.INTENT_ACTION_FLUSH.equals(intent.getAction())) {
				if(Utils.debug)Log.i(TAG, "收到了了广播INTENT_ACTION_FLUSH，View刷新");
				msgStatflush();
			}
		}
	}

	public void dealMyGroups(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			msgStatflush();
			break;
		case OPT_FAILED:
			break;
		default:
			break;
		}
	}

	Runnable mStateFlush = new Runnable() {
		volatile boolean bUpdating = false;
		Runnable mRun1 = new Runnable() {


			public void run() {
				groups = TxGroup.getMyGroupsByUnreadCount(GroupMine.this);
				
				List<TxGroup> my_pub_group = new ArrayList<TxGroup>();
				if(groups!= null && groups.size()>0){
					for(TxGroup group : groups){
						if(group.group_type_channel == TxDB.GROUP_TYPE_PUBLIC){
							my_pub_group.add(group);
						}
					}
				}
				groups = my_pub_group;
				if(Utils.debug)Log.i(TAG, "聊天室个数："+groups.size());
				
//				if (groups != null && groups.size() > 0) {//如果聊天室都被删除完毕了，不发送更新消息，则最后一个聊天室还会显示
					handler.sendEmptyMessage(SHOW_MSGSTAT);
//				}
				bUpdating = false;
			}
		};

		@Override
		public void run() {

			if (!bUpdating) {

				bUpdating = true;

				Utils.executorService.submit(mRun1);

			}
		}

	};

	public void msgStatflush() {
		handler.removeCallbacks(mStateFlush);
		handler.postDelayed(mStateFlush, 500);
		// Message.obtain(handler, SHOW_MSGSTAT).sendToTarget();
	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case SHOW_MSGSTAT:
				msgStatsList = new ArrayList<MsgStat>(MsgStat.getMsgStatsList());
				if (msgStatsList != null && msgStatsList.size() > 0) {
					for (MsgStat ms : msgStatsList) {
						if (ms.group_id > 0) {
							msgStats.put(ms.getGroupID(), ms.getNoRead());
						}
					}

				}
				adapter.setData(groups);
				if(Utils.debug)Log.i(TAG, "要显示前，总共有的grups个数："+groups.size());
				adapter.setMsgStat(msgStats);
				adapter.notifyDataSetChanged();
				break;
			}
		}

	};

	public void dealJoinGroup(ServerRsp serverRsp) {
		//

	}

	@Override
	protected void onStop() {
		if (mAsyncTask != null) {
			mAsyncTask.cancel(true);

		}
		super.onStop();
	}

	private AsyncTask<Object, Object, Object> mAsyncTask = new AsyncTask<Object, Object, Object>() {

		@Override
		protected Object doInBackground(Object... params) {
			if (groups.size() == 0) {
				mSess.getSocketHelper().sendUserQun(
						TX.tm.getTxMe().partner_id);
			} else {
				for (int i = 0; i < groups.size(); i++) {
					TxGroup tg = groups.get(i);
					ArrayList<TXMessage> msglist = TXMessage
							.filterGroupMessageList(getContentResolver(),
									tg.group_id, 0);
					if (msglist.size() > 0) {
//						TXMessage msg = msglist.get(0);
						mSess.getSocketHelper()
								.sendGetGroupOfflineMsg(tg.group_id, "", 1);
						mSess.getSocketHelper()
								.sendGetGroupMessageIds(tg.group_id, "", 11);
					}
				}
			}
			return null;

		}

	};

}
