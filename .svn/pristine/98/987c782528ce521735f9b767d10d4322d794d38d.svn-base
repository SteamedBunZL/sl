package com.shenliao.group.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.shenliao.group.adapter.GroupIndexAdapter;
import com.shenliao.group.util.ADUtils;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.core.MsgHelper;
import com.tuixin11sms.tx.core.MsgInfor;
import com.tuixin11sms.tx.group.TxGroup;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.CommonData;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.tuixin11sms.tx.view.DragListView;

/** 公告聊天室页面 */
public class GroupPublic extends BaseActivity {
	private String TAG = "GroupPublic";
	private DragListView mListView;
	private GroupIndexAdapter adapter;
	private List<TxGroup> groups = new ArrayList<TxGroup>();
	private List<String> list = new ArrayList<String>();
	private List<String> ids = new ArrayList<String>();
	private View mLoading;
	private boolean isGetOver;
	private int currentPage = 0;
	private UpdateReceiver updatareceiver;
	public static boolean CHNAGE = false;
	private View noData;
	private SharedPreferences prefs;
	private Editor editor;
//	protected TxData txData;
	private RelativeLayout rl_adParent;// 测试广告的父view

	// private ImageView iv_ttest;//测试的广告条

	/**
	 * 
	 * @author xuchunhui 公共聊天室
	 * 
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_index_listview_public);
		init();
		registReceiver();

		/** 加载广告图片 */
		String adInfors = getPrefsSet().getString(ADUtils.AD_INFOR, "");
		if (TextUtils.isEmpty(adInfors)) {
			// 没有广告信息，去服务器获取
			requestADInfors();
		} else {
			// 有广告信息，比对广告是否过期
			try {
				JSONObject adInforObj = new JSONObject(adInfors);
				long deadTime = adInforObj.getLong(ADUtils.SAVE_TIME_MILLIS)
						+ adInforObj.getInt(ADUtils.AD_TIMEOUT) * 60 * 1000;
				if (System.currentTimeMillis() > deadTime) {
					// 广告已经过期了，重新获取
					requestADInfors();
				} else {
					// 广告没有过期，加载
					String adIdStr = "" + adInforObj.getInt(ADUtils.AD_ID);
					String folderPath = Utils.getStoragePath(mSess.getContext())
							+ ADUtils.AD_FOLDER_NAME;
					File adFile = new File(folderPath, adIdStr);
					if (adFile != null && adFile.exists()) {
						Bitmap adBm = null;
						try {
							adBm = Utils.fitSizeImg(adFile.getAbsolutePath());
						} catch (OutOfMemoryError err) {
							if (Utils.debug) {
								Log.e(TAG, "请求广告图片OOM异常", err);
							}
							adBm = Utils
									.fitSizeImg(adFile.getAbsolutePath(), 3);
						}
						if (adBm == null) {
							// 加载图片出现异常，重新取服务获取广告信息
							requestADInfors();
						} else {
							if (Utils.debug)
								Log.i(TAG, "本地的广告信息为：" + adInforObj.toString());
							final String forward_url = adInforObj
									.getString("forward_url");
							final int forward_type = adInforObj
									.getInt("forward_type");
							laodAdPic(adIdStr, forward_type, forward_url, adBm);// 加载广告图片
						}
					} else {
						// 本地广告图片文件不存在，则从服务器获取
						requestADInfors();
					}
				}
			} catch (JSONException e) {
				if (Utils.debug)
					Log.e(TAG, "解析异常", e);
			}
		}
	}

	/** 去服务器请求广告信息 */
	private void requestADInfors() {
		ADUtils adTask = new ADUtils(mSess.getContext(), ADUtils.DOWNLOAD_AD_INFOR) {

			@Override
			protected void onPostExecute(Object result) {
				try {
					if (result != null) {
						JSONObject resultObj = (JSONObject) result;
						if (Utils.debug)
							Log.i(TAG, "获取的广告信息为：" + resultObj.toString());
						resultObj.put(ADUtils.SAVE_TIME_MILLIS,
								System.currentTimeMillis());// 添加当前时间毫秒值
						getEditorSet().putString(ADUtils.AD_INFOR,
								resultObj.toString()).commit();
						final int ad_id = resultObj.getInt(ADUtils.AD_ID);
						String img_url = resultObj
								.getString(ADUtils.AD_IMG_URL);
						final String forward_url = resultObj
								.getString(ADUtils.AD_FORWARD_URL);
						final int forward_type = resultObj
								.getInt(ADUtils.AD_FORWARD_TYPE);
						// int timeout = resultObj.getInt("timeout");

						new ADUtils(mSess.getContext(), ADUtils.DOWNLOAD_AD_IMG) {
							@Override
							protected void onPostExecute(Object result) {
								// 下载完广告图片成功后
								if (Utils.debug)
									Log.i(TAG, "广告图片下载完毕");
								if (result != null) {
									Bitmap bm = (Bitmap) result;
									laodAdPic("" + ad_id, forward_type,
											forward_url, bm);// 加载广告图片
								}

							};
						}.execute(img_url, "" + ad_id);
					} else {
						if (Utils.debug)
							Log.i(TAG, "获取的广告信息为空");
					}
				} catch (JSONException e) {
					if (Utils.debug)
						Log.e(TAG, "广告信息下载成功后解析json异常", e);
				}
			}
		};
		adTask.execute("" + TX.tm.getUserID());// 获取广告信息
	}

	/** 加载广告图片 */
	private void laodAdPic(final String adId, final int forward_type,
			final String forward_url, Bitmap bm) {
		int height = bm.getHeight() * Utils.SreenW / bm.getWidth();//
		// 按比例显示广告图片的高度
		// if (Utils.debug)
		// Log.i(TAG, "广告图高为：" + height);
		RelativeLayout.LayoutParams linparams = new RelativeLayout.LayoutParams(
				RelativeLayout.LayoutParams.MATCH_PARENT, height);
		// ImageView iv_ttest = new ImageView(thisContext);
		// rl_adParent.addView(iv_ttest);
		// iv_ttest.setLayoutParams(linparams);
		// iv_ttest.setBackgroundDrawable(new BitmapDrawable(bm));

		mListView.setWandH(Utils.SreenW, height);
		ImageView ad_iv = (ImageView) mHeadView.findViewById(R.id.ad_iv);
		ImageView mbt = (ImageView) mHeadView.findViewById(R.id.ad_close);
		ad_iv.setLayoutParams(linparams);
		mListView.addHeaderChildView(ad_iv, mbt);
		mListView.setADBitmap(new BitmapDrawable(bm));
		mListView.setADOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// if(Utils.debug)
				// showToast("点击了图片，跳转到浏览器");
				if (forward_type == ADUtils.FORWARD_TO_BROWSER) {
					try {
						Utils.openUrlWithBrowser(thisContext, forward_url);
					} catch (Exception e) {
						if (Utils.debug)
							Log.e(TAG, "打开广告网页异常", e);
						showToast("请确认您的手机系统安装有网页浏览器");
					}
				} else {
					if (Utils.debug)
						Log.e(TAG, "点击广告图片，需要跳转到程序内部页面");
				}
			}
		});

		// RelativeLayout.LayoutParams closeAdParams = new
		// RelativeLayout.LayoutParams(height / 3, height / 3);
		// closeAdParams.addRule(RelativeLayout.CENTER_VERTICAL);
		// closeAdParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		// closeAdParams.setMargins(0, 0, Utils.dip2px(5, thisContext), 0);
		//
		// ImageView iv_closeAd = new ImageView(thisContext);
		// rl_adParent.addView(iv_closeAd);
		// iv_closeAd.setLayoutParams(closeAdParams);
		// iv_closeAd.setImageResource(R.drawable.close_ad_pic);
		// // iv_closeAd.setBackgroundColor(Color.BLUE);
		// iv_closeAd.setOnClickListener(new View.OnClickListener() {
		//
		// @Override
		// public void onClick(View v) {
		// if (Utils.debug)
		// showToast("点击了关闭图片按钮");
		// //隐藏listView的header
		// mListView.setSelection(1);
		//
		// //请求不显示此广告
		// new ADUtils(txdata, ADUtils.REQUEST_AD_NO_DISPLAY) {
		//
		// @Override
		// protected void onPostExecute(Object result) {
		// //请求完毕
		// if (result!=null) {
		// //有请求结果返回
		// String resultStr = (String)result;
		// if (resultStr.equals("ok")) {
		// //取消广告显示成功
		// if(Utils.debug)
		// showToast("取消显示广告成功");
		// }else {
		// //取消广告显示失败
		// if(Utils.debug)
		// showToast("取消广告显示失败了");
		// }
		// }
		// };
		//
		// }.execute("" + TX.tm.getUserID(), adId);
		//
		// }
		// });
		//
		mListView.invalidate();

	}

	private void init() {
		prefs = getPrefsMeme();// getSharedPreferences(CommonData.MEME_PREFS,
								// Context.MODE_WORLD_READABLE +
								// Context.MODE_WORLD_WRITEABLE);
		editor = getEditorMeme();// prefs.edit();
//		txData = (TxData) getApplication();
		// mListView = (ListView) findViewById(R.id.group_index_listView);
		mListView = (DragListView) findViewById(R.id.group_index_listView);

		noData = findViewById(R.id.channelRoom_uninit);
		mLoading = findViewById(R.id.group_loading);
		adapter = new GroupIndexAdapter(GroupPublic.this, mListView, noData,
				GroupPublic.this);

		list = getGroupIds();
		if (list.size() > 0)
			groups = getTxGroupListByIds(list);
		else
			groups = TxGroup.getPublicGroups(GroupPublic.this, 0);

		adapter.setData(groups);
		mHeadView = LayoutInflater.from(GroupPublic.this).inflate(
				R.layout.ad_header, null);
		mListView.addHeaderView(mHeadView);
		mListView.setCacheColorHint(0);
		mListView.setAdapter(adapter);
		mListView.setOnScrollListener(new ScrollList());
		currentPage = 0;
		mSess.getSocketHelper().sendPublicGroup(0);

	}

	int lastPos;
	Timer t = null;

	private class ScrollList implements OnScrollListener {

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			lastPos = mListView.getLastVisiblePosition();
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			if (!isGetOver && scrollState == OnScrollListener.SCROLL_STATE_IDLE
					&& lastPos == groups.size() - 1) {
				if (mLoading.getVisibility() == View.VISIBLE)
					return;
				if (getPubMoreGroupList()) {
					mLoading.setVisibility(View.GONE);
				} else {
					timeout();
					mLoading.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	private void timeout() {
		if (t != null) {
			t.cancel();
		}
		t = new Timer();
		t.schedule(new TimerTask() {

			@Override
			public void run() {
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						mLoading.setVisibility(View.GONE);
					}
				});
			}
		}, 10 * 1000);
	}

	@Override
	protected void onResume() {
		groups = TxGroup.getPublicGroups(GroupPublic.this, 0);
		adapter.setData(groups);
		mListView.setAdapter(adapter);
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
		unregisterReceiver(updatareceiver);
		// if(mAsyncTask!=null){
		// mAsyncTask.cancel(true);
		// }
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void registReceiver() {
		if (updatareceiver == null) {
			updatareceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			filter.addAction(Constants.INTENT_ACTION_SEARCH_USER);
			filter.addAction(Constants.INTENT_ACTION_PUBLIC_GROUP_2032);
			filter.addAction(Constants.INTENT_ACTION_JOIN_GROUP_2018);
			filter.addAction(Constants.INTENT_ACTION_FLUSH);
			filter.addAction(Constants.INTENT_ACTION_FLUSH_GROUP);
			this.registerReceiver(updatareceiver, filter);
		}
	}

	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_PUBLIC_GROUP_2032.equals(intent
					.getAction())) {
				dealPublicGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_JOIN_GROUP_2018.equals(intent
					.getAction())) {
				dealJoinGroup(serverRsp);
			} else if (Constants.INTENT_ACTION_FLUSH.equals(intent.getAction())) {
				msgStatflush();
			} else if (Constants.INTENT_ACTION_FLUSH_GROUP.equals(intent
					.getAction())) {
				msgGroupflush();
			}
		}

	}

	private boolean getPubMoreGroupList() {
		if (currentPage < totalPage) {
			if (ids.size() <= 0) {
				return false;
			}
			if (currentPage == totalPage - 1) {

				mSess.getSocketHelper().sendGetMoreGroup(
						ids.subList(currentPage * 10, totalSize),
						MsgInfor.SERVER_GET_PUBLIC_GROUP);

			} else {
				mSess.getSocketHelper().sendGetMoreGroup(
						ids.subList(currentPage * 10, (currentPage + 1) * 10),
						MsgInfor.SERVER_GET_PUBLIC_GROUP);
			}
			return false;
		} else if ((cp + 1) >= ep) {
			isGetOver = true;
			return true;
		} else {
			mSess.getSocketHelper().sendPublicGroup(cp + 1);
			return false;
		}
	}

	int cp;
	int ep;
	int totalPage = 1000000000;
	int totalSize = 0;
	int pageSize = 10;
	private View mHeadView;

	public void dealPublicGroup(ServerRsp serverRsp) {
		switch (serverRsp.getStatusCode()) {
		case RSP_OK:
			if (t != null) {
				t.cancel();
			}
			Bundle bundle = serverRsp.getBundle();
			if (currentPage == 0 && cp == 0) {
				groups.clear();
			}
			currentPage++;
			List<TxGroup> tempList = bundle
					.getParcelableArrayList(MsgHelper.GROUP_LIST);
			groups.addAll(tempList);
			groups = TxGroup.listUniq(groups);
			adapter.setData(groups);
			adapter.notifyDataSetChanged();
			mLoading.setVisibility(View.GONE);
			break;
		case OPT_FAILED:
			break;
		case GET_OVER:
			isGetOver = true;
			mLoading.setVisibility(View.GONE);
			break;
		case GROUP_FOR_PAGE:

			cp = serverRsp.getBundle().getInt("cp");
			ep = serverRsp.getBundle().getInt("ep");
			ArrayList<String> tempIds = serverRsp.getBundle()
					.getStringArrayList("idsList");
			if (tempIds == null) {
				isGetOver = true;
				return;
			}
			if (tempIds != null && tempIds.size() > 0) {
				ids.addAll(tempIds);
				int idsize = ids.size() > 30 ? 30 : ids.size();
				List<String> idList = new ArrayList<String>();
				idList = ids.subList(0, idsize);
				// editor.putInt(CommonData.GROUP_ID_LIST_SISE, idsize);
				mSess.mPrefMeme.group_id_list_sise.setVal(idsize).commit();
				for (int i = 0; i < idsize; i++) {
					editor.putString(CommonData.GROUP_ID_LIST + i,
							idList.get(i));
				}
				editor.commit();
				totalSize = ids.size();
				int temp = totalSize % pageSize;
				if (temp == 0) {
					totalPage = totalSize / pageSize;
				} else {
					totalPage = totalSize / pageSize + 1;
				}

				getPubMoreGroupList();
			}

			break;
		}
	}

	public void msgStatflush() {

	}

	public void dealJoinGroup(ServerRsp serverRsp) {
	}

	private void msgGroupflush() {
		for (int i = 0; i < groups.size(); i++) {
			if (TxData.txGroup != null) {
				if ((TxData.txGroup.group_id) == groups.get(i).group_id) {
					groups.remove(i);
					groups.add(i, TxData.txGroup);
					adapter.setData(groups);
					adapter.notifyDataSetChanged();
					break;
				}
			}
		}

	}

	// private AsyncTask<Object, Object, Object> mAsyncTask=new
	// AsyncTask<Object, Object, Object>(){
	//
	// @Override
	//
	// protected Object doInBackground(Object... params) {
	//
	// SocketHelper.getSocketHelper(txData).sendPublicGroup(0);
	// return null;
	//
	// }
	//
	// };
	/**
	 * 通过id 获取群列表
	 * 
	 * @param ids
	 * @return
	 */
	private List<TxGroup> getTxGroupListByIds(List<String> ids) {

		if (ids.size() > 0) {
		}
		for (int i = 0; i < ids.size(); i++) {
			String gid = ids.get(i);
			if (gid != null) {
				TxGroup txGroup = TxGroup.getTxGroup(
						GroupPublic.this.getContentResolver(),
						Integer.parseInt(gid));
				if (txGroup != null) {
					groups.add(txGroup);
				}
			}

		}
		return groups;
	}

	private List<String> getGroupIds() {
		List<String> groupIds = new ArrayList<String>();
		// int size = prefs.getInt(CommonData.GROUP_ID_LIST_SISE, 0);
		int size = mSess.mPrefMeme.group_id_list_sise.getVal();
		for (int i = 0; i < size; i++) {
			String s = prefs.getString(CommonData.GROUP_ID_LIST + i, "");
			if (s != null && !s.equals(""))
				groupIds.add(s);
		}
		return groupIds;
	}
}
