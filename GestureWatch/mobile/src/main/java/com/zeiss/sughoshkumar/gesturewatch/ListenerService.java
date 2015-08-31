package com.zeiss.sughoshkumar.gesturewatch;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.zeiss.sughoshkumar.watchmouse.SenderObject;

import java.io.IOException;


/**
 * Created by sughoshkumar on 25/08/15.
 */
public class ListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        final int SENSITIVITY_COEFFICIENT = -2;
        String path = messageEvent.getPath();
        byte[] data = messageEvent.getData();
        try {
            DataClasser clas = DataClasser.parseFrom(data);
            int xValue = Math.round(clas.getzRot()) * SENSITIVITY_COEFFICIENT;
            int yValue = Math.round(clas.getyRot()) * SENSITIVITY_COEFFICIENT;
            System.out.println("x : " + xValue + " y : " + yValue);
            SenderObject objectToSend = new SenderObject(xValue,yValue, 1);
            UDPClient client = new UDPClient(ListenerService.this, "172.16.1.192", 8083, SenderObject.toBytes(objectToSend));
            client.execute();
        }
        catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
