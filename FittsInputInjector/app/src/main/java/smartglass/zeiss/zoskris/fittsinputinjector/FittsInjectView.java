package smartglass.zeiss.zoskris.fittsinputinjector;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class FittsInjectView extends SurfaceView implements SurfaceHolder.Callback {
    SurfaceHolder sh;
    ArrayList<Integer> randomTarget;
    private static final int INDEX_OF_DIFFICULTY = 4;
    public static float RADIUS_We;
    private ArrayList<Coordinates> SourceXYPair;
    private ArrayList<Coordinates> TargetXYPair;
    private InjectViewThread thread;
    private Paint mPaint;
    private float x, y, offsetX, offsetY, prevOffsetX, prevOffsetY;
    private static int SIZE = 6;
    private Handler mHandler;
    private boolean isFinished, isInitialized, isTargetSelected;
    private Coordinates currentTarget;
    private int targetCount;
    private int mouseCount = 0;
    private int count = 0;
    private static final int MOUSE_THRESHOLD = 2;
    private static long currentTime = 0;
    private boolean isFirst = false;
    private ArrayList<Double> timeCollection;
    private Coordinates currentPointer;
    private float previousPointerDistance;
    private float currentPointerDistance;
    private Coordinates destination;
    private HashMap<Coordinates, Float> overshootRecord;


    public boolean isTapped, isScrolling;



    public FittsInjectView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
        init();
    }

    private void init(){
        SourceXYPair = new ArrayList<>();
        TargetXYPair = new ArrayList<>();
        randomTarget = new ArrayList<>();
        switch (INDEX_OF_DIFFICULTY) {
            case 4:
                SourceXYPair.add(new Coordinates(40,234));
                SourceXYPair.add(new Coordinates(772.82f, 145));
                SourceXYPair.add(new Coordinates(600,150));
                SourceXYPair.add(new Coordinates(120,400));
                SourceXYPair.add(new Coordinates(130, 250));

                TargetXYPair.add(new Coordinates(640,234));
                TargetXYPair.add(new Coordinates(80,545));
                TargetXYPair.add(new Coordinates(600, 550));
                TargetXYPair.add(new Coordinates(1020, 400));
                TargetXYPair.add(new Coordinates(1089.69f, 592.02f));
                SIZE = 10;
                break;

            case 5:

                SourceXYPair.add(new Coordinates(600,100));
                SourceXYPair.add(new Coordinates(150,560));
                SourceXYPair.add(new Coordinates(60,60));
                SourceXYPair.add(new Coordinates(1050,300));
                SourceXYPair.add(new Coordinates(400, 120));

                TargetXYPair.add(new Coordinates(600,500));
                TargetXYPair.add(new Coordinates(756.21f, 210));
                TargetXYPair.add(new Coordinates(1046.67f, 419.12f));
                TargetXYPair.add(new Coordinates(100, 300));
                TargetXYPair.add(new Coordinates(571.01f, 589.85f));
                SIZE = 10;
                break;

            case 6:
                SourceXYPair.add(new Coordinates(300,200));
                SourceXYPair.add(new Coordinates(150,590));
                SourceXYPair.add(new Coordinates(800,130));
                SourceXYPair.add(new Coordinates(1162.88f,558.27f));
                SourceXYPair.add(new Coordinates(320, 420));

                TargetXYPair.add(new Coordinates(819.61f,500));
                TargetXYPair.add(new Coordinates(680.33f, 59.67f));
                TargetXYPair.add(new Coordinates(800, 580));
                TargetXYPair.add(new Coordinates(30,30));
                TargetXYPair.add(new Coordinates(1120, 420));
                SIZE = 8;
                break;
            default:
                Log.e("INIT-FAILED", "Cannot Initialize Wrong ID", new Throwable("The index of difficulty does not exists"));
                System.exit(1);
                break;
        }
        if (SourceXYPair.size() > 5){
            Log.e("INIT-FAILED", "Cannot Initialize Wrong ID", new Throwable("The index of difficulty does not exists"));
            System.exit(1);
        }
        generateRandomTargets();
        offsetX = offsetY = prevOffsetY = prevOffsetX = 0;
        mHandler = new Handler(Looper.getMainLooper());
        isFinished = isInitialized = isTargetSelected = false;
        isTapped = false;
        timeCollection = new ArrayList<>();
        targetCount = 0;
        previousPointerDistance = 0;
        currentPointerDistance = 0;
        overshootRecord = new HashMap<>();
    }


    /**
     * generate random targets
     */

    private void generateRandomTargets(){
        for (int i = 0; i < 5; i++){
            randomTarget.add(i);
        }
        //Collections.shuffle(randomTarget, new Random());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new InjectViewThread(sh, this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(0.5f);
        mPaint.setColor(Color.RED);
        thread.start();
    }

    private double getRadius(int index){
        double distance = Math.sqrt(Math.pow((TargetXYPair.get(index).getX()-SourceXYPair.get(index).getX()),2) +
                Math.pow((TargetXYPair.get(index).getY() - SourceXYPair.get(index).getY()),2));
        return (distance/Math.pow(2, INDEX_OF_DIFFICULTY));
    }

    /**
     * @param value : boolean value for tapping
     */
    public void setIsTapped(boolean value){
        isTapped = value;
        mHandler.post(thread);
    }

    /**
     * @param value : boolean value for scrolling
     */
    public void setIsScrolling(boolean value){
        isScrolling = value;
    }


    /**
     *
     * @param canvas : draw using the canvas from the inject thread
     */

    public void doDraw(Canvas canvas){
        int canvasHeight = getMeasuredHeight();
        int canvasWidth = getMeasuredWidth();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        int index = -1;

        if (targetCount > 4 && !isFirst) {
            isFinished = true;
            isFirst = true;
            writeTimeAndOvershootToFile();
            printTime();
            System.exit(0);
        }
        else {
            index = randomTarget.get(targetCount);
        }

        if (!isInitialized){
            currentTarget = SourceXYPair.get(index);
            RADIUS_We = (float) getRadius(targetCount);
            x = canvasWidth/2;
            y= canvasHeight/2;
            currentPointer = new Coordinates(x,y);
            isInitialized = true;
        }

        if (!isFinished) {
            mPaint.setColor(Color.YELLOW);
            System.out.println("target : "+ index + " X : " + currentTarget.getX() + " Y : " + currentTarget.getY() + " tapped : " + isTapped);
            canvas.drawCircle(currentTarget.getX(), currentTarget.getY(), RADIUS_We, mPaint);
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(30);
            canvas.drawText(String.valueOf(targetCount), currentTarget.getX(), currentTarget.getY(), mPaint);
        }

        if (isOverShooting() && !hasIntersected(x,y,RADIUS_We,SIZE)){
            overshootRecord.put(destination, previousPointerDistance);
        }

        if (hasIntersected(x, y, RADIUS_We, SIZE) && isTapped && !isScrolling && !isFinished){
            ++count;
            if (count %2 == 1){
                currentTime = System.nanoTime();
                currentTarget = TargetXYPair.get(index);
                currentPointer = new Coordinates(x, y);
            }
            else {
                ++targetCount;
                currentPointer = new Coordinates(x, y);
                double differenceTime = (System.nanoTime() - currentTime)/1e6;
                timeCollection.add(differenceTime);
                if (targetCount < 5) {
                    currentTarget = SourceXYPair.get(randomTarget.get(targetCount));
                }
            }
            isTargetSelected = false;
            isTapped = false;
            isScrolling = true;
        }

        // check redundant offset
        if (isRedundantOffset()) {
            offsetX = 0;
            offsetY = 0;
        }

        x -= offsetX;
        y -= offsetY;

        if (hasIntersected(x, y, RADIUS_We, SIZE)) {
            mPaint.setColor(Color.CYAN);
            canvas.drawCircle(currentTarget.getX(), currentTarget.getY(), RADIUS_We, mPaint);
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

    private boolean hasIntersected(float x, float y, float radiusOne, float radiusTwo){
        float dx = currentTarget.getX() - x;
        float dy = currentTarget.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (radiusOne + radiusTwo);
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

    private void printTime(){
        int  count = 1;
        for (double d : timeCollection){
            System.out.println("Time between source " + count + " target " + count + " is " + d);
            count++;
        }
    }
    private void writeTimeAndOvershootToFile(){
        // create a folder
        File baseDir = new File(Environment.getExternalStorageDirectory(), "Fitts");
        if (!baseDir.exists())
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdir();

        // create file that stores the time between targets
        String fileName = "TimeBetweenTargets_" + INDEX_OF_DIFFICULTY + "_";
        int fileCount = 1;
        String ext = ".txt";
        File mFile = new File(baseDir, fileName + fileCount + ext);
        // check if file exists if so create a new version
        while (mFile.exists()){
            fileCount++;
            mFile = new File(baseDir, fileName + fileCount + ext);
        }

        // write time data to file
        FileOutputStream outputStream;
        try{
            outputStream = new FileOutputStream(mFile);
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
            outputWriter.write(timeCollection.toString());
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        fileName = "Overshoot_" + INDEX_OF_DIFFICULTY + "_";
        fileCount = 1;
        mFile = new File(baseDir, fileName + fileCount + ext);
        // check if file exists if so create a new version
        while (mFile.exists()){
            fileCount++;
            mFile = new File(baseDir, fileName + fileCount + ext);
        }

        try{
            outputStream = new FileOutputStream(mFile);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            for (Map.Entry<Coordinates, Float> e : overshootRecord.entrySet()){
                String toWrite = "Coordinates : " + e.getKey().toString() +
                        " Maximum Overshoot Distance : " + e.getValue() + "\n";
                outputStreamWriter.write(toWrite);
            }
            outputStreamWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private boolean isOverShooting(){
        boolean isLeft = false;
        if (count %2 == 1) {
            destination = TargetXYPair.get(randomTarget.get(targetCount));
            if (currentPointer.getX() < destination.getX()) {
                isLeft = true;
            }
        }
        else {
            destination = SourceXYPair.get(randomTarget.get(targetCount));
            if (currentPointer.getX() > destination.getX()){
                isLeft = true;
            }
        }

        if (isLeft) {
            if (x > destination.getX()){
                currentPointerDistance = (float) Math.sqrt(Math.pow(x - destination.getX(),2) + Math.pow(y - destination.getY(),2));
                if (currentPointerDistance > previousPointerDistance){
                    previousPointerDistance = currentPointerDistance;
                }
                return true;
            }
            else
                return false;
        }
        else {
            if (x < destination.getX()) {
                currentPointerDistance = (float) Math.sqrt(Math.pow(x - destination.getX(), 2) + Math.pow(y - destination.getY(), 2));
                if (currentPointerDistance < previousPointerDistance) {
                    previousPointerDistance = currentPointerDistance;
                }
                return true;
            }
            else
                return false;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }


    /**
     * Check redundant offset that needs to be zeroed.
     */

    private boolean isRedundantOffset() {
        if (offsetX == prevOffsetX && offsetY == prevOffsetY)
            mouseCount++;
        if (mouseCount >= MOUSE_THRESHOLD){
            mouseCount = 0;
            return true;
        }
        else
            return false;
    }


    private class Coordinates {
        float xs;
        float ys;

        Coordinates(float xer, float yer) {
            xs = xer;
            ys = yer;
        }

        public float getX() {
            return xs;
        }

        public float getY() {
            return ys;
        }

        public String toString(){
            return " X : "+xs + " Y: " +ys;
        }
    }
}
