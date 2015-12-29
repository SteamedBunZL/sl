package com.shenliao.group.activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.ListView;

import com.shenliao.group.adapter.GroupIndexAdapter;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 最近访问群组
 * @author webster
 *
 */
public class GroupVisited extends BaseActivity {
	private static final String TAG = GroupVisited.class.getSimpleName();
	private static final int REFRESH_PAGE = 100;
	private ListView mListView;
	private GroupIndexAdapter adapter;
	private List<TxGroup> groups = new ArrayList<TxGroup>();
	private RefreshReceiver refreshReceiver;
	private Handler handler = new GroupVisitedHandler(this);
//	protected TxData txData;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_index_myroom);
		init();
		registReceiver();
		getVisitedGroups(0);
	}

	private void init() {
//		txData = (TxData) getApplication();
		mListView = (ListView) findViewById(R.id.group_index_listView);
		adapter = new GroupIndexAdapter(thisContext, mListView, null, GroupVisited.this);
		mListView.setAdapter(adapter);
	}
	/**
	 * 获取最近访问群组信息
	 * @param delayMillis 指定延时通知页面刷新时间
	 */
	private void getVisitedGroups(final long delayMillis) {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				groups = TxGroup.getGroupsByAccessTime(GroupVisited.this);
				
				List<TxGroup> lastGroup = new ArrayList<TxGroup>();
				if(groups != null && groups.size()>0){
					for(TxGroup group : groups){
						if(TxGroup.isPublicGroup(group)){
							lastGroup.add(group);
						}else{
							if(group.group_type_channel == 1){
								lastGroup.add(group);
							}
						}
					}
				}
				
				groups = lastGroup;
				handler.sendEmptyMessageDelayed(REFRESH_PAGE, delayMillis);
			}
		});
	}

	private void registReceiver() {
		if (refreshReceiver == null) {
			refreshReceiver = new RefreshReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_FLUSH_GROUP);
			this.registerReceiver(refreshReceiver, filter);
		}
	}

	private static class GroupVisitedHandler extends Handler {
		WeakReference<GroupVisited> gv;
		GroupVisitedHandler(GroupVisited gv){
			this.gv = new WeakReference<GroupVisited>(gv);
		}
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case REFRESH_PAGE: {
				gv.get().notifyDataChanged();
				
				break;
			}
			}
		}
	}
	
	private void notifyDataChanged(){
		adapter.setData(groups);
		adapter.notifyDataSetChanged();
	}
	
	public class RefreshReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			dealflushMsgState();
		}
	}

	public void dealflushMsgState() {
		handler.removeMessages(REFRESH_PAGE);
		getVisitedGroups(500);

	}
}
