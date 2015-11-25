package com.thesis.sughoshkumar.fittsexperiment;

import android.graphics.Point;
import android.os.AsyncTask;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class UDPServer extends AsyncTask<Void, Void, Void> {

    // Defining the class parameters.
    private static final int WATCH_RES = 320;
    private static final int MAX_DATAGRAM_PACKET_SIZE = 128;
    private static int SCREEN_X;
    private static int SCREEN_Y;
    private static boolean keepRunning;
    private int port;
    private byte[] receiveData;
    private DatagramSocket datagramSocket;
    private FittsInputInjector iView;
    private static int switchModality = 4;
    float x,y;


    /**
     * Constructor
     * @param p port number
     * @param childView view to whom the values are injected
     * @param dimension dimension of the screen
     */
    public UDPServer(int p, FittsInputInjector childView, Point dimension) {
        port = p;
        receiveData = new byte[MAX_DATAGRAM_PACKET_SIZE];
        keepRunning = true;
        iView = childView;
        SCREEN_X = dimension.x;
        SCREEN_Y = dimension.y;
        x = y = 0.1f;
    }

    /**
     * Mehthod to kill the server
     */
    public static void kill() {
        keepRunning = false;
    }

    /**
     * Input to output pixel converter for CD = 1
     * @param x value of X
     * @return converted value of X
     */
    private float pixelConverterX(float x) {
        return SCREEN_X / WATCH_RES * x;
    }

    /**
     * Input to output pixel converter for CD = 1
     * @param y value of Y
     * @return converted value of Y
     */
    private float pixelConverterY(float y) {
        return SCREEN_Y / WATCH_RES * y;
    }


    /**
     * Background task - starting the server and listening.
     * @param params Void
     * @return null
     */
    @SuppressWarnings("ResourceType")
    @Override
    protected Void doInBackground(Void... params) {
        try {
            datagramSocket = new DatagramSocket(port);
            System.out.println("Server Started");
            while (keepRunning) {
                DatagramPacket datagramPacket = new DatagramPacket(receiveData, receiveData.length);
                datagramSocket.receive(datagramPacket);
                SenderObject objectReceived = SenderObject.parseFrom(receiveData);
                int type = objectReceived.getType();

                x = objectReceived.getX();
                y = objectReceived.getY();
                int modality = objectReceived.getModality();
                System.out.println("UDP " + objectReceived.toString());
                if (switchModality == 0){
                    if (type == 1) {
                        iView.setIsScrolling(true);
                        iView.setIsTapped(false);
                        iView.mouseMove(-y * getCD(modality), x * getCD(modality));
                    }
                    else if (type == 2) {
                        iView.setIsTapped(true);
                        iView.setIsScrolling(false);
                    }
                }
                else if (switchModality == 1){
                    if (type == 1) {
                        iView.setIsScrolling(true);
                        iView.setIsTapped(false);
                        iView.mouseMove(pixelConverterX(x) * getCD(modality), pixelConverterY(y) * getCD(modality));
                    }
                    else{
                        iView.setIsTapped(true);
                        iView.setIsScrolling(false);
                    }
                }
                else {
                        if (type == 1) {
                            iView.setIsScrolling(true);
                            iView.setIsTapped(false);
                            iView.mouseMove(pixelConverterX(x) * getCD(modality), pixelConverterY(y) * getCD(modality));
                        } else if (type == 2) {
                            iView.setIsTapped(true);
                            iView.setIsScrolling(false);
                        }
                    }
            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        if (datagramSocket != null) {
            datagramSocket.close();
        }
        return null;
    }

    /**
     * Get the CD multiplier
     * @param modality type of modality
     * @return multiplier value
     */
    private float getCD(int modality){

        /**
         * 0 : only touch
         * 1 : only gesture
         * 2 : symphony with touch fine and gesture coarse
         * 3 : symphony with gesture fine and touch coarse
         * default : symphony with no coarse and fine
         */

        switch(switchModality){
            case 0:
                if (modality == SenderObject.GESTURE_MODALITY)
                    return 0;
                else
                    return 1;
            case 1:
                if (modality == SenderObject.TOUCH_MODALITY)
                    return 0;
                else
                if (!iView.isFinePointing(x,y))
                    return 1f;
                else
                    return 0.3f;
            case 2 :
                if (modality == SenderObject.GESTURE_MODALITY) {
                    return 1.3f;
                } else {
                    if (iView.isFinePointing(x,y))
                        return 0.2f;
                    else
                        return 0.1f;
                }
            default:
                return 1;
        }
    }

    /**
     * Get string representation of the experiment.
     * @return string representation of the modality
     */
    public static String modalityToString(){
        switch (switchModality){
            case 0: return "Touch";
            case 1: return "Gesture";
            case 2: return "Symphony";
            default: return "BaseController";
        }
    }

    /**
     * Get the user ID and check file redundancy
     * @return user id
     */
    public static int getUserId(){
        return 0;
    }
}