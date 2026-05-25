package com.hmall.api.config;


import com.hmall.common.utils.UserContext;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.context.annotation.Bean;

import java.util.Objects;

public class DefaultFeignConfig {
    @Bean
    public RequestInterceptor userInfoRequestInterceptor(){
        return new RequestInterceptor() {
            @Override
            public void apply(RequestTemplate requestTemplate) {
                Long userInfo = UserContext.getUser();
                if(Objects.nonNull(userInfo)){
                    requestTemplate.header("user-info", userInfo.toString() );
                }

            }
        };
    }
}
