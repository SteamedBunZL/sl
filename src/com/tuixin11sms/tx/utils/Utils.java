package com.tuixin11sms.tx.utils;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ThumbnailUtils;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.activity.GalleryFileActivity;
import com.tuixin11sms.tx.activity.IndexActivity;
import com.tuixin11sms.tx.activity.MusicPlayActivity;
import com.tuixin11sms.tx.activity.SettingsPreference;
import com.tuixin11sms.tx.activity.UnkownFileActivity;
import com.tuixin11sms.tx.activity.VideoPlayActivity;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.contact.Contact;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.LogUtils;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Messages;
import com.tuixin11sms.tx.gif.gifOpenHelper;
import com.tuixin11sms.tx.group.GifTransfer;
import com.tuixin11sms.tx.group.GifTransfer.GifDownUploadListner;
import com.tuixin11sms.tx.group.GifTransfer.GifTaskInfo;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.ChatChannel;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.StatusCode;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;

public class Utils {
	public static final boolean debug = true;
	public static boolean test = false;// 不设置为final,debug版本支持切换正式/测试服务器
										// 2013.12.14 shc
	public static final int level = 2;// 默认等级
	public static final boolean lev = true;// 关于等级的测试数据开关

	public static final String AD_URL = "http://118.145.23.90:9080/siteOperation/";
	public static final String AD_PAGE = "adviseImg.jsp";
	// 自动登陆失败常量
	public static final String AUTOLOGINFAILURE = "autoLoginFailure";
	public static final int DISLOGINFAILURE = 0;
	public static final int FIDLOGINFAILURE = 1;
	public static final int DEFAULTLOGINFAILURE = 2;
	public static Bitmap tempBm;
	// 聊天界面数据接收常量
	public static final String GOOGLE_MAP_STR = "geo:";
	public static final String MSGROOM_TX = "tx";
	// public static final String MSGROOM_THREAD_ID = "threadId";
	// 原来逻辑是开启聊天室时，就把之前的单聊和群聊都关闭，那么这个变量就没有意义了。
	// public static final String MSGROOM_WINDOW_IN = "launch_tuixin";
	public static final String MSGROOM_TX_GROUP = "txgroup";
	public static final String MSGROOM_GROUP_ID = "groupId";
	public static final String CHANGE_NAME = "changename";
	public static final String CHANGE_SIGN = "changesign";
	public static final String CHANGE_PROFESSION = "changeprofession";
	public static final String CHANGE_HOBBY = "changehobby";
	public static final String CHANNEL_LBS = "channel_lbs";
	public static final String CHANNEL_TXMESSAGE = "channel_txmessage";
	public static final String CHANNEL_DATA = "channel_data";
	public static final String INTO_CHANNELROOM_TYPE = "into_channelroom_type";
	public static final String INTO_CHANNELROOM_CHANNELLIST = "into_channelroom_channellist";
	public static final String INTO_CHANNELROOM_MESSAGE = "into_channelroom_message";
	public static String xf_id = "";// 这个是当前单聊的id，notification通过当前id判断是否在通知栏通知
									// 2014.01.07 shc
	public static long xf_group_id = -1;// 这个是当前群聊天室的id，notification通过当前id是否在通知栏通知
										// 2014.01.07 shc
	// 是否成功同步新浪个人资料数据
	public static boolean SINA_SUCCESS_SYNC_DATA;
	public static List<TX> txList;
	public static TxGroup waitSaveTxGroup;
	public static int roomstate = 0;
	public static int msgroom_big_resolution = 1000 * 1000;
	public static int msgroom_small_resolution = 640 * 640;
	public static int msgroom_list_resolution = 240 * 320;
	public static int wxb_default = 640 * 640;
	public static final int IN_SINGLE_ROOM = 1;
	public static final int IN_GROUP_ROOM = 2;
	public static final int IN_OTHER_ROOM = 0;
	public static final int HEADSET_IN = 1;
	public static final int HEADSET_OUT = 0;
	public static long roomid = -1;// 应该是当前所在的群聊天室群id或者单聊的partner_id 2014.01.07
									// shc
	public static String sms_adiou_name = "sl";
	public static String specilStr = "*()";

	public static final int INPUT_USERNAME_MAX_LENGTH = 24;
	public static final int INPUT_GROUPNAME_MAX_LENGTH = 12;

	// public static long st = 0;
	static final String TAG = "Utils";
	public static String osv = android.os.Build.VERSION.RELEASE;
	public static String os = "Android";
	public static String app = "shenliao";
	public static int appver = 41000;
	public static int tuixinver = 41000;
	public static String apptype = "4.1.0";
	public static final int DEFAULT_NUMBER = -1;
	public static Random g_rand = null;
	public static int msgcot;
	public static PopupWindow popupWindow;
	public static String IMAGE_CAMRA = "image_camra";// 这个变量干嘛用啊？单词都拼错了。照相机会识别吗？
														// 2013.12.30
	// public static int exitStep;//没什么用 2014.03.04

	/** 已收到的新增文件数的记录文件名字 */
	public static final String NEW_FILE_COUNT_FILE = "receivedNewFileCount.json";

	public static boolean isNotificationShow = true;// 在聊天界面是否出现通知
	public static boolean isClick = true;

	public static String hostUrl;
	public static String LBSURL;

	public static String GIF_SERVER;
	public static String AVATAR_SERVER;
	public static String IMAGE_SERVER;
	public static String AUDIO_SERVER;
	public static String VCARD_SERVER;
	public static int AVATAR_SERVER_PORT;
	public static int IMAGE_SERVER_PORT;
	public static int AUDIO_SERVER_PORT;
	public static int VCARD_SERVER_PORT;
	public static int GIF_SERVER_PORT;

	static {
		// if (Utils.test) {
		// // 内网
		// hostUrl = "http://118.145.23.21:80/index.php?";
		// LBSURL = "http://192.168.16.168/api/serverlist";
		// AVATAR_SERVER = IMAGE_SERVER = AUDIO_SERVER = VCARD_SERVER =
		// "118.145.23.22";
		// AVATAR_SERVER_PORT = IMAGE_SERVER_PORT = AUDIO_SERVER_PORT =
		// VCARD_SERVER_PORT = 8889;
		// } else {
		// hostUrl = "http://t.tuibo.com/index.php?";
		// LBSURL = "http://geo.clientapi.shenliao.com/api/serverlist";
		// AVATAR_SERVER = "tsm1.clientapi.shenliao.com";
		// IMAGE_SERVER = "tsm2.clientapi.shenliao.com";
		// AUDIO_SERVER = "tsm3.clientapi.shenliao.com";
		// VCARD_SERVER = "tsm4.clientapi.shenliao.com";
		// AVATAR_SERVER_PORT = IMAGE_SERVER_PORT = AUDIO_SERVER_PORT =
		// VCARD_SERVER_PORT = 80;
		// }

		loadHostAndPort();
	}

	/** 加载host地址和端口 */
	public static void loadHostAndPort() {
		if (Utils.test) {
			// 内网
			hostUrl = "http://118.145.23.21:80/index.php?";
			LBSURL = "http://192.168.16.168/api/serverlist";
			AVATAR_SERVER = IMAGE_SERVER = AUDIO_SERVER = VCARD_SERVER = "118.145.23.22";
			AVATAR_SERVER_PORT = IMAGE_SERVER_PORT = AUDIO_SERVER_PORT = VCARD_SERVER_PORT = 8889;
			GIF_SERVER_PORT = 8830;
		} else {
			hostUrl = "http://t.tuibo.com/index.php?";
			LBSURL = "http://geo.clientapi.shenliao.com/api/serverlist";
			AVATAR_SERVER = "tsm1.clientapi.shenliao.com";
			IMAGE_SERVER = "tsm2.clientapi.shenliao.com";
			AUDIO_SERVER = "tsm3.clientapi.shenliao.com";
			VCARD_SERVER = "tsm4.clientapi.shenliao.com";
			GIF_SERVER = "118.145.23.22";
			GIF_SERVER_PORT = 8830;
			AVATAR_SERVER_PORT = IMAGE_SERVER_PORT = AUDIO_SERVER_PORT = VCARD_SERVER_PORT = 80;

		}

	}

	public static int SreenW;// 显示屏宽度
	public static int SreenH;// 显示屏高度
	public static boolean isMoreDefault;

	public static boolean isOpenPlayAdiou = true;// 是否打开自动播放
	public static boolean isSendText = true;// 是否发送文本 true是文本 false 是录音
	public static boolean isHandset = false;// 是否听筒模式
	public static boolean isRrecord = true;// 应该为是否可以录音，true为可以录

	public static int CAN_NOT_DOWNLOAD_LENGTH = 5; // 如果头像地址长度小于这个值，说明是默认头像，不必去服务器下载
	// public static String hostUrl = "http://t.tuibo.net/index.php?";//内网地址
	// ThreadPoolExecutor threadpool;
	// public static Thread getThread(){
	// threadpool=ThreadPoolExecutor.
	// }
	public static int accost_tag = 10011;
	public static int lbs_tag = 10022;
	public static ExecutorService executorService = Executors
			.newCachedThreadPool();

	private static TxData txdata;// Utils在整个进程可用的txdata

	// 在TxData 的 onCreate中初始化context
	public static void initContext(TxData ttxdata) {
		txdata = ttxdata;
	}

	public static boolean arrayEquals(String[] a, String[] b) {
		if (a == null)
			return false;
		if (b == null)
			return false;
		boolean flag = false;
		if (a.length != b.length) {
			return false;
		} else {
			for (int i = 0; i < a.length; i++) {
				if (!a[i].equals(b[i])) {
					flag = false;
					break;
				} else {
					flag = true;
				}
			}
		}
		return flag;
	}

	public static long createMsgId(String id) {

		Random l_x = new Random();
		if (g_rand == null) {
			g_rand = new Random();
		}

		String ret;
		long temp = l_x.nextLong() & 0x7fffffffffffffffl;
		long temp2 = g_rand.nextLong() & 0x7fffffffffffffffl;
		long temp_xor = temp ^ temp2;
		String str1 = String.valueOf(1 + temp_xor % 9);
		ret = str1;

		for (int i = 0; i < 9; ++i) {
			temp = l_x.nextLong() & 0x7fffffffffffffffl;
			temp2 = g_rand.nextLong() & 0x7fffffffffffffffl;
			temp_xor = temp ^ temp2;
			String str2 = String.valueOf(temp_xor % 10);
			ret += str2;
		}
		StringBuffer sb = new StringBuffer(ret);
		int b = (8 - id.length());
		for (int i = 0; i < b; i++) {
			sb.append("0");
		}
		sb.append(id);
		if (Utils.debug)
			Log.i("Utils", "createMsgId : " + sb.toString());

		/*
		 * StringBuffer sb = new StringBuffer(id); int b = (8 - id.length());
		 * for (int i = 0; i < b; i++) { sb.append("0"); } String time = "" +
		 * System.currentTimeMillis(); time = time.substring(time.length() - 8);
		 * sb.append(time); if (msgcot == 100) { msgcot = 0; } if (msgcot < 10)
		 * { sb.append("0").append(msgcot); } else { sb.append(msgcot); }
		 * msgcot++;
		 */
		return Long.parseLong(sb.toString());

	}

	public static boolean isMoreMan(String[] phones) {
		if (phones != null && phones.length > 1) {
			return true;
		}
		return false;
	}

	public static boolean checkApkExist(Context context, String packageName) {
		if (isNull(packageName))
			return false;
		try {
			context.getPackageManager().getApplicationInfo(packageName,
					PackageManager.GET_UNINSTALLED_PACKAGES);
			return true;
		} catch (NameNotFoundException e) {
			return false;
		}
	}

	/**
	 * 弹出软键盘
	 * 
	 * @param c
	 */
	public static void popSoftInput(final Context c) {
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			public void run() {
				InputMethodManager imm = (InputMethodManager) c
						.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
				// imm.isActive()
			}

		}, 200);
	}

	public static void softInput(final Context c) {
		InputMethodManager imm = (InputMethodManager) c
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);

	}

	public static void openKeyboard(Context context, View view) {
		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
		imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT);
	}

	/**
	 * 隐藏软键盘
	 */
	public static void hideSoftInput(final Activity a) {

		InputMethodManager inputManager = (InputMethodManager) a
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(a.getCurrentFocus()
				.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
	}

	public static void hideSoftInput(Context context, View v) {

		InputMethodManager imm = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
	}

	/**
	 * 为程序创建桌面快捷方式
	 * 
	 * 同时需要在manifest中设置以下权限： <uses-permission
	 * android:name="com.android.launcher.permission.INSTALL_SHORTCUT" />
	 */
	public static void addShortcut(Context context) {
		Intent shortcut = new Intent(
				"com.android.launcher.action.INSTALL_SHORTCUT");
		// 快捷方式的名称
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));
		// 不允许重复创建
		shortcut.putExtra("duplicate", false);

		// 指定当前的Activity为快捷方式启动的对象: 如 com.everest.video.VideoPlayer
		// 这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)
		// String action = "com.android.action.tuixinsms";
		Intent respondIntent = new Intent(context, IndexActivity.class);
		respondIntent.setAction("android.intent.action.MAIN");
		respondIntent.addCategory("android.intent.category.LAUNCHER");
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, respondIntent);
		ShortcutIconResource iconRes = Intent.ShortcutIconResource.fromContext(
				context, R.drawable.icon_100);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconRes);

		context.sendBroadcast(shortcut);
	}

	/**
	 * 正则表达式匹配
	 * 
	 * @param content
	 * @param matchString
	 * @return
	 */
	public static boolean matchStr(String content, String matchString) {
		Pattern pattern = Pattern.compile(matchString);
		Matcher matcher = pattern.matcher(content);
		if (matcher.find())
			return true;
		return false;
	}

	/**
	 * 匹配昵称
	 * 
	 * @param nickname
	 * @param context
	 * @return
	 */
	public static boolean matchNickname(String nickname, Context context) {
		if (!Utils.isNull(nickname)) {
			return matchStr(
					nickname,
					context.getResources().getString(
							R.string.regex_nickname_validation));
		}
		return false;
	}

	/**
	 * 过滤关键字符
	 * 
	 * @param string
	 * @return true 合法 false 不合法
	 */
	public static boolean filterStr(String string, Context context) {
		String[] arrays = context.getResources().getStringArray(
				R.array.fiflter_str);
		for (String s : arrays) {
			if (string.indexOf(s) != -1) {
				return false;
			}
		}
		return true;
	}

	/**
	 * 删除程序的快捷方式
	 * 
	 * 同时需要在manifest中设置以下权限： <uses-permission
	 * android:name="com.android.launcher.permission.UNINSTALL_SHORTCUT" />
	 */
	public static void delShortcut(Context context) {
		Intent shortcut = new Intent(
				"com.android.launcher.action.UNINSTALL_SHORTCUT");

		// 快捷方式的名称
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_NAME,
				context.getString(R.string.app_name));

		// 指定当前的Activity为快捷方式启动的对象: 如 com.everest.video.VideoPlayer
		// 这里必须为Intent设置一个action，可以任意(但安装和卸载时该参数必须一致)
		String action = "com.android.action.tuixinsms";
		Intent respondIntent = new Intent(context, IndexActivity.class);
		respondIntent.setAction(action);
		shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, respondIntent);
		// 下面的方法与上面的效果是一样的,另一种构建形式而已
		// 注意: ComponentName的第二个参数必须加上点号(.)，否则快捷方式无法启动相应程序
		// String appClass = this.getPackageName() + "." +
		// this.getLocalClassName();
		// ComponentName comp = new ComponentName(this.getPackageName(),
		// appClass);
		// shortcut.putExtra(Intent.EXTRA_SHORTCUT_INTENT, new
		// Intent(action).setComponent(comp));

		context.sendBroadcast(shortcut);
	}

	private static final Random rand = new Random();

	public static byte[] buildKey() {
		int keylength = 16;
		byte key[] = new byte[keylength];
		for (int i = 0; i < keylength; i++) {
			key[i] = (byte) rand.nextInt(256);
		}
		return key;
	}

	public static final String rootPath = "/shenliao";
	/**
	 * 头像目录
	 */
	public static final String AVATAR_PATH = "avatar";
	/**
	 * 聊天图片目录
	 */
	public static final String IMAGE_PATH = "image";
	/**
	 * Vcard目录
	 */
	public static final String VCARD_PATH = "cvf";
	/**
	 * 音频目录
	 */
	public static final String AUDIO_PATH = "audio";
	/**
	 * 图片保存目录
	 */
	public static final String PHOTO_IMAGE_PATH = "sheliao";

	public static boolean isNull(String str) {
		if (str == null || str.trim().equals(""))
			return true;
		return false;
	}

	public static boolean isLangNull(String str) {
		if (str == null || str.trim().equals("")) {
			return true;
		} else {
			List<String> mlist = StringUtils.str2List(str);
			if (mlist.contains("0") && mlist.size() == 1) {
				return true;
			}
		}
		return false;
	}

	public static boolean isGeoNull() {
		if (TxData.public_latitude == 0 && TxData.public_longitude == 0)
			return true;
		return false;
	}

	public static boolean isIdValid(long num) {
		if (num > 0)
			return true;
		return false;
	}

	/** 是否是官方账号 */
	public static boolean isOfficialUser(long userId) {
		if (isIdValid(userId)) {
			if (userId == TX.TUIXIN_MAN || userId == TX.TUIXIN_FRIEND
					|| userId == TX.SL_GROUP_NOTICE || userId == TX.SL_SAFE) {
				return true;
			}
		}
		return false;
	}

	public static boolean checkSDCard() {
		if (Environment.getExternalStorageState().equals("mounted"))
			return true;
		else
			return false;
	}

	public static boolean checkSDCardAndPrompt(Context context) {
		if (Environment.getExternalStorageState().equals("mounted"))
			return true;
		else {
			Toast.makeText(context, R.string.no_sdcard_info_update,
					Toast.LENGTH_SHORT).show();
			return false;
		}

	}

	@SuppressWarnings("deprecation")
	public static boolean sendSmsMessage(String phoneNumber, String message) {
		try {
			if (Integer.parseInt(Build.VERSION.SDK) < 5) {
				android.telephony.gsm.SmsManager smsManager = android.telephony.gsm.SmsManager
						.getDefault();
				// PendingIntent mPI = PendingIntent.getBroadcast(Setting.this,
				// 0,
				// new Intent(), 0);
				// smsManager.sendTextMessage(phoneNumber, null, "xixihaha",
				// mPI,
				// null);
				if (message.length() > 70) {
					ArrayList<String> msgs = smsManager.divideMessage(message);
					for (String msg : msgs) {
						smsManager.sendTextMessage(phoneNumber, null, msg,
								null, null);
					}
				} else {
					smsManager.sendTextMessage(phoneNumber, null, message,
							null, null);
				}
			} else {
				android.telephony.SmsManager smsManager = android.telephony.SmsManager
						.getDefault();
				// PendingIntent mPI = PendingIntent.getBroadcast(Setting.this,
				// 0,
				// new Intent(), 0);
				// smsManager.sendTextMessage(phoneNumber, null, "xixihaha",
				// mPI,
				// null);
				if (message.length() > 70) {
					ArrayList<String> msgs = smsManager.divideMessage(message);
					for (String msg : msgs) {
						smsManager.sendTextMessage(phoneNumber, null, msg,
								null, null);
					}
				} else {
					smsManager.sendTextMessage(phoneNumber, null, message,
							null, null);
				}
			}

			// Toast.makeText(Setting.this, "短信发送完成", Toast.LENGTH_LONG).show();
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
			return false;
		}
		return true;
	}

	public static boolean checkSIMCardState(Context context) {
		TelephonyManager telMgr = (TelephonyManager) context
				.getSystemService(Context.TELEPHONY_SERVICE);
		if (telMgr.getSimState() == TelephonyManager.SIM_STATE_ABSENT) {
			// 无sim 卡
			return false;
			/*
			 * new AlertDialog.Builder(Setting.this).setTitle(R.string.alert).
			 * setIcon
			 * (R.drawable.icon).setMessage(R.string.nosimcard).setNeutralButton
			 * (R.string.confirm, new DialogInterface.OnClickListener(){ public
			 * void onClick(DialogInterface dialog, int which) { finishMe(); }
			 * 
			 * }).show();
			 */
		} else if (!(telMgr.getSimState() == TelephonyManager.SIM_STATE_READY)) {
			// sim卡被锁定或未知的状态
			return false;
			/*
			 * new
			 * AlertDialog.Builder(TeleSmsMotoCliq.this).setTitle(R.string.alert
			 * ). setIcon(R.drawable.icon).setMessage(R.string.simcardnoready).
			 * setNeutralButton(R.string.confirm, new
			 * DialogInterface.OnClickListener(){ public void
			 * onClick(DialogInterface dialog, int which) { finishMe(); }
			 * 
			 * }).show();
			 */
		}
		return true;
	}

	private static String storagePath = "";

	public static String getStoragePath(Context appContext) {
		try {
			if (Environment.getExternalStorageState().equals("mounted")) {// Environment.MEDIA_MOUNTED
				storagePath = Environment.getExternalStorageDirectory()
						.getAbsolutePath() + rootPath;
			} else {
				storagePath = appContext.getFilesDir().getAbsolutePath()
						+ rootPath;
			}
		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "内存卡上创建神聊目录异常", e);
			return null;
		}
		return storagePath;
	}

	public static Bitmap zoomBitmap(Bitmap bm, int w, int h) {
		int width = bm.getWidth();
		int height = bm.getHeight();
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ((float) w / width); // 计算缩放比例
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true));
		return wref.get();
	}

	private static final String NET_TYPE_WIFI = "WIFI";
	public static final int NET_NOT_AVAILABLE = 0;
	public static final int NET_WIFI = 1;
	public static final int NET_PROXY = 2;
	public static final int NET_NORMAL = 3;

	/**
	 * 判断网络连接是否可用
	 */
	public static boolean checkNetworkAvailable(Context inContext) {
		int type = getNetworkType(inContext);
		if (type == NET_NOT_AVAILABLE || type == NET_PROXY) {
			return false;
		}
		return true;
	}

	/**
	 * 判断网络连接是否可用
	 */
	public static boolean checkNetworkAvailable1(Context context) {
		int type = getNetworkType(context);
		if (type == NET_NOT_AVAILABLE) {
			return false;
		}
		return true;
	}

	/**
	 * 获取网络连接类型
	 */
	public synchronized static int getNetworkType(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			// 当前网络不可用
			networkType = NET_NOT_AVAILABLE;
		} else {
			// 如果当前是WIFI连接
			if (NET_TYPE_WIFI.equals(networkinfo.getTypeName())) {
				networkType = NET_WIFI;
			}
			// 非WIFI联网
			else {
				String proxyHost = android.net.Proxy.getDefaultHost();
				// 代理模式
				if (proxyHost != null) {
					networkType = NET_PROXY;
				}
				// 直连模式
				else {
					networkType = NET_NORMAL;
				}
			}
		}
		return networkType;
	}

	/**
	 * 网络类型, 以下的几个方法都是根据TuixinService1中的广播设置的值处理网络连接的
	 */
	private volatile static int networkType = NET_NOT_AVAILABLE;

	/**
	 * 接收到网络改变的广播时调用该方法
	 * 
	 * @param network_Type
	 */
	public synchronized static void setNetworkType(int network_Type) {
		networkType = network_Type;
	}

	public synchronized static int getNetworkTypeSimple() {
		return networkType;
	}

	public synchronized static boolean checkNetworkAvailableSimple() {
		int type = getNetworkTypeSimple();
		if (type == NET_NOT_AVAILABLE || type == NET_PROXY) {
			return false;
		}
		return true;
	}

	/** 通过bitmap的宽高，和要显示的最大尺寸来计算出放大比例 */
	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);
		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	private static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == -1) ? 1 : (int) Math.ceil(Math
				.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == -1) ? 128 : (int) Math.min(
				Math.floor(w / minSideLength), Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}
		if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
			return 1;
		} else if (minSideLength == -1) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	public static String getIMSI(Context ctxt) {
		TelephonyManager tm = (TelephonyManager) ctxt
				.getSystemService(Context.TELEPHONY_SERVICE);
		return tm.getSubscriberId(); // 取出IMSI
	}

	private static String deviceId = null;

	public static String getImei_Iccid(Context ctxt) {
		if (deviceId != null)
			return deviceId;
		deviceId = TxData.getDeviceId();
		if (!Utils.isNull(deviceId)) {
			return deviceId;
		}

		TelephonyManager tm = (TelephonyManager) ctxt
				.getSystemService(Context.TELEPHONY_SERVICE);
		String imei = "" + tm.getDeviceId(); // 取出IMEI
		if (Utils.debug) {
			// 如果在debug状态下，给模拟器写一个imei。是公司TB-SJ-0027-三星i9250的设备id
			if (imei.equals("000000000000000")) {
				imei = "351554052411569";
			}
		}
		if (imei != null && imei.length() >= 15) {
			Pattern pattern = Pattern.compile("([\\d])\\1{4}");
			Matcher matcher = pattern.matcher(imei.substring(0, 5));
			if (!matcher.find())
				deviceId = imei;
			TxData.saveDeviceId(deviceId);
			return imei;
		}
		String simid = "" + tm.getSimSerialNumber(); // 取出ICCID
		if (simid != null && simid.length() >= 20) {
			deviceId = simid;
			TxData.saveDeviceId(deviceId);
			return simid;
		}
		String imsi = "" + tm.getSubscriberId(); // 取出IMSI
		if (imsi != null && imsi.length() >= 20) {
			deviceId = imsi;
			TxData.saveDeviceId(deviceId);
			return imsi;
		}
		WifiManager wifi = (WifiManager) ctxt
				.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifi.getConnectionInfo();
		String mac = info.getMacAddress();
		if (mac != null) {
			deviceId = mac;
			TxData.saveDeviceId(deviceId);
			return mac;
		}
		final String androidId;
		androidId = ""
				+ android.provider.Settings.Secure.getString(
						ctxt.getContentResolver(),
						android.provider.Settings.Secure.ANDROID_ID);
		String tel = "" + tm.getLine1Number(); // 取出MSISDN，很可能为空
		if (tel != null && tel.length() >= 11 && tel.length() <= 15) {
			UUID deviceUuid = new UUID(androidId.hashCode(),
					((long) imei.hashCode() << 32) | tel.hashCode());
			String uniqueId = deviceUuid.toString();
			deviceId = uniqueId;
			TxData.saveDeviceId(deviceId);
			return uniqueId;
		}
		return null;

	}

	public static String getUserNumber(String num) {
		if (num.startsWith("+86")) {
			num = num.substring(3);
		} else if (num.startsWith("86")) {
			num = num.substring(2);
		} else if (num.startsWith("0086")) {
			num = num.substring(4);
		}
		if (num.length() == 11) {
			return num;
		}
		return null;
	}

	public static String getUpNumber(String num) {
		if (num.startsWith("+")) {
			num = "00" + num.substring(1);
		}
		num = GetNumber(num);
		if (num.length() > 11 && num.length() < 20) {
			return num;
		}
		return null;
	}

	public static String getMccNumber(String num, String mcc) {
		if (num.startsWith("+86")) {
			num = num.substring(3);
		} else if (num.startsWith("86")) {
			num = num.substring(2);
		} else if (num.startsWith("0086")) {
			num = num.substring(4);
		} else if (num.startsWith("+")) {
			num = "00" + num.substring(1);
		} else if (num.startsWith("00")) {

		} else {
			num = mcc + num;
		}
		return num;
	}

	// 是否为手机号码
	public static boolean IsUserNumber(String num) {
		boolean re = false;
		if (num.startsWith("+86")) {
			num = num.substring(3);
		} else if (num.startsWith("86")) {
			num = num.substring(2);
		} else if (num.startsWith("0086")) {
			num = num.substring(4);
		}
		if (num.length() == 11) {
			if (num.startsWith("13")) {
				re = true;
			}
			if (num.startsWith("14")) {
				re = true;
			} else if (num.startsWith("15")) {
				re = true;
			} else if (num.startsWith("18")) {
				re = true;
			}
		}
		return re;
	}

	// 是否为手机号码
	public static boolean IsUpNumber(String num) {
		boolean re = false;
		if (num.startsWith("+")) {
			num = "00" + num.substring(1);
		}
		num = GetNumber(num);
		if (num.length() > 11 && num.length() < 20) {
			return true;
		}
		return re;
	}

	public static Long creatPhoneid(String phone, int personid) {
		if (phone.startsWith("+86")) {
			phone = phone.substring(3);
		} else if (phone.startsWith("86")) {
			phone = phone.substring(2);
		} else if (phone.startsWith("0086")) {
			phone = phone.substring(4);
		} else if (phone.startsWith("+")) {
			phone = phone.substring(1);
		}
		if (phone.length() > 15) {
			phone = phone.substring(0, 14);
		} else {
			int len = 13 - phone.length();
			if (len > 0) {
				StringBuffer sb = new StringBuffer(phone);
				for (int i = 0; i < len; i++)
					sb.append("0");
				phone = sb.toString();
			}
		}
		// 此时phone一定是一个长度15位的字符串
		phone += personid;
		return Long.parseLong(phone);
	}

	public static String filterNumber(String num) {
		num = GetNumber(num);
		if (num.startsWith("+86")) {
			num = num.substring(3);
		} else if (num.startsWith("86")) {
			num = num.substring(2);
		} else if (num.startsWith("0086")) {
			num = num.substring(4);
		} else if (num.startsWith("+")) {
			num = num.substring(1);
		}
		return num;
	}

	// 还原11位手机号
	public static String get11Number(String num) {
		String tel = num;
		if (tel != null) {
			tel = tel.replace(" ", "");
			tel = tel.replace("-", "");
			if (tel.startsWith("+86")) {
				tel = tel.substring(3);
			} else if (tel.startsWith("86")) {
				tel = tel.substring(2);
			} else if (tel.startsWith("12520")
					&& tel.length() != "12520".length()) {
				tel = tel.substring("12520".length());
			}
		} else {
			tel = "";
		}
		return tel;
	}

	public static String GetNumber(String num) {
		String tel = num;
		if (tel != null) {
			// if (tel.startsWith("+86")){
			// tel = tel.substring(3);
			// }else if (tel.startsWith("86")){
			// tel = tel.substring(2);
			// }
			StringBuffer sb = new StringBuffer();
			int len = tel.length();
			for (int i = 0; i < len; i++) {
				char c = tel.charAt(i);
				// if(c!='-' && c!=':' && c!=' ')
				if (c >= '0' && c <= '9' || (i == 0 && c == '+')) {
					sb = sb.append(c);
				}
			}
			tel = sb.toString();
		} else {
			tel = "";
		}
		return tel;
	}

	private static HashMap<Integer, String> mcc_code;
	public static HashMap<String, String> country_code;
	public static HashMap<Integer, String> mcc_country;
	public static String[] country = new String[] { "中国", "Afghanistan",
			"Albania", "Algeria", "American Samoa (US)", "Andorra", "Angola",
			"Anguilla", "Antigua and Barbuda", "Argentine Republic no",
			"Armenia", "Australia", "Austria", "Bahamas", "Bahrain",
			"Bangladesh", "Barbados", "Belarus", "Belgium", "Belize", "Benin",
			"Bermuda (UK)", "Bolivia", "Botswana", "Brazil",
			"Brunei Darussalam", "Bulgaria", "Burundi", "Cambodia", "Cameroon",
			"Canada", "Cayman Islands (UK)", "Central African Republic",
			"Chad", "Chile", "Colombia", "Republic of the Congo",
			"Cook Islands (NZ)", "Côte d'Ivoire", "Cuba", "Czech Republic",
			"Democratic Republic of the Congo", "Denmark", "Djibouti",
			"Dominica", "Ecuador", "Egypt", "El Salvador", "Estonia",
			"Ethiopia", "Fiji", "Finland", "France", "French Guiana (France)",
			"Gabonese Republic", "Gambia", "Georgia", "Ghana",
			"Gibraltar (UK)", "Greece", "Guam (US)", "Guatemala", "Guinea",
			"Guyana", "Haiti", "Honduras", "Hong Kong (PRC)", "Iceland",
			"India", "India", "Indonesia", "Iran", "Iraq", "Ireland", "Israel",
			"Italy", "Jamaica", "Japan", "Jordan", "Kazakhstan", "Kenya",
			"Korea, North", "Korea, South", "Kuwait", "Kyrgyz Republic",
			"Latvia", "Lebanon", "Lesotho", "Liberia", "Libya",
			"Liechtenstein", "Lithuania", "Luxembourg", "Macau (PRC)",
			"Madagascar", "Malawi", "Malaysia", "Maldives", "Mali", "Malta",
			"Mauritius", "Mexico", "Moldova", "Monaco", "Mongolia",
			"Montserrat (UK)", "Morocco", "Mozambique", "Namibia", "Nepal",
			"Netherlands", "Netherlands Antilles (Netherlands)", "New Zealand",
			"Nicaragua", "Niger", "Nigeria", "Norway", "Oman", "Panama",
			"Papua New Guinea", "Paraguay", "Philippines", "Poland",
			"Portugal", "Puerto Rico (US)", "Qatar", "Réunion (France)",
			"Romania", "Russian Federation", "Saint Lucia",
			"Saint Vincent and the Grenadines", "Samoa", "San Marino",
			"São Tomé and Príncipe", "Saudi Arabia", "Senegal", "Seychelles",
			"Sierra Leone", "Singapore", "Slovakia", "Slovenia",
			"Solomon Islands", "Somalia", "Spain", "Sri Lanka", "Sudan",
			"Suriname", "Swaziland", "Sweden", "Switzerland", "Syria",
			"Taiwan", "Tajikistan", "Tanzania", "Togolese Republic", "Tonga",
			"Trinidad and Tobago", "Tunisia", "Turkey", "Turkmenistan",
			"Ukraine", "United Arab Emirates", "United Arab Emirates",
			"United Kingdom", "United States of America",
			"United States of America" };
	public static int[] mcc = new int[] { 460, 412, 276, 603, 544, 213, 631,
			365, 344, 722, 283, 505, 232, 364, 426, 470, 342, 257, 206, 702,
			616, 350, 736, 652, 724, 528, 284, 642, 456, 624, 302, 346, 623,
			622, 730, 732, 629, 548, 612, 368, 230, 630, 238, 638, 366, 740,
			602, 706, 248, 636, 542, 244, 208, 742, 628, 607, 282, 620, 266,
			202, 535, 704, 611, 738, 372, 708, 454, 274, 404, 405, 510, 432,
			418, 272, 425, 222, 338, 440, 416, 401, 639, 467, 450, 419, 437,
			247, 415, 651, 618, 606, 295, 246, 270, 455, 646, 650, 502, 472,
			610, 278, 617, 334, 259, 212, 428, 354, 604, 643, 649, 429, 204,
			362, 530, 710, 614, 621, 242, 422, 714, 537, 744, 515, 260, 268,
			330, 427, 647, 226, 250, 358, 360, 549, 292, 626, 420, 608, 633,
			619, 525, 231, 293, 540, 637, 214, 413, 634, 746, 653, 240, 228,
			417, 466, 436, 640, 615, 539, 374, 605, 286, 438, 255, 424, 431,
			235, 310, 311 };
	public static String[] code = new String[] { "0086", "0093", "00355",
			"00213", "00684", "00376", "00631", "001264", "001268", "0054",
			"00374", "0061", "0043", "001242", "00973", "00880", "001246",
			"00375", "0032", "00501", "00229", "001441", "00591", "00267",
			"0055", "00673", "00359", "00257", "00855", "00237", "001",
			"001345", "00236", "00235", "0056", "0057", "00242", "00682",
			"00225", "0053", "00420", "00242", "0045", "00253", "001890",
			"00593", "0020", "00503", "00372", "00251", "00679", "00358",
			"0033", "00594", "00241", "00220", "00995", "00233", "00350",
			"0030", "001671", "00502", "00224", "00592", "00509", "00504",
			"00852", "00354", "0091", "0091", "0062", "0098", "00964", "00353",
			"00972", "0039", "001876", "0081", "00962", "00327", "00254",
			"00850", "0082", "00965", "00331", "00371", "00961", "00266",
			"00231", "00218", "00423", "00370", "00352", "00853", "00261",
			"00265", "0060", "00960", "00223", "00356", "00230", "0052",
			"00373", "00377", "00976", "001664", "00212", "00258", "00264",
			"00977", "0031", "00599", "0064", "00505", "00227", "00234",
			"0047", "00968", "00507", "00675", "00595", "0063", "0048",
			"00351", "001787", "00974", "00262", "0040", "007", "001758",
			"001784", "00685", "00378", "00239", "00966", "00221", "00248",
			"00232", "0065", "00421", "00386", "00677", "00252", "0034",
			"0094", "00249", "00597", "00268", "0046", "0041", "00963",
			"00886", "00992", "00255", "00228", "00676", "001809", "00216",
			"0090", "00993", "00380", "00971", "00971", "0044", "001", "001" };

	public static HashMap<Integer, String> getMccCode() {
		if (mcc_code == null) {
			mcc_code = new HashMap<Integer, String>();

			for (int i = 0; i < mcc.length; i++) {
				mcc_code.put(mcc[i], code[i]);
			}
		}
		return mcc_code;
	}

	public static int getMccPos(int mc) {
		for (int i = 0; i < mcc.length; i++) {
			if (mc == mcc[i]) {
				return i;
			}
		}
		return -1;
	}

	public static HashMap<Integer, String> getMccCountry() {
		if (mcc_country == null) {
			mcc_country = new HashMap<Integer, String>();

			for (int i = 0; i < mcc.length; i++) {
				mcc_country.put(mcc[i], country[i]);
			}
		}
		return mcc_country;
	}

	public static HashMap<String, String> getCountryCode() {
		if (country_code == null) {
			country_code = new HashMap<String, String>();
			for (int i = 0; i < country.length; i++) {
				country_code.put(country[i], code[i]);
			}
		}
		return country_code;
	}

	/**
	 * 字符串按字节截取
	 * 
	 * @param str
	 *            原字符
	 * @param len
	 *            截取长度
	 * @return String
	 * @author kinglong
	 * @since 2006.07.20
	 */
	public static String splitString(String str, int len) {
		return splitString(str, len, "...");
	}

	public static String splitString(String str, int len, String elide) {
		if (str == null) {
			return "";
		} else if (str.length() < len) {
			return str;
		}
		String s = str.substring(0, len) + elide.trim();
		return s;
	}

	public static File createFile(Bitmap bitmap, String fileName, boolean big) {
		String completeFileName;
		if (big)
			completeFileName = fileName + "_big.jpg";
		else
			completeFileName = fileName + ".jpg";

		File sddir = new File(storagePath + "/" + IMAGE_PATH + "/");
		if (!sddir.exists() && !sddir.mkdirs()) {
			// Log.e("bitmapFromUrl", "Create dir failed");
			return null;
		}

		File file = new File(storagePath + "/" + IMAGE_PATH + "/",
				completeFileName);
		if (!file.exists()) {
			// 创建文件输出流
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				// 存储
				bitmap.compress(CompressFormat.JPEG, 100, os);
				// 关闭流
			} catch (FileNotFoundException e) {
				if (Utils.debug)
					e.printStackTrace();
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (IOException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
			}
		}
		return file;
	}

	public static File createPhotoFile(Bitmap bitmap, String fileName) {

		File sddir = new File(storagePath + "/" + IMAGE_PATH + "/");
		if (!sddir.exists() && !sddir.mkdirs()) {
			// Log.e("bitmapFromUrl", "Create dir failed");
			return null;
		}

		File file = new File(storagePath + "/" + IMAGE_PATH + "/", fileName);
		if (!file.exists()) {
			// 创建文件输出流
			OutputStream os = null;
			try {
				os = new FileOutputStream(file);
				// 存储
				bitmap.compress(CompressFormat.JPEG, 100, os);
				// 关闭流
			} catch (FileNotFoundException e) {
				if (Utils.debug)
					e.printStackTrace();
			} finally {
				try {
					if (os != null)
						os.close();
				} catch (IOException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
			}
		}
		return file;
	}

	public static Bitmap ResizeBitmapLv(Bitmap bitmap, int newWLv) {
		int screeScoW = SreenW >= SreenH ? SreenH : SreenW;
		float tempw = screeScoW / newWLv;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scale = tempw / width;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(resizedBitmap);
		resizedBitmap = null;
		return wref.get();
	}

	public static Bitmap headImg_Resize(Bitmap bitmap, int control_w,
			int control_h) {
		int screenW = control_w;
		int screenH = control_h;
		int screeScoW = screenW >= screenH ? screenH : screenW;
		float tempw = screeScoW / 1;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float scale = tempw / width;
		Matrix matrix = new Matrix();
		matrix.postScale(scale, scale);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(resizedBitmap);
		resizedBitmap = null;
		// bitmap.recycle();

		return wref.get();
	}

	// public static Bitmap fitSizeImg(String path) {
	// if (path == null || path.length() < 1)
	// return null;
	// File file = new File(path);
	// if (!file.exists())
	// return null;
	// // Bitmap resizeBmp = null;
	// BitmapFactory.Options opts = new BitmapFactory.Options();
	// // 数字越大读出的图片占用的heap越小 不然总是溢出
	// if (file.length() < 20480) { // 0-20k
	// opts.inSampleSize = 1;
	// } else if (file.length() < 51200) { // 20-50k
	// opts.inSampleSize = 1;
	// } else if (file.length() < 307200) { // 50-300k
	// opts.inSampleSize = 1;
	// } else if (file.length() < 819200) { // 300-800k
	// opts.inSampleSize = 2;
	// } else if (file.length() < 1048576) { // 800-1024k
	// opts.inSampleSize = 3;
	// } else if (file.length() < 1048576 * 2) { // 1024-1024k
	// opts.inSampleSize = 4;
	// } else {
	// opts.inSampleSize = 4;
	// }
	// WeakReference<Bitmap> wref = new
	// WeakReference<Bitmap>(BitmapFactory.decodeFile(file.getPath(), opts));
	// // resizeBmp = BitmapFactory.decodeFile(file.getPath(), opts);
	// return wref.get();
	// }

	public Bitmap fitSizeBImg(String path) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		// Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 20480) { // 0-20k
			opts.inSampleSize = 1;
		} else if (file.length() < 51200) { // 20-50k
			opts.inSampleSize = 2;
		} else if (file.length() < 307200) { // 50-300k
			opts.inSampleSize = 4;
		} else if (file.length() < 819200) { // 300-800k
			opts.inSampleSize = 6;
		} else if (file.length() < 1048576) { // 800-1024k
			opts.inSampleSize = 8;
		} else if (file.length() < 1048576 * 2) { // 1024-1024k
			opts.inSampleSize = 9;
		} else {
			// opts.inSampleSize = 10;
			return null;
		}
		if (Utils.debug)
			Log.i(TAG, "...opts:" + opts.inSampleSize + "...path:" + path);
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(file.getPath(), opts));
		file = null;
		return wref.get();
	}

	public static int getCid() {
		return cid;
	}

	public static String byte2string(byte b) {
		int high = (b >> 4) & 0x0F;
		int low = (b & 0x0F);
		String convert = "0123456789abcdef";
		String result = "";
		result += convert.charAt(high);
		result += convert.charAt(low);
		return result;
	}

	public static String byte2string(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Utils.byte2string(b[i]);
		}
		return result;
	}

	public static int hexval(char c) {
		if ('0' <= c && c <= '9')
			return (c - '0');
		if ('a' <= c && c <= 'f')
			return (c - 'a' + 10);
		if ('A' <= c && c <= 'F')
			return (c - 'A' + 10);
		return 0;
	}

	public static byte[] string2byte(String s) {
		int length = s.length();
		length = (length / 2);
		byte[] buf = new byte[length];
		for (int i = 0; i < length; i++) {
			int nyb1 = Utils.hexval(s.charAt(2 * i));
			int nyb2 = Utils.hexval(s.charAt(2 * i + 1));
			buf[i] = (byte) ((nyb1 * 16) + nyb2);
		}
		return buf;
	}

	/**
	 * 获取客户端语言信息
	 * 
	 * @return 代表客户端所用语言的整型值
	 */
	public static int getLang() {
		String langid = (Locale.getDefault().getLanguage() + "_" + Locale
				.getDefault().getCountry()).toLowerCase();
		if ("zh_cn".equals(langid)) {
			return 0;
		} else if ("zh_tw".equals(langid)) {
			return 1;
		} else if ("zh_hk".equals(langid)) {
			return 1;
		} else if (langid.startsWith("en_")) {
			return 2;
		} else {
			return 0;
		}
	}

	/**
	 * 需要加载的图片可能是大图，我们需要对其进行合适的缩小处理
	 * 
	 * @param imageUri
	 */
	public static Bitmap getSrcImage(Uri imageUri, Context context,
			int MAX_WIDTH, int MAX_HEIGHT) {
		// Display display = this.getWindowManager().getDefaultDisplay();
		try {
			BitmapFactory.Options ops = new BitmapFactory.Options();
			ops.inJustDecodeBounds = true;
			Bitmap bmp = BitmapFactory.decodeStream(context
					.getContentResolver().openInputStream(imageUri), null, ops);
			int wRatio = (int) Math.ceil(ops.outWidth / (float) MAX_WIDTH);
			int hRatio = (int) Math.ceil(ops.outHeight / (float) MAX_HEIGHT);

			if (wRatio > 1 && hRatio > 1) {
				if (wRatio > hRatio) {
					ops.inSampleSize = wRatio;
				} else {
					ops.inSampleSize = hRatio;
				}
			}

			ops.inJustDecodeBounds = false;
			bmp = BitmapFactory.decodeStream(context.getContentResolver()
					.openInputStream(imageUri), null, ops);

			return bmp;

		} catch (FileNotFoundException e) {
			if (Utils.debug)
				e.printStackTrace();
			if (Utils.debug)
				Log.e(context.getClass().getName(), e.getMessage());
		}

		return null;
	}

	// Post方式获取信息http://photo.tuibo.net/index.php?c=chatpic&a=up
	public static String imgUpUrl = "c=chatpic&a=shenliaoup";

	// private static String
	// imgUpUrl="http://t.tuibo.net/index.php?c=chatpic&a=up";
	public static String postImgs(final Context context, String user_id,
			String file) {
		long start = System.currentTimeMillis();
		String Code = "84197ce68c3538f90ab8bdf6dcdda11d";
		MD5 m = new MD5();
		String key = m.getMD5ofStr(user_id + Code);
		String type = file.substring(file.lastIndexOf(".") + 1, file.length())
				.toLowerCase();
		File mfile = new File(file);
		long len = mfile.length();
		if (Utils.debug)
			Log.i(TAG, "post img file size" + len);
		// Post 方法比Get方法需要设置的参数更多
		String posturl = hostUrl + imgUpUrl + "&uid=" + user_id + "&type="
				+ type + "&key=" + key + "&os=" + 2 + "&v="
				+ android.os.Build.VERSION.RELEASE + "&filesize=" + len
				+ "&tv=" + Utils.apptype;
		if (Utils.debug)
			Log.i(TAG, posturl + "," + user_id);
		HttpURLConnection httpconn;
		String resultDate = "";
		try {
			httpconn = (HttpURLConnection) new URL(posturl).openConnection();
			// post 方式，输入输出需要设置为true
			httpconn.setDoInput(true);
			httpconn.setDoOutput(true);
			httpconn.setRequestMethod("POST"); // 设置为Post方式，默认为get方式
			// httpconn.setUseCaches(true); // 不使用缓存
			// httpconn.setInstanceFollowRedirects(true); // 重定向
			// httpconn.setRequestProperty("user_id",
			// user_id);
			// httpconn.setRequestProperty("type",
			// type);
			// httpconn.setRequestProperty("key",
			// key);
			// httpconn.setRequestProperty("pw",
			// pww);
			httpconn.setRequestProperty("Content-type",
					"Application/x-www-form-urlencoded"); // 设置连接
			// 的Content-type类型为：
			// application/x-www-form-urlencoded
			httpconn.connect(); // 连接
			if (Utils.debug)
				Log.i(TAG, "upimg+++++++++++httpconn.connect()+++++");
			DataOutputStream out = new DataOutputStream(
					httpconn.getOutputStream()); // 声明数据写入流
			Bitmap bm = fitSizeImg(file);
			if (bm != null) {
				if (bm.compress(CompressFormat.JPEG, 85, out)) {
					out.flush();
					out.close();
					if (Utils.debug)
						Log.i(TAG, "========flush ok=======");
				}
				bm.recycle();
				bm = null;
			}
			// FileInputStream fStream = new FileInputStream(file);
			// /* 设定每次写入1024bytes */
			// int bufferSize = 1024;
			// byte[] buffer = new byte[bufferSize];
			//
			// int length = -1;
			// /* 从文件读取数据到缓冲区 */
			// while ((length = fStream.read(buffer)) != -1) {
			// /* 将数据写入DataOutputStream中 */
			// out.write(buffer, 0, length);
			// }
			// fStream.close();
			// // byte[] filecontent = readFileImage(file);
			// // out.write(filecontent, 0, filecontent.length);
			//
			// out.flush();
			// out.close();
			if (Utils.debug)
				Log.i(TAG, "upimg+++++++++++out flush+++++");
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					httpconn.getInputStream()));

			String line = "";

			while ((line = reader.readLine()) != null) {
				resultDate += line;
			}
			if (Utils.debug)
				Log.i(TAG, "upimg++++++++++++++++" + resultDate);
			long l = len + resultDate.getBytes().length;
			SharedPreferences prefs = context.getSharedPreferences(
					PrefsMeme.MEME_PREFS, Context.MODE_WORLD_READABLE
							+ Context.MODE_WORLD_WRITEABLE);
			Editor editor = prefs.edit();
			if (resultDate.startsWith("{")) {
				JSONObject jo = null;
				try {
					jo = new JSONObject(resultDate);
				} catch (JSONException e) {
					if (Utils.debug)
						e.printStackTrace();
				}
				if (jo != null) {
					int code = 0;
					try {
						code = jo.getInt("code");
						if (code == 200) {
							// Log.i(TAG,"up ok");
							long end = System.currentTimeMillis();
							long time = end - start;
							if (Utils.debug)
								Log.i(TAG, "up ok 用时" + time);
							// String picUrl = jo.getString("picname");
							String picUrl = jo.getString("picurl");
							return picUrl;

						} else if (code == 101) {
							if (Utils.debug)
								Log.i(TAG, "key is not right");
						} else if (code == 103) {
							if (Utils.debug)
								Log.i(TAG,
										"your picture format is null or picture format must be jpg,gif or png ");
						}
					} catch (JSONException e) {

						if (Utils.debug)
							e.printStackTrace();
					}
				}
			}
		} catch (MalformedURLException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (IOException e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		} catch (Error e) {
			if (Utils.debug)
				e.printStackTrace();
		} finally {
			final String result = resultDate;
			Utils.executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						String data = LogUtils.makeLogStr(result);
						LogUtils.logFileOperate(context, data);
					} catch (Exception e) {

					} catch (Error e) {

					}
					// Looper.loop();
				}
			});
		}
		long end = System.currentTimeMillis();
		long time = end - start;
		if (Utils.debug)
			Log.i(TAG, "up fail 用时" + time);
		return null;
	}

	// 上传图片
	public static void postImgSocket(final TXMessage txmsg) {
		String filePath = txmsg.msg_path;
		if (Utils.debug)
			Log.v(TAG, "服务中上传失败图片消息的原图地址filePath=" + filePath);
		File file = new File(filePath);
		if (!file.exists()) {
			if (Utils.debug) {
				Log.d(TAG, txmsg.msg_path + "-->file not exists");
			}
			ContentValues values = new ContentValues();
			values.put("read_state", 5);
			txdata.getContentResolver().update(TxDB.Messages.CONTENT_URI,
					values, TxDB.Messages.MSG_ID + "=?",
					new String[] { "" + txmsg.msg_id });
			// SendHandleMsg(SHOW_IMG_NOFIND);
			return;
		}

		Bitmap smallImg = Utils.getImgByPath(txdata, txmsg.msg_path,
				Utils.msgroom_small_resolution);

		Bitmap bigImg = Utils.getImgByPath(txdata, txmsg.msg_path,
				Utils.msgroom_big_resolution);

		if (filePath != null
				&& (txmsg.msg_url == null || txmsg.msg_url.equals(""))) {
			initLoginSession(txdata);

			String tempImgPath = Utils.getStoragePath(txdata) + File.separator
					+ Utils.IMAGE_PATH + File.separator + txmsg.msg_id + ".jpg";
			try {
				// 生成合成的文件
				mSess.mDownUpMgr.getCompoundImgFile(tempImgPath, smallImg,
						bigImg);
			} catch (IOException e) {
				if (Utils.debug) {
					Log.w(TAG, "合成大小图文件异常", e);
				}
			}

			mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
					new DownUploadListner() {

						@Override
						public void onStart(FileTaskInfo taskInfo) {

						}

						@Override
						public void onProgress(FileTaskInfo taskInfo,
								int curlen, int totallen) {

						}

						@Override
						public void onFinish(FileTaskInfo taskInfo) {
							TXMessage txmsg = (TXMessage) taskInfo.mObj;
							if (Utils.debug)
								Log.v(TAG, "文件在server的Path=" + taskInfo.mPath);
							txmsg.msg_url = taskInfo.mServerHost + ":"
									+ taskInfo.mPath + ":" + taskInfo.mTime;
							if (txmsg.msg_type == TxDB.MSG_TYPE_IMAGE_EMS
									|| txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS) {
								txmsg.updateState = TXMessage.UPDATE_OK;
								ContentValues values = new ContentValues();
								values.put(TxDB.Messages.MSG_URL, txmsg.msg_url);
								txdata.getContentResolver().update(
										TxDB.Messages.CONTENT_URI, values,
										TxDB.Messages.MSG_ID + "=?",
										new String[] { "" + txmsg.msg_id });
								if (Utils.isIdValid(txmsg.group_id)) {
									mSess.getSocketHelper().sendGroupMsg(txmsg);
								} else {
									mSess.getSocketHelper().sendSingleMessage(
											txmsg);
								}

							}
						}

						@Override
						public void onError(FileTaskInfo taskInfo, int iCode,
								Object iPara) {
							TXMessage txmsg = (TXMessage) taskInfo.mObj;
							txmsg.updateState = TXMessage.UPDATE_FAILE;

						}
					}, txmsg);

		}

	}

	/**
	 * 从本地地址获得AnimationDrawable
	 * 
	 * @param context
	 * @param path
	 * @return
	 */
	public static AnimationDrawable getGifByPath(Context context, String path) {
		if (TextUtils.isEmpty(path)) {
			return null;
		}
		AnimationDrawable mSmile = null;
		try {
			if (!Utils.checkSDCard()) {

			} else {
				InputStream is = null;
				is = new FileInputStream(path);
				gifOpenHelper gHelper = new gifOpenHelper();
				mSmile = new AnimationDrawable();
				gHelper.read(is);
				BitmapDrawable bd = new BitmapDrawable(gHelper.getImage());
				mSmile.addFrame(bd, gHelper.getDelay(0));
				for (int i = 1; i < gHelper.getFrameCount(); i++) {
					mSmile.addFrame(new BitmapDrawable(gHelper.nextBitmap()),
							gHelper.getDelay(i));
				}
				mSmile.setBounds(0, 0, bd.getIntrinsicWidth(),
						bd.getIntrinsicHeight());
				mSmile.setOneShot(false);

				bd.setBounds(0, 0, bd.getIntrinsicWidth(),
						bd.getIntrinsicHeight());
			}
		} catch (Exception e) {

		}
		return mSmile;

	}

	// 各种工具方法
	public static Bitmap getImgByPath(Context context, String path,
			int resolution) {
		if (TextUtils.isEmpty(path))
			return null;
		Bitmap img = null;
		try {
			if (!Utils.checkSDCard()) {// 无SD卡
				// img = BitmapFactory.decodeResource(context
				// .getApplicationContext().getResources(),
				// R.drawable.no_sd_img);
				if (Utils.debug)
					Log.i(TAG, "没有SD卡，getImgByPath");

			} else {
				img = Utils.fitSizeAutoImg(path, resolution);
				if (img != null && resolution != Utils.msgroom_big_resolution)
					img = Utils.ResizeBitmapLv(img, 3);
			}
		} catch (Exception e) {
			if (Utils.debug) {
				Log.e(TAG, "加载bitmap异常", e);
			}
		} catch (OutOfMemoryError e) {
			if (Utils.debug) {
				Log.e(TAG, "内存溢出ERROR", e);
			}
		}
		return img;
	}

	// 下载图片
	// public static void dowloadImgSocket(final TXMessage txmsg,
	// final TxData txdata) {
	// final SocketClient socketClient = new SocketClient();
	// socketClient.setMode(SocketClient.DOWNLOAD_SMALL_IMG);
	// socketClient.setFileType(SocketClient.FILE_TYPE_IMAGE);
	// socketClient.setId(txmsg.msg_id);
	// socketClient.setContext(txdata);
	// socketClient.setSocketListener(new SocketListener() {
	// @Override
	// public void downloadProgress(int progress) {// 下载进度
	// txmsg.upDataCot = progress;
	// }
	//
	// @Override
	// public void onDownloadFinish(TXMessage txMsg) {// 下载成功
	// txmsg.msg_path = socketClient.getPath();
	// txmsg.upDataState = TXMessage.upDataOk;
	// ContentValues values = new ContentValues();
	// values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
	// txdata.getContentResolver().update(TxDB.Messages.CONTENT_URI,
	// values, TxDB.Messages.MSG_ID + "=?",
	// new String[] { "" + txmsg.msg_id });
	// }
	//
	// @Override
	// public void onStartDownload() {
	// }
	//
	// @Override
	// public void onStartUpload() {
	// }
	//
	// @Override
	// public void dataException(TXMessage txMsg) {
	// }
	//
	// });
	// socketClient.download(txmsg.msg_url);
	// }

	// 下载小图片
	public static void dowloadImgSocket(final TXMessage txmsg) {
		initLoginSession(txdata);
		String imgFilePath = mSess.mDownUpMgr.getImageFile(txmsg.msg_url,
				FileTransfer.SRC_TYPE_DEFAULT, txmsg.msg_id, false);
		mSess.mDownUpMgr.downloadImg(txmsg.msg_url, imgFilePath, 0, false,
				false, new DownUploadListner() {
					@Override
					public void onStart(FileTaskInfo taskInfo) {
					}

					@Override
					public void onProgress(FileTaskInfo taskInfo, int curlen,
							int totallen) {

					}

					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						String msg_path = taskInfo.mFullName;

						{
							switch (txmsg.msg_type) {
							case TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN:
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_QU_IMAGE_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_IMAGE_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE_OK;
								}
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_AUDIO_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE;
								}
								break;
							case TxDB.MSG_TYPE_IMAGE_EMS:
							case TxDB.MSG_TYPE_QU_IMAGE_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE_OK;
								break;

							case TxDB.MSG_TYPE_AUDIO_EMS:
							case TxDB.MSG_TYPE_QU_AUDIO_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE;
								break;

							}
							saveTxMsgToDB(txmsg);
						}

						txmsg.msg_path = taskInfo.mFullName;
						txmsg.updateState = TXMessage.UPDATE_OK;
						ContentValues values = new ContentValues();
						values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
						txdata.getContentResolver().update(
								TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?",
								new String[] { "" + txmsg.msg_id });

					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
					}
				}, txmsg);
	}

	// 上传名片
	// TODO有问题，
	// 因为原来SocketClient.FILE_TYPE_VCARD类型在socketReceive的run方法中没有引用，所以它的onStartUpload()方法也不会被调用，就不会被上传
	public static void PostContactsSocket(final TXMessage txmsg,
			final File cardFile, String fileName) {
		initLoginSession(txdata);
		if (cardFile != null
				&& (txmsg.msg_url == null || txmsg.msg_url.equals(""))) {
			mSess.mDownUpMgr.uploadFile(txmsg.msg_path,
					FileTransfer.FILE_TYPE_VCARD, 0, false, true,
					new DownUploadListner() {

						@Override
						public void onStart(FileTaskInfo taskInfo) {

						}

						@Override
						public void onProgress(FileTaskInfo taskInfo,
								int curlen, int totallen) {

						}

						@Override
						public void onFinish(FileTaskInfo taskInfo) {
							txmsg.msg_url = taskInfo.mServerHost + ":"
									+ taskInfo.mPath + ":" + taskInfo.mTime;
							txmsg.updateState = TXMessage.UPDATE_OK;
							txmsg.read_state = TXMessage.SENT;
							ContentValues values = new ContentValues();
							values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
							values.put(TxDB.Messages.MSG_URL, txmsg.msg_url);
							txdata.getContentResolver().update(
									TxDB.Messages.CONTENT_URI, values,
									TxDB.Messages.MSG_ID + "=?",
									new String[] { "" + txmsg.msg_id });
							if (Utils.isIdValid(txmsg.group_id))
								mSess.getSocketHelper().sendGroupMsg(txmsg);
							else
								mSess.getSocketHelper()
										.sendSingleMessage(txmsg);
							if (Utils.debug)
								Log.d(TAG, "txmsg.msg_url==" + txmsg.msg_url);

						}

						@Override
						public void onError(FileTaskInfo taskInfo, int iCode,
								Object iPara) {

						}
					});
		}
	}

	// 下载名片 vcard
	// public static void downContackSocket(final TXMessage txmsg,
	// final TxData txdata) {
	// final SocketClient socketClient = new SocketClient();
	// socketClient.setMode(SocketClient.DOWNLOAD_MODE);
	// socketClient.setFileType(SocketClient.FILE_TYPE_VCARD);
	// socketClient.setId(txmsg.msg_id);
	// socketClient.setContext(txdata);
	// socketClient.setSocketListener(new SocketListener() {
	//
	// @Override
	// public void downloadProgress(int progress) {// 下载进度
	// txmsg.upDataCot = progress;
	// }
	//
	// @Override
	// public void onDownloadFinish(TXMessage txMsg) {// 下载成功
	// String msg_path = socketClient.getPath();
	// txmsg.upDataState = TXMessage.upDataOk;
	// txmsg.read_state = TXMessage.READ;
	// String name = msg_path.substring(msg_path.lastIndexOf("/") + 1,
	// msg_path.length());
	// Contact contact = new Contact(txdata);
	// contact.readToFile(name, txdata);
	// ContentValues values = new ContentValues();
	// values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
	// txdata.getContentResolver().update(TxDB.Messages.CONTENT_URI,
	// values, TxDB.Messages.MSG_ID + "=?",
	// new String[] { "" + txmsg.msg_id });
	// }
	//
	// @Override
	// public void onStartDownload() {
	// }
	//
	// @Override
	// public void onStartUpload() {
	// }
	//
	// @Override
	// public void dataException(TXMessage txMsg) {
	// }
	//
	// });
	// socketClient.download(txmsg.msg_url);
	// }
	//

	static SessionManager mSess = null;

	private static void initLoginSession(Context context) {
		if (mSess == null) {
			mSess = SessionManager.getInstance();
		}
	}

	// 下载Vcard
	public static void downContackSocket(final TXMessage txmsg) {
		initLoginSession(txdata);
		mSess.mDownUpMgr.downloadVCard(txmsg.msg_url, txmsg.msg_id, true,
				new DownUploadListner() {
					@Override
					public void onStart(FileTaskInfo taskInfo) {
					}

					@Override
					public void onProgress(FileTaskInfo taskInfo, int curlen,
							int totallen) {
					}

					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						String msg_path = taskInfo.mFullName;

						{
							switch (txmsg.msg_type) {
							case TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN:
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_QU_IMAGE_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_IMAGE_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE_OK;
								}
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_AUDIO_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE;
								}
								break;
							case TxDB.MSG_TYPE_IMAGE_EMS:
							case TxDB.MSG_TYPE_QU_IMAGE_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE_OK;
								break;

							case TxDB.MSG_TYPE_AUDIO_EMS:
							case TxDB.MSG_TYPE_QU_AUDIO_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE;
								break;

							}
							saveTxMsgToDB(txmsg);
						}

						txmsg.updateState = TXMessage.UPDATE_OK;
						txmsg.read_state = TXMessage.READ;
						String name = msg_path.substring(
								msg_path.lastIndexOf("/") + 1,
								msg_path.length());
						Contact contact = new Contact(txdata);
						contact.readToFile(name, txdata);
						ContentValues values = new ContentValues();
						values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
						txdata.getContentResolver().update(
								TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?",
								new String[] { "" + txmsg.msg_id });

					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
					}
				}, txmsg);
	}

	/**
	 * 保存消息到DB
	 * 
	 * @param txMsg
	 */
	public static void saveTxMsgToDB(TXMessage txMsg) {
		TXMessage.saveTXMessagetoDB(txMsg, txdata.getContentResolver(), false);
	}

	// 下载音频
	public static void downAduioScoket(final TXMessage txmsg) {
		initLoginSession(txdata);

		String audioFilePath = mSess.mDownUpMgr.getAudioFile(txmsg.msg_url,
				txmsg.msg_id);
		mSess.mDownUpMgr.downloadAudio(txmsg.msg_url, audioFilePath, 0, false,
				new DownUploadListner() {
					@Override
					public void onStart(FileTaskInfo taskInfo) {
					}

					@Override
					public void onProgress(FileTaskInfo taskInfo, int curlen,
							int totallen) {
						TXMessage msg = (TXMessage) taskInfo.mObj;
						msg.updateCount = 0;
					}

					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						String msg_path = taskInfo.mFullName;

						{
							switch (txmsg.msg_type) {
							case TxDB.MSG_TYPE_MANAGER_REPORT_4_ADMIN:
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_QU_IMAGE_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_IMAGE_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE_OK;
								}
								if (txmsg.msg_type2 == TxDB.MSG_TYPE_AUDIO_EMS
										|| txmsg.msg_type2 == TxDB.MSG_TYPE_QU_AUDIO_EMS) {
									txmsg.msg_path = msg_path;
									txmsg.updateState = TXMessage.UPDATE;
								}
								break;
							case TxDB.MSG_TYPE_IMAGE_EMS:
							case TxDB.MSG_TYPE_QU_IMAGE_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE_OK;
								break;

							case TxDB.MSG_TYPE_AUDIO_EMS:
							case TxDB.MSG_TYPE_QU_AUDIO_EMS:
								txmsg.msg_path = msg_path;
								txmsg.updateState = TXMessage.UPDATE;
								break;

							}
							saveTxMsgToDB(txmsg);
						}

						txmsg.msg_path = taskInfo.mFullName;// 储存
						txmsg.updateState = TXMessage.UPDATE_OK;
						ContentValues values = new ContentValues();
						values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
						txdata.getContentResolver().update(
								TxDB.Messages.CONTENT_URI, values,
								TxDB.Messages.MSG_ID + "=?",
								new String[] { "" + txmsg.msg_id });
						if (Utils.debug)
							Log.d(TAG, "adiou_txmsg.msg_path=" + txmsg.msg_path);

					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
					}
				}, txmsg);
	}

	public static String getImageUrl = "c=chatpic&a=down";

	/**
	 * 创建日志文件目录
	 */
	public static File createDirectory(Context context) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			String storagePath = Environment.getExternalStorageDirectory()
					.getAbsolutePath() + "/shenliao/logs";
			File sddir = new File(storagePath);
			if (!sddir.exists()) {
				sddir.mkdirs();
			}
			return sddir;
		} else {
			return null;
		}
	}

	public static String ChangVerString(int appversion) {
		if (appversion != 0) {
			String appver = Integer.toString(appversion);
			int a = Integer.parseInt(appver.substring(0, 1));
			int b = Integer.parseInt(appver.substring(1, 3));
			int c = Integer.parseInt(appver.substring(3, 5));
			StringBuffer buf = new StringBuffer();
			buf.append(Integer.toString(a)).append(".")
					.append(Integer.toString(b)).append(".")
					.append(Integer.toString(c));
			String finalver = buf.toString();
			buf = null;
			return finalver;
		}
		return Integer.toString(0);
	}

	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap) {
		Bitmap output = null;
		try {
			if (bitmap != null) {
				output = Bitmap.createBitmap(bitmap.getWidth(),
						bitmap.getHeight(), Config.ARGB_8888);
			}

		} catch (OutOfMemoryError e) {
			if (Utils.debug) {
				Log.w(TAG, "包裹圆角缩略图时OOM", e);
			}
			System.gc();
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(),
					Config.ARGB_8888);
		}
		if (output != null) {
			Canvas canvas = new Canvas(output);

			final int color = 0xff424242;
			final Paint paint = new Paint();
			final Rect rect = new Rect(0, 0, bitmap.getWidth(),
					bitmap.getHeight());
			final RectF rectF = new RectF(rect);
			final float roundPx = 12;

			paint.setAntiAlias(true);
			canvas.drawARGB(0, 0, 0, 0);
			paint.setColor(color);
			canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

			paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
			canvas.drawBitmap(bitmap, rect, rect, paint);
		}

		return output;
	}

	public static Bitmap readBitMap(String path, boolean isBig) {
		if (path == null || path.length() < 1)
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		// Bitmap resizeBmp = null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		if (isBig) {
			// 数字越大读出的图片占用的heap越小 不然总是溢出
			if (file.length() < 20480) { // 0-20k
				opts.inSampleSize = 1;
			} else if (file.length() < 51200) { // 20-50k
				opts.inSampleSize = 1;
			} else if (file.length() < 307200) { // 50-300k
				opts.inSampleSize = 1;
			} else if (file.length() < 819200) { // 300-800k
				opts.inSampleSize = 2;
			} else if (file.length() < 1048576) { // 800-1024k
				opts.inSampleSize = 3;
			} else if (file.length() < 1048576 * 2) { // 1024-1024k
				opts.inSampleSize = 4;
			} else {
				opts.inSampleSize = 5;
				return null;
			}
		} else {
			// 数字越大读出的图片占用的heap越小 不然总是溢出
			if (file.length() < 20480) { // 0-20k
				opts.inSampleSize = 1;
			} else if (file.length() < 51200) { // 20-50k
				opts.inSampleSize = 2;
			} else if (file.length() < 307200) { // 50-300k
				opts.inSampleSize = 4;
			} else if (file.length() < 819200) { // 300-800k
				opts.inSampleSize = 6;
			} else if (file.length() < 1048576) { // 800-1024k
				opts.inSampleSize = 8;
			} else if (file.length() < 1048576 * 2) { // 1024-1024k
				opts.inSampleSize = 9;
			} else {
				opts.inSampleSize = 10;
				return null;
			}
		}

		// BLog.d(TAG, "...opts:"+opts.inSampleSize+"...path:"+path);
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(
				BitmapFactory.decodeFile(file.getPath(), opts));
		file = null;
		return wref.get();
	}

	public static Bitmap ResizeBitmap(Bitmap bitmap, int newWidth) {
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		float temp = ((float) height) / ((float) width);
		int newHeight = (int) ((newWidth) * temp);
		float scaleWidth = ((float) newWidth) / width;
		float scaleHeight = ((float) newHeight) / height;
		Matrix matrix = new Matrix();
		// resize the bit map
		matrix.postScale(scaleWidth, scaleHeight);
		// matrix.postRotate(45);
		Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height,
				matrix, true);
		// bitmap.recycle();
		WeakReference<Bitmap> wref = new WeakReference<Bitmap>(resizedBitmap);
		resizedBitmap = null;
		return wref.get();
	}

	public static Drawable zoomDrawable(Drawable drawable, int w, int h) {
		int width = drawable.getIntrinsicWidth();
		int height = drawable.getIntrinsicHeight();
		Bitmap oldbmp = drawableToBitmap(drawable); // drawable转换成bitmap
		Matrix matrix = new Matrix(); // 创建操作图片用的Matrix对象
		float scaleWidth = ((float) w / width); // 计算缩放比例
		float scaleHeight = ((float) h / height);
		matrix.postScale(scaleWidth, scaleHeight); // 设置缩放比例
		Bitmap newbmp = Bitmap.createBitmap(oldbmp, 0, 0, width, height,
				matrix, true); // 建立新的bitmap，其内容是对原bitmap的缩放后的图
		return new BitmapDrawable(newbmp); // 把bitmap转换成drawable并返回
	}

	public static Bitmap drawableToBitmap(Drawable drawable) // drawable
																// 转换成bitmap
	{
		if (drawable == null)
			return null;
		int width = drawable.getIntrinsicWidth(); // 取drawable的长宽
		int height = drawable.getIntrinsicHeight();
		Bitmap.Config config = drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
				: Bitmap.Config.RGB_565; // 取drawable的颜色格式
		Bitmap bitmap = Bitmap.createBitmap(width, height, config); // 建立对应bitmap
		Canvas canvas = new Canvas(bitmap); // 建立对应bitmap的画布
		drawable.setBounds(0, 0, width, height);
		drawable.draw(canvas); // 把drawable内容画到画布中
		return bitmap;
	}

	public static String urlToFileName(String url) {
		if (isNull(url)) {
			return null;
		}
		int index = url.lastIndexOf("/");
		return url.substring(index + 1);
	}

	public static void startPromptDialog(Context ctxt, int titleSource, int msg) {
		if (!ison) {
			ison = true;
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(ctxt);
			promptDialog.setTitle(titleSource);
			promptDialog.setMessage(msg);
			promptDialog.setCancelable(false);
			promptDialog.setNegativeButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							ison = false;
						}
					});
			promptDialog.show();
		}

	}

	public static void startPromptDialog(Context ctxt, int titleSource,
			int msg, final View view) {
		if (!ison) {
			ison = true;
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(ctxt);
			promptDialog.setTitle(titleSource);
			promptDialog.setMessage(msg);
			promptDialog.setCancelable(false);
			promptDialog.setNegativeButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							view.requestFocus();
							ison = false;
						}
					});
			promptDialog.show();
		}

	}

	private static boolean ison;

	public static void startPromptDialog(Context ctxt, String titleSource,
			String msg) {
		if (!ison) {
			ison = true;
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(ctxt);
			promptDialog.setTitle(titleSource);
			promptDialog.setMessage(msg);
			promptDialog.setCancelable(false);
			promptDialog.setNegativeButton(R.string.confirm,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							dialog.cancel();
							ison = false;

						}
					});
			promptDialog.show();
		}
	}

	// 无引用 2014.02.25 shc
	// public static int getINTFromByte(byte[] buffer) {
	// int a = 0;
	// a = (0xff000000 & (((int) buffer[0]) << 24) | 0x00ff0000 & (((int)
	// buffer[1]) << 16) | 0x0000ff00
	// & (((int) buffer[2]) << 8) | 0x000000ff & (((int) buffer[3]) << 0));
	// return a;
	// }
	//
	// public static byte[] getBytesFromInt(int value) {
	// byte b[] = new byte[4];
	// b[0] = (byte) (value >> 24 & 0xFF);
	// b[1] = (byte) (value >> 16 & 0xFF);
	// b[2] = (byte) (value >> 8 & 0xFF);
	// b[3] = (byte) (value >> 0 & 0xFF);
	// return b;
	// }
	//
	// /** 新创建一个长度为str字节个数+1的字节数组，最后一个字节放数字0，前面放字符串str的 */
	// public static byte[] getBytesFromString(String str) {
	// if (str != null) {
	// byte[] b = new byte[str.getBytes().length + 1];
	// byte[] zero = new byte[] { 0 };
	// System.arraycopy(str.getBytes(), 0, b, 0, str.getBytes().length);
	// System.arraycopy(zero, 0, b, str.getBytes().length, 1);
	// return b;
	// } else {
	// return new byte[] { 0 };
	// }
	//
	// }

	String specil = "*";
	/**
	 * 渠道ID, 标识软件市场
	 */
	public static String appid = "33";
	public static int cid = 33;
	public static int buildNo = 1;

	// 无引用 2014.02.25 shc
	// public static String filterSpecial(String base) {
	// StringBuffer newName = new StringBuffer();
	// if (!Utils.isNull(base)) {
	// String[] newNames = base.split("");
	// for (String s : newNames) {
	// if (s.equals(""))
	// continue;
	// if (specilStr.indexOf(s) != -1) {
	// newName.append("\\");
	// newName.append(s);
	// } else {
	// newName.append(s);
	// }
	// }
	// }
	// return newName.toString();
	//
	// }
	//
	// public static byte[] bitmapToBytes(Bitmap bitmap, String suffix) {
	// if (bitmap == null) {
	// return null;
	// }
	// final ByteArrayOutputStream os = new ByteArrayOutputStream();
	// // 将Bitmap压缩成PNG编码，质量为100%存储
	// // if(suffix.equals("jpg")||suffix.equals("jpeg")){
	// // bitmap.compress(Bitmap.CompressFormat.JPEG, 100,
	// // os);//除了PNG还有很多常见格式，如jpeg等。
	// // }else if(suffix.equals("png")){
	// bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
	// // }
	// return os.toByteArray();
	// }

	// resolution 分辨率 例如640*640
	public static Bitmap fitSizeAutoImg(String path, int resolution) {
		if (path == null || path.length() < 1)
			return null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		opts.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(path, opts);
		opts.inSampleSize = Utils.computeSampleSize(opts, -1, resolution);
		opts.inJustDecodeBounds = false;
		try {
			// WeakReference<Bitmap> wref = new
			// WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
			// return wref.get();
			return BitmapFactory.decodeFile(path, opts);
		} catch (Exception e) {
			Log.e(TAG, "解析加载图片fitSizeAutoImg异常", e);
			return null;
		} catch (OutOfMemoryError err) {
			System.gc();
			if (Utils.debug) {
				Log.e(TAG, "加载图片为指定分辨率OOM异常了", err);
			}
			try {
				return fitSizeImg(path);
			} catch (OutOfMemoryError e) {
				System.gc();
				if (Utils.debug) {
					Log.e(TAG, "修改加载大图片策略后又OOM异常了", e);
				}
				try {
					return fitSizeImg(path, 3);
				} catch (OutOfMemoryError oerr) {
					if (Utils.debug) {
						Log.e(TAG, "修改为加载小图片策略后咋会也OOM异常了", oerr);
					}
				}
			}
			return null;
		}
	}

	// 无引用 2014.02.25 shc
	// /**
	// *
	// * create the bitmap from a byte array
	// *
	// * @param src
	// * the bitmap object you want proecss
	// *
	// * @param watermark
	// * the water mark above the src
	// *
	// * @return return a bitmap object ,if paramter's length is 0,return null
	// */
	//
	// public static Bitmap createBitmap(Bitmap src, Bitmap watermark, boolean
	// is) {
	// String tag = "createBitmap";
	//
	// if (Utils.debug)
	// Log.d(tag, "create a new bitmap");
	//
	// if (src == null || watermark == null) {
	// return null;
	//
	// }
	//
	// int w = src.getWidth();
	//
	// int h = src.getHeight();
	//
	// int ww = watermark.getWidth();
	//
	// int wh = watermark.getHeight();
	// Bitmap bigdstBitmap = null;
	// // Bitmap bigdstBitmap1=ResizeBitmap(watermark,42);
	// Bitmap bigdstBitmap1 = null;
	// // if(ww<wh){
	// bigdstBitmap1 = Bitmap.createBitmap(watermark, 0, 0, ww, wh);
	// // bigdstBitmap = ResizeBitmap(bigdstBitmap1, 38);
	// bigdstBitmap = zoomBitmap(bigdstBitmap1, 38, 38);
	// // }else{
	// // bigdstBitmap1 = Bitmap.createBitmap(watermark, 0, 0, ww, wh);
	// // bigdstBitmap = ResizeBitmap(bigdstBitmap1, 38);
	// // }
	// // if(!isMoreDefault){
	// // bigdstBitmap=ResizeBitmap(bigdstBitmap1,60);
	// // }else{
	//
	// // }
	//
	// // create the new blank bitmap
	//
	// Bitmap newb = Bitmap.createBitmap(w, h, Config.ARGB_8888);//
	// 创建一个新的和SRC长度宽度一样的位图
	//
	// Canvas cv = new Canvas(newb);
	// cv.drawARGB(0, 255, 255, 255);
	// // cv.drawRGB(255, 255, 255);
	// // draw src into
	//
	// cv.drawBitmap(src, 0, 0, null);// 在 0，0坐标开始画入src
	//
	// // draw watermark into
	//
	// cv.drawBitmap(bigdstBitmap, 1, 5, null);// 在src的右下角画入水印
	//
	// // save all clip
	//
	// cv.save(Canvas.ALL_SAVE_FLAG);// 保存
	//
	// // store
	//
	// cv.restore();// 存储
	// return newb;
	//
	// }

	/** 根据最短边，截取图片中央的正方形 */
	public static Bitmap ImageCrop(Bitmap bitmap) {
		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();

		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

		int retX = w > h ? (w - h) / 2 : 0;// 基于原图，取正方形左上角x坐标
		int retY = w > h ? 0 : (h - w) / 2;

		// 下面这句是关键
		return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
	}

	/** 根据最短边，从左上角截取图片的正方形 */
	public static Bitmap imageCropFromStart(Bitmap bitmap) {
		int w = bitmap.getWidth(); // 得到图片的宽，高
		int h = bitmap.getHeight();

		int wh = w > h ? h : w;// 裁切后所取的正方形区域边长

		int retX = 0;// 从左上角截取
		int retY = 0;

		// 下面这句是关键
		return Bitmap.createBitmap(bitmap, retX, retY, wh, wh, null, false);
	}

	public static Toast creatNoSdCardInfo(Context con) {
		Toast nosdToast = new Toast(con);
		LayoutInflater mInflater = LayoutInflater.from(con);
		View toastView = mInflater.inflate(R.layout.no_sdcard_toast, null);
		TextView toastTextView = (TextView) toastView
				.findViewById(R.id.msgRoom_toast_text); //msgRoom_toast_text
		toastTextView.setText(R.string.no_sdcard_info);
		nosdToast.setDuration(Toast.LENGTH_LONG);
		nosdToast.setView(toastView);
		nosdToast.show();
		return nosdToast;
	}

	// 切换到听筒模式和切换自动下载
	public static void creatMsgRoomUpToolsInfo(Activity con, int message_id,
			int img_id) {
		Toast toast = new Toast(con);
		int toastY = 0;
		RelativeLayout Uplayout = (RelativeLayout) con
				.findViewById(R.id.rl_msgroom_title_bar);
		if (Uplayout != null) {
			toastY = Uplayout.getHeight();
			LayoutInflater mInflater = LayoutInflater.from(con);
			View toastView = mInflater.inflate(R.layout.msgroom_uptools_toast,
					null);
			TextView toastTextView = (TextView) toastView
					.findViewById(R.id.msgRoom_up_toast_text);
			toastTextView.setText(message_id);
			ImageView toastImageView = (ImageView) toastView
					.findViewById(R.id.msgRoom_up_toast_icon);
			toastImageView.setBackgroundResource(img_id);
			toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, toastY);
			toast.setDuration(Toast.LENGTH_SHORT);
			toast.setView(toastView);
			toast.show();
		}
	}

	public static void creatNoRecordInfo(Context con) {
		Toast.makeText(con, R.string.no_record_info, 1).show();
	}

	public static boolean opGpsOrNetwork(final Activity con) {
		// LocationManager alm = (LocationManager)
		// con.getSystemService(Context.LOCATION_SERVICE);
		String str = Settings.Secure.getString(con.getContentResolver(),
				Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		if (str.contains("gps") || (str.contains("network"))) {
			return true;
		}
		if (Utils.getNetworkType(con) == NET_NOT_AVAILABLE) {
			// 网络不通跳转到网络设置
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(con);
			promptDialog.setTitle(R.string.seach_network_title1);
			promptDialog.setMessage(R.string.seach_network_title);
			promptDialog.setPositiveButton(R.string.tsetting_menu,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
							con.startActivity(new Intent(
									Settings.ACTION_WIFI_SETTINGS)); // 直接进入手机中的wifi网络设置界面
						}
					});
			promptDialog.setNegativeButton(
					R.string.chatroom_audio_cancel_button,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.cancel();
						}
					});

			promptDialog.show();
		} else {
			// if
			// (alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)||alm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
			// {
			// return true;
			// }
			AlertDialog.Builder promptDialog = new AlertDialog.Builder(con);
			promptDialog.setTitle(R.string.prompt);
			promptDialog.setMessage(R.string.gps_fail_text1);
			promptDialog.setPositiveButton(R.string.gps_fail_selectl,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							dialog.cancel();
							Intent intent = new Intent();
							intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
							intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							try {
								con.startActivity(intent);

							} catch (ActivityNotFoundException ex) {

								// The Android SDK doc says that the location
								// settings activity
								// may not be found. In that case show the
								// general settings.

								// General settings activity
								intent.setAction(Settings.ACTION_SETTINGS);
								try {
									con.startActivity(intent);
								} catch (Exception e) {
								}
							}
						}
					});
			promptDialog.setNegativeButton(R.string.gps_fail_selectr,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {

							dialog.cancel();
						}
					});

			promptDialog.show();
		}
		return false;
	}

	// 判断网络 是否离线
	public static boolean isOffLine(Activity con) {
		if (Utils.getNetworkType(con) != NET_NOT_AVAILABLE) {
			return false;
		}
		// String str =
		// Settings.Secure.getString(con.getContentResolver(),Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
		// if (str.contains("gps")||str.contains("network")) {
		// LocationManager alm = (LocationManager)
		// con.getSystemService(Context.LOCATION_SERVICE);
		// if(alm.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)||alm.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER))
		// {
		// return false;
		// }
		// }

		return true;
	}

	public static int px2dip(float pxValue, Context ctx) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}

	public static int dip2px(float dipValue, Context ctx) {
		final float scale = ctx.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	// 过滤重复消息
	public static boolean isFilterMsgId(ArrayList<TXMessage> filterTxmsg,
			Long msgid) {
		for (TXMessage temptxmsg : filterTxmsg) {
			if (temptxmsg.msg_id == msgid) {
				return false;
			}
		}
		return true;
	}

	// 在image文件夹里创建 作用是不让该文件夹的图片出现在相册中
	public static void creatNoPhoto(Context con, String directory) {
		String storagePath = Utils.getStoragePath(con);
		if (Utils.isNull(storagePath)) {
			return;
		}
		File sddir = new File(storagePath, directory + "/.nomedia");
		if (!sddir.exists() && !sddir.mkdirs()) {
			sddir.mkdir();
		}
	}

	public static ServerRsp getServerRsp(Intent intent) {
		Bundle bundle = intent.getBundleExtra(Constants.EXTRA_SERVER_RSP_KEY);
		if (bundle != null) {
			ServerRsp serverRsp = new ServerRsp();
			serverRsp.setStatusCode((StatusCode) bundle
					.getSerializable("statusCode"));
			serverRsp.setBundle(bundle);
			return serverRsp;
		}
		return null;
	}

	// TODO 引用解除，待删除 2014.04.02 shc
	// public static TX getTX(Intent intent) {
	// Bundle bundle = intent.getBundleExtra(TX.EXTRA_TX);
	// if (bundle != null) {
	// TX tx = new TX();
	// tx.setBundle(bundle);
	// if (Utils.debug) {
	// Log.i(TAG,
	// "bundle get avatar_url is :"
	// + bundle.getString("avatar_url"));
	// }
	// return tx;
	// }
	// return null;
	//
	// }

	public static Bundle txGroupToBundle(TxGroup txGroup) {
		Bundle bundle = null;
		if (txGroup != null) {
			bundle = new Bundle();
			bundle.putLong("group_id", txGroup.group_id);
			bundle.putInt("group_ver", txGroup.group_ver);
			bundle.putLong("group_own_id", txGroup.group_own_id);
			bundle.putString("group_own_name", txGroup.group_own_name);
			bundle.putString("group_title", txGroup.group_title);
			bundle.putString("group_tx_ids", txGroup.group_tx_ids);
		}
		return bundle;
	}

	public static TxGroup getTxGroup(Intent intent) {
		Bundle bundle = intent.getBundleExtra(Utils.MSGROOM_TX_GROUP);
		TxGroup txGroup = new TxGroup();
		if (bundle != null) {
			txGroup.group_id = bundle.getLong("group_id");
			txGroup.group_ver = bundle.getInt("group_ver");
			txGroup.group_own_id = bundle.getLong("group_own_id");
			txGroup.group_own_name = bundle.getString("group_own_name");
			txGroup.group_title = bundle.getString("group_title");
			txGroup.group_tx_ids = bundle.getString("group_tx_ids");
		}
		return txGroup;
	}

	// 储存公用经纬度
	static SharedPreferences location_prefs = null;
	static Editor location_editor;

	public static void saveLocationData(Context ct, double latitude,
			double longitude) {
		TxData.public_latitude = latitude;
		TxData.public_longitude = longitude;
		TxData.public_location_time = System.currentTimeMillis();
		if (location_prefs == null) {
			location_prefs = ct.getSharedPreferences("location",
					Context.MODE_PRIVATE);
			location_editor = location_prefs.edit();
		}
		if (location_editor != null) {
			location_editor.putFloat("public_latitude",
					(float) TxData.public_latitude);
			location_editor.putFloat("public_longitude",
					(float) TxData.public_longitude);
			location_editor.putLong("public_location_time",
					TxData.public_location_time);
			location_editor.putString("public_location_info",
					TxData.public_location_info);
			location_editor.commit();
		}
	}

	public static void readLocationData(Context ct) {
		if (location_prefs == null) {
			location_prefs = ct.getSharedPreferences("location",
					Context.MODE_PRIVATE);
			location_editor = location_prefs.edit();
		}
		if (TxData.public_latitude == 0 && TxData.public_longitude == 0) {
			TxData.public_latitude = location_prefs.getFloat("public_latitude",
					0);
			TxData.public_longitude = location_prefs.getFloat(
					"public_longitude", 0);
			TxData.public_location_time = location_prefs.getLong(
					"public_location_time", 0);
			TxData.public_location_info = location_prefs.getString(
					"public_location_info", "火星");
		}
	}

	// 根据时间判断相差5分钟就重新定位
	public static boolean isReLocation() {
		// long time = System.currentTimeMillis();
		// if (TxData.public_location_time <= 0) {
		// return true;
		// }
		// if (TxData.public_location_time >= time) {
		// return true;
		// }
		// if (time - TxData.public_location_time >= 300 * 1000) {
		// return true;
		// }
		return true;
	}

	// 储存和读取聊天界面的一些状态 包括开关闭自动播放音频、扬声器和听筒模式、文本输入和录音切换
	static SharedPreferences msgroom_prefs = null;
	static Editor msgroom_editor;

	public static void saveAutoPlayAdiouData(Context ct) {
		if (msgroom_prefs == null) {
			msgroom_prefs = ct.getSharedPreferences("msgroom_prefs",
					Context.MODE_PRIVATE);
			msgroom_editor = msgroom_prefs.edit();
		}
		if (msgroom_editor != null) {
			msgroom_editor.putBoolean("isauto", isOpenPlayAdiou);
			msgroom_editor.putBoolean("ishanset", isHandset);
			msgroom_editor.putBoolean("istext", isSendText);
			msgroom_editor.commit();
		}
	}

	public static void readAutoPlayAdiouData(Context ct) {
		if (msgroom_prefs == null) {
			msgroom_prefs = ct.getSharedPreferences("msgroom_prefs",
					Context.MODE_PRIVATE);
			msgroom_editor = msgroom_prefs.edit();
		}
		if (msgroom_editor != null) {
			isOpenPlayAdiou = msgroom_prefs.getBoolean("isauto", true);
			isHandset = msgroom_prefs.getBoolean("ishanset", false);
			isSendText = msgroom_prefs.getBoolean("istext", true);
		}

	}

	public static int isNetworkAvailable(Context inContext) {
		Context context = inContext.getApplicationContext();
		ConnectivityManager manager = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkinfo = manager.getActiveNetworkInfo();
		if (networkinfo == null || !networkinfo.isAvailable()) {
			// 当前网络不可用 你该干嘛干嘛
			return NET_NOT_AVAILABLE;
		} else {
			// 如果当前是WIFI连接
			if (NET_TYPE_WIFI.equals(networkinfo.getTypeName())) {
				return NET_WIFI;
			}
			// 非WIFI联网
			else {
				String proxyHost = android.net.Proxy.getDefaultHost();
				// Log.i("HttpConnectionImpl", "proxyHost : " + proxyHost);
				// 代理模式
				if (proxyHost != null) {
					return NET_PROXY;
				}
				// 直连模式
				else {
					return NET_NORMAL;
				}
			}
		}
	}

	/**
	 * 从消息中提取出协议号
	 */
	public static int getMessageType(String message) {
		try {
			return Integer.valueOf(message.replaceAll("\\D+(\\d+).*", "$1"));
		} catch (NumberFormatException e) {
			return -1;
		}
	}

	/**
	 * 从输入流中读取文本数据
	 * 
	 * @param ips
	 *            输入流
	 * @param encoding
	 *            字符集
	 * @return 文本数据
	 * @throws IOException
	 */
	public static String readTextFromStream(InputStream ips, String encoding)
			throws IOException {
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while ((len = ips.read(buf)) != -1) {
			sb.append(new String(buf, 0, len, encoding));
		}
		return sb.toString();
	}

	// 利用经纬度计算距离
	private final static double EARTH_RADIUS = 6378137.0; // 地球半径；

	public static double gpsDistance(double lat_a, double lng_a, double lat_b,
			double lng_b) {

		double radLat1 = (lat_a * Math.PI / 180.0);

		double radLat2 = (lat_b * Math.PI / 180.0);

		double a = radLat1 - radLat2;

		double b = (lng_a - lng_b) * Math.PI / 180.0;

		double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)

		+ Math.cos(radLat1) * Math.cos(radLat2)

		* Math.pow(Math.sin(b / 2), 2)));

		s = s * EARTH_RADIUS;

		s = Math.round(s * 10000) / 10000;

		return s;

	}

	// 方向 东南西北 0,1,2,3
	public static int gpsDirection(double lat_a, double lng_a, double lat_b,
			double lng_b) {
		int direction = -1;
		double AngleLat1 = (lat_a * Math.PI / 180.0);
		double AngleLong1 = (lng_a * Math.PI / 180.0);
		double AngleLat2 = (lat_b * Math.PI / 180.0);
		double AngleLong2 = (lng_b * Math.PI / 180.0);
		double y = Math.sin(AngleLong1 - AngleLong2) * Math.cos(AngleLat2);
		double x = Math.cos(AngleLat1) * Math.sin(AngleLat2)
				- Math.sin(AngleLat1) * Math.cos(AngleLat2)
				* Math.cos(AngleLong1 - AngleLong2);
		double Cal_bearing = 360 - (Math.atan2(y, x) * 180.0 / Math.PI + 360) % 360;
		if (Cal_bearing <= 45) {
			direction = 3;
		} else if (Cal_bearing <= 135) {
			direction = 0;
		} else if (Cal_bearing <= 225) {
			direction = 1;
		} else if (Cal_bearing <= 315) {
			direction = 2;
		} else {
			direction = 3;
		}

		return direction;
	}

	/**
	 * 从Bundle对象中获得ChatChannel对象
	 */
	public static ChatChannel generateChatChannel(Bundle bundle) {
		ChatChannel channel = new ChatChannel();
		channel.setChannelId(bundle.getInt("id"));
		channel.setChannelVer(bundle.getInt("ver"));
		channel.setPeopleNum(bundle.getInt("count"));
		channel.setJoin(bundle.getBoolean("join"));
		if (!bundle.containsKey("subject")) {
			return channel;
		}
		channel.setSubject(bundle.getString("subject"));
		channel.setContent(bundle.getString("content"));
		channel.setIconUrl(bundle.getString("icon_url"));
		channel.setRange(bundle.getInt("range"));
		channel.setProp(bundle.getInt("prop"));
		return channel;
	}

	/**
	 * 从Bundle数组中获得ChatChannel集合
	 */
	public static ArrayList<ChatChannel> generateChatChannelList(
			Bundle[] bundles) {
		ArrayList<ChatChannel> channels = new ArrayList<ChatChannel>();
		for (int i = 0; i < bundles.length; i++) {
			channels.add(generateChatChannel(bundles[i]));
		}
		return channels;
	}

	/**
	 * 从Bundle对象中获取TX对象
	 */
	public static TX generateTX(Bundle bundle) {
		TX tx = new TX();
		tx.partner_id = bundle.getInt("uid");
		tx.setSex(bundle.getInt("sex"));
		tx.age = bundle.getInt("age");
		tx.avatar_url = bundle.getString("avatar");
		tx.area = bundle.getString("city");
		tx.setNick_name(bundle.getString("nick"));
		tx.sign = bundle.getString("sign");
		return tx;
	}

	/**
	 * 解析表情字符串初始化
	 */
	public static SmileyParser getSmileyParser(Context context) {
		SmileyParser smileyParser = new SmileyParser(context);
		smileyParser.setInTuixin(false);
		smileyParser.setInChatView(true);
		return smileyParser;
	}

	// 判断是否可以自动下载图片
	public static boolean isAutoDownLoadImg(Context con) {
		SharedPreferences tuixin_setting = con.getSharedPreferences(
				SettingsPreference.TUIXIN_SETTING, Context.MODE_PRIVATE);
		boolean state = tuixin_setting.getBoolean(
				con.getString(R.string.pref_key_save_download_pic), true);
		if (state || getNetworkType(con) == NET_WIFI) {
			// if(state){
			return true;
		}
		return false;
	}

	// 判断是否可以自动下载声音
	public static boolean isAutoDownLoadAdiou(Context con) {
		SharedPreferences tuixin_setting = con.getSharedPreferences(
				SettingsPreference.TUIXIN_SETTING, Context.MODE_PRIVATE);
		boolean state = tuixin_setting.getBoolean(
				con.getString(R.string.pref_key_save_download_audio), true);
		if (state || getNetworkType(con) == NET_WIFI) {
			// if(state){
			return true;
		}
		return false;
	}

	// 判断我的个人资料信息是否完整
	public static boolean ismUserInfoComplete() {
		TX mTx = TX.tm.getTxMe();
		if (mTx.birthday == 0) {
			return false;
		}
		if (isNull(mTx.area)) {
			return false;
		}
		if (isNull(mTx.job)) {
			return false;
		}
		if (isLangNull(mTx.getLanguages())) {
			return false;
		}
		if (isLangNull(mTx.hobby)) {
			return false;
		}
		return true;
	}

	/**
	 * 清除所有聊天记录 清除完毕会以广播形式通
	 * 
	 * @see Constants.INTENT_ACTION_CLEAR_ALL_MSGS_FINISH
	 */
	public static void clearAllMsgs() {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				long b = System.currentTimeMillis();
				MsgStat.clearMsgStats(false, txdata.getContentResolver());
				// MsgStat.delAllMsgStat(txData);//clearMsgStats方法中已经有了清空数据库的操作
				Cursor c = txdata.getContentResolver().query(
						TxDB.Messages.CONTENT_URI,
						new String[] { Messages.MSG_ID, Messages.MSG_PATH },
						null, null, null);
				if (Utils.debug)
					Log.i(TAG, "msg count:" + c.getCount());
				if (c != null) {
					try {
						while (c.moveToNext()) {
							// 删除消息对应的文件
							if (!Utils.isNull(c.getString(1))) {
								File file = new File(c.getString(1));
								if (file != null && file.exists()) {
									file.delete();
								}
							}
						}
					} finally {
						c.close();
					}
				}
				txdata.getContentResolver().delete(TxDB.Messages.CONTENT_URI,
						null, null);
				if (Utils.debug)
					Log.i(TAG,
							"clear all msgs cost time:"
									+ (System.currentTimeMillis() - b) + "ms");
				Intent intent = new Intent(
						Constants.INTENT_ACTION_CLEAR_ALL_MSGS_FINISH);
				txdata.sendBroadcast(intent);
			}
		});
	}

	public static void clearSdFile() {
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				clearStrangerAudio();
				clearStrangerImage(Utils.AVATAR_PATH);
				clearStrangerImage(Utils.IMAGE_PATH);
				Intent intent = new Intent(
						Constants.INTENT_ACTION_CLEAR_AVATAR_FINISH);
				txdata.sendBroadcast(intent);
			}
		});

	}

	public static void scanFilesSize() {
		executorService.submit(new Runnable() {

			@Override
			public void run() {
				if (checkSDCard()) {
					String path = Environment.getExternalStorageDirectory()
							.getAbsolutePath() + rootPath;
					File rootFile = new File(path);
					if (rootFile.exists()) {
						long fileSize = scanFile(rootFile);
						Intent intent = new Intent(
								Constants.INTENT_ACTION_SCAN_FILE_FINISH);
						intent.putExtra("fileSize", fileSize / 1000000);
						txdata.sendBroadcast(intent);
					}
				}
			}
		});
	}

	private static class FileSizeObj {
		public long totalsize = 0;
	}

	private static long scanFile(File file) {
		final FileSizeObj fsobj = new FileSizeObj();
		if (file != null) {
			if (file.isDirectory() && !"image".equals(file.getName())
					&& !"logs".equals(file.getName())) {
				File[] files = file.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String filename) {
						File file = new File(dir, filename);
						if (file.isFile()) {
							fsobj.totalsize += file.length();
							return false;
						}
						return true;
					}
				});
				if (files != null) {
					for (File f : files) {
						fsobj.totalsize += scanFile(f);
					}
				}
			}
		}
		return fsobj.totalsize;
	}

	public static void clearStrangerAudio() {
		File audioDir = new File(getStoragePath(txdata), Utils.AUDIO_PATH);

		if (audioDir != null && audioDir.exists() && audioDir.isDirectory()) {
			audioDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(dir, filename);
					if (file.isFile())
						file.delete();
					return false;
				}
			});
		}
	}

	public static void clearStrangerImage(String folderName) {
		// final Map<String, String> txMap = initCache(txData);
		long b = System.currentTimeMillis();
		File avatarDir = new File(getStoragePath(txdata), folderName);
		if (avatarDir != null && avatarDir.exists() && avatarDir.isDirectory()) {
			avatarDir.listFiles(new FilenameFilter() {
				@Override
				public boolean accept(File dir, String filename) {
					File file = new File(dir, filename);
					if (file.isFile()) {
						String key = filename.lastIndexOf(".") == -1 ? filename
								: filename.substring(0,
										filename.lastIndexOf("."));
						// if (!txMap.containsKey(key)) {//修改为直接判断是否是我的ID
						if (!("" + TX.tm.getUserID()).equals(key)) {
							// 如果不是我的头像，删除
							file.delete();
						}
					}
					return false;
				}
			});
		}
		long e = System.currentTimeMillis();
		if (debug)
			Log.i("Finish",
					"delete finish,cost time is:" + String.valueOf(e - b)
							+ "ms");
	}

	// 该方法集成到了调用方法内
	// private static Map<String, String> initCache(TxData txData) {
	// // 删除SD卡头像文件时,先从DB中查询不能删除的数据，放入map中
	// // Cursor cursor =
	// // txData.getContentResolver().query(TxDB.Tx.CONTENT_URI,
	// // new String[] { TxDB.Tx.TX_ID }, TxDB.Tx.TX_TYPE + " = ?",
	// // new String[] { String.valueOf(TxDB.TX_TYPE_TB) }, null);
	// // Cursor channelC =
	// // txData.getContentResolver().query(TxDB.Channel.CONTENT_URI,
	// // new String[] { TxDB.Channel.CHANNEL_ID }, null, null, null);
	// //
	// Map<String, String> txMap = new HashMap<String, String>();
	// // if (cursor != null) {
	// // while (cursor.moveToNext()) {
	// // txMap.put(cursor.getString(0), cursor.getString(0));
	// // }
	// // cursor.close();
	// // }
	// // if (channelC != null) {
	// // while (channelC.moveToNext()) {
	// // txMap.put(channelC.getString(0), channelC.getString(0));
	// // }
	// // channelC.close();
	// // }
	// String selfId = String.valueOf(TX.tm.getTxMe().partner_id);
	// txMap.put(selfId, selfId);
	// return txMap;
	// }

	public static long str2Long(String str) {
		long num;
		try {
			num = Long.parseLong(str);
		} catch (Exception e) {
			return -1;
		}
		return num;
	}

	public static void delAvatar(Context context, long partnerId, String url) {
		if (isIdValid(partnerId)) {
			try {
				File dir = new File(getStoragePath(context), Utils.AVATAR_PATH);
				if (!dir.exists()) {
					dir.mkdirs();
				}
				String tmp = ".jpg";
				if (!isNull(url)) {
					String[] values = url.split(":");
					if (values != null && values.length > 2) {
						tmp = values[2].substring(values[2].lastIndexOf("."));
					}
				}
				File file = new File(dir, partnerId + tmp);
				if (file.exists()) {
					file.delete();
				}
			} catch (Exception e) {

			}
		}
	}

	// 引用解除，注掉 2014.01.23 shc
	// // 将每条消息的方向和位置计算出来
	// public static void getLbsLoction(TXMessage txmsg) {
	// double txmsg_latitude = 0.0;
	// double txmsg_longitude = 0.0;
	// if (!Utils.isNull(txmsg.geo)) {
	// String loction[] = txmsg.geo.split(",");
	// txmsg_latitude = Double.parseDouble(loction[0]);
	// txmsg_longitude = Double.parseDouble(loction[1]);
	// }
	// txmsg.lbs_distance = Utils.gpsDistance(TxData.public_latitude,
	// TxData.public_longitude, txmsg_latitude,
	// txmsg_longitude);
	// txmsg.lbs_direction = Utils.gpsDirection(TxData.public_latitude,
	// TxData.public_longitude, txmsg_latitude,
	// txmsg_longitude);
	//
	// }

	public static Bitmap fitSizeImg(String path) throws OutOfMemoryError {
		return fitSizeImg(path, 0);
	}

	// 按图片大小缩放图片
	public static Bitmap fitSizeImg(String path, int scaleNum)
			throws OutOfMemoryError {
		if (TextUtils.isEmpty(path))
			return null;
		File file = new File(path);
		if (!file.exists()) {
			return null;
		}
		BitmapFactory.Options opts = new BitmapFactory.Options();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (file.length() < 300 * 1024) { // 0-0.3M
			opts.inSampleSize = 1;
		} else if (file.length() < 800 * 1024) { // 0.3M-0.8M
			opts.inSampleSize = 2;
		} else if (file.length() < 1024 * 1024) { // 0.8M-1M
			opts.inSampleSize = 3;
		} else if (file.length() < 2 * 1024 * 1024) { // 1M-2M
			opts.inSampleSize = 4;
		} else if (file.length() < 5 * 1024 * 1024) { // 2M-5M
			opts.inSampleSize = 8;
		} else if (file.length() < 10 * 1024 * 1024) { // 5M-10M
			opts.inSampleSize = 10;
		} else {
			opts.inSampleSize = 50;
		}
		if (scaleNum > 0) {
			opts.inSampleSize = opts.inSampleSize * scaleNum;
		}

		return BitmapFactory.decodeFile(file.getPath(), opts);
	}

	// 按图片大小缩放图片
	public static Bitmap fitSizeImg(InputStream is, int scaleNum)
			throws OutOfMemoryError, IOException {
		if (is == null)
			return null;
		BitmapFactory.Options opts = new BitmapFactory.Options();
		int streamLength = is.available();
		// 数字越大读出的图片占用的heap越小 不然总是溢出
		if (streamLength < 300 * 1024) { // 0-0.3M
			opts.inSampleSize = 1;
		} else if (streamLength < 800 * 1024) { // 0.3M-0.8M
			opts.inSampleSize = 2;
		} else if (streamLength < 1024 * 1024) { // 0.8M-1M
			opts.inSampleSize = 3;
		} else if (streamLength < 2 * 1024 * 1024) { // 1M-2M
			opts.inSampleSize = 4;
		} else if (streamLength < 5 * 1024 * 1024) { // 2M-5M
			opts.inSampleSize = 8;
		} else if (streamLength < 10 * 1024 * 1024) { // 5M-10M
			opts.inSampleSize = 10;
		} else {
			opts.inSampleSize = 50;
		}
		if (scaleNum > 0) {
			opts.inSampleSize = opts.inSampleSize * scaleNum;
		}

		return BitmapFactory.decodeStream(is, null, opts);
	}

	/**
	 * 退出进程逻辑
	 */
	public static void exitProcessLogic(Activity act) {
		initLoginSession(txdata);
		NotificationPopupWindow.cancelNotification(txdata);
		// editor.putString(CommonData.EXIT, CommonData.USER_EXIT).commit();
		// mSess.mPrefMeme.exit.setVal(PrefsMeme.USER_EXIT).commit();//这里应该不能关闭，否则下次进入不会自动登陆
		// 2014.03.06 shc
		LocationStation.gpsCancel();
		txdata.stopService();
		Utils.executorService.shutdownNow();
		// editor.putString(CommonData.DOOR, "close").commit();
		mSess.mPrefMeme.door.setVal("close").commit();
		TxData.finishAll();// 关闭所有的activity，添加这行语句有问题吗，应该是为了解决用微博登陆时LoginActivity没有finish的问题。2014.01.20
		Intent intent2 = new Intent(Intent.ACTION_MAIN);
		intent2.addCategory(Intent.CATEGORY_HOME);
		intent2.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		act.startActivity(intent2);
		if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
			android.os.Process.killProcess(android.os.Process.myPid());
		} else {
			ActivityManager am = (ActivityManager) txdata
					.getSystemService(Context.ACTIVITY_SERVICE);
			am.restartPackage(txdata.getPackageName());
			System.exit(0);
		}
		NotificationPopupWindow.cancelNotification(txdata);
	}

	// public static int SQRT(int nRoot) {
	// int nSqrt = 0;
	//
	// for (int i = 0x10000000; i != 0; i >>= 2) {
	// System.out.println("i="+i);
	// int nTemp = nSqrt + i;
	// System.out.println("nTemp="+nTemp);
	// nSqrt >>= 1;
	// if (nTemp <= nRoot) {
	// nRoot -= nTemp;
	// nSqrt += i;
	// }
	// System.out.println("nSqrt="+nSqrt);
	// }
	// return nSqrt;
	// }
	/**
	 * Inputstream转成String
	 * 
	 * @param is
	 *            输入流
	 * @return
	 * @throws IOException
	 */
	public static String is2String(InputStream is) throws IOException {
		byte[] buffer = new byte[8192 * 10];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, len));
		}
		return sb.toString();
	}

	/** 上传图片时合成的大小图的临时路径，在聊天室中用到 */
	public static String getUploadImageTempPath(long msgId) {
		String tempImgPath = Utils.getStoragePath(txdata) + File.separator
				+ Utils.IMAGE_PATH + File.separator + msgId + ".jpg";
		return tempImgPath;

	}

	/** 相册大图的生成方法 */
	public static String getAlbumFile(String filePath, Bitmap srcBitmap)
			throws IOException {
		Bitmap smallBitmap = ResizeBitmap(Utils.ImageCrop(srcBitmap), 92);
		if (TextUtils.isEmpty(filePath)) {
			// 文件路径为空
			return null;
		}
		File tempImg = new File(filePath);
		File parent = new File(tempImg.getParent());
		if (!parent.exists()) {
			parent.mkdirs();
		}
		if (tempImg != null && tempImg.exists()) {
			tempImg.delete();
		}
		tempImg.createNewFile();
		// 把小bitmap放在字节输出流中
		ByteArrayOutputStream small_baos = new ByteArrayOutputStream();
		smallBitmap.compress(Bitmap.CompressFormat.JPEG, 90, small_baos);
		// 把大bitmap放在输出流中
		ByteArrayOutputStream big_baos = new ByteArrayOutputStream();
		srcBitmap.compress(Bitmap.CompressFormat.JPEG, 90, big_baos);

		final int maxPixs = 1000000;// 最大像素数一百万

		final int FILE_HEAD_SIZE = 24;
		final int TS_VERSION = 1;
		int smallBitmapLength = small_baos.size();
		int bigBitmapLength = big_baos.size();
		int totalLength = smallBitmapLength + bigBitmapLength;
		if (Utils.debug) {
			Log.i(TAG, "upload total CompoundImg Image size is: " + totalLength);
		}
		// 大图的起始字节位置
		int bigStartPosition = smallBitmapLength + FILE_HEAD_SIZE;
		// int dataTotalSize = FILE_HEAD_SIZE + totalLength; //头信息和大小图字节的总长度

		if (totalLength + FILE_HEAD_SIZE > 1024 * 1024) {
			// 合成图大于1M,则压缩至总像素数不大于1000000
			int pixsCount = srcBitmap.getWidth() * srcBitmap.getHeight();
			float scale = 1;

			if (debug) {
				Log.e(TAG, "相册图片为：" + (totalLength + FILE_HEAD_SIZE) + ",大于1M！");
			}
			if (pixsCount > maxPixs) {
				// 总像素数大于1000000时，缩放至不大于1000000
				scale = maxPixs / pixsCount;
				if (debug) {
					Log.e(TAG, "需要上传的相册总像素数为：" + pixsCount + ",大于1000000,缩放！");
				}
			} else {
				// 这时该怎么处理？直接缩小至1/2？
				scale = 0.5f;
				if (debug) {
					Log.e(TAG, "需要上传的相册总像素数为：" + pixsCount + ",小于1000000,缩小到一半");
				}
			}
			Matrix matrix = new Matrix();
			matrix.postScale(scale, scale);
			Bitmap resizedBitmap = Bitmap.createBitmap(srcBitmap, 0, 0,
					srcBitmap.getWidth(), srcBitmap.getHeight(), matrix, true);
			// 把大bitmap放在输出流中
			big_baos = new ByteArrayOutputStream();
			resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 90, big_baos);

			bigBitmapLength = big_baos.size();
			totalLength = smallBitmapLength + bigBitmapLength;
		} else {
			if (debug) {
				Log.e(TAG, "相册图片为：" + (totalLength + FILE_HEAD_SIZE)
						+ ",小于1M，直接上传！");
			}
		}

		FileOutputStream fos = new FileOutputStream(tempImg);
		DataOutputStream dos = new DataOutputStream(fos);
		dos.writeInt(TS_VERSION);
		dos.writeInt(totalLength);
		dos.writeInt(FILE_HEAD_SIZE);
		dos.writeInt(smallBitmapLength);
		dos.writeInt(bigStartPosition);
		dos.writeInt(bigBitmapLength);
		small_baos.writeTo(dos);
		big_baos.writeTo(dos);
		dos.flush();
		dos.close();
		fos.close();
		small_baos.close();
		big_baos.close();

		return filePath;

	}

	// 上传相册成功后生成个人相册大小图
	public static void creatAlbumFile(String compoundFile, File folder,
			String fileName) throws IOException {

		StringBuffer smallTempPath = new StringBuffer()
				.append(folder.getAbsolutePath()).append("/").append(fileName)
				.append(".jpg");
		StringBuffer bigTempPath = new StringBuffer()
				.append(folder.getAbsolutePath()).append("/").append(fileName)
				.append("_big.jpg");

		FileOutputStream smallFos = new FileOutputStream(
				smallTempPath.toString());
		FileOutputStream bigFos = new FileOutputStream(bigTempPath.toString());
		byte[] buffer = new byte[4 * 1024];

		DataInputStream dis = new DataInputStream(new FileInputStream(
				compoundFile));
		dis.skipBytes(3 * 4);// 跳过3个字节
		int smallBitmapLength = dis.readInt();
		int bigStartPosition = dis.readInt();
		int bigBitmapLength = dis.readInt();

		// 创建小图片文件
		int count = smallBitmapLength / buffer.length;
		for (int i = 0; i < count; i++) {
			// 例如：smallBitmapLength = 3k，
			dis.read(buffer);
			smallFos.write(buffer);
		}
		if (smallBitmapLength > buffer.length * count) {
			dis.read(buffer, 0, smallBitmapLength - buffer.length * count);
			smallFos.write(buffer, 0, smallBitmapLength - buffer.length * count);
		}
		if (debug) {
			Log.i(TAG, "上传相册图片成功后，相册小图生成成功");
		}

		// 创建大图片

		count = bigBitmapLength / buffer.length;
		for (int i = 0; i < count; i++) {
			// 例如：smallBitmapLength = 3k，
			dis.read(buffer);
			bigFos.write(buffer);
		}
		if (bigBitmapLength > buffer.length * count) {
			dis.read(buffer, 0, bigBitmapLength - buffer.length * count);
			bigFos.write(buffer, 0, bigBitmapLength - buffer.length * count);
		}
		if (debug) {
			Log.i(TAG, "上传相册图片成功后，生成相册大图成功");
		}

		dis.close();
		smallFos.close();
		bigFos.close();

	}

	// 上传瞬间图片成功后，生成瞬间图片
	public static void creatBlogFile(String compoundFile, File folder,
			String fileName) throws IOException {

		StringBuffer bigTempPath = new StringBuffer()
				.append(folder.getAbsolutePath()).append("/").append(fileName)
				.append(".jpg");

		FileOutputStream bigFos = new FileOutputStream(bigTempPath.toString());
		byte[] buffer = new byte[4 * 1024];

		DataInputStream dis = new DataInputStream(new FileInputStream(
				compoundFile));
		dis.skipBytes(3 * 4);// 跳过3个字节
		int smallBitmapLength = dis.readInt();
		int bigStartPosition = dis.readInt();
		int bigBitmapLength = dis.readInt();

		// 读取小图片文件
		int count = smallBitmapLength / buffer.length;
		for (int i = 0; i < count; i++) {
			// 例如：smallBitmapLength = 3k，
			dis.read(buffer);
		}
		if (smallBitmapLength > buffer.length * count) {
			dis.read(buffer, 0, smallBitmapLength - buffer.length * count);
		}
		if (debug) {
			Log.i(TAG, "上传相册图片成功后，相册小图生成成功");
		}

		// 创建大图片
		count = bigBitmapLength / buffer.length;
		for (int i = 0; i < count; i++) {
			// 例如：smallBitmapLength = 3k，
			dis.read(buffer);
			bigFos.write(buffer);
		}
		if (bigBitmapLength > buffer.length * count) {
			dis.read(buffer, 0, bigBitmapLength - buffer.length * count);
			bigFos.write(buffer, 0, bigBitmapLength - buffer.length * count);
		}
		if (debug) {
			Log.i(TAG, "上传相册图片成功后，生成相册大图成功");
		}

		dis.close();
		bigFos.close();
	}

	/** 给一个路径，判断该路径是否存在，不存在则创建 */
	public static boolean creatFolder(String folderPath) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			File folderFile = new File(folderPath);
			if (!folderFile.exists()) {
				folderFile.mkdirs();
			}
			return true;
		}
		return false;

	}

	/** 把assets文件夹中的json转换为String */
	public static String getAssetsString(Context context, String fileName)
			throws IOException {

		InputStream is = context.getAssets().open(fileName);
		byte[] buffer = new byte[8192 * 10];
		StringBuilder sb = new StringBuilder();
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			sb.append(new String(buffer, 0, len));
		}
		return sb.toString();

	}

	/** duration的单位是秒 */
	public static String formatDurationTimes(long duration) {
		return String.format("%02d:%02d", duration / 60, duration % 60);
	}

	/**
	 * 旋转动画
	 * 
	 * @param degree
	 *            旋转角度
	 * @param duration
	 *            动画展示时长
	 * @param isClockwise
	 *            是否是顺时方向
	 * @return
	 */
	public static Animation getAnimationRotate(float formDegrees,
			float toDegrees) {
		// long duration = (long) ((Math.abs(formDegrees - toDegrees) / 45) *
		// 200);//四十五度200毫秒的比例增加时长
		long duration = 200;
		RotateAnimation rotate = new RotateAnimation(formDegrees, toDegrees,
				Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
				0.5f);
		rotate.setDuration(duration);
		// rotate.setInterpolator(new AccelerateDecelerateInterpolator());
		rotate.setFillEnabled(true);
		rotate.setFillAfter(true);
		return rotate;
	}

	/** 上传大文件 */
	public static void uploadBigFile(final TXMessage txmsg,
			DownUploadListner uploadListener, final Handler handler) {
		if (!Utils.isNull(txmsg.msg_path)) {
			if (uploadListener == null) {
				uploadListener = new DownUploadListner() {

					@Override
					public void onFinish(FileTaskInfo taskInfo) {

						String fileUrl = taskInfo.mServerHost + ":"
								+ taskInfo.mPath + ":" + taskInfo.mTime;
						if (Utils.debug) {
							Log.i(TAG, "上传的大文件fileUrl-->" + fileUrl);
						}
						txmsg.updateState = TXMessage.UPDATE_OK;
						txmsg.msg_url = fileUrl;
						if (txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
							// 群大文件消息
							mSess.getSocketHelper().sendGroupMsg(txmsg);
						} else if (txmsg.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS) {
							// 个人大文件消息
							mSess.getSocketHelper().sendSingleMessage(txmsg);
						}
						saveTxMsgToDB(txmsg);
						if (handler != null) {
							flushOneLine(handler, txmsg);// 单条刷新
						}
					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
						String fileUrl = taskInfo.mServerHost + ":"
								+ taskInfo.mPath + ":" + taskInfo.mTime;
						if (Utils.debug) {
							Log.i(TAG, "上传失败，大文件的fileUrl-->" + fileUrl);
						}
						txmsg.msg_url = fileUrl;
						txmsg.read_state = TXMessage.FAIL;
						txmsg.updateState = TXMessage.UPDATE_FAILE;
						saveTxMsgToDB(txmsg);
						if (handler != null) {
							flushOneLine(handler, txmsg);
						}
					}
				};
			}
			initLoginSession(txdata);
			mSess.mDownUpMgr.uploadBigFile(txmsg.msg_path, 1, false,
					txmsg.msg_url, uploadListener);
		}

	}

	/** 上传gif包 */
	public static void uploadGifBag(String file_path,
			GifDownUploadListner uploadListener) {
		if (!Utils.isNull(file_path)) {
			if (uploadListener == null) {
				uploadListener = new GifDownUploadListner() {

					@Override
					public void onFinish(GifTaskInfo taskInfo) {

					}

					@Override
					public void onError(GifTaskInfo taskInfo, int iCode,
							Object iPara) {
						super.onError(taskInfo, iCode, iPara);

					}
				};

			}
			initLoginSession(txdata);
			mSess.mGifTransfer.uploadGifBag(file_path, 6, 1, null,
					uploadListener, false);
		}
	}

	/** 单条刷新聊天室listView */
	public static void flushOneLine(Handler handler, TXMessage txmsg) {
		if (handler != null) {
			handler.obtainMessage(BaseMsgRoom.FLUSH_ONE_LINE, txmsg)
					.sendToTarget();
		}
	}

	/** 下载大文件 */
	public static void downloadBigFile(TXMessage txmsg,
			DownUploadListner mBigFileDownloadCallback, final Handler handler) {

		if (!Utils.isNull(txmsg.msg_url)) {
			if (mBigFileDownloadCallback == null) {
				mBigFileDownloadCallback = new DownUploadListner() {

					@Override
					public void onStart(FileTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						txmsg.updateState = TXMessage.UPDATING;
						saveTxMsgToDB(txmsg);
					}

					@Override
					public void onProgress(FileTaskInfo taskInfo, int curlen,
							int totallen) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						txmsg.updateCount = (int) (((double) curlen) * 100.0 / (double) totallen);
						flushOneLine(handler, txmsg);
					}

					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						if (txmsg != null) {
							txmsg.msg_path = taskInfo.mFullName;
							txmsg.updateState = TXMessage.UPDATE_OK;
							Utils.saveTxMsgToDB(txmsg);
							flushOneLine(handler, txmsg);
							// 写文件增加新增文件数目
							String fileName = txmsg.msg_path
									.substring(txmsg.msg_path.lastIndexOf("/") + 1);
							int fileType = 0;
							if (fileName.contains(".")) {
								fileType = FileManager.getFileType(txdata,
										fileName.substring(fileName
												.lastIndexOf(".") + 1));
							} else {
								fileType = FileManager.FILE_TYPE_UNKOWN;
							}
							try {
								increaseNewFileCount(txdata, fileType);
							} catch (Exception e) {
								if (Utils.debug)
									Log.e(TAG, "自增新收到的大文件个数异常", e);
							}
						}
					}

					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						txmsg.updateState = TXMessage.UPDATE_FAILE;
						saveTxMsgToDB(txmsg);
						flushOneLine(handler, txmsg);
					}
				};
			}
			initLoginSession(txdata);
			mSess.mDownUpMgr.downloadBigFile(txmsg.msg_url, 1,
					mBigFileDownloadCallback, (int) txmsg.msg_file_length,
					txmsg);
		}
	}

	/** 调用系统音乐播放器播放音乐 */
	public static void playAudio(Activity activity, String audioPath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + audioPath), "audio/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 调用系统视频播放器播放视频 */
	public static void playVideo(Activity activity, String audioPath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + audioPath), "video/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 打开文档 */
	public static void openDocFile(Activity activity, String filePath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		String prefix = filePath.substring(filePath.lastIndexOf(".") + 1);
		if (prefix.equalsIgnoreCase("doc")) {
			intent.setDataAndType(Uri.parse("file://" + filePath),
					"application/msword");
		} else if (prefix.equalsIgnoreCase("xls")) {
			intent.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.ms-excel");
		} else if (prefix.equalsIgnoreCase("ppt")) {
			intent.setDataAndType(Uri.parse("file://" + filePath),
					"application/vnd.ms-powerpoint");
		} else if (prefix.equalsIgnoreCase("pdf")) {
			intent.setDataAndType(Uri.parse("file://" + filePath),
					"application/pdf");
		} else if (prefix.equalsIgnoreCase("rtf")) {
			intent.setDataAndType(Uri.parse("file://" + filePath),
					"application/rtf");
		} else {
			intent.setDataAndType(Uri.parse("file://" + filePath), "text/*");
		}
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 用其他应用打开图片 */
	public static void openPicture(Activity activity, String filePath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + filePath), "image/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 打开APK软件 */
	public static void openAPKFile(Activity activity, String apkPath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + apkPath),
				"application/vnd.android.package-archive");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 打开未知应用 */
	public static void openUnkownFile(Activity activity, String filePath)
			throws Exception {
		Intent intent = new Intent();
		intent.setAction(android.content.Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://" + filePath), "*/*");
		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		activity.startActivity(intent);
	}

	/** 打开浏览器打开网页 */
	public static void openUrlWithBrowser(Activity activity, String url)
			throws Exception {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		activity.startActivity(intent);
	}

	/** 格式化显示文件大小 */
	public static String formatFileLength(long size) {
		final int KB = 1024;
		final int MG = KB * KB;
		final int GB = MG * KB;
		String display_size;

		if (size > GB)
			display_size = String.format("%.2f Gb ", (double) size / GB);
		else if (size < GB && size > MG)
			display_size = String.format("%.2f Mb ", (double) size / MG);
		else if (size < MG && size > KB)
			display_size = String.format("%.2f Kb ", (double) size / KB);
		else
			display_size = String.format("%.2f bytes ", (double) size);

		return display_size;

	}

	/** 保存图片到系统相册 */
	public static void savePictureToGallery(Context context, Bitmap bm) {
		ContentResolver cr = context.getContentResolver();
		MediaStore.Images.Media.insertImage(cr, bm, "", "");
		context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri
				.parse("file://" + Environment.getExternalStorageDirectory())));

	}

	/** 创建对话框菜单 */
	public static AlertDialog creatDialogMenu(Activity activity,
			final String[] items, String title,
			DialogInterface.OnClickListener onItemClickListener) {
		AlertDialog menuDialog = new AlertDialog.Builder(activity)
				.setTitle(title).setItems(items, onItemClickListener).show();
		return menuDialog;

	}

	/** 创建对话框 */
	public static AlertDialog creatDialog(Activity activity, String message,
			DialogInterface.OnClickListener onPositiveClickListener,
			DialogInterface.OnClickListener onNegativeClickListener) {
		AlertDialog menuDialog = new AlertDialog.Builder(activity)
				.setTitle("提示").setMessage(message)
				.setPositiveButton("确定", onPositiveClickListener)
				.setNegativeButton("取消", onNegativeClickListener).show();
		return menuDialog;
	}

	/** 创建对话框 */
	public static AlertDialog creatDialog(Activity activity, String message,
			String title, String positiveStr,
			DialogInterface.OnClickListener onPositiveClickListener,
			String negativeStr,
			DialogInterface.OnClickListener onNegativeClickListener) {
		AlertDialog menuDialog = new AlertDialog.Builder(activity)
				.setTitle(title).setMessage(message)
				.setPositiveButton(positiveStr, onPositiveClickListener)
				.setNegativeButton(negativeStr, onNegativeClickListener).show();
		return menuDialog;
	}

	/** 创建对话框 */
	public static AlertDialog creatDialog(Activity activity, int message,
			int title, int positiveStr,
			DialogInterface.OnClickListener onPositiveClickListener,
			int negativeStr,
			DialogInterface.OnClickListener onNegativeClickListener) {
		AlertDialog menuDialog = new AlertDialog.Builder(activity)
				.setTitle(title).setMessage(message)
				.setPositiveButton(positiveStr, onPositiveClickListener)
				.setNegativeButton(negativeStr, onNegativeClickListener).show();
		return menuDialog;
	}

	/** 修改新收到的大文件数 */
	public static void changeNewFileCount(Context context, int fileType,
			boolean isCleanCount) throws Exception {
		File contextFile = context.getFilesDir();
		if (Utils.debug) {
			// 内存文件路径：/data/data/com.tuixin11sms.tx/files
			Log.i(TAG, "内存文件路径：" + contextFile.getAbsolutePath());
		}
		File targetFile = new File(contextFile, NEW_FILE_COUNT_FILE);
		String jsonStr = fileToString(targetFile);
		JSONObject jsonObj = null;
		if (TextUtils.isEmpty(jsonStr)) {
			jsonObj = new JSONObject();
		} else {
			jsonObj = new JSONObject(jsonStr);
		}
		JSONObject userJsonObj = jsonObj.optJSONObject("" + TX.tm.getUserID());
		if (userJsonObj == null) {
			userJsonObj = new JSONObject();
			userJsonObj.put("" + fileType, 1);
			jsonObj.put("" + TX.tm.getUserID(), userJsonObj);
		} else {
			userJsonObj.put("" + fileType,
					userJsonObj.optInt("" + fileType) + 1);// 把对应文件类型自增1
		}
		if (isCleanCount) {
			// 如果判定是清空新增文件数据，则直接把对应值置为0
			userJsonObj.put("" + fileType, 0);
		}

		FileWriter fw = new FileWriter(targetFile);
		fw.write(jsonObj.toString());
		fw.flush();
		fw.close();
	}

	/** 自增新收到的大文件数 */
	public static void increaseNewFileCount(Context context, int fileType)
			throws Exception {
		changeNewFileCount(context, fileType, false);

	}

	/** 清除新收到的大文件数目 */
	public static void clearNewFileCount(Context context, int fileType)
			throws Exception {
		changeNewFileCount(context, fileType, true);
	}

	/** 返回新增文件数目 */
	public static Map<Integer, Integer> getNewFileCount(Context context)
			throws Exception {
		Map<Integer, Integer> map = new HashMap<Integer, Integer>();
		File contextFile = context.getFilesDir();
		File targetFile = new File(contextFile, NEW_FILE_COUNT_FILE);
		String jsonStr = fileToString(targetFile);
		JSONObject jsonObj = null;
		if (TextUtils.isEmpty(jsonStr)) {
			jsonObj = new JSONObject();
		} else {
			jsonObj = new JSONObject(jsonStr);
		}
		JSONObject userJsonObj = jsonObj.optJSONObject("" + TX.tm.getUserID());
		if (userJsonObj == null) {
			userJsonObj = new JSONObject();
		}
		map.put(FileManager.FILE_TYPE_PICTURE,
				userJsonObj.optInt("" + FileManager.FILE_TYPE_PICTURE));
		map.put(FileManager.FILE_TYPE_AUDIO,
				userJsonObj.optInt("" + FileManager.FILE_TYPE_AUDIO));
		map.put(FileManager.FILE_TYPE_VIDEO,
				userJsonObj.optInt("" + FileManager.FILE_TYPE_VIDEO));
		map.put(FileManager.FILE_TYPE_DOC,
				userJsonObj.optInt("" + FileManager.FILE_TYPE_DOC));
		map.put(FileManager.FILE_TYPE_UNKOWN,
				userJsonObj.optInt("" + FileManager.FILE_TYPE_UNKOWN));

		return map;
	}

	/** 文件变为字符串 */
	public static String fileToString(File file) throws IOException {
		if (!file.exists()) {
			file.createNewFile();
		}
		FileReader fr = new FileReader(file);
		char[] buffer = new char[2 * 1024];
		StringBuffer sb = new StringBuffer();
		int length = 0;
		while ((length = fr.read(buffer)) != -1) {
			sb.append(buffer, 0, length);
		}
		fr.close();
		return sb.toString();
	}

	// /**
	// * @param sourceImg
	// * 要传入的图片
	// * @param number
	// * 设置的透明值 1~100之间的
	// * @return 返回设置完成的图片
	// */
	// public static Bitmap setAlpha(Bitmap sourceImg, int number) {
	// int[] argb = new int[sourceImg.getWidth() * sourceImg.getHeight()];
	// sourceImg.getPixels(argb, 0, sourceImg.getWidth(), 0, 0,
	// sourceImg.getWidth(), sourceImg.getHeight());// 获得图片的ARGB值
	// number = number * 255 / 100;
	// for (int i = 0; i < argb.length; i++) {
	// argb[i] = (number << 24) | (argb[i] & 0x00FFFFFF);
	// }
	// sourceImg = Bitmap.createBitmap(argb, sourceImg.getWidth(),
	// sourceImg.getHeight(), Config.ARGB_8888);
	// return sourceImg;
	// }

	/**
	 * 根据指定的图像路径和大小来获取缩略图 此方法有两点好处： 1.
	 * 使用较小的内存空间，第一次获取的bitmap实际上为null，只是为了读取宽度和高度，
	 * 第二次读取的bitmap是根据比例压缩过的图像，第三次读取的bitmap是所要的缩略图。 2.
	 * 缩略图对于原图像来讲没有拉伸，这里使用了2.2版本的新工具ThumbnailUtils，使 用这个工具生成的图像不会被拉伸。
	 * 
	 * @param imagePath
	 *            图像的路径
	 * @param width
	 *            指定输出图像的宽度
	 * @param height
	 *            指定输出图像的高度
	 * @return 生成的缩略图
	 */
	@SuppressLint("NewApi")
	public static Bitmap getImageThumbnail(String imagePath, int width,
			int height) {
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// 获取这个图片的宽和高，注意此处的bitmap为null
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		options.inJustDecodeBounds = false; // 设为 false
		// 计算缩放比
		int h = options.outHeight;
		int w = options.outWidth;
		int beWidth = w / width;
		int beHeight = h / height;
		int be = 1;
		if (beWidth < beHeight) {
			be = beWidth;
		} else {
			be = beHeight;
		}
		if (be <= 0) {
			be = 1;
		}
		options.inSampleSize = be;
		// 重新读入图片，读取缩放后的bitmap，注意这次要把options.inJustDecodeBounds 设为 false
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		// 利用ThumbnailUtils来创建缩略图，这里要指定要缩放哪个Bitmap对象
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height,
				ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
	}

	/** 取得本地文件名字 */
	public static String getLocalFileName(String localFilePath) {
		if (!TextUtils.isEmpty(localFilePath)) {
			if (localFilePath.contains("/")) {
				return localFilePath
						.substring(localFilePath.lastIndexOf("/") + 1);
			} else {
				return localFilePath;
			}
		} else {
			return "";
		}
	}

	/** 获取大文件消息的图标 */
	public static int getBigFileThumb(String fileName, int msgUpdateState) {
		int thumbResId = 0;
		if (!TextUtils.isEmpty(fileName) && fileName.contains(".")) {
			String suffix = fileName.substring(fileName.lastIndexOf(".") + 1);
			if (suffix.equalsIgnoreCase("mp3")) {
				if (msgUpdateState == TXMessage.UPDATE_OK) {
					// MP3下载完成
					thumbResId = R.drawable.thumb_music_room_big_file_received;
				} else {
					thumbResId = R.drawable.thumb_music_room_big_file_unreceived;
				}
			} else if (suffix.equalsIgnoreCase("mp4")) {
				if (msgUpdateState == TXMessage.UPDATE_OK) {
					// 下载完成
					thumbResId = R.drawable.thumb_video_room_big_file_received;
				} else {
					thumbResId = R.drawable.thumb_video_room_big_file_unreceived;
				}
			} else {
				thumbResId = R.drawable.thumb_unkown_room_big_file;
			}
		} else {
			thumbResId = R.drawable.thumb_unkown_room_big_file;
		}
		return thumbResId;
	}

	/** 打开查看大文件消息 */
	// 返回值处理的不好，有待改进
	public static int openBigFile(Activity thisContext, TXMessage txmsg) {
		String filePath = txmsg.msg_path;
		File bigFile = new File(filePath);
		if (!bigFile.exists()) {
			txmsg.updateState = TXMessage.UPDATE;// 重置下载状态，让该消息可以再次被下载
			Utils.saveTxMsgToDB(txmsg);
			// flush(FLUSH_ONE_LINE, txmsg);
			// showToast("该文件不存在，请重新下载");
			return -1;
		}
		Intent intent = new Intent();
		if (!TextUtils.isEmpty(filePath) && filePath.contains(".")) {
			String suffix = filePath.substring(filePath.lastIndexOf(".") + 1);
			if (suffix.equalsIgnoreCase("mp3")) {
				// 播放MP3
				intent.setClass(thisContext, MusicPlayActivity.class);
				intent.putExtra(Constants.FILE_LOCAL_PATH, filePath);
				thisContext.startActivity(intent);
			} else if (suffix.equalsIgnoreCase("mp4")) {
				// 播放MP4
				intent.setClass(thisContext, VideoPlayActivity.class);
				// intent.setClass(thisContext, VideoCaptureActivity.class);
				// intent.putExtra(VideoCaptureActivity.IS_RECORD_VIDEO,
				// false);
				intent.putExtra(Constants.FILE_LOCAL_PATH, filePath);
				thisContext.startActivity(intent);
			} else {
				if (FileManager.getFileType(txdata, suffix) == FileManager.FILE_TYPE_PICTURE) {
					// 图片文件;
					intent.setClass(thisContext, GalleryFileActivity.class);
					ArrayList<String> imgPathList = new ArrayList<String>();
					imgPathList.add(filePath);
					intent.putStringArrayListExtra(
							GalleryFileActivity.IMAGE_PATH_LIST, imgPathList);
					thisContext.startActivity(intent);
				} else {
					// 未知文件
					intent.setClass(thisContext, UnkownFileActivity.class);
					intent.putExtra(Constants.FILE_LOCAL_PATH, filePath);
					thisContext.startActivity(intent);
				}
			}
		} else {
			intent.setClass(thisContext, UnkownFileActivity.class);
			intent.putExtra(Constants.FILE_LOCAL_PATH, filePath);
			thisContext.startActivity(intent);
		}
		return 0;

	}

	/** 获取程序版本号 */
	public static int getVersionCode(Context context) {
		PackageManager manager = context.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(context.getPackageName(),
					0);
			return info.versionCode; // 版本名
		} catch (NameNotFoundException e) {
			if (Utils.debug)
				Log.e(TAG, "没有找到版本号异常", e);
		}
		return 0;
	}

	/** 发送广播，让TuixinService退出 */
	public static void broadcastExitMsg(Context context) {
		Intent intent = new Intent(Constants.INTENT_ACTION_SEND_MSG);
		intent.putExtra("exit", "exit");
		context.sendBroadcast(intent);

	}

	/** 格式化消息中的发送时间 */
	public static Date formatDateTime(long time) {
		Date date;
		if (("" + time).length() >= 13 && ("" + time).length() < 16) {
			date = new Date(time);
		} else if (("" + time).length() >= 16) {
			date = new Date(time / 1000);
		} else {
			date = new Date(time * 1000);
		}

		return date;
	}

	/** 格式化消息中的发送时间,显示全部的年月日 */
	public static String formatFullTimeStr(long time) {
		if ((time + "").length() == 10) {
			time = time * 1000;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("yy-MM-dd HH:mm:ss");
		String dateStr = sdf.format(new Date(time));
		return dateStr;
	}

	/** 格式化消息中的发送时间 */
	public static String formatTimeStr(long time) {
		return formatTimeStr(time, false);
	}

	/** 格式化消息中的发送时间 */
	public static String formatTimeStr(long time, boolean is24Hour) {
		if ((time + "").length() == 10) {
			time = time * 1000;
		}
		SimpleDateFormat sdf = null;
		if (is24Hour) {
			sdf = new SimpleDateFormat("MM-dd HH:mm");
		} else {
			sdf = new SimpleDateFormat("MM-dd hh:mm");
		}
		String dateStr = sdf.format(new Date(time));
		return dateStr;
	}

	private static String severKey = "A23e78da413b1819a422d1fb4c361cf4";
	private static String formatted_address;

	/**
	 * 根据经纬度获取地理位置
	 * 
	 * @param lat
	 * @param lon
	 */
	public static String getAddress(final double lat, final double lon) {
		try {
			String path = "http://api.map.baidu.com/geocoder/v2/?ak="
					+ severKey + "&callback=renderReverse&location=" + lat
					+ "," + lon + "&output=json";
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			if (code == 200) {
				// 服务器返回的数据的长度 实际上就是文件的长度

				InputStream is = conn.getInputStream();
				byte[] temp = new byte[1024];
				StringBuffer sb = new StringBuffer();
				int len;
				while ((len = is.read(temp)) != -1) {
					String downloadLen = new String(temp, 0, len);
					sb.append(downloadLen);
				}

				// 解析JSON
				String[] address = sb.toString().split("\\(");
				String[] addrJson = address[1].split("\\)");

				JSONObject jsonObject = new JSONObject(addrJson[0]);

				// 获取结构化地址信息
				formatted_address = jsonObject.getJSONObject("result")
						.getString("formatted_address");

				// 获取地址信息
				JSONObject addressComponent = jsonObject
						.getJSONObject("result").getJSONObject(
								"addressComponent");
				String city = addressComponent.getString("city");
				String district = addressComponent.getString("district");
				String province = addressComponent.getString("province");
				String street = addressComponent.getString("street");
				String street_number = addressComponent
						.getString("street_number");
			} else {
				formatted_address = "服务器错误.";
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return formatted_address;
	}

	/**
	 * 根据经纬度获取地理位置
	 * 
	 * @param lat
	 * @param lon
	 */
	public static String getCity(final double lat, final double lon) {
		try {
			String path = "http://api.map.baidu.com/geocoder/v2/?ak="
					+ severKey + "&callback=renderReverse&location=" + lat
					+ "," + lon + "&output=json";
			URL url = new URL(path);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setRequestMethod("GET");
			int code = conn.getResponseCode();
			if (code == 200) {
				// 服务器返回的数据的长度 实际上就是文件的长度

				InputStream is = conn.getInputStream();
				byte[] temp = new byte[1024];
				StringBuffer sb = new StringBuffer();
				int len;
				while ((len = is.read(temp)) != -1) {
					String downloadLen = new String(temp, 0, len);
					sb.append(downloadLen);
				}

				// 解析JSON
				String[] address = sb.toString().split("\\(");
				String[] addrJson = address[1].split("\\)");

				JSONObject jsonObject = new JSONObject(addrJson[0]);

				// 获取地址信息
				JSONObject addressComponent = jsonObject
						.getJSONObject("result").getJSONObject(
								"addressComponent");
				String city = addressComponent.getString("city");
				formatted_address = city;
			} else {
				formatted_address = "服务器错误.";
			}
		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
		}
		return formatted_address;
	}

}
