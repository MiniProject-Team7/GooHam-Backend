package com.uplus.ureka.repository.user.login;

import com.uplus.ureka.dto.user.login.LoginDTO;
import com.uplus.ureka.dto.user.member.MemberDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

@Repository
@Mapper
public interface LoginMapper {
   /* //로그인을 할 때 회원정보 조회 필요
    @Select("SELECT * FROM Member_table WHERE id = #{member_id}")
    MemberDTO selectMemberById(String id);*/

    //로그인을 할 때 회원정보 조회 필요 id = #{member_id}이 부분은 실제 컬럼이랑 동일해야함
    LoginDTO findByUsername(@Param("member_email") String username);

    MemberDTO findByUsername2(@Param("member_email") String username);

    void updateVerificationCode(@Param("member_email") String email, @Param("token") String token);

    void clearVerificationCode(@Param("member_email") String email);

    // 리프레시 토큰 저장 (update)
    void saveRefreshToken(@Param("member_email") String email, @Param("token") String token);

    // 리프레시 토큰 조회 (select)
    String getRefreshToken(@Param("member_email") String email);

    // 리프레시 토큰 삭제 (update)
    void deleteRefreshToken(@Param("member_email") String email);
}