package io.smilo.api.config;

import io.smilo.commons.block.BlockParser;
import io.smilo.commons.block.BlockStore;
import io.smilo.commons.block.SmiloChainService;
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
    public BlockHandler blockHandler(SmiloChainService smiloChainService, BlockParser blockParser, INetworkState networkState) {
        return new BlockHandler(smiloChainService, blockParser, networkState);
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
