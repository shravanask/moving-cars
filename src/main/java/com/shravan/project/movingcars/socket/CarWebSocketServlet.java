package com.shravan.project.movingcars.socket;

import javax.servlet.annotation.WebServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServlet;
import org.eclipse.jetty.websocket.servlet.WebSocketServletFactory;

/**
 * Simple Servlet to handle websocket connections from the browser
 * @author shravanshetty
 */
@SuppressWarnings("serial")
@WebServlet(urlPatterns = "/carSocket")
public class CarWebSocketServlet extends WebSocketServlet {

    @Override
    public void configure(WebSocketServletFactory factory) {

        factory.register(CarWebSocket.class);
    }
}
