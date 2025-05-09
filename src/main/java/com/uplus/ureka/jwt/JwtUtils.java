package com.uplus.ureka.jwt;

import com.uplus.ureka.exception.AuthException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import jakarta.servlet.http.HttpServletRequest;  // 여기를 javax에서 jakarta로 변경

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtils {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtils.class);

    @Value("${jwt.secret}")
    private String SALT;

    //accessToken 만료시간 설정
    public final static long ACCESS_TOKEN_VALIDATION_SECOND = 1000L*60*60*12; //12시간
    public final static long REFRESH_TOKEN_VALIDATION_SECOND = 1000L*60*60*24*30; //12시간
    public static final String AUTHORIZATION_HEADER = "Authorization"; //헤더 이름

    private SecretKey getSigningKey() {
        byte[] keyBytes = SALT.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // 공통 JWT 생성 메서드
    private String generateToken(String subject, String tokenType, long validityMillis) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + validityMillis);

        return Jwts.builder()
                .setSubject(subject)
                .claim("tokenType", tokenType)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(getSigningKey())
                .compact();
    }

    // Access Token 생성
    public String createAccessToken(String userEmail) {
        return generateToken(userEmail, "ACCESS", ACCESS_TOKEN_VALIDATION_SECOND);
    }

    // Refresh Token 생성
    public String createRefreshToken(String userEmail) {
        return generateToken(userEmail, "REFRESH", REFRESH_TOKEN_VALIDATION_SECOND);
    }

    //토큰 유효성 검증 메서드
    public boolean validateToken(String token){
        //토큰 파싱 후 발생하는 예외를 캐치하여 문제가 있으면 false, 정상이면 true 반환
        try{
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        }
        catch (SignatureException e){
            // 서명이 옳지 않을 때
            System.out.println("잘못된 토큰 서명입니다.");
        }
        catch (ExpiredJwtException e){
            // 토큰이 만료됐을 때
            System.out.println("만료된 토큰입니다.");
        }
        catch(IllegalArgumentException | MalformedJwtException e){
            // 토큰이 올바르게 구성되지 않았을 때 처리
            System.out.println("잘못된 토큰입니다.");
        }
        return false;
    }

    // 토큰에서 member_id를 추출하여 반환하는 메소드
    public String getUserEmail(String token){
        System.out.println("getUserEmail");

        return Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token).getBody().getSubject();
    }

    // HttpServletRequest에서 Authorization Header를 통해 access token을 추출하는 메서드입니다.
    public String getAccessToken(HttpServletRequest httpServletRequest) {
        String bearerToken = httpServletRequest.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    ////TODO 10. Access Token 만료시 Refresh Token을 이용해 새로운 Access Token 발급하는 메서드 작성하기
    public String generateAccessTokenFromRefreshToken(String refreshToken) {
        if (!validateToken(refreshToken)) {
            throw new AuthException("세션이 만료되었습니다."); // Refresh Token이 유효하지 않으면 예외 발생
        }
        String userId = getUserEmail(refreshToken);
        return createAccessToken(userId); // 새로운 Access Token 생성
    }


    public String determineRedirectURI(HttpServletRequest httpServletRequest, String memberURI, String nonMemberURI) {
        String token = getAccessToken(httpServletRequest);
        if (token == null) {
            return nonMemberURI; // 비회원용 URI로 리다이렉트
        } else {
            return memberURI; // 회원용 URI로 리다이렉트
        }
    }
}