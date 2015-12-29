package com.tuixin11sms.tx.contact;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.tuixin11sms.tx.data.TxDB;

/** 和用户账户相关对象，存好友或者黑名单用户 */
public class TxInfor implements Parcelable {

	public static final int TX_TYPE_DEFAULT = 1;// 陌生人
	public static final int TX_TYPE_TB = 2;// 好友
	public static final int TX_TYPE_BLACK = 4;// 黑名单

	public static final int TX_NOT_IN_BLACK_LIST = -1;// -1 正常
	public static final int TX_IN_MY_BLACK_LIST = 0;// 0 你加对方入黑名单
	public static final int TX_ME_IN_BLACK_LIST = 1;// 1 你被对方加黑名单

	public static final int TX_STAR_FRIEND = 1;// 星标好友
	public static final int TX_COMMON_FRIEND = 0;// 普通好友
	public static final int TX_NOT_FRIEND = -1;// 非好友

	private long partner_id;
	private int tx_type = TX_TYPE_DEFAULT;// 该初值不要改变
	private String contacts_person_name;
	private int starFriend = TX_NOT_FRIEND;// -1非好友，0普通好友，1星标好友
	// 备注名
	private String remarkName;
	private long inBlackTime;

	// private TX txNormal;// 普通用户信息

	public TxInfor(long partner_id, int tx_type) {

		if (tx_type == TX_TYPE_TB) {
			// 好友
			setTBType();
		} else if (tx_type == TX_TYPE_BLACK) {
			// 黑名单
			setBlackType();
		} else {
			// 不是上面类型之一，则直接设置类型值，从数据库中直接读取的类型值
			this.tx_type = tx_type;
		}

		this.partner_id = partner_id;
		this.contacts_person_name = "";
		this.remarkName = "";
		this.inBlackTime = 0;
	}

	public long getPartner_id() {
		return partner_id;
	}

	public void setPartner_id(long partner_id) {
		this.partner_id = partner_id;
	}

	public void setTx_type(int tx_type) {
		this.tx_type = tx_type;
	}

	public String getContacts_person_name() {
		return contacts_person_name;
	}

	public void setContacts_person_name(String contacts_person_name) {
		this.contacts_person_name = contacts_person_name;
	}

	public int getStarFriend() {
		return starFriend;
	}

	public void setStarFriend(int starFriend) {
		this.starFriend = starFriend;
	}

	public String getRemarkName() {
		return remarkName;
	}

	public void setRemarkName(String remarkName) {
		this.remarkName = remarkName;
	}

	public long getInBlackTime() {
		return inBlackTime;
	}

	public void setInBlackTime(long inBlackTime) {
		this.inBlackTime = inBlackTime;
	}

	public ContentValues txinforToValues() {
		ContentValues values = new ContentValues();
		values.put(TxDB.TX_Friends.TX_ID, partner_id);
		values.put(TxDB.TX_Friends.TX_TYPE, tx_type);
		values.put(TxDB.TX_Friends.CONTACTS_PERSON_NAME, contacts_person_name);
		values.put(TxDB.TX_Friends.REMARK_NAME, remarkName);
		values.put(TxDB.TX_Friends.IS_STAR_FRIEND, starFriend);
		values.put(TxDB.TX_Friends.IN_BLACK_TIME, inBlackTime);
		return values;
	}

	// 清除所有属性
	public void clearAttrs() {
		this.starFriend = TX_NOT_FRIEND;
		clearAllType();
		this.contacts_person_name = "";
		this.remarkName = "";
		this.inBlackTime = 0;

	}

	// 是否是好友
	public boolean isTBType() {
		return (tx_type & TX_TYPE_TB) != 0;
	}

	// 是否是黑名单
	public boolean isBlackType() {
		return (tx_type & TX_TYPE_BLACK) != 0;
	}

	// 设置为好友
	public void setTBType() {
		tx_type |= TX_TYPE_TB;
		clearBlackType();// 是好友了就不应该再是黑名单了
		starFriend = TX_COMMON_FRIEND;
	}

	// 设置为黑名单
	public void setBlackType() {
		tx_type |= TX_TYPE_BLACK;
		starFriend = TX_NOT_FRIEND;
	}

	// 清除好友标记
	public void clearTBType() {
		tx_type &= ~TX_TYPE_TB;
	}

	// 清除黑名单
	public void clearBlackType() {
		tx_type &= ~TX_TYPE_BLACK;
		inBlackTime = 0;// 重置进入黑名单时间
	}

	// 清除好友和黑名单标记
	public void clearAllType() {
		clearTBType();
		clearBlackType();
	}

	public static final Parcelable.Creator<TxInfor> CREATOR = new Parcelable.Creator<TxInfor>() {
		public TxInfor createFromParcel(Parcel in) {
			return new TxInfor(in);
		}

		public TxInfor[] newArray(int size) {
			return new TxInfor[size];
		}
	};

	private TxInfor(Parcel in) {
		readFromParcel(in);
	}

	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeLong(partner_id);
		out.writeInt(tx_type);
		out.writeString(contacts_person_name);
		out.writeLong(inBlackTime);
		out.writeString(remarkName);
		out.writeInt(starFriend);
	}

	public void readFromParcel(Parcel in) {
		partner_id = in.readLong();
		tx_type = in.readInt();
		contacts_person_name = in.readString();
		inBlackTime = in.readLong();
		remarkName = in.readString();
		starFriend = in.readInt();

	}

	@Override
	public String toString() {
		return "TxInfor [partner_id=" + partner_id + ", tx_type=" + tx_type
				+ ", contacts_person_name=" + contacts_person_name
				+ ", starFriend=" + starFriend + ", remarkName=" + remarkName
				+ ", inBlackTime=" + inBlackTime + "]";
	}
	

}
