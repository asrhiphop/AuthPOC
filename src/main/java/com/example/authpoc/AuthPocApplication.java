package com.example.authpoc;

import com.example.authpoc.security.config.AdminProperties;
import com.example.authpoc.security.config.JwtProperties;
import com.example.authpoc.security.config.LockoutProperties;
import com.example.authpoc.security.config.RsaKeyProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableConfigurationProperties({
        RsaKeyProperties.class,
        AdminProperties.class,
        JwtProperties.class,
        LockoutProperties.class
})
@EnableScheduling
@SpringBootApplication
public class AuthPocApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthPocApplication.class, args);
    }

}
