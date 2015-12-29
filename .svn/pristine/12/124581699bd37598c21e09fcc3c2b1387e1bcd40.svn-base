package com.tuixin11sms.tx.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class Website implements Parcelable{
	public static final int TYPE_HOMEPAGE = 1;
    public static final int TYPE_BLOG = 2;
    public static final int TYPE_PROFILE = 3;
    public static final int TYPE_HOME = 4;
    public static final int TYPE_WORK = 5;
    public static final int TYPE_FTP = 6;
    public static final int TYPE_OTHER = 7;
    private String url;
	private int type;
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public Website() {
		this.url = "";
		this.type = 0;
	}
	public Website(String url, int type) {
		this.url = url;
		this.type = type;
	}
	public String toString() {
		return String.format("@%s: %s", type,url);
	}
	public static final Parcelable.Creator<Website> CREATOR = new Parcelable.Creator<Website>() {
		public Website createFromParcel(Parcel in) {
			return new Website(in);
		}

		public Website[] newArray(int size) {
			return new Website[size];
		}
	};

	private Website(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}


	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(url);

	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		url = in.readString();
	}
}
