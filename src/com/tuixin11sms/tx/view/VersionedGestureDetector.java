package com.tuixin11sms.tx.view;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import com.tuixin11sms.tx.utils.Utils;

public abstract class VersionedGestureDetector {
    private static final String TAG = "VersionedGestureDetector";
    
    OnGestureListener mListener;
    
    public static VersionedGestureDetector newInstance(Context context,
            OnGestureListener listener) {
        final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
        VersionedGestureDetector detector = null;
        if (sdkVersion < Build.VERSION_CODES.ECLAIR) {
            detector = new CupcakeDetector();
        } else if (sdkVersion < Build.VERSION_CODES.FROYO) {
            detector = new EclairDetector();
        } else {
            detector = new FroyoDetector(context);
        }
        
        if(Utils.debug)Log.d(TAG, "Created new " + detector.getClass());
        detector.mListener = listener;
        
        return detector;
    }
    
    public abstract boolean onTouchEvent(MotionEvent ev);
    
    public interface OnGestureListener {
        public void onDrag(float dx, float dy);
        public void onScale(float scaleFactor);
    }

    private static class CupcakeDetector extends VersionedGestureDetector {
        float mLastTouchX;
        float mLastTouchY;
        
        float getActiveX(MotionEvent ev) {
            return ev.getX();
        }

        float getActiveY(MotionEvent ev) {
            return ev.getY();
        }
        
        boolean shouldDrag() {
            return true;
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLastTouchX = getActiveX(ev);
                mLastTouchY = getActiveY(ev);
                break;
            }
            case MotionEvent.ACTION_MOVE: {
                final float x = getActiveX(ev);
                final float y = getActiveY(ev);
                
                if (shouldDrag()) {
                    mListener.onDrag(x - mLastTouchX, y - mLastTouchY);
                }
                
                mLastTouchX = x;
                mLastTouchY = y;
                break;
            }
            }
            return true;
        }
    }
    
    private static class EclairDetector extends CupcakeDetector {
        private static final int INVALID_POINTER_ID = -1;
        private int mActivePointerId = INVALID_POINTER_ID;
        private int mActivePointerIndex = 0;

        @Override
        float getActiveX(MotionEvent ev) {
            return ev.getX(mActivePointerIndex);
        }

        @Override
        float getActiveY(MotionEvent ev) {
            return ev.getY(mActivePointerIndex);
        }

        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            final int action = ev.getAction();
            switch (action & MotionEvent.ACTION_MASK) {
            case MotionEvent.ACTION_DOWN:
                mActivePointerId = ev.getPointerId(0);
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                mActivePointerId = INVALID_POINTER_ID;
                break;
            case MotionEvent.ACTION_POINTER_UP:
                final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
                final int pointerId = ev.getPointerId(pointerIndex);
                if (pointerId == mActivePointerId) {
                    // This was our active pointer going up. Choose a new
                    // active pointer and adjust accordingly.
                    final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
                    mActivePointerId = ev.getPointerId(newPointerIndex);
                    mLastTouchX = ev.getX(newPointerIndex);
                    mLastTouchY = ev.getY(newPointerIndex);
                }
                break;
            }

            mActivePointerIndex = ev.findPointerIndex(mActivePointerId);
            return super.onTouchEvent(ev);
        }
    }
    
    private static class FroyoDetector extends EclairDetector {
        private ScaleGestureDetector mDetector;
        
        public FroyoDetector(Context context) {
            mDetector = new ScaleGestureDetector(context,
                    new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override public boolean onScale(ScaleGestureDetector detector) {
                    mListener.onScale(detector.getScaleFactor());
                    return true;
                }
            });
        }
        
        @Override
        boolean shouldDrag() {
            return !mDetector.isInProgress();
        }
        
        @Override
        public boolean onTouchEvent(MotionEvent ev) {
            mDetector.onTouchEvent(ev);
            return super.onTouchEvent(ev);
        }
    }
}
