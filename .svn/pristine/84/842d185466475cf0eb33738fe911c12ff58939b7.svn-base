package com.shenliao.user.adapter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.tuixin11sms.tx.SessionManager;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.model.AlbumItem;
import com.tuixin11sms.tx.task.CallInfo;
import com.tuixin11sms.tx.task.FileTransfer;
import com.tuixin11sms.tx.task.FileTransfer.FileTaskInfo;
import com.tuixin11sms.tx.utils.AsyncCallback;
import com.tuixin11sms.tx.utils.Utils;

public class AlbumGridViewAdapter extends BaseAdapter {
	private static final String TAG = "AlbumGridViewAdapter";
	private Context mContext;
	private List<AlbumItem> list = new ArrayList<AlbumItem>();
	private TextView albumText;

	public AlbumGridViewAdapter(Context c,TextView albumText) {
		prepairAsyncload();
		mContext = c;
		this.albumText=albumText;
	}

	@Override
	public int getCount() {
		if (list != null) {
			return list.size();
		}
		return 0;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder vh;
		if (convertView == null) {
			vh = new ViewHolder();
			convertView = LayoutInflater.from(mContext).inflate(
					R.layout.sll_album_grid_item, null);
			vh.aiImageView = (ImageView) convertView
					.findViewById(R.id.ai_item_iv);
			convertView.setTag(vh);
		} else {
			vh = (ViewHolder) convertView.getTag();
		}
		if (list.size() > 1) {
			if (albumText != null) {
				albumText.setText("(长按以更换照片)");
			}
		} else {
			if (albumText != null) {
				albumText.setText("(上传自己照片，认识更多好友)");
			}
		}
		final AlbumItem ai = list.get(position);
		if (ai.isAdd()) {
			vh.aiImageView.setImageResource(R.drawable.phono_59);
		} else {
			Bitmap bm = ai.getBitmap();
			final ImageView iv = vh.aiImageView;
			long tag = 999999 + position;
			iv.setTag(tag);
			if (bm == null) {
				if(Utils.debug){
					Log.i(TAG, "加载载第"+position+"个相册缩略图");
				}
				if(!ai.getLoaded()){
					ai.setLoaded();
					loadAlbumImg(tag, ai.getUrl(), new AsyncCallback<Bitmap>() {
						@Override
						public void onSuccess(Bitmap result, long id) {
							if (Long.parseLong(iv.getTag().toString()) == id) {
								//iv.setScaleType(ScaleType.CENTER_CROP);
								iv.setImageBitmap(result);
								ai.setBitmap(result);
								notifyDataSetChanged();
							}
						}
	
						@Override
						public void onFailure(Throwable t, long id) {
	
						}
					});
				}
			}else
				 iv.setImageBitmap(bm);
		}
		return convertView;
	}

	public void setList(List<AlbumItem> albums) {
		if (albums != null) {
			this.list = albums;
		}
	}

	private static final class ViewHolder {
		ImageView aiImageView;
	}
	
	
	//这个加载不能用BaseActivity中的，因为这个相册的图片不能被圆角化
	Handler mAvatarHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 1: {
				CallInfo callinfo = (CallInfo) msg.obj;
				callinfo.mCallback.onSuccess(callinfo.mBitmap, callinfo.mUid);
				break;
			}
			}
		};
	};
	Handler mAsynloader;
	SessionManager mSess = SessionManager
			.getInstance();

	void prepairAsyncload() {
		mAsynloader = new Handler(SessionManager.mgAsynloaderThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				CallInfo ci;
				switch (msg.what) {
				case 1:
					ci = (CallInfo) msg.obj;
					if(ci.mUrl==null)
						break;
					String file = mSess.mDownUpMgr.getAlbumFile(ci.mUrl,false);
					if (file == null)
						break;
					File avatar = new File(file);
					if (avatar.exists()) {
						Bitmap bitmap = Utils.readBitMap(file, false);
						if (bitmap != null) {
							ci.mBitmap = bitmap;
							mAvatarHandler.obtainMessage(1, ci).sendToTarget();
							break;
						}
					}
					mSess.mDownUpMgr.downloadImg(ci.mUrl, file, 1, false,
							true, new FileTransfer.DownUploadListner(){
						// 此处要记录文件名，同时要加载
						@Override
						public void onFinish(FileTaskInfo taskInfo) {
							Bitmap bitmap = Utils.readBitMap(taskInfo.mFullName, false);
							if (bitmap != null) {
								CallInfo ci = (CallInfo) taskInfo.mObj;
								ci.mBitmap = bitmap;
								mAvatarHandler.obtainMessage(1,ci).sendToTarget();
							}
						}

					}, ci);
					break;
				}					
			}
		};
	}

	public void stopAsyncload() {
		mAsynloader = null;
	}

	protected void loadAlbumImg(long tag, String avatar_url,
			AsyncCallback<Bitmap> callback) {
		CallInfo callinfo = new CallInfo(avatar_url, tag, callback);
		mAsynloader.obtainMessage(1, callinfo).sendToTarget();
	}
	
}
