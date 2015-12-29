package com.tuixin11sms.tx.model;

import android.content.ContentValues;

import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB;


/**
 * 瞬间通知
 *
 */
public class BlogNoticeMsg {

	private long blogId;
	private long uid;
	private long time;
	private BlogMsg blogMsg;
	private TX tx;
	
	public BlogNoticeMsg(){
		
	}
	
	public BlogNoticeMsg(long blogId, long uid, long time) {
		super();
		this.blogId = blogId;
		this.uid = uid;
		setTime(time);
	}

	public String getNoticeId() {
		return (""+blogId)+uid;
	}
	
	public long getBlogId() {
		return blogId;
	}
	public void setBlogId(long blogId) {
		this.blogId = blogId;
	}
	public long getUid() {
		return uid;
	}
	public void setUid(long uid) {
		this.uid = uid;
	}
	public long getTime() {
		return time;
	}
	public void setTime(long time) {
		// 1398754691348
		if (("" + time).length() == 10) {
			// 如果时间单位是秒，变为毫秒
			time *= 1000;
		}
		this.time = time;
	}
	public BlogMsg getBlogMsg() {
		return blogMsg;
	}
	public void setBlogMsg(BlogMsg blogMsg) {
		this.blogMsg = blogMsg;
	}
	
	public String getNikeName() {
		if(tx==null){
			return ""+uid;
		}
		return tx.getNick_name();
	}


	public String getAvatarUrl(){
		if(tx==null){
			return "";
		}
		return tx.getAvatar_url();
	}
	
	
	public String getImagePath() {
		if(blogMsg!=null){
			return blogMsg.getImgPath();
		}
		return null;
	}

	public String getImageUrl() {
		if(blogMsg!=null){
			return blogMsg.getImgUrl();
		}
		return null;
	}

	
	
	public void setTx(TX tx) {
		this.tx = tx;
	}
	
	
	public int getAudioTime() {
		if(blogMsg==null){
			return 0;
		}
		return (int) blogMsg.getAduTime();
	}
	
	
	public ContentValues toValues() {
		ContentValues values = new ContentValues();
		values.put(TxDB.LikeNotice.BLOG_ID, blogId);
		values.put(TxDB.LikeNotice.UID,uid);
		values.put(TxDB.LikeNotice.TIME, time);
		return values;
	}
	
	
}
