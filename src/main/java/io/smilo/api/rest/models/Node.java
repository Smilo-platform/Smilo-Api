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

package io.smilo.api.rest.models;

import java.util.Objects;

public class Node {

    private int id;
    private String label;
    private int group;
    private String address;
    private String networkName;

    public Node(int id, String label, int group, String address) {
        this.id = id;
        this.label = label;
        this.group = group;
        this.address = address;
    }

    public Node() {
        this.id = 1;
        this.label = "Node1";
        this.group = 1;
        this.address = "127.0.0.1:8080";
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public int getGroup() {
        return group;
    }

    public String getAddress() {
        return address;
    }

    public String getNetworkName() {
        return networkName;
    }

    public void setId(int id){
        this.id = id;
    }

    public void setLabel(String label){
        this.label = label;
    }

    public void setGroup(int group){
        this.group = group;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public void setNetworkName(String networkName){
        this.networkName = networkName;
    }

    @Override
    public boolean equals(Object obj) {
        final Node other = (Node) obj;
        if (Objects.equals(this.label, other.label)) return true;
        return false;
    }

}
