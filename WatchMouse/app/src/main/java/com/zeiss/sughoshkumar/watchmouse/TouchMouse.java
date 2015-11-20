package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.util.List;

/**
 * Created by sughoshkumar on 26/08/15.
 */
@SuppressWarnings("SuspiciousNameCombination")
public class TouchMouse implements View.OnTouchListener {

    private float initX, initY, sendX, sendY;
    private static final float SCROLL_THRESHOLD = 0.2f;
    private static long mDeBounce;
    private static boolean mIsScrolling;

    public TouchMouse(){
        mIsScrolling = false;
        mDeBounce = 0;
    }


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
                   try {
                       sendCurrentPosition();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
               }

               initX = event.getX();
               initY = event.getY();
               return true;
           case MotionEvent.ACTION_UP:
               if (((event.getEventTime() - mDeBounce) < 400 && event.getPointerCount() == 1) && (!mIsScrolling)) {
                   try {
                       onSingleTap();
                       WearMainActivity.thread.onResume();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
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

    private void onZeroingPosition() throws IOException {
        SenderObject senderObject = new SenderObject(0, 0, 1, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

    private void onSingleTap() throws IOException {
        SenderObject senderObject = new SenderObject(0, 0, 2, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

    private void sendCurrentPosition() throws IOException {
        SenderObject senderObject = new SenderObject(sendY, sendX, 1, SenderObject.TOUCH_MODALITY);
        new UDPClient(senderObject).execute();
    }

}
