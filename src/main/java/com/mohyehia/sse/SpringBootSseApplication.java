package com.mohyehia.sse;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@SpringBootApplication
@EnableWebSecurity
public class SpringBootSseApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootSseApplication.class, args);
    }

}
