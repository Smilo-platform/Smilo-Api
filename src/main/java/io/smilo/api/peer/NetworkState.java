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

import io.smilo.api.block.BlockStore;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class NetworkState {

    private final static Logger LOGGER = Logger.getLogger(NetworkState.class);

    private boolean catchupMode = true;
    private int topBlock = 0;

    private BlockStore blockStore;

    public NetworkState(BlockStore blockStore) {
        this.blockStore = blockStore;
        updateCatchupMode();
    }

    /**
     * Checks if the chain in the database has the same length as the top block. If the top block is higher than the database, set catchupMode to true
     */
    public void updateCatchupMode() {
        /*
         * Current chain is shorter than peer chains.
         * Chain starts counting at 0, so a chain height of 100, for example, means there are 100 blocks, and the top block's index is 99.
         * So we need to catch up!
         *
         */
        if (topBlock > blockStore.getBlockchainLength()) {
            LOGGER.info("currentChainHeight: " + blockStore.getBlockchainLength());
            LOGGER.info("topBlock: " + topBlock);
            catchupMode = true;
        } else {
            if (catchupMode) {
                LOGGER.info("Caught up with network."); //Probably won't be seen with block-add spam.
            }
            catchupMode = false;
        }
    }

    public boolean getCachupMode() {
        return catchupMode;
    }

    public int getTopBlock() {
        return topBlock;
    }

    public void setTopBlock(int topBlock) {
        this.topBlock = topBlock;
        updateCatchupMode();
    }
}
