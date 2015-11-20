package com.thesis.sughoshkumar.fittsexperiment;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

/**
 * Created by zoskris on 05/10/15.
 */
public class InjectViewThread extends Thread {

    SurfaceHolder surfaceHolder;
    FittsInputInjector fittsInputInjector;
    Canvas canvas;

    InjectViewThread(SurfaceHolder sh, FittsInputInjector surfaceView) {
        surfaceHolder = sh;
        fittsInputInjector = surfaceView;
    }

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
