package com.basecode.ksef.notifier.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import pl.akmf.ksef.sdk.api.KsefApiProperties;

@Setter
@Configuration
@ConfigurationProperties(prefix = "ksef.config")
public class DefaultKsefApiProperties extends KsefApiProperties {

    private String baseUri;
    private String suffixUri;
    private String qrUri;
    private int requestTimeout;
    private Map<String, String> defaultHeaders;

    @Override
    public String getBaseUri() {
        return baseUri;
    }

    @Override
    public String getSuffixUri() {
        return suffixUri;
    }

    @Override
    public String getQrUri() {
        return qrUri;
    }

    @Override
    public Duration getRequestTimeout() {
        return Duration.ofSeconds(requestTimeout);
    }

    @Override
    public Map<String, String> getDefaultHeaders() {
        if (defaultHeaders == null) {
            defaultHeaders = new HashMap<>();
        }
        return defaultHeaders;
    }
}
