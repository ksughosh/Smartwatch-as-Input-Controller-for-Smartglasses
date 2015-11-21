package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * This class is not used anymore since this application
 * became stand alone and can communicate with the server
 * directly via WiFi.
 */
public class MessageSender {
    private GoogleApiClient client;
    private String nodeId;

    MessageSender(Context context) {
        client = getGoogleApiClient(context);
        retrieveDeviceNode();
    }

    /**
     * @param context : creates a new GoggleAPI client for this context.
     * @return GoogleAPI client.
     */

    private GoogleApiClient getGoogleApiClient(Context context) {
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    /**
     * Retrieves the devices connected to the shared
     * Data layer.
     */

    private void retrieveDeviceNode() {
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
    }

    /**
     * @param message : message path to be sent to the data layer
     * @param data    : data to be sent in bytes[] to data layer
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
}

