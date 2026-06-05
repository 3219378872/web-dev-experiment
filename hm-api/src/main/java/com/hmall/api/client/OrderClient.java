package com.hmall.api.client;

import com.hmall.api.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "trade-service", contextId = "orderClient")
public interface OrderClient {
    @GetMapping("/orders/{id}")
    OrderDTO queryOrderById(@PathVariable("id") Long id);

    @PutMapping("/users")
    void updateById(@RequestBody OrderDTO orderDTO);
}
