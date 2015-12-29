package com.tuixin11sms.tx.model;

import android.os.Bundle;
import android.os.Parcel;

/**
 * 封装Server返回的数据
 * 
 * @author 郭伟洲
 * 
 */
public class ServerRsp {
	// 会话Id
	private int sessionId;
	// 状态码
	private StatusCode statusCode = StatusCode.OPT_FAILED;
	//Server返回的数据
	//	private HashMap<String, Object> map;

	private Bundle bundle;

	public ServerRsp() {
		super();
		bundle = new Bundle();
	}

	private ServerRsp(Parcel parcel) {
		readFromParcel(parcel);
	}

	//	@Override
	//	public int describeContents() {
	//		//
	//		return 0;
	//	}
	//
	//	@Override
	//	public void writeToParcel(Parcel out, int flags) {
	//		out.writeInt(sessionId);
	//		out.writeSerializable(statusCode);
	//		out.writeBundle(bundle);
	////		out.writeMap(map);
	//	}

	public void readFromParcel(Parcel in) {
		sessionId = in.readInt();
		statusCode = (StatusCode) in.readSerializable();
		//		map = in.readHashMap(getClass().getClassLoader());
		bundle = in.readBundle();
	}

	private void createMap() {
		//		if(map == null){
		//			map = new HashMap<String, Object>();
		//		}
		if (bundle == null) {
			bundle = new Bundle();
		}
	}

	//	public static final Parcelable.Creator<ServerRsp> CREATOR = new Parcelable.Creator<ServerRsp>() {
	//		public ServerRsp createFromParcel(Parcel in) {
	//			return new ServerRsp(in);
	//		}
	//
	//		public ServerRsp[] newArray(int size) {
	//			return new ServerRsp[size];
	//		}
	//	};
	public void putString(String key, String value) {
		createMap();
		//		map.put(key, value);
		bundle.putString(key, value);
	}

	public void putInt(String key, int value) {
		createMap();
		//		map.put(key, value);
		bundle.putInt(key, value);
	}

	public void putBoolean(String key, boolean value) {
		createMap();
		//		map.put(key, value);
		bundle.putBoolean(key, value);
	}

	public String getString(String key) {
		if (bundle != null && bundle.containsKey(key)) {
			return bundle.getString(key);
		}
		//		if(map != null && map.containsKey(key)){
		//			return map.get(key).toString();
		//		}
		return null;
	}

	public int getInt(String key, int defaultValue) {
		if (bundle != null) {
			return bundle.getInt(key, defaultValue);
		}
		//		if(map != null){
		//			Object o = map.get(key);
		//			if(o == null){
		//				return defaultValue;
		//			}
		//			try{
		//				return (Integer)o;
		//			}catch(ClassCastException e){
		//				return defaultValue;
		//			}
		//		}
		return defaultValue;
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		if (bundle != null) {
			return bundle.getBoolean(key, defaultValue);
		}
		//		if(map != null){
		//			Object o = map.get(key);
		//			if(o == null){
		//				return defaultValue;
		//			}
		//			try {
		//				return (Boolean) o;
		//			} catch (ClassCastException e) {
		//				return defaultValue;
		//			}
		//		}
		return defaultValue;
	}

	public int getSessionId() {
		return sessionId;
	}

	public void setSessionId(int sessionId) {
		this.sessionId = sessionId;
	}

	public StatusCode getStatusCode() {
		return statusCode;
	}

	public StatusCode getStatusCode(String key) {
		return (StatusCode) bundle.getSerializable(key);
	}

	public void setStatusCode(StatusCode statusCode) {
		this.statusCode = statusCode;
		bundle.putSerializable("statusCode", statusCode);
	}

	public Bundle getBundle() {
		return bundle;
	}

	public void setBundle(Bundle bundle) {
		this.bundle = bundle;
	}

	public void putStatusCode(String key, StatusCode sc) {
		createMap();
		bundle.putSerializable(key, sc);
	}

}
