package com.tuixin11sms.tx.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ProgressTextBar extends ProgressBar {
	private final float scale = getContext().getResources().getDisplayMetrics().density;

	float x = 0;
	float y = 0;
	float textSize = 0;
	String text = "";
	public ProgressTextBar(Context context, AttributeSet attrs) {
		super(context, attrs);
		String topTextPadding = attrs.getAttributeValue(null, "topTextPadding");  
		String leftTextPadding = attrs.getAttributeValue(null, "leftTextPadding");  
		String textSizeStr = attrs.getAttributeValue(null, "textSize");  
		
		x = convertDisplayUom(leftTextPadding,0);
		y = convertDisplayUom(topTextPadding,0);
		textSize = convertDisplayUom(textSizeStr,30);
		
		 
	}
	
	public void setText(String text){
		this.text = text;
		this.invalidate();
	}
	@Override
	protected synchronized void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		Paint p = new Paint();
		p.setColor(Color.WHITE);
		p.setTextSize(textSize);
		p.setAntiAlias(true);
		canvas.drawText(text, x,
				(float) (y==0.5?(getHeight()*0.7) : y), p);
	}

	private float convertDisplayUom(String sour, float defaultValue) {
		try {
			if (sour.toLowerCase().endsWith("px")) {
				return Float.parseFloat(sour.toLowerCase().replace("px", ""));
			} else if (sour.toLowerCase().endsWith("dp")) {
				return Integer.parseInt(sour.toLowerCase().replace("dp", ""))
						* scale + 0.5f;
			} else if (sour.toLowerCase().endsWith("dip")) {
				return Integer.parseInt(sour.toLowerCase().replace("dip", ""))
						* scale + 0.5f;
			} else if (sour.toLowerCase().endsWith("sp")) {
				return Integer.parseInt(sour.toLowerCase().replace("sp", ""))
						* scale + 0.5f;
			}
		} catch (Exception e) {
		}

		return (defaultValue * scale + 0.5f);
	}

}
