package com.shenliao.set.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.data.DataContainer;
import com.shenliao.set.adapter.UserFavouriteSelectAdapter;
import com.shenliao.user.activity.UserInfoPerfectActivity;
import com.shenliao.view.FavoriteViewPager;
import com.shenliao.view.SlGridView;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.contact.TX;
import com.tuixin11sms.tx.data.TxDB.Tx;
import com.tuixin11sms.tx.model.Hobby;
import com.tuixin11sms.tx.model.ServerRsp;
import com.tuixin11sms.tx.net.SocketHelper;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.StringUtils;
import com.tuixin11sms.tx.utils.Utils;

/**
 * 兴趣爱好
 * 
 * @author xch
 * 
 */
public class SetUpdateFavouriteActivity extends BaseActivity implements
		OnClickListener {
	private FavoriteViewPager viewPager;
	private ArrayList<View> pageViews;
	private MyPagerAdapter myAdapter;
	private TextView submitBtn;
	private int pageNum;
	private List<String> selectList = new ArrayList<String>();
	private UserFavouriteSelectAdapter favouriteadpater;
	private TextView numText;// 记录选中几个
	private int selectNum = 0;
	private GridView selectGridView;
	private SelectedAdapter selectedAdapter;
	private View mView;
	private List<Hobby> list = new ArrayList<Hobby>();
	public static Map<String, View> viewMap = new HashMap<String, View>();
	// private SharedPreferences prefs;
	// private Editor editor;
	private UpdateReceiver updatereceiver;
	private static final int FAVOURITE_CHANGE_SUCCESS = 1;
	private static final int FAVOURITE_CHANGE_FAILED = 2;
	private static final int FAVOURITE_CHANGE_NOTCHANGE = 3;
	private RelativeLayout title;
	private RelativeLayout gridViewRel;
	private RelativeLayout viewPagerRel;
	private ImageView[] imageViews;
	public static final int PERFECTNIFO = 101;
	public static final int NOTPERFECTINFO = 102;
	public static final String GOINPAGE = "goinpage";
	public int goinpage = 102;
	public Intent intent;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_setting_userinfo_update_favourite);
		init();
		show();
	}

	// 初始化
	private void init() {
		gridViewRel = (RelativeLayout) findViewById(R.id.rrr);
		// viewPagerRel = (RelativeLayout) findViewById(R.id.viepager_rel);
		numText = (TextView) findViewById(R.id.sl_tab_setting_userinfo_favourite_mySelectNum);
		selectGridView = (GridView) findViewById(R.id.usl_tab_setting_userinfo_favourite_gridView);
		submitBtn = (TextView) findViewById(R.id.sl_tab_setting_favourite_sendBtn);
		back_left = (LinearLayout) findViewById(R.id.btn_back_left);

		submitBtn.setOnClickListener(this);
		back_left.setOnClickListener(this);
		// prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
		// Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
		// editor = prefs.edit();
		intent = this.getIntent();
		goinpage = intent.getIntExtra(GOINPAGE, NOTPERFECTINFO);
		selectedAdapter = new SelectedAdapter(SetUpdateFavouriteActivity.this);
		selectGridView.setAdapter(selectedAdapter);
		// List<String>
		// mlist=StringUtils.str2List(prefs.getString(CommonData.HOBBY, ""));

		if (mSess.mPrefMeme.hobby.getVal().equals("0")) {
			mSess.mPrefMeme.hobby.setVal("");
		}

		List<String> mlist = StringUtils.str2List(mSess.mPrefMeme.hobby
				.getVal());
		selectList = new ArrayList<String>(mlist);
		if (selectList.contains("0")) {
			selectList.remove("0");
		}		
		selectNum = selectList.size();
		numText.setText("(" + selectNum + "/10)");
		list = DataContainer.getHobbyList();
	}

	public void show() {
		myAdapter = new MyPagerAdapter();
		viewPager = (FavoriteViewPager) findViewById(R.id.sl_userinfo_favourite_pager);
		pageNum = list.size() / 16 + ((list.size() % 16 > 0 ? 1 : 0));
		pageViews = new ArrayList<View>();
		favouriteadpater = new UserFavouriteSelectAdapter(
				SetUpdateFavouriteActivity.this);

		for (int i = 0; i < pageNum; i++) {
			final List<Hobby> subList = list
					.subList(i * 16, ((i + 1) * 16 > list.size()) ? list.size()
							: ((i + 1) * 16));
			SlGridView gd = new SlGridView(SetUpdateFavouriteActivity.this);
			gd.setSelector(new ColorDrawable(Color.TRANSPARENT));
			favouriteadpater = new UserFavouriteSelectAdapter(this);
			favouriteadpater.setList(subList);
			favouriteadpater.setSelectList(selectList);
			gd.setAdapter(favouriteadpater);
			gd.setNumColumns(4);
			gd.setHorizontalSpacing(7);
			gd.setVerticalSpacing(7);
			gd.setColumnWidth(60);
			gd.setStretchMode(2);
			// 添加view
			pageViews.add(gd);
			gd.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View view,
						int position, long arg3) {

					if (selectList.contains(String.valueOf(subList
							.get(position).getId()))) {
						selectList.remove(String.valueOf(subList.get(position)
								.getId()));
						selectNum = selectNum - 1;
						numText.setText("(" + selectNum + "/10)");
						viewMap.remove(String.valueOf(subList.get(position)
								.getId()));

						view.setBackgroundResource(R.drawable.sll_favourite_textbackgroud_selector);
					} else {
						if (selectList.size() < 10) {
							selectNum = selectNum + 1;
							selectList.add(String.valueOf(subList.get(position)
									.getId()));
							viewMap.put(String.valueOf(subList.get(position)
									.getId()), view);
							view.setBackgroundResource(R.drawable.sll_favourite_textbackgroud_press);
							numText.setText("(" + selectNum + "/10)");
						} else {
							Toast.makeText(SetUpdateFavouriteActivity.this,
									"最多只可以选择10项兴趣爱好", Toast.LENGTH_SHORT)
									.show();
						}
					}
					// selectedAdapter.setList(selectList);
					selectedAdapter.notifyDataSetChanged();

				}
			});

		}
		LinearLayout layout = (LinearLayout) findViewById(R.id.favourite_image_linear);
		imageViews = new ImageView[pageViews.size()];
		for (int i = 0; i < pageViews.size(); i++) {

			// LinearLayout newL = new LinearLayout(this);
			ImageView arrowRT = new ImageView(SetUpdateFavouriteActivity.this);
			arrowRT.setLayoutParams(new LinearLayout.LayoutParams(
					LinearLayout.LayoutParams.WRAP_CONTENT,
					LinearLayout.LayoutParams.WRAP_CONTENT));
			arrowRT.setPadding(3, 0, 3, 0);
			// newL.addView(arrowRT, lp_wrap);

			imageViews[i] = arrowRT;
			layout.addView(imageViews[i]);
			// 默认选中的是第一张图片，此时第一个小圆点是选中状态，其他不是
			if (i == 0) {
				imageViews[i].setImageResource(R.drawable.lov_07);
			} else {
				imageViews[i].setImageResource(R.drawable.lov_09);
			}

		}
		viewPager.setAdapter(myAdapter);
		viewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				imageViews[position].setImageResource(R.drawable.lov_07);
				for (int i = 0; i < imageViews.length; i++) {
					if (position != i)
						imageViews[i].setImageResource(R.drawable.lov_09);

				}

			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int arg0) {

			}
		});
	}

	private class MyPagerAdapter extends PagerAdapter {

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public Object instantiateItem(View arg0, int arg1) {
			((ViewPager) arg0).addView(pageViews.get(arg1));
			return pageViews.get(arg1);
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == (arg1);
		}

		@Override
		public void finishUpdate(View arg0) {
		}

		@Override
		public void restoreState(Parcelable arg0, ClassLoader arg1) {
		}

		@Override
		public Parcelable saveState() {
			return null;
		}

		@Override
		public void startUpdate(View arg0) {
		}

	}

	private class SelectedAdapter extends BaseAdapter {

		// private List<String> list = new ArrayList<String>();
		// private Context con;
		private LayoutInflater inflater;
		private int[] colors = new int[] {
				R.drawable.sll_user_select_favourite_one,
				R.drawable.sll_user_select_favourite_two,
				R.drawable.sll_user_select_favourite_three };
		private int[] textColors;

		public SelectedAdapter(Context con) {
			// this.con = con;
			inflater = LayoutInflater.from(con);
			textColors = new int[] {
					con.getResources().getColor(R.color.user_fav_text_one),
					con.getResources().getColor(R.color.user_fav_text_two),
					con.getResources().getColor(R.color.user_fav_text_three) };
		}

		@Override
		public int getCount() {
			if (selectList != null && selectList.size() > 0) {
				return selectList.size();
			} else {
				return 0;
			}
		}

		@Override
		public Object getItem(int arg0) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {
			if (convertView == null) {
				convertView = inflater.inflate(
						R.layout.sll_favourite_selected_gridview_items, null);
			}
			int colorNum = position % 3;
			convertView.setBackgroundResource(colors[colorNum]);
			TextView textView = (TextView) convertView
					.findViewById(R.id.sl_tab_userinfo_favourite_selected_textView);
			textView.setTextColor(textColors[colorNum]);

			textView.setText(DataContainer.getHobbyNameById(selectList
					.get(position)));
			Button btn = (Button) convertView.findViewById(R.id.selected_clear);
			btn.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					if (viewMap.get(selectList.get(position)) != null) {
						viewMap.get(selectList.get(position))
								.setBackgroundResource(
										R.drawable.sll_favourite_textbackgroud_selector);
					}

					selectList.remove(position);
					selectNum = selectNum - 1;
					numText.setText("(" + selectNum + "/10)");
					notifyDataSetChanged();

				}
			});

			return convertView;
		}
	}

	// 单击事件监听
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.sl_tab_setting_favourite_sendBtn:
			// prefs.edit().putString(CommonData.HOBBY,
			// StringUtils.list2String(selectList)).commit();

			if (selectList.contains(0)) {
				selectList.remove(0);
			}
			if (StringUtils.list2String(selectList) != null
					&& !StringUtils.list2String(selectList).equals("")) {

				mSess.mPrefMeme.hobby.setVal(
						StringUtils.list2String(selectList)).commit();
			} else {
				mSess.mPrefMeme.hobby.setVal("0").commit();
			}

			TX.tm.reloadTXMe();// ///
			showDialogTimer(SetUpdateFavouriteActivity.this, R.string.prompt,
					R.string.group_edit_save, 10 * 1000).show();
			mSess.getSocketHelper().sendUserInforChange();
			break;
		case R.id.btn_back_left:
			finish();
			break;
		}

	}

	private void registReceiver() {
		if (updatereceiver == null) {
			updatereceiver = new UpdateReceiver();
			IntentFilter filter = new IntentFilter();
			// 为 BroadcastReceiver 指定 action ，使之用于接收同 action 的广播
			filter.addAction(Constants.INTENT_ACTION_CHANGE_USERNAME_RSP);
			this.registerReceiver(updatereceiver, filter);
		}
	}

	// 修改广播接收器
	public class UpdateReceiver extends BroadcastReceiver {
		public void onReceive(Context context, Intent intent) {
			ServerRsp serverRsp = Utils.getServerRsp(intent);
			if (Constants.INTENT_ACTION_CHANGE_USERNAME_RSP.equals(intent
					.getAction())) {
				dealFavouriteChange(serverRsp);
			}
		}

		// 处理修改昵称返回结果方法
		private void dealFavouriteChange(ServerRsp serverRsp) {
			cancelDialogTimer();
			if (serverRsp != null && serverRsp.getStatusCode(Tx.HOBBY) != null) {

				switch (serverRsp.getStatusCode(Tx.HOBBY)) {

				case CHANGE_HOBBY_SUCCESS:

					handler.sendEmptyMessage(FAVOURITE_CHANGE_SUCCESS);
					break;
				case CHANGE_HOBBY_FAILED:
					// String fbret = serverRsp.getBundle().getString("fbret");
					// Message message =
					// handler.obtainMessage(LANGUAGE_CHANGE_FAILED, fbret);
					handler.sendEmptyMessage(FAVOURITE_CHANGE_FAILED);

					break;
				case CHANGE_HOBBY_NOTCHANGE:
					handler.sendEmptyMessage(FAVOURITE_CHANGE_NOTCHANGE);

					break;
				}
			}

		}
	}

	private Handler handler = new Handler() {

		public void handleMessage(Message msg) {

			int num = msg.what;
			switch (num) {
			case FAVOURITE_CHANGE_SUCCESS:
				if (goinpage == PERFECTNIFO) {
					setResult(UserInfoPerfectActivity.RESULTCODE_FOR_RESULT_FAVOURITE);
				} else {
					setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_FAVOURITE);
				}

				SetUpdateFavouriteActivity.this.finish();
				break;
			case FAVOURITE_CHANGE_FAILED:
				// prefs.edit().putString(CommonData.HOBBY, "").commit();
				mSess.mPrefMeme.hobby.setVal("").commit();
				TX.tm.reloadTXMe();// ////
				showToast("修改兴趣爱好失败");
				break;

			case FAVOURITE_CHANGE_NOTCHANGE:
				if (goinpage == PERFECTNIFO) {
					setResult(UserInfoPerfectActivity.RESULTCODE_FOR_RESULT_FAVOURITE);
				} else {
					setResult(SetUserInfoActivity.RESULTCODE_FOR_RESULT_FAVOURITE);
				}

				SetUpdateFavouriteActivity.this.finish();
				break;
			}
			super.handleMessage(msg);
		}
	};
	private LinearLayout back_left;

	@Override
	protected void onResume() {
		registReceiver();
		super.onResume();
	}

	@Override
	protected void onStop() {
		if (updatereceiver != null) {
			this.unregisterReceiver(updatereceiver);
			updatereceiver = null;
		}

		super.onStop();
	}

}
