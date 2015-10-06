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


    private GoogleApiClient client;
    private String nodeId;
    private BoxInsetLayout mContainerView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear_main);
        setAmbientEnabled();
        new Thread(new SensorThread(this)).start();
        client = getGoogleApiClient(this);
        retrieveDeviceNode();

        mContainerView = (BoxInsetLayout) findViewById(R.id.container);
        mContainerView.setOnTouchListener(new TouchMouse(WearMainActivity.this));

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
            mContainerView.setBackgroundColor(getResources().getColor(android.R.color.black));
        } else {
            mContainerView.setBackground(null);
        }
    }

    private GoogleApiClient getGoogleApiClient(Context context){
        return new GoogleApiClient.Builder(context)
                .addApi(Wearable.API)
                .build();
    }

    private void retrieveDeviceNode(){
        new Thread(new Runnable() {
            @Override
            public void run() {
                client.connect();
                NodeApi.GetConnectedNodesResult results = Wearable.NodeApi.getConnectedNodes(client).await();
                List<Node> nodes = results.getNodes();
                nodeId = nodes.get(0).getId();
                client.disconnect();
            }
        }).start();
    }

    private void sendToast(final String message, final byte[] data) {
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                }
            }).start();
        }
    }

    private class SensorThread implements Runnable {
        private Context mContext;
        private SensorManager sensorManager = null;
        private Sensor mSensor;
        private SensorEventListener mListener = null;
        private HandlerThread mHandlerThread;
        private SensorFusion sensorFusion;
        private float azimuthValue, rollValue, prevAzimuth, prevRoll;
        private boolean isInit = false;
        private static final int SENSITIVITY_MULTIPLIER = -10;


        SensorThread(Context context){
            mContext = context;
            azimuthValue = rollValue = prevRoll = prevAzimuth = 0.0f;
        }


        @Override
        public void run() {
            sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
            mHandlerThread = new HandlerThread("AccelerometerLogListener");
            sensorFusion = new SensorFusion();
            sensorFusion.setMode(SensorFusion.Mode.FUSION);
            mHandlerThread.start();
            Handler handler = new Handler(mHandlerThread.getLooper());
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
                    float valueX = 0;
                    float valueY = 0;
                    float prevValueX = 0;
                    float prevValueY = 0;
                    if (!isInit){
                        isInit = true;
                        prevRoll = (float) (sensorFusion.getRoll());
                        prevAzimuth = (float) (sensorFusion.getAzimuth());
                        return;
                    }

                    azimuthValue = (float) (sensorFusion.getAzimuth());
                    rollValue = (float) (sensorFusion.getRoll());

                    valueX = azimuthValue - prevAzimuth;
                    valueY = rollValue - prevRoll;

                    if (valueX > 0.5f || valueY > 0.5f || valueX < -0.5f || valueY < -0.5f) {
                        System.out.println("X : " + valueX + " Y : "  + valueY + " prevX : "
                                +prevAzimuth + " prevY : " +prevRoll);

                        SenderObject object = new SenderObject((valueX - prevValueX) *
                                SENSITIVITY_MULTIPLIER, (valueY - prevValueY) * SENSITIVITY_MULTIPLIER,
                                1, SenderObject.GESTURE_MODALITY);
                        try {
                            sendToast("GESTURE", SenderObject.toBytes(object));
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                         prevValueX = valueX;
                         prevValueY = valueY;
                    }
                    prevAzimuth = azimuthValue;
                    prevRoll = rollValue;
                }

                @Override
                public void onAccuracyChanged(Sensor sensor, int accuracy) {

                }
            };
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

        public void cleanThread() {

            //Unregister the listener
            if (sensorManager != null) {
                sensorManager.unregisterListener(mListener);
            }

            if (mHandlerThread.isAlive())
                mHandlerThread.quitSafely();
        }
    }

    private float convertDoubleToFloat(double x){
        return Float.parseFloat(String.valueOf(x));
    }
}
