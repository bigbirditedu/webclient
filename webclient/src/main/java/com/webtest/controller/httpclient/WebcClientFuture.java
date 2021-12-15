package com.webtest.controller.httpclient;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
public class WebcClientFuture {
    private final String url = "http://127.0.0.1:8081";
    private WebClient webClient = WebClient.builder().baseUrl(url).build();

    @GetMapping("blockwebcfuture")
    public List<Product> getProductsWebclient() {
        CompletableFuture<List<Product>> products = getProducts();
        List<Product> productList = null;
        try {
            productList = products.get();//最终需要数据的地方才发起阻塞调用
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    public CompletableFuture<List<Product>> getProducts() {
        long start = System.currentTimeMillis();
        Flux<Product> productFlux = webClient.get().uri("/products?type=webcfuture").retrieve().bodyToFlux(Product.class);
        CompletableFuture<List<Product>> future = productFlux.collectList().toFuture();
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " webclient future cost:" + (end - start));
        return future;
    }
}
