package com.shravan.project.movingcars.model;

import java.io.IOException;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.shravan.project.movingcars.socket.CarWebSocket;

/**
 * Simple bean representing a car. This class extends the {@link Thread}, so by
 * default when the start() method is called will move the car right until it
 * hits the max allowed index boundary and resets from the start of that index
 * again.
 * 
 * @author shravanshetty
 */
public class Car extends Thread {

    private static final Integer xMapSize = 5;
    private static final Integer yMapSize = 5;
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
    public Car(Session session, ThreadId threadID, Integer xIndex, Integer yIndex, Direction direction) {
        this.threadId = threadID;
        this.xIndex = xIndex;
        this.yIndex = yIndex;
        this.direction = direction != null ? direction : Direction.RIGHT;
        this.session = session;
    }

    private Integer xIndex;
    private Integer yIndex;
    private Direction direction = Direction.RIGHT;
    @JsonIgnore
    private ThreadId threadId;
    @JsonIgnore
    private Session session;

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

        try {
            if (session != null && session.isOpen()) {
                while (true) {
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
                    Thread.sleep(2000);
                    if (xIndex == xMapSize) {
                        xIndex = 0;
                    }
                    else if (xIndex == 0) {
                        xIndex = xMapSize;
                    }
                    if (yIndex == yMapSize) {
                        yIndex = 0;
                    }
                    else if (yIndex == 0) {
                        yIndex = yMapSize;
                    }
                    //update the socket
                    sendToSession();
                }
            }
            else {
                log.severe("Socket is either closed or not open");
            }
        }
        catch (InterruptedException ex) {
            log.warning(String.format("Car: %s movement got interupted", threadId.get()));
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
                session.getRemote().sendString(xIndex + ":" + yIndex + "-" + direction);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
