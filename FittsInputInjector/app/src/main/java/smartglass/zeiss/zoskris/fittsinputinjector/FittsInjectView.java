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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Sughosh Krishna Kumar on 05/10/15.
 * This is a work of thesis and therefore an academic work
 * This program is not to be used for any other purpose,
 * other than academics.
 */


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
    private boolean isFinished, isInitialized, isGestureAcquired;
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
    private ArrayList<Double> distances;
    public boolean isTapped, isScrolling;


    /**
     *
     * @param context get application context to draw the view.
     */
    public FittsInjectView(Context context) {
        super(context);
        sh = getHolder();
        sh.addCallback(this);
        init();
    }

    /**
     * Detailed initialization of the variables
     */

    private void init(){
        SourceXYPair = new ArrayList<>();
        TargetXYPair = new ArrayList<>();
        randomTarget = new ArrayList<>();
        switch (INDEX_OF_DIFFICULTY) {
            case 4:
                SourceXYPair.add(new Coordinates(40f,234f));
                SourceXYPair.add(new Coordinates(772.82f, 145f));
                SourceXYPair.add(new Coordinates(600f,150f));
                SourceXYPair.add(new Coordinates(120f,400f));
                SourceXYPair.add(new Coordinates(150f, 250f));

                TargetXYPair.add(new Coordinates(640f,234f));
                TargetXYPair.add(new Coordinates(80f,545f));
                TargetXYPair.add(new Coordinates(600f, 550f));
                TargetXYPair.add(new Coordinates(1020f, 400f));
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
        computeDistance();
        offsetX = offsetY = prevOffsetY = prevOffsetX = 0;
        mHandler = new Handler(Looper.getMainLooper());
        isFinished = isInitialized = false;
        isTapped = false;
        timeCollection = new ArrayList<>();
        targetCount = 0;
        previousPointerDistance = 0;
        currentPointerDistance = 0;
        overshootRecord = new HashMap<>();
    }

    /**
     * Compute the distance between the source and destination
     * targets.
     */

    private void computeDistance (){
        distances = new ArrayList<>();
        for (int i = 0; i < randomTarget.size(); i++){
            int ind = randomTarget.get(i);
            double xDiff = Math.pow((TargetXYPair.get(ind).getX() - SourceXYPair.get(ind).getX()),2);
            double yDiff = Math.pow((TargetXYPair.get(ind).getY() - SourceXYPair.get(ind).getY()),2);
            double distance = Math.sqrt(xDiff + yDiff);
            distances.add(distance);
            System.out.println("Distance : " +i + " " + distance);
        }
    }

    /**
     * generate random targets
     */

    private void generateRandomTargets(){
        for (int i = 0; i < 5; i++){
            randomTarget.add(i);
        }
        Collections.shuffle(randomTarget, new Random());
    }

    /**
     * A callback function
     * @param holder surface holder to initialize the thread.
     */

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new InjectViewThread(sh, this);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setStrokeWidth(0.5f);
        mPaint.setColor(Color.RED);
        thread.start();
    }

    /**
     *
     * @param index index value.
     * @return current radius for the distance and ID
     */

    private double getRadius(int index){
        return (distances.get(index)/Math.pow(2, INDEX_OF_DIFFICULTY));
    }

    /**
     * re draw the canvas
     */

    public void reDraw(){
        mHandler.post(thread);
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
            RADIUS_We = (float) getRadius(targetCount);
            mPaint.setColor(Color.YELLOW);
            canvas.drawCircle(currentTarget.getX(), currentTarget.getY(), RADIUS_We, mPaint);
            mPaint.setColor(Color.RED);
            mPaint.setTextSize(30);
            canvas.drawText(String.valueOf(targetCount), currentTarget.getX(), currentTarget.getY(), mPaint);
        }

        isGestureAcquired = hasIntersected(x, y, RADIUS_We, SIZE);

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

    /**
     * Check if the pointer has collided with the target object.
     * @param x : xPos
     * @param y : yPos
     * @param radiusOne : target radius
     * @param radiusTwo : pointer radius
     * @return : boolean if collided or not
     */
    private boolean hasIntersected(float x, float y, float radiusOne, float radiusTwo){
        float dx = currentTarget.getX() - x;
        float dy = currentTarget.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return distance < (radiusOne + radiusTwo);
    }

    /**
     * @param coordinate current coordinate value
     * @return within border conditions
     */

    private float getTopCoordinate(float coordinate) {
        return coordinate - (SIZE);
    }

    /**
     * @param coordinate current coordinate value
     * @return within border conditions
     */

    private float getBottomCoordinate(float coordinate) {
        return coordinate + (SIZE);
    }


    /**
     * Update the pointer position
     * @param xPos : x position
     * @param yPos : y position
     */
    public void mouseMove(final float xPos, final float yPos) {
        offsetX = xPos;
        offsetY = yPos;
        mHandler.post(thread);
        if (isFinished)
            UDPServer.kill();
    }


    /**
     * print the time between objects
     */
    private void printTime(){
        int  count = 1;
        for (double d : timeCollection){
            System.out.println("Time between source " + count + " target " + count + " is " + d);
            count++;
        }
    }

    /**
     * Write the required data to the files for analysis.
     */

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

    public boolean isGestureAcquired(){
        return isGestureAcquired;
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

    /**
     * To get current radius
     * @return current radius
     */
    public float getTargetWidth(){
        return RADIUS_We * 2;
    }

    /**
     * To get the current distance.
     * @return the current distance between the two targets
     */

    public double getDistance(){
       return distances.get(randomTarget.get(targetCount));
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

    /**
     * Class for organizing every value in form of Coordinate measure.
     */
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
