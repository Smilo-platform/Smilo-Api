package io.smilo.api.config;

import io.smilo.commons.block.BlockStore;
import io.smilo.commons.db.Store;
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


}
