package com.tuixin11sms.tx.dao.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.callbacks.ILoginSuccess;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.BlogNoticeMsg;
import com.tuixin11sms.tx.utils.CachedPrefs.PrefsInfors;
import com.tuixin11sms.tx.utils.Utils;

public class LikeNoticeImpl {

	private static final String TAG = "LikeNoticeImpl";
	// 代表被喜欢通知会话的id
	public static final long LIKE_NOTICE_ID = 3421;// 服务器推送的被喜欢提醒协议是34号协议，21号子协议

	private final int COUNT = 10;// 分页获取数据库中数据时，单页条目数
	private int offset = 0;// 分页获取数据库中数据时，起始位置偏移量

	// 被喜欢的提醒通知集合
	private Set<BlogNoticeMsg> mLikeNotices = new TreeSet<BlogNoticeMsg>(
			new Comparator<BlogNoticeMsg>() {

				@Override
				public int compare(BlogNoticeMsg lhs, BlogNoticeMsg rhs) {
					return (int) (rhs.getTime() - lhs.getTime());
				}
			});

	private SessionManager mSess;
	private ReceiveLikeNotice mrpn;
	//private PrefsInfors mPrefInfor = null;// 存储用户杂信息的sp对象
	private boolean isEndOfNoticeList = false;// 是否取到了通知列表底部

	public LikeNoticeImpl(SessionManager sm) {
		mSess = sm;
		mSess.mPrefInfor = new PrefsInfors(mSess.getContext());
		mSess.registLoginSuccessListener(new ILoginSuccess() {

			@Override
			public void onLoginSuccess(long uid) {
				mSess.mPrefInfor.initFiled(uid);
				if(Utils.debug)Log.i(TAG,"当前用户的"+uid);
			}
		});
	}

	public boolean add(BlogNoticeMsg blogNotice) {
		try {
			ContentValues values = blogNotice.toValues();
			mSess.getContentResolver().insert(TxDB.LikeNotice.CONTENT_URI,
					values);
			BlogMsg blog = findBlogMsg(blogNotice.getBlogId());
			if (blog == null) {
				// 瞬间不存在，则不添加此提醒
				return false;
			}
			blogNotice.setBlogMsg(blog);// 给提醒消息设置blog
			blogNotice.setTx(mSess.getTxMgr().getTx(blogNotice.getUid()));
			if (mrpn != null) {
				// 通知观察者更新ui
				mrpn.onReceiveNotice();
			}
			if (mSess.mPrefInfor.hasNewLikeNotice != null) {
				mSess.mPrefInfor.hasNewLikeNotice.setVal(true).commit();// 设置有新的未读提醒
			}

			return true;

		} catch (Exception e) {
			if (Utils.debug)
				Log.e(TAG, "插入失败，应该是外键约束异常", e);
		}

		return false;
	}

	/** 查找瞬间消息表 */
	public BlogMsg findBlogMsg(long blogId) {
		BlogMsg blog = mSess.getBlogDao().findBlogMsg(blogId);
		return blog;
	}

	
	/**删除blogId对应的所有被喜欢提醒*/
	public int delete(String blogId) {
		int row = mSess.getContentResolver().delete(
				TxDB.LikeNotice.CONTENT_URI, TxDB.LikeNotice.BLOG_ID + "=? ",
				new String[] { blogId });
		return row;
	}

	/** 获取通知消息集合 */
	public ArrayList<BlogNoticeMsg> getNoticeLocalList() {

		if (!isEndOfNoticeList) {
			// 没有取到最后一条通知信息
			String sortOrder = TxDB.LikeNotice.DEFAULT_SORT_ORDER + " limit "
					+ offset + "," + COUNT;
			offset += COUNT;// 偏移量增加一页
			Cursor cur = mSess.getContentResolver().query(
					TxDB.LikeNotice.CONTENT_URI, null, null, null, sortOrder);

			if (cur != null) {
				isEndOfNoticeList = cur.getCount() < COUNT ? true : false;
				while (cur.moveToNext()) {
					long blogId = cur.getLong(cur
							.getColumnIndex(TxDB.LikeNotice.BLOG_ID));
					BlogMsg blog = findBlogMsg(blogId);
					if (Utils.debug) {
						if (blog != null) {
							Log.i(TAG, "瞬间id=" + blog.getMid() + ",瞬间本地图片地址="
									+ blog.getImgPath());
						}

					}
					BlogNoticeMsg pn = generateLikeNotice(cur, blog);
					if (pn != null) {
						// 加载到缓存中
						mLikeNotices.add(pn);
					}
				}
			}
			if (!isEndOfNoticeList) {
				isEndOfNoticeList = offset > 39 ? true : false;// 被喜欢提醒最大显示个数为40条
			}
		}

		return new ArrayList<BlogNoticeMsg>(mLikeNotices);
	}

	/** 进入查看通知消息页面,清空未读消息数，返回消息集合 */
	public ArrayList<BlogNoticeMsg> onEnterNoticePage() {
		offset = 0;
		isEndOfNoticeList = false;
		mLikeNotices.clear();// 清空缓存中的数据
		ArrayList<BlogNoticeMsg> pnList = getNoticeLocalList();
		// 清空未读提醒标记
		if (mSess.mPrefInfor.hasNewLikeNotice != null) {
			mSess.mPrefInfor.hasNewLikeNotice.setVal(false).commit();
		}

		return pnList;
	}

	/** 是否有未读被喜欢消息 */
	public boolean hasUnreadLikedNotice() {
		if (mSess.mPrefInfor.hasNewLikeNotice != null) {
			return mSess.mPrefInfor.hasNewLikeNotice.getVal();
		}
		if (Utils.debug) {
			Log.i(TAG, "没有收到登录成功通知？");
		}
		return false;

	}

	public boolean isEndOfList() {
		return isEndOfNoticeList;
	}

	/**
	 * 通过cursor生成PraiseNotice对象
	 */
	private BlogNoticeMsg generateLikeNotice(Cursor c, BlogMsg blog) {
		if (blog == null) {
			return null;
		}
		long blogId = c.getLong(c.getColumnIndex(TxDB.LikeNotice.BLOG_ID));
		long uid = c.getLong(c.getColumnIndex(TxDB.LikeNotice.UID));
		long time = c.getLong(c.getColumnIndex(TxDB.LikeNotice.TIME));

		BlogNoticeMsg blogNotice = null;
		blogNotice = new BlogNoticeMsg(blogId, uid, time);
		blogNotice.setBlogMsg(blog);
		blogNotice.setTx(mSess.getTxMgr().getTx(uid));
		return blogNotice;
	}

	public void registReceiveNoticeListener(ReceiveLikeNotice rpn) {
		if (rpn != null) {
			mrpn = rpn;
		}
	}

	public void unregistReceiveNoticeListener() {
		if (mrpn != null) {
			mrpn = null;
		}
	}

	// 收到被喜欢消息通知的回调
	public interface ReceiveLikeNotice {
		void onReceiveNotice();
	}

}
