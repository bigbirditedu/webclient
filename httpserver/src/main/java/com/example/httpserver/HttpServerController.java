package com.example.httpserver;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

/**
 * Http服务提供方接口;模拟一个基准的HTTP Server接口
 */
@RestController
public class HttpServerController {

    @RequestMapping("product")
    public Product getAllProduct(String type, HttpServletRequest request, HttpServletResponse response) throws InterruptedException {
        long start = System.currentTimeMillis();
        System.out.println("Start:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));

        //输出请求头
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String head = headerNames.nextElement();
            System.out.println(head + ":" + request.getHeader(head));
        }

        System.out.println("cookies=" + request.getCookies());

        Product product = new Product(type + "A", "1", 56.67);
        Thread.sleep(1000);

        //设置响应头和cookie
        response.addHeader("X-appId", "android01");
        response.addCookie(new Cookie("sid", "1000101111"));
        System.out.println("End:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")));
        System.out.println("cost:" + (System.currentTimeMillis() - start) + product);

        return product;
    }

    @RequestMapping("products")
    public List<Product> getAllProducts(String type) throws InterruptedException {
        long start = System.currentTimeMillis();
        List<Product> products = new ArrayList<>();
        products.add(new Product(type + "A", "1", 56.67));
        products.add(new Product(type + "B", "2", 66.66));
        products.add(new Product(type + "C", "3", 88.88));
        Thread.sleep(1000);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + products);
        return products;
    }

    @RequestMapping("product/{pid}")
    public Product getProductById(@PathVariable String pid, @RequestParam String name, @RequestParam double price) throws InterruptedException {
        long start = System.currentTimeMillis();
        Product product = new Product(name, pid, price);
        Thread.sleep(1000);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + product);
        return product;
    }

    @RequestMapping("postProduct")
    public Product postProduct(@RequestParam String id, @RequestParam String name, @RequestParam double price) throws InterruptedException {
        long start = System.currentTimeMillis();
        Product product = new Product(name, id, price);
        Thread.sleep(1000);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + product);
        return product;
    }

    @RequestMapping("postProduct2")
    public Product postProduct(@RequestBody Product product) throws InterruptedException {
        long start = System.currentTimeMillis();
        Thread.sleep(1000);
        System.out.println("cost:" + (System.currentTimeMillis() - start) + product);
        return product;
    }

    @RequestMapping("uploadFile")
    public String uploadFile(MultipartFile file, int age) throws InterruptedException {
        long start = System.currentTimeMillis();
        System.out.println("age=" + age);
        String filePath = "";
        try {
            String filename = file.getOriginalFilename();
            //String extension = FilenameUtils.getExtension(file.getOriginalFilename());
            String dir = "D:\\files";
            filePath = dir + File.separator + filename;
            System.out.println(filePath);
            if (!Files.exists(Paths.get(dir))) {
                new File(dir).mkdirs();
            }
            file.transferTo(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);
        System.out.println("cost:" + (System.currentTimeMillis() - start));
        return filePath;
    }
}

