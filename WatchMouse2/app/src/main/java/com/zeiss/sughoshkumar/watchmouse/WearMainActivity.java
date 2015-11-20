package com.zeiss.sughoshkumar.watchmouse;

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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.zeiss.sughoshkumar.senderobject.SenderObject;

import java.io.IOException;
import java.util.List;

public class WearMainActivity extends WearableActivity{

    private BoxInsetLayout mContainerView;
    public static SensorThread thread;
    public static final String IPAddress = "192.168.43.1";
    public static final int port = 8080;
    public static boolean isConnected = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();

        // set IP address :
        UDPClient client = null;
        try {
            client = new UDPClient(this,IPAddress, port, SenderObject.toBytes(new SenderObject()));
            client.execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("is Connected ? " + isConnected);
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


    static class SensorThread implements Runnable {
        private Context mContext;
        private SensorManager sensorManager = null;
        private SensorEventListener mListener = null;
        private HandlerThread mHandlerThread;
        private SensorFusion sensorFusion;
        private float azimuthValue, rollValue, prevAzimuth, prevRoll;
        private boolean isInit = false;
        private float pitchValue;
        private boolean isListening;
        private static final int SMOOTHING = 8;
        private static final int SENSITIVITY_X = 8;
        private static final int SENSITIVITY_Y = 12;
        private Handler handler;
        private int count;

        SensorThread(Context context){
            mContext = context;
            azimuthValue = rollValue = prevRoll = prevAzimuth = 0.0f;
            pitchValue = 0;
            isListening = false;
            count = 0;
        }


        @Override
        public void run() {
            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            mHandlerThread = new HandlerThread("AccelerometerLogListener");
            sensorFusion = new SensorFusion();
            sensorFusion.setMode(SensorFusion.Mode.FUSION);
            mHandlerThread.start();
            handler = new Handler(mHandlerThread.getLooper());
            mListener = new SensorEventListener() {


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

                private void updateOrientationDisplay() {
                    float valueX;
                    float valueY;
                    if (!isInit){
                        isInit = true;
                        prevRoll = (float) (sensorFusion.getRoll());
                        prevAzimuth = (float) (sensorFusion.getAzimuth());
                        return;
                    }

                    azimuthValue = (float) (sensorFusion.getAzimuth());
                    rollValue = (float) (sensorFusion.getRoll());
                    pitchValue = (float) (sensorFusion.getPitch());

                    valueX = (azimuthValue - prevAzimuth)/SMOOTHING;
                    valueY = (rollValue - prevRoll)/SMOOTHING;

                    if (pitchValue > 72){
                        isListening = true;
                    }

                    if ((valueX > 0.5f || valueY > 0.5f || valueX < -0.5f || valueY < -0.5f) && isListening) {
                        count++;
                        if (count > 2) {
                            System.out.println("X : " + valueX * SENSITIVITY_X + " Y : " + valueY * SENSITIVITY_Y);
                            if (isConnected) {
                                SenderObject object = new SenderObject(
                                        valueX * SENSITIVITY_X * -1, valueY * SENSITIVITY_Y * -1 ,
                                        1, SenderObject.GESTURE_MODALITY);
                                new UDPClient(object).execute();
                            }
                        }
                    }
                    prevAzimuth = azimuthValue;
                    prevRoll = rollValue;
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {
                    //do nothing!
                }
            };
            registerListeners();

        }

        private void registerListeners(){
            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_FASTEST,handler);

            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_FASTEST,handler);

            sensorManager.registerListener(mListener,
                    sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                    SensorManager.SENSOR_DELAY_FASTEST,handler);
        }

        @SuppressWarnings("unused")
        public void cleanThread() {

            //Unregister the listener
            if (sensorManager != null) {
                sensorManager.unregisterListener(mListener);
            }

            if (mHandlerThread.isAlive())
                mHandlerThread.quitSafely();
        }

        public void onPause(){
            if (sensorManager != null)
                sensorManager.unregisterListener(mListener);
            count = 0;
        }

        public void onResume(){
            registerListeners();
            count = 0;
        }
    }

}
