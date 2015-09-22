
import com.zeiss.sughoshkumar.watchmouse.SenderObject;

import java.awt.*;
import java.awt.event.InputEvent;
import java.net.DatagramPacket;
import java.net.DatagramSocket;

/**
 * Created by sughosh kumar on 09/08/15.
 * The server does not need receive a ping message from the client
 * as UDP has unstructured data processing and data from client
 * is not the same as data on server structure wise no packet wise
 */
public class UDPServer {

    public static void main(String [] args) throws Exception{
        DatagramSocket serverSocket = new DatagramSocket(8083);
        int count = 0;
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int middleOfScreenX = (int) Math.round(screenSize.getWidth()/2);
        int middleOfScreenY = (int) Math.round(screenSize.getHeight()/2);

        System.out.println("Server started");
        byte [] receiveData = new byte[94];
        Robot robot = new Robot();
        while (true){
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            serverSocket.receive(receivedPacket);

            SenderObject senderObject = SenderObject.parseFrom(receivedPacket.getData());
            int y = senderObject.getY();
            int x = senderObject.getX();
            int type = senderObject.getType();
            
            //Get the current pointer location.
            Point mousePointer = MouseInfo.getPointerInfo().getLocation();
            
            //Check if the type is mouse click.
            if (type == 2){
                robot.mousePress(InputEvent.BUTTON1_MASK);
                robot.mouseRelease(InputEvent.BUTTON1_MASK);
            }
            // otherwise move the mouse pointer by subtracting to current 
            // coordinates. Subtraction, because of the flipped right and left.
            else {
                int curX = mousePointer.x - x;
                int curY = mousePointer.y - y;
                robot.mouseMove(curX, curY);
            }
        }
    }
}
