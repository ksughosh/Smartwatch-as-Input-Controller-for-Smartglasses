package smartglass.zeiss.zoskris.fittsinputinjector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.ArrayList;

/**
 * Created by zoskris on 05/10/15.
 */
public class InjectSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final int SIZE = 10;
    private static final int MOUSE_THRESHOLD = 2;
    private static final int SENSITIVITY_COEFFICIENT = 10;
    private static final int DISTANCE_DIAMETER = 500;
    private static final int NUMBER_OF_TARGETS = 10;
    public static ArrayList<Coordinates> XYPair;
    private static double INDEX_OF_DIFFICULTY = 4;
    private static int DISTANCE_RADIUS;
    private static float RADIUS_We;
    private static double DISTANCE_ANGLE;
    public long timeAtStart;
    public ArrayList<Double> timeBetweenTargets;
    public boolean isScrolling;
    SurfaceHolder sh;
    InjectViewThread thread;
    private float x, y, offsetX, offsetY, prevOffsetX, prevOffsetY;
    private int mouseCount = 0;
    private Context con;
    private boolean isInitialized, isFinished, isTapped, isAccelerate;
    private int order;
    private long initialTime;
    private Handler mHandler;
    private Paint mPaint;
    private int canvasWidth, canvasHeight;
    private int count;

    public InjectSurfaceView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
        init(getMeasuredHeight() / 2, getMeasuredHeight() / 2);
    }

    private void init(int xer, int yer) {
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        DISTANCE_RADIUS = DISTANCE_DIAMETER / 2;
        RADIUS_We = Float.parseFloat(String.valueOf(DISTANCE_DIAMETER / Math.pow(2, INDEX_OF_DIFFICULTY)));
        DISTANCE_ANGLE = 360 / NUMBER_OF_TARGETS;
        INDEX_OF_DIFFICULTY = Math.log(2 * DISTANCE_DIAMETER / RADIUS_We);
        XYPair = new ArrayList<Coordinates>();
        count = 0;
        x = xer;
        y = yer;
        offsetX = offsetY = 0;
        mPaint = new Paint();
        isInitialized = false;
        isFinished = false;
        timeAtStart = 0;
        timeBetweenTargets = new ArrayList<Double>();
        initialTime = 0;
        isScrolling = false;
        isAccelerate = false;
        mHandler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
//        thread = new InjectViewThread(sh, this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(0.5f);
        mPaint.setColor(Color.RED);
        thread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
    }

    void doDraw(Canvas canvas) {
        canvasHeight = getMeasuredHeight();
        canvasWidth = getMeasuredWidth();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

        if (isFinished) {
            int textSize = 40;
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(textSize);
            canvas.drawColor(Color.BLACK);
            String finished = "FINISHED";
            canvas.drawText(finished, canvasWidth / 2 - Math.round((textSize * 1.5) + finished.length()), canvasHeight / 2, mPaint);

        }

        drawFitts(canvas);
        int prevOrder = -1;
        if (!isInitialized) {
            x = getWidth() / 2;
            y = getHeight() / 2;
            isInitialized = true;
            order = getOrder(prevOrder);
        }

        if (order != -1) {
            mPaint.setColor(Color.YELLOW);
            Coordinates tempCoord = XYPair.get(order);
            canvas.drawCircle(tempCoord.getX(), tempCoord.getY(), RADIUS_We, mPaint);
            prevOrder = order;
        } else {
            isFinished = true;
        }


        if (isSelected(order, x, y) && isTapped && !isScrolling) {
            double time = getTimeInbetween();
            if (time > 0) {
                System.out.println("time inbetween : " + time);
                timeBetweenTargets.add(time);
            }
            isTapped = false;
            order = getOrder(prevOrder);
        }

        /**
         * Draw target marker.
         * This will be updated on X and Y and also check for
         * intersection with the previously drawn permanent
         * targets.
         */

        // check redundant offset
        if (isRedundantOffset()) {
            offsetX = 0;
            offsetY = 0;
        } else if (isAccelerate) {
            offsetX += offsetX;
            offsetY += offsetY;
        }

        x -= offsetX;
        y -= offsetY;


        Coordinates coordinates = hasIntersected(x, y, RADIUS_We, SIZE);
        if (coordinates != null) {
            mPaint.setColor(Color.CYAN);
            canvas.drawCircle(coordinates.getX(), coordinates.getY(), RADIUS_We, mPaint);
        }

        if (getTopCoordinate(x) <= 0)
            x = SIZE;
        else if (getBottomCoordinate(x) >= canvasWidth)
            x = canvasWidth - SIZE;

        if (getTopCoordinate(y) <= 0)
            y = SIZE;
        else if (getBottomCoordinate(y) >= canvasHeight)
            y = canvasHeight - SIZE;

        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(x, y, SIZE, mPaint);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(x, y, SIZE / 2, mPaint);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(x - SIZE, y, x + SIZE, y, mPaint);
        canvas.drawLine(x, y - SIZE, x, y + SIZE, mPaint);
        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
    }

    /**
     * Check redundant offset that needs to be zeroed.
     */

    private boolean isRedundantOffset() {
        if (offsetX == prevOffsetX && offsetY == prevOffsetY)
            mouseCount++;
        if (mouseCount >= MOUSE_THRESHOLD && !isAccelerate) {
            mouseCount = 0;
            return true;
        } else if (mouseCount >= SENSITIVITY_COEFFICIENT && isAccelerate) {
            mouseCount = 0;
            return false;
        } else {
            return false;
        }
    }

    /**
     * Fitts law task.
     * Initialize the constants required.
     * Drawing circles around the circumference.
     * We make use of the parametric equation:
     * x1 = Cx + r cos(theta)
     * y1 = Cy + r sin(theta)
     * theta increments every 30 degrees i.e. 12 targets
     **/

    private void drawFitts(Canvas canvas) {
        int centerX = canvasWidth / 2;
        int centerY = canvasHeight / 2;
        float xValue, yValue;
        int angularDistance = 0;
        while (angularDistance < 360) {          //circular angle
            xValue = (float) (centerX + DISTANCE_RADIUS * Math.cos(Math.toRadians(angularDistance)));
            yValue = (float) (centerY + DISTANCE_RADIUS * Math.sin(Math.toRadians(angularDistance)));
            XYPair.add(new Coordinates(xValue, yValue, angularDistance));
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(xValue, yValue, RADIUS_We, mPaint);
            angularDistance += DISTANCE_ANGLE;
        }
    }

    public void setIsAccelerate(boolean value) {
        isAccelerate = value;
    }

    public double getTimeInbetween() {
        double difference;
        long currentEndTime = System.nanoTime();
        if (initialTime == 0) {
            difference = 0;
        } else if (count % 2 == 1){
            difference = (currentEndTime - initialTime) / 1e6;
        }
        else{
            difference = 0;
        }
        initialTime = currentEndTime;
        count++;
        return difference;
    }


    private float getTopCoordinate(float coordinate) {
        return coordinate - (SIZE);
    }

    private float getBottomCoordinate(float coordinate) {
        return coordinate + (SIZE);
    }

    public void mouseMove(final float xPos, final float yPos) {
        offsetX = xPos;
        offsetY = yPos;
        mHandler.post(thread);
        if (isFinished)
            UDPServer.kill();
    }

    public int getCanvasWidth() {
        return canvasWidth;
    }

    public int getCanvasHeight() {
        return canvasHeight;
    }

    public void setIsTapped(boolean value) {
        isTapped = value;
    }


    public boolean isSelected(int order, float x, float y) {
        if (order != -1) {
            float dx = XYPair.get(order).getX() - x;
            float dy = XYPair.get(order).getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            return distance < RADIUS_We + SIZE;
        }
        return false;
    }


    public void setIsScrolling(boolean value) {
        isScrolling = value;
    }

    public Coordinates hasIntersected(float x, float y, float radiusOne, float radiusTwo) {
        for (Coordinates xy : XYPair) {
            float dx = xy.getX() - x;
            float dy = xy.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < (radiusOne + radiusTwo)) {
                return xy;
            }
        }
        return null;
    }

    private int getOrder(int order) {
        double currentTime = System.nanoTime();
        switch (order) {
            case -1:
                return 7;
            case 7:
                return 2;
            case 2:
                return 8;
            case 8:
                return 3;
            case 3:
                return 9;
            case 9:
                return 4;
            case 4:
                return 0;
            case 0:
                return 5;
            case 5:
                return 1;
            case 1:
                return 6;
            default:
                return -1;
        }
    }


    private class Coordinates {
        float x;
        float y;
        double angle;

        Coordinates(float xer, float yer, double angler) {
            x = xer;
            y = yer;
            angle = angler;
        }

        public float getX() {
            return x;
        }

        public float getY() {
            return y;
        }

        public double getAngle() {
            return angle;
        }
    }
}
