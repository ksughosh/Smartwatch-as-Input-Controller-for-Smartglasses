package com.zeiss.sughoshkumar.watchcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.net.UnknownHostException;
/*
 * Copyright -Protected
 */
public class PhoneMainActivity extends Activity {

    private Button mButton;
    private EditText mEditText;
    private Menu menu;
    public static String ipAddress = "";
    public static int port = 0;
    public static boolean setTCP = false;
    private static final String IP_ADDRESS = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})";
    UDPClient udpClient;
    TCPAndroidClient tcpClient;

    public static boolean isAcknowledged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);
        udpClient = new UDPClient(this);
        tcpClient = new TCPAndroidClient(PhoneMainActivity.this);
        mEditText = (EditText) findViewById(R.id.editText);

        mButton = (Button) findViewById(R.id.button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.length() > 0 && mEditText.getText().toString().matches(IP_ADDRESS)) {
                    String[] parts = mEditText.getText().toString().split(":");
                    ipAddress = parts[0];
                    port = Integer.valueOf(parts[1]);
                    if (isAcknowledged){
                        Intent sendIntent = new Intent(PhoneMainActivity.this, PhoneSendingActivity.class);
                        startActivity(sendIntent);
                    }
                    try {
                        /**
                         * uncomment to enable communication switch
                         */
//                        if (setTCP) {
//                            System.out.println("in TCP");
                            // TCP MODE TRANSMISSION
                            // SENDS A PING UPON RECEIVING PONG THE ACTIVITY WILL START ON THE WATCH
//                           System.out.println("doing nothing");
//                        }
                            // UDP MODE TRANSMISSION
                            udpClient.setIPAddressAndPort(ipAddress,port);
                            udpClient.execute();
//                            tcpClient.setIpAddressAndPort(ipAddress, port);
//                            tcpClient.execute();
//                        }

                    } catch (UnknownHostException e) {
                        e.printStackTrace();
                        Toast.makeText(PhoneMainActivity.this, "Unable to Connect incorrect IP or " +
                                "port wrong", Toast.LENGTH_LONG).show();
                    }
                }
                else
                {
                    Toast.makeText(PhoneMainActivity.this, "Enter IP:Port", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
    /**
     * Enable this block for communication switch
    //@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_phone_main, menu);
        this.menu = menu;
        return true;
    }
    */

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.protocol) {
            updateMenuTitles();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getIpAddress(){
        return ipAddress;
    }

    public static int getPort(){
        return port;
    }

    private void updateMenuTitles(){
        final String tcp = getString(R.string.tcp);
        final String udp = getString(R.string.udp);

        MenuItem menuItem = menu.findItem(R.id.protocol);
        if (menuItem.getTitle().equals(tcp)){
            setTCP = true;
            menuItem.setTitle(udp);
        }
        else if (menuItem.getTitle().equals(udp)){
            setTCP = false;
            menuItem.setTitle(tcp);
        }
    }
    public static boolean getSetTcp(){
        return setTCP;
    }

}
