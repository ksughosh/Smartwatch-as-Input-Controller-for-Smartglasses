package sughoshmasterthesis.com.watchcontroller;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Class that handles the communication of the coordinates.
 * Here the necessary data is sent. The packets are not polled.
 * They're sent on demand and the protocol used is UDP.
 */
public class UDPClient extends AsyncTask<Void, Void, Void> {
    private int port;
    private InetAddress ipAddress;
    private static boolean ERROR_FLAG;
    private Context context;
    private byte[] dataToSend;
    @SuppressWarnings("FieldCanBeLocal")
    private SenderObject temp;

    /**
     * Constructor
     * @param context1 application context
     * @param ip IP address
     * @param p port
     * @param data data to be sent
     * @throws UnknownHostException connection exception.
     **/
    @SuppressWarnings("unused")
    UDPClient(Context context1, String ip, int p, byte[] data) throws UnknownHostException {
        context = context1;
        ipAddress = InetAddress.getByName(ip);
        port = p;
        dataToSend = data;
        ERROR_FLAG = false;
    }

    /**
     * Constructor
     * @param context1 application context
     */
    UDPClient(Context context1) {
        context = context1;
        temp = new SenderObject();
        try {
            ipAddress = InetAddress.getByName(WearMainActivity.ipAddress);
            dataToSend = temp.toBytes();
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        port = 8080;
    }

    /**
     * Constructor
     * @param data to be sent through UDP
     */
    UDPClient(byte[] data){
        try {
            dataToSend = data;
            ipAddress = InetAddress.getByName(WearMainActivity.ipAddress);
            port = 8080;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to set IP address and port separately
     * @param ip IP address as string
     * @param p port number
     * @throws IOException is IP address is not valid for connection
     */
    @SuppressWarnings("unused")
    public void setIpAddressAndPort(String ip, int p) throws IOException {
        ipAddress = InetAddress.getByName(ip);
        port = p;
        dataToSend = new byte[new SenderObject().toBytes().length];
        ERROR_FLAG = false;
    }

    /**
     * Method to set the data to be sent
     * @param data set data to be sent
     */
    @SuppressWarnings("unused")
    public void setDataToSend(byte[] data){
        dataToSend = data;
    }

    /**
     * Main background task to send the values using UDP
     * @param params NO PARAMS
     * @return null
     */

    @Override
    protected Void doInBackground(Void... params) {
        try{
            DatagramSocket udpSocket = new DatagramSocket(port);
            DatagramPacket packetToSend = new DatagramPacket(dataToSend, dataToSend.length,
                    ipAddress, port);
            udpSocket.send(packetToSend);
            udpSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
            ERROR_FLAG = true;
        }
        return null;
    }

    /**
     * Handling post execution
     * @param aVoid null
     */
    @Override
    protected void onPostExecute(Void aVoid) {
        if (ERROR_FLAG && context != null){
            Toast.makeText(context, "ERROR CONNECTION", Toast.LENGTH_LONG).show();
        }
        else if (ERROR_FLAG){
            Log.e("UDP", "Error in connection");
        }
        else{
            WearMainActivity.isConnected = true;
        }
    }
}
