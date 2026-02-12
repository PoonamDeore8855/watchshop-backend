package com.watchshop.watchshop_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class WatchshopBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(WatchshopBackendApplication.class, args);
        System.out.println("Watchshop Backend Started Successfully");
    }
}
