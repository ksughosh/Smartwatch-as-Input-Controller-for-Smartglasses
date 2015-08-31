package com.zeiss.sughoshkumar.gesturewatch;

/**
 * Created by sughoshkumar on 25/08/15.
 */
public class Vector3f {

    public double x;
    public double y;
    public double z;

    public Vector3f() {
        x = 0.0d;
        y = 0.0d;
        z = 0.0d;
    }

    public Vector3f(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setCoordinates(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static Vector3f getInstance() {
        return new Vector3f();
    }

    public double getPolarRadiusXY() {
        return Math.sqrt(x * x + y * y);
    }


    public double getPolarRadiusXZ() {
        return Math.sqrt(x * x + z * z);
    }

    public double getPolarRadiusYZ() {
        return Math.sqrt(y * y + z * z);
    }

    public double getPolarThetaXY() {
        return Math.toDegrees(Math.atan(y / x));
    }

    public double getPolarThetaXZ() {
        return Math.toDegrees(Math.atan(z / x));
    }

    public double getPolarThetaYZ() {
        return Math.toDegrees(Math.atan(z / y));
    }

    public double getPolarThetaYX() {
        return Math.toDegrees(Math.atan(x / y));
    }

    public double getPolarThetaZX() {
        return Math.toDegrees(Math.atan(x / z));
    }

    public double getPolarThetaZY() {
        return Math.toDegrees(Math.atan(y / z));
    }

}
