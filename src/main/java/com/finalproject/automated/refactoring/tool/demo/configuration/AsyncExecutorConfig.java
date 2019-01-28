package com.finalproject.automated.refactoring.tool.demo.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * @author fazazulfikapp
 * @version 1.0.0
 * @since 1 November 2018
 */

@Configuration
@EnableAsync
public class AsyncExecutorConfig {

    private static final String THREAD_NAME_PREFIX = "Demo -";

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor threadPoolTaskExecutor = new ThreadPoolTaskExecutor();

        threadPoolTaskExecutor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        threadPoolTaskExecutor.setCorePoolSize(Integer.MAX_VALUE);
        threadPoolTaskExecutor.initialize();

        return threadPoolTaskExecutor;
    }
}
