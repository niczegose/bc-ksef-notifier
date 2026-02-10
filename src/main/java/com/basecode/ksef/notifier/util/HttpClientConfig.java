package com.basecode.ksef.notifier.util;

import java.net.ProxySelector;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import lombok.Data;

@Data
public class HttpClientConfig {

    private Duration connectTimeout = Duration.ofSeconds(5);
    private HttpClient.Version version = HttpClient.Version.HTTP_2;
    private HttpClient.Redirect followRedirects = HttpClient.Redirect.NORMAL;
    private ExecutorService executor = ForkJoinPool.commonPool();
    private ProxySelector proxySelector;
}
