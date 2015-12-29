package com.tuixin11sms.tx.model;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * 原来写的是实现Serializable接口，但是点击相册缩略图，startActivity查看大图时，
 * 报Parcelable encountered IOException writing serializable object (name = com.tuixin11sms.tx.model.AlbumItem)异常
 * Caused by: java.io.NotSerializableException: android.graphics.Bitmap
 * 先改为实现parcelable接口  2013.08.28
 * 原来好像也这么写的，但是不知道为什么没有问题
 * @author SHC
 *
 */
public class AlbumItem implements Parcelable {
	private String url;
	private boolean isAdd;
	private Bitmap bitmap;
	private boolean bLoaded=false;//是否加入下载队列标记，true 代表已经加入下载队列，不再去重复下载

	
	public AlbumItem(){}
	
	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public boolean isAdd() {
		return isAdd;
	}

	public void setAdd(boolean isAdd) {
		this.isAdd = isAdd;
	}

	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	public boolean getLoaded(){return bLoaded;}
	public void setLoaded(){bLoaded=true;}
	
	public static final Parcelable.Creator<AlbumItem> CREATOR = new Parcelable.Creator<AlbumItem>() {
		public AlbumItem createFromParcel(Parcel in) {
			return new AlbumItem(in);
		}

		public AlbumItem[] newArray(int size) {
			return new AlbumItem[size];
		}
	};
	
	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(url);
		dest.writeInt(isAdd?1:0);
//		dest.writeParcelable(bitmap, flags);
		
	}
	
	
	private AlbumItem(Parcel in) {
		url = in.readString();
		isAdd = in.readInt()==0?false:true;
//		bitmap = in.readParcelable(null); // 这个地方可以为null
	}  

}
