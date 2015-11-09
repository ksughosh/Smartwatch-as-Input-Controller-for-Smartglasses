package smartglass.zeiss.zoskris.fittsinputinjector;

import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by Sughosh Krishna Kumar on 05/10/15.
 * This is a work of thesis and therefore an academic work
 * This program is not to be used for any other purpose,
 * other than academics.
 */

public class UDPServer extends AsyncTask<Void, Void, Void> {
    private static final int WATCH_RES = 320;
    private static final int MAX_DATAGRAM_PACKET_SIZE = 128;
    private static int SCREEN_X;
    private static int SCREEN_Y;
    private static boolean keepRunning;
    private int port;
    private byte[] receiveData;
    private DatagramSocket datagramSocket;
    private FittsInjectView iView;
    private final int switchModality = 0;
    private boolean isInit;
    private double time;


    /**
     *
     * @param p port number
     * @param childView the view to which values are injected
     * @param dimension to get the screen dimension for computations
     */

    public UDPServer(int p, FittsInjectView childView, Point dimension) {
        port = p;
        receiveData = new byte[MAX_DATAGRAM_PACKET_SIZE];
        keepRunning = true;
        iView = childView;
        SCREEN_X = dimension.x;
        SCREEN_Y = dimension.y;
        isInit = false;
        time = 0;
    }

    /**
     * kill the UDP server
     */

    public static void kill() {
        keepRunning = false;
    }

    /**
     * @param x current value
     * @return float resolution dependent value
     */

    private float pixelConverterX(float x) {
        return SCREEN_X / WATCH_RES * x;
    }

    /**
     * @param y Current Value
     * @return Resolution dependent value.
     */

    private float pixelConverterY(float y) {
        return SCREEN_Y / WATCH_RES * y;
    }

    /**
     *
     * @param params Void no params
     * @return success of background execution.
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

                float x = objectReceived.getX();
                float y = objectReceived.getY();
                int modality = objectReceived.getModality();
                System.out.println("UDP " + objectReceived.toString());
                if (switchModality == 0){
                    if (type == 1) {
                        iView.setIsScrolling(true);
                        iView.setIsTapped(false);
                        iView.mouseMove(pixelConverterX(y * getCD(modality)), pixelConverterY(x * getCD(modality) * -1));
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
                        iView.mouseMove(pixelConverterX(x * getCD(modality)), pixelConverterY(y * getCD(modality)));
                    }
                    System.out.println("is acquired ? : " + iView.isGestureAcquired());
                    if (iView.isGestureAcquired() && !isInit) {
                        isInit = true;
                        time = System.nanoTime();
                    }
                    else if (iView.isGestureAcquired() && ((System.nanoTime() - time)/1e6) > 1000){
                        time = System.nanoTime();
                        isInit = true;
                        iView.setIsScrolling(false);
                        iView.setIsTapped(true);
                        iView.reDraw();
                    }
                }
                else {
                    if (type == 1) {
                        iView.setIsScrolling(true);
                        iView.setIsTapped(false);
                        iView.mouseMove(pixelConverterX(x * getCD(modality)), pixelConverterY(y * getCD(modality)));
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
     *
     * @param modality receive the modality to perform get the respective CD
     * @return CD - Control Display gain, a minimum and a maximum depending
     * on the modality.
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
                    return -1;
            case 1:
                if (modality == SenderObject.TOUCH_MODALITY)
                    return 0;
                else
                    return 1;
            case 2 :
                if (modality == SenderObject.GESTURE_MODALITY) {
                    double distance = iView.getDistance();
                    return (float) distance/Math.min(SCREEN_X, SCREEN_Y);
                } else {
                    return iView.getTargetWidth()/200;
                }
            case 3:
                if (modality == SenderObject.TOUCH_MODALITY) {
                    double distance = iView.getDistance();
                    return (float) distance/Math.min(SCREEN_X, SCREEN_Y);
                }
                else{
                    return iView.getTargetWidth()/200;
                }
            default:
                return 1;
        }
    }
}