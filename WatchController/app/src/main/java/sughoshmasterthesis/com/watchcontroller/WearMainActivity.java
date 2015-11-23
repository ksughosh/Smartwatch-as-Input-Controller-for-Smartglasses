package sughoshmasterthesis.com.watchcontroller;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.wearable.activity.WearableActivity;
import android.support.wearable.view.BoxInsetLayout;
import com.zeiss.sughoshkumar.senderobject.SenderObject;
import java.io.IOException;


public class WearMainActivity extends WearableActivity {


    private BoxInsetLayout mContainerView;
    public static SensorThread thread;
    public static boolean isConnected;
    public static final String ipAddress = "192.168.43.1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();
        new UDPClient(this).execute();
        thread = new SensorThread(this);
        new Thread(thread).start();
        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContainerView.setOnTouchListener(new TouchMouse());
    }

    @Override
    public void onEnterAmbient(Bundle ambientDetails) {
        super.onEnterAmbient(ambientDetails);
        updateDisplay();
    }

    @Override
    public void onUpdateAmbient() {
        super.onUpdateAmbient();
        updateDisplay();
    }

    @Override
    public void onExitAmbient() {
        updateDisplay();
        super.onExitAmbient();
    }

    private void updateDisplay() {
        if (isAmbient()) {
            //noinspection deprecation
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }
    }

    /**
     * Reading sensor values and computing the direction using
     * sensor fusion as a thread. Optimized using threading.
     */

    static class SensorThread implements Runnable {
        private Context mContext;
        public SensorManager sensorManager = null;
        private SensorEventListener mListener = null;
        private HandlerThread mHandlerThread;
        private SensorFusion sensorFusion;
        private static final int SENSITIVITY_X = 12;
        private static final int SENSITIVITY_Y = 15;
        private float azimuthValue, rollValue, prevAzimuth, prevRoll;
        private double pitchValue;
        private boolean isInit = false;
        private boolean isListening, hasStarted, mIsPaused;
        private final Object mIsLock;
        private static final int smoothing = 4;
        private Handler handler;
        private final int START_VALUE = 77;
        private float valueY, valueX;
        int count;


        SensorThread(Context context) {
            mContext = context;
            azimuthValue = rollValue = prevRoll = prevAzimuth = 0.0f;
            pitchValue = 0d;
            isListening = false;
            valueX = valueY = 0;
            hasStarted = false;
            count = 0;
            mIsLock = new Object();
            mIsPaused = false;
        }

        /**
         * Body of the thread to be forked in.
         */

        @Override
        public void run() {
            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            mHandlerThread = new HandlerThread("AccelerometerLogListener");
            sensorFusion = new SensorFusion();
            sensorFusion.setMode(SensorFusion.Mode.FUSION);
            mHandlerThread.start();
            handler = new Handler(mHandlerThread.getLooper());
            synchronized (mIsLock) {
                if (!mIsPaused) {
                    mListener = new SensorEventListener() {

                        /**
                         * Read the sensor values and compute the pitch
                         * and yaw with sensor fusion
                         *
                         * @param event generated by the sensors.
                         */
                        @Override
                        public void onSensorChanged(SensorEvent event) {

                            switch (event.sensor.getType()) {
                                case Sensor.TYPE_ACCELEROMETER:
                                    sensorFusion.setAccel(event.values);
                                    sensorFusion.calculateAccMagOrientation();
                                    break;

                                case Sensor.TYPE_GYROSCOPE:
                                    sensorFusion.gyroFunction(event);
                                    break;

                                case Sensor.TYPE_MAGNETIC_FIELD:
                                    sensorFusion.setMagnet(event.values);
                                    break;
                            }
                            updateOrientationDisplay();
                        }


                        /**
                         * Send the gesture values pitch and yaw
                         * computed with sensor fusion through UDP
                         */
                        private void updateOrientationDisplay() {
                            if (!isInit) {
                                prevRoll = (float) (sensorFusion.getRoll());
                                prevAzimuth = (float) (sensorFusion.getAzimuth());
                                pitchValue = sensorFusion.getPitch();
                                isInit = true;
                                return;
                            }


                            azimuthValue = (float) (sensorFusion.getAzimuth());
                            rollValue = (float) (sensorFusion.getRoll());
                            pitchValue = sensorFusion.getPitch();


                            // implementing a low pass filter
                            valueX = (azimuthValue - prevAzimuth) / smoothing;
                            valueY = (rollValue - prevRoll) / smoothing;

                            if (pitchValue > START_VALUE && !isListening) {
                                isListening = true;
                            }

                            if ((valueX > 0.5f || valueY > 0.5f || valueX < -0.5f || valueY < -0.5f) && isListening) {
                                hasStarted = true;
                                count++;
                                if (count > 2) {
                                    System.out.println("X : " + valueX * SENSITIVITY_X + " Y : " + valueY * SENSITIVITY_Y);
                                    SenderObject object = new SenderObject(
                                            valueX * -1 * SENSITIVITY_X,
                                            valueY * -1 * SENSITIVITY_Y,
                                            1,
                                            SenderObject.GESTURE_MODALITY);
                                    if (isConnected) {
                                        try {
                                            new UDPClient(object.toBytes()).execute();
                                        } catch (IOException e) {
                                            e.printStackTrace();

                                        }
                                    }

                                }
                                prevAzimuth = azimuthValue;
                                prevRoll = rollValue;
                            }
                        }

                        @Override
                        public void onAccuracyChanged(Sensor sensor, int accuracy) {

                        }

                    };
                }else{
                    try {
                        mIsLock.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
            registerListeners();
        }


        /**
         * Method to initialize the sensor listeners
         */
        public void registerListeners(){
            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST, handler);

            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_FASTEST, handler);

            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_FASTEST, handler);
        }

        /**
         * Method to handle thread stop.
         */
        @SuppressWarnings("unused")
        public void cleanThread() {

            //Unregister the listener
            if (sensorManager != null) {
                sensorManager.unregisterListener(mListener);
            }

            if (mHandlerThread.isAlive())
                mHandlerThread.quit();
        }

        /**
         * Method to pause the current thread.
         **/
        public void onPause() {
            synchronized (mIsLock){
                mIsPaused = true;
            }
            count = 0;
        }

        /**
         * Method to resume the current thread.
         */
        public void onResume() {
            synchronized (mIsLock){
                mIsPaused = false;
                mIsLock.notifyAll();
            }
            count = 0;
        }
    }
}
