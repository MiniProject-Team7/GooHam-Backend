package com.uplus.ureka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer;

@Configuration
public class MyBatisConfig {
    @Bean
    public ConfigurationCustomizer jsonListTypeHandlerCustomizer() {
        return new ConfigurationCustomizer() {
            @Override
            public void customize(org.apache.ibatis.session.Configuration configuration) {
                // JsonListTypeHandler 등록
                configuration.getTypeHandlerRegistry()
                        .register(JsonListTypeHandler.class);
            }
        };
    }
}