package com.tuixin11sms.tx.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class IM implements Parcelable{
	private String name;
	private int type;
	public static final int PROTOCOL_CUSTOM = -1;
	public static final int PROTOCOL_AIM = 0;
	public static final int PROTOCOL_MSN = 1;
	public static final int PROTOCOL_YAHOO = 2;
	public static final int PROTOCOL_SKYPE = 3;
	public static final int PROTOCOL_QQ = 4;
	public static final int PROTOCOL_GOOGLE_TALK = 5;
	public static final int PROTOCOL_ICQ = 6;
	public static final int PROTOCOL_JABBER = 7;
	public static final int PROTOCOL_NETMEETING = 8;
	public IM(String name, int type) {
		this.name = name;
		this.type = type;
	}
	public IM(){
		this.name = "";
		this.type = 0;
	}
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	
	public String toString() {
		return String.format("@%s: %s", type,name);
	}
	public static final Parcelable.Creator<IM> CREATOR = new Parcelable.Creator<IM>() {
		public IM createFromParcel(Parcel in) {
			return new IM(in);
		}

		public IM[] newArray(int size) {
			return new IM[size];
		}
	};

	private IM(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}


	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(name);

	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		name = in.readString();
	}
}
