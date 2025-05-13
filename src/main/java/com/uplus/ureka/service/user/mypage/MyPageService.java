package com.uplus.ureka.service.user.mypage;

import com.uplus.ureka.dto.user.Mypage.MyPageDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface MyPageService {

    // 특정 회원의 상세 정보
    MyPageDTO getMemberDetails(String member_email);

    // 회원의 프로필 이미지를 업데이트
   // String updateProfileImage(String memberId, MultipartFile profileImage);

    // S3 프로필 이미지 업데이트
    String updateProfileImage(String id, String fileName);


    // 회원의 상세 정보를 업데이트
    int updateMemberInfo(MyPageDTO memberInfo);

    //네비게이션에 이미지 띄우기
    //String getProfileImageByMemberId(String memberId);

    String getProfileImageByMemberId(String memberId);


}