package io.smilo.api.config;

import io.smilo.api.address.AddressStore;
import io.smilo.api.block.BlockStoreAPI;
import io.smilo.api.block.data.transaction.TransactionAddressStore;
import io.smilo.api.block.data.transaction.TransactionStore;
import io.smilo.api.cache.BlockCache;
import io.smilo.api.cache.BlockDataCache;
import io.smilo.api.peer.payloadhandler.BlockHandlerAPI;
import io.smilo.api.peer.payloadhandler.sport.NetworkState;
import io.smilo.api.ws.Websocket;
import io.smilo.commons.block.BlockParser;
import io.smilo.commons.block.BlockStore;
import io.smilo.commons.db.Store;
import io.smilo.commons.ledger.AddressManager;
import io.smilo.commons.peer.PeerEncoder;
import io.smilo.commons.peer.payloadhandler.*;
import io.smilo.commons.peer.sport.INetworkState;
import io.smilo.commons.pendingpool.PendingBlockDataPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@Configuration
@ComponentScan(basePackages = "io.smilo.commons")
public class CommonsConfig {


    @Bean
    public BlockStore blockStore(Store store) {
        return new BlockStore(store);
    }


    @Bean
    public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
        return new ThreadPoolTaskScheduler();
    }


    @Bean
    public BlockHandlerAPI blockHandlerAPI(PendingBlockDataPool pendingBlockDataPool,
                                        BlockParser blockParser,
                                        BlockStoreAPI blockStore,
                                        NetworkState networkState,
                                        Websocket websocket,
                                        BlockCache blockCache,
                                        BlockDataCache blockDataCache,
                                        TransactionStore transactionStore,
                                        AddressStore addressStore,
                                        TransactionAddressStore transactionAddressStore) {
        return new BlockHandlerAPI(
                pendingBlockDataPool,
                blockParser,
                blockStore,
                networkState,
                websocket,
                blockCache,
                blockDataCache,
                transactionStore,
                addressStore,
                transactionAddressStore);
    }

    @Bean
    public NetworkStateHandler networkStateHandler(INetworkState networkState) {
        return new NetworkStateHandler(networkState);
    }

    @Bean
    public PayloadHandlerProvider payloadHandlerProvider() {
        return new PayloadHandlerProvider();
    }

    @Bean
    public PingHandler pingHandler() {
        return new PingHandler();
    }

    @Bean
    public PongHandler pongHandler() {
        return new PongHandler();
    }

    @Bean
    public RequestIdentifierHandler requestIdentifierHandler(AddressManager addressManager, PeerEncoder peerEncoder) {
        return new RequestIdentifierHandler(addressManager, peerEncoder);
    }


    @Bean
    public RequestNetStateHandler requestNetStateHandler(BlockStore blockStore, PendingBlockDataPool pendingBlockDataPool) {
        return new RequestNetStateHandler(blockStore, pendingBlockDataPool);
    }

    @Bean
    public TransactionHandler transactionHandler(PendingBlockDataPool pendingBlockDataPool) {
        return new TransactionHandler(pendingBlockDataPool);
    }
}
