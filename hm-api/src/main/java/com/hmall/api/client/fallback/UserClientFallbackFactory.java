package com.hmall.api.client.fallback;

import com.hmall.api.client.UserClient;
import com.hmall.common.exception.BizIllegalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserClientFallbackFactory implements FallbackFactory<UserClient> {

    @Override
    public UserClient create(Throwable cause) {
        log.error("调用 user-service 失败，触发降级", cause);
        return new UserClient() {
            @Override
            public void deductMoney(String pw, Integer amount) {
                log.error("用户服务不可用，扣减余额失败", cause);
                throw new BizIllegalException("用户服务不可用，请稍后重试");
            }

            @Override
            public Long countNewUsers(Integer days) {
                log.error("用户服务不可用，统计新用户失败，返回0", cause);
                return 0L;
            }
        };
    }
}
