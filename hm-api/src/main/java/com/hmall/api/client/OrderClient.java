package com.hmall.api.client;

import com.hmall.api.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient("order-service")
public interface OrderClient {
    @PutMapping("/users")
    void updateById(@RequestBody OrderDTO orderDTO);
}
