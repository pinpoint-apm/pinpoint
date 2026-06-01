package com.pinpoint.test.springboot4;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class SpringBoot4DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBoot4DemoApplication.class, args);
    }

}
