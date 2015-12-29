package com.tuixin11sms.tx.model;

import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.graphics.Bitmap;

import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 瞬间
 * 
 * @author BrendaZ
 * 
 */
public class BlogMsg {

	// 总的
	private long uid;
	private int blogNums;
	private int likedNums;
	private int accessNums;
	private List<BlogMsg> blogMsgs = new ArrayList<BlogMsg>();

	// 每条信息的
	private long mid;
	private long publish_id; // 发布id
	private int state; // 发布状态
	private String mmsg;
	private long time;
	private long likednum;
	private boolean isdel;
	private List<Long> idlist;
	private String imgUrl;
	private String imgPath;
	private long adutime;
	private String aduUrl;
	private String aduPath;
	private Bitmap img;
	private String mdiaInfo;
	private String geo;
	private String city;

	private int adu_process;

	private int type;// 瞬间类型
	public static final int IMG = 1;
	public static final int AUDIO = 2;
	public static final int MSG = 4; // 纯文本类型
	public static final int IMG_AUDIO = 3;

	private int likedType = 0;
	public static final int LIKED = 1;// 喜欢
	public static final int UNLIKED = 2;// 不喜欢

	public BlogMsg() {
		super();
		this.mid = 0;
		this.mmsg = "";
		this.publish_id = 0;
		this.time = 0;
		this.likednum = 0;
		this.isdel = false;
		this.idlist = new ArrayList<Long>();
		this.imgUrl = "";
		this.imgPath = "";
		this.aduUrl = "";
		this.aduPath = "";
		this.adutime = 0;
		this.city = "";
		this.mdiaInfo = "";
		
	};

	public BlogMsg(long mid, long id, String mmsg, long time, long likenum,
			boolean isdel, String imgpath, String adupath, String mdiaInfo,
			List<Long> idlist,int likedType,long uid,int type) {
		this.mdiaInfo = mdiaInfo;
		this.mid = mid;
		this.mmsg = mmsg;
		this.time = time;
		this.type = type;
		this.idlist = idlist;
		this.publish_id = id;
		this.likednum = likenum;
		this.imgPath = imgpath;
		this.aduPath = adupath;
		this.isdel = isdel;
		this.likedType = likedType;
		this.uid = uid;
	};

	private boolean bLoaded = false;// 是否加入下载队列标记，true 代表已经加入下载队列，不再去重复下载

	public boolean getImgLoaded() {
		return bLoaded;
	}

	public void setImgLoaded() {
		bLoaded = true;
	}

	public int getLikedType() {
		return likedType;
	}

	public void setLikedType(int likedType) {
		this.likedType = likedType;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getGeo() {
		return geo;
	}

	public void setGeo(String geo) {
		this.geo = geo;
	}

	public String getImgPath() {
		return imgPath;
	}

	public void setImgPath(String imgPath) {
		this.imgPath = imgPath;
	}

	public String getAduPath() {
		return aduPath;
	}

	public void setAduPath(String aduPath) {
		this.aduPath = aduPath;
	}

	public List<BlogMsg> getBlogMsgs() {
		return blogMsgs;
	}

	public void setBlogMsgs(List<BlogMsg> blogMsgs) {
		this.blogMsgs = blogMsgs;
	}

	public long getId() {
		return publish_id;
	}

	public void setId(long id) {
		this.publish_id = id;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getMdiaInfo() {
		return mdiaInfo;
	}

	public void setMdiaInfo(String mdiaInfo) {
		this.mdiaInfo = mdiaInfo;
	}

	public long getUid() {
		return uid;
	}

	public void setUid(long uid) {
		this.uid = uid;
	}

	public int getBlogNums() {
		return blogNums;
	}

	public void setBlogNums(int blogNums) {
		this.blogNums = blogNums;
	}

	public int getLikedNums() {
		return likedNums;
	}

	public void setLikedNums(int likedNums) {
		this.likedNums = likedNums;
	}

	public int getAccessNums() {
		return accessNums;
	}

	public void setAccessNums(int accessNums) {
		this.accessNums = accessNums;
	}

	public int getAdu_process() {
		return adu_process;
	}

	public void setAdu_process(int adu_process) {
		this.adu_process = adu_process;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public Bitmap getImg() {
		return img;
	}

	public void setImg(Bitmap img) {
		this.img = img;
	}

	public long getAduTime() {
		return adutime;
	}

	public void setAduTime(long time) {
		this.adutime = time;
	}

	public long getMid() {
		return mid;
	}

	public void setMid(long mid) {
		this.mid = mid;
	}

	public String getMmsg() {
		return mmsg;
	}

	public void setMmsg(String mmsg) {
		this.mmsg = mmsg;
	}

	public long getLikednum() {
		return likednum;
	}

	public void setLikednum(long likednum) {
		this.likednum = likednum;
	}

	public boolean isIsdel() {
		return isdel;
	}

	public void setIsdel(boolean isdel) {
		this.isdel = isdel;
	}

	public List<Long> getIdlist() {
		return idlist;
	}

	public void setIdlist(List<Long> idlist) {
		this.idlist = idlist;
	}

	public String getImgUrl() {
		return imgUrl;
	}

	public void setImgUrl(String imgUrl) {
		this.imgUrl = imgUrl;
	}

	public String getAduUrl() {
		return aduUrl;
	}

	public void setAduUrl(String aduUrl) {
		this.aduUrl = aduUrl;
	}


	public ContentValues toValues() {
		ContentValues values = new ContentValues();

		values.put(TxDB.Blog.BLOG_ID, mid);
		values.put(TxDB.Blog.BLOG_PUBLISH_ID, publish_id);
		values.put(TxDB.Blog.PUBLISH_UID, uid);
		values.put(TxDB.Blog.BLOG_TEXT, mmsg);
		values.put(TxDB.Blog.IMAGE_LOCAL_PATH, imgPath);
		values.put(TxDB.Blog.AUDIO_LOCAL_PATH, aduPath);
		values.put(TxDB.Blog.BLOG_MEDIA_INFOR, mdiaInfo);
		values.put(TxDB.Blog.PRAISED_COUNT, likednum);
		values.put(TxDB.Blog.BLOG_TYPE, type);
		StringBuilder sb = new StringBuilder();
		if (idlist != null) {
			for (int i = 0; i < idlist.size(); i++) {
				sb.append(idlist.get(i) + "");
				if (i != idlist.size() - 1)
					sb.append(Constants.STRING_SEPARATOR);
			}
		}
		values.put(TxDB.Blog.PRAISED_ID_LIST, sb.toString());
		values.put(TxDB.Blog.IS_DEL_BY_OP, isdel);
		values.put(TxDB.Blog.LIKED_STATE, likedType);
		values.put(TxDB.Blog.TIME, time);
		return values;
	}

	@Override
	public String toString() {
		return "BlogMsg [uid=" + uid +
//				+ ", blogNums=" + blogNums
//				+ ", likedNums=" + likedNums + ", accessNums=" + accessNums
//				+ ", blogMsgs=" + blogMsgs + ", time=" + adutime + ", mid="
//				+ mid + ", id=" + publish_id + ", state=" + state + ", mmsg=" + mmsg
//				+ ", likednum=" + likednum + ", isdel=" + isdel + ", idlist="
//				+ idlist + ", imgUrl=" + imgUrl + ", imgPath=" + imgPath
//				+ ", aduUrl=" + aduUrl + ", aduPath=" + aduPath + ", img="
//				+ img + ", mdiaInfo=" + mdiaInfo + ", geo=" + geo
//				+ ", adu_process=" + adu_process + ", type=" + type + ", city="
//				+ city +
				"]";
	}
}
