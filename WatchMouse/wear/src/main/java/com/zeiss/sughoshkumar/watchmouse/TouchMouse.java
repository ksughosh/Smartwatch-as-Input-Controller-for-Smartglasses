package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by sughoshkumar on 26/08/15.
 */
public class TouchMouse implements View.OnTouchListener {

    private int initX, initY, sendX, sendY;
    private static final int WATCH_RESOLUTION = 280;
    private static int WIDTH_RESOLUTION = 100;
    private static int HEIGHT_RESOLUTION = 100;
    private static final int CONNECTION_TIMEOUT_MS = 0;
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
        System.out.println("Sent to DataLayer");
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
       switch (event.getAction()){
           case MotionEvent.ACTION_DOWN:
               initX = (int)(event.getX());
               initY = (int)(event.getY());
               mDeBounce = event.getEventTime();
               return true;

           case MotionEvent.ACTION_MOVE:
               sendX = initX - (int) (event.getX());
               sendY = initY - (int) (event.getY());
               if ( sendX > SCROLL_THRESHOLD || sendY > SCROLL_THRESHOLD){
                   mIsScrolling = true;
               }
               try {
                   sendCurrentPosition();
               } catch (IOException e) {
                   e.printStackTrace();
               }
               initX = (int) (event.getX());
               initY = (int) (event.getY());
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

    private void onSingleTap() throws IOException {
        SenderObject senderObject = new SenderObject(sendX, sendY, 2);
        sendToast("XY", SenderObject.toBytes(senderObject));
    }

    private void sendCurrentPosition() throws IOException {
        SenderObject senderObject = new SenderObject(sendX, sendY,1);
        sendToast("XY", SenderObject.toBytes(senderObject));
    }

    private int calibrateValueX(float x){
        return (int)((WIDTH_RESOLUTION/WATCH_RESOLUTION) * x);
    }

    private int calibrateValueY(float y){
        return (int) ((HEIGHT_RESOLUTION/WATCH_RESOLUTION) * y);
    }
}
