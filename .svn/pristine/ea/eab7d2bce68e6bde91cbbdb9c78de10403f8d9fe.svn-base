package com.tuixin11sms.tx.contact;

import android.os.Parcel;
import android.os.Parcelable;

public class Organization implements Parcelable{
	private String organization = "";
	private String title = "";
	private int type;
	public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_WORK = 1;
    public static final int TYPE_OTHER = 2;
	public String getOrganization() {
		return organization;
	}

	public void setOrganization(String organization) {
		this.organization = organization;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}
	
	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	
	public Organization() {
		this.organization = "";
		this.title = "";
		this.type = 0;
	}

	public Organization(String org, String title,int type) {
		this.organization = org;
		this.title = title;
		this.type = type;
	}
	public String toString() {
		return String.format("@%s: %s: %s", type,title,organization);
	}
	public static final Parcelable.Creator<Organization> CREATOR = new Parcelable.Creator<Organization>() {
		public Organization createFromParcel(Parcel in) {
			return new Organization(in);
		}

		public Organization[] newArray(int size) {
			return new Organization[size];
		}
	};

	private Organization(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}


	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(title);
		out.writeString(organization);
	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		title = in.readString();
		organization = in.readString();
	}
}