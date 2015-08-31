package com.zeiss.sughoshkumar.gesturewatch;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

import android.os.Handler;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by sughoshkumar on 19/08/15.
 */
public class MidAirGestureListener implements SensorEventListener {
    private SensorManager sensorManager;
    private Context appContext;
    // angular speed from gyro.
    private float[] gyro = new float[3];

    // rotation matrix from gyro data
    private float[] gyroMatrix = new float[9];

    // orientation angles from gyro matrix
    private float[] gyroOrientation = new float[3];

    // magnetic field vector
    private float[] magnet = new float[3];

    // accelerometer vector
    private float[] accel = new float[3];

    // orientation angles from accelerometer and magnet
    private float[] accMagOrientation = new float[3];

    // final orientation angles from sensor fusion
    private float[] fusedOrientation = new float[3];

    // accelerometer and magnetometer based rotation matrix
    private float[] rotationMatrix = new float[9];
    private DecimalFormat d = new DecimalFormat("#.##");
    private Timer fuseTimer = new Timer();
    public static final float EPSILON = 0.000000001f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private float timestamp;
    private boolean initState = true;
    public static final int TIME_CONSTANT = 30;
    public static final float FILTER_COEFFICIENT = 0.98f;
    private float[] previousValues = new float[3];
    private static boolean isFirst = true;
    private Handler mHandler;


    // defining constants

    private static int CONNECTION_TIMEOUT_MS = 0;
    public String nodeId;
    GoogleApiClient client;
    TextView mTextView;


    public MidAirGestureListener(Context context) {
        appContext = context;
        client = getGoogleApiClient(appContext);
        retrieveDeviceNode();
        initListeners();
        gyroOrientation[0] = 0.0f;
        gyroOrientation[1] = 0.0f;
        gyroOrientation[2] = 0.0f;

        // initialise gyroMatrix with identity matrix
        gyroMatrix[0] = 1.0f; gyroMatrix[1] = 0.0f; gyroMatrix[2] = 0.0f;
        gyroMatrix[3] = 0.0f; gyroMatrix[4] = 1.0f; gyroMatrix[5] = 0.0f;
        gyroMatrix[6] = 0.0f; gyroMatrix[7] = 0.0f; gyroMatrix[8] = 1.0f;
        fuseTimer.scheduleAtFixedRate(new BackgroundFuseTask(),
                1000, TIME_CONSTANT);
        d.setRoundingMode(RoundingMode.HALF_UP);
        d.setMaximumFractionDigits(3);
        d.setMinimumFractionDigits(3);
        mHandler = new Handler();
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
                if (nodes.size() > 0 ){
                    nodeId = nodes.get(0).getId();
                }
                client.disconnect();
            }
        }).start();
    }

    private final void sendToast(final String message, final byte[] data) {
        System.out.println("node id : " + nodeId);
        if (nodeId != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    client.connect();
                    Wearable.MessageApi.sendMessage(client, nodeId, message, data);
                }
            }).start();
            System.out.println("sent to data layer");
        }
    }


    private void initListeners() {
        sensorManager = (SensorManager) appContext.getSystemService(appContext.SENSOR_SERVICE);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                SensorManager.SENSOR_DELAY_NORMAL);

        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
                SensorManager.SENSOR_DELAY_NORMAL);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                calcAccelerometerMagnetometerOrientation();
                System.arraycopy(event.values, 0, accel, 0, 3);
                break;
            case Sensor.TYPE_GYROSCOPE:
                gyroFunction(event);
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                System.arraycopy(event.values, 0, magnet, 0, 3);
                break;
        }
    }


    private void calcAccelerometerMagnetometerOrientation(){
        if(SensorManager.getRotationMatrix(rotationMatrix, null, accel, magnet)) {
            SensorManager.getOrientation(rotationMatrix, accMagOrientation);
        }
    }

    private void getRotationVectorFromGyro(float[] gyroValues, float[] deltaRotationVector, float timeFactor)
    {
        float[] normValues = new float[3];

        // Calculate the angular speed of the sample
        float omegaMagnitude =
                (float)Math.sqrt(gyroValues[0] * gyroValues[0] +
                        gyroValues[1] * gyroValues[1] +
                        gyroValues[2] * gyroValues[2]);

        // Normalize the rotation vector if it's big enough to get the axis
        if(omegaMagnitude > EPSILON) {
            normValues[0] = gyroValues[0] / omegaMagnitude;
            normValues[1] = gyroValues[1] / omegaMagnitude;
            normValues[2] = gyroValues[2] / omegaMagnitude;
        }

        // Integrate around this axis with the angular speed by the timestep
        // in order to get a delta rotation from this sample over the timestep
        // We will convert this axis-angle representation of the delta rotation
        // into a quaternion before turning it into the rotation matrix.
        float thetaOverTwo = omegaMagnitude * timeFactor;
        float sinThetaOverTwo = (float)Math.sin(thetaOverTwo);
        float cosThetaOverTwo = (float)Math.cos(thetaOverTwo);
        deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
        deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
        deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
        deltaRotationVector[3] = cosThetaOverTwo;
    }

    private void gyroFunction(SensorEvent event){
        if (accMagOrientation == null){
            return;
        }

        if(initState) {
            float[] initMatrix = new float[9];
            initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
            float[] test = new float[3];
            SensorManager.getOrientation(initMatrix, test);
            gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
            initState = false;
        }

        // get a quaternion of the raw gyro data and
        // convert it to gyro rotation

        float[] deltaVector = new float[4];
        if(timestamp != 0) {
            final float dT = (event.timestamp - timestamp) * NS2S;
            System.arraycopy(event.values, 0, gyro, 0, 3);
            getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
        }

        timestamp = event.timestamp;

        float[] deltaMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
        gyroMatrix = matrixMultiplication (gyroMatrix, deltaMatrix);
        SensorManager.getOrientation(gyroMatrix, gyroOrientation);
    }

    private float[] getRotationMatrixFromOrientation(float[] theta){
        float [] xM = new float[9];
        float [] yM = new float[9];
        float [] zM = new float[9];

        float sinX = (float)Math.sin(theta[1]);
        float cosX = (float)Math.cos(theta[1]);
        float sinY = (float)Math.sin(theta[2]);
        float cosY = (float)Math.cos(theta[2]);
        float sinZ = (float)Math.sin(theta[0]);
        float cosZ = (float)Math.cos(theta[0]);

        // rotation about x-axis (pitch)
        xM[0] = 1.0f; xM[1] = 0.0f; xM[2] = 0.0f;
        xM[3] = 0.0f; xM[4] = cosX; xM[5] = sinX;
        xM[6] = 0.0f; xM[7] = -sinX; xM[8] = cosX;

        // rotation about y-axis (roll)
        yM[0] = cosY; yM[1] = 0.0f; yM[2] = sinY;
        yM[3] = 0.0f; yM[4] = 1.0f; yM[5] = 0.0f;
        yM[6] = -sinY; yM[7] = 0.0f; yM[8] = cosY;

        // rotation about z-axis (azimuth)
        zM[0] = cosZ; zM[1] = sinZ; zM[2] = 0.0f;
        zM[3] = -sinZ; zM[4] = cosZ; zM[5] = 0.0f;
        zM[6] = 0.0f; zM[7] = 0.0f; zM[8] = 1.0f;

        // rotation order is y, x, z (roll, pitch, azimuth)
        float[] resultMatrix = matrixMultiplication(xM, yM);
        resultMatrix = matrixMultiplication(zM, resultMatrix);
        return resultMatrix;
    }

    private float[] matrixMultiplication(float[] matrix_1, float [] matrix_2){
        float[] result = new float[9];

        result[0] = matrix_1[0] * matrix_2[0] + matrix_1[1] * matrix_2[3] + matrix_1[2] * matrix_2[6];
        result[1] = matrix_1[0] * matrix_2[1] + matrix_1[1] * matrix_2[4] + matrix_1[2] * matrix_2[7];
        result[2] = matrix_1[0] * matrix_2[2] + matrix_1[1] * matrix_2[5] + matrix_1[2] * matrix_2[8];

        result[3] = matrix_1[3] * matrix_2[0] + matrix_1[4] * matrix_2[3] + matrix_1[5] * matrix_2[6];
        result[4] = matrix_1[3] * matrix_2[1] + matrix_1[4] * matrix_2[4] + matrix_1[5] * matrix_2[7];
        result[5] = matrix_1[3] * matrix_2[2] + matrix_1[4] * matrix_2[5] + matrix_1[5] * matrix_2[8];

        result[6] = matrix_1[6] * matrix_2[0] + matrix_1[7] * matrix_2[3] + matrix_1[8] * matrix_2[6];
        result[7] = matrix_1[6] * matrix_2[1] + matrix_1[7] * matrix_2[4] + matrix_1[8] * matrix_2[7];
        result[8] = matrix_1[6] * matrix_2[2] + matrix_1[7] * matrix_2[5] + matrix_1[8] * matrix_2[8];

        return result;
    }

    private class BackgroundFuseTask extends TimerTask {

        @Override
        public void run() {
            float oneMinusCoefficient = 1.0f - FILTER_COEFFICIENT;
            // calculate the rotation on z-axis.

            if (gyroOrientation[0] < -0.5 * Math.PI && accMagOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoefficient * accMagOrientation[0]);
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[0] < -0.5 * Math.PI && gyroOrientation[0] > 0.0) {
                fusedOrientation[0] = (float) (FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoefficient * (accMagOrientation[0] + 2.0 * Math.PI));
                fusedOrientation[0] -= (fusedOrientation[0] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[0] = FILTER_COEFFICIENT * gyroOrientation[0] + oneMinusCoefficient * accMagOrientation[0];
            }

            // calculation rotation on x-axis.

            if (gyroOrientation[1] < -0.5 * Math.PI && accMagOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoefficient * accMagOrientation[1]);
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[1] < -0.5 * Math.PI && gyroOrientation[1] > 0.0) {
                fusedOrientation[1] = (float) (FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoefficient * (accMagOrientation[1] + 2.0 * Math.PI));
                fusedOrientation[1] -= (fusedOrientation[1] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[1] = FILTER_COEFFICIENT * gyroOrientation[1] + oneMinusCoefficient * accMagOrientation[1];
            }

            // calculation rotation on y-axis.

            if (gyroOrientation[2] < -0.5 * Math.PI && accMagOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoefficient * accMagOrientation[2]);
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI : 0;
            }
            else if (accMagOrientation[2] < -0.5 * Math.PI && gyroOrientation[2] > 0.0) {
                fusedOrientation[2] = (float) (FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoefficient * (accMagOrientation[2] + 2.0 * Math.PI));
                fusedOrientation[2] -= (fusedOrientation[2] > Math.PI)? 2.0 * Math.PI : 0;
            }
            else {
                fusedOrientation[2] = FILTER_COEFFICIENT * gyroOrientation[2] + oneMinusCoefficient * accMagOrientation[2];
            }
            // removing gyro drift
            gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
            System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
            mHandler.post(updateTextTask);
        }
    }

    private void sendGestureWrapIntoDataLayer() throws IOException {
        double xRot;
        double yRot;
        double zRot;

        byte[] sendCoord = new byte[3];
        if (isFirst){
            isFirst = false;
            previousValues = gyroOrientation.clone();
        }
        else {
            xRot =  (gyroOrientation[1] * 180 / Math.PI) - (previousValues[1] * 180 / Math.PI);
            yRot =  ((gyroOrientation[2] * 180 / Math.PI) - (previousValues[2] * 180 / Math.PI));
            zRot =  ((gyroOrientation[0] * 180 / Math.PI) - (previousValues[0] * 180 / Math.PI));
            DataClasser classes = new DataClasser((float)xRot,(float)yRot,(float)zRot);
            byte[] toSend = DataClasser.toBytes(classes);
            sendToast("", toSend);
            previousValues = gyroOrientation.clone();
        }
    }


    private Runnable updateTextTask = new Runnable() {
        @Override
        public void run() {
            try {
                sendGestureWrapIntoDataLayer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

}
