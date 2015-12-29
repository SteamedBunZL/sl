package com.tuixin11sms.tx.dao.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.IPraiseNoticeUpdate;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.PraiseMsgCallBack;
import com.tuixin11sms.tx.dao.impl.PraiseNoticeImpl.IReceivePraiseNotice;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.message.MsgStat;
import com.tuixin11sms.tx.message.PraiseNotice;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.BlogNoticeMsg;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 瞬间数据库处理
 * 
 * @author BrendaZ
 * 
 */
public class BlogMsgImpl {

	private static final String TAG = "BlogMsgImpl";

	private HashMap<Long, BlogMsg> mBlogMsgMap = new HashMap<Long, BlogMsg>();
	private HashMap<Long, ArrayList<BlogMsg>> bloglistByid = new HashMap<Long, ArrayList<BlogMsg>>();

	private SessionManager mSess;

	public BlogMsgImpl(SessionManager sm) {
		mSess = sm;
	}

	/**
	 * 添加瞬间数据
	 * 
	 * @param msg
	 * @return
	 */
	public boolean update(BlogMsg msg) {
		BlogMsg mPn = mBlogMsgMap.get(msg.getMid());
		if (mPn != null) {
			reloadBlog(msg);
		}else{
			mBlogMsgMap.put(msg.getMid(), msg);
		}
		try {
			ContentValues values = msg.toValues();
			int update = mSess.getContentResolver().update(
					TxDB.Blog.CONTENT_URI, values,
					TxDB.Blog.BLOG_ID + "=? ",
					new String[] { msg.getMid() + "" });
			if (update == 0) {
				mSess.getContentResolver().insert(TxDB.Blog.CONTENT_URI,
						values);
			}
			return true;

		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "插入失败，应该是外键约束异常", e);
		}
		
		return false;
	}

	public boolean updateLikeType(BlogMsg msg) {
		try {
			ContentValues values = new ContentValues();
			values.put(TxDB.Blog.BLOG_ID, msg.getMid());
			values.put(TxDB.Blog.LIKED_STATE, msg.getLikedType());
			int update = mSess.getContentResolver().update(
					TxDB.Blog.CONTENT_URI, values, TxDB.Blog.BLOG_ID + "=? ",
					new String[] { msg.getMid() + "" });
			if (update == 0) {
				mSess.getContentResolver()
						.insert(TxDB.Blog.CONTENT_URI, values);
			}
			// 添加到缓存中
			BlogMsg mPn = mBlogMsgMap.get(msg.getMid());
			if (mPn == null) {
				mBlogMsgMap.put(msg.getMid(), msg);
			} else {
				reloadBlog(msg);
			}
			return true;
		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "插入失败，应该是外键约束异常", e);
		}
		return false;
	}

	public BlogMsg reloadBlog(BlogMsg msg) {
		BlogMsg blog = new BlogMsg();

		if (!(msg.getMid() == 0)) {
			blog.setMid(msg.getMid());
		}
		if (!(msg.getId() == 0)) {
			blog.setId(msg.getId());
		}
		if (!(msg.getUid() == 0)) {
			blog.setUid(msg.getUid());
		}

		if (!Utils.isNull(msg.getMmsg())) {
			blog.setMmsg(msg.getMmsg());
		}
		if (!Utils.isNull(msg.getImgPath())) {
			blog.setImgPath(msg.getImgPath());
		}
		if (!Utils.isNull(msg.getImgUrl())) {
			blog.setImgUrl(msg.getImgUrl());
		}
		if (!Utils.isNull(msg.getAduPath())) {
			blog.setAduPath(msg.getAduPath());
		}
		if (!Utils.isNull(msg.getAduUrl())) {
			blog.setAduUrl(msg.getAduUrl());
		}

		if (!(msg.getLikednum() == 0)) {
			blog.setLikednum(msg.getLikednum());
		}
		if (!(msg.getType() == 0)) {
			blog.setType(msg.getType());
		}
		if (!(msg.getTime() == 0)) {
			blog.setTime(msg.getTime());
		}
		if (!(msg.getLikedType() == 0)) {
			blog.setLikedType(msg.getLikedType());
		}
		if (msg.getIdlist() != null && msg.getIdlist().size() > 0) {
			blog.setIdlist(msg.getIdlist());
		}

		return blog;
	}

	public boolean updateIdlist(BlogMsg msg) {
		if (msg == null) {
			return false;
		}
		BlogMsg mPn = mBlogMsgMap.get(msg.getMid());
		if (mPn == null) {
			// 缓存中没有此通知
			try {
				ContentValues values = new ContentValues();
				values.put(TxDB.Blog.BLOG_ID, msg.getMid());
				StringBuilder sb = new StringBuilder();
				if (msg.getIdlist() != null) {
					for (int i = 0; i < msg.getIdlist().size(); i++) {
						sb.append(msg.getIdlist().get(i) + "");
						if (i != msg.getIdlist().size() - 1)
							sb.append(Constants.STRING_SEPARATOR);
					}
				}
				values.put(TxDB.Blog.PRAISED_ID_LIST, sb.toString());
				values.put(TxDB.Blog.PRAISED_COUNT, msg.getLikednum());
				int update = mSess.getContentResolver().update(
						TxDB.Blog.CONTENT_URI, values,
						TxDB.Blog.BLOG_ID + "=? ",
						new String[] { msg.getMid() + "" });
				if (update == 0) {
					mSess.getContentResolver().insert(TxDB.Blog.CONTENT_URI,
							values);
				}
				// 添加到缓存中
				mBlogMsgMap.put(msg.getMid(), msg);
				return true;
			} catch (Exception e) {
				if (Utils.debug)
					Log.e(TAG, "插入失败，应该是外键约束异常", e);
			}
		} else {
			mPn.setLikednum(msg.getLikednum());
			mPn.setIdlist(msg.getIdlist());
		}
		return false;
	}

	/**
	 * 批量添加瞬间数据
	 * 
	 * @param msg
	 * @return
	 */
	public boolean updateAll(List<BlogMsg> msgs) {
		if (msgs != null && msgs.size() > 0) {
			for (BlogMsg blog : msgs) {
				update(blog);
			}
			return true;
		}
		return false;
	}

	/**
	 * 删除数据
	 * 
	 * @param noticeId
	 * @param blogId
	 * @param uid
	 * @return
	 */
	public int delete(long blogId) {
		BlogMsg mPn = mBlogMsgMap.remove(blogId);
		if (mPn != null) {
			if (Utils.debug) {
				Log.i(TAG, "删除缓存中此喜欢通知，mPn=" + mPn.toString());
			}
		}
		int row = mSess.getContentResolver().delete(TxDB.Blog.CONTENT_URI,
				TxDB.Blog.BLOG_ID + "=? ", new String[] { blogId + "" });
		return row;
	}

	/**
	 * 查找瞬间表
	 * 
	 * @param blogId
	 * @return
	 */
	public BlogMsg findBlogMsg(long blogId) {
		BlogMsg blog = mBlogMsgMap.get(blogId);
		if (blog == null) {
			Cursor cur = mSess.getContentResolver().query(
					TxDB.Blog.CONTENT_URI, null, TxDB.Blog.BLOG_ID + "=? ",
					new String[] { "" + blogId }, null);
			if (cur != null && cur.moveToNext()) {
				blog = generateBlog(cur);
				mBlogMsgMap.put(blog.getMid(), blog);
			}
			cur.close();
		}

		return blog;
	}

	/**
	 * 根据uid查找瞬间表
	 * 
	 * @param blogId
	 * @return
	 */
	public List<BlogMsg> findBlogMsgByUid(long uid) {
		ArrayList<BlogMsg> blogmsgs = bloglistByid.get(uid);
		if (blogmsgs == null) {
			blogmsgs = new ArrayList<BlogMsg>();
			Cursor cur = mSess.getContentResolver().query(
					TxDB.Blog.CONTENT_URI, null, TxDB.Blog.PUBLISH_UID + "=? ",
					new String[] { "" + uid }, null);
			if (cur != null) {
				while (cur.moveToNext()) {
					BlogMsg blog = generateBlog(cur);
					mBlogMsgMap.put(blog.getMid(), blog);
					blogmsgs.add(blog);
				}
			}
			cur.close();
		}

		Collections.sort(blogmsgs, new Comparator<BlogMsg>() {

			@Override
			public int compare(BlogMsg lhs, BlogMsg rhs) {
				if (lhs.getTime() > rhs.getTime()) {
					return -1;
				} else {
					return 1;
				}
			}
		});

		return blogmsgs;
	}

	// public ArrayList<BlogMsg> getBlogDataList(long uid) {
	//
	// Cursor cur = mSess.getContentResolver().query(TxDB.Blog.CONTENT_URI,
	// null, null, null, TxDB.Blog.DEFAULT_SORT_ORDER);
	// if (cur != null) {
	// while (cur.moveToNext()) {
	// long blogId = cur
	// .getLong(cur.getColumnIndex(TxDB.Blog.BLOG_ID));
	// if (!mBlogMsgMap.containsKey(blogId)) {
	// // 加载到内存中
	// BlogMsg pn = generateBlog(cur);
	// if (pn != null) {
	// mBlogMsgMap.put(pn.getMid(), pn);
	// }
	// } else {
	// mBlogMsgMap.remove(blogId);
	// BlogMsg pn = generateBlog(cur);
	// if (pn != null) {
	// mBlogMsgMap.put(pn.getMid(), pn);
	// }
	// }
	// }
	// }
	//
	// ArrayList<BlogMsg> bns = new ArrayList<BlogMsg>();
	// Iterator<Map.Entry<Long, BlogMsg>> iter = mBlogMsgMap.entrySet()
	// .iterator();
	// while (iter.hasNext()) {
	// Map.Entry<Long, BlogMsg> entry = (Map.Entry<Long, BlogMsg>) iter
	// .next();
	// BlogMsg val = entry.getValue();
	// if (val.getUid() == uid) {
	// bns.add(val);
	// bloglistByid.put(uid, bns);
	// }
	// }
	// // Collection<BlogMsg> pns = mBlogMsgMap.values();
	// // if (pns != null) {
	// // return new ArrayList<BlogMsg>(pns);
	// // }
	// Collections.sort(bns, new Comparator<BlogMsg>() {
	//
	// @Override
	// public int compare(BlogMsg lhs, BlogMsg rhs) {
	// if (lhs.getTime() > rhs.getTime()) {
	// return -1;
	// } else {
	// return 1;
	// }
	//
	// }
	// });
	//
	// return bns;
	// }

	public ArrayList<BlogMsg> getBlogList() {

		return new ArrayList<BlogMsg>(mBlogMsgMap.values());

	}

	public BlogMsg generateBlog(Cursor c) {

		long blogId = c.getLong(c.getColumnIndex(TxDB.Blog.BLOG_ID));
		long id = c.getLong(c.getColumnIndex(TxDB.Blog.BLOG_PUBLISH_ID));
		long uid = c.getLong(c.getColumnIndex(TxDB.Blog.PUBLISH_UID));
		String mmsg = c.getString(c.getColumnIndex(TxDB.Blog.BLOG_TEXT));
		String imgPath = c.getString(c
				.getColumnIndex(TxDB.Blog.IMAGE_LOCAL_PATH));
		String aduPath = c.getString(c
				.getColumnIndex(TxDB.Blog.AUDIO_LOCAL_PATH));
		String mdiaInfo = c.getString(c
				.getColumnIndex(TxDB.Blog.BLOG_MEDIA_INFOR));
		long likednum = c.getLong(c.getColumnIndex(TxDB.Blog.PRAISED_COUNT));
		int type = c.getInt(c.getColumnIndex(TxDB.Blog.BLOG_TYPE));
		String s = c.getString(c.getColumnIndex(TxDB.Blog.PRAISED_ID_LIST));
		List<Long> idlist = new ArrayList<Long>();

		if (s != null && !s.trim().equals("")) {
			String[] split = s.split(Constants.STRING_SEPARATOR);
			for (int i = 0; i < split.length; i++) {
				idlist.add(Long.parseLong(split[i]));
			}
		}
		long isdel = c.getLong(c.getColumnIndex(TxDB.Blog.IS_DEL_BY_OP));
		int likedType = c.getInt(c.getColumnIndex(TxDB.Blog.LIKED_STATE));
		long time = c.getLong(c.getColumnIndex(TxDB.Blog.TIME));

		boolean del = isdel == 0 ? false : true;

		BlogMsg blogmsg = new BlogMsg(blogId, id, mmsg, time, likednum, del,
				imgPath, aduPath, mdiaInfo, idlist, likedType, uid, type);

		try {
			if (!Utils.isNull(mdiaInfo)) {
				JSONObject objJson = new JSONObject(mdiaInfo);
				String imgUrl = objJson.getString("img");
				blogmsg.setImgUrl(imgUrl);
				if (!Utils.isNull(imgUrl) && !(imgUrl == JSONObject.NULL)) {
					blogmsg.setType(BlogMsg.IMG);
					// info.setImgPath(receiveSameImg(imgUrl));
				}
				// 2. adu:obj,音频
				// a) end:string
				// b) url:str,音频文件地址
				// c) l:uint32,音频文件长度,单位:字节
				// d) t:uint16,音频长度,单位:秒
				JSONObject aduJson = objJson.getJSONObject("adu");
				if (aduJson != null && aduJson.length() > 0) {
					String adu_url = aduJson.getString("url");
					blogmsg.setAduUrl(adu_url);
					if (!Utils.isNull(adu_url) && !(adu_url == JSONObject.NULL)) {
						blogmsg.setType(BlogMsg.AUDIO);
						// info.setAduPath(receiveSameAudio(adu_url));
					}
					long adutime = aduJson.getInt("t");
					blogmsg.setAduTime(adutime);

					if (!Utils.isNull(adu_url) && !(adu_url == JSONObject.NULL)
							&& !Utils.isNull(imgUrl)
							&& !(imgUrl == JSONObject.NULL)) {
						blogmsg.setType(BlogMsg.IMG_AUDIO);
					}

					if ((adu_url == JSONObject.NULL)
							&& (imgUrl == JSONObject.NULL)) {
						blogmsg.setType(BlogMsg.MSG);
					}
				}

				// 3. geo:obj,地理位置
				// a) lo:double,经度
				// b) la:double,纬度
				JSONObject geoJson = objJson.getJSONObject("geo");
				if (geoJson != null && geoJson.length() > 0) {
					String geo = geoJson.getDouble("la") + ","
							+ geoJson.getDouble("lo");
					String city = geoJson.getString("ct");
					blogmsg.setCity(city);
					blogmsg.setGeo(geo);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return blogmsg;
	}

}
