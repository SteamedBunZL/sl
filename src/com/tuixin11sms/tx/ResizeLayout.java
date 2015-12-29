package com.tuixin11sms.tx;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

//TODO 该布局引用被解除，待删除  2014.02.26 shc
public class ResizeLayout  extends RelativeLayout{
	private OnResizeListener mListener;          
	public interface OnResizeListener {         
		void OnResize(int w, int h, int oldw, int oldh);     
	}          
	public void setOnResizeListener(OnResizeListener l) {
		mListener = l;     
	} 
	public ResizeLayout(Context context, AttributeSet attrs) {         
		    super(context, attrs);     
		}
	    @Override     
		protected void onSizeChanged(int w, int h, int oldw, int oldh) {             
			super.onSizeChanged(w, h, oldw, oldh); 
			if (mListener != null) {             
				mListener.OnResize(w, h, oldw, oldh);         
			} 
			//if(Utils.debug)Log.e("onSizeChanged " + count++, "=>onResize called! w="+w + ",h="+h+",oldw="+oldw+",oldh="+oldh);     
		}         
		@Override     
		protected void onLayout(boolean changed, int l, int t, int r, int b) {
			/***
			 * 为了解决这个异常
			 *  java.lang.IllegalStateException:
			 *  The content of the adapter has changed but ListView did not receive a notification. 
			 *  Make sure the content of your adapter is not modified from a background thread, 
			 *  but only from the UI thread. [in ListView(2131558850, class android.widget.ListView) 
			 *  with Adapter(class android.widget.HeaderViewListAdapter)]
			 */
			try{
			  super.onLayout(changed, l, t, r, b);   
			}
			catch(Exception e){
				
			}
			//if(Utils.debug)Log.e("onLayout " + count++, "=>OnLayout called! l=" + l + ", t=" + t + ",r=" + r + ",b="+b);     
		}          
		@Override     
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {         
		    super.onMeasure(widthMeasureSpec, heightMeasureSpec);                  
		   // if(Utils.debug)Log.e("onMeasure " + count++, "=>onMeasure called! widthMeasureSpec=" + widthMeasureSpec + ", heightMeasureSpec=" + heightMeasureSpec);     
	    } 
}
