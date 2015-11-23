package com.zeiss.sughoshkumar.watchcontroller;

import android.content.Context;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

import de.mycable.Argus;

/**
 * Created by sughoshkumar on 13/08/15.
 * @Copyright -Protected.
 */
public class OnSwipeTouchListener implements View.OnTouchListener {

    // Define class properties
    private float baseX, baseY,xPrev, yPrev, pixY, pixX, prevY, diffX, diffY;
    boolean swipeUp, swipeDown, swipeRight, swipeLeft;
    private Context context;
    private String nodeId;
    private static long mDeBounce = 0;
    private static boolean mIsScrolling = false;
    private static boolean mDoubleIsScrolling = false;
    private static boolean isNodeFound = false;
    GoogleApiClient client;
    private boolean onAwake;

    // Set constants
    private static final int SWIPE_THRESHOLD = 100;
    private static final float RESOLUTION_X = 864.0f/320.f;
    private static final float RESOLUTION_Y = 500.0f/320.f;
    private static final int REVERSE_SCROLL_THRESHOLD = 10;
    private static final int LATENCY_CONTROL = 250;
    private static final int DIFFERENCE_THRESHOLD = 10;

    /**
     * @param con : The application context.
     */

    public OnSwipeTouchListener(Context con) {
        context = con;
        client = getGoogleApiClient(context);
        if (!isNodeFound) {
            retrieveDeviceNode();
        }
        onAwake = false;
        swipeUp = false;
        swipeDown = false;
        swipeLeft = false;
        swipeRight = false;
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
                if (nodes.size() > 0) {
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
        if(nodeId != null )
            isNodeFound = true;
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


    public void onSwipeLeft() {
        Argus.ArgusEvent downEvent = Argus.ArgusEvent.newBuilder()
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_SCROLL)
                .setScrolling(Argus.ArgusEvent.ScrollType.SCROLL_EVENT_LEFT)
                .setScrollAmount(1)
                .build();
         sendToast("LEFT", downEvent.toByteArray());
    }



    public void onSwipeRight() {
        Argus.ArgusEvent downEvent = Argus.ArgusEvent.newBuilder()
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_SCROLL)
                .setScrolling(Argus.ArgusEvent.ScrollType.SCROLL_EVENT_RIGHT)
                .setScrollAmount(1)
                .build();
         sendToast("RIGHT", downEvent.toByteArray());
    }


    public void onSwipeUp(){
        Argus.ArgusEvent downEvent = Argus.ArgusEvent.newBuilder()
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_SCROLL)
                .setScrolling(Argus.ArgusEvent.ScrollType.SCROLL_EVENT_UP)
                .setScrollAmount(1)
                .build();
        sendToast("UP", downEvent.toByteArray());
    }



    public void onSwipeDown(){
        Argus.ArgusEvent upEvent = Argus.ArgusEvent.newBuilder()
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_SCROLL)
                .setScrolling(Argus.ArgusEvent.ScrollType.SCROLL_EVENT_DOWN)
                .setScrollAmount(1)
                .build();
        sendToast("DOWN", upEvent.toByteArray());
    }

    /**
     *
     * @param value : float value
     * @return : convert to resolution
     */

    private int returnToHorizontalResolution(float value) {
        return Math.round(value * RESOLUTION_X);
    }

    /**
     *
     * @param value : float value
     * @return : convert to resolution
     */

    private int returnToVerticalResolution(float value){
        return Math.round(value * RESOLUTION_Y);
    }

    /**
     *
     * @param x : xPosition
     * @param y : yPosition
     */

    private void sendStopVideo(float x, float y){
        Argus.ArgusEvent stopVideo = Argus.ArgusEvent.newBuilder()
                .setPosX(returnToHorizontalResolution(x))
                .setPosY(returnToVerticalResolution(y))
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_STOPVIDEO)
                .build();
        sendToast("STOP VIDEO", stopVideo.toByteArray());
    }

    /**
     *
     * @param x : xPosition
     * @param y : yPosition
     */

    private void onSingleTap(float x, float y)
    {
        Argus.ArgusEvent onSingleTap = Argus.ArgusEvent.newBuilder()
                .setPosX(returnToHorizontalResolution(y))
                .setPosY(returnToVerticalResolution(x))
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_TOUCH)
                .build();
        sendToast("TAP", onSingleTap.toByteArray());
    }

    /**
     *
     * @param v : view.
     * @param event : event generated by the view.
     * @return : true if event is consumed else false.
     */

    public boolean onTouch(View v, MotionEvent event) {
        float curX;
        float curY;
        if (event.getPointerCount() == 2) {
            return (twoFingerScroll(event));
        }

        switch (event.getActionMasked()) {

            case MotionEvent.ACTION_DOWN:
                mDeBounce = event.getEventTime();
                baseX = event.getX();
                baseY = event.getY();
                return true;

            case MotionEvent.ACTION_MOVE:
                // starting to swipe
                curX = event.getX();
                curY = event.getY();

                diffY = curY - baseY;
                diffX = curX - baseX;
                if (swipeLeft) {
                    if (curX > xPrev) {
                        //re scrolling right
                        swipeLeft = false;
                        swipeRight = true;
                        baseX = xPrev;
                        diffX = curX - baseX;
                    }
                } else if (swipeRight) {
                    if (curX < xPrev) {
                        //re scrolling left
                        swipeLeft = true;
                        swipeRight = false;
                        baseX = xPrev;
                        diffX = curX - baseX;
                    }
                }
                if (swipeUp) {
                    if (curY > yPrev) {
                        //re scrolling downwards
                        swipeUp = false;
                        swipeDown = true;
                        baseY = yPrev;
                        diffY = curY - baseY;
                    }
                } else if (swipeDown) {
                    if (curY < yPrev) {
                        //re scrolling upwards
                        swipeUp = true;
                        swipeDown = false;
                        baseY = yPrev;
                        diffY = curY - baseY;
                    }
                } else {
                    if (curY < baseY) {
                        swipeUp = true;
                    } else if (curY > baseY) {
                        swipeDown = true;
                    }
                }
                xPrev = curX;
                yPrev = curY;


                // detect which directional swipe
                if (Math.abs(diffY) > Math.abs(diffX)) {
                    if (Math.abs(diffY) > SWIPE_THRESHOLD) {
                        // swiping vertically
                        mIsScrolling = true;
                        // if it is top to down then y value must increase
                        // if it bottom to top then y value must decrease

                        if (diffY > 0) {
                            if((diffY - pixY) < REVERSE_SCROLL_THRESHOLD) {
                                SystemClock.sleep(LATENCY_CONTROL);
                                onSwipeRight();
                                swipeDown = true;
                            }

                        } else {
                            if((pixY - diffY) < REVERSE_SCROLL_THRESHOLD) {
                                SystemClock.sleep(LATENCY_CONTROL);
                                onSwipeLeft();
                                swipeUp = true;
                            }
                        }
                    }
                } else if (Math.abs(diffX) > Math.abs(diffY)) {
                    if (Math.abs(diffX) > SWIPE_THRESHOLD) {
                        //swiping horizontally
                        mIsScrolling = true;
                        if (diffX > 0 ) {
                            if ((diffX - pixX) < REVERSE_SCROLL_THRESHOLD && (diffX - pixX) > DIFFERENCE_THRESHOLD) {
                                SystemClock.sleep(LATENCY_CONTROL);
                                onSwipeUp();
                                swipeRight = true;
                            }
                        } else {
                            if ((pixX - diffX) < REVERSE_SCROLL_THRESHOLD && (pixX - diffX) > DIFFERENCE_THRESHOLD) {
                                SystemClock.sleep(LATENCY_CONTROL);
                                onSwipeDown();
                                swipeLeft = true;
                            }
                        }
                    }
                }
                pixY = diffY;
                pixX = diffX;
                return true;

            case MotionEvent.ACTION_UP:
                if (((event.getEventTime() - mDeBounce) < 400 && event.getPointerCount() == 1) &&
                        (!mIsScrolling && !mDoubleIsScrolling)) {
                    onSingleTap(event.getX(), event.getY());
                    baseX = 0.0f;
                    diffX = 0.0f;
                    diffY = 0.0f;
                    baseY = 0.0f;
                    pixY = 0.0f;
                    pixX = 0.0f;
                    xPrev = 0.0f;
                    yPrev = 0.0f;

                    //As the event is consumed, return true
                    return true;
                }
                mIsScrolling = false;
                baseX = 0.0f;
                diffX = 0.0f;
                diffY = 0.0f;
                baseY = 0.0f;
                pixY = 0.0f;
                pixX = 0.0f;
                xPrev = 0.0f;
                yPrev = 0.0f;

                //As the event is consumed, return true
                return true;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    sendStopVideo(event.getX(), event.getY());
                }
                return true;


            default:
                return false;
        }
    }

    private boolean twoFingerScroll(MotionEvent m){
        int action = m.getActionMasked();
        float curY;
        switch (action)
        {
            case MotionEvent.ACTION_DOWN:
                curY = m.getY(1);
                return true;
            case MotionEvent.ACTION_UP:
                sendStopVideo(m.getX(), m.getY());
                mDoubleIsScrolling = false;
                prevY = 0.0f;
                curY = 0.0f;
                return true;
            case MotionEvent.ACTION_MOVE:
                curY = m.getY(1);
                if ((Math.abs(curY) - Math.abs(prevY)) > SWIPE_THRESHOLD){
                    mDoubleIsScrolling = true;
                }
                prevY = curY;
                return true;
            case MotionEvent.ACTION_POINTER_UP:
                sendStopVideo(m.getX(),m.getY());
                mDoubleIsScrolling = false;
                return true;
            default:
                return false;
        }
    }
}
