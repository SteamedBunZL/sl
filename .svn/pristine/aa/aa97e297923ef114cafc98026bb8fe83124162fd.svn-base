package com.tuixin11sms.tx.contact;
import android.os.Parcel;
import android.os.Parcelable;

public class Phone implements Parcelable{
	private String number;
	private int type;
	public static final int TYPE_CUSTOM = 0;
    public static final int TYPE_HOME = 1;
    public static final int TYPE_MOBILE = 2;
    public static final int TYPE_WORK = 3;
    public static final int TYPE_FAX_WORK = 4;
    public static final int TYPE_FAX_HOME = 5;
    public static final int TYPE_PAGER = 6;
    public static final int TYPE_OTHER = 7;
    public static final int TYPE_CALLBACK = 8;
    public static final int TYPE_CAR = 9;
    public static final int TYPE_COMPANY_MAIN = 10;
    public static final int TYPE_ISDN = 11;
    public static final int TYPE_MAIN = 12;
    public static final int TYPE_OTHER_FAX = 13;
    public static final int TYPE_RADIO = 14;
    public static final int TYPE_TELEX = 15;
    public static final int TYPE_TTY_TDD = 16;
    public static final int TYPE_WORK_MOBILE = 17;
    public static final int TYPE_WORK_PAGER = 18;
    public static final int TYPE_ASSISTANT = 19;
    public static final int TYPE_MMS = 20;
	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
	public Phone() {
		this.number = "";
		this.type = 0;
	}
	public Phone(String n, int t) {
		this.number = n;
		this.type = t;
	}
	public boolean equals(Object obj){
		if(this == obj)
			return true;
		
		if((obj == null) || (obj.getClass() != this.getClass()))
			return false;
		
		Phone that = (Phone) obj;
		return that.number.equals(number) && that.type==type;
	}
	public String toString() {
		return String.format("@%s: %s", type,number);
	}
	public static final Parcelable.Creator<Phone> CREATOR = new Parcelable.Creator<Phone>() {
		public Phone createFromParcel(Parcel in) {
			return new Phone(in);
		}

		public Phone[] newArray(int size) {
			return new Phone[size];
		}
	};

	private Phone(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}


	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(number);

	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		number = in.readString();
	}
}
