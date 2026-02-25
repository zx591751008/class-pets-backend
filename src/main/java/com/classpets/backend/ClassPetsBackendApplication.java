package com.classpets.backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@MapperScan("com.classpets.backend.mapper")
@EnableScheduling
public class ClassPetsBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(ClassPetsBackendApplication.class, args);
    }
}
