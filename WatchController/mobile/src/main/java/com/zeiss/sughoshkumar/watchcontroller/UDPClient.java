package com.zeiss.sughoshkumar.watchcontroller;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.mycable.Argus;

/**
 * Created by sughoshkumar on 13/08/15.
 * Copyright -Protected
 */
public class UDPClient extends AsyncTask <Void,Void,Void> implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener{

    private InetAddress inetAddress;
    private int port;
    private DatagramSocket udpSocket;
    private static boolean isAcknowledged = true;
    private Context context;
    private static boolean ERROR_FLAG = false;
    public static final long CONNECTION_TIMEOUT_MS = 10;
    private static String nodeId;
    private com.google.protobuf.GeneratedMessage message;

    public UDPClient(Context con) {
        context = con;
        retrieveDeviceNode();
        message = Argus.ArgusEvent.newBuilder()
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_EVENT_SCROLL)
                .setScrolling(Argus.ArgusEvent.ScrollType.SCROLL_EVENT_DOWN)
                .setScrollAmount(1)
                .setPosX(0).setPosY(0)
                .build();
    }

    public UDPClient(Context con, String ip, int p, Argus.ArgusEvent msg) throws UnknownHostException {
        context = con;
        inetAddress = InetAddress.getByName(ip);
        port = p;
        message = msg;
    }

    public UDPClient(Context con, Argus.ArgusEvent msg) throws UnknownHostException {
        context = con;
        message = msg;
    }

    public void setMessage(Argus.ArgusEvent msg){
        message = msg;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            udpSocket = new DatagramSocket(port);
            int size = message.getSerializedSize();
            ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream(size);
            message.writeTo(byteArrayOS);
            byte[] dataToSend = byteArrayOS.toByteArray();
            DatagramPacket packetToSend = new DatagramPacket(dataToSend, dataToSend.length, inetAddress, port);
            udpSocket.send(packetToSend);
            System.out.println("message @ UDP : " + Argus.ArgusEvent.parseFrom(packetToSend.getData()).toString());
            udpSocket.close();
        } catch (SocketException e) {
            e.printStackTrace();
            ERROR_FLAG = true;
        } catch (IOException e) {
            e.printStackTrace();
            ERROR_FLAG = true;
        }


        return null;
    }

    public void setIPAddressAndPort(String address, int p) throws UnknownHostException {
        inetAddress = InetAddress.getByName(address);
        port = p;
    }



    @Override
    protected  void onPostExecute (Void param){
        if (ERROR_FLAG){
            Toast.makeText(context, "ERROR SERVER NOT FOUND", Toast.LENGTH_LONG).show();
        }
        else if (isAcknowledged) {
            sendToast("start", null);
            isAcknowledged = false;
        }
    }

    private GoogleApiClient getGoogleApiClient(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode(){
        final GoogleApiClient client = getGoogleApiClient(context);
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.blockingConnect(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                NodeApi.GetConnectedNodesResult results = Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = results.getNodes();
                for (Node n : nodes) {
                    if(!n.getId().equals("cloud")) {
                        nodeId = n.getId();
                    }
                }
            }
        }).start();
    }

    private final void sendToast(final String message, final byte[] data) {
        final GoogleApiClient client = getGoogleApiClient(context);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                }
            }).start();
            System.out.println("message sent");
        }
    }

    @Override
    public void onConnected(Bundle bundle) {
        System.out.println("onConnected called");
    }

    @Override
    public void onConnectionSuspended(int i) {
        System.out.println("Connection is suspended");
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        System.out.println("Connection failed");
    }
}
