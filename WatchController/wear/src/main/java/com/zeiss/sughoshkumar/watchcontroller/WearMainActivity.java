package com.zeiss.sughoshkumar.watchcontroller;

import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.WatchViewStub;

/*
 * Copyright -Protected
 */
public class WearMainActivity extends WearableActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                stub.setOnTouchListener(new OnSwipeTouchListener(getApplicationContext()));
            }
        });
    }
    @Override
    public void onEnterAmbient(Bundle ambientDetails){
        super.onEnterAmbient(ambientDetails);
    }
}
