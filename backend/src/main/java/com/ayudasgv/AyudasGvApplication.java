package com.ayudasgv;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy
public class AyudasGvApplication {

    public static void main(String[] args) {
        SpringApplication.run(AyudasGvApplication.class, args);
    }
}
