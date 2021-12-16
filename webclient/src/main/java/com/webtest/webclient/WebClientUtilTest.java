package com.webtest.webclient;

import com.alibaba.fastjson.JSON;
import com.webtest.controller.httpclient.Product;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

/**
 * 使用webclient工具类来实现各种http请求
 */

@RestController
@RequestMapping("/webcutil")
public class WebClientUtilTest {

    @Autowired
    WebClientUtil webClientUtil;

    @GetMapping("/get")
    public Mono<ResponseEntity<String>> testGet() {
        //普通get请求,异步非阻塞式处理单个结果对象,自动将响应body映射为对象
        Map header = new HashMap();
        header.put("X-merchantId", "T3");
        Mono<ResponseEntity<String>> response = webClientUtil.get("http://127.0.0.1:8081/product", header, null);
        response.subscribe(responseEntity -> {
            Product product = JSON.parseObject(responseEntity.getBody(), Product.class);
            System.out.println("转换为对象：" + product);
        });

        //直接获取body字符串内容
        response.subscribe(System.out::println);
        System.out.println("testGet done");
        return response;
    }

    @GetMapping("/getlist")
    public void testGetList() {
        //异步非阻塞式处理多个结果对象(List)响应
        Mono<ResponseEntity<String>> mono = webClientUtil.get("http://127.0.0.1:8081/products?type=phone", null, null);
        mono.subscribe(new Consumer<ResponseEntity<String>>() {
            @Override
            public void accept(ResponseEntity<String> responseEntity) {
                String body = responseEntity.getBody();
                List<Product> products = JSON.parseArray(body, Product.class);
                System.out.println("products:" + products);
            }
        });

        System.out.println("testGetList done");
    }

    @GetMapping("/getheader")
    public void testGetHeader() {
        //处理其他响应信息,比如响应头
        Mono<ResponseEntity<String>> mono = webClientUtil.get("http://127.0.0.1:8081/product", null, null);
        mono.subscribe(responseEntity -> {
            System.out.println("HTTP返回码:" + responseEntity.getStatusCode());
            System.out.println("HTTP响应头:" + responseEntity.getHeaders());
            System.out.println("HTTP响应体:" + responseEntity.getBody());
            Product product = JSON.parseObject(responseEntity.getBody(), Product.class);
            System.out.println("转换为对象：" + product);
        });

        System.out.println("testGetHeader done");
    }

    @GetMapping("/getheader2")
    public void testGetHeader2() {
        //也可以使用exchangeToMono() and exchangeToFlux()方法处理响应信息,比如响应头,cookie
        //webclientutil 可以增加exchange方法，读者可以自行封装
        System.out.println("testGetHeader2 done");
    }

    @GetMapping("/get4url")
    public void testGet4Url() {
        //get请求url携带查询参数,提交form数据
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("name", "小米");
        params.add("price", "1999");
        params.add("xx", "yyy");
        //定义url参数
        Map<String, Object> uriVariables = new HashMap<>();
        uriVariables.put("id", 101);
        String uri = UriComponentsBuilder.fromUriString("http://127.0.0.1:8081/product/{id}")
                .queryParams(params)
                .uriVariables(uriVariables)
                .toUriString();
        System.out.println("uri:" + uri);
        //webclientUtil解决中文url乱码问题
        Mono<ResponseEntity<String>> mono = webClientUtil.get(uri, null, null);
        mono.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("testGet4Url done");
    }

    @GetMapping("/post")
    public void testPost() {
        //普通post请求
        Mono<ResponseEntity<String>> response = webClientUtil.post("http://127.0.0.1:8081/product", null, null, null);
        response.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("testPost done");
    }

    @GetMapping("/form")
    public void testPostForm() {
        //提交form数据
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "小米");
        formData.add("price", "1999");
        formData.add("id", "100001");
        formData.add("xxx", "yyy");
        Mono<ResponseEntity<String>> response = webClientUtil.post("http://127.0.0.1:8081/postProduct", null, null, formData);
        response.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("testPostForm done");
    }

    @GetMapping("/postjson")
    public void testPostJson() {
        //提交json字符串
        String jsonStr = "{\"name\":\"华为手机\",\"id\":\"18\",\"price\":1956.67}";
        Mono<ResponseEntity<String>> response = webClientUtil.post("http://127.0.0.1:8081/postProduct2", null, MediaType.APPLICATION_JSON, jsonStr);
        response.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("postjson done");
    }

    @GetMapping("/postobj")
    public void testPostObj() {
        //提交Object对象自动转json
        Product product = new Product("苹果手机", "110001", 9111.99);
        Mono<ResponseEntity<String>> response = webClientUtil.post("http://127.0.0.1:8081/postProduct2", null, null, product);
        response.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("postjson done");
    }

    @GetMapping("/uploadfile")
    public void uploadfile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("age", "100");//额外属性
        builder.part("file", new FileSystemResource("D:\\file1.txt"));
        MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();
        Mono<ResponseEntity<String>> response = webClientUtil.post("http://127.0.0.1:8081/uploadFile", null, null, multiValueMap);
        response.subscribe(responseEntity -> System.out.println(responseEntity.getBody()));
        System.out.println("uploadfile done");
    }

    @GetMapping("/https")
    public void https() {
        String url = "https://www.baifubao.com/callback?cmd=1059&callback=phone&phone=18120168516";
        Mono<ResponseEntity<String>> response = webClientUtil.get(url, null, null);
        response.subscribe(responseEntity -> {
            System.out.println(responseEntity.getBody());
        });
        System.out.println("https done");
    }

    /**
     * 以同步阻塞方式使用webclient
     * @return
     */
    @GetMapping("/blockgetlist")
    public String testBlockGetListTomcat() {
        //同步阻塞式处理返回多个对象响应结果(List)
        //本接口可用于压测 调试
        long start = System.currentTimeMillis();
        Mono<ResponseEntity<String>> response = webClientUtil.get("http://127.0.0.1:8081/products", null, null);
        ResponseEntity<String> block = response.block();
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " webclientUtil cost:" + (end - start) + " " + block.getBody());
        return block.getBody();
    }

    /**
     * 以CompletableFuture方式使用webclient
     * @return
     */
    @GetMapping("webcfuture")
    public List<Product> webcFuture() {
        CompletableFuture<ResponseEntity<String>> products = get4Products();//发送http请求
        List<Product> productList = new ArrayList<>();
        try {
            ResponseEntity<String> res = products.get();//最终需要数据的地方才发起阻塞调用
            productList = JSON.parseArray(res.getBody(), Product.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return productList;
    }

    public CompletableFuture<ResponseEntity<String>> get4Products() {
        long start = System.currentTimeMillis();
        CompletableFuture<ResponseEntity<String>> future = webClientUtil.getForFuture("http://127.0.0.1:8081/products", null, null);
        long end = System.currentTimeMillis();
        System.out.println(Thread.currentThread().getName() + " webclient future cost:" + (end - start));
        return future;
    }
}
