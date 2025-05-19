package com.uplus.ureka.service.user.member;

import com.uplus.ureka.dto.user.member.MemberDTO;
import com.uplus.ureka.dto.user.member.MemberSignupDTO;
import com.uplus.ureka.repository.user.member.MemberMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class MemberServiceImpl implements MemberService{
    private  static final Logger logger
            = LoggerFactory.getLogger(MemberServiceImpl.class);
    @Autowired
    private  MemberMapper memberMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;


    //암호화 적용

    @Override
    public boolean register(String member_email, String member_password, String member_name,
                            String member_nickname, String member_phone, String member_introduce,
                            LocalDate birth_date, String profile_image, List<Integer> categoryIds) {

        MemberSignupDTO memberDTO = new MemberSignupDTO();
        logger.info("암호화 전 비밀번호 : "+ member_password);
        String encodedPassword = passwordEncoder.encode(member_password);
        logger.info("암호화 후 비밀번호 : "+ encodedPassword);
        memberDTO.setMember_password(encodedPassword);
        memberDTO.setMember_name(member_name);
        memberDTO.setMember_nickname(member_nickname);
        memberDTO.setMember_email(member_email);
        memberDTO.setMember_phone(member_phone);
        memberDTO.setMember_introduce(member_introduce);
        memberDTO.setBirth_date(birth_date);
        memberDTO.setProfile_image(profile_image);
        memberDTO.setCategoryIds(categoryIds);

        int result = memberMapper.insert(memberDTO);

        if (result == 1 && categoryIds != null && !categoryIds.isEmpty()) {
            memberMapper.insertMemberInterests(memberDTO);
        }

        return result == 1;
    }


    /* 닉네임 중복 검사 */
    @Override
    public boolean isIdDuplicated(String member_nickname) {
        logger.info("서비스 넘어온 nickname : "+ member_nickname);
        return memberMapper.isIdDuplicated(member_nickname);
    }


    /* 이메일 중복 검사 */
    @Override
    public boolean isEmailDuplicated(String member_email) {
        logger.info("서비스 넘어온 email : "+ member_email);
        return memberMapper.isEmailDuplicated(member_email);
    }

}