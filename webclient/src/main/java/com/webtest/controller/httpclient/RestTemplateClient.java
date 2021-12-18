package com.webtest.controller.httpclient;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @Author: zhoupengcheng
 * @Date: 2021/12/6 15:55
 */
@RestController
public class RestTemplateClient {
    private final String url = "http://127.0.0.1:8081/products?type=rest";
    private RestTemplate restTemplate = new RestTemplate();

    @GetMapping("rest")
    public List<Product> getProductsRestTemplate() {
        long start = System.currentTimeMillis();
        ResponseEntity<Product[]> resEntity = restTemplate.getForEntity(url, Product[].class);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " restTemplate cost:" + (end - start));
        List<Product> products = Arrays.asList(resEntity.getBody());
        return products;
    }
}
