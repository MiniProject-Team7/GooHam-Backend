package com.uplus.ureka.service.user.password;

import com.uplus.ureka.exception.CustomExceptions;
import com.uplus.ureka.repository.user.password.MemberMapper_Pass;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MemberService_pass {

    private final MemberMapper_Pass mapper;

    public MemberService_pass(MemberMapper_Pass mapper){
        this.mapper = mapper;
    }

    public void updateCode(String email, int code) {
        if (emailNotExist(email)) {
            throw new IllegalArgumentException("Email not found");
        }
        mapper.updateVerificationCode(email, code);
    }

    public String verifyCodeAndGetPassword(String email, int code) {
        if (emailNotExist(email)) {
            throw new CustomExceptions("Email not found");
        }

        int valid = mapper.isVerificationCodeValid(email, code);

        if (valid == 0) {
            return "인증번호가 일치하지 않습니다.";
        }

        return "인증되었습니다.";
    }


    private boolean emailNotExist(String email) {
        // 이메일이 존재하는지 확인하는 코드 작성
        return mapper.findUserByEmail(email) == null;

    }
    public Integer checkUserStatus(String email) {
        return mapper.getDelflagByEmail(email);
    }

    public boolean checkEmailExists(String email) {
        return Optional.ofNullable(mapper.findUserByEmail(email)).isPresent();
    }
}