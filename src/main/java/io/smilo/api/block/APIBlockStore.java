package io.smilo.api.block;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.smilo.api.address.AddressStore;
import io.smilo.commons.block.Block;
import io.smilo.commons.block.BlockStore;
import io.smilo.commons.db.Store;
import io.smilo.commons.ledger.AddressManager;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

@Component
public class APIBlockStore extends BlockStore {

    private static final Logger LOGGER = Logger.getLogger(APIBlockStore.class);
    private final Store store;
    private static final String COLLECTION_NAME = "block";
    private final ObjectMapper dataMapper;
    private final AddressManager addressManager;
    private final AddressStore addressStore;

    private long latestBlockHeight = -1;
    private String latestBlockHash = "0000000000000000000000000000000000000000000000000000000000000000";

    //
    public APIBlockStore(Store store, ObjectMapper dataMapper, AddressManager addressManager, AddressStore addressStore) {
        super(store);
        this.addressManager = addressManager;
        this.addressStore = addressStore;
        this.store = store;
        this.dataMapper = dataMapper;
        store.initializeCollection(COLLECTION_NAME);
    }

    /**
     * Initialises the height of the latest block
     *
     * @return Boolean
     */
    public void initialiseLatestBlock() {

        // When there is a block in the BlockStore, load the latest.
        // When there is a balance, do nothing, else set 200M Smilo on balance
        if (blockInBlockStoreAvailable()) {
            LOGGER.info("Loading block from DB...");
            BlockDTO latestBlock = getLatestBlockDTOFromStore();
            latestBlockHeight = latestBlock.getBlockNum();
            latestBlockHash = latestBlock.getBlockHash();
        }
    }

    public BlockDTO getLatestBlockDTOFromStore() {
        Block b = super.getLatestBlockFromStore();
        return BlockDTO.toDTO(b);
    }
}
