package com.zeiss.sughoshkumar.watchmouse;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;

import org.json.JSONObject;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * Created by sughoshkumar on 26/08/15.
 */
public class ListenerService extends WearableListenerService {
    @Override
    public void onMessageReceived(MessageEvent messageEvent) {
        String path = messageEvent.getPath();
        byte[] data = messageEvent.getData();
        SenderObject object = null;
        try {
             object = SenderObject.parseFrom(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        int x = object.x;
        int y = object.y;
        int type = object.type;
        System.out.println("x : " + x + " y : " + y + "type : " + type);
        try {
            UDPClient client = new UDPClient(ListenerService.this, "172.16.1.192", 8083, data);
            client.execute();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }
}
