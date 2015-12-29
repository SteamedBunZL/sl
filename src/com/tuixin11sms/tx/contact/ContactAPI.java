package com.tuixin11sms.tx.contact;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Observable;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;

/**
 * 操作用户通讯录的抽象类
 */
public abstract class ContactAPI extends Observable {
	public int totalCount;//需要同步的总数？
//	public int SyncCot;//已经同步的总数？
	private static ContactAPI api;
	public static int ADD_CONTACT = 1;
	public static int EDIT_CONTACT = 2;

	/**
	 * 根据用户android版本获取ContactAPI的唯一实例
	 */
	public static ContactAPI getAPI() {
		// 双重检查加锁
		if (api == null) {
			synchronized (ContactAPI.class) {
				if (api == null) {
					api = new ContactAPISdk5();
					//工程要求最低SDK版本为5,所以不要ContactAPISdk3了。
//					String apiClass;
//					if (Integer.parseInt(Build.VERSION.SDK) >= 5) {
//						apiClass = "com.tuixin11sms.tx.contact.ContactAPISdk5";
//					} else {
//						apiClass = "com.tuixin11sms.tx.contact.ContactAPISdk3";
//					}
//					try {
//						// 将apiClass指向的类作为ContactAPI的子类
//						Class<? extends ContactAPI> realClass = Class.forName(apiClass).asSubclass(
//								ContactAPI.class);
//						api = realClass.newInstance();
//					} catch (Exception e) {
//						throw new IllegalStateException(e);
//					}
				}
			}
		}
		return api;
	}

	public abstract int getContactCount(Context ctxt);

	public abstract ArrayList<String> getPhones(String id);

	public abstract void addContact(Context ctxt);

	public abstract void editContact(Context ctxt, long id);

	public abstract void editContact(Context ctxt, String name);

	public abstract Intent getContactIntent();

	public abstract Uri getContactUri();

	public abstract void lookContact(Context ctxt, long id);

	public abstract String[] getIDAndNameByPhone(String phone);

	public abstract List<String> newContactIdList();

	public abstract Contact getContact(Context ctxt, Uri uri);

	//public abstract ContactList newContactList(Context ctxt);

	public abstract Contact newContact(String id, Context ctxt);

	public abstract Contact newContact(Cursor c, Context ctxt);

	//public abstract ContactList newContactSimplifiedList(Context ctxt);

	public abstract boolean addTxToPhone(String id, String txId, String phone, String txName,
			Bitmap bitmap);

	public abstract Bitmap getContactPhoto(Context ctxt, long personId, Integer defaultResource);

	public abstract void setContactPhoto(ContentResolver c, byte[] bytes, long personId);

	public abstract ContentResolver getCr();

	public abstract void setCr(ContentResolver cr);

	public abstract void delPhone(Context ctxt, String phone, String id);

	public abstract void fillAllContacts(ContentResolver ctr, HashMap<Long, String> contacts);

	public abstract ArrayList<Phone> getPhoneNumbers(String id);

	public abstract boolean insert(Contact contact);

	public abstract boolean insert(Context ctxt, String phone);

	public abstract boolean insert(Context ctxt, String phone, String name);
}