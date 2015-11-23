package com.zeiss.sughoshkumar.watchcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

/*
 * Copyright -Protected
 */
public class PhoneSendingActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR);
        getActionBar().hide();
        setContentView(R.layout.activity_phone_sending);
    }
}
