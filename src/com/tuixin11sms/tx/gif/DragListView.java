package com.tuixin11sms.tx.gif;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.SessionManager.EmojiInfor;
import com.tuixin11sms.tx.gif.GifOrderActivity.DragListAdapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

public class DragListView extends ListView {

	private ImageView dragImageView;// 锟斤拷锟斤拷拽锟斤拷锟筋，锟斤拷实锟斤拷锟斤拷一锟斤拷ImageView
	private int dragSrcPosition;// 锟斤拷指锟较讹拷锟斤拷原始锟斤拷锟叫憋拷锟叫碉拷位锟斤拷
	private int dragPosition;// 锟斤拷指锟较讹拷锟斤拷时锟津，碉拷前锟较讹拷锟斤拷锟斤拷锟叫憋拷锟叫碉拷位锟斤拷

	private int dragPoint;// 锟节碉拷前锟斤拷锟斤拷锟斤拷械锟轿伙拷锟�
	private int dragOffset;// 锟斤拷前锟斤拷图锟斤拷锟斤拷幕锟侥撅拷锟斤拷(锟斤拷锟斤拷只使锟斤拷锟斤拷y锟斤拷锟斤拷锟斤拷)

	private WindowManager windowManager;// windows锟斤拷锟节匡拷锟斤拷锟斤拷
	private WindowManager.LayoutParams windowParams;// 锟斤拷锟节匡拷锟斤拷锟斤拷拽锟斤拷锟斤拷锟绞撅拷牟锟斤拷锟�

	private int scaledTouchSlop;// 锟叫断伙拷锟斤拷锟斤拷一锟斤拷锟斤拷锟斤拷
	private int upScrollBounce;// 锟较讹拷锟斤拷时锟津，匡拷始锟斤拷锟较癸拷锟斤拷锟侥边斤拷
	private int downScrollBounce;// 锟较讹拷锟斤拷时锟津，匡拷始锟斤拷锟铰癸拷锟斤拷锟侥边斤拷
	private boolean isLock = false;

	public DragListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		scaledTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
	}

	public void setLock(boolean lock) {
		isLock = lock;
	}

	// 锟斤拷锟斤拷touch锟铰硷拷锟斤拷锟斤拷实锟斤拷锟角硷拷一锟斤拷锟斤拷锟�
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN && !isLock) {
			int x = (int) ev.getX();
			int y = (int) ev.getY();

			dragSrcPosition = dragPosition = pointToPosition(x, y);
			if (dragPosition == AdapterView.INVALID_POSITION) {
				return super.onInterceptTouchEvent(ev);
			}

			ViewGroup itemView = (ViewGroup) getChildAt(dragPosition
					- getFirstVisiblePosition());
			dragPoint = y - itemView.getTop();
			dragOffset = (int) (ev.getRawY() - y);

			View dragger = itemView.findViewById(R.id.drag_list_item_image);
			if (dragger != null && x > dragger.getLeft() - 20) {
				//
				upScrollBounce = Math.min(y - scaledTouchSlop, getHeight() / 3);
				downScrollBounce = Math.max(y + scaledTouchSlop,
						getHeight() * 2 / 3);

				itemView.setDrawingCacheEnabled(true);
				Bitmap bm = Bitmap.createBitmap(itemView.getDrawingCache());
				startDrag(bm, y);
			}
			return false;
		}
		return super.onInterceptTouchEvent(ev);
	}

	/**
	 * 锟斤拷锟斤拷锟铰硷拷
	 */
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		if (dragImageView != null && dragPosition != INVALID_POSITION
				&& !isLock) {
			int action = ev.getAction();
			switch (action) {
			case MotionEvent.ACTION_UP:
				int upY = (int) ev.getY();
				stopDrag();
				onDrop(upY);
				break;
			case MotionEvent.ACTION_MOVE:
				int moveY = (int) ev.getY();
				onDrag(moveY);
				break;
			default:
				break;
			}
			return true;
		}
		// 也锟斤拷锟斤拷锟斤拷选锟叫碉拷效锟斤拷
		return super.onTouchEvent(ev);
	}

	/**
	 * 准锟斤拷锟较讹拷锟斤拷锟斤拷始锟斤拷锟较讹拷锟斤拷锟酵硷拷锟�
	 * 
	 * @param bm
	 * @param y
	 */
	public void startDrag(Bitmap bm, int y) {
		stopDrag();

		windowParams = new WindowManager.LayoutParams();
		windowParams.gravity = Gravity.TOP;
		windowParams.x = 0;
		windowParams.y = y - dragPoint + dragOffset;
		windowParams.width = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.height = WindowManager.LayoutParams.WRAP_CONTENT;
		windowParams.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
				| WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
				| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
				| WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
		windowParams.format = PixelFormat.TRANSLUCENT;
		windowParams.windowAnimations = 0;

		ImageView imageView = new ImageView(getContext());
		imageView.setImageBitmap(bm);
		windowManager = (WindowManager) getContext().getSystemService("window");
		windowManager.addView(imageView, windowParams);
		dragImageView = imageView;
	}

	/**
	 * 停止锟较讹拷锟斤拷去锟斤拷锟较讹拷锟斤拷锟酵凤拷锟�
	 */
	public void stopDrag() {
		if (dragImageView != null) {
			windowManager.removeView(dragImageView);
			dragImageView = null;
		}
	}

	/**
	 * 锟较讹拷执锟叫ｏ拷锟斤拷Move锟斤拷锟斤拷锟斤拷执锟斤拷
	 * 
	 * @param y
	 */
	public void onDrag(int y) {
		if (dragImageView != null) {
			windowParams.alpha = 0.8f;
			windowParams.y = y - dragPoint + dragOffset;
			windowManager.updateViewLayout(dragImageView, windowParams);
		}
		// 为锟剿憋拷锟解滑锟斤拷锟斤拷锟街革拷锟竭碉拷时锟津，凤拷锟斤拷-1锟斤拷锟斤拷锟斤拷
		int tempPosition = pointToPosition(0, y);
		if (tempPosition != INVALID_POSITION) {
			dragPosition = tempPosition;
		}

		// 锟斤拷锟斤拷
		int scrollHeight = 0;
		if (y < upScrollBounce) {
			scrollHeight = 8;// 锟斤拷锟斤拷锟斤拷锟较癸拷锟斤拷8锟斤拷锟斤拷锟截ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷锟较癸拷锟斤拷锟侥伙拷
		} else if (y > downScrollBounce) {
			scrollHeight = -8;// 锟斤拷锟斤拷锟斤拷锟铰癸拷锟斤拷8锟斤拷锟斤拷锟截ｏ拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟较癸拷锟斤拷锟侥伙拷
		}

		if (scrollHeight != 0) {
			// 锟斤拷锟斤拷锟斤拷锟斤拷姆锟斤拷锟絪etSelectionFromTop()
			setSelectionFromTop(dragPosition,
					getChildAt(dragPosition - getFirstVisiblePosition())
							.getTop() + scrollHeight);
		}
	}

	/**
	 * 锟较讹拷锟斤拷锟铰碉拷时锟斤拷
	 * 
	 * @param y
	 */
	public void onDrop(int y) {

		// 为锟剿憋拷锟解滑锟斤拷锟斤拷锟街革拷锟竭碉拷时锟津，凤拷锟斤拷-1锟斤拷锟斤拷锟斤拷
		int tempPosition = pointToPosition(0, y);
		if (tempPosition != INVALID_POSITION) {
			dragPosition = tempPosition;
		}

		// 锟斤拷锟斤拷锟竭界处锟斤拷
		if (y < getChildAt(0).getTop()) {
			// 锟斤拷锟斤拷锟较边斤拷
			dragPosition = 0;
		} else if (y > getChildAt(getChildCount() - 1).getBottom()) {
			// 锟斤拷锟斤拷锟铰边斤拷
			dragPosition = getAdapter().getCount() - 1;
		}

		// 锟斤拷萁锟斤拷锟�
		if (dragPosition >= 0 && dragPosition < getAdapter().getCount()) {
			@SuppressWarnings("unchecked")
			DragListAdapter adapter = (DragListAdapter) getAdapter();
			EmojiInfor dragItem = (EmojiInfor) adapter.getItem(dragSrcPosition);
			adapter.remove(dragItem);
			adapter.insert(dragItem, dragPosition);
			// Toast.makeText(getContext(), adapter.getList().toString(),
			// Toast.LENGTH_SHORT).show();
		}

	}
}
