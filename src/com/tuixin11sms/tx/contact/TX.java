package com.tuixin11sms.tx.contact;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;

public class TX implements Parcelable {
	private static final String TAG = "TX";
	public static final String EXTRA_TX = "tx";

	/** 特殊的神聊管理员号码 */
	public static final long TUIXIN_MAN = 9999999; // 神聊客服
	public static final long TUIXIN_FRIEND = 9999996; // 好友管家
	public static final long SL_SAFE = 9999993; // 神聊小卫士
	public static final long SL_GROUP_NOTICE = 9999992; // 群组动态

	public static final int MALE_SEX = 0;// 男
	public static final int FEMAL_SEX = 1;// 女
	public static final int DEFAULT_SEX = TX.FEMAL_SEX;// 默认性别设置为女吧 2014.01.20

	private final int LEVEL_TEXT = 1;// 发纯文字所需级别
	private final int LEVEL_AUDIO = 2;// 发语音所需级别
	private final int LEVEL_IMAGE = 3;// 发图片所需级别

	// private long _id;
	// private int tx_type;// tx_type == TxDB.TX_TYPE_TB貌似代表该tx为自己好友
	// 本地联系人和神聊共用字段
	private String phone;
	// 本地联系人字段
	// private int contacts_person_id;//应该是android联系人数据库中的主键“_id”字段，没有什么用，注掉
	// 2014.01.22 shc
	// private String contacts_person_name;// 应该是联系人在本地通讯录的名字？
	// private int phone_type;//电话类型,手机号，座机号 该字段应该没用了,注掉 2014.01.22
	// private String person_name_pinyin;
	// 神聊联系人基础字段
	public long partner_id;
	private String nick_name;// 昵称
	public int friend_ver;// 好友版本号
	private int avatar_ver;// 头像版本号
	public String avatar_url;
	public String sign;
	public String nick_name_pinyin;
	// public long contact_time;
	// public String user_name;
	public String area;
	private int sex;
	private String email;
	private boolean isPhoneBind;
	private boolean isEmailBind;
	// private int in_black_list = -1;// -1 正常，1你被对方加黑名单，0 你加对方入黑名单

	// private int starFriend = -1;// -1非好友，0普通好友，1星标好友

	// 神聊扩展字段
	/** 生日 */
	public int birthday;// 无生日是0，有生日则是19880101
	/* 血型 */
	public int bloodType = -1;
	/* 爱好 */
	public String hobby;
	/* 工作、职业 */
	public String job;
	// /* 备注名 */
	// private String remarkName;
	/* 是否是管理员 */
	public int isop;
	/** 等级 */
	private int level;
	// /* 勋章 */
	// private String medals;
	/* 相册 */
	private ArrayList<AlbumItem> album = new ArrayList<AlbumItem>();
	/* 语言 */
	private String languages;
	/* 相册版本 */
	private int albumVer;
	/* 个人信息版本号 */
	private int infoVer;
	/* 是否接收好友请求 */
	private boolean isReceiveReq;
	// /* 拉黑时间 */
	// private long inBlackTime;// 也许是被拉黑时的“当前时间毫秒值”
	/* 是否在线0不在线，1在线 */
	private int onLine = -1;
	public int distance;// 距离
	public int age;// 年龄
	public String constellation;// 星座

	// 以下字段不要了 2014.03.28 shc
	// public int group_ids;
	// public String group_names;
	// public String group_user_names;
	// public String group_user_signs;
	// 临时字段
	public int auth;//
	public String token;
	// public boolean deltx;// 是否为待删除tx
	private Bundle bundle;
	public boolean haveAlbum;// 是否有相册，看看谁再聊的搜索结果条目上需要显示该用户是否有相册

	private TxInfor txInfor = null;// TX的特殊信息

	// 瞬间信息
	public String blog_head_msg;

	public TX() {
		// this.tx_type = TxDB.TX_TYPE_ST;
		// this.contacts_person_id = Utils.DEFAULT_NUMBER;
		// this.contacts_person_name = "";
		this.partner_id = Utils.DEFAULT_NUMBER;
		this.setNick_name("");
		this.avatar_ver = 0;
		this.sex = TX.DEFAULT_SEX; // 默认女
		this.birthday = 0;
		this.hobby = "";
		this.job = "";
		this.area = "";
		this.distance = 0;// 距离
		this.age = 0;// 年龄
		this.constellation = "";// 星座
		// this.user_name = "";
		this.sign = "";
		this.phone = "";
		this.email = "";
		this.isPhoneBind = false;
		this.isEmailBind = false;
		// this.group_ids = Utils.DEFAULT_NUMBER;
		// this.group_names = "";
		// this.group_user_names = "";
		// this.group_user_signs = "";
		// this.person_name_pinyin = "";
		this.nick_name_pinyin = "";
		// this.phone_type = 0;
		// this.contact_time = 0;
		this.avatar_url = "";
		this.blog_head_msg = "";
		this.level = 0;
		// this.in_black_list = -1;
		// this.starFriend = -1;
		bundle = new Bundle();
	}

	// public long get_id() {
	// return _id;
	// }

	// public void set_id(long _id) {
	// this._id = _id;
	// }

	//
	// public int getContacts_person_id() {
	// return contacts_person_id;
	// }
	//
	// public void setContacts_person_id(int contacts_person_id) {
	// this.contacts_person_id = contacts_person_id;
	// }

	// public String getContacts_person_name() {
	// return contacts_person_name;
	// }
	//
	// public void setContacts_person_name(String contacts_person_name) {
	// if (contacts_person_name == null)
	// contacts_person_name = "";
	// this.contacts_person_name = contacts_person_name;
	// this.setPerson_name_pinyin(CnToSpell.getFullSpell(contacts_person_name));
	// }

	public String getNick_name() {
		return nick_name;
	}

	public String getBlog_head_msg() {
		return blog_head_msg == null ? "" : blog_head_msg;
	}

	public void setBlog_head_msg(String blog_head_msg) {
		this.blog_head_msg = blog_head_msg;
	}

	public void setNick_name(String nick_name) {
		if (nick_name == null)
			nick_name = "";
		this.nick_name = nick_name;
		this.nick_name_pinyin = CnToSpell.getFullSpell(nick_name);
	}

	public String getNick_name_pinyin() {
		return nick_name_pinyin;
	}

	public String getPhone() {
		return phone;
	}

	// public int getTx_type() {
	// return tx_type;
	// }
	//
	// public void setTx_type(int tx_type) {
	// this.tx_type = tx_type;
	// }

	// public int getPhone_type() {
	// return phone_type;
	// }
	//
	// public void setPhone_type(int phone_type) {
	// this.phone_type = phone_type;
	// }

	// public String getPerson_name_pinyin() {
	// return person_name_pinyin;
	// }
	//
	// public void setPerson_name_pinyin(String person_name_pinyin) {
	// if (person_name_pinyin == null)
	// person_name_pinyin = "";
	// this.person_name_pinyin = person_name_pinyin;
	// }

	// public int getIn_black_list() {
	// return in_black_list;
	// }
	//
	// public void setIn_black_list(int in_black_list) {
	// this.in_black_list = in_black_list;
	// }

	public long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(long partner_id) {
		this.partner_id = partner_id;
	}

	public String getAvatar_url() {
		return avatar_url;
	}

	public void setAvatar_url(String avatar_url) {
		if (avatar_url == null)
			avatar_url = "";
		this.avatar_url = avatar_url;
	}

	// 上面的get、set方法都是新添加的，为了有一天能把所有字段都设置成private类型,慢慢替换吧 2013.10.17 shc
	public void setSign(String sign) {
		if (sign == null)
			sign = "";
		this.sign = sign.trim();
	}

	public String getSign() {
		return sign;
	}

	public void setPartnerId(long partner_id) {
		// if (Utils.isIdValid(partner_id)) {
		this.partner_id = partner_id;
		// }
	}

	public int getIsop() {
		return isop;
	}

	public void setIsop(int isop) {
		this.isop = isop;
	}

	public int getAvatar_ver() {
		return avatar_ver;
	}

	public void setAvatar_ver(int avatar_ver) {
		this.avatar_ver = avatar_ver;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public void setBirthday(int birthday) {
		this.birthday = birthday;
	}

	public int getBirthday() {
		return birthday;
	}

	public void setBloodType(int blood_type) {
		this.bloodType = blood_type;
	}

	public int getBloodType() {
		return bloodType;
	}

	public String getHobby() {
		return hobby;
	}

	public void setHobby(String hobby) {
		if (hobby == null)
			hobby = "";
		this.hobby = hobby;
	}

	public String getJob() {
		return job;
	}

	public void setJob(String job) {
		if (job == null)
			job = "";
		this.job = job;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getFriend_ver() {
		return friend_ver;
	}

	public void setFriend_ver(int friend_ver) {
		this.friend_ver = friend_ver;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getLanguages() {
		return languages;
	}

	public void setLanguages(String languages) {
		this.languages = languages;
	}

	public String getRemarkName() {
		return getTxInfor().getRemarkName();
	}

	public void setRemarkName(String remarkName) {
		this.getTxInfor().setRemarkName(remarkName);
	}

	//
	// public String getMedals() {
	// return medals;
	// }
	//
	// public void setMedals(String medals) {
	// this.medals = medals;
	// }
	//
	// public int getGrade() {
	// return grade;
	// }
	//
	// public void setGrade(int grade) {
	// this.grade = grade;
	// }

	public void setConstellation(String constellation) {
		if (constellation == null)
			constellation = "";
		this.constellation = constellation;
	}

	public void setPhone(String phone) {
		if (phone == null)
			phone = "";
		this.phone = phone;
	}

	public void setArea(String area) {
		if (area == null)
			area = "";
		this.area = area;
	}

	public String getArea() {
		return area;
	}

	public String getEmail() {
		return email;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setEmail(String email) {
		if (email == null)
			email = "";
		this.email = email;
	}

	public boolean isEmailBind() {
		return isEmailBind;
	}

	public void setEmailBind(boolean isEmailBind) {
		this.isEmailBind = isEmailBind;
	}

	public boolean isPhoneBind() {
		return isPhoneBind;
	}

	public void setPhoneBind(boolean isPhoneBind) {
		this.isPhoneBind = isPhoneBind;
	}

	public void setNickNamePinyin(String nick_name_pinyin) {
		if (nick_name_pinyin == null)
			nick_name_pinyin = "";
		this.nick_name_pinyin = nick_name_pinyin;
	}

	// public int getStarFriend() {
	// return starFriend;
	// }
	//
	// public void setStarFriend(int starFriend) {
	// this.starFriend = starFriend;
	// }

	@Override
	public String toString() {
		return "TX [phone=" + phone + ", partner_id=" + partner_id
				+ ", nick_name=" + nick_name + ", friend_ver=" + friend_ver
				+ ", avatar_ver=" + avatar_ver + ", avatar_url=" + avatar_url
				+ ", sign=" + sign + ", nick_name_pinyin=" + nick_name_pinyin
				+ ", area=" + area + ", sex=" + sex + ", email=" + email
				+ ", isPhoneBind=" + isPhoneBind + ", isEmailBind="
				+ isEmailBind + ", birthday=" + birthday + ", bloodType="
				+ bloodType + ", hobby=" + hobby + ", job=" + job + ", isop="
				+ isop + ", level=" + level + ", album=" + album
				+ ", languages=" + languages + ", albumVer=" + albumVer
				+ ", infoVer=" + infoVer + ", isReceiveReq=" + isReceiveReq
				+ ", onLine=" + onLine + ", distance=" + distance + ", age="
				+ age + ", constellation=" + constellation + ", auth=" + auth
				+ ", token=" + token + ", bundle=" + bundle + ", haveAlbum="
				+ haveAlbum + ", txInfor=" + txInfor + ", blog_head_msg="
				+ blog_head_msg + "]";
	}

	public static final Parcelable.Creator<TX> CREATOR = new Parcelable.Creator<TX>() {
		public TX createFromParcel(Parcel in) {
			return new TX(in);
		}

		public TX[] newArray(int size) {
			return new TX[size];
		}
	};

	private TX(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		// out.writeInt(tx_type);
		// out.writeInt(contacts_person_id);
		// out.writeString(contacts_person_name);
		out.writeLong(partner_id);
		out.writeString(nick_name);
		out.writeInt(avatar_ver);
		out.writeString(avatar_url);
		// out.writeInt(phone_type);
		// out.writeString(person_name_pinyin);
		out.writeString(nick_name_pinyin);
		out.writeInt(sex);
		out.writeInt(birthday);
		out.writeInt(bloodType);
		out.writeString(hobby);
		out.writeString(job);
		out.writeString(area);
		out.writeInt(distance);
		out.writeInt(age);
		out.writeString(constellation);
		// out.writeString(user_name);
		out.writeString(sign);
		out.writeString(phone);
		out.writeString(email);
		boolean[] bool = new boolean[2];
		bool[0] = isPhoneBind;
		bool[1] = isEmailBind;
		out.writeBooleanArray(bool);
		// out.writeInt(in_black_list);
		// out.writeInt(group_ids);
		// out.writeString(group_names);
		// out.writeString(group_user_names);
		// out.writeString(group_user_signs);
		// out.writeLong(contact_time);
		out.writeList(album);
		out.writeInt(albumVer);
		out.writeString(languages);
		// out.writeLong(inBlackTime);
		boolean[] irq = new boolean[1];
		irq[0] = isReceiveReq;
		out.writeBooleanArray(irq);
		out.writeInt(infoVer);
		out.writeInt(onLine);
		out.writeInt(isop);

		// out.writeString(remarkName);
		out.writeInt(level);
	}

	@SuppressWarnings("unchecked")
	public void readFromParcel(Parcel in) {
		// tx_type = in.readInt();
		// contacts_person_id = in.readInt();
		// contacts_person_name = in.readString();
		partner_id = in.readLong();
		nick_name = in.readString();
		avatar_ver = in.readInt();
		avatar_url = in.readString();
		// phone_type = in.readInt();
		// person_name_pinyin = in.readString();
		nick_name_pinyin = in.readString();
		sex = in.readInt();
		birthday = in.readInt();
		bloodType = in.readInt();
		hobby = in.readString();
		job = in.readString();
		area = in.readString();
		distance = in.readInt();
		age = in.readInt();
		constellation = in.readString();
		// user_name = in.readString();
		sign = in.readString();
		phone = in.readString();
		email = in.readString();
		boolean[] bool = new boolean[2];
		in.readBooleanArray(bool);
		isPhoneBind = bool[0];
		isEmailBind = bool[1];
		// in_black_list = in.readInt();
		// group_ids = in.readInt();
		// group_names = in.readString();
		// group_user_names = in.readString();
		// group_user_signs = in.readString();
		// contact_time = in.readLong();
		album = in.readArrayList(AlbumItem.class.getClassLoader());
		albumVer = in.readInt();
		languages = in.readString();
		// inBlackTime = in.readLong();
		boolean[] irq = new boolean[1];
		in.readBooleanArray(irq);
		isReceiveReq = irq[0];
		infoVer = in.readInt();
		onLine = in.readInt();
		isop = in.readInt();
		// remarkName = in.readString();
		level = in.readInt();

	}

	// TODO 此方法待删除 2014.04.02 shc
	// public Bundle getBundle() {
	// return buildBundle();
	// }

	// TODO 此方法待删除 2014.04.02 shc
	// /** 重新生成一下bundle，防止直接给字段赋值时，bundle中少信息 */
	// private Bundle buildBundle() {
	// if (bundle == null) {
	// bundle = new Bundle();
	// }
	// bundle.putInt("isop", isop);
	// bundle.putString("user_sign", sign);
	// // bundle.putInt("tx_type", tx_type);
	// // bundle.putInt("contacts_person_id", contacts_person_id);
	// // bundle.putString("contacts_person_name", contacts_person_name);
	// // bundle.putString("person_name_pinyin", person_name_pinyin);
	// bundle.putLong("partner_id", partner_id);
	// bundle.putString("nick_name", nick_name);
	// bundle.putString("nick_name_pinyin", nick_name_pinyin);
	// bundle.putInt("sex", sex);
	// bundle.putInt("birthday", birthday);
	// bundle.putInt("blood_type", bloodType);
	// bundle.putString("hobby", hobby);
	// bundle.putString("job", job);
	// bundle.putInt("distance", distance);
	// bundle.putInt("age", age);
	// bundle.putString("lang", languages);
	// // bundle.putString("remarkName", remarkName);
	// // bundle.putInt("in_black_list", in_black_list);
	// bundle.putString("constellation", constellation);
	// bundle.putString("phone", phone);
	// bundle.putString("area", area);
	// bundle.putString("email", email);
	// bundle.putBoolean("is_phone_bind", isPhoneBind);
	// bundle.putBoolean("is_email_bind", isEmailBind);
	// bundle.putInt("avatar_ver", avatar_ver);
	// bundle.putString("avatar_url", avatar_url);
	// // bundle.putInt("starFriend", starFriend);
	// {
	// // 只有set方法，但是没有再bundle中添加的字段，
	// // online
	// // medals
	// // grade
	// // infoVer
	// // isReceiveReq
	// // 至少有这5个
	// //
	//
	// }
	// return bundle;
	// }

	// public void setBundle(Bundle bundle) {
	// this.bundle = bundle;
	// if (bundle != null) {
	// setSign(bundle.getString("user_sign"));
	// setPartnerId(bundle.getLong("partner_id"));
	// setNick_name(bundle.getString("nick_name"));
	// setEmail(bundle.getString("email"));
	// setPhoneBind(bundle.getBoolean("is_phone_bind"));
	// setEmailBind(bundle.getBoolean("is_email_bind"));
	// setArea(bundle.getString("area"));
	// setSex(bundle.getInt("sex", TX.DEFAULT_SEX));// 添加默认值为女
	// setAvatar_url(bundle.getString("avatar_url"));
	// setAvatar_ver(bundle.getInt("avatar_ver"));
	// setBirthday(bundle.getInt("birthday"));
	// setBloodType(bundle.getInt("blood_type"));
	// setHobby(bundle.getString("hobby"));
	// setJob(bundle.getString("job"));
	// // setTx_type(bundle.getInt("tx_type"));
	// setPhone(bundle.getString("phone"));
	// if (Utils.debug) {
	// Log.e(TAG,
	// "是否含有is_black_list字段："
	// + bundle.containsKey("in_black_list"));
	// Log.e(TAG,
	// "在TX(" + bundle.getLong("partner_id")
	// + ")的setBundle中取出是否黑名单字段的值："
	// + bundle.getInt("in_black_list"));
	// }
	// // setIn_black_list(bundle.getInt("in_black_list", -1));// 添加默认值为-1
	// // setStarFriend(bundle.getInt("starFriend", -1));
	// setLanguages(bundle.getString("lang"));
	// // setRemarkName(bundle.getString("remarkName"));
	// setIsop(bundle.getInt("isop"));
	// {
	// // 以下原来没有，为了和buildBundle保持一致，把变量赋值 2013.09.25 shc
	// // setContacts_person_id(bundle.getInt("contacts_person_id"));
	// // setContacts_person_name(bundle
	// // .getString("contacts_person_name"));
	// setNickNamePinyin(bundle.getString("nick_name_pinyin"));
	// setDistance(bundle.getInt("distance"));
	// setAge(bundle.getInt("age"));
	// setConstellation(bundle.getString("constellation"));
	// }
	//
	// }
	// }

	@Override
	public int hashCode() {
		return Long.valueOf(partner_id).hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof TX)) {
			return false;
		}
		TX tx = (TX) obj;
		if (partner_id != tx.partner_id) {
			return false;
		}
		return true;
	}

	// public static List<TX> listUniq(List<TX> list) {
	// Set<TX> set = new LinkedHashSet<TX>();
	// set.addAll(list);
	// List<TX> newlist = new ArrayList<TX>();
	// newlist.addAll(set);
	// return newlist;
	// }

	public ArrayList<AlbumItem> getAlbum() {
		return album;
	}

	// 返回相册数组，存到数据库中时用
	public String getAlubumString() {
		if (album != null && !album.isEmpty()) {
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < album.size(); i++) {
				sb.append(album.get(i).getUrl());
				if (i != album.size() - 1) {
					sb.append(Constants.STRING_SEPARATOR);
				}
			}
			return sb.toString();
		}
		return "";
	}

	public void setAlbum(ArrayList<AlbumItem> album) {
		this.album = album;
	}

	public int getAlbumVer() {
		return albumVer;
	}

	public void setAlbumVer(int albumVer) {
		this.albumVer = albumVer;
	}

	public int getInfoVer() {
		return infoVer;
	}

	public void setInfoVer(int infoVer) {
		this.infoVer = infoVer;
	}

	public boolean isReceiveReq() {
		return isReceiveReq;
	}

	public void setReceiveReq(boolean isReceiveReq) {
		this.isReceiveReq = isReceiveReq;
	}

	// public long getInBlackTime() {
	// return inBlackTime;
	// }
	//
	// public void setInBlackTime(long inBlackTime) {
	// this.inBlackTime = inBlackTime;
	// }

	public int getOnLine() {
		return onLine;
	}

	public void setOnLine(int onLine) {
		this.onLine = onLine;
	}

	/** 可以发送文本消息 */
	public boolean isCanSendText() {
		return (level - LEVEL_TEXT) >= 0;
	}

	/** 可以发送语音消息 */
	public boolean isCanSendAudio() {
		return (level - LEVEL_AUDIO) >= 0;
	}

	/** 可以发送图片消息 */
	public boolean isCanSendImg() {
		return (level - LEVEL_IMAGE) >= 0;
	}

	/**是否显示等级标记*/
	public boolean isDispalyLevel() {
		return !(level>=TUIXIN_MAN); 
	}
	
	
	public TxInfor getTxInfor() {
		if (txInfor == null) {
			// 如果txInfor为空，则返回一个初始化的TxInfor对象
			return new TxInfor(partner_id, TxInfor.TX_TYPE_DEFAULT);
		}
		return txInfor;
	}

	public void setTxInfor(TxInfor txInfor) {
		this.txInfor = txInfor;
	}

	public static SessionManager.TXManager tm = null;

	/** TX管理者 */
	// public static class TXManager {
	//
	// // TX 陌生神聊用户, 最多1000人
	// private LruCache<Long, TX> mSTTXCache = new LruCache<Long, TX>(1000);
	// // // 好友id集合，key是好友id,value是是否星标好友标记
	// // private Map<Long, Integer> mTBTXIds = new HashMap<Long, Integer>();
	// // 好友备注名集合，key是好友id,value是好友备注名
	// // private Map<Long, String> mTBTXRemarkNames = new HashMap<Long,
	// // String>();
	// // // 存储神聊好友，不限长度
	// // private HashMap<Long, TX> mTBTXCache = new HashMap<Long, TX>();
	// // 存储黑名单用户，不限长度
	// // private HashMap<Long, TxInfor> mBlackTXCache = new HashMap<Long,
	// // TxInfor>();
	// // 存储手机本地通讯录联系人，不限长度,key为phone
	// private HashMap<Long, String> mCSCache = new HashMap<Long, String>();
	//
	// // 存储所有与用户相关的信息对象
	// private HashMap<Long, TxInfor> mTxInforCacheMap = new HashMap<Long,
	// TxInfor>();
	// // // 存储所有好友对象
	// // private HashMap<Long, TxInfor> mTBTXCache = new HashMap<Long,
	// // TxInfor>();
	//
	// private static AtomicInteger blackListPageNum = new AtomicInteger(0);
	//
	// private Context mContext = null;
	// private ContentResolver mTxCr = null;
	// // private SharedPreferences prefs=null;
	//
	// private LoginSessionManager mSess = null;
	//
	// private static TX tx_man;// 神聊管家
	// private static TX tx_friend;// 好友管家
	// private static TX sl_notice;// 群组动态
	// private static TX sl_safe;// 神聊小卫士
	// private static TX tx_me;// 自身资料
	//
	// public void setContext(Context context) {
	// if (mContext == null) {
	// this.mContext = context;
	// this.mTxCr = context.getContentResolver();
	// // this.prefs =
	// // context.getSharedPreferences(PrefsMeme.MEME_PREFS,
	// // Context.MODE_PRIVATE);
	// mSess = LoginSessionManager.getManager(context);
	// }
	// }
	//
	// public void initManager() {
	//
	// clearTXCache();
	//
	// if (mCSCache.isEmpty()) {
	// ContactAPI conApi = new ContactAPISdk5();
	// conApi.fillAllContacts(mTxCr, mCSCache);
	// }
	//
	// // 读取神聊数据库神聊好友
	// if (Utils.debug)
	// Log.e(TAG, "开始读取神聊数据库中的神聊好友");
	// // ArrayList<TX> tem = new ArrayList<TX>();
	// // Cursor cur = mTxCr.query(TxDB.Tx.CONTENT_URI, null,
	// // TxDB.Tx.TX_TYPE
	// // + "=?", new String[] { "" + TxDB.TX_TYPE_TB }, null);
	// // if (cur != null) {
	// // tem = fetchAllContacts(cur);
	// // cur.close();
	// // if (tem != null) {
	// // if (Utils.debug)
	// // Log.e(TAG, "从数据库中查到的好友总数为：" + tem.size());
	// // for (TX tx : tem) {
	// // // 添加到好友缓存中
	// // mTBTXCache.put(tx.partner_id, tx);
	// // addTBTXId(tx.partner_id, tx.getStarFriend());
	// // }
	// // } else {
	// // if (Utils.debug)
	// // Log.e(TAG, "查找到的好友集合为空");
	// // }
	// // } else {
	// // if (Utils.debug)
	// // Log.e(TAG, "查找好友的游标为空！！！太恐怖了！！！");
	// // // 这时如果再调用cur.moveToNext()不会异常，FutureTask给捕获了。
	// // }
	// ArrayList<TxInfor> tem = new ArrayList<TxInfor>();
	// Cursor cur = mTxCr.query(TxDB.TX_Friends.CONTENT_URI, null, null,
	// null, null);
	// if (cur != null) {
	// tem = fetchAllTxInfors(cur);
	// cur.close();
	// if (tem != null) {
	// if (Utils.debug)
	// Log.e(TAG, "从数据库中查到的好友总数为：" + tem.size());
	// TX txx = null;
	// for (TxInfor tinfor : tem) {
	// // 添加到好友缓存中
	// // mTBTXCache.put(tx.partner_id, tx);
	// // addTBTXId(tinfor.getPartner_id(),
	// // tinfor.getStarFriend());
	// // mTBTXCache.put(tinfor.getPartner_id(), tinfor);
	// mTxInforCacheMap.put(tinfor.getPartner_id(), tinfor);
	// txx = findTXFromDB(tinfor.getPartner_id());
	// if (txx != null) {
	// txx.setTxInfor(tinfor);
	// addSTTX(txx);
	// // tinfor.setTxNormal(txx);
	// }
	// }
	// } else {
	// if (Utils.debug)
	// Log.e(TAG, "查找到的好友集合为空");
	// }
	// } else {
	// if (Utils.debug)
	// Log.e(TAG, "查找好友的游标为空！！！太恐怖了！！！");
	// // 这时如果再调用cur.moveToNext()不会异常，FutureTask给捕获了。
	// }
	//
	// getLocalGroups();
	// }
	//
	// public int getBlackListPageNum() {
	// return blackListPageNum.getAndIncrement();
	// }
	//
	// public void resetBlackListPageNum() {
	// blackListPageNum.set(0);
	// }
	//
	// /** 清空存储所有与当前账号相关的神聊用户cache */
	// public void clearTXCache() {
	// mSTTXCache.evictAll();
	// // mTBTXCache.clear();
	// // if (mTBTXIds != null) {
	// // mTBTXIds.clear();
	// // }
	// mTxInforCacheMap.clear();
	// // mBlackTXCache.clear();
	// // mTBTXCache.clear();
	//
	// }
	//
	// private ArrayList<TxGroup> getLocalGroups() {
	// ArrayList<TxGroup> tmp = new ArrayList<TxGroup>();
	// ArrayList<TxGroup> tm = new ArrayList<TxGroup>();
	// Cursor cur = mTxCr.query(TxDB.Qun.CONTENT_URI, null,
	// TxDB.Qun.QU_TX_STATE + " <> ? ",
	// new String[] { String.valueOf(TxDB.QU_TX_STATE_OUT) },
	// TxDB.Qun.QU_ID);
	// if (cur != null) {
	// tmp = TxGroup.fetchAllDBGroups(cur);
	// cur.close();
	// }
	// for (TxGroup txgroup : tmp) {
	// if (Utils.debug)
	// Log.i(TAG, "getLocalGroups" + txgroup.toString());
	// boolean add = true;
	// for (TxGroup txgroup1 : tm) {
	// if (txgroup1.group_id == txgroup.group_id) {
	// add = false;
	// break;
	// }
	// }
	// if (add)
	// tm.add(txgroup);
	// }
	// return tm;
	// }
	//
	// // 邀请手机好友的acivity(InviteContactsActivity)需要
	// public final HashMap<Long, TX> getTBTXCache() {
	// HashMap<Long, TX> txcache = new HashMap<Long, TX>();
	// // Set<Entry<Long, TxInfor>> tinforSet = mTBTXCache.entrySet();
	// // Iterator<Entry<Long, TxInfor>> tinforIt = tinforSet.iterator();
	// // while (tinforIt.hasNext()) {
	// // Entry<Long, TxInfor> tbEntry = tinforIt.next();
	// // TxInfor tbtx = tbEntry.getValue();
	// // if (tbtx.getTx_type() == TxInfor.TX_TYPE_TB) {
	// // // 神聊好友
	// // txcache.put(tbtx.getPartner_id(), tbtx.getTxNormal());
	// // }
	// // }
	//
	// Set<Entry<Long, TxInfor>> tinforSet = mTxInforCacheMap.entrySet();
	// Iterator<Entry<Long, TxInfor>> tinforIt = tinforSet.iterator();
	// while (tinforIt.hasNext()) {
	// Entry<Long, TxInfor> tbEntry = tinforIt.next();
	// TxInfor tbtx = tbEntry.getValue();
	// if (!tbtx.isBlackType()&&tbtx.isTBType()) {
	// // 神聊好友
	// TX tx = getTx(tbEntry.getKey());
	// if (tx != null) {
	// // 神聊好友
	// txcache.put(tx.partner_id, tx);
	// }
	// }
	// }
	//
	// // Set<Long> tinforSet = mTxInforCacheMap.keySet();
	// // Iterator<Long> tinforIt = tinforSet.iterator();
	// // while (tinforIt.hasNext()) {
	// // Long tbEntry = tinforIt.next();
	// // TX tx = getTx(tbEntry);
	// // if (tx!=null) {
	// // // 神聊好友
	// // txcache.put(tx.partner_id, tx);
	// // }
	// // }
	// return txcache;
	// }
	//
	// public final HashMap<Long, String> getContactsCache() {
	// return mCSCache;
	// }
	//
	// // 同步mCSCache和神聊好友的本地联系人字段
	// public void syncTBTXs() {
	// // Set<Entry<Long, TxInfor>> tbSet = mTBTXCache.entrySet();
	// // Iterator<Entry<Long, TxInfor>> tbIter = tbSet.iterator();
	// // while (tbIter.hasNext()) {
	// // Entry<Long, TxInfor> tbEntry = tbIter.next();
	// // TxInfor tbtx = tbEntry.getValue();
	// // if (tbtx.getTxNormal() == null) {
	// // tbtx.setTxNormal(getTx(tbtx.getPartner_id()));
	// // }
	// // Long tbPhone = Long.parseLong(tbtx.getTxNormal().getPhone());
	// // String csName = mCSCache.get(tbPhone);
	// // if (!TextUtils.isEmpty(csName)) {
	// // // 这样赋值有效，tbtx取到的是引用，不是拷贝 2014.01.21 shc
	// // tbtx.setContacts_person_name(csName);
	// // }
	// // }
	// Set<Long> tbSet = mTxInforCacheMap.keySet();
	// Iterator<Long> tbIter = tbSet.iterator();
	// TX tx = null;
	// while (tbIter.hasNext()) {
	// Long tinforId = tbIter.next();
	// if ((tx = getTx(tinforId)) != null) {
	// if (!TextUtils.isEmpty(tx.getPhone())) {
	// // 电话号码不为空
	// Long tbPhone = Long.parseLong(tx.getPhone());
	// String csName = mCSCache.get(tbPhone);
	// if (!TextUtils.isEmpty(csName)) {
	// // 这样赋值有效，tbtx取到的是引用，不是拷贝 2014.01.21 shc
	// mTxInforCacheMap.get(tinforId)
	// .setContacts_person_name(csName);
	// }
	// }
	// }
	// }
	// }
	//
	// // /** 添加好友id,默认为非星标好友 */
	// // public void addTBTXId(long partner_id) {
	// // mTBTXIds.put(partner_id, TxDB.TX_COMMON_FRIEND);
	// // }
	//
	// /** 添加好友id */
	// // 好友id集合应该只保存在内存中，不与数据库相关
	// public void addTBTXId(long partner_id, int isStarFriend) {
	// // mTBTXIds.put(partner_id, isStarFriend);
	// TxInfor tinfor = null;
	// if (!mTxInforCacheMap.containsKey(partner_id)) {
	// tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
	// tinfor.setStarFriend(isStarFriend);
	// // tinfor.setTx_type(TxInfor.TX_TYPE_TB);
	// mTxInforCacheMap.put(partner_id, tinfor);
	// // mTxInforCacheMap.put(partner_id, tinfor);
	//
	// ContentValues values = tinfor.txinforToValues();
	// if (updateTxInforByTXId(values, partner_id) == 0) {
	// mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// }
	//
	// } else {
	// tinfor = mTxInforCacheMap.get(partner_id);
	// if (tinfor != null) {
	// tinfor.setStarFriend(isStarFriend);
	//
	// ContentValues values = new ContentValues();
	// values.put(TxDB.TX_Friends.IS_STAR_FRIEND, isStarFriend);
	// if (updateTxInforByTXId(values, partner_id) == 0) {
	// mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// }
	// }
	// }
	//
	// TX tx = getTx(partner_id);
	// if (tx != null) {
	// tx.setTxInfor(tinfor);
	// }
	//
	// }
	//
	// /** 添加好友备注名 */
	// // 先取好友备注名，后取好友详细资料
	// public void addTBTXRemarkName(long partner_id, String remarkName) {
	//
	// TxInfor tinfor = null;
	// if (!mTxInforCacheMap.containsKey(partner_id)) {
	// tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_TB);
	// // tinfor.setStarFriend(TxInfor.TX_COMMON_FRIEND);
	// tinfor.setRemarkName(remarkName);
	// // tinfor.setTx_type(TxInfor.TX_TYPE_TB);
	// mTxInforCacheMap.put(partner_id, tinfor);
	// // mTxInforCacheMap.put(partner_id, tinfor);
	//
	// ContentValues values = tinfor.txinforToValues();
	// if (updateTxInforByTXId(values, partner_id) == 0) {
	// mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// }
	//
	// } else {
	// tinfor = mTxInforCacheMap.get(partner_id);
	// if (tinfor != null) {
	// tinfor.setRemarkName(remarkName);
	//
	// ContentValues values = new ContentValues();
	// values.put(TxDB.TX_Friends.REMARK_NAME, remarkName);
	// if (updateTxInforByTXId(values, partner_id) == 0) {
	// mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// }
	// }
	// }
	//
	// TX tx = getTx(partner_id);
	// if (tx != null) {
	// tx.setTxInfor(tinfor);
	// }
	//
	// }
	//
	// // /** 获取好友备注名 */
	// // private String getTBTXRemarkName(long partner_id) {
	// // return mTBTXRemarkNames.get(partner_id);
	// // }
	// //
	// // private boolean addTBTX(TX tx) {
	// // if (!isTxFriend(tx.partner_id)) {
	// // // 必须在id列表中存在，否则认为不是好友
	// // return false;
	// // }
	// // if (!Utils.isIdValid(tx.partner_id)
	// // || (tx.partner_id == TX.TUIXIN_MAN
	// // || tx.partner_id == TX.TUIXIN_FRIEND || tx.partner_id ==
	// // getUserID())) {
	// // // 联系人类型必须是“神聊联系人”
	// // return false;
	// // }
	// //
	// // if (!TextUtils.isEmpty(tx.getPhone())) {
	// // String person_name = mCSCache.get(tx.getPhone());
	// // tx.setContacts_person_name(person_name);
	// // }
	// //
	// // return true;
	// // }
	//
	// // private boolean addTBTX(TX tx) {
	// // if (!isTxFriend(tx.partner_id)) {
	// // // 必须在id列表中存在，否则认为不是好友
	// // return false;
	// // }
	// // if (!Utils.isIdValid(tx.partner_id)
	// // || (tx.partner_id == TX.TUIXIN_MAN
	// // || tx.partner_id == TX.TUIXIN_FRIEND || tx.partner_id ==
	// // getUserID())) {
	// // // 联系人类型必须是“神聊联系人”
	// // return false;
	// // }
	// //
	// // if (!TextUtils.isEmpty(tx.getPhone())) {
	// // String person_name = mCSCache.get(tx.getPhone());
	// // tx.setContacts_person_name(person_name);
	// // }
	// //
	// // // TxInfor tinfor = null;
	// // // if (!mTxInforCache.containsKey(tx.partner_id)) {
	// // // tinfor = new TxInfor();
	// // // tinfor.setPartner_id(tx.partner_id);
	// // // tinfor.setStarFriend(TxInfor.TX_COMMON_FRIEND);
	// // // tinfor.setTxNormal(tx);
	// // // mTxInforCache.put(tx.partner_id, tinfor);
	// // // }else {
	// // // tinfor = mTxInforCache.get(tx.partner_id);
	// // // if(tinfor!=null){
	// // // tinfor.setTxNormal(tx);
	// // // }
	// // // }
	// //
	// // TX tx0 = mTBTXCache.get(tx.partner_id);
	// // if (tx0 == null) {
	// // // 添加到好友缓存中
	// // tx.setTx_type(TxDB.TX_TYPE_TB);
	// // tx.setStarFriend(getStarFriendAttr(tx.partner_id));
	// // tx.setIn_black_list(TxDB.TX_NOT_IN_BLACK_LIST);
	// //
	// // String remarkName = getTBTXRemarkName(tx.partner_id);
	// // if (!TextUtils.isEmpty(remarkName)) {
	// // tx.setRemarkName(remarkName);
	// // }
	// //
	// // mTBTXCache.put(tx.partner_id, tx);
	// // // 保存到数据库
	// // ContentValues values = txToValues(tx);
	// // // 如果该好友是从陌生联系人变为好友，那么数据库中会存在，所以执行更新操作，失败后再插入
	// // if (updateTxByTXId(values, tx.partner_id) == 0) {
	// // mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// // }
	// // }
	// // return true;
	// // }
	//
	// // private TX updateTBTX(long partner_id, ContentValues values) {
	// // TX tbtx = mTBTXCache.get(partner_id);
	// // if (tbtx != null) {
	// // return updateTXByValues(tbtx, values);
	// // }
	// // return null;
	// // }
	//
	// // /**删除好友*/
	// // public TX removeTBTX(TxData txdata, long partner_id) {
	// // TX tx = null;
	// // mTBTXIds.remove(partner_id);
	// // tx = mTBTXCache.remove(partner_id);
	// //
	// // MsgStat.delMsgStatByPartnerId(txdata,partner_id);
	// // txdata.broadcastMsg(TxData.FLUSH_MSGS);
	// // return tx;
	// // }
	// //
	//
	// // 更新好友的字段
	// private TX updateTXByValues(TX tx, ContentValues values) {
	// if (tx != null) {
	// Set<Entry<String, Object>> valueSet = values.valueSet();
	// Iterator<Entry<String, Object>> txIterator = valueSet
	// .iterator();
	// while (txIterator.hasNext()) {
	// Entry<String, Object> tbEntry = txIterator.next();
	// String key = tbEntry.getKey();
	// Object value = tbEntry.getValue();
	// // if (key.equals(TxDB.Tx.TX_TYPE)) {
	// // // 联系人类型
	// // tx.setTx_type((Integer) value);
	// // } else if (key.equals(TxDB.Tx.CONTACTS_PERSON_ID)) {
	// // // 通讯录索引id
	// // // tx.setContacts_person_id((Integer)value);
	//
	// // } else if (key.equals(TxDB.Tx.CONTACTS_PERSON_NAME)) {
	// // // 本地通讯录显示名称
	// // tx.setContacts_person_name((String) value);
	//
	// // } else if (key.equals(TxDB.Tx.SMS_PERSON)) {
	// // // 短信人 在通讯录列表索引 null 为陌生人
	// //
	// // // TODO 这个值要它何用？！
	//
	// // } else if (key.equals(TxDB.Tx.SMS_ADDRESS)) {
	// // // 短信地址
	// // // TODO 这个值要它何用？！
	//
	// // } else
	// if (key.equals(TxDB.Tx.TX_ID)) {
	// // 神聊号
	// tx.partner_id = (Long) value;
	//
	// } else if (key.equals(TxDB.Tx.DISPLAY_NAME)) {
	// // 神聊联系人昵称名称
	// String nickName = (String) value;
	// tx.setNick_name(nickName);
	// tx.setNickNamePinyin(CnToSpell.getFullSpell(nickName));// 昵称的拼音用昵称现生成
	//
	// } else if (key.equals(TxDB.Tx.AVATAR_MD5)) {
	// // 头像版本号
	// tx.setAvatar_ver((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.AVATAR_URL)) {
	// // 头像网络路径
	// String oldAvatarUrl = tx.getAvatar_url();
	// if (!oldAvatarUrl.equals((String) value)) {
	// // 新头像地址和老地址不一样，删除头像缓存和本地已有的头像大小图，再更新头像地址字段
	// LoginSessionManager mSess = LoginSessionManager
	// .getManager(mContext);
	// // 删除缓存
	// if (BaseActivity
	// .removeTXHeadImgCache(tx.partner_id)) {
	// if (Utils.debug)
	// Log.e(TAG, tx.partner_id + " 头像内存缓存删除成功");
	// } else {
	// if (Utils.debug)
	// Log.e(TAG, "没有找到" + tx.partner_id
	// + " 头像内存缓存");
	// }
	//
	// // 删除本地SD卡大小图
	// String filePath = mSess.mDownUpMgr.getAvatarFile(
	// oldAvatarUrl, tx.partner_id, false);
	// File headFile = null;
	// if (filePath != null) {
	// headFile = new File(filePath);
	// if (headFile.exists()) {
	// headFile.delete();
	// }
	// }
	// filePath = mSess.mDownUpMgr.getAvatarFile(
	// oldAvatarUrl, tx.partner_id, true);
	// if (filePath != null) {
	// headFile = new File(filePath);
	// if (headFile.exists()) {
	// headFile.delete();
	// }
	// }
	//
	// tx.setAvatar_url((String) value);
	// }
	//
	// } else if (key.equals(TxDB.Tx.SEX)) {
	// // 性别
	// tx.setSex((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.BIRTHDAY)) {
	// // 生日
	// tx.setBirthday((String) value);
	//
	// } else if (key.equals(TxDB.Tx.BLOOD_TYPE)) {
	// // 血型
	// tx.setBloodType((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.HOBBY)) {
	// // 爱好
	// tx.setHobby((String) value);
	//
	// } else if (key.equals(TxDB.Tx.PROFESSION)) {
	// // 职业
	// tx.setJob((String) value);
	//
	// } else if (key.equals(TxDB.Tx.HOME)) {
	// // 所在地
	// tx.setArea((String) value);
	//
	// // } else if (key.equals(TxDB.Tx.USER_NAME)) {
	// // // 真实名称
	// // // TODO 这个值要它何用？！
	// // tx.user_name = (String) value;
	//
	// } else if (key.equals(TxDB.Tx.TX_SIGN)) {
	// // 个性签名
	// tx.setSign((String) value);
	//
	// } else if (key.equals(TxDB.Tx.DISTANCE)) {
	// // 距离
	// tx.setDistance((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.AGE)) {
	// // 年龄
	// tx.setAge((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.CONSTELLATION)) {
	// // 星座
	// tx.setConstellation((String) value);
	//
	// } else if (key.equals(TxDB.Tx.PHONE)) {
	// // 手机号
	// tx.setPhone((String) value);
	//
	// } else if (key.equals(TxDB.Tx.EMAIL)) {
	// // 邮箱
	// tx.setEmail((String) value);
	//
	// } else if (key.equals(TxDB.Tx.IS_P_BIND)) {
	// // 手机号绑定
	// tx.is_phone_bind = (Boolean) value;
	//
	// } else if (key.equals(TxDB.Tx.IS_E_BIND)) {
	// // 邮箱绑定
	// tx.is_email_bind = (Boolean) value;
	//
	// // } else if (key.equals(TxDB.Tx.IN_BLACK_LIST)) {
	// // // 黑名单
	// // // boolean inBlack = (Boolean)value;
	// // tx.setIn_black_list((Integer) value);
	//
	// // } else if (key.equals(TxDB.Tx.IS_STAR_FRIEND)) {
	// // // 是否是星标好友， 1：是，0:不是
	// // tx.setStarFriend((Integer) value);
	//
	// // } else if (key.equals(TxDB.Tx.GROUP_IDS)) {
	// // // 群组ids 相对 本人来说的
	// // tx.group_ids = (Integer) value;
	// //
	// // } else if (key.equals(TxDB.Tx.GROUP_NAMES)) {
	// // // 群组名s 相对本人来说
	// // tx.group_names = (String) value;
	// //
	// // } else if (key.equals(TxDB.Tx.GROUP_USER_NAMES)) {
	// // // 用户在群组显示的名
	// // tx.group_user_names = (String) value;
	// //
	// // } else if (key.equals(TxDB.Tx.GROUP_USER_SIGNS)) {
	// // // //用户在群组签名
	// // tx.group_user_signs = (String) value;
	//
	// // } else if (key.equals(TxDB.Tx.FIRST_CHAR)) {
	// // // 本地联系人排序字符
	// // tx.setPerson_name_pinyin((String) value);
	//
	// } else if (key.equals(TxDB.Tx.SECOND_CHAR)) {
	// // 神聊联系排序字符
	// // tx.setNickNamePinyin((String)value);//昵称的拼音在设置昵称时自动生成，不直接设置了
	//
	// // } else if (key.equals(TxDB.Tx.PHONE_TYPE)) {
	// // // TODO 这个字段不要了吧？
	//
	// } else if (key.equals(TxDB.Tx.ALBUM)) {
	// // 相册
	// String album = (String) value;
	// ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
	// if (!Utils.isNull(album)) {
	// for (String url : album
	// .split(Constants.STRING_SEPARATOR)) {
	// AlbumItem ai = new AlbumItem();
	// ai.setUrl(url);
	// list.add(ai);
	// }
	// }
	// tx.setAlbum(list);
	//
	// } else if (key.equals(TxDB.Tx.LANGUAGES)) {
	// // 语言
	// tx.setLanguages((String) value);
	//
	// // } else if (key.equals(TxDB.Tx.MEDALS)) {
	// // // 勋章
	// // tx.setMedals((String) value);
	// //
	// // } else if (key.equals(TxDB.Tx.GRADE)) {
	// // // 等级
	// // tx.setGrade((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.ISOP)) {
	// // 是否是管理员
	// tx.setIsop((String) value);
	//
	// // } else if (key.equals(TxDB.Tx.REMARK_NAME)) {
	// // // 备注名
	// // tx.setRemarkName((String) value);
	//
	// } else if (key.equals(TxDB.Tx.ALBUM_VER)) {
	// // 相册版本
	// tx.setAlbumVer((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.INFO_VER)) {
	// // 信息版本
	// tx.setInfoVer((Integer) value);
	//
	// } else if (key.equals(TxDB.Tx.IS_RECEIVE_REQ)) {
	// // 是否接收好友请求
	// tx.setReceiveReq(((Integer) value) == 0 ? true : false);
	//
	// // } else if (key.equals(TxDB.Tx.BLACK_TIME)) {
	// // // 拉黑时间
	// // tx.setInBlackTime((Long) value);
	//
	// // } else if (key.equals(TxDB.Tx.CONTACT_TIME)) {
	// // // 联系人建立时间
	// // tx.contact_time = (Long) value;
	// }
	// }
	// // 缓存更新完毕，开始更新数据库
	// updateTxByTXId(values, tx.partner_id);
	//
	// }
	// return tx;
	//
	// }
	//
	// // 添加陌生神聊联系人
	// private boolean addSTTX(TX tx) {
	// // if (tx.getTx_type() != TxDB.TX_TYPE_ST
	// // && !Utils.isIdValid(tx.partner_id)) {
	// // return false;
	// // }
	// if (!Utils.isIdValid(tx.partner_id)) {
	// return false;
	// }
	// TX tx0 = mSTTXCache.get(tx.partner_id);
	// if (tx0 == null) {
	// // tx.setTx_type(TxDB.TX_TYPE_ST);
	// // tx.setStarFriend(TxDB.TX_NOT_FRIEND);
	// // tx.setIn_black_list(TxDB.TX_NOT_IN_BLACK_LIST);
	// mSTTXCache.put(tx.partner_id, tx);
	// TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
	// if (tinfor != null) {
	// tx.setTxInfor(tinfor);
	// }
	// ContentValues values = txToValues(tx);
	// if (updateTxByTXId(values, tx.partner_id) == 0) {
	// // 陌生联系人是lrucache,缓存中不存在是也许数据库存在
	// mTxCr.insert(TxDB.Tx.CONTENT_URI, values);
	// }
	// }
	//
	// return true;
	// }
	//
	// private TX updateSTTX(long partner_id, ContentValues values) {
	// TX sttx = mSTTXCache.get(partner_id);
	// if (sttx != null) {
	// return updateTXByValues(sttx, values);
	// } else {
	// sttx = getSTTX(partner_id);
	// if (sttx != null) {
	// return updateTXByValues(sttx, values);
	// }
	// // sttx = findTXFromDB(partner_id);
	// // if (sttx != null) {
	// // TxInfor tinfor = mTxInforCacheMap.get(partner_id);
	// // if(tinfor!=null){
	// // sttx.setTxInfor(tinfor);
	// // }
	// // mSTTXCache.put(sttx.partner_id, sttx);
	// // return updateTXByValues(sttx, values);
	// // }
	// return null;
	// }
	//
	// }
	//
	// private TX getSTTX(long partner_id) {
	// TX tx = mSTTXCache.get(partner_id);
	// if (tx == null) {
	// tx = findTXFromDB(partner_id);
	// if (tx != null) {
	// TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
	// if (tinfor != null) {
	// tx.setTxInfor(tinfor);// 加入缓存前给tx加上与账户相关信息
	// }
	// mSTTXCache.put(partner_id, tx);
	// }
	// }
	// return tx;
	// }
	//
	// // /**删除陌生神聊联系人*/
	// // public TX removeSTTX(long partner_id) {
	// // return mSTTXCache.remove(partner_id);
	// // }
	//
	// private ContentValues txToValues(TX tx) {
	// ContentValues values = new ContentValues();
	// // values.put(TxDB.Tx.TX_TYPE, tx.getTx_type());
	// // values.put(TxDB.Tx.CONTACTS_PERSON_ID,
	// // tx.getContacts_person_id());
	// // values.put(TxDB.Tx.CONTACTS_PERSON_NAME,
	// // tx.getContacts_person_name());
	// // values.put(TxDB.Tx.FIRST_CHAR, tx.getPerson_name_pinyin());
	// // values.put(TxDB.Tx.PHONE_TYPE, tx.getPhone_type());
	// values.put(TxDB.Tx.TX_ID, tx.partner_id);
	// values.put(TxDB.Tx.DISPLAY_NAME, tx.getNick_name());
	// // values.put(TxDB.Tx.USER_NAME, tx.user_name);
	// values.put(TxDB.Tx.TX_SIGN, tx.user_sign);
	// values.put(TxDB.Tx.PHONE, tx.getPhone());
	// values.put(TxDB.Tx.EMAIL, tx.email);
	// values.put(TxDB.Tx.IS_P_BIND, tx.is_phone_bind);
	// values.put(TxDB.Tx.IS_E_BIND, tx.is_email_bind);
	// // 以下字段不要了 2014.03.28 shc
	// // values.put(TxDB.Tx.GROUP_IDS, tx.group_ids);
	// // values.put(TxDB.Tx.GROUP_NAMES, tx.group_names);
	// // values.put(TxDB.Tx.GROUP_USER_NAMES, tx.group_user_names);
	// // values.put(TxDB.Tx.GROUP_USER_SIGNS, tx.group_user_signs);
	// values.put(TxDB.Tx.AVATAR_MD5, tx.getAvatar_ver());
	// values.put(TxDB.Tx.AVATAR_URL, tx.avatar_url);
	// values.put(TxDB.Tx.SEX, tx.sex);
	// values.put(TxDB.Tx.BIRTHDAY, tx.birthday);
	// values.put(TxDB.Tx.BLOOD_TYPE, tx.blood_type);
	// values.put(TxDB.Tx.HOBBY, tx.hobby);
	// values.put(TxDB.Tx.PROFESSION, tx.job);
	// values.put(TxDB.Tx.HOME, tx.area);
	// values.put(TxDB.Tx.DISTANCE, tx.distance);
	// values.put(TxDB.Tx.AGE, tx.age);
	// // values.put(TxDB.Tx.IN_BLACK_LIST, tx.in_black_list);
	// values.put(TxDB.Tx.SECOND_CHAR, tx.nick_name_pinyin);
	// // values.put(TxDB.Tx.CONTACT_TIME, tx.contact_time);
	// // values.put(Tx.GRADE, tx.grade);
	// values.put(Tx.ISOP, tx.isop);
	// // values.put(Tx.REMARK_NAME, tx.remarkName);
	// // values.put(Tx.MEDALS, tx.medals);
	// // 如果有相册信息，为values赋值
	// if (tx.getAlbum() != null && !tx.getAlbum().isEmpty()) {
	// StringBuilder sb = new StringBuilder();
	// for (int i = 0; i < tx.getAlbum().size(); i++) {
	// sb.append(tx.getAlbum().get(i).getUrl());
	// if (i != tx.getAlbum().size() - 1) {
	// sb.append(Constants.STRING_SEPARATOR);
	// }
	// }
	// values.put(Tx.ALBUM, sb.toString());
	// }
	// values.put(Tx.ALBUM_VER, tx.albumVer);
	// values.put(Tx.INFO_VER, tx.infoVer);
	// values.put(Tx.IS_RECEIVE_REQ, tx.isReceiveReq ? 0 : 1);
	// // values.put(Tx.BLACK_TIME, tx.inBlackTime);
	// values.put(Tx.LANGUAGES, tx.languages);
	// // values.put(Tx.IS_STAR_FRIEND, tx.getStarFriend());
	// return values;
	// }
	//
	// /** 根据条件更新数据库TX */
	// private int updateTxByTXId(ContentValues values, long partnerId) {
	// int rows = mTxCr.update(TxDB.Tx.CONTENT_URI, values, TxDB.Tx.TX_ID
	// + "=?", new String[] { "" + partnerId });
	// return rows;
	// }
	//
	// /** 从数据库读取TX对象 */
	// private TX findTXFromDB(long id) {
	// TX tx = null;
	// if (Utils.isIdValid(id)) {
	// Cursor cur = mTxCr.query(TxDB.Tx.CONTENT_URI, null,
	// TxDB.Tx.TX_ID + "=?", new String[] { "" + id }, null);
	//
	// if (cur != null) {
	// if (cur.moveToNext()) {
	// tx = fetchOneContacts(cur);
	// }
	// cur.close();
	// }
	// }
	// return tx;
	// }
	//
	// public boolean isTxFriend(long id) {
	// // return mTBTXIds.containsKey(id);
	// // return mTBTXCache.containsKey(id);
	// TxInfor tinfor = mTxInforCacheMap.get(id);
	// if (tinfor != null && !tinfor.isBlackType()&& tinfor.isTBType()) {
	// return true;
	// }
	// return false;
	// }
	//
	// public boolean addBlackTX(long partner_id,long inBlackTime) {
	// if (!Utils.isIdValid(partner_id)) {
	// // 联系人类型必须是“陌生神聊联系人”
	// return false;
	// }
	//
	// TxInfor tinfor = mTxInforCacheMap.get(partner_id);
	// if (tinfor == null) {
	// // 添加到黑名单缓存中
	// tinfor = new TxInfor(partner_id, TxInfor.TX_TYPE_BLACK);
	// tinfor.setInBlackTime(inBlackTime);
	// // tx0.setTx_type(TxInfor.TX_TYPE_BLACK);
	// // tx0.setStarFriend(TxInfor.TX_NOT_FRIEND);
	// // mBlackTXCache.put(partner_id, tinfor);
	// mTxInforCacheMap.put(partner_id, tinfor);
	// ContentValues values = tinfor.txinforToValues();
	// // values.put(TxDB.TX_Friends.TX_TYPE, TxInfor.TX_TYPE_BLACK);
	// // values.put(TxDB.TX_Friends.IS_STAR_FRIEND,
	// // TxInfor.TX_NOT_FRIEND);
	// if (updateTxInforByTXId(values, partner_id) == 0) {
	// mTxCr.insert(TxDB.TX_Friends.CONTENT_URI, values);
	// }
	// }else {
	// //修改缓存中的对象
	// tinfor.setBlackType();
	// tinfor.setInBlackTime(inBlackTime);
	// updateTxInforByTXId(tinfor.txinforToValues(), partner_id);
	// }
	// // TX tx = getTx(partner_id);
	// // if (tx != null) {
	// // tx.setTxInfor(tinfor);
	// // }
	//
	// return true;
	// }
	//
	// // private TX updateBlackTX(long partner_id, ContentValues values) {
	// // TX blackTx = mBlackTXCache.get(partner_id);
	// // if (blackTx != null) {
	// // return updateTXByValues(blackTx, values);
	// // }
	// // return null;
	// // }
	//
	// public ArrayList<TX> getBlackTXList() {
	// if (Utils.debug)
	// Log.i(TAG, "getBlackTXList()");
	// // 存储黑名单的list集合
	// ArrayList<TX> resultTXList = new ArrayList<TX>();
	// // Collection<TxInfor> tmpCollections = mBlackTXCache.values();
	// // for(TxInfor tinfor : tmpCollections){
	// // if(tinfor.getTxNormal()!=null){
	// // resultTXList.add(tinfor.getTxNormal());
	// // }
	// // }
	// Set<Entry<Long, TxInfor>> tmpSet = mTxInforCacheMap.entrySet();
	// Iterator<Entry<Long, TxInfor>> idIter = tmpSet.iterator();
	// TX tx = null;
	// while (idIter.hasNext()) {
	// Entry<Long, TxInfor> entryItem = idIter.next();
	// if (entryItem.getValue().isBlackType()) {
	//
	// if ((tx = getTx(entryItem.getKey())) != null) {
	// tx.setTxInfor(entryItem.getValue());// 设置黑名单TX特有属性
	// resultTXList.add(tx);
	// }
	// }
	// }
	//
	// // Set<Long> tmpSet = mTxInforCacheMap.keySet();
	// // Iterator<Long> idIter = tmpSet.iterator();
	// // TX tx = null;
	// // while (idIter.hasNext()) {
	// // Long id = idIter.next();
	// // if((tx=getTx(id))!=null){
	// // tx.setTxInfor(mBlackTXCache.get(id));//设置黑名单TX特有属性
	// // resultTXList.add(tx);
	// // }
	// // }
	//
	// // resultTXList.clear();// 先清空集合
	// // resultTXList.addAll(tmpCollections);// 再把黑名单map中的TX全部添加到这个list中
	// // if (Utils.debug) {
	// // Log.i(TAG, "黑名单list:" + resultTXList.toString());
	// // }
	// return resultTXList;
	// }
	//
	// public boolean isInBlack(long partner_id) {
	// // 是否在我的黑名单
	// TxInfor tinfor = mTxInforCacheMap.get(partner_id);
	// if (tinfor != null && tinfor.isBlackType()) {
	// return true;
	// }
	// return false;
	// }
	//
	// // private TX getBlackTx(long partner_id){
	// // return mBlackTXCache.get(partner_id);
	// // }
	//
	// public int getStarFriendAttr(long id) {
	// // if (mTBTXIds.containsKey(id)) {
	// // return mTBTXIds.get(id);
	// // }
	//
	// if (mTxInforCacheMap.containsKey(id)) {
	// return mTxInforCacheMap.get(id).getStarFriend();
	// }
	//
	// return TxInfor.TX_NOT_FRIEND;
	// }
	//
	// // /**获取好友TxInfor*/
	// // public TxInfor getTBTX(long partner_id) {
	// // //获得好友tx
	// // return mTBTXCache.get(partner_id);
	// // }
	// //
	// // /**获取黑名单TxInfor*/
	// // public TxInfor getBlackTX(long partner_id) {
	// // //获得好友tx
	// // return mBlackTXCache.get(partner_id);
	// // }
	//
	// /** 取神聊TX,缓存优先 */
	// public TX getTx(long uid) {
	// if (!Utils.isIdValid(uid)) {
	// return null;
	// }
	// if (uid == TX.TUIXIN_MAN)
	// return getTxMan();
	// if (uid == TX.TUIXIN_FRIEND)
	// return getTxFriend();
	// if (uid == TX.SL_GROUP_NOTICE)
	// return getSlGroupNotice();
	// if (uid == TX.SL_SAFE) {
	// return getSlSafe();
	// }
	// if (uid == TX.tm.getUserID()) {
	// return getTxMe();
	// }
	//
	// TX tx1 = null;
	// // if (isTxFriend(uid)) {
	// // tx1 = mTBTXCache.get(uid);
	// // } else if (isInBlack(uid)) {
	// // tx1 = mBlackTXCache.get(uid);
	// // } else {
	// tx1 = getSTTX(uid);
	// // }
	//
	// return tx1;
	//
	// // TX tx = getTBTX(uid);
	// // if (tx!=null) {
	// // return tx;
	// // }else {
	// // tx = getBlackTx(uid);
	// // if (tx!=null) {
	// // return tx;
	// // }else {
	// // tx = getSTTX(uid);
	// // return tx;
	// // }
	// // }
	// }
	//
	// public boolean addTx(TX tx) {
	// // TxInfor tinfor = mTBTXCache.get(tx.partner_id);
	// // if (tinfor!=null) {
	// // tinfor.setTxNormal(tx);
	// // }else {
	// // tinfor = mBlackTXCache.get(tx.partner_id);
	// // if(tinfor!=null){
	// // tinfor.setTxNormal(tx);
	// // }
	// // }
	//
	// TxInfor tinfor = mTxInforCacheMap.get(tx.partner_id);
	// if (tinfor != null) {
	// tx.setTxInfor(tinfor);
	// }
	//
	// // 再添加到陌生人缓存中
	// if (getSTTX(tx.partner_id) == null) {
	// addSTTX(tx);
	// return true;
	// }
	// return false;
	// }
	//
	// // /** 添加TX */
	// // // 返回值为判定是否添加成功
	// // public boolean addTx(TX tx) {
	// // long partner_id = tx.partner_id;
	// // if (TX.tm.isTxFriend(partner_id)) {
	// // // 好友
	// // if (mTBTXCache.get(partner_id) == null) {
	// // addTBTX(tx);
	// // return true;
	// // }
	// // } else if (TX.tm.isInBlack(partner_id)) {
	// // // 黑名单
	// // if (mBlackTXCache.get(partner_id) == null) {
	// // addBlackTX(tx);
	// // return true;
	// // }
	// // } else {
	// // // 陌生人
	// // if (getSTTX(partner_id) == null) {
	// // addSTTX(tx);
	// // return true;
	// // }
	// // }
	// // return false;
	// // }
	//
	// public TX updateTx(long partner_id, ContentValues values) {
	// return updateSTTX(partner_id, values);
	// }
	//
	// // public TxInfor updateTxInfor(long partner_id, ContentValues values) {
	// // if (mTxInforCacheMap.containsKey(partner_id)) {
	// // //是特殊TX
	// // return updateTXInforByValues(partner_id, values);
	// // }
	// // return null;
	// // }
	//
	//
	// // /** 修改跟账户相关的values，例如好友和黑名单的用户 */
	// // public TX updateTxInfor(long partner_id, ContentValues values) {
	// //
	// // return updateSTTX(partner_id, values);
	// //
	// // }
	//
	// /** 更新Tx */
	// // public TX updateTx(long partner_id, ContentValues values) {
	// // if (!values.containsKey(TxDB.Tx.IS_STAR_FRIEND)) {
	// // // 避免出现要更新行星标好友的值被覆盖了
	// // values.put(TxDB.Tx.IS_STAR_FRIEND,
	// // getStarFriendAttr(partner_id));
	// // }
	// // if (TX.tm.isTxFriend(partner_id)) {
	// // // 避免出现数据不一致,强制赋值
	// // values.put(TxDB.Tx.TX_TYPE, TxDB.TX_TYPE_TB);
	// // // values.put(TxDB.Tx.IN_BLACK_LIST, TxDB.TX_NOT_IN_BLACK_LIST);
	// // return updateTBTX(partner_id, values);
	// // } else if (TX.tm.isInBlack(partner_id)) {
	// // // 避免出现数据不一致,强制赋值
	// // values.put(TxDB.Tx.TX_TYPE, TxDB.TX_TYPE_ST);
	// // // if (isInBlack(partner_id)) {
	// // // values.put(TxDB.Tx.IN_BLACK_LIST, TxDB.TX_IN_MY_BLACK_LIST);
	// // // } else {
	// // // values.put(TxDB.Tx.IN_BLACK_LIST, TxDB.TX_NOT_IN_BLACK_LIST);
	// // // }
	// // return updateBlackTX(partner_id, values);
	// // } else {
	// // // 避免出现数据不一致,强制赋值
	// // values.put(TxDB.Tx.TX_TYPE, TxDB.TX_TYPE_ST);
	// // // values.put(TxDB.Tx.IN_BLACK_LIST, TxDB.TX_NOT_IN_BLACK_LIST);
	// // return updateSTTX(partner_id, values);
	// // }
	// // }
	//
	// /** 更改Tx为陌生人 */
	// public void changeTxToST(TxData txdata, long partner_id) {
	// // TX tx = null;
	// // if (isTxFriend(partner_id)) {
	// // // 好友
	// // mTBTXIds.remove(partner_id);
	// // tx = mTBTXCache.remove(partner_id);
	// // addSTTX(tx);// 添加到陌生人中
	// //
	// // MsgStat.delMsgStatByPartnerId(txdata, partner_id);
	// // txdata.broadcastMsg(TxData.FLUSH_MSGS);
	// // } else if (isInBlack(partner_id)) {
	// // // 黑名单中
	// // tx = mBlackTXCache.remove(partner_id);
	// // addSTTX(tx);// 添加到陌生人中
	// // }
	//
	// TxInfor tinfor = mTxInforCacheMap.remove(partner_id);// 总引用中删除
	// if (tinfor != null) {
	// if (tinfor.isTBType()) {
	// // 好友
	// MsgStat.delMsgStatByPartnerId(txdata, partner_id);
	// }
	// tinfor.clearAttrs();
	// int delRow = mTxCr.delete(TxDB.TX_Friends.CONTENT_URI,
	// TxDB.Tx.TX_ID + "=?", new String[] { "" + partner_id });
	// if (Utils.debug)
	// Log.i(TAG, "删除了【" + delRow + "】条");
	// }
	//
	// // TxInfor tinfor = mTBTXCache.remove(partner_id);
	// //
	// // if (tinfor != null) {
	// // ContentValues values = new ContentValues();
	// // values.put(TxDB.TX_Friends.TX_TYPE, TxInfor.TX_TYPE_DEFAULT);
	// // values.put(TxDB.TX_Friends.IS_STAR_FRIEND,
	// // TxInfor.TX_NOT_FRIEND);
	// // updateTXInforByValues(tinfor, values);
	// //
	// // MsgStat.delMsgStatByPartnerId(txdata, partner_id);
	// // txdata.broadcastMsg(TxData.FLUSH_MSGS);
	// // } else {
	// // tinfor = mBlackTXCache.remove(partner_id);
	// // if (tinfor != null) {
	// // ContentValues values = new ContentValues();
	// // values.put(TxDB.TX_Friends.TX_TYPE, TxInfor.TX_TYPE_DEFAULT);
	// // values.put(TxDB.TX_Friends.IS_STAR_FRIEND,
	// // TxInfor.TX_NOT_FRIEND);
	// // updateTXInforByValues(tinfor, values);
	// // }
	// // }
	// }
	//
	// /** 删除tx并添加到黑名单中 */
	// public void removeTxToBlack(TxData txdata, long partner_id) {
	//
	// // TX tx = null;
	// // if (isTxFriend(partner_id)) {
	// // // 好友
	// // mTBTXIds.remove(partner_id);
	// // tx = mTBTXCache.remove(partner_id);
	// // addBlackTX(tx);// 添加到黑名单中
	// //
	// // MsgStat.delMsgStatByPartnerId(txdata, partner_id);
	// // txdata.broadcastMsg(TxData.FLUSH_MSGS);
	// // } else if (isInBlack(partner_id)) {
	// // // 黑名单中
	// // } else {
	// // // 陌生人
	// // tx = getSTTX(partner_id);
	// // if (tx != null) {
	// // mSTTXCache.remove(partner_id);
	// // addBlackTX(tx);// 添加到黑名单中
	// // }
	// // }
	// //
	//
	// if (isTxFriend(partner_id)) {
	// // 好友
	// // TxInfor tinfor = mTBTXCache.remove(partner_id);
	// TxInfor tinfor = mTxInforCacheMap.remove(partner_id);
	// if (tinfor != null) {
	// tinfor.clearAttrs();
	// }
	//
	// MsgStat.delMsgStatByPartnerId(txdata, partner_id);
	// txdata.broadcastMsg(TxData.FLUSH_MSGS);
	// }
	//
	// addBlackTX(partner_id,System.currentTimeMillis());// 添加到黑名单中
	//
	// }
	//
	// /** 修改陌生人为好友 */
	// public void changeSTToTB(long partner_id) {
	// TX tx = getTx(partner_id);
	// if (tx != null) {
	// addTBTXId(partner_id, TxInfor.TX_COMMON_FRIEND);
	// addTx(tx);
	// }
	// }
	//
	// // /** 修改陌生人为好友 */
	// // public void changeSTToTB(long partner_id) {
	// // TX tx = getTx(partner_id);
	// // if (tx != null && tx.getTx_type() == TxDB.TX_TYPE_ST
	// // && !isInBlack(partner_id)) {
	// // mSTTXCache.remove(partner_id);
	// // addTBTXId(partner_id);
	// // addTx(tx);
	// // }
	// // }
	//
	// private TX fetchOneContacts(Cursor c) {
	// TX o = new TX();
	// // o._id = c.getLong(c.getColumnIndex(TxDB.Tx._ID));
	// // o.setTx_type(c.getInt(c.getColumnIndex(TxDB.Tx.TX_TYPE)));
	// //
	// o.setContacts_person_id(c.getInt(c.getColumnIndex(TxDB.Tx.CONTACTS_PERSON_ID)));
	// // o.setContacts_person_name(c.getString(c
	// // .getColumnIndex(TxDB.Tx.CONTACTS_PERSON_NAME)));
	// o.setPartnerId(c.getLong(c.getColumnIndex(TxDB.Tx.TX_ID)));
	// o.setNick_name(c.getString(c.getColumnIndex(TxDB.Tx.DISPLAY_NAME)));
	// o.setAvatar_ver(c.getInt(c.getColumnIndex(TxDB.Tx.AVATAR_MD5)));
	// o.avatar_url = c.getString(c.getColumnIndex(TxDB.Tx.AVATAR_URL));
	// o.sex = c.getInt(c.getColumnIndex(TxDB.Tx.SEX));
	// o.birthday = c.getString(c.getColumnIndex(TxDB.Tx.BIRTHDAY));
	// o.blood_type = c.getInt(c.getColumnIndex(TxDB.Tx.BLOOD_TYPE));
	// o.hobby = c.getString(c.getColumnIndex(TxDB.Tx.HOBBY));
	// o.job = c.getString(c.getColumnIndex(TxDB.Tx.PROFESSION));
	// o.area = c.getString(c.getColumnIndex(TxDB.Tx.HOME));
	// o.distance = c.getInt(c.getColumnIndex(TxDB.Tx.DISTANCE));// 距离
	// o.age = c.getInt(c.getColumnIndex(TxDB.Tx.AGE));// 年龄
	// o.constellation = c.getString(c
	// .getColumnIndex(TxDB.Tx.CONSTELLATION));// 星座
	// // o.user_name = c.getString(c.getColumnIndex(TxDB.Tx.USER_NAME));
	// o.user_sign = c.getString(c.getColumnIndex(TxDB.Tx.TX_SIGN));
	// o.setPhone(c.getString(c.getColumnIndex(TxDB.Tx.PHONE)));
	// o.email = c.getString(c.getColumnIndex(TxDB.Tx.EMAIL));
	// String is_phone_bind = c.getString(c
	// .getColumnIndex(TxDB.Tx.IS_P_BIND));
	// if (is_phone_bind != null)
	// o.is_phone_bind = is_phone_bind.equals("1");
	// else
	// o.is_phone_bind = false;
	// String is_email_bind = c.getString(c
	// .getColumnIndex(TxDB.Tx.IS_E_BIND));
	// if (is_email_bind != null)
	// o.is_email_bind = is_email_bind.equals("1");
	// else
	// o.is_email_bind = false;
	// // o.group_ids = c.getInt(c.getColumnIndex(TxDB.Tx.GROUP_IDS));
	// // o.group_names =
	// // c.getString(c.getColumnIndex(TxDB.Tx.GROUP_NAMES));
	// // o.group_user_names = c.getString(c
	// // .getColumnIndex(TxDB.Tx.GROUP_USER_NAMES));
	// // o.group_user_signs = c.getString(c
	// // .getColumnIndex(TxDB.Tx.GROUP_USER_SIGNS));
	// // o.setPerson_name_pinyin(c.getString(c
	// // .getColumnIndex(TxDB.Tx.FIRST_CHAR)));
	// // o.nick_name_pinyin =
	// // c.getString(c.getColumnIndex(TxDB.Tx.SECOND_CHAR));
	// o.nick_name_pinyin = CnToSpell.getFullSpell(o.getNick_name());
	// // o.contact_time =
	// // c.getLong(c.getColumnIndex(TxDB.Tx.CONTACT_TIME));
	// // o.in_black_list =
	// // c.getInt(c.getColumnIndex(TxDB.Tx.IN_BLACK_LIST));
	// // o.grade = c.getInt(c.getColumnIndex(Tx.GRADE));
	// o.isop = c.getString(c.getColumnIndex(Tx.ISOP));
	// // o.remarkName = c.getString(c.getColumnIndex(Tx.REMARK_NAME));
	// o.languages = c.getString(c.getColumnIndex(Tx.LANGUAGES));
	// String album = c.getString(c.getColumnIndex(Tx.ALBUM));
	// ArrayList<AlbumItem> list = new ArrayList<AlbumItem>();
	// if (!Utils.isNull(album)) {
	// for (String url : album.split(Constants.STRING_SEPARATOR)) {
	// AlbumItem ai = new AlbumItem();
	// ai.setUrl(url);
	// list.add(ai);
	// }
	// }
	// o.setAlbum(list);
	// // o.medals = c.getString(c.getColumnIndex(Tx.MEDALS));
	// o.infoVer = c.getInt(c.getColumnIndex(Tx.INFO_VER));
	// o.albumVer = c.getInt(c.getColumnIndex(Tx.ALBUM_VER));
	// o.isReceiveReq = c.getInt(c.getColumnIndex(Tx.IS_RECEIVE_REQ)) == 0 ?
	// true
	// : false;
	// // o.inBlackTime = c.getLong(c.getColumnIndex(Tx.BLACK_TIME));
	// // o.setStarFriend(c.getInt(c.getColumnIndex(Tx.IS_STAR_FRIEND)));
	// return o;
	// }
	//
	// public ArrayList<TX> fetchAllContacts(Cursor c) {
	// ArrayList<TX> ret = new ArrayList<TX>();
	// if (c != null) {
	// while (c.moveToNext()) {
	// ret.add(fetchOneContacts(c));
	// }
	// c.close();
	// }
	//
	// return ret;
	// }
	//
	// /** 应该是根据TX的partnerId来判断过滤重复的TX */
	// public List<TX> listUniq(List<TX> list) {
	// Set<TX> set = new LinkedHashSet<TX>();
	// set.addAll(list);
	// List<TX> newlist = new ArrayList<TX>();
	// newlist.addAll(set);
	// return newlist;
	// }
	//
	// /**
	// * 通过会话对象，返回TX用户信息对象
	// *
	// * @param singleMsgStat
	// * 都是好友单聊的会话，该形参不会有群聊的会话
	// */
	// public TX findTXByMsgStat(MsgStat singleMsgStat) {
	// // TxInfor tx = null;
	// // if (Utils.isIdValid(singleMsgStat.partner_id)) {
	// // tx = mTxInforCache.get(singleMsgStat.partner_id);
	// // tx.setContacts_person_name(singleMsgStat.contacts_person_name);
	// // }
	// return getTx(singleMsgStat.partner_id);
	// }
	//
	// public ArrayList<TX> getTBTXList() {
	// long b = System.currentTimeMillis();
	// if (Utils.debug)
	// Log.i(TAG, "getTBTXList()");
	// // Collection<TxInfor> tmpCollections = mTBTXCache.values();
	// ArrayList<TxInfor> tempTXList = new ArrayList<TxInfor>(
	// mTxInforCacheMap.values());
	//
	// ArrayList<TX> resultTXList = new ArrayList<TX>();
	// TX ttx = null;
	// for (TxInfor tinfor : tempTXList) {
	// if (!tinfor.isBlackType()&&tinfor.isTBType()) {
	// // 好友
	// if ((ttx = getTx(tinfor.getPartner_id())) != null) {
	// resultTXList.add(ttx);
	// }
	// }
	// }
	//
	// // FirstCharComparator的规则是按好友拼音排序给list排序，并且星标好友优先排前面
	// Collections.sort(resultTXList, new FirstCharComparator(
	// TxInfor.TX_TYPE_TB));
	//
	// if (Utils.debug) {
	// Log.i(TAG, "好友list:" + resultTXList.toString());
	// Log.i(TAG,
	// "query tx cost time is:"
	// + (System.currentTimeMillis() - b) + "ms");
	// }
	// return resultTXList;
	// }
	//
	// public TX getTxMe() {
	// if (tx_me == null) {
	// tm.reloadTXMe();
	// }
	// return tx_me;
	// }
	//
	// /** 更新TXme，再次从sharedPreference中读取 */
	// public void reloadTXMe() {
	// if (tx_me == null) {
	// tx_me = new TX();
	// }
	// // String user_id = prefs.getString(CommonData.USER_ID, "");
	// String user_id = mSess.mPrefMeme.user_id.getVal();
	// // TODO id无效为什么还要判断取值？
	// if (Utils.isNull(user_id))
	// tx_me.partner_id = -1;
	// else
	// tx_me.partner_id = Long.parseLong(user_id);
	// // tx_me.pwd = prefs.getString(CommonData.PASSWORD, "");//这个字段废弃删除了
	// // tx_me.token = prefs.getString(CommonData.TOKEN, "");
	// // tx_me.setNick_name( prefs.getString(CommonData.NICKNAME, ""));
	// // tx_me.setPhone(prefs.getString(CommonData.TELEPHONE, ""));
	// // tx_me.is_phone_bind = prefs.getBoolean(CommonData.IS_BIND_PHONE,
	// // false);
	// // tx_me.email = prefs.getString(CommonData.EMAIL, "");
	// // tx_me.is_email_bind = prefs.getBoolean(CommonData.IS_BIND_EMAIL,
	// // false);
	// // tx_me.user_sign = prefs.getString(CommonData.SIGN, "");
	// // tx_me.birthday = prefs.getInt(CommonData.BIRTHDAY, 0) + "";
	// // tx_me.age = prefs.getInt(CommonData.AGE, 0);
	// // tx_me.constellation = prefs.getString(CommonData.CONSTELLATION,
	// // "");
	// // tx_me.blood_type = prefs.getInt(CommonData.BLOODTYPE, 0);
	// // tx_me.hobby = prefs.getString(CommonData.HOBBY, "");
	// // tx_me.job = prefs.getString(CommonData.JOB, "");
	// // tx_me.area = prefs.getString(CommonData.AREA, "");
	// // tx_me.sex = prefs.getInt(CommonData.SEX, 1);
	// // tx_me.setAvatar_ver(prefs.getInt(CommonData.AVATARVER, 0));
	// // tx_me.avatar_url = prefs.getString(CommonData.AVATAR_URL, "");
	// // tx_me.friend_ver = prefs.getInt(CommonData.FRIENDVER, 0);
	// // tx_me.auth = prefs.getInt(CommonData.AUTH, 0);
	// // tx_me.setLanguages(prefs.getString(CommonData.LANGUAGES, ""));
	// // tx_me.setReceiveReq(prefs.getBoolean(CommonData.IS_RECEIVE_REQ,
	// // true));
	// // tx_me.setAlbumVer(prefs.getInt(CommonData.ALBUM_VERSION, 0));
	//
	// tx_me.token = mSess.mPrefMeme.token.getVal();
	// tx_me.setNick_name(mSess.mPrefMeme.nickname.getVal());
	// tx_me.setPhone(mSess.mPrefMeme.telephone.getVal());
	// tx_me.is_phone_bind = mSess.mPrefMeme.is_bind_phone.getVal();
	// tx_me.email = mSess.mPrefMeme.email.getVal();
	// tx_me.is_email_bind = mSess.mPrefMeme.is_bind_email.getVal();
	// tx_me.user_sign = mSess.mPrefMeme.sign.getVal();
	// tx_me.birthday = mSess.mPrefMeme.birthday.getVal() + "";
	// tx_me.age = mSess.mPrefMeme.age.getVal();
	// tx_me.constellation = mSess.mPrefMeme.constellation.getVal();
	// tx_me.blood_type = mSess.mPrefMeme.bloodtype.getVal();
	// tx_me.hobby = mSess.mPrefMeme.hobby.getVal();
	// tx_me.job = mSess.mPrefMeme.job.getVal();
	// tx_me.area = mSess.mPrefMeme.area.getVal();
	// tx_me.sex = mSess.mPrefMeme.sex.getVal();
	// tx_me.setAvatar_ver(mSess.mPrefMeme.avatarver.getVal());
	// tx_me.avatar_url = mSess.mPrefMeme.avatar_url.getVal();
	// tx_me.friend_ver = mSess.mPrefMeme.friendver.getVal();
	// tx_me.auth = mSess.mPrefMeme.auth.getVal();
	// tx_me.setLanguages(mSess.mPrefMeme.languages.getVal());
	// tx_me.setReceiveReq(mSess.mPrefMeme.is_receive_req.getVal());
	// tx_me.setAlbumVer(mSess.mPrefMeme.album_version.getVal());
	//
	// // String str = prefs.getString(CommonData.ALBUM, "");
	// String str = mSess.mPrefMeme.album.getVal();
	//
	// ArrayList<AlbumItem> album = new ArrayList<AlbumItem>();
	// if (!"".equals(str)) {
	// List<String> list = StringUtils.str2List(str);
	// for (String url : list) {
	// AlbumItem ai = new AlbumItem();
	// ai.setUrl(url);
	// album.add(ai);
	// }
	// }
	// if (album.size() < 8) {
	// // 照片总张数小于8张，最后添加一张“新添加”的默认照片按钮
	// AlbumItem ai = new AlbumItem();
	// ai.setAdd(true);
	// album.add(ai);
	// }
	// tx_me.setAlbum(album);
	// // tx_me.setTx_type(TxDB.TX_TYPE_TB);
	//
	// }
	//
	// public long getUserID() {
	// return getTxMe().partner_id;
	//
	// }
	//
	// public TX getTxMan() {
	// if (tx_man == null) {
	// tx_man = new TX();
	// // tx_man.setTx_type(TxDB.MS_TYPE_TB);
	// tx_man.partner_id = TUIXIN_MAN;
	// tx_man.setNick_name("神聊客服");
	// tx_man.setEmail("service@shenliao.com");
	// tx_man.setArea("北京;朝阳");
	// tx_man.setSex(1);
	// tx_man.user_sign = "线控对讲机 语音对讲也能盲操作喽~";
	// tx_man.setBirthday("19880808");
	// tx_man.setBloodType(3);
	// tx_man.setPhone("01085870381");
	// }
	// return tx_man;
	// }
	//
	// public TX getTxFriend() {
	// if (tx_friend == null) {
	// tx_friend = new TX();
	// // tx_friend.setTx_type(TxDB.MS_TYPE_TB);
	// tx_friend.partner_id = TUIXIN_FRIEND;
	// tx_friend.setNick_name("好友管家");
	// tx_friend.setEmail("service@shenliao.com");
	// tx_friend.setArea("北京;朝阳");
	// tx_friend.setSex(1);
	// tx_friend.user_sign = "看看附近都有谁，和TA神聊下吧!";
	// tx_friend.setBirthday("19880808");
	// tx_friend.setBloodType(3);
	// tx_friend.setPhone("01085870381");
	// }
	// return tx_friend;
	// }
	//
	// public TX getSlGroupNotice() {
	// if (sl_notice == null) {
	// sl_notice = new TX();
	// // sl_notice.setTx_type(TxDB.MS_TYPE_TB);
	// sl_notice.partner_id = SL_GROUP_NOTICE;
	// sl_notice.setNick_name("群组动态");
	// sl_notice.setEmail("service@shenliao.com");
	// sl_notice.setArea("北京;朝阳");
	// sl_notice.setSex(1);
	// sl_notice.user_sign = "聊天室的新鲜事";
	// sl_notice.setBirthday("19880808");
	// sl_notice.setBloodType(3);
	// sl_notice.setPhone("01085870381");
	// }
	// return sl_notice;
	// }
	//
	// public TX getSlSafe() {
	// if (sl_safe == null) {
	// sl_safe = new TX();
	// // sl_safe.setTx_type(TxDB.MS_TYPE_TB);
	// sl_safe.partner_id = SL_SAFE;
	// sl_safe.setNick_name("神聊小卫士");
	// sl_safe.setEmail("service@shenliao.com");
	// sl_safe.setArea("北京;朝阳");
	// sl_safe.setSex(1);
	// sl_safe.user_sign = "维护聊天";
	// sl_safe.setBirthday("19880808");
	// sl_safe.setBloodType(3);
	// sl_safe.setPhone("01085870381");
	// }
	// return sl_safe;
	// }
	//
	// public void updateTxMan(int avatar_ver, String user_sign,
	// String avatar_url, String phone, String display_name_pinyin,
	// int sex, String birthday, int blood_type, String hobby,
	// String profession, String home, String email, int age,
	// String constellation) {
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
	// tx.setAge(age);
	// tx.setConstellation(constellation);
	// }
	//
	// public void updateTxFriend(int avatar_ver, String user_sign,
	// String avatar_url, String phone, String display_name_pinyin,
	// int sex, String birthday, int blood_type, String hobby,
	// String profession, String home, String email, int age,
	// String constellation, String isop) {
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
	// tx.setAge(age);
	// tx.setConstellation(constellation);
	// tx.setIsop(isop);
	// }
	//
	// private TxInfor fetchOneTxInfor(Cursor c) {
	// long partner_id = c
	// .getLong(c.getColumnIndex(TxDB.TX_Friends.TX_ID));
	// int tx_type = c.getInt(c.getColumnIndex(TxDB.TX_Friends.TX_TYPE));
	// TxInfor o = new TxInfor(partner_id, tx_type);
	// o.setContacts_person_name(c.getString(c
	// .getColumnIndex(TxDB.TX_Friends.CONTACTS_PERSON_NAME)));
	// o.setRemarkName(c.getString(c
	// .getColumnIndex(TxDB.TX_Friends.REMARK_NAME)));
	// o.setStarFriend(c.getInt(c
	// .getColumnIndex(TxDB.TX_Friends.IS_STAR_FRIEND)));
	// o.setInBlackTime(c.getInt(c
	// .getColumnIndex(TxDB.TX_Friends.IN_BLACK_TIME)));
	// return o;
	// }
	//
	// public ArrayList<TxInfor> fetchAllTxInfors(Cursor c) {
	// ArrayList<TxInfor> ret = new ArrayList<TxInfor>();
	// if (c != null) {
	// while (c.moveToNext()) {
	// ret.add(fetchOneTxInfor(c));
	// }
	// c.close();
	// }
	//
	// return ret;
	// }
	//
	// // 更新好友的字段
	// public TxInfor updateTXInforByValues(long partner_id,
	// ContentValues values) {
	//
	// TxInfor tinfor = mTxInforCacheMap.get(partner_id);
	//
	// if (tinfor != null) {
	// Set<Entry<String, Object>> valueSet = values.valueSet();
	// Iterator<Entry<String, Object>> txIterator = valueSet
	// .iterator();
	// while (txIterator.hasNext()) {
	// Entry<String, Object> tbEntry = txIterator.next();
	// String key = tbEntry.getKey();
	// Object value = tbEntry.getValue();
	// if (key.equals(TxDB.TX_Friends.TX_TYPE)) {
	// // 联系人类型
	// tinfor.setTx_type((Integer) value);
	//
	// } else if (key.equals(TxDB.TX_Friends.CONTACTS_PERSON_NAME)) {
	// // 本地通讯录显示名称
	// tinfor.setContacts_person_name((String) value);
	//
	// } else if (key.equals(TxDB.TX_Friends.TX_ID)) {
	// // 神聊号
	// tinfor.setPartner_id((Long) value);
	//
	// } else if (key.equals(TxDB.TX_Friends.IS_STAR_FRIEND)) {
	// // 是否是星标好友， 1：是，0:不是
	// tinfor.setStarFriend((Integer) value);
	//
	// } else if (key.equals(TxDB.TX_Friends.REMARK_NAME)) {
	// // 备注名
	// tinfor.setRemarkName((String) value);
	// } else if (key.equals(TxDB.TX_Friends.IN_BLACK_TIME)) {
	// // 备注名
	// tinfor.setInBlackTime((Long) value);
	//
	// }
	// }
	// // 缓存更新完毕，开始更新数据库
	// updateTxInforByTXId(values, tinfor.getPartner_id());
	//
	// }
	// return tinfor;
	//
	// }
	//
	// /** 根据条件更新数据库TX */
	// private int updateTxInforByTXId(ContentValues values, long partnerId) {
	// int rows = mTxCr.update(TxDB.TX_Friends.CONTENT_URI, values,
	// TxDB.Tx.TX_ID + "=?", new String[] { "" + partnerId });
	// return rows;
	// }
	//
	// }

}
