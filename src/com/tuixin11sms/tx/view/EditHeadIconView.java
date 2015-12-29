package com.tuixin11sms.tx.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

import com.tuixin11sms.tx.R;

public class EditHeadIconView extends View implements OnTouchListener, OnGestureListener{
	private GestureDetector mGestureDetector;
	private Bitmap arrow_ud_img,arrow_lr_img;
	private Bitmap mImage;
    int screenW,screenH;
    private int img_x,img_y;
    private int edit_rect_x,edit_rect_y,edit_rect_wh;
    private Paint paint = new Paint();
    Matrix matrix = new Matrix(); 
    
    private float down_x,down_y,down_wh;
    private int edit_state;//编辑状态
    private static final int EDIT_SCALE = 1;//缩放状态
    private static final int EDIT_MOVE = 3;//移动状态
    private float down_rect = 50;
    private int rect_min_wh = 50;
    private int corner_driection;//四个角
    private static final int CORNER_UP_RIGHT = 1;
    private static final int CORNER_UP_LEFT = 2;
    private static final int CORNER_DOWN_RIGHT = 3;
    private static final int CORNER_DOWN_LEFT = 4;
    public EditHeadIconView(Context context) {
        this(context, null, 0);
    }
    
    public EditHeadIconView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }
    
    public EditHeadIconView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);   
        if(isInEditMode()){
        	return;
        }
        mGestureDetector = new GestureDetector(this);
        mGestureDetector.setIsLongpressEnabled(true);
        arrow_ud_img = BitmapFactory.decodeResource(getResources(),R.drawable.headimg_edit_arrow_ud);
        arrow_lr_img = BitmapFactory.decodeResource(getResources(),R.drawable.headimg_edit_arrow_lr);
    }

    public void setInitPos(int screen_width,int screen_height){
 
    	if(mImage != null){
           screenW = screen_width;
           screenH = screen_height;
           paint.setAntiAlias(true);
           paint.setColor(0xfff5f5f5);
           paint.setStyle(Style.STROKE);
           paint.setStrokeWidth(2);
           edit_rect_wh = getWidth()>= getHeight()?getWidth()/2:getHeight()/2;
           edit_rect_x = (getWidth() - edit_rect_wh)/2;
           edit_rect_y = (getHeight() - edit_rect_wh)/2;
           matrix.postTranslate(100, 100);    
           invalidate();
    	}
    }
    public void setImg(Bitmap image){
    	mImage=image;
    }
    public Bitmap getImg(){
    	return mImage;
    }
    public void initHeadImgData(){
    	img_x = (getWidth() - mImage.getWidth())/2;
    	img_y = (getHeight() - mImage.getHeight())/2;
    }
    @Override
    
    public boolean onTouchEvent(MotionEvent event) {
    	if(event.getAction() == MotionEvent.ACTION_UP){
    		edit_state = 0;
    		corner_driection = 0;
    		invalidate();
    		
    	}
        return mGestureDetector.onTouchEvent(event);    
    } 
    
    @Override
    public void onDraw(Canvas canvas) {
    		super.onDraw(canvas);       
    		if(mImage != null){
    			ColLogic();
	            canvas.save();
	            paint.setColor(0xfff5f5f5);
//	            canvas.rotate(rotate_angle, mImage.getWidth()/2, mImage.getHeight()/2);
	            //画图片
	            canvas.drawBitmap(mImage,img_x,img_y, paint);
	            drawShadow(canvas);
	            drawArrow(canvas);
	            //画矩形编辑框
	            paint.setColor(0xff8ebec8);
	            canvas.drawRect(edit_rect_x, edit_rect_y, edit_rect_x+edit_rect_wh, 
	            		       edit_rect_y+edit_rect_wh, paint);
	            
	            canvas.restore();
    		}
    	
        
    }
    //画四个箭头
    public void drawArrow(Canvas canvas){
    	if(edit_state == EDIT_SCALE){
    	  canvas.drawBitmap(arrow_ud_img,edit_rect_x+(edit_rect_wh - arrow_ud_img.getWidth())/2,
    			  edit_rect_y+ - arrow_ud_img.getHeight()/2, paint);
    	  canvas.drawBitmap(arrow_ud_img,edit_rect_x+(edit_rect_wh - arrow_ud_img.getWidth())/2,
    			  edit_rect_y+edit_rect_wh- arrow_ud_img.getHeight()/2, paint);
    	  canvas.drawBitmap(arrow_lr_img,edit_rect_x- arrow_lr_img.getWidth()/2,
    			  edit_rect_y+(edit_rect_wh - arrow_lr_img.getHeight())/2, paint);
    	  canvas.drawBitmap(arrow_lr_img,edit_rect_x + edit_rect_wh - arrow_lr_img.getWidth()/2,
    			  edit_rect_y+(edit_rect_wh - arrow_lr_img.getHeight())/2, paint);
    	}
    }
    //画四周的阴影
    public void drawShadow(Canvas canvas){
    	
    	paint.setColor(Color.BLACK);
        paint.setStyle(Style.FILL);//实心矩形框  
        paint.setAlpha(200);
    	canvas.drawRect( edit_rect_x, img_y, edit_rect_x+edit_rect_wh,edit_rect_y, paint);
    	canvas.drawRect( edit_rect_x, edit_rect_y+edit_rect_wh, edit_rect_x+edit_rect_wh,img_y+mImage.getHeight(), paint);
    	canvas.drawRect( img_x, img_y, edit_rect_x,img_y+mImage.getHeight(), paint);
    	canvas.drawRect( edit_rect_x+edit_rect_wh, img_y, img_x+mImage.getWidth(),img_y+mImage.getHeight(), paint);
    	paint.setAlpha(255);
    	paint.setStyle(Style.STROKE);//空心矩形框  
    	paint.setColor(Color.BLUE);
    }
    public boolean iscolEditRect(float x,float y){
    	 if(x>=edit_rect_x-down_rect&&x<=edit_rect_x+edit_rect_wh+down_rect&&
				 y>=edit_rect_y-down_rect&&y<=edit_rect_y+edit_rect_wh+down_rect){//判断在编辑框范围之内
    		 return true;
    	 }
    	 return false;
    }
	@Override
	public boolean onTouch(View v, MotionEvent event) {
		//
		
		return false;
	}
	// 用户轻触触摸屏，由1个MotionEvent ACTION_DOWN触发
	@Override
	public boolean onDown(MotionEvent e) {
		//
		 edit_state = 0;
		 down_x = edit_rect_x;
		 down_y = edit_rect_y;
		 down_wh = edit_rect_wh;
		 if(iscolEditRect(e.getX(),e.getY())){//判断在编辑框范围之内
			if(isScroll(e.getX(),e.getY())){//判断缩放编辑框
				edit_state = EDIT_SCALE;
			}
			
			else {//移动编辑框
				edit_state = EDIT_MOVE;
			}
			//判断方向
			getCornerDirection(e.getX(),e.getY());
		 }
		 if(edit_state > 0){
			   invalidate();
		 }
	
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		//
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		//
		
	}
	// 用户按下触摸屏，并拖动，由1个MotionEvent ACTION_DOWN, 多个ACTION_MOVE触发 
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		//
		switch(edit_state){
		  case EDIT_SCALE:
				float distance = 0;
				switch(corner_driection){
				  case CORNER_UP_RIGHT:
					 
					  if(Math.abs(e2.getX() - e1.getX())>=Math.abs(e2.getY() - e1.getY())){
							distance = e2.getX() - e1.getX();
						}
						else {
							distance = e1.getY() - e2.getY();
						}
					 edit_rect_y = (int)(down_y - distance);
					 edit_rect_wh = (int)(down_wh + distance);
				  break;
				  case CORNER_UP_LEFT:
					  if(Math.abs(e2.getX() - e1.getX())>=Math.abs(e2.getY() - e1.getY())){
							distance = e1.getX() - e2.getX();
						}
						else {
							distance = e1.getY() - e2.getY();
						}
					  edit_rect_x = (int)(down_x - distance/2);
					  edit_rect_y = (int)(down_y - distance/2);
					  edit_rect_wh = (int)(down_wh + distance/2);
			      break;
				  case CORNER_DOWN_RIGHT:
					  if(Math.abs(e2.getX() - e1.getX())>=Math.abs(e2.getY() - e1.getY())){
							distance = e2.getX() - e1.getX();
						}
						else {
							distance = e2.getY() - e1.getY();
						}
					  edit_rect_wh = (int)(down_wh + distance/2);
				  break;
				  case CORNER_DOWN_LEFT:
					  if(Math.abs(e2.getX() - e1.getX())>=Math.abs(e2.getY() - e1.getY())){
							distance = e1.getX() - e2.getX();
						}
						else {
							distance = e2.getY() - e1.getY();
						}
					  edit_rect_x = (int)(down_x - distance/2);
					  edit_rect_wh = (int)(down_wh + distance/2);
				  break;
				}
				
		  break;
		  case EDIT_MOVE:
			  edit_rect_x = (int)(down_x + (e2.getX() - e1.getX()));
			  edit_rect_y = (int)(down_y + (e2.getY() - e1.getY()));
			   
		  break;
		}
		if(edit_state > 0){
		   invalidate();
		}
		return false;
	}
	/*   
     * 用户轻触触摸屏，尚未松开或拖动，由一个1个MotionEvent ACTION_DOWN触发   
     * 注意和onDown()的区别，强调的是没有松开或者拖动的状态   
     */  
	@Override
	public void onShowPress(MotionEvent e) {
		//
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		//
		return false;
	}
	//判断是否缩放
  boolean isScroll(float x,float y){
	  if(y <= down_y+down_rect){//上
	  	return true;
	  }
	  if(y >= down_y + edit_rect_wh - down_rect){//下

	  	return true;
	  }
	  if(x <= down_x + down_rect){//左

	  	return true;
	  }
	  if(x >= down_x + edit_rect_wh - down_rect){//右

	  	return true;
	  }
	return false;
  }
  //判断点击区域在哪个角区域
  public void getCornerDirection(float x,float y){
	  if(y <= edit_rect_y+edit_rect_wh/2 && x <= edit_rect_x+edit_rect_wh/2){//上左
		  corner_driection = CORNER_UP_LEFT;
	  }
	  else if(y <= edit_rect_y+edit_rect_wh/2 && x >= edit_rect_x+edit_rect_wh/2){//上右
		  corner_driection = CORNER_UP_RIGHT;
	  }
	  else if(y >= edit_rect_y+edit_rect_wh/2 && x <= edit_rect_x+edit_rect_wh/2){//下 左
		  corner_driection = CORNER_DOWN_LEFT;
	  }
      else if(y >= edit_rect_y+edit_rect_wh/2 && x >= edit_rect_x+edit_rect_wh/2){//下右
    	  corner_driection = CORNER_DOWN_RIGHT;
	  }
  }
  //判断编辑框和控件碰撞逻辑
  public void ColLogic(){
	initHeadImgData();
	if(edit_rect_x <= 0){
		edit_rect_x = 0;
		
	} 
	if(edit_rect_y <= 0){
		edit_rect_y = 0;
	} 
	if(edit_rect_x + edit_rect_wh>= getWidth()){
		edit_rect_x = getWidth() - edit_rect_wh;
	} 
	if(edit_rect_y + edit_rect_wh>= getHeight()){
		edit_rect_y = getHeight() - edit_rect_wh;
	}
	int wh = mImage.getWidth()>=mImage.getHeight()?mImage.getHeight():mImage.getWidth();
	if(edit_rect_wh >= wh){
		edit_rect_wh = wh;
	} 
	
	if(edit_rect_wh <= rect_min_wh){
		edit_rect_wh = rect_min_wh;
	}
	if(edit_rect_x <= img_x){
		edit_rect_x = img_x;
	}
	if(edit_rect_y <= img_y){
		edit_rect_y = img_y;
	}
	if(edit_rect_x + edit_rect_wh >= img_x + mImage.getWidth()){
		edit_rect_x = img_x + mImage.getWidth() - edit_rect_wh;
	}
	if(edit_rect_y + edit_rect_wh >= img_y + mImage.getHeight()){
		edit_rect_y = img_y + mImage.getHeight() - edit_rect_wh;
	}
  }
  public Bitmap getEditImg(){
	  Bitmap EditImg = null;
	  if(mImage != null){
	     EditImg = Bitmap.createBitmap(mImage, edit_rect_x-img_x, edit_rect_y-img_y,edit_rect_wh, edit_rect_wh);
	     
	  }
	  return EditImg;
  }
  public void removeBigImg(){
	  if(mImage != null){
		 mImage.isRecycled();
	     mImage = null;
	  }
  }
  public int getRectWh(){
	  return edit_rect_wh;
  }
}
