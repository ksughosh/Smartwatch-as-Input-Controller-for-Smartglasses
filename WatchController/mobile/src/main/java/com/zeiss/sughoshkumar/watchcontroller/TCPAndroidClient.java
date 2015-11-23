package com.zeiss.sughoshkumar.watchcontroller;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.mycable.Argus;

/**
 * Created by sughoshkumar on 13/08/15.
 * Copyright -Protected
 */
public class TCPAndroidClient extends AsyncTask <Void,Void,Void> {

    private InetAddress ipAddress;
    private int port;
    private static final long CONNECTION_TIMEOUT_MS = 10;
    private static String nodeId;
    public Argus.ArgusEvent dataToSend;
    private static boolean  ERROR_FLAG = false;
    private Socket tcpSocket;
    private static boolean isAcknowledged = true;
    private Context context;


    public TCPAndroidClient(Context con){
        context = con;
        retrieveDeviceNode();
        dataToSend = Argus.ArgusEvent.newBuilder().setPosX(0).setPosY(0)
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_UNUSED)
                .build();
    }
    public TCPAndroidClient(Context con, String ip, int p) throws UnknownHostException {
        context = con.getApplicationContext();
        retrieveDeviceNode();
        ipAddress = InetAddress.getByName(ip);
        port = p;
        dataToSend = Argus.ArgusEvent.newBuilder().setPosX(0).setPosY(0)
                .setType(Argus.ArgusEvent.EventType.EVENT_TYPE_UNUSED)
                .build();
    }

    public TCPAndroidClient(Context con, String address, int p, Argus.ArgusEvent data) throws UnknownHostException {
        context = con.getApplicationContext();
        ipAddress = InetAddress.getByName(address);
        port = p;
        dataToSend = data;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try{
            System.out.println("node id : "+nodeId);
            tcpSocket = new Socket(ipAddress, port);
            OutputStream outputStream = tcpSocket.getOutputStream();
            DataOutputStream dos = new DataOutputStream(outputStream);
            int size = dataToSend.getSerializedSize();
            dos.writeInt(htonl(size));
            dataToSend.writeTo(outputStream);
            ERROR_FLAG = false;
            tcpSocket.close();
        }
        catch (IOException e){
            e.printStackTrace();
            ERROR_FLAG = true;
        }

        return null;
    }
    @Override
    protected void onPostExecute(Void params){
        if (ERROR_FLAG){
            Toast.makeText(context, "Error Connecting", Toast.LENGTH_SHORT).show();
        }
        else if (isAcknowledged) {
            isAcknowledged = false;
            System.out.println("nodeID : "+nodeId);
            sendToast("start",null);
        }
    }

    public void setIpAddressAndPort(String ip, int p) throws UnknownHostException {
        ipAddress = InetAddress.getByName(ip);
        port = p;
    }

    private static int htonl(int p_value)
    {
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN))
        {
            return p_value;
        }

        return Integer.reverseBytes(p_value);
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
                System.out.println("number of nodes : "+nodes.size());
                for (Node n : nodes) {
                    if(!n.getId().equals("cloud")) {
                        nodeId = n.getId();
                    }
                }

                client.disconnect();
            }
        }).start();
    }

    private void sendToast(final String message, final byte[] data) {
        final GoogleApiClient client = getGoogleApiClient(context);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.blockingConnect(CONNECTION_TIMEOUT_MS, TimeUnit.MILLISECONDS);
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                    client.disconnect();
                }
            }).start();
        }
    }
}
