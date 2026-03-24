package org.example.spqr;


import org.example.spqr.config.RestConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.context.annotation.Import;

@Import(RestConfig.class)
public class WebApp {
    public static void main(String[] args) {
        SpringApplication.run(WebApp.class, args);
    }
}
