package com.wjl.hotel3;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;


@EnableJpaRepositories
@EnableTransactionManagement
@SpringBootApplication
public class Hotel3Application {

    public static void main(String[] args) {
        SpringApplication.run(Hotel3Application.class, args);
    }

}
