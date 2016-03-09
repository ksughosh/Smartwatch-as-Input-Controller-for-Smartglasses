public class Coordinates{
    float xer;
    float yer;
    double angle;

    /**
     * Base Constructor
     */
    Coordinates(){
        xer = -1;
        yer = -1;
        angle = -1;
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
     * Constructor with no angle
     * @param x value x
     * @param y value y
     */
    Coordinates(float x, float y){
        xer = x;
        yer = y;
        angle = -1;
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
    @SuppressWarnings("unused")
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

    /**
     * compare two coordinate objects
     * @param c coordinates
     * @return true if equal else false
     */
    public boolean isEqual(Coordinates c){
        if (this.xer == c.getX() && this.yer == c.getY() && this.angle == c.getAngle())
            return true;
        else
            return false;
    }
}