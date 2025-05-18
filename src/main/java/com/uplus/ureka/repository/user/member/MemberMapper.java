package com.uplus.ureka.repository.user.member;

import com.uplus.ureka.dto.user.member.MemberSignupDTO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MemberMapper {

    //회원가입
    int insert(MemberSignupDTO memberDTO);

    //아이디 중복검사
    boolean isIdDuplicated(String member_nickname);

    //이메일 중복검사
    boolean isEmailDuplicated(String member_email);

    //  관심사(카테고리) 추가 (USER_INTERESTS insert)
    void insertMemberInterests(MemberSignupDTO memberDTO);
}