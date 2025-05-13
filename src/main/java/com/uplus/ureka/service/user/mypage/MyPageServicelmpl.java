package com.uplus.ureka.service.user.mypage;

import com.uplus.ureka.dto.user.Mypage.InterestDTO;
import com.uplus.ureka.dto.user.Mypage.MyPageDTO;
import com.uplus.ureka.service.s3.S3Service;
import com.uplus.ureka.repository.user.mypage.MyPageMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    // 회원의 프로필 이미지를 업데이트
    @Override
    public String updateProfileImage(String memberId, MultipartFile profileImage) {
        if (profileImage == null || profileImage.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        String pathJson = s3Service.uploadFile(List.of(profileImage), "user");
        List<String> paths;
        try {
            paths = new ObjectMapper().readValue(pathJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new RuntimeException("S3 업로드 경로 파싱 실패", e);
        }

        String profilePath = paths.get(0); // 단일 파일만 업로드
        MyPageDTO dto = new MyPageDTO();
        dto.setId(memberId); // id가 DB에서 userId
        dto.setProfile_image(profilePath);
        mypageMapper.updateProfileImage(dto);

        return profilePath;
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

    // 네비게이션에 이미지 띄우기
    @Override
    public String getProfileImageByMemberId(String memberId) {
        return mypageMapper.selectProfileImageByMemberId(memberId);
    }
}