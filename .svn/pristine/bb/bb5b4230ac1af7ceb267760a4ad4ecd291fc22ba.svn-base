package com.tuixin11sms.tx.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * 聊天频道对象
 * 
 * @author 郭伟洲
 * 
 */
public class ChatChannel implements Parcelable, Comparable<ChatChannel> {
	/**
	 * 频道Id
	 */
	private int channelId = -1;
	/**
	 * 频道版本
	 */
	private int channelVer = -1;
	/**
	 * 频道当前人数
	 */
	private int peopleNum = 0;
	/**
	 * 是否加入此频道
	 */
	private boolean isJoin;
	/**
	 * 频道主题
	 */
	private String subject;
	/**
	 * 频道内容
	 */
	private String content;
	/**
	 * 频道图像
	 */
	private String iconUrl;
	/**
	 * 默认接听范围
	 */
	private int range;
	/**
	 * 频道属性
	 */
	private int prop;
	/**
	 * 频道是否已关闭
	 */
	private boolean isClosed;
	/**
	 * 频道创建者ID, -1表示系统频道
	 */
	private int createUid = -1;
	/**
	 * 访问时间
	 */
	private long accessTime;
	/**
	 * 自建频道的创建时间
	 */
	private long createTime;
	/**
	 * 频道通知
	 */
	private String notice;
	
	public String getNotice() {
		return notice;
	}

	public void setNotice(String notice) {
		this.notice = notice;
	}

	public long getCreateTime() {
		return createTime;
	}

	public void setCreateTime(long createTime) {
		this.createTime = createTime;
	}

	public long getAccessTime() {
		return accessTime;
	}

	public void setAccessTime(long accessTime) {
		this.accessTime = accessTime;
	}

	public int getCreateUid() {
		return createUid;
	}

	public void setCreateUid(int createUid) {
		this.createUid = createUid;
	}

	public boolean isClosed() {
		return isClosed;
	}

	public void setClosed(boolean isClosed) {
		this.isClosed = isClosed;
	}

	public ChatChannel() {

	}

	private ChatChannel(Parcel parcel) {
		readFromParcel(parcel);
	}

	public int getChannelId() {
		return channelId;
	}

	public void setChannelId(int channelId) {
		this.channelId = channelId;
	}

	public int getChannelVer() {
		return channelVer;
	}

	public void setChannelVer(int channelVer) {
		this.channelVer = channelVer;
	}

	public int getPeopleNum() {
		return peopleNum;
	}

	public void setPeopleNum(int peopleNum) {
		this.peopleNum = peopleNum;
	}

	public boolean isJoin() {
		return isJoin;
	}

	public void setJoin(boolean isJoin) {
		this.isJoin = isJoin;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getIconUrl() {
		return iconUrl;
	}

	public void setIconUrl(String iconUrl) {
		this.iconUrl = iconUrl;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public int getProp() {
		return prop;
	}

	public void setProp(int prop) {
		this.prop = prop;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(channelId);
		out.writeInt(channelVer);
		out.writeInt(peopleNum);
		boolean[] bool = new boolean[1];
		bool[0] = isJoin;
		out.writeBooleanArray(bool);
		out.writeString(subject);
		out.writeString(content);
		out.writeString(iconUrl);
		out.writeInt(range);
		out.writeInt(prop);
		bool[0] = isClosed;
		out.writeBooleanArray(bool);
		out.writeInt(createUid);
		out.writeLong(accessTime);
		out.writeLong(createTime);
		out.writeString(notice);
	}

	public static final Parcelable.Creator<ChatChannel> CREATOR = new Parcelable.Creator<ChatChannel>() {
		public ChatChannel createFromParcel(Parcel in) {
			return new ChatChannel(in);
		}

		public ChatChannel[] newArray(int size) {
			return new ChatChannel[size];
		}
	};

	private void readFromParcel(Parcel parcel) {
		channelId = parcel.readInt();
		channelVer = parcel.readInt();
		peopleNum = parcel.readInt();
		boolean[] bool = new boolean[1];
		parcel.readBooleanArray(bool);
		isJoin = bool[0];
		subject = parcel.readString();
		content = parcel.readString();
		iconUrl = parcel.readString();
		range = parcel.readInt();
		prop = parcel.readInt();
		parcel.readBooleanArray(bool);
		isClosed = bool[0];
		createUid = parcel.readInt();
		accessTime = parcel.readLong();
		createTime = parcel.readLong();
		notice = parcel.readString();
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof ChatChannel)) {
			return false;
		}
		ChatChannel cc = (ChatChannel) obj;
		if (cc.channelId != channelId) {
			return false;
		}
		return true;
	}

	@Override
	public int compareTo(ChatChannel cc) {
		return channelId - cc.channelId;
	}
	
	@Override
	public String toString() {
		if (createUid == -1) {
			return "";
		}
		return "{channelId = " + channelId + ", uid = "  + createUid + "}";
	}
}
