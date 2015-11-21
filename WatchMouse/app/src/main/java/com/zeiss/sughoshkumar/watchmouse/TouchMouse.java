package com.zeiss.sughoshkumar.watchmouse;

import android.view.MotionEvent;
import android.view.View;
import com.zeiss.sughoshkumar.senderobject.SenderObject;

public class TouchMouse implements View.OnTouchListener {

    // Variables for the driver
    private float initX, initY, sendX, sendY;
    private static final float SCROLL_THRESHOLD = 0.2f;
    private static long mDeBounce;
    private static boolean mIsScrolling;

    /**
     * Constructor
     */
    public TouchMouse(){
        mIsScrolling = false;
        mDeBounce = 0;
    }


    /**
     * Super touch event - driver's implementation
     * @param v view
     * @param event event value
     * @return true of event is consumed
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        WearMainActivity.thread.onPause();
        switch (event.getAction()){
           case MotionEvent.ACTION_DOWN:
               WearMainActivity.thread.onPause();
               initX = event.getX();
               initY = event.getY();
               mDeBounce = event.getEventTime();
               return true;

           case MotionEvent.ACTION_MOVE:
               WearMainActivity.thread.onPause();
               sendX = initX - event.getX();
               sendY = initY - event.getY();
               if ( Math.abs(sendX) > SCROLL_THRESHOLD || Math.abs(sendY) > SCROLL_THRESHOLD){
                   mIsScrolling = true;
                   sendCurrentPosition();
               }

               initX = event.getX();
               initY = event.getY();
               return true;
           case MotionEvent.ACTION_UP:
               if (((event.getEventTime() - mDeBounce) < 400 && event.getPointerCount() == 1) && (!mIsScrolling)) {
                   onSingleTap();
                   WearMainActivity.thread.onResume();
                   sendX = 0;
                   sendY = 0;
                   initY = 0;
                   initX = 0;
                   return true;
               }
               WearMainActivity.thread.onResume();
               mIsScrolling = false;
               sendX = 0;
               sendY = 0;
               initY = 0;
               initX = 0;
               return true;

           default:
               return false;
       }
    }

    /**
     * Zeroing the position
     */
    @SuppressWarnings("unused")
    private void onZeroingPosition() {
        SenderObject senderObject = new SenderObject(0, 0, 1, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

    /**
     * Send registered tap
     */
    private void onSingleTap() {
        SenderObject senderObject = new SenderObject(0, 0, 2, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

    /**
     * Send the generated X, Y position
     */
    @SuppressWarnings("SuspiciousNameCombination")
    private void sendCurrentPosition() {
        SenderObject senderObject = new SenderObject(sendY, sendX, 1, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

}
