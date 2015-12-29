package com.tuixin11sms.tx.view;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class LevelTextView extends TextView {

	public LevelTextView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		initTypeFace(context);
	}

	

	public LevelTextView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initTypeFace(context);
	}

	public LevelTextView(Context context) {
		super(context);
		initTypeFace(context);
	}
	
	
	/**初始化字体样式*/
	private void initTypeFace(Context context) {
		if(this.isInEditMode()){
			return;
		}
		
		AssetManager mgr=context.getAssets();//得到AssetManager
		Typeface tf=Typeface.createFromAsset(mgr, "fonts/04B08.TTF");//根据路径得到Typeface
		this.setTypeface(tf);//设置字体  
		
		
	}
	

}
