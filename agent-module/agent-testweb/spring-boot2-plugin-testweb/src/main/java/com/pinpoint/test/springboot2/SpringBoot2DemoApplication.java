package com.pinpoint.test.springboot2;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringBoot2DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot2DemoApplication.class, args);
    }

}
