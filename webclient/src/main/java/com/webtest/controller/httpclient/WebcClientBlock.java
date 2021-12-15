package com.webtest.controller.httpclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
public class WebcClientBlock {
    private final String url = "http://127.0.0.1:8081";
    private WebClient webClient = WebClient.builder().baseUrl(url).build();

    @GetMapping("blockwebc")
    public List<Product> getProductsWebclient() {
        long start = System.currentTimeMillis();
        Flux<Product> productFlux = webClient.get().uri("/products?type=blockwebc").retrieve().bodyToFlux(Product.class);
        List<Product> productList = productFlux.collectList().block();//阻塞方式获取结果
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " block webclient cost:" + (end - start));
        return productList;
    }
}
