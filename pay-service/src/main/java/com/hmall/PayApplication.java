package com.hmall;

import com.hmall.api.client.OrderClient;
import com.hmall.api.client.UserClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@MapperScan({"com.hmall.mapper"})
@EnableFeignClients(basePackageClasses = {OrderClient.class, UserClient.class})
class PayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }

}
