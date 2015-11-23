package sughoshmasterthesis.com.watchcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.AsyncTask;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Test class implemented to observe if the communication is better
 * compared to that of WiFi. It wasn't since there was a huge latency
 * between packets. Hence this class is not used, nevertheless stays
 * as a part of the implementation.
 */

public class BluetoothClient extends AsyncTask<Void, Void,Void>{

    private BluetoothSocket socket;
    private DataOutputStream outWrite;
    private SenderObject dataToSend;
    private BluetoothDevice remoteDevice;
    private BluetoothAdapter adapter;

    /**
     * Constructor
     * @param senderObject object to send
     */

    BluetoothClient(SenderObject senderObject){
        dataToSend = senderObject;
        try {
            connectToDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     */
    public BluetoothClient(){
        dataToSend = new SenderObject();
        try {
            connectToDevice();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Background task
     * @param params none
     * @return null
     */

    @Override
    protected Void doInBackground(Void... params) {
        UUID uuid = UUID.fromString("4e5d48e0-75df-11e3-981f-0800200c9a66");
        System.out.println("Device Name : " + remoteDevice.getName());
        try {
            socket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
            System.out.println("Socket Connected : " + socket.toString());
            socket.connect();
            outWrite = new DataOutputStream(socket.getOutputStream());
            outWrite.write(dataToSend.toBytes());
            outWrite.flush();
            socket.close();
            System.out.println("Sent to bluetooth");
        } catch (IOException e) {
            try {
                socket = remoteDevice.createRfcommSocketToServiceRecord(uuid);
                socket.connect();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }

        return null;
    }

    /**
     * Post exec
     * @param result from the async task.
     */
    @Override
    protected void onPostExecute(Void result){
        WearMainActivity.isConnected = true;
    }


    /**
     * Connect to the device
     * @throws Exception device not found
     */
    private void connectToDevice() throws Exception {
        adapter = BluetoothAdapter.getDefaultAdapter();

        if (adapter.isEnabled()){
            for (BluetoothDevice bt : adapter.getBondedDevices()){
                if (bt.getName().equalsIgnoreCase("X6")){
                    remoteDevice = adapter.getRemoteDevice(bt.getAddress());
                    adapter.cancelDiscovery();
                }
            }
            if (remoteDevice == null)
                throw new Exception("Device not Initialized");
        }
    }

    /**
     * Set data to be sent
     * @param object data to be sent
     */

    public void sendData(SenderObject object){
        dataToSend = object;
    }

    /**
     * Kill the bluetooth client
     * @throws IOException if socket is still open
     */

    public void kill() throws IOException {
        if (socket != null){
            outWrite.close();
            socket.close();
        }
    }
}
