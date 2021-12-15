package com.webtest.webclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebClientUtilConfig {

    @Value("${webclient.maxInMemorySize:0}")
    int maxInMemorySize = 0;

    @Value("${webclient.connectTimeout:3000}")
    int connectTimeout = 3000;

    @Value("${webclient.readTimeout:3000}")
    int readTimeout = 3000;

    @Value("${webclient.maxConnections:1000}")
    int maxConnections = 1000;

    @Value("${webclient.maxPendingCount:2000}")
    int maxPendingCount = 2000;

    @Bean
    WebClientUtil webClientUtils() {
        WebClientUtil webClientUtil = new WebClientUtil(maxInMemorySize, connectTimeout, readTimeout, maxConnections, maxPendingCount);
        return webClientUtil;
    }
}
