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

import java.util.ArrayList;
import java.util.List;

public class MockPeer extends Peer {

    private List<String> writtenData;
    private List<String> receivedData;

    public MockPeer(String host, int port)  {
        this.writtenData = new ArrayList<>();
        this.receivedData = new ArrayList<>();
        this.setInitialized(true);
        super.setRemoteHost(host);
        super.setRemotePort(port);
    }


    @Override
    public void run() {
        setInitialized(true);
    }

    @Override
    public void write(String data) {
        this.writtenData.add(data);
    }

    @Override
    public List<String> readData() {
        List<String> receivedData = new ArrayList<>(this.receivedData);
        this.receivedData.clear();
        return receivedData;
    }

    public List<String> getWrittenData() {
        return writtenData;
    }

    public void mockReceiveData(String data) {
        this.receivedData.add(data);
    }

    public List<String> getReceivedData() {
        return receivedData;
    }
}
