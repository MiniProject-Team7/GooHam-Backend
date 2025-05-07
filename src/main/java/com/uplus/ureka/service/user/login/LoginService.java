package com.uplus.ureka.service.user.login;

import com.uplus.ureka.dto.user.member.MemberDTO;
import com.uplus.ureka.exception.LoginException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


public interface LoginService { // username(UserDetailsService 인터페이스의 일부로서, Spring Security에서 제공)은 user id
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;
    public MemberDTO checkLogin(String username, String password) throws LoginException;

    void saveRefreshToken(String userEmail, String refreshToken) throws Exception;

    String getRefreshToken(String userEmail) throws Exception;

    void delRefreshToken(String userEmail) throws Exception;
}
