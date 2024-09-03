package com.product.productservice.config;

/*
import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

@TestConfiguration
public class WireMockConfig {
    @Bean
    public WireMockServer wireMockServer() {
        WireMockConfiguration config = WireMockConfiguration.wireMockConfig().port(8081);
        WireMockServer wireMockServer = new WireMockServer(config);
        wireMockServer.start();
        return wireMockServer;
    }

    @Bean
    public WireMock wireMock(WireMockServer wireMockServer) {
        return new WireMock(wireMockServer.port());
    }

}

 */