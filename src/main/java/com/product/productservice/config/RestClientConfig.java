package com.product.productservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;


@Configuration
public class RestClientConfig {
    @Bean
    RestClient restClient() {
        return RestClient.builder().baseUrl("http://localhost:8081").build();
    }
}
