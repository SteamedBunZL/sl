package com.shenliao.group.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.shenliao.group.adapter.GroupIndexAdapter;
import com.shenliao.group.util.DownLoadAD;
import com.shenliao.group.util.GroupUtils;
import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.activity.ADActivity;
import com.tuixin11sms.tx.activity.BaseActivity;
import com.tuixin11sms.tx.utils.Constants;
import com.tuixin11sms.tx.utils.Utils;
import com.umeng.analytics.MobclickAgent;

public class GroupIndex extends BaseActivity implements OnClickListener {
	private String title[] = { "热门聊天室", "我的聊天室", "最近访问" };
	private ArrayList<TextView> textViews;
	private ViewPager viewPager;
	private ArrayList<View> pageViews;
	private PopupWindow upMorePopWindow;
	private LayoutInflater mInflate;
	private Display display;
	private RelativeLayout uplist1;
	private RelativeLayout uplist2;
	private ImageView moreBtn;
	private Button mCloseAd;
	private ImageView adImage;
	private RelativeLayout mImageRelative;

	private TextView[] tvs;
	private TextView textView_right;
	private TextView textView_mid;
	private TextView textView_left;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_group_index);
		display = getWindowManager().getDefaultDisplay();
//		linearLayout = (LinearLayout) findViewById(R.id.ll_main);
		viewPager = (ViewPager) findViewById(R.id.pager);
//		horizontalScrollView = (HorizontalScrollView) findViewById(R.id.horizontalScrollView);

		textViews = new ArrayList<TextView>();
		init();
		InItTitle();
		InItView();
		textView_right.setBackgroundResource(R.drawable.tab_23);
		textView_right.setId(0);
		textViews.add(textView_right);
		textView_right.setTextColor(Color.WHITE);
	}

	private int offset = 0;// 动画图片偏移量
	private int currIndex = 0;// 当前页卡编号
	private int bmpW;// 动画图片宽度

	public class MyOnPageChangeListener implements OnPageChangeListener {

		int one = offset * 2 + bmpW;// 页卡1 -> 页卡2 偏移量
		int two = one * 2;// 页卡1 -> 页卡3 偏移量

		public void onPageScrollStateChanged(int arg0) {

		}

		public void onPageScrolled(int arg0, float arg1, int arg2) {

		}

		public void onPageSelected(int arg0) {
			Animation animation = new TranslateAnimation(one * currIndex, one
					* arg0, 0, 0);// 显然这个比较简洁，只有一行代码。
			currIndex = arg0;
			animation.setFillAfter(true);// True:图片停在动画结束位置
			animation.setDuration(300);
			int i = viewPager.getCurrentItem();

			if (textViews.size() > 0 && textViews != null) {
				int id = textViews.get(textViews.size() - 1).getId();
				if (id == 0) {
					textViews.get(textViews.size() - 1).setBackgroundResource(
							R.drawable.tab_18);
				} else if (id == 1) {
					textViews.get(textViews.size() - 1).setBackgroundResource(
							R.drawable.tab_19);
				} else if (id == 2) {
					textViews.get(textViews.size() - 1).setBackgroundResource(
							R.drawable.tab_20);
				}
				textViews.get(textViews.size() - 1).setTextColor(getResources().getColor(R.color.content_color_blue));

			}
			if (i == 0) {
				tvs[i].setBackgroundResource(R.drawable.tab_23);
			} else if (i == 1) {
				tvs[i].setBackgroundResource(R.drawable.tab_24);
			} else if (i == 2) {
				tvs[i].setBackgroundResource(R.drawable.tab_25);
			}
			tvs[i].setTextColor(Color.WHITE);

			textViews.add(tvs[i]);

		}

	}

	/**
	 * 
	 * @return true 显示广告 false 不显示
	 */
	private boolean checkTime() {
		long lastTime = getSharedPreferences(GroupUtils.AD_SETTING, 0).getLong(
				GroupUtils.AD_LASTTIME, 0);
		// long nextTime = 24 * 60 * 60 * 1000 + lastTime;
		long nextTime = 0;
		long nowTime = System.currentTimeMillis();
		if (nowTime > nextTime) {
			return true;
		} else {
			return false;
		}
	}

	private void init() {
		adImage = (ImageView) findViewById(R.id.group_index_ad);
		DownLoadAD.getAdURL(checkTime(), handler, GroupIndex.this);
		mImageRelative = (RelativeLayout) findViewById(R.id.group_index_imagerelative);
		mCloseAd = (Button) findViewById(R.id.group_index_closebtn);
		mCloseAd.setOnClickListener(ItemOclick);
		moreBtn = (ImageView) findViewById(R.id.group_index_more);
		moreBtn.setOnClickListener(ItemOclick);
	}

	/***
	 * init view
	 */
	void InItView() {
		pageViews = new ArrayList<View>();
		View view01 = getLocalActivityManager().startActivity("activity01",
				new Intent(this, GroupPublic.class)).getDecorView();
		View view02 = getLocalActivityManager().startActivity("activity02",
				new Intent(this, GroupMine.class)).getDecorView();
		View view03 = getLocalActivityManager().startActivity("activity03",
				new Intent(this, GroupVisited.class)).getDecorView();

		pageViews.add(view01);
		pageViews.add(view02);
		pageViews.add(view03);

		viewPager.setAdapter(new myPagerView());
		viewPager.clearAnimation();
		viewPager.setCurrentItem(0);
		viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
	}

	/***
	 * init title
	 */
	void InItTitle() {

		// int height = 70;

		textView_right = (TextView) findViewById(R.id.room_name_tv_right);
		textView_right.setId(0);
		textView_right.setOnClickListener(new MyOnClickListener(0));

		textView_mid = (TextView) findViewById(R.id.room_name_tv_mid);
		textView_mid.setId(1);
		textView_mid.setOnClickListener(new MyOnClickListener(1));

		textView_left = (TextView) findViewById(R.id.room_name_tv_left);
		textView_left.setId(2);
		textView_left.setOnClickListener(new MyOnClickListener(2));

		tvs = new TextView[] { textView_right, textView_mid, textView_left };

		// // 分割线
		// View view = new View(this);
		// LinearLayout.LayoutParams layoutParams = new
		// LayoutParams(LayoutParams.WRAP_CONTENT,
		// LayoutParams.WRAP_CONTENT);
		// layoutParams.width = 1;
		// layoutParams.height = hdip;
		// layoutParams.gravity = Gravity.CENTER;
		// view.setLayoutParams(layoutParams);
		// view.setBackgroundColor(R.color.gray);

		// if (i != title.length - 1) {
		// }
	}

	/**
	 * 
	 * 头标点击监听 3
	 */
	private class MyOnClickListener implements OnClickListener {
		private int index = 0;

		public MyOnClickListener(int i) {
			index = i;
		}

		public void onClick(View v) {
			viewPager.setCurrentItem(index);
		}

	}

	/***
	 * 选中效果
	 */
	public void setSelector(int id) {
		for (int i = 0; i < title.length; i++) {
			if (id == i) {
				if (id == 0) {
					textViews.get(id).setBackgroundResource(R.drawable.tab_23);
				} else if (id == 1) {
					textViews.get(id).setBackgroundResource(R.drawable.tab_24);
				} else {
					textViews.get(id).setBackgroundResource(R.drawable.tab_25);
				}
				textViews.get(id).setTextColor(Color.WHITE);
				viewPager.setCurrentItem(i);
			} else {
				if (i == 0) {
					textViews.get(id).setBackgroundResource(R.drawable.tab_23);
				} else if (i == 1) {
					textViews.get(id).setBackgroundResource(R.drawable.tab_24);
				} else {
					textViews.get(id).setBackgroundResource(R.drawable.tab_25);
				}
				textViews.get(i).setTextColor(Color.WHITE);
			}

		}
	}

	@Override
	public void onClick(View v) {
		setSelector(v.getId());

	}

	/**
	 * 点击事件
	 */
	OnClickListener ItemOclick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.group_index_closebtn:
				if (mImageRelative.getVisibility() == View.VISIBLE) {
					mImageRelative.setVisibility(View.GONE);
					// getTopMyGroup();
					getSharedPreferences(GroupUtils.AD_SETTING, 0)
							.edit()
							.putLong(GroupUtils.AD_LASTTIME,
									System.currentTimeMillis()).commit();
				}
				break;
			case R.id.group_index_more:
				creatUpMorePop();
				break;
			default:
				break;
			}

		}
	};

	@Override
	protected void onResume() {
		super.onResume();
		//下面几行代码有什么用吗？ 2014.02.25 shc
//		int[] pxs = new int[] { 66, 65, 44, 15, 6, 122, 86, 9, 8 };
//
//		for (int px : pxs) {
//			int dp = Utils.px2dip(px, GroupIndex.this);
//		}
		MobclickAgent.onResume(this);
		refresh();
	}
	@Override
	protected void onPause() {
		super.onPause();
		MobclickAgent.onPause(this);
	}

	private void refresh() {
		Intent i = new Intent(Constants.INTENT_ACTION_FLUSH_GROUP);
		sendBroadcast(i);
	}

	class myPagerView extends PagerAdapter {
		int mCount = pageViews.size();

		// 显示数目
		@Override
		public int getCount() {
			return pageViews.size();
		}

		@Override
		public boolean isViewFromObject(View arg0, Object arg1) {
			return arg0 == arg1;
		}

		@Override
		public int getItemPosition(Object object) {
			return super.getItemPosition(object);
		}

		@Override
		public void destroyItem(View arg0, int arg1, Object arg2) {
			((ViewPager) arg0).removeView(pageViews.get(arg1));
		}

		/***
		 * 获取每一个item， 类于listview中的getview
		 */
		@Override
		public Object instantiateItem(View arg0, int arg1) {
			if (arg1 >= pageViews.size()) {
				int newPosition = arg1 % pageViews.size();

				arg1 = (int) (newPosition + 0.5);
				mCount++;
			}
			if (arg1 < 0) {
				arg1 = (int) (-arg1 - 0.5);
				mCount--;
			}

			try {
				((ViewPager) arg0).addView(
						pageViews.get(arg1 % pageViews.size()), 0);
			} catch (Exception e) {
			}

			return pageViews.get(arg1 % pageViews.size());
		}

	}

	public void creatUpMorePop() {

		if (upMorePopWindow == null) {
			mInflate = LayoutInflater.from(GroupIndex.this);
			View popupWindow_view = mInflate.inflate(
					R.layout.sl_group_index_more_pop, null);

			upMorePopWindow = new PopupWindow(popupWindow_view,
					(int) (display.getWidth() * 0.55),
					LinearLayout.LayoutParams.WRAP_CONTENT, true);// 创建PopupWindow实例

			upMorePopWindow.setAnimationStyle(R.style.chat_up_anim);

			upMorePopWindow.setFocusable(true);

			upMorePopWindow.setBackgroundDrawable(new BitmapDrawable());

//			createText = (TextView) popupWindow_view
//					.findViewById(R.id.msgRoom_more_item3_text);
//			searchText = (TextView) popupWindow_view
//					.findViewById(R.id.msgRoom_more_item2_text);
			uplist1 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item1);
			uplist2 = (RelativeLayout) popupWindow_view
					.findViewById(R.id.msgRoom_more_item2);

			//3.9版本不能创建聊天室
//			uplist1.setOnClickListener(new OnClickListener() {
//
//				@Override
//				public void onClick(View v) {
//					Intent intent = new Intent(GroupIndex.this,
//							GroupCreate.class);
//					startActivity(intent);
//					upMorePopWindow.dismiss();
//
//				}
//			});
			uplist1.setEnabled(false);
			uplist1.findViewById(R.id.msgRoom_more_item1_icon).getBackground().setAlpha(100);//设置图片透明度
			((TextView)uplist1.findViewById(R.id.msgRoom_more_item1_text)).setTextColor(Color.parseColor("#64ffffff"));//设置文字透明度
			
			
			uplist2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					Intent intent = new Intent(GroupIndex.this,
							GroupSearch.class);
					intent.putExtra("isPublicGroup",true);
					startActivity(intent);
					upMorePopWindow.dismiss();
				}
			});

			upMorePopWindow.setTouchInterceptor(new OnTouchListener() {

				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_DOWN) {

					}
					return false;
				}
			});

			upMorePopWindow.update();

			upMorePopWindow.showAsDropDown(moreBtn,
					-(int) (display.getWidth() * 0.4), 0);

		} else {
			if (upMorePopWindow.isShowing()) {

				upMorePopWindow.dismiss();

			} else {

				upMorePopWindow.update();

				upMorePopWindow.showAsDropDown(moreBtn,
						-(int) (display.getWidth() * 0.4), 0);

			}

		}

	}

	private Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case GroupUtils.SHOW_AD:
				final HashMap<String, String> map = (HashMap<String, String>) msg.obj;
				mImageRelative.setVisibility(View.VISIBLE);
				DisplayMetrics metric = new DisplayMetrics();
				getWindowManager().getDefaultDisplay().getMetrics(metric);
				int wdip = metric.widthPixels;
				int hdip = (int) (0.1575 * wdip);
				Bitmap ddd = Utils.zoomBitmap((new BitmapDrawable(
						(BitmapFactory.decodeFile((String) map.get("local")))))
						.getBitmap(), wdip, hdip);
				adImage.setImageBitmap(ddd);
				adImage.postInvalidate();
				adImage.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						Intent i = new Intent(GroupIndex.this, ADActivity.class);
						i.putExtra("url", map.get("url"));
						startActivity(i);

					}
				});

				break;

			}
		}

	};
	Toast exitToast = null;
	int c = 0;

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
			if (event.getAction() == KeyEvent.ACTION_DOWN) {
				if (isqut == 0) {
					isqut = 1;
					exitToast = new Toast(getApplicationContext());
					LayoutInflater mInflater = LayoutInflater
							.from(getApplicationContext());
					View toastView = mInflater.inflate(
							R.layout.message_exit_toast, null);
					TextView exitText = (TextView) toastView
							.findViewById(R.id.message_exit_text);
					exitText.setText(R.string.message_exit_str);
					exitToast.setDuration(Toast.LENGTH_SHORT);
					exitToast.setView(toastView);
					exitToast.show();
					new Timer().schedule(new TimerTask() {
						public void run() {
							isqut = 0;
						}
					}, 2000);
				} else if (isqut == 1) {
					isqut = 0;
					if (exitToast != null)
						exitToast.cancel();
					TxData.finishAll();

				}
				return true;

			}
			return super.dispatchKeyEvent(event);
		} else {
			return super.dispatchKeyEvent(event);
		}
	}

	int isqut;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		MenuInflater mInflater = getMenuInflater();

		mInflater.inflate(R.menu.tuixin_contacts_menu2_nogroup, menu);

		return super.onCreateOptionsMenu(menu);

	}

	public boolean onPrepareOptionsMenu(Menu menu) { // 更新menu
		super.onPrepareOptionsMenu(menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();
		switch (id) {
		case R.id.texit_menu:
			// Utils.exitProcessLogic(txdata, this, getEditor());
			GroupUtils.showDialog(GroupIndex.this, R.string.logout_prompt,
					R.string.dialog_okbtn, R.string.dialog_nobtn,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							Utils.exitProcessLogic(GroupIndex.this);
						}
					});

			break;
		}

		return super.onOptionsItemSelected(item);

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (GroupIndexAdapter.mAvatarCache != null) {
			GroupIndexAdapter.mAvatarCache.clear();// 清空群组头像的缓存
		}
	}

}
