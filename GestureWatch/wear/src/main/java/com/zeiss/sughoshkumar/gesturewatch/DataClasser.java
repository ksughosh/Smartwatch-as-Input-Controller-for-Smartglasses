package com.zeiss.sughoshkumar.gesturewatch;

import android.hardware.SensorEvent;
import android.hardware.SensorManager;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.IllegalFormatException;

/**
 * Created by sughoshkumar on 26/08/15.
 */
public class DataClasser implements Serializable {

    float xRot;
    float yRot;
    float zRot;
    int type;

    DataClasser(float x, float y, float z) {
        xRot = x;
        yRot = y;
        zRot = z;
    }

    public void setxRot(float x){
        xRot = x;
    }

    public void setyRot(float y){
        yRot = y;
    }

    public void setzRot(float z){
        zRot = z;
    }

    public static byte[] toBytes(DataClasser object) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(object);
        return b.toByteArray();
    }

    public static DataClasser parseFrom(byte[] array) throws IllegalFormatException, IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(array);
        ObjectInputStream o = new ObjectInputStream(b);
        return ((DataClasser) o.readObject());
    }

    public float getxRot(){
        return xRot;
    }

    public float getyRot(){
        return yRot;
    }

    public float getzRot(){
        return zRot;
    }
}
