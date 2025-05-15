package com.hmall.api.log;

import feign.Logger;
import org.springframework.context.annotation.Bean;

public class OpenFeignConfiguration {
    @Bean
    public Logger.Level openFeignLogLevel(){
        return Logger.Level.FULL;
    }
}
