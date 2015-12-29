package com.tuixin11sms.tx.view;

import com.tuixin11sms.tx.R;

import android.content.Context;
import android.os.Parcel;
import android.text.ParcelableSpan;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.ForegroundColorSpan;
import android.text.style.MetricAffectingSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;


/**显示被赞总数的TextView
 * @author SHC
 */
public class PraiseCountTextView extends TextView {

	public PraiseCountTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	public PraiseCountTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public PraiseCountTextView(Context context) {
		super(context);
	}

	@Override
	public void setText(CharSequence text, BufferType type) {
		if (text.charAt(text.length()-1)=='+') {
			SpannableString msp = new SpannableString(text);

			msp.setSpan(new TopAlignSpan(), text.length()-1, text.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);  // 上标
			
			msp.setSpan(new RelativeSizeSpan(0.8f), text.length()-1, text.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 0.5f表示默认字体大小的一半
			// 设置字体前景色
			msp.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.content_color_blue)), 0, text.length(),
					Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); // 设置前景色为主体蓝色
			text = msp;
		}
		super.setText(text, type);
	}
	
	
	
	
	public class TopAlignSpan extends MetricAffectingSpan implements ParcelableSpan {
		private final int TOP_ALIGN_SPAN = 123456789;
		
		
	    public TopAlignSpan() {
	    }
	    
	    public TopAlignSpan(Parcel src) {
	    }
	    
	    public int getSpanTypeId() {
	        return TOP_ALIGN_SPAN;
	    }
	    
	    public int describeContents() {
	        return 0;
	    }

	    public void writeToParcel(Parcel dest, int flags) {
	    }

	    @Override
	    public void updateDrawState(TextPaint tp) {
	        tp.baselineShift += (int) (tp.ascent() / 4);
	    }

	    @Override
	    public void updateMeasureState(TextPaint tp) {
	        tp.baselineShift += (int) (tp.ascent() / 4);
	    }
	}

}



