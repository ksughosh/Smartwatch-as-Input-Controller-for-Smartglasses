package smartglass.zeiss.zoskris.fittsinputinjector;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.ViewGroup;

public class MainActivity extends Activity {

    static InjectSurfaceView iView;
    UDPServer server;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        iView = new InjectSurfaceView(this);
        setContentView(iView, params);
        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        server = new UDPServer(8085, iView, size);
        server.execute();
    }
}
