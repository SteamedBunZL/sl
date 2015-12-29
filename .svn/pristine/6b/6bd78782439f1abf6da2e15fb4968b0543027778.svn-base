package com.tuixin11sms.tx.activity;

import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AbsListView.OnScrollListener;

import com.shenliao.user.activity.UserInformationActivity;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.core.SmileyParser;
import com.tuixin11sms.tx.data.TxDB.Blog;
import com.tuixin11sms.tx.model.BlogMsg;
import com.tuixin11sms.tx.model.BlogNoticeMsg;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.MessageUtil;
import com.tuixin11sms.tx.utils.Utils;

public class LikeNoticeActivity extends BaseActivity implements OnClickListener {
	private static final String TAG = "LikeNoticeActivity";
	private static final int HANDLE_VIEW = 0x10;// 处理view的handler标记
	private static final String BLOG_ID = "blogId";// 瞬间id
	protected Toast unInitRecordToast;
	private ListView lv_blog_notice;
	private ViewGroup ll_loading_notices;//加载中动画
	private BlogAdapter adapter;
	private SmileyParser smileyParser;
	private List<BlogNoticeMsg> list;
	private ParseHandler blogImgHandler;
	
	private View noLikeNoticeBg = null;//没有被赞提醒空背景
	
	
	// 处理界面的handler
	private Handler viewHandler = new WrappedHandler(this) {

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case HANDLE_VIEW:
				//用执行回调设置view
				if(msg.obj!=null&&(msg.obj instanceof ImageView)){
					long blogId = msg.getData().getLong(BLOG_ID);
					ImageView iv_thumb = (ImageView)msg.obj;
					
					if(blogId == (Long)iv_thumb.getTag())
						iv_thumb.setImageBitmap(blogThumbCache.get(blogId).get());
				}
				
				break;

			default:
				break;
			}
		};
	};
	
	/**只是处理Runnable线程队列的handler*/
	private final class ParseHandler extends Handler {

		public ParseHandler(Looper looper) {
			super(looper);
		}

	}


	private Map<Long, SoftReference<Bitmap>> blogThumbCache = new HashMap<Long, SoftReference<Bitmap>>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.activity_myblog_notice);

		HandlerThread handlerThread = new HandlerThread("HandleImage");
		handlerThread.start();
		blogImgHandler = new ParseHandler(handlerThread.getLooper());

		smileyParser = new SmileyParser(this);

		adapter = new BlogAdapter();

		list = mSess.getLikeNoticeDao().onEnterNoticePage();
		
		
		initView();

	}

	protected void initView() {
		
		LinearLayout ll_back = (LinearLayout) findViewById(R.id.btn_back_blognotice);
		ll_back.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				thisContext.finish();
			}
		});
		
		noLikeNoticeBg = findViewById(R.id.rl_no_like_notice);
		if(list.size()>0){
			noLikeNoticeBg.setVisibility(View.GONE);
		}
		
		lv_blog_notice = (ListView) this
				.findViewById(R.id.lv_myblog_notice_list);

		lv_blog_notice.setAdapter(adapter);
		ll_loading_notices = (ViewGroup)findViewById(R.id.ll_loading_notices);
		ll_loading_notices.setVisibility(View.INVISIBLE);//刚进来先隐藏加载中进度条
		
		if(Utils.debug){
			lv_blog_notice.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {
					showToast("BlogId="+list.get(position).getBlogId());
				}
			});
		}
		
		lv_blog_notice.setOnScrollListener(new OnScrollListener() {
			
			@Override
			public void onScrollStateChanged(AbsListView view, int scrollState) {
				if(scrollState == OnScrollListener.SCROLL_STATE_IDLE){
					//停止滚动时
					if(adapter.getCount()>0&&lv_blog_notice.getLastVisiblePosition()+1==adapter.getCount()){
						//最后一条已经显示
						if(!mSess.getLikeNoticeDao().isEndOfList()){
							ll_loading_notices.setVisibility(View.VISIBLE);
							list = mSess.getLikeNoticeDao().getNoticeLocalList();
							adapter.notifyDataSetChanged();
							ll_loading_notices.setVisibility(View.GONE);
						}
					}
				}
			}
			
			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
				
			}
			
		});
		
		
	}

	public class BlogAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return list == null ? 0 : list.size();
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			final ViewHolder viewHolder;
			if (convertView == null) {
				viewHolder = new ViewHolder();
				convertView = View.inflate(thisContext,
						R.layout.sll_item_blog_notice, null);
				viewHolder.time = (TextView) convertView
						.findViewById(R.id.blog_tv_time);
				viewHolder.adutime = (TextView) convertView
						.findViewById(R.id.blog_tv_adutime);
				viewHolder.headimg = (ImageView) convertView
						.findViewById(R.id.blog_iv_headimg);
				viewHolder.displayName = (TextView) convertView
						.findViewById(R.id.blog_tv_name);
				viewHolder.img = (ImageView) convertView
						.findViewById(R.id.blog_iv_img);

				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			final BlogNoticeMsg blogNoticeMsg = list.get(position);
			viewHolder.displayName.setText(smileyParser.addSmileySpans(
					blogNoticeMsg.getNikeName(), true, 0));
			viewHolder.time
					.setText(Utils.formatTimeStr(blogNoticeMsg.getTime(),true));
			viewHolder.img.setTag(blogNoticeMsg.getBlogId());
			
			if(Utils.debug){
				Log.i(TAG, "Blog信息：图片本地路径："+blogNoticeMsg.getBlogMsg().getImgPath()+"##Url="+blogNoticeMsg.getBlogMsg().getImgUrl()+"##blogId="+blogNoticeMsg.getBlogMsg().getMid());
			}
			
			if (!TextUtils.isEmpty(blogNoticeMsg.getImagePath())) {
				// 有图片本地路径，优先显示图片
				SoftReference<Bitmap> imageCache = blogThumbCache
						.get(blogNoticeMsg.getBlogId());
				Bitmap bm = imageCache==null?null:imageCache.get();
				if (bm != null) {
					viewHolder.img.setImageBitmap(bm);
				} else {
					viewHolder.img.setImageResource(R.drawable.msg_img_download_failed);
					loadLocalThumb(viewHolder.img, blogNoticeMsg);
				}

			}else if (!TextUtils.isEmpty(blogNoticeMsg.getImageUrl())) {
				//此if else功能正常，但是不会走到这个判断中，备用 2014.06.11 shc
				//有图片url,则下载图片
				viewHolder.img.setImageResource(R.drawable.msg_img_download_failed);
				String imgFilePath = mSess.mDownUpMgr.getImageFile(blogNoticeMsg.getImageUrl(),
						FileTransfer.SRC_TYPE_DEFAULT, System.currentTimeMillis(), false);
				mSess.mDownUpMgr.downloadImg(blogNoticeMsg.getImageUrl(), imgFilePath, 1, false, true, new FileTransfer.DownUploadListner() {
					
					@Override
					public void onFinish(FileTaskInfo taskInfo) {
						if(taskInfo.mObj instanceof BlogNoticeMsg){
							BlogNoticeMsg notice = (BlogNoticeMsg)taskInfo.mObj;
							notice.getBlogMsg().setImgPath(taskInfo.mFullName);
							mSess.getBlogDao().update(notice.getBlogMsg());//更新数据库
							if(notice!=null&&((Long)viewHolder.img.getTag()==notice.getBlogId())){
								//是对应的blogMsg
								loadLocalThumb(viewHolder.img, notice);
							}
						}
						
					}
					@Override
					public void onError(FileTaskInfo taskInfo, int iCode,
							Object iPara) {
						if(Utils.debug){
							Log.i(TAG, "下载失败blogId="+blogNoticeMsg.getBlogId()+",url="+blogNoticeMsg.getImageUrl());
						}
					}
				}, blogNoticeMsg);
				
			} else {
				// 有图片先设置图片
				if(TextUtils.isEmpty(blogNoticeMsg.getBlogMsg().getAduUrl())){
					//纯文本图片
					viewHolder.img.setImageResource(R.drawable.blog_text_default_bg);
				}else {
					//无图，有音频
					viewHolder.img.setImageResource(R.drawable.blog_adu_notice);
					viewHolder.adutime.setVisibility(View.VISIBLE);
					viewHolder.adutime.setText(MessageUtil
							.getRecordTime(blogNoticeMsg.getAudioTime()));
				}

			}

			viewHolder.headimg.setTag(blogNoticeMsg.getUid());
			Bitmap headBm = mSess.avatarDownload.getHeadIcon(blogNoticeMsg.getUid(), new AsyncCallback<Bitmap>() {
				
				@Override
				public void onSuccess(Bitmap result, long id) {
					if(result!=null&&(Long)viewHolder.headimg.getTag()==id){
						viewHolder.headimg.setBackgroundDrawable(new BitmapDrawable(result));
					}
					
				}
				
				@Override
				public void onFailure(Throwable t, long id) {
					
				}
			});
			if(headBm!=null){
				viewHolder.headimg.setBackgroundDrawable(new BitmapDrawable(headBm));
			}
			
			viewHolder.headimg.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent iSupplement = new Intent(LikeNoticeActivity.this,
							UserInformationActivity.class);
					iSupplement.putExtra(UserInformationActivity.UID, blogNoticeMsg.getUid());
					startActivity(iSupplement);

				}
			});
			
			viewHolder.displayName.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent iSupplement = new Intent(LikeNoticeActivity.this,
							UserInformationActivity.class);
					iSupplement.putExtra(UserInformationActivity.UID, blogNoticeMsg.getUid());
					startActivity(iSupplement);

				}
			});

			return convertView;
		}

		private void loadLocalThumb(final ImageView iv,
				final BlogNoticeMsg blogNoticeMsg) {
			blogImgHandler.post(new Runnable() {
				
				@Override
				public void run() {
					try {
						Bitmap lbm = null;
						if(!TextUtils.isEmpty(blogNoticeMsg.getBlogMsg().getImgUrl())){
							//图片缩略图
							lbm = Utils.readBitMap(
									blogNoticeMsg.getImagePath(), false);
						}else {
							//文字截图缩略图
							lbm = Utils.imageCropFromStart(Utils.readBitMap(
									blogNoticeMsg.getImagePath(), false));
						}
						
						if (lbm != null) {
							blogThumbCache.put(blogNoticeMsg.getBlogId(),
									new SoftReference<Bitmap>(lbm));
							Message msg = Message.obtain();
							msg.what = HANDLE_VIEW;
							Bundle bd = new Bundle();
							bd.putLong(BLOG_ID, blogNoticeMsg.getBlogId());
							msg.setData(bd);
							msg.obj = iv;
							viewHandler.sendMessage(msg);
						}
					} catch (Exception e) {
						if (Utils.debug) {
							Log.e(TAG, "加载图片异常", e);
						}
					}
				}
			});
		}
	}

	public class ViewHolder {
		TextView time;
		TextView adutime;
		ImageView headimg;
		TextView displayName;
		ImageView img;
	}

	@Override
	protected void onRestart() {
		super.onRestart();
	}

	@Override
	protected void onStart() {

		super.onStart();
	}

	@Override
	protected void onDestroy() {
		TxData.popActivityRemove(this);
		super.onDestroy();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_left:
			LikeNoticeActivity.this.finish();
			break;
		}
	}


}
