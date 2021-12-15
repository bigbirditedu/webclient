package com.webtest.webclient;

import com.webtest.controller.httpclient.Product;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.http.Http11SslContextSpec;
import reactor.netty.http.client.HttpClient;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * webclient的api方法代码示例
 * https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html#webflux-client
 * https://segmentfault.com/a/1190000021133071
 */
@RestController
@RequestMapping("/webcapi")
public class WebClientApi {

    @GetMapping("/get")
    public void testGet() {
        //普通get请求,异步非阻塞式处理单个结果对象,自动将响应body映射为对象
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<Product> mono = webClient.get().header("X-merchantId", "T1").retrieve().bodyToMono(Product.class);
        mono.subscribe(System.out::println);

        //直接获取body字符串内容
        Mono<String> monoStr1 = webClient.get().header("X-merchantId", "T2").retrieve().bodyToMono(String.class);
        monoStr1.subscribe(System.out::println);

        //指定base url
        String baseUrl = "http://127.0.0.1:8081";
        WebClient webClient2 = WebClient.create(baseUrl);
        Mono<String> monoStr2 = webClient2.get().uri("product").header("X-merchantId", "T3").retrieve().bodyToMono(String.class);
        monoStr2.subscribe(System.out::println);

        System.out.println("testGet done");
    }

    @GetMapping("/getlist")
    public void testGetList() {
        //异步非阻塞式处理多个结果对象(List)响应
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/products?type=手机");
        Flux<Product> flux = webClient.get().retrieve().bodyToFlux(Product.class);
        flux.subscribe(System.out::println);
        System.out.println("testGetList done");
    }

    @GetMapping("/getheader")
    public void testGetHeader() {
        //上述例子中的bodyToXXX方式是提取http响应的body进行处理,有时需要处理其他响应信息,比如响应头
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<ResponseEntity<Product>> entity = webClient.get().retrieve().toEntity(Product.class);
        entity.subscribe(responseEntity -> {
            System.out.println("HTTP返回码:" + responseEntity.getStatusCode());
            System.out.println("HTTP响应头:" + responseEntity.getHeaders());
            System.out.println("HTTP响应体:" + responseEntity.getBody());
        });

        System.out.println("testGetHeader done");
    }

    @GetMapping("/getheader2")
    public void testGetHeader2() {
        //也可以使用exchangeToMono() and exchangeToFlux()方法处理响应信息,比如响应头,cookie
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<ResponseCookie> cookieMono = webClient.get().exchangeToMono(clientResponse -> {
            System.out.println("HTTP返回码:" + clientResponse.statusCode());
            System.out.println("HTTP响应头:" + clientResponse.headers().asHttpHeaders());
            System.out.println("HTTP响应体(String):" + clientResponse.bodyToMono(String.class));
            System.out.println("HTTP响应体(Product):" + clientResponse.bodyToMono(Product.class));
            System.out.println("cookies:" + clientResponse.cookies());
            ResponseCookie sid = clientResponse.cookies().getFirst("sid");
            System.out.println(sid.getName() + "=" + sid.getValue());
            return Mono.just(sid);
        });
        // 获取到cookie信息保存之后继续请求后端接口,传递cookie信息
        cookieMono.subscribe(responseCookie -> webClient.get().cookie(responseCookie.getName(),
                responseCookie.getValue()).retrieve().bodyToMono(Product.class).subscribe(System.out::println));

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
        Mono<Product> productMono = WebClient.create(uri).get().retrieve().bodyToMono(Product.class);
        productMono.subscribe(System.out::println);
        System.out.println("testGet4Url done");
        //也可以用下列方式
        //webClient.get().uri("http://localhost:8081/product/{id}", 1);
        //webClient.get().uri("http://localhost:8081/product/{p1}/{p2}", "var1", "var2");
    }

    @GetMapping("/post")
    public void testPost() {
        //普通post请求
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<Product> mono = webClient.post().retrieve().bodyToMono(Product.class);
        mono.subscribe(System.out::println);
        System.out.println("testPost done");
    }

    @GetMapping("/form")
    public void testPostForm() {
        //提交form数据
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("name", "xiaomi");
        formData.add("price", "1999");
        formData.add("id", "100001");
        formData.add("xxx", "yyy");
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/postProduct");
        Mono<Product> mono = webClient.post().bodyValue(formData).retrieve().bodyToMono(Product.class);
        mono.subscribe(System.out::println);
        System.out.println("testPostForm done");
    }

    @GetMapping("/postjson")
    public void testPostJson() {
        //提交json字符串
        String jsonStr = "{\"name\":\"华为手机\",\"id\":\"18\",\"price\":1956.67}";
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/postProduct2");
        Mono<Product> mono = webClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(jsonStr).retrieve().bodyToMono(Product.class);
        mono.subscribe(System.out::println);
        System.out.println("postjson done");
    }

    @GetMapping("/postobj")
    public void testPostObj() {
        //提交Object对象自动转json
        Product product = new Product("苹果手机", "110001", 9111.99);
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/postProduct2");
        Mono<Product> mono = webClient.post().contentType(MediaType.APPLICATION_JSON).bodyValue(product).retrieve().bodyToMono(Product.class);
        mono.subscribe(System.out::println);
        System.out.println("postjson done");
    }

    @GetMapping("/uploadfile")
    public void uploadfile() {
        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("age", "100");//额外属性
        builder.part("file", new FileSystemResource("D:\\file01.txt"));
        MultiValueMap<String, HttpEntity<?>> multiValueMap = builder.build();
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/uploadFile");
        Mono<String> mono = webClient.post().bodyValue(multiValueMap).retrieve().bodyToMono(String.class);
        mono.subscribe(System.out::println);
        System.out.println("uploadfile done");
    }

    @GetMapping("/https")
    public void https() {
        String url = "https://www.baifubao.com/callback?cmd=1059&callback=phone&phone=18120168516";
        Http11SslContextSpec http11SslContextSpec = Http11SslContextSpec.forClient();
        HttpClient httpClient = HttpClient.create().secure(spec -> spec.sslContext(http11SslContextSpec));
        WebClient webClient = WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .baseUrl(url).build();
        Mono<String> mono = webClient.get().retrieve().bodyToMono(String.class);
        mono.subscribe(System.out::println);
        System.out.println("https done");
    }

    @GetMapping("/blockget")
    public void testBlockGet() {
        //同步阻塞式处理单个对象响应结果,可以在tomcat服务器环境下使用
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<Product> mono = webClient.get().retrieve().bodyToMono(Product.class);
        Product product = mono.block();
        System.out.println(product);
        System.out.println("testBlockGet done");
    }

    @GetMapping("/blockgetFuture")
    public void testBlockGetFuture() throws ExecutionException, InterruptedException {
        //可以转换为Future，实现同步阻塞式处理单个对象响应结果,
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/product");
        Mono<Product> mono = webClient.get().retrieve().bodyToMono(Product.class);
        Product product = mono.toFuture().get();
        System.out.println(product);
        System.out.println("blockgetFuture done");
    }

    @GetMapping("/blockgetlist")
    public void testBlockGetListTomcat() {
        //同步阻塞式处理返回多个对象响应结果(List),可以在tomcat服务器环境下使用
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/products");
        List<Product> productList = webClient.get().retrieve().bodyToFlux(Product.class).collectList().block();
        System.out.println(productList);
        System.out.println("testBlockGetList done");
    }

    @GetMapping("/blockgetlistFuture")
    public void testBlockGetListFuture() throws ExecutionException, InterruptedException {
        //同步阻塞式处理返回多个对象响应结果(List),可以在tomcat服务器环境下使用
        WebClient webClient = WebClient.create("http://127.0.0.1:8081/products");
        List<Product> productList = webClient.get().retrieve().bodyToFlux(Product.class).collectList().toFuture().get();
        System.out.println(productList);
        System.out.println("testBlockGetList done");
    }
}
