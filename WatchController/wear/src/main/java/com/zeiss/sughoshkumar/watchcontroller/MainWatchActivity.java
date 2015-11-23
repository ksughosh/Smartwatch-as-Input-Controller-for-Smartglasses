package com.zeiss.sughoshkumar.watchcontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

/*
 * Copyright -Protected
 */
public class MainWatchActivity extends Activity {

    private TextView mTextView;
    public static boolean isAcknowledged = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_watch);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("Waiting to Connect...");
                if (isAcknowledged) {
                    Intent scrollIntent = new Intent(MainWatchActivity.this, WearMainActivity.class);
                    scrollIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(scrollIntent);
                }
            }
        });
    }

    public static void setIsAcknowledged(boolean value){
        isAcknowledged = value;
    }


}
