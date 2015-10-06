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
    private InjectSurfaceView iView;
    private static final int TOUCH_MODALITY = 1;
    private static final int GESTURE_MODALITY = 2;
    private static final float THRESHOLD = 0.5f;


    public UDPServer(int p, InjectSurfaceView childView, Point dimension) {
        port = p;
        receiveData = new byte[MAX_DATAGRAM_PACKET_SIZE];
        keepRunning = true;
        iView = childView;
        SCREEN_X = dimension.x;
        SCREEN_Y = dimension.y;
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

    private boolean modalitySwitchGestureAccelerate(int modality){
         return(modality == TOUCH_MODALITY);
    }

    private boolean modalitySwitchTouchAccelerate(int modality){
        return (modality == GESTURE_MODALITY);
    }

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
                Log.i("UDP", objectReceived.toString());
                if (type == 1) {
                    iView.setIsScrolling(true);
                    iView.setIsTapped(false);
//                    if (modality  == GESTURE_MODALITY)
//                        iView.mouseMove(pixelConverterX(10 * x), pixelConverterY(10 * y));
                    iView.mouseMove(pixelConverterX(x), pixelConverterY(y));
                } else if (type == 2) {
                    iView.setIsTapped(true);
                    iView.setIsScrolling(false);
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
}