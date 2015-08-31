package com.zeiss.sughoshkumar.watchmouse;

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
public class SenderObject implements Serializable {
    public int x;
    public int y;
    public int type;

    /**
     * x : x - position
     * y : y - position
     * type : move or tapping (0 / 1)
     * eventType : touch or gesture (0 / 1)
     */

    /**
     * Default the positions are initialized to 0 and
     * the event type is touch and
     * the type of event is move
     */

    SenderObject(){
        x = 0;
        y = 0;
        type = 1;
    }

    public SenderObject(int xPos, int yPos, int types){
        x = xPos;
        y = yPos;
        type = types;
    }

    //serialize the object
    public static byte[] toBytes(SenderObject object) throws IOException {
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream o = new ObjectOutputStream(b);
        o.writeObject(object);
        return b.toByteArray();
    }

    //deserialize the object
    public static SenderObject parseFrom(byte[] array) throws IllegalFormatException, IOException, ClassNotFoundException {
        ByteArrayInputStream b = new ByteArrayInputStream(array);
        ObjectInputStream o = new ObjectInputStream(b);
        return ((SenderObject) o.readObject());
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getType(){
        return type;
    }
}
