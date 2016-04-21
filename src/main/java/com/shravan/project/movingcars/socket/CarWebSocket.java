package com.shravan.project.movingcars.socket;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
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
import com.shravan.project.movingcars.util.JSONFormatter;

/**
 * Class to open a websocket with the server
 * 
 * @author shravanshetty
 */
@WebSocket
public class CarWebSocket {

    private static final Logger log = Logger.getLogger(CarWebSocket.class.getSimpleName());
    private static Map<String, Car> allCarThreads;
    private ThreadId threadId = new ThreadId();

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
            log.info(String.format("Payload received: %s", carPayLoad));
            HashMap<String, String> carDetails = JSONFormatter.deserialize(carPayLoad, true,
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

                    //add a new car
                    Car car = new Car(session, threadId, xIndex, yIndex, Direction.getValue(direction));
                    Thread carThread = new Thread(car, String.valueOf(threadId.get()));
                    carThread.start();
                    allCarThreads.put(String.valueOf(threadId.get()), car);
                    log.info(String.format("Car added: %s", String.valueOf(threadId.get())));
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

        log.info(session.getRemoteAddress().getHostString() + " connected!");
    }

    @OnWebSocketClose
    public void onClose(Session session, int status, String reason) {

        log.info(session.getRemoteAddress().getHostString() + " closed!");
    }
}
