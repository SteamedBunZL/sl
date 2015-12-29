package com.tuixin11sms.tx.gif;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.SessionManager.UserLoginInfor;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.activity.InviteContactsActivity.ConViewHolder;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.activity.explorer.FolderExplorerActivity;
import com.tuixin11sms.tx.core.MyPopupWindow.newAdapter;
import com.tuixin11sms.tx.gif.XListView.IXListViewListener;
import com.tuixin11sms.tx.group.GifTransfer;
import com.tuixin11sms.tx.group.GifTransfer.GifDownUploadListner;
import com.tuixin11sms.tx.group.GifTransfer.GifDownloadTask;
import com.tuixin11sms.tx.group.GifTransfer.GifListDownloadTask;
import com.tuixin11sms.tx.group.GifTransfer.GifPicListDownloadTask;
import com.tuixin11sms.tx.group.GifTransfer.GifTaskInfo;
import com.tuixin11sms.tx.model.Hobby;
import com.tuixin11sms.tx.utils.Utils;

/**
 * Gif包下载的activity,现在已经被gifpackageDownactivity取代
 * 
 * @author SHC
 * 
 */
public class GifDownActivity extends BaseActivity implements OnClickListener {
	public static final String TAG = "GifDownActivity";
	private LinearLayout ll_from_sd;
	private ListView lv_gif_bag_down;
	private TextView tv_set;
	private LinearLayout btn_back_left;
	private ArrayList<GifDownResource> mList = new ArrayList<GifDownActivity.GifDownResource>();
	private LinkedList<ViewHolder> holderList = new LinkedList<GifDownActivity.ViewHolder>();
	private ExecutorService threadPools;
	private ArrayList<String> gif_file_list = new ArrayList<String>();
	static SessionManager mSess = null;
	private HashMap<String, SoftReference<Bitmap>> mCache = new HashMap<String, SoftReference<Bitmap>>();
	private int last_page = 0;
	private int client_cur_page = 0;
	private int server_cur_page = 0;
	private int hasNext = 0;
	private int return_id = -1;
	private String cur_task_id = null;
	public static String GIF_LIST_MESSAGE_ID = "wjfoiwmviwoiwjfowmvwo";
	private static GifDownActivity mActivity = null;
	public static ArrayList<GifDownResource> mDowningList = new ArrayList<GifDownActivity.GifDownResource>();

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			// clear cur task now
			// clearCurTask();
			Intent intent = new Intent();
			setResult(Activity.RESULT_OK, intent);
			finish();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void clearCurTask() {
		if (cur_task_id == null) {
			return;
		}
		mSess.mGifTransfer.clearTasks(cur_task_id);
	}

	public String getCurTaskTaskId() {
		return cur_task_id;
	}

	public static class GifDownResource {
		public static final int GIF_UN_DOWN = 0;// gif还未下载
		public static final int GIF_DOWN = 1;// gif已经下载过
		public static final int GIF_DOWNING = 2;// gif下载中

		// private String gif_img_url;
		private int emoid;
		private String emomd5;
		// private String gif_name;
		private String emoname;
		private int gif_down_state = GIF_UN_DOWN;
		// private String gif_bag_url;
		private String re_emomd5;
		private int progress = 0;

		public int getProgress() {
			return progress;
		}

		public void setProgress(int progress) {
			this.progress = progress;
		}

		public int getEmoid() {
			return emoid;
		}

		public void setEmoid(int emoid) {
			this.emoid = emoid;
		}

		public String getEmomd5() {
			return emomd5;
		}

		public void setEmomd5(String emomd5) {
			this.emomd5 = emomd5;
		}

		public String getEmoname() {
			return emoname;
		}

		public void setEmoname(String emoname) {
			this.emoname = emoname;
		}

		public String getRe_emomd5() {
			return re_emomd5;
		}

		public void setRe_emomd5(String re_emomd5) {
			this.re_emomd5 = re_emomd5;
		}

		public int getGif_down_state() {
			return gif_down_state;
		}

		public void setGif_down_state(int gif_down_state) {
			this.gif_down_state = gif_down_state;
		}

		@Override
		public String toString() {
			return "GifDownResource [emoid=" + emoid + ", emomd5=" + emomd5
					+ ", emoname=" + emoname + ", gif_down_state="
					+ gif_down_state + ", re_emomd5=" + re_emomd5
					+ ", progress=" + progress + "]";
		}

	}

	static class ViewHolder {
		public ImageView imageView;
		public TextView textView;
		public ImageView downView;
		public ImageView downedView;
		public ProgressBar pbDown;
		public GifDownResource gdr;
	}

	class DownAdapter extends BaseAdapter {

		ArrayList<GifDownResource> list = new ArrayList<GifDownActivity.GifDownResource>();
		OnClickListener listener;

		// TODO 数据来源后要作处理，如果本地已经下载过的，状态要为对号
		public DownAdapter(List<GifDownResource> data, OnClickListener listener) {
			list = (ArrayList<GifDownResource>) data;
			DownAdapter.this.listener = listener;
		}

		@Override
		public int getCount() {
			return list.size();
		}

		@Override
		public Object getItem(int position) {
			return list.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		ViewHolder holder = null;

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(thisContext).inflate(
						R.layout.item_lv_gif_down_bag, null);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.img_gif);
				holder.downedView = (ImageView) convertView
						.findViewById(R.id.img_downed);
				holder.downView = (ImageView) convertView
						.findViewById(R.id.img_down);
				holder.textView = (TextView) convertView
						.findViewById(R.id.tv_gif_name);
				holder.pbDown = (ProgressBar) convertView
						.findViewById(R.id.pb_gif_downloading);
				if (listener != null) {
					holder.downView.setOnClickListener(listener);
					// holder.pbDown.setOnClickListener(listener);
				}
				convertView.setTag(holder);
				holderList.add(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			GifDownResource gdr = list.get(position);
			// holder.pbDown.setTag(R.id.pb_gif_downloading, position);
			holder.downView.setTag(R.id.img_down, position);
			holder.gdr = gdr;
			holder.textView.setText(gdr.getEmoname());
			if (gdr.getGif_down_state() == GifDownResource.GIF_DOWN) {
				holder.downView.setVisibility(View.GONE);
				holder.pbDown.setVisibility(View.GONE);
				holder.downedView.setVisibility(View.VISIBLE);
			} else if (gdr.getGif_down_state() == GifDownResource.GIF_DOWNING) {
				holder.downedView.setVisibility(View.GONE);
				holder.downView.setVisibility(View.GONE);
				holder.pbDown.setVisibility(View.VISIBLE);
				holder.pbDown.setProgress(gdr.getProgress());
			} else {
				holder.downedView.setVisibility(View.GONE);
				holder.downView.setVisibility(View.VISIBLE);
				holder.pbDown.setVisibility(View.GONE);
			}
			if (gdr.re_emomd5 != null) {
				setImg(holder.imageView, gdr.re_emomd5);
			}
			return convertView;
		}

	}

	public void setImg(ImageView imageView, String md5) {
		if (mCache.get(md5) != null) {
			imageView.setImageBitmap(mCache.get(md5).get());
			return;
		}

	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// 表情包的名称和id
				dealDownResourceState();
				if (mList.size() < 10) {
					// lv_gif_bag_down.setPullLoadEnable(false);
				} else {
					// lv_gif_bag_down.setPullLoadEnable(true);
				}
				if (mActivity != null) {
					mActivity.da.notifyDataSetChanged();
				}
				getPicMd5List(client_cur_page);
				// lv_gif_bag_down.stopLoadMore();
				break;
			case 1:// 表情包的图片更新
				if (mActivity != null) {
					mActivity.da.notifyDataSetChanged();
				}
				break;
			case 2:// 下载表情包的图片md5值完毕
				String md5 = (String) msg.obj;
				int id = msg.arg1;
				getGifPic(id, md5);
				break;
			}
			super.handleMessage(msg);
		};
	};

	public void updateView(ViewHolder holder) {
		holder.gdr.setGif_down_state(GifDownResource.GIF_DOWNING);
		holder.gdr.setProgress(30);
		da.notifyDataSetChanged();
	}

	/**
	 * 处理下载资源的下载状态，这里要和本地资源作对比
	 */
	public void dealDownResourceState() {
		if (Utils.debug) {
			Log.i(TAG, "mDowingList" + mDowningList.toString());
		}
		if (mList != null && mList.size() > 0) {
			try {
				ArrayList<EmojiInfor> list = mSess.mUserEmojiInforsMgr
						.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());

				for (int i = 0; i < mList.size(); i++) {
					GifDownResource gif = mList.get(i);
					for (int j = 0; j < list.size(); j++) {
						if (list.get(j).emoji_md5.equals(gif.getEmomd5())) {
							gif.setGif_down_state(GifDownResource.GIF_DOWN);
						}
					}
					for (int j = 0; j < mDowningList.size(); j++) {
						if (mDowningList.get(j).getEmomd5()
								.equals(gif.getEmomd5())) {
							gif.setGif_down_state(mDowningList.get(j)
									.getGif_down_state());
							gif.setProgress(mDowningList.get(j).getProgress());
						}
					}
				}
				// for (int i = 0; i < list.size(); i++) {
				// EmojiInfor infor = list.get(i);
				// for (int j = 0; j < mList.size(); j++) {
				// if (infor.emoji_md5.equals(mList.get(j).getEmomd5())) {
				// mList.get(j).setGif_down_state(
				// GifDownResource.GIF_DOWN);
				// }
				// }
				// }
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}

	}

	// 通过test方法构造假数据看界面
	public void test() {
		// for (int i = 0; i < 10; i++) {
		// GifDownResource bdr = new GifDownResource();
		// bdr.setEmoname("下载包" + i);
		// mList.add(bdr);
		// }
		getGifList(0);

		// GifDownResource bdr = new GifDownResource();
		// bdr.setEmoname("test_package");
		// bdr.setEmoid(32);
		// mList.add(bdr);
	}

	// 1.上传请求，gif列表得到名字和id
	public void getGifList(final int page) {
		cur_task_id = GIF_LIST_MESSAGE_ID;
		mSess.mGifTransfer.downGifList(page, new GifDownUploadListner() {

			@Override
			public void onFinish(GifTaskInfo taskInfo) {
				cur_task_id = null;
				last_page++;
				GifTransfer.GifListDownloadTask task = (GifListDownloadTask) taskInfo;
				String json = task.json;
				hasNext = task.hasNextPage;
				server_cur_page = task.page;
				// 解析json中的数据，更新列表，表情包的名称等
				try {
					dealGifListJson(json);
					mHandler.obtainMessage(0).sendToTarget();
					// 请求得到每个代表图片的md5值
				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		}, 1);

	}

	// 2.上传id,请求图片的md5值
	public void getPicMd5List(int page) {
		for (int i = 0; i < mList.size(); i++) {
			getGifPicList(mList.get(i).getEmoid(), page);
		}
	}

	// 上传请求，获取表情图片列表的md5值
	public void getGifPicList(int id, int page) {
		mSess.mGifTransfer.downGifPicList(id, page, new GifDownUploadListner() {

			@Override
			public void onFinish(GifTaskInfo taskInfo) {
				GifPicListDownloadTask task = (GifPicListDownloadTask) taskInfo;
				String json = task.json;
				// 得到表情图片md5的list后，去请求图片下载
				try {
					dealGifPicMd5ListJson(task.mId, json);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}, 1);
	}

	public void getGifPic(int id, final String md5) {
		for (int i = 0; i < mList.size(); i++) {
			if (mList.get(i).getEmoid() == id) {
				mList.get(i).setRe_emomd5(md5);
				// 开启下载线程去下载图片
//				mSess.mGifTransfer.downGifPic(id, md5,
//						new GifDownUploadListner() {
//
//							@Override
//							public void onFinish(GifTaskInfo taskInfo) {
//								GifTransfer.GifDownloadTask info = (GifDownloadTask) taskInfo;
//								if (mCache.get(md5) == null) {
//									Bitmap bitmap = BitmapFactory
//											.decodeFile(info.mFullName);
//									mCache.put(md5, new SoftReference<Bitmap>(
//											bitmap));
//									mHandler.obtainMessage(1).sendToTarget();
//								}
//
//							}
//						}, 1);
			}
		}
	}

	public void dealGifPicMd5ListJson(int id, String json) throws Exception {
		if (json.startsWith("{")) {
			JSONObject jo = null;
			jo = new JSONObject(json);
			if (jo != null) {
				JSONArray array = null;
				array = jo.optJSONArray("emolist");
				if (array != null) {
					String md5 = (String) array.get(0);
					// mHandler.obtainMessage(2, md5).sendToTarget();
					mHandler.obtainMessage(2, id, 0, md5).sendToTarget();
				}
			}
		}
	}

	public void dealGifListJson(String json) throws Exception {
		if (json.startsWith("{")) {
			JSONObject jo = null;
			try {
				jo = new JSONObject(json);
			} catch (Exception e) {

			}
			if (jo != null) {
				JSONArray array = null;
				array = jo.optJSONArray("pkgslist");
				if (array != null) {
					JSONObject jobj = null;
					GifDownResource r = null;
					for (int i = 0; i < array.length(); i++) {
						jobj = array.optJSONObject(i);
						if (jobj != null) {
							r = new GifDownResource();
							r.setEmoid(jobj.getInt("emoid"));
							r.setEmomd5(jobj.getString("emomd5"));
							r.setEmoname(jobj.getString("emoname"));
							mList.add(r);
						}
					}
				}
			}

		}
	}

	DownAdapter da;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gif_bag_down);
		mActivity = this;
		mSess = SessionManager.getInstance();
//		ll_from_sd = (LinearLayout) findViewById(R.id.ll_from_sd);
		threadPools = Executors.newFixedThreadPool(5);
		lv_gif_bag_down = (ListView) findViewById(R.id.lv_gif_bag_down);
		// lv_gif_bag_down.setOnItemClickListener(this);
		tv_set = (TextView) findViewById(R.id.tv_set);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		// lv_gif_bag_down.setPullLoadEnable(false);
		// lv_gif_bag_down.setPullRefreshEnable(false);
		// lv_gif_bag_down.setXListViewListener(this);
		tv_set.setOnClickListener(this);
		btn_back_left.setOnClickListener(this);
		ll_from_sd.setOnClickListener(this);
		test();
		da = new DownAdapter(mList, this);
		lv_gif_bag_down.setAdapter(da);
		da.notifyDataSetChanged();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		// 从SD卡添加
//		case R.id.ll_from_sd:
//			Intent intent = new Intent(this, FolderExplorerGifActivity.class);
//			String sdcardPath = Environment.getExternalStorageDirectory()
//					.getAbsolutePath();
//			intent.putExtra(FolderExplorerActivity.FOLDER_ROOT_PATH, sdcardPath);
//			intent.putExtra(FolderExplorerActivity.TITLE_NAME, "SD卡文件");
//			intent.putExtra(FolderExplorerActivity.IS_SEND_FILES, true);
//			startActivityForResult(intent, BaseMsgRoom.REQ_TAKE_GIF_FILE);
//			mSess.mGifTransfer.downGifPic(34,
//					"23B28D1E31FEE0CB6D22968E859737A0",
//					new GifDownUploadListner() {
//
//						@Override
//						public void onFinish(GifTaskInfo taskInfo) {
//							if (Utils.debug) {
//								Log.i(TAG, "===下载表情成功");
//							}
//
//						}
//					}, 1);
//			break;
		case R.id.tv_set:
			 Intent intent_set = new Intent(this, GifOrderActivity.class);
			 startActivity(intent_set);
			// 点击下载单个表情
			
			break;
		case R.id.img_down:
			int position = (Integer) v.getTag(R.id.img_down);
			final GifDownResource g = da.list.get(position);
			mDowningList.add(g);
			g.setGif_down_state(GifDownResource.GIF_DOWNING);
			g.setProgress(0);
			da.notifyDataSetChanged();
			if (g != null) {
				cur_task_id = g.getEmomd5();
				mSess.mGifTransfer.downGifPackage(g.getEmomd5(), g.getEmoname()
						+ ".eif", g.getEmoid(), new GifDownUploadListner() {
					@Override
					public void onStart(GifTaskInfo taskInfo) {
						// g.setProgress(0);
						// mHandler.post(new Runnable() {
						//
						// @Override
						// public void run() {
						// g.setGif_down_state(GifDownResource.GIF_DOWNING);
						// g.setProgress(0);
						// da.notifyDataSetChanged();
						// }
						// });
					}

					@Override
					public void onProgress(GifTaskInfo taskInfo, int curlen,
							int totallen) {
						g.setProgress((int) ((float) curlen / totallen * 100));
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								g.setGif_down_state(GifDownResource.GIF_DOWNING);
								if (Utils.debug) {
									Log.i(TAG,
											"===progress : " + g.getProgress());
								}
								g.setProgress(g.getProgress());
								if (mActivity != null) {
									mActivity.flushList();
								}
							}
						});
					}

					@Override
					public void onFinish(final GifTaskInfo taskInfo) {
						cur_task_id = null;
						g.setGif_down_state(GifDownResource.GIF_DOWN);
						mDowningList.remove(g);
						mHandler.post(new Runnable() {

							@Override
							public void run() {
								da.notifyDataSetChanged();
								// GifTranscoldMgr
								// .getInstance()
								// .saveGifThumbnailToSDCard(
								// taskInfo.mFullName,
								// FileManager
								// .getShenLiaoGifPath(),
								// FileManager
								// .getFileMD5(taskInfo.mFullName),
								// taskInfo.mId, true);
							}
						});
						gif_file_list.add(taskInfo.mFullName);

					}
				}, 1);
			}
			break;
		}

	}

	// @Override
	// public void onRefresh() {
	// Toast.makeText(this, "下拉刷新", Toast.LENGTH_SHORT).show();
	// // lv_gif_bag_down.stopRefresh();
	//
	// }
	//
	// @Override
	// public void onLoadMore() {
	// if (hasNext == 0) {
	// lv_gif_bag_down.stopLoadMore();
	// } else {
	// getGifList(client_cur_page);
	// }
	// }

	public class Run implements Runnable {
		GifDownResource grr = new GifDownResource();

		public Run(GifDownResource gdr) {
			grr = gdr;
		}

		public void run() {
			while (grr.getProgress() <= 100) {
				Log.d(TAG, "=====thread:" + grr.getEmoname());
				grr.setProgress(grr.getProgress() + 10);
				mHandler.post(new Runnable() {

					@Override
					public void run() {
						grr.setGif_down_state(GifDownResource.GIF_DOWNING);
						grr.setProgress(grr.getProgress());
						da.notifyDataSetChanged();
					}
				});
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			}
			grr.setGif_down_state(GifDownResource.GIF_DOWN);
			mHandler.post(new Runnable() {

				@Override
				public void run() {
					da.notifyDataSetChanged();
				}
			});
		};

	}

	GifDownResource gdr = null;

	public void flushList() {
		da.notifyDataSetChanged();
	}

}
