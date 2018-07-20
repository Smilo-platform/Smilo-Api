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

package io.smilo.api.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import javax.annotation.PreDestroy;

@Configuration
public class ThreadConfig {

    private ThreadPoolTaskExecutor taskExecutor;
    
    @Bean
    public TaskExecutor threadPoolTaskExecutor(@Value("${THREADPOOL.CORESIZE:4}") int coreSize,
                                               @Value("${THREADPOOL.MAXSIZE:4}") int maxSize) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(coreSize);
        executor.setMaxPoolSize(maxSize);
        executor.setThreadNamePrefix("default_task_executor_thread");
        executor.initialize();
        
        this.taskExecutor = executor;
        
        return executor;
    }
    
    @PreDestroy
    public void preDestroy() {
        taskExecutor.shutdown();
    }

}
