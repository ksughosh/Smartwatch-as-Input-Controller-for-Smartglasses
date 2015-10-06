package com.zeiss.sughoshkumar.watchmouse;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
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
                    SenderObject object = new SenderObject();
                    client = new UDPClient(PhoneMainActivity.this);
                    client.setIpAddressAndPort("172.16.10.49", 8085);
                    client.setDataToSend(SenderObject.toBytes(object));
                    client.execute();
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }
}
