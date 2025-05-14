package com.uplus.ureka.config;

import com.uplus.ureka.jwt.JwtInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/gooham/**")  // 모든 /gooham/ 경로에 대해 인터셉터 적용
                .excludePathPatterns(
                        "/gooham/users/login",      // 로그인
                        "/gooham/users/join",   // 회원가입
                        "/gooham/users/generateCode", // 비밀번호 찾기
                        "/error"                  // Spring Boot 기본 에러 경로
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns("http://localhost:3000") // 또는 "*"
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
