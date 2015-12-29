package com.tuixin11sms.tx.db;

import android.R.id;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.utils.Utils;

/** 从数据库，存储与用户不相关的公共数据，例如普通联系人信息 */
public class SubDBHelper extends SQLiteOpenHelper {
	private static final String TAG = "SubDBHelper";

	public static final String DB_NAME = "txdbsub";// 从数据库的名字
	public static final String TXS_TABLE_NAME = "users";// 联系人表名
	private static final int DB_VERSION = 3;//

	public SubDBHelper(Context context) {
		super(context, DB_NAME, null, DB_VERSION);
		// 从数据库的构造方法
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// 创建公共联系人表
		createTXsTable(db);
		createIndices(db);

	}

	
	/** 创建公共联系人表 */
	private void createTXsTable(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TXS_TABLE_NAME + " (" + Tx._ID
				+ " INTEGER," + Tx.TX_ID + " INTEGER PRIMARY KEY,"
				+ Tx.DISPLAY_NAME + " TEXT NOT NULL ON CONFLICT ROLLBACK,"
				+ Tx.TX_SIGN + " TEXT," + Tx.AVATAR_MD5 + " TEXT," + Tx.SEX
				+ " INTEGER," + TxDB.Tx.AVATAR_URL + " TEXT," + Tx.BIRTHDAY
				+ " INTEGER," + Tx.BLOOD_TYPE + " TEXT," + Tx.HOBBY + " TEXT,"
				+ Tx.PROFESSION + " TEXT," + Tx.HOME + " TEXT," + Tx.DISTANCE
				+ " INTEGER," + Tx.AGE + " INTEGER," + Tx.CONSTELLATION
				+ " TEXT," + Tx.PHONE + " TEXT," + Tx.EMAIL + " TEXT,"
				+ Tx.SECOND_CHAR + " TEXT," + Tx.LANGUAGES + " TEXT,"
				+ Tx.ALBUM + " TEXT," + Tx.ISOP + " INTEGER," + Tx.ALBUM_VER
				+ " INTEGER," + Tx.INFO_VER + " INTEGER," + Tx.IS_RECEIVE_REQ
				+ " INTEGER," + Tx.BLOG_INFOR + " TEXT," + Tx.IS_P_BIND
				+ " INTEGER," + Tx.IS_E_BIND + " INTEGER," + Tx.LEVLE
				+ " INTEGER" + ");");

	}

	/**
	 * 创建索引
	 * 
	 * @param db
	 */
	private void createIndices(SQLiteDatabase db) {
		try {
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS tUserPartnerIdIndex ON "
					+ TXS_TABLE_NAME + "(" + TxDB.Tx.TX_ID + ");");

		} catch (Exception ex) {
			if (Utils.debug)
				Log.e(TAG, "普通联系人表创建索引异常", ex);
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		switch (oldVersion) {
		case 1:
			upgradeToVersion2(db);
		case 2:
			upgradeToVersion3(db);
		}
	}

	private void upgradeToVersion2(SQLiteDatabase db) {
		// 给个人信息表增加瞬间的属性字段，json格式（包括：瞬间访问总数，被喜欢总数，瞬间总数字段）
		db.execSQL("ALTER TABLE " + SubDBHelper.TXS_TABLE_NAME + " ADD "
				+ TxDB.Tx.BLOG_INFOR + " TEXT;");

	}

	private void upgradeToVersion3(SQLiteDatabase db) {
		// 给个人信息表增加等级字段
		db.execSQL("ALTER TABLE " + SubDBHelper.TXS_TABLE_NAME + " ADD "
				+ TxDB.Tx.LEVLE + " INTEGER DEFAULT 1;");

	}

}
