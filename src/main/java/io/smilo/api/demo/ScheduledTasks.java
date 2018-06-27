package io.smilo.api.demo;

import io.smilo.api.ws.Websocket;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ScheduledTasks {

    @Autowired
    private Websocket websocket;

    private static final Logger LOGGER = Logger.getLogger(ScheduledTasks.class);

    @Scheduled(fixedRate = 16000)
    public void sendBlock() throws IOException {
        websocket.sendMessage("Test");
    }
}
