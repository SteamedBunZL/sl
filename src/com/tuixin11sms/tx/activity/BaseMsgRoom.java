package com.tuixin11sms.tx.activity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import u.aly.da;

import android.R.integer;
import android.app.AlertDialog;
import android.app.NativeActivity;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;
import android.text.ClipboardManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.PopupWindow.OnDismissListener;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.activity.GroupMember;
import com.shenliao.group.activity.GroupTip;
import com.shenliao.group.activity.GroupWarn;
import com.shenliao.group.util.GroupUtils;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.shenliao.user.expressionpacage.ExpressionPackage;
import com.shenliao.view.MyGroupListView;
import com.shenliao.view.SlGridView;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.activity.explorer.ThumbnailCreator;
import com.tuixin11sms.tx.audio.manager.ClientManager;
import com.tuixin11sms.tx.callbacks.RecordListener;
import com.tuixin11sms.tx.contact.Contact;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MyPopupWindow;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.PraiseMsgCallBack;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.gif.ClickGifMessage;
import com.tuixin11sms.tx.gif.FolderExplorerGifActivity;
import com.tuixin11sms.tx.gif.GifTranscoldMgr;
import com.tuixin11sms.tx.gif.GifTranscoldMgr.GifDecodeListener;
import com.tuixin11sms.tx.gif.gifOpenHelper;
import com.tuixin11sms.tx.group.GifTransfer;
import com.tuixin11sms.tx.group.GifTransfer.GTaskForId;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.group.GifTransfer.GifDownUploadListner;
import com.tuixin11sms.tx.group.GifTransfer.GifDownloadTask;
import com.tuixin11sms.tx.group.GifTransfer.GifTaskInfo;
import com.tuixin11sms.tx.message.MessageComparator;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.DownUploadListner;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.task.FileTransfer.UploadAudioTask;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.DateUtils;
import com.tuixin11sms.tx.utils.LocationStation;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.MusicUtils;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.MsgRoomMainLayout;
import com.tuixin11sms.tx.view.PlayAdiouAnimation;
import com.tuixin11sms.tx.view.PraisedAnim;
import com.tuixin11sms.tx.view.ReceiveGreatAnim;
import com.tuixin11sms.tx.view.WaitAdiouAnimation;
import com.tx.face.nativef.IParseFaceObserver;
import com.tx.face.nativef.NativeFace;

public abstract class BaseMsgRoom extends BaseActivity implements
		OnTouchListener {
	private static final String TAG = "BaseMsgRoom";
	public static final String TAG_HEAD = "BaseMsgRoomHead";// 测试头像专用TAG
	public static final String MSG_FILE_LIST = "msg_file_list";// 消息附件的地址集合
	/***** 主view上的控件变量 ******/
	protected ImageButton sendTypeButton;// 发送音频和文本的切换按钮
	protected ImageButton downMoreButton;// 下功能栏点击更多按钮
	protected ImageView moreButton;// 更多按钮
	protected RelativeLayout downlayout;// 下功能栏
	protected EditText msgEdit;// 输入框
	protected Button btn_recordShortAduio;// 录音按钮
	protected TextView SendButton;// 发送按钮
	protected TextView personName;// 聊天对象名称
	protected ViewGroup rl_praised_anim_view;
	protected MyGroupListView msgRoomList;
	protected boolean mbBottom = true;// 记录当前状态，解决加载多个连续的图片时，最后一个图片随着上面图片加载，消息框撑大，位置不再是屏幕最底部的问题
	protected CharSequence sssss;
	public boolean isStartAudioPlay = false;// 是否开启自动播放
	protected int densityDpi;
	private ThumbnailCreator mThumbnail;
	private String receivedFolderPath;
	private ArrayList<String> mDataSource;// 已接受大文件目录下所有文件名的集合
	/***** handler相关变量 ******/
	protected final static int FLUSH_ROOM_FALSE = 101;
	protected final static int FLUSH_ROOM_TRUE = 107;
	protected final static int FLUSH_LISTVIEW_TRUE = 10117;
	protected final static int FLUSH_LISTVIEW_FALSE = 10111;
	public final static int FLUSH_ONE_LINE = 10118;// 单条刷新
	protected final static int PLAY_AUDIO_C0MPELET = 423;
	protected final static int FLUSH_RCORDEND_BUTTON = 6789;
	protected final static int FLUSH_VOLUEM_AN = 2089;
	protected final static int AUTO_SEND = 1076;
	protected final static int FLUSH_ROOM_INIT = 117;
	protected final static int UPDATE_WINDOWCOMPLETE = 0x245;
	protected final static int FLUSH_NOTICE = 10000;
	protected final static int HANDLE_RECORDER_ERROR = 1;
	protected final static int FLUSH_PROGRESS_TIME = 2;
	protected final static int FLUSH_SEEK_TEIME = 3;
	protected final static int DISMISS_MSG_ROOM_POPUP = 4;// 隐藏聊天室公告、右上角和下面工具栏的popupWindow

	protected final static int CANCLE_SHORT_RECORD = 10;
	protected final static int CANCLE_LONG_RECORD = 11;

	protected final static int RECORD_TIME_SHORT = 12;// 录音时间太短

	protected final static int SET_GEO_MSG = 4455;// 地理位置

	/******* result返回消息 *****/
	public static final int REQ_TAKE_PHOTO = 4;
	public static final int REQ_TAKE_PICTURE = 5;
	public static final int REQ_TAKE_BIG_FILES = 6;
	public static final int REQ_TAKE_MUSIC_FILES = 8;// 发送音乐大文件
	public static final int REQ_EDIT_PICTURE = 7;
	public static final int DOWNLOAD_VIEW_BIG_PIC = 9;// 下载并查看图片大图
	public static final int REQ_TAKE_GIF_FILE = 11;
	public static final int REQ_TAKE_GIF_SETTING = 12;

	public PlayAdiouAnimation playRecordAn;
	public GroupContectListAdapter contectsListAdapter;
	/***** 其他变量 ******/
	public ArrayList<TXMessage> txMsgs = new ArrayList<TXMessage>();// 应该是聊天室中所有的消息
	public ArrayList<TXMessage> synchronizedMsgs = new ArrayList<TXMessage>();
	// public ArrayList<TXMessage> talkMsgsCache = new
	// ArrayList<TXMessage>();//语音消息集合？

	public static Map<Long, ArrayList<Long>> mGroupStarsMap = new HashMap<Long, ArrayList<Long>>();// 存放聊天室上周之星的id

	// protected PopupWindow noticePopWindow;// 聊天室的公告通知
	protected RelativeLayout toastNotice;// 聊天室公告栏-popWindow改成view fc，2014/8/7
	protected Intent mainIntent;
	protected Display display;
	protected AudioManager audioManager;
	protected InputMethodManager imm;
	protected LayoutInflater mInflate;
	protected MusicUtils musicUtils;
	protected boolean mIsWireOpen;// 是否打开后台线控功能
	protected ContactAPI api;
	protected ContentResolver cr;
	protected LocationStation ls;// 定位类
	protected ArrayList<Toast> toastList = new ArrayList<Toast>();
	protected static final String MIME_TYPE_IMAGE_JPEG = "image/*";
	protected long img_msg_id;// 照相返回消息ID
	protected String sendimgPath;// 发送图片的路径
	protected boolean isLocationing;// 是否正在定位中 如果是 再定位提示定位中
	public TXMessage gepTxmsgTemp;
	protected static SmileyParser smileyParser;
	public MyPopupWindow mPopupWindow;// 聊天室中表情的popupWindow？
	protected RelativeLayout mPop;// 也是表情那个layout？
	protected ClientManager mAudioRecPlayer;//
	private boolean isCancelSendAudioMsg = false;// 是否取消发送音频消息
	private TXMessage mLongAudioMsg;// 到达7分钟后自动停止等待发送的长录音消息
	protected boolean isOnPressedListView = false;// 手指是否在触摸listView

	protected boolean isPhone;// 是否来电
	protected static int nickNameMaxNum = 4;

	protected boolean mIsHomeDown = false;// 是否点击home键
	protected boolean record_interrupt;// 判定可否被继续录音？如果为true,再录音则显示“录音过于频繁”的吐司
	protected boolean mIsRecording; // 是否正在录音，通过这个变量判断录音状态，控制是否应该停止录音的变量等操作2014.01.21
	protected boolean mHasRecordErr; // 是否设备异常
	protected final int RECORD_END_TIME = 120;
	protected final int LONG_RECORD_EDN_TIME = 420;
	protected int playTime;
	protected Toast unInitRecordToast;
	protected Toast busyRecordToast;
	protected boolean mIsStartAudio;
	// 播放音频状态
	protected static final int RECORD_PAUSE = 0;
	protected static final int RECORD_PLAY = 1;
	protected int wireControl;// 线控状态
	protected static int WIRECONTROL_PLAY = 1;// 播放状态
	protected static int WIRECONTROL_NOMAL = 0;// 未播放状态

	/******* 各种广播变量 *****/
	protected CallComeReceiver callComeReceiver;
	protected HeadSetReceiver headSetReceiver;
	protected MediaButtonReceiver mediaButtonReceiver;

	/***** 上下弹出功能窗口变量 ******/
	protected PopupWindow upMorePopWindow;// 右上角的【更多按钮】弹出的popupWindow
	protected PopupWindow downMorePopWindow;// 聊天室低栏的更多【加号按钮】弹出的popupWindow
	protected TextView listenerText;
	protected TextView autoPlayAdiouText;
	protected TextView text4;
	protected ImageView autoplayIcon;
	protected ImageView handsetIcon;
	protected TextView checkGroupInfoText;
	protected RelativeLayout uplist1;
	protected RelativeLayout uplist2;
	protected RelativeLayout uplist3;
	protected RelativeLayout uplist4;
	protected RelativeLayout uplistclear;
	protected TextView downlist1;
	protected TextView rl_down_video_capture;
	protected TextView rl_down_music;
	protected TextView rl_down_file;// 选择大文件发送
	protected TextView downlist2;
	protected TextView downlist3;
	protected TextView downlist4;
	protected AlertDialog morePicDialog;
	protected RelativeLayout expressionLayout;
	protected RecorderPopupWindow mRecordPopupWindow;
	protected PraiseMsgCallBack praiseResultCallback;// 赞或者取消赞的回调
	public ArrayList<SoftReference<AnimationDrawable>> cache_amd = new ArrayList<SoftReference<AnimationDrawable>>();
//	private HashMap<String, WeakReference<GifView>> viewCache = new HashMap<String, WeakReference<GifView>>();
	/***** 额外表情添加底部滑动 ******/

	protected HorizontalScrollView msgroom_expression_hsv;
	protected LinearLayout msgroom_expression_hsv_layout;
	protected GifTranscoldMgr gifTranscoldMgr = GifTranscoldMgr.getInstance();

	// TODO 存有表情包对象的list
	protected ArrayList<ExpressionPackage> expression_package_list = new ArrayList<ExpressionPackage>();

	// TODO 初始化添加表情
	public void initExtraExpressionBottomView() {
		// TODO 如何存储每个人都有什么额外的表情
		msgroom_expression_hsv = (HorizontalScrollView) findViewById(R.id.msgroom_expression_hsv);
		msgroom_expression_hsv_layout = (LinearLayout) msgroom_expression_hsv
				.findViewById(R.id.msgroom_expression_hsv_layout);
		// TODO 这里应该有一个装载表情包的list,为这个list初始化数据

	}

	public Handler gifHandler = new Handler() {
		public void handleMessage(Message msg) {

		};
	};

	/*****  ******/
	protected long id_lastTxmsg = 0;

	public Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				if (result == null || result[0] == null)
					return;
				for (ViewHolder holder : mViewLines) {
					if (holder.txmsg != null
							&& holder.txmsg.partner_id == (Long) result[1]
							&& holder.headView != null) {
						holder.headView.setImageBitmap((Bitmap) result[0]);
					}
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	LinkedList<ViewHolder> mViewLines = new LinkedList<ViewHolder>();
	public Handler avatarHandler_my = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;
				if (result == null || result[0] == null)
					return;
				for (ViewHolder holder : mViewLines) {
					if (holder.txmsg != null
							&& holder.txmsg.partner_id == (Long) result[1]
							&& holder.myHeadView != null)
						holder.myHeadView.setImageBitmap((Bitmap) result[0]);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	// 读取好友头像
	public void readHeadImg(ImageView headView1, long tx_partner_id, int sex,
			String tx_avatar, boolean isMy) {
		// Bitmap bm = null;
		// headView1.setTag(tx_partner_id);
		if (headView1 != null) {
			if (!Utils.isIdValid(tx_partner_id))
				return;
			if (TX.TUIXIN_MAN == tx_partner_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				return;
			}
			Bitmap bm = mSess.avatarDownload
					.getPartnerCachedBitmap(tx_partner_id);
			if (bm != null) {
				// if ((Long) headView1.getTag() == tx_partner_id)
				headView1.setImageBitmap(bm);
				return;
			}
			// bm = mSess.cachePartnerDefault(tx_partner_id, sex);
			bm = mSess.getDefaultPartnerAvatar(sex);// TODO 需要这个方法吗？ 2014.04.16
													// shc
			headView1.setImageBitmap(bm);
			if (Utils.isNull(tx_avatar)) {
				if (Utils.debug) {
					Log.e(TAG_HEAD, tx_partner_id + "头像地址为空！！！,返回，没有去加载！！");
				}
				return;
			}

			bm = mSess.cachePartnerDefault(tx_partner_id, sex);

			if (isMy) {
				mSess.avatarDownload.downAvatar(tx_avatar, tx_partner_id, null,
						avatarHandler_my);
			} else {
				mSess.avatarDownload.downAvatar(tx_avatar, tx_partner_id, null,
						avatarHandler);
			}

		}
	}

	// 加载名片头像
	public void readCardHeadImg(ImageView headView1, long card_id, int sex,
			String cardUrl) {
		if (Utils.debug) {
			Log.i(TAG, "readCardHeadImg,card_id = " + card_id);
		}
		Bitmap bm = null;
		if (headView1 != null) {
			if (!Utils.isIdValid(card_id))
				return;
			if (TX.TUIXIN_MAN == card_id) {
				headView1.setImageResource(R.drawable.tuixin_manage);
				headView1.setTag(card_id);
				return;
			}
			bm = mSess.avatarDownload.getPartnerCachedBitmap(card_id);
			if (bm != null) {
				headView1.setImageBitmap(bm);
				return;
			}
			bm = mSess.cachePartnerDefault(card_id, sex);
			headView1.setImageBitmap(bm);

			if (Utils.isNull(cardUrl))
				return;

			headView1.setTag(card_id);
			mSess.avatarDownload.downAvatar(cardUrl, card_id, headView1,
					avatarHandler_card);

			// loadHeadImg(cardUrl, card_id, new AsyncCallback<Bitmap>() {
			// @Override
			// public void onSuccess(Bitmap result, long id) {
			//
			// ImageView iv = (ImageView) msgRoomList.findViewWithTag(id);
			// if (iv != null && result != null) {
			// iv.setImageBitmap(result);
			// } else {
			// }
			// }
			//
			// @Override
			// public void onFailure(Throwable t, long id) {
			//
			// }
			// });
		}
	}

	private Handler avatarHandler_card = new WrappedHandler(thisContext) {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result = (Object[]) msg.obj;

				ImageView iv = (ImageView) msgRoomList
						.findViewWithTag((Long) result[1]);
				if (iv != null && result[0] != null) {
					iv.setImageBitmap((Bitmap) result[0]);
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	private Handler audioHandler = new WrappedHandler(thisContext) {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case ClientManager.ADU:
				TXMessage result = (TXMessage) msg.obj;
				if (result == null)
					return;
				playAudioRecord(result);
				break;
			}
			super.handleMessage(msg);
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TxData.finishOne(SingleMsgRoom.class.getName());
		// TxData.finishOne(GroupMsgRoom.class.getName());
		// prepairAsyncload();
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		TxData.addActivity(this);
		mCurrentMsgRoom = this;
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);// 进入不弹软键盘

		mIsWireOpen = getPrefsSet().getBoolean(
				getString(R.string.pref_key_save_backgroud_wire), false);
		api = ContactAPI.getAPI();
		api.setCr(getContentResolver());
		cr = getContentResolver();

		/**** 音乐音效 *****/
		musicUtils = new MusicUtils();
		musicUtils.CreateSoundpool(this);
		musicUtils.LoadSound(this, 1, R.raw.begin_record, 0);
		musicUtils.LoadSound(this, 2, R.raw.finish_record, 1);
		musicUtils.LoadSound(this, 3, R.raw.play_finish, 2);
		musicUtils.LoadSound(this, 4, R.raw.send_sucess, 3);

		mAudioRecPlayer = ClientManager.getRecordManager();

		// 启动录音管理器
		// Utils.executorService.submit(recordManager);
		// Utils.executorService.submit(playManager);
		Utils.readAutoPlayAdiouData(this);
		setContentView(R.layout.activity_msgroom);

		View v_back = findViewById(R.id.tv_title_back);
		v_back.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// title上的返回按钮
				Utils.hideSoftInput(thisContext);
				// dealLastTxMsgs();
				thisContext.finish();

			}
		});

		mInflate = LayoutInflater.from(this);
		display = getWindowManager().getDefaultDisplay();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

		initMsgRoomData();// 初始化聊天室非公共的数据
	}

	protected ImageView iv;
	protected ImageView iv1;
	protected ImageView iv2;
	protected ImageView iv3;
	protected ImageView iv4;
	protected ImageView iv5;
	protected ImageView iv6;
	protected ImageView iv7;
	protected ImageView iv8;

	protected int[] locations;
	protected int[] location2s;
	protected ImageView ivbg;
	protected ImageView ivbg2;
	protected ImageView iv_aminend;
	protected ReceiveGreatAnim anim;
	protected TextView tv_cur_room_praised_count;// 被赞View

	protected void initMsgRoomData() {
		mainIntent = this.getIntent();
		initControlView();
		initExtraExpressionBottomView();
		initExpressData();
		initAnimView();
		toolLogic();
		// ListenLayoutHeightChange();
		registRecorderTouchListener();
		SendButtonMsg();
		changeType(false, false);
		mRecordPopupWindow = new RecorderPopupWindow();
	}

	protected void initAnimView() {
		rl_praised_anim_view.setVisibility(View.INVISIBLE);
		iv = (ImageView) findViewById(R.id.iv_amin);
		ivbg = (ImageView) findViewById(R.id.iv_aminbg);
		ivbg2 = (ImageView) findViewById(R.id.iv_aminbg2);
		// //先隐藏中间的大心形图标
		// iv.setVisibility(View.GONE);
		// ivbg.setVisibility(View.GONE);
		// ivbg2.setVisibility(View.GONE);
		iv1 = (ImageView) findViewById(R.id.iv_amin1);
		iv2 = (ImageView) findViewById(R.id.iv_amin2);
		iv3 = (ImageView) findViewById(R.id.iv_amin3);
		iv4 = (ImageView) findViewById(R.id.iv_amin4);
		iv5 = (ImageView) findViewById(R.id.iv_amin5);
		iv6 = (ImageView) findViewById(R.id.iv_amin6);
		iv7 = (ImageView) findViewById(R.id.iv_amin7);
		iv8 = (ImageView) findViewById(R.id.iv_amin8);
		iv_aminend = (ImageView) findViewById(R.id.iv_end);

	}

	@Override
	protected void onDestroy() {
		if (mCurrentMsgRoom == thisContext) {
			// 说明是自己销毁的自己，可以置空
			mCurrentMsgRoom = null;
		}

		auths.clear();// 临时这样处理一下
		if (mRecordPopupWindow.isWindowShowing()) {
			stopLongAudioRecord();
			Utils.isRrecord = true;
			mRecordPopupWindow.exitLongRecorderScreen();
		}

		if (mediaButtonReceiver != null) {
			unregisterReceiver(mediaButtonReceiver);
			mediaButtonReceiver = null;
		}
		if (headSetReceiver != null) {
			this.unregisterReceiver(headSetReceiver);
			headSetReceiver = null;
		}
		closeInitAndBusyRecordInfo();

		if (mAudioRecPlayer != null) {
			mAudioRecPlayer.stopPlay();
			mAudioRecPlayer.stopRecord();
		}
		exitAudioRecPlayer();
		// TxData.popActivityRemove(this);
		if (audioManager != null)
			audioManager = null;

		mRecordPopupWindow.cancelRecordTims();

		if (praiseResultCallback != null) {
			// 删掉被赞结果通知的回调
			mSess.getPraiseNoticeDao().unregistePraiseResultCallback(
					praiseResultCallback);
		}
		GifTranscoldMgr.getInstance().recycleHelper();

		super.onDestroy();
	}

	@Override
	protected void onPause() {
		removeAllToasts();
		super.onPause();
	}

	@Override
	protected void onResume() {
		if (audioManager == null)
			audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		super.onResume();
	}

	@Override
	protected void onStart() {
		// if(longrecordPopup==null &&! longrecordPopup.isShowing()){
		// Utils.isRrecord = true;
		// }

		if (!mRecordPopupWindow.isWindowShowing()) {
			Utils.isRrecord = true;
		}

		if (mediaButtonReceiver != null) {
			unregisterReceiver(mediaButtonReceiver);
			mediaButtonReceiver = null;
		}

		// if (mediaButtonReceiver == null) {
		// mediaButtonReceiver = new MediaButtonReceiver();
		// IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);//
		// 线控按钮
		// filter.setPriority(2147483647);
		// registerReceiver(mediaButtonReceiver, filter);
		// }
		if (headSetReceiver == null) {
			headSetReceiver = new HeadSetReceiver();
			registerReceiver(headSetReceiver, new IntentFilter(
					Intent.ACTION_HEADSET_PLUG));// 插拔耳机
		}
		if (callComeReceiver == null) {
			callComeReceiver = new CallComeReceiver();// 来电
			registerReceiver(callComeReceiver, new IntentFilter(
					"android.intent.action.PHONE_STATE"));
		}

		if (audioManager == null) {
			audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
			if (audioManager != null) {
				if (Utils.isHandset) {
					audioManager.setMode(AudioManager.MODE_IN_CALL);
				} else {
					audioManager.setMode(AudioManager.MODE_NORMAL);
				}
			}
		}
		super.onStart();
	}

	@Override
	protected void onNewIntent(Intent newintent) {
		setIntent(newintent);

		processExtraData(false);

	}

	/** 处理非界面的数据，让onCreat和onNewIntent方法共用,isOnCreat：true则不调用父类onCreate方法中的操作 */
	protected abstract void processExtraData(boolean isInOnCreat);

	@Override
	protected void onStop() {
		// if (mediaButtonReceiver == null && !isPhone&&mIsWireOpen &&
		// (longrecordPopup==null || !longrecordPopup.isShowing())) {
		// dealLastTxMsgs();
		if (mediaButtonReceiver == null && !isPhone && mIsWireOpen
				&& !mRecordPopupWindow.isWindowShowing()) {
			mediaButtonReceiver = new MediaButtonReceiver();
			IntentFilter filter = new IntentFilter(Intent.ACTION_MEDIA_BUTTON);// 线控按钮
			filter.setPriority(2147483647);
			registerReceiver(mediaButtonReceiver, filter);
		}
		if (callComeReceiver != null) {
			unregisterReceiver(callComeReceiver);
			callComeReceiver = null;
		}

		// // 停止已接受图片大文件缩略图线程的加载
		// if (mThumbnail != null) {
		// mThumbnail.setCancelThumbnails(true);
		// mThumbnail = null;
		// }

		super.onStop();
	}

	// 载入全局view变量
	public void initControlView() {
		// mainRel = (RelativeLayout) findViewById(R.id.msgRoom_mainLayout11);
		msgRoomList = (MyGroupListView) findViewById(R.id.msgRoom_list);
		msgRoomList.setTranscriptMode(ListView.TRANSCRIPT_MODE_NORMAL);
		msgRoomList.setOnTouchListener(this);
		msgRoomList.setOnScrollListener(new OnScrollListener() {

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				MyGroupListView.firstItemIndex = firstVisibleItem;
				mbBottom = (firstVisibleItem + visibleItemCount) >= totalItemCount;
			}

			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {

			}

		});

		msgRoomList
				.setOnSizeChangeListener(new MyGroupListView.OnSizeChangeListener() {

					@Override
					public void sizeChange(int w, int h, int oldw, int oldh) {
						// 主要是监听软键盘的弹出与隐藏
						if (downMorePopWindow != null
								&& downMorePopWindow.isShowing()) {
							downMorePopWindow.dismiss();
						}
					}
				});
		toastNotice = (RelativeLayout) findViewById(R.id.toastNotice);
		personName = (TextView) findViewById(R.id.MsgRoom_personName);
		rl_praised_anim_view = (ViewGroup) findViewById(R.id.rl_praised_anim_view);
		tv_cur_room_praised_count = (TextView) rl_praised_anim_view
				.findViewById(R.id.tv_cur_room_praised_count);
		rl_praised_anim_view.setVisibility(View.INVISIBLE);// 被赞动画布局不可见
		sendTypeButton = (ImageButton) findViewById(R.id.publicmsg_send_tab);
		sendTypeButton
				.setImageResource(Utils.isSendText == true ? R.drawable.sll_msgroom_intput_text_selector
						: R.drawable.sll_msgroom_record_audio_selector);

		downMoreButton = (ImageButton) findViewById(R.id.publicmsg_send_more);
		moreButton = (ImageView) findViewById(R.id.MsgRoom_more);
		downlayout = (RelativeLayout) findViewById(R.id.publicmsg_DownTools);
		msgEdit = (EditText) findViewById(R.id.publicmsg_Edit);

		expressionLayout = (RelativeLayout) findViewById(R.id.MsgRoom_Expression);
		btn_recordShortAduio = (Button) findViewById(R.id.publicmsg_RecordButton);
		SendButton = (TextView) findViewById(R.id.publicmsg_SendButton);
		autoplayIcon = (ImageView) findViewById(R.id.MsgRoom_autoplay_icon);
		autoplayIcon
				.setVisibility(Utils.isOpenPlayAdiou == true ? View.INVISIBLE
						: View.VISIBLE);
		handsetIcon = (ImageView) findViewById(R.id.MsgRoom_handset_icon);
		handsetIcon.setVisibility(Utils.isHandset == true ? View.VISIBLE
				: View.INVISIBLE);
		sendTypeButton
				.setImageResource(Utils.isSendText == true ? R.drawable.sll_msgroom_intput_text_selector
						: R.drawable.sll_msgroom_record_audio_selector);

		if (Utils.isSendText) {
			sendTypeButton.setOnLongClickListener(new OnLongClickListener() {
				@Override
				public boolean onLongClick(View v) {
					mRecordPopupWindow.showLongRecorderScreen(downlayout);
					return false;
				}
			});
			msgEdit.setVisibility(View.VISIBLE);
			btn_recordShortAduio.setVisibility(View.GONE);
		} else {
			msgEdit.setVisibility(View.GONE);
			btn_recordShortAduio.setVisibility(View.VISIBLE);
		}
	}

	// 初始化表情类的数据
	public void initExpressData() {
		DisplayMetrics metric = new DisplayMetrics();

		mPopupWindow = new MyPopupWindow(this, expressionLayout,
				display.getWidth(), display.getHeight(), msgEdit,
				metric.densityDpi, msgroom_expression_hsv,
				msgroom_expression_hsv_layout, moreButton, txGroup);
		mPop = mPopupWindow.getPopupWindow();
		smileyParser = mPopupWindow.getSParser();
	}

	public void sendGifMessage(final int num) {
		if (mPopupWindow.getmCurEmoji().emoji_id == -1) {
			// 如果包id为-1,这里是为了预防上传包失败，即本地有此包，但表情服务器上没有此包，得重新登录表情服务器获得包id;
			mSess.mGifTransfer.taskGetGifPackageId(
					mPopupWindow.getmCurEmoji().emoji_path, 6, 1, null,
					new GifDownUploadListner() {

						@Override
						public void onFinish(GifTaskInfo taskInfo) {
							// 就不会走到这
						}

						@Override
						public void onError(GifTaskInfo taskInfo, int iCode,
								Object iPara) {
							super.onError(taskInfo, iCode, iPara);
							GifTransfer.GTaskForId task = (GTaskForId) taskInfo;
							if (task.mId != -1 && task.mId != 0) {
								mPopupWindow.getmCurEmoji().emoji_id = task.mId;
								sendGifMessage2(num);
							} else {
								showToast("获取表情id失败,无法发送该表情");
							}
						}
					}, false);
		} else {
			sendGifMessage2(num);
		}

	}

	public void sendGifMessage2(int num) {
		if (txGroup != null) {
			if (Utils.debug) {
				Log.e(TAG,
						"2015/1/26 点击的GIF的MD5值 :"
								+ mPopupWindow.getmCurEmoji().emoji_md5);
			}
			final TXMessage txmsgTemp;
			txmsgTemp = TXMessage.createGroupGifSms(
					this,
					txGroup.group_id,
					txGroup.group_title,
					"这里是自己的头像地址",
					mPopupWindow.getmCurEmoji().emoji_id,
					// //这里需要去获取单个GIF的MD5值
					GifTranscoldMgr.getInstance().getSingleGifMD5(
							mPopupWindow.getmCurEmoji().emoji_path, num)
					// "06cbda2cab80eac6bc27e0ebc154b08b"
					, true, TXMessage.NOT_SENT, mSess.getServerTime(), num,
					mPopupWindow.getmCurEmoji().emoji_md5);
			// , FileManager
			// .getGifDownloadName(
			// mPopupWindow.getmCurEmoji().emoji_id,
			// mPopupWindow.getmCurEmoji().emoji_md5)
			mSess.getSocketHelper().sendGroupMsg(txmsgTemp);
		} else {
			sendGifMsg(
					num,
					mPopupWindow.getmCurEmoji().emoji_id,
					GifTranscoldMgr.getInstance().getSingleGifMD5(
							mPopupWindow.getmCurEmoji().emoji_path, num),
					mPopupWindow.getmCurEmoji().emoji_md5);

		}
	}

	// 创建和显示上弹出功能窗口
	public void creatUpMorePop() {
		if (downMorePopWindow != null && downMorePopWindow.isShowing())
			downMorePopWindow.dismiss();
		if (upMorePopWindow == null) {
			View popupWindow_view = mInflate.inflate(R.layout.msgroom_more_pop,
					null);

			upMorePopWindow = new PopupWindow(popupWindow_view,
					(int) (display.getWidth() * 0.55),
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例

			upMorePopWindow.setAnimationStyle(R.style.chat_up_anim);
			upMorePopWindow.setFocusable(true);
			upMorePopWindow.setBackgroundDrawable(new BitmapDrawable());

			listenerText = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item3_text);
			listenerText
					.setText(Utils.isHandset == true ? R.string.msgroom_more_pop_nomalplay
							: R.string.msgroom_more_pop_close_handsetplay);

			autoPlayAdiouText = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item2_text);
			autoPlayAdiouText
					.setText(Utils.isOpenPlayAdiou == false ? R.string.msgroom_more_pop_autoplay
							: R.string.msgroom_more_pop_close_autoplay);

			checkGroupInfoText = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item1_text);
			upToolList1Text();

			uplist1 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item1);
			uplist2 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item2);
			uplist3 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item3);
			uplist4 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item4);

			uplistclear = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item_clear);

			text4 = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_more_item4_text);
			if (BaseMsgRoom.this instanceof SingleMsgRoom) {
				uplist4.setVisibility(View.GONE);
				uplist1.setVisibility(View.GONE);

				popupWindow_view.findViewById(R.id.msgRoom_more_item4_line)
						.setVisibility(View.GONE);
				popupWindow_view.findViewById(R.id.msgRoom_more_item1_line)
						.setVisibility(View.GONE);

			} else {
				setText4Data();
			}
			uplistclear.setOnTouchListener(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						uplistclear.setBackgroundColor(Color
								.parseColor("#27a5de"));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						uplistclear.setBackgroundDrawable(null);
						clearAllMsg();
						upMorePopWindow.dismiss();
					}
					return true;
				}
			});
			uplist1.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						uplist1.setBackgroundColor(Color.parseColor("#27a5de"));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						uplist1.setBackgroundDrawable(null);
						upToolList1Logic();
						upMorePopWindow.dismiss();
					}
					return true;
				}
			});
			uplist2.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						uplist2.setBackgroundColor(Color.parseColor("#27a5de"));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						uplist2.setBackgroundDrawable(null);
						Utils.isOpenPlayAdiou = !Utils.isOpenPlayAdiou == true;
						autoPlayAdiouText
								.setText(Utils.isOpenPlayAdiou == false ? R.string.msgroom_more_pop_autoplay
										: R.string.msgroom_more_pop_close_autoplay);
						autoplayIcon
								.setVisibility(Utils.isOpenPlayAdiou == true ? View.INVISIBLE
										: View.VISIBLE);
						if (Utils.isOpenPlayAdiou) {
							Utils.creatMsgRoomUpToolsInfo(BaseMsgRoom.this,
									R.string.msgroom_toast_open_auto_play,
									R.drawable.msgroom_more_pop_autoplay);
						} else {
							Utils.creatMsgRoomUpToolsInfo(BaseMsgRoom.this,
									R.string.msgroom_toast_close_auto_play,
									R.drawable.msgroom_more_pop_autoplay);

						}
						Utils.saveAutoPlayAdiouData(BaseMsgRoom.this);
						upMorePopWindow.dismiss();
					}
					return true;
				}
			});
			uplist3.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						uplist3.setBackgroundColor(Color.parseColor("#27a5de"));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						uplist3.setBackgroundDrawable(null);
						Utils.isHandset = !Utils.isHandset;
						if (audioManager != null) {
							audioManager
									.setMode(Utils.isHandset ? AudioManager.MODE_IN_CALL
											: AudioManager.MODE_NORMAL);
						}
						listenerText
								.setText(Utils.isHandset == true ? R.string.msgroom_more_pop_nomalplay
										: R.string.msgroom_more_pop_close_handsetplay);
						handsetIcon
								.setVisibility(Utils.isHandset == true ? View.VISIBLE
										: View.INVISIBLE);
						if (Utils.isHandset) {
							Utils.creatMsgRoomUpToolsInfo(BaseMsgRoom.this,
									R.string.msgroom_toast_handset,
									R.drawable.msgroom_more_pop_handset);
						} else {
							Utils.creatMsgRoomUpToolsInfo(BaseMsgRoom.this,
									R.string.msgroom_toast_loudspeaker,
									R.drawable.msgroom_more_pop_handset);
						}
						Utils.saveAutoPlayAdiouData(BaseMsgRoom.this);
						upMorePopWindow.dismiss();
					}
					return true;
				}
			});

			// 查看群在线人员
			uplist4.setOnTouchListener(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {
						uplist4.setBackgroundColor(Color.parseColor("#27a5de"));
					} else if (event.getAction() == MotionEvent.ACTION_UP) {
						uplist4.setBackgroundDrawable(null);
						// Utils.isHandset = Utils.isHandset == true ? false :
						// true;
						upMorePopWindow.dismiss();

						Intent i = new Intent(thisContext, GroupMember.class);
						// i.putExtra(GroupMember.VIEW_STATE,
						// GroupMember.FROM_MSGROOM);
						if (TxGroup.isPublicGroup(txGroup)) {
							// 公开聊天室
							i.putExtra(GroupMember.VIEW_STATE,
									GroupMember.DISPLAY_ONLINE);
						} else {
							// 我的群
							i.putExtra(GroupMember.VIEW_STATE,
									GroupMember.DISPLAY_MEMBER);
						}
						i.putExtra(Utils.MSGROOM_GROUP_ID, groupid);
						startActivity(i);
						turn = true;
						if (TxGroup.isPublicGroup(txGroup)) {
							Utils.isNotificationShow = false;
						} else {
							Utils.isNotificationShow = true;
						}

						// goMember();
					}

					return true;
				}
			});
			upMorePopWindow.setTouchInterceptor(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (Utils.debug)
						// showToast("这个upMorePopWindow的setTouchInterceptor事件是干嘛的？");//先不弹toast提示
						// 2014.03.07 shc
						if (event.getAction() == MotionEvent.ACTION_DOWN) {
							Rect t = new Rect();
							sendTypeButton.getGlobalVisibleRect(t);
							if (t.contains((int) event.getRawX(),
									(int) event.getRawY())) {
								changeType(true, false);
								Utils.saveAutoPlayAdiouData(BaseMsgRoom.this);
							}
							downMoreButton.getGlobalVisibleRect(t);
							if (t.contains((int) event.getRawX(),
									(int) event.getRawY())) {

								creatDownMorePop();
							}
						}
					return false;
				}
			});
			upMorePopWindow.update();
			upMorePopWindow.showAsDropDown(moreButton,
					-(int) (display.getWidth() * 0.4), 0);
		} else {
			if (upMorePopWindow.isShowing()) {
				upMorePopWindow.dismiss();
			} else {
				upMorePopWindow.update();
				upMorePopWindow.showAsDropDown(moreButton,
						-(int) (display.getWidth() * 0.4), 0);

			}
		}
	}

	protected void setText4Data() {
	}

	// protected void goMember() {}//查看群成员？？？
	protected abstract void clearAllMsg();

	// 隐藏下功能窗口
	public void hideDownTools() {
		if (downMorePopWindow != null && downMorePopWindow.isShowing())
			downMorePopWindow.dismiss();
		if (mPop != null && mPop.getVisibility() == View.VISIBLE)
			mPop.setVisibility(View.GONE);
		msgroom_expression_hsv_layout.setVisibility(View.GONE);
		Utils.hideSoftInput(thisContext);
		if (downlayout.getVisibility() == View.VISIBLE)
			downlayout.setVisibility(View.GONE);
	}

	// 显示下功能窗口
	public void showDwonTools() {

		if (downlayout.getVisibility() == View.GONE)
			downlayout.setVisibility(View.VISIBLE);
		Utils.popSoftInput(this.getApplicationContext());

		msgEdit.requestFocus();
	}

	// 显示下功能窗口,不弹出软键盘
	public void showDwonToolsNo() {

		if (downlayout.getVisibility() == View.GONE)
			downlayout.setVisibility(View.VISIBLE);
		// Utils.popSoftInput(this.getApplicationContext());

		msgEdit.requestFocus();
	}

	// 创建和显示下弹出功能窗口
	public void creatDownMorePop() {
		int[] location = new int[2];

		downlayout.getLocationInWindow(location);
		int downMorePopPosY = display.getHeight() - location[1];
		if (downMorePopPosY < 0)
			downMorePopPosY = 0;

		if (downMorePopWindow == null) {
			View popupWindow_view = mInflate.inflate(
					R.layout.msgroom_down_more_pop, null);
			downMorePopWindow = new PopupWindow(popupWindow_view,
					(int) (display.getWidth() * 0.9),
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例
			downMorePopWindow.setAnimationStyle(R.style.chat_down_anim);
			// downMorePopWindow.setFocusable(false);
			downMorePopWindow.setBackgroundDrawable(new BitmapDrawable());
			downMorePopWindow
					.setOnDismissListener(new PopupWindow.OnDismissListener() {

						@Override
						public void onDismiss() {
							// 执行按钮的逆旋转旋转动画
							downMoreButton.startAnimation(Utils
									.getAnimationRotate(45, 0));// 旋转动画
						}
					});
			downlist1 = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_down_more_item1);
			rl_down_video_capture = (TextView) popupWindow_view
					.findViewById(R.id.rl_down_pop_video_item);
			rl_down_music = (TextView) popupWindow_view
					.findViewById(R.id.rl_down_popup_music_item);
			rl_down_file = (TextView) popupWindow_view
					.findViewById(R.id.rl_down_popup_file_item);
			downlist2 = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_down_more_item2);
			downlist3 = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_down_more_item3);
			downlist4 = (TextView) popupWindow_view
					.findViewById(R.id.msgRoom_down_more_item4);

			// if (hideCard) {
			// downIcon4.setVisibility(View.GONE);
			// downlist4.setVisibility(View.GONE);
			// }

			downlist1.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!Utils.isSendText)
						changeType(true, true);
					hideInputMethod(msgEdit);
					// imm.hideSoftInputFromWindow(msgEdit.getWindowToken(),
					// 0);
					msgEdit.requestFocus();
					mPopupWindow.onClick();// 显示表情popupWindow？
					// TODO 底部添加表情栏的显示与隐藏
					downMorePopWindow.dismiss();

				}
			});

			downlist2.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					downMorePopWindow.dismiss();
					// 只有在聊天室中才有level的限制
					if (txGroup != null && (TxGroup.isPublicGroup(txGroup))) {
						if (!mSess.getTxMgr().getTxMe().isCanSendImg()) {
							// 不可以发送语音
							showToast("需要达到3级才可发照片哦！多多发言可以加快升级哦！");
							return;
						}
					}
					morePicDialog = new AlertDialog.Builder(BaseMsgRoom.this)
							.setTitle(R.string.msg_more_select_str)
							.setItems(R.array.msgroom_pic,
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											dialog.dismiss();
											if (!Utils.checkSDCard()) {// 无SD卡
												toastList.add(Utils
														.creatNoSdCardInfo(BaseMsgRoom.this));
												return;
											}

											if (which == 0) {
												if (Utils
														.checkNetworkAvailable1(mSess
																.getContext()))
													startPicView();
												else
													creatMsgInfo(
															true,
															R.string.msg_nonetstr);
											} else if (which == 1) {
												if (Utils
														.checkNetworkAvailable1(mSess
																.getContext()))
													startPhotoCapture();
												else
													creatMsgInfo(
															true,
															R.string.msg_nonetstr);
											}
										}
									}).show();

				}
			});

			rl_down_video_capture
					.setOnClickListener(new View.OnClickListener() {

						@Override
						public void onClick(View v) {
							if (!Utils.isSendText)
								changeType(true, true);
							hideInputMethod(msgEdit);
							downMorePopWindow.dismiss();

							rl_down_video_capture.setFocusable(true);
							rl_down_video_capture.setFocusableInTouchMode(true);
							rl_down_video_capture.requestFocus();

							String[] items = new String[] { "拍摄视频", "从手机相册选择" };
							Utils.creatDialogMenu(thisContext, items, "操作",
									new DialogInterface.OnClickListener() {

										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											if (which == 0) {
												// 拍摄视频
												if (Build.VERSION.SDK_INT < 10) {
													// SDK版本低于10，不能录制视频
													showToast("您当前系统版本过低，无法使用本功能。");
												} else {
													Intent videoIntent = new Intent(
															thisContext,
															VideoCaptureActivity.class);
													startActivityForResult(
															videoIntent,
															REQ_TAKE_BIG_FILES);
												}

											} else if (which == 1) {
												// 从手机相册选择
												// showToast("别闹，这个功能还不支持呢");

												Intent intent = new Intent();
												intent.setType("video/*"); // 选择android系统摄像机录制的视频
												intent.setAction(Intent.ACTION_GET_CONTENT);
												/* 取得视频后返回本画面 */
												startActivityForResult(intent,
														REQ_TAKE_BIG_FILES);

											}
										}
									});

						}
					});

			downlist3.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					downMorePopWindow.dismiss();
					if (Utils.checkNetworkAvailable1(thisContext))
						startSendMeGeo();
					else
						creatMsgInfo(true, R.string.msg_nonetstr);
				}
			});

			downlist4.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					downMorePopWindow.dismiss();
					startSendPickContacts();
				}
			});

			rl_down_music.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 点击了音乐按钮
					downMorePopWindow.dismiss();
					Intent intent = new Intent();
					intent.setType("audio/*"); // 选择android系统的音频文件
					intent.setAction(Intent.ACTION_GET_CONTENT);
					startActivityForResult(intent, REQ_TAKE_MUSIC_FILES);

				}
			});
			rl_down_file.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					// 选择文件发送
					downMorePopWindow.dismiss();
					Intent intent = new Intent(thisContext,
							SelectFileActivity.class);
					startActivityForResult(intent, REQ_TAKE_BIG_FILES);

				}
			});

			downMorePopWindow.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					// if (event.getAction() == MotionEvent.ACTION_DOWN) {
					// Rect t = new Rect();
					// sendTypeButton.getGlobalVisibleRect(t);
					// if (t.contains((int) event.getRawX(),(int)
					// event.getRawY())) {
					// changeType(true);
					// Utils.saveAutoPlayAdiouData(BaseMsgRoom.this);
					// if (downMorePopWindow != null
					// && downMorePopWindow.isShowing()) {
					// downMorePopWindow.update();
					// downMorePopWindow.showAtLocation(
					// downMoreButton, Gravity.BOTTOM
					// | Gravity.CENTER_HORIZONTAL, 0,
					// display.getHeight() - t.top);
					// }
					// return true;
					// }
					// moreButton.getGlobalVisibleRect(t);
					// if (t.contains((int) event.getRawX(),(int)
					// event.getRawY()))
					// creatUpMorePop();
					// }

					if (event.getAction() == MotionEvent.ACTION_UP) {
						hideInputMethod(v);
					}
					return false;
				}
			});
			// downMorePopWindow.update();
			// downMorePopWindow.showAtLocation(downMoreButton, Gravity.BOTTOM
			// | Gravity.CENTER_HORIZONTAL, 0, downMorePopPosY);
		}
		// else {
		// 官方或公共聊天室禁止发送文件、视频、音频
		// ImageView iv_icon = null;
		Drawable drawable = null;
		// TextView tv_file = null;
		if (txGroup != null && (TxGroup.isPublicGroup(txGroup))) {
			// 文件
			// iv_icon = (ImageView) rl_down_file
			// .findViewById(R.id.iv_down_popup_file_icon);
			// tv_file = (TextView) rl_down_file
			// .findViewById(R.id.tv_down_popup_file_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_file);
			drawable.setAlpha(100);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_file.setCompoundDrawables(drawable, null, null, null);
			rl_down_file.setEnabled(false);
			// iv_icon.getBackground().setAlpha(100);
			rl_down_file.setTextColor(Color.parseColor("#64ffffff"));

			// 视频
			// iv_icon = (ImageView) rl_down_video_capture
			// .findViewById(R.id.iv_down_popup_video_icon);
			// tv_file = (TextView) rl_down_video_capture
			// .findViewById(R.id.tv_down_popup_video_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_video);
			drawable.setAlpha(100);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_video_capture.setCompoundDrawables(drawable, null, null,
					null);
			rl_down_video_capture.setEnabled(false);
			// iv_icon.getBackground().setAlpha(100);
			rl_down_video_capture.setTextColor(Color.parseColor("#64ffffff"));

			// 音频
			// iv_icon = (ImageView) rl_down_music
			// .findViewById(R.id.iv_down_popup_music_icon);
			// tv_file = (TextView) rl_down_music
			// .findViewById(R.id.tv_down_popup_music_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_music);
			drawable.setAlpha(100);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_music.setCompoundDrawables(drawable, null, null, null);
			rl_down_music.setEnabled(false);
			// iv_icon.getBackground().setAlpha(100);
			rl_down_music.setTextColor(Color.parseColor("#64ffffff"));

		} else {
			// 文件
			// iv_icon = (ImageView) rl_down_file
			// .findViewById(R.id.iv_down_popup_file_icon);
			// tv_file = (TextView) rl_down_file
			// .findViewById(R.id.tv_down_popup_file_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_file);
			drawable.setAlpha(255);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_file.setCompoundDrawables(drawable, null, null, null);
			rl_down_file.setEnabled(true);
			// iv_icon.getBackground().setAlpha(255);
			rl_down_file.setTextColor(Color.WHITE);

			// 视频
			// iv_icon = (ImageView) rl_down_video_capture
			// .findViewById(R.id.iv_down_popup_video_icon);
			// tv_file = (TextView) rl_down_video_capture
			// .findViewById(R.id.tv_down_popup_video_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_video);
			drawable.setAlpha(255);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_video_capture.setCompoundDrawables(drawable, null, null,
					null);
			rl_down_video_capture.setEnabled(true);
			// iv_icon.getBackground().setAlpha(255);
			rl_down_video_capture.setTextColor(Color.WHITE);

			// 音频
			// iv_icon = (ImageView) rl_down_music
			// .findViewById(R.id.iv_down_popup_music_icon);
			// tv_file = (TextView) rl_down_music
			// .findViewById(R.id.tv_down_popup_music_text);
			drawable = getResources().getDrawable(
					R.drawable.msgroom_downpop_music);
			drawable.setAlpha(255);
			drawable.setBounds(0, 0, drawable.getMinimumWidth(),
					drawable.getMinimumHeight());
			rl_down_music.setCompoundDrawables(drawable, null, null, null);
			rl_down_music.setEnabled(true);
			// iv_icon.getBackground().setAlpha(255);
			rl_down_music.setTextColor(Color.WHITE);

		}

		if (downMorePopWindow.isShowing()) {
			downMorePopWindow.dismiss();
		} else {
			downMorePopWindow.update();
			downMorePopWindow.showAtLocation(downMoreButton, Gravity.BOTTOM
					| Gravity.CENTER_HORIZONTAL, 0, downMorePopPosY);
		}

	}

	// 创建待转发的音频消息
	public abstract TXMessage getForwardImgTxmsg(String sendImgPath,
			TXMessage txmsg);

	// 创建新的音频消息
	public abstract TXMessage getNewAudioTxmsg();

	// 创建待转发的音频消息
	public abstract TXMessage getForwardAudioTxmsg(TXMessage txmsg);

	// 位置消息返回
	public abstract TXMessage getGeoTxMsg(String geo);

	// 创建待转发的位置消息
	public abstract TXMessage getForwardGeoTxmsg(TXMessage txmsg);

	// 创建待转发的名片消息
	public abstract TXMessage getForwardTCardTxmsg(TXMessage txmsg);

	// 各界面重写发送消息的方法(如果这样的话，上传成功后发送消息直接调用此方法就行了，不用再去判断groupId>0了？？？)
	public abstract void sendMsg(TXMessage txmsg);

	// 各界面重写添加消息的方法
	public abstract void addMsg(TXMessage txmsg);

	// 各界面重写改变消息的方法(原来逻辑好像是只有发送失败了，设置消息状态为failed,才调用此方法？？？)
	public abstract void changeMsg(TXMessage txmsg);

	// 上弹出窗口第一个选项需要根据不同聊天室重写
	public abstract void upToolList1Logic();

	// 删除消息
	public abstract void deleteTxMsg(DialogInterface dialoginterface,
			TXMessage txmsg);

	/** 设置聊天室中对方头像的点击事件 */
	public abstract void setHeadViewOnClickEvent(ImageView iv_headView,
			TXMessage txmsg);

	// 发送消息
	public abstract void send(String text);

	// 上弹出窗口第一个选项文本群和单人不一样
	// 这个暂时不能写为抽象的，因为此方法只有GroupMsgRoom中用到，有实现
	public void upToolList1Text() {

	}

	// 发送地理位置
	public void startSendMeGeo() {

		String geo = "0,0";

		TXMessage txmsgTemp = null;

		if (Utils.isReLocation()) {

			if (isLocationing) {

				Toast.makeText(this, R.string.msgroom_locationing_toast,
						Toast.LENGTH_SHORT).show();

				return;

			}

			isLocationing = true;

			ls = LocationStation.getInstance(mSess.getContext());

			ls.getLocation(this, 30000);

			txmsgTemp = getGeoTxMsg(geo);

			txmsgTemp.updateState = TXMessage.UPDATE;

			gepTxmsgTemp = txmsgTemp;

			addMsg(txmsgTemp);

		} else {

			geo = TxData.public_latitude + "," + TxData.public_longitude;

			txmsgTemp = getGeoTxMsg(geo);

			txmsgTemp.updateState = TXMessage.UPDATE_OK;

			sendMsg(txmsgTemp);

		}

	}

	public void startPicView() {

		String storagePath = Utils.getStoragePath(this);

		if (Utils.isNull(storagePath)) {

			return;

		}

		Intent getImage = new Intent(Intent.ACTION_GET_CONTENT);

		getImage.addCategory(Intent.CATEGORY_OPENABLE);

		getImage.setType(MIME_TYPE_IMAGE_JPEG);

		startActivityForResult(getImage, REQ_TAKE_PICTURE);

		Utils.isNotificationShow = false;

	}

	public void startPhotoCapture() {
		if (Utils.debug) {
			Log.i(TAG, "haishizhe");
		}
		String storagePath = Utils.getStoragePath(BaseMsgRoom.this);

		if (Utils.isNull(storagePath)) {

			return;

		}
		File sddir = new File(storagePath, Utils.IMAGE_PATH);

		if (!sddir.exists() && !sddir.mkdirs()) {

			sddir.mkdir();

		}
		StringBuffer tempPath = new StringBuffer()
				.append(sddir.getAbsolutePath()).append("/").append(img_msg_id)
				.append(".jpg");

		Intent it = new Intent("android.media.action.IMAGE_CAPTURE");

		sendimgPath = tempPath.toString();

		it.putExtra(MediaStore.EXTRA_OUTPUT,
				Uri.fromFile(new File(tempPath.toString())));

		startActivityForResult(it, REQ_TAKE_PHOTO);

		// Utils.inPhoto = true;

		Utils.isNotificationShow = false;

		Utils.creatNoPhoto(BaseMsgRoom.this, Utils.IMAGE_PATH);

	}

	// 分享好友名片进入好友列表界面
	public void startSendPickContacts() {

		Intent intent = new Intent(this, SelectFriendListActivity.class);

		intent.putExtra(SelectFriendListActivity.CHAT_TYPE,
				SelectFriendListActivity.CHAT_TYPE_CARD);

		intent.putExtra(SelectFriendListActivity.CHAT_TYPE_CARD_TYPE,
				SelectFriendListActivity.CHAT_TYPE_CARD_TUIXIN);

		startActivityForResult(intent, SelectFriendListActivity.CHAT_TYPE_CARD);

	}

	// 参数是是否放在上面
	public void creatMsgInfo(boolean isUp, int message) {
		if (isUp) {
			creatCustromInfo(message);
		} else {
			creatSystemInfo(message);
		}
	}

	// 创建公告信息窗口
	public void creatCustromInfo(int message) {
		if (toastList == null) {

			toastList = new ArrayList<Toast>();

		}
		Toast toast = new Toast(getApplicationContext());

		int toastY = 0;

		RelativeLayout Uplayout = (RelativeLayout) findViewById(R.id.rl_msgroom_title_bar);

		toastY = Uplayout.getHeight();

		LayoutInflater mInflater = LayoutInflater.from(this);

		View toastView = mInflater.inflate(R.layout.msgroom_toast, null);

		TextView toastTextView = (TextView) toastView
				.findViewById(R.id.msgRoom_toast_text);

		toastTextView.setText(message);

		toast.setGravity(Gravity.FILL_HORIZONTAL | Gravity.TOP, 0, toastY);

		toast.setDuration(Toast.LENGTH_LONG);

		toast.setView(toastView);

		toast.show();

		toastList.add(toast);

	}

	// 创建系统新题弹出提示
	public void creatSystemInfo(int message) {

		Toast temptoast = Toast.makeText(this, message, Toast.LENGTH_LONG);

		temptoast.show();

		toastList.add(temptoast);

	}

	// 在按下抬起一些按钮时候处理的界面逻辑
	public void changeType(boolean isNoInit, boolean isNoShowInput) {
		if (mPop != null && mPop.getVisibility() == View.VISIBLE) {

			mPop.setVisibility(View.GONE);
			msgroom_expression_hsv_layout.setVisibility(View.GONE);
		}

		if (isNoInit) {
			Utils.isSendText = Utils.isSendText == true ? false : true;
		}

		sendTypeButton
				.setImageResource(Utils.isSendText == true ? R.drawable.sll_msgroom_record_audio_selector
						: R.drawable.sll_msgroom_intput_text_selector);

		if (Utils.isSendText) {
			sendTypeButton.setOnLongClickListener(new OnLongClickListener() {

				@Override
				public boolean onLongClick(View v) {
					if (Utils.checkNetworkAvailable1(mSess.getContext())) {
						if (!Utils.checkSDCard()) {// 无SD卡
							toastList.add(Utils
									.creatNoSdCardInfo(BaseMsgRoom.this));
						} else {
							mRecordPopupWindow
									.showLongRecorderScreen(downlayout);
							hideDownTools();
						}

					} else {
						creatMsgInfo(true, R.string.msg_nonetstr);
					}

					return false;
				}
			});
			msgEdit.setVisibility(View.VISIBLE);

			btn_recordShortAduio.setVisibility(View.GONE);

			SendButton.setVisibility(View.VISIBLE);

			msgEdit.requestFocus();

			if (isNoInit && !isNoShowInput) {
				imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
						InputMethodManager.HIDE_NOT_ALWAYS);
			}

			showInputMethodDialog();

		} else {
			sendTypeButton.setOnLongClickListener(null);
			hideInputMethod(msgEdit);
			// imm.hideSoftInputFromWindow(msgEdit.getWindowToken(), 0);

			msgEdit.setVisibility(View.GONE);

			btn_recordShortAduio.setVisibility(View.VISIBLE);

			SendButton.setVisibility(View.GONE);

		}
	}

	DownUploadListner mUploadImageCallback = new DownUploadListner() {

		@Override
		public void onStart(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateState = TXMessage.UPDATING;
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateCount = (int) (((double) curlen) * 100.0 / (double) totallen);
			if (mCurrentMsgRoom != null) {
				mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
			}
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (Utils.debug)
				Log.i(TAG, "File is uploaded, try to send msg.");
			// 发生 Connection reset by peer异常，服务器断开连接，
			// 且socketClient状态为发送完毕，此时判定这个消息是重传成功的
			txmsg.updateState = TXMessage.UPDATE_OK;
			txmsg.msg_url = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			// saveTxMsgToDB(txmsg);
			Utils.saveTxMsgToDB(txmsg);
			if (txmsg.group_id > 0) {
				mSess.getSocketHelper().sendGroupMsg(txmsg);// 群图片上传成功，发送消息
			} else {
				mSess.getSocketHelper().sendSingleMessage(txmsg);// 图片上传成功，发送消息
			}
			if (mCurrentMsgRoom != null) {
				mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
			}
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.msg_url = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			txmsg.read_state = TXMessage.FAIL;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			// saveTxMsgToDB(txmsg);
			Utils.saveTxMsgToDB(txmsg);
			if (mCurrentMsgRoom != null) {
				mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
			}

		}
	};

	/**
	 * 上传图片并发送
	 * 
	 * @param txmsg
	 */
	protected void postImgSocket(TXMessage txmsg) {
		Bitmap smallImg = Utils.getImgByPath(mSess.getContext(),
				txmsg.msg_path, Utils.msgroom_small_resolution);

		Bitmap bigImg = Utils.getImgByPath(thisContext, txmsg.msg_path,
				Utils.msgroom_big_resolution);

		// int start = txmsg.msg_path.lastIndexOf("/");
		//
		// String fileName = txmsg.msg_path.substring(start + 1);

		if (txmsg.msg_path != null
				&& (txmsg.msg_url == null || txmsg.msg_url.equals(""))) {
			String tempImgPath = Utils.getUploadImageTempPath(txmsg.msg_id);
			try {
				// 生成合成的文件
				mSess.mDownUpMgr.getCompoundImgFile(tempImgPath, smallImg,
						bigImg);
				mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
						mUploadImageCallback, txmsg);
			} catch (IOException e) {
				if (Utils.debug) {
					Log.w(TAG, "合成大小图文件异常", e);
				}
			}
		}

	}

	/** 重传图片 */
	protected void rePostImgSocket(TXMessage txmsg) {
		String tempImgPath = null;
		// 上传地址为空，完全重发
		if (TextUtils.isEmpty(txmsg.msg_path)
				|| new File(txmsg.msg_path).exists()) {
			// 文件本地地址为空或文件不存在
			Bitmap smallImg = Utils.getImgByPath(mSess.getContext(),
					txmsg.msg_path, Utils.msgroom_small_resolution);
			Bitmap bigImg = Utils.getImgByPath(mSess.getContext(),
					txmsg.msg_path, Utils.msgroom_big_resolution);
			tempImgPath = Utils.getUploadImageTempPath(txmsg.msg_id);
			try {
				// 生成合成的文件
				mSess.mDownUpMgr.getCompoundImgFile(tempImgPath, smallImg,
						bigImg);
				if (!TextUtils.isEmpty(txmsg.msg_url)) {
					// 上传地址不为空，用此地址进行续传
					String[] urlArray = mSess.mDownUpMgr
							.getUrlArray(txmsg.msg_url);
					if (urlArray != null && TextUtils.isEmpty(urlArray[2])) {
						// url中截取的path不为空
						mSess.mDownUpMgr.reUploadImg(tempImgPath, 0, true,
								urlArray[2], mReUploadImageCallback, txmsg);
					}
				} else {
					// 上传地址为空，完全重发
					mSess.mDownUpMgr.uploadImg(tempImgPath, 0, true, false,
							mReUploadImageCallback, txmsg);
				}
			} catch (IOException e) {
				if (Utils.debug) {
					Log.w(TAG, "合成大小图文件异常", e);
				}
			}
		}

	}

	/** 重发图片的回调 */
	DownUploadListner mReUploadImageCallback = new DownUploadListner() {
		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateCount = (int) (((double) curlen) * 100.0 / (double) totallen);
			if (Utils.debug) {
				Log.i(TAG, "续传时的上传进度为：" + txmsg.updateCount);
			}
			flush(FLUSH_ONE_LINE, txmsg);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (Utils.debug)
				Log.i(TAG, "File is uploaded, try to send msg.");
			// 发生 Connection reset by peer异常，服务器断开连接，
			// 且socketClient状态为发送完毕，此时判定这个消息是重传成功的
			txmsg.updateState = TXMessage.UPDATE_OK;
			// TODO这个状态应该怎么置？？？之前上传失败的时候置为了failed
			txmsg.read_state = TXMessage.READ;
			txmsg.msg_url = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			TXMessage.updateMsgValues(mSess.getContentResolver(), txmsg.msg_id,
					txmsg.read_state, txmsg.msg_url);
			sendMsg(txmsg);
			flush(FLUSH_ROOM_FALSE);

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.msg_url = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			txmsg.read_state = TXMessage.FAIL;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			flush(FLUSH_ROOM_FALSE);

		}
	};

	/**
	 * 应该是重发语音的方法
	 * 
	 * @param txmsg
	 */
	protected void postAudioSocket(final TXMessage txmsg) {

		if (!Utils.isNull(txmsg.msg_path)) {
			mSess.mDownUpMgr.uploadFile(txmsg.msg_path,
					FileTransfer.FILE_TYPE_AUDIO, 0, true, true,
					new DownUploadListner() {

						@Override
						public void onFinish(FileTaskInfo taskInfo) {

							String fileUrl = taskInfo.mServerHost + ":"
									+ taskInfo.mPath + ":" + taskInfo.mTime;
							if (Utils.debug) {
								Log.i(TAG, "重发的音频fileUrl-->" + fileUrl);
							}

							txmsg.updateState = TXMessage.UPDATE_OK;
							txmsg.msg_url = fileUrl;
							sendMsg(txmsg);
							Utils.saveTxMsgToDB(txmsg);

						}

					});
		}

	}

	protected static BaseMsgRoom mCurrentMsgRoom = null;// 当前MsgRoom对象

	/** 上传大文件 */
	protected void uploadBigFile(final TXMessage txmsg) {

		if (!TextUtils.isEmpty(txmsg.msg_path)) {
			Utils.uploadBigFile(txmsg, new DownUploadListner() {

				@Override
				public void onStart(FileTaskInfo taskInfo) {
					// 开始上传
					txmsg.updateState = TXMessage.UPDATING;
					Utils.saveTxMsgToDB(txmsg);
				}

				@Override
				public void onProgress(FileTaskInfo taskInfo, int curlen,
						int totallen) {
					// 显示上传进度
					txmsg.updateCount = (int) (((double) curlen) * 100.0 / (double) totallen);
					if (mCurrentMsgRoom != null) {
						mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
					}
				}

				@Override
				public void onFinish(FileTaskInfo taskInfo) {

					String fileUrl = taskInfo.mServerHost + ":"
							+ taskInfo.mPath + ":" + taskInfo.mTime;
					if (Utils.debug) {
						Log.i(TAG, "上传的大文件fileUrl-->" + fileUrl);
					}

					// txmsg.read_state = TXMessage.NOT_SENT;//
					// 设置为正在发送？？
					txmsg.updateState = TXMessage.UPDATE_OK;
					txmsg.msg_url = fileUrl;
					sendMsg(txmsg);
					Utils.saveTxMsgToDB(txmsg);
					if (mCurrentMsgRoom != null)
						mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);// 单条刷新
				}

				@Override
				public void onError(FileTaskInfo taskInfo, int iCode,
						Object iPara) {
					// 上传失败
					String fileUrl = taskInfo.mServerHost + ":"
							+ taskInfo.mPath + ":" + taskInfo.mTime;
					txmsg.msg_url = fileUrl;
					txmsg.read_state = TXMessage.FAIL;
					txmsg.updateState = TXMessage.UPDATE_FAILE;
					Utils.saveTxMsgToDB(txmsg);
					if (mCurrentMsgRoom != null)
						mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);// 单条刷新

				}

			}, null);
		}

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
				if (mCurrentMsgRoom != null) {
					mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
				}
			}

			@Override
			public void onFinish(FileTaskInfo taskInfo) {
				if (txmsg != null) {
					txmsg.msg_path = taskInfo.mFullName;
					txmsg.updateState = TXMessage.UPDATE_OK;
					Utils.saveTxMsgToDB(txmsg);
					if (mCurrentMsgRoom != null) {
						mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
					}
					// 写文件增加新增文件数目
					String fileName = txmsg.msg_path.substring(txmsg.msg_path
							.lastIndexOf("/") + 1);
					int fileType = 0;
					if (fileName.contains(".")) {
						fileType = FileManager.getFileType(
								mSess.getContext(),
								fileName.substring(fileName.lastIndexOf(".") + 1));
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
				if (mCurrentMsgRoom != null) {
					mCurrentMsgRoom.flush(FLUSH_ONE_LINE, txmsg);
				}
			}
		}, null);

	}

	// 刷新
	public void flush(int state) {
		if (Utils.debug)
			Log.i(TAG, "发消息刷新的listView");
		MsgHandler.obtainMessage(state, -1, 0).sendToTarget();
	}

	public void flush(int state, int para) {
		if (Utils.debug)
			Log.i(TAG, "发消息刷新的listView,俩参数");
		MsgHandler.obtainMessage(state, para, 0).sendToTarget();
	}

	public void flush(int state, TXMessage txmsg) {

		if (Utils.debug) {
			// 好像state一直都是刷新单条
			if (state == FLUSH_ONE_LINE) {
				Log.i(TAG, "执行了单条刷新");
			}
		}
		MsgHandler.obtainMessage(state, txmsg).sendToTarget();
	}

	// public void flush(Runnable run) {
	// MsgHandler.post(run);
	// }

	DownUploadListner mImageCallback = new DownUploadListner() {
		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (txmsg != null) {

				// 这里下载的是小图，应该不记录时间 2014.05.30 shc
				// if (TextUtils.isEmpty(txmsg.fileDownTime)) {
				// // 从聊天室过来的消息,且第一次下载时间为空
				// txmsg.fileDownTime = System.currentTimeMillis() + "";
				// }

				switch (txmsg.msg_type) {
				case TxDB.MSG_TYPE_IMAGE_EMS:
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:
					txmsg.msg_path = taskInfo.mFullName;
					txmsg.updateState = TXMessage.UPDATE_OK;
					break;
				}
				Utils.saveTxMsgToDB(txmsg);
			}
			Utils.creatNoPhoto(BaseMsgRoom.this, Utils.IMAGE_PATH);
			flush(FLUSH_LISTVIEW_FALSE);

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			// saveTxMsgToDB(txmsg);
			Utils.saveTxMsgToDB(txmsg);
			flush(FLUSH_LISTVIEW_FALSE);
		}
	};

	public void downloadImg(TXMessage txmsg) {
		String imgFilePath = mSess.mDownUpMgr.getImageFile(txmsg.msg_url,
				FileTransfer.SRC_TYPE_DEFAULT, txmsg.msg_id, false);
		mSess.mDownUpMgr.downloadImg(txmsg.msg_url, imgFilePath, 0, false,
				false, mImageCallback, txmsg);
	}

	public void downloadGif(TXMessage txmsg) {
		if (Utils.debug) {
			Log.e(TAG, "2015/1/26 接收至的md5 ：" + txmsg.emomd5);
		}

		mSess.mGifTransfer.downGifPic(txmsg.pkgid, txmsg.emomd5,
				new GifDownUploadListner() {
					@Override
					public void onStart(GifTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						if (txmsg != null) {
							txmsg.updateState = TXMessage.UPDATING;
						}
					}

					@Override
					public void onFinish(GifTaskInfo taskInfo) {
						TXMessage txmsg = (TXMessage) taskInfo.mObj;
						if (Utils.debug) {
							Log.d(TAG, "2015/3/16 md5:" + txmsg.emomd5
									+ ",下载完成");
						}
						GifTransfer.GifDownloadTask info = (GifDownloadTask) taskInfo;
						if (txmsg != null) {

							switch (txmsg.msg_type) {
							case TxDB.MSG_TYPE_SMS_GIF:
							case TxDB.MSG_TYPE_QU_GIF_SMS:
								txmsg.updateState = TXMessage.UPDATE_OK;
								if (info.mFullName != null) {
									AnimationDrawable amd = GifTranscoldMgr
											.getInstance()
											.getAnimationDrawable2(
													txmsg.msg_id, txmsg.emomd5,
													info.mFullName);
									if (amd != null) {
										txmsg.cacheGif = new SoftReference<AnimationDrawable>(
												amd);
										flush(FLUSH_ONE_LINE, txmsg);
										txmsg.clearLoadingImg();
									}
								}
								break;
							}
							Utils.saveTxMsgToDB(txmsg);
						}
						flush(FLUSH_LISTVIEW_FALSE);

					}
				}, 1, txmsg);
	}

	DownUploadListner mAudioDownloadCallback = new DownUploadListner() {
		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage msg = (TXMessage) taskInfo.mObj;
			msg.updateCount = 0;
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			if (txmsg != null) {

				if (TextUtils.isEmpty(txmsg.fileDownTime)) {
					// 从聊天室过来的消息,且第一次下载时间为空
					txmsg.fileDownTime = System.currentTimeMillis() + "";
				}

				txmsg.msg_path = taskInfo.mFullName;
				txmsg.updateState = TXMessage.UPDATE_OK;
				Utils.saveTxMsgToDB(txmsg);
				mAudioRecPlayer.playTalkCache(txmsg, audioHandler);
				if (Utils.debug)
					Log.i(TAG, "音频下载完成，刷新listView");
				flush(FLUSH_LISTVIEW_FALSE);
			}

		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			flush(FLUSH_LISTVIEW_FALSE);
			Utils.saveTxMsgToDB(txmsg);
			mAudioRecPlayer.removeTalkCache(txmsg);
		}
	};

	public void DownAduioScoket(TXMessage txmsg) {
		if (Utils.debug)
			Log.i(TAG, "开始下载音频：txmsg.msg_id:" + txmsg.msg_id + "txmsg.was_me:"
					+ txmsg.was_me + ",txmsg.updateState:" + txmsg.updateState);
		String audioFilePath = mSess.mDownUpMgr.getAudioFile(txmsg.msg_url,
				txmsg.msg_id);
		mSess.mDownUpMgr.downloadAudio(txmsg.msg_url, audioFilePath, 0, false,
				mAudioDownloadCallback, txmsg);
	}

	/** 根据当前用户级别判断是否可以发送语音消息 */
	private boolean isCanSendAudio() {
		if (txGroup != null && (TxGroup.isPublicGroup(txGroup))) {
			if (!mSess.getTxMgr().getTxMe().isCanSendAudio()) {
				// 不可以发送语音
				showToast("需要达到2级才可发语音哦！完善个人资料可以快速升级,赶快去吧！");
				return false;
			}
		}
		return true;

	}

	/**
	 * 录音逻辑相关方法
	 */
	public void registRecorderTouchListener() {
		btn_recordShortAduio.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View v, MotionEvent event) {

				if (record_interrupt) {
					creatBusyRecordInfo();
					return false;
				}

				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					if (!isCanSendAudio()) {
						return false;
					}
					if (Utils.checkNetworkAvailable1(mSess.getContext())) {
						mHasRecordErr = false;
						if (!Utils.checkSDCard()) {// 无SD卡
							toastList.add(Utils
									.creatNoSdCardInfo(BaseMsgRoom.this));
							return false;
						}

						if (mAudioRecPlayer.isRecording()) {
							return false;
						}

						btn_recordShortAduio
								.setText(R.string.publicmsg_recordend);
						mRecordPopupWindow
								.showRecorderScreen(downlayout, false);
						recordAudioUpLoad();
					} else {
						creatMsgInfo(true, R.string.msg_nonetstr);
					}
				} else if (event.getAction() == MotionEvent.ACTION_MOVE) {
					if (event.getY() <= -50) {
						isCancelSendAudioMsg = true;
						exitAudioRecPlayer();
						mSess.mDownUpMgr.removeUploadTask(mSess.mDownUpMgr
								.getUploadTaskId(mAudioRecPlayer
										.getAudioFilePath()));
					}
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					mRecordPopupWindow.cancelRecordProgressTime();
					mRecordPopupWindow.cancelRecordTims();
					mRecordPopupWindow.exitRecorderScreen();
					// isCancelSendAudioMsg = false;
					btn_recordShortAduio.setText(R.string.publicmsg_record);
					if (mHasRecordErr)
						return false;

					if (!mIsRecording) {
						mAudioRecPlayer.stopRecord();
						return false;
					}

					if (mAudioRecPlayer.isRecording())
						stopAudioRecordSocket(false);
				}
				return false;
			}

		});
	}

	// 退出录音
	public void exitAudioRecPlayer() {
		if (mAudioRecPlayer != null) {
			mAudioRecPlayer.stopRecord();
			mAudioRecPlayer.removeAllTalkCache();
			if (mRecordPopupWindow != null) {
				mRecordPopupWindow.cancelRecordProgressTime();
			}
		}
	}

	public void longRecordAudioUpLoad() {
		isCancelSendAudioMsg = false;// 初始化是否取消录音变量
		mAudioRecPlayer.removeAllTalkCache();
		stopPlayAudioRecord();
		if (mAudioRecPlayer != null && !mAudioRecPlayer.isRecording()
				&& mAudioRecPlayer.startToRecord(recordListener)) {
			TXMessage audioMsg = getNewAudioTxmsg();
			audioMsg.read_state = TXMessage.UPDATING;// 设置状态为正在上传
			mAudioRecPlayer.setRecordTxMsg(audioMsg);
			mSess.mDownUpMgr.uploadAudioFile(mAudioRecPlayer, null, false,
					false, mUploadAudioListener, audioMsg);

			// 录音开始暂停播放声音 并且打断自动播放
			if (txMsgs != null) {
				for (TXMessage temptxmsg : txMsgs) {
					temptxmsg.PlayAudio = RECORD_PAUSE;
				}
			}
			closeInitAndBusyRecordInfo();
			mRecordPopupWindow.longrecordSeekTime();
			mRecordPopupWindow.longRecordTimes();

			flush(FLUSH_ROOM_FALSE);
		}

	}

	// 上传录音的listener
	DownUploadListner mUploadAudioListener = new DownUploadListner() {

		@Override
		public void onStart(FileTaskInfo taskInfo) {
			// mUploadTask = ((UploadAudioTask) task);
			if (Utils.debug) {
				Log.i(TAG, "开始上传");
			}
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			if (Utils.debug) {
				Log.e(TAG, "上传进度curlen:" + curlen + "---totallen:" + totallen);
			}

		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			String fileUrl = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			UploadAudioTask upTask = ((UploadAudioTask) taskInfo);
			if (Utils.debug) {
				Log.i(TAG, "fileUrl-->" + fileUrl);
				Log.i(TAG, "录音总时长为："
						+ upTask.getmAudioManager().getAudioDuration() + "s");
				Log.i(TAG, "上传音频完成,开始发送消息");
			}

			if (isCancelSendAudioMsg) {
				if (Utils.debug) {
					Log.i(TAG, "音频信息取消发送");
					Log.i(TAG, "isCancelSend = " + isCancelSendAudioMsg);
				}
				isCancelSendAudioMsg = false;// 把该值复原
				if (Utils.debug) {
					Log.i(TAG, "isCancelSend = " + isCancelSendAudioMsg);
				}
				return;
			}

			TXMessage adiouMsg = (TXMessage) taskInfo.mObj;
			adiouMsg.updateState = TXMessage.UPDATE_OK;// 设置为发送成功？？
			adiouMsg.msg_path = upTask.getmAudioManager().getAudioFilePath();// 设置录音文件本地地址
			adiouMsg.msg_url = fileUrl;
			// 非强制停止的短录音都是2分钟,长录音为7分钟
			adiouMsg.audio_times = upTask.getmAudioManager().getAudioDuration();

			if (!Utils.isRrecord) {
				// 如果还在录音状态，则不发送消息，先存储起来
				// 长录音到达7分钟时，停止录音，但是先不发送消息，用户点击发送按钮时再去发送
				mLongAudioMsg = adiouMsg;
				return;
			}

			// if (isOutTime){
			// //录音超时，那么时长就是120秒
			// adiouMsg.audio_times = 120;
			// }
			// if (adiouMsg.upDataState != TXMessage.upDataFaile) {
			// adiouMsg.upDataState = TXMessage.upDataing;
			// }
			// mRecordAudioManager.setRecordTxMsg(adiouMsg);
			// addMsg(adiouMsg);
			if (adiouMsg.audio_times > 0) {
				// 这里必须再加一个限制，因为上传线程和设置isCancelSendAudioMsg标记的线程不一定同步
				// changeMsg(adiouMsg);//更改音频消息状态
				Utils.saveTxMsgToDB(adiouMsg);

				if (adiouMsg.group_id > 0) {
					mSess.getSocketHelper().sendGroupMsg(adiouMsg);
				} else {
					mSess.getSocketHelper().sendSingleMessage(adiouMsg);
				}
			}

			// TODO为什么再这里置为false,这不是还没停止录音吗？会不会在录音发送时判断出错？
			// mIsRecording = false;
			record_interrupt = true;
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					record_interrupt = false;
					if (mAudioRecPlayer != null) {
						if (Utils.debug) {
							Log.e(TAG, "443");
						}
						// recordManager.setRunning(false);
						mAudioRecPlayer.stopRecord();
					}
					mIsRecording = false;
				}
			};
			timer.schedule(timerTask, 1000);
			mAudioRecPlayer.removeAllTalkCache();

			flush(FLUSH_LISTVIEW_FALSE);
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			if (Utils.debug) {
				Log.i(TAG, "上传音频异常", (Exception) iPara);
			}
			String fileUrl = taskInfo.mServerHost + ":" + taskInfo.mPath + ":"
					+ taskInfo.mTime;
			UploadAudioTask upTask = ((UploadAudioTask) taskInfo);
			TXMessage adiouMsg = (TXMessage) taskInfo.mObj;
			adiouMsg.msg_path = upTask.getmAudioManager().getAudioFilePath();// 设置录音文件本地地址
			adiouMsg.msg_url = fileUrl;
			// 非强制停止的短录音都是2分钟,长录音为7分钟
			adiouMsg.audio_times = upTask.getmAudioManager().getAudioDuration();
			adiouMsg.updateState = TXMessage.UPDATE_FAILE;
			adiouMsg.read_state = TXMessage.FAIL;

			changeMsg(adiouMsg);

			flush(FLUSH_LISTVIEW_FALSE);
		}

	};

	/**
	 * 录音文件上传后，发送音频消息
	 */
	private void sendAudioMsgAfterRocord(TXMessage audioMsg) {
		addMsg(audioMsg);
		sendMsg(audioMsg);
		record_interrupt = true;
		Timer timer = new Timer();
		TimerTask timerTask = new TimerTask() {
			public void run() {
				record_interrupt = false;
				if (mAudioRecPlayer != null) {
					if (Utils.debug) {
						Log.e(TAG, "444");
					}
					// recordManager.setRunning(false);
					mAudioRecPlayer.stopRecord();
				}
				mIsRecording = false;
			}
		};
		timer.schedule(timerTask, 1000);
		mAudioRecPlayer.removeAllTalkCache();

	}

	// 开始录音的相关逻辑
	public void recordAudioUpLoad() {
		isCancelSendAudioMsg = false;// 初始化是否取消录音变量
		mAudioRecPlayer.removeAllTalkCache();
		stopPlayAudioRecord();
		// // 录音开始暂停播放声音 并且打断自动播放
		// if (txMsgs != null) {
		// for (TXMessage temptxmsg : txMsgs) {
		// temptxmsg.PlayAudio = RECORD_PAUSE;
		// }
		// }
		if (mAudioRecPlayer != null && !mAudioRecPlayer.isRecording()
				&& mAudioRecPlayer.startToRecord(recordListener)) {
			TXMessage adiouMsg = getNewAudioTxmsg();
			adiouMsg.read_state = TXMessage.UPDATING;// 设置状态为正在上传
			mAudioRecPlayer.setRecordTxMsg(adiouMsg);
			mSess.mDownUpMgr.uploadAudioFile(mAudioRecPlayer, null, false,
					false, mUploadAudioListener, adiouMsg);

			closeInitAndBusyRecordInfo();
			// closeBusyRecordInfo();
			mRecordPopupWindow.recordProgressTime();
			mRecordPopupWindow.recordTims();
			flush(FLUSH_ROOM_FALSE);
		}
	}

	// 停止播放录音
	public void stopPlayAudioRecord() {
		mAudioRecPlayer.stopPlay();
		TXMessage msg = mAudioRecPlayer.getTxMsg();
		if (msg != null)
			msg.PlayAudio = RECORD_PAUSE;
		// if (playManager != null) {
		// playManager.setRunning(false);
		// }
	}

	/**
	 * 停止长录音，例如长录音时点击取消按钮
	 */
	public void stopLongAudioRecord() {
		if (mAudioRecPlayer != null) {

			record_interrupt = true;
			Timer timer = new Timer();
			TimerTask timerTask = new TimerTask() {
				public void run() {
					mIsRecording = false;
					record_interrupt = false;
					if (mAudioRecPlayer != null) {
						mAudioRecPlayer.stopRecord();
					}
				}
			};
			timer.schedule(timerTask, 1000);
		}
	}

	/**
	 * 录音结束时逻辑 //应该是短录音的，因为录音时长为120s
	 * 
	 * @param isOutTime
	 */
	public void stopAudioRecordSocket(boolean isOutTime) {
		mRecordPopupWindow.cancelRecordProgressTime();

		if (mAudioRecPlayer != null) {
			// 先不把这个语音消息添加到消息队列中
			if (!isCancelSendAudioMsg) {
				// 没有取消该语音消息的发送
				final TXMessage audioMsg = mAudioRecPlayer.getTxMsg();
				audioMsg.read_state = TXMessage.NOT_SENT;
				if (audioMsg.updateState != TXMessage.UPDATE_FAILE)
					audioMsg.updateState = TXMessage.UPDATING;
				audioMsg.msg_path = mAudioRecPlayer.getAudioFilePath();
				if (mAudioRecPlayer.getAudioDuration() > 0) {
					// 录音大于1秒钟时把此录音添加到界面
					addMsg(audioMsg);
				} else {
					// 弹toast显示录音时间过短
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							RECORD_TIME_SHORT);
					isCancelSendAudioMsg = true;// 设置音频消息的标记为取消发送
				}

				// mIsRecording = false;
				record_interrupt = true;
				Timer timer = new Timer();
				TimerTask timerTask = new TimerTask() {
					public void run() {
						record_interrupt = false;
						if (mAudioRecPlayer != null) {
							mAudioRecPlayer.stopRecord();
							mIsRecording = false;

							audioMsg.audio_times = mAudioRecPlayer
									.getAudioDuration();
							flush(FLUSH_ONE_LINE, audioMsg);

						}
					}
				};
				timer.schedule(timerTask, 1000);
				mAudioRecPlayer.removeAllTalkCache();
			}
		}
	}

	// /**
	// * 取消短录音的到2分钟自动停止录音的定时任务
	// */
	// public void cancelRecordTims() {
	// if (recordtime != null) {
	// recordtime.cancel();
	// recordtime = null;
	// }
	// if (recordTask != null) {
	// recordTask.cancel();
	// recordTask = null;
	// }
	// }

	// public void creatUnInitRecordInfo() {
	//
	// if (unInitRecordToast == null) {
	//
	// unInitRecordToast = Toast.makeText(BaseMsgRoom.this,
	// R.string.record_uninit, Toast.LENGTH_SHORT);
	//
	// }
	// unInitRecordToast.show();
	//
	// }

	public void closeInitAndBusyRecordInfo() {
		if (unInitRecordToast != null) {

			unInitRecordToast.cancel();

		}

		if (busyRecordToast != null) {

			busyRecordToast.cancel();

		}

	}

	public void creatBusyRecordInfo() {

		if (busyRecordToast == null) {

			busyRecordToast = Toast.makeText(BaseMsgRoom.this,
					R.string.record_busy, Toast.LENGTH_SHORT);

		}

		busyRecordToast.show();

	}

	public void closeBusyRecordInfo() {

		if (busyRecordToast != null) {

			busyRecordToast.cancel();

		}
	}

	RecordListener recordListener = new RecordListener() {

		@Override
		public void uploadFinish(TXMessage txMsg) {

			if (mAudioRecPlayer != null && txMsg != null) {

				if (txMsg.updateState == TXMessage.UPDATE_OK) {

				} else {

					txMsg.updateState = TXMessage.UPDATE_FAILE;

					txMsg.read_state = TXMessage.FAIL;

					changeMsg(txMsg);

					flush(FLUSH_LISTVIEW_FALSE);

				}
			}
		}

		@Override
		public void recordError(int errcode) {
			// creatUnInitRecordInfo();
			Message msg = Message.obtain(mRecordPopupWindow.getHandler());
			msg.what = HANDLE_RECORDER_ERROR;
			msg.arg1 = errcode;
			msg.sendToTarget();
			mHasRecordErr = true;
		}

		@Override
		public void onPlayFinish(TXMessage txMsg) {

			// playManager.setRunning(false);
			// mAudioRecPlayer.stopPlay();
			txMsg.PlayAudio = RECORD_PAUSE;
			Message message = new Message();// 生成消息，并赋予ID值
			message.what = PLAY_AUDIO_C0MPELET;
			message.obj = txMsg;
			// mainHandler.sendMessage(message);// 投递消息
			MsgHandler.sendMessage(message);// 投递消息

		}

		@Override
		public void doRecordVolume(float volume) {
			volume = Math.abs(volume);
			volume = volume > 1000000 ? volume / 100 : volume;
			volume = volume > 100000 ? volume / 10 : volume;
			int id = (int) (volume / 10000);
			if (id >= volumeResource.length - 1) {
				id = volumeResource.length - 1;
			}
			if (++playTime >= 10) {
				playTime = 0;
				Message message = new Message();
				message.what = FLUSH_VOLUEM_AN;
				message.arg1 = volumeResource[id];
				mRecordPopupWindow.getHandler().sendMessage(message);
			}
		}

		@Override
		public void deviceInitOK() {

			mIsRecording = true;

		}
	};

	// 各种广播的方法和内部类
	// 来电
	class CallComeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Service.TELEPHONY_SERVICE);

			if (tm.getCallState() == TelephonyManager.CALL_STATE_RINGING) {
				isPhone = true;
				MsgRoomMainLayout.isIntercept = false;
				wireControl = WIRECONTROL_NOMAL;
				// 许春会,关闭自动播放音频
				Utils.isOpenPlayAdiou = false;
				Utils.saveAutoPlayAdiouData(BaseMsgRoom.this);
				mRecordPopupWindow.exitRecorderScreen();
				exitAudioRecPlayer();
				mRecordPopupWindow.cancelRecordTims();
				if (txMsgs != null) {
					for (TXMessage temptxmsg1 : txMsgs) {
						temptxmsg1.PlayAudio = RECORD_PAUSE;
					}
				}
				mAudioRecPlayer.removeAllTalkCache();
				stopPlayAudioRecord();
				btn_recordShortAduio.setText(R.string.publicmsg_record);
				flush(FLUSH_LISTVIEW_FALSE);
			}
		}
	}

	// 插拔耳机

	class HeadSetReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction().equals(Intent.ACTION_HEADSET_PLUG)) {
				MsgRoomMainLayout.isIntercept = false;
				wireControl = WIRECONTROL_NOMAL;
				mRecordPopupWindow.exitRecorderScreen();
				exitAudioRecPlayer();
				btn_recordShortAduio.setText(R.string.publicmsg_record);
			}
		}
	}

	// 监听线控广播
	class MediaButtonReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			abortBroadcast();
			String Action = intent.getAction();
			if (Action.equals(Intent.ACTION_MEDIA_BUTTON)) {

				KeyEvent event = (KeyEvent) intent
						.getParcelableExtra(Intent.EXTRA_KEY_EVENT);
				if (event == null)
					return;

				int keyCode = event.getKeyCode();
				int keyAction = event.getAction();
				switch (keyAction) {

				case KeyEvent.ACTION_UP:

					switch (keyCode) {
					case KeyEvent.KEYCODE_HEADSETHOOK:// 播放或暂停
					case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
						if (!Utils.checkSDCard()) {// 无SD卡
							toastList.add(Utils
									.creatNoSdCardInfo(BaseMsgRoom.this));
							return;
						}
						if (record_interrupt) {
							creatBusyRecordInfo();
							return;
						}
						dealWireKeyEvent();
						// if (wireControl == WIRECONTROL_NOMAL) {
						// if (Utils.checkNetworkAvailable1(txdata)) {
						// mHasRecordErr = false;
						// if (mAudioRecPlayer.isRecording())
						// return;
						// wireControl = WIRECONTROL_PLAY;
						// MsgRoomMainLayout.isIntercept = true;
						// mRecordPopupWindow.showRecorderScreen(downlayout,
						// true);
						// recordAudioUpLoad();
						// btn_recordShortAduio.setText(R.string.publicmsg_recordend);
						// musicUtils.PlaySound(1, 1, 0);
						// } else {
						// creatMsgInfo(true, R.string.msg_nonetstr);
						// }
						// } else if (wireControl == WIRECONTROL_PLAY) {
						// wireControl = WIRECONTROL_NOMAL;
						// MsgRoomMainLayout.isIntercept = false;
						// mRecordPopupWindow.cancelRecordTims();
						// mRecordPopupWindow.exitRecorderScreen();
						// btn_recordShortAduio.setText(R.string.publicmsg_record);
						// if (mHasRecordErr)
						// return;
						//
						// if (!mIsRecording) {
						// // recordManager.setRunning(false);
						// mAudioRecPlayer.stopRecord();
						// return;
						// }
						// if (mAudioRecPlayer.isRecording())
						// stopAudioRecordSocket(false);
						// musicUtils.PlaySound(2, 1, 1);
						// }
						break;
					}
					break;
				}
			}
		}
	}

	// 重写dispatchTouchEvent方法
	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {

		if (wireControl == WIRECONTROL_PLAY) {
			return false;
		}
		return super.dispatchTouchEvent(ev);

	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
		if (wireControl == WIRECONTROL_PLAY) {
			return false;
		}
		super.onPrepareOptionsMenu(menu);

		return true;

	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (Utils.debug)
			Log.w(TAG, "点击了按键dispatchKeyEvent:" + event.getKeyCode());
		return super.dispatchKeyEvent(event);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.w(TAG, "点击了按键keyDown:" + event.getKeyCode());
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK:// 播放或暂停
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:// 播放或暂停
			return true;
		case KeyEvent.KEYCODE_BACK:

			if (upMorePopWindow != null && upMorePopWindow.isShowing()) {
				upMorePopWindow.dismiss();
				return false;
			}
			if (downMorePopWindow != null && downMorePopWindow.isShowing()) {
				downMorePopWindow.dismiss();
				return false;
			}
			if (noticeMorePopWindow != null && noticeMorePopWindow.isShowing()) {
				noticeMorePopWindow.dismiss();
				toastNotice.setVisibility(View.VISIBLE);
				return false;
			}

			if (mPop != null && mPop.getVisibility() == View.VISIBLE) {
				mPop.setVisibility(View.GONE);
				msgroom_expression_hsv_layout.setVisibility(View.GONE);
				return false;
			}
			// dealLastTxMsgs();
			ReturnLogic();
			break;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_LOWER,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
			return true;// 此事件不向下传递，避免出现调铃声界面的出现
		case KeyEvent.KEYCODE_VOLUME_UP:

			audioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
					AudioManager.ADJUST_RAISE,
					AudioManager.FX_FOCUS_NAVIGATION_UP);
			return true;// 此事件不向下传递，避免出现调铃声界面的出现
		case KeyEvent.KEYCODE_HOME:
			Intent startMain = new Intent(Intent.ACTION_MAIN);
			startMain.addCategory(Intent.CATEGORY_HOME);
			startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(startMain);
			break;

		}
		return super.onKeyDown(keyCode, event);
	}

	/**
	 * 处理退出聊天室，最后20条
	 */
	public void dealLastTxMsgs() {
		if (txMsgs != null && txMsgs.size() > 0) {

			ArrayList<TXMessage> lasttxmsgsCatch = new ArrayList<TXMessage>();

			int count = 0;

			if (mSess.lastMsgCatch == null) {
				mSess.lastMsgCatch = new HashMap<Long, ArrayList<TXMessage>>();
			}
			for (int i = 0; i < txMsgs.size(); i++) {
				// TODO 处理获取有效信息
				TXMessage txmsg = txMsgs.get(txMsgs.size() - 1 - i);

				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
				} else {
					lasttxmsgsCatch.add(txmsg);
					count++;
					if (count > 19) {
						break;
					}
				}
			}
			mSess.lastMsgCatch.put(id_lastTxmsg, lasttxmsgsCatch);
			if (Utils.debug) {
				Log.i("Zzl",
						"lastMsgCatch : "
								+ mSess.lastMsgCatch.get(id_lastTxmsg)
										.toString());
			}
		}
	}

	/**
	 * 按返回键的逻辑
	 */
	public void ReturnLogic() {
		if (mPop != null && mPop.getVisibility() == View.VISIBLE) {

			mPop.setVisibility(View.GONE);
			msgroom_expression_hsv_layout.setVisibility(View.GONE);
		}

		else {
			{
				// 单聊中的
				if (toastList != null) {
					for (Toast tempToast : toastList) {
						if (tempToast != null) {
							tempToast.cancel();
						}
					}
				}
			}
			Utils.roomid = -1;// 在聊天室点击了返回键要跳转到主页面，则视为退出了聊天室,再来消息就不在通知栏显示
								// 2014.01.15 shc
			Intent intent = new Intent(thisContext, TuiXinMainTab.class);
			startActivity(intent);
			this.finish();
			{
				if (wireControl == WIRECONTROL_PLAY) {
					// 正在录音，则取消线控录音发送
					isCancelSendAudioMsg = true;
					exitAudioRecPlayer();

				}
			}

		}
	}

	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if (Utils.debug)
			Log.w(TAG, "点击了按键keyUp:keyCode=" + keyCode + ",eventKeyCode="
					+ event.getKeyCode());
		switch (keyCode) {
		case KeyEvent.KEYCODE_HEADSETHOOK: // 播放或暂停
		case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE: // 播放或暂停
			if (!Utils.checkSDCard()) { // 无SD卡
				toastList.add(Utils.creatNoSdCardInfo(BaseMsgRoom.this));
				return true;
			}

			if (record_interrupt) {
				creatBusyRecordInfo();
				return true;
			}

			dealWireKeyEvent();// 原来的return true在这个方法中没有体现，不知有没有问题。 2014.01.21
			break;
		}
		return super.onKeyUp(keyCode, event);
	}

	/** 处理线控录音事件 */
	private void dealWireKeyEvent() {
		if (wireControl == WIRECONTROL_NOMAL) {
			if (Utils.checkNetworkAvailable1(mSess.getContext())) {
				mHasRecordErr = false;
				if (mAudioRecPlayer.isRecording())
					return;
				wireControl = WIRECONTROL_PLAY;
				MsgRoomMainLayout.isIntercept = true;
				mRecordPopupWindow.showRecorderScreen(downlayout, true);
				recordAudioUpLoad();
				btn_recordShortAduio.setText(R.string.publicmsg_recordend);
				musicUtils.PlaySound(1, 1, 0);
			} else {
				creatMsgInfo(true, R.string.msg_nonetstr);
			}
		} else if (wireControl == WIRECONTROL_PLAY) {
			wireControl = WIRECONTROL_NOMAL;
			MsgRoomMainLayout.isIntercept = false;
			mRecordPopupWindow.cancelRecordTims();
			mRecordPopupWindow.exitRecorderScreen();
			btn_recordShortAduio.setText(R.string.publicmsg_record);
			if (mHasRecordErr)
				return;

			if (!mIsRecording) {
				// recordManager.setRunning(false);
				mAudioRecPlayer.stopRecord();
				return;
			}
			if (mAudioRecPlayer.isRecording())
				stopAudioRecordSocket(false);
			musicUtils.PlaySound(2, 1, 1);
		}

	}

	// 控制系统播放器广播
	public static final String CMDNAME = "command";
	// public static final String ADIOU_PLAY = "play";
	// public static final String ADIOU_PAUSE = "pause";
	// public static final String ADIOU_TOGGLEPAUSE = "togglepause";

	public static final String SERVICECMD = "com.android.music.musicservicecommand";
	public static final String ADIOU_STATE_ACTION = "com.android.music.playstatechanged";
	public static final String ADIOU_PAUSE_ACTION = "com.android.music.musicservicecommand.pause";
	public static final String TOGGLEPAUSE_ACTION = "com.android.music.musicservicecommand.togglepause";

	public String getRealPathFromURI(Uri contentUri) {
		// can post image
		String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = managedQuery(contentUri, proj, // Which columns to
				// return
				null, // WHERE clause; which rows to return (all rows)
				null, // WHERE clause selection arguments (none)
				null); // Order-by clause (ascending by name)

		int column_index = cursor
				.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		String path = cursor.getString(column_index);
		cursor.close();
		return path;
	}

	/**
	 * 播放音频
	 * 
	 * @param txmsg
	 */
	public void playAudioRecord(TXMessage txmsg) {
		mAudioRecPlayer.startToPlay(txmsg, recordListener);
		if (Utils.debug) {
			Log.i(TAG, "playAudioRecord-->" + txmsg.msg_path);

		}
		txmsg.PlayAudio = RECORD_PLAY;
		if (!txmsg.was_me) {
			if (txmsg.read_state != TXMessage.READ) {// 首播才保存
				txmsg.read_state = TXMessage.READ;
				changeMsg(txmsg);
				// SaveAdiouState(txmsg);
			}
		}
	}

	/**
	 * 添加需要自动播放的音频到队列
	 * 
	 * @param txmsg
	 */
	// public void addTalkCache(TXMessage txmsg) {
	//
	// if (isStartAudioPlay
	// && Utils.isOpenPlayAdiou
	// && !txmsg.was_me
	// && txmsg.read_state == TXMessage.UNREAD
	// && txmsg.updateState != TXMessage.UPDATE_FAILE
	// && (txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS || txmsg.msg_type ==
	// TxDB.MSG_TYPE_QU_AUDIO_EMS)) {
	//
	// if(!talkMsgsCache.contains(txmsg))
	// talkMsgsCache.add(txmsg);
	// }
	// }

	/**
	 * 删除队列里的音频对象
	 * 
	 * @param txmsg
	 */
	// public void removeTalkCache(TXMessage txmsg) {
	// if (talkMsgsCache.size() > 0) {
	// talkMsgsCache.remove(txmsg);
	// }
	// }

	/**
	 * 删除所有音频
	 * 
	 */
	// public void removeAllTalkCache() {
	// talkMsgsCache.clear();
	// }

	/**
	 * 点击播放音频逻辑
	 * 
	 * @param txmsg
	 */
	public void clickPlayTalkCache(TXMessage txmsg) {
		isStartAudioPlay = true;
		mAudioRecPlayer.removeAllTalkCache();

		if (txMsgs.size() > 0) {
			int id = txMsgs.indexOf(txmsg);
			if (txmsg.read_state == TXMessage.READ) {
				if (Utils.debug) {
					Log.i(TAG, "clickPlayTalkCache->" + txmsg.msg_path);
				}
				isStartAudioPlay = false;
			} else if (txmsg.read_state == TXMessage.UNREAD && !txmsg.was_me) {
				for (int i = id + 1; i < txMsgs.size(); i++) {
					mAudioRecPlayer.addTalkCache(txMsgs.get(i),
							isStartAudioPlay);
				}
			}
		}
	}

	// public void playTalkCache(TXMessage txmsg) {
	// if (Utils.isOpenPlayAdiou && talkMsgsCache.size() > 0) {
	//
	// TXMessage txmsg1 = talkMsgsCache.get(0);
	// if (txmsg1 != null && txmsg1.equals(txmsg)) {
	// while (txmsg.updateState == TXMessage.UPDATE_FAILE) {
	// talkMsgsCache.remove(txmsg1);
	// if (talkMsgsCache.size() == 0) {
	// break;
	// }
	// txmsg1 = talkMsgsCache.get(0);
	// }
	// if (!Utils.isNull(txmsg1.msg_path) &&
	// mAudioRecPlayer.getPlayingMsg()==null) {
	// if (Utils.debug) {
	// Log.i(TAG, "playTalkCache->" + txmsg1.msg_path);
	// }
	// playAudioRecord(txmsg1);
	// }
	// }
	// }
	// }

	protected void removeAllToasts() {
		if (toastList != null) {
			for (Toast tst : toastList) {
				if (tst != null)
					tst.cancel();
			}
			toastList.clear();
		}
	}

	// int count = 0;
	//
	// private String calculator(int audiotime) {
	// // count = audiotime / 1000;
	// count = audiotime;// audiotime取到的单位就是s,不能再除以1000了
	// return String.format("%02d:%02d", count / 60, count % 60);
	// }

	// TextWatcher mTextWatcher = new TextWatcher() {
	// private CharSequence temp;
	// private int editStart;
	// private int editEnd;
	//
	// @Override
	// public void onTextChanged(CharSequence s, int start, int before,
	// int count) {
	// temp = s;
	// SendButton
	// .setBackgroundResource((s.length() > 0) ? R.drawable.pubdown_sendbutton
	// : R.drawable.pubgray_sendbutton);
	// }
	//
	// @Override
	// public void beforeTextChanged(CharSequence s, int start, int count,
	// int after) {
	// // countNum.setText(s);//将输入的内容实时显示
	// }
	//
	// @Override
	// public void afterTextChanged(Editable s) {
	// }
	// };

	// public void onAttachedToWindow() {
	// this.getWindow().setType(WindowManager.LayoutParams.TYPE_KEYGUARD);
	// super.onAttachedToWindow();
	// };

	/** 开启地图应用查看地理位置信息 */
	protected void startViewMap(TXMessage txmsg) {
		Uri uri = Uri.parse(Utils.GOOGLE_MAP_STR + txmsg.geo);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		try {
			startActivity(intent);
			// Utils.inPhoto = true;
		} catch (Exception e) {
			Toast.makeText(mSess.getContext(), "请检查您的移动设备上是否已安装地图类应用",
					Toast.LENGTH_LONG).show();
			if (Utils.debug) {
				Log.e(TAG,
						"当用户手机上没有安装地图应用的时候，这时开启地图会报ActivityNotFoundException异常",
						e);
			}
		}

	}

	static class ViewHolder {
		TextView tv_msgTime;// 消息时间
		RelativeLayout rl_msgHeadView;// 消息对方头像布局
		LinearLayout ll_my_head_icon;// 我的消息头像布局
		TextView tv_msgSendName;// 消息发送者昵称
		View leftlist_type1;
		View leftlist_type2;
		View leftlist_type3;
		View leftlist_type4;
		View leftlist_type5;
		View leftlist_type6;
		// 13 是gif消息
		View leftlist_type13;
		View list_type10;
		// 11是定位消息
		View list_type12;
		// ProgressBar loadingView;//没什么用吧，一直都是gone 2014.03.14 shc
		TXMessage txmsg;
		ImageView headView;// 用于回调时头像的显示
		ImageView myHeadView;// 我的头像显示

		View last_week_starts;
		SlGridView gv_last_week_stars;// 在holder里面添加此变量是为了解决star头像晃动问题。
	}

	ViewHolder holder = null;
	boolean waitDouble = true;// 双击头像时用吧？

	// private GroupStarAdapter starAdapter = null;// 星标好友的adapter

	/** 各种类型聊天布局 */
	class GroupContectListAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		ArrayList<TXMessage> txmsgs_list;

		public GroupContectListAdapter() {
			this.mInflater = LayoutInflater.from(mSess.getContext());
			txmsgs_list = new ArrayList<TXMessage>();
		}

		@Override
		public int getCount() {
			if (txmsgs_list != null)
				return txmsgs_list.size();
			else
				return 0;
		}

		public void setData(ArrayList<TXMessage> txMsgs) {
			// TODO 这里进行了克隆
			this.txmsgs_list = (ArrayList<TXMessage>) txMsgs.clone();
			// TODO 暂时先这么比较 定下策略后再改
			Collections.sort(txmsgs_list, new MessageComparator());
		}

		public ArrayList<TXMessage> getData() {
			return this.txmsgs_list;
		}

		@Override
		public Object getItem(int position) {
			return position;
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		abstract class MyOnLongClickListener<TDat> implements
				OnLongClickListener {
			public void setTo(View view, TDat obj) {
				view.setTag(obj);
				view.setOnLongClickListener(this);
			}
		}

		abstract class MyOnClickListener<TDat> implements OnClickListener {
			public void setTo(View view, TDat obj) {
				view.setTag(obj);
				view.setOnClickListener(this);
			}
		}

		MyOnLongClickListener<TXMessage> myOnLongclick = new MyOnLongClickListener<TXMessage>() {
			@Override
			public boolean onLongClick(View v) {
				if (Utils.debug)
					Log.i("bobo",
							"-------------------dianjile000000000000000000---");
				TXMessage txmessage = (TXMessage) v.getTag();
				creatListWindow(txmessage);
				return false;
			}
		};
		// MyOnLongClickListener<ViewHolder> myOnLongclick2 = new
		// MyOnLongClickListener<ViewHolder>() {
		// @Override
		// public boolean onLongClick(View v) {
		// ViewHolder holder = (ViewHolder) v.getTag();
		// creatListWindow(holder.txmsg);
		// return false;
		// }
		// };
		// MyOnClickListener<ViewHolder> mImgOnClick = new
		// MyOnClickListener<ViewHolder>() {
		// @Override
		// public void onClick(View v) {
		// if (!Utils.checkSDCard()) {// 无SD卡
		// toastList.add(Utils.creatNoSdCardInfo(txdata));
		// return;
		// }
		// ViewHolder holder = (ViewHolder) v.getTag();
		// if (holder.loadingView.getVisibility() == View.GONE) {
		// Intent picIntent = new Intent(BaseMsgRoom.this,
		// EditSendImg.class);
		// picIntent.putExtra(EditSendImg.TXMESSAGE, holder.txmsg);
		// picIntent.putExtra(EditSendImg.USER_ID, user_id);
		// startActivity(picIntent);
		// }
		// }
		// };

		// 把图片和音频消息整合到一起了,在mImgViewOnClick2中 2013.12.02 shc
		// /** 应该是聊天室中图片消息的图片点击事件 */
		// MyOnClickListener<ViewHolder> mImgViewOnClick = new
		// MyOnClickListener<ViewHolder>() {
		// @Override
		// public void onClick(View v) {
		// ViewHolder holder = (ViewHolder) v.getTag();
		// if (!Utils.checkSDCard()) {// 无SD卡
		// toastList.add(Utils.creatNoSdCardInfo(txdata));
		// return;
		// }
		//
		// }
		//
		// };

		// 我发送的语音的点击事件
		// MyOnClickListener<ViewHolder> mPlayRecOnClick = new
		// MyOnClickListener<ViewHolder>() {
		// @Override
		// public void onClick(View v) {
		// if (!Utils.checkSDCard()) {// 无SD卡
		// toastList.add(Utils.creatNoSdCardInfo(mSess.getContext()));
		// return;
		// }
		// ViewHolder holder = (ViewHolder) v.getTag();
		// // if (holder.loadingView.getVisibility() == View.GONE) {
		// switch (holder.txmsg.PlayAudio) {
		// case RECORD_PAUSE:
		// // playManager.setRunning(false);
		// stopPlayAudioRecord();
		// clickPlayTalkCache(holder.txmsg);
		// playAudioRecord(holder.txmsg);
		// flush(FLUSH_LISTVIEW_FALSE);
		// {
		// // 从单聊里面拷贝过来的
		// if (Utils.isHandset) {
		// Utils.creatMsgRoomUpToolsInfo(BaseMsgRoom.this,
		// R.string.msgroom_toast_handset,
		// R.drawable.msgroom_more_pop_handset);
		// }
		// }
		// break;
		// case RECORD_PLAY:
		// holder.txmsg.PlayAudio = RECORD_PAUSE;
		// removeAllTalkCache();
		//
		// stopPlayAudioRecord();
		// flush(FLUSH_LISTVIEW_FALSE);
		// break;
		// }
		// // }
		// }
		// };

		/** 应该是聊天室中图片消息和音频消息的点击事件 */
		MyOnClickListener<TXMessage> mImgViewOnClick2 = new MyOnClickListener<TXMessage>() {
			@Override
			public void onClick(View v) {
				TXMessage txmsg = (TXMessage) v.getTag();
				if (!Utils.checkSDCard()) {// 无SD卡
					toastList.add(Utils.creatNoSdCardInfo(mSess.getContext()));
					return;
				}

				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_AUDIO_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_AUDIO_EMS) {
					// 音频消息
					if (txmsg.updateState == TXMessage.UPDATE) {
						clickPlayTalkCache(txmsg);
						txmsg.updateState = TXMessage.UPDATING;
						DownAduioScoket(txmsg);
						flush(FLUSH_LISTVIEW_FALSE);
					} else {
						if (!Utils.isNull(txmsg.msg_path)
								&& txmsg.updateState != TXMessage.UPDATING
								&& txmsg.updateState != TXMessage.UPDATE_FAILE) {
							switch (txmsg.PlayAudio) {

							case RECORD_PAUSE:
								stopPlayAudioRecord();
								clickPlayTalkCache(txmsg);
								playAudioRecord(txmsg);
								flush(FLUSH_LISTVIEW_FALSE);

								break;
							case RECORD_PLAY:
								txmsg.PlayAudio = RECORD_PAUSE;
								mAudioRecPlayer.removeAllTalkCache();
								stopPlayAudioRecord();
								flush(FLUSH_LISTVIEW_FALSE);
								break;
							}

						}
					}
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_IMAGE_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_IMAGE_EMS) {
					// 图片消息
					if (txmsg.updateState == TXMessage.UPDATE_CLICK) {
						if (Utils.isNull(txmsg.msg_path)) {
							txmsg.cacheImg = null;
							txmsg.updateState = TXMessage.UPDATING;
							if (!Utils.checkSDCard()) {// 无SD卡
								txmsg.updateState = TXMessage.UPDATE_FAILE;
								flush(FLUSH_LISTVIEW_FALSE);
							} else {
								downloadImg(txmsg); // 转圈

							}
						}
						txmsg.updateState = TXMessage.UPDATING;
						flush(FLUSH_LISTVIEW_FALSE);
					} else {
						Intent picIntent = new Intent();
						picIntent.setClass(BaseMsgRoom.this, EditSendImg.class);
						picIntent.putExtra(EditSendImg.TXMESSAGE, txmsg);
						picIntent.putExtra(EditSendImg.FROM_MSG_ROOM, true);
						picIntent.putExtra(EditSendImg.USER_ID,
								TX.tm.getUserID());
						if (TextUtils.isEmpty(txmsg.fileDownTime)) {
							startActivityForResult(picIntent,
									DOWNLOAD_VIEW_BIG_PIC);
						} else {
							startActivity(picIntent);
						}
					}
				}

			}
		};
		MyOnClickListener<TXMessage> mCardOnClick = new MyOnClickListener<TXMessage>() {
			@Override
			public void onClick(View v) {
				TXMessage txmsg = (TXMessage) v.getTag();
				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
				} else {
					if (TX.tm.getUserID() == txmsg.tcard_id) {
						Intent intent = new Intent(BaseMsgRoom.this,
								SetUserInfoActivity.class);
						startActivity(intent);
					} else {
						TX tx1 = null;
						if (tx1 == null) {
							if (txmsg != null) {
								tx1 = TX.tm.getTx(txmsg.tcard_id);
								if (tx1 == null) {
									tx1 = new TX();
									tx1.setPartnerId(txmsg.tcard_id);
									tx1.setNick_name(txmsg.tcard_name);
									tx1.setAvatar_url(txmsg.tcard_avatar_url);
									tx1.setSign(txmsg.tcard_sign.trim());
									tx1.setSex(txmsg.tcard_sex);
									tx1.setPhone(txmsg.tcard_phone);

									TX.tm.addTx(tx1);
								}
							}

						}
						Intent intent = new Intent(BaseMsgRoom.this,
								UserInformationActivity.class);
						intent.putExtra(
								UserInformationActivity.pblicInfo,
								TX.tm.isTxFriend(txmsg.tcard_id) ? UserInformationActivity.TUIXIN_USER_INFO
										: UserInformationActivity.NOT_TUIXIN_USER_INFO);
						intent.putExtra(UserInformationActivity.UID,
								tx1.partner_id);
						startActivity(intent);
					}
				}
			}
		};
		MyOnClickListener<TXMessage> mGeoImgOnClick = new MyOnClickListener<TXMessage>() {
			@Override
			public void onClick(View v) {
				TXMessage txmsg = (TXMessage) v.getTag();
				if (txmsg.read_state != TXMessage.NOT_SENT) {
					startViewMap(txmsg);
				}
			}
		};

		// 这个onClick事件存在 意义？语音时长控件那么小，怎么点住呢？ 2014.03.19 shc
		// 先去掉此功能，有问题再修改。2014.05.12 shc
		// MyOnClickListener<TXMessage> mVoiceTimeOnClick = new
		// MyOnClickListener<TXMessage>() {
		// @Override
		// public void onClick(View v) {
		// TXMessage txmsg = (TXMessage) v.getTag();
		// if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
		// txmsg.updateState = TXMessage.UPDATE;
		// flush(FLUSH_LISTVIEW_FALSE);
		// }
		// }
		// };

		MyOnLongClickListener<TXMessage> mHeadOnLongClick = new MyOnLongClickListener<TXMessage>() {
			@Override
			public boolean onLongClick(View v) {
				TXMessage txmsg = (TXMessage) v.getTag();
				createWindow(txmsg);
				return false;
			}
		};

		public void updateListInfo(TX tx) {
			mSess.avatarDownload.downAvatar(tx.avatar_url, tx.partner_id, null,
					avatarHandler);
			for (TXMessage msg : txmsgs_list) {
				if (msg.partner_id == tx.partner_id) {
					// 需要吗？ 2014.04.16
					msg.nick_name = tx.getNick_name();
					msg.sex = tx.getSex();
				}
			}
		}

		/** 单条刷新，通过【消息id】判定holder(因为退出聊天室再进入消息对象都是重新创建的),然后从新设置该holder的显示view */
		public boolean updateView(TXMessage txmsg) {
			for (ViewHolder hldr : mViewLines) {
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

		protected TextView dateView; // 日期
		protected TextView sendTypeView; // 发送类型
		protected RelativeLayout rl_msgHeadView = null;// 对方头像外部布局
		protected LinearLayout ll_my_head_icon = null;// 我的头像外部布局
		private String getAdd;
		protected gifOpenHelper gHelper = new gifOpenHelper();

		public String getEmojiPath(String emod5) {
			try {
				ArrayList<EmojiInfor> list = mSess.mUserEmojiInforsMgr
						.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
				for (int i = 0; i < list.size(); i++) {
					EmojiInfor emojiInfor = list.get(i);
					if (emojiInfor.emoji_md5.equals(emod5)) {
						return emojiInfor.emoji_path;
					}
				}
			} catch (JSONException e) {

				e.printStackTrace();
			}
			return null;
		}

		public String gifKey(String emod5, int num, int id) {
			return emod5 + num + id;
		}

		protected TextView updateView(ViewHolder holder) {
			dateView = holder.tv_msgTime;
			final TXMessage txmsg = holder.txmsg;
			if (Utils.debug) {
				Log.i(TAG, "2015/1/22" + txmsg.toString());
			}
			TextView sendName = null;// 消息发送者昵称
			ImageView FailImgView; // 失败标示
			ProgressBar loadingView; // 加载进度
			ProgressBar myLoadingView; // 我的加载进度

			// 头像
			rl_msgHeadView = (RelativeLayout) holder.rl_msgHeadView;
			ll_my_head_icon = (LinearLayout) holder.ll_my_head_icon;
			holder.headView = (ImageView) rl_msgHeadView
					.findViewById(R.id.msgroomitem_List1_head);
			holder.myHeadView = (ImageView) ll_my_head_icon
					.findViewById(R.id.iv_msg_head_icon_mine);
			// 名字
			sendName = (TextView) holder.tv_msgSendName;

			// 设置时间显示
			if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_SMS
					|| txmsg.msg_type == TxDB.MSG_TYPE_SMS_IMG
					|| txmsg.msg_type == TxDB.MSG_TYPE_SMS_AUDIO
					|| txmsg.msg_type == TxDB.MSG_TYPE_SMS_GEO) {
				dateView.setText(dealDate(txmsg.send_time));
			} else {
				dateView.setText(dealDate(txmsg.send_time * 1000));
			}

			sendName.setVisibility(View.VISIBLE);
			RelativeLayout.LayoutParams rel_params = new RelativeLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
			LinearLayout.LayoutParams lin_params = new LinearLayout.LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

			switch (txmsg.msg_type) {
			case TxDB.MSG_TYPE_SMS_GIF:
			case TxDB.MSG_TYPE_QU_GIF_SMS:
				holder.leftlist_type13.setVisibility(View.VISIBLE);
				FailImgView = (ImageView) holder.leftlist_type13
						.findViewById(R.id.msgroomitem_List13_failImg);
				loadingView = (ProgressBar) holder.leftlist_type13
						.findViewById(R.id.msgroomitem_List13_loading);
				myLoadingView = (ProgressBar) holder.leftlist_type13
						.findViewById(R.id.msgroomitem_RList13_loading);
				FailImgView.setVisibility(View.GONE);
				loadingView.setVisibility(View.GONE);
				myLoadingView.setVisibility(View.GONE);
				// String key = gifKey(txmsg.emomd5, txmsg.num, txmsg.pkgid);

				ImageView contentImgView_gif = (ImageView) holder.leftlist_type13
						.findViewById(R.id.msgroomitem_List13_MsgGIF);

				android.view.ViewGroup.LayoutParams params = contentImgView_gif
						.getLayoutParams();

				contentImgView_gif.setScaleType(ScaleType.FIT_XY);

				LinearLayout shoudong_down_img_gif = (LinearLayout) holder.leftlist_type13
						.findViewById(R.id.shoudong_down_img);

				contentImgView_gif.setImageBitmap(null);//
				// 因为布局文件中默认图片是下载失败文件。不置空会闪一下下载失败的图片
				FrameLayout fl_imageMessage_gif = (FrameLayout) holder.leftlist_type13
						.findViewById(R.id.msgroomitem_List13_dateLayout11);

				AnimationDrawable bitmap_gif = null;
				if (txmsg.was_me) {
					loadingView.setVisibility(View.GONE);

					lin_params.gravity = Gravity.RIGHT;
					holder.leftlist_type13.setLayoutParams(lin_params);

					fl_imageMessage_gif
							.setBackgroundResource(R.drawable.msg_img_bg_mine);
					if (txmsg.cacheGif != null) {
						bitmap_gif = txmsg.cacheGif.get();
						myLoadingView.setVisibility(View.GONE);
					}
					if (bitmap_gif == null && !txmsg.setLoadingImg()) {
						myLoadingView.setVisibility(View.VISIBLE);
						mGroupAsynloader.post(new Runnable() {// 异步加载图片
									@Override
									public void run() {
										if (txmsg.emomd5 != null
												&& txmsg.pkg_emomd5 != null) {
											AnimationDrawable amd = null;
											if (getLocalGifPath(txmsg) != null) {
												amd = gifTranscoldMgr
														.getInstance()
														.getAnimationDrawable2(
																txmsg.msg_id,
																txmsg.emomd5,
																getLocalGifPath(txmsg));

											} else if (amd == null
													&& getEmojiPath(txmsg.pkg_emomd5) != null) {
												amd = GifTranscoldMgr
														.getInstance()
														.getAnimationDrawable(
																txmsg.msg_id,
																txmsg.num,
																getEmojiPath(txmsg.pkg_emomd5),
																txmsg.emomd5,
																txmsg.pkgid);
											}
											if (amd != null) {
												txmsg.cacheGif = new SoftReference<AnimationDrawable>(
														amd);
												flush(FLUSH_ONE_LINE, txmsg);
											}
											txmsg.clearLoadingImg();
											if (txmsg.emomd5 != null
													&& amd == null) {
												if (Utils.debug) {
													Log.d(TAG,
															"2015/3/16 我方 下载的GIF:"
																	+ txmsg.emomd5);
												}
												downloadGif(txmsg); // 转圈
											}
										}
									}
								});
					}
					if (bitmap_gif != null) {
						params.height = bitmap_gif.getIntrinsicHeight()
								* GIF_SIZE;
						params.width = bitmap_gif.getIntrinsicWidth()
								* GIF_SIZE;
					}
					contentImgView_gif.setImageDrawable(bitmap_gif);
					if (bitmap_gif != null) {
						contentImgView_gif.post(new GifDisplayRunnable(
								bitmap_gif));
					}
					shoudong_down_img_gif.setVisibility(View.GONE);
					myLoadingView.setVisibility(View.GONE);

					if (txmsg.updateState == TXMessage.UPDATE) {
						// 显示原图
						if (Utils.isNull(txmsg.msg_url)
								&& !Utils.isNull(txmsg.msg_path)) {
							txmsg.updateState = TXMessage.UPDATE_FAILE;
						} else {
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
					} else if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						FailImgView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATING) {
						// 显示原图
						myLoadingView.setProgress(txmsg.updateCount);
						myLoadingView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
						// 显示原图
						if (txmsg.cacheGif != null) {
							bitmap_gif = txmsg.cacheGif.get();
							myLoadingView.setVisibility(View.GONE);
						}
						if (bitmap_gif != null) {
							params.height = bitmap_gif.getIntrinsicHeight()
									* GIF_SIZE;
							params.width = bitmap_gif.getIntrinsicWidth()
									* GIF_SIZE;
						}
						contentImgView_gif.setImageDrawable(bitmap_gif);
						if (bitmap_gif != null) {
							contentImgView_gif.post(new GifDisplayRunnable(
									bitmap_gif));
						}
						shoudong_down_img_gif.setVisibility(View.GONE);
						myLoadingView.setVisibility(View.GONE);
					}

					// TODO 还有对方发来的gif动图的显示
				} else {
					// 对方发的GIF
					if (Utils.debug) {
						Log.d(TAG, "2015/3/16有对方发送来的GIF消息: " + txmsg.emomd5
								+ ",消息状态是" + txmsg.updateState);
					}
					myLoadingView.setVisibility(View.GONE);
					fl_imageMessage_gif
							.setBackgroundResource(R.drawable.msg_img_bg_other);

					lin_params.gravity = Gravity.LEFT;
					holder.leftlist_type13.setLayoutParams(lin_params);
					switch (txmsg.updateState) {

					case TXMessage.UPDATE:
						if (txmsg.cacheImg != null) {// 从缓存读图片
							bitmap_gif = txmsg.cacheGif.get();
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
						if (bitmap_gif == null && !txmsg.setLoadingImg()) {
							txmsg.updateState = TXMessage.UPDATING;
							mGroupAsynloader.post(new Runnable() {// 异步加载图片
										@Override
										public void run() {
											if (Utils.isAutoDownLoadImg(mSess
													.getContext())) {
												txmsg.updateState = TXMessage.UPDATING;
												AnimationDrawable amd = null;
												if (getLocalGifOppositePath(txmsg) != null) {
													amd = gifTranscoldMgr
															.getInstance()
															.getAnimationDrawable2(
																	txmsg.msg_id,
																	txmsg.emomd5,
																	getLocalGifOppositePath(txmsg));
													if (amd == null) {
														amd = GifTranscoldMgr
																.getInstance()
																.getAnimationDrawable2(
																		txmsg.msg_id,
																		txmsg.emomd5,
																		getLocalGifOppositePath(txmsg));
													}
												}
												if (amd != null) {// 无SD卡
													// 从本地取出GIF
													txmsg.cacheGif = new SoftReference<AnimationDrawable>(
															amd);
													txmsg.updateState = TXMessage.UPDATE_OK;
													// flush(FLUSH_LISTVIEW_FALSE);
												} else {
													if (Utils.debug) {
														Log.d(TAG,
																"2015/3/16 对方 下载的GIF:"
																		+ txmsg.emomd5
																		+ ",调用状态:UPDATE");
													}
													downloadGif(txmsg); // 转圈
												}
											} else {
												txmsg.updateState = TXMessage.UPDATE_CLICK;
											}

											flush(FLUSH_ONE_LINE, txmsg);
											txmsg.clearLoadingImg();
										}
									});
						}
						if (bitmap_gif != null) {
							contentImgView_gif.post(new GifDisplayRunnable(
									bitmap_gif));
						}

						break;
					case TXMessage.UPDATING:
						contentImgView_gif.setImageBitmap(null);
						shoudong_down_img_gif.setVisibility(View.GONE);
						loadingView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_FAILE:
						contentImgView_gif
								.setImageResource(R.drawable.msg_img_download_failed);
						shoudong_down_img_gif.setVisibility(View.GONE);
						FailImgView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_OK:

						if (txmsg.cacheGif != null) {// 从缓存读图片
							bitmap_gif = txmsg.cacheGif.get();
							if (bitmap_gif != null) {
								params.height = bitmap_gif.getIntrinsicHeight()
										* GIF_SIZE;
								params.width = bitmap_gif.getIntrinsicWidth()
										* GIF_SIZE;
							}
							contentImgView_gif.setImageDrawable(bitmap_gif);
						}
						if (bitmap_gif == null && !txmsg.setLoadingImg()) {
							txmsg.updateState = TXMessage.UPDATING;
							mGroupAsynloader.post(new Runnable() {// 异步加载图片
										@Override
										public void run() {
											if (Utils.isAutoDownLoadImg(mSess
													.getContext())) {
												txmsg.updateState = TXMessage.UPDATING;
												AnimationDrawable amd = null;
												if (getLocalGifOppositePath(txmsg) != null) {
													amd = gifTranscoldMgr
															.getInstance()
															.getAnimationDrawable2(
																	txmsg.msg_id,
																	txmsg.emomd5,
																	getLocalGifOppositePath(txmsg));
													if (amd == null) {
														amd = GifTranscoldMgr
																.getInstance()
																.getAnimationDrawable2(
																		txmsg.msg_id,
																		txmsg.emomd5,
																		getLocalGifOppositePath(txmsg));
													}

												}
												if (amd != null) {// 无SD卡
													// 从本地取出GIF
													txmsg.cacheGif = new SoftReference<AnimationDrawable>(
															amd);
													txmsg.updateState = TXMessage.UPDATE_OK;
													// flush(FLUSH_LISTVIEW_FALSE);
												} else {
													if (Utils.debug) {
														Log.d(TAG,
																"2015/3/16 对方 下载的GIF:"
																		+ txmsg.emomd5
																		+ ",调用状态UPDATE_OK");
													}
													downloadGif(txmsg); // 转圈
												}
											} else {
												txmsg.updateState = TXMessage.UPDATE_CLICK;
											}

											flush(FLUSH_ONE_LINE, txmsg);
											txmsg.clearLoadingImg();
										}
									});
						}
						if (bitmap_gif != null) {
							contentImgView_gif.post(new GifDisplayRunnable(
									bitmap_gif));
						}
						shoudong_down_img_gif.setVisibility(View.GONE);
						break;
					case TXMessage.UPDATE_CLICK:
						shoudong_down_img_gif.setVisibility(View.VISIBLE);

						break;
					case TXMessage.LOAD_FAILED:
						contentImgView_gif
								.setImageResource(R.drawable.msg_img_download_failed);
						shoudong_down_img_gif.setVisibility(View.GONE);
						FailImgView.setVisibility(View.GONE);
						break;

					}

				}
				fl_imageMessage_gif
						.setOnLongClickListener(new OnLongClickListener() {

							@Override
							public boolean onLongClick(View v) {
								creatListWindow(txmsg);
								return false;
							}
						});
				break;
			case TxDB.MSG_TYPE_QU_COMMON_SMS:
			case TxDB.MSG_TYPE_COMMON_SMS:
			case TxDB.MSG_TYPE_SMS_SMS:

				holder.leftlist_type1.setVisibility(View.VISIBLE);
				// 包裹纯文本信息的ViewGroup
				ViewGroup ll_leftlist_textMsg = (ViewGroup) holder.leftlist_type1
						.findViewById(R.id.ll_leftlist_textMsg);
				// 更新短信内容
				TextView msgTextView = (TextView) holder.leftlist_type1
						.findViewById(R.id.msgroomitem_List1_MsgText);
				// 已在群聊文字信息中加上txmsg.partner_id，
				// 下面的txmsg.msg_type==TxDB.MSG_TYPE_QU_COMMON_SMS是为了兼容老版本，让自己发的群信息能正常显示，老版本中自己发送的群消息没有txmsg.partner_id
				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS
						|| Utils.isIdValid(txmsg.partner_id)) {
					if (txmsg.cache_charSequence == null) {
						CharSequence s = smileyParser.addSmileySpans(
								txmsg.msg_body, false, 0);
						txmsg.cache_charSequence = smileyParser.addAtpans(s);
					}
					msgTextView.setText(txmsg.cache_charSequence);
				}

				sendTypeView = (TextView) holder.leftlist_type1
						.findViewById(R.id.msgroomitem_RList1_SendTypeText);

				if (txmsg.was_me) {

					// 其他状态文字为蓝色 气泡为正常0xfff69200
					rel_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					ll_leftlist_textMsg.setLayoutParams(rel_params);
					// msgTextView.setLayoutParams(rel_params);
					msgTextView.setBackgroundResource(R.drawable.msg_bg_mine);
					sendTypeView.setTextColor(getResources().getColor(
							R.color.msg_send_color));

					switch (txmsg.read_state) {
					case TXMessage.NOT_SENT:
						sendTypeView.setText(R.string.msg_wait_send);
						// msgTextView.setBackgroundResource(R.drawable.wait_pao);
						sendTypeView.setTextColor(getResources().getColor(
								R.color.msg_send_wait_color));
						break;
					case TXMessage.SENT:
						sendTypeView.setText(null);
						break;
					}
				} else {
					sendTypeView.setVisibility(View.GONE);
					rel_params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
					ll_leftlist_textMsg.setLayoutParams(rel_params);
					// msgTextView.setLayoutParams(rel_params);
					msgTextView.setBackgroundResource(R.drawable.msg_bg_other);
				}

				this.myOnLongclick.setTo(msgTextView, txmsg);

				break;
			case TxDB.MSG_TYPE_QU_IMAGE_EMS:
			case TxDB.MSG_TYPE_IMAGE_EMS:
			case TxDB.MSG_TYPE_SMS_IMG:
				holder.leftlist_type2.setVisibility(View.VISIBLE);
				FailImgView = (ImageView) holder.leftlist_type2
						.findViewById(R.id.msgroomitem_List2_failImg);
				loadingView = (ProgressBar) holder.leftlist_type2
						.findViewById(R.id.msgroomitem_List2_loading);
				myLoadingView = (ProgressBar) holder.leftlist_type2
						.findViewById(R.id.msgroomitem_RList2_loading);
				FailImgView.setVisibility(View.GONE);
				loadingView.setVisibility(View.GONE);
				myLoadingView.setVisibility(View.GONE);
				final ImageView contentImgView = (ImageView) holder.leftlist_type2
						.findViewById(R.id.msgroomitem_List2_MsgImg);
				// msgroomitem_List2_MsgImg

				LinearLayout shoudong_down_img = (LinearLayout) holder.leftlist_type2
						.findViewById(R.id.shoudong_down_img);

				contentImgView.setImageBitmap(null);// 因为布局文件中默认图片是下载失败文件。不置空会闪一下下载失败的图片
				FrameLayout fl_imageMessage = (FrameLayout) holder.leftlist_type2
						.findViewById(R.id.msgroomitem_List2_dateLayout11);

				Bitmap bitmap = null;

				if (txmsg.was_me) {
					loadingView.setVisibility(View.GONE);

					lin_params.gravity = Gravity.RIGHT;
					holder.leftlist_type2.setLayoutParams(lin_params);

					fl_imageMessage
							.setBackgroundResource(R.drawable.msg_img_bg_mine);

					if (txmsg.cacheImg != null) {
						bitmap = txmsg.cacheImg.get();
						myLoadingView.setVisibility(View.GONE);
					}

					if (bitmap == null && !txmsg.setLoadingImg()) {
						myLoadingView.setVisibility(View.VISIBLE);
						mGroupAsynloader.post(new Runnable() {// 异步加载图片
									@Override
									public void run() {
										Bitmap bitmap1 = Utils.getImgByPath(
												mSess.getContext(),
												txmsg.msg_path,
												Utils.msgroom_list_resolution);
										if (bitmap1 != null) {
											txmsg.cacheImg = new SoftReference<Bitmap>(
													bitmap1);
											flush(FLUSH_ONE_LINE, txmsg);
										}
										txmsg.clearLoadingImg();
									}
								});
					}
					contentImgView.setImageBitmap(bitmap);
					shoudong_down_img.setVisibility(View.GONE);
					myLoadingView.setVisibility(View.GONE);

					if (txmsg.updateState == TXMessage.UPDATE) {
						// 显示原图
						if (Utils.isNull(txmsg.msg_url)
								&& !Utils.isNull(txmsg.msg_path)) {
							txmsg.updateState = TXMessage.UPDATE_FAILE;
						} else {
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
					} else if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						FailImgView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATING) {
						// 显示原图
						myLoadingView.setProgress(txmsg.updateCount);
						myLoadingView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
						// 显示原图

					}

				} else {
					// 对方发的图片
					myLoadingView.setVisibility(View.GONE);
					fl_imageMessage
							.setBackgroundResource(R.drawable.msg_img_bg_other);

					lin_params.gravity = Gravity.LEFT;
					holder.leftlist_type2.setLayoutParams(lin_params);

					switch (txmsg.updateState) {
					case TXMessage.UPDATE:
						if (txmsg.cacheImg != null) {// 从缓存读图片
							bitmap = txmsg.cacheImg.get();
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
						if (bitmap == null && !txmsg.setLoadingImg()) {
							txmsg.updateState = TXMessage.UPDATING;
							mGroupAsynloader.post(new Runnable() {// 异步加载图片
										@Override
										public void run() {
											Bitmap bitmap1 = Utils.getImgByPath(
													mSess.getContext(),
													txmsg.msg_path,
													Utils.msgroom_list_resolution);
											if (bitmap1 != null) {
												txmsg.cacheImg = new SoftReference<Bitmap>(
														bitmap1);
												txmsg.updateState = TXMessage.UPDATE_OK;
											} else {
												txmsg.msg_path = "";
												ContentValues values = new ContentValues();
												values.put(
														TxDB.Messages.MSG_PATH,
														txmsg.msg_path);
												cr.update(
														TxDB.Messages.CONTENT_URI,
														values,
														TxDB.Messages.MSG_ID
																+ "=?",
														new String[] { ""
																+ txmsg.msg_id });
												if (Utils
														.isAutoDownLoadImg(mSess
																.getContext())) {
													txmsg.updateState = TXMessage.UPDATING;
													if (!Utils.checkSDCard()) {// 无SD卡
														txmsg.updateState = TXMessage.UPDATE_FAILE;
														flush(FLUSH_LISTVIEW_FALSE);
													} else {
														downloadImg(txmsg); // 转圈
													}
												} else {
													txmsg.updateState = TXMessage.UPDATE_CLICK;
												}

											}

											flush(FLUSH_ONE_LINE, txmsg);
											txmsg.clearLoadingImg();
										}
									});
						}

						break;
					case TXMessage.UPDATING:
						contentImgView.setImageBitmap(null);
						shoudong_down_img.setVisibility(View.GONE);
						loadingView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_FAILE:
						contentImgView
								.setImageResource(R.drawable.msg_img_download_failed);
						shoudong_down_img.setVisibility(View.GONE);
						FailImgView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_OK:

						if (txmsg.cacheImg != null) {// 从缓存读图片
							bitmap = txmsg.cacheImg.get();
							contentImgView.setImageBitmap(bitmap);
						}
						if (bitmap == null && !txmsg.setLoadingImg()) {
							txmsg.updateState = TXMessage.UPDATING;
							mGroupAsynloader.post(new Runnable() {// 异步加载图片
										@Override
										public void run() {
											Bitmap bitmap1 = Utils.getImgByPath(
													mSess.getContext(),
													txmsg.msg_path,
													Utils.msgroom_list_resolution);
											if (bitmap1 != null) {
												txmsg.cacheImg = new SoftReference<Bitmap>(
														bitmap1);
												txmsg.updateState = TXMessage.UPDATE_OK;
												flush(FLUSH_ONE_LINE, txmsg);
											} else {
												txmsg.updateState = TXMessage.LOAD_FAILED;
												flush(FLUSH_ONE_LINE, txmsg);
												Utils.saveTxMsgToDB(txmsg);
											}
											txmsg.clearLoadingImg();
										}
									});
						}
						shoudong_down_img.setVisibility(View.GONE);
						break;
					case TXMessage.UPDATE_CLICK:
						shoudong_down_img.setVisibility(View.VISIBLE);

						break;
					case TXMessage.LOAD_FAILED:
						contentImgView
								.setImageResource(R.drawable.msg_img_download_failed);
						shoudong_down_img.setVisibility(View.GONE);
						FailImgView.setVisibility(View.GONE);
						break;

					}
				}

				fl_imageMessage
						.setOnLongClickListener(new View.OnLongClickListener() {

							@Override
							public boolean onLongClick(View v) {
								creatListWindow(txmsg);
								return false;
							}
						});

				mImgViewOnClick2.setTo(fl_imageMessage, txmsg);

				break;

			case TxDB.MSG_TYPE_QU_AUDIO_EMS:
			case TxDB.MSG_TYPE_AUDIO_EMS:
			case TxDB.MSG_TYPE_SMS_AUDIO:

				showAudioMsgView(holder, txmsg, lin_params);

				break;
			case TxDB.MSG_TYPE_QU_CARD_EMS:
			case TxDB.MSG_TYPE_QU_TCARD_SMS:
			case TxDB.MSG_TYPE_CARD_EMS:
			case TxDB.MSG_TYPE_TCARD_SMS:

				holder.leftlist_type4.setVisibility(View.VISIBLE);
				ImageView cardHeadView = (ImageView) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_card_head);
				cardHeadView.setImageResource(R.drawable.user_infor_head_boy);
				// 名片名字
				// TextView cardName = (TextView) holder.leftlist_type4
				// .findViewById(R.id.msgroomitem_List4_tuijian);

				FailImgView = (ImageView) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_failImg);
				loadingView = (ProgressBar) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_loading);
				sendTypeView = (TextView) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_RList4_SendType);

				loadingView.setVisibility(View.GONE);
				FailImgView.setVisibility(View.GONE);
				// 其他状态文字为蓝色 气泡为正常0xfff69200
				FrameLayout paoBackLayout4 = (FrameLayout) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_Layout1);
				if (txmsg.was_me) {
					lin_params.gravity = Gravity.RIGHT;
					holder.leftlist_type4.setLayoutParams(lin_params);
					paoBackLayout4
							.setBackgroundResource(R.drawable.msg_bg_mine);

					if (txmsg.updateState == TXMessage.UPDATE) {

					} else if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						FailImgView.setVisibility(View.VISIBLE);
						sendTypeView.setText(null);
					} else if (txmsg.updateState == TXMessage.UPDATING) {
						// 显示原图
						loadingView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
						// 显示原图
					}

					sendTypeView.setTextColor(getResources().getColor(
							R.color.msg_send_color));
					switch (txmsg.read_state) {
					case TXMessage.NOT_SENT:
						sendTypeView.setText(R.string.msg_wait_send);
						// paoBackLayout4
						// .setBackgroundResource(R.drawable.wait_pao);
						sendTypeView.setTextColor(getResources().getColor(
								R.color.msg_send_wait_color));
						break;
					case TXMessage.SENT:
						sendTypeView.setText(null);
						break;
					}
					// paoBackLayout4.setPadding(Utils.dip2px(10, txdata),
					// Utils.dip2px(15, txdata), Utils.dip2px(15, txdata),
					// Utils.dip2px(15, txdata));

				} else {
					lin_params.gravity = Gravity.LEFT;
					holder.leftlist_type4.setLayoutParams(lin_params);
					sendTypeView.setVisibility(View.GONE);// 对方名片消息不显示 发送状态

					paoBackLayout4
							.setBackgroundResource(R.drawable.msg_bg_other);
					if (txmsg.updateState == TXMessage.UPDATE) {
						if (txmsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS
								|| txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
							if (!Utils.isNull(txmsg.msg_url)
									&& Utils.isNull(txmsg.msg_path)) {
								// new
								// DownContactTask(GroupMsgRoom.this,""+user_id).execute(txmsg);
								if (!Utils.checkSDCard()) {// 无SD卡
									txmsg.updateState = TXMessage.UPDATE_FAILE;
									flush(FLUSH_LISTVIEW_FALSE);
									// flush(FLUSH_ROOM_FALSE);
								} else if (!txmsg.setLoadingImg()) {
									mGroupAsynloader.post(new Runnable() {
										@Override
										public void run() {
											DownContackSocket(txmsg);
											// 需要更新
											txmsg.clearLoadingImg();
										}
									});
								}
								txmsg.updateState = TXMessage.UPDATING;
								loadingView.setVisibility(View.VISIBLE);
							} else {
								txmsg.updateState = TXMessage.UPDATE_OK;
							}
						}
					}// else
					if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						FailImgView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATING) {
						loadingView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {

					}
				}

				// dateView.setText(dealDate(txmsg.send_time * 1000));
				String name = "";
				String partnerId = "";
				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_CARD_EMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_CARD_EMS) {
					// 神聊通讯录名片
					if (Utils.debug) {
						Log.e(TAG, "加载对方发送的通讯录联系人名片---"
								+ txmsg.contacts_person_id + "-----");
					}
					name = txmsg.contacts_person_name;
					// phone = "手机:"+txmsg.partner_phone;
					name = name.trim();
					// cardName.setText(R.string.msg_card_title_content);
					// 名片头像
					readCardHeadImg(cardHeadView, -1, txmsg.tcard_sex,
							txmsg.tcard_avatar_url);
				} else if (txmsg.msg_type == TxDB.MSG_TYPE_QU_TCARD_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_TCARD_SMS) {
					// 神聊联系人名片，现在是不是只有ios有神聊通讯录名片了？
					if (Utils.debug) {
						Log.e(TAG, "加载对方发送的神聊联系人名片---" + txmsg.tcard_id
								+ "-----");
					}
					name = txmsg.tcard_name;
					partnerId = getResources().getString(
							R.string.msg_card_shenliao_code)
							+ txmsg.tcard_id;
					name = name.trim();
					// cardName.setText(R.string.msg_card_title_shenliao);
					// 名片头像
					readCardHeadImg(cardHeadView, txmsg.tcard_id,
							txmsg.tcard_sex, txmsg.tcard_avatar_url);
				}
				// 更新名片名字
				TextView NameView = (TextView) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_cardName);
				NameView.setText(name);
				// 更新名片神聊号和电话
				TextView partnerId_view = (TextView) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_cardtxId);
				if (!partnerId.equals("")) {
					partnerId_view.setText(partnerId);
					partnerId_view.setVisibility(View.VISIBLE);
				} else {
					partnerId_view.setText(null);
					partnerId_view.setVisibility(View.GONE);
				}

				// 名片点击
				LinearLayout cardLayout = (LinearLayout) holder.leftlist_type4
						.findViewById(R.id.msgroomitem_List4_Layout22);
				mCardOnClick.setTo(cardLayout, txmsg);
				myOnLongclick.setTo(cardLayout, txmsg);

				break;
			case TxDB.MSG_TYPE_QU_GEO_SMS:
			case TxDB.MSG_TYPE_GEO_SMS:
			case TxDB.MSG_TYPE_SMS_GEO:
				holder.leftlist_type5.setVisibility(View.VISIBLE);

				View ll_msg_send_state = holder.leftlist_type5
						.findViewById(R.id.ll_msg_send_state);// 发送失败和发送状态的父view

				FailImgView = (ImageView) ll_msg_send_state
						.findViewById(R.id.msgroomitem_RList5_failImg);
				sendTypeView = (TextView) ll_msg_send_state
						.findViewById(R.id.msgroomitem_RList5_SendType);
				FailImgView.setVisibility(View.GONE);

				// 定位
				final TextView msgroomitem_List5_MsgImg = (TextView) holder.leftlist_type5
						.findViewById(R.id.msgroomitem_List5_MsgImg);

				if (txmsg.was_me) {
					lin_params.gravity = Gravity.RIGHT;
					holder.leftlist_type5.setLayoutParams(lin_params);
					// 其他状态文字为蓝色 气泡为正常0xfff69200
					FrameLayout paoBackLayout5 = (FrameLayout) holder.leftlist_type5
							.findViewById(R.id.msgroomitem_List5_dateLayout11);
					paoBackLayout5
							.setBackgroundResource(R.drawable.msg_bg_mine);
					// paoBackLayout5.setPadding(Utils.dip2px(8, txdata),
					// Utils.dip2px(5, txdata), Utils.dip2px(9, txdata),
					// Utils.dip2px(5, txdata));

					if (Utils.isNull(txmsg.sns_id)) {

						if (Utils.debug)
							Log.i("bobo",
									"----------------加载了" + txmsg.toString());
						msgroomitem_List5_MsgImg.setText("正在加载...");
						mGroupAsynloader.post(new Runnable() {
							@Override
							public void run() {
								String[] address = txmsg.geo.split(",");
								txmsg.sns_id = Utils.getAddress(
										Double.parseDouble(address[0]),
										Double.parseDouble(address[1]));

								Message message = new Message();
								message.what = SET_GEO_MSG;

								Object[] o = new Object[] { txmsg.sns_id,
										msgroomitem_List5_MsgImg };
								message.obj = o;
								MsgHandler.sendMessage(message);

								for (TXMessage tx : txmsgs_list) {
									if (tx.gmid == txmsg.gmid) {
										tx.sns_id = txmsg.sns_id;
									}
								}

							}
						});
					}

					if (txmsg.updateState == TXMessage.UPDATE) {
						if (Utils.isNull(txmsg.geo)
								&& Utils.isNull(txmsg.sns_id)) {
							txmsg.updateState = TXMessage.UPDATE_FAILE;
						} else {
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
					} else if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
						// 显示原图加失败红色惊叹号
						FailImgView.setVisibility(View.VISIBLE);
					} else if (txmsg.updateState == TXMessage.UPDATING) {

					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
						msgroomitem_List5_MsgImg.setText(txmsg.sns_id);
					}

					sendTypeView.setTextColor(getResources().getColor(
							R.color.msg_send_color));
					if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_GEO) {
						// dateView.setText(dealDate(txmsg.send_time));
						switch (txmsg.read_state) {
						case TXMessage.NOT_SENT:
							if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
								sendTypeView.setVisibility(View.GONE);
							} else {
								sendTypeView.setText(R.string.msg_wait_send);
								sendTypeView.setTextColor(getResources()
										.getColor(R.color.msg_send_wait_color));
							}
							// dateView.setText(dealDate(txmsg.send_time));
							// 待发状态文字为橙色 气泡为橙色 0xff0e70a1
							// paoBackLayout5
							// .setBackgroundResource(R.drawable.wait_pao);
							// paoBackLayout5.setPadding(Utils.dip2px(5,
							// txdata),
							// Utils.dip2px(5, txdata),
							// Utils.dip2px(10, txdata),
							// Utils.dip2px(5, txdata));
							break;

						case TXMessage.UNREAD:
							sendTypeView.setText(R.string.msg_fail);
							break;
						case TXMessage.SENT:
							sendTypeView.setText(null);
							break;
						case TXMessage.READ:
							sendTypeView.setText(R.string.msg_go);
							break;
						}
					} else {
						switch (txmsg.read_state) {
						case TXMessage.NOT_SENT:
							if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
								sendTypeView.setVisibility(View.GONE);
							} else {
								sendTypeView.setText(R.string.msg_wait_send);
								sendTypeView.setTextColor(getResources()
										.getColor(R.color.msg_send_wait_color));
							}
							// 待发状态文字为橙色 气泡为橙色 0xff0e70a1
							// paoBackLayout5
							// .setBackgroundResource(R.drawable.wait_pao);
							// paoBackLayout5.setPadding(Utils.dip2px(5,
							// txdata),
							// Utils.dip2px(5, txdata),
							// Utils.dip2px(10, txdata),
							// Utils.dip2px(5, txdata));
							break;
						case TXMessage.SENT:
							sendTypeView.setText(null);
							break;
						}
					}

				} else {
					sendTypeView.setVisibility(View.GONE);
					lin_params.gravity = Gravity.LEFT;
					holder.leftlist_type5.setLayoutParams(lin_params);
					// 其他状态文字为蓝色 气泡为正常0xfff69200
					FrameLayout paoBackLayout5 = (FrameLayout) holder.leftlist_type5
							.findViewById(R.id.msgroomitem_List5_dateLayout11);
					paoBackLayout5
							.setBackgroundResource(R.drawable.msg_bg_other);
					// paoBackLayout5.setPadding(Utils.dip2px(8, txdata),
					// Utils.dip2px(5, txdata), Utils.dip2px(9, txdata),
					// Utils.dip2px(5, txdata));

					switch (txmsg.updateState) {
					case TXMessage.UPDATE:
						if (txmsg.sns_id != null) {// 从缓存读图片
							msgroomitem_List5_MsgImg.setText(txmsg.sns_id);
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
						if (Utils.isNull(txmsg.sns_id)) {
							if (Utils.debug)
								Log.i("bobo", "----------------加载了2222");
							mGroupAsynloader.post(new Runnable() {// 异步加载图片
										@Override
										public void run() {
											String[] address = txmsg.geo
													.split(",");
											txmsg.sns_id = Utils.getAddress(
													Double.parseDouble(address[0]),
													Double.parseDouble(address[1]));
											Message message = new Message();
											message.what = SET_GEO_MSG;
											Object[] o = new Object[] {
													txmsg.sns_id,
													msgroomitem_List5_MsgImg };
											message.obj = o;
											MsgHandler.sendMessage(message);

											for (TXMessage tx : txmsgs_list) {
												if (tx.gmid == txmsg.gmid) {
													tx.sns_id = txmsg.sns_id;
												}
											}
										}
									});
						}

						break;
					case TXMessage.UPDATING:
						msgroomitem_List5_MsgImg.setText("正在加载...");
						break;
					case TXMessage.UPDATE_FAILE:
						FailImgView.setVisibility(View.VISIBLE);
						break;
					case TXMessage.UPDATE_OK:

						if (txmsg.sns_id != null) {
							msgroomitem_List5_MsgImg.setText(txmsg.sns_id);
							txmsg.updateState = TXMessage.UPDATE_OK;
						}
						msgroomitem_List5_MsgImg.setText(txmsg.sns_id);
						break;
					}
				}

				mGeoImgOnClick.setTo(msgroomitem_List5_MsgImg, txmsg);
				this.myOnLongclick.setTo(msgroomitem_List5_MsgImg, txmsg);
				break;
			case TxDB.MSG_TYPE_BIG_FILE_SMS:
			case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
				// 大文件消息
				holder.leftlist_type6.setVisibility(View.VISIBLE);
				RelativeLayout rl_content = (RelativeLayout) holder.leftlist_type6
						.findViewById(R.id.rl_big_file_message);
				View upload_progress = holder.leftlist_type6
						.findViewById(R.id.rl_upload_progress);
				View v_trnasfer_failedImage = holder.leftlist_type6
						.findViewById(R.id.iv_failed_image);// 上传下载失败的叹号

				ProgressBar pd_uploading = (ProgressBar) upload_progress
						.findViewById(R.id.pb_big_file_uploading);
				ImageView iv_stopUpload = (ImageView) upload_progress
						.findViewById(R.id.iv_stop_upload);
				iv_stopUpload.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 点击停止上传，删除消息
						Utils.creatDialog(thisContext, "取消上传操作？",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// 确定按钮
										deleteTxMsg(dialog, txmsg);
									}
								}, new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										dialog.dismiss();
									}
								});

					}
				});
				View v_download_progress = holder.leftlist_type6
						.findViewById(R.id.ll_download_progress);
				ProgressBar pd_downloading = (ProgressBar) v_download_progress
						.findViewById(R.id.pb_big_file_downloading);
				ImageView iv_stopDownload = (ImageView) v_download_progress
						.findViewById(R.id.iv_stop_download);
				iv_stopDownload.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {
						// 点击开始或停止下载
						if (txmsg.updateState == TXMessage.UPDATING) {
							// 正在下载，点击停止下载任务
							getDownUpMgr().removeDownloadBigTask(
									getDownUpMgr().getDownloadTaskId(
											txmsg.msg_url), false);
							txmsg.updateState = TXMessage.UPDATE;
							Utils.saveTxMsgToDB(txmsg);
						} else {
							// 开始下载（下载完成后，下载按钮会隐藏）
							// Utils.downloadBigFile(txdata, txmsg, null,
							// mCurrentMsgRoom.MsgHandler);
							downloadBigFile(txmsg);
							// TODO直接置为下载中状态，防止出现无网络点击下载按钮没反应，多个下载任务添加到队列的bug，后期还是要找多个重复任务添加到队列的问题所在
							txmsg.updateState = TXMessage.UPDATING;
							Utils.saveTxMsgToDB(txmsg);// 如果不存数据库，那么退出聊天室再进入，消息状态不会是正在下载，也不会显示下载进度条

						}
						flush(FLUSH_ONE_LINE, txmsg);
					}
				});
				TextView tv_fileName = (TextView) rl_content
						.findViewById(R.id.tv_big_file_name);
				ImageView iv_fileThumb = (ImageView) rl_content
						.findViewById(R.id.iv_big_file_thumb);
				TextView tv_fileLength = (TextView) rl_content
						.findViewById(R.id.tv_big_file_length);
				tv_fileLength.setText(Utils
						.formatFileLength(txmsg.msg_file_length));
				String fileName = Utils.getLocalFileName(txmsg.msg_path);
				if (Utils.debug)
					Log.i(TAG, "大文件的文件名为：" + fileName);
				tv_fileName.setText(fileName);

				iv_fileThumb.setImageResource(Utils.getBigFileThumb(fileName,
						txmsg.updateState));// 设置大文件消息的缩略图

				String file_etx = fileName
						.substring(fileName.lastIndexOf(".") + 1);
				if (file_etx.equalsIgnoreCase("jpg")
						|| file_etx.equalsIgnoreCase("png")) {
					// 只有jpg和png格式的图片文件需要显示缩略图
					final File imageFile = new File(txmsg.msg_path);
					if (imageFile.exists() && imageFile.length() != 0) {
						Bitmap thumb = mThumbnail.isBitmapCached(imageFile
								.getPath());

						if (thumb == null) {
							new AsyncTask<String, Void, Void>() {

								@Override
								protected Void doInBackground(String... params) {
									Bitmap bm = Utils.getImageThumbnail(
											params[0], 52, 52);
									mThumbnail.cacheThumbBitmap(params[0], bm);
									return null;
								}

								@Override
								protected void onPostExecute(Void result) {
									// 获取bitmap成功后
									flush(FLUSH_ONE_LINE, txmsg);
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

				if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
					v_trnasfer_failedImage.setVisibility(View.VISIBLE);
				} else {
					v_trnasfer_failedImage.setVisibility(View.GONE);
				}

				if (txmsg.was_me) {
					if (Utils.debug)
						Log.e(TAG, "我发的大文件消息");
					// 其他状态文字为蓝色 气泡为正常0xfff69200
					rel_params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
					rl_content.setLayoutParams(rel_params);
					rl_content.setBackgroundResource(R.drawable.msg_bg_mine);
					v_download_progress.setVisibility(View.GONE);
					if (txmsg.read_state == TXMessage.NOT_SENT) {
						upload_progress.setVisibility(View.VISIBLE);
						pd_uploading.setProgress(txmsg.updateCount);
						if (Utils.debug)
							Log.e(TAG, "刷新了上传的进度条：" + txmsg.updateCount);
						// rl_content.setBackgroundResource(R.drawable.wait_pao);
					} else {
						upload_progress.setVisibility(View.GONE);
						if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
							// 修改发送大文件失败时错误叹号的图标位置
							RelativeLayout.LayoutParams failed_params = new RelativeLayout.LayoutParams(
									LayoutParams.WRAP_CONTENT,
									LayoutParams.WRAP_CONTENT);
							failed_params.addRule(RelativeLayout.LEFT_OF,
									R.id.rl_big_file_message);
							v_download_progress.setVisibility(View.GONE);// 下载失败状态，隐藏下载按钮，等用户长按消息点击“重新下载”再去显示下载按钮
							v_trnasfer_failedImage
									.setLayoutParams(failed_params);
							v_trnasfer_failedImage.setVisibility(View.VISIBLE);
						}
					}

				} else {
					if (Utils.debug)
						Log.e(TAG, "别人发的大文件消息，我收到的");
					rel_params.addRule(RelativeLayout.RIGHT_OF,
							R.id.iv_failed_image);// 在下载失败图标的按钮
					rl_content.setLayoutParams(rel_params);
					rl_content.setBackgroundResource(R.drawable.msg_bg_other);
					v_download_progress.setVisibility(View.VISIBLE);
					upload_progress.setVisibility(View.GONE);

					if (Utils.debug)
						Log.e(TAG, "该消息的下载状态，txmsg.updateState = "
								+ txmsg.updateState + ",fileName = " + fileName);

					if (txmsg.updateState == TXMessage.UPDATING) {
						// 正在下载状态
						pd_downloading.setVisibility(View.VISIBLE);
						pd_downloading.setProgress(txmsg.updateCount);
						iv_stopDownload
								.setImageResource(R.drawable.msgroom_stop_down_upload);
					} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
						v_download_progress.setVisibility(View.GONE);
					} else if (txmsg.updateState == TXMessage.UPDATE
							|| txmsg.updateState == TXMessage.UPDATE_FAILE
							|| txmsg.updateState == TXMessage.UPDATE_CLICK) {
						// 待下载或下载失败状态
						pd_downloading.setVisibility(View.GONE);
						iv_stopDownload
								.setImageResource(R.drawable.msgroom_start_download);
					} else {
						// 其他状态
						if (Utils.debug)
							Log.e(TAG, "附件的上传下载状态又出问题了，txmsg.updateState = "
									+ txmsg.updateState + ",fileName = "
									+ fileName);
					}
				}
				rl_content.setOnClickListener(new View.OnClickListener() {

					@Override
					public void onClick(View v) {

						if (txmsg.updateState == TXMessage.UPDATE
								&& txmsg.was_me) {
							showToast("找不到文件路径");

						} else if (txmsg.updateState == TXMessage.UPDATING) {
							if (Utils.debug) {
								showToast("文件没有传输完毕");
							}

						} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
							if (Utils.debug)
								showToast("点击查看大文件");
							if (Utils.openBigFile(thisContext, txmsg) == -1) {
								// 单条刷新，显示吐司
								flush(FLUSH_ONE_LINE, txmsg);
								showToast("该文件不存在，请重新下载");
							}
						}
					}
				});
				this.myOnLongclick.setTo(rl_content, txmsg);

				break;
			}

			// 消息发送者头像加载
			if (txmsg.was_me) {
				rl_msgHeadView.setVisibility(View.INVISIBLE);
				holder.headView.setImageBitmap(null);
				sendName.setVisibility(View.GONE);

				ll_my_head_icon.setVisibility(View.VISIBLE);
				readHeadImg(holder.myHeadView, TX.tm.getUserID(), TX.tm
						.getTxMe().getSex(), TX.tm.getTxMe().avatar_url, true);
			} else {
				ll_my_head_icon.setVisibility(View.INVISIBLE);
				holder.myHeadView.setImageBitmap(null);

				rl_msgHeadView.setVisibility(View.VISIBLE);
				// 头像
				readHeadImg(holder.headView, txmsg.partner_id, txmsg.sex,
						txmsg.avatar_url, false);
				setHeadViewOnClickEvent(holder.headView, txmsg);

				String sendNick = null;
				if (!Utils.isNull(String.valueOf(txmsg.partner_id))) {
					TX tx = TX.tm.getTx(txmsg.partner_id, mTXAsyncWeakRef);
					if (tx != null && !Utils.isNull(tx.getRemarkName()))
						sendNick = tx.getRemarkName();
					else if (!Utils.isNull(txmsg.partner_name))
						sendNick = txmsg.partner_name;
					else if (tx != null && !Utils.isNull(tx.getNick_name()))
						sendNick = tx.getNick_name();
					else if (Utils.isIdValid(txmsg.partner_id))
						sendNick = String.valueOf(txmsg.partner_id);
					else
						sendNick = "";

					sendName.setMaxEms(BaseMsgRoom.nickNameMaxNum);
					sendName.setText(smileyParser.addSmileySpans(sendNick,
							true, BaseMsgRoom.nickNameMaxNum));
					sendName.setTextColor(getResources()
							.getColor(
									txmsg.sex == TX.FEMAL_SEX ? R.color.msgroom_nick_name_female
											: R.color.msgroom_nick_name_male));
				}
				sendName.setVisibility(View.VISIBLE);
			}

			return dateView;
		}

		private void showAudioMsgView(ViewHolder holder, final TXMessage txmsg,
				LinearLayout.LayoutParams lin_params) {
			ImageView FailImgView;
			holder.leftlist_type3.setVisibility(View.VISIBLE);
			WaitAdiouAnimation playRecord = (WaitAdiouAnimation) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_dateLayout178);
			// playRecord.stopAdiouPlayAn(txmsg.was_me);
			playRecordAn = (PlayAdiouAnimation) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_PlayRecordImg);
			playRecordAn.setVisibility(View.VISIBLE);
			sendTypeView = (TextView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_RList3_SendTypeText);

			// 是否已经播放过
			final ImageView readImageView = (ImageView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_PlayType);
			final ImageView iv_praise = (ImageView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_Praise);// 赞按钮
			//

			iv_praise.setOnClickListener(new View.OnClickListener() {

				@Override
				public void onClick(View v) {
					if (!txmsg.isCanBePraise()) {
						// 超过10分钟，不可取消赞（超时后，赞按钮不显示，所以不会是赞点击操作）
						if (Utils.debug) {
							showToast("取消赞超时");
						}
						return;
					}
					if (txmsg.praisedState == PraiseNotice.ACTION_NONE) {
						// 状态是没有被赞，执行赞
						PraisedAnim animp = new PraisedAnim(iv_praise);
						animp.start();
					}
					mSess.getPraiseNoticeDao().praiseMsg(txGroup.group_id,
							txmsg.partner_id, txmsg.gmid, txmsg.praisedState);
				}
			});

			FailImgView = (ImageView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_failImg);
			FailImgView.setVisibility(View.GONE);
			sendTypeView.setVisibility(View.INVISIBLE);

			if (Utils.debug)
				Log.i(TAG, "刚开始：txmsg.msg_id:" + txmsg.msg_id + "txmsg.was_me:"
						+ txmsg.was_me + ",txmsg.updateState:"
						+ txmsg.updateState);

			TextView tv_otherAudioLength = (TextView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_List3_recordTime);// 对方音频时长度

			TextView tv_myAudioLength = (TextView) holder.leftlist_type3
					.findViewById(R.id.msgroomitem_RList3_recordTime);// 我的音频时长度

			LinearLayout shoudong_down_adiuo = (LinearLayout) holder.leftlist_type3
					.findViewById(R.id.shoudong_down_adiuo);// 非自动下载

			shoudong_down_adiuo.setVisibility(View.GONE);

			setPraiseBtnStyle(iv_praise, txmsg);

			if (txmsg.was_me) {
				lin_params.gravity = Gravity.RIGHT;
				holder.leftlist_type3.setLayoutParams(lin_params);
				playRecord.setBackgroundResource(R.drawable.msg_bg_mine);

				// 显示我发送的音频时长和发送状态
				// ll_sendTime.setVisibility(View.VISIBLE);
				tv_myAudioLength.setVisibility(View.VISIBLE);
				tv_otherAudioLength.setVisibility(View.GONE);

				tv_myAudioLength.setText(MessageUtil
						.getRecordTime(txmsg.audio_times));
				// mVoiceTimeOnClick.setTo(tv_myAudioLength, txmsg);

				if (txmsg.updateState == TXMessage.UPDATE) {

					if (Utils.isNull(txmsg.msg_url)
							&& !Utils.isNull(txmsg.msg_path)) {
						txmsg.updateState = TXMessage.UPDATING;
					} else {
						txmsg.updateState = TXMessage.UPDATE_OK;
					}

					if (Utils.debug)
						Log.i(TAG, "111：txmsg.msg_id:" + txmsg.msg_id
								+ "txmsg.was_me:" + txmsg.was_me
								+ ",txmsg.updateState:" + txmsg.updateState);

					if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_AUDIO) {

					} else if (Utils.isNull(txmsg.msg_url)) {
						txmsg.updateState = TXMessage.UPDATE_FAILE;
						flush(FLUSH_LISTVIEW_FALSE);

						if (Utils.debug)
							Log.i(TAG, "222：txmsg.msg_id:" + txmsg.msg_id
									+ "txmsg.was_me:" + txmsg.was_me
									+ ",txmsg.updateState:" + txmsg.updateState);
					}

				} else if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
					// 显示原图加失败红色惊叹号
					FailImgView.setVisibility(View.VISIBLE);
					readImageView.setVisibility(View.GONE);
					sendTypeView.setText(null);
				} else if (txmsg.updateState == TXMessage.UPDATING) {
					// loadingView.setVisibility(View.VISIBLE);
				} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
					playRecord.stopAdiouPlayAn(txmsg.was_me);
				}

				if (Utils.debug)
					Log.i(TAG, "333：txmsg.msg_id:" + txmsg.msg_id
							+ "txmsg.was_me:" + txmsg.was_me
							+ ",txmsg.updateState:" + txmsg.updateState);

				if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_AUDIO) {
				} else {
					switch (txmsg.read_state) {
					case TXMessage.NOT_SENT:
						sendTypeView.setText(R.string.msg_wait_send);
						// sendTypeView.setTextColor(getResources().getColor(
						// R.color.msg_send_wait_color));
						// playRecord.startAdiouPlayAn(txmsg.was_me);//TODO
						// 加上这个发送动画，语音背景会变很高。不知道为什么,待解决 2014.04.11 shc
						playRecordAn.setVisibility(View.GONE);
						break;
					case TXMessage.SENT:
						sendTypeView.setText(null);
						break;
					}
				}

				// 播放音频
				if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
					sendTypeView.setText(null);
				}
				if (txmsg != mAudioRecPlayer.getPlayingMsg()) {
					playRecordAn.stopAdiouPlayAn(txmsg.was_me);
				} else if (txmsg.PlayAudio == RECORD_PLAY) {
					playRecordAn.startAdiouPlayAn(txmsg.was_me);
				}
				if (txmsg.updateState != TXMessage.UPDATING
						&& txmsg.updateState != TXMessage.UPDATE_FAILE) {

					// mPlayRecOnClick.setTo(playRecord,
					// holder);//用mImgViewOnClick2，合并成一个 2014.05.12 shc
					mImgViewOnClick2.setTo(playRecord, holder.txmsg);
				} else {
					playRecord.setOnClickListener(null);
				}

				// this.myOnLongclick2.setTo(playRecord, holder);
				this.myOnLongclick.setTo(playRecord, txmsg);

				readImageView.setVisibility(View.GONE);

			} else {
				lin_params.gravity = Gravity.LEFT;
				holder.leftlist_type3.setLayoutParams(lin_params);
				playRecord.setBackgroundResource(R.drawable.msg_bg_other);
				sendTypeView.setVisibility(View.GONE);
				// shoudong_down_adiuo.setVisibility(View.GONE);
				// ll_sendTime.setVisibility(View.GONE);

				tv_otherAudioLength.setText(MessageUtil
						.getRecordTime(txmsg.audio_times));
				tv_otherAudioLength.setVisibility(View.GONE);
				tv_myAudioLength.setVisibility(View.GONE);
				// mVoiceTimeOnClick.setTo(tv_otherAudioLength, txmsg);

				if (txmsg.updateState == TXMessage.UPDATE) {
					if (Utils.isNull(txmsg.msg_path)) {
						if (!Utils.checkSDCard()) {// 无SD卡
							txmsg.updateState = TXMessage.UPDATE_FAILE;
							flush(FLUSH_LISTVIEW_FALSE);

							if (Utils.debug)
								Log.i(TAG, "666刷新view：txmsg.msg_id:"
										+ txmsg.msg_id + "txmsg.was_me:"
										+ txmsg.was_me + ",txmsg.updateState:"
										+ txmsg.updateState);
						} else {
							if (Utils.isAutoDownLoadAdiou(mSess.getContext())) {
								// 自动下载音频的状态
								shoudong_down_adiuo.setVisibility(View.GONE);
								txmsg.updateState = TXMessage.UPDATING;// 先设置下载中状态，否则下载失败的状态会有错
								DownAduioScoket(txmsg);
								// 转圈
								playRecord.startAdiouPlayAn(txmsg.was_me);
								playRecordAn.setVisibility(View.GONE);
							} else {
								txmsg.updateState = TXMessage.UPDATE;
								// playRecord.setBackgroundResource(R.drawable.msgroom_click_download_adiou);

								shoudong_down_adiuo.setVisibility(View.VISIBLE);
								tv_otherAudioLength.setVisibility(View.GONE);
								playRecordAn.setVisibility(View.GONE);
							}

							if (Utils.debug)
								Log.i(TAG, "777view：txmsg.msg_id:"
										+ txmsg.msg_id + "txmsg.was_me:"
										+ txmsg.was_me + ",txmsg.updateState:"
										+ txmsg.updateState);
						}
					} else {
						txmsg.updateState = TXMessage.UPDATE_OK;
						if (Utils.debug)
							Log.i(TAG, "888：txmsg.msg_id:" + txmsg.msg_id
									+ "txmsg.was_me:" + txmsg.was_me
									+ ",txmsg.updateState:" + txmsg.updateState);

					}
				}// else

				int visiblity = txmsg.read_state == TXMessage.READ ? View.GONE
						: View.VISIBLE;
				if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
					// 显示失败红色惊叹号,隐藏未读标记;
					FailImgView.setVisibility(View.VISIBLE);
					visiblity = View.GONE;
					playRecordAn.setVisibility(View.GONE);

				} else if (txmsg.updateState == TXMessage.UPDATING) {
					playRecord.startAdiouPlayAn(txmsg.was_me);
					playRecordAn.setVisibility(View.GONE);

				} else if (txmsg.updateState == TXMessage.UPDATE_OK) {
					// 显示下载完成显示音频时长
					playRecord.stopAdiouPlayAn(txmsg.was_me);
					tv_otherAudioLength.setVisibility(View.VISIBLE);
				}

				if (Utils.debug)
					Log.i(TAG, "999：txmsg.msg_id:" + txmsg.msg_id
							+ "txmsg.was_me:" + txmsg.was_me
							+ ",txmsg.updateState:" + txmsg.updateState);

				// 播放音频
				if (txmsg.updateState != TXMessage.UPDATING
						&& txmsg.updateState != TXMessage.UPDATE_FAILE) {
					if (txmsg != mAudioRecPlayer.getPlayingMsg()) {
						playRecordAn.stopAdiouPlayAn(txmsg.was_me);
					} else {
						playRecordAn.startAdiouPlayAn(txmsg.was_me);
					}
				}

				readImageView.setVisibility(visiblity);
				this.myOnLongclick.setTo(playRecord, txmsg);
				this.myOnLongclick.setTo(shoudong_down_adiuo, txmsg);

			}
			mImgViewOnClick2.setTo(playRecord, txmsg);

			if (Utils.debug)
				Log.i(TAG, "txmsg.msg_id:" + txmsg.msg_id + "txmsg.was_me:"
						+ txmsg.was_me + ",txmsg.updateState:"
						+ txmsg.updateState);
			if (txmsg.was_me
					|| (!txmsg.was_me && txmsg.updateState == TXMessage.UPDATE_OK)
					|| (!txmsg.was_me && txmsg.updateState == TXMessage.UPDATE_FAILE)) {
				// 对方的消息，并且在上传下载状态下，不更新背景长度
				// 更新时长
				// RelativeLayout.LayoutParams lp = new
				// RelativeLayout.LayoutParams(
				// RelativeLayout.LayoutParams.WRAP_CONTENT,
				// RelativeLayout.LayoutParams.WRAP_CONTENT);

				if (Utils.debug)
					Log.i(TAG, "下载完成 txmsg.msg_id:" + txmsg.msg_id
							+ "txmsg.was_me:" + txmsg.was_me
							+ ",txmsg.updateState:" + txmsg.updateState);

				if (Utils.debug)
					Log.i(TAG, "------------------------++++++++++"
							+ txmsg.updateState);

				if (!txmsg.was_me
						&& txmsg.updateState == TXMessage.UPDATE_FAILE) {
					// 对方的语音消息接受失败
					tv_otherAudioLength.setVisibility(View.INVISIBLE);
					txmsg.audio_times = 5;
				}

				// long timeWidth = 100 + txmsg.audio_times * 10;
				long timeWidth = 30 + txmsg.audio_times * 10;
				int jianWidth = 170 * display.getWidth() / 480;
				if (timeWidth > display.getWidth() - jianWidth) {
					timeWidth = display.getWidth() - jianWidth;
				}
				// lp = new RelativeLayout.LayoutParams((int) (timeWidth),
				// RelativeLayout.LayoutParams.WRAP_CONTENT);
				// lp.addRule(RelativeLayout.RIGHT_OF,
				// R.id.ll_audio_message_left);
				//
				// playRecord.setLayoutParams(lp);

				if (txmsg.was_me) {
					tv_myAudioLength.setWidth((int) timeWidth);
				} else {
					tv_otherAudioLength.setWidth((int) timeWidth);
				}

			}
		}

		/** 设置赞按钮的样式 */
		private void setPraiseBtnStyle(ImageView praiseBtn, TXMessage txmsg) {
			if (txmsg.was_me) {
				praiseBtn.setVisibility(View.GONE);
			} else {
				praiseBtn.setVisibility(View.INVISIBLE);
				if (txGroup != null && txGroup.isOfficialGroup()) {
					// 是公开聊天室

					if (txmsg.praisedState == PraiseNotice.ACTION_PRAISE) {
						// 赞过
						praiseBtn.setVisibility(View.VISIBLE);
						praiseBtn.setImageResource(R.drawable.state_praised);
					} else if (txmsg.praisedState == PraiseNotice.ACTION_NONE) {
						// 没有被赞过
						if (txmsg.isCanBePraise()) {
							// 该消息可以被赞或者取消赞
							// 和判断是否正在播放的条件一样
							if (txmsg == mAudioRecPlayer.getPlayingMsg()
									&& txmsg.PlayAudio == RECORD_PLAY) {
								// 消息播放中且可以被赞
								praiseBtn.setVisibility(View.VISIBLE);
								praiseBtn
										.setImageResource(R.drawable.state_unpraised);
							}
						}

					}

					// if(txmsg.isCanBePraise()){
					// //该消息可以被赞或者取消赞
					// if(txmsg.praisedState == PraiseNotice.ACTION_NONE){
					// //没有被赞过
					// //和判断是否正在播放的条件一样
					// if (txmsg ==
					// mAudioRecPlayer.getPlayingMsg()&&txmsg.PlayAudio ==
					// RECORD_PLAY) {
					// //消息播放中且可以被赞
					// praiseBtn.setVisibility(View.VISIBLE);
					// praiseBtn.setImageResource(R.drawable.state_unpraised);
					// }
					// }else if (txmsg.praisedState ==
					// PraiseNotice.ACTION_PRAISE) {
					// //赞过
					// praiseBtn.setVisibility(View.VISIBLE);
					// praiseBtn.setImageResource(R.drawable.state_praised);
					// }
					// }
				}
			}
		}

		Handler mTXAsyncNotify = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case SessionManager.TXManager.TxDBAsyncRead: {
					TX tx = (TX) msg.obj;
					if (tx != null) {
						for (ViewHolder vh : mViewLines) {
							if (vh.txmsg.partner_id == tx.partner_id)
								updateView(vh);
						}
					}
				}
					break;
				}
			}
		};
		WeakReference<Handler> mTXAsyncWeakRef = new WeakReference<Handler>(
				mTXAsyncNotify);

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater
						.inflate(R.layout.msgroom_mainlist, null);
				holder = new ViewHolder();
				holder.tv_msgTime = (TextView) convertView
						.findViewById(R.id.msgroomitem_List1_Date);
				holder.rl_msgHeadView = (RelativeLayout) convertView
						.findViewById(R.id.rl_msgroom_head);
				holder.ll_my_head_icon = (LinearLayout) convertView
						.findViewById(R.id.ll_my_head_icon);
				holder.tv_msgSendName = (TextView) convertView
						.findViewById(R.id.msgroomitem_List1_Name);

				holder.leftlist_type1 = convertView
						.findViewById(R.id.msgroom_leftlist_type1);
				holder.leftlist_type2 = convertView
						.findViewById(R.id.msgroom_leftlist_type2);
				holder.leftlist_type3 = convertView
						.findViewById(R.id.msgroom_leftlist_type3);
				holder.leftlist_type4 = convertView
						.findViewById(R.id.msgroom_leftlist_type4);
				holder.leftlist_type5 = convertView
						.findViewById(R.id.msgroom_leftlist_type5);
				holder.leftlist_type6 = convertView
						.findViewById(R.id.rl_msglist_msg_big_file);
				holder.list_type10 = convertView
						.findViewById(R.id.msgroom_list_type10);
				holder.list_type12 = convertView
						.findViewById(R.id.msgroom_list_type12);// 群里面没有type12，XXX进入聊天室
				holder.leftlist_type13 = convertView
						.findViewById(R.id.msgroom_leftlist_type13);
				holder.last_week_starts = convertView
						.findViewById(R.id.ll_lastWeekStars);// 显示本期之星的条目
				holder.gv_last_week_stars = (SlGridView) holder.last_week_starts
						.findViewById(R.id.gv_lastWeekStars);

				holder.leftlist_type1.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type2.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type3.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type4.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type5.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type6.setOnTouchListener(BaseMsgRoom.this);
				holder.list_type10.setOnTouchListener(BaseMsgRoom.this);
				holder.leftlist_type13.setOnTouchListener(BaseMsgRoom.this);

				convertView.setTag(holder);
				mViewLines.add(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			holder.tv_msgTime.setVisibility(View.GONE);
			holder.rl_msgHeadView.setVisibility(View.GONE);
			holder.ll_my_head_icon.setVisibility(View.GONE);
			holder.tv_msgSendName.setVisibility(View.GONE);

			holder.leftlist_type1.setVisibility(View.GONE);
			holder.leftlist_type2.setVisibility(View.GONE);
			holder.leftlist_type3.setVisibility(View.GONE);
			holder.leftlist_type4.setVisibility(View.GONE);
			holder.leftlist_type5.setVisibility(View.GONE);
			holder.leftlist_type6.setVisibility(View.GONE);
			// holder.rightlist_type1.setVisibility(View.GONE);
			// holder.rightlist_type2.setVisibility(View.GONE);
			// holder.rightlist_type3.setVisibility(View.GONE);
			// holder.rightlist_type4.setVisibility(View.GONE);
			// holder.rightlist_type5.setVisibility(View.GONE);
			holder.list_type10.setVisibility(View.GONE);
			holder.list_type12.setVisibility(View.GONE);
			holder.leftlist_type13.setVisibility(View.GONE);

			holder.last_week_starts.setVisibility(View.GONE);

			if (mThumbnail == null)
				mThumbnail = new ThumbnailCreator();

			if (receivedFolderPath == null) {
				// 创建已接受文件目录
				receivedFolderPath = getDownUpMgr().getStoragePath()
						+ File.separator + FileTransfer.RECEIVE_FILE_FOLDER;
				Utils.creatFolder(receivedFolderPath);
			}

			if (mDataSource == null) {
				// 目录下面的所有的文件路径
				mDataSource = new ArrayList<String>();
				// 遍历目录下面的所有文件
				new File(receivedFolderPath).listFiles(new FilenameFilter() {

					@Override
					public boolean accept(File dir, String filename) {
						// 添加文件名到目录
						mDataSource.add(filename);
						return false;
					}
				});
			}

			if (txmsgs_list != null) {
				TXMessage txmsg = txmsgs_list.get(position);
				holder.txmsg = txmsg;

				TextView dateView = updateView(holder);
				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_NOTICE_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_PROMPT_SMS
						|| txmsg.msg_type == TxDB.MSG_TYPE_QU_LASK_WEEK_STARTS_SMS) {
					// 群提示消息和上周之星消息，日期一律不显示
					dateView.setVisibility(View.GONE);
				} else if (dateView != null) {
					int vis = View.VISIBLE;
					if (position - 1 >= 0) {
						TXMessage txMsg = txmsgs_list.get(position - 1);
						if ((txmsg.send_time - txMsg.send_time) < 15 * 60) {
							vis = View.GONE;
						}
					}
					dateView.setVisibility(vis);
				}
			}

			return convertView;
		}
	}

	// 通过intent读取数据
	protected long groupid;
	public TxGroup txGroup;

	// 长按某条消息弹出的窗口
	public void creatListWindow(final TXMessage txmsg) {
		final String copy = this.getResources().getString(
				R.string.chatroom_longclick_copy);
		final String del = this.getResources().getString(
				R.string.delete_confirm);
		final String forword_by_tb = this.getResources().getString(
				R.string.chatroom_item_forword_by_tb);
		final String forword_by_sms = this.getResources().getString(
				R.string.chatroom_item_forword_by_sms);
		final String forword = this.getResources().getString(
				R.string.chatroom_item_forword);
		final String resend = this.getResources().getString(
				R.string.chatroom_item_resend);
		final String redown = getResources().getString(
				R.string.chatroom_item_redown);
		final String report = getResources().getString(
				R.string.chatroom_item_report);

		String[] items = { del, forword_by_tb, forword_by_sms, report };

		if (txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS
				|| txmsg.msg_type == TxDB.MSG_TYPE_COMMON_SMS
				|| txmsg.msg_type == TxDB.MSG_TYPE_SMS_SMS) {
			if (txmsg.updateState == TXMessage.UPDATE_FAILE) {
				if (txmsg.was_me) {
					items = new String[] { resend, copy, del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { copy, del };
					} else {
						items = new String[] { copy, del, report };
					}

				}
			} else if (txmsg.updateState == TXMessage.UPDATE_OK
					|| txmsg.updateState == TXMessage.UPDATE) {
				if (txmsg.was_me) {
					items = new String[] { forword, copy, del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { forword, copy, del };
					} else {
						items = new String[] { forword, copy, del, report };
					}

				}

			} else if (txmsg.updateState == TXMessage.UPDATING) {
				if (txmsg.was_me) {
					items = new String[] { copy, del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { copy, del };
					} else {
						items = new String[] { copy, del, report };
					}
				}
			}
		} else {
			if (txmsg.updateState == TXMessage.UPDATE_FAILE) {

				if (txmsg.was_me) {
					items = new String[] { resend, del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { redown, del };
					} else {
						items = new String[] { redown, del, report };
					}
				}
			} else if (txmsg.updateState == TXMessage.UPDATE_OK
					|| txmsg.updateState == TXMessage.UPDATE
					|| txmsg.updateState == TXMessage.UPDATE_CLICK) {
				if (txmsg.was_me) {
					items = new String[] { forword, del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { forword, del };
					} else {
						items = new String[] { forword, del, report };
					}

				}

				if (txmsg.msg_type == TxDB.MSG_TYPE_QU_TCARD_SMS) {
					if (txmsg.was_me) {
						items = new String[] { del };
					} else {
						if (TxData.isOP()
								&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
							items = new String[] { del };
						} else {
							items = new String[] { del, report };
						}

					}
				}
			} else if (txmsg.updateState == TXMessage.UPDATING) {
				if (txmsg.was_me) {
					items = new String[] { del };
				} else {
					if (TxData.isOP()
							&& txmsg.msg_type == TxDB.MSG_TYPE_QU_COMMON_SMS) {
						items = new String[] { del };
					} else {
						items = new String[] { del, report };
					}
				}
			}

			if (txmsg.msg_type == TxDB.MSG_TYPE_BIG_FILE_SMS
					|| txmsg.msg_type == TxDB.MSG_TYPE_QU_BIG_FILE_SMS) {
				// 如果是大文件消息，则去掉【转发】选项

				List<String> itemList = new ArrayList<String>();
				for (int i = 0; i < items.length; i++) {
					if (!items[i].equals(forword)) {
						// 大文件消息去掉转发选项
						itemList.add(items[i]);
					}
				}
				items = new String[itemList.size()];
				itemList.toArray(items);
			}

			if (txmsg.msg_type == TxDB.MSG_TYPE_SMS_GIF
					|| txmsg.msg_type == TxDB.MSG_TYPE_QU_GIF_SMS) {
				// TODO GIF的长按
				List<String> itemList = new ArrayList<String>();
				for (int i = 0; i < items.length; i++) {
					if (!items[i].equals(forword)) {
						// 大文件消息去掉转发选项
						itemList.add(items[i]);
					}
				}
				items = new String[itemList.size()];
				itemList.toArray(items);
			}

		}
		final String[] fitems = items;

		Utils.creatDialogMenu(thisContext, fitems,
				getResources().getString(R.string.chatroom_longclick_titile),
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						if (Utils.debug) {
							Log.i(TAG, "item-->" + fitems[which]);
						}
						if (fitems[which].equals(copy)) {
							ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							String clip = txmsg.msg_body;
							clipboard.setText(clip);
						} else if (fitems[which].equals(resend)) {
							txmsg.updateState = TXMessage.UPDATE;
							txmsg.read_state = TXMessage.NOT_SENT;
							switch (txmsg.msg_type) {
							case TxDB.MSG_TYPE_QU_GEO_SMS:
							case TxDB.MSG_TYPE_GEO_SMS:
								if (isLocationing) {
									Toast.makeText(mSess.getContext(),
											R.string.msgroom_locationing_toast,
											Toast.LENGTH_SHORT).show();
									return;
								}
								ls = LocationStation.getInstance(mSess
										.getContext());
								ls.getLocation(mSess.getContext(), 30000);
								isLocationing = true;
								break;
							case TxDB.MSG_TYPE_QU_IMAGE_EMS:
							case TxDB.MSG_TYPE_IMAGE_EMS:
								txmsg.updateState = TXMessage.UPDATING;
								// 改用重发图片的方法 2013.09.25 shc
								rePostImgSocket(txmsg);
								break;
							case TxDB.MSG_TYPE_QU_AUDIO_EMS:
							case TxDB.MSG_TYPE_AUDIO_EMS:
								txmsg.updateState = TXMessage.UPDATING;
								postAudioSocket(txmsg);
								break;
							case TxDB.MSG_TYPE_QU_BIG_FILE_SMS:
							case TxDB.MSG_TYPE_BIG_FILE_SMS:
								// 直接置为下载中状态，防止出现无网络点击下载按钮没反应，多个下载任务添加到队列的bug，后期还是要找多个重复任务添加到队列的问题所在
								txmsg.updateState = TXMessage.UPDATING;
								uploadBigFile(txmsg);
								break;
							default:
								sendMsg(txmsg);
								break;
							}
							flush(FLUSH_ROOM_FALSE);

						} else if (fitems[which].equals(redown)) {
							txmsg.updateState = TXMessage.UPDATE;
							flush(FLUSH_ROOM_FALSE);
						} else if (fitems[which].equals(report)) {
							Intent i = new Intent(BaseMsgRoom.this,
									GroupTip.class);
							i.putExtra(GroupTip.TXMSG, txmsg);
							i.putExtra(GroupTip.UID, txmsg.partner_id);
							i.putExtra(GroupTip.GID, groupid);
							startActivity(i);
						} else if (fitems[which].equals(del)) {
							// 点击删除消息，弹出确认对话框
							new AlertDialog.Builder(BaseMsgRoom.this)
									.setTitle(R.string.delete_confirm)
									// .setCancelable(false)
									// //设置不能通过“后退”按钮关闭对话框
									.setMessage(R.string.delete_confirm_message)
									.setPositiveButton(
											R.string.delete_confirm,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialoginterface,
														int ii) {
													// 确认删除的点击事件
													deleteTxMsg(
															dialoginterface,
															txmsg);// 删除此消息
												}
											})
									.setNegativeButton(
											R.string.cancel,
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int id) {
													dialog.cancel();
												}
											}).show();// 显示对话框
						} else if (fitems[which].equals(forword)) {
							shareMsg(txmsg);
						}

					}

				});

		// longItemDialog = new AlertDialog.Builder(BaseMsgRoom.this)
		// .setTitle(
		// BaseMsgRoom.this.getResources().getString(
		// R.string.chatroom_longclick_titile))
		// .setItems().show();

	}

	protected Set<Long> banList = new HashSet<Long>();// 禁言列表id集合
	protected List<String> auths = new ArrayList<String>();// 各种权限，例如：禁言、踢出群、封ID等

	// 聊天室中长按头像弹出的窗口
	protected void createWindow(final TXMessage txmsg) {

		final TX mtx;

		if (txmsg.partner_id != 0) {

			TX tx = TX.tm.getTx(txmsg.partner_id);

			if (tx == null) {
				tx = new TX();
				tx.partner_id = txmsg.partner_id;
				tx.setNick_name(txmsg.nick_name);
				tx.setSex(txmsg.tcard_sex);
				tx.sign = txmsg.tcard_sign;
				tx.avatar_url = txmsg.tcard_avatar_url;
			}
			mtx = tx;

			mSess.getSocketHelper().sendGetUserInfor(txmsg.partner_id);
		} else {

			return;
		}

		// 设置各种权限
		if (txGroup != null) {
			auths = TxGroup.initMsgRoom(txGroup);
		} else {
			return;
		}
		if (auths.size() == 0 || mtx.partner_id == TX.tm.getUserID())
			return;

		// boolean isBan = false;

		if (banList == null) {
			// 再去取禁言列表
			mSess.getSocketHelper().sendGetGroupBanList(txGroup.group_id, 0);
		} else if (banList.contains(mtx.partner_id)) {
			if (auths.contains("禁言")) {
				int index = auths.indexOf("禁言");
				auths.remove(index);
				auths.add(index, "取消禁言");
				// isBan = true;
			}
		}

		String[] items = auths.toArray(new String[] {});

		// final boolean isBan2 = isBan;

		new AlertDialog.Builder(BaseMsgRoom.this).setItems(items,
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						String s = auths.get(which);
						if (s.contains("禁言")) {
							// isBan2 == true,代表是取消禁言
							// if (isBan2) {
							if (banList.contains(mtx.partner_id)) {
								GroupUtils
										.showDialog(
												BaseMsgRoom.this,
												"警告",
												"是否取消 "
														+ smileyParser
																.addSmileySpans(
																		Utils.isNull(mtx
																				.getRemarkName()) ? mtx
																				.getNick_name()
																				: mtx.getRemarkName(),
																		true, 0)
														+ " 的禁言处罚?",
												R.string.dialog_okbtn,
												R.string.dialog_nobtn,
												new DialogInterface.OnClickListener() {

													@Override
													public void onClick(
															DialogInterface dialog,
															int which) {
														mSess.getSocketHelper()
																.sendShutupGroup(
																		txGroup.group_id,
																		mtx.partner_id,
																		1, 0);
													}
												});

							} else {
								// 点击了“禁言”，弹出禁言时长列表
								shutupOpt(mtx);
							}
						} else if (s.equals("踢出群")) {
							final List<Long> id = new ArrayList<Long>();
							id.add(mtx.partner_id);
							GroupUtils
									.showDialog(
											BaseMsgRoom.this,
											"踢出群",
											"是否确定将 "
													+ smileyParser.addSmileySpans(
															Utils.isNull(mtx
																	.getRemarkName()) ? mtx
																	.getNick_name()
																	: mtx.getRemarkName(),
															true, 0)
													+ " 移除此聊天室?",
											R.string.dialog_okbtn,
											R.string.dialog_nobtn,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													mSess.getSocketHelper()
															.sendDealGroup(
																	txGroup.group_id,
																	false, id);

												}
											});

						} else if (s.equals("警告")) {

							Intent i = new Intent(BaseMsgRoom.this,
									GroupWarn.class);
							i.putExtra("txmsg", txmsg);
							i.putExtra("uid", txmsg.partner_id);
							i.putExtra("gid", groupid);
							startActivity(i);

						} else if (s.equals("封ID")) {
							GroupUtils
									.showDialog(
											BaseMsgRoom.this,
											"警告",
											"是否确定将 "
													+ smileyParser.addSmileySpans(
															Utils.isNull(mtx
																	.getRemarkName()) ? mtx
																	.getNick_name()
																	: mtx.getRemarkName(),
															true, 0)
													+ " 处以封id的处罚? 该用户id将被永久封停!",
											R.string.dialog_okbtn,
											R.string.dialog_nobtn,
											new DialogInterface.OnClickListener() {
												@Override
												public void onClick(
														DialogInterface dialog,
														int which) {
													mSess.getSocketHelper()
															.sendBlock(
																	mtx.partner_id,
																	false);
												}
											});

						} else if (s.equals("封设备")) {
							GroupUtils.showDialog(
									BaseMsgRoom.this,
									"警告",
									"是否封锁 "
											+ smileyParser.addSmileySpans(
													Utils.isNull(mtx
															.getRemarkName()) ? mtx
															.getNick_name()
															: mtx.getRemarkName(),
													true, 0)
											+ " 的设备?该用户设备将永久无法使用神聊!",
									R.string.dialog_okbtn,
									R.string.dialog_nobtn,
									new DialogInterface.OnClickListener() {
										@Override
										public void onClick(
												DialogInterface dialog,
												int which) {
											mSess.getSocketHelper().sendBlock(
													mtx.partner_id, true);
										}
									});

						} else if (s.equals("举报")) {
							Intent i = new Intent(BaseMsgRoom.this,
									GroupTip.class);
							i.putExtra(GroupTip.TXMSG, txmsg);
							i.putExtra(GroupTip.UID, txmsg.partner_id);
							i.putExtra(GroupTip.GID, groupid);
							startActivity(i);
						}
					}
				}).show();

	}

	protected long mUpdateId = 0;// 这个值是干嘛用的？

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

	protected boolean turn = false;// 控制通知栏提示的？？？2014.01.20

	// 把时间毫秒值变为年月日
	private String dealDate(Long time) {
		return DateUtils.parseDateToLocal(time);
	}

	DownUploadListner mVCardCallback = new DownUploadListner() {
		@Override
		public void onStart(FileTaskInfo taskInfo) {
		}

		@Override
		public void onProgress(FileTaskInfo taskInfo, int curlen, int totallen) {
			TXMessage msg = (TXMessage) taskInfo.mObj;
			msg.updateCount = 0;
			flush(FLUSH_LISTVIEW_FALSE);
		}

		@Override
		public void onFinish(FileTaskInfo taskInfo) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.msg_path = taskInfo.mFullName;
			txmsg.updateState = TXMessage.UPDATE_OK;
			String name = txmsg.msg_path.substring(
					txmsg.msg_path.lastIndexOf("/") + 1,
					txmsg.msg_path.length());
			Contact contact = new Contact(BaseMsgRoom.this);
			contact.readToFile(name, BaseMsgRoom.this);
			ContentValues values = new ContentValues();
			values.put(TxDB.Messages.MSG_PATH, txmsg.msg_path);
			cr.update(TxDB.Messages.CONTENT_URI, values, TxDB.Messages.MSG_ID
					+ "=?", new String[] { "" + txmsg.msg_id });
			// TXMessage.saveTXMessagetoDB(txmsg, msgactTxdata);//这个是以前注释掉的
			flush(FLUSH_LISTVIEW_FALSE);
		}

		@Override
		public void onError(FileTaskInfo taskInfo, int iCode, Object iPara) {
			TXMessage txmsg = (TXMessage) taskInfo.mObj;
			txmsg.updateState = TXMessage.UPDATE_FAILE;
			flush(FLUSH_LISTVIEW_FALSE);
		}
	};

	public void DownContackSocket(TXMessage txmsg) {
		mSess.mDownUpMgr.downloadVCard(txmsg.msg_url, txmsg.msg_id, true,
				mVCardCallback, txmsg);
	}

	protected PopupWindow noticeMorePopWindow;
	protected View toastView;

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// InputMethodManager imm = (InputMethodManager) txdata
		// .getSystemService(Context.INPUT_METHOD_SERVICE);
		// if (imm != null && imm.isActive()) {
		// imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
		// }

		if (v.getId() == R.id.msgRoom_list) {
			// 触摸了listView
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				// 按下了listView
				isOnPressedListView = true;
				// 按下了listView再刷新就不能总停留在底部
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				// 手指抬起
				isOnPressedListView = true;
			}
		}

		hideInputMethod(v);// 隐藏软键盘

		if (downMorePopWindow != null && downMorePopWindow.isShowing()) {
			downMorePopWindow.dismiss();
		}
		if (mPop.getVisibility() == View.VISIBLE) {
			mPop.setVisibility(View.GONE);
			msgroom_expression_hsv_layout.setVisibility(View.GONE);
		}
		// if (event.getAction() == MotionEvent.ACTION_DOWN) {
		// if (noticeMorePopWindow != null && noticeMorePopWindow.isShowing()) {
		// Rect t = new Rect();
		// toastView.getLocalVisibleRect(t);
		// if (!t.contains((int) event.getRawX(), (int) event.getRawY())
		// && !t.contains((int) event.getRawX(),
		// (int) event.getRawY())) {
		// if (noticeMorePopWindow != null
		// && noticeMorePopWindow.isShowing())
		// noticeMorePopWindow.dismiss();
		//
		// }
		// }
		// }

		if (event.getAction() == MotionEvent.ACTION_DOWN) {
			Rect t = new Rect();// 发送按钮矩形区域
			sendTypeButton.getGlobalVisibleRect(t);
			Rect t2 = new Rect();// 更多按钮矩形区域，低栏加号
			downMoreButton.getGlobalVisibleRect(t2);
			if (!t.contains((int) event.getRawX(), (int) event.getRawY())
					&& !t.contains((int) event.getRawX(), (int) event.getRawY())) {
				// 点击区域不再这两个按钮上，则让更多popupWindow消失
				if (upMorePopWindow != null && upMorePopWindow.isShowing())
					upMorePopWindow.dismiss();
				if (downMorePopWindow != null && downMorePopWindow.isShowing())
					downMorePopWindow.dismiss();
			}
		}

		return false;
	}

	protected void localDelete(TXMessage txmsg, DialogInterface dialoginterface) {
		if (txMsgs.size() > 1) {
			// playManager.setRunning(false);
			mAudioRecPlayer.stopPlay();
			mSess.getSocketHelper().deleteGroupMessage(txGroup.group_id,
					txmsg.msg_id);
			mAudioRecPlayer.removeTalkCache(txmsg);
		} else {
			deletAllMsg();
			if (dialoginterface != null) {
				dialoginterface.dismiss();
			}
			finish();
		}
		showToast(R.string.del_success);
	}

	/** 弹出禁言时长 */
	private void shutupOpt(final TX tx) {
		final String[] items = new String[] { "5分钟", "30分钟", "24小时", "永久" };
		new AlertDialog.Builder(this).setItems(items,
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
								BaseMsgRoom.this,
								"警告",
								"是否将 "
										+ smileyParser.addSmileySpans(
												tx.getNick_name(), true, 0)
										+ " 处以 " + disTime + " 禁言的处罚?",
								R.string.dialog_okbtn, R.string.dialog_nobtn,
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										mSess.getSocketHelper()
												.sendShutupGroup(
														txGroup.group_id,
														tx.partner_id, 0, t);
									}
								});
					}
				}).show();
	}

	// 删除整个会话
	public void deletAllMsg() {
		if (Utils.isIdValid(txGroup.group_id)) {
			cr.delete(TxDB.Messages.CONTENT_URI, TxDB.Messages.MSG_GROUP_ID
					+ "=?", new String[] { "" + txGroup.group_id });

			// cr.delete(TxDB.MsgStat.CONTENT_URI, TxDB.MsgStat.MSG_GROUP_ID
			// + "=?", new String[] { "" + txGroup.group_id });

			MsgStat.delMsgStatByGroupId(mSess.getContentResolver(),
					txGroup.group_id);
		}
		if (txMsgs != null)
			txMsgs.clear();
	}

	// 单条刷新
	protected void flushline(Object obj) {
		if (obj == null || !(obj instanceof TXMessage))
			return;
		if (Utils.debug)
			Log.i(TAG, "notifhDataSetChanged单挑刷新");
		TXMessage txmsg = (TXMessage) obj;
		if (mCurrentMsgRoom != null) {
			mCurrentMsgRoom.contectsListAdapter.updateView(txmsg);
			mCurrentMsgRoom.contectsListAdapter.notifyDataSetChanged();
		}
		if (!isOnPressedListView && mbBottom) {
			msgRoomList.setSelection(contectsListAdapter.getCount());
		}
		if (!turn) {
			Utils.isNotificationShow = true;
		}
	}

	protected int mCurYPos = 0;
	protected ScrollView mScrollView;

	// handle更新界面
	protected Handler MsgHandler = new WrappedHandler(thisContext) {

		public void handleMessage(Message msg) {

			switch (msg.what) {
			case UPDATE_WINDOWCOMPLETE:
				comeInInfo();
				break;
			case FLUSH_ROOM_TRUE:
				removeMessages(msg.what);
				flushme(true);
				break;
			case FLUSH_ROOM_FALSE:
				removeMessages(msg.what);
				// msg.arg1 = -1 代码写的固定值
				flushme(msg.arg1);
				break;
			case FLUSH_LISTVIEW_TRUE:
				removeMessages(msg.what);
				flushme(true);
				break;
			case FLUSH_LISTVIEW_FALSE:
				removeMessages(msg.what);
				flushme(false);
				break;
			case FLUSH_ONE_LINE:
				flushline(msg.obj);
				break;
			case FLUSH_ROOM_INIT:// 为了搜索定位
				// if (iv_groupHeadView != null && txGroup != null) {
				// // 更新群头像
				// if (mGroupAvatar == null
				// || (mGroupAvatar.get(txGroup.group_id) == null)) {
				// iv_groupHeadView
				// .setImageResource(R.drawable.sl_group_default_head);
				// loadGroupImg(txGroup.group_avatar, txGroup.group_id,
				// avatarCallback);
				// } else
				// iv_groupHeadView.setImageBitmap(mGroupAvatar
				// .get(txGroup.group_id));
				// }// 这个是群聊的if语句，单聊用着没有有问题，因为有txGroup != null的判断
				setNameStr();// 设置单聊聊天室的名字显示，群聊调用也没问题，因为如果tx==null就直接return了
								// 2014.01.21 shc
				if (autoTxmsg != null) {
					flush(AUTO_SEND);
				}
				if (mAudioRecPlayer.talkMsgsCache.size() > 0) {
					mAudioRecPlayer.playTalkCache(
							mAudioRecPlayer.talkMsgsCache.get(0), audioHandler);
				}
				flushme(true);
				break;

			case PLAY_AUDIO_C0MPELET:
				musicUtils.PlaySound(3, 1, 2);
				TXMessage removeTxg = (TXMessage) msg.obj;
				mAudioRecPlayer.removeTalkCache(removeTxg);
				if (mAudioRecPlayer.talkMsgsCache.size() > 0) {
					mAudioRecPlayer.playTalkCache(
							mAudioRecPlayer.talkMsgsCache.get(0), audioHandler);
				}
				flush(FLUSH_LISTVIEW_FALSE);
				break;
			case AUTO_SEND:
				AutoSendMsg();
				break;
			// case FLUSH_VOLUEM_AN:
			// if (volumeImgView != null) {
			// int reid = msg.arg1;
			// volumeImgView.setImageResource(reid);
			// volumeImgView.invalidate();
			// }
			//
			// if (longvolumeImgView != null) {
			//
			// int reid = msg.arg1;
			// longvolumeImgView.setImageResource(reid);
			// longvolumeImgView.invalidate();
			// }
			// break;
			// 发送该消息的地方被屏蔽了，不再这么处理 2014.02.26 shc
			// case LAYOUT_CHANGE_HEIGHT:
			// if (downMorePopWindow != null && downMorePopWindow.isShowing()) {
			// RelativeLayout ll = (RelativeLayout)
			// findViewById(R.id.publicmsg_DownTools);
			// int[] location = new int[2];
			// ll.getLocationInWindow(location);
			// int y = display.getHeight() - location[1];
			// if (y < 0)
			// y = 0;
			// downMorePopWindow.dismiss();
			// downMorePopWindow.update();
			// downMorePopWindow.showAtLocation(downMoreButton,
			// Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, y);
			// }
			// break;
			case FLUSH_RCORDEND_BUTTON:
				btn_recordShortAduio.setText(R.string.publicmsg_record);
				break;
			case FLUSH_NOTICE:
				mScrollView.scrollTo(0, mCurYPos);
				break;
			case DISMISS_MSG_ROOM_POPUP:
				// if (noticePopWindow != null)
				// noticePopWindow.dismiss();
				if (downMorePopWindow != null) {
					downMorePopWindow.dismiss();
				}
				if (downMorePopWindow != null) {
					downMorePopWindow.dismiss();
				}
				break;
			case SET_GEO_MSG:
				Object[] obj = (Object[]) msg.obj;
				String geo_msg = (String) obj[0];
				TextView tv = (TextView) obj[1];
				tv.setText(geo_msg);
				break;
			}
			super.handleMessage(msg);
		}
	};

	// bottem：是否定位到最下端
	private void flushme(boolean isBottem) {
		if (Utils.debug)
			Log.i(TAG, "notifhDataSetChanged消息adapter");
		if (mCurrentMsgRoom != null) {
			mCurrentMsgRoom.contectsListAdapter.setData(txMsgs);
			mCurrentMsgRoom.contectsListAdapter.notifyDataSetChanged();
		}
		if (Utils.debug) {
			Log.i("Zzl", "txMsg : " + txMsgs);
		}
		if (isBottem) {
			msgRoomList.setSelection(contectsListAdapter.getCount());
		}
		if (!turn) {
			Utils.isNotificationShow = true;
		}
	}

	private void flushme(int pos) {
		if (Utils.debug)
			Log.i(TAG, "notifhDataSetChanged消息adapter,位置=" + pos);
		contectsListAdapter.setData(txMsgs);
		contectsListAdapter.notifyDataSetChanged();
		if (pos > 0)
			msgRoomList.setSelection(pos);

	}

	/**
	 * 如果是转发进来执行下面的逻辑
	 */
	// 转发后自动发送信息
	protected TXMessage autoTxmsg;// 自动发送的消息

	public void AutoSendMsg() {
		if (autoTxmsg != null) {
			if (tx != null && !Utils.isIdValid(tx.partner_id)) {
				// 单聊的短信信息
				msgEdit.setText(autoTxmsg.msg_body);
				msgEdit.setHint(null);
			} else {
				switch (autoTxmsg.msg_type) {
				// 神聊部分
				case TxDB.MSG_TYPE_COMMON_SMS:// 普通神聊
				case TxDB.MSG_TYPE_QU_COMMON_SMS:// 普通群消息
					msgEdit.setText(autoTxmsg.msg_body);
					msgEdit.setHint(null);
					if (autoTxmsg.msg_type == TxDB.MSG_TYPE_COMMON_SMS) {
						// 单聊中有这些语句
						msgEdit.setVisibility(View.VISIBLE);
						btn_recordShortAduio.setVisibility(View.GONE);
						SendButton.setVisibility(View.VISIBLE);
						msgEdit.requestFocus();
					}
					break;
				case TxDB.MSG_TYPE_IMAGE_EMS:// 图片
				case TxDB.MSG_TYPE_QU_IMAGE_EMS:// 群图片消息
					sendimgPath = autoTxmsg.msg_path;
					TXMessage txmsgTemp = getForwardImgTxmsg(sendimgPath,
							autoTxmsg);
					sendMsg(txmsgTemp);
					sendimgPath = null;
					break;
				case TxDB.MSG_TYPE_AUDIO_EMS:// 音频
				case TxDB.MSG_TYPE_QU_AUDIO_EMS:// 群音频
					TXMessage txmsg = getForwardAudioTxmsg(autoTxmsg);
					sendMsg(txmsg);
					break;
				case TxDB.MSG_TYPE_GEO_SMS:// 地理位置
				case TxDB.MSG_TYPE_QU_GEO_SMS:// 群地理位置
					TXMessage txmsgTemp1 = getForwardGeoTxmsg(autoTxmsg);
					sendMsg(txmsgTemp1);
					break;

				case TxDB.MSG_TYPE_TCARD_SMS:// tcard名片
				case TxDB.MSG_TYPE_QU_TCARD_SMS:// 群tcard名片
					TXMessage txmsg2 = getForwardTCardTxmsg(autoTxmsg);
					sendMsg(txmsg2);
					break;
				}
			}
		}
	}

	protected TX tx;// 应该是聊天对方的TX

	// 单聊设置显示名称？？？
	public abstract void setNameStr();

	// 进会话界面
	public void comeInInfo() {
		if (Utils.checkNetworkAvailable1(this)) {
			if (Utils.getNetworkType(this) == Utils.NET_PROXY) {
				creatMsgInfo(true, R.string.msg_wapstr);
			}
		} else {
			creatMsgInfo(true, R.string.msg_nonetstr);
		}
	}

	// 点击发送发送短消息
	// float downx = 0, downy = 0;

	/**
	 * 点发送按钮发送逻辑
	 */
	public void SendButtonMsg() {

		// SendButton.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		//
		// if (event.getAction() == MotionEvent.ACTION_MOVE) {
		//
		// } else if (event.getAction() == MotionEvent.ACTION_DOWN) {
		// downx = event.getX();
		// downy = event.getY();
		// } else if (event.getAction() == MotionEvent.ACTION_UP) {
		// if (Utils.checkNetworkAvailable1(txdata)) {
		// if (Utils.getNetworkType(txdata) == Utils.NET_PROXY) {
		// creatMsgInfo(false, R.string.msg_wapstr);
		// } else {
		// String comandText = msgEdit.getText().toString();
		// if (!TextUtils.isEmpty(comandText)) {
		// String sendStr = comandText.trim();
		// send(sendStr);
		// msgEdit.setText(null);
		// }
		// }
		// } else {
		// creatMsgInfo(false, R.string.msg_nonetstr);
		// }
		//
		// }
		// return false;
		// }
		//
		// });

		SendButton.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (Utils.checkNetworkAvailable1(mSess.getContext())) {
					if (Utils.getNetworkType(mSess.getContext()) == Utils.NET_PROXY) {
						creatMsgInfo(false, R.string.msg_wapstr);
					} else {
						String comandText = msgEdit.getText().toString();
						if (!TextUtils.isEmpty(comandText.trim())) {
							String sendStr = comandText.trim();
							send(sendStr);
							msgEdit.setText(null);
						}
					}
				} else {
					creatMsgInfo(false, R.string.msg_nonetstr);
				}

			}
		});
	}

	// 关闭软键盘
	protected void hideInputMethod(View view) {
		if (imm != null && imm.isActive()) {
			imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
		}

	}

	protected void showInputMethodDialog() {
		if (!imm.isActive()) {
			imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					InputMethodManager.HIDE_NOT_ALWAYS);
		}
	}

	// 应该是聊天室下面输入框的事件设置
	private void toolLogic() {
		downMoreButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				v.startAnimation(Utils.getAnimationRotate(0, 45));// 旋转动画
				creatDownMorePop();
			}

		});

		sendTypeButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				if (downMorePopWindow != null) {
					downMorePopWindow.dismiss();
				}

				// MsgHandler.sendEmptyMessageDelayed(LAYOUT_CHANGE_HEIGHT,
				// 200);
				changeType(true, false);
				Utils.saveAutoPlayAdiouData(thisContext);

			}

		});

		// 更多按钮
		// moreButton.setOnTouchListener(new OnTouchListener() {
		//
		// @Override
		// public boolean onTouch(View v, MotionEvent event) {
		// //
		// if (event.getAction() == MotionEvent.ACTION_DOWN) {
		// moreButton
		// .setBackgroundResource(R.drawable.sl_search_press);
		// moreButton.setPadding(2, 2, 2, 2);
		// } else if (event.getAction() == MotionEvent.ACTION_UP) {
		// moreButton
		// .setBackgroundResource(R.drawable.sl_search_normal);
		// moreButton.setPadding(2, 2, 2, 2);
		// // GroupButtonLogic();
		// creatUpMorePop();
		// }
		//
		// return true;
		// }
		// });

		moreButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				creatUpMorePop();
			}
		});

		if (Utils.isSendText) {
			msgEdit.setVisibility(View.VISIBLE);
			btn_recordShortAduio.setVisibility(View.GONE);
		} else {
			msgEdit.setVisibility(View.GONE);
			btn_recordShortAduio.setVisibility(View.VISIBLE);
		}

		// 输入框监听文字改变
		msgEdit.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				sssss = s;
				SendButton.setEnabled(s.length() > 0);
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			@Override
			public void afterTextChanged(Editable s) {
				int selectionEnd = msgEdit.getSelectionEnd();
				if (sssss.length() > 255) {
					s.delete(255, selectionEnd);
					msgEdit.setText(s);
					msgEdit.setSelection(255);// 设置光标在最后
				}
			}
		});

		// 对键盘的隐藏和弹出要放在ontouch方法里
		msgEdit.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (mPop.getVisibility() == View.VISIBLE && msgEdit.isFocused()) {
					mPop.setVisibility(View.GONE);
					msgroom_expression_hsv_layout.setVisibility(View.GONE);
					showInputMethodDialog();
					// if (!imm.isActive())
					// imm.toggleSoftInput(InputMethodManager.SHOW_IMPLICIT,
					// InputMethodManager.HIDE_NOT_ALWAYS);
				} else {
				}
				return false;
			}

		});

		DisplayMetrics metric = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metric);
		densityDpi = metric.densityDpi;

		// 长按编辑框弹出菜单
		msgEdit.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {

				final EditText et = (EditText) v;
				final String paste = thisContext.getResources().getString(
						R.string.chatroom_editText_paste);
				final String copy = thisContext.getResources().getString(
						R.string.chatroom_longclick_copy);
				final String inputMethod = thisContext.getResources()
						.getString(R.string.chatroom_editText_inputMethod);
				final String[] items = { copy, paste, inputMethod };

				Utils.creatDialogMenu(thisContext, items, getResources()
						.getString(R.string.chatroom_longclick_titile),
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								if (items[which].equals(copy)) {
									ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
									clipboard.setText(et.getText().toString());
								} else if (items[which].equals(paste)) {
									ClipboardManager clipboard2 = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
									StringBuffer clipsb = new StringBuffer()
											.append(clipboard2.getText());
									String clip = clipsb.toString();
									if (densityDpi > 160)
										et.append(smileyParser.addSmileySpans(
												clip, true, 0));
									else
										et.append(smileyParser.addSmileySpans(
												clip, true, 0));
									et.requestFocus();

								} else if (items[which].equals(inputMethod)) {
									showInputMethodDialog();
								}
							}
						});

				// longEditTextDialog = new AlertDialog.Builder(thisContext)
				// .setTitle(R.string.chatroom_longclick_titile)
				// .setItems().show();
				return true;
			}
		});
	}

	// 为了解决popupWindow显示时，再弹出软键盘被废弃 2014.02.26 shc
	// /**
	// * 监听界面布局高度变化
	// */
	// public static final int LAYOUT_HEIGHT_BIG = 0;
	// public static final int LAYOUT_HEIGHT_SMALL = 1;
	//
	// public void ListenLayoutHeightChange() {
	// ResizeLayout layout = (ResizeLayout)
	// findViewById(R.id.msgRoom_mainLayout);
	// layout.setOnResizeListener(new ResizeLayout.OnResizeListener() {
	// public void OnResize(int w, int h, int oldw, int oldh) {
	//
	// int change = LAYOUT_HEIGHT_BIG;
	// boolean isHidde = Math.abs(display.getHeight() - h) > 100 ? true
	// : false;
	// Message message = new Message();
	// if (isHidde) {
	//
	// if (h < oldh) {
	// change = LAYOUT_HEIGHT_SMALL;
	// }
	// }
	// message.what = LAYOUT_CHANGE_HEIGHT;
	// message.arg1 = change;
	// MsgHandler.sendMessage(message);
	// }
	// });
	// }

	// 消息列表变化，单聊和群聊统一共用
	protected void dealChatMsgChanged(Message msg) {
		int state = msg.arg1;
		txMsgs.clear();
		// Collections.synchronizedList(txMsgs);
		// txMsgs.addAll(txMsgs);
		// if (Utils.debug)
		// Log.e(TAG, "数据长度：" + txMsgs.size());

		synchronized (synchronizedMsgs) {
			txMsgs.addAll(synchronizedMsgs);
			if (Utils.debug)
				Log.e(TAG, "数据长度：" + synchronizedMsgs.size());

		}
		switch (state) {
		case SocketHelper.CHAT_MSG_DEL:// 删除
			if (Utils.debug)
				Log.e(TAG, "删除数据");

			flush(FLUSH_ROOM_FALSE);
			break;
		case SocketHelper.CHAT_MSG_ADD:// 增加
			if (Utils.debug)
				Log.e(TAG, "增加数据");

			flush(FLUSH_ROOM_FALSE);
			if (Utils.isOpenPlayAdiou && !mIsRecording) {
				long msgid = 0;
				if (msg.obj != null) {
					msgid = (Long) msg.obj;
				}
				if (msgid > 0) {
					for (TXMessage temptxmsg : txMsgs) {
						if (temptxmsg.msg_id == msgid
								&& temptxmsg.read_state != TXMessage.READ) {
							mAudioRecPlayer.addTalkCache(temptxmsg,
									isStartAudioPlay);
							break;
						}
					}
				}
			}
			break;
		case SocketHelper.CHAT_MSG_ADD_SELF:// 自己发的消息
			if (Utils.debug)
				Log.e(TAG, "自己发的消息数据");

			flush(FLUSH_ROOM_TRUE);
			break;
		case SocketHelper.CHAT_MSG_ADD_OLD:// 加载历史数据
			if (Utils.debug)
				Log.e(TAG, "加载历史数据");

			msgRoomList.onRefreshComplete();
			Map<String, Long> map = (Map<String, Long>) msg.obj;
			if (map != null) {
				long eof = map.get("eof");
				if (Utils.debug) {
					Log.i(TAG, "eof = " + eof + ",msg.arg2:" + msg.arg2);// msg.arg2也许是添加消息的总数
				}
				if (eof == 1) {
					msgRoomList.setonRefreshListener(null, true);
				} else {
					msgRoomList
							.setonRefreshListener(
									new com.shenliao.view.MyGroupListView.OnRefreshListener() {
										@Override
										public void onRefresh() {
											mSess.getSocketHelper()
													.getGroupOldMessage(
															txGroup.group_id);
										}
									}, false);
				}
			}
			if (msg.arg2 > 0) {
				if (Utils.debug)
					Log.e(TAG, "msg.arg2 > 0，等于：" + msg.arg2 + ",刷新View");
				flush(FLUSH_ROOM_FALSE, msg.arg2);
			}
			break;
		case SocketHelper.CHAT_MSG_UPDATE:// 数据更新, 消息状态改变
			if (Utils.debug)
				Log.e(TAG, "数据更新");

			if (msg.arg2 == SocketHelper.CHAT_AUDIO_SENT) {
				musicUtils.PlaySound(4, 1, 3);
			}
			flush(FLUSH_ROOM_FALSE);
			break;

		}

	}

	/**
	 * deal {@link #onActivityResult(int, int, Intent)} to get gif file
	 * 
	 * @param requestCode
	 * @param data
	 */
	String fileName;
	RandomAccessFile mFile;
	String gifName;
	String newName;

	// protected void dealAddGifFile(int requestCode, Intent data)
	// throws IOException {
	// if (Utils.debug) {
	// if (data != null) {
	// ArrayList<String> gif = data
	// .getStringArrayListExtra(MSG_FILE_LIST);
	// String gifFileName = "";
	// int id = data.getIntExtra(
	// FolderExplorerGifActivity.GIF_PACKAGE_ID, -1);
	// if (gif.size() > 0) {
	// gifFileName = gif.get(0).toString();
	// Log.d(TAG, "====" + gifFileName);
	// mPopupWindow.addSingleView(gifFileName, id);
	// }
	//
	// }
	// } else {
	//
	// }
	//
	// }

	protected void dealSetGif(int requestCode, Intent data) throws IOException {
		mPopupWindow.initLastView();
		try {
			ArrayList<EmojiInfor> list = mSess.mUserEmojiInforsMgr
					.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
			// int id =
			// data.getIntExtra(FolderExplorerGifActivity.GIF_PACKAGE_ID, -1);
			mPopupWindow.setViews(list);
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	/** 处理onActivityResult发送大文件 */
	protected void dealSendBigFile(int requestCode, Intent data) {
		// 选择发送大文件
		if (data != null) {
			ArrayList<String> sendFileList = data
					.getStringArrayListExtra(MSG_FILE_LIST);// 发送的文件路径集合

			File bigFile = null;
			if (sendFileList == null) {
				// 代表是从系统的视频中选择的文件
				String mediaPath = null;
				Uri uri = data.getData();
				Cursor cursor = getContentResolver().query(uri, null, null,
						null, null);
				if (cursor != null) {
					// 从手机视频库选择的文件
					cursor.moveToFirst();
					mediaPath = cursor.getString(1); // 视频文件本地路径

				} else {
					// 从手机文件夹选择的文件
					mediaPath = uri.getPath();
				}

				// 视频发送入口只允许发送mp4视频文件
				String sub_ext = mediaPath
						.substring(mediaPath.lastIndexOf(".") + 1);// 文件后缀名
				if (requestCode == REQ_TAKE_MUSIC_FILES) {
					// 发送音乐文件
					if (!sub_ext.equalsIgnoreCase("mp3")) {
						showToast("不支持此音频类型，请通过文件方式发送");
					} else {
						uploadAndAddBigFileMsg(mediaPath);
					}

				} else {
					// 发送视频文件
					if (!sub_ext.equalsIgnoreCase("mp4")) {
						mediaPath = null;// 把视频路径变量重置
						showToast("不支持此视频类型，请通过文件方式发送");
					}
					if (mediaPath != null) {
						// 视频路径有数据，则预览
						if (Utils.debug)
							Log.e(TAG, "视频文件本地路径：" + mediaPath);

						Intent intent = new Intent(thisContext,
								VideoPlayActivity.class);
						intent.putExtra(Constants.FILE_LOCAL_PATH, mediaPath);
						intent.putExtra(VideoPlayActivity.IS_PREVIEW_VIDEO,
								true);
						startActivityForResult(intent, REQ_TAKE_BIG_FILES);
					}
				}

			} else {
				boolean isBigFileValid = true;// 待发送的大文件消息是否全部合法
				// 自己定义传递过来的文件
				for (String tempFilePath : sendFileList) {
					// 遍历检查文件大小是否超过20M
					if (!TextUtils.isEmpty(tempFilePath)) {
						// 文件路径不能为空
						bigFile = new File(tempFilePath);
						if (!bigFile.exists()
								|| bigFile.length() > 20 * 1024 * 1024) {
							// 文件不存在，或者大于20M
							isBigFileValid = false;
						}
					} else {
						isBigFileValid = false;
						break;
					}
				}
				if (isBigFileValid) {
					// 大文件消息有效
					for (String filePath : sendFileList) {
						uploadAndAddBigFileMsg(filePath);
						// final TXMessage txmsgTemp = TXMessage
						// .creatGroupBigFileSms(
						// txGroup.group_id,
						// txGroup.group_title,
						// filePath,
						// System.currentTimeMillis()
						// / 1000
						// + getPrefsMeme().getLong(
						// CommonData.ST, 0));
						// uploadBigFile(txmsgTemp);// 上传大文件
						//
						// SocketHelper.getSocketHelper(txdata).addGroupMessage(
						// txmsgTemp);
					}
				} else {
					showToast("为了保证传输效率，请不要选择20M以上的文件。");
				}
			}

			sendFileList = null;
		}
	}

	/** 上传并且添加大文件消息 */
	protected abstract void uploadAndAddBigFileMsg(String filePath);

	protected abstract void sendGifMsg(int num, int pkgid, String emomd5,
			String pkg_emomd5);

	/** 录音的popupWindow */
	private class RecorderPopupWindow {
		private int recordWindowW, recordWindowH;
		private int longrecordWindowW, longrecordWindowH;
		protected View popupWindow_view;
		protected PopupWindow recordPopup;
		protected ImageView volumeImgView;
		protected ImageView longvolumeImgView;
		protected TextView recordPopupText1;
		protected TextView recordPopupText2;
		protected TextView progressTime;
		protected View popupWindow_view_long;
		protected PopupWindow longrecordPopup;
		protected Button canclebtn;
		protected Button beginRecord;
		protected TextView currentTime;
		protected SeekBar volumeSeek;
		protected Timer progressTimer;
		protected TimerTask progressTask;
		protected Timer seekTimer;
		protected TimerTask seekTask;

		protected Timer recordtime;
		protected TimerTask recordTask;
		protected Timer longRecordTimer;
		protected TimerTask longRecordTask;

		private Handler recordWindowHandler;
		private ImageView longrecord_point;

		public RecorderPopupWindow() {
			recordWindowHandler = new Handler() {
				@Override
				public void handleMessage(Message msg) {
					int resid;
					switch (msg.what) {
					case HANDLE_RECORDER_ERROR:
						switch (msg.arg1) {
						case RecordListener.ERR_FAILCREATEFILE:
							resid = R.string.record_failcreatefile;
							break;
						case RecordListener.ERR_DEVNOTINITED:
							resid = R.string.record_uninit;
							break;
						case RecordListener.ERR_DEVFILEERR:
							resid = R.string.record_devfileerr;
							break;
						case RecordListener.ERR_INTERNALERR:
							resid = R.string.record_internal;
							break;
						default:
							resid = R.string.record_internal;
							break;
						}
						if (longrecordPopup != null
								&& longrecordPopup.isShowing()) {

							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							stopLongAudioRecord();
							longrecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							longrecordPopup.dismiss();
						}
						if (recordPopup != null && recordPopup.isShowing()) {
							recordPopup.dismiss();
						}

						if (unInitRecordToast == null) {
							unInitRecordToast = Toast.makeText(thisContext,
									resid, Toast.LENGTH_SHORT);
						}
						unInitRecordToast.show();

						break;
					case FLUSH_PROGRESS_TIME:
						if (mAudioRecPlayer != null) {
							int ms = mAudioRecPlayer.getAudioDuration();
							String str = Utils.formatDurationTimes(ms);
							if (Utils.debug) {
								Log.i(TAG, "FLUSH_PROGRESS_TIME" + str);
							}
							progressTime.setText(str);
						}
						break;

					case FLUSH_SEEK_TEIME:
						int mss = mAudioRecPlayer.getAudioDuration();
						if (Utils.debug) {
							Log.i(TAG, "长录音时间长audio time is:" + mss);
						}

						// if ((mss / 1000) <= LONG_RECORD_EDN_TIME) {
						if (mss <= LONG_RECORD_EDN_TIME) {// 取到的mss单位已经是秒了，不能再除以1000
							currentTime.setText(Utils.formatDurationTimes(mss));
							volumeSeek.setProgress(mss);
						}

						break;
					case CANCLE_SHORT_RECORD:
						// 短录音时间到，取消短录音
						stopShortRecord();

						break;
					case CANCLE_LONG_RECORD:
						// 长录音时间到，取消长录音
						mRecordPopupWindow.stopLongRecord();

						break;
					case RECORD_TIME_SHORT:
						// 录音时间太短
						Toast.makeText(mSess.getContext(),
								R.string.record_time_short, Toast.LENGTH_SHORT)
								.show();
						break;

					case FLUSH_VOLUEM_AN:
						if (volumeImgView != null) {
							int reid = msg.arg1;
							volumeImgView.setImageResource(reid);
							volumeImgView.invalidate();
						}

						if (longvolumeImgView != null) {

							int reid = msg.arg1;
							longvolumeImgView.setImageResource(reid);
							longvolumeImgView.invalidate();
						}
						break;
					}
				}
			};
		}

		// 是否有window在展示
		public boolean isWindowShowing() {
			if (recordPopup != null && recordPopup.isShowing()) {
				return true;
			}
			if (longrecordPopup != null && longrecordPopup.isShowing()) {
				return true;
			}
			return false;

		}

		// 显示短录音窗口
		public void showRecorderScreen(View parent, boolean isControl) {

			if (recordPopup == null) {

				// 获得录音窗口宽高
				Bitmap windowImg = BitmapFactory.decodeResource(getResources(),
						R.drawable.record_back);
				if (windowImg != null) {
					recordWindowW = windowImg.getWidth();
					recordWindowH = windowImg.getHeight();
				}
				windowImg.recycle();

				popupWindow_view = mInflate
						.inflate(R.layout.recordscreen, null);

				recordPopup = new PopupWindow(popupWindow_view, recordWindowW,
						recordWindowH, true);// 创建PopupWindow实例

				recordPopup.setFocusable(false);

				volumeImgView = (ImageView) popupWindow_view
						.findViewById(R.id.recordscreen_MicValoum);

				recordPopupText1 = (TextView) popupWindow_view
						.findViewById(R.id.recordscreen_text1);

				recordPopupText2 = (TextView) popupWindow_view
						.findViewById(R.id.recordscreen_text2);
				progressTime = (TextView) popupWindow_view
						.findViewById(R.id.recordscreen_progressTime);

			}

			if (recordPopup != null) {

				recordPopupText1
						.setText(isControl == true ? R.string.record_screen_info4
								: R.string.record_screen_info1);

				recordPopupText2
						.setText(isControl == true ? R.string.record_screen_info3
								: R.string.record_screen_info2);

				recordPopup.showAtLocation(parent, Gravity.CENTER, 0, 0);

			}

		}

		// 显示长录音界面
		public void showLongRecorderScreen(View parent) {

			if (longrecordPopup == null) {
				// 获得长按录音窗口宽高
				Bitmap windowImg = BitmapFactory.decodeResource(getResources(),
						R.drawable.longrecordbg);

				if (windowImg != null) {

					longrecordWindowW = windowImg.getWidth();

					longrecordWindowH = windowImg.getHeight();

				}

				windowImg.recycle();

				popupWindow_view_long = mInflate.inflate(
						R.layout.longrecordscreen, null);
				longrecordPopup = new PopupWindow(popupWindow_view_long,
						longrecordWindowW, longrecordWindowH, true);// 创建PopupWindow实例
				canclebtn = (Button) popupWindow_view_long
						.findViewById(R.id.longrecord_del);
				beginRecord = (Button) popupWindow_view_long
						.findViewById(R.id.longrecord_begin);
				longvolumeImgView = (ImageView) popupWindow_view_long
						.findViewById(R.id.longrecordscreen_MicValoum);
				longrecord_point = (ImageView) popupWindow_view_long
						.findViewById(R.id.longrecord_point);
				currentTime = (TextView) popupWindow_view_long
						.findViewById(R.id.curtime);
				volumeSeek = (SeekBar) popupWindow_view_long
						.findViewById(R.id.longrecord_seekBar);
				volumeSeek.setMax(421);
				popupWindow_view_long.setFocusable(true);
				popupWindow_view_long.setFocusableInTouchMode(true);
				longrecordPopup.setFocusable(true);
				popupWindow_view_long.setOnKeyListener(new OnKeyListener() {
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (keyCode == KeyEvent.KEYCODE_BACK) {
							if (Utils.isRrecord) {
								longrecordPopup.dismiss();
								showDwonToolsNo();
							}

							return true;
						}
						return false;
					}
				});
				longrecordPopup.setOnDismissListener(new OnDismissListener() {

					@Override
					public void onDismiss() {
						showDwonToolsNo();

					}
				});

				/**
				 * 长按录音的相关逻辑方法 开始录音按钮和点击发送按钮的点击事件
				 */
				beginRecord.setOnClickListener(new OnClickListener() {

					@Override
					public void onClick(View v) {
						if (!isCanSendAudio()) {
							mRecordPopupWindow.exitLongRecorderScreen();

							return;
						}

						if (Utils.isRrecord) {
							if (Utils.checkNetworkAvailable1(mSess.getContext())) {
								mHasRecordErr = false;
								if (!Utils.checkSDCard()) {// 无SD卡
									toastList.add(Utils
											.creatNoSdCardInfo(BaseMsgRoom.this));

								} else {
									Utils.isRrecord = false;
									longrecord_point.setVisibility(View.GONE);
									beginRecord.setText("立即发送");
									longRecordAudioUpLoad();
								}

							} else {

								creatMsgInfo(true, R.string.msg_nonetstr);
							}
						} else {
							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							mRecordPopupWindow.exitLongRecorderScreen();
							longrecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							if (mLongAudioMsg != null) {
								// 有等待发送的长录音消息，直接发送消息
								sendAudioMsgAfterRocord(mLongAudioMsg);
								mLongAudioMsg = null;
							} else {
								if (!mIsRecording) {
									// recordManager.setRunning(false);
									mAudioRecPlayer.stopRecord();
								}
								if (mAudioRecPlayer != null) {
									stopAudioRecordSocket(false);
								}
							}
							showDwonToolsNo();
						}
					}
				});

				// 取消按钮点击事件
				canclebtn.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mAudioRecPlayer != null
								&& mAudioRecPlayer.isRecording()) {
							// 如果不在录音状态，则不做任何操作，直接退出界面
							stopLongAudioRecord();
							Utils.isRrecord = true;
							cancelLongRecordSeekTime();
							cancelLongRecordTimes();
							currentTime.setText("00:00");
							volumeSeek.setProgress(0);
							longrecord_point.setVisibility(View.VISIBLE);
							beginRecord.setText("开始录音");
							isCancelSendAudioMsg = true;// 取消音频发送
							mSess.mDownUpMgr.removeUploadTask(mSess.mDownUpMgr
									.getUploadTaskId(mAudioRecPlayer
											.getAudioFilePath()));

						}
						mRecordPopupWindow.exitLongRecorderScreen();
						showDwonToolsNo();
					}
				});

			}

			if (longrecordPopup != null) {
				longrecordPopup.showAtLocation(parent, Gravity.CENTER, 0, 0);
			}

		}

		/**
		 * 短录音取消逻辑，//TODO 取消逻辑应该统一处理。现在入口也比较多
		 */
		public void stopShortRecord() {
			wireControl = WIRECONTROL_NOMAL;
			MsgRoomMainLayout.isIntercept = false;
			exitRecorderScreen();
			// flush(FLUSH_RCORDEND_BUTTON);
			stopAudioRecordSocket(true);
			if (wireControl == WIRECONTROL_PLAY) {
				musicUtils.PlaySound(2, 1, 1);
			}
			mRecordPopupWindow.cancelRecordTims();
			mRecordPopupWindow.cancelRecordProgressTime();
		}

		public Handler getHandler() {
			return recordWindowHandler;

		}

		public void exitLongRecorderScreen() {
			if (longrecordPopup != null && longrecordPopup.isShowing()) {
				longrecordPopup.dismiss();
			}

		}

		// 关闭录音
		public void exitRecorderScreen() {

			if (recordPopup != null && recordPopup.isShowing()) {

				recordPopup.dismiss();

			}
		}

		/**
		 * 取消短录音的每个半秒钟取录音时长去更新的任务
		 */
		public void cancelRecordProgressTime() {
			if (progressTimer != null) {
				progressTimer.cancel();
				progressTimer = null;
			}
			if (progressTask != null) {
				progressTask.cancel();
				progressTask = null;
			}
			if (progressTime != null) {
				progressTime.setText("00:00");
			}
		}

		/**
		 * 短录音的每隔半秒钟取录音时间，更新popupWindow的录音时长
		 */
		public void recordProgressTime() {
			if (progressTimer != null) {
				if (progressTask != null) {
					progressTask.cancel();
				}
			}
			progressTask = new TimerTask() {
				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							FLUSH_PROGRESS_TIME);
				}
			};
			progressTimer = new Timer(true);
			progressTimer.schedule(progressTask, 100, 500);
		}

		public void longrecordSeekTime() {

			if (seekTimer != null) {
				if (seekTask != null) {
					seekTask.cancel();
				}
			}
			seekTask = new TimerTask() {
				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							FLUSH_SEEK_TEIME);
				}
			};
			seekTimer = new Timer(true);
			seekTimer.schedule(seekTask, 100, 500);

		}

		/**
		 * 长录音到7分钟自动结束的结束方法
		 */
		private void stopLongRecord() {
			stopLongAudioRecord();
			// Utils.isRrecord = true;
			cancelLongRecordSeekTime();
			cancelLongRecordTimes();
			// currentTime.setText("00:00");
			// volumeSeek.setProgress(0);
			// beginRecord.setImageResource(R.drawable.longrecordbegin);
			//
			// exitLongRecorderScreen();
			// showDwonTools();
			//
			// wireControl = WIRECONTROL_NOMAL;
			//
			// MsgRoomMainLayout.isIntercept = false;
			//
			// if (wireControl == WIRECONTROL_PLAY) {
			//
			// musicUtils.PlaySound(2, 1, 1);
			//
			// }

		}

		/**
		 * 取消长录音的每隔半秒取录音时长，发消息更新popupWindow
		 */
		public void cancelLongRecordSeekTime() {
			if (seekTimer != null) {
				seekTimer.cancel();
				seekTimer = null;
			}
			if (seekTask != null) {
				seekTask.cancel();
				seekTask = null;
			}

		}

		public void cancelLongRecordTimes() {
			if (longRecordTimer != null) {
				longRecordTimer.cancel();
				longRecordTimer = null;
			}
			if (longRecordTask != null) {
				longRecordTask.cancel();
				longRecordTask = null;
			}
		}

		/**
		 * 取消短录音的到2分钟自动停止录音的定时任务
		 */
		public void cancelRecordTims() {
			if (recordtime != null) {
				recordtime.cancel();
				recordtime = null;
			}
			if (recordTask != null) {
				recordTask.cancel();
				recordTask = null;
			}
		}

		// 长按录音开始会自动计时7分钟后会停止录音

		public void longRecordTimes() {

			longRecordTimer = new Timer();

			longRecordTask = new TimerTask() {

				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							CANCLE_LONG_RECORD);

				}
			};

			longRecordTimer.schedule(longRecordTask,
					LONG_RECORD_EDN_TIME * 1000);

		}

		// 录音开始后自动计时 2分钟后自动停止录音
		public void recordTims() {
			recordtime = new Timer();
			recordTask = new TimerTask() {
				public void run() {
					mRecordPopupWindow.getHandler().sendEmptyMessage(
							CANCLE_SHORT_RECORD);
				}
			};
			recordtime.schedule(recordTask, RECORD_END_TIME * 1000);
		}
	}

	class GifDisplayRunnable implements Runnable {
		AnimationDrawable amd;

		GifDisplayRunnable(AnimationDrawable amd) {
			this.amd = amd;
		}

		@Override
		public void run() {
			if (amd != null) {
				amd.stop();
				amd.start();
			}
		}

	}

	private String getLocalGifOppositePath(TXMessage txmsg) {
		String path = "/sdcard/shenliao/gif_opposite/" + txmsg.pkgid + "_"
				+ txmsg.emomd5 + ".gif";
		File file = new File(path);
		if (file.exists()) {
			return file.getPath();
		}
		return null;
	}

	private String getLocalGifPath(TXMessage txmsg) {
		String path = "/sdcard/shenliao/gif/" + txmsg.pkgid + "_"
				+ txmsg.emomd5 + ".gif";
		File file = new File(path);
		if (file.exists()) {
			return file.getPath();
		}
		return null;
	}

	public static final int GIF_SIZE = 3;

}