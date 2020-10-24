package com.xatu.easyChat.utils.config;

import com.xatu.easyChat.utils.IdWorker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IDConfig {
    @Bean
    public IdWorker getIdWorker() {
        return new IdWorker();
    }


}
