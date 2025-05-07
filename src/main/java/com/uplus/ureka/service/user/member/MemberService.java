package com.uplus.ureka.service.user.member;

import com.uplus.ureka.dto.user.member.MemberDTO;

import java.time.LocalDate;

public interface MemberService {

    //회원가입
    public boolean register(String member_email, String member_password, String member_name,
                            String member_nickname, String member_phone, String member_introduce,
                            LocalDate birth_date);

    //아이디 중복 검사
    boolean isIdDuplicated(String member_nickname);

    //이메일 중복 검사
    boolean isEmailDuplicated(String member_email);
}