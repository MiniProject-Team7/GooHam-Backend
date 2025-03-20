package com.uplus.ureka.repository.user.member;

import com.uplus.ureka.dto.user.member.MemberDTO;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface MemberMapper {

    //회원가입
    int insert(MemberDTO memberDTO);

    //아이디 중복검사
    boolean isIdDuplicated(String member_nickname);

    //이메일 중복검사
    boolean isEmailDuplicated(String member_email);
}