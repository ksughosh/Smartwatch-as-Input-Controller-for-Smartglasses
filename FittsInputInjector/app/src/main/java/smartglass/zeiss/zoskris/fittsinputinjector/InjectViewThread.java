package smartglass.zeiss.zoskris.fittsinputinjector;

import android.graphics.Canvas;
import android.os.Looper;
import android.view.SurfaceHolder;

/**
 * Created by Sughosh Krishna Kumar on 05/10/15.
 * This is a work of thesis and therefore an academic work
 * This program is not to be used for any other purpose,
 * other than academics.
 */
public class InjectViewThread extends Thread {

    SurfaceHolder surfaceHolder;
    FittsInjectView injectSurfaceView;
    Canvas canvas;

    InjectViewThread(SurfaceHolder sh, FittsInjectView surfaceView) {
        surfaceHolder = sh;
        injectSurfaceView = surfaceView;
    }

    /**
     * Start the surface view and handle the drawing.
     */

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
