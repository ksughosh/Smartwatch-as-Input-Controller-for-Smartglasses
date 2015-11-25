package com.thesis.sughoshkumar.fittsexperiment;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.util.Xml;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import org.xmlpull.v1.XmlSerializer;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;



public class FittsInputInjector extends SurfaceView implements SurfaceHolder.Callback{

    // Define the variables for the experiment.
    private static ArrayList<Coordinates> XYPair;
    private float INDEX_OF_DIFFICULTY;
    private HashMap<Coordinates, Double> XYPathBetweenTargets;
    private File baseDir;
    private Coordinates currentTarget;
    private int exCount;
    private Handler mHandler;
    private Paint mPaint;
    private int mouseCount;
    private float offsetX;
    private float offsetY;
    private float prevOffsetX;
    private float prevOffsetY;
    private Coordinates prevTarget;
    private float radiusOfTargets;
    private SurfaceHolder sh = getHolder();
    private int targetCount;
    private InjectViewThread thread;
    private ArrayList<Double> timeBetweenTargets;
    private double timeStart;
    private Vibrator vibrate;
    private long vibratePattern;
    private float x,y;
    private ArrayList<Float> IDs;
    private File XYPathFile;
    private float prevX, prevY;
    private StringWriter writer;
    private XmlSerializer xmlSerializer;
    private double timeOfXYPath;
    private int xmlCount;



    //Define the constants
    private static final int ANGLE_OF_NEXT_TARGET = 36;
    private static final int DISTANCE_BETWEEN_TARGETS = 600;
    private static final int MOUSE_THRESHOLD = 2;
    private static final int NUMBER_OF_TARGETS = 10;
    private static final int NUMBER_OF_TRIALS = 2;
    private static final int SIZE_OF_POINTER = 8;
    private static final float PERCENT = 0.4f;


    // Define boolean switches
    private boolean isFinished;
    private boolean isInit;
    private boolean isScrolling;
    private boolean isTapped;
    private boolean hasStarted;

    /**
     * constructor
     * @param context application context
     */

    FittsInputInjector(Context context){
        super (context);
        sh = getHolder();
        sh.addCallback(this);
        vibrate = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mHandler = new Handler(Looper.getMainLooper());
        init();
    }

    /**
     * Initialization function
     */
    private void init()
    {
        XYPair = new ArrayList<>();
        XYPathBetweenTargets = new HashMap<>();
        timeBetweenTargets = new ArrayList<>();
        targetCount = 0;

        y = 0.0F;
        x = 0.0F;

        offsetY = 0.0F;
        offsetX = 0.0F;

        prevOffsetY = 0.0F;
        prevOffsetX = 0.0F;

        mouseCount = 0;
        exCount = 0;
        xmlCount = 0;
        timeOfXYPath = 0;

        isScrolling = true;
        isInit = false;
        isTapped = false;
        isFinished = false;
        hasStarted = false;
        hasStarted = false;

        timeStart = 0.0D;
        vibratePattern = 50L;

        randomizeIDs();
        INDEX_OF_DIFFICULTY = IDs.get(exCount);
        computeRadius();
        writer = new StringWriter();
        xmlSerializer = Xml.newSerializer();
        try {
            xmlSerializer.setOutput(writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        createDirectories();
    }

    /**
     * Method to generate random order for IDs
     */
    private void randomizeIDs(){
        float i = 3.5f;
        IDs = new ArrayList<>();
        while (i <= 4.5f){
            IDs.add(i);
            i += 0.5f;
        }
        Collections.shuffle(IDs);
    }

    /**
     * Method to check, verify and create
     *the directories
     */
    private void createDirectories(){
        File foundation = new File(Environment.getExternalStorageDirectory(), "Fitts");
        if (!foundation.exists())
            //noinspection ResultOfMethodCallIgnored
            foundation.mkdir();
        int userId = UDPServer.getUserId();
        boolean isCreated = false;
        File baseFolder = new File(foundation, "User_"+String.valueOf(userId));
        while (baseFolder.exists()){
            File[] contents = baseFolder.listFiles();
            if (contents.length > 3){
                baseFolder = new File(foundation, "User_" + String.valueOf(++userId));
            }
            else {
                isCreated = true;
                break;
            }
        }
        if (!isCreated)
            //noinspection ResultOfMethodCallIgnored
            baseFolder.mkdir();

        baseDir = new File(baseFolder, UDPServer.modalityToString()+"Modality");
        if (!baseDir.exists())
            //noinspection ResultOfMethodCallIgnored
            baseDir.mkdir();
        createXMLFiles();
    }

    /**
     * Method to create the xml files for the path data
     * and initializing the XMLSerializer object.
     */
    private void createXMLFiles(){
        // create the xml files
        File mFolder = new File(baseDir, "XYPATH");
        if (!mFolder.exists())
            //noinspection ResultOfMethodCallIgnored
            mFolder.mkdir();
        String pathFile = "XYPath_"+INDEX_OF_DIFFICULTY + "_";
        int fileCount = 0;
        String xmlExt = ".xml";
        XYPathFile = new File (mFolder, pathFile + fileCount + xmlExt);
        while (XYPathFile.exists()){
            ++fileCount;
            XYPathFile = new File (mFolder, pathFile + fileCount + xmlExt);
        }
        try {
            xmlSerializer.startDocument("UTF-8", true);
            xmlSerializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
            xmlSerializer.startTag(null, "BLOCK");
            xmlSerializer.comment("Beginning of the block");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Setter for index of difficulty
     */
    private void setID(){
        ++exCount ;
        if (exCount > NUMBER_OF_TRIALS) {
            INDEX_OF_DIFFICULTY = 0;
            isFinished = true;
        }
        else
            INDEX_OF_DIFFICULTY = IDs.get(exCount);
        computeRadius();
        createXMLFiles();
    }

    /**
     * Compute the radius for each ID
     */
    private void computeRadius()
    {
        radiusOfTargets = ((float)(600.0D / (Math.pow(2.0D, INDEX_OF_DIFFICULTY) - 1.0D)) / 2.0F);
    }

    /**
     * Set scrolling switch
     * @param paramBoolean true or false for the scrolling switch.
     */
    public void setIsScrolling(boolean paramBoolean)
    {
        isScrolling = paramBoolean;
    }

    /**
     * Set tapping switch
     * @param paramBoolean boolean switch
     */
    public void setIsTapped(boolean paramBoolean)
    {
        isTapped = paramBoolean;

        // redraw
        mHandler.post(this.thread);
    }

    /**
     * Main drawing function
     * @param canvas object to draw
     */
    public void doDraw(Canvas canvas){
        int canvasWidth = getMeasuredWidth();
        int canvasHeight = getMeasuredHeight();
        canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
        // draw the fitts law
        drawFittsLaw(canvas);

        // Check if the experiment block has come to an end
        if (targetCount >= NUMBER_OF_TARGETS){
            if (exCount > NUMBER_OF_TRIALS){
                isFinished = true;
            }
            else{
                writeDataToFiles();
                XYPair = new ArrayList<>();
                drawFittsLaw(canvas);
                XYPathBetweenTargets = new HashMap<>();
                timeBetweenTargets = new ArrayList<>();
                targetCount = 0;
                isInit = false;
            }
            writeFinishToPathFiles();
            setID();
        }

        // Upon first Initialization
        if (!isInit){
            currentTarget = getOrder(new Coordinates());
            x = canvasWidth/2;
            y = canvasHeight/2;
            isInit = true;
        }

        // Keep drawing the target until not finished
        if (!isFinished){
            mPaint.setColor(Color.RED);
            canvas.drawCircle(currentTarget.getX(), currentTarget.getY(), radiusOfTargets, mPaint);
        } else{
            writeArrayToFile();
        }

        // has the experiment started ?
        if (!hasStarted) {
            timeOfXYPath = System.nanoTime();
        }
        // start recording the path after first target acquisition
        if (targetCount > 0 && (x != prevX || y != prevY)){
            XYPathBetweenTargets.put(new Coordinates(x, y), (System.nanoTime() - timeOfXYPath)/1e6);
            timeOfXYPath = System.nanoTime();
        }


        // check and draw in blue if object is selected
        if (isSelect()!= null) {
            mPaint.setColor(Color.BLUE);
            canvas.drawCircle(isSelect().getX(), isSelect().getY(), radiusOfTargets, mPaint);
        }


        // Handle what happens on tap
        if (isSelectedTarget(currentTarget) && isTapped && !isScrolling){
            // vibration feedback
            vibrate.vibrate(vibratePattern);
            targetCount ++;
            if (targetCount > 0)
                hasStarted = true;

            //get previous target
            prevTarget = currentTarget;

            // get the next target
            currentTarget = getOrder(currentTarget);

            // write the time between targets into the array
            if (timeStart != 0)
                timeBetweenTargets.add((System.nanoTime() - timeStart)/1e6);
            timeStart = System.nanoTime();

            // write the path information into the file
            if (targetCount > 1)
                writeArrayToFile();
        }

        // check if offsets are redundant
        if (isRedundantOffset()) {
            offsetX = 0;
            offsetY = 0;
        }

        x -= offsetX;
        y -= offsetY;


        // draw the pointer / cursor
        if (getTopCoordinate(x) <= 0)
            x = SIZE_OF_POINTER;
        else if (getBottomCoordinate(x) >= canvasWidth)
            x = canvasWidth - SIZE_OF_POINTER;

        if (getTopCoordinate(y) <= 0)
            y = SIZE_OF_POINTER;
        else if (getBottomCoordinate(y) >= canvasHeight)
            y = canvasHeight - SIZE_OF_POINTER;

        // drawing the pointer.
        mPaint.setColor(Color.GREEN);
        canvas.drawCircle(x, y, SIZE_OF_POINTER, mPaint);
        mPaint.setColor(Color.RED);
        canvas.drawCircle(x, y, SIZE_OF_POINTER / 2, mPaint);
        mPaint.setColor(Color.BLUE);
        mPaint.setStrokeWidth(2);
        canvas.drawLine(x - SIZE_OF_POINTER, y, x + SIZE_OF_POINTER, y, mPaint);
        canvas.drawLine(x, y - SIZE_OF_POINTER, x, y + SIZE_OF_POINTER, mPaint);
        prevOffsetX = offsetX;
        prevOffsetY = offsetY;
        prevX = x;
        prevY = y;
    }

    /**
     * Drawing the fitts law task
     * @param canvas object to draw
     */
    private void drawFittsLaw(Canvas canvas){
        int centerX = getMeasuredWidth() / 2;
        int centerY = getMeasuredHeight() / 2;
        float xValue, yValue;
        int angularDistance = 0;
        while (angularDistance < 360) {
            //circular angle
            xValue = (float) (centerX + DISTANCE_BETWEEN_TARGETS/2 * Math.cos(Math.toRadians(angularDistance)));
            yValue = (float) (centerY + DISTANCE_BETWEEN_TARGETS/2 * Math.sin(Math.toRadians(angularDistance)));
            XYPair.add(new Coordinates(xValue, yValue, angularDistance));
            mPaint.setColor(Color.WHITE);
            canvas.drawCircle(xValue, yValue, radiusOfTargets, mPaint);
            angularDistance += ANGLE_OF_NEXT_TARGET;
        }
    }

    /**
     * Boundary condition
     * @param coordinate current position
     * @return corrected position according to the screen dimensions
     */
    private float getTopCoordinate(float coordinate) {
        return coordinate - (SIZE_OF_POINTER);
    }

    /**
     * Boundary condition
     * @param coordinate current position
     * @return corrected position according to the screen dimensions
     */
    private float getBottomCoordinate(float coordinate) {
        return coordinate + (SIZE_OF_POINTER);
    }

    /**
     * Get the next target
     * @param current target
     * @return next target after the current
     */
    private Coordinates getOrder(Coordinates current){
        Coordinates target = null;
        int targetAdd;
        if (current.getAngle() + 180 < 360)
            targetAdd = 180;
        else
            targetAdd = -180;

        if (targetCount == 0) {
            for (Coordinates c : XYPair)
                for (int i = 269; i >= (270 - ANGLE_OF_NEXT_TARGET); i--)
                    if (c.getAngle() == i)
                        target = c;

        }
        else if (targetCount % 2 == 1){
            for (Coordinates c : XYPair)
                if (c.getAngle() == current.getAngle() + targetAdd)
                    target = c;

        }
        else {
            for (Coordinates c : XYPair){
                double a = (current.getAngle() + targetAdd + ANGLE_OF_NEXT_TARGET == 360)? 0
                        : current.getAngle() + targetAdd + ANGLE_OF_NEXT_TARGET;
                if (c.getAngle() == a)
                    target = c;
            }
        }
        assert target != null;
        return target;
    }

    /**
     * Check if the objects are selected
     * @return coordinates of the selected object.
     */
    private Coordinates isSelect() {
        Coordinates coordinate = null;
        float threshold = PERCENT * radiusOfTargets;
        for (Coordinates xy : XYPair) {
            float dx = xy.getX() - x;
            float dy = xy.getY() - y;
            double distance = Math.sqrt(dx * dx + dy * dy);
            if (distance < (radiusOfTargets - threshold + SIZE_OF_POINTER)){
               coordinate = xy;
            }

        }
        return coordinate;
    }

    /**
     * Check intersection with the current target
     * @param current target
     * @return true if selected else false
     */
    private boolean isSelectedTarget(Coordinates current){
        float threshold = PERCENT * radiusOfTargets;
        float dx = current.getX() - x;
        float dy = current.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return (distance < (radiusOfTargets - threshold + SIZE_OF_POINTER));
    }

    /**
     * Check for repeating offset values
     * @return true if repeating else false
     */
    private boolean isRedundantOffset(){
        if (offsetX == prevOffsetX && offsetY == prevOffsetY)
            mouseCount++;
        if (mouseCount >= MOUSE_THRESHOLD) {
            mouseCount = 0;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Control the pointer movement
     * @param xPos x offset added to current x
     * @param yPos y offset added to current y
     */
    public void mouseMove(final float xPos, final float yPos) {
        offsetX = xPos;
        offsetY = yPos;
        mHandler.post(thread);
        if (isFinished)
            UDPServer.kill();
    }

    /**
     * Method to write the time values onto a file
     */
    private void writeDataToFiles(){
        String filename = "TimeBetweenTargets_" + INDEX_OF_DIFFICULTY + "_";
        int fileCount = 1;
        String ext = ".txt";
        File mFile = new File(baseDir, filename + fileCount + ext);
        // check if file exists if so create a new version
        while (mFile.exists()){
            fileCount++;
            mFile = new File(baseDir, filename + fileCount + ext);
        }

        // write time data to file
        FileOutputStream outputStream;
        try{
            outputStream = new FileOutputStream(mFile, true);
            OutputStreamWriter outputWriter = new OutputStreamWriter(outputStream);
            for (int i = 0 ; i < timeBetweenTargets.size(); i++)
                outputWriter.write("Time Between " + i + " and " + (i+1) + " : " + timeBetweenTargets.get(i) + ", " );
            outputWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Method to finish writing the xml file.
     */
    private void writeFinishToPathFiles(){
        try {
            xmlSerializer.comment("Ending the block");
            xmlSerializer.endTag(null, "BLOCK");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            FileOutputStream fStream = new FileOutputStream(XYPathFile, true);
            fStream.write(writer.toString().getBytes());
            fStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    /**
     * Method to write the path values into the file
     */
    private void writeArrayToFile() {
        ++xmlCount;
        try {
            xmlSerializer.startTag(null, "TargetBlock");
            xmlSerializer.attribute(null, "Source", prevTarget.toString());
            xmlSerializer.attribute(null, "Target", currentTarget.toString());
            xmlSerializer.attribute(null, "Index", String.valueOf(xmlCount));
            for (Map.Entry<Coordinates, Double> e : XYPathBetweenTargets.entrySet()) {
                xmlSerializer.startTag(null, "PointerPosition");
                xmlSerializer.attribute(null, "x", String.valueOf(e.getKey().getX()));
                xmlSerializer.attribute(null, "y", String.valueOf(e.getKey().getY()));
                xmlSerializer.attribute(null, "time", String.valueOf(e.getValue()));
                xmlSerializer.endTag(null, "PointerPosition");
            }
            xmlSerializer.endTag(null, "TargetBlock");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check if the pointer is in the threshold of fine pointing
     * @param x input position in x
     * @param y input position in y
     * @return true to activate fine pointing else false
     */
    public boolean isFinePointing(float x, float y){
        float threshold = 3;
        float dx = currentTarget.getX() - x;
        float dy = currentTarget.getY() - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        return (distance < radiusOfTargets * threshold + SIZE_OF_POINTER);
    }

    /**
     * Super class methods
     * @param holder surface view holder
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread = new InjectViewThread(sh, this);
        mPaint = new Paint();
        mPaint.setStrokeWidth(0.5F);
        mPaint.setColor(Color.RED);
        thread.start();
    }

    /**
     * Super class methods
     * @param holder surface holder
     * @param format changed format
     * @param width changed width
     * @param height changed height
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("Fitts", "Surface Changed");
    }

    /**
     * Super class method
     * @param holder surface holder
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("Fitts", "Surface Destroyed");
    }

    /**
     * Holder class for maintaining the target data
     */
    static class Coordinates{
        float xer;
        float yer;
        double angle;

        /**
         * Constructor
         */
        Coordinates(){
            xer = 0f;
            yer = 0f;
            angle = 0d;
        }

        /**
         * Constructor
         * @param x value x
         * @param y value y
         */
        Coordinates (float x, float y){
            xer = x;
            yer = y;
            angle = 0d;
        }

        /**
         * Constructor
         * @param x value x
         * @param y value y
         * @param angle angular position of the target
         */
        Coordinates (float x, float y, double angle){
            xer = x;
            yer = y;
            this.angle = angle;
        }

        /**
         * getter for x
         * @return x value
         */
        float getX(){
            return xer;
        }

        /**
         * getter for y
         * @return y value
         */
        float getY(){
            return yer;
        }

        /**
         * getter for angle
         * @return angle value
         */
        double getAngle(){
            return angle;
        }

        /**
         * String representation of the object
         * @return string value of the object.
         */
        public String toString(){
            return " X : " + xer + " Y : " + yer + " Angle : "  + angle;
        }
    }


    /**
     * Method included to make it compatible with the
     * provided input.
     * @param event generated event form system
     * @return true if the event is consumed
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        x = event.getX();
        y = event.getY();
        if (isSelectedTarget(currentTarget)) {
            setIsTapped(true);
            setIsScrolling(false);
        }
        mHandler.post(thread);
        return super.onTouchEvent(event);
    }
}