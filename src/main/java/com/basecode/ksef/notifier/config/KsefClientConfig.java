package com.basecode.ksef.notifier.config;

import com.basecode.ksef.notifier.util.HttpClientBuilder;
import com.basecode.ksef.notifier.util.HttpClientConfig;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.net.http.HttpClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import pl.akmf.ksef.sdk.api.DefaultKsefClient;
import pl.akmf.ksef.sdk.api.services.DefaultCryptographyService;
import pl.akmf.ksef.sdk.api.services.DefaultQrCodeService;
import pl.akmf.ksef.sdk.client.interfaces.KSeFClient;
import pl.akmf.ksef.sdk.client.interfaces.QrCodeService;

@EnableRetry
@Configuration
@RequiredArgsConstructor
public class KsefClientConfig {

    private final DefaultKsefApiProperties apiProperties;

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Bean
    public KSeFClient ksefClient() {
        HttpClient apiClient = HttpClientBuilder.createHttpBuilder(new HttpClientConfig()).build();
        return new DefaultKsefClient(apiClient, apiProperties, objectMapper());
    }

    @Bean
    public DefaultCryptographyService defaultCryptographyService(KSeFClient kSeFClient) {
        return new DefaultCryptographyService(kSeFClient);
    }

    @Bean
    public QrCodeService qrCodeService() {
        return new DefaultQrCodeService();
    }
}
