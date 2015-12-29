package com.tuixin11sms.tx.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuixin11sms.tx.message.TXMessage;

/**
 * 通过LBS找到的神聊用户信息
 * 
 * @author 郭伟洲
 * 
 */
public class LBSUserInfo implements Parcelable {
	private int uid;
	private double lat;
	private double lng;
	private int dist;
	private String nickName;
	private int sex;
	private String avatar;
	private String signature;
	private int age;
	private String area;
	private TXMessage txMsg;

	public LBSUserInfo() {
		super();
	}

	public LBSUserInfo(int uid, double lat, double lng, int dist,
			String nickName, int sex, String avatar, String signature, int age,
			String area) {
		super();
		this.uid = uid;
		this.lat = lat;
		this.lng = lng;
		this.dist = dist;
		this.nickName = nickName;
		this.sex = sex;
		this.avatar = avatar;
		this.signature = signature;
		this.age = age;
		this.area = area;
	}

	public int getUid() {
		return uid;
	}

	public void setUid(int uid) {
		this.uid = uid;
	}

	public double getLat() {
		return lat;
	}

	public void setLat(double lat) {
		this.lat = lat;
	}

	public double getLng() {
		return lng;
	}

	public void setLng(double lng) {
		this.lng = lng;
	}

	public int getDist() {
		return dist;
	}

	public void setDist(int dist) {
		this.dist = dist;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}
	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Override
	public String toString() {
		return "uid:" + getUid() + "|nickname:" + getNickName() + "|sex:"
				+ getSex() + "|avatar:" + getAvatar() + "|lat:" + getLat()
				+ "|lng:" + getLng() + "|dist:" + getDist() + "|signature:"
				+ getSignature()+"|age:" + getAge()+"|area:" + getArea();
	}

	public static final Parcelable.Creator<LBSUserInfo> CREATOR = new Parcelable.Creator<LBSUserInfo>() {
		public LBSUserInfo createFromParcel(Parcel in) {
			return new LBSUserInfo(in);
		}

		public LBSUserInfo[] newArray(int size) {
			return new LBSUserInfo[size];
		}
	};

	private LBSUserInfo(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		//
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(uid);
		out.writeDouble(lat);
		out.writeDouble(lng);
		out.writeInt(dist);
		out.writeString(nickName);
		out.writeInt(sex);
		out.writeString(avatar);
		out.writeString(signature);
		out.writeInt(age);
		out.writeString(area);
	}

	public void readFromParcel(Parcel in) {
		uid = in.readInt();
		lat = in.readDouble();
		lng = in.readDouble();
		dist = in.readInt();
		nickName = in.readString();
		sex = in.readInt();
		avatar = in.readString();
		signature = in.readString();
		age = in.readInt();
		area = in.readString();
	}

	public TXMessage getTxMsg() {
		return txMsg;
	}

	public void setTxMsg(TXMessage txMsg) {
		this.txMsg = txMsg;
	}

	@Override
	public int hashCode() {
		return new Integer(uid).hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}
		if (!(o instanceof LBSUserInfo)) {
			return false;
		}
		LBSUserInfo user = (LBSUserInfo) o;
		if (uid != user.uid) {
			return false;
		}
		return true;
	}
}
