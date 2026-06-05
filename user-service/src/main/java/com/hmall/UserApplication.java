package com.hmall;

import com.hmall.api.client.ItemClient;
import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@MapperScan({"com.hmall.mapper"})
@SpringBootApplication
@EnableFeignClients(basePackageClasses = {ItemClient.class}, defaultConfiguration = {DefaultFeignConfig.class})
class UserApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserApplication.class, args);
    }

}
