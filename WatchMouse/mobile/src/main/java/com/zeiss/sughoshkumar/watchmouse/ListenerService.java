package com.zeiss.sughoshkumar.watchmouse;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by sughoshkumar on 26/08/15.
 */
public class ListenerService extends WearableListenerService {
   @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        System.out.println(" Message. Path : " + path + " received");
        byte[] data = messageEvent.getData();
        SenderObject object = null;
        try {
             object = SenderObject.parseFrom(data);
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        float x = object.getX();
        float y = object.getY();
        int type = object.getType();
        int modality = object.getModality();
        System.out.println("x : " + x + " y : " + y + "type : " + type + "modality " + object.getModality(modality));
        try {
            if (PhoneMainActivity.isConnected) {
                System.out.println("Sending from the listener to IP : " + PhoneMainActivity.ipAddress + " port : " + PhoneMainActivity.port);
                UDPClient client = new UDPClient(ListenerService.this, PhoneMainActivity.ipAddress, PhoneMainActivity.port, data);
                client.execute();
            }
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
