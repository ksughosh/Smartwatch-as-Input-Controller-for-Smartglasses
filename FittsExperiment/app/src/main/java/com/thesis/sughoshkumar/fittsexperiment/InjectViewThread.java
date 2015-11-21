package com.thesis.sughoshkumar.fittsexperiment;

import android.graphics.Canvas;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class InjectViewThread extends Thread {

    // Thread parameter required to draw
    SurfaceHolder surfaceHolder;

    // View that needs drawing
    FittsInputInjector fittsInputInjector;

    // Object required to draw
    Canvas canvas;

    /**
     * Constructor
     * @param sh surface view holder
     * @param surfaceView surface view that needs to be drawn
     */
    InjectViewThread(SurfaceHolder sh, SurfaceView surfaceView) {
        surfaceHolder = sh;
        fittsInputInjector = (FittsInputInjector) surfaceView;
    }

    /**
     * Perform the drawing in a separate thread and notify the
     * main UI thread to draw it on the screen.
     */
    @Override
    public void run() {
        canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            synchronized (surfaceHolder) {
                fittsInputInjector.doDraw(canvas);
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
