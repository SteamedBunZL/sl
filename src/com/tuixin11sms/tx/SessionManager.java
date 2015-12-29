package com.tuixin11sms.tx;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Future;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v4.util.LruCache;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import com.shenliao.data.DataContainer;
import com.tuixin11sms.tx.activity.FriendManagerActivity;
import com.tuixin11sms.tx.callbacks.ILoginSuccess;
import com.tuixin11sms.tx.contact.CnToSpell;
import com.tuixin11sms.tx.contact.ContactAPI;
import com.tuixin11sms.tx.contact.ContactAPISdk5;
import com.tuixin11sms.tx.contact.FirstCharComparator;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.core.MsgHelper;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.core.MyPopupWindow.newAdapter;
import com.tuixin11sms.tx.dao.impl.BlogMsgImpl;
import com.tuixin11sms.tx.dao.impl.LikeNoticeImpl;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.db.TxDBContentProvider;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.engine.BlogOpea;
import com.tuixin11sms.tx.engine.ReleaseBlogOpea;
import com.tuixin11sms.tx.group.GifTransfer.GSLogonPara;
import com.tuixin11sms.tx.group.GifTransfer.GSServerProp;
import com.tuixin11sms.tx.group.GifTransfer;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.BlogNoticeMsg;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.model.StatusCode;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.sms.NotificationPopupWindow;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.TSLogonPara;
import com.tuixin11sms.tx.task.FileTransfer.TSServerProp;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsInfors;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsMeme;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

public class SessionManager {
	private static final String TAG = "SessionManager";

	public static final int OPT_OK = 1;// 操作成功
	public static final int OPT_FAILED = 2;// 操作失败

	private static SessionManager session;

	/** 为了兼容老版本遗留的几个字段 */
	// MORE_USER_251:存储的是多个账户的账号密码，账户之间用�#�分割，一个账户的账号密码之间用�分割 2013.12.23
	public static final String MORE_USER_251 = "more_user_251";// string
	public static final String AVATAR_URL_USER = "avatar_url_user";// String
	public static final String SEX_USER = "sex_user";// int
	public static final String IS_SAVE_PWD = "is_save_pwd"; // boolean
	public static final String PASSWORD = "password";// 这个字段存在时为了保证与3.8.7以前版本的兼容性，以后不再需要这个字段
														// 2013.12.20

	private static Context context;
	private SharedPreferences prefsMeme;
	private Editor editorMeme;

	public PrefsMeme mPrefMeme = null;// 存储个人账户相关的sp对象

	private Future userFuture;// 用户开启子线程读取数据库的future

	/** 登陆的账号可能不是神聊号，是手机号或邮箱，这里暂时记住用户输入的密码和是否记住密码状态，登陆成功后再去持久化 */
	private String tempLoginPwd = "";// 登陆界面输入的登陆密码
	// private boolean tempIsSavePwd = false;// 登陆界面是否勾选记住密码

	public UserLoginInforsMgr mUserLoginInfor;// 记录用户登录时账号密码信息的对象

	public UserEmojiInforsMgr mUserEmojiInforsMgr;
	/** 注册时小头像 */
	private Bitmap smallAvatar;

	/** 注册时大头像 */
	private Bitmap bigAvatar;

	private int mSex = TX.FEMAL_SEX;

	/**
	 * 推信号, 用于自动登录
	 */
	private String userid;
	// /**
	// * 密码, 用于自动登录
	// */
	// private String password;
	/**
	 * 当前登录令牌
	 */
	private String token;

	/**
	 * 标示是否登录成功
	 */
	private boolean loginSuccessed;

	/**
	 * 登陆模式为USER_LOGIN或AUTO_LOGIN等
	 */
	private LoginMode mode;

	/**
	 * 标识登陆时使用的账户类型，SHEN_LIAO_ACCOUNT或SINA_ACCOUNT
	 */
	private int accountType;
	/**
	 * 使用神聊账号登陆, accountType的候选值
	 */
	public static final int SHEN_LIAO_ACCOUNT = 0;
	/**
	 * 使用新浪微博账号登陆, accountType的候选值
	 */
	public static final int SINA_ACCOUNT = 1;

	/** 标识使用外部账号登陆时使用的授权类型 */
	private int authType;
	/**
	 * 无效的授权类型, authType的候选值
	 */
	public static final int INVALID_OATH = -1;
	/**
	 * xauth授权, authType的候选值
	 */
	public static final int XAUTH = 0;
	/**
	 * oauth1.0授权, authType的候选值
	 */
	public static final int OAUTH_ONE = 1;
	/**
	 * oauth2.0授权, authType的候选值
	 */
	public static final int OAUTH_TWO = 2;

	/**
	 * 其他账号的id
	 */
	private String weibo_user_id;
	/**
	 * 使用外部账号登陆时的token
	 */
	private String weibo_token;
	/**
	 * 使用外部账号登录时是否属于自动登录
	 */
	private boolean weiboAuto = true;

	/**
	 * 注册, 登录, 找回密码时的json字符串
	 */
	private String loginJson;

	public MsgHandler mMsgHandler;

	public AvatarDownload avatarDownload;
	Bitmap mHeadBoy, mHeadGirl;

	// 加载并缓存, 待优化
	public Bitmap cachePartnerDefault(long partner_id, int sex) {
		Bitmap bm = sex == TX.MALE_SEX ? mHeadBoy : mHeadGirl;
		avatarDownload.cachePartnerBitmapDir(partner_id, bm);
		return bm;
	}

	// 取默认头像
	public Bitmap getDefaultPartnerAvatar(int sex) {
		Bitmap bm = sex == TX.MALE_SEX ? mHeadBoy : mHeadGirl;
		return bm;
	}

	public static HandlerThread mgAsynloaderThread = null;
	public static HandlerThread mDBHandlerThread = null;

	// TODO 聊天室消息是否要缓存在这里
	public HashMap<Long, ArrayList<TXMessage>> lastMsgCatch = new HashMap<Long, ArrayList<TXMessage>>();

	private SessionManager(Context context) {
		this.context = context;
		if (mgAsynloaderThread == null || !mgAsynloaderThread.isAlive()) {
			mgAsynloaderThread = new HandlerThread("AsynloaderThread");
			mgAsynloaderThread.start();
		}
		if (mDBHandlerThread == null || !mDBHandlerThread.isAlive()) {
			mDBHandlerThread = new HandlerThread("AsyncDBThread");
			mDBHandlerThread.start();
		}
		prefsMeme = context.getSharedPreferences(PrefsMeme.MEME_PREFS,
				Context.MODE_PRIVATE);
		editorMeme = prefsMeme.edit();
		if (Utils.debug) {
			Log.i(TAG,
					"Login的SP中登陆的信息："
							+ context.getSharedPreferences(
									PrefsMeme.MEME_PREFS, Context.MODE_PRIVATE)
									.getString("user_login_infors", ""));
		}
		mPrefMeme = new PrefsMeme(context);

		avatarDownload = new AvatarDownload(context, this);

		mMsgHandler = new MsgHandler();// 给MsgHelper调用处理消息的方法

		mode = LoginMode.AUTO_LOGIN;
		loginSuccessed = false;

		// userid = prefsMeme.getString(CommonData.USER_ID, "");
		userid = mPrefMeme.user_id.getVal();
		String password = "";

		mUserLoginInfor = new UserLoginInforsMgr();// 先创建这个对象，后面要用到

		mUserEmojiInforsMgr = new UserEmojiInforsMgr();

		if (!mUserLoginInfor.hasNewLoginInfor()) {
			// 兼容3.8.7以前的版本
			password = prefsMeme.getString(PASSWORD, "");
			// CommonData.MORE_USER_251存储的是多个账户的账号密码，账户之间用�#�分割，一个账户的账号密码之间用�分割
			String moreUserInfors = prefsMeme.getString(MORE_USER_251, "");
			try {
				mUserLoginInfor
						.moveLoginInfor(userid, password, moreUserInfors);
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "转移登陆数据时异常", e);
				}
			}
		} else {
			// 3.8.7以后的新版本
			try {
				// 取出pwd
				mUserLoginInfor.loadSPToList();
				password = mUserLoginInfor.getCurrentPwd();
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "获取用户密码异常", e);
			}
		}

		// weibo_user_id = prefsMeme.getString(CommonData.WEIBO_USER_ID, "");
		// weibo_token = prefsMeme.getString(CommonData.WEIBO_TOKEN, "");
		// accountType = prefsMeme.getInt(CommonData.ACCOUNT_TYPE,
		// SHEN_LIAO_ACCOUNT);
		// authType = prefsMeme.getInt(CommonData.AUTH_TYPE, INVALID_OATH);
		// token = prefsMeme.getString(CommonData.TOKEN, "");
		weibo_user_id = mPrefMeme.weibo_user_id.getVal();
		weibo_token = mPrefMeme.weibo_token.getVal();
		accountType = mPrefMeme.account_type.getVal();
		authType = mPrefMeme.auth_type.getVal();
		token = mPrefMeme.token.getVal();

		int tempUid = 0;
		if (!TextUtils.isEmpty(userid)) {
			tempUid = Integer.parseInt(userid.trim());
		}

		initFileXfr(context, tempUid, token.getBytes());
		tm = new TXManager(context, mPrefMeme, mDownUpMgr);
		TX.tm = tm;// 把这个引用赋值给TX的tm

		// registerReceiver(context);// 注册广播

		if (!PrefsMeme.USER_EXIT.equals(mPrefMeme.exit.getVal())) {
			// 非手动退出状态
			if (accountType == SHEN_LIAO_ACCOUNT) {
				if (!Utils.isNull(userid) && !Utils.isNull(password)) {
					saveTempPwd(password);

					setAutoLoginInfor(userid, password);
					userLogin(userid);// 执行打开数据库读取TX的操作
				} else {
					loginJson = null;
				}
			} else {
				if (!Utils.isNull(weibo_user_id) && !Utils.isNull(weibo_token)
						&& authType != INVALID_OATH) {
					weiboAuto = true;
					setOtherAccountLoginInfor(weibo_user_id, weibo_token,
							accountType, authType);
				} else {
					loginJson = null;
				}
			}

		}
		mHeadBoy = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.user_infor_head_boy)).getBitmap();
		mHeadGirl = ((BitmapDrawable) context.getResources().getDrawable(
				R.drawable.user_infor_head_girl)).getBitmap();

		mPrefInfor = new PrefsInfors(context);

		mPrefInfor.initFiled(TX.tm.getUserID());// ①当直接登录时初始化②当没有登录时初始化，此时id为默认id(-1)，当登录成功后,再初始化正确的id---bobo

		// registLoginSuccessListener(new ILoginSuccess() {
		//
		// @Override
		// public void onLoginSuccess(long uid) {
		//
		// mPrefInfor.initFiled(TX.tm.getUserID());
		// }
		// });
	}

	/** UI调用的tx接口 */
	public ITxManager getTxMgr() {
		return tm;
	}

	public static SessionManager getInstance() {
		return session;
	}

	// 获取SessionManager对象
	public static SessionManager getManager(Context context) {
		if (session == null) {
			synchronized (SessionManager.class) {
				if (session == null) {
					session = new SessionManager(
							context.getApplicationContext());
				}
			}
		}
		return session;
	}

	/** 获取服务器当前时间 */
	public long getServerTime() {
		long serverTime = System.nanoTime() / (1000 * 1000 * 1000)
				+ mPrefMeme.st.getVal();
		return serverTime;

	}

	// /**
	// * 获得当前账号的密码
	// */
	// public synchronized String getPwd() {
	// return password;
	// }

	public synchronized String getWeiboUserUD() {
		return weibo_user_id;
	}

	public synchronized String getWeiboToken() {
		return weibo_token;
	}

	public synchronized int getAccountType() {
		return accountType;
	}

	public synchronized int getAuthType() {
		return authType;
	}

	public long getUserid() {
		if ("".equals(userid)) {
			// userid = prefsMeme.getString(CommonData.USER_ID, "");
			userid = mPrefMeme.user_id.getVal();
		}
		return userid.equals("") ? -1 : Long.parseLong(userid);
	}

	/**
	 * 设置当前账号的uid和密码, //注册成功或登录成功之后调用？2014.01.09
	 */
	public synchronized void setAutoLoginInfor(String userid, String pwd) {
		try {
			mPrefMeme.account_type.setVal(SHEN_LIAO_ACCOUNT);
			mPrefMeme.user_id.setVal(userid).commit();
			this.accountType = SHEN_LIAO_ACCOUNT;
			this.userid = userid;

			if (!Utils.isNull(userid) && !Utils.isNull(pwd)) {
				setLoginMode(LoginMode.AUTO_LOGIN);
				loginJson = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_LOGIN).key("u").value(userid)
						.key("p").value(pwd).endObject().toString();
			} else {
				loginJson = null;
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 用户选择其他账号登陆时调用该方法
	 */
	public synchronized void setOtherAccountLoginInfor(String weibo_user_id,
			String weibo_token, int accountType, int authType) {
		try {
			// editorMeme.putString(CommonData.WEIBO_USER_ID, weibo_user_id);
			// editorMeme.putString(CommonData.WEIBO_TOKEN, weibo_token);
			// editorMeme.putInt(CommonData.ACCOUNT_TYPE, accountType);
			// editorMeme.putInt(CommonData.AUTH_TYPE, authType);
			// editorMeme.commit();
			mPrefMeme.weibo_user_id.setVal(weibo_user_id);
			mPrefMeme.weibo_token.setVal(weibo_token);
			mPrefMeme.account_type.setVal(accountType);
			mPrefMeme.auth_type.setVal(authType).commit();

			this.weibo_user_id = weibo_user_id;
			this.weibo_token = weibo_token;
			this.accountType = accountType;
			this.authType = authType;

			if (!Utils.isNull(weibo_user_id) && !Utils.isNull(weibo_token)
					&& accountType != SHEN_LIAO_ACCOUNT
					&& authType != INVALID_OATH) {
				setLoginMode(LoginMode.OTHER_ACCOUNT_LOGIN);
				loginJson = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_LOGIN).key("u")
						.value(weibo_user_id).key("p").value(weibo_token)
						.key("t").value(accountType).key("auth")
						.value(authType).endObject().toString();
			} else {
				loginJson = null;
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 修改密码
	 */
	public synchronized void changePassword(String newPwd) {
		setAutoLoginInfor("" + getUserid(), newPwd);
	}

	/**
	 * 获取当前账号的token
	 */
	public synchronized String getToken() {
		if ("".equals(token)) {
			// token = prefsMeme.getString(CommonData.TOKEN, "");
			token = mPrefMeme.token.getVal();
		}
		return token;
	}

	/**
	 * 是否是第三方账号登录神聊 如新浪
	 * 
	 * @return true 是 false 不是
	 */
	public boolean isOtherAccount() {
		int type = mPrefMeme.account_type.getVal();
		if (type != SHEN_LIAO_ACCOUNT) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 设置当前账号的uid和token
	 */
	public synchronized void setUidAndToken(String uid, String token) {
		this.userid = uid;
		this.token = token;
		mTSLogonPara.setUserToken(Integer.parseInt(uid), token.getBytes());
		mGSLogonPara.setUserToken(Integer.parseInt(uid), token.getBytes());
		mPrefMeme.user_id.setVal(userid);
		mPrefMeme.token.setVal(token).commit();
		// tm.reloadTXMe();// /////

	}

	/**
	 * 设置注册时所需的信息, 注册之前调用
	 */
	public synchronized void setRegisterInfor(String registerName,
			String registerPassword, int sex) {
		try {
			this.mSex = sex;// 保存注册时用户的性别 2014.01.02
			setLoginMode(LoginMode.REGISTER_LOGIN);
			mPrefMeme.nickname.setVal(registerName);
			mPrefMeme.account_type.setVal(SHEN_LIAO_ACCOUNT).commit();
			TX.tm.reloadTXMe();// ////
			this.accountType = SHEN_LIAO_ACCOUNT;
			loginJson = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_REGSTER).key("nickname")
					.value(registerName).key("p").value(registerPassword)
					.endObject().toString();
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 设置登录时所需的用户信息, 用户点击登录时调用
	 */
	public synchronized void setLoginInfor(String userName, String userPassword) {
		try {
			setLoginMode(LoginMode.USER_LOGIN);
			// editorMeme.putInt(CommonData.ACCOUNT_TYPE, SHEN_LIAO_ACCOUNT);
			// editorMeme.commit();
			mPrefMeme.account_type.setVal(SHEN_LIAO_ACCOUNT).commit();
			this.accountType = SHEN_LIAO_ACCOUNT;
			loginJson = new JSONStringer().object().key("mt")
					.value(MsgInfor.CLENT_LOGIN).key("u").value(userName)
					.key("p").value(userPassword).endObject().toString();
		} catch (JSONException e) {
			if (Utils.debug)
				Log.e(TAG, "设置登录信息异常", e);
		}
	}

	/**
	 * 设置找回密码所需的用户信息, 找回密码之前调用 phoneOrEmailInfor参数为手机号, 或者邮箱账号 以是否包含@区分这两者
	 */
	public synchronized void setFindPasswordBackInfor(String phoneOrEmailInfor) {
		try {
			setLoginMode(LoginMode.FIND_PWD);
			if (phoneOrEmailInfor.contains("@")) {
				loginJson = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_PASSWORDGET).key("o").value("")
						.key("e").value(phoneOrEmailInfor).endObject()
						.toString();
			} else {
				loginJson = new JSONStringer().object().key("mt")
						.value(MsgInfor.CLENT_PASSWORDGET).key("o")
						.value(phoneOrEmailInfor).key("e").value("")
						.endObject().toString();
			}
		} catch (JSONException e) {
			if (Utils.debug)
				e.printStackTrace();
		}
	}

	/**
	 * 获取登录json String
	 */
	public synchronized String getLoginMsg() {
		if (loginJson == null) {
			return null;
		}
		String temp = new String(loginJson);
		if (mode == LoginMode.REGISTER_LOGIN || mode == LoginMode.FIND_PWD) {
			loginJson = null;
		}
		return temp;
	}

	/**
	 * 设置登录状态
	 */
	public synchronized void setLoginSuccessed(boolean successed) {
		loginSuccessed = successed;
	}

	/**
	 * 获取登录状态
	 */
	public synchronized boolean isLoginSuccessed() {
		return loginSuccessed;
	}

	/**
	 * 设置登录模式
	 */
	private synchronized void setLoginMode(LoginMode mode) {
		this.mode = mode;
	}

	/**
	 * 获取登录模式
	 */
	public synchronized LoginMode getLoginMode() {
		return mode;
	}

	public synchronized boolean isWeiboAuto() {
		return weiboAuto;
	}

	public synchronized void setWeiboAuto(boolean weiboAuto) {
		this.weiboAuto = weiboAuto;
	}

	/**
	 * 登录模式的枚举
	 */
	public static enum LoginMode {
		/**
		 * 注册登录, 该登录模式特指由用户点击注册按钮引发的注册行为 注册登录需要提供registerName和registerPassword
		 */
		REGISTER_LOGIN,
		/**
		 * 使用账号密码登录, 该登录模式特指由用户点击登录按钮引发的登录行为 账号可以是神聊号, 手机号, 邮箱
		 * 需要提供userName和userPassword
		 */
		USER_LOGIN,
		/**
		 * 找回密码, 该模式特指由用户点击找回密码按钮引发的登录行为 需要提供phoneOrEmailString, 包含
		 */
		FIND_PWD,
		/**
		 * 自动登录, 该模式包含启动应用程序时的自动登录, 以及首次连接被断开之后的自动后台登录 自动登录仅使用神聊号和密码登录
		 */
		AUTO_LOGIN,
		/**
		 * 用户选择其他账号登陆. 需要提供外部账号, 账号授权, 账号类型, 账号授权类型
		 */
		OTHER_ACCOUNT_LOGIN;
	}

	public Bitmap getSmallAvatar() {
		return smallAvatar;
	}

	public void setSmallAvatar(Bitmap smallAvatar) {
		this.smallAvatar = smallAvatar;
	}

	public Bitmap getBigAvatar() {
		return bigAvatar;
	}

	public void setBigAvatar(Bitmap bigAvatar) {
		this.bigAvatar = bigAvatar;
	}

	/**
	 * 销毁头像
	 */
	public void recyleAvatar() {
		if (smallAvatar != null) {
			smallAvatar.recycle();
		}
		if (bigAvatar != null) {
			bigAvatar.recycle();
		}
	}

	public int getSex() {
		return mSex;
	}

	public void setSex(int sex) {
		this.mSex = sex;
	}

	/** 存储所有监听登陆成功的监听者 */
	private Set<ILoginSuccess> loginSuccessListeners = new HashSet<ILoginSuccess>();

	private SocketHelper socketHelper;
	private static MsgHelper msgHelper;
	private getReceiver gr;
	// private ParseHandler parseHandler0;//优先级比较高
	// private ParseHandler parseHandler1;

	private MsgParser msgParser;

	private PraiseNoticeImpl pnd;

	private BlogOpea blogOpea;
	private ReleaseBlogOpea releaseblogOpea;
	private LikeNoticeImpl lnd;// 操作被喜欢的中间件
	private BlogMsgImpl bnd;

	/** 初始化helper */
	public void initHelper() {
		msgHelper = MsgHelper.CreateMsgHelper(this);

		// if (parseHandler0 == null) {
		// HandlerThread handlerThread = new HandlerThread("ParseMsg0");
		// handlerThread.start();
		// parseHandler0 = new ParseHandler(handlerThread.getLooper());
		// }

		if (msgParser == null) {
			msgParser = new MsgParser();
		}

		// 接受socketChannelIml分发的消息
		if (gr == null) {
			gr = new getReceiver();
			IntentFilter filter = new IntentFilter();
			filter.setPriority(1000);
			filter.addAction(Constants.INTENT_ACTION_RECEIVE_MSG);
			context.registerReceiver(gr, filter);
		}

		pnd = new PraiseNoticeImpl(this);
		blogOpea = new BlogOpea(this);
		releaseblogOpea = new ReleaseBlogOpea();
		lnd = new LikeNoticeImpl(this);
		bnd = new BlogMsgImpl(this);

		socketHelper = SocketHelper.getSocketHelper(this);// 准备做完后再去开启线程与服务器通信
															// 2014.05.27
	}

	/** 注册登陆成功的监听 */
	public void registLoginSuccessListener(ILoginSuccess loginListener) {
		loginSuccessListeners.add(loginListener);

	}

	// 供外部获取Context
	public Context getContext() {
		return context;
	}

	// 供外部获取ContentResolver
	public ContentResolver getContentResolver() {
		return context.getContentResolver();
	}

	public SocketHelper getSocketHelper() {
		return socketHelper;
	}

	// 获取操作被赞消息的dao
	public PraiseNoticeImpl getPraiseNoticeDao() {
		return pnd;
	}

	// 获取操作被赞消息的dao
	public LikeNoticeImpl getLikeNoticeDao() {
		return lnd;
	}

	// 获取瞬间操作类
	public BlogOpea getBlogOpea() {
		return blogOpea;
	}

	// 获取瞬间操作类
	public ReleaseBlogOpea getReleaseBlogOpea() {
		return releaseblogOpea;
	}

	// 获取瞬间操作类
	public BlogMsgImpl getBlogDao() {
		return bnd;
	}

	// 用户自动登陆或者登陆成功后对数据库的操作
	public void userLogin(String partner_id) {
		// 自动登陆的同时，打开数据库读本地信息，避免登录一直没响应时进入主界面是空白
		// final TxData txdata = (TxData) context.getApplicationContext();
		boolean hasOpenNewDB = TxDBContentProvider.creatDB(context, partner_id);
		if (hasOpenNewDB) {
			// 新打开了数据库,重新读取tx和msgstat
			if (Utils.debug) {
				Log.e(TAG, "数据库已打开，开始读取数据库中TX和会话msgStat");
			}

			// // 如果这个线程没有执行完毕，则取消，避免切换用户过快造成用户的数据不正确
			if (userFuture != null && !userFuture.isDone()) {
				userFuture.cancel(true);
			}
			userFuture = Utils.executorService.submit(new Runnable() {
				@Override
				public void run() {
					tm.initManager();
					MsgStat.filterTXList(context.getContentResolver());// 这个也放在登陆成功后执行合适吗？
				}
			});
		}
	}

	public TSLogonPara mTSLogonPara;
	public TSServerProp mTSServerProp;

	public GSLogonPara mGSLogonPara;
	public GSServerProp mGSServerProp;

	public FileTransfer mDownUpMgr;
	public GifTransfer mGifTransfer;

	private void initFileXfr(Context context, int uid, byte[] token) {
		mTSServerProp = new TSServerProp(Utils.AVATAR_SERVER,
				Utils.AVATAR_SERVER_PORT, 20000);
		mTSLogonPara = new TSLogonPara();
		mTSLogonPara.setUserToken(uid, token);
		mGSServerProp = new GSServerProp(Utils.GIF_SERVER,
				Utils.GIF_SERVER_PORT, 20000);
		mGSLogonPara = new GSLogonPara();
		mGSLogonPara.setUserToken(uid, token);
		mDownUpMgr = FileTransfer.Init(mTSServerProp, mTSLogonPara);
		mGifTransfer = GifTransfer.Init(mGSServerProp, mGSLogonPara);
		mDownUpMgr.mAppContext = context;
	}

	/** 返回与个人信息相关的SharedPrenference引用 */
	public SharedPreferences getPrefMeme() {
		return prefsMeme;
	}

	/** 返回与个人信息相关的Editor引用 */
	public Editor getEditorMeme() {
		return editorMeme;
	}

	/** 用户登陆时需要的信息 */
	public static class UserLoginInfor {
		public String partner_id;
		public String pwd;
		public int sex;// 0:男，1:女
		public String avatar_url;

		public UserLoginInfor() {
		}

		public UserLoginInfor(String uid, String pwd, int sex, String avatar_url) {
			this.partner_id = uid;
			this.pwd = pwd;
			this.sex = sex;
			this.avatar_url = avatar_url;
		}
	}

	public static class UserEmojiInfor {
		public String partner_id;
		public ArrayList<EmojiInfor> emojiList = new ArrayList<SessionManager.EmojiInfor>();

		public UserEmojiInfor() {

		}

		public UserEmojiInfor(String uid, List<EmojiInfor> data) {
			this.partner_id = uid;
			this.emojiList = (ArrayList<EmojiInfor>) data;
		}
	}

	/**
	 * emoji_path 是什么待定
	 * 
	 * @author SHC
	 * 
	 */
	public static class EmojiInfor {
		public String emoji_name;
		// 例如：rs.txf
		public String emoji_path;
		public String emoji_md5;
		public int emoji_id = -1;
		public int emoji_num = -1;

		public EmojiInfor() {

		}

		public EmojiInfor(String name, String path, String emoji_md5) {
			this.emoji_name = name;
			this.emoji_path = path;
			this.emoji_md5 = emoji_md5;
		}
	}

	/**
	 * 处理用户表情记录情况的类
	 * 
	 * @author SHC
	 * 
	 */
	public class UserEmojiInforsMgr {
		private final String EMOJIS = "emojis";
		// private ArrayList<UserEmojiInfor> emojiInforList = new
		// ArrayList<SessionManager.UserEmojiInfor>();
		private final String EMOJI_NAME = "emoji_name";
		private final String EMOJI_PATH = "emoji_path";
		private final String EMOJI_MD5 = "emoji_md5";
		private final String EMOJI_ID = "emoji_id";
		private final String EMOJI_NUM = "emoji_num";
		private final String USER_EMOJI_INFOS = "user_emoji_infos";
		private ArrayList<EmojiInfor> emojiList = new ArrayList<SessionManager.EmojiInfor>();

		private void saveListToSP(String current_userid, List<EmojiInfor> list)
				throws JSONException {
			JSONObject mainJosonobj = new JSONObject();
			JSONArray emojiArray = new JSONArray();
			// 获取当前用户的id
			// String user_id = mSess.mPrefMeme.user_id.getVal();
			mainJosonobj.put(current_userid, emojiArray);
			JSONObject emojiObject = null;
			for (EmojiInfor emojiInfor : list) {
				emojiObject = new JSONObject();
				emojiObject.put(EMOJI_NAME, emojiInfor.emoji_name);
				emojiObject.put(EMOJI_PATH, emojiInfor.emoji_path);
				emojiObject.put(EMOJI_MD5, emojiInfor.emoji_md5);
				emojiObject.put(EMOJI_NUM, emojiInfor.emoji_num);
				emojiObject.put(EMOJI_ID, emojiInfor.emoji_id);
				emojiArray.put(emojiObject);
			}
			editorMeme.putString(USER_EMOJI_INFOS, mainJosonobj.toString())
					.commit();
		}

		private void loadSPToList(String current_userid) throws JSONException {
			String emojiInfors = prefsMeme.getString(USER_EMOJI_INFOS, "");
			JSONObject maJsonObject = null;
			if (!TextUtils.isEmpty(emojiInfors)) {
				maJsonObject = new JSONObject(emojiInfors);
				// 获取当前帐号的emoji
				JSONArray curArray = maJsonObject.optJSONArray(current_userid);
				if (curArray != null) {
					emojiList.clear();
					JSONObject j = null;
					EmojiInfor emojiInfor = null;
					for (int i = 0; i < curArray.length(); i++) {
						j = curArray.optJSONObject(i);
						if (j != null) {
							emojiInfor = new EmojiInfor();
							emojiInfor.emoji_name = j.getString(EMOJI_NAME);
							emojiInfor.emoji_path = j.getString(EMOJI_PATH);
							emojiInfor.emoji_md5 = j.getString(EMOJI_MD5);
							emojiInfor.emoji_id = j.getInt(EMOJI_ID);
							emojiInfor.emoji_num = j.getInt(EMOJI_NUM);
							emojiList.add(emojiInfor);
						}

					}
				}
			}
		}

		/**
		 * 由子地址到神聊本地上找到
		 * 
		 * @param path
		 * @return
		 */
		public String getShenLiaoLocalGifPath(String path) {
			if (Environment.getExternalStorageState().equals(
					Environment.MEDIA_MOUNTED)) {
				String base = Environment.getExternalStorageDirectory()
						.getPath() + "/shenliao/gif_package/";
				return base + path;
			}
			return null;
		}

		// 更新或保存当前用户所持有的表情包
		public void saveOrUpdateCurUserEmoji(String current_userid,
				List<EmojiInfor> list) throws JSONException {
			saveListToSP(current_userid, list);
		}

		// 获取当前用户所持有的表情包集合
		public ArrayList<EmojiInfor> getCurUserEmoji(String current_userid)
				throws JSONException {
			loadSPToList(current_userid);
			return emojiList;
		}

	}

	/** 处理用户登陆时账号密码记录情况的类 */
	public class UserLoginInforsMgr {
		private final String USER_LOGIN_INFORS = "user_login_infors";// 在SharedPreference中存储的用户账号密码头像的key
		private final String USERS = "users";// 所有登录过的用户的账户集合的key
		// private final String USER_IDS = "userIds";//所有登录过的用户的账户id集合的key
		private final String CURRENT_USER = "current_user";// 记录当前登陆成功用户的账号密码的key
		private final String USERID = "userid";// 在sp中存储的神聊号的key
		private final String PWD = "pwd";// 在sp中存储的密码的key
		private final String AVATAR_URL = "avatar_url";// 在sp中存储的头像的key
		private final String SEX = "sex";// 在sp中存储的用户性别的key

		private String currentUid = null;// 当前登陆的用户id
		private String currentPwd = null;// 当前登陆的用户密码
		private ArrayList<UserLoginInfor> userInforList = new ArrayList<SessionManager.UserLoginInfor>();

		/** 把用户的登陆信息保存到sp中 */
		private void saveListToSP() throws JSONException {
			JSONObject mainJosonobj = new JSONObject();
			JSONArray userArray = new JSONArray();
			mainJosonobj.put(USERS, userArray);
			JSONObject currentUserJsonObj = null;
			for (UserLoginInfor usrInfor : userInforList) {
				currentUserJsonObj = new JSONObject();
				currentUserJsonObj.put(USERID, usrInfor.partner_id);
				currentUserJsonObj.put(PWD, usrInfor.pwd);
				currentUserJsonObj.put(AVATAR_URL, usrInfor.avatar_url);
				currentUserJsonObj.put(SEX, usrInfor.sex);
				userArray.put(currentUserJsonObj);
			}
			currentUserJsonObj = new JSONObject();
			currentUserJsonObj.put(USERID, currentUid);
			currentUserJsonObj.put(PWD, currentPwd);
			mainJosonobj.put(CURRENT_USER, currentUserJsonObj);

			editorMeme.putString(USER_LOGIN_INFORS, mainJosonobj.toString())
					.commit();
		}

		/** 把SharedPreference中的json信息加载到list */
		private void loadSPToList() throws JSONException {
			String userInfors = prefsMeme.getString(USER_LOGIN_INFORS, "");
			JSONObject mainJosonobj = null;
			if (!TextUtils.isEmpty(userInfors)) {
				mainJosonobj = new JSONObject(userInfors);

				// 保存当前账号信息
				JSONObject currObj = mainJosonobj.optJSONObject(CURRENT_USER);
				if (currObj != null) {
					currentUid = currObj.getString(USERID);
					currentPwd = currObj.getString(PWD);
				}

				JSONArray userArray = mainJosonobj.optJSONArray(USERS);// 所有账号的数组
				if (userArray != null) {
					userInforList.clear();// 清空登陆信息的list
					JSONObject jobj = null;
					UserLoginInfor usrInfor = null;

					// 保存登陆信息集合序列
					for (int i = 0, size = userArray.length(); i < size; i++) {
						jobj = userArray.optJSONObject(i);
						if (jobj != null) {
							usrInfor = new UserLoginInfor();
							usrInfor.partner_id = jobj.getString(USERID);
							usrInfor.avatar_url = jobj.getString(AVATAR_URL);
							usrInfor.pwd = jobj.getString(PWD);
							usrInfor.sex = jobj.getInt(SEX);
							userInforList.add(usrInfor);
						}
					}
				}
			}
		}

		/**
		 * 更新SharedPreference中的用户密码和头像(pwd和avatar_url任一值为空，则代表跳过此字段的判定和修改阶段)
		 * 
		 * @return
		 */
		private boolean saveUserInfors(String partner_id, String pwd, int sex,
				String avatar_url) throws JSONException {
			if (TextUtils.isEmpty(partner_id)
					|| ((TextUtils.isEmpty(pwd) || pwd.length() < 6) && avatar_url == null)) {
				// partner为空，或者两个值都为null则不去执行任何操作
				return false;
			}

			// 设置当前登陆的账号密码
			currentUid = partner_id;
			currentPwd = pwd;

			UserLoginInfor curUsrInfor = null;
			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					// 找到了对应的id
					curUsrInfor = usrInfor;
					usrInfor.sex = sex;
					usrInfor.pwd = pwd;
					if (avatar_url != null) {
						usrInfor.avatar_url = avatar_url;
					}
				}
			}

			if (curUsrInfor != null) {
				// 把当前用户信息对象移到第一位
				userInforList.remove(curUsrInfor);
				userInforList.add(0, curUsrInfor);
			} else {
				// 新用户，则添加到list中
				UserLoginInfor usrInfor = new UserLoginInfor();
				usrInfor.partner_id = partner_id;
				usrInfor.pwd = pwd;
				usrInfor.sex = sex;
				usrInfor.avatar_url = avatar_url;
				userInforList.add(0, usrInfor);
			}

			saveListToSP();
			return true;
		}

		/** 更新SharedPreference中的用户密码 */
		public void saveRegistUserPwds(String partner_id, String pwd)
				throws JSONException {
			saveUserInfors(partner_id, pwd, mSex, "");
			onLoginSuccess(partner_id);
		}

		/** 用户登录成功后保存登陆信息 */
		public void saveLoginSuccessUserInfor(String partner_id,
				String avatarUrl) throws JSONException {
			mUserLoginInfor.saveUserInfors(partner_id, tempLoginPwd, mSex,
					avatarUrl);
			// 这里不能重置临时的密码和记住账号状态，因为在app使用过程中可能会出现多次自动的登录，收到多次的登录成功的返回
			// 登陆成功后就会执行此方法，那么此时的tempLoginPwd为空，tempIsSavePwd是false,会把原来账号的正常状态修改掉，这里待修改
			// 2014.01.09 shc
			// tempLoginPwd = "";
			// tempIsSavePwd = false;

			onLoginSuccess(partner_id);
		}

		/** 登录成功调用的方法 */
		public void onLoginSuccess(String partnerId) {
			long uid = Long.parseLong(partnerId);
			Iterator<ILoginSuccess> iter = loginSuccessListeners.iterator();
			while (iter.hasNext()) {
				ILoginSuccess listener = iter.next();
				listener.onLoginSuccess(uid);
			}
		}

		/** 更新当前登录的账号密码 */
		public void updateUserPwd(String partner_id, String pwd)
				throws JSONException {
			if (TextUtils.isEmpty(partner_id)
					|| (TextUtils.isEmpty(pwd) || pwd.length() < 6)) {
				// partner或者pwd无效则不去执行任何操作
				return;
			}

			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					if (TextUtils.isEmpty(usrInfor.pwd)
							&& usrInfor.pwd.length() >= 6) {
						// 密码不为空且大于6位，代表用户记住了密码
						usrInfor.pwd = pwd;
					}
					if (currentUid.equals(partner_id)) {
						currentPwd = pwd;
					}
					saveListToSP();
				}
			}
		}

		/** 修改用户的头像url */
		public void updateUserAvatarSex(long partner_id, String avatar_url)
				throws JSONException {
			if (!Utils.isIdValid(partner_id)) {
				// partner无效则不去执行任何操作
				return;
			}

			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					if (avatar_url != null) {
						usrInfor.avatar_url = avatar_url;
					}
					saveListToSP();
				}
			}
		}

		/** 获取某个用户的密码 */
		public String getUserLoginPwd(String partner_id) throws JSONException {
			if (TextUtils.isEmpty(partner_id)) {
				// partner无效则不去执行任何操作
				return null;
			}

			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					return usrInfor.pwd;
				}
			}

			return null;

		}

		/** 登陆界面获取用户的密码 */
		public String getCurrentPwd() throws JSONException {
			return currentPwd;
		}

		// /** LoginSessionManager构造函数获取当前用户是否记住密码 */
		// public boolean getCurrentIsSavePwd(String partner_id) throws
		// JSONException {
		// for(UserLoginInfor usrInfor : userInforList){
		// if(usrInfor.partner_id.equals(partner_id)){
		// //找到了对应的用户
		// return !TextUtils.isEmpty(usrInfor.pwd);
		// }
		// }
		// return false;//没有找到用户设置为不记住密码
		// }

		/** 获取用户登陆信息的list */
		public ArrayList<UserLoginInfor> getUserInforList() {
			return userInforList;
		}

		/** 3.8.7以后的版本才有此字段 */
		public boolean hasNewLoginInfor() {
			return prefsMeme.contains(USER_LOGIN_INFORS);
		}

		/** 转移旧的登陆信息格式为新的格式 */
		public void moveLoginInfor(String crtId, String crtPwd,
				String moreUserInfors) throws JSONException {

			if (!TextUtils.isEmpty(moreUserInfors)) {
				String[] userpwds = moreUserInfors.split("�#�");
				boolean isSavePwd = false;
				String userAvatarUrl = "";
				int sex = TX.MALE_SEX;
				UserLoginInfor usrInfor = null;
				for (String idPwd : userpwds) {
					String[] idPwdArray = idPwd.split("�");
					if (idPwdArray.length == 2) {
						isSavePwd = prefsMeme.getBoolean(IS_SAVE_PWD
								+ idPwdArray[0], false);
						userAvatarUrl = prefsMeme.getString(AVATAR_URL_USER
								+ idPwdArray[0], "");
						sex = prefsMeme.getInt(SEX_USER + idPwdArray[0], 0);
						usrInfor = new UserLoginInfor(idPwdArray[0],
								isSavePwd ? idPwdArray[1] : "", sex,
								userAvatarUrl);
						userInforList.add(usrInfor);
					}
				}
				// 转移当前的账号密码
				currentUid = crtId;
				currentPwd = crtPwd;

				saveListToSP();

			} else {
				if (Utils.debug) {
					Log.e(TAG, "个人账号密码信息为空");
				}
			}
		}

		/**
		 * 删除已登录账号的相关信息
		 * 
		 * @return 是否删除成功
		 */
		public boolean removeLoginUserInfor(String partner_id)
				throws JSONException {
			if (TextUtils.isEmpty(partner_id)) {
				// partner不合法，则不去执行任何操作
				return false;
			}

			UserLoginInfor removeUser = null;// 待删除用户的信息对象
			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					removeUser = usrInfor;
				}
			}
			if (removeUser != null) {
				userInforList.remove(removeUser);
				// TODO 这个操作有必要吗？2014.01.02
				if (removeUser.partner_id.equals(currentUid)) {
					currentUid = "";
					currentPwd = "";
				}
				saveListToSP();
				return true;
			}
			return false;
		}

		/**
		 * 删除一个账号的密码
		 * 
		 * @return 是否删除成功
		 */
		public boolean clearUserPwd(String partner_id) throws JSONException {
			if (TextUtils.isEmpty(partner_id)) {
				// partner不合法，则不去执行任何操作
				return false;
			}

			UserLoginInfor targetUser = null;// 待删除用户的信息对象
			for (UserLoginInfor usrInfor : userInforList) {
				if (usrInfor.partner_id.equals(partner_id)) {
					targetUser = usrInfor;
				}
			}
			if (targetUser != null) {
				targetUser.pwd = "";
				// TODO 这个操作有必要吗？2014.01.02
				if (targetUser.partner_id.equals(currentUid)) {
					currentUid = "";
					currentPwd = "";
				}
				saveListToSP();
				return true;
			}
			return false;
		}
	}

	/** 临时存储登陆页面的密码和是否保存密码状态 */
	public void saveTempPwd(String pwd) {
		tempLoginPwd = pwd;
	}

	/** 预处理登陆返回，登录成功后调用 */
	public void setLoginSuccessInfo(String partner_id) {
		mPrefMeme.exit.setVal("").commit();// 登陆成功，设置手动退出为false 2014.03.06 shc
		setAutoLoginInfor(partner_id, tempLoginPwd);
	}

	// 从TxData中挪过来的
	public void uploadSinaFriend() {
		Utils.executorService.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String userId = prefsMeme.getString(CommonData.WEIBO_USER_ID
						+ "�" + TX.tm.getUserID(), "");
				if (Utils.isNull(userId))
					return;
				// long lastTime =
				// prefsMeme.getLong(CommonData.WEIBO_UPLOAD_LAST_TIME, 0);
				long lastTime = mPrefMeme.weibo_upload_last_time.getVal();
				long day = 1000 * 60 * 60 * 24;
				long now = System.currentTimeMillis();
				if ((now > lastTime + day) && !"".equals(userId)) {
					// prefsMeme.edit().putLong(CommonData.WEIBO_UPLOAD_LAST_TIME,
					// now).commit();
					mPrefMeme.weibo_upload_last_time.setVal(now).commit();
					String token = prefsMeme.getString(CommonData.WEIBO_TOKEN
							+ "�" + TX.tm.getUserID(), "");
					String tokenSecret = prefsMeme.getString(
							CommonData.WEIBO_TOKEN_SECRET + "�"
									+ TX.tm.getUserID(), "");
					if (!Utils.isNull(token) && !Utils.isNull(tokenSecret)) {
						// TODO 空实现？2014.04.23 shc
					}
				}
			}
		});
	}

	// 获取资源对象
	public static Resources getResources() {
		return context.getResources();
	}

	// 实现一个 BroadcastReceiver，用于接收指定的 Broadcast
	private class getReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			if (Utils.debug) {
				Log.e(TAG, "Service收到了广播，执行了onReceive()方法");
			}
			final String msg = intent.getStringExtra("msg");
			if (msg.trim().length() != 0) {
				if (Utils.debug) {
					Log.i(TAG, "msg字符串长度>0，开始处理");
				}
				int msgType = Utils.getMessageType(msg);

				msgParser.parseMsg(msgType, msg);

				// if (msgType == MsgInfor.SERVER_INFO_QUN
				// || msgType == MsgInfor.SERVER_USERINFRO) {
				// if (Utils.debug) {
				// Log.i(TAG, "获取群组信息结果|||或者|||返回用户基本信息，不是登陆响应");
				// }
				// // parseTask.putData(msg);
				// if (parseHandler0 != null) {
				// Message message = parseHandler0.obtainMessage();
				// message.obj = msg;
				// parseHandler0.sendMessage(message);
				// }
				// } else {
				// if (Utils.debug) {
				// Log.i(TAG, "其他的服务器响应，具体是什么就不知道了。");
				// }
				// parseHandler0.post(new Runnable() {
				//
				// public void run() {
				// setDefaultDisplay();// 处理前先设置一下分辨率，因为可能是上传图片，需要这个操作
				//
				// if (Utils.debug) {
				// try {
				// int type = 0;
				// JSONObject jo = new JSONObject(msg);
				// if (jo != null)
				// type = jo.getInt("mt");
				// if (type == MsgInfor.SERVER_LOGIN) {
				// Log.e(TAG,
				// "在getReceiver广播中调用了msghelper.dealMsg(msg);是登陆成功返回的消息哦！！！！");
				// } else {
				// Log.i(TAG, "不是登陆成功，这个类型type=" + type);
				// }
				// } catch (JSONException e) {
				// if (Utils.debug) {
				// Log.e(TAG, "格式化Json异常", e);
				// }
				// }
				// }
				// msgHelper.dealMsg(msg);
				// }
				//
				// });
				// }
			} else {
				if (Utils.debug) {
					Log.e(TAG, "msg字符串长度为0，这是怎么回事儿？？？");
				}
			}
		}
	}

	private static final class ParseHandler extends Handler {
		private int handleCount;

		public ParseHandler(Looper looper) {
			super(looper);
		}

		@Override
		public void handleMessage(Message message) {
			if (message.obj != null) {
				handleCount++;
				String msg = message.obj.toString();
				setDefaultDisplay();// 处理前先设置一下分辨率，因为可能是上传图片，需要这个操作
				if (Utils.debug) {
					try {
						JSONObject jo = new JSONObject(msg);
						if (jo != null
								&& jo.getInt("mt") == MsgInfor.SERVER_LOGIN) {
							Log.e(TAG, "是登陆成功返回的消息！--ParseHandler");
						}
					} catch (JSONException e) {
						// 外面已经有了一个if(Utils.debug)判断
						e.printStackTrace();
					}

				}
				msgHelper.dealMsg(msg);
				if (Utils.debug)
					Log.i(TAG, "handleCount is:" + handleCount);
			}
		}

	}

	/** 设置默认分辨率值，因为有可能需要上传图片，需要这个值 */
	private static void setDefaultDisplay() {
		DisplayMetrics dm = new DisplayMetrics();
		dm = getResources().getDisplayMetrics();
		Utils.SreenW = dm.widthPixels;
		Utils.SreenH = dm.heightPixels;
	}

	private TXManager tm = null;

	private long flushDataTime = 0;// 刷新数据的时间，被赞的个数不能来一个写到rom一个，需要定时刷新写入
	private long FLUSH_SPACE_TIME = 1 * 60 * 1000;// 两次数据刷新的间隔时间

	/** 与当前账户相关的杂信息 */
	public PrefsInfors mPrefInfor;

	/** TX管理者 */
	public static class TXManager implements ITxManager {

		private final String TAG = "TXManager";

		// TX 陌生神聊用户, 最多1000人
		private LruCache<Long, TX> mSTTXCache = new LruCache<Long, TX>(1000);
		// 存储手机本地通讯录联系人，不限长度,key为phone
		private HashMap<Long, String> mCSCache = new HashMap<Long, String>();

		// 存储所有与用户相关的信息对象
		private HashMap<Long, TxInfor> mTxInforCacheMap = new HashMap<Long, TxInfor>();

		// private static AtomicInteger blackListPageNum = new AtomicInteger(0);

		private Context mContext = null;
		private ContentResolver mTxCr = null;

		// private LoginSessionManager mSess = null;
		private PrefsMeme mPrefMeme = null;
		private FileTransfer mDownUpMgr = null;

		private static TX tx_man;// 神聊管家
		private static TX tx_friend;// 好友管家
		private static TX sl_notice;// 群组动态
		private static TX sl_safe;// 神聊小卫士
		private static TX tx_me;// 自身资料

		public static final int TxDBAsyncRead = 1;
		private Handler mTxDBAsyncHandler = new Handler(
				mDBHandlerThread.getLooper()) {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					TX tx = getSTTX(msg.arg1, true);
					if (tx != null) {
						@SuppressWarnings("unchecked")
						WeakReference<Handler> weakhdlr = (WeakReference<Handler>) msg.obj;
						Handler hdlr = weakhdlr != null ? weakhdlr.get() : null;
						if (hdlr != null)
							hdlr.obtainMessage(TxDBAsyncRead, tx)
									.sendToTarget();
					}
					break;
				}
			};
		};

		private TXManager(Context context, PrefsMeme prefMeme,
				FileTransfer downupMgr) {
			if (mContext == null) {
				this.mContext = context;
				this.mTxCr = context.getContentResolver();
				// mSess = LoginSessionManager.getManager(context);
				mPrefMeme = prefMeme;
				mDownUpMgr = downupMgr;
			}
		}

		private void initManager() {

			clearTXCache();

			if (mCSCache.isEmpty()) {
				ContactAPI conApi = new ContactAPISdk5();
				conApi.fillAllContacts(mTxCr, mCSCache);
			}

			// 读取神聊数据库神聊好友
			if (Utils.debug)
				Log.e(TAG, "开始读取神聊数据库中的神聊好友");
			Cursor cur = mTxCr.query(TxDB.TX_Friends.CONTENT_URI, null, null,
					null, null);
			if (cur != null) {
				ArrayList<TxInfor> tem = fetchAllTxInfors(cur);
				cur.close();
				if (Utils.debug)
					Log.e(TAG, "从数据库中查到的好友总数为：" + tem.size());
				for (TxInfor tinfor : tem) {
					// 添加到好友缓存中
					mTxInforCacheMap.put(tinfor.getPartner_id(), tinfor);
					TX txx = findTXFromDB(tinfor.getPartner_id());
					if (txx != null) {
						txx.setTxInfor(tinfor);
						addSTTX(txx);
						// tinfor.setTxNormal(txx);
					}
				}
			} else {
				if (Utils.debug)
					Log.e(TAG, "查找好友的游标为空！！！太恐怖了！！！");
				// 这时如果再调用cur.moveToNext()不会异常，FutureTask给捕获了。
			}

			getLocalGroups();
		}

		// public int getBlackListPageNum() {
		// return blackListPageNum.getAndIncrement();
		// }
		//
		// public void resetBlackListPageNum() {
		// blackListPageNum.set(0);
		// }

		/** 清空存储所有与当前账号相关的神聊用户cache */
		public void clearTXCache() {
			if (Utils.debug) {
				Log.i(TAG,
						"clearTXCache,客服在缓存对象=" + mSTTXCache.get(TX.TUIXIN_MAN));
			}
			mSTTXCache.evictAll();
			mTxInforCacheMap.clear();
			tx_me = null;
		}

		private ArrayList<TxGroup> getLocalGroups() {
			ArrayList<TxGroup> tmp = new ArrayList<TxGroup>();
			ArrayList<TxGroup> tm = new ArrayList<TxGroup>();
			Cursor cur = mTxCr.query(TxDB.Qun.CONTENT_URI, null,
					TxDB.Qun.QU_TX_STATE + " <> ? ",
					new String[] { String.valueOf(TxDB.QU_TX_STATE_OUT) },
					TxDB.Qun.QU_ID);
			if (cur != null) {
				tmp = TxGroup.fetchAllDBGroups(cur);
				cur.close();
			}
			for (TxGroup txgroup : tmp) {
				if (Utils.debug)
					Log.i(TAG, "getLocalGroups" + txgroup.toString());
				boolean add = true;
				for (TxGroup txgroup1 : tm) {
					if (txgroup1.group_id == txgroup.group_id) {
						add = false;
						break;
					}
				}
				if (add)
					tm.add(txgroup);
			}
			return tm;
		}

		// 邀请手机好友的acivity(InviteContactsActivity)需要
		public final HashMap<Long, TX> getTBTXCache() {
			HashMap<Long, TX> txcache = new HashMap<Long, TX>();

			Set<Entry<Long, TxInfor>> tinforSet = mTxInforCacheMap.entrySet();
			Iterator<Entry<Long, TxInfor>> tinforIt = tinforSet.iterator();
			while (tinforIt.hasNext()) {
				Entry<Long, TxInfor> tbEntry = tinforIt.next();
				TxInfor tbtx = tbEntry.getValue();
				if (!tbtx.isBlackType() && tbtx.isTBType()) {
					// 神聊好友
					TX tx = getTx(tbEntry.getKey());
					if (tx != null) {
						// 神聊好友
						txcache.put(tx.partner_id, tx);
					}
				}
			}

			return txcache;
		}

		public final HashMap<Long, String> getContactsCache() {
			return mCSCache;
		}

		// 同步mCSCache和神聊好友的本地联系人字段
		public void syncTBTXs() {
			Set<Long> tbSet = mTxInforCacheMap.keySet();
			Iterator<Long> tbIter = tbSet.iterator();
			TX tx = null;
			while (tbIter.hasNext()) {
				Long tinforId = tbIter.next();
				if ((tx = getTx(tinforId)) != null) {
					if (!TextUtils.isEmpty(tx.getPhone())) {
						// 电话号码不为空
						Long tbPhone = Long.parseLong(tx.getPhone());
						String csName = mCSCache.get(tbPhone);
						if (!TextUtils.isEmpty(csName)) {
							// 这样赋值有效，tbtx取到的是引用，不是拷贝 2014.01.21 shc
							mTxInforCacheMap.get(tinforId)
									.setContacts_person_name(csName);
						}
					}
				}
			}
		}

		/** 添加好友id */
		// 好友id集合应该只保存在内存中，不与数据库相关
		public void addTBTXId(long partner_id, int isStarFriend) {
			TxInfor tinfor = null;
			if (!mTxInforCacheMap.containsKey(partner_id)) {
				tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
				tinfor.setStarFriend(isStarFriend);
				mTxInforCacheMap.put(partner_id, tinfor);

				ContentValues values = tinfor.txinforToValues();
				if (updateTxInforByTXId(values, partner_id) == 0) {
					mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
				}

			} else {
				tinfor = mTxInforCacheMap.get(partner_id);
				if (tinfor != null) {
					tinfor.setStarFriend(isStarFriend);

					ContentValues values = new ContentValues();
					values.put(TxDB.TX_Friends.IS_STAR_FRIEND, isStarFriend);
					if (updateTxInforByTXId(values, partner_id) == 0) {
						mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
					}
				}
			}

			TX tx = getTx(partner_id);
			if (tx != null) {
				tx.setTxInfor(tinfor);
			}

		}

		/** 添加好友备注名 */
		// 先取好友备注名，后取好友详细资料
		public void addTBTXRemarkName(long partner_id, String remarkName) {

			TxInfor tinfor = null;
			if (!mTxInforCacheMap.containsKey(partner_id)) {
				tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
				tinfor.setRemarkName(remarkName);
				mTxInforCacheMap.put(partner_id, tinfor);

				ContentValues values = tinfor.txinforToValues();
				if (updateTxInforByTXId(values, partner_id) == 0) {
					mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
				}

			} else {
				tinfor = mTxInforCacheMap.get(partner_id);
				if (tinfor != null) {
					tinfor.setRemarkName(remarkName);

					ContentValues values = new ContentValues();
					values.put(TxDB.TX_Friends.REMARK_NAME, remarkName);
					if (updateTxInforByTXId(values, partner_id) == 0) {
						mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
					}
				}
			}

			TX tx = getTx(partner_id);
			if (tx != null) {
				tx.setTxInfor(tinfor);
			}

		}

		// 更新好友的字段
		private TX updateTXByValues(TX tx, ContentValues values) {
			if (tx != null) {
				Set<Entry<String, Object>> valueSet = values.valueSet();
				Iterator<Entry<String, Object>> txIterator = valueSet
						.iterator();
				while (txIterator.hasNext()) {
					Entry<String, Object> tbEntry = txIterator.next();
					String key = tbEntry.getKey();
					Object value = tbEntry.getValue();

					if (key.equals(TxDB.Tx.TX_ID)) {
						// 神聊号
						tx.partner_id = (Long) value;

					} else if (key.equals(TxDB.Tx.DISPLAY_NAME)) {
						// 神聊联系人昵称名称
						String nickName = (String) value;
						tx.setNick_name(nickName);
						tx.setNickNamePinyin(CnToSpell.getFullSpell(nickName));// 昵称的拼音用昵称现生成

					} else if (key.equals(TxDB.Tx.AVATAR_MD5)) {
						// 头像版本号
						tx.setAvatar_ver((Integer) value);

					} else if (key.equals(TxDB.Tx.AVATAR_URL)) {
						// 头像网络路径
						String oldAvatarUrl = tx.getAvatar_url();
						if (!oldAvatarUrl.equals((String) value)) {
							// 新头像地址和老地址不一样，删除头像缓存和本地已有的头像大小图，再更新头像地址字段
							// 删除缓存
							if (AvatarDownload
									.removeTXHeadImgCache(tx.partner_id)) {
								if (Utils.debug)
									Log.e(TAG, tx.partner_id + " 头像内存缓存删除成功");
							} else {
								if (Utils.debug)
									Log.e(TAG, "没有找到" + tx.partner_id
											+ " 头像内存缓存");
							}

							// 删除本地SD卡大小图
							String filePath = mDownUpMgr.getAvatarFile(
									oldAvatarUrl, tx.partner_id, false);
							File headFile = null;
							if (filePath != null) {
								headFile = new File(filePath);
								if (headFile.exists()) {
									headFile.delete();
								}
							}
							filePath = mDownUpMgr.getAvatarFile(oldAvatarUrl,
									tx.partner_id, true);
							if (filePath != null) {
								headFile = new File(filePath);
								if (headFile.exists()) {
									headFile.delete();
								}
							}

							tx.setAvatar_url((String) value);
						}

					} else if (key.equals(TxDB.Tx.SEX)) {
						// 性别
						tx.setSex((Integer) value);

					} else if (key.equals(TxDB.Tx.BIRTHDAY)) {
						// 生日
						tx.setBirthday((Integer) value);

					} else if (key.equals(TxDB.Tx.BLOOD_TYPE)) {
						// 血型
						tx.setBloodType((Integer) value);

					} else if (key.equals(TxDB.Tx.HOBBY)) {
						// 爱好
						tx.setHobby((String) value);

					} else if (key.equals(TxDB.Tx.PROFESSION)) {
						// 职业
						tx.setJob((String) value);

					} else if (key.equals(TxDB.Tx.HOME)) {
						// 所在地
						tx.setArea((String) value);

					} else if (key.equals(TxDB.Tx.TX_SIGN)) {
						// 个性签名
						tx.setSign((String) value);

					} else if (key.equals(TxDB.Tx.DISTANCE)) {
						// 距离
						tx.setDistance((Integer) value);

					} else if (key.equals(TxDB.Tx.AGE)) {
						// 年龄
						tx.setAge((Integer) value);

					} else if (key.equals(TxDB.Tx.CONSTELLATION)) {
						// 星座
						tx.setConstellation((String) value);

					} else if (key.equals(TxDB.Tx.PHONE)) {
						// 手机号
						tx.setPhone((String) value);

					} else if (key.equals(TxDB.Tx.EMAIL)) {
						// 邮箱
						tx.setEmail((String) value);

					} else if (key.equals(TxDB.Tx.IS_P_BIND)) {
						// 手机号绑定
						tx.setPhoneBind((Boolean) value);

					} else if (key.equals(TxDB.Tx.IS_E_BIND)) {
						// 邮箱绑定
						tx.setEmailBind((Boolean) value);

					} else if (key.equals(TxDB.Tx.SECOND_CHAR)) {
						// 神聊联系排序字符
						// tx.setNickNamePinyin((String)value);//昵称的拼音在设置昵称时自动生成，不直接设置了

					} else if (key.equals(TxDB.Tx.ALBUM)) {
						// 相册
						String album = (String) value;
						ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
						if (!Utils.isNull(album)) {
							for (String url : album
									.split(Constants.STRING_SEPARATOR)) {
								AlbumItem ai = new AlbumItem();
								ai.setUrl(url);
								list.add(ai);
							}
						}
						tx.setAlbum(list);

					} else if (key.equals(TxDB.Tx.LANGUAGES)) {
						// 语言
						tx.setLanguages((String) value);

					} else if (key.equals(TxDB.Tx.ISOP)) {
						// 是否是管理员
						tx.setIsop((Integer) value);

					} else if (key.equals(TxDB.Tx.ALBUM_VER)) {
						// 相册版本
						tx.setAlbumVer((Integer) value);

					} else if (key.equals(TxDB.Tx.INFO_VER)) {
						// 信息版本
						tx.setInfoVer((Integer) value);

					} else if (key.equals(TxDB.Tx.IS_RECEIVE_REQ)) {
						// 是否接收好友请求
						tx.setReceiveReq(((Integer) value) == 0 ? true : false);
					} else if (key.equals(TxDB.Tx.BLOG_INFOR)) {
						// s瞬间信息
						tx.setBlog_head_msg((String) value);
					} else if (key.equals(TxDB.Tx.LEVLE)) {
						// 等级信息
						tx.setLevel((Integer) value);
					}
				}
				// 缓存更新完毕，开始更新数据库
				updateTxByTXId(values, tx.partner_id);

			}
			return tx;

		}

		// 添加陌生神聊联系人
		private boolean addSTTX(TX tx) {
			if (!Utils.isIdValid(tx.partner_id)) {
				return false;
			}
			if (Utils.debug) {
				Log.i(TAG, "addSTTX-->" + tx.partner_id + ",客服在缓存对象="
						+ mSTTXCache.get(TX.TUIXIN_MAN));
			}
			TX tx0 = mSTTXCache.get(tx.partner_id);
			if (tx0 == null) {
				mSTTXCache.put(tx.partner_id, tx);
				TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
				if (tinfor != null) {
					tx.setTxInfor(tinfor);
				}
				ContentValues values = txToValues(tx);
				if (updateTxByTXId(values, tx.partner_id) == 0) {
					// 陌生联系人是lrucache,缓存中不存在是也许数据库存在
					mTxCr.insert(TxDB.Tx.CONTENT_URI, values);
				}
			}

			return true;
		}

		private TX updateSTTX(long partner_id, ContentValues values) {
			if (Utils.debug) {
				Log.i(TAG, "updateSTTX-->" + partner_id + ",客服在缓存对象="
						+ mSTTXCache.get(TX.TUIXIN_MAN));
			}
			TX sttx = mSTTXCache.get(partner_id);
			if (sttx != null) {
				return updateTXByValues(sttx, values);
			} else {
				sttx = getSTTX(partner_id, true);
				if (sttx != null) {
					if (Utils.debug) {
						Log.i(TAG, "updateSTTX-->" + partner_id + ",对象area="
								+ sttx.getArea());
					}
					return updateTXByValues(sttx, values);
				}
				return null;
			}

		}

		private TX getSTTX(long partner_id, boolean bTryDB) {
			TX tx = mSTTXCache.get(partner_id);
			if (Utils.debug) {
				Log.i(TAG,
						"getSTTX-->" + partner_id + ",客服在缓存对象="
								+ mSTTXCache.get(TX.TUIXIN_MAN));
			}
			if (tx == null) {

				// 陌生人中没有取到
				if (partner_id == TX.TUIXIN_MAN)
					tx = getTxMan();
				else if (partner_id == TX.TUIXIN_FRIEND)
					tx = getTxFriend();
				else if (partner_id == TX.SL_GROUP_NOTICE)
					tx = getSlGroupNotice();
				else if (partner_id == TX.SL_SAFE) {
					tx = getSlSafe();
				} else if (bTryDB)
					tx = findTXFromDB(partner_id);
				if (tx != null)
					mSTTXCache.put(partner_id, tx);
			} else {
				TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
				if (tinfor != null) {
					tx.setTxInfor(tinfor);// 加入缓存前给tx加上与账户相关信息
					mSTTXCache.put(partner_id, tx);
				}
			}
			return tx;
		}

		private ContentValues txToValues(TX tx) {
			ContentValues values = new ContentValues();
			values.put(TxDB.Tx.TX_ID, tx.partner_id);
			values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
			values.put(TxDB.Tx.TX_SIGN, tx.sign);
			values.put(TxDB.Tx.PHONE, tx.getPhone());
			values.put(TxDB.Tx.EMAIL, tx.getEmail());
			values.put(TxDB.Tx.IS_P_BIND, tx.isPhoneBind());
			values.put(TxDB.Tx.IS_E_BIND, tx.isEmailBind());
			values.put(TxDB.Tx.AVATAR_MD5, tx.getAvatar_ver());
			values.put(TxDB.Tx.AVATAR_URL, tx.avatar_url);
			values.put(TxDB.Tx.SEX, tx.getSex());
			values.put(TxDB.Tx.BIRTHDAY, tx.birthday);
			values.put(TxDB.Tx.BLOOD_TYPE, tx.bloodType);
			values.put(TxDB.Tx.HOBBY, tx.hobby);
			values.put(TxDB.Tx.PROFESSION, tx.job);
			values.put(TxDB.Tx.HOME, tx.area);
			values.put(TxDB.Tx.DISTANCE, tx.distance);
			values.put(TxDB.Tx.AGE, tx.age);
			values.put(TxDB.Tx.SECOND_CHAR, tx.nick_name_pinyin);
			values.put(Tx.ISOP, tx.isop);
			// 如果有相册信息，为values赋值
			if (tx.getAlbum() != null && !tx.getAlbum().isEmpty()) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < tx.getAlbum().size(); i++) {
					sb.append(tx.getAlbum().get(i).getUrl());
					if (i != tx.getAlbum().size() - 1) {
						sb.append(Constants.STRING_SEPARATOR);
					}
				}
				values.put(Tx.ALBUM, sb.toString());
			}
			values.put(Tx.ALBUM_VER, tx.getAlbumVer());
			values.put(Tx.INFO_VER, tx.getInfoVer());
			values.put(Tx.IS_RECEIVE_REQ, tx.isReceiveReq() ? 0 : 1);
			values.put(Tx.LANGUAGES, tx.getLanguages());
			values.put(Tx.LEVLE, tx.getLevel());
			return values;
		}

		/** 根据条件更新数据库TX */
		private int updateTxByTXId(ContentValues values, long partnerId) {
			int rows = mTxCr.update(TxDB.Tx.CONTENT_URI, values, TxDB.Tx.TX_ID
					+ "=?", new String[] { "" + partnerId });
			return rows;
		}

		/** 从数据库读取TX对象 */
		private TX findTXFromDB(long id) {
			TX tx = null;
			if (Utils.isIdValid(id)) {
				Cursor cur = mTxCr.query(TxDB.Tx.CONTENT_URI, null,
						TxDB.Tx.TX_ID + "=?", new String[] { "" + id }, null);

				if (cur != null) {
					if (cur.moveToNext()) {
						tx = fetchOneContacts(cur);
					}
					cur.close();
				}
			}
			return tx;
		}

		public boolean isTxFriend(long id) {
			TxInfor tinfor = mTxInforCacheMap.get(id);
			if (tinfor != null && !tinfor.isBlackType() && tinfor.isTBType()) {
				return true;
			}
			return false;
		}

		public boolean addBlackTX(long partner_id, long inBlackTime) {
			if (!Utils.isIdValid(partner_id)) {
				// 联系人类型必须是“陌生神聊联系人”
				return false;
			}

			TxInfor tinfor = mTxInforCacheMap.get(partner_id);
			if (tinfor == null) {
				// 添加到黑名单缓存中
				tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_BLACK);
				tinfor.setInBlackTime(inBlackTime);
				mTxInforCacheMap.put(partner_id, tinfor);
				ContentValues values = tinfor.txinforToValues();
				if (updateTxInforByTXId(values, partner_id) == 0) {
					mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
				}
			} else {
				// 修改缓存中的对象
				tinfor.setBlackType();
				tinfor.setInBlackTime(inBlackTime);
				updateTxInforByTXId(tinfor.txinforToValues(), partner_id);
			}

			return true;
		}

		public ArrayList<TX> getBlackTXList() {
			if (Utils.debug)
				Log.i(TAG, "getBlackTXList()");
			// 存储黑名单的list集合
			ArrayList<TX> resultTXList = new ArrayList<TX>();
			Set<Entry<Long, TxInfor>> tmpSet = mTxInforCacheMap.entrySet();
			Iterator<Entry<Long, TxInfor>> idIter = tmpSet.iterator();
			TX tx = null;
			while (idIter.hasNext()) {
				Entry<Long, TxInfor> entryItem = idIter.next();
				if (entryItem.getValue().isBlackType()) {

					if ((tx = getTx(entryItem.getKey())) != null) {
						tx.setTxInfor(entryItem.getValue());// 设置黑名单TX特有属性
						resultTXList.add(tx);
					}
				}
			}

			return resultTXList;
		}

		public boolean isInBlack(long partner_id) {
			// 是否在我的黑名单
			TxInfor tinfor = mTxInforCacheMap.get(partner_id);
			if (tinfor != null && tinfor.isBlackType()) {
				return true;
			}
			return false;
		}

		public int getStarFriendAttr(long id) {

			if (mTxInforCacheMap.containsKey(id)) {
				return mTxInforCacheMap.get(id).getStarFriend();
			}

			return TxInfor.TX_NOT_FRIEND;
		}

		/** 取神聊TX,缓存优先 */
		@Override
		public TX getTx(long uid) {
			if (!Utils.isIdValid(uid)) {
				return null;
			}

			if (uid == getUserID()) {
				return getTxMe();
			}

			TX tx1 = getSTTX(uid, true);

			return tx1;

		}

		@Override
		public TX getTx(long uid, WeakReference<Handler> weakhdlr) {
			if (!Utils.isIdValid(uid)) {
				return null;
			}

			if (uid == getUserID()) {
				return getTxMe();
			}

			TX tx1 = getSTTX(uid, false);
			if (tx1 == null) {
				// 异步加载
				mTxDBAsyncHandler.obtainMessage(1, (int) uid, 0, weakhdlr)
						.sendToTarget();
			}

			return tx1;
		}

		public boolean addTx(TX tx) {
			if (tx == null) {
				return false;
			}
			TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
			if (tinfor != null) {
				tx.setTxInfor(tinfor);
			}

			// 再添加到陌生人缓存中
			if (getSTTX(tx.partner_id, true) == null) {
				addSTTX(tx);
				return true;
			}
			return false;
		}

		public TX updateTx(long partner_id, ContentValues values) {
			return updateSTTX(partner_id, values);
		}

		/** 更改Tx为陌生人 */
		public void changeTxToST(long partner_id) {

			TxInfor tinfor = mTxInforCacheMap.remove(partner_id);// 总引用中删除
			if (tinfor != null) {
				if (tinfor.isTBType()) {
					// 好友
					MsgStat.delMsgStatByPartnerId(context.getContentResolver(),
							partner_id);
				}
				tinfor.clearAttrs();
				int delRow = mTxCr.delete(TxDB.TX_Friends.CONTENT_URI,
						TxDB.Tx.TX_ID + "=?", new String[] { "" + partner_id });
				if (Utils.debug)
					Log.i(TAG, "删除了【" + delRow + "】条");
			}

		}

		/** 删除tx并添加到黑名单中 */
		public void removeTxToBlack(long partner_id) {

			if (isTxFriend(partner_id)) {
				// 好友
				TxInfor tinfor = mTxInforCacheMap.remove(partner_id);
				if (tinfor != null) {
					tinfor.clearAttrs();
				}

				MsgStat.delMsgStatByPartnerId(context.getContentResolver(),
						partner_id);
				broadcastMsg(TxData.FLUSH_MSGS);
			}

			addBlackTX(partner_id, System.currentTimeMillis());// 添加到黑名单中

		}

		/** 修改陌生人为好友 */
		public void changeSTToTB(long partner_id) {
			TX tx = getTx(partner_id);
			if (tx != null) {
				addTBTXId(partner_id, TxInfor.TX_COMMON_FRIEND);
				addTx(tx);
			}
		}

		private TX fetchOneContacts(Cursor c) {
			TX o = new TX();
			o.setPartnerId(c.getLong(c.getColumnIndex(TxDB.Tx.TX_ID)));
			o.setNick_name(c.getString(c.getColumnIndex(TxDB.Tx.DISPLAY_NAME)));
			o.setAvatar_ver(c.getInt(c.getColumnIndex(TxDB.Tx.AVATAR_MD5)));
			o.avatar_url = c.getString(c.getColumnIndex(TxDB.Tx.AVATAR_URL));
			o.setSex(c.getInt(c.getColumnIndex(TxDB.Tx.SEX)));
			o.birthday = c.getInt(c.getColumnIndex(TxDB.Tx.BIRTHDAY));
			o.bloodType = c.getInt(c.getColumnIndex(TxDB.Tx.BLOOD_TYPE));
			o.hobby = c.getString(c.getColumnIndex(TxDB.Tx.HOBBY));
			o.job = c.getString(c.getColumnIndex(TxDB.Tx.PROFESSION));
			o.area = c.getString(c.getColumnIndex(TxDB.Tx.HOME));
			o.distance = c.getInt(c.getColumnIndex(TxDB.Tx.DISTANCE));// 距离
			o.age = c.getInt(c.getColumnIndex(TxDB.Tx.AGE));// 年龄
			o.constellation = c.getString(c
					.getColumnIndex(TxDB.Tx.CONSTELLATION));// 星座
			o.sign = c.getString(c.getColumnIndex(TxDB.Tx.TX_SIGN));
			o.setPhone(c.getString(c.getColumnIndex(TxDB.Tx.PHONE)));
			o.setEmail(c.getString(c.getColumnIndex(TxDB.Tx.EMAIL)));
			String is_phone_bind = c.getString(c
					.getColumnIndex(TxDB.Tx.IS_P_BIND));
			if (is_phone_bind != null)
				o.setPhoneBind(is_phone_bind.equals("1"));
			else
				o.setPhoneBind(false);
			String is_email_bind = c.getString(c
					.getColumnIndex(TxDB.Tx.IS_E_BIND));
			if (is_email_bind != null)
				o.setEmailBind(is_email_bind.equals("1"));
			else
				o.setEmailBind(false);
			o.nick_name_pinyin = CnToSpell.getFullSpell(o.getNick_name());
			o.isop = c.getInt(c.getColumnIndex(Tx.ISOP));
			o.setLanguages(c.getString(c.getColumnIndex(Tx.LANGUAGES)));
			o.setBlog_head_msg(c.getString(c.getColumnIndex(TxDB.Tx.BLOG_INFOR)));
			String album = c.getString(c.getColumnIndex(Tx.ALBUM));
			ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
			if (!Utils.isNull(album)) {
				for (String url : album.split(Constants.STRING_SEPARATOR)) {
					AlbumItem ai = new AlbumItem();
					ai.setUrl(url);
					list.add(ai);
				}
			}
			o.setAlbum(list);
			o.setInfoVer(c.getInt(c.getColumnIndex(Tx.INFO_VER)));
			o.setAlbumVer(c.getInt(c.getColumnIndex(Tx.ALBUM_VER)));
			o.setReceiveReq(c.getInt(c.getColumnIndex(Tx.IS_RECEIVE_REQ)) == 0 ? true
					: false);
			o.setLevel(c.getInt(c.getColumnIndex(Tx.LEVLE)));
			return o;
		}

		public ArrayList<TX> fetchAllContacts(Cursor c) {
			ArrayList<TX> ret = new ArrayList<TX>();
			if (c != null) {
				while (c.moveToNext()) {
					ret.add(fetchOneContacts(c));
				}
				c.close();
			}

			return ret;
		}

		/** 应该是根据TX的partnerId来判断过滤重复的TX */
		public List<TX> listUniq(List<TX> list) {
			Set<TX> set = new LinkedHashSet<TX>();
			set.addAll(list);
			List<TX> newlist = new ArrayList<TX>();
			newlist.addAll(set);
			return newlist;
		}

		// 引用解除 2014.06.19 shc
		// /**
		// * 通过会话对象，返回TX用户信息对象
		// *
		// * @param singleMsgStat
		// * 都是好友单聊的会话，该形参不会有群聊的会话
		// */
		// public TX findTXByMsgStat(MsgStat singleMsgStat) {
		// return getTx(singleMsgStat.partner_id);
		// }

		@Override
		public ArrayList<TX> getTBTXList() {
			long b = System.currentTimeMillis();
			if (Utils.debug)
				Log.i(TAG, "getTBTXList()");
			// Collection<TxInfor> tmpCollections = mTBTXCache.values();
			ArrayList<TxInfor> tempTXList = new ArrayList<TxInfor>(
					mTxInforCacheMap.values());

			ArrayList<TX> resultTXList = new ArrayList<TX>();
			TX ttx = null;
			for (TxInfor tinfor : tempTXList) {
				if (!tinfor.isBlackType() && tinfor.isTBType()) {
					// 好友
					if ((ttx = getTx(tinfor.getPartner_id())) != null) {
						resultTXList.add(ttx);
					}
				}
			}

			// FirstCharComparator的规则是按好友拼音排序给list排序，并且星标好友优先排前面
			Collections.sort(resultTXList, new FirstCharComparator(
					TxInfor.TX_TYPE_TB));

			if (Utils.debug) {
				Log.i(TAG, "好友list:" + resultTXList.toString());
				Log.i(TAG,
						"query tx cost time is:"
								+ (System.currentTimeMillis() - b) + "ms");
			}
			return resultTXList;
		}

		public TX getTxMe() {
			if (tx_me == null) {
				reloadTXMe();

			}
			return tx_me;
		}

		/** 更新TXme，再次从sharedPreference中读取 */
		public void reloadTXMe() {
			if (tx_me == null) {
				tx_me = new TX();
				String user_id = mPrefMeme.user_id.getVal();
				if (!TextUtils.isEmpty(user_id)) {
					// 取出来的id不为空字符串
					long partner_id = Long.parseLong(user_id);
					if (Utils.isIdValid(partner_id)) {
						TX ttx = findTXFromDB(partner_id);
						if (ttx != null) {
							mPrefMeme.blogmsg.setVal(ttx.getBlog_head_msg());
							mPrefMeme.level.setVal(ttx.getLevel()).commit();
						}
					}
				}
			}
			// String user_id = prefs.getString(CommonData.USER_ID, "");
			String user_id = mPrefMeme.user_id.getVal();
			// TODO id无效为什么还要判断取值？
			if (Utils.isNull(user_id))
				tx_me.partner_id = -1;
			else {
				tx_me.partner_id = Long.parseLong(user_id);
			}

			tx_me.token = mPrefMeme.token.getVal();
			tx_me.setNick_name(mPrefMeme.nickname.getVal());
			tx_me.setPhone(mPrefMeme.telephone.getVal());
			tx_me.setPhoneBind(mPrefMeme.is_bind_phone.getVal());
			tx_me.setEmail(mPrefMeme.email.getVal());
			tx_me.setEmailBind(mPrefMeme.is_bind_email.getVal());
			tx_me.sign = mPrefMeme.sign.getVal();
			tx_me.birthday = mPrefMeme.birthday.getVal();
			tx_me.age = mPrefMeme.age.getVal();
			tx_me.constellation = mPrefMeme.constellation.getVal();
			tx_me.bloodType = mPrefMeme.bloodtype.getVal();
			if (mPrefMeme.hobby.getVal().equals("0")) {
				tx_me.hobby = "";
			} else {
				tx_me.hobby = mPrefMeme.hobby.getVal();
			}
			tx_me.job = mPrefMeme.job.getVal();
			tx_me.area = mPrefMeme.area.getVal();
			tx_me.setSex(mPrefMeme.sex.getVal());
			tx_me.setAvatar_ver(mPrefMeme.avatarver.getVal());
			tx_me.avatar_url = mPrefMeme.avatar_url.getVal();
			tx_me.friend_ver = mPrefMeme.friendver.getVal();
			tx_me.auth = mPrefMeme.auth.getVal();
			if (mPrefMeme.languages.getVal().equals("0")) {
				tx_me.setLanguages("");
			} else {
				tx_me.setLanguages(mPrefMeme.languages.getVal());
			}
			tx_me.setReceiveReq(mPrefMeme.is_receive_req.getVal());
			tx_me.setAlbumVer(mPrefMeme.album_version.getVal());

			String str = mPrefMeme.album.getVal();

			ArrayList<AlbumItem> album = new ArrayList<AlbumItem>();
			if (!"".equals(str)) {
				List<String> list = StringUtils.str2List(str);
				for (String url : list) {
					AlbumItem ai = new AlbumItem();
					ai.setUrl(url);
					album.add(ai);
				}
			}
			if (album.size() < 8) {
				// 照片总张数小于8张，最后添加一张“新添加”的默认照片按钮
				AlbumItem ai = new AlbumItem();
				ai.setAdd(true);
				album.add(ai);
			}

			tx_me.setBlog_head_msg(mPrefMeme.blogmsg.getVal());

			tx_me.setAlbum(album);
			tx_me.setLevel(mPrefMeme.level.getVal());

		}

		// 3049189 123456
		public long getUserID() {
			return getTxMe().partner_id;

		}

		public TX getTxMan() {
			if (tx_man == null) {
				tx_man = new TX();
				// tx_man.setTx_type(TxDB.MS_TYPE_TB);
				tx_man.partner_id = TX.TUIXIN_MAN;
				tx_man.setNick_name("神聊客服");
				tx_man.setEmail("service@shenliao.com");
				tx_man.setArea("北京;朝阳");
				tx_man.setSex(1);
				tx_man.sign = "线控对讲机 语音对讲也能盲操作喽~";
				tx_man.setBirthday(19880808);
				tx_man.setBloodType(3);
				tx_man.setPhone("01085870381");
				tx_man.setLevel((int) TX.TUIXIN_MAN);
			}
			return tx_man;
		}

		public TX getTxFriend() {
			if (tx_friend == null) {
				tx_friend = new TX();
				// tx_friend.setTx_type(TxDB.MS_TYPE_TB);
				tx_friend.partner_id = TX.TUIXIN_FRIEND;
				tx_friend.setNick_name("好友管家");
				tx_friend.setEmail("service@shenliao.com");
				tx_friend.setArea("北京;朝阳");
				tx_friend.setSex(1);
				tx_friend.sign = "看看附近都有谁，和TA神聊下吧!";
				tx_friend.setBirthday(19880808);
				tx_friend.setBloodType(3);
				tx_friend.setPhone("01085870381");
				tx_friend.setLevel((int) TX.TUIXIN_MAN);
			}
			return tx_friend;
		}

		public TX getSlGroupNotice() {
			if (sl_notice == null) {
				sl_notice = new TX();
				// sl_notice.setTx_type(TxDB.MS_TYPE_TB);
				sl_notice.partner_id = TX.SL_GROUP_NOTICE;
				sl_notice.setNick_name("群组动态");
				sl_notice.setEmail("service@shenliao.com");
				sl_notice.setArea("北京;朝阳");
				sl_notice.setSex(1);
				sl_notice.sign = "聊天室的新鲜事";
				sl_notice.setBirthday(19880808);
				sl_notice.setBloodType(3);
				sl_notice.setPhone("01085870381");
				sl_notice.setLevel((int) TX.TUIXIN_MAN);
			}
			return sl_notice;
		}

		public TX getSlSafe() {
			if (sl_safe == null) {
				sl_safe = new TX();
				// sl_safe.setTx_type(TxDB.MS_TYPE_TB);
				sl_safe.partner_id = TX.SL_SAFE;
				sl_safe.setNick_name("神聊小卫士");
				sl_safe.setEmail("service@shenliao.com");
				sl_safe.setArea("北京;朝阳");
				sl_safe.setSex(1);
				sl_safe.sign = "维护聊天";
				sl_safe.setBirthday(19880808);
				sl_safe.setBloodType(3);
				sl_safe.setPhone("01085870381");
				sl_safe.setLevel((int) TX.TUIXIN_MAN);
			}
			return sl_safe;
		}

		// public void updateTxMan(int avatar_ver, String user_sign,
		// String avatar_url, String phone, String display_name_pinyin,
		// int sex, String birthday, int blood_type, String hobby,
		// String profession, String home, String email) {
		// TX tx = getTxMan();
		// tx.setNick_name(display_name_pinyin);
		// tx.setEmail(email);
		// tx.setArea(home);
		// tx.setSex(sex);
		// tx.setAvatar_url(avatar_url);
		// tx.setAvatar_ver(avatar_ver);
		// tx.setSign(user_sign);
		// tx.setBirthday(birthday);
		// tx.setBloodType(blood_type);
		// tx.setHobby(hobby);
		// tx.setJob(profession);
		// tx.setPhone(phone);
		// }

		// public void updateTxFriend(int avatar_ver, String user_sign,
		// String avatar_url, String phone, String display_name_pinyin,
		// int sex, String birthday, int blood_type, String hobby,
		// String profession, String home, String email, String isop) {
		// TX tx = getTxFriend();
		// tx.setNick_name(display_name_pinyin);
		// tx.setEmail(email);
		// tx.setArea(home);
		// tx.setSex(sex);
		// tx.setAvatar_url(avatar_url);
		// tx.setAvatar_ver(avatar_ver);
		// tx.setSign(user_sign);
		// tx.setBirthday(birthday);
		// tx.setBloodType(blood_type);
		// tx.setHobby(hobby);
		// tx.setJob(profession);
		// tx.setPhone(phone);
		// tx.setIsop(isop);
		// }

		private TxInfor fetchOneTxInfor(Cursor c) {
			long partner_id = c
					.getLong(c.getColumnIndex(TxDB.TX_Friends.TX_ID));
			int tx_type = c.getInt(c.getColumnIndex(TxDB.TX_Friends.TX_TYPE));
			TxInfor o = new TxInfor(partner_id, tx_type);
			o.setContacts_person_name(c.getString(c
					.getColumnIndex(TxDB.TX_Friends.CONTACTS_PERSON_NAME)));
			o.setRemarkName(c.getString(c
					.getColumnIndex(TxDB.TX_Friends.REMARK_NAME)));
			o.setStarFriend(c.getInt(c
					.getColumnIndex(TxDB.TX_Friends.IS_STAR_FRIEND)));
			o.setInBlackTime(c.getInt(c
					.getColumnIndex(TxDB.TX_Friends.IN_BLACK_TIME)));
			return o;
		}

		public ArrayList<TxInfor> fetchAllTxInfors(Cursor c) {
			ArrayList<TxInfor> ret = new ArrayList<TxInfor>();
			if (c != null) {
				while (c.moveToNext()) {
					ret.add(fetchOneTxInfor(c));
				}
			}

			return ret;
		}

		// 更新好友的字段
		public TxInfor updateTXInforByValues(long partner_id,
				ContentValues values) {

			TxInfor tinfor = mTxInforCacheMap.get(partner_id);

			if (tinfor != null) {
				Set<Entry<String, Object>> valueSet = values.valueSet();
				Iterator<Entry<String, Object>> txIterator = valueSet
						.iterator();
				while (txIterator.hasNext()) {
					Entry<String, Object> tbEntry = txIterator.next();
					String key = tbEntry.getKey();
					Object value = tbEntry.getValue();
					if (key.equals(TxDB.TX_Friends.TX_TYPE)) {
						// 联系人类型
						tinfor.setTx_type((Integer) value);

					} else if (key.equals(TxDB.TX_Friends.CONTACTS_PERSON_NAME)) {
						// 本地通讯录显示名称
						tinfor.setContacts_person_name((String) value);

					} else if (key.equals(TxDB.TX_Friends.TX_ID)) {
						// 神聊号
						tinfor.setPartner_id((Long) value);

					} else if (key.equals(TxDB.TX_Friends.IS_STAR_FRIEND)) {
						// 是否是星标好友， 1：是，0:不是
						tinfor.setStarFriend((Integer) value);

					} else if (key.equals(TxDB.TX_Friends.REMARK_NAME)) {
						// 备注名
						tinfor.setRemarkName((String) value);
					} else if (key.equals(TxDB.TX_Friends.IN_BLACK_TIME)) {
						// 备注名
						tinfor.setInBlackTime((Long) value);
					}
				}
				// 缓存更新完毕，开始更新数据库
				updateTxInforByTXId(values, tinfor.getPartner_id());

			}
			return tinfor;

		}

		/** 根据条件更新数据库TX */
		private int updateTxInforByTXId(ContentValues values, long partnerId) {
			int rows = mTxCr.update(TxDB.TX_Friends.CONTENT_URI, values,
					TxDB.Tx.TX_ID + "=?", new String[] { "" + partnerId });
			return rows;
		}

		/** 比较tx本地的url是否是最新的，如果不是，则删除本地头像大小图 */
		private void updateUserAvatarFile(TX newTx) {

			// 比较头像url是否一致，不一致则删除本地头像文件。
			TX localTx = TX.tm.getTx(newTx.partner_id);
			if (localTx != null) {
				// 本地有该用户
				if (!localTx.getAvatar_url().equals(newTx.getAvatar_url())) {
					// 本地头像地址和最新头像地址不一样，删除本地缓存
					int delNum = mDownUpMgr.delTXAvatarFiles(
							localTx.getAvatar_url(), newTx.partner_id);
					boolean isSuccess = AvatarDownload
							.removeTXHeadImgCache(newTx.partner_id);// 删除头像缓存
					if (Utils.debug) {
						String strr = "删除的头像个数" + delNum;
						Log.i(TAG, strr + ",从内存缓存中删除=" + isSuccess);
					}
				}
			} else {
				// 本地没有这个人的信息，也执行删除头像操作，防止卸载遗留有头像文件
				int delNum = mDownUpMgr.delTXAvatarFiles(newTx.getAvatar_url(),
						newTx.partner_id);
				if (Utils.debug) {
					String strr = "没有查到此TX时，删除的头像个数" + delNum;
					Log.i(TAG, strr);
				}
			}

		}

	}

	/** 所有TX的管理暴露给UI的接口 */
	public interface ITxManager {
		// 获取TxMe对象
		TX getTxMe();

		// 获取TX对象
		TX getTx(long partner_id);

		TX getTx(long uid, WeakReference<Handler> weakhdlr);

		// 获取好友集合
		ArrayList<TX> getTBTXList();

		// 获取黑名单集合
		ArrayList<TX> getBlackTXList();
	}

	/** 和MsgHelper通信时，Msghelper回调方法 */
	public class MsgHandler {

		// 获取黑名单列表
		public void onServerGetBlacklist_79(ArrayList<TxInfor> blackList) {
			if (blackList != null) {
				for (TxInfor tinfor : blackList) {
					tm.addBlackTX(tinfor.getPartner_id(),
							tinfor.getInBlackTime());
				}
			}
		}

		// 获取备注名列表
		public void onServerGetRemarkNames_82(ArrayList<TxInfor> remarkNameList) {
			if (remarkNameList != null) {
				for (TxInfor tinfor : remarkNameList) {
					tm.addTBTXRemarkName(tinfor.getPartner_id(),
							tinfor.getRemarkName());
				}
			}

		}

		public void onServerGetUsersinfo_94(ArrayList<TX> usersInforList,
				boolean isEndOfList, int snType, int optResult) {
			// 批量获取用户信息
			String action = null;

			if (String.valueOf(snType).startsWith("1")) {
				action = Constants.INTENT_ACTION_GET_MORE_GROUP_USER;
			} else if (String.valueOf(snType).startsWith("2")) {
				action = Constants.INTENT_ACTION_BLACK_LIST_GROUP_2036;
			} else if (String.valueOf(snType).startsWith("3")) {
				action = Constants.INTENT_ACTION_GET_BLACKlIST_RSP;
			}

			if (Utils.debug) {
				if (String.valueOf(snType).startsWith("1")) {
					Log.e(TAG, "获取群成员");
				} else if (String.valueOf(snType).startsWith("2")) {
					Log.e(TAG, "获取群黑名单？？？？");
				} else if (String.valueOf(snType).startsWith("3")) {
					Log.e(TAG, "获取用户的整个黑名单？？？？？");
				}
			}

			ServerRsp serverRsp = new ServerRsp();
			if (optResult == OPT_OK) {
				if (usersInforList != null) {
					for (TX ttx : usersInforList) {
						ContentValues values = new ContentValues();
						values.put(TxDB.Tx.SEX, ttx.getSex());// sex
						values.put(TxDB.Tx.DISPLAY_NAME, ttx.getNick_name());// nickname
						values.put(TxDB.Tx.AVATAR_URL, ttx.avatar_url);// avatar

						tm.updateUserAvatarFile(ttx);// 更新本地头像

						// area
						// TODO
						// narea(是数字数组，代表国家，省，市)，area(是文字数组，代表市、区)2014.01.22 shc
						values.put(TxDB.Tx.HOME, ttx.area);
						values.put(TxDB.Tx.TX_SIGN, ttx.sign);// sign
						values.put(TxDB.Tx.LEVLE, ttx.getLevel());// level

						if (tm.updateTx(ttx.partner_id, values) == null) {
							tm.addTx(ttx);
						}
						ttx = tm.getTx(ttx.partner_id);
					}

					Bundle b = serverRsp.getBundle();
					b.putParcelableArrayList(MsgHelper.USER_LIST,
							usersInforList);
					serverRsp.putBoolean("isEnd", isEndOfList);
					serverRsp.setStatusCode(StatusCode.RSP_OK);

				}
			} else if (optResult == OPT_FAILED) {
				serverRsp.setStatusCode(StatusCode.OPT_FAILED);
			}
			broadcastMsg(action, serverRsp);

		}

		public void onServerFriendsIds_121(ArrayList<TxInfor> remarkNameList) {
			// 获取好友id列表
			if (remarkNameList != null) {
				for (TxInfor tinfor : remarkNameList) {
					tm.addTBTXId(tinfor.getPartner_id(), tinfor.getStarFriend());
				}
			}
		}

		public void onServerSearch_25(TX comTx) {
			if (comTx != null) {
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.TX_ID, comTx.partner_id);
				values.put(TxDB.Tx.DISPLAY_NAME, comTx.getNick_name());
				values.put(TxDB.Tx.EMAIL, comTx.getEmail());
				values.put(TxDB.Tx.HOME, comTx.getArea());
				values.put(TxDB.Tx.SEX, comTx.getSex());
				values.put(TxDB.Tx.AVATAR_URL, comTx.getAvatar_url());
				values.put(TxDB.Tx.AVATAR_MD5, comTx.getAvatar_ver());
				values.put(TxDB.Tx.TX_SIGN, comTx.sign);
				values.put(TxDB.Tx.BIRTHDAY, comTx.birthday);
				values.put(TxDB.Tx.BLOOD_TYPE, comTx.bloodType);
				values.put(TxDB.Tx.HOBBY, comTx.hobby);
				values.put(TxDB.Tx.PROFESSION, comTx.job);
				values.put(TxDB.Tx.PHONE, comTx.getPhone());
				values.put(TxDB.Tx.LEVLE, comTx.getLevel());

				if (comTx.partner_id == TX.TUIXIN_MAN) {
					// 神聊管家

				} else if (comTx.partner_id == TX.TUIXIN_FRIEND) {
					// 好友管家
					// ContentValues values = new ContentValues();
					// values.put(TxDB.Tx.TX_ID, comTx.partner_id);
					// values.put(TxDB.Tx.DISPLAY_NAME, comTx.getNick_name());
					// values.put(TxDB.Tx.EMAIL, comTx.getEmail());
					// values.put(TxDB.Tx.HOME, comTx.getArea());
					// values.put(TxDB.Tx.SEX, comTx.getSex());
					// values.put(TxDB.Tx.AVATAR_URL, comTx.getAvatar_url());
					// values.put(TxDB.Tx.AVATAR_MD5, comTx.getAvatar_ver());
					// values.put(TxDB.Tx.TX_SIGN, comTx.sign);
					// values.put(TxDB.Tx.BIRTHDAY, comTx.birthday);
					// values.put(TxDB.Tx.BLOOD_TYPE, comTx.bloodType);
					// values.put(TxDB.Tx.HOBBY, comTx.hobby);
					// values.put(TxDB.Tx.PROFESSION, comTx.job);
					// values.put(TxDB.Tx.PHONE, comTx.getPhone());
					values.put(TxDB.Tx.ISOP, comTx.getIsop());

					// TX tx = tm.updateTx(comTx.partner_id, values);
					// if (tx == null) {
					// // 更新失败，执行添加tx操作
					// tm.addTx(comTx);
					// }

				} else {
					// 其他人
					// ContentValues values = new ContentValues();
					// values.put(TxDB.Tx.TX_ID, comTx.partner_id);
					// values.put(TxDB.Tx.DISPLAY_NAME, comTx.getNick_name());
					// values.put(TxDB.Tx.EMAIL, comTx.getEmail());
					values.put(TxDB.Tx.IS_E_BIND, comTx.isEmailBind());
					values.put(TxDB.Tx.IS_P_BIND, comTx.isPhoneBind());
					// values.put(TxDB.Tx.HOME, comTx.getArea());
					// values.put(TxDB.Tx.SEX, comTx.getSex());
					values.put(TxDB.Tx.LANGUAGES, comTx.getLanguages());
					// values.put(TxDB.Tx.AVATAR_URL, comTx.getAvatar_url());
					// values.put(TxDB.Tx.AVATAR_MD5, comTx.getAvatar_ver());
					// values.put(TxDB.Tx.TX_SIGN, comTx.sign);
					// values.put(TxDB.Tx.BIRTHDAY, comTx.birthday);
					// values.put(TxDB.Tx.BLOOD_TYPE, comTx.bloodType);
					// values.put(TxDB.Tx.HOBBY, comTx.hobby);
					// values.put(TxDB.Tx.PROFESSION, comTx.job);
					// values.put(TxDB.Tx.PHONE, comTx.getPhone());

					// TX tx = tm.updateTx(comTx.partner_id, values);
					// if (tx == null) {
					// // 更新失败，执行添加tx操作
					// tm.addTx(comTx);
					// }
				}

				TX tx = tm.updateTx(comTx.partner_id, values);
				if (tx == null) {
					// 更新失败，执行添加tx操作
					tm.addTx(comTx);
				}

			}
		}

		// 登录成功的返回处理 2014.06.06 shc
		public void onServerLogin_3(TX comTx, MsgHelper.IExtraOpreater opera1,
				MsgHelper.IExtraOpreater opera2) {
			// 得到一个用户的详细资料
			if (comTx != null) {

				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.TX_ID, comTx.getPartner_id());
				values.put(TxDB.Tx.DISPLAY_NAME, comTx.getNick_name());
				values.put(TxDB.Tx.IS_RECEIVE_REQ, comTx.isReceiveReq() ? 0 : 1);
				values.put(TxDB.Tx.PHONE, comTx.getPhone());
				values.put(TxDB.Tx.EMAIL, comTx.getEmail());
				values.put(TxDB.Tx.IS_P_BIND, comTx.isPhoneBind());
				values.put(TxDB.Tx.IS_E_BIND, comTx.isEmailBind());
				values.put(TxDB.Tx.ALBUM_VER, comTx.getAlbumVer());
				values.put(TxDB.Tx.AVATAR_URL, comTx.getAvatar_url());
				values.put(TxDB.Tx.SEX, comTx.getSex());
				values.put(TxDB.Tx.HOME, comTx.getArea());
				values.put(TxDB.Tx.TX_SIGN, comTx.getSign());
				values.put(TxDB.Tx.BIRTHDAY, comTx.getBirthday());
				values.put(TxDB.Tx.BLOOD_TYPE, comTx.getBloodType());
				values.put(TxDB.Tx.HOBBY, comTx.getHobby());
				values.put(TxDB.Tx.PROFESSION, comTx.getJob());
				values.put(TxDB.Tx.LANGUAGES, comTx.getLanguages());
				values.put(TxDB.Tx.AVATAR_MD5, comTx.getAvatar_ver());
				values.put(TxDB.Tx.INFO_VER, comTx.getInfoVer());
				values.put(TxDB.Tx.ISOP, comTx.getIsop());
				values.put(TxDB.Tx.LEVLE, comTx.getLevel());

				mPrefMeme.user_id.setVal(comTx.getPartner_id() + "");//
				mPrefMeme.nickname.setVal(comTx.getNick_name());//
				mPrefMeme.auth.setVal(comTx.getIsop());
				mPrefMeme.is_receive_req.setVal(comTx.isReceiveReq());//

				if (Utils.debug) {
					Log.i(TAG, "返回用户头像地址:" + comTx.getAvatar_url());
				}
				// 处理91和92号协议
				opera1.operate();
				getSocketHelper().sendUserQun(comTx.getPartner_id());

				if (Utils.debug)
					Log.e(TAG, "登录 成功 creatDB ,创建数据库，发送广播");
				userLogin(comTx.getPartner_id() + "");

				mPrefMeme.email.setVal(comTx.getEmail());//
				mPrefMeme.telephone.setVal(comTx.getPhone());//

				if (comTx.isPhoneBind()) {
					mPrefMeme.tel_bind_state.setVal(PrefsMeme.TEL_BIND_SUCCESS);
				}
				mPrefMeme.is_bind_phone.setVal(comTx.isPhoneBind());//
				mPrefMeme.is_bind_email.setVal(comTx.isEmailBind());//

				// 如果本地好友版本号和Server版本号不同，那么请求好友列表
				opera2.operate();
				// 本地相册版本号小于Server则请求相册
				if (mPrefMeme.album_version.getVal() < comTx.getAlbumVer()) {
					getSocketHelper().getAlbum(comTx.getPartner_id(),
							mPrefMeme.album_version.getVal());
					mPrefMeme.album_version.setVal(comTx.getAlbumVer())
							.commit();//
				}

				mPrefMeme.avatar_url.setVal(comTx.getAvatar_url()).commit();//
				mPrefMeme.login_first.setVal("loginfirst");

				setSex(comTx.getSex());// 给LoginSessionManager设置sex属性，saveLoginSuccessUserInfor方法要用
				// 2014.01.02
				if (getAccountType() == SessionManager.SHEN_LIAO_ACCOUNT) {
					// 如果是神聊登录，则保存账号信息，新浪登陆则不保存
					try {
						mUserLoginInfor.saveLoginSuccessUserInfor(
								comTx.getPartner_id() + "",
								comTx.getAvatar_url());
					} catch (JSONException e) {
						if (Utils.debug)
							Log.e(TAG, "保存登录信息异常", e);
					}
				}
				mPrefMeme.sex.setVal(comTx.getSex());//
				mPrefMeme.first_commondata.setVal(true).commit();
				mPrefMeme.area.setVal(comTx.getArea());//
				mPrefMeme.sign.setVal(comTx.getSign());//

				mPrefMeme.birthday.setVal(comTx.getBirthday());//
				mPrefMeme.bloodtype.setVal(comTx.getBloodType());//

				mPrefMeme.hobby.setVal(comTx.getHobby());//
				mPrefMeme.job.setVal(comTx.getJob());//
				mPrefMeme.level.setVal(comTx.getLevel());//

				mPrefMeme.languages.setVal(comTx.getLanguages()).commit();//

				if (tm.tx_me != null) {
					if (tm.tx_me.getLevel() < comTx.getLevel()) {
						// 说明升级了，记录标志
						if (Utils.debug) {
							Log.i("bobo",
									"-------- 这里更新了" + tm.tx_me.getLevel());
						}
						mPrefInfor.isLevelUp.setVal(true).commit();
					}
				}

				getSocketHelper()
						.sendGetSingleOfflineMsg(comTx.getPartner_id());
				getSocketHelper().sendGetOffLineReceipt(comTx.getPartner_id());
				getSocketHelper()
						.sendGetOfflineSystemMsg(comTx.getPartner_id());
				// 查询未发送成功的消息
				Cursor c = getContentResolver().query(
						TxDB.Messages.CONTENT_URI,
						null,
						TxDB.Messages.READ_STATE + "=? and ("
								+ TxDB.Messages.CHANNEL_ID + " is NULL or "
								+ TxDB.Messages.CHANNEL_ID + "=0)",
						new String[] { String.valueOf(TXMessage.NOT_SENT) },
						null);
				ArrayList<TXMessage> list = new ArrayList<TXMessage>();
				if (c != null) {
					list = TXMessage.fetchAllDBMsgs(c);
					c.close();
				}
				if (Utils.debug) {
					if (list != null)
						Log.i(TAG,
								"未发送成功的消息数？？no Send Msg Count is : "
										+ list.size());
				}
				getSocketHelper().sendNoSendMsg(list,
						comTx.getPartner_id() + "");
				getSocketHelper().sendGetUserInfor(TX.TUIXIN_MAN);
				getSocketHelper().sendGetUserInfor(TX.TUIXIN_FRIEND);

				// tm.updateUserAvatarFile(comTx);// 更新本地头像
				TX tx = tm.getTx(comTx.getPartner_id());

				if (Utils.debug) {
					Log.i("bobo", "-------- 这里更新了么？？" + tx.getLevel() + "==="
							+ comTx.getLevel());
				}

				if (tx.getLevel() < comTx.getLevel()) {
					// 说明升级了，记录标志
					mPrefInfor.isLevelUp.setVal(true).commit();
				}
				mPrefMeme.level.setVal(comTx.getLevel()).commit();
				mPrefMeme.blogmsg.setVal(tx.getBlog_head_msg()).commit();
				onReloadTXMe();
				TX updateTx = tm.updateTx(comTx.partner_id, values);

				if (updateTx == null) {
					// 这里应该是本地没有数据
					mPrefInfor.isLevelUp.setVal(true).commit();
					tm.addTx(comTx);
				}

				onReloadTXMe();

			}

		}

		public void onServerUserinfor_33(TX comTx) {
			// 得到一个用户的详细资料
			if (comTx != null) {
				ServerRsp serverRsp = new ServerRsp();
				Intent intentSub = new Intent(
						Constants.INTENT_ACTION_USERINFO_RSP);
				intentSub.putExtra(Constants.EXTRA_SERVER_RSP_KEY,
						serverRsp.getBundle());

				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.PHONE, comTx.getPhone());
				values.put(TxDB.Tx.DISPLAY_NAME, comTx.getNick_name());
				values.put(TxDB.Tx.EMAIL, comTx.getEmail());
				values.put(TxDB.Tx.AVATAR_URL, comTx.getAvatar_url());
				values.put(TxDB.Tx.AVATAR_MD5, comTx.getAvatar_ver());
				values.put(TxDB.Tx.SEX, comTx.getSex());
				values.put(TxDB.Tx.HOME, comTx.area);
				values.put(TxDB.Tx.TX_SIGN, comTx.sign);
				values.put(TxDB.Tx.BIRTHDAY, comTx.birthday);
				values.put(TxDB.Tx.BLOOD_TYPE, comTx.bloodType);
				values.put(TxDB.Tx.HOBBY, comTx.hobby);
				values.put(TxDB.Tx.PROFESSION, comTx.job);
				values.put(TxDB.Tx.LEVLE, comTx.getLevel());
				if (Utils.debug) {
					Log.i(TAG,
							"onServerUserinfor_33-->" + comTx.getPartner_id()
									+ "地区信息为：" + comTx.area);
				}
				if (comTx.getPartner_id() == getUserid()) {
					// 如果是本人的详细信息

					mPrefMeme.nickname.setVal(comTx.getNick_name());
					mPrefMeme.telephone.setVal(comTx.getPhone());
					mPrefMeme.email.setVal(comTx.getEmail());
					mPrefMeme.is_bind_phone.setVal(comTx.isPhoneBind());
					mPrefMeme.is_bind_email.setVal(comTx.isEmailBind());
					mPrefMeme.friendver.setVal(comTx.getFriend_ver());
					mPrefMeme.avatar_url.setVal(comTx.getAvatar_url());
					mPrefMeme.avatarver.setVal(comTx.getAvatar_ver());
					mPrefMeme.sex.setVal(comTx.getSex());
					try {
						mUserLoginInfor.updateUserAvatarSex(
								comTx.getPartner_id(), comTx.getAvatar_url());
					} catch (JSONException e) {
						if (Utils.debug)
							Log.e(TAG, "更新本人头像和性别异常", e);
					}// 更新用户登录头像和性别信息
					mPrefMeme.first_commondata.setVal(true);
					mPrefMeme.area.setVal(comTx.getArea());
					mPrefMeme.sign.setVal(comTx.getSign());
					mPrefMeme.birthday.setVal(comTx.getBirthday());
					mPrefMeme.bloodtype.setVal(comTx.getBloodType());
					mPrefMeme.hobby.setVal(comTx.getHobby());
					mPrefMeme.job.setVal(comTx.getJob());
					mPrefMeme.languages.setVal(comTx.getLanguages());
					mPrefMeme.album_version.setVal(comTx.getAlbumVer());
					mPrefMeme.level.setVal(comTx.getLevel());

					// TODO 这时有个人信息了么？？

					if (TX.tm.getTxMe() != null) {
						if (TX.tm.getTxMe().getLevel() < comTx.getLevel()) {
							// 说明升级了，记录标志
							mPrefInfor.isLevelUp.setVal(true).commit();
						}
					}
					mPrefMeme.blogmsg.setVal(comTx.getBlog_head_msg()).commit();

					tm.reloadTXMe();// /////

				} else if (comTx.getPartner_id() == TX.TUIXIN_MAN) {
					// 神聊客服的详细信息

				} else if (comTx.getPartner_id() == TX.TUIXIN_FRIEND) {
					// 好友管家的详细信息
					values.put(TxDB.Tx.ISOP, comTx.getIsop());

				} else {
					// 用户指定搜索的其它神聊用户的详细信息

					values.put(TxDB.Tx.ISOP, comTx.getIsop());
					values.put(TxDB.Tx.IS_P_BIND, comTx.isPhoneBind());
					values.put(TxDB.Tx.IS_E_BIND, comTx.isEmailBind());
					values.put(TxDB.Tx.ALBUM_VER, comTx.getAlbumVer());
					values.put(TxDB.Tx.INFO_VER, comTx.getInfoVer());
					values.put(TxDB.Tx.LANGUAGES, comTx.getLanguages());

					socketHelper.getAlbum(comTx.getPartner_id(), 0);

					intentSub.putExtra(TX.EXTRA_TX, comTx.partner_id);
					MsgStat.updateMsgStatByTX(context.getContentResolver(),
							comTx);// 更新缓存后更新数据库
					ContentValues values1 = new ContentValues();
					values1.put(TxDB.Messages.MSG_SEX, comTx.getSex());
					values1.put(TxDB.Messages.MSG_PARTNER_NAME,
							comTx.getNick_name());
					ContentResolver cr = context.getContentResolver();
					cr.update(TxDB.Messages.CONTENT_URI, values1,
							TxDB.Messages.MSG_PARTNER_ID + "=?",
							new String[] { "" + comTx.partner_id });

					TXMessage tmsg = TXMessage.findTXMessageByTcardId(cr,
							comTx.getPartner_id() + "");
					if (tmsg != null) {
						tmsg.tcard_name = comTx.getNick_name();
						tmsg.tcard_avatar_url = comTx.getAvatar_url();
						tmsg.tcard_sex = comTx.getSex();
						tmsg.tcard_sign = comTx.getSign();
						TXMessage.updateTcardTXMessage(cr, tmsg);
					}
				}

				tm.updateUserAvatarFile(comTx);// 更新本地头像

				if (values.size() != 0) {
					// values不为空，非用户自己的信息
					if (!tm.addTx(comTx)) {
						// 如果没有添加tx成功，则执行更新操作
						tm.updateTx(comTx.partner_id, values);
					}
				}

				context.sendBroadcast(intentSub);// 封装好intent,系统发广播的方法

				// 调用SocketHelper中的回调
				List<AsyncCallback<?>> callList = socketHelper.userInforsCallback
						.get(comTx.getPartner_id());
				if (callList != null) {
					for (AsyncCallback<?> callback : callList) {
						callback.onSuccess(null, comTx.getPartner_id());
					}
					callList.clear();// 遍历完毕，清空回调
				}
			}

		}

		public void onServerAddPartner_38(TX ttx) {
			// 添加好友请求返回
			// TODO 这里不应该改变陌生人为好友吧
			if (ttx != null) {
				if (!ttx.getTxInfor().isBlackType()) {
					tm.changeSTToTB(ttx.partner_id);
				}
			}

		}

		public void onServerAgreeMsg_42(TX tx) {
			// 对方同意加为好友
			if (tx != null) {

				TX.tm.changeSTToTB(tx.partner_id);

				// TxData txdata = (TxData) context.getApplicationContext();

				// 伪造一条客户端信息
				TXMessage txmsg = TXMessage.creatCommonSms(
						context,
						tx.partner_id,
						tx.getNick_name(),
						tx.getPhone(),
						context.getResources().getString(
								R.string.receiver_false_msg), false,
						TXMessage.READ, getServerTime());
				TXMessage.saveTXMessagetoDB(txmsg,
						context.getContentResolver(), true);
				socketHelper.showNotification(txmsg, false);
			}

		}

		public void onServerPublicOnlineMember_2043(TX tx) {
			// 返回在线成员
			if (tx != null) {
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
				values.put(TxDB.Tx.AVATAR_URL, tx.getAvatar_url());
				values.put(TxDB.Tx.SEX, tx.getSex());
				values.put(TxDB.Tx.HOME, tx.getArea());
				values.put(TxDB.Tx.TX_SIGN, tx.sign);
				values.put(TxDB.Tx.LEVLE, tx.getLevel());

				TX ttx = tm.updateTx(tx.partner_id, values);
				if (ttx == null) {
					// 更新失败，添加tx
					TX.tm.addTx(tx);
				}
			}

		}

		public void onServerSystem_34_12(long uid, int blackType) {
			// 系统通知我被对方加入黑名单
			if (Utils.isIdValid(uid) && (blackType == 0)) {
				// TxData txdata = (TxData) context.getApplicationContext();

				tm.changeTxToST(uid);

			}

		}

		public void onServerSystem_34_0(TX tx) {

			// 推荐的通讯录好友
			TX ttx = tm.getTx(tx.partner_id);
			if (ttx == null) {
				tm.addTx(tx);
			}

		}

		public void onServerSystem_34_18(TX tempTx) {

			// 收到服务器推送的举报信息

			ContentValues values = new ContentValues();
			values.put(TxDB.Tx.SEX, tempTx.getSex());
			values.put(TxDB.Tx.DISPLAY_NAME, tempTx.getNick_name());
			values.put(TxDB.Tx.SECOND_CHAR,
					CnToSpell.getFullSpell(tempTx.getNick_name()));
			values.put(TxDB.Tx.AVATAR_URL, tempTx.getAvatar_url());

			if (tm.updateTx(tempTx.partner_id, values) == null) {
				// 更新失败，添加tx
				tm.addTx(tempTx);
			}

		}

		public void onServerSystem_34_11(long id) {

			// 好友请求
			TX tx = tm.getTx(id);
			// TxData txdata = (TxData) context.getApplicationContext();

			if (tx != null && !tx.getTxInfor().isTBType()
					&& !tx.getTxInfor().isBlackType()) {

				tm.changeSTToTB(id);

				// 伪造一条客户端信息
				TXMessage txmsg = TXMessage.creatCommonSms(
						context,
						tx.partner_id,
						tx.getNick_name(),
						tx.getPhone(),
						context.getResources().getString(
								R.string.receiver_false_msg), false,
						TXMessage.READ, getServerTime());
				TXMessage.saveTXMessagetoDB(txmsg,
						context.getContentResolver(), true);
				socketHelper.showNotification(txmsg, false);
			}

		}

		public void onServerSystem_34_10(TX tx, long ltime, String ac,
				String desc) {
			// 好友请求
			TX ttx = tm.getTx(tx.partner_id);

			// TxData txdata = (TxData) context.getApplicationContext();
			ContentResolver cr = context.getContentResolver();
			long id = tx.partner_id;

			if (ttx != null && ttx.getTxInfor().isTBType()) {
				// 好友
				socketHelper.sendAgreeMsg(true, id, true, ac);

			} else if (ttx == null
					|| (!ttx.getTxInfor().isTBType() && !ttx.getTxInfor()
							.isBlackType())) {
				// 是陌生人
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
				values.put(TxDB.Tx.SECOND_CHAR,
						CnToSpell.getFullSpell(tx.getNick_name()));
				values.put(TxDB.Tx.HOME, tx.getArea());
				values.put(TxDB.Tx.SEX, tx.getSex());
				values.put(TxDB.Tx.AVATAR_URL, tx.getAvatar_url());
				values.put(TxDB.Tx.TX_SIGN, tx.sign);

				if (tm.updateTx(tx.partner_id, values) == null) {
					tm.addTx(tx);
				}

				if ((ltime + "".length()) <= 10) {
					ltime = ltime * 1000;
				}
				String pre = tx.getNick_name()
						+ context.getResources().getString(
								R.string.recommended_want_add2);
				String body = Utils.isNull(desc) ? pre : desc;
				TXMessage tmsg = TXMessage.creatAddFriendsms(context, tx
						.getNick_name(), TX.TUIXIN_FRIEND, context
						.getResources().getString(R.string.tuixinfriend), body,
						tx.getNick_name(), tx.partner_id, tx.sign, tx.getSex(),
						tx.getAvatar_url(), ac, ltime);

				// tmsg.ac = ac;
				boolean isNew = true;
				Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
						new String[] { TxDB.Messages.MSG_ID },
						TxDB.Messages.TCARD_ID + "=? AND "
								+ TxDB.Messages.MSG_PARTNER_ID + " = "
								+ TX.TUIXIN_FRIEND, new String[] { ""
								+ tmsg.tcard_id }, null);

				if (c != null) {
					if (c.moveToFirst()) {
						tmsg.msg_id = c.getLong(0);
						isNew = false;
					}
					c.close();
				}
				// if (isNew) {
				socketHelper.showNotification(tmsg, false);
				TXMessage.saveTXMessagetoDB(tmsg, context.getContentResolver(),
						true);
				// } else {
				// TXMessage.updateTcardTXMessage(cr, tmsg);
				// }

				if (id != tm.getUserID()) {
					Intent i = new Intent(
							FriendManagerActivity.RECEIVER_FOR_RECOMMENDINFO);
					i.putExtra("newMsg", tmsg);
					i.putExtra("isNew", isNew);
					context.sendBroadcast(i);
				}
			}
		}

		public void onServerSystem_34_8(TX tx) {

			// 打招呼
			TX ttx = tm.getTx(tx.partner_id);
			if (ttx == null) {
				tm.addTx(tx);
			} else if (!ttx.getTxInfor().isBlackType()) {
				// 是陌生人
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
				values.put(TxDB.Tx.SECOND_CHAR,
						CnToSpell.getFullSpell(tx.getNick_name()));
				values.put(TxDB.Tx.HOME, tx.getArea());
				values.put(TxDB.Tx.SEX, tx.getSex());
				values.put(TxDB.Tx.AVATAR_URL, tx.getAvatar_url());
				values.put(TxDB.Tx.TX_SIGN, tx.sign);
				if (tm.updateTx(tx.partner_id, values) == null) {
					tm.addTx(tx);
				}
			}

		}

		public void onServerSystem_34_9(TX tx) {
			// sns好友推送
			TX ttx = tm.getTx(tx.partner_id);
			if (ttx == null) {
				tm.addTx(tx);
			} else if (!ttx.getTxInfor().isBlackType()) {
				// 是陌生人
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
				values.put(TxDB.Tx.SECOND_CHAR,
						CnToSpell.getFullSpell(tx.getNick_name()));
				values.put(TxDB.Tx.HOME, tx.getArea());
				values.put(TxDB.Tx.SEX, tx.getSex());
				values.put(TxDB.Tx.AVATAR_URL, tx.getAvatar_url());
				values.put(TxDB.Tx.TX_SIGN, tx.sign);

				if (tm.updateTx(tx.partner_id, values) == null) {
					tm.addTx(tx);
				}
			}
		}

		/** 收到用户消息被赞的通知 */
		public void onReceivePraiseNotice_128(PraiseNotice pn) {

			autoIncreasePraiseData(pn.getUid());
			getPraiseNoticeDao().onReceivePrisedNotice(pn);

		}

		public void onServerSystem_34_21(long blogId, long uid, int time) {
			// 被喜欢的瞬间提醒通知消息
			autoIncreasePraiseData(uid);
			BlogNoticeMsg blogNotice = new BlogNoticeMsg(blogId, uid, time);
			getLikeNoticeDao().add(blogNotice);

		}

		/** 自增点赞总数 */
		private void autoIncreasePraiseData(long praiseUid) {
			// mPrefInfor.praisedInfors.setVal(mPrefInfor.praisedInfors.getVal()+1);
			try {
				mPrefInfor.increasePraiseCount(praiseUid);
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "自增点赞总数异常", e);
			}

			long currentTime = System.currentTimeMillis();
			if ((currentTime - flushDataTime) > FLUSH_SPACE_TIME) {
				// 需要刷新此值并且发送Runnable
				msgParser.postRunnable(new Runnable() {

					@Override
					public void run() {
						mPrefInfor.commit();
					}
				}, FLUSH_SPACE_TIME);

				flushDataTime = currentTime;// 修改当前数据刷新时间
			}
		}

		public void onServerSystem_34_22(long blogId, int uid, int time) {
			// 瞬间被管理员删除的通知消息
			BlogMsg bmsg = bnd.findBlogMsg(blogId);
			TXMessage txMessage = TXMessage.createBlogDeleteNotice(
					getContext(), uid, "", time);
			if (bmsg != null) {
				String timeStr = "(" + Utils.formatFullTimeStr(bmsg.getTime())
						+ ")";
				txMessage.msg_body = "你于" + timeStr + " 发布的瞬间内容包含违规信息已被删除。";
			} else {
				txMessage.msg_body = "你发布的瞬间内容包含违规信息已被删除。";
			}
			TXMessage.saveTXMessagetoDB(txMessage, getContentResolver(), true);
			getSocketHelper().showNotification(txMessage, false);

		}

		public void onServerGetGroupLastWeekStars_2060(boolean isFromServer,
				TX tempTx) {
			// 更新上周之星
			if (isFromServer) {
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.SEX, tempTx.getSex());
				values.put(TxDB.Tx.DISPLAY_NAME, tempTx.getNick_name());
				values.put(TxDB.Tx.AVATAR_MD5, tempTx.getAvatar_ver());
				values.put(TxDB.Tx.AVATAR_URL, tempTx.getAvatar_url());
				if (tm.updateTx(tempTx.partner_id, values) == null) {
					// 更新tx失败，添加tx
					tm.addTx(tempTx);
				}
			} else {
				if (tm.getTx(tempTx.partner_id) == null) {
					tm.addTx(tempTx);
				}
			}

		}

		public void onServerSearchUser_117(ArrayList<TX> usersInforList,
				boolean isEnd) {
			// 搜索好友的结果
			if (usersInforList != null) {
				for (TX ttx : usersInforList) {
					ContentValues values = new ContentValues();
					values.put(TxDB.Tx.TX_ID, ttx.partner_id);// partner_id
					values.put(TxDB.Tx.DISPLAY_NAME, ttx.getNick_name());// nickname
					values.put(TxDB.Tx.SEX, ttx.getSex());// sex
					values.put(TxDB.Tx.AGE, ttx.getAge());// age
					values.put(TxDB.Tx.HOME, ttx.area);// area//TODO
														// narea(是数字数组，代表国家，省，市)，area(是文字数组，代表市、区)2014.01.22
														// shc
					values.put(TxDB.Tx.AVATAR_URL, ttx.avatar_url);// avatar
					values.put(TxDB.Tx.LEVLE, ttx.getLevel());// 级别

					tm.updateUserAvatarFile(ttx);// 更新本地头像
					if (tm.updateTx(ttx.partner_id, values) == null) {
						tm.addTx(ttx);
					}
					ttx = tm.getTx(ttx.partner_id);
				}
			}

			DataContainer.addSearchUser(usersInforList);
			ServerRsp serverRsp = new ServerRsp();
			serverRsp.setStatusCode(StatusCode.RSP_OK);
			serverRsp.putBoolean("isEnd", isEnd);
			broadcastMsg(Constants.INTENT_ACTION_SEARCH_USER_RSP, serverRsp);

		}

		public void onServerGetUserList_146(TX tx) {
			// 返回瞬间批量获取个人信息
			if (tx != null) {
				ContentValues values = new ContentValues();
				values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
				values.put(TxDB.Tx.AVATAR_URL, tx.getAvatar_url());
				values.put(TxDB.Tx.SEX, tx.getSex());
				values.put(TxDB.Tx.HOME, tx.getArea());
				values.put(TxDB.Tx.TX_SIGN, tx.sign);

				TX ttx = tm.updateTx(tx.partner_id, values);
				if (ttx == null) {
					// 更新失败，添加tx
					TX.tm.addTx(tx);
				}
			}

		}

		/** 重新加载txme */
		public void onReloadTXMe() {
			tm.reloadTXMe();
		}

	}

	private class MsgParser {

		// 优先级较低的协议号数组
		private final int[] protocalArray = { MsgInfor.SERVER_INFO_QUN,
				MsgInfor.SERVER_USERINFRO, MsgInfor.SERVER_GET_USERSINFO };

		private ParseHandler parseHandler0;// 优先级比较高
		private ParseHandler parseHandler1;// 优先级比较低

		public MsgParser() {
			if (parseHandler0 == null) {
				HandlerThread handlerThread = new HandlerThread("ParseMsg0");
				handlerThread.start();
				parseHandler0 = new ParseHandler(handlerThread.getLooper());
			}
			if (parseHandler1 == null) {
				HandlerThread handlerThread = new HandlerThread("ParseMsg1");
				handlerThread.start();
				parseHandler1 = new ParseHandler(handlerThread.getLooper());
			}
		}

		public void parseMsg(int type, String serverMsg) {
			Message message = Message.obtain();
			message.obj = serverMsg;

			for (int i = 0; i < protocalArray.length; i++) {
				if (protocalArray[i] == type) {
					// 优先级较低
					parseHandler1.sendMessage(message);
					return;
				}
			}
			// 优先级较高
			parseHandler0.sendMessage(message);

		}

		/** 执行一个延时任务 */
		public void postRunnable(Runnable runnable, long delayMillis) {
			parseHandler0.postDelayed(runnable, delayMillis);

		}

	}

	/**
	 * 发送广播消息（通知界面）serverRsp：Server响应数据
	 */
	private void broadcastMsg(String action, ServerRsp serverRsp) {
		Intent intent = new Intent(action);
		intent.putExtra(Constants.EXTRA_SERVER_RSP_KEY, serverRsp.getBundle());
		intent.setExtrasClassLoader(ServerRsp.class.getClassLoader());
		context.sendBroadcast(intent);
	}

	public static void broadcastMsg(String msg) {
		MsgStat.sendConstactsBroadCast();
		if (!msg.trim().equals("")) {
			// 指定广播目标的 action （注：指定了此 action 的 receiver 会接收此广播）
			Intent intent = new Intent(Constants.INTENT_ACTION_FLUSH);
			// 需要传递的参数
			intent.putExtra("msg", msg);
			// 发送广播
			context.sendBroadcast(intent);
		}
	}

	public void clear() {
		NotificationPopupWindow.cancelNotification(context);
		mPrefMeme.auth.setVal(0);
		mPrefMeme.user_id.setVal("");
		mPrefMeme.nickname.setVal("");
		mPrefMeme.token.setVal("");
		mPrefMeme.telephone.setVal("");
		mPrefMeme.auth.setVal(TxDB.TX_AUTH_NORMAL);
		mPrefMeme.is_bind_phone.setVal(false);
		mPrefMeme.email.setVal("");
		mPrefMeme.is_bind_email.setVal(false);
		mPrefMeme.sign.setVal("");
		mPrefMeme.birthday.setVal(0);
		mPrefMeme.age.setVal(0);
		mPrefMeme.constellation.setVal("");
		mPrefMeme.bloodtype.setVal(0);
		mPrefMeme.hobby.setVal("");
		mPrefMeme.job.setVal("");
		mPrefMeme.area.setVal("");
		mPrefMeme.sex.setVal(TX.DEFAULT_SEX);
		mPrefMeme.avatarver.setVal(0);
		mPrefMeme.avatar_url.setVal("");
		mPrefMeme.friendver.setVal(0);
		mPrefMeme.avatar_path.setVal("");
		mPrefMeme.account_type.setVal(SessionManager.SHEN_LIAO_ACCOUNT);
		mPrefMeme.weibo_user_id.setVal("");
		mPrefMeme.weibo_token.setVal("");
		mPrefMeme.weibo_token_secret.setVal("");
		mPrefMeme.auth_type.setVal(SessionManager.INVALID_OATH);
		mPrefMeme.album.setVal("");
		mPrefMeme.album_version.setVal(-1);
		mPrefMeme.blogmsg.setVal("");
		mPrefMeme.level.setVal(0);
		mPrefMeme.languages.setVal("").commit();

		TX.tm.reloadTXMe();// /////
		TX.tm.clearTXCache();

		MsgStat.clearMsgStats(true, getContentResolver());
		TxGroup.clearGroupCache();
		SocketHelper.upCount = 0;

		if (Utils.debug) {
			Log.i(TAG, "SessionManager 清空了所有的数据");
		}
	}

}
