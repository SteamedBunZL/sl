package com.tuixin11sms.tx.activity;

import java.io.File;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.ActivityGroup;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.db.TxDBContentProvider;
import com.tuixin11sms.tx.download.AutoUpdater;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.WapShare;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

//继承ActivityGroup是因为GroupIndex中要显示三个activity，用到了ActivityGroup的技术
public abstract class BaseActivity extends ActivityGroup {
	private static final String TAG = "BaseActivity";
//	protected TxData txdata;
	protected Activity thisContext;// 当前activity的引用
	private SharedPreferences prefs_meme = null;
	private Editor editor_meme;
	private SharedPreferences prefs_set = null;
	private Editor editor_set;
	private NotifyReceiver verUpReceiver;
	private SystemNotifyReceiver systemNotify;// 接收被迫下线提示的广播
	private final int AUTO_UPDATE = 100;
	private final int FORCE_UPDATE = 101;
	private final int SYSTEM_NOTIFY = 106;
	private final int SYSTEM_MSG_DIALOG = 107;
	private final int BLACK_DEVICE = 108;
	private final int SYSTEM_WARN = 109;
	private final int SHOW_TOAST = 110;// 弹出toast
	private String system_notify_title;
	private String system_notify_content;
	private String system_dialog_msg;
	private String system_dialog_title;
	private String black_device_msg;
	private String black_device_title;

	private AlertDialog promptDialogOtherlogin;
	private DialogAdapter dialogAdapter;
	private Dialog dialog;
	private ProgressDialog shareProgressDialog;
	public static final int[] volumeResource = new int[] { R.drawable.amp1,
			R.drawable.amp2, R.drawable.amp3, R.drawable.amp4, R.drawable.amp5,
			R.drawable.amp6, R.drawable.amp7, R.drawable.amp8, R.drawable.amp9,
			R.drawable.amp10, };
	private List<WapShare> shareList;
	protected int defaultHeaderImg;
	protected static int defaultHeaderImgMan;
	protected static int defaultHeaderImgFemale;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		thisContext = this;
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onCreate");
		prefs_meme = getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_PRIVATE);
		editor_meme = prefs_meme.edit();

		prefs_set = getSharedPreferences(SettingsPreference.TUIXIN_SETTING,
				Context.MODE_PRIVATE);
		editor_set = prefs_set.edit();

//		txdata = (TxData) getApplication();
		defaultHeaderImgMan = R.drawable.user_infor_head_boy;
		defaultHeaderImgFemale = R.drawable.user_infor_head_girl;

		setDisplay();// 每次打开activity，都刷一遍这个值，是不是不太好？？
		prepairAsyncload();

		if (thisContext.getComponentName().getClassName()
				.endsWith("MessageActivity")) {

		}

		super.onCreate(savedInstanceState);

	}

	/**
	 * 设置显示屏分辨率
	 */
	private void setDisplay() {
		Utils.SreenW = getWindowManager().getDefaultDisplay().getWidth();
		Utils.SreenH = getWindowManager().getDefaultDisplay().getHeight();
	}

	private String lastactivityName;

	@Override
	protected void onPause() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onPause");
		unregistReceiver();
		super.onPause();
	}

	@Override
	protected void onRestart() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onRestart");
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onResume");
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStart() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onStart");
		if (systemNotify == null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);
			systemNotify = new SystemNotifyReceiver();
			registerReceiver(systemNotify, filter);
		}
		super.onStart();
	}

	@Override
	protected void onStop() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onStop");
		if (systemNotify != null) {
			unregisterReceiver(systemNotify);
			systemNotify = null;
		}
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		if (Utils.debug)
			Log.i(TAG, thisContext.getComponentName().getClassName()
					+ "--onDestroy");
		super.onDestroy();
	}

	public Editor getEditorMeme() {
		return editor_meme;
	}

	public Editor getEditorSet() {
		return editor_set;
	}

	public SharedPreferences getPrefsMeme() {
		return prefs_meme;
	}

	public SharedPreferences getPrefsSet() {
		return prefs_set;
	}

	private void registReceiver() {
		if (verUpReceiver == null) {
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_AUTO_VERSION_UPDATE);
			filter.addAction(Constants.INTENT_ACTION_FORCE_VERSION_UPDATE);
			// filter.addAction(Constants.INTENT_ACTION_SYSTEM_MSG);//被迫下线等通知的receive
			filter.addAction(Constants.INTENT_ACTION_WAP_SHARE_RSP);
			filter.addAction(Constants.INTENT_ACTION_WARN_USER_OTHER);
			verUpReceiver = new NotifyReceiver();
			registerReceiver(verUpReceiver, filter);
		}
	}

	private void unregistReceiver() {
		if (verUpReceiver != null) {
			unregisterReceiver(verUpReceiver);
			verUpReceiver = null;
		}
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AUTO_UPDATE: {
				if (!AutoUpdater.isUping) {
					AutoUpdater.isUping = true;
					new AlertDialog.Builder(BaseActivity.this)
							.setTitle(R.string.up_prompt)
							.setCancelable(false)
							// .setMessage(getPrefsMeme().getString(CommonData.UPDATA_LOG,""))
							.setMessage(mSess.mPrefMeme.updata_log.getVal())
							.setPositiveButton(R.string.confirm,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialoginterface,
												int i) {
											// AutoUpdater.CheckForUpdate(BaseActivity.this,
											// getPrefsMeme().getString(CommonData.UPDATA_URL,""),
											// getPrefsMeme().getString(CommonData.UPDATA_LOG,""),
											// Utils.appver);
											// editor_meme.putInt(CommonData.OLD_VER,Utils.appver);
											// editor_meme.commit();
											AutoUpdater.CheckForUpdate(
													BaseActivity.this,
													mSess.mPrefMeme.updata_url
															.getVal(),
													mSess.mPrefMeme.updata_log
															.getVal(),
													Utils.appver);
											mSess.mPrefMeme.old_ver.setVal(
													Utils.appver).commit();
											// 清除本地缓存的音频文件
											Utils.clearStrangerAudio();
										}
									})
							.setNegativeButton(R.string.cancel,
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
											AutoUpdater.isUping = false;
											// editor_meme.putInt(CommonData.OLD_VER,Utils.appver);
											// editor_meme.commit();
											mSess.mPrefMeme.old_ver.setVal(
													Utils.appver).commit();
										}
									}).show();// 显示对话框
				}
				break;

			}
			case FORCE_UPDATE: {
				new AlertDialog.Builder(BaseActivity.this)
						.setTitle(R.string.up_prompt)
						.setCancelable(false)
						.setMessage(R.string.force_upgrade)
						.setNegativeButton(R.string.confirm,
								new DialogInterface.OnClickListener() {
									public void onClick(
											DialogInterface dialoginterface,
											int i) {
										AutoUpdater.isUping = false;
										// AutoUpdater.CheckForUpdate(BaseActivity.this,
										// getPrefsMeme().getString(CommonData.UPDATA_URL,""),"",
										// getPrefsMeme().getInt(CommonData.UPDATA_VER,0));
										AutoUpdater.CheckForUpdate(
												BaseActivity.this,
												mSess.mPrefMeme.updata_url
														.getVal(), "",
												mSess.mPrefMeme.updata_ver
														.getVal());
										// 清除本地缓存的音频文件
										Utils.clearStrangerAudio();
										// getEditor().putInt(
										// CommonData.OLD_VER,
										// appver);
										// getEditor().commit();
									}
								}).show();// 显示对话�?
				break;
			}
			case SYSTEM_NOTIFY:
				startPromptDialogOtherLogin(system_notify_title,
						system_notify_content);
				break;
			case SYSTEM_MSG_DIALOG:
				startPromptDialog(system_dialog_title, system_dialog_msg);
				break;
			case BLACK_DEVICE:
				startPromptBlackDevice(black_device_title, black_device_msg);
				break;
			case SYSTEM_WARN:
				// String warnMsg = (String) msg.obj;
				// new AlertDialog.Builder(BaseActivity.this)
				// .setTitle(R.string.system_warn)
				// .setCancelable(false)
				// .setMessage(
				// "你由于“" + warnMsg + "”被警告了，请自觉遵守神聊管理规范并保护聊天室秩序。")
				// .setPositiveButton(R.string.confirm,
				// new DialogInterface.OnClickListener() {
				// public void onClick(DialogInterface dialog,
				// int i) {
				// dialog.cancel();
				// }
				// }).show();// 显示对话框

				break;
			case SHOW_TOAST:
				String content = (String) msg.obj;
				Toast.makeText(thisContext, content, msg.arg1).show();// 显示toast
				break;
			}

		}
	};

	private class NotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message msg = handler.obtainMessage();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_AUTO_VERSION_UPDATE.equals(intent
					.getAction())) {
				msg.what = AUTO_UPDATE;
				handler.sendMessage(msg);
			} else if (Constants.INTENT_ACTION_FORCE_VERSION_UPDATE
					.equals(intent.getAction())) {
				msg.what = FORCE_UPDATE;
				handler.sendMessage(msg);
			} else if (Constants.INTENT_ACTION_WARN_USER_OTHER.equals(intent
					.getAction())) {
				dealWarnUser(intent);
				// } else if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent
				// .getAction())) {
				// dealSystemMsg(serverRsp);
			} else if (Constants.INTENT_ACTION_WAP_SHARE_RSP.equals(intent
					.getAction())) {
				// 转发信息的服务器返回
				dealWapShare(serverRsp);
			}
		}

	}

	/** 单独抽取出来的接收系统广播的receiver,例如提示被迫下线 */
	private class SystemNotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			Message msg = handler.obtainMessage();
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_SYSTEM_MSG.equals(intent.getAction())) {
				dealSystemMsg(serverRsp);
			}
		}

	}

	/**
	 * 处理被警告信息
	 * 
	 * @param intent
	 */
	private void dealWarnUser(Intent intent) {
		if (intent != null) {
			TXMessage msg = intent.getParcelableExtra("warnMsg");
			String warnMsg = msg.msg_body;
			if (!Utils.isNull(warnMsg)) {
				Message message = new Message();
				message.what = SYSTEM_WARN;
				message.obj = warnMsg;
				handler.sendMessage(message);
			}
		}
	}

	private void dealSystemMsg(ServerRsp serverRsp) {
		if (serverRsp != null && serverRsp.getStatusCode() != null) {
			switch (serverRsp.getStatusCode()) {
			case SYSTEM_MSG_LOGIN_OTHER: {
				if (Utils.debug) {
					Log.e(TAG, "收到被迫下线的系统通知");
				}
				this.system_notify_content = this
						.getString(R.string.other_login);
				this.system_notify_title = this
						.getString(R.string.other_login_title);
				Message m = new Message();
				m.what = SYSTEM_NOTIFY;
				handler.sendMessage(m);
				break;
			}
			case SYSTEM_MSG_SYS_DIALOG: {
				this.system_dialog_msg = serverRsp.getString("msg");
				this.system_dialog_title = serverRsp.getString("title");
				Message m = new Message();
				m.what = this.SYSTEM_MSG_DIALOG;
				handler.sendMessage(m);
				break;
			}
			case THE_BLACK_DEVICE: {
				this.black_device_msg = this
						.getString(R.string.black_device_msg);
				this.black_device_title = this
						.getString(R.string.black_device_title);
				Message m = new Message();
				m.what = BLACK_DEVICE;
				handler.sendMessage(m);
				break;
			}
			}
		}

	}

	/** 处理转发信息的返回 */
	private void dealWapShare(ServerRsp serverRsp) {
		if (shareProgressDialog != null) {
			shareProgressDialog.cancel();
			shareProgressDialog = null;
		}
		if (serverRsp != null) {
			switch (serverRsp.getStatusCode()) {
			case RSP_OK: {
				if (shareList != null) {
					for (WapShare wapShare : shareList) {
						if (wapShare != null) {
							TXMessage txMsg = wapShare.getTxMsg();
							wapShare.setUrl(serverRsp.getString("url"));
							if (txMsg != null
									&& String.valueOf(txMsg.msg_id).equals(
											serverRsp.getString("sn"))) {
								showShareDialog(wapShare);
								break;
							}
						}
					}
				}

				break;
			}
			default: {
				showToast(R.string.request_forward_failed);
				break;
			}
			}
		}
	}

	private AlertDialog dialogblack;

	private void startPromptDialog(String system_dialog_title,
			String system_dialog_msg) {
		if (dialogblack == null
				|| (dialogblack != null && !dialogblack.isShowing())) {
			dialogblack = new AlertDialog.Builder(BaseActivity.this).create();
			dialogblack.setTitle(system_dialog_title);
			dialogblack.setMessage(system_dialog_msg);
			dialogblack.setCancelable(false);
			dialogblack.setButton(
					BaseActivity.this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							NotificationPopupWindow
									.cancelNotification(BaseActivity.this);
						}
					});
			dialogblack.show();
		}
	}

	private void startPromptDialogOtherLogin(String titleSource, String msg) {
		if (promptDialogOtherlogin == null
				|| (promptDialogOtherlogin != null && !promptDialogOtherlogin
						.isShowing())) {
			promptDialogOtherlogin = new AlertDialog.Builder(
					getParent() != null ? getParent() : this).create();
			promptDialogOtherlogin.setTitle(titleSource);
			promptDialogOtherlogin.setMessage(msg);
			promptDialogOtherlogin.setCancelable(false);
			promptDialogOtherlogin.setButton(
					BaseActivity.this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();

							exitLogin();// 和手动退出登录保持一致

							// Intent i = new Intent(BaseActivity.this,
							// LoginActivity.class);
							// i.putExtra("needinit", true);
							// startActivity(i);
							// // TxData.finishAll();
							// 干嘛要finishAll，还是在开启activity之后呢？
							// NotificationPopupWindow
							// .cancelNotification(BaseActivity.this);
						}
					});
			promptDialogOtherlogin.show();
		}
	}

	private AlertDialog promptDialogBlackDevice;

	private void startPromptBlackDevice(String titleSource, String msg) {
		if (promptDialogBlackDevice == null
				|| (promptDialogBlackDevice != null && !promptDialogBlackDevice
						.isShowing())) {
			promptDialogBlackDevice = new AlertDialog.Builder(BaseActivity.this)
					.create();
			promptDialogBlackDevice.setTitle(titleSource);
			promptDialogBlackDevice.setMessage(msg);
			promptDialogBlackDevice.setCancelable(false);
			promptDialogBlackDevice.setButton(
					BaseActivity.this.getString(R.string.confirm),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							TxData.finishAll();
							NotificationPopupWindow
									.cancelNotification(BaseActivity.this);
						}
					});
			promptDialogBlackDevice.show();
		}
	}

	private class DialogAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Item> items;
		private TXMessage txMsg;
		private WapShare wapShare;

		public DialogAdapter() {
			mInflater = getLayoutInflater();
			items = new ArrayList<Item>();
		}

		@Override
		public int getCount() {
			return items.size();
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		public void setData(List<Item> list) {
			items = list;
		}

		public void setTxMsg(TXMessage txMsg) {
			this.txMsg = txMsg;
		}

		public void setWapShare(WapShare wapShare) {
			this.wapShare = wapShare;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.share_dialog_item,
						null);
				holder = new ViewHolder();
				holder.textView = (TextView) convertView
						.findViewById(R.id.text);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			final Item item = items.get(position);
			holder.textView.setText(item.label);
			// holder.imageView.setImageDrawable(item.drawable);
			holder.imageView.setBackgroundDrawable(item.drawable);
			convertView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (item.label.equals(getString(R.string.app_name))) {
						Intent intent = new Intent(BaseActivity.this,
								SelectFriendListActivity.class);
						intent.putExtra(
								SelectFriendListActivity.CHAT_TYPE_ZF_OGJ,
								txMsg);
						intent.putExtra(SelectFriendListActivity.CHAT_TYPE,
								SelectFriendListActivity.CHAT_TYPE_ZF);
						startActivity(intent);
						// 把聊天室的输入类型变成文字输入，避免转发文字时到聊天室，文字信息被语音录制按钮隐藏掉 2014.01.13
						// shc
						Utils.isSendText = true;
						Utils.saveAutoPlayAdiouData(thisContext);
					} else {
						Intent intent = new Intent(Intent.ACTION_SEND);
						intent.setType("text/plain");
						intent.putExtra(Intent.EXTRA_TEXT,
								wapShare.getSnsText(2));
						if (item.label.contains(getString(R.string.weibo))
								&& txMsg != null) {

							intent.putExtra(Intent.EXTRA_TEXT,
									wapShare.getSnsText(1));
							if (txMsg.msg_path != null) {
								if (Utils.debug) {
									Log.i(TAG, "转发图片地址：" + txMsg.msg_path);
								}
								File file = new File(txMsg.msg_path);
								if (file.exists()) {
									intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
									intent.putExtra(Intent.EXTRA_STREAM,
											Uri.fromFile(file));
									intent.setType("image/*");
								}
							}

						}

						String[] mails = getResources().getStringArray(
								R.array.mails);
						for (String mail : mails) {
							if (item.label.contains(mail)) {
								intent.putExtra(Intent.EXTRA_TEXT,
										wapShare.getSnsText(3));
								intent.putExtra(Intent.EXTRA_SUBJECT,
										wapShare.getSubject());
								break;
							}
						}
						intent.setComponent(new ComponentName(item.packageName,
								item.className));
						startActivity(intent);
					}
					dialog.cancel();
				}
			});
			return convertView;
		}

	}

	private class Item {
		String label;
		Drawable drawable;
		String className;
		String packageName;
	}

	public class ViewHolder {
		ImageView imageView;
		TextView textView;
	}

	/** 显示转发的列表 */
	public void showShareDialog(WapShare wapShare) {
		Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		// intent.setType("image/*");
		PackageManager pm = getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(intent,
				PackageManager.MATCH_DEFAULT_ONLY);
		String[] blackApps = getResources().getStringArray(R.array.black_app);
		String[] items = new String[list.size()];
		List<Item> itemList = new ArrayList<Item>();
		for (int i = 0; i < list.size(); i++) {
			Item item = new Item();
			ResolveInfo resolveInfo = list.get(i);
			ActivityInfo activityInfo = resolveInfo.activityInfo;
			items[i] = resolveInfo.loadLabel(pm).toString();
			item.label = resolveInfo.loadLabel(pm).toString();
			item.drawable = activityInfo.loadIcon(pm);
			item.className = activityInfo.name;
			item.packageName = activityInfo.packageName;
			if (blackApps != null) {
				for (int j = 0; j < blackApps.length; j++) {
					if (item.label.contains(blackApps[j])) {
						break;
					}
					if (j == blackApps.length - 1)
						itemList.add(item);
				}
			}

		}
		Item item = new Item();
		item.label = getString(R.string.app_name);
		item.drawable = new BitmapDrawable(BitmapFactory.decodeResource(
				getResources(), R.drawable.icon));
		itemList.add(0, item);
		View view = getLayoutInflater().inflate(R.layout.share_dialog_list,
				null);
		ListView listView = (ListView) view.findViewById(R.id.list);
		dialogAdapter = new DialogAdapter();
		dialogAdapter.setData(itemList);
		dialogAdapter.setTxMsg(wapShare.getTxMsg());
		dialogAdapter.setWapShare(wapShare);
		listView.setAdapter(dialogAdapter);
		dialog = new AlertDialog.Builder(this).setTitle(R.string.share_type)
				.setView(view).create();
		dialog.show();
	}

	// 转发消息
	public void shareMsg(TXMessage txMsg) {
		if (txMsg != null) {
			if (txMsg.msg_type == TxDB.MSG_TYPE_GEO_SMS
					|| txMsg.msg_type == TxDB.MSG_TYPE_QU_GEO_SMS
					|| txMsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS
					|| txMsg.msg_type == TxDB.MSG_TYPE_QU_TCARD_SMS
					|| txMsg.msg_type == TxDB.MSG_TYPE_CARD_EMS
					|| txMsg.msg_type == TxDB.MSG_TYPE_TCARD_SMS) {
				// 地理位置和名片的转发暂时采用这种形式 2014.01.07 shc
				Intent intent = new Intent(BaseActivity.this,
						SelectFriendListActivity.class);
				intent.putExtra(SelectFriendListActivity.CHAT_TYPE_ZF_OGJ,
						txMsg);
				intent.putExtra(SelectFriendListActivity.CHAT_TYPE,
						SelectFriendListActivity.CHAT_TYPE_ZF);
				startActivity(intent);
			} else {
				if (shareList == null) {
					shareList = new ArrayList<WapShare>();
				}
				WapShare wapShare = new WapShare(txMsg);
				wapShare.setContext(this);
				shareList.add(wapShare);
				mSess.getSocketHelper().sendWapShare(
						wapShare.getContent(), wapShare.getType(),
						wapShare.getSn());
				shareProgressDialog = new ProgressDialog(this);
				shareProgressDialog
						.setMessage(getText(R.string.request_forward));
				shareProgressDialog.show();
			}
		}
	}

	private Timer baseTimer;
	private ProgressDialog baseDialog;

	public ProgressDialog showDialogTimer(Context context, int title,
			int content, int milliseconds, BaseTimerTask timerTask) {
		return showDialogTimer(context, title, this.getString(content),
				milliseconds, timerTask);
	}

	public ProgressDialog showDialogTimer(Context context, int title,
			int content, int milliseconds) {
		return showDialogTimer(context, title, this.getString(content),
				milliseconds, new BaseTimerTask() {

					@Override
					public void run() {
						super.run();
						runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(BaseActivity.this,
										R.string.request_outtime,
										Toast.LENGTH_SHORT).show();
							}
						});
					}
				});
	}

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

	public class BaseTimerTask extends TimerTask {

		@Override
		public void run() {
			cancelDialog();
		}

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
				if (Utils.debug)
					Log.i(TAG, "取消定时器有异常", e);
			}
			baseTimer = null;
		}
	}

	public void cancelDialogTimer() {
		cancelDialog();
		cancelTimer();
	}

	/** 短时间显示吐司 */
	protected void showToast(String content) {
		showToast(content, true);
	}

	/** 短时间显示吐司 */
	protected void showToast(int resId) {
		showToast(resId, true);
	}

	/** 发送消息，显示吐司,isShortShow: true:显示短时间，false:显示长时间 */
	protected void showToast(String content, boolean isShortShow) {
		Message msg = Message.obtain();
		msg.what = SHOW_TOAST;
		msg.obj = content;
		msg.arg1 = isShortShow ? 0 : 1;
		handler.sendMessage(msg);
	}

	/** 发送消息，显示吐司,isShortShow: true:显示短时间，false:显示长时间 */
	protected void showToast(int resId, boolean isShortShow) {
		Message msg = Message.obtain();
		msg.what = SHOW_TOAST;
		msg.obj = getResources().getString(resId);
		msg.arg1 = isShortShow ? 0 : 1;
		handler.sendMessage(msg);
	}

	// 用一个队列管理个人和群头像的回调有问题吗？
	protected Handler mAvatarHandler = new Handler() {
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

	private static Handler mPartnerAsynloader;
	protected static Handler mGroupAsynloader;
	public SessionManager mSess = SessionManager.getInstance();

	// 获取上传下载manager
	protected FileTransfer getDownUpMgr() {
		return mSess.mDownUpMgr;
	}

	protected void prepairAsyncload() {
		if (mPartnerAsynloader != null
				&& mGroupAsynloader != null) {
			return;
		}
		// mPartnerAsynloader = new Handler(mHandlerThread.getLooper()) {
		// @Override
		// public void handleMessage(Message msg) {
		// CallInfo ci;
		// switch (msg.what) {
		// case 1:
		// ci = (CallInfo) msg.obj;
		// if (!TextUtils.isEmpty(ci.mUrl)) {
		// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
		// ci.mUid, false);
		// if (file != null) {
		// File avatar = new File(file);
		// if (avatar.exists()) {
		// Bitmap bitmap = Utils.readBitMap(file, false);
		// if (bitmap != null) {
		// ci.mBitmap = cachePartnerBitmap(ci.mUid,
		// bitmap);
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
		// ci.mBitmap = cachePartnerBitmap(
		// taskInfo.mSrcId, bitmap);
		// mAvatarHandler.obtainMessage(1,
		// taskInfo.mObj).sendToTarget();
		// }
		// }
		// }, ci);
		// break;
		// case 2:
		// ci = (CallInfo) msg.obj;
		// if (!TextUtils.isEmpty(ci.mUrl)) {
		// String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
		// ci.mUid, false);
		// if (file != null) {
		// File avatar = new File(file);
		// if (avatar.exists()) {
		// Bitmap bitmap = Utils.readBitMap(file, false);
		// if (bitmap != null) {
		// ci.mBitmap = cachePartnerBitmap_nearlyGv(
		// ci.mUid, bitmap);
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
		// ci.mBitmap = cachePartnerBitmap_nearlyGv(
		// taskInfo.mSrcId, bitmap);
		// mAvatarHandler.obtainMessage(1,
		// taskInfo.mObj).sendToTarget();
		// }
		// }
		// }, ci);
		// break;
		// }
		// }
		// };
		mGroupAsynloader = new Handler(SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				CallInfo ci;
				switch (msg.what) {
				case 1:
					ci = (CallInfo) msg.obj;
					ci.mBitmap = getGroupCachedBitmap(ci.mUid);
					if (ci.mBitmap != null) {
						mAvatarHandler.obtainMessage(1, ci).sendToTarget();
						break;
					} else if (ci.mUrl != null) {
						String file = mSess.mDownUpMgr.getAvatarFile(ci.mUrl,
								true, ci.mUid, false);
						if (file != null) {
							File avatar = new File(file);
							if (avatar.exists()) {
								Bitmap bitmap = Utils.readBitMap(file, false);
								if (bitmap != null) {
									ci.mBitmap = cacheGroupBitmap(ci.mUid,
											bitmap);
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
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = Utils.readBitMap(
											taskInfo.mFullName, false);
									if (bitmap != null) {
										CallInfo ci = (CallInfo) taskInfo.mObj;
										ci.mBitmap = cacheGroupBitmap(
												taskInfo.mSrcId, bitmap);
										mAvatarHandler.obtainMessage(1,
												taskInfo.mObj).sendToTarget();
									}
								}
							}, ci);
					break;
				}
			}
		};
	}

	// protected void stopAsyncload() {
	// mHandlerThread.quit();
	// mHandlerThread = null;
	// //这个置空先注掉，这里有个问题，当在单聊页面，这是有一条群聊消息通知，点击通知栏进入群聊消息页面会报空指针，因为单聊的onDestroy方法在群聊的onCreat方法执行之后进行
	// //该变量是静态的，两个聊天室在任务栈中相邻就容易出现问题。还是不置为空了
	// // mAsynloader = null;
	// }

	// 个人头像缓存
	// private static HashMap<Long, SoftReference<Bitmap>> mPartnerAvatarCache =
	// AvatarDownload.mPartnerAvatarCache;
	// 群头像缓存
	private static HashMap<Long, SoftReference<Bitmap>> mGroupAvatarCache = new HashMap<Long, SoftReference<Bitmap>>();

	// 部分adapter中要用BaseActivity对象调用该方法，故需要用public
	// public static Bitmap getPartnerCachedBitmap(long tx_partner_id) {
	// // 个人头像
	// synchronized (mPartnerAvatarCache) {
	// SoftReference<Bitmap> soft = mPartnerAvatarCache.get(tx_partner_id);
	// return (soft != null) ? soft.get() : null;
	// }
	// }

	protected static Bitmap getGroupCachedBitmap(long group_id) {
		// 群头像
		synchronized (mGroupAvatarCache) {
			SoftReference<Bitmap> soft = mGroupAvatarCache.get(group_id);
			return (soft != null) ? soft.get() : null;
		}
	}

	// protected Bitmap cachePartnerBitmap(long tx_partner_id, Bitmap bitmap) {
	// synchronized (mPartnerAvatarCache) {
	// bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
	// mPartnerAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
	// bitmap));
	// return bitmap;
	// }
	// }

	// protected Bitmap cachePartnerBitmap_nearlyGv(long tx_partner_id,
	// Bitmap bitmap) {
	// synchronized (mPartnerAvatarCache) {
	// // bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
	// mPartnerAvatarCache.put(tx_partner_id, new SoftReference<Bitmap>(
	// bitmap));
	// return bitmap;
	// }
	// }

	protected Bitmap cacheGroupBitmap(long group_id, Bitmap bitmap) {
		synchronized (mGroupAvatarCache) {
			bitmap = Utils.getRoundedCornerBitmap(bitmap);// 包装成圆角bitmap
			mGroupAvatarCache.put(group_id, new SoftReference<Bitmap>(bitmap));
			return bitmap;
		}
	}

	// // 部分adapter要直接用BaseActivity对象调用此方法，所以需要设置为public
	// public static void loadHeadImg(String avatar_url, long partner_id,
	// AsyncCallback<Bitmap> callback) {
	// CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
	// mPartnerAsynloader.obtainMessage(1, callinfo).sendToTarget();
	// }
	//
	// // 附近的人头像模式显示图片
	// public static void loadHeadImg_nearGv(String avatar_url, long partner_id,
	// AsyncCallback<Bitmap> callback) {
	// CallInfo callinfo = new CallInfo(avatar_url, partner_id, callback);
	// mPartnerAsynloader.obtainMessage(2, callinfo).sendToTarget();
	// }

	protected void loadGroupImg(String avatar_url, long group_id,
			AsyncCallback<Bitmap> callback) {
		CallInfo callinfo = new CallInfo(avatar_url, group_id, callback);
		mGroupAsynloader.obtainMessage(1, callinfo).sendToTarget();
	}

	/** 删除TX头像缓存,返回值为是否删除 */
	public boolean removeTXHeadImgCache(long partner_id) {
		SoftReference<Bitmap> removedImg = mSess.avatarDownload.mPartnerAvatarCache
				.remove(partner_id);
		if (removedImg != null) {
			removedImg.clear();
		}
		return removedImg != null;
	}

	/** 退出登录 */
	// 手动退出和被迫下线都走这个流程
	protected void exitLogin() {
		mSess.getSocketHelper().recovery();
		mSess.clear();
		if (TxDBContentProvider.getmOpenHelper() != null) {
			TxDBContentProvider.closeDB();// 加一个关闭数据库的操作 2013.11.06 shc
		}
		// getEditorMeme().putString(CommonData.EXIT,
		// CommonData.USER_EXIT).commit();
		mSess.mPrefMeme.exit.setVal(PrefsMeme.USER_EXIT).commit();
		Intent i = new Intent(thisContext, LoginActivity.class);
		i.putExtra(LoginActivity.NEED_INIT, true);
		startActivity(i);
		TxData.finishAll();
		NotificationPopupWindow.cancelNotification(thisContext);
	}

	/** 一个包装handler，持有activity的弱引用，避免出现内存泄露 */
	protected static class WrappedHandler extends Handler {
		public WeakReference<Activity> mActivity;

		WrappedHandler(Activity activity) {
			mActivity = new WeakReference<Activity>(activity);
		}

	};

}
