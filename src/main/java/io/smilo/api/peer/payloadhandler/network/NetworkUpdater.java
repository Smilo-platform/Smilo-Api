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
 */

package io.smilo.api.peer.payloadhandler.network;

import io.smilo.api.peer.payloadhandler.sport.NetworkState;
import io.smilo.commons.ledger.AddressManager;
import io.smilo.commons.peer.INetworkUpdater;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 * Handles all network joining and updating
 */
@Component
public class NetworkUpdater implements INetworkUpdater {

    private final static Logger LOGGER = Logger.getLogger(NetworkUpdater.class);

    private final NetworkState networkState;

    private final AddressManager addressManager;

    public NetworkUpdater(
                          NetworkState networkState,

                          AddressManager addressManager) {
        this.networkState = networkState;

        this.addressManager = addressManager;
    }

    public void updateNetworks() {
        //TODO: Implement simpler node connection

//        if (networkState.getNetworks().size() < targetAmountOfNetworks && !networkState.getCatchupMode() && !networkLinker.isLinking()) {
//            LOGGER.info("Target amount of networks ("+ targetAmountOfNetworks + ") not yet reached, looking for new network!");
//            networkLinker.startLinkingProcess();
//        }
//        if (networkState.getNetworks().size() == targetAmountOfNetworks) {
//            networkState.getNetworks().stream()
//                    .filter(network -> network.getNetworkStatus() == NetworkStatus.LINKED)
//                    .filter(network -> !networkJoiner.isJoining(network.getIdentifier(), addressManager.getDefaultAddress()))
//                    .forEach(networkJoiner::becomeFullNode);
//        }
    }
}
