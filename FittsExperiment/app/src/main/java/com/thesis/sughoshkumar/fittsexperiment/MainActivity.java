package com.thesis.sughoshkumar.fittsexperiment;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

public class MainActivity extends Activity {
    // Define the view that needs to be drawn
    static FittsInputInjector iView;

    // Define the server that will receive and inject
    // values into the view
    UDPServer server;

    /**
     * Main drawing callback
     * @param savedInstanceState bundle instance for main UI.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iView = new FittsInputInjector(this);
        setContentView(iView, params);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        server = new UDPServer(8080, iView, size);
        server.execute();
    }
}
