package com.hmall;

import com.hmall.api.client.CartClient;
import com.hmall.api.client.ItemClient;
import com.hmall.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan({"com.hmall.mapper"})
@EnableFeignClients(basePackages = {"com.hmall.api.client"},defaultConfiguration = {DefaultFeignConfig.class})
class TradeApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradeApplication.class, args);
    }

}
