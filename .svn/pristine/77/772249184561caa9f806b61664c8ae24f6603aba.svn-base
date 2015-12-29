package com.tuixin11sms.tx.contact;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Note;
import android.provider.ContactsContract.CommonDataKinds.StructuredName;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Log;

import com.tuixin11sms.tx.utils.Utils;

public class ContactAPISdk5 extends ContactAPI {
	private static final String TAG = "ContactAPISdk5";
	// String TAG = ContactAPISdk5.class.getSimpleName();
	// private Cursor cur;
	private ContentResolver cr;

	// public Cursor getCur() {
	// return cur;
	// }
	//
	// public void setCur(Cursor cur) {
	// this.cur = cur;
	// }
	public Bitmap getContactPhoto(Context ctxt, long personId,
			Integer defaultResource) {
		Uri contactUri = ContentUris.withAppendedId(
				ContactsContract.Contacts.CONTENT_URI, personId);
		getContactPhoto(ctxt, contactUri, defaultResource);
		return getContactPhoto(ctxt, contactUri, defaultResource);
	}

	public Bitmap getContactPhoto(Context ctxt, Uri uri, Integer defaultResource) {
		Bitmap img = null;
		InputStream s = ContactsContract.Contacts.openContactPhotoInputStream(
				ctxt.getContentResolver(), uri);
		if (null != s) {
			int len = 0;
			try {
				len = s.available();
				if (Utils.debug)
					Log.i(TAG,"============++len++" + len);
			} catch (IOException e1) {
				//
				e1.printStackTrace();
			}
			BitmapFactory.Options opts = new BitmapFactory.Options();
			opts.inPreferredConfig = Bitmap.Config.RGB_565;
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			if (len < 20480) { // 0-20k
				opts.inSampleSize = 1;
			} else if (len < 51200) { // 20-50k
				opts.inSampleSize = 2;
			} else if (len < 307200) { // 50-300k
				opts.inSampleSize = 4;
			} else if (len < 819200) { // 300-800k
				opts.inSampleSize = 6;
			} else if (len < 1048576) { // 800-1024k
				opts.inSampleSize = 8;
			} else if (len < 1048576 * 2) { // 1024-1024k
				opts.inSampleSize = 9;
			} else {
				opts.inSampleSize = 10;
			}
			img = BitmapFactory.decodeStream(s, null, opts);
			try {
				if (s != null)
					s.close();
			} catch (IOException e) {
				if(Utils.debug)e.printStackTrace();
			}
			if (img != null) {
				// Log.d("Contacts5", "Success! Photo Bitmap ready to load");
				WeakReference<Bitmap> wref = new WeakReference<Bitmap>(img);
				img = null;
				return wref.get();
			} else {
				// img = BitmapFactory.decodeResource(ctxt.getResources(),
				// defaultResource);
				// WeakReference<Bitmap> wref=new WeakReference<Bitmap>(img);
				// img=null;
				return null;
			}
		}
		return null;
	}

	public void setContactPhoto(ContentResolver c, byte[] bytes, long personId) {
		ContentValues values = new ContentValues();
		int photoRow = -1;
		String where = ContactsContract.Data.RAW_CONTACT_ID + " = " + personId
				+ " AND " + ContactsContract.Data.MIMETYPE + "=='"
				+ ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE
				+ "'";
		Cursor cursor = c.query(ContactsContract.Data.CONTENT_URI, null, where,
				null, null);
		if (cursor != null) {
			int idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Data._ID);
			if (cursor.moveToFirst()) {
				photoRow = cursor.getInt(idIdx);
			}
			cursor.close();
		}
		values.put(ContactsContract.Data.RAW_CONTACT_ID, personId);
		values.put(ContactsContract.Data.IS_SUPER_PRIMARY, 1);
		values.put(ContactsContract.CommonDataKinds.Photo.PHOTO, bytes);
		values.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

		if (photoRow >= 0) {
			c.update(ContactsContract.Data.CONTENT_URI, values,
					ContactsContract.Data._ID + " = " + photoRow, null);
		} else {
			c.insert(ContactsContract.Data.CONTENT_URI, values);
		}
	}

	public ContentResolver getCr() {
		return cr;
	}

	public void setCr(ContentResolver cr) {
		this.cr = cr;
	}

	public Intent getContactIntent() {
		return (new Intent(Intent.ACTION_PICK,
				ContactsContract.Contacts.CONTENT_URI));
	}

	public Uri getContactUri() {
		//
		return ContactsContract.Contacts.CONTENT_URI;
	}

	public boolean addTxToPhone(String id, String txId, String phone,
			String txName, Bitmap bitmap) {
		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentValues contentValues = new ContentValues();
		contentValues.put(ContactsContract.Data.RAW_CONTACT_ID, id);
		contentValues.put(ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
		contentValues.put(
				android.provider.ContactsContract.CommonDataKinds.Event.LABEL,
				phone);
		contentValues
				.put(android.provider.ContactsContract.CommonDataKinds.Event.TYPE,
						0);
		contentValues
				.put(android.provider.ContactsContract.CommonDataKinds.Event.START_DATE,
						"神聊11");
		contentValues.put(Data.IS_PRIMARY, 1);

		operationList.add(ContentProviderOperation
				.newInsert(ContactsContract.Data.CONTENT_URI)
				.withValues(contentValues).build());
		// String account = null;
		// String accountName = null;
		// builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE,
		// account);
		// builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
		// accountName);
		// operationList.add(builder.build());
		// builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
		// builder.withValueBackReference(
		// android.provider.ContactsContract.CommonDataKinds.Im.RAW_CONTACT_ID,
		// 0);//Integer.parseInt(id)
		// builder.withValue(
		// Data.MIMETYPE,
		// android.provider.ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);
		// builder.withValue(
		// android.provider.ContactsContract.CommonDataKinds.Im.LABEL,
		// phone);
		// builder.withValue(
		// android.provider.ContactsContract.CommonDataKinds.Im.TYPE, 0);
		// builder.withValue(
		// android.provider.ContactsContract.CommonDataKinds.Im.DATA,
		// "神聊11");
		// builder.withValue(Data.IS_PRIMARY, 1);
		// operationList.add(builder.build());
		try {
			this.cr.applyBatch(ContactsContract.AUTHORITY, operationList);
		} catch (RemoteException e) {
			if (Utils.debug)
				Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
		} catch (OperationApplicationException e) {
			if (Utils.debug)
				Log.e(TAG, String.format("%s: %s", e.toString(), e.getMessage()));
		}
		return true;
	}

	public ArrayList<String> getPhones(String id) {
		ArrayList<String> phones = new ArrayList<String>();
		Cursor pCur = this.cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
				new String[] { id }, null);
		if (pCur != null) {
			while (pCur.moveToNext()) {
				String phone = pCur
						.getString(pCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				if (!phones.contains(phone))
					phones.add(phone);
			}
			pCur.close();
		}
		return (phones);
	}

	public List<String> newContactIdList() {
		List<String> idList = new ArrayList<String>();
		Cursor cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts._ID }, null, null,
				null);
		String id;
		// Log.i(TAG, "++++++++++++++++++++++++++");
		// for (String str : cur.getColumnNames())
		// Log.i(TAG, str);
		// Log.i(TAG, "-------------------------------");
		if (cur != null && cur.getCount() > 0) {
			int cot = 0;
			while (cur.moveToNext()) {
				id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				// Log.i(TAG, "id:" + id);
				idList.add(id);
				// Log.i(TAG, "cot:" + cot);
				cot++;
			}
			cur.close();
		}
		return idList;
	}

	/**给contacts填充所有的联系人*/
	public void fillAllContacts(ContentResolver ctr, HashMap<Long, String> contacts) {
		Cursor pCur = ctr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER}, null,
				null, null);
				while (pCur != null && pCur.moveToNext()) {
					String name = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					String phone = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
					String phoneNum = Utils.filterNumber(phone);
					if (!TextUtils.isEmpty(phoneNum)) {
						//电话号码不为空
						contacts.put(Long.parseLong(phoneNum), name);
					}
				}
			pCur.close();

	}
	

	public Contact newContact(Cursor cur, Context ctxt) {
		Contact c = new Contact(ctxt);
		String id;
		int cot = 0;
		if (cur != null && cur.getCount() > 0) {

			while (cur.moveToNext()) {
				id = cur.getString(cur
						.getColumnIndex(ContactsContract.Contacts._ID));
				// Log.i(TAG,
				// cur.getString(cur
				// .getColumnIndex(ContactsContract.Contacts._ID))
				// + "id:" + id);
				c.setId(id);
				String displayName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				// Log.i(TAG, "displayName:" + displayName);
				c.setDisplayName(displayName);
				boolean hasPhone = Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0;
				// Log.i(TAG, "hasPhone:" + hasPhone);
				c.setHasPhone(hasPhone);
				if (hasPhone) {
					c.setPhone(this.getPhoneNumbers(id));
				}
				c.setEmail(this.getEmailAddresses(id));
				c.setNotes(this.getContactNotes(id));
				c.setAddresses(this.getContactAddresses(id));
				c.setImAddresses(this.getIM(id));
				c.setOrganizations(this.getContactOrg(id));
				c.setWebsites(this.getWebsite(id));
				// Log.i(TAG, "cot:" + cot);
				cot++;
			}
			cur.close();
		}

		return c;
	}

	public Contact newContact(String id, Context ctxt) {
		Cursor cur;
		String where = ContactsContract.Contacts._ID + " = ?";

		cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI, null, where,
				new String[] { id }, null);
		Contact c = new Contact(ctxt);
		int cot = 0;
		if (cur != null && cur.getCount() > 0) {
			while (cur.moveToNext()) {
				// id = cur.getString(cur
				// .getColumnIndex(ContactsContract.Contacts._ID));
				// Log.i(TAG,
				// cur.getString(cur
				// .getColumnIndex(ContactsContract.Contacts._ID))
				// + "id:" + id);
				c.setId(id);
				String displayName = cur
						.getString(cur
								.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
				// Log.i(TAG, "displayName:" + displayName);
				c.setDisplayName(displayName);
				boolean hasPhone = Integer
						.parseInt(cur.getString(cur
								.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0;
				// Log.i(TAG, "hasPhone:" + hasPhone);
				c.setHasPhone(hasPhone);
				if (hasPhone) {
					c.setPhone(this.getPhoneNumbers(id));
				}
				c.setEmail(this.getEmailAddresses(id));
				c.setNotes(this.getContactNotes(id));
				c.setAddresses(this.getContactAddresses(id));
				c.setImAddresses(this.getIM(id));
				c.setOrganizations(this.getContactOrg(id));
				c.setWebsites(this.getWebsite(id));
				// Log.i(TAG, "cot:" + cot);
				cot++;
			}
			cur.close();
		}
		return c;
	}


	public ArrayList<Phone> getPhoneNumbers(String id) {
		ArrayList<Phone> phones = new ArrayList<Phone>();
		Cursor pCur = this.cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
				new String[] { id }, null);
		if (pCur != null && pCur.getCount() > 0) {
			while (pCur.moveToNext()) {
				String number = pCur
						.getString(pCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				// Log.i("TAG", "number:" + number);
				int type = pCur
						.getInt(pCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				// Log.i("TAG", "type:" + type);
				if (number != null) {
					Phone phone = new Phone(number, type);
					if (!phones.contains(phone)) {
						// BLog.i(TAG, "add:"+phone.toString());
						phones.add(phone);
					}
				}
			}
		}

		pCur.close();
		return (phones);
	}

	public ArrayList<Email> getEmailAddresses(String id) {
		ArrayList<Email> emails = new ArrayList<Email>();

		Cursor emailCur = this.cr.query(
				ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Email.CONTACT_ID + " = ?",
				new String[] { id }, null);
		if (emailCur != null && emailCur.getCount() > 0) {
			while (emailCur.moveToNext()) {
				// This would allow you get several email addresses
				String data = emailCur
						.getString(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
				// Log.i(TAG, "data:" + data);
				int type = emailCur
						.getInt(emailCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
				// Log.i(TAG, "type:" + type);
				Email e = new Email(data, type);
				emails.add(e);
			}
		}

		emailCur.close();
		return (emails);
	}

	public ArrayList<String> getContactNotes(String id) {
		ArrayList<String> notes = new ArrayList<String>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] { id,
				ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
		Cursor noteCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null,
				where, whereParameters, null);
		if (noteCur != null && noteCur.getCount() > 0) {
			// if (noteCur.moveToFirst()) {
			while (noteCur.moveToNext()) {
				String note = noteCur
						.getString(noteCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Note.NOTE));
				// Log.i(TAG, "note:" + note);
				if (note.length() > 0) {
					notes.add(note);
				}
			}
		}

		noteCur.close();
		return (notes);
	}

	public ArrayList<Address> getContactAddresses(String id) {
		ArrayList<Address> addrList = new ArrayList<Address>();

		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] {
				id,
				ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE };

		Cursor addrCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null,
				where, whereParameters, null);
		if (addrCur != null && addrCur.getCount() > 0) {
			while (addrCur.moveToNext()) {
				String poBox = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POBOX));
				// Log.i(TAG, "poBox:" + poBox);
				String street = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.STREET));
				// Log.i(TAG, "street:" + street);
				String city = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CITY));
				// Log.i(TAG, "city:" + city);
				String state = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.REGION));
				// Log.i(TAG, "state:" + state);
				String postalCode = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.POSTCODE));
				// Log.i(TAG, "postalCode:" + postalCode);
				String country = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.COUNTRY));
				// Log.i(TAG, "country:" + country);
				int type = addrCur
						.getInt(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.TYPE));
				// Log.i(TAG, "type:" + type);
				String adds = addrCur
						.getString(addrCur
								.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.DATA1));
				// Log.i(TAG, "adds:" + adds);
				Address a = new Address(poBox, street, city, state, postalCode,
						country, type);
				a.setAddr(adds);
				addrList.add(a);
			}
		}

		addrCur.close();
		return (addrList);
	}

	public ArrayList<Website> getWebsite(String id) {
		ArrayList<Website> websiteList = new ArrayList<Website>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] { id,
				ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE };

		Cursor websiteCur = this.cr.query(ContactsContract.Data.CONTENT_URI,
				null, where, whereParameters, null);
		if (websiteCur != null && websiteCur.getCount() > 0) {
			// if (imCur.moveToFirst()) {
			// Log.i(TAG, "++++++++++++++++++++++++++++++++++++++++++");
			// for(String str:websiteCur.getColumnNames()){
			// Log.i(TAG, str);
			// }
			// Log.i(TAG, "------------------------------------------");
			while (websiteCur.moveToNext()) {
				String websiteName = websiteCur
						.getString(websiteCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
				// Log.i(TAG, "website:" + websiteName);
				String websiteType;
				websiteType = websiteCur
						.getString(websiteCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Website.TYPE));
				// Log.i(TAG, "websiteType:" + websiteType);
				if (websiteName.length() > 0) {
					if (websiteType == null) {
						continue;
					}
					Website im = new Website(websiteName,
							Integer.parseInt(websiteType));
					websiteList.add(im);
				}
			}
		}

		websiteCur.close();
		return (websiteList);
	}

	public ArrayList<IM> getIM(String id) {
		ArrayList<IM> imList = new ArrayList<IM>();
		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] { id,
				ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE };

		Cursor imCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null,
				where, whereParameters, null);
		if (imCur != null && imCur.getCount() > 0) {
			// if (imCur.moveToFirst()) {
			while (imCur.moveToNext()) {
				String imName = imCur
						.getString(imCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Im.DATA));
				// Log.i(TAG, "imName:" + imName);
				String imType;
				imType = imCur
						.getString(imCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Im.PROTOCOL));
				// Log.i(TAG, "imType:" + imType);
				if (imName.length() > 0) {
					if (imType == null) {
						continue;
					}
					IM im = new IM(imName, Integer.parseInt(imType));
					imList.add(im);
				}
			}
		}

		imCur.close();
		return (imList);
	}

	public ArrayList<Organization> getContactOrg(String id) {
		ArrayList<Organization> organizationList = new ArrayList<Organization>();

		String where = ContactsContract.Data.CONTACT_ID + " = ? AND "
				+ ContactsContract.Data.MIMETYPE + " = ?";
		String[] whereParameters = new String[] { id,
				ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE };

		Cursor orgCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null,
				where, whereParameters, null);
		if (orgCur != null && orgCur.getCount() > 0) {
			while (orgCur.moveToNext()) {
				Organization org = new Organization();
				String orgName = orgCur
						.getString(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
				// Log.i(TAG, "orgName:" + orgName);
				String title = orgCur
						.getString(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
				// Log.i(TAG, "title:" + title);
				int type = orgCur
						.getInt(orgCur
								.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TYPE));
				// Log.i(TAG, "type:" + type);
				if (orgName.length() > 0) {
					org.setOrganization(orgName);
					org.setTitle(title);
					org.setType(type);
					organizationList.add(org);
				}
			}
		}

		orgCur.close();
		return (organizationList);
	}

	// public Organization getContactOrg(String id) {
	// Organization org = new Organization();
	// String where = ContactsContract.Data.CONTACT_ID + " = ? AND " +
	// ContactsContract.Data.MIMETYPE + " = ?";
	// String[] whereParameters = new String[]{id,
	// ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE};
	//
	// Cursor orgCur = this.cr.query(ContactsContract.Data.CONTENT_URI, null,
	// where, whereParameters, null);
	//
	// if (orgCur.moveToFirst()) {
	// String orgName =
	// orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
	// String title =
	// orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
	// if (orgName.length() > 0) {
	// org.setOrganization(orgName);
	// org.setTitle(title);
	// }
	// }
	// orgCur.close();
	// return(org);
	// }

	public void addContact(Context ctxt) {
		Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
		// intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME,
		// "中国");
		// intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
		// "12345678");
		Activity activity = (Activity) ctxt;
		activity.startActivityForResult(intent, ADD_CONTACT);
		;

	}

	public void editContact(Context ctxt, String name) {
		Cursor pCur = this.cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME },
				ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
				new String[] { name }, null);
		if (pCur != null) {
			// String []names=pCur.getColumnNames();
			// for(int i=0;i<names.length;i++){
			// BLog.i("++++", i+"names"+names[i]);
			// }
			// BLog.i("++++", "==============="+pCur.getCount());
			if (pCur.getCount() > 0) {
				if (pCur.moveToNext()) {// .moveToNext()
					String id = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
					Intent intent = new Intent(
							Intent.ACTION_EDIT,
							Uri.withAppendedPath(
									android.provider.ContactsContract.Contacts.CONTENT_URI,
									id));
					// ContentUris.withAppendedId(android.provider.ContactsContract.Contacts.CONTENT_URI,
					// id));
					// intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
					Activity activity = (Activity) ctxt;
					activity.startActivityForResult(intent, EDIT_CONTACT);
				}
			}
			pCur.close();
		}

	}

	@Override
	public void editContact(Context ctxt, long id) {
		Intent intent = new Intent(Intent.ACTION_EDIT,
				ContentUris.withAppendedId(
						android.provider.ContactsContract.Contacts.CONTENT_URI,
						id));
		// intent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
		Activity activity = (Activity) ctxt;
		activity.startActivityForResult(intent, EDIT_CONTACT);
	}

	public void notifyChange() {
		setChanged();
		notifyObservers();
	}


	@Override
	public String[] getIDAndNameByPhone(String phone) {
		String[] in = new String[2];
		// Cursor pCur = this.cr.query(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
		// ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
		// ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME},
		// ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
		// new String[] { phone }, null);
		String secondPhone = phone;
		if (phone != null && phone.length() == 11) {
			secondPhone = "";
			String one = phone.substring(0, 1);
			String two = phone.substring(1, 4);
			String three = phone.substring(4, 7);
			String four = phone.substring(7);
			secondPhone = one + "-" + two + "-" + three + "-" + four;
		}

		Cursor pCur = this.cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				new String[] {
						ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
						ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
						ContactsContract.CommonDataKinds.Phone.NUMBER },
				ContactsContract.CommonDataKinds.Phone.NUMBER + " like '%"
						+ phone + "%' or "
						+ ContactsContract.CommonDataKinds.Phone.NUMBER
						+ " like '%" + secondPhone + "%'", null, null);
		// Cursor pCur = this.cr.query(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		// ContactsContract.CommonDataKinds.Phone.NUMBER + " = ?",
		// new String[] { phone }, null);
		// Cursor pCur = this.cr.query(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		// null,null, null);
		if (pCur != null) {
			// String []names=pCur.getColumnNames();
			// for(int i=0;i<names.length;i++){
			// BLog.i("++++", i+"names"+names[i]);
			// }
			// BLog.i("++++", "==============="+pCur.getCount());
			if (pCur.getCount() > 0) {
				while (pCur.moveToNext()) {// .moveToNext()
					// for(int i=0;i<names.length;i++){
					// BLog.i("++++", phone+":"+names[i]+":"+pCur.getString(i));
					// }
					in[0] = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
					// BLog.i("=====", in[0]);
					in[1] = pCur
							.getString(pCur
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
					// BLog.i("=====", in[1]);
				}

			}
			pCur.close();
		}
		// Cursor p = this.cr.query(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, new String[]{
		// ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
		// ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
		// ContactsContract.CommonDataKinds.Phone.NUMBER},
		// null,
		// null, null);
		// if(p!=null){
		// // String []names=pCur.getColumnNames();
		// // for(int i=0;i<names.length;i++){
		// // BLog.i("++++", i+"names"+names[i]);
		// // }
		// BLog.i("++333++", "====== ========="+p.getCount());
		// if(p.getCount()>0){
		// while(p.moveToNext()){//.moveToNext()
		// // for(int i=0;i<names.length;i++){
		// // BLog.i("++++", phone+":"+names[i]+":"+pCur.getString(i));
		// // }
		// String
		// aaa=p.getString(p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
		// BLog.i("===31133==", aaa);
		// String
		// bbb=p.getString(p.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
		// BLog.i("===333==", bbb);
		// }
		//
		// }
		// p.close();
		// }
		// //////////////////////////////////////////////////////////////////////////
		// // ContactList contacts = new ContactList();
		// String id;
		// Cursor cur;
		// // cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI, null,
		// null,
		// // null, null);
		// cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI,
		// new String[] {
		// ContactsContract.Contacts._ID,ContactsContract.Contacts.DISPLAY_NAME,
		// ContactsContract.Contacts.HAS_PHONE_NUMBER }, null,
		// null, null);
		// if (cur != null && cur.getCount() > 0) {
		// // Log.i(TAG, "++++++++++++++++++++++++++");
		// // for (String str : cur.getColumnNames())
		// // Log.i(TAG, str);
		// // Log.i(TAG, "-------------------------------");
		// while (cur.moveToNext()) {
		// // Contact c = new Contact(ctxt);
		// id = cur.getString(cur
		// .getColumnIndex(ContactsContract.Contacts._ID));
		// in[0]=id;
		// // Log.i(TAG, "id:" + id);
		// // c.setId(id);
		// boolean hasPhone = Integer
		// .parseInt(cur.getString(cur
		// .getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0;
		// // Log.i(TAG, "hasPhone:" + hasPhone);
		// String name=cur.getString(cur
		// .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
		// in[1]=name;
		// // c.setHasPhone(hasPhone);
		// if (hasPhone) {
		// Cursor c = this.cr.query(
		// ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
		// ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
		// new String[] { id }, null);
		// if (c != null && c.getCount() > 0) {
		// while (c.moveToNext()) {
		// String number = c
		// .getString(c
		// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
		// // Log.i(TAG, "number:" + number);
		// if(phone.equals(number)){
		// c.close();
		// cur.close();
		// return in;
		// }
		// }
		// }
		//
		// c.close();
		// }
		// }
		// }
		// cur.close();
		// // contacts.sort();
		return in;
	}

	
	public void updata(Contact contact){

		 ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
		 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
		          .withSelection(Data._ID + "=?", new String[]{contact.getId()})
		          .withValue(StructuredName.DISPLAY_NAME, contact.getDisplayName())
		          .withValue(android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER, contact.getPhone())
		          .build());
		 try {
			cr.applyBatch(ContactsContract.AUTHORITY, ops);
			} catch (RemoteException e) {
			if (Utils.debug)
				Log.i(TAG,String.format("%s: %s", e.toString(), e.getMessage()));
		} catch (OperationApplicationException e) {
			if (Utils.debug)
				Log.i(TAG,String.format("%s: %s", e.toString(), e.getMessage()));

		} catch (Exception e) {
			if (Utils.debug)
				Log.i(TAG,String.format("%s: %s", e.toString(), e.getMessage()));

		}

		 
				
//				 ArrayList<ContentProviderOperation> ops = Lists.newArrayList();
//				 ops.add(ContentProviderOperation.newUpdate(Data.CONTENT_URI)
//				          .withSelection(Data._ID + "=?", new String[]{String.valueOf(dataId)})
//				          .withValue(Email.DATA, "somebody@android.com")
//				          .build());
//				 getContentResolver().applyBatch(ContactsContract.AUTHORITY, ops);

	}
	@Override
	public boolean insert(Contact contact) {

		ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
		ContentProviderOperation.Builder builder = ContentProviderOperation
				.newInsert(RawContacts.CONTENT_URI);
		String account = null;
		String accountName = null;
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account);
		builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME,
				accountName);
		operationList.add(builder.build());

		/* 人的信息 */
		{
			builder = ContentProviderOperation
					.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(StructuredName.RAW_CONTACT_ID, 0);
			builder.withValue(Data.MIMETYPE, StructuredName.CONTENT_ITEM_TYPE);
			builder.withValue(StructuredName.DISPLAY_NAME,
					contact.getDisplayName());
			operationList.add(builder.build());
		}
		ArrayList<Phone> phones = contact.getPhone();
		/* 电话信息 */
		if (phones != null) {
			for (Phone p : phones) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Phone.TYPE,
						p.getType());
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Phone.NUMBER,
						p.getNumber());
				builder.withValue(Data.IS_PRIMARY, 0);
				operationList.add(builder.build());
			}
		}
		ArrayList<Organization> organizations = contact.getOrganizations();
		/* 组织信息 */
		if (organizations != null) {
			for (Organization org : organizations) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Organization.TYPE,
						org.getType());
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Organization.COMPANY,
						org.getOrganization());
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Organization.TITLE,
						org.getTitle());
				builder.withValue(Data.IS_PRIMARY, 1);
				operationList.add(builder.build());
			}

		}
		ArrayList<Email> emails = contact.getEmail();
		/* email */
		if (emails != null) {
			for (Email e : emails) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);

				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Email.TYPE,
						e.getType());
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Email.DATA,
						e.getAddress());
				builder.withValue(Data.IS_PRIMARY, 1);
				operationList.add(builder.build());
			}
		}
		ArrayList<Address> addresses = contact.getAddresses();
		if (addresses != null) {
			for (Address adr : addresses) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.StructuredPostal.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE);
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.TYPE,
						adr.getType());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA1,
						adr.toString());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA4,
						adr.getStreet());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA5,
						adr.getPoBox());
				// values.put(ContactsContract.CommonDataKinds.StructuredPostal.DATA6,
				// address.get);//街区
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA7,
						adr.getCity());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA8,
						adr.getCountry());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA9,
						adr.getPostalCode());
				builder.withValue(
						ContactsContract.CommonDataKinds.StructuredPostal.DATA10,
						adr.getState());
				builder.withValue(Data.IS_PRIMARY, 1);
				operationList.add(builder.build());
			}
		}
		ArrayList<Website> websites = contact.getWebsites();
		if (websites != null) {
			for (Website web : websites) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.Website.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE);
				builder.withValue(
						ContactsContract.CommonDataKinds.Website.TYPE,
						web.getType());
				builder.withValue(
						ContactsContract.CommonDataKinds.Website.DATA,
						web.getUrl());
				builder.withValue(Data.IS_PRIMARY, 1);
				operationList.add(builder.build());
			}
		}
		/* im */
		ArrayList<IM> ims = contact.getImAddresses();
		if (ims != null) {
			for (IM imData : ims) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(
						android.provider.ContactsContract.CommonDataKinds.Im.RAW_CONTACT_ID,
						0);
				builder.withValue(
						Data.MIMETYPE,
						android.provider.ContactsContract.CommonDataKinds.Im.CONTENT_ITEM_TYPE);

				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Im.TYPE,
						imData.getType());
				builder.withValue(
						android.provider.ContactsContract.CommonDataKinds.Im.DATA,
						imData.getName());
				builder.withValue(Data.IS_PRIMARY, 1);
				operationList.add(builder.build());
			}
		}
		ArrayList<String> notes = contact.getNotes();
		if (notes != null) {
			for (String note : notes) {
				builder = ContentProviderOperation
						.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
				builder.withValueBackReference(Note.RAW_CONTACT_ID, 0);
				builder.withValue(Data.MIMETYPE, Note.CONTENT_ITEM_TYPE);

				builder.withValue(Note.NOTE, note);
				operationList.add(builder.build());
			}
		}
		byte[] pho = contact.getPhotoBytes();
		if (pho != null && pho.length > 0) {
			builder = ContentProviderOperation
					.newInsert(android.provider.ContactsContract.Data.CONTENT_URI);
			builder.withValueBackReference(
					android.provider.ContactsContract.CommonDataKinds.Photo.RAW_CONTACT_ID,
					0);
			builder.withValue(
					Data.MIMETYPE,
					android.provider.ContactsContract.CommonDataKinds.Photo.CONTENT_ITEM_TYPE);

			builder.withValue(
					android.provider.ContactsContract.CommonDataKinds.Photo.PHOTO,
					pho);
			operationList.add(builder.build());
		}
		// if (groupId != null) {
		// builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
		// builder.withValueBackReference(GroupMembership.RAW_CONTACT_ID, 0);
		// builder.withValue(Data.MIMETYPE, GroupMembership.CONTENT_ITEM_TYPE);
		// builder.withValue(GroupMembership.GROUP_ROW_ID, groupId);
		// operationList.add(builder.build());
		// }
		try {
			cr.applyBatch(ContactsContract.AUTHORITY, operationList);
		} catch (RemoteException e) {
			if (Utils.debug)
				Log.i(TAG,String.format("%s: %s", e.toString(), e.getMessage()));
			return false;
		} catch (OperationApplicationException e) {
			if (Utils.debug)
				Log.i(TAG,
						String.format("%s: %s", e.toString(), e.getMessage()));
			return false;
		} catch (Exception e) {
			if (Utils.debug)
				Log.i(TAG,
						String.format("%s: %s", e.toString(), e.getMessage()));
			return false;
		}

		return true;
	}

	@Override
	public int getContactCount(Context ctxt) {
		Cursor cur;
		cur = this.cr.query(ContactsContract.Contacts.CONTENT_URI,
				new String[] { ContactsContract.Contacts._ID }, null, null,
				null);
		int cot = 0;
		if (cur != null) {
			cot = cur.getCount();
			cur.close();
		}
		return cot;
	}

	@Override
	public boolean insert(Context ctxt, String phone) {
		Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
		// intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME,
		// "中国");
		intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
				phone);
		Activity activity = (Activity) ctxt;
		activity.startActivityForResult(intent, ADD_CONTACT);
		;
		return true;
	}

	@Override
	public boolean insert(Context ctxt, String phone, String name) {
		//
		Uri insertUri = android.provider.ContactsContract.Contacts.CONTENT_URI;
		Intent intent = new Intent(Intent.ACTION_INSERT, insertUri);
		intent.putExtra(android.provider.ContactsContract.Intents.Insert.NAME,
				name);
		intent.putExtra(android.provider.ContactsContract.Intents.Insert.PHONE,
				phone);
		Activity activity = (Activity) ctxt;
		activity.startActivityForResult(intent, ADD_CONTACT);
		;
		return true;
	}

	@Override
	public void lookContact(Context ctxt, long id) {
		Uri uri = Uri
				.withAppendedPath(
						android.provider.ContactsContract.Contacts.CONTENT_URI,
						"" + id);
		Intent it = new Intent(Intent.ACTION_VIEW, uri);
		ctxt.startActivity(it);

	}

	@Override
	public Contact getContact(Context ctxt, Uri uri) {
		Contact cont = null;
		Cursor c = ctxt.getContentResolver().query(uri, null, null, null, null);
		if (c != null) {
			while (c.moveToNext()) {
				// 取得联系人名字
				// cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
				String name = c
						.getString(c
								.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
				// 取得联系人ID
				int contactId = c.getInt(c
						.getColumnIndex(ContactsContract.Contacts._ID));
				int has_number = c.getInt(c.getColumnIndex("has_phone_number"));
				boolean has_phone_number = false;
				if (has_number == 1) {
					has_phone_number = true;
				}
				cont = new Contact(ctxt);
				cont.setId("" + contactId);
				cont.setDisplayName(name);
				if (has_phone_number) {
					ArrayList<Phone> ptmp = this
							.getPhoneNumbers("" + contactId);
					cont.addPhones(ptmp);
					// for(Phone ph:ptmp){
					// String number=Utils.GetNumber(ph.getNumber());
					// // if(!Phones.contains(number)){
					// // Phones.add(number);
					// // }
					// }
				}
				String pinyin = CnToSpell.getFullSpell(name);
				cont.setFirstChar(pinyin);
				// CnToSpell.getCnToSpell().clear();
				// String leftStr =
				// GetChinessFirstSpell.GetChineseSpell(leftName);
				// char leftChar=leftStr.charAt(0);
				// //System.out.println("left.getDisplay_name(): "+left.getDisplay_name()+" ,c : "+c+" ,leftStr : "+leftStr+" ,leftChar : "+leftChar);
				// if(leftChar>='a'&&leftChar<='z'){
				// leftChar=(char)(leftChar-32);
				// cont.setFirst_char("" + leftChar);
				// }else if(leftChar>='A'&&leftChar<='Z'){
				// cont.setFirst_char("" + leftChar);
				// }else{
				// leftChar= 0;
				// cont.setFirst_char("" + leftChar);
				// }

			}
			c.close();
		}
		return cont;
	}

	@Override
	public void delPhone(Context ctxt, String phone, String id) {
        if(Utils.debug)Log.i(TAG, "del phone"+phone+":"+id);
		//
		// ArrayList<Phone> phones = new ArrayList<Phone>();
		Cursor pCur = this.cr.query(
				ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
				ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
				new String[] { id }, null);
		if (pCur != null) {
			int len = pCur.getCount();
			if (len == 1) {
				// ctxt.getContentResolver().delete(ContactsContract.Data.CONTENT_URI,
				// ContactsContract.Data.RAW_CONTACT_ID + "=?" ,new
				// String[]{id});
				String where = ContactsContract.Data.RAW_CONTACT_ID + " = ? ";
				String[] params = new String[] { id };

				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				ops.add(ContentProviderOperation
						.newDelete(ContactsContract.Data.CONTENT_URI)
						.withSelection(where, params).build());
				try {
					cr.applyBatch(ContactsContract.AUTHORITY, ops);
				} catch (RemoteException e) {
					//
					if(Utils.debug)e.printStackTrace();
				} catch (OperationApplicationException e) {
					//
					if(Utils.debug)e.printStackTrace();
				}
				if(Utils.debug)Log.i(TAG, "del phone1");
			} else {
				String where = ContactsContract.Data.DATA1 + " = ? ";
				String[] params = new String[] { phone };

				ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
				ops.add(ContentProviderOperation
						.newDelete(ContactsContract.Data.CONTENT_URI)
						.withSelection(where, params).build());
				try {
					cr.applyBatch(ContactsContract.AUTHORITY, ops);
				} catch (RemoteException e) {
					//
					if(Utils.debug)e.printStackTrace();
				} catch (OperationApplicationException e) {
					//
					if(Utils.debug)e.printStackTrace();
				}
				if(Utils.debug)Log.i(TAG, "del phone 2");
				// while (pCur.moveToNext()) {
				// String number = pCur
				// .getString(pCur
				// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				// // Log.i("TAG", "number:" + number);
				// int type = pCur
				// .getInt(pCur
				// .getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
				// // Log.i("TAG", "type:" + type);
				// if(number.equals(phone)){
				// ContentValues values = new ContentValues();
				// values.put(ContactsContract.CommonDataKinds.Phone.NUMBER,
				// "");
				// //
				// ctxt.getContentResolver().update(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
				// values,
				// ContactsContract.CommonDataKinds.Phone.CONTACT_ID+" =? AND "+
				// // ContactsContract.CommonDataKinds.Phone.NUMBER+" =?  AND "+
				// // ContactsContract.CommonDataKinds.Phone.TYPE+" =?", new
				// String[]{id,phone,String.valueOf(type)});
				// ctxt.getContentResolver().update(ContactsContract.Data.CONTENT_URI,
				// values, ContactsContract.Data.RAW_CONTACT_ID + " = ? AND "
				// + ContactsContract.Data.DATA1+ " = ? AND "+
				// ContactsContract.Data.MIMETYPE + " = ?"
				// , new
				// String[]{id,phone,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
				// });
				// }
				// }
			}

		}

		pCur.close();

	}

}
