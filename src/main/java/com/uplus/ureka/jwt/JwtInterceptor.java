package com.uplus.ureka.jwt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import com.uplus.ureka.exception.*;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

// JWT를 이용한 인터셉터 구현
@Component
public class JwtInterceptor implements HandlerInterceptor {
    private static final Logger logger = LoggerFactory.getLogger(JwtInterceptor.class);

    private final JwtUtils jwtUtils; //JWT 유틸리티 객체 주입

    @Autowired
    public JwtInterceptor(JwtUtils jwtUtils) {
        this.jwtUtils = jwtUtils;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

//        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
//            return true;
//        }
        String uri = request.getRequestURI();
        System.out.println("uri:" + uri);

        // 토큰 받기
        System.out.println("preHandle 실행");
        // 요청이 들어오면 실행되는 메서드
        String accessToken = jwtUtils.getAccessToken(request); //헤더에서 액세스 토큰을 가져옴
        System.out.println("Interceptor accessToken : " + accessToken); //요청 url 로깅을 위해 가져옴
        //로깅용 URI
        String requestURI = request.getRequestURI();

        // 비회원일 때(액세스 토큰이 없을 때)
        if (accessToken == null) {
            logger.debug("비회원 유저입니다 URI : {}", requestURI);
            throw new AuthException("UnAuthorized - Missing JWT Token");
        } else {
            logger.debug("access 존재합니다.");
            System.out.println("access 존재합니다.");
            // 액세스 토큰이 유효 시
            if (jwtUtils.validateToken(accessToken)) {
                logger.debug("유효한 토큰 정보입니다. URI : {}", requestURI);
                System.out.println("유효" + requestURI);
                return true;
            } else {
                //액세스 토큰이 유효하지 않을 시
                logger.error("Invalid token: " + accessToken);
                throw new AuthException("Unauthorized - Invalid JWT token");
            }
        }
    }
}