package com.webtest.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author zhou
 */
@RestController
@RequestMapping("/springmvc")
public class SpringMvcController {

    public static AtomicLong COUNT = new AtomicLong(0);

    @GetMapping("/hello/{latency}")
    public String hello(@PathVariable long latency) {
        System.out.println("Start:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        System.out.println("Page count:" + COUNT.incrementAndGet());
        try {
            //阻塞latency秒，模拟处理耗时
            TimeUnit.SECONDS.sleep(latency);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return "Exception during thread sleep";
        }
        System.out.println("End:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        return "welcome to Spring MVC";
    }
}
