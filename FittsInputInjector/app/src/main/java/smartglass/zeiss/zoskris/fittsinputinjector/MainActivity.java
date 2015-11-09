package smartglass.zeiss.zoskris.fittsinputinjector;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

/**
 * Created by Sughosh Krishna Kumar on 05/10/15.
 * This is a work of thesis and therefore an academic work
 * This program is not to be used for any other purpose,
 * other than academics.
 */

public class MainActivity extends Activity {

    static FittsInjectView iView;
    UDPServer server;
    public static int SCREEN_X, SCREEN_Y;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // get the view parameters and set the view.
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iView = new FittsInjectView(this);
        setContentView(iView, params);

        // get screen size
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        SCREEN_X = size.x;
        SCREEN_Y = size.y;

        // initialize and start the UDP server
        server = new UDPServer(8085, iView, size);
        server.execute();
    }
}
