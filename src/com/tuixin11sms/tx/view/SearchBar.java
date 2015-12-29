package com.tuixin11sms.tx.view;

import java.util.Map;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.tuixin11sms.tx.R;

public class SearchBar extends View {

	public static String[] strArray = new String[] { "#","A", "B", "C", "D", "E",
			"F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
			"S", "T", "U", "V", "W", "X", "Y", "Z" };
	private int width;
	public static int height;
	
	private TextView tv;
	    
	public static ListView listview;
	
	public static Map<String,Integer> posMap;
	
	public SearchBar(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	

	/* (non-Javadoc)
	 * @see android.view.View#onTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN|| event.getAction() == MotionEvent.ACTION_MOVE) {
        	tv.setVisibility(View.VISIBLE);
        	int tempY = (int) ((event.getY()) / height); 
            if (tempY < strArray.length&&tempY>0) {
            	String zimu = strArray[tempY];
            	tv.setText(zimu);
        		if (event.getY() > 0 && posMap.containsKey(zimu.toLowerCase())){ 
        			listview.setSelection(posMap.get(zimu.toLowerCase())); 
        		}                       
			}
            if(tempY == 0 ){
            	/*ImageSpan span = new ImageSpan(bitmap,DynamicDrawableSpan.ALIGN_BOTTOM);
        		SpannableString sp = new SpannableString("bitmap");
        		sp.setSpan(span, 0, "bitmap".length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);*/
            	/*tv.setText(strArray[0]);
            	listview.setSelection(0);*/
            	tv.setText(strArray[0]);
            	listview.setSelection(0);
            	tv.setVisibility(View.VISIBLE);
//            	setBackgroundResource(0);
            }          
            setBackgroundResource(R.drawable.contact_a_z);
            return true;
        }else{
        	tv.setVisibility(View.GONE);
        	setBackgroundResource(0);
        	
        }
		return super.onTouchEvent(event);
	}
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		for (int i = 0; i < strArray.length; i++) {
//			if(i == 0){
//				bitmap = BitmapFactory.decodeResource(this.getResources(), R.drawable.lens);
//				float scale = (float) ( height/ (float)bitmap.getHeight() * 0.9);
//				Matrix matrix = new Matrix();
//				matrix.postScale(scale, scale);
//				Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//						bitmap.getHeight(), matrix, true);
//				
//				canvas.drawBitmap(resizeBmp, width/2 - resizeBmp.getWidth()/2, height/2 - resizeBmp.getHeight()/2 , new Paint());
//			}else{
				Paint foreground = new Paint(Paint.ANTI_ALIAS_FLAG);
				foreground.setFakeBoldText(true);
				foreground.setColor(Color.GRAY);
				foreground.setStyle(Style.FILL);
				foreground.setTextSize(height * 0.75f);
				foreground.setTextAlign(Paint.Align.CENTER);
				FontMetrics fm = foreground.getFontMetrics();
				float x = width / 2;
				
				float y = height / 2 - (fm.ascent + fm.descent) / 2;
				canvas.drawText(strArray[i], x, i * height + y, foreground);
//			}
		}
		
		/*System.out.println("height="+this.getHeight());
		System.out.println("width="+this.getWidth());*/
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		height = h / strArray.length;
		super.onSizeChanged(w, h, oldw, oldh);
	}


	public TextView getTv() {
		return tv;
	}


	public void setTv(TextView tv) {
		this.tv = tv;
	}


}
