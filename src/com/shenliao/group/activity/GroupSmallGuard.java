package com.shenliao.group.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.util.GroupHolder;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.EditSendImg;
import com.tuixin11sms.tx.activity.GroupMsgRoom;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.activity.explorer.ThumbnailCreator;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.contact.Contact;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.MusicUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.PlayAdiouAnimation;
import com.tuixin11sms.tx.view.WaitAdiouAnimation;
import com.umeng.analytics.MobclickAgent;

/**
 * 神聊小卫士列表activity
 * 
 */
public class GroupSmallGuard extends BaseActivity {
	protected static final String TAG = "GroupSmallGuard";
	private ListView listView;
	private GroupSmallGuardAdapter adapter;
	private List<TXMessage> txMsgList = new ArrayList<TXMessage>();// 所有神聊小卫士消息
	private UpdateReceiver updatareceiver;
	// private String FLUSH_ROOM_FALSE = "refresh";
	// private static int defaltHeaderImgMan = R.drawable.tui_con_photo;
	// private static int defaltHeaderImgFemale =
	// R.drawable.sl_regist_head_femal;
	private ClientManager playManager;
	private MusicUtils musicUtils;
	private Display display = null;
	private SmileyParser smileyParser;

	private ThumbnailCreator mThumbnail;

	private static final int FLUSH_ONE_LINE = 0x100;
	private static final int FLUSH_ROOM_FALSE = 0x101;

	private int playTime;

	private final int PLAY_ADIOU_C0MPELET = 423;
	private static final int RECORD_PLAY = 1;
	private static final int RECORD_PAUSE = 0;
	public final int[] volumeResource = new int[] { R.drawable.amp1,
			R.drawable.amp2, R.drawable.amp3, R.drawable.amp4, R.drawable.amp5,
			R.drawable.amp6, R.drawable.amp7, R.drawable.amp8, R.drawable.amp9,
			R.drawable.amp10, };
	private GroupHolder groupholder;
	Handler MsgHandler = new Handler() {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case PLAY_ADIOU_C0MPELET:
				if (txMsgList != null) {
					for (TXMessage temptxmsg1 : txMsgList) {
						temptxmsg1.PlayAudio = RECORD_PAUSE;
					}
				}
				musicUtils.PlaySound(3, 1, 2);
				flushOneLine(msg.obj);
				break;
			case FLUSH_ROOM_FALSE:
				// 全部adapter刷新
				adapter.notifyDataSetChanged();
				break;
			case FLUSH_ONE_LINE:
				// 单条刷新
				flushOneLine(msg.obj);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_smallguard);

		View v_clear_msgs = findViewById(R.id.tv_clear_msgs);
		v_clear_msgs.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// 删除所有信息

				GroupUtils.showDialog(thisContext, "警告", "是否删除所有信息？",
						R.string.dialog_okbtn, R.string.dialog_nobtn,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								MsgStat.delMsgStatByPartnerId(mSess.getContentResolver(),
										TX.SL_SAFE);
								txMsgList.clear();
								adapter.notifyDataSetChanged();
								int num = TXMessage.clearSLSafeList(mSess.getContentResolver());// 删除数据库中的神聊小卫士信息
								if (Utils.debug)
									Log.i(TAG, "删除的神聊小卫士条目总数为：" + num);
								Toast.makeText(thisContext, "删除成功",
										Toast.LENGTH_SHORT).show();
								thisContext.finish();
							}
						});

			}
		});

		mCurrentActivity = this;

		playManager = ClientManager.getPlayManager();
		display = getWindowManager().getDefaultDisplay();
		smileyParser = new SmileyParser(mSess.getContext());

		musicUtils = new MusicUtils();
		musicUtils.CreateSoundpool(mSess.getContext());
		musicUtils.LoadSound(mSess.getContext(), 1, R.raw.begin_record, 0);
		musicUtils.LoadSound(mSess.getContext(), 2, R.raw.finish_record, 1);
		musicUtils.LoadSound(mSess.getContext(), 3, R.raw.play_finish, 2);
		musicUtils.LoadSound(mSess.getContext(), 4, R.raw.send_sucess, 3);

		init();
	}

	private void init() {
		listView = (ListView) findViewById(R.id.group_smallGuard_listView);
		txMsgList = TXMessage.getSLSafeList(getContentResolver());
		adapter = new GroupSmallGuardAdapter();
		listView.setAdapter(adapter);
		if(txMsgList!=null&&txMsgList.size()>0){
			MsgStat.clearMessageUnread(mSess.getContentResolver(), TxDB.MS_TYPE_TB,TX.SL_SAFE);
		}
	}

	// 刷新单条adapter
	private void flushOneLine(Object obj) {
		if (obj != null && (obj instanceof TXMessage)) {
			TXMessage txmsg = (TXMessage) obj;
			adapter.updateView(txmsg);
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
		// 发送更新消息红点的广播
		mSess.getSocketHelper().sendNoReadMsg();
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
			// filter.addAction(FLUSH_ROOM_FALSE);
			filter.addAction(Constants.INTENT_ACTION_SHUTUP_GROUP_2028);
			filter.addAction(Constants.INTENT_ACTION_ADD_BLACK_GROUP_2026);
			filter.addAction(Constants.INTENT_ACTION_DEL_GROUP_MEMBER);
			filter.addAction(Constants.INTENT_ACTION_WARN_USER);
			filter.addAction(Constants.INTENT_ACTION_BLOCK_USER);
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			// if (intent.getAction().equals(FLUSH_ROOM_FALSE)) {
			// // txMsgs = TXMessage.getSLSafeList(getContentResolver());
			// // adapter.setData(txMsgs);
			// adapter.notifyDataSetChanged();
			// } else
			if (Constants.INTENT_ACTION_DEL_GROUP_MEMBER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealDelMember(serverRsp);
			} else if (Constants.INTENT_ACTION_ADD_BLACK_GROUP_2026
					.equals(intent.getAction())) {
				cancelDialogTimer();
				dealSetBlack(serverRsp);
			} else if (Constants.INTENT_ACTION_SHUTUP_GROUP_2028.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealShutup(serverRsp);
			} else if (Constants.INTENT_ACTION_WARN_USER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealWarnUser(serverRsp);
			} else if (Constants.INTENT_ACTION_BLOCK_USER.equals(intent
					.getAction())) {
				cancelDialogTimer();
				dealBlock(serverRsp);
			} else if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent
					.getAction())) {
				// 系统推送消息?
				dealSystem(serverRsp);
			}
		}
	}

	public void dealBlock(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			int sn = serverRsp.getInt("sn", 0);
			if (sn == 0) {
				Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
						R.string.seal_id_success);
			} else {
				Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
						R.string.seal_moible_success);
			}
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case USER_NO_EXIST:
			Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
					R.string.user_no_exists);
			break;
		case DONE:
			Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
					R.string.seal_done);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
					R.string.opt_failed);
			break;
		case BLOCK_FAILED:
			Utils.startPromptDialog(GroupSmallGuard.this, R.string.prompt,
					R.string.seal_moible_failed);
			break;
		}
	}

	public void dealSystem(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {

			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_SHUTUP:
			case SYSTEM_MSG_WARN:
			case SYSTEM_MSG_REPORT:
				txMsgList = TXMessage.getSLSafeList(getContentResolver());
				// adapter.setData(txMsgs);
				adapter.notifyDataSetChanged();
				if(txMsgList!=null&&txMsgList.size()>0){
					MsgStat.clearMessageUnread(mSess.getContentResolver(),TxDB.MS_TYPE_TB,TX.SL_SAFE);
				}
				break;
			}
		}

	}

	public void dealWarnUser(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			Utils.startPromptDialog(this, R.string.prompt, R.string.opt_success);
			break;
		case USER_NO_EXIST:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.user_no_exists);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(this, R.string.prompt, R.string.opt_failed);
			break;
		}
	}

	public void dealShutup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			int op = serverRsp.getBundle().getInt("op");
			boolean did = serverRsp.getBundle().getBoolean("did");
			if (did) {
				if (op == 0) {
					showToastText(R.string.shutup_done);
				} else {
					showToastText(R.string.shutup_cancel);
				}
			} else {
				showToastText(R.string.opt_success);
			}

			break;
		case OPT_FAILED:
			Utils.startPromptDialog(this, R.string.prompt, R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_NOT_MEMBER:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.group_not_member);
			break;
		case GROUP_NO_EXIST:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.group_not_exists);
			break;
		}

	}

	public void dealSetBlack(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			boolean did = serverRsp.getBundle().getBoolean("did");
			int op = serverRsp.getBundle().getInt("op");
			if (did) {
				if (op == 0) {
					showToastText(R.string.black_done);
				} else {
					showToastText(R.string.black_done2);
				}
			} else {
				showToastText(R.string.opt_success);
			}
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(this, R.string.prompt, R.string.opt_failed);
			break;
		case NO_PERMISSION:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.cannot_slience);
			break;
		case GROUP_BLACK_LIST_TO_MORE:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.group_black_list_to_more);
			break;
		case GROUP_NO_EXIST:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.group_not_exists);
			break;

		}

	}

	public void dealDelMember(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			showToastText(R.string.opt_success);
			break;
		case OPT_FAILED:
			Utils.startPromptDialog(this, R.string.prompt, R.string.opt_failed);
			break;
		case GROUP_MEMBER_SIZE_INVALID:
			Utils.startPromptDialog(this, R.string.prompt,
					R.string.group_del_to_more);
			break;
		}
	}

	private void showToastText(final int msg) {
		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				Toast.makeText(GroupSmallGuard.this, msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

	/** 下载大文件 */
	private void downloadBigFile(final TXMessage txmsg) {
		Utils.downloadBigFile(txmsg, new DownUploadListner() {

			@Override
			public void onStart(FileTaskInfo taskInfo) {
				txmsg.updateState = TXMessage.UPDATING;
				Utils.saveTxMsgToDB(txmsg);
			}

			@Override
			public void onProgress(FileTaskInfo taskInfo, int curlen,
					int totallen) {
				txmsg.updateCount = (int) (((double) curlen) * 100.0 / (double) totallen);
				if (mCurrentActivity != null) {
					mCurrentActivity.flush(txmsg);
				}
			}

			@Override
			public void onFinish(FileTaskInfo taskInfo) {
				if (txmsg != null) {
					txmsg.msg_path = taskInfo.mFullName;
					txmsg.updateState = TXMessage.UPDATE_OK;
					Utils.saveTxMsgToDB(txmsg);
					if (mCurrentActivity != null) {
						mCurrentActivity.flush(txmsg);
					}
					// 写文件增加新增文件数目
					String fileName = txmsg.msg_path.substring(txmsg.msg_path
							.lastIndexOf("/") + 1);
					int fileType = 0;
					if (fileName.contains(".")) {
						fileType = FileManager.getFileType(mSess.getContext(), fileName
								.substring(fileName.lastIndexOf(".") + 1));
					} else {
						fileType = FileManager.FILE_TYPE_UNKOWN;
					}
					try {
						Utils.increaseNewFileCount(mSess.getContext(), fileType);
					} catch (Exception e) {
						if (Utils.debug)
							Log.e(TAG, "自增新收到的大文件个数异常", e);
					}
				}
			}

			@Override
			public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
				txmsg.updateState = TXMessage.UPDATE_FAILE;
				Utils.saveTxMsgToDB(txmsg);
				if (mCurrentActivity != null) {
					mCurrentActivity.flush(txmsg);
				}
			}
		}, null);

	}

	/** 单条刷新 */
	public void flush(TXMessage txmsg) {
		MsgHandler.obtainMessage(FLUSH_ONE_LINE, txmsg).sendToTarget();
	}

	// 这个方法不调用了，调用Utils里面的方法，看看有没有什么不妥 2013.12.20 shc
	// 各种工具方法
	// private Bitmap getImgByPath(String path, int resolution) {
	// if (path == null)
	// return null;
	// try {
	// if (!Utils.checkSDCard()) {// 无SD卡
	// Bitmap nosd_img = BitmapFactory.decodeResource(
	// txdata.getResources(), R.drawable.no_sd_img);
	// return nosd_img;
	// }
	// Bitmap img = Utils.fitSizeAutoImg(path, resolution);
	// if (img == null)
	// return null;
	// WeakReference<Bitmap> wref1 = null;
	// if (resolution == Utils.msgroom_big_resolution) {
	// wref1 = new WeakReference<Bitmap>(img);
	// img = null;
	// } else {
	// WeakReference<Bitmap> wref = new WeakReference<Bitmap>(img);
	// Bitmap bm = Utils.ResizeBitmapLv(wref.get(), 3);
	// img = null;
	// wref1 = new WeakReference<Bitmap>(bm);
	// bm = null;
	// }
	// return wref1.get();
	//
	// } catch (Exception e) {
	//
	// } catch (OutOfMemoryError e) {
	//
	// }
	// return null;
	// }

	// DownUploadListner mImageCallback = new DownUploadListner() {
	//
	// @Override
	// public void onFinish(FileTaskInfo taskInfo) {
	// flush(FLUSH_ROOM_FALSE);
	// Utils.creatNoPhoto(txdata, Utils.IMAGE_PATH);
	//
	// }
	//
	// @Override
	// public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
	// flush(FLUSH_ROOM_FALSE);
	// }
	// };

	public void downloadImg(final TXMessage txmsg) {
		String imgFilePath = mSess.mDownUpMgr.getImageFile(txmsg.msg_url,
				FileTransfer.SRC_TYPE_ALBUM, txmsg.msg_id, false);
		// 这个回调中不需要与消息相关操作，故最后一个参数obj传为null
		mSess.mDownUpMgr.downloadImg(txmsg.msg_url, imgFilePath, 1, false,
				false, new DownUploadListner() {

					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						txmsg.msg_path = taskInfo.mFullName;
						txmsg.updateState = TXMessage.UPDATE_OK;
						Utils.creatNoPhoto(mSess.getContext(), Utils.IMAGE_PATH);
						Utils.saveTxMsgToDB(txmsg);
						flush(FLUSH_ROOM_FALSE);

					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
						txmsg.updateState = TXMessage.UPDATE_FAILE;
						Utils.saveTxMsgToDB(txmsg);
						flush(FLUSH_ROOM_FALSE);
					}
				}, null);
	}

	// LoginSessionManager mSess = LoginSessionManager
	// .getManager(txdata);

	DownUploadListner mAudioCallback = new DownUploadListner() {
		@Override
		public void onStart(FileTaskInfo taskInfo) {
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage msg = (TXMessage) taskInfo.mObj;
			msg.updateCount = 0;
			// flush(FLUSH_ROOM_FALSE);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (txmsg != null) {
				txmsg.msg_path = taskInfo.mFullName;
				txmsg.updateState = TXMessage.UPDATE_OK;
				String name = txmsg.msg_path.substring(
						txmsg.msg_path.lastIndexOf("/") + 1,
						txmsg.msg_path.length());
				Contact contact = new Contact(mSess.getContext());
				contact.readToFile(name, mSess.getContext());
				ContentValues values = new ContentValues();
				values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
				values.put(TxDB.Messages.UPDATE_STATE, TXMessage.UPDATE_OK);
				mSess.getContentResolver().update(TxDB.Messages.CONTENT_URI,
						values, TxDB.Messages.MSG_ID + "=?",
						new String[] { "" + txmsg.msg_id });
				// TXMessage.saveTXMessagetoDB(txmsg, msgactTxdata);//这个是以前注释掉的
				flush(FLUSH_ROOM_FALSE);
			}

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (txmsg != null) {
				txmsg.updateState = TXMessage.UPDATE_FAILE;
				Utils.saveTxMsgToDB(txmsg);
				flush(FLUSH_ROOM_FALSE);
			}
		}
	};

	public void DownAduioScoket(TXMessage txmsg) {
		String audioFilePath = mSess.mDownUpMgr.getAudioFile(txmsg.msg_url,
				txmsg.msg_id);
		mSess.mDownUpMgr.downloadAudio(txmsg.msg_url, audioFilePath, 0, false,
				mAudioCallback, txmsg);
	}

	// // TODO待播放的音频消息，这里好像不用这个集合 2013.12.18 shc
	// public ArrayList<TXMessage> talkMsgsCache = new ArrayList<TXMessage>();

	/**
	 * 播放音频
	 */
	public void playAudioRecord(TXMessage txmsg) {
		playManager.startToPlay(txmsg, recordListener);
		if (!txmsg.was_me) {
			txmsg.PlayAudio = RECORD_PLAY;
			txmsg.read_state = TXMessage.READ;
			Utils.saveTxMsgToDB(txmsg);
		}
	}

	RecordListener recordListener = new RecordListener() {

		@Override
		public void uploadFinish(TXMessage txMsg) {
		}

		@Override
		public void onPlayFinish(TXMessage txMsg) {
			// playManager.setRunning(false);
			playManager.stopPlay();
			Message message = new Message();// 生成消息，并赋予ID值
			message.what = PLAY_ADIOU_C0MPELET;
			message.obj = txMsg;
			MsgHandler.sendMessage(message);// 投递消息
		}

		@Override
		public void doRecordVolume(float volume) {

		}

		@Override
		public void deviceInitOK() {
		}

		@Override
		public void recordError(int errcode) {

		}
	};

	// 停止播放录音
	public void stopPlayAudioRecord() {
		if (playManager != null) {
			// playManager.setRunning(false);
			playManager.stopPlay();
		}
	}

	DownUploadListner mVCardCallback = new DownUploadListner() {
		@Override
		public void onStart(FileTaskInfo taskInfo) {
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage msg = (TXMessage) taskInfo.mObj;
			msg.updateCount = 0;
			// flush(FLUSH_ROOM_FALSE);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;

			if (txmsg != null) {
				txmsg.msg_path = taskInfo.mFullName;
				txmsg.updateState = TXMessage.UPDATE_OK;
				Utils.saveTxMsgToDB(txmsg);
				flush(FLUSH_ROOM_FALSE);
			}

			// String name = txmsg.msg_path.substring(
			// txmsg.msg_path.lastIndexOf("/") + 1,
			// txmsg.msg_path.length());
			// Contact contact = new Contact(txdata);
			// contact.readToFile(name, txdata);
			// ContentValues values = new ContentValues();
			// values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
			// txdata.getContentResolver().update(TxDB.Messages.CONTENT_URI,
			// values, TxDB.Messages.MSG_ID + "=?",
			// new String[] { "" + txmsg.msg_id });
			// // TXMessage.saveTXMessagetoDB(txmsg, msgactTxdata);//这个是以前注释掉的
			// flush(FLUSH_ROOM_FALSE);

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (txmsg != null) {
				txmsg.updateState = TXMessage.UPDATE_FAILE;
				Utils.saveTxMsgToDB(txmsg);
				flush(FLUSH_ROOM_FALSE);
			}

		}
	};

	public void DownContackSocket(TXMessage txmsg) {
		mSess.mDownUpMgr.downloadVCard(txmsg.msg_url, txmsg.msg_id, true,
				mVCardCallback, txmsg);
	}

	private String showStr(long uid) {
		// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
		TX tx = TX.tm.getTx(uid);

		if (tx != null) {
			return tx.getNick_name() + "(" + uid + ")";
		} else {
			return "" + uid;
		}
	}

	private void flush(int what) {
		// Intent i = new Intent(act);
		// txdata.sendBroadcast(i);
		MsgHandler.sendEmptyMessage(what);

	}

	private SpannableString linkGroup(String str, String groupName,
			TxGroup txGroup) {

		SpannableString sp = new SpannableString(str);
		String name = groupName;
		int index = str.indexOf(name);
		if (index == -1) {
			return sp;
		}

		sp.setSpan(new ForegroundColorSpan(Color.BLUE), index, index
				+ groupName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
		final Intent groupIntent = new Intent(thisContext, GroupMsgRoom.class);
		// TxGroup txGroup = TxGroup.findGroupById(txData, (int)gid);
		groupIntent.putExtra(Utils.MSGROOM_TX_GROUP, txGroup);
		sp.setSpan(new IntentSpan(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(groupIntent);
			}
		}), index, index + groupName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		return sp;

	}

	private SpannableString linkUser(String str, String userName, long uid) {

		SpannableString sp = new SpannableString(str);
		String name1 = userName;
		int index = str.indexOf(name1);
		if (index == -1)
			return sp;

		// TX tx = TX.findTXByPartnerID4DB(uid);//不要直接访问数据库 2013.10.17 shc
		TX tx = TX.tm.getTx(uid);

		if (tx == null) {
			mSess.getSocketHelper().sendGetUserInfor(uid);
			return sp;
		}
		sp.setSpan(new ForegroundColorSpan(Color.BLUE), index,
				index + userName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		final Intent iSupplement = new Intent(thisContext,
				UserInformationActivity.class);
		iSupplement
				.putExtra(
						UserInformationActivity.pblicInfo,
						TX.tm.isTxFriend(uid) == true ? UserInformationActivity.TUIXIN_USER_INFO
								: UserInformationActivity.NOT_TUIXIN_USER_INFO);
		iSupplement.putExtra(UserInformationActivity.UID, tx.partner_id);
		sp.setSpan(new IntentSpan(new OnClickListener() {

			@Override
			public void onClick(View v) {
				startActivity(iSupplement);

			}
		}), index, index + userName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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

	protected static GroupSmallGuard mCurrentActivity = null;// 当前activity对象

	public class GroupSmallGuardAdapter extends BaseAdapter {

		LinkedList<GroupHolder> mViewHolderList = new LinkedList<GroupHolder>();

		@Override
		public int getCount() {
			return txMsgList.size();
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
		public View getView(int position, View convertView, ViewGroup arg2) {
			final GroupHolder viewHolder;
			if (convertView == null) {
				viewHolder = new GroupHolder();
				mViewHolderList.add(viewHolder);
				convertView = View.inflate(thisContext,
						R.layout.sll_item_group_smallguard, null);
				// viewHolder.ll_time_layout = (LinearLayout) convertView
				// .findViewById(R.id.ll_time_layout);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.mTime);
				viewHolder.report = convertView.findViewById(R.id.report1);
				viewHolder.headimg = (ImageView) convertView
						.findViewById(R.id.guardHeadPic);
				viewHolder.displayName = (TextView) convertView
						.findViewById(R.id.guardNickname);
				// viewHolder.sex = (ImageView) convertView
				// .findViewById(R.id.guardSex);
				viewHolder.uid = (TextView) convertView
						.findViewById(R.id.guardContent);
				viewHolder.reportContont = convertView
						.findViewById(R.id.reportContont);
				viewHolder.failImg = (ImageView) convertView
						.findViewById(R.id.iv_msg_failImg);
				viewHolder.v1_text = (TextView) convertView
						.findViewById(R.id.v1_text);
				viewHolder.v2_img = convertView.findViewById(R.id.v2_img);
				viewHolder.v3_audio = convertView.findViewById(R.id.v3_audio);
				viewHolder.v4_vcard = convertView.findViewById(R.id.v4_card);
				viewHolder.v5_geo = convertView.findViewById(R.id.v5_geo);
				viewHolder.v6_bigFile = convertView
						.findViewById(R.id.v6_big_file);

				viewHolder.reportName = (TextView) convertView
						.findViewById(R.id.group_smallGuard_listView_tipName);
				viewHolder.reportGroup = (TextView) convertView
						.findViewById(R.id.group_smallGuard_listView_comeText);

				viewHolder.handler = convertView.findViewById(R.id.handler1);
				viewHolder.handlerContent = (TextView) convertView
						.findViewById(R.id.handlerContent);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (GroupHolder) convertView.getTag();
			}

			if (mThumbnail == null)
				mThumbnail = new ThumbnailCreator();

			final TXMessage txMsg = txMsgList.get(position);
			viewHolder.txmsg = txMsg;
			readHeadImg(viewHolder.headimg, txMsg.contacts_person_id,txMsg.sex); 
			viewHolder.headimg.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(thisContext, UserInformationActivity.class);
					intent.putExtra(UserInformationActivity.UID, (long)viewHolder.txmsg.contacts_person_id);
					if(Utils.debug){Log.i(TAG, "person_id"+viewHolder.txmsg.contacts_person_id);}
					startActivity(intent);
				}
			});
			viewHolder.displayName.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent intent = new Intent(thisContext, UserInformationActivity.class);
					intent.putExtra(UserInformationActivity.UID, (long)viewHolder.txmsg.contacts_person_id);
					startActivity(intent);
					
				}
			});
			updateView(viewHolder);

			return convertView;
		}

		protected void updateView(final GroupHolder viewHolder) {

			groupholder = viewHolder;

			final TXMessage txMsg = viewHolder.txmsg;
			// TODO 举报消息的字段组合有些混乱啊！！！ 2013.12.20 shc
			// viewHolder.sex.setImageResource(txMsg.sex==TX.MALE_SEX?R.drawable.ic_sex_male:R.drawable.ic_sex_female);

			NotificationPopupWindow.showNotification(txMsg.msg_id, false);
			viewHolder.failImg.setVisibility(View.GONE);
			viewHolder.handler.setVisibility(View.GONE);
			viewHolder.report.setVisibility(View.GONE);
			viewHolder.time.setText(new SimpleDateFormat("MM月dd日  HH:mm")
					.format(Utils.formatDateTime(txMsg.send_time)));

			// 重新定位举报消息的相对位置，防止复用了
			RelativeLayout.LayoutParams rl_bigFileParam = new RelativeLayout.LayoutParams(
					RelativeLayout.LayoutParams.WRAP_CONTENT,
					RelativeLayout.LayoutParams.WRAP_CONTENT);
			rl_bigFileParam.addRule(RelativeLayout.ALIGN_LEFT,
					R.id.rl_from_msgroom);
			rl_bigFileParam.addRule(RelativeLayout.BELOW, R.id.rl_from_msgroom);
			viewHolder.reportContont.setLayoutParams(rl_bigFileParam);

			switch (txMsg.msg_type) {

			case TxDB.MSG_TYPE_MANAGER_WARN:// 警告
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent
						.setText("警告:由于您"
								+ txMsg.msg_body
								+ ",为了维护聊天室的正常秩序，为网友们创造一个健康、文明、温馨、清静的聊天环境，请您进入聊天室的聊友自觉遵守聊天室纪律。");
				break;
			case TxDB.MSG_TYPE_MANAGER_NOTICE_BLOG_DELETE:// 警告
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent.setText(txMsg.msg_body);
				break;
			case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER:// 禁言
			case TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER_OVER:// 解除禁言
			case TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_MEMBER:// 封ID 封设备
			case TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_MEMBER:
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent.setText(txMsg.msg_body);
				break;
			case TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_ADMIN:
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent.setText("你封了用户"
						+ showStr(txMsg.tcard_id) + "的ID");
				break;
			case TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_ADMIN:
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent.setText("你封了用户"
						+ showStr(txMsg.tcard_id) + "的设备");
				break;
			case TxDB.MSG_TYPE_MANAGER_WARN_4_ADMIN:
				viewHolder.handler.setVisibility(View.VISIBLE);
				viewHolder.handlerContent.setText("你警告了用户"
						+ showStr(txMsg.tcard_id));
				break;
			// 举报信息
			case TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN:
				viewHolder.handler.setVisibility(View.GONE);
				viewHolder.report.setVisibility(View.VISIBLE);

				viewHolder.uid.setText("(" + txMsg.contacts_person_id + ")");
				viewHolder.displayName.setText(txMsg.partner_name);
				showView(viewHolder, (int) txMsg.msg_type2);
				TxGroup txGroup = null;
				// 说明只是举报单条信息
				if (txMsg.group_id_notice == 0) {
					viewHolder.reportGroup.setVisibility(View.GONE);
				} else {
					viewHolder.reportGroup.setVisibility(View.VISIBLE);
					txGroup = TxGroup.getTxGroup(mSess.getContentResolver(),
							(int) txMsg.group_id_notice);
					String conStr = null;
					if (txGroup == null) {
						mSess.getSocketHelper().sendGetGroup(
								txMsg.group_id_notice);
						conStr = "来自【" + txMsg.group_id_notice + "】聊天室 ";
						viewHolder.reportGroup.setText(smileyParser
								.addSmileySpans(
										linkGroup(conStr, txMsg.group_id_notice
												+ "", txGroup), true, 0));
						viewHolder.reportGroup
								.setMovementMethod(LinkMovementMethod
										.getInstance());
					} else {
						conStr = "来自【" + txGroup.group_title + "】聊天室 ";
						viewHolder.reportGroup.setText(smileyParser
								.addSmileySpans(
										linkGroup(conStr, txGroup.group_title,
												txGroup), true, 0));
						viewHolder.reportGroup
								.setMovementMethod(LinkMovementMethod
										.getInstance());
					}
					// viewHolder.reportGroup.setText(conStr);

				}
				// viewHolder.reportName.setText(txMsg.reportName + "举报 \"" +
				// txMsg.reportContext + "\"");
				viewHolder.reportName.setText(smileyParser.addSmileySpans(
						linkUser(txMsg.reportName + "举报 \""
								+ txMsg.reportContext + "\"", txMsg.reportName,
								txMsg.reportId), true, 0));
				viewHolder.reportName.setMovementMethod(LinkMovementMethod
						.getInstance());
				final TxGroup txGroup2 = txGroup;
				// convertView.setOnLongClickListener(new OnLongClickListener()
				// {
				viewHolder.report
						.setOnLongClickListener(new OnLongClickListener() {

							@Override
							public boolean onLongClick(View arg0) {
								final List<String> auths = TxGroup
										.initAuth(txGroup2);
								if (auths.contains("设置管理员")) {
									auths.remove("设置管理员");
								}
								String[] items = auths.toArray(new String[] {});

								new AlertDialog.Builder(thisContext).setItems(
										items,
										new DialogInterface.OnClickListener() {

											@Override
											public void onClick(
													DialogInterface dialog,
													int which) {

												String s = auths.get(which);
												if (s.contains("禁言")) {
													shutupOpt(
															txMsg.contacts_person_id,
															txMsg.partner_name,
															txMsg.group_id_notice);
												} else if (s.equals("踢出群")) {
													final List<Long> id = new ArrayList<Long>();
													id.add(Long
															.valueOf(""
																	+ txMsg.contacts_person_id));
													GroupUtils
															.showDialog(
																	thisContext,
																	"取消黑名单",
																	"是否确定将 "
																			+ smileyParser
																					.addSmileySpans(
																							txMsg.partner_name,
																							true,
																							0)
																			+ " 移除此群?",
																	R.string.dialog_okbtn,
																	R.string.dialog_nobtn,
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			mSess.getSocketHelper()
																					.sendDealGroup(
																							txMsg.group_id_notice,
																							false,
																							id);

																		}
																	});

												} else if (s.contains("黑名单")) {
													GroupUtils
															.showDialog(
																	thisContext,
																	"加入黑名单",
																	"是否确定将 "
																			+ smileyParser
																					.addSmileySpans(
																							txMsg.partner_name,
																							true,
																							0)
																			+ " 移至黑名单?",
																	R.string.dialog_okbtn,
																	R.string.dialog_nobtn,
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			mSess.getSocketHelper()
																					.sendSetBlackInGroup(
																							txMsg.group_id_notice,
																							txMsg.contacts_person_id,
																							0);
																		}
																	});

												} else if (s.equals("警告")) {

													Intent i = new Intent(
															thisContext,
															GroupTip.class);
													i.putExtra("txmsg", txMsg);
													i.putExtra(GroupTip.UID,
															txMsg.partner_id);
													i.putExtra(
															GroupTip.GID,
															txMsg.group_id_notice);
													startActivity(i);

												} else if (s.equals("封ID")) {
													GroupUtils
															.showDialog(
																	thisContext,
																	"警告",
																	"是否确定将 "
																			+ smileyParser
																					.addSmileySpans(
																							txMsg.partner_name,
																							true,
																							0)
																			+ " 处以封ID的处罚?",
																	R.string.dialog_okbtn,
																	R.string.dialog_nobtn,
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			mSess.getSocketHelper()
																					.sendBlock(
																							txMsg.contacts_person_id,
																							false);
																		}
																	});

												} else if (s.equals("封设备")) {
													GroupUtils
															.showDialog(
																	thisContext,
																	"警告",
																	"是否封锁 "
																			+ smileyParser
																					.addSmileySpans(
																							txMsg.partner_name,
																							true,
																							0)
																			+ " 的设备?该用户设备将无法启动神聊应用!",
																	R.string.dialog_okbtn,
																	R.string.dialog_nobtn,
																	new DialogInterface.OnClickListener() {
																		@Override
																		public void onClick(
																				DialogInterface dialog,
																				int which) {
																			mSess.getSocketHelper()
																					.sendBlock(
																							txMsg.contacts_person_id,
																							true);
																		}
																	});

												}
											}

										}).show();

								return false;
							}
						});

				ProgressBar loadingView = null;
				// 根据具体信息类型 进行显示
				switch ((int) txMsg.msg_type2) {
				case TxDB.MSG_TYPE_QU_COMMON_SMS:
				case TxDB.MSG_TYPE_COMMON_SMS:
					viewHolder.v1_text.setText(smileyParser.addSmileySpans(
							txMsg.msg_body, true, 0));
					break;
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:
				case TxDB.MSG_TYPE_IMAGE_EMS:
					loadingView = (ProgressBar) viewHolder.v2_img
							.findViewById(R.id.msgroomitem_List2_loading);
					loadingView.setVisibility(View.GONE);
					viewHolder.failImg.setVisibility(View.GONE);
					final ImageView imgView = (ImageView) viewHolder.v2_img
							.findViewById(R.id.msgroomitem_List2_MsgImg);
					Bitmap bitmap = null;
					switch (txMsg.updateState) {
					case TXMessage.UPDATE:
						if (txMsg.cacheImg != null) {// 从缓存读图片
							bitmap = txMsg.cacheImg.get();
							txMsg.updateState = TXMessage.UPDATE_OK;
						}
						if (bitmap == null) {
							// 缓存中有这个图片
							txMsg.updateState = TXMessage.UPDATING;
							Bitmap bitmap1 = Utils.getImgByPath(mSess.getContext(),
									txMsg.msg_path,
									Utils.msgroom_list_resolution);
							if (bitmap1 != null) {
								txMsg.cacheImg = new SoftReference<Bitmap>(
										bitmap1);
								txMsg.updateState = TXMessage.UPDATE_OK;
							} else {
								txMsg.msg_path = "";
								ContentValues values = new ContentValues();
								values.put(TxDB.Messages.MSG_PATH,
										txMsg.msg_path);
								mSess.getContentResolver().update(
										TxDB.Messages.CONTENT_URI, values,
										TxDB.Messages.MSG_ID + "=?",
										new String[] { "" + txMsg.msg_id });
								if (Utils.isAutoDownLoadImg(mSess.getContext())) {
									txMsg.updateState = TXMessage.UPDATING;
									if (!Utils.checkSDCard()) {// 无SD卡
										txMsg.updateState = TXMessage.UPDATE_FAILE;
										flush(FLUSH_ROOM_FALSE);
									} else {
										downloadImg(txMsg); // 转圈
									}
								} else {
									txMsg.updateState = TXMessage.UPDATE_CLICK;
								}

							}
							flush(FLUSH_ROOM_FALSE);
						}
						break;
					case TXMessage.UPDATING:
						imgView.setImageBitmap(null);
						break;
					case TXMessage.UPDATE_FAILE:
						imgView.setImageResource(R.drawable.msg_img_download_failed);
						break;
					case TXMessage.UPDATE_OK:
						if (txMsg.cacheImg != null) {// 从缓存读图片
							bitmap = txMsg.cacheImg.get();
							imgView.setImageBitmap(bitmap);
						}
						if (bitmap == null) {
							txMsg.updateState = TXMessage.UPDATING;
							Bitmap bitmap1 = Utils.getImgByPath(mSess.getContext(),
									txMsg.msg_path,
									Utils.msgroom_list_resolution);
							if (bitmap1 != null) {
								txMsg.cacheImg = new SoftReference<Bitmap>(
										bitmap1);
								txMsg.updateState = TXMessage.UPDATE_OK;
								flush(FLUSH_ROOM_FALSE);
							}
						}
						break;
					case TXMessage.UPDATE_CLICK:
						imgView.setImageResource(R.drawable.msgroom_click_download_img);
						break;
					}
					switch (txMsg.updateState) {
					case TXMessage.UPDATE:
						break;
					case TXMessage.UPDATING:
						loadingView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_FAILE:
						viewHolder.failImg.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_OK:
						break;
					}

					imgView.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							//
							if (!Utils.checkSDCard()) {// 无SD卡
								Utils.creatNoSdCardInfo(mSess.getContext()).show();
								return;
							}
							if (txMsg.updateState == TXMessage.UPDATE_CLICK) {
								if (Utils.isNull(txMsg.msg_path)) {
									txMsg.cacheImg = null;
									txMsg.updateState = TXMessage.UPDATING;
									if (!Utils.checkSDCard()) {// 无SD卡
										txMsg.updateState = TXMessage.UPDATE_FAILE;
										flush(FLUSH_ROOM_FALSE);
									} else {
										downloadImg(txMsg); // 转圈

									}
								}
								txMsg.updateState = TXMessage.UPDATING;
								flush(FLUSH_ROOM_FALSE);
							} else {
								Intent picIntent = new Intent();
								picIntent.setClass(thisContext,
										EditSendImg.class);
								picIntent
										.putExtra(EditSendImg.TXMESSAGE, txMsg);
								picIntent.putExtra(EditSendImg.USER_ID,
										TX.tm.getTxMe().partner_id);
								startActivity(picIntent);
							}
						}

					});
					break;
				case TxDB.MSG_TYPE_QU_AUDIO_EMS:
				case TxDB.MSG_TYPE_AUDIO_EMS:
					final WaitAdiouAnimation waitAudioAnim = (WaitAdiouAnimation) viewHolder.v3_audio
							.findViewById(R.id.v3_audio_anti);
					loadingView = (ProgressBar) viewHolder.v3_audio
							.findViewById(R.id.msgroomitem_List3_loading);
					PlayAdiouAnimation playAudioAnim = (PlayAdiouAnimation) viewHolder.v3_audio
							.findViewById(R.id.msgroomitem_List3_PlayRecordImg);
					// 是否已经播放过
					ImageView readImageView = (ImageView) viewHolder.v3_audio
							.findViewById(R.id.msgroomitem_List3_PlayType);
					readImageView
							.setVisibility(txMsg.read_state == TXMessage.READ ? View.GONE
									: View.VISIBLE);
					waitAudioAnim.stopAdiouPlayAn(txMsg.was_me);
					waitAudioAnim
							.setBackgroundResource(R.drawable.msg_bg_other);// 是不是跟上面的stopAudio功能重复了？2014.03.11
																			// shc
					playAudioAnim.setVisibility(View.VISIBLE);
					loadingView.setVisibility(View.GONE);

					if (txMsg.updateState == TXMessage.UPDATE) {
						if (Utils.isNull(txMsg.msg_path)) {
							// 本地地址为空，代表未下载
							if (!Utils.checkSDCard()) {
								// 无SD卡
								txMsg.updateState = TXMessage.UPDATE_FAILE;
								flush(FLUSH_ROOM_FALSE);
							} else {
								// 有SD卡
								if (Utils.isAutoDownLoadAdiou(mSess.getContext())) {
									DownAduioScoket(txMsg);
									txMsg.updateState = TXMessage.UPDATING;
									waitAudioAnim
											.startAdiouPlayAn(txMsg.was_me);
									playAudioAnim.setVisibility(View.GONE);
								} else {
									txMsg.updateState = TXMessage.UPDATE;
									waitAudioAnim
											.setBackgroundResource(R.drawable.msgroom_click_download_adiou);
									playAudioAnim.setVisibility(View.GONE);
								}
							}
						} else {
							// 本地地址不为空，代表已经下载完成
							txMsg.updateState = TXMessage.UPDATE_OK;
						}
						Utils.saveTxMsgToDB(txMsg);
					} else if (txMsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示消息失败红色惊叹号;
						viewHolder.failImg.setVisibility(View.VISIBLE);
						playAudioAnim.setVisibility(View.GONE);

					} else if (txMsg.updateState == TXMessage.UPDATING) {
						// 音频附件正在下载
						waitAudioAnim.startAdiouPlayAn(txMsg.was_me);
						playAudioAnim.setVisibility(View.GONE);

					} else if (txMsg.updateState == TXMessage.UPDATE_OK) {
						// 下载成功
					}

					// 播放音频
					if (txMsg.updateState != TXMessage.UPDATING
							&& txMsg.updateState != TXMessage.UPDATE_FAILE) {
						if (txMsg != playManager.getPlayingMsg()) {
							playAudioAnim.stopAdiouPlayAn(txMsg.was_me);
						} else {
							playAudioAnim.startAdiouPlayAn(txMsg.was_me);
						}
					}
					waitAudioAnim.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!Utils.checkSDCard()) {// 无SD卡
								Utils.creatNoSdCardInfo(mSess.getContext()).show();
								return;
							}
							if (txMsg.updateState == TXMessage.UPDATE) {
								// clickPlayTalkCache(txMsg);
								// playRecord
								// .setBackgroundResource(R.drawable.other_adiou_pao);
								DownAduioScoket(txMsg);
								txMsg.updateState = TXMessage.UPDATING;
								Utils.saveTxMsgToDB(txMsg);
								// flush(FLUSH_ROOM_FALSE);
							} else {
								if (!Utils.isNull(txMsg.msg_path)
										&& txMsg.updateState != TXMessage.UPDATING
										&& txMsg.updateState != TXMessage.UPDATE_FAILE) {

									// 播放音频
									if (txMsg != playManager.getPlayingMsg()) {
										playManager.stopPlay();
										playAudioRecord(txMsg);
									} else {
										stopPlayAudioRecord();
									}

								}
							}
							flush(FLUSH_ROOM_FALSE);
						}
					});

					// 更新时长
					if (txMsg.updateState != TXMessage.UPDATE
							&& txMsg.updateState != TXMessage.UPDATING) {
						long timeWidth = 80 + txMsg.audio_times * 10;
						int jianWidth = 170 * display.getWidth() / 480;
						if (timeWidth > display.getWidth() - jianWidth) {
							timeWidth = display.getWidth() - jianWidth;
						}
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								(int) (timeWidth),
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						// lp.addRule(RelativeLayout.BELOW, R.id.ll_name_uid);
						// lp.addRule(RelativeLayout.RIGHT_OF,
						// R.id.msgroomitem_List4_failImg);
						// lp.topMargin = Utils.dip2px(10,txdata);
						// lp.leftMargin = Utils.dip2px(5,txdata);
						// playRecord.setPadding(Utils.dip2px(10,txdata),
						// Utils.dip2px(10,txdata), Utils.dip2px(10,txdata),
						// Utils.dip2px(10,txdata));
						waitAudioAnim.setLayoutParams(lp);
					} else {
						RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
								RelativeLayout.LayoutParams.WRAP_CONTENT,
								RelativeLayout.LayoutParams.WRAP_CONTENT);
						// lp.addRule(RelativeLayout.BELOW, R.id.ll_name_uid);
						// lp.addRule(RelativeLayout.RIGHT_OF,
						// R.id.msgroomitem_List4_failImg);
						// lp.topMargin = Utils.dip2px(10,txdata);
						// lp.leftMargin = Utils.dip2px(5,txdata);
						// playRecord.setPadding(Utils.dip2px(10,txdata),
						// Utils.dip2px(10,txdata), Utils.dip2px(10,txdata),
						// Utils.dip2px(10,txdata));
						waitAudioAnim.setLayoutParams(lp);
					}
					// TextView voiceTime = (TextView) convertView
					TextView voiceTime = (TextView) viewHolder.v3_audio
							.findViewById(R.id.small_guard_audio_msg_recordTime);

					voiceTime.setText(MessageUtil
							.getRecordTime(txMsg.audio_times));
					// voiceTime.setOnClickListener(new OnClickListener() {
					//
					// @Override
					// public void onClick(View v) {
					// if (txMsg.updateState == TXMessage.UPDATE_FAILE) {
					// //
					// txMsg.updateState = TXMessage.UPDATE;
					// flush(FLUSH_ROOM_FALSE);
					// }
					// }
					// });

					break;
				case TxDB.MSG_TYPE_QU_CARD_EMS:
				case TxDB.MSG_TYPE_QU_TCARD_SMS:
				case TxDB.MSG_TYPE_CARD_EMS:
				case TxDB.MSG_TYPE_TCARD_SMS:
					viewHolder.v4_vcard.setVisibility(View.VISIBLE);
					// ImageView sexView = (ImageView) viewHolder.v4_vcard
					// .findViewById(R.id.msgroomitem_List4_card_sex);
					ImageView cardHeadView = (ImageView) viewHolder.v4_vcard
							.findViewById(R.id.msgroomitem_List4_card_head);
					cardHeadView
							.setBackgroundResource(R.drawable.user_infor_head_boy);
					// if (txMsg.msg_type2 == TxDB.MSG_TYPE_TCARD_SMS) {
					// sexView.setVisibility(View.VISIBLE);
					// } else {
					// sexView.setVisibility(View.GONE);
					// }
					// 名片名字
					loadingView = (ProgressBar) viewHolder.v4_vcard
							.findViewById(R.id.msgroomitem_List4_loading);
					loadingView.setVisibility(View.GONE);
					viewHolder.failImg.setVisibility(View.GONE);
					if (txMsg.updateState == TXMessage.UPDATE) {
						// 显示原图
						if (txMsg.msg_type2 == TxDB.MSG_TYPE_QU_CARD_EMS) {
							if (!Utils.isNull(txMsg.msg_url)
									&& Utils.isNull(txMsg.msg_path)) {
								// new
								// DownContactTask(GroupMsgRoom.this,""+user_id).execute(txMsg);
								if (!Utils.checkSDCard()) {// 无SD卡
									txMsg.updateState = TXMessage.UPDATE_FAILE;
									flush(FLUSH_ROOM_FALSE);
								} else {
									Utils.executorService
											.submit(new Runnable() {
												@Override
												public void run() {
													DownContackSocket(txMsg);
												}
											});
								}
								txMsg.updateState = TXMessage.UPDATING;
								loadingView.setVisibility(View.VISIBLE);
							} else {
								txMsg.updateState = TXMessage.UPDATE_OK;
							}
						}
					} else if (txMsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						viewHolder.failImg.setVisibility(View.VISIBLE);
					} else if (txMsg.updateState == TXMessage.UPDATING) {
						// 显示原图
						loadingView.setVisibility(View.VISIBLE);
					} else if (txMsg.updateState == TXMessage.UPDATE_OK) {
						// 显示原
					}
					String name = "";
					String partnerId = "";
					String phone = "";
					if (txMsg.msg_type2 == TxDB.MSG_TYPE_QU_CARD_EMS) {
						name = txMsg.contacts_person_name;
						// phone = "手机:"+txMsg.partner_phone;
						name = name.trim();
						// 名片头像
						readCardHeadImg(cardHeadView, txMsg.tcard_id,
								txMsg.tcard_sex, txMsg.tcard_avatar_url);
					} else if (txMsg.msg_type2 == TxDB.MSG_TYPE_QU_TCARD_SMS) {
						name = txMsg.tcard_name;
						partnerId = mSess.getContext().getResources().getString(
								R.string.msg_card_shenliao_code)
								+ txMsg.tcard_id;
						name = name.trim();
						// 名片头像
						readCardHeadImg(cardHeadView, txMsg.tcard_id,
								txMsg.tcard_sex, txMsg.tcard_avatar_url);
					}
					// 更新名片名字
					TextView NameView = (TextView) viewHolder.v4_vcard
							.findViewById(R.id.msgroomitem_List4_cardName);
					NameView.setText(smileyParser.addSmileySpans(name, true, 0));
					// 更新名片神聊号和电话
					TextView partnerId_view = (TextView) viewHolder.v4_vcard
							.findViewById(R.id.msgroomitem_List4_cardtxId);
					if (!partnerId.equals("")) {
						partnerId_view.setText(partnerId);
					} else {
						partnerId_view.setText(null);
						partnerId_view.setVisibility(View.GONE);
					}

					// TextView phone_view = (TextView) viewHolder.v4_vcard
					// .findViewById(R.id.msgroomitem_List4_card_phone);
					// if (!phone.equals("")) {
					// phone_view.setText(phone);
					// } else {
					// phone_view.setText(null);
					// phone_view.setVisibility(View.GONE);
					// }
					// 名片点击
					LinearLayout cardLayout = (LinearLayout) viewHolder.v4_vcard
							.findViewById(R.id.msgroomitem_List4_Layout22);
					cardLayout.setOnClickListener(new OnClickListener() {

						@Override
						public void onClick(View v) {
							if (txMsg.msg_type2 == TxDB.MSG_TYPE_QU_CARD_EMS) {

							} else {
								if (TX.tm.getUserID() == txMsg.tcard_id) {
									Intent intent = new Intent(thisContext,
											SetUserInfoActivity.class);
									startActivity(intent);
								} else {

									TX tx1 = null;
									long partner_id = txMsg.tcard_id;

									tx1 = TX.tm.getTx(partner_id);
									if (tx1 == null) {
										tx1 = new TX();
										tx1.setPartnerId(txMsg.tcard_id);
										tx1.setNick_name(txMsg.tcard_name);
										tx1.setAvatar_url(txMsg.tcard_avatar_url);
										tx1.setSign(txMsg.tcard_sign.trim());
										tx1.setSex(txMsg.tcard_sex);
										tx1.setPhone(txMsg.tcard_phone);

										TX.tm.addTx(tx1);
									}

									Intent intent = new Intent(thisContext,
											UserInformationActivity.class);
									intent.putExtra(
											UserInformationActivity.pblicInfo,
											TX.tm.isTxFriend(txMsg.tcard_id) ? UserInformationActivity.TUIXIN_USER_INFO
													: UserInformationActivity.NOT_TUIXIN_USER_INFO);
									intent.putExtra(
											UserInformationActivity.UID,
											tx1.partner_id);
									startActivity(intent);
								}
							}
						}

					});
					// 性别标志
					// ImageView sexImgView = (ImageView) viewHolder.v4_vcard
					// .findViewById(R.id.msgroomitem_List4_card_sex);
					// sexImgView
					// .setBackgroundResource(txMsg.tcard_sex == TX.FEMAL_SEX ?
					// R.drawable.ic_sex_female
					// : R.drawable.ic_sex_male);
					// sexImgView.setVisibility(View.VISIBLE);
					break;
				case TxDB.MSG_TYPE_QU_GEO_SMS:
				case TxDB.MSG_TYPE_GEO_SMS:
					// 名字
					loadingView = (ProgressBar) viewHolder.v5_geo
							.findViewById(R.id.msgroomitem_List5_loading);
					loadingView.setVisibility(View.GONE);
					// 定位
					ImageView msgroomitem_List5_MsgImg = (ImageView) viewHolder.v5_geo
							.findViewById(R.id.msgroomitem_List5_MsgImg);
					msgroomitem_List5_MsgImg
							.setOnClickListener(new OnClickListener() {
								@Override
								public void onClick(View v) {
									//
									if (txMsg.read_state != TXMessage.NOT_SENT) {
										Uri uri = Uri
												.parse(Utils.GOOGLE_MAP_STR
														+ txMsg.geo);
										Intent intent = new Intent(
												Intent.ACTION_VIEW, uri);
										try {
											startActivity(intent);
											// Utils.inPhoto = true;
										} catch (Exception e) {
											Toast.makeText(mSess.getContext(),
													"请检查您的移动设备上是否已安装地图类应用",
													Toast.LENGTH_LONG).show();
											if (Utils.debug) {
												Log.e(TAG,
														"当用户手机上没有安装地图应用的时候，这时开启地图会空指针异常",
														e);
											}
										}
									}
								}
							});
					break;
				case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
				case TxDB.MSG_TYPE_BIG_FILE_SMS:
					// 大文件消息

					// 修改大文件举报消息的位置
					// RelativeLayout.LayoutParams rl_bigFileParam = new
					// RelativeLayout.LayoutParams(
					// RelativeLayout.LayoutParams.WRAP_CONTENT,
					// RelativeLayout.LayoutParams.WRAP_CONTENT);
					rl_bigFileParam.addRule(RelativeLayout.ALIGN_LEFT,
							R.id.guardHeadPic);
					rl_bigFileParam.addRule(RelativeLayout.BELOW,
							R.id.rl_from_msgroom);
					viewHolder.reportContont.setLayoutParams(rl_bigFileParam);

					RelativeLayout rl_content = (RelativeLayout) viewHolder.v6_bigFile
							.findViewById(R.id.rl_big_file_message);

					View v_download_progress = viewHolder.v6_bigFile
							.findViewById(R.id.rl_download_progress);
					ProgressBar pd_downloading = (ProgressBar) v_download_progress
							.findViewById(R.id.pb_big_file_downloading);
					ImageView iv_stopDownload = (ImageView) v_download_progress
							.findViewById(R.id.iv_stop_download);
					iv_stopDownload
							.setOnClickListener(new View.OnClickListener() {

								@Override
								public void onClick(View v) {
									// 点击开始或停止下载
									if (txMsg.updateState == TXMessage.UPDATING) {
										// 正在下载，点击停止下载任务
										getDownUpMgr().removeDownloadBigTask(
												getDownUpMgr()
														.getDownloadTaskId(
																txMsg.msg_url),
												false);
										txMsg.updateState = TXMessage.UPDATE;
										Utils.saveTxMsgToDB(txMsg);
									} else {
										// 开始下载（下载完成后，下载按钮会隐藏）
										downloadBigFile(txMsg);
										// 直接置为下载中状态，防止出现无网络点击下载按钮没反应，多个下载任务添加到队列的bug，后期还是要找多个重复任务添加到队列的问题所在
										txMsg.updateState = TXMessage.UPDATING;
										Utils.saveTxMsgToDB(txMsg);// 如果不存数据库，那么退出聊天室再进入，消息状态不会是正在下载，也不会显示下载进度条

									}
									flush(FLUSH_ROOM_FALSE);
								}
							});
					TextView tv_fileName = (TextView) rl_content
							.findViewById(R.id.tv_big_file_name);
					ImageView iv_fileThumb = (ImageView) rl_content
							.findViewById(R.id.iv_big_file_thumb);
					TextView tv_fileLength = (TextView) rl_content
							.findViewById(R.id.tv_big_file_length);
					tv_fileLength.setText(Utils
							.formatFileLength(txMsg.msg_file_length));
					String fileName = Utils.getLocalFileName(txMsg.msg_path);
					if (Utils.debug)
						Log.i(TAG, "大文件的文件名为：" + fileName);
					tv_fileName.setText(fileName);

					iv_fileThumb.setImageResource(Utils.getBigFileThumb(
							fileName, txMsg.updateState));// 设置大文件消息的缩略图

					String file_etx = fileName.substring(fileName
							.lastIndexOf(".") + 1);
					if (file_etx.equalsIgnoreCase("jpg")
							|| file_etx.equalsIgnoreCase("png")) {
						// 只有jpg和png格式的图片文件需要显示缩略图
						final File imageFile = new File(txMsg.msg_path);
						if (imageFile.exists() && imageFile.length() != 0) {
							Bitmap thumb = mThumbnail.isBitmapCached(imageFile
									.getPath());

							if (thumb == null) {
								new AsyncTask<String, Void, Void>() {

									@Override
									protected Void doInBackground(
											String... params) {
										Bitmap bm = Utils.getImageThumbnail(
												params[0], 52, 52);
										mThumbnail.cacheThumbBitmap(params[0],
												bm);
										return null;
									}

									@Override
									protected void onPostExecute(Void result) {
										// 获取bitmap成功后
										flush(FLUSH_ROOM_FALSE);
									}

								}.execute(imageFile.getPath());
							} else {
								iv_fileThumb.setImageBitmap(thumb);
							}

						} else {
							iv_fileThumb
									.setImageResource(R.drawable.thumb_picture_room_big_file_unreceived);
						}

					}

					if (txMsg.updateState == TXMessage.UPDATE_FAILE) {
						viewHolder.failImg.setVisibility(View.VISIBLE);
					} else {
						viewHolder.failImg.setVisibility(View.GONE);
					}

					if (Utils.debug)
						Log.e(TAG, "该消息的下载状态，txmsg.updateState = "
								+ txMsg.updateState + ",fileName = " + fileName);

					if (txMsg.updateState == TXMessage.UPDATING) {
						// 正在下载状态
						pd_downloading.setVisibility(View.VISIBLE);
						pd_downloading.setProgress(txMsg.updateCount);
						iv_stopDownload
								.setImageResource(R.drawable.msgroom_stop_down_upload);
					} else if (txMsg.updateState == TXMessage.UPDATE_OK) {
						v_download_progress.setVisibility(View.GONE);
					} else if (txMsg.updateState == TXMessage.UPDATE
							|| txMsg.updateState == TXMessage.UPDATE_FAILE
							|| txMsg.updateState == TXMessage.UPDATE_CLICK) {
						// 待下载或下载失败状态
						pd_downloading.setVisibility(View.GONE);
						iv_stopDownload
								.setImageResource(R.drawable.msgroom_start_download);
					} else {
						// 其他状态
						if (Utils.debug)
							Log.e(TAG, "附件的上传下载状态又出问题了，txmsg.updateState = "
									+ txMsg.updateState + ",fileName = "
									+ fileName);
					}
					rl_content.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {

							if (txMsg.updateState == TXMessage.UPDATE
									&& txMsg.was_me) {
								showToast("找不到文件路径");

							} else if (txMsg.updateState == TXMessage.UPDATING) {
								if (Utils.debug) {
									showToast("文件没有传输完毕");
								}

							} else if (txMsg.updateState == TXMessage.UPDATE_OK) {
								if (Utils.debug)
									showToast("点击查看大文件");
								Utils.openBigFile(thisContext, txMsg);
							}
						}
					});
					break;
				}
				break;
			}

		}

		/** 单条刷新，通过【消息id】判定holder(因为退出聊天室再进入消息对象都是重新创建的),然后从新设置该holder的显示view */
		public boolean updateView(TXMessage txmsg) {
			for (GroupHolder hldr : mViewHolderList) {
				// if(Utils.debug)Log.d(TAG,"需要更新的txmsg.msg_id="+txmsg.msg_id+",hldr.txmsg.msg_id="+hldr.txmsg.msg_id);
				if (hldr.txmsg.msg_id == txmsg.msg_id) {
					if (Utils.debug)
						Log.e(TAG, "msg_id相等：" + txmsg.msg_id + ",文件本地路径："
								+ txmsg.msg_path + ",更新view");
					hldr.txmsg.updateCount = txmsg.updateCount;// 把count值赋给当前对象的txmsg
					hldr.txmsg.updateState = txmsg.updateState;
					hldr.txmsg.msg_url = txmsg.msg_url;
					hldr.txmsg.read_state = txmsg.read_state;
					updateView(hldr);
					return true;
				}
			}
			return false;
		}

		private void showView(GroupHolder viewHolder, int msgType2) {
			viewHolder.v1_text.setVisibility(View.GONE);
			viewHolder.v2_img.setVisibility(View.GONE);
			viewHolder.v3_audio.setVisibility(View.GONE);
			viewHolder.v4_vcard.setVisibility(View.GONE);
			viewHolder.v5_geo.setVisibility(View.GONE);
			viewHolder.v6_bigFile.setVisibility(View.GONE);
			switch (msgType2) {
			case TxDB.MSG_TYPE_QU_COMMON_SMS:
			case TxDB.MSG_TYPE_COMMON_SMS:
				viewHolder.v1_text.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_IMAGE_EMS:
			case TxDB.MSG_TYPE_IMAGE_EMS:
				viewHolder.v2_img.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
			case TxDB.MSG_TYPE_AUDIO_EMS:
				viewHolder.v3_audio.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_CARD_EMS:
			case TxDB.MSG_TYPE_QU_TCARD_SMS:
			case TxDB.MSG_TYPE_CARD_EMS:
			case TxDB.MSG_TYPE_TCARD_SMS:
				viewHolder.v4_vcard.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_GEO_SMS:
			case TxDB.MSG_TYPE_GEO_SMS:
				viewHolder.v5_geo.setVisibility(View.VISIBLE);
				break;
			case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
			case TxDB.MSG_TYPE_BIG_FILE_SMS:
				viewHolder.v6_bigFile.setVisibility(View.VISIBLE);
				break;
			}
		}

		private void shutupOpt(final int contactsPersonId, final String name,
				final long gid) {
			final String[] items = new String[] { "5分钟", "30分钟", "24小时", "永久" };
			new AlertDialog.Builder(thisContext).setItems(items,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							int time = 5 * 60;
							String disTime = null;
							switch (which) {
							case 0:
								time = 5 * 60;
								disTime = items[0];
								break;
							case 1:
								time = 30 * 60;
								disTime = items[1];
								break;
							case 2:
								time = 24 * 60 * 60;
								disTime = items[2];
								break;
							case 3:
								time = 0;
								disTime = items[3];
								break;
							}
							final int t = time;
							GroupUtils.showDialog(
									thisContext,
									"警告",
									"是否将 "
											+ smileyParser.addSmileySpans(name,
													true, 0) + " 处以 " + disTime
											+ " 禁言的处罚?", R.string.dialog_okbtn,
									R.string.dialog_nobtn,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mSess.getSocketHelper()
													.sendShutupGroup(gid,
															contactsPersonId,
															0, t);

										}
									});

						}
					}).show();
		}

	}

	// 读取好友头像
	public void readHeadImg(final ImageView headView1, long tx_partner_id,int sex) {
		Bitmap bm = null;
		if (!Utils.isIdValid(tx_partner_id))
			return;
		if (headView1 != null) {
			headView1.setTag(tx_partner_id);
			if (TX.TUIXIN_MAN == tx_partner_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				return;
			}
			
			Bitmap bmHead = mSess.avatarDownload.getHeadIcon(tx_partner_id, new AsyncCallback<Bitmap>() {
				
				@Override
				public void onSuccess(Bitmap result, long id) {
					if(((Long)headView1.getTag())==id&&result!=null){
						headView1.setImageBitmap(result);
					}
				}
				
				@Override
				public void onFailure(Throwable t, long id) {
					
				}
			});
			if(bmHead!=null){
				headView1.setImageBitmap(bmHead);
			}else {
				headView1.setImageBitmap(mSess.getDefaultPartnerAvatar(sex));
			}
			
			
			
			
//			TX ttx = TX.tm.getTx(tx_partner_id);
//			if (Utils.isNull(ttx.avatar_url)) {
//				if (Utils.debug) {
//					Log.e(TAG, tx_partner_id + "头像地址为空！！！");
//				}
//				bm = ((BitmapDrawable) getResources().getDrawable(
//						ttx.getSex() == TX.MALE_SEX ? defaultHeaderImgMan
//								: defaultHeaderImgFemale)).getBitmap();
//				return;
//			}
////			headView1.setTag(tx_partner_id);
//			bm = mSess.avatarDownload.getAvatar(ttx.avatar_url, tx_partner_id,
//					headView1, avatarHandler);
//			if (bm != null) {
//				headView1.setImageBitmap(bm);
//				return;
//			} else {
//				if (ttx != null) {
//					bm = mSess.cachePartnerDefault(tx_partner_id, ttx.getSex());
//					headView1.setImageBitmap(bm);
//				}
//			}
		}
	}

	// 加载名片头像
	private void readCardHeadImg(ImageView headView1, long card_id, int sex,
			String cardUrl) {
		Bitmap bm = null;
		if (headView1 != null) {
			if (!Utils.isIdValid(card_id))
				return;
			if (TX.TUIXIN_MAN == card_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				headView1.setTag(card_id);
				return;
			}

			if (Utils.isNull(cardUrl)) {
				bm = ((BitmapDrawable) getResources().getDrawable(
						sex == TX.MALE_SEX ? defaultHeaderImgMan
								: defaultHeaderImgFemale)).getBitmap();
				return;
			}
			headView1.setTag(card_id);
			bm = mSess.avatarDownload.getAvatar(cardUrl, card_id, headView1,
					avatarHandler_card);
			if (bm != null) {
				headView1.setImageBitmap(bm);
				return;
			}
			
			bm = mSess.cachePartnerDefault(card_id, sex);
			headView1.setImageBitmap(bm);
		}
	}

	// handler
	private Handler avatarHandler_card = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;

				ImageView iv = (ImageView) listView
						.findViewWithTag((Long) result[1]);
				if (iv != null && result != null) {
					iv.setImageBitmap((Bitmap) result[0]);
				}

				break;
			}
			super.handleMessage(msg);
		}
	};
	// handler
	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.AVATAR_RESULT:
				Object[] result = (Object[]) msg.obj;
				if (groupholder.headimg != null) {
					long tid = (Long) groupholder.headimg.getTag();
					if (result != null && tid == (Long) result[1]) {
						groupholder.headimg.setImageBitmap((Bitmap) result[0]);
					}
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onDestroy() {
		if (mCurrentActivity == thisContext) {
			// 说明是自己销毁的自己，可以置空
			mCurrentActivity = null;
		}
		super.onDestroy();
	}

}
