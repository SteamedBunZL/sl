package com.tuixin11sms.tx.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class Email implements Parcelable{
	public static final int TYPE_CUSTOM = 0;
	public static final int TYPE_HOME = 1;
    public static final int TYPE_WORK = 2;
    public static final int TYPE_OTHER = 3;
    public static final int TYPE_MOBILE = 4;
	private String address;
	private int type;
	public Email(String a, int t) {
		this.address = a;
		this.type = t;
	}
	public Email(){
		this.address = "";
		this.type = 0;
	}
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public int getType() {
		return type;
	}

	public void setType(int t) {
		this.type = t;
	}

	
	public String toString() {
		return String.format("@%s: %s", type,address);
	}
	public static final Parcelable.Creator<Email> CREATOR = new Parcelable.Creator<Email>() {
		public Email createFromParcel(Parcel in) {
			return new Email(in);
		}

		public Email[] newArray(int size) {
			return new Email[size];
		}
	};

	private Email(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}


	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(address);

	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		address = in.readString();
	}
}