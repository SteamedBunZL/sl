package com.shenliao.set.activity;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.adapter.SetUpdateAreaAdapter;
import com.shenliao.user.activity.UserInfoPerfectActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.FindConditionFriendActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.Area;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 地区选择
 * 
 * @author xch
 * 
 */
public class SetUpdateAreaActivity extends BaseActivity {
	private ListView listView;
	private List<Area> list = new ArrayList<Area>();
	private List<String> areaIdList = new ArrayList<String>();
	private LinkedList<List<Area>> viewList = new LinkedList<List<Area>>();
	private SetUpdateAreaAdapter adapter;
	private UpdateReceiver updatereceiver;
//	private SharedPreferences prefs;
//	private Editor editor;
	public static final int AREA_CHANGE_SUCCESS = 8;
	public static final int AREA_CHANGE_FAILED = 9;
	public static final int AREA_CHANGE_NOTRULE = 10;
	public static final int AREA_CHAGE_NOTCHAGE=11;
	public static final int FINDFIEND = 101;
	public static final int NOTFINDFRIEND = 100;
	public static final int PERFECTINFO = 102;
	public static final String GOINPAGE = "goinpage";
	private int goinpage = 100;
	private Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_userinfo_update_area);
		init();
	}

	private void init() {
		listView = (ListView) findViewById(R.id.sl_tab_setting_area_listView);
		back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		line = findViewById(R.id.line);
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
		adapter = new SetUpdateAreaAdapter(this);
		intent = this.getIntent();
		goinpage = intent.getIntExtra(GOINPAGE, NOTFINDFRIEND);
		list = new ArrayList<Area>(DataContainer.getAreaList());
		if(goinpage == FINDFIEND){
			list.add(0, Area.createUnlimitArea());
		}else{
			
		}
		adapter.setData(list);
		viewList.add(list);
		listView.setAdapter(adapter);
		//如果是修改个人资料，地区列表默认置底
		if(goinpage == NOTFINDFRIEND){
			listView.setSelection(list.size()-1);
		}
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View view, int pos, long rowId) {
				if(!areaIdList.contains(String.valueOf(list.get(pos-1).getId())))
					areaIdList.add(String.valueOf(list.get(pos-1).getId()));
				if (!list.get(pos-1).getAreaList().isEmpty()) {
					list = list.get(pos-1).getAreaList();
					if(goinpage == FINDFIEND){
						list = new ArrayList<Area>(list);
						list.add(0, Area.createUnlimitArea());
					}
					viewList.add(list);
					adapter.setData(list);
					adapter.notifyDataSetChanged();
					listView.setSelection(0);
				} else {

					if (goinpage == FINDFIEND) {
						Intent intent = new Intent();
						intent.putExtra("area", StringUtils.list2String(areaIdList));
						setResult(FindConditionFriendActivity.RESULTCODE_FOR_RESULT_AREA, intent);
						SetUpdateAreaActivity.this.finish();
					} else {
//						getEditorMeme().putString(CommonData.AREA, StringUtils.list2String(areaIdList)).commit();
						mSess.mPrefMeme.area.setVal(StringUtils.list2String(areaIdList)).commit();
						TX.tm.reloadTXMe();///////////
						showDialogTimer(SetUpdateAreaActivity.this, R.string.prompt, R.string.group_edit_save,
								10 * 1000).show();
						mSess.getSocketHelper().sendUserInforChange();
					}
				}

			}
		});
		
		back_left.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		
		
		
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (viewList.size() > 1) {
				viewList.removeLast();
				list = viewList.getLast();
				areaIdList.remove(areaIdList.size() -1);
				adapter.setData(list);
				adapter.notifyDataSetChanged();
				return true;
			}
		}
		return super.onKeyDown(keyCode, event);
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
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent.getAction())) {
				dealAreaChange(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealAreaChange(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null && serverRsp.getStatusCode(Tx.HOME) != null) {

				switch (serverRsp.getStatusCode(Tx.HOME)) {

				case CHANGE_AREA_SUCCESS:

					handler.sendEmptyMessage(AREA_CHANGE_SUCCESS);
					break;
				case CHANGE_AREA_FAILED:
					handler.sendEmptyMessage(AREA_CHANGE_FAILED);
  
					break;
				case CHANGE_AREA_NOTCHANGE:
					handler.sendEmptyMessage(AREA_CHAGE_NOTCHAGE);
					break;
				}
			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			switch (num) {
			case AREA_CHANGE_SUCCESS:
				if (goinpage == PERFECTINFO) {
					setResult(UserInfoPerfectActivity.RESULTCODE_FOR_RESULT_AREA);
				} else {
					setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_AREA);
				}

				finish();
				break;
			case AREA_CHANGE_FAILED:
//				prefs.edit().putString(CommonData.AREA, "").commit();
				mSess.mPrefMeme.area.setVal("").commit();
				TX.tm.reloadTXMe();///////////
				Toast.makeText(SetUpdateAreaActivity.this, "修改地区失败", 1).show();
				break;
			case AREA_CHAGE_NOTCHAGE:
				if (goinpage == PERFECTINFO) {
					setResult(UserInfoPerfectActivity.RESULTCODE_FOR_RESULT_AREA);
				} else {
					setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_AREA);
				}

				finish();
				break;
			}
			super.handleMessage(msg);
		}
	};
	private LinearLayout back_left;
	private View line;

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
