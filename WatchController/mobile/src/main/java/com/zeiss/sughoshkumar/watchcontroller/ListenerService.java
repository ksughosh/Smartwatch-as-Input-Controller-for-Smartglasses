package com.zeiss.sughoshkumar.watchcontroller;

import android.content.Intent;
import android.os.Handler;
import android.widget.Toast;

import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.WearableListenerService;
import com.google.protobuf.InvalidProtocolBufferException;

import java.net.UnknownHostException;

import de.mycable.Argus;

/**
 * Created by sughoshkumar on 13/08/15.
 * Copyright -Protected
 * Listener service listening to data from the wearable.
 * Continuous data from the wearable is packed and sent here.
 */
public class ListenerService extends WearableListenerService {

    /**
     * @param messageEvent : gives the message from the data layer
     */

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        System.out.println("Message at listener : "+ messageEvent.getPath());
        verifyAndSend(messageEvent.getPath(), messageEvent.getData());
    }

    /**
     * @param message : String value contains path of the data
     * @param data : byte array contains the data on the data layer
     */

    private void verifyAndSend (String message, byte[] data) {
        showToast(message, 100);
        try {
            if (message.equals("start")){
                showToast("Starting ... ", 500);
                Intent sendIntent = new Intent(this, PhoneSendingActivity.class);
                sendIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sendIntent);
            }
            else
            {
                Argus.ArgusEvent eventToSend = Argus.ArgusEvent.parseFrom(data);
//                System.out.println("message : " + eventToSend.toString());
                if (PhoneMainActivity.getSetTcp()) {
                    sendToTCPServer(eventToSend);
                }
                else{
                    sendToUDPServer(eventToSend);
                }
            }
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), "Invalid Setting - CODE", Toast.LENGTH_SHORT).show();
        }

    }
    /**
     * @param message : String value to be Toasted.
     * @param duration : Length of the Toast.
     */

    private void showToast(String message, long duration){
        final Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    /**
     * Methods to send to server using TCP
     * @param message : Argus.ArgusEvent is the Builder object that is sent
     */

    private void sendToTCPServer(Argus.ArgusEvent message) throws UnknownHostException {
        System.out.println("Message received " + message.toString());
        TCPAndroidClient client = new TCPAndroidClient(getApplicationContext(),
                PhoneMainActivity.getIpAddress(),PhoneMainActivity.getPort(),message);
        client.execute();
    }


    /**
     * Methods to send to server using UDP
     * @param message : Argus.ArgusEvent is the Builder object that is sent
     */

    private void sendToUDPServer(Argus.ArgusEvent message) throws UnknownHostException {
//        System.out.println("iP:" + PhoneMainActivity.getIpAddress() + " port : "+PhoneMainActivity.getPort());
//        System.out.println("Message received " +message.toString());
        UDPClient client = new UDPClient(getApplicationContext());
        client.setIPAddressAndPort(PhoneMainActivity.getIpAddress(), PhoneMainActivity.getPort());
        client.setMessage(message);
        client.execute();
    }
}
