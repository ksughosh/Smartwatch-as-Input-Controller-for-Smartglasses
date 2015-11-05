package com.zeiss.sughoshkumar.watchmouse;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.net.UnknownHostException;

public class PhoneMainActivity extends Activity {
   private UDPClient client;
    private Button mButton;
    private EditText mEditText;
    private TextView mTextView;
    public static boolean isConnected = false;
    public static String ipAddress;
    public static int port;
    private static final String IP_ADDRESS = "\\d{1,3}(?:\\.\\d{1,3}){3}(?::\\d{1,5})";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_main);
        mButton = (Button) findViewById(R.id.button);
        mEditText = (EditText) findViewById(R.id.editText);
        mTextView = (TextView) findViewById(R.id.textView);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditText.length() > 0 && mEditText.getText().toString().matches(IP_ADDRESS)) {
                    String[] parts = mEditText.getText().toString().split(":");
                    ipAddress = parts[0];
                    port = Integer.valueOf(parts[1]);
                }

                try {
                    SenderObject object = new SenderObject();
                    client = new UDPClient(PhoneMainActivity.this);
                    client.setIpAddressAndPort(ipAddress, port);
                    client.setDataToSend(SenderObject.toBytes(object));
                    client.execute();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                mEditText.setVisibility(View.GONE);
                mTextView.setText("Sending data");
                mButton.setVisibility(View.GONE);
            }
        });

    }
}
