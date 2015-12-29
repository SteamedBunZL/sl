package com.tuixin11sms.tx.group;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Qun;
import com.tuixin11sms.tx.db.TxDBContentProvider;
import com.tuixin11sms.tx.db.TxDBContentProvider.DatabaseHelper;
import com.tuixin11sms.tx.utils.Utils;

public class TxGroup implements Parcelable {
	private static final String TAG = TxGroup.class.getSimpleName();
//	private final static Object lock = new Object();
	public static long ACCOST_ID = -9997;// 很早以前（至少3.8.3以前）版本的一个特殊聊天室的id,现在留下这个值是为了兼容老版本数据
											// 2014.01.21 shc

	public int group_type;// 区别 101公共/102搜索 /103我的群
	public long group_id;// 群号
	public int group_ver;// 群版本
	public String group_title;// 显示名称
	public String group_sign;// 个性签名 群有
	public long group_own_id;// 群主号
	public String group_own_name;// 群主姓名
	public long group_time;// 群创建时间
	public String group_tx_ids;// 对群联系人来说 群成员id
	public int group_tx_state=-1;//构造时默认值是-1  2014.07.18 shc
	public String group_tx_admin_ids; // 群管理员IDs
	public String group_tx_admin_names;// 群管理员名字
	public String group_avatar; // 群头像
	public int group_type_channel;// 群类型 0为半公开，1为公开，2为私密，3为官方聊天室
	public String group_bulletin;// 群公告
	public int group_sn;
	public int group_all_num; // 群总人数
	public int group_ol_num;// 在线人数
	public String ban_ids; // 被禁言的人员名单
	public long group_index;// 用于排序
	public long access_time;

	public int rcv_msg;// 是否接收群消息 1接收 0不接受
	public int rcv_push;// 是否接受群提醒 1接收 0不接受

	// 存储所有群的缓存，key是群id，不限长度
	private static HashMap<Long, TxGroup> mTxGroupCache = new HashMap<Long, TxGroup>();

	// 临时字段
	// public String []group_ids;
	// public String []group_name;
	// public String []group_avatars;

	public static boolean isPublicGroup(TxGroup txGroup) {
		if (txGroup.group_type_channel == TxDB.GROUP_TYPE_OFFICIAL
				|| txGroup.group_type_channel == TxDB.GROUP_TYPE_PUBLIC) {
			return true;
		} else {
			return false;
		}
	}
	
	//是否是官方聊天室
	public boolean isOfficialGroup(){
		return group_type_channel == TxDB.GROUP_TYPE_OFFICIAL;
	}
	

	// 存储的是群成员的id,该list一直和group_tx_ids同步变化 2014.01.23
	public ArrayList<Long> group_ids_list = new ArrayList<Long>();

	public TxGroup() {
		group_type = -1;
		group_id = -1;
		group_title = "";
		group_sign = "";
		group_own_id = -1;
		group_own_name = "";
		group_tx_ids = "";
		group_ver = 1;
		group_time = 0;
		group_tx_state = -1;
		group_tx_admin_ids = ""; // 群管理员IDs
		group_tx_admin_names = "";
		group_avatar = ""; // 群头像
		group_type_channel = -1;// 群类型，0为半公开(TxDB.GROUP_TYPE_REQUEST)，1为公开，2为私密
		group_bulletin = "";// 群公告
		ban_ids = "";
		group_sn = -1;
		group_ids_list = new ArrayList<Long>();
		group_index = 0;
		access_time = 0;
		group_all_num = 0;
		group_ol_num = 0;
		rcv_msg = 1;
		rcv_push = 1;
	}

	// 放到调用者方法里面 2014.01.23 shc
	// private static TxGroup findGroupById(ContentResolver cr, long groupId) {
	// TxGroup txGroup = null;
	// if (Utils.isIdValid(groupId)) {
	// try {
	// // Cursor cur = cr.query(TxDB.Qun.CONTENT_URI, null, TxDB.Qun._ID +
	// " = ?",//原来是用_id查询的，现在用群id查询 2014.01.23 shc
	// Cursor cur = cr.query(TxDB.Qun.CONTENT_URI, null, TxDB.Qun.QU_ID +
	// " = ?",
	// new String[] { "" + groupId }, null);
	// if (cur != null) {
	// if (cur.moveToNext()) {
	// txGroup = fetchOneGroup(cur);
	// }
	// cur.close();
	// }
	// } catch (Exception e) {
	// if(Utils.debug)Log.e(TAG,"数据库状态不对，如果刚登陆就退出，数据库关闭了，但是服务还要去读数据库，会出现这个问题",e);
	// }
	// }
	// return txGroup;
	// }

	public static int checkAdminCreator(TxGroup txGroup) {
		if (txGroup.group_own_id == TX.tm.getTxMe().partner_id) {
			return TxDB.QU_TX_STATE_OWN;
		} else {
			String[] ids = txGroup.group_tx_admin_ids.split("�");
			if (ids != null) {
				for (int i = 0; i < ids.length; i++) {
					if (ids[i].equals(TX.tm.getTxMe().partner_id + "")) {
						return TxDB.QU_TX_STATE_GM;
					}
				}
			}
			return TxDB.QU_TX_STATE_CM;
		}
	}

	public static List<String> initAuth(TxGroup txGroup) {
		int meType = -1;
		List<String> auths = new ArrayList<String>();
		if (txGroup != null) {
			meType = checkAdminCreator(txGroup);
			if (meType == TxDB.QU_TX_STATE_OWN) {
				auths.add("设置管理员");
				auths.add("禁言");
				if (!TxGroup.isPublicGroup(txGroup)) {
					auths.add("踢出群");
				}
				auths.add("加入黑名单");
			} else if (meType == TxDB.QU_TX_STATE_GM) {
				auths.add("禁言");
				if (!TxGroup.isPublicGroup(txGroup)) {
					auths.add("踢出群");
				}
				auths.add("加入黑名单");
			}
		}

		int auth = TX.tm.getTxMe().auth;
		boolean isOp = false;
		switch (auth) {
		case TxDB.TX_AUTH_OP:
			if (!auths.contains("禁言")) {
				auths.add("禁言");
			}
			if (!TxGroup.isPublicGroup(txGroup) && !auths.contains("踢出群")) {
				auths.add("踢出群");
			}
			if (!auths.contains("加入黑名单")) {
				auths.add("加入黑名单");
			}
			if (!auths.contains("警告")) {
				auths.add("警告");
			}

			isOp = true;
			break;
		case TxDB.TX_AUTH_CLO_OP:
		case TxDB.TX_AUTH_S_OP:
			if (!auths.contains("禁言")) {
				auths.add("禁言");
			}
			if (!TxGroup.isPublicGroup(txGroup) && !auths.contains("踢出群")) {
				auths.add("踢出群");
			}
			if (!auths.contains("加入黑名单")) {
				auths.add("加入黑名单");
			}
			if (!auths.contains("警告")) {
				auths.add("警告");
			}
			auths.add("封ID");
			auths.add("封设备");
			isOp = true;
			break;
		}

		if (txGroup != null) {
			// boolean isPublic = TxGroup.isPublicGroup(txGroup);
			if (isOp && meType != TxDB.QU_TX_STATE_OWN) {
				// auths.remove("禁言");
				auths.remove("踢出群");
				auths.remove("加入黑名单");

			}
		}
		return auths;
	}

	/**
	 * 聊天室内各种权限
	 * 
	 * @param txGroup
	 * @return list<String>
	 * @author 许春会
	 */
	public static List<String> initMsgRoom(TxGroup txGroup) {
		List<String> auths = new ArrayList<String>();
		if (txGroup != null) {
			int meType = checkAdminCreator(txGroup);
			if (meType == TxDB.QU_TX_STATE_OWN) {

				auths.add("禁言");
				if (!TxGroup.isPublicGroup(txGroup)) {
					auths.add("踢出群");
				}

			} else if (meType == TxDB.QU_TX_STATE_GM) {
				auths.add("禁言");
				if (!TxGroup.isPublicGroup(txGroup)) {
					auths.add("踢出群");
				}

			} else if (meType == TxDB.QU_TX_STATE_CM) {
				auths.add("举报");

			}
		}

		int auth = TX.tm.getTxMe().auth;
		boolean isOp = false;
		switch (auth) {
		case TxDB.TX_AUTH_OP:
			if (!auths.contains("禁言")) {
				auths.add("禁言");
			}
			if (!TxGroup.isPublicGroup(txGroup) && !auths.contains("踢出群")) {
				auths.add("踢出群");
			}

			auths.add("警告");
			isOp = true;
			break;
		case TxDB.TX_AUTH_CLO_OP:
		case TxDB.TX_AUTH_S_OP:
			if (!auths.contains("禁言")) {
				auths.add("禁言");
			}
			if (!TxGroup.isPublicGroup(txGroup) && !auths.contains("踢出群")) {
				auths.add("踢出群");
			}

			auths.add("警告");
			auths.add("封ID");
			auths.add("封设备");
			isOp = true;
			break;
		}

		if (auth == TxDB.TX_AUTH_CLO_OP) {

			auths.remove("禁言");

		}
		// if(txGroup != null){
		// boolean isPublic = TxGroup.isPublicGroup(txGroup);
		// if(!isPublic && isOp){
		// auths.remove("禁言");
		// auths.remove("踢出聊天室");
		//
		// }
		// }

		return auths;

	}

	/**
	 * 获得公共群
	 * 
	 * @param context
	 * @param pageNo
	 *            页码 从0开始
	 * @return
	 */
	public static List<TxGroup> getPublicGroups(Context context, int pageNo) {
		List<TxGroup> groups = new ArrayList<TxGroup>();
		String limit = " limit " + (pageNo * 10) + ",10";
		Cursor cur = context.getContentResolver().query(TxDB.Qun.CONTENT_URI,
				null,
				TxDB.Qun.QU_TYPE + " = ? AND " + TxDB.Qun.QU_INDEX + " > 0",
				new String[] { "" + TxDB.QU_GET_TYPE_PUBLIC },
				TxDB.Qun.QU_INDEX + " ASC " + limit);
		if (cur != null) {
			groups = fetchAllDBGroups(cur);
			cur.close();
		}
		groups = TxGroup.listUniq(groups);
		return groups;
	}

	/**
	 * 获取我的所有群，包括我创建的群，我是管理员的群，我是群成员的群
	 */
	public static List<TxGroup> getMyGroups(Context context) {
		List<TxGroup> groups = new ArrayList<TxGroup>();
		Cursor cur = context.getContentResolver().query(TxDB.Qun.CONTENT_URI,
				null, TxDB.Qun.QU_TX_STATE + " in (0,1,2) ", null,
				Qun.QU_ID + " ASC ");
		if (cur != null) {
			groups = fetchAllDBGroups(cur);
			cur.close();
		}
		groups = TxGroup.listUniq(groups);
		//去掉官方聊天室 求搭讪（11643），隔壁（11640），K歌控（11642）和公共聊天室
		List<TxGroup> delGroup = new ArrayList<TxGroup>();//待删除的官方群
		for(TxGroup txg : groups){
			if(TxGroup.isPublicGroup(txg)){
				delGroup.add(txg);
			}
		}
		if(delGroup.size()>0){
			groups.removeAll(delGroup);
		}
		
		Collections.sort(groups, new GroupTypeComparator());//按群id排序，我创建的群优先
		
		if (Utils.debug){
			Log.i(TAG, "我的群list:"+groups.toString());
		}
		return groups;
	}

	/** 通过访问时间获取群列表 */
	public static List<TxGroup> getGroupsByAccessTime(Context context) {
		List<TxGroup> groups = new ArrayList<TxGroup>();
		String limit = " limit " + 0 + ",20";
		Cursor cur = context.getContentResolver().query(
				TxDB.Qun.CONTENT_URI,
				null,
				TxDB.Qun.QU_ACCESS_TIME + " > 0 AND " + TxDB.Qun.QU_TX_STATE
						+ " <> 3", null, Qun.QU_ACCESS_TIME + " DESC " + limit);
		if (cur != null) {
			groups = fetchAllDBGroups(cur);
			cur.close();
		}
		groups = TxGroup.listUniq(groups);
		return groups;
	}

	private static SQLiteDatabase txDB;

//	// 删除此群缓存，使用mTxGroupCache作为处理的缓存 2014.01.23 shc
//	public static List<TxGroup> mGroups = new ArrayList<TxGroup>();

	/** 应该是获取我的所有群，按照未读数排序 */
	public static List<TxGroup> getMyGroupsByUnreadCount(Context context) {
		List<TxGroup> groups = new ArrayList<TxGroup>();
		DatabaseHelper tempHelper = TxDBContentProvider.getmOpenHelper();// TODO
																			// 这种直接获取openHelper的方式应整理避免
																			// 2014.01.09
																			// shc
		if (tempHelper != null) { 
			txDB = tempHelper.getReadableDatabase();
			Cursor cur = txDB
					.rawQuery(
							"select * from qun q left join msgstat m on m.group_id=q.qu_id where q.qu_tx_state in (0,1,2) order by m.no_read desc",
							null);
			if (cur != null) {
				groups = fetchAllDBGroups(cur);
				cur.close();
			}
			groups = TxGroup.listUniq(groups);
			for(TxGroup txGroup : groups){
				//更新一下缓存中的群信息，//TODO 需要吗？2014.01.23
				updateTxGroupByValues(context.getContentResolver(), txGroup, txgroupToValues(txGroup), false);
			}
//			mGroups = groups;
		}
		return groups;
	}

//	public static List<TxGroup> myGroups = new ArrayList<TxGroup>();// 我的所有群
//																	// 2014.01.23
//																	// shc
//
//	/** 获取我所有的私有群，按照未读数排序 */
//	public static List<TxGroup> getMyGroupsByUnreadCount() {
//		Set<Entry<Long, TxGroup>> groupSet = mTxGroupCache.entrySet();
//		Iterator<Entry<Long, TxGroup>> groupIter = groupSet.iterator();
//		myGroups.clear();
//		TxGroup tempGroup = null;
//		while (groupIter.hasNext()) {
//			Entry<Long, TxGroup> groupEntry = groupIter.next();
//			tempGroup = groupEntry.getValue();
//			if (tempGroup != null
//					&& (groupEntry.getValue().group_tx_state == TxDB.QU_TX_STATE_OWN
//							|| groupEntry.getValue().group_tx_state == TxDB.QU_TX_STATE_GM || groupEntry
//							.getValue().group_tx_state == TxDB.QU_TX_STATE_CM)) {
//				//是我的群
//				myGroups.add(groupEntry.getValue());
//			}
//		}
//		if(myGroups.size()>0){
//			Collections.sort(list, comparator);
//		}
//
//		return null;
//
//	}

	public static TxGroup fetchOneGroup(Cursor c) {
		TxGroup chat = new TxGroup();
		chat.group_type = c.getInt(c.getColumnIndex(TxDB.Qun.QU_TYPE));
		chat.group_id = c.getLong(c.getColumnIndex(TxDB.Qun.QU_ID));
		chat.group_ver = c.getInt(c.getColumnIndex(TxDB.Qun.QU_VER));
		chat.group_title = c.getString(c
				.getColumnIndex(TxDB.Qun.QU_DISPLAY_NAME));
		chat.group_sign = c.getString(c.getColumnIndex(TxDB.Qun.QU_SIGN));
		chat.group_own_id = c.getLong(c.getColumnIndex(TxDB.Qun.QU_OWN_ID));
		chat.group_own_name = c.getString(c
				.getColumnIndex(TxDB.Qun.QU_OWN_NAME));
		chat.group_time = c.getLong(c.getColumnIndex(TxDB.Qun.QU_TIME));
		chat.group_tx_ids = c.getString(c.getColumnIndex(TxDB.Qun.QU_TX_IDS));
		chat.group_tx_state = c.getInt(c.getColumnIndex(TxDB.Qun.QU_TX_STATE));
		chat.group_avatar = c.getString(c.getColumnIndex(TxDB.Qun.QU_AVATAR));
		chat.group_tx_admin_ids = c.getString(c
				.getColumnIndex(TxDB.Qun.QU_TX_ADMIN_IDS));
		chat.group_tx_admin_names = c.getString(c
				.getColumnIndex(TxDB.Qun.QU_TX_ADMIN_NAMES));
		chat.group_bulletin = c.getString(c
				.getColumnIndex(TxDB.Qun.QU_BULLETIN));
		chat.group_sn = c.getInt(c.getColumnIndex(TxDB.Qun.QU_SN));
		chat.group_type_channel = c.getInt(c
				.getColumnIndex(TxDB.Qun.QU_TYPE_CHANNEL));
		chat.rcv_msg = c.getInt(c.getColumnIndex(TxDB.Qun.QU_RCV_MSG));
		chat.rcv_push = c.getInt(c.getColumnIndex(TxDB.Qun.QU_RCV_PUSH));
		chat.group_index = c.getLong(c.getColumnIndex(TxDB.Qun.QU_INDEX));
		chat.access_time = c.getLong(c.getColumnIndex(TxDB.Qun.QU_ACCESS_TIME));
		chat.group_all_num = c.getInt(c.getColumnIndex(TxDB.Qun.ALL_NUM));
		chat.group_ol_num = c.getInt(c.getColumnIndex(TxDB.Qun.OL_NUM));
		initListArray(chat);

		return chat;
	}

	public static TxGroup initListArray(TxGroup txGroup) {
		Set<Long> members = new HashSet<Long>();
		String[] ids = txGroup.group_tx_ids.split("�");
		for (String id : ids) {
			if (!Utils.isNull(id)) {
				members.add(Long.valueOf(id));
			}
		}
		ArrayList<Long> newlist = new ArrayList<Long>();
		newlist.addAll(members);
		txGroup.group_ids_list = newlist;

		return txGroup;
	}

	public static ArrayList<TxGroup> fetchAllDBGroups(Cursor c) {
		ArrayList<TxGroup> ret = new ArrayList<TxGroup>();
		while (c.moveToNext()) {
			ret.add(fetchOneGroup(c));
		}
		return ret;
	}

	// 引用解除，不再使用此方法
	// public static Uri saveTxGroupToDB(final TxGroup txgroup, final TxData
	// txData) {
	// synchronized (lock) {
	// ContentValues values = new ContentValues();
	// Uri aMsgUri = ContentUris.withAppendedId(TxDB.Qun.CONTENT_URI,
	// txgroup.group_id);
	// values.put(TxDB.Qun._ID, txgroup.group_id);
	// values.put(TxDB.Qun.QU_TYPE, txgroup.group_type);
	// values.put(TxDB.Qun.QU_ID, txgroup.group_id);
	// values.put(TxDB.Qun.QU_VER, txgroup.group_ver);
	// values.put(TxDB.Qun.QU_SIGN, txgroup.group_sign);
	// values.put(TxDB.Qun.QU_DISPLAY_NAME, txgroup.group_title);
	// values.put(TxDB.Qun.QU_OWN_ID, txgroup.group_own_id);
	// values.put(TxDB.Qun.QU_OWN_NAME, txgroup.group_own_name);
	// values.put(TxDB.Qun.QU_TIME, txgroup.group_time);
	// values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
	// values.put(TxDB.Qun.QU_TX_STATE, txgroup.group_tx_state);
	// values.put(TxDB.Qun.QU_AVATAR, txgroup.group_avatar);
	// values.put(TxDB.Qun.QU_TX_ADMIN_IDS, txgroup.group_tx_admin_ids);
	// values.put(TxDB.Qun.QU_TX_ADMIN_NAMES, txgroup.group_tx_admin_names);
	// values.put(TxDB.Qun.QU_BULLETIN, txgroup.group_bulletin);
	// values.put(TxDB.Qun.QU_SN, txgroup.group_sn);
	// values.put(TxDB.Qun.QU_TYPE_CHANNEL, txgroup.group_type_channel);
	// values.put(TxDB.Qun.QU_RCV_MSG, txgroup.rcv_msg);
	// values.put(TxDB.Qun.QU_RCV_PUSH, txgroup.rcv_push);
	// values.put(TxDB.Qun.QU_INDEX, txgroup.group_index);
	// values.put(TxDB.Qun.QU_ACCESS_TIME, txgroup.access_time);
	// values.put(TxDB.Qun.ALL_NUM, txgroup.group_all_num);
	// values.put(TxDB.Qun.OL_NUM, txgroup.group_ol_num);
	// if ((txData.getContentResolver().update(aMsgUri, values, null, null)) ==
	// 0) {
	// txData.getContentResolver().insert(TxDB.Qun.CONTENT_URI, values);
	// }
	// MsgStat.updateGroupInfo(txData,txgroup);
	// return aMsgUri;
	// }
	// }

	//判断这个群是否是我的私人群
	public static boolean isMyGroup(long group_id) {
		if(mTxGroupCache.containsKey(group_id)){
			int state = mTxGroupCache.get(group_id).group_tx_state;
			if (state == TxDB.QU_TX_STATE_OWN||state == TxDB.QU_TX_STATE_GM||state == TxDB.QU_TX_STATE_CM) {
				//代表我是群中一员
				return true;
			}
		}

		return false;

	}
	
	
	/**
	 * 
	 * @return 是否添加成功
	 */
	public static boolean addTxGroup(ContentResolver cr, TxGroup txgroup) {
		if (!Utils.isIdValid(txgroup.group_id)) {
			// 群id不合法
			return false;
		}
		TxGroup tx0 = mTxGroupCache.get(txgroup.group_id);
		if (tx0 == null) {
			// 添加到群缓存中
			mTxGroupCache.put(txgroup.group_id, txgroup);
			// 保存到数据库
			ContentValues values = txgroupToValues(txgroup);
			// 如果数据库中存在该群，先执行更新操作，失败后再插入 //这个会有这种情况吗？2014.01.23 shc
			if (updateTxGroupByGroupId(cr, values,txgroup.group_id) == 0) {
				cr.insert(TxDB.Qun.CONTENT_URI, values);
			}
		}
		return true;

	}

	// 查询群
	public static TxGroup getTxGroup(ContentResolver cr, long group_id) {
		if (!Utils.isIdValid(group_id)) {
			return null;
		}
		TxGroup group = mTxGroupCache.get(group_id);
		
		if (group == null) {
			// 缓存中没有查到，去数据库中查询
			try {
				// Cursor cur = cr.query(TxDB.Qun.CONTENT_URI, null,
				// TxDB.Qun._ID + " = ?",//原来是用_id查询的，现在用群id查询 2014.01.23 shc
				Cursor cur = cr.query(TxDB.Qun.CONTENT_URI, null,
						TxDB.Qun.QU_ID + " = ?",
						new String[] { "" + group_id }, null);
				if (cur != null) {
					if (cur.moveToNext()) {
						group = fetchOneGroup(cur);
					}
					cur.close();
				}
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "数据库状态不对，如果刚登陆就退出，数据库关闭了，但是服务还要去读数据库，会出现这个问题", e);
			}

			if (group != null) {
				// 在数据库中查询到了该群，添加到缓存中
				mTxGroupCache.put(group.group_id, group);
			}
		}
		return group;
	}

	// 把TxGoup变成Values
	private static ContentValues txgroupToValues(TxGroup txgroup) {
		ContentValues values = new ContentValues();
		values.put(TxDB.Qun._ID, txgroup.group_id);
		values.put(TxDB.Qun.QU_TYPE, txgroup.group_type);
		values.put(TxDB.Qun.QU_ID, txgroup.group_id);
		values.put(TxDB.Qun.QU_VER, txgroup.group_ver);
		values.put(TxDB.Qun.QU_SIGN, txgroup.group_sign);
		values.put(TxDB.Qun.QU_DISPLAY_NAME, txgroup.group_title);
		values.put(TxDB.Qun.QU_OWN_ID, txgroup.group_own_id);
		values.put(TxDB.Qun.QU_OWN_NAME, txgroup.group_own_name);
		values.put(TxDB.Qun.QU_TIME, txgroup.group_time);
		values.put(TxDB.Qun.QU_TX_IDS, txgroup.group_tx_ids);
		values.put(TxDB.Qun.QU_TX_STATE, txgroup.group_tx_state);
		values.put(TxDB.Qun.QU_AVATAR, txgroup.group_avatar);
		values.put(TxDB.Qun.QU_TX_ADMIN_IDS, txgroup.group_tx_admin_ids);
		values.put(TxDB.Qun.QU_TX_ADMIN_NAMES, txgroup.group_tx_admin_names);
		values.put(TxDB.Qun.QU_BULLETIN, txgroup.group_bulletin);
		values.put(TxDB.Qun.QU_SN, txgroup.group_sn);
		values.put(TxDB.Qun.QU_TYPE_CHANNEL, txgroup.group_type_channel);
		values.put(TxDB.Qun.QU_RCV_MSG, txgroup.rcv_msg);
		values.put(TxDB.Qun.QU_RCV_PUSH, txgroup.rcv_push);
		values.put(TxDB.Qun.QU_INDEX, txgroup.group_index);
		values.put(TxDB.Qun.QU_ACCESS_TIME, txgroup.access_time);
		values.put(TxDB.Qun.ALL_NUM, txgroup.group_all_num);
		values.put(TxDB.Qun.OL_NUM, txgroup.group_ol_num);
		return values;
	}

	// 更新TxGroup
	public static TxGroup updateTxGroup(ContentResolver cr, long group_id,
			ContentValues values) {
		TxGroup group = mTxGroupCache.get(group_id);
		if (group != null) {
			return updateTxGroupByValues(cr, group,
					values);
		} else {
			group = getTxGroup(cr, group_id);
			if (group != null) {
				mTxGroupCache.put(group.group_id, group);
				return updateTxGroupByValues(cr,
						group, values);
			}
		}

		return null;
	}

	// 更新群的字段,并且更新数据库
	private static TxGroup updateTxGroupByValues(ContentResolver ctr,
			TxGroup txGroup, ContentValues values) {
		return updateTxGroupByValues(ctr, txGroup, values, true);
		
	}
	// 更新群的字段
	private static TxGroup updateTxGroupByValues(ContentResolver ctr,
			TxGroup txGroup, ContentValues values,boolean isUpdateDB) {
		if (txGroup != null) {
			Set<Entry<String, Object>> valueSet = values.valueSet();
			Iterator<Entry<String, Object>> txIterator = valueSet.iterator();
			while (txIterator.hasNext()) {
				Entry<String, Object> tbEntry = txIterator.next();
				String key = tbEntry.getKey();
				Object value = tbEntry.getValue();
				if (key.equals(TxDB.Qun._ID)) {
					// 群的_ID
					txGroup.group_id = (Long) value;
				} else if (key.equals(TxDB.Qun.QU_TYPE)) {
					// 群类型
					txGroup.group_type = (Integer) value;
				} else if (key.equals(TxDB.Qun.QU_ID)) {
					// 群id
					txGroup.group_id = (Long) value;
				} else if (key.equals(TxDB.Qun.QU_VER)) {
					// 群版本
					txGroup.group_ver = (Integer) value;
				} else if (key.equals(TxDB.Qun.QU_SIGN)) {
					// 群签名
					txGroup.group_sign = (String) value;
				} else if (key.equals(TxDB.Qun.QU_DISPLAY_NAME)) {
					txGroup.group_title = (String) value;

				} else if (key.equals(TxDB.Qun.QU_OWN_ID)) {
					txGroup.group_own_id = (Long) value;

				} else if (key.equals(TxDB.Qun.QU_OWN_NAME)) {
					txGroup.group_own_name = (String) value;

				} else if (key.equals(TxDB.Qun.QU_TIME)) {
					txGroup.group_time = (Long) value;
				} else if (key.equals(TxDB.Qun.QU_TX_IDS)) {
					txGroup.group_tx_ids = (String) value;

				} else if (key.equals(TxDB.Qun.QU_TX_STATE)) {
					txGroup.group_tx_state = (Integer) value;

				} else if (key.equals(TxDB.Qun.QU_AVATAR)) {
					txGroup.group_avatar = (String) value;

				} else if (key.equals(TxDB.Qun.QU_TX_ADMIN_IDS)) {
					txGroup.group_tx_admin_ids = (String) value;

				} else if (key.equals(TxDB.Qun.QU_TX_ADMIN_NAMES)) {
					txGroup.group_tx_admin_names = (String) value;

				} else if (key.equals(TxDB.Qun.QU_BULLETIN)) {
					txGroup.group_bulletin = (String) value;

				} else if (key.equals(TxDB.Qun.QU_SN)) {
					txGroup.group_sn = (Integer) value;

				} else if (key.equals(TxDB.Qun.QU_TYPE_CHANNEL)) {
					txGroup.group_type_channel = (Integer) value;

				} else if (key.equals(TxDB.Qun.QU_RCV_MSG)) {
					txGroup.rcv_msg = (Integer) value;

				} else if (key.equals(TxDB.Qun.QU_RCV_PUSH)) {
					txGroup.rcv_push = (Integer) value;

				} else if (key.equals(TxDB.Qun.QU_INDEX)) {
					txGroup.group_index = (Long) value;

				} else if (key.equals(TxDB.Qun.QU_ACCESS_TIME)) {
					txGroup.access_time = (Long) value;

				} else if (key.equals(TxDB.Qun.ALL_NUM)) {
					txGroup.group_all_num = (Integer) value;

				} else if (key.equals(TxDB.Qun.OL_NUM)) {
					txGroup.group_ol_num = (Integer) value;
				}
			}
			if (isUpdateDB) {
				// 缓存更新完毕，开始更新数据库
				updateTxGroupByGroupId(ctr, values, txGroup.group_id);
			}
		}
		return txGroup;

	}

	/** 根据条件更新数据库TxGroup */
	private static int updateTxGroupByGroupId(ContentResolver cr,
			ContentValues values, long groupId) {
		Uri aMsgUri = ContentUris.withAppendedId(TxDB.Qun.CONTENT_URI, groupId);
		int rows = cr.update(aMsgUri, values, null, null);
		return rows;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(group_id).append(":").append(group_type).append(":")
				.append(group_sign).append(":").append(group_title).append(":")
				.append(group_own_id).append(":").append(group_own_name)
				.append(":").append(group_tx_ids).append(":").append(group_avatar);
		return sb.toString();
	}

	public static final Parcelable.Creator<TxGroup> CREATOR = new Parcelable.Creator<TxGroup>() {
		public TxGroup createFromParcel(Parcel in) {
			return new TxGroup(in);
		}

		public TxGroup[] newArray(int size) {
			return new TxGroup[size];
		}
	};

	private TxGroup(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		//
		return 0;
	}

	// 过滤相同的群
	public static List<TxGroup> listUniq(List<TxGroup> list) {
		Set<TxGroup> set = new LinkedHashSet<TxGroup>();
		set.addAll(list);
		List<TxGroup> newlist = new ArrayList<TxGroup>();
		newlist.addAll(set);
		return newlist;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (group_id ^ (group_id >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TxGroup other = (TxGroup) obj;
		if (group_id != other.group_id)
			return false;
		return true;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(group_type);
		out.writeLong(group_id);
		out.writeInt(group_ver);
		out.writeString(group_title);
		out.writeString(group_sign);
		out.writeLong(group_own_id);
		out.writeString(group_own_name);
		out.writeString(group_tx_ids);
		out.writeInt(group_tx_state);
		out.writeLong(group_time);
		out.writeString(group_avatar);
		out.writeString(group_tx_admin_ids);
		out.writeString(group_tx_admin_names);
		out.writeString(group_bulletin);
		out.writeInt(group_type_channel);
		out.writeInt(group_all_num);
		out.writeInt(group_ol_num);
		out.writeString(ban_ids);
		out.writeInt(rcv_msg);
		out.writeInt(rcv_push);
		out.writeLong(group_index);
		out.writeLong(access_time);
		int nl = 0;
		if (group_ids_list != null && group_ids_list.size() > 0) {
			nl = group_ids_list.size();
			out.writeInt(nl);
			for (int i = 0; i < nl; i++) {
				out.writeLong(group_ids_list.get(i));
			}
		} else {
			out.writeInt(nl);
		}
	}

	/**
	 * 添加或删除群成员
	 * 
	 * @param isAdd
	 *            true 添加群成员 false 删除成员
	 * @return
	 */
	public TxGroup changeMembers(TxGroup txGroup, Long uid, boolean isAdd) {

		Set<Long> members = new HashSet<Long>();
		String[] ids = txGroup.group_tx_ids.split("�");
		for (String id : ids) {
			if (!Utils.isNull(id)) {
				members.add(Long.valueOf(id));
			}
		}

		// 加入群
		if (isAdd) {
			StringBuffer idsStrt = new StringBuffer();
			for (Long id : members) {
				idsStrt.append(id);
				idsStrt.append("�");
			}

			idsStrt.append(uid);
			idsStrt.append("�");
			txGroup.group_tx_ids = idsStrt.toString();

			members.add(uid);
			ArrayList<Long> newlist = new ArrayList<Long>();
			newlist.addAll(members);
			txGroup.group_ids_list = newlist;

		} else {
			members.remove(uid);
			ArrayList<Long> newlist = new ArrayList<Long>();
			newlist.addAll(members);
			txGroup.group_ids_list = newlist;

			StringBuffer idss = new StringBuffer();
			for (Long id : txGroup.group_ids_list) {
				if (id > 0) {
					idss.append(id);
					idss.append("�");
				}
			}

			txGroup.group_tx_ids = idss.toString();
		}
		Set<Long> adminids = new HashSet<Long>();
		String[] adids = txGroup.group_tx_admin_ids.split("�");
		for (String id : adids) {
			if (!Utils.isNull(id)) {
				adminids.add(Long.valueOf(id));
			}
		}
		txGroup.group_all_num = txGroup.group_ids_list.size() + 1
				+ adminids.size();

		return txGroup;
	}

	public TxGroup changeAdmin(TxGroup txGroup, String uid, boolean isAdd) {
		// 现在的管理员集合
		HashSet<String> adminSet = new HashSet<String>();
		String[] ids = txGroup.group_tx_admin_ids.split("�");
		for (String id : ids) {
			if (!Utils.isNull(id)) {
				adminSet.add(id);
			}
		}
		// 加入群
		if (isAdd) {
			adminSet.add(uid);
		} else {
			adminSet.remove(uid);
		}
		StringBuffer idsb = new StringBuffer();
		for (String sb : adminSet) {
			idsb.append(sb);
			idsb.append("�");
		}
		txGroup.group_tx_admin_ids = idsb.toString();
		return txGroup;
	}

	public void readFromParcel(Parcel in) {
		group_type = in.readInt();
		group_id = in.readLong();
		group_ver = in.readInt();
		group_title = in.readString();
		group_sign = in.readString();
		group_own_id = in.readLong();
		group_own_name = in.readString();
		group_tx_ids = in.readString();
		group_tx_state = in.readInt();
		group_time = in.readLong();
		group_avatar = in.readString();
		group_tx_admin_ids = in.readString();
		group_tx_admin_names = in.readString();
		group_bulletin = in.readString();
		group_type_channel = in.readInt();
		group_all_num = in.readInt();
		group_ol_num = in.readInt();
		ban_ids = in.readString();
		rcv_msg = in.readInt();
		rcv_push = in.readInt();
		group_index = in.readLong();
		access_time = in.readLong();
		int nl = in.readInt();
		if (nl > 0) {
			group_ids_list = new ArrayList<Long>();
			for (int i = 0; i < nl; i++) {
				group_ids_list.add(in.readLong());
			}
		}
	}

	/**退出登录后，清空群的缓存，避免群信息串*/
	public static void clearGroupCache() {
		mTxGroupCache.clear();
		
	}

}
