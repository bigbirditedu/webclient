package com.webtest.controller.httpclient;

import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;

/**
 * @Author: zhoupengcheng
 * @Date: 2021/12/6 15:55
 */
@RestController
public class WebcClient {
    private final String url = "http://127.0.0.1:8081";
    HttpClient httpClient = HttpClient.create()
            .responseTimeout(Duration.ofSeconds(3));
    private WebClient webClient = WebClient.builder().baseUrl(url).clientConnector(new ReactorClientHttpConnector(httpClient)).build();

    @GetMapping("webc")
    public Flux<Product> getProductsWebclient() {
        long start = System.currentTimeMillis();
        Flux<Product> productFlux = webClient.get().uri("/products?type=webc").retrieve().bodyToFlux(Product.class);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " webclient cost:" + (end - start));
        return productFlux;
    }
}
