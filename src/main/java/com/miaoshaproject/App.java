package com.miaoshaproject;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 */
@SpringBootApplication(scanBasePackages = {"com.miaoshaproject"})//被spring托管
@RestController
@MapperScan("com.miaoshaproject.dao")
public class App {


    public static void main(String[] args) {
        SpringApplication.run(App.class);
    }
}
