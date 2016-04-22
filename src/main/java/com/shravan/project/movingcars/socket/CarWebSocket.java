package com.shravan.project.movingcars.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Logger;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import com.fasterxml.jackson.core.type.TypeReference;
import com.shravan.project.movingcars.model.Car;
import com.shravan.project.movingcars.model.Car.Direction;
import com.shravan.project.movingcars.model.ThreadId;
import com.shravan.project.movingcars.util.ServerUtils;

/**
 * Class to open a websocket with the server
 * 
 * @author shravanshetty
 */
@WebSocket
public class CarWebSocket {

    private static final Logger log = Logger.getLogger(CarWebSocket.class.getSimpleName());
    private static Map<String, Car> allCarThreads;
    private static ThreadId threadId = new ThreadId();
    private static AtomicInteger carID = new AtomicInteger(0);
    public static ThreadLocal<Integer> xMapSize = new ThreadLocal<Integer>();
    public static ThreadLocal<Integer> yMapSize = new ThreadLocal<Integer>();

    /**
     * Simple websocket implementation to add a car at a specific coordinate
     * 
     * @param session
     *            The current socket session
     * @param carPayLoad
     *            The serialized payload having details of where the car is
     *            supposed to be added and which direction it is heading to
     * @throws Exception
     */
    @OnWebSocketMessage
    public void onCarAdd(Session session, String carPayLoad) throws Exception {

        if (session.isOpen()) {
            log.info(String.format("Payload received: %s. address:%s", carPayLoad, session.getLocalAddress()));
            HashMap<String, String> carDetails = ServerUtils.deserialize(carPayLoad, true,
                new TypeReference<HashMap<String, String>>() {
                });
            String carMapIndex = carDetails.get("carMapIndex");
            String carId = carDetails.get("carId");
            String direction = carDetails.get("direction");

            allCarThreads = allCarThreads != null ? allCarThreads : new HashMap<String, Car>();
            //add a new car with the given index
            if (carMapIndex != null && carMapIndex.split(",").length == 2) {
                String[] carCoordinates = carMapIndex.split(",");
                try {
                    int xIndex = Integer.parseInt(carCoordinates[0]);
                    int yIndex = Integer.parseInt(carCoordinates[1]);
                    //increment the carId - initially from 0 to 1
                    carID.incrementAndGet();
                    //add a new car
//                    Car car = new Car(session, threadId, xIndex, yIndex, Direction.getValue(direction));
                    Car car = new Car(session, threadId, carID.get(), xIndex, yIndex, Direction.getValue(direction));
                    String threadName = String.valueOf(carID.get());
                    Thread carThread = new Thread(car, threadName);
                    carThread.start();
                    log.info(String.format("Car started in new thread. name: %s", threadName));
                    allCarThreads.put(threadName, car);
                    log.info(String.format("Car added: %s", threadName));
                }
                catch (Exception e) {
                    e.printStackTrace();
                    session.getRemote().sendString(e.toString());
                }
            }
            //else the request is just to change direction
            else if (carId != null && direction != null && allCarThreads.get(carId) != null) {
                Car car = allCarThreads.get(carId);
                log.info(String.format("Car: %s changed direction: %s", carId, direction));
                car.setDirection(Direction.getValue(direction));
            }
            //just stop the car by default
            else {
                log.info(String.format("Car: %s is interruped", carId));
                Car car = allCarThreads.get(carId);
                if (car != null) {
                    car.interrupt();
                }
            }
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) throws IOException {

        //initialize the map size
        String requestUrl = session.getUpgradeRequest().getRequestURI().toString();
        //default the map to 5x5
        Integer xMapSize = 5;
        Integer yMapSize = 5;
        try {
            HashMap<String, String> queryParams = ServerUtils.getAllQuerParameters(requestUrl);
            xMapSize = Integer.parseInt(queryParams.get("xMapSize"));
            yMapSize = Integer.parseInt(queryParams.get("yMapSize"));
        }
        catch (Exception e) {
            log.severe(String.format("Given map size in query string: %s is invalid. Defaulting to 5x5", requestUrl));
        }
        //update the map size
        threadId.setxMapSize(xMapSize);
        threadId.setyMapSize(yMapSize);
        carID = carID != null ? carID : new AtomicInteger(0);
        log.info(session.getRemoteAddress().getHostString() + " connected!");
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {

        log.info(session.getRemoteAddress().getHostString() + " closed!");
        //clear all the car threads
        for (String carId : allCarThreads.keySet()) {
            log.info(String.format("Interupting car with id: %s", carId));
            if (allCarThreads.get(carId) != null) {
                allCarThreads.get(carId).interrupt();
            }
        }
        //flush the current threadId
//        threadId.remove();
        carID = null;
    }
}
