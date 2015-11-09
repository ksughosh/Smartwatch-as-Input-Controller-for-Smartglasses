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
 * Created by zoskris on 05/10/15.
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
    private static final float MAXIMUM_AMPLITUDE = 0.5f;
    private final int switchModality = 2;
    private boolean isInit;
    private double time;


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

    public static void kill() {
        keepRunning = false;
    }

    private float pixelConverterX(float x) {
        return SCREEN_X / WATCH_RES * x;
    }

    private float pixelConverterY(float y) {
        return SCREEN_Y / WATCH_RES * y;
    }


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
//                System.out.println("UDP " + objectReceived.toString());
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
                    return (float) distance/Math.min(SCREEN_X,SCREEN_Y);
                } else {
                    return iView.getTargetWidth()/100;
                }
            case 3:
                if (modality == SenderObject.TOUCH_MODALITY) {
                    double distance = iView.getDistance();
                    return (float) distance/Math.min(SCREEN_X,SCREEN_Y);
                }
                else{
                    return iView.getTargetWidth()/100;
                }
            default:
                return 1;
        }
    }
}