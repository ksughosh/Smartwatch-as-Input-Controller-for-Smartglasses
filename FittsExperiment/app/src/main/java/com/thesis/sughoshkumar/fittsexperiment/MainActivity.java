package com.thesis.sughoshkumar.fittsexperiment;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    static FittsInputInjector iView;
    UDPServer server;
    public static int SCREEN_X, SCREEN_Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iView = new FittsInputInjector(this);
        setContentView(iView, params);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_X = size.x;
        SCREEN_Y = size.y;
        server = new UDPServer(8080, iView, size);
        server.execute();
    }
}
