package com.zeiss.sughoshkumar.watchmouse;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import com.zeiss.sughoshkumar.senderobject.SenderObject;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class UDPClient extends AsyncTask<Void, Void, Void> {

    // Define the required UDP parameters
    private DatagramSocket udpSocket;
    private int port;
    private InetAddress ipAddress;
    private static boolean ERROR_FLAG;
    private Context context;
    private byte[] dataToSend;

    /**
     * Constructor
     * @param context1 application context for first connect to determine the connection
     * @param ip ip address
     * @param p port
     * @param data data to be sent
     * @throws UnknownHostException if host for the IP not found
     */
    UDPClient(Context context1, String ip, int p, byte[] data) throws UnknownHostException {
        context = context1;
        ipAddress = InetAddress.getByName(ip);
        port = p;
        dataToSend = data;
        ERROR_FLAG = false;
    }

    /**
     * Constructor
     * @param data data to be sent
     */
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

    /**
     * Setter for data to be sent
     * @param data data to be sent
     */
    public void setDataToSend(byte[] data){
        dataToSend = data;
    }

    /**
     * Send the data to the server in background
     * @param params Void
     * @return null
     */
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

    /**
     * Post execute
     * @param aVoid result from execution
     */
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
