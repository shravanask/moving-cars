package com.shravan.project.movingcars.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shravan.project.movingcars.socket.CarWebSocket;
import com.shravan.project.movingcars.util.ServerUtils;

/**
 * Simple bean representing a car. This class extends the {@link Thread}, so by
 * default when the start() method is called will move the car right until it
 * hits the max allowed index boundary and resets from the start of that index
 * again.
 * 
 * @author shravanshetty
 */
public class Car extends Thread {

    private static final Logger log = Logger.getLogger(CarWebSocket.class.getSimpleName());

    /**
     * Shows the current direction in which the car moving
     */
    public enum Direction {
            UP, DOWN, LEFT, RIGHT;

        @JsonCreator
        public static Direction getValue(String direction) {

            for (Direction directionEnum : Direction.values()) {
                if (directionEnum.name().equalsIgnoreCase(direction)) {
                    return directionEnum;
                }
            }
            return null;
        }
    }

    /**
     * Simple constructor creates a new car instance with a {@link Car#carId} as
     * the current total number of cars + 1
     * 
     * @param session
     *            The webSocket session to which the position of the car is
     *            updated
     * @param threadID
     *            The threadid instance to uniquely identify this thread
     * @param xIndex
     *            The initial x-coordinate index of the car in the map
     * @param yIndex
     *            The initial y-coordinate index of the car in the map
     * @param direction
     *            If null, defaults it to {@link Direction#RIGHT}
     */
//    public Car(Session session, ThreadId threadID, Integer xIndex, Integer yIndex, Direction direction) {
//        this.threadId = threadID;
//        this.xIndex = xIndex;
//        this.yIndex = yIndex;
//        this.direction = direction != null ? direction : Direction.RIGHT;
//        //update the previous direction with the given one
//        this.previousDirection = this.direction;
//        this.session = session;
//        this.carId = threadID.getCurrentId();
//    }
    
    public Car(Session session, ThreadId threadId, Integer carId, Integer xIndex, Integer yIndex, Direction direction) {
        this.threadId = threadId;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.direction = direction != null ? direction : Direction.RIGHT;
        //update the previous direction with the given one
        this.previousDirection = this.direction;
        this.session = session;
        this.carId = carId;
    }

    private Integer xIndex;
    private Integer yIndex;
    private Direction direction = Direction.RIGHT;
    @JsonIgnore
    private ThreadId threadId;
    //keep a reference to the threadId as it can trigger a new one in the new thread
    public Integer carId;
    @JsonIgnore
    private Session session;
    /**
     * Maintain a previous direction
     */
    private Direction previousDirection;

    //getters and setters
    public Integer getxIndex() {

        return xIndex;
    }

    public void setxIndex(Integer xIndex) {

        this.xIndex = xIndex;
    }

    public Integer getyIndex() {

        return yIndex;
    }

    public void setyIndex(Integer yIndex) {

        this.yIndex = yIndex;
    }

    public Direction getDirection() {

        return direction;
    }

    public void setDirection(Direction direction) {

        this.direction = direction;
    }

    public ThreadId getThreadID() {

        return threadId;
    }

    public void setThreadID(ThreadId threadID) {

        this.threadId = threadID;
    }

    /**
     * This will move the car from the given co-ordinates ({@link Car#xIndex},
     * {@link Car#yIndex} ) in the direction of {@link Car#direction}
     * incrementally unless stoped
     */
    public void run() {

        String threadGet = String.valueOf(carId);
        log.info("run1" + threadGet);
        synchronized (threadGet.intern()) {
            try {
                //send the first update as it is if there is no change in direction
                if (direction.equals(previousDirection)) {
                    sendToSession();
                }
                while (session != null && session.isOpen()) {
                    //                    //send the first update as it is if there is change in direction
                    //                    if (!direction.equals(previousDirection)) {
                    //                        sendToSession();
                    //                    }
                    switch (direction) {
                        case RIGHT:
                            xIndex++;
                            break;
                        case LEFT:
                            xIndex--;
                            break;
                        case UP:
                            yIndex++;
                            break;
                        case DOWN:
                            yIndex--;
                            break;
                    }
                    Thread.sleep(1000);
                    if (xIndex == threadId.getxMapSize()) {
                        xIndex = 0;
                    }
                    else if (xIndex < 0) {
                        xIndex = threadId.getxMapSize() - 1;
                    }
                    if (yIndex == threadId.getyMapSize()) {
                        yIndex = 0;
                    }
                    else if (yIndex < 0) {
                        yIndex = threadId.getyMapSize() - 1;
                    }
                    log.info("run2: " + carId);
                    log.info(String.format("CarId: %s - (%s, %s) - %s", carId, xIndex, yIndex, direction));
                    //update the socket and send again
                    sendToSession();
                    //update the previous direction to the current one
                    previousDirection = direction;
                }
                log.severe("Socket is either closed or not open");
            }
            catch (InterruptedException ex) {
                log.warning(String.format("Car: %s movement got interupted", carId));
            }
        }
    }

    /**
     * Simple method to send text to the linked session. As multiple cars are
     * linked to the same session, this method has a syncronized access to claim
     * the session and write to it.
     */
    private void sendToSession() {

        synchronized (session) {
            try {
                if (session.isOpen()) {
                    HashMap<String, Object> result = new HashMap<String, Object>();
                    result.put("xIndex", xIndex);
                    result.put("yIndex", yIndex);
                    log.info("sendToSession " + carId);
                    result.put("carId", String.valueOf(carId));
                    result.put("direction", direction);
                    session.getRemote().sendString(ServerUtils.serialize(result));
                }
                else {
                    log.warning(String.format("Socket connection is not open. CarId: %s", carId));
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
