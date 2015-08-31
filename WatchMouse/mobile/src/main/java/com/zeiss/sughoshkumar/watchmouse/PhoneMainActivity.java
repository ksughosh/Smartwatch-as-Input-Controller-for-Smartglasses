package com.zeiss.sughoshkumar.watchmouse;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import java.net.UnknownHostException;

public class PhoneMainActivity extends Activity {
    private UDPClient client;
    private Button mButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);
        mButton = (Button) findViewById(R.id.button);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    client = new UDPClient(PhoneMainActivity.this);
                    client.setIpAddressAndPort("172.16.1.192", 8083);
                    client.setDataToSend(null);
                    client.execute();
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
