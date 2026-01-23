package com.example;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 应用启动类
 */
@SpringBootApplication
@MapperScan("com.example.mapper") // 扫描MyBatis Mapper接口
public class SpringbootVue3DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringbootVue3DemoApplication.class, args);
    }

}