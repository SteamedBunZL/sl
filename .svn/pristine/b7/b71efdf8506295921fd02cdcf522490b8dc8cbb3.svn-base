package com.shenliao.view;

import java.util.Hashtable;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import com.tuixin11sms.tx.utils.Utils;

public class SLRetiveLayout extends RelativeLayout {

	private static final String TAG = "SLRetiveLayout";

	int mLeft, mRight, mTop, mBottom, currentBottom;

	Hashtable<View, Position> map = new Hashtable<View, SLRetiveLayout.Position>();

	public SLRetiveLayout(Context context, AttributeSet attrs, int defStyle) {

		super(context, attrs, defStyle);

	}

	public SLRetiveLayout(Context context, AttributeSet attrs) {
		
		super(context, attrs);
	
	}

	public SLRetiveLayout(Context context) {

		super(context);

	}
	
	
	
	

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

		//

		int count = getChildCount();

		for (int i = 0; i < count; i++) {

			View child = getChildAt(i);

			Position pos = map.get(child);

			if (pos != null) {

				child.layout(pos.left, pos.top, pos.right, pos.bottom);

			} else {

				if(Utils.debug)Log.i(TAG,"MyLayout---error");

			}

		}

	}

	public int getPosition(int IndexInRow, int childIndex) {

		if (IndexInRow > 0) {

			return getPosition(IndexInRow - 1, childIndex - 1)

			+ getChildAt(childIndex - 1).getMeasuredWidth() + 10;

		}

		return 0;

	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

		//

		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

		int width = MeasureSpec.getSize(widthMeasureSpec);

		mLeft = 10;

		mRight = 10;

		mTop = 15;

		mBottom = 10;

		int j = 0;

		int count = getChildCount();

		for (int i = 0; i < count; i++) {

			Position position = new Position();

			View view = getChildAt(i);

			mLeft = getPosition(i - j, i)+10;

			mRight = mLeft + view.getMeasuredWidth();

			if (mRight >= width) {

				j = i;

				mLeft = getPosition(i - j, i)+10;

				mRight = mLeft + view.getMeasuredWidth();

				mTop += getChildAt(i).getMeasuredHeight() + 15;
				

			}

			
			mBottom = mTop + view.getMeasuredHeight();
			position.left = mLeft;

			position.top = mTop;

			position.right = mRight;

			position.bottom = mBottom;

			map.put(view, position);
			mBottom=mTop+view.getMeasuredHeight()+15;

		}
		
		//mBottom = mTop+40;
		setMeasuredDimension(width, mBottom);

	}

	private class Position {

		int left, top, right, bottom;

	}

}
