package com.webtest.controller.httpclient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.List;

/**
 * @Author: zhoupengcheng
 * @Date: 2021/12/6 15:55
 */
@RestController
public class PooledWebcClientFuture {
    private final String url = "http://127.0.0.1:8081";
    private WebClient webClient;

    public PooledWebcClientFuture() {
        this.webClient = getBuilder().build();
    }

    public WebClient.Builder getBuilder() {
        ConnectionProvider provider = ConnectionProvider.builder("test")
                .maxConnections(2000)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                // Set custom max pending requests
                .pendingAcquireMaxCount(4000)
                .build();
        HttpClient httpClient = HttpClient.create(provider)
                .compress(true);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(url);
    }

    @GetMapping("pooledfuturewebc")
    public List<Product> getProductsWebclient() {
        long start = System.currentTimeMillis();
        Flux<Product> productFlux = webClient.get().uri("/products?type=pooledfuturewebc").retrieve().bodyToFlux(Product.class);
        List<Product> productList = productFlux.collectList().block();
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " pooled future webclient cost:" + (end - start));
        return productList;
    }
}
