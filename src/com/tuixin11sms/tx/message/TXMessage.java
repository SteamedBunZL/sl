package com.tuixin11sms.tx.message;

import java.io.File;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.ContactsContract.CommonDataKinds.Email;
import android.text.TextUtils;
import android.util.Log;

import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.data.TxDB.Messages;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.utils.Utils;

public class TXMessage implements Parcelable {
	private static final String TAG = TXMessage.class.getSimpleName();
	private final static Object lock = new Object();
	public static final int SELECT_COUNT = 20;
	public static final String IMG_TYPE = "img:";
	public static final String MOBILE_TYPE = "moblle:";
	public int msg_type;// 消息类型
	public long msg_id;// 消息id
	public long msg_id2;// 消息id 用于处理被举报信息ID
	/****************** 通讯录数据 **********************/
	public int contacts_person_id;// 通讯录索引id -2代表多人群发
	public String contacts_person_name;// 通讯录名称
	/****************** 神聊数据 **********************/
	public long partner_id;// sender_partner_id
	public String partner_name;// TODO 这个字段不知道干嘛用的？和nickName有什么区别？ 2014.06.23
								// shc
	// public String partner_phone;//消息的phone应该也没有用,删除 2014.01.22 shc
	/** 群组Id（群组消息使用） */
	public long group_id;
	public String group_name;
	public String group_avatars_url;// 这个是消息中群头像url 2014.01.21 shc
	public String msg_subject;// 主题 神聊消息没有，彩信代表附件类型
	public String msg_body;// 消息正文
	public String msg_path;// 消息附件在本地路径
	public String msg_url;// 消息附件在服务器路径
	public long msg_file_length;// 消息附件大小
	public long audio_times;// 音频时长
	public String audio_end;// 音频后缀
	public String geo;// 地理位置

	public String tcard_name;// 名片姓名
	public long tcard_id;// 名片神聊id
	public String tcard_sign;// 名片签名
	public int tcard_sex;// 名片电话
	public String tcard_phone;// 名片性别
	public String tcard_avatar_url;// 名片头像地址
	public boolean agree;// 加好友结果
	public String ac;
	/********** 下面四个字段除了跟数据库相关，没什么用 2013.12.03 shc *************/
	// public int sns_type;//sns 类型
	public String sns_id;// sns 联系人id ----设置为信息地址 bobo
	public String sns_name;// sns 联系人id

	public boolean was_me;// 本人发送与否
	public static final int NOT_SENT = 0;// 发送中
	public static final int SENT = 1; // (系统短信彩信发送成功) // tui 已发 短信 已发
	public static final int UNREAD = 2;// (系统短信彩信发送失败) // tui 未读 短信失败
	public static final int READ = 3; // tui 已读 短信 送达
	public static final int FAIL = 4; // 发送失败
	public static final int FORBID = 5; // 禁言
	public int read_state; // 消息阅读状态 1 2 未读 3已读
	public long send_time;// 发送时间
	public static String default_sms_id = "9999990";
	public static String user_id = "9999994";

	/*********** 举报消息附带的消息信息字段 *******************/
	public long msg_type2;// 举报消息里的消息类型
	public long reportId;
	public String reportName;
	public String reportContext;
	public long shutup_st; // 禁言开始时间
	public long shutup_du; // 禁言时长

	public long gmid;// 服务器消息ID
	public long group_id_notice;
	public String sn;
	public String rs;
	public int op;// 应该是是否操作的字段，例如同意了请求，拒绝了请求等 2014.03.11 shc
	public int opId;
	public String opName;
	// 临时数据
	int mLoadings = 0;
	public static SessionManager mSess = SessionManager.getInstance();
	// 新增gif字段 2015/1/22
	public int pkgid;
	public String emomd5;
	public int num;
	public String pkg_emomd5;

	public synchronized boolean setLoadingImg() {
		boolean bRet = (mLoadings & 1) != 0;
		mLoadings |= 1;
		return bRet;
	}

	public synchronized void clearLoadingImg() {
		mLoadings &= ~1;
	}

	public int updateCount;// 上传下载的总进度
	public int updateState;// 消息附件的上传下载状态
	public final static int UPDATE = 0;// 这个是的待上传下载状态 2013.12.05
	public final static int UPDATING = 1;// 正在下载？
	public final static int UPDATE_OK = 2;// 下载完成？
	public final static int UPDATE_FAILE = 3;// 下载失败？
	public final static int UPDATE_CLICK = 4;// 点击下载状态
	public final static int LOAD_FAILED = 6;// 图片加载失败（本地下载完成的文件可能被删除） 2014.06.23
											// shc
	public int PlayAudio;// TODO 抽时间整理删除该字段的引用，再删除该字段。 2013.09.23
	public SoftReference<Bitmap> cacheImg;// 图片缓存
	public SoftReference<AnimationDrawable> cacheGif;// gif动画缓存
	public String avatar_url;// 头像地址
	public String nick_name;// 昵称
	public int sex = TX.DEFAULT_SEX; // 性别(0:男，1：女)
	public CharSequence cache_charSequence;

	public int praisedState = PraiseNotice.ACTION_NONE;// 赞状态
	public String fileDownTime = "";// 附件第一次下载成功的时间

	public void getInfoFromTX(TX tx, boolean bSingle) {
		if (tx != null) {
			avatar_url = tx.avatar_url;
			if (bSingle) {
				sex = tx.getSex();
			}
			if (!bSingle)
				group_avatars_url = tx.avatar_url;
		}
	}

	public TXMessage() {
		this.msg_type = TxDB.MSG_TYPE_COMMON_SMS;
		this.msg_id = Utils.DEFAULT_NUMBER;
		this.msg_id2 = Utils.DEFAULT_NUMBER;
		this.contacts_person_id = Utils.DEFAULT_NUMBER;

		this.contacts_person_name = "";
		// this.sms_address = "";
		this.partner_id = Utils.DEFAULT_NUMBER;
		this.partner_name = "";
		// this.partner_phone = "";
		this.group_id = Utils.DEFAULT_NUMBER;
		this.group_name = "";
		this.group_avatars_url = "";
		this.msg_subject = "";
		this.msg_body = "";
		this.msg_path = "";
		this.msg_url = "";
		this.msg_file_length = 0;
		this.audio_times = 0;
		this.audio_end = "";
		this.geo = "";
		this.tcard_name = "";// 名片姓名
		this.tcard_id = -1;// 名片神聊id
		this.tcard_sign = "";// 名片签名
		this.tcard_sex = TX.DEFAULT_SEX;// 名片电话
		this.tcard_avatar_url = "";
		this.tcard_phone = "";
		this.agree = false;// 加好友
		this.ac = "";
		// this.sns_type = -1;//sns 类型
		this.sns_id = "";// sns 联系人id
		this.sns_name = "";
		this.was_me = false;
		this.read_state = -1;
		this.praisedState = PraiseNotice.ACTION_NONE;
		this.fileDownTime = "";
		this.updateState = UPDATE;
		this.send_time = 0;
		this.msg_type2 = 0;
		this.reportId = 0;
		this.reportContext = "";
		this.reportName = "";
		this.gmid = 0;
		this.group_id_notice = -1;
		this.sn = "";
		this.rs = "";
		this.op = -1;
		this.opId = -1;
		this.opName = "";
		this.sex = TX.DEFAULT_SEX;// 性别原来是-1，现在给的默认值是女
	}

	public TXMessage(String aduPath, String aduUrl, long aduTime) {
		this.msg_path = aduPath;
		this.msg_url = aduUrl;
		this.audio_times = aduTime;
		// this.PlayAudio = PlayAudio;
	}

	// 通过下载完成时间判断是否可以被赞
	public boolean isCanBePraise() {
		if (!TextUtils.isEmpty(fileDownTime)) {
			try {
				long timeDelta = System.currentTimeMillis()
						- Long.parseLong(fileDownTime);// 时间差
				if (Utils.debug)
					Log.i(TAG, "消息时间差是=" + timeDelta / 1000 + "秒");
				if (timeDelta < (10 * 60 * 1000)) {
					// 下载完成时间小于10分钟，则可以点赞
					return true;
				}
			} catch (NumberFormatException e) {
				if (Utils.debug)
					Log.e(TAG, "Long转换异常", e);
			}
		} else {
			// 下载时间为空，代表没有下载过，可以被点赞
			return true;
		}
		return false;
	}

	public static TXMessage creatDraft(Context ctxt, long partner_id,
			long group_id, String partner_display_name, String partner_phone,
			String msg_body, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_DRAFT;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		return txmsg;
	}

	/**
	 * 创建普通消息
	 * 
	 * @return
	 */
	public static TXMessage creatCommonSms(Context ctxt, long partner_id,
			String partner_name, String partner_phone, String msg_body,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_COMMON_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_body = msg_body;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/** 创建单聊大文件消息 */
	public static TXMessage creatBigFileSms(long partner_id,
			String partner_name, String partner_phone, String msg_file_path,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_BIG_FILE_SMS;
		txmsg.msg_id = Utils.createMsgId(TX.tm.getUserID() + "");
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_body = "对方给您发送了一个文件，马上升级最新客户端查看吧!下载地址：http://shenliao.com";
		txmsg.msg_path = msg_file_path;
		txmsg.msg_file_length = new File(msg_file_path).length();
		txmsg.msg_url = "";// 大文件的url创建信息的时候应该还没有
		txmsg.was_me = true;
		txmsg.read_state = TXMessage.NOT_SENT;
		txmsg.updateState = TXMessage.UPDATING;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/** 创建神聊gif消息 */
	public static TXMessage creatGifSms(long partner_id, String partner_name,
			String partner_phone, long send_time, int num, int pkg_id,
			String md5,String pkg_emomd5) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_SMS_GIF;
		txmsg.msg_id = Utils.createMsgId(TX.tm.getUserID() + "");
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.msg_body = "对方给您发送了一个表情";
		txmsg.was_me = true;
		txmsg.read_state = TXMessage.NOT_SENT;
		// txmsg.updateState = TXMessage.UPDATING;
		txmsg.send_time = send_time;
		txmsg.pkgid = pkg_id;
		txmsg.emomd5 = md5;
		txmsg.pkg_emomd5 = pkg_emomd5;
		txmsg.num = num;
		return txmsg;

	}

	// TODO 创建gif消息
	public static TXMessage createGroupGifSms(Context ctxt, long group_id,
			String group_name, String group_avatars_url, int pkg_id,
			String emomd5, boolean was_me, int read_state, long send_time,
			int num, String pkg_emomd5) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_GIF_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.pkgid = pkg_id;
		txmsg.emomd5 = emomd5;
		txmsg.pkg_emomd5 = pkg_emomd5;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		txmsg.num = num;
		return txmsg;
	}

	/** 创建收到的GIF消息 */
	public static TXMessage creatGifSmsWhenReceive(Context ctxt, long msg_id,
			long partner_id, String partner_name, long send_time, int pkg_id,
			String emomd5) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_SMS_GIF;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.was_me = false;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		txmsg.updateState = TXMessage.UPDATE;// 待下载状态
		txmsg.pkgid = pkg_id;
		txmsg.emomd5 = emomd5;
		return txmsg;
	}

	/** 创建收到的单聊大文件消息 */
	public static TXMessage creatBigFileSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, String file_url,
			int file_length, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_BIG_FILE_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.was_me = false;
		txmsg.msg_url = file_url;
		txmsg.msg_path = SessionManager.getInstance().mDownUpMgr
				.getDownLoadBigFilePath(file_url);
		;
		txmsg.msg_file_length = file_length;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		txmsg.updateState = TXMessage.UPDATE;// 待下载状态
		return txmsg;
	}

	public static TXMessage createSetGroupAdmin(Context ctxt, String msg_body,
			int gid, String groupName, String groupurl, int op, int opId,
			String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_SET_GROUP_ADMIN;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = "群组动态";
		txmsg.msg_body = msg_body;
		txmsg.read_state = UNREAD;
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupurl;
		txmsg.op = op;
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 申请加入群 给管理员审核用的
	 * 
	 * @param ctxt
	 * @param partner_id
	 * @param partner_display_name
	 * @param msg_body
	 * @param gid
	 * @param sn
	 * @param ac
	 * @param rs
	 * @param send_time
	 * @return
	 */
	public static TXMessage createRequestJoinGroup4Admin(Context ctxt,
			long userid, String name, int gid, String groupName,
			String groupurl, String sn, String ac, String rs, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN;
		txmsg.tcard_id = userid;
		txmsg.tcard_name = name;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = name;
		txmsg.msg_body = "申请加入聊天室【�slgroup�】";
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupurl;
		txmsg.read_state = UNREAD;
		txmsg.ac = ac;
		txmsg.sn = sn;
		txmsg.rs = rs;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 申请加入群结果 给申请人看的
	 * 
	 * @param ctxt
	 * @param partner_id
	 * @param partner_display_name
	 * @param msg_body
	 * @param gid
	 * @param sn
	 * @param ac
	 * @param rs
	 * @param send_time
	 * @return
	 */
	public static TXMessage createRequestJoinGroup4Member(Context ctxt,
			String msg_body, int gid, String groupName, String groupurl,
			boolean agree, int opId, String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = "";
		txmsg.msg_body = msg_body;
		txmsg.op = agree ? 1 : 0;
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupurl;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 群主解散群
	 * 
	 * @param ctxt
	 * @param msg_body
	 * @param gid
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createDismissGroup(Context ctxt, int gid,
			String groupName, String groupUrl, int opId, String opName,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_DISMISS_GROUP;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = "";
		txmsg.msg_body = "群主" + "(" + opId + ")" + "解散了聊天室【�slgroup�】";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupUrl;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage createLeaveGroup(Context ctxt, int gid,
			String groupName, String groupUrl, int opId, String opName,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_LEAVE;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = "群组动态";
		txmsg.msg_body = "您被移出聊天室【�slgroup�】。";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupUrl;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage createInGroup(Context ctxt, int gid,
			String groupName, String groupUrl, int opId, String opName,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_IN;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.partner_name = "群组动态";
		txmsg.msg_body = "你加入聊天室【�slgroup�】。";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.group_id_notice = gid;
		txmsg.group_name = groupName;
		txmsg.group_avatars_url = groupUrl;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/***
	 * 
	 * @param ctxt
	 * @param gid
	 * @param st
	 *            开始时间 单位秒
	 * @param du
	 *            结束时间
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createShutup(Context ctxt, long gid, long st,
			long du, int opId, String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.group_id_notice = gid;
		txmsg.partner_name = "神聊小卫士";

		String temp = "";
		if (du == 5 * 60) {
			temp = "禁言5分钟。";
		} else if (du == 30 * 60) {
			temp = "禁言30分钟。";
		} else if (du == 24 * 60 * 60) {
			temp = "禁言24小时。";
		} else if (du == 0) {
			temp = "永久禁言。";
		}
		// String time = "禁言时间为"+GroupUtils.dealshutupDate(st*1000,
		// ctxt)+"至"+GroupUtils.dealshutupDate((st+du) * 1000, ctxt);
		String time = "禁言时间为" + GroupUtils.formatTime(st * 1000) + "至"
				+ GroupUtils.formatTime((st + du) * 1000);

		// String e = "如对处罚不满请发邮件申诉 help@shenliao.com";
		// 您已被管理员xxx禁言xxx分钟.禁言将在9月30日14:23分结束.如对处罚不满请发邮件申诉 help@shenliao.com
		// 禁言时间为yyyy-MM-dd hh:mm至yyyy-MM-dd hh:mm

		String body = "您已被该聊天室管理员" + opName + "(" + opId + ")" + temp + time;
		txmsg.msg_body = body;
		txmsg.shutup_st = st;
		txmsg.shutup_du = du;
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 自己的群被官方解散
	 * 
	 * @param ctxt
	 * @param gid
	 * @param topic
	 * @param rs
	 * @param send_time
	 * @return
	 */
	public static TXMessage createGuanDismiss(Context ctxt, long gid,
			String topic, String rs, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_DISMISS_4_CREATOR;
		txmsg.partner_id = TX.SL_GROUP_NOTICE;
		txmsg.group_id_notice = gid;
		txmsg.group_name = topic;
		txmsg.partner_name = "群组动态";
		txmsg.msg_body = "您创建的聊天室【" + topic + "】因" + rs + "已被官方移除";
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 禁言解除
	 * 
	 * @param ctxt
	 * @param gid
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createShutupOver(Context ctxt, long gid, int opId,
			String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SHUTUP_4_MEMBER_OVER;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.group_id_notice = gid;
		txmsg.partner_name = "神聊小卫士";
		txmsg.msg_body = "您已被管理员" + opName + "(" + opId + ")" + "解除禁言";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 给管理员看的 表示他警告了某人
	 * 
	 * @param ctxt
	 * @param gid
	 * @param uid
	 * @param name
	 * @param send_time
	 * @return
	 */
	public static TXMessage createWarn4Admin(Context ctxt, long uid,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_WARN_4_ADMIN;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.tcard_id = uid;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 给管理员看的 表示他已禁言了某人
	 * 
	 * @param ctxt
	 * @param gid
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createShutup4Admin(Context ctxt, long uid,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SHUTUP_4_ADMIN;
		txmsg.msg_body = "你禁言了【�sluser�】";
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.tcard_id = uid;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 表示他已经解除了某人的禁言
	 * 
	 * @param ctxt
	 * @param gid
	 * @param uid
	 * @param name
	 * @param send_time
	 * @return
	 */
	public static TXMessage createShutup4Admin_clear(Context ctxt, long uid,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SHUTUP_4_ADMIN_CLEAR;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.msg_body = "您解除了【�sluser�】的禁言";
		txmsg.partner_name = "神聊小卫士";
		txmsg.tcard_id = uid;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 给管理员看的 表示他已经把某人的id封掉了
	 * 
	 * @param ctxt
	 * @param gid
	 * @param uid
	 * @param name
	 * @param send_time
	 * @return
	 */
	public static TXMessage createSealId4Admin(Context ctxt, long uid,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_ADMIN;
		txmsg.msg_body = "您封杀了【�sluser�】的ID";
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.tcard_id = uid;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 给管理员看的 表示他已经把某人的设备封掉了
	 * 
	 * @param ctxt
	 * @param gid
	 * @param uid
	 * @param name
	 * @param send_time
	 * @return
	 */
	public static TXMessage createSealMoible4Admin(Context ctxt, long uid,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_ADMIN;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.tcard_id = uid;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 封号
	 * 
	 * @param ctxt
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createSealID(Context ctxt, int opId, String opName,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SEAL_ID_4_MEMBER;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.msg_body = "您已被封号。";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 封设备
	 * 
	 * @param ctxt
	 * @param opId
	 * @param opName
	 * @param send_time
	 * @return
	 */
	public static TXMessage createSealMobile(Context ctxt, int opId,
			String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_SEAL_MOBILE_4_MEMBER;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.msg_body = "您已被封设备。";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage createWarn(Context ctxt, int opId, String opName,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_WARN;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.msg_body = "警告：为了维护聊天室的正常秩序，为网友们创造一个健康、文明、温馨、清静的聊天环境，请您进入聊天室的聊友自觉遵守聊天室纪律。";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage createBlogDeleteNotice(Context ctxt, int opId,
			String opName, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.msg_type = TxDB.MSG_TYPE_MANAGER_NOTICE_BLOG_DELETE;
		txmsg.partner_id = TX.SL_SAFE;
		txmsg.partner_name = "神聊小卫士";
		txmsg.opId = opId;
		txmsg.opName = opName;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 收到普通单聊消息
	 */
	public static TXMessage creatCommonSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, String msg_body,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_COMMON_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_body = msg_body;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupCommonSms(Context ctxt, long group_id,
			String group_name, String group_avatars_url, String msg_body,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_COMMON_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_body = msg_body;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		txmsg.partner_id = TX.tm.getTxMe().partner_id;// 为什么群聊信息中没有partner_id，先加上
														// 2013.10.12 shc
		return txmsg;
	}

	/** 创建群聊大文件消息 */
	public static TXMessage creatGroupBigFileSms(long group_id,
			String group_name, String msg_file_path, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_BIG_FILE_SMS;
		txmsg.msg_id = Utils.createMsgId(TX.tm.getUserID() + "");
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = "";// 在这个消息中，群的url应该不用设置吧2014.01.21
		txmsg.msg_body = "对方给您发送了一个文件，马上升级最新客户端查看吧!下载地址：http://shenliao.com";
		txmsg.msg_path = msg_file_path;
		txmsg.msg_file_length = new File(msg_file_path).length();
		txmsg.msg_url = "";// url创建信息的时候应该还没有
		txmsg.was_me = true;
		txmsg.read_state = TXMessage.NOT_SENT;
		txmsg.updateState = TXMessage.UPDATING;
		txmsg.send_time = send_time;
		txmsg.partner_id = TX.tm.getUserID();// 为什么群聊信息中没有partner_id，先加上
												// 2013.10.12 shc
		return txmsg;
	}

	/** 创建收到群聊的大文件消息 */
	public static TXMessage creatGroupBigFileSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String file_url,
			int file_length, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_BIG_FILE_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_body = "";
		txmsg.msg_url = file_url;
		txmsg.msg_path = SessionManager.getInstance().mDownUpMgr
				.getDownLoadBigFilePath(file_url);
		;
		txmsg.msg_file_length = file_length;
		txmsg.was_me = false;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		txmsg.updateState = TXMessage.UPDATE;// 待下载状态

		return txmsg;
	}

	/** 创建收到群聊的gif消息 */
	public static TXMessage creatGroupGIFSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String emod5,
			int pkgid, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_GIF_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_body = "";
		txmsg.emomd5 = emod5;
		txmsg.pkgid = pkgid;
		txmsg.was_me = false;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		txmsg.updateState = TXMessage.UPDATE;// 待下载状态

		return txmsg;
	}

	public static TXMessage creatGroupCommonSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String msg_body,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_COMMON_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_body = msg_body;
		txmsg.was_me = false;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 无引用，注掉 2014.01.22 shc
	// public static TXMessage creatGroupNotifySms(Context ctxt, long group_id,
	// String group_name,
	// String group_avatars_url, String msg_body, boolean was_me, int
	// read_state, long send_time) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_QU_NOTICE_SMS;
	// txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
	// txmsg.group_id = group_id;
	// txmsg.group_name = group_name;
	// txmsg.group_avatars_url = group_avatars_url;
	// txmsg.msg_body = msg_body;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }
	//
	// public static TXMessage creatGroupPromptSms(Context ctxt, long group_id,
	// String group_name,
	// String group_avatars_url, String msg_body, boolean was_me, int
	// read_state, long send_time) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_QU_PROMPT_SMS;
	// txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
	// txmsg.group_id = group_id;
	// txmsg.group_name = group_name;
	// txmsg.group_avatars_url = group_avatars_url;
	// txmsg.msg_body = msg_body;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }

	/**
	 * 创建地理位置消息
	 */
	public static TXMessage creatGeoSms(Context ctxt, long partner_id,
			String partner_name, String geo, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_GEO_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.geo = geo;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGeoSms(Context ctxt, long msg_id,
			long partner_id, String partner_name, String geo, boolean was_me,
			int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_GEO_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.geo = geo;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 无引用 2013.11.29 shc
	// public static TXMessage creatSmsGeo(Context ctxt, long partner_id, String
	// partner_name, String partner_phone,
	// String geo, boolean was_me, int read_state, long send_time, String phone)
	// {// 短信
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_SMS_GEO;
	// txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
	// txmsg.partner_id = partner_id;
	// txmsg.partner_name = partner_name;
	// txmsg.partner_phone = partner_phone;
	// txmsg.geo = geo;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }

	public static TXMessage creatGroupGeoSms(Context ctxt, long group_id,
			String group_name, String group_avatars_url, String geo,
			int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_GEO_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.geo = geo;
		txmsg.was_me = true;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupGeoSmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String geo,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_GEO_SMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.geo = geo;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 创建图片消息
	 */
	public static TXMessage creatImageEms(Context ctxt, long partner_id,
			String partner_name, String partner_phone, String img_path,
			String img_url, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_IMAGE_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = img_path;
		txmsg.msg_url = img_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 无引用 2013.11.29 shc
	// public static TXMessage creatImageSms(Context ctxt, long partner_id,
	// String partner_name, String partner_phone,
	// String img_path, String img_url, boolean was_me, int read_state, long
	// send_time) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_SMS_IMG;
	// txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
	// txmsg.partner_id = partner_id;
	// txmsg.partner_name = partner_name;
	// txmsg.partner_phone = partner_phone;
	// txmsg.msg_path = img_path;
	// txmsg.msg_url = img_url;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }

	public static TXMessage creatImageEms(Context ctxt, long msg_id,
			long partner_id, String partner_name, String img_path,
			String img_url, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_IMAGE_EMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = img_path;
		txmsg.msg_url = img_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 创建群图片
	 */
	public static TXMessage creatGroupImageEms(Context ctxt, long group_id,
			String group_name, String group_avatars_url, String img_path,
			String img_url, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_IMAGE_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = img_path;
		txmsg.msg_url = img_url;
		txmsg.was_me = true;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupImageEmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String img_path,
			String img_url, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_IMAGE_EMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = img_path;
		txmsg.msg_url = img_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 创建音频消息
	 */
	public static TXMessage creatAudioEms(Context ctxt, long partner_id,
			String partner_name, String partner_phone, String audio_path,
			String audio_url, long audio_length, long audio_times,
			String audio_end, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_AUDIO_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = audio_path;
		txmsg.msg_url = audio_url;
		txmsg.msg_file_length = audio_length;
		txmsg.audio_times = audio_times;
		txmsg.audio_end = audio_end;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatAudioEms(Context ctxt, long msg_id,
			long partner_id, String partner_name, String audio_path,
			String audio_url, long audio_length, long audio_times,
			String audio_end, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_AUDIO_EMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = audio_path;
		txmsg.msg_url = audio_url;
		txmsg.msg_file_length = audio_length;
		txmsg.audio_times = audio_times;
		txmsg.audio_end = audio_end;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 无引用 2013.11.29 shc
	// public static TXMessage creatAudioSms(Context ctxt, long partner_id,
	// String partner_name, String partner_phone,
	// String audio_path, String audio_url, long audio_length, long audio_times,
	// String audio_end, boolean was_me,
	// int read_state, long send_time, String phone) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_SMS_AUDIO;
	// txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
	// txmsg.partner_id = partner_id;
	// txmsg.partner_name = partner_name;
	// txmsg.partner_phone = partner_phone;
	// txmsg.msg_path = audio_path;
	// txmsg.msg_url = audio_url;
	// txmsg.msg_file_length = audio_length;
	// txmsg.audio_times = audio_times;
	// txmsg.audio_end = audio_end;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }

	/**
	 * 创建群音频
	 */
	public static TXMessage creatGroupAudioEms(Context ctxt, long group_id,
			String group_name, String group_avatars_url, String audio_path,
			String audio_url, long audio_length, long audio_times,
			String audio_end, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_AUDIO_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = audio_path;
		txmsg.msg_url = audio_url;
		txmsg.msg_file_length = audio_length;
		txmsg.audio_times = audio_times;
		txmsg.audio_end = audio_end;
		txmsg.was_me = true;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupAudioEmsWhenReceive(Context ctxt,
			long msg_id, long partner_id, String partner_name, long group_id,
			String group_name, String group_avatars_url, String audio_path,
			String audio_url, long audio_length, long audio_times,
			String audio_end, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_AUDIO_EMS;
		txmsg.msg_id = msg_id;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = audio_path;
		txmsg.msg_url = audio_url;
		txmsg.msg_file_length = audio_length;
		txmsg.audio_times = audio_times;
		txmsg.audio_end = audio_end;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * 创建名片消息
	 */
	public static TXMessage creatCardEms(Context ctxt,
			String contacts_person_name, long partner_id, String partner_name,
			String partner_phone, String card_path, String card_url,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_CARD_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = card_path;
		txmsg.msg_url = card_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatCardEms(Context ctxt, long msg_id,
			String contacts_person_name, long partner_id, String partner_name,
			String card_path, String card_url, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_CARD_EMS;
		txmsg.msg_id = msg_id;
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.msg_path = card_path;
		txmsg.msg_url = card_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 无引用 2013.11.29 shc
	// public static TXMessage creatCardSms(Context ctxt, long msg_id, String
	// contacts_person_name, long partner_id,
	// String partner_name, String partner_phone, String card_path, String
	// card_url, boolean was_me,
	// int read_state, long send_time) {
	// TXMessage txmsg = new TXMessage();
	// txmsg.msg_type = TxDB.MSG_TYPE_SMS_CRAD;
	// txmsg.msg_id = msg_id;
	// txmsg.contacts_person_name = contacts_person_name;
	// txmsg.partner_id = partner_id;
	// txmsg.partner_name = partner_name;
	// txmsg.partner_phone = partner_phone;
	// txmsg.msg_path = card_path;
	// txmsg.msg_url = card_url;
	// txmsg.was_me = was_me;
	// txmsg.read_state = read_state;
	// txmsg.send_time = send_time;
	// return txmsg;
	// }

	/**
	 * 创建群名片
	 */
	public static TXMessage creatGroupCardEms(Context ctxt,
			String contacts_person_name, long group_id, String group_name,
			String group_avatars_url, String card_path, String card_url,
			int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_CARD_EMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = card_path;
		txmsg.msg_url = card_url;
		txmsg.was_me = true;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupCardEmsWhenReceive(Context ctxt,
			long msg_id, String contacts_person_name, long sender_id,
			String partner_name, long group_id, String group_name,
			String group_avatars_url, String card_path, String card_url,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_CARD_EMS;
		txmsg.msg_id = msg_id;
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.partner_id = sender_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.msg_path = card_path;
		txmsg.msg_url = card_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	/**
	 * tcard
	 */
	public static TXMessage creatTCardEms(Context ctxt, long partnerid,
			String contacts_person_name, String tcard_name, long tcard_id,
			String tcard_sign, int tcard_sex, String tcard_phone,
			String tcard_avatar_url, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.partner_id = partnerid;
		txmsg.msg_type = TxDB.MSG_TYPE_TCARD_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_phone = tcard_phone;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatTCardEms(Context ctxt, long msg_id,
			String contacts_person_name, long partner_id,
			String partner_display_name, String tcard_name, long tcard_id,
			String tcard_sign, int tcard_sex, String tcard_phone,
			String tcard_avatar_url, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_TCARD_SMS;
		txmsg.msg_id = msg_id;
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		// txmsg.partner_phone = partner_phone;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_phone = tcard_phone;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupTCardEms(Context ctxt,
			String contacts_person_name, long group_id, String group_name,
			String group_avatars_url, String tcard_name, long tcard_id,
			String tcard_sign, int tcard_sex, String tcard_phone,
			String tcard_avatar_url, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_TCARD_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_phone = tcard_phone;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.was_me = true;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatGroupTCardEmsWhenReceive(Context ctxt,
			long msg_id, String contacts_person_name, long sender_id,
			String partner_name, long group_id, String group_name,
			String group_avatars_url, String tcard_name, long tcard_id,
			String tcard_sign, int tcard_sex, String tcard_phone,
			String tcard_avatar_url, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_QU_TCARD_SMS;
		txmsg.msg_id = msg_id;
		txmsg.contacts_person_name = contacts_person_name;
		txmsg.partner_id = sender_id;
		txmsg.partner_name = partner_name;
		txmsg.group_id = group_id;
		txmsg.group_name = group_name;
		txmsg.group_avatars_url = group_avatars_url;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_phone = tcard_phone;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 打招呼
	public static TXMessage creatGreetsms(Context ctxt,
			String contacts_person_name, long partner_id,
			String partner_display_name, String msg_body, String tcard_name,
			long tcard_id, String tcard_avatar_url, String tcard_sign,
			int tcard_sex, String greet, boolean was_me, int read_state,
			long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_GREET_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		txmsg.msg_body = msg_body;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.msg_body = greet;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static TXMessage creatContactssms(Context ctxt,
			String contacts_person_name, long partner_id,
			String partner_display_name, String msg_body, String tcard_name,
			long tcard_id, String tcard_sign, int tcard_sex,
			String tcard_phone, String tcard_avatar_url, boolean was_me,
			int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_CONTACTS_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		txmsg.msg_body = msg_body;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_phone = tcard_phone;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// sns
	public static TXMessage createSNSsms(Context ctxt,
			String contacts_person_name, long partner_id,
			String partner_display_name, String msg_body, String tcard_name,
			long tcard_id, String tcard_sign, int tcard_sex,
			String tcard_avatar_url, int sns_type, String sns_id,
			boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_SNS_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		txmsg.msg_body = msg_body;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.sns_id = sns_id;
		// txmsg.sns_type = sns_type;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 创建加好友请求
	public static TXMessage creatAddFriendsms(Context ctxt,
			String contacts_person_name, long partner_id,
			String partner_display_name, String msg_body, String tcard_name,
			long tcard_id, String tcard_sign, int tcard_sex,
			String tcard_avatar_url, String ac, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_ADD_FRIEND_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		txmsg.msg_body = msg_body;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.ac = ac;
		txmsg.was_me = false;
		txmsg.read_state = UNREAD;
		txmsg.send_time = send_time;
		return txmsg;
	}

	// 好友验证结果请求
	public static TXMessage creatAddFriendResms(Context ctxt,
			String contacts_person_name, long partner_id, String content,
			String partner_display_name, String tcard_name, long tcard_id,
			String tcard_sign, int tcard_sex, String tcard_avatar_url,
			boolean agree, boolean was_me, int read_state, long send_time) {
		TXMessage txmsg = new TXMessage();
		txmsg.msg_type = TxDB.MSG_TYPE_ADD_FRIEND_RE_SMS;
		txmsg.msg_id = Utils.createMsgId(getUser_id(ctxt));
		txmsg.contacts_person_name = tcard_name;
		txmsg.partner_id = partner_id;
		txmsg.partner_name = partner_display_name;
		txmsg.msg_body = content;
		txmsg.tcard_id = tcard_id;
		txmsg.tcard_name = tcard_name;
		txmsg.tcard_sign = tcard_sign;
		txmsg.tcard_sex = tcard_sex;
		txmsg.tcard_avatar_url = tcard_avatar_url;
		txmsg.agree = agree;
		txmsg.was_me = was_me;
		txmsg.read_state = read_state;
		txmsg.send_time = send_time;
		return txmsg;
	}

	public static String getUser_id(Context ctxt) {
		if (user_id.equals("9999994")) {
			// SharedPreferences prefs =
			// ctxt.getSharedPreferences(CommonData.MEME_PREFS,
			// Context.MODE_WORLD_READABLE
			// + Context.MODE_WORLD_WRITEABLE);
			// user_id = prefs.getString(CommonData.USER_ID, "9999994");
			user_id = TX.tm.getUserID() + "";
		}
		return user_id;
	}

	// 无引用 2014.05.05 shc
	// static class ColumeIds {
	// public int msg_type;
	// public int msg_id;
	// public int contacts_person_id;
	// public int contacts_person_name;
	// // public int sms_address;
	// public int partner_id;
	// public int partner_name;
	// public int partner_phone;
	// public int group_id;
	// public int group_name;
	// public int group_avatars_url;
	// public int msg_subject;
	// public int msg_body;
	// public int msg_path;
	// public int msg_url;
	// public int msg_file_length;
	// public int audio_times;
	// public int audio_end;
	// public int geo;
	// public int tcard_id;
	// public int tcard_name;
	// public int tcard_sex;
	// public int tcard_sign;
	// public int tcard_phone;
	// public int tcard_avatar_url;
	// public int ac;
	// public int sns_id;
	// public int sns_name;
	// public int sns_type;
	// public int agree;
	// public int was_me;
	// public int read_state;
	// public int msg_state;
	// public int updateState;
	// public int send_time;
	// public int channelId;
	// public int gmid;
	// public int group_id_notice;
	// public int sn;
	// public int rs;
	// public int op;
	// public int opId;
	// public int opName;
	// public int msg_id2;
	// public int msg_type2;
	// public int reportId;
	// public int reportName;
	// public int reportContext;
	// public int shutup_st;
	// public int shutup_du;
	// public int msg_sex;
	// public int msg_nickname;
	//
	// public ColumeIds(Cursor c) {
	// Init(c);
	// }
	//
	// public void Init(Cursor c) {
	// msg_type = c.getColumnIndex(TxDB.Messages.MSG_TYPE);
	// msg_id = c.getColumnIndex(TxDB.Messages.MSG_ID);
	// contacts_person_id = c.getColumnIndex(TxDB.Messages.CONTACTS_PERSON_ID);
	// contacts_person_name =
	// c.getColumnIndex(TxDB.Messages.CONTACTS_PERSON_NAME);
	// // sms_address = c.getColumnIndex(TxDB.Messages.SMS_ADDRESS);
	// partner_id = c.getColumnIndex(TxDB.Messages.MSG_PARTNER_ID);
	// partner_name = c.getColumnIndex(TxDB.Messages.MSG_PARTNER_NAME);
	// partner_phone = c.getColumnIndex(TxDB.Messages.MSG_PARTNER_PHONE);
	// group_id = c.getColumnIndex(TxDB.Messages.MSG_GROUP_ID);
	// group_name = c.getColumnIndex(TxDB.Messages.MSG_GROUP_NAME);
	// group_avatars_url = c.getColumnIndex(TxDB.Messages.MSG_GROUP_URL);
	// msg_subject = c.getColumnIndex(TxDB.Messages.MSG_SUBJECT);
	// msg_body = c.getColumnIndex(TxDB.Messages.MSG_BODY);
	// msg_path = c.getColumnIndex(TxDB.Messages.MSG_PATH);
	// msg_url = c.getColumnIndex(TxDB.Messages.MSG_URL);
	// msg_file_length = c.getColumnIndex(TxDB.Messages.MSG_FILE_LENGTH);
	// audio_times = c.getColumnIndex(TxDB.Messages.AUD_TIMES);
	// audio_end = c.getColumnIndex(TxDB.Messages.AUD_END);
	// geo = c.getColumnIndex(TxDB.Messages.GEO);
	// tcard_id = c.getColumnIndex(TxDB.Messages.TCARD_ID);
	// tcard_name = c.getColumnIndex(TxDB.Messages.TCARD_NAME);
	// tcard_sex = c.getColumnIndex(TxDB.Messages.TCARD_SEX);
	// tcard_sign = c.getColumnIndex(TxDB.Messages.TCARD_SIGN);
	// tcard_phone = c.getColumnIndex(TxDB.Messages.TCARD_PHONE);
	// tcard_avatar_url = c.getColumnIndex(TxDB.Messages.TCARD_AVATAR_URL);
	// ac = c.getColumnIndex(TxDB.Messages.AC);
	// sns_id = c.getColumnIndex(TxDB.Messages.SNS_ID);
	// sns_name = c.getColumnIndex(TxDB.Messages.SNS_NAME);
	// sns_type = c.getColumnIndex(TxDB.Messages.SNS_TYPE);
	// agree = c.getColumnIndex(TxDB.Messages.AGREE);
	// was_me = c.getColumnIndex(TxDB.Messages.WASME);
	// read_state = c.getColumnIndex(TxDB.Messages.READ_STATE);
	// msg_state = c.getColumnIndex(TxDB.Messages.MSG_STATE);
	// updateState = c.getColumnIndex(TxDB.Messages.UPDATE_STATE);
	// send_time = c.getColumnIndex(TxDB.Messages.SEND_TIME);
	// channelId = c.getColumnIndex(TxDB.Messages.CHANNEL_ID);
	// gmid = c.getColumnIndex(TxDB.Messages.GMID);
	// group_id_notice = c.getColumnIndex(TxDB.Messages.GROUP_ID_NOTICE);
	// sn = c.getColumnIndex(TxDB.Messages.SN);
	// rs = c.getColumnIndex(TxDB.Messages.RS);
	// op = c.getColumnIndex(TxDB.Messages.OP);
	// opId = c.getColumnIndex(TxDB.Messages.OP_ID);
	// opName = c.getColumnIndex(TxDB.Messages.OP_NAME);
	// msg_id2 = c.getColumnIndex(TxDB.Messages.MSG_ID_2);
	// msg_type2 = c.getColumnIndex(TxDB.Messages.MSG_TYPE_2);
	// reportId = c.getColumnIndex(TxDB.Messages.REPORT_ID);
	// reportName = c.getColumnIndex(TxDB.Messages.REPORT_NAME);
	// reportContext = c.getColumnIndex(TxDB.Messages.REPORT_CONTEXT);
	// shutup_st = c.getColumnIndex(TxDB.Messages.SHUTUP_ST);
	// shutup_du = c.getColumnIndex(TxDB.Messages.SHUTUP_DU);
	// msg_sex=c.getColumnIndex(TxDB.Messages.MSG_SEX);
	// }
	// }

	// public static TXMessage fetchOneMsg(Cursor c, ColumeIds ids) {
	// TXMessage chat = new TXMessage();
	// chat.msg_type = c.getInt(ids.msg_type);
	// chat.msg_id = c.getLong(ids.msg_id);
	// chat.contacts_person_id = c.getInt(ids.contacts_person_id);
	// chat.contacts_person_name = c.getString(ids.contacts_person_name);
	// // chat.sms_address = c.getString(ids.sms_address);
	// chat.partner_id = c.getLong(ids.partner_id);
	// chat.partner_name = c.getString(ids.partner_name);
	// // chat.partner_phone = c.getString(ids.partner_phone);
	// chat.group_id = c.getLong(ids.group_id);
	// chat.group_name = c.getString(ids.group_name);
	// chat.group_avatars_url = c.getString(ids.group_avatars_url);
	// chat.msg_subject = c.getString(ids.msg_subject);
	// chat.msg_body = c.getString(ids.msg_body);
	// chat.msg_path = c.getString(ids.msg_path);
	// chat.msg_url = c.getString(ids.msg_url);
	// chat.msg_file_length = c.getInt(ids.msg_file_length);
	// chat.audio_times = c.getLong(ids.audio_times);
	// chat.audio_end = c.getString(ids.audio_end);
	// chat.geo = c.getString(ids.geo);
	// chat.tcard_id = c.getLong(ids.tcard_id);
	// chat.tcard_name = c.getString(ids.tcard_name);
	// chat.tcard_sex = c.getInt(ids.tcard_sex);
	// chat.tcard_sign = c.getString(ids.tcard_sign);
	// chat.tcard_phone = c.getString(ids.tcard_phone);
	// chat.tcard_avatar_url = c.getString(ids.tcard_avatar_url);
	// chat.ac = c.getString(ids.ac);
	// chat.sns_id = c.getString(ids.sns_id);
	// chat.sns_name = c.getString(ids.sns_name);
	// chat.sns_type = c.getInt(ids.sns_type);
	// chat.agree = c.getString(ids.agree).equals("1");
	// chat.was_me = c.getString(ids.was_me).equals("1");
	// chat.read_state = c.getInt(ids.read_state);
	// // chat.msg_state = c.getInt(ids.msg_state);
	// chat.updateState = c.getInt(ids.updateState);
	// chat.send_time = c.getLong(ids.send_time);
	// // chat.channelId = c.getInt(ids.channelId);
	// chat.gmid = c.getLong(ids.gmid);
	// chat.group_id_notice = c.getInt(ids.group_id_notice);
	// chat.sn = c.getString(ids.sn);
	// chat.rs = c.getString(ids.rs);
	// chat.op = c.getInt(ids.op);
	// chat.opId = c.getInt(ids.opId);
	// chat.opName = c.getString(ids.opName);
	// chat.msg_id2 = c.getLong(ids.msg_id2);
	// chat.msg_type2 = c.getInt(ids.msg_type2);
	// chat.reportId = c.getLong(ids.reportId);
	// chat.reportName = c.getString(ids.reportName);
	// chat.reportContext = c.getString(ids.reportContext);
	// chat.shutup_st = c.getLong(ids.shutup_st);
	// chat.shutup_du = c.getLong(ids.shutup_du);
	// chat.sex=c.getInt(ids.msg_sex);
	// chat.nick_name=c.getString(ids.msg_nickname);
	//
	//
	// return chat;
	// }

	public static TXMessage fetchOneMsg(Cursor c) {
		TXMessage chat = new TXMessage();
		chat.msg_type = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_TYPE));
		chat.msg_id = c.getLong(c.getColumnIndex(TxDB.Messages.MSG_ID));
		chat.contacts_person_id = c.getInt(c
				.getColumnIndex(TxDB.Messages.CONTACTS_PERSON_ID));
		chat.contacts_person_name = c.getString(c
				.getColumnIndex(TxDB.Messages.CONTACTS_PERSON_NAME));
		// chat.sms_address =
		// c.getString(c.getColumnIndex(TxDB.Messages.SMS_ADDRESS));
		chat.partner_id = c.getLong(c
				.getColumnIndex(TxDB.Messages.MSG_PARTNER_ID));
		chat.partner_name = c.getString(c
				.getColumnIndex(TxDB.Messages.MSG_PARTNER_NAME));
		// chat.partner_phone =
		// c.getString(c.getColumnIndex(TxDB.Messages.MSG_PARTNER_PHONE));
		chat.group_id = c.getLong(c.getColumnIndex(TxDB.Messages.MSG_GROUP_ID));
		chat.group_name = c.getString(c
				.getColumnIndex(TxDB.Messages.MSG_GROUP_NAME));
		chat.group_avatars_url = c.getString(c
				.getColumnIndex(TxDB.Messages.MSG_GROUP_URL));
		chat.msg_subject = c.getString(c
				.getColumnIndex(TxDB.Messages.MSG_SUBJECT));
		chat.msg_body = c.getString(c.getColumnIndex(TxDB.Messages.MSG_BODY));
		chat.msg_path = c.getString(c.getColumnIndex(TxDB.Messages.MSG_PATH));
		chat.msg_url = c.getString(c.getColumnIndex(TxDB.Messages.MSG_URL));
		chat.msg_file_length = c.getInt(c
				.getColumnIndex(TxDB.Messages.MSG_FILE_LENGTH));
		chat.audio_times = c.getLong(c.getColumnIndex(TxDB.Messages.AUD_TIMES));
		chat.audio_end = c.getString(c.getColumnIndex(TxDB.Messages.AUD_END));
		chat.geo = c.getString(c.getColumnIndex(TxDB.Messages.GEO));
		chat.tcard_id = c.getLong(c.getColumnIndex(TxDB.Messages.TCARD_ID));
		chat.tcard_name = c.getString(c
				.getColumnIndex(TxDB.Messages.TCARD_NAME));
		chat.tcard_sex = c.getInt(c.getColumnIndex(TxDB.Messages.TCARD_SEX));
		chat.tcard_sign = c.getString(c
				.getColumnIndex(TxDB.Messages.TCARD_SIGN));
		chat.tcard_phone = c.getString(c
				.getColumnIndex(TxDB.Messages.TCARD_PHONE));
		chat.tcard_avatar_url = c.getString(c
				.getColumnIndex(TxDB.Messages.TCARD_AVATAR_URL));
		chat.ac = c.getString(c.getColumnIndex(TxDB.Messages.AC));
		chat.sns_id = c.getString(c.getColumnIndex(TxDB.Messages.SNS_ID));
		chat.sns_name = c.getString(c.getColumnIndex(TxDB.Messages.SNS_NAME));
		// chat.sns_type = c.getInt(c.getColumnIndex(TxDB.Messages.SNS_TYPE));
		chat.agree = c.getString(c.getColumnIndex(TxDB.Messages.AGREE)).equals(
				"1");
		chat.was_me = c.getString(c.getColumnIndex(TxDB.Messages.WASME))
				.equals("1");
		chat.read_state = c.getInt(c.getColumnIndex(TxDB.Messages.READ_STATE));
		// chat.msg_state = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_STATE));
		chat.updateState = c.getInt(c
				.getColumnIndex(TxDB.Messages.UPDATE_STATE));
		chat.send_time = c.getLong(c.getColumnIndex(TxDB.Messages.SEND_TIME));
		chat.praisedState = c.getInt(c
				.getColumnIndex(TxDB.Messages.PRAISE_STATE));
		chat.fileDownTime = c.getString(c
				.getColumnIndex(TxDB.Messages.FILE_DOWN_TIME));
		chat.gmid = c.getLong(c.getColumnIndex(TxDB.Messages.GMID));
		chat.group_id_notice = c.getInt(c
				.getColumnIndex(TxDB.Messages.GROUP_ID_NOTICE));
		chat.sn = c.getString(c.getColumnIndex(TxDB.Messages.SN));
		chat.rs = c.getString(c.getColumnIndex(TxDB.Messages.RS));
		chat.op = c.getInt(c.getColumnIndex(TxDB.Messages.OP));
		chat.opId = c.getInt(c.getColumnIndex(TxDB.Messages.OP_ID));
		chat.opName = c.getString(c.getColumnIndex(TxDB.Messages.OP_NAME));
		chat.msg_id2 = c.getLong(c.getColumnIndex(TxDB.Messages.MSG_ID_2));
		chat.msg_type2 = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_TYPE_2));
		chat.reportId = c.getLong(c.getColumnIndex(TxDB.Messages.REPORT_ID));
		chat.reportName = c.getString(c
				.getColumnIndex(TxDB.Messages.REPORT_NAME));
		chat.reportContext = c.getString(c
				.getColumnIndex(TxDB.Messages.REPORT_CONTEXT));
		chat.shutup_st = c.getLong(c.getColumnIndex(TxDB.Messages.SHUTUP_ST));
		chat.shutup_du = c.getLong(c.getColumnIndex(TxDB.Messages.SHUTUP_DU));
		chat.sex = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_SEX));
		chat.emomd5 = c.getString(c.getColumnIndex(TxDB.Messages.MSG_EMOD5));
		chat.num = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_NUM));
		chat.pkgid = c.getInt(c.getColumnIndex(TxDB.Messages.MSG_PKGID));
		chat.pkg_emomd5 = c.getString(c.getColumnIndex(TxDB.Messages.MSG_PKG_EMOMD5));

		return chat;
	}

	public static ArrayList<TXMessage> fetchAllDBMsgs(Cursor c) {
		ArrayList<TXMessage> ret = new ArrayList<TXMessage>();
		// ColumeIds ids=new ColumeIds(c);
		while (c.moveToNext()) {
			ret.add(fetchOneMsg(c));
		}

		return ret;
	}

	/**
	 * 更新或保存消息到数据库
	 * 
	 * @param isUpdateMsgStat
	 *            是否更新消息会话表
	 * @return
	 */
	public static Uri saveTXMessagetoDB(final TXMessage tmsg,
			ContentResolver cr, boolean isUpdateMsgStat) {
		// 存数据库之前存cache
		// saveNewMessageToCache(tmsg);
		synchronized (lock) {
			ContentValues values = new ContentValues();
			Uri aMsgUri = ContentUris.withAppendedId(TxDB.Messages.CONTENT_URI,
					tmsg.msg_id);
			values.put(TxDB.Messages._ID, tmsg.msg_id);
			values.put(TxDB.Messages.MSG_TYPE, tmsg.msg_type);
			values.put(TxDB.Messages.MSG_ID, tmsg.msg_id);
			values.put(TxDB.Messages.CONTACTS_PERSON_ID,
					tmsg.contacts_person_id);
			values.put(TxDB.Messages.CONTACTS_PERSON_NAME,
					tmsg.contacts_person_name);
			// values.put(TxDB.Messages.SMS_ADDRESS, tmsg.sms_address);
			values.put(TxDB.Messages.MSG_PARTNER_ID, tmsg.partner_id);
			values.put(TxDB.Messages.MSG_PARTNER_NAME, tmsg.partner_name);
			// values.put(TxDB.Messages.MSG_PARTNER_PHONE, tmsg.partner_phone);
			values.put(TxDB.Messages.MSG_GROUP_ID, tmsg.group_id);
			values.put(TxDB.Messages.MSG_GROUP_NAME, tmsg.group_name);
			values.put(TxDB.Messages.MSG_GROUP_URL, tmsg.group_avatars_url);
			values.put(TxDB.Messages.MSG_SUBJECT, tmsg.msg_subject);
			values.put(TxDB.Messages.MSG_BODY, tmsg.msg_body);
			values.put(TxDB.Messages.MSG_PATH, tmsg.msg_path);
			values.put(TxDB.Messages.MSG_URL, tmsg.msg_url);
			values.put(TxDB.Messages.MSG_FILE_LENGTH, tmsg.msg_file_length);
			values.put(TxDB.Messages.AUD_TIMES, tmsg.audio_times);
			values.put(TxDB.Messages.AUD_END, tmsg.audio_end);
			values.put(TxDB.Messages.GEO, tmsg.geo);
			values.put(TxDB.Messages.TCARD_ID, tmsg.tcard_id);
			values.put(TxDB.Messages.TCARD_NAME, tmsg.tcard_name);
			values.put(TxDB.Messages.TCARD_SEX, tmsg.tcard_sex);
			values.put(TxDB.Messages.TCARD_SIGN, tmsg.tcard_sign);
			values.put(TxDB.Messages.TCARD_PHONE, tmsg.tcard_phone);
			values.put(TxDB.Messages.TCARD_AVATAR_URL, tmsg.tcard_avatar_url);
			values.put(TxDB.Messages.SNS_ID, tmsg.sns_id);
			values.put(TxDB.Messages.SNS_NAME, tmsg.sns_name);
			// values.put(TxDB.Messages.SNS_TYPE, tmsg.sns_type);
			values.put(TxDB.Messages.AGREE, tmsg.agree);
			values.put(TxDB.Messages.AC, tmsg.ac);
			values.put(TxDB.Messages.WASME, tmsg.was_me);
			values.put(TxDB.Messages.READ_STATE, tmsg.read_state);
			values.put(TxDB.Messages.PRAISE_STATE, tmsg.praisedState);
			values.put(TxDB.Messages.FILE_DOWN_TIME, tmsg.fileDownTime);
			values.put(TxDB.Messages.UPDATE_STATE, tmsg.updateState);
			values.put(TxDB.Messages.SEND_TIME, tmsg.send_time);
			// values.put(TxDB.Messages.CHANNEL_ID, tmsg.getChannelId());
			values.put(TxDB.Messages.GMID, tmsg.gmid);
			values.put(TxDB.Messages.GROUP_ID_NOTICE, tmsg.group_id_notice);
			values.put(TxDB.Messages.SN, tmsg.sn);
			values.put(TxDB.Messages.RS, tmsg.rs);
			values.put(TxDB.Messages.OP, tmsg.op);
			values.put(TxDB.Messages.OP_ID, tmsg.opId);
			values.put(TxDB.Messages.OP_NAME, tmsg.opName);
			values.put(TxDB.Messages.MSG_ID_2, tmsg.msg_id2);
			values.put(TxDB.Messages.MSG_TYPE_2, tmsg.msg_type2);
			values.put(TxDB.Messages.REPORT_ID, tmsg.reportId);
			values.put(TxDB.Messages.REPORT_NAME, tmsg.reportName);
			values.put(TxDB.Messages.REPORT_CONTEXT, tmsg.reportContext);
			values.put(TxDB.Messages.SHUTUP_ST, tmsg.shutup_st);
			values.put(TxDB.Messages.SHUTUP_DU, tmsg.shutup_du);
			values.put(TxDB.Messages.MSG_SEX, tmsg.sex);
			values.put(TxDB.Messages.MSG_EMOD5, tmsg.emomd5);
			values.put(TxDB.Messages.MSG_NUM, tmsg.num);
			values.put(TxDB.Messages.MSG_PKGID, tmsg.pkgid);
			values.put(TxDB.Messages.MSG_PKG_EMOMD5, tmsg.pkg_emomd5);
			if ((cr.update(aMsgUri, values, null, null)) == 0) {
				cr.insert(TxDB.Messages.CONTENT_URI, values);
			}

			if (isUpdateMsgStat) {
				// 更新消息会话数据库
				if (tmsg.msg_type > 0) {
					if (tmsg.group_id == TxGroup.ACCOST_ID) {

					} else if (Utils.isIdValid(tmsg.group_id)) {
						MsgStat.updateMsgStatByTxmsg(tmsg, cr, TxDB.MS_TYPE_QU,
								tmsg.gmid, -1, false);
					} else if (Utils.isIdValid(tmsg.partner_id)) {
						MsgStat.updateMsgStatByTxmsg(tmsg, cr, TxDB.MS_TYPE_TB,
								tmsg.gmid, -1, false);
					} else {
					}
					SessionManager.broadcastMsg(TxData.FLUSH_MSGS);// 发送刷新list会话列表广播
				}
			}
			return aMsgUri;
		}
	}

	public static void saveNewMessageToCache(TXMessage tmsg) {
		// 群消息
		if (tmsg.partner_id != -1 || tmsg.group_id != -1) {
			if (tmsg.group_id == -1) {
				ArrayList<TXMessage> arrayList = mSess.lastMsgCatch
						.get(tmsg.partner_id);
				if (arrayList != null) {
					boolean hasMsgId = false;
					for (int i = 0; i < arrayList.size(); i++) {
						TXMessage mtmsg = arrayList.get(i);
						if (mtmsg.msg_id == tmsg.msg_id) {
							hasMsgId = true;
							mtmsg.msg_path = tmsg.msg_path;
							mtmsg.updateState = tmsg.updateState;
						}
					}
					if (!hasMsgId) {
						lastMstCacthAdd(arrayList, tmsg);
					}
				}
			} else {
				ArrayList<TXMessage> arrayList = mSess.lastMsgCatch
						.get(tmsg.group_id);
				if (arrayList != null) {
					boolean hasMsgId = false;
					for (int i = 0; i < arrayList.size(); i++) {
						TXMessage mtmsg = arrayList.get(i);
						if (mtmsg.msg_id == tmsg.msg_id) {
							hasMsgId = true;
							mtmsg.msg_path = tmsg.msg_path;
							mtmsg.updateState = tmsg.updateState;
						}
					}
					if (!hasMsgId) {
						lastMstCacthAdd(arrayList, tmsg);
					}
				}
			}
		}
	}

	/**
	 * 对缓存List限制的单独处理方法
	 */
	public static void lastMstCacthAdd(ArrayList<TXMessage> arrayList,
			TXMessage tmsg) {
		if (arrayList.size() == 20) {
			if (Utils.debug) {
				Log.i("Zzl", "执行了去掉第一条");
			}
			arrayList.remove(arrayList.size() - 1);
		}
		arrayList.add(0, tmsg);
		if (Utils.debug) {
			// Log.i("Zzl",
			// "arraylist.size:"+arrayList.size()+arrayList.toString());
		}

	}

	public String toString() {
		// StringBuffer sb = new StringBuffer();
		// sb.append(msg_type).append(":").append(msg_id).append(":")
		// .append(msg_body).append(":").append(":").append(msg_url)
		// .append(contacts_person_id).append(":")
		// .append(contacts_person_name).append(":").append(partner_id)
		// .append(":").append(group_id).append(":").append(partner_name)
		// .append(":").append(msg_subject).append(":").append(msg_body)
		// .append(":").append(msg_path).append(":")
		// .append(msg_file_length).append(":").append(audio_times)
		// .append(":").append(audio_end).append(":").append(geo)
		// .append("::sns_id==").append(sns_id).append(":").append(was_me)
		// .append(":").append(send_time).append(":").append(sex)
		// .append(":").append(nick_name).append(":").append(updateState);
		// return sb.toString();
		return "[ pkgid :" + pkgid + ",emod5 :" + emomd5 + ",num : " + num
				+ " ]";
	}

	public static final Parcelable.Creator<TXMessage> CREATOR = new Parcelable.Creator<TXMessage>() {
		public TXMessage createFromParcel(Parcel in) {
			return new TXMessage(in);
		}

		public TXMessage[] newArray(int size) {
			return new TXMessage[size];
		}
	};

	private TXMessage(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(msg_type);
		out.writeLong(msg_id);
		// out.writeInt(mms_sms_id);
		// out.writeInt(thread_id);
		// out.writeString(morechat_send_to_person_name);
		out.writeInt(contacts_person_id);
		out.writeString(contacts_person_name);
		// out.writeString(sms_address);
		out.writeLong(partner_id);
		out.writeString(partner_name);
		// out.writeString(partner_phone);
		out.writeLong(group_id);
		out.writeString(group_name);
		out.writeString(group_avatars_url);
		out.writeString(msg_subject);
		out.writeString(msg_body);
		out.writeString(msg_path);
		out.writeString(msg_url);
		out.writeLong(msg_file_length);
		out.writeLong(audio_times);
		out.writeString(geo);
		out.writeLong(tcard_id);
		out.writeString(tcard_name);
		out.writeInt(tcard_sex);
		out.writeString(tcard_sign);
		out.writeString(tcard_phone);
		out.writeString(tcard_avatar_url);
		out.writeString(ac);
		out.writeString(sns_id);
		// out.writeInt(sns_type);
		// out.writeInt(msg_state);
		out.writeInt(updateState);
		boolean[] bool = new boolean[2];
		bool[0] = was_me;
		bool[1] = agree;
		out.writeBooleanArray(bool);
		out.writeLong(send_time);
		// out.writeInt(channelId);
		out.writeInt(read_state);
		out.writeLong(gmid);
		out.writeLong(group_id_notice);
		out.writeString(sn);
		out.writeString(rs);
		out.writeInt(op);
		out.writeInt(opId);
		out.writeString(opName);
		out.writeLong(msg_id2);
		out.writeLong(msg_type2);
		out.writeLong(reportId);
		out.writeString(reportName);
		out.writeString(reportContext);
		out.writeLong(shutup_st);
		out.writeLong(shutup_du);
		out.writeInt(sex);
		out.writeString(nick_name);
		out.writeInt(praisedState);
		out.writeString(fileDownTime);

	}

	public void readFromParcel(Parcel in) {
		msg_type = in.readInt();
		msg_id = in.readLong();
		// mms_sms_id = in.readInt();
		// thread_id = in.readInt();
		// morechat_send_to_person_name = in.readString();
		contacts_person_id = in.readInt();
		contacts_person_name = in.readString();
		// sms_address = in.readString();
		partner_id = in.readLong();
		partner_name = in.readString();
		// partner_phone = in.readString();
		group_id = in.readLong();
		group_name = in.readString();
		group_avatars_url = in.readString();
		msg_subject = in.readString();
		msg_body = in.readString();
		msg_path = in.readString();
		msg_url = in.readString();
		msg_file_length = in.readLong();
		audio_times = in.readLong();
		geo = in.readString();
		tcard_id = in.readLong();
		tcard_name = in.readString();
		tcard_sex = in.readInt();
		tcard_sign = in.readString();
		tcard_phone = in.readString();
		tcard_avatar_url = in.readString();
		ac = in.readString();
		sns_id = in.readString();
		// sns_type = in.readInt();
		// msg_state = in.readInt();
		updateState = in.readInt();
		boolean[] bool = new boolean[2];
		in.readBooleanArray(bool);
		was_me = bool[0];
		agree = bool[0];
		send_time = in.readLong();
		// channelId = in.readInt();
		read_state = in.readInt();
		gmid = in.readLong();
		group_id_notice = in.readLong();
		sn = in.readString();
		rs = in.readString();
		opId = in.readInt();
		op = in.readInt();
		opName = in.readString();
		msg_id2 = in.readLong();
		msg_type2 = in.readLong();
		reportId = in.readLong();
		reportName = in.readString();
		reportContext = in.readString();
		shutup_st = in.readLong();
		shutup_du = in.readLong();
		sex = in.readInt();
		nick_name = in.readString();
		praisedState = in.readInt();
		fileDownTime = in.readString();
	}

	/**
	 * 根据ID删除神聊信息
	 */
	public static int deleteByMsgId(ContentResolver cr, long id) {
		int count = cr.delete(TxDB.Messages.CONTENT_URI, TxDB.Messages.MSG_ID
				+ "=" + id, null);
		return count;
	}

	/**
	 * 根据gmid删除神聊信息
	 */

	public static int deleleByGmid(ContentResolver cr, long id) {

		int count = cr.delete(TxDB.Messages.CONTENT_URI, TxDB.Messages.GMID
				+ "=" + id, null);
		return count;

	}

	/**
	 * 根据msgID获得该消息的消息状态
	 */
	public static int getMsgStatById(ContentResolver cr, long id) {
		int state = -1;
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI,
				new String[] { TxDB.Messages.READ_STATE }, TxDB.Messages.MSG_ID
						+ "=" + id, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				state = c.getInt(0);
			}
		}
		return state;
	}

	/**
	 * 更新消息状态
	 */
	public static int updateByMsgId(ContentResolver cr, long msgID,
			int readState) {
		// 更新MsgStat
		MsgStat.updateMsgStatReadState(cr, msgID, readState);
		// 更新数据库
		ContentValues cv = new ContentValues();
		cv.put(TxDB.Messages.READ_STATE, readState);
		return cr.update(TxDB.Messages.CONTENT_URI, cv, TxDB.Messages.MSG_ID
				+ "=" + msgID, null);
	}

	/**
	 * 更新消息状态
	 */
	public static int updateByMsgId(ContentResolver cr, long msgID,
			int readState, long gmid) {
		// 更新MsgStat
		MsgStat.updateMsgStatReadState(cr, msgID, readState);
		// 更新数据库
		ContentValues cv = new ContentValues();
		cv.put(TxDB.Messages.READ_STATE, readState);
		cv.put(TxDB.Messages.GMID, gmid);
		return cr.update(TxDB.Messages.CONTENT_URI, cv, TxDB.Messages.MSG_ID
				+ "=" + msgID, null);
	}

	/** 更新消息的的values，2013.09.25 shc */
	public static int updateMsgValues(ContentResolver cr, long msgID,
			int readState, String msgUrl) {
		// 更新MsgStat
		MsgStat.updateMsgStatReadState(cr, msgID, readState);
		// 更新数据库
		ContentValues values = new ContentValues();
		values.put(TxDB.Messages.READ_STATE, readState);
		values.put(TxDB.Messages.MSG_URL, msgUrl);
		return cr.update(TxDB.Messages.CONTENT_URI, values,
				TxDB.Messages.MSG_ID + "=" + msgID, null);
	}

	/**
	 * 更新Tcard消息
	 */
	public static int updateTcardTXMessage(ContentResolver cr, TXMessage msg) {
		ContentValues cv = new ContentValues();
		cv.put(TxDB.Messages.TCARD_SEX, msg.tcard_sex);
		cv.put(TxDB.Messages.TCARD_NAME, msg.tcard_name);
		cv.put(TxDB.Messages.TCARD_SIGN, msg.tcard_sign);
		cv.put(TxDB.Messages.TCARD_AVATAR_URL, msg.tcard_avatar_url);
		cv.put(TxDB.Messages.TCARD_PHONE, msg.tcard_phone);
		return cr.update(TxDB.Messages.CONTENT_URI, cv, TxDB.Messages.MSG_ID
				+ "=" + msg.msg_id, null);
	}

	/** 获取群组动态信息集合 */
	public static ArrayList<TXMessage> getSLGroupNoticeList(ContentResolver cr) {
		ArrayList<TXMessage> tmp = new ArrayList<TXMessage>();
		Cursor cur = cr.query(TxDB.Messages.CONTENT_URI, null,
				TxDB.Messages.MSG_PARTNER_ID + "= ? ", new String[] { ""
						+ TX.SL_GROUP_NOTICE }, TxDB.Messages.SEND_TIME
						+ " DESC");
		if (cur != null) {
			tmp = TXMessage.fetchAllDBMsgs(cur);
			cur.close();
		}
		return tmp;
	}

	/** 删除所有群组动态信息 */
	public static int clearSLGroupNoticeList(ContentResolver cr) {
		int delNum = cr.delete(TxDB.Messages.CONTENT_URI,
				TxDB.Messages.MSG_PARTNER_ID + "= ? ", new String[] { ""
						+ TX.SL_GROUP_NOTICE });
		return delNum;
	}

	/** 获取神聊小卫士消息列表 */
	public static ArrayList<TXMessage> getSLSafeList(ContentResolver cr) {
		ArrayList<TXMessage> tmp = new ArrayList<TXMessage>();
		Cursor cur = cr.query(TxDB.Messages.CONTENT_URI, null,
				TxDB.Messages.MSG_PARTNER_ID + "= ? ", new String[] { ""
						+ TX.SL_SAFE }, TxDB.Messages.SEND_TIME + " DESC");
		if (cur != null) {
			tmp = TXMessage.fetchAllDBMsgs(cur);
			cur.close();
		}
		return tmp;
	}

	/** 删除所有神聊小卫士消息 */
	public static int clearSLSafeList(ContentResolver cr) {
		int delNum = cr.delete(TxDB.Messages.CONTENT_URI,
				TxDB.Messages.MSG_PARTNER_ID + "= ? ", new String[] { ""
						+ TX.SL_SAFE });
		return delNum;
	}

	/**
	 * 读取单聊历史消息
	 */
	public static ArrayList<TXMessage> filterSingleMessageList(
			ContentResolver cr, long partnerID, int beginpos) {
		ArrayList<TXMessage> msgs = new ArrayList<TXMessage>();
		String selection = TxDB.Messages.MSG_PARTNER_ID + "=" + partnerID
				+ " AND " + TxDB.Messages.MSG_TYPE + ">0 AND "
				+ TxDB.Messages.MSG_GROUP_ID + " < 0 AND ("
				+ TxDB.Messages.CHANNEL_ID + "=0 or "
				+ TxDB.Messages.CHANNEL_ID + " is null) and "
				+ TxDB.Messages.MSG_GROUP_ID + " <> " + TxGroup.ACCOST_ID;
		ArrayList<TXMessage> dbmsgs = new ArrayList<TXMessage>();
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, selection, null,
				"send_time desc limit " + beginpos + "," + SELECT_COUNT);
		if (c != null) {
			dbmsgs = TXMessage.fetchAllDBMsgs(c);
			c.close();
		}
		if (dbmsgs != null && dbmsgs.size() > 0) {
			msgs.addAll(dbmsgs);
		}
		Collections.sort(msgs, new MessageComparator());
		return msgs;
	}

	// 无引用，暂时注掉 2013.10.11 shc
	// public static ArrayList<TXMessage> filterTXMsgList(final GroupMsgRoom
	// msgroom, final ContentResolver cr, int page) {
	// ArrayList<TXMessage> msgs = new ArrayList<TXMessage>();
	// String selection = TxDB.Messages.MSG_GROUP_ID + "=" +
	// msgroom.txGroup.group_id + " AND "
	// + TxDB.Messages.MSG_TYPE + ">0 AND (" + TxDB.Messages.CHANNEL_ID +
	// "=0 or " + TxDB.Messages.CHANNEL_ID
	// + " is null)";
	// int beginPos = page * SELECT_COUNT - SELECT_COUNT;
	// if (Utils.isIdValid(msgroom.txGroup.group_id)) {
	// ArrayList<TXMessage> dbmsgs = new ArrayList<TXMessage>();
	// Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, selection, null,
	// "send_time desc limit " + beginPos
	// + "," + SELECT_COUNT);
	// if (c != null) {
	// dbmsgs = TXMessage.fetchAllDBMsgs(c);
	// c.close();
	// }
	// if (dbmsgs != null && dbmsgs.size() > 0) {
	// msgs.addAll(dbmsgs);
	// }
	//
	// }
	// msgroom.isComMsglist = true;
	// return msgs;
	// }

	/**
	 * 读取群聊历史消息
	 */
	public static ArrayList<TXMessage> filterGroupMessageList(
			ContentResolver cr, long groupID, int beginpos) {
		// ArrayList<TXMessage> msgs = new ArrayList<TXMessage>();
		String selection = TxDB.Messages.MSG_GROUP_ID + "=" + groupID + " AND "
				+ TxDB.Messages.MSG_TYPE + ">0 AND ("
				+ TxDB.Messages.CHANNEL_ID + "=0 or "
				+ TxDB.Messages.CHANNEL_ID + " is null)";
		ArrayList<TXMessage> dbmsgs = new ArrayList<TXMessage>();
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, selection, null,
				"send_time desc limit " + beginpos + "," + SELECT_COUNT);
		if (c != null) {
			dbmsgs = TXMessage.fetchAllDBMsgs(c);
			c.close();
		}
		return dbmsgs;
	}

	/**
	 * 读取群聊历史消息
	 */
	public static ArrayList<TXMessage> filterGroupMessageUnreadList(
			ContentResolver cr, long groupID, int beginpos) {
		ArrayList<TXMessage> msgs = new ArrayList<TXMessage>();
		String selection = TxDB.Messages.MSG_GROUP_ID + "=" + groupID + " AND "
				+ TxDB.Messages.MSG_TYPE + ">0 AND " + TxDB.Messages.READ_STATE
				+ " = " + TXMessage.UNREAD + " AND  ("
				+ TxDB.Messages.CHANNEL_ID + "=0 or "
				+ TxDB.Messages.CHANNEL_ID + " is null)";
		ArrayList<TXMessage> dbmsgs = new ArrayList<TXMessage>();
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, selection, null,
				"gmid desc limit " + beginpos + "," + SELECT_COUNT);
		if (c != null) {
			dbmsgs = TXMessage.fetchAllDBMsgs(c);
			c.close();
		}
		if (dbmsgs != null && dbmsgs.size() > 0) {
			msgs.addAll(dbmsgs);
		}
		return msgs;
	}

	/**
	 * 读取聊天室历史消息
	 */
	public static ArrayList<TXMessage> filterTXMsgList(int channelId,
			final ContentResolver cr, int beginPos) {
		ArrayList<TXMessage> msgs = new ArrayList<TXMessage>();
		String selection = TxDB.Messages.CHANNEL_ID + "=" + channelId + " AND "
				+ TxDB.Messages.MSG_TYPE + ">0";
		if (Utils.isIdValid(channelId)) {
			ArrayList<TXMessage> dbmsgs = new ArrayList<TXMessage>();
			Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, selection,
					null, "send_time desc limit " + beginPos + ","
							+ SELECT_COUNT);
			if (c != null) {
				dbmsgs = TXMessage.fetchAllDBMsgs(c);
				c.close();
			}
			if (dbmsgs != null && dbmsgs.size() > 0) {
				msgs.addAll(dbmsgs);
			}
		}
		return msgs;
	}

	/**
	 * 根据TCARD_ID获取TXMessage对象
	 */
	public static TXMessage findTXMessageByTcardId(ContentResolver cr, String id) {
		TXMessage cm = null;
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null,
				TxDB.Messages.TCARD_ID + "=? ", new String[] { id }, null);
		if (c != null && c.moveToNext()) {
			cm = TXMessage.fetchOneMsg(c);
		}
		if (c != null) {
			c.close();
		}
		return cm;
	}

	/**
	 * 根据gmid获取TXMessage对象
	 */
	public static TXMessage findTXMessageByGmid(ContentResolver cr, long gmid) {
		TXMessage cm = null;
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null, TxDB.Messages.GMID
				+ "=? ", new String[] { "" + gmid }, null);
		if (c != null && c.moveToNext()) {
			cm = TXMessage.fetchOneMsg(c);
		}
		if (c != null) {
			c.close();
		}
		return cm;
	}

	/**
	 * 通过msgid获取TXMessage对象
	 */
	public static TXMessage findTXMessageByMsgid(ContentResolver cr,
			String msgid) {
		TXMessage tm = null;
		Cursor c = cr.query(TxDB.Messages.CONTENT_URI, null,
				TxDB.Messages.MSG_ID + "=?", new String[] { msgid }, null);
		if (c != null && c.moveToNext()) {
			tm = fetchOneMsg(c);
		}
		if (c != null) {
			c.close();
		}
		return tm;
	}

	// 从TxData中挪过来的 2014.04.23 shc
	public static ArrayList<TXMessage> getFriendHelperList(ContentResolver cr) {
		ArrayList<TXMessage> tmp = new ArrayList<TXMessage>();
		Cursor cur = cr.query(TxDB.Messages.CONTENT_URI, null,
				TxDB.Messages.MSG_PARTNER_ID + "= ? AND "
						+ TxDB.Messages.MSG_TYPE + " <> ? and "
						+ TxDB.Messages.TCARD_ID + " > 0 and "
						+ TxDB.Messages.TCARD_ID + " <> ? ", new String[] {
						"" + TX.TUIXIN_FRIEND, "" + TxDB.MSG_TYPE_TCARD_SMS,
						"" + TX.tm.getTxMe().partner_id },
				TxDB.Messages.SEND_TIME);
		if (cur != null) {
			tmp = TXMessage.fetchAllDBMsgs(cur);
			cur.close();
		}
		return tmp;
	}

	// public int getChannelId() {
	// return channelId;
	// }
	//
	// public void setChannelId(int channelId) {
	// this.channelId = channelId;
	// }

	@Override
	public int hashCode() {
		return Long.valueOf(msg_id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TXMessage)) {
			return false;
		}
		TXMessage objTx = (TXMessage) obj;
		if (msg_id != objTx.msg_id) {
			return false;
		}
		return true;
	}

}
