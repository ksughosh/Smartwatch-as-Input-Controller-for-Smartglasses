package com.example.sughoshkumar.trials;

import android.graphics.Canvas;
import android.os.Looper;
import android.view.SurfaceHolder;

/**
 * Created by zoskris on 05/10/15.
 */
public class InjectViewThread extends Thread {

    public static Looper surfaceLooper;
    SurfaceHolder surfaceHolder;
    InjectSurfaceView injectSurfaceView;
    Canvas canvas;

    InjectViewThread(SurfaceHolder sh, InjectSurfaceView surfaceView) {
        surfaceHolder = sh;
        injectSurfaceView = surfaceView;
    }

    @Override
    public void run() {
        canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            synchronized (surfaceHolder) {
                injectSurfaceView.doDraw(canvas);
            }
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}
