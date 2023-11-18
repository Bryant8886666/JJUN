package com;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;


//@SpringBootApplication(scanBasePackages = {"com.two.generator","com.two.generator.exception"})
//@MapperScan("com.two.generator.mapper")
@SpringBootApplication(scanBasePackages = {"com.two.exception","com.two"})
@MapperScan("com.two.mapper")
@EnableScheduling
public class OneApplication {

    public static void main(String[] args) {
        SpringApplication.run(OneApplication.class, args);
    }

}
