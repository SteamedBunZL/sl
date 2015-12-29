package com.tuixin11sms.tx.gif;

import java.io.File;
import java.lang.ref.SoftReference;
import java.security.interfaces.RSAKey;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.json.JSONException;

import u.aly.ba;
import u.aly.da;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.location.ad;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.activity.BaseMsgRoom;
import com.tuixin11sms.tx.activity.SearchFriendActivity;
import com.tuixin11sms.tx.activity.explorer.FileManager;
import com.tuixin11sms.tx.utils.Utils;

public class GifOrderActivity extends BaseActivity implements OnClickListener {
	private DragListAdapter adapter = null;
	private TextView tv_order_delete;
	private LinearLayout btn_back_left;
	public static final String TAG = "GifOrderActivity";
	public boolean isDelete = true;
	private DragListView dragListView;
	private SessionManager mSess = SessionManager.getInstance();
	private ArrayList<EmojiInfor> data;
	private ArrayList<String> data2;
	private HashMap<String, SoftReference<Bitmap>> bitmapCache = new HashMap<String, SoftReference<Bitmap>>();
	private int AVATAR_TAG = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gif_bag_set);
		tv_order_delete = (TextView) findViewById(R.id.tv_order_delete);
		btn_back_left = (LinearLayout) findViewById(R.id.btn_back_left);
		btn_back_left.setOnClickListener(this);
		tv_order_delete.setOnClickListener(this);
		initData();
		dragListView = (DragListView) findViewById(R.id.lv_gif_bag_drag);
		dragListView.setLock(true);
		adapter = new DragListAdapter(this, data, this);
		dragListView.setAdapter(adapter);
		adapter.notifyDataSetChanged();
	}

	/**
	 * 从本地读取当前用户的表情包
	 */
	public void initData() {

		try {
			data = mSess.mUserEmojiInforsMgr
					.getCurUserEmoji(mSess.mPrefMeme.user_id.getVal());
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			back();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void back() {
		Intent intent = new Intent();
		// intent.putExtra(name, value);
		setResult(Activity.RESULT_OK, intent);
		try {
			mSess.mUserEmojiInforsMgr.saveOrUpdateCurUserEmoji(
					mSess.mPrefMeme.user_id.getVal(), data);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		finish();
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btn_back_left:
			// Log.d(TAG, "====list" + adapter.getList().toString());
			back();
			break;
		case R.id.tv_order_delete:
			if (isDelete) {
				// 变为排序状态
				tv_order_delete.setText("删除");
				adapter.setDelete(false);
				dragListView.setLock(false);
				adapter.notifyDataSetChanged();
				isDelete = false;
			} else {
				// 变为删除状态
				tv_order_delete.setText("排序");
				adapter.setDelete(true);
				dragListView.setLock(true);
				adapter.notifyDataSetChanged();
				isDelete = true;
			}
			break;
		case R.id.delete_list_item_image:
			int position = (Integer) v.getTag(R.id.delete_list_item_image);
			showDeleteDialog(position);
			break;
		}
	}

	private void showDeleteDialog(final int position) {
		AlertDialog.Builder deleteDialog = new AlertDialog.Builder(this);
		deleteDialog.setTitle("提示");
		deleteDialog.setMessage("确认删除此项么？");
		deleteDialog.setPositiveButton("确定",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						data.remove(position);
						adapter.notifyDataSetChanged();
					}
				});
		deleteDialog.setNegativeButton("取消",
				new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.dismiss();
					}
				});
		deleteDialog.show();

	}

	public class DragListAdapter extends BaseAdapter {
		Context context;
		ArrayList<EmojiInfor> list = new ArrayList<EmojiInfor>();
		BitmapLoader loader;
		private boolean isDelete = true;
		private OnClickListener listener;

		public void setDelete(boolean delete) {
			isDelete = delete;
		}

		public DragListAdapter(Context context, List<EmojiInfor> list,
				OnClickListener listener) {
			this.context = context;
			this.list = (ArrayList<EmojiInfor>) list;
			loader = new BitmapLoader();
			this.listener = listener;
		}

		ViewHolder holder = null;

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			if (convertView == null) {
				holder = new ViewHolder();
				convertView = LayoutInflater.from(context).inflate(
						R.layout.drag_list_item_2, null);
				holder.textView = (TextView) convertView
						.findViewById(R.id.drag_list_item_text);
				holder.imageView = (ImageView) convertView
						.findViewById(R.id.drag_list_item_image);
				holder.deleteView = (TextView) convertView
						.findViewById(R.id.delete_list_item_image);
				holder.avatar = (ImageView) convertView
						.findViewById(R.id.image_avatar);
				if (listener != null && holder.deleteView != null) {
					holder.deleteView.setOnClickListener(listener);
					holder.deleteView.setTag(R.id.delete_list_item_image,
							position);
				}
				holder.avatar.setTag(AVATAR_TAG, list.get(position).emoji_md5);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}
			if (isDelete) {
				holder.deleteView.setVisibility(View.VISIBLE);
				holder.imageView.setVisibility(View.INVISIBLE);
			} else {
				holder.deleteView.setVisibility(View.INVISIBLE);
				holder.imageView.setVisibility(View.VISIBLE);
			}

			holder.textView.setText(list.get(position).emoji_name);
			// 1.先从缓存中读取
			String key = list.get(position).emoji_md5;
			SoftReference<Bitmap> soft = null;
			Bitmap bitmap = null;
			soft = bitmapCache.get(key);
			if (soft != null) {
				bitmap = soft.get();
			}
			if (bitmap != null) {
				holder.avatar.setImageBitmap(bitmap);
			} else {
				loader.loadBitmap(
						holder.avatar,
						SessionManager.getInstance().mUserEmojiInforsMgr
								.getShenLiaoLocalGifPath(list.get(position).emoji_path),
						list.get(position).emoji_md5,
						list.get(position).emoji_id);
			}

			return convertView;
		}

		class ViewHolder {
			TextView textView;
			ImageView imageView;
			TextView deleteView;
			ImageView avatar;

		}

		class BitmapLoader {
			private Handler handler = new Handler();
			private ThreadPoolExecutor poolConnect;
			private LinkedBlockingQueue<Runnable> queneConnect;
			private ThreadPoolExecutor executor;
			private GifTranscoldMgr gifTranscoldMgr;

			public BitmapLoader() {
				queneConnect = new LinkedBlockingQueue<Runnable>();
				poolConnect = new ThreadPoolExecutor(20, 50, 180,
						TimeUnit.SECONDS, queneConnect);
				executor = new ThreadPoolExecutor(1, 50, 180, TimeUnit.SECONDS,
						queneConnect);
				gifTranscoldMgr = GifTranscoldMgr.getInstance();
			}

			public void loadBitmap(ImageView imageView, String path,
					String md5, int id) {
				if (Utils.debug) {
					Log.d(TAG, "id :" + id);
				}
				executor.execute(new DoAnalysis(imageView, path, md5, id));
			}

			class DoAnalysis implements Runnable {
				String path;
				ImageView imageView;
				String md5;
				int id;

				DoAnalysis(ImageView iv, String path, String md5, int id) {
					this.imageView = iv;
					this.path = path;
					this.md5 = md5;
					this.id = id;
				}

				@Override
				public void run() {
					Bitmap bitmapDrawable = null;
					Bitmap bitmap = null;
					if (Utils.debug) {
						Log.d(TAG, "id run :" + id);
					}

					String path = FileManager.getShenLiaoGifPackagePath() + id
							+ ".jpg";
					File file = new File(path);
					if (file.exists()) {
						bitmapDrawable = BitmapFactory.decodeFile(path);
					}
					if (bitmapDrawable != null) {
						bitmap = Utils.getRoundedCornerBitmap(bitmapDrawable);
					} else {
						bitmapDrawable = gifTranscoldMgr
								.getSingleEmojiRepresend(this.path);
						bitmap = Utils.getRoundedCornerBitmap(bitmapDrawable);
					}

					if (bitmap != null) {
						bitmapCache.put(md5, new SoftReference<Bitmap>(bitmap));
						if (imageView.getTag(AVATAR_TAG).equals(md5)) {
							handler.post(new OnAnalysis(bitmap, this.imageView));
						}
					}
				}
			}

			class OnAnalysis implements Runnable {
				Bitmap bd;
				ImageView iv;

				OnAnalysis(Bitmap bd, ImageView iv) {
					this.bd = bd;
					this.iv = iv;
				}

				@Override
				public void run() {
					if (bd != null) {
						iv.setImageBitmap(bd);
						iv.invalidate();
					}

				}

			}

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

		public void remove(EmojiInfor dragItem) {
			list.remove(dragItem);
			notifyDataSetChanged();
		}

		public void insert(EmojiInfor dragItem, int dragPosition) {
			list.add(dragPosition, dragItem);
			notifyDataSetChanged();

		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		// 手动回收图片
		if (bitmapCache != null) {
			Iterator i = bitmapCache.entrySet().iterator();
			while (i.hasNext()) {
				Map.Entry entry = (Entry) i.next();
				SoftReference<Bitmap> soft = (SoftReference<Bitmap>) entry
						.getValue();
				if (soft != null) {
					Bitmap bitmap = soft.get();
					if (bitmap != null) {
						bitmap.recycle();
					}
					soft = null;
				}
			}
			bitmapCache.clear();
		}
	}
}
