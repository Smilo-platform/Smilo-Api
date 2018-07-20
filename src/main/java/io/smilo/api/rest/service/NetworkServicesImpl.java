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

package io.smilo.api.rest.service;

import io.smilo.api.rest.models.Node;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Service("networkServices")
public class NetworkServicesImpl implements NetworkServices {

    private MessageDigest md;
    private Base64 base64 = new Base64(true);
    private static final Logger LOGGER = Logger.getLogger(NetworkServicesImpl.class);

    private Map<String, Object> response = new HashMap<>();
    private Map<String, ArrayList<Node>> network = new HashMap<>();
    private ArrayList edgesList = new ArrayList();
    private ArrayList<Node> nodesList = new ArrayList<>();

    private String addNetwork;
    private ArrayList ignoreNodes = new ArrayList();

    private int maxNetworkSize = 20;
    private int max2ndNetworkSize = 15;
    private int maxNodes = 200;

    @Autowired
    NetworkServices networkServices;

    public NetworkServicesImpl () {
        try {
            md = MessageDigest.getInstance("SHA-256"); //Initializes md for SHA256 functions to use
        } catch (Exception e) {
            System.out.println(e);
        }
        generateNetwork();
    }

    public Map<String, ArrayList<Node>> getNetwork(String networkName) {
        Map<String, ArrayList<Node>> result = new HashMap<>();
        ArrayList<Node> nodes = new ArrayList<>();
        if (network.containsKey(networkName)){
            nodes.addAll(network.get(networkName));
            result.put(networkName, nodes);
        }
        return result;
    }

    private void generateNetwork(){
        for (int i=maxNodes*2; i>=1; i--){
            // i is id
            Node node = new Node();
            node.setId(i);
            if ((i & 1) == 0){
                node.setLabel(SHA256Short("Node-"+i)+":"+i);
            } else {
                node.setLabel(SHA256Short("Node-"+(i+1))+":"+(i+1));
                edgesList.add(newEdge(i,i+1));
            }
            addNodeToNetwork(node);
        }
        connectingDots();
    }

    public Map<String, Object> getAll() {
        response.put("edges", edgesList);
        response.put("nodes", nodesList);
        response.put("network", network);
        return response;
    }

    private void connectingDots(){
        for (Node node: nodesList) {
            for (Node nodeToConnect: nodesList) {
                if (nodeToConnect.getGroup() == node.getGroup() && nodeToConnect.getId() != node.getId()) {
                    if (nodeToConnect.getId() > node.getId()) {
                        if (!edgesList.contains(newEdge(node.getId(), nodeToConnect.getId()))) {
                            edgesList.add(newEdge(node.getId(), nodeToConnect.getId()));
                        }
                    } else {
                        if (!edgesList.contains(newEdge(nodeToConnect.getId(), node.getId()))) {
                            edgesList.add(newEdge(nodeToConnect.getId(), node.getId()));
                        }
                    }
                }
            }
        }
    }

    private void addNodeToNetwork(Node node){
        addNetwork = null;
        int networkCounter = 1;
        if ((node.getId() & 1) == 0){
            // node First network
            ignoreNodes.clear();
            while (addNetwork == null){
                String name = SHA256Short("Network-" + networkCounter);
                if (network.containsKey(name)){
                    // This network exist, let's find out if there is space to add.
                    if (network.get(name).size() < maxNetworkSize) {
                        // This is a network where space is available, since this is the first network for this node to connect, go ahead!!
                        node.setGroup(networkCounter);
                        node.setNetworkName(name);
                        addNetwork = name;
                    }
                } else {
                    // since this network does not exist we can create this.
                    node.setGroup(networkCounter);
                    node.setNetworkName(name);
                    addNetwork = name;
                }
                networkCounter++;
            }

        } else {
            // node second network
            while (addNetwork == null){
                String name = SHA256Short("Network-" + networkCounter);
                if (network.containsKey(name)){
                    // This network exist, let's find out if there is space to add.
                  if (network.get(name).size() < max2ndNetworkSize) {
                      // This is a network where space is available.
                      node.setGroup(networkCounter);
                      node.setNetworkName(name);
                      // Lets check if this network already contains a node from network 1.
                      addNetwork = name;
                      for (Node n : network.get(name)) {
                          if (ignoreNodes.contains(n.getLabel())){
                              addNetwork = null;
                              break;
                          }
                      }
                  }
                } else {
                    // since this network does not exist we can create this.
                    node.setGroup(networkCounter);
                    node.setNetworkName(name);
                    addNetwork = name;
                }
                networkCounter++;
            }
        }
        nodesList.add(node);
        newNetwork(addNetwork, node);
        if (network.containsKey(addNetwork)){
            for (Node n : network.get(addNetwork)){
                ignoreNodes.add(n.getLabel());
            }
        }
    }

    private void newNetwork(String networkName, Node node){
        if (network.containsKey(networkName)){
            network.get(networkName).add(node);
        } else {
            ArrayList<Node> nodes = new ArrayList<>();
            nodes.add(node);
            network.put(networkName, nodes);
        }
    }

    private Map<String, Integer> newEdge(int from, int to){
        Map<String, Integer> edge = new HashMap<>();
        edge.put("from", from);
        edge.put("to", to);
        return edge;
    }

    private String SHA256Short(String toHash) //Each hash is shortened to 16 characters based on a 64-character charset. 64^16=79,228,162,514,264,337,593,543,950,336 (Aka more than enough for Lamport)
    {
        try {
            return base64.encodeAsString(md.digest(String.valueOf(toHash).getBytes(UTF_8))).substring(0, 16);
        } catch (Exception e) {
            LOGGER.error("Creating SHA256Short failed!", e);
        }
        return null;
    }
}
