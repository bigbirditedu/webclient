package com.webtest.controller.httpclient;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoupengcheng
 * @Date: 2021/12/6 15:55
 */
@RestController
public class PooledRestTemplateClient {
    private final String url = "http://127.0.0.1:8081/products?type=pooledrest";
    private RestTemplate restTemplate;

    public PooledRestTemplateClient() {
        HttpClient httpClient = HttpClientBuilder.create().setMaxConnTotal(4000)
                .setMaxConnPerRoute(4000).setConnectionTimeToLive(60, TimeUnit.SECONDS)
                .disableAutomaticRetries().evictExpiredConnections()
                .evictIdleConnections(10, TimeUnit.SECONDS).build();
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);
        factory.setConnectTimeout(3000);
        //factory.setReadTimeout(6000);
        factory.setConnectionRequestTimeout(6000);
        restTemplate = new RestTemplate(factory);
        //可以给restTemplate加拦截器，比如对出去的http请求统一加上唯一追踪标识
        restTemplate.setInterceptors(Collections.singletonList(restTemplateInterceptor()));
    }

    public RestTemplateInterceptor restTemplateInterceptor() {
        return new RestTemplateInterceptor();
    }

    @GetMapping("pooledrest")
    public List<Product> getProductsRestTemplate() {
        long start = System.currentTimeMillis();
        ResponseEntity<Product[]> resEntity = restTemplate.getForEntity(url, Product[].class);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " pooled restTemplate cost:" + (end - start));
        List<Product> products = Arrays.asList(resEntity.getBody());
        return products;
    }
}
