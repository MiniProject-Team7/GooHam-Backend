package com.uplus.ureka.repository.user.mypage;

import com.uplus.ureka.dto.user.Mypage.MyPageDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
@Mapper
public interface MyPageMapper {

    // 회원 정보를 ID를 통해 조회(MemberDetail 및 Member_table 조인)
    MyPageDTO selectMemberDetailsById(String memberId);

    // 이미지 업로드
    int updateProfileImageByMemberId(String memberId, byte[] profileImage);
    int updateProfileImage(MyPageDTO myPageDTO);

    // 회원 정보 수정(관심사 업데이트 경우 관심사 삭제하고 다시 insert)
    int updateMemberInfo(MyPageDTO memberInfo);

    // 회원의 관심사 목록 조회
    List<Integer> getMemberInterests(@Param("userId") String userId);

    // 관심사 추가
    void insertMemberInterests(@Param("userId") String userId, @Param("categoryId") Integer categoryId);
    // 특정 관심사 삭제
    void deleteSpecificMemberInterest(@Param("userId") String userId, @Param("categoryId") Integer categoryId);

    // 프로필 이미지 조회
    String selectProfileImageByMemberId(String memberId);

}