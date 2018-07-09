package io.smilo.api.ws;

import org.springframework.stereotype.Component;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

@Component
@ServerEndpoint("/websocket")
public class Websocket {

    private static Set<Session> clients =
            Collections.synchronizedSet(new HashSet<Session>());

    public void sendMessage(String message)
            throws IOException {

        synchronized(clients){
            // Iterate over the connected sessions
            // and broadcast the received message
            for(Session client : clients){
                client.getBasicRemote().sendText(message);
            }
        }
    }

    @OnOpen
    public void onOpen (Session session) throws IOException {
        // Add session to the connected sessions set
        clients.add(session);
        sendMessage("Welcome tot the Smilo Api!");
    }

    @OnClose
    public void onClose (Session session) {
        // Remove session from the connected sessions set
        clients.remove(session);
    }

}