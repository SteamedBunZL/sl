package com.tuixin11sms.tx.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ImageView;
import android.widget.ListView;

public class DragListView extends ListView implements OnScrollListener {

	// 拖拉ListView枚举所有状态
	private enum DListViewState {
		LV_NORMAL, // 普通状态
		LV_PULL_REFRESH, // 下拉状态（为超过mHeadViewHeight）
		LV_RELEASE_REFRESH, // 松开可刷新状态（超过mHeadViewHeight）
		LV_LOADING;// 加载状态
	}

	// 点击加载更多枚举所有状态
	private enum DListViewLoadingMore {
		LV_NORMAL, // 普通状态
		LV_LOADING, // 加载状态
		LV_OVER; // 结束状态
	}

	private View mHeadView;// 头部headView
	private int mHeadViewWidth; // headView的宽（mHeadView）
	private int mHeadViewHeight;// headView的高（mHeadView）

	private int mFirstItemIndex = -1;// 当前视图能看到的第一个项的索引

	// 用于保证startY的值在一个完整的touch事件中只被记录一次
	private boolean mIsRecord = false;

	private int mStartY, mMoveY;// 按下是的y坐标,move时的y坐标

	private DListViewState mlistViewState = DListViewState.LV_NORMAL;// 拖拉状态.(自定义枚举)

	private DListViewLoadingMore loadingMoreState = DListViewLoadingMore.LV_NORMAL;// 加载更多默认状态.

	private final static int RATIO = 2;// 手势下拉距离比.

	private boolean mBack = false;// headView是否返回.

	private OnRefreshLoadingMoreListener onRefreshLoadingMoreListener;// 下拉刷新接口（自定义）

	private boolean isScroller = true;// 是否屏蔽ListView滑动。
	private ImageView mbt;
	private ImageView ad_iv;

	public DragListView(Context context) {
		super(context, null);
		initDragListView(context);
	}

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initDragListView(context);
	}

	// 注入下拉刷新接口
	public void setOnRefreshListener(
			OnRefreshLoadingMoreListener onRefreshLoadingMoreListener) {
		this.onRefreshLoadingMoreListener = onRefreshLoadingMoreListener;
	}

	/***
	 * 初始化ListView
	 */
	public void initDragListView(Context context) {

		initHeadView(context);// 初始化该head.

		setOnScrollListener(this);// ListView滚动监听
	}

	/***
	 * 初始话头部HeadView
	 * 
	 * @param context
	 *            上下文
	 * @param time
	 *            上次更新时间
	 */
	public void initHeadView(Context context) {
		// mHeadView = LayoutInflater.from(context).inflate(R.layout.ad_header,
		// null);
		// mHeadView.setPadding(0, 0, 0, 0);
	}

	public void addHeaderChildView(ImageView ad_iv, ImageView ad_close) {
		this.ad_iv = ad_iv;
		this.mbt = ad_close;
		mlistViewState = DListViewState.LV_LOADING;
		ad_iv.setVisibility(View.VISIBLE);
		mbt.setVisibility(View.VISIBLE);
		mbt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);
				mlistViewState = DListViewState.LV_NORMAL;
				loadingMoreState = DListViewLoadingMore.LV_NORMAL;
			}
		});
	}

	@Override
	public void addHeaderView(View v, Object data, boolean isSelectable) {
		this.mHeadView = v;
		setHeaderDividersEnabled(false);
		super.addHeaderView(v, data, isSelectable);
	}

	public void setWandH(int width, int height) {
		mHeadViewHeight = height;
		mHeadViewWidth = width;
	}

	public void setADBitmap(Drawable d) {
		ad_iv.setBackgroundDrawable(d);
	}

	public void setADOnClickListener(OnClickListener l) {
		ad_iv.setOnClickListener(l);
	}

	/**
	 * touch 事件监听
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		switch (ev.getAction()) {
		// 按下
		case MotionEvent.ACTION_DOWN:
			doActionDown(ev);
			break;
		// 移动
		case MotionEvent.ACTION_MOVE:
			doActionMove(ev);
			break;
		// 抬起
		case MotionEvent.ACTION_UP:
			doActionUp(ev);
			break;
		default:
			break;
		}
		/***
		 * 如果是ListView本身的拉动，那么返回true，这样ListView不可以拖动.
		 * 如果不是ListView的拉动，那么调用父类方法，这样就可以上拉执行.
		 */
		if (isScroller) {
			return super.onTouchEvent(ev);
		} else {
			return true;
		}

	}

	/***
	 * 摁下操作
	 * 
	 * 作用：获取摁下是的y坐标
	 * 
	 * @param event
	 */
	void doActionDown(MotionEvent event) {
		if (mIsRecord == false && mFirstItemIndex == 0) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
	}

	/***
	 * 拖拽移动操作
	 * 
	 * @param event
	 */
	void doActionMove(MotionEvent event) {
		mMoveY = (int) event.getY();// 获取实时滑动y坐标
		// 检测是否是一次touch事件.
		if (mIsRecord == false && mFirstItemIndex == 0) {
			mStartY = (int) event.getY();
			mIsRecord = true;
		}
		/***
		 * 如果touch关闭或者正处于Loading状态的话 return.
		 */
		if (mIsRecord == false || mlistViewState == DListViewState.LV_LOADING) {
			return;
		}
		// 向下啦headview移动距离为y移动的一半.（比较友好）
		int offset = (mMoveY - mStartY) / RATIO;

		switch (mlistViewState) {
		// 普通状态
		case LV_NORMAL: {
			// 如果<0，则意味着上滑动.
			if (offset > 0) {
				// 设置headView的padding属性.
				mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
				switchViewState(DListViewState.LV_PULL_REFRESH);// 下拉状态
			}

		}
			break;
		// 下拉状态
		case LV_PULL_REFRESH: {
			setSelection(0);// 时时保持在顶部.
			// 设置headView的padding属性.
			mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
			if (offset < 0) {
				/***
				 * 要明白为什么isScroller = false;
				 */
				isScroller = false;
				switchViewState(DListViewState.LV_NORMAL);// 普通状态
			} else if (offset > 60) {// 如果下拉的offset超过headView的高度则要执行刷新.
				switchViewState(DListViewState.LV_RELEASE_REFRESH);// 更新为可刷新的下拉状态.
			}
		}
			break;
		// 可刷新状态
		case LV_RELEASE_REFRESH: {
			setSelection(0);// 时时保持在顶部
			// 设置headView的padding属性.
			mHeadView.setPadding(0, offset - mHeadViewHeight, 0, 0);
			// 下拉offset>0，但是没有超过headView的高度.那么要goback 原装.
			if (offset >= 0 && offset <= mHeadViewHeight) {
				mBack = true;
				switchViewState(DListViewState.LV_PULL_REFRESH);
			} else if (offset < 0) {
				switchViewState(DListViewState.LV_NORMAL);
			} else {

			}
		}
			break; 
		default:
			return;
		}
	}

	/***
	 * 手势抬起操作
	 * 
	 * @param event
	 */
	public void doActionUp(MotionEvent event) {
		mIsRecord = false;// 此时的touch事件完毕，要关闭。
		isScroller = true;// ListView可以Scrooler滑动.
		mBack = false;
		// 如果下拉状态处于loading状态.
		if (mlistViewState == DListViewState.LV_LOADING) {
			return;
		}
		// 处理相应状态.
		switch (mlistViewState) {
		// 普通状态
		case LV_NORMAL:

			break;
		// 下拉状态
		case LV_PULL_REFRESH:
			mHeadView.setPadding(0, -1 * mHeadViewHeight, 0, 0);
			switchViewState(mlistViewState.LV_NORMAL);
			break;
		// 刷新状态
		case LV_RELEASE_REFRESH:
			mHeadView.setPadding(0, 0, 0, 0);
			switchViewState(mlistViewState.LV_LOADING);
			onRefresh();// 下拉刷新
			break;
		}

	}

	// 切换headview视图
	private void switchViewState(DListViewState state) {
		// 切记不要忘记时时更新状态。
		mlistViewState = state;
	}

	/***
	 * 下拉刷新
	 */
	private void onRefresh() {
		if (onRefreshLoadingMoreListener != null) {
			onRefreshLoadingMoreListener.onRefresh();
		}
	}

	/***
	 * ListView 滑动监听
	 */
	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem,
			int visibleItemCount, int totalItemCount) {
		mFirstItemIndex = firstVisibleItem;
	}

	/***
	 * 自定义接口
	 */
	public interface OnRefreshLoadingMoreListener {
		/***
		 * // 下拉刷新执行
		 */
		void onRefresh();

		/***
		 * 点击加载更多
		 */
		void onLoadMore();
	}

}
