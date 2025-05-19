package com.uplus.ureka.service.user.mypage;

import com.uplus.ureka.dto.user.Mypage.MyPageDTO;
import com.uplus.ureka.service.s3.S3Service;
import com.uplus.ureka.repository.user.mypage.MyPageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;


import java.util.List;

@Service
public class MyPageServicelmpl implements MyPageService{

    @Autowired
    private MyPageMapper mypageMapper;

    @Autowired
    private S3Service s3Service;

    //회원 데이터베이스에 저장된 데이터들을 가져옴
    @Override
    public MyPageDTO getMemberDetails(String member_email) {
        return mypageMapper.selectMemberDetailsById(member_email);
    }

    // S3 프로필 이미지 업데이트
    @Override
    public String updateProfileImage(String memberId, String fileName) {
        MyPageDTO myPageDTO = new MyPageDTO();
        myPageDTO.setId(memberId);
        myPageDTO.setProfile_image(fileName);
        mypageMapper.updateProfileImage(myPageDTO);
        return fileName;
    }


    //회원의 정보를 업데이트
    @Override
    @Transactional
    public int updateMemberInfo(MyPageDTO memberInfo) {
        mypageMapper.updateMemberInfo(memberInfo);
//        mypageMapper.updateMemberInfoDetail(memberInfo);

        // 관심사 처리
        if (memberInfo.getCategoryIds() != null && !memberInfo.getCategoryIds().isEmpty()) {
            // 기존 관심사 조회
            List<Integer> existingInterests = mypageMapper.getMemberInterests(memberInfo.getId());
            List<Integer> newInterests = memberInfo.getCategoryIds();

            // 삭제할 관심사 찾기 (기존에는 있었지만, 새로운 요청에는 없는 경우)
            for (Integer existingCategoryId : existingInterests) {
                if (!newInterests.contains(existingCategoryId)) {
                    mypageMapper.deleteSpecificMemberInterest(memberInfo.getId(), existingCategoryId);
                }
            }

            // 추가할 관심사 찾기 (새로운 요청에 있지만, 기존에는 없던 경우)
            for (Integer newCategoryId : newInterests) {
                if (!existingInterests.contains(newCategoryId)) {
                    mypageMapper.insertMemberInterests(memberInfo.getId(), newCategoryId);
                }
            }
        }

        return 1;
    }

    // 프로필 이미지
    @Override
    public String getProfileImageByMemberId(String memberId) {
        return mypageMapper.selectProfileImageByMemberId(memberId);
    }
}