package smartglass.zeiss.zoskris.fittsinputinjector;

import android.graphics.Canvas;
import android.os.Looper;
import android.view.SurfaceHolder;

/**
 * Created by zoskris on 05/10/15.
 */
public class InjectViewThread extends Thread {

    public static Looper surfaceLooper;
    SurfaceHolder surfaceHolder;
    FittsInjectView injectSurfaceView;
    Canvas canvas;

    InjectViewThread(SurfaceHolder sh, FittsInjectView surfaceView) {
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
