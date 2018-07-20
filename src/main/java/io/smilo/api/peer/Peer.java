/*
 * Copyright (c) 2018 Smilo Platform B.V.
 *
 * Licensed under the Apache License, Version 2.0 (the “License”);
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an “AS IS” BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.smilo.api.peer;

import org.apache.log4j.Logger;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class Peer implements Runnable {

    private static final Logger LOGGER = Logger.getLogger(Peer.class);
    private Socket socket;
    private String remoteHost;
    private int remotePort;
    private PeerInput peerInput;
    private PeerOutput peerOutput;
    private boolean initialized;

    /**
     * Constructor sets socket
     *
     * @param socket Socket with peer
     */
    public Peer(Socket socket) {
        this.socket = socket;
        this.initialized = false;
        this.remoteHost = socket.getInetAddress().getHostAddress();
        this.remotePort = socket.getPort();
        try {
            this.socket.setKeepAlive(true);
        } catch (SocketException e) {
            LOGGER.error("Could not set keep-Alive on socket" + e);
        }
    }

    public Peer(String host, int port) throws IOException {
        this(new Socket(host, port));
    }

    public Peer() {

    }

    /**
     * As the name might suggest, each PeerThread runs on its own thread. Additionally, each child network IO thread runs on its own thread.
     */
    @Override
    public void run() {
        LOGGER.info("Got connection from " + getIdentifier() + ".");
        peerInput = new PeerInput(socket);
        peerInput.start();
        peerOutput = new PeerOutput(socket);
        initialized = true;
        LOGGER.info("Initialized connection " + getIdentifier());
        peerOutput.run();
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public List<String> readData() {
        if (initialized) {
            return peerInput.readData();
        } else {
            LOGGER.warn("Peer was not yet initialized! Not reading data!");
            return new ArrayList<>();
        }
    }

    public void write(String string) {
        if (initialized) {
            peerOutput.write(string);
        } else {
            LOGGER.warn("Peer was not yet initialized! Not writing data!");
        }
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public void setRemotePort(int remotePort) {
        this.remotePort = remotePort;
    }

    public String getRawPeer() {
        return remoteHost + " " + remotePort;
    }

    public String getIdentifier() {
        return remoteHost + " " + remotePort;
    }

    @PreDestroy
    public void closePeer() {
        LOGGER.warn("Closing peer...");
        try {
            socket.close();
        } catch (IOException e) {
            LOGGER.debug("Nothing to close");
        }
    }
}
