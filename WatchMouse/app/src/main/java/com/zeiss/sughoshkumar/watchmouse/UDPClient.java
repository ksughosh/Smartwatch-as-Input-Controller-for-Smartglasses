package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by sughoshkumar on 26/08/15.
 */
public class UDPClient extends AsyncTask<Void, Void, Void> {
    private DatagramSocket udpSocket;
    private int port;
    private InetAddress ipAddress;
    private static boolean ERROR_FLAG;
    private Context context;

    private byte[] dataToSend;

    UDPClient(Context context1, String ip, int p, byte[] data) throws UnknownHostException {
        context = context1;
        ipAddress = InetAddress.getByName(ip);
        port = p;
        dataToSend = data;
        ERROR_FLAG = false;
    }

    UDPClient(SenderObject data){
        try {
            dataToSend = SenderObject.toBytes(data);
            ipAddress = InetAddress.getByName(WearMainActivity.IPAddress);
            port = 8080;
            ERROR_FLAG = false;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    UDPClient(Context context1) {
        context = context1;
        try {
            dataToSend = SenderObject.toBytes(new SenderObject());
            ipAddress = InetAddress.getByName(WearMainActivity.IPAddress);
            port = 8080;
            ERROR_FLAG = false;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setDataToSend(byte[] data){
        dataToSend = data;
    }

    @Override
    protected Void doInBackground(Void... params) {
        try{
            udpSocket = new DatagramSocket(port);
            DatagramPacket packetToSend = new DatagramPacket(dataToSend, dataToSend.length,
                    ipAddress, port);
            udpSocket.send(packetToSend);
            System.out.println("@UDP Client send successfully " + SenderObject.parseFrom(dataToSend).toString());
            udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            ERROR_FLAG = true;
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (ERROR_FLAG){
            Log.e("UDP", "ERROR CONNECTION");
        }
        else{
            WearMainActivity.isConnected = true;
        }
    }
}
