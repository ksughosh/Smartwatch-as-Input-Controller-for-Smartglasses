package com.zeiss.sughoshkumar.watchcontroller;

import android.content.Context;
import android.content.Intent;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.concurrent.TimeUnit;

/**
 * Created by sughoshkumar on 13/08/15.
 *
 * Copyright -Protected
 *
 */
public class WearListenerService extends WearableListenerService {
    public static int CONNECTION_TIMEOUT_MS = 0;
    String nodeId;

    @Override
    public void onMessageReceived(MessageEvent messageEvent){
        nodeId = messageEvent.getSourceNodeId();
        if (messageEvent.getPath().equals("start")){
            Intent scrollIntent = new Intent(getApplicationContext(), WearMainActivity.class);
            scrollIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(scrollIntent);
            sendToast("start",null);
        }
    }

    private GoogleApiClient getGoogleApiClient(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void sendToast(final String message, final byte[] data) {
        final GoogleApiClient client = getGoogleApiClient(this);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                }
            }).start();
        }
    }
}
