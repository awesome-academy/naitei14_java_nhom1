package org.example.foodanddrinkproject.config;

import java.util.concurrent.Executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class AsyncConfig implements AsyncConfigurer {

    private static final Logger logger = LoggerFactory.getLogger(AsyncConfig.class);

    @Bean(name = "asyncExecutor")
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("async-notification-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        
        executor.setRejectedExecutionHandler((r, exec) -> 
            logger.warn("Task rejected from async executor: {}", r)
        );
        
        executor.initialize();
        logger.info("AsyncExecutor initialized with core pool size: {}, max pool size: {}", 
                   executor.getCorePoolSize(), executor.getMaxPoolSize());
        return executor;
    }

    @Override
    public Executor getAsyncExecutor() {
        return asyncExecutor();
    }
}
