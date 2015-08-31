package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.TimerTask;

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

    UDPClient(Context context1) {
        context = context1;
    }

    public void setIpAddressAndPort(String ip, int p) throws UnknownHostException {
        ipAddress = InetAddress.getByName(ip);
        port = p;
        ERROR_FLAG = false;
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

    @Override
    protected void onPostExecute(Void aVoid) {
        if (ERROR_FLAG){
            Toast.makeText(context, "ERROR CONNECTION", Toast.LENGTH_LONG).show();
        }
    }
}
