package com.tuixin11sms.tx.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.RelativeSizeSpan;
import android.text.style.SuperscriptSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.AbsListView.OnScrollListener;

import com.shenliao.group.activity.GroupTip;
import com.shenliao.set.activity.SetUserInfoActivity;
import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.NearlyFriendActivity.ScrollListener;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MD5;
import com.tuixin11sms.tx.dao.impl.LikeNoticeImpl.ReceiveLikeNotice;
import com.tuixin11sms.tx.data.TxDB;
import com.tuixin11sms.tx.download.AvatarDownload;
import com.tuixin11sms.tx.engine.BlogOpea;
import com.tuixin11sms.tx.message.TXMessage;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.LBSUserInfo;
import com.tuixin11sms.tx.net.LBSSocketHelper;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.QueneCallBack;
import com.tuixin11sms.tx.task.TaskFinish;
import com.tuixin11sms.tx.task.TuixinQueue;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.BlogMusicSeekBar;
import com.umeng.analytics.MobclickAgent;

/**
 * 我的瞬间
 * 
 * @author bobo
 * 
 */
public class MyBlogActivity extends BaseActivity implements OnClickListener {
	protected static final int RECORD_PAUSE = 0;
	protected static final int RECORD_PLAY = 1;

	protected static final int REQUEST_ADD_BLOG = 2;
	protected static final int RESULT_ADD_BLOG = 3;
	protected static final int SHOW_NOTICE = 15;

	public static final String REPORT_BLOG = "isReportBlog";
	private ListView lv_myblog;
	private MyBlogAdapter myBlogAdapter;
	private List<BlogMsg> list = new ArrayList<BlogMsg>();
	public static final String ISMY = "ismy";
	public static final String TXINFO = "tx";
	private boolean isMy;
	private boolean isdowning = false;
	private BlogOpea opea;
	private TX user;
	private HashMap<Long, TX> usermap = new HashMap<Long, TX>();
	private BlogMsg msg_header;
	private BlogMsg delblog;
	private boolean isEnd = false;
	private boolean isShowNoBlog = false;
	private boolean isShowAvatuarl = false;
	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case BlogOpea.SEEDBAR_PROCESS:

				Object[] result = (Object[]) msg.obj;

				for (ViewHolder holder : holders) {
					if (holder.sb_myblog_process != null) {
						BlogMsg blogMsg = holder.blogMsg;
						if (blogMsg != null) {
							long id = blogMsg.getMid();
							long tag = (Long) result[0];
							if (id == tag) {
								if (txmsg.PlayAudio == RECORD_PLAY
										&& txmsg.gmid == id) {
									holder.sb_myblog_process
											.setProgress((Integer) result[1]);
									blogMsg.setAdu_process((Integer) result[1]);
								}
							}
						}
					}
				}

				break;
			case BlogOpea.RECORD_PLAY_FINISH:
				// 播完了
				TXMessage msg_finish = (TXMessage) msg.obj;

				for (ViewHolder holder : holders) {
					if (holder.iv_myblog_play != null && msg_finish != null) {
						BlogMsg blogMsg = holder.blogMsg;
						if (blogMsg != null
								&& blogMsg.getMid() == msg_finish.gmid) {
							holder.sb_myblog_process.setProgress((int) blogMsg
									.getAduTime() * 1000);
							holder.iv_myblog_musicplay.clearAnimation();
							holder.iv_myblog_play
									.setImageResource(R.drawable.myblog_play);
							holder.tv_myblog_play.setText("Play");
							if (txmsg == null) {
								txmsg = new TXMessage();
							}
							txmsg.PlayAudio = RECORD_PAUSE;
							holder.sb_myblog_process.setProgress(0);
							playHolder = null;
							blogMsg.setAdu_process(0);
						}
					}
				}
				break;
			case BlogOpea.SERVER_SUCCESS_HEAD:

				msg_header = (BlogMsg) msg.obj;
				uid = msg_header.getUid();
				myBlogAdapter.setHeaderData(msg_header);
				if (msg_header != null && msg_header.getBlogNums() == 0) {
					tuixin_blog_title.setVisibility(View.VISIBLE);
					isShowAvatuarl = true;
					blog_loading.setVisibility(View.GONE);
					isShowNoBlog = true;
					myBlogAdapter.setData(list);
				}
				myBlogAdapter.notifyDataSetChanged();

				break;
			case BlogOpea.SERVER_SUCCESS_LIST:

				// if (list != null && list.size() > 0) {
				// list.clear();
				// }

				List<BlogMsg> msg_list = (List<BlogMsg>) msg.obj;

				isShowNoBlog = true;
				isShowAvatuarl = true;
				isloading = false;
				tuixin_blog_title.setVisibility(View.VISIBLE);
				blog_loading.setVisibility(View.GONE);
				loadmore.setVisibility(View.GONE);

				if (msg_list == null || msg_list.size() == 0) {
					isEnd = true;
					myBlogAdapter.setData(list);
					myBlogAdapter.notifyDataSetChanged();
					break;
				}

				if (msg_header != null && msg_list != null
						&& msg_list.size() > 0) {
					List<Long> allIdList = new ArrayList<Long>();
					// 将所有id添加的id集合里
					for (int i = 0; i < msg_list.size(); i++) {
						List<Long> idlist = msg_list.get(i).getIdlist();
						Collections.reverse(idlist);
						msg_list.get(i).setIdlist(idlist);
						allIdList.addAll(idlist);
					}

					List<Long> noCatchIds = new ArrayList<Long>();
					if (allIdList != null && allIdList.size() > 0) {
						for (long uid : allIdList) {
							TX tx = TX.tm.getTx(uid);
							if (tx != null) {
								if (!usermap.containsKey(uid)) {
									usermap.put(uid, tx);
								} else {
									usermap.remove(uid);
									usermap.put(uid, tx);
								}
							} else {
								// 添加没有数据的TX
								noCatchIds.add(uid);
							}
						}
					}

					// 获取没有本地数据的TX的信息
					if (noCatchIds != null && noCatchIds.size() > 0) {
						opea.sendUserList(noCatchIds, null);
					}

					// if(list!=null&&list.size()>0&&msg_list.get(msg_list.size()-1).getTime()>list.get(0).getTime()){
					// list.addAll(0, msg_list);
					// }else{
					// }

					for (BlogMsg msginfo : msg_list) {
						if (!list.contains(msginfo)) {
							list.add(msginfo);
						}
					}
					//
					// list.addAll(msg_list);
					myBlogAdapter.setData(list);
					myBlogAdapter.notifyDataSetChanged();
				}
				break;
			case BlogOpea.SERVER_DEL_BLOG:
				Object[] del_obj = (Object[]) msg.obj;

				if (del_obj != null) {
					String del_s = (String) del_obj[0];
					showToast(del_s, true);
					if (list != null && list.size() == 1) {
						list.clear();
					}
					myblog_refresh.setVisibility(View.VISIBLE);

					if (del_s.equals("删除成功")) {
						deal_delete();
					}
					cancelProgressDialog();
				}
				break;
			case BlogOpea.SERVER_USER_LIST:
				// 返回的是没有本地数据的TX集合
				HashMap<Long, TX> map = (HashMap<Long, TX>) msg.obj;
				usermap.putAll(map);
				myBlogAdapter.notifyDataSetChanged();
				break;
			case BlogOpea.SERVER_LIKE_BLOG:
				Object[] like_obj = (Object[]) msg.obj;
				if (like_obj != null) {
					String like_s = (String) like_obj[0];
					if (like_s == "操作成功") {
						BlogMsg likeblog = (BlogMsg) like_obj[1];
						if (holders != null && holders.size() > 0) {
							for (ViewHolder holder : holders) {
								if (holder.tv_myBlog_great != null) {

									long tag = (Long) holder.tv_myBlog_great
											.getTag();
									if (tag == likeblog.getMid()) {
										if (likeblog.getLikedType() == 1) {
											like_success(holder, holder.blogMsg);
											showToast("喜欢成功", true);
										}
										// 有取消喜欢协议，但目前客户端，不做取消处理
										// else if (likeblog.getLikedType() ==
										// 2) {
										// unlike_success(holder,
										// holder.blogMsg);
										// showToast("取消喜欢成功", true);
										// }
									}

								}
							}
						}
					} else {
						showToast(like_s, true);
					}
					cancelProgressDialog();
					myBlogAdapter.setData(list);
					myBlogAdapter.notifyDataSetChanged();
				}
				break;
			case BlogOpea.RECORD_DOWNLOAD_FINISH:
				BlogMsg recordmsg = (BlogMsg) msg.obj;
				isdowning = false;
				showToast("下载完成", true);
				for (ViewHolder holder : holders) {
					BlogMsg blogMsg = holder.blogMsg;
					if (blogMsg != null) {
						long id = blogMsg.getMid();
						long tag = recordmsg.getMid();
						if (id == tag) {
							recoedPlay(recordmsg, holder);
							blogMsg.setAduPath(recordmsg.getAduPath());
							mSess.getBlogDao().update(blogMsg);
						}
					}
				}
				break;
			case SHOW_NOTICE:
				// 显示喜欢瞬间的通知
				iv_myblog_msg.setVisibility(View.VISIBLE);

				break;
			case BlogOpea.SERVER_ERROR:

				String errors = (String) msg.obj;

				if (errors.equals("没有瞬间")) {
					isShowNoBlog = true;
					if (msg_header == null)
						msg_header = new BlogMsg();

					msg_header.setBlogNums(0);
					msg_header.setLikedNums(0);
					msg_header.setAccessNums(0);

					myBlogAdapter.setHeaderData(msg_header);
					myBlogAdapter.notifyDataSetChanged();
				} else if (errors.equals("没有更多瞬间了")) {
					isEnd = true;
					isloading = false;
					myblog_refresh.setVisibility(View.VISIBLE);
				}
				isShowAvatuarl = true;
				tuixin_blog_title.setVisibility(View.VISIBLE);
				blog_loading.setVisibility(View.GONE);
				loadmore.setVisibility(View.GONE);
				showToast(errors);
				break;
			}
			super.handleMessage(msg);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		opea = mSess.getBlogOpea();
		opea.setHandler(handler);
		setContentView(R.layout.activity_myblog);

		Intent intent = getIntent();
		// 判断自己的瞬间还是别人的瞬间
		isMy = intent.getBooleanExtra(ISMY, false);
		user = intent.getParcelableExtra(TXINFO);
		prepairAsyncload();
		init();
	}

	/**
	 * 初始化
	 */
	public void init() {
		LinearLayout ll_back = (LinearLayout) findViewById(R.id.btn_back_myblog);
		blog_loading = (LinearLayout) findViewById(R.id.blog_loading);
		Button bt_myblog_add = (Button) findViewById(R.id.bt_myblog_add);
		Button bt_myblog_msg = (Button) findViewById(R.id.bt_myblog_msg);
		iv_myblog_msg = (ImageView) findViewById(R.id.iv_myblog_msg);
		loadmore = (LinearLayout) findViewById(R.id.blog_loadmore);

		// 有喜欢的瞬间消息
		mSess.getLikeNoticeDao().registReceiveNoticeListener(
				new ReceiveLikeNotice() {

					@Override
					public void onReceiveNotice() {
						// 传到handler
						handler.obtainMessage(SHOW_NOTICE).sendToTarget();
					}
				});

		tuixin_blog_title = (TextView) findViewById(R.id.tuixin_blog_title);

		if (isMy) {
			uid = TX.tm.getUserID();
			tuixin_blog_title.setText("我的瞬间");
		} else {
			uid = user.partner_id;
			tuixin_blog_title.setText("Ta的瞬间");
			bt_myblog_add.setVisibility(View.GONE);
			bt_myblog_msg.setVisibility(View.GONE);
		}

		lv_myblog = (ListView) findViewById(R.id.lv_myblog);

		myblog_refresh = getLayoutInflater().inflate(
				R.layout.sll_myblog_refresh, null);
		lv_myblog.addFooterView(myblog_refresh);

		myBlogAdapter = new MyBlogAdapter(MyBlogActivity.this);

		ll_back.setOnClickListener(this);
		bt_myblog_add.setOnClickListener(this);
		bt_myblog_msg.setOnClickListener(this);

		// load(user.partner_id);
		tuixin_blog_title.setVisibility(View.GONE);
		blog_loading.setVisibility(View.VISIBLE);

		TX tx = null;
		// 从数据库中获取
		if (isMy) {
			tx = TX.tm.getTxMe();
		} else {
			tx = mSess.getTxMgr().getTx(uid);
		}

		if (Utils.debug) {
			Log.i("bobo", "=========tx===" + tx.toString());
		}

		if (tx != null && !Utils.isNull(tx.blog_head_msg)) {
			String[] split = tx.blog_head_msg.split(",");
			if (!split[0].equals("") && !split[1].equals("")
					&& !split[2].equals("")) {
				if (msg_header == null) {
					msg_header = new BlogMsg();
				}
				msg_header.setBlogNums(Integer.parseInt(split[0]));
				msg_header.setAccessNums(Integer.parseInt(split[1]));
				msg_header.setLikedNums(Integer.parseInt(split[2]));
				myBlogAdapter.setHeaderData(msg_header);
			}
		}
		getBlogData(uid, 0);
		lv_myblog.setAdapter(myBlogAdapter);
		lv_myblog.setOnScrollListener(new ScrollListener());
	}

	private boolean isloading = false;

	// 上拉刷新
	public class ScrollListener implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {

		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			//
			// 分别是开始滚动（SCROLL_STATE_FLING ），正在滚动(SCROLL_STATE_TOUCH_SCROLL ),
			// 已经停止（SCROLL_STATE_IDLE ）
			if (view.getLastVisiblePosition() == view.getCount() - 1
					&& scrollState == OnScrollListener.SCROLL_STATE_IDLE) {
				// 滑到底部了
				if (!isloading) {
					if (!isEnd && list != null && list.size() > 0) {
						// 显示加载更多
						isloading = true;
						loadmore.setVisibility(View.VISIBLE);
						myblog_refresh.setVisibility(View.GONE);
						long mid = list.get(list.size() - 1).getMid();
						getBlogData(uid, mid);
					} else {
						myblog_refresh.setVisibility(View.VISIBLE);
						loadmore.setVisibility(View.GONE);
					}
				}
			}
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_myblog:
			recoedStop();
			this.finish();
			break;
		case R.id.bt_myblog_add:
			recoedStop();
			Intent addIntent = new Intent(MyBlogActivity.this,
					AddMyBlogActivity.class);
			startActivityForResult(addIntent, REQUEST_ADD_BLOG);
			break;
		case R.id.bt_myblog_msg:
			recoedStop();
			Intent noticeIntent = new Intent(MyBlogActivity.this,
					LikeNoticeActivity.class);
			startActivity(noticeIntent);
			break;

		default:
			break;
		}
	}

	private Handler avatarHandler = new Handler() {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case AvatarDownload.DOWN_AVATAR_RESULT_ALL:
				Object[] result2 = (Object[]) msg.obj;
				if (result2[0] != null) {
					myBlogAdapter.getItem(0);
					mHeadHolder.iv_myblog_head
							.setImageBitmap((Bitmap) result2[0]);
				}
				break;
			case AvatarDownload.AVATAR_RESULT:
				Object[] result_iv = (Object[]) msg.obj;
				if (imageviews != null && imageviews.size() > 0) {
					for (ImageView iv : imageviews) {
						long tag = (Long) iv.getTag();
						long uid = (Long) result_iv[1];
						if (tag == uid) {
							iv.setImageBitmap((Bitmap) result_iv[0]);
							if (holders != null && holders.size() > 0) {
								for (ViewHolder holder : holders) {
									if (holder.blogMsg.getIdlist() != null
											&& holder.blogMsg.getIdlist()
													.size() > 0
											&& holder.blogMsg.getIdlist()
													.contains(uid)) {
										MyBlogGridAdapter adapter = (MyBlogGridAdapter) holder.gv_myblog_liked_img
												.getAdapter();
										adapter.notifyDataSetChanged();
									}
								}
							}
						}
					}
				}

				// if (result2[0] != null) {
				// iv_myblog_head.setImageBitmap((Bitmap) result2[0]);
				// }
				break;
			}
			super.handleMessage(msg);
		}
	};

	private HeadViewHolder mHeadHolder;
	private List<ViewHolder> holders = new ArrayList<MyBlogActivity.ViewHolder>();
	private LinearLayout blog_loading;
	private TextView tuixin_blog_title;
	private Long uid;

	private class MyBlogAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<BlogMsg> bmsgs;
		private BlogMsg msgHeader;

		private void setData(List<BlogMsg> list) {
			this.bmsgs = list;
		}

		private void setHeaderData(BlogMsg msgHeader) {
			this.msgHeader = msgHeader;
		}

		public MyBlogAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			// txs=new ArrayList<TX>();
			bmsgs = new ArrayList<BlogMsg>();
		}

		public int getCount() {
			// noContacts.setVisibility(View.VISIBLE);
			if (bmsgs != null && bmsgs.size() > 0) {
				return bmsgs.size() + 1;
			} else {
				return 2;
			}
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public int getItemViewType(int position) {
			return position == 0 ? 0 : 1;
		}

		AsyncCallback<Bitmap> mBlogImgAsynCallback = new AsyncCallback<Bitmap>() {
			@Override
			public void onSuccess(Bitmap result, long id) {
				for (ViewHolder vh : holders) {
					if (vh.blogMsg.getMid() == id) {
						vh.iv_myblog.setImageBitmap(result);
					}
				}
			}

			@Override
			public void onFailure(Throwable t, long id) {
			}
		};

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			if (position == 0) {
				HeadViewHolder vh;
				if (convertView == null) {
					mHeadHolder = vh = new HeadViewHolder();
					convertView = mInflater.inflate(
							R.layout.sll_myblog_listitem_title, null);
					vh.iv_myblog_head = (ImageView) convertView
							.findViewById(R.id.iv_myblog_head);
					vh.tv_myblog_name = (TextView) convertView
							.findViewById(R.id.tv_myblog_name);
					vh.tv_blog_blognums = (TextView) convertView
							.findViewById(R.id.tv_blog_blognums);
					vh.tv_blog_accessnums = (TextView) convertView
							.findViewById(R.id.tv_blog_accessnums);
					vh.tv_blog_likednums = (TextView) convertView
							.findViewById(R.id.tv_blog_likednums);
					convertView.setTag(vh);
				} else
					vh = (HeadViewHolder) convertView.getTag();

				// 头像
				if (uid == TX.TUIXIN_MAN) {
					vh.iv_myblog_head
							.setImageResource(R.drawable.tuixin_manage);
				} else {
					vh.iv_myblog_head.setImageBitmap(mSess
							.getDefaultPartnerAvatar(user.getSex()));
					if (isShowAvatuarl && user.avatar_url != null
							&& !TextUtils.isEmpty(user.avatar_url)) {
						vh.iv_myblog_head.setTag(uid);
						mSess.avatarDownload.downAvatar(user.avatar_url, uid,
								vh.iv_myblog_head, avatarHandler);
					}
				}
				// 昵称
				if (uid == TX.TUIXIN_MAN) {
					vh.tv_myblog_name.setText(TX.tm.getTxMan().getNick_name());
				} else {
					if (!Utils.isNull(user.getRemarkName())) {
						vh.tv_myblog_name.setText(user.getRemarkName());
					} else {
						if (!Utils.isNull(user.getNick_name())) {
							vh.tv_myblog_name.setText(user.getNick_name());
						} else {
							vh.tv_myblog_name.setText(user.getTxInfor()
									.getContacts_person_name());
						}
					}
				}
				// 数量
				if (msgHeader != null) {
					vh.tv_blog_likednums.setText(msgHeader.getLikedNums() + "");
					vh.tv_blog_accessnums.setText(msgHeader.getAccessNums()
							+ "");
					vh.tv_blog_blognums.setText(msgHeader.getBlogNums() + "");
				}
				return convertView;

			}

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.sll_myblog_listitem,
						null);
				holder = new ViewHolder();
				holder.iv_myblog = (ImageView) convertView
						.findViewById(R.id.iv_myblog);
				holder.iv_myblog_musicplay = (ImageView) convertView
						.findViewById(R.id.iv_myblog_musicplay);
				holder.iv_myBlog_great = (ImageView) convertView
						.findViewById(R.id.iv_myBlog_great);
				holder.ll_myblog_music = (LinearLayout) convertView
						.findViewById(R.id.ll_myblog_music);
				holder.rl_myblog_musicall = (RelativeLayout) convertView
						.findViewById(R.id.rl_myblog_musicall);
				holder.rl_blog_timeandplace = (RelativeLayout) convertView
						.findViewById(R.id.rl_blog_timeandplace);
				holder.ll_myblog_musicplay = (LinearLayout) convertView
						.findViewById(R.id.ll_myblog_musicplay);
				holder.ll_myblog_down = (ImageView) convertView
						.findViewById(R.id.ll_myblog_down);
				holder.tv_myblog = (TextView) convertView
						.findViewById(R.id.tv_myblog);
				holder.tv_myblog_process = (TextView) convertView
						.findViewById(R.id.tv_myblog_process);
				holder.sb_myblog_process = (BlogMusicSeekBar) convertView
						.findViewById(R.id.sb_myblog_process);
				holder.tv_myBlog_great = (TextView) convertView
						.findViewById(R.id.tv_myBlog_great);
				holder.tv_myblog_play = (TextView) convertView
						.findViewById(R.id.tv_myblog_play);
				holder.iv_myblog_more = (ImageView) convertView
						.findViewById(R.id.iv_myblog_more);
				holder.iv_myblog_play = (ImageView) convertView
						.findViewById(R.id.iv_myblog_play);
				holder.gv_myblog_liked_img = (GridView) convertView
						.findViewById(R.id.gv_myblog_liked_img);
				holder.no_blog = (RelativeLayout) convertView
						.findViewById(R.id.no_blog);
				holder.tv_no_blog = (TextView) convertView
						.findViewById(R.id.tv_no_blog);
				holder.ll_blog = (LinearLayout) convertView
						.findViewById(R.id.ll_blog);
				holder.ll_myblog_place = (LinearLayout) convertView
						.findViewById(R.id.ll_myblog_place);
				holder.tv_myblog_place = (TextView) convertView
						.findViewById(R.id.tv_myblog_place);
				holder.tv_blog_time = (TextView) convertView
						.findViewById(R.id.tv_blog_time);

				convertView.setTag(holder);
				holders.add(holder);

			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (bmsgs != null && bmsgs.size() > 0) {
				holder.no_blog.setVisibility(View.GONE);
				holder.ll_blog.setVisibility(View.VISIBLE);
			} else {
				if (isShowNoBlog && msgHeader.getBlogNums() == 0) {
					holder.no_blog.setVisibility(View.VISIBLE);
					if (isMy) {
						holder.tv_no_blog.setText("你还没有发布瞬间哟~");
					} else {
						holder.tv_no_blog.setText("Ta还没有发布过瞬间哟~");
					}
					holder.ll_blog.setVisibility(View.GONE);
					myblog_refresh.setVisibility(View.VISIBLE);
				} else {
					holder.no_blog.setVisibility(View.GONE);
					holder.ll_blog.setVisibility(View.INVISIBLE);
					myblog_refresh.setVisibility(View.GONE);
				}
				return convertView;
			}

			BlogMsg blogMsg = bmsgs.get(position - 1);
			holder.blogMsg = blogMsg;
			holder.sb_myblog_process.setTag(blogMsg);
			holder.iv_myblog_play.setTag(blogMsg);
			holder.tv_myBlog_great.setTag(blogMsg.getMid());

			if (blogMsg.getLikednum() > 99) {
				setAddText(holder.tv_myBlog_great);
			} else {
				holder.tv_myBlog_great.setText(blogMsg.getLikednum() + "");
			}

			// 设置文字
			if (blogMsg.getMmsg() == null) {
				holder.tv_myblog.setVisibility(View.GONE);
			} else {
				if (blogMsg.getMmsg().trim().equals("")) {
					holder.tv_myblog.setVisibility(View.GONE);
				} else {
					holder.tv_myblog.setVisibility(View.VISIBLE);
					holder.tv_myblog.setText(blogMsg.getMmsg());
				}
			}
			// 设置点赞人
			MyBlogGridAdapter gridAdapter = new MyBlogGridAdapter(
					MyBlogActivity.this);
			List<Long> likedids = bmsgs.get(position - 1).getIdlist();
			gridAdapter.setData(likedids);
			holder.gv_myblog_liked_img.setAdapter(gridAdapter);
			// 设置喜欢图片
			if (!isMy) {
				if (blogMsg.getIdlist() == null) {
					if (blogMsg.getLikedType() == 1) {
						holder.iv_myBlog_great
								.setImageResource(R.drawable.blog_liked);
					} else {
						holder.iv_myBlog_great
								.setImageResource(R.drawable.myblog_great);
					}
				} else {
					if (blogMsg.getLikedType() == 1
							|| blogMsg.getIdlist().contains(TX.tm.getUserID())) {
						holder.iv_myBlog_great
								.setImageResource(R.drawable.blog_liked);
					} else {
						holder.iv_myBlog_great
								.setImageResource(R.drawable.myblog_great);
					}
				}

			}

			// 设置音频
			if (blogMsg.getAduTime() != 0) {
				int time = (int) blogMsg.getAduTime();
				holder.tv_myblog_process.setText(MessageUtil
						.getRecordTime(time));
				holder.sb_myblog_process.setMax(time * 1000);
			}

			// 设置音频是否下载
			if (Utils.isNull(blogMsg.getAduPath())) {
				holder.ll_myblog_down.setVisibility(View.VISIBLE);
				holder.ll_myblog_musicplay.setVisibility(View.GONE);
			} else {
				holder.ll_myblog_down.setVisibility(View.GONE);
				holder.ll_myblog_musicplay.setVisibility(View.VISIBLE);
			}

			// 设置瞬间类型
			blogMsg = opea.checkBlogType(blogMsg);
			switch (blogMsg.getType()) {
			case BlogMsg.IMG:
				holder.iv_myblog.setVisibility(View.VISIBLE);
				holder.ll_myblog_music.setVisibility(View.GONE);
				holder.iv_myblog_musicplay.setVisibility(View.GONE);
				holder.iv_myblog_musicplay.clearAnimation();
				holder.iv_myblog.setImageResource(R.drawable.blog_img_fail);
				holder.rl_blog_timeandplace
						.setBackgroundResource(R.color.myblog_img);

				Bitmap img = opea.getcatchImg(blogMsg.getMid());
				final ImageView iv = holder.iv_myblog;
				iv.setTag(blogMsg.getMid());
				if (img == null) {
					// 异步加载/下载图片
					loadAlbumImg(blogMsg.getMid(), blogMsg.getImgUrl(),
							mBlogImgAsynCallback, blogMsg);
				} else {
					holder.iv_myblog.setImageBitmap(img);
				}
//				System.gc();
				img = null;

				break;
			case BlogMsg.AUDIO:
				holder.iv_myblog_musicplay.setVisibility(View.VISIBLE);
				holder.ll_myblog_music.setVisibility(View.VISIBLE);
				holder.iv_myblog.setVisibility(View.VISIBLE);
				holder.iv_myblog.setImageResource(R.drawable.myblog_music);
				holder.rl_blog_timeandplace
						.setBackgroundResource(R.color.myblog_noimg);
				if (playHolder != null) {
					if (playRecordId == holder.blogMsg.getMid()) {
						// 说明正在播放的
						holder.iv_myblog_play
								.setImageResource(R.drawable.stop_music);
						holder.tv_myblog_play.setText("Stop");
					} else {
						holder.iv_myblog_play
								.setImageResource(R.drawable.myblog_play);
						holder.tv_myblog_play.setText("Play");
						holder.sb_myblog_process.setProgress(0);
					}
				}

				break;
			case BlogMsg.IMG_AUDIO:
				holder.iv_myblog_musicplay.setVisibility(View.GONE);
				holder.iv_myblog_musicplay.clearAnimation();
				holder.ll_myblog_music.setVisibility(View.VISIBLE);
				holder.iv_myblog.setVisibility(View.VISIBLE);
				holder.iv_myblog.setImageResource(R.drawable.blog_img_fail);
				holder.rl_blog_timeandplace
						.setBackgroundResource(R.color.myblog_img);
				if (playHolder != null) {
					if (playRecordId == holder.blogMsg.getMid()) {
						// 说明正在播放的
						holder.iv_myblog_play
								.setImageResource(R.drawable.stop_music);
						holder.tv_myblog_play.setText("Stop");
					} else {
						holder.iv_myblog_play
								.setImageResource(R.drawable.myblog_play);
						holder.tv_myblog_play.setText("Play");
						holder.sb_myblog_process.setProgress(0);
					}
				}
				Bitmap img_audio = opea.getcatchImg(blogMsg.getMid());
				final ImageView iv_ad = holder.iv_myblog;
				iv_ad.setTag(blogMsg.getMid());
				if (img_audio == null) {
					// 下载图片
					loadAlbumImg(blogMsg.getMid(), blogMsg.getImgUrl(),
							mBlogImgAsynCallback, blogMsg);
				} else {
					holder.iv_myblog.setImageBitmap(img_audio);
				}
//				System.gc();
				img_audio = null;
				break;
			case BlogMsg.MSG:
				holder.iv_myblog.setVisibility(View.GONE);
				holder.ll_myblog_music.setVisibility(View.GONE);
				holder.iv_myblog_musicplay.setVisibility(View.GONE);
				holder.iv_myblog_musicplay.clearAnimation();
				holder.rl_blog_timeandplace
						.setBackgroundResource(R.color.myblog_noimg);
				break;
			}

			// 设置时间
			long time = blogMsg.getTime();
			String dealDate = opea.dealDate(time);
			holder.tv_blog_time.setText(dealDate);

			// 设置地理位置
			if (blogMsg.getCity() != null && !blogMsg.getCity().equals("")
					&& !blogMsg.getCity().equals("null")) {
				holder.ll_myblog_place.setVisibility(View.VISIBLE);
				holder.tv_myblog_place.setText(blogMsg.getCity());
			} else {
				holder.ll_myblog_place.setVisibility(View.GONE);
			}

			BlogOnClickListener onclicklistener = new BlogOnClickListener(
					position - 1, holder);
			// 点击事件
			holder.iv_myblog_more.setOnClickListener(onclicklistener);
			holder.iv_myblog.setOnClickListener(onclicklistener);
			holder.rl_myblog_musicall.setOnClickListener(onclicklistener);
			holder.iv_myBlog_great.setOnClickListener(onclicklistener);
			return convertView;
		}
	}

	List<ImageView> imageviews = new ArrayList<ImageView>();

	private class MyBlogGridAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private List<Long> idlist;

		private void setData(List<Long> match) {
			idlist = match;
		}

		public MyBlogGridAdapter(Context context) {
			this.mInflater = LayoutInflater.from(context);
			// txs=new ArrayList<TX>();
		}

		public int getCount() {
			// noContacts.setVisibility(View.VISIBLE);
			if (idlist != null) {
				if (idlist.size() > 4) {
					return 4;
				} else {
					return idlist.size();
				}
			} else {
				return 0;
			}
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {

			ViewHolder holder = null;
			if (convertView == null) {
				convertView = mInflater.inflate(R.layout.sll_myblog_grid_item,
						null);
				holder = new ViewHolder();
				holder.griditem_img = (ImageView) convertView
						.findViewById(R.id.griditem_img);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			final long uid = idlist.get(position);
			holder.griditem_img.setTag(uid);
			imageviews.add(holder.griditem_img);
			if (usermap != null && usermap.size() > 0) {
				TX user = usermap.get(uid);
				holder.griditem_img.setTag(uid);
				if (user != null) {
					holder.griditem_img.setImageBitmap(mSess
							.getDefaultPartnerAvatar(user.getSex()));

					Bitmap avatar = mSess.avatarDownload.getAvatar(
							user.avatar_url, uid, holder.griditem_img,
							avatarHandler);
					if (avatar != null) {
						holder.griditem_img.setImageBitmap(avatar);
					}
				} else {
					TX me = TX.tm.getTxMe();
					holder.griditem_img.setImageBitmap(mSess
							.getDefaultPartnerAvatar(me.getSex()));
					Bitmap avatar = mSess.avatarDownload.getAvatar(
							me.avatar_url, TX.tm.getUserID(),
							holder.griditem_img, avatarHandler);
					if (avatar != null) {
						holder.griditem_img.setImageBitmap(avatar);
					}
				}
			}
			holder.griditem_img.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {

					if (uid == TX.tm.getUserID()) {
						Intent iUserInfo = new Intent(MyBlogActivity.this,
								SetUserInfoActivity.class);
						startActivity(iUserInfo);
					} else {
						Intent iSupplement = new Intent(MyBlogActivity.this,
								UserInformationActivity.class);
						iSupplement.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
						iSupplement
								.putExtra(
										UserInformationActivity.pblicInfo,
										TX.tm.isTxFriend(uid) == true ? UserInformationActivity.TUIXIN_USER_INFO
												: UserInformationActivity.NOT_TUIXIN_USER_INFO);
						iSupplement.putExtra(UserInformationActivity.UID, uid);
						startActivity(iSupplement);
					}
				}
			});
			return convertView;
		}
	}

	public class BlogOnClickListener implements OnClickListener {

		private int position;
		private String[] items;
		private ViewHolder holder;

		public BlogOnClickListener(int position, ViewHolder holder) {
			this.position = position;
			this.holder = holder;
		}

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			// 更多事件
			case R.id.iv_myblog_more:

				if (isMy) {
					items = new String[] { "删除" };
				} else {
					if (TxData.isOP()) {
						items = new String[] { "举报", "删除" };
					} else {
						items = new String[] { "举报" };
					}
				}

				new AlertDialog.Builder(MyBlogActivity.this).setItems(items,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

								String s = items[which];
								if (s.equals("举报")) {
									Intent i = new Intent(MyBlogActivity.this,
											GroupTip.class);
									i.putExtra(GroupTip.UID, uid);// txmsg.partner_id
									i.putExtra(REPORT_BLOG, true);
									i.putExtra("mid", list.get(position)
											.getMid() + "");
									startActivity(i);
								} else if (s.equals("删除")) {
									delblog = list.get(position);
									recoedStop();
									opea.delete(list.get(position), uid);
									showProgressDialog();
								}
							}
						}).show();
				break;
			// 打开大图
			case R.id.iv_myblog:
				BlogMsg imgBlog = list.get(position);
				if (imgBlog.getType() == BlogMsg.IMG
						|| imgBlog.getType() == BlogMsg.IMG_AUDIO) {
					Intent bigImgIntent = new Intent(MyBlogActivity.this,
							CheckBigImgActivity.class);
					bigImgIntent.putExtra(CheckBigImgActivity.IMG_PATH,
							imgBlog.getImgPath());
					bigImgIntent.putExtra(CheckBigImgActivity.IMG_URL,
							imgBlog.getImgUrl());

					startActivity(bigImgIntent);
				}
				break;
			// 喜欢
			case R.id.iv_myBlog_great:
				if (!isMy) {
					BlogMsg likeBlog = list.get(position);
					int likedType = likeBlog.getLikedType();
					if (likeBlog.getIdlist() != null
							&& likeBlog.getIdlist().contains(TX.tm.getUserID())) {
						// 返回的likedidlist里已经存在id
						showToast("您已经执行过喜欢了。。");
					} else {
						if (likedType == 0) {
							// //返回的likedidlist里不存在id，并且该本地保存的瞬间状态为没喜欢过
							if (Utils
									.checkNetworkAvailable1(MyBlogActivity.this)) {
								showProgressDialog();
								opea.islikeBlog(likeBlog, true, uid);
								likeBlog.setLikedType(1);
							} else {
								showToast("请确认网络");
							}
						} else {
							showToast("您已经执行过喜欢了。。");
						}
					}
				}
				break;
			// 播放音频
			case R.id.rl_myblog_musicall:
				BlogMsg blogMsg_play = list.get(position);

				if (!Utils.isNull(blogMsg_play.getAduPath())) {
					recoedPlay(blogMsg_play, holder);
				} else {
					// TODO 要怎么提示用户
					if (isdowning) {
						showToast("正在下载音频", true);
					} else {
						opea.DownAduioScoket(blogMsg_play);
					}

				}
				break;
			}
		}
	}

	private TXMessage txmsg;
	private ViewHolder playHolder;

	public void recoedPlay(BlogMsg blogMsg_play, ViewHolder holder) {

		if (!Utils.checkSDCard()) {// 无SD卡
			return;
		}

		holder.ll_myblog_musicplay.setVisibility(View.VISIBLE);
		holder.ll_myblog_down.setVisibility(View.GONE);
		if (txmsg == null) {
			txmsg = new TXMessage(blogMsg_play.getAduPath(),
					blogMsg_play.getAduUrl(), blogMsg_play.getAduTime());
			txmsg.gmid = blogMsg_play.getMid();
		} else {
			if (txmsg.gmid != blogMsg_play.getMid()) {
				if (playHolder != null) {
					opea.stopPlayAudioRecord();
					playHolder.iv_myblog_musicplay.clearAnimation();
					playHolder.iv_myblog_play
							.setImageResource(R.drawable.myblog_play);
					playHolder.tv_myblog_play.setText("Play");
					playHolder.sb_myblog_process.setProgress(0);
					opea.flag = false;
					playHolder = null;
				}
				txmsg = new TXMessage(blogMsg_play.getAduPath(),
						blogMsg_play.getAduUrl(), blogMsg_play.getAduTime());
				txmsg.gmid = blogMsg_play.getMid();
			}
		}

		if (txmsg.PlayAudio == RECORD_PLAY) {

			if (holder.blogMsg.getType() == BlogMsg.AUDIO) {
				holder.iv_myblog_musicplay.clearAnimation();
			}

			holder.iv_myblog_play.setImageResource(R.drawable.myblog_play);
			holder.tv_myblog_play.setText("Play");
			playHolder = null;
			holder.sb_myblog_process.setProgress(0);
			opea.stopPlayAudioRecord();
		} else if (txmsg.PlayAudio == RECORD_PAUSE) {

			if (holder.blogMsg.getType() == BlogMsg.AUDIO) {
				holder.iv_myblog_musicplay.setAnimation(opea.getAnim());
			}

			holder.iv_myblog_play.setImageResource(R.drawable.stop_music);
			holder.tv_myblog_play.setText("Stop");
			holder.blogMsg.setAdu_process(0);
			playHolder = holder;
			playRecordId = playHolder.blogMsg.getMid();
			opea.playAudioRecord(txmsg, txmsg.gmid);
		}

	}

	// 停止播放音频
	public void recoedStop() {
		if (playHolder != null) {
			opea.stopPlayAudioRecord();
			playHolder.iv_myblog_musicplay.clearAnimation();
			playHolder.iv_myblog_play.setImageResource(R.drawable.myblog_play);
			playHolder.tv_myblog_play.setText("Play");
			playHolder.sb_myblog_process.setProgress(0);
			opea.flag = false;
			playHolder = null;
		}
	}

	public final class ViewHolder {
		public ImageView iv_myblog;
		public ImageView iv_myBlog_great;
		public ImageView iv_myblog_musicplay;
		public ImageView iv_myblog_more;
		public ImageView iv_myblog_play;
		public ImageView griditem_img;
		public ImageView ll_myblog_down;
		public LinearLayout ll_myblog_music;
		public LinearLayout ll_myblog_musicplay;
		public TextView tv_myblog;
		public TextView tv_no_blog;
		public TextView tv_myblog_process;
		public TextView tv_myBlog_great;
		public TextView tv_myblog_play;
		public TextView tv_myblog_place;
		public TextView tv_blog_time;
		public GridView gv_myblog_liked_img;
		public BlogMusicSeekBar sb_myblog_process;
		public BlogMsg blogMsg;
		public RelativeLayout no_blog;
		public RelativeLayout rl_myblog_musicall;
		public RelativeLayout rl_blog_timeandplace;
		public LinearLayout ll_blog;
		public LinearLayout ll_myblog_place;
	}

	private final class HeadViewHolder {
		public ImageView iv_myblog_head;
		public TextView tv_myblog_name;
		public TextView tv_blog_blognums;
		public TextView tv_blog_accessnums;
		public TextView tv_blog_likednums;
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub

		recoedStop();
		super.onPause();
		MobclickAgent.onPause(this);
	}

	@Override
	protected void onRestart() {
		// TODO Auto-generated method stub
		super.onRestart();
	}

	@Override
	protected void onResume() {
		if (isMy && mSess.getLikeNoticeDao().hasUnreadLikedNotice()) {
			iv_myblog_msg.setVisibility(View.VISIBLE);
		} else {
			iv_myblog_msg.setVisibility(View.GONE);
		}
		super.onResume();
		MobclickAgent.onResume(this);
	}
	

	@Override
	protected void onStart() {
		opea.setHandler(handler);
		// TODO Auto-generated method stub
		super.onStart();
	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		mSess.getLikeNoticeDao().unregistReceiveNoticeListener();// 反注册接受瞬间被喜欢的通知
		// 释放占用的资源
		opea.recycle();
		uid = (long) 0;
		msg_header = null;
		super.onDestroy();
	}

	private ProgressDialog pd;

	private void showProgressDialog() {
		if (pd == null) {
			pd = new ProgressDialog(this);
			pd.setMessage("正在操作，请稍后");
			pd.setCancelable(false);
		}
		if (!pd.isShowing()) {
			pd.show();
		}
	}

	private void cancelProgressDialog() {
		if (pd != null && pd.isShowing()) {
			pd.cancel();
		}
	}

	public void like_success(ViewHolder holder, BlogMsg likeblog) {
		int like = Integer
				.parseInt(holder.tv_myBlog_great.getText().toString()) + 1;

		msg_header.setLikedNums(msg_header.getLikedNums() + 1);
		myBlogAdapter.setHeaderData(msg_header);
		mHeadHolder.tv_blog_likednums.setText(msg_header.getLikedNums() + "");
		holder.tv_myBlog_great.setText(like + "");

		// 在喜欢的人中加入自己

		if (!holder.blogMsg.getIdlist().contains(TX.tm.getUserID())) {
			usermap.put(TX.tm.getUserID(), TX.tm.getTx(TX.tm.getUserID()));

			holder.blogMsg.getIdlist().add(0, TX.tm.getUserID());
			holder.blogMsg.setLikednum(like);
			holder.blogMsg.setLikedType(BlogMsg.LIKED);
			holder.iv_myBlog_great.setImageResource(R.drawable.blog_liked);
			MyBlogGridAdapter adapter = (MyBlogGridAdapter) holder.gv_myblog_liked_img
					.getAdapter();
			List<Long> likedids = holder.blogMsg.getIdlist();
			adapter.setData(likedids);
			adapter.notifyDataSetChanged();

			likeblog.setLikedType(BlogMsg.LIKED);
			likeblog.setLikednum(like);
			mSess.getBlogDao().updateLikeType(likeblog);
		}

		// TODO 需不需要刷新数据
	}

	public void deal_delete() {

		if (delblog == null) {
			return;
		}

		List<Long> mids = new ArrayList<Long>();
		if (list != null && list.contains(delblog)) {
			list.remove(delblog);
		}

		// 该条瞬间喜欢的人数
		int likenum = (int) delblog.getLikednum();
		// 瞬间总数-1
		msg_header.setBlogNums(msg_header.getBlogNums() - 1);
		msg_header.setLikedNums(msg_header.getLikedNums() - likenum);
		ContentValues values = new ContentValues();
		values.put(TxDB.Tx.BLOG_INFOR, "0," + msg_header.getAccessNums() + ",0");

		TX.tm.updateTx(uid, values);
		myBlogAdapter.setHeaderData(msg_header);
		myBlogAdapter.setData(list);
		mSess.getBlogDao().delete(delblog.getMid());
		mSess.getLikeNoticeDao().delete(delblog.getMid() + "");
		myBlogAdapter.notifyDataSetChanged();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode == RESULT_OK) {
			if (requestCode == REQUEST_ADD_BLOG) {

				getBlogData(uid, 0);
				if (list != null && list.size() > 0) {
					list.clear();
				}
				isShowNoBlog = false;
			}
		}
	}

	/**
	 * 获取数据
	 * 
	 * @param uid
	 */
	public void getBlogData(Long uid, long beginMid) {
		tuixin_blog_title.setVisibility(View.GONE);
		blog_loading.setVisibility(View.VISIBLE);
		if (Utils.checkNetworkAvailable1(MyBlogActivity.this)) {
			opea.getData(uid, beginMid);
		} else {
			String blog_head_msg = TX.tm.getTxMe().blog_head_msg;
			int blognums = 0;
			if (!Utils.isNull(blog_head_msg)) {
				String[] split = blog_head_msg.split(",");
				if (msg_header == null)
					msg_header = new BlogMsg();
				msg_header.setBlogNums(Integer.parseInt(split[0]));
				msg_header.setAccessNums(Integer.parseInt(split[1]));
				msg_header.setLikedNums(Integer.parseInt(split[2]));
				myBlogAdapter.setHeaderData(msg_header);
			}
			list = mSess.getBlogDao().findBlogMsgByUid(uid);
			if (list != null && list.size() >= blognums) {
				isEnd = true;
			}
			isShowNoBlog = true;
			isShowAvatuarl = true;
			tuixin_blog_title.setVisibility(View.VISIBLE);
			blog_loading.setVisibility(View.GONE);
			myBlogAdapter.setData(list);
			myBlogAdapter.notifyDataSetChanged();
		}
	}

	// 下载图片
	// 这个加载不能用BaseActivity中的，因为这个相册的图片不能被圆角化
	Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				Object[] obj = (Object[]) msg.obj;
				CallInfo callinfo = (CallInfo) obj[0];
				callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
				break;
			}
			}
		};
	};
	Handler mAsynloader;
	private ImageView iv_myblog_msg;
	private View myblog_refresh;
	private LinearLayout loadmore;
	private long playRecordId;

	public void prepairAsyncload() {
		mAsynloader = new Handler(SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {

			}
		};
	}

	private TuixinQueue<Message> loadQueue = new TuixinQueue<Message>(2, 200,
			new QueneCallBack() {

				@Override
				public void handle(Message msg) {
					if (msg != null) {
						// mAvatarHandler.sendMessage(msg);
						CallInfo ci;
						switch (msg.what) {
						case 1:
							Object[] obj = (Object[]) msg.obj;
							ci = (CallInfo) obj[0];
							final BlogMsg blogmsg = (BlogMsg) obj[1];
							if (ci.mUrl == null)
								break;
							String file = mSess.mDownUpMgr.getAlbumFile(
									ci.mUrl, false);
							if (file == null)
								break;
							File avatar = new File(file);
							if (avatar.exists()) {
								// blogmsg.setImg(Utils.readBitMap(file, true));
								Bitmap bitmap = opea.getBitmap(file);
								if (bitmap != null) {
									blogmsg.setImgPath(file);

									bitmap = opea.dealBitmap(bitmap);
									// bitmap = opea.compressImage(bitmap);
									opea.cachImg(blogmsg.getMid(), bitmap);

									ci.mBitmap = bitmap;
									Object[] objblog = new Object[] { ci,
											blogmsg };
									mSess.getBlogDao().update(blogmsg); // 更新数据库
									// TaskFinish.setLoadFinished(true);
									mAvatarHandler.obtainMessage(1, objblog)
											.sendToTarget();
									// System.gc();
									break;
								}
							} else {
								downQueue.add(msg);
								downQueue.waitTask();

							}
							break;
						}
					}
				}

			}, 0);
	private TuixinQueue<Message> downQueue = new TuixinQueue<Message>(2, 200,
			new QueneCallBack() {

				@Override
				public void handle(Message msg) {
					Object[] downobj = (Object[]) msg.obj;
					final BlogMsg blogmsg = (BlogMsg) downobj[1];
					CallInfo ci = (CallInfo) downobj[0];
					String downfile = mSess.mDownUpMgr.getAlbumFile(ci.mUrl,
							false);
					mSess.mDownUpMgr.downloadImg(ci.mUrl, downfile, 1, true,
							true, new FileTransfer.DownUploadListner() {
								// 此处要记录文件名，同时要加载
								@Override
								public void onFinish(FileTaskInfo taskInfo) {
									Bitmap bitmap = opea
											.getBitmap(taskInfo.mFullName);
									if (bitmap != null) {
										TaskFinish.setDownFinished(true);
										String storagePath = Utils
												.getStoragePath(MyBlogActivity.this);
										final File sddir = new File(
												storagePath, Utils.IMAGE_PATH);
										String fileName = taskInfo.mTime + "";
										try {
											Utils.creatBlogFile(
													taskInfo.mFullName, sddir,
													fileName);
											CallInfo ci = (CallInfo) taskInfo.mObj;

											bitmap = opea.dealBitmap(bitmap);
											// bitmap =
											// opea.compressImage(bitmap);
											ci.mBitmap = bitmap;
											blogmsg.setImgPath(taskInfo.mFullName);
											opea.cachImg(blogmsg.getMid(),
													blogmsg.getImg());
											Object[] obj = new Object[] { ci,
													blogmsg };
											mSess.getBlogDao().update(blogmsg); // 更新数据库
											mAvatarHandler
													.obtainMessage(1, obj)
													.sendToTarget();
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
									// System.gc();
								}

							}, ci);
				}
			}, 1);

	protected void loadAlbumImg(long tag, String avatar_url,
			AsyncCallback<Bitmap> callback, BlogMsg blogmsg) {
		CallInfo callinfo = new CallInfo(avatar_url, tag, callback);
		Object[] obj = new Object[] { callinfo, blogmsg };
		Message msg = new Message();
		msg.what = 1;
		msg.obj = obj;
		loadQueue.add(msg);
		loadQueue.waitTask();
	}

	public void setAddText(TextView tv) {
		SpannableString msp = new SpannableString("99+");

		msp.setSpan(new RelativeSizeSpan(0.7f), 2, 3,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 0.5f表示默认字体大小的一半
		msp.setSpan(new SuperscriptSpan(), 2, 3,
				Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

		tv.setText(msp);
		tv.setMovementMethod(LinkMovementMethod.getInstance());

	}

}
