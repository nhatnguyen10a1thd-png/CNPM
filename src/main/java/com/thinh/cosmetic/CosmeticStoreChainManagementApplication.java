package com.thinh.cosmetic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableAsync
@SpringBootApplication
public class CosmeticStoreChainManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(CosmeticStoreChainManagementApplication.class, args);
    }

}
