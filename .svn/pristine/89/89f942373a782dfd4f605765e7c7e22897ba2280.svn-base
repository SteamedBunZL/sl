package com.tuixin11sms.tx.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnDoubleTapListener;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ViewFlipper;

import com.tuixin11sms.tx.R;
import com.tuixin11sms.tx.TxData;
import com.tuixin11sms.tx.utils.Utils;

public class TutorialTeachActivity extends BaseActivity implements
		OnGestureListener, OnDoubleTapListener {
	protected static final String TAG = "TutorialTeachActivity";
	private ViewFlipper mViewFlipper;
//	private Editor editor;
//	private SharedPreferences prefs;
	private GestureDetector mGestureDetector;
    private int index=1;
    private ImageView point1;
    private ImageView point2;
    private ImageView point3;
	private ImageView point4;
//    private ImageView point5;
    private ImageView turnButton;
//    private ImageView next_l;
//    private ImageView next_r;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		TxData.addActivity(this);
		setContentView(R.layout.activity_tutorialteach);
		
		mGestureDetector = new GestureDetector(this);
//		prefs = getSharedPreferences(PrefsMeme.MEME_PREFS,
//				Context.MODE_WORLD_READABLE + Context.MODE_WORLD_WRITEABLE);
//		editor = prefs.edit();
		point1 = (ImageView) findViewById(R.id.point1);
		point2 = (ImageView) findViewById(R.id.point2);
		point3 = (ImageView) findViewById(R.id.point3);
		point4 = (ImageView) findViewById(R.id.point4);
//		point5=(ImageView)findViewById(R.id.point5);
//		next_l = (ImageView) findViewById(R.id.next_l);
//		next_r = (ImageView) findViewById(R.id.next_r);
//		next_l.setVisibility(View.GONE);
		ImageView buttonNext1 = (ImageView) findViewById(R.id.btn1);
		mViewFlipper = (ViewFlipper) findViewById(R.id.teach);
		buttonNext1.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// 在layout中定义的属性，也可以在代码中指定
				// mViewFlipper.setInAnimation(getApplicationContext(),
				// R.anim.push_left_in);
				// mViewFlipper.setOutAnimation(getApplicationContext(),
				// R.anim.push_left_out);
				// mViewFlipper.setPersistentDrawingCache(ViewGroup.PERSISTENT_ALL_CACHES);
				// mViewFlipper.setFlipInterval(1000);
				mViewFlipper.showNext();
				// 调用下面的函数将会循环显示mViewFlipper内的所有View。
				// mViewFlipper.startFlipping();
				if (index < mViewFlipper.getChildCount()) {
					index++;
				} else {
					index = 1;
				}
				handleMessage(1);
				
			}
		});

//		ImageView turnView1 = (ImageView) findViewById(R.id.turnimg1);//跳过引导图，直接进入主页面
//		turnView1.setOnClickListener(new View.OnClickListener() {
//			public void onClick(View view) {
////				editor.putBoolean(CommonData.TEACHER, true).commit();
//				mSess.mPrefMeme.teacher.setVal(true).commit();
//				TutorialTeachActivity.this.finish();
//				System.gc();
//				Intent helpIntent = new Intent(TutorialTeachActivity.this,
//						TuiXinMainTab.class);
//				startActivity(helpIntent);
//			}
//
//		});
		 turnButton =  (ImageView) findViewById(R.id.turnButton);
		 turnButton.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
//					editor.putBoolean(CommonData.TEACHER, true).commit();
					mSess.mPrefMeme.teacher.setVal(true).commit();
					TutorialTeachActivity.this.finish();
					System.gc();
					Intent helpIntent = new Intent(TutorialTeachActivity.this,
							TuiXinMainTab.class);
					startActivity(helpIntent);
				}

			});

//		 next_l.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View view) {
//					// 调用下面的函数将会循环显示mViewFlipper内的所有View。
//					// mViewFlipper.startFlipping();
//					mViewFlipper.setInAnimation(getApplicationContext(),
//							R.anim.slide_right_in);
//					mViewFlipper.setOutAnimation(getApplicationContext(),
//							R.anim.slide_right_out);
//					mViewFlipper.showPrevious();
//					mViewFlipper.setInAnimation(getApplicationContext(),
//							R.anim.push_left_in);
//					mViewFlipper.setOutAnimation(getApplicationContext(),
//							R.anim.push_left_out);
//
//					if (index > 1) {
//						index--;
//					} else {
//						index =  mViewFlipper.getChildCount();
//					}
//					
//					handleMessage(1);
//				}
//
//			});
//		 next_r.setOnClickListener(new View.OnClickListener() {
//				public void onClick(View view) {
//					mViewFlipper.showNext();
//					// 调用下面的函数将会循环显示mViewFlipper内的所有View。
//					// mViewFlipper.startFlipping();
//					if (index < mViewFlipper.getChildCount()) {
//						index++;
//					} else {
////						editor.putBoolean(CommonData.TEACHER, true).commit();
//						mSess.mPrefMeme.teacher.setVal(true).commit();
//						TutorialTeachActivity.this.finish();
//						System.gc();
//						Intent helpIntent = new Intent(TutorialTeachActivity.this,
//								TuiXinMainTab.class);
//						startActivity(helpIntent);
//						if (Utils.debug)
//							Log.i(TAG, ">>>>>>>>>>>>>>>>>");
//					}
//					handleMessage(1);
//				}
//
//			});
	}
	
	   final Handler uiUpdateHandler = new Handler();

	   final Runnable updateResults = new Runnable() {
	        public void run() {
	        	updateUI();
	        }
	    };
	    
	private void updateUI(){
		switch(index){
		case 1:
			point1.setImageResource(R.drawable.teach_click_point);
			point2.setImageResource(R.drawable.teach_unclick_point);
			point3.setImageResource(R.drawable.teach_unclick_point);
			point4.setImageResource(R.drawable.teach_unclick_point);
//			point5.setImageResource(R.drawable.teach_unclick_point);
			turnButton.setVisibility(View.GONE);
//			next_l.setVisibility(View.GONE);
			break;
		case 2:
			point1.setImageResource(R.drawable.teach_unclick_point);
			point2.setImageResource(R.drawable.teach_click_point);
			point3.setImageResource(R.drawable.teach_unclick_point);
			point4.setImageResource(R.drawable.teach_unclick_point);
//			point5.setImageResource(R.drawable.teach_unclick_point);
			turnButton.setVisibility(View.GONE);
//			next_l.setVisibility(View.VISIBLE);
			break;
		case 3:
			point1.setImageResource(R.drawable.teach_unclick_point);
			point2.setImageResource(R.drawable.teach_unclick_point);
			point3.setImageResource(R.drawable.teach_click_point);
			point4.setImageResource(R.drawable.teach_unclick_point);
//			point5.setImageResource(R.drawable.teach_unclick_point);
			turnButton.setVisibility(View.GONE);
//			next_l.setVisibility(View.VISIBLE);
			break;
		case 4:
			point1.setImageResource(R.drawable.teach_unclick_point);
			point2.setImageResource(R.drawable.teach_unclick_point);
			point3.setImageResource(R.drawable.teach_unclick_point);
			point4.setImageResource(R.drawable.teach_click_point);
//			point5.setImageResource(R.drawable.teach_unclick_point);
			turnButton.setVisibility(View.VISIBLE);
//			next_l.setVisibility(View.VISIBLE);
			break;
//		case 5:
//			point1.setImageResource(R.drawable.teach_unclick_point);
//			point2.setImageResource(R.drawable.teach_unclick_point);
//			point3.setImageResource(R.drawable.teach_unclick_point);
//			point4.setImageResource(R.drawable.teach_unclick_point);
////			point5.setImageResource(R.drawable.teach_click_point);
//			turnButton.setVisibility(View.VISIBLE);
//			break;
			
		}
		
	}
	protected void onDestroy()
	{
		TxData.popActivityRemove(this);
	    super.onDestroy();

	}
	
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	public boolean onTouchEvent(MotionEvent event) {
		if (mGestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (Utils.debug)
			Log.d(TAG, "teach...onFling...");
		
		if (e1.getX() > e2.getX()) {// move to left
			if(index<mViewFlipper.getChildCount()){
			  mViewFlipper.showNext();
			  index++;
			}else{
//				editor.putBoolean(CommonData.TEACHER, true).commit();
				mSess.mPrefMeme.teacher.setVal(true).commit();
				
				TutorialTeachActivity.this.finish();
				System.gc();
				Intent helpIntent = new Intent(TutorialTeachActivity.this,
						TuiXinMainTab.class);
				startActivity(helpIntent);
				if (Utils.debug)
					Log.i(TAG, ">>>>>>>>>>>>>>>>>");
			}
//			System.out.println(mViewFlipper.getChildCount()+"index="+index);
			
			handleMessage(1);
		} else if (e1.getX() < e2.getX()) {
			if(index>1){
			mViewFlipper.setInAnimation(getApplicationContext(),
					R.anim.slide_right_in);
			mViewFlipper.setOutAnimation(getApplicationContext(),
					R.anim.slide_right_out);
			mViewFlipper.showPrevious();
			mViewFlipper.setInAnimation(getApplicationContext(),
					R.anim.push_left_in);
			mViewFlipper.setOutAnimation(getApplicationContext(),
					R.anim.push_left_out);
			index--;
			
			}else{
				return false;
			}
			handleMessage(1);
		} else {
			return false;
		}
		return true;
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		//
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		//
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		//
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		//
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		//

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		//
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		//

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		//
		return false;
	}
	private void handleMessage(int what){
	    uiUpdateHandler.post(updateResults); //高速UI线程可以更新结果了
	}
	
//	private Handler mHandler = new Handler() {
//		public void handleMessage(Message msg) {
//			switch (msg.what) {
//			case 1:
//				switch(index){
//				case 1:
//					point1.setImageResource(R.drawable.teach_click_point);
//					point2.setImageResource(R.drawable.teach_unclick_point);
//					point3.setImageResource(R.drawable.teach_unclick_point);
//					point4.setImageResource(R.drawable.teach_unclick_point);
//					break;
//				case 2:
//					point1.setImageResource(R.drawable.teach_unclick_point);
//					point2.setImageResource(R.drawable.teach_click_point);
//					point3.setImageResource(R.drawable.teach_unclick_point);
//					point4.setImageResource(R.drawable.teach_unclick_point);
//					break;
//				case 3:
//					point1.setImageResource(R.drawable.teach_unclick_point);
//					point2.setImageResource(R.drawable.teach_unclick_point);
//					point3.setImageResource(R.drawable.teach_click_point);
//					point4.setImageResource(R.drawable.teach_unclick_point);
//					break;
//				case 4:
//					point1.setImageResource(R.drawable.teach_unclick_point);
//					point2.setImageResource(R.drawable.teach_unclick_point);
//					point3.setImageResource(R.drawable.teach_unclick_point);
//					point4.setImageResource(R.drawable.teach_click_point);
//					break;
//				}
//				break;
//			
//			}
//		}
//	};
}
