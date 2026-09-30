package com.example.authpoc;

import com.example.authpoc.security.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@EnableConfigurationProperties(RsaKeyProperties.class)
@SpringBootApplication
public class AuthPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthPocApplication.class, args);
    }

}
