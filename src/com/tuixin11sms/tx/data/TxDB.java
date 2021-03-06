package com.tuixin11sms.tx.data;

import android.net.Uri;
import android.provider.BaseColumns;

import com.tuixin11sms.tx.db.SubDBHelper;
import com.tuixin11sms.tx.db.TxDBContentProvider;

public class TxDB {
	// msg 消息类型
	public static final int MSG_TYPE_DRAFT = 0;// 草稿
	public static final int MSG_TYPE_COMMON_SMS = 1;// 神聊普通消息
	public static final int MSG_TYPE_GEO_SMS = 2;// 神聊地理位置
	public static final int MSG_TYPE_IMAGE_EMS = 3;// 神聊图片消息
	public static final int MSG_TYPE_AUDIO_EMS = 4;// 神聊音频消息
	public static final int MSG_TYPE_CARD_EMS = 5;// 神聊通讯录名片消息
	public static final int MSG_TYPE_TCARD_SMS = 6;// 神聊联系人名片消息
	public static final int MSG_TYPE_GREET_SMS = 7;// 神聊打招呼消息 //早已废弃 2014.03.06
													// shc
	public static final int MSG_TYPE_SNS_SMS = 8;// 神聊sns匹配信息消息 //早已废弃
													// 2014.03.06 shc
	public static final int MSG_TYPE_ADD_FRIEND_SMS = 9;// 神聊添加好友消息
	public static final int MSG_TYPE_ADD_FRIEND_RE_SMS = 10;// 神聊添加好友结果消息
	public static final int MSG_TYPE_CONTACTS_SMS = 12;// 神聊通讯录匹配信息
	public static final int MSG_TYPE_BIG_FILE_SMS = 13;// 神聊大文件消息
	public static final int MSG_TYPE_SMS_GIF = 14;// 神聊GIF消息
	/** 被设置或取消群管理员 */
	public static final int MSG_TYPE_SET_GROUP_ADMIN = 41;
	/** 申请加入群 给管理员看的 */
	public static final int MSG_TYPE_REQUEST_JOIN_GROUP_4_ADMIN = 42;
	/** 申请加入群结果 给成员看的 */
	public static final int MSG_TYPE_REQUEST_JOIN_GROUP_4_MEMBER = 43;
	/** 群主解散群 */
	public static final int MSG_TYPE_DISMISS_GROUP = 44;
	/** 本人被T */
	public static final int MSG_TYPE_LEAVE = 45;
	/** 本人被拉进一个群 */
	public static final int MSG_TYPE_IN = 46;
	/** 自己的群被官方解散 */
	public static final int MSG_TYPE_DISMISS_4_CREATOR = 47;

	// 封设备 举报 封id 禁言 警告
	public static final int MSG_TYPE_MANAGER_WARN = 101; // 给用户看的，提示他被警告
	public static final int MSG_TYPE_MANAGER_REPORT_4_ADMIN = 103; // 给管理员处理的
	public static final int MSG_TYPE_MANAGER_SHUTUP_4_MEMBER = 104; // 给用户看的，提示他被禁言
	public static final int MSG_TYPE_MANAGER_SEAL_ID_4_MEMBER = 105; // 给用户看的，提示他被封ID
	public static final int MSG_TYPE_MANAGER_SEAL_MOBILE_4_MEMBER = 106; // 给用户看的，提示他被封设备
	public static final int MSG_TYPE_MANAGER_SHUTUP_4_MEMBER_OVER = 107; // 给用户看的，提示他已被解除禁言
	public static final int MSG_TYPE_MANAGER_SHUTUP_4_ADMIN = 108; // 给管理员看的，提示他处理了某人
	public static final int MSG_TYPE_MANAGER_SEAL_ID_4_ADMIN = 109; // 给管理员看的，提示他处理了某人
	public static final int MSG_TYPE_MANAGER_SEAL_MOBILE_4_ADMIN = 110; // 给管理员看的，提示他处理了某人
	public static final int MSG_TYPE_MANAGER_WARN_4_ADMIN = 111; // 给管理员看的，提示他处理了某人
	public static final int MSG_TYPE_MANAGER_SHUTUP_4_ADMIN_CLEAR = 112; // 给管理员看的，提示他处理了某人
	public static final int MSG_TYPE_MANAGER_NOTICE_BLOG_DELETE = 120; // 给用户看的，瞬间内容违规被删除

	public static final int MSG_TYPE_SMS_SMS = 30;// 系统普通消息
	public static final int MSG_TYPE_SMS_EMS = 31;// 系统彩信消息
	public static final int MSG_TYPE_SMS_AUDIO = 32;// 短信通道发送音频
	public static final int MSG_TYPE_SMS_IMG = 33;// 短信通道发送图片
	public static final int MSG_TYPE_SMS_CRAD = 34;// 短信通道发送名片
	public static final int MSG_TYPE_SMS_GEO = 35;// 短信通道发送地理位置
	public static final int MSG_TYPE_SMS_DRAFT = 36; // 系统短信草稿
	public static final int MSG_TYPE_MMS_DRAFT = 37; // 系统彩信草稿

	public static final int MSG_TYPE_QU_LASK_WEEK_STARTS_SMS = -29;// 显示本期之星的消息
																	// 2013.10.12
																	// shc

	public static final int MSG_TYPE_QU_COMMON_SMS = 20;// 群普通消息
	public static final int MSG_TYPE_QU_GEO_SMS = 21;// 群地理位置
	public static final int MSG_TYPE_QU_IMAGE_EMS = 22;// 群图片消息
	public static final int MSG_TYPE_QU_AUDIO_EMS = 23;// 群音频消息
	public static final int MSG_TYPE_QU_CARD_EMS = 24;// 群通讯录名片消息
	public static final int MSG_TYPE_QU_TCARD_SMS = 25;// 群神聊联系人名片消息
	public static final int MSG_TYPE_QU_NOTICE_SMS = 26;// 群公告信息消息
	public static final int MSG_TYPE_QU_PROMPT_SMS = 27;// 群提示信息消息
	public static final int MSG_TYPE_GEO_PROMPT = 28;// 聊天室里显示自己的位置
	public static final int MSG_TYPE_QU_BIG_FILE_SMS = 29;// 群大文件消息，其实是文本消息多了一个大文件的附件
	public static final int MSG_TYPE_QU_GIF_SMS = 50;// 群GIF消息
	// public static final int MSG_TYPE_FRIEND_SMS= 25;//好友打招呼消息
	// public static final int MSG_TYPE_PUSH_SMS= 26;//好友推送消息
	// tx 联系人类型 神聊联系人，陌生人联系人， 群，通讯录联系人
	// // public static final int TX_TYPE_CS=0;//通讯录联系人 // 这个概念被屏蔽,删除 2014.01.21
	// shc
	// public static final int TX_TYPE_ST=1;//陌生人联系人
	public static final int TX_TYPE_TB = 2;// 神聊联系人（应该就是神聊好友）
	// public static final int TX_TYPE_LB=3;//lbs联系人

	// tx 联系人类型 神聊联系人，陌生人联系人， 群，通讯录联系人
	public static final int TX_AUTH_NORMAL = 0;// 普通用户
	// public static final int TX_AUTH_UNKNOW=1;//未知
	// public static final int TX_AUTH_UNKNOW2=2;//未知
	public static final int TX_AUTH_OP = 3;// OP
	public static final int TX_AUTH_S_OP = 4;// 超级OP
	public static final int TX_AUTH_CLO_OP = 5;// 隐身op
	// qu
	public static final int QU_TYPE_DG = 3;// 群讨论组discussion group
	public static final int QU_TX_STATE_OWN = 0;// 群本人在群中状态群主
	public static final int QU_TX_STATE_GM = 1;// 群本人在群中状态管理
	public static final int QU_TX_STATE_CM = 2;// 群本人在群中状态成员
	public static final int QU_TX_STATE_OUT = 3;// 群本人在群中状态离开

	public static final int QU_GET_TYPE_PUBLIC = 101;// 公共
	public static final int QU_GET_TYPE_SEARCH = 102;// 搜索
	public static final int QU_GET_TYPE_OWN = 103;// 我的群

	// MsgStat 对话类型 神聊单人会话，神聊群聊会话，本地通讯录单人会话，本地通讯录多人会话
	public static final int MS_TYPE_CS = 0;// 本地通讯录单人会话
	public static final int MS_TYPE_MORE_CS = 1;// 本地通讯录多人会话
	public static final int MS_TYPE_TB = 2;// 神聊单人会话
	public static final int MS_TYPE_QU = 3;// 神聊群聊会话
	public static final int MS_TYPE_CHANNEL = 4; // 频道聊天室会话
													// //这个概念应该不存在了，是lbs聊天时代的概念
													// 2014.01.21 shc
	public static final int MS_TYPE_NOTICE = 5; // 系统通知类会话

	public static final int GROUP_TYPE_REQUEST = 0; // 半公开（私有群）
	public static final int GROUP_TYPE_PUBLIC = 1; // 公共聊天室，首页中除三个官方以外的聊天室
	public static final int GROUP_TYPE_SECRET = 2; // 私密群（应该不能被搜到，只能邀请别人加入）
	public static final int GROUP_TYPE_OFFICIAL = 3;// 三个官方聊天室

	/**
	 * Messages table 消息表
	 */
	public static final class Messages implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath("msgs").build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.messages";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.messages";
		public static final String MSG_TYPE = "msg_type";// 消息类型
		public static final String MSG_ID = "msg_id";// 消息id
		public static final String CONTACTS_PERSON_ID = "contacts_person_id";// 通讯录索引id
		public static final String CONTACTS_PERSON_NAME = "contacts_person_name";// 通讯录名称
		// public static final String SMS_ADDRESS="sms_address";//短信地址
		public static final String MSG_PARTNER_ID = "partner_id";// 聊天对象id or
																	// 群发送者id
		public static final String MSG_PARTNER_NAME = "partner_display_name";// 聊天对象名称
																				// or
																				// 群发送者名称
		public static final String MSG_PARTNER_PHONE = "partner_phone";// 聊天对象电话
		public static final String MSG_GROUP_ID = "group_id";// 群id a
		public static final String MSG_GROUP_NAME = "group_name";// 群名称
		public static final String MSG_GROUP_URL = "group_url";// 群显示头像
		public static final String MSG_SUBJECT = "msg_subject";// 主题
																// 神聊消息没有，短信有可能有
		public static final String MSG_SEX = "msg_sex";// 消息表用户性别
		public static final String MSG_BODY = "msg_body";// 消息正文
		public static final String MSG_PATH = "msg_path";// 消息附件本地路径
		public static final String MSG_URL = "msg_url";// 消息附件服务器路径
		public static final String MSG_FILE_LENGTH = "msg_file_length";// 附件大小
		public static final String AUD_TIMES = "audio_times";// 音频时长
		public static final String AUD_END = "audio_end";// 音频后缀
		public static final String GEO = "geo";// 地理位置
		public static final String TCARD_NAME = "tcard_name";// 名片姓名
		public static final String TCARD_ID = "tcard_id";// 名片神聊id
		public static final String TCARD_SIGN = "tcard_sign";// 名片签名
		public static final String TCARD_SEX = "tcard_sex";// 名片性别
		public static final String TCARD_PHONE = "tcard_phone";// 名片性别
		public static final String TCARD_AVATAR_URL = "tcard_avatar_url";// 头像
		public static final String AGREE = "agree";// 加好友结果
		public static final String AC = "ac";// 加好友验证码
		// public static final String SNS_TYPE = "sns_type";//sns 类型
		public static final String SNS_ID = "sns_id";// id
		public static final String SNS_NAME = "sns_name";// 名称
		public static final String WASME = "was_me";// 本人发送与否
		public static final String MSG_STATE = "msg_state"; // 消息状态
		public static final String READ_STATE = "read_state"; // 消息阅读状态
		public static final String UPDATE_STATE = "update_state"; // 消息附件的上传下载状态
		public static final String SEND_TIME = "send_time";// 发送时间
		public static final String CHANNEL_ID = "channel_id"; // 频道Id

		public static final String MSG_TYPE_2 = "msg_type_2"; // 举报类型
		public static final String REPORT_ID = "report_id"; // 举报人ID
		public static final String REPORT_NAME = "report_name"; // 举报人名字
		public static final String REPORT_CONTEXT = "report_context"; // 举报信息
		public static final String SHUTUP_ST = "shutup_st"; // 禁言开始时间
		public static final String SHUTUP_DU = "shutup_du"; // 禁言时长

		public static final String GMID = "gmid";
		public static final String GROUP_ID_NOTICE = "group_id_notice"; // 被操作的群ID
																		// 用于群组动态
		public static final String SN = "sn"; // sn 用于申请加入群
		public static final String RS = "rs"; // 申请理由
		public static final String OP = "op"; // 操作结果 1肯定0否定
		public static final String OP_ID = "op_id"; // 操作人id
		public static final String OP_NAME = "op_name"; // 操作人名字

		public static final String MSG_ID_2 = "msg_id_2"; // 操作人名字

		public static final String PRAISE_STATE = "praise_state";// -1:未操作，0:赞，1:取消赞
		public static final String FILE_DOWN_TIME = "file_down_time";// 附件第一次下载成功的时间

		public static final String DEFAULT_SORT_ORDER = SEND_TIME + " DESC";

		public static final String MSG_PKGID = "msg_pkgid";
		public static final String MSG_EMOD5 = "msg_emod5";// msg_emod5
		public static final String MSG_NUM = "msg_num";
		public static final String MSG_PKG_EMOMD5 = "msg_pkg_emomd5";
	}

	public static final class MsgStat implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath("msgstat").build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.msgstat";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.msgstat";
		public static final String MSG_TYPE = "msg_type";// 消息类型
		public static final String MSG_ID = "msg_id";// 消息id
		// MSGSTAT_TYPE和MSG_SESSION_ID是3.8.5新添加表字段
		public static final String MSG_STAT_TYPE = "msgstat_type";// 会话类型
		public static final String MSG_SESSION_ID = "msg_session_id";// 消息会话id，根据【消息类型】和【发送者id】生成的唯一id,避免混淆同一个人发送的单聊和群聊消息

		public static final String MSG_DATE = "msg_date";// 消息时间
		public static final String MSG_COUT = "message_count";// 消息总数
		public static final String CONTACTS_PERSON_ID = "contacts_person_id";// 通讯录索引id
		public static final String CONTACTS_PERSON_NAME = "contacts_person_name";
		public static final String MSG_PARTNER_ID = "t_partner_id";// 聊天对象id
																	// 或者群发送者id
		public static final String MSG_DISPLAY_NAME = "partner_display_name";// 聊天对象名称，或者群发送者名称
		public static final String MSG_GROUP_ID = "group_id";// 群id
		public static final String MSG_GROUP_NAME = "group_name";// 群名称
		public static final String MSG_GROUP_DISPLAY_AVATARS = "group_display_avatars";// 群显示的头像
		public static final String MSG_BODY = "msg_body";// 消息正文
		public static final String PHONE = "phone";// 手机
		public static final String WASME = "was_me";// 本人发送与否
		public static final String MSG_NOTREAD_COUT = "no_read";// 未读数
		public static final String READ_STATE = "read_state"; // 消息阅读状态
		public static final String CHANNEL_ID = "channel_id"; // 频道Id
		public static final String GROUP_ID_NOTICE = "group_id_notice"; // 被操作的群ID
																		// 用于群组动态
		public static final String GMID = "gmid"; // 被操作的群ID 用于群组动态
		public static final String DEFAULT_SORT_ORDER = MSG_DATE + " DESC";
	}

	/**
	 * Tx table 联系人表
	 */
	public static final class Tx implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath(SubDBHelper.TXS_TABLE_NAME).build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.txs";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.txs";

		public static final String TX_ID = "partner_id";// 神聊号
		public static final String DISPLAY_NAME = "display_name";// 神聊联系人昵称名称
		public static final String AVATAR_MD5 = "avatar_blob";// 头像变化标志
		public static final String AVATAR_URL = "avatar_url";// 头像网络路径
		public static final String SEX = "sex";// 性别
		public static final String BIRTHDAY = "birthday";// 生日
		public static final String BLOOD_TYPE = "blood_type";// 血型
		public static final String HOBBY = "hobby";// 爱好
		public static final String PROFESSION = "profession";// 职业
		public static final String HOME = "home";// 所在地
		public static final String TX_SIGN = "user_sign";// 个性签名
		public static final String DISTANCE = "distance";// 距离
		public static final String AGE = "age";// 年龄
		public static final String CONSTELLATION = "constellation";// 星座
		public static final String PHONE = "phone";// 手机号
		public static final String EMAIL = "email";// 邮箱
		public static final String IS_P_BIND = "is_phone_bind";// 手机号绑定
		public static final String IS_E_BIND = "is_email_bind";// 邮箱绑定

		// 下面几个字段（IS_STAR_FRIEND，BLACK_TIME,REMARK_NAME,TX_TYPE,CONTACTS_PERSON_NAME,MEDALS,GRADE）是为了兼容以前版本数据升级使用
		public static final String IS_STAR_FRIEND = "is_star_friend";// 是否是星标好友，
																		// 1：是，0:不是
		public static final String BLACK_TIME = "black_time"; // 拉黑时间
		public static final String REMARK_NAME = "remark_name"; // 备注名
		public static final String TX_TYPE = "tx_type";// 联系人类型
		public static final String CONTACTS_PERSON_NAME = "contacts_person_name";// 本地通讯录显示名称
		public static final String MEDALS = "medals"; // 勋章
		public static final String GRADE = "grade"; // 等级

		public static final String SECOND_CHAR = "second_char";// 神聊联系排序字符
		public static final String ALBUM = "album"; // 相册
		public static final String LANGUAGES = "languages";// 语言

		public static final String ISOP = "isop";// 是否是管理员
		public static final String ALBUM_VER = "album_ver"; // 相册版本
		public static final String INFO_VER = "info_ver"; // 信息版本
		public static final String IS_RECEIVE_REQ = "is_receive_req"; // 是否接收好友请求
		public static final String BLOG_INFOR = "blogInfor"; // 瞬间的属性信息（瞬间的总条数、总访问数、被喜欢总数等）
		public static final String LEVLE = "level"; // 等级

		// public static final String CONTACT_TIME="contact_time";//联系人建立时间
		// public static final String DEFAULT_SORT_ORDER = FIRST_CHAR+ " ASC";
		public static final String DEFAULT_SORT_ORDER = SECOND_CHAR + " ASC";// 用神聊联系人排序
																				// 2014.03.28
																				// shc
	}

	/**
	 * 好友联系人表
	 * 
	 * @author shc
	 */
	public static final class TX_Friends implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon()
				.appendPath(TxDBContentProvider.TX_FRIEND_TABLE_NAME).build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.friend";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.friend";

		public static final String TX_ID = "partner_id";// 神聊号
		public static final String TX_TYPE = "tx_type";// 联系人类型
		public static final String IS_STAR_FRIEND = "is_star_friend";// 是否是星标好友，
																		// 1：是，0:不是
		public static final String REMARK_NAME = "remark_name"; // 备注名
		public static final String CONTACTS_PERSON_NAME = "contacts_person_name";// 本地通讯录显示名称
		public static final String IN_BLACK_TIME = "in_black_time";// 拉入黑名单的时间

	}

	public static final class Qun implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath("qun").build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.qun";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.qun";
		public static final String QU_TYPE = "qu_type";// 群类型
		public static final String QU_ID = "qu_id";// 群号
		public static final String QU_VER = "qu_ver";// 群号
		public static final String QU_DISPLAY_NAME = "qu_display_name";// 显示名称
		public static final String QU_SIGN = "qu_sign";// 群简介 群有
		public static final String QU_OWN_ID = "qu_own_id";// 群主号
		public static final String QU_OWN_NAME = "qu_own_name";// 群主姓名
		public static final String QU_TIME = "qu_time";// 群创建时间
		public static final String QU_TX_STATE = "qu_tx_state";// b本人在群中的状态，0
																// 群主，1
																// 管家，2普通成员3 离开群
		public static final String QU_TX_IDS = "qu_tx_ids";// 对群联系人来说 群成员id
		public static final String ALL_NUM = "all_num"; // 总人数
		public static final String OL_NUM = "ol_num";// 在线人数

		public static final String QU_TX_ADMIN_IDS = "qu_tx_admin_ids";// 对群联系人来说
																		// 群成员id
		public static final String QU_TX_ADMIN_NAMES = "qu_tx_admin_names";// 对群联系人来说
																			// 群成员名字
		public static final String QU_AVATAR = "qu_avatar";// 群头像
		public static final String QU_TYPE_CHANNEL = "qu_type_channel";// 群类型，0为半公开，1为公开，2为私密
		public static final String QU_BULLETIN = "qu_bulletin";// 群公告
		public static final String QU_RCV_MSG = "qu_rcv_msg"; // 接收群消息
		public static final String QU_RCV_PUSH = "qu_rcv_push"; // 接收群提醒

		public static final String QU_SN = "qu_sn";// 创建群时才会用到

		public static final String QU_INDEX = "qu_index";
		public static final String QU_ACCESS_TIME = "qu_access_time";

		public static final String DEFAULT_SORT_ORDER = QU_DISPLAY_NAME
				+ " ASC";
	}

	/**
	 * Aid table 辅助表
	 */
	public static final class Aid implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath("aid").build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.aid";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.aid";
		public static final String UP_PHONES = "up_phones";// 上传的电话
		// public static final String PARTNER_ID="partner_id";//用户名
		// public static final String DRAFT = "draft";//草稿
		public static final String DEFAULT_SORT_ORDER = UP_PHONES + " ASC";
	}

	/**
	 * 被赞消息通知表
	 * 
	 * @author shc
	 */
	public static final class PraiseNotice implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon()
				.appendPath(TxDBContentProvider.NOTICE_PRAISE_TABLE_NAME)
				.build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.praise";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.praise";

		public static final String GROUP_ID = "groupId";// 聊天室id
		public static final String GMID = "gmid";// 聊天室消息id
		// public static final String PRAISE_FLAG="praise_flag";//
		// 0:赞，1:取消赞//TODO 被点赞表中不应该有此字段 2014.05.05
		public static final String ID_LIST = "idList"; // 点过赞的id集合
		public static final String TIME = "time";// 执行赞的最后时间

		public static final String DEFAULT_SORT_ORDER = GMID + " ASC";

	}

	/**
	 * 被喜欢消息通知表
	 * 
	 * @author shc
	 */
	public static final class LikeNotice implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon()
				.appendPath(TxDBContentProvider.NOTICE_LIKE_TABLE_NAME).build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.like";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.like";

		public static final String BLOG_ID = "blogId";// 瞬间id
		public static final String UID = "uid"; // 执行喜欢的用户uid
		public static final String TIME = "time";// 执行喜欢的最后时间

		public static final String DEFAULT_SORT_ORDER = TIME + " DESC";

	}

	/**
	 * 瞬间消息表
	 * 
	 * @author shc
	 */
	public static final class Blog implements BaseColumns {
		public static final Uri CONTENT_URI = TxDBContentProvider.CONTENT_URI
				.buildUpon().appendPath(TxDBContentProvider.BLOG_TABLE_NAME)
				.build();
		public static final String CONTENT_TYPE = "vnd.tuixin11.cursor.dir/vnd.com.tuixin11.smstx.blog";
		public static final String CONTENT_ITEM_TYPE = "vnd.tuixin11.cursor.item/vnd.com.tuixin11.smstx.blog";

		public static final String BLOG_ID = "blogId";// 瞬间id
		public static final String BLOG_PUBLISH_ID = "blogPublishId";// 瞬间发布id，避免瞬间重复发布
		public static final String PUBLISH_UID = "publishUid"; // 瞬间发布者ID
		public static final String BLOG_TEXT = "blogText"; // 瞬间文字信息
		public static final String BLOG_TYPE = "blogType"; // 瞬间信息的组合类型
		public static final String IMAGE_LOCAL_PATH = "imgLocalPath"; // 瞬间图片本地路径
		public static final String AUDIO_LOCAL_PATH = "audioLocalPath"; // 瞬间音频文件本地路径
		public static final String BLOG_MEDIA_INFOR = "blogMediaInfor";// 瞬间媒体信息
		public static final String PRAISED_COUNT = "praisedCount";// 瞬间被喜欢的总数
		public static final String PRAISED_ID_LIST = "praisedIdList";// 喜欢的用户id
		public static final String IS_DEL_BY_OP = "isDel";// 此瞬间是否被op删除
		public static final String LIKED_STATE = "likedState";// 瞬间被喜欢状态
		public static final String TIME = "time";// 瞬间发布时间（时间由服务器返回）

		public static final String DEFAULT_SORT_ORDER = TIME + " ASC";
	}

}
