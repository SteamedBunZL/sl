package com.tuixin11sms.tx.contact;

import android.os.Parcel;
import android.os.Parcelable;

import com.tuixin11sms.tx.utils.Utils;

public class Address implements Parcelable {
	public static final int TYPE_CUSTOM = 0;
	public static final int TYPE_HOME = 1;
	public static final int TYPE_WORK = 2;
	public static final int TYPE_OTHER = 3;
	private String poBox;
	private String street;
	private String city;
	private String state;
	private String postalCode;
	private String country;
	private int type;
	private String asString;

	public Address(String asString, int type) {
		this.asString = asString;
		this.type = type;
		poBox = "";
		street = "";
		city = "";
		state = "";
		postalCode = "";
		country = "";
	}

	public Address() {
		this.asString = "";
		this.type = 0;
		poBox = "";
		street = "";
		city = "";
		state = "";
		postalCode = "";
		country = "";
	}

	public Address(String poBox, String street, String city, String state,
			String postalCode, String country, int type) {
		this.asString = poBox + ";" + street + ";" + city + ";" + state + ";"
		+ postalCode + ";" + country;
		this.type = type;
		this.poBox = poBox;
		this.street = street;
		this.city = city;
		this.state = state;
		this.postalCode = postalCode;
		this.country = country;
	}

	public String getAddr() {
		if (Utils.isNull(asString)) {
			asString = poBox + ";" + street + ";" + city + ";" + state + ";"
					+ postalCode + ";" + country;
		}
		return asString;
	}

	public void setAddr(String addr) {
		this.asString = addr;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getPoBox() {
		return poBox;
	}

	public void setPoBox(String poBox) {
		this.poBox = poBox;
	}

	public String getStreet() {
		return street;
	}

	public void setStreet(String street) {
		this.street = street;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getState() {
		return state;
	}

	public void setState(String state) {
		this.state = state;
	}

	public String getPostalCode() {
		return postalCode;
	}

	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String toString() {
		if (this.asString.length() > 0) {
			return (this.asString);
		} else {
			String addr = "";
			if (this.getPoBox() != null) {
				addr = addr + this.getPoBox() + "n";
			}
			if (this.getStreet() != null) {
				addr = addr + this.getStreet() + "n";
			}
			if (this.getCity() != null) {
				addr = addr + this.getCity() + ", ";
			}
			if (this.getState() != null) {
				addr = addr + this.getState() + " ";
			}
			if (this.getPostalCode() != null) {
				addr = addr + this.getPostalCode() + " ";
			}
			if (this.getCountry() != null) {
				addr = addr + this.getCountry();
			}
			return (addr);
		}
	}

	public static final Parcelable.Creator<Address> CREATOR = new Parcelable.Creator<Address>() {
		public Address createFromParcel(Parcel in) {
			return new Address(in);
		}

		public Address[] newArray(int size) {
			return new Address[size];
		}
	};

	private Address(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}
	public void writeToParcel(Parcel out, int flags) {		
		out.writeInt(type);
		out.writeString(asString);
		out.writeString(poBox);
		out.writeString(street);
		out.writeString(city);
		out.writeString(state);
		out.writeString(postalCode);
		out.writeString(country);
	}
	public void readFromParcel(Parcel in) {	
		type=in.readInt();
		asString = in.readString();
		poBox = in.readString();
		street = in.readString();
		city = in.readString();
		state = in.readString();
		postalCode = in.readString();
		country = in.readString();
	}
}
