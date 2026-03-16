package com.smartforum;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.smartforum.mapper")
public class SmartForumApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartForumApplication.class, args);
    }
}
