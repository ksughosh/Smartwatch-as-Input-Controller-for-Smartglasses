package sughoshmasterthesis.com.watchcontroller;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

/**
 * This class is not used. It is retained as it was part of version 1's
 * implementation. Since the application became standalone, there is no
 * necessity for this class. Hence not used anywhere.
 */
public class SendMessage implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private GoogleApiClient client;
    private String nodeId;
    private String connectionStatus;

    SendMessage(Context context){
        client = getGoogleApiClient(context);
        retrieveDeviceNode();
    }

    /**
     * @param context : creates a new GoggleAPI client for this context.
     * @return GoogleAPI client.
     */

    public GoogleApiClient getGoogleApiClient(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    /**
     * Retrieves the devices connected to the shared
     * Data layer.
     */

    public void retrieveDeviceNode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.connect();
                NodeApi.GetConnectedNodesResult results = Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = results.getNodes();
                if (nodes.size() > 0 ){
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }

    /**
     *
     * @param message : message path to be sent to the data layer
     * @param data : data to be sent in bytes[] to data layer
     */
    public final void sendToast(final String message, final byte[] data) {
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

    @Override
    public void onConnected(Bundle bundle) {
        Log.d("WATCH", "Connected");
        connectionStatus = "0";
        sendToast(connectionStatus,connectionStatus.getBytes());
    }

    @Override
    public void onConnectionSuspended(int i) {
        connectionStatus = "-2";
        sendToast(connectionStatus,connectionStatus.getBytes());
        Log.e("WATCH", "Connection Suspended" + i, new Throwable("Connection has been suspended"));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e("WATCH", "Connection Failed", new Throwable(connectionResult.toString()));
        connectionStatus = "-1";
        sendToast(connectionStatus, connectionStatus.getBytes());
    }
}
