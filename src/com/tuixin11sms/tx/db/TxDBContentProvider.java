package com.tuixin11sms.tx.db;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Message;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.contact.TxInfor;
import com.tuixin11sms.tx.data.SQLiteQueryBuilder;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Aid;
import com.tuixin11sms.tx.data.TxDB.Blog;
import com.tuixin11sms.tx.data.TxDB.LikeNotice;
import com.tuixin11sms.tx.data.TxDB.Messages;
import com.tuixin11sms.tx.data.TxDB.MsgStat;
import com.tuixin11sms.tx.data.TxDB.PraiseNotice;
import com.tuixin11sms.tx.data.TxDB.Qun;
import com.tuixin11sms.tx.data.TxDB.TX_Friends;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.utils.Utils;

public class TxDBContentProvider extends ContentProvider {
	public final static String TAG = "TxDBContentProvider";
	public static final String AUTHORITY = "com.tuixin11.smstx";
	public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY);
	private static final String DATABASE_NAME_BASE = "txdb.sqlite";
	public static String DATABASE_NAME = "";
	private static final int DATABASE_VERSION = 19;// 7,8是3.3.0的版本，添加了notice表和群的很多字段
													// 而且已被废弃
	private static final String MSGS_TABLE_NAME = "msgs";
	private static final String TX_TABLE_NAME = "txs";// 该表从3.9.0已经被废弃
														// 2014.05.14
	public static final String TX_FRIEND_TABLE_NAME = "friends";
	private static final String AID_TABLE_NAME = "aid";
	private static final String MSG_TRIGGER_NAME = "msgstat";
	private static final String QU_TABLE_NAME = "qun";
	public static final String NOTICE_PRAISE_TABLE_NAME = "praise";// 赞通知表
	public static final String NOTICE_LIKE_TABLE_NAME = "likedNotice";// 被喜欢通知表
	public static final String BLOG_TABLE_NAME = "blog";// 瞬间消息表

	private static final String VIEW_FRIENDS = "friendstable";
	private static HashMap<String, String> sMsgProjectionMap;
	private static HashMap<String, String> sTxProjectionMap;
	private static HashMap<String, String> sAidProjectionMap;
	private static HashMap<String, String> sMsgstatProjectionMap;
	private static HashMap<String, String> sQunProjectionMap;
	/** 好友表 */
	private static HashMap<String, String> sFriendProjectionMap;
	/** 被赞消息通知表 */
	private static HashMap<String, String> sPraisedNoticeProjectionMap;
	/** 被喜欢消息通知表 */
	private static HashMap<String, String> sLikedNoticeProjectionMap;
	/** 瞬间消息表 */
	private static HashMap<String, String> sBlogProjectionMap;
	private final static Object lock = new Object();
	public static boolean distinct = false;
	public static String limit = null;
	public static String groupby = null;
	public static String having = null;
	/** 匹配码，uriMatcher匹配uri后返回的匹配码 */
	private static final int MSGS = 1;
	private static final int MSGS_ID = 2;
	private static final int TXS = 3;
	private static final int TXS_ID = 4;
	private static final int AID = 5;
	private static final int AID_ID = 6;
	private static final int MSGSTAT = 7;
	private static final int MSGSTAT_ID = 8;
	private static final int QUN = 9;
	private static final int QUN_ID = 10;
	// private static final int CHANNEL = 11;
	// private static final int CHANNEL_ID = 12;
	/** 好友联系人 */
	private static final int TX_FRIENDS = 13;
	private static final int TX_FRIENDS_ID = 14;
	private static final int PRAISE_NOTICE = 15;
	private static final int PRAISE_NOTICE_ID = 16;
	private static final int LIKE_NOTICE = 17;
	private static final int LIKE_NOTICE_ID = 18;
	private static final int BLOG = 19;
	private static final int BLOG_ID = 20;

	private static DatabaseHelper mOpenHelper;
	private static SubDBHelper mSubHelper;// 从数据库
	private static final UriMatcher URL_MATCHER;

	public static class DatabaseHelper extends SQLiteOpenHelper {
		private final String attachAlias = "txdb1";// 附加的从数据库的别名
		public static final String TXS_TABLE_NAME = "txdb1.users";// 联系人表名

		public DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		protected SQLiteDatabase mDB = null;

		public synchronized SQLiteDatabase getWritableDatabase() {
			SQLiteDatabase db = super.getWritableDatabase();
			if (mDB != db)
				attachSubDB(db);
			mDB = db;
			return db;
		}

		public synchronized SQLiteDatabase getReadableDatabase() {
			SQLiteDatabase db = super.getReadableDatabase();
			if (mDB != db)
				attachSubDB(db);
			mDB = db;
			return db;
		}

		public synchronized void close() {
			mDB = null;
			super.close();
		}

		public void onCreate(SQLiteDatabase db) {
			if (Utils.debug)
				Log.e(TAG, "DatabaseHelper执行了onCreate方法");
			createMessageTable(db);
			createMsgStatTable(db);
			// createUserTable(db);
			createFriendsTable(db);
			createGroupTable(db);
			createAidTable(db);
			createPraiseNoticeTable(db);
			createBlogTable(db);
			createLikeNoticeTable(db);
			createIndices(db);
			// attachSubDB(db);

		}

		/** 附加子数据库 */
		public void attachSubDB(SQLiteDatabase db) {
			SQLiteDatabase subDB = null;
			if (mSubHelper != null) {
				subDB = mSubHelper.getWritableDatabase();// 创建子数据库
				subDB.close();
			}

			if (Utils.debug) {
				Log.i(TAG, "从数据库subDB状态：" + subDB.isOpen());
			}
			// 关联副数据库
			String path = new File(db.getPath()).getParent() + "/"
					+ SubDBHelper.DB_NAME;
			db.execSQL("attach " + "\"" + path + "\"" + " as " + attachAlias
					+ ";");
			// 创建好友视图
			db.execSQL("create temp view if not exists " + VIEW_FRIENDS
					+ " as select * from " + TX_FRIEND_TABLE_NAME
					+ " left join " + attachAlias + "."
					+ SubDBHelper.TXS_TABLE_NAME + " t on "
					+ TX_FRIEND_TABLE_NAME + ".partner_id=t.partner_id;");

			if (Utils.debug) {
				Log.i(TAG, "attach到主数据库后，从数据库subDB状态：" + subDB.isOpen());
			}

		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			if (Utils.debug)
				Log.e(TAG, "执行了onUpgrade方法：oldVersion=" + oldVersion
						+ ",newVersion=" + newVersion);

			// 之前数据库升级逻辑复杂，如果版本小于10，重新创建所有表
			if (oldVersion > newVersion || oldVersion < 10) {
				dropAll(db);
				onCreate(db);
				return;
			}
			try {
				db.beginTransaction();
				switch (oldVersion) {
				case 10:
					upgradeDatabaseToVersion11(db);
				case 11:
					upgradeDatabaseToVersion12(db);
				case 13:
					upgradeDatabaseToVersion14(db);
				case 14:
					upgradeDatabaseToVersion15(db);
				case 15:
					upgradeDatabaseToVersion16(db);
				case 16:
					upgradeDatabaseToVersion17(db);
				case 17:
					upgradeDatabaseToVersion18(db);
				case 18:
					upgradeDatabaseToVersion19(db);
				}
				db.setTransactionSuccessful();
				db.endTransaction();
			} catch (SQLException e) {
				db.endTransaction();
				if (Utils.debug)
					Log.e(TAG, "升级数据库异常", e);
				dropAll(db);
				onCreate(db);
			}
		}

		/**
		 * 创建消息表
		 * 
		 * @param db
		 */
		private void createMessageTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + MSGS_TABLE_NAME + " (" + Messages._ID
					+ " INTEGER PRIMARY KEY," + Messages.MSG_TYPE + " INTEGER,"
					+ Messages.MSG_ID + " INTEGER,"
					+ Messages.CONTACTS_PERSON_ID + " INTEGER,"
					+ Messages.CONTACTS_PERSON_NAME + " TEXT,"
					+ Messages.PRAISE_STATE + " INTEGER,"
					+ Messages.FILE_DOWN_TIME + " TEXT,"
					+ Messages.MSG_PARTNER_ID + " INTEGER,"
					+ Messages.MSG_PARTNER_NAME + " TEXT,"
					+ Messages.MSG_PARTNER_PHONE + " TEXT,"
					+ Messages.MSG_GROUP_ID + " INTEGER,"
					+ Messages.MSG_GROUP_NAME + " TEXT,"
					+ Messages.MSG_GROUP_URL + " TEXT," + Messages.MSG_SUBJECT
					+ " TEXT," + Messages.MSG_BODY + " TEXT,"
					+ Messages.MSG_PATH + " TEXT," + Messages.MSG_URL
					+ " TEXT," + Messages.MSG_FILE_LENGTH + " INTEGER,"
					+ Messages.AUD_TIMES + " INTEGER," + Messages.AUD_END
					+ " TEXT," + Messages.GEO + " TEXT," + Messages.AC
					+ " TEXT," + Messages.TCARD_NAME + " TEXT,"
					+ Messages.TCARD_ID + " INTEGER," + Messages.TCARD_SIGN
					+ " TEXT," + Messages.TCARD_SEX + " INTEGER,"
					+ Messages.TCARD_PHONE + " TEXT,"
					+ Messages.TCARD_AVATAR_URL + " TEXT," + Messages.AGREE
					+ " INTEGER," + Messages.SNS_ID + " TEXT,"
					+ Messages.SNS_NAME + " TEXT," + Messages.MSG_STATE
					+ " INTEGER," + Messages.WASME + " INTEGER,"
					+ Messages.READ_STATE + " INTEGER," + Messages.UPDATE_STATE
					+ " INTEGER," + Messages.CHANNEL_ID + " INTEGER,"
					+ Messages.SEND_TIME + " INTEGER," + Messages.MSG_TYPE_2
					+ " INTEGER," + Messages.REPORT_ID + " INTEGER,"
					+ Messages.REPORT_NAME + " TEXT," + Messages.REPORT_CONTEXT
					+ " TEXT," + Messages.SHUTUP_ST + " INTEGER,"
					+ Messages.SHUTUP_DU + " INTEGER,"

					+ Messages.GMID + " INTEGER," + Messages.GROUP_ID_NOTICE
					+ " INTEGER," + Messages.SN + " TEXT," + Messages.RS
					+ " TEXT," + Messages.OP + " INTEGER," + Messages.OP_ID
					+ " INTEGER," + Messages.MSG_ID_2 + " INTEGER,"
					+ Messages.MSG_SEX + " INTEGER," + Messages.OP_NAME
					+ " TEXT," + Messages.MSG_EMOD5 + " TEXT,"
					+ Messages.MSG_NUM + " INTEGER," + Messages.MSG_PKG_EMOMD5
					+ " TEXT," + Messages.MSG_PKGID + " INTEGER);");
		}

		/**
		 * 创建消息会话表
		 * 
		 * @param db
		 *            SQLiteDatabase instance
		 */
		private void createMsgStatTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + MSG_TRIGGER_NAME + " (" + MsgStat._ID
					+ " INTEGER PRIMARY KEY AUTOINCREMENT," + MsgStat.MSG_TYPE
					+ " INTEGER," + MsgStat.MSG_STAT_TYPE + " INTEGER,"
					+ MsgStat.MSG_SESSION_ID + " INTEGER," + MsgStat.MSG_ID
					+ " INTEGER," + MsgStat.CONTACTS_PERSON_ID + " INTEGER,"
					+ MsgStat.CONTACTS_PERSON_NAME + " TEXT,"
					+ MsgStat.MSG_PARTNER_ID + " INTEGER,"
					+ MsgStat.MSG_DISPLAY_NAME + " TEXT,"
					+ MsgStat.MSG_GROUP_NAME + " TEXT,"
					+ MsgStat.MSG_GROUP_DISPLAY_AVATARS + " TEXT,"
					+ MsgStat.MSG_GROUP_ID + " INTEGER," + MsgStat.MSG_BODY
					+ " TEXT," + MsgStat.PHONE + " TEXT," + Messages.WASME
					+ " INTEGER," + MsgStat.MSG_DATE + " INTEGER,"
					+ MsgStat.READ_STATE + " INTEGER," + MsgStat.MSG_COUT
					+ " INTEGER," + MsgStat.CHANNEL_ID + " INTEGER,"
					+ MsgStat.MSG_NOTREAD_COUT + " INTEGER,"
					+ MsgStat.GROUP_ID_NOTICE + " INTEGER," + MsgStat.GMID
					+ " INTEGER);");
		}

		/**
		 * 创建好友信息相关表
		 */
		private void createFriendsTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + TX_FRIEND_TABLE_NAME + " ("
					+ TX_Friends._ID + " INTEGER," + TX_Friends.TX_ID
					+ " INTEGER PRIMARY KEY," + TX_Friends.IS_STAR_FRIEND
					+ " INTEGER," + TX_Friends.CONTACTS_PERSON_NAME + " TEXT,"
					+ TX_Friends.TX_TYPE + " INTEGER,"
					+ TX_Friends.IN_BLACK_TIME + " INTEGER,"
					+ TX_Friends.REMARK_NAME + " VARCHAR(30)" + ");");
		}

		/**
		 * 创建群组表
		 * 
		 * @param db
		 */
		private void createGroupTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + QU_TABLE_NAME + " (" + Qun._ID
					+ " INTEGER PRIMARY KEY," + Qun.QU_TYPE + " INTEGER,"
					+ Qun.QU_ID + " INTEGER," + Qun.QU_VER + " INTEGER,"
					+ Qun.QU_DISPLAY_NAME + " TEXT," + Qun.QU_SIGN + " TEXT,"
					+ Qun.QU_OWN_ID + " INTEGER," + Qun.QU_TX_STATE
					+ " INTEGER," + Qun.QU_OWN_NAME + " TEXT," + Qun.QU_TIME
					+ " INTEGER," + Qun.QU_TX_IDS + " TEXT,"
					+ Qun.QU_TX_ADMIN_IDS + " TEXT," + Qun.QU_TX_ADMIN_NAMES
					+ " TEXT," + Qun.QU_AVATAR + " TEXT," + Qun.QU_TYPE_CHANNEL
					+ " INTEGER," + Qun.QU_BULLETIN + " TEXT," + Qun.QU_SN
					+ " INTEGER," + Qun.QU_RCV_MSG + " INTEGER,"
					+ Qun.QU_RCV_PUSH + " INTEGER," + Qun.QU_INDEX
					+ " INTEGER," + Qun.QU_ACCESS_TIME + " INTEGER,"
					+ Qun.ALL_NUM + " INTEGER," + Qun.OL_NUM + " INTEGER"
					+ ");");
		}

		/**
		 * 创建aid表
		 * 
		 * @param db
		 */
		private void createAidTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + AID_TABLE_NAME + " (" + Aid._ID
					+ " INTEGER PRIMARY KEY," + Aid.UP_PHONES + " TEXT" + ");");
		}

		/** 创建被赞消息通知表 */
		private void createPraiseNoticeTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NOTICE_PRAISE_TABLE_NAME + " ("
					+ PraiseNotice._ID + " INTEGER PRIMARY KEY,"
					+ PraiseNotice.GROUP_ID + " INTEGER," + PraiseNotice.GMID
					+ " INTEGER REFERENCES " + MSGS_TABLE_NAME + "("
					+ TxDB.Messages.GMID + ")," + PraiseNotice.ID_LIST
					+ " TEXT," + PraiseNotice.TIME + " INTEGER" + ");");
		}

		/** 创建瞬间消息表 */
		private void createBlogTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + BLOG_TABLE_NAME + " (" + Blog._ID
					+ " INTEGER," + Blog.BLOG_ID + " INTEGER PRIMARY KEY,"
					+ Blog.BLOG_PUBLISH_ID + " INTEGER," + Blog.BLOG_TEXT
					+ " TEXT," + Blog.BLOG_TYPE + " INTEGER,"
					+ Blog.BLOG_MEDIA_INFOR + " TEXT," + Blog.IMAGE_LOCAL_PATH
					+ " TEXT," + Blog.AUDIO_LOCAL_PATH + " TEXT,"
					+ Blog.PRAISED_COUNT + " INTEGER," + Blog.PRAISED_ID_LIST
					+ " TEXT," + Blog.IS_DEL_BY_OP + " INTEGER,"
					+ Blog.LIKED_STATE + " INTEGER," + Blog.PUBLISH_UID
					+ " INTEGER," + Blog.TIME + " INTEGER" + ");");
		}

		/** 创建被喜欢消息通知表 */
		private void createLikeNoticeTable(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + NOTICE_LIKE_TABLE_NAME + " ("
					+ LikeNotice._ID + " INTEGER," + LikeNotice.BLOG_ID
					+ " INTEGER REFERENCES " + BLOG_TABLE_NAME + "("
					+ Blog.BLOG_ID + ")," + LikeNotice.UID + " INTEGER,"
					+ LikeNotice.TIME + " INTEGER," + " PRIMARY KEY("
					+ LikeNotice.BLOG_ID + "," + LikeNotice.UID + "));");
		}

		/**
		 * 创建索引
		 * 
		 * @param db
		 */
		private void createIndices(SQLiteDatabase db) {
			try {
				db.execSQL("CREATE INDEX IF NOT EXISTS mMsgPartnerIdIndex ON "
						+ MSGS_TABLE_NAME + "(" + TxDB.Messages.MSG_PARTNER_ID
						+ ");");
				db.execSQL("CREATE INDEX IF NOT EXISTS mMsgGroupIdIndex ON "
						+ MSGS_TABLE_NAME + "(" + TxDB.Messages.MSG_GROUP_ID
						+ ");");
				db.execSQL("CREATE INDEX IF NOT EXISTS mMsgChannelIdIndex ON "
						+ MSGS_TABLE_NAME + "(" + TxDB.Messages.CHANNEL_ID
						+ ");");
				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS mGroupIdIndex ON "
						+ QU_TABLE_NAME + "(" + TxDB.Qun.QU_ID + ");");
				// 给消息id添加索引查询
				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS msg_msgId_index ON "
						+ MSGS_TABLE_NAME + "(" + TxDB.Messages.MSG_ID + ");");

				// 3.8.5新添加，正确性待测试 2013.10.22 shc
				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS mMsgStatSessionIdIndex ON "
						+ MSG_TRIGGER_NAME
						+ "("
						+ TxDB.MsgStat.MSG_SESSION_ID
						+ ");");

				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS friendtidIndex ON "
						+ TX_FRIEND_TABLE_NAME
						+ "("
						+ TxDB.TX_Friends.TX_ID
						+ ");");

				// 给被赞消息通知的gmid添加非唯一性索引
				db.execSQL("CREATE INDEX IF NOT EXISTS praiseNotice_gmid_index ON "
						+ NOTICE_PRAISE_TABLE_NAME
						+ "("
						+ TxDB.PraiseNotice.GMID + ");");
				// 给被喜欢消息通知的blogid添加非唯一性索引
				db.execSQL("CREATE INDEX IF NOT EXISTS likeNotice_blogid_index ON "
						+ NOTICE_LIKE_TABLE_NAME
						+ "("
						+ TxDB.LikeNotice.BLOG_ID + ");");

				// 给被喜欢消息通知的time添加非唯一性索引
				db.execSQL("CREATE INDEX IF NOT EXISTS likeNotice_time_index ON "
						+ NOTICE_LIKE_TABLE_NAME
						+ "("
						+ TxDB.LikeNotice.TIME
						+ ");");

				// TxDB.Blog.BLOG_ID是主键，不应该再建索引吧？ 2014.05.14 shc
				// //给瞬间消息id添加唯一性索引
				db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS blog_blogid_index ON "
						+ BLOG_TABLE_NAME + "(" + TxDB.Blog.BLOG_ID + ");");

			} catch (Exception ex) {
				if (Utils.debug)
					Log.e(TAG, "got exception creating indices: ", ex);
			}
		}

		/**
		 * 删除所有表
		 * 
		 * @param db
		 */
		private void dropAll(SQLiteDatabase db) {
			db.execSQL("DROP TABLE IF EXISTS " + MSG_TRIGGER_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + MSGS_TABLE_NAME);
			// db.execSQL("DROP TABLE IF EXISTS " + TX_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + QU_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + AID_TABLE_NAME);
			db.execSQL("DROP TABLE IF EXISTS " + TX_FRIEND_TABLE_NAME);
			// db.execSQL("DROP TABLE IF EXISTS " + CHANNEL_TABLE_NAME);
		}

		/**
		 * 升级数据库到版本11
		 * 
		 * @param db
		 */
		private void upgradeDatabaseToVersion11(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.LANGUAGES
					+ " TEXT;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.MEDALS
					+ " TEXT;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.GRADE
					+ " INTEGER;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.ALBUM
					+ " TEXT;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD "
					+ Tx.REMARK_NAME + " VARCHAR(30)");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.ALBUM_VER
					+ " INTEGER;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.INFO_VER
					+ " INTEGER;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD "
					+ Tx.IS_RECEIVE_REQ + " INTEGER;");
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.BLACK_TIME
					+ " INTEGER;");
		}

		/** 升级数据库到版本12 */
		private void upgradeDatabaseToVersion12(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD " + Tx.ISOP
					+ " TEXT;");
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.MSG_SEX + " INTEGER;");
		}

		/** 升级数据库到版本13 */
		private void upgradeDatabaseToVersion14(SQLiteDatabase db) {
			// 给TX表添加是否星标好友字段(isStarFriend)

			db.execSQL("ALTER TABLE " + TX_TABLE_NAME + " ADD "
					+ Tx.IS_STAR_FRIEND + " INTEGER DEFAULT -1;");

			// 给消息会话表添加消息sessionId和msgStat_type字段，
			db.execSQL("ALTER TABLE " + MSG_TRIGGER_NAME + " ADD "
					+ MsgStat.MSG_SESSION_ID + " INTEGER;");
			db.execSQL("ALTER TABLE " + MSG_TRIGGER_NAME + " ADD "
					+ MsgStat.MSG_STAT_TYPE + " INTEGER;");

			// 删除原来消息会话表中重复错误的信息,把老的表每条记录都生成一个sessionId
			Map<Integer, Integer> msgStatMap = new HashMap<Integer, Integer>();
			List<Long> waitToDeletedList = new ArrayList<Long>();// 待删除记录的_id号
			String[] columns = new String[] { TxDB.MsgStat._ID,
					TxDB.MsgStat.MSG_PARTNER_ID, TxDB.MsgStat.MSG_GROUP_ID };
			Cursor curMS = db.query(MSG_TRIGGER_NAME, columns, null, null,
					null, null, "msg_date desc");
			while (curMS.moveToNext()) {
				long _id = curMS
						.getLong(curMS.getColumnIndex(TxDB.MsgStat._ID));
				Integer partner_id = curMS.getInt(curMS
						.getColumnIndex(TxDB.MsgStat.MSG_PARTNER_ID));
				Integer group_id = curMS.getInt(curMS
						.getColumnIndex(TxDB.MsgStat.MSG_GROUP_ID));
				if (msgStatMap.containsKey(partner_id)) {
					// 存在这个partner_id,如果group_id相等则该记录待删除
					if (msgStatMap.get(partner_id) == group_id) {
						waitToDeletedList.add(_id);
					} else {
						// 添加该记录到map
						msgStatMap.put(partner_id, group_id);
					}
				} else {
					msgStatMap.put(partner_id, group_id);
				}

				ContentValues values = new ContentValues();
				if (Utils.isIdValid(group_id)) {
					// 群会话消息
					values.put(TxDB.MsgStat.MSG_STAT_TYPE, TxDB.MS_TYPE_QU);
					values.put(TxDB.MsgStat.MSG_SESSION_ID,
							com.tuixin11sms.tx.message.MsgStat
									.getMsgStatsSessionId(TxDB.MS_TYPE_QU,
											group_id));
				} else {
					// 单人会话消息
					values.put(TxDB.MsgStat.MSG_STAT_TYPE, TxDB.MS_TYPE_TB);
					values.put(TxDB.MsgStat.MSG_SESSION_ID,
							com.tuixin11sms.tx.message.MsgStat
									.getMsgStatsSessionId(TxDB.MS_TYPE_TB,
											partner_id));
				}
				db.update(MSG_TRIGGER_NAME, values, TxDB.MsgStat._ID + "=?",
						new String[] { "" + _id });// 给新添加两个字段赋值
			}
			// 删除重复的记录
			for (int i = 0, size = waitToDeletedList.size(); i < size; i++) {
				db.delete(MSG_TRIGGER_NAME, TxDB.MsgStat._ID + "=?",
						new String[] { "" + waitToDeletedList.get(i) });
			}

			// 3.8.5新添加，正确性待测试 2013.10.22 shc
			db.execSQL("CREATE INDEX IF NOT EXISTS msgstatSessionIdIndex ON "
					+ MSG_TRIGGER_NAME + "(" + TxDB.MsgStat.MSG_SESSION_ID
					+ ");");

		}

		/** 升级数据库到版本15 */
		private void upgradeDatabaseToVersion15(SQLiteDatabase db) {
			// 给消息表增加消息附件上传进度字段
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.UPDATE_STATE + " INTEGER DEFAULT 0;");
		}

		/** 升级数据库到版本16 */
		// 删除TXMessage中的thread_id、彩信Uri、morechat_send_to_person_name、channelId、msg_state、partner_phone、TxDB.Messages.MSG_PARTNER_PHONE、sms_address
		// 删除MsgStat中的thread_id、channelId、TxDB.MsgStat.CONTACTS_PERSON_ID、phone、TxDB.MsgStat.PHONE、phones、names、more_name、InContacts
		// 删除TxDB.Tx.SMS_PERSON、TxDB.Tx.SMS_ADDRESS、TxDB.Tx.SMS_ADDRESS、TxDB.Tx.USER_NAME、TxDB.Tx.PHONE_TYPE
		// 删除TX中的contacts_person_id、phone_type
		// TODO Sqlite不支持删除表中字段，需要复制表内容 2014.01.21 shc
		private void upgradeDatabaseToVersion16(SQLiteDatabase db) {
			// 创建friends好友表和索引
			createFriendsTable(db);

			// 删掉原来的主键索引，重新建立
			db.execSQL("DROP INDEX IF EXISTS mpartnerIdIndex;");
			db.execSQL("DROP INDEX IF EXISTS mgroupIdIndex;");
			db.execSQL("DROP INDEX IF EXISTS mchannelIdIndex;");
			db.execSQL("DROP INDEX IF EXISTS msgstatSessionIdIndex;");

			createIndices(db);

			// 迁移好友数据
			String[] columns = new String[] { TxDB.Tx.TX_ID,
					TxDB.Tx.REMARK_NAME, TxDB.Tx.IS_STAR_FRIEND,
					TxDB.Tx.CONTACTS_PERSON_NAME, TxDB.Tx.TX_TYPE,
					TxDB.Tx.DISPLAY_NAME, TxDB.Tx.TX_SIGN, TxDB.Tx.AVATAR_MD5,
					TxDB.Tx.SEX, TxDB.Tx.AVATAR_URL, TxDB.Tx.BIRTHDAY,
					TxDB.Tx.BLOOD_TYPE, TxDB.Tx.HOBBY, TxDB.Tx.PROFESSION,
					TxDB.Tx.HOME, TxDB.Tx.DISTANCE, TxDB.Tx.AGE,
					TxDB.Tx.CONSTELLATION, TxDB.Tx.PHONE, TxDB.Tx.EMAIL,
					TxDB.Tx.SECOND_CHAR, TxDB.Tx.LANGUAGES, TxDB.Tx.ALBUM,
					TxDB.Tx.ISOP, TxDB.Tx.ALBUM_VER, TxDB.Tx.INFO_VER,
					TxDB.Tx.IS_RECEIVE_REQ, TxDB.Tx.IS_P_BIND,
					TxDB.Tx.IS_E_BIND };
			// Cursor curMS = db.query(TX_TABLE_NAME, columns, TxDB.Tx.TX_TYPE
			// + "=?", new String[] { "" + TxDB.TX_TYPE_TB }, null, null,
			// null);
			Cursor curMS = db.query(TX_TABLE_NAME, columns, null, null, null,
					null, null);// 查询出所有的用户资料
			while (curMS.moveToNext()) {
				Integer partner_id = curMS.getInt(curMS
						.getColumnIndex(TxDB.Tx.TX_ID));

				int txType = curMS
						.getInt(curMS.getColumnIndex(TxDB.Tx.TX_TYPE));// 获取原来联系人类型

				if (txType == TxDB.TX_TYPE_TB) {
					// 好友用户
					String remarkName = curMS.getString(curMS
							.getColumnIndex(TxDB.Tx.REMARK_NAME));
					String contactName = curMS.getString(curMS
							.getColumnIndex(TxDB.Tx.CONTACTS_PERSON_NAME));
					Integer isStarFriend = curMS.getInt(curMS
							.getColumnIndex(TxDB.Tx.IS_STAR_FRIEND));

					TxInfor tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
					tinfor.setRemarkName(remarkName);
					tinfor.setContacts_person_name(contactName);
					tinfor.setStarFriend(isStarFriend);
					ContentValues values = tinfor.txinforToValues();

					db.insert(TX_FRIEND_TABLE_NAME,
							TxDB.TX_Friends.REMARK_NAME, values);
				}

				// 转移好友TX的一般字段属性

				ContentValues values_tx = new ContentValues();
				values_tx.put(TxDB.Tx.TX_ID, partner_id);
				values_tx.put(TxDB.Tx.DISPLAY_NAME, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.DISPLAY_NAME)));
				values_tx.put(TxDB.Tx.TX_SIGN,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.TX_SIGN)));
				values_tx.put(TxDB.Tx.PHONE,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.PHONE)));
				values_tx.put(TxDB.Tx.EMAIL,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.EMAIL)));
				values_tx.put(TxDB.Tx.IS_P_BIND, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.IS_P_BIND)));
				values_tx.put(TxDB.Tx.IS_E_BIND, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.IS_E_BIND)));
				values_tx.put(TxDB.Tx.AVATAR_MD5,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.AVATAR_MD5)));
				values_tx.put(TxDB.Tx.AVATAR_URL, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.AVATAR_URL)));
				values_tx.put(TxDB.Tx.SEX,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.SEX)));
				try {
					String tempStr;
					if (TextUtils.isEmpty(tempStr = curMS.getString(curMS
							.getColumnIndex(TxDB.Tx.BIRTHDAY)))) {
						values_tx.put(TxDB.Tx.BIRTHDAY, 0);
					} else {
						values_tx.put(TxDB.Tx.BIRTHDAY,
								Integer.parseInt(tempStr));
					}
					if (TextUtils.isEmpty(tempStr = curMS.getString(curMS
							.getColumnIndex(TxDB.Tx.ISOP)))) {
						values_tx.put(Tx.ISOP, 0);
					} else {
						values_tx.put(Tx.ISOP, Integer.parseInt(tempStr));// TODO
																			// 把一个String类型的变量放在Integer类型中，不知有没有问题
																			// 2014.04.08
																			// shc
					}
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "数据库升级转换生日、管理权限为数字异常", e);
				}
				values_tx.put(TxDB.Tx.BLOOD_TYPE,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.BLOOD_TYPE)));
				values_tx.put(TxDB.Tx.HOBBY,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.HOBBY)));
				values_tx.put(TxDB.Tx.PROFESSION, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.PROFESSION)));
				values_tx.put(TxDB.Tx.HOME,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.HOME)));
				values_tx.put(TxDB.Tx.DISTANCE,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.DISTANCE)));
				values_tx.put(TxDB.Tx.AGE,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.AGE)));
				values_tx
						.put(TxDB.Tx.SECOND_CHAR, curMS.getInt(curMS
								.getColumnIndex(TxDB.Tx.SECOND_CHAR)));
				// values_tx.put(Tx.GRADE,
				// curMS.getInt(curMS.getColumnIndex(TxDB.Tx.GRADE)));
				// values_tx.put(Tx.MEDALS,
				// curMS.getString(curMS.getColumnIndex(TxDB.Tx.MEDALS)));
				values_tx.put(Tx.ALBUM,
						curMS.getString(curMS.getColumnIndex(TxDB.Tx.ALBUM)));
				values_tx.put(Tx.ALBUM_VER,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.ALBUM_VER)));
				values_tx.put(Tx.INFO_VER,
						curMS.getInt(curMS.getColumnIndex(TxDB.Tx.INFO_VER)));
				values_tx.put(Tx.IS_RECEIVE_REQ, curMS.getInt(curMS
						.getColumnIndex(TxDB.Tx.IS_RECEIVE_REQ)));
				values_tx.put(Tx.LANGUAGES, curMS.getString(curMS
						.getColumnIndex(TxDB.Tx.LANGUAGES)));

				mSubHelper.getWritableDatabase().insert(
						SubDBHelper.TXS_TABLE_NAME, TxDB.Tx.PHONE, values_tx);

			}
			curMS.close();// 关闭cursor集

			// 删除原来txs表
			db.execSQL("DROP TABLE IF EXISTS " + TX_TABLE_NAME);
		}

		/** 升级数据库到版本17 */
		private void upgradeDatabaseToVersion17(SQLiteDatabase db) {
			// 给群组表添加唯一性索引
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS mGroupIdIndex ON "
					+ QU_TABLE_NAME + "(" + TxDB.Qun.QU_ID + ");");
			db.execSQL("CREATE INDEX IF NOT EXISTS msg_msgId_index ON "
					+ MSGS_TABLE_NAME + "(" + TxDB.Messages.MSG_ID + ");");

		}

		/** 升级数据库到版本18 */
		private void upgradeDatabaseToVersion18(SQLiteDatabase db) {

			// 创建被赞消息通知表，并给被赞消息通知的gmid添加非唯一性索引
			createPraiseNoticeTable(db);
			db.execSQL("CREATE INDEX IF NOT EXISTS praiseNotice_gmid_index ON "
					+ NOTICE_PRAISE_TABLE_NAME + "(" + TxDB.PraiseNotice.GMID
					+ ");");

			// 创建被喜欢消息通知表，并给被喜欢消息通知的blogid添加非唯一性索引，因为一个瞬间可以被多人喜欢
			createLikeNoticeTable(db);
			db.execSQL("CREATE INDEX IF NOT EXISTS likeNotice_blogid_index ON "
					+ NOTICE_LIKE_TABLE_NAME + "(" + TxDB.LikeNotice.BLOG_ID
					+ ");");

			// 给被喜欢消息通知的time添加非唯一性索引
			db.execSQL("CREATE INDEX IF NOT EXISTS likeNotice_time_index ON "
					+ NOTICE_LIKE_TABLE_NAME + "(" + TxDB.LikeNotice.TIME
					+ ");");

			// 创建瞬间消息表，给瞬间消息id添加唯一性索引
			createBlogTable(db);
			// TxDB.Blog.BLOG_ID是主键，不应该再建索引吧？ 2014.05.14 shc
			// db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS blog_blogid_index ON "
			// + BLOG_TABLE_NAME + "(" + TxDB.Blog.BLOG_ID
			// + ");");

			// 给消息表增加消息被赞状态,附件第一次下载完成时间字段
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.PRAISE_STATE + " INTEGER DEFAULT "
					+ com.tuixin11sms.tx.message.PraiseNotice.ACTION_NONE + ";");
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.FILE_DOWN_TIME + " TEXT;");

			// // 给个人信息表增加瞬间的属性字段，json格式（包括：瞬间访问总数，被喜欢总数，瞬间总数字段）
			// mSubHelper.getWritableDatabase().execSQL(
			// "ALTER TABLE " + SubDBHelper.TXS_TABLE_NAME + " ADD "
			// + TxDB.Tx.BLOG_INFOR + " TEXT;");
		}

		/**
		 * 升级数据库到版本19(预留)
		 */
		private void upgradeDatabaseToVersion19(SQLiteDatabase db) {
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.MSG_EMOD5 + " TEXT;");
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.MSG_NUM + " INTEGER DEFAULT 0;");
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.MSG_PKG_EMOMD5 + " TEXT;");
			db.execSQL("ALTER TABLE " + MSGS_TABLE_NAME + " ADD "
					+ Messages.MSG_PKGID + " INTEGER DEFAULT 0;");

		}

		/**
		 * 升级数据库到版本20(预留)
		 */
		private void upgradeDatabaseToVersion20(SQLiteDatabase db) {

		}

	}

	public boolean onCreate() {
		return true;
	}

	public static boolean creatDB(final Context ctxt, String userid) {
		// 创建主副数据库
		return createMainDB(ctxt, userid) && createSubDB(ctxt, userid);
	}

	/** 创建主数据库 */
	private static boolean createMainDB(final Context ctxt, String userid) {
		String dbName = DATABASE_NAME_BASE + userid;
		if (mOpenHelper != null && !dbName.equals(DATABASE_NAME)) {
			mOpenHelper.close();
			mOpenHelper = null;
		}
		if (mOpenHelper == null) {
			if (Utils.debug)
				Log.i(TAG, "creatDB :" + dbName);
			// 这应该是为了兼容以前的某个版本吧？2014.03.27 shc
			if (!Utils.isNull(userid)) {
				File f = ctxt.getDatabasePath(DATABASE_NAME_BASE);
				if (f.exists()) {
					if (Utils.debug)
						Log.i(TAG, "creatDB rename " + dbName);
					File renameFile = ctxt.getDatabasePath(dbName);
					f.renameTo(renameFile);
				}
			}
			DATABASE_NAME = dbName;
			mOpenHelper = new DatabaseHelper(ctxt);

			return true;// 新创建打开了数据库
		}

		return false;// 没有新创建打开数据库
	}

	// 创建从数据库
	private static boolean createSubDB(final Context ctxt, String userid) {
		if (mSubHelper == null) {
			if (Utils.debug)
				Log.i(TAG, "creatSubDB");
			mSubHelper = new SubDBHelper(ctxt);
		}

		return true;// 为了兼容以前通过true和false的判定，这里只返回true
	}

	/** 用户退出登陆后执行关闭数据库操作 */
	// 避免出现最近会话消息的问题
	public static void closeDB() {
		mOpenHelper.close();
		mOpenHelper = null;
	}

	public static DatabaseHelper getmOpenHelper() {
		return mOpenHelper;
	}

	public static void setmOpenHelper(DatabaseHelper helper) {
		mOpenHelper = helper;
	}

	Object mWriteLock = new Object();

	// Object mSubWriteLock = new Object();// 子数据库的写锁
	// //子库的所有操作都是通过操作主库对象实现的，所以删掉字库的锁,应该不需要 2014.05.26 shc

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		if (mOpenHelper == null)
			return 0;
		int count;
		String segment;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		synchronized (mWriteLock) {
			switch (URL_MATCHER.match(uri)) {
			case MSGS:
				count = db.delete(MSGS_TABLE_NAME, selection, selectionArgs);
				break;
			case MSGS_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(MSGS_TABLE_NAME, TxDB.Messages.MSG_ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;

			case TXS:

				// synchronized (mSubWriteLock) {
				count = db.delete(DatabaseHelper.TXS_TABLE_NAME, selection,
						selectionArgs);
				// }

				break;

			case TXS_ID:

				// synchronized (mSubWriteLock) {
				segment = uri.getPathSegments().get(1);
				count = db.delete(DatabaseHelper.TXS_TABLE_NAME, TxDB.Tx._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				// }

				break;

			// 好友联系人操作
			case TX_FRIENDS:

				count = db.delete(TX_FRIEND_TABLE_NAME, selection,
						selectionArgs);

				break;

			case TX_FRIENDS_ID:

				segment = uri.getPathSegments().get(1);
				count = db.delete(TX_FRIEND_TABLE_NAME, TxDB.Tx._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);

				break;

			case AID:
				count = db.delete(AID_TABLE_NAME, selection, selectionArgs);
				break;
			case AID_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(AID_TABLE_NAME, TxDB.Aid._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case MSGSTAT:
				count = db.delete(MSG_TRIGGER_NAME, selection, selectionArgs);
				break;
			case MSGSTAT_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(MSG_TRIGGER_NAME, TxDB.MsgStat._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case QUN:
				count = db.delete(QU_TABLE_NAME, selection, selectionArgs);
				break;
			case QUN_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(QU_TABLE_NAME, TxDB.Qun._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case PRAISE_NOTICE:
				count = db.delete(NOTICE_PRAISE_TABLE_NAME, selection,
						selectionArgs);
				break;
			case PRAISE_NOTICE_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(
						NOTICE_PRAISE_TABLE_NAME,
						TxDB.PraiseNotice._ID
								+ "="
								+ segment
								+ (!TextUtils.isEmpty(selection) ? " AND ("
										+ selection + ')' : ""), selectionArgs);
				break;
			case LIKE_NOTICE:
				count = db.delete(NOTICE_LIKE_TABLE_NAME, selection,
						selectionArgs);
				break;
			case LIKE_NOTICE_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(NOTICE_LIKE_TABLE_NAME, TxDB.LikeNotice._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case BLOG:
				count = db.delete(BLOG_TABLE_NAME, selection, selectionArgs);
				break;
			case BLOG_ID:
				segment = uri.getPathSegments().get(1);
				count = db.delete(BLOG_TABLE_NAME, TxDB.Blog._ID
						+ "="
						+ segment
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);

			}
		}
		return count;
	}

	@Override
	public String getType(Uri uri) {
		switch (URL_MATCHER.match(uri)) {
		case MSGS:
			return TxDB.Messages.CONTENT_TYPE;
		case MSGS_ID:
			return TxDB.Messages.CONTENT_ITEM_TYPE;
		case TXS:
			return TxDB.Tx.CONTENT_TYPE;
		case TXS_ID:
			return TxDB.Tx.CONTENT_ITEM_TYPE;
		case AID:
			return TxDB.Aid.CONTENT_TYPE;
		case AID_ID:
			return TxDB.Aid.CONTENT_ITEM_TYPE;
		case MSGSTAT:
			return TxDB.MsgStat.CONTENT_ITEM_TYPE;
		case MSGSTAT_ID:
			return TxDB.MsgStat.CONTENT_ITEM_TYPE;
		case QUN:
			return TxDB.Qun.CONTENT_ITEM_TYPE;
		case QUN_ID:
			return TxDB.Qun.CONTENT_ITEM_TYPE;
		case PRAISE_NOTICE:
			return TxDB.PraiseNotice.CONTENT_ITEM_TYPE;
		case PRAISE_NOTICE_ID:
			return TxDB.PraiseNotice.CONTENT_ITEM_TYPE;
		case LIKE_NOTICE:
			return TxDB.LikeNotice.CONTENT_ITEM_TYPE;
		case LIKE_NOTICE_ID:
			return TxDB.LikeNotice.CONTENT_ITEM_TYPE;
		case BLOG:
			return TxDB.Blog.CONTENT_ITEM_TYPE;
		case BLOG_ID:
			return TxDB.Blog.CONTENT_ITEM_TYPE;
		default:
			throw new IllegalArgumentException("Unknown URL " + uri);
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues initvalues) {
		if (mOpenHelper == null)
			return null;
		// synchronized (mOpenHelper) {
		ContentValues values;
		long rowId;
		TimeZone t = TimeZone.getTimeZone("GMT+8");
		Locale l = new Locale("zh", "CN");
		Calendar cal = Calendar.getInstance(t, l);
		Long now = Long.valueOf(cal.getTimeInMillis());
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		String table;
		String nullColumnHack;
		Uri contentUri;

		if (initvalues != null) {
			values = new ContentValues(initvalues);
		} else {
			values = new ContentValues();
		}

		// ContentValues subValues = null;// 插入到好友表中的values
		switch (URL_MATCHER.match(uri)) {
		case MSGS:
			table = MSGS_TABLE_NAME;
			nullColumnHack = TxDB.Messages.MSG_BODY;
			contentUri = TxDB.Messages.CONTENT_URI;
			if (values.containsKey(TxDB.Messages.SEND_TIME) == false)
				values.put(TxDB.Messages.SEND_TIME, now);
			if (values.containsKey(TxDB.Messages.MSG_BODY) == false)
				values.put(TxDB.Messages.MSG_BODY, "");
			if (values.containsKey(TxDB.Messages.MSG_PARTNER_NAME) == false)
				values.put(TxDB.Messages.MSG_PARTNER_NAME, "");
			if (values.containsKey(TxDB.Messages.MSG_TYPE) == false)
				values.put(TxDB.Messages.MSG_TYPE, TxDB.MSG_TYPE_COMMON_SMS);
			break;

		// 插入好友联系人
		case TX_FRIENDS:
			table = TX_FRIEND_TABLE_NAME;
			nullColumnHack = TxDB.TX_Friends.REMARK_NAME;
			contentUri = TxDB.TX_Friends.CONTENT_URI;

			break;

		case TXS:
			table = DatabaseHelper.TXS_TABLE_NAME;
			nullColumnHack = TxDB.Tx.PHONE;
			contentUri = TxDB.Tx.CONTENT_URI;
			if (values.containsKey(TxDB.Tx.TX_ID) == false)
				values.put(TxDB.Tx.TX_ID, "");
			if (values.containsKey(TxDB.Tx.PHONE) == false)
				values.put(TxDB.Tx.PHONE, "");
			break;

		case AID:
			table = AID_TABLE_NAME;
			nullColumnHack = TxDB.Aid.UP_PHONES;
			contentUri = TxDB.Aid.CONTENT_URI;
			if (values.containsKey(TxDB.Aid.UP_PHONES) == false)
				values.put(TxDB.Aid.UP_PHONES, "");
			break;
		case MSGSTAT:
			table = MSG_TRIGGER_NAME;
			nullColumnHack = TxDB.MsgStat.MSG_PARTNER_ID;
			contentUri = TxDB.MsgStat.CONTENT_URI;
			if (values.containsKey(TxDB.MsgStat.MSG_PARTNER_ID) == false)
				values.put(TxDB.MsgStat.MSG_PARTNER_ID, 0);
			break;
		case QUN:
			table = QU_TABLE_NAME;
			nullColumnHack = TxDB.Qun._ID;
			contentUri = TxDB.Qun.CONTENT_URI;
			break;
		case PRAISE_NOTICE:
			table = NOTICE_PRAISE_TABLE_NAME;
			nullColumnHack = TxDB.PraiseNotice._ID;
			contentUri = TxDB.PraiseNotice.CONTENT_URI;
			break;
		case LIKE_NOTICE:
			table = NOTICE_LIKE_TABLE_NAME;
			nullColumnHack = TxDB.LikeNotice._ID;
			contentUri = TxDB.LikeNotice.CONTENT_URI;
			break;
		case BLOG:
			table = BLOG_TABLE_NAME;
			nullColumnHack = TxDB.Blog._ID;
			contentUri = TxDB.Blog.CONTENT_URI;
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		try {

			// if (URL_MATCHER.match(uri) == TXS) {
			// // 操作子数据库
			//
			// synchronized (mSubWriteLock) {
			// rowId = db.insert(
			// DatabaseHelper.TXS_TABLE_NAME, nullColumnHack, values);//
			// 插入子数据库普通联系人表字段
			// }
			//
			// } else {
			// // 操作主库

			synchronized (mWriteLock) {
				rowId = db.insert(table, nullColumnHack, values);
			}
			// }
			if (rowId > 0) {
				Uri newUri = ContentUris.withAppendedId(contentUri, rowId);
				getContext().getContentResolver().notifyChange(newUri, null);
				return newUri;
			}

		} catch (Exception e) {
			if (Utils.debug)
				e.printStackTrace();
			return null;
		}

		throw new SQLException("Failed to insert row into " + uri);

	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		if (mOpenHelper == null)
			return null;
		// Log.i(TAG, uri.toString());
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		// String limit=null;
		switch (URL_MATCHER.match(uri)) {
		case MSGS:
			qb.setTables(MSGS_TABLE_NAME);
			qb.setProjectionMap(sMsgProjectionMap);
			break;

		case MSGS_ID:
			qb.setTables(MSGS_TABLE_NAME);
			qb.setProjectionMap(sMsgProjectionMap);
			qb.appendWhere(TxDB.Messages.MSG_ID + "="
					+ uri.getPathSegments().get(1));
			// limit="0,100";
			break;
		case TXS:
			qb.setTables(DatabaseHelper.TXS_TABLE_NAME);
			qb.setProjectionMap(sTxProjectionMap);
			break;

		case TXS_ID:
			qb.setTables(DatabaseHelper.TXS_TABLE_NAME);
			qb.setProjectionMap(sTxProjectionMap);
			qb.appendWhere(TxDB.Tx._ID + "=" + uri.getPathSegments().get(1));
			break;
		// 查询好友视图
		case TX_FRIENDS:
			qb.setTables(VIEW_FRIENDS);
			qb.setProjectionMap(sFriendProjectionMap);
			break;

		case TX_FRIENDS_ID:
			qb.setTables(VIEW_FRIENDS);
			qb.setProjectionMap(sFriendProjectionMap);
			qb.appendWhere(TxDB.Tx._ID + "=" + uri.getPathSegments().get(1));
			break;

		case AID:
			qb.setTables(AID_TABLE_NAME);
			qb.setProjectionMap(sAidProjectionMap);
			break;
		case AID_ID:
			qb.setTables(AID_TABLE_NAME);
			qb.setProjectionMap(sAidProjectionMap);
			qb.appendWhere(TxDB.Aid._ID + "=" + uri.getPathSegments().get(1));
			break;
		case MSGSTAT:
			qb.setTables(MSG_TRIGGER_NAME);
			qb.setProjectionMap(sMsgstatProjectionMap);
			break;
		case MSGSTAT_ID:
			qb.setTables(MSG_TRIGGER_NAME);
			qb.setProjectionMap(sMsgstatProjectionMap);
			qb.appendWhere(TxDB.MsgStat._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case QUN:
			qb.setTables(QU_TABLE_NAME);
			qb.setProjectionMap(sQunProjectionMap);
			break;
		case QUN_ID:
			qb.setTables(QU_TABLE_NAME);
			qb.setProjectionMap(sQunProjectionMap);
			qb.appendWhere(TxDB.Qun._ID + "=" + uri.getPathSegments().get(1));
			break;
		case PRAISE_NOTICE:
			qb.setTables(NOTICE_PRAISE_TABLE_NAME);
			qb.setProjectionMap(sPraisedNoticeProjectionMap);
			break;
		case PRAISE_NOTICE_ID:
			qb.setTables(NOTICE_PRAISE_TABLE_NAME);
			qb.setProjectionMap(sPraisedNoticeProjectionMap);
			qb.appendWhere(TxDB.PraiseNotice._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case LIKE_NOTICE:
			qb.setTables(NOTICE_LIKE_TABLE_NAME);
			qb.setProjectionMap(sLikedNoticeProjectionMap);
			break;
		case LIKE_NOTICE_ID:
			qb.setTables(NOTICE_LIKE_TABLE_NAME);
			qb.setProjectionMap(sLikedNoticeProjectionMap);
			qb.appendWhere(TxDB.LikeNotice._ID + "="
					+ uri.getPathSegments().get(1));
			break;
		case BLOG:
			qb.setTables(BLOG_TABLE_NAME);
			qb.setProjectionMap(sBlogProjectionMap);
			break;
		case BLOG_ID:
			qb.setTables(BLOG_TABLE_NAME);
			qb.setProjectionMap(sBlogProjectionMap);
			qb.appendWhere(TxDB.Blog._ID + "=" + uri.getPathSegments().get(1));
			break;
		default: {
			throw new IllegalArgumentException("Unknown URI " + uri);
		}
		}

		// If no sort order is specified use the default
		String orderBy;
		if (TextUtils.isEmpty(sortOrder)) {
			switch (URL_MATCHER.match(uri)) {
			case MSGS:
			case MSGS_ID:
				orderBy = TxDB.Messages.DEFAULT_SORT_ORDER;
				break;
			case TX_FRIENDS:
			case TX_FRIENDS_ID:
			case TXS:
			case TXS_ID:
				orderBy = TxDB.Tx.DEFAULT_SORT_ORDER;
				break;

			case AID:
			case AID_ID:
				orderBy = TxDB.Aid.DEFAULT_SORT_ORDER;
				break;
			case MSGSTAT:
			case MSGSTAT_ID:
				orderBy = TxDB.MsgStat.DEFAULT_SORT_ORDER;
				break;
			case QUN:
			case QUN_ID:
				orderBy = TxDB.Qun.DEFAULT_SORT_ORDER;
				break;
			case PRAISE_NOTICE:
			case PRAISE_NOTICE_ID:
				orderBy = TxDB.PraiseNotice.DEFAULT_SORT_ORDER;
				break;
			case LIKE_NOTICE:
			case LIKE_NOTICE_ID:
				orderBy = TxDB.LikeNotice.DEFAULT_SORT_ORDER;
				break;
			case BLOG:
			case BLOG_ID:
				orderBy = TxDB.Blog.DEFAULT_SORT_ORDER;
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		} else {
			orderBy = sortOrder;
		}

		// distinct默认为false,需要时distinct = true 查询后改为false
		qb.setDistinct(distinct);

		// 从库已经加载到主库上，所以db对象应该用同一个 2014.05.26 shc
		// SQLiteDatabase db = null;
		// if (URL_MATCHER.match(uri) == TXS || URL_MATCHER.match(uri) ==
		// TXS_ID) {
		// // 只查询普通联系人，用子数据库
		// db = mSubHelper.getReadableDatabase();
		// } else {
		// // 主数据库
		// db = mOpenHelper.getReadableDatabase();
		// }

		SQLiteDatabase db = mOpenHelper.getReadableDatabase();

		// limit默认为null,需要时设置 ,查询后改为null
		Cursor c = qb.query(db, projection, selection, selectionArgs, groupby,
				having, orderBy, limit);
		c.setNotificationUri(getContext().getContentResolver(), uri);

		return c;

	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		if (mOpenHelper == null)
			return 0;
		// synchronized (mOpenHelper) {
		// 因为这里报过NullPointer Exception，所以加此判断
		if (mOpenHelper == null)
			return 0;
		SQLiteDatabase db = mOpenHelper.getWritableDatabase();
		int count;
		synchronized (mWriteLock) {
			switch (URL_MATCHER.match(uri)) {
			case MSGS:
				count = db.update(MSGS_TABLE_NAME, values, selection,
						selectionArgs);
				break;

			case MSGS_ID:
				String noteId = uri.getPathSegments().get(1);
				count = db.update(MSGS_TABLE_NAME, values, TxDB.Messages.MSG_ID
						+ "="
						+ noteId
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;

			// 更新好友视图
			case TX_FRIENDS:
				// ContentValues subValues = removeNormalValues(values);
				// count = db.update(TX_FRIEND_TABLE_NAME, subValues, selection,
				// selectionArgs);
				count = db.update(TX_FRIEND_TABLE_NAME, values, selection,
						selectionArgs);

				break;
			case TXS:

				// synchronized (mSubWriteLock) {
				count = db.update(DatabaseHelper.TXS_TABLE_NAME, values,
						selection, selectionArgs);
				// }
				break;

			// 更新好友视图
			case TX_FRIENDS_ID:
				// subValues = removeNormalValues(values);
				//
				// String Id = uri.getPathSegments().get(1);
				// count = db.update(TX_FRIEND_TABLE_NAME, values, TxDB.Tx._ID
				// + "="
				// + Id
				// + (!TextUtils.isEmpty(selection) ? " AND (" + selection
				// + ')' : ""), selectionArgs);

				String Id = uri.getPathSegments().get(1);
				count = db.update(TX_FRIEND_TABLE_NAME, values, TxDB.Tx._ID
						+ "="
						+ Id
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);

				break;
			case TXS_ID:
				Id = uri.getPathSegments().get(1);

				// synchronized (mSubWriteLock) {
				String tempId = uri.getPathSegments().get(1);
				count = db.update(
						DatabaseHelper.TXS_TABLE_NAME,
						values,
						TxDB.Tx._ID
								+ "="
								+ tempId
								+ (!TextUtils.isEmpty(selection) ? " AND ("
										+ selection + ')' : ""), selectionArgs);
				// }

				// String Id = uri.getPathSegments().get(1);
				// count = db.update(TX_TABLE_NAME, values, TxDB.Tx._ID
				// + "="
				// + Id
				// + (!TextUtils.isEmpty(selection) ? " AND (" + selection
				// + ')' : ""), selectionArgs);
				break;

			case AID:
				count = db.update(AID_TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case AID_ID:
				String aId = uri.getPathSegments().get(1);
				count = db.update(AID_TABLE_NAME, values, TxDB.Aid._ID
						+ "="
						+ aId
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case MSGSTAT:
				count = db.update(MSG_TRIGGER_NAME, values, selection,
						selectionArgs);
				break;
			case MSGSTAT_ID:
				String statId = uri.getPathSegments().get(1);
				count = db.update(MSG_TRIGGER_NAME, values, TxDB.MsgStat._ID
						+ "="
						+ statId
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case QUN:
				count = db.update(QU_TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case QUN_ID:
				String qunId = uri.getPathSegments().get(1);
				count = db.update(QU_TABLE_NAME, values, TxDB.Qun._ID
						+ "="
						+ qunId
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			case PRAISE_NOTICE:
				count = db.update(NOTICE_PRAISE_TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case PRAISE_NOTICE_ID:
				String praiseNoticeGmid = uri.getPathSegments().get(1);
				count = db.update(NOTICE_PRAISE_TABLE_NAME, values,
						TxDB.PraiseNotice._ID
								+ "="
								+ praiseNoticeGmid
								+ (!TextUtils.isEmpty(selection) ? " AND ("
										+ selection + ')' : ""), selectionArgs);
				break;
			case LIKE_NOTICE:
				count = db.update(NOTICE_LIKE_TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case LIKE_NOTICE_ID:
				String blogid = uri.getPathSegments().get(1);
				count = db.update(
						NOTICE_LIKE_TABLE_NAME,
						values,
						TxDB.LikeNotice._ID
								+ "="
								+ blogid
								+ (!TextUtils.isEmpty(selection) ? " AND ("
										+ selection + ')' : ""), selectionArgs);
				break;
			case BLOG:
				count = db.update(BLOG_TABLE_NAME, values, selection,
						selectionArgs);
				break;
			case BLOG_ID:
				blogid = uri.getPathSegments().get(1);
				count = db.update(BLOG_TABLE_NAME, values, TxDB.Blog._ID
						+ "="
						+ blogid
						+ (!TextUtils.isEmpty(selection) ? " AND (" + selection
								+ ')' : ""), selectionArgs);
				break;
			default:
				throw new IllegalArgumentException("Unknown URI " + uri);
			}
		}
		return count;

	}

	// /**挑出好友表中的特殊字段*/
	// private ContentValues removeNormalValues(ContentValues subValues) {
	// ContentValues values = new ContentValues();
	// if (subValues.containsKey(TxDB.Tx.TX_ID)) {
	// values.put(TxDB.Tx.TX_ID,
	// (Integer) values.get(TxDB.Tx.TX_ID));
	// subValues.remove(TxDB.Tx.TX_ID);
	// }
	// if (subValues.containsKey(TxDB.Tx.IS_STAR_FRIEND)) {
	// values.put(TxDB.Tx.IS_STAR_FRIEND,
	// (Integer) values.get(TxDB.Tx.IS_STAR_FRIEND));
	// subValues.remove(TxDB.Tx.IS_STAR_FRIEND);
	// }
	// if (subValues.containsKey(TxDB.Tx.REMARK_NAME)) {
	// values.put(TxDB.Tx.REMARK_NAME,
	// (Integer) values.get(TxDB.Tx.REMARK_NAME));
	// subValues.remove(TxDB.Tx.REMARK_NAME);
	// }
	// if (subValues.containsKey(TxDB.Tx.CONTACTS_PERSON_NAME)) {
	// values.put(TxDB.Tx.CONTACTS_PERSON_NAME,
	// (Integer) values.get(TxDB.Tx.CONTACTS_PERSON_NAME));
	// subValues.remove(TxDB.Tx.CONTACTS_PERSON_NAME);
	// }
	// return values;
	//
	// }

	public static Uri updateAid(ContentResolver contentResolver, String phone) {
		// {"partner_id":"2381621377","display_name":"实诚人","user_name":"实诚","PHONE":"13911115555","email":"piaoliuwuji@126.com","sex":"f","in_roster":false,"is_stub":false}
		synchronized (lock) {
			ContentValues values = new ContentValues();
			// Construct the Uri to existing record
			// BLog.w("provider33333333", "save:"+phone);
			phone = Utils.filterNumber(phone);
			// BLog.w("provider22222222", "save:"+phone);
			if (Utils.isNull(phone)) {
				return null;
			}
			Long lContactsId = Utils.creatPhoneid(phone, 0);
			Uri aContactsUri = ContentUris.withAppendedId(TxDB.Aid.CONTENT_URI,
					lContactsId);
			values.put(BaseColumns._ID, lContactsId.toString());
			values.put(TxDB.Aid.UP_PHONES, phone);
			// BLog.w("provider", "save:"+phone);
			if ((contentResolver.update(aContactsUri, values, null, null)) == 0) {
				try {
					contentResolver.insert(TxDB.Aid.CONTENT_URI, values);
				} catch (Exception e) {
					if (Utils.debug)
						Log.e(TAG, "updateAid()异常：", e);
					return null;
				} catch (Error e) {
					if (Utils.debug)
						Log.e(TAG, "updateAid()错误ERROR：", e);
					return null;
				}
			}
			return aContactsUri;
		}
	}

	static {
		URL_MATCHER = new UriMatcher(UriMatcher.NO_MATCH);
		URL_MATCHER.addURI(AUTHORITY, MSGS_TABLE_NAME, MSGS);
		URL_MATCHER.addURI(AUTHORITY, MSGS_TABLE_NAME + "/#", MSGS_ID);
		// URL_MATCHER.addURI(AUTHORITY, TX_TABLE_NAME, TXS);
		// URL_MATCHER.addURI(AUTHORITY, TX_TABLE_NAME + "/#", TXS_ID);
		URL_MATCHER.addURI(AUTHORITY, SubDBHelper.TXS_TABLE_NAME, TXS);
		URL_MATCHER
				.addURI(AUTHORITY, SubDBHelper.TXS_TABLE_NAME + "/#", TXS_ID);
		/** 好友联系人 */
		URL_MATCHER.addURI(AUTHORITY, TX_FRIEND_TABLE_NAME, TX_FRIENDS);
		URL_MATCHER.addURI(AUTHORITY, TX_FRIEND_TABLE_NAME + "/#",
				TX_FRIENDS_ID);

		URL_MATCHER.addURI(AUTHORITY, AID_TABLE_NAME, AID);
		URL_MATCHER.addURI(AUTHORITY, AID_TABLE_NAME + "/#", AID_ID);
		URL_MATCHER.addURI(AUTHORITY, MSG_TRIGGER_NAME, MSGSTAT);
		URL_MATCHER.addURI(AUTHORITY, MSG_TRIGGER_NAME + "/#", MSGSTAT_ID);
		URL_MATCHER.addURI(AUTHORITY, QU_TABLE_NAME, QUN);
		URL_MATCHER.addURI(AUTHORITY, QU_TABLE_NAME + "/#", QUN_ID);
		// 被赞通知表
		URL_MATCHER.addURI(AUTHORITY, NOTICE_PRAISE_TABLE_NAME, PRAISE_NOTICE);
		URL_MATCHER.addURI(AUTHORITY, NOTICE_PRAISE_TABLE_NAME + "/#",
				PRAISE_NOTICE_ID);
		// 被喜欢通知表
		URL_MATCHER.addURI(AUTHORITY, NOTICE_LIKE_TABLE_NAME, LIKE_NOTICE);
		URL_MATCHER.addURI(AUTHORITY, NOTICE_LIKE_TABLE_NAME + "/#",
				LIKE_NOTICE_ID);
		// 瞬间消息表
		URL_MATCHER.addURI(AUTHORITY, BLOG_TABLE_NAME, BLOG);
		URL_MATCHER.addURI(AUTHORITY, BLOG_TABLE_NAME + "/#", BLOG_ID);

		sMsgProjectionMap = new HashMap<String, String>();
		sMsgProjectionMap.put(TxDB.Messages._ID, TxDB.Messages._ID);
		sMsgProjectionMap.put(TxDB.Messages.MSG_TYPE, TxDB.Messages.MSG_TYPE);
		sMsgProjectionMap.put(TxDB.Messages.MSG_ID, TxDB.Messages.MSG_ID);
		sMsgProjectionMap.put(TxDB.Messages.MSG_SUBJECT,
				TxDB.Messages.MSG_SUBJECT);
		sMsgProjectionMap.put(TxDB.Messages.MSG_BODY, TxDB.Messages.MSG_BODY);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PATH, TxDB.Messages.MSG_PATH);
		sMsgProjectionMap.put(TxDB.Messages.MSG_URL, TxDB.Messages.MSG_URL);
		sMsgProjectionMap.put(TxDB.Messages.MSG_FILE_LENGTH,
				TxDB.Messages.MSG_FILE_LENGTH);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PARTNER_ID,
				TxDB.Messages.MSG_PARTNER_ID);
		sMsgProjectionMap.put(TxDB.Messages.MSG_GROUP_ID,
				TxDB.Messages.MSG_GROUP_ID);
		sMsgProjectionMap.put(TxDB.Messages.MSG_GROUP_NAME,
				TxDB.Messages.MSG_GROUP_NAME);
		sMsgProjectionMap.put(TxDB.Messages.MSG_GROUP_URL,
				TxDB.Messages.MSG_GROUP_URL);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PARTNER_NAME,
				TxDB.Messages.MSG_PARTNER_NAME);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PARTNER_PHONE,
				TxDB.Messages.MSG_PARTNER_PHONE);
		sMsgProjectionMap.put(TxDB.Messages.CONTACTS_PERSON_ID,
				TxDB.Messages.CONTACTS_PERSON_ID);
		sMsgProjectionMap.put(TxDB.Messages.CONTACTS_PERSON_NAME,
				TxDB.Messages.CONTACTS_PERSON_NAME);
		// sMsgProjectionMap.put(TxDB.Messages.SMS_ADDRESS,
		// TxDB.Messages.SMS_ADDRESS);
		sMsgProjectionMap.put(TxDB.Messages.AUD_END, TxDB.Messages.AUD_END);
		sMsgProjectionMap.put(TxDB.Messages.AUD_TIMES, TxDB.Messages.AUD_TIMES);
		sMsgProjectionMap.put(TxDB.Messages.GEO, TxDB.Messages.GEO);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_ID, TxDB.Messages.TCARD_ID);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_NAME,
				TxDB.Messages.TCARD_NAME);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_SEX, TxDB.Messages.TCARD_SEX);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_SIGN,
				TxDB.Messages.TCARD_SIGN);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_PHONE,
				TxDB.Messages.TCARD_PHONE);
		sMsgProjectionMap.put(TxDB.Messages.TCARD_AVATAR_URL,
				TxDB.Messages.TCARD_AVATAR_URL);
		sMsgProjectionMap.put(TxDB.Messages.AGREE, TxDB.Messages.AGREE);
		sMsgProjectionMap.put(TxDB.Messages.AC, TxDB.Messages.AC);
		sMsgProjectionMap.put(TxDB.Messages.SNS_ID, TxDB.Messages.SNS_ID);
		sMsgProjectionMap.put(TxDB.Messages.SNS_NAME, TxDB.Messages.SNS_NAME);
		sMsgProjectionMap.put(TxDB.Messages.MSG_STATE, TxDB.Messages.MSG_STATE);
		sMsgProjectionMap.put(TxDB.Messages.READ_STATE,
				TxDB.Messages.READ_STATE);
		sMsgProjectionMap.put(TxDB.Messages.UPDATE_STATE,
				TxDB.Messages.UPDATE_STATE);
		sMsgProjectionMap.put(TxDB.Messages.WASME, TxDB.Messages.WASME);
		sMsgProjectionMap.put(TxDB.Messages.SEND_TIME, TxDB.Messages.SEND_TIME);
		sMsgProjectionMap.put(TxDB.Messages.CHANNEL_ID,
				TxDB.Messages.CHANNEL_ID);
		sMsgProjectionMap.put(TxDB.Messages.GMID, TxDB.Messages.GMID);
		sMsgProjectionMap.put(TxDB.Messages.GROUP_ID_NOTICE,
				TxDB.Messages.GROUP_ID_NOTICE);
		sMsgProjectionMap.put(TxDB.Messages.SN, TxDB.Messages.SN);
		sMsgProjectionMap.put(TxDB.Messages.RS, TxDB.Messages.RS);
		sMsgProjectionMap.put(TxDB.Messages.OP, TxDB.Messages.OP);
		sMsgProjectionMap.put(TxDB.Messages.MSG_SEX, TxDB.Messages.MSG_SEX);
		sMsgProjectionMap.put(TxDB.Messages.OP_ID, TxDB.Messages.OP_ID);
		sMsgProjectionMap.put(TxDB.Messages.OP_NAME, TxDB.Messages.OP_NAME);
		sMsgProjectionMap.put(TxDB.Messages.MSG_ID_2, TxDB.Messages.MSG_ID_2);
		sMsgProjectionMap.put(TxDB.Messages.MSG_TYPE_2,
				TxDB.Messages.MSG_TYPE_2);
		sMsgProjectionMap.put(TxDB.Messages.REPORT_ID, TxDB.Messages.REPORT_ID);
		sMsgProjectionMap.put(TxDB.Messages.REPORT_NAME,
				TxDB.Messages.REPORT_NAME);
		sMsgProjectionMap.put(TxDB.Messages.REPORT_CONTEXT,
				TxDB.Messages.REPORT_CONTEXT);
		sMsgProjectionMap.put(TxDB.Messages.SHUTUP_ST, TxDB.Messages.SHUTUP_ST);
		sMsgProjectionMap.put(TxDB.Messages.SHUTUP_DU, TxDB.Messages.SHUTUP_DU);
		sMsgProjectionMap.put(TxDB.Messages.PRAISE_STATE,
				TxDB.Messages.PRAISE_STATE);
		sMsgProjectionMap.put(TxDB.Messages.FILE_DOWN_TIME,
				TxDB.Messages.FILE_DOWN_TIME);
		sMsgProjectionMap.put(TxDB.Messages.MSG_EMOD5, TxDB.Messages.MSG_EMOD5);
		sMsgProjectionMap.put(TxDB.Messages.MSG_NUM, TxDB.Messages.MSG_NUM);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PKGID, TxDB.Messages.MSG_PKGID);
		sMsgProjectionMap.put(TxDB.Messages.MSG_PKG_EMOMD5,
				TxDB.Messages.MSG_PKG_EMOMD5);

		sTxProjectionMap = new HashMap<String, String>();
		sTxProjectionMap.put(TxDB.Tx._ID, TxDB.Tx._ID);
		sTxProjectionMap.put(TxDB.Tx.DISPLAY_NAME, TxDB.Tx.DISPLAY_NAME);
		sTxProjectionMap.put(TxDB.Tx.TX_ID, TxDB.Tx.TX_ID);
		sTxProjectionMap.put(TxDB.Tx.SEX, TxDB.Tx.SEX);
		sTxProjectionMap.put(TxDB.Tx.AVATAR_MD5, TxDB.Tx.AVATAR_MD5);
		sTxProjectionMap.put(TxDB.Tx.AVATAR_URL, TxDB.Tx.AVATAR_URL);
		sTxProjectionMap.put(TxDB.Tx.BIRTHDAY, TxDB.Tx.BIRTHDAY);
		sTxProjectionMap.put(TxDB.Tx.BLOOD_TYPE, TxDB.Tx.BLOOD_TYPE);
		sTxProjectionMap.put(TxDB.Tx.HOBBY, TxDB.Tx.HOBBY);
		sTxProjectionMap.put(TxDB.Tx.HOME, TxDB.Tx.HOME);
		sTxProjectionMap.put(TxDB.Tx.DISTANCE, TxDB.Tx.DISTANCE);
		sTxProjectionMap.put(TxDB.Tx.AGE, TxDB.Tx.AGE);
		sTxProjectionMap.put(TxDB.Tx.CONSTELLATION, TxDB.Tx.CONSTELLATION);
		sTxProjectionMap.put(TxDB.Tx.PROFESSION, TxDB.Tx.PROFESSION);
		sTxProjectionMap.put(TxDB.Tx.EMAIL, TxDB.Tx.EMAIL);
		sTxProjectionMap.put(TxDB.Tx.IS_E_BIND, TxDB.Tx.IS_E_BIND);
		sTxProjectionMap.put(TxDB.Tx.IS_P_BIND, TxDB.Tx.IS_P_BIND);
		sTxProjectionMap.put(TxDB.Tx.PHONE, TxDB.Tx.PHONE);
		sTxProjectionMap.put(TxDB.Tx.TX_SIGN, TxDB.Tx.TX_SIGN);
		sTxProjectionMap.put(TxDB.Tx.SECOND_CHAR, TxDB.Tx.SECOND_CHAR);
		sTxProjectionMap.put(TxDB.Tx.LANGUAGES, TxDB.Tx.LANGUAGES);
		sTxProjectionMap.put(Tx.ALBUM, Tx.ALBUM);
		sTxProjectionMap.put(Tx.ISOP, Tx.ISOP);
		sTxProjectionMap.put(Tx.ALBUM_VER, Tx.ALBUM_VER);
		sTxProjectionMap.put(Tx.INFO_VER, Tx.INFO_VER);
		sTxProjectionMap.put(Tx.IS_RECEIVE_REQ, Tx.IS_RECEIVE_REQ);
		sTxProjectionMap.put(Tx.BLOG_INFOR, Tx.BLOG_INFOR);
		sTxProjectionMap.put(Tx.LEVLE, Tx.LEVLE);

		sAidProjectionMap = new HashMap<String, String>();
		sAidProjectionMap.put(TxDB.Aid._ID, TxDB.Aid._ID);
		sAidProjectionMap.put(TxDB.Aid.UP_PHONES, TxDB.Aid.UP_PHONES);
		sQunProjectionMap = new HashMap<String, String>();
		sQunProjectionMap.put(TxDB.Qun._ID, TxDB.Qun._ID);
		sQunProjectionMap.put(TxDB.Qun.QU_ID, TxDB.Qun.QU_ID);
		sQunProjectionMap.put(TxDB.Qun.QU_VER, TxDB.Qun.QU_VER);
		sQunProjectionMap.put(TxDB.Qun.QU_TYPE, TxDB.Qun.QU_TYPE);
		sQunProjectionMap.put(TxDB.Qun.QU_DISPLAY_NAME,
				TxDB.Qun.QU_DISPLAY_NAME);
		sQunProjectionMap.put(TxDB.Qun.QU_SIGN, TxDB.Qun.QU_SIGN);
		sQunProjectionMap.put(TxDB.Qun.QU_TX_STATE, TxDB.Qun.QU_TX_STATE);
		sQunProjectionMap.put(TxDB.Qun.QU_OWN_ID, TxDB.Qun.QU_OWN_ID);
		sQunProjectionMap.put(TxDB.Qun.QU_OWN_NAME, TxDB.Qun.QU_OWN_NAME);
		sQunProjectionMap.put(TxDB.Qun.QU_TIME, TxDB.Qun.QU_TIME);
		sQunProjectionMap.put(TxDB.Qun.QU_TX_IDS, TxDB.Qun.QU_TX_IDS);
		sQunProjectionMap.put(TxDB.Qun.QU_AVATAR, TxDB.Qun.QU_AVATAR);
		sQunProjectionMap.put(TxDB.Qun.QU_TX_ADMIN_IDS,
				TxDB.Qun.QU_TX_ADMIN_IDS);
		sQunProjectionMap.put(TxDB.Qun.QU_TX_ADMIN_NAMES,
				TxDB.Qun.QU_TX_ADMIN_NAMES);
		sQunProjectionMap.put(TxDB.Qun.QU_BULLETIN, TxDB.Qun.QU_BULLETIN);
		sQunProjectionMap.put(TxDB.Qun.QU_SN, TxDB.Qun.QU_SN);
		sQunProjectionMap.put(TxDB.Qun.QU_TYPE_CHANNEL,
				TxDB.Qun.QU_TYPE_CHANNEL);
		sQunProjectionMap.put(TxDB.Qun.QU_RCV_MSG, TxDB.Qun.QU_RCV_MSG);
		sQunProjectionMap.put(TxDB.Qun.QU_RCV_PUSH, TxDB.Qun.QU_RCV_PUSH);
		sQunProjectionMap.put(TxDB.Qun.QU_INDEX, TxDB.Qun.QU_INDEX);
		sQunProjectionMap.put(TxDB.Qun.QU_ACCESS_TIME, TxDB.Qun.QU_ACCESS_TIME);
		sQunProjectionMap.put(TxDB.Qun.ALL_NUM, TxDB.Qun.ALL_NUM);
		sQunProjectionMap.put(TxDB.Qun.OL_NUM, TxDB.Qun.OL_NUM);

		sMsgstatProjectionMap = new HashMap<String, String>();
		sMsgstatProjectionMap.put(TxDB.MsgStat._ID, TxDB.MsgStat._ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.CONTACTS_PERSON_ID,
				TxDB.MsgStat.CONTACTS_PERSON_ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.CONTACTS_PERSON_NAME,
				TxDB.MsgStat.CONTACTS_PERSON_NAME);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_ID, TxDB.MsgStat.MSG_ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_BODY, TxDB.MsgStat.MSG_BODY);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_COUT, TxDB.MsgStat.MSG_COUT);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_DATE, TxDB.MsgStat.MSG_DATE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_GROUP_ID,
				TxDB.MsgStat.MSG_GROUP_ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_GROUP_NAME,
				TxDB.MsgStat.MSG_GROUP_NAME);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_GROUP_DISPLAY_AVATARS,
				TxDB.MsgStat.MSG_GROUP_DISPLAY_AVATARS);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_NOTREAD_COUT,
				TxDB.MsgStat.MSG_NOTREAD_COUT);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_TYPE, TxDB.MsgStat.MSG_TYPE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_PARTNER_ID,
				TxDB.MsgStat.MSG_PARTNER_ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.PHONE, TxDB.MsgStat.PHONE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.WASME, TxDB.MsgStat.WASME);
		sMsgstatProjectionMap.put(TxDB.MsgStat.READ_STATE,
				TxDB.MsgStat.READ_STATE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_DISPLAY_NAME,
				TxDB.MsgStat.MSG_DISPLAY_NAME);
		sMsgstatProjectionMap.put(TxDB.MsgStat.CHANNEL_ID,
				TxDB.MsgStat.CHANNEL_ID);
		sMsgstatProjectionMap.put(TxDB.MsgStat.GROUP_ID_NOTICE,
				TxDB.MsgStat.GROUP_ID_NOTICE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.GMID, TxDB.MsgStat.GMID);
		// 3.8.5新添加字段
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_STAT_TYPE,
				TxDB.MsgStat.MSG_STAT_TYPE);
		sMsgstatProjectionMap.put(TxDB.MsgStat.MSG_SESSION_ID,
				TxDB.MsgStat.MSG_SESSION_ID);

		/** 好友联系人map */
		sFriendProjectionMap = new HashMap<String, String>();
		// sFriendProjectionMap.put(TxDB.TX_Friends._ID, TxDB.TX_Friends._ID);
		sFriendProjectionMap.put(TxDB.TX_Friends.TX_ID, TxDB.TX_Friends.TX_ID);
		sFriendProjectionMap.put(TxDB.TX_Friends.IS_STAR_FRIEND,
				TxDB.TX_Friends.IS_STAR_FRIEND);
		sFriendProjectionMap.put(TxDB.TX_Friends.REMARK_NAME,
				TxDB.TX_Friends.REMARK_NAME);
		sFriendProjectionMap.put(TxDB.TX_Friends.CONTACTS_PERSON_NAME,
				TxDB.TX_Friends.CONTACTS_PERSON_NAME);
		sFriendProjectionMap.put(TxDB.TX_Friends.TX_TYPE,
				TxDB.TX_Friends.TX_TYPE);
		sFriendProjectionMap.put(TxDB.TX_Friends.IN_BLACK_TIME,
				TxDB.TX_Friends.IN_BLACK_TIME);

		// 普通联系人的map
		sFriendProjectionMap.put(TxDB.Tx.DISPLAY_NAME, TxDB.Tx.DISPLAY_NAME);
		sFriendProjectionMap.put(TxDB.Tx.SEX, TxDB.Tx.SEX);
		sFriendProjectionMap.put(TxDB.Tx.AVATAR_MD5, TxDB.Tx.AVATAR_MD5);
		sFriendProjectionMap.put(TxDB.Tx.AVATAR_URL, TxDB.Tx.AVATAR_URL);
		sFriendProjectionMap.put(TxDB.Tx.BIRTHDAY, TxDB.Tx.BIRTHDAY);
		sFriendProjectionMap.put(TxDB.Tx.BLOOD_TYPE, TxDB.Tx.BLOOD_TYPE);
		sFriendProjectionMap.put(TxDB.Tx.HOBBY, TxDB.Tx.HOBBY);
		sFriendProjectionMap.put(TxDB.Tx.HOME, TxDB.Tx.HOME);
		sFriendProjectionMap.put(TxDB.Tx.DISTANCE, TxDB.Tx.DISTANCE);
		sFriendProjectionMap.put(TxDB.Tx.AGE, TxDB.Tx.AGE);
		sFriendProjectionMap.put(TxDB.Tx.CONSTELLATION, TxDB.Tx.CONSTELLATION);
		sFriendProjectionMap.put(TxDB.Tx.PROFESSION, TxDB.Tx.PROFESSION);
		sFriendProjectionMap.put(TxDB.Tx.EMAIL, TxDB.Tx.EMAIL);
		sFriendProjectionMap.put(TxDB.Tx.IS_E_BIND, TxDB.Tx.IS_E_BIND);
		sFriendProjectionMap.put(TxDB.Tx.IS_P_BIND, TxDB.Tx.IS_P_BIND);
		sFriendProjectionMap.put(TxDB.Tx.PHONE, TxDB.Tx.PHONE);
		sFriendProjectionMap.put(TxDB.Tx.TX_SIGN, TxDB.Tx.TX_SIGN);
		sFriendProjectionMap.put(TxDB.Tx.SECOND_CHAR, TxDB.Tx.SECOND_CHAR);
		sFriendProjectionMap.put(TxDB.Tx.LANGUAGES, TxDB.Tx.LANGUAGES);
		sFriendProjectionMap.put(Tx.ALBUM, Tx.ALBUM);
		sFriendProjectionMap.put(Tx.ISOP, Tx.ISOP);
		sFriendProjectionMap.put(Tx.ALBUM_VER, Tx.ALBUM_VER);
		sFriendProjectionMap.put(Tx.INFO_VER, Tx.INFO_VER);
		sFriendProjectionMap.put(Tx.IS_RECEIVE_REQ, Tx.IS_RECEIVE_REQ);
		sFriendProjectionMap.put(Tx.BLOG_INFOR, Tx.BLOG_INFOR);
		sFriendProjectionMap.put(Tx.LEVLE, Tx.LEVLE);

		sPraisedNoticeProjectionMap = new HashMap<String, String>();
		sPraisedNoticeProjectionMap.put(TxDB.PraiseNotice.GROUP_ID,
				TxDB.PraiseNotice.GROUP_ID);
		sPraisedNoticeProjectionMap.put(TxDB.PraiseNotice.GMID,
				TxDB.PraiseNotice.GMID);
		sPraisedNoticeProjectionMap.put(TxDB.PraiseNotice.ID_LIST,
				TxDB.PraiseNotice.ID_LIST);
		sPraisedNoticeProjectionMap.put(TxDB.PraiseNotice.TIME,
				TxDB.PraiseNotice.TIME);

		sLikedNoticeProjectionMap = new HashMap<String, String>();
		sLikedNoticeProjectionMap.put(TxDB.LikeNotice.BLOG_ID,
				TxDB.LikeNotice.BLOG_ID);
		sLikedNoticeProjectionMap.put(TxDB.LikeNotice.UID, TxDB.LikeNotice.UID);
		sLikedNoticeProjectionMap.put(TxDB.LikeNotice.TIME,
				TxDB.LikeNotice.TIME);

		sBlogProjectionMap = new HashMap<String, String>();
		sBlogProjectionMap.put(TxDB.Blog.BLOG_ID, TxDB.Blog.BLOG_ID);
		sBlogProjectionMap.put(TxDB.Blog.BLOG_PUBLISH_ID,
				TxDB.Blog.BLOG_PUBLISH_ID);
		sBlogProjectionMap.put(TxDB.Blog.PUBLISH_UID, TxDB.Blog.PUBLISH_UID);
		sBlogProjectionMap.put(TxDB.Blog.BLOG_TEXT, TxDB.Blog.BLOG_TEXT);
		sBlogProjectionMap.put(TxDB.Blog.BLOG_TYPE, TxDB.Blog.BLOG_TYPE);
		sBlogProjectionMap.put(TxDB.Blog.IMAGE_LOCAL_PATH,
				TxDB.Blog.IMAGE_LOCAL_PATH);
		sBlogProjectionMap.put(TxDB.Blog.AUDIO_LOCAL_PATH,
				TxDB.Blog.AUDIO_LOCAL_PATH);
		sBlogProjectionMap
				.put(TxDB.Blog.PRAISED_COUNT, TxDB.Blog.PRAISED_COUNT);
		sBlogProjectionMap.put(TxDB.Blog.PRAISED_ID_LIST,
				TxDB.Blog.PRAISED_ID_LIST);
		sBlogProjectionMap.put(TxDB.Blog.IS_DEL_BY_OP, TxDB.Blog.IS_DEL_BY_OP);
		sBlogProjectionMap.put(TxDB.Blog.LIKED_STATE, TxDB.Blog.LIKED_STATE);
		sBlogProjectionMap.put(TxDB.Blog.BLOG_MEDIA_INFOR,
				TxDB.Blog.BLOG_MEDIA_INFOR);
		sBlogProjectionMap.put(TxDB.Blog.TIME, TxDB.Blog.TIME);

	}
}
