package com.uplus.ureka.controller.user;

import com.uplus.ureka.dto.user.member.MemberDTO;
import com.uplus.ureka.exception.CustomExceptions;
import com.uplus.ureka.exception.LoginException;
import com.uplus.ureka.jwt.JwtUtils;
import com.uplus.ureka.service.user.login.LoginService;
import com.uplus.ureka.service.user.login.LoginServiceImpl;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static io.netty.handler.codec.http.HttpStatusClass.SUCCESS;

@RestController
@RequestMapping("/gooham/users")
public class LoginController {

    // AuthenticationManager를 스프링에서 자동으로 주입받아 사용
    // 사용자 인증을 위해 필요합니다.
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    LoginService loginService;

    // JWT 토큰 생성을 위해 필요
    @Autowired
    private JwtUtils jwtUtils;
    @Autowired
    private LoginServiceImpl loginServiceImpl;


    private Logger logger = LoggerFactory.getLogger(getClass());


    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody MemberDTO memberDTO){
        try {
            MemberDTO authenticatedMember = loginService.checkLogin(memberDTO.getMember_email(), memberDTO.getMember_password());

            String accessToken = jwtUtils.createAccessToken(authenticatedMember.getMember_email());
            String refreshToken = jwtUtils.createRefreshToken(authenticatedMember.getMember_email());

            loginServiceImpl.saveVerificationToken(authenticatedMember.getMember_email(), refreshToken);

            Map<String, Object> userData = new HashMap<>();
            userData.put("member_email", authenticatedMember.getMember_email());
            userData.put("member_name", authenticatedMember.getMember_name());
            userData.put("user_id", authenticatedMember.getId());

            Map<String, Object> data = new HashMap<>();
            data.put("token", accessToken);
            data.put("user", userData);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "로그인 성공");
            response.put("data", data);

            logger.info("Authenticated Member ID: " + authenticatedMember.getMember_email());

            // accessToken을 쿠키에 담아 보내기
            ResponseCookie refreshCookie = ResponseCookie.from("Refresh_Token", refreshToken)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Lax")
                    .maxAge(60 * 60 * 24 * 7) // 7일
                    .build();

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body(response);

        }
        catch (LoginException e){
            logger.error("Login failed: {}", e.getMessage());
            // 로그인 실패 시 형식 맞춤
            Map<String, Object> response = new HashMap<>();
            response.put("status", "fail");
            response.put("message", e.getMessage());
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        catch (CustomExceptions e){
            logger.error("Authentication failed: {}", e.getMessage());
            // 인증 실패 형식 맞춤
            Map<String, Object> response = new HashMap<>();
            response.put("status", "fail");
            response.put("message", "인증 실패 : 이메일이나 비밀번호를 확인해주세요");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
        }
        catch(Exception e){
            logger.error("Login error: ", e); // 전체 스택 트레이스를 출
            logger.info("로그인 오류 상세: {}", e.getMessage());

            // 서버 오류 형식 맞춤
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "서버 내부 오류");
            response.put("data", null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    // JWT 토큰을 담을 내부 클래스를 정의
    @Getter
    @Setter
    class JwtResponse {
        private String token;

        // 생성자를 통해 토큰을 초기화
        public JwtResponse(String token) {
            this.token = token;
        }
    }

    @GetMapping("test")
    public String test() {
        return "토큰 테스트";
    }

    // 로그아웃
    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("member_email");

            // DB에서 refreshToken 삭제
            loginServiceImpl.logout(email);

            // 쿠키 제거
            ResponseCookie deleteCookie = ResponseCookie.from("Refresh_Token", "")
                    .path("/")
                    .maxAge(0)
                    .httpOnly(true)
                    .secure(false)
                    .sameSite("Lax")
                    .build();

            // 응답 생성
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "로그아웃 성공");
            response.put("data", null);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                    .body(response);
        } catch (Exception e) {
            e.printStackTrace();

            // 오류 응답
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "로그아웃 중 오류 발생: " + e.getMessage());
            response.put("data", null);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private final String HEADER_AUTH = "Authorization";
    private final String REFRESH_COOKIE = "Refresh_Token";

    @Value("${jwt.access-token.expiretime}")
    private long accessTokenExpireTime;

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh( @RequestBody MemberDTO member
            , @CookieValue(REFRESH_COOKIE) String refreshToken) {

        logger.debug("refresh..............................refreshToken:{}", refreshToken);
        String id = member.getMember_email();
        Map<String, Object> result = new HashMap<>();
        HttpStatus status = HttpStatus.OK;
        HttpHeaders headers = new HttpHeaders();
        try {
            String myRefresh = loginService.getRefreshToken(id);
            logger.debug("refresh..............................myRefresh:{}", myRefresh);
            if(myRefresh.equals(refreshToken) && jwtUtils.validateToken(refreshToken)) {
                String accessToken = jwtUtils.createAccessToken(member.getMember_email());
                logger.debug("re id:{}  accessToken:{}", member.getMember_email(),  accessToken);
                ResponseCookie cookie = ResponseCookie.from("accessToken", accessToken)
                        .httpOnly(true)
                        .secure(false) // 배포 환경에서는 true로 변경하기
                        .sameSite("Lax")
                        .path("/")
                        .maxAge(Duration.ofMillis(accessTokenExpireTime)) // 적절한 시간 설정
                        .build();

                headers.add(HttpHeaders.SET_COOKIE, cookie.toString());
                result.put("message", SUCCESS);
            }else {
                logger.error("유효하지 않은 토큰");
                result.put("message", "유효하지 않은 토큰");
                status = HttpStatus.INTERNAL_SERVER_ERROR;
            }
        }catch (Exception e) {
            logger.error("refresh 토큰 생성 실패:{}", e);
            result.put("message", e.getMessage());
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }
        return new ResponseEntity<Map<String, Object>>(result, headers, status);
    }

    public boolean isAuth(String userId, String authorHeader) {
        logger.debug("search - Authorization:{}", authorHeader);
        String token = authorHeader.replace("Bearer ", "");
        if (!jwtUtils.validateToken(token))
            return false;
        String tokenId = jwtUtils.getUserEmail(authorHeader.replace("Bearer ", ""));
        if (userId.equals(tokenId)) {
            return true;
        } else {
            return false;
        }
    }


}