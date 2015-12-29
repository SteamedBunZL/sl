package com.shenliao.group.activity;

import java.util.List;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupHolder;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

/**
 * 群组动态
 */
public class GroupNewsActivity extends BaseActivity implements OnClickListener {
	protected static final String TAG = "GroupNewsActivity";
	private ListView listView;
	private GroupMessageAdapter adapter;
	private TextView delButton;
	UpdateReceiver updatareceiver;
	private List<TXMessage> tList;
	private int type = 0;
	private long date;
	private long a;
	private String time;
	GroupHolder holder = null;
	public static final String REFRESH_NOTICE = "refresh_notice";

	// int defaltHeaderImg;
	// int defaltHeaderImgMan;
	// int defaltHeaderImgFemale;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// prepairAsyncload();
		setContentView(R.layout.activity_group_message);

		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// title上的返回按钮
				thisContext.finish();

			}
		});
		init();
	}

	private void init() {
		// defaltHeaderImgMan = R.drawable.tui_con_photo;
		// defaltHeaderImgFemale = R.drawable.sl_regist_head_femal;
		listView = (ListView) findViewById(R.id.group_message_listView);
		delButton = (TextView) findViewById(R.id.group_message_delbtn);
		adapter = new GroupMessageAdapter(mSess.getContext());
		tList = TXMessage.getSLGroupNoticeList(GroupNewsActivity.this
				.getContentResolver());
		adapter.setData(tList);
		listView.setAdapter(adapter);
		if(tList!=null&&tList.size()>0){
			MsgStat.clearMessageUnread(mSess.getContentResolver(), tList.get(tList.size()-1));
		}
		delButton.setOnClickListener(this);
	}

	/**
	 * 
	 * 点击事件
	 */
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.group_message_delbtn:
			GroupUtils.showDialog(this, "警告", "是否删除所有信息？",
					R.string.dialog_okbtn, R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							// MsgStat.delGroupNoticeMsgStat(txdata);//用下面的删partner_id的方法
							MsgStat.delMsgStatByPartnerId(mSess.getContentResolver(),
									TX.SL_GROUP_NOTICE);
							tList.clear();
							adapter.notifyDataSetChanged();
							int num = TXMessage.clearSLGroupNoticeList(mSess.getContentResolver());// 删除数据库中的群组动态信息
							if (Utils.debug)
								Log.i(TAG, "删除的群组动态条目总数为：" + num);
							Toast.makeText(GroupNewsActivity.this, "删除成功",
									Toast.LENGTH_SHORT).show();
							GroupNewsActivity.this.finish();
						}
					});
			break;

		default:
			break;
		}

	}

	public class GroupMessageAdapter extends BaseAdapter {

//		private TxData txdata;
		private LayoutInflater inflater;

		private TxGroup txGroup;

		private Bitmap bm;

		public GroupMessageAdapter(Context context) {
//			this.txdata = txdata;
			inflater = LayoutInflater.from(context);

		}

		public void setData(List<TXMessage> tXMessage) {
			tList = tXMessage;
		}

		@Override
		public int getCount() {
			return tList.size();
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int arg0) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (Utils.debug) {
				Log.i(TAG, "执行了" + position + "的getView()方法");
			}
			GroupHolder holder = null;
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.sll_item_group_message,
						null);
				holder = new GroupHolder();
				holder.message = (TextView) convertView
						.findViewById(R.id.group_message_content);
				holder.time = (TextView) convertView
						.findViewById(R.id.group_message_listView_time);
				holder.agreeBtn = (Button) convertView
						.findViewById(R.id.group_message_okbtn);
				holder.refuseBtn = (Button) convertView
						.findViewById(R.id.group_message_nobtn);
				holder.headimg = (ImageView) convertView
						.findViewById(R.id.group_message_listView_item_photo);
				// holder.messageusername = (TextView)
				// convertView.findViewById(R.id.group_message_userName);
				holder.resultState = (TextView) convertView
						.findViewById(R.id.group_message_btnresult);

				convertView.setTag(holder);
			} else {
				holder = (GroupHolder) convertView.getTag();
			}

			TXMessage txMessage = tList.get(position);
			NotificationPopupWindow.showNotification(txMessage.msg_id, false);
			final TXMessage txMessage2 = txMessage;
			holder.resultState.setVisibility(View.GONE);
			switch (txMessage.msg_type) {
			// 群组动态
			case TxDB.MSG_TYPE_SET_GROUP_ADMIN: // 被设置或取消群管理员
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				// setMessage(txGroup, txMessage, date, a, time, holder);

				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				setOtherMessage(txGroup, txMessage, holder);
				setGroupHead(txMessage, holder);
				break;
			case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN:// 申请加入群 给管理员看的
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				setMessage(txGroup, txMessage, holder);
				// setMemberHead(txMessage, holder);

				final long uid = txMessage.tcard_id;

				holder.headimg.setTag(uid);
				String url = null;
				Bitmap bitmap = null;
				if (uid == TX.tm.getUserID())
					url = TX.tm.getTxMe().avatar_url;
				else {
					url = txMessage.tcard_avatar_url;
				}

				if (url != null) {
					bitmap = mSess.avatarDownload.getAvatar(url, uid,
							holder.headimg, avatarHandler);
				}
				if (bitmap == null) {
					holder.headimg
							.setImageResource(txMessage.tcard_sex == TX.MALE_SEX ? defaultHeaderImgMan
									: defaultHeaderImgFemale);
				} else {
					holder.headimg.setImageBitmap(bitmap);
				}

				holder.headimg.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						TX tx = TX.tm.getTx(uid);
						Intent intent = new Intent(thisContext,
								UserInformationActivity.class);
						intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
						intent.putExtra(
								UserInformationActivity.pblicInfo,
								TX.tm.isTxFriend(uid) == true ? UserInformationActivity.TUIXIN_USER_INFO
										: UserInformationActivity.NOT_TUIXIN_USER_INFO);
						intent.putExtra(UserInformationActivity.UID,
								tx.partner_id);

						thisContext.startActivity(intent);
					}
				});

				if (txMessage.op == -1) {
					holder.resultState.setVisibility(View.GONE);
					holder.agreeBtn.setVisibility(View.VISIBLE);
					holder.refuseBtn.setVisibility(View.VISIBLE);
					holder.agreeBtn.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View v) {
							showDialogTimer(GroupNewsActivity.this,
									R.string.prompt, R.string.group_sending,
									10 * 1000).show();
							mSess.getSocketHelper()
									.sendAgreeGroupReq(
											txMessage2.group_id_notice,
											txMessage2.tcard_id, txMessage2.sn,
											txMessage2.ac, true,
											txMessage2.msg_id);

						}
					});

					holder.refuseBtn.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							showDialogTimer(GroupNewsActivity.this,
									R.string.prompt, R.string.group_sending,
									10 * 1000).show();

							mSess.getSocketHelper()
									.sendAgreeGroupReq(
											txMessage2.group_id_notice,
											txMessage2.tcard_id, txMessage2.sn,
											txMessage2.ac, false,
											txMessage2.msg_id);

						}
					});
				} else {
					holder.agreeBtn.setVisibility(View.GONE);
					holder.refuseBtn.setVisibility(View.GONE);
					holder.resultState.setVisibility(View.GONE);
					if (txMessage.op == 1) {
						holder.resultState.setVisibility(View.VISIBLE);
						holder.resultState.setText("已通过");
						// holder.resultState.setTextColor(R.id.back);//TODO
						// 什么效果？ 2014.03.17 shc
						holder.resultState.setTextColor(Color.BLACK);

					} else if (txMessage.op == 2) {
						holder.resultState.setVisibility(View.VISIBLE);
						holder.resultState.setText("已拒绝");
						holder.resultState.setTextColor(Color.RED);
					}
				}
				break;
			case TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER:// 申请加入群结果 给成员看的
				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				setOtherMessage(txGroup, txMessage, holder);
				setGroupHead(txMessage, holder);

				break;
			case TxDB.MSG_TYPE_DISMISS_GROUP:// 群主解散群
				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				setOtherMessage(txGroup, txMessage, holder);
				setGroupHead(txMessage, holder);
				break;
			case TxDB.MSG_TYPE_LEAVE: // 本人被T
				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				setOtherMessage(txGroup, txMessage, holder);
				setGroupHead(txMessage, holder);
				break;
			case TxDB.MSG_TYPE_IN:// 本人被拉进一个群
				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
						(int) txMessage.group_id_notice);
				setJoinedMessage(txGroup, txMessage, holder);
				setGroupHead(txMessage, holder);
				break;
			case TxDB.MSG_TYPE_DISMISS_4_CREATOR:
				holder.agreeBtn.setVisibility(View.GONE);
				holder.refuseBtn.setVisibility(View.GONE);
				holder.message.setText(txMessage.msg_body);
				date = txMessage.send_time;
				a = date * 1000;
				if (Utils.debug)
					Log.i(TAG, "time:" + a);
				time = GroupUtils.dealDate(a, mSess.getContext());
				holder.time.setText(time);
				holder.headimg.setImageResource(R.drawable.slgroupnotice);
				break;
			}

			return convertView;
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
			filter.addAction(Constants.INTENT_ACTION_AGREE_GROUP_2020);
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			filter.addAction(Constants.INTENT_ACTION_AGREE_GROUP_JOIN);
			filter.addAction(Constants.INTENT_ACTION_ADD_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_DEL_GROUP_MEMBER);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			cancelDialogTimer();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_AGREE_GROUP_2020.equals(intent
					.getAction())) {
				dealAgreeGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent
					.getAction())) {
				dealSystem(serverRsp);
			} else if (Constants.INTENT_ACTION_AGREE_GROUP_JOIN.equals(intent
					.getAction())
					|| Constants.INTENT_ACTION_ADD_GROUP_MEMBER.equals(intent
							.getAction())
					|| Constants.INTENT_ACTION_DEL_GROUP_MEMBER.equals(intent
							.getAction())) {
				ref();
			}
		}
	}

	public void dealSystem(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_REQUEST_GROUP:
			case SYSTEM_MSG_SET_ADMIN:
				ref();
				break;
			}
		}

	}

	private void ref() {
		tList = TXMessage.getSLGroupNoticeList(GroupNewsActivity.this
				.getContentResolver());
		adapter.setData(tList);
		adapter.notifyDataSetChanged();
		if(tList!=null&&tList.size()>0){
			MsgStat.clearMessageUnread(mSess.getContentResolver(), tList.get(tList.size()-1));
		}
	}

	public void dealAgreeGroup(ServerRsp serverRsp) {
		cancelDialogTimer();
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			boolean agree = serverRsp.getBundle().getBoolean("agree");
			int gid = serverRsp.getBundle().getInt("gid");
			long uid = serverRsp.getBundle().getLong("uid");
			long clisn = serverRsp.getBundle().getLong("clisn");

			for (int i = 0; i < tList.size(); i++) {
				TXMessage tmsg = tList.get(i);
				if (tmsg.tcard_id == uid && tmsg.group_id_notice == gid) {
					if (agree) {
						tmsg.op = 1;
					} else {
						tmsg.op = 2;
					}
					TXMessage.saveTXMessagetoDB(tmsg, mSess.getContentResolver(), false);
				}
			}
			tList = TXMessage.getSLGroupNoticeList(GroupNewsActivity.this
					.getContentResolver());
			adapter.setData(tList);
			adapter.notifyDataSetChanged();
			break;
		case GROUP_FULL:
			Utils.startPromptDialog(GroupNewsActivity.this, R.string.prompt,
					R.string.group_join_full);
			break;
		case GROUP_MEMBER_OPT_NO_PERMISSION:
			Utils.startPromptDialog(GroupNewsActivity.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupNewsActivity.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case GROUP_MODIFY_GROUP_NOT_EXIST:
			Utils.startPromptDialog(GroupNewsActivity.this, R.string.prompt,
					R.string.group_not_exists);
			break;
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				ImageView iv = (ImageView) listView
						.findViewWithTag((Long) result[1]);
				if (iv != null) {
					long tag = (Long) iv.getTag();
					long id = (Long) result[1];
					if (result != null && tag == id) {
						iv.setImageBitmap((Bitmap) result[0]);
						adapter.notifyDataSetChanged();// 刷新adapter,为了相同的个人头像都能显示
					}
				}

				break;
			}
			super.handleMessage(msg);
		}
	};

	// AsyncCallback<Bitmap> avatarCallback = new AsyncCallback<Bitmap>() {
	//
	// @Override
	// public void onFailure(Throwable t, long id) {
	// }
	//
	// @Override
	// public void onSuccess(Bitmap result, long id) {
	// if (Utils.debug) {
	// Log.i(TAG, "执行了个人----" + id + "----的回调");
	// }
	// ImageView iv = (ImageView) listView.findViewWithTag(id);
	// if (iv != null && result != null) {
	// iv.setImageBitmap(result);
	// adapter.notifyDataSetChanged();// 刷新adapter,为了相同的个人头像都能显示
	//
	// if (Utils.debug) {
	// Log.i(TAG, "找到了个人----" + id + "--");
	// }
	// } else {
	// if (Utils.debug) {
	// Log.i(TAG, "没找到个人头像----" + id + "---");
	// }
	// }
	//
	// }
	// };

	// /**设置申请人头像 */
	// private void setMemberHead(final TXMessage txMessage, GroupHolder holder)
	// {
	// Bitmap bitmap = getPartnerCachedBitmap(txMessage.tcard_id);
	// if (bitmap == null) {
	// String url = null;
	// if (txMessage.tcard_id == TX.tm.getTxMe().partner_id)
	// url = TX.tm.getTxMe().avatar_url;
	// else {
	// // TX tx = TX.findTXByPartnerID4DB(uid,txdata.getContentResolver());
	// // if (tx != null)
	// // url = tx.avatar_url;
	// url = txMessage.tcard_avatar_url;
	// }
	// holder.headimg.setTag(txMessage.tcard_id);
	// if (url != null) {
	// CallInfo callinfo = new CallInfo(url, txMessage.tcard_id,
	// avatarCallback);
	// mPartnerAsynloader.obtainMessage(1, callinfo).sendToTarget();
	// }
	// holder.headimg.setImageResource(txMessage.tcard_sex==0?defaltHeaderImgMan:defaltHeaderImgFemale);
	// }else {
	// holder.headimg.setImageBitmap(bitmap);
	// }
	//
	// holder.headimg.setOnClickListener(new OnClickListener() {
	//
	// @Override
	// public void onClick(View v) {
	// // TX tx = TX.findTXByPartnerID4DB(txMessage.tcard_id);//不要直接访问数据库
	// 2013.10.17 shc
	// TX tx = TX.tm.getTx(txMessage.tcard_id);
	//
	//
	// Intent intent = new Intent(txdata, UserInformationActivity.class);
	// intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	// intent.putExtra(UserInformationActivity.pblicInfo,
	// TX.tm.isTxFriend(txMessage.tcard_id) == true ?
	// UserInformationActivity.TUIXIN_USER_INFO
	// : UserInformationActivity.NOT_TUIXIN_USER_INFO);
	// intent.putExtra(UserInformationActivity.infoTX, tx);
	//
	// txdata.startActivity(intent);
	// }
	// });
	// }

	/** 设置群头像 */
	private void setGroupHead(TXMessage txMessage, GroupHolder holder) {
		Bitmap bitmap = getGroupCachedBitmap(txMessage.group_id_notice);
		if (bitmap == null) {
			holder.headimg.setTag("group_" + txMessage.group_id_notice);
			holder.headimg.setImageResource(R.drawable.qun_default);

			loadGroupImg(txMessage.group_avatars_url,
					txMessage.group_id_notice, groupAvatarCallback);
			// CallInfo callinfo = new CallInfo(txMessage.group_avatars_url,
			// txMessage.group_id_notice, groupAvatarCallback);
			// mGroupAsynloader.obtainMessage(1, callinfo).sendToTarget();

		} else {
			holder.headimg.setImageBitmap(bitmap);
		}

	}

	//
	// private static HashMap<String, SoftReference<Bitmap>> mAvatarCache=new
	// HashMap<String, SoftReference<Bitmap>>();
	// Bitmap getCachedBitmap(String tx_partner_id)
	// {
	// synchronized(mAvatarCache){
	// SoftReference<Bitmap> soft=mAvatarCache.get(tx_partner_id);
	// return (soft!=null) ? soft.get() : null;
	// }
	// }
	// Bitmap cacheBitmap(String tx_partner_id, Bitmap bitmap)
	// {
	// synchronized(mAvatarCache){
	// bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
	// mAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(bitmap));
	// return bitmap;
	// }
	// }

	// 移到了setMemberImage中，先注掉 2013.09.12
	// protected Bitmap loadHeadImage(long uid, String
	// avatarUrl,AsyncCallback<Bitmap> callback) {
	// Bitmap bitmap = getCachedBitmap("" + uid);
	// if (bitmap == null) {
	// String url = null;
	// if (uid == TX.tm.getTxMe().partner_id)
	// url = TX.tm.getTxMe().avatar_url;
	// else {
	// // TX tx = TX.findTXByPartnerID4DB(uid,
	// // txdata.getContentResolver());
	// // if (tx != null)
	// // url = tx.avatar_url;
	// url = avatarUrl;
	// }
	// if (url != null) {
	// CallInfo callinfo = new CallInfo(url, uid, callback);
	// mAsynloader.obtainMessage(1, callinfo).sendToTarget();
	// }
	// }
	// return bitmap;
	// }
	//

	// 集成在了setGroupHead中，先注掉 2013.09.12
	// protected void loadGroupImg(String avatar_url, long group_id,
	// AsyncCallback<Bitmap> callback) {
	// CallInfo callinfo = new CallInfo(avatar_url, group_id, callback);
	// mAsynloader.obtainMessage(2, callinfo).sendToTarget();
	// }

	// Handler mAvatarHandler = new Handler() {
	// @Override
	// public void handleMessage(Message msg) {
	// switch (msg.what) {
	// case 1: {
	// CallInfo callinfo = (CallInfo) msg.obj;
	// callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
	// break;
	// }
	// }
	// };
	// };
	//
	// HandlerThread mHandlerThread;
	// Handler mAsynloader;
	// LoginSessionManager mSess = LoginSessionManager
	// .getLoginSessionManager(this);
	//
	// void prepairAsyncload() {
	// mHandlerThread = new HandlerThread("Asynloader");
	// mHandlerThread.start();
	// mAsynloader = new Handler(mHandlerThread.getLooper()) {
	// @Override
	// public void handleMessage(Message msg) {
	// CallInfo ci;
	// switch (msg.what) {
	// case 1:
	// ci = (CallInfo) msg.obj;
	// if (ci.mUrl != null) {
	// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
	// ci.mUid, false);
	// if (file != null) {
	// File avatar = new File(file);
	// if (avatar.exists()) {
	// Bitmap bitmap = Utils.readBitMap(file, false);
	// if (bitmap != null) {
	// ci.mBitmap=cacheBitmap(""+ci.mUid, bitmap);
	// mAvatarHandler.obtainMessage(1, ci)
	// .sendToTarget();
	// break;
	// }
	// }
	// }
	// }
	// mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, false,
	// true, new FileTransfer.DownUploadListner() {
	// // 此处要记录文件名，同时要加载
	// @Override
	// public void onFinish(FileTaskInfo taskInfo) {
	// Bitmap bitmap = Utils.readBitMap(
	// taskInfo.mFullName, false);
	// if (bitmap != null) {
	// CallInfo ci = (CallInfo) taskInfo.mObj;
	// ci.mBitmap=cacheBitmap(""+ci.mUid, bitmap);
	// mAvatarHandler.obtainMessage(1,
	// taskInfo.mObj).sendToTarget();
	// }
	// }
	// }, ci);
	// break;
	// case 2:
	// ci = (CallInfo) msg.obj;
	// if (ci.mUrl != null) {
	// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,true,
	// ci.mUid, false);
	// if (file != null) {
	// File avatar = new File(file);
	// if (avatar.exists()) {
	// Bitmap bitmap = Utils.readBitMap(file, false);
	// if (bitmap != null) {
	// ci.mBitmap = cacheBitmap("group_"+ci.mUid,bitmap);
	// mAvatarHandler.obtainMessage(1, ci)
	// .sendToTarget();
	// break;
	// }
	// }
	// }
	// }
	// mSess.mDownUpMgr.downloadAvatar(ci.mUrl, ci.mUid, 1, true,false,
	// true, new FileTransfer.DownUploadListner() {
	// // 此处要记录文件名，同时要加载
	// @Override
	// public void onFinish(FileTaskInfo taskInfo) {
	// Bitmap bitmap = Utils.readBitMap(
	// taskInfo.mFullName, false);
	// if (bitmap != null) {
	// CallInfo ci = (CallInfo) taskInfo.mObj;
	// ci.mBitmap = cacheBitmap("group_"+ci.mUid,bitmap);
	// mAvatarHandler.obtainMessage(1,
	// taskInfo.mObj).sendToTarget();
	// }
	// }
	// }, ci);
	// break;
	// }
	// }
	// };
	// }
	//
	// void stopAsyncload() {
	// mHandlerThread.quit();
	// mHandlerThread = null;
	// mAsynloader = null;
	// }

	/**
	 * 设置"申请加入群，给管理员看的信息"
	 */
	private void setMessage(TxGroup txGroup, TXMessage txMessage,
			GroupHolder holder) {
		if (txGroup != null) {
			String content = Utils.isNull(txGroup.group_title) ? ""
					+ txGroup.group_id : txGroup.group_title;
			if (txMessage.rs != null && !txMessage.rs.equals("")) {
				holder.message.setText(linkUser(
						txMessage.tcard_name
								+ "("
								+ txMessage.tcard_id
								+ ")"
								+ txMessage.msg_body.replace("�slgroup�",
										content) + "("
								+ txMessage.group_id_notice + ")",
						txMessage.tcard_name, txMessage.tcard_id, content,
						txGroup)
						+ "附言：" + txMessage.rs);
				holder.message.setMovementMethod(LinkMovementMethod
						.getInstance());
			} else {
				holder.message.setText(linkUser(
						txMessage.tcard_name
								+ "("
								+ txMessage.tcard_id
								+ ")"
								+ txMessage.msg_body.replace("�slgroup�",
										content) + "("
								+ txMessage.group_id_notice + ")",
						txMessage.tcard_name, txMessage.tcard_id, content,
						txGroup)
						+ txMessage.rs);

				holder.message.setMovementMethod(LinkMovementMethod
						.getInstance());

			}

		} else {
			if (txMessage.rs != null && !txMessage.rs.equals("")) {
				holder.message.setText(linkUser(
						txMessage.tcard_name
								+ "("
								+ txMessage.tcard_id
								+ ")"
								+ txMessage.msg_body.replace("�slgroup�", ""
										+ txMessage.group_id_notice) + "'"
								+ txMessage.rs + "'", txMessage.tcard_name,
						txMessage.tcard_id, txMessage.group_id_notice + "",
						txGroup)
						+ "附言：" + txMessage.rs);
				holder.message.setMovementMethod(LinkMovementMethod
						.getInstance());
			} else {
				holder.message.setText(linkUser(
						txMessage.tcard_name
								+ "("
								+ txMessage.tcard_id
								+ ")"
								+ txMessage.msg_body.replace("�slgroup�", ""
										+ txMessage.group_id_notice),
						txMessage.tcard_name, txMessage.tcard_id,
						txMessage.group_id_notice + "", txGroup)
						+ txMessage.rs);

				holder.message.setMovementMethod(LinkMovementMethod
						.getInstance());
			}

		}
		date = txMessage.send_time;
		a = date * 1000;
		if (Utils.debug)
			Log.i(TAG, "time:" + a);
		time = GroupUtils.dealDate(a, mSess.getContext());
		holder.time.setText(time);

	}

	private void setJoinedMessage(TxGroup txGroup, TXMessage txMessage,
			GroupHolder holder) {

		if (txGroup != null) {
			String content = Utils.isNull(txGroup.group_title) ? ""
					+ txGroup.group_id : txGroup.group_title;
			holder.message
					.setText(linkGroup(
							txMessage.msg_body.replace("�slgroup�", content)
									+ "(" + txMessage.group_id_notice + ")",
							content, txGroup));
			holder.message.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			holder.message.setText(linkGroup(
					txMessage.msg_body.replace("�slgroup�", ""
							+ txMessage.group_id_notice), ""
							+ txMessage.group_id_notice, txGroup));
			holder.message.setMovementMethod(LinkMovementMethod.getInstance());
		}
		date = txMessage.send_time;
		a = date * 1000;
		if (Utils.debug)
			Log.i(TAG, "time:" + a);
		time = GroupUtils.dealDate(a, mSess.getContext());
		holder.time.setText(time);

	}

	private void setOtherMessage(TxGroup txGroup, TXMessage txMessage,
			GroupHolder holder) {
		if (txGroup != null) {
			String content = Utils.isNull(txGroup.group_title) ? ""
					+ txGroup.group_id : txGroup.group_title;
			// holder.message.setText(txMessage.tcard_name +
			// txMessage.msg_body.replace("�slgroup�", content));
			holder.message.setText(linkGroup(txMessage.tcard_name
					+ txMessage.msg_body.replace("�slgroup�", content),
					content, txGroup));
			holder.message.setMovementMethod(LinkMovementMethod.getInstance());
		} else {
			// holder.message.setText(txMessage.tcard_name
			// + txMessage.msg_body.replace("�slgroup�", "" +
			// txMessage.group_id_notice));
			holder.message.setText(linkGroup(
					txMessage.tcard_name
							+ txMessage.msg_body.replace("�slgroup�", ""
									+ txMessage.group_id_notice), ""
							+ txMessage.group_id_notice, txGroup));

			holder.message.setMovementMethod(LinkMovementMethod.getInstance());
		}
		date = txMessage.send_time;
		a = date * 1000;
		if (Utils.debug)
			Log.i(TAG, "time:" + a);
		time = GroupUtils.dealDate(a, mSess.getContext());
		holder.time.setText(time);

	}

	AsyncCallback<Bitmap> groupAvatarCallback = new AsyncCallback<Bitmap>() {

		@Override
		public void onFailure(Throwable t, long id) {
		}

		@Override
		public void onSuccess(Bitmap result, long id) {
			if (Utils.debug) {
				Log.i(TAG, "执行了群头像----" + id + "----的回调");
			}
			ImageView iv = (ImageView) listView.findViewWithTag("group_" + id);
			if (iv != null && result != null) {
				iv.setImageBitmap(result);
				adapter.notifyDataSetChanged();// 刷新adapter,为了使所有相同的群头像都能显示

				if (Utils.debug) {
					Log.i(TAG, "找到了群头像----group_" + id + "----");
				}
			} else {
				if (Utils.debug) {
					Log.i(TAG, "没找到群头像----group_" + id + "----");
				}
			}

		}
	};

	private SpannableString linkGroup(String str, String groupName,
			TxGroup txGroup) {
		// 不加连接暂时注释掉
		SpannableString sp = new SpannableString(str);
		// String name=groupName;
		// int index=str.indexOf(name);
		// if(index==-1){
		// return sp;
		// }
		//
		// sp.setSpan(new ForegroundColorSpan(Color.BLUE), index, index +
		// name.length(),
		// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		// final Intent groupIntent=new
		// Intent(GroupMessage.this,GroupMsgRoom.class);
		// // TxGroup txGroup = TxGroup.findGroupById(txdata, (int)gid);
		// groupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// groupIntent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
		// sp.setSpan(new IntentSpan(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// txdata.startActivity(groupIntent);
		// }
		// }), index, index+name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) ;
		//

		return sp;

	}

	/**
	 * 拼联系人的链接和群链接
	 */
	private SpannableString linkUser(String str, String userName, long uid,
			String groupName, TxGroup txGroup) {
		SpannableString sp = new SpannableString(str);
		// 不加连接暂时注掉，没有修改 怕以后再加上。

		// String name1 = userName+"("+uid+")";;
		// String name2 = groupName;
		// int index2 = str.indexOf(name2);
		// if (index2 == -1) {
		// return sp;
		//
		// }
		// int index = str.indexOf(name1);
		// if (index == -1)
		// return sp;

		// sp.setSpan(new ForegroundColorSpan(Color.BLUE), index2,
		// index2 + name2.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// final Intent groupIntent = new Intent(this, GroupMsgRoom.class);
		// // TxGroup txGroup = TxGroup.findGroupById(txdata, (int)gid);
		// groupIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// groupIntent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
		// sp.setSpan(new IntentSpan(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// txdata.startActivity(groupIntent);
		// }
		// }), index2, index2 + name2.length(),
		// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		// TX tx = TX.findTXByPartnerID4DB(uid, txdata.getContentResolver());
		// if (tx == null) {
		// SocketHelper.getSocketHelper(txdata).sendGetUserInfor(uid);
		// return sp;
		// }
		// sp.setSpan(new ForegroundColorSpan(Color.BLUE), index, index +
		// name1.length(),
		// Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		// final Intent iSupplement = new Intent(this, UserInfoActivity.class);
		// iSupplement.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// iSupplement.putExtra(UserInfoActivity.pblicInfo,
		// txdata.IsTxFriend(uid) == true ? UserInfoActivity.TUIXINTOUSERINFO
		// : UserInfoActivity.NOTUIXINTOUSERINFO);
		// iSupplement.putExtra(UserInfoActivity.infoTX, tx);
		// sp.setSpan(new IntentSpan(new OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// txdata.startActivity(iSupplement);
		//
		// }
		// }), index, index + name1.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		return sp;
	}

	public class IntentSpan extends ClickableSpan {

		private final OnClickListener listener;

		public IntentSpan(View.OnClickListener listener) {
			this.listener = listener;
		}

		@Override
		public void onClick(View view) {
			listener.onClick(view);
		}
	}

	@Override
	protected void onDestroy() {
		// stopAsyncload();
		super.onDestroy();
	}
}
