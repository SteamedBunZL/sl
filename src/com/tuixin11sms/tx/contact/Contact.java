package com.tuixin11sms.tx.contact;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import a_vcard.android.provider.Contacts;
import a_vcard.android.syncml.pim.PropertyNode;
import a_vcard.android.syncml.pim.VDataBuilder;
import a_vcard.android.syncml.pim.VNode;
import a_vcard.android.syncml.pim.vcard.ContactStruct;
import a_vcard.android.syncml.pim.vcard.ContactStruct.ContactMethod;
import a_vcard.android.syncml.pim.vcard.ContactStruct.OrganizationData;
import a_vcard.android.syncml.pim.vcard.ContactStruct.PhoneData;
import a_vcard.android.syncml.pim.vcard.VCardComposer;
import a_vcard.android.syncml.pim.vcard.VCardException;
import a_vcard.android.syncml.pim.vcard.VCardParser;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.utils.Utils;

public class Contact implements Parcelable{
	final String TAG = Contact.class.getSimpleName();
	private String id;
	private String displayName;
	private boolean hasPhone;
	private boolean isTx;
	private boolean isEditable;
	private String firstChar;
	private ArrayList<Phone> phones;
	private ArrayList<Email> emails;
	private ArrayList<String> notes;
	private ArrayList<Address> addresses;
	private ArrayList<IM> imAddresses;
	private ArrayList<Organization> organizations;
	private ArrayList<Website> websites;
	private ArrayList<TX> txs;
    private byte[] photoBytes;
    private String msgid;
    private Context context;
	public Contact(Context context) {
		isEditable = true;
		this.context= context;
	}
	public byte[] getPhotoBytes() {
		return photoBytes;
	}

	public void setPhotoBytes(byte[] photoBytes) {
		this.photoBytes = photoBytes;
	}
	// private Organization organization;
	public String getMsgid() {
		return msgid;
	}

	public void setMsgid(String msgid) {
		this.msgid = msgid;
	}
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	public String getFirstChar() {
		return firstChar;
	}

	public void setFirstChar(String firstChar) {
		this.firstChar = firstChar;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public boolean isTx() {
		return isTx;
	}

	public void setIsTx(boolean isTx) {
		this.isTx = isTx;
	}

	public boolean isEditable() {
		return isEditable;
	}

	public void setIsEditable(boolean isEditable) {
		this.isEditable = isEditable;
	}

	public boolean hasPhone() {
		return hasPhone;
	}

	public void setHasPhone(boolean hasPhone) {
		this.hasPhone = hasPhone;
	}

	public ArrayList<Phone> getPhone() {
		if(hasPhone&&phones==null){
			ContactAPI api = ContactAPI.getAPI();
			api.setCr(this.context
					.getContentResolver());	
			phones=api.getPhoneNumbers(id);
		}
		return phones;
	}

	public void setPhone(ArrayList<Phone> phone) {
		this.phones = phone;
	}
    public void addPhones(ArrayList<Phone> phones){
    	if(this.phones==null){
    		this.phones=new ArrayList<Phone>();
    	}
    	for(Phone ph:phones){
    		addPhone(ph);
    	}
    }
	public void addPhone(Phone phone) {
		if(this.phones==null){
    		this.phones=new ArrayList<Phone>();
    	}
		if(!this.phones.contains(phone))
		  this.phones.add(phone);
	}

	public ArrayList<Email> getEmail() {
		return emails;
	}

	public void setEmail(ArrayList<Email> email) {
		this.emails = email;
	}

	public void addEmail(Email email) {
		if(this.emails==null){
			this.emails=new ArrayList<Email>(); 
		}
		if(!this.emails.contains(email))
		  this.emails.add(email);
	}

	public ArrayList<String> getNotes() {
		return notes;
	}

	public void setNotes(ArrayList<String> notes) {
		this.notes = notes;
	}

	public void AddNotes(String note) {
		if(this.notes==null)
			this.notes=new ArrayList<String>();
		if(!this.notes.contains(note))
		  this.notes.add(note);
	}

	public ArrayList<Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(ArrayList<Address> addresses) {
		this.addresses = addresses;
	}

	public void addAddress(Address address) {
		if(this.addresses==null){
			this.addresses=new ArrayList<Address>();
		}
		if(!this.addresses.contains(address))
		  this.addresses.add(address);
	}

	public ArrayList<IM> getImAddresses() {
		return imAddresses;
	}

	public void setImAddresses(ArrayList<IM> imAddresses) {
		this.imAddresses = imAddresses;
	}

	public void addImAddresses(IM imAddr) {
		if(this.imAddresses==null){
			this.imAddresses=new ArrayList<IM>();
		}
		if(!this.imAddresses.contains(imAddr))
		  this.imAddresses.add(imAddr);
	}

	public ArrayList<Organization> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(ArrayList<Organization> organizations) {
		this.organizations = organizations;
	}

	public void addOrganizations(Organization organization) {
		if(this.organizations==null){
			this.organizations=new ArrayList<Organization>();
		}
		if(!this.organizations.contains(organization))
		  this.organizations.add(organization);
	}

	public ArrayList<Website> getWebsites() {
		return websites;
	}

	public void setWebsites(ArrayList<Website> websites) {
		this.websites = websites;
	}

	public void addWebsites(Website website) {
		if(this.websites==null){
			this.websites=new ArrayList<Website>();
		}
		if(!this.websites.contains(website))
		  this.websites.add(website);
	}

	public void setTXs(ArrayList<TX> txs) {
		this.txs = txs;
	}

	public void addTXs(TX tx) {
		if(txs==null){
			txs=new ArrayList<TX>();
		}
		if(!txs.contains(tx))
		   this.txs.add(tx);
	}
	
	public String toString() {
		StringBuffer sb=new StringBuffer();
		sb.append("id:").append(id).append(";")
		.append("displayName:").append(displayName).append(";")
		.append("hasPhone:").append(hasPhone).append(";")
		.append("isTx:").append(isTx).append(";")
		.append("isEditable:").append(isEditable).append(";")
		.append("firstChar:").append(firstChar).append(";");
		if(phones!=null){
			for(Phone obj:phones){
				sb.append("phone:{").append(obj.toString()).append("};");
			}
		}
		if(emails!=null){
			for(Email obj:emails){
				sb.append("email:{").append(obj.toString()).append("};");
			}
		}
		if(notes!=null){
			for(String obj:notes){
				sb.append("note:{").append(obj.toString()).append("};");
			}
		}
		if(addresses!=null){
			for(Address obj:addresses){
				sb.append("address:{").append(obj.toString()).append("};");
			}
		}
		if(imAddresses!=null){
			for(IM obj:imAddresses){
				sb.append("imAddress:{").append(obj.toString()).append("};");
			}
		}
		if(organizations!=null){
			for(Organization obj:organizations){
				sb.append("organization:{").append(obj.toString()).append("};");
			}
		}
		if(websites!=null){
			for(Website obj:websites){
				sb.append("website:{").append(obj.toString()).append("};");
			}
		}
		if(txs!=null){
			for(TX obj:txs){
				sb.append("tx:{").append(obj.toString()).append("};");
			}
		}
		return sb.toString();
	}
	public static final Parcelable.Creator<Contact> CREATOR = new Parcelable.Creator<Contact>() {
		public Contact createFromParcel(Parcel in) {
			return new Contact(in);
		}

		public Contact[] newArray(int size) {
			return new Contact[size];
		}
	};

	private Contact(Parcel in) {
		readFromParcel(in);
	}
	public int describeContents() {
		//
		return 0;
	}

	public void writeToParcel(Parcel out, int flags) {
		out.writeString(id);
		out.writeString(displayName);
		out.writeString(firstChar);
		boolean[] bool = new boolean[3];
		bool[0]=hasPhone;
		bool[1]=isTx;
		bool[2]=isEditable;
		out.writeBooleanArray(bool);
		if(phones!=null){
			out.writeInt(phones.size());
			for(Phone obj:phones){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(emails!=null){
			out.writeInt(emails.size());
			for(Email obj:emails){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(notes!=null){
			out.writeInt(notes.size());
			for(String obj:notes){
				out.writeString(obj);
			}
		}else{
			out.writeInt(0);
		}
		if(addresses!=null){
			out.writeInt(addresses.size());
			for(Address obj:addresses){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(imAddresses!=null){
			out.writeInt(imAddresses.size());
			for(IM obj:imAddresses){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(organizations!=null){
			out.writeInt(organizations.size());
			for(Organization obj:organizations){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(websites!=null){
			out.writeInt(websites.size());
			for(Website obj:websites){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
		if(txs!=null){
			out.writeInt(txs.size());
			for(TX obj:txs){
				out.writeParcelable(obj, flags);
			}
		}else{
			out.writeInt(0);
		}
	}
	public void readFromParcel(Parcel in) {	
		id = in.readString();
		displayName = in.readString();
		firstChar = in.readString();
		boolean[] bool = new boolean[3];	
		in.readBooleanArray(bool);
		hasPhone = bool[0];
		isTx = bool[1];
		isEditable = bool[2];
		phones=new ArrayList<Phone>();
		int num=in.readInt();
		for (int i = 0; i < num; i++) {
			Phone phone = in.readParcelable(Phone.class.getClassLoader());
			phones.add(phone);
	    }
		emails=new ArrayList<Email>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			Email obj = in.readParcelable(Email.class.getClassLoader());
			emails.add(obj);
	    }
		notes=new ArrayList<String>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			String obj = in.readString();
			notes.add(obj);
	    }
		addresses=new ArrayList<Address>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			Address obj = in.readParcelable(Address.class.getClassLoader());
			addresses.add(obj);
	    }
		imAddresses=new ArrayList<IM>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			IM obj = in.readParcelable(IM.class.getClassLoader());
			imAddresses.add(obj);
	    }
		organizations=new ArrayList<Organization>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			Organization obj = in.readParcelable(Organization.class.getClassLoader());
			organizations.add(obj);
	    }
		websites=new ArrayList<Website>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			Website obj = in.readParcelable(Website.class.getClassLoader());
			websites.add(obj);
	    }
		txs=new ArrayList<TX>();
		num=in.readInt();
		for (int i = 0; i < num; i++) {
			TX obj = in.readParcelable(TX.class.getClassLoader());
			txs.add(obj);
	    }
	}

	@SuppressWarnings("deprecation")
	public boolean readToFile(String name,Context ctx) {
		String storagePath=Utils.getStoragePath(ctx);
   	    if(Utils.isNull(storagePath)){
   	    	return false;
   	    }
   	    File file = null;
		file = new File(storagePath + "/"+Utils.VCARD_PATH, name);
   	    
		if(!file.exists())
			return false;
		String UTF8 = "UTF-8";
//		int BUFFER_SIZE = 1024*600;
		VCardParser parser = new VCardParser();
		VDataBuilder builder = new VDataBuilder();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(file), UTF8));
			if(Utils.debug)Log.i(TAG,""+ br);
			String vcardString = "";
			String line;
			
			while ((line = br.readLine()) != null) {
				if(Utils.debug)Log.i(TAG,""+ vcardString);
				vcardString += line + "\n";
			}
			br.close();
			if(Utils.debug)Log.i(TAG, vcardString);
			// parse the string
			boolean parsed = parser.parse(vcardString, "UTF-8", builder);
			if (!parsed) {
				throw new VCardException("Could not parse vCard file: " + file);
			}

			// get all parsed contacts
			List<VNode> pimContacts = builder.vNodeList;

			for (VNode contact : pimContacts) {
				ContactStruct cs = ContactStruct.constructContactFromVNode(
						contact, ContactStruct.NAME_ORDER_TYPE_ENGLISH);
				if(Utils.debug)Log.i(TAG, "cs.company:" + cs.company);
				if(Utils.debug)Log.i(TAG, "cs.name:" + cs.name);
				this.displayName=cs.name;
				if(Utils.debug)Log.i(TAG, "cs.phoneticName:" + cs.phoneticName);
				if(Utils.debug)Log.i(TAG, "cs.photoType:" + cs.photoType);
				// Log.i(TAG, "cs.photoBytes:"+cs.photoBytes.length);
				if(cs.photoBytes!=null&&cs.photoBytes.length>0){
					if(Utils.debug)Log.i(TAG, "cs.photoBytes:"+cs.photoBytes.length);
					photoBytes=cs.photoBytes;
					Bitmap bm=BitmapFactory.decodeByteArray(photoBytes, 0, photoBytes.length); 
					if (bm != null) {
						String n=name.substring(0,name.lastIndexOf("."))+".jpg";
						if(Utils.debug)Log.i(TAG, "file========="+n);
						file = new File(storagePath + "/vcf", n);
						FileOutputStream outStream = new FileOutputStream(file);
						if (bm.compress(CompressFormat.JPEG, 85, outStream)) {
							outStream.flush();
							outStream.close();
							if(Utils.debug)Log.i(TAG, "===img=====down ok=======");
						}
						bm.recycle();
						bm = null;					
					}
//					ContactAPI api=ContactAPI.getAPI();
//			    	api.setCr(ctx.getContentResolver());
//			    	api.setContactPhoto(c, bytes, personId);
				}
				if(Utils.debug)Log.i(TAG, "cs.title:" + cs.title);
				if(Utils.debug)Log.i(TAG, "cs.notes:" + cs.notes);
				if(cs.notes!=null&&cs.notes.size()>0){
					this.notes=new ArrayList<String>();
					this.notes.addAll(cs.notes);
				}
				  
				if(Utils.debug)Log.i(TAG, "cs.organizationList:" + cs.organizationList);
				
				if (cs.organizationList != null){
					organizations= new ArrayList<Organization>();
					for (OrganizationData od : cs.organizationList) {
						if(Utils.debug)Log.i(TAG, "cs.organizationList:od" + od.toString());
						organizations.add(new Organization(od.positionName,od.companyName,od.type));	
					}
				}
				if(Utils.debug)Log.i(TAG, "cs.contactmethodList:" + cs.contactmethodList);
				if (cs.contactmethodList != null){
					emails=new ArrayList<Email>();
					addresses=new ArrayList<Address>();
					for (ContactMethod cm : cs.contactmethodList) {
						if(Utils.debug)Log.i(TAG, "cs.contactmethodList:cm" + cm.toString());
						if(cm.kind==Contacts.KIND_EMAIL){
							emails.add(new Email(cm.data,cm.type));
						}else if(cm.kind==Contacts.KIND_POSTAL){
							addresses.add(new Address(cm.data,cm.type));
						}
					}
				}
				if (cs.phoneList != null){
					phones=new ArrayList<Phone>();
					for (PhoneData pd : cs.phoneList) {
						if(Utils.debug)Log.i(TAG, "cs.phoneList:pd" + pd.toString());
						phones.add(new Phone(pd.data,pd.type));
					}
				}
				if(Utils.debug)Log.i(TAG, "cs.extensionMap:" + cs.extensionMap);
				if(Utils.debug)Log.i(TAG, "++++++++++++++++++++++++++++++++++++++++");
				if (cs.extensionMap != null) {
					Set<Entry<String, List<String>>> set = cs.extensionMap
							.entrySet();
					Iterator<Entry<String, List<String>>> i = set.iterator();
					imAddresses=new ArrayList<IM>();
					websites=new ArrayList<Website>();
					while (i.hasNext()) {
						Entry<String, List<String>> entry = i.next();
						if(Utils.debug)Log.i(TAG, entry.getKey() + "cs.extensionMap key");
						if(Utils.debug)Log.i(TAG, entry.getValue() + "cs.extensionMap value");
						if(entry.getKey().equals("URL")){
							for(String str:entry.getValue()){
								if(Utils.debug)Log.i(TAG, str + "entry.getValue() str");
//								PropertyNode propertyNode=PropertyNode.decode(str);
								websites.add(new Website(str,Website.TYPE_HOMEPAGE));
//								Log.i(TAG, propertyNode.propName + "propertyNode propName");
//								Log.i(TAG, propertyNode.propValue + "propertyNode propValue");
//								for(String str1:propertyNode.paramMap_TYPE)
//								    Log.i(TAG, str1 + "propertyNode paramMap_TYPE");						
//								if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("HOMEPAGE")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_HOMEPAGE));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("BLOG")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_BLOG));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("HOME")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_HOME));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("FTP")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_FTP));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("OTHER")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_OTHER));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("PROFILE")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_PROFILE));
//								}else if(propertyNode.paramMap_TYPE!=null&&
//										propertyNode.paramMap_TYPE.equals("WORK")){
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_WORK));
//								}else {
//									websites.add(new Website(propertyNode.propValue,Website.TYPE_HOMEPAGE));
//								}
							}													 
						}else if(entry.getKey().equals("X-AIM")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								if(Utils.debug)Log.i(TAG, str + "entry.getValue() str");
								imAddresses.add(new IM(str,IM.PROTOCOL_AIM));
							}							
						}else if(entry.getKey().equals("X-GOOGLE_TALK")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_GOOGLE_TALK));
							}							
						}else if(entry.getKey().equals("X-ICQ")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_ICQ));
							}							
						}else if(entry.getKey().equals("X-JABBER")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_JABBER));
							}							
						}else if(entry.getKey().equals("X-MSN")){
							for(String str:entry.getValue()){
								if(Utils.debug)Log.i(TAG, str + "entry.getValue() str");
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_MSN));
							}							
						}else if(entry.getKey().equals("X-NETMEETING")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_NETMEETING));
							}							
						}else if(entry.getKey().equals("X-QQ")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_QQ));
							}							
						}else if(entry.getKey().equals("X-SKYPE")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_SKYPE));
							}							
						}else if(entry.getKey().equals("X-YAHOO")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_YAHOO));
							}							
						}else if(entry.getKey().equals("X-CUSTOM")){
							for(String str:entry.getValue()){
//								PropertyNode propertyNode=PropertyNode.decode(str);
								imAddresses.add(new IM(str,IM.PROTOCOL_CUSTOM));
							}							
						}
					}
					if(Utils.debug)Log.i(TAG, "cs.phoneList:" + cs.phoneList);
				}
				
			}
		} catch (UnsupportedEncodingException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		} catch (IOException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		} catch (VCardException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		}catch (Exception e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean writeToFile(Context ctx) {
		String storagePath=Utils.getStoragePath(ctx);
   	    if(Utils.isNull(storagePath)){
   	    	return false;
   	    }
   	    File sddir = null;File file = null;
		sddir = new File(storagePath,"/"+Utils.VCARD_PATH);
		if (!sddir.exists() && !sddir.mkdirs()) {
				// Log.e("bitmapFromUrl", "Create dir failed");
			return false;
		}

		    file = new File(storagePath + "/"+Utils.VCARD_PATH, msgid + ".vcf");
   	    
   	   
		String UTF8 = "UTF-8";
		int BUFFER_SIZE = 8192;

		try {
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(
					new FileOutputStream(file), UTF8), BUFFER_SIZE);
			VCardComposer composer = new VCardComposer();

			// create a contact
			ContactStruct contact1 = new ContactStruct();
			if(displayName!=null)
			   contact1.name = displayName.replaceAll(",", "");
			else
			   contact1.name ="";
			ContactAPI api=ContactAPI.getAPI();
	    	api.setCr(ctx.getContentResolver());
	    	
	    	Bitmap bm=api.getContactPhoto(ctx, Long.parseLong(id), R.drawable.icon);
	    	
	    	if(bm!=null){
	    		Bitmap bm1=Utils.zoomBitmap(bm,81,81);
	    		ByteArrayOutputStream baos = new ByteArrayOutputStream();    
	    		bm1.compress(Bitmap.CompressFormat.JPEG, 100, baos);    
		        byte[] photoByteArray= baos.toByteArray();  
		        contact1.photoType="JPEG";
		        contact1.photoBytes=photoByteArray;
		        baos.close();
//		        Bitmap bm2=Utils.zoomBitmap(bm,81,81);		       
				String n=msgid+".jpg";
				if(Utils.debug)Log.i(TAG, "file========="+n);
				file = new File(storagePath + "/"+Utils.VCARD_PATH, n);
				FileOutputStream outStream = new FileOutputStream(file);
				if (bm1.compress(CompressFormat.JPEG, 85, outStream)) {
						outStream.flush();
						outStream.close();
						if(Utils.debug)Log.i(TAG, "===img=====down ok=======");
				}				
	    	}	    			
			if (phones != null && phones.size() > 0) {
				for (Phone ph : phones) {
					contact1.addPhone(ph.getType(), ph.getNumber(), null, true);
				}
			}
			if (emails != null && emails.size() > 0) {
				for (Email em : emails) {
					contact1.addContactmethod(Contacts.KIND_EMAIL,
							em.getType(), em.getAddress(), null, true);
				}
			}
			if (addresses != null && addresses.size() > 0) {
				for (Address adr : addresses) {
					if(Utils.debug)Log.i(TAG, "+++++++++++++++==adr"+adr.getAddr());
					contact1.addContactmethod(Contacts.KIND_POSTAL,
							adr.getType(), adr.getAddr(), null, true);
				}
			}
			if (imAddresses != null && imAddresses.size() > 0) {
				for (IM im : imAddresses) {
					switch (im.getType()) {
					case IM.PROTOCOL_AIM:
						contact1.addExtension(new PropertyNode("X-AIM", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_GOOGLE_TALK:
						contact1.addExtension(new PropertyNode("X-GOOGLE_TALK",
								im.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_ICQ:
						contact1.addExtension(new PropertyNode("X-ICQ", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_JABBER:
						contact1.addExtension(new PropertyNode("X-JABBER", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_MSN:
						contact1.addExtension(new PropertyNode("X-MSN", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_NETMEETING:
						contact1.addExtension(new PropertyNode("X-NETMEETING",
								im.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_QQ:
						contact1.addExtension(new PropertyNode("X-QQ", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_SKYPE:
						contact1.addExtension(new PropertyNode("X-SKYPE", im
								.getName(), null, null, null, null, null));
						break;
					case IM.PROTOCOL_YAHOO:
						contact1.addExtension(new PropertyNode("X-YAHOO", im
								.getName(), null, null, null, null, null));
						break;
					default:
						contact1.addExtension(new PropertyNode("X-CUSTOM", im
								.getName(), null, null, null, null, null));
						break;
					}

				}
			}
			if (organizations != null && organizations.size() > 0) {
				for (Organization org : organizations) {
					contact1.addOrganization(org.getType(), org.getTitle(),
							org.getOrganization(), true);
				}
			}
			if (websites != null && websites.size() > 0) {
				for (Website web : websites) {
					switch (web.getType()) {
					case Website.TYPE_HOMEPAGE:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("HOMEPAGE")),
								null));
						break;
					case Website.TYPE_BLOG:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("BLOG")),
								null));
						break;
					case Website.TYPE_HOME:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("HOME")),
								null));
						break;
					case Website.TYPE_FTP:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("FTP")), null));
						break;
					case Website.TYPE_OTHER:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("OTHER")),
								null));
						break;
					case Website.TYPE_PROFILE:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("PROFILE")),
								null));
						break;
					case Website.TYPE_WORK:
						contact1.addExtension(new PropertyNode("URL", web
								.getUrl(), null, null, null,
								new HashSet<String>(Arrays.asList("WORK")),
								null));
						break;
					}

				}
			}
			// create vCard representation
			String vcardString = composer.createVCard(contact1,
					VCardComposer.VERSION_VCARD30_INT);

			// write vCard to the output stream
			bw.write(vcardString);
			bw.write("\n"); // add empty lines between contacts

			// repeat for other contacts
			// ...

			bw.close();
		} catch (UnsupportedEncodingException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		} catch (FileNotFoundException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		}
		// OutputStreamWriter writer = new OutputStreamWriter(
		// new FileOutputStream("example.vcf"), "UTF-8");
		catch (VCardException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		} catch (IOException e) {
			if(Utils.debug)e.printStackTrace();
			return false;
		}

		return true;
	}
	// public Organization getOrganization() {
	// return organization;
	// }
	//
	// public void setOrganization(Organization organization) {
	// this.organization = organization;
	// }
}