package com.shenliao.set.activity;

import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;

import com.shenliao.set.adapter.SetBlackManageAdapter;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 黑名单管理
 * 
 * @author xch
 */
public class SetBlackManageActivity extends BaseActivity {
	private ListView listView;// 显示黑名单列表listview
	private SetBlackManageAdapter blackManageAdapter;// listview适配器
	private View mLoading;// 加载更多view
	private UpdateReceiver updatereceiver;
	private boolean isGetOver = false;
	private static final int REMOVE_CHANGE_SUCCESS = 0;
	private static final int REMOVE_CHANGE_FAILED = 1;
	private List<TX> blackList = null;//无引用，删掉 2014.01.21
//	private List<TX> tList = new ArrayList<TX>();//无引用，删掉 2014.01.21
//	private ContentResolver cr;//无引用，删掉 2014.01.21

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_set_blackmanage);
		
		View v_back_btn = findViewById(R.id.btn_back_left);
		
		v_back_btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 返回按钮
				thisContext.finish();
				
			}
		});
		init();
	}

	// 初始化
	private void init() {

//		cr = this.getContentResolver();
		showDialogTimer(SetBlackManageActivity.this, R.string.prompt,
				R.string.black_member, 15 * 1000, new BaseTimerTask() {

					@Override
					public void run() {
						super.run();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								AlertDialog.Builder promptDialog = new AlertDialog.Builder(
										SetBlackManageActivity.this);
								promptDialog.setTitle(R.string.prompt);
								promptDialog.setMessage("获取黑名单列表超时，请重试");
								promptDialog.setCancelable(false);
								promptDialog.setNegativeButton(
										R.string.confirm,
										new DialogInterface.OnClickListener() {
											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {
												dialog.cancel();
												SetBlackManageActivity.this
														.finish();
											}
										});
								promptDialog.show();

							}
						});

					}

				}).show();
		mSess.getSocketHelper().sendGetBlackList(0);
		mLoading = findViewById(R.id.black_loading);
		listView = (ListView) findViewById(R.id.sl_tab_setting_black_listView);
		blackManageAdapter = new SetBlackManageAdapter(mSess);
//		blackManageAdapter.setData(DataContainer.getBlackList());//用TX管理黑名单吧，2014.01.21 shc
		blackList = TX.tm.getBlackTXList();
		blackManageAdapter.setData(blackList);
		
		listView.setAdapter(blackManageAdapter);
		listView.setOnItemClickListener(myOnItemClickListener);
		listView.setOnScrollListener(new ScrollList());

	}
	
	
	@Override
	protected void onRestart() {
		blackList = TX.tm.getBlackTXList();
		blackManageAdapter.setData(blackList);
		blackManageAdapter.notifyDataSetChanged();
		super.onRestart();
	}
	
	
	
	// listview单击事件
	OnItemClickListener myOnItemClickListener = new OnItemClickListener() {

		@Override
		public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
				long arg3) {
			showDialog(R.string.black_dialog_title,
					R.array.black_dialog_items_all, arg2);
		}

	};

	// dialog
	private void showDialog(int title, int id, final int postion) {
		new AlertDialog.Builder(SetBlackManageActivity.this).setTitle(title)
				.setItems(id, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
						// 查看个人资料
						case 0:
							Intent iSupplement = new Intent(
									SetBlackManageActivity.this,
									UserInformationActivity.class);
							//用TX管理黑名单吧，2014.01.21 shc
//							iSupplement.putExtra(
//									UserInformationActivity.pblicInfo,
//									TX.tm.isTxFriend(DataContainer.getBlackList().get(postion).partner_id) == true ? UserInformationActivity.TUIXIN_USER_INFO
//											: UserInformationActivity.NOT_TUIXIN_USER_INFO);
//							iSupplement.putExtra(
//									UserInformationActivity.infoTX,
//									DataContainer.getBlackList().get(postion));
							iSupplement.putExtra(
									UserInformationActivity.pblicInfo,
									TX.tm.isTxFriend(blackList.get(postion).partner_id) == true ? UserInformationActivity.TUIXIN_USER_INFO
											: UserInformationActivity.NOT_TUIXIN_USER_INFO);
							iSupplement.putExtra(
									UserInformationActivity.UID,
									blackList.get(postion).partner_id);
							startActivity(iSupplement);
							dialog.cancel();
							break;
						// 移除黑名单
						case 1:
							//开启一个计时器，15秒后显示超时
							showDialogTimer(SetBlackManageActivity.this,
									R.string.prompt, R.string.black_result,
									15 * 1000, new BaseTimerTask() {

										@Override
										public void run() {
											super.run();
											runOnUiThread(new Runnable() {

												@Override
												public void run() {
													AlertDialog.Builder promptDialog = new AlertDialog.Builder(
															SetBlackManageActivity.this);
													promptDialog
															.setTitle(R.string.prompt);
													promptDialog
															.setMessage("移除黑名单超时，请重试");
													promptDialog
															.setCancelable(false);
													promptDialog
															.setNegativeButton(
																	R.string.confirm,
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			dialog.cancel();
																			SetBlackManageActivity.this
																					.finish();
																		}
																	});
													promptDialog.show();

												}
											});

										}

									}).show();
//							TX currentTx = DataContainer.getBlackList().get(postion);
							TX currentTx = blackList.get(postion);
							ContentValues values = new ContentValues();
//							currentTx.setIn_black_list(TxDB.TX_NOT_IN_BLACK_LIST);
//							values.put(TxDB.Tx.IN_BLACK_LIST, TxDB.TX_NOT_IN_BLACK_LIST);
							
							TX.tm.changeTxToST(currentTx.partner_id);
							
							
							mSess.getSocketHelper().sendRmvBlackList(currentTx.partner_id);
							blackList.remove(postion);
//							DataContainer.removeBlackUser(currentTx);//用TX管理黑名单吧，2014.01.21 shc
							blackManageAdapter.setData(blackList);
							blackManageAdapter.notifyDataSetChanged();
							break;
						}
					}
				}).create().show();
	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_GET_BLACKlIST_RSP);
			filter.addAction(Constants.INTENT_ACTION_OPT_BLACKLIST_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_GET_BLACKlIST_RSP.equals(intent
					.getAction())) {
				dealBlackResult(serverRsp);
			} else if (Constants.INTENT_ACTION_OPT_BLACKLIST_RSP.equals(intent
					.getAction())) {
				dealRemoveResult(serverRsp);
			}
		}

		// 处理移除黑名单
		private void dealRemoveResult(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null && serverRsp.getStatusCode() != null) {

				switch (serverRsp.getStatusCode()) {

				case RSP_OK:
					int type = serverRsp.getInt("type", -1);

					handler.obtainMessage(REMOVE_CHANGE_SUCCESS, type)
							.sendToTarget();
					break;
				case OPT_FAILED:
					handler.sendEmptyMessage(REMOVE_CHANGE_FAILED);
					break;

				}
			}

		}

		// 处理获取黑名单回结果方法
		private void dealBlackResult(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null && serverRsp.getStatusCode() != null) {

				switch (serverRsp.getStatusCode()) {

				case RSP_OK:
					//重新获取黑名单list集合，更新adapter
//					blackList = TX.tm.getBlackTXList();
					
					Bundle bundle = serverRsp.getBundle();
//					blackList = bundle.getParcelableArrayList(MsgHelper.USER_LIST);
					
//					if(blackList == null ){
						blackList = TX.tm.getBlackTXList();
//					}
					
					blackManageAdapter.setData(blackList);
					blackManageAdapter.notifyDataSetChanged();
					isGetOver = serverRsp.getBoolean("isEnd", false);
					mLoading.setVisibility(View.GONE);
					break;
				case OPT_FAILED:
					Utils.startPromptDialog(SetBlackManageActivity.this,
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

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			int tempTotal = 0;

			if (scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& lastPos == tempTotal) {
				if (mLoading.getVisibility() == View.VISIBLE)
					return;
				if (!isGetOver) {
					mLoading.setVisibility(View.VISIBLE);
					// SocketHelper.getSocketHelper(txData).sendSearchUser(tx);
				} else {
					mLoading.setVisibility(View.GONE);

				}

			}
		}

	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			switch (num) {
			case REMOVE_CHANGE_SUCCESS:
				if ((Integer) msg.obj == 1) {
					Toast.makeText(SetBlackManageActivity.this, "移除黑名单成功",
							Toast.LENGTH_LONG).show();
				}

				break;
			case REMOVE_CHANGE_FAILED:
				Toast.makeText(SetBlackManageActivity.this, "移除黑名单失败",
						Toast.LENGTH_LONG).show();
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onResume() {
		registReceiver();
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
//		DataContainer.clearBlackList();//用TX管理黑名单吧，2014.01.21 shc
		super.onDestroy();
	}

}
