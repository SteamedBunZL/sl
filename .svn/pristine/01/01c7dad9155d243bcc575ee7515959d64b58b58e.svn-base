package com.shenliao.group.adapter;

import java.io.File;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupJoin;
import com.shenliao.group.util.GroupHolder;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Utils;

public class SearchGroupIndexAdapter extends BaseAdapter {
	protected static final String TAG = "GroupIndexAdapter";
//	private TxData txData;
	private LayoutInflater inflater;
	private List<TxGroup> groupList = new ArrayList<TxGroup>();
	private SmileyParser smileyParser;
	private ListView listView;
	private View noData;
	private List<Long> myGroupList;
	private HashMap<Long, Integer> msgStats = new HashMap<Long, Integer>();// 对话数据
																			// key：groupid
																			// value：unreadcount
	private Context context;

	public SearchGroupIndexAdapter(ListView listView,
			View noData, Context mActivity,List<Long> myGroupList) {
		context = mActivity;
//		this.txData = txData;
		this.listView = listView;
		this.noData = noData;
		this.myGroupList = myGroupList;
		prepairAsyncload();
		inflater = LayoutInflater.from(context);
		smileyParser = new SmileyParser(context);
	}

	public void setMsgStat(HashMap<Long, Integer> msgStats) {
		this.msgStats = msgStats;
	}

	public void setData(List<TxGroup> groupList) {
		this.groupList = groupList;

		if (groupList.size() == 0) {
			this.listView.setVisibility(View.GONE);
			if (noData != null) {
				this.noData.setVisibility(View.VISIBLE);
			}

		} else {
			this.listView.setVisibility(View.VISIBLE);
			if (noData != null) {
				this.noData.setVisibility(View.GONE);
			}
		}
	}

	@Override
	public int getCount() {
		int size = groupList.size();
		return size;
	}

	@Override
	public Object getItem(int arg0) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		GroupHolder viewHolder;
		convertView = inflater.inflate(
				R.layout.sll_searchgroup_index_listview_items, null);
		viewHolder = new GroupHolder();
		viewHolder.avatar = (ImageView) convertView
				.findViewById(R.id.group_index_listView_item_photo);
		viewHolder.groupName = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_GroupName);
		viewHolder.introl = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_notice);
		viewHolder.unReadCount = (TextView) convertView
				.findViewById(R.id.sl_unread_count);
		viewHolder.memberNum = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_onlineMember);
		// viewHolder.Limage = (ImageView) convertView
		// .findViewById(R.id.group_index_listView_item_publicImageView);
		viewHolder.Gimage = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_gfImageView);
		viewHolder.Pbimage = (ImageView) convertView
				.findViewById(R.id.group_rcv_msg);
		viewHolder.manager = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_manager);
		viewHolder.managername = (TextView) convertView
				.findViewById(R.id.group_index_listView_item_manager_name);
		final TxGroup txGroup = groupList.get(position);
		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL) {
			viewHolder.Gimage.setVisibility(View.VISIBLE);
			viewHolder.manager.setText("管理");

			String names = txGroup.group_tx_admin_names;
			String replace = null;
			if (names.contains("�")) {
				replace = names.replace("�", ",");
				if (Utils.debug)
					Log.i(TAG, replace);
				if (replace.endsWith(",")) {
					replace = replace.substring(0, replace.length() - 1);
				}
			} else {
				replace = names;
			}
			viewHolder.managername.setText(replace);
			// viewHolder.Limage
			// .setImageResource(R.drawable.sl_group_index_chaneltype_liao);
		} else if (txGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC) {
			SmileyParser smileyParser = new SmileyParser(context);

			viewHolder.managername.setText(smileyParser.addSmileySpans(
					txGroup.group_own_name, true, 0));
			// viewHolder.Limage
			// .setImageResource(R.drawable.sl_group_index_chaneltype_liao);
		} else if (txGroup.group_type_channel == TxDB.GROUP_TYPE_SECRET) {
			// viewHolder.Limage
			// .setImageResource(R.drawable.sl_group_index_chanel_secret);

		} else {
			// viewHolder.Limage
			// .setImageResource(R.drawable.sl_group_index_chaneltype_si);

		}
		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_REQUEST
				|| txGroup.group_type_channel == TxDB.GROUP_TYPE_SECRET) {
			if (txGroup.rcv_msg == 0) {
				viewHolder.Pbimage
						.setImageResource(R.drawable.sl_group_index_pingbi);
			}
		}

		String name = Utils.isNull(txGroup.group_title) ? "" + txGroup.group_id
				: txGroup.group_title;
		viewHolder.groupName
				.setText(smileyParser.addSmileySpans(name, true, 0));
		viewHolder.avatar.setTag("group_" + txGroup.group_id);
		viewHolder.introl.setText(smileyParser.addSmileySpans(
				txGroup.group_sign, true, 0));
		if (TxGroup.isPublicGroup(txGroup)) {
			viewHolder.memberNum.setText("" + txGroup.group_ol_num);
		} else {
			viewHolder.memberNum.setText("" + txGroup.group_all_num);
		}
		// TxGroup temp2 = TxGroup.findGroupById(txData, (int)txGroup.group_id);
		// if(temp2 != null){
		// viewHolder.memberNum.setText("在线:" + temp2.group_ol_num);
		// }else{
		// viewHolder.memberNum.setText("在线:0");
		// }

		if (msgStats.containsKey(txGroup.group_id)
				&& msgStats.get(txGroup.group_id) > 0) {
			viewHolder.unReadCount.setVisibility(View.VISIBLE);
			viewHolder.unReadCount.setText("" + msgStats.get(txGroup.group_id));

		} else {
			viewHolder.unReadCount.setVisibility(View.GONE);
		}

		// 头像

		Bitmap bm = loadGroupImg(txGroup.group_avatar, txGroup.group_id,
				avatarCallback);
		viewHolder.avatar.setImageBitmap(bm);

		convertView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				TxGroup temp = TxGroup.getTxGroup(context.getContentResolver(),
						(int) txGroup.group_own_id);
				if (temp == null) {
					temp = txGroup;
				}
				if (TxData.isCloOP()) {
					goRoom(temp);
				} else {
					if (TxGroup.isPublicGroup(txGroup)) {
						// 公共群
						goRoom(temp);
					} else if (temp.group_tx_state != -1
							&& temp.group_tx_state != TxDB.QU_TX_STATE_OUT) {
						//说明temp.group_tx_state 等于0、1、2，是群主、管理或者群成员，是我的群
						
						if(myGroupList!=null&&!myGroupList.contains(temp.group_id)){
							Intent intent = new Intent(context,
									GroupJoin.class);
							intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							context.getApplicationContext().startActivity(
									intent);
						}else{
							goRoom(temp);
						}
					
					} else if (txGroup.group_type_channel == TxDB.GROUP_TYPE_REQUEST) {
						Intent intent = new Intent(context, GroupJoin.class);
						intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						context.getApplicationContext().startActivity(intent);
					}
				}
			}
		});

		return convertView;
	}

	public static class AvatarCache {
		private HashMap<Long, SoftReference<Bitmap>> mAvatarCache = new HashMap<Long, SoftReference<Bitmap>>();
		Bitmap[] mBitmaps;

		public AvatarCache(Resources res, int[] defs) {
			mBitmaps = new Bitmap[defs.length];
			for (int i = 0; i < defs.length; i++) {
				Bitmap bm = ((BitmapDrawable) res.getDrawable(defs[i]))
						.getBitmap();
				mBitmaps[i] = Utils.getRoundedCornerBitmap(bm);
			}
		}

		public Bitmap getCachedBitmap(long tx_partner_id) {
			synchronized (mAvatarCache) {
				SoftReference<Bitmap> soft = mAvatarCache.get(tx_partner_id);
				return (soft != null) ? soft.get() : null;
			}
		}

		public Bitmap cacheRoundedBitmap(long tx_partner_id, Bitmap bitmap) {
			bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
			synchronized (mAvatarCache) {
				mAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
						bitmap));
				return bitmap;
			}
		}

		public Bitmap cacheBitmap(long tx_partner_id, Bitmap bitmap) {
			synchronized (mAvatarCache) {
				mAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
						bitmap));
				return bitmap;
			}
		}

		public Bitmap toDefault(long tid, int i) {
			Bitmap bm = (i < 0 || i >= mBitmaps.length) ? mBitmaps[0]
					: mBitmaps[i];
			cacheBitmap(tid, bm);
			return bm;
		}

		public void clear() {
			mAvatarCache.clear();
		}
	}

	public static AvatarCache mAvatarCache;// viewpager的三个页面都是用这个缓存

	protected Bitmap loadGroupImg(String avatar_url, long group_id,
			AsyncCallback<Bitmap> callback) {
		if (mAvatarCache == null)
			mAvatarCache = new AvatarCache(context.getResources(),
					new int[] { R.drawable.qun_default });
		Bitmap bm = mAvatarCache.getCachedBitmap(group_id);
		if (bm == null) {
			bm = mAvatarCache.toDefault(group_id, 0);
			CallInfo callinfo = new CallInfo(avatar_url, group_id, callback);
			mAsynloader.obtainMessage(2, callinfo).sendToTarget();
		}
		return bm;
	}

	Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				CallInfo callinfo = (CallInfo) msg.obj;
				callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
				break;
			}
			}
		};
	};

	Handler mAsynloader;
	SessionManager mSess = SessionManager.getInstance();

	void prepairAsyncload() {
		mAsynloader = new Handler(SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				CallInfo ci;
				switch (msg.what) {
				case 2:
					ci = (CallInfo) msg.obj;
					if (ci.mUrl != null) {
						String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
								true, ci.mUid, false);
						if (Utils.debug) {
							Log.i(TAG, "getAvatar:file=" + file);
						}
						if (file != null) {
							File avatar = new File(file);
							if (avatar.exists()) {
								Bitmap bitmap = Utils.readBitMap(file, false);
								if (bitmap != null) {
									if (Utils.debug) {
										Log.i(TAG, "群组头相本地存在,加载bitmap，不去下载头像");
									}
									ci.mBitmap = mAvatarCache
											.cacheRoundedBitmap(ci.mUid, bitmap);
									mAvatarHandler.obtainMessage(1, ci)
											.sendToTarget();
									break;
								}
							}
						}
					}
					mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, true,
							false, true, new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onStart(FileTaskInfo taskInfo) {
								}

								@Override
								public void onProgress(FileTaskInfo taskInfo,
										int curlen, int totallen) {
								}

								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, false);
									if (Utils.debug) {
										Log.i(TAG, "downAvatar:file="
												+ taskInfo.mFullName);
									}
									if (bitmap != null) {
										CallInfo ci = (CallInfo) taskInfo.mObj;
										ci.mBitmap = mAvatarCache
												.cacheRoundedBitmap(
														taskInfo.mSrcId, bitmap);
										mAvatarHandler.obtainMessage(1,
												taskInfo.mObj).sendToTarget();
									}
								}

								@Override
								public void onError(FileTaskInfo taskInfo,
										int iCode, Object iPara) {
								}
							}, ci);
					break;
				}
			}
		};
	}

	void stopAsyncload() {
		mAsynloader = null;
	}

	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(context, R.string.request_outtime, Toast.LENGTH_SHORT)
					.show();

		}

	};

	private void goRoom(TxGroup txGroup) {
		changeTime(txGroup);
		Intent intent = new Intent(context, GroupMsgRoom.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
		context.startActivity(intent);
	}

//	private void joinRoom(TxGroup txGroup) {
//		Intent intent = new Intent(context, GroupJoin.class);
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//		intent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
//		context.startActivity(intent);
//	}

	private void changeTime(TxGroup txGroup) {
		txGroup.access_time = System.currentTimeMillis() / 1000;
		// TxGroup.saveTxGroupToDB(txGroup, txData);//用下面的方法 2014.01.23 shc

		ContentValues values = new ContentValues();
		values.put(TxDB.Qun.QU_ACCESS_TIME, txGroup.access_time);
		TxGroup.updateTxGroup(mSess.getContentResolver(), txGroup.group_id, values);
	}

	public ProgressDialog showDialogTimer(Context context, int title,
			int content, int milliseconds) {
		return showDialogTimer(context, title, context.getString(content),
				milliseconds, new BaseTimerTask() {

					@Override
					public void run() {
						super.run();
						handler.sendEmptyMessage(0);
					}
				});
	}

	AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {

		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			ImageView iv = (ImageView) listView.findViewWithTag("group_" + id);
			if (iv != null && result != null) {
				iv.setImageBitmap(result);
			}
		}
	};

	public class BaseTimerTask extends TimerTask {

		@Override
		public void run() {
			cancelDialog();
		}

	}

	private Timer baseTimer;
	private ProgressDialog baseDialog;

	public ProgressDialog showDialogTimer(Context context, int title,
			String content, int milliseconds, BaseTimerTask timerTask) {

		cancelDialog();
		baseDialog = new ProgressDialog(context);
		if (title != 0) {
			baseDialog.setTitle(title);
		}
		if (!Utils.isNull(content)) {
			baseDialog.setMessage(content);
		}
		baseDialog.setOnDismissListener(new OnDismissListener() {
			public void onDismiss(DialogInterface dialog) {
				cancelTimer();
			}

		});
		cancelTimer();
		baseTimer = new Timer();
		baseTimer.schedule(timerTask, milliseconds);
		return baseDialog;
	}

	public void cancelDialog() {
		if (baseDialog != null) {
			baseDialog.cancel();
			baseDialog = null;
		}
	}

	public void cancelTimer() {
		if (baseTimer != null) {
			try {
				baseTimer.cancel();
			} catch (Exception e) {

			}
			baseTimer = null;
		}
	}

}
