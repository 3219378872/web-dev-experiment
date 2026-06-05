package com.hmall.api.client.fallback;

import com.hmall.api.client.OrderClient;
import com.hmall.api.dto.OrderDTO;
import com.hmall.common.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderClientFallbackFactory implements FallbackFactory<OrderClient> {

    @Override
    public OrderClient create(Throwable cause) {
        log.error("调用 trade-service 失败，触发降级", cause);
        return new OrderClient() {
            @Override
            public OrderDTO queryOrderById(Long id) {
                log.error("订单服务不可用，查询订单[id={}]失败", id, cause);
                return null;
            }

            @Override
            public void updateById(OrderDTO orderDTO) {
                log.error("订单服务不可用，更新订单失败", cause);
                throw new BizIllegalException("订单服务不可用，请稍后重试");
            }
        };
    }
}
