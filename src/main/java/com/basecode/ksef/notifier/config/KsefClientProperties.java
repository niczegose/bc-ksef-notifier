package com.basecode.ksef.notifier.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "ksef.context")
public class KsefClientProperties {

    private String nip;
    private String token;
    private String notificationEmail;
}
