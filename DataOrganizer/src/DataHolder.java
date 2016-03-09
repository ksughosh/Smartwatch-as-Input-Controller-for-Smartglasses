public class DataHolder{
    float x;
    float y;
    float relativeTime;
    float startTime;

    DataHolder(float xer, float yer, float relative, float start) {
        x = xer;
        y = yer;
        relativeTime = relative;
        startTime = start;
    }

    public boolean isEqual(DataHolder d) {
        return d != null && (this.x == d.x && this.y == d.y && this.relativeTime == d.getRelativeTime() && this.startTime == d.getStartTime());
    }

    public float getX(){
        return x;
    }

    public float getY(){
        return y;
    }
    public float getRelativeTime(){
        return relativeTime;
    }

    public float getStartTime(){
        return startTime;
    }

    public boolean isXYEqual(DataHolder d){
        return (this.x == d.getX() && this.y == d.getY());
    }

    @Override
    public boolean equals(Object obj) {
        boolean value = false;
        if (obj instanceof DataHolder) {
            DataHolder d = (DataHolder) obj;
            if (this.x == d.getX() && this.y == d.getY())
                value = true;
        }
        return value;
    }

    @Override
    public int hashCode() {
        return String.valueOf(x).hashCode();
    }
}