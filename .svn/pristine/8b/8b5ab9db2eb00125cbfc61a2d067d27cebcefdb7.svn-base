package com.tuixin11sms.tx.utils;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl;

/** 给SharedPrefernce添加缓存 */
public class CachedPrefs {

	private SharedPreferences mPref;
	private Editor mEditor = null;
	private boolean needCommit = false;// 如果不需要commit,则不调用commit操作

	public CachedPrefs(SharedPreferences pref) {
		mPref = pref;
		mEditor = pref.edit();
	}

	public void commit() {
		if (needCommit) {
			mEditor.commit();
			needCommit = false;
		}
	}

	public abstract class PrefVal<DatType> {
		private DatType mDat;// Value值
		protected String mKey;

		protected abstract DatType getValue(DatType defVal);

		protected abstract void setValue(DatType val);

		PrefVal(String key, DatType defVal) {
			mKey = key;
			mDat = getValue(defVal);
		}

		public DatType getVal() {
			return mDat;
		}

		public CachedPrefs setVal(DatType val) {
			if (!mDat.equals(val)) {
				needCommit = true;
				mDat = val;
				setValue(val);
			}
			return CachedPrefs.this;
		}
	}

	public class BoolVal extends PrefVal<Boolean> {
		public BoolVal(String key, boolean defVal) {
			super(key, defVal);
		}

		protected Boolean getValue(Boolean defVal) {
			return mPref.getBoolean(mKey, defVal);
		}

		protected void setValue(Boolean val) {
			mEditor.putBoolean(mKey, val);
		}
	}

	public class IntVal extends PrefVal<Integer> {
		public IntVal(String key, int defVal) {
			super(key, defVal);
		}

		protected Integer getValue(Integer defVal) {
			return mPref.getInt(mKey, defVal);
		}

		protected void setValue(Integer val) {
			mEditor.putInt(mKey, val);
		}
	}

	public class StringVal extends PrefVal<String> {
		public StringVal(String key, String defVal) {
			super(key, defVal);
		}

		protected String getValue(String defVal) {
			return mPref.getString(mKey, defVal);
		}

		protected void setValue(String val) {
			mEditor.putString(mKey, val);
		}
	}

	public class LongVal extends PrefVal<Long> {
		public LongVal(String key, Long defVal) {
			super(key, defVal);
		}

		protected Long getValue(Long defVal) {
			return mPref.getLong(mKey, defVal);
		}

		protected void setValue(Long val) {
			mEditor.putLong(mKey, val);
		}
	}

	/** 管理preferSearch的类 */
	public static class PrefsSearch extends CachedPrefs {
		public static final String SEARCH_PREFS = "com.sheniao.search.preferences_%1$s";

		public PrefsSearch(SharedPreferences pref) {
			super(pref);
		}

	}

	/** 管理preferMeme的类 */
	public static class PrefsMeme extends CachedPrefs {
		public static final String MEME_PREFS = "com.tuixin11sms.tx.sms_preferences";
		public static final String START_FIRST = "startfirst";// string
		public static final String SHORTCUT = "shortcut";// string
		public static final int TEL_BIND_SUCCESS = 1;
		public static final int TEL_NO_CHECK = 2;
		public static final int TEL_BIND_FAILED = 3;
		public static final int TEL_HAVE_BINDED = 4;
		public static final int TEL_BIND_INIT_CODE = 100;
		public static final String AGE = "age";// int
		public static final String CONSTELLATION = "constellation";// String
		public static final String BLOODTYPE = "bloodtype";// int
		public static final String AREA = "area";// String
		public static final String SEX = "sex";// int
		public static final String LANGUAGES = "languages";// 语言 //String
		public static final String IS_ONLINE = "is_online"; // int
		public static final String VIBRATE = "vibrate"; // boolean
		public static final String CONTACTSUP = "contactsup"; // String
		public static final String WEIBO_NEW = "weibo_new";// 是否是神聊新用户 //boolean
		public static final String USER_EXIT = "user_exit";// String
		public static final String EXCPTION_EXIT = "exception_exit";// String

		public static String GROUP_ID_LIST = "idList";// String

		/** 字段对象 */
		public BoolVal is_installday = new BoolVal("is_install_day", false);// boolean
		/**
		 * 登录当天记录字段
		 */
		// 增加一个判定是否是今天第一次登录的字段，如果为“”是第一次，或者取出时间进行比对
		public LongVal day_login_time = new LongVal("day_login_time",
				System.currentTimeMillis());
		public LongVal day_mark = new LongVal("day_mark", 0l);// long
		public StringVal start_first = new StringVal("startfirst", "");// string
		public StringVal login_first = new StringVal("loginfirst", "");// string
		public LongVal tel_check_time = new LongVal("tel_check_time", -1l);// long
		public LongVal tel_check_click_time = new LongVal(
				"tel_check_click_time", -1l);// long
		public LongVal password_check_click_time = new LongVal(
				"password_check_click_time", -1l);// long
		public LongVal soket_check_click_time = new LongVal(
				"soket_check_click_time", -1l);// long
		public StringVal shortcut = new StringVal("shortcut", "");// string
		public IntVal updata_ver = new IntVal("updata_ver", -1);// int
		public StringVal updata_url = new StringVal("updta_url", "");// string
		public StringVal updata_log = new StringVal("updata_log", "");// string
		public IntVal tel_bind_state = new IntVal("tel_bind_state",
				TEL_BIND_INIT_CODE);// int
		public StringVal regist_name = new StringVal("registname", "");// string
		public StringVal foreign_check_sms_number = new StringVal(
				"foreign_check_sms_number", "");// string
		public StringVal foreign_check_sms_text = new StringVal(
				"foreign_check_sms_text", "");// string
		public LongVal foreign_check_time = new LongVal("forgien_check_time",
				-1l);// long
		// 用户资料
		public IntVal old_ver = new IntVal("oldver", -1);// 上次升级的版本//int
		public StringVal user_id = new StringVal("userid", "-1");// 一些地方给user_id的默认值为-1
																	// //String
		public StringVal token = new StringVal("token", "");// String
		public StringVal nickname = new StringVal("nickname", "");// String
		public StringVal realname = new StringVal("realname", "");// String
		public IntVal auth = new IntVal("auth", 0); // 用户权限,0普通用户,1,2忽略（不应该出现）,3是OP,4是超级OP,5是隐身OP
		public StringVal telephone = new StringVal("telephone", "");// 绑定的电话
																	// //String
		public BoolVal is_bind_phone = new BoolVal("obd", false); // 是否绑定手机//boolean
		public StringVal email = new StringVal("email", "");// String
		public BoolVal is_bind_email = new BoolVal("ebd", false);// 是否绑定email//boolean
		public StringVal sign = new StringVal("sign", "");// String
		public IntVal birthday = new IntVal("birthday", 0);// int
		public IntVal age = new IntVal("age", 0);// int
		public StringVal constellation = new StringVal("constellation", "");// String
		public IntVal bloodtype = new IntVal("bloodtype", 0);// int
		public StringVal hobby = new StringVal("hobby", "");// String
		public StringVal job = new StringVal("job", "");// String
		public StringVal area = new StringVal("area", "");// String
		public IntVal sex = new IntVal("sex", TX.DEFAULT_SEX);// 整理该字段前，这里默认值有的是1有的的-1//int
		public IntVal avatarver = new IntVal("avatarver", 0);// int
		public StringVal avatar_url = new StringVal("avatar_url", "");// String
		public IntVal friendver = new IntVal("friendver", 0); // int
		public BoolVal first_alert = new BoolVal("firstalert", false); // boolean
		public StringVal album = new StringVal("album", ""); // 相册//String
		public IntVal album_version = new IntVal("album_version", 0); // 相册版本
																		// //int
		public StringVal languages = new StringVal("languages", "");// 语言
																	// //String
		public StringVal blogmsg = new StringVal("blogmsg", "");// 瞬间//String
		public BoolVal is_receive_req = new BoolVal("is_receive_req", false); // 是否接收好友请求
																				// //boolean
		public BoolVal first_commondata = new BoolVal("firstCommondata", false); // boolean
		public BoolVal sound = new BoolVal("sound", true); // boolean
		public StringVal contactsup = new StringVal("contactsup", ""); // String
		public LongVal st = new LongVal("st", 0l);// 服务器时间与本地时间的差值，登陆成功后获取的值
													// //long
		public IntVal langid = new IntVal("langid", -1);// int
		public BoolVal is_sethead = new BoolVal("is_sethead", false);// boolean
		public StringVal avatar_path = new StringVal("avatar_path", "");// String
		public BoolVal teacher = new BoolVal("teacher", false);// boolean
		public IntVal select_sex_state = new IntVal("select_sex_state", -1);// int
		public StringVal device_id = new StringVal("device_id", "");// String
		public StringVal door = new StringVal("door", "");// String
		public StringVal weibo_token = new StringVal("weiboToken", "");// String
		public StringVal weibo_token_secret = new StringVal("tokenSecret", "");// String
		public StringVal weibo_user_id = new StringVal("weibo_userid", "");// String
		public LongVal weibo_over_time = new LongVal("weibo_over_time", 0l);// long
		public LongVal weibo_shenliao_login_id = new LongVal(
				"weibo_shenliao_login_id", 0l);// long
		public BoolVal weibo_upload_first = new BoolVal("weibo_first", false);// boolean
																				// 只有输入没有输出
																				// 2014.01.08
		public LongVal weibo_upload_last_time = new LongVal("weibo_last_time",
				0l);// long
		public IntVal account_type = new IntVal("account_type",
				SessionManager.SHEN_LIAO_ACCOUNT);// int
		public IntVal auth_type = new IntVal("auth_type",
				SessionManager.INVALID_OATH);// int
		public BoolVal weibo_new = new BoolVal("weibo_new", false);// 是否是神聊新用户
																	// //boolean
		public StringVal user_exit = new StringVal("user_exit", "");// String
		public StringVal exit = new StringVal("progress_exit", USER_EXIT);// String
		public StringVal excption_exit = new StringVal("exception_exit", "");// String
		public StringVal channel_order = new StringVal("channel_order", "");// String
		public StringVal group_id_list = new StringVal(GROUP_ID_LIST, "");// String
		public IntVal group_id_list_sise = new IntVal("idlistsize", 0);// int
		public IntVal level = new IntVal("level", 1);// int 等级

		public PrefsMeme(Context context) {
			super(context
					.getSharedPreferences(MEME_PREFS, Context.MODE_PRIVATE));
		}

	}

	/** 存储与账户相关的杂信息 */
	public static class PrefsInfors extends CachedPrefs {
		private static final String TAG = "PrefsInfors";
		public static final String INFOR_PREFS = "infor_prefs";

		public static final String PRAISED_COUNT = "praisedCount";// 被赞的总数的JSON
																	// key
		public static final String PRAISED_USERS = "praisedUsers";// 点赞用户id的JSON
																	// key

		private long currentUid = 0;

		/** 字段对象 */
		public BoolVal hasNewLikeNotice = null;// 是否有新的被喜欢提醒信息
		public BoolVal isLevelUp = null;// 是否升级
		public StringVal praisedInfors = null;// 用户消息和瞬间被赞的总数和最近点赞的人
		public BoolVal isNoReadMsg = null;// 是否有未读消息
		public PrefsInfors(Context context) {
			super(context.getSharedPreferences(INFOR_PREFS,
					Context.MODE_PRIVATE));
		}

		/** 初始化与用户相关的字段 */
		public void initFiled(long uid) {
			if (currentUid != uid) {
				// 如果不是同一个账号，则重新构造字段
				currentUid = uid;
				hasNewLikeNotice = new BoolVal("hasNewLikeNotice" + uid, false);
				isLevelUp = new BoolVal("isLevelUp" + uid, false);
				praisedInfors = new StringVal("praisedCount" + uid, "");
				isNoReadMsg=new BoolVal("isNoReadMsg"+uid, false);
			}
		}

		/** 取得被赞的总数 */
		public int getPraisedCount() {
			if (TextUtils.isEmpty(praisedInfors.getVal())) {
				return 0;
			}
			try {
				JSONObject praiseJson = new JSONObject(praisedInfors.getVal());
				return praiseJson.getInt(PRAISED_COUNT);
			} catch (Exception e) {
				if (Utils.debug) {
					Log.e(TAG, "获取点赞总数异常", e);
				}
				return 0;
			}
		}

		/** 取得点赞的用户id集合 */
		public ArrayList<Long> getPraiseUserIds() {
			ArrayList<Long> userIds = new ArrayList<Long>();
			if (TextUtils.isEmpty(praisedInfors.getVal())) {
				return userIds;
			}
			try {
				JSONObject praiseJson = new JSONObject(praisedInfors.getVal());
				JSONArray usersArray = praiseJson.getJSONArray(PRAISED_USERS);
				int gap = usersArray.length()
						- PraiseNoticeImpl.PRAISED_USERS_LIMIT;// array长度和12的差值
				int arrayEnd = gap > 0 ? gap : 0;// 最多显示12个
				for (int i = usersArray.length(); i > arrayEnd; i--) {
					// 倒叙添加到list中
					userIds.add(usersArray.getLong(i - 1));
				}
			} catch (Exception e) {
				if (Utils.debug) {
					e.printStackTrace();
				}
			}
			return userIds;
		}

		/** 增加点赞的总数 */
		public void increasePraiseCount(long praiseUserId) throws JSONException {
			JSONObject praiseJson = null;
			if (TextUtils.isEmpty(praisedInfors.getVal())) {
				praiseJson = new JSONObject();
			} else {
				praiseJson = new JSONObject(praisedInfors.getVal());
			}
			int praiseCount = praiseJson.optInt(PRAISED_COUNT);
			JSONArray usersArray = praiseJson.optJSONArray(PRAISED_USERS);
			if (usersArray == null) {
				usersArray = new JSONArray();
				praiseJson.put(PRAISED_USERS, usersArray);
			}

			int position = -1;// 被添加的userId在集合中的位置
			for (int i = 0; i < usersArray.length(); i++) {
				if (praiseUserId == usersArray.getLong(i)) {
					position = i;
					break;
				}
			}

			if (position != -1) {
				// 在array中有重复的uid
				JSONArray newUsersArray = new JSONArray();
				for (int i = 0; i < usersArray.length(); i++) {
					if (position == i) {
						continue;
					}
					newUsersArray.put(usersArray.get(i));
				}
				newUsersArray.put(praiseUserId);
				praiseJson.put(PRAISED_USERS, newUsersArray);
			} else {
				// array中没有此uid,全都是新人
				usersArray.put(praiseUserId);

				if (usersArray.length() > PraiseNoticeImpl.PRAISED_USERS_LIMIT) {
					JSONArray newUsersArray = new JSONArray();
					for (int i = 1; i < PraiseNoticeImpl.PRAISED_USERS_LIMIT + 1; i++) {
						if (position == i) {
							continue;
						}
						newUsersArray.put(usersArray.get(i));
					}
					praiseJson.put(PRAISED_USERS, newUsersArray);
				}
			}

			praiseJson.put(PRAISED_COUNT, praiseCount + 1);// 点赞总数加一

			praisedInfors.setVal(praiseJson.toString());

		}

	}

	void test() {
		PrefsMeme pref = new PrefsMeme(null);
		// pref.mVal1.getVal();
		// pref.mVal1.setVal("Abcd");
		// pref.mIVal1.setVal(20).commit();
	}

}
