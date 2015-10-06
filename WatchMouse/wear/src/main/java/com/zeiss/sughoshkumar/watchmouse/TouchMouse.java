package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
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
public class TouchMouse implements View.OnTouchListener {

    private float initX, initY, sendX, sendY;
    private static final int WATCH_RESOLUTION = 320;
//    private static int WIDTH_RESOLUTION = 100;
//    private static int HEIGHT_RESOLUTION = 100;
    private static final int SCROLL_THRESHOLD = 50;
    private static String nodeId;
    GoogleApiClient client;
    private static long mDeBounce = 0;
    private static boolean mIsScrolling = false;
    private Context context;

    public TouchMouse(Context context){
        this.context = context;
        client = getGoogleApiClient(this.context);
        retrieveDeviceNode();
    }


    /**
     * @param context : creates a new GoggleAPI client for this context.
     * @return GoogleAPI client.
     */

    private GoogleApiClient getGoogleApiClient(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    /**
     * Retrieves the devices connected to the shared
     * Data layer.
     */

    private void retrieveDeviceNode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.connect();
                NodeApi.GetConnectedNodesResult results = Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = results.getNodes();
                if (nodes.size() > 0 ){
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }

    /**
     *
     * @param message : message path to be sent to the data layer
     * @param data : data to be sent in bytes[] to data layer
     */
    private final void sendToast(final String message, final byte[] data) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                }
            }).start();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       switch (event.getAction()){
           case MotionEvent.ACTION_DOWN:
               initX = event.getX();
               initY = event.getY();
               mDeBounce = event.getEventTime();
               return true;

           case MotionEvent.ACTION_MOVE:
               sendX = initX - event.getX();
               sendY = initY - event.getY();
               if ( sendX > SCROLL_THRESHOLD || sendY > SCROLL_THRESHOLD){
                   mIsScrolling = true;
               }
               try {
                   sendCurrentPosition();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               initX = event.getX();
               initY = event.getY();
               return true;
           case MotionEvent.ACTION_UP:
               System.out.println("difference " + (event.getEventTime() - mDeBounce) + " isScrolling " + mIsScrolling + " pointer " + event.getPointerCount());
               if (((event.getEventTime() - mDeBounce) < 400 && event.getPointerCount() == 1) && (!mIsScrolling)) {
                   try {
                       onSingleTap();
                   } catch (IOException e) {
                       e.printStackTrace();
                   }
                   sendX = 0;
                   sendY = 0;
                   initY = 0;
                   initX = 0;
                   return true;
               }
               try {
                   onZeroingPosition();
               } catch (IOException e) {
                   e.printStackTrace();
               }
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
        sendToast("STOP", SenderObject.toBytes(senderObject));
    }

    private void onSingleTap() throws IOException {
        SenderObject senderObject = new SenderObject(sendY, sendX * -1, 2, SenderObject.TOUCH_MODALITY);
        sendToast("XY", SenderObject.toBytes(senderObject));
    }

    private void sendCurrentPosition() throws IOException {
        SenderObject senderObject = new SenderObject(sendY, sendX * -1, 1, SenderObject.TOUCH_MODALITY);
        sendToast("XY", SenderObject.toBytes(senderObject));
    }

//    private float calibrateValueX(float x){
//        return (WIDTH_RESOLUTION/WATCH_RESOLUTION) * Y;
//    }
//
//    private float calibrateValueY(float y){
//        return (HEIGHT_RESOLUTION/WATCH_RESOLUTION) * y;
//    }
}
