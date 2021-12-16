package com.webtest.webclient;

import io.netty.channel.ChannelOption;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.DefaultUriBuilderFactory;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * WebClient工具类,可以用于简单的get/post请求,支持线程池、超时时间配置
 */
public class WebClientUtil {

    private WebClient webClient;

    private List<HttpMethod> supports;

    int maxInMemorySize = 0;

    int connectTimeout = 3000;

    int readTimeout = 3000;

    int maxConnections = 1000;

    int maxPendingCount = 2000;

    public WebClientUtil() {

        if (maxInMemorySize <= 0) {
            maxInMemorySize = 10 * 1024 * 1024;//10Mb
        }

        supports = new ArrayList<>();
        supports.add(HttpMethod.GET);
        supports.add(HttpMethod.POST);
        supports.add(HttpMethod.PUT);
        supports.add(HttpMethod.HEAD);
        supports.add(HttpMethod.DELETE);

        this.webClient = builder().build();
    }

    public WebClientUtil(int maxInMemorySize, int connectTimeout, int readTimeout, int maxConnections, int maxPendingCount) {

        if (maxInMemorySize <= 0) {
            maxInMemorySize = 10 * 1024 * 1024;
        }
        this.setMaxInMemorySize(maxInMemorySize);
        this.setConnectTimeout(connectTimeout);
        this.setReadTimeout(readTimeout);
        this.setMaxConnections(maxConnections);
        this.setMaxPendingCount(maxPendingCount);

        supports = new ArrayList<>();
        supports.add(HttpMethod.GET);
        supports.add(HttpMethod.POST);
        supports.add(HttpMethod.PUT);
        supports.add(HttpMethod.HEAD);
        supports.add(HttpMethod.DELETE);

        this.webClient = builder().build();
    }

    private WebClient.Builder builder() {
        ConnectionProvider provider = ConnectionProvider.builder("custom")
                .maxConnections(maxConnections)//注意该值不是越大越好,其默认值只是max(16,cpu核数*2)
                .maxIdleTime(Duration.ofSeconds(20))
                .maxLifeTime(Duration.ofSeconds(60))
                .pendingAcquireTimeout(Duration.ofSeconds(60))
                .evictInBackground(Duration.ofSeconds(120))
                .pendingAcquireMaxCount(maxPendingCount)//等待队列大小,默认是2*max connections
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, connectTimeout)
                .responseTimeout(Duration.ofMillis(readTimeout))
                .compress(true)
                .secure(t -> t.sslContext(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)));//支持https

        DefaultUriBuilderFactory ubf = new DefaultUriBuilderFactory();
        ubf.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.NONE);

        return WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(maxInMemorySize))
                .uriBuilderFactory(ubf)
                .clientConnector(new ReactorClientHttpConnector(httpClient));
    }

    /**
     * 普通get请求
     * @param uri
     * @param headers
     * @param mediaType
     * @return
     */
    public Mono<ResponseEntity<String>> get(String uri, Map<String, String> headers, MediaType mediaType) {
        return invoke(HttpMethod.GET, uri, headers, "", mediaType);
    }

    /**
     * 普通post请求
     * @param uri
     * @param headers
     * @param mediaType
     * @param bodyValue
     * @return
     */
    public Mono<ResponseEntity<String>> post(String uri, Map<String, String> headers, MediaType mediaType, Object bodyValue) {
        return invoke(HttpMethod.POST, uri, headers, bodyValue, mediaType);
    }

    /**
     * CompletableFuture方式的异步调用
     *
     * @param method
     * @param uri
     * @param headers
     * @param bodyValue
     * @return
     */
    public CompletableFuture<ResponseEntity<String>> invokeForFuture(HttpMethod method,
                                                                     String uri,
                                                                     Map<String, String> headers,
                                                                     MediaType mediaType,
                                                                     String bodyValue) {
        return invoke(method, uri, headers, bodyValue, mediaType).toFuture();
    }

    public CompletableFuture<ResponseEntity<String>> getForFuture(String uri, Map<String, String> headers, MediaType mediaType) {
        return invokeForFuture(HttpMethod.GET, uri, headers, mediaType, "");
    }

    public CompletableFuture<ResponseEntity<String>> postForFuture(String uri, Map<String, String> headers, String bodyValue, MediaType mediaType) {
        return invokeForFuture(HttpMethod.POST, uri, headers, mediaType, bodyValue);
    }

    public Mono<ResponseEntity<String>> invoke(HttpMethod method,
                                               String uri,
                                               Map<String, String> headers,
                                               Object bodyValue, MediaType mediaType) {

        if (null == method) {
            return Mono.error(new RuntimeException("method_is_empty"));
        }

        if (!supports.contains(method)) {
            return Mono.error(new RuntimeException("invalid_method"));
        }

        if (!StringUtils.hasText(uri)) {
            return Mono.error(new RuntimeException("uri_is_empty"));
        }

        if (null == bodyValue) bodyValue = "";

        Mono<ResponseEntity<String>> mono = webClient
                .method(method)
                .uri(uri)
                .contentType(mediaType)
                .headers(httpHeaders -> addHeaders(httpHeaders, headers))
                .bodyValue(bodyValue)
                .retrieve()
                .toEntity(String.class);

        return mono;
    }

    void addHeaders(HttpHeaders httpHeaders, Map<String, String> headers) {
        if (null != headers && headers.size() > 0) {
            httpHeaders.setAll(headers);
        }
    }

    public int getMaxInMemorySize() {
        return maxInMemorySize;
    }

    public void setMaxInMemorySize(int maxInMemorySize) {
        this.maxInMemorySize = maxInMemorySize;
    }

    public int getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(int connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public void setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
    }

    public int getMaxConnections() {
        return maxConnections;
    }

    public void setMaxConnections(int maxConnections) {
        this.maxConnections = maxConnections;
    }

    public int getMaxPendingCount() {
        return maxPendingCount;
    }

    public void setMaxPendingCount(int maxPendingCount) {
        this.maxPendingCount = maxPendingCount;
    }
}
