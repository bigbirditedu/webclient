package com.webtest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhou
 */
@RestController
@RequestMapping("/webflux")
public class WebFluxController {

    public static AtomicLong COUNT = new AtomicLong(0);

    @GetMapping("/hello/{latency}")
    public Mono<String> hello(@PathVariable long latency) {
        System.out.println("Start:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        System.out.println("Page count:" + COUNT.incrementAndGet());
        Mono<String> res = Mono.just("welcome to Spring Webflux").delayElement(Duration.ofSeconds(latency));//阻塞latency秒，模拟处理耗时
        System.out.println("End:  " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        return res;
    }
}
