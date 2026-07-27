package com.fintech.insights;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class InsightsServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(InsightsServiceApplication.class, args);
    }
}
