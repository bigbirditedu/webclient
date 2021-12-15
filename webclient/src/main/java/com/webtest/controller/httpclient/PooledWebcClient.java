package com.webtest.controller.httpclient;

import io.netty.channel.ChannelOption;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;

/**
 * @Author: zhoupengcheng
 * https://projectreactor.io/docs/netty/snapshot/reference/index.html#_connection_pool_2
 */
@RestController
public class PooledWebcClient {
    private final String url = "http://127.0.0.1:8081";
    private WebClient webClient;

    public PooledWebcClient() {
        this.webClient = getBuilder().build();
    }

    public WebClient.Builder getBuilder() {
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(1000)//注意该值不是越大越好,其默认值只是max(16,cpu核数*2)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .pendingAcquireMaxCount(4000)//等待队列大小,默认是2*max connections
                .build();
        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
                //.responseTimeout(Duration.ofMillis(6000))
                .compress(true);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(url);
    }

    @GetMapping("pooledwebc")
    public Flux<Product> getProductsWebclient() {
        long start = System.currentTimeMillis();
        Flux<Product> productFlux = webClient.get().uri("/products?type=pooledwebc").retrieve().bodyToFlux(Product.class);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " pooled webclient cost:" + (end - start));
        return productFlux;
    }
}
